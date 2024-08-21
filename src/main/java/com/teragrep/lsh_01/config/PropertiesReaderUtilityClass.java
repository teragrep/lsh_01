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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReaderUtilityClass {

    private final String file;
    private final Properties properties = new Properties();

    public PropertiesReaderUtilityClass(String file) {
        this.file = file;
    }

    public String getStringProperty(String key) throws IllegalArgumentException {
        if (properties.isEmpty()) { // read from file just once
            try (InputStream input = new FileInputStream(file)) {
                properties.load(input);
            }
            catch (IOException ex) {
                throw new IllegalArgumentException("Can't find properties file: " + file, ex);
            }
        }

        String property = System.getProperty(key, properties.getProperty(key));
        if (property == null) {
            throw new IllegalArgumentException("Missing property: " + key);
        }
        return property;
    }

    public int getIntProperty(String key) {
        return Integer.parseInt(getStringProperty(key));
    }

    public boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(getStringProperty(key));
    }
}
