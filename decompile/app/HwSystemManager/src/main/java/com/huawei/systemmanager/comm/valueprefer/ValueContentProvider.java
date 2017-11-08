package com.huawei.systemmanager.comm.valueprefer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Map;

public class ValueContentProvider extends ContentProvider {
    public static final String AUTH = "com.huawei.systemmanager.valueprovider";
    private static final int DB_VERSION = 1;
    public static final String KEY_CONTENT_VALUES = "key_contentvalues";
    public static final String KEY_RESULT = "key_result";
    public static final String KEY_VALUE = "key_value";
    public static final String KEY_VALUE_PAIRS = "key_value_pairs";
    public static final String METHOD_BACKUP = "method_backup";
    public static final String METHOD_CHECK = "method_check";
    public static final String METHOD_GET = "method_getvalue";
    public static final String METHOD_GET_VALUE_BULK = "method_getbulkvalue";
    public static final String METHOD_PUT = "method_putvalue";
    public static final String METHOD_PUT_VALUE_BULK = "method_putbulkvalue";
    private static final String PREFRENCE_NAME = "hsm_contentvalue";
    private static final String TAG = "ValueContentProvider";
    public static final Uri URI_AUTHORITY = Uri.parse("content://com.huawei.systemmanager.valueprovider");
    private Map<String, MethodHanlde> handleMap = HsmCollections.newHashMap();
    private SharedPreferences mPrefer;
    private MethodHanlde methodCheckValue = new MethodHanlde() {
        public Bundle call(String arg, Bundle extras) {
            String key = arg;
            return ValueContentProvider.buildRes(ValueContentProvider.this.getPreference().contains(arg));
        }
    };
    private MethodHanlde methodGet = new MethodHanlde() {
        public Bundle call(String arg, Bundle extras) {
            String key = arg;
            if (TextUtils.isEmpty(arg)) {
                return null;
            }
            String value = ValueContentProvider.this.getPreference().getString(arg, "");
            Bundle res = new Bundle();
            res.putString(arg, value);
            return res;
        }
    };
    private MethodHanlde methodGetBulkValue = new MethodHanlde() {
        public Bundle call(String arg, Bundle extras) {
            if (extras == null) {
                HwLog.e(ValueContentProvider.TAG, "methodGetBulkValue, extra is null!");
                return null;
            }
            extras.setClassLoader(ValuePair.class.getClassLoader());
            ArrayList<ValuePair> keyList = extras.getParcelableArrayList(ValueContentProvider.KEY_VALUE_PAIRS);
            if (HsmCollections.isEmpty(keyList)) {
                HwLog.e(ValueContentProvider.TAG, "methodGetBulkValue, keylist is empty");
                return null;
            }
            ContentValues values = new ContentValues();
            for (ValuePair pair : keyList) {
                String key = pair.getKey();
                values.put(key, ValueContentProvider.this.getPreference().getString(key, pair.getValue()));
            }
            Bundle res = new Bundle();
            res.putParcelable(ValueContentProvider.KEY_CONTENT_VALUES, values);
            return res;
        }
    };
    private MethodHanlde methodPut = new MethodHanlde() {
        public Bundle call(String arg, Bundle extras) {
            if (extras == null) {
                HwLog.e(ValueContentProvider.TAG, "methodPut, extra is null!");
                return ValueContentProvider.buildRes(false);
            }
            String key = arg;
            String putValue = extras.getString(arg);
            if (TextUtils.isEmpty(putValue)) {
                return ValueContentProvider.buildRes(false);
            }
            boolean success = ValueContentProvider.this.getPreference().edit().putString(arg, putValue).commit();
            ValueContentProvider.this.notifyChanged(arg);
            return ValueContentProvider.buildRes(success);
        }
    };
    private MethodHanlde methodPutBulkValue = new MethodHanlde() {
        public Bundle call(String arg, Bundle extras) {
            if (extras == null) {
                HwLog.e(ValueContentProvider.TAG, "methodPutBulkValue, extra is null!");
                return null;
            }
            ArrayList<ValuePair> pairs = extras.getParcelableArrayList(ValueContentProvider.KEY_VALUE_PAIRS);
            if (HsmCollections.isEmpty(pairs)) {
                HwLog.e(ValueContentProvider.TAG, "methodPutBulkValue, pairs is empty");
                return ValueContentProvider.buildRes(false);
            }
            Editor editor = ValueContentProvider.this.getPreference().edit();
            for (ValuePair pair : pairs) {
                editor.putString(pair.getKey(), pair.getValue());
            }
            editor.commit();
            for (ValuePair pair2 : pairs) {
                ValueContentProvider.this.notifyChanged(pair2.getKey());
            }
            return ValueContentProvider.buildRes(true);
        }
    };

    interface MethodHanlde {
        Bundle call(String str, Bundle bundle);
    }

    public boolean onCreate() {
        this.handleMap.put(METHOD_GET, this.methodGet);
        this.handleMap.put(METHOD_GET_VALUE_BULK, this.methodGetBulkValue);
        this.handleMap.put(METHOD_PUT, this.methodPut);
        this.handleMap.put(METHOD_PUT_VALUE_BULK, this.methodPutBulkValue);
        this.handleMap.put(METHOD_CHECK, this.methodCheckValue);
        getPreference();
        return true;
    }

    @Nullable
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public Bundle call(String method, String arg, Bundle extras) {
        MethodHanlde hanlde = (MethodHanlde) this.handleMap.get(method);
        if (hanlde != null) {
            return hanlde.call(arg, extras);
        }
        HwLog.e(TAG, "cannnot find method:" + method);
        return super.call(method, arg, extras);
    }

    private SharedPreferences getPreference() {
        if (this.mPrefer != null) {
            return this.mPrefer;
        }
        this.mPrefer = getContext().getSharedPreferences(PREFRENCE_NAME, 0);
        return this.mPrefer;
    }

    private static final Bundle buildRes(boolean success) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("key_result", success);
        return bundle;
    }

    private void notifyChanged(String key) {
        getContext().getContentResolver().notifyChange(Uri.withAppendedPath(URI_AUTHORITY, key), null);
    }
}
