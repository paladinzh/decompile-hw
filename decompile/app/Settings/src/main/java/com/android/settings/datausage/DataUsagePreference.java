package com.android.settings.datausage;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.text.format.Formatter;
import android.util.AttributeSet;
import com.android.settings.Utils;
import com.android.settings.datausage.TemplatePreference.NetworkServices;
import com.android.settingslib.net.DataUsageController;
import com.android.settingslib.net.DataUsageController.DataUsageInfo;

public class DataUsagePreference extends Preference implements TemplatePreference {
    private int mSubId;
    private NetworkTemplate mTemplate;

    public DataUsagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTemplate(NetworkTemplate template, int subId, NetworkServices services) {
        this.mTemplate = template;
        this.mSubId = subId;
        DataUsageInfo usageInfo = new DataUsageController(getContext()).getDataUsageInfo(this.mTemplate);
        setSummary(getContext().getString(2131627155, new Object[]{Formatter.formatFileSize(getContext(), usageInfo.usageLevel), usageInfo.period}));
        setIntent(getIntent());
    }

    public Intent getIntent() {
        Bundle args = new Bundle();
        args.putParcelable("network_template", this.mTemplate);
        args.putInt("sub_id", this.mSubId);
        return Utils.onBuildStartFragmentIntent(getContext(), DataUsageList.class.getName(), args, getContext().getPackageName(), 0, getTitle(), false);
    }
}
