package com.huawei.rcs.util;

import com.android.rcs.RcsCommonConfig;
import java.util.HashSet;

public class RcsSelectRecorder {
    private SelectChangeExtListener mListener;
    private HashSet<Integer> mSelectedMsgPositions = new HashSet();
    private Object syncObject;

    public interface SelectChangeExtListener {
        void onItemChangedPosition(int i);
    }

    public RcsSelectRecorder(Object obj) {
        this.syncObject = obj;
    }

    public void setSelectChangeExtListener(SelectChangeExtListener l) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mListener = l;
        }
    }

    private final void noticeChangePosition(int position) {
        if (this.mListener != null && RcsCommonConfig.isRCSSwitchOn()) {
            this.mListener.onItemChangedPosition(position);
        }
    }

    public boolean addPosition(int position) {
        boolean ret = false;
        if (RcsCommonConfig.isRCSSwitchOn()) {
            synchronized (this.syncObject) {
                ret = this.mSelectedMsgPositions.add(Integer.valueOf(position));
            }
            noticeChangePosition(position);
        }
        return ret;
    }

    public boolean removePosition(int position) {
        boolean ret = false;
        if (RcsCommonConfig.isRCSSwitchOn()) {
            synchronized (this.syncObject) {
                ret = this.mSelectedMsgPositions.remove(Integer.valueOf(position));
            }
            noticeChangePosition(position);
        }
        return ret;
    }

    public void clearPosition() {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            synchronized (this.syncObject) {
                this.mSelectedMsgPositions.clear();
            }
            noticeChangePosition(-1);
        }
    }

    public void replacePosition(HashSet<Integer> newItems) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            synchronized (this.syncObject) {
                this.mSelectedMsgPositions = newItems;
            }
            noticeChangePosition(-1);
        }
    }

    public int positionSize() {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return 0;
        }
        int size;
        synchronized (this.syncObject) {
            size = this.mSelectedMsgPositions.size();
        }
        return size;
    }

    public Integer[] getAllSelectPositions() {
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return new Integer[0];
        }
        Integer[] numArr;
        synchronized (this.syncObject) {
            numArr = (Integer[]) this.mSelectedMsgPositions.toArray(new Integer[this.mSelectedMsgPositions.size()]);
        }
        return numArr;
    }
}
