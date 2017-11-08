package com.android.settings.datausage;

import android.content.Context;
import android.net.NetworkTemplate;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import com.android.settings.datausage.TemplatePreference.NetworkServices;

public class NetworkRestrictionsPreference extends Preference implements TemplatePreference {
    public NetworkRestrictionsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTemplate(NetworkTemplate template, int subId, NetworkServices services) {
    }
}
