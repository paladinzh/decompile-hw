package com.huawei.mms.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import com.google.android.gms.R;

public abstract class HwAxisListView extends ListView {
    private Drawable mDrawable;
    private int mListHeadHeight;
    private int mListitemHeight;

    public HwAxisListView(Context context) {
        super(context);
        init();
    }

    public HwAxisListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HwAxisListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        this.mListitemHeight = (int) this.mContext.getResources().getDimension(R.dimen.list_item_height);
        this.mDrawable = this.mContext.getResources().getDrawable(R.drawable.csp_list_time_line);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    private int getListViewScrollY() {
        View firstChild = getChildAt(0);
        if (firstChild == null) {
            return 0;
        }
        return (-firstChild.getTop()) + (this.mListitemHeight * getFirstVisiblePosition());
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!(getHeaderViewsCount() == getChildCount() || getCount() <= 0 || this.mDrawable == null)) {
            if (this.mListHeadHeight != 0) {
                Rect rect = this.mDrawable.getBounds();
                rect.top = this.mListHeadHeight - getListViewScrollY();
                this.mDrawable.setBounds(rect);
            }
            this.mDrawable.draw(canvas);
        }
    }

    public void setHeadHeight(int height) {
        this.mListHeadHeight = height;
    }
}
