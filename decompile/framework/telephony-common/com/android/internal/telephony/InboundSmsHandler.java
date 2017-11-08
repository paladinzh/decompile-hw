package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.app.BroadcastOptions;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.provider.SettingsEx.Systemex;
import android.provider.Telephony.Carriers;
import android.provider.Telephony.CellBroadcasts;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Inbox;
import android.provider.Telephony.Sms.Intents;
import android.provider.Telephony.TextBasedSmsColumns;
import android.service.carrier.ICarrierMessagingCallback.Stub;
import android.service.carrier.ICarrierMessagingService;
import android.service.carrier.MessagePdu;
import android.telephony.CarrierMessagingServiceManager;
import android.telephony.Rlog;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Jlog;
import com.android.internal.telephony.SmsConstants.MessageClass;
import com.android.internal.telephony.SmsHeader.ConcatRef;
import com.android.internal.telephony.SmsHeader.PortAddrs;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.huawei.internal.telephony.HwRadarUtils;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class InboundSmsHandler extends StateMachine {
    public static final int ADDRESS_COLUMN = 6;
    public static final int COUNT_COLUMN = 5;
    public static final int DATE_COLUMN = 3;
    protected static final boolean DBG = true;
    public static final int DESTINATION_PORT_COLUMN = 2;
    protected static final int EVENT_BROADCAST_COMPLETE = 3;
    public static final int EVENT_BROADCAST_SMS = 2;
    public static final int EVENT_INJECT_SMS = 8;
    public static final int EVENT_NEW_SMS = 1;
    private static final int EVENT_RELEASE_WAKELOCK = 5;
    private static final int EVENT_RETURN_TO_IDLE = 4;
    public static final int EVENT_START_ACCEPTING_SMS = 6;
    private static final int EVENT_UPDATE_PHONE_OBJECT = 7;
    public static final int ID_COLUMN = 7;
    private static final String INTERCEPTION_SMS_RECEIVED = "android.provider.Telephony.INTERCEPTION_SMS_RECEIVED";
    private static final int MAX_SMS_LIST_DEFAULT = 30;
    public static final int MESSAGE_BODY_COLUMN = 8;
    private static final int NOTIFICATION_ID_NEW_MESSAGE = 1;
    private static final String NOTIFICATION_TAG = "InboundSmsHandler";
    private static final int NO_DESTINATION_PORT = -1;
    static final long PARTIAL_SEGMENT_EXPIRE_TIME = 259200000;
    public static final int PDU_COLUMN = 0;
    private static final String[] PDU_PROJECTION = new String[]{"pdu"};
    private static final String[] PDU_SEQUENCE_PORT_PROJECTION = new String[]{"pdu", "sequence", "destination_port"};
    private static final String RECEIVE_SMS_PERMISSION = "huawei.permission.RECEIVE_SMS_INTERCEPTION";
    public static final int REFERENCE_NUMBER_COLUMN = 4;
    public static final String SELECT_BY_ID = "_id=?";
    public static final String SELECT_BY_REFERENCE = "address=? AND reference_number=? AND count=? AND deleted=0";
    public static final int SEQUENCE_COLUMN = 1;
    private static final int SMS_BROADCAST_DURATION_TIMEOUT = 180000;
    private static final int SMS_INSERTDB_DURATION_TIMEOUT = 60000;
    private static final boolean VDBG = false;
    private static final int WAKELOCK_TIMEOUT = 3000;
    protected static final Uri sRawUri = Uri.withAppendedPath(Sms.CONTENT_URI, "raw");
    protected static final Uri sRawUriPermanentDelete = Uri.withAppendedPath(Sms.CONTENT_URI, "raw/permanentDelete");
    private final int DELETE_PERMANENTLY = 1;
    private final int MARK_DELETED = 2;
    private String defaultSmsApplicationName = "";
    private boolean isAlreadyDurationTimeout = false;
    private boolean isClass0 = false;
    private AtomicInteger mAlreadyReceivedSms = new AtomicInteger(0);
    protected CellBroadcastHandler mCellBroadcastHandler;
    protected final Context mContext;
    private final DefaultState mDefaultState = new DefaultState();
    private final DeliveringState mDeliveringState = new DeliveringState();
    IDeviceIdleController mDeviceIdleController;
    private final IdleState mIdleState = new IdleState();
    private ContentObserver mInsertObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfUpdate) {
            InboundSmsHandler.this.mAlreadyReceivedSms.getAndDecrement();
            if (InboundSmsHandler.this.mAlreadyReceivedSms.get() < 0) {
                InboundSmsHandler.this.mAlreadyReceivedSms.set(0);
            }
        }
    };
    protected Phone mPhone;
    private final ContentResolver mResolver;
    private ArrayList<byte[]> mSmsList = new ArrayList();
    private final boolean mSmsReceiveDisabled;
    private final StartupState mStartupState = new StartupState();
    protected SmsStorageMonitor mStorageMonitor;
    private Runnable mUpdateCountRunner = new Runnable() {
        public void run() {
            if (InboundSmsHandler.this.mAlreadyReceivedSms.get() > 0) {
                HwRadarUtils.report(InboundSmsHandler.this.mContext, HwRadarUtils.ERR_SMS_RECEIVE, "sms receive fail:" + InboundSmsHandler.this.defaultSmsApplicationName, InboundSmsHandler.this.subIdForReceivedSms);
            }
            InboundSmsHandler.this.mAlreadyReceivedSms.set(0);
        }
    };
    private UserManager mUserManager;
    private final WaitingState mWaitingState = new WaitingState();
    private final WakeLock mWakeLock;
    private final WapPushOverSms mWapPush;
    private int subIdForReceivedSms = -1;

    private final class CarrierSmsFilter extends CarrierMessagingServiceManager {
        private final int mDestPort;
        private final byte[][] mPdus;
        private final SmsBroadcastReceiver mSmsBroadcastReceiver;
        private volatile CarrierSmsFilterCallback mSmsFilterCallback;
        private final String mSmsFormat;

        CarrierSmsFilter(byte[][] pdus, int destPort, String smsFormat, SmsBroadcastReceiver smsBroadcastReceiver) {
            this.mPdus = pdus;
            this.mDestPort = destPort;
            this.mSmsFormat = smsFormat;
            this.mSmsBroadcastReceiver = smsBroadcastReceiver;
        }

        void filterSms(String carrierPackageName, CarrierSmsFilterCallback smsFilterCallback) {
            this.mSmsFilterCallback = smsFilterCallback;
            if (bindToCarrierMessagingService(InboundSmsHandler.this.mContext, carrierPackageName)) {
                InboundSmsHandler.this.logv("bindService() for carrier messaging service succeeded");
                return;
            }
            InboundSmsHandler.this.loge("bindService() for carrier messaging service failed");
            smsFilterCallback.onFilterComplete(0);
        }

        protected void onServiceReady(ICarrierMessagingService carrierMessagingService) {
            try {
                carrierMessagingService.filterSms(new MessagePdu(Arrays.asList(this.mPdus)), this.mSmsFormat, this.mDestPort, InboundSmsHandler.this.mPhone.getSubId(), this.mSmsFilterCallback);
            } catch (RemoteException e) {
                InboundSmsHandler.this.loge("Exception filtering the SMS: " + e);
                this.mSmsFilterCallback.onFilterComplete(0);
            }
        }
    }

    private final class CarrierSmsFilterCallback extends Stub {
        private final CarrierSmsFilter mSmsFilter;
        private final boolean mUserUnlocked;

        CarrierSmsFilterCallback(CarrierSmsFilter smsFilter, boolean userUnlocked) {
            this.mSmsFilter = smsFilter;
            this.mUserUnlocked = userUnlocked;
        }

        public void onFilterComplete(int result) {
            this.mSmsFilter.disposeConnection(InboundSmsHandler.this.mContext);
            InboundSmsHandler.this.logv("onFilterComplete: result is " + result);
            if ((result & 1) != 0) {
                long token = Binder.clearCallingIdentity();
                try {
                    InboundSmsHandler.this.deleteFromRawTable(this.mSmsFilter.mSmsBroadcastReceiver.mDeleteWhere, this.mSmsFilter.mSmsBroadcastReceiver.mDeleteWhereArgs, 2);
                    InboundSmsHandler.this.sendMessage(3);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else if (this.mUserUnlocked) {
                InboundSmsHandler.this.dispatchSmsDeliveryIntent(this.mSmsFilter.mPdus, this.mSmsFilter.mSmsFormat, this.mSmsFilter.mDestPort, this.mSmsFilter.mSmsBroadcastReceiver);
            } else {
                if (!InboundSmsHandler.this.isSkipNotifyFlagSet(result)) {
                    InboundSmsHandler.this.showNewMessageNotification();
                }
                InboundSmsHandler.this.sendMessage(3);
            }
        }

        public void onSendSmsComplete(int result, int messageRef) {
            InboundSmsHandler.this.loge("Unexpected onSendSmsComplete call with result: " + result);
        }

        public void onSendMultipartSmsComplete(int result, int[] messageRefs) {
            InboundSmsHandler.this.loge("Unexpected onSendMultipartSmsComplete call with result: " + result);
        }

        public void onSendMmsComplete(int result, byte[] sendConfPdu) {
            InboundSmsHandler.this.loge("Unexpected onSendMmsComplete call with result: " + result);
        }

        public void onDownloadMmsComplete(int result) {
            InboundSmsHandler.this.loge("Unexpected onDownloadMmsComplete call with result: " + result);
        }
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public boolean processMessage(Message msg) {
            switch (msg.what) {
                case 7:
                    InboundSmsHandler.this.onUpdatePhoneObject((Phone) msg.obj);
                    break;
                default:
                    String errorText = "processMessage: unhandled message type " + msg.what + " currState=" + InboundSmsHandler.this.getCurrentState().getName();
                    if (!Build.IS_DEBUGGABLE) {
                        InboundSmsHandler.this.loge(errorText);
                        break;
                    }
                    InboundSmsHandler.this.loge("---- Dumping InboundSmsHandler ----");
                    InboundSmsHandler.this.loge("Total records=" + InboundSmsHandler.this.getLogRecCount());
                    for (int i = Math.max(InboundSmsHandler.this.getLogRecSize() - 20, 0); i < InboundSmsHandler.this.getLogRecSize(); i++) {
                        if (InboundSmsHandler.this.getLogRec(i) != null) {
                            InboundSmsHandler.this.loge("Rec[%d]: %s\n" + i + InboundSmsHandler.this.getLogRec(i).toString());
                        }
                    }
                    InboundSmsHandler.this.loge("---- Dumped InboundSmsHandler ----");
                    throw new RuntimeException(errorText);
            }
            return true;
        }
    }

    private class DeliveringState extends State {
        private DeliveringState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Delivering state");
        }

        public void exit() {
            InboundSmsHandler.this.log("leaving Delivering state");
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("DeliveringState.processMessage:" + msg.what);
            switch (msg.what) {
                case 1:
                    InboundSmsHandler.this.handleNewSms((AsyncResult) msg.obj);
                    InboundSmsHandler.this.sendMessage(4);
                    return true;
                case 2:
                    if (InboundSmsHandler.this.processMessagePart(msg.obj)) {
                        InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mWaitingState);
                    } else {
                        InboundSmsHandler.this.log("No broadcast sent on processing EVENT_BROADCAST_SMS in Delivering state. Return to Idle state");
                        InboundSmsHandler.this.sendMessage(4);
                    }
                    return true;
                case 4:
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mIdleState);
                    return true;
                case 5:
                    InboundSmsHandler.this.mWakeLock.release();
                    if (!InboundSmsHandler.this.mWakeLock.isHeld()) {
                        InboundSmsHandler.this.loge("mWakeLock released while delivering/broadcasting!");
                    }
                    return true;
                case 8:
                    InboundSmsHandler.this.handleInjectSms((AsyncResult) msg.obj);
                    InboundSmsHandler.this.sendMessage(4);
                    return true;
                default:
                    return false;
            }
        }
    }

    private class IdleState extends State {
        private IdleState() {
        }

        public void enter() {
            InboundSmsHandler.this.log("entering Idle state");
            InboundSmsHandler.this.sendMessageDelayed(5, 3000);
        }

        public void exit() {
            InboundSmsHandler.this.mWakeLock.acquire();
            InboundSmsHandler.this.log("acquired wakelock, leaving Idle state");
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("IdleState.processMessage:" + msg.what);
            InboundSmsHandler.this.log("Idle state processing message type " + msg.what);
            switch (msg.what) {
                case 1:
                case 2:
                case 8:
                    InboundSmsHandler.this.deferMessage(msg);
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mDeliveringState);
                    return true;
                case 4:
                    return true;
                case 5:
                    InboundSmsHandler.this.mWakeLock.release();
                    if (InboundSmsHandler.this.mWakeLock.isHeld()) {
                        InboundSmsHandler.this.log("mWakeLock is still held after release");
                    } else {
                        InboundSmsHandler.this.log("mWakeLock released");
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    private final class SmsBroadcastReceiver extends BroadcastReceiver {
        private long mBroadcastTimeNano = System.nanoTime();
        private final String mDeleteWhere;
        private final String[] mDeleteWhereArgs;

        SmsBroadcastReceiver(InboundSmsTracker tracker) {
            this.mDeleteWhere = tracker.getDeleteWhere();
            this.mDeleteWhereArgs = tracker.getDeleteWhereArgs();
        }

        public void onReceive(Context context, Intent intent) {
            if (HwTelephonyFactory.getHwInnerSmsManager().shouldSendReceivedActionInPrivacyMode(InboundSmsHandler.this, context, intent, this.mDeleteWhere, this.mDeleteWhereArgs)) {
                String action = intent.getAction();
                if (action.equals(Intents.SMS_DELIVER_ACTION)) {
                    intent.setAction(Intents.SMS_RECEIVED_ACTION);
                    intent.setComponent(null);
                    Intent intent2 = intent;
                    InboundSmsHandler.this.dispatchIntent(intent2, "android.permission.RECEIVE_SMS", 16, InboundSmsHandler.this.handleSmsWhitelisting(null), this, UserHandle.ALL);
                } else if (action.equals(Intents.WAP_PUSH_DELIVER_ACTION)) {
                    intent.setAction(Intents.WAP_PUSH_RECEIVED_ACTION);
                    intent.setComponent(null);
                    Bundle options = null;
                    try {
                        long duration = InboundSmsHandler.this.mDeviceIdleController.addPowerSaveTempWhitelistAppForMms(InboundSmsHandler.this.mContext.getPackageName(), 0, "mms-broadcast");
                        BroadcastOptions bopts = BroadcastOptions.makeBasic();
                        bopts.setTemporaryAppWhitelistDuration(duration);
                        options = bopts.toBundle();
                    } catch (RemoteException e) {
                    }
                    String mimeType = intent.getType();
                    InboundSmsHandler.this.dispatchIntent(intent, WapPushOverSms.getPermissionForType(mimeType), WapPushOverSms.getAppOpsPermissionForIntent(mimeType), options, this, UserHandle.SYSTEM);
                } else {
                    int rc;
                    if (!(Intents.DATA_SMS_RECEIVED_ACTION.equals(action) || Intents.SMS_RECEIVED_ACTION.equals(action) || Intents.DATA_SMS_RECEIVED_ACTION.equals(action) || Intents.WAP_PUSH_RECEIVED_ACTION.equals(action))) {
                        InboundSmsHandler.this.loge("unexpected BroadcastReceiver action: " + action);
                    }
                    if ("true".equals(Systemex.getString(InboundSmsHandler.this.mResolver, "disableErrWhenReceiveSMS"))) {
                        rc = -1;
                    } else {
                        rc = getResultCode();
                    }
                    if (rc == -1 || rc == 1) {
                        InboundSmsHandler.this.log("successful broadcast, deleting from raw table.");
                    } else {
                        InboundSmsHandler.this.loge("a broadcast receiver set the result code to " + rc + ", deleting from raw table anyway!");
                    }
                    InboundSmsHandler.this.deleteFromRawTable(this.mDeleteWhere, this.mDeleteWhereArgs, 2);
                    InboundSmsHandler.this.sendMessage(3);
                    int durationMillis = (int) ((System.nanoTime() - this.mBroadcastTimeNano) / 1000000);
                    if (durationMillis >= AbstractPhoneBase.SET_TO_AOTO_TIME) {
                        InboundSmsHandler.this.loge("Slow ordered broadcast completion time: " + durationMillis + " ms");
                    } else {
                        InboundSmsHandler.this.log("ordered broadcast completed in: " + durationMillis + " ms");
                    }
                    InboundSmsHandler.this.reportSmsReceiveTimeout(durationMillis);
                }
            }
        }
    }

    private class StartupState extends State {
        private StartupState() {
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("StartupState.processMessage:" + msg.what);
            switch (msg.what) {
                case 1:
                case 2:
                case 8:
                    InboundSmsHandler.this.deferMessage(msg);
                    return true;
                case 6:
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mIdleState);
                    return true;
                default:
                    return false;
            }
        }
    }

    private class WaitingState extends State {
        private WaitingState() {
        }

        public boolean processMessage(Message msg) {
            InboundSmsHandler.this.log("WaitingState.processMessage:" + msg.what);
            switch (msg.what) {
                case 2:
                    InboundSmsHandler.this.deferMessage(msg);
                    return true;
                case 3:
                    InboundSmsHandler.this.sendMessage(4);
                    InboundSmsHandler.this.transitionTo(InboundSmsHandler.this.mDeliveringState);
                    return true;
                case 4:
                    return true;
                default:
                    return false;
            }
        }
    }

    private int addTrackerToRawTable(com.android.internal.telephony.InboundSmsTracker r27, boolean r28) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0145 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r26 = this;
        r8 = r27.getAddress();
        r2 = r27.getReferenceNumber();
        r20 = java.lang.Integer.toString(r2);
        r2 = r27.getMessageCount();
        r9 = java.lang.Integer.toString(r2);
        if (r28 == 0) goto L_0x014d;
    L_0x0016:
        r10 = 0;
        r24 = r27.getSequenceNumber();	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r21 = java.lang.Integer.toString(r24);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = r27.getTimestamp();	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r11 = java.lang.Long.toString(r2);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r15 = r27.getMessageBody();	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r0 = r26;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = r0.mResolver;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r3 = sRawUri;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r4 = PDU_PROJECTION;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r5 = "address=? AND reference_number=? AND count=? AND sequence=? AND date=? AND message_body=?";	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r6 = 6;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r6 = new java.lang.String[r6];	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r7 = 0;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r6[r7] = r8;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r7 = 1;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r6[r7] = r20;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r7 = 2;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r6[r7] = r9;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r7 = 3;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r6[r7] = r21;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r7 = 4;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r6[r7] = r11;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r7 = 5;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r6[r7] = r15;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r7 = 0;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r10 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = r10.moveToNext();	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        if (r2 == 0) goto L_0x00d3;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
    L_0x0056:
        r2 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2.<init>();	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r3 = "Discarding duplicate message segment, refNumber=";	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r0 = r20;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = r2.append(r0);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r3 = " seqNumber=";	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r0 = r21;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = r2.append(r0);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r3 = " count=";	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = r2.append(r9);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = r2.toString();	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r0 = r26;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r0.loge(r2);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = 0;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r18 = r10.getString(r2);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r19 = r27.getPdu();	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r17 = com.android.internal.util.HexDump.hexStringToByteArray(r18);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = r27.getPdu();	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r0 = r17;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = java.util.Arrays.equals(r0, r2);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        if (r2 != 0) goto L_0x00cc;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
    L_0x00a2:
        r2 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2.<init>();	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r3 = "Warning: dup message segment PDU of length ";	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r0 = r19;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r3 = r0.length;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r3 = " is different from existing PDU of length ";	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r0 = r17;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r3 = r0.length;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = r2.toString();	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r0 = r26;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r0.loge(r2);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
    L_0x00cc:
        r2 = 5;
        if (r10 == 0) goto L_0x00d2;
    L_0x00cf:
        r10.close();
    L_0x00d2:
        return r2;
    L_0x00d3:
        if (r10 == 0) goto L_0x00d8;
    L_0x00d5:
        r10.close();
    L_0x00d8:
        r25 = r27.getContentValues();
        r2 = "sub_id";
        r0 = r26;
        r3 = r0.mPhone;
        r3 = r3.getSubId();
        r3 = java.lang.Integer.valueOf(r3);
        r0 = r25;
        r0.put(r2, r3);
        r0 = r26;
        r2 = r0.mResolver;
        r3 = sRawUri;
        r0 = r25;
        r16 = r2.insert(r3, r0);
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "URI of new row -> ";
        r2 = r2.append(r3);
        r0 = r16;
        r2 = r2.append(r0);
        r2 = r2.toString();
        r0 = r26;
        r0.log(r2);
        r22 = android.content.ContentUris.parseId(r16);	 Catch:{ Exception -> 0x016b }
        r2 = r27.getMessageCount();	 Catch:{ Exception -> 0x016b }
        r3 = 1;	 Catch:{ Exception -> 0x016b }
        if (r2 != r3) goto L_0x0156;	 Catch:{ Exception -> 0x016b }
    L_0x0122:
        r2 = "_id=?";	 Catch:{ Exception -> 0x016b }
        r3 = 1;	 Catch:{ Exception -> 0x016b }
        r3 = new java.lang.String[r3];	 Catch:{ Exception -> 0x016b }
        r4 = java.lang.Long.toString(r22);	 Catch:{ Exception -> 0x016b }
        r5 = 0;	 Catch:{ Exception -> 0x016b }
        r3[r5] = r4;	 Catch:{ Exception -> 0x016b }
        r0 = r27;	 Catch:{ Exception -> 0x016b }
        r0.setDeleteWhere(r2, r3);	 Catch:{ Exception -> 0x016b }
    L_0x0134:
        r2 = 1;
        return r2;
    L_0x0136:
        r13 = move-exception;
        r2 = "Can't access SMS database";	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r0 = r26;	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r0.loge(r2, r13);	 Catch:{ SQLException -> 0x0136, all -> 0x0146 }
        r2 = 2;
        if (r10 == 0) goto L_0x0145;
    L_0x0142:
        r10.close();
    L_0x0145:
        return r2;
    L_0x0146:
        r2 = move-exception;
        if (r10 == 0) goto L_0x014c;
    L_0x0149:
        r10.close();
    L_0x014c:
        throw r2;
    L_0x014d:
        r2 = "Skipped message de-duping logic";
        r0 = r26;
        r0.logd(r2);
        goto L_0x00d8;
    L_0x0156:
        r2 = 3;
        r12 = new java.lang.String[r2];	 Catch:{ Exception -> 0x016b }
        r2 = 0;	 Catch:{ Exception -> 0x016b }
        r12[r2] = r8;	 Catch:{ Exception -> 0x016b }
        r2 = 1;	 Catch:{ Exception -> 0x016b }
        r12[r2] = r20;	 Catch:{ Exception -> 0x016b }
        r2 = 2;	 Catch:{ Exception -> 0x016b }
        r12[r2] = r9;	 Catch:{ Exception -> 0x016b }
        r2 = "address=? AND reference_number=? AND count=? AND deleted=0";	 Catch:{ Exception -> 0x016b }
        r0 = r27;	 Catch:{ Exception -> 0x016b }
        r0.setDeleteWhere(r2, r12);	 Catch:{ Exception -> 0x016b }
        goto L_0x0134;
    L_0x016b:
        r14 = move-exception;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "error parsing URI for new row: ";
        r2 = r2.append(r3);
        r0 = r16;
        r2 = r2.append(r0);
        r2 = r2.toString();
        r0 = r26;
        r0.loge(r2, r14);
        r2 = 2;
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.addTrackerToRawTable(com.android.internal.telephony.InboundSmsTracker, boolean):int");
    }

    private boolean processMessagePart(com.android.internal.telephony.InboundSmsTracker r43) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x013f in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r42 = this;
        r34 = r43.getMessageCount();
        r23 = r43.getDestPort();
        r4 = 1;
        r0 = r34;
        if (r0 != r4) goto L_0x0050;
    L_0x000d:
        r4 = 1;
        r0 = new byte[r4][];
        r22 = r0;
        r4 = r43.getPdu();
        r5 = 0;
        r22[r5] = r4;
    L_0x0019:
        r38 = java.util.Arrays.asList(r22);
        r4 = r38.size();
        if (r4 == 0) goto L_0x002c;
    L_0x0023:
        r4 = 0;
        r0 = r38;
        r4 = r0.contains(r4);
        if (r4 == 0) goto L_0x014c;
    L_0x002c:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "processMessagePart: returning false due to ";
        r5 = r4.append(r5);
        r4 = r38.size();
        if (r4 != 0) goto L_0x0147;
    L_0x003e:
        r4 = "pduList.size() == 0";
    L_0x0041:
        r4 = r5.append(r4);
        r4 = r4.toString();
        r0 = r42;
        r0.loge(r4);
        r4 = 0;
        return r4;
    L_0x0050:
        r42.scanAndDeleteOlderPartialMessages(r43);
        r30 = 0;
        r27 = r43.getAddress();	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = r43.getReferenceNumber();	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r40 = java.lang.Integer.toString(r4);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = r43.getMessageCount();	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r29 = java.lang.Integer.toString(r4);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = 3;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r8 = new java.lang.String[r4];	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = 0;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r8[r4] = r27;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = 1;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r8[r4] = r40;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = 2;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r8[r4] = r29;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r0 = r42;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = r0.mResolver;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r5 = sRawUri;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r6 = PDU_SEQUENCE_PORT_PROJECTION;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r7 = "address=? AND reference_number=? AND count=? AND deleted=0";	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r9 = 0;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r30 = r4.query(r5, r6, r7, r8, r9);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r31 = r30.getCount();	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4.<init>();	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r5 = "processMessagePart, cursorCount: ";	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r0 = r31;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = r4.append(r0);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r5 = ", ref|seq/count(";	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r5 = r43.getReferenceNumber();	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r5 = "|";	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r5 = r43.getSequenceNumber();	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r5 = "/";	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r0 = r34;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = r4.append(r0);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r5 = ")";	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = r4.append(r5);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = r4.toString();	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r0 = r42;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r0.log(r4);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r0 = r31;
        r1 = r34;
        if (r0 >= r1) goto L_0x00e3;
    L_0x00dc:
        r4 = 0;
        if (r30 == 0) goto L_0x00e2;
    L_0x00df:
        r30.close();
    L_0x00e2:
        return r4;
    L_0x00e3:
        r0 = r34;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r0 = new byte[r0][];	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r22 = r0;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
    L_0x00e9:
        r4 = r30.moveToNext();	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        if (r4 == 0) goto L_0x0127;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
    L_0x00ef:
        r4 = 1;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r0 = r30;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = r0.getInt(r4);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r5 = r43.getIndexOffset();	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r33 = r4 - r5;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = 0;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r0 = r30;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = r0.getString(r4);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = com.android.internal.util.HexDump.hexStringToByteArray(r4);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r22[r33] = r4;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        if (r33 != 0) goto L_0x00e9;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
    L_0x010b:
        r4 = 2;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r0 = r30;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = r0.isNull(r4);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        if (r4 != 0) goto L_0x00e9;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
    L_0x0114:
        r4 = 2;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r0 = r30;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r39 = r0.getInt(r4);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r39 = com.android.internal.telephony.InboundSmsTracker.getRealDestPort(r39);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = -1;
        r0 = r39;
        if (r0 == r4) goto L_0x00e9;
    L_0x0124:
        r23 = r39;
        goto L_0x00e9;
    L_0x0127:
        if (r30 == 0) goto L_0x0019;
    L_0x0129:
        r30.close();
        goto L_0x0019;
    L_0x012e:
        r32 = move-exception;
        r4 = "Can't access multipart SMS database";	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r0 = r42;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r1 = r32;	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r0.loge(r4, r1);	 Catch:{ SQLException -> 0x012e, all -> 0x0140 }
        r4 = 0;
        if (r30 == 0) goto L_0x013f;
    L_0x013c:
        r30.close();
    L_0x013f:
        return r4;
    L_0x0140:
        r4 = move-exception;
        if (r30 == 0) goto L_0x0146;
    L_0x0143:
        r30.close();
    L_0x0146:
        throw r4;
    L_0x0147:
        r4 = "pduList.contains(null)";
        goto L_0x0041;
    L_0x014c:
        r0 = r42;
        r4 = r0.mUserManager;
        r4 = r4.isUserUnlocked();
        if (r4 != 0) goto L_0x0163;
    L_0x0156:
        r0 = r42;
        r1 = r43;
        r2 = r22;
        r3 = r23;
        r4 = r0.processMessagePartWithUserLocked(r1, r2, r3);
        return r4;
    L_0x0163:
        r12 = new com.android.internal.telephony.InboundSmsHandler$SmsBroadcastReceiver;
        r0 = r42;
        r1 = r43;
        r12.<init>(r1);
        r4 = 2948; // 0xb84 float:4.131E-42 double:1.4565E-320;
        r0 = r23;
        if (r0 != r4) goto L_0x01fe;
    L_0x0172:
        r36 = new java.io.ByteArrayOutputStream;
        r36.<init>();
        r11 = 0;
        r4 = 0;
        r0 = r22;
        r5 = r0.length;
    L_0x017c:
        if (r4 >= r5) goto L_0x01b1;
    L_0x017e:
        r37 = r22[r4];
        r6 = r43.is3gpp2();
        if (r6 != 0) goto L_0x0199;
    L_0x0186:
        r6 = "3gpp";
        r0 = r37;
        r35 = android.telephony.SmsMessage.createFromPdu(r0, r6);
        if (r35 == 0) goto L_0x01a7;
    L_0x0191:
        r37 = r35.getUserData();
        r11 = r35.getOriginatingAddress();
    L_0x0199:
        r0 = r37;
        r6 = r0.length;
        r7 = 0;
        r0 = r36;
        r1 = r37;
        r0.write(r1, r7, r6);
        r4 = r4 + 1;
        goto L_0x017c;
    L_0x01a7:
        r4 = "processMessagePart: SmsMessage.createFromPdu returned null";
        r0 = r42;
        r0.loge(r4);
        r4 = 0;
        return r4;
    L_0x01b1:
        r0 = r42;
        r4 = r0.mWapPush;
        r0 = r43;
        r4.saveSmsTracker(r0);
        r0 = r42;
        r9 = r0.mWapPush;
        r10 = r36.toByteArray();
        r14 = r43.is3gpp2();
        r13 = r42;
        r41 = r9.dispatchWapPdu(r10, r11, r12, r13, r14);
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "dispatchWapPdu() returned ";
        r4 = r4.append(r5);
        r0 = r41;
        r4 = r4.append(r0);
        r4 = r4.toString();
        r0 = r42;
        r0.log(r4);
        r4 = -1;
        r0 = r41;
        if (r0 != r4) goto L_0x01ee;
    L_0x01ec:
        r4 = 1;
        return r4;
    L_0x01ee:
        r4 = r43.getDeleteWhere();
        r5 = r43.getDeleteWhereArgs();
        r6 = 2;
        r0 = r42;
        r0.deleteFromRawTable(r4, r5, r6);
        r4 = 0;
        return r4;
    L_0x01fe:
        r15 = new android.content.Intent;
        r15.<init>();
        r4 = "pdus";
        r0 = r22;
        r15.putExtra(r4, r0);
        r4 = "format";
        r5 = r43.getFormat();
        r15.putExtra(r4, r5);
        r0 = r42;
        r4 = r0.mPhone;
        r4 = r4.getPhoneId();
        android.telephony.SubscriptionManager.putPhoneIdAndSubIdExtra(r15, r4);
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "putPhoneIdAndSubIdExtra";
        r4 = r4.append(r5);
        r0 = r42;
        r5 = r0.mPhone;
        r5 = r5.getPhoneId();
        r4 = r4.append(r5);
        r4 = r4.toString();
        r0 = r42;
        r0.log(r4);
        r4 = -1;
        r0 = r23;
        if (r4 != r0) goto L_0x026b;
    L_0x0246:
        r0 = r42;
        r4 = r0.mContext;
        r5 = r43.getAddress();
        r4 = com.android.internal.telephony.BlockChecker.isBlocked(r4, r5);
        if (r4 == 0) goto L_0x026b;
    L_0x0254:
        r4 = com.android.internal.telephony.HwTelephonyFactory.getHwInnerSmsManager();
        r4.sendGoogleSmsBlockedRecord(r15);
        r4 = r43.getDeleteWhere();
        r5 = r43.getDeleteWhereArgs();
        r6 = 2;
        r0 = r42;
        r0.deleteFromRawTable(r4, r5, r6);
        r4 = 0;
        return r4;
    L_0x026b:
        r4 = -1;
        r0 = r23;
        if (r4 != r0) goto L_0x02b0;
    L_0x0270:
        r13 = com.android.internal.telephony.HwTelephonyFactory.getHwInnerSmsManager();
        r0 = r42;
        r14 = r0.mContext;
        r17 = r43.getDeleteWhere();
        r18 = r43.getDeleteWhereArgs();
        r19 = 0;
        r16 = r42;
        r4 = r13.newSmsShouldBeIntercepted(r14, r15, r16, r17, r18, r19);
        if (r4 == 0) goto L_0x02b0;
    L_0x028a:
        r17 = new android.content.Intent;
        r0 = r17;
        r0.<init>(r15);
        r4 = "android.provider.Telephony.INTERCEPTION_SMS_RECEIVED";
        r0 = r17;
        r0.setAction(r4);
        r4 = 0;
        r0 = r42;
        r20 = r0.handleSmsWhitelisting(r4);
        r18 = "huawei.permission.RECEIVE_SMS_INTERCEPTION";
        r22 = android.os.UserHandle.ALL;
        r19 = 16;
        r21 = 0;
        r16 = r42;
        r16.dispatchIntent(r17, r18, r19, r20, r21, r22);
        r4 = 1;
        return r4;
    L_0x02b0:
        r26 = 1;
        r21 = r42;
        r24 = r43;
        r25 = r12;
        r28 = r21.filterSmsWithCarrierOrSystemApp(r22, r23, r24, r25, r26);
        if (r28 != 0) goto L_0x02cb;
    L_0x02be:
        r4 = r43.getFormat();
        r0 = r42;
        r1 = r22;
        r2 = r23;
        r0.dispatchSmsDeliveryIntent(r1, r4, r2, r12);
    L_0x02cb:
        r4 = 1;
        return r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.InboundSmsHandler.processMessagePart(com.android.internal.telephony.InboundSmsTracker):boolean");
    }

    protected abstract void acknowledgeLastIncomingSms(boolean z, int i, Message message);

    protected abstract int dispatchMessageRadioSpecific(SmsMessageBase smsMessageBase);

    protected abstract boolean is3gpp2();

    protected InboundSmsHandler(String name, Context context, SmsStorageMonitor storageMonitor, Phone phone, CellBroadcastHandler cellBroadcastHandler) {
        boolean z = false;
        super(name);
        this.mContext = context;
        this.mStorageMonitor = storageMonitor;
        this.mPhone = phone;
        this.mCellBroadcastHandler = cellBroadcastHandler;
        this.mResolver = context.getContentResolver();
        this.mWapPush = HwTelephonyFactory.getHwInnerSmsManager().createHwWapPushOverSms(context);
        HwTelephonyFactory.getHwInnerSmsManager().createSmsInterceptionService(context);
        if (!TelephonyManager.from(this.mContext).getSmsReceiveCapableForPhone(this.mPhone.getPhoneId(), this.mContext.getResources().getBoolean(17956959))) {
            z = true;
        }
        this.mSmsReceiveDisabled = z;
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, name);
        this.mWakeLock.acquire();
        this.mUserManager = (UserManager) this.mContext.getSystemService(Carriers.USER);
        this.mDeviceIdleController = TelephonyComponentFactory.getInstance().getIDeviceIdleController();
        addState(this.mDefaultState);
        addState(this.mStartupState, this.mDefaultState);
        addState(this.mIdleState, this.mDefaultState);
        addState(this.mDeliveringState, this.mDefaultState);
        addState(this.mWaitingState, this.mDeliveringState);
        setInitialState(this.mStartupState);
        addInboxInsertObserver(this.mContext);
        log("created InboundSmsHandler");
    }

    public void dispose() {
        quit();
    }

    public void updatePhoneObject(Phone phone) {
        sendMessage(7, phone);
    }

    protected void onQuitting() {
        this.mWapPush.dispose();
        while (this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    public Phone getPhone() {
        return this.mPhone;
    }

    private void handleNewSms(AsyncResult ar) {
        if (ar.exception != null) {
            loge("Exception processing incoming SMS: " + ar.exception);
            return;
        }
        int result;
        try {
            SmsMessage sms = ar.result;
            result = dispatchMessage(sms.mWrappedSmsMessage);
            if (sms.mWrappedSmsMessage.blacklistFlag) {
                result = -1;
                HwRadarUtils.report(this.mContext, HwRadarUtils.ERR_SMS_RECEIVE, "receive a blacklist sms, modem has acked it, fw need't reply" + this.defaultSmsApplicationName, 0);
                log("receive a blacklist sms, modem has acked it, fw need't reply");
            }
        } catch (RuntimeException ex) {
            loge("Exception dispatching message", ex);
            result = 2;
        }
        if (result != -1) {
            boolean handled = result == 1;
            if (!handled) {
                Jlog.d(50, "JL_DISPATCH_SMS_FAILED");
            }
            notifyAndAcknowledgeLastIncomingSms(handled, result, null);
        }
    }

    private void handleInjectSms(AsyncResult ar) {
        int result;
        PendingIntent pendingIntent = null;
        try {
            pendingIntent = (PendingIntent) ar.userObj;
            SmsMessage sms = ar.result;
            if (sms == null) {
                result = 2;
            } else {
                result = dispatchMessage(sms.mWrappedSmsMessage);
            }
        } catch (RuntimeException ex) {
            loge("Exception dispatching message", ex);
            result = 2;
        }
        if (pendingIntent != null) {
            try {
                pendingIntent.send(result);
            } catch (CanceledException e) {
            }
        }
    }

    private int dispatchMessage(SmsMessageBase smsb) {
        if (smsb == null) {
            loge("dispatchSmsMessage: message is null");
            return 2;
        } else if (this.mSmsReceiveDisabled) {
            log("Received short message on device which doesn't support receiving SMS. Ignored.");
            return 1;
        } else if (hasSameSmsPdu(smsb.getPdu())) {
            log("receive a duplicated SMS and abandon it.");
            return 1;
        } else {
            boolean onlyCore = false;
            try {
                onlyCore = IPackageManager.Stub.asInterface(ServiceManager.getService(Intents.EXTRA_PACKAGE_NAME)).isOnlyCoreApps();
            } catch (RemoteException e) {
            }
            if (!onlyCore) {
                return dispatchMessageRadioSpecific(smsb);
            }
            log("Received a short message in encrypted state. Rejecting.");
            return 2;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean hasSameSmsPdu(byte[] pdu) {
        log("hasSameSmsPdu: check if there is a same pdu in mSmsList.");
        synchronized (this.mSmsList) {
            for (byte[] oldPdu : this.mSmsList) {
                if (Arrays.equals(pdu, oldPdu)) {
                    return true;
                }
            }
            this.mSmsList.add(pdu);
            log("hasSameSmsPdu: mSmsList.size() = " + this.mSmsList.size());
            if (this.mSmsList.size() > 30) {
                log("hasSameSmsPdu: mSmsList.size() > MAX_SMS_LIST_DEFAULT");
                this.mSmsList.remove(0);
            }
        }
    }

    protected void onUpdatePhoneObject(Phone phone) {
        this.mPhone = phone;
        this.mStorageMonitor = this.mPhone.mSmsStorageMonitor;
        log("onUpdatePhoneObject: phone=" + this.mPhone.getClass().getSimpleName());
    }

    private void notifyAndAcknowledgeLastIncomingSms(boolean success, int result, Message response) {
        if (!success) {
            Intent intent = new Intent(Intents.SMS_REJECTED_ACTION);
            intent.putExtra("result", result);
            this.mContext.sendBroadcast(intent, "android.permission.RECEIVE_SMS");
        }
        acknowledgeLastIncomingSms(success, result, response);
    }

    protected int dispatchNormalMessage(SmsMessageBase sms) {
        InboundSmsTracker tracker;
        SmsHeader smsHeader = sms.getUserDataHeader();
        this.isClass0 = sms.getMessageClass() == MessageClass.CLASS_0;
        Jlog.d(48, "JL_DISPATCH_NORMAL_SMS");
        if (smsHeader == null || smsHeader.concatRef == null) {
            int destPort = -1;
            if (!(smsHeader == null || smsHeader.portAddrs == null)) {
                destPort = smsHeader.portAddrs.destPort;
                log("destination port: " + destPort);
            }
            tracker = TelephonyComponentFactory.getInstance().makeInboundSmsTracker(sms.getPdu(), sms.getTimestampMillis(), destPort, is3gpp2(), false, sms.getDisplayOriginatingAddress(), sms.getMessageBody());
        } else {
            ConcatRef concatRef = smsHeader.concatRef;
            PortAddrs portAddrs = smsHeader.portAddrs;
            tracker = TelephonyComponentFactory.getInstance().makeInboundSmsTracker(sms.getPdu(), sms.getTimestampMillis(), portAddrs != null ? portAddrs.destPort : -1, is3gpp2(), sms.getDisplayOriginatingAddress(), concatRef.refNumber, concatRef.seqNumber, concatRef.msgCount, false, sms.getMessageBody());
        }
        return addTrackerToRawTableAndSendMessage(tracker, tracker.getDestPort() == -1);
    }

    protected int addTrackerToRawTableAndSendMessage(InboundSmsTracker tracker, boolean deDup) {
        switch (addTrackerToRawTable(tracker, deDup)) {
            case 1:
                if (((PowerManager) this.mContext.getSystemService("power")).isScreenOn()) {
                    Jlog.d(50, "JL_DISPATCH_SMS_FAILED");
                } else {
                    Jlog.d(49, "JL_SEND_BROADCAST_SMS");
                }
                sendMessage(2, tracker);
                return 1;
            case 5:
                return 1;
            default:
                return 2;
        }
    }

    private void scanAndDeleteOlderPartialMessages(InboundSmsTracker tracker) {
        String address = tracker.getAddress();
        String refNumber = Integer.toString(tracker.getReferenceNumber());
        String count = Integer.toString(tracker.getMessageCount());
        int delCount = 0;
        try {
            delCount = this.mResolver.delete(sRawUri, new StringBuilder("date < " + (tracker.getTimestamp() - PARTIAL_SEGMENT_EXPIRE_TIME) + " AND " + SELECT_BY_REFERENCE).toString(), new String[]{address, refNumber, count});
        } catch (Exception e) {
            loge("scanAndDeleteOlderPartialMessages got exception ", e);
        }
        if (delCount > 0) {
            log("scanAndDeleteOlderPartialMessages: delete " + delCount + " raw sms older than " + PARTIAL_SEGMENT_EXPIRE_TIME + " days for " + tracker.getAddress());
        }
    }

    private boolean processMessagePartWithUserLocked(InboundSmsTracker tracker, byte[][] pdus, int destPort) {
        log("Credential-encrypted storage not available. Port: " + destPort);
        if (destPort == SmsHeader.PORT_WAP_PUSH) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (byte[] pdu : pdus) {
                byte[] pdu2;
                if (!tracker.is3gpp2()) {
                    SmsMessage msg = SmsMessage.createFromPdu(pdu2, SmsMessage.FORMAT_3GPP);
                    if (msg != null) {
                        pdu2 = msg.getUserData();
                    } else {
                        loge("processMessagePartWithUserLocked: SmsMessage.createFromPdu returned null");
                        return false;
                    }
                }
                output.write(pdu2, 0, pdu2.length);
            }
            if (this.mWapPush.isWapPushForMms(output.toByteArray(), this)) {
                showNewMessageNotification();
                return false;
            }
        }
        if (destPort != -1) {
            return false;
        }
        if (filterSmsWithCarrierOrSystemApp(pdus, destPort, tracker, null, false)) {
            return true;
        }
        showNewMessageNotification();
        return false;
    }

    private void showNewMessageNotification() {
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            log("Show new message notification.");
            ((NotificationManager) this.mContext.getSystemService("notification")).notify(NOTIFICATION_TAG, 1, new Builder(this.mContext).setSmallIcon(17301646).setAutoCancel(true).setVisibility(1).setDefaults(-1).setContentTitle(this.mContext.getString(17040846)).setContentText(this.mContext.getString(17040847)).setContentIntent(PendingIntent.getActivity(this.mContext, 1, Intent.makeMainSelectorActivity("android.intent.action.MAIN", "android.intent.category.APP_MESSAGING"), 0)).build());
        }
    }

    static void cancelNewMessageNotification(Context context) {
        ((NotificationManager) context.getSystemService("notification")).cancel(NOTIFICATION_TAG, 1);
    }

    private boolean filterSmsWithCarrierOrSystemApp(byte[][] pdus, int destPort, InboundSmsTracker tracker, SmsBroadcastReceiver resultReceiver, boolean userUnlocked) {
        List carrierPackages = null;
        UiccCard card = UiccController.getInstance().getUiccCard(this.mPhone.getPhoneId());
        if (card != null) {
            carrierPackages = card.getCarrierPackageNamesForIntent(this.mContext.getPackageManager(), new Intent("android.service.carrier.CarrierMessagingService"));
        } else {
            loge("UiccCard not initialized.");
        }
        List<String> systemPackages = getSystemAppForIntent(new Intent("android.service.carrier.CarrierMessagingService"));
        CarrierSmsFilter smsFilter;
        if (carrierPackages != null && carrierPackages.size() == 1) {
            log("Found carrier package.");
            smsFilter = new CarrierSmsFilter(pdus, destPort, tracker.getFormat(), resultReceiver);
            smsFilter.filterSms((String) carrierPackages.get(0), new CarrierSmsFilterCallback(smsFilter, userUnlocked));
            return true;
        } else if (systemPackages == null || systemPackages.size() != 1) {
            logv("Unable to find carrier package: " + carrierPackages + ", nor systemPackages: " + systemPackages);
            return false;
        } else {
            log("Found system package.");
            smsFilter = new CarrierSmsFilter(pdus, destPort, tracker.getFormat(), resultReceiver);
            smsFilter.filterSms((String) systemPackages.get(0), new CarrierSmsFilterCallback(smsFilter, userUnlocked));
            return true;
        }
    }

    private List<String> getSystemAppForIntent(Intent intent) {
        List<String> packages = new ArrayList();
        PackageManager packageManager = this.mContext.getPackageManager();
        String carrierFilterSmsPerm = "android.permission.CARRIER_FILTER_SMS";
        for (ResolveInfo info : packageManager.queryIntentServices(intent, 0)) {
            if (info.serviceInfo == null) {
                loge("Can't get service information from " + info);
            } else {
                String packageName = info.serviceInfo.packageName;
                if (packageManager.checkPermission(carrierFilterSmsPerm, packageName) == 0) {
                    packages.add(packageName);
                    log("getSystemAppForIntent: added package " + packageName);
                }
            }
        }
        return packages;
    }

    public void dispatchIntent(Intent intent, String permission, int appOp, Bundle opts, BroadcastReceiver resultReceiver, UserHandle user) {
        intent.addFlags(134217728);
        String action = intent.getAction();
        if (Intents.SMS_DELIVER_ACTION.equals(action) || Intents.SMS_RECEIVED_ACTION.equals(action) || Intents.WAP_PUSH_DELIVER_ACTION.equals(action) || Intents.WAP_PUSH_RECEIVED_ACTION.equals(action)) {
            intent.addFlags(268435456);
        }
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        if (user.equals(UserHandle.ALL)) {
            int[] users = null;
            try {
                users = ActivityManagerNative.getDefault().getRunningUserIds();
            } catch (RemoteException e) {
            }
            if (users == null) {
                users = new int[]{user.getIdentifier()};
            }
            for (int i = users.length - 1; i >= 0; i--) {
                UserHandle targetUser = new UserHandle(users[i]);
                if (users[i] != 0) {
                    if (!this.mUserManager.hasUserRestriction("no_sms", targetUser)) {
                        UserInfo info = this.mUserManager.getUserInfo(users[i]);
                        if (info != null) {
                            if (info.isManagedProfile()) {
                            }
                        }
                    }
                }
                this.mContext.sendOrderedBroadcastAsUser(intent, targetUser, permission, appOp, opts, users[i] == 0 ? resultReceiver : null, getHandler(), -1, null, null);
            }
            return;
        }
        this.mContext.sendOrderedBroadcastAsUser(intent, user, permission, appOp, opts, resultReceiver, getHandler(), -1, null, null);
        triggerInboxInsertDoneDetect(intent);
    }

    private void triggerInboxInsertDoneDetect(Intent intent) {
        if (Intents.SMS_DELIVER_ACTION.equals(intent.getAction()) && !this.isClass0) {
            ComponentName componentName = intent.getComponent();
            if (componentName != null) {
                this.defaultSmsApplicationName = componentName.getPackageName();
            }
            this.subIdForReceivedSms = intent.getIntExtra("subscription", SubscriptionManager.getDefaultSmsSubscriptionId());
            this.mAlreadyReceivedSms.getAndIncrement();
            getHandler().removeCallbacks(this.mUpdateCountRunner);
            getHandler().postDelayed(this.mUpdateCountRunner, 60000);
        }
    }

    private void addInboxInsertObserver(Context context) {
        context.getContentResolver().registerContentObserver(Uri.parse("content://sms/inbox-insert"), true, this.mInsertObserver);
    }

    private void reportSmsReceiveTimeout(int durationMillis) {
        if (!this.isAlreadyDurationTimeout && durationMillis >= SMS_BROADCAST_DURATION_TIMEOUT) {
            this.isAlreadyDurationTimeout = true;
            HwRadarUtils.report(this.mContext, HwRadarUtils.ERR_SMS_RECEIVE, "sms receive timeout:" + durationMillis + this.defaultSmsApplicationName, this.subIdForReceivedSms);
        }
    }

    private void deleteFromRawTable(String deleteWhere, String[] deleteWhereArgs, int deleteType) {
        int rows = this.mResolver.delete(deleteType == 1 ? sRawUriPermanentDelete : sRawUri, deleteWhere, deleteWhereArgs);
        if (rows == 0) {
            loge("No rows were deleted from raw table!");
        } else {
            log("Deleted " + rows + " rows from raw table.");
        }
    }

    private Bundle handleSmsWhitelisting(ComponentName target) {
        String pkgName;
        String reason;
        if (target != null) {
            pkgName = target.getPackageName();
            reason = "sms-app";
        } else {
            pkgName = this.mContext.getPackageName();
            reason = "sms-broadcast";
        }
        try {
            long duration = this.mDeviceIdleController.addPowerSaveTempWhitelistAppForSms(pkgName, 0, reason);
            BroadcastOptions bopts = BroadcastOptions.makeBasic();
            bopts.setTemporaryAppWhitelistDuration(duration);
            return bopts.toBundle();
        } catch (RemoteException e) {
            return null;
        }
    }

    private void dispatchSmsDeliveryIntent(byte[][] pdus, String format, int destPort, BroadcastReceiver resultReceiver) {
        Intent intent = new Intent();
        intent.putExtra("pdus", pdus);
        intent.putExtra(CellBroadcasts.MESSAGE_FORMAT, format);
        if (destPort == -1) {
            intent.setAction(Intents.SMS_DELIVER_ACTION);
            ComponentName componentName = SmsApplication.getDefaultSmsApplication(this.mContext, true);
            if (componentName != null) {
                intent.setComponent(componentName);
                log("Delivering SMS to: " + componentName.getPackageName() + " " + componentName.getClassName());
            } else {
                intent.setComponent(null);
            }
            if (SmsManager.getDefault().getAutoPersisting()) {
                Uri uri = writeInboxMessage(intent);
                if (uri != null) {
                    intent.putExtra("uri", uri.toString());
                }
            }
        } else {
            intent.setAction(Intents.DATA_SMS_RECEIVED_ACTION);
            intent.setData(Uri.parse("sms://localhost:" + destPort));
            intent.setComponent(null);
        }
        dispatchIntent(intent, "android.permission.RECEIVE_SMS", 16, handleSmsWhitelisting(intent.getComponent()), resultReceiver, UserHandle.SYSTEM);
    }

    static boolean isCurrentFormat3gpp2() {
        return 2 == TelephonyManager.getDefault().getCurrentPhoneType();
    }

    private boolean isSkipNotifyFlagSet(int callbackResult) {
        return (callbackResult & 2) > 0;
    }

    protected void log(String s) {
        Rlog.d(getName(), s);
    }

    protected void loge(String s) {
        Rlog.e(getName(), s);
    }

    protected void loge(String s, Throwable e) {
        Rlog.e(getName(), s, e);
    }

    private Uri writeInboxMessage(Intent intent) {
        SmsMessage[] messages = Intents.getMessagesFromIntent(intent);
        if (messages == null || messages.length < 1) {
            loge("Failed to parse SMS pdu");
            return null;
        }
        int i = 0;
        int length = messages.length;
        while (i < length) {
            try {
                messages[i].getDisplayMessageBody();
                i++;
            } catch (NullPointerException e) {
                loge("NPE inside SmsMessage");
                return null;
            }
        }
        ContentValues values = parseSmsMessage(messages);
        long identity = Binder.clearCallingIdentity();
        Uri insert;
        try {
            insert = this.mContext.getContentResolver().insert(Inbox.CONTENT_URI, values);
            return insert;
        } catch (Exception e2) {
            insert = "Failed to persist inbox message";
            loge(insert, e2);
            return null;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private static ContentValues parseSmsMessage(SmsMessage[] msgs) {
        int i = 0;
        SmsMessage sms = msgs[0];
        ContentValues values = new ContentValues();
        values.put("address", sms.getDisplayOriginatingAddress());
        values.put("body", buildMessageBodyFromPdus(msgs));
        values.put("date_sent", Long.valueOf(sms.getTimestampMillis()));
        values.put("date", Long.valueOf(System.currentTimeMillis()));
        values.put("protocol", Integer.valueOf(sms.getProtocolIdentifier()));
        values.put("seen", Integer.valueOf(0));
        values.put("read", Integer.valueOf(0));
        String subject = sms.getPseudoSubject();
        if (!TextUtils.isEmpty(subject)) {
            values.put(TextBasedSmsColumns.SUBJECT, subject);
        }
        String str = TextBasedSmsColumns.REPLY_PATH_PRESENT;
        if (sms.isReplyPathPresent()) {
            i = 1;
        }
        values.put(str, Integer.valueOf(i));
        values.put(TextBasedSmsColumns.SERVICE_CENTER, sms.getServiceCenterAddress());
        return values;
    }

    private static String buildMessageBodyFromPdus(SmsMessage[] msgs) {
        int i = 0;
        if (msgs.length == 1) {
            return replaceFormFeeds(msgs[0].getDisplayMessageBody());
        }
        StringBuilder body = new StringBuilder();
        int length = msgs.length;
        while (i < length) {
            body.append(msgs[i].getDisplayMessageBody());
            i++;
        }
        return replaceFormFeeds(body.toString());
    }

    private static String replaceFormFeeds(String s) {
        return s == null ? "" : s.replace('\f', '\n');
    }

    public WakeLock getWakeLock() {
        return this.mWakeLock;
    }

    public int getWakeLockTimeout() {
        return WAKELOCK_TIMEOUT;
    }
}
