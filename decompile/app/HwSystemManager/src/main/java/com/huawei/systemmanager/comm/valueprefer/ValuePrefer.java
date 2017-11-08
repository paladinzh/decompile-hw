package com.huawei.systemmanager.comm.valueprefer;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class ValuePrefer {
    private static final Uri AUTHORITY_URI = ValueContentProvider.URI_AUTHORITY;
    private static final String TAG = "ValuePrefer";

    public static final String getValueString(Context ctx, String key, String defValue) {
        Bundle res = ctx.getContentResolver().call(AUTHORITY_URI, ValueContentProvider.METHOD_GET, key, null);
        if (res != null) {
            return res.getString(key, defValue);
        }
        HwLog.e(TAG, "getValueString res bundle is null!");
        return defValue;
    }

    public static final boolean checkIfValueExsist(Context ctx, String key) {
        Bundle res = ctx.getContentResolver().call(AUTHORITY_URI, ValueContentProvider.METHOD_CHECK, key, null);
        if (res != null) {
            return res.getBoolean("key_result", false);
        }
        HwLog.e(TAG, "checkIfValueExsist res bundle is null!");
        return false;
    }

    public static final boolean putValueString(Context ctx, String key, String value) {
        Bundle in = new Bundle(1);
        in.putString(key, value);
        Bundle res = ctx.getContentResolver().call(AUTHORITY_URI, ValueContentProvider.METHOD_PUT, key, in);
        if (res != null) {
            return res.getBoolean("key_result", false);
        }
        HwLog.e(TAG, "putValueString res bundle is null!");
        return false;
    }

    public static final ContentValues geValueBulk(Context ctx, ArrayList<ValuePair> keyList) {
        if (HsmCollections.isEmpty(keyList)) {
            return new ContentValues();
        }
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ValueContentProvider.KEY_VALUE_PAIRS, keyList);
        Bundle res = ctx.getContentResolver().call(AUTHORITY_URI, ValueContentProvider.METHOD_GET_VALUE_BULK, null, bundle);
        if (res == null) {
            return new ContentValues();
        }
        ContentValues values = (ContentValues) res.getParcelable(ValueContentProvider.KEY_CONTENT_VALUES);
        if (values == null) {
            return new ContentValues();
        }
        return values;
    }

    public static final boolean putValueBulk(Context ctx, ArrayList<ValuePair> valuePairs) {
        if (HsmCollections.isEmpty(valuePairs)) {
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ValueContentProvider.KEY_VALUE_PAIRS, valuePairs);
        Bundle res = ctx.getContentResolver().call(AUTHORITY_URI, ValueContentProvider.METHOD_PUT_VALUE_BULK, null, bundle);
        if (res != null) {
            return res.getBoolean("key_result", false);
        }
        HwLog.e(TAG, "putValueBulk res bundle is null!");
        return false;
    }

    public static final int getValueInt(Context ctx, String key, int defaultValue) {
        String str = getValueString(ctx, key, "");
        if (TextUtils.isEmpty(str)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static final boolean getValueBoolean(Context ctx, String key, boolean defaultValue) {
        String str = getValueString(ctx, key, "");
        if (TextUtils.isEmpty(str)) {
            return defaultValue;
        }
        try {
            return Boolean.parseBoolean(str);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static final boolean putValueBoolean(Context ctx, String key, boolean value) {
        return putValueString(ctx, key, String.valueOf(value));
    }

    public static final boolean putValueInt(Context ctx, String key, int value) {
        return putValueString(ctx, key, String.valueOf(value));
    }
}
