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
package com.teragrep.lsh_01.authentication;

import com.teragrep.jai_02.CredentialLookup;

import java.util.Base64;

public class BasicAuthentication {

    private final Base64.Decoder decoder;
    private final CredentialLookup credentialLookup;

    public BasicAuthentication(CredentialLookup credentialLookup) {
        this.decoder = Base64.getDecoder();
        this.credentialLookup = credentialLookup;
    }

    public boolean isCredentialOk(String token) {
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
}
