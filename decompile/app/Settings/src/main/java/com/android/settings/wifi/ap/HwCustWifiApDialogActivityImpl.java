package com.android.settings.wifi.ap;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.INetworkManagementService.Stub;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import com.huawei.android.provider.SettingsEx.Systemex;

public class HwCustWifiApDialogActivityImpl extends HwCustWifiApDialogActivity implements OnItemSelectedListener {
    private static final String BROADCAST_SSID_ALERT = "hw_show_checkbox_braodcast_ssid_msg";
    private static final int HOTSPOT_ALWAYS_ON = 2;
    private static final String HOTSPOT_ALWAYS_ON_VALUE = "always_on";
    private static final int HOTSPOT_DISABLE_AFTER_10_MINS = 1;
    private static final String HOTSPOT_DISABLE_AFTER_10_MINS_VALUE = "after_10_mins";
    private static final int HOTSPOT_DISABLE_AFTER_5_MINS = 0;
    private static final String HOTSPOT_DISABLE_AFTER_5_MINS_VALUE = "after_5_mins";
    private static final String HOTSPOT_POWER_MODE = "hotspot_power_mode";
    private static final boolean HOTSPOT_POWER_MODE_ON = SystemProperties.getBoolean("ro.config.hotspot_power_mode_on", false);
    protected static final boolean HWFLOW;
    private static final int SOFTAP_2G = 1;
    private static final int SOFTAP_2G_AND_5G = 2;
    private static final int SOFTAP_DEFAULT = 0;
    private static final String TAG = "HwCustWifiApDialog";
    private String WIFI_AP_IGNOREBROADCASTSSID = "wifi_ap_ignorebroadcastssid";
    private Switch mBroadcastNetworkName;
    private String mInterfaceName = SystemProperties.get("wifi.interface", "wlan0");
    private int mPowerMode;
    private Button mSaveBtn;
    private int mWifiApBand = 0;
    private WifiApDialogActivity mWifiApDialogActivity;
    private LinearLayout mWifiApDialogLayout;
    private Spinner mWifiHotspotPowerMode;
    private int oldPowerMode;

    class BroadcastSSIDCheckedChangeListener implements OnCheckedChangeListener {
        BroadcastSSIDCheckedChangeListener() {
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int i;
            ContentResolver contentResolver = HwCustWifiApDialogActivityImpl.this.mWifiApDialogActivity.getContentResolver();
            String -get0 = HwCustWifiApDialogActivityImpl.this.WIFI_AP_IGNOREBROADCASTSSID;
            if (isChecked) {
                i = 0;
            } else {
                i = 1;
            }
            Secure.putInt(contentResolver, -get0, i);
            if (!isChecked && System.getInt(HwCustWifiApDialogActivityImpl.this.mWifiApDialogActivity.getContentResolver(), HwCustWifiApDialogActivityImpl.BROADCAST_SSID_ALERT, 1) == 1) {
                getAlertDialog(HwCustWifiApDialogActivityImpl.this.mWifiApDialogActivity).show();
            }
        }

        private Dialog getAlertDialog(Context context) {
            Builder builder = new Builder(context);
            View view = LayoutInflater.from(builder.getContext()).inflate(2130968660, null, false);
            CheckBox mNoAlert = (CheckBox) view.findViewById(2131886322);
            builder.setTitle(2131629160);
            builder.setIconAttribute(16843605);
            builder.setView(view);
            mNoAlert.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    System.putInt(HwCustWifiApDialogActivityImpl.this.mWifiApDialogActivity.getContentResolver(), HwCustWifiApDialogActivityImpl.BROADCAST_SSID_ALERT, isChecked ? 0 : 1);
                }
            });
            builder.setPositiveButton(2131629161, null);
            Dialog mDialog = builder.create();
            mDialog.setCanceledOnTouchOutside(false);
            return mDialog;
        }
    }

    static {
        boolean z = false;
        if (Log.HWINFO) {
            z = true;
        } else if (Log.HWModuleLog) {
            z = Log.isLoggable(TAG, 4);
        }
        HWFLOW = z;
    }

    public HwCustWifiApDialogActivityImpl(WifiApDialogActivity wifiApDialogActivity) {
        super(wifiApDialogActivity);
        this.mWifiApDialogActivity = wifiApDialogActivity;
    }

    public void onCustCreate() {
        Log.d(TAG, "HOTSPOT_POWER_MODE_ON :" + HOTSPOT_POWER_MODE_ON);
        if (HOTSPOT_POWER_MODE_ON) {
            addPowerModeView();
        }
        addBroadcardSSID();
        custSecuritySpinner();
    }

    public void onCustClick(View view) {
        this.mSaveBtn = (Button) this.mWifiApDialogActivity.findViewById(2131887474);
        if (view == this.mSaveBtn && this.oldPowerMode != this.mPowerMode && this.mWifiHotspotPowerMode != null) {
            Secure.putInt(this.mWifiApDialogActivity.getContentResolver(), HOTSPOT_POWER_MODE, this.mPowerMode);
        }
    }

    public int getWifiApBand() {
        return SystemProperties.getInt("ro.config.hw_wifi_ap_band", 0);
    }

    public boolean isSetWifiApBand2G() {
        this.mWifiApBand = getWifiApBand();
        if (HWFLOW) {
            Log.i(TAG, "mWifiApBand: " + this.mWifiApBand);
        }
        if (this.mWifiApBand != 1) {
            return false;
        }
        if (HWFLOW) {
            Log.i(TAG, "open 2GHz");
        }
        return true;
    }

    public boolean isSetWifiApBand5G(String countryCode) {
        this.mWifiApBand = getWifiApBand();
        if (HWFLOW) {
            Log.i(TAG, "countryCode: " + countryCode);
        }
        if (this.mWifiApBand != 2 || countryCode == null) {
            return false;
        }
        if (HWFLOW) {
            Log.i(TAG, "open 5GHz");
        }
        return true;
    }

    private void addPowerModeView() {
        LayoutInflater layoutInflater = LayoutInflater.from(this.mWifiApDialogActivity);
        this.mWifiApDialogLayout = (LinearLayout) this.mWifiApDialogActivity.findViewById(2131887458);
        this.mWifiApDialogLayout.addView(layoutInflater.inflate(2130969266, this.mWifiApDialogLayout, false));
        this.mWifiHotspotPowerMode = (Spinner) this.mWifiApDialogActivity.findViewById(2131887477);
        this.mPowerMode = Secure.getInt(this.mWifiApDialogActivity.getContentResolver(), HOTSPOT_POWER_MODE, 1);
        this.oldPowerMode = this.mPowerMode;
        this.mWifiHotspotPowerMode.setSelection(this.mPowerMode);
        this.mWifiHotspotPowerMode.setOnItemSelectedListener(this);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (this.mWifiHotspotPowerMode == parent) {
            this.mPowerMode = position;
        }
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    private boolean isCustSecurity() {
        return Systemex.getInt(this.mWifiApDialogActivity.getContentResolver(), "show_security_level_high_to_low", 0) == 1;
    }

    private boolean isShowBroadcast() {
        return Systemex.getInt(this.mWifiApDialogActivity.getContentResolver(), "show_broadcast_ssid_config", 0) == 1;
    }

    public void custSecuritySpinner() {
        if (isCustSecurity()) {
            this.mWifiApDialogActivity.setOpenIndexValue(1);
            this.mWifiApDialogActivity.setWap2IndexValue(0);
            Spinner securitySpinner = (Spinner) this.mWifiApDialogActivity.findViewById(2131887460);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.mWifiApDialogActivity, 2131362014, 17367048);
            adapter.setDropDownViewResource(17367049);
            securitySpinner.setAdapter(adapter);
            securitySpinner.setSelection(this.mWifiApDialogActivity.getSecurityTypeIndex() == this.mWifiApDialogActivity.getOpenIndexValue() ? this.mWifiApDialogActivity.getWap2IndexValue() : this.mWifiApDialogActivity.getOpenIndexValue());
            if (this.mWifiApDialogActivity.getSecurityTypeIndex() == this.mWifiApDialogActivity.getOpenIndexValue()) {
                CharSequence charSequence;
                EditText editText = (EditText) this.mWifiApDialogActivity.findViewById(2131887420);
                if (this.mWifiApDialogActivity.getWifiConfig() != null) {
                    charSequence = this.mWifiApDialogActivity.getWifiConfig().preSharedKey;
                } else {
                    charSequence = "";
                }
                editText.setText(charSequence);
            }
        }
    }

    public void custConfig(WifiConfiguration config) {
        if (isShowBroadcast()) {
            try {
                Stub.asInterface(ServiceManager.getService("network_management")).setAccessPoint(config, this.mInterfaceName);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void addBroadcardSSID() {
        if (isShowBroadcast()) {
            LinearLayout mApLayout = (LinearLayout) this.mWifiApDialogActivity.findViewById(2131887458);
            View broadcastView = LayoutInflater.from(this.mWifiApDialogActivity).inflate(2130969260, mApLayout, false);
            mApLayout.addView(broadcastView, 3);
            broadcastView.setVisibility(0);
            this.mBroadcastNetworkName = (Switch) this.mWifiApDialogActivity.findViewById(2131887454);
            if (this.mBroadcastNetworkName != null) {
                boolean z;
                int iIgnorebroadcastssid = Secure.getInt(this.mWifiApDialogActivity.getContentResolver(), this.WIFI_AP_IGNOREBROADCASTSSID, 0);
                Switch switchR = this.mBroadcastNetworkName;
                if (iIgnorebroadcastssid == 0) {
                    z = true;
                } else {
                    z = false;
                }
                switchR.setChecked(z);
                this.mBroadcastNetworkName.setOnCheckedChangeListener(new BroadcastSSIDCheckedChangeListener());
            }
        }
    }
}
