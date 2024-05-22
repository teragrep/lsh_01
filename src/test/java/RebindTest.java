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
import com.teragrep.lsh_01.pool.ManagedRelpConnection;
import com.teragrep.lsh_01.pool.RebindableRelpConnection;
import com.teragrep.lsh_01.pool.RelpConnectionWithConfig;
import com.teragrep.lsh_01.util.CountingFrameDelegate;
import com.teragrep.lsh_01.util.RelpServer;
import com.teragrep.rlo_14.Facility;
import com.teragrep.rlo_14.Severity;
import com.teragrep.rlo_14.SyslogMessage;
import com.teragrep.rlp_01.RelpConnection;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RebindTest {

    private RelpServer relpServer;

    @BeforeAll
    void setUp() {
        System.setProperty("relp.rebindEnabled", "true");
        System.setProperty("relp.rebindRequestAmount", "5");
        System.setProperty("relp.reconnectInterval", "1000");

        this.relpServer = new RelpServer();
        this.relpServer.setUpCounting();
    }

    @AfterEach
    void reset() {
        this.relpServer.clear();
    }

    @AfterAll
    void tearDown() {
        System.clearProperty("relp.rebindEnabled");
        System.clearProperty("relp.rebindRequestAmount");
        System.clearProperty("relp.reconnectInterval");
        this.relpServer.tearDown();
    }

    @Test
    public void testRebind() {
        RelpConfig relpConfig = new RelpConfig();
        RebindableRelpConnection connection = new RebindableRelpConnection(
                new ManagedRelpConnection(new RelpConnectionWithConfig(new RelpConnection(), relpConfig)),
                relpConfig.rebindRequestAmount
        );
        connection.connect();

        SyslogMessage syslogMessage = new SyslogMessage()
                .withFacility(Facility.USER)
                .withSeverity(Severity.INFORMATIONAL)
                .withMsg("foobar");

        for (int i = 0; i <= relpConfig.rebindRequestAmount * 3; i++) { // 3 rebinds
            connection.ensureSent(syslogMessage.toRfc5424SyslogMessage().getBytes(StandardCharsets.UTF_8));
        }

        List<CountingFrameDelegate> frameDelegates = relpServer.frameDelegates();
        Assertions.assertEquals(4, frameDelegates.size());

        Assertions.assertEquals(relpConfig.rebindRequestAmount, frameDelegates.get(0).recordsReceived());
        Assertions.assertEquals(relpConfig.rebindRequestAmount, frameDelegates.get(1).recordsReceived());
        Assertions.assertEquals(relpConfig.rebindRequestAmount, frameDelegates.get(2).recordsReceived());
        Assertions.assertEquals(1, frameDelegates.get(3).recordsReceived()); // last one receives only 1 message
    }
}
