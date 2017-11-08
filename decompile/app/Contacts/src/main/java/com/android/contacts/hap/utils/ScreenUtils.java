package com.android.contacts.hap.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.util.HwLog;

public class ScreenUtils {
    public static boolean isLandscape(Context context) {
        boolean z = false;
        if (context == null) {
            HwLog.e("ScreenUtils", "isLandscape, context is null");
            return false;
        }
        if (context.getResources().getConfiguration().orientation == 2) {
            z = true;
        }
        return z;
    }

    public static void updateButtonView(Context context, View view, int marrgin, int width) {
        if (context != null && view != null) {
            LayoutParams params = view.getLayoutParams();
            if (params instanceof RelativeLayout.LayoutParams) {
                ((RelativeLayout.LayoutParams) params).setMarginStart(marrgin);
                ((RelativeLayout.LayoutParams) params).setMarginEnd(marrgin);
            } else if (params instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) params).setMarginStart(marrgin);
                ((LinearLayout.LayoutParams) params).setMarginEnd(marrgin);
            } else if (params instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) params).setMarginStart(marrgin);
                ((FrameLayout.LayoutParams) params).setMarginEnd(marrgin);
            }
            if (width != 0) {
                params.width = width;
            } else {
                params.width = -1;
            }
            view.setLayoutParams(params);
        }
    }

    public static void updateViewTopMarrgin(Context context, View view, int id) {
        if (context != null && view != null) {
            int top = ContactDpiAdapter.getNewPxDpi(id, context);
            LayoutParams params = view.getLayoutParams();
            if (params instanceof RelativeLayout.LayoutParams) {
                ((RelativeLayout.LayoutParams) params).topMargin = top;
            } else if (params instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) params).topMargin = top;
            } else if (params instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) params).topMargin = top;
            }
            view.setLayoutParams(params);
        }
    }

    public static void updateViewMarrginValue(Context context, View view, int left, int top, int right, int bottom) {
        if (context != null && view != null) {
            LayoutParams params = view.getLayoutParams();
            LayoutParams marginParams = null;
            if (params instanceof RelativeLayout.LayoutParams) {
                marginParams = (MarginLayoutParams) params;
            } else if (params instanceof LinearLayout.LayoutParams) {
                marginParams = (MarginLayoutParams) params;
            } else if (params instanceof FrameLayout.LayoutParams) {
                marginParams = (MarginLayoutParams) params;
            }
            if (marginParams != null) {
                marginParams.topMargin = top;
                marginParams.bottomMargin = bottom;
                marginParams.setMarginStart(left);
                marginParams.setMarginEnd(right);
                view.setLayoutParams(marginParams);
            }
        }
    }

    public static void adjustPaddingTop(Context context, View contentView, boolean isTwoColum) {
        if (contentView != null && context != null) {
            int statusHeight = ContactDpiAdapter.getStatusBarHeight(context);
            int actionbarHeight = ContactDpiAdapter.getActionbarHeight(context);
            if (isTwoColum) {
                contentView.setPadding(0, statusHeight + actionbarHeight, 0, 0);
            } else {
                contentView.setPadding(0, statusHeight, 0, 0);
            }
        }
    }
}
