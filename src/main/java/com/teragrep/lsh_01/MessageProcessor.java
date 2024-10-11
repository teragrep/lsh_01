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

import com.teragrep.lsh_01.authentication.*;
import com.teragrep.lsh_01.config.InternalEndpointUrlConfig;
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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MessageProcessor implements RejectableRunnable {

    private final ChannelHandlerContext ctx;
    private final FullHttpRequest req;
    private final IMessageHandler messageHandler;
    private final HttpResponseStatus responseStatus;
    private final InternalEndpointUrlConfig internalEndpointUrlConfig;

    private static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;
    private final static Logger LOGGER = LogManager.getLogger(MessageProcessor.class);

    MessageProcessor(
            ChannelHandlerContext ctx,
            FullHttpRequest req,
            IMessageHandler messageHandler,
            HttpResponseStatus responseStatus,
            InternalEndpointUrlConfig internalEndpointUrlConfig
    ) {
        this.ctx = ctx;
        this.req = req;
        this.messageHandler = messageHandler;
        this.responseStatus = responseStatus;
        this.internalEndpointUrlConfig = internalEndpointUrlConfig;
    }

    public void onRejection() {
        try {
            final FullHttpResponse response = generateFailedResponse(HttpResponseStatus.TOO_MANY_REQUESTS);
            LOGGER.warn("Too many requests, returning code <{}>", response.status().code());
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
            if (isInternalEndpoint()) {
                LOGGER.debug("Healthcheck endpoint called");
                response = generateResponse(messageHandler.responseHeaders());
            }
            else {
                if (messageHandler.requiresToken()) {
                    if (req.headers().contains(HttpHeaderNames.AUTHORIZATION)) {
                        HttpResponse response1;
                        try {
                            Subject subject = messageHandler
                                    .asSubject(req.headers().get(HttpHeaderNames.AUTHORIZATION));
                            // Headers are inserted to structured data so remove it to prevent credentials leaking
                            req.headers().remove(HttpHeaderNames.AUTHORIZATION);
                            if (subject.isStub()) {
                                LOGGER.debug("Authentication failed; rejecting request.");
                                response1 = generateFailedResponse(HttpResponseStatus.UNAUTHORIZED);
                            }
                            else {
                                LOGGER.debug("Processing message");
                                response1 = processMessage(subject);
                            }
                        }
                        catch (Exception e) {
                            LOGGER.debug("Invalid authorization; rejecting request.");
                            response1 = generateFailedResponse(HttpResponseStatus.BAD_REQUEST);
                        }
                        response = response1;
                    }
                    else {
                        LOGGER.debug("Required authorization not provided; requesting authentication.");
                        response = generateAuthenticationRequestResponse();
                    }
                }
                else {
                    Subject subject = new SubjectAnonymous();
                    response = processMessage(subject);
                }
            }
            ctx.writeAndFlush(response);
        }
        finally {
            req.release();
        }
    }

    private boolean isInternalEndpoint() {
        return internalEndpointUrlConfig.healthcheckEnabled
                && internalEndpointUrlConfig.healthcheckUrl.equals(req.uri());
    }

    private FullHttpResponse processMessage(Subject subject) {
        final Map<String, String> formattedHeaders = formatHeaders(req.headers());
        final String body = req.content().toString(UTF8_CHARSET);
        if (messageHandler.onNewMessage(subject, formattedHeaders, body)) {
            return generateResponse(messageHandler.responseHeaders());
        }
        else {
            FullHttpResponse response = generateFailedResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            LOGGER.warn("Processing failed, returning code <{}>", response.status().code());
            return response;
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
        for (Map.Entry<String, String> entry : stringHeaders.entrySet()) {
            headers.set(entry.getKey(), entry.getValue());
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
