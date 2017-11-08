package com.android.gallery3d.ui;

import android.text.TextPaint;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.ui.DownUpDetector.DownUpListener;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;

public class AlbumSlotScrollBarView extends SlotScrollBarView {
    private static final int BAR_MIN_HEIGHT = GalleryUtils.dpToPixel(40);
    private static final int CONTAINER_WIDTH = GalleryUtils.dpToPixel(20);
    private static final int LABEL_MONTH_TEXT_SIZE = GalleryUtils.dpToPixel(32);
    private static final int LABEL_YEAR_TEXT_SIZE = GalleryUtils.dpToPixel(12);
    private int mBarHeight;
    private int mBarPressedPosition = -1;
    private boolean mBarPressing = false;
    private int mBarTop;
    private final DownUpDetector mDownUpDetector;
    private final GestureDetector mGestureDetector;
    private boolean mHasFastScrolled = false;
    private boolean mInFastMode = false;
    private TextPaint mMonthTextPaint = new TextPaint();
    private ResourceTexture mRollingBarTexture;
    private int mScrollAreaLeft;
    private int mScrollBarLeft;
    private TextPaint mYearTextPaint = new TextPaint();

    private class MyDownUpListener implements DownUpListener {
        private MyDownUpListener() {
        }

        public void onDown(MotionEvent e) {
            AlbumSlotScrollBarView.this.mBarPressing = AlbumSlotScrollBarView.this.inBarArea(e.getX(), e.getY());
            if (AlbumSlotScrollBarView.this.mBarPressing) {
                AlbumSlotScrollBarView.this.mBarPressedPosition = AlbumSlotScrollBarView.this.mBarTop;
                if (AlbumSlotScrollBarView.this.mScrollListener != null) {
                    AlbumSlotScrollBarView.this.mScrollListener.onDown();
                }
                AlbumSlotScrollBarView.this.show();
                AlbumSlotScrollBarView.this.invalidate();
                return;
            }
            AlbumSlotScrollBarView.this.mBarPressedPosition = -1;
        }

        public void onUp(MotionEvent e) {
            AlbumSlotScrollBarView.this.mBarPressing = false;
            AlbumSlotScrollBarView.this.mBarPressedPosition = -1;
            AlbumSlotScrollBarView.this.hide();
            AlbumSlotScrollBarView.this.invalidate();
            if (AlbumSlotScrollBarView.this.mHasFastScrolled) {
                AlbumSlotScrollBarView.this.mHasFastScrolled = false;
                ReportToBigData.report(15);
            }
        }
    }

    private class MyGestureListener implements OnGestureListener {
        private MyGestureListener() {
        }

        public boolean onDown(MotionEvent e) {
            return false;
        }

        public void onShowPress(MotionEvent e) {
        }

        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (AlbumSlotScrollBarView.this.mScrollListener == null || -1 == AlbumSlotScrollBarView.this.mBarPressedPosition) {
                return false;
            }
            AlbumSlotScrollBarView.this.mBarTop = AlbumSlotScrollBarView.this.mBarPressedPosition + ((int) (e2.getY() - e1.getY()));
            AlbumSlotScrollBarView.this.mBarTop = Utils.clamp(AlbumSlotScrollBarView.this.mBarTop, 0, AlbumSlotScrollBarView.this.mViewHeight - AlbumSlotScrollBarView.this.mBarHeight);
            AlbumSlotScrollBarView.this.mScrollListener.updateScrollPosition(AlbumSlotScrollBarView.this.mBarTop, AlbumSlotScrollBarView.this.mViewHeight - AlbumSlotScrollBarView.this.mBarHeight);
            AlbumSlotScrollBarView.this.mHasFastScrolled = true;
            return true;
        }

        public void onLongPress(MotionEvent e) {
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }
    }

    public AlbumSlotScrollBarView(GalleryContext activity, int scrollDrawableId, int fastscrollDrawableId) {
        super(activity);
        this.mRollingBarTexture = new ResourceTexture(activity.getAndroidContext(), fastscrollDrawableId);
        this.mMonthTextPaint.setTextSize((float) LABEL_MONTH_TEXT_SIZE);
        this.mMonthTextPaint.setAntiAlias(true);
        this.mMonthTextPaint.setColor(-1);
        GalleryUtils.setTypeFaceAsSlim(this.mMonthTextPaint);
        this.mYearTextPaint.setTextSize((float) LABEL_YEAR_TEXT_SIZE);
        this.mYearTextPaint.setAntiAlias(true);
        this.mYearTextPaint.setColor(-1);
        this.mYearTextPaint.setAlpha(153);
        GalleryUtils.setTypeFaceAsSlim(this.mYearTextPaint);
        this.mGestureDetector = new GestureDetector(activity.getAndroidContext(), new MyGestureListener());
        this.mGestureDetector.setIsLongpressEnabled(false);
        this.mDownUpDetector = new DownUpDetector(new MyDownUpListener());
    }

    public void layout(int left, int top, int right, int bottom) {
        super.layout(left, SCROLLBAR_VIEW_MARGIN + top, right, bottom - SCROLLBAR_VIEW_MARGIN);
        if (this.mLayoutRTL) {
            this.mScrollAreaLeft = 0;
            this.mScrollBarLeft = 0;
            return;
        }
        this.mScrollAreaLeft = this.mViewWidth - CONTAINER_WIDTH;
        this.mScrollBarLeft = this.mViewWidth - this.mRollingBarTexture.getWidth();
    }

    protected void render(GLCanvas canvas) {
        if (this.mContentLen > 0) {
            super.render(canvas);
            if (!(this.mHideAnimation == null || this.mHideAnimation.calculate(AnimationTime.get()))) {
                this.mInFastMode = false;
                this.mHideAnimation = null;
            }
            this.mBarHeight = BAR_MIN_HEIGHT;
            int y = (int) ((((float) (this.mViewHeight - this.mBarHeight)) * ((float) this.mContentOffset)) / ((float) this.mContentLen));
            this.mBarTop = y;
            this.mRollingBarTexture.draw(canvas, this.mScrollBarLeft, y);
        }
    }

    private boolean inScrollArea(int x, int y) {
        if (x <= this.mScrollAreaLeft || x >= this.mScrollAreaLeft + CONTAINER_WIDTH || y <= 0 || y >= this.mViewHeight) {
            return false;
        }
        return true;
    }

    private boolean inBarArea(float x, float y) {
        if (!this.mInFastMode || x <= ((float) this.mScrollAreaLeft) || x >= ((float) (this.mScrollAreaLeft + CONTAINER_WIDTH)) || y <= ((float) this.mBarTop) || y >= ((float) (this.mBarTop + this.mBarHeight))) {
            return false;
        }
        return true;
    }

    public void enterFastScrollMode(float velocity) {
        super.enterFastScrollMode(velocity);
        this.mInFastMode = true;
    }

    protected boolean onTouch(MotionEvent event) {
        if (this.mContentLen <= 0) {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (event.getAction() == 0 && !inScrollArea(x, y)) {
            return false;
        }
        this.mGestureDetector.onTouchEvent(event);
        this.mDownUpDetector.onTouchEvent(event);
        return true;
    }

    public void show() {
        if (getVisibility() != 0) {
            this.mInFastMode = true;
        }
        super.show();
    }

    public void hide() {
        if (!this.mBarPressing) {
            super.hide();
        }
    }
}
