package com.fyusion.sdk.common.ext.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import com.fyusion.sdk.common.ext.e;
import com.fyusion.sdk.common.ext.h;
import com.fyusion.sdk.common.ext.j;
import com.fyusion.sdk.common.ext.l;
import com.fyusion.sdk.common.i.b;
import com.fyusion.sdk.core.a.c;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import org.mtnwrw.pdqimg.ConversionService;
import org.mtnwrw.pdqimg.DecompressionService;
import org.mtnwrw.pdqimg.DecompressionService.ImageInformation;
import org.mtnwrw.pdqimg.PDQImage;

/* compiled from: Unknown */
public class a {
    public static Bitmap a(Bitmap bitmap, e eVar) {
        if (bitmap == null) {
            return null;
        }
        int i;
        Matrix matrix = new Matrix();
        if (eVar.getCameraOrientation() != 270) {
            i = 0;
        } else {
            boolean z = true;
        }
        float gravityX = eVar.getGravityX();
        i = Math.abs(gravityX) > Math.abs(eVar.getGravityY()) ? gravityX > 0.0f ? 180 : 0 : i == 0 ? 90 : -90;
        matrix.postRotate((float) i);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap a(File file) throws Exception {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        Options options2 = new Options();
        if (options.outWidth >= j.c.height) {
            options2.inSampleSize = 4;
            options2.inPreferredConfig = Config.RGB_565;
        }
        Bitmap decodeFile = BitmapFactory.decodeFile(file.getAbsolutePath(), options2);
        return decodeFile != null ? decodeFile : FyuseUtils.getThumbnail(file);
    }

    public static Bitmap a(File file, e eVar) throws Exception {
        return b(file, eVar);
    }

    public static Bitmap b(Bitmap bitmap, e eVar) throws FileNotFoundException {
        int i = 180;
        if (bitmap == null) {
            return null;
        }
        int i2;
        Matrix matrix = new Matrix();
        if (eVar.getCameraOrientation() != 270) {
            i2 = 0;
        } else {
            boolean z = true;
        }
        float gravityX = eVar.getGravityX();
        if (Math.abs(gravityX) > Math.abs(eVar.getGravityY())) {
            if (gravityX > 0.0f) {
                if (i2 != 0) {
                    i2 = 0;
                }
            } else if (i2 == 0) {
                i = 0;
            }
            i2 = i;
        } else {
            i2 = 90;
        }
        matrix.postRotate((float) i2);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private static Bitmap b(File file, e eVar) throws Exception {
        if (file.isDirectory()) {
            file = new File(file.getAbsoluteFile() + File.separator + j.ak);
        }
        l lVar = new l(file);
        if (eVar == null) {
            eVar = lVar.d();
        }
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        int i = options.outWidth;
        if (options.outHeight * i < ((int) (eVar.getProcessedSize().width * eVar.getProcessedSize().height))) {
            return c(file, eVar);
        }
        Bitmap decodeFile = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (decodeFile == null) {
            decodeFile = FyuseUtils.getThumbnail(file);
        }
        return decodeFile;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Bitmap c(File file, e eVar) throws Exception {
        ByteBuffer map;
        PDQImage pDQImage;
        Bitmap bitmap = null;
        h hVar = new h(file);
        byte[] bArr = new byte[64];
        if (hVar.a(com.fyusion.sdk.common.i.a.READ_ONLY, b.NONE)) {
            FileInputStream b = hVar.b(0);
            try {
                FileChannel channel = b.getChannel();
                try {
                    long position = channel.position();
                    if (b.read(bArr) >= 64) {
                        ImageInformation imageInformation = DecompressionService.getImageInformation(bArr);
                        map = channel.map(MapMode.READ_ONLY, position, (long) imageInformation.StreamSize);
                        pDQImage = new PDQImage(imageInformation.Width, imageInformation.Height, imageInformation.ImageType);
                        Bitmap createBitmap = Bitmap.createBitmap(imageInformation.Width, imageInformation.Height, Config.ARGB_8888);
                        DecompressionService.decompressImage(map, pDQImage);
                        pDQImage.swapUVChannels(false);
                        ConversionService.convertPDQImageToBitmap(pDQImage, createBitmap, false, false);
                        map.clear();
                        pDQImage.close();
                        bitmap = createBitmap;
                    }
                    if (channel != null) {
                        channel.close();
                    }
                } catch (Throwable th) {
                    Throwable th2 = th;
                    Throwable th3 = null;
                    if (channel != null) {
                        if (th3 == null) {
                            channel.close();
                        } else {
                            try {
                                channel.close();
                            } catch (Throwable th4) {
                                th3.addSuppressed(th4);
                            }
                        }
                    }
                    throw th2;
                }
            } catch (IOException e) {
                c cVar = new c();
                l lVar = new l(file);
                bitmap = (Bitmap) cVar.a(hVar.a((int) eVar.getProcessedSize().width, (int) eVar.getProcessedSize().height, eVar.getThumbnailIndex()), c.b.FULL_RESOLUTION).a();
            }
        }
        return a(bitmap, eVar);
    }
}
