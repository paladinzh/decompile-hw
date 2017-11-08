package com.fyusion.sdk.camera.impl;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.fyusion.sdk.camera.SnapShot;
import com.fyusion.sdk.camera.SnapShotCallback;
import com.fyusion.sdk.common.DLog;
import com.huawei.watermark.manager.parse.WMElement;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/* compiled from: Unknown */
public final class n {
    private static final String a = n.class.getSimpleName();
    private final int b;
    private final int c;
    private final boolean d;
    private SnapShotCallback e;
    private String f;
    private int g = 17;

    public n(int i, int i2, boolean z) {
        this.b = i;
        this.c = i2;
        this.d = z;
    }

    private Bitmap a(Bitmap bitmap, int i) {
        Matrix matrix = new Matrix();
        matrix.setValues(new float[]{GroundOverlayOptions.NO_DIMENSION, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, 0.0f, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1});
        matrix.postRotate((float) i);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void a(Bitmap bitmap) throws IOException {
        OutputStream fileOutputStream = new FileOutputStream(new File(this.f));
        bitmap.compress(CompressFormat.JPEG, 100, fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    private void a(final byte[] bArr, final int i, final int i2) {
        new Thread(new Runnable(this) {
            final /* synthetic */ n d;

            public void run() {
                SnapShot snapShot = new SnapShot();
                Object obj = new byte[bArr.length];
                System.arraycopy(bArr, 0, obj, 0, bArr.length);
                snapShot.setImageData(obj);
                snapShot.setWidth(i);
                snapShot.setHeight(i2);
                snapShot.setImageFormat(this.d.g);
                switch (this.d.g) {
                    case 17:
                        this.d.e.snapShotInfo(snapShot);
                        return;
                    case 256:
                        try {
                            YuvImage yuvImage = new YuvImage(snapShot.getImageData(), 17, snapShot.getWidth(), snapShot.getHeight(), null);
                            OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            yuvImage.compressToJpeg(new Rect(0, 0, snapShot.getWidth(), snapShot.getHeight()), 100, byteArrayOutputStream);
                            this.d.a(this.d.a(BitmapFactory.decodeByteArray(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size()), this.d.b()));
                            DLog.d(n.a, "Created JPEG  snapshot : " + this.d.f);
                            this.d.e.snapShotInfo(snapShot);
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                    default:
                        return;
                }
            }
        }).start();
    }

    private int b() {
        return !this.d ? 0 : 270;
    }

    public void a(SnapShotCallback snapShotCallback) {
        this.e = snapShotCallback;
    }

    public void a(byte[] bArr) {
        this.g = 17;
        a(bArr, this.b, this.c);
    }
}
