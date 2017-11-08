package com.amap.api.services.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* compiled from: DBOperation */
public class bj {
    private bn a;
    private SQLiteDatabase b;
    private bi c;

    public bj(Context context, bi biVar) {
        try {
            this.a = new bn(context, biVar.a(), null, biVar.b(), biVar);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        this.c = biVar;
    }

    private SQLiteDatabase a(boolean z) {
        try {
            this.b = this.a.getReadableDatabase();
        } catch (Throwable th) {
            if (z) {
                th.printStackTrace();
            } else {
                ay.a(th, "DBOperation", "getReadAbleDataBase");
            }
        }
        return this.b;
    }

    private SQLiteDatabase b(boolean z) {
        try {
            this.b = this.a.getWritableDatabase();
        } catch (Throwable th) {
            ay.a(th, "DBOperation", "getReadAbleDataBase");
        }
        return this.b;
    }

    public static String a(Map<String, String> map) {
        if (map == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        Object obj = 1;
        for (String str : map.keySet()) {
            Object obj2;
            if (obj == null) {
                stringBuilder.append(" and ").append(str).append(" = '").append((String) map.get(str)).append("'");
                obj2 = obj;
            } else {
                stringBuilder.append(str).append(" = '").append((String) map.get(str)).append("'");
                obj2 = null;
            }
            obj = obj2;
        }
        return stringBuilder.toString();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public <T> void a(String str, bk<T> bkVar) {
        synchronized (this.c) {
            if (!(bkVar.b() == null || str == null)) {
                if (this.b == null || this.b.isReadOnly()) {
                    this.b = b(false);
                }
                if (this.b != null) {
                    try {
                        this.b.delete(bkVar.b(), str, null);
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

    public <T> void b(String str, bk<T> bkVar) {
        a(str, bkVar, false);
    }

    public <T> void a(String str, bk<T> bkVar, boolean z) {
        synchronized (this.c) {
            if (bkVar == null || str == null || bkVar.b() == null) {
                return;
            }
            ContentValues a = bkVar.a();
            if (a != null) {
                if (this.b == null || this.b.isReadOnly()) {
                    this.b = b(z);
                }
                if (this.b != null) {
                    try {
                        this.b.update(bkVar.b(), a, str, null);
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
                } else {
                    return;
                }
            }
        }
    }

    public <T> void a(bk<T> bkVar) {
        a((bk) bkVar, false);
    }

    public <T> void a(bk<T> bkVar, boolean z) {
        synchronized (this.c) {
            if (this.b == null || this.b.isReadOnly()) {
                this.b = b(z);
            }
            if (this.b != null) {
                try {
                    a(this.b, (bk) bkVar);
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

    private <T> void a(SQLiteDatabase sQLiteDatabase, bk<T> bkVar) {
        if (bkVar != null && sQLiteDatabase != null) {
            ContentValues a = bkVar.a();
            if (a != null && bkVar.b() != null) {
                sQLiteDatabase.insert(bkVar.b(), null, a);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public <T> List<T> b(String str, bk<T> bkVar, boolean z) {
        synchronized (this.c) {
            List<T> arrayList = new ArrayList();
            if (this.b == null) {
                this.b = a(z);
            }
            if (this.b != null) {
                if (!(bkVar.b() == null || str == null)) {
                    Cursor query;
                    try {
                        query = this.b.query(bkVar.b(), null, str, null, null, null, null);
                        if (query != null) {
                            while (query.moveToNext()) {
                                try {
                                    arrayList.add(bkVar.a(query));
                                } catch (Throwable th) {
                                    th = th;
                                }
                            }
                            if (query != null) {
                                try {
                                    query.close();
                                } catch (Throwable th2) {
                                    if (!z) {
                                        ay.a(th2, "DataBase", "searchListData");
                                    }
                                }
                            }
                            try {
                                if (this.b != null) {
                                    this.b.close();
                                    this.b = null;
                                }
                            } catch (Throwable th22) {
                                if (!z) {
                                    ay.a(th22, "DataBase", "searchListData");
                                }
                            }
                            return arrayList;
                        }
                        this.b.close();
                        this.b = null;
                        if (query != null) {
                            try {
                                query.close();
                            } catch (Throwable th222) {
                                if (!z) {
                                    Throwable th2222;
                                    ay.a(th2222, "DataBase", "searchListData");
                                }
                            }
                        }
                        try {
                            if (this.b != null) {
                                this.b.close();
                                this.b = null;
                            }
                        } catch (Throwable th22222) {
                            if (!z) {
                                ay.a(th22222, "DataBase", "searchListData");
                            }
                        }
                        return arrayList;
                    } catch (Throwable th3) {
                        th22222 = th3;
                        query = null;
                        if (query != null) {
                            query.close();
                        }
                        if (this.b != null) {
                            this.b.close();
                            this.b = null;
                        }
                        throw th22222;
                    }
                }
            }
        }
    }

    public <T> List<T> c(String str, bk<T> bkVar) {
        return b(str, bkVar, false);
    }
}
