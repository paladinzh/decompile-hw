package com.android.settings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageView;

public class SettingsDividerDecoration extends ItemDecoration {
    private int mBiggerIconSize;
    private Drawable mDivider;
    private int mDividerHeight;
    private Drawable mDividerWithBiggerIcon;
    private Drawable mDividerWithIcon;
    private boolean mUseNormalDividerOnly = false;

    public SettingsDividerDecoration(Context context, boolean useNormalDividerOnly) {
        this.mDivider = context.getDrawable(2130838529);
        this.mDividerWithIcon = context.getDrawable(2130838531);
        this.mDividerWithBiggerIcon = context.getDrawable(2130838530);
        this.mBiggerIconSize = context.getResources().getDimensionPixelOffset(2131558765);
        this.mDividerHeight = this.mDivider.getIntrinsicHeight();
        this.mUseNormalDividerOnly = useNormalDividerOnly;
    }

    public void onDrawOver(Canvas c, RecyclerView parent, State state) {
        if (this.mDivider != null) {
            int childCount = parent.getChildCount();
            int width = parent.getWidth();
            for (int childViewIndex = 0; childViewIndex < childCount; childViewIndex++) {
                View view = parent.getChildAt(childViewIndex);
                if (shouldDrawDividerBelow(view, parent)) {
                    int top = ((int) ViewCompat.getY(view)) + view.getHeight();
                    if (!this.mUseNormalDividerOnly) {
                        switch (getDividerType(view, parent)) {
                            case 2:
                                this.mDividerWithIcon.setBounds(0, top, width, this.mDividerHeight + top);
                                this.mDividerWithIcon.draw(c);
                                break;
                            case 3:
                                this.mDividerWithBiggerIcon.setBounds(0, top, width, this.mDividerHeight + top);
                                this.mDividerWithBiggerIcon.draw(c);
                                break;
                            default:
                                this.mDivider.setBounds(0, top, width, this.mDividerHeight + top);
                                this.mDivider.draw(c);
                                break;
                        }
                    }
                    this.mDivider.setBounds(0, top, width, this.mDividerHeight + top);
                    this.mDivider.draw(c);
                }
            }
        }
    }

    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        if (shouldDrawDividerBelow(view, parent)) {
            outRect.bottom = this.mDividerHeight;
        }
    }

    private int getDividerType(View view, RecyclerView parent) {
        ViewHolder holder = parent.getChildViewHolder(view);
        if (holder instanceof PreferenceViewHolder) {
            ImageView icon = (ImageView) ((PreferenceViewHolder) holder).findViewById(16908294);
            if (icon != null && icon.getVisibility() == 0 && icon.getWidth() > 0) {
                if (icon.getWidth() >= this.mBiggerIconSize) {
                    return 3;
                }
                return 2;
            }
        }
        return 1;
    }

    private boolean shouldDrawDividerBelow(View view, RecyclerView parent) {
        boolean dividerAllowedBelow;
        ViewHolder holder = parent.getChildViewHolder(view);
        if (holder instanceof PreferenceViewHolder) {
            dividerAllowedBelow = ((PreferenceViewHolder) holder).isDividerAllowedBelow();
        } else {
            dividerAllowedBelow = false;
        }
        if (!dividerAllowedBelow) {
            return false;
        }
        boolean nextAllowed = true;
        int index = parent.indexOfChild(view);
        if (index < parent.getChildCount() - 1) {
            ViewHolder nextHolder = parent.getChildViewHolder(parent.getChildAt(index + 1));
            if (nextHolder instanceof PreferenceViewHolder) {
                nextAllowed = ((PreferenceViewHolder) nextHolder).isDividerAllowedAbove();
            } else {
                nextAllowed = false;
            }
        }
        return nextAllowed;
    }
}
