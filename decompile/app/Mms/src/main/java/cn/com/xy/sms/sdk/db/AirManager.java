package cn.com.xy.sms.sdk.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.io.LineNumberReader;

/* compiled from: Unknown */
public class AirManager {
    public static final String AIR_NUM = "air_num";
    public static final String COMPANY = "company";
    public static final String CREATE_TABLE = "create table  if not exists tb_air (id INTEGER PRIMARY KEY,air_num TEXT not null unique,start_city TEXT,end_city TEXT,start_place TEXT,end_place TEXT,start_time TEXT,end_time TEXT,company TEXT)";
    public static final String DROP_TABLE = " DROP TABLE IF EXISTS tb_air";
    public static final String END_CITY = "end_city";
    public static final String END_PALCE = "end_place";
    public static final String END_TIME = "end_time";
    public static final String ID = "id";
    public static final String START_CITY = "start_city";
    public static final String START_PLACE = "start_place";
    public static final String START_TIME = "start_time";
    public static final String TABLE_NAME = "tb_air";

    public static boolean checkUpdateData() {
        new a().start();
        return true;
    }

    public static void importAirData(Context context) {
        LineNumberReader lineByCompressFile;
        SQLiteDatabase sQLiteDatabase;
        SQLiteDatabase sQLiteDatabase2;
        Throwable th;
        LineNumberReader lineNumberReader;
        String str = Constant.getDRAWBLE_PATH() + "air_data.txt";
        try {
            lineByCompressFile = XyUtil.getLineByCompressFile(str);
            try {
                sQLiteDatabase = DBManager.getSQLiteDatabase();
            } catch (Throwable th2) {
                sQLiteDatabase2 = null;
                LineNumberReader lineNumberReader2 = lineByCompressFile;
                th = th2;
                lineNumberReader = lineNumberReader2;
                DBManager.closeDB(str, true, lineNumberReader, null, sQLiteDatabase2);
                throw th;
            }
            try {
                sQLiteDatabase.beginTransaction();
                String readLine = lineByCompressFile.readLine();
                if (readLine != null) {
                    String[] split = readLine.split("=");
                    while (true) {
                        readLine = lineByCompressFile.readLine();
                        if (readLine != null) {
                            String[] split2 = readLine.split(" ");
                            if (split2.length >= 3) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(AIR_NUM, split2[0]);
                                contentValues.put(START_PLACE, split[Integer.valueOf(split2[1]).intValue()]);
                                contentValues.put(END_PALCE, split[Integer.valueOf(split2[2]).intValue()]);
                                if (!(((long) sQLiteDatabase.update(TABLE_NAME, contentValues, "air_num=?", new String[]{split2[0]})) >= 1)) {
                                    sQLiteDatabase.insert(TABLE_NAME, null, contentValues);
                                }
                            }
                        } else {
                            DBManager.closeDB(str, true, lineByCompressFile, null, sQLiteDatabase);
                            return;
                        }
                    }
                }
                DBManager.closeDB(str, true, lineByCompressFile, null, sQLiteDatabase);
            } catch (Throwable th3) {
                Throwable th4 = th3;
                sQLiteDatabase2 = sQLiteDatabase;
                lineNumberReader = lineByCompressFile;
                th = th4;
                DBManager.closeDB(str, true, lineNumberReader, null, sQLiteDatabase2);
                throw th;
            }
        } catch (Throwable th5) {
            th = th5;
            lineNumberReader = null;
            sQLiteDatabase2 = null;
            DBManager.closeDB(str, true, lineNumberReader, null, sQLiteDatabase2);
            throw th;
        }
    }

    public static String[] queryStartEndPlace(String str) {
        XyCursor query;
        Throwable th;
        XyCursor xyCursor = null;
        try {
            query = DBManager.query(TABLE_NAME, new String[]{START_PLACE, END_PALCE}, "air_num = ?", new String[]{new StringBuilder(String.valueOf(str)).toString()});
            if (query != null) {
                try {
                    if (query.moveToNext()) {
                        int columnIndex = query.getColumnIndex(START_PLACE);
                        int columnIndex2 = query.getColumnIndex(END_PALCE);
                        String string = query.getString(columnIndex);
                        String string2 = query.getString(columnIndex2);
                        String[] strArr = new String[]{string, string2};
                        XyCursor.closeCursor(query, true);
                        return strArr;
                    }
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    xyCursor = query;
                    th = th3;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(query, true);
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
        return null;
    }
}
