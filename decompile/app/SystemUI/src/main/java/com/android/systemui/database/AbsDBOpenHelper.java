package com.android.systemui.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import com.android.systemui.utils.HwLog;

public abstract class AbsDBOpenHelper extends SQLiteOpenHelper {
    public AbsDBOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public Cursor queryInner(String tableOrViewName, String[] projection, String selection, String[] selectionArgs) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db != null) {
                return db.query(tableOrViewName, projection, selection, selectionArgs, null, null, null);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            HwLog.e("AbsDBOpenHelper", "queryInner catch SQLException:" + ex.getMessage());
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e("AbsDBOpenHelper", "queryInner catch exception " + ex2.getMessage());
        }
        return null;
    }

    public int deleteInner(String tableName, String whereClause, String[] whereArgs) {
        SQLiteDatabase sQLiteDatabase = null;
        try {
            sQLiteDatabase = getWritableDatabase();
            if (sQLiteDatabase != null) {
                int delete = sQLiteDatabase.delete(tableName, whereClause, whereArgs);
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.close();
                }
                return delete;
            }
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
            return -1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            HwLog.e("AbsDBOpenHelper", "deleteInner catch SQLException:" + ex.getMessage());
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e("AbsDBOpenHelper", "deleteInner catch exception " + ex2.getMessage());
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        } catch (Throwable th) {
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        }
    }

    public int updateInner(String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase sQLiteDatabase = null;
        try {
            sQLiteDatabase = getWritableDatabase();
            if (sQLiteDatabase != null) {
                int update = sQLiteDatabase.update(tableName, values, whereClause, whereArgs);
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.close();
                }
                return update;
            }
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
            return -1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            HwLog.e("AbsDBOpenHelper", "deleteInner catch SQLException:" + ex.getMessage());
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e("AbsDBOpenHelper", "deleteInner catch exception " + ex2.getMessage());
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        } catch (Throwable th) {
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        }
    }

    public long insertInner(String tableName, ContentValues values) {
        SQLiteDatabase sQLiteDatabase = null;
        try {
            sQLiteDatabase = getWritableDatabase();
            if (sQLiteDatabase != null) {
                long insert = sQLiteDatabase.insert(tableName, null, values);
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.close();
                }
                return insert;
            }
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
            return -1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            HwLog.e("AbsDBOpenHelper", "insertInner catch SQLException:" + ex.getMessage());
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        } catch (Exception ex2) {
            ex2.printStackTrace();
            HwLog.e("AbsDBOpenHelper", "insertInner catch exception " + ex2.getMessage());
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        } catch (Throwable th) {
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
        }
    }
}
