package com.huawei.hwid.ui.common.login;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import com.huawei.hwid.c.a;
import com.huawei.hwid.core.c.k;
import com.huawei.hwid.core.c.q;

/* compiled from: LoginActivity */
class h implements OnClickListener {
    final /* synthetic */ LoginActivity a;

    h(LoginActivity loginActivity) {
        this.a = loginActivity;
    }

    public void onClick(View view) {
        Intent intent = new Intent();
        intent.putExtras(this.a.getIntent());
        if (k.f(this.a) || k.e(this.a) || q.a(this.a, (int) MsgUrlService.RESULT_NOT_IMPL).startsWith("460") || a.a()) {
            intent.setClass(this.a, RegisterViaPhoneNumberActivity.class);
        } else {
            intent.setClass(this.a, RegisterViaEmailActivity.class);
        }
        this.a.startActivity(intent);
    }
}
