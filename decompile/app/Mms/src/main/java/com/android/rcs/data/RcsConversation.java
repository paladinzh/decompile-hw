package com.android.rcs.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony.Threads;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.HwCustConversation.ParmWrapper;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.ConversationListAdapter;
import com.android.mms.util.DraftCache;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.data.RcsGroupCache.RcsGroupData;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.util.NumberUtils;
import com.huawei.rcs.util.RCSConst;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsTransaction;
import com.huawei.rcs.utils.RcseMmsExt;

public class RcsConversation {
    static final Uri GET_OR_CREATE_RCS_THEAD_URI = Uri.parse("content://rcsim/get_or_create_rcs_thread");
    private static final Uri GET_RCS_GROUP_MEMBERS_CONTACTS = Uri.parse("content://rcsim/rcs_groups/get_member_contact");
    private static final Uri GET_RCS_GROUP_SUBJECT_URI = Uri.parse("content://rcsim/rcs_groups/get_subject");
    static final Uri GET_RCS_THEAD_URI = Uri.parse("content://rcsim/get_rcs_thread");
    private static final String[] GROUPS_PROJECTION = new String[]{"subject", "status", "thread_id", "is_groupchat_notify_silent"};
    private static final Uri IM_GROUPCHAT_URI = Uri.parse("content://rcsim/rcs_groups");
    static final Uri RCSAllThreadsUri = RCSConst.RCS_URI_CONVERSATIONS.buildUpon().appendQueryParameter("seperate", "true").build();
    private static final String[] RCS_ALL_THREADS_PROJECTION = new String[]{"_id", "date", "message_count", "recipient_ids", "snippet", "snippet_cs", "read", "error", "has_attachment", "sub_id", "network_type", "unread_count", "priority", "number_type", "undelivered_status", "file_type", "thread_type"};
    private static final Uri RCS_CONVERSATION_URI = Uri.parse("content://rcsim/rcs_conversations");
    static final String[] RCS_PINUP_THREADS_PROJECTION = new String[]{"_id", "date", "priority", "thread_type"};
    public static final Uri sRcsAllMixedCommonThreadsUri = sRcsAllMixedThreads.buildUpon().appendQueryParameter("number_type", String.valueOf(255)).build();
    public static final Uri sRcsAllMixedThreads = Uri.parse("content://rcsim/conversationAll");
    static final Uri sRcsAllSeperateCommonThreadsUri = RCSAllThreadsUri.buildUpon().appendQueryParameter("number_type", String.valueOf(255)).build();
    private boolean isRcsEnable = RcsCommonConfig.isRCSSwitchOn();
    private ConversationListAdapter mAdapter = null;
    private int mFileType = 0;
    private String mGroupChatID = "";
    private long mGroupChatThreadId = -1;
    private String mGroupId = null;
    private boolean mHasUndelivered = false;
    private boolean mIsRcsThreadId = false;
    private int mRcsThreadType = 1;

    private boolean isThreadIdInRcsDB(long r12, android.content.Context r14) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x006e in list []
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
        r11 = this;
        r9 = 1;
        r8 = 0;
        if (r14 != 0) goto L_0x0005;
    L_0x0004:
        return r8;
    L_0x0005:
        r6 = 0;
        r0 = "content://rcsim/chat";	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r1 = android.net.Uri.parse(r0);	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r0 = 1;	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r2 = new java.lang.String[r0];	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r0 = "thread_id";	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r3 = 0;	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r2[r3] = r0;	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r0 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r0.<init>();	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r3 = "thread_id =";	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r0 = r0.append(r3);	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r0 = r0.append(r12);	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r3 = r0.toString();	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r4 = 0;	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r5 = 0;	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r0 = r14;	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r6 = com.huawei.cspcommon.ex.SqliteWrapper.query(r0, r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        if (r6 == 0) goto L_0x003f;	 Catch:{ Exception -> 0x005f, all -> 0x006f }
    L_0x0033:
        r0 = r6.getCount();	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        if (r0 <= 0) goto L_0x003f;
    L_0x0039:
        if (r6 == 0) goto L_0x003e;
    L_0x003b:
        r6.close();
    L_0x003e:
        return r9;
    L_0x003f:
        r0 = "RcsConversation";	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r1.<init>();	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r2 = "isThreadIdInRcsDB: Can't find thread ID =";	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r1 = r1.append(r12);	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r1 = r1.toString();	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        com.huawei.cspcommon.MLog.d(r0, r1);	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        if (r6 == 0) goto L_0x005e;
    L_0x005b:
        r6.close();
    L_0x005e:
        return r8;
    L_0x005f:
        r7 = move-exception;
        r0 = "RcsConversation";	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        r1 = "isThreadIdInRcsDB error";	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        com.huawei.cspcommon.MLog.e(r0, r1, r7);	 Catch:{ Exception -> 0x005f, all -> 0x006f }
        if (r6 == 0) goto L_0x006e;
    L_0x006b:
        r6.close();
    L_0x006e:
        return r8;
    L_0x006f:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0075;
    L_0x0072:
        r6.close();
    L_0x0075:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.rcs.data.RcsConversation.isThreadIdInRcsDB(long, android.content.Context):boolean");
    }

    public void setAdapter(ConversationListAdapter adapter) {
        this.mAdapter = adapter;
    }

    private boolean isScroll() {
        if (this.mAdapter != null) {
            return this.mAdapter.isScroll();
        }
        return false;
    }

    public boolean isRcsSwitchOn() {
        return this.isRcsEnable;
    }

    public void setIsRcsThreadId(boolean isRcs) {
        if (this.isRcsEnable) {
            this.mIsRcsThreadId = isRcs;
        }
    }

    public int getRcsThreadType() {
        if (this.isRcsEnable) {
            return this.mRcsThreadType;
        }
        return 0;
    }

    public void setRcsThreadType(int type) {
        this.mRcsThreadType = type;
    }

    public void setRcsThreadType(Cursor cursor) {
        if (this.isRcsEnable) {
            try {
                setRcsThreadType(cursor.getInt(cursor.getColumnIndexOrThrow("thread_type")));
            } catch (IllegalArgumentException ex) {
                MLog.e("RcsConversation", "setRcsThreadType" + ex.toString());
                setThreadTypeXmsOnly();
            }
        }
    }

    private void setThreadTypeXmsOnly() {
        if (this.isRcsEnable) {
            setRcsThreadType(1);
        }
    }

    public void setRcsGroupChatId(String id) {
        if (this.isRcsEnable) {
            if (!Contact.isEmailAddress(id) ? Contact.getHwCustContact().isGroupID(id) : false) {
                this.mRcsThreadType = 4;
                this.mGroupChatID = id;
            }
        }
    }

    public String getRcsGroupChatID() {
        if (this.isRcsEnable) {
            return this.mGroupChatID;
        }
        return null;
    }

    public void sendImReadReport(ContactList list, long threadId) {
        if (this.isRcsEnable && list.size() == 1 && threadId > 0) {
            RcsTransaction.sendImReadReport(((Contact) list.get(0)).getNumber());
        }
    }

    public boolean markAsReadDoInBackground(Uri threadUri, String[] UNREAD_PROJECTION, String UNREAD_SELECTION, ContentValues readContentValues, Conversation cov, Context ctx) {
        if (!this.isRcsEnable || ctx == null || cov == null) {
            return false;
        }
        Cursor cursor = null;
        long threadId = cov.getThreadId();
        long otherThreadId = -1;
        Cursor c1 = null;
        boolean needUpdate = true;
        boolean needUpdateOtherThread = false;
        boolean isUpdateXms = false;
        MLog.i("RcsConversation", "markAsReadDoInBackground ThreadType = " + this.mRcsThreadType);
        if (this.mRcsThreadType == 2) {
            cursor = SqliteWrapper.query(ctx, Uri.parse("content://rcsim/chat/"), UNREAD_PROJECTION, "thread_id = " + Long.toString(threadId) + " AND " + UNREAD_SELECTION, null, null);
            otherThreadId = RcsConversationUtils.getHwCustUtils().querySmsThreadIdWithAddress(RcsConversationUtils.getHwCustUtils().queryAddressWithid(threadId, 2, ctx), ctx);
            if (otherThreadId > 0) {
                c1 = SqliteWrapper.query(ctx, ContentUris.withAppendedId(Threads.CONTENT_URI, otherThreadId), UNREAD_PROJECTION, UNREAD_SELECTION, null, null);
            }
        } else if (this.mRcsThreadType == 1) {
            cursor = SqliteWrapper.query(ctx, threadUri, UNREAD_PROJECTION, UNREAD_SELECTION, null, null);
            otherThreadId = RcsConversationUtils.getHwCustUtils().queryChatThreadIdWithAddress(RcsConversationUtils.getHwCustUtils().queryAddressWithid(threadId, 1, ctx), ctx);
            if (otherThreadId > 0) {
                c1 = SqliteWrapper.query(ctx, Uri.parse("content://rcsim/chat/"), UNREAD_PROJECTION, "thread_id = " + Long.toString(otherThreadId) + " AND " + UNREAD_SELECTION, null, null);
            }
        } else if (this.mRcsThreadType == 4) {
            final String groupId = this.mGroupId;
            final Context context = ctx;
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    RcsConversation.this.updateRcsMessagesDBAsRead(groupId, context);
                }
            }, "RcsGroupChatComposeMessageActivity.markRcsGroupMessageAsRead");
            thread.setPriority(1);
            thread.start();
        }
        if (cursor != null) {
            try {
                needUpdate = cursor.getCount() > 0;
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        }
        if (c1 != null) {
            try {
                needUpdateOtherThread = c1.getCount() > 0;
                c1.close();
            } catch (Throwable th2) {
                c1.close();
            }
        }
        MLog.d("RcsConversation", "markAsReadDoInBackground needUpdate = " + needUpdate);
        MLog.d("RcsConversation", "markAsReadDoInBackground needUpdateOtherThread = " + needUpdateOtherThread);
        if (needUpdate) {
            if (this.mRcsThreadType == 1) {
                SqliteWrapper.update(ctx, threadUri, readContentValues, UNREAD_SELECTION, null);
                isUpdateXms = true;
            } else if (this.mRcsThreadType == 2) {
                SqliteWrapper.update(ctx, Uri.parse("content://rcsim/chat/"), readContentValues, UNREAD_SELECTION + "AND (thread_id  = ?)", new String[]{String.valueOf(threadId)});
            }
        }
        if (needUpdateOtherThread) {
            if (this.mRcsThreadType == 1) {
                SqliteWrapper.update(ctx, Uri.parse("content://rcsim/chat/"), readContentValues, UNREAD_SELECTION + "AND (thread_id  = ?)", new String[]{String.valueOf(otherThreadId)});
            } else if (this.mRcsThreadType == 2) {
                SqliteWrapper.update(ctx, ContentUris.withAppendedId(Threads.CONTENT_URI, otherThreadId), readContentValues, UNREAD_SELECTION, null);
                isUpdateXms = true;
            }
        }
        if (!isUpdateXms && (needUpdate || needUpdateOtherThread)) {
            MessagingNotification.blockingUpdateAllNotifications(ctx, -2);
        }
        return isUpdateXms;
    }

    private void updateRcsMessagesDBAsRead(String groupId, Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        int count = 0;
        long threadId = 0;
        try {
            Context context2 = context;
            cursor = SqliteWrapper.query(context2, resolver, Uri.parse("content://rcsim/rcs_groups"), null, "name = ?", new String[]{groupId}, null);
            if (cursor != null && cursor.getCount() == 1 && cursor.moveToFirst()) {
                threadId = cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"));
                cursor.close();
                cursor = SqliteWrapper.query(context, resolver, Uri.parse("content://rcsim/rcs_group_message"), new String[]{"read"}, "(type = 1 OR type = 101  OR type=4 OR type = 100)AND read = 0 AND thread_id = " + threadId, null, null);
                if (cursor != null) {
                    count = cursor.getCount();
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception exception) {
            MLog.e("RcsConversation", "IllegalStateException: unstableCount < 0: -1:" + exception);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (count != 0) {
            ContentValues values = new ContentValues(2);
            values.put("read", Integer.valueOf(1));
            values.put("seen", Integer.valueOf(1));
            SqliteWrapper.update(context, resolver, Uri.parse("content://rcsim/rcs_group_message"), values, "(type = 1 OR type = 101 OR type=4  OR type = 100 ) AND read = 0 AND thread_id = " + threadId, null);
        }
    }

    public void getEnsureThreadId(Conversation cov, Context ctx) {
        if (this.isRcsEnable && ctx != null && cov != null) {
            if (RcsProfile.getRcsService() != null && RcseMmsExt.isRcsMode()) {
                Cursor cursor = null;
                if (this.mRcsThreadType != 2 || cov.getThreadId() <= 0 || !isThreadIdInRcsDB(cov.getThreadId(), ctx)) {
                    try {
                        Context context = ctx;
                        cursor = SqliteWrapper.query(context, GET_OR_CREATE_RCS_THEAD_URI.buildUpon().appendPath(((Contact) cov.getRecipients().get(0)).getNumber()).build(), null, null, null, null);
                        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
                            cov.setThreadId(0);
                        } else {
                            cov.setThreadId(cursor.getLong(0));
                            MLog.d("RcsConversation", "getEnsureThreadId ensureThreadId mThreadId = " + cov.getThreadId());
                        }
                        this.mRcsThreadType = 2;
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (RuntimeException e) {
                        MLog.e("RcsConversation", "getEnsureThreadId error ");
                        cov.setThreadId(0);
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            }
            if (!RcseMmsExt.isRcsMode()) {
                if (this.mRcsThreadType == 2 || cov.getThreadId() <= 0 || !cov.isThreadIdInDB(cov.getThreadId())) {
                    try {
                        cov.setThreadId(Conversation.getOrCreateThreadId(ctx, cov.getRecipients()));
                        this.mRcsThreadType = 1;
                    } catch (Exception e2) {
                        MLog.e("RcsConversation", "getEnsureThreadId error " + e2.toString());
                        cov.setThreadId(0);
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hasDraft(Conversation cov, Context ctx) {
        if (!this.isRcsEnable || ctx == null || cov == null || getSmsThreadId(cov, ctx) == 0) {
            return false;
        }
        switch (this.mRcsThreadType) {
            case 1:
            case 2:
                return DraftCache.getInstance().hasDraft(getSmsThreadId(cov, ctx));
            case 4:
                if (DraftCache.getInstance().getHwCust() != null) {
                    return DraftCache.getInstance().getHwCust().hasGroupDraft(getSmsThreadId(cov, ctx));
                }
                break;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDraftState(boolean hasDraft, Conversation cov, Context ctx) {
        if (this.isRcsEnable && ctx != null && cov != null && getSmsThreadId(cov, ctx) != 0) {
            switch (this.mRcsThreadType) {
                case 1:
                case 2:
                    DraftCache.getInstance().setDraftState(getSmsThreadId(cov, ctx), hasDraft);
                    break;
            }
        }
    }

    public Cursor getOtherTypeCursorFromThreadId(Context context, long threadId, ParmWrapper wrapper, String[] ALL_THREADS_PROJECTION) {
        if (!this.isRcsEnable) {
            return null;
        }
        Cursor c = null;
        Context context2;
        if (wrapper.whichTable != null) {
            if (100 == wrapper.whichTable.intValue()) {
                Context context3 = context;
                c = SqliteWrapper.query(context3, context.getContentResolver(), ContentUris.withAppendedId(RCSConst.RCS_URI_CONVERSATIONS, threadId).buildUpon().appendQueryParameter("threadMod", "Im").appendQueryParameter("newMessage", "true").build(), RCS_ALL_THREADS_PROJECTION, null, null, null);
            } else {
                context2 = context;
                c = SqliteWrapper.query(context2, context.getContentResolver(), ContentUris.withAppendedId(RCSConst.RCS_URI_CONVERSATIONS, threadId).buildUpon().appendQueryParameter("getAll", "true").appendQueryParameter("threadType", "1").build(), RCS_ALL_THREADS_PROJECTION, null, null, null);
            }
        } else if (wrapper.threadType != null) {
            context2 = context;
            c = SqliteWrapper.query(context2, context.getContentResolver(), ContentUris.withAppendedId(RCSConst.RCS_URI_CONVERSATIONS, threadId).buildUpon().appendQueryParameter("getAll", "true").appendQueryParameter("threadType", String.valueOf(wrapper.threadType)).build(), RCS_ALL_THREADS_PROJECTION, null, null, null);
        }
        return c;
    }

    public long getRcsThreadId(Conversation cov) {
        if (!this.isRcsEnable) {
            return 0;
        }
        return RcsConversationUtils.judgeRcsThreadId(cov.getThreadId(), getRcsThreadType());
    }

    public long getSmsThreadId(Conversation cov, Context ctx) {
        if (!this.isRcsEnable || ctx == null || cov == null) {
            return 0;
        }
        if (this.mRcsThreadType == 1 || this.mRcsThreadType == 4) {
            return cov.getThreadId();
        }
        if (cov.getRecipients() == null || cov.getRecipients() == null || cov.getRecipients().size() == 0) {
            return 0;
        }
        Cursor cursor = null;
        long smsThreadId = 0;
        try {
            cursor = SqliteWrapper.query(ctx, RCSConst.RCS_URI_GET_SMS_THREAD.buildUpon().appendPath(PhoneNumberUtils.normalizeNumber(((Contact) cov.getRecipients().get(0)).getNumber())).build(), null, null, null, null);
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                smsThreadId = cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (IllegalArgumentException e) {
            MLog.e("RcsConversation", "getSmsThreadId error ");
            smsThreadId = 0;
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return smsThreadId;
    }

    public long getSmsThreadId(long threadId, Conversation cov, Context ctx) {
        if (this.isRcsEnable) {
            return getSmsThreadId(cov, ctx);
        }
        return threadId;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Uri getGroupMessageUri(Conversation cov, Context ctx) {
        if (!this.isRcsEnable || ctx == null || cov == null || cov.getThreadId() <= 0) {
            return null;
        }
        Uri resultUri;
        Object id = null;
        if (cov.getRecipients().size() == 1) {
            String phoneNumber = PhoneNumberUtils.normalizeNumber(((Contact) cov.getRecipients().get(0)).getNumber());
            if (TextUtils.isEmpty(phoneNumber)) {
                return null;
            }
            if (needQueryReciptId(ctx, cov)) {
                MLog.d("RcsConversation", "cov.getThreadId() <= 0");
                id = queryReciptIdFromNumber(ctx, phoneNumber);
            }
        }
        if (TextUtils.isEmpty(id)) {
            resultUri = ContentUris.withAppendedId(RCSConst.RCS_URI_CONVERSATIONS, cov.getThreadId()).buildUpon().appendQueryParameter("threadMod", "Mix").appendQueryParameter("threadType", String.valueOf(this.mRcsThreadType)).appendQueryParameter("isWithGroupId", "false").build();
        } else {
            resultUri = ContentUris.withAppendedId(RCSConst.RCS_URI_CONVERSATIONS, cov.getThreadId()).buildUpon().appendQueryParameter("threadMod", "Mix").appendQueryParameter("threadType", String.valueOf(this.mRcsThreadType)).appendQueryParameter("isWithGroupId", "false").appendQueryParameter("threadAddress", id).build();
        }
        return resultUri;
    }

    private boolean needQueryReciptId(Context ctx, Conversation conv) {
        if (conv.getThreadId() <= 0) {
            return true;
        }
        if (this.mRcsThreadType == 2 && !isThreadIdInRcsDB(conv.getThreadId(), ctx)) {
            return true;
        }
        if (this.mRcsThreadType != 1 || conv.isThreadIdInDB(conv.getThreadId())) {
            return false;
        }
        return true;
    }

    private String queryReciptIdFromNumber(Context ctx, String phoneNumber) {
        String str = null;
        Cursor cursor = null;
        boolean useStrictPhoneNumberComparation = ctx.getResources().getBoolean(17956931);
        try {
            cursor = SqliteWrapper.query(ctx, Uri.parse("content://mms-sms/canonical-addresses"), null, "(address = \"" + phoneNumber + "\"" + " OR PHONE_NUMBERS_EQUAL(address, " + phoneNumber + (useStrictPhoneNumberComparation ? ", 1))" : ", 0))"), null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                str = String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("_id")));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (IllegalArgumentException e) {
            MLog.e("RcsConversation", "getGroupMessageUri no column _id.");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return str;
    }

    public ContactList getRecipients(String spaceIds, boolean allowQuery, Context ctx) {
        if (!this.isRcsEnable || ctx == null) {
            return null;
        }
        ContactList recipients;
        if (!Contact.isEmailAddress(spaceIds) ? Contact.getHwCustContact().isGroupID(spaceIds) : false) {
            this.mGroupId = spaceIds;
            recipients = getContactByGroupID(spaceIds, ctx);
        } else {
            recipients = ContactList.getByIds(spaceIds, allowQuery);
        }
        return recipients;
    }

    private ContactList getContactByGroupID(String recipientsIds, Context ctx) {
        Exception e;
        String[] rcsIds;
        int i;
        String number;
        int i2;
        Throwable th;
        if (!this.isRcsEnable || ctx == null || recipientsIds == null) {
            return null;
        }
        ContactList list;
        String groupChatSubject;
        RcsGroupData groupData = RcsGroupCache.getInstance().getGroupData(recipientsIds);
        if (isScroll() && groupData != null) {
            list = getContactFromCache(recipientsIds, groupData);
            if (list.size() > 0) {
                return list;
            }
        }
        if (groupData == null) {
            groupData = new RcsGroupData();
            RcsGroupCache.getInstance().putGroupData(recipientsIds, groupData);
        }
        list = new ContactList();
        String groupChatSubject2 = "";
        int status = 1;
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(ctx, GET_RCS_GROUP_SUBJECT_URI.buildUpon().appendPath(recipientsIds).build(), GROUPS_PROJECTION, null, null, null);
            if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
                MLog.w("RcsConversation", "getContactByGroupID fillFromCursor: cannot find recipientIds by group chat name");
                groupChatSubject = groupChatSubject2;
            } else {
                groupChatSubject = cursor.getString(cursor.getColumnIndex("subject"));
                try {
                    status = cursor.getInt(cursor.getColumnIndex("status"));
                    if (TextUtils.isEmpty(groupChatSubject)) {
                        groupChatSubject = ctx.getString(R.string.chat_topic_default);
                    }
                    this.mGroupChatThreadId = cursor.getLong(cursor.getColumnIndex("thread_id"));
                    groupData.setSubject(groupChatSubject);
                    groupData.setGroupThreadId(this.mGroupChatThreadId);
                    groupData.setStatus(status);
                    groupData.setNotifySilent(cursor.getInt(cursor.getColumnIndex("is_groupchat_notify_silent")));
                } catch (Exception e2) {
                    e = e2;
                    try {
                        MLog.e("RcsConversation", "getContactByGroupID IllegalStateException: query group chat error: " + e);
                        if (cursor != null) {
                            cursor.close();
                        }
                        list.add(Contact.get(0, 0, recipientsIds, groupChatSubject));
                        cursor = SqliteWrapper.query(ctx, GET_RCS_GROUP_MEMBERS_CONTACTS.buildUpon().appendPath(recipientsIds).build(), null, null, null, null);
                        if (cursor != null) {
                            rcsIds = new String[cursor.getCount()];
                            i = 0;
                            while (cursor.moveToNext()) {
                                number = cursor.getString(cursor.getColumnIndex("rcs_id"));
                                i2 = i + 1;
                                rcsIds[i] = number;
                                list.add(Contact.get(number, false));
                                i = i2;
                            }
                            groupData.setRcsIds(rcsIds);
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (status == 1) {
                            list.add(Contact.getMe(false));
                        }
                        return list;
                    } catch (Throwable th2) {
                        th = th2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e3) {
            e = e3;
            groupChatSubject = groupChatSubject2;
            MLog.e("RcsConversation", "getContactByGroupID IllegalStateException: query group chat error: " + e);
            if (cursor != null) {
                cursor.close();
            }
            list.add(Contact.get(0, 0, recipientsIds, groupChatSubject));
            cursor = SqliteWrapper.query(ctx, GET_RCS_GROUP_MEMBERS_CONTACTS.buildUpon().appendPath(recipientsIds).build(), null, null, null, null);
            if (cursor != null) {
                rcsIds = new String[cursor.getCount()];
                i = 0;
                while (cursor.moveToNext()) {
                    number = cursor.getString(cursor.getColumnIndex("rcs_id"));
                    i2 = i + 1;
                    rcsIds[i] = number;
                    list.add(Contact.get(number, false));
                    i = i2;
                }
                groupData.setRcsIds(rcsIds);
            }
            if (cursor != null) {
                cursor.close();
            }
            if (status == 1) {
                list.add(Contact.getMe(false));
            }
            return list;
        } catch (Throwable th3) {
            th = th3;
            groupChatSubject = groupChatSubject2;
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        list.add(Contact.get(0, 0, recipientsIds, groupChatSubject));
        try {
            cursor = SqliteWrapper.query(ctx, GET_RCS_GROUP_MEMBERS_CONTACTS.buildUpon().appendPath(recipientsIds).build(), null, null, null, null);
            if (cursor != null) {
                rcsIds = new String[cursor.getCount()];
                i = 0;
                while (cursor.moveToNext()) {
                    number = cursor.getString(cursor.getColumnIndex("rcs_id"));
                    i2 = i + 1;
                    rcsIds[i] = number;
                    list.add(Contact.get(number, false));
                    i = i2;
                }
                groupData.setRcsIds(rcsIds);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e4) {
            MLog.e("RcsConversation", "getContactByGroupID IllegalStateException: query group chat members address error: " + e4);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th4) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (status == 1) {
            list.add(Contact.getMe(false));
        }
        return list;
    }

    public long ensureFtSendThreadId(Conversation cov, Context ctx) {
        if (!this.isRcsEnable || ctx == null || cov == null) {
            return 0;
        }
        if (RcsProfile.getRcsService() != null) {
            Cursor cursor = null;
            try {
                if (RcsProfile.isImAvailable(PhoneNumberUtils.normalizeNumber(((Contact) cov.getRecipients().get(0)).getNumber()))) {
                    Context context = ctx;
                    cursor = SqliteWrapper.query(context, GET_OR_CREATE_RCS_THEAD_URI.buildUpon().appendPath(((Contact) cov.getRecipients().get(0)).getNumber()).build(), null, null, null, null);
                    if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
                        cov.setThreadId(0);
                    } else {
                        cov.setThreadId(cursor.getLong(0));
                        MLog.d("RcsConversation", "ensureFtSendThreadId mThreadId = " + cov.getThreadId());
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (RuntimeException e) {
                MLog.e("RcsConversation", "ensureFtSendThreadId error ");
                cov.setThreadId(0);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return cov.getThreadId();
    }

    public long ensureDraftThreadId(Conversation cov, Context ctx) {
        if (!this.isRcsEnable || cov == null || ctx == null) {
            return 0;
        }
        setThreadTypeXmsOnly();
        try {
            cov.setThreadId(Conversation.getOrCreateThreadId(ctx, cov.getRecipients()));
        } catch (Exception e) {
            MLog.e("RcsConversation", "ensureDraftThreadId Conversation getOrCreateThreadId error");
            cov.setThreadId(0);
        }
        return cov.getThreadId();
    }

    public synchronized void setHasUndelivered(Cursor cursor) {
        boolean z = false;
        synchronized (this) {
            if (this.isRcsEnable) {
                try {
                    if (cursor.getInt(cursor.getColumnIndexOrThrow("undelivered_status")) > 0) {
                        z = true;
                    }
                    this.mHasUndelivered = z;
                    MLog.d("RcsConversation", "setHasUndelivered hasUndelivered = " + this.mHasUndelivered);
                } catch (IllegalArgumentException e) {
                    MLog.e("RcsConversation", "setHasUndelivered setHasUndelivered error");
                }
            } else {
                return;
            }
        }
    }

    public synchronized boolean hasUndeliveredMsg() {
        if (!this.isRcsEnable) {
            return false;
        }
        return this.mHasUndelivered;
    }

    public synchronized void setFileType(Cursor cursor) {
        if (this.isRcsEnable) {
            try {
                this.mFileType = cursor.getInt(cursor.getColumnIndexOrThrow("file_type"));
                MLog.d("RcsConversation", "setFileType mFileType = " + this.mFileType);
            } catch (IllegalArgumentException e) {
                MLog.e("RcsConversation", "setFileType fileType error");
            }
        }
    }

    public synchronized int getFileType() {
        if (!this.isRcsEnable) {
            return 0;
        }
        return this.mFileType;
    }

    public String getGroupId() {
        if (!this.isRcsEnable) {
            return "";
        }
        MLog.d("RcsConversation", "setFileType groupId:" + this.mGroupId);
        return this.mGroupId;
    }

    public void setGroupId(String groupId) {
        this.mGroupId = groupId;
    }

    public boolean isGroupChat() {
        boolean z = false;
        if (!this.isRcsEnable) {
            return false;
        }
        if (this.mRcsThreadType == 4) {
            z = true;
        }
        return z;
    }

    public boolean isXms() {
        boolean z = true;
        if (!this.isRcsEnable) {
            return true;
        }
        if (this.mRcsThreadType != 1) {
            z = false;
        }
        return z;
    }

    public long getGroupChatThreadId() {
        if (this.isRcsEnable) {
            return this.mGroupChatThreadId;
        }
        return -1;
    }

    public static String[] getRcsAllThreadProjection() {
        return (String[]) RCS_ALL_THREADS_PROJECTION.clone();
    }

    public boolean isGroupChatNotDisturb(Context context, long threadId) {
        boolean z = true;
        if (context == null || this.mGroupId == null) {
            MLog.w("RcsConversation", "isGroupChatNotDisturb context is null");
            return false;
        }
        MLog.d("RcsConversation", "isGroupChatNotDisturb threadId: " + threadId);
        RcsGroupData groupData = RcsGroupCache.getInstance().getGroupData(this.mGroupId);
        if (!isScroll() || groupData == null) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            int notifySilent = 0;
            boolean isNotDisturb = false;
            try {
                Context context2 = context;
                cursor = SqliteWrapper.query(context2, resolver, IM_GROUPCHAT_URI, new String[]{"is_groupchat_notify_silent"}, "name = ?", new String[]{this.mGroupId}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    notifySilent = cursor.getInt(cursor.getColumnIndexOrThrow("is_groupchat_notify_silent"));
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (RuntimeException e) {
                notifySilent = 0;
                MLog.e("RcsConversation", "isGroupChatNotDisturb error: " + e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (notifySilent > 0) {
                isNotDisturb = true;
            }
            MLog.d("RcsConversation", "isGroupChatNotDisturb isNotDisturb: " + isNotDisturb);
            return isNotDisturb;
        }
        if (groupData.getNotifySilent() <= 0) {
            z = false;
        }
        return z;
    }

    private ContactList getContactFromCache(String recipientsIds, RcsGroupData groupData) {
        ContactList list = new ContactList();
        if (groupData.getSubject() != null) {
            list.add(Contact.get(0, 0, recipientsIds, groupData.getSubject()));
        }
        String[] rcsIds = groupData.getRcsIds();
        if (rcsIds != null) {
            for (String number : rcsIds) {
                if (number != null) {
                    list.add(Contact.get(number, false));
                }
            }
        }
        if (groupData.getStatus() == 1) {
            list.add(Contact.getMe(false));
        }
        return list;
    }

    public String getLastMessageFromName(Conversation cov, Context context) {
        if (cov == null || context == null || this.mGroupId == null) {
            return null;
        }
        RcsGroupData groupData = RcsGroupCache.getInstance().getGroupData(this.mGroupId);
        if (!isScroll()) {
            String str = null;
            Cursor cursor = null;
            long groupThreadId = 0;
            try {
                cursor = SqliteWrapper.query(context, Uri.parse("content://rcsim/get_newest_rcs_group_message_address").buildUpon().appendPath(String.valueOf(cov.getThreadId())).build(), new String[]{"address", "thread_id"}, null, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    str = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    groupThreadId = cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"));
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (IllegalArgumentException e) {
                MLog.e("RcsConversation", "there is no column address in rcs_group_message");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (str == null || str.isEmpty()) {
                return null;
            }
            String address = getFromName(str, groupThreadId);
            if (groupData != null) {
                groupData.setAddress(address);
            }
            return address;
        } else if (groupData != null) {
            return groupData.getAddress();
        } else {
            return null;
        }
    }

    private String getFromName(String number, long groupThreadId) {
        String address = NumberUtils.normalizeNumber(number);
        Contact contact = Contact.get(address, true);
        String nickname = "";
        if (contact.existsInDatabase()) {
            return contact.getName();
        }
        if (RcsProfile.isGroupChatNicknameEnabled()) {
            nickname = RcsProfile.getGroupMemberNickname(address, groupThreadId);
            if (!TextUtils.isEmpty(nickname)) {
                return nickname;
            }
        }
        return address;
    }
}
