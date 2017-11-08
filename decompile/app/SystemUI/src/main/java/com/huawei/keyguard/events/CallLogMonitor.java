package com.huawei.keyguard.events;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.OperationCanceledException;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import com.huawei.keyguard.events.MonitorImpl.MonitorChangeListener;
import com.huawei.keyguard.util.HwLog;

public class CallLogMonitor extends MonitorImpl {
    private static final String[] PROJECTION = new String[]{"type", "name", "number"};

    public static class CallLogInfo {
        public int mMissedcount;
        public String mName;
        public String mNumber;

        public String toString() {
            return "[CallLogInfo] mMissedcount = " + this.mMissedcount + ", mName = " + this.mName + ", mNumber = " + this.mNumber;
        }

        public int getMissedcount() {
            return this.mMissedcount;
        }

        public void setmMissedcount(int missedCount) {
            this.mMissedcount = missedCount;
        }
    }

    public CallLogMonitor(Context context, MonitorChangeListener callback, int monitorId) {
        super(context, callback, monitorId);
    }

    public void register() {
        registerContent(CallLog.CONTENT_URI);
    }

    public static Intent getCallLogIntent(int missedCount) {
        Intent intent = new Intent();
        if (missedCount > 0) {
            intent.setAction("com.huawei.android.intent.action.CALL");
            intent.setClassName("com.android.contacts", "com.android.contacts.activities.CallLogActivity");
        } else {
            intent.setAction("android.intent.action.DIAL");
            intent.setClassName("com.android.contacts", "com.android.contacts.activities.DialtactsActivity");
        }
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setFlags(805306368);
        return intent;
    }

    Object onQueryDatabase() {
        CallLogInfo info = null;
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(Calls.CONTENT_URI, PROJECTION, "(type=3 and new= 1)", null, null);
        } catch (SQLiteException ex) {
            HwLog.w("CalllogMonitor", "query CallLog.ex = ", ex);
        } catch (OperationCanceledException ex2) {
            HwLog.w("CalllogMonitor", "query CallLog.ex = ", ex2);
        } catch (SecurityException ex3) {
            HwLog.w("CalllogMonitor", "query CallLog.ex = ", ex3);
        }
        if (cursor != null) {
            info = new CallLogInfo();
            info.setmMissedcount(cursor.getCount());
            if (cursor.moveToLast()) {
                info.mName = cursor.getString(cursor.getColumnIndex("name"));
                info.mNumber = cursor.getString(cursor.getColumnIndex("number"));
            }
            cursor.close();
        } else {
            HwLog.w("CalllogMonitor", "onQueryDatabase query CallLog fail");
        }
        return info;
    }
}
