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

import com.teragrep.lsh_01.config.SecurityConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SecurityConfigTest {

    @Test
    public void testEquals() {
        System.setProperty("properties.file", "src/test/resources/properties/defaultTest.properties");
        SecurityConfig config1 = new SecurityConfig();
        SecurityConfig config2 = new SecurityConfig();

        // calling functions shouldn't have effect on an immutable object
        config1.validate();

        Assertions.assertEquals(config1, config2);
        System.clearProperty("properties.file");
    }

    @Test
    public void testNotEquals() {
        System.setProperty("properties.file", "src/test/resources/properties/defaultTest.properties");
        SecurityConfig config1 = new SecurityConfig();
        System.setProperty("properties.file", "src/test/resources/properties/customTest.properties");
        SecurityConfig config2 = new SecurityConfig();

        Assertions.assertNotEquals(config1, config2);
        System.clearProperty("properties.file");
    }

    @Test
    public void testHashCode() {
        System.setProperty("properties.file", "src/test/resources/properties/defaultTest.properties");
        SecurityConfig config1 = new SecurityConfig();
        SecurityConfig config2 = new SecurityConfig();
        System.setProperty("properties.file", "src/test/resources/properties/customTest.properties");
        SecurityConfig config3 = new SecurityConfig();

        Assertions.assertEquals(config1.hashCode(), config2.hashCode());
        Assertions.assertNotEquals(config1.hashCode(), config3.hashCode());
        System.clearProperty("properties.file");
    }
}
