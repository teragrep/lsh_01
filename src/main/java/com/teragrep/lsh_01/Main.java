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

import com.teragrep.lsh_01.authentication.BasicAuthentication;
import com.teragrep.lsh_01.authentication.BasicAuthenticationFactory;
import com.teragrep.lsh_01.config.*;
import com.teragrep.lsh_01.pool.RelpConnectionFactory;
import com.teragrep.lsh_01.pool.RelpConnectionPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private final static Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        NettyConfig nettyConfig = new NettyConfig();
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        InternalEndpointUrlConfig internalEndpointUrlConfig = new InternalEndpointUrlConfig();
        LookupConfig lookupConfig = new LookupConfig();
        PayloadConfig payloadConfig = new PayloadConfig();
        try {
            nettyConfig.validate();
            relpConfig.validate();
            securityConfig.validate();
            internalEndpointUrlConfig.validate();
            lookupConfig.validate();
            payloadConfig.validate();
        }
        catch (IllegalArgumentException e) {
            LOGGER.error("Can't parse config properly: {}", e.getMessage());
            System.exit(1);
        }
        LOGGER.info("Got server config: <[{}]>", nettyConfig);
        LOGGER.info("Got relp config: <[{}]>", relpConfig);
        LOGGER.info("Got internal endpoint config: <[{}]>", internalEndpointUrlConfig);
        LOGGER.info("Got lookup table config: <[{}]>", lookupConfig);
        LOGGER.info("Got payload config: <[{}]>", payloadConfig);
        LOGGER.info("Authentication required: <[{}]>", securityConfig.authRequired);

        RelpConnectionFactory relpConnectionFactory = new RelpConnectionFactory(relpConfig);
        RelpConnectionPool relpConnectionPool = new RelpConnectionPool(
                relpConnectionFactory,
                relpConfig.rebindRequestAmount,
                relpConfig.rebindEnabled
        );

        RelpConversion relpConversion = new RelpConversion(
                relpConnectionPool,
                securityConfig,
                basicAuthentication,
                lookupConfig,
                payloadConfig
        );

        try (
                NettyHttpServer server = new NettyHttpServer(
                        nettyConfig,
                        relpConversion,
                        null,
                        200,
                        internalEndpointUrlConfig
                )
        ) {
            server.run();
        }
        finally {
            relpConnectionPool.close();
        }
    }
}
