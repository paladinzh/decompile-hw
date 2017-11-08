package com.android.mms.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms.Conversations;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.mms.MmsConfig;
import com.android.mms.data.Conversation;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;

public abstract class Recycler {
    private static MmsRecycler sMmsRecycler;
    private static SmsRecycler sSmsRecycler;

    public static class MmsRecycler extends Recycler {
        private static final String[] ALL_MMS_THREADS_PROJECTION = new String[]{"thread_id", "count(*) as msg_count"};
        private static final String[] MMS_MESSAGE_PROJECTION = new String[]{"_id", "thread_id", "date"};

        public int getMessageLimit(Context context) {
            return getValidMessageLimit(PreferenceManager.getDefaultSharedPreferences(context).getInt("MaxMmsMessagesPerThread", MmsConfig.getDefaultMMSMessagesPerThread()));
        }

        public void setMessageLimit(Context context, int limit) {
            Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editPrefs.putInt("MaxMmsMessagesPerThread", limit);
            editPrefs.apply();
        }

        protected long getThreadId(Cursor cursor) {
            return cursor.getLong(0);
        }

        protected Cursor getAllThreads(Context context) {
            return SqliteWrapper.query(context, context.getContentResolver(), Uri.withAppendedPath(Mms.CONTENT_URI, "threads"), ALL_MMS_THREADS_PROJECTION, null, null, "date DESC");
        }

        public void deleteOldMessagesInSameThreadAsMessage(Context context, Uri uri) {
            Throwable th;
            if (Recycler.isAutoDeleteEnabled(context)) {
                Cursor cursor = null;
                long latestDate;
                try {
                    Context context2 = context;
                    cursor = SqliteWrapper.query(context2, context.getContentResolver(), Mms.CONTENT_URI, MMS_MESSAGE_PROJECTION, "thread_id in (select thread_id from pdu where _id=" + uri.getLastPathSegment() + ") AND locked=0" + " AND (m_type = 128 OR m_type = 132 OR m_type = 130)", null, "date DESC");
                    if (cursor == null) {
                        MLog.e("Recycler", "MMS: deleteOldMessagesInSameThreadAsMessage got back null cursor");
                        if (cursor != null) {
                            cursor.close();
                        }
                        return;
                    }
                    int count = cursor.getCount();
                    int keep = getMessageLimit(context);
                    if (count - keep <= 0) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return;
                    }
                    cursor.move(keep);
                    latestDate = cursor.getLong(2);
                    try {
                        long threadId = cursor.getLong(1);
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (threadId != 0) {
                            deleteMessagesOlderThanDate(context, threadId, latestDate);
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    latestDate = 0;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
        }

        protected void deleteMessagesForThread(Context context, long threadId, int keep) {
            if (threadId != 0) {
                Cursor cursor = null;
                try {
                    cursor = SqliteWrapper.query(context, context.getContentResolver(), Mms.CONTENT_URI, MMS_MESSAGE_PROJECTION, "thread_id=" + threadId + " AND locked=0 " + " AND (m_type = 128 OR m_type = 132 OR m_type = 130)", null, "date DESC");
                    if (cursor == null) {
                        MLog.e("Recycler", "MMS: deleteMessagesForThread got back null cursor");
                    } else if (cursor.getCount() - keep <= 0) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    } else {
                        cursor.move(keep);
                        long latestDate = cursor.getLong(2);
                        if (cursor != null) {
                            cursor.close();
                        }
                        deleteMessagesOlderThanDate(context, threadId, latestDate);
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }

        private void deleteMessagesOlderThanDate(Context context, long threadId, long latestDate) {
            MLog.v("Recycler", "MMS: deleteMessagesOlderThanDate cntDeleted: " + ((long) SqliteWrapper.delete(context, context.getContentResolver(), Mms.CONTENT_URI, "thread_id=" + threadId + " AND locked=0 AND date<" + latestDate, null)));
        }

        protected boolean anyThreadOverLimit(android.content.Context r14) {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unexpected register number in merge insn: ?: MERGE  (r7_4 'cursor' android.database.Cursor) = (r7_3 android.database.Cursor), (r7_5 'msgs' android.database.Cursor)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.replaceMerge(EliminatePhiNodes.java:84)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.replaceMergeInstructions(EliminatePhiNodes.java:68)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.visit(EliminatePhiNodes.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r13 = this;
            r12 = 0;
            r7 = r13.getAllThreads(r14);
            if (r7 != 0) goto L_0x0008;
        L_0x0007:
            return r12;
        L_0x0008:
            r8 = r13.getMessageLimit(r14);
        L_0x000c:
            r0 = r7.moveToNext();	 Catch:{ all -> 0x0061 }
            if (r0 == 0) goto L_0x006b;	 Catch:{ all -> 0x0061 }
        L_0x0012:
            r1 = r14.getContentResolver();	 Catch:{ all -> 0x0061 }
            r10 = r13.getThreadId(r7);	 Catch:{ all -> 0x0061 }
            r2 = android.provider.Telephony.Mms.CONTENT_URI;	 Catch:{ all -> 0x0061 }
            r3 = MMS_MESSAGE_PROJECTION;	 Catch:{ all -> 0x0061 }
            r0 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0061 }
            r0.<init>();	 Catch:{ all -> 0x0061 }
            r4 = "thread_id=";	 Catch:{ all -> 0x0061 }
            r0 = r0.append(r4);	 Catch:{ all -> 0x0061 }
            r0 = r0.append(r10);	 Catch:{ all -> 0x0061 }
            r4 = " AND locked=0";	 Catch:{ all -> 0x0061 }
            r0 = r0.append(r4);	 Catch:{ all -> 0x0061 }
            r4 = " AND (m_type = 128 OR m_type = 132 OR m_type = 130)";	 Catch:{ all -> 0x0061 }
            r0 = r0.append(r4);	 Catch:{ all -> 0x0061 }
            r4 = r0.toString();	 Catch:{ all -> 0x0061 }
            r6 = "date DESC";	 Catch:{ all -> 0x0061 }
            r5 = 0;	 Catch:{ all -> 0x0061 }
            r0 = r14;	 Catch:{ all -> 0x0061 }
            r9 = com.huawei.cspcommon.ex.SqliteWrapper.query(r0, r1, r2, r3, r4, r5, r6);	 Catch:{ all -> 0x0061 }
            if (r9 != 0) goto L_0x004f;
        L_0x004b:
            r7.close();
            return r12;
        L_0x004f:
            r0 = r9.getCount();	 Catch:{ all -> 0x0066 }
            if (r0 < r8) goto L_0x005d;
        L_0x0055:
            r9.close();	 Catch:{ all -> 0x0061 }
            r0 = 1;
            r7.close();
            return r0;
        L_0x005d:
            r9.close();	 Catch:{ all -> 0x0061 }
            goto L_0x000c;
        L_0x0061:
            r0 = move-exception;
            r7.close();
            throw r0;
        L_0x0066:
            r0 = move-exception;
            r9.close();	 Catch:{ all -> 0x0061 }
            throw r0;	 Catch:{ all -> 0x0061 }
        L_0x006b:
            r7.close();
            return r12;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.mms.util.Recycler.MmsRecycler.anyThreadOverLimit(android.content.Context):boolean");
        }
    }

    public static class SmsRecycler extends Recycler {
        private static final String[] ALL_SMS_THREADS_PROJECTION = new String[]{"thread_id", "msg_count"};
        private static final String[] SMS_MESSAGE_PROJECTION = new String[]{"_id", "thread_id", "date", "read", NumberInfo.TYPE_KEY, "status"};

        public int getMessageLimit(Context context) {
            return getValidMessageLimit(PreferenceManager.getDefaultSharedPreferences(context).getInt("MaxSmsMessagesPerThread", MmsConfig.getDefaultSMSMessagesPerThread()));
        }

        public void setMessageLimit(Context context, int limit) {
            Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editPrefs.putInt("MaxSmsMessagesPerThread", limit);
            editPrefs.apply();
        }

        protected long getThreadId(Cursor cursor) {
            return cursor.getLong(0);
        }

        protected Cursor getAllThreads(Context context) {
            return SqliteWrapper.query(context, context.getContentResolver(), Conversations.CONTENT_URI, ALL_SMS_THREADS_PROJECTION, null, null, "date DESC");
        }

        protected void deleteMessagesForThread(Context context, long threadId, int keep) {
            if (Conversation.isGroupConversation(context, threadId)) {
                MLog.i("Recycler", "SMS: deleteMessagesForThread ignore for group-sms");
                return;
            }
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = SqliteWrapper.query(context, resolver, ContentUris.withAppendedId(Conversations.CONTENT_URI, threadId), SMS_MESSAGE_PROJECTION, "locked=0", null, "date DESC");
                if (cursor == null) {
                    MLog.e("Recycler", "SMS: deleteMessagesForThread got back null cursor");
                } else if (cursor.getCount() - keep <= 0) {
                    if (cursor != null) {
                        cursor.close();
                    }
                } else {
                    cursor.move(keep);
                    Context context2 = context;
                    MLog.v("Recycler", "SMS: deleteMessagesForThread cntDeleted: " + ((long) SqliteWrapper.delete(context2, resolver, ContentUris.withAppendedId(Conversations.CONTENT_URI, threadId), "locked=0 AND date<" + cursor.getLong(2), null)));
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        protected boolean anyThreadOverLimit(android.content.Context r14) {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unexpected register number in merge insn: ?: MERGE  (r7_4 'cursor' android.database.Cursor) = (r7_3 android.database.Cursor), (r7_5 'msgCursor' android.database.Cursor)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.replaceMerge(EliminatePhiNodes.java:84)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.replaceMergeInstructions(EliminatePhiNodes.java:68)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.visit(EliminatePhiNodes.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r13 = this;
            r12 = 0;
            r7 = r13.getAllThreads(r14);
            if (r7 != 0) goto L_0x0008;
        L_0x0007:
            return r12;
        L_0x0008:
            r8 = r13.getMessageLimit(r14);
        L_0x000c:
            r0 = r7.moveToNext();	 Catch:{ all -> 0x0046 }
            if (r0 == 0) goto L_0x0050;	 Catch:{ all -> 0x0046 }
        L_0x0012:
            r10 = r13.getThreadId(r7);	 Catch:{ all -> 0x0046 }
            r1 = r14.getContentResolver();	 Catch:{ all -> 0x0046 }
            r0 = android.provider.Telephony.Sms.Conversations.CONTENT_URI;	 Catch:{ all -> 0x0046 }
            r2 = android.content.ContentUris.withAppendedId(r0, r10);	 Catch:{ all -> 0x0046 }
            r3 = SMS_MESSAGE_PROJECTION;	 Catch:{ all -> 0x0046 }
            r4 = "locked=0";	 Catch:{ all -> 0x0046 }
            r6 = "date DESC";	 Catch:{ all -> 0x0046 }
            r5 = 0;	 Catch:{ all -> 0x0046 }
            r0 = r14;	 Catch:{ all -> 0x0046 }
            r9 = com.huawei.cspcommon.ex.SqliteWrapper.query(r0, r1, r2, r3, r4, r5, r6);	 Catch:{ all -> 0x0046 }
            if (r9 != 0) goto L_0x0034;
        L_0x0030:
            r7.close();
            return r12;
        L_0x0034:
            r0 = r9.getCount();	 Catch:{ all -> 0x004b }
            if (r0 < r8) goto L_0x0042;
        L_0x003a:
            r9.close();	 Catch:{ all -> 0x0046 }
            r0 = 1;
            r7.close();
            return r0;
        L_0x0042:
            r9.close();	 Catch:{ all -> 0x0046 }
            goto L_0x000c;
        L_0x0046:
            r0 = move-exception;
            r7.close();
            throw r0;
        L_0x004b:
            r0 = move-exception;
            r9.close();	 Catch:{ all -> 0x0046 }
            throw r0;	 Catch:{ all -> 0x0046 }
        L_0x0050:
            r7.close();
            return r12;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.mms.util.Recycler.SmsRecycler.anyThreadOverLimit(android.content.Context):boolean");
        }
    }

    protected abstract boolean anyThreadOverLimit(Context context);

    protected abstract void deleteMessagesForThread(Context context, long j, int i);

    public abstract int getMessageLimit(Context context);

    public abstract void setMessageLimit(Context context, int i);

    public static SmsRecycler getSmsRecycler() {
        if (sSmsRecycler == null) {
            sSmsRecycler = new SmsRecycler();
        }
        return sSmsRecycler;
    }

    public static MmsRecycler getMmsRecycler() {
        if (sMmsRecycler == null) {
            sMmsRecycler = new MmsRecycler();
        }
        return sMmsRecycler;
    }

    public static boolean checkForThreadsOverLimit(Context context) {
        return !getSmsRecycler().anyThreadOverLimit(context) ? getMmsRecycler().anyThreadOverLimit(context) : true;
    }

    public void deleteOldMessagesByThreadId(Context context, long threadId) {
        if (isAutoDeleteEnabled(context)) {
            MLog.i("Recycler", "Recycler.deleteOldMessagesByThreadId for Id: " + threadId);
            deleteMessagesForThread(context, threadId, getMessageLimit(context));
        }
    }

    public static boolean isAutoDeleteEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_auto_delete", false);
    }

    public int getMessageMinLimit() {
        return MmsConfig.getMinMessageCountPerThread();
    }

    public int getMessageMaxLimit() {
        return MmsConfig.getMaxMessageCountPerThread();
    }

    public int getValidMessageLimit(int value) {
        int msgMaxLimit = getMessageMaxLimit();
        if (value < getMessageMinLimit() || value > msgMaxLimit) {
            return msgMaxLimit;
        }
        return value;
    }
}
