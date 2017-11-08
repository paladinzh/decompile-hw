package com.fyusion.sdk.common.ext.util.exif;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;

/* compiled from: Unknown */
class g {
    private final ExifInterface a;

    g(ExifInterface exifInterface) {
        this.a = exifInterface;
    }

    protected c a(InputStream inputStream) throws ExifInvalidFormatException, IOException {
        f a = f.a(inputStream, this.a);
        c cVar = new c(a.o());
        for (int a2 = a.a(); a2 != 5; a2 = a.a()) {
            ExifTag c;
            byte[] bArr;
            switch (a2) {
                case 0:
                    cVar.a(new h(a.d()));
                    break;
                case 1:
                    c = a.c();
                    if (!c.hasValue()) {
                        a.a(c);
                        break;
                    }
                    cVar.b(c.getIfd()).a(c);
                    break;
                case 2:
                    c = a.c();
                    if (c.getDataType() == (short) 7) {
                        a.b(c);
                    }
                    cVar.b(c.getIfd()).a(c);
                    break;
                case 3:
                    bArr = new byte[a.g()];
                    if (bArr.length == a.a(bArr)) {
                        cVar.a(bArr);
                        break;
                    }
                    Log.w("ExifReader", "Failed to read the compressed thumbnail");
                    break;
                case 4:
                    bArr = new byte[a.f()];
                    if (bArr.length == a.a(bArr)) {
                        cVar.a(a.e(), bArr);
                        break;
                    }
                    Log.w("ExifReader", "Failed to read the strip bytes");
                    break;
                default:
                    break;
            }
        }
        return cVar;
    }
}
