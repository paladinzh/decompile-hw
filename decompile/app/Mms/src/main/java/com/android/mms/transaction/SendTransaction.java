package com.android.mms.transaction;

import android.content.Context;
import android.net.Uri;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.HwCustUpdateUserBehavior;

public class SendTransaction extends Transaction {
    private HwCustUpdateUserBehavior mHwCustUpdateUserBehavior = null;
    public final Uri mSendReqURI;

    public void run() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0428 in list []
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
        r32 = com.android.mms.util.RateController.getInstance();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r32.isLimitSurpassed();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r4 == 0) goto L_0x0010;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x000a:
        r4 = r32.isAllowedByUser();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r4 == 0) goto L_0x0127;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x0010:
        r4 = "Mms_TXN";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = 2;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = com.huawei.cspcommon.MLog.isLoggable(r4, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r4 == 0) goto L_0x0036;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x001a:
        r4 = "Mms_TXM_ST";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5.<init>();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = "send transaction launched: ";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.append(r6);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.append(r0);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.toString();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        com.huawei.cspcommon.MLog.v(r4, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x0036:
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r0.mContext;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r31 = com.google.android.mms.pdu.PduPersister.getPduPersister(r4);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r0.mSendReqURI;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r31;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r37 = r0.load(r4);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r37 = (com.google.android.mms.pdu.SendReq) r37;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = java.lang.System.currentTimeMillis();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r8 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r26 = r4 / r8;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r37;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r1 = r26;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0.setDate(r1);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r7 = new android.content.ContentValues;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = 1;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r7.<init>(r4);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = "date";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = java.lang.Long.valueOf(r26);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r7.put(r4, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r0.mContext;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r0.mContext;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.getContentResolver();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = r0.mSendReqURI;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r8 = 0;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r9 = 0;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        com.huawei.cspcommon.ex.SqliteWrapper.update(r4, r5, r6, r7, r8, r9);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r25 = com.android.mms.MmsConfig.getForbiddenSetFrom();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r25 != 0) goto L_0x00b8;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x0084:
        r28 = com.android.mms.ui.MessageUtils.getLocalNumber();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = "Mms_TXM_ST";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5.<init>();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = "[ForbiddenSetForm] run: send mms msg =";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.append(r6);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = r0.mId;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.append(r6);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.toString();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        com.huawei.cspcommon.MLog.d(r4, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = android.text.TextUtils.isEmpty(r28);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r4 != 0) goto L_0x00b8;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x00ac:
        r4 = new com.google.android.mms.pdu.EncodedStringValue;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r28;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4.<init>(r0);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r37;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0.setFrom(r4);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x00b8:
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r0.mSendReqURI;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r40 = android.content.ContentUris.parseId(r4);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = java.lang.Long.valueOf(r40);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = com.android.mms.util.SendingProgressTokenManager.get(r4);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = new com.google.android.mms.pdu.PduComposer;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r8 = r0.mContext;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r37;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6.<init>(r8, r0);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = r6.make();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r36 = r0.sendPdu(r4, r6);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r12 = r26 * r4;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r36 != 0) goto L_0x0152;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x00e3:
        r4 = "Mms_TXM_ST";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = "send transaction response is null, return";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        com.huawei.cspcommon.MLog.e(r4, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r0.mHwCustUpdateUserBehavior;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r4 == 0) goto L_0x0105;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x00f2:
        r14 = java.lang.System.currentTimeMillis();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r8 = r0.mHwCustUpdateUserBehavior;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r9 = r0.mContext;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r11 = "send transaction response is null, return";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r10 = 1;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r8.upLoadSendMesFail(r9, r10, r11, r12, r14);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x0105:
        r0 = r42;
        r4 = r0.mTransactionState;
        r4 = r4.getState();
        r5 = 1;
        if (r4 == r5) goto L_0x0123;
    L_0x0110:
        r0 = r42;
        r4 = r0.mTransactionState;
        r5 = 2;
        r4.setState(r5);
        r0 = r42;
        r4 = r0.mTransactionState;
        r0 = r42;
        r5 = r0.mSendReqURI;
        r4.setContentUri(r5);
    L_0x0123:
        r42.notifyObservers();
        return;
    L_0x0127:
        r4 = "Mms_TXM_ST";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = "Sending rate limit surpassed.";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        com.huawei.cspcommon.MLog.e(r4, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;
        r4 = r0.mTransactionState;
        r4 = r4.getState();
        r5 = 1;
        if (r4 == r5) goto L_0x014e;
    L_0x013b:
        r0 = r42;
        r4 = r0.mTransactionState;
        r5 = 2;
        r4.setState(r5);
        r0 = r42;
        r4 = r0.mTransactionState;
        r0 = r42;
        r5 = r0.mSendReqURI;
        r4.setContentUri(r5);
    L_0x014e:
        r42.notifyObservers();
        return;
    L_0x0152:
        r4 = java.lang.Long.valueOf(r40);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        com.android.mms.util.SendingProgressTokenManager.remove(r4);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = "Mms_TXN";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = 2;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = com.huawei.cspcommon.MLog.isLoggable(r4, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r4 == 0) goto L_0x019b;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x0163:
        r35 = new java.lang.String;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = java.nio.charset.Charset.defaultCharset();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r35;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r1 = r36;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0.<init>(r1, r4);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = "Mms_TXM_ST";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5.<init>();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = "[SendTransaction] run: send mms msg (";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.append(r6);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = r0.mId;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.append(r6);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = "), resp=";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.append(r6);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r35;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.append(r0);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.toString();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        com.huawei.cspcommon.MLog.d(r4, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x019b:
        r4 = new com.google.android.mms.pdu.PduParser;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = 0;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r36;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4.<init>(r0, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r16 = r4.parse();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r16 = (com.google.android.mms.pdu.SendConf) r16;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r16 != 0) goto L_0x01ef;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x01ab:
        r4 = "Mms_TXM_ST";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = "No M-Send.conf received. return";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        com.huawei.cspcommon.MLog.e(r4, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r0.mHwCustUpdateUserBehavior;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r4 == 0) goto L_0x01cd;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x01ba:
        r14 = java.lang.System.currentTimeMillis();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r8 = r0.mHwCustUpdateUserBehavior;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r9 = r0.mContext;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r11 = "No M-Send.conf received. return";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r10 = 1;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r8.upLoadSendMesFail(r9, r10, r11, r12, r14);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x01cd:
        r0 = r42;
        r4 = r0.mTransactionState;
        r4 = r4.getState();
        r5 = 1;
        if (r4 == r5) goto L_0x01eb;
    L_0x01d8:
        r0 = r42;
        r4 = r0.mTransactionState;
        r5 = 2;
        r4.setState(r5);
        r0 = r42;
        r4 = r0.mTransactionState;
        r0 = r42;
        r5 = r0.mSendReqURI;
        r4.setContentUri(r5);
    L_0x01eb:
        r42.notifyObservers();
        return;
    L_0x01ef:
        r33 = r37.getTransactionId();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r24 = r16.getTransactionId();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r33;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r1 = r24;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = java.util.Arrays.equals(r0, r1);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r4 != 0) goto L_0x02a9;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x0201:
        r4 = "Mms_TXM_ST";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5.<init>();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = "Inconsistent Transaction-ID: req=";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.append(r6);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = new java.lang.String;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r8 = java.nio.charset.Charset.defaultCharset();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r33;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6.<init>(r0, r8);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.append(r6);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = ", conf=";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.append(r6);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = new java.lang.String;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r8 = java.nio.charset.Charset.defaultCharset();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r24;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6.<init>(r0, r8);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.append(r6);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.toString();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        com.huawei.cspcommon.MLog.e(r4, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r0.mHwCustUpdateUserBehavior;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r4 == 0) goto L_0x0287;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x0242:
        r14 = java.lang.System.currentTimeMillis();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r8 = r0.mHwCustUpdateUserBehavior;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r9 = r0.mContext;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4.<init>();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = "Inconsistent Transaction-ID: req=";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r4.append(r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = new java.lang.String;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = java.nio.charset.Charset.defaultCharset();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r33;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5.<init>(r0, r6);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r4.append(r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = ", conf=";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r4.append(r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = new java.lang.String;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = java.nio.charset.Charset.defaultCharset();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r24;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5.<init>(r0, r6);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r4.append(r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r11 = r4.toString();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r10 = 1;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r8.upLoadSendMesFail(r9, r10, r11, r12, r14);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x0287:
        r0 = r42;
        r4 = r0.mTransactionState;
        r4 = r4.getState();
        r5 = 1;
        if (r4 == r5) goto L_0x02a5;
    L_0x0292:
        r0 = r42;
        r4 = r0.mTransactionState;
        r5 = 2;
        r4.setState(r5);
        r0 = r42;
        r4 = r0.mTransactionState;
        r0 = r42;
        r5 = r0.mSendReqURI;
        r4.setContentUri(r5);
    L_0x02a5:
        r42.notifyObservers();
        return;
    L_0x02a9:
        r7 = new android.content.ContentValues;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = 2;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r7.<init>(r4);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r34 = r16.getResponseStatus();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = "resp_st";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = java.lang.Integer.valueOf(r34);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r7.put(r4, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = 128; // 0x80 float:1.794E-43 double:6.32E-322;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r34;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r0 == r4) goto L_0x0342;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x02c3:
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r0.mContext;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r0.mContext;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.getContentResolver();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = r0.mSendReqURI;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r8 = 0;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r9 = 0;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        com.huawei.cspcommon.ex.SqliteWrapper.update(r4, r5, r6, r7, r8, r9);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = "Mms_TXM_ST";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5.<init>();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = "Server returned an error code: ";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.append(r6);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r34;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.append(r0);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.toString();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        com.huawei.cspcommon.MLog.e(r4, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r0.mHwCustUpdateUserBehavior;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r4 == 0) goto L_0x0320;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x02fa:
        r14 = java.lang.System.currentTimeMillis();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r8 = r0.mHwCustUpdateUserBehavior;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r9 = r0.mContext;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4.<init>();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = "Server returned an error code: ";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r4.append(r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r34;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r4.append(r0);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r11 = r4.toString();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r10 = 1;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r8.upLoadSendMesFail(r9, r10, r11, r12, r14);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x0320:
        r0 = r42;
        r4 = r0.mTransactionState;
        r4 = r4.getState();
        r5 = 1;
        if (r4 == r5) goto L_0x033e;
    L_0x032b:
        r0 = r42;
        r4 = r0.mTransactionState;
        r5 = 2;
        r4.setState(r5);
        r0 = r42;
        r4 = r0.mTransactionState;
        r0 = r42;
        r5 = r0.mSendReqURI;
        r4.setContentUri(r5);
    L_0x033e:
        r42.notifyObservers();
        return;
    L_0x0342:
        r30 = r16.getMessageId();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r29 = "";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r30 == 0) goto L_0x0353;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x034b:
        r4 = r16.getMessageId();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r29 = com.google.android.mms.pdu.PduPersister.toIsoString(r4);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x0353:
        r4 = "m_id";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r29;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r7.put(r4, r0);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r0.mContext;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r0.mContext;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.getContentResolver();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = r0.mSendReqURI;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r8 = 0;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r9 = 0;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        com.huawei.cspcommon.ex.SqliteWrapper.update(r4, r5, r6, r7, r8, r9);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r0.mSendReqURI;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = android.provider.Telephony.Mms.Sent.CONTENT_URI;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r31;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r39 = r0.move(r4, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r0.mTransactionState;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = 1;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4.setState(r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r0.mHwCustUpdateUserBehavior;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r4 == 0) goto L_0x03ae;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x038a:
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r0.mHwCustUpdateUserBehavior;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r0.mContext;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4.playSentSuccessTone(r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r0.mHwCustUpdateUserBehavior;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r17 = r0;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r0.mContext;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r18 = r0;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r20 = r26 * r4;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r22 = java.lang.System.currentTimeMillis();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r19 = 1;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r17.upLoadSendMesSucc(r18, r19, r20, r22);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x03ae:
        r0 = r42;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = r0.mTransactionState;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r39;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4.setContentUri(r0);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = "Mms_TXN";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = 2;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r4 = com.huawei.cspcommon.MLog.isLoggable(r4, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        if (r4 == 0) goto L_0x03dd;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x03c1:
        r4 = "Mms_TXM_ST";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5.<init>();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r6 = "MSG_APP_send transaction success messageId";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.append(r6);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r29;	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.append(r0);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = r5.toString();	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        com.huawei.cspcommon.MLog.v(r4, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
    L_0x03dd:
        r0 = r42;
        r4 = r0.mTransactionState;
        r4 = r4.getState();
        r5 = 1;
        if (r4 == r5) goto L_0x03fb;
    L_0x03e8:
        r0 = r42;
        r4 = r0.mTransactionState;
        r5 = 2;
        r4.setState(r5);
        r0 = r42;
        r4 = r0.mTransactionState;
        r0 = r42;
        r5 = r0.mSendReqURI;
        r4.setContentUri(r5);
    L_0x03fb:
        r42.notifyObservers();
    L_0x03fe:
        return;
    L_0x03ff:
        r38 = move-exception;
        r4 = "Mms_TXM_ST";	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r5 = com.huawei.cspcommon.MLog.getStackTraceString(r38);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        com.huawei.cspcommon.MLog.e(r4, r5);	 Catch:{ Throwable -> 0x03ff, all -> 0x042c }
        r0 = r42;
        r4 = r0.mTransactionState;
        r4 = r4.getState();
        r5 = 1;
        if (r4 == r5) goto L_0x0428;
    L_0x0415:
        r0 = r42;
        r4 = r0.mTransactionState;
        r5 = 2;
        r4.setState(r5);
        r0 = r42;
        r4 = r0.mTransactionState;
        r0 = r42;
        r5 = r0.mSendReqURI;
        r4.setContentUri(r5);
    L_0x0428:
        r42.notifyObservers();
        goto L_0x03fe;
    L_0x042c:
        r4 = move-exception;
        r0 = r42;
        r5 = r0.mTransactionState;
        r5 = r5.getState();
        r6 = 1;
        if (r5 == r6) goto L_0x044b;
    L_0x0438:
        r0 = r42;
        r5 = r0.mTransactionState;
        r6 = 2;
        r5.setState(r6);
        r0 = r42;
        r5 = r0.mTransactionState;
        r0 = r42;
        r6 = r0.mSendReqURI;
        r5.setContentUri(r6);
    L_0x044b:
        r42.notifyObservers();
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.transaction.SendTransaction.run():void");
    }

    public SendTransaction(Context context, int transId, TransactionSettings connectionSettings, String uri) {
        super(context, transId, connectionSettings);
        this.mSendReqURI = Uri.parse(uri);
        this.mId = uri;
        this.mSubId = Transaction.querySubscription(context, this.mSendReqURI);
        this.mHwCustUpdateUserBehavior = (HwCustUpdateUserBehavior) HwCustUtils.createObj(HwCustUpdateUserBehavior.class, new Object[0]);
        attach(RetryScheduler.getInstance(context));
    }

    public int getType() {
        return 2;
    }

    public Uri getUri() {
        return this.mSendReqURI;
    }
}
