package com.huawei.gallery.burst;

import com.android.gallery3d.data.MediaItem;
import java.util.ArrayList;
import java.util.Arrays;

public class BurstSelectManager {
    private ArrayList<MediaItem> mItemArray = new ArrayList();
    private boolean[] mToggleState;

    public void setData(ArrayList<MediaItem> itemArray) {
        this.mItemArray.clear();
        this.mItemArray.addAll(itemArray);
        this.mToggleState = new boolean[itemArray.size()];
        Arrays.fill(this.mToggleState, false);
    }

    public void onDestroy() {
        this.mItemArray.clear();
        this.mToggleState = null;
    }

    public boolean toggle(int index) {
        boolean z = false;
        if (this.mToggleState == null || this.mToggleState.length < index || this.mToggleState.length == 0) {
            return false;
        }
        boolean[] zArr = this.mToggleState;
        if (!this.mToggleState[index]) {
            z = true;
        }
        zArr[index] = z;
        return this.mToggleState[index];
    }

    public boolean isSelected(int position) {
        boolean[] togleState = this.mToggleState;
        if (togleState == null || togleState.length <= position) {
            return false;
        }
        return togleState[position];
    }

    public int getToggleCount() {
        int count = 0;
        int i = 0;
        while (this.mToggleState != null && i < this.mToggleState.length) {
            if (this.mToggleState[i]) {
                count++;
            }
            i++;
        }
        return count;
    }

    public ArrayList<MediaItem> getSelectItems() {
        ArrayList<MediaItem> result = new ArrayList();
        int i = 0;
        while (this.mToggleState != null && i < this.mToggleState.length) {
            if (this.mToggleState[i]) {
                result.add((MediaItem) this.mItemArray.get(i));
            }
            i++;
        }
        return result;
    }
}
