package com.huawei.hwid.ui.common.login;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.ui.common.g;

/* compiled from: StartUpGuideLoginForAPPActivity */
class co extends g {
    final /* synthetic */ TextView a;
    final /* synthetic */ StartUpGuideLoginForAPPActivity b;

    co(StartUpGuideLoginForAPPActivity startUpGuideLoginForAPPActivity, Context context, TextView textView) {
        this.b = startUpGuideLoginForAPPActivity;
        this.a = textView;
        super(context);
    }

    public void onClick(View view) {
        this.a.setText(this.b.getString(m.a(this.b, "CS_welcome_view_inner_more_newstr"), (Object[]) new String[]{this.b.getString(m.a(this.b, "CS_welcome_view_start")), this.b.getString(m.a(this.b, "CS_welcome_view_end"))}));
    }
}
