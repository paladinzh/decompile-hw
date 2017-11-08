package com.android.mms.ui;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.provider.Telephony.Sms;
import com.android.mms.transaction.MmsMessageSender;
import com.android.mms.transaction.SmsMessageSender;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.ui.HwBaseActivity;

public class MessageComposeBackground extends HwBaseActivity {
    public static com.android.mms.ui.MessageItem getMessageItem(android.content.Context r21, long r22, java.lang.String r24, long r25) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00cc in list []
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
        if (r21 == 0) goto L_0x0008;
    L_0x0002:
        r2 = 1;
        r2 = (r22 > r2 ? 1 : (r22 == r2 ? 0 : -1));
        if (r2 >= 0) goto L_0x0040;
    L_0x0008:
        r2 = "CMABackground";
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "ERROR: getMessageItem faile. with context threadId type msgId ";
        r3 = r3.append(r4);
        r0 = r22;
        r3 = r3.append(r0);
        r4 = "-";
        r3 = r3.append(r4);
        r0 = r24;
        r3 = r3.append(r0);
        r4 = "-";
        r3 = r3.append(r4);
        r0 = r25;
        r3 = r3.append(r0);
        r3 = r3.toString();
        com.huawei.cspcommon.MLog.e(r2, r3);
        r2 = 0;
        return r2;
    L_0x0040:
        r2 = "sms";
        r0 = r24;
        r2 = r2.equals(r0);
        if (r2 != 0) goto L_0x0056;
    L_0x004b:
        r2 = "mms";
        r0 = r24;
        r2 = r2.equals(r0);
        if (r2 == 0) goto L_0x0008;
    L_0x0056:
        r2 = 1;
        r2 = (r25 > r2 ? 1 : (r25 == r2 ? 0 : -1));
        if (r2 < 0) goto L_0x0008;
    L_0x005c:
        r18 = 0;
        r19 = 0;
        r20 = 0;
        r2 = "(transport_type=\"%s\")";
        r3 = 1;
        r3 = new java.lang.Object[r3];
        r4 = 0;
        r3[r4] = r24;
        r5 = java.lang.String.format(r2, r3);
        r9 = 0;
        r10 = new com.android.mms.ui.MessageListAdapter$ColumnsMap;	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        r10.<init>();	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        r15 = r10.mColumnMsgId;	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        r2 = android.provider.Telephony.Threads.CONTENT_URI;	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        r0 = r22;	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        r3 = android.content.ContentUris.withAppendedId(r2, r0);	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        r4 = com.android.mms.ui.MessageListAdapter.PROJECTION;	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        r6 = 0;	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        r7 = 0;	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        r2 = r21;	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        r9 = com.huawei.cspcommon.ex.SqliteWrapper.query(r2, r3, r4, r5, r6, r7);	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        if (r9 == 0) goto L_0x00aa;	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
    L_0x008b:
        r2 = r9.moveToFirst();	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        if (r2 == 0) goto L_0x00aa;	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
    L_0x0091:
        r16 = r9.getLong(r15);	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        r2 = (r16 > r25 ? 1 : (r16 == r25 ? 0 : -1));
        if (r2 != 0) goto L_0x00b8;
    L_0x0099:
        r6 = new com.android.mms.ui.MessageItem;	 Catch:{ MmsException -> 0x00b1 }
        r11 = 0;	 Catch:{ MmsException -> 0x00b1 }
        r12 = 2;	 Catch:{ MmsException -> 0x00b1 }
        r7 = r21;	 Catch:{ MmsException -> 0x00b1 }
        r8 = r24;	 Catch:{ MmsException -> 0x00b1 }
        r6.<init>(r7, r8, r9, r10, r11, r12);	 Catch:{ MmsException -> 0x00b1 }
    L_0x00a4:
        if (r9 == 0) goto L_0x00a9;
    L_0x00a6:
        r9.close();
    L_0x00a9:
        return r6;
    L_0x00aa:
        r2 = 0;
        if (r9 == 0) goto L_0x00b0;
    L_0x00ad:
        r9.close();
    L_0x00b0:
        return r2;
    L_0x00b1:
        r14 = move-exception;
        r14.printStackTrace();	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        r6 = r18;	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        goto L_0x00a4;	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
    L_0x00b8:
        r2 = r9.moveToNext();	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        if (r2 != 0) goto L_0x0091;
    L_0x00be:
        r6 = r18;
        goto L_0x00a4;
    L_0x00c1:
        r13 = move-exception;
        r0 = r21;	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        com.huawei.cspcommon.ex.SqliteWrapper.checkSQLiteException(r0, r13);	 Catch:{ SQLiteException -> 0x00c1, all -> 0x00cf }
        if (r9 == 0) goto L_0x00cc;
    L_0x00c9:
        r9.close();
    L_0x00cc:
        r6 = r18;
        goto L_0x00a9;
    L_0x00cf:
        r2 = move-exception;
        if (r9 == 0) goto L_0x00d5;
    L_0x00d2:
        r9.close();
    L_0x00d5:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.ui.MessageComposeBackground.getMessageItem(android.content.Context, long, java.lang.String, long):com.android.mms.ui.MessageItem");
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        long threadId = intent.getLongExtra("thread_id", 0);
        if (intent.hasExtra("ex_uri")) {
            Uri mUriEp = (Uri) intent.getExtra("ex_uri");
            if (mUriEp != null) {
                try {
                    MessageItem mMsgItem = getMessageItem(this, threadId, mUriEp.getAuthority(), (long) Integer.parseInt((String) mUriEp.getPathSegments().get(0)));
                    if (mMsgItem != null) {
                        resendMessage(this, mMsgItem);
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(String.format("onCreate. %s, mUri:%s", new Object[]{e.toString(), mUriEp}));
                }
            }
            finish();
            return;
        }
        finish();
    }

    protected static void resendMessage(Activity act, MessageItem msgItem) {
        if (msgItem.isMms()) {
            try {
                MLog.d("CMABackground", "Mms_TX CMA resend mms, uri=" + msgItem.mMessageUri.toString());
                ContentValues values = new ContentValues(3);
                values.put("err_type", Integer.valueOf(0));
                values.put("err_code", Integer.valueOf(0));
                values.put("retry_index", Integer.valueOf(0));
                SqliteWrapper.update(act, PendingMessages.CONTENT_URI, values, "msg_id=" + msgItem.mMsgId, null);
                new MmsMessageSender(act, msgItem.mMessageUri, (long) msgItem.mMessageSize).sendMessage(msgItem.mThreadId);
                return;
            } catch (Exception e) {
                MLog.e("CMABackground", "Failed to resend mms failed: " + msgItem.mMessageUri + ", threadId=" + msgItem.mThreadId, (Throwable) e);
                return;
            }
        }
        try {
            new SmsMessageSender(act, new String[]{msgItem.mAddress}, msgItem.mBody, msgItem.mThreadId, msgItem.mSubId).sendMessage(0);
        } catch (Exception e2) {
            MLog.e("CMABackground", "Mms_TX CMA Failed to resend sms failed: " + msgItem.mMessageUri + ", threadId=" + msgItem.mThreadId, (Throwable) e2);
        }
        SqliteWrapper.delete(act, act.getContentResolver(), ContentUris.withAppendedId(Sms.CONTENT_URI, msgItem.mMsgId), null, null);
    }
}
