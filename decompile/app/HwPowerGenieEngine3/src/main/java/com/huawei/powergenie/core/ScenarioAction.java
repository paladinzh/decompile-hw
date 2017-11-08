package com.huawei.powergenie.core;

import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class ScenarioAction extends PowerAction {
    private static ScenarioAction sPool;
    private static int sPoolSize = 0;
    private static final Object sPoolSync = new Object();
    private ArrayList<Integer> mEventIds;
    boolean mInPool;
    private String mPackageName;
    private Set<String> mPkgName;
    private String mStateName;
    private int mType;
    private ArrayList<String> mValue;
    ScenarioAction next;

    public ScenarioAction(int actionId, String name, int type, long timestamp, String pkg) {
        super(actionId, timestamp);
        this.mType = 0;
        this.mEventIds = new ArrayList();
        this.mPkgName = new HashSet();
        this.mValue = new ArrayList();
        this.mInPool = false;
        this.mType = type;
        this.mStateName = name;
        this.mPackageName = pkg;
    }

    public ScenarioAction(int actionId, String name) {
        this(actionId, name, 0, 0, "");
    }

    public void resetAs(int actionId, String name, int type, long timestamp, String pkg) {
        super.reset(actionId, timestamp);
        this.mType = type;
        this.mStateName = name;
        this.mPackageName = pkg;
    }

    public static ScenarioAction obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
                ScenarioAction m = sPool;
                sPool = m.next;
                m.next = null;
                m.mInPool = false;
                sPoolSize--;
                return m;
            }
            Log.i("ScenarioAction", "new ScenarioAction sPoolSize: " + sPoolSize);
            return new ScenarioAction(0, "");
        }
    }

    public void recycle() {
        if (this.mInPool) {
            Log.e("ScenarioAction", "This ScenarioAction cannot be recycled because it is still in sPool.");
        }
        synchronized (sPoolSync) {
            if (sPoolSize < 20) {
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
        return 5;
    }

    public int getStateType() {
        return this.mType;
    }

    public String getPkgName() {
        return this.mPackageName;
    }

    public String getStateName() {
        return this.mStateName;
    }

    public boolean addEventId(int eventId) {
        if (eventId < 0) {
            return false;
        }
        this.mEventIds.add(Integer.valueOf(eventId));
        return true;
    }

    public ArrayList<Integer> getEventIds() {
        return this.mEventIds;
    }

    public boolean addPkgName(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        this.mPkgName.add(name);
        return true;
    }

    public boolean matchPkgName(String pkgName) {
        boolean z = false;
        if (this.mPkgName.size() == 0) {
            return true;
        }
        if (pkgName != null) {
            z = this.mPkgName.contains(pkgName);
        }
        return z;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(" name=").append(this.mStateName);
        builder.append(" pkg=").append(this.mPackageName);
        return builder.toString();
    }
}
