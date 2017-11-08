package com.android.gallery3d.ui;

import android.content.Context;
import android.graphics.Movie;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.LocalMediaItem;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.PhotoShareImage;
import com.android.gallery3d.data.UriImage;
import com.android.gallery3d.util.GalleryLog;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.InputStream;

public class GifParseThread extends Thread {
    private Context mContext;
    private MediaItem mMediaItem;
    private ScreenNail mScreenNail;
    private TileImageView mTileImageView;

    public GifParseThread(Context context, MediaItem mediaItem, TileImageView tileImageView, ScreenNail screenNail) {
        this.mContext = context;
        this.mMediaItem = mediaItem;
        this.mTileImageView = tileImageView;
        this.mScreenNail = screenNail;
    }

    private byte[] streamToBytes(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(FragmentTransaction.TRANSIT_ENTER_MASK);
        byte[] buffer = new byte[FragmentTransaction.TRANSIT_ENTER_MASK];
        while (is != null) {
            try {
                int len = is.read(buffer);
                if (len < 0) {
                    break;
                }
                os.write(buffer, 0, len);
            } catch (Exception e) {
                GalleryLog.i("GifParseThread", "InputStream.read() failed in streamToBytes() method.");
            }
        }
        return os.toByteArray();
    }

    public void run() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:37)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:61)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r28 = this;
        r0 = r28;
        r0 = r0.mScreenNail;
        r23 = r0;
        r0 = r23;
        r0 = r0 instanceof com.android.gallery3d.ui.AbstractGifScreenNail;
        r23 = r0;
        if (r23 == 0) goto L_0x0016;
    L_0x000e:
        r0 = r28;
        r0 = r0.mTileImageView;
        r23 = r0;
        if (r23 != 0) goto L_0x0017;
    L_0x0016:
        return;
    L_0x0017:
        r0 = r28;
        r2 = r0.mScreenNail;
        r2 = (com.android.gallery3d.ui.AbstractGifScreenNail) r2;
        r24 = 500; // 0x1f4 float:7.0E-43 double:2.47E-321;
        java.lang.Thread.sleep(r24);	 Catch:{ InterruptedException -> 0x002b, Exception -> 0x0029 }
        r9 = r28.getGifMovie();
        if (r9 != 0) goto L_0x0036;
    L_0x0028:
        return;
    L_0x0029:
        r6 = move-exception;
        return;
    L_0x002b:
        r7 = move-exception;
        r23 = "GifParseThread";
        r24 = "Thread.sleep() failed in run() method, reason: InterruptedException.";
        com.android.gallery3d.util.GalleryLog.i(r23, r24);
        return;
    L_0x0036:
        r11 = r9.duration();
        if (r11 > 0) goto L_0x003d;
    L_0x003c:
        return;
    L_0x003d:
        r16 = android.os.SystemClock.uptimeMillis();
        r8 = 0;
        r3 = 0;
        r18 = 0;
        r20 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r21 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r22 = r9.width();	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r10 = r9.height();	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r19 = r22 * r10;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r23 = 1048576; // 0x100000 float:1.469368E-39 double:5.180654E-318;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r0 = r19;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r1 = r23;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        if (r0 <= r1) goto L_0x0113;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
    L_0x005b:
        r13 = r22;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r12 = r10;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r22 = r2.getWidth();	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r10 = r2.getHeight();	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r0 = r22;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r0 = (float) r0;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r23 = r0;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r0 = (float) r13;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r24 = r0;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r20 = r23 / r24;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r0 = (float) r10;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r23 = r0;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r0 = (float) r12;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r24 = r0;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r21 = r23 / r24;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r23 = "GifParseThread";	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r24 = "ScreenNail[%s,%s],Movie[%s,%s], scale[%s,%s]";	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r25 = 6;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r0 = r25;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r0 = new java.lang.Object[r0];	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r25 = r0;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r26 = java.lang.Integer.valueOf(r22);	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r27 = 0;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r25[r27] = r26;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r26 = java.lang.Integer.valueOf(r10);	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r27 = 1;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r25[r27] = r26;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r26 = java.lang.Integer.valueOf(r13);	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r27 = 2;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r25[r27] = r26;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r26 = java.lang.Integer.valueOf(r12);	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r27 = 3;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r25[r27] = r26;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r26 = java.lang.Float.valueOf(r20);	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r27 = 4;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r25[r27] = r26;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r26 = java.lang.Float.valueOf(r21);	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r27 = 5;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r25[r27] = r26;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r24 = java.lang.String.format(r24, r25);	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        com.android.gallery3d.util.GalleryLog.i(r23, r24);	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r18 = 1;
        r4 = r3;
    L_0x00c0:
        r23 = java.lang.Thread.interrupted();	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
        if (r23 != 0) goto L_0x0122;	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
    L_0x00c6:
        r14 = android.os.SystemClock.uptimeMillis();	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
        r24 = r14 - r16;	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
        r0 = (long) r11;	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
        r26 = r0;	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
        r24 = r24 % r26;	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
        r0 = r24;	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
        r5 = (int) r0;	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
        r23 = r9.setTime(r5);	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
        if (r23 == 0) goto L_0x012b;	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
    L_0x00da:
        r0 = r22;	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
        r8 = r2.dequeue(r0, r10);	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
        if (r8 == 0) goto L_0x0129;	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
    L_0x00e2:
        r23 = 0;	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
        r0 = r23;	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
        r8.eraseColor(r0);	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
        r3 = new android.graphics.Canvas;	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
        r3.<init>(r8);	 Catch:{ InterruptedException -> 0x0123, Exception -> 0x0126 }
        if (r18 == 0) goto L_0x00f7;
    L_0x00f0:
        r0 = r20;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r1 = r21;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r3.scale(r0, r1);	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
    L_0x00f7:
        r23 = 0;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r24 = 0;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r0 = r23;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r1 = r24;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r9.draw(r3, r0, r1);	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r2.enqueue(r8);	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
    L_0x0105:
        r0 = r28;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r0 = r0.mTileImageView;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r23 = r0;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        r23.invalidate();	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
    L_0x010e:
        r24 = 33;	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
        java.lang.Thread.sleep(r24);	 Catch:{ InterruptedException -> 0x0117, Exception -> 0x0115 }
    L_0x0113:
        r4 = r3;
        goto L_0x00c0;
    L_0x0115:
        r6 = move-exception;
    L_0x0116:
        return;
    L_0x0117:
        r7 = move-exception;
    L_0x0118:
        r23 = "GifParseThread";
        r24 = "Thread.sleep() failed in run() method. reason: InterruptedException.";
        com.android.gallery3d.util.GalleryLog.i(r23, r24);
        return;
    L_0x0122:
        return;
    L_0x0123:
        r7 = move-exception;
        r3 = r4;
        goto L_0x0118;
    L_0x0126:
        r6 = move-exception;
        r3 = r4;
        goto L_0x0116;
    L_0x0129:
        r3 = r4;
        goto L_0x0105;
    L_0x012b:
        r3 = r4;
        goto L_0x010e;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.gallery3d.ui.GifParseThread.run():void");
    }

    private Movie getGifMovie() {
        Closeable closeable = null;
        Movie gifMovie = null;
        try {
            if (this.mMediaItem instanceof LocalMediaItem) {
                closeable = new FileInputStream(this.mMediaItem.filePath);
            } else if (this.mMediaItem instanceof UriImage) {
                closeable = this.mContext.getContentResolver().openInputStream(this.mMediaItem.getContentUri());
            } else if (this.mMediaItem instanceof PhotoShareImage) {
                Object is = new FileInputStream(this.mMediaItem.getFilePath());
            }
            byte[] byteArray = streamToBytes(closeable);
            gifMovie = Movie.decodeByteArray(byteArray, 0, byteArray.length);
        } catch (Exception e) {
            GalleryLog.i("GifParseThread", "An exception has occurred in run() method." + e.getMessage());
        } finally {
            Utils.closeSilently(closeable);
        }
        return gifMovie;
    }
}
