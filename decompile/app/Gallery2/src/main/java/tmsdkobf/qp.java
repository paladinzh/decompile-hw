package tmsdkobf;

import android.util.SparseArray;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public final class qp {
    static SparseArray<qq> Kc = new SparseArray();

    static {
        Kc.append(0, new qq("info", "reportSoftList"));
        Kc.append(1, new qq("info", "reportChannelInfo"));
        Kc.append(2, new qq("virusinfo", "getVirusInfos"));
        Kc.append(6, new qq("cloudcheck", "getAnalyseInfo"));
        Kc.append(7, new qq("info", "getUpdatesV2"));
        Kc.append(9, new qq("info", "getGuid"));
        Kc.append(19, new qq("info", "browerCheck"));
        Kc.append(11, new qq("conf", "getConfigV3CPT"));
        Kc.append(12, new qq("sms", "reportSms"));
        Kc.append(13, new qq("sms", "reportTel"));
        Kc.append(14, new qq("sms", "reportSoftFeature"));
        Kc.append(15, new qq("traffic", "getTrafficTemplate"));
        Kc.append(16, new qq("traffic", "getQueryInfo"));
        Kc.append(17, new qq("check", "checkUrl"));
        Kc.append(18, new qq("check", "checkUrlExt"));
        Kc.append(20, new qq("check", "getlicencedate"));
        Kc.append(999, new qq("tipsmain", "getMainTips"));
        Kc.append(21, new qq("sms", "reportHitTel"));
        Kc.append(21, new qq("sms", "reportHitTel"));
        Kc.append(22, new qq("antitheft", "reportCmdResult"));
        Kc.append(23, new qq("phonenumquery", "getTagPhonenum"));
    }

    public static qs cw(int i) {
        d.d("QQPimSecure", "wup request: " + i);
        return new qs(i, (qq) Kc.get(i));
    }
}
