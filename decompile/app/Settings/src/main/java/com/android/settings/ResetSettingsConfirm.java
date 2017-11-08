package com.android.settings;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.INotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver.Stub;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.net.NetworkPolicyManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.settings.bluetooth.Utils;
import com.android.settings.fuelgauge.PowerWhitelistBackend;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.display.DisplayDensityUtils;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

public class ResetSettingsConfirm extends OptionsMenuFragment {
    private static final String[] BROADCAST_SENDER = new String[]{"com.huawei.android.hwouc", "com.huawei.vassistant", "com.huawei.motionservice", "com.huawei.android.thememanager"};
    private static final Uri DEFAULTAPN_URI = Uri.parse("content://telephony/carriers/restore");
    private static final Uri URL_RESTOREAPN_USING_SUBID = Uri.parse("content://telephony/carriers/restore/subId");
    private ClearUserDataObserver mClearDataObserver;
    private byte mCompleteFlag = (byte) 0;
    private View mContentView;
    private Context mContext;
    private OnClickListener mFinalClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (!Utils.isMonkeyRunning()) {
                ResetSettingsConfirm.this.showProgress();
                ResetSettingsConfirm.this.mNeedReboot = false;
                ResetSettingsConfirm.this.factoryResetSettings(ResetSettingsConfirm.this.mContext);
            }
        }
    };
    private Handler mHandler = new Handler();
    private boolean mIsShowingProgress = false;
    private boolean mNeedReboot = false;
    private ProgressDialog mProgressDialog;
    private BroadcastReceiver mResetResultReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("com.android.huawei.RESTORE_SETTINGS_RESULT")) {
                String packageName = intent.getStringExtra("package_name");
                String result = intent.getStringExtra("restore_result");
                int serialNumber = intent.getIntExtra("serial_number", 0);
                if ("ok".equalsIgnoreCase(result) && serialNumber == ResetSettingsConfirm.this.mSerialNumber) {
                    int index = Arrays.asList(ResetSettingsConfirm.BROADCAST_SENDER).indexOf(packageName);
                    if (index >= 0 && index <= 7) {
                        ResetSettingsConfirm resetSettingsConfirm = ResetSettingsConfirm.this;
                        resetSettingsConfirm.mCompleteFlag = (byte) (resetSettingsConfirm.mCompleteFlag | (1 << index));
                        if (ResetSettingsConfirm.this.mCompleteFlag == (byte) 15) {
                            ResetSettingsConfirm.this.dismissProgress();
                            if (ResetSettingsConfirm.this.mNeedReboot) {
                                ResetSettingsConfirm.this.mNeedReboot = false;
                                ResetSettingsConfirm.this.rebootSystem();
                            } else {
                                Toast.makeText(context, 2131628278, 0).show();
                            }
                        }
                    }
                }
            }
        }
    };
    private int mSerialNumber = -1;
    private Runnable mTimeoutRunnable = new Runnable() {
        public void run() {
            ResetSettingsConfirm.this.checkResponseAndShowLog();
            ResetSettingsConfirm.this.dismissProgress();
            if (ResetSettingsConfirm.this.mNeedReboot) {
                ResetSettingsConfirm.this.mNeedReboot = false;
                ResetSettingsConfirm.this.rebootSystem();
            }
        }
    };

    class ClearUserDataObserver extends Stub {
        ClearUserDataObserver() {
        }

        public void onRemoveCompleted(String packageName, boolean succeeded) {
            Log.d("Remove user data succeed: " + succeeded + ". Pkg name is: ", packageName);
        }
    }

    private static class PokeServicesWorker extends AsyncTask<Void, Void, Void> {
        private PokeServicesWorker() {
        }

        protected Void doInBackground(Void... params) {
            String[] services = ServiceManager.listServices();
            if (services == null) {
                return null;
            }
            for (String service : services) {
                IBinder obj = ServiceManager.checkService(service);
                if (obj != null) {
                    Parcel data = Parcel.obtain();
                    try {
                        obj.transact(1599295570, data, null, 0);
                    } catch (RemoteException e) {
                    } catch (Exception e2) {
                        Log.i("ResetSettingsConfirm", "Someone wrote a bad service '" + service + "' that doesn't like to be poked: " + e2);
                    }
                    data.recycle();
                }
            }
            return null;
        }
    }

    private static class SerialGenerator extends SecureRandom {
        static final long serialVersionUID = 100;

        private SerialGenerator() {
        }

        public int nextInt(int bits) {
            return next(bits);
        }

        public static int generateSerialNumber() {
            return new SerialGenerator().nextInt(32);
        }
    }

    private void showProgress() {
        if (this.mProgressDialog == null) {
            this.mProgressDialog = new ProgressDialog(getActivity());
            this.mProgressDialog.setMessage(getResources().getString(2131628280));
            this.mProgressDialog.setProgressStyle(0);
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
        }
        if (!this.mProgressDialog.isShowing()) {
            this.mProgressDialog.show();
            this.mIsShowingProgress = true;
        }
        this.mHandler.postDelayed(this.mTimeoutRunnable, 15000);
    }

    private void dismissProgress() {
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
            this.mProgressDialog = null;
            this.mIsShowingProgress = false;
        }
    }

    private void rebootSystem() {
        if (this.mContext != null) {
            Intent reboot = new Intent("android.intent.action.REBOOT");
            reboot.putExtra("android.intent.extra.KEY_CONFIRM", false);
            reboot.setFlags(268435456);
            this.mContext.startActivity(reboot);
        }
    }

    private void checkResponseAndShowLog() {
        int appCount = BROADCAST_SENDER.length;
        for (int idx = 0; idx < appCount; idx++) {
            if ((this.mCompleteFlag & (1 << idx)) == 0) {
                MLog.e("ResetSettingsConfirm", BROADCAST_SENDER[idx] + " no response!");
            }
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (this.mIsShowingProgress) {
            showProgress();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.huawei.RESTORE_SETTINGS_RESULT");
        this.mContext.getApplicationContext().registerReceiver(this.mResetResultReceiver, intentFilter);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        dismissProgress();
        this.mContext.getApplicationContext().unregisterReceiver(this.mResetResultReceiver);
    }

    public void onDetach() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            dismissProgress();
            this.mIsShowingProgress = true;
        }
        super.onDetach();
    }

    private void establishFinalConfirmationState() {
        this.mContentView.findViewById(2131887085).setOnClickListener(this.mFinalClickListener);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mContentView = inflater.inflate(2130969057, null);
        establishFinalConfirmationState();
        return this.mContentView;
    }

    protected int getMetricsCategory() {
        return 84;
    }

    private void factoryResetSettings(final Context context) {
        Context ctx = context;
        this.mSerialNumber = SerialGenerator.generateSerialNumber();
        new AsyncTask<Void, Void, Boolean>() {
            protected Boolean doInBackground(Void... params) {
                if (ResetSettingsConfirm.this.mClearDataObserver == null) {
                    ResetSettingsConfirm.this.mClearDataObserver = new ClearUserDataObserver();
                }
                ActivityManager am = (ActivityManager) context.getSystemService("activity");
                am.clearApplicationUserData("com.huawei.systemmanager", ResetSettingsConfirm.this.mClearDataObserver);
                am.clearApplicationUserData("com.huawei.vassistant", ResetSettingsConfirm.this.mClearDataObserver);
                am.clearApplicationUserData("com.android.inputmethod.latin", ResetSettingsConfirm.this.mClearDataObserver);
                am.clearApplicationUserData("com.huawei.android.launcher", ResetSettingsConfirm.this.mClearDataObserver);
                am.clearApplicationUserData("com.android.providers.userdictionary", ResetSettingsConfirm.this.mClearDataObserver);
                am.clearApplicationUserData("com.android.cellbroadcastreceiver", ResetSettingsConfirm.this.mClearDataObserver);
                ResetSettingsConfirm.this.resetAppsSettings(context);
                ResetSettingsConfirm.this.changeToDefaultUIStyle(context);
                ResetSettingsConfirm.this.restoreApnSettings();
                Utils.resetBtAndWifiP2pDeviceName(context);
                return Boolean.valueOf(Utils.factoryReset(context, "99"));
            }

            protected void onPostExecute(Boolean result) {
                Global.putInt(context.getContentResolver(), "debug_view_attributes", 0);
                new PokeServicesWorker().execute(new Void[0]);
                ResetSettingsConfirm.this.mNeedReboot = result.booleanValue();
                PowerWhitelistBackend.getInstance().refreshList();
                ResetSettingsConfirm.this.turnOffBluetooth(context);
                DisplayDensityUtils.clearForcedDisplayDensity(0);
                Intent intent = new Intent("com.android.huawei.RESTORE_DEFAULT_SETTINGS");
                intent.putExtra("restore_step", 1);
                intent.putExtra("serial_number", ResetSettingsConfirm.this.mSerialNumber);
                context.sendBroadcast(intent);
            }
        }.execute(new Void[0]);
        if (context != null) {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putString("pref_key_agps_profile_selected", "GOOGLE");
            editor.putString("bt_discoverable_timeout", "twomin");
            editor.putInt("bt_discoverable_timeout_number", 120);
            editor.apply();
        }
    }

    private void changeToDefaultUIStyle(Context ctx) {
        int defaultUIStyle = SystemProperties.getInt("ro.config.hw_simpleui_enable", 1);
        ComponentName normalUI = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.unihome.UniHomeLauncher");
        ComponentName drawerUI = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.drawer.DrawerLauncher");
        ComponentName simpleUI = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.simpleui.SimpleUILauncher");
        PackageManager pm = ctx.getPackageManager();
        switch (defaultUIStyle) {
            case 1:
                pm.setComponentEnabledSetting(drawerUI, 2, 0);
                pm.setComponentEnabledSetting(simpleUI, 2, 0);
                pm.setComponentEnabledSetting(normalUI, 1, 0);
                break;
            case 2:
                pm.setComponentEnabledSetting(normalUI, 2, 0);
                pm.setComponentEnabledSetting(drawerUI, 2, 0);
                pm.setComponentEnabledSetting(simpleUI, 1, 0);
                break;
            case 4:
                pm.setComponentEnabledSetting(normalUI, 2, 0);
                pm.setComponentEnabledSetting(simpleUI, 2, 0);
                pm.setComponentEnabledSetting(drawerUI, 1, 0);
                break;
            default:
                Log.e("ResetSettingsConfirm", "Invalid default UI style configuration!");
                return;
        }
        LauncherModeSettingsActivity.updateConfiguration(defaultUIStyle);
    }

    private void restoreApnSettings() {
        if (this.mContext != null && !Utils.isWifiOnly(this.mContext)) {
            ContentResolver resolver = this.mContext.getContentResolver();
            for (int subId : new int[]{0, 1}) {
                Uri uri = DEFAULTAPN_URI;
                if (TelephonyManager.getDefault().isMultiSimEnabled() && !SystemProperties.getBoolean("ro.config.mtk_platform_apn", false)) {
                    uri = ContentUris.withAppendedId(URL_RESTOREAPN_USING_SUBID, (long) subId);
                }
                resolver.delete(uri, null, null);
            }
        }
    }

    private void turnOffBluetooth(Context ctx) {
        if (ctx != null) {
            LocalBluetoothManager manager = Utils.getLocalBtManager(ctx);
            if (manager == null) {
                MLog.w("ResetSettingsConfirm", "bluetooth is not supported!");
            } else {
                manager.getBluetoothAdapter().setBluetoothEnabled(false);
            }
        }
    }

    private void resetAppsSettings(Context ctx) {
        if (ctx != null) {
            PackageManager pm = ctx.getPackageManager();
            INotificationManager nm = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
            IPackageManager ipm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
            List<ApplicationInfo> apps = pm.getInstalledApplications(512);
            for (int i = 0; i < apps.size(); i++) {
                ApplicationInfo app = (ApplicationInfo) apps.get(i);
                try {
                    nm.setNotificationsEnabledForPackage(app.packageName, app.uid, true);
                } catch (RemoteException e) {
                }
                if (!app.enabled && pm.getApplicationEnabledSetting(app.packageName) == 3) {
                    pm.setApplicationEnabledSetting(app.packageName, 0, 1);
                }
            }
            try {
                ipm.resetApplicationPreferences(UserHandle.myUserId());
            } catch (RemoteException e2) {
            }
            ((AppOpsManager) ctx.getSystemService("appops")).resetAllModes();
            NetworkPolicyManager npm = NetworkPolicyManager.from(ctx);
            int[] restrictedUids = npm.getUidsWithPolicy(1);
            int currentUserId = ActivityManager.getCurrentUser();
            for (int uid : restrictedUids) {
                if (UserHandle.getUserId(uid) == currentUserId) {
                    npm.setUidPolicy(uid, 0);
                }
            }
        }
    }
}
