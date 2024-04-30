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

import com.teragrep.lsh_01.config.PayloadConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * A message from a log source
 */
final public class Payload {

    private final static Logger LOGGER = LogManager.getLogger(Payload.class);
    private final PayloadConfig payloadConfig;
    private final String body;

    public Payload(PayloadConfig payloadConfig, String body) {
        this.payloadConfig = payloadConfig;
        this.body = body;
    }

    /**
     * Splits the payload into multiple payloads if there is a defined split regex in the body.
     * @return list of Payloads
     */
    public List<Payload> split() {
        ArrayList<Payload> payloads = new ArrayList<>();

        try {
            String[] messages = body.split(payloadConfig.splitRegex);

            for (String message: messages) {
                payloads.add(new Payload(payloadConfig, message));
            }
        } catch (PatternSyntaxException e) {
            LOGGER.error("Invalid splitRegex in configuration: <{}>", payloadConfig.splitRegex);
            payloads.add(this);
        }

        return payloads;
    }

    /**
     * Takes the message from the payload.
     * @return message body
     */
    public String take() {
        return body;
    }
}
