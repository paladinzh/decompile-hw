package com.huawei.gallery.refocus.wideaperture.RangeMeasure.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import com.android.gallery3d.R;
import com.autonavi.amap.mapcore.VirtualEarthProjection;

public class RangeMeasureView extends RelativeLayout {
    private String distanceText = null;
    private Point mBeginPosInImage = new Point();
    private Point mBeginPosInScreen = new Point();
    private Point mEndPosInImage = new Point();
    private Point mEndPosInScreen = new Point();
    private Point mIconCenterOffset = new Point();
    private Point mLineOffset = new Point();
    private Paint mLinePaint = new Paint(1);
    Listener mListener = null;
    private Drawable mPositionBeginIcon;
    private Drawable mPositionEndIcon;
    private int mPositionIconHeight;
    private int mPositionIconWidth;
    private NinePatchDrawable mTextBackground;
    private Rect mTextBkgPadding = new Rect();
    private TextPaint mTextPaint = new TextPaint();
    private Point mTouchDownPos = new Point();
    private int mTouchState = -1;

    public interface Listener {
        Point getDisplayPositionInScreen(Point point);

        Point getRealPositionInImage(Point point);

        int rangeMeasurePointLocated(Point point, Point point2);

        void rangeMeasurePointUnLocated();
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public RangeMeasureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mPositionIconWidth = context.getResources().getDrawable(R.drawable.ic_position).getIntrinsicWidth();
        this.mPositionIconHeight = context.getResources().getDrawable(R.drawable.ic_position).getIntrinsicHeight();
        this.mPositionBeginIcon = getResources().getDrawable(R.drawable.ic_position);
        this.mPositionEndIcon = getResources().getDrawable(R.drawable.ic_position);
        this.mPositionBeginIcon.setVisible(false, false);
        this.mPositionEndIcon.setVisible(false, false);
        this.mLinePaint.setColor(getResources().getColor(R.color.distance_line_color));
        this.mLinePaint.setStrokeWidth((float) (getResources().getDimensionPixelSize(R.dimen.distance_line_width) - 2));
        this.mTextPaint.setTextSize((float) getResources().getDimensionPixelSize(R.dimen.distance_text_font_size));
        this.mTextPaint.setColor(context.getResources().getColor(R.color.distance_text_color));
        this.mTextBackground = (NinePatchDrawable) getResources().getDrawable(R.drawable.tips_beauty_bar);
        this.mTextBackground.getPadding(this.mTextBkgPadding);
    }

    public boolean onTouch(MotionEvent event) {
        switch (event.getActionMasked() & 255) {
            case 0:
                return onTouchEventDown(new Point((int) event.getX(), (int) event.getY()));
            case 1:
                return onTouchEventUp(new Point((int) event.getX(), (int) event.getY()));
            case 2:
                return onTouchEventMove(new Point((int) event.getX(), (int) event.getY()));
            default:
                return false;
        }
    }

    private boolean onTouchEventDown(Point point) {
        if (isTouchOnIconRect(this.mPositionBeginIcon, point.x, point.y)) {
            this.mTouchState = 0;
            this.mPositionBeginIcon.setState(PRESSED_STATE_SET);
            invalidate();
            this.mIconCenterOffset = getIconCenterOffset(this.mBeginPosInScreen, point);
            return true;
        } else if (isTouchOnIconRect(this.mPositionEndIcon, point.x, point.y)) {
            this.mTouchState = 1;
            this.mPositionEndIcon.setState(PRESSED_STATE_SET);
            invalidate();
            this.mIconCenterOffset = getIconCenterOffset(this.mEndPosInScreen, point);
            return true;
        } else {
            this.mTouchState = 4;
            this.mTouchDownPos.set(point.x, point.y);
            return false;
        }
    }

    private boolean onTouchEventMove(Point point) {
        switch (this.mTouchState) {
            case 0:
                if (!isMoveOverThreshold(this.mTouchDownPos, point)) {
                    return false;
                }
                this.mTouchState = 2;
                return true;
            case 1:
                if (!isMoveOverThreshold(this.mTouchDownPos, point)) {
                    return false;
                }
                this.mTouchState = 3;
                return true;
            case 2:
                onPositionIconMoving(this.mPositionBeginIcon, point);
                return true;
            case 3:
                onPositionIconMoving(this.mPositionEndIcon, point);
                return true;
            case 4:
                if (!isMoveOverThreshold(this.mTouchDownPos, point)) {
                    return true;
                }
                this.mTouchState = -1;
                return false;
            default:
                return false;
        }
    }

    private boolean onTouchEventUp(Point point) {
        boolean ret = false;
        switch (this.mTouchState) {
            case 0:
                this.mPositionBeginIcon.setState(ENABLED_STATE_SET);
                invalidate();
                ret = true;
                break;
            case 1:
                this.mPositionEndIcon.setState(ENABLED_STATE_SET);
                invalidate();
                ret = true;
                break;
            case 2:
                this.mPositionBeginIcon.setState(ENABLED_STATE_SET);
                rangeMeasurePointLocated();
                invalidate();
                ret = true;
                break;
            case 3:
                this.mPositionEndIcon.setState(ENABLED_STATE_SET);
                rangeMeasurePointLocated();
                invalidate();
                ret = true;
                break;
            case 4:
                addPositionIcon(point.x, point.y);
                ret = true;
                break;
        }
        this.mTouchState = -1;
        return ret;
    }

    private void onPositionIconMoving(Drawable icon, Point point) {
        Point posInScreen = new Point(point.x + this.mIconCenterOffset.x, point.y + this.mIconCenterOffset.y);
        if (this.mListener != null) {
            Point posInImage = this.mListener.getRealPositionInImage(posInScreen);
            if (posInImage.x > 0 && posInImage.y > 0) {
                if (this.mPositionBeginIcon.equals(icon)) {
                    this.mBeginPosInScreen.set(posInScreen.x, posInScreen.y);
                    this.mBeginPosInImage.set(posInImage.x, posInImage.y);
                } else if (this.mPositionEndIcon.equals(icon)) {
                    this.mEndPosInScreen.set(posInScreen.x, posInScreen.y);
                    this.mEndPosInImage.set(posInImage.x, posInImage.y);
                }
                if (this.mPositionBeginIcon.isVisible() && this.mPositionEndIcon.isVisible()) {
                    updateIconRotationAndDistanceLineOffset();
                }
                invalidate();
            }
        }
    }

    private boolean isMoveOverThreshold(Point beginPos, Point endPos) {
        if (Math.abs(beginPos.x - endPos.x) >= 15 || Math.abs(beginPos.y - endPos.y) >= 15) {
            return true;
        }
        return false;
    }

    private boolean isTouchOnIconRect(Drawable icon, int x, int y) {
        if (icon.isVisible() && icon.getBounds().contains(x, y)) {
            return true;
        }
        return false;
    }

    private Point getIconCenterOffset(Point iconPos, Point touchPos) {
        return new Point(iconPos.x - touchPos.x, iconPos.y - touchPos.y);
    }

    private int calculatePositionIconRotationAngle() {
        int x = Math.abs(this.mBeginPosInScreen.x - this.mEndPosInScreen.x);
        int y = Math.abs(this.mBeginPosInScreen.y - this.mEndPosInScreen.y);
        int degree = Math.round((float) ((Math.asin(((double) x) / Math.sqrt((double) ((x * x) + (y * y)))) / 3.141592653589793d) * VirtualEarthProjection.MaxLongitude));
        if ((this.mEndPosInScreen.x <= this.mBeginPosInScreen.x || this.mEndPosInScreen.y >= this.mBeginPosInScreen.y) && (this.mEndPosInScreen.x >= this.mBeginPosInScreen.x || this.mEndPosInScreen.y <= this.mBeginPosInScreen.y)) {
            return 360 - degree;
        }
        return degree;
    }

    private void setPositionIconRatation(int degree) {
        this.mPositionBeginIcon.setLevel((degree * 10000) / 360);
        this.mPositionEndIcon.setLevel((degree * 10000) / 360);
    }

    private void setDistanceLineBeginAndEndOffset(int degree) {
        if (degree > 180) {
            degree = 360 - degree;
        }
        this.mLineOffset.x = (int) ((Math.sin((((double) degree) * 3.141592653589793d) / VirtualEarthProjection.MaxLongitude) * ((double) this.mPositionIconWidth)) / 2.0d);
        this.mLineOffset.y = (int) ((Math.cos((((double) degree) * 3.141592653589793d) / VirtualEarthProjection.MaxLongitude) * ((double) this.mPositionIconWidth)) / 2.0d);
    }

    private void updateIconRotationAndDistanceLineOffset() {
        int degree = calculatePositionIconRotationAngle();
        setPositionIconRatation(degree);
        setDistanceLineBeginAndEndOffset(degree);
    }

    private void rangeMeasurePointLocated() {
        if (this.mPositionBeginIcon.isVisible() && this.mPositionEndIcon.isVisible()) {
            updateIconRotationAndDistanceLineOffset();
            int retVal = -1;
            if (this.mListener != null) {
                retVal = this.mListener.rangeMeasurePointLocated(this.mBeginPosInImage, this.mEndPosInImage);
                if (retVal < 0) {
                    this.distanceText = null;
                    return;
                }
            }
            this.distanceText = String.valueOf(((float) retVal) / 10.0f) + "cm";
        }
    }

    private void rangeMeasurePointUnLocated() {
        setPositionIconRatation(0);
        if (this.mListener != null) {
            this.mListener.rangeMeasurePointUnLocated();
        }
    }

    private void addPositionIcon(int x, int y) {
        if (!this.mPositionBeginIcon.isVisible() || !this.mPositionEndIcon.isVisible()) {
            if (!this.mPositionBeginIcon.isVisible()) {
                this.mBeginPosInScreen.set(x, y);
                if (this.mListener != null) {
                    this.mBeginPosInImage = this.mListener.getRealPositionInImage(this.mBeginPosInScreen);
                }
                if (this.mBeginPosInImage.x > 0 && this.mBeginPosInImage.y > 0) {
                    this.mPositionBeginIcon.setVisible(true, true);
                }
            } else if (!this.mPositionEndIcon.isVisible()) {
                this.mEndPosInScreen.set(x, y);
                if (this.mListener != null) {
                    this.mEndPosInImage = this.mListener.getRealPositionInImage(this.mEndPosInScreen);
                }
                if (this.mEndPosInImage.x > 0 && this.mEndPosInImage.y > 0) {
                    this.mPositionEndIcon.setVisible(true, true);
                }
            }
            rangeMeasurePointLocated();
            invalidate();
        }
    }

    private void setPositionIconBounds(Drawable icon, int x, int y) {
        icon.setBounds(new Rect(x - (this.mPositionIconWidth / 2), y - (this.mPositionIconHeight / 2), (this.mPositionIconWidth / 2) + x, (this.mPositionIconHeight / 2) + y));
    }

    public void removeRangeMeasureView() {
        this.mPositionBeginIcon.setVisible(false, true);
        this.mPositionEndIcon.setVisible(false, true);
        rangeMeasurePointUnLocated();
        invalidate();
    }

    private void drawDistanceLine(Canvas canvas) {
        Point lineBeginPoint = new Point();
        Point lineEndPoint = new Point();
        if (this.mBeginPosInScreen.x > this.mEndPosInScreen.x) {
            lineBeginPoint.x = this.mBeginPosInScreen.x - this.mLineOffset.x;
            lineEndPoint.x = this.mEndPosInScreen.x + this.mLineOffset.x;
        } else {
            lineBeginPoint.x = this.mBeginPosInScreen.x + this.mLineOffset.x;
            lineEndPoint.x = this.mEndPosInScreen.x - this.mLineOffset.x;
        }
        if (this.mBeginPosInScreen.y > this.mEndPosInScreen.y) {
            lineBeginPoint.y = this.mBeginPosInScreen.y - this.mLineOffset.y;
            lineEndPoint.y = this.mEndPosInScreen.y + this.mLineOffset.y;
        } else {
            lineBeginPoint.y = this.mBeginPosInScreen.y + this.mLineOffset.y;
            lineEndPoint.y = this.mEndPosInScreen.y - this.mLineOffset.y;
        }
        canvas.drawLine((float) lineBeginPoint.x, (float) lineBeginPoint.y, (float) lineEndPoint.x, (float) lineEndPoint.y, this.mLinePaint);
    }

    private void drawDistanceText(Canvas canvas) {
        int lineCenterX = (this.mBeginPosInScreen.x > this.mEndPosInScreen.x ? this.mEndPosInScreen.x : this.mBeginPosInScreen.x) + (Math.abs(this.mBeginPosInScreen.x - this.mEndPosInScreen.x) / 2);
        int lineCenterY = (this.mBeginPosInScreen.y > this.mEndPosInScreen.y ? this.mEndPosInScreen.y : this.mBeginPosInScreen.y) + (Math.abs(this.mBeginPosInScreen.y - this.mEndPosInScreen.y) / 2);
        Rect bounds = new Rect();
        this.mTextPaint.getTextBounds(this.distanceText, 0, this.distanceText.length(), bounds);
        int textWidth = (bounds.width() + this.mTextBkgPadding.left) + this.mTextBkgPadding.right;
        int textHeight = (bounds.height() + this.mTextBkgPadding.top) + this.mTextBkgPadding.bottom;
        int textBackgroundWidth = this.mTextBackground.getIntrinsicWidth();
        int textBackgroundHeight = this.mTextBackground.getIntrinsicHeight();
        int width = textBackgroundWidth > textWidth ? textBackgroundWidth : textWidth;
        int height = textBackgroundHeight > textHeight ? textBackgroundHeight : textHeight;
        Rect bkgRect = new Rect(lineCenterX - (width / 2), lineCenterY - height, (width / 2) + lineCenterX, lineCenterY);
        this.mTextBackground.setBounds(bkgRect.left, bkgRect.top, bkgRect.right, bkgRect.bottom);
        this.mTextBackground.draw(canvas);
        canvas.drawText(this.distanceText, (float) ((bkgRect.left + this.mTextBkgPadding.left) + ((width - textWidth) / 2)), (float) ((bkgRect.bottom - this.mTextBkgPadding.bottom) - ((height - textHeight) / 2)), this.mTextPaint);
    }

    private void drawDistanceInfo(Canvas canvas) {
        drawDistanceLine(canvas);
        if (this.distanceText != null && this.mTouchState != 2 && this.mTouchState != 3) {
            drawDistanceText(canvas);
        }
    }

    public void refreshRangeMeasureView() {
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        if (this.mListener != null) {
            if (this.mPositionBeginIcon != null && this.mPositionBeginIcon.isVisible()) {
                Point beginPos = this.mListener.getDisplayPositionInScreen(this.mBeginPosInImage);
                this.mBeginPosInScreen.set(beginPos.x, beginPos.y);
                setPositionIconBounds(this.mPositionBeginIcon, beginPos.x, beginPos.y);
                this.mPositionBeginIcon.draw(canvas);
            }
            if (this.mPositionEndIcon != null && this.mPositionEndIcon.isVisible()) {
                Point endPos = this.mListener.getDisplayPositionInScreen(this.mEndPosInImage);
                this.mEndPosInScreen.set(endPos.x, endPos.y);
                setPositionIconBounds(this.mPositionEndIcon, endPos.x, endPos.y);
                this.mPositionEndIcon.draw(canvas);
            }
            if (this.mPositionBeginIcon != null && this.mPositionBeginIcon.isVisible() && this.mPositionEndIcon != null && this.mPositionEndIcon.isVisible()) {
                drawDistanceInfo(canvas);
            }
        }
    }
}
