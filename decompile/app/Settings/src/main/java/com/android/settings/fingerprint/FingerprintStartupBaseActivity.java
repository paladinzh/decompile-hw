package com.android.settings.fingerprint;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.WindowInsets;
import android.widget.LinearLayout.LayoutParams;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;

public class FingerprintStartupBaseActivity extends Activity {
    private static String TAG = "FingerprintStartupBaseActivity";
    protected FingerStartupWindowListener mWindowListenr = new FingerStartupWindowListener();

    private static class FingerStartupWindowListener implements OnApplyWindowInsetsListener {
        int statusBarHeight;
        View windowView;

        private FingerStartupWindowListener() {
            this.windowView = null;
            this.statusBarHeight = 0;
        }

        public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
            if (this.windowView != null) {
                this.windowView.setPadding(0, this.statusBarHeight, 0, 0);
            }
            return insets;
        }
    }

    private class OnSystemUiVisibilityChangeListenerForFinger implements OnSystemUiVisibilityChangeListener {
        private OnSystemUiVisibilityChangeListenerForFinger() {
        }

        public void onSystemUiVisibilityChange(int arg0) {
            Utils.hideNavigationBar(FingerprintStartupBaseActivity.this.getWindow(), 5890);
        }
    }

    protected void setSystemUiVisibilityChangeListener() {
        try {
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListenerForFinger());
        } catch (Exception e) {
            Log.e(TAG, "setSystemUiVisibilityChangeListener()-->Exception");
        }
    }

    protected void prepareContentView() {
        setContentView(2130968801);
        int statusBarHeight = Utils.getStatusBarHeight(this);
        SettingsExtUtils.hideActionBarInStartupGuide(this);
        Utils.hideNavigationBar(getWindow(), 5890);
        setValidLayoutArea(statusBarHeight);
        setLogoViewHeight();
    }

    protected void onResume() {
        super.onResume();
        Utils.hideNavigationBar(getWindow(), 5890);
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (SettingsExtUtils.isStartupGuideMode(getContentResolver())) {
            Utils.hideNavigationBar(getWindow(), 5890);
        }
    }

    private void setValidLayoutArea(int statusBarHeight) {
        Log.d(TAG, "setValidLayoutArea statusBarHeight = " + statusBarHeight);
        View root = findViewById(16908290);
        root.setFitsSystemWindows(false);
        this.mWindowListenr.statusBarHeight = statusBarHeight;
        this.mWindowListenr.windowView = root;
        root.setOnApplyWindowInsetsListener(this.mWindowListenr);
    }

    private void setLogoViewHeight() {
        View logoView = findViewById(2131886352);
        View logoContainer = findViewById(2131886353);
        if (logoView != null && logoContainer != null) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int logoViewHeight;
            if (!Utils.isTablet()) {
                logoViewHeight = (displayMetrics.widthPixels * 4) / 5;
                logoView.getLayoutParams().height = logoViewHeight;
                ((LayoutParams) logoContainer.getLayoutParams()).topMargin = (logoViewHeight * 16) / 100;
            } else if (2 == getResources().getConfiguration().orientation) {
                descContainer = findViewById(2131886639);
                logoViewHeight = (displayMetrics.widthPixels * 268) / 1000;
                descContainerWidth = (displayMetrics.widthPixels * 5) / 6;
                logoView.getLayoutParams().height = logoViewHeight;
                descContainer.getLayoutParams().width = descContainerWidth;
                ((LayoutParams) logoContainer.getLayoutParams()).topMargin = (logoViewHeight * 13) / 100;
            } else {
                descContainer = findViewById(2131886639);
                logoViewHeight = (displayMetrics.widthPixels * 62) / 100;
                descContainerWidth = displayMetrics.widthPixels;
                logoView.getLayoutParams().height = logoViewHeight;
                descContainer.getLayoutParams().width = descContainerWidth;
                ((LayoutParams) logoContainer.getLayoutParams()).topMargin = (logoViewHeight * 38) / 100;
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLogoViewHeight();
    }
}
