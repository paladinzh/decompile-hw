package com.fyusion.sdk.viewer.internal.b.c;

import com.fyusion.sdk.common.h;
import com.fyusion.sdk.viewer.internal.b.b.f.b;
import com.fyusion.sdk.viewer.internal.b.c.g.a;
import com.fyusion.sdk.viewer.internal.b.e;
import java.io.File;

/* compiled from: Unknown */
public class c {
    private b a;

    public c(b bVar) {
        this.a = bVar;
    }

    private a<File> a(h hVar, String str, int i) {
        e dVar = new d(hVar.getId(), hVar.getUrl() + str);
        return new a(dVar, new com.fyusion.sdk.viewer.internal.b.a.b(dVar, this.a.a(), i));
    }

    private static String a(int i) {
        return "fyuse_h264_" + i + ".mp4";
    }

    private static String a(int i, int i2, boolean z) {
        return String.valueOf(i) + "x" + i2 + (!z ? "base" : "h444") + ".mp4";
    }

    public a<File> a(h hVar) {
        return a(hVar, "tween.magic", 2500);
    }

    public a<File> a(h hVar, int i, boolean z, boolean z2) {
        String a = !hVar.hasLowResolutionSlice() ? a(0) : !z ? a(hVar.getPreviewWidth(), hVar.getPreviewHeight(), z2) : a(i);
        return a(hVar, a, 5000);
    }
}
