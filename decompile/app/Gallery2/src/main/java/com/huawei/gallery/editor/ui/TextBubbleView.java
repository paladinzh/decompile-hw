package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.widget.RelativeLayout;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.gallery.editor.cache.BubbleCache;
import com.huawei.gallery.editor.filters.FilterLabelRepresentation.LabelHolder;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.ui.GalleryCustEditor;
import com.huawei.gallery.ui.GalleryCustEditor.OnTextChangedListener;
import com.huawei.watermark.manager.parse.WMElement;
import java.lang.ref.SoftReference;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class TextBubbleView extends View {
    private static final /* synthetic */ int[] -com-huawei-gallery-editor-ui-TextBubbleView$ClickStateSwitchesValues = null;
    private static final int NO_BORDER_BUBBLE_INIT_HEIGHT = GalleryUtils.dpToPixel(40);
    private static final int NO_BORDER_BUBBLE_INIT_WIDTH = GalleryUtils.dpToPixel((int) SmsCheckResult.ESCT_160);
    private static Paint sDecroPaint;
    private static SoftReference<Bitmap> sDeleteIconReference;
    private static SoftReference<Bitmap> sRotateAndScaleBubbleIconReference;
    private NinePatchDrawable mBubble;
    private BubbleDelegate mBubbleDelegate;
    private Rect mBubbleInitRect = new Rect();
    private PointF mCenterPoint = new PointF(0.5f, 0.5f);
    private String mContent;
    private ClickState mCurrentClickState = ClickState.MOVE;
    private float mCurrentRotatedAngle = 0.0f;
    private float mCurrentScale = WMElement.CAMERASIZEVALUE1B1;
    private long mId;
    private GalleryCustEditor mLabelEditor;
    private PointF mLastCenterPoint = new PointF();
    private float mLastRotatedAngle;
    private LabelPainData mPaintData;
    private boolean mRenderRequested = false;
    private ScaleGestureDetector mScaleGestureDetector;
    private StaticLayout mStaticLayout;
    private RectF mTextInitRect = new RectF();
    private TextPaint mTextPaint = new TextPaint();
    private RectF mTmpRect = new RectF();
    private PointF mTouchCurrentPoint = new PointF();
    private PointF mTouchLastPoint = new PointF(Float.MIN_VALUE, Float.MIN_VALUE);
    private float mTouchStartAngle;
    private PointF mTouchStartPoint = new PointF();
    private boolean mUserChoosed;
    private int mViewInitHeight;
    private double mViewInitRadius;
    private int mViewInitWidth;

    public interface BubbleDelegate {
        BubbleCache getBubbleCache();

        boolean isSecureCameraMode();

        void onUserChoosed(TextBubbleView textBubbleView);

        void onUserDeleted(TextBubbleView textBubbleView);
    }

    private enum ClickState {
        DELETE(0),
        ROTATE_AND_SCALE(1),
        MOVE(2),
        INPUT(3),
        OUT(4),
        SCALE(5);
        
        final int state;

        private ClickState(int s) {
            this.state = s;
        }
    }

    private class LoadThumbnailTask extends AsyncTask<Void, Void, NinePatchDrawable> {
        private int mRes;

        LoadThumbnailTask(int res) {
            this.mRes = res;
        }

        protected NinePatchDrawable doInBackground(Void... voids) {
            BubbleDelegate bubbleDelegate = TextBubbleView.this.mBubbleDelegate;
            if (bubbleDelegate == null || this.mRes == 0) {
                return null;
            }
            return bubbleDelegate.getBubbleCache().getBubble(this.mRes, TextBubbleView.this.getContext());
        }

        protected void onPostExecute(NinePatchDrawable drawable) {
            TextBubbleView.this.mBubble = drawable;
            if (drawable != null) {
                int intrinsicWidth = drawable.getIntrinsicWidth();
                int intrinsicHeight = drawable.getIntrinsicHeight();
                TextBubbleView.this.mViewInitWidth = (int) (((float) intrinsicWidth) * 0.6f);
                TextBubbleView.this.mViewInitHeight = (int) (((float) intrinsicHeight) * 0.6f);
                TextBubbleView.this.mViewInitRadius = Math.hypot((double) TextBubbleView.this.mViewInitWidth, (double) TextBubbleView.this.mViewInitHeight) / 2.0d;
                TextBubbleView.this.mBubbleInitRect.set(0, 0, TextBubbleView.this.mViewInitWidth, TextBubbleView.this.mViewInitHeight);
                Rect padding = new Rect();
                drawable.getPadding(padding);
                GalleryLog.d("TextBubbleView", "width:" + intrinsicWidth + ", height:" + intrinsicHeight + ", bubble padding:" + padding);
                TextBubbleView.this.mTextInitRect.set(((float) TextBubbleView.this.mBubbleInitRect.left) + (((float) TextBubbleView.this.mBubbleInitRect.width()) * (((float) padding.left) / ((float) intrinsicWidth))), ((float) TextBubbleView.this.mBubbleInitRect.top) + (((float) TextBubbleView.this.mBubbleInitRect.height()) * (((float) padding.top) / ((float) intrinsicHeight))), ((float) TextBubbleView.this.mBubbleInitRect.left) + (((float) TextBubbleView.this.mBubbleInitRect.width()) * (WMElement.CAMERASIZEVALUE1B1 - (((float) padding.right) / ((float) intrinsicWidth)))), ((float) TextBubbleView.this.mBubbleInitRect.top) + (((float) TextBubbleView.this.mBubbleInitRect.height()) * (WMElement.CAMERASIZEVALUE1B1 - (((float) padding.bottom) / ((float) intrinsicHeight)))));
                GalleryLog.d("TextBubbleView", "mBubbleInitRect:" + TextBubbleView.this.mBubbleInitRect + ", mTextInitRect:" + TextBubbleView.this.mTextInitRect);
            } else {
                TextBubbleView.this.mViewInitWidth = TextBubbleView.NO_BORDER_BUBBLE_INIT_WIDTH;
                TextBubbleView.this.mViewInitHeight = TextBubbleView.NO_BORDER_BUBBLE_INIT_HEIGHT;
                TextBubbleView.this.mViewInitRadius = Math.hypot((double) TextBubbleView.this.mViewInitWidth, (double) TextBubbleView.this.mViewInitHeight) / 2.0d;
                TextBubbleView.this.mBubbleInitRect.set(0, 0, TextBubbleView.this.mViewInitWidth, TextBubbleView.this.mViewInitHeight);
                TextBubbleView.this.mTextInitRect.set(((float) TextBubbleView.this.mBubbleInitRect.left) + (((float) TextBubbleView.this.mBubbleInitRect.width()) * 0.125f), ((float) TextBubbleView.this.mBubbleInitRect.top) + (((float) TextBubbleView.this.mBubbleInitRect.height()) * 0.26f), ((float) TextBubbleView.this.mBubbleInitRect.left) + (((float) TextBubbleView.this.mBubbleInitRect.width()) * 0.875f), ((float) TextBubbleView.this.mBubbleInitRect.top) + (((float) TextBubbleView.this.mBubbleInitRect.height()) * 0.74f));
                GalleryLog.d("TextBubbleView", "mBubbleInitRect:" + TextBubbleView.this.mBubbleInitRect + ", mTextInitRect:" + TextBubbleView.this.mTextInitRect);
            }
            TextBubbleView.this.mContent = TextBubbleView.this.mContent == null ? TextBubbleView.this.getContext().getResources().getString(R.string.edit_bubble_label) : TextBubbleView.this.mContent;
            TextBubbleView.this.mPaintData.updateTextPaint(TextBubbleView.this.mTextPaint);
            TextBubbleView.this.mStaticLayout = EditorUtils.getStaticLayout(TextBubbleView.this.mContent, TextBubbleView.this.mTextPaint, TextBubbleView.this.mTextInitRect);
            TextBubbleView.this.invalidate();
        }
    }

    private class MyScaleListener extends SimpleOnScaleGestureListener {
        private MyScaleListener() {
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            TextBubbleView.this.mCurrentClickState = ClickState.SCALE;
            return super.onScaleBegin(detector);
        }

        public boolean onScale(ScaleGestureDetector detector) {
            TextBubbleView textBubbleView = TextBubbleView.this;
            textBubbleView.mCurrentScale = textBubbleView.mCurrentScale * detector.getScaleFactor();
            TextBubbleView.this.mCurrentScale = Math.min(TextBubbleView.this.mCurrentScale, 4.0f);
            TextBubbleView.this.invalidate();
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-editor-ui-TextBubbleView$ClickStateSwitchesValues() {
        if (-com-huawei-gallery-editor-ui-TextBubbleView$ClickStateSwitchesValues != null) {
            return -com-huawei-gallery-editor-ui-TextBubbleView$ClickStateSwitchesValues;
        }
        int[] iArr = new int[ClickState.values().length];
        try {
            iArr[ClickState.DELETE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ClickState.INPUT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ClickState.MOVE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ClickState.OUT.ordinal()] = 5;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ClickState.ROTATE_AND_SCALE.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ClickState.SCALE.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        -com-huawei-gallery-editor-ui-TextBubbleView$ClickStateSwitchesValues = iArr;
        return iArr;
    }

    public TextBubbleView(Context context, BubbleDelegate delegate, LabelPainData painData) {
        super(context);
        this.mScaleGestureDetector = new ScaleGestureDetector(context, new MyScaleListener());
        this.mId = LabelId.nextId();
        this.mBubbleDelegate = delegate;
        this.mPaintData = new LabelPainData(painData);
        this.mPaintData.updateTextPaint(this.mTextPaint);
        new LoadThumbnailTask(this.mPaintData.iconRes).execute(new Void[0]);
    }

    private void requestRender() {
        if (!this.mRenderRequested) {
            this.mRenderRequested = true;
            invalidate();
        }
    }

    public void pause() {
        if (this.mLabelEditor != null) {
            this.mLabelEditor.pause();
            this.mLabelEditor = null;
        }
    }

    public void updateStaticLayout(LabelPainData painData) {
        LabelPainData oldPaintData = this.mPaintData;
        this.mPaintData = new LabelPainData(painData);
        if (oldPaintData.iconRes != this.mPaintData.iconRes) {
            new LoadThumbnailTask(this.mPaintData.iconRes).execute(new Void[0]);
            return;
        }
        this.mContent = this.mContent == null ? getContext().getResources().getString(R.string.edit_bubble_label) : this.mContent;
        this.mPaintData.updateTextPaint(this.mTextPaint);
        this.mStaticLayout = EditorUtils.getStaticLayout(this.mContent, this.mTextPaint, this.mTextInitRect);
        invalidate();
    }

    public LabelPainData getPaintData() {
        return this.mPaintData;
    }

    public void updateLabelHolderData(LabelHolder labelHolder) {
        labelHolder.updateParameters(this.mId, this.mViewInitWidth, this.mViewInitHeight, this.mBubbleInitRect, this.mTextInitRect, this.mCurrentRotatedAngle, this.mCurrentScale, this.mContent == null ? getContext().getResources().getString(R.string.edit_bubble_label) : this.mContent, this.mCenterPoint, this.mPaintData);
    }

    private void drawBubble(Canvas canvas) {
        NinePatchDrawable bubble = this.mBubble;
        if (bubble != null) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            canvas.save();
            canvas.translate(this.mCenterPoint.x * ((float) width), this.mCenterPoint.y * ((float) height));
            canvas.rotate((-this.mCurrentRotatedAngle) * 57.295776f);
            canvas.scale(this.mCurrentScale, this.mCurrentScale);
            canvas.translate(((float) (-this.mViewInitWidth)) / 2.0f, ((float) (-this.mViewInitHeight)) / 2.0f);
            canvas.translate((float) this.mBubbleInitRect.left, (float) this.mBubbleInitRect.top);
            Rect bubbleRect = new Rect(0, 0, this.mBubbleInitRect.width(), this.mBubbleInitRect.height());
            bubble.setFilterBitmap(true);
            bubble.setDither(true);
            bubble.setBounds(bubbleRect);
            bubble.draw(canvas);
            canvas.restore();
        }
    }

    private void drawText(Canvas canvas) {
        StaticLayout staticLayout = this.mStaticLayout;
        if (staticLayout != null) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            canvas.save();
            canvas.translate(this.mCenterPoint.x * ((float) width), this.mCenterPoint.y * ((float) height));
            canvas.rotate((-this.mCurrentRotatedAngle) * 57.295776f);
            canvas.scale(this.mCurrentScale, this.mCurrentScale);
            canvas.translate(((float) (-this.mViewInitWidth)) / 2.0f, ((float) (-this.mViewInitHeight)) / 2.0f);
            canvas.translate(this.mTextInitRect.left + ((this.mTextInitRect.width() - ((float) staticLayout.getWidth())) / 2.0f), this.mTextInitRect.top + ((this.mTextInitRect.height() - ((float) staticLayout.getHeight())) / 2.0f));
            staticLayout.draw(canvas);
            canvas.restore();
        }
    }

    private void drawDecor(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        canvas.save();
        canvas.translate(this.mCenterPoint.x * ((float) width), this.mCenterPoint.y * ((float) height));
        canvas.rotate((-this.mCurrentRotatedAngle) * 57.295776f);
        canvas.translate((((float) (-this.mViewInitWidth)) * this.mCurrentScale) / 2.0f, (((float) (-this.mViewInitHeight)) * this.mCurrentScale) / 2.0f);
        canvas.drawRect(0.0f, 0.0f, ((float) this.mViewInitWidth) * this.mCurrentScale, ((float) this.mViewInitHeight) * this.mCurrentScale, getDecroPaint());
        Bitmap deleteIcon = getDeleteIcon(getContext());
        canvas.drawBitmap(deleteIcon, ((float) (-deleteIcon.getWidth())) / 2.0f, ((float) (-deleteIcon.getHeight())) / 2.0f, null);
        Bitmap rotateAndScaleIcon = getRotateAndScaleBubbleIcon(getContext());
        canvas.drawBitmap(rotateAndScaleIcon, (((float) this.mViewInitWidth) * this.mCurrentScale) - (((float) rotateAndScaleIcon.getWidth()) / 2.0f), (((float) this.mViewInitHeight) * this.mCurrentScale) - (((float) rotateAndScaleIcon.getHeight()) / 2.0f), null);
        canvas.restore();
    }

    protected void onDraw(Canvas canvas) {
        this.mRenderRequested = false;
        drawBubble(canvas);
        drawText(canvas);
        if (this.mCurrentClickState != ClickState.OUT) {
            drawDecor(canvas);
        }
        notifyUserChoosedIfNeed();
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (oldw > 0 && oldh > 0 && w > 0 && h > 0) {
            this.mCurrentScale *= Math.min(((float) w) / ((float) oldw), ((float) h) / ((float) oldh));
            this.mCurrentScale = Math.min(this.mCurrentScale, 4.0f);
            invalidate();
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        switch (ev.getAction()) {
            case 0:
                this.mLastCenterPoint.set(this.mCenterPoint);
                this.mTouchStartPoint.set(ev.getX(), ev.getY());
                this.mCurrentClickState = getClickState();
                if (this.mCurrentClickState != ClickState.ROTATE_AND_SCALE) {
                    if (this.mCurrentClickState == ClickState.OUT) {
                        requestRender();
                        break;
                    }
                }
                this.mLastRotatedAngle = this.mCurrentRotatedAngle;
                this.mTouchStartAngle = EditorUtils.calculateAngle(this.mTouchStartPoint.x, this.mTouchStartPoint.y, this.mCenterPoint.x * ((float) getWidth()), this.mCenterPoint.y * ((float) getHeight()));
                break;
                break;
            case 1:
            case 3:
                this.mTouchCurrentPoint.set(ev.getX(), ev.getY());
                if (calculateCenterPoint()) {
                    requestRender();
                }
                if (this.mCurrentClickState != ClickState.INPUT || isMoveEnough(this.mTouchCurrentPoint, this.mTouchStartPoint)) {
                    if (this.mCurrentClickState == ClickState.DELETE && !isMoveEnough(this.mTouchCurrentPoint, this.mTouchStartPoint)) {
                        post(new Runnable() {
                            public void run() {
                                if (TextBubbleView.this.mBubbleDelegate != null) {
                                    TextBubbleView.this.mBubbleDelegate.onUserDeleted(TextBubbleView.this);
                                }
                            }
                        });
                        break;
                    }
                }
                post(new Runnable() {
                    public void run() {
                        TextBubbleView.this.mLabelEditor = new GalleryCustEditor(TextBubbleView.this.getContext(), TextBubbleView.this.mContent, new OnTextChangedListener() {
                            public void onTextChanged(String text) {
                                TextBubbleView.this.mContent = text;
                                TextBubbleView.this.mPaintData.updateTextPaint(TextBubbleView.this.mTextPaint);
                                TextBubbleView.this.mStaticLayout = EditorUtils.getStaticLayout(TextBubbleView.this.mContent, TextBubbleView.this.mTextPaint, TextBubbleView.this.mTextInitRect);
                                TextBubbleView.this.invalidate();
                            }
                        }, TextBubbleView.this.inSecureCameraMode());
                        TextBubbleView.this.mLabelEditor.updateDialogWindowSize();
                    }
                });
                break;
                break;
            case 2:
                this.mTouchCurrentPoint.set(ev.getX(), ev.getY());
                if (calculateCenterPoint()) {
                    requestRender();
                    break;
                }
                break;
        }
        boolean clickOut = this.mCurrentClickState == ClickState.OUT;
        if (!clickOut) {
            this.mScaleGestureDetector.onTouchEvent(ev);
        }
        if (clickOut) {
            return false;
        }
        return true;
    }

    private static boolean isMoveEnough(PointF p1, PointF p2) {
        return Math.hypot((double) Math.abs(p1.x - p2.x), (double) Math.abs(p1.y - p2.y)) > 4.0d;
    }

    private static boolean isScaleAndRotateNotEnough(PointF p1, PointF p2) {
        if (Math.abs(p1.x - p2.x) >= WMElement.CAMERASIZEVALUE1B1 || Math.abs(p1.y - p2.y) >= WMElement.CAMERASIZEVALUE1B1) {
            return false;
        }
        return true;
    }

    private boolean calculateCenterPoint() {
        switch (-getcom-huawei-gallery-editor-ui-TextBubbleView$ClickStateSwitchesValues()[this.mCurrentClickState.ordinal()]) {
            case 1:
            case 2:
            case 3:
                PointF touchStartPoint = this.mTouchStartPoint;
                PointF touchCurrentPoint = this.mTouchCurrentPoint;
                float deltaWidth = touchStartPoint.x - touchCurrentPoint.x;
                float deltaHeight = touchStartPoint.y - touchCurrentPoint.y;
                this.mCenterPoint.x = Utils.clamp(this.mLastCenterPoint.x - (deltaWidth / ((float) getWidth())), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
                this.mCenterPoint.y = Utils.clamp(this.mLastCenterPoint.y - (deltaHeight / ((float) getHeight())), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
                return true;
            case 4:
                if (isScaleAndRotateNotEnough(this.mTouchLastPoint, this.mTouchCurrentPoint)) {
                    return false;
                }
                this.mTouchLastPoint.set(this.mTouchCurrentPoint);
                this.mCurrentScale = (float) (Math.hypot((double) Math.abs(this.mTouchCurrentPoint.x - (this.mCenterPoint.x * ((float) getWidth()))), (double) Math.abs(this.mTouchCurrentPoint.y - (this.mCenterPoint.y * ((float) getHeight())))) / this.mViewInitRadius);
                this.mCurrentScale = Math.min(this.mCurrentScale, 4.0f);
                this.mCurrentRotatedAngle = (EditorUtils.calculateAngle(this.mTouchCurrentPoint.x, this.mTouchCurrentPoint.y, this.mCenterPoint.x * ((float) getWidth()), this.mCenterPoint.y * ((float) getHeight())) - this.mTouchStartAngle) + this.mLastRotatedAngle;
                return true;
            default:
                return false;
        }
    }

    public boolean isUserChoosed() {
        return this.mUserChoosed;
    }

    public void clearClickState() {
        this.mCurrentClickState = ClickState.OUT;
        this.mUserChoosed = false;
        invalidate();
    }

    private ClickState getClickState() {
        Bitmap rotateAndScaleIcon = getRotateAndScaleBubbleIcon(getContext());
        float left = (this.mCenterPoint.x * ((float) getWidth())) + ((((float) this.mViewInitWidth) * this.mCurrentScale) / 2.0f);
        float top = (this.mCenterPoint.y * ((float) getHeight())) + ((((float) this.mViewInitHeight) * this.mCurrentScale) / 2.0f);
        this.mTmpRect.set(left - (((float) rotateAndScaleIcon.getWidth()) / 2.0f), top - (((float) rotateAndScaleIcon.getHeight()) / 2.0f), (((float) rotateAndScaleIcon.getWidth()) / 2.0f) + left, (((float) rotateAndScaleIcon.getHeight()) / 2.0f) + top);
        if (isClickInRect(this.mTouchStartPoint, this.mTmpRect, this.mCurrentRotatedAngle)) {
            return ClickState.ROTATE_AND_SCALE;
        }
        left = ((this.mCenterPoint.x * ((float) getWidth())) - ((((float) this.mViewInitWidth) * this.mCurrentScale) / 2.0f)) + (this.mTextInitRect.left * this.mCurrentScale);
        top = ((this.mCenterPoint.y * ((float) getHeight())) - ((((float) this.mViewInitHeight) * this.mCurrentScale) / 2.0f)) + (this.mTextInitRect.top * this.mCurrentScale);
        this.mTmpRect.set(left, top, (this.mTextInitRect.width() * this.mCurrentScale) + left, (this.mTextInitRect.height() * this.mCurrentScale) + top);
        if (isClickInRect(this.mTouchStartPoint, this.mTmpRect, this.mCurrentRotatedAngle)) {
            return ClickState.INPUT;
        }
        left = (this.mCenterPoint.x * ((float) getWidth())) - ((((float) this.mViewInitWidth) * this.mCurrentScale) / 2.0f);
        top = (this.mCenterPoint.y * ((float) getHeight())) - ((((float) this.mViewInitHeight) * this.mCurrentScale) / 2.0f);
        this.mTmpRect.set(left, top, (((float) this.mViewInitWidth) * this.mCurrentScale) + left, (((float) this.mViewInitHeight) * this.mCurrentScale) + top);
        if (isClickInRect(this.mTouchStartPoint, this.mTmpRect, this.mCurrentRotatedAngle)) {
            return ClickState.MOVE;
        }
        Bitmap deleteIcon = getDeleteIcon(getContext());
        left = (this.mCenterPoint.x * ((float) getWidth())) - ((((float) this.mViewInitWidth) * this.mCurrentScale) / 2.0f);
        top = (this.mCenterPoint.y * ((float) getHeight())) - ((((float) this.mViewInitHeight) * this.mCurrentScale) / 2.0f);
        this.mTmpRect.set(left - ((float) deleteIcon.getWidth()), top - ((float) deleteIcon.getHeight()), ((float) deleteIcon.getWidth()) + left, ((float) deleteIcon.getHeight()) + top);
        if (isClickInRect(this.mTouchStartPoint, this.mTmpRect, this.mCurrentRotatedAngle)) {
            return ClickState.DELETE;
        }
        return ClickState.OUT;
    }

    private void notifyUserChoosedIfNeed() {
        if (this.mCurrentClickState == ClickState.OUT) {
            this.mUserChoosed = false;
        } else if (!this.mUserChoosed) {
            this.mUserChoosed = true;
            RelativeLayout parent = (RelativeLayout) getParent();
            for (int index = 0; index < parent.getChildCount(); index++) {
                View view = parent.getChildAt(index);
                if (view instanceof TextBubbleView) {
                    TextBubbleView bubbleView = (TextBubbleView) view;
                    if (bubbleView != this) {
                        bubbleView.clearClickState();
                    }
                }
            }
            if (this.mBubbleDelegate != null) {
                this.mBubbleDelegate.onUserChoosed(this);
            }
        }
    }

    private boolean isClickInRect(PointF clickPoint, RectF src, float rotateAngle) {
        float[] matrix = new float[16];
        float[] iden = new float[16];
        Matrix.setIdentityM(iden, 0);
        Matrix.rotateM(matrix, 0, iden, 0, 57.295776f * rotateAngle, 0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1);
        rt = new float[4];
        st = new float[4];
        float centerX = this.mCenterPoint.x * ((float) getWidth());
        float centerY = this.mCenterPoint.y * ((float) getHeight());
        st[0] = clickPoint.x - centerX;
        st[1] = clickPoint.y - centerY;
        Matrix.multiplyMV(rt, 0, matrix, 0, st, 0);
        return src.contains(rt[0] + centerX, rt[1] + centerY);
    }

    private static synchronized Bitmap getDeleteIcon(Context context) {
        synchronized (TextBubbleView.class) {
            Bitmap deleteIcon;
            if (sDeleteIconReference != null) {
                deleteIcon = (Bitmap) sDeleteIconReference.get();
                if (deleteIcon != null) {
                    return deleteIcon;
                }
            }
            deleteIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_gallery_edit_label_delete);
            sDeleteIconReference = new SoftReference(deleteIcon);
            return deleteIcon;
        }
    }

    private static synchronized Bitmap getRotateAndScaleBubbleIcon(Context context) {
        synchronized (TextBubbleView.class) {
            Bitmap rotateAndScaleBubbleIcon;
            if (sRotateAndScaleBubbleIconReference != null) {
                rotateAndScaleBubbleIcon = (Bitmap) sRotateAndScaleBubbleIconReference.get();
                if (rotateAndScaleBubbleIcon != null) {
                    return rotateAndScaleBubbleIcon;
                }
            }
            rotateAndScaleBubbleIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_gallery_edit_label_rotate);
            sRotateAndScaleBubbleIconReference = new SoftReference(rotateAndScaleBubbleIcon);
            return rotateAndScaleBubbleIcon;
        }
    }

    private static synchronized Paint getDecroPaint() {
        synchronized (TextBubbleView.class) {
            if (sDecroPaint != null) {
                Paint paint = sDecroPaint;
                return paint;
            }
            sDecroPaint = new Paint();
            sDecroPaint.setColor(Color.parseColor("#80FFFFFF"));
            sDecroPaint.setStyle(Style.STROKE);
            sDecroPaint.setStrokeCap(Cap.SQUARE);
            sDecroPaint.setAntiAlias(true);
            sDecroPaint.setStrokeWidth(MapConfig.MIN_ZOOM);
            paint = sDecroPaint;
            return paint;
        }
    }

    private boolean inSecureCameraMode() {
        return this.mBubbleDelegate != null ? this.mBubbleDelegate.isSecureCameraMode() : false;
    }
}
