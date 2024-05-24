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

import com.teragrep.lsh_01.config.RelpConfig;
import com.teragrep.rlp_03.channel.socket.PlainFactory;
import com.teragrep.rlp_03.eventloop.EventLoop;
import com.teragrep.rlp_03.eventloop.EventLoopFactory;
import com.teragrep.rlp_03.frame.delegate.DefaultFrameDelegate;
import com.teragrep.rlp_03.frame.delegate.FrameContext;
import com.teragrep.rlp_03.frame.delegate.FrameDelegate;
import com.teragrep.rlp_03.server.ServerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RelpServer {

    private EventLoop eventLoop;
    private Thread eventLoopThread;
    private final int listenPort;
    private final ExecutorService executorService;
    private final Consumer<FrameContext> syslogConsumer;
    private final List<CountingFrameDelegate> frameDelegates;

    private final List<String> payloads;

    public RelpServer() {
        RelpConfig relpConfig = new RelpConfig();
        this.listenPort = relpConfig.relpPort;
        this.payloads = new ArrayList<>();
        this.frameDelegates = new ArrayList<>();

        int threads = 1;
        this.executorService = Executors.newFixedThreadPool(threads);

        this.syslogConsumer = new Consumer<>() {

            // NOTE: synchronized because frameDelegateSupplier returns this instance for all the parallel connections
            @Override
            public synchronized void accept(FrameContext frameContext) {
                payloads.add(frameContext.relpFrame().payload().toString());
            }
        };
    }

    /**
     * Set up the server before running end-to-end tests. Uses DefaultFrameDelegate.
     */
    public void setUpDefault() {
        setUp(() -> new DefaultFrameDelegate(this.syslogConsumer));
    }

    /**
     * Set up the server before running end-to-end tests. Uses CountingFrameDelegate.
     */
    public void setUpCounting() {
        setUp(this::addCountingFrameDelegate);
    }

    /**
     * Set up the server before running end-to-end tests.
     *
     * @param frameDelegateSupplier custom FrameDelegateSupplier
     */
    private void setUp(Supplier<FrameDelegate> frameDelegateSupplier) {
        /*
         * EventLoop is used to notice any events from the connections
         */
        EventLoopFactory eventLoopFactory = new EventLoopFactory();
        try {
            eventLoop = eventLoopFactory.create();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        eventLoopThread = new Thread(eventLoop);
        /*
         * eventLoopThread must run, otherwise nothing will be processed
         */
        eventLoopThread.start();

        /*
         * ServerFactory is used to create server instances
         */
        ServerFactory serverFactory = new ServerFactory(
                eventLoop,
                executorService,
                new PlainFactory(),
                frameDelegateSupplier
        );

        try {
            serverFactory.create(listenPort);
        }
        catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        }
    }

    /**
     * Close the server after tests.
     */
    public void tearDown() {
        /*
         * Stop eventLoop
         */
        eventLoop.stop();

        /*
         * Wait for stop to complete
         */
        try {
            eventLoopThread.join();
        }
        catch (InterruptedException interruptedException) {
            throw new RuntimeException(interruptedException);
        }

        executorService.shutdown();
    }

    /**
     * Clear payloads in between tests
     */
    public void clear() {
        payloads.clear();
        frameDelegates.clear();
    }

    public List<String> payloads() {
        return payloads;
    }

    public List<CountingFrameDelegate> frameDelegates() {
        return frameDelegates;
    }

    private FrameDelegate addCountingFrameDelegate() {
        CountingFrameDelegate frameDelegate = new CountingFrameDelegate();
        frameDelegates.add(frameDelegate);
        return frameDelegate;
    }
}
