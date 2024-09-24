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
import com.teragrep.jlt_01.StringLookupTable;
import com.teragrep.lsh_01.conversion.RelpConversion;
import com.teragrep.lsh_01.authentication.BasicAuthentication;
import com.teragrep.lsh_01.authentication.BasicAuthenticationFactory;
import com.teragrep.lsh_01.authentication.Subject;
import com.teragrep.lsh_01.config.LookupConfig;
import com.teragrep.lsh_01.config.RelpConfig;
import com.teragrep.lsh_01.config.SecurityConfig;
import com.teragrep.lsh_01.lookup.LookupTableFactory;
import com.teragrep.lsh_01.pool.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LookupTest {

    private final String credentialsFile = "src/test/resources/credentials.json";

    @BeforeEach
    public void addProperties() {
        System.setProperty("lookups.hostname.file", "src/test/resources/hostname.json");
        System.setProperty("lookups.appname.file", "src/test/resources/appname.json");
    }

    @AfterEach
    public void cleanProperties() {
        System.clearProperty("security.authRequired");
        System.clearProperty("credentials.file");
        System.clearProperty("lookups.hostname.file");
        System.clearProperty("lookups.appname.file");
    }

    @Test
    public void testAppnameLookup() {
        LookupConfig lookupConfig = new LookupConfig();
        StringLookupTable appNameLookup = new LookupTableFactory().create(lookupConfig.appNamePath);
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);

        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConnectionFactory relpConnectionFactory = new RelpConnectionFactory(relpConfig, new MetricRegistry());
        Pool<IManagedRelpConnection> pool = new Pool<>(relpConnectionFactory, new ManagedRelpConnectionStub());
        RelpConversion relpConversion = new RelpConversion(
                pool,
                securityConfig,
                basicAuthentication,
                new LookupConfig()
        );

        // FirstUser:VeryFirstPassword!
        Subject subject = relpConversion.asSubject("Basic Rmlyc3RVc2VyOlZlcnlGaXJzdFBhc3N3b3Jk");
        Assertions.assertEquals(appNameLookup.lookup(subject.subject()), "users-first-magnificent-app");
    }

    @Test
    public void testHostnameLookup() {
        LookupConfig lookupConfig = new LookupConfig();
        StringLookupTable hostnameLookup = new LookupTableFactory().create(lookupConfig.hostnamePath);
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);

        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConnectionFactory relpConnectionFactory = new RelpConnectionFactory(relpConfig, new MetricRegistry());
        Pool<IManagedRelpConnection> pool = new Pool<>(relpConnectionFactory, new ManagedRelpConnectionStub());
        RelpConversion relpConversion = new RelpConversion(
                pool,
                securityConfig,
                basicAuthentication,
                new LookupConfig()
        );

        // FirstUser:VeryFirstPassword!
        Subject subject = relpConversion.asSubject("Basic Rmlyc3RVc2VyOlZlcnlGaXJzdFBhc3N3b3Jk");
        Assertions.assertEquals(hostnameLookup.lookup(subject.subject()), "host-one.example.com");
    }

    @Test
    public void testMissingLookups() {
        LookupConfig lookupConfig = new LookupConfig();
        StringLookupTable hostnameLookup = new LookupTableFactory().create(lookupConfig.hostnamePath);
        StringLookupTable appNameLookup = new LookupTableFactory().create(lookupConfig.appNamePath);

        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);

        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConnectionFactory relpConnectionFactory = new RelpConnectionFactory(relpConfig, new MetricRegistry());
        Pool<IManagedRelpConnection> pool = new Pool<>(relpConnectionFactory, new ManagedRelpConnectionStub());
        RelpConversion relpConversion = new RelpConversion(
                pool,
                securityConfig,
                basicAuthentication,
                new LookupConfig()
        );

        // MissingHostname:MyHostnameIsMissing
        Subject missingHostname = relpConversion.asSubject("Basic TWlzc2luZ0hvc3RuYW1lOk15SG9zdG5hbWVJc01pc3Npbmc=");
        Assertions.assertEquals(appNameLookup.lookup(missingHostname.subject()), "users-third-magnificent-app");
        Assertions.assertEquals(hostnameLookup.lookup(missingHostname.subject()), "fallback-hostname.example.com");

        // MissingAppName:MyAppNameIsMissing
        Subject missingAppName = relpConversion.asSubject("Basic TWlzc2luZ0FwcE5hbWU6TXlBcHBOYW1lSXNNaXNzaW5n");
        Assertions.assertEquals(appNameLookup.lookup(missingAppName.subject()), "fallback-appname");
        Assertions.assertEquals(hostnameLookup.lookup(missingAppName.subject()), "host-four.example.com");
    }

    @Test
    public void testValidHostnameFile() {
        System.setProperty("lookups.hostname.file", "src/test/resources/hostname.json");
        LookupConfig lookupConfig = new LookupConfig();
        Assertions.assertEquals(lookupConfig.hostnamePath, "src/test/resources/hostname.json");
    }

    @Test
    public void testValidAppnameFile() {
        System.setProperty("lookups.appname.file", "src/test/resources/appname.json");
        LookupConfig lookupConfig = new LookupConfig();
        Assertions.assertEquals(lookupConfig.appNamePath, "src/test/resources/appname.json");
    }

    @Test
    public void testInvalidHostnamePath() {

        System.setProperty("lookups.hostname.file", "hosts-gone");
        LookupConfig lookupConfig = new LookupConfig();
        IllegalArgumentException e = Assertions
                .assertThrows(
                        IllegalArgumentException.class, () -> new LookupTableFactory().create(lookupConfig.hostnamePath)
                );
        Assertions.assertEquals("Can't find lookup table from path <[hosts-gone]>: ", e.getMessage());
    }

    @Test
    public void testInvalidAppNamePath() {

        System.setProperty("lookups.appname.file", "apps-gone");
        LookupConfig lookupConfig = new LookupConfig();
        IllegalArgumentException e = Assertions
                .assertThrows(
                        IllegalArgumentException.class, () -> new LookupTableFactory().create(lookupConfig.appNamePath)
                );
        Assertions.assertEquals("Can't find lookup table from path <[apps-gone]>: ", e.getMessage());
    }
}
