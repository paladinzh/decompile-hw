package com.huawei.powergenie.core;

import java.util.ArrayList;
import java.util.HashMap;

public class PowerAction {
    public static final HashMap<Integer, Integer> mSubActionMap = new HashMap<Integer, Integer>() {
        {
            put(Integer.valueOf(246), Integer.valueOf(247));
            put(Integer.valueOf(210), Integer.valueOf(211));
            put(Integer.valueOf(506), Integer.valueOf(507));
            put(Integer.valueOf(238), Integer.valueOf(239));
            put(Integer.valueOf(502), Integer.valueOf(503));
            put(Integer.valueOf(504), Integer.valueOf(505));
            put(Integer.valueOf(267), Integer.valueOf(268));
            put(Integer.valueOf(500), Integer.valueOf(501));
            put(Integer.valueOf(221), Integer.valueOf(244));
            put(Integer.valueOf(508), Integer.valueOf(509));
            put(Integer.valueOf(258), Integer.valueOf(259));
            put(Integer.valueOf(240), Integer.valueOf(241));
            put(Integer.valueOf(231), Integer.valueOf(232));
            put(Integer.valueOf(510), Integer.valueOf(511));
        }
    };
    private int mActionId;
    private boolean mExtraBool;
    private int mExtraInt;
    private HashMap<String, String> mExtraKeyValString = null;
    private ArrayList<Integer> mExtraListInteger = null;
    private ArrayList<String> mExtraListString = null;
    private long mExtraLong;
    private String mExtraStr;
    private int mFlag;
    private long mTimestamp;

    public PowerAction(int actionId, long ts) {
        this.mActionId = actionId;
        this.mTimestamp = ts;
        this.mFlag = 3;
    }

    protected void reset(int actionId, long ts) {
        this.mActionId = actionId;
        this.mTimestamp = ts;
        this.mExtraStr = null;
        this.mExtraInt = 0;
        this.mExtraLong = 0;
        this.mExtraBool = false;
        this.mExtraListString = null;
        this.mExtraListInteger = null;
        this.mExtraKeyValString = null;
        this.mFlag = 3;
    }

    public void recycle() {
    }

    public void updateFlag(int flag) {
        this.mFlag = flag;
    }

    public int getActionId() {
        return this.mActionId;
    }

    public int getType() {
        if (this.mActionId < 400 || this.mActionId > 407) {
            return -1;
        }
        return 0;
    }

    public int getSubFlag() {
        return this.mFlag;
    }

    public long getTimeStamp() {
        return this.mTimestamp;
    }

    public String getPkgName() {
        return "";
    }

    public void putExtra(String value) {
        this.mExtraStr = value;
    }

    public void putExtra(int value) {
        this.mExtraInt = value;
    }

    public void putExtra(long value) {
        this.mExtraLong = value;
    }

    public void putExtra(boolean value) {
        this.mExtraBool = value;
    }

    public void putExtra(ArrayList<String> value) {
        this.mExtraListString = value;
    }

    public void putExtraListInteger(ArrayList<Integer> value) {
        this.mExtraListInteger = value;
    }

    public void putExtra(String key, String value) {
        if (this.mExtraKeyValString == null) {
            this.mExtraKeyValString = new HashMap();
        }
        this.mExtraKeyValString.put(key, value);
    }

    public String getExtraString() {
        return this.mExtraStr;
    }

    public int getExtraInt() {
        return this.mExtraInt;
    }

    public long getExtraLong() {
        return this.mExtraLong;
    }

    public boolean getExtraBoolean() {
        return this.mExtraBool;
    }

    public ArrayList<String> getExtraListString() {
        return this.mExtraListString;
    }

    public ArrayList<Integer> getExtraListInteger() {
        return this.mExtraListInteger;
    }

    public String getExtraValString(String key) {
        if (this.mExtraKeyValString != null) {
            return (String) this.mExtraKeyValString.get(key);
        }
        return null;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(" action id=").append(this.mActionId);
        builder.append(" time=").append(this.mTimestamp);
        builder.append(" Flag=").append(this.mFlag);
        return builder.toString();
    }
}
