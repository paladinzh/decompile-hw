package com.fyusion.sdk.viewer.view;

import com.fyusion.sdk.common.h;
import com.fyusion.sdk.common.o;
import com.fyusion.sdk.common.p;
import java.io.File;

/* compiled from: Unknown */
class e {
    private b a;

    /* compiled from: Unknown */
    interface b {
        void init(o oVar, p pVar);

        void init(File file, p pVar);

        a queryBlendingInfoForFrameId(float f);
    }

    /* compiled from: Unknown */
    public static class a {
        public int a;
        public float b;
        public com.fyusion.sdk.common.c.b c;
        public int d;
        public float e;
        public com.fyusion.sdk.common.c.b f;
    }

    /* compiled from: Unknown */
    static class c implements b {
        c() {
        }

        public void init(o oVar, p pVar) {
        }

        public void init(File file, p pVar) {
        }

        public a queryBlendingInfoForFrameId(float f) {
            return null;
        }
    }

    private e() {
        try {
            this.a = (b) Class.forName("com.fyusion.sdk.viewer.view.NativeFrameBlender").newInstance();
            h.a("FyusionFrameBlender", "Use NativeFrameBlender");
        } catch (ClassNotFoundException e) {
            h.a("FyusionFrameBlender", "Use FakeFrameBlender");
            this.a = new c();
        } catch (Throwable e2) {
            h.c("FyusionFrameBlender", "Exception during FrameBlender instantiation", e2);
        }
    }

    e(o oVar, p pVar) {
        this();
        this.a.init(oVar, pVar);
    }

    e(File file, p pVar) {
        this();
        this.a.init(file, pVar);
    }

    public a a(float f) {
        return this.a.queryBlendingInfoForFrameId(f);
    }
}
