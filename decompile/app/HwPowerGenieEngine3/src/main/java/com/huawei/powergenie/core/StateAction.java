package com.huawei.powergenie.core;

import android.content.Intent;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class StateAction extends PowerAction {
    private static StateAction sPool;
    private static int sPoolSize = 0;
    private static final Object sPoolSync = new Object();
    private ArrayList<Integer> mEventIds = new ArrayList();
    boolean mInPool = false;
    private Intent mIntent;
    private String mPackageName;
    private Set<String> mPkgName = new HashSet();
    private String mStateName;
    private int mType = 0;
    private ArrayList<String> mValue = new ArrayList();
    StateAction next;

    protected StateAction() {
        super(0, 0);
    }

    public void resetAs(int actionId, int type, String name) {
        super.reset(actionId, System.currentTimeMillis());
        this.mType = type;
        this.mStateName = name;
        this.mIntent = null;
        this.mPackageName = null;
    }

    public void resetAs(int actionId, String name, int type, long timestamp, Intent intent) {
        super.reset(actionId, timestamp);
        this.mType = type;
        this.mStateName = name;
        this.mIntent = intent;
        this.mPackageName = null;
    }

    public static StateAction obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
                StateAction m = sPool;
                sPool = m.next;
                m.next = null;
                m.mInPool = false;
                sPoolSize--;
                return m;
            }
            Log.i("StateAction", "new StateAction");
            return new StateAction();
        }
    }

    public void recycle() {
        if (this.mInPool) {
            Log.e("StateAction", "This StateAction cannot be recycled because it is still in sPool.");
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
        return 1;
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    public void updatePkgName(String name) {
        this.mPackageName = name;
    }

    public String getPkgName() {
        return this.mPackageName;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(" name=").append(this.mStateName);
        builder.append(" pkg=").append(this.mPackageName);
        return builder.toString();
    }
}
