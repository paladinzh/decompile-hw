package com.huawei.gallery.ui;

import java.util.ArrayList;

public class ScrollSelectionManager {
    private boolean mInitSelectedStatus;
    private Listener mListener;
    private ArrayList<Integer> mScrollItemIndexList = new ArrayList();

    public interface Listener {
        boolean isSelected(int i);

        void onScrollSelect(int i, boolean z);
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void addScrollIndex(int index) {
        if (index != -1) {
            if (this.mScrollItemIndexList.isEmpty()) {
                this.mInitSelectedStatus = !this.mListener.isSelected(index);
                this.mScrollItemIndexList.add(Integer.valueOf(index));
                this.mListener.onScrollSelect(index, this.mInitSelectedStatus);
                return;
            }
            if (!this.mScrollItemIndexList.contains(Integer.valueOf(index))) {
                this.mListener.onScrollSelect(index, this.mInitSelectedStatus);
            }
        }
    }

    public void clearup() {
        this.mScrollItemIndexList.clear();
        this.mInitSelectedStatus = false;
    }
}
