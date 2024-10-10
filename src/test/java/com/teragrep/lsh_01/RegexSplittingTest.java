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
public class RegexSplittingTest {

    private RelpServer relpServer;
    private Thread program;
    private NettyConfig nettyConfig;

    @BeforeAll
    void setUp() throws InterruptedException {
        System.setProperty("properties.file", "src/test/resources/properties/regexSplittingTest.properties");

        // Start listening to HTTP-requests
        program = new Thread(() -> Main.main(new String[] {}));
        program.start();

        Thread.sleep(3000); // wait for netty to start up

        this.relpServer = new RelpServer();
        this.relpServer.setUpDefault();

        this.nettyConfig = new NettyConfig();
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
    public void testRegexSplittingTwoMessages() {
        String requestBody = "foofoo\nbar";

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

        // assert that payload was correctly split into two
        Assertions.assertEquals(2, payloads.size());
        Assertions.assertTrue(payloads.get(0).contains("foofoo"));
        Assertions.assertFalse(payloads.get(0).contains("bar"));
        Assertions.assertTrue(payloads.get(1).contains("bar"));
        Assertions.assertFalse(payloads.get(1).contains("foofoo"));
    }

    @Test
    public void testRegexSplittingThreeMessages() {
        String requestBody = "foofoo\nbar\nfoo bar";

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

        // assert that payload was correctly split into three parts
        Assertions.assertEquals(3, payloads.size());
        Assertions.assertTrue(payloads.get(0).contains("foofoo"));
        Assertions.assertFalse(payloads.get(0).contains("bar"));
        Assertions.assertTrue(payloads.get(1).contains("bar"));
        Assertions.assertFalse(payloads.get(1).contains("foofoo"));
        Assertions.assertTrue(payloads.get(2).contains("foo bar"));
    }
}
