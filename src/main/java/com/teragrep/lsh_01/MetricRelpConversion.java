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
package com.teragrep.lsh_01;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingWindowReservoir;
import com.codahale.metrics.Timer;
import com.teragrep.lsh_01.authentication.Subject;

import java.util.Map;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Decorator for IMessageHandler. Responsible for reporting metrics.
 */
public class MetricRelpConversion implements IMessageHandler {

    private final IMessageHandler relpConversion;
    private final MetricRegistry metricRegistry;
    private final Timer sendLatency;

    public MetricRelpConversion(IMessageHandler relpConversion, MetricRegistry metricRegistry) {
        this.relpConversion = relpConversion;
        this.metricRegistry = metricRegistry;
        this.sendLatency = metricRegistry
                .timer(name(RelpConversion.class, "sendLatency"), () -> new Timer(new SlidingWindowReservoir(10000)));
    }

    @Override
    public boolean onNewMessage(Subject subject, Map<String, String> headers, String body) {
        boolean sent;
        try (Timer.Context ctx = sendLatency.time()) {
            sent = relpConversion.onNewMessage(subject, headers, body);
        }
        return sent;
    }

    @Override
    public IMessageHandler copy() {
        return new MetricRelpConversion(relpConversion.copy(), metricRegistry);
    }

    @Override
    public Subject asSubject(String token) {
        return relpConversion.asSubject(token);
    }

    @Override
    public boolean requiresToken() {
        return relpConversion.requiresToken();
    }

    @Override
    public Map<String, String> responseHeaders() {
        return relpConversion.responseHeaders();
    }
}
