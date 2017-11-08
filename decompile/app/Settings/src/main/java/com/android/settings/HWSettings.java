package com.android.settings;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.android.settings.Settings.AdvancedSettingsActivity;
import com.android.settings.Settings.WifiSettingsActivity;
import com.android.settingslib.Utils;
import com.huawei.cust.HwCustUtils;

public class HWSettings extends SettingsActivity {
    private HwCustSplitUtils mHwCustSplitUtils;
    private boolean mIsFirstLoaded = true;

    protected void onCreate(Bundle savedState) {
        this.mHwCustSplitUtils = (HwCustSplitUtils) HwCustUtils.createObj(HwCustSplitUtils.class, new Object[]{this});
        Log.i("HWSettings", "is emui lite : " + Utils.isEmuiLite());
        if (Utils.isEmuiLite()) {
            Log.i("HWSettings", "remove drawer and delay update categories for lite version! ");
            setNoDrawer(true);
        }
        super.onCreate(savedState);
        if (Utils.isSimpleModeOn()) {
            Intent intent;
            if (!this.mHwCustSplitUtils.reachSplitSize()) {
                intent = new Intent();
                intent.setClass(this, SimpleSettings.class);
                startActivity(intent);
                finish();
                return;
            } else if (getIntent().getBooleanExtra("extra_split", false)) {
                i = new Intent("android.settings.WIFI_SETTINGS");
                i.setClass(this, WifiSettingsActivity.class);
                this.mHwCustSplitUtils.setTargetIntent(i);
            } else {
                intent = new Intent();
                intent.setClass(this, SimpleSettings.class);
                startActivity(intent);
                i = new Intent();
                i.setClass(this, AdvancedSettingsActivity.class);
                this.mHwCustSplitUtils.setTargetIntent(i);
                finish();
                return;
            }
        }
        if (savedState != null) {
            this.mDisplaySearch = savedState.getBoolean(":settings:show_search");
            if (this.mDisplaySearch) {
                this.mSearchMenuItemExpanded = savedState.getBoolean(":settings:search_menu_expanded");
                this.mSearchQuery = savedState.getString(":settings:search_query");
                switchToSearchResult();
                MLog.d("HWSettings", "Switch to search fragment from save instance");
            }
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (this.mHwCustSplitUtils.reachSplitSize() && Utils.isSimpleModeOn()) {
            Intent i;
            if (intent.getBooleanExtra("extra_split", false)) {
                i = new Intent("android.settings.WIFI_SETTINGS");
                i.setClass(this, WifiSettingsActivity.class);
                this.mHwCustSplitUtils.setTargetIntent(i);
            } else {
                i = new Intent();
                i.setClass(this, SimpleSettings.class);
                startActivity(i);
                finish();
            }
        }
    }

    public void onResume() {
        if (this.mIsFirstLoaded) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("com.android.settings.action.CLEAR_TASK"));
            this.mIsFirstLoaded = false;
        }
        if (1 == System.getInt(getContentResolver(), "Simple mode", 0)) {
            MLog.d("HWSettings", "Detect launcher mode changes, refresh categories.");
            System.putInt(getContentResolver(), "Simple mode", 0);
        }
        super.onResume();
    }

    public void showBackIcon() {
    }

    public void onBackPressed() {
        if (!isTaskRoot() || this.mDisplaySearch) {
            super.onBackPressed();
            return;
        }
        if (!moveTaskToBack(false)) {
            super.onBackPressed();
        }
    }
}
