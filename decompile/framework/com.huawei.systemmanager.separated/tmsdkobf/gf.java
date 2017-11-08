package tmsdkobf;

import android.database.sqlite.SQLiteDatabase;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class gf {
    private static Object lock = new Object();

    public static void a(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("create table if not exists dcp_info(info1 text,info2 blob,info4 blob,info3 blob)");
        sQLiteDatabase.execSQL("create table if not exists dcr_info(info1 text,info2 blob)");
        sQLiteDatabase.execSQL("create index if not exists dcp_index on dcp_info(info1)");
        sQLiteDatabase.execSQL("create index if not exists dcr_index on dcr_info(info1)");
        sQLiteDatabase.execSQL("create table if not exists up(info1 text primary key,info2 integer)");
    }

    public static void a(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        d.d("DeepCleanCloudDB", "upgradeDB  " + i + " >> " + i2 + ">> " + 18);
        if (i < 18) {
            try {
                sQLiteDatabase.execSQL("ALTER TABLE dcp_info ADD COLUMN info4 blob");
                d.d("DeepCleanCloudDB", "add  column sucess");
            } catch (Exception e) {
                d.d("DeepCleanCloudDB", "add  column::" + e.toString());
            }
            kz.dx().dy();
        }
        a(sQLiteDatabase);
    }

    public static void b(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("drop table if exists dcd_info");
        sQLiteDatabase.execSQL("create table if not exists dcd_info(info1 text,info2 blob)");
        sQLiteDatabase.execSQL("drop table if exists dcp_info");
        sQLiteDatabase.execSQL("create table if not exists dcp_info(info1 text,info2 blob,info4 blob,info3 blob)");
        sQLiteDatabase.execSQL("create index if not exists dcd_index on dcd_info(info1)");
        sQLiteDatabase.execSQL("create index if not exists dcp_index on dcp_info(info1)");
    }

    public static void b(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        d.d("DeepCleanCloudDB", "downgradeDB");
        b(sQLiteDatabase);
        a(sQLiteDatabase);
    }
}
