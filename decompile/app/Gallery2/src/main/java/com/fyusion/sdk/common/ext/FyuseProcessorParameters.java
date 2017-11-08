package com.fyusion.sdk.common.ext;

import com.fyusion.sdk.common.ext.internal.Settings;
import com.fyusion.sdk.common.ext.internal.b;
import com.fyusion.sdk.common.ext.internal.b.a;

/* compiled from: Unknown */
public class FyuseProcessorParameters extends b {
    private ZoomMode a;
    private boolean b;
    private boolean c;

    /* compiled from: Unknown */
    public static class Builder extends a {
        private ZoomMode b = ZoomMode.FULL_WITH_NONE_FOR_360;
        private boolean c = false;
        private boolean d = false;

        public FyuseProcessorParameters build() {
            return new FyuseProcessorParameters(this.a, this.b, this.d, this.c);
        }

        public Builder enableLoopClosure(boolean z) {
            this.d = z;
            return this;
        }

        public Builder forceCheckLoopClosure(boolean z) {
            this.c = z;
            return this;
        }

        public Builder zoomMode(ZoomMode zoomMode) {
            this.b = zoomMode;
            return this;
        }
    }

    private FyuseProcessorParameters(Settings settings, ZoomMode zoomMode, boolean z, boolean z2) {
        super(settings);
        this.a = zoomMode;
        this.b = z2;
        this.c = z;
    }

    public boolean getEnableLoopClosure() {
        return this.c;
    }

    public boolean getForceCheckLoopClosure() {
        return this.b;
    }

    public ZoomMode getZoomMode() {
        return this.a;
    }
}
