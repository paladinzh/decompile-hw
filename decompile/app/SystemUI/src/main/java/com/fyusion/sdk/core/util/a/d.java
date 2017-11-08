package com.fyusion.sdk.core.util.a;

import org.mtnwrw.pdqimg.PDQBuffer;
import org.mtnwrw.pdqimg.PDQBuffer.PDQBufferError;

/* compiled from: Unknown */
public class d extends a<PDQBuffer> {
    public static final d a = new d(5);

    private d(int i) {
        super(i);
    }

    protected /* synthetic */ Object a(int i) {
        return d(i);
    }

    protected boolean a(PDQBuffer pDQBuffer, int i) {
        boolean z = false;
        try {
            if (pDQBuffer.getBuffer().capacity() >= i) {
                z = true;
            }
            return z;
        } catch (PDQBufferError e) {
            e.printStackTrace();
            return false;
        }
    }

    protected PDQBuffer d(int i) {
        PDQBuffer pDQBuffer = null;
        try {
            pDQBuffer = PDQBuffer.allocateBuffer((long) i);
        } catch (PDQBufferError e) {
            e.printStackTrace();
        }
        return pDQBuffer;
    }
}
