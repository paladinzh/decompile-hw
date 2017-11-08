package cn.com.xy.sms.sdk.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import cn.com.xy.sms.sdk.db.b;
import cn.com.xy.sms.sdk.net.NetUtil;
import java.util.Map;

/* compiled from: Unknown */
public class ContactsProvider extends ContentProvider {
    public static final String AUTHORITY = "cn.com.xy.sms.sdk.provider.contacts";
    public static final String URI = "content://cn.com.xy.sms.sdk.provider.contacts/contacts";
    private static final UriMatcher a;
    private static final int b = 1;
    private static final String c = "SELECT name FROM contacts WHERE phone = ? ";
    private b d;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        a = uriMatcher;
        uriMatcher.addURI(AUTHORITY, "contacts", 1);
    }

    public static void addContactsToDb(Context context, Map<String, Object> map) {
        NetUtil.executeRunnable(new a(map));
    }

    public int delete(Uri uri, String str, String[] strArr) {
        if (a.match(uri) == 1) {
            return this.d.getReadableDatabase().delete("contacts", str, strArr);
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        int i = 1;
        if (a.match(uri) == 1) {
            if (contentValues == null) {
                contentValues = new ContentValues();
            }
            contentValues.put("update_time", String.valueOf(System.currentTimeMillis()));
            String[] strArr = new String[]{contentValues.getAsString("phone")};
            SQLiteDatabase writableDatabase = this.d.getWritableDatabase();
            Cursor rawQuery = writableDatabase.rawQuery(c, strArr);
            boolean moveToFirst = rawQuery.moveToFirst();
            rawQuery.close();
            long insert = !moveToFirst ? writableDatabase.insert("contacts", null, contentValues) : (long) writableDatabase.update("contacts", contentValues, "phone = ? ", strArr);
            if (insert > 0) {
                i = 0;
            }
            if (i == 0) {
                Uri withAppendedId = ContentUris.withAppendedId(uri, insert);
                getContext().getContentResolver().notifyChange(withAppendedId, null);
                return withAppendedId;
            }
            throw new SQLException("Failed to insert row into" + uri);
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    public boolean onCreate() {
        this.d = new b(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        if (a.match(uri) == 1) {
            return this.d.getReadableDatabase().query("contacts", strArr, str, strArr2, null, null, str2);
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        return 0;
    }
}
