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

import com.teragrep.rlp_01.RelpBatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ManagedRelpConnection {

    private static final Logger LOGGER = LogManager.getLogger(ManagedRelpConnection.class);
    private final IRelpConnection relpConnection;

    public ManagedRelpConnection(IRelpConnection relpConnection) {
        this.relpConnection = relpConnection;
    }

    private void connect() {
        boolean notConnected = true;
        while (notConnected) {
            boolean connected = false;
            try {
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
            }
            if (connected) {
                notConnected = false;
            }
            else {
                try {
                    Thread.sleep(relpConnection.relpConfig().relpReconnectInterval);
                }
                catch (InterruptedException e) {
                    LOGGER.error("Reconnect timer interrupted, reconnecting now");
                }
            }
        }
    }

    private void tearDown() {
        relpConnection.tearDown();
    }

    private void disconnect() {
        boolean disconnected = false;
        try {
            disconnected = relpConnection.disconnect();
        }
        catch (IllegalStateException | IOException | TimeoutException e) {
            LOGGER.error("Forcefully closing connection due to exception <{}>", e.getMessage());
        }
        finally {
            this.tearDown();
        }
    }

    public void ensureSent(byte[] bytes) {
        final RelpBatch relpBatch = new RelpBatch();
        relpBatch.insert(bytes);
        boolean notSent = true;
        while (notSent) {
            try {
                relpConnection.commit(relpBatch);
            }
            catch (IllegalStateException | IOException | TimeoutException e) {
                LOGGER.error("Failed to send relp message: <{}>", e.getMessage());
            }
            if (!relpBatch.verifyTransactionAll()) {
                relpBatch.retryAllFailed();
                this.tearDown();
                this.connect();
            }
            else {
                notSent = false;
            }
        }
    }
}
