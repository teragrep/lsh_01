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

import jakarta.json.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A Json array payload splittable into individual json objects.
 */
public final class JsonPayload implements Payload {

    private final Payload payload;

    public JsonPayload(Payload payload) {
        this.payload = payload;
    }

    /**
     * Splits the array of JSON objects into payloads with one object each. Has a side effect of removing whitespace
     * from the payloads because of jsonObject.toString().
     *
     * @return list of messages
     */
    @Override
    public List<String> messages() {
        List<String> allMessages = new ArrayList<>();
        for (String message : payload.messages()) {
            JsonReader reader = Json.createReader(new StringReader(message));
            JsonArray payloadMessages = reader.readArray();

            // transform all json objects into DefaultPayloads and return the list
            allMessages.addAll(payloadMessages.getValuesAs(JsonObject::toString));
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
        final JsonPayload cast = (JsonPayload) object;
        return payload.equals(cast.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(payload);
    }
}
