package com.huawei.watermark.ui.baseview.viewpager;

import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

public abstract class WMBasePagerAdapter {
    public static final int POSITION_NONE = -2;
    public static final int POSITION_UNCHANGED = -1;
    private DataSetObserver mObserver;

    interface DataSetObserver {
        void onDataSetChanged();
    }

    public abstract void destroyItem(ViewGroup viewGroup, int i, Object obj);

    public abstract int getCount();

    public abstract Object instantiateItem(ViewGroup viewGroup, int i);

    public abstract boolean isViewFromObject(View view, Object obj);

    public abstract Parcelable saveState();

    public int getItemPosition(Object object) {
        return -1;
    }

    public void notifyDataSetChanged() {
        if (this.mObserver != null) {
            this.mObserver.onDataSetChanged();
        }
    }

    void setDataSetObserver(DataSetObserver observer) {
        this.mObserver = observer;
    }
}
