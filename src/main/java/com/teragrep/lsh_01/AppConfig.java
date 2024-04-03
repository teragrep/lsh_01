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

public class AppConfig {

    public final String hostname;
    public final int port;
    public final int threads;
    public final int maxPendingRequests;
    public final int maxContentLength;

    public AppConfig() {
        hostname = System.getProperty("hostname", "localhost");
        port = Integer.parseInt(System.getProperty("port", "8080"));
        threads = Integer.parseInt(System.getProperty("threads", "1"));
        maxPendingRequests = Integer.parseInt(System.getProperty("maxPendingRequests", "128"));
        maxContentLength = Integer.parseInt(System.getProperty("maxContentLength", "20000"));
    }

    public void validate() throws IllegalArgumentException {
        if (hostname == null || hostname.isEmpty()) {
            throw new IllegalArgumentException("Hostname must be specified");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 0 and 65535, was <[" + port + "]>");
        }
        if (threads <= 0) {
            throw new IllegalArgumentException("Threads must be positive, was <[" + threads + "]>");
        }
        if (maxPendingRequests <= 0) {
            throw new IllegalArgumentException(
                    "MaxPendingRequests must be positive, was <[" + maxPendingRequests + "]>"
            );
        }
        if (maxContentLength <= 0) {
            throw new IllegalArgumentException("MaxContentLength must be positive, was <[" + maxContentLength + "]>");
        }
    }

    @Override
    public String toString() {
        return "AppConfig{" + "hostname='" + hostname + '\'' + ", port=" + port + ", threads=" + threads
                + ", maxPendingRequests=" + maxPendingRequests + ", maxContentLength=" + maxContentLength + '}';
    }
}
