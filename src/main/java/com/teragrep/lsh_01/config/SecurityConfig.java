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

import com.teragrep.jai_02.CredentialLookup;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Base64;

public class SecurityConfig implements Validateable {

    public final boolean authRequired;
    CredentialLookup credentialLookup;
    Base64.Decoder decoder = Base64.getDecoder();

    public SecurityConfig() {
        PropertiesReaderUtilityClass propertiesReader = new PropertiesReaderUtilityClass(
                System.getProperty("properties.file", "etc/config.properties")
        );
        authRequired = propertiesReader.getBooleanProperty("security.authRequired");
        if (authRequired) {
            BufferedReader br;
            String credentialsFile = System.getProperty("credentials.file", "etc/credentials.json");
            try {
                br = new BufferedReader(new FileReader(credentialsFile));
            }
            catch (FileNotFoundException e) {
                throw new IllegalArgumentException("Can't find <[" + credentialsFile + "]>: ", e);
            }
            credentialLookup = new CredentialLookup(br);
        }
    }

    public boolean isCredentialOk(String token) {
        if (!authRequired) {
            return true;
        }
        if (!token.startsWith("Basic ")) {
            return false;
        }
        try {
            String tokenString = new String(decoder.decode(token.substring("Basic".length()).trim()));
            if (!tokenString.contains(":")) {
                return false;
            }
            String[] credentialPair = tokenString.split(":", 2);
            if (credentialPair[0] == null || credentialPair[1] == null) {
                return false;
            }
            return credentialPair[1].equals(credentialLookup.getCredential(credentialPair[0]));
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void validate() {

    }
}
