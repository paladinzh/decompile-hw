package com.fyusion.sdk.core.util.pool;

import org.mtnwrw.pdqimg.PDQBuffer;
import org.mtnwrw.pdqimg.PDQBuffer.PDQBufferError;

/* compiled from: Unknown */
public class c extends a<PDQBuffer> {
    public static final c a = new c(5);

    private c(int i) {
        super(i);
    }

    protected /* synthetic */ Object a(int i) {
        return b(i);
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

    protected PDQBuffer b(int i) {
        PDQBuffer pDQBuffer = null;
        try {
            pDQBuffer = PDQBuffer.allocateBuffer((long) i);
        } catch (PDQBufferError e) {
            e.printStackTrace();
        }
        return pDQBuffer;
    }
}
