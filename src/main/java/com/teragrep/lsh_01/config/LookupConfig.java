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

import java.util.Objects;

public class LookupConfig implements Validateable {

    public final String hostnamePath;
    public final String appNamePath;

    public LookupConfig() {
        PropertiesReaderUtilityClass propertiesReader = new PropertiesReaderUtilityClass(
                System.getProperty("properties.file", "etc/config.properties")
        );
        hostnamePath = propertiesReader.getStringProperty("lookups.hostname.file");
        appNamePath = propertiesReader.getStringProperty("lookups.appname.file");
    }

    @Override
    public void validate() {
    }

    @Override
    public String toString() {
        return "LookupConfig{" + "hostnamePath='" + hostnamePath + '\'' + ", appNamePath='" + appNamePath + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final LookupConfig cast = (LookupConfig) o;
        return hostnamePath.equals(cast.hostnamePath) && appNamePath.equals(cast.appNamePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostnamePath, appNamePath);
    }
}
