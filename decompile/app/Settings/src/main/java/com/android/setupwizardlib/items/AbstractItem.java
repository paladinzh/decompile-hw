package com.android.setupwizardlib.items;

import android.content.Context;
import android.util.AttributeSet;

public abstract class AbstractItem extends AbstractItemHierarchy implements IItem {
    public AbstractItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getCount() {
        return 1;
    }

    public IItem getItemAt(int position) {
        return this;
    }

    public ItemHierarchy findItemById(int id) {
        if (id == getId()) {
            return this;
        }
        return null;
    }
}
