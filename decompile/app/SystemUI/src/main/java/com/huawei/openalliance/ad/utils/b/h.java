package com.huawei.openalliance.ad.utils.b;

import android.util.Log;
import com.huawei.openalliance.ad.utils.b.g.a;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

/* compiled from: Unknown */
public abstract class h {
    private static String b = System.getProperty("line.separator");
    protected f a;
    private int c;
    private String d;
    private long e;
    private final Map<String, f> f;

    private void a(File file, String str, boolean z, boolean z2, String str2) {
        OutputStreamWriter outputStreamWriter;
        Throwable th;
        OutputStreamWriter outputStreamWriter2 = null;
        try {
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8");
            if (z) {
                Object obj = !z2 ? "failure" : "success";
                Object a = k.a();
                a.a("rename to ").a(str2).a(" ").a(obj);
                if (this.c > 0) {
                    a.b().a(b.a());
                }
                outputStreamWriter.write(new a(null, f.OUT).a().a(a).toString());
                outputStreamWriter.write(b);
            }
            try {
                outputStreamWriter.write(str);
                outputStreamWriter.write(b);
                outputStreamWriter.flush();
                if (outputStreamWriter != null) {
                    try {
                        outputStreamWriter.close();
                    } catch (IOException e) {
                        Log.e("LoggerBase", "println, close oswriter error");
                    }
                }
            } catch (FileNotFoundException e2) {
                try {
                    Log.e("LoggerBase", "println error, error:FileNotFoundException, file:" + file);
                    if (outputStreamWriter != null) {
                        try {
                            outputStreamWriter.close();
                        } catch (IOException e3) {
                            Log.e("LoggerBase", "println, close oswriter error");
                        }
                    }
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    outputStreamWriter2 = outputStreamWriter;
                    th = th3;
                    if (outputStreamWriter2 != null) {
                        try {
                            outputStreamWriter2.close();
                        } catch (IOException e4) {
                            Log.e("LoggerBase", "println, close oswriter error");
                        }
                    }
                    throw th;
                }
            } catch (IOException e5) {
                Log.e("LoggerBase", "println error, error:IOException, file:" + file);
                if (outputStreamWriter != null) {
                    try {
                        outputStreamWriter.close();
                    } catch (IOException e6) {
                        Log.e("LoggerBase", "println, close oswriter error");
                    }
                }
            }
        } catch (FileNotFoundException e7) {
            outputStreamWriter = null;
            Log.e("LoggerBase", "println error, error:FileNotFoundException, file:" + file);
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
        } catch (IOException e8) {
            outputStreamWriter = null;
            Log.e("LoggerBase", "println error, error:IOException, file:" + file);
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
        } catch (Throwable th4) {
            th = th4;
            if (outputStreamWriter2 != null) {
                outputStreamWriter2.close();
            }
            throw th;
        }
    }

    private boolean a(File file, File file2) {
        if (file2.exists() && !file2.delete() && Log.isLoggable("LoggerBase", 6)) {
            Log.e("LoggerBase", "delete file failed:" + file2);
        }
        return file.renameTo(file2);
    }

    public synchronized f a(String str) {
        f fVar;
        fVar = (f) this.f.get(str);
        if (fVar == null) {
            fVar = this.a;
        }
        return fVar;
    }

    public abstract void a(g gVar);

    protected void a(String str, f fVar, String str2) {
        if (a(str, fVar)) {
            b(str2);
        }
    }

    public boolean a(String str, f fVar) {
        return fVar.a() >= a(str).a();
    }

    public long b() {
        return this.e;
    }

    public abstract void b(g gVar);

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized void b(String str) {
        String str2 = null;
        boolean z = true;
        boolean z2 = false;
        synchronized (this) {
            if (this.d != null) {
                long b = b();
                File file = new File(this.d);
                if (!file.exists()) {
                    file.setReadable(true);
                    file.setWritable(true);
                    file.setExecutable(false, false);
                }
                if (!(b <= 0)) {
                    File parentFile = file.getParentFile();
                    if (parentFile == null || parentFile.exists() || parentFile.mkdirs()) {
                        if (!(file.length() + ((long) str.length()) <= b)) {
                            str2 = this.d + ".bak";
                            z2 = a(file, new File(str2));
                            a(file, str, z, z2, str2);
                            this.c++;
                        }
                    } else {
                        return;
                    }
                }
                z = false;
                a(file, str, z, z2, str2);
                this.c++;
            }
        }
    }
}
