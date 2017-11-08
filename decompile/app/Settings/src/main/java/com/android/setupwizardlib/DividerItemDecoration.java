package com.android.setupwizardlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class DividerItemDecoration extends ItemDecoration {
    private Drawable mDivider;
    private int mDividerCondition;
    private int mDividerHeight;
    private int mDividerIntrinsicHeight;

    public interface DividedViewHolder {
        boolean isDividerAllowedAbove();

        boolean isDividerAllowedBelow();
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface DividerCondition {
    }

    public static DividerItemDecoration getDefault(Context context) {
        TypedArray a = context.obtainStyledAttributes(R$styleable.SuwDividerItemDecoration);
        Drawable divider = a.getDrawable(R$styleable.SuwDividerItemDecoration_android_listDivider);
        int dividerHeight = a.getDimensionPixelSize(R$styleable.SuwDividerItemDecoration_android_dividerHeight, 0);
        int dividerCondition = a.getInt(R$styleable.SuwDividerItemDecoration_suwDividerCondition, 0);
        a.recycle();
        DividerItemDecoration decoration = new DividerItemDecoration();
        decoration.setDivider(divider);
        decoration.setDividerHeight(dividerHeight);
        decoration.setDividerCondition(dividerCondition);
        return decoration;
    }

    public void onDraw(Canvas c, RecyclerView parent, State state) {
        if (this.mDivider != null) {
            int childCount = parent.getChildCount();
            int width = parent.getWidth();
            int dividerHeight = this.mDividerHeight != 0 ? this.mDividerHeight : this.mDividerIntrinsicHeight;
            for (int childViewIndex = 0; childViewIndex < childCount; childViewIndex++) {
                View view = parent.getChildAt(childViewIndex);
                if (shouldDrawDividerBelow(view, parent)) {
                    int top = ((int) ViewCompat.getY(view)) + view.getHeight();
                    this.mDivider.setBounds(0, top, width, top + dividerHeight);
                    this.mDivider.draw(c);
                }
            }
        }
    }

    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        if (shouldDrawDividerBelow(view, parent)) {
            outRect.bottom = this.mDividerHeight != 0 ? this.mDividerHeight : this.mDividerIntrinsicHeight;
        }
    }

    private boolean shouldDrawDividerBelow(View view, RecyclerView parent) {
        ViewHolder holder = parent.getChildViewHolder(view);
        int index = holder.getLayoutPosition();
        int lastItemIndex = parent.getAdapter().getItemCount() - 1;
        if (holder instanceof DividedViewHolder) {
            if (((DividedViewHolder) holder).isDividerAllowedBelow()) {
                if (this.mDividerCondition == 0) {
                    return true;
                }
            } else if (this.mDividerCondition == 1 || index == lastItemIndex) {
                return false;
            }
        }
        if (index < lastItemIndex) {
            ViewHolder nextHolder = parent.findViewHolderForLayoutPosition(index + 1);
            return !(nextHolder instanceof DividedViewHolder) || ((DividedViewHolder) nextHolder).isDividerAllowedAbove();
        }
    }

    public void setDivider(Drawable divider) {
        if (divider != null) {
            this.mDividerIntrinsicHeight = divider.getIntrinsicHeight();
        } else {
            this.mDividerIntrinsicHeight = 0;
        }
        this.mDivider = divider;
    }

    public Drawable getDivider() {
        return this.mDivider;
    }

    public void setDividerHeight(int dividerHeight) {
        this.mDividerHeight = dividerHeight;
    }

    public void setDividerCondition(int dividerCondition) {
        this.mDividerCondition = dividerCondition;
    }
}
