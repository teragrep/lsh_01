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

/**
 * This class is implemented in ruby in `lib/logstash/inputs/http/message_listener`,
 * this class is used to link the events triggered from the different connection to the actual
 * work inside the plugin.
 */
// This need to be implemented in Ruby
public class MessageHandler implements IMessageHandler {
    private final static Logger logger = LogManager.getLogger(MessageHandler.class);

    /**
     * This is triggered on every new message parsed by the http handler
     * and should be executed in the ruby world.
     *
     * @param remoteAddress
     * @param headers
     * @param body
     */
    public boolean onNewMessage(String remoteAddress, Map<String,String> headers, String body) {
        logger.debug("onNewMessage");
        return false;
    }

    public MessageHandler copy() {
        logger.debug("copy");
        return new MessageHandler();
    }

    public boolean validatesToken(String token) {
        logger.debug("validatesToken");
        return false;
    }

    public Map<String, String> responseHeaders() {
        logger.debug("responseHeaders");
        return null;
    }

    public boolean requiresToken() { return false; }
}
