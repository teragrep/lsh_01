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

import com.teragrep.lsh_01.conversion.DefaultPayload;
import com.teragrep.lsh_01.conversion.JsonPayload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class JsonPayloadTest {

    @Test
    public void testSingleSplitting() {
        String message = "{\"foo\": 1}";
        String requestBody = "[\n" + message + "\n]";

        JsonPayload payload = new JsonPayload(new DefaultPayload(requestBody));
        List<String> messages = payload.messages();

        String expected = message.replaceAll("\\s", "");

        Assertions.assertEquals(expected, messages.get(0));
    }

    @Test
    public void testMultipleSplitting() {
        String message1 = "{\"foo\": 1}";
        String message2 = "{\"bar\": 2}";
        String requestBody = "[\n" + message1 + ",\n" + message2 + "\n]";

        JsonPayload payload = new JsonPayload(new DefaultPayload(requestBody));
        List<String> messages = payload.messages();

        String expected1 = message1.replaceAll("\\s", "");
        String expected2 = message2.replaceAll("\\s", "");

        Assertions.assertEquals(2, messages.size());
        Assertions.assertEquals(expected1, messages.get(0));
        Assertions.assertEquals(expected2, messages.get(1));
    }

    @Test
    public void testObjectEquality() {
        String requestBody = "[\n{\"foo\": 1}\n]";
        String difRequestBody = "[\n{\"bar\": 2}\n]";
        JsonPayload payload = new JsonPayload(new DefaultPayload(requestBody));
        JsonPayload samePayload = new JsonPayload(new DefaultPayload(requestBody));
        JsonPayload difPayload = new JsonPayload(new DefaultPayload(difRequestBody));

        // public methods of JsonPayload shouldn't affect an immutable object
        payload.messages();

        Assertions.assertEquals(payload, samePayload);
        Assertions.assertNotEquals(payload, difPayload);
        Assertions.assertNotEquals(samePayload, difPayload);
    }
}
