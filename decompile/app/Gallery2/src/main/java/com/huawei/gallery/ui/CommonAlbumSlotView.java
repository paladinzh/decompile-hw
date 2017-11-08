package com.huawei.gallery.ui;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.GLCanvas;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.Paper;
import com.android.gallery3d.ui.ScrollerHelper;
import com.android.gallery3d.ui.SlotScrollBarView;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.ui.CommonAlbumSlidingWindow.AlbumEntry;
import com.huawei.gallery.ui.SlotView.AbsLayout;
import com.huawei.gallery.ui.SlotView.DeleteSlotAnimation;
import com.huawei.gallery.ui.SlotView.DownShiftAnimation;
import com.huawei.gallery.ui.SlotView.SlotRenderInterface;
import com.huawei.gallery.ui.SlotView.SlotUIListener;
import com.huawei.watermark.manager.parse.WMElement;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.HashMap;

public class CommonAlbumSlotView extends SlotView {
    public static final int DOWN_TIME_OUT = ((ViewConfiguration.getTapTimeout() + ViewConfiguration.getLongPressTimeout()) + 100);
    private boolean mCommonLayout = true;
    protected boolean mDoSelect = false;
    protected boolean mDownInScrolling;
    protected boolean mFirstFling = true;
    protected boolean mFirstScroll = true;
    protected final GestureDetector mGestureDetector;
    protected int mIndexDown = -1;
    protected int mIndexUp = -1;
    protected final Layout mLayout;
    private Listener mListener;
    protected int mOverscrollEffect = 2;
    protected final Paper mPaper = new Paper(false);
    private boolean mPreviewMode;
    protected SlotRenderer mRenderer;
    protected final ScrollerHelper mScroller;
    protected SlotScrollBarView mSlotScrollBar;
    private int mStartIndex = -1;
    protected final Rect mTempRect = new Rect();

    public interface Listener extends SlotUIListener {
        boolean inSelectionMode();

        void onDown(int i);

        void onLongTap(int i);

        void onScroll(int i);

        void onScrollPositionChanged(int i, int i2);

        void onSingleTapUp(int i, boolean z);

        void onTouchDown(MotionEvent motionEvent);

        void onTouchMove(MotionEvent motionEvent);

        void onTouchUp(MotionEvent motionEvent);

        void onUp(boolean z);
    }

    public static class SimpleListener implements Listener {
        public void onDown(int index) {
        }

        public void onUp(boolean followedByLongPress) {
        }

        public void onSingleTapUp(int index, boolean cornerPressed) {
        }

        public void onLongTap(int index) {
        }

        public void onScrollPositionChanged(int position, int total) {
        }

        public void onScroll(int index) {
        }

        public void onTouchUp(MotionEvent event) {
        }

        public void onTouchMove(MotionEvent event) {
        }

        public void onTouchDown(MotionEvent event) {
        }

        public boolean inSelectionMode() {
            return false;
        }

        public boolean onDeleteSlotAnimationStart() {
            return false;
        }

        public boolean onDeleteSlotAnimationEnd() {
            return false;
        }
    }

    public interface SlotRenderer extends CornerPressedListener, SlotRenderInterface {
        void onSlotSizeChanged(int i, int i2);

        void onVisibleRangeChanged(int i, int i2);

        void prepareDrawing();

        void renderSlot(GLCanvas gLCanvas, int i, int i2, int i3, int i4);

        void renderSlot(GLCanvas gLCanvas, AlbumEntry albumEntry, int i, int i2, int i3);

        void renderTopTitle(GLCanvas gLCanvas, int i, int i2, int i3, int i4);
    }

    public class Layout extends AbsLayout {
        protected IntegerAnimation mHorizontalPadding = new IntegerAnimation();
        protected int mSlotCount;
        protected Spec mSpec;
        protected IntegerAnimation mVerticalPadding = new IntegerAnimation();
        protected int mVisibleEnd;
        protected int mVisibleStart;

        public Layout(Spec spec) {
            super();
            this.mSpec = spec;
        }

        protected Layout clone() {
            Layout layout = CommonAlbumSlotView.this.createLayout(this.mSpec);
            copyAllParameters(layout);
            layout.mVisibleStart = this.mVisibleStart;
            layout.mVisibleEnd = this.mVisibleEnd;
            layout.mSlotCount = this.mSlotCount;
            layout.mVerticalPadding = this.mVerticalPadding;
            layout.mHorizontalPadding = this.mHorizontalPadding;
            return layout;
        }

        public boolean setSlotCount(int slotCount) {
            boolean z = true;
            if (slotCount == this.mSlotCount) {
                return false;
            }
            if (this.mSlotCount != 0) {
                this.mHorizontalPadding.setEnabled(true);
                this.mVerticalPadding.setEnabled(true);
            }
            this.mSlotCount = slotCount;
            int hPadding = this.mHorizontalPadding.getTarget();
            int vPadding = this.mVerticalPadding.getTarget();
            initLayoutParameters(null);
            if (vPadding == this.mVerticalPadding.getTarget() && hPadding == this.mHorizontalPadding.getTarget()) {
                z = false;
            }
            return z;
        }

        public Rect getSlotRect(int index, Rect rect) {
            int row = index / this.mUnitCount;
            int col = index - (this.mUnitCount * row);
            int x = this.mHorizontalPadding.get() + ((this.mSlotWidth + this.mSlotWidthGap) * col);
            int y = (this.mVerticalPadding.get() + ((this.mSlotHeight + this.mSlotHeightGap) * row)) + CommonAlbumSlotView.this.mHeadCoverHeight;
            int slotWidth = this.mSlotWidth + getSizeDelta(col);
            if (CommonAlbumSlotView.this.mIsLayoutRtl) {
                x = CommonAlbumSlotView.this.getWidth() - x;
                rect.set(x - slotWidth, y, x, this.mSlotHeight + y);
                rect.offset(-getOffset(col), 0);
            } else {
                rect.set(x, y, x + slotWidth, this.mSlotHeight + y);
                rect.offset(getOffset(col), 0);
            }
            return rect;
        }

        public Rect getTargetSlotRect(Object index, Rect rect) {
            return getSlotRect(((Integer) index).intValue(), rect);
        }

        public boolean isPreVisibleStartIndex(Object index) {
            return ((Integer) index).intValue() < this.mVisibleStart;
        }

        protected void onInitLayoutfinish(Layout oldLayout) {
            apportionMargin();
            if (CommonAlbumSlotView.this.mRenderer != null) {
                CommonAlbumSlotView.this.mRenderer.onSlotSizeChanged(this.mSlotWidth, this.mSlotHeight);
            }
            if (!updatePositionWithFocus(oldLayout)) {
                updateVisibleSlotRange();
            }
            if (CommonAlbumSlotView.this.mSlotScrollBar != null) {
                CommonAlbumSlotView.this.mSlotScrollBar.updateContentLen(CommonAlbumSlotView.this.mLayout.getScrollLimit());
            }
        }

        protected void initLayoutParameters(Layout oldLayout) {
            if (this.mWidth != 0 && this.mHeight != 0) {
                this.mSlotWidthGap = CommonAlbumSlotView.this.mCommonLayout ? this.mSpec.slot_gap : this.mSpec.camera_video_slot_gap;
                this.mSlotHeightGap = this.mSlotWidthGap;
                this.mVerticalPadding.startAnimateTo(0);
                int horizontalPadding = Math.max(0, (CommonAlbumSlotView.this.mCommonLayout ? this.mSpec.slot_horizontal_padding : this.mSpec.camera_video_slot_horizontal_padding) - this.mSlotWidthGap);
                this.mHorizontalPadding.startAnimateTo(horizontalPadding);
                if (isPort()) {
                    this.mUnitCount = CommonAlbumSlotView.this.mCommonLayout ? this.mSpec.port_slot_count : this.mSpec.camera_video_port_slot_count;
                } else {
                    this.mUnitCount = CommonAlbumSlotView.this.mCommonLayout ? this.mSpec.land_slot_count : this.mSpec.camera_video_land_slot_count;
                }
                this.mSlotWidth = ((this.mWidth - (horizontalPadding * 2)) - ((this.mUnitCount - 1) * this.mSlotWidthGap)) / this.mUnitCount;
                this.mSlotHeight = this.mSlotWidth;
                int count = ((this.mSlotCount + this.mUnitCount) - 1) / this.mUnitCount;
                this.mContentLength = ((this.mSlotHeight * count) + ((count - 1) * this.mSlotHeightGap)) + 0;
                onInitLayoutfinish(oldLayout);
            }
        }

        protected boolean updatePositionWithFocus(Layout oldLayout) {
            if (oldLayout == null) {
                return false;
            }
            int visibleStart = oldLayout.getVisibleStart();
            if (visibleStart == 0 && oldLayout.getVisibleEnd() == 0) {
                return false;
            }
            int newScrollPosition = (int) ((((float) this.mSlotHeight) * (((float) (this.mScrollPosition - oldLayout.getSlotRect(visibleStart, CommonAlbumSlotView.this.mTempRect).top)) / ((float) oldLayout.mSlotHeight))) + ((float) getSlotRect(visibleStart, CommonAlbumSlotView.this.mTempRect).top));
            boolean scrollPositionChanged = newScrollPosition != this.mScrollPosition;
            setScrollPosition(newScrollPosition);
            return scrollPositionChanged;
        }

        public void setSize(int width, int height) {
            Layout layout = clone();
            this.mWidth = width;
            this.mHeight = height;
            initLayoutParameters(layout);
        }

        protected void updateVisibleSlotRange() {
            int position = Math.max(0, (this.mScrollPosition - this.mVerticalPadding.get()) - CommonAlbumSlotView.this.mHeadCoverHeight);
            setVisibleRange(Math.max(0, this.mUnitCount * (position / (this.mSlotHeight + this.mSlotHeightGap))), Math.min(this.mSlotCount, this.mUnitCount * ((((((this.mHeight + position) + this.mSlotHeight) + this.mSlotHeightGap) - 1) / (this.mSlotHeight + this.mSlotHeightGap)) + 1)));
        }

        public void setScrollPosition(int position) {
            if (this.mScrollPosition != position) {
                this.mScrollPosition = position;
                updateVisibleSlotRange();
                if (CommonAlbumSlotView.this.mSlotScrollBar != null) {
                    CommonAlbumSlotView.this.mSlotScrollBar.updateContentOffset(this.mScrollPosition);
                }
            }
        }

        protected void setVisibleRange(int start, int end) {
            if (start != this.mVisibleStart || end != this.mVisibleEnd) {
                if (start < end) {
                    this.mVisibleStart = start;
                    this.mVisibleEnd = end;
                } else {
                    this.mVisibleEnd = 0;
                    this.mVisibleStart = 0;
                }
                if (CommonAlbumSlotView.this.mRenderer != null) {
                    CommonAlbumSlotView.this.mRenderer.onVisibleRangeChanged(this.mVisibleStart, this.mVisibleEnd);
                }
            }
        }

        public int getVisibleStart() {
            return this.mVisibleStart;
        }

        public int getVisibleEnd() {
            return this.mVisibleEnd;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int getSlotIndexByPosition(float x, float y) {
            int absoluteX = Math.round(x) - this.mHorizontalPadding.get();
            int absoluteY = ((Math.round(y) + this.mScrollPosition) - CommonAlbumSlotView.this.mHeadCoverHeight) - this.mVerticalPadding.get();
            if (absoluteX < 0 || absoluteY < 0) {
                return -1;
            }
            int columnIdx = absoluteX / (this.mSlotWidth + this.mSlotWidthGap);
            if (CommonAlbumSlotView.this.mIsLayoutRtl) {
                columnIdx = (this.mUnitCount - columnIdx) - 1;
            }
            int rowIdx = absoluteY / (this.mSlotHeight + this.mSlotHeightGap);
            if (columnIdx >= this.mUnitCount || columnIdx < 0 || absoluteX % (this.mSlotWidth + this.mSlotWidthGap) <= this.mSlotWidthGap || absoluteY % (this.mSlotHeight + this.mSlotHeightGap) <= this.mSlotHeightGap) {
                return -1;
            }
            int index = (this.mUnitCount * rowIdx) + columnIdx;
            if (index >= this.mSlotCount) {
                index = -1;
            }
            return index;
        }

        public boolean advanceAnimation(long animTime) {
            return this.mVerticalPadding.calculate(animTime) | this.mHorizontalPadding.calculate(animTime);
        }

        public void transformRect(int index, int downIndex, float progress, Rect target) {
            int unitCount = this.mUnitCount;
            int width = this.mSlotWidth;
            int height = this.mSlotHeight;
            float[] shift = new float[2];
            int dx = (downIndex % unitCount) - (index % unitCount);
            int dy = (downIndex / unitCount) - (index / unitCount);
            float distance = (float) Math.sqrt((double) ((dx * dx) + (dy * dy)));
            float factor = (24.0f * (Math.abs(dx) + Math.abs(dy) == 1 ? 1.2f : WMElement.CAMERASIZEVALUE1B1)) * (CommonAlbumSlotView.this.mCommonLayout ? WMElement.CAMERASIZEVALUE1B1 : 2.0f);
            shift[0] = dx == 0 ? 0.0f : ((((float) width) / (distance * factor)) * Math.signum((float) dx)) * progress;
            shift[1] = dy == 0 ? 0.0f : ((((float) height) / (distance * factor)) * Math.signum((float) dy)) * progress;
            CommonAlbumSlotView.this.translateRect(target, shift);
        }

        public boolean isAlbumName(float y) {
            return false;
        }

        public TextPaint getTextPaint(int index) {
            return null;
        }
    }

    protected static class IntegerAnimation extends Animation {
        private int mCurrent = 0;
        private boolean mEnabled = false;
        private int mFrom = 0;
        private int mTarget;

        protected IntegerAnimation() {
        }

        public void setEnabled(boolean enabled) {
            this.mEnabled = enabled;
        }

        public void startAnimateTo(int target) {
            if (!this.mEnabled) {
                this.mCurrent = target;
                this.mTarget = target;
            } else if (target != this.mTarget) {
                this.mFrom = this.mCurrent;
                this.mTarget = target;
                setDuration(180);
                start();
            }
        }

        public int get() {
            return this.mCurrent;
        }

        public int getTarget() {
            return this.mTarget;
        }

        protected void onCalculate(float progress) {
            this.mCurrent = Math.round(((float) this.mFrom) + (((float) (this.mTarget - this.mFrom)) * progress));
            if (progress == WMElement.CAMERASIZEVALUE1B1) {
                this.mEnabled = false;
            }
        }
    }

    protected class MyGestureListener implements OnGestureListener {
        private boolean isDown = false;
        private Handler mTimeOutHandler = new Handler() {
            public void handleMessage(Message msg) {
                MyGestureListener.this.cancelDown(false);
            }
        };

        protected MyGestureListener() {
        }

        public void onShowPress(MotionEvent e) {
            CommonAlbumSlotView.this.logd("CommonAlbumSlotView onShowPress");
            GLRoot root = CommonAlbumSlotView.this.getGLRoot();
            if (root != null) {
                root.lockRenderThread();
                try {
                    if (!isDown()) {
                        int index = CommonAlbumSlotView.this.mIndexDown;
                        if (!(index == -1 || CommonAlbumSlotView.this.mListener == null)) {
                            setDownFlag(true);
                            CommonAlbumSlotView.this.mListener.onDown(index);
                        }
                        root.unlockRenderThread();
                    }
                } finally {
                    root.unlockRenderThread();
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected synchronized void cancelDown(boolean byLongPress) {
            CommonAlbumSlotView.this.logd("cancelDown");
            cancelTimeOut();
            if (isDown()) {
                setDownFlag(false);
                if (CommonAlbumSlotView.this.mListener != null) {
                    CommonAlbumSlotView.this.mListener.onUp(byLongPress);
                }
            }
        }

        public boolean onDown(MotionEvent e) {
            CommonAlbumSlotView.this.logd("onDown");
            cancelTimeOut();
            CommonAlbumSlotView.this.mIndexUp = -1;
            CommonAlbumSlotView.this.mIndexDown = CommonAlbumSlotView.this.mLayout.getSlotIndexByPosition(e.getX(), e.getY());
            startTimeOut();
            return false;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            CommonAlbumSlotView.this.logd("onFling");
            cancelDown(false);
            if (CommonAlbumSlotView.this.mPreviewMode) {
                return true;
            }
            if (CommonAlbumSlotView.this.mFirstFling) {
                CommonAlbumSlotView.this.mFirstFling = false;
            }
            int scrollLimit = CommonAlbumSlotView.this.mLayout.getScrollLimit();
            if (scrollLimit == 0) {
                return false;
            }
            CommonAlbumSlotView.this.mSlotScrollBar.enterFastScrollMode(velocityY);
            CommonAlbumSlotView.this.mScroller.fling((int) (-velocityY), 0, scrollLimit);
            CommonAlbumSlotView.this.invalidate();
            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (CommonAlbumSlotView.this.mPreviewMode) {
                return true;
            }
            CommonAlbumSlotView.this.logd("onScroll");
            cancelDown(false);
            if (CommonAlbumSlotView.this.mFirstScroll) {
                CommonAlbumSlotView.this.mFirstScroll = false;
                CommonAlbumSlotView.this.mDoSelect = Math.abs(distanceX) > Math.abs(distanceY);
            }
            if (CommonAlbumSlotView.this.mListener != null && CommonAlbumSlotView.this.mListener.inSelectionMode() && CommonAlbumSlotView.this.mDoSelect) {
                CommonAlbumSlotView.this.mListener.onScroll(CommonAlbumSlotView.this.mLayout.getSlotIndexByPosition(e2.getX(), e2.getY()));
            } else {
                int overDistance = CommonAlbumSlotView.this.mScroller.startScroll(Math.round(distanceY), 0, CommonAlbumSlotView.this.mLayout.getScrollLimit(), CommonAlbumSlotView.this.mLayout.mHeight);
                if (CommonAlbumSlotView.this.mOverscrollEffect == 0 && overDistance != 0) {
                    CommonAlbumSlotView.this.mPaper.overScroll((float) overDistance);
                }
            }
            CommonAlbumSlotView.this.invalidate();
            return true;
        }

        public boolean onSingleTapUp(MotionEvent e) {
            CommonAlbumSlotView.this.logd("onSingleTapUp");
            CommonAlbumSlotView.this.mIndexUp = CommonAlbumSlotView.this.mLayout.getSlotIndexByPosition(e.getX(), e.getY());
            boolean isCornerPressed = false;
            if (CommonAlbumSlotView.this.mRenderer != null) {
                isCornerPressed = CommonAlbumSlotView.this.mRenderer.isCornerPressed(e.getX(), e.getY(), CommonAlbumSlotView.this.mLayout.getSlotRect(CommonAlbumSlotView.this.mIndexUp, new Rect()), CommonAlbumSlotView.this.mLayout.mScrollPosition, false);
            }
            cancelDown(false);
            if (!(CommonAlbumSlotView.this.mDownInScrolling || CommonAlbumSlotView.this.mIndexUp == -1 || CommonAlbumSlotView.this.mListener == null)) {
                CommonAlbumSlotView.this.mListener.onSingleTapUp(CommonAlbumSlotView.this.mIndexUp, isCornerPressed);
            }
            return true;
        }

        public void onLongPress(MotionEvent e) {
            CommonAlbumSlotView.this.logd("onLongPress");
            cancelDown(true);
            if (!CommonAlbumSlotView.this.mDownInScrolling) {
                CommonAlbumSlotView.this.lockRendering();
                try {
                    int index = CommonAlbumSlotView.this.mLayout.getSlotIndexByPosition(e.getX(), e.getY());
                    if (!(index == -1 || CommonAlbumSlotView.this.mListener == null)) {
                        CommonAlbumSlotView.this.mListener.onLongTap(index);
                    }
                    CommonAlbumSlotView.this.unlockRendering();
                } catch (Throwable th) {
                    CommonAlbumSlotView.this.unlockRendering();
                }
            }
        }

        protected synchronized void setDownFlag(boolean down) {
            this.isDown = down;
        }

        protected synchronized boolean isDown() {
            return this.isDown;
        }

        protected void startTimeOut() {
            this.mTimeOutHandler.sendEmptyMessageDelayed(0, (long) CommonAlbumSlotView.DOWN_TIME_OUT);
        }

        protected void cancelTimeOut() {
            this.mTimeOutHandler.removeMessages(0);
        }
    }

    public class MyScrollListener implements com.android.gallery3d.ui.SlotScrollBarView.Listener {
        public void updateScrollPosition(int offset, int contentLen) {
            CommonAlbumSlotView.this.setScrollPosition((int) ((((float) CommonAlbumSlotView.this.mLayout.getScrollLimit()) * ((float) offset)) / ((float) contentLen)));
            CommonAlbumSlotView.this.invalidate();
        }

        public void onDown() {
            GalleryLog.printDFXLog("CommonAlbumSlotView onDown called  for DFX");
            CommonAlbumSlotView.this.setScrollPosition(CommonAlbumSlotView.this.mScrollY);
            CommonAlbumSlotView.this.invalidate();
        }
    }

    public static class Spec {
        public int album_name_text_height = -1;
        public int camera_video_land_slot_count = -1;
        public int camera_video_port_slot_count = -1;
        public int camera_video_slot_gap = -1;
        public int camera_video_slot_horizontal_padding = -1;
        @SuppressWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
        public int camera_video_slot_vertical_padding = -1;
        public int land_slot_count = -1;
        public int port_slot_count = -1;
        public int slotHeight = -1;
        public int slotWidth = -1;
        public int slot_gap = -1;
        public int slot_horizontal_padding = -1;
        public int slot_vertical_padding = -1;

        public int getDefaultWidth() {
            int horizontalPadding = Math.max(0, this.slot_horizontal_padding - this.slot_gap);
            int unitCount = this.port_slot_count;
            return ((GalleryUtils.getWidthPixels() - (horizontalPadding * 2)) - ((unitCount + 1) * this.slot_gap)) / unitCount;
        }
    }

    public CommonAlbumSlotView(GalleryContext activity, Spec spec) {
        boolean z = true;
        super(activity.getAndroidContext());
        this.mGestureDetector = createGestureDetector(activity);
        this.mScroller = new ScrollerHelper(activity.getAndroidContext());
        ScrollerHelper scrollerHelper = this.mScroller;
        if (this.mOverscrollEffect != 1) {
            z = false;
        }
        scrollerHelper.setOverfling(z);
        this.mLayout = createLayout(spec);
        this.mCurrentLayout = this.mLayout;
    }

    protected GestureDetector createGestureDetector(GalleryContext activity) {
        return new GestureDetector(activity.getAndroidContext(), new MyGestureListener());
    }

    public void setHeadCoverHeight(int height) {
        this.mHeadCoverHeight = height;
    }

    public int getSlotIndexByPosition(float x, float y) {
        return this.mLayout.getSlotIndexByPosition(x, y);
    }

    public Rect getPreviewImageRect(float x, float y) {
        int index = this.mLayout.getSlotIndexByPosition(x, y);
        if (index < 0) {
            return null;
        }
        Rect rect = getSlotRect(index);
        int left = (rect.left + this.mBounds.left) - this.mScrollX;
        int top = (rect.top + this.mBounds.top) - this.mScrollY;
        rect.set(left, top, left + rect.width(), top + rect.height());
        return rect;
    }

    public void setScrollBar(SlotScrollBarView scrollBar) {
        this.mSlotScrollBar = scrollBar;
        this.mSlotScrollBar.setScrollListener(new MyScrollListener());
    }

    public void setCommonLayout(boolean commonLayout) {
        this.mCommonLayout = commonLayout;
    }

    public void setSlotRenderer(SlotRenderer slotDrawer) {
        this.mRenderer = slotDrawer;
        this.mCurrentSlotRender = slotDrawer;
        if (this.mRenderer != null) {
            this.mRenderer.onSlotSizeChanged(this.mLayout.mSlotWidth, this.mLayout.mSlotHeight);
            this.mRenderer.onVisibleRangeChanged(getVisibleStart(), getVisibleEnd());
        }
    }

    public void updatePreviewMode(boolean previewMode) {
        this.mPreviewMode = previewMode;
    }

    public Rect getAnimRect() {
        if (this.mIndexUp > this.mLayout.mVisibleEnd - 1 || this.mIndexUp < this.mLayout.mVisibleStart) {
            GalleryLog.e("CommonAlbumSlotView", "the up index is wrong, this should not happen");
            return null;
        }
        Rect rt = getSlotRect(this.mIndexUp);
        rt.offset(-this.mScrollX, -this.mScrollY);
        rt.offset(this.mBounds.left, this.mBounds.top);
        return rt;
    }

    public void setCenterIndex(int index) {
        int slotCount = this.mLayout.mSlotCount;
        if (index >= 0 && index < slotCount) {
            Rect rect = this.mLayout.getSlotRect(index, this.mTempRect);
            setScrollPosition(((rect.top + rect.bottom) - getHeight()) / 2);
        }
    }

    public void makeSlotVisible(int index) {
        Rect rect = this.mLayout.getSlotRect(Math.max(0, index), this.mTempRect);
        int visibleBegin = this.mScrollY;
        int visibleLength = getHeight();
        int visibleEnd = visibleLength + visibleBegin;
        int slotEnd = rect.bottom;
        int slotBegin = rect.top;
        int position = visibleBegin;
        if (visibleLength < slotEnd - slotBegin) {
            position = visibleBegin;
        } else if (slotBegin < visibleBegin) {
            position = slotBegin;
        } else if (slotEnd > visibleEnd) {
            position = slotEnd - visibleLength;
        }
        GalleryLog.printDFXLog("DFX position " + position);
        setScrollPosition(position);
    }

    public void setScrollPosition(int position) {
        position = Utils.clamp(position, 0, this.mLayout.getScrollLimit());
        this.mScroller.setPosition(position);
        updateScrollPosition(position, false);
    }

    public void addComponent(GLView view) {
        throw new UnsupportedOperationException();
    }

    protected void onLayout(boolean changeSize, int l, int t, int r, int b) {
        if (changeSize) {
            GalleryLog.printDFXLog("CommonAlbumSlotView onLayout  for DFX");
            this.mLayout.setSize(r - l, b - t);
            setScrollPosition(this.mLayout.mScrollPosition);
            if (this.mOverscrollEffect == 0) {
                this.mPaper.setSize(r - l, b - t);
            }
        }
    }

    private void updateScrollPosition(int position, boolean force) {
        if (force || position != this.mScrollY) {
            this.mScrollY = position;
            this.mLayout.setScrollPosition(position);
            onScrollPositionChanged(position);
        }
    }

    protected void onScrollPositionChanged(int newPosition) {
        int limit = this.mLayout.getScrollLimit();
        if (this.mListener != null) {
            this.mListener.onScrollPositionChanged(newPosition, limit);
        }
    }

    public Rect getSlotRect(int slotIndex) {
        return this.mLayout.getSlotRect(slotIndex, new Rect());
    }

    protected boolean onTouch(MotionEvent event) {
        boolean z = false;
        if (event.getAction() == 0) {
            event.setDownTime(event.getEventTime());
        }
        this.mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case 0:
                this.mFirstScroll = true;
                this.mFirstFling = true;
                this.mDoSelect = false;
                if (!this.mScroller.isFinished()) {
                    z = true;
                }
                this.mDownInScrolling = z;
                this.mScroller.forceFinished();
                if (this.mListener != null) {
                    this.mListener.onTouchDown(event);
                    break;
                }
                break;
            case 1:
                this.mFirstScroll = true;
                this.mFirstFling = true;
                this.mDoSelect = false;
                this.mPaper.onRelease();
                if (this.mListener != null) {
                    this.mListener.onTouchUp(event);
                }
                this.mScroller.release(this.mLayout.getScrollLimit());
                invalidate();
                break;
            case 2:
                if (this.mListener != null) {
                    this.mListener.onTouchMove(event);
                    break;
                }
                break;
        }
        return true;
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    protected void render(GLCanvas canvas) {
        if (this.mRenderer != null) {
            this.mRenderer.prepareDrawing();
            long animTime = AnimationTime.get();
            boolean more = this.mScroller.advanceAnimation(animTime);
            if (this.mSlotScrollBar != null) {
                if (this.mScroller.getPosition() > this.mLayout.getScrollLimit()) {
                    this.mSlotScrollBar.updateContentLen(this.mScroller.getPosition());
                }
                if (more) {
                    this.mSlotScrollBar.show();
                } else {
                    this.mSlotScrollBar.hide();
                }
            }
            more |= this.mLayout.advanceAnimation(animTime);
            int oldX = this.mScrollY;
            updateScrollPosition(this.mScroller.getPosition(), false);
            boolean paperActive = false;
            if (this.mOverscrollEffect == 0) {
                int newX = this.mScrollY;
                int limit = this.mLayout.getScrollLimit();
                if (oldX <= 0 || newX != 0) {
                    if (oldX < limit && newX == limit) {
                    }
                    paperActive = this.mPaper.advanceAnimation();
                }
                float v = this.mScroller.getCurrVelocity();
                if (newX == limit) {
                    v = -v;
                }
                if (!Float.isNaN(v)) {
                    this.mPaper.edgeReached(v);
                }
                paperActive = this.mPaper.advanceAnimation();
            }
            more = (more | paperActive) | needRenderSlotAnimationMore(animTime);
            canvas.translate((float) (-this.mScrollX), (float) (-this.mScrollY));
            renderDeletedItem(canvas);
            int i = this.mLayout.mVisibleEnd - 1;
            while (i >= this.mLayout.mVisibleStart) {
                if (!"fade_texture".equals(this.mTextureType) || i != this.mIndexUp) {
                    renderItem(canvas, i, 0, paperActive);
                }
                i--;
            }
            canvas.translate((float) this.mScrollX, (float) this.mScrollY);
            if (this.mLayout.mVisibleEnd - 1 >= this.mLayout.mVisibleStart) {
                this.mRenderer.renderTopTitle(canvas, 0, -this.mScrollY, getWidth(), this.mHeadCoverHeight - this.mScrollY);
            }
            if (more) {
                invalidate();
            }
        }
    }

    private void renderDeletedItem(GLCanvas canvas) {
        if (this.mRenderer != null) {
            DeleteSlotAnimation deleteSlotAnimation = this.mDeleteSlotAnimation;
            if (deleteSlotAnimation != null) {
                HashMap<Path, Object> visiblePathEntryMap = deleteSlotAnimation.getVisibleItemPathMap();
                HashMap<Object, Object> visibleIndexEntryMap = deleteSlotAnimation.getVisibleItemIndexMap();
                if (visiblePathEntryMap != null && visibleIndexEntryMap != null) {
                    Layout fromLayout = (Layout) deleteSlotAnimation.getFromLayout();
                    if (fromLayout != null) {
                        int i;
                        for (i = this.mLayout.mVisibleEnd - 1; i >= this.mLayout.mVisibleStart; i--) {
                            Path targetPath = this.mRenderer.getItemPath(Integer.valueOf(i));
                            if (targetPath != null) {
                                AlbumEntry fromEntry = (AlbumEntry) visiblePathEntryMap.get(targetPath);
                                if (fromEntry != null) {
                                    fromEntry.guessDeleted = false;
                                }
                            }
                        }
                        for (i = fromLayout.mVisibleEnd - 1; i >= fromLayout.mVisibleStart; i--) {
                            AlbumEntry entry = (AlbumEntry) visibleIndexEntryMap.get(Integer.valueOf(i));
                            if (entry != null && entry.guessDeleted) {
                                Rect rect = fromLayout.getSlotRect(i, this.mTempRect);
                                rect.set(rect.left, (rect.top - fromLayout.mScrollPosition) + this.mLayout.mScrollPosition, rect.right, (rect.bottom - fromLayout.mScrollPosition) + this.mLayout.mScrollPosition);
                                deleteSlotAnimation.applyDeletedItem(rect);
                                canvas.save(3);
                                canvas.setAlpha(deleteSlotAnimation.getAlpha());
                                canvas.translate((float) rect.left, (float) rect.top, 0.0f);
                                this.mRenderer.renderSlot(canvas, entry, 0, rect.width(), rect.height());
                                canvas.restore();
                            }
                        }
                    }
                }
            }
        }
    }

    public void transformRect(Object index, Object downIndex, float progress, Rect target) {
        this.mLayout.transformRect(((Integer) index).intValue(), ((Integer) downIndex).intValue(), progress, target);
    }

    private void renderItem(GLCanvas canvas, int index, int pass, boolean paperActive) {
        if (this.mRenderer != null) {
            canvas.save(3);
            Rect rect = this.mLayout.getSlotRect(index, this.mTempRect);
            if (paperActive) {
                canvas.multiplyMatrix(this.mPaper.getTransform(rect, (float) this.mScrollY), 0);
            } else {
                DownShiftAnimation downShiftAnimation = this.mDownShiftAnimation;
                if (downShiftAnimation != null) {
                    downShiftAnimation.apply(Integer.valueOf(index), rect);
                }
                DeleteSlotAnimation deleteSlotAnimation = this.mDeleteSlotAnimation;
                if (deleteSlotAnimation != null) {
                    deleteSlotAnimation.apply(Integer.valueOf(index), rect);
                }
                canvas.translate((float) rect.left, (float) rect.top, 0.0f);
            }
            this.mRenderer.renderSlot(canvas, index, pass, rect.right - rect.left, rect.bottom - rect.top);
            canvas.restore();
        }
    }

    public boolean setSlotCount(int slotCount) {
        boolean changed = this.mLayout.setSlotCount(slotCount);
        if (this.mStartIndex != -1) {
            setCenterIndex(this.mStartIndex);
            this.mStartIndex = -1;
        }
        setScrollPosition(this.mScrollY);
        return changed;
    }

    public int getVisibleStart() {
        return this.mLayout.getVisibleStart();
    }

    public int getVisibleEnd() {
        return this.mLayout.getVisibleEnd();
    }

    public int getScrollX() {
        return this.mScrollX;
    }

    public int getScrollY() {
        return this.mScrollY;
    }

    protected void logd(String message) {
        if (Constant.DBG) {
            GalleryLog.d("CommonAlbumSlotView", message);
        }
    }

    public AbsLayout cloneLayout() {
        return this.mLayout.clone();
    }

    public SlotUIListener getSlotUIListener() {
        return this.mListener;
    }

    protected Layout createLayout(Spec spec) {
        return new Layout(spec);
    }

    public void setIndexUp(int indexUp) {
        this.mIndexUp = indexUp;
    }

    public boolean isScrolling() {
        return (this.mFirstScroll && this.mFirstFling) ? false : true;
    }
}
