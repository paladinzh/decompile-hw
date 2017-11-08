package com.android.contacts.hap.widget;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Scroller;
import android.widget.TextView;
import com.amap.api.maps.model.WeightedLatLng;
import com.android.contacts.ContactDpiAdapter;
import com.android.contacts.activities.ContactInfoFragment;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.calllog.CallLogDetailFragment;
import com.android.contacts.detail.ContactDetailFragment;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.statistical.StatisticalHelper;
import com.google.android.gms.R;
import huawei.android.widget.SubTabWidget;

public class MultiShrinkScroller extends RelativeLayout {
    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return ((((t * t) * t) * t) * t) + 1.0f;
        }
    };
    private TextView companyView;
    private int[] locContent;
    private GradientDrawable mActionBarGradientDrawable;
    private View mActionBarGradientView;
    private int mActionBarSize;
    private int mActivePointerId;
    ValueAnimator mAniDown;
    private View mBackImage;
    private CallLogDetailFragment mCallLogDetailFragment;
    ListView mCallLogList;
    private final ColorMatrix mColorMatrix;
    private ContactDetailFragment mContactDetailFragment;
    private View mContentContainer;
    private Context mContext;
    private float mCurrentMarginLevel;
    private float mDeltaDetailHeight;
    private float mDeltaHeader;
    ListView mDetailList;
    private float mDetailScreen;
    private float mFirstLevelMarginTop;
    private ContactInfoFragment mFragment;
    private final int[] mGradientColors;
    private boolean mHasBigPhoto;
    private boolean mHasSmallPhoto;
    private float mHeadMaxHeight;
    private float mHeadMinHeight;
    private View mHeaderContainer;
    private boolean mIsBeingDragged;
    private float[] mLastEventPosition;
    private int mLastMotionX;
    private int mLastMotionY;
    private float mLastY;
    private float mLimitContentContainerHight;
    private float mMaxContentContainerHight;
    private final int mMaximumVelocity;
    private float mMinContentContainerHight;
    private final int mMinimumVelocity;
    private float mNameContainerMarginStart;
    private float mQRCodeSize;
    private int mScreenHeight;
    private final Scroller mScroller;
    private float mSecondLevelMarginTop;
    private MySimpleOnGestureListener mSimpleGestureListener;
    private float mSplitBarHeight;
    private int mStatusBarHeight;
    private SubTabWidget mSubTabWidget;
    private float mThirdLevelMarginTop;
    private GradientDrawable mTitleGradientDrawable;
    private View mTitleGradientView;
    private final int mTouchSlop;
    private VelocityTracker mVelocityTracker;
    private ViewPager mViewPager;
    private View nameContainer;
    private ImageView photoContainer;
    private TextView professionView;
    private View smallPhotoPress;
    private ImageView smallPhotoView;

    private class MySimpleOnGestureListener extends SimpleOnGestureListener {
        private MySimpleOnGestureListener() {
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
            if (e2 == null || MultiShrinkScroller.this.mContentContainer == null) {
                return false;
            }
            float delta = e2.getY() - MultiShrinkScroller.this.mLastY;
            MultiShrinkScroller.this.mLastY = e2.getY();
            float containerHeight = MultiShrinkScroller.this.mMaxContentContainerHight - (MultiShrinkScroller.this.mContentContainer.getTranslationY() + delta);
            if (containerHeight > MultiShrinkScroller.this.mMaxContentContainerHight) {
                containerHeight = MultiShrinkScroller.this.mMaxContentContainerHight;
            } else if (containerHeight < MultiShrinkScroller.this.mLimitContentContainerHight) {
                containerHeight = MultiShrinkScroller.this.mLimitContentContainerHight;
            }
            MultiShrinkScroller.this.updateDetailPosition((float) ((int) containerHeight));
            return false;
        }

        public void onLongPress(MotionEvent e) {
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }
    }

    private class UpdateAnimation implements AnimatorUpdateListener {
        private UpdateAnimation() {
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            Object updateValue = animation.getAnimatedValue();
            if (updateValue instanceof Float) {
                MultiShrinkScroller.this.updateDetailPosition(((Float) updateValue).floatValue());
            }
        }
    }

    public MultiShrinkScroller(Context context) {
        this(context, null);
    }

    public MultiShrinkScroller(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiShrinkScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mFragment = null;
        this.mSimpleGestureListener = null;
        this.mLastEventPosition = new float[]{0.0f, 0.0f};
        this.mIsBeingDragged = false;
        this.mLastY = 0.0f;
        this.mLastMotionY = 0;
        this.mLastMotionX = 0;
        this.mActivePointerId = -1;
        this.mGradientColors = new int[]{0, Integer.MIN_VALUE};
        this.mTitleGradientDrawable = new GradientDrawable(Orientation.TOP_BOTTOM, this.mGradientColors);
        this.mActionBarGradientDrawable = new GradientDrawable(Orientation.BOTTOM_TOP, this.mGradientColors);
        this.mColorMatrix = new ColorMatrix();
        ViewConfiguration configuration = ViewConfiguration.get(context);
        setFocusable(false);
        this.mScroller = new Scroller(context, sInterpolator);
        this.mContext = context;
        this.mTouchSlop = configuration.getScaledTouchSlop();
        this.mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    private static void doOnPreDraw(final View view, final boolean drawNextFrame, final Runnable runnable) {
        view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                runnable.run();
                return drawNextFrame;
            }
        });
    }

    public void initValue(float headMinHeight) {
        this.mHeadMinHeight = headMinHeight;
        this.mHeadMaxHeight = getResources().getDimension(R.dimen.detail_header_max_height_dpi);
        this.mQRCodeSize = getResources().getDimension(R.dimen.contact_detail_qrcode_width);
        this.mThirdLevelMarginTop = getResources().getDimension(R.dimen.detail_header_third_level_margin_top);
        this.mSecondLevelMarginTop = getResources().getDimension(R.dimen.detail_header_second_level_margin_top);
        this.mFirstLevelMarginTop = getResources().getDimension(R.dimen.detail_header_first_level_margin_top);
        this.mNameContainerMarginStart = getResources().getDimension(R.dimen.detail_header_cmp_margin_start);
        this.mCurrentMarginLevel = this.mFirstLevelMarginTop;
        this.mActionBarSize = ContactDpiAdapter.getActionbarHeight(getContext());
        this.mDeltaHeader = this.mHeadMaxHeight - this.mHeadMinHeight;
        this.mDeltaDetailHeight = this.mQRCodeSize + this.mDeltaHeader;
        this.mStatusBarHeight = ContactDpiAdapter.getStatusBarHeight(getContext());
        this.mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        this.mSplitBarHeight = getResources().getDimension(R.dimen.contact_split_bar_height);
        this.mDetailScreen = ((float) (this.mScreenHeight - this.mStatusBarHeight)) - this.mSplitBarHeight;
        this.mMaxContentContainerHight = this.mDetailScreen - this.mHeadMinHeight;
        this.mMinContentContainerHight = this.mDetailScreen - this.mHeadMaxHeight;
        this.mLimitContentContainerHight = this.mMinContentContainerHight - this.mQRCodeSize;
    }

    public void initView() {
        this.mSubTabWidget = (SubTabWidget) findViewById(R.id.subTab_layout);
        this.mViewPager = (ViewPager) findViewById(R.id.detail_tab_pager);
        this.mContentContainer = findViewById(R.id.detail_content_container);
        this.mHeaderContainer = findViewById(R.id.detail_header_container);
        this.nameContainer = findViewById(R.id.name_container);
        this.mBackImage = findViewById(R.id.backImg);
        this.photoContainer = (ImageView) findViewById(R.id.static_photo_container);
        this.smallPhotoView = (ImageView) findViewById(R.id.small_photo);
        this.smallPhotoPress = findViewById(R.id.small_photo_press);
        this.companyView = (TextView) findViewById(R.id.company);
        this.professionView = (TextView) findViewById(R.id.profession);
        this.mTitleGradientView = findViewById(R.id.title_gradient);
        this.mTitleGradientView.setBackground(this.mTitleGradientDrawable);
        this.mActionBarGradientView = findViewById(R.id.action_bar_gradient);
        this.mActionBarGradientView.setBackground(this.mActionBarGradientDrawable);
        this.mSimpleGestureListener = new MySimpleOnGestureListener();
        if (this.mContentContainer != null) {
            this.mContentContainer.setTranslationY(this.mDeltaHeader);
        }
        setDetailHeightParams(this.mDetailScreen);
        this.mAniDown = ValueAnimator.ofFloat(new float[]{0.0f, this.mQRCodeSize});
        this.mAniDown.setInterpolator(new DecelerateInterpolator());
        this.mAniDown.addUpdateListener(new UpdateAnimation());
        doOnPreDraw(this.photoContainer, false, new Runnable() {
            public void run() {
                if (MultiShrinkScroller.this.getLayoutDirection() == 1) {
                    MultiShrinkScroller.this.nameContainer.setPivotX((float) MultiShrinkScroller.this.nameContainer.getWidth());
                } else {
                    MultiShrinkScroller.this.nameContainer.setPivotX(0.0f);
                }
                MultiShrinkScroller.this.configureGradientViewHeights();
            }
        });
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        return super.drawChild(canvas, child, drawingTime);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    private void configureGradientViewHeights() {
        LayoutParams actionBarGradientLayoutParams = (LayoutParams) this.mActionBarGradientView.getLayoutParams();
        actionBarGradientLayoutParams.height = this.mActionBarSize * 2;
        this.mActionBarGradientView.setLayoutParams(actionBarGradientLayoutParams);
        LayoutParams titleGradientLayoutParams = (LayoutParams) this.mTitleGradientView.getLayoutParams();
        titleGradientLayoutParams.height = this.mActionBarSize * 2;
        this.mTitleGradientView.setLayoutParams(titleGradientLayoutParams);
    }

    private int getCurrentTab() {
        return this.mSubTabWidget.getSelectedSubTabPostion();
    }

    public void setFragment(ContactInfoFragment fragment) {
        this.mFragment = fragment;
    }

    public void initDetailView(ContactDetailFragment contactDetailFragment) {
        this.mContactDetailFragment = contactDetailFragment;
        this.mDetailList = this.mContactDetailFragment.getContactDetailList();
    }

    public void initCallLogView(CallLogDetailFragment callLogDetailFragment) {
        this.mCallLogDetailFragment = callLogDetailFragment;
        this.mCallLogList = this.mCallLogDetailFragment.getCallLogList();
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (EmuiFeatureManager.isDetailHeaderAnimationFeatureEnable(this.mContext)) {
            return onInterceptTouchEventTotal(event);
        }
        return super.onInterceptTouchEvent(event);
    }

    private void disableDetailHapViewPagerScroll(boolean disable) {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && (this.mViewPager instanceof HapViewPager)) {
            ((HapViewPager) this.mViewPager).disableViewPagerScroll(disable);
        }
    }

    private boolean isTouchInContent(float aPosY) {
        if (!CommonUtilMethods.calcIfNeedSplitScreen()) {
            return false;
        }
        boolean isIn;
        int posY = ((int) aPosY) + getLocationY();
        if (this.mContentContainer != null) {
            int[] location = new int[2];
            this.mContentContainer.getLocationOnScreen(location);
            isIn = posY < location[1] + this.mContentContainer.getHeight() && posY > location[1];
        } else {
            isIn = false;
        }
        return isIn;
    }

    private void disableHapViewPagerSlide() {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && this.mFragment != null && (this.mFragment.getActivity() instanceof PeopleActivity)) {
            PeopleActivity activity = (PeopleActivity) this.mFragment.getActivity();
            ViewPager page = activity.getViewPager();
            if ((page instanceof HapViewPager) && activity.isInMainTabPage()) {
                ((HapViewPager) page).disableViewPagerSlide(true);
            }
        }
    }

    private boolean processDragDown(boolean isDragDown) {
        if (!isDragDown) {
            return true;
        }
        if ((getCurrentTab() == 0 && this.mDetailList != null && this.mDetailList.canScrollVertically(-1)) || (getCurrentTab() == 1 && this.mCallLogList != null && this.mCallLogList.canScrollVertically(-1))) {
            return false;
        }
        if (getCurrentTab() == 0 || this.mCallLogList == null || !this.mCallLogList.canScrollVertically(-1)) {
            return getCurrentTab() == 0 || this.mDetailList == null || !this.mDetailList.canScrollVertically(-1);
        } else {
            return false;
        }
    }

    private boolean onInterceptTouchEventTotal(MotionEvent event) {
        if (this.mFragment != null && this.mFragment.isUnKnownNumberCall()) {
            return super.onInterceptTouchEvent(event);
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(event);
        int action = event.getAction();
        if (this.mContentContainer == null) {
            return false;
        }
        boolean markDragged;
        int y;
        switch (action & 255) {
            case 0:
                y = (int) event.getY();
                this.mLastY = (float) y;
                this.mLastMotionY = y;
                this.mLastMotionX = (int) event.getX();
                this.mActivePointerId = event.getPointerId(0);
                updateLastEventPosition(event);
                if (isTouchInHeader((float) y)) {
                    disableDetailHapViewPagerScroll(true);
                } else {
                    disableDetailHapViewPagerScroll(false);
                }
                if (isTouchInContent((float) y)) {
                    disableHapViewPagerSlide();
                }
                return false;
            case 1:
            case 3:
                markDragged = false;
                this.mActivePointerId = -1;
                if (this.mIsBeingDragged) {
                    return true;
                }
                break;
            case 2:
                if (this.mIsBeingDragged) {
                    return true;
                }
                if (!this.mScroller.isFinished()) {
                    return true;
                }
                int activePointerId = this.mActivePointerId;
                markDragged = false;
                if (activePointerId != -1) {
                    int pointerIndex = event.findPointerIndex(activePointerId);
                    if (pointerIndex != -1) {
                        y = (int) event.getY(pointerIndex);
                        int x = (int) event.getX(pointerIndex);
                        int yDiff = Math.abs(y - this.mLastMotionY);
                        int xDiff = Math.abs(x - this.mLastMotionX);
                        boolean isDragDown = y - this.mLastMotionY > 0;
                        if (yDiff > this.mTouchSlop && yDiff > xDiff) {
                            this.mLastMotionY = y;
                            this.mLastMotionX = x;
                            if (equalFloat(this.mContentContainer.getTranslationY(), this.mDeltaHeader)) {
                                if (!processDragDown(isDragDown)) {
                                    return false;
                                }
                                if (this.mSimpleGestureListener != null) {
                                    this.mSimpleGestureListener.onScroll(null, event, 0.0f, 0.0f);
                                    return true;
                                }
                            } else if (this.mContentContainer.getTranslationY() == 0.0f) {
                                if (isDragDown) {
                                    if (!processDragDown(isDragDown)) {
                                        return false;
                                    }
                                    if (this.mSimpleGestureListener != null) {
                                        this.mSimpleGestureListener.onScroll(null, event, 0.0f, 0.0f);
                                        return true;
                                    }
                                }
                            } else if (equalFloat(this.mContentContainer.getTranslationY(), this.mDeltaDetailHeight)) {
                                if (!(isDragDown || this.mSimpleGestureListener == null)) {
                                    this.mSimpleGestureListener.onScroll(null, event, 0.0f, 0.0f);
                                    return true;
                                }
                            } else if (this.mContentContainer.getTranslationY() <= this.mDeltaHeader || this.mContentContainer.getTranslationY() >= this.mDeltaDetailHeight) {
                                if (this.mContentContainer.getTranslationY() > 0.0f && this.mContentContainer.getTranslationY() < this.mDeltaHeader && this.mSimpleGestureListener != null) {
                                    this.mSimpleGestureListener.onScroll(null, event, 0.0f, 0.0f);
                                    return true;
                                }
                            } else if (this.mSimpleGestureListener != null) {
                                this.mSimpleGestureListener.onScroll(null, event, 0.0f, 0.0f);
                                return true;
                            }
                        }
                    }
                }
                break;
            default:
                markDragged = false;
                this.mActivePointerId = -1;
                break;
        }
        if (!markDragged) {
            super.onInterceptTouchEvent(event);
        }
        return markDragged;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (EmuiFeatureManager.isDetailHeaderAnimationFeatureEnable(this.mContext)) {
            return onTouchEventTotal(event);
        }
        return super.onTouchEvent(event);
    }

    private boolean onTouchEventTotal(MotionEvent event) {
        boolean z = true;
        if (this.mFragment != null && this.mFragment.isUnKnownNumberCall()) {
            return super.onTouchEvent(event);
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(event);
        int action = event.getAction();
        switch (action & 255) {
            case 0:
                updateLastEventPosition(event);
                if (isTouchInHeader((float) ((int) event.getY()))) {
                    return true;
                }
                if (isTouchInContent((float) ((int) event.getY()))) {
                    disableHapViewPagerSlide();
                }
                return false;
            case 1:
            case 3:
                if (action != 3) {
                    z = false;
                }
                stopDrag(z);
                break;
            case 2:
                if (this.mIsBeingDragged || !this.mScroller.isFinished()) {
                    return true;
                }
                int activePointerId = this.mActivePointerId;
                if (activePointerId == -1 || event.findPointerIndex(activePointerId) == -1 || this.mSimpleGestureListener == null) {
                    return false;
                }
                this.mSimpleGestureListener.onScroll(null, event, 0.0f, 0.0f);
                return true;
        }
        return false;
    }

    private void updateLastEventPosition(MotionEvent event) {
        this.mLastEventPosition[0] = event.getX();
        this.mLastEventPosition[1] = event.getY();
    }

    private boolean equalFloat(float a, float b) {
        return ((double) Math.abs(a - b)) < 1.0E-7d;
    }

    public void computeScroll() {
        if (this.mScroller.computeScrollOffset()) {
            scrollTo(this.mScroller.getCurrX(), this.mScroller.getCurrY());
            postInvalidate();
        }
        super.computeScroll();
    }

    private void updateDetailPosition(float height) {
        if (this.mContentContainer != null) {
            float transY = this.mMaxContentContainerHight - height;
            if (equalFloat(height, this.mMaxContentContainerHight)) {
                setDetailHeightParams(this.mDetailScreen);
            }
            if (transY > this.mDeltaHeader) {
                transY = this.mDeltaHeader;
            } else if (transY < 0.0f) {
                transY = 0.0f;
            }
            this.mTitleGradientView.setTranslationY(transY - this.mDeltaHeader);
            this.photoContainer.setTranslationY((transY - this.mDeltaHeader) / 2.0f);
            this.mContentContainer.setTranslationY(transY);
            updateNameContainerScaleAndAlpha(transY);
            if (this.mHasBigPhoto) {
                updatePhotoTintAndDropShadow(transY);
            }
        }
    }

    private void updateNameContainerScaleAndAlpha(float transY) {
        if (equalFloat(this.nameContainer.getPivotY(), 0.0f)) {
            this.nameContainer.setPivotY(((float) this.nameContainer.getHeight()) / 2.0f);
        }
        float scale = ((this.mDeltaHeader * 2.0f) + transY) / (this.mDeltaHeader * 3.0f);
        this.nameContainer.setScaleX(scale);
        this.nameContainer.setScaleY(scale);
        float alphaTrans = this.mDeltaHeader / 2.0f;
        if (transY >= this.mDeltaHeader / 2.0f) {
            alphaTrans = transY;
        }
        setViewAlpha(alphaTrans, this.companyView, this.professionView, this.smallPhotoView);
        this.nameContainer.setTranslationY(((transY - this.mDeltaHeader) * (this.mDeltaHeader - this.mCurrentMarginLevel)) / this.mDeltaHeader);
        float transX = 0.0f;
        float transSplitX = 0.0f;
        if (!this.mHasSmallPhoto) {
            transX = ((this.mDeltaHeader - transY) * this.mNameContainerMarginStart) / this.mDeltaHeader;
        }
        if (this.mHasSmallPhoto && this.mBackImage.getVisibility() != 0) {
            transSplitX = ((this.mDeltaHeader - transY) * (0.0f - this.mNameContainerMarginStart)) / this.mDeltaHeader;
        }
        if (getLayoutDirection() == 1) {
            transX = -transX;
            transSplitX = -transSplitX;
        }
        if (this.mBackImage.getVisibility() == 0) {
            this.nameContainer.setTranslationX(transX);
        } else if (this.mHasSmallPhoto && this.mBackImage.getVisibility() != 0) {
            this.nameContainer.setTranslationX(transSplitX);
        }
    }

    private void setViewAlpha(float transY, View... targetView) {
        boolean z = false;
        float alpha = (transY - (this.mDeltaHeader / 2.0f)) / (this.mDeltaHeader / 2.0f);
        View view = this.smallPhotoPress;
        if (((double) alpha) > 0.01d) {
            z = true;
        }
        view.setClickable(z);
        int i = 0;
        while (i < targetView.length && targetView[i] != null) {
            if (targetView[i].getVisibility() == 0) {
                targetView[i].setAlpha(alpha);
            }
            i++;
        }
    }

    private void setDetailHeightParams(float aHeight) {
        if (this.mContentContainer != null) {
            LayoutParams params = (LayoutParams) this.mContentContainer.getLayoutParams();
            int height = (int) (0.5f + aHeight);
            if (params.height != height) {
                params.height = height;
                this.mContentContainer.setLayoutParams(params);
            }
        }
    }

    private boolean isTouchInHeader(float aPosY) {
        int posY = ((int) aPosY) + getLocationY();
        if (this.mHeaderContainer == null) {
            return false;
        }
        int[] location = new int[2];
        this.mHeaderContainer.getLocationOnScreen(location);
        return posY < location[1] + this.mHeaderContainer.getHeight() && posY > location[1];
    }

    private int getLocationY() {
        if (this.locContent == null) {
            this.locContent = new int[2];
            getLocationOnScreen(this.locContent);
        }
        return this.locContent[1];
    }

    private void updatePhotoTintAndDropShadow(float transY) {
        this.photoContainer.clearColorFilter();
        this.mColorMatrix.reset();
        float alpha = 1.0f - ((float) Math.min(Math.pow((double) calculateHeightRatioToBlendingStartHeight(this.mHeadMinHeight + transY), 1.5d) * 2.0d, WeightedLatLng.DEFAULT_INTENSITY));
        this.mColorMatrix.setSaturation(0.0f);
        int gradientAlpha = (int) (255.0f * alpha);
        if (alpha >= 0.9f) {
            this.mTitleGradientDrawable.setAlpha(gradientAlpha);
            this.mActionBarGradientDrawable.setAlpha(gradientAlpha);
        }
    }

    private float calculateHeightRatioToBlendingStartHeight(float height) {
        float intermediateHeight = this.mHeadMaxHeight * 0.5f;
        float interpolatingHeightRange = intermediateHeight - this.mHeadMinHeight;
        if (height > intermediateHeight) {
            return 0.0f;
        }
        return (intermediateHeight - height) / interpolatingHeightRange;
    }

    public void resetAndUpdateViewVisibleState() {
        boolean z = false;
        updateDetailPosition(this.mMaxContentContainerHight - this.mDeltaHeader);
        if (this.smallPhotoView.getVisibility() == 0) {
            z = true;
        }
        this.mHasSmallPhoto = z;
        int visibleViewCount = 1;
        if (this.companyView.getVisibility() == 0) {
            visibleViewCount = 2;
        }
        if (this.professionView.getVisibility() == 0) {
            visibleViewCount++;
        }
        if (visibleViewCount == 3) {
            this.mCurrentMarginLevel = this.mThirdLevelMarginTop;
        } else if (visibleViewCount == 2) {
            this.mCurrentMarginLevel = this.mSecondLevelMarginTop;
        } else {
            this.mCurrentMarginLevel = this.mFirstLevelMarginTop;
        }
    }

    public void setExsitBigPhoto(Boolean bigPhoto) {
        this.mHasBigPhoto = bigPhoto.booleanValue();
    }

    private void stopDrag(boolean cancelled) {
        this.mIsBeingDragged = false;
        StatisticalHelper.report(2040);
        if (!cancelled && getChildCount() > 0) {
            float velocity = getCurrentVelocity();
            if (velocity > ((float) this.mMinimumVelocity) || velocity < ((float) (-this.mMinimumVelocity))) {
                fling(-velocity);
            }
        }
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private float getCurrentVelocity() {
        if (this.mVelocityTracker == null) {
            return 0.0f;
        }
        this.mVelocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
        return this.mVelocityTracker.getYVelocity();
    }

    private void fling(float velocity) {
        if (this.mContentContainer != null && this.mContentContainer.getTranslationY() > 0.0f && this.mContentContainer.getTranslationY() < this.mDeltaHeader) {
            float endY = (this.mMaxContentContainerHight - this.mContentContainer.getTranslationY()) + (velocity / 10.0f);
            this.mAniDown.setFloatValues(new float[]{startY, endY});
            this.mAniDown.setDuration(300);
            this.mAniDown.start();
        }
    }
}
