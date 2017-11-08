package com.huawei.thermal.event;

import java.util.HashMap;

public final class SceneEvent extends Event {
    public static final HashMap<Integer, Integer> mSubActionMap = new HashMap<Integer, Integer>() {
        {
            put(Integer.valueOf(10015), Integer.valueOf(10016));
            put(Integer.valueOf(10005), Integer.valueOf(10006));
            put(Integer.valueOf(10018), Integer.valueOf(10019));
            put(Integer.valueOf(10007), Integer.valueOf(10017));
            put(Integer.valueOf(10020), Integer.valueOf(10021));
        }
    };
    private String mPkg;

    public SceneEvent(int evtID, String pkg) {
        super(evtID);
        this.mPkg = pkg;
    }

    public void resetAs(SceneEvent evt) {
        super.setEventId(evt.getEventId());
        this.mPkg = evt.getPkg();
    }

    public void setEventId(int evtId) {
        super.setEventId(evtId);
    }

    public void setPkg(String pkg) {
        this.mPkg = pkg;
    }

    public String getPkg() {
        return this.mPkg;
    }

    public int getSubFlag(int id) {
        if (mSubActionMap.containsKey(Integer.valueOf(id))) {
            return 1;
        }
        if (mSubActionMap.containsValue(Integer.valueOf(id))) {
            return 2;
        }
        return 3;
    }

    public int getType() {
        return 3;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(" EventID =").append(getEventId());
        builder.append(" Time =").append(getTimeStamp());
        builder.append(" Pkg =").append(this.mPkg);
        builder.append(" Flag =").append(getSubFlag(getEventId()));
        return builder.toString();
    }
}
