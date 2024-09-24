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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Payload splittable with a regex pattern.
 */
final public class RegexPayload implements Payload {

    private final Payload payload;
    private final Pattern splitPattern;

    public RegexPayload(Payload payload, Pattern splitPattern) {
        this.payload = payload;
        this.splitPattern = splitPattern;
    }

    /**
     * Splits the payload into multiple payloads if there is a defined split regex in the body.
     *
     * @return list of Payloads
     */
    @Override
    public List<String> messages() {
        ArrayList<String> allMessages = new ArrayList<>();

        for (String message : payload.messages()) {
            String[] payloadMessages = splitPattern.split(message);
            allMessages.addAll(List.of(payloadMessages));
        }

        return allMessages;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (object.getClass() != this.getClass())
            return false;
        final RegexPayload cast = (RegexPayload) object;
        return payload.equals(cast.payload) && splitPattern.equals(cast.splitPattern);
    }
}
