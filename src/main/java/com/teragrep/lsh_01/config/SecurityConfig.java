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

public class SecurityConfig implements Validateable {

    public final boolean authRequired;

    public SecurityConfig() {
        PropertiesReaderUtilityClass propertiesReader = new PropertiesReaderUtilityClass(
                System.getProperty("properties.file", "etc/config.properties")
        );
        authRequired = propertiesReader.getBooleanProperty("security.authRequired");
    }

    @Override
    public void validate() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final SecurityConfig cast = (SecurityConfig) o;
        return authRequired == cast.authRequired;
    }
}
