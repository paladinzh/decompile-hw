package com.android.contacts.dialpad.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.DeletedContacts;
import com.android.contacts.dialpad.SmartDialNameMatcher;
import com.android.contacts.dialpad.SmartDialPrefix;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.StopWatch;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class DialerDatabaseHelper extends SQLiteOpenHelper {
    private static final boolean DEBUG = HwLog.HWDBG;
    private static final Object mLock = new Object();
    private static final AtomicBoolean sInUpdate = new AtomicBoolean(false);
    private static DialerDatabaseHelper sSingleton = null;
    private final Context mContext;

    private static class ContactMatch {
        private final long id;
        private final String lookupKey;

        public ContactMatch(String lookupKey, long id) {
            this.lookupKey = lookupKey;
            this.id = id;
        }

        public int hashCode() {
            return Objects.hashCode(this.lookupKey, Long.valueOf(this.id));
        }

        public boolean equals(Object object) {
            boolean z = false;
            if (this == object) {
                return true;
            }
            if (!(object instanceof ContactMatch)) {
                return false;
            }
            ContactMatch that = (ContactMatch) object;
            if (Objects.equal(this.lookupKey, that.lookupKey)) {
                z = Objects.equal(Long.valueOf(this.id), Long.valueOf(that.id));
            }
            return z;
        }
    }

    public static class ContactNumber {
        public final String company;
        public final String data1;
        public final String data10;
        public final String data11;
        public final String data2;
        public final String data3;
        public final long dataId;
        public final String displayName;
        public final long id;
        public final String lookup;
        public final long photoId;
        public final String photoUri;
        public final String sortKey;
        public final long timeContacted;

        public ContactNumber(long id, long dataId, String displayName, String sortKey, long photoId, String photoUri, String data1, String data2, String data3, String data10, String data11, String lookup, long timeContacted, String company) {
            this.id = id;
            this.dataId = dataId;
            this.displayName = displayName;
            this.sortKey = sortKey;
            this.photoId = photoId;
            this.photoUri = photoUri;
            this.data1 = data1;
            this.data2 = data2;
            this.data3 = data3;
            this.data10 = data10;
            this.data11 = data11;
            this.lookup = lookup;
            this.timeContacted = timeContacted;
            this.company = company;
        }

        public int hashCode() {
            return Objects.hashCode(Long.valueOf(this.id), Long.valueOf(this.dataId), this.displayName, this.data1, this.lookup, Long.valueOf(this.photoId));
        }

        public boolean equals(Object object) {
            boolean z = false;
            if (this == object) {
                return true;
            }
            if (!(object instanceof ContactNumber)) {
                return false;
            }
            ContactNumber that = (ContactNumber) object;
            if (Objects.equal(Long.valueOf(this.id), Long.valueOf(that.id)) && Objects.equal(Long.valueOf(this.dataId), Long.valueOf(that.dataId)) && Objects.equal(this.displayName, that.displayName) && Objects.equal(this.sortKey, that.sortKey) && Objects.equal(Long.valueOf(this.photoId), Long.valueOf(that.photoId)) && Objects.equal(this.photoUri, that.photoUri) && Objects.equal(this.data1, that.data1) && Objects.equal(this.data2, that.data2) && Objects.equal(this.data3, that.data3) && Objects.equal(this.data10, that.data10) && Objects.equal(this.data11, that.data11) && Objects.equal(this.lookup, that.lookup) && Objects.equal(Long.valueOf(this.timeContacted), Long.valueOf(that.timeContacted))) {
                z = Objects.equal(this.company, that.company);
            }
            return z;
        }
    }

    public interface DeleteContactQuery {
        public static final String[] PROJECTION = new String[]{"contact_id", "contact_deleted_timestamp"};
        public static final Uri URI = DeletedContacts.CONTENT_URI;
    }

    public interface PhoneQuery {
        public static final String[] PROJECTION = new String[]{"_id", "data2", "data3", "data1", "contact_id", "lookup", "display_name", "photo_id", "last_time_used", "times_used", "starred", "is_super_primary", "in_visible_group", "is_primary", "photo_uri", "sort_key"};
        public static final Uri URI = Phone.CONTENT_URI.buildUpon().appendQueryParameter("directory", String.valueOf(0)).appendQueryParameter("remove_duplicate_entries", "true").build();
    }

    private class SmartDialUpdateAsyncTask extends AsyncTask {
        private SmartDialUpdateAsyncTask() {
        }

        protected Object doInBackground(Object[] objects) {
            if (DialerDatabaseHelper.DEBUG) {
                HwLog.v("DialerDatabaseHelper", "Updating database");
            }
            DialerDatabaseHelper.this.updateSmartDialDatabase();
            return null;
        }

        protected void onCancelled() {
            if (DialerDatabaseHelper.DEBUG) {
                HwLog.v("DialerDatabaseHelper", "Updating Cancelled");
            }
            super.onCancelled();
        }

        protected void onPostExecute(Object o) {
            if (DialerDatabaseHelper.DEBUG) {
                HwLog.v("DialerDatabaseHelper", "Updating Finished");
            }
            super.onPostExecute(o);
        }
    }

    public static synchronized DialerDatabaseHelper getInstance(Context context) {
        DialerDatabaseHelper dialerDatabaseHelper;
        synchronized (DialerDatabaseHelper.class) {
            if (DEBUG) {
                HwLog.v("DialerDatabaseHelper", "Getting Instance");
            }
            if (sSingleton == null) {
                sSingleton = new DialerDatabaseHelper(context.getApplicationContext(), "dialer.db");
            }
            dialerDatabaseHelper = sSingleton;
        }
        return dialerDatabaseHelper;
    }

    @VisibleForTesting
    static DialerDatabaseHelper getNewInstanceForTest(Context context) {
        return new DialerDatabaseHelper(context, null);
    }

    protected DialerDatabaseHelper(Context context, String databaseName) {
        this(context, databaseName, 4);
    }

    protected DialerDatabaseHelper(Context context, String databaseName, int dbVersion) {
        super(context, databaseName, null, dbVersion);
        this.mContext = (Context) Preconditions.checkNotNull(context, "Context must not be null");
    }

    public void onCreate(SQLiteDatabase db) {
        setupTables(db);
    }

    private void setupTables(SQLiteDatabase db) {
        dropTables(db);
        db.execSQL("CREATE TABLE smartdial_table (id INTEGER PRIMARY KEY AUTOINCREMENT,data_id INTEGER, phone_number TEXT,contact_id INTEGER,lookup TEXT,display_name TEXT, photo_id INTEGER, last_smartdial_update_time LONG, last_time_used LONG, times_used INTEGER, starred INTEGER, is_super_primary INTEGER, in_visible_group INTEGER, is_primary INTEGER, photo_uri TEXT, sort_key_primary TEXT, sort_key TEXT, data1 TEXT, data2 TEXT, data3 TEXT, data10 TEXT, data11 TEXT, times_contacted LONG,company TEXT);");
        db.execSQL("CREATE TABLE prefix_table (_id INTEGER PRIMARY KEY AUTOINCREMENT,prefix TEXT COLLATE NOCASE, contact_id INTEGER);");
        db.execSQL("CREATE TABLE properties (property_key TEXT PRIMARY KEY, property_value TEXT );");
        setProperty(db, "database_version", String.valueOf(4));
        resetSmartDialLastUpdatedTime();
    }

    public void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS prefix_table");
        db.execSQL("DROP TABLE IF EXISTS smartdial_table");
        db.execSQL("DROP TABLE IF EXISTS properties");
    }

    public void onUpgrade(SQLiteDatabase db, int oldNumber, int newNumber) {
        int oldVersion = getPropertyAsInt(db, "database_version", 0);
        if (oldVersion == 0) {
            HwLog.e("DialerDatabaseHelper", "Malformed database version..recreating database");
        }
        if (oldVersion < 4) {
            setupTables(db);
        } else if (oldVersion != 4) {
            throw new IllegalStateException("error upgrading the database to version 4");
        } else {
            setProperty(db, "database_version", String.valueOf(4));
        }
    }

    public void setProperty(SQLiteDatabase db, String key, String value) {
        ContentValues values = new ContentValues();
        values.put("property_key", key);
        values.put("property_value", value);
        db.replace("properties", null, values);
    }

    public String getProperty(SQLiteDatabase db, String key, String defaultValue) {
        Cursor cursor;
        try {
            SQLiteDatabase sQLiteDatabase = db;
            cursor = sQLiteDatabase.query("properties", new String[]{"property_value"}, "property_key=?", new String[]{key}, null, null, null);
            String value = null;
            if (cursor.moveToFirst()) {
                value = cursor.getString(0);
            }
            cursor.close();
            if (value == null) {
                value = defaultValue;
            }
            return value;
        } catch (SQLiteException e) {
            return defaultValue;
        } catch (Throwable th) {
            cursor.close();
        }
    }

    public int getPropertyAsInt(SQLiteDatabase db, String key, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(db, key, ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void resetSmartDialLastUpdatedTime() {
        Editor editor = this.mContext.getSharedPreferences("com.android.dialer", 0).edit();
        editor.putLong("last_updated_millis", 0);
        editor.commit();
    }

    public void startSmartDialUpdateThread() {
        new SmartDialUpdateAsyncTask().execute(new Object[0]);
    }

    private void removeDeletedContacts(SQLiteDatabase db, String last_update_time) {
        Cursor deletedContactCursor = this.mContext.getContentResolver().query(DeleteContactQuery.URI, DeleteContactQuery.PROJECTION, "contact_deleted_timestamp > ?", new String[]{last_update_time}, null);
        db.beginTransaction();
        while (deletedContactCursor.moveToNext()) {
            try {
                Long deleteContactId = Long.valueOf(deletedContactCursor.getLong(0));
                db.delete("smartdial_table", "contact_id=" + deleteContactId, null);
                db.delete("prefix_table", "contact_id=" + deleteContactId, null);
            } finally {
                deletedContactCursor.close();
                db.endTransaction();
            }
        }
        db.setTransactionSuccessful();
    }

    private void removePotentiallyCorruptedContacts(SQLiteDatabase db, String last_update_time) {
        db.delete("prefix_table", "contact_id IN (SELECT contact_id FROM smartdial_table WHERE last_smartdial_update_time > " + last_update_time + ")", null);
        db.delete("smartdial_table", "last_smartdial_update_time > " + last_update_time, null);
    }

    @VisibleForTesting
    void removeAllContacts(SQLiteDatabase db) {
        db.delete("smartdial_table", null, null);
        db.delete("prefix_table", null, null);
    }

    @VisibleForTesting
    int countPrefixTableRows(SQLiteDatabase db) {
        return (int) DatabaseUtils.longForQuery(db, "SELECT COUNT(1) FROM prefix_table", null);
    }

    private void removeUpdatedContacts(SQLiteDatabase db, Cursor updatedContactCursor) {
        db.beginTransaction();
        while (updatedContactCursor.moveToNext()) {
            try {
                Long contactId = Long.valueOf(updatedContactCursor.getLong(10));
                db.delete("smartdial_table", "contact_id=" + contactId, null);
                db.delete("prefix_table", "contact_id=" + contactId, null);
            } finally {
                db.endTransaction();
            }
        }
        db.setTransactionSuccessful();
    }

    @VisibleForTesting
    protected void insertUpdatedContactsAndNumberPrefix(SQLiteDatabase db, Cursor updatedContactCursor, Long currentMillis) {
        db.beginTransaction();
        try {
            String sqlInsert = "INSERT INTO smartdial_table (data_id, display_name, sort_key, photo_id, photo_uri, data1, data2, data3, data10, data11, contact_id, lookup, times_contacted, company, last_smartdial_update_time)  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            SQLiteStatement insert = db.compileStatement("INSERT INTO smartdial_table (data_id, display_name, sort_key, photo_id, photo_uri, data1, data2, data3, data10, data11, contact_id, lookup, times_contacted, company, last_smartdial_update_time)  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            String numberSqlInsert = "INSERT INTO prefix_table (contact_id, prefix)  VALUES (?, ?)";
            SQLiteStatement numberInsert = db.compileStatement("INSERT INTO prefix_table (contact_id, prefix)  VALUES (?, ?)");
            updatedContactCursor.moveToPosition(-1);
            while (updatedContactCursor.moveToNext()) {
                insert.bindLong(1, updatedContactCursor.getLong(0));
                String displayName = updatedContactCursor.getString(1);
                if (displayName != null) {
                    insert.bindString(2, displayName);
                }
                String sortKey = updatedContactCursor.getString(2);
                if (sortKey != null) {
                    insert.bindString(3, sortKey);
                }
                insert.bindLong(4, updatedContactCursor.getLong(3));
                String photoUri = updatedContactCursor.getString(4);
                if (photoUri != null) {
                    insert.bindString(5, photoUri);
                }
                String phoneNumber = updatedContactCursor.getString(5);
                if (phoneNumber != null) {
                    insert.bindString(6, phoneNumber);
                }
                String phonetype = updatedContactCursor.getString(6);
                if (phonetype != null) {
                    insert.bindString(7, phonetype);
                }
                String phontLabel = updatedContactCursor.getString(7);
                if (phontLabel != null) {
                    insert.bindString(8, phontLabel);
                }
                if (updatedContactCursor.getString(8) != null) {
                    insert.bindString(9, updatedContactCursor.getString(8));
                }
                if (updatedContactCursor.getString(9) != null) {
                    insert.bindString(10, updatedContactCursor.getString(9));
                }
                insert.bindLong(11, updatedContactCursor.getLong(10));
                String lookupKey = updatedContactCursor.getString(11);
                if (lookupKey != null) {
                    insert.bindString(12, lookupKey);
                }
                insert.bindLong(13, updatedContactCursor.getLong(12));
                String company = updatedContactCursor.getString(13);
                if (company != null) {
                    insert.bindString(14, company);
                }
                insert.bindLong(15, currentMillis.longValue());
                insert.executeInsert();
                insert.clearBindings();
                for (String numberPrefix : SmartDialPrefix.parseToNumberTokens(updatedContactCursor.getString(5))) {
                    numberInsert.bindLong(1, updatedContactCursor.getLong(10));
                    numberInsert.bindString(2, numberPrefix);
                    numberInsert.executeInsert();
                    numberInsert.clearBindings();
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @VisibleForTesting
    void insertNamePrefixes(SQLiteDatabase db, Cursor nameCursor) {
        int columnIndexName = nameCursor.getColumnIndex("display_name");
        int columnIndexContactId = nameCursor.getColumnIndex("contact_id");
        db.beginTransaction();
        try {
            String sqlInsert = "INSERT INTO prefix_table (contact_id, prefix)  VALUES (?, ?)";
            SQLiteStatement insert = db.compileStatement("INSERT INTO prefix_table (contact_id, prefix)  VALUES (?, ?)");
            while (nameCursor.moveToNext()) {
                for (String namePrefix : SmartDialPrefix.generateNamePrefixes(nameCursor.getString(columnIndexName))) {
                    insert.bindLong(1, nameCursor.getLong(columnIndexContactId));
                    insert.bindString(2, namePrefix);
                    insert.executeInsert();
                    insert.clearBindings();
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void updateSmartDialDatabase() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Assign predecessor not found for B:61:? from B:65:?
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.replaceMerge(EliminatePhiNodes.java:102)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.replaceMergeInstructions(EliminatePhiNodes.java:68)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.visit(EliminatePhiNodes.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r15 = this;
        r8 = r15.getWritableDatabase();
        r14 = mLock;
        monitor-enter(r14);
        r0 = DEBUG;	 Catch:{ all -> 0x0183 }
        if (r0 == 0) goto L_0x0014;	 Catch:{ all -> 0x0183 }
    L_0x000b:
        r0 = "DialerDatabaseHelper";	 Catch:{ all -> 0x0183 }
        r1 = "Starting to update database";	 Catch:{ all -> 0x0183 }
        com.android.contacts.util.HwLog.v(r0, r1);	 Catch:{ all -> 0x0183 }
    L_0x0014:
        r0 = DEBUG;	 Catch:{ all -> 0x0183 }
        if (r0 == 0) goto L_0x0091;	 Catch:{ all -> 0x0183 }
    L_0x0018:
        r0 = "Updating databases";	 Catch:{ all -> 0x0183 }
        r12 = com.android.contacts.util.StopWatch.start(r0);	 Catch:{ all -> 0x0183 }
    L_0x001f:
        r0 = r15.mContext;	 Catch:{ all -> 0x0183 }
        r1 = "com.android.dialer";	 Catch:{ all -> 0x0183 }
        r2 = 0;	 Catch:{ all -> 0x0183 }
        r7 = r0.getSharedPreferences(r1, r2);	 Catch:{ all -> 0x0183 }
        r0 = "last_updated_millis";	 Catch:{ all -> 0x0183 }
        r2 = 0;	 Catch:{ all -> 0x0183 }
        r0 = r7.getLong(r0, r2);	 Catch:{ all -> 0x0183 }
        r10 = java.lang.String.valueOf(r0);	 Catch:{ all -> 0x0183 }
        r0 = DEBUG;	 Catch:{ all -> 0x0183 }
        if (r0 == 0) goto L_0x0054;	 Catch:{ all -> 0x0183 }
    L_0x003a:
        r0 = "DialerDatabaseHelper";	 Catch:{ all -> 0x0183 }
        r1 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0183 }
        r1.<init>();	 Catch:{ all -> 0x0183 }
        r2 = "Last updated at ";	 Catch:{ all -> 0x0183 }
        r1 = r1.append(r2);	 Catch:{ all -> 0x0183 }
        r1 = r1.append(r10);	 Catch:{ all -> 0x0183 }
        r1 = r1.toString();	 Catch:{ all -> 0x0183 }
        com.android.contacts.util.HwLog.v(r0, r1);	 Catch:{ all -> 0x0183 }
    L_0x0054:
        r0 = r15.mContext;	 Catch:{ all -> 0x0183 }
        r0 = r0.getContentResolver();	 Catch:{ all -> 0x0183 }
        r1 = com.android.contacts.dialpad.database.DialerDatabaseHelper.PhoneQuery.URI;	 Catch:{ all -> 0x0183 }
        r2 = com.huawei.cspcommon.util.SmartDialType.getProjection();	 Catch:{ all -> 0x0183 }
        r3 = "contact_last_updated_timestamp > ?";	 Catch:{ all -> 0x0183 }
        r4 = 1;	 Catch:{ all -> 0x0183 }
        r4 = new java.lang.String[r4];	 Catch:{ all -> 0x0183 }
        r5 = 0;	 Catch:{ all -> 0x0183 }
        r4[r5] = r10;	 Catch:{ all -> 0x0183 }
        r5 = 0;	 Catch:{ all -> 0x0183 }
        r13 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ all -> 0x0183 }
        r0 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x0183 }
        r6 = java.lang.Long.valueOf(r0);	 Catch:{ all -> 0x0183 }
        r0 = DEBUG;	 Catch:{ all -> 0x0183 }
        if (r0 == 0) goto L_0x0080;	 Catch:{ all -> 0x0183 }
    L_0x007a:
        r0 = "Queried the Contacts database";	 Catch:{ all -> 0x0183 }
        r12.lap(r0);	 Catch:{ all -> 0x0183 }
    L_0x0080:
        if (r13 != 0) goto L_0x0093;	 Catch:{ all -> 0x0183 }
    L_0x0082:
        r0 = DEBUG;	 Catch:{ all -> 0x0183 }
        if (r0 == 0) goto L_0x008f;	 Catch:{ all -> 0x0183 }
    L_0x0086:
        r0 = "DialerDatabaseHelper";	 Catch:{ all -> 0x0183 }
        r1 = "SmartDial query received null for cursor";	 Catch:{ all -> 0x0183 }
        com.android.contacts.util.HwLog.e(r0, r1);	 Catch:{ all -> 0x0183 }
    L_0x008f:
        monitor-exit(r14);
        return;
    L_0x0091:
        r12 = 0;
        goto L_0x001f;
    L_0x0093:
        r0 = sInUpdate;	 Catch:{ all -> 0x0183 }
        r1 = 1;	 Catch:{ all -> 0x0183 }
        r0.getAndSet(r1);	 Catch:{ all -> 0x0183 }
        r15.removeDeletedContacts(r8, r10);	 Catch:{ all -> 0x0183 }
        r15.removePotentiallyCorruptedContacts(r8, r10);	 Catch:{ all -> 0x0183 }
        r0 = DEBUG;	 Catch:{ all -> 0x0183 }
        if (r0 == 0) goto L_0x00a9;	 Catch:{ all -> 0x0183 }
    L_0x00a3:
        r0 = "Finished deleting deleted entries";	 Catch:{ all -> 0x0183 }
        r12.lap(r0);	 Catch:{ all -> 0x0183 }
    L_0x00a9:
        r0 = "0";	 Catch:{ all -> 0x017e }
        r0 = r10.equals(r0);	 Catch:{ all -> 0x017e }
        if (r0 != 0) goto L_0x00bf;	 Catch:{ all -> 0x017e }
    L_0x00b2:
        r15.removeUpdatedContacts(r8, r13);	 Catch:{ all -> 0x017e }
        r0 = DEBUG;	 Catch:{ all -> 0x017e }
        if (r0 == 0) goto L_0x00bf;	 Catch:{ all -> 0x017e }
    L_0x00b9:
        r0 = "Finished deleting updated entries";	 Catch:{ all -> 0x017e }
        r12.lap(r0);	 Catch:{ all -> 0x017e }
    L_0x00bf:
        r15.insertUpdatedContactsAndNumberPrefix(r8, r13, r6);	 Catch:{ all -> 0x017e }
        r0 = DEBUG;	 Catch:{ all -> 0x017e }
        if (r0 == 0) goto L_0x00cc;	 Catch:{ all -> 0x017e }
    L_0x00c6:
        r0 = "Finished building the smart dial table";	 Catch:{ all -> 0x017e }
        r12.lap(r0);	 Catch:{ all -> 0x017e }
    L_0x00cc:
        r13.close();	 Catch:{ all -> 0x0183 }
        r0 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0183 }
        r0.<init>();	 Catch:{ all -> 0x0183 }
        r1 = "SELECT DISTINCT display_name, contact_id FROM smartdial_table WHERE last_smartdial_update_time = ";	 Catch:{ all -> 0x0183 }
        r0 = r0.append(r1);	 Catch:{ all -> 0x0183 }
        r2 = r6.longValue();	 Catch:{ all -> 0x0183 }
        r1 = java.lang.Long.toString(r2);	 Catch:{ all -> 0x0183 }
        r0 = r0.append(r1);	 Catch:{ all -> 0x0183 }
        r0 = r0.toString();	 Catch:{ all -> 0x0183 }
        r1 = 0;	 Catch:{ all -> 0x0183 }
        r1 = new java.lang.String[r1];	 Catch:{ all -> 0x0183 }
        r11 = r8.rawQuery(r0, r1);	 Catch:{ all -> 0x0183 }
        r0 = DEBUG;	 Catch:{ all -> 0x0183 }
        if (r0 == 0) goto L_0x00fc;	 Catch:{ all -> 0x0183 }
    L_0x00f6:
        r0 = "Queried the smart dial table for contact names";	 Catch:{ all -> 0x0183 }
        r12.lap(r0);	 Catch:{ all -> 0x0183 }
    L_0x00fc:
        if (r11 == 0) goto L_0x010e;
    L_0x00fe:
        r15.insertNamePrefixes(r8, r11);	 Catch:{ all -> 0x0186 }
        r0 = DEBUG;	 Catch:{ all -> 0x0186 }
        if (r0 == 0) goto L_0x010b;	 Catch:{ all -> 0x0186 }
    L_0x0105:
        r0 = "Finished building the name prefix table";	 Catch:{ all -> 0x0186 }
        r12.lap(r0);	 Catch:{ all -> 0x0186 }
    L_0x010b:
        r11.close();	 Catch:{ all -> 0x0183 }
    L_0x010e:
        r0 = "CREATE INDEX IF NOT EXISTS smartdial_contact_id_index ON smartdial_table (contact_id);";	 Catch:{ all -> 0x0183 }
        r8.execSQL(r0);	 Catch:{ all -> 0x0183 }
        r0 = "CREATE INDEX IF NOT EXISTS smartdial_last_update_index ON smartdial_table (last_smartdial_update_time);";	 Catch:{ all -> 0x0183 }
        r8.execSQL(r0);	 Catch:{ all -> 0x0183 }
        r0 = "CREATE INDEX IF NOT EXISTS smartdial_sort_index ON smartdial_table (starred, is_super_primary, last_time_used, times_used, in_visible_group, display_name, contact_id, is_primary);";	 Catch:{ all -> 0x0183 }
        r8.execSQL(r0);	 Catch:{ all -> 0x0183 }
        r0 = "CREATE INDEX IF NOT EXISTS nameprefix_index ON prefix_table (prefix);";	 Catch:{ all -> 0x0183 }
        r8.execSQL(r0);	 Catch:{ all -> 0x0183 }
        r0 = "CREATE INDEX IF NOT EXISTS nameprefix_contact_id_index ON prefix_table (contact_id);";	 Catch:{ all -> 0x0183 }
        r8.execSQL(r0);	 Catch:{ all -> 0x0183 }
        r0 = DEBUG;	 Catch:{ all -> 0x0183 }
        if (r0 == 0) goto L_0x0136;	 Catch:{ all -> 0x0183 }
    L_0x0130:
        r0 = "DialerDatabaseHelperFinished recreating index";	 Catch:{ all -> 0x0183 }
        r12.lap(r0);	 Catch:{ all -> 0x0183 }
    L_0x0136:
        r0 = "ANALYZE smartdial_table";	 Catch:{ all -> 0x0183 }
        r8.execSQL(r0);	 Catch:{ all -> 0x0183 }
        r0 = "ANALYZE prefix_table";	 Catch:{ all -> 0x0183 }
        r8.execSQL(r0);	 Catch:{ all -> 0x0183 }
        r0 = "ANALYZE smartdial_contact_id_index";	 Catch:{ all -> 0x0183 }
        r8.execSQL(r0);	 Catch:{ all -> 0x0183 }
        r0 = "ANALYZE smartdial_last_update_index";	 Catch:{ all -> 0x0183 }
        r8.execSQL(r0);	 Catch:{ all -> 0x0183 }
        r0 = "ANALYZE nameprefix_index";	 Catch:{ all -> 0x0183 }
        r8.execSQL(r0);	 Catch:{ all -> 0x0183 }
        r0 = "ANALYZE nameprefix_contact_id_index";	 Catch:{ all -> 0x0183 }
        r8.execSQL(r0);	 Catch:{ all -> 0x0183 }
        r0 = DEBUG;	 Catch:{ all -> 0x0183 }
        if (r0 == 0) goto L_0x0165;	 Catch:{ all -> 0x0183 }
    L_0x015e:
        r0 = "DialerDatabaseHelperFinished updating index stats";	 Catch:{ all -> 0x0183 }
        r1 = 0;	 Catch:{ all -> 0x0183 }
        r12.stopAndLog(r0, r1);	 Catch:{ all -> 0x0183 }
    L_0x0165:
        r0 = sInUpdate;	 Catch:{ all -> 0x0183 }
        r1 = 0;	 Catch:{ all -> 0x0183 }
        r0.getAndSet(r1);	 Catch:{ all -> 0x0183 }
        r9 = r7.edit();	 Catch:{ all -> 0x0183 }
        r0 = "last_updated_millis";	 Catch:{ all -> 0x0183 }
        r2 = r6.longValue();	 Catch:{ all -> 0x0183 }
        r9.putLong(r0, r2);	 Catch:{ all -> 0x0183 }
        r9.commit();	 Catch:{ all -> 0x0183 }
        monitor-exit(r14);
        return;
    L_0x017e:
        r0 = move-exception;
        r13.close();	 Catch:{ all -> 0x0183 }
        throw r0;	 Catch:{ all -> 0x0183 }
    L_0x0183:
        r0 = move-exception;
        monitor-exit(r14);
        throw r0;
    L_0x0186:
        r0 = move-exception;
        r11.close();	 Catch:{ all -> 0x0183 }
        throw r0;	 Catch:{ all -> 0x0183 }
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.dialpad.database.DialerDatabaseHelper.updateSmartDialDatabase():void");
    }

    public ArrayList<ContactNumber> getLooseMatches(String query, SmartDialNameMatcher nameMatcher) {
        if (sInUpdate.get()) {
            return Lists.newArrayList();
        }
        SQLiteDatabase db = getReadableDatabase();
        String looseQuery = query + "%";
        ArrayList<ContactNumber> result = Lists.newArrayList();
        StopWatch start = DEBUG ? StopWatch.start(":Name Prefix query") : null;
        String currentTimeStamp = Long.toString(System.currentTimeMillis());
        Cursor cursor = db.rawQuery("SELECT data_id, display_name, sort_key, photo_id, photo_uri, data1, data2, data3, data10, data11, contact_id, lookup, times_contacted, company FROM smartdial_table WHERE contact_id IN  (SELECT contact_id FROM prefix_table WHERE prefix_table.prefix LIKE '" + looseQuery + "')" + " ORDER BY " + "smartdial_table.starred DESC, smartdial_table.is_super_primary DESC, (CASE WHEN ( ?1 - smartdial_table.last_time_used) < 259200000 THEN 0  WHEN ( ?1 - smartdial_table.last_time_used) < 2592000000 THEN 1  ELSE 2 END), smartdial_table.times_used DESC, smartdial_table.in_visible_group DESC, smartdial_table.display_name, smartdial_table.contact_id, smartdial_table.is_primary DESC", new String[]{currentTimeStamp});
        if (DEBUG) {
            start.lap("Prefix query completed");
        }
        if (DEBUG) {
            start.lap("Found column IDs");
        }
        Set<ContactMatch> duplicates = new HashSet();
        int counter = 0;
        try {
            if (DEBUG) {
                start.lap("Moved cursor to start");
            }
            while (cursor.moveToNext() && counter < 20) {
                long dataId = cursor.getLong(0);
                String displayName = cursor.getString(1);
                String sortKey = cursor.getString(2);
                long photoId = cursor.getLong(3);
                String photoUri = cursor.getString(4);
                String data1 = cursor.getString(5);
                String data2 = cursor.getString(6);
                String data3 = cursor.getString(7);
                String data10 = cursor.getString(8);
                String data11 = cursor.getString(9);
                long contactId = cursor.getLong(10);
                String lookup = cursor.getString(11);
                long timeContacted = cursor.getLong(12);
                String company = cursor.getString(13);
                ContactMatch contactMatch = new ContactMatch(lookup, contactId);
                if (!duplicates.contains(contactMatch)) {
                    boolean nameMatches = nameMatcher.matches(displayName);
                    boolean numberMatches = nameMatcher.matchesNumber(data1, query) != null;
                    if (nameMatches || numberMatches) {
                        duplicates.add(contactMatch);
                        result.add(new ContactNumber(contactId, dataId, displayName, sortKey, photoId, photoUri, data1, data2, data3, data10, data11, lookup, timeContacted, company));
                        counter++;
                        if (DEBUG) {
                            start.lap("Added one result");
                        }
                    }
                }
            }
            if (DEBUG) {
                start.stopAndLog("DialerDatabaseHelperFinished loading cursor", 0);
            }
            cursor.close();
            return result;
        } catch (Throwable th) {
            cursor.close();
        }
    }
}
