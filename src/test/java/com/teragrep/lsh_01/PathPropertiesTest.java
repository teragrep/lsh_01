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

import com.teragrep.lsh_01.config.PathProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PathPropertiesTest {

    @Test
    public void testEquals() {
        String fileName = "src/test/resources/properties/defaultTest.properties";
        PathProperties props1 = new PathProperties(fileName);
        PathProperties props2 = new PathProperties(fileName);

        // calling functions shouldn't have effect on an immutable object
        Assertions.assertDoesNotThrow(props1::deepCopyAsUnmodifiableMap);

        Assertions.assertEquals(props1, props2);
    }

    @Test
    public void testNotEquals() {
        PathProperties props1 = new PathProperties("src/test/resources/properties/defaultTest.properties");
        PathProperties props2 = new PathProperties("src/test/resources/properties/customTest.properties");

        Assertions.assertNotEquals(props1, props2);
    }

    @Test
    public void testHashCode() {
        PathProperties props1 = new PathProperties("src/test/resources/properties/defaultTest.properties");
        PathProperties props2 = new PathProperties("src/test/resources/properties/defaultTest.properties");
        PathProperties props3 = new PathProperties("src/test/resources/properties/customTest.properties");

        Assertions.assertEquals(props1.hashCode(), props2.hashCode());
        Assertions.assertNotEquals(props1.hashCode(), props3.hashCode());
    }
}
