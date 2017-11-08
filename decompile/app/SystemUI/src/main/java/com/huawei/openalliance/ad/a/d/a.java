package com.huawei.openalliance.ad.a.d;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.huawei.openalliance.ad.a.a.f;
import com.huawei.openalliance.ad.a.a.h;
import com.huawei.openalliance.ad.a.f.c;
import com.huawei.openalliance.ad.a.f.e;
import com.huawei.openalliance.ad.a.g.b;
import com.huawei.openalliance.ad.inter.constant.EventType;
import com.huawei.openalliance.ad.utils.b.d;
import com.huawei.openalliance.ad.utils.db.bean.AdEventRecord;
import com.huawei.openalliance.ad.utils.db.bean.ThirdPartyEventRecord;
import com.huawei.openalliance.ad.utils.k;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* compiled from: Unknown */
public class a {
    private static Map<Integer, com.huawei.openalliance.ad.a.f.a> a = new HashMap();

    static {
        a.put(Integer.valueOf(1), new e());
        a.put(Integer.valueOf(2), new c());
    }

    public static void a(Context context, int i, com.huawei.openalliance.ad.a.a.c cVar) {
        if (cVar == null || context == null) {
            d.c("AdEventManager", "event is null");
            return;
        }
        d.b("AdEventManager", "eventType:", cVar.getType__(), " | time:", String.valueOf(cVar.getTime__()));
        b(context);
        b(context, i, cVar);
        if (((com.huawei.openalliance.ad.a.f.a) a.get(Integer.valueOf(i))).a(cVar.getType__()) && com.huawei.openalliance.ad.a.g.d.a(context)) {
            d(context);
        }
    }

    public static void a(Context context, int i, EventType eventType, com.huawei.openalliance.ad.a.a.b.e eVar) {
        if (eVar == null || context == null) {
            d.c("AdEventManager", "content is null");
            return;
        }
        a(context, i, com.huawei.openalliance.ad.a.g.d.a(eventType.value(), eVar));
        if ("click".equalsIgnoreCase(eventType.value())) {
            a(context, i, eVar.getClickmonitorurl__());
        } else if ("imp".equalsIgnoreCase(eventType.value())) {
            a(context, i, eVar.getImpmonitorurl__());
        }
    }

    private static void a(Context context, int i, List<String> list) {
        if (context != null) {
            if (list == null || list.isEmpty()) {
                b(context, i);
            } else {
                for (String str : list) {
                    try {
                        new b(context, str, new h(str), new b(i)).executeOnExecutor(k.b, new Void[0]);
                    } catch (Exception e) {
                        d.c("AdEventManager", "report third party event fail");
                    }
                }
            }
        }
    }

    public static void b(Context context) {
        if (context != null) {
            for (com.huawei.openalliance.ad.a.f.a a : a.values()) {
                a.a(context);
            }
        }
    }

    private static void b(Context context, int i) {
        Throwable th;
        Cursor cursor = null;
        com.huawei.openalliance.ad.utils.db.a a = com.huawei.openalliance.ad.utils.db.a.a(context);
        Cursor a2;
        try {
            a2 = a.a(ThirdPartyEventRecord.class.getSimpleName(), null, "(lockTime = 0 or lockTime < ?) and adType = ?", new String[]{String.valueOf(com.huawei.openalliance.ad.a.g.d.c() - 120000), String.valueOf(i)}, "time asc", String.valueOf(3));
            if (a2 != null) {
                try {
                    if (a2.getCount() > 0) {
                        a2.moveToFirst();
                        ContentValues contentValues = new ContentValues();
                        while (true) {
                            String string = a2.getString(a2.getColumnIndex("url"));
                            contentValues.put("lockTime", Long.valueOf(r8));
                            a.a(ThirdPartyEventRecord.class.getSimpleName(), contentValues, "_id = ?", new String[]{a2.getString(a2.getColumnIndex("_id"))});
                            com.huawei.openalliance.ad.a.a.a.b hVar = new h(string);
                            hVar.set_id(a2.getString(a2.getColumnIndex("_id")));
                            new b(context, string, hVar, new c()).executeOnExecutor(k.b, new Void[0]);
                            a2.moveToNext();
                            if (a2.isAfterLast()) {
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    try {
                        d.c("AdEventManager", "report third party cache fail");
                        if (a2 != null) {
                            a2.close();
                        }
                        a.close();
                    } catch (Throwable th2) {
                        cursor = a2;
                        th = th2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        a.close();
                        throw th;
                    }
                }
            }
            if (a2 != null) {
                a2.close();
            }
            a.close();
        } catch (Exception e2) {
            a2 = null;
            d.c("AdEventManager", "report third party cache fail");
            if (a2 != null) {
                a2.close();
            }
            a.close();
        } catch (Throwable th3) {
            th = th3;
            if (cursor != null) {
                cursor.close();
            }
            a.close();
            throw th;
        }
    }

    private static void b(Context context, int i, com.huawei.openalliance.ad.a.a.c cVar) {
        if (cVar != null) {
            com.huawei.openalliance.ad.utils.db.a a = com.huawei.openalliance.ad.utils.db.a.a(context);
            AdEventRecord adEventRecord = new AdEventRecord(cVar);
            adEventRecord.a(i);
            try {
                a.a(AdEventRecord.class.getSimpleName(), adEventRecord.q());
            } catch (Exception e) {
                d.c("AdEventManager", "insert event fail");
            } finally {
                a.close();
            }
        }
    }

    private static List<com.huawei.openalliance.ad.a.a.c> c(Context context) {
        Cursor a;
        Throwable th;
        Cursor cursor = null;
        List<com.huawei.openalliance.ad.a.a.c> arrayList = new ArrayList(4);
        com.huawei.openalliance.ad.utils.db.a a2 = com.huawei.openalliance.ad.utils.db.a.a(context);
        try {
            a = a2.a(AdEventRecord.class.getSimpleName(), null, "lockTime = 0 or lockTime < ?", new String[]{String.valueOf(com.huawei.openalliance.ad.a.g.d.c() - 120000)}, "_id desc", String.valueOf(50));
            if (a != null) {
                try {
                    if (a.getCount() > 0) {
                        a.moveToFirst();
                        List arrayList2 = new ArrayList(4);
                        do {
                            AdEventRecord adEventRecord = new AdEventRecord();
                            adEventRecord.a(a);
                            arrayList.add(adEventRecord.a());
                            arrayList2.add(adEventRecord.b());
                            a.moveToNext();
                        } while (!a.isAfterLast());
                        if (!arrayList2.isEmpty()) {
                            a2.a(AdEventRecord.class.getSimpleName(), arrayList2, r10);
                        }
                    }
                } catch (Exception e) {
                    try {
                        d.c("AdEventManager", "get event cache fail");
                        if (a != null) {
                            a.close();
                        }
                        a2.close();
                        return arrayList;
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
        } catch (Exception e2) {
            a = null;
            d.c("AdEventManager", "get event cache fail");
            if (a != null) {
                a.close();
            }
            a2.close();
            return arrayList;
        } catch (Throwable th3) {
            th = th3;
            if (cursor != null) {
                cursor.close();
            }
            a2.close();
            throw th;
        }
        return arrayList;
    }

    private static void d(Context context) {
        List c = c(context);
        if (!c.isEmpty()) {
            try {
                new com.huawei.openalliance.ad.a.g.c(context, com.huawei.openalliance.ad.a.b.a.d, new f(c), new d(c)).executeOnExecutor(k.b, new Void[0]);
            } catch (Exception e) {
                d.c("AdEventManager", "report event cache fail");
            }
        }
    }
}
