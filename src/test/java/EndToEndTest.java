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
import com.teragrep.lsh_01.Main;
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
import java.util.concurrent.ExecutionException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EndToEndTest {

    private RelpServer relpServer;
    private NettyConfig nettyConfig;

    @BeforeAll
    void setUp() throws InterruptedException {
        System.setProperty("payload.splitEnabled", "true");
        System.setProperty("security.authRequired", "false");

        // Start listening to HTTP-requests
        Thread program = new Thread(() -> Main.main(new String[]{}));
        program.start();

        Thread.sleep(3000); // wait for netty to start up

        this.relpServer = new RelpServer();
        this.relpServer.setUp();

        this.nettyConfig = new NettyConfig();
    }

    @AfterEach
    void reset() {
        this.relpServer.clear();
    }

    @AfterAll
    void tearDown() {
        this.relpServer.tearDown();
    }

    @Test
    public void testSplittingMessage1() throws InterruptedException, ExecutionException {
        String requestBody = "foofoo\nbar";

        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest
                .newBuilder(URI.create("http://" + nettyConfig.listenAddress + ":" + nettyConfig.listenPort))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        CompletableFuture<HttpResponse<String>> response = httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, response.get().statusCode());

        List<String> payloads = this.relpServer.payloads();

        // assert that payload was correctly split into two
        Assertions.assertEquals(2, payloads.size());
        Assertions.assertTrue(payloads.get(0).contains("foofoo"));
        Assertions.assertFalse(payloads.get(0).contains("bar"));
        Assertions.assertTrue(payloads.get(1).contains("bar"));
        Assertions.assertFalse(payloads.get(1).contains("foofoo"));
    }

    @Test
    public void testSplittingMessage2() throws InterruptedException, ExecutionException {
        String requestBody = "foofoo\nbar\nfoo bar";

        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest
                .newBuilder(URI.create("http://" + nettyConfig.listenAddress + ":" + nettyConfig.listenPort))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        CompletableFuture<HttpResponse<String>> response = httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, response.get().statusCode());

        List<String> payloads = this.relpServer.payloads();

        // assert that payload was correctly split into three parts
        Assertions.assertEquals(3, payloads.size());
        Assertions.assertTrue(payloads.get(0).contains("foofoo"));
        Assertions.assertFalse(payloads.get(0).contains("bar"));
        Assertions.assertTrue(payloads.get(1).contains("bar"));
        Assertions.assertFalse(payloads.get(1).contains("foofoo"));
        Assertions.assertTrue(payloads.get(2).contains("foo bar"));
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
    public void testMultipleRequests() throws ExecutionException, InterruptedException {
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

            Assertions.assertEquals(200, response.get().statusCode());
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
