package cn.com.xy.sms.sdk.util;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.net.NetUtil;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
final class r implements XyCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;

    r(String str, String str2, String str3) {
        this.a = str;
        this.b = str2;
        this.c = str3;
    }

    public final void execute(Object... objArr) {
        if (objArr != null && objArr.length > 0) {
            String obj = objArr[0].toString();
            if (obj.equals("1")) {
                NetUtil.requestNewTokenAndPostRequestAgain(NetUtil.URL_MENU_CLICKED, this.a, Constant.FIVE_MINUTES, true, false, true, null, this.b, this);
            } else if (obj.equals("0") && objArr.length == 2) {
                try {
                    JSONObject jSONObject = new JSONObject(objArr[1].toString());
                    jSONObject.get("code");
                    if (jSONObject.get("code").equals(Constant.FIND_CMD_STATUS)) {
                        Constant.getContext();
                        SysParamEntityManager.setParam("LastMenuActionCountActionUpdate", this.c);
                        try {
                            DBManager.delete("tb_menu_action", "date < ?", new String[]{this.c});
                        } catch (Throwable th) {
                        }
                    }
                } catch (JSONException e) {
                }
            }
        }
    }
}
