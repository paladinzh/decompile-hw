package com.android.systemui.statusbar.phone;

import com.android.internal.statusbar.StatusBarIcon;
import java.util.ArrayList;

public class StatusBarIconList {
    private ArrayList<StatusBarIcon> mIcons = new ArrayList();
    private ArrayList<String> mSlots = new ArrayList();

    public void defineSlots(String[] slots) {
        this.mSlots.clear();
        for (Object add : slots) {
            this.mSlots.add(add);
            if (this.mIcons.size() < this.mSlots.size()) {
                this.mIcons.add(null);
            }
        }
    }

    public int getSlotIndex(String slot) {
        int N = this.mSlots.size();
        for (int i = 0; i < N; i++) {
            if (slot.equals(this.mSlots.get(i))) {
                return i;
            }
        }
        this.mSlots.add(0, slot);
        this.mIcons.add(0, null);
        return 0;
    }

    public void setIcon(int index, StatusBarIcon icon) {
        this.mIcons.set(index, icon);
    }

    public void removeIcon(int index) {
        this.mIcons.set(index, null);
    }

    public String getSlot(int index) {
        return (String) this.mSlots.get(index);
    }

    public StatusBarIcon getIcon(int index) {
        return (StatusBarIcon) this.mIcons.get(index);
    }

    public int getViewIndex(int index) {
        int count = 0;
        for (int i = 0; i < index; i++) {
            if (this.mIcons.get(i) != null) {
                count++;
            }
        }
        return count;
    }
}
