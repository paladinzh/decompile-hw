package com.android.deskclock.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.R;
import com.android.deskclock.alarmclock.AlarmSetDialogManager;
import com.android.deskclock.worldclock.City.LocationColumns;
import com.android.deskclock.worldclock.TimeZoneUtils;
import com.android.util.HwLog;
import com.android.util.Log;
import com.android.util.Utils;
import java.io.Closeable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class AlarmDatabaseHelper extends SQLiteOpenHelper {
    private String[] mCities;
    private String[] mCountryname;
    private Context mCtx;
    private String[] mIds;
    private Map<String, Integer> mIndexMap;
    private String[] mTimezones;

    private void upgradeLowVersionMapToNew(android.content.Context r28, android.database.sqlite.SQLiteDatabase r29) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0117 in list [B:26:0x0114]
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r27 = this;
        r4 = r28.getResources();
        r5 = 2131230728; // 0x7f080008 float:1.8077517E38 double:1.052967886E-314;
        r14 = r4.getStringArray(r5);
        r4 = r28.getResources();
        r5 = 2131230729; // 0x7f080009 float:1.807752E38 double:1.0529678866E-314;
        r15 = r4.getStringArray(r5);
        r13 = 0;
        r5 = "locations";	 Catch:{ all -> 0x010b }
        r4 = 3;	 Catch:{ all -> 0x010b }
        r6 = new java.lang.String[r4];	 Catch:{ all -> 0x010b }
        r4 = "_id";	 Catch:{ all -> 0x010b }
        r7 = 0;	 Catch:{ all -> 0x010b }
        r6[r7] = r4;	 Catch:{ all -> 0x010b }
        r4 = "city_index";	 Catch:{ all -> 0x010b }
        r7 = 1;	 Catch:{ all -> 0x010b }
        r6[r7] = r4;	 Catch:{ all -> 0x010b }
        r4 = "homecity";	 Catch:{ all -> 0x010b }
        r7 = 2;	 Catch:{ all -> 0x010b }
        r6[r7] = r4;	 Catch:{ all -> 0x010b }
        r7 = 0;	 Catch:{ all -> 0x010b }
        r8 = 0;	 Catch:{ all -> 0x010b }
        r9 = 0;	 Catch:{ all -> 0x010b }
        r10 = 0;	 Catch:{ all -> 0x010b }
        r11 = 0;	 Catch:{ all -> 0x010b }
        r4 = r29;	 Catch:{ all -> 0x010b }
        r13 = r4.query(r5, r6, r7, r8, r9, r10, r11);	 Catch:{ all -> 0x010b }
        if (r13 == 0) goto L_0x0112;	 Catch:{ all -> 0x010b }
    L_0x003c:
        r0 = r27;	 Catch:{ all -> 0x010b }
        r4 = r0.mCtx;	 Catch:{ all -> 0x010b }
        r5 = "timeZone.cfg";	 Catch:{ all -> 0x010b }
        r6 = 0;	 Catch:{ all -> 0x010b }
        r23 = com.android.util.Utils.getSharedPreferences(r4, r5, r6);	 Catch:{ all -> 0x010b }
        r18 = r23.edit();	 Catch:{ all -> 0x010b }
    L_0x004c:
        r4 = r13.moveToNext();	 Catch:{ all -> 0x010b }
        if (r4 == 0) goto L_0x0112;	 Catch:{ all -> 0x010b }
    L_0x0052:
        r4 = "city_index";	 Catch:{ all -> 0x010b }
        r4 = r13.getColumnIndex(r4);	 Catch:{ all -> 0x010b }
        r16 = r13.getString(r4);	 Catch:{ all -> 0x010b }
        r4 = "_id";	 Catch:{ all -> 0x010b }
        r4 = r13.getColumnIndex(r4);	 Catch:{ all -> 0x010b }
        r12 = r13.getInt(r4);	 Catch:{ all -> 0x010b }
        r4 = "homecity";	 Catch:{ all -> 0x010b }
        r4 = r13.getColumnIndex(r4);	 Catch:{ all -> 0x010b }
        r20 = r13.getInt(r4);	 Catch:{ all -> 0x010b }
        r0 = r27;	 Catch:{ all -> 0x010b }
        r1 = r28;	 Catch:{ all -> 0x010b }
        r2 = r16;	 Catch:{ all -> 0x010b }
        r24 = r0.querySettingDatabase(r1, r2);	 Catch:{ all -> 0x010b }
        if (r24 != 0) goto L_0x004c;	 Catch:{ all -> 0x010b }
    L_0x007f:
        r21 = 0;	 Catch:{ all -> 0x010b }
    L_0x0081:
        r4 = r14.length;	 Catch:{ all -> 0x010b }
        r0 = r21;	 Catch:{ all -> 0x010b }
        if (r0 >= r4) goto L_0x004c;	 Catch:{ all -> 0x010b }
    L_0x0086:
        r4 = r14[r21];	 Catch:{ all -> 0x010b }
        r0 = r16;	 Catch:{ all -> 0x010b }
        r4 = r0.equals(r4);	 Catch:{ all -> 0x010b }
        if (r4 == 0) goto L_0x009b;	 Catch:{ all -> 0x010b }
    L_0x0090:
        r4 = "";	 Catch:{ all -> 0x010b }
        r5 = r15[r21];	 Catch:{ all -> 0x010b }
        r4 = r4.equals(r5);	 Catch:{ all -> 0x010b }
        if (r4 == 0) goto L_0x009e;	 Catch:{ all -> 0x010b }
    L_0x009b:
        r21 = r21 + 1;	 Catch:{ all -> 0x010b }
        goto L_0x0081;	 Catch:{ all -> 0x010b }
    L_0x009e:
        r4 = "";	 Catch:{ all -> 0x010b }
        r0 = r23;	 Catch:{ all -> 0x010b }
        r1 = r16;	 Catch:{ all -> 0x010b }
        r22 = r0.getString(r1, r4);	 Catch:{ all -> 0x010b }
        r0 = r18;	 Catch:{ all -> 0x010b }
        r1 = r16;	 Catch:{ all -> 0x010b }
        r0.remove(r1);	 Catch:{ all -> 0x010b }
        r16 = r15[r21];	 Catch:{ all -> 0x010b }
        r17 = new android.content.ContentValues;	 Catch:{ all -> 0x010b }
        r17.<init>();	 Catch:{ all -> 0x010b }
        r4 = "city_index";	 Catch:{ all -> 0x010b }
        r0 = r17;	 Catch:{ all -> 0x010b }
        r1 = r16;	 Catch:{ all -> 0x010b }
        r0.put(r4, r1);	 Catch:{ all -> 0x010b }
        r26 = "_id=?";	 Catch:{ all -> 0x010b }
        r4 = 1;	 Catch:{ all -> 0x010b }
        r0 = new java.lang.String[r4];	 Catch:{ all -> 0x010b }
        r25 = r0;	 Catch:{ all -> 0x010b }
        r4 = java.lang.String.valueOf(r12);	 Catch:{ all -> 0x010b }
        r5 = 0;	 Catch:{ all -> 0x010b }
        r25[r5] = r4;	 Catch:{ all -> 0x010b }
        r4 = "locations";	 Catch:{ all -> 0x010b }
        r0 = r29;	 Catch:{ all -> 0x010b }
        r1 = r17;	 Catch:{ all -> 0x010b }
        r2 = r26;	 Catch:{ all -> 0x010b }
        r3 = r25;	 Catch:{ all -> 0x010b }
        r0.update(r4, r1, r2, r3);	 Catch:{ all -> 0x010b }
        r0 = r18;	 Catch:{ all -> 0x010b }
        r1 = r16;	 Catch:{ all -> 0x010b }
        r2 = r22;	 Catch:{ all -> 0x010b }
        r0.putString(r1, r2);	 Catch:{ all -> 0x010b }
        r18.commit();	 Catch:{ all -> 0x010b }
        r4 = 1;	 Catch:{ all -> 0x010b }
        r0 = r20;	 Catch:{ all -> 0x010b }
        if (r4 != r0) goto L_0x009b;	 Catch:{ all -> 0x010b }
    L_0x00ef:
        r4 = "setting_activity";	 Catch:{ all -> 0x010b }
        r5 = 0;	 Catch:{ all -> 0x010b }
        r0 = r28;	 Catch:{ all -> 0x010b }
        r4 = com.android.util.Utils.getSharedPreferences(r0, r4, r5);	 Catch:{ all -> 0x010b }
        r19 = r4.edit();	 Catch:{ all -> 0x010b }
        r4 = "home_time_index";	 Catch:{ all -> 0x010b }
        r0 = r19;	 Catch:{ all -> 0x010b }
        r1 = r16;	 Catch:{ all -> 0x010b }
        r0.putString(r4, r1);	 Catch:{ all -> 0x010b }
        r19.commit();	 Catch:{ all -> 0x010b }
        goto L_0x009b;
    L_0x010b:
        r4 = move-exception;
        if (r13 == 0) goto L_0x0111;
    L_0x010e:
        r13.close();
    L_0x0111:
        throw r4;
    L_0x0112:
        if (r13 == 0) goto L_0x0117;
    L_0x0114:
        r13.close();
    L_0x0117:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.deskclock.provider.AlarmDatabaseHelper.upgradeLowVersionMapToNew(android.content.Context, android.database.sqlite.SQLiteDatabase):void");
    }

    public AlarmDatabaseHelper(Context context) {
        super(context, "alarms.db", null, 15);
        this.mCtx = context;
    }

    public void onCreate(SQLiteDatabase db) {
        Log.d("AlarmDatabaseHelper", "onCreate");
        db.execSQL("CREATE TABLE alarms (_id INTEGER PRIMARY KEY,hour INTEGER, minutes INTEGER, daysofweek INTEGER, alarmtime INTEGER, enabled INTEGER, vibrate INTEGER, volume INTEGER, message TEXT, alert TEXT, daysofweektype INTEGER, daysofweekshow TEXT);");
        String insertMe = "INSERT INTO alarms (hour, minutes, daysofweek, alarmtime, enabled, vibrate, volume, message, alert, daysofweektype, daysofweekshow) VALUES ";
        db.execSQL(insertMe + "(7, 30, 31, 0, 0, 1, 3, '', '', 1, " + "'" + AlarmSetDialogManager.getRepeatType(DeskClockApplication.getDeskClockApplication(), 1) + "');");
        db.execSQL(insertMe + "(9, 00, 96, 0, 0, 1, 3, '', '', 3, '" + AlarmSetDialogManager.getRepeatType(DeskClockApplication.getDeskClockApplication(), 3) + "');");
        db.execSQL("CREATE TABLE locations (_id INTEGER PRIMARY KEY AUTOINCREMENT,sort_order INTEGER,city_index TEXT,timezone TEXT,homecity INTEGER);");
        db.execSQL("CREATE TABLE widgets (_id INTEGER PRIMARY KEY AUTOINCREMENT,cityname VARCHAR(128),timezone VARCHAR(128),widget_id INTEGER,first_timezone VARCHAR(128),second_timezone VARCHAR(128),first_index VARCHAR(128),second_index VARCHAR(128));");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        Log.i("AlarmDatabaseHelper", "onUpgrade : Upgrading alarms database from version " + oldVersion + " to " + currentVersion + ", which will destroy all old data");
        if (oldVersion < 8) {
            db.execSQL("DROP TABLE IF EXISTS alarms");
            db.execSQL("DROP TABLE IF EXISTS locations");
            db.execSQL("DROP TABLE IF EXISTS widgets");
            onCreate(db);
            return;
        }
        Utils.setVersionUpdate(true);
        if (oldVersion == 8 || oldVersion == 9) {
            upgradeForAlarm(db);
            upgradeForLocation(db);
            oldVersion = 10;
        }
        if (oldVersion == 10) {
            upgradeForNoNemu(db);
            oldVersion = 11;
        }
        if (oldVersion == 11) {
            updateV11TOV12(db);
            oldVersion = 12;
        }
        if (oldVersion == 12) {
            buildLowVersionMap(this.mCtx);
            upgradeWorldSettingPref(this.mCtx);
            upgradeTableLocation(db);
            upgradeTableWorldClockWidget(db);
            clearUpdateData();
            oldVersion = 13;
        }
        if (oldVersion == 13) {
            deletebtnInSQL(db);
            oldVersion = 14;
        }
        if (oldVersion == 14) {
            upgradeLowVersionMapToNew(this.mCtx, db);
            TimeZoneUtils.queryRemoteTimeZone(this.mCtx, TimeZoneUtils.getExitCities(this.mCtx));
        }
        Utils.setVersionUpdate(false);
    }

    public void buildLowVersionMap(Context context) {
        Resources resources = context.getResources();
        this.mCities = resources.getStringArray(R.array.cities_names_key);
        this.mCountryname = resources.getStringArray(R.array.countries_names_key);
        this.mTimezones = resources.getStringArray(R.array.cities_tz);
        this.mIds = resources.getStringArray(R.array.cities_id);
        this.mIndexMap = new HashMap();
        for (int i = 0; i < this.mIds.length; i++) {
            this.mIndexMap.put(this.mIds[i], Integer.valueOf(i));
        }
    }

    public String querySettingDatabase(Context context, String key) {
        String value = null;
        String[] TIMEZONE_COLUMNS = new String[]{"name"};
        String[] sqlArgs = new String[]{key};
        Cursor query = context.getContentResolver().query(Uri.parse("content://com.android.settings.timezonesprovider/timezones"), TIMEZONE_COLUMNS, "unique_id=?", sqlArgs, null);
        if (query != null && query.moveToNext()) {
            value = query.getString(query.getColumnIndex(TIMEZONE_COLUMNS[0]));
        }
        if (query != null) {
            query.close();
        }
        return value;
    }

    private void upgradeWorldSettingPref(Context context) {
        SharedPreferences pref = Utils.getSharedPreferences(context, "setting_activity", 0);
        String id = pref.getString("home_time_index", "");
        if (!TextUtils.isEmpty(id)) {
            try {
                int index = ((Integer) this.mIndexMap.get(id)).intValue();
                if (index != -1) {
                    String realIndex = this.mTimezones[index] + "_" + this.mCities[index] + "_" + this.mCountryname[index];
                    Editor editor = pref.edit();
                    editor.putString("home_time_index", realIndex);
                    editor.commit();
                    editor = Utils.getSharedPreferences(context, "timeZone.cfg", 0).edit();
                    editor.putString(realIndex, this.mCities[index] + "_" + index);
                    editor.commit();
                }
            } catch (RuntimeException e) {
                HwLog.i("update", "exceptin id = " + id);
            }
        }
    }

    public void clearUpdateData() {
        this.mCities = null;
        this.mCountryname = null;
        this.mTimezones = null;
        this.mIds = null;
        this.mIndexMap.clear();
        this.mIndexMap = null;
        TimeZoneUtils.clearCityData();
    }

    private void upgradeTableWorldClockWidget(SQLiteDatabase db) {
        Log.printf("+upgradeTableWorldClockWidget", new Object[0]);
        Cursor cursor = null;
        SQLiteDatabase sQLiteDatabase = db;
        cursor = sQLiteDatabase.query("widgets", new String[]{"_id", "first_index", "second_index"}, null, null, null, null, null);
        if (cursor != null) {
            Editor editor = Utils.getSharedPreferences(this.mCtx, "timeZone.cfg", 0).edit();
            while (cursor.moveToNext()) {
                String cityIndex1 = cursor.getString(cursor.getColumnIndex("first_index"));
                String cityIndex2 = cursor.getString(cursor.getColumnIndex("second_index"));
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                int i = 0;
                String realIndex1 = null;
                try {
                    i = ((Integer) this.mIndexMap.get(cityIndex1)).intValue();
                    realIndex1 = this.mTimezones[i] + "_" + this.mCities[i] + "_" + this.mCountryname[i];
                } catch (RuntimeException e) {
                    Log.e("AlarmDatabaseHelper", "cityIndex1 =" + cityIndex1 + ", list = " + Arrays.asList(new Set[]{this.mIndexMap.entrySet()}).toString());
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                int i2 = 0;
                String realIndex2 = null;
                try {
                    i2 = ((Integer) this.mIndexMap.get(cityIndex2)).intValue();
                    realIndex2 = this.mTimezones[i2] + "_" + this.mCities[i2] + "_" + this.mCountryname[i2];
                } catch (RuntimeException e2) {
                    Log.e("AlarmDatabaseHelper", "cityIndex2 =" + cityIndex2 + ", list = " + Arrays.asList(new Set[]{this.mIndexMap.entrySet()}).toString());
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put("first_index", realIndex1);
                contentValues.put("second_index", realIndex2);
                SQLiteDatabase sQLiteDatabase2 = db;
                sQLiteDatabase2.update("widgets", contentValues, "_id=?", new String[]{String.valueOf(_id)});
                editor.putString(realIndex1, this.mCities[i] + "_" + i);
                editor.putString(realIndex2, this.mCities[i2] + "_" + i2);
            }
            editor.commit();
        }
        if (cursor != null) {
            cursor.close();
        }
        Log.printf("-upgradeTableWorldClockWidget", new Object[0]);
    }

    private void upgradeTableLocation(SQLiteDatabase db) {
        Log.printf("+upgradeTableLocation", new Object[0]);
        Cursor cursor = null;
        String cityIndex;
        try {
            SQLiteDatabase sQLiteDatabase = db;
            cursor = sQLiteDatabase.query("locations", new String[]{"_id", "city_index"}, null, null, null, null, null);
            if (cursor != null) {
                Editor editor = Utils.getSharedPreferences(this.mCtx, "timeZone.cfg", 0).edit();
                while (cursor.moveToNext()) {
                    cityIndex = cursor.getString(cursor.getColumnIndex("city_index"));
                    int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                    if (!("c500".equals(cityIndex) || "c501".equals(cityIndex) || "c502".equals(cityIndex))) {
                        int index = ((Integer) this.mIndexMap.get(cityIndex)).intValue();
                        String realIndex = this.mTimezones[index] + "_" + this.mCities[index] + "_" + this.mCountryname[index];
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("city_index", realIndex);
                        SQLiteDatabase sQLiteDatabase2 = db;
                        sQLiteDatabase2.update("locations", contentValues, "_id=?", new String[]{String.valueOf(_id)});
                        editor.putString(realIndex, this.mCities[index] + "_" + index);
                    }
                }
                editor.commit();
            }
            if (cursor != null) {
                cursor.close();
            }
            Log.printf("-upgradeTableLocation", new Object[0]);
        } catch (RuntimeException e) {
            Log.e("AlarmDatabaseHelper", "cityIndex =" + cityIndex + ", list = " + Arrays.asList(new Set[]{this.mIndexMap.entrySet()}).toString());
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("AlarmDatabaseHelper", "onDowngrade : Downgrading alarms database from version " + newVersion + " to " + oldVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS alarms");
        db.execSQL("DROP TABLE IF EXISTS locations");
        db.execSQL("DROP TABLE IF EXISTS widgets");
        onCreate(db);
    }

    private void upgradeForAlarm(SQLiteDatabase db) {
        String columns = getColumnNames(db, "alarms");
        if (columns != null) {
            updateTable(db, "alarms", columns);
        }
    }

    private void upgradeForLocation(SQLiteDatabase db) {
        Cursor cursor = null;
        boolean doInsertOperation = true;
        try {
            cursor = db.query("locations", new String[]{"timezone"}, "timezone = 'Add' OR timezone = 'Menu'", null, null, null, null);
            if (cursor != null) {
                doInsertOperation = cursor.getCount() <= 0;
            }
            if (cursor != null) {
                cursor.close();
            }
            if (doInsertOperation) {
                String insertDefault = "INSERT INTO locations (sort_order , city_index , timezone , homecity ) VALUES ";
                db.execSQL(insertDefault + "(10000, 'c500', 'Add', 0);");
                db.execSQL(insertDefault + "(20000, 'c501', 'Menu', 0);");
            }
            db.execSQL("UPDATE locations SET sort_order=9999 WHERE sort_order=2147483647");
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void upgradeForNoNemu(SQLiteDatabase db) {
        db.execSQL("INSERT INTO locations (sort_order , city_index , timezone , homecity ) VALUES " + "(15000, 'c502', 'Edit', 0);");
    }

    private void updateV11TOV12(SQLiteDatabase db) {
        db.execSQL("UPDATE alarms SET  daysofweektype = 2  WHERE  daysofweek = 127 AND  daysofweektype = 3");
        db.execSQL("UPDATE alarms SET  daysofweektype = 1  WHERE  daysofweek = 31 AND  daysofweektype = 3");
    }

    private void deletebtnInSQL(SQLiteDatabase db) {
        db.execSQL("DELETE FROM locations WHERE city_index = 'c500'");
        db.execSQL("DELETE FROM locations WHERE city_index = 'c501'");
        db.execSQL("DELETE FROM locations WHERE city_index = 'c502'");
    }

    protected void updateTable(SQLiteDatabase db, String tableName, String columns) {
        try {
            db.beginTransaction();
            String reColumn = columns.substring(0, columns.length() - 1);
            String tempTable = tableName + "temp_table";
            db.execSQL("ALTER TABLE " + tableName + " RENAME TO " + tempTable);
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
            if ("alarms".equals(tableName)) {
                db.execSQL("CREATE TABLE alarms (_id INTEGER PRIMARY KEY,hour INTEGER, minutes INTEGER, daysofweek INTEGER, alarmtime INTEGER, enabled INTEGER, vibrate INTEGER, volume INTEGER, message TEXT, alert TEXT, daysofweektype INTEGER, daysofweekshow TEXT);");
            } else if ("locations".equals(tableName)) {
                db.execSQL("CREATE TABLE locations (_id INTEGER PRIMARY KEY AUTOINCREMENT,sort_order INTEGER,city_index TEXT,timezone TEXT,homecity INTEGER);");
            } else if ("widgets".equals(tableName)) {
                db.execSQL("CREATE TABLE widgets (_id INTEGER PRIMARY KEY AUTOINCREMENT,cityname VARCHAR(128),timezone VARCHAR(128),widget_id INTEGER,first_timezone VARCHAR(128),second_timezone VARCHAR(128),first_index VARCHAR(128),second_index VARCHAR(128));");
            }
            db.execSQL("INSERT INTO " + tableName + " (" + reColumn + ") " + "SELECT " + reColumn + "" + " " + " FROM " + tempTable);
            db.execSQL("UPDATE " + tableName + " SET " + " daysofweektype = 3 " + " WHERE " + " daysofweek > 0 AND " + " daysofweektype IS NULL");
            db.execSQL("DROP TABLE IF EXISTS " + tempTable);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.w("AlarmDatabaseHelper", "updateTable : SQLException = " + e.getMessage());
        } catch (Exception e2) {
            Log.w("AlarmDatabaseHelper", "updateTable : Exception = " + e2.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    protected String getColumnNames(SQLiteDatabase db, String tableName) {
        Exception e;
        Throwable th;
        StringBuffer stringBuffer = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndex("name");
                if (-1 == columnIndex) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return null;
                }
                StringBuffer sb = new StringBuffer(cursor.getCount());
                try {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        sb.append(cursor.getString(columnIndex));
                        sb.append(",");
                        cursor.moveToNext();
                    }
                    stringBuffer = sb;
                } catch (Exception e2) {
                    e = e2;
                    stringBuffer = sb;
                    try {
                        Log.w("AlarmDatabaseHelper", "getColumnNames : Exception = " + e.getMessage());
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (stringBuffer == null) {
                            return stringBuffer.toString();
                        }
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e3) {
            e = e3;
            Log.w("AlarmDatabaseHelper", "getColumnNames : Exception = " + e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            if (stringBuffer == null) {
                return null;
            }
            return stringBuffer.toString();
        }
        if (stringBuffer == null) {
            return null;
        }
        return stringBuffer.toString();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getHomeClockExistId() {
        int id = -1;
        try {
            Cursor cursor = this.mCtx.getContentResolver().query(LocationColumns.CONTENT_URI, new String[]{"_id"}, "homecity = ?", new String[]{"1"}, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    id = cursor.getInt(cursor.getColumnIndex("_id"));
                }
            }
            closeQuietly(cursor);
        } catch (Exception e) {
            Log.w("AlarmDatabaseHelper", "getHomeClockExistId : Exception = " + e.getMessage());
        } catch (Throwable th) {
            closeQuietly(null);
        }
        return id;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getCityIndexExistId(String uuid) {
        int id = -1;
        try {
            Cursor cursor = this.mCtx.getContentResolver().query(LocationColumns.CONTENT_URI, new String[]{"_id"}, "city_index = ?", new String[]{uuid}, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    id = cursor.getInt(cursor.getColumnIndex("_id"));
                }
            }
            closeQuietly(cursor);
        } catch (Exception e) {
            Log.w("AlarmDatabaseHelper", "getCityIndexExistId : Exception = " + e.getMessage());
        } catch (Throwable th) {
            closeQuietly(null);
        }
        return id;
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                Log.w("AlarmDatabaseHelper", "closeQuietly : Exception = " + e.getMessage());
            }
        }
    }
}
