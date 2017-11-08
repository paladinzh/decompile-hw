package com.fyusion.sdk.viewer.internal.f.a;

/* compiled from: Unknown */
public abstract class b {

    /* compiled from: Unknown */
    private static class a extends b {
        private volatile RuntimeException a;

        private a() {
            super();
        }

        void a(boolean z) {
            if (z) {
                this.a = new RuntimeException("Released");
            } else {
                this.a = null;
            }
        }

        public void b() {
            if (this.a != null) {
                throw new IllegalStateException("Already released", this.a);
            }
        }
    }

    private b() {
    }

    public static b a() {
        return new a();
    }

    abstract void a(boolean z);

    public abstract void b();
}
