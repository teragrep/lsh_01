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
import com.codahale.metrics.SlidingWindowReservoir;
import com.codahale.metrics.Timer;
import com.teragrep.lsh_01.config.RelpConfig;
import com.teragrep.rlp_01.RelpBatch;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Decorator for IRelpConnection. Responsible for reporting metrics.
 */
public class MetricRelpConnection implements IRelpConnection {

    private final IRelpConnection relpConnection;
    private final Counter connects;
    private final Timer connectLatency;

    public MetricRelpConnection(IRelpConnection relpConnection, MetricRegistry metricRegistry) {
        this.relpConnection = relpConnection;
        this.connects = metricRegistry.counter(name(MetricRelpConnection.class, "connects"));
        this.connectLatency = metricRegistry
                .timer(name(MetricRelpConnection.class, "connectLatency"), () -> new Timer(new SlidingWindowReservoir(10000)));
    }

    @Override
    public boolean connect(String hostname, int port) throws IOException, IllegalStateException, TimeoutException {
        final Timer.Context context = connectLatency.time(); // reset the time (new context)
        boolean connected = relpConnection.connect(hostname, port);
        /*
        Not closing the context in case of an exception thrown in .connect() will leave the timer.context
        for garbage collector to remove. This will happen even if the context is closed because of how
        the Timer is implemented.
         */
        context.close(); // manually close here, so the timer is only updated if no exceptions were thrown
        connects.inc();
        return connected;
    }

    @Override
    public int getReadTimeout() {
        return this.relpConnection.getReadTimeout();
    }

    @Override
    public void setReadTimeout(int readTimeout) {
        this.relpConnection.setReadTimeout(readTimeout);
    }

    @Override
    public int getWriteTimeout() {
        return this.relpConnection.getWriteTimeout();
    }

    @Override
    public void setWriteTimeout(int writeTimeout) {
        this.relpConnection.setWriteTimeout(writeTimeout);
    }

    @Override
    public int getConnectionTimeout() {
        return this.relpConnection.getConnectionTimeout();
    }

    @Override
    public void setConnectionTimeout(int timeout) {
        this.relpConnection.setConnectionTimeout(timeout);
    }

    @Override
    public void setKeepAlive(boolean on) {
        this.relpConnection.setKeepAlive(on);
    }

    @Override
    public int getRxBufferSize() {
        return this.relpConnection.getRxBufferSize();
    }

    @Override
    public void setRxBufferSize(int size) {
        this.relpConnection.setRxBufferSize(size);
    }

    @Override
    public int getTxBufferSize() {
        return this.relpConnection.getTxBufferSize();
    }

    @Override
    public void setTxBufferSize(int size) {
        this.relpConnection.setTxBufferSize(size);
    }

    @Override
    public void tearDown() {
        this.relpConnection.tearDown();
    }

    @Override
    public boolean disconnect() throws IOException, IllegalStateException, TimeoutException {
        return this.relpConnection.disconnect();
    }

    @Override
    public void commit(RelpBatch relpBatch) throws IOException, IllegalStateException, TimeoutException {
        this.relpConnection.commit(relpBatch);
    }

    @Override
    public RelpConfig relpConfig() {
        return this.relpConnection.relpConfig();
    }
}
