package com.android.mms.transaction;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.Telephony.Sms.Inbox;
import android.provider.Telephony.Threads;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.PreferenceUtils;
import com.android.mms.util.Recycler;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.SimCursorManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WapPushMsg {
    public static final String WAP_PUSH_MESSAGE_ID = MmsConfig.getMmsStringConfig("defaultWapPushMessageId", "WAPPush");
    private int mMsgType = -1;
    private WapPushItem mPushMsg = null;

    private static class WapPushItem {
        public String created;
        public String expired;
        public String from;
        public String href;
        public String priority;
        public String serviceCenter;
        public String si_id;
        public String subId;
        public String text;

        private WapPushItem() {
        }

        public String toString() {
            return "HREF:" + this.href + " PRIORITY:" + this.priority + " SID:" + this.si_id + " CREATED:" + this.created + " EXPIRED: " + this.expired;
        }
    }

    public WapPushMsg(int type) {
        this.mMsgType = type;
        this.mPushMsg = new WapPushItem();
        initDefaultValues();
    }

    private void initDefaultValues() {
        this.mPushMsg.from = WAP_PUSH_MESSAGE_ID;
        this.mPushMsg.si_id = "";
        this.mPushMsg.text = null;
    }

    private boolean updateWappushRowBySi_id(Context context) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues updateValues = new ContentValues();
        if (MmsConfig.getEnableWapSenderAddress()) {
            updateValues.put("address", this.mPushMsg.from);
        } else {
            updateValues.put("address", WAP_PUSH_MESSAGE_ID);
        }
        updateValues.put("date", Long.valueOf(System.currentTimeMillis()));
        updateValues.put("read", Integer.valueOf(0));
        updateValues.put("seen", Integer.valueOf(0));
        if (TextUtils.isEmpty(this.mPushMsg.text)) {
            updateValues.put("body", this.mPushMsg.href);
        } else {
            String body;
            if (TextUtils.isEmpty(this.mPushMsg.href)) {
                body = this.mPushMsg.text;
            } else {
                body = this.mPushMsg.text + "\nURL:" + this.mPushMsg.href;
            }
            updateValues.put("body", body);
        }
        updateValues.put("thread_id", Long.valueOf(Threads.getOrCreateThreadId(context, updateValues.getAsString("address"))));
        if (resolver.update(Inbox.CONTENT_URI, updateValues, "si_id = ? AND sub_id = ? ", new String[]{this.mPushMsg.si_id, this.mPushMsg.subId}) > 0) {
            return true;
        }
        return false;
    }

    public Uri storeWapPushMessage(Context context) {
        boolean deleteOld = false;
        WapPushItem lOlderMsg = null;
        String lSID = getSid();
        if (this.mMsgType == 0 && !TextUtils.isEmpty(lSID)) {
            lOlderMsg = getOlderMessage(context, lSID);
            if (lOlderMsg != null) {
                if (convertTime(lOlderMsg.created) > convertTime(this.mPushMsg.created)) {
                    MLog.i("WapPushMsg", "Out of order message; Discard");
                    return null;
                } else if (!MmsConfig.getEnableWapPushReplace()) {
                    deleteOld = true;
                }
            }
        }
        if ("signal-delete".equals(this.mPushMsg.priority)) {
            MLog.i("WapPushMsg", "Action Delete: Discard");
            if (lOlderMsg != null) {
                deleteMsg(context, lOlderMsg.si_id);
            }
            return null;
        }
        String body;
        ContentResolver resolver = context.getContentResolver();
        if (!(!MmsConfig.getEnableWapPushReplace() || this.mPushMsg.si_id == null || "".equals(this.mPushMsg.si_id))) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(Uri.withAppendedPath(Inbox.CONTENT_URI, "si_id"), new String[]{"si_id", "rowid"}, "si_id = ? AND sub_id = ? ", new String[]{this.mPushMsg.si_id, this.mPushMsg.subId}, null);
                if (cursor != null && cursor.moveToFirst() && updateWappushRowBySi_id(context)) {
                    Uri resultUri = ContentUris.withAppendedId(Inbox.CONTENT_URI, cursor.getLong(1));
                    MLog.d("WapPushMsg", "update Wappush done, return URI: " + resultUri.toString());
                    if (cursor != null) {
                        cursor.close();
                    }
                    return resultUri;
                } else if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable e) {
                MLog.e("WapPushMsg", "update wappush exception ", e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        ContentValues values = new ContentValues();
        if (!(this.mPushMsg.si_id == null || "".equals(this.mPushMsg.si_id))) {
            values.put("si_id", this.mPushMsg.si_id);
        }
        if (MmsConfig.getEnableWapSenderAddress()) {
            values.put("address", this.mPushMsg.from);
        } else {
            values.put("address", WAP_PUSH_MESSAGE_ID);
        }
        values.put("date", Long.valueOf(System.currentTimeMillis()));
        values.put("protocol", Integer.valueOf(0));
        values.put("read", Integer.valueOf(0));
        values.put("reply_path_present", Integer.valueOf(0));
        values.put("service_center", this.mPushMsg.serviceCenter);
        if (TextUtils.isEmpty(this.mPushMsg.text)) {
            body = this.mPushMsg.href;
        } else if (TextUtils.isEmpty(this.mPushMsg.href)) {
            body = this.mPushMsg.text;
        } else {
            body = this.mPushMsg.text + "\nURL:" + this.mPushMsg.href;
        }
        values.put("body", body);
        values.put("addr_body", HwMessageUtils.getAddressPos(body));
        values.put("time_body", HwMessageUtils.getTimePosString(body));
        values.put("mid", lSID);
        values.put("created", this.mPushMsg.created);
        values.put("mtype", Integer.valueOf(10));
        String subId = getAttributeValueString(10);
        values.put("network_type", Integer.valueOf(MessageUtils.getNetworkType(Integer.parseInt(subId))));
        if (MessageUtils.isCTCdmaCardInGsmMode()) {
            subId = "0";
        }
        values.put("sub_id", subId);
        Long threadId = Long.valueOf(Threads.getOrCreateThreadId(context, values.getAsString("address")));
        values.put("thread_id", threadId);
        Uri insertedUri = SqliteWrapper.insert(context, Inbox.CONTENT_URI, values);
        if (deleteOld && lOlderMsg != null) {
            deleteMsg(context, lOlderMsg.si_id);
        }
        if (threadId == null) {
            threadId = Long.valueOf(0);
        }
        Recycler.getSmsRecycler().deleteOldMessagesByThreadId(context, threadId.longValue());
        if ("signal-none".equals(this.mPushMsg.priority)) {
            MLog.i("WapPushMsg", "Signal None: Dont Notify User: Just store");
            return null;
        }
        Context contextEx = context;
        boolean needSaveIcc = false;
        if (MessageUtils.isMultiSimEnabled() && MmsConfig.isSaveModeMultiCardPerf()) {
            if (PreferenceUtils.getUsingSIMCardStorageWithSubId(context, "0".equals(subId) ? 0 : 1)) {
                needSaveIcc = true;
            }
        } else if (PreferenceUtils.getUsingSIMCardStorage(context)) {
            needSaveIcc = true;
        }
        if (needSaveIcc) {
            final Context context2 = context;
            ThreadEx.execute(new Runnable() {
                public void run() {
                    WapPushMsg.this.insertSMSToIcc(context2);
                    if (MessageUtils.isMultiSimEnabled()) {
                        SimCursorManager.self().clearCursor("0".equals(WapPushMsg.this.getAttributeValueString(3)) ? 1 : 2);
                    } else {
                        SimCursorManager.self().clearCursor();
                    }
                    LocalBroadcastManager.getInstance(context2).sendBroadcast(new Intent("android.simcard.action.SMS_SIM_RECEIVED"));
                }
            });
        }
        return insertedUri;
    }

    public void insertSMSToIcc(Context context) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        Uri uri = Uri.parse("content://sms/addtoicc");
        String subId = getAttributeValueString(3);
        if (MessageUtils.isMultiSimEnabled()) {
            if ("0".equals(subId)) {
                uri = Uri.parse("content://sms/copytoicc1");
            } else {
                uri = Uri.parse("content://sms/copytoicc2");
            }
        }
        if (this.mPushMsg == null) {
            MLog.e("WapPushMsg", "insertSMSToIcc, bad parameter");
            return;
        }
        values.put("status", Integer.valueOf(1));
        values.put("address", this.mPushMsg.from);
        values.put("timestamp", Long.valueOf(System.currentTimeMillis()));
        if (TextUtils.isEmpty(this.mPushMsg.text)) {
            values.put("body", this.mPushMsg.href);
        } else {
            String body;
            if (TextUtils.isEmpty(this.mPushMsg.href)) {
                body = this.mPushMsg.text;
            } else {
                body = this.mPushMsg.text + "\nURL:" + this.mPushMsg.href;
            }
            values.put("body", body);
        }
        if (resolver.insert(uri, values) == null) {
            MLog.e("WapPushMsg", "save sms to icc failed");
        }
    }

    public void setAttributeValue(int attrType, String value) {
        MLog.v("WapPushMsg", "attribute type =  " + attrType);
        switch (attrType) {
            case 0:
                this.mPushMsg.from = value;
                return;
            case 1:
                this.mPushMsg.href = value;
                return;
            case 2:
                this.mPushMsg.priority = value;
                return;
            case 3:
                this.mPushMsg.si_id = value;
                return;
            case 4:
                this.mPushMsg.text = value;
                return;
            case 5:
                this.mPushMsg.created = value;
                return;
            case 6:
                this.mPushMsg.expired = value;
                return;
            case 9:
                this.mPushMsg.serviceCenter = value;
                return;
            case 10:
                this.mPushMsg.subId = value;
                return;
            default:
                MLog.v("WapPushMsg", "Unknown attribute type =  " + attrType);
                return;
        }
    }

    public String getAttributeValueString(int attrType) {
        switch (attrType) {
            case 0:
                return this.mPushMsg.from;
            case 1:
                return this.mPushMsg.href;
            case 2:
                return this.mPushMsg.priority;
            case 3:
                return this.mPushMsg.si_id;
            case 4:
                return this.mPushMsg.text;
            case 5:
                return this.mPushMsg.created;
            case 6:
                return this.mPushMsg.expired;
            case 9:
                return this.mPushMsg.serviceCenter;
            case 10:
                return this.mPushMsg.subId;
            default:
                MLog.v("WapPushMsg", "Unknown attribute type =  " + attrType);
                return null;
        }
    }

    private static long convertTime(String aTime) {
        if (TextUtils.isEmpty(aTime)) {
            return 0;
        }
        SimpleDateFormat lDateFormat;
        int lTimelength = aTime.length();
        if (lTimelength == 20) {
            lDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'z");
        } else if (lTimelength == 19) {
            lDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssz");
        } else if (lTimelength == 14) {
            lDateFormat = new SimpleDateFormat("yyyyMMddHHmmssz");
        } else if (lTimelength != 12) {
            return 0;
        } else {
            lDateFormat = new SimpleDateFormat("yyyyMMddHHmmz");
        }
        try {
            Date lDate = lDateFormat.parse(aTime + "+0000");
            MLog.i("WapPushMsg", "LDate" + lDate);
            return lDate.getTime();
        } catch (ParseException e) {
            MLog.e("WapPushMsg", "ParseException" + e);
            return 0;
        }
    }

    private void deleteMsg(Context context, String aSID) {
        SqliteWrapper.delete(context, Uri.parse("content://sms/" + aSID), null, null);
        try {
            MessagingNotification.cancelNotification(context, Integer.parseInt(aSID));
        } catch (Exception e) {
            MLog.e("WapPushMsg", "ParseException in deleteMsg() " + e);
        }
    }

    private String getSid() {
        if (TextUtils.isEmpty(this.mPushMsg.si_id)) {
            return this.mPushMsg.href;
        }
        return this.mPushMsg.si_id;
    }

    private WapPushItem getOlderMessage(Context aContext, String aSID) {
        SQLiteException ex;
        Throwable th;
        WapPushItem wapPushItem = null;
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(aContext, Inbox.CONTENT_URI, new String[]{"_id,created"}, "mtype = 10 AND mid = '" + aSID + "'", null, null);
            if (cursor != null && cursor.moveToFirst()) {
                WapPushItem lOlderMsg = new WapPushItem();
                try {
                    lOlderMsg.si_id = cursor.getString(0);
                    lOlderMsg.created = cursor.getString(1);
                    wapPushItem = lOlderMsg;
                } catch (SQLiteException e) {
                    ex = e;
                    wapPushItem = lOlderMsg;
                    try {
                        MLog.e("WapPushMsg", "ParseException" + ex);
                        if (cursor != null) {
                            cursor.close();
                        }
                        return wapPushItem;
                    } catch (Throwable th2) {
                        th = th2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e2) {
            ex = e2;
            MLog.e("WapPushMsg", "ParseException" + ex);
            if (cursor != null) {
                cursor.close();
            }
            return wapPushItem;
        }
        return wapPushItem;
    }
}
