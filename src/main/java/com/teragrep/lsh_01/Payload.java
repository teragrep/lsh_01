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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A message from a log source
 */
final public class Payload {

    private final String body;
    private final Pattern splitPattern;

    public Payload(String body, Pattern splitPattern) {
        this.body = body;
        this.splitPattern = splitPattern;
    }

    /**
     * Splits the payload into multiple payloads if there is a defined split regex in the body.
     * 
     * @return list of Payloads
     */
    public List<Payload> split() {
        ArrayList<Payload> payloads = new ArrayList<>();

        String[] messages = splitPattern.split(body);

        for (String message : messages) {
            payloads.add(new Payload(message, splitPattern));
        }

        return payloads;
    }

    /**
     * Takes the message from the payload.
     * 
     * @return message body
     */
    public String take() {
        return body;
    }
}
