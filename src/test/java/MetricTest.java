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

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.teragrep.lsh_01.config.RelpConfig;
import com.teragrep.lsh_01.pool.IRelpConnection;
import com.teragrep.lsh_01.pool.ManagedRelpConnection;
import com.teragrep.rlo_14.Facility;
import com.teragrep.rlo_14.Severity;
import com.teragrep.rlo_14.SyslogMessage;
import fakes.RelpConnectionFake;
import fakes.ResendingRelpConnectionFake;
import fakes.ThrowingRelpConnectionFake;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static com.codahale.metrics.MetricRegistry.name;

public class MetricTest {

    @Test
    public void testRecordMetric() {
        final int messages = 100;

        final SyslogMessage syslogMessage = new SyslogMessage()
                .withSeverity(Severity.INFORMATIONAL)
                .withFacility(Facility.LOCAL0)
                .withMsgId("123")
                .withMsg("test");

        RelpConfig relpConfig = new RelpConfig();
        IRelpConnection relpConnection = new RelpConnectionFake(relpConfig);
        MetricRegistry registry = new MetricRegistry();

        try (ManagedRelpConnection managedRelpConnection = new ManagedRelpConnection(relpConnection, registry)) {
            for (int i = 0; i < messages; i++) {
                managedRelpConnection.ensureSent(syslogMessage.toRfc5424SyslogMessage().getBytes(StandardCharsets.UTF_8));
            }
        }

        Counter recordsCounter = registry.counter(name(ManagedRelpConnection.class, "records"));
        Assertions.assertEquals(messages, recordsCounter.getCount());
    }

    @Test
    public void testBytesMetric() {
        final int messages = 100;

        final SyslogMessage syslogMessage = new SyslogMessage()
                .withSeverity(Severity.INFORMATIONAL)
                .withFacility(Facility.LOCAL0)
                .withMsgId("123")
                .withMsg("test");

        final byte[] bytes = syslogMessage.toRfc5424SyslogMessage().getBytes(StandardCharsets.UTF_8);
        final int bytesLength = bytes.length;

        RelpConfig relpConfig = new RelpConfig();
        IRelpConnection relpConnection = new RelpConnectionFake(relpConfig);
        MetricRegistry registry = new MetricRegistry();

        try (ManagedRelpConnection managedRelpConnection = new ManagedRelpConnection(relpConnection, registry)) {
            for (int i = 0; i < messages; i++) {
                managedRelpConnection.ensureSent(bytes);
            }
        }

        Counter bytesCounter = registry.counter(name(ManagedRelpConnection.class, "bytes"));
        Assertions.assertEquals(bytesLength, bytesCounter.getCount());
    }

    @Test
    public void testResendsMetric() {
        final int messages = 100;
        final int resends = 50;

        final SyslogMessage syslogMessage = new SyslogMessage()
                .withSeverity(Severity.INFORMATIONAL)
                .withFacility(Facility.LOCAL0)
                .withMsgId("123")
                .withMsg("test");

        RelpConfig relpConfig = new RelpConfig();
        IRelpConnection relpConnection = new ResendingRelpConnectionFake(new RelpConnectionFake(relpConfig), resends);
        MetricRegistry registry = new MetricRegistry();

        try (ManagedRelpConnection managedRelpConnection = new ManagedRelpConnection(relpConnection, registry)) {
            for (int i = 0; i < messages; i++) {
                managedRelpConnection.ensureSent(syslogMessage.toRfc5424SyslogMessage().getBytes(StandardCharsets.UTF_8));
            }
        }

        Counter resendsCounter = registry.counter(name(ManagedRelpConnection.class, "resends"));
        Assertions.assertEquals(resends, resendsCounter.getCount());
    }

    @Test
    public void testConnectsMetric() {
        final int messages = 100;

        final SyslogMessage syslogMessage = new SyslogMessage()
                .withSeverity(Severity.INFORMATIONAL)
                .withFacility(Facility.LOCAL0)
                .withMsgId("123")
                .withMsg("test");

        RelpConfig relpConfig = new RelpConfig();
        IRelpConnection relpConnection = new RelpConnectionFake(relpConfig);
        MetricRegistry registry = new MetricRegistry();

        try (ManagedRelpConnection managedRelpConnection = new ManagedRelpConnection(relpConnection, registry)) {
            for (int i = 0; i < messages; i++) {
                managedRelpConnection.ensureSent(syslogMessage.toRfc5424SyslogMessage().getBytes(StandardCharsets.UTF_8));
            }
        }

        Counter connectsCounter = registry.counter(name(ManagedRelpConnection.class, "connects"));
        Assertions.assertEquals(1, connectsCounter.getCount()); // just the initial connect (1)
    }

    @Test
    public void testRetriedConnectsMetric() {
        System.setProperty("relp.reconnectInterval", "1"); // set reconnect interval so the test is faster

        final int messages = 100;
        final int reconnects = 10;

        final SyslogMessage syslogMessage = new SyslogMessage()
                .withSeverity(Severity.INFORMATIONAL)
                .withFacility(Facility.LOCAL0)
                .withMsgId("123")
                .withMsg("test");

        RelpConfig relpConfig = new RelpConfig();
        IRelpConnection relpConnection = new ThrowingRelpConnectionFake(new RelpConnectionFake(relpConfig), reconnects);
        MetricRegistry registry = new MetricRegistry();

        try (ManagedRelpConnection managedRelpConnection = new ManagedRelpConnection(relpConnection, registry)) {
            for (int i = 0; i < messages; i++) {
                managedRelpConnection.ensureSent(syslogMessage.toRfc5424SyslogMessage().getBytes(StandardCharsets.UTF_8));
            }
        }

        Counter retriedConnectsCounter = registry.counter(name(ManagedRelpConnection.class, "retriedConnects"));
        Assertions.assertEquals(reconnects, retriedConnectsCounter.getCount());
    }

    @Test
    public void testSendLatencyMetric() {
        final int messages = 10;
        final int sendLatency = 10; // sleep for 10ms after sending a message (commit)

        final SyslogMessage syslogMessage = new SyslogMessage()
                .withSeverity(Severity.INFORMATIONAL)
                .withFacility(Facility.LOCAL0)
                .withMsgId("123")
                .withMsg("test");

        RelpConfig relpConfig = new RelpConfig();
        IRelpConnection relpConnection = new RelpConnectionFake(relpConfig, sendLatency, 0);
        MetricRegistry registry = new MetricRegistry();

        try (ManagedRelpConnection managedRelpConnection = new ManagedRelpConnection(relpConnection, registry)) {
            for (int i = 0; i < messages; i++) {
                managedRelpConnection.ensureSent(syslogMessage.toRfc5424SyslogMessage().getBytes(StandardCharsets.UTF_8));
            }
        }

        Timer sendLatencyTimer = registry.timer(name(ManagedRelpConnection.class, "sendLatency"));
        // mean rate means how many timer updates per second there were
        Assertions.assertTrue(sendLatency * messages <= sendLatencyTimer.getMeanRate());
    }

    @Test
    public void testConnectLatencyMetric() {
        final int messages = 10;
        final int connectLatency = 200; // sleep for 200ms after connecting to RELP

        final SyslogMessage syslogMessage = new SyslogMessage()
                .withSeverity(Severity.INFORMATIONAL)
                .withFacility(Facility.LOCAL0)
                .withMsgId("123")
                .withMsg("test");

        RelpConfig relpConfig = new RelpConfig();
        IRelpConnection relpConnection = new RelpConnectionFake(relpConfig, 0, connectLatency);
        MetricRegistry registry = new MetricRegistry();

        try (ManagedRelpConnection managedRelpConnection = new ManagedRelpConnection(relpConnection, registry)) {
            for (int i = 0; i < messages; i++) {
                managedRelpConnection.ensureSent(syslogMessage.toRfc5424SyslogMessage().getBytes(StandardCharsets.UTF_8));
            }
        }

        Timer connectLatencyTimer = registry.timer(name(ManagedRelpConnection.class, "connectLatency"));
        // mean rate means how many timer updates per second there were
        Assertions.assertTrue(connectLatencyTimer.getMeanRate() >= (double) (1000 / connectLatency) / 2); // rate is higher than this
        Assertions.assertTrue(connectLatencyTimer.getMeanRate() <= (double) 1000 / connectLatency); // rate is lower than this
    }
}
