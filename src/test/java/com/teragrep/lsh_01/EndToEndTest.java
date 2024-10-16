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

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EndToEndTest {

    private RelpServer relpServer;
    private NettyConfig nettyConfig;
    private Thread program;

    @BeforeAll
    void setUp() throws InterruptedException {
        System.setProperty("payload.splitType", "none");
        System.setProperty("security.authRequired", "false");
        System.setProperty("relp.port", "1601");

        // Start listening to HTTP-requests
        program = new Thread(() -> Main.main(new String[] {}));
        program.start();

        Thread.sleep(3000); // wait for netty to start up

        relpServer = new RelpServer();
        relpServer.setUpDefault();

        nettyConfig = new NettyConfig();
    }

    @AfterEach
    void reset() {
        relpServer.clear();
    }

    @AfterAll
    void tearDown() {
        System.clearProperty("payload.splitType");
        System.clearProperty("security.authRequired");
        System.clearProperty("relp.port");
        relpServer.tearDown();
        program.interrupt();
    }

    @Test
    public void testNullHeaders() {
        /*
            Have to use the old HttpURLConnection because HttpClient doesn't allow sending null headers.
         */
        Assertions.assertDoesNotThrow(() -> {
            String listenAddress = nettyConfig.listenAddress;
            if (listenAddress.equals("127.0.0.1")) {
                // HttpURLConnection doesn't work with the IP.
                listenAddress = "localhost";
            }

            URL url = new URL("http://" + listenAddress + ":" + nettyConfig.listenPort);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", null);

            Assertions.assertEquals(200, connection.getResponseCode());
        });

        List<String> payloads = relpServer.payloads();
        Assertions.assertEquals(1, payloads.size());
        Assertions.assertTrue(payloads.get(0).contains("content_type=\"\"")); // not null but empty string
    }

    @Test
    public void testMultipleRequests() {
        ArrayList<String> requestBodies = new ArrayList<>();

        HttpClient httpClient = HttpClient.newHttpClient();

        for (int i = 0; i < 100; i++) {
            String requestBody = randomString();
            requestBodies.add(requestBody);

            HttpRequest request = HttpRequest
                    .newBuilder(URI.create("http://" + nettyConfig.listenAddress + ":" + nettyConfig.listenPort))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            CompletableFuture<HttpResponse<String>> response = httpClient
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = Assertions.assertDoesNotThrow(() -> response.get().statusCode());
            Assertions.assertEquals(200, statusCode);
        }

        List<String> payloads = this.relpServer.payloads();

        Assertions.assertEquals(100, payloads.size());
        for (int i = 0; i < payloads.size(); i++) {
            Assertions.assertTrue(payloads.get(i).contains(requestBodies.get(i))); // all message bodies are correct
        }
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
