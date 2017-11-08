package com.android.settings.wifi;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.ActionListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceFrameLayout;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import com.android.settings.ItemUseStat;
import com.android.settings.LinkifyUtils;
import com.android.settings.ListSpinner;
import com.android.settings.SettingsActivity;
import com.android.settingslib.wifi.AccessPoint;
import com.huawei.android.net.wifi.WifiManagerEx;
import com.huawei.cust.HwCustUtils;
import java.util.List;

public class WifiAddFragment extends Fragment implements WifiConfigUiBase, OnClickListener {
    private String bssid;
    private boolean isHiLinkNetwork;
    private AccessPoint mAccessPoint;
    private Button mCancelButton;
    private WifiConfiguration mConfig;
    private ActionListener mConnectListener;
    private boolean mConnectionHappened;
    private long mConnectionStartTime;
    private Context mContext;
    private WifiConfigController mController;
    private int mCurrentDialogId = -1;
    private String mCurrentSSID;
    private Dialog mDialog;
    private final IntentFilter mFilter = new IntentFilter();
    private Button mForgetButton;
    private ActionListener mForgetListener;
    private Handler mHandler = new Handler();
    private final BroadcastReceiver mHiLinkReceiver;
    private HwCustWifiAddFragment mHwCustWifiAddFragment;
    private int mMode = 0;
    private int[] mNetworkSelectionDisableRecord = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private int mNotFoundCount = 0;
    private DialogInterface.OnClickListener mPasswordErrorListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            Activity activity = WifiAddFragment.this.getActivity();
            if (activity != null && !activity.isFinishing()) {
                ((InputMethodManager) activity.getSystemService("input_method")).toggleSoftInput(1, 2);
            }
        }
    };
    private final BroadcastReceiver mReceiver;
    private boolean mReceiverRegistered;
    private ActionListener mSaveListener;
    private String mSavedSSID;
    private Runnable mScanRunnable = new Runnable() {
        public void run() {
            Activity activity = WifiAddFragment.this.getActivity();
            if (activity != null && !activity.isFinishing() && WifiAddFragment.this.mDialog != null && WifiAddFragment.this.mDialog.isShowing() && WifiAddFragment.this.mCurrentDialogId == 1000 && !WifiAddFragment.this.mConnectionHappened && !WifiAddFragment.this.isCurrentAccessPointExist()) {
                WifiAddFragment.this.mDialog.dismiss();
                WifiAddFragment.this.showDialog(1001);
                WifiAddFragment.this.mConnectionHappened = false;
            }
        }
    };
    private DetailedState mState;
    private Button mSubmitButton;
    private Runnable mTimeOutRunnable = new Runnable() {
        public void run() {
            Activity activity = WifiAddFragment.this.getActivity();
            if (activity != null && !activity.isFinishing() && WifiAddFragment.this.mDialog != null && WifiAddFragment.this.mDialog.isShowing() && WifiAddFragment.this.mCurrentDialogId == 1000) {
                WifiAddFragment.this.mDialog.dismiss();
                WifiAddFragment.this.showDialog(1007);
            }
        }
    };
    private View mView;
    private WifiManager mWifiManager;
    private int mWifiState = 4;

    private class ConnectingDialogOnKeyListener implements OnKeyListener {
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (event.getAction() != 0 || keyCode != 4) {
                return false;
            }
            WifiAddFragment.this.forget();
            dialog.dismiss();
            return true;
        }
    }

    public static class DefaultActionListener implements ActionListener {
        public void onSuccess() {
        }

        public void onFailure(int reason) {
        }
    }

    public WifiAddFragment() {
        this.mFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        this.mFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                WifiAddFragment.this.handleEvent(context, intent);
            }
        };
        this.mHiLinkReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (WifiAddFragment.this.mAccessPoint != null) {
                    WifiAddFragment.this.mCurrentSSID = WifiAddFragment.this.mAccessPoint.getSsidStr();
                }
                WifiAddFragment.this.showDialog(1000);
                WifiAddFragment.this.registerReceiver();
            }
        };
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("connection_start_time")) {
                this.mConnectionStartTime = savedInstanceState.getLong("connection_start_time");
            }
            if (savedInstanceState.containsKey("connection_happened")) {
                this.mConnectionHappened = savedInstanceState.getBoolean("connection_happened");
            }
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.mMode = bundle.getInt("dialog_mode", 0);
            if (!bundle.getBoolean("is_add_network", true)) {
                this.mAccessPoint = new AccessPoint(getActivity(), bundle);
                if (this.mAccessPoint.getNetworkId() != -1) {
                    this.mConfig = new WifiConfiguration();
                    this.mConfig.SSID = convertToQuotedString(this.mAccessPoint.getSsidStr());
                    Log.d("WifiAddFragment", ".onCreate().mConfig.SSID:" + this.mConfig.SSID);
                }
            }
            this.isHiLinkNetwork = bundle.getBoolean("is_hi_link_network");
            if (this.mAccessPoint != null) {
                this.mAccessPoint.setHiLinkNetwork(this.isHiLinkNetwork);
            }
            this.bssid = bundle.getString("bssid");
        }
        this.mContext = getActivity();
        this.mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService("wifi");
    }

    private void init() {
        this.mCancelButton = (Button) this.mView.findViewById(2131887544);
        this.mCancelButton.setOnClickListener(this);
        this.mForgetButton = (Button) this.mView.findViewById(2131887545);
        this.mForgetButton.setOnClickListener(this);
        this.mSubmitButton = (Button) this.mView.findViewById(2131887546);
        this.mSubmitButton.setOnClickListener(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mView = inflater.inflate(2130969272, null);
        init();
        if (savedInstanceState != null) {
            boolean isChecked = savedInstanceState.getBoolean("wifi_advanced_togglebox");
            if (isChecked) {
                ((CheckBox) this.mView.findViewById(2131887463)).setChecked(isChecked);
                this.mView.findViewById(2131887515).setVisibility(0);
                ((Spinner) this.mView.findViewById(2131887517)).setSelection(savedInstanceState.getInt("wifi_proxy_settings"));
                ((Spinner) this.mView.findViewById(2131887527)).setSelection(savedInstanceState.getInt("wifi_ip_settings"));
            }
            if (savedInstanceState.getBoolean("dialog_showing")) {
                if (savedInstanceState.getInt("saved_dialog_id") == 1000) {
                    registerReceiver();
                }
                this.mSavedSSID = savedInstanceState.getString("current_ssid");
                showDialog(savedInstanceState.getInt("saved_dialog_id"));
            }
        }
        this.mController = new WifiConfigController(this, this.mView, this.mAccessPoint, this.mMode);
        if (savedInstanceState != null) {
            ((Spinner) this.mView.findViewById(2131887490)).setSelection(savedInstanceState.getInt("method"));
            this.mController.mEapMethodFromSavedInstance = savedInstanceState.getInt("method");
            ((Spinner) this.mView.findViewById(2131887492)).setSelection(savedInstanceState.getInt("phase2"));
            this.mController.mPhase2FromSavedInstance = savedInstanceState.getInt("phase2");
        }
        if (!(this.mMode == 2 || this.mAccessPoint == null || this.mAccessPoint.getSecurity() == 3 || getForgetButton().getVisibility() != 8 || getSubmitButton().getVisibility() != 0)) {
            getSubmitButton().setEnabled(false);
        }
        super.onCreate(savedInstanceState);
        this.mHwCustWifiAddFragment = (HwCustWifiAddFragment) HwCustUtils.createObj(HwCustWifiAddFragment.class, new Object[0]);
        return this.mView;
    }

    public void onResume() {
        super.onResume();
        if (!(this.mHwCustWifiAddFragment == null || this.mHwCustWifiAddFragment.isSupportStaP2pCoexist())) {
            this.mHwCustWifiAddFragment.setIsConnectguiProp(true);
        }
        if (this.isHiLinkNetwork && !TextUtils.isEmpty(this.bssid) && this.mAccessPoint != null) {
            WifiManagerEx.enableHiLinkHandshake(true, this.bssid);
            this.mContext.registerReceiver(this.mHiLinkReceiver, new IntentFilter("android.net.wifi.action.HILINK_STATE_CHANGED"));
        }
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(this.mContext);
        if (this.mDialog != null && this.mDialog.isShowing() && this.mCurrentDialogId == 1000 && getActivity().isFinishing()) {
            this.mDialog.dismiss();
        }
        if (!(this.mHwCustWifiAddFragment == null || this.mHwCustWifiAddFragment.isSupportStaP2pCoexist())) {
            this.mHwCustWifiAddFragment.setIsConnectguiProp(false);
        }
        if (this.isHiLinkNetwork && !TextUtils.isEmpty(this.bssid) && this.mAccessPoint != null) {
            WifiManagerEx.enableHiLinkHandshake(false, this.bssid);
            this.mContext.unregisterReceiver(this.mHiLinkReceiver);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.mView != null) {
            outState.putBoolean("wifi_advanced_togglebox", ((CheckBox) this.mView.findViewById(2131887463)).isChecked());
            outState.putInt("wifi_proxy_settings", ((Spinner) this.mView.findViewById(2131887517)).getSelectedItemPosition());
            outState.putInt("wifi_ip_settings", ((Spinner) this.mView.findViewById(2131887527)).getSelectedItemPosition());
            outState.putInt("method", ((Spinner) this.mView.findViewById(2131887490)).getSelectedItemPosition());
            outState.putInt("phase2", ((Spinner) this.mView.findViewById(2131887492)).getSelectedItemPosition());
        }
        if (this.mDialog != null && this.mDialog.isShowing()) {
            outState.putInt("saved_dialog_id", this.mCurrentDialogId);
            outState.putString("current_ssid", this.mCurrentSSID);
            outState.putBoolean("dialog_showing", true);
        }
        outState.putLong("connection_start_time", this.mConnectionStartTime);
        outState.putBoolean("connection_happened", this.mConnectionHappened);
        super.onSaveInstanceState(outState);
    }

    public Context getContext() {
        return getActivity();
    }

    public void setTitle(int id) {
        getActivity().setTitle(id);
    }

    public void setTitle(CharSequence title) {
        getActivity().setTitle(title);
    }

    public void setSubmitButton(CharSequence text) {
        this.mSubmitButton.setVisibility(0);
        this.mSubmitButton.setText(text);
    }

    public void setForgetButton(CharSequence text) {
        this.mForgetButton.setVisibility(0);
        this.mForgetButton.setText(text);
    }

    public void setCancelButton(CharSequence text) {
        this.mCancelButton.setVisibility(0);
        this.mCancelButton.setText(text);
    }

    public Button getSubmitButton() {
        return this.mSubmitButton;
    }

    public Button getForgetButton() {
        return this.mForgetButton;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 2131887544:
                getActivity().onBackPressed();
                break;
            case 2131887545:
                ((SettingsActivity) getActivity()).finishPreferencePanel(this, -2, new Intent());
                break;
            case 2131887546:
                ItemUseStat.getInstance().handleClick(this.mContext, 2, "add_wifi_connect");
                this.mNotFoundCount = 0;
                this.mConnectionHappened = false;
                this.mConnectionStartTime = System.currentTimeMillis();
                hideSoftInputFromWindow();
                if (this.mAccessPoint == null || this.mAccessPoint.getSecurity() != 3) {
                    WifiConfiguration currentConfig = this.mController.getConfig();
                    if (currentConfig != null) {
                        String str;
                        if (currentConfig.SSID == null) {
                            currentConfig.SSID = this.mConfig.SSID;
                        }
                        if (currentConfig.SSID == null) {
                            str = "";
                        } else {
                            str = WifiInfo.removeDoubleQuotes(currentConfig.SSID);
                        }
                        this.mCurrentSSID = str;
                        this.mWifiManager.save(currentConfig, this.mSaveListener);
                        this.mWifiManager.connect(currentConfig, this.mConnectListener);
                        WifiExtUtils.setManualConnect(getActivity());
                        showDialog(1000);
                        registerReceiver();
                        this.mHandler.removeCallbacks(this.mScanRunnable);
                        this.mHandler.postDelayed(this.mScanRunnable, 12000);
                        Log.d("WifiAddFragment", "detect connection time out is set to 12000ms");
                        this.mHandler.removeCallbacks(this.mTimeOutRunnable);
                        this.mHandler.postDelayed(this.mTimeOutRunnable, 50000);
                        break;
                    }
                }
                finishFragment(-3);
                return;
                break;
        }
        if (Global.getInt(getActivity().getContentResolver(), "device_provisioned", 1) == 0) {
            hideSoftInputFromWindow();
        }
    }

    public String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    public void finishFragment(int resultCode) {
        if (!(this.mDialog == null || !this.mDialog.isShowing() || this.mCurrentDialogId == 1000)) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
        SettingsActivity activity = (SettingsActivity) getActivity();
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable("wifi_configuration", this.mController.getConfig());
        int mode = this.mController.getMode();
        intent.putExtra("wifi_config", bundle);
        intent.putExtra("dialog_mode", mode);
        activity.finishPreferencePanel(this, resultCode, intent);
    }

    public boolean isCurrentAccessPointExist() {
        List<ScanResult> results = this.mWifiManager.getScanResults();
        if (!(results == null || this.mCurrentSSID == null)) {
            for (ScanResult result : results) {
                if (result != null && this.mCurrentSSID.equals(result.SSID)) {
                    if (this.mAccessPoint == null || this.mAccessPoint.getSecurity() == WifiExtUtils.getSecurity(result)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void forget() {
        unregisterReceiver();
        if (this.mController == null) {
            Log.w("WifiAddFragment", "forget()--> mController is null");
            return;
        }
        WifiConfiguration currentConfig = this.mController.getConfig();
        if (currentConfig == null || currentConfig.allowedKeyManagement.cardinality() > 1) {
            Log.e("WifiAddFragment", "forget network failed as WifiConfiguration is null or invalid WifiConfiguration");
            return;
        }
        if (currentConfig.SSID == null) {
            currentConfig.SSID = this.mConfig.SSID;
        }
        List<WifiConfiguration> configs = this.mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                if (TextUtils.equals(WifiInfo.removeDoubleQuotes(config.SSID), WifiInfo.removeDoubleQuotes(currentConfig.SSID)) && config.getAuthType() == currentConfig.getAuthType()) {
                    if (config.networkId != -1) {
                        this.mWifiManager.forget(config.networkId, this.mForgetListener);
                    } else {
                        Log.e("WifiAddFragment", "forget network failed as networkId is invalid, config.SSID:" + config.SSID);
                    }
                }
            }
        }
    }

    private void createAndShowConnectingDialog() {
        this.mDialog = new ProgressDialog(this.mContext);
        ProgressDialog progressDialog = this.mDialog;
        progressDialog.setMessage(getString(2131628052, new Object[]{convertToQuotedString(this.mCurrentSSID)}));
        progressDialog.setProgressStyle(0);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnKeyListener(new ConnectingDialogOnKeyListener());
        progressDialog.show();
    }

    public void showDialog(int dialogId) {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
        this.mCurrentDialogId = dialogId;
        if (dialogId != 1000) {
            unregisterReceiver();
        }
        if (this.mCurrentSSID == null && this.mSavedSSID != null) {
            this.mCurrentSSID = this.mSavedSSID;
        }
        switch (dialogId) {
            case 1000:
                createAndShowConnectingDialog();
                return;
            case 1001:
                createAndShowDialog(null, getString(2131628053, new Object[]{convertToQuotedString(this.mCurrentSSID)}), false);
                forget();
                return;
            case 1002:
                createAndShowDialog(null, getString(2131628148), false, this.mPasswordErrorListener);
                forget();
                return;
            case 1003:
                createAndShowLinkifyDialog(getString(2131628146), false, null);
                return;
            case 1004:
                createAndShowLinkifyDialog(getString(2131628145), false, null);
                return;
            case 1005:
                createAndShowLinkifyDialog(getString(2131628144), false, null);
                return;
            case 1006:
                createAndShowLinkifyDialog(getString(2131628147), false, null);
                return;
            case 1007:
                createAndShowDialog(null, getString(2131628088, new Object[]{convertToQuotedString(this.mCurrentSSID)}), false);
                return;
            default:
                return;
        }
    }

    public LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(getActivity());
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        if (getActivity() != null) {
            PreferenceFrameLayout frameLayout = (PreferenceFrameLayout) getActivity().findViewById(16909261);
            if (frameLayout != null) {
                frameLayout.setPaddingRelative(0, frameLayout.getPaddingTop(), 0, frameLayout.getPaddingBottom());
            }
        }
        this.mConnectListener = new DefaultActionListener();
        this.mSaveListener = new DefaultActionListener();
        this.mForgetListener = new DefaultActionListener();
    }

    private void handleEvent(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                Log.d("WifiAddFragment", ".handleEvent().received action:" + action);
                WifiConfiguration currentConfig = this.mController.getConfig();
                if (this.mMode != 2 || this.mAccessPoint != null || currentConfig != null) {
                    if (currentConfig == null) {
                        currentConfig = this.mConfig;
                    } else if (currentConfig.SSID == null) {
                        currentConfig = this.mConfig;
                    }
                    WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
                    String activeSSID = wifiInfo == null ? "" : wifiInfo.getSSID();
                    String str = null;
                    int currentSecurity = -1;
                    if (currentConfig != null) {
                        str = currentConfig.SSID;
                        currentSecurity = WifiExtUtils.getSecurity(currentConfig);
                    }
                    if (this.mAccessPoint != null) {
                        currentSecurity = this.mAccessPoint.getSecurity();
                    }
                    this.mWifiState = intent.getIntExtra("wifi_state", 4);
                    if ("android.net.wifi.SCAN_RESULTS".equals(action)) {
                        boolean found = isCurrentAccessPointExist();
                        if (!found) {
                            this.mNotFoundCount++;
                            Log.d("WifiAddFragment", " handleEvent().mNotFoundCount:" + this.mNotFoundCount);
                        }
                        if (!(this.mConnectionHappened || found || this.mNotFoundCount < 2 || this.mHandler.hasCallbacks(this.mScanRunnable))) {
                            long duration = System.currentTimeMillis() - this.mConnectionStartTime;
                            if (this.mConnectionStartTime <= 0 || duration <= 0 || duration >= 12000) {
                                showDialog(1001);
                                this.mNotFoundCount = 0;
                                return;
                            }
                            Log.d("WifiAddFragment", " handleEvent().SCAN_RESULTS_AVAILABLE_ACTION.connection time is not enough, duration:" + duration + "|mConnectionStartTime:" + this.mConnectionStartTime);
                        }
                    } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                        Log.d("WifiAddFragment", ".handleEvent().WIFI_STATE_CHANGED.WIFI_STATE: " + this.mWifiState);
                        if (this.mWifiState == 1) {
                            finishFragment(-1);
                        }
                    } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                        NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if (info != null) {
                            this.mState = info.getDetailedState();
                        }
                        Log.d("WifiAddFragment", ".handleEvent().NETWORK_STATE_CHANGED.DetailedState:" + this.mState);
                        if (this.mState != null && this.mState == DetailedState.CONNECTED && TextUtils.equals(str, activeSSID)) {
                            SupplicantState supplicantState = null;
                            if (wifiInfo != null) {
                                supplicantState = wifiInfo.getSupplicantState();
                            }
                            if (supplicantState == null || supplicantState == SupplicantState.COMPLETED) {
                                finishFragment(-4);
                            }
                        }
                    } else if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
                        fetchEverConnectedState(intent, str, activeSSID);
                        WifiConfiguration config = getWifiConfiguration(str, currentSecurity);
                        if (config != null) {
                            int stopReason = getStopReason(config);
                            if (isConfigStateError(config)) {
                                showErrorMessageDialog(config, stopReason);
                            }
                        }
                    }
                }
            }
        }
    }

    private void fetchEverConnectedState(Intent intent, String operatingSSID, String activeSSID) {
        SupplicantState state = (SupplicantState) intent.getParcelableExtra("newState");
        Log.d("WifiAddFragment", ".handleEvent().SUPPLICANT_STATE_CHANGED.SupplicantState:" + state);
        if (TextUtils.equals(operatingSSID, activeSSID)) {
            if (!(state == SupplicantState.ASSOCIATED || state == SupplicantState.FOUR_WAY_HANDSHAKE || state == SupplicantState.GROUP_HANDSHAKE)) {
                if (state != SupplicantState.COMPLETED) {
                    return;
                }
            }
            if (!this.mConnectionHappened) {
                Log.w("WifiAddFragment", "mConnectionHappened was set to true, activeSSID:" + activeSSID);
            }
            this.mConnectionHappened = true;
        }
    }

    private WifiConfiguration getWifiConfiguration(String ssid, int security) {
        this.mWifiManager.startScan();
        List<WifiConfiguration> configs = this.mWifiManager.getConfiguredNetworks();
        if (configs == null) {
            return null;
        }
        for (WifiConfiguration config : configs) {
            if (config.SSID != null && config.SSID.equals(ssid) && WifiExtUtils.getSecurity(config) == security) {
                return config;
            }
        }
        return null;
    }

    private int getStopReason(WifiConfiguration config) {
        int stopReason = 0;
        NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        for (int reason = 5; reason >= 2; reason--) {
            int disableReasonCounter = networkStatus.getDisableReasonCounter(reason);
            if (disableReasonCounter > this.mNetworkSelectionDisableRecord[reason]) {
                stopReason = reason;
            }
            this.mNetworkSelectionDisableRecord[reason] = disableReasonCounter;
        }
        return stopReason;
    }

    private boolean isConfigStateError(WifiConfiguration config) {
        if (config.getNetworkSelectionStatus().isNetworkEnabled()) {
            return false;
        }
        return true;
    }

    private void showErrorMessageDialog(WifiConfiguration config, int stopReason) {
        NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
        if (stopReason == 0) {
            stopReason = networkStatus.getNetworkSelectionDisableReason();
        }
        switch (stopReason) {
            case -1:
                finishFragment(-1);
                return;
            case 2:
                int status_code = System.getInt(this.mContext.getContentResolver(), "wifi_association_reject_status_code", 1);
                if (status_code == 17) {
                    showDialog(1004);
                    return;
                } else if (status_code == 15) {
                    showDialog(1002);
                    return;
                } else {
                    showDialog(1005);
                    return;
                }
            case 3:
                showDialog(1002);
                return;
            case 4:
            case 5:
                showDialog(1003);
                return;
            default:
                Log.d("WifiAddFragment", "WifiConfiguration status disabled, into default.");
                finishFragment(-1);
                return;
        }
    }

    protected void hideSoftInputFromWindow() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService("input_method");
        View focus = getActivity().getCurrentFocus();
        if (focus != null) {
            imm.hideSoftInputFromWindow(focus.getApplicationWindowToken(), 2);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
        this.mHandler.removeCallbacks(this.mScanRunnable);
        this.mHandler.removeCallbacks(this.mTimeOutRunnable);
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        View security = this.mView.findViewById(2131887460);
        if (security instanceof ListSpinner) {
            ((ListSpinner) security).dismissDialog();
        }
        View method = this.mView.findViewById(2131887490);
        if (method instanceof ListSpinner) {
            ((ListSpinner) method).dismissDialog();
        }
        View phase2 = this.mView.findViewById(2131887492);
        if (phase2 instanceof ListSpinner) {
            ((ListSpinner) phase2).dismissDialog();
        }
        View caCert = this.mView.findViewById(2131887494);
        if (caCert instanceof ListSpinner) {
            ((ListSpinner) caCert).dismissDialog();
        }
        View userCert = this.mView.findViewById(2131887500);
        if (userCert instanceof ListSpinner) {
            ((ListSpinner) userCert).dismissDialog();
        }
        View proxySettings = this.mView.findViewById(2131887517);
        if (proxySettings instanceof ListSpinner) {
            ((ListSpinner) proxySettings).dismissDialog();
        }
        View ipSettings = this.mView.findViewById(2131887527);
        if (ipSettings instanceof ListSpinner) {
            ((ListSpinner) ipSettings).dismissDialog();
        }
    }

    private void registerReceiver() {
        if (!this.mReceiverRegistered) {
            this.mContext.registerReceiver(this.mReceiver, this.mFilter);
            this.mReceiverRegistered = true;
        }
    }

    private void unregisterReceiver() {
        if (this.mReceiverRegistered) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiverRegistered = false;
        }
    }

    private Dialog createAndShowDialog(String title, String message, boolean cancelable, DialogInterface.OnClickListener listener) {
        this.mDialog = new Builder(getActivity()).setTitle(title).setMessage(message).setCancelable(cancelable).setPositiveButton(2131627945, listener).create();
        this.mDialog.show();
        return this.mDialog;
    }

    private Dialog createAndShowDialog(String title, String message, boolean cancelable) {
        return createAndShowDialog(title, message, cancelable, null);
    }

    private Dialog createAndShowLinkifyDialog(String message, boolean cancelable, DialogInterface.OnClickListener listener) {
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(message);
        LinkifyUtils.deleteLink(contentBuilder);
        return createAndShowDialog(null, contentBuilder.toString(), cancelable, listener);
    }

    public void dispatchSubmit() {
    }
}
