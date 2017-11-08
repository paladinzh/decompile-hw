package com.android.contacts.hap.roaming;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDiskIOException;
import android.net.Uri;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.util.HwLog;
import java.util.LinkedList;

public class RoamingLearnManage {
    static final String[] _PROJECTION = new String[]{"_id", "number", "date", "duration", "type", "name", "numbertype", "numberlabel", "is_read"};
    private static final LinkedList<String> mCallLogList = new LinkedList();
    private boolean mIsHasCallLogNumber;
    private final LinkedList<RoamingLearnCarrier> mLearnList;
    QueryThread mRoamingLearnIdThread;
    Context mUpData4Context;

    private class QueryThread extends Thread {
        private volatile boolean mDone = false;

        public QueryThread() {
            super("RoamingLearnManage.QueryThread");
        }

        public void stopProcessing() {
            this.mDone = true;
        }

        public void run() {
            while (!this.mDone) {
                String str = null;
                synchronized (RoamingLearnManage.mCallLogList) {
                    if (!RoamingLearnManage.mCallLogList.isEmpty()) {
                        str = (String) RoamingLearnManage.mCallLogList.removeFirst();
                    }
                }
                if (str != null) {
                    try {
                        RoamingLearnManage.this.updateDataTableOfData4(RoamingLearnManage.getInstance().mUpData4Context, str);
                    } catch (SQLiteDiskIOException e) {
                        HwLog.e("RoamingLearnManage", "android.database.sqlite.SQLiteDiskIOException: disk I/O error");
                    }
                } else {
                    synchronized (RoamingLearnManage.mCallLogList) {
                        if (RoamingLearnManage.mCallLogList.isEmpty()) {
                            RoamingLearnManage.this.mIsHasCallLogNumber = false;
                        } else {
                            RoamingLearnManage.this.mIsHasCallLogNumber = true;
                        }
                        while (!RoamingLearnManage.this.mIsHasCallLogNumber) {
                            try {
                                RoamingLearnManage.mCallLogList.wait(1000);
                            } catch (InterruptedException e2) {
                            }
                        }
                    }
                }
            }
        }
    }

    private static class RoamingLearnManageHanlder {
        public static final RoamingLearnManage instance = new RoamingLearnManage();

        private RoamingLearnManageHanlder() {
        }
    }

    private RoamingLearnManage() {
        this.mLearnList = new LinkedList();
        this.mUpData4Context = null;
        this.mIsHasCallLogNumber = false;
        this.mRoamingLearnIdThread = null;
    }

    public static RoamingLearnManage getInstance() {
        return RoamingLearnManageHanlder.instance;
    }

    public void addRoamingLearnCarrie(RoamingLearnCarrier roamingLearnCarrier) {
        if (roamingLearnCarrier != null) {
            synchronized (this.mLearnList) {
                if (!this.mLearnList.contains(roamingLearnCarrier) && this.mLearnList.size() < 100) {
                    this.mLearnList.add(roamingLearnCarrier);
                }
            }
        }
    }

    private void removeRoamingLearnCarrie(RoamingLearnCarrier roamingLearnCarrier) {
        if (roamingLearnCarrier != null) {
            synchronized (this.mLearnList) {
                if (this.mLearnList.contains(roamingLearnCarrier)) {
                    this.mLearnList.remove(roamingLearnCarrier);
                }
            }
        }
    }

    public void addCallLog(Context context, String number) {
        if (!IsPhoneNetworkRoamingUtils.isStringEmpty(number)) {
            if (context != null && this.mUpData4Context == null) {
                this.mUpData4Context = context.getApplicationContext();
            }
            synchronized (mCallLogList) {
                if (!mCallLogList.contains(number) && mCallLogList.size() < 100) {
                    mCallLogList.add(number);
                    this.mIsHasCallLogNumber = true;
                    mCallLogList.notifyAll();
                }
            }
        }
    }

    public void updateDataTableOfData4(Context context, String number) {
        if (context != null && this.mLearnList.size() != 0) {
            RoamingLearnCarrier roamingLearnCarrier = getRoamingLearnCarrier(number);
            if (roamingLearnCarrier == null) {
                HwLog.i("RoamingLearnManage", "update, there is no data");
                return;
            }
            ContentResolver contentResolver = context.getContentResolver();
            boolean updateRes = false;
            if (queryValidDurationByNumber(contentResolver, roamingLearnCarrier)) {
                updateRes = updateData4ByNumber(contentResolver, number, roamingLearnCarrier);
            }
            if (HwLog.HWDBG) {
                HwLog.d("RoamingLearnManage", "update, result:" + updateRes + " size:" + this.mLearnList.size());
            }
            removeRoamingLearnCarrie(roamingLearnCarrier);
        }
    }

    private RoamingLearnCarrier getRoamingLearnCarrier(String number) {
        if (number == null || number.length() == 0) {
            HwLog.d("RoamingLearnManage", "get RoamingLearnCarrier, the paramter is null");
            return null;
        }
        RoamingLearnCarrier result = null;
        for (RoamingLearnCarrier roamingLearnCarrier : this.mLearnList) {
            if (number.equals(roamingLearnCarrier.getDialNumber())) {
                result = roamingLearnCarrier;
                break;
            }
        }
        return result;
    }

    private boolean updateData4ByNumber(ContentResolver contentResolver, String number, RoamingLearnCarrier roamingLearnCarrier) {
        String data4;
        String data1;
        boolean z = true;
        if (IsPhoneNetworkRoamingUtils.isPhoneNetworkRoamging()) {
            if (number.startsWith("+")) {
                data4 = number;
            } else {
                data4 = IsPhoneNetworkRoamingUtils.produectData4(number);
            }
            data1 = roamingLearnCarrier.getOriginalNumber();
        } else {
            data4 = IsPhoneNetworkRoamingUtils.produectData4(number);
            data1 = number;
        }
        if (IsPhoneNetworkRoamingUtils.isStringEmpty(data4) || IsPhoneNetworkRoamingUtils.isStringEmpty(data1)) {
            HwLog.d("RoamingLearnManage", "update, the param is null");
            return false;
        }
        Uri uri = Data.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put("data1", data1);
        values.put("data4", data4);
        StringBuffer where = new StringBuffer();
        where.append("replace(replace(replace(replace(").append("data1").append(", '-', ''), ' ', ''), '(', ''), ')', '') = ?");
        where.append(" AND (data4 IS NULL OR data4 = '')");
        if (contentResolver.update(uri, values, where.toString(), new String[]{data1}) <= 0) {
            z = false;
        }
        return z;
    }

    private boolean queryValidDurationByNumber(ContentResolver contentResolver, RoamingLearnCarrier roamingLearnCarrier) {
        boolean hasValidDuration = false;
        if (contentResolver == null || roamingLearnCarrier == null) {
            return false;
        }
        String dilaNumber = roamingLearnCarrier.getDialNumber();
        if (TextUtils.isEmpty(dilaNumber)) {
            return false;
        }
        String selectionCalls;
        String[] selectionArgs;
        if (dilaNumber.startsWith(IsPhoneNetworkRoamingUtils.DEFAULT_INTERNATIONAL_PHONE_PREFIX)) {
            selectionCalls = "duration > 0 AND number = ? ";
            selectionArgs = new String[]{dilaNumber};
        } else {
            selectionCalls = "duration > 0 AND number = ? AND countryiso = ?";
            selectionArgs = new String[]{dilaNumber, roamingLearnCarrier.getmDailNetworkCountryIso()};
        }
        Cursor callsCursor = contentResolver.query(QueryUtil.getCallsContentUri(), _PROJECTION, selectionCalls, selectionArgs, "date DESC");
        if (callsCursor != null) {
            try {
                hasValidDuration = callsCursor.getCount() > 0;
                callsCursor.close();
            } catch (Throwable th) {
                callsCursor.close();
            }
        }
        return hasValidDuration;
    }

    public static boolean saveRoamingLearnCarrier(Context context, String number, String dailNumber) {
        getInstance().addRoamingLearnCarrie(new RoamingLearnCarrier(dailNumber, number, true));
        return true;
    }

    public synchronized void startRequestProcessing() {
        if (this.mRoamingLearnIdThread == null) {
            this.mRoamingLearnIdThread = new QueryThread();
            this.mRoamingLearnIdThread.setPriority(1);
            this.mRoamingLearnIdThread.start();
        }
    }

    public synchronized void stopRequestProcessing() {
        if (this.mRoamingLearnIdThread != null) {
            this.mRoamingLearnIdThread.stopProcessing();
            this.mRoamingLearnIdThread.interrupt();
            this.mRoamingLearnIdThread = null;
        }
    }
}
