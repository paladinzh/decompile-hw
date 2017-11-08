package com.android.systemui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.LocaleList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import com.android.systemui.compat.ActivityInfoWrapper;
import com.android.systemui.utils.HwLog;
import java.util.ArrayList;
import java.util.List;

public class AutoReinflateContainer extends FrameLayout {
    private int mDensity;
    private final List<InflateListener> mInflateListeners = new ArrayList();
    private Configuration mLastConfig = new Configuration();
    private final int mLayout;
    private LocaleList mLocaleList;

    public interface InflateListener {
        void onAllViewsRemoved();

        void onInflated(View view);
    }

    public AutoReinflateContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mDensity = context.getResources().getConfiguration().densityDpi;
        this.mLocaleList = context.getResources().getConfiguration().getLocales();
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.AutoReinflateContainer);
        if (a.hasValue(0)) {
            this.mLayout = a.getResourceId(0, 0);
            inflateLayout();
            return;
        }
        throw new IllegalArgumentException("AutoReinflateContainer must contain a layout");
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean shouldInflateLayout = false;
        int density = newConfig.densityDpi;
        if (density != this.mDensity) {
            this.mDensity = density;
            shouldInflateLayout = true;
        }
        LocaleList localeList = newConfig.getLocales();
        if (localeList != this.mLocaleList) {
            this.mLocaleList = localeList;
            shouldInflateLayout = true;
        }
        if (ActivityInfoWrapper.isThemeChanged(this.mLastConfig.updateFrom(newConfig))) {
            HwLog.i("AutoReinflateContainer", "onConfigurationChanged:theme changed");
            shouldInflateLayout = true;
        }
        if (shouldInflateLayout) {
            inflateLayout();
        }
    }

    private void inflateLayout() {
        int i;
        HwLog.i("AutoReinflateContainer", "inflateLayout");
        int N = this.mInflateListeners.size();
        for (i = 0; i < N; i++) {
            ((InflateListener) this.mInflateListeners.get(i)).onAllViewsRemoved();
        }
        removeAllViews();
        LayoutInflater.from(getContext()).inflate(this.mLayout, this);
        for (i = 0; i < N; i++) {
            ((InflateListener) this.mInflateListeners.get(i)).onInflated(getChildAt(0));
        }
    }

    public void addInflateListener(InflateListener listener) {
        this.mInflateListeners.add(listener);
        listener.onInflated(getChildAt(0));
    }

    public void onThemeChanged(Configuration newConfig) {
        onConfigurationChanged(newConfig);
    }
}
