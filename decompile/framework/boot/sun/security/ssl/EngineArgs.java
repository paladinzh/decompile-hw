package sun.security.ssl;

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

class EngineArgs {
    static final /* synthetic */ boolean -assertionsDisabled = (!EngineArgs.class.desiredAssertionStatus());
    ByteBuffer[] appData;
    private int[] appLims;
    private int[] appPoss;
    private int appRemaining = 0;
    private int len;
    ByteBuffer netData;
    private int netLim;
    private int netPos;
    private int offset;
    private boolean wrapMethod = false;

    EngineArgs(ByteBuffer[] appData, int offset, int len, ByteBuffer netData) {
        init(netData, appData, offset, len);
    }

    EngineArgs(ByteBuffer netData, ByteBuffer[] appData, int offset, int len) {
        init(netData, appData, offset, len);
    }

    private void init(ByteBuffer netData, ByteBuffer[] appData, int offset, int len) {
        if (netData == null || appData == null) {
            throw new IllegalArgumentException("src/dst is null");
        } else if (offset < 0 || len < 0 || offset > appData.length - len) {
            throw new IndexOutOfBoundsException();
        } else if (this.wrapMethod && netData.isReadOnly()) {
            throw new ReadOnlyBufferException();
        } else {
            this.netPos = netData.position();
            this.netLim = netData.limit();
            this.appPoss = new int[appData.length];
            this.appLims = new int[appData.length];
            int i = offset;
            while (i < offset + len) {
                if (appData[i] == null) {
                    throw new IllegalArgumentException("appData[" + i + "] == null");
                } else if (this.wrapMethod || !appData[i].isReadOnly()) {
                    this.appRemaining += appData[i].remaining();
                    this.appPoss[i] = appData[i].position();
                    this.appLims[i] = appData[i].limit();
                    i++;
                } else {
                    throw new ReadOnlyBufferException();
                }
            }
            this.netData = netData;
            this.appData = appData;
            this.offset = offset;
            this.len = len;
        }
    }

    void gather(int spaceLeft) {
        for (int i = this.offset; i < this.offset + this.len && spaceLeft > 0; i++) {
            int amount = Math.min(this.appData[i].remaining(), spaceLeft);
            this.appData[i].limit(this.appData[i].position() + amount);
            this.netData.put(this.appData[i]);
            this.appRemaining -= amount;
            spaceLeft -= amount;
        }
    }

    void scatter(ByteBuffer readyData) {
        Object obj = null;
        int amountLeft = readyData.remaining();
        for (int i = this.offset; i < this.offset + this.len && amountLeft > 0; i++) {
            int amount = Math.min(this.appData[i].remaining(), amountLeft);
            readyData.limit(readyData.position() + amount);
            this.appData[i].put(readyData);
            amountLeft -= amount;
        }
        if (!-assertionsDisabled) {
            if (readyData.remaining() == 0) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
    }

    int getAppRemaining() {
        return this.appRemaining;
    }

    int deltaNet() {
        return this.netData.position() - this.netPos;
    }

    int deltaApp() {
        int sum = 0;
        for (int i = this.offset; i < this.offset + this.len; i++) {
            sum += this.appData[i].position() - this.appPoss[i];
        }
        return sum;
    }

    void resetPos() {
        this.netData.position(this.netPos);
        for (int i = this.offset; i < this.offset + this.len; i++) {
            this.appData[i].position(this.appPoss[i]);
        }
    }

    void resetLim() {
        this.netData.limit(this.netLim);
        for (int i = this.offset; i < this.offset + this.len; i++) {
            this.appData[i].limit(this.appLims[i]);
        }
    }
}
