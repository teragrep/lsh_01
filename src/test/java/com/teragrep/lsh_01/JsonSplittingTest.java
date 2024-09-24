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

import com.teragrep.lsh_01.config.NettyConfig;
import com.teragrep.lsh_01.util.RelpServer;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JsonSplittingTest {

    private RelpServer relpServer;
    private Thread program;

    @BeforeAll
    void setUp() throws InterruptedException {
        // have to set config here before running Main
        System.setProperty("payload.splitType", "json_array");
        System.setProperty("security.authRequired", "false");
        System.setProperty("relp.port", "1601");

        // Start listening to HTTP-requests
        program = new Thread(() -> Main.main(new String[] {}));
        program.start();

        Thread.sleep(3000); // wait for netty to start up

        this.relpServer = new RelpServer();
        this.relpServer.setUpDefault();
    }

    @AfterEach
    void reset() {
        this.relpServer.clear();
    }

    @AfterAll
    void tearDown() {
        System.clearProperty("payload.splitType");
        System.clearProperty("security.authRequired");
        System.clearProperty("relp.port");
        this.relpServer.tearDown();
        program.interrupt();
    }

    @Test
    public void testJsonSplittingOneMessage() { // no splitting needed
        String message = "{\"foo\": 1}";
        String requestBody = "[\n" + message + "\n]";

        NettyConfig nettyConfig = new NettyConfig();

        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest
                .newBuilder(URI.create("http://" + nettyConfig.listenAddress + ":" + nettyConfig.listenPort))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        CompletableFuture<HttpResponse<String>> response = httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = Assertions.assertDoesNotThrow(() -> response.get().statusCode());
        Assertions.assertEquals(200, statusCode);

        List<String> payloads = this.relpServer.payloads();

        String expected = message.replaceAll("\\s", "");

        // assert that there is just one payload with the correct message
        Assertions.assertEquals(1, payloads.size());
        Assertions.assertTrue(payloads.get(0).contains(expected));
    }

    @Test
    public void testJsonSplittingTwoMessages() {
        String message1 = "{\"foo\": 1}";
        String message2 = "{\"bar\": 2}";
        String requestBody = "[\n" + message1 + ",\n" + message2 + "\n]";

        NettyConfig nettyConfig = new NettyConfig();

        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest
                .newBuilder(URI.create("http://" + nettyConfig.listenAddress + ":" + nettyConfig.listenPort))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        CompletableFuture<HttpResponse<String>> response = httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = Assertions.assertDoesNotThrow(() -> response.get().statusCode());
        Assertions.assertEquals(200, statusCode);

        List<String> payloads = this.relpServer.payloads();

        String expected1 = message1.replaceAll("\\s", "");
        String expected2 = message2.replaceAll("\\s", "");

        // assert that payload was correctly split
        Assertions.assertEquals(2, payloads.size());
        Assertions.assertTrue(payloads.get(0).contains(expected1));
        Assertions.assertFalse(payloads.get(0).contains(expected2));

        Assertions.assertTrue(payloads.get(1).contains(expected2));
        Assertions.assertFalse(payloads.get(1).contains(expected1));
    }

    @Test
    public void testJsonSplittingThreeMessages() {
        String message1 = "{\"foo\": 1}";
        String message2 = "{\"bar\": 2}";
        String message3 = "{\"foobar\": 3}";
        String requestBody = "[\n" + message1 + ",\n" + message2 + "\n, \n" + message3 + "\n]";

        NettyConfig nettyConfig = new NettyConfig();

        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest
                .newBuilder(URI.create("http://" + nettyConfig.listenAddress + ":" + nettyConfig.listenPort))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        CompletableFuture<HttpResponse<String>> response = httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = Assertions.assertDoesNotThrow(() -> response.get().statusCode());
        Assertions.assertEquals(200, statusCode);

        List<String> payloads = this.relpServer.payloads();

        String expected1 = message1.replaceAll("\\s", "");
        String expected2 = message2.replaceAll("\\s", "");
        String expected3 = message3.replaceAll("\\s", "");

        // assert that payload was correctly split
        Assertions.assertEquals(3, payloads.size());
        Assertions.assertTrue(payloads.get(0).contains(expected1));
        Assertions.assertFalse(payloads.get(0).contains(expected2));
        Assertions.assertFalse(payloads.get(0).contains(expected3));

        Assertions.assertTrue(payloads.get(1).contains(expected2));
        Assertions.assertFalse(payloads.get(1).contains(expected1));
        Assertions.assertFalse(payloads.get(1).contains(expected3));

        Assertions.assertTrue(payloads.get(2).contains(expected3));
        Assertions.assertFalse(payloads.get(2).contains(expected1));
        Assertions.assertFalse(payloads.get(2).contains(expected2));
    }

    @Test
    public void testJsonSplittingNestedObjects() {
        String payload = "{\"foo\": {\"bar\": 2}}";
        String requestBody = "[\n" + payload + "\n]";

        NettyConfig nettyConfig = new NettyConfig();

        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest
                .newBuilder(URI.create("http://" + nettyConfig.listenAddress + ":" + nettyConfig.listenPort))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        CompletableFuture<HttpResponse<String>> response = httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = Assertions.assertDoesNotThrow(() -> response.get().statusCode());
        Assertions.assertEquals(200, statusCode);

        List<String> payloads = this.relpServer.payloads();

        String expected = payload.replaceAll("\\s", "");

        // assert that payload was correctly split
        Assertions.assertEquals(1, payloads.size());
        Assertions.assertTrue(payloads.get(0).contains(expected));
    }
}
