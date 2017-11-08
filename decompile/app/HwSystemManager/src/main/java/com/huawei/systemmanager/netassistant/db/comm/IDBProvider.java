package com.huawei.systemmanager.netassistant.db.comm;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.ArrayMap;
import com.huawei.systemmanager.backup.HsmContentProvider;
import com.huawei.systemmanager.netassistant.traffic.backgroundtraffic.BackgroundTrafficInfo;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@TargetApi(19)
public abstract class IDBProvider extends HsmContentProvider {
    private static final String DISTINCT_PARAMETE = "distinct";
    private static final String LIMIT_PARAMETER = "limit";
    private static final int NUMBER_ZERO = 0;
    private static final String TAG = IDBProvider.class.getSimpleName();
    private String mAuthority;
    protected IDataBaseHelper mHelper;
    private Object mLock = new Object();
    ArrayMap<AtomicInteger, DBTable> mTables;
    private UriMatcher mUriMatcher;

    public abstract String getAuthority();

    public abstract IDataBaseHelper initDatabase();

    public boolean onCreate() {
        this.mHelper = initDatabase();
        this.mTables = initTableMap();
        this.mAuthority = getAuthority();
        createUri();
        return true;
    }

    private void createUri() {
        this.mUriMatcher = new UriMatcher(-1);
        for (AtomicInteger key : this.mTables.keySet()) {
            this.mUriMatcher.addURI(this.mAuthority, ((DBTable) this.mTables.get(key)).getTableName(), key.get());
        }
    }

    private ArrayMap<AtomicInteger, DBTable> initTableMap() {
        HwLog.d(TAG, "init table map");
        ArrayMap<AtomicInteger, DBTable> tableMap = new ArrayMap();
        DBTable[] tables = this.mHelper.getTables();
        int N = tables.length;
        for (int i = 0; i < N; i++) {
            tableMap.put(new AtomicInteger(i), tables[i]);
        }
        return tableMap;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        if (BackgroundTrafficInfo.BACKGROUND_TRAFFIC_URI.equals(uri)) {
            BackgroundTrafficInfo.setBackgroundTrafficPreference(values);
            return uri;
        }
        if (values == null) {
            values = new ContentValues();
        }
        int match = this.mUriMatcher.match(uri);
        for (AtomicInteger code : this.mTables.keySet()) {
            if (match == code.get()) {
                String table = ((DBTable) this.mTables.get(code)).getTableName();
                if (table == null) {
                    HwLog.e(TAG, "/delete: unknow Uri");
                } else {
                    this.mHelper.insert(table, ((DBTable) this.mTables.get(code)).getPrimaryColumn(), values);
                }
            }
        }
        return uri;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        int match = this.mUriMatcher.match(uri);
        for (AtomicInteger code : this.mTables.keySet()) {
            if (match == code.get()) {
                String table = ((DBTable) this.mTables.get(code)).getTableName();
                if (table == null) {
                    HwLog.e(TAG, "/delete: unknow Uri");
                } else {
                    count = this.mHelper.delete(table, selection, selectionArgs);
                }
            }
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        String table = getTable(uri);
        if (values == null) {
            values = new ContentValues();
        }
        if (table == null) {
            HwLog.e(TAG, "/update: unknow Uri");
        } else {
            count = this.mHelper.update(table, values, selection, selectionArgs);
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (BackgroundTrafficInfo.BACKGROUND_TRAFFIC_URI.equals(uri)) {
            return BackgroundTrafficInfo.getBackgroundTrafficCursor();
        }
        String limit = uri.getQueryParameter(LIMIT_PARAMETER);
        String table = getTable(uri);
        String whereClause = selection;
        boolean distinct = false;
        if (TextUtils.isEmpty(uri.getQueryParameter(DISTINCT_PARAMETE))) {
            distinct = true;
        }
        Cursor cursor = null;
        if (table != null) {
            cursor = this.mHelper.query(Boolean.valueOf(distinct), table, projection, selection, selectionArgs, null, null, sortOrder, limit);
            if (cursor != null) {
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
            }
        }
        return cursor;
    }

    private String getTable(Uri uri) {
        String table = null;
        int match = this.mUriMatcher.match(uri);
        for (AtomicInteger code : this.mTables.keySet()) {
            if (match == code.get()) {
                table = ((DBTable) this.mTables.get(code)).getTableName();
                if (table != null) {
                    return table;
                }
                HwLog.e(TAG, "/delete: unknow Uri");
            }
        }
        return table;
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (BackgroundTrafficInfo.BACKGROUND_TRAFFIC_URI.equals(uri)) {
            return BackgroundTrafficInfo.setBackgroundTrafficPreference(values);
        }
        SQLiteDatabase db = null;
        synchronized (this.mLock) {
            try {
                db = this.mHelper.openDatabase();
            } catch (Exception e) {
                HwLog.d(TAG, "bulkInsert exception lock");
            }
        }
        if (db == null) {
            return -1;
        }
        List<DBSqlBean> dbSqlBeans = InsertSqlUtils.createInsertSql(getTable(uri), values);
        db.beginTransaction();
        for (DBSqlBean bean : dbSqlBeans) {
            Object[] objArr = null;
            if (bean.getBindArgs() != null) {
                List<Object> mBindArgList = bean.getBindArgs();
                if (!mBindArgList.isEmpty()) {
                    objArr = new Object[mBindArgList.size()];
                    mBindArgList.toArray(objArr);
                }
            }
            if (!TextUtils.isEmpty(bean.getSql())) {
                if (objArr == null || objArr.length <= 0) {
                    db.execSQL(bean.getSql());
                } else {
                    db.execSQL(bean.getSql(), objArr);
                }
            }
        }
        db.setTransactionSuccessful();
        int size = dbSqlBeans.size();
        db.endTransaction();
        return size;
    }
}
