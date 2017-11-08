package com.huawei.hwid.ui.common.password;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.core.datatype.EmailInfo;
import com.huawei.hwid.core.datatype.PhoneNumInfo;
import com.huawei.hwid.ui.common.c;
import com.huawei.hwid.ui.common.j;
import java.util.ArrayList;
import java.util.List;

/* compiled from: FindpwdByHwIdActivity */
class e extends c {
    final /* synthetic */ FindpwdByHwIdActivity b;

    public e(FindpwdByHwIdActivity findpwdByHwIdActivity, Context context) {
        this.b = findpwdByHwIdActivity;
        super(findpwdByHwIdActivity, context);
    }

    public void onSuccess(Bundle bundle) {
        super.onSuccess(bundle);
        List parcelableArrayList = bundle.getParcelableArrayList("securityEmail");
        ArrayList parcelableArrayList2 = bundle.getParcelableArrayList("securityPhone");
        if (parcelableArrayList == null || parcelableArrayList.isEmpty() || parcelableArrayList2 == null || parcelableArrayList2.isEmpty()) {
            if (parcelableArrayList == null || parcelableArrayList.isEmpty()) {
                if (parcelableArrayList2 == null || parcelableArrayList2.isEmpty()) {
                    if ("1".equals(d.b(this.b.e))) {
                        this.b.g = 2;
                    } else if ("2".equals(d.b(this.b.e))) {
                        this.b.g = 1;
                    } else {
                        this.b.b();
                        a.b("FindpwdByHwIdActivity", "GetUserAcctInfocallBack->onSuccess->hwid is not phoneNumber and not email");
                        this.b.a.setError(this.b.getString(m.a(this.b, "CS_no_security_Email_and_Phone")));
                        return;
                    }
                }
            }
            if (parcelableArrayList == null || parcelableArrayList.isEmpty()) {
                this.b.g = 1;
            } else {
                this.b.g = 2;
            }
        } else {
            this.b.g = 3;
        }
        switch (this.b.g) {
            case 1:
                if (parcelableArrayList2 != null && !parcelableArrayList2.isEmpty()) {
                    if (1 == parcelableArrayList2.size()) {
                        this.b.a((PhoneNumInfo) parcelableArrayList2.get(0));
                        break;
                    }
                    this.b.b();
                    this.b.a(parcelableArrayList2);
                    break;
                } else if ("2".equals(d.b(this.b.e))) {
                    this.b.a(new PhoneNumInfo(this.b, d.d(this.b.e), null));
                    break;
                } else {
                    this.b.b();
                    a.b("FindpwdByHwIdActivity", "GetUserAcctInfocallBack->onSuccess->hwid is not phoneNumber");
                    return;
                }
                break;
            case 2:
                if (parcelableArrayList != null && !parcelableArrayList.isEmpty()) {
                    if (1 == parcelableArrayList.size()) {
                        this.b.b(((EmailInfo) parcelableArrayList.get(0)).a());
                        break;
                    }
                    this.b.b();
                    this.b.a(parcelableArrayList);
                    break;
                } else if ("1".equals(d.b(this.b.e))) {
                    this.b.b(this.b.e);
                    break;
                } else {
                    this.b.b();
                    a.b("FindpwdByHwIdActivity", "GetUserAcctInfocallBack->onSuccess->hwid is not email");
                    return;
                }
                break;
            case 3:
                this.b.b();
                if (!(parcelableArrayList == null || parcelableArrayList2 == null)) {
                    this.b.a((ArrayList) parcelableArrayList, parcelableArrayList2);
                    break;
                }
            default:
                this.b.b();
                break;
        }
    }

    public void onFail(Bundle bundle) {
        this.b.b();
        if (bundle.getBoolean("isRequestSuccess", false)) {
            Dialog create = j.a(this.b, m.a(this.b, "CS_no_network_content"), m.a(this.b, "CS_no_network_title")).create();
            this.b.a(create);
            create.show();
        }
        super.onFail(bundle);
    }
}
