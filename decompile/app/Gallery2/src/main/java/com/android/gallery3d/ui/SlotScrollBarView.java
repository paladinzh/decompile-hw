package com.android.gallery3d.ui;

import android.os.Looper;
import android.os.Message;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.anim.AlphaAnimation;
import com.huawei.gallery.anim.CanvasAnimation;
import com.huawei.watermark.manager.parse.WMElement;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class SlotScrollBarView extends GLView {
    public static final int SCROLLBAR_VIEW_MARGIN = GalleryUtils.dpToPixel(8);
    protected int mContentLen;
    protected int mContentOffset;
    protected final SynchronizedHandler mHandler;
    protected CanvasAnimation mHideAnimation;
    protected boolean mLayoutRTL = GalleryUtils.isLayoutRTL();
    protected Listener mScrollListener;
    protected int mViewHeight;
    protected int mViewWidth;

    public interface Listener {
        void onDown();

        void updateScrollPosition(int i, int i2);
    }

    public SlotScrollBarView(GalleryContext context) {
        this.mHandler = new SynchronizedHandler(Looper.getMainLooper(), context.getGLRoot()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        SlotScrollBarView.this.setVisibility(1);
                        SlotScrollBarView.this.mHideAnimation = new AlphaAnimation(WMElement.CAMERASIZEVALUE1B1, 0.0f);
                        SlotScrollBarView.this.mHideAnimation.setDuration(500);
                        SlotScrollBarView.this.startAnimation(SlotScrollBarView.this.mHideAnimation);
                        return;
                    case 1:
                        SlotScrollBarView.this.setVisibility(0);
                        CanvasAnimation showAnimation = new AlphaAnimation(0.0f, WMElement.CAMERASIZEVALUE1B1);
                        showAnimation.setDuration(SmsCheckResult.ESCT_200);
                        SlotScrollBarView.this.startAnimation(showAnimation);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    protected void onLayout(boolean changeSize, int l, int t, int r, int b) {
        this.mViewWidth = r - l;
        this.mViewHeight = b - t;
    }

    protected void onDetachFromRoot() {
        this.mHandler.removeMessages(0);
        this.mHandler.removeMessages(1);
        setVisibility(1);
        super.onDetachFromRoot();
    }

    public void setGLRoot(GLRoot root) {
        this.mHandler.setGLRoot(root);
    }

    public void show() {
        this.mHandler.removeMessages(0);
        if (getVisibility() != 0 && !this.mHandler.hasMessages(1)) {
            this.mHandler.sendEmptyMessage(1);
        }
    }

    public void hide() {
        if (1 != getVisibility() && !this.mHandler.hasMessages(0)) {
            this.mHandler.sendEmptyMessageDelayed(0, 600);
        }
    }

    public void enterFastScrollMode(float velocity) {
    }

    public void updateContentLen(int len) {
        this.mContentLen = len;
    }

    public void updateContentOffset(int offset) {
        this.mContentOffset = offset;
    }

    public void setScrollListener(Listener listener) {
        this.mScrollListener = listener;
    }
}
