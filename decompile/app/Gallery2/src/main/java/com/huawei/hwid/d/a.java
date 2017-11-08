package com.huawei.hwid.d;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.huawei.hwid.core.d.b.e;

public class a extends SQLiteOpenHelper {
    public a(Context context) {
        super(context, "vipdatabase.db", null, 1);
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("Create table vip_config( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, value TEXT);");
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        e.a("VIPDatabase", "onUpgradeoldVersion=" + i + "newVersion=" + i2);
    }

    public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        e.a("VIPDatabase", "onDowngradeoldVersion=" + i + "newVersion=" + i2);
    }

    public static boolean a(Context context, String str, String str2) {
        Cursor cursor;
        Throwable th;
        boolean z = true;
        SQLiteDatabase sQLiteDatabase = null;
        Cursor query;
        try {
            SQLiteDatabase writableDatabase = new a(context).getWritableDatabase();
            try {
                query = writableDatabase.query("vip_config", new String[]{"value"}, "name = ?", new String[]{str}, null, null, null);
            } catch (Throwable e) {
                Throwable th2 = e;
                cursor = null;
                sQLiteDatabase = writableDatabase;
                th = th2;
                try {
                    e.d("VIPDatabase", th.getMessage(), th);
                    if (cursor != null) {
                        cursor.close();
                    }
                    if (sQLiteDatabase != null) {
                        sQLiteDatabase.close();
                    }
                    return false;
                } catch (Throwable th3) {
                    th = th3;
                    query = cursor;
                    if (query != null) {
                        query.close();
                    }
                    if (sQLiteDatabase != null) {
                        sQLiteDatabase.close();
                    }
                    throw th;
                }
            } catch (Throwable e2) {
                query = null;
                sQLiteDatabase = writableDatabase;
                th = e2;
                if (query != null) {
                    query.close();
                }
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.close();
                }
                throw th;
            }
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", str);
                contentValues.put("value", str2);
                if (query != null && query.moveToNext()) {
                    boolean z2 = writableDatabase.update("vip_config", contentValues, "name = ?", new String[]{str}) > 0;
                    if (query != null) {
                        query.close();
                    }
                    if (writableDatabase != null) {
                        writableDatabase.close();
                    }
                    return z2;
                }
                if (writableDatabase.insert("vip_config", "", contentValues) <= 0) {
                    z = false;
                }
                if (query != null) {
                    query.close();
                }
                if (writableDatabase != null) {
                    writableDatabase.close();
                }
                return z;
            } catch (Throwable e22) {
                sQLiteDatabase = writableDatabase;
                th = e22;
                cursor = query;
                e.d("VIPDatabase", th.getMessage(), th);
                if (cursor != null) {
                    cursor.close();
                }
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.close();
                }
                return false;
            } catch (Throwable e222) {
                sQLiteDatabase = writableDatabase;
                th = e222;
                if (query != null) {
                    query.close();
                }
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.close();
                }
                throw th;
            }
        } catch (Exception e3) {
            th = e3;
            cursor = null;
            e.d("VIPDatabase", th.getMessage(), th);
            if (cursor != null) {
                cursor.close();
            }
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
            return false;
        } catch (Throwable th4) {
            th = th4;
            query = null;
            if (query != null) {
                query.close();
            }
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
            throw th;
        }
    }

    public static String a(Context context, String str) {
        Cursor cursor;
        SQLiteDatabase sQLiteDatabase;
        Throwable th;
        SQLiteDatabase sQLiteDatabase2 = null;
        Cursor query;
        try {
            SQLiteDatabase writableDatabase = new a(context).getWritableDatabase();
            try {
                query = writableDatabase.query("vip_config", new String[]{"value"}, "name = ?", new String[]{str}, null, null, null);
                if (query != null && query.moveToNext()) {
                    String string = query.getString(0);
                    if (query != null) {
                        query.close();
                    }
                    if (writableDatabase != null) {
                        writableDatabase.close();
                    }
                    return string;
                }
                try {
                    e.b("VIPDatabase", str + " is not exist in " + "vip_config");
                    if (query != null) {
                        query.close();
                    }
                    if (writableDatabase != null) {
                        writableDatabase.close();
                    }
                    return null;
                } catch (Throwable e) {
                    Throwable th2 = e;
                    cursor = query;
                    sQLiteDatabase = writableDatabase;
                    th = th2;
                    try {
                        e.d("VIPDatabase", th.getMessage(), th);
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (sQLiteDatabase != null) {
                            sQLiteDatabase.close();
                        }
                        return null;
                    } catch (Throwable th3) {
                        th = th3;
                        sQLiteDatabase2 = sQLiteDatabase;
                        query = cursor;
                        if (query != null) {
                            query.close();
                        }
                        if (sQLiteDatabase2 != null) {
                            sQLiteDatabase2.close();
                        }
                        throw th;
                    }
                } catch (Throwable e2) {
                    sQLiteDatabase2 = writableDatabase;
                    th = e2;
                    if (query != null) {
                        query.close();
                    }
                    if (sQLiteDatabase2 != null) {
                        sQLiteDatabase2.close();
                    }
                    throw th;
                }
            } catch (Throwable e22) {
                sQLiteDatabase = writableDatabase;
                th = e22;
                cursor = null;
                e.d("VIPDatabase", th.getMessage(), th);
                if (cursor != null) {
                    cursor.close();
                }
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.close();
                }
                return null;
            } catch (Throwable e222) {
                query = null;
                sQLiteDatabase2 = writableDatabase;
                th = e222;
                if (query != null) {
                    query.close();
                }
                if (sQLiteDatabase2 != null) {
                    sQLiteDatabase2.close();
                }
                throw th;
            }
        } catch (Exception e3) {
            th = e3;
            cursor = null;
            sQLiteDatabase = null;
            e.d("VIPDatabase", th.getMessage(), th);
            if (cursor != null) {
                cursor.close();
            }
            if (sQLiteDatabase != null) {
                sQLiteDatabase.close();
            }
            return null;
        } catch (Throwable th4) {
            th = th4;
            query = null;
            if (query != null) {
                query.close();
            }
            if (sQLiteDatabase2 != null) {
                sQLiteDatabase2.close();
            }
            throw th;
        }
    }
}
