package com.huawei.gallery.photoshare.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;

public class PullDownListView extends ListView implements OnScrollListener {
    private RotateAnimation animation;
    private ImageView arrowImageView;
    private int firstItemIndex;
    private int headContentHeight;
    private int headContentWidth;
    private LinearLayout headView;
    private LayoutInflater inflater;
    private boolean isBack;
    private boolean isRecored;
    private long lastUpdateTime;
    private TextView lastUpdatedTextView;
    private Context mContext;
    private boolean mSelectDownEnable = false;
    private SharedPreferences preferences;
    private ProgressBar progressBar;
    public OnRefreshListener refreshListener;
    private RotateAnimation reverseAnimation;
    private int startY;
    private int state = 3;
    private TextView tipsTextview;
    private float xDistance;
    private float xLast;
    private float yDistance;
    private float yLast;

    public interface OnRefreshListener {
        void onRefresh();
    }

    public PullDownListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        if (deltaY + scrollY < 0) {
            deltaY = 0;
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    private void init(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.mContext = context;
        this.headView = (LinearLayout) this.inflater.inflate(R.layout.pulldown_listview_head, null);
        this.arrowImageView = (ImageView) this.headView.findViewById(R.id.head_arrowImageView);
        this.arrowImageView.setMinimumWidth(50);
        this.arrowImageView.setMinimumHeight(50);
        this.progressBar = (ProgressBar) this.headView.findViewById(R.id.head_progressBar);
        this.tipsTextview = (TextView) this.headView.findViewById(R.id.description);
        this.lastUpdatedTextView = (TextView) this.headView.findViewById(R.id.head_lastUpdatedTextView);
        measureView(this.headView);
        this.headContentHeight = this.headView.getMeasuredHeight();
        this.headContentWidth = this.headView.getMeasuredWidth();
        setPadding(0, -5, 0, 0);
        this.headView.setPadding(0, this.headContentHeight * -1, 0, 0);
        this.headView.invalidate();
        GalleryLog.v("size", "width:" + this.headContentWidth + " height:" + this.headContentHeight);
        addHeaderView(this.headView);
        setOnScrollListener(this);
        this.animation = new RotateAnimation(0.0f, -180.0f, 1, 0.5f, 1, 0.5f);
        this.animation.setInterpolator(new LinearInterpolator());
        this.animation.setDuration(250);
        this.animation.setFillAfter(true);
        this.reverseAnimation = new RotateAnimation(-180.0f, 0.0f, 1, 0.5f, 1, 0.5f);
        this.reverseAnimation.setInterpolator(new LinearInterpolator());
        this.reverseAnimation.setDuration(250);
        this.reverseAnimation.setFillAfter(true);
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        refreshUpdatedAtValue();
    }

    public void refreshUpdatedAtValue() {
        this.lastUpdateTime = this.preferences.getLong("updated_at", -1);
        if (this.lastUpdateTime == -1) {
            this.lastUpdatedTextView.setVisibility(8);
            return;
        }
        String updateAtValue = GalleryUtils.getlocalizedDateTime(this.mContext, this.lastUpdateTime);
        this.lastUpdatedTextView.setVisibility(0);
        this.lastUpdatedTextView.setText(String.format(this.mContext.getString(R.string.lastUpdate_tip), new Object[]{updateAtValue}));
    }

    public void onScroll(AbsListView arg0, int firstVisiableItem, int arg2, int arg3) {
        this.firstItemIndex = firstVisiableItem;
    }

    public void onScrollStateChanged(AbsListView arg0, int arg1) {
        GalleryLog.i("PullDownListView", "onScrollStateChanged:" + arg1);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mSelectDownEnable) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case 0:
                if (this.firstItemIndex == 0 && !this.isRecored) {
                    this.startY = (int) event.getY();
                    this.isRecored = true;
                    GalleryLog.v("PullDownListView", "在down时候记录当前位置");
                    break;
                }
            case 1:
            case 3:
                GalleryLog.v("PullDownListView", "MotionEvent " + event.getAction());
                if (this.state != 2) {
                    if (this.state == 1) {
                        this.state = 3;
                        changeHeaderViewByState();
                        GalleryLog.v("PullDownListView", "由下拉刷新状态，到done状态");
                    }
                    if (this.state == 0) {
                        this.state = 2;
                        changeHeaderViewByState();
                        onRefresh();
                        GalleryLog.v("PullDownListView", "由松开刷新状态，到done状态");
                    }
                }
                this.isRecored = false;
                this.isBack = false;
                break;
            case 2:
                int tempY = (int) event.getY();
                if (!this.isRecored && this.firstItemIndex == 0) {
                    GalleryLog.v("PullDownListView", "在move时候记录下位置");
                    this.isRecored = true;
                    this.startY = tempY;
                }
                if (this.state != 2 && this.isRecored) {
                    GalleryLog.v("PullDownListView", "state:" + this.state);
                    if (this.state == 0) {
                        if (this.headView.getHeight() + this.headView.getTop() < this.headContentHeight && tempY - this.startY > 0) {
                            this.state = 1;
                            changeHeaderViewByState();
                            GalleryLog.v("PullDownListView", "由松开刷新状态转变到下拉刷新状态");
                        } else if (tempY - this.startY <= 0) {
                            this.state = 3;
                            changeHeaderViewByState();
                            GalleryLog.v("PullDownListView", "由松开刷新状态转变到done状态");
                        }
                    }
                    if (this.state == 1) {
                        if (this.headView.getHeight() + this.headView.getTop() >= this.headContentHeight) {
                            this.state = 0;
                            this.isBack = true;
                            changeHeaderViewByState();
                            GalleryLog.v("PullDownListView", "由done或者下拉刷新状态转变到松开刷新");
                        } else if (tempY - this.startY <= 0) {
                            this.state = 3;
                            changeHeaderViewByState();
                            GalleryLog.v("PullDownListView", "由DOne或者下拉刷新状态转变到done状态");
                        }
                    }
                    if (this.state == 3 && tempY - this.startY > 0) {
                        this.state = 1;
                        changeHeaderViewByState();
                    }
                    if (this.state == 1) {
                        this.headView.setPadding(0, (this.headContentHeight * -1) + ((int) (((float) (tempY - this.startY)) * 0.55f)), 0, 0);
                        this.headView.invalidate();
                    }
                    if (this.state == 0) {
                        this.headView.setPadding(0, ((int) (((float) (tempY - this.startY)) * 0.55f)) - this.headContentHeight, 0, 0);
                        this.headView.invalidate();
                        break;
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void changeHeaderViewByState() {
        GalleryLog.i("PullDownListView", "changeHeaderViewByState state:" + this.state);
        switch (this.state) {
            case 0:
                this.arrowImageView.setVisibility(0);
                this.progressBar.setVisibility(8);
                this.tipsTextview.setVisibility(0);
                this.arrowImageView.clearAnimation();
                this.arrowImageView.startAnimation(this.animation);
                this.tipsTextview.setText(R.string.app_list_header_release_to_refresh);
                GalleryLog.v("PullDownListView", "当前状态，松开刷新");
                return;
            case 1:
                this.progressBar.setVisibility(8);
                this.tipsTextview.setVisibility(0);
                this.arrowImageView.clearAnimation();
                this.arrowImageView.setVisibility(0);
                if (this.isBack) {
                    this.isBack = false;
                    this.arrowImageView.clearAnimation();
                    this.arrowImageView.startAnimation(this.reverseAnimation);
                    this.tipsTextview.setText(R.string.pull_to_refresh);
                } else {
                    refreshUpdatedAtValue();
                    this.tipsTextview.setText(R.string.pull_to_refresh);
                }
                GalleryLog.v("PullDownListView", "当前状态，下拉刷新");
                return;
            case 2:
                this.headView.setPadding(0, 0, 0, 0);
                this.headView.invalidate();
                this.progressBar.setVisibility(0);
                this.arrowImageView.clearAnimation();
                this.arrowImageView.setVisibility(8);
                this.tipsTextview.setText(R.string.refreshing);
                GalleryLog.v("PullDownListView", "当前状态,正在刷新...");
                return;
            case 3:
                this.headView.setPadding(0, this.headContentHeight * -1, 0, 0);
                this.headView.invalidate();
                if (this.headView.getHeight() + this.headView.getTop() > 0) {
                    GalleryLog.i("PullDownListView", "headView.getHeight():" + this.headView.getHeight() + " headView.getTop()" + this.headView.getTop() + "回到顶部");
                    setSelection(0);
                }
                this.progressBar.setVisibility(8);
                this.arrowImageView.clearAnimation();
                this.tipsTextview.setText(R.string.pull_to_refresh);
                GalleryLog.v("PullDownListView", "当前状态，done");
                return;
            default:
                return;
        }
    }

    public void setonRefreshListener(OnRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }

    private void onRefresh() {
        if (this.refreshListener != null) {
            this.refreshListener.onRefresh();
        }
    }

    private void measureView(View child) {
        int childHeightSpec;
        LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new LayoutParams(-1, -2);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int lpHeight = p.height;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, 1073741824);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, 0);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case 0:
                this.yDistance = 0.0f;
                this.xDistance = 0.0f;
                this.xLast = ev.getX();
                this.yLast = ev.getY();
                break;
            case 2:
                float curX = ev.getX();
                float curY = ev.getY();
                this.xDistance += Math.abs(curX - this.xLast);
                this.yDistance += Math.abs(curY - this.yLast);
                this.xLast = curX;
                this.yLast = curY;
                if (this.xDistance > this.yDistance) {
                    return false;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
