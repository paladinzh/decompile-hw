package com.android.settings.display;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.util.Log;
import com.android.settings.HwCustSplitUtils;
import com.android.settings.PreviewSeekBarPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.display.DisplayDensityUtils;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.List;

public class ScreenZoomSettings extends PreviewSeekBarPreferenceFragment implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            Resources res = context.getResources();
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = res.getString(2131627943);
            data.screenTitle = res.getString(2131627943);
            data.keywords = res.getString(2131627074);
            return new ArrayList(1);
        }
    };
    private int mDefaultDensity;
    protected HwCustSplitUtils mHwCustSplitUtils;
    private int[] mValues;

    private void setInitDpi() {
        int initDpi = Secure.getInt(getContext().getContentResolver(), "init_dpi", -1);
        int intiDisplayDpi = DisplayDensityUtils.getDefaultDisplayDensity(0);
        if (initDpi == -1) {
            Secure.putInt(getContext().getContentResolver(), "init_dpi", intiDisplayDpi);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        setInitDpi();
        this.mHwCustSplitUtils = (HwCustSplitUtils) HwCustUtils.createObj(HwCustSplitUtils.class, new Object[]{getActivity()});
        this.mHwCustSplitUtils.setControllerShowing(true);
        super.onCreate(savedInstanceState);
        this.mActivityLayoutResId = 2130969074;
        this.mPreviewSampleResIds = new int[]{2130969075, 2130968752};
        DisplayDensityUtils density = new DisplayDensityUtils(getContext());
        int initialIndex = density.getCurrentIndex();
        if (initialIndex < 0) {
            this.mValues = new int[]{getResources().getDisplayMetrics().densityDpi};
            this.mEntries = new String[]{getString(DisplayDensityUtils.SUMMARY_DEFAULT)};
            this.mInitialIndex = 0;
            this.mDefaultDensity = densityDpi;
            return;
        }
        this.mValues = density.getValues();
        this.mEntries = density.getEntries();
        this.mInitialIndex = initialIndex;
        this.mDefaultDensity = density.getDefaultDensity();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mHwCustSplitUtils.setControllerShowing(false);
    }

    protected Configuration createConfig(Configuration origConfig, int index) {
        Configuration config = new Configuration(origConfig);
        config.densityDpi = this.mValues[index];
        return config;
    }

    protected void commit() {
        int densityDpi = this.mValues[this.mCurrentIndex];
        try {
            Configuration curConfig = new Configuration();
            curConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
            curConfig.extraConfig.setDensityDPI(densityDpi);
        } catch (RemoteException e) {
            Log.e("ScreenZoomSettings", "Unable to set dpi scale");
        } catch (Exception e2) {
            Log.e("ScreenZoomSettings", "get configuration error, error msg: " + e2.getMessage());
        }
        DisplayDensityUtils.setForcedDisplayDensity(0, densityDpi);
    }

    protected int getMetricsCategory() {
        return 339;
    }
}
