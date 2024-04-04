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

import com.teragrep.rlo_14.*;
import com.teragrep.rlp_01.RelpBatch;
import com.teragrep.rlp_01.RelpConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RelpConversion implements IMessageHandler {

    private final static Logger LOGGER = LogManager.getLogger(RelpConversion.class);
    private final RelpConnection relpConnection;
    private boolean isConnected = false;
    private final AppConfig appConfig;

    public RelpConversion(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.relpConnection = new RelpConnection();
    }

    public boolean onNewMessage(String remoteAddress, Map<String, String> headers, String body) {
        sendMessage(body, headers);
        return true;
    }

    public boolean validatesToken(String token) {
        return true;
    }

    public boolean requiresToken() {
        return false;
    }

    public RelpConversion copy() {
        LOGGER.debug("RelpConversion.copy called");
        return new RelpConversion(appConfig);
    }

    public Map<String, String> responseHeaders() {
        return new HashMap<String, String>();
    }

    private void connect() {
        boolean notConnected = true;
        while (notConnected) {
            boolean connected = false;
            try {
                String realHostname = java.net.InetAddress.getLocalHost().getHostName();
                connected = relpConnection.connect(appConfig.relpTarget, appConfig.relpPort);
            }
            catch (Exception e) {
                LOGGER
                        .error(
                                "Failed to connect to relp server <[{}]>:<[{}]>: {}", appConfig.relpTarget,
                                appConfig.relpPort, e.getMessage()
                        );
            }
            if (connected) {
                notConnected = false;
            }
            else {
                try {
                    Thread.sleep(appConfig.relpReconnectInterval);
                }
                catch (InterruptedException e) {
                    LOGGER.error("Reconnect timer interrupted, reconnecting now");
                }
            }
        }
        isConnected = true;
    }

    private void tearDown() {
        relpConnection.tearDown();
    }

    private void disconnect() {
        boolean disconnected = false;
        try {
            disconnected = relpConnection.disconnect();
        }
        catch (IllegalStateException | IOException | TimeoutException e) {
            LOGGER.error("Forcefully closing connection due to exception <{}>", e.getMessage());
        }
        finally {
            this.tearDown();
        }
        isConnected = false;
    }

    private void sendMessage(String message, Map<String, String> headers) {
        if (!isConnected) {
            connect();
        }
        final RelpBatch relpBatch = new RelpBatch();
        Instant time = Instant.now();
        SDElement sdElement = new SDElement("lsh_01@48577");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            sdElement.addSDParam(new SDParam(header.getKey(), header.getValue()));
        }
        SyslogMessage syslogMessage = new SyslogMessage()
                .withTimestamp(time.toEpochMilli())
                .withAppName(appConfig.relpAppName)
                .withHostname(appConfig.relpHostname)
                .withFacility(Facility.USER)
                .withSeverity(Severity.INFORMATIONAL)
                .withMsg(message)
                .withSDElement(sdElement);
        relpBatch.insert(syslogMessage.toRfc5424SyslogMessage().getBytes(StandardCharsets.UTF_8));
        boolean notSent = true;
        while (notSent) {
            try {
                relpConnection.commit(relpBatch);
            }
            catch (IllegalStateException | IOException | TimeoutException e) {
                LOGGER.error("Failed to send relp message: <{}>", e.getMessage());
            }
            if (!relpBatch.verifyTransactionAll()) {
                relpBatch.retryAllFailed();
                this.tearDown();
                this.connect();
            }
            else {
                notSent = false;
            }
        }
    }
}
