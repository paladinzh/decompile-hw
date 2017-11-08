package com.android.mms.transaction;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Telephony.Mms.Outbox;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.PreferenceUtils;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.ReadRecInd;
import com.google.android.mms.pdu.SendReq;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import java.nio.charset.Charset;

public class MmsMessageSender implements MessageSender {
    private final Context mContext;
    private final long mMessageSize;
    private final Uri mMessageUri;

    public static class ReadRecContent {
        public String messageId;
        public int status;
        public int subscription;
        public String to;

        public ReadRecContent(String msgId, String to, int status, int subScription) {
            this.messageId = msgId;
            this.to = to;
            this.status = status;
            this.subscription = subScription;
        }
    }

    public boolean sendMessage(long r22) throws com.google.android.mms.MmsException {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x01d3 in list []
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
        r21 = this;
        r2 = "Mms_app";
        r3 = 2;
        r2 = com.huawei.cspcommon.MLog.isLoggable(r2, r3);
        if (r2 == 0) goto L_0x0028;
    L_0x000a:
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "sendMessage uri: ";
        r2 = r2.append(r3);
        r0 = r21;
        r3 = r0.mMessageUri;
        r2 = r2.append(r3);
        r2 = r2.toString();
        r3 = 0;
        r3 = new java.lang.Object[r3];
        com.android.mms.LogTag.debug(r2, r3);
    L_0x0028:
        r0 = r21;
        r2 = r0.mContext;
        r14 = com.google.android.mms.pdu.PduPersister.getPduPersister(r2);
        r0 = r21;
        r2 = r0.mMessageUri;
        r15 = r14.load(r2);
        r2 = r15.getMessageType();
        r3 = 128; // 0x80 float:1.794E-43 double:6.32E-322;
        if (r2 == r3) goto L_0x005e;
    L_0x0040:
        r2 = new com.google.android.mms.MmsException;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "Invalid message: ";
        r3 = r3.append(r4);
        r4 = r15.getMessageType();
        r3 = r3.append(r4);
        r3 = r3.toString();
        r2.<init>(r3);
        throw r2;
    L_0x005e:
        r16 = r15;
        r16 = (com.google.android.mms.pdu.SendReq) r16;
        r0 = r21;
        r1 = r16;
        r0.updatePreferencesHeaders(r1);
        r2 = "personal";
        r3 = java.nio.charset.Charset.defaultCharset();
        r2 = r2.getBytes(r3);
        r0 = r16;
        r0.setMessageClass(r2);
        r2 = java.lang.System.currentTimeMillis();
        r18 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r2 = r2 / r18;
        r0 = r16;
        r0.setDate(r2);
        r0 = r21;
        r2 = r0.mMessageSize;
        r0 = r16;
        r0.setMessageSize(r2);
        r0 = r21;
        r2 = r0.mMessageUri;
        r0 = r16;
        r14.updateHeaders(r2, r0);
        r0 = r21;
        r2 = r0.mMessageUri;
        r12 = android.content.ContentUris.parseId(r2);
        r0 = r21;
        r2 = r0.mMessageUri;
        r2 = r2.toString();
        r3 = android.provider.Telephony.Mms.Draft.CONTENT_URI;
        r3 = r3.toString();
        r2 = r2.startsWith(r3);
        if (r2 != 0) goto L_0x01db;
    L_0x00b4:
        r9 = 0;
        r0 = r21;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r2 = r0.mContext;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r0 = r21;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = r0.mContext;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = r3.getContentResolver();	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r4 = android.provider.Telephony.MmsSms.PendingMessages.CONTENT_URI;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r6 = "msg_id = ? ";	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r7 = 1;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r7 = new java.lang.String[r7];	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r8 = java.lang.Long.toString(r12);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r18 = 0;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r7[r18] = r8;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r5 = 0;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r8 = 0;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r9 = com.huawei.cspcommon.ex.SqliteWrapper.query(r2, r3, r4, r5, r6, r7, r8);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        if (r9 != 0) goto L_0x00e9;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
    L_0x00d9:
        r2 = "MmsMessageSender";	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = "sendMessage to query by messageId cursor is null !";	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        com.huawei.cspcommon.MLog.d(r2, r3);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r2 = 0;
        if (r9 == 0) goto L_0x00e8;
    L_0x00e5:
        r9.close();
    L_0x00e8:
        return r2;
    L_0x00e9:
        r17 = 0;
        r2 = r9.getCount();	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        if (r2 != 0) goto L_0x01a1;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
    L_0x00f1:
        r5 = new android.content.ContentValues;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r2 = 7;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r5.<init>(r2);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r2 = "msg_id";	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = java.lang.Long.valueOf(r12);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r5.put(r2, r3);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
    L_0x0101:
        r2 = "proto_type";	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = 1;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = java.lang.Integer.valueOf(r3);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r5.put(r2, r3);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r2 = "msg_type";	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = r15.getMessageType();	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = java.lang.Integer.valueOf(r3);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r5.put(r2, r3);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r2 = "err_type";	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = 0;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = java.lang.Integer.valueOf(r3);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r5.put(r2, r3);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r2 = "err_code";	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = 0;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = java.lang.Integer.valueOf(r3);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r5.put(r2, r3);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r2 = "retry_index";	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = 0;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = java.lang.Integer.valueOf(r3);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r5.put(r2, r3);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r2 = "due_time";	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = 0;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = java.lang.Integer.valueOf(r3);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r5.put(r2, r3);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        if (r17 == 0) goto L_0x01ab;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
    L_0x0148:
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r2.<init>();	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = "msg_id = ";	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r2 = r2.append(r12);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r6 = r2.toString();	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r0 = r21;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r2 = r0.mContext;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r0 = r21;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = r0.mContext;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = r3.getContentResolver();	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r4 = android.provider.Telephony.MmsSms.PendingMessages.CONTENT_URI;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r7 = 0;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        com.huawei.cspcommon.ex.SqliteWrapper.update(r2, r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
    L_0x016e:
        if (r9 == 0) goto L_0x0173;
    L_0x0170:
        r9.close();
    L_0x0173:
        r2 = java.lang.Long.valueOf(r12);
        r0 = r22;
        com.android.mms.util.SendingProgressTokenManager.put(r2, r0);
        r0 = r21;
        r2 = r0.mContext;
        r2 = com.android.mms.ui.PreferenceUtils.isCancelSendEnable(r2);
        if (r2 == 0) goto L_0x01e5;
    L_0x0186:
        r2 = com.huawei.mms.util.DelaySendManager.getInst();
        r0 = -r12;
        r18 = r0;
        r3 = "mms";
        r4 = 0;
        r0 = r18;
        r2.addDelayMsg(r0, r3, r4);
        r2 = "MmsMessageSender";
        r3 = "addDelayMsg";
        com.huawei.cspcommon.MLog.d(r2, r3);
        r2 = 1;
        return r2;
    L_0x01a1:
        r5 = new android.content.ContentValues;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r2 = 6;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r5.<init>(r2);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r17 = 1;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        goto L_0x0101;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
    L_0x01ab:
        r0 = r21;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r2 = r0.mContext;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r0 = r21;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = r0.mContext;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = r3.getContentResolver();	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r4 = android.provider.Telephony.MmsSms.PendingMessages.CONTENT_URI;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r11 = com.huawei.cspcommon.ex.SqliteWrapper.insert(r2, r3, r4, r5);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        if (r11 != 0) goto L_0x016e;	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
    L_0x01bf:
        r2 = "MmsMessageSender";	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r3 = "MmsMessageSender.sendMessage insert Error";	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        com.huawei.cspcommon.MLog.e(r2, r3);	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        goto L_0x016e;
    L_0x01c9:
        r10 = move-exception;
        r10.printStackTrace();	 Catch:{ Exception -> 0x01c9, all -> 0x01d4 }
        r2 = 0;
        if (r9 == 0) goto L_0x01d3;
    L_0x01d0:
        r9.close();
    L_0x01d3:
        return r2;
    L_0x01d4:
        r2 = move-exception;
        if (r9 == 0) goto L_0x01da;
    L_0x01d7:
        r9.close();
    L_0x01da:
        throw r2;
    L_0x01db:
        r0 = r21;
        r2 = r0.mMessageUri;
        r3 = android.provider.Telephony.Mms.Outbox.CONTENT_URI;
        r14.move(r2, r3);
        goto L_0x0173;
    L_0x01e5:
        r0 = r21;
        r2 = r0.mContext;
        com.android.mms.transaction.TransactionService.startMe(r2);
        r2 = 1;
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.transaction.MmsMessageSender.sendMessage(long):boolean");
    }

    public MmsMessageSender(Context context, Uri location, long messageSize) {
        this(context, location, messageSize, 0);
    }

    public MmsMessageSender(Context context, Uri location, long messageSize, int subId) {
        this.mContext = context;
        this.mMessageUri = location;
        this.mMessageSize = messageSize;
        if (this.mMessageUri == null) {
            throw new IllegalArgumentException("Null message URI.");
        }
    }

    private void updatePreferencesHeaders(SendReq sendReq) throws MmsException {
        int i;
        int i2 = 128;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        int expiry = MmsConfig.getMmsExpiry(this.mContext);
        if (expiry > 0) {
            sendReq.setExpiry((long) expiry);
        }
        sendReq.setPriority(prefs.getInt("pref_key_mms_priority", 129));
        if (prefs.getBoolean("pref_key_mms_delivery_reports", false)) {
            i = 128;
        } else {
            i = 129;
        }
        sendReq.setDeliveryReport(i);
        if (!prefs.getBoolean("pref_key_mms_read_reports", false)) {
            i2 = 129;
        }
        sendReq.setReadReport(i2);
    }

    public static void sendReadRec(Context context, String to, String messageId, int status) {
        sendReadRec(context, to, messageId, status, 0);
    }

    public static void sendReadRec(Context context, String to, String messageId, int status, int subScription) {
        try {
            GenericPdu readRec = new ReadRecInd(new EncodedStringValue("insert-address-token".getBytes(Charset.defaultCharset())), messageId.getBytes(Charset.defaultCharset()), 18, status, new EncodedStringValue[]{new EncodedStringValue(to)});
            MLog.v("MmsMessageSender", "send read-report for " + messageId);
            readRec.setDate(System.currentTimeMillis() / 1000);
            setTransactionSub(context, PduPersister.getPduPersister(context).persist(readRec, Outbox.CONTENT_URI, true, PreferenceUtils.getIsGroupMmsEnabled(context), null), subScription);
            TransactionService.startMe(context);
        } catch (Throwable e) {
            MLog.e("MmsMessageSender", "Invalide header value", e);
        } catch (Throwable e2) {
            MLog.e("MmsMessageSender", "Persist message failed", e2);
        } catch (Throwable e3) {
            MLog.e("MmsMessageSender", "sendReadRec Exception e:", e3);
        }
    }

    private static void setTransactionSub(Context context, Uri mmsUri, int subscription) {
        if (MessageUtils.isMultiSimEnabled() && mmsUri != null) {
            ContentValues values = new ContentValues(2);
            values.put("sub_id", Integer.valueOf(subscription));
            values.put("network_type", Integer.valueOf(MessageUtils.getNetworkType(subscription)));
            SqliteWrapper.update(context, context.getContentResolver(), mmsUri, values, null, null);
        }
    }
}
