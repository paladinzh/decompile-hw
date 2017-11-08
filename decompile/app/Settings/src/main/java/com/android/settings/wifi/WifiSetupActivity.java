package com.android.settings.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.ButtonBarHandler;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;

public class WifiSetupActivity extends WifiPickerActivity implements ButtonBarHandler {
    private boolean mAutoFinishOnConnection;
    private IntentFilter mFilter = new IntentFilter();
    private boolean mIsNetworkRequired;
    private boolean mIsWifiRequired;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            WifiSetupActivity.this.refreshConnectionState();
            WifiSetupActivity.this.switchCustomButtonText();
        }
    };
    private boolean mUserSelectedNetwork;

    protected void onCreate(Bundle savedInstanceState) {
        boolean z = false;
        super.onCreate(savedInstanceState);
        hideNavigationBar(getWindow());
        final LinearLayout parentView = (LinearLayout) findViewById(2131887012);
        parentView.setFitsSystemWindows(true);
        parentView.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                parentView.setPadding(0, insets.getSystemWindowInsetTop(), 0, 0);
                return insets;
            }
        });
        if (SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
                public void onSystemUiVisibilityChange(int visibility) {
                    WifiSetupActivity.hideNavigationBar(WifiSetupActivity.this.getWindow());
                    Log.i("WifiSetupActivity", "onSystemUiVisibilityChange");
                }
            });
        }
        Intent intent = getIntent();
        this.mFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mAutoFinishOnConnection = intent.getBooleanExtra("wifi_auto_finish_on_connect", false);
        this.mIsNetworkRequired = intent.getBooleanExtra("is_network_required", false);
        this.mIsWifiRequired = intent.getBooleanExtra("is_wifi_required", false);
        if (!intent.getBooleanExtra("wifi_require_user_network_selection", false)) {
            z = true;
        }
        this.mUserSelectedNetwork = z;
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("userSelectedNetwork", this.mUserSelectedNetwork);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mUserSelectedNetwork = savedInstanceState.getBoolean("userSelectedNetwork", true);
    }

    private boolean isWifiConnected() {
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService("connectivity");
        if (connectivity != null) {
            return connectivity.getNetworkInfo(1).isConnected();
        }
        return false;
    }

    private void refreshConnectionState() {
        if (isWifiConnected() && this.mAutoFinishOnConnection && this.mUserSelectedNetwork) {
            Log.d("WifiSetupActivity", "Auto-finishing with connection");
            finish(-1);
            this.mUserSelectedNetwork = false;
        }
    }

    void networkSelected() {
        Log.d("WifiSetupActivity", "Network selected by user");
        this.mUserSelectedNetwork = true;
    }

    public void onResume() {
        super.onResume();
        if (!(isSupportOrientation() || Utils.isTablet())) {
            setRequestedOrientation(1);
        }
        hideNavigationBar(getWindow());
        registerReceiver(this.mReceiver, this.mFilter);
        refreshConnectionState();
        switchCustomButtonText();
    }

    public void onPause() {
        unregisterReceiver(this.mReceiver);
        super.onPause();
    }

    protected boolean isValidFragment(String fragmentName) {
        return WifiSettingsForSetupWizard.class.getName().equals(fragmentName);
    }

    Class<? extends PreferenceFragment> getWifiSettingsClass() {
        return WifiSettingsForSetupWizard.class;
    }

    public void finish(int resultCode) {
        Log.d("WifiSetupActivity", "finishing, resultCode=" + resultCode);
        setResult(resultCode);
        finish();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideNavigationBar(getWindow());
    }

    public static void hideNavigationBar(Window window) {
        if (window != null && 5890 != window.getDecorView().getSystemUiVisibility()) {
            window.getDecorView().setSystemUiVisibility(5890);
        }
    }

    private boolean isSupportOrientation() {
        return getResources().getBoolean(2131492920);
    }

    private void switchCustomButtonText() {
        TextView customButton = (TextView) findViewById(2131886329);
        if (customButton == null) {
            return;
        }
        if (isWifiConnected()) {
            customButton.setText(2131626195);
        } else {
            customButton.setText(2131626194);
        }
    }
}
