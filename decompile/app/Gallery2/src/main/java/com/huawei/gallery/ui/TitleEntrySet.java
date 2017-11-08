package com.huawei.gallery.ui;

import android.graphics.RectF;
import com.huawei.gallery.data.AddressRegionLoader.AddressRegionListener;
import com.huawei.gallery.data.AddressStringLoader.AddressStringListener;
import com.huawei.gallery.ui.TimeAxisLabel.TitleArgs;

public class TitleEntrySet extends EntrySet {
    private final AddressRegionListener mAddressListener;
    private final AddressStringListener mAddressStringListener;
    private final TitleEntrySetListener mListener;
    private final TitleLoaderListener mLoaderListener;

    public TitleEntrySet(TitleEntrySetListener listener, TitleLoaderListener loaderListener, AddressRegionListener addressListener, AddressStringListener addressStringListener, int cacheSize, int size) {
        super(listener, cacheSize, size);
        this.mListener = listener;
        this.mLoaderListener = loaderListener;
        this.mAddressListener = addressListener;
        this.mAddressStringListener = addressStringListener;
    }

    protected BaseEntry getEntry(int index) {
        TitleArgs titleArgs = new TitleArgs();
        titleArgs.groupData = this.mListener.getGroupData(index);
        titleArgs.index = index;
        return new TitleEntry(this.mListener, this.mLoaderListener, this.mAddressListener, this.mAddressStringListener, titleArgs);
    }

    protected Object getObjectIndex(int index) {
        return this.mListener.getTitleObjectIndex(index);
    }

    protected boolean supportEntry(BaseEntry baseEntry) {
        return baseEntry instanceof TitleEntry;
    }

    protected boolean isBitmapTextureOpaque() {
        return false;
    }

    public void updateAddressRect(int index, RectF addressRect) {
        TitleEntry entry = this.mData[index % this.mData.length];
        if (entry != null) {
            entry.updateAddressRect(addressRect);
        }
    }

    public void updateAddressString(int index, String address) {
        TitleEntry entry = this.mData[index % this.mData.length];
        if (entry != null) {
            entry.updateAddressString(address);
        }
    }

    public void updateAddressFlag(int index) {
        TitleEntry entry = this.mData[index % this.mData.length];
        if (entry != null) {
            entry.clearAddressTitleFlag();
        }
    }
}
