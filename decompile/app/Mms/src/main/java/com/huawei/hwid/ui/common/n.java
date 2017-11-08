package com.huawei.hwid.ui.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.hwid.core.helper.handler.b;
import com.huawei.hwid.ui.common.password.FindpwdByHwIdActivity;

/* compiled from: UIUtil */
final class n implements OnClickListener {
    final /* synthetic */ b a;
    final /* synthetic */ String b;
    final /* synthetic */ Activity c;
    final /* synthetic */ AlertDialog d;
    final /* synthetic */ boolean e;

    n(b bVar, String str, Activity activity, AlertDialog alertDialog, boolean z) {
        this.a = bVar;
        this.b = str;
        this.c = activity;
        this.d = alertDialog;
        this.e = z;
    }

    public void onClick(View view) {
        if (this.a == null) {
            Intent intent = new Intent();
            intent.putExtra("userAccount", this.b);
            intent.setClass(this.c, FindpwdByHwIdActivity.class);
            this.c.startActivity(intent);
        } else {
            this.a.onForget();
        }
        if (this.d != null) {
            this.d.dismiss();
        }
        if (this.e && this.c != null) {
            this.c.finish();
        }
    }
}
