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

import com.codahale.metrics.MetricRegistry;
import com.teragrep.lsh_01.config.RelpConfig;
import com.teragrep.lsh_01.pool.IManagedRelpConnection;
import com.teragrep.lsh_01.pool.ManagedRelpConnection;
import com.teragrep.lsh_01.pool.MetricRelpConnection;

import java.util.function.Supplier;

public class RelpConnectionFactoryFake implements Supplier<IManagedRelpConnection> {

    private final int sendLatency;
    private final int connectLatency;
    private final RelpConfig relpConfig;
    private final MetricRegistry metricRegistry;

    public RelpConnectionFactoryFake(
            int sendLatency,
            int connectLatency,
            RelpConfig relpConfig,
            MetricRegistry metricRegistry
    ) {
        this.sendLatency = sendLatency;
        this.connectLatency = connectLatency;
        this.relpConfig = relpConfig;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public IManagedRelpConnection get() {
        return new ManagedRelpConnection(
                new MetricRelpConnection(new RelpConnectionFake(relpConfig, sendLatency, connectLatency), metricRegistry), metricRegistry
        );
    }
}
