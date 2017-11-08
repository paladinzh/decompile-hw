package com.fyusion.sdk.common.a;

import com.fyusion.sdk.common.s;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: Unknown */
public class a {
    private List<s> a = new ArrayList();
    private int b = 0;
    private int c = 0;

    public void a(int i, int i2) {
        this.b = i;
        this.c = i2;
        for (s a : this.a) {
            a.a(i, i2);
        }
    }

    public void a(GL10 gl10) {
        for (s a : this.a) {
            a.a(gl10);
        }
    }
}
