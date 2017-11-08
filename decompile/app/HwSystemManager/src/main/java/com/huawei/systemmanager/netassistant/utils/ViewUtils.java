package com.huawei.systemmanager.netassistant.utils;

import android.view.MenuItem;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.comm.misc.Utility;

public final class ViewUtils {
    private ViewUtils() {
    }

    public static void setVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    public static void setVisible(MenuItem item, boolean flag) {
        if (item != null) {
            item.setVisible(flag);
        }
    }

    public static void setVisibility(View view, boolean flag) {
        setVisibility(view, flag ? 0 : 8);
    }

    public static void setChecked(Checkable view, boolean flag) {
        if (view != null) {
            view.setChecked(flag);
        }
    }

    public static void setEnabled(View view, boolean flag) {
        if (view != null) {
            view.setEnabled(flag);
        }
    }

    public static boolean isChecked(Checkable view) {
        if (view != null) {
            return view.isChecked();
        }
        return false;
    }

    public static void setEnableAlpha(View view, boolean enable) {
        if (view == null) {
            return;
        }
        if (enable) {
            view.setAlpha(Utility.ALPHA_MAX);
        } else {
            view.setAlpha(0.3f);
        }
    }

    public static void setTextSize(TextView tv, int size) {
        if (tv != null) {
            tv.setTextSize(1, (float) size);
        }
    }

    public static void setText(TextView tv, int id) {
        if (tv != null) {
            tv.setText(id);
        }
    }

    public static void setText(TextView tv, String value) {
        if (tv != null) {
            tv.setText(value);
        }
    }

    public static void setImageResource(ImageView imageView, int imageId) {
        if (imageView != null) {
            imageView.setImageResource(imageId);
        }
    }
}
