package com.android.setupwizardlib.items;

import android.content.Context;

public class ItemInflater extends GenericInflater<ItemHierarchy> {
    private final Context mContext;

    public interface ItemParent {
        void addChild(ItemHierarchy itemHierarchy);
    }

    public ItemInflater(Context context) {
        super(context);
        this.mContext = context;
        setDefaultPackage(Item.class.getPackage().getName() + ".");
    }

    public Context getContext() {
        return this.mContext;
    }

    protected void onAddChildItem(ItemHierarchy parent, ItemHierarchy child) {
        if (parent instanceof ItemParent) {
            ((ItemParent) parent).addChild(child);
            return;
        }
        throw new IllegalArgumentException("Cannot add child item to " + parent);
    }
}
