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
package com.teragrep.lsh_01.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObjectAggregator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggingHttpObjectAggregator extends HttpObjectAggregator {

    private static final Logger LOGGER = LogManager.getLogger(LoggingHttpObjectAggregator.class);

    public LoggingHttpObjectAggregator(int maxContentLength) {
        super(maxContentLength);
    }

    public LoggingHttpObjectAggregator(int maxContentLength, boolean closeOnExpectationFailed) {
        super(maxContentLength, closeOnExpectationFailed);
    }

    @Override
    protected void handleOversizedMessage(ChannelHandlerContext ctx, HttpMessage oversized) throws Exception {
        LOGGER.warn("Oversized HTTP message received, exceeds maxContentLength <{}>", maxContentLength());
        super.handleOversizedMessage(ctx, oversized);
    }

}
