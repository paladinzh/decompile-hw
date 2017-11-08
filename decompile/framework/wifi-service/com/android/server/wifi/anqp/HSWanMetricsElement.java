package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;

public class HSWanMetricsElement extends ANQPElement {
    private final boolean mCapped;
    private final int mDlLoad;
    private final long mDlSpeed;
    private final int mLMD;
    private final LinkStatus mStatus;
    private final boolean mSymmetric;
    private final int mUlLoad;
    private final long mUlSpeed;

    public enum LinkStatus {
        Reserved,
        Up,
        Down,
        Test
    }

    public HSWanMetricsElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        boolean z = true;
        super(infoID);
        if (payload.remaining() != 13) {
            throw new ProtocolException("Bad WAN metrics length: " + payload.remaining());
        }
        boolean z2;
        int status = payload.get() & 255;
        this.mStatus = LinkStatus.values()[status & 3];
        if ((status & 4) != 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.mSymmetric = z2;
        if ((status & 8) == 0) {
            z = false;
        }
        this.mCapped = z;
        this.mDlSpeed = ((long) payload.getInt()) & Constants.INT_MASK;
        this.mUlSpeed = ((long) payload.getInt()) & Constants.INT_MASK;
        this.mDlLoad = payload.get() & 255;
        this.mUlLoad = payload.get() & 255;
        this.mLMD = payload.getShort() & Constants.SHORT_MASK;
    }

    public LinkStatus getStatus() {
        return this.mStatus;
    }

    public boolean isSymmetric() {
        return this.mSymmetric;
    }

    public boolean isCapped() {
        return this.mCapped;
    }

    public long getDlSpeed() {
        return this.mDlSpeed;
    }

    public long getUlSpeed() {
        return this.mUlSpeed;
    }

    public int getDlLoad() {
        return this.mDlLoad;
    }

    public int getUlLoad() {
        return this.mUlLoad;
    }

    public int getLMD() {
        return this.mLMD;
    }

    public String toString() {
        return String.format("HSWanMetrics{mStatus=%s, mSymmetric=%s, mCapped=%s, mDlSpeed=%d, mUlSpeed=%d, mDlLoad=%f, mUlLoad=%f, mLMD=%d}", new Object[]{this.mStatus, Boolean.valueOf(this.mSymmetric), Boolean.valueOf(this.mCapped), Long.valueOf(this.mDlSpeed), Long.valueOf(this.mUlSpeed), Double.valueOf((((double) this.mDlLoad) * 100.0d) / 256.0d), Double.valueOf((((double) this.mUlLoad) * 100.0d) / 256.0d), Integer.valueOf(this.mLMD)});
    }
}
