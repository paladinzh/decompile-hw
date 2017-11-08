package com.huawei.rcs.db;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.huawei.harassmentinterception.common.CommonObject.SmsMsgInfo;
import com.huawei.harassmentinterception.common.Tables.TbMessages;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.util.CommonHelper;
import com.huawei.rcs.common.HwRcsCommonObject;
import com.huawei.rcs.common.HwRcsCommonObject.FileRcsExtColumns;
import com.huawei.rcs.common.HwRcsCommonObject.RcsExtendColumn;
import com.huawei.rcs.util.HwRcsFeatureEnabler;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.phonematch.PhoneMatch;
import java.util.ArrayList;
import java.util.List;

public class RcsDBAdapter {
    private static final Uri BLACKLIST_MESSAGE_URI = Uri.parse("content://rcsim/blacklist_messages");
    private static final String TAG = "RcsDBAdapter";
    private static final Uri messages_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), "interception_messages");

    private void addInterceptedRcsMsg(android.content.Context r11, long r12, int r14, int r15) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0032 in list [B:9:0x00d3]
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
        r10 = this;
        r8 = new android.content.ContentValues;
        r8.<init>();
        r6 = 0;
        r7 = r10.getChatTypeFromMsgType(r14);
        r0 = "RcsDBAdapter";
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "addInterceptedRcsMsg: type = ";
        r1 = r1.append(r2);
        r1 = r1.append(r7);
        r2 = ", rcsMsgType = ";
        r1 = r1.append(r2);
        r1 = r1.append(r14);
        r1 = r1.toString();
        com.huawei.systemmanager.util.HwLog.d(r0, r1);
        switch(r7) {
            case 1: goto L_0x0033;
            case 2: goto L_0x0032;
            default: goto L_0x0032;
        };
    L_0x0032:
        return;
    L_0x0033:
        r0 = r11.getContentResolver();	 Catch:{ all -> 0x00d8 }
        r1 = com.huawei.rcs.common.HwRcsCommonObject.rcs_message_sms_uri;	 Catch:{ all -> 0x00d8 }
        r2 = 3;	 Catch:{ all -> 0x00d8 }
        r2 = new java.lang.String[r2];	 Catch:{ all -> 0x00d8 }
        r3 = "address";	 Catch:{ all -> 0x00d8 }
        r4 = 0;	 Catch:{ all -> 0x00d8 }
        r2[r4] = r3;	 Catch:{ all -> 0x00d8 }
        r3 = "body";	 Catch:{ all -> 0x00d8 }
        r4 = 1;	 Catch:{ all -> 0x00d8 }
        r2[r4] = r3;	 Catch:{ all -> 0x00d8 }
        r3 = "date";	 Catch:{ all -> 0x00d8 }
        r4 = 2;	 Catch:{ all -> 0x00d8 }
        r2[r4] = r3;	 Catch:{ all -> 0x00d8 }
        r3 = "sms._id = ?";	 Catch:{ all -> 0x00d8 }
        r4 = 1;	 Catch:{ all -> 0x00d8 }
        r4 = new java.lang.String[r4];	 Catch:{ all -> 0x00d8 }
        r5 = java.lang.String.valueOf(r12);	 Catch:{ all -> 0x00d8 }
        r9 = 0;	 Catch:{ all -> 0x00d8 }
        r4[r9] = r5;	 Catch:{ all -> 0x00d8 }
        r5 = 0;	 Catch:{ all -> 0x00d8 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ all -> 0x00d8 }
        if (r6 == 0) goto L_0x00d1;	 Catch:{ all -> 0x00d8 }
    L_0x0062:
        r0 = r6.getCount();	 Catch:{ all -> 0x00d8 }
        if (r0 <= 0) goto L_0x00d1;	 Catch:{ all -> 0x00d8 }
    L_0x0068:
        r6.moveToFirst();	 Catch:{ all -> 0x00d8 }
        r0 = "phone";	 Catch:{ all -> 0x00d8 }
        r1 = 0;	 Catch:{ all -> 0x00d8 }
        r1 = r6.getString(r1);	 Catch:{ all -> 0x00d8 }
        r1 = com.huawei.harassmentinterception.db.DBAdapter.formatPhoneNumber(r1);	 Catch:{ all -> 0x00d8 }
        r8.put(r0, r1);	 Catch:{ all -> 0x00d8 }
        r0 = "name";	 Catch:{ all -> 0x00d8 }
        r1 = 0;	 Catch:{ all -> 0x00d8 }
        r1 = r6.getString(r1);	 Catch:{ all -> 0x00d8 }
        r1 = com.huawei.harassmentinterception.db.DBAdapter.getNameFromBlacklist(r11, r1);	 Catch:{ all -> 0x00d8 }
        r8.put(r0, r1);	 Catch:{ all -> 0x00d8 }
        r0 = "body";	 Catch:{ all -> 0x00d8 }
        r1 = 1;	 Catch:{ all -> 0x00d8 }
        r1 = r6.getString(r1);	 Catch:{ all -> 0x00d8 }
        r8.put(r0, r1);	 Catch:{ all -> 0x00d8 }
        r0 = "date";	 Catch:{ all -> 0x00d8 }
        r1 = 2;	 Catch:{ all -> 0x00d8 }
        r1 = r6.getString(r1);	 Catch:{ all -> 0x00d8 }
        r8.put(r0, r1);	 Catch:{ all -> 0x00d8 }
        r0 = "sub_id";	 Catch:{ all -> 0x00d8 }
        r1 = 0;	 Catch:{ all -> 0x00d8 }
        r1 = java.lang.Integer.valueOf(r1);	 Catch:{ all -> 0x00d8 }
        r8.put(r0, r1);	 Catch:{ all -> 0x00d8 }
        r0 = "message_type";	 Catch:{ all -> 0x00d8 }
        r1 = java.lang.Integer.valueOf(r14);	 Catch:{ all -> 0x00d8 }
        r8.put(r0, r1);	 Catch:{ all -> 0x00d8 }
        r0 = "message_id";	 Catch:{ all -> 0x00d8 }
        r1 = java.lang.Long.valueOf(r12);	 Catch:{ all -> 0x00d8 }
        r8.put(r0, r1);	 Catch:{ all -> 0x00d8 }
        r0 = "block_reason";	 Catch:{ all -> 0x00d8 }
        r1 = java.lang.Integer.valueOf(r15);	 Catch:{ all -> 0x00d8 }
        r8.put(r0, r1);	 Catch:{ all -> 0x00d8 }
        r0 = r11.getContentResolver();	 Catch:{ all -> 0x00d8 }
        r1 = messages_uri;	 Catch:{ all -> 0x00d8 }
        r0.insert(r1, r8);	 Catch:{ all -> 0x00d8 }
    L_0x00d1:
        if (r6 == 0) goto L_0x0032;
    L_0x00d3:
        r6.close();
        goto L_0x0032;
    L_0x00d8:
        r0 = move-exception;
        if (r6 == 0) goto L_0x00de;
    L_0x00db:
        r6.close();
    L_0x00de:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.rcs.db.RcsDBAdapter.addInterceptedRcsMsg(android.content.Context, long, int, int):void");
    }

    private void updateBlacklist(android.content.Context r11, long r12, int r14, int r15) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0047 in list [B:9:0x0082]
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
        r10 = this;
        r6 = 0;
        r0 = "RcsDBAdapter";
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = " updateBlacklist rcsMsgType =  ";
        r1 = r1.append(r2);
        r1 = r1.append(r14);
        r1 = r1.toString();
        com.huawei.systemmanager.util.HwLog.w(r0, r1);
        r8 = r10.getChatTypeFromMsgType(r14);
        r0 = "RcsDBAdapter";
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "updateBlacklist: type = ";
        r1 = r1.append(r2);
        r1 = r1.append(r8);
        r2 = ", rcsMsgType = ";
        r1 = r1.append(r2);
        r1 = r1.append(r14);
        r1 = r1.toString();
        com.huawei.systemmanager.util.HwLog.d(r0, r1);
        switch(r8) {
            case 1: goto L_0x0048;
            case 2: goto L_0x0047;
            default: goto L_0x0047;
        };
    L_0x0047:
        return;
    L_0x0048:
        r0 = r11.getContentResolver();	 Catch:{ all -> 0x0086 }
        r1 = com.huawei.rcs.common.HwRcsCommonObject.rcs_message_sms_uri;	 Catch:{ all -> 0x0086 }
        r2 = 1;	 Catch:{ all -> 0x0086 }
        r2 = new java.lang.String[r2];	 Catch:{ all -> 0x0086 }
        r3 = "address";	 Catch:{ all -> 0x0086 }
        r4 = 0;	 Catch:{ all -> 0x0086 }
        r2[r4] = r3;	 Catch:{ all -> 0x0086 }
        r3 = "sms._id = ?";	 Catch:{ all -> 0x0086 }
        r4 = 1;	 Catch:{ all -> 0x0086 }
        r4 = new java.lang.String[r4];	 Catch:{ all -> 0x0086 }
        r5 = java.lang.String.valueOf(r12);	 Catch:{ all -> 0x0086 }
        r9 = 0;	 Catch:{ all -> 0x0086 }
        r4[r9] = r5;	 Catch:{ all -> 0x0086 }
        r5 = 0;	 Catch:{ all -> 0x0086 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ all -> 0x0086 }
        if (r6 == 0) goto L_0x0080;	 Catch:{ all -> 0x0086 }
    L_0x006b:
        r0 = r6.getCount();	 Catch:{ all -> 0x0086 }
        if (r0 <= 0) goto L_0x0080;	 Catch:{ all -> 0x0086 }
    L_0x0071:
        r6.moveToFirst();	 Catch:{ all -> 0x0086 }
        r0 = 0;	 Catch:{ all -> 0x0086 }
        r0 = r6.getString(r0);	 Catch:{ all -> 0x0086 }
        r7 = com.huawei.harassmentinterception.db.DBAdapter.formatPhoneNumber(r0);	 Catch:{ all -> 0x0086 }
        com.huawei.harassmentinterception.db.DBAdapter.updateBlacklistStatInfo(r11, r7, r15);	 Catch:{ all -> 0x0086 }
    L_0x0080:
        if (r6 == 0) goto L_0x0047;
    L_0x0082:
        r6.close();
        goto L_0x0047;
    L_0x0086:
        r0 = move-exception;
        if (r6 == 0) goto L_0x008c;
    L_0x0089:
        r6.close();
    L_0x008c:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.rcs.db.RcsDBAdapter.updateBlacklist(android.content.Context, long, int, int):void");
    }

    public boolean isNotOriginalType(Context context, SmsMsgInfo msg) {
        if (!HwRcsFeatureEnabler.isRcsEnabled()) {
            return false;
        }
        RcsExtendColumn rcsExtendColumn = getRcsExtendColumn(context, msg);
        int type = getChatTypeFromMsgType(rcsExtendColumn.getMessageType());
        HwLog.i(TAG, "getFileRcsColumns: type = " + type + ", msgType = " + rcsExtendColumn.getMessageType());
        switch (type) {
            case 0:
                return false;
            case 1:
                restoreRcsMessage(rcsExtendColumn, context);
                return true;
            case 2:
                return true;
            default:
                HwLog.w(TAG, "Unknown message type to restore.");
                return true;
        }
    }

    public List<SmsMsgInfo> getOriginalMsgList(Context context, List<SmsMsgInfo> msgList) {
        if (!HwRcsFeatureEnabler.isRcsEnabled()) {
            return msgList;
        }
        List<SmsMsgInfo> retList = new ArrayList();
        ArrayList<RcsExtendColumn> rcsExtendColumnList = new ArrayList();
        for (int i = 0; i < msgList.size(); i++) {
            SmsMsgInfo message = (SmsMsgInfo) msgList.get(i);
            RcsExtendColumn rcsExtendColumn = getRcsExtendColumn(context, message);
            switch (getChatTypeFromMsgType(rcsExtendColumn.getMessageType())) {
                case 0:
                    retList.add(message);
                    break;
                case 1:
                    rcsExtendColumnList.add(rcsExtendColumn);
                    break;
                default:
                    break;
            }
        }
        if (rcsExtendColumnList.size() != 0) {
            restoreRcsMessageBatch(rcsExtendColumnList, context);
        }
        return retList;
    }

    private String getRcsMsgPhoneAddress(Context context, long rcsMsgId) {
        if (-1 == rcsMsgId) {
            HwLog.w(TAG, "getRcsMsgPhoneAddress: Invalid rcsMsgId");
            return null;
        }
        String phone = null;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(HwRcsCommonObject.rcs_message_sms_uri, new String[]{"address"}, "sms._id = ?", new String[]{String.valueOf(rcsMsgId)}, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                phone = cursor.getString(0);
            }
            if (cursor != null) {
                cursor.close();
            }
            return phone;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String querySingleImMsgInfo(Context context, long interceptedMsgId) {
        if (!HwRcsFeatureEnabler.isRcsEnabled()) {
            return "";
        }
        String body = "";
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(HwRcsCommonObject.rcs_message_sms_uri, new String[]{"address", TbMessages.BODY, "date"}, "sms._id = ?", new String[]{String.valueOf(interceptedMsgId)}, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                body = cursor.getString(1);
            }
            if (cursor != null) {
                cursor.close();
            }
            return body;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private RcsExtendColumn getRcsExtendColumn(Context context, SmsMsgInfo msg) {
        String phone = msg.getPhone();
        long date = msg.getDate();
        Cursor cursor = null;
        RcsExtendColumn ret = new RcsExtendColumn();
        if (CommonHelper.isInvalidPhoneNumber(phone)) {
            HwLog.w(TAG, "getRcsExtendColumn : Invalid phone number");
            return ret;
        }
        String[] selectionArgs;
        String selection = "";
        if (PhoneMatch.getPhoneNumberMatchInfo(phone).isExactMatch()) {
            selection = "phone=? AND date = ?";
            selectionArgs = new String[]{PhoneMatch.getPhoneNumberMatchInfo(phone).getPhoneNumber(), String.valueOf(date)};
        } else {
            selection = "phone like ? AND date = ?";
            selectionArgs = new String[]{"%" + PhoneMatch.getPhoneNumberMatchInfo(phone).getPhoneNumber(), String.valueOf(date)};
        }
        try {
            cursor = context.getContentResolver().query(messages_uri, new String[]{"message_type", "message_id", "group_message_name"}, selection, selectionArgs, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                ret.setMessageType(cursor.getInt(0));
                ret.setMessageId(cursor.getLong(1));
                ret.setGroupMessageName(cursor.getString(2));
            }
            if (cursor != null) {
                cursor.close();
            }
            return ret;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void restoreRcsMessage(RcsExtendColumn rcsExtendColumn, Context context) {
        Intent intent = new Intent();
        intent.setAction(HwRcsCommonObject.ACTION_RESTORE_RCS_MESSAGE);
        intent.putExtra("message_type", rcsExtendColumn.getMessageType());
        intent.putExtra("message_id", rcsExtendColumn.getMessageId());
        intent.putExtra("group_message_name", rcsExtendColumn.getGroupMessageName());
        context.sendBroadcast(intent, "org.gsma.joyn.RCS_USE_CHAT");
    }

    private void restoreRcsMessageBatch(ArrayList<RcsExtendColumn> rcsExtendColumnList, Context context) {
        if (rcsExtendColumnList != null && rcsExtendColumnList.size() != 0) {
            Intent intent = new Intent();
            intent.setAction(HwRcsCommonObject.ACTION_RESTORE_RCS_MESSAGE_BATCH);
            int[] msgTypeList = new int[rcsExtendColumnList.size()];
            long[] msgIdArray = new long[rcsExtendColumnList.size()];
            ArrayList<String> grouopMsgNameList = new ArrayList();
            int i = 0;
            for (RcsExtendColumn rcsExtendColumn : rcsExtendColumnList) {
                msgTypeList[i] = rcsExtendColumn.getMessageType();
                msgIdArray[i] = rcsExtendColumn.getMessageId();
                grouopMsgNameList.add(rcsExtendColumn.getGroupMessageName());
                i++;
            }
            intent.putExtra(HwRcsCommonObject.INTENT_KEY_MSG_TYPE_BATCH, msgTypeList);
            intent.putExtra(HwRcsCommonObject.INTENT_KEY_MSG_ID_BATCH, msgIdArray);
            intent.putExtra(HwRcsCommonObject.INTENT_KEY_GROUP_MSG_NAME_BATCH, grouopMsgNameList);
            context.sendBroadcast(intent, "org.gsma.joyn.RCS_USE_CHAT");
        }
    }

    public int handleRcsByBlackList(Context context, long rcsMsgId, int rcsMsgType, String phone) {
        if (!HwRcsFeatureEnabler.isRcsEnabled()) {
            return -1;
        }
        if (-1 == rcsMsgId) {
            HwLog.e(TAG, "handleRcsByBlackList : rcsMsgId is error");
            return -1;
        }
        if (TextUtils.isEmpty(phone)) {
            phone = getRcsMsgPhoneAddress(context, rcsMsgId);
        }
        if (DBAdapter.checkMatchBlacklist(context, phone, 1) != 0) {
            HwLog.i(TAG, "handleIm: Not in blacklist or option doesn't match, pass");
            return -1;
        } else if (addRcsToInterceptRecord(context, rcsMsgId, rcsMsgType, 1)) {
            HwLog.i(TAG, "handleRcsByBlackList: The im should be blocked");
            return 1;
        } else {
            HwLog.i(TAG, "handleRcsByBlackList: The im should be blocked, but intercept error!");
            return 0;
        }
    }

    public boolean addRcsToInterceptRecord(Context context, long interceptedMsgId, int rcsMsgType, int blockReason) {
        if (!HwRcsFeatureEnabler.isRcsEnabled()) {
            return false;
        }
        if (-1 == interceptedMsgId) {
            HwLog.e(TAG, "Invalid interceptedMsgId");
            return false;
        }
        try {
            addInterceptedRcsMsg(context, interceptedMsgId, rcsMsgType, blockReason);
            updateBlacklist(context, interceptedMsgId, rcsMsgType, 0);
            if (DBAdapter.getUnreadMsgCount(context) > 0) {
                CommonHelper.sendNotificationForAll(context);
            }
            HsmStat.statE((int) Events.E_HARASSMENT_BLOCK_MSG, HsmStatConst.PARAM_VAL, String.valueOf(blockReason));
            return true;
        } catch (Exception e) {
            HwLog.e(TAG, "addToInterceptRecord: Exception ", e);
            return false;
        }
    }

    public Cursor getMsgListCursorInBatches(Context context, int nBatchSize, long nLastMsgDate) {
        if (!HwRcsFeatureEnabler.isRcsEnabled()) {
            return null;
        }
        String strSelection = "";
        if (nLastMsgDate <= 0) {
            strSelection = String.format("SELECT address, body, date FROM (SELECT address, body, date FROM chat UNION ALL SELECT address, body, date FROM sms order by date) GROUP BY address ORDER BY date DESC LIMIT %1$s ", new Object[]{Integer.valueOf(nBatchSize)});
        } else {
            strSelection = String.format("SELECT address, body, date FROM (SELECT address, body, date FROM chat UNION ALL SELECT address, body, date FROM sms order by date) WHERE date<%1$d GROUP BY address ORDER BY date DESC LIMIT %2$s ", new Object[]{Long.valueOf(nLastMsgDate), Integer.valueOf(nBatchSize)});
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(BLACKLIST_MESSAGE_URI, null, strSelection, null, null);
        } catch (Exception e) {
            HwLog.e(TAG, "getMsgListCursorInBatches error");
        }
        return cursor;
    }

    public void setFileRcsBodyText(Context context, SmsMsgInfo msgInfo, TextView textView) {
        if (HwRcsFeatureEnabler.isRcsEnabled()) {
            RcsExtendColumn rcscolum = getRcsExtendColumn(context, msgInfo);
            boolean isFileType = isFileTypeFromMsgType(rcscolum.getMessageType());
            HwLog.e(TAG, "setFileRcsBodyText -> msg id = " + rcscolum.getMessageId() + ", isFileType = " + isFileType);
            if (isFileType) {
                HwLog.e(TAG, "setFileRcsBodyText -> find file msg. msg id = " + rcscolum.getMessageId() + ", msgType = " + rcscolum.getMessageType());
                if (textView != null) {
                    FileRcsExtColumns fileColumns = getFileRcsColumns(context, rcscolum.getMessageId(), rcscolum.getMessageType());
                    HwLog.d(TAG, "setFileRcsBodyText -> msg id = " + rcscolum.getMessageId() + ", fileType = " + fileColumns.getFileType());
                    textView.setText(fileColumns.formatInfo(context));
                }
            }
        }
    }

    private FileRcsExtColumns getFileRcsColumns(Context context, long msgId, int msgType) {
        Cursor cursor = null;
        FileRcsExtColumns ret = new FileRcsExtColumns();
        int chatType = getChatTypeFromMsgType(msgType);
        int fileType = getFileTypeFromMsgType(msgType);
        HwLog.d(TAG, "getFileRcsColumns: chatType = " + chatType + ", msgType = " + msgType);
        if (fileType == 96) {
            ret.setFileType(96);
            return ret;
        }
        try {
            cursor = context.getContentResolver().query(HwRcsCommonObject.rcs_message_file_uri, new String[]{"file_name", "file_size", "compress_path"}, "msg_id = ? AND chat_type = ?", new String[]{String.valueOf(msgId), String.valueOf(chatType)}, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                ret.setFileName(cursor.getString(0));
                ret.setFileSize(cursor.getLong(1));
                ret.setFileImage(cursor.getString(2));
                ret.setFileType(getFileTypeFromMsgType(msgType));
            }
            if (cursor != null) {
                cursor.close();
            }
            return ret;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int getChatTypeFromMsgType(int msgType) {
        return msgType & 15;
    }

    public boolean isFileTypeFromMsgType(int msgType) {
        if (HwRcsFeatureEnabler.isRcsEnabled() && (msgType & HwRcsCommonObject.BLACKLIST_MSG_FILE_TYPE_MASK) != 0) {
            return true;
        }
        return false;
    }

    private int getFileTypeFromMsgType(int msgType) {
        if ((msgType & HwRcsCommonObject.BLACKLIST_MSG_FILE_TYPE_MASK) == 16) {
            return 16;
        }
        if ((msgType & HwRcsCommonObject.BLACKLIST_MSG_FILE_TYPE_MASK) == 32) {
            return 32;
        }
        if ((msgType & HwRcsCommonObject.BLACKLIST_MSG_FILE_TYPE_MASK) == 48) {
            return 48;
        }
        if ((msgType & HwRcsCommonObject.BLACKLIST_MSG_FILE_TYPE_MASK) == 64) {
            return 64;
        }
        if ((msgType & HwRcsCommonObject.BLACKLIST_MSG_FILE_TYPE_MASK) == 96) {
            return 96;
        }
        return 80;
    }

    public int handleRcsByBlockStranger(Context context, long rcsMsgId, int rcsMsgType, String phone) {
        if (!HwRcsFeatureEnabler.isRcsEnabled()) {
            return -1;
        }
        if (-1 == rcsMsgId) {
            HwLog.e(TAG, "handleRcsByBlockStranger : rcsMsgId is error");
            return -1;
        } else if (TextUtils.isEmpty(phone)) {
            HwLog.d(TAG, "handleRcsByBlockStranger phone num is null, do not block.");
            return -1;
        } else if (DBAdapter.isContact(context, phone)) {
            HwLog.d(TAG, "handleRcsByBlockStranger: in contact, pass");
            return 0;
        } else if (!addRcsToInterceptRecord(context, rcsMsgId, rcsMsgType, 3)) {
            return 0;
        } else {
            HwLog.i(TAG, "handleRcsByBlockStranger: The im should be no contact");
            return 1;
        }
    }

    public void setRcsFtLayout(LinearLayout smmaryView, TextView ftMsgName, TextView ftMsgSize, TextView bodyView, Context context, SmsMsgInfo msgInfo) {
        if (HwRcsFeatureEnabler.isRcsEnabled() && smmaryView != null && ftMsgName != null && ftMsgSize != null && bodyView != null) {
            RcsExtendColumn rcscolum = getRcsExtendColumn(context, msgInfo);
            if (isFileTypeFromMsgType(rcscolum.getMessageType())) {
                smmaryView.setVisibility(0);
                FileRcsExtColumns fileColumn = getFileRcsColumns(context, rcscolum.getMessageId(), rcscolum.getMessageType());
                if (fileColumn.getFileType() == 80) {
                    ftMsgName.setText(context.getString(R.string.rcs_file_trans_detail_name, new Object[]{""}));
                    String fullName = fileColumn.getFileName();
                    if (!TextUtils.isEmpty(fullName)) {
                        if (fullName.split("/").length > 0) {
                            ftMsgName.setText(context.getString(R.string.rcs_file_trans_detail_name, new Object[]{fullName.split("/")[fullName.split("/").length - 1]}));
                        }
                    }
                } else {
                    ftMsgName.setVisibility(8);
                }
                ftMsgSize.setText(context.getString(R.string.harassment_mms_size, new Object[]{Integer.valueOf((int) Math.ceil(((double) fileColumn.getFileSize()) / 1024.0d))}));
                bodyView.setText(context.getString(R.string.rcs_file_trans_detail_info));
            }
        }
    }
}
