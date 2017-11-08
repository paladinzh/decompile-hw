package com.android.settings.wifi;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.ActionListener;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.ItemUseStat;
import com.android.settings.ScanSettingsPreference;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsActivity.ContextMenuClosedListener;
import com.android.settings.Utils;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Index;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.wifi.AccessPointPreference.UserBadgeCache;
import com.android.settings.wifi.WifiDialog.WifiDialogListener;
import com.android.settings.wifi.cmcc.SwitchToWifiUtils;
import com.android.settings.wifi.cmcc.WifiExt;
import com.android.settings.wifi.p2p.WifiP2pSettings;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.drawer.DrawerLayoutEx;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.AccessPoint.AccessPointListener;
import com.android.settingslib.wifi.WifiStatusTracker;
import com.android.settingslib.wifi.WifiTracker;
import com.android.settingslib.wifi.WifiTracker.WifiListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiSettings extends WifiSettingsHwBase implements Indexable, WifiListener, AccessPointListener, WifiDialogListener {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = res.getString(2131624902);
            data.screenTitle = res.getString(2131624902);
            data.keywords = res.getString(2131626642);
            result.add(data);
            if (SystemProperties.getBoolean("ro.config.hw_wifipro_enable", false) && Utils.isOwnerUser()) {
                data = new SearchIndexableRaw(context);
                data.title = res.getString(2131627880);
                data.summaryOn = res.getString(2131628224);
                data.summaryOff = res.getString(2131628224);
                data.screenTitle = res.getString(2131624902);
                result.add(data);
            }
            for (AccessPoint accessPoint : WifiTracker.getCurrentAccessPoints(context, true, false, false)) {
                data = new SearchIndexableRaw(context);
                data.title = accessPoint.getSsidStr();
                data.screenTitle = res.getString(2131624902);
                data.enabled = enabled;
                result.add(data);
            }
            return result;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private boolean isDataOn;
    private HandlerThread mBgThread;
    private ActionListener mConnectListener;
    private ConnectivityManager mConnectivityManager;
    private WifiDialog mDialog;
    private AccessPoint mDlgAccessPoint;
    private boolean mEnableNextOnConnection;
    private ActionListener mForgetListener;
    private HwTelephonyManagerInner mHwTelephonyManagerInner;
    private MobileDateObserver mObserver;
    private String mOpenSsid;
    private ActionListener mSaveListener;
    private MenuItem mScanMenuItem;
    private TelephonyManager mTelephonyManager;
    private UserBadgeCache mUserBadgeCache;
    private WifiEnabler mWifiEnabler;
    private WriteWifiConfigToNfcDialog mWifiToNfcDialog;
    private long oldTime = 0;

    private class MobileDateObserver extends ContentObserver {
        private ContentResolver cr;

        public MobileDateObserver(Handler handler) {
            super(handler);
            this.cr = WifiSettings.this.getActivity().getContentResolver();
        }

        public void register() {
            this.cr.registerContentObserver(Global.getUriFor("mobile_data"), true, this, -1);
        }

        public void unregister() {
            this.cr.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            int mDataSatus = Global.getInt(WifiSettings.this.getContentResolver(), "mobile_data", 0);
            if (WifiSettings.this.mSwitchOnMoblieDataPreference == null) {
                return;
            }
            if (1 == mDataSatus) {
                WifiSettings.this.mSwitchOnMoblieDataPreference.setTitle(2131628930);
            } else {
                WifiSettings.this.mSwitchOnMoblieDataPreference.setTitle(2131628929);
            }
        }
    }

    private static class SummaryProvider extends BroadcastReceiver implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;
        private final WifiManager mWifiManager;
        private final WifiStatusTracker mWifiTracker = new WifiStatusTracker(this.mWifiManager);

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
            this.mWifiManager = (WifiManager) context.getSystemService(WifiManager.class);
        }

        private CharSequence getSummary() {
            if (!this.mWifiTracker.enabled) {
                return this.mContext.getString(2131627699);
            }
            if (this.mWifiTracker.connected) {
                return WifiInfo.removeDoubleQuotes(this.mWifiTracker.ssid);
            }
            return this.mContext.getString(2131626191);
        }

        public void setListening(boolean listening) {
            if (listening) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
                filter.addAction("android.net.wifi.STATE_CHANGE");
                filter.addAction("android.net.wifi.RSSI_CHANGED");
                this.mSummaryLoader.registerReceiver(this, filter);
            }
        }

        public void onReceive(Context context, Intent intent) {
            this.mWifiTracker.handleBroadcast(intent);
            this.mSummaryLoader.setSummary(this, getSummary());
        }
    }

    public WifiSettings() {
        super("no_config_wifi");
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (this.mHwCustWifiSettingsHwBase != null) {
            this.mHwCustWifiSettingsHwBase.setFlagForDsDisabled(getActivity().getIntent());
            if (this.mHwCustWifiSettingsHwBase.getFlagForDsDisabled()) {
                this.mHwCustWifiSettingsHwBase.initService();
            }
        }
        if (this.mHwCustWifiSettingsHwBase == null || !this.mHwCustWifiSettingsHwBase.getFlagForDsDisabled()) {
            if (this.mSetupWizardMode) {
                addPreferencesFromResource(2131230947);
            } else {
                addPreferencesFromResource(2131230946);
                if (!(SystemProperties.getBoolean("ro.config.hw_wifipro_enable", false) && Utils.isOwnerUser())) {
                    removePreference("wifi_plus_entry");
                }
            }
        }
        addPreferencesFromResource(2131230945);
        this.mUserBadgeCache = new UserBadgeCache(getPackageManager());
        this.mBgThread = new HandlerThread("WifiSettings", 10);
        this.mBgThread.start();
        setMenuClosedListener();
        this.mObserver = new MobileDateObserver(new Handler());
        this.mObserver.register();
    }

    private void setMenuClosedListener() {
        Activity activity = getActivity();
        if (activity instanceof SettingsActivity) {
            ((SettingsActivity) activity).setContextMenuClosedListener(new ContextMenuClosedListener() {
                public void onContextMenuClosed() {
                    WifiSettings.this.onAccessPointsChanged();
                }
            });
        }
    }

    public void onDestroy() {
        this.mBgThread.quit();
        setHasOptionsMenu(false);
        super.onDestroy();
        this.mObserver.unregister();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mWifiTracker = new WifiTracker(getActivity(), this, this.mBgThread.getLooper(), false, true, false);
        this.mWifiManager = this.mWifiTracker.getManager();
        this.mConnectListener = new ActionListener() {
            public void onSuccess() {
            }

            public void onFailure(int reason) {
            }
        };
        this.mSaveListener = new ActionListener() {
            public void onSuccess() {
            }

            public void onFailure(int reason) {
            }
        };
        this.mForgetListener = new ActionListener() {
            public void onSuccess() {
            }

            public void onFailure(int reason) {
            }
        };
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            this.mEnableNextOnConnection = intent.getBooleanExtra("wifi_enable_next_on_connect", false);
        }
        if (this.mEnableNextOnConnection && hasNextButton()) {
            ConnectivityManager connectivity = (ConnectivityManager) getActivity().getSystemService("connectivity");
            if (connectivity != null) {
                changeNextButtonState(connectivity.getNetworkInfo(1).isConnected());
            }
        }
        initPreferences(this.mWifiManager);
        if (this.mHwCustWifiSettingsHwBase == null) {
            this.mWifiEnabler = new WifiEnabler(getActivity(), this.mWifiSwitchPreference);
        } else if (!(this.mHwCustWifiSettingsHwBase.getFlagForDsDisabled() || this.mHwCustWifiSettingsHwBase.getFlagForWifiDisabled(this.mWifiSwitchPreference))) {
            this.mWifiEnabler = new WifiEnabler(getActivity(), this.mWifiSwitchPreference);
        }
        if (this.mHwCustWifiSettingsHwBase != null && this.mHwCustWifiSettingsHwBase.getFlagForDsDisabled()) {
            this.mHwCustWifiSettingsHwBase.setMovementMethod(getEmptyTextView());
        }
        setHasOptionsMenu(true);
        if (intent != null && intent.hasExtra("wifi_start_connect_ssid")) {
            this.mOpenSsid = intent.getStringExtra("wifi_start_connect_ssid");
            onAccessPointsChanged();
        }
        this.mHwTelephonyManagerInner = HwTelephonyManagerInner.getDefault();
        this.mTelephonyManager = (TelephonyManager) getActivity().getSystemService("phone");
        this.mConnectivityManager = (ConnectivityManager) getActivity().getSystemService("connectivity");
        initMobileDataSwitch();
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onStart() {
        super.onStart();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mHwCustWifiSettingsHwBase == null || !this.mHwCustWifiSettingsHwBase.getFlagForDsDisabled()) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        return this.mHwCustWifiSettingsHwBase.getDsDisabledView(inflater, container);
    }

    public void onResume() {
        Activity activity = getActivity();
        super.onResume();
        removePreference("dummy");
        Bundle args = getArguments();
        if (args != null) {
            args.putString(":settings:fragment_args_key", "wifi_switch");
        }
        if (UserHandle.getCallingUserId() == 0) {
            WifiExtUtils.setBeamPushUrisCallback(getActivity(), "content://huawei/wlanshare/4");
        } else {
            Log.d("WifiSettings", "/ WifiSettings:The sub UserId = " + UserHandle.getCallingUserId());
        }
        if (this.mWifiEnabler != null) {
            this.mWifiEnabler.resume();
        }
        this.mWifiTracker.startTracking();
        activity.invalidateOptionsMenu();
        if (!(this.mHwCustWifiSettingsHwBase == null || this.mHwCustWifiSettingsHwBase.isSupportStaP2pCoexist())) {
            this.mHwCustWifiSettingsHwBase.setIsConnectguiProp(true);
        }
        if (this.mConnectedConfig != null) {
            onAccessPointsChanged();
        }
        updateSwithTitle();
    }

    public void onPause() {
        super.onPause();
        if (this.mWifiEnabler != null) {
            this.mWifiEnabler.pause();
        }
        this.mWifiTracker.stopTracking();
        if (this.mHwCustWifiSettingsHwBase != null && !this.mHwCustWifiSettingsHwBase.isSupportStaP2pCoexist()) {
            this.mHwCustWifiSettingsHwBase.setIsConnectguiProp(false);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!isUiRestricted()) {
            if (this.mHwCustWifiSettingsHwBase == null || !this.mHwCustWifiSettingsHwBase.getFlagForDsDisabled()) {
                super.onCreateOptionsMenu(menu, inflater);
            }
        }
    }

    protected int getMetricsCategory() {
        return 103;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mDialog != null && this.mDialog.isShowing()) {
            outState.putInt("dialog_mode", this.mDialogMode);
            if (this.mDlgAccessPoint != null) {
                this.mAccessPointSavedState = new Bundle();
                this.mDlgAccessPoint.saveWifiState(this.mAccessPointSavedState);
                outState.putBundle("wifi_ap_state", this.mAccessPointSavedState);
            }
        }
        if (this.mWifiToNfcDialog != null && this.mWifiToNfcDialog.isShowing()) {
            Bundle savedState = new Bundle();
            this.mWifiToNfcDialog.saveState(savedState);
            outState.putBundle("wifi_nfc_dlg_state", savedState);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (isUiRestricted()) {
            return false;
        }
        switch (item.getItemId()) {
            case 1:
                showDialog(2);
                return true;
            case 2:
                showDialog(3);
                return true;
            case 3:
                handleMenuItemClick(3);
                if (this.mHwCustWifiSettingsHwBase != null && !this.mHwCustWifiSettingsHwBase.isSupportStaP2pCoexist()) {
                    return this.mHwCustWifiSettingsHwBase.changeP2pModeCoexist();
                }
                if (getActivity() instanceof SettingsActivity) {
                    ((SettingsActivity) getActivity()).startPreferencePanel(WifiP2pSettings.class.getCanonicalName(), null, 2131625037, null, this, 0);
                } else {
                    startFragment(this, WifiP2pSettings.class.getCanonicalName(), 2131625037, -1, null);
                }
                return true;
            case 5:
                handleMenuItemClick(5);
                Intent wifiIntent = new Intent("android.settings.WIFI_IP_SETTINGS");
                wifiIntent.putExtra("extra_not_ip_settings", true);
                startActivity(wifiIntent);
                return true;
            case 6:
                handleMenuItemClick(6);
                MetricsLogger.action(getActivity(), 136);
                this.mWifiTracker.forceScan();
                startScan();
                requestJlogEnable(false);
                return true;
            case 11:
                if (getActivity() instanceof SettingsActivity) {
                    ((SettingsActivity) getActivity()).startPreferencePanel(ConfigureWifiSettings.class.getCanonicalName(), null, 2131625021, null, this, 0);
                } else {
                    startFragment(this, ConfigureWifiSettings.class.getCanonicalName(), 2131625021, -1, null);
                }
                return true;
            case 14:
                startActivity(new Intent("android.intent.action.WIFI_HELP"));
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "wifi_help");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
        DrawerLayoutEx.setPreventMeasure(true);
        Preference preference = (Preference) view.getTag();
        if (preference instanceof LongPressAccessPointPreference) {
            this.mSelectedAccessPoint = ((LongPressAccessPointPreference) preference).getAccessPoint();
            menu.setHeaderTitle(this.mSelectedAccessPoint.getSsid());
            NetworkInfo networkInfo = this.mSelectedAccessPoint.getNetworkInfo();
            boolean isConnectableEx = false;
            if (networkInfo != null && this.mSelectedAccessPoint.getLevel() != -1 && this.mSelectedAccessPoint.getDetailedState() == DetailedState.SCANNING && networkInfo.getState() == State.DISCONNECTED) {
                isConnectableEx = true;
            }
            if (this.mSelectedAccessPoint.isConnectable() || isConnectableEx) {
                menu.add(0, 7, 0, 2131624941);
            }
            if (this.mSelectedAccessPoint.getDetailedState() != null && this.mWifiSettingsExt.shouldAddDisconnectMenu(getActivity().getApplicationContext())) {
                menu.add(0, 13, 0, 2131627516);
            }
            if (!isEditabilityLockedDown(getActivity(), this.mSelectedAccessPoint.getConfig())) {
                if (Utils.isOwner(getActivity()) && !this.mSelectedAccessPoint.isTempCreated() && (this.mSelectedAccessPoint.isSaved() || this.mSelectedAccessPoint.isEphemeral())) {
                    menu.add(0, 8, 0, 2131624943);
                }
                if (this.mSelectedAccessPoint.isSaved() && !this.mSelectedAccessPoint.isTempCreated()) {
                    menu.add(0, 9, 0, 2131624944);
                    NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
                    if (!(nfcAdapter == null || !nfcAdapter.isEnabled() || this.mSelectedAccessPoint.getSecurity() == 0 || this.mSelectedAccessPoint.getSecurity() == 3)) {
                        menu.add(0, 10, 0, 2131624945);
                    }
                }
                if (!(this.mSelectedAccessPoint.getNetworkId() == -1 || this.mHwCustWifiSettingsHwBase == null)) {
                    this.mHwCustWifiSettingsHwBase.remoeveModifyMenu(this.mSelectedAccessPoint, menu, 9);
                }
            } else {
                return;
            }
        }
        if (this.mHwCustWifiSettingsHwBase != null) {
            this.mHwCustWifiSettingsHwBase.custContextMenu(this.mSelectedAccessPoint, menu);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        if (this.mSelectedAccessPoint == null) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case 7:
                if (this.mSelectedAccessPoint.isSaved()) {
                    connect(this.mSelectedAccessPoint.getConfig());
                    SwitchToWifiUtils.getInstance(getActivity()).onUserConnectEvent();
                } else if (this.mSelectedAccessPoint.getSecurity() == 0) {
                    this.mSelectedAccessPoint.generateOpenNetworkConfig();
                    connect(this.mSelectedAccessPoint.getConfig());
                    SwitchToWifiUtils.getInstance(getActivity()).onUserConnectEvent();
                } else {
                    showDialog(this.mSelectedAccessPoint, 1);
                }
                return true;
            case 8:
                forget();
                SwitchToWifiUtils.getInstance(getActivity()).onUserForgetEvent();
                return true;
            case 9:
                showDialog(this.mSelectedAccessPoint, 2);
                return true;
            case 10:
                showDialog(6);
                return true;
            case 13:
                this.mWifiSettingsExt.disconnect(this.mSelectedAccessPoint.getNetworkId());
                SwitchToWifiUtils.getInstance(getActivity()).onUserDisconnectEvent();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        boolean z = false;
        handlePreferenceClick(preference);
        if (preference instanceof LongPressAccessPointPreference) {
            this.mSelectedAccessPoint = ((LongPressAccessPointPreference) preference).getAccessPoint();
            Log.d("WifiSettings", "Click " + this.mSelectedAccessPoint);
            if (this.mSelectedAccessPoint == null) {
                return false;
            }
            boolean isRecommendingAccessPoints = Secure.getInt(getContentResolver(), "wifipro_recommending_access_points", 0) == 1;
            if (this.mSelectedAccessPoint.getSecurity() == 0 && ((!this.mSelectedAccessPoint.isSaved() && !this.mSelectedAccessPoint.isActive()) || (this.mSelectedAccessPoint.isActive() && isRecommendingAccessPoints && this.mSelectedAccessPoint.isTempCreated()))) {
                this.mSelectedAccessPoint.generateOpenNetworkConfig();
                connect(this.mSelectedAccessPoint.getConfig());
                SwitchToWifiUtils.getInstance(getActivity()).onUserConnectEvent();
            } else if (this.mSelectedAccessPoint.isSaved()) {
                showDialog(this.mSelectedAccessPoint, 0);
            } else {
                showDialog(this.mSelectedAccessPoint, 1);
            }
        } else if (preference == this.mAddPreference) {
            if (this.mWifiManager.isWifiEnabled()) {
                onAddNetworkPressed();
            }
        } else if (preference != this.mSwitchOnMoblieDataPreference) {
            return super.onPreferenceTreeClick(preference);
        } else {
            long curTime = new Date().getTime();
            if (curTime - this.oldTime < 1500) {
                return super.onPreferenceTreeClick(preference);
            }
            this.oldTime = curTime;
            this.isDataOn = this.mTelephonyManager.getDataEnabled();
            if (!isTwoSimCard() || this.isDataOn) {
                TelephonyManager telephonyManager = this.mTelephonyManager;
                if (!this.isDataOn) {
                    z = true;
                }
                telephonyManager.setDataEnabled(z);
            } else {
                showChooseMoblieSimDialog();
            }
        }
        return true;
    }

    private void showDialog(AccessPoint accessPoint, int dialogMode) {
        if (accessPoint != null) {
            if (isEditabilityLockedDown(getActivity(), accessPoint.getConfig()) && accessPoint.isActive()) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(), RestrictedLockUtils.getDeviceOwner(getActivity()));
                return;
            }
        }
        if (this.mDialog != null) {
            removeDialog(1);
            this.mDialog = null;
        }
        this.mDlgAccessPoint = accessPoint;
        this.mDialogMode = dialogMode;
        this.mAccessPointSavedState = null;
        if (this.mDlgAccessPoint == null || this.mDlgAccessPoint.getNetworkId() == -1 || this.mDialogMode != 0) {
            startFragment();
        } else {
            showDialog(1);
        }
    }

    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case 1:
                AccessPoint ap = this.mDlgAccessPoint;
                if (ap == null && this.mAccessPointSavedState != null) {
                    ap = new AccessPoint(getActivity(), this.mAccessPointSavedState);
                    this.mDlgAccessPoint = ap;
                    this.mAccessPointSavedState = null;
                }
                this.mSelectedAccessPoint = ap;
                this.mDialog = new WifiDialog(getActivity(), this, ap, 0, false);
                return this.mDialog;
            case 2:
                return new WpsDialog(getActivity(), 0);
            case 3:
                return new WpsDialog(getActivity(), 1);
            case 4:
                return new Builder(getActivity()).setTitle(2131624494).setMessage(2131625016).setCancelable(false).setNegativeButton(2131625014, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        WifiSettings.this.getActivity().setResult(1);
                        WifiSettings.this.getActivity().finish();
                    }
                }).setPositiveButton(2131625015, null).create();
            case 5:
                return new Builder(getActivity()).setTitle(2131624494).setMessage(2131625017).setCancelable(false).setNegativeButton(2131625014, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        WifiSettings.this.getActivity().setResult(1);
                        WifiSettings.this.getActivity().finish();
                    }
                }).setPositiveButton(2131625015, null).create();
            case 6:
                if (this.mSelectedAccessPoint != null) {
                    this.mWifiToNfcDialog = new WriteWifiConfigToNfcDialog(getActivity(), this.mSelectedAccessPoint.getConfig().networkId, this.mSelectedAccessPoint.getSecurity(), this.mWifiManager);
                } else if (this.mWifiNfcDialogSavedState != null) {
                    this.mWifiToNfcDialog = new WriteWifiConfigToNfcDialog(getActivity(), this.mWifiNfcDialogSavedState, this.mWifiManager);
                }
                return this.mWifiToNfcDialog;
            default:
                return super.onCreateDialog(dialogId);
        }
    }

    public void onAccessPointsChanged() {
        if (getActivity() != null) {
            requestJlogEnable(false);
            if (isUiRestricted()) {
                if (!isUiRestrictedByOnlyAdmin()) {
                    addMessagePreference(2131624948);
                }
                getPreferenceScreen().removeAll();
                return;
            }
            int wifiState = this.mWifiManager.getWifiState();
            Preference locWifiHelpPref = findPreference("wifi_location_help_pref");
            switch (wifiState) {
                case 0:
                    addMessagePreference(2131624909);
                    break;
                case 1:
                    setOffMessage();
                    if (this.mScanMenuItem != null) {
                        this.mScanMenuItem.setEnabled(false);
                    }
                    invalidateOptionsMenu();
                    break;
                case 2:
                    this.mWifiSettingsExt.cleanWifiRootList(getPreferenceScreen());
                    if (locWifiHelpPref != null) {
                        getPreferenceScreen().removePreference(locWifiHelpPref);
                        break;
                    }
                    break;
                case 3:
                    Collection<AccessPoint> accessPoints = Collections.emptyList();
                    if (!this.mWifiSettingsExt.isCatogoryExist()) {
                        accessPoints = this.mWifiTracker.getAccessPoints();
                    }
                    ArrayList<AccessPoint> accessPointsList = (ArrayList) accessPoints;
                    if (!(this.mConnectedConfig == null || this.mConnectedConfig.SSID == null)) {
                        String connectedSSID = WifiInfo.removeDoubleQuotes(this.mConnectedConfig.SSID);
                        int security = WifiExtUtils.getSecurity(this.mConnectedConfig);
                        Object connectedAccessPoint = null;
                        for (AccessPoint eachAccessPoint : accessPointsList) {
                            if (connectedSSID.equals(eachAccessPoint.getSsidStr()) && eachAccessPoint.getSecurity() == security) {
                                connectedAccessPoint = eachAccessPoint;
                            }
                        }
                        if (connectedAccessPoint != null) {
                            accessPointsList.remove(connectedAccessPoint);
                            accessPointsList.add(0, connectedAccessPoint);
                        }
                    }
                    this.mConnectedConfig = null;
                    this.mWifiSettingsExt.clearWifiApList(getPreferenceScreen());
                    if (locWifiHelpPref != null) {
                        getPreferenceScreen().removePreference(locWifiHelpPref);
                    }
                    boolean hasAvailableAccessPoints = false;
                    int index = 0;
                    for (AccessPoint accessPoint : accessPoints) {
                        if (accessPoint.getLevel() != -1 || (this.mHwCustWifiSettingsHwBase != null && this.mHwCustWifiSettingsHwBase.showAllAccessPoints())) {
                            String key = accessPoint.getBssid();
                            hasAvailableAccessPoints = true;
                            Preference pref = (LongPressAccessPointPreference) getCachedPreference(key);
                            int index2;
                            if (pref != null) {
                                index2 = index + 1;
                                pref.setOrder(index);
                                this.mWifiListCategory.addPreference(pref);
                                index = index2;
                            } else {
                                LongPressAccessPointPreference preference = new LongPressAccessPointPreference(accessPoint, getPrefContext(), this.mUserBadgeCache, false, this);
                                preference.setKey(key);
                                index2 = index + 1;
                                preference.setOrder(index);
                                if (!(this.mOpenSsid == null || !this.mOpenSsid.equals(accessPoint.getSsidStr()) || accessPoint.isSaved() || accessPoint.getSecurity() == 0)) {
                                    onPreferenceTreeClick(preference);
                                    this.mOpenSsid = null;
                                }
                                this.mWifiListCategory.addPreference(preference);
                                accessPoint.setListener(this);
                                index = index2;
                            }
                        }
                    }
                    if (!hasAvailableAccessPoints) {
                        addMessagePreferenceOnly(2131624947);
                    }
                    if (this.mScanMenuItem != null) {
                        this.mScanMenuItem.setEnabled(true);
                        break;
                    }
                    break;
            }
            requestJlogEnable(true);
        }
    }

    private void setOffMessage() {
        if (this.mHwCustWifiSettingsHwBase != null && this.mHwCustWifiSettingsHwBase.getFlagForDsDisabled()) {
            addMessagePreference(2131624946);
        }
        this.mWifiSettingsExt.cleanWifiRootList(getPreferenceScreen());
        if (isUiRestricted()) {
            if (!isUiRestrictedByOnlyAdmin()) {
                addMessagePreference(2131624948);
            }
        } else if (getEmptyTextView() != null) {
            CharSequence briefText = getText(2131624946);
            boolean wifiScanningMode = Global.getInt(getActivity().getContentResolver(), "wifi_scan_always_enabled", 0) == 1;
            Preference pref = findPreference("wifi_location_help_pref");
            Log.d("WifiSettings", "setOffMessage pref = " + pref + ", wifiScanningMode = " + wifiScanningMode);
            if (pref == null) {
                if (wifiScanningMode) {
                    pref = new ScanSettingsPreference(getActivity(), getText(2131624922));
                    pref.setKey("wifi_location_help_pref");
                    getPreferenceScreen().addPreference(pref);
                }
            } else if (!wifiScanningMode) {
                getPreferenceScreen().removePreference(pref);
            }
        }
    }

    private void addMessagePreference(int messageId) {
        TextView emptyTextView = getEmptyTextView();
        if (emptyTextView != null) {
            emptyTextView.setText(messageId);
        }
        this.mWifiSettingsExt.cleanWifiRootList(getPreferenceScreen());
    }

    private void addMessagePreferenceOnly(int messageId) {
        if (getEmptyTextView() != null) {
            getEmptyTextView().setText(messageId);
        }
        this.mWifiSettingsExt.removeWifiDeviceList(getPreferenceScreen());
    }

    public void onWifiStateChanged(int state) {
        switch (state) {
            case 1:
                setOffMessage();
                if (this.mScanMenuItem != null) {
                    this.mScanMenuItem.setEnabled(false);
                }
                invalidateOptionsMenu();
                return;
            case 2:
                addMessagePreference(2131624908);
                return;
            case 3:
                startScan();
                return;
            default:
                return;
        }
    }

    public void onConnectedChanged() {
        changeNextButtonState(this.mWifiTracker.isConnected());
    }

    private void changeNextButtonState(boolean enabled) {
        if (this.mEnableNextOnConnection && hasNextButton()) {
            getNextButton().setEnabled(enabled);
        }
    }

    public void onForget(WifiDialog dialog) {
        forget();
    }

    public void onSubmit(WifiDialog dialog) {
        if (this.mDialog != null) {
            submit(this.mDialog.getController());
        }
    }

    void submit(WifiConfiguration config, boolean isEdit) {
        if (config == null) {
            if (this.mSelectedAccessPoint != null && this.mSelectedAccessPoint.isSaved()) {
                DetailedState state = this.mSelectedAccessPoint.getDetailedState();
                if (state == null) {
                    WifiConfiguration curConfig = this.mSelectedAccessPoint.getConfig();
                    if (this.mSelectedAccessPoint.getLevel() == -1 || curConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason() == 3) {
                        showDialog(this.mSelectedAccessPoint, 1);
                    } else {
                        connect(this.mSelectedAccessPoint.getNetworkId());
                        SwitchToWifiUtils.getInstance(getActivity()).onUserConnectEvent();
                    }
                } else if (WifiExt.shouldSetDisconnectButton(getActivity())) {
                    this.mWifiSettingsExt.disconnect(this.mSelectedAccessPoint.getNetworkId());
                    SwitchToWifiUtils.getInstance(getActivity()).onUserDisconnectEvent();
                } else {
                    Log.e("WifiSettings", "submit:clicked connect or disconnect button, but nothing is handled as DetailedState is incorrect, DetailedState:" + state);
                }
            }
        } else if (config.networkId == -1) {
            if (this.mSelectedAccessPoint != null) {
                connect(config);
            }
            SwitchToWifiUtils.getInstance(getActivity()).onUserConnectEvent();
        } else if (this.mSelectedAccessPoint != null) {
            this.mWifiManager.save(config, this.mSaveListener);
            if (this.mWifiManager.getConnectionInfo() != null && this.mSelectedAccessPoint.getNetworkId() == this.mWifiManager.getConnectionInfo().getNetworkId()) {
                this.mWifiManager.reassociate();
            } else if (!((ConnectivityManager) getSystemService("connectivity")).getNetworkInfo(1).isConnected()) {
                if (this.mSelectedAccessPoint.getSecurity() == 3) {
                    this.mWifiManager.connect(config, this.mConnectListener);
                } else {
                    showDialog(this.mSelectedAccessPoint, 1);
                }
                WifiExtUtils.setManualConnect(getActivity());
            }
            SwitchToWifiUtils.getInstance(getActivity()).onUserConnectEvent();
        }
        this.mWifiTracker.resumeScanning();
    }

    void forget() {
        MetricsLogger.action(getActivity(), 137);
        if (this.mSelectedAccessPoint.isSaved()) {
            this.mWifiManager.forget(this.mSelectedAccessPoint.getConfig().networkId, this.mForgetListener);
            SwitchToWifiUtils.getInstance(getActivity()).onUserForgetEvent();
        } else if (this.mSelectedAccessPoint.getNetworkInfo() == null || this.mSelectedAccessPoint.getNetworkInfo().getState() == State.DISCONNECTED) {
            Log.e("WifiSettings", "Failed to forget invalid network " + this.mSelectedAccessPoint.getConfig());
            return;
        } else {
            this.mWifiManager.disableEphemeralNetwork(AccessPoint.convertToQuotedString(this.mSelectedAccessPoint.getSsidStr()));
        }
        this.mWifiTracker.resumeScanning();
        changeNextButtonState(false);
        Index.getInstance(getActivity()).updateFromClassNameResource(SavedAccessPointsWifiSettings.class.getName(), true, true);
    }

    protected void connect(WifiConfiguration config) {
        MetricsLogger.action(getActivity(), 135);
        this.mWifiManager.connect(config, this.mConnectListener);
        WifiExtUtils.setManualConnect(getActivity());
    }

    protected void connect(int networkId) {
        MetricsLogger.action(getActivity(), 135);
        this.mWifiManager.connect(networkId, this.mConnectListener);
        WifiExtUtils.setManualConnect(getActivity());
    }

    void onAddNetworkPressed() {
        MetricsLogger.action(getActivity(), 134);
        this.mSelectedAccessPoint = null;
        showDialog(null, 1);
    }

    public void onAccessPointChanged(AccessPoint accessPoint) {
        ((LongPressAccessPointPreference) accessPoint.getTag()).refresh();
    }

    public void onLevelChanged(AccessPoint accessPoint) {
        ((LongPressAccessPointPreference) accessPoint.getTag()).onLevelChanged();
    }

    static boolean isEditabilityLockedDown(Context context, WifiConfiguration config) {
        return !canModifyNetwork(context, config);
    }

    static boolean canModifyNetwork(Context context, WifiConfiguration config) {
        boolean z = false;
        if (config == null) {
            return true;
        }
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        PackageManager pm = context.getPackageManager();
        if (pm.hasSystemFeature("android.software.device_admin") && dpm == null) {
            return false;
        }
        boolean isConfigEligibleForLockdown = false;
        if (dpm != null) {
            ComponentName deviceOwner = dpm.getDeviceOwnerComponentOnAnyUser();
            if (deviceOwner != null) {
                try {
                    isConfigEligibleForLockdown = pm.getPackageUidAsUser(deviceOwner.getPackageName(), dpm.getDeviceOwnerUserId()) == config.creatorUid;
                } catch (NameNotFoundException e) {
                }
            }
        }
        if (!isConfigEligibleForLockdown) {
            return true;
        }
        boolean isLockdownFeatureEnabled;
        if (Global.getInt(context.getContentResolver(), "wifi_device_owner_configs_lockdown", 0) != 0) {
            isLockdownFeatureEnabled = true;
        } else {
            isLockdownFeatureEnabled = false;
        }
        if (!isLockdownFeatureEnabled) {
            z = true;
        }
        return z;
    }

    private void chooseMoblieSim(int slotId) {
        HwFrameworkFactory.getHwInnerTelephonyManager().setDefaultDataSlotId(slotId);
        this.mTelephonyManager.setDataEnabled(!this.isDataOn);
    }

    private boolean isTwoSimCard() {
        boolean z = false;
        if (this.mTelephonyManager.getPhoneCount() == 1) {
            return false;
        }
        if (this.mHwTelephonyManagerInner.isCardPresent(0)) {
            z = this.mHwTelephonyManagerInner.isCardPresent(1);
        }
        return z;
    }

    private boolean isNoSimCard() {
        boolean z = false;
        if (this.mTelephonyManager.getPhoneCount() == 1) {
            if (!this.mHwTelephonyManagerInner.isCardPresent(0)) {
                z = true;
            }
            return z;
        }
        if (!(this.mHwTelephonyManagerInner.isCardPresent(0) || this.mHwTelephonyManagerInner.isCardPresent(1))) {
            z = true;
        }
        return z;
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList();
        TelephonyManager mTelephonyManager = (TelephonyManager) getActivity().getSystemService("phone");
        String OperatorName1 = mTelephonyManager.getNetworkOperatorName(0);
        String phoneNumber1 = mTelephonyManager.getLine1Number(0);
        String OperatorName2 = mTelephonyManager.getNetworkOperatorName(1);
        String phoneNumber2 = mTelephonyManager.getLine1Number(1);
        String prefTitle1 = getString(2131628159, new Object[]{Integer.valueOf(1), OperatorName1});
        String prefTitle2 = getString(2131628159, new Object[]{Integer.valueOf(2), OperatorName2});
        Map<String, Object> map = new HashMap();
        map.put("OperatorName", prefTitle1);
        map.put("phoneNumber", phoneNumber1);
        map.put("rb", Boolean.valueOf(false));
        list.add(map);
        map = new HashMap();
        map.put("OperatorName", prefTitle2);
        map.put("phoneNumber", phoneNumber2);
        map.put("rb", Boolean.valueOf(false));
        list.add(map);
        return list;
    }

    private void showChooseMoblieSimDialog() {
        Builder builder = new Builder(getActivity());
        builder.setTitle(2131628931);
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), getData(), 2130969134, new String[]{"OperatorName", "phoneNumber", "rb"}, new int[]{2131887086, 2131886842, 2131886165});
        builder.setPositiveButton(2131628932, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setSingleChoiceItems(adapter, 1, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                WifiSettings.this.chooseMoblieSim(which);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void initMobileDataSwitch() {
        SharedPreferences sf = getActivity().getSharedPreferences("show_data_switch_sp", 0);
        int matched = sf.getInt("show_data_switch", 0);
        this.isDataOn = this.mTelephonyManager.getDataEnabled();
        if ((this.isDataOn && matched == 0) || isNoSimCard() || Utils.isWifiOnly(getActivity())) {
            removePreference("switch_on_moblie_data");
            return;
        }
        Editor ed = sf.edit();
        ed.putInt("show_data_switch", 1);
        ed.commit();
    }

    private void updateSwithTitle() {
        this.isDataOn = this.mTelephonyManager.getDataEnabled();
        if (this.mSwitchOnMoblieDataPreference == null) {
            return;
        }
        if (this.isDataOn) {
            this.mSwitchOnMoblieDataPreference.setTitle(2131628930);
        } else {
            this.mSwitchOnMoblieDataPreference.setTitle(2131628929);
        }
    }
}
