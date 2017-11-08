package com.android.mms.transaction;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.SettingsEx.Systemex;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.widget.Toast;
import com.android.mms.HwCustMmsConfigImpl;
import com.android.mms.MmsApp;
import com.android.mms.util.HwCustUiUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import java.util.TimerTask;

public class HwCustSmsReceiverServiceImpl extends HwCustSmsReceiverService {
    private static final String BACKSLASH_SIGN_ONLY_STRING = "//";
    private static final String PATTERN_MULT_MESSAGE_FOR_SPRINT = "[\\(|\\{\\[]?\\s*\\d+\\s*(\\/|of|OF)\\s*\\d+\\s*[\\)|\\}\\]]?[\\s\\S]*";
    private static final String PATTERN_PREFIX_BLANK_FOR_SPRINT = "[\\(|\\{\\[]";
    private static final String PATTERN_PREFIX_MULT_MESSAGE_FOR_SPRINT = "[\\(|\\{\\[]?\\s*\\d+\\s*(\\/|of|OF)\\s*\\d+\\s*[\\)|\\}\\]]?";
    private static final String PATTERN_SPLITE_FOR_SPRINT = "(\\/|of|OF)";
    private static final String PATTERN_SURFIX_BLANK_FOR_SPRINT = "[\\)|\\}\\]]";
    private static final int SMS_ERROR_CAUSE_97 = 97;
    private static final String TAG = "HwCustSmsReceiverServiceImpl";

    /* renamed from: com.android.mms.transaction.HwCustSmsReceiverServiceImpl$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ Context val$context;

        AnonymousClass1(Context val$context) {
            this.val$context = val$context;
        }

        public void run() {
            HwCustSmsReceiverServiceImpl.this.showMessageFailedToast(this.val$context, this.val$context.getString(R.string.sms_error_cause_97), 1);
        }
    }

    /* renamed from: com.android.mms.transaction.HwCustSmsReceiverServiceImpl$2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ Context val$context;
        final /* synthetic */ String val$from;

        AnonymousClass2(Context val$context, String val$from) {
            this.val$context = val$context;
            this.val$from = val$from;
        }

        public void run() {
            HwCustSmsReceiverServiceImpl.this.showMessageFailedToast(this.val$context, this.val$context.getString(R.string.sms_error_cause_general, new Object[]{this.val$from}), 0);
        }
    }

    public static class SplitedSmsTask extends TimerTask {
        private String mAddress = null;
        private Context mContext = null;
        private ContentResolver mResolver = null;
        private int mTotalPage;

        public void run() {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00b3 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r20 = this;
            r14 = 0;
            r2 = "content://splited_sms";	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r4 = android.net.Uri.parse(r2);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r0 = r20;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r2 = r0.mContext;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r0 = r20;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r3 = r0.mResolver;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r6 = " address = ? and totalpage = ? ";	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r5 = 2;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r7 = new java.lang.String[r5];	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r0 = r20;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r5 = r0.mAddress;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r8 = 0;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r7[r8] = r5;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r0 = r20;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r5 = r0.mTotalPage;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r5 = java.lang.String.valueOf(r5);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r8 = 1;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r7[r8] = r5;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r8 = "date desc";	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r5 = 0;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r14 = com.huawei.cspcommon.ex.SqliteWrapper.query(r2, r3, r4, r5, r6, r7, r8);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            if (r14 == 0) goto L_0x0038;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
        L_0x0032:
            r2 = r14.getCount();	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            if (r2 != 0) goto L_0x003e;
        L_0x0038:
            if (r14 == 0) goto L_0x003d;
        L_0x003a:
            r14.close();
        L_0x003d:
            return;
        L_0x003e:
            r14.moveToFirst();	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r11 = r14.getColumnNames();	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r2 = -1;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r14.moveToPosition(r2);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
        L_0x0049:
            r2 = r14.moveToNext();	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            if (r2 == 0) goto L_0x00e9;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
        L_0x004f:
            r13 = new android.content.ContentValues;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r13.<init>();	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r2 = 0;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r5 = r11.length;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r3 = r2;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
        L_0x0057:
            if (r3 >= r5) goto L_0x00c3;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
        L_0x0059:
            r10 = r11[r3];	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r2 = "curpage";	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r2 = r10.equals(r2);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            if (r2 != 0) goto L_0x0085;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
        L_0x0064:
            r2 = "totalpage";	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r2 = r10.equals(r2);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            if (r2 != 0) goto L_0x0085;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
        L_0x006d:
            r2 = "_id";	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r2 = r10.equals(r2);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
        L_0x0074:
            if (r2 != 0) goto L_0x0081;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
        L_0x0076:
            r9 = r14.getColumnIndex(r10);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r12 = r14.getType(r9);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            switch(r12) {
                case 1: goto L_0x0087;
                case 2: goto L_0x0081;
                case 3: goto L_0x00b4;
                default: goto L_0x0081;
            };	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
        L_0x0081:
            r2 = r3 + 1;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r3 = r2;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            goto L_0x0057;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
        L_0x0085:
            r2 = 1;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            goto L_0x0074;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
        L_0x0087:
            r6 = r14.getLong(r9);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r2 = java.lang.Long.valueOf(r6);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r13.put(r10, r2);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            goto L_0x0081;
        L_0x0093:
            r15 = move-exception;
            r2 = "HwCustSmsReceiverServiceImpl";	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r3 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r3.<init>();	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r5 = "SplitedSmsTask query data error! ";	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r3 = r3.append(r5);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r3 = r3.append(r15);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r3 = r3.toString();	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            com.huawei.cspcommon.MLog.e(r2, r3);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            if (r14 == 0) goto L_0x00b3;
        L_0x00b0:
            r14.close();
        L_0x00b3:
            return;
        L_0x00b4:
            r2 = r14.getString(r9);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r13.put(r10, r2);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            goto L_0x0081;
        L_0x00bc:
            r2 = move-exception;
            if (r14 == 0) goto L_0x00c2;
        L_0x00bf:
            r14.close();
        L_0x00c2:
            throw r2;
        L_0x00c3:
            r0 = r20;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r2 = r0.mContext;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r0 = r20;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r3 = r0.mResolver;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r5 = android.provider.Telephony.Sms.Inbox.CONTENT_URI;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r16 = com.huawei.cspcommon.ex.SqliteWrapper.insert(r2, r3, r5, r13);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            if (r16 == 0) goto L_0x0049;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
        L_0x00d3:
            r0 = r20;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r2 = r0.mContext;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r0 = r16;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r18 = com.android.mms.transaction.MessagingNotification.getSmsThreadId(r2, r0);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r0 = r20;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r2 = r0.mContext;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r3 = 0;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r0 = r18;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            com.android.mms.transaction.MessagingNotification.blockingUpdateNewMessageIndicator(r2, r0, r3);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            goto L_0x0049;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
        L_0x00e9:
            r0 = r20;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r2 = r0.mContext;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r0 = r20;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r3 = r0.mResolver;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r5 = "address = ?  and totalpage=? ";	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r6 = 2;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r6 = new java.lang.String[r6];	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r0 = r20;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r7 = r0.mAddress;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r8 = 0;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r6[r8] = r7;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r0 = r20;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r7 = r0.mTotalPage;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r7 = java.lang.String.valueOf(r7);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r8 = 1;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            r6[r8] = r7;	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            com.huawei.cspcommon.ex.SqliteWrapper.delete(r2, r3, r4, r5, r6);	 Catch:{ Exception -> 0x0093, all -> 0x00bc }
            if (r14 == 0) goto L_0x00b3;
        L_0x010e:
            r14.close();
            goto L_0x00b3;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.mms.transaction.HwCustSmsReceiverServiceImpl.SplitedSmsTask.run():void");
        }

        public SplitedSmsTask(Context context, ContentResolver resolver, String address, int totalPage) {
            this.mContext = context;
            this.mResolver = resolver;
            this.mAddress = address;
            this.mTotalPage = totalPage;
        }
    }

    public void handleMessageFailedToSend(android.content.Context r14, android.net.Uri r15, int r16, android.os.Handler r17) {
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
        r13 = this;
        r12 = com.android.mms.HwCustMmsConfigImpl.showToastWhenSendError();
        if (r12 == 0) goto L_0x0032;
    L_0x0006:
        r1 = "HwCustSmsReceiverServiceImpl";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "handleMessageFailedToSend error code = ";
        r2 = r2.append(r3);
        r0 = r16;
        r2 = r2.append(r0);
        r2 = r2.toString();
        com.huawei.cspcommon.MLog.d(r1, r2);
        r1 = 97;
        r0 = r16;
        if (r1 != r0) goto L_0x0033;
    L_0x0028:
        r1 = new com.android.mms.transaction.HwCustSmsReceiverServiceImpl$1;
        r1.<init>(r14);
        r0 = r17;
        r0.post(r1);
    L_0x0032:
        return;
    L_0x0033:
        r9 = 0;
        r2 = r14.getContentResolver();	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r1 = 1;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r4 = new java.lang.String[r1];	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r1 = "address";	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r3 = 0;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r4[r3] = r1;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r5 = 0;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r6 = 0;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r7 = 0;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r1 = r14;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r3 = r15;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r9 = com.huawei.cspcommon.ex.SqliteWrapper.query(r1, r2, r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        if (r9 == 0) goto L_0x0073;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
    L_0x004c:
        r1 = r9.getCount();	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r2 = 1;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        if (r1 != r2) goto L_0x0073;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
    L_0x0053:
        r1 = r9.moveToFirst();	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        if (r1 == 0) goto L_0x0073;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
    L_0x0059:
        r1 = 0;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r8 = r9.getString(r1);	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        if (r8 == 0) goto L_0x0079;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
    L_0x0060:
        r1 = 0;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r1 = com.android.mms.data.Contact.get(r8, r1);	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r11 = r1.getName();	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
    L_0x0069:
        r1 = new com.android.mms.transaction.HwCustSmsReceiverServiceImpl$2;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r1.<init>(r14, r11);	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r0 = r17;	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r0.post(r1);	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
    L_0x0073:
        if (r9 == 0) goto L_0x0032;
    L_0x0075:
        r9.close();
        goto L_0x0032;
    L_0x0079:
        r1 = 2131493607; // 0x7f0c02e7 float:1.8610699E38 double:1.0530977655E-314;
        r11 = r14.getString(r1);	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        goto L_0x0069;
    L_0x0081:
        r10 = move-exception;
        r1 = "HwCustSmsReceiverServiceImpl";	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        r2 = "handleMessageFailedToSend Caught an exception in showErrorCodeToast";	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        com.huawei.cspcommon.MLog.d(r1, r2);	 Catch:{ Exception -> 0x0081, all -> 0x0091 }
        if (r9 == 0) goto L_0x0032;
    L_0x008d:
        r9.close();
        goto L_0x0032;
    L_0x0091:
        r1 = move-exception;
        if (r9 == 0) goto L_0x0097;
    L_0x0094:
        r9.close();
    L_0x0097:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.transaction.HwCustSmsReceiverServiceImpl.handleMessageFailedToSend(android.content.Context, android.net.Uri, int, android.os.Handler):void");
    }

    public android.net.Uri hwCustStoreMessage(android.content.Context r30, android.content.ContentResolver r31, android.net.Uri r32, android.content.ContentValues r33) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0175 in list []
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
        r29 = this;
        r4 = r29.isNeedMergeMultMessage(r30);
        if (r4 == 0) goto L_0x01be;
    L_0x0006:
        r4 = "body";
        r0 = r33;
        r21 = r0.get(r4);
        r21 = (java.lang.String) r21;
        r0 = r29;
        r1 = r21;
        r4 = r0.isSlipedMessageForSprint(r1);
        if (r4 == 0) goto L_0x01be;
    L_0x001b:
        r16 = new android.content.ContentValues;
        r0 = r16;
        r1 = r33;
        r0.<init>(r1);
        r4 = "content://splited_sms";
        r6 = android.net.Uri.parse(r4);
        r4 = "[\\(|\\{\\[]?\\s*\\d+\\s*(\\/|of|OF)\\s*\\d+\\s*[\\)|\\}\\]]?";
        r22 = java.util.regex.Pattern.compile(r4);
        r0 = r22;
        r1 = r21;
        r19 = r0.matcher(r1);
        r23 = 0;
        r12 = 0;
        r26 = 0;
        r4 = r19.find();
        if (r4 == 0) goto L_0x0086;
    L_0x0045:
        r23 = r19.group();
        r4 = "[\\(|\\{\\[]";
        r5 = "";
        r0 = r23;
        r23 = r0.replaceFirst(r4, r5);
        r4 = "[\\)|\\}\\]]";
        r5 = "";
        r0 = r23;
        r23 = r0.replaceFirst(r4, r5);
        r4 = "(\\/|of|OF)";
        r0 = r23;
        r24 = r0.split(r4);
        r0 = r24;
        r4 = r0.length;
        r5 = 2;
        if (r4 != r5) goto L_0x0086;
    L_0x0070:
        r4 = 0;
        r4 = r24[r4];
        r4 = r4.trim();
        r12 = java.lang.Integer.parseInt(r4);
        r4 = 1;
        r4 = r24[r4];
        r4 = r4.trim();
        r26 = java.lang.Integer.parseInt(r4);
    L_0x0086:
        r4 = 100;
        r0 = r26;
        if (r0 <= r4) goto L_0x008e;
    L_0x008c:
        r4 = 0;
        return r4;
    L_0x008e:
        r4 = "curpage";
        r5 = java.lang.Integer.valueOf(r12);
        r0 = r16;
        r0.put(r4, r5);
        r4 = "totalpage";
        r5 = java.lang.Integer.valueOf(r26);
        r0 = r16;
        r0.put(r4, r5);
        r0 = r30;
        r1 = r31;
        r2 = r16;
        r17 = com.huawei.cspcommon.ex.SqliteWrapper.insert(r0, r1, r6, r2);
        r20 = new java.util.Timer;
        r20.<init>();
        r4 = "address";
        r0 = r33;
        r11 = r0.get(r4);
        r11 = (java.lang.String) r11;
        r27 = r26;
        r4 = new com.android.mms.transaction.HwCustSmsReceiverServiceImpl$SplitedSmsTask;
        r0 = r30;
        r1 = r31;
        r2 = r27;
        r4.<init>(r0, r1, r11, r2);
        r8 = 300000; // 0x493e0 float:4.2039E-40 double:1.482197E-318;
        r0 = r20;
        r0.schedule(r4, r8);
        r13 = 0;
        r8 = "address = ? and totalpage=?";	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r4 = 2;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r9 = new java.lang.String[r4];	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r4 = "address";	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r0 = r33;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r4 = (java.lang.String) r4;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r5 = 0;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r9[r5] = r4;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r4 = java.lang.String.valueOf(r26);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r5 = 1;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r9[r5] = r4;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r10 = "curpage asc";	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r7 = 0;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r4 = r30;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r5 = r31;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r13 = com.huawei.cspcommon.ex.SqliteWrapper.query(r4, r5, r6, r7, r8, r9, r10);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        if (r13 == 0) goto L_0x01b1;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
    L_0x00ff:
        r4 = r13.getCount();	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r0 = r26;
        if (r4 == r0) goto L_0x010d;
    L_0x0107:
        if (r13 == 0) goto L_0x010c;
    L_0x0109:
        r13.close();
    L_0x010c:
        return r17;
    L_0x010d:
        r4 = -1;
        r13.moveToPosition(r4);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r15 = 1;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r18 = 1;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
    L_0x0114:
        r4 = r13.moveToNext();	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        if (r4 == 0) goto L_0x0129;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
    L_0x011a:
        r4 = "curpage";	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r4 = r13.getColumnIndex(r4);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r4 = r13.getInt(r4);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        if (r4 == r15) goto L_0x0176;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
    L_0x0127:
        r18 = 0;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
    L_0x0129:
        if (r18 == 0) goto L_0x01b1;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
    L_0x012b:
        r4 = -1;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r13.moveToPosition(r4);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r25 = new java.lang.StringBuffer;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r25.<init>();	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
    L_0x0134:
        r4 = r13.moveToNext();	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        if (r4 == 0) goto L_0x0179;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
    L_0x013a:
        r4 = "body";	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r4 = r13.getColumnIndex(r4);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r4 = r13.getString(r4);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r5 = "[\\(|\\{\\[]?\\s*\\d+\\s*(\\/|of|OF)\\s*\\d+\\s*[\\)|\\}\\]]?";	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r7 = "";	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r4 = r4.replaceFirst(r5, r7);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r0 = r25;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r0.append(r4);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        goto L_0x0134;
    L_0x0155:
        r14 = move-exception;
        r4 = "HwCustSmsReceiverServiceImpl";	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r5.<init>();	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r7 = "hwCustStoreMessage insert data error! ";	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r5 = r5.append(r7);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r5 = r5.append(r14);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r5 = r5.toString();	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        com.huawei.cspcommon.MLog.e(r4, r5);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        if (r13 == 0) goto L_0x0175;
    L_0x0172:
        r13.close();
    L_0x0175:
        return r17;
    L_0x0176:
        r15 = r15 + 1;
        goto L_0x0114;
    L_0x0179:
        r4 = "body";	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r5 = r25.toString();	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r0 = r33;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r0.put(r4, r5);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r28 = com.huawei.cspcommon.ex.SqliteWrapper.insert(r30, r31, r32, r33);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r5 = "address = ?  and totalpage=? ";	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r4 = 2;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r7 = new java.lang.String[r4];	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r4 = "address";	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r0 = r33;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r4 = r0.get(r4);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r4 = (java.lang.String) r4;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r8 = 0;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r7[r8] = r4;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r4 = java.lang.String.valueOf(r26);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r8 = 1;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r7[r8] = r4;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r0 = r30;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        r1 = r31;	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        com.huawei.cspcommon.ex.SqliteWrapper.delete(r0, r1, r6, r5, r7);	 Catch:{ Exception -> 0x0155, all -> 0x01b7 }
        if (r13 == 0) goto L_0x01b0;
    L_0x01ad:
        r13.close();
    L_0x01b0:
        return r28;
    L_0x01b1:
        if (r13 == 0) goto L_0x0175;
    L_0x01b3:
        r13.close();
        goto L_0x0175;
    L_0x01b7:
        r4 = move-exception;
        if (r13 == 0) goto L_0x01bd;
    L_0x01ba:
        r13.close();
    L_0x01bd:
        throw r4;
    L_0x01be:
        r4 = 0;
        return r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.transaction.HwCustSmsReceiverServiceImpl.hwCustStoreMessage(android.content.Context, android.content.ContentResolver, android.net.Uri, android.content.ContentValues):android.net.Uri");
    }

    public boolean isDiscardSms(String messageBody) {
        if (!HwCustMmsConfigImpl.isDiscardSmsBackslash() || messageBody == null || !messageBody.startsWith(BACKSLASH_SIGN_ONLY_STRING)) {
            return false;
        }
        MLog.i(TAG, "isDiscardSms SMS is started with '//',it will be hidden to user");
        return true;
    }

    private void showMessageFailedToast(Context context, String text, int time) {
        Toast.makeText(context, text, time).show();
    }

    private boolean isNeedMergeMultMessage(Context context) {
        return "true".equals(Systemex.getString(context.getContentResolver(), "hw_need_temp_smstable"));
    }

    private boolean isSlipedMessageForSprint(String message) {
        if (TextUtils.isEmpty(message)) {
            return false;
        }
        return message.matches(PATTERN_MULT_MESSAGE_FOR_SPRINT);
    }

    public boolean isNeedShowMultiMessage(Uri insertedUri) {
        if (insertedUri != null) {
            return insertedUri.toString().startsWith("content://sms");
        }
        return false;
    }

    public boolean isDiscardSMSFrom3311(SmsMessage message) {
        if (HwCustMmsConfigImpl.isDiscardSms()) {
            boolean lMailboxAppEnabled = true;
            try {
                int lMailboxAppState = MmsApp.getApplication().getApplicationContext().getPackageManager().getApplicationEnabledSetting("de.telekom.mds.mbp");
                MLog.v(TAG, "de.telekom Mailbox app lMailboxAppState = " + lMailboxAppState);
                if (lMailboxAppState == 2 || lMailboxAppState == 3) {
                    lMailboxAppEnabled = false;
                }
            } catch (Exception e) {
                lMailboxAppEnabled = false;
                MLog.v(TAG, "de.telekom Mailbox app not installed");
            }
            if (lMailboxAppEnabled) {
                String addr = message.getDisplayOriginatingAddress();
                String messagebody = message.getMessageBody();
                boolean isBlockMessage = false;
                if (messagebody != null) {
                    isBlockMessage = messagebody.contains("Mobilbox Pro");
                }
                if ("3311".equals(addr) && r2) {
                    MLog.v(TAG, "de.telekom Mailbox app enabled and sms from MBP server will be discarded");
                    return true;
                }
            }
        }
        return false;
    }

    public long getReceivedTime(long aTimeNow, long aServerTime) {
        if (HwCustMmsConfigImpl.isEnableLocalTime() || HwCustUiUtils.isLocalTimeRight(aTimeNow, aServerTime)) {
            return aTimeNow;
        }
        return aServerTime;
    }
}
