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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.teragrep.lsh_01.util.RejectableRunnable;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class MessageProcessor implements RejectableRunnable {

    private final ChannelHandlerContext ctx;
    private final FullHttpRequest req;
    private final String remoteAddress;
    private final IMessageHandler messageHandler;
    private final HttpResponseStatus responseStatus;

    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    private final static Logger LOGGER = LogManager.getLogger(MessageHandler.class);

    MessageProcessor(
            ChannelHandlerContext ctx,
            FullHttpRequest req,
            String remoteAddress,
            IMessageHandler messageHandler,
            HttpResponseStatus responseStatus
    ) {
        this.ctx = ctx;
        this.req = req;
        this.remoteAddress = remoteAddress;
        this.messageHandler = messageHandler;
        this.responseStatus = responseStatus;
    }

    public void onRejection() {
        try {
            final FullHttpResponse response = generateFailedResponse(HttpResponseStatus.TOO_MANY_REQUESTS);
            ctx.writeAndFlush(response);
        }
        finally {
            req.release();
        }
    }

    @Override
    public void run() {
        try {
            final HttpResponse response;
            if (messageHandler.requiresToken() && !req.headers().contains(HttpHeaderNames.AUTHORIZATION)) {
                LOGGER.debug("Required authorization not provided; requesting authentication.");
                response = generateAuthenticationRequestResponse();
            }
            else {
                final String token = req.headers().get(HttpHeaderNames.AUTHORIZATION);
                req.headers().remove(HttpHeaderNames.AUTHORIZATION);
                if (messageHandler.validatesToken(token)) {
                    LOGGER.debug("Valid authorization; processing request.");
                    response = processMessage();
                }
                else {
                    LOGGER.debug("Invalid authorization; rejecting request.");
                    response = generateFailedResponse(HttpResponseStatus.UNAUTHORIZED);
                }
            }
            ctx.writeAndFlush(response);
        }
        finally {
            req.release();
        }
    }

    private FullHttpResponse processMessage() {
        final Map<String, String> formattedHeaders = formatHeaders(req.headers());
        final String body = req.content().toString(UTF8_CHARSET);
        if (messageHandler.onNewMessage(remoteAddress, formattedHeaders, body)) {
            return generateResponse(messageHandler.responseHeaders());
        }
        else {
            return generateFailedResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private FullHttpResponse generateFailedResponse(HttpResponseStatus status) {
        final FullHttpResponse response = new DefaultFullHttpResponse(req.protocolVersion(), status);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        return response;
    }

    private FullHttpResponse generateAuthenticationRequestResponse() {
        final FullHttpResponse response = new DefaultFullHttpResponse(
                req.protocolVersion(),
                HttpResponseStatus.UNAUTHORIZED
        );
        response.headers().set(HttpHeaderNames.WWW_AUTHENTICATE, "Basic realm=\"Logstash HTTP Input\"");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        return response;
    }

    private FullHttpResponse generateResponse(Map<String, String> stringHeaders) {

        final FullHttpResponse response = new DefaultFullHttpResponse(req.protocolVersion(), responseStatus);
        final DefaultHttpHeaders headers = new DefaultHttpHeaders();
        for (String key : stringHeaders.keySet()) {
            headers.set(key, stringHeaders.get(key));
        }
        response.headers().set(headers);

        if (responseStatus != HttpResponseStatus.NO_CONTENT) {
            final ByteBuf payload = Unpooled.wrappedBuffer("ok".getBytes(UTF8_CHARSET));
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, payload.readableBytes());
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            response.content().writeBytes(payload);
        }

        return response;
    }

    private Map<String, String> formatHeaders(HttpHeaders headers) {
        final HashMap<String, String> formattedHeaders = new HashMap<>();
        for (Map.Entry<String, String> header : headers) {
            String key = header.getKey();
            key = key.toLowerCase();
            key = key.replace('-', '_');
            formattedHeaders.put(key, header.getValue());
        }
        formattedHeaders.put("http_accept", formattedHeaders.remove("accept"));
        formattedHeaders.put("http_host", formattedHeaders.remove("host"));
        formattedHeaders.put("http_user_agent", formattedHeaders.remove("user_agent"));
        formattedHeaders.put("request_method", req.method().name());
        formattedHeaders.put("request_path", req.uri());
        formattedHeaders.put("http_version", req.protocolVersion().text());
        return formattedHeaders;
    }
}
