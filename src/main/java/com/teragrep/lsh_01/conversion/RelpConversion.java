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
package com.teragrep.lsh_01.conversion;

import com.teragrep.jlt_01.StringLookupTable;
import com.teragrep.lsh_01.authentication.BasicAuthentication;
import com.teragrep.lsh_01.authentication.Subject;
import com.teragrep.lsh_01.config.LookupConfig;
import com.teragrep.lsh_01.config.SecurityConfig;
import com.teragrep.lsh_01.lookup.LookupTableFactory;
import com.teragrep.lsh_01.pool.*;
import com.teragrep.rlo_14.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class RelpConversion implements IMessageHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(RelpConversion.class);
    private final Pool<IManagedRelpConnection> relpConnectionPool;
    private final SecurityConfig securityConfig;
    private final BasicAuthentication basicAuthentication;
    private final LookupConfig lookupConfig;
    private final StringLookupTable hostnameLookup;
    private final StringLookupTable appnameLookup;

    public RelpConversion(
            Pool<IManagedRelpConnection> relpConnectionPool,
            SecurityConfig securityConfig,
            BasicAuthentication basicAuthentication,
            LookupConfig lookupConfig
    ) {
        this.relpConnectionPool = relpConnectionPool;
        this.securityConfig = securityConfig;
        this.basicAuthentication = basicAuthentication;
        this.lookupConfig = lookupConfig;
        this.hostnameLookup = new LookupTableFactory().create(lookupConfig.hostnamePath);
        this.appnameLookup = new LookupTableFactory().create(lookupConfig.appNamePath);
    }

    public boolean onNewMessage(Subject subject, Map<String, String> headers, String body) {
        try {
            sendMessage(
                    body, headers, subject.subject(), hostnameLookup.lookup(subject.subject()), appnameLookup.lookup(subject.subject())
            );
        }
        catch (Exception e) {
            LOGGER.error("Unexpected error when sending a message: <{}>", e.getMessage(), e);
            return false;
        }
        return true;
    }

    public Subject asSubject(String token) {
        return basicAuthentication.asSubject(token);
    }

    public boolean requiresToken() {
        return securityConfig.authRequired;
    }

    public RelpConversion copy() {
        LOGGER.debug("RelpConversion.copy called");
        return new RelpConversion(relpConnectionPool, securityConfig, basicAuthentication, lookupConfig);
    }

    public Map<String, String> responseHeaders() {
        return new HashMap<String, String>();
    }

    private void sendMessage(
            String message,
            Map<String, String> headers,
            String subject,
            String hostname,
            String appName
    ) {
        Instant time = Instant.now();

        // FIXME add origin sd-element: String realHostname = java.net.InetAddress.getLocalHost().getHostName();

        SDElement headerSDElement = new SDElement("lsh_01_headers@48577");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            String headerValue = header.getValue();
            if (headerValue == null) {
                headerValue = "";
            }
            headerSDElement.addSDParam(new SDParam(header.getKey(), headerValue));
        }
        SDElement sdElement = new SDElement("lsh_01@48577");
        sdElement.addSDParam("subject", subject);
        SyslogMessage syslogMessage = new SyslogMessage()
                .withTimestamp(time.toEpochMilli())
                .withAppName(appName)
                .withHostname(hostname)
                .withFacility(Facility.USER)
                .withSeverity(Severity.INFORMATIONAL)
                .withMsg(message)
                .withSDElement(headerSDElement)
                .withSDElement(sdElement);

        IManagedRelpConnection relpConnection = relpConnectionPool.get();
        relpConnection.ensureSent(syslogMessage.toRfc5424SyslogMessage().getBytes(StandardCharsets.UTF_8));
        relpConnectionPool.offer(relpConnection);
    }
}
