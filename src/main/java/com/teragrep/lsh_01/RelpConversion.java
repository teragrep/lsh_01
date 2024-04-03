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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class RelpConversion implements IMessageHandler {

    private final static Logger LOGGER = LogManager.getLogger(RelpConversion.class);

    public boolean onNewMessage(String remoteAddress, Map<String, String> headers, String body) {
        LOGGER.error("RelpConversion onNewMessage not yet implemented. Contact assistance.");
        return false;
    }

    public boolean validatesToken(String token) {
        LOGGER.error("RelpConversion validatesToken not yet implemented. Contact assistance.");
        return false;
    }

    public boolean requiresToken() {
        LOGGER.error("RelpConversion requiresToken not yet implemented. Contact assistance.");
        return false;
    }

    public RelpConversion copy() {
        LOGGER.debug("RelpConversion.copy called");
        return new RelpConversion();
    }

    public Map<String, String> responseHeaders() {
        LOGGER.error("RelpConversion responseHeaders not yet implemented. Contact assistance.");
        return null;
    }
}
