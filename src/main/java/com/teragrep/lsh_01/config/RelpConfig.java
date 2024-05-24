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

public class RelpConfig implements Validateable {

    public final String relpTarget;
    public final int relpPort;
    public final int relpReconnectInterval;
    public final int rebindRequestAmount;
    public final boolean rebindEnabled;

    public RelpConfig() {
        PropertiesReaderUtilityClass propertiesReader = new PropertiesReaderUtilityClass(
                System.getProperty("properties.file", "etc/config.properties")
        );
        relpTarget = propertiesReader.getStringProperty("relp.target");
        relpPort = propertiesReader.getIntProperty("relp.port");
        relpReconnectInterval = propertiesReader.getIntProperty("relp.reconnectInterval");
        rebindRequestAmount = propertiesReader.getIntProperty("relp.rebindRequestAmount");
        rebindEnabled = propertiesReader.getBooleanProperty("relp.rebindEnabled");
    }

    @Override
    public void validate() {
        if (rebindEnabled && rebindRequestAmount < 1) {
            throw new IllegalArgumentException("relp.rebindRequestAmount has to be a positive number");
        }
    }

    @Override
    public String toString() {
        return "RelpConfig{" + "relpTarget='" + relpTarget + '\'' + ", relpPort=" + relpPort
                + ", relpReconnectInterval=" + relpReconnectInterval + ", rebindRequestAmount=" + rebindRequestAmount
                + ", rebindEnabled=" + rebindEnabled + '}';
    }
}
