package com.android.mms.transaction;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Inbox;
import android.provider.Telephony.Sms.Intents;
import android.provider.Telephony.Sms.Outbox;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.ServiceState;
import android.telephony.SmsMessage;
import android.telephony.SmsMessage.MessageClass;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.HarassNumberUtil;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.db.TrainManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.amap.api.services.core.AMapException;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.RecipientIdCache;
import com.android.mms.ui.ClassZeroActivity;
import com.android.mms.ui.FdnIndicateDialogActivity;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.PreferenceUtils;
import com.android.mms.util.HwCustEcidLookup;
import com.android.mms.util.Recycler;
import com.android.mms.widget.MmsWidgetProvider;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.transaction.RcsSmsReceiverService;
import com.google.android.gms.R;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ErrorMonitor;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.CspFragment;
import com.huawei.mms.util.DelaySendManager;
import com.huawei.mms.util.FloatMmsRequsetReceiver;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwCustUpdateUserBehavior;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.MmsRadarInfoManager;
import com.huawei.mms.util.SimCursorManager;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.mms.util.WindowManagerEx;
import com.huawei.rcs.incallui.service.MessagePlusService;
import com.huawei.rcs.utils.RcsTransaction;
import java.util.ArrayList;
import java.util.List;

public class SmsReceiverService extends Service {
    private static final String[] REPLACE_PROJECTION = new String[]{"_id", "address", "protocol"};
    private static final String[] SEND_PROJECTION = new String[]{"_id", "thread_id", "address", "body", "status", "sub_id", "group_id"};
    private static HwCustEcidLookup mHwCustEcidLookup = ((HwCustEcidLookup) HwCustUtils.createObj(HwCustEcidLookup.class, new Object[0]));
    private static Boolean[] mIsInService = new Boolean[]{Boolean.valueOf(true), Boolean.valueOf(true)};
    private long endTime = -1;
    private int mActiveId = 255;
    private CryptoSmsReceiverService mCryptoSmsReceiverService = new CryptoSmsReceiverService();
    private HwCustMessagingNotification mHwCustMessagingNotification = null;
    private HwCustSmsReceiverService mHwCustSmsReceiverService = null;
    private HwCustUpdateUserBehavior mHwCustUpdateUserBehavior = null;
    private int mIsSecret;
    MmsRadarInfoManager mMmsRadarInfoManager = null;
    private RcsSmsReceiverService mRcsSmsReceiverService = null;
    private int mResultCode;
    private int mRetryTimes = 0;
    Handler mSendSmsHandler = null;
    private boolean mSending;
    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    public Handler mToastHandler = new Handler();
    private long startTime = -1;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int serviceId = msg.arg1;
            int resultCode = msg.arg2;
            Intent intent = msg.obj;
            if (MLog.isLoggable("Mms_TXN", 2)) {
                MLog.v("Mms_TXS_SVC", "handleMessage serviceId: " + serviceId);
            }
            if (intent != null && MmsConfig.isSmsEnabled(SmsReceiverService.this.getApplicationContext())) {
                String action = intent.getAction();
                int error = intent.getIntExtra("errorCode", 0);
                if (MLog.isLoggable("Mms_TXN", 2)) {
                    MLog.v("Mms_TXS_SVC", "handleMessage action: " + action + " error: " + error);
                }
                if ("com.android.mms.transaction.MESSAGE_SENT".equals(intent.getAction())) {
                    SmsReceiverService.this.handleSmsSent(intent, error, resultCode);
                } else if ("android.provider.Telephony.SMS_DELIVER".equals(action)) {
                    SmsReceiverService.this.handleSmsReceived(intent, error);
                } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    SmsReceiverService.this.handleBootCompleted();
                } else if ("android.intent.action.SERVICE_STATE".equals(action) || "android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(action)) {
                    SmsReceiverService.this.handleServiceStateChanged(intent);
                } else if (action != null && "com.android.mms.transaction.SEND_MESSAGE".endsWith(action)) {
                    SmsReceiverService.this.handleSendMessage();
                } else if ("com.android.mms.transaction.SEND_INACTIVE_MESSAGE".equals(action)) {
                    SmsReceiverService.this.handleSendInactiveMessage();
                } else if ("com.android.mms.VERIFITION_SMS_ACTION".equals(action)) {
                    SmsReceiverService.this.handleSmsReceived(intent, error);
                } else if (SmsReceiverService.this.mRcsSmsReceiverService != null) {
                    SmsReceiverService.this.mRcsSmsReceiverService.handleMessageReceived(action, intent, SmsReceiverService.this.mToastHandler);
                }
            }
            SmsReceiver.finishStartingService(SmsReceiverService.this, serviceId);
        }
    }

    private void handleSmsSent(android.content.Intent r28, int r29, int r30) {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r27 = this;
        r26 = r28.getData();
        r0 = r27;
        r1 = r26;
        r2 = r30;
        r0.feedbackSmsState(r1, r2);
        r4 = 0;
        r0 = r27;
        r0.mSending = r4;
        r4 = "SendNextMsg";
        r6 = 0;
        r0 = r28;
        r22 = r0.getBooleanExtra(r4, r6);
        r0 = r27;
        r4 = r0.mSendSmsHandler;
        r6 = 103; // 0x67 float:1.44E-43 double:5.1E-322;
        r21 = r4.obtainMessage(r6);
        r0 = r27;
        r4 = r0.mSendSmsHandler;
        r0 = r21;
        r4.sendMessage(r0);
        r4 = r27.getApplicationContext();
        r0 = r26;
        r7 = com.huawei.mms.util.HwMessageUtils.querySubscription(r4, r0);
        r5 = 0;
        r4 = -1;
        r0 = r30;
        if (r0 != r4) goto L_0x0040;
    L_0x003f:
        r5 = 1;
    L_0x0040:
        r0 = r27;
        r4 = r0.mMmsRadarInfoManager;
        r9 = "sms send fail";
        r6 = 1311; // 0x51f float:1.837E-42 double:6.477E-321;
        r8 = r29;
        r4.reportReceiveOrSendResult(r5, r6, r7, r8, r9);
        r4 = -1;
        r0 = r30;
        if (r0 != r4) goto L_0x010b;
    L_0x0053:
        r0 = r27;
        r4 = r0.mMmsRadarInfoManager;
        r4.syncSmscFromCard(r7);
        r4 = "Mms_TXS_SVC";
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r8 = "handleSmsSent move message to sent folder uri: ";
        r6 = r6.append(r8);
        r0 = r26;
        r6 = r6.append(r0);
        r6 = r6.toString();
        com.huawei.cspcommon.MLog.w(r4, r6);
        r4 = 2001; // 0x7d1 float:2.804E-42 double:9.886E-321;
        r0 = r27;
        com.huawei.mms.util.StatisticalHelper.incrementReportCount(r0, r4);
        r0 = r27;
        r4 = r0.mHwCustUpdateUserBehavior;
        if (r4 == 0) goto L_0x00c1;
    L_0x0083:
        r8 = java.lang.System.currentTimeMillis();
        r0 = r27;
        r0.endTime = r8;
        r0 = r27;
        r4 = r0.mHwCustUpdateUserBehavior;
        r6 = r27.getApplicationContext();
        r8 = "send_sms_duration";
        r9 = "duration";
        r8 = r4.getTime(r6, r8, r9);
        r0 = r27;
        r0.startTime = r8;
        r0 = r27;
        r9 = r0.mHwCustUpdateUserBehavior;
        r10 = r27.getApplicationContext();
        r0 = r27;
        r12 = r0.startTime;
        r0 = r27;
        r14 = r0.endTime;
        r11 = 0;
        r9.upLoadSendMesSucc(r10, r11, r12, r14);
        r8 = -1;
        r0 = r27;
        r0.startTime = r8;
        r8 = -1;
        r0 = r27;
        r0.endTime = r8;
    L_0x00c1:
        r4 = 2;
        r0 = r27;
        r1 = r26;
        r2 = r29;
        r4 = android.provider.Telephony.Sms.moveMessageToFolder(r0, r1, r4, r2);
        if (r4 != 0) goto L_0x00f1;
    L_0x00ce:
        r4 = "Mms_TXS_SVC";
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r8 = "handleSmsSent: failed to move message ";
        r6 = r6.append(r8);
        r0 = r26;
        r6 = r6.append(r0);
        r8 = " to sent folder";
        r6 = r6.append(r8);
        r6 = r6.toString();
        com.huawei.cspcommon.MLog.e(r4, r6);
    L_0x00f1:
        if (r22 == 0) goto L_0x0107;
    L_0x00f3:
        r0 = r27;
        r4 = r0.mHwCustUpdateUserBehavior;
        if (r4 == 0) goto L_0x0104;
    L_0x00f9:
        r0 = r27;
        r4 = r0.mHwCustUpdateUserBehavior;
        r6 = r27.getApplicationContext();
        r4.playSentSuccessTone(r6);
    L_0x0104:
        r27.sendFirstQueuedMessage();
    L_0x0107:
        com.android.mms.transaction.MessagingNotification.nonBlockingUpdateSendFailedNotification(r27);
    L_0x010a:
        return;
    L_0x010b:
        r4 = 2;
        r0 = r30;
        if (r0 == r4) goto L_0x0115;
    L_0x0110:
        r4 = 4;
        r0 = r30;
        if (r0 != r4) goto L_0x0276;
    L_0x0115:
        r4 = "Mms_TXS_SVC";
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r8 = "handleSmsSent: no service, queuing message w/ uri: ";
        r6 = r6.append(r8);
        r0 = r26;
        r6 = r6.append(r0);
        r6 = r6.toString();
        com.huawei.cspcommon.MLog.w(r4, r6);
        r0 = r27;
        r4 = r0.mHwCustUpdateUserBehavior;
        if (r4 == 0) goto L_0x0178;
    L_0x0137:
        r8 = java.lang.System.currentTimeMillis();
        r0 = r27;
        r0.endTime = r8;
        r0 = r27;
        r4 = r0.mHwCustUpdateUserBehavior;
        r6 = r27.getApplicationContext();
        r8 = "send_sms_duration";
        r9 = "duration";
        r8 = r4.getTime(r6, r8, r9);
        r0 = r27;
        r0.startTime = r8;
        r0 = r27;
        r8 = r0.mHwCustUpdateUserBehavior;
        r9 = r27.getApplicationContext();
        r11 = "handleSmsSent: no service";
        r0 = r27;
        r12 = r0.startTime;
        r0 = r27;
        r14 = r0.endTime;
        r10 = 0;
        r8.upLoadSendMesFail(r9, r10, r11, r12, r14);
        r8 = -1;
        r0 = r27;
        r0.startTime = r8;
        r8 = -1;
        r0 = r27;
        r0.endTime = r8;
    L_0x0178:
        r20 = new android.content.Intent;
        r20.<init>();
        r4 = "huawei.selfrepair.message";
        r0 = r20;
        r0.setAction(r4);
        r0 = r27;
        r1 = r20;
        r0.sendBroadcast(r1);
        r4 = mIsInService;
        r6 = 0;
        r4 = r4[r6];
        r4 = r4.booleanValue();
        if (r4 == 0) goto L_0x01a2;
    L_0x0197:
        r4 = mIsInService;
        r6 = 1;
        r4 = r4[r6];
        r4 = r4.booleanValue();
        if (r4 != 0) goto L_0x021d;
    L_0x01a2:
        r4 = com.android.mms.ui.MessageUtils.isMultiSimEnabled();
        if (r4 == 0) goto L_0x021d;
    L_0x01a8:
        r4 = 6;
        r0 = r27;
        r1 = r26;
        r2 = r29;
        android.provider.Telephony.Sms.moveMessageToFolder(r0, r1, r4, r2);
        r0 = r27;
        r4 = r0.mToastHandler;
        r6 = new com.android.mms.transaction.SmsReceiverService$1;
        r0 = r27;
        r6.<init>();
        r4.post(r6);
        r4 = com.android.mms.ui.MessageUtils.isMultiSimEnabled();
        if (r4 == 0) goto L_0x010a;
    L_0x01c6:
        r16 = 0;
        r9 = r27.getContentResolver();	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r11 = SEND_PROJECTION;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r14 = "date ASC";	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r12 = 0;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r13 = 0;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r8 = r27;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r10 = r26;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r16 = com.huawei.cspcommon.ex.SqliteWrapper.query(r8, r9, r10, r11, r12, r13, r14);	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        if (r16 == 0) goto L_0x0216;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
    L_0x01dd:
        r4 = r16.moveToFirst();	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        if (r4 == 0) goto L_0x0216;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
    L_0x01e3:
        r4 = 5;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r0 = r16;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r25 = r0.getInt(r4);	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r4 = "Mms_TXS_SVC";	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r6 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r6.<init>();	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r8 = "No service subid = ";	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r6 = r6.append(r8);	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r0 = r25;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r6 = r6.append(r0);	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r6 = r6.toString();	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        com.huawei.cspcommon.MLog.d(r4, r6);	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        if (r25 == 0) goto L_0x020d;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
    L_0x0208:
        r4 = 1;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r0 = r25;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        if (r0 != r4) goto L_0x0221;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
    L_0x020d:
        r4 = mIsInService;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r6 = 0;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r6 = java.lang.Boolean.valueOf(r6);	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r4[r25] = r6;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
    L_0x0216:
        if (r16 == 0) goto L_0x010a;
    L_0x0218:
        r16.close();
        goto L_0x010a;
    L_0x021d:
        r27.registerForServiceStateChanges();
        goto L_0x01a8;
    L_0x0221:
        r4 = "Mms_TXS_SVC";	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r6 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r6.<init>();	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r8 = "No serviced prefered subid ,set prefered SmsSubscription ";	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r6 = r6.append(r8);	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r8 = com.android.mms.ui.MessageUtils.getPreferredSmsSubscription();	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r6 = r6.append(r8);	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r6 = r6.toString();	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        com.huawei.cspcommon.MLog.d(r4, r6);	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r4 = mIsInService;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r6 = com.android.mms.ui.MessageUtils.getPreferredSmsSubscription();	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r8 = 0;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r8 = java.lang.Boolean.valueOf(r8);	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r4[r6] = r8;	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        goto L_0x0216;
    L_0x024d:
        r19 = move-exception;
        r4 = "Mms_TXS_SVC";	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r6 = "handleSmsSent: RuntimeException";	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        com.huawei.cspcommon.MLog.e(r4, r6);	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        if (r16 == 0) goto L_0x010a;
    L_0x0259:
        r16.close();
        goto L_0x010a;
    L_0x025e:
        r18 = move-exception;
        r4 = "Mms_TXS_SVC";	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        r6 = "handleSmsSent: Exception";	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        com.huawei.cspcommon.MLog.e(r4, r6);	 Catch:{ RuntimeException -> 0x024d, Exception -> 0x025e, all -> 0x026f }
        if (r16 == 0) goto L_0x010a;
    L_0x026a:
        r16.close();
        goto L_0x010a;
    L_0x026f:
        r4 = move-exception;
        if (r16 == 0) goto L_0x0275;
    L_0x0272:
        r16.close();
    L_0x0275:
        throw r4;
    L_0x0276:
        r4 = 6;
        r0 = r30;
        if (r0 != r4) goto L_0x030c;
    L_0x027b:
        r17 = r27.getApplicationContext();
        r0 = r17;
        r1 = r26;
        r24 = com.huawei.mms.util.HwMessageUtils.querySubscription(r0, r1);
        r0 = r27;
        r1 = r26;
        r2 = r30;
        r0.messageFailedToSend(r1, r2);
        r0 = r27;
        r1 = r24;
        r23 = r0.getSmsCenterNumber(r1);
        r0 = r27;
        r1 = r23;
        r2 = r24;
        r4 = r0.isNumberInFdnList(r1, r2);
        if (r4 != 0) goto L_0x02fd;
    L_0x02a4:
        r0 = r27;
        r1 = r17;
        r2 = r23;
        r3 = r24;
        r0.displayFdnIndicateDialog(r1, r2, r3);
    L_0x02af:
        r0 = r27;
        r4 = r0.mHwCustUpdateUserBehavior;
        if (r4 == 0) goto L_0x02f6;
    L_0x02b5:
        r8 = java.lang.System.currentTimeMillis();
        r0 = r27;
        r0.endTime = r8;
        r0 = r27;
        r4 = r0.mHwCustUpdateUserBehavior;
        r6 = r27.getApplicationContext();
        r8 = "send_sms_duration";
        r9 = "duration";
        r8 = r4.getTime(r6, r8, r9);
        r0 = r27;
        r0.startTime = r8;
        r0 = r27;
        r8 = r0.mHwCustUpdateUserBehavior;
        r9 = r27.getApplicationContext();
        r11 = "fdn check failure";
        r0 = r27;
        r12 = r0.startTime;
        r0 = r27;
        r14 = r0.endTime;
        r10 = 0;
        r8.upLoadSendMesFail(r9, r10, r11, r12, r14);
        r8 = -1;
        r0 = r27;
        r0.startTime = r8;
        r8 = -1;
        r0 = r27;
        r0.endTime = r8;
    L_0x02f6:
        if (r22 == 0) goto L_0x010a;
    L_0x02f8:
        r27.sendFirstQueuedMessage();
        goto L_0x010a;
    L_0x02fd:
        r0 = r27;
        r4 = r0.mToastHandler;
        r6 = new com.android.mms.transaction.SmsReceiverService$2;
        r0 = r27;
        r6.<init>();
        r4.post(r6);
        goto L_0x02af;
    L_0x030c:
        r0 = r27;
        r1 = r26;
        r2 = r29;
        r0.messageFailedToSend(r1, r2);
        r0 = r27;
        r4 = r0.mHwCustUpdateUserBehavior;
        if (r4 == 0) goto L_0x036f;
    L_0x031b:
        r8 = java.lang.System.currentTimeMillis();
        r0 = r27;
        r0.endTime = r8;
        r0 = r27;
        r4 = r0.mHwCustUpdateUserBehavior;
        r6 = r27.getApplicationContext();
        r8 = "send_sms_duration";
        r9 = "duration";
        r8 = r4.getTime(r6, r8, r9);
        r0 = r27;
        r0.startTime = r8;
        r0 = r27;
        r8 = r0.mHwCustUpdateUserBehavior;
        r9 = r27.getApplicationContext();
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r6 = "error code = ";
        r4 = r4.append(r6);
        r0 = r29;
        r4 = r4.append(r0);
        r11 = r4.toString();
        r0 = r27;
        r12 = r0.startTime;
        r0 = r27;
        r14 = r0.endTime;
        r10 = 0;
        r8.upLoadSendMesFail(r9, r10, r11, r12, r14);
        r8 = -1;
        r0 = r27;
        r0.startTime = r8;
        r8 = -1;
        r0 = r27;
        r0.endTime = r8;
    L_0x036f:
        if (r22 == 0) goto L_0x010a;
    L_0x0371:
        r27.sendFirstQueuedMessage();
        goto L_0x010a;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.transaction.SmsReceiverService.handleSmsSent(android.content.Intent, int, int):void");
    }

    public boolean isNumberInFdnList(java.lang.String r12, int r13) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x005f in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r11 = this;
        r10 = 1;
        r9 = 0;
        r1 = "content://icc/fdn/exits_query/subId/";
        r8 = android.net.Uri.parse(r1);
        r1 = r11.getApplicationContext();
        r0 = r1.getContentResolver();
        if (r12 == 0) goto L_0x001d;
    L_0x0013:
        r1 = " ";
        r2 = "";
        r12 = r12.replaceAll(r1, r2);
    L_0x001d:
        r6 = 0;
        r2 = (long) r13;
        r1 = android.content.ContentUris.withAppendedId(r8, r2);	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        r2 = 1;	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        r4 = new java.lang.String[r2];	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        r2 = 0;	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        r4[r2] = r12;	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        r2 = 0;	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        r3 = 0;	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        r5 = 0;	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        if (r6 == 0) goto L_0x0041;	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
    L_0x0032:
        r1 = "Mms_TXS_SVC";	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        r2 = "[mms_fdn]number exists in fdn list, number is xxx";	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        com.huawei.cspcommon.MLog.d(r1, r2);	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        if (r6 == 0) goto L_0x0040;
    L_0x003d:
        r6.close();
    L_0x0040:
        return r10;
    L_0x0041:
        r1 = "Mms_TXS_SVC";	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        r2 = "[mms_fdn]number doesn't exists in fdn list, number is xxx";	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        com.huawei.cspcommon.MLog.d(r1, r2);	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        if (r6 == 0) goto L_0x004f;
    L_0x004c:
        r6.close();
    L_0x004f:
        return r9;
    L_0x0050:
        r7 = move-exception;
        r1 = "Mms_TXS_SVC";	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        r2 = "[mms_fdn]get fdn List exception ";	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        com.huawei.cspcommon.MLog.e(r1, r2, r7);	 Catch:{ Exception -> 0x0050, all -> 0x0060 }
        if (r6 == 0) goto L_0x005f;
    L_0x005c:
        r6.close();
    L_0x005f:
        return r9;
    L_0x0060:
        r1 = move-exception;
        if (r6 == 0) goto L_0x0066;
    L_0x0063:
        r6.close();
    L_0x0066:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.transaction.SmsReceiverService.isNumberInFdnList(java.lang.String, int):boolean");
    }

    public void onCreate() {
        super.onCreate();
        this.mHwCustSmsReceiverService = (HwCustSmsReceiverService) HwCustUtils.createObj(HwCustSmsReceiverService.class, new Object[0]);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mRcsSmsReceiverService == null) {
            this.mRcsSmsReceiverService = new RcsSmsReceiverService();
        }
        if (this.mRcsSmsReceiverService != null) {
            this.mRcsSmsReceiverService.setHwCustSmsReceiverService(this);
        }
        this.mHwCustUpdateUserBehavior = (HwCustUpdateUserBehavior) HwCustUtils.createObj(HwCustUpdateUserBehavior.class, new Object[0]);
        this.mHwCustMessagingNotification = (HwCustMessagingNotification) HwCustUtils.createObj(HwCustMessagingNotification.class, new Object[0]);
        HandlerThread thread = new HandlerThread("Mms_TXS_SVC", 10);
        thread.start();
        this.mServiceLooper = thread.getLooper();
        this.mServiceHandler = new ServiceHandler(this.mServiceLooper);
        this.mMmsRadarInfoManager = MmsRadarInfoManager.getInstance();
        this.mSendSmsHandler = this.mMmsRadarInfoManager.getHandler();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        int i = 0;
        if (MmsConfig.isSmsEnabled(this)) {
            int intExtra;
            if (intent != null) {
                intExtra = intent.getIntExtra("result", 0);
            } else {
                intExtra = 0;
            }
            this.mResultCode = intExtra;
            if (intent != null) {
                i = intent.getIntExtra("is_secret", 0);
            }
            this.mIsSecret = i;
            if (this.mResultCode != 0) {
                MLog.v("Mms_TXS_SVC", "onStart: #" + startId + " mResultCode: " + this.mResultCode + " = " + translateResultCode(this.mResultCode));
            }
            if (20 == startId) {
                ErrorMonitor.reportRadar(907000002, "SmsReceiverService not response!");
            }
            Message msg = this.mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            msg.arg2 = this.mResultCode;
            msg.obj = intent;
            this.mServiceHandler.sendMessage(msg);
            return 2;
        }
        MLog.d("Mms_TXS_SVC", "SmsReceiverService: is not the default sms app");
        SmsReceiver.finishStartingService(this, startId);
        return 2;
    }

    private static String translateResultCode(int resultCode) {
        switch (resultCode) {
            case -1:
                return "Activity.RESULT_OK";
            case 1:
                return "SmsManager.RESULT_ERROR_GENERIC_FAILURE";
            case 2:
                return "SmsManager.RESULT_ERROR_RADIO_OFF";
            case 3:
                return "SmsManager.RESULT_ERROR_NULL_PDU";
            case 4:
                return "SmsManager.RESULT_ERROR_NO_SERVICE";
            case 5:
                return "SmsManager.RESULT_ERROR_LIMIT_EXCEEDED";
            case 6:
                return "SmsManager.RESULT_ERROR_FDN_CHECK_FAILURE";
            default:
                return "Unknown error code";
        }
    }

    public void onDestroy() {
        if (this.mServiceLooper != null) {
            this.mServiceLooper.quit();
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
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

    private void handleServiceStateChanged(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            ServiceState serviceState = ServiceState.newFromBundle(extras);
            int subscription = MessageUtils.getSimIdFromIntent(intent, -1);
            if (!MessageUtils.isMultiSimEnabled() || -1 != subscription) {
                int prefSubscription = MessageUtils.getPreferredSmsSubscription();
                if (MessageUtils.isMultiSimEnabled()) {
                    synchronized (this) {
                        this.mActiveId = subscription;
                        MLog.d("Mms_TXS_SVC", "subscription is:" + subscription + " prefSubscription is:" + prefSubscription);
                    }
                }
                if (serviceState.getState() == 0) {
                    String inEcm = SystemProperties.get("ril.cdma.inecmmode");
                    if (inEcm == null || !Boolean.parseBoolean(inEcm)) {
                        if (MessageUtils.isMultiSimEnabled()) {
                            mIsInService[subscription] = Boolean.valueOf(true);
                        }
                        sendFirstQueuedMessage();
                    }
                }
            }
        }
    }

    private void handleSendMessage() {
        if (!this.mSending) {
            sendFirstQueuedMessage();
        }
    }

    private void handleSendInactiveMessage() {
        moveOutboxMessagesToQueuedBox();
        sendFirstQueuedMessage();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void sendFirstQueuedMessage() {
        boolean success = true;
        Uri uri = Uri.parse("content://sms/queued");
        ContentResolver resolver = getContentResolver();
        Cursor c = null;
        if (!MessageUtils.isMultiSimEnabled()) {
            c = SqliteWrapper.query(this, resolver, uri, SEND_PROJECTION, null, null, "date ASC");
        } else if ((mIsInService[0].booleanValue() && mIsInService[1].booleanValue()) || (!mIsInService[0].booleanValue() && !mIsInService[1].booleanValue())) {
            c = SqliteWrapper.query(this, resolver, uri, SEND_PROJECTION, null, null, "date ASC");
        } else if (mIsInService[0].booleanValue()) {
            MLog.d("Mms_TXS_SVC", "only sub0 is in service");
            c = SqliteWrapper.query(this, resolver, uri, SEND_PROJECTION, "sub_id = 0", null, "date ASC");
        } else if (mIsInService[1].booleanValue()) {
            MLog.d("Mms_TXS_SVC", "only sub1 is in service");
            c = SqliteWrapper.query(this, resolver, uri, SEND_PROJECTION, "sub_id = 1", null, "date ASC");
        } else {
            MLog.w("Mms_TXS_SVC", "Both subs is not in service");
        }
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    String msgText = c.getString(3);
                    String address = c.getString(2);
                    int threadId = c.getInt(1);
                    int status = c.getInt(4);
                    long msgId = c.getLong(0);
                    Uri msgUri = ContentUris.withAppendedId(Sms.CONTENT_URI, msgId);
                    long delayMsgId = c.getLong(6);
                    boolean isGroup = HwMessageUtils.isGroupSmsId(getApplicationContext(), delayMsgId);
                    if (!isGroup) {
                        delayMsgId = msgId;
                    }
                    if (DelaySendManager.getInst().isDelayMsg(delayMsgId, "sms", isGroup)) {
                        MLog.w("Mms_TXS_SVC", "it is Delay Msg");
                    } else {
                        int subId = c.getInt(5);
                        msgText = this.mCryptoSmsReceiverService.transBeforeSend(msgText, address, subId);
                        if (MessageUtils.isMultiSimEnabled()) {
                            MLog.d("Mms_TXS_SVC", "dual card message subId is:" + subId + " activeId is:" + this.mActiveId);
                            if (subId == this.mActiveId || 255 == this.mActiveId) {
                                this.mActiveId = 255;
                            } else {
                                this.mRetryTimes++;
                                this.mActiveId = 255;
                                MLog.d("Mms_TXS_SVC", "dual card message retry time is:" + this.mRetryTimes);
                                if (this.mRetryTimes < 10) {
                                    registerForServiceStateChanges();
                                    MLog.d("Mms_TXS_SVC", "dual card message retry !!");
                                } else {
                                    this.mRetryTimes = 0;
                                    messageFailedToSend(msgUri, 2);
                                    MLog.d("Mms_TXS_SVC", "dual card message send failed!!");
                                }
                                c.close();
                                return;
                            }
                        }
                        if (!MessageUtils.isMultiSimEnabled()) {
                            try {
                                subId = MessageUtils.getPreferredSmsSubscription();
                            } catch (NullPointerException e) {
                                subId = 0;
                            }
                        }
                        SmsMessageSender sender = new SmsSingleRecipientSender(this, address, msgText, (long) threadId, status == 32, msgUri, subId);
                        MLog.v("Mms_TXS_SVC", "sendFirstQueuedMessage " + msgUri + ", threadId: " + threadId);
                        Context context = getApplicationContext();
                        try {
                            if (this.mHwCustUpdateUserBehavior != null) {
                                this.startTime = System.currentTimeMillis();
                                this.mHwCustUpdateUserBehavior.saveTime(context, "send_sms_duration", TrainManager.DURATION, this.startTime);
                            }
                            sender.sendMessage(-1);
                            this.mSending = true;
                            this.mMmsRadarInfoManager.writeLogMsg(1311, "1007");
                            this.mSendSmsHandler.sendMessage(this.mSendSmsHandler.obtainMessage(102, subId, 0));
                        } catch (MmsException e2) {
                            if (this.mHwCustUpdateUserBehavior != null) {
                                this.mHwCustUpdateUserBehavior.upLoadSendMesFail(context, 0, "MmsException e", this.startTime, this.endTime);
                            }
                            this.mMmsRadarInfoManager.writeLogMsg(1311, "sms send to fw get exception:" + e2.toString());
                            this.mSending = false;
                            messageFailedToSend(msgUri, 1);
                            success = false;
                            sendBroadcast(new Intent("com.android.mms.transaction.SEND_MESSAGE", null, this, SmsReceiver.class));
                        }
                    }
                }
                c.close();
            } finally {
                c.close();
            }
        }
        if (success && ((mIsInService[0].booleanValue() && mIsInService[1].booleanValue()) || !MessageUtils.isMultiSimEnabled())) {
            unRegisterForServiceStateChanges();
        }
    }

    private void feedbackSmsState(Uri uri, int resultCode) {
        if (isMessagePlusServiceRunning()) {
            int size = MessagePlusService.getSmsUriListSize();
            if (size > 0) {
                int i = 0;
                while (i < size) {
                    String storeUri = MessagePlusService.getUriFromSmsUriList(i);
                    if (storeUri == null || !storeUri.equals(uri.toString())) {
                        i++;
                    } else {
                        MessagePlusService.notifySmsStateUri(resultCode);
                        MLog.d("Mms_TXS_SVC", "SmsReceiverService notifySmsStateUri==========================");
                        MessagePlusService.removeFromSmsUriList(i);
                        return;
                    }
                }
            }
        }
    }

    private void displayFdnIndicateDialog(Context context, String smsc, int sub) {
        Intent smsFdnDialogIntent = new Intent(context, FdnIndicateDialogActivity.class);
        smsFdnDialogIntent.putExtra(HarassNumberUtil.NUMBER, smsc);
        smsFdnDialogIntent.putExtra("subscription", sub);
        smsFdnDialogIntent.setFlags(402653184);
        context.startActivity(smsFdnDialogIntent);
    }

    private void messageFailedToSend(Uri uri, int error) {
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("Mms_TXS_SVC", "messageFailedToSend msg failed uri: " + uri + " error: " + error);
        }
        Intent intentbroadcast = new Intent();
        intentbroadcast.setAction("huawei.selfrepair.message");
        sendBroadcast(intentbroadcast);
        Sms.moveMessageToFolder(this, uri, 5, error);
        if (this.mHwCustSmsReceiverService != null) {
            this.mHwCustSmsReceiverService.handleMessageFailedToSend(getApplicationContext(), uri, error, this.mToastHandler);
        }
        MessagingNotification.notifySendFailed(getApplicationContext(), true);
        if (38 == error || 330 == error) {
            this.mMmsRadarInfoManager.tryToRepairSmscInCard(HwMessageUtils.querySubscription(getApplicationContext(), uri));
        }
    }

    public String getSmsCenterNumber(int subID) {
        String smsCenterNum = null;
        String[] strArray;
        if (MmsConfig.isModifySMSCenterAddressOnCard()) {
            smsCenterNum = MessageUtils.getSmsAddressBySubID(subID);
            if (!TextUtils.isEmpty(smsCenterNum)) {
                strArray = smsCenterNum.split("\"");
                if (strArray.length > 1) {
                    smsCenterNum = strArray[1];
                }
            }
            if (!TextUtils.isEmpty(smsCenterNum) || MmsConfig.getSMSCAddress() == null) {
                return smsCenterNum;
            }
            return MmsConfig.getSMSCAddress();
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        switch (subID) {
            case -1:
                smsCenterNum = sp.getString("sms_center_number", null);
                break;
            case 0:
                smsCenterNum = sp.getString("pref_key_simuim1_message_center", null);
                break;
            case 1:
                smsCenterNum = sp.getString("pref_key_simuim2_message_center", null);
                break;
        }
        if (!TextUtils.isEmpty(smsCenterNum)) {
            return smsCenterNum;
        }
        smsCenterNum = MessageUtils.getSmsAddressBySubID(subID);
        if (!TextUtils.isEmpty(smsCenterNum)) {
            strArray = smsCenterNum.split("\"");
            if (strArray.length > 1) {
                smsCenterNum = strArray[1];
            }
        }
        if (TextUtils.isEmpty(smsCenterNum) && MmsConfig.getSMSCAddress() != null) {
            smsCenterNum = MmsConfig.getSMSCAddress();
        }
        if (TextUtils.isEmpty(smsCenterNum)) {
            return smsCenterNum;
        }
        setSmsCenterNumberOnBoard(subID, smsCenterNum);
        return smsCenterNum;
    }

    private void setSmsCenterNumberOnBoard(int subId, String number) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        switch (subId) {
            case -1:
                editor.putString("sms_center_number", number);
                break;
            case 0:
                editor.putString("pref_key_simuim1_message_center", number);
                break;
            case 1:
                editor.putString("pref_key_simuim2_message_center", number);
                break;
        }
        editor.commit();
    }

    private void handleSmsReceived(Intent intent, int error) {
        SmsMessage[] msgs = Intents.getMessagesFromIntent(intent);
        String format = intent.getStringExtra("format");
        if (msgs.length <= 0 || msgs[0] == null) {
            MLog.d("Mms_TXS_SVC", "receive a empty sms Intent");
            return;
        }
        if (this.mHwCustUpdateUserBehavior != null) {
            this.mHwCustUpdateUserBehavior.upLoadReceiveMesInfo(getApplicationContext(), 0);
        }
        if (msgs[0].mWrappedSmsMessage == null) {
            MLog.e("Mms_TXS_SVC", "msgs[0].mWrappedSmsMessage is null");
        } else if (64 == msgs[0].getProtocolIdentifier()) {
            MLog.d("Mms_TXS_SVC", "Type 0 sms. from ***");
        } else if (this.mHwCustSmsReceiverService == null || !(this.mHwCustSmsReceiverService.isDiscardSms(msgs[0].getMessageBody()) || this.mHwCustSmsReceiverService.isDiscardSMSFrom3311(msgs[0]))) {
            Uri messageUri = insertMessage(this, msgs, intent, error, format);
            int subscription = MessageUtils.getSubId(msgs[0]);
            boolean isInfoMsg = HwMessageUtils.isInfoMsg(getApplicationContext(), messageUri);
            if (MessageUtils.isMultiSimActive()) {
                if (subscription == 0 && !isInfoMsg) {
                    StatisticalHelper.incrementReportCount(getApplicationContext(), 2012);
                } else if (1 == subscription && !isInfoMsg) {
                    StatisticalHelper.incrementReportCount(getApplicationContext(), 2013);
                } else if (subscription == 0 && isInfoMsg) {
                    StatisticalHelper.incrementReportCount(getApplicationContext(), 2010);
                } else if (1 == subscription && isInfoMsg) {
                    StatisticalHelper.incrementReportCount(getApplicationContext(), 2011);
                }
            }
            if (isInfoMsg) {
                StatisticalHelper.incrementReportCount(getApplicationContext(), 2065);
            }
            StatisticalHelper.incrementReportCount(this, AMapException.CODE_AMAP_SERVICE_MAINTENANCE);
            if (MLog.isLoggable("Mms_TXN", 2)) {
                MLog.v("Mms_TXS_SVC", "handleSmsReceived. replace " + msgs[0].isReplace());
            }
            if (messageUri != null) {
                long threadId = MessagingNotification.getSmsThreadId(this, messageUri);
                if (MmsConfig.getSupportSmartSmsFeature()) {
                    SmartSmsSdkUtil.parseMsg(messageUri.getLastPathSegment(), msgs);
                }
                MLog.d("Mms_TXS_SVC", "handleSmsReceived messageUri: " + messageUri + " threadId: " + threadId);
                if (MmsConfig.isSmsDefaultApp(this) && !RcsTransaction.isIncallChatting(threadId)) {
                    if (mHwCustEcidLookup == null || msgs[0] == null || !mHwCustEcidLookup.delayedNotification(getApplicationContext(), threadId, false, msgs[0].getOriginatingAddress())) {
                        MessagingNotification.blockingUpdateNewMessageIndicator((Context) this, threadId, messageUri);
                    }
                }
                if (!(WindowManagerEx.isTopFullscreen() || this.mCryptoSmsReceiverService.isReceivedMsgBodyEncrypted())) {
                    FloatMmsRequsetReceiver.noticeNewMessage(getApplicationContext(), Long.valueOf(threadId), messageUri);
                }
                this.mCryptoSmsReceiverService.setReceivedMsgBodyEncrypted(false);
                MessageUtils.updateConvListInDeleteMode(getApplicationContext(), threadId, extractContentValues(msgs[0]).getAsString("address"), messageUri);
                CspFragment.setNotificationCleared(false);
                String number = msgs[0].getOriginatingAddress();
                if (number != null) {
                    final String str = number;
                    HwBackgroundLoader.getBackgroundHandler().post(new Runnable() {
                        public void run() {
                            ArrayList<String> numbers = new ArrayList();
                            numbers.add(HwMessageUtils.replaceNumberFromDatabase(str, SmsReceiverService.this.getApplicationContext()));
                            HwMessageUtils.updateRecentContactsToDB(SmsReceiverService.this.getApplicationContext(), numbers);
                        }
                    });
                }
            }
        } else {
            MLog.w("Mms_TXS_SVC", "WARNING .... HwCustSmsReceiverService drop  a received Sms!");
        }
    }

    private void handleBootCompleted() {
        if (moveOutboxMessagesToFailedBox() > 0) {
            MessagingNotification.notifySendFailed(getApplicationContext(), true);
        }
        sendFirstQueuedMessage();
        if (MmsConfig.noticeNewMessageWhenBootup()) {
            MessagingNotification.blockingUpdateNewMessageIndicator((Context) this, -1, false);
        }
    }

    private int moveOutboxMessagesToQueuedBox() {
        ContentValues values = new ContentValues(1);
        values.put(NumberInfo.TYPE_KEY, Integer.valueOf(6));
        int messageCount = SqliteWrapper.update(getApplicationContext(), getContentResolver(), Outbox.CONTENT_URI, values, "type = 4", null);
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("Mms_TXS_SVC", "moveOutboxMessagesToQueuedBox messageCount: " + messageCount);
        }
        return messageCount;
    }

    private int moveOutboxMessagesToFailedBox() {
        ContentValues values = new ContentValues(3);
        values.put(NumberInfo.TYPE_KEY, Integer.valueOf(5));
        values.put("error_code", Integer.valueOf(1));
        values.put("read", Integer.valueOf(0));
        int messageCount = SqliteWrapper.update(getApplicationContext(), getContentResolver(), Outbox.CONTENT_URI, values, "type = 4", null);
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("Mms_TXS_SVC", "moveOutboxMessagesToFailedBox messageCount: " + messageCount);
        }
        return messageCount;
    }

    private Uri insertMessage(Context context, SmsMessage[] msgs, Intent intent, int error, String format) {
        SmsMessage sms = msgs[0];
        MessageClass clazz = sms.getMessageClass();
        boolean isReplace = sms.isReplace();
        boolean isEnableSpecialSMS = MmsConfig.getEnableSpecialSMS();
        MLog.v("Mms_TXS_SVC", "insertMessage: MessageClass = " + clazz + ", isReplace = " + isReplace + ", isEnableSpecialSMS = " + isEnableSpecialSMS);
        if (isEnableSpecialSMS) {
            if (clazz == MessageClass.CLASS_0) {
                displayClassZeroMessage(context, intent, format);
                return null;
            } else if (isReplace) {
                return replaceMessage(context, msgs, error);
            } else {
                return storeMessage(context, msgs, error);
            }
        } else if (clazz == MessageClass.CLASS_0 || clazz == MessageClass.CLASS_1 || clazz == MessageClass.CLASS_2 || clazz == MessageClass.CLASS_3 || isReplace) {
            return null;
        } else {
            return storeMessage(context, msgs, error);
        }
    }

    private Uri replaceMessage(Context context, SmsMessage[] msgs, int error) {
        SmsMessage sms = msgs[0];
        ContentValues values = extractContentValues(sms);
        values.put("error_code", Integer.valueOf(error));
        String bodyText = "";
        if (pduCount == 1) {
            values.put("body", replaceFormFeeds(replaceFormFeeds(sms.getDisplayMessageBody())));
        } else {
            StringBuilder body = new StringBuilder();
            for (SmsMessage sms2 : msgs) {
                if (sms2.mWrappedSmsMessage != null) {
                    body.append(sms2.getDisplayMessageBody());
                }
            }
            bodyText = replaceFormFeeds(body.toString());
            values.put("body", replaceFormFeeds(bodyText));
            if (bodyText.length() > 70) {
                StatisticalHelper.incrementReportCount(getApplicationContext(), 2061);
            }
        }
        ContentResolver resolver = context.getContentResolver();
        String originatingAddress = sms2.getOriginatingAddress();
        int protocolIdentifier = sms2.getProtocolIdentifier();
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("Mms_TXS_SVC", " SmsReceiverService: replaceMessage:");
        }
        Context context2 = context;
        Cursor cursor = SqliteWrapper.query(context2, resolver, Inbox.CONTENT_URI, REPLACE_PROJECTION, "address = ? AND protocol = ? AND sub_id = ? ", new String[]{originatingAddress, Integer.toString(protocolIdentifier), Integer.toString(MessageUtils.getSubId(sms2))}, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    Uri messageUri = ContentUris.withAppendedId(Sms.CONTENT_URI, cursor.getLong(0));
                    SqliteWrapper.update(context, resolver, messageUri, values, null, null);
                    return messageUri;
                }
                cursor.close();
            } finally {
                cursor.close();
            }
        }
        return storeMessage(context, msgs, error);
    }

    public static String replaceFormFeeds(String s) {
        String str = "";
        if (s != null) {
            return s.replace('\f', '\n');
        }
        return str;
    }

    private Uri storeMessage(Context context, SmsMessage[] msgs, int error) {
        String bodyText;
        SmsMessage sms = msgs[0];
        boolean isClass2 = sms.getMessageClass() == MessageClass.CLASS_2;
        ContentValues values = extractContentValues(sms);
        values.put("error_code", Integer.valueOf(error));
        int sub = MessageUtils.getSubId(sms);
        if (MessageUtils.isCTCdmaCardInGsmMode()) {
            sub = 0;
        }
        values.put("sub_id", Integer.valueOf(sub));
        values.put("network_type", Integer.valueOf(MessageUtils.getNetworkType(MessageUtils.getSubId(sms))));
        if (pduCount == 1) {
            bodyText = this.mCryptoSmsReceiverService.transBeforeStore(replaceFormFeeds(sms.getDisplayMessageBody()), sub);
            values.put("body", bodyText);
        } else {
            StringBuilder body = new StringBuilder();
            for (SmsMessage sms2 : msgs) {
                if (sms2.mWrappedSmsMessage != null) {
                    body.append(sms2.getDisplayMessageBody());
                }
            }
            bodyText = this.mCryptoSmsReceiverService.transBeforeStore(replaceFormFeeds(body.toString()), sub);
            values.put("body", bodyText);
        }
        if (bodyText.length() > 70) {
            StatisticalHelper.incrementReportCount(getApplicationContext(), 2061);
        }
        if (this.mCryptoSmsReceiverService.isCryptoRegistResponse(bodyText, sub)) {
            MLog.d("Mms_TXS_SVC", "storeMessage regist crypto message does not save.");
            return null;
        }
        boolean needSaveIcc = false;
        if (MessageUtils.isMultiSimEnabled() && MmsConfig.isSaveModeMultiCardPerf()) {
            if (PreferenceUtils.getUsingSIMCardStorageWithSubId(context, sub) && !isClass2) {
                needSaveIcc = true;
            }
        } else if (PreferenceUtils.getUsingSIMCardStorage(context) && !isClass2) {
            needSaveIcc = true;
        }
        if (this.mCryptoSmsReceiverService.mIsReceivedMsgBodyEncrypted) {
            needSaveIcc = false;
        }
        if (needSaveIcc) {
            final int i = pduCount;
            final SmsMessage[] smsMessageArr = msgs;
            ThreadEx.execute(new Runnable() {
                public void run() {
                    for (int i = 0; i < i; i++) {
                        if (SmsReceiverService.this.insertSMSToIcc(smsMessageArr[i]) == null) {
                            MLog.d("Mms_TXS_SVC", "insertMessagesToIcc failed");
                            break;
                        }
                        MLog.d("Mms_TXS_SVC", "insertMessagesToIcc");
                        StatisticalHelper.incrementReportCount(SmsReceiverService.this.getApplicationContext(), 2063);
                    }
                    SimCursorManager.self().clearCursor();
                    SimCursorManager.self().clearCursor(1);
                    SimCursorManager.self().clearCursor(2);
                    LocalBroadcastManager.getInstance(SmsReceiverService.this).sendBroadcast(new Intent("android.simcard.action.SMS_SIM_RECEIVED"));
                }
            });
        }
        Long threadId = values.getAsLong("thread_id");
        if (threadId == null) {
            threadId = Long.valueOf(0);
        }
        String address = values.getAsString("address");
        boolean isServerAddr = MessageUtils.isServerAddress(address);
        if (mHwCustEcidLookup != null) {
            mHwCustEcidLookup.addSender(address);
        }
        Contact cacheContact = null;
        if (TextUtils.isEmpty(address)) {
            address = getString(R.string.unknown_sender);
            values.put("address", address);
        } else {
            cacheContact = Contact.get(address, true);
            if (MmsConfig.getIsRefreshRxNum()) {
                cacheContact.setNumber(address);
            }
        }
        if (threadId.longValue() == 0 && !TextUtils.isEmpty(address)) {
            threadId = Long.valueOf(Conversation.getOrCreateThreadId(context, address, isServerAddr, true));
            values.put("thread_id", threadId);
        }
        if (MmsConfig.getIsRefreshRxNum()) {
            ContactList list = new ContactList();
            list.add(cacheContact);
            if (threadId.longValue() != 0) {
                MLog.v("Mms_TXS_SVC", "Sms storeMessage threadId =" + threadId);
                RecipientIdCache.updateNumbers(threadId.longValue(), list);
            }
        }
        values.put("addr_body", HwMessageUtils.getAddressPos(bodyText));
        values.put("time_body", HwMessageUtils.getTimePosString(bodyText));
        ContentResolver resolver = context.getContentResolver();
        Uri insertedUri = null;
        if (this.mHwCustSmsReceiverService != null) {
            MLog.d("Mms_TXS_SVC", "storeMessage: HwCust storeMessage");
            insertedUri = this.mHwCustSmsReceiverService.hwCustStoreMessage(context, resolver, Inbox.CONTENT_URI, values);
        }
        if (insertedUri == null) {
            MLog.d("Mms_TXS_SVC", "storeMessage: to insert sms");
            insertedUri = SqliteWrapper.insert(context, resolver, Inbox.CONTENT_URI, values);
        }
        HwMessageUtils.checkRiskUrlAndWriteToDB(context, insertedUri, bodyText, address);
        Conversation.checkConversationWithMessages(context, threadId.longValue());
        Recycler.getSmsRecycler().deleteOldMessagesByThreadId(context, threadId.longValue());
        MmsWidgetProvider.notifyDatasetChanged(context);
        Conversation.updateHwSendName(context, threadId.longValue(), address, bodyText);
        HwMessageUtils.triggleCallMissingChr(context, sub, address, bodyText, 2);
        return insertedUri;
    }

    public Uri insertSMSToIcc(SmsMessage msg) {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        Uri uri = Uri.parse("content://sms/addtoicc");
        int subId = MessageUtils.getSubId(msg);
        if (MessageUtils.isMultiSimEnabled()) {
            if (subId == 0) {
                uri = Uri.parse("content://sms/copytoicc1");
            } else {
                uri = Uri.parse("content://sms/copytoicc2");
            }
        }
        values.put("status", Integer.valueOf(1));
        values.put("address", msg.getDisplayOriginatingAddress());
        values.put("timestamp", Long.valueOf(System.currentTimeMillis()));
        values.put("body", msg.getDisplayMessageBody());
        Uri ret = resolver.insert(uri, values);
        int simMsgCount = MessageUtils.getSIMMsgCount(getApplicationContext());
        MLog.d("Mms_TXS_SVC", "simMsgCount = " + simMsgCount + " simMaxMsgCount = " + PreferenceUtils.getSimMaxMessageCount());
        if (ret == null) {
            MLog.e("Mms_TXN", "save sms to icc failed");
        }
        return ret;
    }

    private ContentValues extractContentValues(SmsMessage sms) {
        int i = 1;
        ContentValues values = new ContentValues();
        values.put("address", sms.getDisplayOriginatingAddress());
        long now = System.currentTimeMillis();
        if (this.mHwCustSmsReceiverService != null) {
            now = this.mHwCustSmsReceiverService.getReceivedTime(now, sms.getTimestampMillis());
        }
        values.put("date", Long.valueOf(now));
        values.put("date_sent", Long.valueOf(sms.getTimestampMillis()));
        values.put("protocol", Integer.valueOf(sms.getProtocolIdentifier()));
        values.put("read", Integer.valueOf(0));
        values.put("seen", Integer.valueOf(0));
        if (sms.getPseudoSubject().length() > 0) {
            values.put("subject", sms.getPseudoSubject());
        }
        values.put("reply_path_present", Integer.valueOf(sms.isReplyPathPresent() ? 1 : 0));
        values.put("service_center", sms.getServiceCenterAddress());
        String str = "is_secret";
        if (this.mIsSecret != 1) {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        return values;
    }

    private void displayClassZeroMessage(Context context, Intent intent, String format) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            MLog.w("Mms_TXS_SVC", "displayClassZeroMessage: extras is null!");
            return;
        }
        context.startActivity(new Intent(context, ClassZeroActivity.class).putExtras(extras).putExtra("format", format).setFlags(402653184));
        if (MmsConfig.getEnableChangeClassZeroMessageShow()) {
            MessagingNotification.sendClassZeroMessageNotification(context, intent);
        }
    }

    private void registerForServiceStateChanges() {
        Context context = getApplicationContext();
        unRegisterForServiceStateChanges();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SERVICE_STATE");
        intentFilter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        MLog.v("Mms_TXN", "Mms_TXS_SVC", "registerForServiceStateChanges");
        context.registerReceiver(SmsReceiver.getInstance(), intentFilter);
    }

    private void unRegisterForServiceStateChanges() {
        MLog.v("Mms_TXN", "Mms_TXS_SVC", "unRegisterForServiceStateChanges");
        try {
            getApplicationContext().unregisterReceiver(SmsReceiver.getInstance());
        } catch (IllegalArgumentException e) {
            MLog.w("Mms_TXS_SVC", "SmsReceiverService unRegisterForServiceStateChanges has exception" + e.getMessage());
        }
    }
}
