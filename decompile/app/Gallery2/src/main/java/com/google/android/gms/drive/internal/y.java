package com.google.android.gms.drive.internal;

import com.google.android.gms.internal.jz;
import com.google.android.gms.internal.ke;
import java.io.IOException;

/* compiled from: Unknown */
public final class y extends ke {
    public static final y[] DU = new y[0];
    public String DV = "";
    public long DW = -1;
    public long DX = -1;
    private int DY = -1;
    public int versionCode = 1;

    public void a(jz jzVar) throws IOException {
        jzVar.f(1, this.versionCode);
        jzVar.b(2, this.DV);
        jzVar.c(3, this.DW);
        jzVar.c(4, this.DX);
    }

    public int c() {
        int g = (((jz.g(1, this.versionCode) + 0) + jz.g(2, this.DV)) + jz.e(3, this.DW)) + jz.e(4, this.DX);
        this.DY = g;
        return g;
    }

    public int eW() {
        if (this.DY < 0) {
            c();
        }
        return this.DY;
    }
}
