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
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.compression.DecompressionException;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadPoolExecutor;

import static io.netty.buffer.Unpooled.copiedBuffer;

/**
 * Created by joaoduarte on 11/10/2017.
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final IMessageHandler messageHandler;
    private final ThreadPoolExecutor executorGroup;
    private final HttpResponseStatus responseStatus;
    private final InternalEndpointUrlConfig internalEndpointUrlConfig;

    public HttpServerHandler(
            IMessageHandler messageHandler,
            ThreadPoolExecutor executorGroup,
            HttpResponseStatus responseStatus,
            InternalEndpointUrlConfig internalEndpointUrlConfig
    ) {
        this.messageHandler = messageHandler;
        this.executorGroup = executorGroup;
        this.responseStatus = responseStatus;
        this.internalEndpointUrlConfig = internalEndpointUrlConfig;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        final String remoteAddress = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        msg.retain();
        final MessageProcessor messageProcessor = new MessageProcessor(
                ctx,
                msg,
                remoteAddress,
                messageHandler,
                responseStatus,
                internalEndpointUrlConfig
        );
        executorGroup.execute(messageProcessor);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final ByteBuf content = copiedBuffer(cause.getMessage().getBytes());
        final HttpResponseStatus responseStatus;

        if (cause instanceof DecompressionException) {
            responseStatus = HttpResponseStatus.BAD_REQUEST;
        }
        else {
            responseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        }
        final DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                responseStatus,
                content
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        ctx.writeAndFlush(response);
    }
}
