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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Base64;

public class BasicAuthentication {

    private final Base64.Decoder decoder;
    private final CredentialLookup credentialLookup;
    private final Subject subjectStub;
    private final static Logger LOGGER = LogManager.getLogger(BasicAuthentication.class);

    public BasicAuthentication(CredentialLookup credentialLookup) {
        this(Base64.getDecoder(), new SubjectStub(), credentialLookup);
    }

    public BasicAuthentication(Base64.Decoder decoder, SubjectStub subjectStub, CredentialLookup credentialLookup) {
        this.decoder = decoder;
        this.credentialLookup = credentialLookup;
        this.subjectStub = subjectStub;
    }

    public Subject asSubject(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Got null or empty token");
        }
        if (!token.startsWith("Basic ")) {
            throw new IllegalArgumentException("Got invalid token, doesn't start with Basic");
        }
        String tokenString = new String(decoder.decode(token.substring("Basic".length()).trim()));
        if (!tokenString.contains(":")) {
            throw new IllegalArgumentException("Got invalid token, doesn't include colon");
        }
        String[] credentialPair = tokenString.split(":", 2);
        String username = credentialPair[0];
        String password = credentialPair[1];
        if ("".equals(username) || "".equals(password)) {
            throw new IllegalArgumentException("Got invalid token, username or password is not present");
        }
        if (password.equals(credentialLookup.getCredential(username))) {
            LOGGER.debug("Authentication ok");
            return new SubjectImpl(username);
        }
        else {
            LOGGER.debug("Authentication failed, credential mismatch");
            throw new IllegalArgumentException("Authentication failed, credential mismatch");
        }
    }
}
