package com.android.mms.data;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.android.mms.data.Conversation.ConversationQueryHandler;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class HwCustConversationUtils {
    private static HwCustConversationUtils mHwCust = ((HwCustConversationUtils) HwCustUtils.createObj(HwCustConversationUtils.class, new Object[0]));

    public static HwCustConversationUtils getHwCustUtils() {
        return mHwCust;
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public long querySmsThreadIdWithAddress(String address, Context mContext) {
        return -1;
    }

    public Uri getRcsUri(long threadId, int threadType, boolean isMix) {
        return null;
    }

    public void pinConversationRcs(Context context, Collection<Long> collection, boolean isPinup) {
    }

    public void freshPinupCache(Context context, HashSet<Long> hashSet, long[] pinUpArchiveIds) {
    }

    public Collection<Long> getOtherThreadFromGivenThread(Context context, Collection<Long> collection, int type) {
        return null;
    }

    public void startQueryByThreadSettings(AsyncQueryHandler handler, int token, Context context, int numberType) {
    }

    public boolean switchToXmsThreadIds(Context context, Collection<Long> collection, ConversationQueryHandler queryHandler) {
        return false;
    }

    public String getGroupSubjectByThreadID(Context context, long threadId) {
        return null;
    }

    public String queryAddressWithid(long thread_id, int type, Context mContext) {
        return null;
    }

    public long queryChatThreadIdWithAddress(String address, Context mContext) {
        return -1;
    }

    public void blockingMarkAllOtherMessagesAsSeen(Context context, String[] SEEN_PROJECTION) {
    }

    public boolean isRcsConversation(ContactList recipients) {
        return false;
    }

    public long getOrCreateRcsThreadId(Context context, ContactList list) {
        return 0;
    }

    public Conversation getRcsConversation(Context context, Uri uri, boolean allowQuery) {
        return null;
    }

    public long getRcsModeValueOfXms() {
        return 1;
    }

    public long getRcsModeValueOfIm() {
        return 2;
    }

    public Collection<Conversation> getAllConversation() {
        return new ArrayList();
    }

    public boolean isRcsGroupThread(Cursor c) {
        return false;
    }
}
