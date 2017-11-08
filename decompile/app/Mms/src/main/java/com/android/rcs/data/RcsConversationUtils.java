package com.android.rcs.data;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.IContentProvider;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony.Threads;
import com.amap.api.services.core.AMapException;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.Conversation.ConversationQueryHandler;
import com.android.mms.data.Conversation.PinUpdateRequeset;
import com.android.mms.data.HwCustConversation.ParmWrapper;
import com.android.mms.util.DraftCache;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.transaction.RcsMessagingNotification;
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.rcs.util.RCSConst;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsTransaction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class RcsConversationUtils {
    private static RcsConversationUtils mHwCust = new RcsConversationUtils();
    private boolean isRcsEnable = RcsCommonConfig.isRCSSwitchOn();

    public static RcsConversationUtils getHwCustUtils() {
        return mHwCust;
    }

    public boolean isRcsSwitchOn() {
        return this.isRcsEnable;
    }

    public long querySmsThreadIdWithAddress(String address, Context context) {
        if (!this.isRcsEnable) {
            return -1;
        }
        long sms_thread_id = -1;
        Cursor cursor = null;
        if (address == null) {
            return -1;
        }
        try {
            cursor = SqliteWrapper.query(context, Uri.parse("content://rcsim/get_sms_thread").buildUpon().appendPath(address).build(), null, null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                sms_thread_id = cursor.getLong(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return sms_thread_id;
    }

    public Uri getRcsUri(long threadId, int threadType, boolean isMix) {
        if (!this.isRcsEnable) {
            return null;
        }
        if (1 != threadType) {
            return ContentUris.withAppendedId(RCSConst.RCS_URI_CONVERSATIONS, threadId).buildUpon().appendQueryParameter("threadType", String.valueOf(threadType)).build();
        }
        return ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);
    }

    public void pinConversationRcs(final Context context, final Collection<Long> threadIds, final boolean isPinup) {
        if (this.isRcsEnable) {
            HwBackgroundLoader.getInst().postTask(new Runnable() {
                public void run() {
                    if (threadIds.size() == 0) {
                        MLog.w("RcsConversationUtils", "pinConversationRcs threadIds size 0");
                        return;
                    }
                    IContentProvider icp = context.getContentResolver().acquireProvider(Uri.parse("content://rcsim"));
                    if (icp != null) {
                        String callingPkg = context.getPackageName();
                        Bundle extras = new Bundle();
                        Collection<Long> xmsThreadIds = new ArrayList();
                        Collection<Long> rcsThreadIds = new ArrayList();
                        for (Long thread_id : threadIds) {
                            long thread_id_value = thread_id.longValue();
                            if (thread_id_value >= 0 || thread_id_value == -10000000011L || thread_id_value == -10000000012L) {
                                xmsThreadIds.add(thread_id);
                            } else {
                                rcsThreadIds.add(Long.valueOf(-thread_id.longValue()));
                            }
                        }
                        Collection<Long> otherRcsThreadIds = RcsConversationUtils.this.getOtherThreadFromGivenThread(context, xmsThreadIds, 1);
                        xmsThreadIds.addAll(RcsConversationUtils.this.getOtherThreadFromGivenThread(context, rcsThreadIds, 2));
                        rcsThreadIds.addAll(otherRcsThreadIds);
                        MLog.d("RcsConversationUtils", "pinConversationRcs all the rcsThreadIds = " + rcsThreadIds);
                        MLog.d("RcsConversationUtils", "pinConversationRcs all the xmsThreadIds = " + xmsThreadIds);
                        long[] xmsIds = RcsConversationUtils.this.threadCollectionTolong(xmsThreadIds);
                        long[] rcsIds = RcsConversationUtils.this.threadCollectionTolong(rcsThreadIds);
                        extras.putLongArray("xms_thread_ids", xmsIds);
                        extras.putLongArray("rcs_thread_ids", rcsIds);
                        extras.putBoolean("pin_up", isPinup);
                        try {
                            Bundle result = icp.call(callingPkg, "method_pin_up_rcs", null, extras);
                            if (result != null) {
                                RcsConversationUtils.this.updatePinupCacheWithResult(xmsThreadIds, rcsThreadIds, isPinup, result);
                            }
                        } catch (Throwable e) {
                            MLog.e("RcsConversationUtils", "pinConversationRcs call METHOD_PIN_UP excetpion : ", e);
                        }
                    }
                }
            });
        }
    }

    public void freshPinupCache(Context context, HashSet<Long> pinupIds, long[] pinUpArchiveIds) {
        if (this.isRcsEnable) {
            synchronized (pinupIds) {
                pinupIds.clear();
                Cursor cursor = null;
                Long threadId = Long.valueOf(0);
                try {
                    cursor = SqliteWrapper.query(context, RcsConversation.RCSAllThreadsUri, RcsConversation.RCS_PINUP_THREADS_PROJECTION, "priority > 0", null, "date DESC");
                    if (cursor == null || !cursor.moveToFirst()) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        MLog.i("RcsConversationUtils", "freshPinupCache after fresh, mPinupIds = " + pinupIds);
                    } else {
                        do {
                            if (cursor.getInt(2) > 0) {
                                threadId = Long.valueOf(cursor.getLong(0));
                                if (cursor.getLong(RcsConversation.RCS_PINUP_THREADS_PROJECTION.length - 1) == 2) {
                                    threadId = Long.valueOf(-threadId.longValue());
                                }
                                pinupIds.add(threadId);
                            }
                        } while (cursor.moveToNext());
                        if (cursor != null) {
                            cursor.close();
                        }
                        MLog.i("RcsConversationUtils", "freshPinupCache after fresh, mPinupIds = " + pinupIds);
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (pinUpArchiveIds != null) {
                for (long id : pinUpArchiveIds) {
                    pinupIds.add(Long.valueOf(id));
                }
            }
        }
    }

    public Collection<Long> getOtherThreadFromGivenThread(Context context, Collection<Long> givenThreadIds, int type) {
        if (!this.isRcsEnable) {
            return null;
        }
        Cursor cursor = null;
        Collection<Long> newThreadIds = new ArrayList();
        if (givenThreadIds.isEmpty()) {
            return newThreadIds;
        }
        Uri resultUri;
        if (1 == type) {
            resultUri = Uri.parse("content://rcsim/get_other_thread_from_given_thread/").buildUpon().appendQueryParameter("QueryMode", "rcsFromXms").build();
        } else {
            resultUri = Uri.parse("content://rcsim/get_other_thread_from_given_thread/").buildUpon().appendQueryParameter("QueryMode", "xmsFromRcs").build();
        }
        String s = givenThreadIds.toString();
        try {
            cursor = SqliteWrapper.query(context, resultUri, null, "_id IN (" + s.substring(1, s.length() - 1) + ")", null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return newThreadIds;
            }
            do {
                newThreadIds.add(Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("thread_id"))));
            } while (cursor.moveToNext());
            if (cursor != null) {
                cursor.close();
            }
            return newThreadIds;
        } catch (Exception exception) {
            MLog.e("RcsConversationUtils", "getOtherThreadFromGivenThread error " + exception);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void updatePinupCacheWithResult(Collection<Long> xmsThreadIds, Collection<Long> rcsThreadIds, boolean isPinup, Bundle result) {
        final Boolean isRcsPinUpdate = Boolean.valueOf(result.getBoolean("call_result_rcs"));
        final Boolean isXmsPinUpdate = Boolean.valueOf(result.getBoolean("call_result_xms"));
        if (isRcsPinUpdate.booleanValue() || isXmsPinUpdate.booleanValue()) {
            final boolean z = isPinup;
            final Collection<Long> collection = rcsThreadIds;
            final Collection<Long> collection2 = xmsThreadIds;
            HwBackgroundLoader.getUIHandler().post(new Runnable() {
                public void run() {
                    if (isRcsPinUpdate.booleanValue()) {
                        RcsConversationUtils.this.updatePinupCacheRcs(new PinUpdateRequeset(z, collection));
                    }
                    if (isXmsPinUpdate.booleanValue()) {
                        Conversation.updatePinupCache(new PinUpdateRequeset(z, collection2));
                    }
                }
            });
        }
    }

    private long[] threadCollectionTolong(Collection<Long> threadIds) {
        long[] Ids = new long[threadIds.size()];
        int i = 0;
        for (Long longValue : threadIds) {
            Ids[i] = longValue.longValue();
            i++;
        }
        return Ids;
    }

    private void updatePinupCacheRcs(Object obj) {
        if (obj instanceof PinUpdateRequeset) {
            PinUpdateRequeset req = (PinUpdateRequeset) obj;
            synchronized (Conversation.getPinupIds()) {
                for (Long id : req.mDatas) {
                    if (req.mIsAdd) {
                        Conversation.getPinupIds().add(Long.valueOf(id.longValue() * -1));
                    } else {
                        Conversation.getPinupIds().remove(Long.valueOf(id.longValue() * -1));
                    }
                }
                MLog.i("RcsConversationUtils", "updatePinupCacheRcs conversation mPinupIds =  " + Conversation.getPinupIds());
            }
        }
    }

    public void startQueryByThreadSettings(AsyncQueryHandler handler, int token, Context context, int numberType) {
        if (this.isRcsEnable) {
            handler.cancelOperation(token);
            if (numberType == 1 || numberType == 2) {
                Conversation.startQuery(handler, token, null, numberType);
                return;
            }
            Uri allThreads;
            if (numberType == 255) {
                allThreads = RcsConversation.sRcsAllMixedCommonThreadsUri;
            } else {
                allThreads = RcsConversation.sRcsAllMixedThreads;
            }
            handler.startQuery(token, null, allThreads, RcsConversation.getRcsAllThreadProjection(), null, null, "priority DESC, date DESC");
        }
    }

    public boolean switchToXmsThreadIds(Context context, Collection<Long> threadIds, ConversationQueryHandler queryHandler) {
        if (!this.isRcsEnable) {
            return false;
        }
        Collection<Long> xmsThreadIds = getXmsThreadIdAndDeleteRcsConversation(context, threadIds, queryHandler);
        threadIds.clear();
        threadIds.addAll(xmsThreadIds);
        MLog.i("RcsConversationUtils", "switchToXmsThreadIds mThreadIds = " + threadIds);
        return threadIds.isEmpty();
    }

    private Collection<Long> getXmsThreadIdAndDeleteRcsConversation(Context context, Collection<Long> threadIds, ConversationQueryHandler handler) {
        Collection<Long> xmsThreadIds = new ArrayList();
        Collection<Long> rcsImChatThreadIds = new ArrayList();
        Collection<String> rcsGroupChatGroupIds = new ArrayList();
        Collection<Long> rcsThreadIds = new ArrayList();
        MLog.i("RcsConversationUtils", "getXmsThreadIdAndDeleteRcsConversation all the threadIds = " + threadIds);
        for (Long thread_id : threadIds) {
            if (thread_id.longValue() < 0) {
                rcsThreadIds.add(Long.valueOf(-thread_id.longValue()));
            } else {
                xmsThreadIds.add(thread_id);
            }
        }
        dividOneOneChatAndGroupChatWithThreadId(rcsThreadIds, context, rcsImChatThreadIds, rcsGroupChatGroupIds);
        Collection<Long> newRcsImThreadIds = getOtherThreadFromGivenThread(context, xmsThreadIds, 1);
        xmsThreadIds.addAll(getOtherThreadFromGivenThread(context, rcsImChatThreadIds, 2));
        rcsImChatThreadIds.addAll(newRcsImThreadIds);
        MLog.d("RcsConversationUtils", "getXmsThreadIdAndDeleteRcsConversation all the xmsThreadIds = " + xmsThreadIds);
        MLog.d("RcsConversationUtils", "getXmsThreadIdAndDeleteRcsConversation all the rcsImChatThreadIds = " + rcsImChatThreadIds);
        MLog.d("RcsConversationUtils", "getXmsThreadIdAndDeleteRcsConversation all the rcsGroupChatGroupIds = " + rcsGroupChatGroupIds);
        int token = AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST;
        if (xmsThreadIds.isEmpty()) {
            token = AMapException.CODE_AMAP_CLIENT_ERROR_PROTOCOL;
        }
        if (!rcsImChatThreadIds.isEmpty()) {
            RcsTransaction.cancelFtMsgBeforeDeleteConversation(context, rcsImChatThreadIds, 1);
            startDeleteOnceForRcsImChat(handler, token, true, rcsImChatThreadIds);
        }
        if (!rcsGroupChatGroupIds.isEmpty()) {
            RcsTransaction.cancelFtMsgBeforeDeleteConversation(context, rcsGroupChatGroupIds, 2);
            RcsTransaction.exitGroupChatBeforeDeleteConversation(rcsGroupChatGroupIds);
            clearGroupCreateNotificationByIds(context, rcsGroupChatGroupIds);
            if (DraftCache.getInstance().getHwCust() != null) {
                DraftCache.getInstance().getHwCust().setRcsGroupDraftGroupState(rcsThreadIds, false);
            }
            startDeleteOnceForRcsGroupChat(handler, token, true, rcsGroupChatGroupIds);
        }
        return xmsThreadIds;
    }

    private void dividOneOneChatAndGroupChatWithThreadId(Collection<Long> rcsThreadIds, Context context, Collection<Long> rcsImChatThreadIds, Collection<String> rcsGroupChatGroupIds) {
        Cursor cursor = null;
        Uri resultUri = Uri.parse("content://rcsim/rcs_conversations/");
        if (!rcsThreadIds.isEmpty()) {
            String s = rcsThreadIds.toString();
            try {
                cursor = SqliteWrapper.query(context, resultUri, new String[]{"_id", "recipient_ids"}, "_id IN (" + s.substring(1, s.length() - 1) + ")", null, null);
                if (cursor == null || !cursor.moveToFirst()) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                do {
                    Long id = Long.valueOf(cursor.getLong(0));
                    String recipient = cursor.getString(1);
                    if (Contact.getHwCustContact().isGroupID(recipient)) {
                        rcsGroupChatGroupIds.add("'" + recipient + "'");
                    } else {
                        rcsImChatThreadIds.add(id);
                    }
                } while (cursor.moveToNext());
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception exception) {
                MLog.e("RcsConversationUtils", "dividOneOneChatAndGroupChatWithThreadId error " + exception);
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

    private void startDeleteOnceForRcsGroupChat(ConversationQueryHandler handler, int token, boolean deleteAll, Collection<String> rcsGroupChatGroupIds) {
        synchronized (Conversation.getDeletingThreadsLock()) {
            if (Conversation.getDeletingThreads()) {
                MLog.w("RcsConversationUtils", "RcsGroupChat already in the middle of a delete", new Exception());
            }
            Conversation.setDeletingThreads(true);
            Uri uri = Uri.parse("content://rcsim/delete_sel_rcs_groupchat_conversation/");
            if (rcsGroupChatGroupIds.isEmpty()) {
                return;
            }
            String s = rcsGroupChatGroupIds.toString();
            String selection = "recipient_ids IN (" + s.substring(1, s.length() - 1) + ")";
            MLog.i("RcsConversationUtils", "startDeleteOnceForRcsGroupChat selection=" + selection);
            handler.setDeleteToken(token);
            handler.startDelete(token, null, uri, selection, null);
        }
    }

    private void startDeleteOnceForRcsImChat(ConversationQueryHandler handler, int token, boolean deleteAll, Collection<Long> rcsImChatThreadIds) {
        synchronized (Conversation.getDeletingThreadsLock()) {
            if (Conversation.getDeletingThreads()) {
                MLog.w("RcsConversationUtils", "RcsImChat already in the middle of a delete", new Exception());
            }
            Conversation.setDeletingThreads(true);
            Uri uri = Uri.parse("content://rcsim/delete_sel_rcs_1to1chat_conversation/");
            if (rcsImChatThreadIds.isEmpty()) {
                return;
            }
            String s = rcsImChatThreadIds.toString();
            String selection = "_id IN (" + s.substring(1, s.length() - 1) + ")";
            MLog.i("RcsConversationUtils", "startDeleteOnceForRcsImChat selection=" + selection);
            handler.setDeleteToken(token);
            handler.startDelete(token, null, uri, selection, null);
        }
    }

    public String getGroupSubjectByThreadID(Context context, long threadId) {
        if (!this.isRcsEnable) {
            return null;
        }
        Cursor cursor = null;
        String groupChatSubject = "";
        try {
            Context context2 = context;
            cursor = SqliteWrapper.query(context2, RcsGroupChatComposeMessageFragment.sGroupUri, null, "thread_id = ?", new String[]{String.valueOf(threadId)}, null);
            if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
                MLog.w("RcsConversationUtils", "getGroupSubjectByThreadID fillFromCursor: cannot find subject by threadID=" + threadId);
            } else {
                groupChatSubject = cursor.getString(cursor.getColumnIndex("subject"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            MLog.e("RcsConversationUtils", "getGroupSubjectByThreadID IllegalStateException: query group chat error: " + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return groupChatSubject;
    }

    public String queryAddressWithid(long thread_id, int type, Context context) {
        if (this.isRcsEnable) {
            return queryAddressWithRecipient(queryRecipientWithThreadId(thread_id, type, context), context);
        }
        return null;
    }

    private String queryRecipientWithThreadId(long thread_id, int type, Context context) {
        String recipient = "";
        Cursor cursor = null;
        Uri resultUri = null;
        if (type == 1) {
            resultUri = Uri.withAppendedPath(ContentUris.withAppendedId(Uri.parse("content://mms-sms/conversations/"), thread_id), "recipients");
        } else if (type == 2) {
            resultUri = Uri.withAppendedPath(ContentUris.withAppendedId(Uri.parse("content://rcsim/conversations/"), thread_id), "recipients");
        }
        try {
            cursor = SqliteWrapper.query(context, resultUri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                recipient = cursor.getString(2);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return recipient;
    }

    private String queryAddressWithRecipient(String recipient, Context context) {
        String str = null;
        Cursor cursor = null;
        if (recipient.length() <= 0) {
            return null;
        }
        try {
            cursor = SqliteWrapper.query(context, Uri.withAppendedPath(Uri.parse("content://mms-sms/canonical-address/"), recipient), null, null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                str = cursor.getString(0);
            }
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

    public long queryChatThreadIdWithAddress(String address, Context context) {
        if (!this.isRcsEnable) {
            return -1;
        }
        long chat_thread_id = -1;
        Cursor cursor = null;
        if (address == null) {
            return -1;
        }
        try {
            cursor = SqliteWrapper.query(context, Uri.parse("content://rcsim/get_rcs_thread").buildUpon().appendPath(address).build(), null, null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                chat_thread_id = cursor.getLong(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return chat_thread_id;
    }

    public void blockingMarkAllOtherMessagesAsSeen(Context context, String[] SEEN_PROJECTION) {
        if (this.isRcsEnable) {
            blockingMarkAllImMessagesAsSeen(context, SEEN_PROJECTION);
            blockingMarkAllRcsGroupMessagesAsSeen(context, SEEN_PROJECTION);
        }
    }

    private void blockingMarkAllImMessagesAsSeen(Context context, String[] SEEN_PROJECTION) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = SqliteWrapper.query(context, resolver, Uri.parse("content://rcsim/chat/"), SEEN_PROJECTION, "seen=0", null, null);
            if (cursor != null) {
                count = cursor.getCount();
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception exception) {
            MLog.e("RcsConversationUtils", "IllegalStateException: unstableCount < 0: -1:" + exception);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        MLog.i("RcsConversationUtils", "mark " + count + " Im msgs as seen");
        if (count != 0) {
            ContentValues values = new ContentValues(1);
            values.put("seen", Integer.valueOf(1));
            SqliteWrapper.update(context, resolver, Uri.parse("content://rcsim/chat/"), values, "seen=0", null);
        }
    }

    private void blockingMarkAllRcsGroupMessagesAsSeen(Context context, String[] SEEN_PROJECTION) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = SqliteWrapper.query(context, resolver, Uri.parse("content://rcsim/rcs_group_message"), SEEN_PROJECTION, "seen=0", null, null);
            if (cursor != null) {
                count = cursor.getCount();
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception exception) {
            MLog.e("RcsConversationUtils", "blockingMarkAllRcsGroupMessagesAsSeen IllegalStateException: unstableCount < 0: -1:" + exception);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        MLog.i("RcsConversationUtils", "blockingMarkAllRcsGroupMessagesAsSeen mark " + count + " RCS msgs as seen");
        if (count != 0) {
            ContentValues values = new ContentValues(1);
            values.put("seen", Integer.valueOf(1));
            SqliteWrapper.update(context, resolver, Uri.parse("content://rcsim/rcs_group_message"), values, "seen=0", null);
        }
    }

    public boolean isRcsConversation(ContactList recipients) {
        if (this.isRcsEnable && recipients.size() == 1) {
            return RcsProfile.isImAvailable(((Contact) recipients.get(0)).getNumber());
        }
        return false;
    }

    public long getOrCreateRcsThreadId(Context context, ContactList list) {
        if (!this.isRcsEnable) {
            return 0;
        }
        Cursor cursor = null;
        long rcsThreadId = 0;
        try {
            Context context2 = context;
            cursor = SqliteWrapper.query(context2, RcsConversation.GET_OR_CREATE_RCS_THEAD_URI.buildUpon().appendPath(((Contact) list.get(0)).getNumber()).build(), null, null, null, null);
            if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
                rcsThreadId = 0;
            } else {
                rcsThreadId = cursor.getLong(0);
                MLog.i("RcsConversationUtils", "getOrCreateRcsThreadId rcsThreadId = " + rcsThreadId);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RuntimeException e) {
            MLog.e("RcsConversationUtils", "getOrCreateRcsThreadId error ");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return rcsThreadId;
    }

    public Conversation getRcsConversation(Context context, Uri uri, boolean allowQuery) {
        if (!this.isRcsEnable) {
            return null;
        }
        long threadType;
        long threadId = Long.parseLong((String) uri.getPathSegments().get(1));
        if (uri.getQueryParameter("threadType") != null) {
            threadType = Long.parseLong(uri.getQueryParameter("threadType"));
        } else {
            threadType = 1;
        }
        return Conversation.get(context, threadId, allowQuery, new ParmWrapper(Long.valueOf(threadType), null));
    }

    public static long judgeRcsThreadId(long threadId, int type) {
        long id = threadId;
        if (threadId == -10000000011L || threadId == -10000000012L) {
            return threadId;
        }
        if (threadId < 0) {
            id = 0;
        }
        if (type == 2 || type == 4) {
            id = -threadId;
            MLog.i("RcsConversationUtils", "judgeRcsThreadId theadId = " + threadId);
        }
        return id;
    }

    public long getRcsModeValueOfXms() {
        return 1;
    }

    public long getRcsModeValueOfIm() {
        return 2;
    }

    public Collection<Conversation> getAllConversation() {
        if (this.isRcsEnable) {
            return RcsProfile.queryAllConversation();
        }
        return null;
    }

    private void clearGroupCreateNotificationByIds(Context context, Collection<String> groupChatIds) {
        if (groupChatIds != null && !groupChatIds.isEmpty()) {
            for (String groupId : groupChatIds) {
                RcsMessagingNotification.clearGroupCreateNotificationByGroupId(context, groupId.replaceAll("'", ""));
            }
        }
    }

    public boolean isRcsGroupThread(Cursor c) {
        if (!this.isRcsEnable || c == null) {
            return false;
        }
        try {
            String id = c.getString(c.getColumnIndexOrThrow("recipient_ids"));
            if (!Contact.isEmailAddress(id) && Contact.getHwCustContact().isGroupID(id)) {
                return true;
            }
        } catch (IllegalArgumentException e) {
            MLog.e("RcsConversationUtils", "isRcsGroupThread there is no coloum thread_type while load conversation");
        }
        return false;
    }
}
