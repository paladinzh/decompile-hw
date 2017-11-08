package com.fyusion.sdk.processor.a;

import android.os.Environment;
import com.fyusion.sdk.common.DLog;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileLock;

/* compiled from: Unknown */
public class a {
    private static a a;
    private FileLock b;

    private a() {
    }

    public static a a() {
        if (a == null) {
            synchronized (a.class) {
                if (a == null) {
                    a = new a();
                }
            }
        }
        return a;
    }

    public synchronized FileLock b() {
        if (this.b == null) {
            try {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ".lck");
                this.b = new FileOutputStream(file).getChannel().lock();
                if (this.b != null) {
                    file.deleteOnExit();
                }
                return this.b;
            } catch (Throwable e) {
                DLog.e("LM", "Failed to aqcuireLock", e);
            }
        }
        return this.b;
    }

    public synchronized void c() {
        if (this.b != null) {
            try {
                this.b.release();
                this.b.channel().close();
                this.b = null;
            } catch (Throwable e) {
                DLog.e("LM", "Fail to release lock.", e);
            }
        }
    }
}
