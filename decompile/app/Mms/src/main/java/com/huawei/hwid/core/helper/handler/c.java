package com.huawei.hwid.core.helper.handler;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.action.NearbyPoint;
import com.amap.api.services.core.AMapException;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.model.http.g;
import java.util.ArrayList;

/* compiled from: RequestCallback */
public class c {
    protected Context a;

    public ArrayList getUIHandlerErrCodeList(Bundle bundle) {
        ArrayList integerArrayList = bundle.getIntegerArrayList("UIHandlerErrCodeList");
        if (integerArrayList != null) {
            return integerArrayList;
        }
        return new ArrayList();
    }

    public boolean getIsUIHandlerAllErrCode(Bundle bundle) {
        return bundle.getBoolean("isUIHandlerAllErrCode");
    }

    public c(Context context) {
        this.a = context;
    }

    public void onSuccess(Bundle bundle) {
    }

    public void onFail(Bundle bundle) {
    }

    public void disposeRequestMessage(Bundle bundle) {
        try {
            int i = bundle.getInt("responseCode");
            int i2 = bundle.getInt("resultCode");
            a.b("RequestCallback", "disposeRequestMessage  orgin responseCode = " + i + " orgin resultCode = " + i2);
            int a = g.a(bundle, bundle.getInt("errorCode"));
            String string = bundle.getString("errorDesc");
            Bundle bundle2;
            Parcelable errorStatus;
            if (3000 != i) {
                String a2;
                Bundle bundle3;
                Parcelable errorStatus2;
                if (SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE != i) {
                    if (!getIsUIHandlerAllErrCode(bundle)) {
                        String str = "";
                        if (i == 307) {
                            i2 = 70001104;
                        }
                        a.b("RequestManager", "network is unavailable, code = " + i);
                        a2 = a(i, i2);
                        if (i != 3008) {
                            i = NearbyPoint.QUERY_PARAM_ERROR;
                        }
                        bundle3 = new Bundle();
                        errorStatus2 = new ErrorStatus(i, a2);
                        a.e("RequestCallback", "error: " + errorStatus2.toString());
                        bundle3.putParcelable("requestError", errorStatus2);
                        a.b("RequestCallback", "time: " + d.i("yyyy/MM/dd HH:mm:ss:SSS"));
                        onFail(bundle3);
                        return;
                    }
                }
                ArrayList uIHandlerErrCodeList = getUIHandlerErrCodeList(bundle);
                if (i2 == 0 || uIHandlerErrCodeList.contains(Integer.valueOf(a)) || getIsUIHandlerAllErrCode(bundle)) {
                    bundle2 = new Bundle();
                    if (i2 != 0) {
                        errorStatus = new ErrorStatus(a, string);
                        a.e("RequestCallback", "error: " + errorStatus.toString());
                        bundle2.putParcelable("requestError", errorStatus);
                        bundle2.putBoolean("isRequestSuccess", true);
                        onFail(bundle2);
                        return;
                    }
                    onSuccess(bundle);
                    return;
                }
                a.a("RequestManager", "errorCode = " + a + ", errorDesc = " + f.a(string));
                a2 = g.a(this.a, a);
                if (p.e(a2)) {
                    i = NearbyPoint.QUERY_RESULT_RECEIVE;
                } else {
                    i = a;
                }
                bundle3 = new Bundle();
                errorStatus2 = new ErrorStatus(i, a2);
                a.e("RequestCallback", "error: " + errorStatus2.toString());
                bundle3.putParcelable("requestError", errorStatus2);
                onFail(bundle3);
                return;
            }
            bundle2 = new Bundle();
            errorStatus = new ErrorStatus(NearbyPoint.GET_QUERY_URL_FAILURE, "token invalid");
            a.e("RequestCallback", "error: " + errorStatus.toString());
            bundle2.putParcelable("requestError", errorStatus);
            onFail(bundle2);
        } catch (Throwable th) {
            a.d("RequestCallback", th.toString(), th);
        }
    }

    private String a(int i, int i2) {
        if (3008 != i) {
            int a;
            if (1005 == i || 1002 == i || 1001 == i || 1003 == i || AMapException.CODE_AMAP_SERVICE_MAINTENANCE == i || 3000 == i || 3001 == i || 1006 == i || AMapException.CODE_AMAP_ID_NOT_EXIST == i) {
                a = m.a(this.a, "CS_network_connect_error");
            } else {
                a = m.a(this.a, "CS_server_network_error");
            }
            return this.a.getString(a);
        }
        return this.a.getString(m.a(this.a, "CS_ssl_exception"));
    }
}
