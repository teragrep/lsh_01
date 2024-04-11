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
import com.teragrep.lsh_01.config.NettyConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpResponseStatus;
import com.teragrep.lsh_01.util.CustomRejectedExecutionHandler;
import com.teragrep.lsh_01.util.SslHandlerProvider;

import java.io.Closeable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.teragrep.lsh_01.util.DaemonThreadFactory.daemonThreadFactory;

/**
 * Created by joaoduarte on 11/10/2017.
 */
public class NettyHttpServer implements Runnable, Closeable {

    private final ServerBootstrap serverBootstrap;
    private final String host;
    private final int port;
    private final int connectionBacklog = 128;

    private final EventLoopGroup processorGroup;
    private final ThreadPoolExecutor executorGroup;
    private final HttpResponseStatus responseStatus;
    private final InternalEndpointUrlConfig internalEndpointUrlConfig;

    public NettyHttpServer(
            NettyConfig nettyConfig,
            IMessageHandler messageHandler,
            SslHandlerProvider sslHandlerProvider,
            int responseCode,
            InternalEndpointUrlConfig internalEndpointUrlConfig
    ) {
        this.host = nettyConfig.listenAddress;
        this.port = nettyConfig.listenPort;
        this.internalEndpointUrlConfig = internalEndpointUrlConfig;
        this.responseStatus = HttpResponseStatus.valueOf(responseCode);
        processorGroup = new NioEventLoopGroup(nettyConfig.threads, daemonThreadFactory("http-input-processor"));

        executorGroup = new ThreadPoolExecutor(
                nettyConfig.threads,
                nettyConfig.threads,
                0,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(nettyConfig.maxPendingRequests),
                daemonThreadFactory("http-input-handler-executor"),
                new CustomRejectedExecutionHandler()
        );

        final HttpInitializer httpInitializer = new HttpInitializer(
                messageHandler,
                executorGroup,
                nettyConfig.maxContentLength,
                responseStatus,
                internalEndpointUrlConfig
        );

        if (sslHandlerProvider != null) {
            httpInitializer.enableSSL(sslHandlerProvider);
        }

        serverBootstrap = new ServerBootstrap()
                .group(processorGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, connectionBacklog)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(httpInitializer);
    }

    @Override
    public void run() {
        try {
            executorGroup.prestartAllCoreThreads();
            final ChannelFuture channel = serverBootstrap.bind(host, port);
            channel.sync().channel().closeFuture().sync();
        }
        catch (final InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void close() {
        try {
            // stop accepting new connections first
            processorGroup.shutdownGracefully(0, 10, TimeUnit.SECONDS).sync();
            // then shutdown the message handler executor
            executorGroup.shutdown();
            try {
                if (!executorGroup.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorGroup.shutdownNow();
                }
            }
            catch (InterruptedException e) {
                throw new IllegalStateException("Arrived at illegal state during thread pool shutdown {}", e);
            }
            executorGroup.shutdownNow();
        }
        catch (final InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
