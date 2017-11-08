package com.android.settings.display;

import android.content.Context;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.R$attr;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.android.settingslib.display.DisplayDensityUtils;

public class ScreenZoomPreference extends PreferenceGroup {
    public ScreenZoomPreference(Context context, AttributeSet attrs) {
        super(context, attrs, TypedArrayUtils.getAttr(context, R$attr.preferenceScreenStyle, 16842891));
        if (TextUtils.isEmpty(getFragment())) {
            setFragment("com.android.settings.display.ScreenZoomSettings");
        }
        DisplayDensityUtils density = new DisplayDensityUtils(context);
        if (density.getCurrentIndex() < 0) {
            setVisible(false);
            setEnabled(false);
        } else if (TextUtils.isEmpty(getSummary())) {
            setSummary(density.getEntries()[density.getCurrentIndex()]);
        }
    }

    protected boolean isOnSameScreenAsChildren() {
        return false;
    }
}
