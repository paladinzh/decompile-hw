package com.huawei.gallery.kidsmode;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.ui.DownUpDetector;
import com.android.gallery3d.ui.DownUpDetector.DownUpListener;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.ResourceTexture;
import com.android.gallery3d.ui.SlotScrollBarView;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;

public class KidsAlbumSlotScrollBarView extends SlotScrollBarView {
    private static final int ROLLINGBAR_HEIGHT = GalleryUtils.getHeightPixels();
    private static final int ROLLINGBAR_WIDTH = GalleryUtils.dpToPixel(8);
    private int mBarPressedPosition = -1;
    private boolean mBarPressing = false;
    private int mBarTop;
    private GalleryContext mContext;
    private final DownUpDetector mDownUpDetector;
    private final GestureDetector mGestureDetector;
    private int mPinBarHeight;
    private int mRollingBarLeft;
    private int mRollingBarPinLeft;
    private ResourceTexture mRollingBarPinTexture;
    private ResourceTexture mRollingBarTexture;
    private int mScrollAreaLeft;
    private int mScrollMargin;

    private class MyDownUpListener implements DownUpListener {
        private MyDownUpListener() {
        }

        public void onDown(MotionEvent e) {
            KidsAlbumSlotScrollBarView.this.mBarPressing = KidsAlbumSlotScrollBarView.this.inBarArea(e.getX(), e.getY());
            if (KidsAlbumSlotScrollBarView.this.mBarPressing) {
                KidsAlbumSlotScrollBarView.this.mBarPressedPosition = KidsAlbumSlotScrollBarView.this.mBarTop;
                if (KidsAlbumSlotScrollBarView.this.mScrollListener != null) {
                    KidsAlbumSlotScrollBarView.this.mScrollListener.onDown();
                }
                GalleryLog.v("KidsAlbumSlotScrollBarView", "kids scrollbar press onDown ");
                KidsAlbumSlotScrollBarView.this.show();
                KidsAlbumSlotScrollBarView.this.invalidate();
                return;
            }
            KidsAlbumSlotScrollBarView.this.mBarPressedPosition = -1;
            GalleryLog.v("KidsAlbumSlotScrollBarView", "touch down the invalid position");
        }

        public void onUp(MotionEvent e) {
            GalleryLog.v("KidsAlbumSlotScrollBarView", "kids scrollbar press onUp ");
            KidsAlbumSlotScrollBarView.this.mBarPressing = false;
            KidsAlbumSlotScrollBarView.this.mBarPressedPosition = -1;
            KidsAlbumSlotScrollBarView.this.hide();
            KidsAlbumSlotScrollBarView.this.invalidate();
        }
    }

    private class MyGestureListener implements OnGestureListener {
        private MyGestureListener() {
        }

        public boolean onDown(MotionEvent e) {
            GalleryLog.d("KidsAlbumSlotScrollBarView", "Gesture onDown");
            return false;
        }

        public void onShowPress(MotionEvent e) {
            GalleryLog.d("KidsAlbumSlotScrollBarView", "Gesture onShowPress");
        }

        public boolean onSingleTapUp(MotionEvent e) {
            GalleryLog.d("KidsAlbumSlotScrollBarView", "Gesture onSingleTapUp");
            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (KidsAlbumSlotScrollBarView.this.mScrollListener == null || -1 == KidsAlbumSlotScrollBarView.this.mBarPressedPosition) {
                return false;
            }
            KidsAlbumSlotScrollBarView.this.mBarTop = KidsAlbumSlotScrollBarView.this.mBarPressedPosition + ((int) (e2.getY() - e1.getY()));
            KidsAlbumSlotScrollBarView.this.mBarTop = Utils.clamp(KidsAlbumSlotScrollBarView.this.mBarTop, 0, KidsAlbumSlotScrollBarView.this.mViewHeight - KidsAlbumSlotScrollBarView.this.mPinBarHeight);
            KidsAlbumSlotScrollBarView.this.mScrollListener.updateScrollPosition(KidsAlbumSlotScrollBarView.this.mBarTop, KidsAlbumSlotScrollBarView.this.mViewHeight - KidsAlbumSlotScrollBarView.this.mPinBarHeight);
            return true;
        }

        public void onLongPress(MotionEvent e) {
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }
    }

    public KidsAlbumSlotScrollBarView(GalleryContext activity, int scrollDrawableId, int fastscrollDrawableId) {
        super(activity);
        this.mContext = activity;
        Context context = activity.getAndroidContext();
        this.mRollingBarTexture = new ResourceTexture(context, fastscrollDrawableId);
        this.mRollingBarPinTexture = new ResourceTexture(context, R.drawable.scrollbar_point);
        this.mGestureDetector = new GestureDetector(activity.getAndroidContext(), new MyGestureListener());
        this.mGestureDetector.setIsLongpressEnabled(false);
        this.mDownUpDetector = new DownUpDetector(new MyDownUpListener());
    }

    public void layout(int left, int top, int right, int bottom) {
        super.layout(left, top, right, bottom);
        this.mScrollMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.kids_album_scroll_right_margin);
        if (this.mLayoutRTL) {
            this.mScrollAreaLeft = 0;
            this.mRollingBarLeft = this.mScrollMargin;
        } else {
            this.mScrollAreaLeft = this.mViewWidth - (this.mScrollMargin * 2);
            this.mRollingBarLeft = (this.mViewWidth - this.mScrollMargin) - (ROLLINGBAR_WIDTH / 2);
        }
        this.mRollingBarPinLeft = this.mRollingBarLeft + ((ROLLINGBAR_WIDTH - this.mRollingBarPinTexture.getWidth()) / 2);
    }

    protected void render(GLCanvas canvas) {
        if (this.mContentLen > 0) {
            super.render(canvas);
            this.mPinBarHeight = this.mRollingBarPinTexture.getHeight();
            int pinY = (int) ((((float) (this.mViewHeight - this.mPinBarHeight)) * ((float) this.mContentOffset)) / ((float) this.mContentLen));
            this.mBarTop = pinY;
            this.mRollingBarTexture.draw(canvas, this.mRollingBarLeft, 0, ROLLINGBAR_WIDTH, this.mViewHeight);
            this.mRollingBarPinTexture.draw(canvas, this.mRollingBarPinLeft, pinY);
            if (this.mScrollListener != null) {
            }
        }
    }

    private boolean inScrollArea(int x, int y) {
        if (x <= this.mScrollAreaLeft || x >= this.mScrollAreaLeft + (this.mScrollMargin * 2) || y <= 0 || y >= this.mViewHeight) {
            return false;
        }
        return true;
    }

    private boolean inBarArea(float x, float y) {
        if (x <= ((float) this.mScrollAreaLeft) || x >= ((float) (this.mScrollAreaLeft + (this.mScrollMargin * 2))) || y <= ((float) this.mBarTop) || y >= ((float) (this.mBarTop + this.mPinBarHeight))) {
            return false;
        }
        return true;
    }

    public void enterFastScrollMode(float velocity) {
        super.enterFastScrollMode(velocity);
    }

    protected boolean onTouch(MotionEvent event) {
        if (this.mContentLen <= 0) {
            GalleryLog.v("KidsAlbumSlotScrollBarView", "onTouch mContentLen is 0");
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (event.getAction() != 0 || inScrollArea(x, y)) {
            this.mGestureDetector.onTouchEvent(event);
            this.mDownUpDetector.onTouchEvent(event);
            return true;
        }
        GalleryLog.v("KidsAlbumSlotScrollBarView", "onTouch return, touch point is not in the scroll area");
        return false;
    }

    public void show() {
        GalleryLog.d("KidsAlbumSlotScrollBarView", "no need show");
    }

    public void hide() {
        GalleryLog.d("KidsAlbumSlotScrollBarView", "no need hide");
    }
}
