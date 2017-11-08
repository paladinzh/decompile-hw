package com.android.contacts.hap.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import com.android.contacts.util.HwLog;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;

public class ContactsAppDatabaseHelper extends SQLiteOpenHelper {

    public interface NumberMark {
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.contacts.app/number_mark");
    }

    public interface NumberMarkExtras {
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.contacts.app/number_mark_extras");
    }

    public interface SpeedDialContract {
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.contacts.app/speed_dial");
    }

    public static synchronized ContactsAppDatabaseHelper getInstance(Context context) {
        ContactsAppDatabaseHelper contactsAppDatabaseHelper;
        synchronized (ContactsAppDatabaseHelper.class) {
            if (ContactsAppProvider.sSingleton == null) {
                ContactsAppProvider.sSingleton = new ContactsAppDatabaseHelper(context.createDeviceProtectedStorageContext());
            }
            contactsAppDatabaseHelper = ContactsAppProvider.sSingleton;
        }
        return contactsAppDatabaseHelper;
    }

    public static ContactsAppDatabaseHelper resetInstance(Context context) {
        ContactsAppProvider.sSingleton = new ContactsAppDatabaseHelper(context.createDeviceProtectedStorageContext());
        return ContactsAppProvider.sSingleton;
    }

    ContactsAppDatabaseHelper(Context context) {
        super(context, "contacts_app.db", null, 5);
    }

    public void onUpgrade(SQLiteDatabase aDb, int aOldVersion, int aNewVersion) {
        HwLog.i("ContactsAppProvider", "Upgrading database from version " + aOldVersion + " to " + aNewVersion + ", which will destroy all old data");
        aDb.execSQL("DROP TABLE IF EXISTS pre_installed");
        createSpeedDialTable(aDb);
        if (aOldVersion < 4) {
            createPropertyTable(aDb);
            createNumberMarkTable(aDb);
            createYellowPageTable(aDb);
            createYellowPagePhoneTable(aDb);
            createYellowPageView(aDb);
            setProperty(aDb, "yellow_page_version", 0);
        }
        if (aOldVersion == 4) {
            upgradeToVersion5(aDb);
        }
    }

    public void onCreate(SQLiteDatabase aDb) {
        createYellowPageTable(aDb);
        createYellowPagePhoneTable(aDb);
        createSpeedDialTable(aDb);
        createPropertyTable(aDb);
        createNumberMarkTable(aDb);
        createNumberMarkExtrasTable(aDb);
        createYellowPageView(aDb);
        setProperty(aDb, "yellow_page_version", 0);
    }

    private void createYellowPageTable(SQLiteDatabase aDb) {
        StringBuffer builder = new StringBuffer();
        builder.append("CREATE TABLE ").append("yellow_page").append(" (").append("_ID").append(" INTEGER PRIMARY KEY AUTOINCREMENT,").append("name").append(" TEXT,").append("group_name").append(" TEXT,").append(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH).append(" TEXT,").append("photo").append(" TEXT").append(");");
        aDb.execSQL(builder.toString());
    }

    private void createYellowPagePhoneTable(SQLiteDatabase aDb) {
        StringBuffer builder = new StringBuffer();
        builder.append("CREATE TABLE ").append("yellow_page_phone").append(" (").append("_ID").append(" INTEGER PRIMARY KEY AUTOINCREMENT,").append("name").append(" TEXT,").append("number").append(" TEXT,").append("hot_points").append(" NUMERIC,").append("dial_map").append(" TEXT, ").append("ypid").append(" INTEGER, ").append(" FOREIGN KEY (").append("ypid").append(") REFERENCES ").append("yellow_page").append(" (").append("_ID").append(") ON DELETE CASCADE").append(");");
        aDb.execSQL(builder.toString());
    }

    private void createSpeedDialTable(SQLiteDatabase aDb) {
        StringBuffer builder = new StringBuffer();
        builder.append("CREATE TABLE IF NOT EXISTS ").append("speed_dial").append("(").append("key_number").append(" INTEGER PRIMARY KEY,").append("phone_data_id").append(" INTEGER DEFAULT -1,").append("number").append(" TEXT,").append("pre_set").append(" INTEGER DEFAULT 0 );");
        aDb.execSQL(builder.toString());
    }

    private void createNumberMarkTable(SQLiteDatabase aDb) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ").append("number_mark").append("(").append("_ID").append(" INTEGER PRIMARY KEY AUTOINCREMENT,").append("NUMBER").append(" Text,").append("NAME").append(" Text,").append("CLASSIFY").append(" Text,").append("MARKED_COUNT").append(" int DEFAULT 0,").append("IS_CLOUD").append(" int default 0,").append("DESCRIPTION").append(" Text,").append("SAVE_TIMESTAMP").append(" Text,").append("SUPPLIER").append(" Text);");
        aDb.execSQL(builder.toString());
    }

    private void createNumberMarkExtrasTable(SQLiteDatabase aDb) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ").append("number_mark_extras").append("(").append("_ID").append(" INTEGER PRIMARY KEY AUTOINCREMENT,").append("NUMBER").append(" TEXT,").append("TITLE").append(" TEXT,").append("CONTENT").append(" TEXT,").append("TYPE").append(" TEXT,").append("ICON").append(" TEXT,").append("INTERNAL_LINK").append(" TEXT,").append("EXTERNAL_LINK").append(" TEXT,").append("LONGITUDE").append(" TEXT,").append("LATITUDE").append(" TEXT,").append("TIMESTAMP").append(" TEXT);");
        aDb.execSQL(builder.toString());
    }

    private void createPropertyTable(SQLiteDatabase aDb) {
        aDb.execSQL("CREATE TABLE properties (property_key TEXT PRIMARY KEY, property_value INTEGER );");
    }

    private void createYellowPageView(SQLiteDatabase aDb) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE VIEW ").append("yellow_page_view").append(" AS SELECT ").append("yellow_page_phone").append(".").append("_ID").append(", ").append("yellow_page").append(".").append("photo").append(", ").append("'").append(ContactsAppProvider.YELLOW_PAGE_DATA_URI).append("?path='||").append("yellow_page").append(".").append("photo").append(" as ").append("photouri").append(", ").append("yellow_page_phone").append(".").append("ypid").append(", ").append("yellow_page_phone").append(".").append("name").append(", ").append("yellow_page_phone").append(".").append("dial_map").append(",").append("yellow_page_phone").append(".").append("hot_points").append(",").append("yellow_page_phone").append(".").append("number").append(" from ").append("yellow_page_phone").append(",").append("yellow_page").append(" WHERE ").append("yellow_page_phone").append(".").append("ypid").append(" = ").append("yellow_page").append(".").append("_ID");
        aDb.execSQL(builder.toString());
    }

    public void setProperty(SQLiteDatabase db, String key, int value) {
        ContentValues values = new ContentValues();
        values.put("property_key", key);
        values.put("property_value", Integer.valueOf(value));
        db.replace("properties", null, values);
    }

    public int getProperty(SQLiteDatabase db, String key, int defaultValue) {
        Cursor cursor;
        try {
            SQLiteDatabase sQLiteDatabase = db;
            cursor = sQLiteDatabase.query("properties", new String[]{"property_value"}, "property_key=?", new String[]{key}, null, null, null);
            int value = defaultValue;
            if (cursor.moveToFirst()) {
                value = cursor.getInt(0);
            }
            cursor.close();
            return value;
        } catch (SQLiteException e) {
            HwLog.w("ContactsAppDatabaseHelper", "get property failure.", e);
            return defaultValue;
        } catch (Throwable th) {
            cursor.close();
        }
    }

    private void upgradeToVersion5(SQLiteDatabase aDb) {
        aDb.execSQL("ALTER TABLE number_mark ADD SAVE_TIMESTAMP TEXT;");
        aDb.execSQL("ALTER TABLE number_mark ADD SUPPLIER TEXT;");
        createNumberMarkExtrasTable(aDb);
    }
}
