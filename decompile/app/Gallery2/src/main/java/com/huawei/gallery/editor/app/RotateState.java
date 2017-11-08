package com.huawei.gallery.editor.app;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.editor.app.EditorState.ActionInfo;
import com.huawei.gallery.editor.filters.FilterCropRepresentation;
import com.huawei.gallery.editor.filters.FilterMirrorRepresentation;
import com.huawei.gallery.editor.filters.FilterMirrorRepresentation.Mirror;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.FilterRotateRepresentation;
import com.huawei.gallery.editor.filters.FilterRotateRepresentation.Rotation;
import com.huawei.gallery.editor.filters.FilterStraightenRepresentation;
import com.huawei.gallery.editor.glrender.BaseRender;
import com.huawei.gallery.editor.glrender.RotateRender;
import com.huawei.gallery.editor.glrender.RotateRender.RotateDelegate;
import com.huawei.gallery.editor.imageshow.GeometryMathUtils;
import com.huawei.gallery.editor.imageshow.GeometryMathUtils.GeometryHolder;
import com.huawei.gallery.editor.pipeline.EditorLoadLib;
import com.huawei.gallery.editor.step.GeometryEditorStep;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.editor.ui.BaseEditorView;
import com.huawei.gallery.editor.ui.EditorUIController;
import com.huawei.gallery.editor.ui.RotateUIController;
import com.huawei.gallery.editor.ui.RotateUIController.RotateListener;
import com.huawei.watermark.manager.parse.WMElement;

public class RotateState extends EditorState implements RotateDelegate, RotateListener {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static final int DISPLAY_PADDING_BOTTOM = ((int) (((double) GalleryUtils.getWidthPixels()) * 0.0667d));
    private static final int DISPLAY_PADDING_LEFT = ((int) (((double) GalleryUtils.getWidthPixels()) * 0.0833d));
    private static final int DISPLAY_PADDING_RIGHT = ((int) (((double) GalleryUtils.getWidthPixels()) * 0.0833d));
    private static final int DISPLAY_PADDING_TOP = ((int) (((double) GalleryUtils.getWidthPixels()) * 0.0667d));
    private float mCurrentRotatedAngle;
    private final Animation mFadeIn;
    private FilterMirrorRepresentation mFilterMirrorRepresentation;
    private FilterRotateRepresentation mFilterRotateRepresentation;
    private FilterStraightenRepresentation mFilterStraightenRepresentation;
    private volatile boolean mForceUpdateView = false;
    private RotateRender mRotateRender;
    private RotateUIController mRotateUIController;

    private class RotateTask extends AsyncTask<Bitmap, Boolean, Float> {
        private RotateTask() {
        }

        protected Float doInBackground(Bitmap... params) {
            if (params == null) {
                return Float.valueOf(0.0f);
            }
            Bitmap bitmap = params[0];
            if (bitmap == null || bitmap.isRecycled()) {
                return Float.valueOf(0.0f);
            }
            float value = RotateState.nativeTiltcorrection(bitmap);
            RotateState.this.getImage().getBitmapCache().cache(bitmap);
            return Float.valueOf(value);
        }

        protected void onPostExecute(Float targetAngle) {
            if (targetAngle != null && RotateState.this.mActivited && Math.abs(targetAngle.floatValue()) > 0.0f) {
                RotateState.this.mRotateRender.prepareAutoRotateAnimation(targetAngle.floatValue(), new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        RotateState.this.mRotateUIController.setRotatedAngle(((Float) valueAnimator.getAnimatedValue()).floatValue());
                    }
                }, new AnimatorListener() {
                    public void onAnimationStart(Animator animator) {
                        RotateState.this.mRotateUIController.setText(R.string.straighten_auto);
                    }

                    public void onAnimationEnd(Animator animator) {
                        RotateState.this.mRotateUIController.setText(R.string.straighten_reset);
                    }

                    public void onAnimationCancel(Animator animator) {
                        RotateState.this.mRotateUIController.setText(R.string.straighten_reset);
                    }

                    public void onAnimationRepeat(Animator animator) {
                    }
                });
            }
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 2;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 3;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 4;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 5;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 6;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 7;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 8;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 9;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 10;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 11;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 12;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 13;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 14;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 15;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 16;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 17;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 18;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 19;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 20;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 21;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 22;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 23;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 24;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 25;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 26;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 27;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 28;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 29;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 30;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 31;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 32;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 33;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 34;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 35;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 36;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 37;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 38;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 1;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 39;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 40;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 41;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 42;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 43;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 44;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 45;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 46;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 47;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 48;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 49;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 50;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 51;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 52;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 53;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 54;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 55;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 56;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 57;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 58;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 59;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 60;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 61;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 62;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 63;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 64;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 65;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Action.PRINT.ordinal()] = 66;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Action.RANGE_MEASURE.ordinal()] = 67;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Action.RECYCLE_CLEAN_BIN.ordinal()] = 68;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Action.RECYCLE_DELETE.ordinal()] = 69;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Action.RECYCLE_RECOVERY.ordinal()] = 70;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Action.REDO.ordinal()] = 71;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Action.REMOVE.ordinal()] = 72;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Action.RENAME.ordinal()] = 73;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Action.RE_SEARCH.ordinal()] = 74;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Action.ROTATE_LEFT.ordinal()] = 75;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Action.ROTATE_RIGHT.ordinal()] = 76;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Action.SAVE.ordinal()] = 77;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[Action.SAVE_BURST.ordinal()] = 78;
        } catch (NoSuchFieldError e78) {
        }
        try {
            iArr[Action.SEE_BARCODE_INFO.ordinal()] = 79;
        } catch (NoSuchFieldError e79) {
        }
        try {
            iArr[Action.SETAS.ordinal()] = 80;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[Action.SETAS_BOTH.ordinal()] = 81;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[Action.SETAS_FIXED.ordinal()] = 82;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[Action.SETAS_FIXED_ACTIVED.ordinal()] = 83;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[Action.SETAS_HOME.ordinal()] = 84;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE.ordinal()] = 85;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE_ACTIVED.ordinal()] = 86;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[Action.SETAS_UNLOCK.ordinal()] = 87;
        } catch (NoSuchFieldError e87) {
        }
        try {
            iArr[Action.SETTINGS.ordinal()] = 88;
        } catch (NoSuchFieldError e88) {
        }
        try {
            iArr[Action.SHARE.ordinal()] = 89;
        } catch (NoSuchFieldError e89) {
        }
        try {
            iArr[Action.SHOW_ON_MAP.ordinal()] = 90;
        } catch (NoSuchFieldError e90) {
        }
        try {
            iArr[Action.SINGLE_SELECTION.ordinal()] = 91;
        } catch (NoSuchFieldError e91) {
        }
        try {
            iArr[Action.SINGLE_SELECTION_ON.ordinal()] = 92;
        } catch (NoSuchFieldError e92) {
        }
        try {
            iArr[Action.SLIDESHOW.ordinal()] = 93;
        } catch (NoSuchFieldError e93) {
        }
        try {
            iArr[Action.STORY_ALBUM_REMOVE.ordinal()] = 94;
        } catch (NoSuchFieldError e94) {
        }
        try {
            iArr[Action.STORY_ITEM_REMOVE.ordinal()] = 95;
        } catch (NoSuchFieldError e95) {
        }
        try {
            iArr[Action.STORY_RENAME.ordinal()] = 96;
        } catch (NoSuchFieldError e96) {
        }
        try {
            iArr[Action.TIME.ordinal()] = 97;
        } catch (NoSuchFieldError e97) {
        }
        try {
            iArr[Action.TOGIF.ordinal()] = 98;
        } catch (NoSuchFieldError e98) {
        }
        try {
            iArr[Action.UNDO.ordinal()] = 99;
        } catch (NoSuchFieldError e99) {
        }
        try {
            iArr[Action.WITHOUT_UPDATE.ordinal()] = 100;
        } catch (NoSuchFieldError e100) {
        }
        try {
            iArr[Action.WITH_UPDATE.ordinal()] = 101;
        } catch (NoSuchFieldError e101) {
        }
        -com-huawei-gallery-actionbar-ActionSwitchesValues = iArr;
        return iArr;
    }

    private static native float nativeTiltcorrection(Bitmap bitmap);

    public RotateState(Context context, ViewGroup layout, BaseEditorView editorView) {
        super(context, layout, editorView);
        this.mActionInfo = new ActionInfo(context.getResources().getString(R.string.rotate), Action.BACK, null, false);
        for (FilterRepresentation rep : this.mEditorView.getEditorManager().getTools()) {
            if (rep instanceof FilterRotateRepresentation) {
                this.mFilterRotateRepresentation = (FilterRotateRepresentation) rep;
                this.mFilterRotateRepresentation.setClockwise(true);
            } else if (rep instanceof FilterMirrorRepresentation) {
                this.mFilterMirrorRepresentation = (FilterMirrorRepresentation) rep;
            } else if (rep instanceof FilterStraightenRepresentation) {
                this.mFilterStraightenRepresentation = (FilterStraightenRepresentation) rep;
            }
        }
        this.mFadeIn = AnimationUtils.loadAnimation(context, R.anim.dial_fade_in);
    }

    protected BaseRender createRender() {
        this.mRotateRender = new RotateRender(this.mEditorView, this, this.mContext);
        this.mRotateRender.setDisplayPaddingRect(DISPLAY_PADDING_LEFT, DISPLAY_PADDING_TOP, DISPLAY_PADDING_RIGHT, DISPLAY_PADDING_BOTTOM);
        return this.mRotateRender;
    }

    protected EditorUIController createUIController() {
        this.mRotateUIController = new RotateUIController(this.mContext, this.mParentLayout, this, this.mEditorView);
        return this.mRotateUIController;
    }

    public void show() {
        super.show();
        if (EditorLoadLib.TILTCORRECTIONJNI_LOADED) {
            RotateTask task = new RotateTask();
            Bitmap source = getImage().getFilteredImage();
            float scale = EditorUtils.getScaleByHalfScreen(source.getWidth(), source.getHeight(), GalleryUtils.getWidthPixels(), GalleryUtils.getHeightPixels());
            task.execute(new Bitmap[]{getImage().getBitmapCache().getBitmapCopy(source, scale)});
        }
    }

    public boolean getForceUpdateView() {
        return this.mForceUpdateView;
    }

    public float getCurrentRotatedAngle() {
        return this.mCurrentRotatedAngle;
    }

    public Mirror getMirror() {
        return this.mFilterMirrorRepresentation.getMirror();
    }

    public Rotation getRotation() {
        return this.mFilterRotateRepresentation.getRotation();
    }

    public int getPaintColor() {
        return this.mContext.getResources().getColor(R.color.crop_bolder_selected);
    }

    public void rotateCW() {
        this.mFilterRotateRepresentation.rotateCW();
    }

    public void onRotateClicked() {
        GLRoot root = this.mEditorView.getGLRoot();
        if (root != null) {
            root.lockRenderThread();
            try {
                this.mRotateRender.cancelAutoRotateAnimation();
                this.mForceUpdateView = true;
                if (!startRotationAnimation()) {
                    this.mFilterRotateRepresentation.rotateCW();
                    this.mEditorView.invalidate();
                }
                root.unlockRenderThread();
            } catch (Throwable th) {
                root.unlockRenderThread();
            }
        }
    }

    public void onMirrorClicked() {
        GLRoot root = this.mEditorView.getGLRoot();
        if (root != null) {
            root.lockRenderThread();
            try {
                this.mRotateRender.cancelAutoRotateAnimation();
                this.mFilterMirrorRepresentation.setHorizontal(true);
                this.mFilterMirrorRepresentation.cycle(this.mFilterRotateRepresentation.getRotation());
                this.mEditorView.invalidate();
            } finally {
                root.unlockRenderThread();
            }
        }
    }

    public void onResetClicked() {
        this.mRotateRender.prepareResetAnimation();
    }

    public void onAngleChanged(float degrees, boolean fromUser) {
        if (fromUser) {
            this.mRotateRender.cancelAutoRotateAnimation();
        }
        GLRoot root = this.mEditorView.getGLRoot();
        if (root != null) {
            root.lockRenderThread();
            try {
                this.mCurrentRotatedAngle = degrees;
                this.mEditorView.invalidate();
            } finally {
                root.unlockRenderThread();
            }
        }
    }

    public void onStartTrackingTouch() {
        this.mRotateRender.prepareLineFadeInAnimation();
    }

    public void onStopTrackingTouch() {
        this.mRotateRender.prepareLineFadeOutAnimation();
    }

    public void hide() {
        super.hide();
        this.mRotateRender.hide();
        this.mCurrentRotatedAngle = 0.0f;
        clearRepresentation();
    }

    public void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
    }

    public void executeAction(Action action) {
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
                this.mFilterStraightenRepresentation.setStraighten(this.mCurrentRotatedAngle);
                GeometryEditorStep step = new GeometryEditorStep();
                step.add(this.mFilterRotateRepresentation);
                step.add(this.mFilterMirrorRepresentation);
                step.add(this.mFilterStraightenRepresentation);
                Bitmap bmp = getImage().getFilteredImage();
                RectF crop = new RectF(0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1);
                GeometryHolder holder = GeometryMathUtils.unpackGeometry(step.getFilterRepresentationList());
                if (Math.abs(this.mCurrentRotatedAngle) > 0.0f) {
                    Rect renderRect = this.mRotateRender.getRenderRect();
                    crop = GeometryMathUtils.updateCurrentCrop(holder, bmp.getWidth(), bmp.getHeight(), renderRect.width(), renderRect.height(), this.mCurrentRotatedAngle);
                    step.add(new FilterCropRepresentation(crop));
                }
                TransitionStore transitionStore = this.mEditorView.getTransitionStore();
                if (transitionStore != null) {
                    holder.straighten = this.mFilterStraightenRepresentation.getStraighten();
                    transitionStore.put("key-quit-rotate-for-editor", holder.rotation);
                    transitionStore.put("key-quit-mirror-for-editor", holder.mirror);
                    transitionStore.put("key-quit-angle-for-editor", Float.valueOf(holder.straighten));
                    transitionStore.put("key-quit-crop-rectf--for-editor", crop);
                    transitionStore.put("key-quit-rect-for-editor", this.mRotateRender.computeRect(bmp.getWidth(), bmp.getHeight(), this.mFilterRotateRepresentation.getRotation()));
                    transitionStore.put("key-quit-bitmap-rect-for-editor", new Rect(0, 0, bmp.getWidth(), bmp.getHeight()));
                }
                getSimpleEditorManager().pushEditorStep(step);
                return;
            default:
                return;
        }
    }

    public void onLeaveEditor() {
        super.onLeaveEditor();
    }

    protected boolean enableComparison() {
        return false;
    }

    public void onAnimationRenderFinished(Rect source, final Rect currentRect) {
        this.mForceUpdateView = false;
        Activity activity = this.mEditorView.getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    RotateState.this.mRotateUIController.updateDisplayBounds(new RectF(currentRect), RotateState.this.mCurrentRotatedAngle, RotateState.this.mFadeIn);
                }
            });
        }
    }

    public void onNavigationBarChanged() {
        this.mForceUpdateView = true;
        super.onNavigationBarChanged();
    }

    private void clearRepresentation() {
        this.mFilterRotateRepresentation.setRotation(FilterRotateRepresentation.getNil());
        this.mFilterMirrorRepresentation.setMirror(FilterMirrorRepresentation.getNil());
        this.mFilterStraightenRepresentation.setStraighten(FilterStraightenRepresentation.getNil());
    }

    private boolean startRotationAnimation() {
        Bitmap bmp = computeRenderTexture();
        if (bmp == null) {
            return false;
        }
        RectF crop = new RectF(0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1);
        if (Math.abs(this.mCurrentRotatedAngle) > 0.0f) {
            this.mFilterStraightenRepresentation.setStraighten(this.mCurrentRotatedAngle);
            GeometryEditorStep step = new GeometryEditorStep();
            step.add(this.mFilterRotateRepresentation);
            step.add(this.mFilterMirrorRepresentation);
            step.add(this.mFilterStraightenRepresentation);
            Rect renderRect = this.mRotateRender.getRenderRect();
            crop = GeometryMathUtils.updateCurrentCrop(GeometryMathUtils.unpackGeometry(step.getFilterRepresentationList()), bmp.getWidth(), bmp.getHeight(), renderRect.width(), renderRect.height(), this.mCurrentRotatedAngle);
        }
        this.mRotateRender.prepareEditorRotationAnimation(this.mFilterRotateRepresentation.getRotation(), this.mFilterRotateRepresentation.getNextRotation(), crop);
        return true;
    }
}
