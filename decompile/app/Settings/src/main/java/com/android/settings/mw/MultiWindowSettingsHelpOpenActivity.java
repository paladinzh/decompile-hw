package com.android.settings.mw;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import java.util.ArrayList;
import java.util.List;

public class MultiWindowSettingsHelpOpenActivity extends Activity implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            String screenTitle = context.getResources().getString(2131627926);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.intentAction = "com.huawei.multiwindow.HELPOPEN";
            data.intentTargetPackage = "com.android.settings";
            data.intentTargetClass = "com.android.settings.mw.MultiWindowSettingsHelpOpenActivity";
            result.add(data);
            return result;
        }
    };
    private LinearLayout mButtonTextView;
    private BroadcastReceiver mMWToolboxReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent in) {
            if ("com.android.huawei.MW_ACTION_SETTING_CHANGED".equals(in.getAction())) {
                MultiWindowSettingsHelpOpenActivity.this.updateState();
            }
        }
    };
    private MwNavigationSpotsView mNavigationSpotsView;
    private boolean mRegistered = false;
    private Activity mSelf;
    private MwSettingPageViewAdaptor mSettingPageAdaptor;
    private ViewPager mSetttingPager;
    private Switch mSwitch;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130968874);
        this.mSetttingPager = (ViewPager) findViewById(2131886805);
        this.mNavigationSpotsView = (MwNavigationSpotsView) findViewById(2131886711);
        this.mSettingPageAdaptor = new MwSettingPageViewAdaptor(this);
        this.mSetttingPager.setOnPageChangeListener(new MwSettingPageChangeListener(this.mNavigationSpotsView));
        this.mSetttingPager.setAdapter(this.mSettingPageAdaptor);
        this.mSelf = this;
        this.mButtonTextView = (LinearLayout) findViewById(2131886335);
        this.mButtonTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!MultiWindowSettingsHelpOpenActivity.this.isTalkBackServicesOn(MultiWindowSettingsHelpOpenActivity.this.mSelf)) {
                    MultiWindowSettingsHelpOpenActivity.this.mSwitch.toggle();
                }
            }
        });
        this.mSwitch = (Switch) findViewById(2131886807);
        this.mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                System.putIntForUser(MultiWindowSettingsHelpOpenActivity.this.mSelf.getContentResolver(), "multiwindow_mode_settings", isChecked ? 1 : 0, -2);
            }
        });
        if (Utils.isSimpleModeOn()) {
            this.mButtonTextView.setEnabled(false);
            ((TextView) findViewById(2131886806)).setEnabled(false);
            this.mSwitch.setEnabled(false);
        }
    }

    protected void onResume() {
        super.onResume();
        if (!(this.mSelf == null || this.mSwitch == null)) {
            if (isTalkBackServicesOn(this.mSelf)) {
                this.mSwitch.setEnabled(false);
                Toast.makeText(this, getResources().getString(2131627928), 1).show();
            } else if (!Utils.isSimpleModeOn()) {
                this.mSwitch.setEnabled(true);
            }
            updateState();
        }
        if (!this.mRegistered) {
            this.mRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.android.huawei.MW_ACTION_SETTING_CHANGED");
            registerReceiver(this.mMWToolboxReceiver, filter);
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.mRegistered) {
            unregisterReceiver(this.mMWToolboxReceiver);
            this.mRegistered = false;
        }
    }

    private void updateState() {
        boolean z = true;
        if (this.mSelf != null && this.mSwitch != null) {
            int mwState = System.getIntForUser(this.mSelf.getContentResolver(), "multiwindow_mode_settings", 1, -2);
            Switch switchR = this.mSwitch;
            if (mwState != 1) {
                z = false;
            }
            switchR.setChecked(z);
        }
    }

    private boolean isTalkBackServicesOn(Context aContext) {
        boolean lAccessibilityEnabled = true;
        if (aContext == null) {
            return false;
        }
        if (Secure.getInt(aContext.getContentResolver(), "accessibility_enabled", 0) != 1) {
            lAccessibilityEnabled = false;
        }
        if (!lAccessibilityEnabled) {
            return false;
        }
        String lEnabledServices = Secure.getString(aContext.getContentResolver(), "enabled_accessibility_services");
        if (lEnabledServices == null) {
            return false;
        }
        return lEnabledServices.contains(Utils.sTalkBackComponent.flattenToString());
    }
}
