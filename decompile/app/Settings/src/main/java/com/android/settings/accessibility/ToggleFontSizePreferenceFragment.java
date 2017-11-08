package com.android.settings.accessibility;

import android.content.ContentResolver;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings.System;
import android.widget.Toast;
import com.android.settings.HwCustSplitUtils;
import com.android.settings.PreviewSeekBarPreferenceFragment;
import com.huawei.cust.HwCustUtils;

public class ToggleFontSizePreferenceFragment extends PreviewSeekBarPreferenceFragment {
    private HwCustSplitUtils mHwCustSplitUtils = null;
    private float[] mValues;

    public void onCreate(Bundle savedInstanceState) {
        this.mHwCustSplitUtils = (HwCustSplitUtils) HwCustUtils.createObj(HwCustSplitUtils.class, new Object[]{getActivity()});
        this.mHwCustSplitUtils.setControllerShowing(true);
        super.onCreate(savedInstanceState);
        this.mActivityLayoutResId = 2130968818;
        this.mPreviewSampleResIds = new int[]{2130968819};
        Resources res = getContext().getResources();
        ContentResolver resolver = getContext().getContentResolver();
        this.mEntries = res.getStringArray(2131361838);
        String[] strEntryValues = res.getStringArray(2131361839);
        this.mInitialIndex = fontSizeValueToIndex(System.getFloat(resolver, "font_scale", 1.0f), strEntryValues);
        this.mValues = new float[strEntryValues.length];
        for (int i = 0; i < strEntryValues.length; i++) {
            this.mValues[i] = Float.parseFloat(strEntryValues[i]);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mHwCustSplitUtils != null) {
            this.mHwCustSplitUtils.setControllerShowing(false);
            this.mHwCustSplitUtils = null;
        }
    }

    protected Configuration createConfig(Configuration origConfig, int index) {
        Configuration config = new Configuration(origConfig);
        config.fontScale = this.mValues[index];
        return config;
    }

    protected void commit() {
        if (getContext() != null) {
            if (this.mCurrentIndex == 4) {
                Toast.makeText(getContext(), getResources().getString(2131627435), 1).show();
            }
            System.putFloat(getContext().getContentResolver(), "font_scale", this.mValues[this.mCurrentIndex]);
        }
    }

    protected int getMetricsCategory() {
        return 340;
    }

    public static int fontSizeValueToIndex(float val, String[] indices) {
        float lastVal = Float.parseFloat(indices[0]);
        for (int i = 1; i < indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < ((thisVal - lastVal) * 0.5f) + lastVal) {
                return i - 1;
            }
            lastVal = thisVal;
        }
        return indices.length - 1;
    }
}
