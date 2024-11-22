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
import com.teragrep.lsh_01.config.PropertiesReaderUtilityClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {

    @Test
    public void testNonexistentProperties() {
        String emptyProps = "etc/nonexistent.properties";
        PropertiesReaderUtilityClass propReader = new PropertiesReaderUtilityClass(emptyProps);

        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class, () -> propReader.getStringProperty("payload.splitEnabled")
        );
        assertEquals("Can't find properties file: " + emptyProps, e.getMessage());
    }

    @Test
    public void testProperties() {
        String props = "etc/config.properties";
        PropertiesReaderUtilityClass propReader = new PropertiesReaderUtilityClass(props);

        assertDoesNotThrow(() -> propReader.getStringProperty("payload.splitEnabled"));
    }
}
