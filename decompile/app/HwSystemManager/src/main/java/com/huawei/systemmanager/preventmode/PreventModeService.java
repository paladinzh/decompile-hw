package com.huawei.systemmanager.preventmode;

import android.annotation.SuppressLint;
import android.app.INotificationManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import com.hsm.notificationmanager.M2NAdapter;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.preventmode.IHoldPreventService.Stub;
import com.huawei.systemmanager.preventmode.util.PreventDataHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import com.huawei.systemmanager.util.contacts.ContactsObserverHelper;

public class PreventModeService extends Service {
    private static final int MSG_CHECK_SUPER_POWER_OFF_CONTINUE = 0;
    private static final String TAG = "PreventModeService";
    private static Object sSyn = new Object();
    private PreventModeContactObserver mContactsObserver = null;
    private MyHandler mHandler = new MyHandler();
    private HoldPreventServiceBinder mHoldServiceBinder = null;
    private MyReceiver mReceiver = new MyReceiver();

    class HoldPreventServiceBinder extends Stub {
        private static final String EXTRA_NUMBER_TYPE_SMS = "com.huawei.hsm.number_type_sms";
        private static final String KEY_ZEN_CALL_WHITE_LIST_ENABLED = "zen_call_white_list_enabled";
        private static final String KEY_ZEN_MESSAGE_WHITE_LIST_ENABLED = "zen_message_white_list_enabled";
        private static final String MMS_PACKAGE_NAME = "com.android.mms";
        private static final int ZEN_WHITE_LIST_DISABLED = 0;
        private static final int ZEN_WHITE_LIST_ENABLED = 1;

        HoldPreventServiceBinder() {
        }

        public boolean isPrevent(String phoneNumber, boolean fromPhone) throws RemoteException {
            PreventModeService.this.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            if (VERSION.SDK_INT < 23) {
                return false;
            }
            if (TextUtils.isEmpty(phoneNumber)) {
                HwLog.w(PreventModeService.TAG, "give me an empty number");
            }
            if (fromPhone) {
                return shouldPreventTheCall(phoneNumber);
            }
            if (getHighPriority() && getZenMode()) {
                return false;
            }
            return shouldPreventTheMessage(phoneNumber);
        }

        private boolean getZenMode() {
            if (1 == PreventDataHelper.getCurrentZenMode(PreventModeService.this)) {
                return true;
            }
            return false;
        }

        private boolean getHighPriority() {
            boolean z = false;
            int uid = 0;
            try {
                HsmPkgInfo pkgInfo = HsmPackageManager.getInstance().getPkgInfo(MMS_PACKAGE_NAME);
                if (pkgInfo != null) {
                    uid = pkgInfo.getUid();
                }
                if (M2NAdapter.getPriority(INotificationManager.Stub.asInterface(ServiceManager.getService("notification")), MMS_PACKAGE_NAME, uid) == 2) {
                    z = true;
                }
                return z;
            } catch (Exception e) {
                HwLog.w(PreventModeService.TAG, "Error sending message NoMan", e);
                return false;
            }
        }

        private boolean shouldPreventTheMessage(String phoneNumber) {
            PreventModeService context = PreventModeService.this;
            if (shouldHanldeWhiteNumber(context, KEY_ZEN_MESSAGE_WHITE_LIST_ENABLED)) {
                boolean prevent = !new PreventConfig(context).isWhiteNumber(phoneNumber);
                HwLog.i(PreventModeService.TAG, "shouldPreventTheMessage, prevent:" + prevent);
                return prevent;
            }
            prevent = !shouldRingForContact(getUri(phoneNumber), true);
            HwLog.i(PreventModeService.TAG, "shouldPreventTheMessage, they says " + prevent);
            return prevent;
        }

        private boolean shouldPreventTheCall(String phoneNumber) {
            boolean z = false;
            PreventModeService context = PreventModeService.this;
            boolean prevent;
            if (!shouldHanldeWhiteNumber(context, KEY_ZEN_CALL_WHITE_LIST_ENABLED)) {
                prevent = !shouldRingForContact(getUri(phoneNumber), false);
                HwLog.i(PreventModeService.TAG, "shouldPreventTheCall, they says " + prevent);
                return prevent;
            } else if (new PreventConfig(context).isWhiteNumber(phoneNumber)) {
                HwLog.i(PreventModeService.TAG, "shouldPreventTheCall,don't prevent");
                return false;
            } else {
                prevent = !shouldRingForContact(getUri(phoneNumber), false);
                String str = PreventModeService.TAG;
                StringBuilder append = new StringBuilder().append("shouldPreventTheCall, repeat? ");
                if (!prevent) {
                    z = true;
                }
                HwLog.i(str, append.append(z).toString());
                return prevent;
            }
        }

        private boolean shouldHanldeWhiteNumber(Context context, String type) {
            int zenMode = PreventDataHelper.getCurrentZenMode(context);
            boolean priorityMode = 1 == zenMode;
            boolean onlyWhiteNumber = Secure.getInt(context.getContentResolver(), type, 0) == 1;
            boolean z = priorityMode ? onlyWhiteNumber : false;
            HwLog.i(PreventModeService.TAG, "shouldPrevent, zenMode:" + zenMode + ", caller:" + Binder.getCallingPid() + ", use white number:" + onlyWhiteNumber);
            return z;
        }

        private Uri getUri(String number) {
            return Uri.fromParts("tel", number, null);
        }

        private boolean shouldRingForContact(Uri contactUri, boolean forSms) {
            NotificationManager manager = (NotificationManager) PreventModeService.this.getSystemService("notification");
            Bundle extras = new Bundle();
            if (contactUri != null) {
                extras.putStringArray(NotificationCompat.EXTRA_PEOPLE, new String[]{contactUri.toString()});
                if (forSms) {
                    extras.putBoolean(EXTRA_NUMBER_TYPE_SMS, forSms);
                }
            }
            return manager.matchesCallFilter(extras);
        }

        public String[] queryAllWhiteListPhoneNo() throws RemoteException {
            PreventModeService.this.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            return new PreventConfig(PreventModeService.this).queryAllPhoneNo();
        }
    }

    @SuppressLint({"HandlerLeak"})
    class MyHandler extends Handler {
        private static final int INTERVAL = 1000;
        private static final int MAX_COUNT = 5;
        private int mCount = 0;

        MyHandler() {
        }

        public void init() {
            removeMessages(0);
            this.mCount = 0;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (SystemProperties.getBoolean("sys.super_power_save", false)) {
                        if (this.mCount < 5) {
                            this.mCount++;
                            sendMessageDelayed(Message.obtain(this, 0), 1000);
                            break;
                        }
                        return;
                    }
                    PreventDataHelper.updateNotification(PreventModeService.this);
                    break;
            }
        }
    }

    private class MyReceiver extends BroadcastReceiver {
        private MyReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwLog.w(PreventModeService.TAG, "null intent in broadcast.");
                return;
            }
            HwLog.i(PreventModeService.TAG, "received action:" + intent.getAction());
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                PreventDataHelper.updateNotification(PreventModeService.this);
            } else if (ActionConst.INTENT_SHUTDOWN_SUPER_POWER_SAVING_MODE.equals(intent.getAction())) {
                PreventModeService.this.mHandler.init();
                Message.obtain(PreventModeService.this.mHandler, 0).sendToTarget();
            }
        }
    }

    public IBinder onBind(Intent arg0) {
        return getHoldPreventServiceBinderInstance();
    }

    public void onCreate() {
        super.onCreate();
        if (UserHandle.myUserId() == 0) {
            addServiceBinder();
            observePreventWhiteContacts();
        } else {
            HwLog.i(TAG, "This is no owner user, we don't handle prevent.");
        }
        PreventDataHelper.updateVisibility(this);
        registerMyReceiver();
    }

    private void registerMyReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_SWITCHED");
        filter.addAction(ActionConst.INTENT_SHUTDOWN_SUPER_POWER_SAVING_MODE);
        registerReceiver(this.mReceiver, filter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    private void observePreventWhiteContacts() {
        this.mContactsObserver = new PreventModeContactObserver(this);
        ContactsObserverHelper.getInstance(this).registerObserver(this.mContactsObserver);
        HwLog.i(TAG, "ContactsObserver is created , " + this.mContactsObserver);
    }

    private void addServiceBinder() {
        this.mHoldServiceBinder = getHoldPreventServiceBinderInstance();
        try {
            ServiceManager.addService("com.huawei.systemmanager.preventmode.PreventModeService", this.mHoldServiceBinder);
        } catch (SecurityException e) {
            HwLog.e(TAG, "Hold Service create fail.");
        }
    }

    public void onDestroy() {
        unregisterReceiver(this.mReceiver);
        if (this.mContactsObserver != null) {
            ContactsObserverHelper.getInstance(this).unregisterObserver(this.mContactsObserver);
        }
        HwLog.i(TAG, "onDestroy");
    }

    public HoldPreventServiceBinder getHoldPreventServiceBinderInstance() {
        synchronized (sSyn) {
            try {
                if (this.mHoldServiceBinder == null) {
                    this.mHoldServiceBinder = new HoldPreventServiceBinder();
                }
            } catch (NoClassDefFoundError e) {
                HwLog.e(TAG, "getHoldServiceBinderInstance error");
                return null;
            }
        }
        return this.mHoldServiceBinder;
    }
}
