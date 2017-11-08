package com.android.settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build.VERSION;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.xmlpull.v1.XmlPullParserException;

public class TimeZonesDatabaseHelper extends SQLiteOpenHelper {
    private static TimeZonesDatabaseHelper mDBHelper;
    private Context mContext;

    private TimeZonesDatabaseHelper(Context context, String name, int version) {
        super(context, name, null, version);
        this.mContext = context;
    }

    public static synchronized TimeZonesDatabaseHelper getInstance(Context context) {
        TimeZonesDatabaseHelper timeZonesDatabaseHelper;
        synchronized (TimeZonesDatabaseHelper.class) {
            if (mDBHelper == null) {
                mDBHelper = new TimeZonesDatabaseHelper(context, "timezones.db", 1);
            }
            timeZonesDatabaseHelper = mDBHelper;
        }
        return timeZonesDatabaseHelper;
    }

    public void onCreate(SQLiteDatabase db) {
        bootstrapDB(db);
    }

    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        Log.i("ZonePickerDatabaseHelper", "Using schema version: " + db.getVersion());
        if (VERSION.INCREMENTAL.equals(getBuildVersion(db)) && Locale.getDefault().toString().equals(getLocale(db))) {
            Log.i("ZonePickerDatabaseHelper", "Index is fine");
            return;
        }
        Log.w("ZonePickerDatabaseHelper", "Index needs to be rebuilt as build-version or locale is not the same");
        reconstruct(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            Log.w("ZonePickerDatabaseHelper", "Detected schema version '" + oldVersion + "'. " + "Index needs to be rebuilt for schema version '" + newVersion + "'.");
            reconstruct(db);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("ZonePickerDatabaseHelper", "Detected schema version '" + oldVersion + "'. " + "Index needs to be rebuilt for schema version '" + newVersion + "'.");
        reconstruct(db);
    }

    private void insertRecord(SQLiteDatabase db, String table, ContentValues row) {
        db.insert(table, null, row);
    }

    private List<Map<String, Object>> insertTimeZones(SQLiteDatabase db) {
        List<Map<String, Object>> myData = new ArrayList();
        long date = Calendar.getInstance().getTimeInMillis();
        XmlResourceParser xmlResourceParser = null;
        try {
            xmlResourceParser = this.mContext.getResources().getXml(2131230913);
            do {
            } while (xmlResourceParser.next() != 2);
            xmlResourceParser.next();
            while (xmlResourceParser.getEventType() != 3) {
                while (xmlResourceParser.getEventType() != 2) {
                    if (xmlResourceParser.getEventType() == 1) {
                        if (xmlResourceParser != null) {
                            xmlResourceParser.close();
                        }
                        return myData;
                    }
                    xmlResourceParser.next();
                }
                if (xmlResourceParser.getName().equals("timezone")) {
                    addItem(db, myData, xmlResourceParser.getAttributeValue(null, "id"), xmlResourceParser.getAttributeValue(null, "city"), xmlResourceParser.getAttributeValue(null, "country"), xmlResourceParser.nextText(), date);
                }
                while (xmlResourceParser.getEventType() != 3) {
                    xmlResourceParser.next();
                }
                xmlResourceParser.next();
            }
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (XmlPullParserException e) {
            Log.e("ZonePickerDatabaseHelper", "Ill-formatted timezones.xml file");
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (IOException e2) {
            Log.e("ZonePickerDatabaseHelper", "Unable to read timezones.xml file");
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
        this.mContext.sendBroadcast(new Intent("huawei.intent.action.ZONE_PICKER_LOAD_COMPLETED"), "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM");
        Log.d("ZonePickerDatabaseHelper", "insertTimeZones.sendBroadcast:huawei.intent.action.ZONE_PICKER_LOAD_COMPLETED");
        return myData;
    }

    private void addItem(SQLiteDatabase db, List<Map<String, Object>> myData, String id, String city, String country, String displayName, long date) {
        ContentValues contentValues = new ContentValues();
        HashMap<String, Object> map = new HashMap();
        contentValues.put("id", id);
        contentValues.put("city", city);
        contentValues.put("country", country);
        contentValues.put("unique_id", id + "_" + city + "_" + country);
        contentValues.put("name", displayName);
        int offset = TimeZone.getTimeZone(id).getOffset(date);
        int p = Math.abs(offset);
        StringBuilder gmt = new StringBuilder();
        gmt.append("GMT");
        if (offset < 0) {
            gmt.append('-');
        } else {
            gmt.append('+');
        }
        gmt.append(p / 3600000);
        gmt.append(':');
        int min = (p / 60000) % 60;
        if (min < 10) {
            gmt.append('0');
        }
        gmt.append(min);
        contentValues.put("gmt", gmt.toString());
        contentValues.put("offset", Integer.valueOf(offset));
        if (contentValues.size() > 0) {
            insertRecord(db, "timezones", contentValues);
        }
        myData.add(map);
    }

    private void createTimeZoneTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS timezones (_id INTEGER PRIMARY KEY,unique_id TEXT,id TEXT,city TEXT,country TEXT,name TEXT,gmt TEXT,offset INTEGER);");
    }

    private void dropTimeZoneTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS timezones");
    }

    private void dropAllTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS meta_index");
        db.execSQL("DROP TABLE IF EXISTS locale_metadata");
        dropTimeZoneTable(db);
    }

    private void reconstruct(SQLiteDatabase db) {
        dropAllTables(db);
        bootstrapDB(db);
    }

    private void bootstrapDB(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE meta_index(build VARCHAR(32) NOT NULL)");
        db.execSQL("INSERT INTO meta_index VALUES ('" + VERSION.INCREMENTAL + "');");
        db.execSQL("CREATE TABLE locale_metadata(locale VARCHAR(32) NOT NULL)");
        db.execSQL("INSERT INTO locale_metadata VALUES ('" + Locale.getDefault().toString() + "');");
        createTimeZoneTable(db);
        insertTimeZones(db);
        Log.i("ZonePickerDatabaseHelper", "Bootstrapped database completed!");
    }

    private String getBuildVersion(SQLiteDatabase db) {
        String str = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT build FROM meta_index LIMIT 1;", null);
            if (cursor.moveToFirst()) {
                str = cursor.getString(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("ZonePickerDatabaseHelper", "Cannot get build version from Index metadata");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return str;
    }

    private String getLocale(SQLiteDatabase db) {
        String str = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT locale FROM locale_metadata LIMIT 1;", null);
            if (cursor.moveToFirst()) {
                str = cursor.getString(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("ZonePickerDatabaseHelper", "Cannot get build version from Index metadata");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return str;
    }
}
