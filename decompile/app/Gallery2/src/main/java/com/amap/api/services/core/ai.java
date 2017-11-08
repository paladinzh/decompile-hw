package com.amap.api.services.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

/* compiled from: DBOperation */
public class ai {
    private ah a;
    private SQLiteDatabase b;

    public ai(Context context) {
        this.a = new ah(context, "logdb.db", null, 1);
    }

    private SQLiteDatabase a() {
        this.b = this.a.getReadableDatabase();
        return this.b;
    }

    private SQLiteDatabase b() {
        this.b = this.a.getWritableDatabase();
        return this.b;
    }

    public <T> void a(String str, ap<T> apVar) {
        if (apVar.a() != null && str != null) {
            if (this.b == null || this.b.isReadOnly()) {
                this.b = b();
            }
            if (this.b != null) {
                try {
                    this.b.delete(apVar.a(), str, null);
                    if (this.b != null) {
                        this.b.close();
                        this.b = null;
                    }
                } catch (Throwable th) {
                    if (this.b != null) {
                        this.b.close();
                        this.b = null;
                    }
                }
            }
        }
    }

    public <T> void b(String str, ap<T> apVar) {
        if (apVar != null && str != null && apVar.a() != null) {
            ContentValues b = apVar.b();
            if (b != null) {
                if (this.b == null || this.b.isReadOnly()) {
                    this.b = b();
                }
                if (this.b != null) {
                    try {
                        this.b.update(apVar.a(), b, str, null);
                        if (this.b != null) {
                            this.b.close();
                            this.b = null;
                        }
                    } catch (Throwable th) {
                        if (this.b != null) {
                            this.b.close();
                            this.b = null;
                        }
                    }
                }
            }
        }
    }

    public <T> void a(ap<T> apVar) {
        if (apVar != null) {
            ContentValues b = apVar.b();
            if (b != null && apVar.a() != null) {
                if (this.b == null || this.b.isReadOnly()) {
                    this.b = b();
                }
                if (this.b != null) {
                    try {
                        this.b.insert(apVar.a(), null, b);
                        if (this.b != null) {
                            this.b.close();
                            this.b = null;
                        }
                    } catch (Throwable th) {
                        if (this.b != null) {
                            this.b.close();
                            this.b = null;
                        }
                    }
                }
            }
        }
    }

    public <T> List<T> c(String str, ap<T> apVar) {
        Throwable th;
        List<T> arrayList = new ArrayList();
        if (this.b == null) {
            this.b = a();
        }
        if (this.b == null || apVar.a() == null || str == null) {
            return arrayList;
        }
        Cursor query;
        try {
            query = this.b.query(apVar.a(), null, str, null, null, null, null);
            if (query != null) {
                while (query.moveToNext()) {
                    try {
                        arrayList.add(apVar.b(query));
                    } catch (Throwable th2) {
                        th = th2;
                    }
                }
                if (query != null) {
                    try {
                        query.close();
                    } catch (Throwable th3) {
                        ay.a(th3, "DataBase", "searchListData");
                        th3.printStackTrace();
                    }
                }
                try {
                    if (this.b != null) {
                        this.b.close();
                        this.b = null;
                    }
                } catch (Throwable th32) {
                    ay.a(th32, "DataBase", "searchListData");
                    th32.printStackTrace();
                }
                return arrayList;
            }
            this.b.close();
            this.b = null;
            if (query != null) {
                try {
                    query.close();
                } catch (Throwable th322) {
                    ay.a(th322, "DataBase", "searchListData");
                    th322.printStackTrace();
                }
            }
            try {
                if (this.b != null) {
                    this.b.close();
                    this.b = null;
                }
            } catch (Throwable th3222) {
                ay.a(th3222, "DataBase", "searchListData");
                th3222.printStackTrace();
            }
            return arrayList;
        } catch (Throwable th4) {
            th3222 = th4;
            query = null;
            if (query != null) {
                query.close();
            }
            if (this.b != null) {
                this.b.close();
                this.b = null;
            }
            throw th3222;
        }
    }
}
