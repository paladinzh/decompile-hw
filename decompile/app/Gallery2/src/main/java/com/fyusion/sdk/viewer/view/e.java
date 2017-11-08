package com.fyusion.sdk.viewer.view;

import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.m;
import com.fyusion.sdk.common.n;
import java.io.File;

/* compiled from: Unknown */
class e {
    private b a;

    /* compiled from: Unknown */
    interface b {
        void init(m mVar, n nVar);

        void init(File file, n nVar);

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

        public void init(m mVar, n nVar) {
        }

        public void init(File file, n nVar) {
        }

        public a queryBlendingInfoForFrameId(float f) {
            return null;
        }
    }

    private e() {
        try {
            this.a = (b) Class.forName("com.fyusion.sdk.viewer.view.NativeFrameBlender").newInstance();
            DLog.d("FyusionFrameBlender", "Use NativeFrameBlender");
        } catch (ClassNotFoundException e) {
            DLog.d("FyusionFrameBlender", "Use FakeFrameBlender");
            this.a = new c();
        } catch (Throwable e2) {
            DLog.e("FyusionFrameBlender", "Exception during FrameBlender instantiation", e2);
        }
    }

    e(m mVar, n nVar) {
        this();
        this.a.init(mVar, nVar);
    }

    e(File file, n nVar) {
        this();
        this.a.init(file, nVar);
    }

    public a a(float f) {
        return this.a.queryBlendingInfoForFrameId(f);
    }
}
