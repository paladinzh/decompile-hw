package com.huawei.gallery.util;

import android.content.Context;
import android.os.Debug;
import android.os.Debug.MemoryInfo;
import com.android.gallery3d.util.GalleryLog;
import java.io.File;
import java.util.Calendar;

public class DumpHprofThread extends Thread {
    private Context mContext;
    private boolean mFirst = true;
    private int mLastPss = 0;
    private volatile boolean mStopped = false;

    public DumpHprofThread(Context context) {
        this.mContext = context;
        setName("DumpHprofThread");
    }

    public void run() {
        while (!this.mStopped) {
            MemoryInfo memInfo = new MemoryInfo();
            Debug.getMemoryInfo(memInfo);
            if (this.mFirst) {
                this.mLastPss = memInfo.getTotalPss();
                this.mFirst = false;
            } else if (memInfo.getTotalPss() - this.mLastPss >= 40960) {
                GalleryLog.w("DumpHprofThread", "dump gallery hprof pss:" + memInfo.getTotalPss());
                this.mLastPss = memInfo.getTotalPss();
                generateHprof(this.mContext, memInfo.getTotalPss());
            }
            try {
                if (this.mStopped) {
                    continue;
                } else {
                    synchronized (this) {
                        wait(60000);
                    }
                }
            } catch (InterruptedException e) {
            }
        }
    }

    private void generateHprof(Context context, int pss) {
        File file = new File(context.getExternalFilesDir(null), Calendar.getInstance().getTimeInMillis() + "-" + pss + ".hprof");
        try {
            long start = System.currentTimeMillis();
            Debug.dumpHprofData(file.getPath());
            GalleryLog.e("DumpHprofThread", "dump hprof time:" + (System.currentTimeMillis() - start) + "ms");
        } catch (Throwable e) {
            GalleryLog.w("DumpHprofThread", e);
        }
    }
}
