package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.session.MediaSessionLegacyHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.InputQueue.Callback;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder.Callback2;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.view.WindowManagerGlobal;
import android.widget.FrameLayout;
import com.android.internal.view.FloatingActionMode;
import com.android.internal.widget.FloatingToolbar;
import com.android.systemui.R;
import com.android.systemui.R$styleable;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.statusbar.DragDownHelper;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.utils.HwLog;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;

public class StatusBarWindowView extends FrameLayout {
    private View mBrightnessMirror;
    DragDownHelper mDragDownHelper;
    private Window mFakeWindow = new Window(this.mContext) {
        public void takeSurface(Callback2 callback) {
        }

        public void takeInputQueue(Callback callback) {
        }

        public boolean isFloating() {
            return false;
        }

        public void alwaysReadCloseOnTouchAttr() {
        }

        public void setContentView(int layoutResID) {
        }

        public void setContentView(View view) {
        }

        public void setContentView(View view, android.view.ViewGroup.LayoutParams params) {
        }

        public void addContentView(View view, android.view.ViewGroup.LayoutParams params) {
        }

        public void clearContentView() {
        }

        public View getCurrentFocus() {
            return null;
        }

        public LayoutInflater getLayoutInflater() {
            return null;
        }

        public void setTitle(CharSequence title) {
        }

        public void setTitleColor(int textColor) {
        }

        public void openPanel(int featureId, KeyEvent event) {
        }

        public void closePanel(int featureId) {
        }

        public void togglePanel(int featureId, KeyEvent event) {
        }

        public void invalidatePanelMenu(int featureId) {
        }

        public boolean performPanelShortcut(int featureId, int keyCode, KeyEvent event, int flags) {
            return false;
        }

        public boolean performPanelIdentifierAction(int featureId, int id, int flags) {
            return false;
        }

        public void closeAllPanels() {
        }

        public boolean performContextMenuIdentifierAction(int id, int flags) {
            return false;
        }

        public void onConfigurationChanged(Configuration newConfig) {
        }

        public void setBackgroundDrawable(Drawable drawable) {
        }

        public void setFeatureDrawableResource(int featureId, int resId) {
        }

        public void setFeatureDrawableUri(int featureId, Uri uri) {
        }

        public void setFeatureDrawable(int featureId, Drawable drawable) {
        }

        public void setFeatureDrawableAlpha(int featureId, int alpha) {
        }

        public void setFeatureInt(int featureId, int value) {
        }

        public void takeKeyEvents(boolean get) {
        }

        public boolean superDispatchKeyEvent(KeyEvent event) {
            return false;
        }

        public boolean superDispatchKeyShortcutEvent(KeyEvent event) {
            return false;
        }

        public boolean superDispatchTouchEvent(MotionEvent event) {
            return false;
        }

        public boolean superDispatchTrackballEvent(MotionEvent event) {
            return false;
        }

        public boolean superDispatchGenericMotionEvent(MotionEvent event) {
            return false;
        }

        public View getDecorView() {
            return StatusBarWindowView.this;
        }

        public View peekDecorView() {
            return null;
        }

        public Bundle saveHierarchyState() {
            return null;
        }

        public void restoreHierarchyState(Bundle savedInstanceState) {
        }

        protected void onActive() {
        }

        public void setChildDrawable(int featureId, Drawable drawable) {
        }

        public void setChildInt(int featureId, int value) {
        }

        public boolean isShortcutKey(int keyCode, KeyEvent event) {
            return false;
        }

        public void setVolumeControlStream(int streamType) {
        }

        public int getVolumeControlStream() {
            return 0;
        }

        public int getStatusBarColor() {
            return 0;
        }

        public void setStatusBarColor(int color) {
        }

        public int getNavigationBarColor() {
            return 0;
        }

        public void setNavigationBarColor(int color) {
        }

        public void setDecorCaptionShade(int decorCaptionShade) {
        }

        public void setResizingCaptionDrawable(Drawable drawable) {
        }

        public void onMultiWindowModeChanged() {
        }

        public void reportActivityRelaunched() {
        }
    };
    private FalsingManager mFalsingManager;
    private ActionMode mFloatingActionMode;
    private View mFloatingActionModeOriginatingView;
    private FloatingToolbar mFloatingToolbar;
    private OnPreDrawListener mFloatingToolbarPreDrawListener;
    HwKeyguardDragHelper mKeyguardDrager = new HwKeyguardDragHelper();
    NotificationPanelView mNotificationPanel;
    private int mRightInset = 0;
    private PhoneStatusBar mService;
    private NotificationStackScrollLayout mStackScrollLayout;
    private final Paint mTransparentSrcPaint = new Paint();

    private class ActionModeCallback2Wrapper extends ActionMode.Callback2 {
        private final ActionMode.Callback mWrapped;

        public ActionModeCallback2Wrapper(ActionMode.Callback wrapped) {
            this.mWrapped = wrapped;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return this.mWrapped.onCreateActionMode(mode, menu);
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            StatusBarWindowView.this.requestFitSystemWindows();
            return this.mWrapped.onPrepareActionMode(mode, menu);
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return this.mWrapped.onActionItemClicked(mode, item);
        }

        public void onDestroyActionMode(ActionMode mode) {
            this.mWrapped.onDestroyActionMode(mode);
            if (mode == StatusBarWindowView.this.mFloatingActionMode) {
                StatusBarWindowView.this.cleanupFloatingActionModeViews();
                StatusBarWindowView.this.mFloatingActionMode = null;
            }
            StatusBarWindowView.this.requestFitSystemWindows();
        }

        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            if (this.mWrapped instanceof ActionMode.Callback2) {
                ((ActionMode.Callback2) this.mWrapped).onGetContentRect(mode, view, outRect);
            } else {
                super.onGetContentRect(mode, view, outRect);
            }
        }
    }

    public class LayoutParams extends android.widget.FrameLayout.LayoutParams {
        public boolean ignoreRightInset;

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R$styleable.StatusBarWindowView_Layout);
            this.ignoreRightInset = a.getBoolean(0, false);
            a.recycle();
        }
    }

    public StatusBarWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMotionEventSplittingEnabled(false);
        this.mTransparentSrcPaint.setColor(0);
        this.mTransparentSrcPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
        this.mFalsingManager = FalsingManager.getInstance(context);
    }

    protected boolean fitSystemWindows(Rect insets) {
        boolean changed = true;
        if (getFitsSystemWindows()) {
            boolean paddingChanged = (insets.left == getPaddingLeft() && insets.top == getPaddingTop()) ? insets.bottom != getPaddingBottom() : true;
            if (insets.right != this.mRightInset) {
                this.mRightInset = insets.right;
                applyMargins();
            }
            if (paddingChanged) {
                setPadding(insets.left, 0, 0, 0);
            }
            insets.left = 0;
            insets.top = 0;
            insets.right = 0;
        } else {
            if (this.mRightInset != 0) {
                this.mRightInset = 0;
                applyMargins();
            }
            if (getPaddingLeft() == 0 && getPaddingRight() == 0 && getPaddingTop() == 0 && getPaddingBottom() == 0) {
                changed = false;
            }
            if (changed) {
                setPadding(0, 0, 0, 0);
            }
            insets.top = 0;
        }
        return false;
    }

    private void applyMargins() {
        int N = getChildCount();
        for (int i = 0; i < N; i++) {
            View child = getChildAt(i);
            if (child.getLayoutParams() instanceof LayoutParams) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (!(lp.ignoreRightInset || lp.rightMargin == this.mRightInset)) {
                    lp.rightMargin = this.mRightInset;
                    child.requestLayout();
                }
            }
        }
    }

    public android.widget.FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected android.widget.FrameLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -1);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mStackScrollLayout = (NotificationStackScrollLayout) findViewById(R.id.notification_stack_scroller);
        this.mNotificationPanel = (NotificationPanelView) findViewById(R.id.notification_panel);
        this.mBrightnessMirror = findViewById(R.id.brightness_mirror);
    }

    public void setService(PhoneStatusBar service) {
        this.mService = service;
        this.mDragDownHelper = new DragDownHelper(getContext(), this, this.mStackScrollLayout, this.mService);
    }

    public void initKeyguardDrager() {
        this.mKeyguardDrager.init(this, this.mService);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mService.isScrimSrcModeEnabled()) {
            IBinder windowToken = getWindowToken();
            android.view.WindowManager.LayoutParams lp = (android.view.WindowManager.LayoutParams) getLayoutParams();
            lp.token = windowToken;
            setLayoutParams(lp);
            WindowManagerGlobal.getInstance().changeCanvasOpacity(windowToken, true);
            setWillNotDraw(false);
            return;
        }
        setWillNotDraw(true);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean down = event.getAction() == 0;
        switch (event.getKeyCode()) {
            case 4:
                if (down || this.mService.isFullscreenBouncer()) {
                    return true;
                }
                this.mService.onBackPressed();
                return true;
            case 24:
            case 25:
                if (this.mService.isDozing()) {
                    MediaSessionLegacyHelper.getHelper(this.mContext).sendVolumeKeyEvent(event, true);
                    return true;
                }
                break;
            case 62:
                break;
            case 82:
                if (!down) {
                    return this.mService.onMenuPressed();
                }
                break;
        }
        if (!down) {
            return this.mService.onSpacePressed();
        }
        if (this.mService.interceptMediaKey(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        this.mFalsingManager.onTouchEvent(ev, getWidth(), getHeight());
        if (this.mBrightnessMirror != null && this.mBrightnessMirror.getVisibility() == 0 && ev.getActionMasked() == 5) {
            return false;
        }
        if (ev.getActionMasked() == 0) {
            this.mStackScrollLayout.closeControlsIfOutsideTouch(ev);
        }
        int event = ev.getAction();
        if (!(3 == event || 1 == event)) {
            if (6 == event) {
            }
            return super.dispatchTouchEvent(ev);
        }
        HwKeyguardUpdateMonitor monitor = HwKeyguardUpdateMonitor.getInstance();
        if (!(monitor.isInBouncer() || monitor.isOccluded() || !monitor.isShowing())) {
            HwLog.i("StatusBarWindowView", "onInterceptTouchEvent mKeyguardDrager.onProcFyuseMotionEvent");
            this.mKeyguardDrager.onProcFyuseMotionEvent(ev);
        }
        try {
            return super.dispatchTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        if (this.mKeyguardDrager.onInterceptTouchEvent(ev)) {
            return true;
        }
        int action = ev.getActionMasked();
        if (this.mNotificationPanel.isFullyExpanded() && this.mStackScrollLayout.getVisibility() == 0 && this.mService.getBarState() == 1 && !this.mService.isBouncerShowing()) {
            intercept = this.mDragDownHelper.onInterceptTouchEvent(ev);
            if (action == 0) {
                this.mService.wakeUpIfDozing(ev.getEventTime(), ev);
            }
        }
        if (!intercept) {
            super.onInterceptTouchEvent(ev);
        }
        if (intercept) {
            MotionEvent cancellation = MotionEvent.obtain(ev);
            cancellation.setAction(3);
            this.mStackScrollLayout.onInterceptTouchEvent(cancellation);
            this.mNotificationPanel.onInterceptTouchEvent(cancellation);
            cancellation.recycle();
        }
        return intercept;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (this.mKeyguardDrager.isTouchBlocked()) {
            return this.mKeyguardDrager.onTouchEvent(ev);
        }
        boolean handled = false;
        if (this.mService.getBarState() == 1) {
            handled = this.mDragDownHelper.onTouchEvent(ev);
        }
        if (!handled) {
            handled = super.onTouchEvent(ev);
        }
        if (!handled && (action == 1 || action == 3)) {
            this.mService.setInteracting(1, false);
        }
        return handled;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mService.isScrimSrcModeEnabled()) {
            int paddedBottom = getHeight() - getPaddingBottom();
            int paddedRight = getWidth() - getPaddingRight();
            if (getPaddingTop() != 0) {
                canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) getPaddingTop(), this.mTransparentSrcPaint);
            }
            if (getPaddingBottom() != 0) {
                canvas.drawRect(0.0f, (float) paddedBottom, (float) getWidth(), (float) getHeight(), this.mTransparentSrcPaint);
            }
            if (getPaddingLeft() != 0) {
                canvas.drawRect(0.0f, (float) getPaddingTop(), (float) getPaddingLeft(), (float) paddedBottom, this.mTransparentSrcPaint);
            }
            if (getPaddingRight() != 0) {
                canvas.drawRect((float) paddedRight, (float) getPaddingTop(), (float) getWidth(), (float) paddedBottom, this.mTransparentSrcPaint);
            }
        }
    }

    public void cancelExpandHelper() {
        if (this.mStackScrollLayout != null) {
            this.mStackScrollLayout.cancelExpandHelper();
        }
    }

    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback, int type) {
        if (type == 1) {
            return startActionMode(originalView, callback, type);
        }
        return super.startActionModeForChild(originalView, callback, type);
    }

    private ActionMode createFloatingActionMode(View originatingView, ActionMode.Callback2 callback) {
        if (this.mFloatingActionMode != null) {
            this.mFloatingActionMode.finish();
        }
        cleanupFloatingActionModeViews();
        final FloatingActionMode mode = new FloatingActionMode(this.mContext, callback, originatingView);
        this.mFloatingActionModeOriginatingView = originatingView;
        this.mFloatingToolbarPreDrawListener = new OnPreDrawListener() {
            public boolean onPreDraw() {
                mode.updateViewLocationInWindow();
                return true;
            }
        };
        return mode;
    }

    private void setHandledFloatingActionMode(ActionMode mode) {
        this.mFloatingActionMode = mode;
        this.mFloatingToolbar = new FloatingToolbar(this.mContext, this.mFakeWindow);
        ((FloatingActionMode) this.mFloatingActionMode).setFloatingToolbar(this.mFloatingToolbar);
        this.mFloatingActionMode.invalidate();
        this.mFloatingActionModeOriginatingView.getViewTreeObserver().addOnPreDrawListener(this.mFloatingToolbarPreDrawListener);
    }

    private void cleanupFloatingActionModeViews() {
        if (this.mFloatingToolbar != null) {
            this.mFloatingToolbar.dismiss();
            this.mFloatingToolbar = null;
        }
        if (this.mFloatingActionModeOriginatingView != null) {
            if (this.mFloatingToolbarPreDrawListener != null) {
                this.mFloatingActionModeOriginatingView.getViewTreeObserver().removeOnPreDrawListener(this.mFloatingToolbarPreDrawListener);
                this.mFloatingToolbarPreDrawListener = null;
            }
            this.mFloatingActionModeOriginatingView = null;
        }
    }

    private ActionMode startActionMode(View originatingView, ActionMode.Callback callback, int type) {
        ActionMode.Callback2 wrappedCallback = new ActionModeCallback2Wrapper(callback);
        ActionMode mode = createFloatingActionMode(originatingView, wrappedCallback);
        if (mode == null || !wrappedCallback.onCreateActionMode(mode, mode.getMenu())) {
            return null;
        }
        setHandledFloatingActionMode(mode);
        return mode;
    }
}
