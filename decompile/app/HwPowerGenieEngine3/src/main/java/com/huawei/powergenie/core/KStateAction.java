package com.huawei.powergenie.core;

import android.util.Log;
import java.util.ArrayList;

public final class KStateAction extends PowerAction {
    private static KStateAction sPool;
    private static int sPoolSize = 0;
    private static final Object sPoolSync = new Object();
    boolean mInPool = false;
    private int mPid = -1;
    private ArrayList<Integer> mUids = null;
    private String mValue;
    KStateAction next;

    protected KStateAction() {
        super(0, 0);
    }

    protected void resetAs(int actionId, int pid, ArrayList<Integer> uids, String value) {
        super.reset(actionId, System.currentTimeMillis());
        this.mPid = pid;
        this.mUids = uids;
        this.mValue = value;
    }

    public static KStateAction obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
                KStateAction m = sPool;
                sPool = m.next;
                m.next = null;
                m.mInPool = false;
                sPoolSize--;
                return m;
            }
            Log.i("KStateAction", "new KStateAction");
            return new KStateAction();
        }
    }

    public void recycle() {
        if (this.mInPool) {
            Log.e("KStateAction", "This KStateAction cannot be recycled because it is still in sPool.");
        }
        synchronized (sPoolSync) {
            if (sPoolSize < 10) {
                this.next = sPool;
                sPool = this;
                sPoolSize++;
                this.mInPool = true;
            }
        }
    }

    public int getType() {
        if (super.getType() >= 0) {
            return super.getType();
        }
        return 4;
    }

    public int getPid() {
        return this.mPid;
    }

    public ArrayList<Integer> getUid() {
        return this.mUids;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(" mPid =").append(this.mPid);
        builder.append(" mUids =").append(this.mUids);
        builder.append(" mValue =").append(this.mValue);
        return builder.toString();
    }
}
