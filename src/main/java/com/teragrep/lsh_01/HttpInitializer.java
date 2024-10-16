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

import com.teragrep.lsh_01.config.InternalEndpointUrlConfig;
import com.teragrep.lsh_01.conversion.IMessageHandler;
import com.teragrep.lsh_01.util.LoggingHttpObjectAggregator;
import com.teragrep.lsh_01.util.SslHandlerProvider;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by joaoduarte on 11/10/2017.
 */
public class HttpInitializer extends ChannelInitializer<SocketChannel> {

    private final IMessageHandler messageHandler;
    private SslHandlerProvider sslHandlerProvider;
    private final int maxContentLength;
    private final HttpResponseStatus responseStatus;
    private final ThreadPoolExecutor executorGroup;
    private final InternalEndpointUrlConfig internalEndpointUrlConfig;

    public HttpInitializer(
            IMessageHandler messageHandler,
            ThreadPoolExecutor executorGroup,
            int maxContentLength,
            HttpResponseStatus responseStatus,
            InternalEndpointUrlConfig internalEndpointUrlConfig
    ) {
        this.messageHandler = messageHandler;
        this.executorGroup = executorGroup;
        this.maxContentLength = maxContentLength;
        this.responseStatus = responseStatus;
        this.internalEndpointUrlConfig = internalEndpointUrlConfig;
    }

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        if (sslHandlerProvider != null) {
            SslHandler sslHandler = sslHandlerProvider.getSslHandler(socketChannel.alloc());
            pipeline.addLast(sslHandler);
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpContentDecompressor());
        pipeline.addLast(new LoggingHttpObjectAggregator(maxContentLength));
        pipeline
                .addLast(
                        new HttpServerHandler(
                                messageHandler.copy(),
                                executorGroup,
                                responseStatus,
                                internalEndpointUrlConfig
                        )
                );
    }

    public void enableSSL(SslHandlerProvider sslHandlerProvider) {
        this.sslHandlerProvider = sslHandlerProvider;
    }
}
