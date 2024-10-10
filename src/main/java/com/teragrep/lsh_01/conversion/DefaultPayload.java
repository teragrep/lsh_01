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
package com.teragrep.lsh_01.conversion;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DefaultPayload implements Payload {

    private final String message;

    public DefaultPayload(String message) {
        this.message = message;
    }

    @Override
    public List<String> messages() {
        return Collections.singletonList(message);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (object.getClass() != this.getClass())
            return false;
        final DefaultPayload cast = (DefaultPayload) object;
        return message.equals(cast.message);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(message);
    }
}
