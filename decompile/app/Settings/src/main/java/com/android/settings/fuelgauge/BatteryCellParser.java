package com.android.settings.fuelgauge;

import android.os.BatteryStats.HistoryItem;
import android.util.SparseIntArray;
import com.android.settings.Utils;
import com.android.settings.fuelgauge.BatteryActiveView.BatteryActiveProvider;
import com.android.settingslib.BatteryInfo.BatteryDataParser;

public class BatteryCellParser implements BatteryDataParser, BatteryActiveProvider {
    private final SparseIntArray mData = new SparseIntArray();
    private long mLastTime;
    private int mLastValue;
    private long mLength;

    protected int getValue(HistoryItem rec) {
        if (((rec.states & 448) >> 6) == 3) {
            return 0;
        }
        if ((rec.states & 2097152) != 0) {
            return 1;
        }
        return ((rec.states & 56) >> 3) + 2;
    }

    public void onParsingStarted(long startTime, long endTime) {
        this.mLength = endTime - startTime;
    }

    public void onDataPoint(long time, HistoryItem record) {
        int value = getValue(record);
        if (value != this.mLastValue) {
            this.mData.put((int) time, value);
            this.mLastValue = value;
        }
        this.mLastTime = time;
    }

    public void onDataGap() {
        if (this.mLastValue != 0) {
            this.mData.put((int) this.mLastTime, 0);
            this.mLastValue = 0;
        }
    }

    public void onParsingDone() {
        if (this.mLastValue != 0) {
            this.mData.put((int) this.mLastTime, 0);
            this.mLastValue = 0;
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

    private int getColor(int i) {
        return Utils.BADNESS_COLORS[i];
    }
}
