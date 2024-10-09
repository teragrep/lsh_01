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

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class Pool<T extends Poolable> implements AutoCloseable, Supplier<T> {

    private static final Logger LOGGER = LogManager.getLogger(com.teragrep.lsh_01.pool.Pool.class);

    private final Supplier<T> supplier;

    private final ConcurrentLinkedQueue<T> queue;

    private final T stub;

    private final Lock lock = new ReentrantLock();

    private final AtomicBoolean close;

    public Pool(final Supplier<T> supplier, T stub) {
        this.supplier = supplier;
        this.queue = new ConcurrentLinkedQueue<>();
        this.stub = stub;
        this.close = new AtomicBoolean();
    }

    public T get() {
        T object;
        if (close.get()) {
            object = stub;
        }
        else {
            // get or create
            object = queue.poll();
            if (object == null) {
                object = supplier.get();
            }
        }

        return object;
    }

    public void offer(T object) {
        if (!object.isStub()) {
            queue.add(object);
        }

        if (close.get()) {
            while (queue.peek() != null) {
                if (lock.tryLock()) {
                    while (true) {
                        T pooled = queue.poll();
                        if (pooled == null) {
                            break;
                        }
                        else {
                            try {
                                LOGGER.debug("Closing poolable <{}>", pooled);
                                pooled.close();
                                LOGGER.debug("Closed poolable <{}>", pooled);
                            }
                            catch (Exception exception) {
                                LOGGER
                                        .warn(
                                                "Exception <{}> while closing poolable <{}>", exception.getMessage(),
                                                pooled
                                        );
                            }
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
        offer(stub);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final Pool<?> cast = (Pool<?>) o;
        return supplier.equals(cast.supplier) && queue.equals(cast.queue) && stub.equals(cast.stub)
                && lock.equals(cast.lock) && (close.get() == cast.close.get());
    }
}
