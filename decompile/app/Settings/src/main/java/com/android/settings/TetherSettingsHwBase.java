package com.android.settings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v7.preference.Preference;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import com.android.settings.Utils.ImmersionIcon;
import com.android.settings.wifi.WifiApDialog;
import com.android.settings.wifi.WifiExtUtils;
import java.util.Locale;

public class TetherSettingsHwBase extends RestrictedSettingsFragment {
    protected static final boolean IS_OUTBOARD_SDCARD_AVAILABLE = SystemProperties.getBoolean("ro.config.SupportSdcard", true);
    protected Context mContext;
    protected Preference mCreateNetwork;
    protected String[] mSecurityType;
    protected String[] mUsbRegexs;
    protected TwoStatePreference mUsbTether;
    protected WifiConfiguration mWifiConfig;
    protected WifiManager mWifiManager;
    protected String[] mWifiRegexs;

    public static class HelperActivity extends Activity {
        private WebView mView;

        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (VERSION.SDK_INT >= 22) {
                finish();
                return;
            }
            this.mView = new WebView(this);
            setContentView(this.mView);
            Intent i = getIntent();
            Object url = null;
            if (i != null) {
                url = i.getStringExtra("help_url");
            }
            if (TextUtils.isEmpty(url)) {
                MLog.w("TetherSettingsHwBase", "no content url for helper");
            } else {
                this.mView.loadUrl(url);
            }
        }
    }

    public TetherSettingsHwBase(String restrictionKey) {
        super(restrictionKey);
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (VERSION.SDK_INT >= 22) {
            super.onCreateOptionsMenu(menu, inflater);
            return;
        }
        AlphaStateListDrawable drawable = new AlphaStateListDrawable();
        drawable.addState(new int[0], getResources().getDrawable(Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_HELP)));
        menu.add(0, 2, 0, 2131625452).setIcon(drawable).setShowAsAction(2);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 2:
                if (VERSION.SDK_INT < 22) {
                    ItemUseStat.getInstance().handleClick(this.mContext, 2, "tether_help");
                    showTetherHelp();
                    break;
                }
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    protected void registerWifiApReceiver(BroadcastReceiver tetherChangeReceiver) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_AP_STA_LEAVE");
        filter.addAction("android.net.wifi.WIFI_AP_STA_JOIN");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        this.mContext.registerReceiver(tetherChangeReceiver, filter);
    }

    protected void showTetherHelp() {
        String fileName = "tethering_help.html";
        if (this.mUsbRegexs.length != 0 && this.mWifiRegexs.length == 0) {
            fileName = "tethering_usb_help.html";
        } else if (this.mWifiRegexs.length != 0 && this.mUsbRegexs.length == 0) {
            fileName = "tethering_wifi_help.html";
        }
        if (!IS_OUTBOARD_SDCARD_AVAILABLE && Locale.getDefault().getLanguage().toLowerCase(Locale.US).equals("zh")) {
            if (fileName.contains("usb")) {
                fileName = "tethering_usb_nosdcard_help.html";
            } else {
                fileName = "tethering_nosdcard_help.html";
            }
        }
        String url = Utils.getAssetPath(getActivity(), fileName, true);
        Intent helpIntent = new Intent(getActivity(), HelperActivity.class);
        helpIntent.putExtra("help_url", url);
        startActivity(helpIntent);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 10 && resultCode == -1) {
            this.mWifiConfig = (WifiConfiguration) intent.getParcelableExtra("wifi_config");
            if (this.mWifiConfig != null) {
                if (this.mWifiManager.getWifiApState() == 13) {
                    WifiExtUtils.setWifiApEnabled(this.mContext, this.mWifiManager, null, false);
                    WifiExtUtils.setWifiApEnabled(this.mContext, this.mWifiManager, this.mWifiConfig, true);
                } else {
                    this.mWifiManager.setWifiApConfiguration(this.mWifiConfig);
                }
                int index = WifiApDialog.getSecurityTypeIndex(this.mWifiConfig);
                this.mCreateNetwork.setSummary(String.format(getString(2131625063), new Object[]{this.mWifiConfig.SSID, this.mSecurityType[index]}));
            }
        }
    }

    protected boolean isMassStorageActive() {
        StorageManager storageManager = (StorageManager) getSystemService("storage");
        for (StorageVolume storageVolume : storageManager.getVolumeList()) {
            if (Utils.isVolumeExternalSDcard(this.mContext, storageVolume)) {
                return "shared".equals(storageManager.getVolumeState(storageVolume.getPath()));
            }
        }
        return false;
    }

    protected void updateUsbTetherChargingOnly() {
        if (SystemProperties.get("sys.usb.state").equals("none") && this.mUsbTether != null) {
            this.mUsbTether.setSummary(2131625440);
            this.mUsbTether.setEnabled(false);
        }
    }

    public void onResume() {
        super.onResume();
        updateUsbTetherChargingOnly();
    }

    public void onPause() {
        ItemUseStat.getInstance().cacheData(this.mContext);
        super.onPause();
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
