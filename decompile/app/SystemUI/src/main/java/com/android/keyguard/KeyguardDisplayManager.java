package com.android.keyguard;

import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Point;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import android.media.MediaRouter.SimpleCallback;
import android.os.Bundle;
import android.util.Slog;
import android.view.Display;
import android.view.View;
import android.view.WindowManager.InvalidDisplayException;

public class KeyguardDisplayManager {
    private static boolean DEBUG = true;
    private Context mContext;
    private MediaRouter mMediaRouter;
    private final SimpleCallback mMediaRouterCallback = new SimpleCallback() {
        public void onRouteSelected(MediaRouter router, int type, RouteInfo info) {
            if (KeyguardDisplayManager.DEBUG) {
                Slog.d("KeyguardDisplayManager", "onRouteSelected: type=" + type + ", info=" + info);
            }
            KeyguardDisplayManager.this.updateDisplays(KeyguardDisplayManager.this.mShowing);
        }

        public void onRouteUnselected(MediaRouter router, int type, RouteInfo info) {
            if (KeyguardDisplayManager.DEBUG) {
                Slog.d("KeyguardDisplayManager", "onRouteUnselected: type=" + type + ", info=" + info);
            }
            KeyguardDisplayManager.this.updateDisplays(KeyguardDisplayManager.this.mShowing);
        }

        public void onRoutePresentationDisplayChanged(MediaRouter router, RouteInfo info) {
            if (KeyguardDisplayManager.DEBUG) {
                Slog.d("KeyguardDisplayManager", "onRoutePresentationDisplayChanged: info=" + info);
            }
            KeyguardDisplayManager.this.updateDisplays(KeyguardDisplayManager.this.mShowing);
        }
    };
    private OnDismissListener mOnDismissListener = new OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            KeyguardDisplayManager.this.mPresentation = null;
        }
    };
    Presentation mPresentation;
    private boolean mShowing;

    private static final class KeyguardPresentation extends Presentation {
        private View mClock;
        private int mMarginLeft;
        private int mMarginTop;
        Runnable mMoveTextRunnable = new Runnable() {
            public void run() {
                int y = KeyguardPresentation.this.mMarginTop + ((int) (Math.random() * ((double) (KeyguardPresentation.this.mUsableHeight - KeyguardPresentation.this.mClock.getHeight()))));
                KeyguardPresentation.this.mClock.setTranslationX((float) (KeyguardPresentation.this.mMarginLeft + ((int) (Math.random() * ((double) (KeyguardPresentation.this.mUsableWidth - KeyguardPresentation.this.mClock.getWidth()))))));
                KeyguardPresentation.this.mClock.setTranslationY((float) y);
                KeyguardPresentation.this.mClock.postDelayed(KeyguardPresentation.this.mMoveTextRunnable, 10000);
            }
        };
        private int mUsableHeight;
        private int mUsableWidth;

        public KeyguardPresentation(Context context, Display display, int theme) {
            super(context, display, theme);
            getWindow().setType(2009);
        }

        public void onDetachedFromWindow() {
            this.mClock.removeCallbacks(this.mMoveTextRunnable);
        }

        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Point p = new Point();
            getDisplay().getSize(p);
            this.mUsableWidth = (p.x * 80) / 100;
            this.mUsableHeight = (p.y * 80) / 100;
            this.mMarginLeft = (p.x * 20) / 200;
            this.mMarginTop = (p.y * 20) / 200;
            setContentView(R$layout.keyguard_presentation);
            this.mClock = findViewById(R$id.clock);
            this.mClock.post(this.mMoveTextRunnable);
        }
    }

    public KeyguardDisplayManager(Context context) {
        this.mContext = context;
        this.mMediaRouter = (MediaRouter) this.mContext.getSystemService("media_router");
    }

    public void show() {
        if (!this.mShowing) {
            if (DEBUG) {
                Slog.v("KeyguardDisplayManager", "show");
            }
            this.mMediaRouter.addCallback(4, this.mMediaRouterCallback, 8);
            updateDisplays(true);
        }
        this.mShowing = true;
    }

    public void hide() {
        if (this.mShowing) {
            if (DEBUG) {
                Slog.v("KeyguardDisplayManager", "hide");
            }
            this.mMediaRouter.removeCallback(this.mMediaRouterCallback);
            updateDisplays(false);
        }
        this.mShowing = false;
    }

    protected void updateDisplays(boolean showing) {
        boolean useDisplay = true;
        if (showing) {
            RouteInfo route = this.mMediaRouter.getSelectedRoute(4);
            if (route == null) {
                useDisplay = false;
            } else if (route.getPlaybackType() != 1) {
                useDisplay = false;
            }
            Display presentationDisplay = useDisplay ? route.getPresentationDisplay() : null;
            if (!(this.mPresentation == null || this.mPresentation.getDisplay() == presentationDisplay)) {
                if (DEBUG) {
                    Slog.v("KeyguardDisplayManager", "Display gone: " + this.mPresentation.getDisplay());
                }
                this.mPresentation.dismiss();
                this.mPresentation = null;
            }
            if (this.mPresentation == null && presentationDisplay != null) {
                if (DEBUG) {
                    Slog.i("KeyguardDisplayManager", "Keyguard enabled on display: " + presentationDisplay);
                }
                this.mPresentation = new KeyguardPresentation(this.mContext, presentationDisplay, R$style.keyguard_presentation_theme);
                this.mPresentation.setOnDismissListener(this.mOnDismissListener);
                try {
                    this.mPresentation.show();
                } catch (InvalidDisplayException ex) {
                    Slog.w("KeyguardDisplayManager", "Invalid display:", ex);
                    this.mPresentation = null;
                }
            }
        } else if (this.mPresentation != null) {
            this.mPresentation.dismiss();
            this.mPresentation = null;
        }
    }
}
