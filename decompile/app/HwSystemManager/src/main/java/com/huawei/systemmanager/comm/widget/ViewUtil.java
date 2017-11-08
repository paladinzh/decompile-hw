package com.huawei.systemmanager.comm.widget;

import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;

public class ViewUtil {
    public static final String EMUI_SELECTOR_BACKGROUND = "@*androidhwext:drawable/item_background_emui";
    public static final int HWID_TEXT_1 = 34603077;
    public static final int HWID_TEXT_2 = 34603078;
    public static final int HWID_TEXT_3 = 34603079;

    public static TextView findTextView(ViewGroup viewGroup, int index) {
        if (viewGroup == null) {
            return null;
        }
        int childCount = viewGroup.getChildCount();
        int txNo = 0;
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof TextView) {
                txNo++;
                if (txNo == index) {
                    return (TextView) child;
                }
            }
        }
        return null;
    }

    public static void initEmptyViewMargin(Context ctx, View emptyView) {
        if (emptyView != null) {
            MarginLayoutParams params = (MarginLayoutParams) emptyView.getLayoutParams();
            int screenHeight = Utility.getScreenHeight(ctx);
            int statusBarHeight = ctx.getResources().getDimensionPixelSize(R.dimen.statusbar_height);
            int marginTop = (((screenHeight * 3) / 10) - statusBarHeight) - ctx.getResources().getDimensionPixelSize(R.dimen.actionbar_height);
            if (params.topMargin == 0) {
                params.topMargin = marginTop;
            }
        }
    }

    public static void hiddenMenu(Menu mMenu) {
        if (mMenu != null) {
            for (int i = 0; i < mMenu.size(); i++) {
                mMenu.getItem(i).setVisible(false);
                mMenu.getItem(i).setEnabled(false);
            }
        }
    }

    public static void showMenu(Menu mMenu) {
        if (mMenu != null) {
            for (int i = 0; i < mMenu.size(); i++) {
                mMenu.getItem(i).setVisible(true);
                mMenu.getItem(i).setEnabled(true);
            }
        }
    }
}
