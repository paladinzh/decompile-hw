package com.huawei.openalliance.ad.a.e;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.openalliance.ad.a.a.b.b;
import com.huawei.openalliance.ad.a.a.b.i;
import com.huawei.openalliance.ad.a.a.b.m;
import com.huawei.openalliance.ad.a.a.c;
import com.huawei.openalliance.ad.inter.MagLockAd;
import com.huawei.openalliance.ad.inter.MagLockAdInfo;
import com.huawei.openalliance.ad.utils.b.d;
import com.huawei.openalliance.ad.utils.db.a;
import com.huawei.openalliance.ad.utils.db.bean.AdEventRecord;
import com.huawei.openalliance.ad.utils.db.bean.MaterialRecord;
import com.huawei.openalliance.ad.utils.j;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;

/* compiled from: Unknown */
public class e {
    public static com.huawei.openalliance.ad.a.a.b.e a(MaterialRecord materialRecord) {
        if (materialRecord == null) {
            return null;
        }
        com.huawei.openalliance.ad.a.a.b.e eVar = new com.huawei.openalliance.ad.a.a.b.e();
        eVar.setContentid__(materialRecord.d());
        eVar.setEndtime__(materialRecord.e());
        eVar.setCreativetype__(materialRecord.k());
        eVar.setHtml__(materialRecord.i());
        eVar.setInteractiontype__(materialRecord.j());
        eVar.setMd5__(materialRecord.a());
        eVar.setSha256__(materialRecord.b());
        eVar.setSkipText__(materialRecord.c());
        if (!TextUtils.isEmpty(materialRecord.p())) {
            i iVar = new i();
            iVar.setIntentUri__(materialRecord.p());
            eVar.setInteractionparam__(iVar);
        }
        eVar.setMaxtimes__(materialRecord.h());
        m mVar = new m();
        if (!TextUtils.isEmpty(materialRecord.o())) {
            try {
                mVar.fromJson(new JSONObject(materialRecord.o()));
            } catch (Exception e) {
                d.c("AdSourceUtil", "convert param json fail");
                mVar = null;
            }
            eVar.setParamfromserver__(mVar);
        }
        Object m = materialRecord.m();
        if (!TextUtils.isEmpty(m)) {
            eVar.setImpmonitorurl__(Arrays.asList(m.split(";")));
        }
        m = materialRecord.l();
        if (!TextUtils.isEmpty(m)) {
            eVar.setClickmonitorurl__(Arrays.asList(m.split(";")));
        }
        return eVar;
    }

    public static MaterialRecord a() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        MaterialRecord materialRecord = new MaterialRecord();
        materialRecord.d(0);
        materialRecord.g(simpleDateFormat.format(new Date()));
        return materialRecord;
    }

    public static MaterialRecord a(String str, int i, com.huawei.openalliance.ad.a.a.b.e eVar, boolean z, boolean z2) {
        if (eVar == null || TextUtils.isEmpty(str)) {
            return null;
        }
        MaterialRecord a = a();
        if (z) {
            a.b(1);
        } else {
            a.b(0);
        }
        a.f(eVar.getInteractiontype__());
        if (eVar.getInteractionparam__() != null) {
            a.l(eVar.getInteractionparam__().getIntentUri__());
        }
        a.h(eVar.getCreativetype__());
        a.h(eVar.getHtml__());
        a.f(eVar.getContentid__());
        a.e(eVar.getMaxtimes__());
        if (eVar.getMaxtimes__() <= 0) {
            a.e(5);
        }
        a.e(str);
        a.a(eVar.getEndtime__());
        a.g(0);
        if (z2) {
            a.c(0);
        } else {
            a.c(1);
        }
        a.b(com.huawei.openalliance.ad.a.g.d.c());
        if (eVar.getParamfromserver__() != null) {
            try {
                a.k(eVar.getParamfromserver__().toJson());
            } catch (Exception e) {
                d.c("AdSourceUtil", "convert param fail");
                a.k(null);
            }
        }
        a.a(eVar.getMd5__());
        a.b(eVar.getSha256__());
        a.c(eVar.getSkipText__());
        if (eVar.getMetaData__() != null) {
            try {
                a.d(eVar.getMetaData__().toJson());
            } catch (Exception e2) {
                d.c("AdSourceUtil", "convert metadata fail");
                a.d(null);
            }
        }
        a.a(i);
        a.j(j.a(eVar.getImpmonitorurl__(), ";"));
        a.i(j.a(eVar.getClickmonitorurl__(), ";"));
        return a;
    }

    public static void a(Context context, List<c> list) {
        if (list != null) {
            List arrayList = new ArrayList(4);
            a a = a.a(context);
            try {
                for (c cVar : list) {
                    if (cVar != null) {
                        arrayList.add(cVar.getSeq__());
                        if (!arrayList.isEmpty()) {
                            a.a(AdEventRecord.class.getSimpleName(), arrayList, 0);
                        }
                    }
                }
            } catch (Exception e) {
                d.c("AdSourceUtil", "update lock time fail");
            } finally {
                a.close();
            }
        }
    }

    public static void a(b bVar, List<String> list, List<String> list2) {
        if (bVar != null) {
            if (a(bVar.getRetcode())) {
                list.add(bVar.getSeq());
            } else {
                list2.add(bVar.getSeq());
            }
        }
    }

    private static boolean a(int i) {
        return 200 == i || 601 == i || 611 == i;
    }

    public static boolean a(com.huawei.openalliance.ad.a.a.b bVar) {
        return (bVar.getMultiad__() == null || bVar.getMultiad__().isEmpty()) ? false : true;
    }

    public static boolean a(MagLockAd magLockAd) {
        return (magLockAd.getAdList() == null || magLockAd.getAdList().isEmpty()) ? false : true;
    }

    public static boolean a(MagLockAdInfo magLockAdInfo) {
        return (magLockAdInfo.getMultiAds() == null || magLockAdInfo.getMultiAds().isEmpty()) ? false : true;
    }
}
