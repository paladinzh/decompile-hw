package com.amap.api.mapcore.util;

import android.content.Context;
import android.text.TextUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;

/* compiled from: OfflineMapRemoveTask */
public class o {
    private Context a;

    public o(Context context) {
        this.a = context;
    }

    public void a(g gVar) {
        b(gVar);
    }

    private boolean b(g gVar) {
        if (gVar == null) {
            return false;
        }
        boolean a = a(gVar.getAdcode(), this.a);
        if (a) {
            gVar.h();
            return a;
        }
        gVar.g();
        return false;
    }

    private boolean a(String str, Context context) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        List<String> b = x.a(context).b(str);
        String a = bj.a(context);
        for (String str2 : b) {
            try {
                File file = new File(a + "vmap/" + str2);
                if (file.exists() && !af.b(file)) {
                    af.a("deleteDownload delete some thing wrong!");
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e2) {
                e2.printStackTrace();
                return false;
            }
        }
        try {
            af.c(a + "vmap/");
            af.a(str, context);
            return true;
        } catch (IOException e3) {
            e3.printStackTrace();
            return false;
        } catch (Exception e22) {
            e22.printStackTrace();
            return false;
        }
    }
}
