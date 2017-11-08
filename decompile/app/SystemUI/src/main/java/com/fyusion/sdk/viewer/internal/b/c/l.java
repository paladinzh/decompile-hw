package com.fyusion.sdk.viewer.internal.b.c;

import com.fyusion.sdk.viewer.internal.b.b.a.a.c;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* compiled from: Unknown */
public class l implements c {
    private InputStream a;

    public l(InputStream inputStream) {
        this.a = inputStream;
    }

    public boolean a(File file) {
        OutputStream fileOutputStream;
        IOException e;
        Throwable th;
        byte[] bArr = new byte[65536];
        try {
            fileOutputStream = new FileOutputStream(file);
            while (true) {
                try {
                    int read = this.a.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    fileOutputStream.write(bArr, 0, read);
                } catch (IOException e2) {
                    e = e2;
                }
            }
            if (fileOutputStream == null) {
                return true;
            }
            try {
                fileOutputStream.close();
                return true;
            } catch (IOException e3) {
                e3.printStackTrace();
                return true;
            }
        } catch (IOException e4) {
            e = e4;
            fileOutputStream = null;
            try {
                e.printStackTrace();
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
                return false;
            } catch (Throwable th2) {
                th = th2;
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            fileOutputStream = null;
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            throw th;
        }
    }
}
