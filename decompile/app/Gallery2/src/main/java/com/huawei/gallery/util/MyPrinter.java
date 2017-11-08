package com.huawei.gallery.util;

import com.android.gallery3d.util.GalleryLog;
import java.io.PrintWriter;
import java.io.StringWriter;

public final class MyPrinter {
    private String mPre;
    private final String mTag;

    public MyPrinter(String tag, String pre) {
        this.mTag = tag;
        this.mPre = pre;
    }

    public MyPrinter(String tag) {
        this.mTag = tag;
        this.mPre = "debug-info : ";
    }

    public void d(Exception e) {
        d(this.mPre + toString(e));
    }

    public void d(String msg) {
        GalleryLog.d(this.mTag, this.mPre + msg);
    }

    public void d(boolean debug, String msg) {
        if (debug) {
            d(msg);
        }
    }

    public void d(String msg, Exception e) {
        GalleryLog.d(this.mTag, this.mPre + msg + "." + e.getMessage());
    }

    public void w(String msg) {
        GalleryLog.w(this.mTag, this.mPre + msg);
    }

    public void w(String msg, Exception e) {
        GalleryLog.w(this.mTag, this.mPre + msg + '\n' + toString(e));
    }

    public void e(String msg) {
        GalleryLog.e(this.mTag, this.mPre + msg);
    }

    public void e(String msg, Exception e) {
        GalleryLog.e(this.mTag, this.mPre + msg + "." + e.getMessage());
    }

    private String toString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
}
