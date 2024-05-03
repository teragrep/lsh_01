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
package com.teragrep.lsh_01.pool;

import com.teragrep.lsh_01.config.RelpConfig;
import com.teragrep.rlp_01.RelpConnection;

import java.util.function.Supplier;

public class RelpConnectionFactory implements Supplier<IRelpConnection> {

    private final RelpConfig relpConfig;

    public RelpConnectionFactory(RelpConfig relpConfig) {
        this.relpConfig = relpConfig;
    }

    @Override
    public IRelpConnection get() {
        RelpConnection relpConnection = new RelpConnection();
        return new RelpConnectionWrap(relpConnection, relpConfig);
    }

}
