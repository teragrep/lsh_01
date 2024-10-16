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
package com.teragrep.lsh_01.conversion;

import com.teragrep.lsh_01.authentication.Subject;

import java.util.Map;

/**
 * Decorator for IMessageHandler that splits messages arriving as an array of JSON objects.
 */
public final class JsonConversion implements IMessageHandler {

    private final IMessageHandler conversion;

    public JsonConversion(IMessageHandler conversion) {
        this.conversion = conversion;
    }

    @Override
    public boolean onNewMessage(Subject subject, Map<String, String> headers, String body) {
        JsonPayload originalPayload = new JsonPayload(new DefaultPayload(body));

        boolean msgSent = true;
        for (String message : originalPayload.messages()) { // each object individually as a String
            if (!conversion.onNewMessage(subject, headers, message)) {
                msgSent = false;
            }
        }

        return msgSent;
    }

    @Override
    public Subject asSubject(String token) {
        return conversion.asSubject(token);
    }

    @Override
    public boolean requiresToken() {
        return conversion.requiresToken();
    }

    @Override
    public IMessageHandler copy() {
        return new JsonConversion(conversion.copy());
    }

    @Override
    public Map<String, String> responseHeaders() {
        return conversion.responseHeaders();
    }
}
