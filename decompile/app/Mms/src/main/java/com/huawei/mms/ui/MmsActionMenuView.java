package com.huawei.mms.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Menu;
import com.android.internal.view.menu.MenuBuilder;
import huawei.android.widget.HwActionMenuView;

public class MmsActionMenuView extends HwActionMenuView {
    private int mContentHeight = 0;
    MenuBuilder mMenu;

    public MmsActionMenuView(Context context) {
        super(context);
    }

    public MmsActionMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (top > 0) {
            super.onLayout(changed, left, 0, right, bottom - top);
            return;
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    public Menu getMenu() {
        return this.mMenu;
    }
}
