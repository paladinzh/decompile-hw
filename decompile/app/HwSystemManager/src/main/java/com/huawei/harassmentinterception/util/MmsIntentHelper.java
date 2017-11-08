package com.huawei.harassmentinterception.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.DeliveryInd;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduParser;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.ReadOrigInd;
import com.google.android.mms.util.SqliteWrapper;
import com.huawei.harassmentinterception.common.CommonObject.MessageInfo;
import com.huawei.harassmentinterception.common.CommonObject.MsgIntentWrapper;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.strategy.StrategyManager;
import com.huawei.systemmanager.antimal.MalwareConst;
import com.huawei.systemmanager.push.PushResponse;
import com.huawei.systemmanager.util.HwLog;
import java.nio.charset.Charset;

public class MmsIntentHelper {
    private static final int STATE_UNSTARTED = 128;
    private static final String SUB_ID = "sub_id";
    private static final String TAG = "MmsIntentHelper";

    public static int handleWapPushDeliverAction(Context context, Bundle wapPush) {
        HwLog.d(TAG, "handleWapPushDeliverAction: Receive a WapPush");
        if (wapPush == null) {
            HwLog.w(TAG, "handleWapPushDeliverAction: Invalid WapPush info");
            return -1;
        } else if (wapPush.containsKey(ConstValues.HANDLE_KEY_AIDL_WAPPUSHINTENT)) {
            Intent wapintent = (Intent) wapPush.getParcelable(ConstValues.HANDLE_KEY_AIDL_WAPPUSHINTENT);
            if (wapintent == null) {
                HwLog.w(TAG, "handleWapPushDeliverAction: fail to get wappush intent");
                return -1;
            } else if ("application/vnd.wap.mms-message".equals(wapintent.getType())) {
                MessageInfo mmsMsgInfo = getMmsInfoFromIntent(context, wapintent);
                if (mmsMsgInfo == null) {
                    HwLog.w(TAG, "handleWapPushDeliverAction: this is not a normal mms ");
                    return -1;
                }
                return StrategyManager.getInstance(context).applyStrategyForMms(new MsgIntentWrapper(mmsMsgInfo, wapintent));
            } else {
                HwLog.w(TAG, "handleWapPushDeliverAction: this wap push is not mms Type");
                return 0;
            }
        } else {
            HwLog.w(TAG, "handleWapPushDeliverAction: Invalid param, no wappush info");
            return -1;
        }
    }

    public static MessageInfo getMmsInfoFromIntent(Context context, Intent intent) {
        Exception e;
        byte[] pushData = intent.getByteArrayExtra(PushResponse.DATA_FIELD);
        if (pushData == null) {
            HwLog.w(TAG, "handleWapPushDeliverAction: this is not a normal mms ");
            return null;
        }
        GenericPdu genericPdu = new PduParser(pushData, false).parse();
        if (genericPdu == null) {
            HwLog.w(TAG, "handleWapPushDeliverAction: this is not a normal mms ");
            return null;
        } else if (genericPdu.getMessageType() != 130) {
            HwLog.w(TAG, "handleWapPushDeliverAction: this is not notification mms");
            return null;
        } else {
            MessageInfo mmsMsgInfo;
            NotificationInd nInd = (NotificationInd) genericPdu;
            try {
                String phone = nInd.getFrom().getString();
                String name = "";
                String body = null;
                if (nInd.getSubject() != null) {
                    body = nInd.getSubject().getString();
                }
                byte[] pdu = pushData;
                mmsMsgInfo = new MessageInfo(phone, name, body, nInd.getMessageSize(), System.currentTimeMillis(), nInd.getExpiry(), intent.getIntExtra("subscription", 0), pushData, 1);
                try {
                    HwLog.i(TAG, mmsMsgInfo.toString());
                } catch (Exception e2) {
                    e = e2;
                    HwLog.e(TAG, e.getMessage(), e);
                    return mmsMsgInfo;
                }
            } catch (Exception e3) {
                e = e3;
                mmsMsgInfo = null;
                HwLog.e(TAG, e.getMessage(), e);
                return mmsMsgInfo;
            }
            return mmsMsgInfo;
        }
    }

    public static boolean writeMmsToMmsInbox(Context context, MessageInfo mmsMsgInfo) {
        HwLog.d(TAG, "call writeMmsToMmsInbox");
        if (mmsMsgInfo == null) {
            HwLog.i(TAG, "call writeMmsToMmsInbox, but not a valid mms");
            return false;
        }
        HwLog.d(TAG, "mms is = " + mmsMsgInfo.toString());
        byte[] pduData = mmsMsgInfo.getPdu();
        if (pduData.length <= 0) {
            HwLog.e(TAG, "pduData null,means not a valid mms ");
            return false;
        }
        GenericPdu pdu = new PduParser(pduData, false).parse();
        if (pdu == null) {
            HwLog.e(TAG, "pdu  parsered null,means not a valid mms ");
            return false;
        }
        PduPersister p = PduPersister.getPduPersister(context);
        ContentResolver cr = context.getContentResolver();
        int type = pdu.getMessageType();
        HwLog.i(TAG, "message type:" + type);
        if (type == 130) {
            Uri uri;
            try {
                uri = p.persist(pdu, Inbox.CONTENT_URI, true, true, null);
            } catch (MmsException e) {
                e.printStackTrace();
                HwLog.e(TAG, e.getMessage(), e);
                uri = null;
            }
            if (uri == null) {
                return false;
            }
            ContentValues values = new ContentValues(4);
            values.put("sub_id", Integer.valueOf(mmsMsgInfo.getSubId()));
            values.put(MalwareConst.INSTALL_SPACE_TIME, Integer.valueOf(128));
            values.put("exp", Long.valueOf(mmsMsgInfo.getExpDate()));
            values.put("date", Long.valueOf(mmsMsgInfo.getDate() / 1000));
            HwLog.d(TAG, "write MMS thread = " + findThreadId(context, pdu, type, uri));
            HwLog.i(TAG, "update mms inbox = " + SqliteWrapper.update(context, cr, uri, values, null, null));
            return true;
        }
        HwLog.w(TAG, "this is not a MMS  in type: MESSAGE_TYPE_NOTIFICATION_IND");
        return false;
    }

    private static long findThreadId(Context context, GenericPdu pdu, int type, Uri uri) {
        String messageId = "";
        int mmsType = 128;
        StringBuilder sb = new StringBuilder(40);
        if (type == 134) {
            messageId = new String(((DeliveryInd) pdu).getMessageId(), Charset.defaultCharset());
        } else if (type != 130) {
            messageId = new String(((ReadOrigInd) pdu).getMessageId(), Charset.defaultCharset());
        } else if (uri == null) {
            HwLog.d(TAG, "uri is null");
            return -1;
        } else {
            mmsType = 130;
            try {
                sb.append("_id");
                sb.append('=');
                sb.append(ContentUris.parseId(uri));
            } catch (NumberFormatException e) {
                HwLog.e(TAG, "ContentUris parse Id NumberFormatException error >>>>" + e);
                return -1;
            } catch (UnsupportedOperationException e2) {
                HwLog.e(TAG, "ContentUris parse Id has UnsupportedOperationException error >>>" + e2);
                return -1;
            }
        }
        if (130 != type) {
            sb.append("m_id");
            sb.append('=');
            sb.append(DatabaseUtils.sqlEscapeString(messageId));
        }
        sb.append(" AND ");
        sb.append("m_type");
        sb.append('=');
        sb.append(mmsType);
        HwLog.i(TAG, "findThreadId fun sb=" + sb.toString());
        Context context2 = context;
        Cursor cursor = SqliteWrapper.query(context2, context.getContentResolver(), Mms.CONTENT_URI, new String[]{"thread_id"}, sb.toString(), null, null);
        if (cursor != null) {
            try {
                if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                    HwLog.i(TAG, "threadid?=" + cursor.getLong(0));
                    long j = cursor.getLong(0);
                    return j;
                }
                cursor.close();
            } finally {
                cursor.close();
            }
        }
        HwLog.d(TAG, "can not find Thread Id");
        return -1;
    }
}
