package com.huawei.mms.util;

import com.android.rcs.RcsCommonConfig;
import com.huawei.cust.HwCustUtils;
import com.huawei.rcs.util.RcsSelectRecorder;
import java.util.HashSet;

public class SelectRecorder {
    public static final Long[] EMPTY_RECORD = new Long[0];
    private HwCustSelectRecorder mHwCust = ((HwCustSelectRecorder) HwCustUtils.createObj(HwCustSelectRecorder.class, new Object[]{this.syncObject}));
    private SelectChangeListener mListener;
    private RcsSelectRecorder mRcsSelectRecorder;
    private HashSet<Long> mSelectedMsgItems = new HashSet();
    private Object syncObject = new Object();

    public interface SelectChangeListener {
        void onItemChanged(long j);
    }

    public SelectRecorder() {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mRcsSelectRecorder = new RcsSelectRecorder(this.syncObject);
        }
    }

    public RcsSelectRecorder getRcsSelectRecorder() {
        if (this.mRcsSelectRecorder == null) {
            this.mRcsSelectRecorder = new RcsSelectRecorder(this.syncObject);
        }
        return this.mRcsSelectRecorder;
    }

    public boolean contains(long itemId) {
        boolean contains;
        synchronized (this.syncObject) {
            contains = this.mSelectedMsgItems.contains(Long.valueOf(itemId));
        }
        return contains;
    }

    public void setChangeListener(SelectChangeListener l) {
        this.mListener = l;
    }

    private final void noticeChange(long id) {
        if (this.mListener != null) {
            this.mListener.onItemChanged(id);
        }
    }

    public boolean add(long itemId) {
        boolean ret;
        synchronized (this.syncObject) {
            ret = this.mSelectedMsgItems.add(Long.valueOf(itemId));
        }
        noticeChange(itemId);
        return ret;
    }

    public boolean remove(long itemId) {
        boolean ret;
        synchronized (this.syncObject) {
            ret = this.mSelectedMsgItems.remove(Long.valueOf(itemId));
        }
        noticeChange(itemId);
        return ret;
    }

    public void clear() {
        synchronized (this.syncObject) {
            this.mSelectedMsgItems.clear();
        }
        noticeChange(-1);
    }

    public void replace(HashSet<Long> newItems) {
        synchronized (this.syncObject) {
            this.mSelectedMsgItems = newItems;
        }
        noticeChange(-1);
    }

    public int size() {
        int size;
        synchronized (this.syncObject) {
            size = this.mSelectedMsgItems.size();
        }
        return size;
    }

    public Long[] getAllSelectItems() {
        Long[] lArr;
        synchronized (this.syncObject) {
            lArr = (Long[]) this.mSelectedMsgItems.toArray(new Long[this.mSelectedMsgItems.size()]);
        }
        return lArr;
    }
}
