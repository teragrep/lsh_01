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
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig, basicAuthentication);
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
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig, basicAuthentication);
        Assertions.assertTrue(relpConversion.requiresToken());
        // FirstUser:VeryFirstPassword
        Assertions.assertFalse(relpConversion.asSubject("Basic Rmlyc3RVc2VyOlZlcnlGaXJzdFBhc3N3b3Jk").isStub());
        // ThirdUser:PasswordIsThree!
        Assertions.assertFalse(relpConversion.asSubject("Basic VGhpcmRVc2VyOlBhc3N3b3JkSXNUaHJlZSE=").isStub());
    }

    @Test
    public void testInvalidAuths() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig, basicAuthentication);
        Assertions.assertTrue(relpConversion.requiresToken());
        // Shady:Hacker
        Assertions.assertTrue(relpConversion.asSubject("Basic U2hhZHk6SGFja2Vy").isStub());
    }

    @Test
    public void testValidBase64ButNoColon() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig, basicAuthentication);
        Assertions.assertTrue(relpConversion.requiresToken());
        // Test
        Assertions.assertTrue(relpConversion.asSubject("Basic VGVzdA==").isStub());
    }

    @Test
    public void testMultipleColons() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig, basicAuthentication);
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
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig, basicAuthentication);
        Assertions.assertTrue(relpConversion.requiresToken());
        Assertions.assertTrue(relpConversion.asSubject("Basic BasicButNotBase64").isStub());
    }

    @Test
    public void testNonBasicAuth() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig, basicAuthentication);
        Assertions.assertTrue(relpConversion.requiresToken());
        Assertions.assertTrue(relpConversion.asSubject("I am not basic auth").isStub());
    }

    @Test
    public void testWrongCredentials() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig, basicAuthentication);
        Assertions.assertTrue(relpConversion.requiresToken());
        // SecondUser:WrongPassword -> Right user
        Assertions.assertTrue(relpConversion.asSubject("Basic U2Vjb25kVXNlcjpXcm9uZ1Bhc3N3b3Jk").isStub());
        // WrongUser:AlreadySecondPassword -> Right password
        Assertions.assertTrue(relpConversion.asSubject("Basic V3JvbmdVc2VyOkFscmVhZHlTZWNvbmRQYXNzd29yZA==").isStub());
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
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig, basicAuthentication);
        Assertions.assertTrue(relpConversion.requiresToken());
        // :VeryFirstPassword -> Valid password, null username
        Assertions.assertTrue(relpConversion.asSubject("Basic OlZlcnlGaXJzdFBhc3N3b3Jk").isStub());
    }

    @Test
    public void testEmptyPassword() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig, basicAuthentication);
        Assertions.assertTrue(relpConversion.requiresToken());
        // FirstUser: -> Valid username, null password
        Assertions.assertTrue(relpConversion.asSubject("Basic Rmlyc3RVc2VyOg==").isStub());
    }

    @Test
    public void testNullToken() {
        System.setProperty("security.authRequired", "true");
        System.setProperty("credentials.file", credentialsFile);
        RelpConfig relpConfig = new RelpConfig();
        SecurityConfig securityConfig = new SecurityConfig();
        BasicAuthentication basicAuthentication = new BasicAuthenticationFactory().create();
        RelpConversion relpConversion = new RelpConversion(relpConfig, securityConfig, basicAuthentication);
        Assertions.assertTrue(relpConversion.requiresToken());
        Assertions.assertTrue(relpConversion.asSubject(null).isStub());
    }
}
