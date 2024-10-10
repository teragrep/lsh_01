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

import com.teragrep.jai_02.CredentialLookup;
import com.teragrep.lsh_01.authentication.BasicAuthentication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;

public class BasicAuthenticationTest {

    @Test
    public void testEquals() {
        String credentialsFile = "src/test/resources/credentials.json";
        BufferedReader br = Assertions.assertDoesNotThrow(() -> new BufferedReader(new FileReader(credentialsFile)));
        CredentialLookup credentials = new CredentialLookup(br);

        BasicAuthentication auth1 = new BasicAuthentication(credentials);
        BasicAuthentication auth2 = new BasicAuthentication(credentials);

        Assertions.assertEquals(auth1, auth2);
    }

    @Test
    public void testNotEquals() {
        String credentialsFile = "src/test/resources/credentials.json";
        BufferedReader br = Assertions.assertDoesNotThrow(() -> new BufferedReader(new FileReader(credentialsFile)));
        CredentialLookup credentials = new CredentialLookup(br);

        String credentialsFile2 = "src/test/resources/exampleCredentials.json";
        BufferedReader br2 = Assertions.assertDoesNotThrow(() -> new BufferedReader(new FileReader(credentialsFile2)));
        CredentialLookup credentials2 = new CredentialLookup(br2);

        BasicAuthentication auth1 = new BasicAuthentication(credentials);
        BasicAuthentication auth2 = new BasicAuthentication(credentials2);

        Assertions.assertNotEquals(auth1, auth2);
    }

    @Test
    public void testHashCode() {
        String credentialsFile = "src/test/resources/credentials.json";
        BufferedReader br = Assertions.assertDoesNotThrow(() -> new BufferedReader(new FileReader(credentialsFile)));
        CredentialLookup credentials = new CredentialLookup(br);

        String credentialsFile2 = "src/test/resources/exampleCredentials.json";
        BufferedReader br2 = Assertions.assertDoesNotThrow(() -> new BufferedReader(new FileReader(credentialsFile2)));
        CredentialLookup credentials2 = new CredentialLookup(br2);

        BasicAuthentication auth1 = new BasicAuthentication(credentials);
        BasicAuthentication auth2 = new BasicAuthentication(credentials);
        BasicAuthentication auth3 = new BasicAuthentication(credentials2);

        Assertions.assertEquals(auth1.hashCode(), auth2.hashCode());
        Assertions.assertNotEquals(auth1.hashCode(), auth3.hashCode());
    }
}
