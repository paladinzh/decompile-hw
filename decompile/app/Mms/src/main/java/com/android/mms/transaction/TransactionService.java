package com.android.mms.transaction;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Outbox;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.amap.api.services.core.AMapException;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.messaging.util.BugleActivityUtil;
import com.android.messaging.util.OsUtil;
import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageListAdapter;
import com.android.mms.ui.MessageUtils;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.HwCustEcidLookup;
import com.android.mms.util.RateController;
import com.google.android.gms.R;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduParser;
import com.google.android.mms.pdu.PduPersister;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ErrorMonitor;
import com.huawei.cspcommon.ex.ErrorMonitor.Radar;
import com.huawei.cspcommon.ex.HandlerEx;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.CspFragment;
import com.huawei.mms.util.DelaySendManager;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwCustHwMessageUtils;
import com.huawei.mms.util.HwCustUpdateUserBehavior;
import com.huawei.mms.util.HwSpecialUtils;
import com.huawei.mms.util.MmsCommon;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.rcs.incallui.service.MessagePlusService;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TransactionService extends Service implements Observer {
    private static HwCustEcidLookup mHwCustEcidLookup = ((HwCustEcidLookup) HwCustUtils.createObj(HwCustEcidLookup.class, new Object[0]));
    private final String BEGIN_MMS_CONNECTIVITY_ERROR = "Cannot establish MMS connectivity";
    private boolean isHWRetry = false;
    private ConnectivityManager mConnMgr;
    private HwCustHwMessageUtils mHwCustHwMessageUtils = null;
    private HwCustTransactionService mHwCustTransactionService = null;
    private HwCustUpdateUserBehavior mHwCustUpdateUserBehavior = null;
    private final ArrayList<Transaction> mPending = new ArrayList();
    private PhoneStateListener mPhoneListener = null;
    private TelephonyManager mPhoneManager = null;
    private PowerOffReceiver mPowerOffReceiver;
    private final ArrayList<Transaction> mProcessing = new ArrayList();
    private ConnectivityBroadcastReceiver mReceiver;
    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    private int mSubInUse = -1;
    public Handler mToastHandler = new HandlerEx() {
        public void handleMessage(Message msg) {
            CharSequence str = null;
            if (msg.what == 5) {
                str = TransactionService.this.getString(R.string.mms_download_failed);
            } else if (msg.what == 3) {
                str = TransactionService.this.getString(R.string.no_apn);
            }
            if (TransactionService.this.mHwCustHwMessageUtils != null) {
                str = TransactionService.this.mHwCustHwMessageUtils.getMessageQueuedStr(msg, str, TransactionService.this.getApplicationContext());
            }
            if (str != null) {
                ResEx.makeToast(str, 1);
            }
        }
    };
    private WakeLock mWakeLock;
    private long startTime;
    private boolean transaction_failed_notified = false;

    private class CallStateListener extends PhoneStateListener {
        CallStateListener() {
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            MLog.e("Mms_TXM_SVC", "CallStateChanged add onCallStateChanged: " + state);
            if (state == 0) {
                MLog.i("Mms_TXM_SVC", "CallStateChanged to IDEL ");
                TransactionService.this.mServiceHandler.processPendingTransaction(null, null);
                if (TransactionService.this.mPhoneManager != null) {
                    TransactionService.this.mPhoneManager.listen(TransactionService.this.mPhoneListener, 0);
                    return;
                }
                return;
            }
            MLog.i("Mms_TXM_SVC", "CallStateChanged to " + state);
        }
    }

    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        private ConnectivityBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            boolean z = true;
            String action = intent.getAction();
            if (MLog.isLoggable("Mms_TXN", 2)) {
                MLog.w("Mms_TXM_SVC", "ConnectivityBroadcastReceiver.onReceive() action: " + action);
            }
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                NetworkInfo mmsNetworkInfo = null;
                if (TransactionService.this.mConnMgr != null) {
                    mmsNetworkInfo = TransactionService.this.mConnMgr.getNetworkInfo(2);
                } else if (MLog.isLoggable("Mms_TXN", 2)) {
                    MLog.v("Mms_TXM_SVC", "mConnMgr is null, bail");
                }
                TransactionService.this.log("Handle ConnectivityBroadcastReceiver.onReceive(): " + mmsNetworkInfo);
                if (mmsNetworkInfo == null) {
                    TransactionService.this.log("mms type is null or mobile data is turned off, bail");
                    TransactionService.this.mSubInUse = -1;
                } else {
                    if (TransactionService.this.isHWRetry && !mmsNetworkInfo.isAvailable()) {
                        MLog.d("Mms_TXM_SVC", "net not available");
                        TransactionService.this.mSubInUse = -1;
                    } else if (mmsNetworkInfo.getType() != 2) {
                        if (MLog.isLoggable("Mms_TXN", 2)) {
                            MLog.v("Mms_TXM_SVC", "   type is not TYPE_MOBILE_MMS, bail");
                        }
                        if ("2GVoiceCallEnded".equals(mmsNetworkInfo.getReason())) {
                            if (MLog.isLoggable("Mms_TXN", 2)) {
                                MLog.v("Mms_TXM_SVC", "   reason is 2GVoiceCallEnded, retrying mms connectivity");
                            }
                            TransactionService.this.renewMmsConnectivity();
                        }
                        return;
                    }
                    if (mmsNetworkInfo.isConnected()) {
                        TransactionSettings settings = new TransactionSettings(TransactionService.this, mmsNetworkInfo.getExtraInfo(), TransactionService.this.mSubInUse);
                        if (TextUtils.isEmpty(settings.getMmscUrl())) {
                            MLog.v("Mms_TXM_SVC", "   empty MMSC url, bail");
                            TransactionService.this.mToastHandler.sendEmptyMessage(3);
                            TransactionService.this.mServiceHandler.markAllPendingTransactionsAsFailed();
                            TransactionService.this.endMmsConnectivity();
                            return;
                        }
                        TransactionService.this.mServiceHandler.processPendingTransaction(null, settings);
                    } else {
                        if (MLog.isLoggable("Mms_TXN", 2)) {
                            MLog.v("Mms_TXM_SVC", "   TYPE_MOBILE_MMS not connected, bail");
                        }
                        if (mmsNetworkInfo.isAvailable()) {
                            if (!(mmsNetworkInfo.getState() == State.CONNECTING || mmsNetworkInfo.getState() == State.SUSPENDED)) {
                                z = false;
                            }
                            if (!z) {
                                synchronized (TransactionService.this.mProcessing) {
                                    z = TransactionService.this.mProcessing.size() > 0 || TransactionService.this.mPending.size() > 0;
                                }
                            }
                            if (MLog.isLoggable("Mms_TXN", 2)) {
                                MLog.v("Mms_TXM_SVC", "   retrying mms connectivity for it's available and renew = " + z);
                            }
                            if (z) {
                                TransactionService.this.renewMmsConnectivity();
                                return;
                            }
                        }
                        TransactionService.this.stopTransactionService(Integer.valueOf(-1));
                    }
                }
            }
        }
    }

    private static class PowerOffReceiver extends BroadcastReceiver {
        private Timer mTimer;

        private PowerOffReceiver() {
            this.mTimer = null;
        }

        private void stopTimer() {
            if (this.mTimer != null) {
                this.mTimer.cancel();
                this.mTimer = null;
                MLog.v("Mms_TXM_SVC", "stopTimer:mTimer=" + this.mTimer);
            }
        }

        private void startTimer() {
            stopTimer();
            this.mTimer = new Timer();
            MLog.v("Mms_TXM_SVC", "startTimer:" + this.mTimer);
        }

        public void onReceive(final Context context, Intent intent) {
            if (MLog.isLoggable("Mms_TXN", 2)) {
                MLog.v("Mms_TXM_SVC", "Intent received");
            }
            String action = intent.getAction();
            if ("android.intent.action.SCREEN_ON".equals(action)) {
                stopTimer();
            }
            if ("android.intent.action.SCREEN_OFF".equals(action)) {
                MLog.d("Mms_TXM_SVC", "Screen off :" + action);
                startTimer();
                try {
                    this.mTimer.schedule(new TimerTask() {
                        public void run() {
                            try {
                                DownloadManager.getInstance().updateStateToUnstarted(context);
                                ContentValues values = new ContentValues(1);
                                values.put("err_type", Integer.valueOf(10));
                                SqliteWrapper.update(context, context.getContentResolver(), PendingMessages.CONTENT_URI, values, "msg_type = 128", null);
                            } catch (Throwable e) {
                                ErrorMonitor.reportErrorInfo(2, "TransactionService Exception in TimerTask.", e);
                            }
                            context.stopService(new Intent(context, TransactionService.class));
                            HwBackgroundLoader.getUIHandler().post(new Runnable() {
                                public void run() {
                                    PowerOffReceiver.this.stopTimer();
                                }
                            });
                        }
                    }, 1200000);
                } catch (Exception e) {
                    ErrorMonitor.reportErrorInfo(2, "Transaction Service when schedual Timer ", e);
                }
            }
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        private String decodeMessage(Message msg) {
            if (msg.what == 100) {
                return "EVENT_QUIT";
            }
            if (msg.what == 3) {
                return "EVENT_CONTINUE_MMS_CONNECTIVITY";
            }
            if (msg.what == 1) {
                return "EVENT_TRANSACTION_REQUEST";
            }
            if (msg.what == 4) {
                return "EVENT_HANDLE_NEXT_PENDING_TRANSACTION";
            }
            if (msg.what == 5) {
                return "EVENT_NEW_INTENT";
            }
            if (msg.what == 101) {
                return "EVENT_STOP_SERVICE";
            }
            if (msg.what == 102) {
                return "EVENT_PENDING_TRANSACTION_TIMEOUT";
            }
            return "unknown message.what";
        }

        private String decodeTransactionType(int transactionType) {
            if (transactionType == 0) {
                return "NOTIFICATION_TRANSACTION";
            }
            if (transactionType == 1) {
                return "RETRIEVE_TRANSACTION";
            }
            if (transactionType == 2) {
                return "SEND_TRANSACTION";
            }
            if (transactionType == 3) {
                return "READREC_TRANSACTION";
            }
            return "invalid transaction type";
        }

        private void sendPendingTransactionTimeoutEvent(Transaction transaction) {
            TransactionService.this.mServiceHandler.sendMessageDelayed(TransactionService.this.mServiceHandler.obtainMessage(102, transaction), 180000);
            MLog.d("Mms_TXM_SVC", "sendPendingTransactionTimeoutEvent for transaction:" + transaction);
        }

        private void processPendingTransactionTimeout(Transaction transaction) {
            Uri uri = null;
            if (transaction != null) {
                boolean hasTransaction;
                MLog.d("Mms_TXM_SVC", "processPendingTransactionTimeout for transaction:" + transaction);
                synchronized (TransactionService.this.mProcessing) {
                    hasTransaction = TransactionService.this.mPending.remove(transaction);
                    if (TransactionService.this.mPending.size() == 0 && TransactionService.this.mProcessing.size() == 0) {
                        TransactionService.this.releaseWakeLock();
                    }
                }
                if (hasTransaction) {
                    if ((transaction instanceof NotificationTransaction) || (transaction instanceof RetrieveTransaction) || (transaction instanceof SendTransaction)) {
                        uri = transaction.getUri();
                        transaction.mTransactionState.setState(2);
                        transaction.mTransactionState.setContentUri(uri);
                    }
                    ContentValues values = new ContentValues(1);
                    values.put("resp_st", Integer.valueOf(130));
                    SqliteWrapper.update(TransactionService.this, TransactionService.this.getContentResolver(), uri, values, null, null);
                    transaction.notifyObservers();
                    Radar.reportChr(transaction.getSubscription(), 1339, "mms network overtime");
                }
                TransactionService.this.stopSelfIfIdle(transaction.getServiceId());
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            Exception ex;
            Uri uri;
            Throwable th;
            if (MLog.isLoggable("Mms_TXN", 2)) {
                MLog.v("Mms_TXM_SVC", "Handling incoming message: " + msg + " = " + decodeMessage(msg));
            }
            Transaction transaction = null;
            switch (msg.what) {
                case 1:
                    if (TransactionService.this.mHwCustTransactionService != null) {
                        TransactionService.this.mHwCustTransactionService.handleRequest(TransactionService.this.getApplicationContext());
                    }
                    int serviceId = msg.arg1;
                    int transactionType;
                    try {
                        TransactionSettings transactionSettings;
                        TransactionBundle args = msg.obj;
                        if (MLog.isLoggable("Mms_TXN", 2)) {
                            MLog.v("Mms_TXM_SVC", "EVENT_TRANSACTION_REQUEST");
                        }
                        String mmsc = args.getMmscUrl();
                        if (mmsc != null) {
                            MLog.d("Mms_TXM_SVC", "Create TransactionSettings from msg.obj");
                            transactionSettings = new TransactionSettings(mmsc, args.getProxyAddress(), args.getProxyPort());
                        } else if (MmsCommon.PLATFORM_MTK) {
                            transactionSettings = new TransactionSettings(TransactionService.this, null, msg.arg2);
                        } else {
                            transactionSettings = new TransactionSettings(TransactionService.this, null);
                        }
                        transactionType = args.getTransactionType();
                        if (MLog.isLoggable("Mms_TXN", 2)) {
                            MLog.v("Mms_TXM_SVC", "handle EVENT_TRANSACTION_REQUEST: transactionType=" + transactionType + " " + decodeTransactionType(transactionType));
                        }
                        switch (transactionType) {
                            case 0:
                                if (TransactionService.this.mHwCustUpdateUserBehavior != null) {
                                    TransactionService.this.mHwCustUpdateUserBehavior.upLoadReceiveMesInfo(TransactionService.this.getApplicationContext(), 1);
                                }
                                String uri2 = args.getUri();
                                if (uri2 != null) {
                                    transaction = new NotificationTransaction(TransactionService.this, serviceId, transactionSettings, uri2);
                                } else {
                                    byte[] pushData = args.getPushData();
                                    if (pushData == null) {
                                        MLog.e("Mms_TXM_SVC", "null PUSH data.");
                                        if (MLog.isLoggable("Mms_TXN", 2)) {
                                            MLog.v("Mms_TXM_SVC", "Transaction was null. Stopping self: " + serviceId);
                                        }
                                        TransactionService.this.endMmsConnectivity();
                                        if (MessageUtils.isMultiSimEnabled()) {
                                            TransactionService.this.stopSelfIfIdle(serviceId);
                                        } else {
                                            TransactionService.this.stopSelf(serviceId);
                                        }
                                        return;
                                    }
                                    GenericPdu ind = new PduParser(pushData, false).parse();
                                    if (ind == null || ind.getMessageType() != 130) {
                                        MLog.e("Mms_TXM_SVC", "Invalid PUSH data.");
                                        if (MLog.isLoggable("Mms_TXN", 2)) {
                                            MLog.v("Mms_TXM_SVC", "Transaction was null. Stopping self: " + serviceId);
                                        }
                                        TransactionService.this.endMmsConnectivity();
                                        if (MessageUtils.isMultiSimEnabled()) {
                                            TransactionService.this.stopSelfIfIdle(serviceId);
                                        } else {
                                            TransactionService.this.stopSelf(serviceId);
                                        }
                                        return;
                                    }
                                    transaction = new NotificationTransaction(TransactionService.this, serviceId, transactionSettings, (NotificationInd) ind);
                                }
                                break;
                            case 1:
                                Transaction transaction2 = new RetrieveTransaction(TransactionService.this, serviceId, transactionSettings, args.getUri());
                                try {
                                    ((RetrieveTransaction) transaction2).setDownloadButtonClickCount(args.getButtonDownloadClickCount());
                                    transaction = transaction2;
                                    if (4 != transactionType) {
                                        stopRetrieveTransaction(transaction);
                                    } else if (!processTransaction(transaction)) {
                                        if (MLog.isLoggable("Mms_TXN", 2)) {
                                            MLog.v("Mms_TXM_SVC", "Transaction was null. Stopping self: " + serviceId);
                                        }
                                        TransactionService.this.endMmsConnectivity();
                                        if (MessageUtils.isMultiSimEnabled()) {
                                            TransactionService.this.stopSelfIfIdle(serviceId);
                                        } else {
                                            TransactionService.this.stopSelf(serviceId);
                                        }
                                        return;
                                    }
                                    if (MLog.isLoggable("Mms_TXN", 2)) {
                                        MLog.v("Mms_TXM_SVC", "Started processing of incoming message");
                                    }
                                    if (transaction == null) {
                                        if (MLog.isLoggable("Mms_TXN", 2)) {
                                            MLog.v("Mms_TXM_SVC", "Transaction was null. Stopping self: " + serviceId);
                                        }
                                        TransactionService.this.endMmsConnectivity();
                                        if (MessageUtils.isMultiSimEnabled()) {
                                            TransactionService.this.stopSelfIfIdle(serviceId);
                                        } else {
                                            TransactionService.this.stopSelf(serviceId);
                                        }
                                    }
                                } catch (Exception e) {
                                    ex = e;
                                    transaction = transaction2;
                                    try {
                                        MLog.w("Mms_TXM_SVC", "Exception occurred while handling message: " + msg + " " + ex.getMessage());
                                        if (transaction != null) {
                                            if ((!(transaction instanceof NotificationTransaction) || (transaction instanceof RetrieveTransaction)) && "Cannot establish MMS connectivity".equals(ex.getMessage())) {
                                                uri = transaction.getUri();
                                                if (uri != null) {
                                                    MLog.i("Mms_TXM_SVC", "reset mms to unstarted state");
                                                    DownloadManager.getInstance().markState(uri, 128);
                                                }
                                            } else {
                                                try {
                                                    if ((transaction instanceof SendTransaction) && MessageUtils.isMultiSimEnabled() && 2 == MessageUtils.getIccCardStatus(transaction.getSubscription()) && "Cannot establish MMS connectivity".equals(ex.getMessage())) {
                                                        handleCardNotInsert(transaction);
                                                    }
                                                } catch (Throwable th2) {
                                                    transaction = null;
                                                }
                                            }
                                            transaction.detach(TransactionService.this);
                                            synchronized (TransactionService.this.mProcessing) {
                                                if (TransactionService.this.mProcessing.contains(transaction)) {
                                                    TransactionService.this.mProcessing.remove(transaction);
                                                }
                                            }
                                            transaction = null;
                                        }
                                        transactionType = ((TransactionBundle) msg.obj).getTransactionType();
                                        MLog.i("Mms_TXM_SVC", "e.getMessage =" + ex.getMessage());
                                        if (transactionType == 0 && "Cannot establish MMS connectivity".equals(ex.getMessage())) {
                                            MessagingNotification.blockingUpdateNewMessageIndicator(TransactionService.this, -1, false);
                                        }
                                    } catch (Exception e2) {
                                        MLog.e("Mms_TXM_SVC", "TransactionService blockingUpdate or updateDownload Exception:", (Throwable) e2);
                                    } catch (Throwable th3) {
                                        th = th3;
                                    }
                                    if (transaction == null) {
                                        if (MLog.isLoggable("Mms_TXN", 2)) {
                                            MLog.v("Mms_TXM_SVC", "Transaction was null. Stopping self: " + serviceId);
                                        }
                                        TransactionService.this.endMmsConnectivity();
                                        if (MessageUtils.isMultiSimEnabled()) {
                                            TransactionService.this.stopSelf(serviceId);
                                        } else {
                                            TransactionService.this.stopSelfIfIdle(serviceId);
                                        }
                                    }
                                    return;
                                } catch (Throwable th4) {
                                    th = th4;
                                    transaction = transaction2;
                                    if (transaction == null) {
                                        if (MLog.isLoggable("Mms_TXN", 2)) {
                                            MLog.v("Mms_TXM_SVC", "Transaction was null. Stopping self: " + serviceId);
                                        }
                                        TransactionService.this.endMmsConnectivity();
                                        if (MessageUtils.isMultiSimEnabled()) {
                                            TransactionService.this.stopSelfIfIdle(serviceId);
                                        } else {
                                            TransactionService.this.stopSelf(serviceId);
                                        }
                                    }
                                    throw th;
                                }
                                break;
                            case 2:
                                transaction = new SendTransaction(TransactionService.this, serviceId, transactionSettings, args.getUri());
                                if (4 != transactionType) {
                                    stopRetrieveTransaction(transaction);
                                } else if (processTransaction(transaction)) {
                                    if (MLog.isLoggable("Mms_TXN", 2)) {
                                        MLog.v("Mms_TXM_SVC", "Transaction was null. Stopping self: " + serviceId);
                                    }
                                    TransactionService.this.endMmsConnectivity();
                                    if (MessageUtils.isMultiSimEnabled()) {
                                        TransactionService.this.stopSelf(serviceId);
                                    } else {
                                        TransactionService.this.stopSelfIfIdle(serviceId);
                                    }
                                    return;
                                }
                                if (MLog.isLoggable("Mms_TXN", 2)) {
                                    MLog.v("Mms_TXM_SVC", "Started processing of incoming message");
                                }
                                if (transaction == null) {
                                    if (MLog.isLoggable("Mms_TXN", 2)) {
                                        MLog.v("Mms_TXM_SVC", "Transaction was null. Stopping self: " + serviceId);
                                    }
                                    TransactionService.this.endMmsConnectivity();
                                    if (MessageUtils.isMultiSimEnabled()) {
                                        TransactionService.this.stopSelfIfIdle(serviceId);
                                        break;
                                    } else {
                                        TransactionService.this.stopSelf(serviceId);
                                        break;
                                    }
                                }
                                break;
                            case 3:
                                transaction = new ReadRecTransaction(TransactionService.this, serviceId, transactionSettings, args.getUri());
                                if (4 != transactionType) {
                                    stopRetrieveTransaction(transaction);
                                } else if (processTransaction(transaction)) {
                                    if (MLog.isLoggable("Mms_TXN", 2)) {
                                        MLog.v("Mms_TXM_SVC", "Transaction was null. Stopping self: " + serviceId);
                                    }
                                    TransactionService.this.endMmsConnectivity();
                                    if (MessageUtils.isMultiSimEnabled()) {
                                        TransactionService.this.stopSelfIfIdle(serviceId);
                                    } else {
                                        TransactionService.this.stopSelf(serviceId);
                                    }
                                    return;
                                }
                                if (MLog.isLoggable("Mms_TXN", 2)) {
                                    MLog.v("Mms_TXM_SVC", "Started processing of incoming message");
                                }
                                if (transaction == null) {
                                    if (MLog.isLoggable("Mms_TXN", 2)) {
                                        MLog.v("Mms_TXM_SVC", "Transaction was null. Stopping self: " + serviceId);
                                    }
                                    TransactionService.this.endMmsConnectivity();
                                    if (MessageUtils.isMultiSimEnabled()) {
                                        TransactionService.this.stopSelf(serviceId);
                                        break;
                                    } else {
                                        TransactionService.this.stopSelfIfIdle(serviceId);
                                        break;
                                    }
                                }
                                break;
                            case 4:
                                transaction = new StopRetrieveTransaction(TransactionService.this, serviceId, transactionSettings, args.getUri());
                            default:
                                MLog.w("Mms_TXM_SVC", "Invalid transaction type: " + serviceId);
                                if (MLog.isLoggable("Mms_TXN", 2)) {
                                    MLog.v("Mms_TXM_SVC", "Transaction was null. Stopping self: " + serviceId);
                                }
                                TransactionService.this.endMmsConnectivity();
                                if (MessageUtils.isMultiSimEnabled()) {
                                    TransactionService.this.stopSelfIfIdle(serviceId);
                                } else {
                                    TransactionService.this.stopSelf(serviceId);
                                }
                                return;
                        }
                    } catch (Exception e3) {
                        ex = e3;
                        MLog.w("Mms_TXM_SVC", "Exception occurred while handling message: " + msg + " " + ex.getMessage());
                        if (transaction != null) {
                            if (transaction instanceof NotificationTransaction) {
                                break;
                            }
                            uri = transaction.getUri();
                            if (uri != null) {
                                MLog.i("Mms_TXM_SVC", "reset mms to unstarted state");
                                DownloadManager.getInstance().markState(uri, 128);
                            }
                            transaction.detach(TransactionService.this);
                            synchronized (TransactionService.this.mProcessing) {
                                if (TransactionService.this.mProcessing.contains(transaction)) {
                                    TransactionService.this.mProcessing.remove(transaction);
                                }
                            }
                            transaction = null;
                            break;
                        }
                        transactionType = ((TransactionBundle) msg.obj).getTransactionType();
                        MLog.i("Mms_TXM_SVC", "e.getMessage =" + ex.getMessage());
                        MessagingNotification.blockingUpdateNewMessageIndicator(TransactionService.this, -1, false);
                        if (transaction == null) {
                            if (MLog.isLoggable("Mms_TXN", 2)) {
                                MLog.v("Mms_TXM_SVC", "Transaction was null. Stopping self: " + serviceId);
                            }
                            TransactionService.this.endMmsConnectivity();
                            if (MessageUtils.isMultiSimEnabled()) {
                                TransactionService.this.stopSelf(serviceId);
                            } else {
                                TransactionService.this.stopSelfIfIdle(serviceId);
                            }
                        }
                        return;
                    }
                    break;
                case 3:
                    continueMmsConnectivity();
                    return;
                case 4:
                    processPendingTransaction(null, (TransactionSettings) msg.obj);
                    return;
                case 5:
                    TransactionService.this.onNewIntent((Intent) msg.obj, msg.arg1);
                    return;
                case 100:
                    getLooper().quit();
                    return;
                case 101:
                    TransactionService.this.stopSelfIfIdle(msg.arg1);
                    return;
                case 102:
                    processPendingTransactionTimeout((Transaction) msg.obj);
                    return;
                default:
                    MLog.w("Mms_TXM_SVC", "what=" + msg.what);
                    return;
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void continueMmsConnectivity() {
            synchronized (TransactionService.this.mProcessing) {
                if (TransactionService.this.mProcessing.isEmpty()) {
                    MLog.v("Mms_TXM_SVC", "mProcessing is empty, return");
                }
            }
        }

        private void handleCardNotInsert(Transaction transaction) {
            Uri uri = ((SendTransaction) transaction).mSendReqURI;
            long msgId = ContentUris.parseId(uri);
            Builder uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
            uriBuilder.appendQueryParameter("protocol", "mms");
            uriBuilder.appendQueryParameter("message", String.valueOf(msgId));
            Cursor cursor = SqliteWrapper.query(TransactionService.this, TransactionService.this.getContentResolver(), uriBuilder.build(), null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                        int retryIndex = cursor.getInt(cursor.getColumnIndexOrThrow("retry_index")) + 1;
                        long current = System.currentTimeMillis();
                        ContentValues readValues = new ContentValues(1);
                        readValues.put("read", Integer.valueOf(0));
                        SqliteWrapper.update(TransactionService.this, TransactionService.this.getContentResolver(), uri, readValues, null, null);
                        MessagingNotification.notifySendFailed(TransactionService.this, true);
                        ContentValues values = new ContentValues(3);
                        values.put("err_type", Integer.valueOf(10));
                        values.put("retry_index", Integer.valueOf(retryIndex));
                        values.put("last_try", Long.valueOf(current));
                        SqliteWrapper.update(TransactionService.this, TransactionService.this.getContentResolver(), PendingMessages.CONTENT_URI, values, "_id=" + cursor.getLong(cursor.getColumnIndexOrThrow("_id")), null);
                    }
                    cursor.close();
                } catch (Throwable th) {
                    cursor.close();
                }
            }
        }

        public void markAllPendingTransactionsAsFailed() {
            synchronized (TransactionService.this.mProcessing) {
                while (TransactionService.this.mPending.size() != 0) {
                    Transaction transaction = (Transaction) TransactionService.this.mPending.remove(0);
                    MLog.d("Mms_TXM_SVC", "remove EVENT_PENDING_TRANSACTION_TIMEOUT for markAllPendingTransactionsAsFailed");
                    TransactionService.this.mServiceHandler.removeMessages(102, transaction);
                    transaction.mTransactionState.setState(2);
                    if (transaction instanceof SendTransaction) {
                        Uri uri = ((SendTransaction) transaction).mSendReqURI;
                        transaction.mTransactionState.setContentUri(uri);
                        ContentValues values = new ContentValues(1);
                        values.put("resp_st", Integer.valueOf(134));
                        SqliteWrapper.update(TransactionService.this, TransactionService.this.getContentResolver(), uri, values, null, null);
                    }
                    transaction.notifyObservers();
                }
            }
        }

        public void processPendingTransaction(Transaction transaction, TransactionSettings settings) {
            TransactionService.this.log("processPendingTxn: transaction=" + transaction);
            int cursub = -1;
            if (MessageUtils.isMultiSimEnabled()) {
                cursub = TransactionService.this.getPreferredDataSubscription();
            }
            synchronized (TransactionService.this.mProcessing) {
                if (TransactionService.this.mPending.size() != 0) {
                    if (MessageUtils.isMultiSimEnabled()) {
                        if (cursub != TransactionService.this.mSubInUse) {
                            MLog.e("Mms_TXM_SVC", "ProcessPending Transaction with prefer sub " + cursub + " " + " active sub " + TransactionService.this.mSubInUse);
                        }
                        for (Transaction t : TransactionService.this.mPending) {
                            if (-1 != cursub && t.getSubscription() == cursub) {
                                transaction = t;
                                break;
                            }
                        }
                    }
                    if (transaction != null) {
                        TransactionService.this.mPending.remove(transaction);
                    } else {
                        transaction = (Transaction) TransactionService.this.mPending.remove(0);
                    }
                }
                synchronized (TransactionService.this.mProcessing) {
                    int numProcessTransaction = TransactionService.this.mProcessing.size();
                }
            }
            if (transaction == null) {
                if (numProcessTransaction == 0) {
                    MLog.w("Mms_TXM_SVC", "processPending Transaction: no more transaction, endMmsConnectivity");
                    TransactionService.this.endMmsConnectivity();
                }
                return;
            }
            if (MessageUtils.isMultiSimEnabled() && transaction.getSubscription() != cursub) {
                if (numProcessTransaction != 0) {
                    TransactionService.this.log("processPendingTransaction: processing list not empty, add transaction to pending list again.");
                    synchronized (TransactionService.this.mProcessing) {
                        TransactionService.this.mPending.add(0, transaction);
                    }
                    return;
                }
                TransactionService.this.endMmsConnectivity();
            }
            if (settings != null) {
                transaction.setConnectionSettings(settings);
            }
            try {
                int serviceId = transaction.getServiceId();
                MLog.w("Mms_TXM_SVC", "processPending Transaction : " + serviceId + "-" + transaction.mId + ", sub: " + TransactionService.this.mSubInUse);
                if (processTransaction(transaction)) {
                    TransactionService.this.log("Started deferred processing of transaction  " + transaction);
                } else {
                    TransactionService.this.stopSelf(serviceId);
                }
            } catch (IOException e) {
                ErrorMonitor.reportErrorInfo(2, e.getMessage(), e);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean processTransaction(Transaction transaction) throws IOException {
            synchronized (TransactionService.this.mProcessing) {
                int curSub;
                for (Transaction t : TransactionService.this.mPending) {
                    if (t.isEquivalent(transaction)) {
                        TransactionService.this.log("Transaction already pending: " + transaction.getServiceId());
                        TransactionService.this.reProcessPendingTransaction(t);
                        return true;
                    }
                }
                for (Transaction t2 : TransactionService.this.mProcessing) {
                    if (t2.isEquivalent(transaction)) {
                        TransactionService.this.log("Duplicated transaction: " + transaction.getServiceId());
                        return true;
                    }
                }
                TransactionService.this.log("processTransaction: call beginMmsConnectivity...");
                int connectivityResult = 3;
                if (MessageUtils.isMultiSimEnabled()) {
                    curSub = transaction.getSubscription();
                    if (!TransactionService.this.isHWRetry || TransactionService.this.isNetworkAvailable(curSub)) {
                        if (TransactionService.this.mSubInUse != -1) {
                            if (transaction.getSubscription() != TransactionService.this.mSubInUse) {
                                TransactionService.this.mPending.add(transaction);
                                MLog.e("Mms_TXM_SVC", "processTransaction: add to pending list -- different with current sub=" + TransactionService.this.mSubInUse);
                                return true;
                            }
                            MLog.e("DSMMS", "Not same subID, pending process subID is: " + TransactionService.this.mSubInUse);
                        }
                        if (MessageUtils.getIccCardStatus(curSub) != 1) {
                            throw new IOException("Cannot establish MMS connectivity");
                        }
                        connectivityResult = TransactionService.this.beginMmsConnectivity(curSub);
                    } else {
                        MLog.e("Mms_TXM_SVC", "HWRetry scheme and nonetwork, avoid beginMmsConectivity, or it will stop transaction");
                    }
                } else if (!TransactionService.this.isHWRetry || TransactionService.this.isNetworkAvailable()) {
                    connectivityResult = TransactionService.this.beginMmsConnectivity();
                } else {
                    MLog.v("Mms_TXM_SVC", "HWRetry scheme and nonetwork, avoid beginMmsConectivity, or it will stop transaction");
                }
                if (connectivityResult == 2560) {
                    MLog.v("Mms_TXM_SVC", "processTransaction: active apn fail and phone is busy now");
                    TransactionService.this.mPending.add(transaction);
                    TransactionService.this.reProcessPendingTransaction(transaction);
                    return true;
                } else if (connectivityResult == 1 || (TransactionService.this.mHwCustTransactionService != null && TransactionService.this.mHwCustTransactionService.isUsingWifi(connectivityResult))) {
                    int callState;
                    TransactionService.this.mPending.add(transaction);
                    if (MLog.isLoggable("Mms_TXN", 2)) {
                        MLog.v("Mms_TXM_SVC", "processTransaction: connResult=APN_REQUEST_STARTED, defer transaction pending MMS connectivity");
                    }
                    DownloadManager downloadManager = DownloadManager.getInstance();
                    boolean isAutoDownload = downloadManager.isAuto();
                    if (MessageUtils.isMultiSimEnabled()) {
                        curSub = transaction.getSubscription();
                        isAutoDownload = downloadManager.isAuto(curSub);
                        callState = MmsApp.getDefaultTelephonyManager().getCallState(curSub);
                    } else {
                        callState = MmsApp.getDefaultTelephonyManager().getCallState();
                    }
                    Uri uri = transaction.getUri();
                    if (uri != null && isAutoDownload && downloadManager.getState(uri) == 128) {
                        downloadManager.markState(uri, 129);
                        MessageListAdapter.saveConnectionManagerToMap(uri.toString(), false, true, false, null);
                        MLog.d("Mms_TXM_SVC", "Mark state as started for " + transaction.getType());
                    }
                    if (callState == 0) {
                        MLog.w("Mms_TXM_SVC", " Call state is IDLE, Check timeout: " + callState);
                        sendPendingTransactionTimeoutEvent(transaction);
                    } else if (TransactionService.this.mPhoneManager == null) {
                        MLog.e("Mms_TXM_SVC", "In Call state no PhoneManager");
                    } else {
                        MLog.w("Mms_TXM_SVC", "In Call state waiting for call state change.");
                        TransactionService.this.mPhoneManager.listen(TransactionService.this.mPhoneListener, 32);
                    }
                    return true;
                } else {
                    if (connectivityResult == 0) {
                        MLog.d("Mms_TXM_SVC", "remove EVENT_PENDING_TRANSACTION_TIMEOUT for APN_ALREADY_ACTIVE");
                        TransactionService.this.mServiceHandler.removeMessages(102, transaction);
                    }
                    if (MessageUtils.isMultiSimEnabled() && connectivityResult == 0 && TransactionService.this.getPreferredDataSubscription() != transaction.getSubscription()) {
                        TransactionService.this.mPending.add(transaction);
                        MLog.e("DSMMS", "processTransaction: add to pending list -- connectivityResult already active, but different sim.");
                        return true;
                    } else if (TransactionService.this.mProcessing.size() > 0) {
                        if (MLog.isLoggable("Mms_TXN", 2)) {
                            MLog.v("Mms_TXM_SVC", "Adding transaction to 'mPending' list: " + transaction);
                        }
                        TransactionService.this.mPending.add(transaction);
                        return true;
                    } else {
                        if (MLog.isLoggable("Mms_TXN", 2)) {
                            MLog.v("Mms_TXM_SVC", "Adding transaction to 'mProcessing' list: " + transaction);
                        }
                        TransactionService.this.mProcessing.add(transaction);
                    }
                }
            }
        }

        private boolean stopRetrieveTransaction(Transaction transaction) throws IOException {
            synchronized (TransactionService.this.mProcessing) {
                Object processingTransaction = null;
                Object pendingTransaction = null;
                for (Transaction t : TransactionService.this.mProcessing) {
                    if (t != null && t.getUri() != null && t.getUri().equals(transaction.getUri())) {
                        MLog.v("Mms_TXM_SVC", "remove mProcessing: " + transaction.getServiceId());
                        processingTransaction = t;
                        break;
                    }
                }
                if (processingTransaction != null) {
                    TransactionService.this.mProcessing.remove(processingTransaction);
                    transaction.attach(TransactionService.this);
                    transaction.process();
                }
                for (Transaction t2 : TransactionService.this.mPending) {
                    if (t2 != null && t2.getUri() != null && t2.getUri().equals(transaction.getUri())) {
                        MLog.v("Mms_TXM_SVC", "remove mPending: " + transaction.getServiceId());
                        pendingTransaction = t2;
                        break;
                    }
                }
                if (pendingTransaction != null) {
                    TransactionService.this.mPending.remove(pendingTransaction);
                    MLog.d("Mms_TXM_SVC", "remove EVENT_PENDING_TRANSACTION_TIMEOUT for stopRetrieveTransaction");
                    TransactionService.this.mServiceHandler.removeMessages(102, transaction);
                }
            }
            return true;
        }
    }

    public void onCreate() {
        super.onCreate();
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("Mms_TXM_SVC", "Creating TransactionService");
        }
        this.mPhoneManager = (TelephonyManager) getApplicationContext().getSystemService("phone");
        this.mPhoneListener = new CallStateListener();
        HandlerThread thread = new HandlerThread("TransactionService");
        thread.start();
        this.mServiceLooper = thread.getLooper();
        this.mServiceHandler = new ServiceHandler(this.mServiceLooper);
        this.mReceiver = new ConnectivityBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(this.mReceiver, intentFilter);
        this.transaction_failed_notified = false;
        boolean z = MmsConfig.getModifyMmsRetryScheme() == 1 ? !MmsCommon.PLATFORM_MTK : false;
        this.isHWRetry = z;
        this.mHwCustTransactionService = (HwCustTransactionService) HwCustUtils.createObj(HwCustTransactionService.class, new Object[0]);
        this.startTime = System.currentTimeMillis();
        this.mPowerOffReceiver = new PowerOffReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        registerReceiver(this.mPowerOffReceiver, intentFilter);
        this.mHwCustUpdateUserBehavior = (HwCustUpdateUserBehavior) HwCustUtils.createObj(HwCustUpdateUserBehavior.class, new Object[0]);
        this.mHwCustHwMessageUtils = (HwCustHwMessageUtils) HwCustUtils.createObj(HwCustHwMessageUtils.class, new Object[0]);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Message msg = this.mServiceHandler.obtainMessage(5);
            msg.arg1 = startId;
            msg.obj = intent;
            this.mServiceHandler.sendMessage(msg);
            removeMessages(101);
            MLog.i("Mms_TXM_SVC", "onStartCommand send EVENT_NEW_INTENT. service-id " + startId);
        }
        return 2;
    }

    public void onNewIntent(Intent intent, int serviceId) {
        if (MmsConfig.isSmsEnabled(this)) {
            this.mConnMgr = (ConnectivityManager) getSystemService("connectivity");
            if (this.mConnMgr == null || !MmsConfig.isSmsEnabled(getApplicationContext())) {
                endMmsConnectivity();
                stopSelf(serviceId);
                return;
            }
            boolean noNetwork = !isNetworkAvailable() && (this.mHwCustTransactionService == null || !this.mHwCustTransactionService.mmsOverWifiEnabled(getApplicationContext()));
            if (MLog.isLoggable("Mms_TXN", 2)) {
                MLog.v("Mms_TXM_SVC", "    networkAvailable=" + (!noNetwork));
            }
            String action = intent.getAction();
            TransactionBundle args;
            if ("android.intent.action.ACTION_ONALARM".equals(action) || "android.intent.action.ACTION_ENABLE_AUTO_RETRIEVE".equals(action) || intent.getExtras() == null) {
                Cursor cursor = null;
                try {
                    cursor = PduPersister.getPduPersister(this).getPendingMessages(System.currentTimeMillis());
                } catch (SQLiteDiskIOException ex) {
                    MLog.e("Mms_TXM_SVC", "TransactionServie onNewIntent has an SQLiteDiskIOException", (Throwable) ex);
                } catch (SQLiteException sqliteEx) {
                    MLog.e("Mms_TXM_SVC", "onNewIntent: there has an SQLiteException >>> " + sqliteEx);
                }
                if (cursor != null) {
                    int count = cursor.getCount();
                    if (MLog.isLoggable("Mms_TXN", 2)) {
                        MLog.v("Mms_TXM_SVC", "onNewIntent: cursor.count=" + count + " action=" + action);
                    }
                    if (count == 0) {
                        if (MLog.isLoggable("Mms_TXN", 2)) {
                            MLog.v("Mms_TXM_SVC", "onNewIntent: no pending messages. Stopping service.");
                        }
                        RetryScheduler.setRetryAlarm(this);
                        stopSelfIfIdle(serviceId);
                        return;
                    }
                    int columnIndexOfMsgId = cursor.getColumnIndexOrThrow("msg_id");
                    int columnIndexOfMsgType = cursor.getColumnIndexOrThrow("msg_type");
                    boolean needRetrieveFailToast = true;
                    boolean needSendFailToast = true;
                    while (cursor.moveToNext()) {
                        int msgType = cursor.getInt(columnIndexOfMsgType);
                        int transactionType = getTransactionType(msgType);
                        if (MLog.isLoggable("Mms_TXN", 2)) {
                            MLog.v("Mms_TXM_SVC", "onNewIntent: msgType=" + msgType + " transactionType=" + transactionType);
                        }
                        if (transactionType != 2 || !DelaySendManager.getInst().isDelayMsg(-cursor.getLong(columnIndexOfMsgId), "mms")) {
                            if (noNetwork && !MessageUtils.isMultiSimEnabled()) {
                                MLog.d("Mms_TXM_SVC", "onNewIntent: no Network and isHWRetry = " + this.isHWRetry);
                                if (!this.isHWRetry) {
                                    onNetworkUnavailable(serviceId, transactionType, this.isHWRetry);
                                    cursor.close();
                                    return;
                                } else if (System.getInt(getContentResolver(), "airplane_mode_on", 0) != 0) {
                                    onNetworkUnavailable(serviceId, -1, false);
                                    cursor.close();
                                    return;
                                } else if (transactionType == 1) {
                                    if (needRetrieveFailToast) {
                                        onNetworkUnavailable(serviceId, transactionType, this.isHWRetry);
                                    }
                                    needRetrieveFailToast = false;
                                } else if (transactionType == 2) {
                                    if (needSendFailToast) {
                                        onNetworkUnavailable(serviceId, transactionType, this.isHWRetry);
                                    }
                                    needSendFailToast = false;
                                } else {
                                    onNetworkUnavailable(serviceId, transactionType, this.isHWRetry);
                                }
                            }
                            Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, cursor.getLong(columnIndexOfMsgId));
                            switch (transactionType) {
                                case -1:
                                    continue;
                                case 1:
                                    try {
                                        int failureType = cursor.getInt(cursor.getColumnIndexOrThrow("err_type"));
                                        DownloadManager downloadManager = DownloadManager.getInstance();
                                        boolean autoDownload = downloadManager.isAuto();
                                        if (MLog.isLoggable("Mms_TXN", 2)) {
                                            MLog.v("Mms_TXM_SVC", "onNewIntent: failureType=" + failureType + " action=" + action + " isTransientFailure:" + isTransientFailure(failureType) + " autoDownload=" + autoDownload + " noNetWork " + noNetwork);
                                        }
                                        if (autoDownload) {
                                            if ((failureType != 0 || !"android.intent.action.ACTION_ENABLE_AUTO_RETRIEVE".equals(action)) && !isTransientFailure(failureType)) {
                                                if (MLog.isLoggable("Mms_TXN", 2)) {
                                                    MLog.v("Mms_TXM_SVC", "onNewIntent: skipping - permanent error");
                                                }
                                                if (noNetwork && MessageUtils.isMultiSimEnabled()) {
                                                    onNetworkUnavailable(serviceId, transactionType, false);
                                                    break;
                                                }
                                            }
                                            if (MLog.isLoggable("Mms_TXN", 2)) {
                                                MLog.v("Mms_TXM_SVC", "onNewIntent: falling through and processing");
                                            }
                                            DownloadManager.getInstance().markState(uri, 129);
                                            break;
                                        }
                                        if (MLog.isLoggable("Mms_TXN", 2)) {
                                            MLog.v("Mms_TXM_SVC", "onNewIntent: skipping - autodownload off");
                                        }
                                        downloadManager.markState(uri, 137);
                                        continue;
                                    } finally {
                                        cursor.close();
                                    }
                                    break;
                            }
                            args = new TransactionBundle(transactionType, uri.toString());
                            if (MLog.isLoggable("Mms_TXN", 2)) {
                                MLog.v("Mms_TXM_SVC", "onNewIntent: launchTransaction uri=" + uri);
                            }
                            launchTransaction(serviceId, args, false);
                        }
                    }
                    cursor.close();
                } else {
                    if (MLog.isLoggable("Mms_TXN", 2)) {
                        MLog.v("Mms_TXM_SVC", "onNewIntent: no pending messages. Stopping service.");
                    }
                    RetryScheduler.setRetryAlarm(this);
                    stopSelfIfIdle(serviceId);
                }
            } else {
                if (3 == intent.getIntExtra(NumberInfo.TYPE_KEY, -1)) {
                    args = new TransactionBundle(intent.getExtras());
                    launchTransaction(serviceId, args, noNetwork);
                    MLog.d("Mms_TXM_SVC", "transaction type:" + args.getTransactionType());
                } else {
                    if (MLog.isLoggable("Mms_TXN", 2)) {
                        MLog.v("Mms_TXM_SVC", "onNewIntent: launch transaction...");
                    }
                    launchTransaction(serviceId, new TransactionBundle(intent.getExtras()), noNetwork);
                }
            }
            return;
        }
        MLog.d("Mms_TXM_SVC", "TransactionService: is not the default sms app");
        stopSelf(serviceId);
    }

    private void stopSelfIfIdle(int startId) {
        boolean hasQueuedService;
        if (this.mServiceHandler.hasMessages(1)) {
            hasQueuedService = true;
        } else {
            hasQueuedService = this.mServiceHandler.hasMessages(5);
        }
        synchronized (this.mProcessing) {
            if (!hasQueuedService) {
                if (this.mProcessing.isEmpty() && this.mPending.isEmpty()) {
                    if (MLog.isLoggable("Mms_TXN", 2)) {
                        MLog.v("Mms_TXM_SVC", "stopSelfIfIdle: STOP!");
                    }
                    stopSelf(startId);
                }
            }
            MLog.v("Mms_TXM_SVC", "stopSelfIfIdle::hasQueuedService=" + hasQueuedService + ", processing size=" + this.mProcessing.size() + ", pending size=" + this.mPending.size());
        }
    }

    private static boolean isTransientFailure(int type) {
        return type > 0 && type < 10;
    }

    private boolean isNetworkAvailable() {
        boolean z = false;
        if (this.mConnMgr == null) {
            return false;
        }
        Context context = getApplicationContext();
        if (MessageUtils.isUsingVoWifi(context)) {
            return true;
        }
        if (HwSpecialUtils.isAlwaysEnableMmsMobileLink(context)) {
            if (!MessageUtils.isAirplanModeOn(context)) {
                z = true;
            }
            return z;
        } else if (!MessageUtils.isDataSwitchOn(context)) {
            return false;
        } else {
            if (!HuaweiTelephonyConfigs.isChinaTelecom()) {
                MLog.i("Mms_TXM_SVC", "The device is not Chinatelecom device.");
                if (MessageUtils.isNetworkRoaming() && !MessageUtils.getRoamingDataEnabled(context, 0)) {
                    return false;
                }
            }
            NetworkInfo ni = this.mConnMgr.getNetworkInfo(2);
            if (ni != null) {
                z = ni.isAvailable();
            }
            return z;
        }
    }

    private boolean isNetworkAvailable(int sub) {
        boolean z = false;
        if (this.mConnMgr == null) {
            return false;
        }
        Context context = getApplicationContext();
        if (HwSpecialUtils.isAlwaysEnableMmsMobileLink(context, sub)) {
            if (!MessageUtils.isAirplanModeOn(context)) {
                z = true;
            }
            return z;
        } else if (!MessageUtils.isDataSwitchOn(context)) {
            return false;
        } else {
            if (!HuaweiTelephonyConfigs.isChinaTelecom()) {
                MLog.i("Mms_TXM_SVC", "The device is not Chinatelecom device.");
                if (MessageUtils.isNetworkRoaming(sub) && !MessageUtils.getRoamingDataEnabled(context, sub)) {
                    MLog.i("Mms_TXM_SVC", "net is roaming, but data roaming is not enabled");
                    return false;
                }
            }
            NetworkInfo ni = this.mConnMgr.getNetworkInfo(2);
            if (ni != null) {
                z = ni.isAvailable();
            }
            return z;
        }
    }

    private int getTransactionType(int msgType) {
        switch (msgType) {
            case 128:
                return 2;
            case 130:
                return 1;
            case 135:
                return 3;
            default:
                MLog.w("Mms_TXM_SVC", "Unrecognized MESSAGE_TYPE: " + msgType);
                return -1;
        }
    }

    private void launchTransaction(int serviceId, TransactionBundle txnBundle, boolean noNetwork) {
        int curSub = -1;
        if (MessageUtils.isMultiSimEnabled()) {
            curSub = Transaction.querySubscription(getApplicationContext(), Uri.parse(txnBundle.getUri()));
            noNetwork = !isNetworkAvailable(curSub);
        }
        if (noNetwork) {
            if (isAirplanModeOn()) {
                onNetworkUnavailable(serviceId, -1, false);
                MLog.w("Mms_TXM_SVC", "launchTransaction() quit as airplan-mode on: " + curSub);
                return;
            } else if (curSub == -1 || curSub == getPreferredDataSubscription()) {
                onNetworkUnavailable(serviceId, txnBundle.getTransactionType(), this.isHWRetry);
                MLog.w("Mms_TXM_SVC", "launchTransaction() no network: " + curSub + " " + this.isHWRetry);
                if (!this.isHWRetry) {
                    return;
                }
            }
        }
        Message msg = this.mServiceHandler.obtainMessage(1);
        msg.arg1 = serviceId;
        msg.arg2 = curSub;
        msg.obj = txnBundle;
        log("launchTransaction: sending message ");
        this.mServiceHandler.sendMessage(msg);
    }

    private void onNetworkUnavailable(int serviceId, int transactionType, boolean ignore) {
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("Mms_TXM_SVC", "onNetworkUnavailable: sid=" + serviceId + ", type=" + transactionType);
        }
        int toastType = -1;
        if (transactionType == 1) {
            toastType = 2;
        } else if (transactionType == 0) {
            try {
                MLog.i("Mms_TXM_SVC", "NOTIFICATION_TRANSACTION notified when no network.");
                MessagingNotification.blockingUpdateNewMessageIndicator((Context) this, -1, false);
            } catch (Exception e) {
                MLog.e("Mms_TXM_SVC", "TransactionService blockingUpdate or  updateDownload Exception:", (Throwable) e);
            }
        }
        if (this.mHwCustHwMessageUtils != null) {
            toastType = this.mHwCustHwMessageUtils.getToastType(toastType, transactionType);
        }
        if (toastType != -1) {
            this.mToastHandler.sendEmptyMessage(toastType);
        }
        if (!ignore) {
            stopSelf(serviceId);
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("Mms_TXM_SVC", "Destroying TransactionService");
        }
        synchronized (this.mProcessing) {
            boolean isEmpty = this.mPending.isEmpty();
        }
        if (!isEmpty) {
            MLog.w("Mms_TXM_SVC", "TransactionService exiting with transaction still pending");
        }
        releaseWakeLock();
        unregisterReceiver(this.mReceiver);
        this.mPowerOffReceiver.stopTimer();
        unregisterReceiver(this.mPowerOffReceiver);
        this.mServiceHandler.sendEmptyMessage(100);
    }

    private boolean isMessagePlusServiceRunning() {
        List<RunningServiceInfo> runningServices = ((ActivityManager) getSystemService("activity")).getRunningServices(Integer.MAX_VALUE);
        if (runningServices != null) {
            for (RunningServiceInfo service : runningServices) {
                if ("com.huawei.rcs.incallui.service.MessagePlusService".equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setMmsStateCallBack(TransactionState state) {
        if (isMessagePlusServiceRunning()) {
            int size = MessagePlusService.getMmsUriListSize();
            if (size > 0) {
                String sendUri = state.getContentUri().toString();
                boolean flag = false;
                for (int i = 0; i < size; i++) {
                    String uri = MessagePlusService.getUriFromMmsUriList(i);
                    if (sendUri.contains("inbox")) {
                        sendUri = sendUri.replace("inbox", "sent");
                    }
                    if (uri != null && uri.equals(sendUri)) {
                        flag = true;
                        MessagePlusService.removeFromMmsUriList(i);
                        break;
                    }
                }
                if (flag) {
                    MessagePlusService.notifyMmsStateUri(1);
                } else {
                    MessagePlusService.notifyMmsStateUri(2);
                }
            }
        }
    }

    public void update(Observable observable) {
        Transaction transaction = (Transaction) observable;
        int serviceId = transaction.getServiceId();
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("Mms_TXM_SVC", "update transaction " + serviceId);
        }
        if (this.mHwCustTransactionService != null) {
            this.mHwCustTransactionService.update();
        }
        Cursor cursor;
        try {
            Transaction t = (Transaction) observable;
            TransactionState state = t.getState();
            if (MLog.isLoggable("Mms_TXN", 2) || LogTag.SHOW_MMS_LOG) {
                MLog.v("Mms_TXM_SVC", "MSG_APP_update TransactionState " + state);
            }
            if (this.isHWRetry) {
                Uri uri = state.getContentUri();
                if (uri != null) {
                    long msgId = ContentUris.parseId(uri);
                    Builder uriBuilder = PendingMessages.CONTENT_URI.buildUpon();
                    uriBuilder.appendQueryParameter("protocol", "mms");
                    uriBuilder.appendQueryParameter("message", String.valueOf(msgId));
                    cursor = SqliteWrapper.query(this, getContentResolver(), uriBuilder.build(), null, null, null, null);
                    int retryIndex;
                    if (MessageListAdapter.getUserStopTransaction(uri.toString())) {
                        if (cursor != null && cursor.getCount() == 1 && cursor.moveToFirst()) {
                            retryIndex = cursor.getInt(cursor.getColumnIndexOrThrow("retry_index"));
                            ContentValues values = new ContentValues(2);
                            values.put("err_type", Integer.valueOf(0));
                            values.put("retry_index", Integer.valueOf(0));
                            MLog.d("Mms_TXM_SVC", "user stop transaction update " + uri + " retry index to: " + (retryIndex - 1));
                            SqliteWrapper.update(this, getContentResolver(), PendingMessages.CONTENT_URI, values, "msg_id = " + msgId, null);
                        }
                    } else if (state.getState() == 2 && !this.transaction_failed_notified) {
                        this.transaction_failed_notified = true;
                        if (cursor != null) {
                            try {
                                if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                                    retryIndex = cursor.getInt(cursor.getColumnIndexOrThrow("retry_index"));
                                    DefaultRetryScheme defaultRetryScheme = new DefaultRetryScheme(this, retryIndex);
                                    boolean retry = true;
                                    if (getResponseStatus(msgId) == 132) {
                                        retry = false;
                                    }
                                    MLog.v("Mms_TXM_SVC", "retry index " + retryIndex);
                                    if (retryIndex < defaultRetryScheme.getRetryLimit() && retry && (t instanceof RetrieveTransaction) && MessageListAdapter.getManualDownloadFromMap(uri.toString()) && ((RetrieveTransaction) t).getDownloadButtonClickCount() > 0) {
                                        this.mToastHandler.sendEmptyMessage(5);
                                    }
                                }
                            } catch (Throwable e) {
                                MLog.e("Mms_TXM_SVC", "TransactionService update has unknow exception.", e);
                                if (cursor != null) {
                                    cursor.close();
                                }
                            } catch (Throwable th) {
                                if (cursor != null) {
                                    cursor.close();
                                }
                            }
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            synchronized (this.mProcessing) {
                this.mProcessing.remove(transaction);
                if (this.mPending.size() > 0) {
                    if (MLog.isLoggable("Mms_TXN", 2)) {
                        MLog.v("Mms_TXM_SVC", "update: handle next pending transaction...");
                    }
                    this.mServiceHandler.sendMessage(this.mServiceHandler.obtainMessage(4, transaction.getConnectionSettings()));
                } else if (this.mProcessing.isEmpty()) {
                    if (MLog.isLoggable("Mms_TXN", 2)) {
                        MLog.v("Mms_TXM_SVC", "update: endMmsConnectivity");
                    }
                    endMmsConnectivity();
                } else if (MLog.isLoggable("Mms_TXN", 2)) {
                    MLog.v("Mms_TXM_SVC", "update: mProcessing is not empty");
                }
            }
            Intent intent = new Intent("android.intent.action.TRANSACTION_COMPLETED_ACTION");
            int result = state.getState();
            intent.putExtra(ParseItemManager.STATE, result);
            setMmsStateCallBack(state);
            switch (result) {
                case 1:
                    if (MLog.isLoggable("Mms_TXN", 2)) {
                        MLog.v("Mms_TXM_SVC", "Transaction complete: " + serviceId);
                    }
                    intent.putExtra("uri", state.getContentUri());
                    CspFragment.setNotificationCleared(false);
                    switch (transaction.getType()) {
                        case 0:
                        case 1:
                            long threadId = MessagingNotification.getThreadId(this, state.getContentUri());
                            if (mHwCustEcidLookup == null || !mHwCustEcidLookup.delayedNotification(getApplicationContext(), threadId, false, state.getContentUri())) {
                                MessagingNotification.blockingUpdateNewMessageIndicator((Context) this, threadId, state.getContentUri());
                            }
                            MessagingNotification.updateDownloadFailedNotification(this);
                            break;
                        case 2:
                            StatisticalHelper.incrementReportCount(this, AMapException.CODE_AMAP_ENGINE_TABLEID_NOT_EXIST);
                            RateController.getInstance().update();
                            break;
                        default:
                            break;
                    }
                case 2:
                    if (MLog.isLoggable("Mms_TXN", 2)) {
                        MLog.v("Mms_TXM_SVC", "Transaction failed: " + serviceId);
                        break;
                    }
                    break;
                default:
                    MLog.v("Mms_TXN", "Mms_TXM_SVC", "Transaction state unknown: %d result %d", Integer.valueOf(serviceId), Integer.valueOf(result));
                    break;
            }
            MLog.v("Mms_TXN", "Mms_TXM_SVC", "update: broadcast transaction result %d", Integer.valueOf(result));
            sendBroadcast(intent);
            transaction.detach(this);
            stopTransactionService(Integer.valueOf(serviceId));
        } catch (Throwable th2) {
            transaction.detach(this);
            stopTransactionService(Integer.valueOf(serviceId));
        }
    }

    private synchronized void createWakeLock() {
        if (this.mWakeLock == null) {
            this.mWakeLock = ((PowerManager) getSystemService("power")).newWakeLock(1, "MMS Connectivity");
            this.mWakeLock.setReferenceCounted(false);
        }
    }

    private void acquireWakeLock() {
        this.mWakeLock.acquire();
    }

    private void releaseWakeLock() {
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    protected int beginMmsConnectivity() throws IOException {
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("Mms_TXM_SVC", "beginMmsConnectivity");
        }
        if (MessageUtils.isMultiSimEnabled()) {
            return beginMmsConnectivity(-1);
        }
        return beginMmsConnectivity(0);
    }

    protected int beginMmsConnectivity(int subscription) throws IOException {
        createWakeLock();
        if (this.mHwCustTransactionService == null || !this.mHwCustTransactionService.mmsUseWifi()) {
            String feature;
            int result;
            if (!(this.mSubInUse == -1 || this.mSubInUse == subscription)) {
                MLog.e("Mms_TXM_SVC", "beginMmsConnectivity to another sub: " + subscription + " already use sub: " + this.mSubInUse);
            }
            if (MessageUtils.isMultiSimEnabled()) {
                MLog.e("Mms_TXM_SVC", "beginMmsConnectivity DSDS, subscription = " + subscription);
                if (subscription == 0) {
                    feature = MessageUtils.getFeatureEnableMms(0);
                    this.mSubInUse = 0;
                } else if (1 == subscription) {
                    feature = MessageUtils.getFeatureEnableMms(1);
                    this.mSubInUse = 1;
                } else {
                    feature = "enableMMS";
                }
                result = MessageUtils.startUsingNetworkFeature(this.mConnMgr, 0, feature, subscription);
            } else {
                feature = "enableMMS";
                result = this.mConnMgr.startUsingNetworkFeature(0, feature);
            }
            MLog.i("Mms_TXM_SVC", "beginMmsConnectivity: result=" + result + " feature: " + feature);
            switch (result) {
                case 0:
                case 1:
                    acquireWakeLock();
                    return result;
                default:
                    this.mSubInUse = -1;
                    ErrorMonitor.reportRadar(907000021, "startUsingNetworkFeature fail. result: " + result);
                    throw new IOException("Cannot establish MMS connectivity");
            }
        }
        MLog.v("Mms_TXM_SVC", "mHwCustTransactionService.getWifiState");
        return this.mHwCustTransactionService.getWifiState();
    }

    protected void endMmsConnectivity() {
        try {
            MLog.i("Mms_TXM_SVC", "endMmsConnectivity for sub =" + this.mSubInUse);
            this.mServiceHandler.removeMessages(3);
            if (this.mHwCustTransactionService != null && this.mHwCustTransactionService.mmsUseWifi()) {
                this.mHwCustTransactionService.setWifiDisconnect();
            } else if (this.mConnMgr != null) {
                if (MessageUtils.isMultiSimEnabled()) {
                    MessageUtils.stopUsingNetworkFeature(this.mConnMgr, 0, MessageUtils.getFeatureEnableMms(this.mSubInUse), this.mSubInUse);
                } else {
                    this.mConnMgr.stopUsingNetworkFeature(0, "enableMMS");
                }
            }
            this.mSubInUse = -1;
        } finally {
            releaseWakeLock();
        }
    }

    private int getResponseStatus(long msgID) {
        int respStatus = 0;
        Cursor cursor = SqliteWrapper.query(this, getContentResolver(), Outbox.CONTENT_URI, null, "_id=" + msgID, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    respStatus = cursor.getInt(cursor.getColumnIndexOrThrow("resp_st"));
                }
            } catch (Exception e) {
                ErrorMonitor.reportErrorInfo(8, "TransactionService getResponseStatus unexception", e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        if (respStatus != 0) {
            MLog.e("Mms_TXM_SVC", "Response status is: " + respStatus);
        }
        return respStatus;
    }

    private void renewMmsConnectivity() {
        sendMessageDelayed(3, 30000);
    }

    private void reProcessPendingTransaction(Transaction transaction) {
        sendMessageDelayed(4, transaction.getConnectionSettings(), 30000);
    }

    private void stopTransactionService(Integer serviceId) {
        this.mServiceHandler.sendMessage(this.mServiceHandler.obtainMessage(101, serviceId.intValue(), 0));
    }

    private void sendMessageDelayed(int event, long delay) {
        sendMessageDelayed(event, null, delay);
    }

    private void removeMessages(int event) {
        this.mServiceHandler.removeMessages(event);
    }

    private void sendMessageDelayed(int event, Object obj, long delay) {
        this.mServiceHandler.sendMessageDelayed(this.mServiceHandler.obtainMessage(event, obj), delay);
    }

    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("PendingTransaction:\n");
        synchronized (this.mProcessing) {
            for (Transaction t : this.mPending) {
                sb.append(t).append("\n");
            }
            sb.append("ProcessingTransaction:\n");
            for (Transaction t2 : this.mProcessing) {
                sb.append(t2).append("\n");
            }
            int curSub = MessageUtils.getMmsAutoSetDataSubscription();
            int curpreSub = getPreferredDataSubscription();
            writer.println("Service Start already: " + ((System.currentTimeMillis() - this.startTime) / 1000) + " s");
            writer.println("MmsAutoSetDataSubscription:" + curSub + " PreferredDataSubscription:" + curpreSub);
            writer.println("isNetworkAvailable:" + isNetworkAvailable());
            writer.println("mPending Size:" + this.mPending.size());
            writer.println("mProcessing Size:" + this.mProcessing.size());
            writer.println(sb);
        }
    }

    public static void startMe(Context context) {
        if (OsUtil.isAtLeastL() && OsUtil.isSecondaryUser()) {
            MLog.w("Mms_TXM_SVC", "Start transaction in secondary.");
            MmsPermReceiver.broadcastForSendMms(context);
            return;
        }
        startService(context, new Intent(context, TransactionService.class));
    }

    public static void startMe(Context context, Uri uri, int type) {
        startMe(context, uri.toString(), type);
    }

    public static void startMe(Context context, String action) {
        if (OsUtil.isAtLeastL() && OsUtil.isSecondaryUser()) {
            MLog.w("Mms_TXM_SVC", "Start transaction in secondary, switch to OWNER.");
            MmsPermReceiver.broadcastForSendMms(context, action);
            return;
        }
        startService(context, new Intent(action, null, context, TransactionService.class));
    }

    public static void startMe(Context context, String uri, int type) {
        if (OsUtil.isAtLeastL() && OsUtil.isSecondaryUser()) {
            MLog.w("Mms_TXM_SVC", "Start transaction in secondary, switch to OWNER. Type " + type);
            MmsPermReceiver.broadcastForSendMms(context, uri, type);
        } else if (TextUtils.isEmpty(uri)) {
            MLog.e("Mms_TXM_SVC", "startMe with empty uri");
        } else {
            Uri msgUri = Uri.parse(uri);
            DownloadManager.getInstance().isNeedUpdateTime = true;
            DownloadManager.getInstance().markState(msgUri, 128);
            DownloadManager.getInstance().isNeedUpdateTime = false;
            Intent svc = new Intent(context, TransactionService.class);
            svc.putExtra("uri", uri);
            svc.putExtra(NumberInfo.TYPE_KEY, type);
            startService(context, svc);
        }
    }

    static void startMeDelayed(final Context context, long delay) {
        MLog.e("Mms_TXM_SVC", "isSecondaryUser() start ", Long.valueOf(System.currentTimeMillis()));
        boolean isSecondaryUser = OsUtil.isSecondaryUser();
        MLog.e("Mms_TXM_SVC", "isSecondaryUser() end ", Long.valueOf(System.currentTimeMillis()));
        if (OsUtil.isAtLeastL() && isSecondaryUser) {
            MLog.e("Mms_TXM_SVC", "Unsupport startMeDelayed in SecondaryUser", new Exception());
            return;
        }
        MLog.e("Mms_TXM_SVC", "removeCallbacksAndMessages start ", Long.valueOf(System.currentTimeMillis()));
        HwBackgroundLoader.getBackgroundHandler().removeCallbacksAndMessages(Integer.valueOf(10001));
        MLog.e("Mms_TXM_SVC", "removeCallbacksAndMessages end ", Long.valueOf(System.currentTimeMillis()));
        HwBackgroundLoader.getBackgroundHandler().postAtTime(new Runnable() {
            public void run() {
                TransactionService.startService(context, new Intent(context, TransactionService.class));
            }
        }, Integer.valueOf(10001), SystemClock.uptimeMillis() + delay);
        MLog.e("Mms_TXM_SVC", "startService end ", Long.valueOf(System.currentTimeMillis()));
    }

    private static void startService(final Context context, final Intent intent) {
        BugleActivityUtil.checkPermissionIfNeeded(context, new Runnable() {
            public void run() {
                context.startService(intent);
            }
        });
    }

    static void retryStart(final Context context, final long retryAt) {
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("Mms_TXM_SVC", "TransactionService retryStart at: " + retryAt + " now: " + System.currentTimeMillis());
        }
        if (OsUtil.isAtLeastL() && OsUtil.isSecondaryUser()) {
            MLog.e("Mms_TXM_SVC", "Unsupport  retryStart in SecondaryUser", new Exception());
        } else {
            BugleActivityUtil.checkPermissionIfNeeded(context, new Runnable() {
                public void run() {
                    ((AlarmManager) context.getSystemService("alarm")).set(1, retryAt, PendingIntent.getService(context, 0, new Intent("android.intent.action.ACTION_ONALARM", null, context, TransactionService.class), 1073741824));
                }
            });
        }
    }

    private int getPreferredDataSubscription() {
        return MmsCommon.PLATFORM_MTK ? this.mSubInUse : MessageUtils.getPreferredDataSubscription();
    }

    private boolean isAirplanModeOn() {
        return MessageUtils.isAirplanModeOn(this);
    }

    private void log(String msg) {
        if (MLog.isLoggable("Mms_TXN", 2) || LogTag.SHOW_MMS_LOG) {
            MLog.v("Mms_TXM_SVC", "MSG_APP_" + msg);
        }
    }
}
