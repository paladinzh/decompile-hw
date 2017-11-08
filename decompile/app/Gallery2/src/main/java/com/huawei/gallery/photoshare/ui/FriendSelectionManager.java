package com.huawei.gallery.photoshare.ui;

import com.huawei.android.cg.vo.ShareReceiver;
import java.util.ArrayList;

public class FriendSelectionManager {
    private boolean mInSelectionMode = false;
    FriendSelectionListener mListener = null;
    private ArrayList<ShareReceiver> mSelectionItems = new ArrayList();

    public interface FriendSelectionListener {
        void onItemChange(ShareReceiver shareReceiver, boolean z);

        void onModeChange(int i);
    }

    public void setListener(FriendSelectionListener listener) {
        this.mListener = listener;
    }

    public boolean inSelectionMode() {
        return this.mInSelectionMode;
    }

    public void toggle(ShareReceiver item) {
        if (this.mSelectionItems.contains(item)) {
            this.mSelectionItems.remove(item);
        } else {
            this.mSelectionItems.add(item);
        }
        if (this.mListener != null) {
            this.mListener.onItemChange(item, isItemSelected(item));
        }
    }

    public void enterSelectionMode() {
        this.mInSelectionMode = true;
        if (this.mListener != null) {
            this.mListener.onModeChange(1);
        }
    }

    public void leaveSelectionMode() {
        this.mSelectionItems.clear();
        this.mInSelectionMode = false;
        if (this.mListener != null) {
            this.mListener.onModeChange(2);
        }
    }

    public int size() {
        return this.mSelectionItems.size();
    }

    public boolean isItemSelected(ShareReceiver item) {
        return this.mSelectionItems.contains(item);
    }

    public ArrayList<ShareReceiver> getAllItems() {
        return (ArrayList) this.mSelectionItems.clone();
    }
}
