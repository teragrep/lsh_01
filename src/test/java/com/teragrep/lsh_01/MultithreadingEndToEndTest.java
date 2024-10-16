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
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MultithreadingEndToEndTest {

    private RelpServer relpServer;
    private Thread program;

    @BeforeAll
    void setUp() throws InterruptedException {
        // have to set config here before running Main
        System.setProperty("properties.file", "src/test/resources/properties/multithreadingTest.properties");

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
    public void testMultithreadingTenClients() {
        List<String> requestBodies = Collections.synchronizedList(new ArrayList<>());

        HttpClient httpClient = HttpClient.newHttpClient();
        NettyConfig nettyConfig = new NettyConfig();

        final int clients = 10;
        final int messagesPerClient = 100;

        ExecutorService executor = Executors.newFixedThreadPool(clients);
        List<Future<?>> futures = new ArrayList<>();

        // Send messages in parallel
        for (int i = 0; i < clients; i++) {
            Future<?> future = executor.submit(() -> {
                for (int j = 0; j < messagesPerClient; j++) {
                    String requestBody = randomString();
                    requestBodies.add(requestBody);

                    HttpRequest request = HttpRequest
                            .newBuilder(URI.create("http://" + nettyConfig.listenAddress + ":" + nettyConfig.listenPort)).POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

                    CompletableFuture<HttpResponse<String>> response = httpClient
                            .sendAsync(request, HttpResponse.BodyHandlers.ofString());

                    // Assert that there is a successful response
                    int statusCode = Assertions.assertDoesNotThrow(() -> response.get().statusCode());
                    Assertions.assertEquals(200, statusCode);
                }
            });
            futures.add(future);
        }

        // wait until all threads are done
        for (Future<?> future : futures)
            Assertions.assertDoesNotThrow(() -> future.get());

        List<String> payloads = this.relpServer.payloads(); // get the results
        Assertions.assertEquals(clients * messagesPerClient, payloads.size());

        int loops = 0;
        RFC5424Frame frame = new RFC5424Frame();
        for (String payload : payloads) {
            frame.load(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));
            Assertions.assertDoesNotThrow(frame::next);
            Assertions.assertTrue(requestBodies.contains(frame.msg.toString())); // order of payloads can differ
            loops++;
        }
        Assertions.assertEquals(clients * messagesPerClient, loops);
    }

    private String randomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random
                .ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
