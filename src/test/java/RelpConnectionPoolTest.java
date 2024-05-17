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
import com.teragrep.lsh_01.config.RelpConfig;
import com.teragrep.lsh_01.pool.IRelpConnection;
import com.teragrep.lsh_01.pool.RelpConnectionFactory;
import com.teragrep.lsh_01.pool.RelpConnectionPool;
import com.teragrep.lsh_01.util.RelpServer;
import com.teragrep.rlp_01.RelpBatch;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RelpConnectionPoolTest {

    private RelpServer relpServer;

    @BeforeAll
    void setUp() {
        System.setProperty("relp.reconnectInterval", "1000");

        this.relpServer = new RelpServer();
        this.relpServer.setUp();
    }

    @BeforeEach
    public void addProperties() {
        // defaults
        System.setProperty("relp.rebindRequestAmount", "1000000");
        System.setProperty("relp.rebindEnabled", "false");
    }

    @AfterEach
    void reset() {
        System.clearProperty("relp.rebindRequestAmount");
        System.clearProperty("relp.rebindEnabled");
        this.relpServer.clear();
    }

    @AfterAll
    void tearDown() {
        System.clearProperty("relp.reconnectInterval");
        this.relpServer.tearDown();
    }

    @Test
    public void multipleRebindsTest() throws IOException, TimeoutException {
        System.setProperty("relp.rebindEnabled", "true");
        System.setProperty("relp.rebindRequestAmount", "1000");

        RelpConfig relpConfig = new RelpConfig();
        RelpConnectionFactory relpConnectionFactory = new RelpConnectionFactory(relpConfig);
        RelpConnectionPool relpConnectionPool = new RelpConnectionPool(
                relpConnectionFactory,
                relpConfig.rebindRequestAmount,
                relpConfig.rebindEnabled
        );

        IRelpConnection initialRelpConnection = relpConnectionPool.take();
        initialRelpConnection.commit(new RelpBatch());
        relpConnectionPool.offer(initialRelpConnection);
        IRelpConnection newRelpConnection;

        for (int i = 0; i < 3; i++) { // three rebinds
            for (int j = 0; j < relpConfig.rebindRequestAmount - 1; j++) {
                newRelpConnection = relpConnectionPool.take();
                Assertions.assertEquals(initialRelpConnection, newRelpConnection); // reuse the same connection

                newRelpConnection.commit(new RelpBatch()); // send empty batch to RELP server

                relpConnectionPool.offer(newRelpConnection);
            }

            newRelpConnection = relpConnectionPool.take();
            newRelpConnection.commit(new RelpBatch());
            relpConnectionPool.offer(newRelpConnection);

            // after rebindRequestAmount of requests, there should be a new connection
            Assertions.assertNotEquals(initialRelpConnection, newRelpConnection);
            initialRelpConnection = newRelpConnection;
        }
    }
}
