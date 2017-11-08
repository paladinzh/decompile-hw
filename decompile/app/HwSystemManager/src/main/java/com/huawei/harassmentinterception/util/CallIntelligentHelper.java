package com.huawei.harassmentinterception.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.harassmentinterception.common.CommonObject.NumberMarkInfo;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.numbermark.HsmNumberMarkerManager;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.io.Closeable;

public class CallIntelligentHelper {
    private static final int CLOUDMARK_DEFAULT = 0;
    private static final String EROR_MSG = "error_msg";
    private static final Uri NUMBERMARK_URI = Uri.parse("content://com.android.contacts.app/number_mark");
    private static final String TAG = "CallIntelligentHelper";
    private static final int TIMEOUT_DEFAULT = -1;
    private static final Uri URI_CSP_NUMBERMARK = Uri.parse("content://com.android.contacts.app/number_mark_to_system_manager");
    private static final Uri URI_NUMBERMARK = Uri.parse("content://com.huawei.systemmanager.BlockCheckProvider/numbermark");

    @Deprecated
    public static NumberMarkInfo getNumberMarkInfo(Context context, String phone) {
        Uri queryUri = Uri.withAppendedPath(URI_NUMBERMARK, phone);
        Cursor cursor = context.getContentResolver().query(queryUri, new String[]{String.valueOf(2), String.valueOf(18), String.valueOf(2000)}, null, null, null);
        NumberMarkInfo numberMarkInfo = null;
        if (cursor != null && cursor.getCount() > 0) {
            numberMarkInfo = getNumberMarkInfoFromCursor(phone, cursor);
        }
        Closeables.close(cursor);
        return numberMarkInfo;
    }

    private static NumberMarkInfo getNumberMarkInfoFromCursor(String phone, Cursor cursor) {
        cursor.moveToNext();
        String tagCount = cursor.getString(cursor.getColumnIndex("tagCount"));
        String tagType = cursor.getString(cursor.getColumnIndex("tagType"));
        NumberMarkInfo numberMarkInfo = new NumberMarkInfo(Integer.parseInt(tagType), Integer.parseInt(tagCount), cursor.getString(cursor.getColumnIndex("tagName")), phone);
        cursor.close();
        return numberMarkInfo;
    }

    @Deprecated
    public static NumberMarkInfo getNumberMarkInfoFromContact(String number) {
        HwLog.d(TAG, "queryLocalNumberMark");
        if (TextUtils.isEmpty(number) || !TextUtils.isDigitsOnly(number)) {
            HwLog.w(TAG, "not valid number");
            return null;
        }
        Closeable closeable = null;
        NumberMarkInfo numberMarkInfo;
        try {
            StringBuilder selection = new StringBuilder();
            selection.append("number = ?");
            String[] projection = new String[]{ConstValues.MARK_CLASSIFY, ConstValues.MARK_NAME};
            closeable = GlobalContext.getContext().getContentResolver().query(URI_CSP_NUMBERMARK, projection, selection.toString(), new String[]{number}, null);
            if (closeable == null) {
                HwLog.i(TAG, "no data in local");
                numberMarkInfo = null;
                return numberMarkInfo;
            }
            int classifyIndex = closeable.getColumnIndex(ConstValues.MARK_CLASSIFY);
            int nameIndex = closeable.getColumnIndex(ConstValues.MARK_NAME);
            int cloudIndex = closeable.getColumnIndex(ConstValues.ISCLOUD);
            while (closeable.moveToNext()) {
                if (closeable.getInt(cloudIndex) == 0) {
                    String classify = closeable.getString(classifyIndex);
                    String name = closeable.getString(nameIndex);
                    HwLog.i(TAG, "classify = " + classify + " name= " + name);
                    NumberMarkInfo numberMarkInfo2 = new NumberMarkInfo(NumberMarkInfo.getTypeFromClassify(classify), 0, name, number);
                    Closeables.close(closeable);
                    return numberMarkInfo2;
                }
            }
            Closeables.close(closeable);
            HwLog.i(TAG, "no match local numbermark data");
            return null;
        } catch (Exception e) {
            numberMarkInfo = TAG;
            HwLog.e(numberMarkInfo, "queryLocalNumberMarkFromCSP error" + e.getMessage());
        } finally {
            Closeables.close(closeable);
        }
    }

    public static NumberMarkInfo queryNumberMark(Context context, String number) {
        Exception e;
        if (context == null || TextUtils.isEmpty(number)) {
            HwLog.e(TAG, "queryNumberMark param error");
            return null;
        }
        HsmNumberMarkerManager.getInstance(context);
        HwLog.i(TAG, "queryNumberMark called, numberMarkUseNetwork:" + HsmNumberMarkerManager.isContactUseNetwokMark(context));
        NumberMarkInfo numberMarkInfo = null;
        Closeable closeable = null;
        try {
            closeable = context.getContentResolver().query(NUMBERMARK_URI.buildUpon().appendQueryParameter("number", number).build(), null, null, null, null);
            if (closeable == null) {
                HwLog.i(TAG, "queryNumberMark cursor is null");
                Closeables.close(closeable);
                return null;
            }
            int classifyIndex = closeable.getColumnIndex(ConstValues.MARK_CLASSIFY);
            int nameIndex = closeable.getColumnIndex(ConstValues.MARK_NAME);
            int cloudIndex = closeable.getColumnIndex(ConstValues.ISCLOUD);
            int markCountIndex = closeable.getColumnIndex(ConstValues.MARK_COUNT);
            int supplierIndex = closeable.getColumnIndex(ConstValues.SUPPLIER);
            int descriptionIndex = closeable.getColumnIndex(ConstValues.DESCRIPTION);
            int saveTimeStampIndex = closeable.getColumnIndex(ConstValues.SAVE_TIMESTAMP);
            int isTimeoutIndex = closeable.getColumnIndex(EROR_MSG);
            NumberMarkInfo info = null;
            while (closeable.moveToNext()) {
                try {
                    if (info == null || !info.getIsLocal()) {
                        String classify = closeable.getString(classifyIndex);
                        String name = closeable.getString(nameIndex);
                        int cloudMark = closeable.getInt(cloudIndex);
                        int markCount = closeable.getInt(markCountIndex);
                        String supplier = closeable.getString(supplierIndex);
                        String description = closeable.getString(descriptionIndex);
                        String saveTimeStamp = closeable.getString(saveTimeStampIndex);
                        HwLog.i(TAG, "queryNumberMark, classify:" + classify + ", name:" + name + ", cloudMark:" + cloudMark + ", markCount:" + markCount + ", supplier:" + supplier + ",isTimeoutIndex:" + isTimeoutIndex);
                        numberMarkInfo = new NumberMarkInfo();
                        numberMarkInfo.mMarkNumber = number;
                        numberMarkInfo.mMarkType = NumberMarkInfo.getTypeFromClassify(classify);
                        numberMarkInfo.mMarkName = name;
                        numberMarkInfo.setIsLocal(cloudMark == 0);
                        numberMarkInfo.mMarkCount = markCount;
                        numberMarkInfo.setDescription(description);
                        numberMarkInfo.setSaveTimeStamp(saveTimeStamp);
                        numberMarkInfo.setSupplier(supplier);
                        numberMarkInfo.setClassify(classify);
                        numberMarkInfo.setIsTimeout(isTimeoutIndex != -1);
                        info = numberMarkInfo;
                    } else {
                        Closeables.close(closeable);
                        return info;
                    }
                } catch (Exception e2) {
                    e = e2;
                    numberMarkInfo = info;
                } catch (Throwable th) {
                    Throwable th2 = th;
                    numberMarkInfo = info;
                }
            }
            Closeables.close(closeable);
            numberMarkInfo = info;
            return numberMarkInfo;
        } catch (Exception e3) {
            e = e3;
        }
        try {
            HwLog.e(TAG, "queryNumberMark error", e);
            Closeables.close(closeable);
            return numberMarkInfo;
        } catch (Throwable th3) {
            th2 = th3;
            Closeables.close(closeable);
            throw th2;
        }
    }
}
