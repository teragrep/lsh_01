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
import com.teragrep.lsh_01.conversion.RegexPayload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

public class RegexPayloadTest {

    @Test
    public void testDefaultSplitRegex() {
        String body = "foo\nbar\nfoobar";
        Pattern splitPattern = Pattern.compile("\\n");
        RegexPayload payload = new RegexPayload(new DefaultPayload(body), splitPattern);
        List<String> messages = payload.messages();

        Assertions.assertEquals(3, messages.size());
        Assertions.assertEquals("foo", messages.get(0));
        Assertions.assertEquals("bar", messages.get(1));
        Assertions.assertEquals("foobar", messages.get(2));
    }

    @Test
    public void testCustomSplitRegex() {
        String body = "foo,bar,foobar";
        Pattern splitPattern = Pattern.compile(",");
        RegexPayload payload = new RegexPayload(new DefaultPayload(body), splitPattern);
        List<String> messages = payload.messages();

        Assertions.assertEquals(3, messages.size());
        Assertions.assertEquals("foo", messages.get(0));
        Assertions.assertEquals("bar", messages.get(1));
        Assertions.assertEquals("foobar", messages.get(2));
    }

    @Test
    public void testNoSplittingRequired() {
        String body = "foobar";
        Pattern splitPattern = Pattern.compile("\\n");
        RegexPayload payload = new RegexPayload(new DefaultPayload(body), splitPattern);
        List<String> messages = payload.messages();

        Assertions.assertEquals(1, messages.size());
        Assertions.assertEquals("foobar", messages.get(0));
    }

    @Test
    public void testObjectEquality() {
        Pattern splitPattern = Pattern.compile("\\n");
        Pattern splitPattern2 = Pattern.compile(",");
        String requestBody = "[\n{\"foo\": 1}\n]";
        String difRequestBody = "[\n{\"bar\": 2}\n]";
        RegexPayload payload = new RegexPayload(new DefaultPayload(requestBody), splitPattern);
        RegexPayload samePayload = new RegexPayload(new DefaultPayload(requestBody), splitPattern);
        RegexPayload difPayload = new RegexPayload(new DefaultPayload(difRequestBody), splitPattern);
        RegexPayload difPattern = new RegexPayload(new DefaultPayload(requestBody), splitPattern2);

        // public methods of JsonPayload shouldn't affect an immutable object
        payload.messages();

        Assertions.assertEquals(payload, samePayload);
        Assertions.assertNotEquals(payload, difPayload);
        Assertions.assertNotEquals(payload, difPattern);
    }
}
