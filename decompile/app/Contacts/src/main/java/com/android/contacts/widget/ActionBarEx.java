package com.android.contacts.widget;

import android.app.ActionBar;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;

public class ActionBarEx {
    public static void setStartIcon(ActionBar actionBar, boolean icon1Visible, Drawable icon1, OnClickListener listener1) {
        try {
            com.huawei.android.app.ActionBarEx.setStartIcon(actionBar, icon1Visible, icon1, listener1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setEndIcon(ActionBar actionBar, boolean icon2Visible, Drawable icon2, OnClickListener listener2) {
        try {
            com.huawei.android.app.ActionBarEx.setEndIcon(actionBar, icon2Visible, icon2, listener2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setCustomTitle(ActionBar actionBar, View view) {
        try {
            com.huawei.android.app.ActionBarEx.setCustomTitle(actionBar, view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
