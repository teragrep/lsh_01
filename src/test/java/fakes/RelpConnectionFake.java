package fakes;

import com.teragrep.lsh_01.config.RelpConfig;
import com.teragrep.lsh_01.pool.IRelpConnection;
import com.teragrep.rlp_01.RelpBatch;

public final class RelpConnectionFake implements IRelpConnection {

    private final RelpConfig relpConfig;
    private final int sendLatency;
    private final int connectLatency;

    public RelpConnectionFake(RelpConfig relpConfig) {
        this(relpConfig, 0, 0);
    }

    public RelpConnectionFake(RelpConfig relpConfig, int sendLatency, int connectLatency) {
        this.relpConfig = relpConfig;
        this.sendLatency = sendLatency;
        this.connectLatency = connectLatency;
    }

    @Override
    public int getReadTimeout() {
        return 0;
    }

    @Override
    public void setReadTimeout(int readTimeout) {
        // no-op in fake
    }

    @Override
    public int getWriteTimeout() {
        return 0;
    }

    @Override
    public void setWriteTimeout(int writeTimeout) {
        // no-op in fake
    }

    @Override
    public int getConnectionTimeout() {
        return 0;
    }

    @Override
    public void setConnectionTimeout(int timeout) {
        // no-op in fake
    }

    @Override
    public void setKeepAlive(boolean on) {
        // no-op in fake
    }

    @Override
    public int getRxBufferSize() {
        return 0;
    }

    @Override
    public void setRxBufferSize(int size) {
        // no-op in fake
    }

    @Override
    public int getTxBufferSize() {
        return 0;
    }

    @Override
    public void setTxBufferSize(int size) {
        // no-op in fake
    }

    @Override
    public boolean connect(String hostname, int port) {
        try {
            Thread.sleep(connectLatency);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public void tearDown() {
        // no-op in fake
    }

    @Override
    public boolean disconnect() {
        return true;
    }

    @Override
    public void commit(RelpBatch relpBatch) {
        try {
            Thread.sleep(sendLatency);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // remove all the requests from relpBatch in the fake
        // so that the batch will return true in verifyTransactionAll()
        while (relpBatch.getWorkQueueLength() > 0) {
            long reqId = relpBatch.popWorkQueue();
            relpBatch.removeRequest(reqId);
        }
    }

    @Override
    public RelpConfig relpConfig() {
        return relpConfig;
    }
}
