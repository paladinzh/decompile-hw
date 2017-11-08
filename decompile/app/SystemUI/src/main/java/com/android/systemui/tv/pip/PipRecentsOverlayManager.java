package com.android.systemui.tv.pip;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import com.android.systemui.R;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.tv.pip.PipRecentsControlsView.Listener;

public class PipRecentsOverlayManager {
    private Callback mCallback;
    private boolean mHasFocusableInRecents;
    private boolean mIsPipFocusedInRecent;
    private boolean mIsPipRecentsOverlayShown;
    private boolean mIsRecentsShown;
    private View mOverlayView;
    private PipRecentsControlsView mPipControlsView;
    private Listener mPipControlsViewListener = new Listener() {
        public void onClosed() {
            if (PipRecentsOverlayManager.this.mCallback != null) {
                PipRecentsOverlayManager.this.mCallback.onClosed();
            }
        }

        public void onBackPressed() {
            if (PipRecentsOverlayManager.this.mCallback != null) {
                PipRecentsOverlayManager.this.mCallback.onBackPressed();
            }
        }
    };
    private final PipManager mPipManager = PipManager.getInstance();
    private LayoutParams mPipRecentsControlsViewFocusedLayoutParams;
    private LayoutParams mPipRecentsControlsViewLayoutParams;
    private View mRecentsView;
    private final SystemServicesProxy mSystemServicesProxy;
    private boolean mTalkBackEnabled;
    private final WindowManager mWindowManager;

    public interface Callback {
        void onBackPressed();

        void onClosed();

        void onRecentsFocused();
    }

    PipRecentsOverlayManager(Context context) {
        this.mWindowManager = (WindowManager) context.getSystemService(WindowManager.class);
        this.mSystemServicesProxy = SystemServicesProxy.getInstance(context);
        initViews(context);
    }

    private void initViews(Context context) {
        this.mOverlayView = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.tv_pip_recents_overlay, null);
        this.mPipControlsView = (PipRecentsControlsView) this.mOverlayView.findViewById(R.id.pip_controls);
        this.mRecentsView = this.mOverlayView.findViewById(R.id.recents);
        this.mRecentsView.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    PipRecentsOverlayManager.this.clearFocus();
                }
            }
        });
        this.mOverlayView.measure(0, 0);
        this.mPipRecentsControlsViewLayoutParams = new LayoutParams(this.mOverlayView.getMeasuredWidth(), this.mOverlayView.getMeasuredHeight(), 2008, 24, -3);
        this.mPipRecentsControlsViewLayoutParams.gravity = 49;
        this.mPipRecentsControlsViewFocusedLayoutParams = new LayoutParams(this.mOverlayView.getMeasuredWidth(), this.mOverlayView.getMeasuredHeight(), 2008, 0, -3);
        this.mPipRecentsControlsViewFocusedLayoutParams.gravity = 49;
    }

    void addPipRecentsOverlayView() {
        if (!this.mIsPipRecentsOverlayShown) {
            this.mTalkBackEnabled = this.mSystemServicesProxy.isTouchExplorationEnabled();
            this.mRecentsView.setVisibility(this.mTalkBackEnabled ? 0 : 8);
            this.mIsPipRecentsOverlayShown = true;
            this.mIsPipFocusedInRecent = true;
            this.mWindowManager.addView(this.mOverlayView, this.mPipRecentsControlsViewFocusedLayoutParams);
        }
    }

    public void removePipRecentsOverlayView() {
        if (this.mIsPipRecentsOverlayShown) {
            this.mWindowManager.removeView(this.mOverlayView);
            this.mPipControlsView.reset();
            this.mIsPipRecentsOverlayShown = false;
        }
    }

    public void requestFocus(boolean hasFocusableInRecents) {
        this.mHasFocusableInRecents = hasFocusableInRecents;
        if (this.mIsPipRecentsOverlayShown && this.mIsRecentsShown && !this.mIsPipFocusedInRecent && this.mPipManager.isPipShown()) {
            this.mIsPipFocusedInRecent = true;
            this.mPipControlsView.startFocusGainAnimation();
            this.mWindowManager.updateViewLayout(this.mOverlayView, this.mPipRecentsControlsViewFocusedLayoutParams);
            this.mPipManager.resizePinnedStack(4);
            if (this.mTalkBackEnabled) {
                this.mPipControlsView.requestFocus();
                this.mPipControlsView.sendAccessibilityEvent(8);
            }
        }
    }

    public void clearFocus() {
        if (this.mIsPipRecentsOverlayShown && this.mIsRecentsShown && this.mIsPipFocusedInRecent && this.mPipManager.isPipShown() && this.mHasFocusableInRecents) {
            this.mIsPipFocusedInRecent = false;
            this.mPipControlsView.startFocusLossAnimation();
            this.mWindowManager.updateViewLayout(this.mOverlayView, this.mPipRecentsControlsViewLayoutParams);
            this.mPipManager.resizePinnedStack(3);
            if (this.mCallback != null) {
                this.mCallback.onRecentsFocused();
            }
        }
    }

    public void setCallback(Callback listener) {
        Listener listener2 = null;
        this.mCallback = listener;
        PipRecentsControlsView pipRecentsControlsView = this.mPipControlsView;
        if (this.mCallback != null) {
            listener2 = this.mPipControlsViewListener;
        }
        pipRecentsControlsView.setListener(listener2);
    }

    public void onRecentsResumed() {
        if (this.mPipManager.isPipShown()) {
            this.mIsRecentsShown = true;
            this.mIsPipFocusedInRecent = true;
            this.mPipManager.resizePinnedStack(4);
        }
    }

    public void onRecentsPaused() {
        this.mIsRecentsShown = false;
        this.mIsPipFocusedInRecent = false;
        removePipRecentsOverlayView();
        if (this.mPipManager.isPipShown()) {
            this.mPipManager.resizePinnedStack(1);
        }
    }

    boolean isRecentsShown() {
        return this.mIsRecentsShown;
    }

    void onConfigurationChanged(Context context) {
        if (this.mIsRecentsShown) {
            Log.w("PipRecentsOverlayManager", "Configuration is changed while Recents is shown");
        }
        initViews(context);
    }
}
