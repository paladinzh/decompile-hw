package android.support.v17.leanback.widget;

import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.view.View;

final class ForegroundHelper {
    static final ForegroundHelper sInstance = new ForegroundHelper();
    ForegroundHelperVersionImpl mImpl;

    interface ForegroundHelperVersionImpl {
        void setForeground(View view, Drawable drawable);
    }

    private static final class ForegroundHelperApi23Impl implements ForegroundHelperVersionImpl {
        private ForegroundHelperApi23Impl() {
        }

        public void setForeground(View view, Drawable drawable) {
            ForegroundHelperApi23.setForeground(view, drawable);
        }
    }

    private static final class ForegroundHelperStubImpl implements ForegroundHelperVersionImpl {
        private ForegroundHelperStubImpl() {
        }

        public void setForeground(View view, Drawable drawable) {
        }
    }

    private ForegroundHelper() {
        if (supportsForeground()) {
            this.mImpl = new ForegroundHelperApi23Impl();
        } else {
            this.mImpl = new ForegroundHelperStubImpl();
        }
    }

    public static ForegroundHelper getInstance() {
        return sInstance;
    }

    public static boolean supportsForeground() {
        return VERSION.SDK_INT >= 23;
    }

    public void setForeground(View view, Drawable drawable) {
        this.mImpl.setForeground(view, drawable);
    }
}
