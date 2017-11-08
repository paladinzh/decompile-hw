package com.android.settings.deviceinfo;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.cust.HwCustUtils;

public class UsbSettingsService extends UsbConnectedService {
    private static Intent simChangeIntent = new Intent("com.android.settings.usb.CHANGE_SIM_LIMIT");
    private boolean isChargeOnlyStarted = false;
    private boolean isFirstStarted = false;
    private Context mContext;
    private Handler mHandler;
    private HwCustUsbConnectedService mHwCustUsbConnectedService;
    private boolean mInitRun = false;
    private BroadcastReceiver mIntentReceiver;
    private boolean mIsNcmRequest = false;
    private boolean mIsRegisterSimActivateUsbObserver = false;
    private AlertDialog mMtpCautionDialog;
    private NotificationManager mNm;
    private ContentObserver mSimActiviteUsbObserver;
    private UsbBackend mUsbBackend;
    private UsbManager mUsbManager;
    private Notification mUsbModeNotification;
    private Builder mUsbNoteBuilder;
    private String[] mUsbRegexs;
    private boolean nowIsUsbTether = false;

    public UsbSettingsService() {
        simChangeIntent.setAction("com.android.settings.usb.CHANGE_SIM_LIMIT");
        simChangeIntent.setPackage("com.android.settings");
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                Log.d("UsbSettingsService", "handleMessage, msg.what = " + msg.what);
                if (msg.what == 1) {
                    if (UsbSettingsService.this.nowIsUsbTether) {
                        Log.d("UsbSettingsService", "usb_Tether,do not show usb prompt dialog");
                        return;
                    }
                    UsbSettingsService.this.refreshUsbModeNotification();
                } else if (msg.what == 2) {
                    if (UsbSettingsService.this.mIsNcmRequest) {
                        Log.d("UsbSettingsService", "NCM Request, do not show usb prompt dialog");
                        return;
                    }
                    UsbSettingsService.this.showMtpDialog();
                }
            }
        };
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (ActivityManager.getCurrentUser() != context.getUserId()) {
                    Log.d("UsbSettingsService", "There might exist two Settings'process in master user");
                    return;
                }
                String action = intent.getAction();
                Log.d("UsbSettingsService", "BroadcastReceiver, action = " + action);
                if (UsbSettingsService.this.mHwCustUsbConnectedService == null || !UsbSettingsService.this.mHwCustUsbConnectedService.custHandleUsbRestriction(UsbSettingsService.this.mContext)) {
                    if ("android.hardware.usb.action.USB_STATE".equals(action)) {
                        UsbSettingsService.this.handleUsbChanged(intent);
                    } else if ("usb_tethered".equals(action)) {
                        UsbSettingsService.this.handleUsbTether(intent);
                    } else if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
                        UsbSettingsService.this.handleLocaleChanged();
                    } else if ("android.hardware.usb.action.USB_PORT_CHANGED".equals(action)) {
                        UsbBackend backend = new UsbBackend(UsbSettingsService.this.mContext);
                        Log.d("UsbSettingsService", "ACTION_USB_PORT_CHANGED deviceConnected = " + backend.isDeviceConnected() + ", dataRole = " + backend.getCurrentDataRole());
                        if (backend.isPortConnected() || backend.isDeviceConnected()) {
                            UsbSettingsService.this.sendRefresh();
                        } else {
                            Log.i("UsbSettingsService", "ACTION_USB_PORT_CHANGED stop service!");
                            if (UsbSettingsService.this.mMtpCautionDialog != null && UsbSettingsService.this.mMtpCautionDialog.isShowing()) {
                                UsbSettingsService.this.mMtpCautionDialog.dismiss();
                            }
                            UsbSettingsService.this.mIsNcmRequest = false;
                            UsbSettingsService.this.stopSelf();
                        }
                    } else if ("com.android.settings.usb.CHANGE_STATE".equals(action)) {
                        UsbSettingsService.this.sendRefresh();
                    } else if ("com.android.settings.usb.KEYGUARD_UNLOCK".equals(action)) {
                        UsbSettingsService.this.sendRefresh();
                    } else if ("com.android.settings.usb.UPDATE_CHOOSERS".equals(action)) {
                        UsbSettingsService.this.sendRefresh();
                    }
                }
            }
        };
        this.mSimActiviteUsbObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                boolean simState = Secure.getInt(UsbSettingsService.this.getContentResolver(), "cmcc_usb_limit", 1) == 0;
                Log.i("UsbSettingsService", "mSimActivateUSB: " + simState);
                if (simState) {
                    UsbSettingsService.this.refreshUsbModeNotification();
                }
            }
        };
    }

    private void handleUsbChanged(Intent intent) {
        boolean usbConnected = intent.getBooleanExtra("connected", false);
        boolean chargingOnly = intent.getBooleanExtra("only_charging", false);
        Log.d("UsbSettingsService", "ACTION_USB_STATE, usbConnected = " + usbConnected);
        int dataRole = new UsbBackend(this.mContext).getCurrentDataRole();
        boolean deviceConnected = UsbBackend.isDeviceConnected(usbConnected, chargingOnly);
        Log.d("UsbSettingsService", "ACTION_USB_STATE usbConnected = " + usbConnected + ", chargingOnly = " + chargingOnly + ", deviceConnected = " + deviceConnected + ", dataRole = " + dataRole);
        if (deviceConnected || UsbBackend.isPortConnected(dataRole)) {
            Log.i("UsbSettingsService", "ACTION_USB_STATE start service!");
            sendRefresh();
            return;
        }
        Log.i("UsbSettingsService", "ACTION_USB_STATE stop service!");
        if (this.mHwCustUsbConnectedService != null) {
            this.mHwCustUsbConnectedService.custUsbDisconnected(this.mContext);
        }
        if (this.mMtpCautionDialog != null && this.mMtpCautionDialog.isShowing()) {
            this.mMtpCautionDialog.dismiss();
        }
        this.mIsNcmRequest = false;
        stopSelf();
    }

    public void onCreate() {
        Log.e("UsbSettingsService", "usb service onCreate!");
        this.mHwCustUsbConnectedService = (HwCustUsbConnectedService) HwCustUtils.createObj(HwCustUsbConnectedService.class, new Object[]{this});
        init();
        this.mInitRun = true;
    }

    public void onDestroy() {
        this.mUsbManager = null;
        if (this.mHwCustUsbConnectedService != null) {
            this.mHwCustUsbConnectedService.custUnRegisterUsbReceiver(this.mContext);
        }
        cancelNotification();
        unregisterReceiver(this.mIntentReceiver);
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        if (this.mIsRegisterSimActivateUsbObserver) {
            getContentResolver().unregisterContentObserver(this.mSimActiviteUsbObserver);
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private void init() {
        this.mContext = this;
        this.mUsbRegexs = ((ConnectivityManager) getSystemService("connectivity")).getTetherableUsbRegexs();
        this.mUsbManager = (UsbManager) getSystemService("usb");
        this.mNm = (NotificationManager) getSystemService("notification");
        initUsbModeNotification();
        this.mUsbBackend = new UsbBackend(this.mContext);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_PORT_CHANGED");
        filter.addAction("android.hardware.usb.action.USB_STATE");
        filter.addAction("usb_tethered");
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        filter.addAction("com.android.settings.usb.CHANGE_STATE");
        filter.addAction("com.android.settings.usb.KEYGUARD_UNLOCK");
        filter.addAction("com.android.settings.usb.UPDATE_CHOOSERS");
        registerReceiver(this.mIntentReceiver, filter);
        if (this.mHwCustUsbConnectedService != null) {
            this.mHwCustUsbConnectedService.custRegisterUsbReceiver(this.mContext);
        }
        if (isSupportUsbLimit()) {
            getContentResolver().registerContentObserver(Secure.getUriFor("cmcc_usb_limit"), true, this.mSimActiviteUsbObserver);
            this.mIsRegisterSimActivateUsbObserver = true;
        }
    }

    private CharSequence getTickerText(int mode) {
        if (isSupportUsbLimit()) {
            return getChargeOnlyTitle(this.mContext);
        }
        if (mode == 2) {
            return this.mContext.getText(2131627801);
        }
        if (mode == 4) {
            return this.mContext.getText(2131627802);
        }
        if (mode == 1) {
            return this.mContext.getText(2131628392);
        }
        if (mode == 6) {
            return this.mContext.getText(2131628393);
        }
        if (mode != 0) {
            Log.w("UsbSettingsService", "Invalid USB mode = " + mode);
        }
        return this.mContext.getText(2131627464);
    }

    private void cancelNotification() {
        if (this.mNm != null) {
            this.mNm.cancel(null, 1000002);
        }
    }

    private void showNotification() {
        if (this.mNm != null && this.mUsbNoteBuilder != null) {
            this.mNm.notify(null, 1000002, this.mUsbNoteBuilder.build());
        }
    }

    private void handleUsbTether(Intent intent) {
        String strIntent = intent.getStringExtra("usb_tethered_type");
        if ("usb_tethered_open".equals(strIntent)) {
            cancelNotification();
            this.nowIsUsbTether = true;
        } else if ("usb_tethered_close".equals(strIntent)) {
            showNotification();
            this.nowIsUsbTether = false;
        }
    }

    private void handleLocaleChanged() {
        cancelNotification();
        refreshUsbModeNotification();
    }

    private void startSimUsbLimitActivity(Context context) {
        Intent helpIntent = new Intent("com.android.huawei.SIM_USB_LIMIT");
        helpIntent.setFlags(268435456);
        context.startActivity(helpIntent);
    }

    private CharSequence getChargeOnlyTitle(Context mContext) {
        return mContext.getText(2131628026);
    }

    private boolean isSupportUsbLimit() {
        return UsbConnUtils.isSupportUsbLimit();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean z = false;
        if (intent != null) {
            z = intent.getBooleanExtra("is_from_usb_receiver", false);
            this.isChargeOnlyStarted = intent.getBooleanExtra("is_charge_only", false);
            if (intent.getBooleanExtra("ncm_requested", false)) {
                this.mIsNcmRequest = true;
            }
        }
        if (!this.isFirstStarted) {
            if (isSupportUsbLimit() && r0) {
                startSimUsbLimitActivity(this.mContext);
            } else {
                this.mHandler.removeMessages(2);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), 1000);
            }
            this.isFirstStarted = true;
        }
        if (this.mInitRun) {
            refreshUsbModeNotification();
            this.mInitRun = false;
        }
        return 0;
    }

    private int getContentTitle(int mode) {
        if (mode == 2) {
            return 2131628388;
        }
        if (mode == 4) {
            return 2131628387;
        }
        if (mode == 1) {
            return 2131628389;
        }
        if (mode == 6) {
            return 2131628390;
        }
        if (mode != 0) {
            Log.w("UsbSettingsService", "Invalid USB mode = " + mode);
        }
        return 2131628386;
    }

    private void initUsbModeNotification() {
        this.mUsbNoteBuilder = new Builder(this.mContext);
        this.mUsbNoteBuilder.setSmallIcon(33751155);
        this.mUsbNoteBuilder.setOngoing(true);
        this.mUsbNoteBuilder.setPriority(1);
        this.mUsbModeNotification = this.mUsbNoteBuilder.build();
    }

    private void refreshUsbModeNotification() {
        UsbBackend backend = new UsbBackend(this.mContext);
        int mode = getValidMode(backend, backend.getCurrentMode());
        this.mUsbNoteBuilder.setTicker(getTickerText(mode));
        this.mUsbNoteBuilder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, isSupportUsbLimit() ? UsbConnUtils.getSimLimitHelpIntent() : UsbConnUtils.getModeChooserIntent(), 134217728));
        this.mUsbNoteBuilder.setContentTitle(this.mContext.getString(getContentTitle(mode)));
        this.mUsbNoteBuilder.setContentText(this.mContext.getString(2131628385));
        this.mUsbModeNotification = this.mUsbNoteBuilder.build();
        this.mNm.notify(null, 1000002, this.mUsbModeNotification);
    }

    private void sendRefresh() {
        this.mHandler.removeMessages(1);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), 700);
    }

    private int getValidMode(UsbBackend backend, int mode) {
        if (UsbBackend.isConnModeValid(mode)) {
            return mode;
        }
        boolean deviceConnected = backend.isDeviceConnected();
        boolean hostConnected = backend.isHostConnected();
        Log.w("UsbSettingsService", "detected invalid usb mode = " + mode + ", deviceConnected = " + deviceConnected + ", hostConnected = " + hostConnected);
        if (!deviceConnected && hostConnected) {
            return 1;
        }
        return 0;
    }

    private void showMtpDialog() {
        if (UsbConnUtils.isMtpDialogEnabled(this.mContext)) {
            int themeId = getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
            if (themeId != 0) {
                setTheme(themeId);
            }
            if (getValidMode(this.mUsbBackend, this.mUsbBackend.getCurrentMode()) == 0) {
                if (this.mMtpCautionDialog == null) {
                    this.mMtpCautionDialog = UsbConnUtils.createMtpCautionDialog(this.mContext, new OnClickListener() {
                        public void onClick(View v) {
                            if (UsbSettingsService.this.mMtpCautionDialog != null) {
                                UsbSettingsService.this.mMtpCautionDialog.dismiss();
                            }
                            if (UsbSettingsService.this.mIsNcmRequest) {
                                Log.i("UsbSettingsService", "mIsNcmRequest is true, ignore click under mirrorlink!");
                                return;
                            }
                            UsbSettingsService.this.mUsbBackend.setMode(2);
                            UsbSettingsService.this.mContext.sendBroadcast(UsbConnUtils.getUpdateChooserIntent());
                        }
                    }, new OnClickListener() {
                        public void onClick(View v) {
                            if (UsbSettingsService.this.mMtpCautionDialog != null) {
                                UsbSettingsService.this.mMtpCautionDialog.dismiss();
                            }
                        }
                    });
                } else if (this.mMtpCautionDialog.isShowing()) {
                    this.mMtpCautionDialog.dismiss();
                }
                this.mMtpCautionDialog.show();
            }
        }
    }
}
