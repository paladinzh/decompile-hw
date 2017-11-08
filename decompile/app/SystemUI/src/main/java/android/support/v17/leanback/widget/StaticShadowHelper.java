package android.support.v17.leanback.widget;

import android.os.Build.VERSION;

final class StaticShadowHelper {
    static final StaticShadowHelper sInstance = new StaticShadowHelper();
    ShadowHelperVersionImpl mImpl;
    boolean mSupportsShadow;

    interface ShadowHelperVersionImpl {
    }

    private static final class ShadowHelperJbmr2Impl implements ShadowHelperVersionImpl {
        private ShadowHelperJbmr2Impl() {
        }
    }

    private static final class ShadowHelperStubImpl implements ShadowHelperVersionImpl {
        private ShadowHelperStubImpl() {
        }
    }

    private StaticShadowHelper() {
        if (VERSION.SDK_INT >= 18) {
            this.mSupportsShadow = true;
            this.mImpl = new ShadowHelperJbmr2Impl();
            return;
        }
        this.mSupportsShadow = false;
        this.mImpl = new ShadowHelperStubImpl();
    }

    public static StaticShadowHelper getInstance() {
        return sInstance;
    }

    public boolean supportsShadow() {
        return this.mSupportsShadow;
    }
}
