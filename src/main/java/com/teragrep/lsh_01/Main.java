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
import com.teragrep.lsh_01.authentication.BasicAuthentication;
import com.teragrep.lsh_01.authentication.BasicAuthenticationFactory;
import com.teragrep.lsh_01.config.*;
import com.teragrep.lsh_01.metrics.HttpReport;
import com.teragrep.lsh_01.metrics.JmxReport;
import com.teragrep.lsh_01.metrics.Report;
import com.teragrep.lsh_01.metrics.Slf4jReport;
import com.teragrep.lsh_01.conversion.*;
import com.teragrep.lsh_01.pool.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

public class Main {

    private final static Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        Map<String, String> propsMap;
        try {
            propsMap = new PathProperties(System.getProperty("properties.file", "etc/config.properties"))
                    .deepCopyAsUnmodifiableMap();
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Can't find properties file: ", e);
        }

        NettyConfig nettyConfig = new NettyConfig();
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        InternalEndpointUrlConfig internalEndpointUrlConfig = new InternalEndpointUrlConfig();
        LookupConfig lookupConfig = new LookupConfig();
        MetricsConfig metricsConfig = new MetricsConfig();
        try {
            nettyConfig.validate();
            relpConfig.validate();
            securityConfig.validate();
            internalEndpointUrlConfig.validate();
            lookupConfig.validate();
            metricsConfig.validate();
        }
        catch (IllegalArgumentException e) {
            LOGGER.error("Can't parse config properly: {}", e.getMessage());
            System.exit(1);
        }
        LOGGER.info("Got server config: <[{}]>", nettyConfig);
        LOGGER.info("Got relp config: <[{}]>", relpConfig);
        LOGGER.info("Got internal endpoint config: <[{}]>", internalEndpointUrlConfig);
        LOGGER.info("Got lookup table config: <[{}]>", lookupConfig);
        LOGGER.info("Authentication required: <[{}]>", securityConfig.authRequired);

        // metrics
        MetricRegistry metricRegistry = new MetricRegistry();
        Report report = new Slf4jReport(
                new JmxReport(new HttpReport(metricRegistry, metricsConfig.prometheusPort), metricRegistry),
                metricRegistry
        );

        RelpConnectionFactory relpConnectionFactory = new RelpConnectionFactory(relpConfig, metricRegistry);
        Pool<IManagedRelpConnection> pool = new Pool<>(relpConnectionFactory, new ManagedRelpConnectionStub());

        IMessageHandler conversion = new MetricRelpConversion(new ConversionFactory(
                propsMap,
                pool,
                securityConfig,
                basicAuthentication,
                lookupConfig
        ).conversion(),
                metricRegistry);

        try (
                HttpServer server = new MetricHttpServer(
                        new NettyHttpServer(nettyConfig, conversion, null, 200, internalEndpointUrlConfig),
                        report
                )
        ) {
            server.run();
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Failed to close the server: " + e.getMessage());
        }
        finally {
            pool.close();
        }
    }
}
