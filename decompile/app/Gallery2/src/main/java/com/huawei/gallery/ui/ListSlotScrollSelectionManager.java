package com.huawei.gallery.ui;

import com.huawei.gallery.ui.ListSlotView.ItemCoordinate;
import java.util.ArrayList;

public class ListSlotScrollSelectionManager {
    private boolean mInitItemSelectStatus;
    private Listener mListener;
    private ArrayList<ItemCoordinate> mScrollItemIndexList = new ArrayList();

    public interface Listener {
        boolean isSelected(ItemCoordinate itemCoordinate);

        void onScrollSelect(ItemCoordinate itemCoordinate, boolean z);
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void addScrollIndex(ItemCoordinate index) {
        if (index != null && !index.isTitle()) {
            if (this.mScrollItemIndexList.isEmpty()) {
                this.mInitItemSelectStatus = !this.mListener.isSelected(index);
                this.mScrollItemIndexList.add(index);
                this.mListener.onScrollSelect(index, this.mInitItemSelectStatus);
                return;
            }
            if (!this.mScrollItemIndexList.contains(index)) {
                this.mListener.onScrollSelect(index, this.mInitItemSelectStatus);
            }
        }
    }

    public void clearup() {
        this.mScrollItemIndexList.clear();
        this.mInitItemSelectStatus = false;
    }
}
