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

import com.codahale.metrics.MetricRegistry;
import com.teragrep.lsh_01.config.RelpConfig;
import com.teragrep.lsh_01.pool.IManagedRelpConnection;
import com.teragrep.lsh_01.pool.ManagedRelpConnection;
import com.teragrep.lsh_01.pool.MetricRelpConnection;
import com.teragrep.poj_01.pool.Pool;
import com.teragrep.poj_01.pool.PoolableSupplier;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class RelpConnectionFactoryFake
        implements PoolableSupplier<Pool<IManagedRelpConnection>, IManagedRelpConnection> {

    private final int sendLatency;
    private final int connectLatency;
    private final RelpConfig relpConfig;
    private final MetricRegistry metricRegistry;

    public RelpConnectionFactoryFake(
            final int sendLatency,
            final int connectLatency,
            final RelpConfig relpConfig,
            final MetricRegistry metricRegistry
    ) {
        this.sendLatency = sendLatency;
        this.connectLatency = connectLatency;
        this.relpConfig = relpConfig;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public void accept(final IManagedRelpConnection iManagedRelpConnection) {
        try {
            iManagedRelpConnection.close();
        }
        catch (final IOException e) {
            throw new UncheckedIOException("Exception occurred in accept() method", e);
        }
    }

    @Override
    public IManagedRelpConnection apply(final Pool<IManagedRelpConnection> iManagedRelpConnectionPool) {
        return new ManagedRelpConnection(
                new MetricRelpConnection(new RelpConnectionFake(relpConfig, sendLatency, connectLatency), metricRegistry), metricRegistry
        );
    }
}
