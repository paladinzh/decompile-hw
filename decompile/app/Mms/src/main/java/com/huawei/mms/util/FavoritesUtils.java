package com.huawei.mms.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Telephony.Sms;
import com.android.mms.MmsApp;
import com.android.mms.ui.FavoritesActivity;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.rcs.util.RcsFavoritesUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class FavoritesUtils {
    private static final String[] FAV_SMS_PROJECTION = new String[]{"origin_id", "address", "body"};
    private static final long[] NULL_ARRAY_NULL = new long[0];
    private static final String[] SMS_PROJECTION = new String[]{"_id", "address", "body"};
    public static final Uri URI_FAV = Uri.parse("content://fav-mms/");
    public static final Uri URI_FAV_MMS = Uri.parse("content://fav-mms/mms");
    public static final Uri URI_FAV_SMS = Uri.parse("content://fav-mms/sms");
    private static RcsFavoritesUtils mHwCust = new RcsFavoritesUtils();

    private static class SmsData {
        String mAddress;
        String mBody;
        long mId;

        public SmsData(long id, String body, String address) {
            this.mId = id;
            this.mBody = body;
            this.mAddress = address;
        }
    }

    public static RcsFavoritesUtils getHwCust() {
        return mHwCust;
    }

    public static final void gotoFavoritesActivity(Context context) {
        Intent intent = new Intent(context, FavoritesActivity.class);
        intent.setFlags(872415232);
        context.startActivity(intent);
    }

    public static String getSelectCondition(Collection<Long> idLists) {
        StringBuilder selection = new StringBuilder(" _id IN ( ");
        int i = 0;
        for (Long id : idLists) {
            selection.append(i == 0 ? " " : ", ");
            selection.append(id);
            i++;
        }
        selection.append(" ) ");
        return selection.toString();
    }

    public static long[] getDuplicateSms(IContentProvider icp, List<Long> ids) {
        if (ids == null || ids.size() == 0) {
            return NULL_ARRAY_NULL;
        }
        try {
            Bundle bd = icp.call("com.android.mms", "CHECK-DUPLICATE", "sms-multy", getBundle(ids));
            if (bd != null && bd.containsKey("result-array")) {
                return bd.getLongArray("result-array");
            }
        } catch (NullPointerException e) {
            MLog.e("FavUtils", "CHECK existsDuplicateSms ", (Throwable) e);
        } catch (RemoteException e2) {
            MLog.e("FavUtils", "CHECK existsDuplicateSms ", (Throwable) e2);
        }
        return NULL_ARRAY_NULL;
    }

    public static long[] getDuplicateMms(IContentProvider icp, List<Long> ids) {
        if (ids == null || ids.size() == 0) {
            return NULL_ARRAY_NULL;
        }
        try {
            Bundle bd = icp.call("com.android.mms", "CHECK-DUPLICATE", "mms-multy", getBundle(ids));
            if (bd != null && bd.containsKey("result-array")) {
                return bd.getLongArray("result-array");
            }
        } catch (RemoteException e) {
            MLog.e("FavUtils", "CHECK existsDuplicateSms ", (Throwable) e);
        }
        return NULL_ARRAY_NULL;
    }

    private static Bundle getBundle(List<Long> ids) {
        Bundle bd = new Bundle();
        bd.putString("where-condition", getSelectionString("origin_id", (List) ids));
        return bd;
    }

    public static String getSelectionString(String col, List<Long> idList) {
        if (idList == null || idList.size() == 0) {
            return col + "=-1";
        }
        StringBuilder selection = new StringBuilder(" ").append(col).append(" in (").append(idList.get(0));
        for (int i = 1; i < idList.size(); i++) {
            selection.append(", ").append(idList.get(i));
        }
        return selection.append(" )").toString();
    }

    public static ContentValues getAddFavoritesContent(String type, List<Long> idList) {
        ContentValues cv = new ContentValues();
        cv.put("inser_total", Integer.valueOf(idList.size()));
        cv.put("oper-type", type);
        cv.put("where-condition", getSelectionString("_id", (List) idList));
        return cv;
    }

    public static int checkAndRemoveDuplicateMsgs(Context context, List<Long> smsIds, List<Long> mmsIds) {
        IContentProvider icp = context.getContentResolver().acquireProvider(URI_FAV);
        if (icp == null) {
            return -1;
        }
        return removeDuplicate(smsIds, filterDumplicateSms(getDuplicateSms(icp, smsIds))) + removeDuplicate(mmsIds, getDuplicateMms(icp, mmsIds));
    }

    private static int removeDuplicate(Collection<Long> from, long[] tomove) {
        int i = 0;
        if (tomove == null || tomove.length == 0) {
            return 0;
        }
        int removed = 0;
        int length = tomove.length;
        while (i < length) {
            if (from.remove(Long.valueOf(tomove[i]))) {
                removed++;
            }
            i++;
        }
        return removed;
    }

    private static long[] filterDumplicateSms(long[] smsIds) {
        return filterDumplicateFromTable(smsIds, Sms.CONTENT_URI);
    }

    public static long[] filterDumplicateFromTable(long[] smsIds, Uri tableUri) {
        if (smsIds == null || smsIds.length == 0 || tableUri == null) {
            return smsIds;
        }
        ArrayList<SmsData> smsData = new ArrayList();
        HashMap<Long, SmsData> favSmsData = new HashMap();
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(MmsApp.getApplication().getApplicationContext(), tableUri, SMS_PROJECTION, getSelectionString("_id", smsIds), null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            do {
                smsData.add(new SmsData(cursor.getLong(0), cursor.getString(2), cursor.getString(1)));
            } while (cursor.moveToNext());
            if (cursor != null) {
                cursor.close();
            }
            Cursor cursor2 = null;
            try {
                cursor2 = SqliteWrapper.query(MmsApp.getApplication().getApplicationContext(), URI_FAV_SMS, FAV_SMS_PROJECTION, getSelectionString("origin_id", smsIds), null, null);
                if (cursor2 == null || !cursor2.moveToFirst()) {
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                    return null;
                }
                SmsData data;
                do {
                    favSmsData.put(Long.valueOf(cursor2.getLong(0)), new SmsData(cursor2.getLong(0), cursor2.getString(2), cursor2.getString(1)));
                } while (cursor2.moveToNext());
                if (cursor2 != null) {
                    cursor2.close();
                }
                Iterator<SmsData> iterator = smsData.iterator();
                while (iterator.hasNext()) {
                    data = (SmsData) iterator.next();
                    SmsData favData = (SmsData) favSmsData.get(Long.valueOf(data.mId));
                    if (favData == null) {
                        iterator.remove();
                    } else if (!favData.mAddress.equals(data.mAddress) || !favData.mBody.equals(data.mBody)) {
                        iterator.remove();
                    }
                }
                long[] result = new long[smsData.size()];
                int i = 0;
                for (SmsData data2 : smsData) {
                    int i2 = i + 1;
                    result[i] = data2.mId;
                    i = i2;
                }
                return result;
            } catch (Exception e) {
                MLog.e("FavUtils", "query fav table error", (Throwable) e);
                if (cursor2 != null) {
                    cursor2.close();
                }
            } catch (Throwable th) {
                if (cursor2 != null) {
                    cursor2.close();
                }
            }
        } catch (Exception e2) {
            MLog.e("FavUtils", "query Sms table error", (Throwable) e2);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th2) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static String getSelectionString(String col, long[] idList) {
        if (idList == null || idList.length == 0) {
            return col + "=-1";
        }
        StringBuilder selection = new StringBuilder(" ").append(col).append(" in (").append(idList[0]);
        for (int i = 1; i < idList.length; i++) {
            selection.append(", ").append(idList[i]);
        }
        return selection.append(" )").toString();
    }
}
