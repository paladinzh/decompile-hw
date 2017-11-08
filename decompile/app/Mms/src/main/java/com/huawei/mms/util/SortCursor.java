package com.huawei.mms.util;

import android.content.Context;
import android.database.ContentObserver;
import android.database.CursorWrapper;
import android.database.MatrixCursor;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import com.huawei.cspcommon.ICspSortCursor;
import java.util.ArrayList;
import java.util.HashMap;

public class SortCursor extends CursorWrapper implements ICspSortCursor {
    private static MatrixCursor mContactCursor = null;
    private static HashMap<Long, ContactQueryInfo> mContactQueryInfo = new HashMap();
    private static boolean mContactsDataChange = true;
    private static ContentObserver mContactsObserver = null;
    private static Context mContext;
    private static boolean mLoadContactQueryInfo = false;
    private static Thread mLoadQueryInfoTask = null;
    private static int mSortOrder = -1;
    private ArrayList<ContactCursorInfo> mContactCursorInfoList = null;
    private ArrayList<SortEntry> mNewSortList = null;
    private String mOldQueryString = null;
    private int mPos = -1;
    private ArrayList<SortEntry> mSortList = null;
    private boolean mbNewQuery = false;

    private static class ContactCursorInfo {
        long contactId;
        String contactName;
        boolean inSearchResult;

        private ContactCursorInfo() {
        }
    }

    public static class SortEntry {
        public int dataType;
        public int matchType;
        public int order;
        public int queryIndex;
        public int timeContacted;
    }

    public int compare(SortEntry entry1, SortEntry entry2) {
        int ret = 0;
        if (entry1.dataType != entry2.dataType) {
            if (5 == entry1.dataType) {
                return -1;
            }
            if (5 == entry2.dataType) {
                return 1;
            }
        }
        if (entry1.matchType != entry2.matchType) {
            if (7 == entry1.matchType) {
                ret = -1;
            } else if (7 == entry2.matchType) {
                ret = 1;
            } else if (5 == entry1.matchType) {
                ret = -1;
            } else if (5 == entry2.matchType) {
                ret = 1;
            }
        }
        if (ret == 0) {
            ret = entry1.queryIndex - entry2.queryIndex;
            if (ret == 0) {
                ret = entry2.timeContacted - entry1.timeContacted;
            }
        }
        return ret;
    }

    public SortCursor() {
        super(mContactCursor);
        if (mContactCursor != null && mContactCursor.getCount() > 0) {
            appendContactCursorInfo();
        }
    }

    public int getCount() {
        int count = 0;
        if (this.mSortList != null) {
            return this.mSortList.size();
        }
        if (this.mCursor != null) {
            count = this.mCursor.getCount();
        }
        return count;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void appendContactCursorInfo() {
        Throwable th;
        try {
            if (this.mCursor != null) {
                int count = this.mCursor.getCount();
                if (count != 0) {
                    if (this.mContactCursorInfoList == null) {
                        this.mContactCursorInfoList = new ArrayList();
                    } else {
                        this.mContactCursorInfoList.clear();
                    }
                    if (this.mCursor.moveToFirst()) {
                        int i = 0;
                        ContactCursorInfo cursorInfo = null;
                        while (i < count) {
                            ContactCursorInfo cursorInfo2;
                            try {
                                long currentCID = this.mCursor.getLong(4);
                                String currentName = this.mCursor.getString(0);
                                boolean shouldAdd = true;
                                for (int j = this.mContactCursorInfoList.size() - 1; j >= 0; j--) {
                                    if (currentCID == ((ContactCursorInfo) this.mContactCursorInfoList.get(j)).contactId) {
                                        shouldAdd = false;
                                        break;
                                    }
                                    if (currentName != null) {
                                        if (!currentName.equals(((ContactCursorInfo) this.mContactCursorInfoList.get(j)).contactName)) {
                                            break;
                                        }
                                    }
                                }
                                if (shouldAdd) {
                                    cursorInfo2 = new ContactCursorInfo();
                                    cursorInfo2.contactId = currentCID;
                                    cursorInfo2.inSearchResult = true;
                                    cursorInfo2.contactName = currentName;
                                    this.mContactCursorInfoList.add(cursorInfo2);
                                } else {
                                    cursorInfo2 = cursorInfo;
                                }
                                if (!this.mCursor.moveToNext()) {
                                    break;
                                }
                                i++;
                                cursorInfo = cursorInfo2;
                            } catch (Throwable th2) {
                                th = th2;
                                cursorInfo2 = cursorInfo;
                            }
                        }
                    } else {
                        return;
                    }
                }
            }
        } catch (Throwable th3) {
            th = th3;
        }
        throw th;
    }

    public boolean moveToPosition(int position) {
        if (this.mCursor == null || !this.mCursor.moveToFirst()) {
            return false;
        }
        if (this.mSortList == null) {
            return this.mCursor.moveToPosition(position);
        }
        int listSize = this.mSortList.size();
        if (listSize > 0) {
            if (position >= 0 && position < listSize) {
                this.mPos = position;
                return this.mCursor.moveToPosition(((SortEntry) this.mSortList.get(position)).order);
            } else if (position < 0) {
                this.mPos = -1;
                return false;
            } else if (position >= listSize) {
                this.mPos = listSize;
                return false;
            }
        }
        return false;
    }

    public boolean moveToFirst() {
        return moveToPosition(0);
    }

    public boolean moveToLast() {
        return moveToPosition(getCount() - 1);
    }

    public boolean moveToNext() {
        if (this.mCursor == null) {
            return false;
        }
        if (this.mSortList == null) {
            return this.mCursor.moveToNext();
        }
        return moveToPosition(this.mPos + 1);
    }

    public boolean moveToPrevious() {
        if (this.mCursor == null) {
            return false;
        }
        if (this.mSortList == null) {
            return this.mCursor.moveToPrevious();
        }
        return moveToPosition(this.mPos - 1);
    }

    public boolean move(int offset) {
        if (this.mCursor == null) {
            return false;
        }
        if (this.mSortList == null) {
            return this.mCursor.move(offset);
        }
        return moveToPosition(this.mPos + offset);
    }

    public int getPosition() {
        if (this.mCursor == null) {
            return -1;
        }
        if (this.mSortList == null) {
            return this.mCursor.getPosition();
        }
        return this.mPos;
    }

    public static String getSortOrder(Context context) {
        if (mSortOrder == -1) {
            try {
                mSortOrder = System.getInt(context.getContentResolver(), "android.contacts.SORT_ORDER");
            } catch (SettingNotFoundException e) {
                mSortOrder = 1;
            }
        }
        if (1 == mSortOrder) {
            return "sort_key,contact_id";
        }
        return "sort_key_alt,contact_id";
    }

    public static void unRegisterContactsObserver() {
        mContext.getContentResolver().unregisterContentObserver(mContactsObserver);
        mLoadQueryInfoTask.interrupt();
        mLoadQueryInfoTask = null;
    }
}
