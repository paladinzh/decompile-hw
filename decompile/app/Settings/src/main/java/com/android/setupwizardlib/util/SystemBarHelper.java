package com.android.setupwizardlib.util;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.os.Build.VERSION;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager.LayoutParams;

public class SystemBarHelper {

    private interface OnDecorViewInstalledListener {
        void onDecorViewInstalled(View view);
    }

    private static class DecorViewFinder {
        private OnDecorViewInstalledListener mCallback;
        private Runnable mCheckDecorViewRunnable;
        private final Handler mHandler;
        private int mRetries;
        private Window mWindow;

        private DecorViewFinder() {
            this.mHandler = new Handler();
            this.mCheckDecorViewRunnable = new Runnable() {
                public void run() {
                    View decorView = DecorViewFinder.this.mWindow.peekDecorView();
                    if (decorView != null) {
                        DecorViewFinder.this.mCallback.onDecorViewInstalled(decorView);
                        return;
                    }
                    DecorViewFinder decorViewFinder = DecorViewFinder.this;
                    decorViewFinder.mRetries = decorViewFinder.mRetries - 1;
                    if (DecorViewFinder.this.mRetries >= 0) {
                        DecorViewFinder.this.mHandler.post(DecorViewFinder.this.mCheckDecorViewRunnable);
                    } else {
                        Log.w("SystemBarHelper", "Cannot get decor view of window: " + DecorViewFinder.this.mWindow);
                    }
                }
            };
        }

        public void getDecorView(Window window, OnDecorViewInstalledListener callback, int retries) {
            this.mWindow = window;
            this.mRetries = retries;
            this.mCallback = callback;
            this.mCheckDecorViewRunnable.run();
        }
    }

    @TargetApi(21)
    private static class WindowInsetsListener implements OnApplyWindowInsetsListener {
        private int mBottomOffset;
        private boolean mHasCalculatedBottomOffset;

        private WindowInsetsListener() {
            this.mHasCalculatedBottomOffset = false;
        }

        public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
            if (!this.mHasCalculatedBottomOffset) {
                this.mBottomOffset = SystemBarHelper.getBottomDistance(view);
                this.mHasCalculatedBottomOffset = true;
            }
            int bottomInset = insets.getSystemWindowInsetBottom();
            int bottomMargin = Math.max(insets.getSystemWindowInsetBottom() - this.mBottomOffset, 0);
            MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
            if (bottomMargin < lp.bottomMargin + view.getHeight()) {
                lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, bottomMargin);
                view.setLayoutParams(lp);
                bottomInset = 0;
            }
            return insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), bottomInset);
        }
    }

    public static void hideSystemBars(Dialog dialog) {
        if (VERSION.SDK_INT >= 21) {
            Window window = dialog.getWindow();
            temporarilyDisableDialogFocus(window);
            addVisibilityFlag(window, 4098);
            addImmersiveFlagsToDecorView(window, 4098);
            window.setNavigationBarColor(0);
            window.setStatusBarColor(0);
        }
    }

    public static void hideSystemBars(Window window) {
        if (VERSION.SDK_INT >= 21) {
            addVisibilityFlag(window, 5634);
            addImmersiveFlagsToDecorView(window, 5634);
            window.setNavigationBarColor(0);
            window.setStatusBarColor(0);
        }
    }

    public static void addVisibilityFlag(View view, int flag) {
        if (VERSION.SDK_INT >= 11) {
            view.setSystemUiVisibility(view.getSystemUiVisibility() | flag);
        }
    }

    public static void addVisibilityFlag(Window window, int flag) {
        if (VERSION.SDK_INT >= 11) {
            LayoutParams attrs = window.getAttributes();
            attrs.systemUiVisibility |= flag;
            window.setAttributes(attrs);
        }
    }

    public static void setImeInsetView(View view) {
        if (VERSION.SDK_INT >= 21) {
            view.setOnApplyWindowInsetsListener(new WindowInsetsListener());
        }
    }

    @TargetApi(21)
    private static void addImmersiveFlagsToDecorView(Window window, final int vis) {
        getDecorView(window, new OnDecorViewInstalledListener() {
            public void onDecorViewInstalled(View decorView) {
                SystemBarHelper.addVisibilityFlag(decorView, vis);
            }
        });
    }

    private static void getDecorView(Window window, OnDecorViewInstalledListener callback) {
        new DecorViewFinder().getDecorView(window, callback, 3);
    }

    private static void temporarilyDisableDialogFocus(final Window window) {
        window.setFlags(8, 8);
        window.setSoftInputMode(256);
        new Handler().post(new Runnable() {
            public void run() {
                window.clearFlags(8);
            }
        });
    }

    private static int getBottomDistance(View view) {
        int[] coords = new int[2];
        view.getLocationInWindow(coords);
        return (view.getRootView().getHeight() - coords[1]) - view.getHeight();
    }
}
