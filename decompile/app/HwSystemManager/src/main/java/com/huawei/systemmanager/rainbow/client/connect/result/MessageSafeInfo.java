package com.huawei.systemmanager.rainbow.client.connect.result;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.rainbow.db.CloudDBAdapter;
import com.huawei.systemmanager.rainbow.db.base.CloudConst.MessageSafeConfigFile;
import com.huawei.systemmanager.util.HwLog;
import java.util.Arrays;
import java.util.HashSet;

public class MessageSafeInfo extends WhiteBlackListInfo {
    private static final int ADD_FLAG = 0;
    private static final int DELETE_FLAG = 1;
    private static final String TAG = "MessageSafeInfo";

    public MessageSafeInfo(int type) {
        super(type);
    }

    boolean validOutputData() {
        return true;
    }

    public void updateDatabase(Context ctx) {
        if (ctx != null) {
            updatePartnerInfo(ctx);
        }
    }

    private void updatePartnerInfo(Context ctx) {
        try {
            CloudDBAdapter.getInstance(ctx);
            for (ContentValues values : this.mContValuesList) {
                int deleteFlag = values.getAsInteger("status").intValue();
                String partner = values.getAsString(MessageSafeConfigFile.COL_PARTNER);
                String secureLink = values.getAsString(MessageSafeConfigFile.COL_SECURE_LINK);
                String messageNo = values.getAsString(MessageSafeConfigFile.COL_MESSAGE_NUMBER);
                if (!TextUtils.isEmpty(partner)) {
                    if (deleteFlag == 1) {
                        ctx.getContentResolver().delete(MessageSafeConfigFile.CONTENT_OUTERTABLE_NUMBER_URI, "messageNo = '" + messageNo + "'", null);
                    } else if (deleteFlag == 0) {
                        if (!TextUtils.isEmpty(messageNo)) {
                            ContentValues contentvalues = new ContentValues();
                            contentvalues.put(MessageSafeConfigFile.COL_MESSAGE_NUMBER, messageNo);
                            contentvalues.put(MessageSafeConfigFile.COL_PARTNER, partner);
                            ctx.getContentResolver().insert(MessageSafeConfigFile.CONTENT_OUTERTABLE_NUMBER_URI, contentvalues);
                        }
                    }
                    updateLinks(ctx, secureLink, partner, deleteFlag);
                }
            }
        } catch (Exception e) {
            HwLog.e(TAG, e.getMessage());
        }
    }

    private void updateLinks(Context ctx, String secureLink, String partner, int updateFlag) {
        if (!TextUtils.isEmpty(secureLink)) {
            Cursor cursor = ctx.getContentResolver().query(MessageSafeConfigFile.CONTENT_OUTERTABLE_LINK_URI, null, "partner = '" + partner + "'", null, null);
            String links = "";
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        if (cursor.moveToFirst()) {
                            links = cursor.getString(cursor.getColumnIndex(MessageSafeConfigFile.COL_SECURE_LINK));
                            if (!TextUtils.isEmpty(links)) {
                                String temp = getLinks(secureLink, links, updateFlag);
                                if (TextUtils.equals(temp, links)) {
                                    if (cursor != null) {
                                        cursor.close();
                                    }
                                    return;
                                }
                                links = temp;
                                updateLinkRecord(ctx, partner, temp, false);
                            } else {
                                return;
                            }
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (updateFlag == 0) {
                updateLinkRecord(ctx, partner, secureLink + SqlMarker.SQL_END, true);
            }
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String getLinks(String link, String links, int flag) {
        HashSet<String> linkSet = new HashSet(Arrays.asList(links.split(SqlMarker.SQL_END)));
        if (linkSet.contains(link)) {
            if (flag == 1) {
                linkSet.remove(link);
            }
        } else if (flag == 0) {
            linkSet.add(link);
        }
        StringBuilder sb = new StringBuilder();
        for (String l : linkSet) {
            sb.append(l);
            sb.append(SqlMarker.SQL_END);
        }
        return sb.toString();
    }

    private void updateLinkRecord(Context ctx, String partner, String links, boolean newAdd) {
        if (!newAdd) {
            String partnerSelection = "partner = '" + partner + "'";
            ctx.getContentResolver().delete(MessageSafeConfigFile.CONTENT_OUTERTABLE_LINK_URI, partnerSelection, null);
            if (TextUtils.isEmpty(links)) {
                ctx.getContentResolver().delete(MessageSafeConfigFile.CONTENT_OUTERTABLE_NUMBER_URI, partnerSelection, null);
                return;
            }
        }
        ContentValues contentvalues = new ContentValues();
        contentvalues.put(MessageSafeConfigFile.COL_PARTNER, partner);
        contentvalues.put(MessageSafeConfigFile.COL_SECURE_LINK, links);
        ctx.getContentResolver().insert(MessageSafeConfigFile.CONTENT_OUTERTABLE_LINK_URI, contentvalues);
    }
}
