package com.huawei.openalliance.ad.a.e;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.openalliance.ad.a.a.b;
import com.huawei.openalliance.ad.a.a.b.a;
import com.huawei.openalliance.ad.a.a.b.e;
import com.huawei.openalliance.ad.a.a.b.m;
import com.huawei.openalliance.ad.inter.MagLockAd;
import com.huawei.openalliance.ad.inter.MagLockAdContent;
import com.huawei.openalliance.ad.inter.MagLockAdInfo;
import com.huawei.openalliance.ad.utils.b.d;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;

/* compiled from: Unknown */
public class k {
    private static e a(MagLockAdContent magLockAdContent) {
        e eVar = new e();
        eVar.setContentid__(magLockAdContent.getContentId());
        eVar.setEndtime__(magLockAdContent.getEndTime());
        eVar.setCreativetype__(5);
        m mVar = new m();
        if (!TextUtils.isEmpty(magLockAdContent.getParamFromServer())) {
            try {
                mVar.fromJson(new JSONObject(magLockAdContent.getParamFromServer()));
            } catch (Exception e) {
                d.c("MagLockTools", "convert paramFromServer json fail");
                mVar = null;
            }
            eVar.setParamfromserver__(mVar);
        }
        com.huawei.openalliance.ad.a.a.b.k kVar = new com.huawei.openalliance.ad.a.a.b.k();
        if (!TextUtils.isEmpty(magLockAdContent.getMetaData())) {
            try {
                kVar.fromJson(new JSONObject(magLockAdContent.getMetaData()));
            } catch (Exception e2) {
                d.c("MagLockTools", "convert metaData fail");
            }
            eVar.setMetaData__(kVar);
        }
        Object impMonitorUrl = magLockAdContent.getImpMonitorUrl();
        if (!TextUtils.isEmpty(impMonitorUrl)) {
            eVar.setImpmonitorurl__(Arrays.asList(impMonitorUrl.split(";")));
        }
        impMonitorUrl = magLockAdContent.getClickMonitorUrl();
        if (!TextUtils.isEmpty(impMonitorUrl)) {
            eVar.setClickmonitorurl__(Arrays.asList(impMonitorUrl.split(";")));
        }
        return eVar;
    }

    private static b a(MagLockAdInfo magLockAdInfo) {
        b bVar = new b();
        bVar.setRetcode__(magLockAdInfo.getRetCode());
        bVar.setInvalidcontentid__(magLockAdInfo.getInvalidContentIds());
        if (e.a(magLockAdInfo)) {
            List arrayList = new ArrayList(4);
            for (MagLockAd magLockAd : magLockAdInfo.getMultiAds()) {
                if (magLockAd != null) {
                    a aVar = new a();
                    aVar.setSlotid__(magLockAd.getSlotId());
                    aVar.setRetcode30__(magLockAd.getRetCode());
                    if (e.a(magLockAd)) {
                        List arrayList2 = new ArrayList(4);
                        for (MagLockAdContent magLockAdContent : magLockAd.getAdList()) {
                            if (magLockAdContent != null) {
                                arrayList2.add(a(magLockAdContent));
                            }
                        }
                        aVar.setContent__(arrayList2);
                    }
                    arrayList.add(aVar);
                }
            }
            bVar.setMultiad__(arrayList);
        }
        return bVar;
    }

    public static void a(Context context, MagLockAdInfo magLockAdInfo, boolean z) {
        if (magLockAdInfo != null) {
            try {
                new h().b(context, a(magLockAdInfo), z);
            } catch (Exception e) {
                d.c("MagLockTools", "iterator maglock content fail");
            }
        }
    }
}
