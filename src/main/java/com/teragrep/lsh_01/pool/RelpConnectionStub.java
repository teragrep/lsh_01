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

import com.teragrep.lsh_01.config.RelpConfig;
import com.teragrep.rlp_01.RelpBatch;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RelpConnectionStub implements IRelpConnection {

    @Override
    public int getReadTimeout() {
        throw new IllegalStateException("RelpConnectionStub does not support this");
    }

    @Override
    public void setReadTimeout(int readTimeout) {
        throw new IllegalStateException("RelpConnectionStub does not support this");
    }

    @Override
    public int getWriteTimeout() {
        throw new IllegalStateException("RelpConnectionStub does not support this");
    }

    @Override
    public void setWriteTimeout(int writeTimeout) {
        throw new IllegalStateException("RelpConnectionStub does not support this");
    }

    @Override
    public int getConnectionTimeout() {
        throw new IllegalStateException("RelpConnectionStub does not support this");
    }

    @Override
    public void setConnectionTimeout(int timeout) {
        throw new IllegalStateException("RelpConnectionStub does not support this");

    }

    @Override
    public void setKeepAlive(boolean on) {
        throw new IllegalStateException("RelpConnectionStub does not support this");

    }

    @Override
    public int getRxBufferSize() {
        throw new IllegalStateException("RelpConnectionStub does not support this");
    }

    @Override
    public void setRxBufferSize(int size) {
        throw new IllegalStateException("RelpConnectionStub does not support this");

    }

    @Override
    public int getTxBufferSize() {
        throw new IllegalStateException("RelpConnectionStub does not support this");
    }

    @Override
    public void setTxBufferSize(int size) {
        throw new IllegalStateException("RelpConnectionStub does not support this");

    }

    @Override
    public boolean connect(String hostname, int port) throws IOException, IllegalStateException, TimeoutException {
        throw new IllegalStateException("RelpConnectionStub does not support this");
    }

    @Override
    public void tearDown() {
        throw new IllegalStateException("RelpConnectionStub does not support this");

    }

    @Override
    public boolean disconnect() throws IOException, IllegalStateException, TimeoutException {
        throw new IllegalStateException("RelpConnectionStub does not support this");

    }

    @Override
    public void commit(RelpBatch relpBatch) throws IOException, IllegalStateException, TimeoutException {
        throw new IllegalStateException("RelpConnectionStub does not support this");
    }

    @Override
    public boolean isStub() {
        return true;
    }

    @Override
    public RelpConfig relpConfig() {
        throw new IllegalStateException("RelpConnectionStub does not support this");
    }

}
