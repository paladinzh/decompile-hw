package com.amap.api.mapcore.util;

import android.content.Context;
import android.text.TextUtils;
import java.io.File;
import java.io.IOException;

/* compiled from: OfflineMapRemoveTask */
public class bd {
    private Context a;

    public bd(Context context) {
        this.a = context;
    }

    public void a(aw awVar) {
        b(awVar);
    }

    private boolean b(aw awVar) {
        if (awVar == null) {
            return false;
        }
        boolean a = a(awVar.getPinyin(), this.a);
        if (a) {
            awVar.i();
            return a;
        }
        awVar.h();
        return false;
    }

    private boolean a(String str, Context context) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        String a = eh.a(context);
        try {
            File file = new File(a + "vmap/" + str);
            if (file.exists()) {
                if (!bu.b(file)) {
                    bu.a("deleteDownload delete some thing wrong!");
                    return false;
                }
            }
            try {
                bu.b(a + "vmap/");
                bu.b(str, context);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e2) {
                e2.printStackTrace();
                return false;
            }
        } catch (IOException e3) {
            e3.printStackTrace();
            return false;
        } catch (Exception e22) {
            e22.printStackTrace();
            return false;
        }
    }
}
