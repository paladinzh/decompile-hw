package com.android.settings.localepicker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

public class LocaleLinearLayoutManager extends LinearLayoutManager {
    private final AccessibilityActionCompat mActionMoveBottom = new AccessibilityActionCompat(2131886098, this.mContext.getString(2131624563));
    private final AccessibilityActionCompat mActionMoveDown = new AccessibilityActionCompat(2131886096, this.mContext.getString(2131624561));
    private final AccessibilityActionCompat mActionMoveTop = new AccessibilityActionCompat(2131886097, this.mContext.getString(2131624562));
    private final AccessibilityActionCompat mActionMoveUp = new AccessibilityActionCompat(2131886095, this.mContext.getString(2131624560));
    private final AccessibilityActionCompat mActionRemove = new AccessibilityActionCompat(2131886099, this.mContext.getString(2131624564));
    private final LocaleDragAndDropAdapter mAdapter;
    private final Context mContext;

    public LocaleLinearLayoutManager(Context context, LocaleDragAndDropAdapter adapter) {
        super(context);
        this.mContext = context;
        this.mAdapter = adapter;
    }

    public void onInitializeAccessibilityNodeInfoForItem(Recycler recycler, State state, View host, AccessibilityNodeInfoCompat info) {
        super.onInitializeAccessibilityNodeInfoForItem(recycler, state, host, info);
        int itemCount = getItemCount();
        int position = getPosition(host);
        info.setContentDescription((position + 1) + ", " + ((LocaleDragCell) host).getCheckbox().getContentDescription());
        if (!this.mAdapter.isRemoveMode()) {
            if (position > 0) {
                info.addAction(this.mActionMoveUp);
                info.addAction(this.mActionMoveTop);
            }
            if (position + 1 < itemCount) {
                info.addAction(this.mActionMoveDown);
                info.addAction(this.mActionMoveBottom);
            }
            if (itemCount > 1) {
                info.addAction(this.mActionRemove);
            }
        }
    }

    public boolean performAccessibilityActionForItem(Recycler recycler, State state, View host, int action, Bundle args) {
        int itemCount = getItemCount();
        int position = getPosition(host);
        boolean result = false;
        switch (action) {
            case 2131886095:
                if (position > 0) {
                    this.mAdapter.onItemMove(position, position - 1);
                    result = true;
                    break;
                }
                break;
            case 2131886096:
                if (position + 1 < itemCount) {
                    this.mAdapter.onItemMove(position, position + 1);
                    result = true;
                    break;
                }
                break;
            case 2131886097:
                if (position != 0) {
                    this.mAdapter.onItemMove(position, 0);
                    result = true;
                    break;
                }
                break;
            case 2131886098:
                if (position != itemCount - 1) {
                    this.mAdapter.onItemMove(position, itemCount - 1);
                    result = true;
                    break;
                }
                break;
            case 2131886099:
                if (itemCount > 1) {
                    this.mAdapter.removeItem(position);
                    result = true;
                    break;
                }
                break;
            default:
                return super.performAccessibilityActionForItem(recycler, state, host, action, args);
        }
        if (result) {
            this.mAdapter.doTheUpdate();
        }
        return result;
    }
}
