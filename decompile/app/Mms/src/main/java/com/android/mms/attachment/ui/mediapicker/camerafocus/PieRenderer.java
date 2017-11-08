package com.android.mms.attachment.ui.mediapicker.camerafocus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public class PieRenderer extends OverlayRenderer implements FocusIndicator {
    private ScaleAnimation mAnimation = new ScaleAnimation();
    private boolean mBlockFocus;
    private Point mCenter;
    private int mCenterX;
    private int mCenterY;
    private RectF mCircle;
    private int mCircleSize;
    private PieItem mCurrentItem;
    private RectF mDial;
    private int mDialAngle;
    private Runnable mDisappear = new Disappear();
    private Point mDown;
    private AnimationListener mEndAction = new EndAction();
    private LinearAnimation mFadeIn;
    private int mFailColor;
    private volatile boolean mFocusCancelled;
    private Paint mFocusPaint;
    private int mFocusX;
    private int mFocusY;
    private boolean mFocused;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (PieRenderer.this.mListener != null) {
                        PieRenderer.this.mListener.onPieOpened(PieRenderer.this.mCenter.x, PieRenderer.this.mCenter.y);
                        return;
                    }
                    return;
                case 1:
                    if (PieRenderer.this.mListener != null) {
                        PieRenderer.this.mListener.onPieClosed();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private int mInnerOffset;
    private int mInnerStroke;
    private List<PieItem> mItems;
    private PieListener mListener;
    private PieItem mOpenItem;
    private boolean mOpening;
    private int mOuterStroke;
    private Point mPoint1;
    private Point mPoint2;
    private int mRadius;
    private int mRadiusInc;
    private Paint mSelectedPaint;
    private int mStartAnimationAngle;
    private volatile int mState;
    private Paint mSubPaint;
    private int mSuccessColor;
    private boolean mTapMode;
    private int mTouchOffset;
    private int mTouchSlopSquared;
    private LinearAnimation mXFade;

    private class Disappear implements Runnable {
        private Disappear() {
        }

        public void run() {
            if (PieRenderer.this.mState != 8) {
                PieRenderer.this.setVisible(false);
                PieRenderer.this.mFocusX = PieRenderer.this.mCenterX;
                PieRenderer.this.mFocusY = PieRenderer.this.mCenterY;
                PieRenderer.this.mState = 0;
                PieRenderer.this.setCircle(PieRenderer.this.mFocusX, PieRenderer.this.mFocusY);
                PieRenderer.this.mFocused = false;
            }
        }
    }

    private class EndAction implements AnimationListener {
        private EndAction() {
        }

        public void onAnimationEnd(Animation animation) {
            if (!PieRenderer.this.mFocusCancelled) {
                PieRenderer.this.mOverlay.postDelayed(PieRenderer.this.mDisappear, 200);
            }
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
    }

    private static class LinearAnimation extends Animation {
        private float mFrom;
        private float mTo;
        private float mValue;

        public LinearAnimation(float from, float to) {
            setFillAfter(true);
            setInterpolator(new LinearInterpolator());
            this.mFrom = from;
            this.mTo = to;
        }

        public float getValue() {
            return this.mValue;
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            this.mValue = this.mFrom + ((this.mTo - this.mFrom) * interpolatedTime);
        }
    }

    public interface PieListener {
        void onPieClosed();

        void onPieOpened(int i, int i2);
    }

    private class ScaleAnimation extends Animation {
        private float mFrom = ContentUtil.FONT_SIZE_NORMAL;
        private float mTo = ContentUtil.FONT_SIZE_NORMAL;

        public ScaleAnimation() {
            setFillAfter(true);
        }

        public void setScale(float from, float to) {
            this.mFrom = from;
            this.mTo = to;
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            PieRenderer.this.mDialAngle = (int) (this.mFrom + ((this.mTo - this.mFrom) * interpolatedTime));
        }
    }

    public PieRenderer(Context context) {
        init(context);
    }

    private void init(Context ctx) {
        setVisible(false);
        this.mItems = new ArrayList();
        Resources res = ctx.getResources();
        this.mRadius = res.getDimensionPixelSize(R.dimen.pie_radius_start);
        this.mCircleSize = this.mRadius - res.getDimensionPixelSize(R.dimen.focus_radius_offset);
        this.mRadiusInc = res.getDimensionPixelSize(R.dimen.pie_radius_increment);
        this.mTouchOffset = res.getDimensionPixelSize(R.dimen.pie_touch_offset);
        this.mCenter = new Point(0, 0);
        this.mSelectedPaint = new Paint();
        this.mSelectedPaint.setColor(Color.argb(255, 51, 181, 229));
        this.mSelectedPaint.setAntiAlias(true);
        this.mSubPaint = new Paint();
        this.mSubPaint.setAntiAlias(true);
        this.mSubPaint.setColor(Color.argb(SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE, 250, 230, 128));
        this.mFocusPaint = new Paint();
        this.mFocusPaint.setAntiAlias(true);
        this.mFocusPaint.setColor(-1);
        this.mFocusPaint.setStyle(Style.STROKE);
        this.mSuccessColor = -16711936;
        this.mFailColor = -65536;
        this.mCircle = new RectF();
        this.mDial = new RectF();
        this.mPoint1 = new Point();
        this.mPoint2 = new Point();
        this.mInnerOffset = res.getDimensionPixelSize(R.dimen.focus_inner_offset);
        this.mOuterStroke = res.getDimensionPixelSize(R.dimen.focus_outer_stroke);
        this.mInnerStroke = res.getDimensionPixelSize(R.dimen.focus_inner_stroke);
        this.mState = 0;
        this.mBlockFocus = false;
        this.mTouchSlopSquared = ViewConfiguration.get(ctx).getScaledTouchSlop();
        this.mTouchSlopSquared *= this.mTouchSlopSquared;
        this.mDown = new Point();
    }

    private void show(boolean show) {
        int i = 0;
        if (show) {
            this.mState = 8;
            this.mCurrentItem = null;
            this.mOpenItem = null;
            for (PieItem item : this.mItems) {
                item.setSelected(false);
            }
            layoutPie();
            fadeIn();
        } else {
            this.mState = 0;
            this.mTapMode = false;
            if (this.mXFade != null) {
                this.mXFade.cancel();
            }
        }
        setVisible(show);
        Handler handler = this.mHandler;
        if (!show) {
            i = 1;
        }
        handler.sendEmptyMessage(i);
    }

    private void fadeIn() {
        this.mFadeIn = new LinearAnimation(0.0f, ContentUtil.FONT_SIZE_NORMAL);
        this.mFadeIn.setDuration(200);
        this.mFadeIn.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                PieRenderer.this.mFadeIn = null;
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
        this.mFadeIn.startNow();
        this.mOverlay.startAnimation(this.mFadeIn);
    }

    public void setCenter(int x, int y) {
        this.mCenter.x = x;
        this.mCenter.y = y;
        alignFocus(x, y);
    }

    private void layoutPie() {
        layoutItems(this.mItems, 1.5707964f, this.mRadius + 2, (this.mRadius + this.mRadiusInc) - 2, 1);
    }

    private void layoutItems(List<PieItem> items, float centerAngle, int inner, int outer, int gap) {
        float sweep = (2.0943952f - 0.2617994f) / ((float) items.size());
        float angle = ((centerAngle - 1.0471976f) + 0.1308997f) + (sweep / 2.0f);
        for (PieItem item : items) {
            if (item.getCenter() >= 0.0f) {
                sweep = item.getSweep();
                break;
            }
        }
        Path path = makeSlice(getDegrees(0.0d) - ((float) gap), getDegrees((double) sweep) + ((float) gap), outer, inner, this.mCenter);
        float angle2 = angle;
        for (PieItem item2 : items) {
            item2.setPath(path);
            if (item2.getCenter() >= 0.0f) {
                angle2 = item2.getCenter();
            }
            int w = item2.getIntrinsicWidth();
            int h = item2.getIntrinsicHeight();
            int r = inner + (((outer - inner) * 2) / 3);
            int y = (this.mCenter.y - ((int) (((double) r) * Math.sin((double) angle2)))) - (h / 2);
            int x = (this.mCenter.x + ((int) (((double) r) * Math.cos((double) angle2)))) - (w / 2);
            item2.setBounds(x, y, x + w, y + h);
            item2.setGeometry(angle2 - (sweep / 2.0f), sweep, inner, outer);
            if (item2.hasItems()) {
                layoutItems(item2.getItems(), angle2, inner, outer + (this.mRadiusInc / 2), gap);
            }
            angle2 += sweep;
        }
    }

    private Path makeSlice(float start, float end, int outer, int inner, Point center) {
        RectF bb = new RectF((float) (center.x - outer), (float) (center.y - outer), (float) (center.x + outer), (float) (center.y + outer));
        RectF bbi = new RectF((float) (center.x - inner), (float) (center.y - inner), (float) (center.x + inner), (float) (center.y + inner));
        Path path = new Path();
        path.arcTo(bb, start, end - start, true);
        path.arcTo(bbi, end, start - end);
        path.close();
        return path;
    }

    private float getDegrees(double angle) {
        return (float) (360.0d - ((180.0d * angle) / 3.141592653589793d));
    }

    private void startFadeOut() {
        this.mOverlay.animate().alpha(0.0f).setListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                PieRenderer.this.deselect();
                PieRenderer.this.show(false);
                PieRenderer.this.mOverlay.setAlpha(ContentUtil.FONT_SIZE_NORMAL);
                super.onAnimationEnd(animation);
            }
        }).setDuration(300);
    }

    public void onDraw(Canvas canvas) {
        float alpha = ContentUtil.FONT_SIZE_NORMAL;
        if (this.mXFade != null) {
            alpha = this.mXFade.getValue();
        } else if (this.mFadeIn != null) {
            alpha = this.mFadeIn.getValue();
        }
        int state = canvas.save();
        if (this.mFadeIn != null) {
            float sf = 0.9f + (0.1f * alpha);
            canvas.scale(sf, sf, (float) this.mCenter.x, (float) this.mCenter.y);
        }
        drawFocus(canvas);
        if (this.mState == 2) {
            canvas.restoreToCount(state);
            return;
        }
        if (this.mOpenItem == null || this.mXFade != null) {
            for (PieItem item : this.mItems) {
                drawItem(canvas, item, alpha);
            }
        }
        if (this.mOpenItem != null) {
            for (PieItem inner : this.mOpenItem.getItems()) {
                drawItem(canvas, inner, this.mXFade != null ? ContentUtil.FONT_SIZE_NORMAL - (0.5f * alpha) : ContentUtil.FONT_SIZE_NORMAL);
            }
        }
        canvas.restoreToCount(state);
    }

    private void drawItem(Canvas canvas, PieItem item, float alpha) {
        if (this.mState == 8 && item.getPath() != null) {
            if (item.isSelected()) {
                Paint p = this.mSelectedPaint;
                int state = canvas.save();
                canvas.rotate(getDegrees((double) item.getStartAngle()), (float) this.mCenter.x, (float) this.mCenter.y);
                canvas.drawPath(item.getPath(), p);
                canvas.restoreToCount(state);
            }
            item.setAlpha(alpha * (item.isEnabled() ? ContentUtil.FONT_SIZE_NORMAL : 0.3f));
            item.draw(canvas);
        }
    }

    public boolean onTouchEvent(MotionEvent evt) {
        boolean z;
        float x = evt.getX();
        float y = evt.getY();
        int action = evt.getActionMasked();
        if (this.mTapMode) {
            z = false;
        } else {
            z = true;
        }
        PointF polar = getPolar(x, y, z);
        PieItem item;
        if (action == 0) {
            this.mDown.x = (int) evt.getX();
            this.mDown.y = (int) evt.getY();
            this.mOpening = false;
            if (this.mTapMode) {
                item = findItem(polar);
                if (!(item == null || this.mCurrentItem == item)) {
                    this.mState = 8;
                    onEnter(item);
                }
            } else {
                setCenter((int) x, (int) y);
                show(true);
            }
            return true;
        }
        if (1 == action) {
            if (isVisible()) {
                item = this.mCurrentItem;
                if (this.mTapMode) {
                    item = findItem(polar);
                    if (item != null && this.mOpening) {
                        this.mOpening = false;
                        return true;
                    }
                }
                if (item == null) {
                    this.mTapMode = false;
                    show(false);
                } else if (!(this.mOpening || item.hasItems())) {
                    item.performClick();
                    startFadeOut();
                    this.mTapMode = false;
                }
                return true;
            }
        } else if (3 == action) {
            if (isVisible() || this.mTapMode) {
                show(false);
            }
            deselect();
            return false;
        } else if (2 == action) {
            if (polar.y < ((float) this.mRadius)) {
                if (this.mOpenItem != null) {
                    this.mOpenItem = null;
                } else {
                    deselect();
                }
                return false;
            }
            item = findItem(polar);
            boolean moved = hasMoved(evt);
            if (!(item == null || this.mCurrentItem == item || (this.mOpening && !moved))) {
                this.mOpening = false;
                if (moved) {
                    this.mTapMode = false;
                }
                onEnter(item);
            }
        }
        return false;
    }

    private boolean hasMoved(MotionEvent e) {
        return ((float) this.mTouchSlopSquared) < ((e.getX() - ((float) this.mDown.x)) * (e.getX() - ((float) this.mDown.x))) + ((e.getY() - ((float) this.mDown.y)) * (e.getY() - ((float) this.mDown.y)));
    }

    private void onEnter(PieItem item) {
        if (this.mCurrentItem != null) {
            this.mCurrentItem.setSelected(false);
        }
        if (item == null || !item.isEnabled()) {
            this.mCurrentItem = null;
            return;
        }
        item.setSelected(true);
        this.mCurrentItem = item;
        if (this.mCurrentItem != this.mOpenItem && this.mCurrentItem.hasItems()) {
            openCurrentItem();
        }
    }

    private void deselect() {
        if (this.mCurrentItem != null) {
            this.mCurrentItem.setSelected(false);
        }
        if (this.mOpenItem != null) {
            this.mOpenItem = null;
        }
        this.mCurrentItem = null;
    }

    private void openCurrentItem() {
        if (this.mCurrentItem != null && this.mCurrentItem.hasItems()) {
            this.mCurrentItem.setSelected(false);
            this.mOpenItem = this.mCurrentItem;
            this.mOpening = true;
            this.mXFade = new LinearAnimation(ContentUtil.FONT_SIZE_NORMAL, 0.0f);
            this.mXFade.setDuration(200);
            this.mXFade.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    PieRenderer.this.mXFade = null;
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
            this.mXFade.startNow();
            this.mOverlay.startAnimation(this.mXFade);
        }
    }

    private PointF getPolar(float x, float y, boolean useOffset) {
        PointF res = new PointF();
        res.x = 1.5707964f;
        x -= (float) this.mCenter.x;
        y = ((float) this.mCenter.y) - y;
        res.y = (float) Math.sqrt((double) ((x * x) + (y * y)));
        if (x != 0.0f) {
            res.x = (float) Math.atan2((double) y, (double) x);
            if (res.x < 0.0f) {
                res.x = (float) (((double) res.x) + 6.283185307179586d);
            }
        }
        res.y = ((float) (useOffset ? this.mTouchOffset : 0)) + res.y;
        return res;
    }

    private PieItem findItem(PointF polar) {
        for (PieItem item : this.mOpenItem != null ? this.mOpenItem.getItems() : this.mItems) {
            if (inside(polar, item)) {
                return item;
            }
        }
        return null;
    }

    private boolean inside(PointF polar, PieItem item) {
        if (((float) item.getInnerRadius()) >= polar.y || item.getStartAngle() >= polar.x || item.getStartAngle() + item.getSweep() <= polar.x) {
            return false;
        }
        return !this.mTapMode || ((float) item.getOuterRadius()) > polar.y;
    }

    public boolean handlesTouch() {
        return true;
    }

    public void setFocus(int x, int y) {
        this.mFocusX = x;
        this.mFocusY = y;
        setCircle(this.mFocusX, this.mFocusY);
    }

    public void alignFocus(int x, int y) {
        this.mOverlay.removeCallbacks(this.mDisappear);
        this.mAnimation.cancel();
        this.mAnimation.reset();
        this.mFocusX = x;
        this.mFocusY = y;
        this.mDialAngle = 157;
        setCircle(x, y);
        this.mFocused = false;
    }

    public int getSize() {
        return this.mCircleSize * 2;
    }

    private int getRandomRange() {
        return (int) ((Math.random() * 120.0d) - 0.0703125d);
    }

    public void layout(int l, int t, int r, int b) {
        super.layout(l, t, r, b);
        this.mCenterX = (r - l) / 2;
        this.mCenterY = (b - t) / 2;
        this.mFocusX = this.mCenterX;
        this.mFocusY = this.mCenterY;
        setCircle(this.mFocusX, this.mFocusY);
        if (isVisible() && this.mState == 8) {
            setCenter(this.mCenterX, this.mCenterY);
            layoutPie();
        }
    }

    private void setCircle(int cx, int cy) {
        this.mCircle.set((float) (cx - this.mCircleSize), (float) (cy - this.mCircleSize), (float) (this.mCircleSize + cx), (float) (this.mCircleSize + cy));
        this.mDial.set((float) ((cx - this.mCircleSize) + this.mInnerOffset), (float) ((cy - this.mCircleSize) + this.mInnerOffset), (float) ((this.mCircleSize + cx) - this.mInnerOffset), (float) ((this.mCircleSize + cy) - this.mInnerOffset));
    }

    public void drawFocus(Canvas canvas) {
        if (!this.mBlockFocus) {
            this.mFocusPaint.setStrokeWidth((float) this.mOuterStroke);
            canvas.drawCircle((float) this.mFocusX, (float) this.mFocusY, (float) this.mCircleSize, this.mFocusPaint);
            if (this.mState != 8) {
                int color = this.mFocusPaint.getColor();
                if (this.mState == 2) {
                    this.mFocusPaint.setColor(this.mFocused ? this.mSuccessColor : this.mFailColor);
                }
                this.mFocusPaint.setStrokeWidth((float) this.mInnerStroke);
                drawLine(canvas, this.mDialAngle, this.mFocusPaint);
                drawLine(canvas, this.mDialAngle + 45, this.mFocusPaint);
                drawLine(canvas, this.mDialAngle + 180, this.mFocusPaint);
                drawLine(canvas, this.mDialAngle + 225, this.mFocusPaint);
                canvas.save();
                canvas.rotate((float) this.mDialAngle, (float) this.mFocusX, (float) this.mFocusY);
                canvas.drawArc(this.mDial, 0.0f, 45.0f, false, this.mFocusPaint);
                canvas.drawArc(this.mDial, 180.0f, 45.0f, false, this.mFocusPaint);
                canvas.restore();
                this.mFocusPaint.setColor(color);
            }
        }
    }

    private void drawLine(Canvas canvas, int angle, Paint p) {
        convertCart(angle, this.mCircleSize - this.mInnerOffset, this.mPoint1);
        convertCart(angle, (this.mCircleSize - this.mInnerOffset) + (this.mInnerOffset / 3), this.mPoint2);
        canvas.drawLine((float) (this.mPoint1.x + this.mFocusX), (float) (this.mPoint1.y + this.mFocusY), (float) (this.mPoint2.x + this.mFocusX), (float) (this.mPoint2.y + this.mFocusY), p);
    }

    private static void convertCart(int angle, int radius, Point out) {
        double a = (((double) (angle % 360)) * 6.283185307179586d) / 360.0d;
        out.x = (int) ((((double) radius) * Math.cos(a)) + 0.5d);
        out.y = (int) ((((double) radius) * Math.sin(a)) + 0.5d);
    }

    public void showStart() {
        if (this.mState != 8) {
            cancelFocus();
            this.mStartAnimationAngle = 67;
            startAnimation(600, false, (float) this.mStartAnimationAngle, (float) (this.mStartAnimationAngle + getRandomRange()));
            this.mState = 1;
        }
    }

    public void showSuccess(boolean timeout) {
        if (this.mState == 1) {
            startAnimation(100, timeout, (float) this.mStartAnimationAngle);
            this.mState = 2;
            this.mFocused = true;
        }
    }

    public void showFail(boolean timeout) {
        if (this.mState == 1) {
            startAnimation(100, timeout, (float) this.mStartAnimationAngle);
            this.mState = 2;
            this.mFocused = false;
        }
    }

    private void cancelFocus() {
        this.mFocusCancelled = true;
        this.mOverlay.removeCallbacks(this.mDisappear);
        if (this.mAnimation != null) {
            this.mAnimation.cancel();
        }
        this.mFocusCancelled = false;
        this.mFocused = false;
        this.mState = 0;
    }

    public void clear() {
        if (this.mState != 8) {
            cancelFocus();
            this.mOverlay.post(this.mDisappear);
        }
    }

    private void startAnimation(long duration, boolean timeout, float toScale) {
        startAnimation(duration, timeout, (float) this.mDialAngle, toScale);
    }

    private void startAnimation(long duration, boolean timeout, float fromScale, float toScale) {
        setVisible(true);
        this.mAnimation.reset();
        this.mAnimation.setDuration(duration);
        this.mAnimation.setScale(fromScale, toScale);
        this.mAnimation.setAnimationListener(timeout ? this.mEndAction : null);
        this.mOverlay.startAnimation(this.mAnimation);
        update();
    }
}
