package cn.com.xy.sms.sdk.db.entity.a;

import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;

/* compiled from: Unknown */
final class i implements Runnable {
    private final /* synthetic */ int a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ int d;
    private final /* synthetic */ String e;
    private final /* synthetic */ String f;
    private final /* synthetic */ int g;

    i(int i, String str, String str2, int i2, String str3, String str4, int i3) {
        this.a = i;
        this.b = str;
        this.c = str2;
        this.d = i2;
        this.e = str3;
        this.f = str4;
        this.g = i3;
    }

    public final void run() {
        try {
            String str = "tb_public_num_info";
            String[] strArr = new String[]{this.c, String.valueOf(this.d)};
            DBManager.saveOrUpdateTableData(str, BaseManager.getContentValues(null, "pubId", String.valueOf(this.a), "areaCode", this.b, IccidInfoManager.NUM, this.c, "ptype", String.valueOf(this.d), "lastloadtime", String.valueOf(System.currentTimeMillis()), "isrulenum", "1", "purpose", this.e, "extend", this.f, "isuse", "1", "nameType", String.valueOf(this.g)), " num = ? and ptype = ? ", strArr);
        } catch (Throwable th) {
        }
    }
}
