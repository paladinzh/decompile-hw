package com.huawei.harassmentinterception.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.text.TextUtils;
import com.google.common.collect.Sets;
import com.huawei.android.telephony.SmsInterceptionListenerEx;
import com.huawei.android.telephony.SmsInterceptionManagerEx;
import com.huawei.harassmentinterception.blackwhitelist.BlackWhiteDBDataUpdater;
import com.huawei.harassmentinterception.blackwhitelist.GoogleBlackListContract;
import com.huawei.harassmentinterception.common.BlockReason;
import com.huawei.harassmentinterception.common.CommonObject.BlacklistInfo;
import com.huawei.harassmentinterception.common.CommonObject.InCommingCall;
import com.huawei.harassmentinterception.common.CommonObject.MessageInfo;
import com.huawei.harassmentinterception.common.CommonObject.NumberMarkInfo;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.SmsMsgInfo;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.engine.HwEngineManager;
import com.huawei.harassmentinterception.receiver.ContactsObserver;
import com.huawei.harassmentinterception.service.IHarassmentInterceptionService.Stub;
import com.huawei.harassmentinterception.strategy.StrategyManager;
import com.huawei.harassmentinterception.update.UpdateHelper;
import com.huawei.harassmentinterception.util.CallIntelligentHelper;
import com.huawei.harassmentinterception.util.HotlineNumberHelper;
import com.huawei.harassmentinterception.util.MmsIntentHelper;
import com.huawei.harassmentinterception.util.MmsInterceptionHelper;
import com.huawei.harassmentinterception.util.PreferenceHelper;
import com.huawei.harassmentinterception.util.SmsIntentHelper;
import com.huawei.harassmentinterception.util.SmsInterceptionHelper;
import com.huawei.rcs.service.RcsHarassmentInterceptionService;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.contacts.ContactsObserverHelper;
import java.util.HashSet;
import java.util.List;

public class HarassmentInterceptionService extends Service {
    private static final long CHECK_CMMC_DELAY = 600000;
    private static final String CMCC_NUM = "10658112210002";
    public static final int MSG_ENGINE_CHANGE = 2;
    public static final int MSG_INIT_GOOGLE_BLACK_LIST = 3;
    public static final int MSG_INIT_STRATEGY = 0;
    public static final int MSG_STRATEGY_CHANGE = 1;
    private static final int SMS_INTERCEPTION_PRIORITY = 7000;
    private static final String TAG = "HarassmentInterceptionService";
    public static final int YELLOW_PAGE_COUNT = 0;
    public static final boolean YELLOW_PAGE_DEFAULT_ISTIMEOUT = false;
    public static final boolean YELLOW_PAGE_DEFAULT_LOCAL = false;
    private ContentObserver mBlackListObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri == null) {
                HwLog.e(HarassmentInterceptionService.TAG, "mBlackListObserver url is null");
                return;
            }
            HwLog.i(HarassmentInterceptionService.TAG, "selfChange = " + selfChange + "Uri = " + uri.toSafeString());
            if (GoogleBlackListContract.isBlackListDeleted(uri)) {
                HarassmentInterceptionService.this.syncHwBlackListByNBDeleted();
            } else if (GoogleBlackListContract.isBlackListAdded(uri)) {
                String blockedNumber = GoogleBlackListContract.getBlockedNumberById(uri);
                if (!TextUtils.isEmpty(blockedNumber)) {
                    HarassmentInterceptionService.this.syncHwBlackListByNBAdded(blockedNumber);
                    HarassmentInterceptionService.this.syncWhiteListByBlackNumberAdded(blockedNumber);
                }
            }
        }
    };
    private ContactsObserver mContactsObserver = null;
    private Context mContext = null;
    private Handler mHandler = null;
    private HoldHarassmentInterceptionBinder mHoldServiceBinder = null;
    private boolean mIsReRegDone = false;
    private RcsHarassmentInterceptionService mRcs;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Utility.checkBroadcast(context, intent)) {
                String action = intent.getAction();
                if (ConstValues.ACTION_INTERCEPT_RULE_CHANGE.equals(action)) {
                    HarassmentInterceptionService.this.mHandler.removeMessages(1);
                    HarassmentInterceptionService.this.mHandler.sendEmptyMessageDelayed(1, 1000);
                } else if (ConstValues.ACTION_INTERCEPT_ENGINE_CHANGE.equals(action)) {
                    HarassmentInterceptionService.this.mHandler.sendEmptyMessage(2);
                }
            }
        }
    };
    private SimStateReceiver mSimStateReceiver;
    private HsmSmsInterceptionListener mSmsInterceptionListener;
    private Object mSyncObject = null;
    private ContentObserver mWhiteListObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (ContentUris.parseId(uri) < 0) {
                HwLog.i(HarassmentInterceptionService.TAG, "there is no id in URI, so it's not insert action");
            } else {
                HarassmentInterceptionService.this.syncBlackListByWhiteNumber();
            }
        }
    };

    class HoldHarassmentInterceptionBinder extends Stub {
        HoldHarassmentInterceptionBinder() {
        }

        public int setPhoneNumberBlockList(Bundle blocknumberlist, int type, int source) throws RemoteException {
            HarassmentInterceptionService.this.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HwLog.i(HarassmentInterceptionService.TAG, "setPhoneNumberBlockList: starts, type + " + type);
            if (blocknumberlist != null) {
                int nResult = ServiceHelper.setPhoneNumberBlockList(HarassmentInterceptionService.this.mContext, blocknumberlist, type, source) > 0 ? 0 : -1;
                HwLog.i(HarassmentInterceptionService.TAG, "setPhoneNumberBlockList: nResult = " + nResult);
                return nResult;
            }
            HwLog.i(HarassmentInterceptionService.TAG, "setPhoneNumberBlockList: Invalid blocknumberlist");
            return -1;
        }

        public int addPhoneNumberBlockItem(Bundle blocknumber, int type, int source) throws RemoteException {
            HarassmentInterceptionService.this.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HwLog.i(HarassmentInterceptionService.TAG, "addPhoneNumberBlockItem: starts, type = " + type);
            if (blocknumber != null) {
                int nRet = ServiceHelper.addPhoneNumberBlockItem(HarassmentInterceptionService.this.mContext, blocknumber, type, source) >= 0 ? 0 : -1;
                HwLog.i(HarassmentInterceptionService.TAG, "addPhoneNumberBlockItem: return = " + nRet);
                return nRet;
            }
            HwLog.i(HarassmentInterceptionService.TAG, "addPhoneNumberBlockItem: Invalid blocknumber");
            return -1;
        }

        public int removePhoneNumberBlockItem(Bundle blocknumber, int type, int source) throws RemoteException {
            HarassmentInterceptionService.this.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HwLog.i(HarassmentInterceptionService.TAG, "removePhoneNumberBlockItem: starts, type = " + type);
            if (blocknumber != null) {
                int nResult = ServiceHelper.removePhoneNumberBlockItem(HarassmentInterceptionService.this.mContext, blocknumber, type, source) > 0 ? 0 : -1;
                HwLog.i(HarassmentInterceptionService.TAG, "removePhoneNumberBlockItem: nResult = " + nResult);
                return nResult;
            }
            HwLog.i(HarassmentInterceptionService.TAG, "removePhoneNumberBlockItem: Invalid blocknumber");
            return -1;
        }

        public String[] queryPhoneNumberBlockItem() throws RemoteException {
            HarassmentInterceptionService.this.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HwLog.i(HarassmentInterceptionService.TAG, "queryPhoneNumberBlockItem: starts");
            String[] blacklistNumbers = ServiceHelper.queryPhoneNumberBlockItem(HarassmentInterceptionService.this.mContext);
            HwLog.i(HarassmentInterceptionService.TAG, "queryPhoneNumberBlockItem: blacklist count = " + blacklistNumbers.length);
            return blacklistNumbers;
        }

        public int checkPhoneNumberFromBlockItem(Bundle checknumber, int type) throws RemoteException {
            HarassmentInterceptionService.this.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HwLog.i(HarassmentInterceptionService.TAG, "checkPhoneNumberFromBlockItem: starts , type = " + type);
            if (checknumber != null) {
                int nResult = ServiceHelper.checkPhoneNumberFromBlockItem(HarassmentInterceptionService.this.mContext, checknumber, type);
                HwLog.i(HarassmentInterceptionService.TAG, "checkPhoneNumberFromBlockItem: nResult = " + nResult);
                return nResult;
            }
            HwLog.w(HarassmentInterceptionService.TAG, "checkPhoneNumberFromBlockItem: Invalid checknumber");
            return -1;
        }

        public void sendCallBlockRecords(Bundle callBlockRecords) throws RemoteException {
            HarassmentInterceptionService.this.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HwLog.i(HarassmentInterceptionService.TAG, "sendCallBlockRecords: starts");
            if (callBlockRecords != null) {
                ServiceHelper.sendCallBlockRecords(HarassmentInterceptionService.this.mContext, callBlockRecords, null);
            } else {
                HwLog.w(HarassmentInterceptionService.TAG, "sendCallBlockRecords: Invalid params");
            }
        }

        public int handleSmsDeliverAction(Bundle smsInfo) throws RemoteException {
            HarassmentInterceptionService.this.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HwLog.i(HarassmentInterceptionService.TAG, "handleSmsDeliverAction: Receive a sms");
            if (smsInfo == null) {
                HwLog.w(HarassmentInterceptionService.TAG, "handleSmsDeliverAction: Invalid sms info");
                return -1;
            } else if (!SmsInterceptionHelper.isDefaultSmsApp()) {
                return -1;
            } else {
                if (HarassmentInterceptionService.this.mRcs != null && !HarassmentInterceptionService.this.mRcs.isImIntentEmpty(smsInfo)) {
                    return HarassmentInterceptionService.this.mRcs.getImIntentResult(smsInfo);
                }
                if (smsInfo.containsKey(ConstValues.HANDLE_KEY_AIDL_SMSINTENT)) {
                    Intent intent = (Intent) smsInfo.getParcelable(ConstValues.HANDLE_KEY_AIDL_SMSINTENT);
                    if (intent == null) {
                        HwLog.w(HarassmentInterceptionService.TAG, "handleSmsDeliverAction: fail to get sms intent");
                        return -1;
                    }
                    SmsMsgInfo smsMsg = SmsIntentHelper.getSmsInfoFromIntent(HarassmentInterceptionService.this.mContext, intent);
                    if (smsMsg == null) {
                        HwLog.w(HarassmentInterceptionService.TAG, "handleSmsDeliverAction: Invalid sms");
                        return -1;
                    } else if (!HarassmentInterceptionService.CMCC_NUM.equals(smsMsg.getPhone()) || SystemClock.elapsedRealtime() >= 600000) {
                        Utility.initSDK(HarassmentInterceptionService.this.mContext);
                        int nHandleResult = StrategyManager.getInstance(HarassmentInterceptionService.this.mContext).applyStrategyForSms(new SmsIntentWrapper(smsMsg, intent));
                        if (1 == nHandleResult) {
                            Intent smsInent = new Intent(intent);
                            smsInent.setComponent(null);
                            smsInent.setAction("com.huawei.dianxinos.optimizer.action.HARASSMENT_SMS");
                            HarassmentInterceptionService.this.mContext.sendBroadcast(smsInent, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
                            HwLog.d(HarassmentInterceptionService.TAG, "handleSmsDeliverAction: Sms is re-delivered as action.HARASSMENT_SMS");
                        }
                        HwLog.d(HarassmentInterceptionService.TAG, "handleSmsDeliverAction: Handle result = " + nHandleResult);
                        return nHandleResult;
                    } else {
                        HwLog.w(HarassmentInterceptionService.TAG, "handleSmsDeliverAction: this sms is send by CMCC,and not a true sms,so return");
                        return 0;
                    }
                }
                HwLog.w(HarassmentInterceptionService.TAG, "handleSmsDeliverAction: Invalid param, no sms info");
                return -1;
            }
        }

        public int handleIncomingCallAction(Bundle callInfo) throws RemoteException {
            Utility.initSDK(HarassmentInterceptionService.this.mContext);
            HarassmentInterceptionService.this.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HwLog.d(HarassmentInterceptionService.TAG, "handleIncomingCallAction: Receive an incoming call");
            if (callInfo == null) {
                HwLog.w(HarassmentInterceptionService.TAG, "handleIncomingCallAction: Invalid call info");
                return -1;
            } else if (callInfo.containsKey(ConstValues.HANDLE_KEY_AIDL_CALLINFO)) {
                String phoneNumber = callInfo.getString(ConstValues.HANDLE_KEY_AIDL_CALLINFO);
                int presentation = callInfo.getInt(ConstValues.HANDLE_KEY_AIDL_NUMBER_PRESENTATION, 1);
                int subId = callInfo.getInt(ConstValues.HANDLE_KEY_AIDL_SUB_ID, -1);
                HwLog.i(HarassmentInterceptionService.TAG, "handleIncomingCallAction  presentation = " + presentation + ", subId:" + subId);
                int nHandleResult = StrategyManager.getInstance(HarassmentInterceptionService.this.mContext).applyStrategyForCall(new InCommingCall(phoneNumber, presentation, ServiceHelper.convertCallSubId(subId)));
                HwLog.d(HarassmentInterceptionService.TAG, "handleIncomingCallAction: Handle result = " + nHandleResult);
                return nHandleResult;
            } else {
                HwLog.w(HarassmentInterceptionService.TAG, "handleIncomingCallAction: Invalid param, no call info");
                return -1;
            }
        }

        public Bundle handleInComingCallAndGetNumberMark(Bundle callInfo) throws RemoteException {
            boolean z = false;
            Utility.initSDK(HarassmentInterceptionService.this.mContext);
            HarassmentInterceptionService.this.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HwLog.i(HarassmentInterceptionService.TAG, "handleIncomingCallActionAndGetNumberMark: Receive an incoming call");
            Bundle handleResult = new Bundle();
            if (callInfo == null) {
                HwLog.w(HarassmentInterceptionService.TAG, "handleIncomingCallAction: Invalid call info");
                handleResult.putInt(ConstValues.HANDLE_RESULT, -1);
                return handleResult;
            } else if (callInfo.containsKey(ConstValues.HANDLE_KEY_AIDL_CALLINFO)) {
                String phoneNumber = callInfo.getString(ConstValues.HANDLE_KEY_AIDL_CALLINFO);
                int presentation = callInfo.getInt(ConstValues.HANDLE_KEY_AIDL_NUMBER_PRESENTATION, 1);
                int subId = callInfo.getInt(ConstValues.HANDLE_KEY_AIDL_SUB_ID, -1);
                HwLog.i(HarassmentInterceptionService.TAG, "handleInComingCallAndGetNumberMark  presentation = " + presentation + ", subId:" + subId);
                InCommingCall inCommingCall = new InCommingCall(phoneNumber, presentation, ServiceHelper.convertCallSubId(subId));
                NumberMarkInfo info = new NumberMarkInfo();
                if (HotlineNumberHelper.isHotlineNumber(HarassmentInterceptionService.this.mContext, phoneNumber)) {
                    HwLog.i(HarassmentInterceptionService.TAG, "yellow page,so don't query number mark");
                    info.mMarkCount = 0;
                    info.setIsLocal(false);
                    info.setIsTimeout(false);
                } else {
                    info = CallIntelligentHelper.queryNumberMark(HarassmentInterceptionService.this.mContext, phoneNumber);
                    if (info != null) {
                        inCommingCall.setMarkType(info.getmMarkType());
                        inCommingCall.setMarkName(info.getmMarkName());
                        inCommingCall.setMarkCount(info.getmMarkCount());
                        inCommingCall.setIsLocal(info.getIsLocal());
                    } else {
                        HwLog.i(HarassmentInterceptionService.TAG, "queryNumberMark failed");
                    }
                }
                int nHandleResult = StrategyManager.getInstance(HarassmentInterceptionService.this.mContext).applyStrategyForCall(inCommingCall);
                handleResult.putInt(ConstValues.HANDLE_RESULT, nHandleResult);
                handleResult.putString(ConstValues.NUMBER, phoneNumber);
                HwLog.i(HarassmentInterceptionService.TAG, "handleIncomingCallAction: Handle result = " + nHandleResult);
                if (info != null) {
                    handleResult.putString(ConstValues.MARK_CLASSIFY, info.getClassify());
                    handleResult.putString(ConstValues.MARK_NAME, info.getmMarkName());
                    handleResult.putInt(ConstValues.MARK_COUNT, info.getmMarkCount());
                    String str = ConstValues.ISCLOUD;
                    if (!info.getIsLocal()) {
                        z = true;
                    }
                    handleResult.putBoolean(str, z);
                    handleResult.putString(ConstValues.DESCRIPTION, info.getDescription());
                    handleResult.putString(ConstValues.SAVE_TIMESTAMP, info.getSaveTimeStamp());
                    handleResult.putString(ConstValues.SUPPLIER, info.getSupplier());
                    handleResult.putBoolean(ConstValues.IS_TIMEOUT, info.getIsTimeout());
                }
                return handleResult;
            } else {
                HwLog.w(HarassmentInterceptionService.TAG, "handleIncomingCallAction: Invalid param, no call info");
                handleResult.putInt(ConstValues.HANDLE_RESULT, -1);
                return handleResult;
            }
        }

        public int checkPhoneNumberFromWhiteItem(Bundle checknumber, int type) throws RemoteException {
            HarassmentInterceptionService.this.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HwLog.i(HarassmentInterceptionService.TAG, "checkPhoneNumberFromWhiteItem: starts");
            if (checknumber != null) {
                int nResult = ServiceHelper.checkPhoneNumberFromWhiteList(HarassmentInterceptionService.this.mContext, checknumber, type) ? 0 : -1;
                HwLog.i(HarassmentInterceptionService.TAG, "checkPhoneNumberFromWhiteItem: nResult = " + nResult);
                return nResult;
            }
            HwLog.w(HarassmentInterceptionService.TAG, "checkPhoneNumberFromWhiteItem: Invalid checknumber");
            return -1;
        }

        public int removePhoneNumberFromWhiteItem(Bundle blocknumber, int type, int source) throws RemoteException {
            HarassmentInterceptionService.this.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HwLog.i(HarassmentInterceptionService.TAG, "removePhoneNumberFromWhiteItem: starts");
            if (blocknumber != null) {
                int nResult = ServiceHelper.removePhoneNumberFromWhitelist(HarassmentInterceptionService.this.mContext, blocknumber, type, source) > 0 ? 0 : -1;
                HwLog.i(HarassmentInterceptionService.TAG, "removePhoneNumberFromWhiteItem: nResult = " + nResult);
                return nResult;
            }
            HwLog.i(HarassmentInterceptionService.TAG, "removePhoneNumberFromWhiteItem: Invalid blocknumber");
            return -1;
        }

        public int addPhoneNumberWhiteItem(Bundle blocknumber, int type, int source) throws RemoteException {
            HarassmentInterceptionService.this.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HwLog.i(HarassmentInterceptionService.TAG, "addPhoneNumberWhiteItem: starts");
            return -1;
        }

        public String[] queryPhoneNumberWhiteItem() throws RemoteException {
            HarassmentInterceptionService.this.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HwLog.i(HarassmentInterceptionService.TAG, "queryPhoneNumberWhiteItem: starts");
            return new String[0];
        }

        public Bundle callHarassmentInterceptionService(String method, Bundle params) {
            HwLog.i(HarassmentInterceptionService.TAG, "callHarassmentInterceptionService  method: " + method);
            return HsmInterceptionCaller.call(HarassmentInterceptionService.this.getApplicationContext(), method, params);
        }

        public boolean sendGoogleNBRecord(Bundle info) {
            HarassmentInterceptionService.this.enforceCallingOrSelfPermission("com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
            HwLog.i(HarassmentInterceptionService.TAG, "handleRecordBlockedNumber");
            if (info == null) {
                HwLog.w(HarassmentInterceptionService.TAG, "handleRecordBlockedNumber: Invalid sms info");
                return false;
            }
            switch (info.getInt(ConstValues.BLOCK_TYPE)) {
                case 1:
                    return handlePhoneBlockRecord(info);
                case 2:
                    return handleSmsBlockRecord(info);
                case 3:
                    return HarassmentInterceptionService.this.handleWapPushBlockRecord(info);
                default:
                    return false;
            }
        }

        private boolean handlePhoneBlockRecord(Bundle info) {
            HwLog.i(HarassmentInterceptionService.TAG, "sendCallBlockRecords: starts");
            if (info != null) {
                try {
                    ServiceHelper.sendCallBlockRecords(HarassmentInterceptionService.this.mContext, info, new BlockReason(1, 1));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                HwLog.w(HarassmentInterceptionService.TAG, "sendCallBlockRecords: Invalid params");
            }
            return false;
        }

        private boolean handleSmsBlockRecord(Bundle info) {
            Intent intent = (Intent) info.getParcelable(ConstValues.HANDLE_KEY_AIDL_SMSINTENT);
            if (intent == null) {
                HwLog.w(HarassmentInterceptionService.TAG, "handleRecordBlockedNumber: fail to get sms intent");
                return false;
            }
            SmsMsgInfo smsMsg = SmsIntentHelper.getSmsInfoFromIntent(HarassmentInterceptionService.this.mContext, intent);
            if (smsMsg == null) {
                HwLog.w(HarassmentInterceptionService.TAG, "handleRecordBlockedNumber: Invalid sms");
                return false;
            }
            Intent smsInent = new Intent(intent);
            smsInent.setComponent(null);
            smsInent.setAction("com.huawei.dianxinos.optimizer.action.HARASSMENT_SMS");
            HarassmentInterceptionService.this.mContext.sendBroadcast(smsInent, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
            HwLog.i(HarassmentInterceptionService.TAG, "handleSmsBlockRecord: Sms is re-delivered as action.HARASSMENT_SMS");
            return SmsInterceptionHelper.addToInterceptRecord(HarassmentInterceptionService.this.mContext, smsMsg, 1);
        }
    }

    class HsmSmsInterceptionListener extends SmsInterceptionListenerEx {
        HsmSmsInterceptionListener() {
        }

        public int handleSmsDeliverAction(Bundle smsInfo) {
            HwLog.i(HarassmentInterceptionService.TAG, "handleSmsDeliverAction: Receive a sms from listener");
            int nResult = 0;
            try {
                nResult = HarassmentInterceptionService.this.mHoldServiceBinder.handleSmsDeliverAction(smsInfo);
            } catch (Exception e) {
                HwLog.e(HarassmentInterceptionService.TAG, "handleSmsDeliverAction: Exception", e);
            }
            return nResult;
        }

        public int handleWapPushDeliverAction(Bundle wapPushInfo) {
            HwLog.i(HarassmentInterceptionService.TAG, "handleWapPushDeliverAction: Receive a mms from listener");
            int nResult = 0;
            try {
                nResult = MmsIntentHelper.handleWapPushDeliverAction(HarassmentInterceptionService.this.mContext, wapPushInfo);
            } catch (Exception e) {
                HwLog.e(HarassmentInterceptionService.TAG, "handleWapPushDeliverAction: Exception", e);
            }
            return nResult;
        }

        public boolean sendNumberBlockedRecord(Bundle smsInfo) {
            HwLog.i(HarassmentInterceptionService.TAG, "sendNumberBlockedRecord: Receive a mms from listener");
            try {
                return HarassmentInterceptionService.this.mHoldServiceBinder.sendGoogleNBRecord(smsInfo);
            } catch (Exception e) {
                HwLog.e(HarassmentInterceptionService.TAG, "sendNumberBlockedRecord: Exception", e);
                return false;
            }
        }
    }

    private class InnerHandler extends Handler {
        public InnerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                case 1:
                    StrategyManager.getInstance(HarassmentInterceptionService.this.mContext).updateStrategyFromDb();
                    return;
                case 2:
                    HwEngineManager.switchEngine(HarassmentInterceptionService.this.mContext);
                    return;
                case 3:
                    HarassmentInterceptionService.this.initGoogleBlackList();
                    return;
                default:
                    HwLog.w(HarassmentInterceptionService.TAG, "handleMessage: Invalid message ," + msg.arg1);
                    return;
            }
        }
    }

    class SimStateReceiver extends BroadcastReceiver {
        SimStateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
                HwLog.i(HarassmentInterceptionService.TAG, "receive simcard stat change action, simcardState:" + intent.getStringExtra("ss"));
                HarassmentInterceptionService.this.reRegSmsInterceptionListener();
            }
        }
    }

    public IBinder onBind(Intent intent) {
        HwLog.d(TAG, "onBind, intent = " + intent);
        return getHoldHarassmentInterceptionBinderInstance();
    }

    public void onCreate() {
        super.onCreate();
        this.mRcs = new RcsHarassmentInterceptionService(this.mContext);
        this.mSyncObject = new Object();
        this.mContext = getApplicationContext();
        HandlerThread thread = new HandlerThread("harassment_service_handlethread");
        thread.start();
        this.mHandler = new InnerHandler(thread.getLooper());
        registerHarasReceiver(this.mContext);
        this.mHandler.sendEmptyMessage(0);
        this.mHandler.sendEmptyMessage(3);
        this.mHoldServiceBinder = getHoldHarassmentInterceptionBinderInstance();
        if (this.mHoldServiceBinder != null) {
            try {
                ServiceManager.addService("com.huawei.harassmentinterception.service.HarassmentInterceptionService", this.mHoldServiceBinder);
                this.mContactsObserver = new ContactsObserver(this);
                ContactsObserverHelper.getInstance(this.mContext).registerObserver(this.mContactsObserver);
                HwLog.i(TAG, "ContactsObserver is created , " + this.mContactsObserver);
                this.mSmsInterceptionListener = new HsmSmsInterceptionListener();
                SmsInterceptionManagerEx.getInstance().registerListener(this.mSmsInterceptionListener, SMS_INTERCEPTION_PRIORITY);
                this.mSimStateReceiver = new SimStateReceiver();
                registerReceiver(this.mSimStateReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
                triggerAutoUpdate();
                if (PreferenceHelper.getBlackWhiteListDBUpdatedStatus(this.mContext) == 0) {
                    HwLog.i(TAG, "triggle BlackWhiteDBDataUpdater");
                    BlackWhiteDBDataUpdater.getInstance(this, null, 1).triggleUpdate();
                }
                HwLog.i(TAG, "Phone number location feature status = " + CustomizeWrapper.isNumberLocationEnabled());
            } catch (SecurityException e) {
                HwLog.e(TAG, "Fail to add HarassmentInterceptionService.", e);
            }
        }
    }

    private void triggerAutoUpdate() {
        new Thread(new Runnable() {
            public void run() {
                int updateStrategy = UpdateHelper.getAutoUpdateStrategy(HarassmentInterceptionService.this.mContext);
                HwLog.i(HarassmentInterceptionService.TAG, "triggerAutoUpdate, updateStrategy:" + updateStrategy);
                UpdateHelper.setAutoUpdateStrategy(HarassmentInterceptionService.this.mContext, updateStrategy);
            }
        }, "HarassIntercept_scheduleAutoUpdate").start();
    }

    private void reRegSmsInterceptionListener() {
        if (!this.mIsReRegDone) {
            if (this.mSmsInterceptionListener == null) {
                this.mSmsInterceptionListener = new HsmSmsInterceptionListener();
            }
            SmsInterceptionManagerEx.getInstance().registerListener(this.mSmsInterceptionListener, SMS_INTERCEPTION_PRIORITY);
            this.mIsReRegDone = true;
            HwLog.i(TAG, "reRegSmsInterceptionListener: Done");
        }
    }

    public void onDestroy() {
        super.onDestroy();
        unRegisterHarasReceiver(this.mContext);
        this.mHandler.getLooper().quit();
        if (this.mContactsObserver != null) {
            ContactsObserverHelper.getInstance(this.mContext).unregisterObserver(this.mContactsObserver);
        }
        if (this.mSimStateReceiver != null) {
            unregisterReceiver(this.mSimStateReceiver);
        }
        if (this.mSmsInterceptionListener != null) {
            SmsInterceptionManagerEx.getInstance().unregisterListener(SMS_INTERCEPTION_PRIORITY);
        }
        HwLog.i(TAG, "onDestroy");
    }

    public HoldHarassmentInterceptionBinder getHoldHarassmentInterceptionBinderInstance() {
        HoldHarassmentInterceptionBinder holdHarassmentInterceptionBinder;
        synchronized (this.mSyncObject) {
            try {
                if (this.mHoldServiceBinder == null) {
                    this.mHoldServiceBinder = new HoldHarassmentInterceptionBinder();
                }
                holdHarassmentInterceptionBinder = this.mHoldServiceBinder;
            } catch (NoClassDefFoundError e) {
                HwLog.e(TAG, "getHoldHarassmentInterceptionBinderInstance error", e);
                return null;
            }
        }
        return holdHarassmentInterceptionBinder;
    }

    private boolean handleWapPushBlockRecord(Bundle wapPush) {
        if (wapPush == null) {
            HwLog.w(TAG, "handleWapPushBlockRecord: Invalid WapPush info");
            return false;
        } else if (wapPush.containsKey(ConstValues.HANDLE_KEY_AIDL_WAPPUSHINTENT)) {
            Intent wapintent = (Intent) wapPush.getParcelable(ConstValues.HANDLE_KEY_AIDL_WAPPUSHINTENT);
            if (wapintent == null) {
                HwLog.w(TAG, "handleWapPushBlockRecord: fail to get wappush intent");
                return false;
            } else if ("application/vnd.wap.mms-message".equals(wapintent.getType())) {
                MessageInfo mmsMsgInfo = MmsIntentHelper.getMmsInfoFromIntent(this.mContext, wapintent);
                if (mmsMsgInfo == null) {
                    HwLog.w(TAG, "handleWapPushBlockRecord: this is not a normal mms ");
                    return false;
                }
                mmsMsgInfo.setName(DBAdapter.getNameFromBlacklist(this.mContext, mmsMsgInfo.getPhone()));
                return MmsInterceptionHelper.addToInterceptRecord(this.mContext, mmsMsgInfo, 1);
            } else {
                HwLog.w(TAG, "handleWapPushBlockRecord: this wap push is not mms Type");
                return false;
            }
        } else {
            HwLog.w(TAG, "handleWapPushBlockRecord: Invalid param, no wappush info");
            return false;
        }
    }

    private void registerHarasReceiver(Context ctx) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConstValues.ACTION_INTERCEPT_RULE_CHANGE);
        filter.addAction(ConstValues.ACTION_INTERCEPT_ENGINE_CHANGE);
        ctx.registerReceiver(this.mReceiver, filter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    private void unRegisterHarasReceiver(Context ctx) {
        ctx.unregisterReceiver(this.mReceiver);
    }

    private void syncHwBlackListByNBDeleted() {
        List<String> blockedNumbers = GoogleBlackListContract.getBlockedNumbers();
        for (String blackNumber : DBAdapter.getBlacklistedPhones(GlobalContext.getContext())) {
            if (!blockedNumbers.contains(blackNumber)) {
                HwLog.i(TAG, "syncHwBlackListByNBDeleted delete Google Blocked Number ");
                DBAdapter.deleteBlacklistDft(GlobalContext.getContext(), blackNumber);
            }
        }
    }

    private void syncHwBlackListByNBAdded(String blockedNumber) {
        if (!DBAdapter.getBlacklistedPhones(GlobalContext.getContext()).contains(blockedNumber)) {
            HwLog.i(TAG, "syncHwBlackListByNBAdded add Google Blocked Number ");
            DBAdapter.addBlacklist(GlobalContext.getContext(), blockedNumber, "", 3);
        }
    }

    private void syncWhiteListByBlackNumberAdded(String blockedNumber) {
        List<String> whiteList = DBAdapter.getWhitelistPhones(GlobalContext.getContext());
        HwLog.i(TAG, "syncWhiteListByBlackNumberAdded block number  white list size = " + whiteList.size());
        if (!whiteList.contains(blockedNumber)) {
            return;
        }
        if (DBAdapter.deleteWhitelist(GlobalContext.getContext(), blockedNumber) > 0) {
            HwLog.i(TAG, "syncWhiteListByBlackNumberAdded black number has been removed in white list ");
        } else {
            HwLog.e(TAG, "syncWhiteListByBlackNumberAdded black number not in white list ");
        }
    }

    private void initGoogleBlackList() {
        long startTime = SystemClock.elapsedRealtime();
        int addGoolgeBlackListNum = 0;
        int addHwBlackListNum = 0;
        List<BlacklistInfo> blacklist = DBAdapter.getBlacklist(GlobalContext.getContext());
        HashSet<String> hwBlackListMaps = Sets.newHashSet();
        for (BlacklistInfo blackInfo : blacklist) {
            if (blackInfo.getOption() != 3) {
                DBAdapter.updateBlackListOption(GlobalContext.getContext(), blackInfo.getId(), 3);
            }
            HwLog.i(TAG, "initGoogleBlackList add number to google nb ");
            if (GoogleBlackListContract.addBlockedNumber(blackInfo.getPhone())) {
                addGoolgeBlackListNum++;
            }
            hwBlackListMaps.add(blackInfo.getPhone());
        }
        for (String number : GoogleBlackListContract.getBlockedNumbers()) {
            if (!hwBlackListMaps.contains(number)) {
                addHwBlackListNum++;
                DBAdapter.addBlacklist(GlobalContext.getContext(), number, "", 3);
            }
        }
        HwLog.i(TAG, "initGoogleBlackList, called, add number into googleblack:" + addGoolgeBlackListNum + ", add number into hwBlack:" + addHwBlackListNum + ", cost time:" + (SystemClock.elapsedRealtime() - startTime));
        this.mContext.getContentResolver().registerContentObserver(GoogleBlackListContract.BLACK_LIST_URI, true, this.mBlackListObserver);
        this.mContext.getContentResolver().registerContentObserver(DBAdapter.whitelist_uri, true, this.mWhiteListObserver);
    }

    private void syncBlackListByWhiteNumber() {
        List<String> whiteList = DBAdapter.getWhitelistPhones(this.mContext);
        List<String> blackList = GoogleBlackListContract.getBlockedNumbers();
        HwLog.i(TAG, "syncWhiteListByBlackNumber black list size = " + blackList.size() + " white list size = " + whiteList.size());
        for (String whiteNumber : whiteList) {
            if (blackList.contains(whiteNumber)) {
                int res = DBAdapter.deleteBlacklist(GlobalContext.getContext(), whiteNumber);
                GoogleBlackListContract.deleteBlockedNumber(whiteNumber);
                if (res > 0) {
                    HwLog.i(TAG, "white number has been removed in black list ");
                } else {
                    HwLog.e(TAG, "white number not in black list ");
                }
            }
        }
    }
}
