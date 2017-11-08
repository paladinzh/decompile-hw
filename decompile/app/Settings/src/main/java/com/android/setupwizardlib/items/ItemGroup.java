package com.android.setupwizardlib.items;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import com.android.setupwizardlib.items.ItemHierarchy.Observer;
import com.android.setupwizardlib.items.ItemInflater.ItemParent;
import java.util.ArrayList;
import java.util.List;

public class ItemGroup extends AbstractItemHierarchy implements ItemParent, Observer {
    private List<ItemHierarchy> mChildren = new ArrayList();
    private int mCount = 0;
    private boolean mDirty = false;
    private SparseIntArray mHierarchyStart = new SparseIntArray();

    private static int binarySearch(SparseIntArray array, int value) {
        int lo = 0;
        int hi = array.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int midVal = array.valueAt(mid);
            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal <= value) {
                return array.keyAt(mid);
            } else {
                hi = mid - 1;
            }
        }
        return array.keyAt(lo - 1);
    }

    public ItemGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addChild(ItemHierarchy child) {
        this.mChildren.add(child);
        child.registerObserver(this);
        onHierarchyChanged();
    }

    public int getCount() {
        updateDataIfNeeded();
        return this.mCount;
    }

    public IItem getItemAt(int position) {
        int itemIndex = getItemIndex(position);
        return ((ItemHierarchy) this.mChildren.get(itemIndex)).getItemAt(position - this.mHierarchyStart.get(itemIndex));
    }

    public void onChanged(ItemHierarchy hierarchy) {
        this.mDirty = true;
        notifyChanged();
    }

    private void onHierarchyChanged() {
        onChanged(null);
    }

    public ItemHierarchy findItemById(int id) {
        if (id == getId()) {
            return this;
        }
        for (ItemHierarchy child : this.mChildren) {
            ItemHierarchy childFindItem = child.findItemById(id);
            if (childFindItem != null) {
                return childFindItem;
            }
        }
        return null;
    }

    private void updateDataIfNeeded() {
        if (this.mDirty) {
            this.mCount = 0;
            this.mHierarchyStart.clear();
            for (int itemIndex = 0; itemIndex < this.mChildren.size(); itemIndex++) {
                ItemHierarchy item = (ItemHierarchy) this.mChildren.get(itemIndex);
                if (item.getCount() > 0) {
                    this.mHierarchyStart.put(itemIndex, this.mCount);
                }
                this.mCount += item.getCount();
            }
            this.mDirty = false;
        }
    }

    private int getItemIndex(int position) {
        updateDataIfNeeded();
        if (position < 0 || position >= this.mCount) {
            throw new IndexOutOfBoundsException("size=" + this.mCount + "; index=" + position);
        }
        int result = binarySearch(this.mHierarchyStart, position);
        if (result >= 0) {
            return result;
        }
        throw new IllegalStateException("Cannot have item start index < 0");
    }
}
