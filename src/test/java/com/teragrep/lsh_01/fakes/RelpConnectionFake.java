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
package com.teragrep.lsh_01.fakes;

import com.teragrep.lsh_01.config.RelpConfig;
import com.teragrep.lsh_01.pool.IRelpConnection;
import com.teragrep.rlp_01.RelpBatch;

public final class RelpConnectionFake implements IRelpConnection {

    private final RelpConfig relpConfig;
    private final int sendLatency;
    private final int connectLatency;

    public RelpConnectionFake(RelpConfig relpConfig) {
        this(relpConfig, 0, 0);
    }

    public RelpConnectionFake(RelpConfig relpConfig, int sendLatency, int connectLatency) {
        this.relpConfig = relpConfig;
        this.sendLatency = sendLatency;
        this.connectLatency = connectLatency;
    }

    @Override
    public int getReadTimeout() {
        return 0;
    }

    @Override
    public void setReadTimeout(int readTimeout) {
        // no-op in fake
    }

    @Override
    public int getWriteTimeout() {
        return 0;
    }

    @Override
    public void setWriteTimeout(int writeTimeout) {
        // no-op in fake
    }

    @Override
    public int getConnectionTimeout() {
        return 0;
    }

    @Override
    public void setConnectionTimeout(int timeout) {
        // no-op in fake
    }

    @Override
    public void setKeepAlive(boolean on) {
        // no-op in fake
    }

    @Override
    public int getRxBufferSize() {
        return 0;
    }

    @Override
    public void setRxBufferSize(int size) {
        // no-op in fake
    }

    @Override
    public int getTxBufferSize() {
        return 0;
    }

    @Override
    public void setTxBufferSize(int size) {
        // no-op in fake
    }

    @Override
    public boolean connect(String hostname, int port) {
        try {
            Thread.sleep(connectLatency);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public void tearDown() {
        // no-op in fake
    }

    @Override
    public boolean disconnect() {
        return true;
    }

    @Override
    public void commit(RelpBatch relpBatch) {
        try {
            Thread.sleep(sendLatency);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // remove all the requests from relpBatch in the fake
        // so that the batch will return true in verifyTransactionAll()
        while (relpBatch.getWorkQueueLength() > 0) {
            long reqId = relpBatch.popWorkQueue();
            relpBatch.removeRequest(reqId);
        }
    }

    @Override
    public RelpConfig relpConfig() {
        return relpConfig;
    }
}
