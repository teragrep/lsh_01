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

import com.teragrep.lsh_01.authentication.BasicAuthenticationFactory;
import com.teragrep.lsh_01.config.*;
import com.teragrep.lsh_01.pool.IManagedRelpConnection;
import com.teragrep.lsh_01.pool.ManagedRelpConnectionStub;
import com.teragrep.lsh_01.pool.Pool;
import com.teragrep.lsh_01.pool.RelpConnectionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConversionFactoryTest {

    @Test
    public void testInvalidSplitRegex() {
        String regexPattern = "(a*b{)";
        String splitType = "regex";

        RelpConnectionFactory relpConnectionFactory = new RelpConnectionFactory(new RelpConfig());
        Pool<IManagedRelpConnection> pool = new Pool<>(relpConnectionFactory, new ManagedRelpConnectionStub());

        ConversionFactory conversionFactory = new ConversionFactory(
                splitType,
                regexPattern,
                pool,
                new SecurityConfig(),
                new BasicAuthenticationFactory().create(),
                new LookupConfig()
        );
        IllegalArgumentException e = Assertions
                .assertThrows(IllegalArgumentException.class, conversionFactory::conversion);

        Assertions
                .assertEquals(
                        "Configuration has an invalid regex (payload.splitType.regex.pattern): " + regexPattern,
                        e.getMessage()
                );
    }

    @Test
    public void testValidSplitRegex() {
        String regexPattern = "\\n";
        String splitType = "regex";

        RelpConnectionFactory relpConnectionFactory = new RelpConnectionFactory(new RelpConfig());
        Pool<IManagedRelpConnection> pool = new Pool<>(relpConnectionFactory, new ManagedRelpConnectionStub());

        ConversionFactory conversionFactory = new ConversionFactory(
                splitType,
                regexPattern,
                pool,
                new SecurityConfig(),
                new BasicAuthenticationFactory().create(),
                new LookupConfig()
        );

        Assertions.assertDoesNotThrow(conversionFactory::conversion);
    }

    @Test
    public void testInvalidSplitType() {
        String splitType = "invalid";
        String regexPattern = "";

        RelpConnectionFactory relpConnectionFactory = new RelpConnectionFactory(new RelpConfig());
        Pool<IManagedRelpConnection> pool = new Pool<>(relpConnectionFactory, new ManagedRelpConnectionStub());

        ConversionFactory conversionFactory = new ConversionFactory(
                splitType,
                regexPattern,
                pool,
                new SecurityConfig(),
                new BasicAuthenticationFactory().create(),
                new LookupConfig()
        );

        IllegalArgumentException e = Assertions
                .assertThrows(IllegalArgumentException.class, conversionFactory::conversion);
        Assertions
                .assertEquals(
                        "Configuration has an invalid splitType: " + splitType
                                + ". Has to be 'regex', 'json_array' or 'none'.",
                        e.getMessage()
                );
    }

    @Test
    public void testValidSplitType() {
        String regexPattern = "";
        String splitType = "json_array";

        RelpConnectionFactory relpConnectionFactory = new RelpConnectionFactory(new RelpConfig());
        Pool<IManagedRelpConnection> pool = new Pool<>(relpConnectionFactory, new ManagedRelpConnectionStub());

        ConversionFactory conversionFactory = new ConversionFactory(
                splitType,
                regexPattern,
                pool,
                new SecurityConfig(),
                new BasicAuthenticationFactory().create(),
                new LookupConfig()
        );
        Assertions.assertDoesNotThrow(conversionFactory::conversion);

        splitType = "regex";
        conversionFactory = new ConversionFactory(
                splitType,
                regexPattern,
                pool,
                new SecurityConfig(),
                new BasicAuthenticationFactory().create(),
                new LookupConfig()
        );
        Assertions.assertDoesNotThrow(conversionFactory::conversion);

        splitType = "none";
        conversionFactory = new ConversionFactory(
                splitType,
                regexPattern,
                pool,
                new SecurityConfig(),
                new BasicAuthenticationFactory().create(),
                new LookupConfig()
        );
        Assertions.assertDoesNotThrow(conversionFactory::conversion);
    }
}
