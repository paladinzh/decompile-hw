package com.amap.api.mapcore.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.os.StatFs;
import com.amap.api.mapcore.util.gt.b;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.util.HashMap;

/* compiled from: ImageCache */
public class dw {
    private static final CompressFormat a = CompressFormat.PNG;
    private gt b;
    private eb<String, Bitmap> c;
    private a d;
    private final Object e = new Object();
    private boolean f = true;
    private HashMap<String, WeakReference<Bitmap>> g;

    /* compiled from: ImageCache */
    public static class a {
        public int a = 5242880;
        public int b = 10485760;
        public File c;
        public CompressFormat d = dw.a;
        public int e = 100;
        public boolean f = true;
        public boolean g = true;
        public boolean h = false;

        public a(Context context, String str) {
            this.c = dw.a(context, str);
        }

        public void a(int i) {
            this.a = i;
        }

        public void b(int i) {
            if (i <= 0) {
                this.g = false;
            }
            this.b = i;
        }

        public void a(String str) {
            this.c = new File(str);
        }

        public void a(boolean z) {
            this.f = z;
        }

        public void b(boolean z) {
            this.g = z;
        }
    }

    private dw(a aVar) {
        b(aVar);
    }

    public static dw a(a aVar) {
        return new dw(aVar);
    }

    private void b(a aVar) {
        this.d = aVar;
        if (this.d.f) {
            if (eh.c()) {
                this.g = new HashMap();
            }
            this.c = new eb<String, Bitmap>(this, this.d.a) {
                final /* synthetic */ dw a;

                protected void a(boolean z, String str, Bitmap bitmap, Bitmap bitmap2) {
                    if (eh.c() && this.a.g != null && bitmap != null && !bitmap.isRecycled()) {
                        this.a.g.put(str, new WeakReference(bitmap));
                    }
                }

                protected int a(String str, Bitmap bitmap) {
                    int a = dw.a(bitmap);
                    return a != 0 ? a : 1;
                }
            };
        }
        if (aVar.h) {
            a();
        }
    }

    public void a() {
        Object obj = 1;
        synchronized (this.e) {
            if (this.b == null || this.b.a()) {
                File file = this.d.c;
                if (this.d.g && file != null) {
                    try {
                        if (file.exists()) {
                            b(file);
                        }
                        file.mkdir();
                    } catch (Throwable th) {
                    }
                    if (a(file) > ((long) this.d.b)) {
                        obj = null;
                    }
                    if (obj == null) {
                        try {
                            this.b = gt.a(file, 1, 1, (long) this.d.b);
                        } catch (Throwable th2) {
                            this.d.c = null;
                        }
                    }
                }
            }
            this.f = false;
            this.e.notifyAll();
        }
    }

    private void b(File file) throws IOException {
        int i = 0;
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            int length = listFiles.length;
            while (i < length) {
                File file2 = listFiles[i];
                if (file2.isDirectory()) {
                    b(file2);
                }
                if (file2.delete()) {
                    i++;
                } else {
                    throw new IOException("failed to delete file: " + file2);
                }
            }
            return;
        }
        throw new IOException("not a readable directory: " + file);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void a(String str, Bitmap bitmap) {
        OutputStream outputStream = null;
        if (str != null && bitmap != null && !bitmap.isRecycled()) {
            if (this.c != null) {
                this.c.b(str, bitmap);
            }
            synchronized (this.e) {
                if (this.b != null) {
                    String c = c(str);
                    try {
                        b a = this.b.a(c);
                        if (a != null) {
                            a.a(0).close();
                        } else {
                            com.amap.api.mapcore.util.gt.a b = this.b.b(c);
                            if (b != null) {
                                outputStream = b.a(0);
                                bitmap.compress(this.d.d, this.d.e, outputStream);
                                b.a();
                                outputStream.close();
                            }
                        }
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (Throwable th) {
                            }
                        }
                    } catch (Throwable th2) {
                        Throwable th3 = th2;
                        OutputStream outputStream2 = outputStream;
                        Throwable th4 = th3;
                        if (outputStream2 != null) {
                            try {
                                outputStream2.close();
                            } catch (Throwable th5) {
                            }
                        }
                        throw th4;
                    }
                }
            }
        }
    }

    public Bitmap a(String str) {
        Bitmap bitmap;
        if (eh.c() && this.g != null) {
            WeakReference weakReference = (WeakReference) this.g.get(str);
            if (weakReference == null) {
                bitmap = null;
            } else {
                bitmap = (Bitmap) weakReference.get();
                if (bitmap == null || bitmap.isRecycled()) {
                    bitmap = null;
                }
                this.g.remove(str);
            }
        } else {
            bitmap = null;
        }
        if (bitmap == null && this.c != null) {
            bitmap = (Bitmap) this.c.a((Object) str);
        }
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        return bitmap;
    }

    public Bitmap b(String str) {
        InputStream inputStream;
        Throwable th;
        Bitmap bitmap = null;
        String c = c(str);
        synchronized (this.e) {
            while (this.f) {
                try {
                    this.e.wait();
                } catch (Throwable th2) {
                }
            }
            if (this.b != null) {
                try {
                    b a = this.b.a(c);
                    if (a == null) {
                        inputStream = bitmap;
                    } else {
                        inputStream = a.a(0);
                        if (inputStream != null) {
                            try {
                                bitmap = dy.a(((FileInputStream) inputStream).getFD(), Integer.MAX_VALUE, Integer.MAX_VALUE, this);
                            } catch (Throwable th3) {
                                th = th3;
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (Throwable th4) {
                                    }
                                }
                                throw th;
                            }
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable th5) {
                        }
                    }
                } catch (Throwable th6) {
                    th = th6;
                    inputStream = bitmap;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    throw th;
                }
            }
        }
        return bitmap;
    }

    public void b() {
        if (eh.c() && this.g != null) {
            this.g.clear();
        }
        if (this.c != null) {
            this.c.a();
        }
        synchronized (this.e) {
            this.f = true;
            if (this.b != null) {
                if (!this.b.a()) {
                    try {
                        this.b.c();
                    } catch (Throwable th) {
                    }
                    this.b = null;
                    a();
                }
            }
        }
    }

    public void c() {
        synchronized (this.e) {
            if (this.b != null) {
                try {
                    this.b.b();
                } catch (Throwable th) {
                }
            }
        }
    }

    public void d() {
        if (eh.c() && this.g != null) {
            this.g.clear();
        }
        if (this.c != null) {
            this.c.a();
        }
        synchronized (this.e) {
            if (this.b != null) {
                try {
                    if (!this.b.a()) {
                        this.b.c();
                        this.b = null;
                    }
                } catch (Throwable th) {
                }
            }
        }
    }

    public static File a(Context context, String str) {
        String path;
        File a = a(context);
        if ("mounted".equals(Environment.getExternalStorageState()) || !e()) {
            if (a != null) {
                path = a.getPath();
                return new File(path + File.separator + str);
            }
        }
        path = context.getCacheDir().getPath();
        return new File(path + File.separator + str);
    }

    public static String c(String str) {
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(str.getBytes("utf-8"));
            return a(instance.digest());
        } catch (Throwable th) {
            return String.valueOf(str.hashCode());
        }
    }

    private static String a(byte[] bArr) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bArr) {
            String toHexString = Integer.toHexString(b & 255);
            if (toHexString.length() == 1) {
                stringBuilder.append('0');
            }
            stringBuilder.append(toHexString);
        }
        return stringBuilder.toString();
    }

    public static int a(Bitmap bitmap) {
        if (eh.d()) {
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public static boolean e() {
        if (eh.b()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    public static File a(Context context) {
        try {
            if (eh.a()) {
                return context.getExternalCacheDir();
            }
            return new File(Environment.getExternalStorageDirectory().getPath() + ("/Android/data/" + context.getPackageName() + "/cache/"));
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public static long a(File file) {
        if (eh.b()) {
            return file.getUsableSpace();
        }
        StatFs statFs = new StatFs(file.getPath());
        return ((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize());
    }
}
