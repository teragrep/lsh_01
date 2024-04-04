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

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Properties;

public class AppConfig {

    public final String listenAddress;
    public final int listenPort;
    public final int serverThreads;
    public final int serverMaxPendingRequests;
    public final int serverMaxContentLength;
    public final String relpTarget;
    public final int relpPort;
    public final int relpReconnectInterval;
    public final String relpHostname;
    public final String relpAppName;
    private final Properties localProperties = new Properties();

    public AppConfig() {
        try (InputStream input = new FileInputStream(System.getProperty("properties.file", "etc/config.properties"))) {
            localProperties.load(input);
        }
        catch (IOException ex) {
            throw new IllegalArgumentException("Can't find properties file: ", ex);
        }
        listenAddress = getStringProperty("listen.address");
        listenPort = getIntProperty("listen.port");
        serverThreads = getIntProperty("server.threads");
        serverMaxPendingRequests = getIntProperty("server.maxPendingRequests");
        serverMaxContentLength = getIntProperty("server.maxContentLength");
        relpTarget = getStringProperty("relp.target");
        relpPort = getIntProperty("relp.port");
        relpReconnectInterval = getIntProperty("relp.reconnectInterval");
        relpHostname = getStringProperty("relp.hostname");
        relpAppName = getStringProperty("relp.appName");
    }

    private String getStringProperty(String key) throws IllegalArgumentException {
        String property = System.getProperty(key, localProperties.getProperty(key));
        if (property == null) {
            throw new IllegalArgumentException("Missing property: " + key);
        }
        return property;
    }

    private int getIntProperty(String key) {
        return Integer.parseInt(getStringProperty(key));
    }

    public void validate() {
        // Fixme
    }

    @Override
    public String toString() {
        return "AppConfig{" + "listenAddress='" + listenAddress + '\'' + ", listenPort=" + listenPort
                + ", serverThreads=" + serverThreads + ", serverMaxPendingRequests=" + serverMaxPendingRequests
                + ", serverMaxContentLength=" + serverMaxContentLength + ", relpTarget='" + relpTarget + '\''
                + ", relpPort=" + relpPort + "}";
    }
}
