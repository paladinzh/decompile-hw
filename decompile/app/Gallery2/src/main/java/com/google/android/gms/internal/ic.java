package com.google.android.gms.internal;

import com.google.android.gms.internal.hx.a;
import java.util.concurrent.BlockingQueue;

/* compiled from: Unknown */
public class ic {
    private static int OB = 10000;
    private static int OC = 1000;
    private final String OD;
    private final BlockingQueue<a> OE;
    private final int Ou;

    public void a(a.a aVar) {
        aVar.aK(this.OD);
        aVar.bv(this.Ou);
        this.OE.offer(aVar.gJ());
    }
}
