package com.huawei.gallery.editor.ui;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.app.IntentChooser.IntentChooserDialogClickListener;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.GestureRecognizer;
import com.android.gallery3d.ui.PositionController;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.animation.CubicBezierInterpolator;
import com.huawei.gallery.editor.animation.EditorOpenOrQuitEffect;
import com.huawei.gallery.editor.app.BasePreviewState;
import com.huawei.gallery.editor.app.EditorState;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.imageshow.MasterImage;
import com.huawei.gallery.editor.pipeline.ProcessingTaskController;
import com.huawei.gallery.editor.pipeline.SimpleEditorManager;
import com.huawei.gallery.editor.pipeline.SimpleEditorManager.Listener;
import com.huawei.gallery.editor.screenshotseditor.app.ScreenShotsPreviewState;
import com.huawei.gallery.util.UIUtils;
import com.huawei.watermark.manager.parse.WMElement;
import java.lang.ref.WeakReference;

public abstract class BaseEditorView extends GLView implements Listener, EditorViewDelegate {
    protected Activity mActivity;
    protected EditorState mCurrentState;
    protected Delegate mDelegate;
    protected SimpleEditorManager mEditorManager;
    protected int mExitMode = 0;
    private GalleryContext mGalleryContext;
    private final GestureRecognizer mGestureRecognizer;
    private Handler mHandler = null;
    protected ImageView mHeadBackground;
    private boolean mIsSavingImage;
    protected boolean mIsSecure = false;
    protected EditorOpenOrQuitEffect mLeaveEffect;
    private ValueAnimator mNavigationBarChangeAnimation;
    private int mNavigationBarHeight;
    protected int mOrientation = 1;
    protected ViewGroup mParentLayout;
    protected BasePreviewState mPreviewState;
    private WeakReference<ProgressDialog> mSavingProgressDialog;

    public interface Delegate {
        int getActionBarHeight();

        String getToken();

        TransitionStore getTransitionStore();

        boolean isFromLocalImage();

        void jumpPituIfNeeded(boolean z);

        void onLeaveEditorMode(Uri uri);

        void share(Uri uri, IntentChooserDialogClickListener intentChooserDialogClickListener);

        boolean waitForDataLoad(Uri uri, Bitmap bitmap);
    }

    private class MyGestureListener implements GestureRecognizer.Listener {
        private MyGestureListener() {
        }

        public boolean onSingleTapUp(float x, float y) {
            return BaseEditorView.this.mCurrentState != null ? BaseEditorView.this.mCurrentState.onSingleTapUp(x, y) : false;
        }

        public boolean onDoubleTap(float x, float y) {
            return BaseEditorView.this.mCurrentState != null ? BaseEditorView.this.mCurrentState.onDoubleTap(x, y) : false;
        }

        public boolean onScroll(float dx, float dy, float totalX, float totalY) {
            return BaseEditorView.this.mCurrentState != null ? BaseEditorView.this.mCurrentState.onScroll(dx, dy, totalX, totalY) : false;
        }

        public boolean onFling(float velocityX, float velocityY) {
            return BaseEditorView.this.mCurrentState != null ? BaseEditorView.this.mCurrentState.onFling(velocityX, velocityY) : false;
        }

        public boolean onScaleBegin(float focusX, float focusY) {
            return BaseEditorView.this.mCurrentState != null ? BaseEditorView.this.mCurrentState.onScaleBegin(focusX, focusY) : false;
        }

        public boolean onScale(float focusX, float focusY, float scale) {
            return BaseEditorView.this.mCurrentState != null ? BaseEditorView.this.mCurrentState.onScale(focusX, focusY, scale) : false;
        }

        public void onScaleEnd() {
            if (BaseEditorView.this.mCurrentState != null) {
                BaseEditorView.this.mCurrentState.onScaleEnd();
            }
        }

        public void onLongPress(MotionEvent e) {
            if (BaseEditorView.this.mCurrentState != null) {
                BaseEditorView.this.mCurrentState.onLongPress(e);
            }
        }

        public void onDown(float x, float y) {
            if (BaseEditorView.this.mCurrentState != null) {
                BaseEditorView.this.mCurrentState.onDown(x, y);
            }
        }

        public void onUp() {
            if (BaseEditorView.this.mCurrentState != null) {
                BaseEditorView.this.mCurrentState.onUp();
            }
        }
    }

    public BaseEditorView(Delegate delegate, Activity activity) {
        this.mDelegate = delegate;
        this.mActivity = activity;
        this.mEditorManager = new SimpleEditorManager(activity, this);
        this.mEditorManager.setListener(this);
        this.mGestureRecognizer = new GestureRecognizer(activity, new MyGestureListener());
        this.mOrientation = activity.getResources().getConfiguration().orientation;
        setVisibility(1);
        this.mHeadBackground = new ImageView(getContext());
    }

    protected void render(GLCanvas canvas) {
        if (this.mLeaveEffect != null) {
            this.mLeaveEffect.calculate(AnimationTime.get());
            if (!this.mLeaveEffect.isActive()) {
                this.mLeaveEffect = null;
                if (this.mHandler != null) {
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            GLRoot root = BaseEditorView.this.getGLRoot();
                            if (root == null) {
                                BaseEditorView.this.setVisibility(1);
                                return;
                            }
                            root.lockRenderThread();
                            try {
                                BaseEditorView.this.mEditorManager.freeTexture();
                                BaseEditorView.this.setVisibility(1);
                            } finally {
                                root.unlockRenderThread();
                            }
                        }
                    });
                }
            }
            invalidate();
            if (this.mLeaveEffect != null) {
                canvas.fillRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), -16777216);
                this.mLeaveEffect.render(canvas, getImage().getPreviewTexture());
            }
        } else {
            EditorState currentState = this.mCurrentState;
            if (currentState != null) {
                if (currentState.needRenderBeforeSuperView()) {
                    currentState.render(canvas);
                    super.render(canvas);
                } else {
                    super.render(canvas);
                    currentState.render(canvas);
                }
            }
        }
    }

    protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        EditorState currentState = this.mCurrentState;
        if (currentState != null) {
            currentState.onLayout(changeSize, left, top, right, bottom);
        }
    }

    protected boolean onTouch(MotionEvent event) {
        if (this.mCurrentState != null) {
            this.mCurrentState.onTouch(event);
        }
        this.mGestureRecognizer.onTouchEvent(event);
        return true;
    }

    public void setParentLayout(RelativeLayout layout) {
        this.mParentLayout = layout;
        if (this.mPreviewState != null) {
            this.mPreviewState.updateParentLayout(layout);
        }
    }

    public void create() {
        this.mEditorManager.create();
    }

    public void resume() {
        if (this.mCurrentState != null) {
            this.mCurrentState.resume();
        }
        this.mHandler = new SynchronizedHandler(getGLRoot());
    }

    public void pause() {
        if (this.mCurrentState != null) {
            this.mCurrentState.pause();
        }
    }

    public void destroy() {
        if (this.mCurrentState != null) {
            this.mCurrentState.destroy();
        }
        this.mEditorManager.destroy();
    }

    public void changeGadiusCover(boolean isVisible) {
        int i;
        GalleryLog.printDFXLog("U called stub method: " + this.mOrientation);
        View gradient = this.mParentLayout.findViewById(R.id.gradient);
        if (gradient == null) {
            ViewStub stub = (ViewStub) this.mParentLayout.findViewById(R.id.gradient_stub);
            if (stub != null) {
                stub.inflate();
            }
            gradient = this.mParentLayout.findViewById(R.id.gradient);
        }
        if (isVisible) {
            i = 0;
        } else {
            i = 8;
        }
        gradient.setVisibility(i);
    }

    public void updateBackground() {
        GalleryLog.printDFXLog("U called stub method: " + this.mOrientation);
        this.mHeadBackground.setLayoutParams(new LayoutParams(-1, getActionBarHeight()));
        this.mHeadBackground.setBackground(getContext().getDrawable(R.drawable.ab_bg_dark_gallery));
    }

    public void enterEditor() {
        if (getEditorManager().getImage().getOriginalBitmapLargeTemporary() != null) {
            changeGadiusCover(true);
            this.mParentLayout.addView(this.mHeadBackground);
        }
    }

    public void leaveEditor(Uri uri, boolean needAnime) {
        this.mParentLayout.removeView(this.mHeadBackground);
        synchronized (ProcessingTaskController.EDITOR_LOCK) {
            if (this.mCurrentState != null) {
                this.mCurrentState.hide();
                this.mCurrentState = null;
            }
            this.mPreviewState.onLeaveEditor();
            TransitionStore transitionStore = getTransitionStore();
            this.mDelegate.onLeaveEditorMode(uri);
            hideSavingProgress();
            Rect sourceRect = (Rect) getTransitionStore().get("key-quit-rect-for-editor");
            Bitmap bitmap = getImage().computeRenderTexture();
            if (!needAnime || bitmap == null || sourceRect == null) {
                this.mEditorManager.onLeaveEditor(true);
                setVisibility(1);
            } else {
                this.mEditorManager.onLeaveEditor(false);
                this.mLeaveEffect = new EditorOpenOrQuitEffect(new CubicBezierInterpolator(0.3f, 0.15f, 0.1f, 0.85f));
                this.mLeaveEffect.init(sourceRect, computeDisplayRect(bitmap.getWidth(), bitmap.getHeight()));
                this.mLeaveEffect.setDuration(300);
                this.mLeaveEffect.start();
                invalidate();
            }
            if (transitionStore != null) {
                transitionStore.clear();
            }
            this.mExitMode = 0;
            if (this.mParentLayout != null) {
                UIUtils.showStatusBar(this.mParentLayout);
            }
        }
        changeGadiusCover(false);
    }

    public void leaveEditor() {
        GalleryLog.printDFXLog("U called leaveEditor: " + this.mOrientation);
        leaveEditor(null, true);
    }

    protected Rect computeDisplayRect(int imageWidth, int imageHeight) {
        GalleryLog.printDFXLog("add member call: " + this.mOrientation);
        float scale = PositionController.getMinimalScale(imageWidth, imageHeight, getWidth(), getHeight());
        float renderWidth = ((float) imageWidth) * scale;
        float renderHeight = ((float) imageHeight) * scale;
        int offsetX = Math.round((((float) getWidth()) - renderWidth) / 2.0f);
        int offsetY = Math.round((((float) getHeight()) - renderHeight) / 2.0f);
        return new Rect(offsetX, offsetY, Math.round(renderWidth) + offsetX, Math.round(renderHeight) + offsetY);
    }

    public boolean onBackPressed() {
        if (this.mCurrentState == null) {
            return true;
        }
        GLRoot root = getGLRoot();
        if (root == null) {
            return true;
        }
        root.lockRenderThread();
        try {
            boolean onBackPressed = this.mCurrentState.onBackPressed();
            return onBackPressed;
        } finally {
            root.unlockRenderThread();
        }
    }

    public void changeToPreviewState() {
        GalleryLog.printDFXLog("add member call: " + this.mOrientation);
        changeState(this.mPreviewState);
    }

    public void changeState(EditorState state) {
        if (this.mCurrentState != null) {
            this.mCurrentState.hide();
        }
        this.mCurrentState = state;
        this.mCurrentState.show();
        requestLayout();
    }

    public void jumpPituIfNeeded(boolean hasModify) {
        GalleryLog.printDFXLog("add member call: " + this.mOrientation);
        this.mDelegate.jumpPituIfNeeded(hasModify);
    }

    public boolean isFromLocalImage() {
        return this.mDelegate.isFromLocalImage();
    }

    public void onActionItemClick(Action action) {
        if (this.mCurrentState != null) {
            this.mCurrentState.onActionItemClick(action);
        }
    }

    public void commitCurrentStateChanges() {
        if (this.mCurrentState != null) {
            this.mCurrentState.executeAction(Action.OK);
        }
    }

    public void commitCurrentStateAction(Action action) {
        if (this.mCurrentState != null && !(this.mCurrentState instanceof ScreenShotsPreviewState)) {
            this.mCurrentState.onActionItemClick(action);
        }
    }

    public boolean setSource(MediaItem mediaItem, Bitmap bitmap, int orientation) {
        if (mediaItem.getContentUri() == null) {
            return false;
        }
        return this.mEditorManager.setSource(mediaItem, bitmap, orientation);
    }

    public void setGalleryContext(GalleryContext context) {
        GalleryLog.printDFXLog("add member call: " + this.mOrientation);
        this.mGalleryContext = context;
    }

    public GalleryContext getGalleryContext() {
        GalleryLog.printDFXLog("add member call: " + this.mOrientation);
        return this.mGalleryContext;
    }

    public Context getContext() {
        GalleryLog.printDFXLog("add member call: " + this.mOrientation);
        return this.mActivity;
    }

    public Activity getActivity() {
        GalleryLog.printDFXLog("add member call: " + this.mOrientation);
        return this.mActivity;
    }

    public SimpleEditorManager getEditorManager() {
        GalleryLog.printDFXLog("add member call: " + this.mOrientation);
        return this.mEditorManager;
    }

    public void onDetectedFace(boolean result) {
        this.mPreviewState.onDetectedFace(result);
    }

    public void onStackChanged(boolean canUndo, boolean canRedo, boolean canCompare) {
        this.mPreviewState.onStackChanged(canUndo, canRedo, canCompare);
    }

    public TransitionStore getTransitionStore() {
        return this.mDelegate.getTransitionStore();
    }

    public boolean isSavingImage() {
        return this.mIsSavingImage;
    }

    public void showSavingProgress(boolean showDialog) {
        this.mIsSavingImage = true;
        if (showDialog) {
            if (this.mSavingProgressDialog != null) {
                ProgressDialog progress = (ProgressDialog) this.mSavingProgressDialog.get();
                if (progress != null) {
                    progress.show();
                    return;
                }
            }
            this.mSavingProgressDialog = new WeakReference(ProgressDialog.show(this.mActivity, "", this.mActivity.getString(R.string.saving_image), true, false));
        }
    }

    public void hideSavingProgress() {
        this.mIsSavingImage = false;
        if (this.mSavingProgressDialog != null) {
            ProgressDialog progress = (ProgressDialog) this.mSavingProgressDialog.get();
            if (progress != null) {
                GalleryUtils.dismissDialogSafely(progress, null);
            }
            this.mSavingProgressDialog.clear();
        }
    }

    public void onNavigationBarChange(int currHeight) {
        if (this.mCurrentState == null) {
            this.mNavigationBarHeight = currHeight;
        } else {
            doNavigationBarChangeAnimation(this.mNavigationBarHeight, currHeight);
        }
    }

    public boolean isNavigationBarAnimationRunning() {
        GalleryLog.printDFXLog("add member call: " + this.mOrientation);
        if (this.mNavigationBarChangeAnimation == null) {
            return false;
        }
        return this.mNavigationBarChangeAnimation.isRunning();
    }

    protected void notifyStateOnNavigationBarChanged() {
        if (this.mCurrentState != null) {
            this.mCurrentState.onNavigationBarChanged();
        }
    }

    private void doNavigationBarChangeAnimation(final int oldHeight, final int newHeight) {
        if (this.mNavigationBarChangeAnimation != null) {
            this.mNavigationBarChangeAnimation.cancel();
        }
        this.mNavigationBarChangeAnimation = ValueAnimator.ofFloat(new float[]{0.0f, WMElement.CAMERASIZEVALUE1B1});
        this.mNavigationBarChangeAnimation.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                BaseEditorView.this.mNavigationBarHeight = (int) (((float) oldHeight) + (((float) (newHeight - oldHeight)) * ((Float) valueAnimator.getAnimatedValue()).floatValue()));
                BaseEditorView.this.notifyStateOnNavigationBarChanged();
                BaseEditorView.this.updateContainerLayoutParams();
                BaseEditorView.this.requestLayout();
            }
        });
        this.mNavigationBarChangeAnimation.setDuration(200);
        this.mNavigationBarChangeAnimation.start();
    }

    public int getNavigationBarHeight() {
        GalleryLog.printDFXLog("add member call: " + this.mOrientation);
        return this.mNavigationBarHeight;
    }

    public void onConfigurationChanged(Configuration config) {
        this.mOrientation = config.orientation;
        if (this.mCurrentState != null) {
            this.mCurrentState.onConfigurationChanged(config);
            this.mCurrentState.onNavigationBarChanged();
        }
    }

    public int getActionBarHeight() {
        return this.mDelegate.getActionBarHeight();
    }

    public String getToken() {
        return this.mDelegate.getToken();
    }

    public void updateOriginalCompareButton() {
        GalleryLog.printDFXLog("add member call: " + this.mOrientation);
    }

    public boolean inSimpleEditor() {
        return getVisibility() == 0 && this.mLeaveEffect == null;
    }

    public boolean waitForDataLoad(Uri targetUri, Bitmap previewBitmap) {
        return this.mDelegate.waitForDataLoad(targetUri, previewBitmap);
    }

    public boolean inPreviewState() {
        return this.mCurrentState == this.mPreviewState;
    }

    protected void updateContainerLayoutParams() {
        GalleryLog.printDFXLog("add member call: " + this.mOrientation);
    }

    public void setEditorOriginalCompareButtonVisible(boolean visible) {
        GalleryLog.printDFXLog("add member call: " + this.mOrientation);
    }

    public void setExitMode(int exitMode) {
        GalleryLog.printDFXLog("add member call: " + this.mOrientation);
        this.mExitMode = exitMode;
    }

    public void setSecureCameraMode(boolean isSecure) {
        GalleryLog.printDFXLog("add member call: " + this.mOrientation);
        this.mIsSecure = isSecure;
    }

    public boolean getIsSecureCameraMode() {
        GalleryLog.printDFXLog("add member call: " + this.mOrientation);
        return this.mIsSecure;
    }

    public MasterImage getImage() {
        return this.mEditorManager.getImage();
    }

    public boolean isPort() {
        return this.mOrientation == 1;
    }
}
