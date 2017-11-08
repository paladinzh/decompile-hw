package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.R;
import fyusion.vislib.BuildConfig;

public class EmptyShadeView extends StackScrollerDecorView {
    public EmptyShadeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ((TextView) findViewById(R.id.no_notifications)).setText(BuildConfig.FLAVOR);
    }

    protected View findContentView() {
        return findViewById(R.id.no_notifications);
    }
}
