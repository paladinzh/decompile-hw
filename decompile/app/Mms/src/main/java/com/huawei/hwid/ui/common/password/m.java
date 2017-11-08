package com.huawei.hwid.ui.common.password;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import com.huawei.hwid.ui.common.j;

/* compiled from: FindpwdbyEmailActivity */
class m implements OnClickListener {
    final /* synthetic */ FindpwdbyEmailActivity a;

    m(FindpwdbyEmailActivity findpwdbyEmailActivity) {
        this.a = findpwdbyEmailActivity;
    }

    public void onClick(View view) {
        if (this.a.d != null && this.a.d.length > 0) {
            Object obj = new String[this.a.d.length];
            System.arraycopy(this.a.d, 0, obj, 0, this.a.d.length);
            Dialog create = new Builder(this.a, j.b(this.a)).setAdapter(new ArrayAdapter(this.a, com.huawei.hwid.core.c.m.d(this.a, "cs_listview_item"), com.huawei.hwid.core.c.m.e(this.a, "id_txt"), obj), new n(this)).create();
            create.show();
            this.a.a(create);
        }
    }
}
