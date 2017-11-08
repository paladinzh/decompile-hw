package com.android.mms.data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.IContentProvider;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Sms.Inbox;
import android.provider.Telephony.Threads;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.LongSparseArray;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact.UpdateListener;
import com.android.mms.data.HwCustConversation.ParmWrapper;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.MmsMessageSender.ReadRecContent;
import com.android.mms.transaction.MmsPermReceiver;
import com.android.mms.transaction.NotificationReceiver;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.PreferenceUtils;
import com.android.rcs.data.RcsConversation;
import com.android.rcs.data.RcsConversationUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.AsyncQueryHandlerEx;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwRecipientUtils;
import com.huawei.mms.util.NumberUtils;
import com.huawei.mms.util.PrivacyModeReceiver;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.SmartArchiveSettingUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressLint({"NewApi"})
public class Conversation {
    private static final String[] ALL_THREADS_PROJECTION = new String[]{"_id", "date", "message_count", "recipient_ids", "snippet", "snippet_cs", "read", "error", "has_attachment", "sub_id", "network_type", "unread_count", "priority", "number_type"};
    private static final String[] SEEN_PROJECTION = new String[]{"seen"};
    static final String[] SMS_PROJECTION = new String[]{"_id", "thread_id", "address", "body", "date", "read", NumberInfo.TYPE_KEY, "status", "locked", "error_code"};
    static final String[] UNREAD_PROJECTION = new String[]{"_id", "read"};
    private static final Uri URI_CONVERSATIONS_DELETE_ONCE_FOR_ALL = Uri.withAppendedPath(MmsSms.CONTENT_URI, "conversations-once-for-all");
    public static final Uri URI_CONVERSATIONS_MARK_AS_READ = Uri.withAppendedPath(MmsSms.CONTENT_URI, "conversations-mark-as-read");
    public static final Uri URI_HW_NOTIFICATIONS_MARK_AS_READ = Uri.withAppendedPath(MmsSms.CONTENT_URI, "hwNotifications-mark-as-read");
    public static final Uri URI_MMS_RCS_UNREAD_COUNT = Uri.parse("content://rcsim/get_mms_rcs_unread_count");
    public static final Uri URI_NOTIFICATIONS_MARK_AS_READ = Uri.withAppendedPath(MmsSms.CONTENT_URI, "notifications-mark-as-read");
    private static Context mContext = null;
    private static final LongSparseArray<String> mHwMessageName = new LongSparseArray();
    private static HashSet<Long> mPinupIds = new HashSet();
    private static String resPrefixFromBegin = null;
    private static String resPrefixFromEnd = null;
    public static final Uri sAllThreadsUri = Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
    private static final Uri sCommonArchiveThreadsUri = sAllThreadsUri.buildUpon().appendQueryParameter("number_type", String.valueOf(255)).build();
    private static final Uri sCommonThreadsUri = sAllThreadsUri.buildUpon().appendQueryParameter("number_type", String.valueOf(0)).build();
    private static boolean sDeletingThreads;
    private static Object sDeletingThreadsLock = new Object();
    private static boolean sHasUnreadMsg = false;
    private static HwCustConversation sHwCustConversation = ((HwCustConversation) HwCustUtils.createObj(HwCustConversation.class, new Object[0]));
    private static final Uri sHwServiceThreadsUri = sAllThreadsUri.buildUpon().appendQueryParameter("number_type", String.valueOf(1)).build();
    private static boolean sLoadingThreads;
    private static ContentValues sReadContentValues;
    private static final Uri sServiceThreadsUri = sAllThreadsUri.buildUpon().appendQueryParameter("number_type", String.valueOf(2)).build();
    private static int sUIUpdateHash = 0;
    private long mDate;
    private int mFlag;
    private RcsConversation mHwCustConversation;
    private HwCustConversation mHwCustConversationObj;
    private int mMessageCount;
    private int mPhoneType;
    private ContactList mRecipients;
    private long mThreadId;
    private int mUpdateHash;

    public static class Cache {
        private static final Cache sInstance = new Cache();
        private boolean mHasThreadsCached = false;
        private final LongSparseArray<Conversation> mMapCache = new LongSparseArray();

        public static Cache getInstance() {
            return sInstance;
        }

        public static Conversation get(long threadId) {
            Conversation conversation;
            synchronized (sInstance) {
                conversation = (Conversation) sInstance.mMapCache.get(threadId);
            }
            return conversation;
        }

        public static Conversation get(ContactList list) {
            synchronized (sInstance) {
                if (MLog.isLoggable("Mms_threadcache", 2)) {
                    LogTag.debug("Conversation get with ContactList", new Object[0]);
                }
                LongSparseArray<Conversation> cache = sInstance.mMapCache;
                int size = cache.size();
                for (int idx = 0; idx < size; idx++) {
                    Conversation c = (Conversation) cache.valueAt(idx);
                    if (c.getRecipients().equals(list)) {
                        return c;
                    }
                }
                return null;
            }
        }

        public static Conversation getRcsGroupCacheByGroupChatId(long groupChatId) {
            synchronized (sInstance) {
                if (MLog.isLoggable("Mms_threadcache", 2)) {
                    LogTag.debug("Conversation get with groupChatId", new Object[0]);
                }
                LongSparseArray<Conversation> cache = sInstance.mMapCache;
                int size = cache.size();
                int idx = 0;
                while (idx < size) {
                    Conversation c = (Conversation) cache.valueAt(idx);
                    if (c.getHwCust() == null || c.getHwCust().getGroupChatThreadId() != groupChatId) {
                        idx++;
                    } else {
                        return c;
                    }
                }
                return null;
            }
        }

        public static void put(Conversation c) {
            synchronized (sInstance) {
                if (sInstance.mMapCache.indexOfKey(c.getThreadId()) >= 0) {
                    throw new IllegalStateException("cache already contains threadId: " + c.mThreadId);
                }
                sInstance.mMapCache.put(c.getThreadId(), c);
            }
        }

        static boolean replace(Conversation c) {
            synchronized (sInstance) {
                if (sInstance.mMapCache.indexOfKey(c.getThreadId()) < 0) {
                    return false;
                }
                sInstance.mMapCache.put(c.getThreadId(), c);
                return true;
            }
        }

        static void remove(long threadId) {
            synchronized (sInstance) {
                sInstance.mMapCache.remove(Long.valueOf(threadId).longValue());
            }
        }

        static void dumpCache() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        static void keepOnly(Set<Long> threads) {
            Throwable th;
            synchronized (sInstance) {
                LongSparseArray<Conversation> cache = sInstance.mMapCache;
                synchronized (sInstance) {
                    try {
                        int size = cache.size();
                        Set<Long> toBeRemoved = new HashSet(size);
                        int idx = 0;
                        while (idx < size) {
                            try {
                                toBeRemoved.add(Long.valueOf(cache.keyAt(idx)));
                                idx++;
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
            }
        }

        public static Collection<Conversation> getCache() {
            ArrayList<Conversation> rets;
            synchronized (sInstance) {
                LongSparseArray<Conversation> cache = sInstance.mMapCache;
                int size = cache.size();
                rets = new ArrayList(size);
                for (int idx = 0; idx < size; idx++) {
                    rets.add((Conversation) cache.valueAt(idx));
                }
            }
            return rets;
        }
    }

    public static class ConversationQueryHandler extends AsyncQueryHandlerEx {
        private CryptoConversation mCryptoConversation = new CryptoConversation();
        private int mDeleteToken;

        public ConversationQueryHandler(ContentResolver cr) {
            super(cr);
        }

        public void setDeleteToken(int token) {
            this.mDeleteToken = token;
        }

        protected void onDeleteComplete(int token, Object cookie, int result) {
            this.mCryptoConversation.updateSwitchState(token, cookie, result);
            if (token == this.mDeleteToken) {
                synchronized (Conversation.sDeletingThreadsLock) {
                    Conversation.sDeletingThreads = false;
                    MLog.i("Mms_conv", "Conversation onDeleteComplete sDeletingThreads: " + Conversation.sDeletingThreads);
                    Conversation.sDeletingThreadsLock.notifyAll();
                }
            }
        }
    }

    public static class PinUpdateRequeset {
        public Collection<Long> mDatas;
        public boolean mIsAdd;

        public PinUpdateRequeset(boolean isAdd, Collection<Long> datas) {
            this.mIsAdd = isAdd;
            this.mDatas = datas;
        }

        public boolean isPinup() {
            return this.mIsAdd;
        }

        public long[] getThreads() {
            if (this.mDatas == null || this.mDatas.size() == 0) {
                return new long[0];
            }
            long[] datas = new long[this.mDatas.size()];
            int idx = 0;
            for (Long val : this.mDatas) {
                int idx2 = idx + 1;
                datas[idx] = val.longValue();
                idx = idx2;
            }
            return datas;
        }
    }

    public static void dumpSmsTable(android.content.Context r8) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00bc in list []
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
        r1 = 0;
        r0 = "**** Dump of sms table ****";
        r1 = new java.lang.Object[r1];
        com.android.mms.LogTag.debug(r0, r1);
        r6 = 0;
        r1 = android.provider.Telephony.Sms.CONTENT_URI;	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r2 = SMS_PROJECTION;	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r5 = "_id DESC";	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r3 = 0;	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r4 = 0;	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0 = r8;	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r6 = com.huawei.cspcommon.ex.SqliteWrapper.query(r0, r1, r2, r3, r4, r5);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        if (r6 != 0) goto L_0x0020;
    L_0x001a:
        if (r6 == 0) goto L_0x001f;
    L_0x001c:
        r6.close();
    L_0x001f:
        return;
    L_0x0020:
        r0 = -1;
        r6.moveToPosition(r0);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
    L_0x0024:
        r0 = r6.moveToNext();	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        if (r0 == 0) goto L_0x00bd;	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
    L_0x002a:
        r0 = r6.getPosition();	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = 20;	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        if (r0 >= r1) goto L_0x00bd;	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
    L_0x0032:
        r0 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0.<init>();	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = "dumpSmsTable _id: ";	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0 = r0.append(r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = 0;	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r2 = r6.getLong(r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0 = r0.append(r2);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = " ";	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0 = r0.append(r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = "thread_id";	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0 = r0.append(r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = " : ";	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0 = r0.append(r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = 1;	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r2 = r6.getLong(r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0 = r0.append(r2);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = " ";	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0 = r0.append(r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = "date";	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0 = r0.append(r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = " : ";	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0 = r0.append(r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = 4;	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r2 = r6.getLong(r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0 = r0.append(r2);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = " ";	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0 = r0.append(r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = "type";	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0 = r0.append(r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = " : ";	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0 = r0.append(r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = 6;	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = r6.getInt(r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0 = r0.append(r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r0 = r0.toString();	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = 0;	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = new java.lang.Object[r1];	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        com.android.mms.LogTag.debug(r0, r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        goto L_0x0024;
    L_0x00ad:
        r7 = move-exception;
        r0 = "Mms_conv";	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        r1 = "DumpSmsTable error";	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        com.huawei.cspcommon.MLog.e(r0, r1);	 Catch:{ SQLiteException -> 0x00ad, all -> 0x00c3 }
        if (r6 == 0) goto L_0x00bc;
    L_0x00b9:
        r6.close();
    L_0x00bc:
        return;
    L_0x00bd:
        if (r6 == 0) goto L_0x00bc;
    L_0x00bf:
        r6.close();
        goto L_0x00bc;
    L_0x00c3:
        r0 = move-exception;
        if (r6 == 0) goto L_0x00c9;
    L_0x00c6:
        r6.close();
    L_0x00c9:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.data.Conversation.dumpSmsTable(android.content.Context):void");
    }

    private synchronized void setMask(boolean r1, int r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.mms.data.Conversation.setMask(boolean, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.data.Conversation.setMask(boolean, int):void");
    }

    private synchronized void setMaskNumber(int r1, int r2, int r3) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.mms.data.Conversation.setMaskNumber(int, int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.data.Conversation.setMaskNumber(int, int, int):void");
    }

    public static final String[] getThreadProjection() {
        return (String[]) ALL_THREADS_PROJECTION.clone();
    }

    private Context getContext() {
        return MmsApp.getApplication();
    }

    public void setHasTempDraft(boolean hasDraft) {
        setMask(hasDraft, 1);
    }

    public synchronized boolean hasTempDraft() {
        boolean z = false;
        synchronized (this) {
            if ((this.mFlag & 1) != 0) {
                z = true;
            }
        }
        return z;
    }

    private void setMarkAsReadBlocked(boolean mark) {
        setMask(mark, 2);
    }

    private synchronized boolean isMarkAsReadBlocked() {
        boolean z = false;
        synchronized (this) {
            if ((this.mFlag & 2) != 0) {
                z = true;
            }
        }
        return z;
    }

    private void setMarkAsReadWaiting(boolean waiting) {
        setMask(waiting, 4);
    }

    private synchronized boolean isMarkAsReadWaiting() {
        boolean z = false;
        synchronized (this) {
            if ((this.mFlag & 4) != 0) {
                z = true;
            }
        }
        return z;
    }

    public void setHasAttachment(boolean hasAttachment) {
        setMask(hasAttachment, 32);
    }

    public void setThumbnail(Cursor c) {
        if (this.mHwCustConversationObj != null) {
            this.mHwCustConversationObj.setThumanailPath(c);
        }
    }

    public synchronized boolean hasAttachment() {
        boolean z = false;
        synchronized (this) {
            if ((this.mFlag & 32) != 0) {
                z = true;
            }
        }
        return z;
    }

    public synchronized boolean hasUnreadMessages() {
        boolean z = false;
        synchronized (this) {
            if ((this.mFlag & 8) != 0) {
                z = true;
            }
        }
        return z;
    }

    private void setHasUnreadMessages(boolean unread) {
        setMask(unread, 8);
    }

    public synchronized boolean isServiceNumber() {
        boolean z = false;
        synchronized (this) {
            if ((this.mFlag & 64) != 0) {
                z = true;
            }
        }
        return z;
    }

    private void setIsServiceNumber(boolean isServiceNumber) {
        setMask(isServiceNumber, 64);
    }

    public void setUnreadMessageCount(int cnt) {
        setMaskNumber(cnt, 17, 12);
    }

    public synchronized int getUnreadMessageCount() {
        return (this.mFlag & 536739840) >> 17;
    }

    public void setPriority(int priority) {
        setMaskNumber(priority, 10, 3);
    }

    public synchronized int getPriority() {
        return (this.mFlag & 7168) >> 10;
    }

    public synchronized int getSubId() {
        return (this.mFlag & 768) >> 8;
    }

    public void setSubId(int subId) {
        setMaskNumber(subId, 8, 2);
    }

    public synchronized int getNumberType() {
        return (this.mFlag & 122880) >> 13;
    }

    public void setNumberType(int type) {
        setMaskNumber(type, 13, 4);
    }

    public synchronized boolean hasError() {
        boolean z = false;
        synchronized (this) {
            if ((this.mFlag & 16) != 0) {
                z = true;
            }
        }
        return z;
    }

    public synchronized boolean lastMessageIsError() {
        boolean z = false;
        synchronized (this) {
            if ((this.mFlag & 128) != 0) {
                z = true;
            }
        }
        return z;
    }

    public void setHasErrorMessages(boolean hasError) {
        setMask(hasError, 16);
    }

    public HwCustConversation getHwCustConversationObj() {
        return this.mHwCustConversationObj;
    }

    public void setLastMessageError(boolean lastError) {
        setMask(lastError, 128);
    }

    public Conversation(Conversation conversation) {
        this.mHwCustConversation = null;
        this.mPhoneType = -1;
        this.mFlag = 0;
        this.mUpdateHash = 0;
        this.mHwCustConversationObj = (HwCustConversation) HwCustUtils.createObj(HwCustConversation.class, new Object[0]);
        createHwCustConversation();
        this.mRecipients = new ContactList();
        if (conversation.mRecipients != null) {
            for (Contact c : conversation.mRecipients) {
                this.mRecipients.add(c);
            }
        }
        this.mThreadId = conversation.mThreadId;
        this.mDate = conversation.mDate;
        this.mMessageCount = conversation.mMessageCount;
        this.mFlag = conversation.mFlag;
        this.mUpdateHash = conversation.mUpdateHash;
    }

    private Conversation(Context context) {
        this.mHwCustConversation = null;
        this.mPhoneType = -1;
        this.mFlag = 0;
        this.mUpdateHash = 0;
        this.mHwCustConversationObj = (HwCustConversation) HwCustUtils.createObj(HwCustConversation.class, new Object[0]);
        createHwCustConversation();
        this.mRecipients = new ContactList();
        this.mThreadId = 0;
    }

    public Conversation(Context context, long threadId, boolean allowQuery) {
        this.mHwCustConversation = null;
        this.mPhoneType = -1;
        this.mFlag = 0;
        this.mUpdateHash = 0;
        this.mHwCustConversationObj = (HwCustConversation) HwCustUtils.createObj(HwCustConversation.class, new Object[0]);
        createHwCustConversation();
        if (!loadFromThreadId(threadId, allowQuery)) {
            this.mRecipients = new ContactList();
            this.mThreadId = 0;
        }
    }

    private Conversation(Context context, Cursor cursor, boolean allowQuery) {
        this.mHwCustConversation = null;
        this.mPhoneType = -1;
        this.mFlag = 0;
        this.mUpdateHash = 0;
        this.mHwCustConversationObj = (HwCustConversation) HwCustUtils.createObj(HwCustConversation.class, new Object[0]);
        createHwCustConversation();
        fillFromCursor(context, this, cursor, allowQuery, cursor.hashCode());
    }

    public static Conversation createNew(Context context) {
        return new Conversation(context);
    }

    public static void checkConversationWithMessages(Context context, long threadId) {
        if (get(context, threadId, true, true).getMessageCount() <= 0) {
            MLog.e("Mms_conv", "Recieve message but conversation not updated.");
        }
    }

    public static Conversation get(Context context, long threadId, boolean allowQuery) {
        return get(context, threadId, allowQuery, false);
    }

    public static Conversation get(Context context, long threadId, boolean allowQuery, boolean checkMsgSize) {
        context = checkContextType(context);
        Conversation conv = Cache.get(threadId);
        if (conv != null) {
            if (!checkMsgSize || conv.getMessageCount() > 0) {
                return conv;
            }
            Cache.remove(threadId);
        }
        conv = new Conversation(context, threadId, allowQuery);
        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error("Tried to add duplicate Conversation to Cache (from threadId): " + conv.getThreadId());
            if (!Cache.replace(conv)) {
                LogTag.error("get by threadId cache.replace failed on " + conv.getThreadId());
            }
        }
        return conv;
    }

    public static Conversation get(Context context, ContactList recipients, boolean allowQuery) {
        context = checkContextType(context);
        if (recipients.size() < 1) {
            return createNew(context);
        }
        if ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""))) {
            recipients.removeIPAndZeroPrefixForChina();
        }
        Conversation conv = Cache.get(recipients);
        if (conv != null) {
            return conv;
        }
        if (RcsConversationUtils.getHwCustUtils() == null || !RcsConversationUtils.getHwCustUtils().isRcsSwitchOn()) {
            conv = new Conversation(context, getOrCreateThreadId(context, recipients), allowQuery);
        } else if (RcsConversationUtils.getHwCustUtils().isRcsConversation(recipients)) {
            conv = new Conversation(context, RcsConversationUtils.getHwCustUtils().getOrCreateRcsThreadId(context, recipients), allowQuery, new ParmWrapper(Long.valueOf(RcsConversationUtils.getHwCustUtils().getRcsModeValueOfIm()), null));
            if (conv.getHwCust() != null) {
                conv.getHwCust().setIsRcsThreadId(true);
            }
        } else {
            conv = new Conversation(context, getOrCreateThreadId(context, recipients), allowQuery, new ParmWrapper(Long.valueOf(RcsConversationUtils.getHwCustUtils().getRcsModeValueOfXms()), null));
            if (conv.getHwCust() != null) {
                conv.getHwCust().setIsRcsThreadId(false);
            }
        }
        MLog.d("Mms_conv", "Conversation.get: created new conversation xxxxxxx");
        if (!conv.getRecipients().equals(recipients)) {
            LogTag.error("Mms_conv", "Conversation.get: new conv's recipients don't match input recpients xxxxxxx");
        }
        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error("Tried to add duplicate Conversation to Cache (from recipients): " + conv.getThreadId());
            if (!Cache.replace(conv)) {
                LogTag.error("get by recipients cache.replace failed on " + conv.getThreadId());
            }
        }
        return conv;
    }

    public static Conversation get(Context context, Uri uri, boolean allowQuery) {
        context = checkContextType(context);
        if (uri == null) {
            return createNew(context);
        }
        if (uri.getPathSegments().size() >= 2) {
            try {
                long threadId = Long.parseLong((String) uri.getPathSegments().get(1));
                if (RcsConversationUtils.getHwCustUtils() == null || !RcsConversationUtils.getHwCustUtils().isRcsSwitchOn()) {
                    return get(context, threadId, allowQuery);
                }
                return RcsConversationUtils.getHwCustUtils().getRcsConversation(context, uri, allowQuery);
            } catch (NumberFormatException e) {
                MLog.e("Mms_conv", "Conversation get FROM URI NumberFormatException");
            }
        }
        return get(context, ContactList.getByNumbers(PhoneNumberUtils.replaceUnicodeDigits(getRecipients(uri)).replace(',', ';').replace(" ", ""), allowQuery, true), allowQuery);
    }

    public static Conversation createtNew(Context context, Uri uri) {
        context = checkContextType(context);
        if (uri.getPathSegments().size() >= 2) {
            return createNew(context);
        }
        ContactList<Contact> cl = ContactList.getByNumbers(PhoneNumberUtils.replaceUnicodeDigits(getRecipients(uri)).replace(',', ';'), false, true);
        if ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""))) {
            cl.removeIPAndZeroPrefixForChina();
        }
        ContactList invalideContactList = new ContactList();
        for (Contact c : cl) {
            String number = MessageUtils.parseMmsAddress(c.getOriginNumber(), true);
            if (TextUtils.isEmpty(number)) {
                number = c.getOriginNumber();
            }
            if (!HwRecipientUtils.isValidAddress(number, true)) {
                invalideContactList.add(c);
            }
        }
        if (invalideContactList.size() > 0) {
            cl.removeAll(invalideContactList);
        }
        Conversation cv = createNew(context);
        cv.setRecipients(cl);
        return cv;
    }

    public static Conversation from(Context context, Cursor cursor) {
        Conversation conv;
        context = checkContextType(context);
        long threadId = cursor.getLong(0);
        if (threadId > 0) {
            conv = Cache.get(threadId);
            if (conv != null) {
                fillFromCursor(context, conv, cursor, false);
                return conv;
            }
        }
        conv = new Conversation(context, cursor, false);
        try {
            Cache.put(conv);
        } catch (IllegalStateException e) {
            LogTag.error("Mms_conv", "Tried to add duplicate Conversation to Cache (from cursor): " + conv.getThreadId());
            if (!Cache.replace(conv)) {
                LogTag.error("Converations.from cache.replace failed on " + conv.getThreadId());
            }
        }
        return conv;
    }

    public static ContentValues getReadContentValues() {
        ContentValues contentValues;
        synchronized (Conversation.class) {
            if (sReadContentValues == null) {
                sReadContentValues = new ContentValues(2);
                sReadContentValues.put("read", Integer.valueOf(1));
                sReadContentValues.put("seen", Integer.valueOf(1));
            }
            contentValues = sReadContentValues;
        }
        return contentValues;
    }

    public void markAsRead() {
        if (!MmsConfig.isSmsEnabled(getContext())) {
            return;
        }
        if (!NotificationReceiver.getInst().isUserPresent(getContext())) {
            MLog.d("Mms_conv", "Block mark as read as user not present");
        } else if (!isMarkAsReadWaiting()) {
            if (isMarkAsReadBlocked()) {
                setMarkAsReadWaiting(true);
                return;
            }
            synchronized (this) {
                if (this.mThreadId == 0) {
                    return;
                }
                final Uri threadUri = getUri();
                new AsyncTask<Void, Void, List<ReadRecContent>>() {
                    private List<ReadRecContent> getPendingRecords(Context context, long threadId, int status) {
                        if (!MmsConfig.isShowMmsReadReportDialog() && !PreferenceUtils.isEnableAutoReplyMmsRR(context)) {
                            return null;
                        }
                        List<Long> convs = new ArrayList();
                        convs.add(Long.valueOf(threadId));
                        return MessageUtils.getReadReportData(Conversation.this.getContext(), convs, 128);
                    }

                    protected void onPostExecute(List<ReadRecContent> pendingRecs) {
                        if (pendingRecs != null && pendingRecs.size() != 0) {
                            Context context = Conversation.this.getContext();
                            if (MmsConfig.isShowMmsReadReportDialog()) {
                                MessageUtils.confirmReadReportDialog(context, pendingRecs, null);
                                return;
                            }
                            ResEx.makeToast(context.getResources().getQuantityString(R.plurals.read_report_toast_msg, pendingRecs.size(), new Object[]{Integer.valueOf(pendingRecs.size())}), 0);
                            MessageUtils.sendReadReport(context, pendingRecs);
                        }
                    }

                    protected synchronized List<ReadRecContent> doInBackground(Void... none) {
                        List<ReadRecContent> pendings;
                        if (MLog.isLoggable("Mms_app", 2)) {
                            LogTag.debug("markAsRead.doInBackground and threadUri is : " + threadUri, new Object[0]);
                        }
                        Context context = Conversation.this.getContext();
                        if (Conversation.this.mHwCustConversation != null) {
                            Conversation.this.mHwCustConversation.sendImReadReport(Conversation.this.mRecipients, Conversation.this.mThreadId);
                        }
                        pendings = null;
                        if (threadUri != null) {
                            if (Conversation.this.mHwCustConversation == null || !Conversation.this.mHwCustConversation.isRcsSwitchOn()) {
                                boolean hasUnreadMessages;
                                Cursor c = SqliteWrapper.query(context, threadUri, Conversation.UNREAD_PROJECTION, "(read=0 OR seen=0)", null, null);
                                if (c != null) {
                                    try {
                                        LogTag.debug("There are " + c.getCount() + " thread that is no readed and the unreadcount is : " + Conversation.this.getUnreadMessageCount() + "  hasUnreadMessages() is :" + Conversation.this.hasUnreadMessages(), new Object[0]);
                                        hasUnreadMessages = (c.getCount() > 0 || Conversation.this.getUnreadMessageCount() > 0) ? true : Conversation.this.hasUnreadMessages();
                                        c.close();
                                    } catch (Throwable th) {
                                        c.close();
                                    }
                                } else {
                                    LogTag.debug("Thee unreadcount is : " + Conversation.this.getUnreadMessageCount(), new Object[0]);
                                    hasUnreadMessages = Conversation.this.getUnreadMessageCount() <= 0 ? Conversation.this.hasUnreadMessages() : true;
                                }
                                if (hasUnreadMessages) {
                                    synchronized (this) {
                                    }
                                    pendings = getPendingRecords(context, Conversation.this.mThreadId, 128);
                                    LogTag.debug("markAsRead: update read/seen for thread uri: " + threadUri, new Object[0]);
                                    SqliteWrapper.update(context, threadUri, Conversation.getReadContentValues(), "(read=0 OR seen=0)", null);
                                    MessagingNotification.blockingUpdateAllNotifications(context, -2);
                                }
                            } else if (Conversation.this.mHwCustConversation.markAsReadDoInBackground(threadUri, Conversation.UNREAD_PROJECTION, "(read=0 OR seen=0)", Conversation.getReadContentValues(), Conversation.this, Conversation.this.getContext())) {
                                pendings = getPendingRecords(context, Conversation.this.mThreadId, 128);
                                MLog.d("Mms_conv", "markAsRead: update read/seen for thread uri: " + threadUri);
                                MessagingNotification.blockingUpdateAllNotifications(context, -2);
                            }
                            Conversation.this.setHasUnreadMessages(false);
                            Conversation.this.setUnreadMessageCount(0);
                        }
                        return pendings;
                    }
                }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
            }
        }
    }

    public void blockMarkAsRead(boolean block) {
        if (MLog.isLoggable("Mms_app", 2)) {
            LogTag.debug("blockMarkAsRead: " + block, new Object[0]);
        }
        if (block != isMarkAsReadBlocked()) {
            setMarkAsReadBlocked(block);
            if (!isMarkAsReadBlocked() && isMarkAsReadWaiting()) {
                setMarkAsReadWaiting(false);
                markAsRead();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized Uri getUri() {
        if (MmsConfig.isSupportDraftWithoutRecipient()) {
        }
        return null;
    }

    public static Uri getUri(long threadId) {
        return ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);
    }

    public synchronized long getThreadId() {
        return this.mThreadId;
    }

    public boolean isThreadIdInDB(long threadId) {
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(getContext(), sAllThreadsUri, new String[]{"_id"}, "_id=" + Long.toString(threadId), null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                LogTag.debug("isThreadIdInDB: Can't find thread ID =" + threadId, new Object[0]);
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            if (cursor != null) {
                cursor.close();
            }
            return true;
        } catch (Exception e) {
            MLog.e("Mms_conv", " c closed  unformally");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public synchronized long ensureThreadId() {
        if (this.mHwCustConversation != null && this.mHwCustConversation.isRcsSwitchOn()) {
            this.mHwCustConversation.getEnsureThreadId(this, getContext());
        } else if (this.mThreadId <= 0 || !isThreadIdInDB(this.mThreadId)) {
            try {
                this.mThreadId = getOrCreateThreadId(getContext(), this.mRecipients);
            } catch (Exception e) {
                e.printStackTrace();
                this.mThreadId = 0;
            }
        }
        return this.mThreadId;
    }

    public void clearThreadId() {
        long tempTID;
        synchronized (this) {
            if (MLog.isLoggable("Mms_app", 2)) {
                LogTag.debug("clearThreadId old threadId was: " + this.mThreadId + " now zero", new Object[0]);
            }
            tempTID = this.mThreadId;
            this.mThreadId = 0;
        }
        Cache.remove(tempTID);
    }

    public synchronized void setRecipients(ContactList list) {
        if (MLog.isLoggable("Mms_app", 2)) {
            MLog.d("Mms_conv", "setRecipients before:tid " + this.mThreadId);
        }
        if ("CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""))) {
            list.removeIPAndZeroPrefixForChina();
        }
        this.mRecipients = list;
        if (getMessageCount() == 0 && !hasDraft()) {
            if (!hasTempDraft()) {
                clearThreadId();
                if (MLog.isLoggable("Mms_app", 2)) {
                    MLog.d("Mms_conv", "setRecipients after:tid " + this.mThreadId);
                }
            }
        }
        this.mThreadId = 0;
        if (MLog.isLoggable("Mms_app", 2)) {
            MLog.d("Mms_conv", "setRecipients after:tid " + this.mThreadId);
        }
    }

    public synchronized ContactList getRecipients() {
        return this.mRecipients;
    }

    public ContactList getPurePhoneNumberRecipients() {
        ContactList mPurePhoneNumberRecipients = new ContactList();
        if (getRecipients() != null) {
            for (Contact c : getRecipients()) {
                if (!c.isEmail()) {
                    mPurePhoneNumberRecipients.add(c);
                }
            }
        }
        return mPurePhoneNumberRecipients;
    }

    public ContactList getPureEmailAddressRecipients() {
        ContactList mPureEmailAddressRecipients = new ContactList();
        if (getRecipients() != null) {
            for (Contact c : getRecipients()) {
                if (c.isEmail()) {
                    mPureEmailAddressRecipients.add(c);
                }
            }
        }
        return mPureEmailAddressRecipients;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean hasDraft() {
        if (this.mHwCustConversation == null || !this.mHwCustConversation.isRcsSwitchOn()) {
            if (MmsConfig.isSupportDraftWithoutRecipient()) {
            }
            return false;
        }
        return this.mHwCustConversation.hasDraft(this, getContext());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setDraftState(boolean hasDraft) {
        if (this.mHwCustConversation != null && this.mHwCustConversation.isRcsSwitchOn()) {
            this.mHwCustConversation.setDraftState(hasDraft, this, getContext());
        } else if (MmsConfig.isSupportDraftWithoutRecipient()) {
        }
    }

    public synchronized long getDate() {
        return this.mDate;
    }

    public synchronized int getMessageCount() {
        return this.mMessageCount;
    }

    public synchronized void setMessageCount(int cnt) {
        this.mMessageCount = cnt;
    }

    public synchronized boolean isMessageCountValid() {
        boolean z = false;
        synchronized (this) {
            if (this.mMessageCount > 0) {
                z = true;
            }
        }
        return z;
    }

    public synchronized boolean isMessageCountSingle() {
        boolean z = true;
        synchronized (this) {
            if (this.mMessageCount != 1) {
                z = false;
            }
        }
        return z;
    }

    public static long getOrCreateThreadId(Context context, ContactList list) {
        HashSet<String> recipients = new HashSet();
        HashSet<String> recipientsSrc = new HashSet();
        long retVal = 0;
        for (Contact c : list) {
            Contact cacheContact = Contact.get(c.getNumber(), false);
            if (cacheContact != null) {
                String afterParseCacheContact = cacheContact.getNumber();
                if (cacheContact.isEmail()) {
                    recipientsSrc.add(afterParseCacheContact);
                    try {
                        afterParseCacheContact = Mms.extractAddrSpec(afterParseCacheContact);
                    } catch (Exception e) {
                        MLog.e("Mms_conv", "getOrCreateThreadId extractAddrSpec exception: " + e);
                    }
                    recipients.add(afterParseCacheContact);
                } else {
                    afterParseCacheContact = MessageUtils.parseMmsAddress(cacheContact.getNumber(), true);
                    if (TextUtils.isEmpty(afterParseCacheContact)) {
                        recipients.add(cacheContact.getNumber());
                        recipientsSrc.add(cacheContact.getNumber());
                    } else {
                        recipients.add(afterParseCacheContact);
                        recipientsSrc.add(afterParseCacheContact);
                    }
                }
            } else {
                String address = c.getNumber();
                if (Contact.isEmailAddress(address)) {
                    recipientsSrc.add(address);
                    try {
                        address = Mms.extractAddrSpec(address);
                    } catch (Exception e2) {
                        MLog.e("Mms_conv", "getOrCreateThreadId extractAddrSpec exception: " + e2);
                    }
                    recipients.add(address);
                } else {
                    String afterParseContact = MessageUtils.parseMmsAddress(address, true);
                    if (TextUtils.isEmpty(afterParseContact)) {
                        recipients.add(address);
                        recipientsSrc.add(address);
                    } else {
                        recipients.add(afterParseContact);
                        recipientsSrc.add(afterParseContact);
                    }
                }
            }
        }
        synchronized (sDeletingThreadsLock) {
            long now = System.currentTimeMillis();
            while (sDeletingThreads) {
                try {
                    sDeletingThreadsLock.wait(30000);
                } catch (InterruptedException e3) {
                }
                if (System.currentTimeMillis() - now > 29000) {
                    MLog.e("Mms_conv", "getOrCreateThreadId timed out waiting for delete to complete", new Exception());
                    sDeletingThreads = false;
                    break;
                }
            }
            try {
                retVal = getThreadIdByBundleCall(context, new ArrayList(recipients), false);
                if (-1 == retVal) {
                    retVal = Threads.getOrCreateThreadId(context, recipientsSrc);
                }
            } catch (IllegalArgumentException e4) {
                MLog.e("Mms_conv", "Get threadId exception:" + e4.getMessage());
            }
            if (MLog.isLoggable("Mms_app", 2)) {
                LogTag.debug("[Conversation] getOrCreateThreadId for returned %d", Long.valueOf(retVal));
            }
        }
        return retVal;
    }

    public static long getOrCreateThreadId(Context context, String address) {
        return getOrCreateThreadId(context, address, false);
    }

    public static long getOrCreateThreadId(Context context, String address, boolean isServerAddr) {
        return getOrCreateThreadId(context, address, isServerAddr, false);
    }

    public static long getOrCreateThreadId(Context context, String address, boolean isServerAddr, boolean lock) {
        long retVal;
        if (isServerAddr) {
            MLog.v("Mms_conv", "getOrCreateThreadId:: the address is server address!");
        } else {
            String formatedAddress = MessageUtils.parseMmsAddress(address, true);
            if (TextUtils.isEmpty(formatedAddress)) {
                MLog.v("Mms_conv", "getOrCreateThreadId::parse the address return null!");
            } else {
                address = formatedAddress;
            }
        }
        synchronized (sDeletingThreadsLock) {
            long now = System.currentTimeMillis();
            while (sDeletingThreads) {
                try {
                    sDeletingThreadsLock.wait(30000);
                } catch (InterruptedException e) {
                }
                if (System.currentTimeMillis() - now > 29000) {
                    sDeletingThreads = false;
                    MLog.e("Mms_conv", "getOrCreateThreadId timed out waiting for delete to complete", new Exception());
                    break;
                }
            }
            retVal = 0;
            try {
                ArrayList<String> recipients = new ArrayList();
                String addressSrc = address;
                if (Contact.isEmailAddress(address)) {
                    address = Mms.extractAddrSpec(address);
                }
                recipients.add(address);
                retVal = getThreadIdByBundleCall(context, recipients, lock);
                if (-1 == retVal) {
                    retVal = Threads.getOrCreateThreadId(context, addressSrc);
                }
            } catch (IllegalArgumentException e2) {
                MLog.w("Mms_conv", e2.getMessage());
            }
            if (MLog.isLoggable("Mms_app", 2)) {
                LogTag.debug("[Conversation] getOrCreateThreadId2 for returned %d", Long.valueOf(retVal));
            }
        }
        return retVal;
    }

    private static long getThreadIdByBundleCall(Context context, ArrayList<String> recipients, boolean lock) {
        Bundle extras = new Bundle();
        extras.putStringArrayList("recipient", recipients);
        if (lock) {
            extras.putBoolean("lock", true);
        }
        Bundle result = SqliteWrapper.call(context, MmsSms.CONTENT_URI, context.getPackageName(), "method_getorcreate_threadid", null, extras);
        if (result == null || !result.getBoolean("call_result")) {
            return -1;
        }
        return result.getLong("thread_id");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean equals(Object obj) {
        boolean z = false;
        synchronized (this) {
            if (obj == null) {
                return false;
            }
            try {
                Conversation other = (Conversation) obj;
                if (this.mRecipients.size() > 0) {
                    z = this.mRecipients.equals(other.mRecipients);
                }
            } catch (ClassCastException e) {
                return false;
            }
        }
    }

    public synchronized int hashCode() {
        return (int) this.mThreadId;
    }

    public synchronized String toString() {
        return String.format(" (tid %d)", new Object[]{Long.valueOf(this.mThreadId)});
    }

    public static void asyncDeleteObsoleteThreads(AsyncQueryHandler handler, int token) {
        handler.startDelete(token, null, Threads.OBSOLETE_THREADS_URI, null, null);
    }

    public static void startQueryForAll(AsyncQueryHandler handler, int token, int queryType) {
        if (handler == null) {
            MLog.e("Mms_conv", "startQueryForAll error! handler is null token = " + token + " queryType = " + queryType);
            return;
        }
        handler.cancelOperation(token);
        startQuery(handler, token, null, queryType);
    }

    public static void startQuery(AsyncQueryHandler handler, int token, String selection, int queryType) {
        String[] strArr;
        handler.cancelOperation(token);
        Uri threadUri = getThreadUri(queryType);
        HwCustConversation lHwCustConversationTemp = (HwCustConversation) HwCustUtils.createObj(HwCustConversation.class, new Object[0]);
        if (lHwCustConversationTemp == null) {
            strArr = ALL_THREADS_PROJECTION;
        } else {
            strArr = lHwCustConversationTemp.getAllThreadsProjection(ALL_THREADS_PROJECTION);
        }
        handler.startQuery(token, null, threadUri, strArr, selection, null, "priority DESC, date DESC");
    }

    private static Uri getThreadUri(int queryType) {
        Uri threadUri;
        if (queryType == 255) {
            threadUri = sAllThreadsUri.buildUpon().appendQueryParameter("number_type", String.valueOf(255)).build();
        } else if (queryType == 1) {
            threadUri = sHwServiceThreadsUri;
        } else if (queryType == 2) {
            threadUri = sServiceThreadsUri;
        } else if (queryType == 0) {
            threadUri = sCommonThreadsUri;
        } else {
            threadUri = sAllThreadsUri;
        }
        return threadUri.buildUpon().appendQueryParameter("unread_pinup_enable", String.valueOf(MmsConfig.getUnreadPinupEnable(mContext))).build();
    }

    public static void startDeleteOnceForAll(ConversationQueryHandler handler, int token, boolean deleteAll, Collection<Long> threadIds) {
        synchronized (sDeletingThreadsLock) {
            if (sDeletingThreads) {
                MLog.e("Mms_conv", "startDeleteAll already in the middle of a delete", new Exception());
            }
            sDeletingThreads = true;
            Uri uri = URI_CONVERSATIONS_DELETE_ONCE_FOR_ALL;
            if (threadIds.isEmpty()) {
                return;
            }
            String s = threadIds.toString();
            String selection = "thread_id IN (" + s.substring(1, s.length() - 1) + ")";
            if (!deleteAll) {
                selection = selection + "AND (locked=0) ";
            }
            MmsApp.getApplication().getPduLoaderManager().clear();
            MmsApp.getApplication().clearThumbnail();
            MLog.i("Mms_conv", "startDeleteOnceForAll...and threadIds = " + s);
            handler.setDeleteToken(token);
            handler.startDelete(token, s, uri, selection, null);
        }
    }

    public static void startDeleteAll(ConversationQueryHandler handler, int token, boolean deleteAll) {
        synchronized (sDeletingThreadsLock) {
            MLog.i("Mms_conv", "Conversation startDeleteAll sDeletingThreads: " + sDeletingThreads);
            if (sDeletingThreads) {
                MLog.e("Mms_conv", "startDeleteAll already in the middle of a delete", new Exception());
            }
            sDeletingThreads = true;
            String str = deleteAll ? null : "locked=0";
            MmsApp app = MmsApp.getApplication();
            app.getPduLoaderManager().clear();
            app.clearThumbnail();
            handler.setDeleteToken(token);
            handler.startDelete(token, String.valueOf(-1), Threads.CONTENT_URI, str, null);
        }
    }

    public static void startQueryHaveUnreadMessages(AsyncQueryHandler handler, int token, int runningMode) {
        handler.cancelOperation(token);
        Uri uri = URI_CONVERSATIONS_MARK_AS_READ;
        String selection = "unread_count > 0";
        if (runningMode == 4) {
            selection = selection + " AND number_type=2";
        } else if (runningMode == 5) {
            selection = selection + " AND number_type=1";
        }
        AsyncQueryHandler asyncQueryHandler = handler;
        int i = token;
        asyncQueryHandler.startQuery(i, new String[]{"unread_count"}, uri, ALL_THREADS_PROJECTION, selection, null, "unread_count limit 1");
    }

    public static void startQueryUnreadMessageCount(AsyncQueryHandler handler, int token, int runningMode) {
        handler.cancelOperation(token);
        AsyncQueryHandler asyncQueryHandler;
        int i;
        if (RcsConversationUtils.getHwCustUtils() == null || !RcsConversationUtils.getHwCustUtils().isRcsSwitchOn()) {
            Uri uri = URI_CONVERSATIONS_MARK_AS_READ;
            String selection = "unread_count > 0";
            if (runningMode == 4) {
                selection = selection + " AND number_type=2";
            } else if (runningMode == 5) {
                selection = selection + " AND number_type=1";
            }
            asyncQueryHandler = handler;
            i = token;
            asyncQueryHandler.startQuery(i, null, uri, new String[]{"sum(unread_count)"}, selection, null, "date DESC");
        } else if (runningMode != 4 && runningMode != 5) {
            asyncQueryHandler = handler;
            i = token;
            asyncQueryHandler.startQuery(i, null, URI_MMS_RCS_UNREAD_COUNT, new String[]{"sum(unread_count)"}, null, null, "date DESC");
        }
    }

    public static void startQueryHaveLockedMessages(AsyncQueryHandler handler, Collection<Long> threadIds, int token) {
        handler.cancelOperation(token);
        AsyncQueryHandler asyncQueryHandler = handler;
        int i = token;
        Collection<Long> collection = threadIds;
        asyncQueryHandler.startQuery(i, collection, MmsSms.CONTENT_LOCKED_URI, ALL_THREADS_PROJECTION, getThreadsSelection("thread_id", threadIds), null, "date DESC");
    }

    public static void confirmDeleteThreads(AsyncQueryHandler handler, Collection<Long> threadIds, Collection<Long> notificationIds, int token) {
        if (notificationIds.size() != 0) {
            handler.cancelOperation(token);
            StringBuilder selection = new StringBuilder("number_type in (");
            int i = 0;
            for (Long longValue : notificationIds) {
                long id = longValue.longValue();
                if (id == -10000000011L) {
                    if (i > 0) {
                        selection.append(",");
                    }
                    selection.append(1);
                    i++;
                } else if (id == -10000000012L) {
                    if (i > 0) {
                        selection.append(",");
                    }
                    selection.append(2);
                    i++;
                }
            }
            selection.append(")");
            AsyncQueryHandler asyncQueryHandler = handler;
            int i2 = token;
            Collection<Long> collection = threadIds;
            asyncQueryHandler.startQuery(i2, collection, sAllThreadsUri, new String[]{"_id"}, selection.toString(), null, "date DESC");
        }
    }

    public static String getThreadsSelection(String collomn, Collection<Long> threadIds) {
        if (threadIds == null || threadIds.size() <= 0) {
            return "thread_id= -1";
        }
        StringBuilder buf = new StringBuilder(collomn).append(" in ( ");
        int i = 0;
        for (Long longValue : threadIds) {
            long threadId = longValue.longValue();
            int i2 = i + 1;
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(threadId);
            i = i2;
        }
        buf.append(" )");
        return buf.toString();
    }

    public static void startQueryHaveLockedMessages(AsyncQueryHandler handler, long threadId, int token) {
        Collection threadIds = null;
        if (threadId != -1) {
            threadIds = new ArrayList();
            threadIds.add(Long.valueOf(threadId));
        }
        startQueryHaveLockedMessages(handler, threadIds, token);
    }

    private static void fillFromCursor(Context context, Conversation conv, Cursor c, boolean allowQuery) {
        fillFromCursor(context, conv, c, allowQuery, c.hashCode());
    }

    public static String getSnippetFromCursor(Context context, Cursor c) {
        String snippet = MessageUtils.cleanseMmsSubject(context, MessageUtils.extractEncStrFromCursor(c, 4, 5));
        if (!TextUtils.isEmpty(snippet)) {
            return snippet;
        }
        if (RcsConversationUtils.getHwCustUtils() != null && RcsConversationUtils.getHwCustUtils().isRcsGroupThread(c)) {
            return snippet;
        }
        snippet = context.getString(R.string.no_subject_view);
        if (sHwCustConversation != null) {
            return sHwCustConversation.getDefaultEmptySubject(context, snippet);
        }
        return snippet;
    }

    private static void fillFromCursor(Context context, Conversation conv, Cursor c, boolean allowQuery, int updateHash) {
        boolean z = true;
        synchronized (conv) {
            if (conv.mUpdateHash != updateHash) {
                boolean z2;
                conv.mUpdateHash = updateHash;
                conv.mThreadId = c.getLong(0);
                conv.mDate = c.getLong(1);
                conv.mMessageCount = (int) c.getLong(2);
                conv.setSubId((int) c.getLong(9));
                conv.setHasUnreadMessages(c.getInt(6) == 0);
                if (c.getInt(7) != 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                conv.setHasErrorMessages(z2);
                if (c.getInt(8) != 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                conv.setHasAttachment(z2);
                if (c.getInt(7) != 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                conv.setLastMessageError(z2);
                conv.setUnreadMessageCount(c.getInt(11));
                conv.setPriority(c.getInt(12));
                int numberType = c.getInt(13);
                if (numberType == 0 || !SmartArchiveSettingUtils.isSmartArchiveEnabled(context)) {
                    conv.setNumberType(0);
                    if (numberType == 0) {
                        z = false;
                    }
                    conv.setIsServiceNumber(z);
                } else {
                    conv.setNumberType(numberType);
                    conv.setIsServiceNumber(true);
                    if (1 == conv.getNumberType() && !SmartArchiveSettingUtils.isHwNotiReceived()) {
                        SmartArchiveSettingUtils.setHasHwNotice(context, true);
                    }
                }
                conv.setPhoneType(numberType);
                conv.setThumbnail(c);
            }
        }
        String spaceIds = "";
        if (!(conv.mThreadId == -10000000012L || conv.mThreadId == -10000000011L)) {
            spaceIds = c.getString(3);
        }
        ContactList recipients;
        if (conv.getHwCust() != null && conv.getHwCust().isRcsSwitchOn()) {
            recipients = conv.getHwCust().getRecipients(spaceIds, allowQuery, context);
            synchronized (conv) {
                conv.getHwCust().setRcsThreadType(c);
                conv.getHwCust().setHasUndelivered(c);
                conv.getHwCust().setFileType(c);
                conv.getHwCust().setRcsGroupChatId(spaceIds);
                conv.mRecipients = recipients;
            }
        } else if (!RecipientIdCache.isContactsDataMatch(spaceIds, conv.mRecipients)) {
            recipients = ContactList.getByIds(spaceIds, allowQuery);
            synchronized (conv) {
                conv.mRecipients = recipients;
            }
        } else {
            return;
        }
    }

    public static void clear(Context context) {
        HwBackgroundLoader.getInst().reloadData(8);
    }

    public static void markAllConversationsAsSeen(final Context context) {
        if (MmsConfig.isSmsEnabled(context)) {
            HwBackgroundLoader.getInst().postTaskDelayed(new Runnable() {
                public String toString() {
                    return "Conversation.markAllConversationsAsSeen";
                }

                public void run() {
                    Conversation.blockingMarkAllSmsMessagesAsSeen(context);
                    Conversation.blockingMarkAllMmsMessagesAsSeen(context);
                    if (RcsConversationUtils.getHwCustUtils() != null) {
                        RcsConversationUtils.getHwCustUtils().blockingMarkAllOtherMessagesAsSeen(context, Conversation.SEEN_PROJECTION);
                    }
                    MessagingNotification.blockingUpdateAllNotifications(context, -2);
                }
            }, 1500);
        }
    }

    private static void blockingMarkAllSmsMessagesAsSeen(Context context) {
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = SqliteWrapper.query(context, Inbox.CONTENT_URI, SEEN_PROJECTION, "seen=0", null, null);
            if (cursor != null) {
                count = cursor.getCount();
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception exception) {
            MLog.e("Mms_conv", "IllegalStateException: unstableCount < 0: -1:" + exception);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (count != 0) {
            if (MLog.isLoggable("Mms_app", 2)) {
                MLog.d("Mms_conv", "mark " + count + " SMS msgs as seen");
            }
            ContentValues values = new ContentValues(1);
            values.put("seen", Integer.valueOf(1));
            SqliteWrapper.update(context, Inbox.CONTENT_URI, values, "seen=0", null);
        }
    }

    private static void blockingMarkAllMmsMessagesAsSeen(Context context) {
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(context, Mms.Inbox.CONTENT_URI, SEEN_PROJECTION, "seen=0", null, null);
            if (cursor != null) {
                int count = cursor.getCount();
                if (cursor != null) {
                    cursor.close();
                }
                if (count != 0) {
                    if (MLog.isLoggable("Mms_app", 2)) {
                        MLog.d("Mms_conv", "mark " + count + " MMS msgs as seen");
                    }
                    ContentValues values = new ContentValues(1);
                    values.put("seen", Integer.valueOf(1));
                    SqliteWrapper.update(context, Mms.Inbox.CONTENT_URI, values, "seen=0", null);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void cacheAllThreads(Context context) {
        Thread.currentThread().setPriority(1);
        int updateId = sUIUpdateHash;
        MLog.i("Mms_conv", "Conversation cacheAllThreads start priority = " + Thread.currentThread().getPriority());
        synchronized (Cache.getInstance()) {
            if (sLoadingThreads) {
                return;
            }
            sLoadingThreads = true;
        }
        Cache.keepOnly(threadsOnDisk);
        if (RcsConversationUtils.getHwCustUtils() == null || !RcsConversationUtils.getHwCustUtils().isRcsSwitchOn()) {
            freshPinupCache(context);
        } else {
            synchronized (mPinupIds) {
                RcsConversationUtils.getHwCustUtils().freshPinupCache(context, mPinupIds, getPinUpArchiveIds(context));
            }
        }
        Cache.getInstance().mHasThreadsCached = true;
        if (MLog.isLoggable("Mms_threadcache", 2)) {
            LogTag.debug("[Conversation] cacheAllThreads: finished", new Object[0]);
            Cache.dumpCache();
        }
        HwBackgroundLoader.getInst().loadDataDelayed(2, 300);
        MLog.i("Mms_conv", "Conversation cacheAllThreads finish");
    }

    public static boolean hasAllThreadsCached() {
        return Cache.getInstance().mHasThreadsCached;
    }

    private boolean loadFromThreadId(long threadId, boolean allowQuery) {
        Cursor c = SqliteWrapper.query(getContext(), sAllThreadsUri, ALL_THREADS_PROJECTION, "_id=" + Long.toString(threadId), null, null);
        if (c == null) {
            return false;
        }
        try {
            if (c.moveToFirst()) {
                fillFromCursor(getContext(), this, c, allowQuery);
                if (threadId != getThreadId()) {
                    LogTag.error("loadFromThreadId: fillFromCursor returned differnt thread_id! threadId=" + threadId + ", mThreadId=" + getThreadId());
                }
                c.close();
                return true;
            }
            LogTag.error("loadFromThreadId: Can't find thread ID " + threadId);
            c.close();
            return false;
        } catch (Throwable th) {
            c.close();
        }
    }

    public static String getRecipients(Uri uri) {
        String base = uri.getSchemeSpecificPart();
        if (base == null) {
            MLog.e("Mms_conv", "getRecipients(Uri uri) get error uri");
            return "";
        }
        int pos = base.indexOf(63);
        if (pos != -1) {
            base = base.substring(0, pos);
        }
        return base;
    }

    public static void dump() {
        Cache.dumpCache();
    }

    public static void dumpThreadsTable(Context context) {
        LogTag.debug("**** Dump of threads table ****", new Object[0]);
        Cursor c = SqliteWrapper.query(context, sAllThreadsUri, ALL_THREADS_PROJECTION, null, null, "date ASC");
        if (c != null) {
            try {
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    MLog.d("Mms_conv", "dumpThreadsTable threadId: " + c.getLong(0) + " " + "date" + " : " + c.getLong(1) + " " + "message_count" + " : " + c.getInt(2) + " " + "read" + " : " + c.getInt(6) + " " + "error" + " : " + c.getInt(7) + " " + "has_attachment" + " : " + c.getInt(8) + " " + "recipient_ids" + " : " + c.getString(3));
                }
            } finally {
                c.close();
            }
        }
    }

    public static String verifySingleRecipient(Context context, long threadId, String recipientStr) {
        if (threadId <= 0) {
            LogTag.error("verifySingleRecipient threadId is ZERO, recipient");
            LogTag.dumpInternalTables(context);
            return recipientStr;
        }
        Cursor c = SqliteWrapper.query(context, sAllThreadsUri, ALL_THREADS_PROJECTION, "_id=" + Long.toString(threadId), null, null);
        if (c == null) {
            LogTag.error("verifySingleRecipient threadId: " + threadId + " resulted in NULL cursor");
            LogTag.dumpInternalTables(context);
            return recipientStr;
        }
        try {
            if (c.moveToFirst()) {
                String recipientIds = c.getString(3);
                c.close();
                String[] ids = recipientIds.split(" ");
                if (ids.length != 1) {
                    return recipientStr;
                }
                String address = RecipientIdCache.getSingleAddressFromCanonicalAddressInDb(context, ids[0]);
                if (TextUtils.isEmpty(address)) {
                    LogTag.error("verifySingleRecipient threadId: " + threadId + " getSingleNumberFromCanonicalAddresses returned empty " + "recipientIds for: " + recipientIds);
                    LogTag.dumpInternalTables(context);
                    return recipientStr;
                } else if (PhoneNumberUtils.compareLoosely(recipientStr, address)) {
                    return recipientStr;
                } else {
                    if (context instanceof Activity) {
                        LogTag.warnPossibleRecipientMismatch("verifySingleRecipient for threadId: " + threadId, (Activity) context);
                    }
                    LogTag.dumpInternalTables(context);
                    if (MLog.isLoggable("Mms_threadcache", 2)) {
                        LogTag.debug("verifySingleRecipient for threadId: " + threadId, new Object[0]);
                    }
                    return address;
                }
            }
            LogTag.error("verifySingleRecipient threadId: " + threadId + " can't moveToFirst");
            LogTag.dumpInternalTables(context);
            return recipientStr;
        } finally {
            c.close();
        }
    }

    public static Collection<Conversation> getConversations() {
        if (RcsConversationUtils.getHwCustUtils() == null || !RcsConversationUtils.getHwCustUtils().isRcsSwitchOn()) {
            return Cache.getCache();
        }
        return RcsConversationUtils.getHwCustUtils().getAllConversation();
    }

    public boolean isPrivacyConversation(Context context) {
        return PrivacyModeReceiver.isPrivacyThread(context, getThreadId());
    }

    private static Context checkContextType(Context context) {
        return context.getApplicationContext();
    }

    public synchronized Uri getGroupMessageUri() {
        if (this.mThreadId < 0) {
            return null;
        }
        return ContentUris.withAppendedId(Uri.withAppendedPath(MmsSms.CONTENT_URI, "conversations_group_id"), this.mThreadId);
    }

    public static void pinConversation(final Context context, final Collection<Long> threadIds, final boolean isPinup) {
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                if (threadIds.size() == 0) {
                    MLog.d("Mms_conv", "threadIds size 0");
                    return;
                }
                String callingPkg = context.getPackageName();
                Bundle extras = new Bundle();
                long[] ids = new long[threadIds.size()];
                int i = 0;
                for (Long longValue : threadIds) {
                    ids[i] = longValue.longValue();
                    i++;
                }
                extras.putLongArray("thread_ids", ids);
                extras.putBoolean("pin_up", isPinup);
                Bundle result = SqliteWrapper.call(context, MmsSms.CONTENT_URI, callingPkg, "method_pin_up", null, extras);
                if (result != null && result.getBoolean("call_result")) {
                    Handler uIHandler = HwBackgroundLoader.getUIHandler();
                    final boolean z = isPinup;
                    final Collection collection = threadIds;
                    uIHandler.post(new Runnable() {
                        public void run() {
                            Conversation.updatePinupCache(new PinUpdateRequeset(z, collection));
                        }
                    });
                }
            }
        });
    }

    private static long[] getPinUpArchiveIds(Context context) {
        IContentProvider icp = context.getContentResolver().acquireProvider(MmsSms.CONTENT_URI);
        if (icp == null) {
            return new long[0];
        }
        try {
            Bundle result = icp.call(context.getPackageName(), "method_get_archive_pin_ids", null, null);
            if (result == null) {
                return new long[0];
            }
            return result.getLongArray("archive_pin_ids");
        } catch (Exception e) {
            MLog.e("Mms_conv", "call METHOD_GET_PIN_UP_IDS excetpion : " + e.getMessage());
            return new long[0];
        }
    }

    public static boolean hasUnpinnedConversation(Long[] threadIds) {
        synchronized (mPinupIds) {
            int length = threadIds.length;
            int i = 0;
            while (i < length) {
                if (mPinupIds.contains(threadIds[i])) {
                    i++;
                } else {
                    return true;
                }
            }
            return false;
        }
    }

    private static void freshPinupCache(Context context) {
        long[] pinUpArchiveIds = getPinUpArchiveIds(context);
        Collection<Conversation> allConversatons = getConversations();
        synchronized (mPinupIds) {
            mPinupIds.clear();
            for (Conversation c : allConversatons) {
                if (c.getPriority() == 2) {
                    mPinupIds.add(Long.valueOf(c.getThreadId()));
                }
            }
            if (pinUpArchiveIds != null) {
                for (long id : pinUpArchiveIds) {
                    mPinupIds.add(Long.valueOf(id));
                }
            }
        }
    }

    public static void updatePinupCache(Object obj) {
        if (obj instanceof PinUpdateRequeset) {
            PinUpdateRequeset req = (PinUpdateRequeset) obj;
            MmsPermReceiver.noticeThreadPinupState(req);
            updatePinupCacheWithoutNotice(req);
        }
    }

    public static void updatePinupCacheWithoutNotice(PinUpdateRequeset req) {
        synchronized (mPinupIds) {
            for (Long id : req.mDatas) {
                if (req.mIsAdd) {
                    mPinupIds.add(id);
                } else {
                    mPinupIds.remove(id);
                }
            }
            MLog.d("Mms_conv", "conversation pinup size " + mPinupIds.size());
        }
    }

    public static boolean isPinned(long id) {
        boolean contains;
        synchronized (mPinupIds) {
            contains = mPinupIds.contains(Long.valueOf(id));
        }
        return contains;
    }

    public static boolean isContactChanged(Contact updated, Conversation cov) {
        Conversation covTemp = cov;
        if (cov == null) {
            return false;
        }
        if (updated == null) {
            return true;
        }
        for (Contact c : cov.getRecipients()) {
            if (c.getKey() == updated.getKey()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasUnreadMsg() {
        return sHasUnreadMsg;
    }

    public static void sethasUnreadMsg(boolean hasUnread) {
        sHasUnreadMsg = hasUnread;
    }

    public static boolean isGroupConversation(Context context, long tid) {
        return getGroupMemberCount(context, tid) > 1;
    }

    public static int getGroupMemberCount(Context context, long tid) {
        if (tid <= 0) {
            return 1;
        }
        Cache.getInstance();
        Conversation conv = Cache.get(tid);
        if (conv != null) {
            return conv.getRecipients().size();
        }
        String spaceSepIds = getThreadData(context, tid, "recipient_ids");
        if (TextUtils.isEmpty(spaceSepIds)) {
            return 1;
        }
        return ContactList.getByIds(spaceSepIds, false).size();
    }

    public static String getThreadData(Context context, long thread_id, String dataName) {
        Cursor cursor = null;
        try {
            Context context2 = context;
            cursor = SqliteWrapper.query(context2, context.getContentResolver(), sAllThreadsUri, new String[]{dataName}, "_id=?", new String[]{String.valueOf(thread_id)}, null);
            if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            String string = cursor.getString(0);
            if (cursor != null) {
                cursor.close();
            }
            return string;
        } catch (Exception e) {
            MLog.d("Mms_conv", "getThreadData fail for " + thread_id);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void checkPrefix(Context context) {
        if (resPrefixFromBegin == null) {
            resPrefixFromBegin = context.getResources().getString(R.string.snippet_prefix_begin);
            resPrefixFromEnd = context.getResources().getString(R.string.snippet_prefix_end);
        }
    }

    public static String updateHwSenderName(long threadId, String snippet) {
        String hwName = getSenderNameFromMessageBody(snippet);
        synchronized (mHwMessageName) {
            Object obj;
            MLog.d("Mms_conv", "update conversation's name for hw sender" + threadId);
            LongSparseArray longSparseArray = mHwMessageName;
            if (TextUtils.isEmpty(hwName)) {
                obj = "";
            } else {
                String str = hwName;
            }
            longSparseArray.put(threadId, obj);
        }
        return hwName;
    }

    public static String getFromNumberForHw(long tid) {
        String str;
        synchronized (mHwMessageName) {
            str = (String) mHwMessageName.get(tid);
        }
        return str;
    }

    public String getFromNumberForHw(String snippet) {
        return getFromNumberForHw(null, snippet, null);
    }

    public String getFromNumberForHw(final Context context, String snippet, final UpdateListener updater) {
        if (this.mRecipients.size() != 1 || this.mThreadId <= 0) {
            MLog.e("Mms_conv", "Call getFromNumberForHw for group-msg or invalide thread-id=" + this.mThreadId);
            return null;
        } else if (((Contact) this.mRecipients.get(0)).existsInDatabase()) {
            MLog.v("Mms_conv", "Huawei number  stored in Contacts?");
            return null;
        } else {
            String name = getFromNumberForHw(this.mThreadId);
            if (name != null) {
                if ("".equals(name)) {
                    name = null;
                }
                return name;
            } else if (getMessageCount() == 1) {
                return updateHwSenderName(this.mThreadId, snippet);
            } else {
                if (context == null) {
                    return null;
                }
                if (updater == null) {
                    return getSendNameFromDB(context, this.mThreadId);
                }
                Contact.pushTask(new Runnable() {
                    public void run() {
                        Conversation.getSendNameFromDB(context, Conversation.this.mThreadId);
                        Handler uIHandler = HwBackgroundLoader.getUIHandler();
                        final UpdateListener updateListener = updater;
                        uIHandler.post(new Runnable() {
                            public void run() {
                                MLog.d("Mms_conv", "notice for huawei sender");
                                updateListener.onUpdate((Contact) Conversation.this.mRecipients.get(0));
                            }
                        });
                    }
                });
                return null;
            }
        }
    }

    private static final String getSendNameFromDB(Context context, long threadId) {
        Cursor cursor = null;
        try {
            Context context2 = context;
            cursor = SqliteWrapper.query(context2, context.getContentResolver(), Inbox.CONTENT_URI, new String[]{"body"}, "thread_id = " + threadId, null, "_id desc limit 1");
            String updateHwSenderName;
            if (cursor == null || cursor.getCount() == 0) {
                MLog.e("Mms_conv", "Can't get first sms-msg for thread :" + threadId);
                updateHwSenderName = updateHwSenderName(threadId, "");
                if (cursor != null) {
                    cursor.close();
                }
                return updateHwSenderName;
            } else if (cursor.moveToFirst()) {
                updateHwSenderName = updateHwSenderName(threadId, cursor.getString(0));
                if (cursor != null) {
                    cursor.close();
                }
                return updateHwSenderName;
            } else {
                if (cursor != null) {
                    cursor.close();
                }
                return "";
            }
        } catch (SQLException e) {
            MLog.e("Mms_conv", "Query for HwMessage fail.", (Throwable) e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String getSenderNameFromMessageBody(String snippet) {
        if (snippet == null || snippet.length() < 3) {
            return null;
        }
        String strPrefix = null;
        if (startsWithIgnoreCase(snippet, "From")) {
            strPrefix = "From";
        }
        if (strPrefix == null && !"From".equals(resPrefixFromBegin) && startsWithIgnoreCase(snippet, resPrefixFromBegin)) {
            strPrefix = resPrefixFromBegin;
        }
        if (strPrefix == null) {
            return null;
        }
        int endIndex = Integer.MAX_VALUE;
        if (!TextUtils.isEmpty(resPrefixFromEnd)) {
            endIndex = snippet.indexOf(resPrefixFromEnd, strPrefix.length());
            if (endIndex == -1) {
                endIndex = Integer.MAX_VALUE;
            }
        }
        if (Integer.MAX_VALUE == endIndex || !":".equals(resPrefixFromEnd)) {
            int end = snippet.indexOf(":", strPrefix.length());
            if (end > 0 && end < endIndex) {
                endIndex = end;
            }
        }
        if (Integer.MAX_VALUE == endIndex) {
            return null;
        }
        return getSubStringWithoutSpace(snippet, strPrefix.length(), endIndex);
    }

    private static final String getSubStringWithoutSpace(String str, int begin, int end) {
        int len = end;
        int idx = begin;
        while (idx < end && str.charAt(idx) == ' ') {
            idx++;
        }
        len = end - 1;
        while (len > idx && str.charAt(len) == ' ') {
            len--;
        }
        return str.substring(idx, len + 1);
    }

    public static boolean startsWithIgnoreCase(String str, String prefix) {
        if (str == null || prefix == null) {
            return false;
        }
        int len = prefix.length();
        if (len == 0 || str.length() < len) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            char a1 = str.charAt(i);
            char a2 = prefix.charAt(i);
            if (a1 != a2 && foldCase(a1) != foldCase(a2)) {
                return false;
            }
        }
        return true;
    }

    private static char foldCase(char ch) {
        if (ch >= '') {
            return Character.toLowerCase(Character.toUpperCase(ch));
        }
        if ('A' > ch || ch > 'Z') {
            return ch;
        }
        return (char) (ch + 32);
    }

    public static ArrayList<String> getAddressesByThreadId(Context context, long threadId) {
        ArrayList<String> numbers = new ArrayList();
        Cursor cursor = null;
        String str = null;
        try {
            cursor = SqliteWrapper.query(context, sAllThreadsUri, new String[]{"recipient_ids"}, "_id=" + Long.toString(threadId), null, null);
            if (cursor != null && cursor.moveToFirst()) {
                str = cursor.getString(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            MLog.e("Mms_conv", "getAddressesByThreadId::query recipient ids occur exception: " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (str == null) {
            return numbers;
        }
        String[] ids = str.split(" ");
        StringBuilder selection = new StringBuilder();
        long length = (long) ids.length;
        if (1 == length) {
            selection.append("_id=").append(ids[0]);
        } else {
            selection.append("_id in (");
            for (int i = 0; ((long) i) < length; i++) {
                selection.append(ids[i]);
                if (((long) i) != length - 1) {
                    selection.append(",");
                } else {
                    selection.append(")");
                }
            }
        }
        Cursor cursor2 = null;
        try {
            cursor2 = SqliteWrapper.query(context, RecipientIdCache.getCanonicalAddressUri(), null, selection.toString(), null, null);
            if (cursor2 == null || !cursor2.moveToFirst()) {
                if (cursor2 != null) {
                    cursor2.close();
                }
                return numbers;
            }
            do {
                numbers.add(cursor2.getString(1));
            } while (cursor2.moveToNext());
            if (cursor2 != null) {
                cursor2.close();
            }
            return numbers;
        } catch (Exception e2) {
            MLog.e("Mms_conv", "getAddressesByThreadId::query numbers occur exception: " + e2);
            if (cursor2 != null) {
                cursor2.close();
            }
        } catch (Throwable th2) {
            if (cursor2 != null) {
                cursor2.close();
            }
        }
    }

    private Conversation(Context context, long threadId, boolean allowQuery, ParmWrapper wrapper) {
        this.mHwCustConversation = null;
        this.mPhoneType = -1;
        this.mFlag = 0;
        this.mUpdateHash = 0;
        this.mHwCustConversationObj = (HwCustConversation) HwCustUtils.createObj(HwCustConversation.class, new Object[0]);
        createHwCustConversation();
        if (!loadFromThreadId(threadId, allowQuery, wrapper)) {
            this.mRecipients = new ContactList();
            this.mThreadId = 0;
        }
    }

    public static Conversation get(Context context, long threadId, boolean allowQuery, ParmWrapper wrapper) {
        context = checkContextType(context);
        Conversation conv = Cache.get(threadId);
        if (conv != null) {
            return conv;
        }
        if (wrapper.whichTable != null) {
            conv = new Conversation(context, threadId, allowQuery, new ParmWrapper(null, wrapper.whichTable));
        } else if (wrapper.threadType != null) {
            conv = new Conversation(context, threadId, allowQuery, new ParmWrapper(wrapper.threadType, null));
        } else {
            MLog.i("Mms_conv", "wrapper error threadid = : " + threadId);
            return new Conversation(context);
        }
        return conv;
    }

    private boolean loadFromThreadId(long threadId, boolean allowQuery, ParmWrapper wrapper) {
        Cursor c = null;
        synchronized (this) {
            if (this.mHwCustConversation != null) {
                c = this.mHwCustConversation.getOtherTypeCursorFromThreadId(getContext(), threadId, wrapper, ALL_THREADS_PROJECTION);
            }
        }
        if (c == null) {
            return false;
        }
        try {
            if (c.moveToFirst()) {
                fillFromCursor(getContext(), this, c, allowQuery);
                if (threadId != getThreadId()) {
                    LogTag.error("loadFromThreadId: fillFromCursor returned differnt thread_id! threadId=" + threadId + ", mThreadId=" + getThreadId());
                }
                c.close();
                return true;
            }
            MLog.w("Mms_conv", "loadFromThreadId: Can't find thread ID " + threadId);
            c.close();
            return false;
        } catch (Throwable th) {
            c.close();
        }
    }

    public synchronized RcsConversation getHwCust() {
        return this.mHwCustConversation;
    }

    public static Object getDeletingThreadsLock() {
        Object obj;
        synchronized (sDeletingThreadsLock) {
            obj = sDeletingThreadsLock;
        }
        return obj;
    }

    public static boolean getDeletingThreads() {
        boolean z;
        synchronized (sDeletingThreadsLock) {
            z = sDeletingThreads;
        }
        return z;
    }

    public static void setDeletingThreads(boolean deleting) {
        synchronized (sDeletingThreadsLock) {
            sDeletingThreads = deleting;
        }
    }

    public synchronized void setThreadId(long threadId) {
        this.mThreadId = threadId;
    }

    public static Set<Long> getPinupIds() {
        Set set;
        synchronized (mPinupIds) {
            set = mPinupIds;
        }
        return set;
    }

    private synchronized void createHwCustConversation() {
        if (MmsConfig.isRcsSwitchOn() && this.mHwCustConversation == null) {
            this.mHwCustConversation = new RcsConversation();
        }
    }

    public static void updateHwSendName(Context context, long tid, String number, String snippet) {
        if (tid > 0 && NumberUtils.isHwMessageNumber(number)) {
            updateHwSendNameWithoutNotice(context, tid, number, snippet);
            MmsPermReceiver.noticeHwSendName(context, tid, number, snippet);
        }
    }

    public static void updateHwSendNameWithoutNotice(Context context, long tid, String number, String snippet) {
        checkPrefix(context);
        updateHwSenderName(tid, snippet);
    }

    public static List<Long> getConversationIdFromCursor(Cursor cursor) {
        List<Long> ids = new ArrayList();
        cursor.moveToFirst();
        do {
            long threadId = cursor.getLong(0);
            if (threadId > 0) {
                ids.add(Long.valueOf(threadId));
            }
        } while (cursor.moveToNext());
        return ids;
    }

    public static Conversation getConvsationFromId(Context context, Cursor cursor, long tid) {
        cursor.moveToFirst();
        while (cursor.getLong(0) != tid) {
            if (!cursor.moveToNext()) {
                return null;
            }
        }
        return from(context, cursor);
    }

    public synchronized void setPhoneType(int phoneType) {
        this.mPhoneType = phoneType;
    }

    public synchronized int getPhoneType() {
        return this.mPhoneType;
    }

    public static void setContextForQuery(Context context) {
        mContext = context;
    }

    public boolean isGroupConversation() {
        return this.mRecipients != null && this.mRecipients.size() > 1;
    }
}
