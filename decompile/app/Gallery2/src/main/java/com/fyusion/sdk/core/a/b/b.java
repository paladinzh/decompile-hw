package com.fyusion.sdk.core.a.b;

import android.annotation.SuppressLint;
import android.media.MediaFormat;
import com.fyusion.sdk.core.a.g;
import com.fyusion.sdk.core.a.h;
import com.fyusion.sdk.core.util.pool.c;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.mtnwrw.pdqimg.CompressionService;
import org.mtnwrw.pdqimg.CompressionService.quality;
import org.mtnwrw.pdqimg.PDQBuffer;
import org.mtnwrw.pdqimg.PDQBuffer.PDQBufferError;

/* compiled from: Unknown */
public class b implements g {
    MediaFormat a = new MediaFormat();

    static {
        CompressionService.initialize(Math.max(2, Runtime.getRuntime().availableProcessors()));
    }

    @SuppressLint({"InlinedApi"})
    public b() {
        this.a.setInteger("color-format", 2135033992);
    }

    public void a(com.fyusion.sdk.core.a.b bVar, h hVar) {
        PDQBuffer pDQBuffer;
        try {
            int c = bVar.c();
            int d = bVar.d();
            int e = bVar.e();
            if (!this.a.containsKey("width") || c != this.a.getInteger("width")) {
                this.a.setInteger("width", c);
                this.a.setInteger("stride", e);
            }
            if (!this.a.containsKey("height") || d != this.a.getInteger("height")) {
                this.a.setInteger("height", d);
                this.a.setInteger("slice-height", d);
            }
            pDQBuffer = (PDQBuffer) c.a.mustAcquire(bVar.a().capacity());
            if (CompressionService.compressMediaCodecBuffer(bVar.a(), this.a, quality.QUALITY_LOW, pDQBuffer)) {
                ByteBuffer buffer = pDQBuffer.getBuffer();
                hVar.a(buffer.limit());
                FileOutputStream c2 = hVar.c();
                c2.getChannel().write(buffer);
                hVar.a(c2);
            }
            c.a.release(pDQBuffer);
            hVar.b(1);
        } catch (PDQBufferError e2) {
            e2.printStackTrace();
        } catch (FileNotFoundException e3) {
        } catch (IOException e4) {
            e4.printStackTrace();
        } catch (Throwable th) {
            c.a.release(pDQBuffer);
        }
    }

    public boolean a(int i) {
        return i == 1;
    }
}
