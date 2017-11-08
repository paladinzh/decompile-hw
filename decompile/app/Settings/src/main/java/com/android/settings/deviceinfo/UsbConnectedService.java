package com.android.settings.deviceinfo;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RemoteViews;
import com.android.settings.Utils;
import com.huawei.cust.HwCustUtils;

public class UsbConnectedService extends Service {
    private boolean isChargeOnlyStarted = false;
    private boolean isFirstStarted = false;
    private AlertDialog mCautionDialog;
    private Intent mChooseMtpIntent;
    private Intent mChoosePtpIntent;
    private int mChooseType = 0;
    private Context mContext;
    private Intent mDeactiveMtpIntent;
    private Intent mDeactivePtpIntent;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d("UsbConnectedService", "handleMessage, msg.what = " + msg.what);
            if (msg.what == 1 || msg.what == 2) {
                UsbConnectedService.this.initRefreshNotification();
            }
        }
    };
    private HwCustUsbConnectedService mHwCustUsbConnectedService;
    private boolean mInitRun = false;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("UsbConnectedService", "BroadcastReceiver, action = " + action);
            if (UsbConnectedService.this.mHwCustUsbConnectedService == null || !UsbConnectedService.this.mHwCustUsbConnectedService.custHandleUsbRestriction(UsbConnectedService.this.mContext)) {
                if ("android.hardware.usb.action.USB_STATE".equals(action)) {
                    boolean usbConnected = intent.getBooleanExtra("connected", false);
                    boolean chargingOnly = intent.getBooleanExtra("only_charging", false);
                    Log.d("UsbConnectedService", "ACTION_USB_STATE, usbConnected = " + usbConnected);
                    if (UsbConnectedService.this.needStop(usbConnected, chargingOnly)) {
                        if (UsbConnectedService.this.mHwCustUsbConnectedService != null) {
                            UsbConnectedService.this.mHwCustUsbConnectedService.custUsbDisconnected(UsbConnectedService.this.mContext);
                        }
                        if (UsbConnectedService.this.mCautionDialog != null && UsbConnectedService.this.mCautionDialog.isShowing()) {
                            UsbConnectedService.this.mCautionDialog.dismiss();
                        }
                        UsbConnectedService.this.stopSelf();
                    }
                } else if ("usb_tethered".equals(action)) {
                    UsbConnectedService.this.handleUsbTether(intent);
                } else if ("usb_connected_choose_ptp".equals(action)) {
                    if (!Utils.isMonkeyRunning()) {
                        UsbConnectedService.this.isChargeOnlyStarted = false;
                        UsbConnectedService.this.handlePtpMode();
                    }
                } else if ("usb_connected_choose_mtp".equals(action)) {
                    if (!Utils.isMonkeyRunning()) {
                        UsbConnectedService.this.isChargeOnlyStarted = false;
                        UsbConnectedService.this.handleMtpMode();
                    }
                } else if ("android.intent.action.LOCALE_CHANGED".equals(action)) {
                    UsbConnectedService.this.handleLocaleChanged();
                } else if ("usb_connected_deactive_mtp".equals(action)) {
                    if (!Utils.isMonkeyRunning()) {
                        UsbConnectedService.this.isChargeOnlyStarted = false;
                        UsbConnectedService.this.handleMtpDeactived();
                    }
                } else if ("usb_connected_deactive_ptp".equals(action)) {
                    if (!Utils.isMonkeyRunning()) {
                        UsbConnectedService.this.isChargeOnlyStarted = false;
                        UsbConnectedService.this.handlePtpDeactived();
                    }
                } else if ("com.android.settings.usb.KEYGUARD_UNLOCK".equals(action)) {
                    Log.d("UsbConnectedService", "Keyguard unlock!");
                    UsbConnectedService.this.mHandler.removeMessages(2);
                    UsbConnectedService.this.mHandler.sendMessageDelayed(UsbConnectedService.this.mHandler.obtainMessage(2), 200);
                }
            }
        }
    };
    private boolean mIsRegisterSimActiviteUSBObserver = false;
    private boolean mMtpCmdSent = false;
    private NotificationManager mNm;
    private Notification mNotification;
    private boolean mPtpCmdSent = false;
    private RemoteViews mRemoteView;
    private ContentObserver mSimActiviteUSBObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            boolean simState = Secure.getInt(UsbConnectedService.this.getContentResolver(), "cmcc_usb_limit", 1) == 0;
            Log.i("UsbConnectedService", "mSimActiviteUSB: " + simState);
            if (simState) {
                UsbConnectedService.this.initRefreshNotification();
                UsbConnectedService.this.handleMtpMode();
                UsbConnectedService.this.refreshNotification();
            }
        }
    };
    private RemoteViews mTwoBtnView;
    private UsbManager mUsbManager;
    private String[] mUsbRegexs;

    public void onCreate() {
        super.onCreate();
        this.mHwCustUsbConnectedService = (HwCustUsbConnectedService) HwCustUtils.createObj(HwCustUsbConnectedService.class, new Object[]{this});
        init();
        this.mInitRun = true;
        int themeId = getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        if (themeId != 0) {
            setTheme(themeId);
        }
    }

    public void onDestroy() {
        this.mUsbManager = null;
        if (this.mHwCustUsbConnectedService != null) {
            this.mHwCustUsbConnectedService.custUnRegisterUsbReceiver(this.mContext);
        }
        this.mContext = null;
        this.mNm.cancel(1000001);
        this.mNm = null;
        this.mUsbRegexs = null;
        this.mDeactivePtpIntent = null;
        this.mDeactiveMtpIntent = null;
        this.mChoosePtpIntent = null;
        this.mChooseMtpIntent = null;
        unregisterReceiver(this.mIntentReceiver);
        this.mIntentReceiver = null;
        this.mHandler.removeMessages(1);
        this.mRemoteView = null;
        this.mTwoBtnView = null;
        this.mNotification = null;
        if (this.mIsRegisterSimActiviteUSBObserver) {
            getContentResolver().unregisterContentObserver(this.mSimActiviteUSBObserver);
            this.mSimActiviteUSBObserver = null;
            this.mIsRegisterSimActiviteUSBObserver = false;
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private Context getContext() {
        return this;
    }

    private void init() {
        this.mContext = getContext();
        this.mUsbRegexs = ((ConnectivityManager) getSystemService("connectivity")).getTetherableUsbRegexs();
        this.mUsbManager = (UsbManager) getSystemService("usb");
        this.mRemoteView = new RemoteViews(this.mContext.getPackageName(), 2130969232);
        this.mTwoBtnView = new RemoteViews(this.mContext.getPackageName(), 2130969233);
        this.mNm = (NotificationManager) getSystemService("notification");
        this.mNotification = new Builder(this.mContext).build();
        this.mChooseMtpIntent = new Intent("usb_connected_choose_mtp");
        this.mChoosePtpIntent = new Intent("usb_connected_choose_ptp");
        this.mChooseMtpIntent.setPackage("com.android.settings");
        this.mChoosePtpIntent.setPackage("com.android.settings");
        this.mDeactiveMtpIntent = new Intent("usb_connected_deactive_mtp");
        this.mDeactivePtpIntent = new Intent("usb_connected_deactive_ptp");
        this.mDeactiveMtpIntent.setPackage("com.android.settings");
        this.mDeactivePtpIntent.setPackage("com.android.settings");
        IntentFilter filter = new IntentFilter();
        filter.addAction("usb_connected_choose_mtp");
        filter.addAction("usb_connected_choose_ptp");
        filter.addAction("android.hardware.usb.action.USB_STATE");
        filter.addAction("usb_tethered");
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        filter.addAction("usb_connected_deactive_mtp");
        filter.addAction("usb_connected_deactive_ptp");
        filter.addAction("com.android.settings.usb.KEYGUARD_UNLOCK");
        registerReceiver(this.mIntentReceiver, filter);
        if (this.mHwCustUsbConnectedService != null) {
            this.mHwCustUsbConnectedService.custRegisterUsbReceiver(this.mContext);
        }
        if (isSupportUsbLimit()) {
            getContentResolver().registerContentObserver(Secure.getUriFor("cmcc_usb_limit"), true, this.mSimActiviteUSBObserver);
            this.mIsRegisterSimActiviteUSBObserver = true;
        }
    }

    void initRefreshNotification() {
        if (this.mRemoteView != null) {
            this.mRemoteView.addView(2131887351, this.mTwoBtnView);
            this.mRemoteView.setOnClickPendingIntent(2131887356, PendingIntent.getBroadcast(this.mContext, 0, this.mChooseMtpIntent, 134217728));
            this.mRemoteView.setOnClickPendingIntent(2131887354, PendingIntent.getBroadcast(this.mContext, 0, this.mChoosePtpIntent, 134217728));
            this.mRemoteView.setOnClickPendingIntent(2131887355, PendingIntent.getBroadcast(this.mContext, 0, this.mDeactiveMtpIntent, 134217728));
            this.mRemoteView.setOnClickPendingIntent(2131887353, PendingIntent.getBroadcast(this.mContext, 0, this.mDeactivePtpIntent, 134217728));
            this.mRemoteView.setImageViewResource(2131887346, 2130838687);
            this.mRemoteView.setTextViewText(2131887347, this.mContext.getText(2131627800));
            this.mRemoteView.setTextViewText(2131887348, this.mContext.getText(2131627803));
            initRefreshLastNotification();
            this.mNotification.largeIcon = BitmapFactory.decodeResource(this.mContext.getResources(), 2130838687);
            this.mNotification.icon = 17303250;
            this.mNotification.tickerText = getTickerText();
            this.mNotification.flags = 2;
            this.mNotification.priority = 1;
            this.mNotification.bigContentView = this.mRemoteView;
            this.mNotification.extras = Utils.getNotificationThemeData(2130838688, 2131887346, 4, 15);
            if (isSupportUsbLimit()) {
                initControlAndStartSimUsbLimitActivity(this.mRemoteView, this.mContext);
            }
            try {
                this.mNm.notify(1000001, this.mNotification);
            } catch (Exception e) {
                if (this.mNotification.getSmallIcon() == null) {
                    Log.w("UsbConnectedService", "UsbConnectedService-->initRefreshNotification()-->mNotification Smaill Icon is null !!");
                }
                Log.e("UsbConnectedService", "UsbConnectedService-->initRefreshNotification()-->Exception : " + e);
            }
        }
    }

    private CharSequence getTickerText() {
        if (isSupportUsbLimit()) {
            return getChargeOnlyTitle(this.mContext);
        }
        String function = getDefaultFunction();
        if ("mtp".equals(function) || "hisuite".equals(function)) {
            return this.mContext.getText(2131627801);
        }
        if ("ptp".equals(function)) {
            return this.mContext.getText(2131627802);
        }
        return this.mContext.getText(2131627464);
    }

    private void initRefreshLastNotification() {
        updateButtons(getDefaultFunction());
    }

    private void updateButtons(String function) {
        Log.d("UsbConnectedService", "updateButtons, function = " + function);
        this.mRemoteView.setTextViewText(2131887348, this.mContext.getText(2131627899));
        if ("mtp".equals(function) || "hisuite".equals(function)) {
            updateHelpBtn("usb_connected_mtp_help");
            updateFunctionBtn(2131887354, 0, 2131887353, 8);
            updateFunctionBtn(2131887356, 8, 2131887355, 0);
            this.mMtpCmdSent = true;
        } else if ("ptp".equals(function)) {
            updateHelpBtn("usb_connected_ptp_help");
            updateFunctionBtn(2131887354, 8, 2131887353, 0);
            updateFunctionBtn(2131887356, 0, 2131887355, 8);
            this.mPtpCmdSent = true;
        } else if (!"midi".equals(function)) {
            this.mRemoteView.setTextViewText(2131887348, this.mContext.getText(2131627464));
            updateAllBtnsNormal();
        }
        if (((UserManager) this.mContext.getSystemService("user")).hasUserRestriction("no_usb_file_transfer")) {
            updateFunctionBtn(2131887354, 8, 2131887353, 8);
            this.mPtpCmdSent = true;
            updateFunctionBtn(2131887356, 8, 2131887355, 8);
            this.mMtpCmdSent = true;
        }
    }

    void refreshNotification() {
        switch (this.mChooseType) {
            case 1:
                updateHelpBtn("usb_connected_ptp_help");
                updateFunctionBtn(2131887354, 8, 2131887353, 0);
                updateFunctionBtn(2131887356, 0, 2131887355, 8);
                this.mRemoteView.setTextViewText(2131887348, this.mContext.getText(2131627899));
                this.mNotification.tickerText = this.mContext.getText(2131627802);
                this.mNotification.bigContentView = this.mRemoteView;
                break;
            case 2:
                updateHelpBtn("usb_connected_mtp_help");
                updateFunctionBtn(2131887356, 8, 2131887355, 0);
                updateFunctionBtn(2131887354, 0, 2131887353, 8);
                this.mRemoteView.setTextViewText(2131887348, this.mContext.getText(2131627899));
                this.mNotification.tickerText = this.mContext.getText(2131627801);
                this.mNotification.bigContentView = this.mRemoteView;
                break;
        }
        try {
            this.mNm.notify(1000001, this.mNotification);
        } catch (Exception e) {
            if (this.mNotification.getSmallIcon() == null) {
                Log.w("UsbConnectedService", "UsbConnectedService-->refreshNotification()-->mNotification Smaill Icon is null !!");
            }
            Log.e("UsbConnectedService", "UsbConnectedService-->refreshNotification()-->Exception : " + e);
        }
    }

    private void updateFunctionBtn(int normalViewId, int normalVisibility, int selectedViewId, int selectedVisibility) {
        if (this.mRemoteView != null) {
            this.mRemoteView.setViewVisibility(normalViewId, normalVisibility);
            this.mRemoteView.setViewVisibility(selectedViewId, selectedVisibility);
        }
    }

    private void updateHelpBtn(String typeOfHelp) {
        if (this.mRemoteView != null) {
            if (typeOfHelp != null) {
                Intent typeIntent = new Intent(this.mContext, UsbConnectedHelp.class);
                typeIntent.putExtra("usb_connected_help_type", typeOfHelp);
                typeIntent.setFlags(268435456);
                PendingIntent helpIntent = PendingIntent.getActivity(this.mContext, 0, typeIntent, 134217728);
                if (this.mHwCustUsbConnectedService != null && this.mHwCustUsbConnectedService.isHideHisuiteSupport()) {
                    helpIntent = null;
                }
                this.mRemoteView.setOnClickPendingIntent(2131886316, helpIntent);
            } else {
                this.mRemoteView.setOnClickPendingIntent(2131886316, null);
            }
        }
    }

    private void cancelNotification() {
        if (this.mNm != null) {
            this.mNm.cancel(1000001);
        }
    }

    private void showNotification() {
        if (this.mNm != null && this.mNotification != null) {
            try {
                this.mNm.notify(1000001, this.mNotification);
            } catch (Exception e) {
                if (this.mNotification.getSmallIcon() == null) {
                    Log.w("UsbConnectedService", "UsbConnectedService-->showNotification()-->mNotification Smaill Icon is null !!");
                }
                Log.e("UsbConnectedService", "UsbConnectedService-->showNotification()-->Exception : " + e);
            }
        }
    }

    private void handleUsbTether(Intent intent) {
        String strIntent = intent.getStringExtra("usb_tethered_type");
        if ("usb_tethered_open".equals(strIntent)) {
            cancelNotification();
        } else if ("usb_tethered_close".equals(strIntent)) {
            showNotification();
        }
    }

    void handleMtpMode() {
        this.mChooseType = 2;
        refreshNotification();
        this.mPtpCmdSent = false;
        if (!this.mMtpCmdSent) {
            this.mUsbManager.setCurrentFunction("hisuite,mtp,mass_storage");
            this.mUsbManager.setUsbDataUnlocked(true);
            setUsbUnTether();
            this.mMtpCmdSent = true;
            this.mHandler.removeMessages(1);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), 800);
        }
    }

    private void handlePtpMode() {
        this.mChooseType = 1;
        refreshNotification();
        this.mMtpCmdSent = false;
        if (!this.mPtpCmdSent) {
            this.mUsbManager.setCurrentFunction("ptp");
            this.mUsbManager.setUsbDataUnlocked(true);
            this.mPtpCmdSent = true;
            this.mHandler.removeMessages(1);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), 800);
        }
    }

    private void handleLocaleChanged() {
        this.mRemoteView = new RemoteViews(this.mContext.getPackageName(), 2130969232);
        this.mTwoBtnView = new RemoteViews(this.mContext.getPackageName(), 2130969233);
        cancelNotification();
        initRefreshNotification();
    }

    private void handleMtpDeactived() {
        this.mMtpCmdSent = false;
        updateFunctionBtn(2131887356, 0, 2131887355, 8);
        handleDeactived(2);
    }

    private void handlePtpDeactived() {
        this.mPtpCmdSent = false;
        updateFunctionBtn(2131887354, 0, 2131887353, 8);
        handleDeactived(1);
    }

    private void handleDeactived(int deactiveType) {
        Log.d("UsbConnectedService", "handleDeactived, deactiveType = " + deactiveType);
        updateHelpBtn(null);
        this.mRemoteView.setTextViewText(2131887348, this.mContext.getText(2131627464));
        this.mNotification.tickerText = this.mContext.getText(2131627464);
        if (Utils.isLegacyChargingMode()) {
            this.mUsbManager.setCurrentFunction("none");
        } else {
            this.mUsbManager.setCurrentFunction(null);
            this.mUsbManager.setUsbDataUnlocked(false);
        }
        this.mNotification.bigContentView = this.mRemoteView;
        try {
            this.mNm.notify(1000001, this.mNotification);
        } catch (Exception e) {
            if (this.mNotification.getSmallIcon() == null) {
                Log.w("UsbConnectedService", "UsbConnectedService-->handleDeactived()-->mNotification Smaill Icon is null !!");
            }
            Log.e("UsbConnectedService", "UsbConnectedService-->handleDeactived()-->Exception : " + e);
        }
    }

    private void updateAllBtnsNormal() {
        updateFunctionBtn(2131887354, 0, 2131887353, 8);
        updateFunctionBtn(2131887356, 0, 2131887355, 8);
    }

    private boolean setUsbUnTether() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService("connectivity");
        String[] tethered = cm.getTetheredIfaces();
        Log.d("UsbConnectedService", "setUsbUnTether");
        String usbIface = findIface(tethered, this.mUsbRegexs);
        if (usbIface == null) {
            Log.d("UsbConnectedService", "usbIface is null!");
            return true;
        } else if (cm.untether(usbIface) != 0) {
            Log.d("UsbConnectedService", "untether error!!");
            return true;
        } else {
            Log.d("UsbConnectedService", "setUsbUnTether over");
            return false;
        }
    }

    private String findIface(String[] ifaces, String[] regexes) {
        for (String iface : ifaces) {
            for (String regex : regexes) {
                if (iface.matches(regex)) {
                    return iface;
                }
            }
        }
        return null;
    }

    private void startSimUsbLimitActivity(Context context) {
        Intent helpIntent = new Intent("com.android.huawei.SIM_USB_LIMIT");
        helpIntent.setFlags(268435456);
        context.startActivity(helpIntent);
    }

    private void initControlAndStartSimUsbLimitActivity(RemoteViews mRemoteView, Context mContext) {
        Intent intent = new Intent("com.android.huawei.SIM_USB_LIMIT");
        intent.setFlags(268435456);
        intent.setPackage("com.android.settings");
        mRemoteView.setOnClickPendingIntent(2131886316, PendingIntent.getActivity(mContext, 0, intent, 134217728));
        mRemoteView.removeAllViews(2131887351);
        mRemoteView.setTextViewText(2131887347, mContext.getText(2131627800));
        mRemoteView.setTextViewText(2131887348, mContext.getText(2131627464));
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
        }
        if (!this.isFirstStarted) {
            if (isSupportUsbLimit() && r0) {
                startSimUsbLimitActivity(this.mContext);
            } else {
                showMtpGuideDialog();
            }
            this.isFirstStarted = true;
        }
        if (this.mInitRun) {
            initRefreshNotification();
            this.mInitRun = false;
        }
        return 0;
    }

    private String getDefaultFunction() {
        if (!Utils.isLegacyChargingMode() && this.isChargeOnlyStarted) {
            return "none";
        }
        if (this.mUsbManager.isFunctionEnabled("ptp")) {
            return "ptp";
        }
        if (this.mUsbManager.isFunctionEnabled("mtp")) {
            return "mtp";
        }
        if (this.mUsbManager.isFunctionEnabled("hisuite")) {
            return "hisuite";
        }
        if (this.mUsbManager.isFunctionEnabled("midi")) {
            return "midi";
        }
        return "none";
    }

    private boolean needStop(boolean usbConnected, boolean chargingOnly) {
        boolean z = false;
        if (Utils.isLegacyChargingMode()) {
            if (!(usbConnected || chargingOnly)) {
                z = true;
            }
            return z;
        }
        if (!usbConnected) {
            z = true;
        }
        return z;
    }

    private void showMtpGuideDialog() {
        if (UsbConnUtils.isMtpDialogEnabled(this.mContext) && "none".equals(getDefaultFunction())) {
            if (this.mCautionDialog != null) {
                if (this.mCautionDialog.isShowing()) {
                    this.mCautionDialog.dismiss();
                }
                this.mCautionDialog.show();
            } else {
                this.mCautionDialog = UsbConnUtils.createMtpCautionDialog(this.mContext, new OnClickListener() {
                    public void onClick(View v) {
                        UsbConnectedService.this.handleMtpMode();
                        UsbConnectedService.this.mCautionDialog.dismiss();
                    }
                }, new OnClickListener() {
                    public void onClick(View v) {
                        UsbConnectedService.this.mCautionDialog.dismiss();
                    }
                });
                this.mCautionDialog.show();
            }
        }
    }
}
