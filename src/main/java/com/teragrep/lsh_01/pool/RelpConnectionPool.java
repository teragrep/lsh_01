/*
  logstash-http-input to syslog bridge
  Copyright 2024 Suomen Kanuuna Oy

  Derivative Work of Elasticsearch
  Copyright 2012-2015 Elasticsearch

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.teragrep.lsh_01.pool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

// TODO create test cases
public class RelpConnectionPool implements AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger(RelpConnectionPool.class);

    private final Supplier<IRelpConnection> relpConnectionWrapSupplier;

    private final int rebindRequestAmount;

    private final boolean rebindEnabled;

    private final ConcurrentLinkedQueue<IRelpConnection> queue;

    // connection, how many times it has been used
    private final HashMap<IRelpConnection, Integer> connectionsTaken;

    private final RelpConnectionStub relpConnectionStub;

    private final Lock lock = new ReentrantLock();

    private final AtomicBoolean close;

    public RelpConnectionPool(
            final Supplier<IRelpConnection> relpConnectionWrapSupplier,
            int rebindRequestAmount,
            boolean rebindEnabled
    ) {
        this.relpConnectionWrapSupplier = relpConnectionWrapSupplier;
        this.rebindRequestAmount = rebindRequestAmount;
        this.rebindEnabled = rebindEnabled;
        this.queue = new ConcurrentLinkedQueue<>();
        this.connectionsTaken = new HashMap<>();
        this.relpConnectionStub = new RelpConnectionStub();
        this.close = new AtomicBoolean();

        // TODO maximum number of available connections should be perhaps limited?
    }

    public IRelpConnection take() {
        IRelpConnection frameDelegate;
        if (close.get()) {
            frameDelegate = relpConnectionStub;
        }
        else {
            // get or create
            frameDelegate = queue.poll();
            if (frameDelegate == null) {
                frameDelegate = relpConnectionWrapSupplier.get();
                connectionsTaken.put(frameDelegate, 1);
            }
            else if (connectionsTaken.get(frameDelegate) >= rebindRequestAmount && rebindEnabled) {
                // Rebind
                this.connectionTearDown(frameDelegate);
                frameDelegate = relpConnectionWrapSupplier.get();
                connectionsTaken.put(frameDelegate, 1);
            }
            else {
                // Reuse
                connectionsTaken.put(frameDelegate, connectionsTaken.get(frameDelegate) + 1);
            }
        }

        return frameDelegate;
    }

    public void offer(IRelpConnection iRelpConnection) {
        if (!iRelpConnection.isStub()) {
            queue.add(iRelpConnection);
        }

        if (close.get()) {
            while (queue.peek() != null) {
                if (lock.tryLock()) {
                    while (true) {
                        IRelpConnection queuedConnection = queue.poll();
                        if (queuedConnection == null) {
                            break;
                        }
                        else {
                            this.connectionTearDown(queuedConnection);
                        }
                    }
                    lock.unlock();
                }
                else {
                    break;
                }
            }
        }
    }

    public void close() {
        close.set(true);

        // close all that are in the pool right now
        offer(relpConnectionStub);
    }

    private void connectionTearDown(IRelpConnection connection) {
        try {
            LOGGER.debug("Closing frameDelegate <{}>", connection);
            connection.disconnect();
            LOGGER.debug("Closed frameDelegate <{}>", connection);
        }
        catch (Exception exception) {
            LOGGER.warn("Exception <{}> while closing frameDelegate <{}>", exception.getMessage(), connection);
        }
        finally {
            connection.tearDown();
        }
    }
}
