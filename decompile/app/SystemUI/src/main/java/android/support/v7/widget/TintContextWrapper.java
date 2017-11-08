package android.support.v7.widget;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class TintContextWrapper extends ContextWrapper {
    private static final ArrayList<WeakReference<TintContextWrapper>> sCache = new ArrayList();
    private Resources mResources;
    private final Theme mTheme;

    public static Context wrap(@NonNull Context context) {
        if (!shouldWrap(context)) {
            return context;
        }
        TintContextWrapper wrapper;
        int count = sCache.size();
        for (int i = 0; i < count; i++) {
            WeakReference<TintContextWrapper> ref = (WeakReference) sCache.get(i);
            if (ref != null) {
                wrapper = (TintContextWrapper) ref.get();
            } else {
                wrapper = null;
            }
            if (wrapper != null && wrapper.getBaseContext() == context) {
                return wrapper;
            }
        }
        wrapper = new TintContextWrapper(context);
        sCache.add(new WeakReference(wrapper));
        return wrapper;
    }

    private static boolean shouldWrap(@NonNull Context context) {
        if ((context instanceof TintContextWrapper) || (context.getResources() instanceof TintResources) || (context.getResources() instanceof VectorEnabledTintResources)) {
            return false;
        }
        if (!AppCompatDelegate.isCompatVectorFromResourcesEnabled() || VERSION.SDK_INT <= 20) {
            return true;
        }
        return false;
    }

    private TintContextWrapper(@NonNull Context base) {
        super(base);
        if (VectorEnabledTintResources.shouldBeUsed()) {
            this.mTheme = getResources().newTheme();
            this.mTheme.setTo(base.getTheme());
            return;
        }
        this.mTheme = null;
    }

    public Theme getTheme() {
        return this.mTheme == null ? super.getTheme() : this.mTheme;
    }

    public void setTheme(int resid) {
        if (this.mTheme == null) {
            super.setTheme(resid);
        } else {
            this.mTheme.applyStyle(resid, true);
        }
    }

    public Resources getResources() {
        if (this.mResources == null) {
            Resources tintResources;
            if (this.mTheme == null) {
                tintResources = new TintResources(this, super.getResources());
            } else {
                tintResources = new VectorEnabledTintResources(this, super.getResources());
            }
            this.mResources = tintResources;
        }
        return this.mResources;
    }
}
