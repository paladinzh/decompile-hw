package com.fyusion.sdk.common.internal;

import android.support.annotation.NonNull;
import com.fyusion.sdk.common.o;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: Unknown */
public class a {
    private List<o> a = new ArrayList();
    private int b = 0;
    private int c = 0;

    public void a(int i, int i2) {
        this.b = i;
        this.c = i2;
        for (o a : this.a) {
            a.a(i, i2);
        }
    }

    public void a(@NonNull o oVar) {
        oVar.a(this.b, this.c);
        this.a.add(oVar);
    }

    public void a(GL10 gl10) {
        for (o a : this.a) {
            a.a(gl10);
        }
    }
}
