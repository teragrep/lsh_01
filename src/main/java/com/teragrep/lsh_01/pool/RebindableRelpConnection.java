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

import java.io.IOException;

public class RebindableRelpConnection implements IManagedRelpConnection {

    private static final Logger LOGGER = LogManager.getLogger(RebindableRelpConnection.class);

    private final IManagedRelpConnection managedRelpConnection;
    private int recordsSent;
    private final int rebindRequestAmount;

    public RebindableRelpConnection(IManagedRelpConnection managedRelpConnection, int rebindRequestAmount) {
        this.managedRelpConnection = managedRelpConnection;
        this.recordsSent = 0;
        this.rebindRequestAmount = rebindRequestAmount;
    }

    @Override
    public void ensureSent(byte[] bytes) {
        if (recordsSent >= rebindRequestAmount) {
            LOGGER.debug("Rebinding ManagedRelpConnection <{}>", managedRelpConnection);
            try {
                close();
            }
            catch (Exception exception) {
                LOGGER
                        .warn(
                                "Exception <{}> while closing ManagedRelpConnection <{}>", exception.getMessage(),
                                managedRelpConnection
                        );
            }
            recordsSent = 0;
        }
        managedRelpConnection.ensureSent(bytes);
        recordsSent++;
    }

    @Override
    public boolean isStub() {
        return false;
    }

    @Override
    public void close() throws IOException {
        managedRelpConnection.close();
    }
}
