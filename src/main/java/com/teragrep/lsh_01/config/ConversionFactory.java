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
package com.teragrep.lsh_01.config;

import com.teragrep.lsh_01.authentication.BasicAuthentication;
import com.teragrep.lsh_01.conversion.IMessageHandler;
import com.teragrep.lsh_01.conversion.JsonConversion;
import com.teragrep.lsh_01.conversion.RegexConversion;
import com.teragrep.lsh_01.conversion.RelpConversion;
import com.teragrep.lsh_01.pool.IManagedRelpConnection;
import com.teragrep.lsh_01.pool.Pool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class ConversionFactory {

    private final static Logger LOGGER = LogManager.getLogger(ConversionFactory.class);

    private final String splitType;
    private final String regexPattern;
    private final Pool<IManagedRelpConnection> pool;
    private final SecurityConfig securityConfig;
    private final BasicAuthentication basicAuthentication;
    private final LookupConfig lookupConfig;

    public ConversionFactory(
            Map<String, String> configuration,
            Pool<IManagedRelpConnection> pool,
            SecurityConfig securityConfig,
            BasicAuthentication basicAuthentication,
            LookupConfig lookupConfig
    ) {
        // if system property is not specified, defaults to config file (the Map)
        this(
                System.getProperty("payload.splitType", configuration.get("payload.splitType")),
                System
                        .getProperty(
                                "payload.splitType.regex.pattern", configuration.get("payload.splitType.regex.pattern")
                        ),
                pool,
                securityConfig,
                basicAuthentication,
                lookupConfig
        );
    }

    public ConversionFactory(
            String splitType,
            String regexPattern,
            Pool<IManagedRelpConnection> pool,
            SecurityConfig securityConfig,
            BasicAuthentication basicAuthentication,
            LookupConfig lookupConfig
    ) {
        this.splitType = splitType;
        this.regexPattern = regexPattern;
        this.pool = pool;
        this.securityConfig = securityConfig;
        this.basicAuthentication = basicAuthentication;
        this.lookupConfig = lookupConfig;
    }

    public IMessageHandler conversion() {
        LOGGER
                .info(
                        "Creating IMessageHandler with given configuration: payload.splitType=<[{}]>, payload.splitType.regex.pattern=<[{}]>",
                        splitType, regexPattern
                );

        validateConfiguration();

        IMessageHandler conversion = new RelpConversion(pool, securityConfig, basicAuthentication, lookupConfig);

        // apply splitting if configured. "none" value is skipped
        switch (splitType) {
            case "regex":
                conversion = new RegexConversion(conversion, regexPattern);
                break;
            case "json_array":
                conversion = new JsonConversion(conversion);
                break;
        }

        return conversion;
    }

    private void validateConfiguration() {
        switch (splitType) {
            case "regex":
                try {
                    Pattern.compile(regexPattern);
                }
                catch (PatternSyntaxException e) {
                    throw new IllegalArgumentException(
                            "Configuration has an invalid regex (payload.splitType.regex.pattern): " + regexPattern
                    );
                }
                break;
            case "json_array":
            case "none":
                break;
            default:
                throw new IllegalArgumentException(
                        "Configuration has an invalid splitType: " + splitType
                                + ". Has to be 'regex', 'json_array' or 'none'."
                );
        }
    }
}
