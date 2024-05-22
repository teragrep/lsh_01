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

import com.teragrep.rlp_03.frame.delegate.DefaultFrameDelegate;
import com.teragrep.rlp_03.frame.delegate.FrameContext;
import com.teragrep.rlp_03.frame.delegate.FrameDelegate;

import java.util.function.Consumer;

/**
 * FrameDelegate that counts the number of incoming messages (for the connection). Useful for rebind testing.
 */
public class CountingFrameDelegate implements FrameDelegate {

    private final DefaultFrameDelegate frameDelegate;
    private int recordsReceived = 0;

    public CountingFrameDelegate() {
        Consumer<FrameContext> countingConsumer = new Consumer<>() {

            // NOTE: synchronized because frameDelegateSupplier returns this instance for all the parallel connections
            @Override
            public synchronized void accept(FrameContext frameContext) {
                recordsReceived++;
            }
        };

        this.frameDelegate = new DefaultFrameDelegate(countingConsumer);
    }

    public int recordsReceived() {
        return recordsReceived;
    }

    @Override
    public boolean accept(FrameContext frameContext) {
        return frameDelegate.accept(frameContext);
    }

    @Override
    public void close() throws Exception {
        frameDelegate.close();
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
