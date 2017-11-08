package android.support.v17.leanback.widget;

import android.os.Build.VERSION;
import android.view.View;

final class ShadowHelper {
    static final ShadowHelper sInstance = new ShadowHelper();
    ShadowHelperVersionImpl mImpl;
    boolean mSupportsDynamicShadow;

    interface ShadowHelperVersionImpl {
        void setZ(View view, float f);
    }

    private static final class ShadowHelperApi21Impl implements ShadowHelperVersionImpl {
        private ShadowHelperApi21Impl() {
        }

        public void setZ(View view, float z) {
            ShadowHelperApi21.setZ(view, z);
        }
    }

    private static final class ShadowHelperStubImpl implements ShadowHelperVersionImpl {
        private ShadowHelperStubImpl() {
        }

        public void setZ(View view, float z) {
        }
    }

    private ShadowHelper() {
        if (VERSION.SDK_INT >= 21) {
            this.mSupportsDynamicShadow = true;
            this.mImpl = new ShadowHelperApi21Impl();
            return;
        }
        this.mImpl = new ShadowHelperStubImpl();
    }

    public static ShadowHelper getInstance() {
        return sInstance;
    }

    public boolean supportsDynamicShadow() {
        return this.mSupportsDynamicShadow;
    }

    public void setZ(View view, float z) {
        this.mImpl.setZ(view, z);
    }
}
