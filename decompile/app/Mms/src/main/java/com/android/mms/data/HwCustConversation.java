package com.android.mms.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class HwCustConversation {

    public static class ParmWrapper {
        public Long threadType = null;
        public Integer whichTable = null;

        public ParmWrapper(Long type, Integer table) {
            this.threadType = type;
            this.whichTable = table;
        }
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public boolean isRcsThreadId() {
        return false;
    }

    public void setIsRcsThreadId(boolean isRcs) {
    }

    public int getRcsThreadType() {
        return 0;
    }

    public void setRcsThreadType(Cursor cursor) {
    }

    public void setRcsGroupChatId(String id) {
    }

    public String getRcsGroupChatID() {
        return null;
    }

    public void sendImReadReport(ContactList list, long threadId) {
    }

    public boolean markAsReadDoInBackground(Uri threadUri, String[] UNREAD_PROJECTION, String UNREAD_SELECTION, ContentValues readContentValues, Conversation cov, Context ctx) {
        return false;
    }

    public void getEnsureThreadId(Conversation cov, Context ctx) {
    }

    public boolean hasDraft(Conversation cov, Context ctx) {
        return false;
    }

    public void setDraftState(boolean hasDraft, Conversation cov, Context ctx) {
    }

    public Cursor getOtherTypeCursorFromThreadId(Context context, long threadId) {
        return null;
    }

    public Cursor getOtherTypeCursorFromThreadId(Context context, long threadId, ParmWrapper wrapper, String[] ALL_THREADS_PROJECTION) {
        return null;
    }

    public boolean isImEnable() {
        return false;
    }

    public long getRcsThreadId(Conversation cov) {
        return 0;
    }

    public long getSmsThreadId(Conversation cov, Context ctx) {
        return 0;
    }

    public long getSmsThreadId(long threadId, Conversation cov, Context ctx) {
        return threadId;
    }

    public Uri getGroupMessageUri(Conversation cov, Context ctx) {
        return null;
    }

    public ContactList getRecipients(String spaceIds, boolean allowQuery, Context ctx) {
        return null;
    }

    public String getSnippet(Cursor cursor, String spaceIds, int columnRawBytes, int columnCharset, Context ctx) {
        return null;
    }

    public long ensureFtSendThreadId(Conversation cov, Context ctx) {
        return 0;
    }

    public long ensureFtThreadId(Conversation cov, Context ctx) {
        return 0;
    }

    public long ensureDraftThreadId(Conversation cov, Context ctx) {
        return 0;
    }

    public synchronized void setHasUndelivered(Cursor cursor) {
    }

    public synchronized boolean hasUndeliveredMsg() {
        return false;
    }

    public synchronized void setFileType(Cursor cursor) {
    }

    public synchronized int getFileType() {
        return 0;
    }

    public String getGroupId() {
        return "";
    }

    public boolean isGroupChat() {
        return false;
    }

    public long getGroupChatThreadId() {
        return -1;
    }

    public void setThumanailPath(Cursor aCursor) {
    }

    public String getThumbnailPath() {
        return "";
    }

    public String[] getAllThreadsProjection(String[] aAllThreadsProjection) {
        return aAllThreadsProjection;
    }

    public String getDefaultEmptySubject(Context aContext, String aSubject) {
        return aSubject;
    }
}
