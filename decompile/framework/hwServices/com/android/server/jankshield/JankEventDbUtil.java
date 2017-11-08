package com.android.server.jankshield;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.JankBdData;
import android.os.JankEventData;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.io.File;

public class JankEventDbUtil {
    private static final String DATABASE_NAME = "JankEventDb.db";
    private static final int DATABASE_VERSION = 4;
    protected static final boolean HWDBG;
    protected static final boolean HWFLOW;
    protected static final boolean HWLOGW_E = true;
    private static final String TAG = "JankShield";
    private String EXTERN_PATH = null;
    protected Context mContext = null;
    private SQLiteDatabase mDb = null;
    private JankSQLHelper mHelper;

    private static class JankSQLHelper extends SQLiteOpenHelper {
        public JankSQLHelper(Context context, String jankDatabaseName) {
            super(context, jankDatabaseName, null, 4);
        }

        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(TableJankEvent.getCreateSql());
                db.execSQL(TableJankBd.getCreateSql());
            } catch (Exception e) {
                Log.w(JankEventDbUtil.TAG, "onCreate(db) Exception" + e.toString());
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                TableJankEvent.upgrade(db, oldVersion, newVersion);
                TableJankBd.upgrade(db, oldVersion, newVersion);
            } catch (Exception e) {
                Log.w(JankEventDbUtil.TAG, "onUpgrade(db) Exception" + e.toString());
                reCreate(db);
            }
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                TableJankEvent.downgrade(db, oldVersion, newVersion);
                TableJankBd.downgrade(db, oldVersion, newVersion);
            } catch (Exception e) {
                Log.w(JankEventDbUtil.TAG, "onDowngrade(db) Exception" + e.toString());
                reCreate(db);
            }
        }

        private void reCreate(SQLiteDatabase db) {
            TableJankEvent.dropTb(db);
            TableJankBd.dropTb(db);
            onCreate(db);
        }
    }

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
        if (!Log.HWLog) {
            z = Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false;
        }
        HWDBG = z;
    }

    public JankEventDbUtil(Context context) {
        if (getChipType().equals("QUALCOMM")) {
            this.EXTERN_PATH = "/data/log/jank/";
        } else {
            this.EXTERN_PATH = "/splash2/jank/";
        }
        this.mContext = context;
        if (openDb()) {
            TableJankEvent.getCount(this.mDb);
            TableJankBd.getCount(this.mDb);
            return;
        }
        Log.w(TAG, "file path not exits and can`t create so db can`t use");
    }

    public void insertEvent(JankEventData jankevent) {
        if (this.mDb != null) {
            TableJankEvent.insert(this.mDb, jankevent);
        }
    }

    public void insertBd(JankBdData jankbd) {
        if (this.mDb != null) {
            TableJankBd.insert(this.mDb, jankbd);
        }
    }

    public boolean openDb() {
        if (mkdir(this.EXTERN_PATH)) {
            this.mHelper = new JankSQLHelper(this.mContext, this.EXTERN_PATH + DATABASE_NAME);
            try {
                this.mDb = this.mHelper.getWritableDatabase();
                if (this.mDb == null || !this.mDb.isOpen()) {
                    this.mHelper = null;
                    return false;
                }
                this.mDb.execSQL(TableJankEvent.getCreateSql());
                this.mDb.execSQL(TableJankBd.getCreateSql());
                return true;
            } catch (Exception e) {
                Log.w(TAG, "db.open()  Exception " + e.toString());
                return false;
            }
        }
        Log.w(TAG, "file path  does not exit, failed to create bd file.");
        this.mHelper = null;
        return false;
    }

    public void closeJankDb() {
        try {
            this.mDb.close();
        } catch (Exception e) {
            Log.w(TAG, "db.close() Exception " + e.toString());
        }
        if (HWFLOW) {
            Log.i(TAG, "db has been closed ");
        }
    }

    private boolean mkdir(String path) {
        boolean mkRet = true;
        File dir = new File(path);
        if (!dir.exists()) {
            mkRet = dir.mkdirs();
            if (HWDBG) {
                Log.d(TAG, "make path " + path);
            }
        }
        return mkRet;
    }

    private String getChipType() {
        String chipType = SystemProperties.get("ro.board.platform", AppHibernateCst.INVALID_PKG);
        if (chipType.startsWith("msm") || chipType.startsWith("qsc") || chipType.startsWith("MSM") || chipType.startsWith("QSC")) {
            return "QUALCOMM";
        }
        return "HISI";
    }
}
