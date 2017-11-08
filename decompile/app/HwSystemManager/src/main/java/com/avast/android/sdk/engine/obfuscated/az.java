package com.avast.android.sdk.engine.obfuscated;

import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

/* compiled from: Unknown */
public class az extends FileObserver {
    private static final Semaphore a = new Semaphore(0);
    private String b;
    private final Handler c;
    private String d = null;

    public az(Handler handler, String str, int i) {
        super(str, i | 1472);
        this.b = str;
        this.c = handler;
    }

    public static void a() {
        a.release();
    }

    public void a(String str) {
        this.b = str;
    }

    public void onEvent(int i, String str) {
        int i2 = -1;
        if ((i & 1) != 0) {
            i2 = 1;
        }
        if ((i & 8) != 0) {
            i2 = 8;
        }
        if ((i & 256) != 0) {
            i2 = 256;
        }
        if ((i & 1024) != 0) {
            i2 = 1024;
        }
        if ((i & 64) != 0) {
            i2 = 64;
        }
        if ((i & 128) != 0) {
            i2 = 128;
        }
        File file = new File(this.b + "/" + str);
        switch (i2) {
            case 1:
                if (file.isFile()) {
                    Message.obtain(this.c, 1, this.b + "/" + str).sendToTarget();
                    return;
                }
                return;
            case 8:
                if (file.isFile()) {
                    Message.obtain(this.c, 0, this.b + "/" + str).sendToTarget();
                    return;
                }
                return;
            case 64:
                this.d = this.b + "/" + str;
                return;
            case 128:
                if (file.isDirectory()) {
                    List linkedList = new LinkedList();
                    linkedList.add(this.d);
                    linkedList.add(this.b + "/" + str);
                    Message.obtain(this.c, 4, linkedList).sendToTarget();
                    try {
                        a.acquire();
                    } catch (InterruptedException e) {
                    }
                    this.d = null;
                    return;
                }
                this.d = null;
                Message.obtain(this.c, 0, this.b + "/" + str).sendToTarget();
                return;
            case 256:
                if (file.isDirectory()) {
                    Message.obtain(this.c, 2, this.b + "/" + str).sendToTarget();
                    return;
                }
                return;
            case 1024:
                Message.obtain(this.c, 3, this.b).sendToTarget();
                return;
            default:
                return;
        }
    }
}
