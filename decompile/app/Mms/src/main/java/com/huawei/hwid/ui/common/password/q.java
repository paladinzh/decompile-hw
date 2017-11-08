package com.huawei.hwid.ui.common.password;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.ui.common.j;

/* compiled from: FindpwdbyPhonenumberActivity */
class q implements OnClickListener {
    final /* synthetic */ FindpwdbyPhonenumberActivity a;

    q(FindpwdbyPhonenumberActivity findpwdbyPhonenumberActivity) {
        this.a = findpwdbyPhonenumberActivity;
    }

    public void onClick(View view) {
        if (this.a.f != null && !this.a.f.isEmpty()) {
            Dialog create = new Builder(this.a, j.b(this.a)).setAdapter(new ArrayAdapter(this.a, m.d(this.a, "cs_listview_item"), m.e(this.a, "id_txt"), this.a.f), new r(this)).create();
            create.show();
            this.a.a(create);
        }
    }
}
