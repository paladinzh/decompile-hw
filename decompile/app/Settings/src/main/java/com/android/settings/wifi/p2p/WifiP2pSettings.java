package com.android.settings.wifi.p2p;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.PersistentGroupInfoListener;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.android.settings.ProgressCategory;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utf8ByteLengthFilter;
import com.android.settings.Utils;
import com.android.settings.Utils.ImmersionIcon;
import com.android.settings.inputmethod.InputMethodExtUtils;
import com.android.settings.wifi.WifiExtUtils;
import com.huawei.cust.HwCustUtils;

public class WifiP2pSettings extends WifiP2pSettingsHwBase implements PersistentGroupInfoListener, PeerListListener {
    private OnClickListener mCancelConnectListener;
    private Channel mChannel;
    private int mConnectedDevices;
    private OnClickListener mDeleteGroupListener;
    private OnClickListener mDisconnectListener;
    private HwCustWifiP2pSettings mHwCustWifiP2pSettings;
    private final IntentFilter mIntentFilter = new IntentFilter();
    private boolean mLastGroupFormed = false;
    private PreferenceGroup mPersistentGroup;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean z = true;
            String action = intent.getAction();
            if ("android.net.wifi.p2p.STATE_CHANGED".equals(action)) {
                WifiP2pSettings wifiP2pSettings = WifiP2pSettings.this;
                if (intent.getIntExtra("wifi_p2p_state", 1) != 2) {
                    z = false;
                }
                wifiP2pSettings.mWifiP2pEnabled = z;
                WifiP2pSettings.this.handleP2pStateChanged();
            } else if ("android.net.wifi.p2p.PEERS_CHANGED".equals(action)) {
                WifiP2pSettings.this.mPeers = (WifiP2pDeviceList) intent.getParcelableExtra("wifiP2pDeviceList");
                WifiP2pSettings.this.handlePeersChanged();
            } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                if (WifiP2pSettings.this.mWifiP2pManager != null) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    WifiP2pInfo wifip2pinfo = (WifiP2pInfo) intent.getParcelableExtra("wifiP2pInfo");
                    if (networkInfo != null && networkInfo.isConnected()) {
                        Log.d("WifiP2pSettings", "Connected");
                    } else if (!(wifip2pinfo == null || wifip2pinfo.groupFormed)) {
                        if (WifiP2pSettings.this.mHwCustWifiP2pSettings.isSupportStaP2pCoexist()) {
                            WifiP2pSettings.this.startSearch();
                        } else if (WifiP2pSettings.this.mHwCustWifiP2pSettings.isWifiP2pEnabled()) {
                            WifiP2pSettings.this.startSearch();
                        }
                    }
                    if (wifip2pinfo != null) {
                        WifiP2pSettings.this.mLastGroupFormed = wifip2pinfo.groupFormed;
                    }
                }
            } else if ("android.net.wifi.p2p.THIS_DEVICE_CHANGED".equals(action)) {
                WifiP2pSettings.this.mThisDevice = (WifiP2pDevice) intent.getParcelableExtra("wifiP2pDevice");
                WifiP2pSettings.this.updateDevicePref();
            } else if ("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE".equals(action)) {
                int discoveryState = intent.getIntExtra("discoveryState", 1);
                Log.d("WifiP2pSettings", "Discovery state changed: " + discoveryState);
                if (discoveryState == 2) {
                    WifiP2pSettings.this.updateSearchMenu(true);
                    WifiP2pSettings.this.removePreference(WifiP2pSettings.this.mPeersGroup, "faq_no_device_found");
                } else {
                    WifiP2pSettings.this.updateSearchMenu(false);
                    if (discoveryState == 1) {
                        WifiP2pSettings.this.displayCausesOfUndiscovered(WifiP2pSettings.this.mPeersGroup);
                    }
                }
            } else if ("android.net.wifi.p2p.CONNECT_STATE_CHANGE".equals(action)) {
                int state = intent.getIntExtra("extraState", 0);
                Log.d("WifiP2pSettings", "ACTION_CONNECT_STATE_CHANGE: state = " + state + ", mScreenType = " + WifiP2pSettings.this.mScreenType);
                if (state == 2) {
                    if (WifiP2pSettings.this.mScreenType == 1) {
                        WifiP2pSettings.this.mScreenType = 0;
                        WifiP2pSettings.this.getActivity().setResult(1, null);
                        WifiP2pSettings.this.getActivity().finish();
                    }
                } else if (state == 3) {
                    WifiP2pSettings.this.showDialog(5);
                }
            } else if ("android.net.wifi.p2p.PERSISTENT_GROUPS_CHANGED".equals(action) && WifiP2pSettings.this.mWifiP2pManager != null) {
                WifiP2pSettings.this.mWifiP2pManager.requestPersistentGroupInfo(WifiP2pSettings.this.mChannel, WifiP2pSettings.this);
            }
        }
    };
    private OnClickListener mRenameListener;
    private String mSavedDeviceName;
    private WifiP2pPersistentGroup mSelectedGroup;
    private String mSelectedGroupName;
    private WifiP2pPeer mSelectedWifiPeer;
    private Preference mThisDevicePref;
    private boolean mWifiP2pEnabled;

    public WifiP2pSettings() {
        Log.d("WifiP2pSettings", "Creating WifiP2pSettings ...");
        this.mHwCustWifiP2pSettings = (HwCustWifiP2pSettings) HwCustUtils.createObj(HwCustWifiP2pSettings.class, new Object[]{this});
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        addPreferencesFromResource(2131230942);
        this.mIntentFilter.addAction("android.net.wifi.p2p.STATE_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.p2p.PEERS_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.p2p.THIS_DEVICE_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.p2p.PERSISTENT_GROUPS_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.p2p.CONNECT_STATE_CHANGE");
        Activity activity = getActivity();
        this.mWifiManager = (WifiManager) activity.getApplicationContext().getSystemService("wifi");
        this.mWifiP2pManager = (WifiP2pManager) getSystemService("wifip2p");
        if (this.mWifiP2pManager != null) {
            this.mChannel = this.mWifiP2pManager.initialize(activity, getActivity().getMainLooper(), null);
            if (this.mChannel == null) {
                Log.e("WifiP2pSettings", "Failed to set up connection with wifi p2p service");
                this.mWifiP2pManager = null;
            }
        } else {
            Log.e("WifiP2pSettings", "mWifiP2pManager is null !");
        }
        if (savedInstanceState != null && savedInstanceState.containsKey("PEER_STATE")) {
            this.mSelectedWifiPeer = new WifiP2pPeer(getActivity(), (WifiP2pDevice) savedInstanceState.getParcelable("PEER_STATE"));
        }
        if (savedInstanceState != null && savedInstanceState.containsKey("DEV_NAME")) {
            this.mSavedDeviceName = savedInstanceState.getString("DEV_NAME");
        }
        if (savedInstanceState != null && savedInstanceState.containsKey("GROUP_NAME")) {
            this.mSelectedGroupName = savedInstanceState.getString("GROUP_NAME");
        }
        this.mRenameListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1 && WifiP2pSettings.this.mWifiP2pManager != null) {
                    String name = null;
                    if (WifiP2pSettings.this.mDeviceNameText != null) {
                        name = WifiP2pSettings.this.mDeviceNameText.getText().toString();
                    }
                    if (name != null) {
                        int i = 0;
                        while (i < name.length()) {
                            char cur = name.charAt(i);
                            if (Character.isDigit(cur) || Character.isLetter(cur) || cur == '-' || cur == '_' || cur == ' ') {
                                i++;
                            } else {
                                Toast.makeText(WifiP2pSettings.this.getActivity(), 2131625046, 1).show();
                                return;
                            }
                        }
                    }
                    WifiP2pSettings.this.mWifiP2pManager.setDeviceName(WifiP2pSettings.this.mChannel, WifiP2pSettings.this.mDeviceNameText.getText().toString(), new ActionListener() {
                        public void onSuccess() {
                            Log.d("WifiP2pSettings", " device rename success");
                        }

                        public void onFailure(int reason) {
                            if (WifiP2pSettings.this.getActivity() != null) {
                                Toast.makeText(WifiP2pSettings.this.getActivity(), 2131627285, 1).show();
                            }
                        }
                    });
                }
            }
        };
        this.mDisconnectListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1 && WifiP2pSettings.this.mWifiP2pManager != null) {
                    WifiP2pSettings.this.mWifiP2pManager.removeGroup(WifiP2pSettings.this.mChannel, new ActionListener() {
                        public void onSuccess() {
                            Log.d("WifiP2pSettings", " remove group success");
                        }

                        public void onFailure(int reason) {
                            Log.d("WifiP2pSettings", " remove group fail " + reason);
                        }
                    });
                }
            }
        };
        this.mCancelConnectListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1 && WifiP2pSettings.this.mWifiP2pManager != null) {
                    WifiP2pSettings.this.mWifiP2pManager.cancelConnect(WifiP2pSettings.this.mChannel, new ActionListener() {
                        public void onSuccess() {
                            Log.d("WifiP2pSettings", " cancel connect success");
                        }

                        public void onFailure(int reason) {
                            Log.d("WifiP2pSettings", " cancel connect fail " + reason);
                        }
                    });
                }
            }
        };
        this.mDeleteGroupListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    if (WifiP2pSettings.this.mWifiP2pManager == null) {
                        return;
                    }
                    if (WifiP2pSettings.this.mSelectedGroup != null) {
                        Log.d("WifiP2pSettings", " deleting group " + WifiP2pSettings.this.mSelectedGroup.getGroupName());
                        WifiP2pSettings.this.mWifiP2pManager.deletePersistentGroup(WifiP2pSettings.this.mChannel, WifiP2pSettings.this.mSelectedGroup.getNetworkId(), new ActionListener() {
                            public void onSuccess() {
                                Log.d("WifiP2pSettings", " delete group success");
                            }

                            public void onFailure(int reason) {
                                Log.d("WifiP2pSettings", " delete group fail " + reason);
                            }
                        });
                        WifiP2pSettings.this.mSelectedGroup = null;
                        return;
                    }
                    Log.w("WifiP2pSettings", " No selected group to delete!");
                } else if (which == -2 && WifiP2pSettings.this.mSelectedGroup != null) {
                    Log.d("WifiP2pSettings", " forgetting selected group " + WifiP2pSettings.this.mSelectedGroup.getGroupName());
                    WifiP2pSettings.this.mSelectedGroup = null;
                }
            }
        };
        setHasOptionsMenu(true);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.removeAll();
        preferenceScreen.setOrderingAsAdded(true);
        buildDeviceCategory(preferenceScreen);
        this.mThisDevicePref = new Preference(getPrefContext());
        this.mThisDevicePref.setPersistent(false);
        InputMethodExtUtils.buildPreference(this.mThisDevicePref, 2131627569, 2130968977, 2130968998);
        preferenceScreen.addPreference(this.mThisDevicePref);
        this.mPeersGroup = new ProgressCategory(getPrefContext());
        this.mPeersGroup.setTitle(2131624838);
        this.mPeersGroup.setEmptyTextRes(2131624445);
        preferenceScreen.addPreference(this.mPeersGroup);
        this.mPersistentGroup = new PreferenceCategory(getPrefContext());
        this.mPersistentGroup.setLayoutResource(2130968916);
        this.mPersistentGroup.setTitle(2131625044);
        preferenceScreen.addPreference(this.mPersistentGroup);
        if (!SystemProperties.getBoolean("ro.config.wifi_show_p2p_group", false)) {
            preferenceScreen.removePreference(this.mPersistentGroup);
        }
        super.onActivityCreated(savedInstanceState);
    }

    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(this.mReceiver, this.mIntentFilter);
        if (this.mWifiP2pManager != null) {
            this.mWifiP2pManager.requestPeers(this.mChannel, this);
        }
        if (UserHandle.getCallingUserId() == 0) {
            WifiExtUtils.setBeamPushUrisCallback(getActivity(), "content://huawei/p2pconnect/3");
        } else {
            Log.d("WifiP2pSettings", "/ WifiP2pSettings:The sub UserId = " + UserHandle.getCallingUserId());
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mWifiP2pManager != null) {
            this.mWifiP2pManager.stopPeerDiscovery(this.mChannel, null);
        }
        getActivity().unregisterReceiver(this.mReceiver);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        int textId;
        if (this.mWifiP2pSearching) {
            textId = 2131627572;
        } else {
            textId = 2131627571;
        }
        menu.add(0, 1, 0, textId).setEnabled(this.mWifiP2pEnabled).setTitle(textId).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), this.mWifiP2pSearching ? Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_CLOSE) : Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_SEARCH))).setShowAsAction(1);
        if (this.mHwCustWifiP2pSettings != null) {
            this.mHwCustWifiP2pSettings.setP2pSearchMenuEnabled(menu.findItem(1), this.mWifiP2pEnabled);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                if (this.mWifiP2pSearching) {
                    stopSearch();
                } else {
                    startSearch();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == this.mThisDevicePref) {
            Intent intent = new Intent("android.settings.DEVICE_NAME_SETTINGS");
            intent.putExtra("device_name", preference.getSummary());
            getActivity().startActivity(intent);
        }
        if (preference instanceof WifiP2pPeer) {
            this.mSelectedWifiPeer = (WifiP2pPeer) preference;
            if (this.mSelectedWifiPeer.device.status == 0) {
                showDialog(1);
            } else if (this.mSelectedWifiPeer.device.status == 1) {
                showDialog(2);
            } else {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = this.mSelectedWifiPeer.device.deviceAddress;
                int forceWps = SystemProperties.getInt("wifidirect.wps", -1);
                if (forceWps != -1) {
                    config.wps.setup = forceWps;
                } else if (this.mSelectedWifiPeer.device.wpsPbcSupported()) {
                    config.wps.setup = 0;
                } else if (this.mSelectedWifiPeer.device.wpsKeypadSupported()) {
                    config.wps.setup = 2;
                } else {
                    config.wps.setup = 1;
                }
                if (this.mWifiP2pManager != null) {
                    this.mWifiP2pManager.connect(this.mChannel, config, new ActionListener() {
                        public void onSuccess() {
                            Log.d("WifiP2pSettings", " connect success");
                        }

                        public void onFailure(int reason) {
                            Log.e("WifiP2pSettings", " connect fail " + reason);
                            WifiP2pSettings.this.showDialog(5);
                        }
                    });
                }
            }
        } else if (preference instanceof WifiP2pPersistentGroup) {
            this.mSelectedGroup = (WifiP2pPersistentGroup) preference;
            showDialog(4);
        }
        return super.onPreferenceTreeClick(preference);
    }

    public Dialog onCreateDialog(int id) {
        String deviceName;
        if (id == 1) {
            String msg;
            if (TextUtils.isEmpty(this.mSelectedWifiPeer.device.deviceName)) {
                deviceName = this.mSelectedWifiPeer.device.deviceAddress;
            } else {
                deviceName = this.mSelectedWifiPeer.device.deviceName;
            }
            if (this.mConnectedDevices > 1) {
                msg = getActivity().getString(2131625049, new Object[]{deviceName, Integer.valueOf(this.mConnectedDevices - 1)});
            } else {
                msg = getActivity().getString(2131625048, new Object[]{deviceName});
            }
            return new Builder(getActivity()).setTitle(2131625047).setMessage(msg).setPositiveButton(getActivity().getString(2131625656), this.mDisconnectListener).setNegativeButton(getActivity().getString(2131625657), null).create();
        } else if (id == 2) {
            if (TextUtils.isEmpty(this.mSelectedWifiPeer.device.deviceName)) {
                deviceName = this.mSelectedWifiPeer.device.deviceAddress;
            } else {
                deviceName = this.mSelectedWifiPeer.device.deviceName;
            }
            return new Builder(getActivity()).setTitle(2131625050).setMessage(getActivity().getString(2131625051, new Object[]{deviceName})).setPositiveButton(getActivity().getString(2131625656), this.mCancelConnectListener).setNegativeButton(getActivity().getString(2131625657), null).create();
        } else if (id == 3) {
            View renameView = LayoutInflater.from(getActivity()).inflate(2130968742, null);
            this.mDeviceNameText = (EditText) renameView.findViewById(2131886503);
            this.mDeviceNameText.setFilters(new InputFilter[]{new Utf8ByteLengthFilter(30)});
            this.mDeviceNameText.setSingleLine();
            if (this.mSavedDeviceName != null) {
                this.mDeviceNameText.setText(this.mSavedDeviceName);
                this.mDeviceNameText.setSelection(this.mSavedDeviceName.length());
            } else if (!(this.mThisDevice == null || TextUtils.isEmpty(this.mThisDevice.deviceName))) {
                String tempName = this.mThisDevice.deviceName;
                if (tempName.length() > 30) {
                    tempName = tempName.substring(0, 30);
                }
                this.mDeviceNameText.setText(tempName);
                this.mDeviceNameText.setSelection(tempName.length());
            }
            this.mSavedDeviceName = null;
            this.mDeviceNameText.addTextChangedListener(this);
            this.mAlertDialog = new Builder(getActivity()).setTitle(2131627349).setView(renameView).setPositiveButton(getActivity().getString(2131625656), this.mRenameListener).setNegativeButton(getActivity().getString(2131625657), null).create();
            this.mAlertDialog.getWindow().setSoftInputMode(5);
            this.mAlertDialog.setOnShowListener(this.mAlertDlgOnShowListener);
            return this.mAlertDialog;
        } else if (id == 4) {
            return new Builder(getActivity()).setTitle(getActivity().getString(2131625052)).setPositiveButton(getActivity().getString(2131625656), this.mDeleteGroupListener).setNegativeButton(getActivity().getString(2131625657), this.mDeleteGroupListener).create();
        } else {
            if (id == 5) {
                return showConnectFailDialog(getActivity());
            }
            return null;
        }
    }

    protected int getMetricsCategory() {
        return 109;
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.mSelectedWifiPeer != null) {
            outState.putParcelable("PEER_STATE", this.mSelectedWifiPeer.device);
        }
        if (this.mDeviceNameText != null) {
            outState.putString("DEV_NAME", this.mDeviceNameText.getText().toString());
        }
        if (this.mSelectedGroup != null) {
            outState.putString("GROUP_NAME", this.mSelectedGroup.getGroupName());
        }
    }

    protected void handlePeersChanged() {
        Activity activity = getActivity();
        if (activity != null) {
            this.mPeersGroup.removeAll();
            this.mConnectedDevices = 0;
            Log.d("WifiP2pSettings", "List of available peers");
            for (WifiP2pDevice peer : this.mPeers.getDeviceList()) {
                Log.d("WifiP2pSettings", "-> " + peer.toString().substring(0, peer.toString().indexOf(10)));
                this.mPeersGroup.addPreference(new WifiP2pPeer(activity, peer));
                if (peer.status == 0) {
                    this.mConnectedDevices++;
                }
            }
            Log.d("WifiP2pSettings", " mConnectedDevices " + this.mConnectedDevices);
            if (this.mStopScanning) {
                displayCausesOfUndiscovered(this.mPeersGroup);
            }
        }
    }

    public void onPersistentGroupInfoAvailable(WifiP2pGroupList groups) {
        Activity activity = getActivity();
        if (activity != null) {
            this.mPersistentGroup.removeAll();
            for (WifiP2pGroup group : groups.getGroupList()) {
                Log.d("WifiP2pSettings", " group " + group);
                if (group != null) {
                    WifiP2pPersistentGroup wppg = new WifiP2pPersistentGroup(activity, group);
                    wppg.setWidgetLayoutResource(2130968998);
                    this.mPersistentGroup.addPreference(wppg);
                    if (wppg.getGroupName().equals(this.mSelectedGroupName)) {
                        Log.d("WifiP2pSettings", "Selecting group " + wppg.getGroupName());
                        this.mSelectedGroup = wppg;
                        this.mSelectedGroupName = null;
                    }
                }
            }
            if (this.mSelectedGroupName != null) {
                Log.w("WifiP2pSettings", " Selected group " + this.mSelectedGroupName + " disappered on next query ");
            }
        }
    }

    public void onPeersAvailable(WifiP2pDeviceList peers) {
        Log.d("WifiP2pSettings", "Requested peers are available");
        this.mPeers = peers;
        handlePeersChanged();
    }

    private void handleP2pStateChanged() {
        updateSearchMenu(false);
        this.mThisDevicePref.setEnabled(this.mWifiP2pEnabled);
        this.mPeersGroup.setEnabled(this.mWifiP2pEnabled);
        this.mPersistentGroup.setEnabled(this.mWifiP2pEnabled);
        if (this.mWifiP2pManager != null) {
            this.mWifiP2pManager.requestPeers(this.mChannel, this);
        }
        if (this.mHwCustWifiP2pSettings != null) {
            this.mHwCustWifiP2pSettings.updateAllDevicePrefEnabled(this.myDeviceCatotgory, this.mThisDevicePref, this.mPeersGroup, this.mPersistentGroup);
        }
    }

    protected void updateSearchMenu(boolean searching) {
        this.mWifiP2pSearching = searching;
        this.mStopScanning = !searching;
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
        this.mPeersGroup.setProgress(searching);
    }

    private void startSearch() {
        if (this.mWifiP2pManager != null && !this.mWifiP2pSearching) {
            Preference pref = this.mPeersGroup.findPreference("faq_no_device_found");
            if (pref != null) {
                this.mPeersGroup.removePreference(pref);
            }
            removePreference("empty");
            this.mWifiP2pManager.discoverPeers(this.mChannel, new ActionListener() {
                public void onSuccess() {
                }

                public void onFailure(int reason) {
                    Log.d("WifiP2pSettings", " discover fail " + reason);
                    if (!(WifiP2pSettings.this.mHwCustWifiP2pSettings == null || WifiP2pSettings.this.mHwCustWifiP2pSettings.isSupportStaP2pCoexist())) {
                        WifiP2pSettings.this.updateSearchMenu(false);
                        Log.d("WifiP2pSettings", "update SearchMenu false with do P2P_FIND command failed");
                    }
                    WifiP2pSettings.this.displayCausesOfUndiscovered(WifiP2pSettings.this.mPeersGroup);
                }
            });
            this.mStopScanning = false;
            updateSearchMenu(true);
        }
    }

    private void updateDevicePref() {
        if (this.mThisDevice != null) {
            CharSequence diviceName = this.mThisDevice.deviceName;
            if (TextUtils.isEmpty(diviceName)) {
                diviceName = this.mThisDevice.deviceAddress;
            }
            this.mThisDevicePref.setSummary(diviceName);
            this.mThisDevicePref.setPersistent(false);
            this.mThisDevicePref.setEnabled(this.mWifiP2pEnabled);
            this.mStopScanning = false;
            if (this.mHwCustWifiP2pSettings != null) {
                this.mHwCustWifiP2pSettings.updateDevicePrefEnabled(this.mThisDevicePref);
            }
        }
    }
}
