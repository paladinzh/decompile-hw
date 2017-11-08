package com.android.contacts.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.android.contacts.calllog.CallLogFragment.CallLogTouch;
import com.android.contacts.util.HwLog;

public class SuspentionScroller extends RelativeLayout implements OnTouchListener, OnGestureListener {
    private GestureDetector detector = new GestureDetector(this);
    private LayoutParams layoutParams;
    private Context mContext;
    private CallLogTouch mDailpadFragmentListener;
    private View mHeadView;
    private boolean mInterceptTouch;
    private ListView mListView;
    private int mScrollState = 0;
    private View mSuspentionView;
    private int mSuspentionViewHeight;
    private int mVisibility;

    public static class SuspentionAnimation extends Animation {
        private int mAnimateViewHeight;
        private View mAnimatedView;
        private int mType;
        private int mdistance;

        public SuspentionAnimation(View view, int type, int distance, int textviewHeight) {
            this.mAnimatedView = view;
            this.mAnimateViewHeight = textviewHeight;
            this.mType = type;
            this.mdistance = distance;
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            if (interpolatedTime < 1.0f) {
                if (this.mType == 0) {
                    this.mAnimatedView.scrollTo(0, this.mdistance - ((int) (((float) this.mdistance) * interpolatedTime)));
                    return;
                }
                this.mAnimatedView.scrollTo(0, (int) ((((float) this.mAnimateViewHeight) * interpolatedTime) + (((float) this.mdistance) * (1.0f - interpolatedTime))));
            } else if (this.mType == 0) {
                this.mAnimatedView.scrollTo(0, 0);
            } else {
                this.mAnimatedView.scrollTo(0, this.mAnimateViewHeight);
            }
        }
    }

    private static class SuspentionViewTouchListenr implements OnTouchListener {
        private SuspentionViewTouchListenr() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    public SuspentionScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.layoutParams = new LayoutParams(-1, 0);
    }

    private View addHeaderView() {
        View view = new View(this.mContext);
        this.layoutParams.height = this.mSuspentionViewHeight - this.mSuspentionView.getScrollY();
        view.setLayoutParams(this.layoutParams);
        return view;
    }

    private void setViewHeight(int height) {
        if (this.layoutParams == null || this.mHeadView == null) {
            HwLog.w("SuspentionScroller", "layoutParams IS NULL or mHeadView is NULL");
            return;
        }
        this.layoutParams.height = height;
        this.mHeadView.setLayoutParams(this.layoutParams);
    }

    public void setSuspentionView(View view) {
        this.mSuspentionView = view;
        this.mSuspentionViewHeight = this.mSuspentionView.getMeasuredHeight();
    }

    public void setSuspentionViewHeight(int height) {
        this.mSuspentionViewHeight = height;
    }

    public void init() {
        this.mListView.setOnTouchListener(this);
        this.mHeadView = addHeaderView();
        this.mListView.addHeaderView(this.mHeadView, null, false);
        this.mVisibility = this.layoutParams.height == 0 ? 2 : 1;
        addView(this.mSuspentionView);
    }

    public void setListView(boolean onScrolled) {
        this.mListView = (ListView) findViewById(16908298);
        if (!onScrolled) {
            this.mListView.setFastScrollEnabled(true);
        }
    }

    public void setCallLogListener(CallLogTouch listener) {
        this.mDailpadFragmentListener = listener;
    }

    public void showSuspentionView() {
        startAnimation(0);
    }

    public void hideSuspentionView(int itemPos, boolean switchState) {
        startAnimation(1);
        if (itemPos == 0) {
            setViewHeight(0);
        }
        this.mInterceptTouch = switchState;
    }

    private void applyTransformation(int distance) {
        if ((this.mScrollState == 1 || !showTotalItem()) && Math.abs(distance) <= 50 && this.mSuspentionView != null) {
            if ((this.mSuspentionView.getScrollY() < this.mSuspentionViewHeight || distance <= 0) && (this.mSuspentionView.getScrollY() > 0 || distance >= 0)) {
                this.mSuspentionView.scrollBy(0, distance);
                if (!(this.mHeadView == null || this.layoutParams.height == this.mSuspentionViewHeight)) {
                    setViewHeight(this.mSuspentionViewHeight - this.mSuspentionView.getScrollY());
                }
                this.mVisibility = -1;
            } else if (distance > 0) {
                setViewHeight(0);
                this.mSuspentionView.scrollTo(0, this.mSuspentionViewHeight);
                this.mVisibility = 2;
            } else {
                this.mSuspentionView.scrollTo(0, 0);
                this.mVisibility = 1;
            }
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (this.mDailpadFragmentListener != null) {
            this.mDailpadFragmentListener.touchLogCalls(event);
        }
        if (event.getAction() == 1) {
            if (this.mSuspentionView.getScrollY() < this.mSuspentionViewHeight / 2 || (this.mListView.getFirstVisiblePosition() == 0 && this.mVisibility != 2)) {
                startAnimation(0);
            } else {
                startAnimation(1);
            }
        }
        if (this.mSuspentionView == null || this.mInterceptTouch || (showTotalItem() && this.layoutParams.height == this.mSuspentionViewHeight)) {
            return false;
        }
        this.detector.onTouchEvent(event);
        return false;
    }

    private void startAnimation(int type) {
        if (this.mSuspentionView != null) {
            if (this.mVisibility != (type == 0 ? 1 : 2)) {
                int distance;
                if (type == 0) {
                    distance = this.mSuspentionView.getScrollY();
                    if (HwLog.HWDBG) {
                        HwLog.d("SuspentionScroller", "EXPAND");
                    }
                    this.mSuspentionView.setOnTouchListener(new SuspentionViewTouchListenr());
                    setViewHeight(this.mSuspentionViewHeight);
                } else {
                    distance = this.mSuspentionView.getScrollY();
                    this.mSuspentionView.setOnTouchListener(null);
                    setViewHeight(0);
                    if (HwLog.HWDBG) {
                        HwLog.d("SuspentionScroller", "COLLAPSE");
                    }
                }
                this.mVisibility = type == 0 ? 1 : 2;
                SuspentionAnimation animation = new SuspentionAnimation(this.mSuspentionView, type, distance, this.mSuspentionViewHeight);
                animation.setDuration(280);
                this.mSuspentionView.startAnimation(animation);
            }
        }
    }

    public boolean onDown(MotionEvent e) {
        return false;
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        applyTransformation((int) distanceY);
        return false;
    }

    public void onLongPress(MotionEvent e) {
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (Math.abs(velocityY) < 600.0f) {
            return false;
        }
        if (velocityY > 0.0f) {
            startAnimation(0);
        } else {
            startAnimation(1);
        }
        return false;
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.mScrollState = scrollState;
    }

    private boolean showTotalItem() {
        return this.mListView.getLastVisiblePosition() - this.mListView.getFirstVisiblePosition() == this.mListView.getCount() + -1;
    }
}
