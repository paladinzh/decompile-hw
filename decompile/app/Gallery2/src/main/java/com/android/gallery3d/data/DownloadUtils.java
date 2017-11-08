package com.android.gallery3d.data;

import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.ThreadPool.CancelListener;
import com.android.gallery3d.util.ThreadPool.JobContext;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URL;

public class DownloadUtils {
    public static boolean download(com.android.gallery3d.util.ThreadPool.JobContext r5, java.net.URL r6, java.io.OutputStream r7) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:13:? in {4, 9, 10, 12, 14, 15} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r0 = 0;
        r0 = r6.openStream();	 Catch:{ Throwable -> 0x000d, all -> 0x0031 }
        dump(r5, r0, r7);	 Catch:{ Throwable -> 0x000d, all -> 0x0031 }
        r2 = 1;
        com.android.gallery3d.common.Utils.closeSilently(r0);
        return r2;
    L_0x000d:
        r1 = move-exception;
        r2 = "DownloadService";	 Catch:{ Throwable -> 0x000d, all -> 0x0031 }
        r3 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x000d, all -> 0x0031 }
        r3.<init>();	 Catch:{ Throwable -> 0x000d, all -> 0x0031 }
        r4 = "fail to download.";	 Catch:{ Throwable -> 0x000d, all -> 0x0031 }
        r3 = r3.append(r4);	 Catch:{ Throwable -> 0x000d, all -> 0x0031 }
        r4 = r1.getMessage();	 Catch:{ Throwable -> 0x000d, all -> 0x0031 }
        r3 = r3.append(r4);	 Catch:{ Throwable -> 0x000d, all -> 0x0031 }
        r3 = r3.toString();	 Catch:{ Throwable -> 0x000d, all -> 0x0031 }
        com.android.gallery3d.util.GalleryLog.w(r2, r3);	 Catch:{ Throwable -> 0x000d, all -> 0x0031 }
        r2 = 0;
        com.android.gallery3d.common.Utils.closeSilently(r0);
        return r2;
    L_0x0031:
        r2 = move-exception;
        com.android.gallery3d.common.Utils.closeSilently(r0);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.gallery3d.data.DownloadUtils.download(com.android.gallery3d.util.ThreadPool$JobContext, java.net.URL, java.io.OutputStream):boolean");
    }

    public static boolean requestDownload(JobContext jc, URL url, File file) {
        Throwable th;
        Closeable fos = null;
        try {
            Closeable fos2 = new FileOutputStream(file);
            try {
                boolean download = download(jc, url, fos2);
                Utils.closeSilently(fos2);
                return download;
            } catch (Throwable th2) {
                th = th2;
                fos = fos2;
                Utils.closeSilently(fos);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            Utils.closeSilently(fos);
            throw th;
        }
    }

    public static void dump(JobContext jc, InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[FragmentTransaction.TRANSIT_ENTER_MASK];
        int rc = is.read(buffer, 0, buffer.length);
        final Thread thread = Thread.currentThread();
        jc.setCancelListener(new CancelListener() {
            public void onCancel() {
                thread.interrupt();
            }
        });
        while (rc > 0) {
            if (jc.isCancelled()) {
                throw new InterruptedIOException();
            }
            os.write(buffer, 0, rc);
            rc = is.read(buffer, 0, buffer.length);
        }
        jc.setCancelListener(null);
        Thread.interrupted();
    }
}
