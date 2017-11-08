package com.android.settings.wifi.ap;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import com.android.settings.CustomSwitchPreference;
import com.android.settings.ItemUseStat;
import com.android.settings.MLog;
import com.android.settings.RadarReporter;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.datausage.DataSaverBackend;
import com.android.settings.datausage.DataSaverBackend.Listener;
import com.android.settings.wifi.WifiApEnabler;
import com.huawei.cust.HwCustUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class WifiApSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Listener, WifiApClientListener {
    private static final int[] USAGE_LIMIT_SIZE = new int[]{-1, 10, 20, 50, 100};
    private CreateBeamUrisCallback beamUrisCallback = new CreateBeamUrisCallback() {
        public Uri[] createBeamUris(NfcEvent event) {
            return new Uri[]{Uri.parse("content://huawei/wificonnect/2")};
        }
    };
    private boolean mApChangedWhenEnabled = false;
    private Preference mCreateNetwork;
    private HwCustWifiApSettings mCust;
    private DataSaverBackend mDataSaverBackend;
    private AlertDialog mDialog;
    private CustomSwitchPreference mEnableWifiAp;
    private boolean mEnabling = false;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (WifiApSettings.this.mEnabling && WifiApSettings.this.mWifiManager != null) {
                        HashMap<Short, Object> map = new HashMap();
                        map.put(Short.valueOf((short) 0), Integer.valueOf(WifiApSettings.this.mWifiManager.getWifiApState()));
                        RadarReporter.reportRadar(907018004, map);
                        return;
                    }
                    return;
                default:
                    MLog.w("WifiApSettings", "Ignore WIFI AP reportRadar, state = " + msg.what);
                    return;
            }
        }
    };
    private CustListPreference mOneUsageLimit;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("com.android.settings.wifi.action.WIFI_AP_CONFIG_CHANGED".equals(intent.getAction())) {
                MLog.d("WifiApSettings", "ACTION_WIFI_AP_CONFIG_CHANGED");
                WifiApSettings.this.mWifiConfig = WifiApSettings.this.mWifiManager.getWifiApConfiguration();
                WifiApSettings.this.updateConfigSummary();
                WifiApSettings.this.updateWifiapHelpContent();
            } else if ("com.android.settings.wifi.action.connected_devices_changed".equals(intent.getAction())) {
                MLog.d("WifiApSettings", "ACTION_CONNECTED_DEVICES_CHANGED");
                WifiApSettings.this.updateWifiapManagerApdevicesSummary();
            } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction())) {
                MLog.d("WifiApSettings", "WIFI_AP_STATE_CHANGED_ACTION");
                WifiApSettings.this.handleWifiApStateChanged(intent.getIntExtra("wifi_state", 14));
            }
        }
    };
    private WifiApEnabler mWifiApEnabler;
    private WifiConfiguration mWifiConfig;
    private ContentObserver mWifiHotspotLimitObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            WifiApSettings.this.updateOneUsageLimitNetherSummary();
        }
    };
    private WifiManager mWifiManager;
    private PreferenceCategory mWifiapHelpCategory;
    private Preference mWifiapHelpContent;
    private Preference mWifiapManagerApdevices;

    private void startStatsUsageObserver() {
        getContentResolver().registerContentObserver(Secure.getUriFor("wifiap_one_usage_stats"), true, this.mWifiHotspotLimitObserver);
    }

    private void stopStatsUsageObserver() {
        getContentResolver().unregisterContentObserver(this.mWifiHotspotLimitObserver);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230936);
        this.mEnableWifiAp = (CustomSwitchPreference) findPreference("enable_wifi_ap");
        this.mWifiapManagerApdevices = findPreference("wifiap_manager_ap_devices");
        this.mWifiapHelpCategory = (PreferenceCategory) findPreference("wifiap_use_help_category");
        this.mWifiapHelpContent = findPreference("wifiap_use_help_content");
        this.mDataSaverBackend = new DataSaverBackend(getActivity());
        this.mDataSaverBackend.addListener(this);
        this.mWifiApEnabler = new WifiApEnabler(getActivity(), this.mDataSaverBackend, this.mEnableWifiAp);
        this.mCust = (HwCustWifiApSettings) HwCustUtils.createObj(HwCustWifiApSettings.class, new Object[]{this});
        initWifiTethering();
        initOneUsageLimit();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private boolean needShowLimitHelp() {
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        if (sp != null) {
            return sp.getBoolean("wifiap_donot_prompt_limit_help", true);
        }
        return true;
    }

    private void enableLimitHelp(boolean enable) {
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        if (sp != null) {
            Editor spEdit = sp.edit();
            spEdit.putBoolean("wifiap_donot_prompt_limit_help", enable);
            try {
                spEdit.apply();
            } catch (Exception e) {
                e.printStackTrace();
                spEdit.commit();
            }
        }
    }

    private void initOneUsageLimit() {
        this.mOneUsageLimit = (CustListPreference) findPreference("wifiap_one_usage_limit");
        if (this.mOneUsageLimit != null) {
            int i;
            if (!Utils.isCheckAppExist(getActivity(), "com.huawei.systemmanager")) {
                PreferenceCategory cotogery = (PreferenceCategory) findPreference("wifiap_settings");
                if (cotogery != null) {
                    cotogery.removePreference(this.mOneUsageLimit);
                }
            }
            CharSequence[] entryValues = new CharSequence[(USAGE_LIMIT_SIZE.length + 1)];
            for (i = 0; i < entryValues.length; i++) {
                entryValues[i] = String.valueOf(i);
            }
            CharSequence[] entries = new CharSequence[(USAGE_LIMIT_SIZE.length + 1)];
            entries[0] = getResources().getString(2131627729);
            for (i = 1; i < USAGE_LIMIT_SIZE.length; i++) {
                long size = (long) USAGE_LIMIT_SIZE[i];
                entries[i] = getString(2131628419, new Object[]{Long.valueOf(size), getString(17039499)});
            }
            entries[entries.length - 1] = getResources().getString(2131627730);
            this.mOneUsageLimit.setEntryValues(entryValues);
            this.mOneUsageLimit.setEntries(entries);
            updateOneUsageLimitSummary();
            this.mOneUsageLimit.setOnPreferenceChangeListener(this);
        }
    }

    private void updateOneUsageLimitSummary() {
        if (getActivity() == null) {
            Log.e("WifiApSettings", "updateOneUsageLimitSummary failed as activity is null");
        } else if (this.mOneUsageLimit != null) {
            CharSequence[] entryValues = this.mOneUsageLimit.getEntryValues();
            CharSequence[] entries = this.mOneUsageLimit.getEntries();
            int index = Arrays.binarySearch(USAGE_LIMIT_SIZE, Secure.getInt(getContentResolver(), "wifiap_one_usage_limit", -1));
            if (index < 0) {
                this.mOneUsageLimit.setValue(String.valueOf(entryValues.length - 1));
                this.mOneUsageLimit.setSummary(getString(2131628419, new Object[]{Integer.valueOf(wifiHotspotLimit), getString(17039499)}));
            } else {
                this.mOneUsageLimit.setValue(String.valueOf(index));
                this.mOneUsageLimit.setSummary(entries[index]);
            }
        }
    }

    private void initWifiTethering() {
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        this.mWifiConfig = this.mWifiManager.getWifiApConfiguration();
        if (this.mCust != null) {
            this.mCust.custWifiConfiguration(this.mWifiConfig);
        }
        this.mCreateNetwork = findPreference("wifi_ap_ssid_and_security");
        updateConfigSummary();
    }

    public void onStart() {
        super.onStart();
        this.mEnableWifiAp.setOnPreferenceChangeListener(this);
        this.mWifiApEnabler.resume();
        IntentFilter f = new IntentFilter("com.android.settings.wifi.action.WIFI_AP_CONFIG_CHANGED");
        f.addAction("com.android.settings.wifi.action.connected_devices_changed");
        f.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        getActivity().registerReceiver(this.mReceiver, f);
        if (this.mCust != null) {
            this.mCust.registerReceiverForWps();
        }
        updateWifiapManagerApdevicesSummary();
        if (UserHandle.getCallingUserId() == 0) {
            NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
            if (mNfcAdapter != null) {
                mNfcAdapter.setBeamPushUrisCallback(this.beamUrisCallback, getActivity());
            }
        } else {
            MLog.d("WifiApSettings", "/ WifiApSettings:The sub UserId = " + UserHandle.getCallingUserId());
        }
        updateOneUsageLimitNetherSummary();
        updateWifiapHelpContent();
        startStatsUsageObserver();
        onDataSaverChanged(this.mDataSaverBackend.isDataSaverEnabled());
    }

    public void onStop() {
        super.onStop();
        ItemUseStat.getInstance().cacheData(getActivity());
        this.mEnableWifiAp.setOnPreferenceChangeListener(null);
        this.mWifiApEnabler.pause();
        getActivity().unregisterReceiver(this.mReceiver);
        if (this.mCust != null) {
            this.mCust.unregisterReceiverForWps();
        }
        stopStatsUsageObserver();
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
        if (preference == this.mCreateNetwork) {
            startWifiApConfig();
        } else if (preference == this.mOneUsageLimit) {
            if (this.mDialog != null && this.mDialog.isShowing()) {
                return true;
            }
            if (needShowLimitHelp()) {
                showLimitHelpDialog();
            } else {
                this.mOneUsageLimit.showDialog();
            }
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference != this.mOneUsageLimit || this.mOneUsageLimit == null) {
            if (preference == this.mEnableWifiAp) {
                ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, value);
                boolean enable = ((Boolean) value).booleanValue();
                if (this.mCust != null && this.mCust.showCustWifiApDialog(this.mWifiApEnabler, enable)) {
                    return false;
                }
                if (enable && this.mDataSaverBackend.isDataSaverEnabled()) {
                    showDisableDataSaverWarning(enable);
                } else {
                    handleWifiApClick(enable);
                }
            }
            return false;
        }
        int index = Integer.parseInt(value.toString());
        ItemUseStat.getInstance().handleClickListPreference(getActivity(), this.mOneUsageLimit, ItemUseStat.KEY_LISTPREFERENCE_ONE_USAGE_LIMIT, (String) value);
        if (index == USAGE_LIMIT_SIZE.length) {
            showCustLimitDialog();
        } else {
            Secure.putInt(getContentResolver(), "wifiap_one_usage_limit", USAGE_LIMIT_SIZE[index]);
            this.mOneUsageLimit.setSummary(this.mOneUsageLimit.getEntries()[index]);
        }
        return true;
    }

    private void showDisableDataSaverWarning(final boolean enable) {
        new Builder(getActivity()).setTitle(2131628959).setMessage(2131628960).setNegativeButton(2131628961, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setPositiveButton(2131628962, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                WifiApSettings.this.handleWifiApClick(enable);
            }
        }).create().show();
    }

    private void handleWifiApClick(boolean enable) {
        if (enable) {
            if (this.mCust != null) {
                this.mCust.showWifiApNotification();
            }
            String[] appDetails = getResources().getStringArray(17235992);
            if (appDetails.length != 2) {
                Log.i("WifiApSettings", "appDetails.length");
                this.mWifiApEnabler.setSoftapEnabled(true);
                startCount();
                return;
            }
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setClassName(appDetails[0], appDetails[1]);
            PackageManager packageManager = getPackageManager();
            if (packageManager == null || SystemProperties.getBoolean("net.tethering.noprovisioning", false) || packageManager.queryIntentActivities(intent, 65536).size() <= 0) {
                Log.i("WifiApSettings", "!appDetails.length ");
                this.mWifiApEnabler.setSoftapEnabled(true);
                startCount();
                return;
            }
            Log.i("WifiApSettings", "PackageManager.MATCH_DEFAULT_ONLY");
            intent.putExtra("TETHER_TYPE", 0);
            startActivityForResult(intent, 0);
            MLog.w("WifiApSettings", "Show hotspot provisioning first,see commit for config_mobile_hotspot_provision_app");
            return;
        }
        Log.i("WifiApSettings", "mEnableWifiAp+enable");
        this.mWifiApEnabler.setSoftapEnabled(false);
        stopCount();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 0) {
            if (resultCode == -1) {
                this.mWifiApEnabler.setSoftapEnabled(true);
            }
        } else if (requestCode == 10 && resultCode == -1) {
            int channel = intent.getIntExtra("wifi_ap_channel", -1);
            int maxConnections = intent.getIntExtra("wifi_ap_max_connections", -1);
            if (channel != -1) {
                setChannel(channel);
            }
            if (maxConnections != -1) {
                setMaxConnections(maxConnections);
            }
            this.mWifiConfig = (WifiConfiguration) intent.getParcelableExtra("wifi_config");
            if (this.mWifiConfig != null) {
                if (this.mCust != null) {
                    this.mCust.compareWithLastWifiApConfig(this.mWifiConfig);
                }
                if (this.mWifiManager.getWifiApState() == 13) {
                    this.mWifiManager.setWifiApConfiguration(this.mWifiConfig);
                    this.mWifiApEnabler.setSoftapEnabled(false);
                    this.mApChangedWhenEnabled = true;
                } else {
                    this.mWifiManager.setWifiApConfiguration(this.mWifiConfig);
                }
                updateConfigSummary();
            }
        } else if (requestCode == 10 && resultCode == 0) {
            initWifiTethering();
        }
    }

    private void setChannel(int channel) {
        Secure.putInt(getContentResolver(), "wifi_ap_channel", channel);
    }

    private void setMaxConnections(int maxConenctions) {
        Secure.putInt(getContentResolver(), "wifi_ap_maxscb", maxConenctions);
    }

    private void updateConfigSummary() {
        String apName = "";
        if (this.mWifiConfig == null) {
            apName = getString(17040332);
        } else {
            apName = this.mWifiConfig.SSID;
        }
        this.mEnableWifiAp.setTitle((CharSequence) apName);
    }

    public void onDeviceDisconnected() {
        updateWifiapManagerApdevicesSummary();
    }

    public void onDeviceRemoved() {
        updateWifiapManagerApdevicesSummary();
    }

    private void startWifiApConfig() {
        Intent intent = new Intent(getActivity(), WifiApDialogActivity.class);
        intent.putExtra("wifi_config", this.mWifiConfig);
        startActivityForResult(intent, 10);
    }

    private void showLimitHelpDialog() {
        dismissDialog();
        View view = LayoutInflater.from(getActivity()).inflate(2130969264, null);
        ((CheckBox) view.findViewById(2131886216)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                WifiApSettings.this.enableLimitHelp(!isChecked);
            }
        });
        AlertDialog dialog = new Builder(getActivity()).setView(view).setTitle(2131627727).setPositiveButton(2131625656, null).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                WifiApSettings.this.mOneUsageLimit.showDialog();
            }
        }).create();
        dialog.show();
        this.mDialog = dialog;
    }

    private void dismissDialog() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mDataSaverBackend.remListener(this);
        dismissDialog();
    }

    private void showCustLimitDialog() {
        dismissDialog();
        View view = LayoutInflater.from(getActivity()).inflate(2130969265, null);
        final EditText limitedEditText = (EditText) view.findViewById(2131887475);
        int limit = Secure.getInt(getContentResolver(), "wifiap_one_usage_limit", -1);
        String limitText = "";
        if (limit == -1) {
            limitText = "";
        } else {
            limitText = limit + "";
        }
        limitedEditText.setText(limitText);
        limitedEditText.setSelection(limitedEditText.getText().length());
        AlertDialog dialog = new Builder(getActivity()).setView(view).setTitle(2131627730).setNegativeButton(2131625657, null).setPositiveButton(2131625656, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Secure.putInt(WifiApSettings.this.getContentResolver(), "wifiap_one_usage_limit", Integer.parseInt(limitedEditText.getText().toString()));
            }
        }).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                WifiApSettings.this.updateOneUsageLimitSummary();
            }
        }).create();
        TextWatcher mLimitedEdiTextTextWatcher = new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                boolean z = false;
                if (WifiApSettings.this.mDialog != null) {
                    int value = 0;
                    try {
                        value = Integer.parseInt(s.toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    Button button = WifiApSettings.this.mDialog.getButton(-1);
                    if (s.length() > 0 && value > 0) {
                        z = true;
                    }
                    button.setEnabled(z);
                }
            }
        };
        dialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialog) {
                boolean z = false;
                Button button = ((AlertDialog) dialog).getButton(-1);
                if (limitedEditText.getText().length() > 0) {
                    z = true;
                }
                button.setEnabled(z);
            }
        });
        limitedEditText.addTextChangedListener(mLimitedEdiTextTextWatcher);
        dialog.getWindow().setSoftInputMode(5);
        dialog.show();
        this.mDialog = dialog;
    }

    private void updateOneUsageLimitNetherSummary() {
        if (isAdded() && this.mOneUsageLimit != null && this.mWifiManager != null) {
            long stats;
            if (this.mWifiManager.isWifiApEnabled()) {
                stats = Secure.getLong(getContentResolver(), "wifiap_one_usage_stats", 0);
                this.mOneUsageLimit.setNetherSummary(getString(2131627728, new Object[]{Formatter.formatFileSize(getActivity(), stats)}));
            } else {
                stats = Secure.getLong(getContentResolver(), "wifiap_one_usage_stats", 0);
                int wifiHotspotLimit = Secure.getInt(getContentResolver(), "wifiap_one_usage_limit", -1);
                long wifiHotspotLimitLong = ((long) wifiHotspotLimit) * 1048576;
                if (wifiHotspotLimit == -1 || stats < wifiHotspotLimitLong) {
                    this.mOneUsageLimit.setNetherSummary(getString(2131627747, new Object[]{Formatter.formatFileSize(getActivity(), stats)}));
                } else {
                    this.mOneUsageLimit.setNetherSummary(getString(2131627746));
                }
            }
        }
    }

    private void updateWifiapManagerApdevicesSummary() {
        if (this.mWifiapManagerApdevices != null && this.mWifiManager != null) {
            if (this.mWifiManager.isWifiApEnabled()) {
                List<WifiApClientInfo> list = WifiApClientUtils.getInstance(getActivity()).getConnectedList();
                int count = 0;
                if (list != null) {
                    count = list.size();
                }
                this.mWifiapManagerApdevices.setSummary(getResources().getQuantityString(2131689522, count, new Object[]{Integer.valueOf(count)}));
            } else {
                this.mWifiapManagerApdevices.setSummary(getResources().getQuantityString(2131689522, 0, new Object[]{Integer.valueOf(0)}));
            }
        }
    }

    private void setHelpContentOnOpen(Preference wifiapHelpContent, String name) {
        CharSequence summary = getResources().getString(2131628877, new Object[]{name, Integer.valueOf(1), Integer.valueOf(2)});
        if (!TextUtils.isEmpty(summary)) {
            wifiapHelpContent.setSummary(summary);
        }
    }

    private void setHelpContentOnClose(Preference wifiapHelpContent) {
        CharSequence summary = String.format(getResources().getString(2131628876, new Object[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)}), new Object[0]);
        if (!TextUtils.isEmpty(summary)) {
            wifiapHelpContent.setSummary(summary);
        }
    }

    private void updateWifiapHelpContent() {
        if (this.mWifiapHelpContent != null && this.mWifiapHelpCategory != null && this.mWifiManager != null) {
            if (!this.mWifiManager.isWifiApEnabled() || this.mWifiConfig == null) {
                this.mWifiapHelpCategory.setTitle(2131627733);
                setHelpContentOnClose(this.mWifiapHelpContent);
            } else {
                this.mWifiapHelpCategory.setTitle(2131627735);
                String name = this.mWifiConfig.SSID;
                if (TextUtils.isEmpty(name)) {
                    MLog.w("WifiApSettings", "Error!WifiConfig is not null, SSID is null, this shouldn't happen!");
                    getString(17040332);
                }
                setHelpContentOnOpen(this.mWifiapHelpContent, name);
            }
        }
    }

    private void handleWifiApStateChanged(int state) {
        MLog.d("WifiApSettings", "Handle WIFI AP state = " + state);
        switch (state) {
            case 11:
                MLog.d("WifiApSettings", "Handle WIFI_AP_STATE_DISABLED.");
                handleApDisabled();
                if (this.mApChangedWhenEnabled && this.mWifiApEnabler != null) {
                    Log.d("WifiApSettings", "Re-Enable wifi ap.");
                    this.mWifiApEnabler.setSoftapEnabled(true);
                    this.mApChangedWhenEnabled = false;
                }
                stopCount();
                break;
            case 13:
                MLog.d("WifiApSettings", "Handle WIFI_AP_STATE_ENABLED.");
                handleApEnabled();
                stopCount();
                break;
            default:
                MLog.w("WifiApSettings", "Ignore WIFI AP state change, state = " + state);
                break;
        }
        MLog.d("WifiApSettings", "Handle WIFI AP state change finish. state = " + state);
    }

    private void handleApEnabled() {
        updateWifiapHelpContent();
        if (this.mWifiapManagerApdevices != null && this.mWifiManager != null) {
            if (this.mWifiManager.isWifiApEnabled()) {
                new AsyncTask<Context, Object, Object>() {
                    protected Object doInBackground(Context... params) {
                        int i = 0;
                        MLog.d("WifiApSettings", "doInBackground fetch connected WIFI AP devices start...");
                        List<WifiApClientInfo> list = WifiApClientUtils.getInstance(params[0]).getConnectedList();
                        MLog.d("WifiApSettings", "doInBackground fetch connected WIFI AP devices finish");
                        if (list != null) {
                            i = list.size();
                        }
                        return Integer.valueOf(i);
                    }

                    protected void onPostExecute(Object result) {
                        if (WifiApSettings.this.isAdded()) {
                            CharSequence summary = WifiApSettings.this.getResources().getQuantityString(2131689522, result == null ? 0 : ((Integer) result).intValue(), new Object[]{Integer.valueOf(result == null ? 0 : ((Integer) result).intValue())});
                            if (WifiApSettings.this.mWifiapManagerApdevices != null) {
                                WifiApSettings.this.mWifiapManagerApdevices.setSummary(summary);
                            }
                            MLog.d("WifiApSettings", "Update WIFI AP devices summary finish!");
                        }
                    }
                }.execute(new Context[]{getActivity()});
            } else {
                this.mWifiapManagerApdevices.setSummary(getResources().getQuantityString(2131689522, 0, new Object[]{Integer.valueOf(0)}));
            }
        }
    }

    private void handleApDisabled() {
        updateOneUsageLimitNetherSummary();
        updateWifiapHelpContent();
        updateWifiapManagerApdevicesSummary();
    }

    private void startCount() {
        this.mEnabling = true;
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 1), 10000);
    }

    private void stopCount() {
        this.mEnabling = false;
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        }
    }

    public void onDataSaverChanged(boolean isDataSaving) {
        boolean isAirplaneMode;
        if (Global.getInt(getActivity().getContentResolver(), "airplane_mode_on", 0) != 0) {
            isAirplaneMode = true;
        } else {
            isAirplaneMode = false;
        }
        if (isAirplaneMode) {
            this.mEnableWifiAp.setEnabled(false);
        } else {
            this.mEnableWifiAp.setEnabled(true);
        }
    }

    public void onWhitelistStatusChanged(int uid, boolean isWhitelisted) {
    }

    public void onBlacklistStatusChanged(int uid, boolean isBlacklisted) {
    }

    protected int getMetricsCategory() {
        return 100000;
    }
}
