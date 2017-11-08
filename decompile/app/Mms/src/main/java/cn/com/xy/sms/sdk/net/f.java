package cn.com.xy.sms.sdk.net;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.net.util.j;

/* compiled from: Unknown */
final class f implements XyCallBack {
    f() {
    }

    public final void execute(Object... objArr) {
        if (objArr != null && objArr[0].toString().equals("0") && objArr.length == 2) {
            String d = j.d(objArr[1].toString());
            if (d != null) {
                SysParamEntityManager.setParam(Constant.HTTPTOKEN, d);
                SysParamEntityManager.cacheMap.put(Constant.HTTPTOKEN, d);
            }
        }
    }
}
