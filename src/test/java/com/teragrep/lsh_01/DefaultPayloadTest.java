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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefaultPayloadTest {

    @Test
    public void testEquals() {
        DefaultPayload payload1 = new DefaultPayload("payload");
        DefaultPayload payload2 = new DefaultPayload("payload");

        // calling functions shouldn't have effect on an immutable object
        payload1.messages();

        Assertions.assertEquals(payload1, payload2);
    }

    @Test
    public void testNotEquals() {
        DefaultPayload payload1 = new DefaultPayload("payload");
        DefaultPayload payload2 = new DefaultPayload("");

        Assertions.assertNotEquals(payload1, payload2);
    }

    @Test
    public void testHashCode() {
        DefaultPayload payload1 = new DefaultPayload("payload");
        DefaultPayload payload2 = new DefaultPayload("payload");
        DefaultPayload payload3 = new DefaultPayload("");

        Assertions.assertEquals(payload1.hashCode(), payload2.hashCode());
        Assertions.assertNotEquals(payload1.hashCode(), payload3.hashCode());
    }
}
