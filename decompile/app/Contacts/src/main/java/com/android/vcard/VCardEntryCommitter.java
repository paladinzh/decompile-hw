package com.android.vcard;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.ContactsContract.Groups;
import android.text.TextUtils;
import android.util.Log;
import com.android.vcard.VCardEntry.ExtendDataCommitter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VCardEntryCommitter implements VCardEntryHandler {
    public static String LOG_TAG = "vCard";
    private Map<Group, Long> groupMap = new HashMap();
    private final ContentResolver mContentResolver;
    private int mCounter;
    private final ArrayList<Uri> mCreatedUris = new ArrayList();
    private MyExtendDataCommitter mExtendDataCommitter = new MyExtendDataCommitter();
    boolean mNeedSleep = true;
    private ArrayList<ContentProviderOperation> mOperationList;
    private long mTimeToCommit;

    private static class Group {
        Account account;
        String title;

        public Group(String title, Account account) {
            this.title = title;
            this.account = account;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (o == this) {
                return true;
            }
            if (!(o instanceof Group) || this.title == null || this.account == null) {
                return false;
            }
            Group other = (Group) o;
            if (this.title.equals(other.title)) {
                z = this.account.equals(other.account);
            }
            return z;
        }

        public int hashCode() {
            int result = 17;
            if (this.title != null) {
                result = this.title.hashCode() + 527;
            }
            if (this.account != null) {
                return (result * 31) + this.account.hashCode();
            }
            return result;
        }
    }

    private class MyExtendDataCommitter implements ExtendDataCommitter {
        private MyExtendDataCommitter() {
        }

        public long commitGroupmemberShip(String title, Account account) {
            if (TextUtils.isEmpty(title)) {
                return -1;
            }
            Group group = new Group(title, account);
            Long groupId = (Long) VCardEntryCommitter.this.groupMap.get(group);
            if (groupId != null && groupId.longValue() >= 0) {
                return groupId.longValue();
            }
            String selection = "title=? AND account_name=? AND account_type=? AND deleted<>1";
            try {
                Cursor cursor = VCardEntryCommitter.this.mContentResolver.query(Groups.CONTENT_URI, null, "title=? AND account_name=? AND account_type=? AND deleted<>1", new String[]{title, account.name, account.type}, null);
                if (cursor == null) {
                    Log.i(VCardEntryCommitter.LOG_TAG, "query group returns null cursor");
                    return -1;
                }
                groupId = Long.valueOf(-1);
                if (cursor.moveToFirst()) {
                    groupId = Long.valueOf(cursor.getLong(cursor.getColumnIndex("_id")));
                    Log.i(VCardEntryCommitter.LOG_TAG, "query group returns groupId = " + groupId);
                }
                if (groupId.longValue() < 0) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("title", title);
                    contentValues.put("account_name", account.name);
                    contentValues.put("account_type", account.type);
                    groupId = Long.valueOf(ContentUris.parseId(VCardEntryCommitter.this.mContentResolver.insert(Groups.CONTENT_URI, contentValues)));
                    Log.i(VCardEntryCommitter.LOG_TAG, "insert group returns groupId = " + groupId);
                }
                cursor.close();
                if (groupId.longValue() >= 0) {
                    VCardEntryCommitter.this.groupMap.put(group, groupId);
                }
                return groupId.longValue();
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
    }

    public VCardEntryCommitter(ContentResolver resolver) {
        this.mContentResolver = resolver;
    }

    public void onStart() {
    }

    public void onEnd() {
        if (this.mOperationList != null) {
            this.mCreatedUris.add(pushIntoContentResolver(this.mOperationList));
        }
        if (VCardConfig.showPerformanceLog()) {
            Log.d(LOG_TAG, String.format("time to commit entries: %d ms", new Object[]{Long.valueOf(this.mTimeToCommit)}));
        }
    }

    public void setImportVcardNeedSleep(boolean needSleep) {
        this.mNeedSleep = needSleep;
    }

    public void onEntryCreated(VCardEntry vcardEntry) {
        long start = System.currentTimeMillis();
        vcardEntry.setExtendDataCommitter(this.mExtendDataCommitter);
        this.mOperationList = vcardEntry.constructInsertOperations(this.mContentResolver, this.mOperationList);
        this.mCounter++;
        if (this.mCounter >= 20) {
            this.mCreatedUris.add(pushIntoContentResolver(this.mOperationList));
            this.mCounter = 0;
            this.mOperationList = null;
            if (this.mNeedSleep) {
                SystemClock.sleep(350);
                Log.d(LOG_TAG, "already parse 20 records and msleep 350");
            }
        }
        this.mTimeToCommit += System.currentTimeMillis() - start;
    }

    private Uri pushIntoContentResolver(ArrayList<ContentProviderOperation> operationList) {
        Uri uri = null;
        try {
            ContentProviderResult[] results = this.mContentResolver.applyBatch("com.android.contacts", operationList);
            if (!(results == null || results.length == 0 || results[0] == null)) {
                uri = results[0].uri;
            }
            return uri;
        } catch (RemoteException e) {
            Log.e(LOG_TAG, String.format("%s: %s", new Object[]{e.toString(), e.getMessage()}));
            return null;
        } catch (OperationApplicationException e2) {
            Log.e(LOG_TAG, String.format("%s: %s", new Object[]{e2.toString(), e2.getMessage()}));
            return null;
        }
    }

    public ArrayList<Uri> getCreatedUris() {
        return this.mCreatedUris;
    }
}
