package cn.com.xy.sms.sdk.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.db.entity.PhoneSmsParseManager;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.dex.DexUtil;

/* compiled from: Unknown */
final class h extends SQLiteOpenHelper {
    public h(Context context, String str, CursorFactory cursorFactory, int i) {
        super(context, str, null, 48);
    }

    public final void onCreate(SQLiteDatabase sQLiteDatabase) {
        try {
            DBManager.createDb(sQLiteDatabase);
        } catch (Throwable th) {
        }
    }

    public final void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        try {
            sQLiteDatabase.execSQL(SysParamEntityManager.DROP_TABLE);
            sQLiteDatabase.execSQL(IccidInfoManager.DROP_TABLE);
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_public_info");
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_public_menu_info");
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_public_num_info");
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_centernum_location_info");
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_scene_config");
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_res_download");
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_scenerule_config");
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_jar_list");
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_count_scene");
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_popup_action_scene");
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_menu_action");
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_button_action_scene");
            sQLiteDatabase.execSQL(TrainManager.DROP_TABLE);
            sQLiteDatabase.execSQL(AirManager.DROP_TABLE);
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_menu_list");
            sQLiteDatabase.execSQL(MatchCacheManager.DROP_TABLE);
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_update_task");
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_xml_res_download");
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_resourse_queue");
            sQLiteDatabase.execSQL(PhoneSmsParseManager.DROP_TABLE);
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_netquery_time");
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_num_name");
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS tb_sms_parse_recorder");
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_shard_data");
            sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS tb_phonenum_menu");
        } catch (Throwable th) {
            DexUtil.saveExceptionLog(th);
        }
        DBManager.createDb(sQLiteDatabase);
    }

    public final void onOpen(SQLiteDatabase sQLiteDatabase) {
        try {
            super.onOpen(sQLiteDatabase);
        } catch (Throwable th) {
        }
    }

    public final void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        DBManager.createDb(sQLiteDatabase);
    }
}
