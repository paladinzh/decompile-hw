package com.android.setupwizardlib.items;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import com.android.setupwizardlib.DividerItemDecoration.DividedViewHolder;

class ItemViewHolder extends ViewHolder implements DividedViewHolder {
    private boolean mIsEnabled;
    private IItem mItem;

    public ItemViewHolder(View itemView) {
        super(itemView);
    }

    public boolean isDividerAllowedAbove() {
        return this.mIsEnabled;
    }

    public boolean isDividerAllowedBelow() {
        return this.mIsEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.mIsEnabled = isEnabled;
        this.itemView.setClickable(isEnabled);
        this.itemView.setEnabled(isEnabled);
        this.itemView.setFocusable(isEnabled);
    }

    public void setItem(IItem item) {
        this.mItem = item;
    }

    public IItem getItem() {
        return this.mItem;
    }
}
