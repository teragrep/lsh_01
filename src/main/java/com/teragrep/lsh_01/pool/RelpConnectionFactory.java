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

public class RelpConnectionFactory implements Supplier<IManagedRelpConnection> {

    private final RelpConfig relpConfig;

    public RelpConnectionFactory(RelpConfig relpConfig) {
        this.relpConfig = relpConfig;
    }

    @Override
    public IManagedRelpConnection get() {
        RelpConnection relpConnection = new RelpConnection();

        RelpConnectionWithConfig relpConnectionWithConfig = new RelpConnectionWithConfig(relpConnection, relpConfig);
        IManagedRelpConnection managedRelpConnection = new ManagedRelpConnection(relpConnectionWithConfig);

        if (relpConfig.rebindEnabled) {
            managedRelpConnection = new RebindableRelpConnection(managedRelpConnection, relpConfig.rebindRequestAmount);
        }

        /*
         TODO remove: shouldn't be here, but there is a bug in tearDown, so we initialize connection here
         see https://github.com/teragrep/rlp_01/issues/63 for further info
         */
        managedRelpConnection.connect();

        return managedRelpConnection;
    }

}
