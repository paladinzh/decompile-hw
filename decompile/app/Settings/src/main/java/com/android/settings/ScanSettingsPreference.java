package com.android.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.widget.TextView;
import com.android.settings.LinkifyUtils.OnClickListener;
import com.android.settings.location.ScanningSettings;

public class ScanSettingsPreference extends Preference {
    private Context mContext;
    private String mText;

    private class onScanSettingsClickListener implements OnClickListener {
        public void onClick() {
            ((SettingsActivity) ScanSettingsPreference.this.mContext).startPreferencePanel(ScanningSettings.class.getName(), null, 2131628150, null, null, 0);
        }
    }

    public ScanSettingsPreference(Context context, CharSequence text) {
        super(context);
        if (text == null) {
            Log.e("ScanSettingsPreference", "null text.");
            this.mText = "";
        } else {
            this.mText = text.toString();
        }
        this.mContext = context;
        setLayoutResource(2130968958);
        setSelectable(false);
        setOnPreferenceClickListener(null);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        udpateViewContent(view);
    }

    public void udpateViewContent(PreferenceViewHolder view) {
        TextView textView = (TextView) view.findViewById(2131886930);
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(this.mText);
        view.setDividerAllowedAbove(true);
        LinkifyUtils.linkify(this.mContext, textView, contentBuilder, new onScanSettingsClickListener());
    }
}
