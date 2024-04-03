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
package com.teragrep.lsh_01.config;

public class RelpConfig {

    public final String target;
    public final String appName;
    public final String hostname;
    public final int port;
    public final int reconnectInterval;

    public RelpConfig() {
        this.target = System.getProperty("relp.target", "127.0.0.1");
        this.appName = System.getProperty("relp.appname", "lsh_01");
        this.hostname = System.getProperty("relp.hostname", "lsh");
        this.port = Integer.parseInt(System.getProperty("relp.port", "601"));
        this.reconnectInterval = Integer.parseInt(System.getProperty("relp.reconnectInterval", "1000"));
    }

    @Override
    public String toString() {
        return "RelpConfig{" + "target='" + target + '\'' + ", appName='" + appName + '\'' + ", hostname='" + hostname
                + '\'' + ", port=" + port + ", reconnectInterval=" + reconnectInterval + '}';
    }
}
