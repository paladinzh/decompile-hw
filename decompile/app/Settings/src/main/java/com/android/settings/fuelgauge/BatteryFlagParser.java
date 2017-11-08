package com.android.settings.fuelgauge;

import android.os.BatteryStats.HistoryItem;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import com.android.settings.fuelgauge.BatteryActiveView.BatteryActiveProvider;
import com.android.settingslib.BatteryInfo.BatteryDataParser;

public class BatteryFlagParser implements BatteryDataParser, BatteryActiveProvider {
    private final int mAccentColor;
    private final SparseBooleanArray mData = new SparseBooleanArray();
    private final int mFlag;
    private boolean mLastSet;
    private long mLastTime;
    private long mLength;
    private final boolean mState2;

    public BatteryFlagParser(int accent, boolean state2, int flag) {
        this.mAccentColor = accent;
        this.mFlag = flag;
        this.mState2 = state2;
    }

    protected boolean isSet(HistoryItem record) {
        return ((this.mState2 ? record.states2 : record.states) & this.mFlag) != 0;
    }

    public void onParsingStarted(long startTime, long endTime) {
        this.mLength = endTime - startTime;
    }

    public void onDataPoint(long time, HistoryItem record) {
        boolean isSet = isSet(record);
        if (isSet != this.mLastSet) {
            this.mData.put((int) time, isSet);
            this.mLastSet = isSet;
        }
        this.mLastTime = time;
    }

    public void onDataGap() {
        if (this.mLastSet) {
            this.mData.put((int) this.mLastTime, false);
            this.mLastSet = false;
        }
    }

    public void onParsingDone() {
        if (this.mLastSet) {
            this.mData.put((int) this.mLastTime, false);
            this.mLastSet = false;
        }
    }

    public long getPeriod() {
        return this.mLength;
    }

    public boolean hasData() {
        return this.mData.size() > 1;
    }

    public SparseIntArray getColorArray() {
        SparseIntArray ret = new SparseIntArray();
        for (int i = 0; i < this.mData.size(); i++) {
            ret.put(this.mData.keyAt(i), getColor(this.mData.valueAt(i)));
        }
        return ret;
    }

    private int getColor(boolean b) {
        if (b) {
            return this.mAccentColor;
        }
        return 0;
    }
}
