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
package com.teragrep.lsh_01.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;

import java.io.IOException;

public final class JmxReport implements Report {

    private final Report report;
    private final JmxReporter jmxReporter;

    public JmxReport(Report report, MetricRegistry metricRegistry) {
        this.report = report;
        this.jmxReporter = JmxReporter.forRegistry(metricRegistry).build();
    }

    @Override
    public void start() {
        jmxReporter.start();
        report.start();
    }

    @Override
    public void close() throws IOException {
        report.close();
        jmxReporter.close();
    }
}
