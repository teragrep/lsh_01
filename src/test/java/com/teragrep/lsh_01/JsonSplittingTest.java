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
import com.teragrep.rlo_06.RFC5424Frame;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JsonSplittingTest {

    private RelpServer relpServer;
    private Thread program;

    @BeforeAll
    void setUp() throws InterruptedException {
        // have to set config here before running Main
        System.setProperty("properties.file", "src/test/resources/properties/jsonSplittingTest.properties");

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
        System.clearProperty("properties.file");
        this.relpServer.tearDown();
        program.interrupt();
    }

    @Test
    public void testJsonSplittingOneMessage() { // no splitting needed
        String message = "{\"foo\": 1}";
        String requestBody = "[\n" + message + "\n]";
        String expected = message.replaceAll("\\s", "");

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

        // assert that there is just one payload with the correct message
        Assertions.assertEquals(1, payloads.size());

        RFC5424Frame frame = new RFC5424Frame();
        frame.load(new ByteArrayInputStream(payloads.get(0).getBytes(StandardCharsets.UTF_8)));
        Assertions.assertDoesNotThrow(frame::next);

        Assertions.assertEquals(expected, frame.msg.toString());
    }

    @Test
    public void testJsonSplittingTwoMessages() {
        String message1 = "{\"foo\": 1}";
        String message2 = "{\"bar\": 2}";

        ArrayList<String> expectedList = new ArrayList<>();
        expectedList.add(message1.replaceAll("\\s", ""));
        expectedList.add(message2.replaceAll("\\s", ""));

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

        // assert that payload was correctly split
        Assertions.assertEquals(2, payloads.size());

        int loops = 0;
        RFC5424Frame frame = new RFC5424Frame();
        for (int i = 0; i < payloads.size(); i++) {
            frame.load(new ByteArrayInputStream(payloads.get(i).getBytes(StandardCharsets.UTF_8)));
            Assertions.assertDoesNotThrow(frame::next);
            Assertions.assertEquals(expectedList.get(i), frame.msg.toString());
            loops++;
        }
        Assertions.assertEquals(payloads.size(), loops);
    }

    @Test
    public void testJsonSplittingThreeMessages() {
        String message1 = "{\"foo\": 1}";
        String message2 = "{\"bar\": 2}";
        String message3 = "{\"foobar\": 3}";
        String requestBody = "[\n" + message1 + ",\n" + message2 + "\n, \n" + message3 + "\n]";

        ArrayList<String> expectedList = new ArrayList<>();
        expectedList.add(message1.replaceAll("\\s", ""));
        expectedList.add(message2.replaceAll("\\s", ""));
        expectedList.add(message3.replaceAll("\\s", ""));

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

        // assert that payload was correctly split
        Assertions.assertEquals(3, payloads.size());

        int loops = 0;
        RFC5424Frame frame = new RFC5424Frame();
        for (int i = 0; i < payloads.size(); i++) {
            frame.load(new ByteArrayInputStream(payloads.get(i).getBytes(StandardCharsets.UTF_8)));
            Assertions.assertDoesNotThrow(frame::next);
            Assertions.assertEquals(expectedList.get(i), frame.msg.toString());
            loops++;
        }
        Assertions.assertEquals(payloads.size(), loops);
    }

    @Test
    public void testJsonSplittingNestedObjects() {
        String payload = "{\"foo\": {\"bar\": 2}}";
        String expected = payload.replaceAll("\\s", "");
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

        // assert that payload was correctly split
        Assertions.assertEquals(1, payloads.size());

        RFC5424Frame frame = new RFC5424Frame();
        frame.load(new ByteArrayInputStream(payloads.get(0).getBytes(StandardCharsets.UTF_8)));
        Assertions.assertDoesNotThrow(frame::next);

        Assertions.assertEquals(expected, frame.msg.toString());
    }
}
