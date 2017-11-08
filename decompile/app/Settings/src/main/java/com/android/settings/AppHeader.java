package com.android.settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.applications.AppInfoBase;
import com.android.settings.applications.InstalledAppDetails;

public class AppHeader {
    public static void createAppHeader(SettingsPreferenceFragment fragment, Drawable icon, CharSequence label, String pkgName, int uid) {
        createAppHeader(fragment, icon, label, pkgName, uid, 0, null);
    }

    public static void createAppHeader(SettingsPreferenceFragment fragment, Drawable icon, CharSequence label, String pkgName, int uid, Intent externalSettings) {
        createAppHeader(fragment, icon, label, pkgName, uid, 0, externalSettings);
    }

    public static void createAppHeader(Activity activity, Drawable icon, CharSequence label, String pkgName, int uid, ViewGroup pinnedHeader) {
        View bar = activity.getLayoutInflater().inflate(2130968627, pinnedHeader, false);
        setupHeaderView(activity, icon, label, pkgName, uid, false, 0, bar, null);
        pinnedHeader.addView(bar);
    }

    public static void createAppHeader(SettingsPreferenceFragment fragment, Drawable icon, CharSequence label, String pkgName, int uid, int tintColorRes) {
        createAppHeader(fragment, icon, label, pkgName, uid, tintColorRes, null);
    }

    public static void createAppHeader(SettingsPreferenceFragment fragment, Drawable icon, CharSequence label, String pkgName, int uid, int tintColorRes, Intent externalSettings) {
        Drawable drawable = icon;
        CharSequence charSequence = label;
        String str = pkgName;
        int i = uid;
        setupHeaderView(fragment.getActivity(), drawable, charSequence, str, i, includeAppInfo(fragment), tintColorRes, fragment.setPinnedHeaderView(2130968627), externalSettings);
    }

    public static View setupHeaderView(final Activity activity, Drawable icon, CharSequence label, final String pkgName, final int uid, final boolean includeAppInfo, int tintColorRes, View bar, final Intent externalSettings) {
        ImageView appIcon = (ImageView) bar.findViewById(2131886245);
        appIcon.setImageDrawable(icon);
        if (tintColorRes != 0) {
            appIcon.setImageTintList(ColorStateList.valueOf(activity.getColor(tintColorRes)));
        }
        ((TextView) bar.findViewById(2131886246)).setText(label);
        if (!(pkgName == null || pkgName.equals("os"))) {
            bar.setClickable(true);
            bar.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (includeAppInfo) {
                        AppInfoBase.startAppInfoFragment(InstalledAppDetails.class, 2131625599, pkgName, uid, activity, 1);
                    } else {
                        activity.finish();
                    }
                }
            });
            ImageView appSettings = (ImageView) bar.findViewById(2131886247);
            appSettings.setVisibility(0);
            if (externalSettings != null) {
                appSettings.setImageResource(2130838315);
                appSettings.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        try {
                            activity.startActivityAsCaller(externalSettings, null, false, UserHandle.myUserId());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        return bar;
    }

    public static boolean includeAppInfo(Fragment fragment) {
        Bundle args = fragment.getArguments();
        boolean showInfo = true;
        if (args != null && args.getBoolean("hideInfoButton", false)) {
            showInfo = false;
        }
        Intent intent = fragment.getActivity().getIntent();
        if (intent == null || !intent.getBooleanExtra("hideInfoButton", false)) {
            return showInfo;
        }
        return false;
    }
}
