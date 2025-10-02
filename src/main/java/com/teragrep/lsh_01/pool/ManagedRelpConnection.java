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

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.teragrep.rlp_01.RelpBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static com.codahale.metrics.MetricRegistry.name;

public class ManagedRelpConnection implements IManagedRelpConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedRelpConnection.class);
    private final IRelpConnection relpConnection;
    private boolean hasConnected;

    // metrics
    private final Counter records;
    private final Counter bytes;
    private final Counter resends;
    private final Counter retriedConnects;

    public ManagedRelpConnection(IRelpConnection relpConnection, MetricRegistry metricRegistry) {
        this.relpConnection = relpConnection;
        this.hasConnected = false;

        this.records = metricRegistry.counter(name(ManagedRelpConnection.class, "records"));
        this.bytes = metricRegistry.counter(name(ManagedRelpConnection.class, "bytes"));
        this.resends = metricRegistry.counter(name(ManagedRelpConnection.class, "resends"));
        this.retriedConnects = metricRegistry.counter(name(ManagedRelpConnection.class, "retriedConnects"));
    }

    private void connect() {
        boolean connected = false;
        while (!connected) {
            try {
                this.hasConnected = true;
                connected = relpConnection
                        .connect(relpConnection.relpConfig().relpTarget, relpConnection.relpConfig().relpPort);
            }
            catch (Exception e) {
                LOGGER
                        .error(
                                "Failed to connect to relp server <[{}]>:<[{}]>: {}",
                                relpConnection.relpConfig().relpTarget, relpConnection.relpConfig().relpPort,
                                e.getMessage()
                        );

                try {
                    retriedConnects.inc();
                    Thread.sleep(relpConnection.relpConfig().relpReconnectInterval);
                }
                catch (InterruptedException exception) {
                    LOGGER.error("Reconnection timer interrupted, reconnecting now");
                }
            }
        }
    }

    private void tearDown() {
        /*
         TODO remove: wouldn't need a check hasConnected but there is a bug in RLP-01 tearDown()
         see https://github.com/teragrep/rlp_01/issues/63 for further info
         */
        if (hasConnected) {
            relpConnection.tearDown();
        }
    }

    @Override
    public void ensureSent(byte[] bytes) {
        final RelpBatch relpBatch = new RelpBatch();
        relpBatch.insert(bytes);
        boolean notSent = true;
        while (notSent) {
            try {
                relpConnection.commit(relpBatch);
            }
            catch (IllegalStateException | IOException | TimeoutException e) {
                LOGGER.error("Exception <{}> while sending relpBatch. Will retry", e.getMessage());
            }
            if (!relpBatch.verifyTransactionAll()) {
                relpBatch.retryAllFailed();
                this.tearDown();
                this.connect();
                resends.inc();
            }
            else {
                notSent = false;
            }
        }
        records.inc();
        this.bytes.inc(bytes.length);
    }

    @Override
    public boolean isStub() {
        return false;
    }

    @Override
    public void close() {
        try {
            this.relpConnection.disconnect();
        }
        catch (IllegalStateException | IOException | TimeoutException e) {
            LOGGER.error("Forcefully closing connection due to exception <{}>", e.getMessage());
        }
        finally {
            tearDown();
        }
    }
}
