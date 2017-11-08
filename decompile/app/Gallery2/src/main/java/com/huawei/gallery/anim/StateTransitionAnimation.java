package com.huawei.gallery.anim;

import android.graphics.Rect;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import com.android.gallery3d.anim.BaseTransition;
import com.android.gallery3d.anim.BaseTransition.TransformationInfo;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.RawTexture;
import com.android.gallery3d.ui.TiledScreenNail;
import com.android.gallery3d.util.GalleryUtils;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.gallery.animation.CubicBezierInterpolator;
import com.huawei.watermark.manager.parse.WMElement;

public class StateTransitionAnimation extends Animation {
    private float mCurrentBackgroundAlpha;
    private float mCurrentBackgroundScale;
    private float mCurrentContentAlpha;
    private float mCurrentContentScale;
    private float mCurrentOverlayAlpha;
    private float mCurrentOverlayScale;
    private RawTexture mOldAnimTexture;
    private RawTexture mOldScreenTexture;
    private float mProgress;
    private Rect mRectContent;
    private Rect mRectOld;
    private final Spec mTransitionSpec;
    private BaseTransition mTransitionX;

    public static class Spec {
        private static final /* synthetic */ int[] -com-huawei-gallery-anim-StateTransitionAnimation$TransitionSwitchesValues = null;
        private static final Interpolator DEFAULT_INTERPOLATOR = new DecelerateInterpolator();
        public static final Spec INCOMING = new Spec();
        public static final Spec OUTGOING = new Spec();
        public static final Spec PHOTO_INCOMING = INCOMING;
        public static final Spec SLIDE_OUTGOING = new Spec();
        public float backgroundAlphaFrom = 0.0f;
        public float backgroundAlphaTo = 0.0f;
        public float backgroundScaleFrom = 0.0f;
        public float backgroundScaleTo = 0.0f;
        public float contentAlphaFrom = WMElement.CAMERASIZEVALUE1B1;
        public float contentAlphaTo = WMElement.CAMERASIZEVALUE1B1;
        public float contentScaleFrom = WMElement.CAMERASIZEVALUE1B1;
        public float contentScaleTo = WMElement.CAMERASIZEVALUE1B1;
        public int duration = 330;
        public Interpolator interpolator = DEFAULT_INTERPOLATOR;
        public float overlayAlphaFrom = 0.0f;
        public float overlayAlphaTo = 0.0f;
        public float overlayScaleFrom = 0.0f;
        public float overlayScaleTo = 0.0f;

        private static /* synthetic */ int[] -getcom-huawei-gallery-anim-StateTransitionAnimation$TransitionSwitchesValues() {
            if (-com-huawei-gallery-anim-StateTransitionAnimation$TransitionSwitchesValues != null) {
                return -com-huawei-gallery-anim-StateTransitionAnimation$TransitionSwitchesValues;
            }
            int[] iArr = new int[Transition.values().length];
            try {
                iArr[Transition.Incoming.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Transition.None.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Transition.Outgoing.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[Transition.PhotoIncoming.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[Transition.SlideOutgoing.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            -com-huawei-gallery-anim-StateTransitionAnimation$TransitionSwitchesValues = iArr;
            return iArr;
        }

        static {
            OUTGOING.backgroundAlphaFrom = 0.5f;
            OUTGOING.backgroundAlphaTo = 0.0f;
            OUTGOING.backgroundScaleFrom = WMElement.CAMERASIZEVALUE1B1;
            OUTGOING.backgroundScaleTo = 0.0f;
            OUTGOING.contentAlphaFrom = 0.5f;
            OUTGOING.contentAlphaTo = WMElement.CAMERASIZEVALUE1B1;
            OUTGOING.contentScaleFrom = MapConfig.MIN_ZOOM;
            OUTGOING.contentScaleTo = WMElement.CAMERASIZEVALUE1B1;
            INCOMING.overlayAlphaFrom = WMElement.CAMERASIZEVALUE1B1;
            INCOMING.overlayAlphaTo = 0.0f;
            INCOMING.overlayScaleFrom = WMElement.CAMERASIZEVALUE1B1;
            INCOMING.overlayScaleTo = MapConfig.MIN_ZOOM;
            INCOMING.contentAlphaFrom = 0.0f;
            INCOMING.contentAlphaTo = WMElement.CAMERASIZEVALUE1B1;
            INCOMING.contentScaleFrom = 0.25f;
            INCOMING.contentScaleTo = WMElement.CAMERASIZEVALUE1B1;
            SLIDE_OUTGOING.backgroundAlphaFrom = 0.7f;
            SLIDE_OUTGOING.backgroundAlphaTo = 0.0f;
            SLIDE_OUTGOING.backgroundScaleFrom = WMElement.CAMERASIZEVALUE1B1;
            SLIDE_OUTGOING.backgroundScaleTo = 0.5f;
            SLIDE_OUTGOING.contentAlphaFrom = 0.5f;
            SLIDE_OUTGOING.contentAlphaTo = WMElement.CAMERASIZEVALUE1B1;
        }

        private static Spec specForTransition(Transition t) {
            switch (-getcom-huawei-gallery-anim-StateTransitionAnimation$TransitionSwitchesValues()[t.ordinal()]) {
                case 1:
                    return INCOMING;
                case 3:
                    return OUTGOING;
                case 4:
                    return PHOTO_INCOMING;
                case 5:
                    return SLIDE_OUTGOING;
                default:
                    return null;
            }
        }
    }

    public static class SpecX {
        private static final /* synthetic */ int[] -com-huawei-gallery-anim-StateTransitionAnimation$TransitionSwitchesValues = null;
        public static final BaseTransition PHOTO_INCOMING_ANIMATION = new ClickThumbUpTransitionBig();
        public static final BaseTransition PHOTO_INCOMING_THUMBNAIL_ANIMATION = new ClickThumbUpTransitionSmall();

        private static /* synthetic */ int[] -getcom-huawei-gallery-anim-StateTransitionAnimation$TransitionSwitchesValues() {
            if (-com-huawei-gallery-anim-StateTransitionAnimation$TransitionSwitchesValues != null) {
                return -com-huawei-gallery-anim-StateTransitionAnimation$TransitionSwitchesValues;
            }
            int[] iArr = new int[Transition.values().length];
            try {
                iArr[Transition.Incoming.ordinal()] = 3;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Transition.None.ordinal()] = 1;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Transition.Outgoing.ordinal()] = 4;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[Transition.PhotoIncoming.ordinal()] = 2;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[Transition.SlideOutgoing.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            -com-huawei-gallery-anim-StateTransitionAnimation$TransitionSwitchesValues = iArr;
            return iArr;
        }

        private static BaseTransition specForTransitionX(Transition t) {
            switch (-getcom-huawei-gallery-anim-StateTransitionAnimation$TransitionSwitchesValues()[t.ordinal()]) {
                case 2:
                    return PHOTO_INCOMING_ANIMATION;
                default:
                    return null;
            }
        }
    }

    public enum Transition {
        None,
        Outgoing,
        Incoming,
        PhotoIncoming,
        SlideOutgoing
    }

    public StateTransitionAnimation(Transition t, TransitionStore store) {
        this(Spec.specForTransition(t), store);
        if (GalleryUtils.IS_SUPPORT_HW_ANIMATION) {
            this.mTransitionX = SpecX.specForTransitionX(t);
        }
        if (this.mTransitionX != null) {
            this.mOldAnimTexture = (RawTexture) store.get("anim_texture");
            this.mRectOld = (Rect) store.get("start_pos");
            setInterpolator(new CubicBezierInterpolator(0.3f, 0.15f, 0.1f, 0.85f));
            setDuration(250);
        }
    }

    public StateTransitionAnimation(Spec spec, TransitionStore store) {
        if (spec == null) {
            spec = Spec.OUTGOING;
        }
        this.mTransitionSpec = spec;
        setDuration(this.mTransitionSpec.duration);
        setInterpolator(this.mTransitionSpec.interpolator);
        this.mOldScreenTexture = (RawTexture) store.get("fade_texture");
        TiledScreenNail.disableDrawPlaceholder();
    }

    public boolean calculate(long currentTimeMillis) {
        boolean retval = super.calculate(currentTimeMillis);
        if (!isActive()) {
            if (this.mOldScreenTexture != null) {
                this.mOldScreenTexture.recycle();
                this.mOldScreenTexture = null;
            }
            if (this.mOldAnimTexture != null) {
                this.mOldAnimTexture.recycle();
                this.mOldAnimTexture = null;
            }
            TiledScreenNail.enableDrawPlaceholder();
        }
        return retval;
    }

    protected void onCalculate(float progress) {
        if (this.mTransitionX != null) {
            this.mProgress = progress;
            if (this.mTransitionX == SpecX.PHOTO_INCOMING_ANIMATION) {
                if (this.mProgress < WMElement.CAMERASIZEVALUE1B1) {
                    this.mCurrentBackgroundAlpha = WMElement.CAMERASIZEVALUE1B1;
                } else {
                    this.mCurrentBackgroundAlpha = 0.0f;
                }
            }
            return;
        }
        this.mCurrentContentScale = this.mTransitionSpec.contentScaleFrom + ((this.mTransitionSpec.contentScaleTo - this.mTransitionSpec.contentScaleFrom) * progress);
        this.mCurrentContentAlpha = this.mTransitionSpec.contentAlphaFrom + ((this.mTransitionSpec.contentAlphaTo - this.mTransitionSpec.contentAlphaFrom) * progress);
        this.mCurrentBackgroundAlpha = this.mTransitionSpec.backgroundAlphaFrom + ((this.mTransitionSpec.backgroundAlphaTo - this.mTransitionSpec.backgroundAlphaFrom) * progress);
        this.mCurrentBackgroundScale = this.mTransitionSpec.backgroundScaleFrom + ((this.mTransitionSpec.backgroundScaleTo - this.mTransitionSpec.backgroundScaleFrom) * progress);
        this.mCurrentOverlayScale = this.mTransitionSpec.overlayScaleFrom + ((this.mTransitionSpec.overlayScaleTo - this.mTransitionSpec.overlayScaleFrom) * progress);
        this.mCurrentOverlayAlpha = this.mTransitionSpec.overlayAlphaFrom + ((this.mTransitionSpec.overlayAlphaTo - this.mTransitionSpec.overlayAlphaFrom) * progress);
    }

    private void applyOldTexture(GLView view, GLCanvas canvas, float alpha, float scale, boolean clear) {
        if (this.mOldScreenTexture != null) {
            if (clear) {
                canvas.clearBuffer(view.getBackgroundColor());
            }
            canvas.save();
            if (this.mTransitionX == null || this.mRectOld == null) {
                canvas.setAlpha(alpha);
                int xOffset = view.getWidth() / 2;
                int yOffset = view.getHeight() / 2;
                canvas.translate((float) xOffset, (float) yOffset);
                canvas.scale(scale, scale, WMElement.CAMERASIZEVALUE1B1);
                this.mOldScreenTexture.draw(canvas, -xOffset, -yOffset);
            } else {
                applyOldTextureX(view, canvas, clear);
            }
            canvas.restore();
        }
    }

    private void applyOldTextureX(GLView view, GLCanvas canvas, boolean clear) {
        if (this.mRectContent == null) {
            this.mRectContent = new Rect(0, 0, view.getWidth(), view.getHeight());
        }
        if (this.mTransitionX == SpecX.PHOTO_INCOMING_ANIMATION) {
            float oalpha = canvas.getAlpha();
            if (this.mProgress > 0.5f) {
                canvas.multiplyAlpha(WMElement.CAMERASIZEVALUE1B1 - ((this.mProgress - 0.5f) / 0.5f));
            }
            this.mOldScreenTexture.draw(canvas, 0, 0);
            if (this.mProgress > 0.5f) {
                canvas.setAlpha(oalpha);
            }
        }
    }

    public void applyBackground(GLView view, GLCanvas canvas) {
        if (this.mCurrentBackgroundAlpha > 0.0f) {
            applyOldTexture(view, canvas, this.mCurrentBackgroundAlpha, this.mCurrentBackgroundScale, true);
        }
    }

    public void applyContentTransform(GLView view, GLCanvas canvas) {
        if (this.mTransitionX != null) {
            applyContentTransformX(view, canvas);
            return;
        }
        int xOffset = view.getWidth() / 2;
        int yOffset = view.getHeight() / 2;
        canvas.translate((float) xOffset, (float) yOffset);
        canvas.scale(this.mCurrentContentScale, this.mCurrentContentScale, WMElement.CAMERASIZEVALUE1B1);
        canvas.translate((float) (-xOffset), (float) (-yOffset));
        canvas.setAlpha(this.mCurrentContentAlpha);
    }

    private void applyContentTransformX(GLView view, GLCanvas canvas) {
        float process = this.mProgress;
        if (this.mRectContent == null) {
            this.mRectContent = new Rect(0, 0, view.getWidth(), view.getHeight());
        }
        this.mTransitionX.setOldRect(this.mRectOld);
        TransformationInfo info = this.mTransitionX.getTransformation3D(process, this.mRectContent);
        if (info != null) {
            if (info.mMatrixDirty) {
                canvas.multiplyMatrix(info.mMatrix3D, 0);
            }
            if (info.mAlphaDirty) {
                canvas.multiplyAlpha(info.mAlpha);
            }
        }
    }

    public void applyOverlay(GLView view, GLCanvas canvas) {
        if (this.mCurrentOverlayAlpha > 0.0f) {
            applyOldTexture(view, canvas, this.mCurrentOverlayAlpha, this.mCurrentOverlayScale, false);
        }
    }

    public final float getProgress() {
        return this.mProgress;
    }
}
