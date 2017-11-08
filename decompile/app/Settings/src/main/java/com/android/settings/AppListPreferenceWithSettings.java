package com.android.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class AppListPreferenceWithSettings extends AppListPreference {
    private ComponentName mSettingsComponent;
    private View mSettingsIcon;

    public AppListPreferenceWithSettings(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(2130968977);
        setWidgetLayoutResource(2130968998);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mSettingsIcon = view.findViewById(2131886968);
        ((ViewGroup) this.mSettingsIcon.getParent()).setPaddingRelative(0, 0, 0, 0);
        updateSettingsVisibility();
    }

    private void updateSettingsVisibility() {
        if (!(this.mSettingsIcon == null || this.mSettingsComponent == null)) {
            this.mSettingsIcon.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.setComponent(AppListPreferenceWithSettings.this.mSettingsComponent);
                    AppListPreferenceWithSettings.this.getContext().startActivity(new Intent(intent));
                }
            });
        }
    }

    protected void setSettingsComponent(ComponentName settings) {
        this.mSettingsComponent = settings;
        updateSettingsVisibility();
    }
}
