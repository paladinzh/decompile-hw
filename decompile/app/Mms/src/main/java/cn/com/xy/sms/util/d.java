package cn.com.xy.sms.util;

import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

/* compiled from: Unknown */
final class d implements Runnable {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ long e;

    d(String str, String str2, String str3, String str4, long j) {
        this.a = str;
        this.b = str2;
        this.c = str3;
        this.d = str4;
        this.e = j;
    }

    public final void run() {
        try {
            String md5 = MatchCacheManager.getMD5(this.a, this.b);
            if (!StringUtils.isNull(md5)) {
                String[] strArr = new String[12];
                strArr[0] = "msg_num_md5";
                strArr[1] = md5;
                strArr[2] = NetUtil.REQ_QUERY_NUM;
                strArr[3] = StringUtils.getPhoneNumberNo86(this.a);
                strArr[4] = "msg_id";
                strArr[5] = this.c;
                strArr[6] = "session_reuslt";
                strArr[7] = this.d != null ? this.d : "";
                strArr[8] = "save_time";
                strArr[9] = String.valueOf(this.e);
                strArr[10] = "session_lasttime";
                strArr[11] = String.valueOf(System.currentTimeMillis());
                MatchCacheManager.insertOrUpdate(BaseManager.getContentValues(null, strArr), 1);
            }
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
