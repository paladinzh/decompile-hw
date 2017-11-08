package com.android.deskclock;

import android.app.Activity;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Global;
import android.view.View;
import com.android.util.Log;
import com.android.util.Utils;

public class BaseActivity extends Activity {
    private int mCurOrientation;
    private OnGlobalLayoutListener mGlobalLayoutListener = new OnGlobalLayoutListener();
    private View mMianView = null;
    public int mNaviHeight = 144;
    public int mNaviWidth = 126;
    private ContentObserver navigationBarObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            BaseActivity.this.navigationBarChange(Utils.getNavigationbarStatus(BaseActivity.this));
        }
    };

    private class OnGlobalLayoutListener implements android.view.ViewTreeObserver.OnGlobalLayoutListener {
        private OnGlobalLayoutListener() {
        }

        public void onGlobalLayout() {
            BaseActivity.this.navigationBarChange(Utils.getNavigationbarStatus(BaseActivity.this));
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        this.mCurOrientation = getResources().getConfiguration().orientation;
        transAbStatusBar();
        transNavigationBar();
        super.onCreate(savedInstanceState);
        navigationBarChange(Utils.getNavigationbarStatus(this));
        getContentResolver().registerContentObserver(Global.getUriFor("navigationbar_is_min"), true, this.navigationBarObserver);
        this.mNaviHeight = getResources().getDimensionPixelSize(17104920);
        this.mNaviWidth = getResources().getDimensionPixelSize(17104922);
        if (this.mNaviHeight > 144) {
            if (VERSION.SDK_INT >= 23) {
                this.mNaviHeight = 126;
            } else {
                this.mNaviHeight = 144;
            }
        }
        if (this.mNaviWidth > 126) {
            this.mNaviWidth = 126;
        }
        Log.e("BaseActivity", "sdk int = " + VERSION.SDK_INT);
    }

    protected void onResume() {
        super.onResume();
        if (this.mMianView == null) {
            this.mMianView = getWindow().getDecorView();
        }
        if (this.mMianView != null) {
            this.mMianView.getViewTreeObserver().addOnGlobalLayoutListener(this.mGlobalLayoutListener);
        }
        navigationBarChange(Utils.getNavigationbarStatus(this));
    }

    protected void onPause() {
        super.onPause();
        if (this.mMianView != null) {
            this.mMianView.getViewTreeObserver().removeOnGlobalLayoutListener(this.mGlobalLayoutListener);
        }
    }

    protected void navigationBarChange(boolean isShow) {
    }

    protected void onDestroy() {
        getContentResolver().unregisterContentObserver(this.navigationBarObserver);
        super.onDestroy();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int oldOrientation = this.mCurOrientation;
        this.mCurOrientation = newConfig.orientation;
        if (this.mCurOrientation != oldOrientation) {
            navigationBarChange(Utils.getNavigationbarStatus(this));
        }
    }

    protected void transAbStatusBar() {
        if (VERSION.SDK_INT >= 19) {
            getWindow().setFlags(134217728, 134217728);
        }
    }

    protected void transNavigationBar() {
        if (VERSION.SDK_INT >= 19) {
            getWindow().setFlags(67108864, 67108864);
        }
    }
}
