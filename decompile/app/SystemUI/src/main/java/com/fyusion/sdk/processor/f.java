package com.fyusion.sdk.processor;

import android.util.Log;
import com.fyusion.sdk.common.ext.FyuseState;
import com.fyusion.sdk.common.ext.k;
import com.fyusion.sdk.common.ext.m;
import com.fyusion.sdk.common.util.a;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/* compiled from: Unknown */
public class f {
    private final m a;
    private final File b;

    public f(m mVar) {
        this.a = mVar;
        this.b = new File(mVar.c(), "upload");
        if (!this.b.exists()) {
            this.b.mkdirs();
        }
    }

    File a() {
        return this.a.b();
    }

    public File a(int i) {
        File file = new File(this.b, String.format(Locale.US, k.aA, new Object[]{Integer.valueOf(i)}));
        if (file.exists() && file.isDirectory()) {
            file.delete();
            try {
                file.createNewFile();
            } catch (Throwable e) {
                Log.e("UploadDataManager", "Unable to create slice file: " + i, e);
                e.printStackTrace();
            }
        }
        return file;
    }

    public void a(FyuseState fyuseState) {
        this.a.a(fyuseState);
    }

    public void a(com.fyusion.sdk.common.ext.f fVar) {
        this.a.a(this.b, fVar);
    }

    public void b() throws IOException {
        File e = this.a.e();
        a.a(e, new File(this.b, e.getName()));
    }

    public void c() {
        String[] list = this.b.list();
        if (list != null) {
            for (String str : list) {
                if (str.startsWith(k.ap)) {
                    new File(this.b, str).delete();
                }
            }
        }
    }
}
