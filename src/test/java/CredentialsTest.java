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
import com.teragrep.lsh_01.authentication.BasicAuthentication;
import com.teragrep.lsh_01.RelpConversion;
import com.teragrep.lsh_01.authentication.BasicAuthenticationFactory;
import com.teragrep.lsh_01.config.LookupConfig;
import com.teragrep.lsh_01.config.RelpConfig;
import com.teragrep.lsh_01.config.SecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CredentialsTest {

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
    public void testNoAuthRequired() {
        System.setProperty("security.authRequired", "false");
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(
                relpConfig,
                securityConfig,
                basicAuthentication,
                new LookupConfig()
        );
        Assertions.assertFalse(relpConversion.requiresToken());
    }

    @Test
    public void testCredentialsFileExist() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", "path/doesnt/exist.json");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new BasicAuthenticationFactory().create());
        System.setProperty("credentials.file", credentialsFile);
        Assertions.assertDoesNotThrow(() -> new BasicAuthenticationFactory().create());
    }

    @Test
    public void testAuthRequired() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(
                relpConfig,
                securityConfig,
                basicAuthentication,
                new LookupConfig()
        );
        Assertions.assertTrue(relpConversion.requiresToken());
        // FirstUser:VeryFirstPassword
        Assertions.assertFalse(relpConversion.asSubject("Basic Rmlyc3RVc2VyOlZlcnlGaXJzdFBhc3N3b3Jk").isStub());
        // ThirdUser:PasswordIsThree!
        Assertions.assertFalse(relpConversion.asSubject("Basic VGhpcmRVc2VyOlBhc3N3b3JkSXNUaHJlZSE=").isStub());
    }

    @Test
    public void testValidBase64ButNoColon() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(
                relpConfig,
                securityConfig,
                basicAuthentication,
                new LookupConfig()
        );
        Assertions.assertTrue(relpConversion.requiresToken());
        // Test
        IllegalArgumentException e = Assertions
                .assertThrows(IllegalArgumentException.class, () -> relpConversion.asSubject("Basic VGVzdA==").isStub());
        Assertions.assertEquals("Got invalid token, doesn't include colon", e.getMessage());
    }

    @Test
    public void testMultipleColons() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(
                relpConfig,
                securityConfig,
                basicAuthentication,
                new LookupConfig()
        );
        Assertions.assertTrue(relpConversion.requiresToken());
        // UserWithColons:My:Password:Yay
        Assertions.assertFalse(relpConversion.asSubject("Basic VXNlcldpdGhDb2xvbnM6TXk6UGFzc3dvcmQ6WWF5").isStub());
    }

    @Test
    public void testInvalidBase64Auth() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(
                relpConfig,
                securityConfig,
                basicAuthentication,
                new LookupConfig()
        );
        Assertions.assertTrue(relpConversion.requiresToken());
        IllegalArgumentException e = Assertions
                .assertThrows(IllegalArgumentException.class, () -> relpConversion.asSubject("Basic BasicButNotBase64").isStub());
        Assertions.assertEquals("Last unit does not have enough valid bits", e.getMessage());
    }

    @Test
    public void testNonBasicAuth() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(
                relpConfig,
                securityConfig,
                basicAuthentication,
                new LookupConfig()
        );
        Assertions.assertTrue(relpConversion.requiresToken());
        IllegalArgumentException e = Assertions
                .assertThrows(
                        IllegalArgumentException.class,
                        () -> Assertions.assertTrue(relpConversion.asSubject("I am not basic auth").isStub())
                );
        Assertions.assertEquals("Got invalid token, doesn't start with Basic", e.getMessage());
    }

    @Test
    public void testWrongCredentials() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(
                relpConfig,
                securityConfig,
                basicAuthentication,
                new LookupConfig()
        );
        Assertions.assertTrue(relpConversion.requiresToken());
        // SecondUser:WrongPassword -> Right user
        IllegalArgumentException e1 = Assertions
                .assertThrows(
                        IllegalArgumentException.class,
                        () -> relpConversion.asSubject("Basic U2Vjb25kVXNlcjpXcm9uZ1Bhc3N3b3Jk").isStub()
                );
        Assertions.assertEquals("Authentication failed, credential mismatch", e1.getMessage());
        // WrongUser:AlreadySecondPassword -> Right password
        IllegalArgumentException e2 = Assertions
                .assertThrows(
                        IllegalArgumentException.class,
                        () -> relpConversion.asSubject("Basic V3JvbmdVc2VyOkFscmVhZHlTZWNvbmRQYXNzd29yZA==").isStub()
                );
        Assertions.assertEquals("Authentication failed, credential mismatch", e2.getMessage());
        // SecondUser:AlreadySecondPassword -> Right user and right password
        Assertions.assertFalse(relpConversion.asSubject("Basic U2Vjb25kVXNlcjpBbHJlYWR5U2Vjb25kUGFzc3dvcmQ=").isStub());
    }

    @Test
    public void testEmptyUsername() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(
                relpConfig,
                securityConfig,
                basicAuthentication,
                new LookupConfig()
        );
        Assertions.assertTrue(relpConversion.requiresToken());
        // :VeryFirstPassword -> Valid password, null username
        IllegalArgumentException e = Assertions
                .assertThrows(
                        IllegalArgumentException.class, () -> relpConversion.asSubject("Basic OlZlcnlGaXJzdFBhc3N3b3Jk").isStub()
                );
        Assertions.assertEquals("Got invalid token, username or password is not present", e.getMessage());
    }

    @Test
    public void testEmptyPassword() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(
                relpConfig,
                securityConfig,
                basicAuthentication,
                new LookupConfig()
        );
        Assertions.assertTrue(relpConversion.requiresToken());
        // FirstUser: -> Valid username, null password
        IllegalArgumentException e = Assertions
                .assertThrows(IllegalArgumentException.class, () -> relpConversion.asSubject("Basic Rmlyc3RVc2VyOg==").isStub());
        Assertions.assertEquals("Got invalid token, username or password is not present", e.getMessage());
    }

    @Test
    public void testNullToken() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(
                relpConfig,
                securityConfig,
                basicAuthentication,
                new LookupConfig()
        );
        Assertions.assertTrue(relpConversion.requiresToken());
        IllegalArgumentException e = Assertions
                .assertThrows(IllegalArgumentException.class, () -> relpConversion.asSubject(null).isStub());
        Assertions.assertEquals("Got null or empty token", e.getMessage());
    }
}
