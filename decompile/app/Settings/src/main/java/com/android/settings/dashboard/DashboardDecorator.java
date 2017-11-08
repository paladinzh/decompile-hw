package com.android.settings.dashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

public class DashboardDecorator extends ItemDecoration {
    private final Context mContext;
    private final Drawable mDivider = this.mContext.getDrawable(2130838531);

    public DashboardDecorator(Context context) {
        this.mContext = context;
    }

    public void onDrawOver(Canvas c, RecyclerView parent, State state) {
        int childCount = parent.getChildCount();
        int width = parent.getWidth();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            if (shouldDrawDividerBelow(child, parent)) {
                int top = ((int) ViewCompat.getY(child)) + child.getHeight();
                this.mDivider.setBounds(0, top, width, this.mDivider.getIntrinsicHeight() + top);
                this.mDivider.draw(c);
            }
        }
    }

    private boolean holderNeedDivider(ViewHolder holder) {
        if (holder.getItemViewType() == 2130968716 || holder.getItemViewType() == 2130968715 || holder.getItemViewType() == 2130968717 || holder.getItemViewType() == 2130969162 || holder.getItemViewType() == 2130968680) {
            return true;
        }
        return false;
    }

    private boolean shouldDrawDividerBelow(View view, RecyclerView parent) {
        if (!holderNeedDivider(parent.getChildViewHolder(view))) {
            return false;
        }
        boolean nextAllowed = true;
        int index = parent.indexOfChild(view);
        if (index < parent.getChildCount() - 1) {
            nextAllowed = holderNeedDivider(parent.getChildViewHolder(parent.getChildAt(index + 1)));
        }
        return nextAllowed;
    }
}
