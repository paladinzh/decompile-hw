package com.android.deskclock;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.android.util.Log;
import java.lang.ref.WeakReference;

public class CustViewPage extends ViewPager {
    private static boolean mSlip = true;
    private LocalHandler mHandler;
    private int mOrientation;
    private int mPageScrollOffset;
    private int mWidthDefault;
    private int oldRight;

    static class LocalHandler extends Handler {
        private WeakReference<CustViewPage> mContextWR;

        public LocalHandler(CustViewPage context) {
            this.mContextWR = new WeakReference(context);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CustViewPage custViewPage = (CustViewPage) this.mContextWR.get();
            if (custViewPage != null) {
                switch (msg.what) {
                    case 0:
                        int x = (custViewPage.mPageScrollOffset + custViewPage.getCurrentItem()) * custViewPage.getWidth();
                        Log.d("CustViewPage", "handleMessage : curItem = " + custViewPage.getCurrentItem() + " scrollTo " + x);
                        custViewPage.scrollTo(x, 0);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public CustViewPage(Context context) {
        this(context, null);
    }

    public CustViewPage(Context context, AttributeSet attrs) {
        int i = 720;
        super(context, attrs);
        this.mOrientation = -1;
        this.mWidthDefault = 720;
        if (getResources().getConfiguration().orientation != 1) {
            i = 1198;
        }
        this.mWidthDefault = i;
        init();
    }

    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        try {
            if (mSlip) {
                return super.onInterceptTouchEvent(arg0);
            }
            return false;
        } catch (IllegalArgumentException e) {
            Log.e("CustViewPage", "onInterceptTouchEvent : IllegalArgumentException" + e.getMessage());
            return false;
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mOrientation < 0) {
            this.mOrientation = getResources().getConfiguration().orientation;
            if (getWidth() == 0) {
                this.mPageScrollOffset = (getScrollX() / this.mWidthDefault) - getCurrentItem();
            } else {
                this.mPageScrollOffset = (getScrollX() / getWidth()) - getCurrentItem();
            }
            Log.i("CustViewPage", "onLayout : mPageScrollOffset = " + this.mPageScrollOffset + " mWidthDefault = " + this.mWidthDefault);
        }
        int orientation = getResources().getConfiguration().orientation;
        if (!(this.mOrientation == orientation || this.oldRight == this.mRight)) {
            this.mHandler.sendEmptyMessageDelayed(0, 200);
            this.mOrientation = orientation;
        }
        this.oldRight = right;
    }

    public void init() {
        this.mHandler = new LocalHandler(this);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mHandler.removeCallbacksAndMessages(null);
        setBackgroundDrawable(null);
        recycleBackBitmap();
    }

    private void recycleBackBitmap() {
    }
}
