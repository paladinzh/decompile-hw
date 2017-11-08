package com.huawei.hwid.ui.common.login;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import com.huawei.hwid.core.c.m;
import com.huawei.hwid.ui.common.j;

/* compiled from: RegisterViaPhoneNumberActivity */
class bl implements OnClickListener {
    final /* synthetic */ bk a;

    bl(bk bkVar) {
        this.a = bkVar;
    }

    public void onClick(View view) {
        if (this.a.a.l != null && this.a.a.l.length > 0) {
            Object obj = new String[this.a.a.l.length];
            System.arraycopy(this.a.a.l, 0, obj, 0, this.a.a.l.length);
            Dialog create = new Builder(this.a.a, j.b(this.a.a)).setAdapter(new ArrayAdapter(this.a.a, m.d(this.a.a, "cs_listview_item"), m.e(this.a.a, "id_txt"), obj), new bm(this)).create();
            create.show();
            this.a.a.a(create);
        }
    }
}
