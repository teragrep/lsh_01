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
package com.teragrep.lsh_01.util;

import com.teragrep.lsh_01.RelpConversion;
import com.teragrep.lsh_01.config.RelpConfig;
import com.teragrep.lsh_01.config.SecurityConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CredentialsTest {

    private final String credentialsFile = "src/test/resources/credentials.json";

    @Test
    public void testNoAuthRequired() {
        System.setProperty("security.authRequired", "false");
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig);
        Assertions.assertFalse(relpConversion.requiresToken());
    }

    @Test
    public void testCredentialsFileExist() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", "path/doesnt/exist.json");
        Assertions.assertThrows(IllegalArgumentException.class, SecurityConfig::new);
        System.setProperty("credentials.file", credentialsFile);
        Assertions.assertDoesNotThrow(SecurityConfig::new);
    }

    @Test
    public void testAuthRequired() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig);
        Assertions.assertTrue(relpConversion.requiresToken());
        // FirstUser:VeryFirstPassword
        Assertions.assertTrue(relpConversion.validatesToken("Basic Rmlyc3RVc2VyOlZlcnlGaXJzdFBhc3N3b3Jk"));
        // ThirdUser:PasswordIsThree!
        Assertions.assertTrue(relpConversion.validatesToken("Basic VGhpcmRVc2VyOlBhc3N3b3JkSXNUaHJlZSE="));
    }

    @Test
    public void testInvalidAuths() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig);
        Assertions.assertTrue(relpConversion.requiresToken());
        // Shady:Hacker
        Assertions.assertFalse(relpConversion.validatesToken("Basic U2hhZHk6SGFja2Vy"));
    }

    @Test
    public void testValidBase64ButNoColon() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig);
        Assertions.assertTrue(relpConversion.requiresToken());
        // Test
        Assertions.assertFalse(relpConversion.validatesToken("Basic VGVzdA=="));
    }

    @Test
    public void testMultipleColons() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig);
        Assertions.assertTrue(relpConversion.requiresToken());
        // UserWithColons:My:Password:Yay
        Assertions.assertTrue(relpConversion.validatesToken("Basic VXNlcldpdGhDb2xvbnM6TXk6UGFzc3dvcmQ6WWF5"));
    }

    @Test
    public void testInvalidBase64Auth() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig);
        Assertions.assertTrue(relpConversion.requiresToken());
        Assertions.assertFalse(relpConversion.validatesToken("Basic BasicButNotBase64"));
    }

    @Test
    public void testNonBasicAuth() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig);
        Assertions.assertTrue(relpConversion.requiresToken());
        Assertions.assertFalse(relpConversion.validatesToken("I am not basic auth"));
    }

    @Test
    public void testWrongCredentials() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig);
        Assertions.assertTrue(relpConversion.requiresToken());
        // SecondUser:WrongPassword -> Right user
        Assertions.assertFalse(relpConversion.validatesToken("Basic U2Vjb25kVXNlcjpXcm9uZ1Bhc3N3b3Jk"));
        // WrongUser:AlreadySecondPassword -> Right password
        Assertions.assertFalse(relpConversion.validatesToken("Basic V3JvbmdVc2VyOkFscmVhZHlTZWNvbmRQYXNzd29yZA=="));
        // SecondUser:AlreadySecondPassword -> Right user and right password
        Assertions.assertTrue(relpConversion.validatesToken("Basic U2Vjb25kVXNlcjpBbHJlYWR5U2Vjb25kUGFzc3dvcmQ="));
    }
}
