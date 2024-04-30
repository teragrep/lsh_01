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

import com.teragrep.lsh_01.Payload;
import com.teragrep.lsh_01.config.PayloadConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PayloadTest {

    @BeforeEach
    public void addProperties() {
        System.setProperty("payload.splitRegex", "\\n");
    }

    @AfterEach
    public void cleanProperties() {
        System.clearProperty("payload.splitRegex");
    }

    @Test
    public void testDefaultSplitRegex() {
        String body = "foo\nbar\nfoobar";
        PayloadConfig payloadConfig = new PayloadConfig();
        Payload payload = new Payload(payloadConfig, body);
        List<Payload> payloads = payload.split();

        Assertions.assertEquals(3, payloads.size());
        Assertions.assertEquals("foo", payloads.get(0).take());
        Assertions.assertEquals("bar", payloads.get(1).take());
        Assertions.assertEquals("foobar", payloads.get(2).take());
    }

    @Test
    public void testCustomSplitRegex() {
        System.setProperty("payload.splitRegex", ",");

        String body = "foo,bar,foobar";
        PayloadConfig payloadConfig = new PayloadConfig();
        Payload payload = new Payload(payloadConfig, body);
        List<Payload> payloads = payload.split();

        Assertions.assertEquals(3, payloads.size());
        Assertions.assertEquals("foo", payloads.get(0).take());
        Assertions.assertEquals("bar", payloads.get(1).take());
        Assertions.assertEquals("foobar", payloads.get(2).take());
    }

    @Test
    public void testNoSplittingRequired() {
        String body = "foobar";
        PayloadConfig payloadConfig = new PayloadConfig();
        Payload payload = new Payload(payloadConfig, body);
        List<Payload> payloads = payload.split();

        Assertions.assertEquals(1, payloads.size());
        Assertions.assertEquals("foobar", payloads.get(0).take());
    }
}
