package com.huawei.cspcommon.util;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class ViewUtil {
    private static final boolean LOG_DEBUG = HwLog.HWDBG;

    private ViewUtil() {
    }

    public static int getConstantPreLayoutWidth(View view) {
        LayoutParams p = view.getLayoutParams();
        if (p.width >= 0) {
            return p.width;
        }
        throw new IllegalStateException("Expecting view's width to be a constant rather than a result of the layout pass");
    }

    public static void setMenuItemStateListIcon(Context context, MenuItem item) {
        if (item != null) {
            Drawable drawable = item.getIcon();
            if (drawable instanceof BitmapDrawable) {
                if (LOG_DEBUG) {
                    HwLog.d("ViewUtil", String.valueOf(item.getTitle()));
                }
                item.setIcon(CustomStateListDrawable.createStateDrawable(context, (BitmapDrawable) drawable, false));
            }
        }
    }

    public static void setStateListIcon(Context context, View view) {
        if (view != null && (view instanceof ImageView)) {
            Drawable drawable = ((ImageView) view).getDrawable();
            if (drawable instanceof BitmapDrawable) {
                ((ImageView) view).setImageDrawable(CustomStateListDrawable.createStateDrawable(context, (BitmapDrawable) drawable));
            }
        }
    }

    public static void setStateListIcon(Context context, View view, boolean isHighlight) {
        if (view != null && (view instanceof ImageView)) {
            Drawable drawable = ((ImageView) view).getDrawable();
            if (drawable instanceof BitmapDrawable) {
                ((ImageView) view).setImageDrawable(CustomStateListDrawable.createStateDrawable(context, (BitmapDrawable) drawable, isHighlight));
            }
        }
    }

    public static void setMenuItemsStateListIcon(Context context, Menu menu) {
        if (menu != null) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                Drawable drawable = item.getIcon();
                if (drawable instanceof BitmapDrawable) {
                    if (LOG_DEBUG) {
                        HwLog.d("ViewUtil", String.valueOf(item.getTitle()));
                    }
                    item.setIcon(CustomStateListDrawable.createStateDrawable(context, (BitmapDrawable) drawable, false));
                }
            }
        }
    }

    public static Drawable getMenuItemIcon(Context context, int resId) {
        Drawable drawable = context.getResources().getDrawable(resId);
        if (drawable instanceof BitmapDrawable) {
            return CustomStateListDrawable.createStateDrawable(context, (BitmapDrawable) drawable, false);
        }
        return drawable;
    }

    public static Drawable getSelectAllItemIcon(Context context) {
        if (context == null) {
            return null;
        }
        if (context.getResources().getConfiguration().orientation == 1 || ImmersionUtils.getImmersionStyle(context) != 1) {
            return getMenuItemIcon(context, R.drawable.csp_selected_all_normal);
        }
        return getMenuItemIcon(context, R.drawable.csp_selected_all_normal_light);
    }

    public static StateListDrawable getSelectorDrawable(Drawable normalDrawable, float pressedAlphaPercent, float focusedAlphaPercent) {
        StateListDrawable selector = new StateListDrawable();
        Drawable pressed = normalDrawable.getConstantState().newDrawable();
        Drawable focused = normalDrawable.getConstantState().newDrawable();
        int nomalAlpha = normalDrawable.getAlpha();
        pressed.setAlpha((int) (((float) nomalAlpha) * pressedAlphaPercent));
        focused.setAlpha((int) (((float) nomalAlpha) * focusedAlphaPercent));
        selector.addState(new int[]{16842919, 16842910}, pressed);
        selector.addState(new int[]{16842910, 16842908}, focused);
        selector.addState(new int[]{16842908}, focused);
        selector.addState(new int[]{16842919}, pressed);
        selector.addState(StateSet.WILD_CARD, normalDrawable);
        return selector;
    }

    public static Drawable getSelectNoneItemIcon(Context context) {
        if (context == null) {
            return null;
        }
        Drawable drawable = context.getResources().getDrawable(R.drawable.csp_selected_all_highlight);
        if (drawable instanceof BitmapDrawable) {
            drawable = CustomStateListDrawable.createStateDrawable(context, (BitmapDrawable) drawable, true);
        }
        return drawable;
    }
}
