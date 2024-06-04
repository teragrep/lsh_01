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
import com.teragrep.lsh_01.pool.*;
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
        System.setProperty("relp.port", "1601");

        this.relpServer = new RelpServer();
        this.relpServer.setUpCounting();
    }

    @BeforeEach
    void setProperties() {
        System.setProperty("relp.rebindEnabled", "true");
        System.setProperty("relp.rebindRequestAmount", "5");
    }

    @AfterEach
    void reset() {
        this.relpServer.clear();
    }

    @AfterAll
    void tearDown() {
        System.clearProperty("relp.rebindEnabled");
        System.clearProperty("relp.rebindRequestAmount");
        System.clearProperty("relp.port");
        this.relpServer.tearDown();
    }

    @Test
    public void testRebind() {
        RelpConfig relpConfig = new RelpConfig();
        RebindableRelpConnection connection = new RebindableRelpConnection(
                new ManagedRelpConnection(new RelpConnectionWithConfig(new RelpConnection(), relpConfig)),
                relpConfig.rebindRequestAmount
        );

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

    @Test
    public void testRebindMultipleConnections() {
        RelpConfig relpConfig = new RelpConfig();

        // Multiple connections together using the RelpConnectionFactory and the Pool
        RelpConnectionFactory relpConnectionFactory = new RelpConnectionFactory(relpConfig);
        Pool<IManagedRelpConnection> pool = new Pool<>(relpConnectionFactory, new ManagedRelpConnectionStub());

        IManagedRelpConnection firstConnection = pool.get();
        IManagedRelpConnection secondConnection = pool.get();
        IManagedRelpConnection thirdConnection = pool.get();

        SyslogMessage syslogMessage = new SyslogMessage()
                .withFacility(Facility.USER)
                .withSeverity(Severity.INFORMATIONAL)
                .withMsg("foobar");

        byte[] bytes = syslogMessage.toRfc5424SyslogMessage().getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i <= relpConfig.rebindRequestAmount * 3; i++) { // 3 rebinds
            firstConnection.ensureSent(bytes);
            pool.offer(firstConnection);
            secondConnection.ensureSent(bytes);
            pool.offer(secondConnection);
            thirdConnection.ensureSent(bytes);
            pool.offer(thirdConnection);
        }

        List<CountingFrameDelegate> frameDelegates = relpServer.frameDelegates();
        Assertions.assertEquals(12, frameDelegates.size());

        for (int i = 0; i < 9; i++) {
            Assertions.assertEquals(relpConfig.rebindRequestAmount, frameDelegates.get(i).recordsReceived());
        }

        for (int i = 9; i < 12; i++) {
            Assertions.assertEquals(1, frameDelegates.get(i).recordsReceived()); // last one receives only 1 message
        }
    }

    @Test
    public void testRebindDisabled() {
        System.setProperty("relp.rebindEnabled", "false");

        RelpConfig relpConfig = new RelpConfig();

        RelpConnectionFactory relpConnectionFactory = new RelpConnectionFactory(relpConfig);
        Pool<IManagedRelpConnection> pool = new Pool<>(relpConnectionFactory, new ManagedRelpConnectionStub());

        IManagedRelpConnection connection = pool.get();

        SyslogMessage syslogMessage = new SyslogMessage()
                .withFacility(Facility.USER)
                .withSeverity(Severity.INFORMATIONAL)
                .withMsg("foobar");

        for (int i = 0; i <= relpConfig.rebindRequestAmount; i++) { // rebind + 1
            connection.ensureSent(syslogMessage.toRfc5424SyslogMessage().getBytes(StandardCharsets.UTF_8));
        }

        // Only one connection should have been used with rebindEnabled = false
        Assertions.assertEquals(1, relpServer.frameDelegates().size());
        Assertions
                .assertEquals(relpConfig.rebindRequestAmount + 1, relpServer.frameDelegates().get(0).recordsReceived());
    }

    @Test
    public void testCloseWithoutConnecting() {
        ManagedRelpConnection managedRelpConnection = new ManagedRelpConnection(
                new RelpConnectionWithConfig(new RelpConnection(), new RelpConfig())
        );
        Assertions.assertDoesNotThrow(managedRelpConnection::close);
    }

    @Test
    public void testThrowsWithInvalidRebindRequestAmount_negative() {
        System.setProperty("relp.rebindRequestAmount", "-1");

        Assertions.assertThrows(IllegalArgumentException.class, () -> new RelpConfig().validate());
    }

    @Test
    public void testThrowsWithInvalidRebindRequestAmount_zero() {
        System.setProperty("relp.rebindRequestAmount", "0");

        Assertions.assertThrows(IllegalArgumentException.class, () -> new RelpConfig().validate());
    }
}
