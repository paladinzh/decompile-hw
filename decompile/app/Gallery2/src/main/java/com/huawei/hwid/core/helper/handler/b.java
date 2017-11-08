package com.huawei.hwid.core.helper.handler;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.j;
import com.huawei.hwid.core.encrypt.f;
import java.util.ArrayList;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class b {
    protected Context b;

    public ArrayList<Integer> c(Bundle bundle) {
        ArrayList<Integer> integerArrayList = bundle.getIntegerArrayList("UIHandlerErrCodeList");
        if (integerArrayList != null) {
            return integerArrayList;
        }
        return new ArrayList();
    }

    public boolean d(Bundle bundle) {
        return bundle.getBoolean("isUIHandlerAllErrCode");
    }

    public b(Context context) {
        this.b = context;
    }

    public void a(Bundle bundle) {
    }

    public void b(Bundle bundle) {
    }

    public void e(Bundle bundle) {
        try {
            int i = bundle.getInt("responseCode");
            int i2 = bundle.getInt("resultCode");
            e.b("RequestCallback", "disposeRequestMessage  orgin responseCode = " + i + " orgin resultCode = " + i2);
            int a = com.huawei.hwid.core.b.a.b.a(bundle, bundle.getInt("errorCode"));
            String string = bundle.getString("errorDesc");
            Bundle bundle2;
            Parcelable errorStatus;
            if (3000 != i) {
                Bundle bundle3;
                Parcelable errorStatus2;
                if (SmsCheckResult.ESCT_200 != i) {
                    if (!d(bundle)) {
                        String str = "";
                        if (i == SmsCheckResult.ESCT_307) {
                            i2 = 70001104;
                        }
                        e.b("RequestManager", "network is unavailable, code = " + i);
                        String a2 = a(i, i2);
                        if (!(i == 3008 || i == 1007)) {
                            i = 4098;
                        }
                        bundle3 = new Bundle();
                        errorStatus2 = new ErrorStatus(i, a2);
                        e.e("RequestCallback", "error: " + errorStatus2.toString());
                        bundle3.putParcelable("requestError", errorStatus2);
                        e.b("RequestCallback", "time: " + com.huawei.hwid.core.d.b.f("yyyy/MM/dd HH:mm:ss:SSS"));
                        b(bundle3);
                        return;
                    }
                }
                ArrayList c = c(bundle);
                if (i2 == 0 || c.contains(Integer.valueOf(a)) || d(bundle)) {
                    bundle2 = new Bundle();
                    if (i2 != 0) {
                        errorStatus = new ErrorStatus(a, string);
                        e.e("RequestCallback", "error: " + errorStatus.toString());
                        bundle2.putParcelable("requestError", errorStatus);
                        bundle2.putBoolean("isRequestSuccess", true);
                        b(bundle2);
                        return;
                    }
                    a(bundle);
                    return;
                }
                e.a("RequestManager", "errorCode = " + a + ", errorDesc = " + f.a(string));
                Object a3 = com.huawei.hwid.core.b.a.b.a(this.b, a);
                if (TextUtils.isEmpty(a3)) {
                    i = FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
                } else {
                    i = a;
                }
                bundle3 = new Bundle();
                errorStatus2 = new ErrorStatus(i, a3);
                e.e("RequestCallback", "error: " + errorStatus2.toString());
                bundle3.putParcelable("requestError", errorStatus2);
                b(bundle3);
                return;
            }
            bundle2 = new Bundle();
            errorStatus = new ErrorStatus(FragmentTransaction.TRANSIT_FRAGMENT_FADE, "token invalid");
            e.e("RequestCallback", "error: " + errorStatus.toString());
            bundle2.putParcelable("requestError", errorStatus);
            b(bundle2);
        } catch (Throwable th) {
            e.d("RequestCallback", th.toString(), th);
        }
    }

    private String a(int i, int i2) {
        int a;
        if (1007 != i) {
            a = j.a(this.b, "CS_ERR_for_cannot_conn_service");
        } else {
            a = j.a(this.b, "CS_network_connect_error");
        }
        return this.b.getString(a);
    }
}
