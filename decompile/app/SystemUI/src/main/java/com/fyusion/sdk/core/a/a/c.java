package com.fyusion.sdk.core.a.a;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import com.fyusion.sdk.core.a.b;
import com.fyusion.sdk.core.a.g;
import com.fyusion.sdk.core.a.h;
import java.io.FileOutputStream;

/* compiled from: Unknown */
public class c implements g {
    public void a(b bVar, h hVar) throws Exception {
        Bitmap createBitmap = Bitmap.createBitmap(bVar.c(), bVar.d(), Config.ARGB_8888);
        createBitmap.copyPixelsFromBuffer(bVar.a());
        FileOutputStream c = hVar.c();
        createBitmap.compress(CompressFormat.JPEG, 85, c);
        hVar.a(c);
        hVar.b(0);
    }

    public boolean a(int i) {
        return i == 0;
    }
}
