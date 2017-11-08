package com.a.a;

import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public class d implements p {
    private int a;
    private int b;
    private final int c;
    private final float d;

    public d() {
        this(2500, 0, WMElement.CAMERASIZEVALUE1B1);
    }

    public d(int i, int i2, float f) {
        this.a = i;
        this.c = i2;
        this.d = f;
    }

    public int a() {
        return this.a;
    }

    public void a(s sVar) throws s {
        this.b++;
        this.a = (int) (((float) this.a) + (((float) this.a) * this.d));
        if (!b()) {
            throw sVar;
        }
    }

    protected boolean b() {
        return this.b <= this.c;
    }
}
