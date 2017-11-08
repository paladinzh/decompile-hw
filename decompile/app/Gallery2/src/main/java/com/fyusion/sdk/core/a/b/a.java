package com.fyusion.sdk.core.a.b;

import android.util.Log;
import com.fyusion.sdk.core.a.d;
import com.fyusion.sdk.core.a.f;
import com.fyusion.sdk.core.a.h;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import org.mtnwrw.pdqimg.DecompressionService;
import org.mtnwrw.pdqimg.DecompressionService.DecompressError;
import org.mtnwrw.pdqimg.PDQImage;

/* compiled from: Unknown */
public class a implements f<PDQImage> {
    static {
        DecompressionService.initialize(Math.min(2, Runtime.getRuntime().availableProcessors()));
    }

    public d a(h hVar, PDQImage pDQImage, int i, int i2) {
        FileInputStream d;
        Throwable e;
        FileInputStream fileInputStream;
        DecompressError e2;
        try {
            d = hVar.d();
            try {
                PDQImage decompressImage;
                c cVar;
                long position = d.getChannel().position();
                int g = hVar.g();
                ByteBuffer map = d.getChannel().map(MapMode.READ_ONLY, position, (long) g);
                if (pDQImage == null) {
                    decompressImage = DecompressionService.decompressImage(map, 0, g, i, i2);
                } else {
                    DecompressionService.decompressImage(map, pDQImage);
                    decompressImage = pDQImage;
                }
                if (decompressImage == null) {
                    Log.w("PDQDecoder", "Unexpected null image, reusableObj: " + pDQImage);
                    cVar = null;
                } else {
                    cVar = new c(decompressImage);
                }
                hVar.a(d);
                return cVar;
            } catch (IOException e3) {
                e = e3;
                fileInputStream = d;
                try {
                    Log.w("PDQDecoder", "Index out of bounds, maybe the file was deleted", e);
                    hVar.a(fileInputStream);
                    return null;
                } catch (Throwable th) {
                    e = th;
                    d = fileInputStream;
                    hVar.a(d);
                    throw e;
                }
            } catch (DecompressError e4) {
                e2 = e4;
                try {
                    e2.printStackTrace();
                    hVar.a(d);
                    return null;
                } catch (Throwable th2) {
                    e = th2;
                    hVar.a(d);
                    throw e;
                }
            }
        } catch (IOException e5) {
            e = e5;
            fileInputStream = null;
            Log.w("PDQDecoder", "Index out of bounds, maybe the file was deleted", e);
            hVar.a(fileInputStream);
            return null;
        } catch (DecompressError e6) {
            e2 = e6;
            d = null;
            e2.printStackTrace();
            hVar.a(d);
            return null;
        } catch (Throwable th3) {
            e = th3;
            d = null;
            hVar.a(d);
            throw e;
        }
    }

    public boolean a(int i) {
        return i == 1;
    }
}
