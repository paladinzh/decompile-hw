package cn.com.xy.sms.sdk.db;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import cn.com.xy.sms.sdk.db.a.a;

/* compiled from: Unknown */
public class XyCursor {
    private SQLiteDatabase a = null;
    private Cursor b = null;
    private int c = 0;

    public XyCursor(SQLiteDatabase sQLiteDatabase, Cursor cursor, int i) {
        this.a = sQLiteDatabase;
        this.b = cursor;
        this.c = i;
    }

    private void a(boolean z) {
        try {
            if (this.b != null) {
                this.b.close();
            }
        } catch (Throwable th) {
        }
        if (z) {
            try {
                if (this.a != null) {
                    if (this.c == 0) {
                        DBManager.close(this.a);
                    } else if (this.c == 1) {
                        a.a(this.a);
                    } else if (this.c == 2) {
                        c.a(this.a);
                    } else if (this.c != 3) {
                        throw new Exception("unknown type:" + this.c);
                    } else {
                        e.a(this.a);
                    }
                    this.a = null;
                }
            } catch (Throwable th2) {
            }
        }
    }

    public static void closeCursor(XyCursor xyCursor, boolean z) {
        if (xyCursor != null) {
            try {
                if (xyCursor.b != null) {
                    xyCursor.b.close();
                }
            } catch (Throwable th) {
            }
            if (z) {
                try {
                    if (xyCursor.a != null) {
                        if (xyCursor.c == 0) {
                            DBManager.close(xyCursor.a);
                        } else if (xyCursor.c == 1) {
                            a.a(xyCursor.a);
                        } else if (xyCursor.c == 2) {
                            c.a(xyCursor.a);
                        } else if (xyCursor.c != 3) {
                            throw new Exception("unknown type:" + xyCursor.c);
                        } else {
                            e.a(xyCursor.a);
                        }
                        xyCursor.a = null;
                    }
                } catch (Throwable th2) {
                }
            }
        }
    }

    public void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {
        if (this.b != null) {
            this.b.copyStringToBuffer(i, charArrayBuffer);
        }
    }

    public void deactivate() {
        if (this.b != null) {
            this.b.deactivate();
        }
    }

    public byte[] getBlob(int i) {
        return this.b == null ? null : this.b.getBlob(i);
    }

    public int getColumnCount() {
        return this.b == null ? 0 : this.b.getColumnCount();
    }

    public int getColumnIndex(String str) {
        return this.b == null ? -1 : this.b.getColumnIndex(str);
    }

    public int getColumnIndexOrThrow(String str) {
        return this.b == null ? -1 : this.b.getColumnIndexOrThrow(str);
    }

    public String getColumnName(int i) {
        return this.b == null ? null : this.b.getColumnName(i);
    }

    public String[] getColumnNames() {
        return this.b == null ? null : this.b.getColumnNames();
    }

    public int getCount() {
        return this.b == null ? 0 : this.b.getCount();
    }

    public Cursor getCur() {
        return this.b;
    }

    public double getDouble(int i) {
        return this.b == null ? 0.0d : this.b.getDouble(i);
    }

    public Bundle getExtras() {
        return this.b == null ? null : this.b.getExtras();
    }

    public float getFloat(int i) {
        return this.b == null ? 0.0f : this.b.getFloat(i);
    }

    public int getInt(int i) {
        return this.b == null ? 0 : this.b.getInt(i);
    }

    public long getLong(int i) {
        return this.b == null ? 0 : this.b.getLong(i);
    }

    public SQLiteDatabase getMySQLiteDatabase() {
        return this.a;
    }

    public int getPosition() {
        return this.b == null ? -1 : this.b.getPosition();
    }

    public short getShort(int i) {
        return this.b == null ? (short) 0 : this.b.getShort(i);
    }

    public String getString(int i) {
        return this.b == null ? null : this.b.getString(i);
    }

    public boolean getWantsAllOnMoveCalls() {
        return this.b == null ? true : this.b.getWantsAllOnMoveCalls();
    }

    public boolean isAfterLast() {
        return this.b == null ? true : this.b.isAfterLast();
    }

    public boolean isBeforeFirst() {
        return this.b == null ? true : this.b.isBeforeFirst();
    }

    public boolean isClosed() {
        return this.b == null ? true : this.b.isClosed();
    }

    public boolean isFirst() {
        return this.b == null ? false : this.b.isFirst();
    }

    public boolean isLast() {
        return this.b == null ? false : this.b.isLast();
    }

    public boolean isNull(int i) {
        return this.b == null ? false : this.b.isNull(i);
    }

    public boolean move(int i) {
        return this.b == null ? false : this.b.move(i);
    }

    public boolean moveToFirst() {
        return this.b == null ? false : this.b.moveToFirst();
    }

    public boolean moveToLast() {
        return this.b == null ? false : this.b.moveToLast();
    }

    public boolean moveToNext() {
        return this.b == null ? false : this.b.moveToNext();
    }

    public boolean moveToPosition(int i) {
        return this.b == null ? false : this.b.moveToPosition(i);
    }

    public boolean moveToPrevious() {
        return this.b == null ? false : this.b.moveToPrevious();
    }

    public void registerContentObserver(ContentObserver contentObserver) {
        if (this.b != null) {
            this.b.registerContentObserver(contentObserver);
        }
    }

    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        if (this.b != null) {
            this.b.registerDataSetObserver(dataSetObserver);
        }
    }

    public boolean requery() {
        return this.b == null ? false : this.b.requery();
    }

    public Bundle respond(Bundle bundle) {
        return this.b == null ? null : this.b.respond(bundle);
    }

    public void setCur(Cursor cursor) {
        this.b = cursor;
    }

    public void setMySQLiteDatabase(SQLiteDatabase sQLiteDatabase) {
        this.a = sQLiteDatabase;
    }

    public void setNotificationUri(ContentResolver contentResolver, Uri uri) {
        if (this.b != null) {
            this.b.setNotificationUri(contentResolver, uri);
        }
    }

    public void unregisterContentObserver(ContentObserver contentObserver) {
        if (this.b != null) {
            this.b.unregisterContentObserver(contentObserver);
        }
    }

    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        if (this.b != null) {
            this.b.unregisterDataSetObserver(dataSetObserver);
        }
    }
}
