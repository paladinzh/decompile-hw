package com.fyusion.sdk.processor;

import android.util.Log;
import com.fyusion.sdk.common.ext.FyuseState;
import com.fyusion.sdk.common.ext.e;
import com.fyusion.sdk.common.ext.j;
import com.fyusion.sdk.common.ext.l;
import com.fyusion.sdk.common.util.a;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/* compiled from: Unknown */
public class g {
    private final l a;
    private final File b;

    public g(l lVar) {
        this.a = lVar;
        this.b = new File(lVar.c(), "upload");
        if (!this.b.exists()) {
            this.b.mkdirs();
        }
    }

    File a() {
        return this.a.b();
    }

    public File a(int i) {
        File file = new File(this.b, String.format(Locale.US, j.aA, new Object[]{Integer.valueOf(i)}));
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

    public void a(e eVar) {
        this.a.a(this.b, eVar);
    }

    public InputStream b(int i) {
        File file = new File(this.b, String.format(Locale.US, j.aq, new Object[]{Integer.valueOf(i)}));
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                Log.w("UploadDataManager", "Metadata could not be obtained, file does not exist.");
                return null;
            }
        }
        Log.w("UploadDataManager", "Metadata could not be obtained, file does not exist: " + file);
        return null;
    }

    public void b() throws IOException {
        File e = this.a.e();
        a.a(e, new File(this.b, e.getName()));
    }

    public InputStream c() throws FileNotFoundException {
        File file = new File(this.b, j.ae);
        if (file.exists()) {
            return new FileInputStream(file);
        }
        throw new FileNotFoundException("Metadata could not be obtained, file does not exist: " + file);
    }

    public e d() throws FileNotFoundException {
        return this.a.d();
    }

    public InputStream e() throws FileNotFoundException {
        File file = new File(this.a.c(), j.ad);
        if (file.exists()) {
            return new FileInputStream(file);
        }
        throw new FileNotFoundException("Thumbnail file" + file + " is not found");
    }

    public FileInputStream f() throws FileNotFoundException {
        File file = new File(this.b, j.ao);
        if (file.exists()) {
            return new FileInputStream(file);
        }
        throw new FileNotFoundException("Metadata could not be obtained, file does not exist: " + file);
    }

    public void g() {
        String[] list = this.b.list();
        if (list != null) {
            for (String str : list) {
                if (str.startsWith(j.ap)) {
                    new File(this.b, str).delete();
                }
            }
        }
    }

    public void h() {
        a.a(this.b);
        a.a(this.a.c());
    }
}
