package com.huawei.openalliance.ad.a.d;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.text.TextUtils;
import com.huawei.openalliance.ad.utils.b;
import com.huawei.openalliance.ad.utils.b.d;
import com.huawei.openalliance.ad.utils.db.a;
import com.huawei.openalliance.ad.utils.db.bean.MaterialRecord;
import com.huawei.openalliance.ad.utils.h;
import com.huawei.openalliance.ad.utils.j;
import com.huawei.openalliance.ad.utils.k;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class e {
    public static com.huawei.openalliance.ad.a.a.b.e a(Context context, String str) {
        Cursor a;
        Throwable th;
        Cursor cursor = null;
        if (context == null || TextUtils.isEmpty(str)) {
            return null;
        }
        a a2 = a.a(context);
        long currentTimeMillis = System.currentTimeMillis();
        try {
            a2.a(MaterialRecord.class.getSimpleName(), "materialId = ? and validTime < ?", new String[]{str, String.valueOf(currentTimeMillis)});
            a = a2.a(MaterialRecord.class.getSimpleName(), null, "materialId = ? and validTime >= ?", new String[]{str, String.valueOf(currentTimeMillis)}, null);
            if (a != null) {
                try {
                    if (a.getCount() > 0) {
                        a.moveToFirst();
                        MaterialRecord materialRecord = new MaterialRecord();
                        materialRecord.a(a);
                        com.huawei.openalliance.ad.a.a.b.e a3 = com.huawei.openalliance.ad.a.e.e.a(materialRecord);
                        if (a != null) {
                            a.close();
                        }
                        a2.close();
                        return a3;
                    }
                } catch (Exception e) {
                    try {
                        d.c("MaterialManager", "get material fail");
                        if (a != null) {
                            a.close();
                        }
                        a2.close();
                        return null;
                    } catch (Throwable th2) {
                        cursor = a;
                        th = th2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        a2.close();
                        throw th;
                    }
                }
            }
            if (a != null) {
                a.close();
            }
            a2.close();
            return null;
        } catch (Exception e2) {
            a = null;
            d.c("MaterialManager", "get material fail");
            if (a != null) {
                a.close();
            }
            a2.close();
            return null;
        } catch (Throwable th3) {
            th = th3;
            if (cursor != null) {
                cursor.close();
            }
            a2.close();
            throw th;
        }
    }

    public static void a(Context context, String str, int i, com.huawei.openalliance.ad.a.a.b.e eVar, boolean z, Handler handler, boolean z2) {
        Object obj;
        ContentValues q;
        String a;
        Throwable th;
        if (context != null && eVar != null && !j.a(eVar.getContentid__())) {
            String simpleName = MaterialRecord.class.getSimpleName();
            MaterialRecord a2 = com.huawei.openalliance.ad.a.e.e.a(str, i, eVar, z, z2);
            if (a2 != null) {
                a a3 = a.a(context);
                Cursor a4;
                try {
                    a4 = a3.a(simpleName, new String[]{"htmlStr"}, "materialId = ?", new String[]{a2.d()}, "updateTime desc");
                    if (a4 != null) {
                        try {
                            if (a4.getCount() > 0) {
                                obj = 1;
                                try {
                                    a4.moveToFirst();
                                    String a5 = b.a(a4.getString(a4.getColumnIndex("htmlStr")));
                                    if (a5 != null) {
                                        File file = new File(a5);
                                        if (file.exists()) {
                                            if (file.length() == 0) {
                                            }
                                        }
                                        obj = null;
                                        b.a(file);
                                        a3.a(simpleName, "materialId = ?", new String[]{a2.d()});
                                    }
                                    if (a4 != null) {
                                        a4.close();
                                    }
                                } catch (Exception e) {
                                    try {
                                        d.c("MaterialManager", "query or delete material fail");
                                        if (a4 != null) {
                                            a4.close();
                                        }
                                        if (obj != null) {
                                            d.b("MaterialManager", "show content and has material");
                                            q = a2.q();
                                            q.remove("displayCount");
                                            q.remove("displayDate");
                                            q.remove("htmlStr");
                                            a3.a(simpleName, q, "materialId = ?", new String[]{a2.d()});
                                            if (handler != null) {
                                                handler.sendEmptyMessage(1002);
                                            }
                                            a(context, z, z2, i, simpleName);
                                            a3.close();
                                        }
                                        if (obj != null) {
                                            d.b("MaterialManager", "pre content and has material");
                                            q = new ContentValues();
                                            q.put("updateTime", Long.valueOf(a2.f()));
                                            a3.a(simpleName, q, "materialId = ?", new String[]{a2.d()});
                                            a(context, z, z2, i, simpleName);
                                            a3.close();
                                        }
                                        d.b("MaterialManager", "not has material, begin to down pic");
                                        if (1 == i) {
                                            a = j.a(a2.i(), "img", "src");
                                            if (!TextUtils.isEmpty(a)) {
                                                new com.huawei.openalliance.ad.utils.c.a.b(context, new com.huawei.openalliance.ad.utils.c.a.a(a, com.huawei.openalliance.ad.a.g.d.a(context, a2.k()), a2.a(), a2.b()), new f(a2, a, handler)).executeOnExecutor(k.a, new Void[0]);
                                            }
                                        } else if (2 == i) {
                                            a3.a(MaterialRecord.class.getSimpleName(), a2.q());
                                        }
                                        a(context, z, z2, i, simpleName);
                                        a3.close();
                                    } catch (Throwable th2) {
                                        th = th2;
                                        if (a4 != null) {
                                            a4.close();
                                        }
                                        throw th;
                                    }
                                }
                                if (obj != null && !z2) {
                                    d.b("MaterialManager", "show content and has material");
                                    q = a2.q();
                                    q.remove("displayCount");
                                    q.remove("displayDate");
                                    q.remove("htmlStr");
                                    a3.a(simpleName, q, "materialId = ?", new String[]{a2.d()});
                                    if (handler != null) {
                                        handler.sendEmptyMessage(1002);
                                    }
                                } else if (obj != null && z2) {
                                    d.b("MaterialManager", "pre content and has material");
                                    q = new ContentValues();
                                    q.put("updateTime", Long.valueOf(a2.f()));
                                    a3.a(simpleName, q, "materialId = ?", new String[]{a2.d()});
                                } else {
                                    d.b("MaterialManager", "not has material, begin to down pic");
                                    if (1 == i) {
                                        a = j.a(a2.i(), "img", "src");
                                        if (TextUtils.isEmpty(a)) {
                                            new com.huawei.openalliance.ad.utils.c.a.b(context, new com.huawei.openalliance.ad.utils.c.a.a(a, com.huawei.openalliance.ad.a.g.d.a(context, a2.k()), a2.a(), a2.b()), new f(a2, a, handler)).executeOnExecutor(k.a, new Void[0]);
                                        }
                                    } else if (2 == i) {
                                        a3.a(MaterialRecord.class.getSimpleName(), a2.q());
                                    }
                                }
                                a(context, z, z2, i, simpleName);
                                a3.close();
                            }
                        } catch (Exception e2) {
                            obj = null;
                            d.c("MaterialManager", "query or delete material fail");
                            if (a4 != null) {
                                a4.close();
                            }
                            if (obj != null) {
                                d.b("MaterialManager", "show content and has material");
                                q = a2.q();
                                q.remove("displayCount");
                                q.remove("displayDate");
                                q.remove("htmlStr");
                                a3.a(simpleName, q, "materialId = ?", new String[]{a2.d()});
                                if (handler != null) {
                                    handler.sendEmptyMessage(1002);
                                }
                                a(context, z, z2, i, simpleName);
                                a3.close();
                            }
                            if (obj != null) {
                                d.b("MaterialManager", "pre content and has material");
                                q = new ContentValues();
                                q.put("updateTime", Long.valueOf(a2.f()));
                                a3.a(simpleName, q, "materialId = ?", new String[]{a2.d()});
                                a(context, z, z2, i, simpleName);
                                a3.close();
                            }
                            d.b("MaterialManager", "not has material, begin to down pic");
                            if (1 == i) {
                                a = j.a(a2.i(), "img", "src");
                                if (TextUtils.isEmpty(a)) {
                                    new com.huawei.openalliance.ad.utils.c.a.b(context, new com.huawei.openalliance.ad.utils.c.a.a(a, com.huawei.openalliance.ad.a.g.d.a(context, a2.k()), a2.a(), a2.b()), new f(a2, a, handler)).executeOnExecutor(k.a, new Void[0]);
                                }
                            } else if (2 == i) {
                                a3.a(MaterialRecord.class.getSimpleName(), a2.q());
                            }
                            a(context, z, z2, i, simpleName);
                            a3.close();
                        }
                    }
                    obj = null;
                    if (a4 != null) {
                        a4.close();
                    }
                } catch (Exception e3) {
                    a4 = null;
                    obj = null;
                    d.c("MaterialManager", "query or delete material fail");
                    if (a4 != null) {
                        a4.close();
                    }
                    if (obj != null) {
                        d.b("MaterialManager", "show content and has material");
                        q = a2.q();
                        q.remove("displayCount");
                        q.remove("displayDate");
                        q.remove("htmlStr");
                        a3.a(simpleName, q, "materialId = ?", new String[]{a2.d()});
                        if (handler != null) {
                            handler.sendEmptyMessage(1002);
                        }
                        a(context, z, z2, i, simpleName);
                        a3.close();
                    }
                    if (obj != null) {
                        d.b("MaterialManager", "pre content and has material");
                        q = new ContentValues();
                        q.put("updateTime", Long.valueOf(a2.f()));
                        a3.a(simpleName, q, "materialId = ?", new String[]{a2.d()});
                        a(context, z, z2, i, simpleName);
                        a3.close();
                    }
                    d.b("MaterialManager", "not has material, begin to down pic");
                    if (1 == i) {
                        a = j.a(a2.i(), "img", "src");
                        if (TextUtils.isEmpty(a)) {
                            new com.huawei.openalliance.ad.utils.c.a.b(context, new com.huawei.openalliance.ad.utils.c.a.a(a, com.huawei.openalliance.ad.a.g.d.a(context, a2.k()), a2.a(), a2.b()), new f(a2, a, handler)).executeOnExecutor(k.a, new Void[0]);
                        }
                    } else if (2 == i) {
                        a3.a(MaterialRecord.class.getSimpleName(), a2.q());
                    }
                    a(context, z, z2, i, simpleName);
                    a3.close();
                } catch (Throwable th3) {
                    th = th3;
                    a4 = null;
                    if (a4 != null) {
                        a4.close();
                    }
                    throw th;
                }
                if (obj != null) {
                    d.b("MaterialManager", "show content and has material");
                    q = a2.q();
                    q.remove("displayCount");
                    q.remove("displayDate");
                    q.remove("htmlStr");
                    a3.a(simpleName, q, "materialId = ?", new String[]{a2.d()});
                    if (handler != null) {
                        handler.sendEmptyMessage(1002);
                    }
                    a(context, z, z2, i, simpleName);
                    a3.close();
                }
                if (obj != null) {
                    d.b("MaterialManager", "pre content and has material");
                    q = new ContentValues();
                    q.put("updateTime", Long.valueOf(a2.f()));
                    a3.a(simpleName, q, "materialId = ?", new String[]{a2.d()});
                    a(context, z, z2, i, simpleName);
                    a3.close();
                }
                try {
                    d.b("MaterialManager", "not has material, begin to down pic");
                    if (1 == i) {
                        a = j.a(a2.i(), "img", "src");
                        if (TextUtils.isEmpty(a)) {
                            new com.huawei.openalliance.ad.utils.c.a.b(context, new com.huawei.openalliance.ad.utils.c.a.a(a, com.huawei.openalliance.ad.a.g.d.a(context, a2.k()), a2.a(), a2.b()), new f(a2, a, handler)).executeOnExecutor(k.a, new Void[0]);
                        }
                    } else if (2 == i) {
                        a3.a(MaterialRecord.class.getSimpleName(), a2.q());
                    }
                    a(context, z, z2, i, simpleName);
                    a3.close();
                } catch (Exception e4) {
                    d.c("MaterialManager", "handle and update material fail");
                } finally {
                    a3.close();
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void a(Context context, List<String> list) {
        Cursor cursor = null;
        if (context != null && list != null && !list.isEmpty()) {
            a a = a.a(context);
            try {
                cursor = a.b(MaterialRecord.class.getSimpleName(), "adType = 1 and materialId", list);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        while (true) {
                            String a2 = b.a(cursor.getString(cursor.getColumnIndex("htmlStr")));
                            if (a2 != null) {
                                File file = new File(a2);
                                if (file.exists()) {
                                    b.a(file);
                                }
                            }
                            if (cursor.moveToNext()) {
                            }
                        }
                        a.a(MaterialRecord.class.getSimpleName(), "materialId", (List) list);
                        if (cursor != null) {
                            cursor.close();
                        }
                        a.close();
                    }
                }
                a.a(MaterialRecord.class.getSimpleName(), "materialId", (List) list);
                if (cursor != null) {
                    cursor.close();
                }
                a.close();
            } catch (Exception e) {
                d.c("MaterialManager", "delete material fail");
                if (cursor != null) {
                    cursor.close();
                }
                a.close();
            } catch (Throwable th) {
                Throwable th2 = th;
                Cursor cursor2 = cursor;
                Throwable th3 = th2;
                if (cursor2 != null) {
                    cursor2.close();
                }
                a.close();
                throw th3;
            }
        }
    }

    private static void a(Context context, boolean z, boolean z2, int i, String str) {
        Cursor cursor;
        Throwable th;
        Throwable th2;
        a aVar = null;
        if (1 == i) {
            int i2;
            if (z) {
                i2 = 3;
            } else {
                i2 = !z2 ? h.a(context).a() : h.a(context).e();
            }
            String str2 = "adType = 1 and isSplashPreContent <> 0";
            if (z2) {
                str2 = "adType = 1 and isSplashPreContent = 0";
            }
            Cursor a;
            try {
                a a2 = a.a(context);
                try {
                    a = a2.a(str, new String[]{"htmlStr", "materialId"}, str2, null, "updateTime asc");
                    if (a != null) {
                        try {
                            int count = a.getCount();
                            if (count > i2) {
                                a.moveToFirst();
                                List arrayList = new ArrayList(4);
                                while (true) {
                                    count--;
                                    String a3 = b.a(a.getString(a.getColumnIndex("htmlStr")));
                                    if (a3 != null) {
                                        b.a(new File(a3));
                                    }
                                    arrayList.add(a.getString(a.getColumnIndex("materialId")));
                                    if (count > i2) {
                                        if (!a.moveToNext()) {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                                a2.a(str, "materialId", arrayList);
                            }
                        } catch (Exception e) {
                            aVar = a2;
                            cursor = a;
                        } catch (Throwable th3) {
                            aVar = a2;
                            th = th3;
                        }
                    }
                    if (a != null) {
                        a.close();
                    }
                    if (a2 != null) {
                        a2.close();
                    }
                } catch (Exception e2) {
                    aVar = a2;
                    Object obj = null;
                    try {
                        d.c("MaterialManager", "delete overtime material fail");
                        if (cursor != null) {
                            cursor.close();
                        }
                        if (aVar != null) {
                            aVar.close();
                        }
                    } catch (Throwable th4) {
                        th2 = th4;
                        a = cursor;
                        th = th2;
                        if (a != null) {
                            a.close();
                        }
                        if (aVar != null) {
                            aVar.close();
                        }
                        throw th;
                    }
                } catch (Throwable th42) {
                    th2 = th42;
                    a = null;
                    aVar = a2;
                    th = th2;
                    if (a != null) {
                        a.close();
                    }
                    if (aVar != null) {
                        aVar.close();
                    }
                    throw th;
                }
            } catch (Exception e3) {
                cursor = null;
                d.c("MaterialManager", "delete overtime material fail");
                if (cursor != null) {
                    cursor.close();
                }
                if (aVar != null) {
                    aVar.close();
                }
            } catch (Throwable th5) {
                th = th5;
                a = null;
                if (a != null) {
                    a.close();
                }
                if (aVar != null) {
                    aVar.close();
                }
                throw th;
            }
        }
    }
}
