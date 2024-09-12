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

package fakes;

import com.teragrep.lsh_01.config.RelpConfig;
import com.teragrep.lsh_01.pool.IRelpConnection;
import com.teragrep.rlp_01.RelpBatch;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Fake that resends data until the amount given is reached.
 */
public class ResendingRelpConnectionFake implements IRelpConnection {
    final private IRelpConnection relpConnection;
    final private int commitLimit;
    private int timesCommitted;

    public ResendingRelpConnectionFake(IRelpConnection relpConnection, int commitLimit) {
        this.relpConnection = relpConnection;
        this.commitLimit = commitLimit;
    }

    @Override
    public int getReadTimeout() {
        return relpConnection.getReadTimeout();
    }

    @Override
    public void setReadTimeout(int readTimeout) {
        relpConnection.setReadTimeout(readTimeout);
    }

    @Override
    public int getWriteTimeout() {
        return relpConnection.getWriteTimeout();
    }

    @Override
    public void setWriteTimeout(int writeTimeout) {
        relpConnection.setWriteTimeout(writeTimeout);
    }

    @Override
    public int getConnectionTimeout() {
        return relpConnection.getConnectionTimeout();
    }

    @Override
    public void setConnectionTimeout(int timeout) {
        relpConnection.setConnectionTimeout(timeout);
    }

    @Override
    public void setKeepAlive(boolean on) {
        relpConnection.setKeepAlive(on);
    }

    @Override
    public int getRxBufferSize() {
        return relpConnection.getRxBufferSize();
    }

    @Override
    public void setRxBufferSize(int size) {
        relpConnection.setRxBufferSize(size);
    }

    @Override
    public int getTxBufferSize() {
        return relpConnection.getTxBufferSize();
    }

    @Override
    public void setTxBufferSize(int size) {
        relpConnection.setTxBufferSize(size);
    }

    @Override
    public boolean connect(String hostname, int port) throws IOException, TimeoutException {
        return relpConnection.connect(hostname, port);
    }

    @Override
    public void tearDown() {
        relpConnection.tearDown();
    }

    @Override
    public boolean disconnect() throws IOException, IllegalStateException, TimeoutException {
        return relpConnection.disconnect();
    }

    @Override
    public void commit(RelpBatch relpBatch) throws IOException, IllegalStateException, TimeoutException {
        timesCommitted++;
        if (timesCommitted <= commitLimit) {
            // no-op, will lead to verifyTransactionAll to return false
        } else {
            relpConnection.commit(relpBatch);
        }
    }

    @Override
    public RelpConfig relpConfig() {
        return relpConnection.relpConfig();
    }
}
