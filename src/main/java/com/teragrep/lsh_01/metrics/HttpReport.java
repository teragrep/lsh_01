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
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.MetricsServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.IOException;

public final class HttpReport implements Report {

    private final Server jettyServer;
    private final MetricRegistry metricRegistry;

    public HttpReport(MetricRegistry metricRegistry, int prometheusPort) {
        this.metricRegistry = metricRegistry;
        jettyServer = new Server(prometheusPort);
    }

    @Override
    public void start() {
        // prometheus-exporter
        CollectorRegistry.defaultRegistry.register(new DropwizardExports(metricRegistry));

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        jettyServer.setHandler(context);

        MetricsServlet metricsServlet = new MetricsServlet();
        ServletHolder servletHolder = new ServletHolder(metricsServlet);
        context.addServlet(servletHolder, "/metrics");

        // Start the webserver.
        try {
            jettyServer.start();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            jettyServer.stop();
        }
        catch (Exception e) {
            throw new IOException(e);
        }
    }
}
