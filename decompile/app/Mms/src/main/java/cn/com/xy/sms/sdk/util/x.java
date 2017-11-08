package cn.com.xy.sms.sdk.util;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.db.entity.v;
import cn.com.xy.sms.sdk.net.util.i;

/* compiled from: Unknown */
final class x implements XyCallBack {
    private final /* synthetic */ String a;

    x(String str) {
        this.a = str;
    }

    public final void execute(Object... objArr) {
        if (objArr != null && objArr[0].toString().equals("0") && objArr.length == 2 && i.d(objArr[1].toString())) {
            SysParamEntityManager.setParam("LastSceneCountActionUpdate", this.a);
            try {
                DBManager.delete("tb_popup_action_scene", "date < ?", new String[]{this.a});
            } catch (Throwable th) {
            }
            v.a(this.a);
        }
    }
}
