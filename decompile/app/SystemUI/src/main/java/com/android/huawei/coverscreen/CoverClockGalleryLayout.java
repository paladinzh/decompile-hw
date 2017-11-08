package com.android.huawei.coverscreen;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.android.huawei.coverscreen.VerticalGalleryAdapterView.OnItemClickListener;
import com.android.huawei.coverscreen.VerticalGalleryAdapterView.OnItemSelectedListener;
import com.android.keyguard.R$array;
import com.android.keyguard.R$drawable;
import com.android.keyguard.R$id;
import com.huawei.keyguard.util.MusicUtils;
import fyusion.vislib.BuildConfig;

public class CoverClockGalleryLayout extends RelativeLayout {
    private static final int DEFAULT_BG_DRAWABLE_ID = R$drawable.cover_clock_background;
    private static final String WINDOWS_SIZE = SystemProperties.get("ro.config.small_cover_size", BuildConfig.FLAVOR);
    private static int mClockSize = 560;
    private static int mGalleryHeight = 750;
    private static int mGalleryWidth = 450;
    private static int mTotalHeight = 1060;
    private boolean loadClockBackground;
    private int mAnalogClockNormal;
    private int mAnalogClockNum;
    private int mAnalogClockShrink;
    private boolean mBInShrinkMode;
    private int[] mBackgroudDrawableIdArray;
    private View mBackgroundView;
    private View mBgBackgroundView;
    private View[] mClockArray;
    private View mContainer;
    private CoverDigitalClockLayout mCoverDigitalClockLayout;
    private int mCurClockIndex;
    private BitmapDrawable[] mDrawable;
    Handler mHandler;
    private int mNormalAmLeft;
    private int mNormalAmPmTimeTop;
    private int mNormalHourTimeTop;
    private int mNormalMinionTimeTop;
    private int mNormalTimeLeft;
    private int mShrinkAmLeft;
    private int mShrinkAmPmTimeTop;
    private int mShrinkHourTimeTop;
    private int mShrinkMinionTimeTop;
    private int mShrinkTimeLeft;
    private View mSwitchPanel;
    ThreadAddClockBackground mThread;
    private TimeSubject mTimeSubject;
    private int mTmpClockIndex;
    private int mTotalClockNum;
    private VerticalGallery mVerticalGallery;
    private VerticalGalleryAdatper mVerticalGalleryAdapter;

    private class ThreadAddClockBackground extends Thread {
        private boolean isLoadClockBgComplete;

        private ThreadAddClockBackground() {
            this.isLoadClockBgComplete = true;
        }

        public void setLoadClockBgComplete(boolean on) {
            this.isLoadClockBgComplete = on;
        }

        public boolean getLoadClockBgComplete() {
            return this.isLoadClockBgComplete;
        }

        public void run() {
            if (CoverClockGalleryLayout.this.createAllClockBackground()) {
                CoverClockGalleryLayout.this.mHandler.removeMessages(15);
                CoverClockGalleryLayout.this.mHandler.sendEmptyMessage(15);
            }
        }
    }

    private class VerticalGalleryAdatper extends BaseAdapter {
        private VerticalGalleryAdatper() {
        }

        public int getCount() {
            return CoverClockGalleryLayout.this.mClockArray.length;
        }

        public Object getItem(int position) {
            return Integer.valueOf(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View contentView, ViewGroup parent) {
            return CoverClockGalleryLayout.this.mClockArray[position];
        }
    }

    private void initDigitalClockLayout() {
        if (!BuildConfig.FLAVOR.equals(WINDOWS_SIZE)) {
            this.mNormalAmPmTimeTop = ((LayoutParams) this.mCoverDigitalClockLayout.getAmPmTextView().getLayoutParams()).topMargin;
            this.mNormalHourTimeTop = ((LayoutParams) this.mCoverDigitalClockLayout.getmHourTimeTextView().getLayoutParams()).topMargin;
            this.mNormalMinionTimeTop = ((LayoutParams) this.mCoverDigitalClockLayout.getmMinutesTimeTextView().getLayoutParams()).topMargin;
            this.mNormalTimeLeft = ((LayoutParams) this.mCoverDigitalClockLayout.getmHourTimeTextView().getLayoutParams()).leftMargin;
            this.mNormalAmLeft = ((LayoutParams) this.mCoverDigitalClockLayout.getAmPmTextView().getLayoutParams()).leftMargin;
            this.mShrinkAmPmTimeTop = (int) (((double) this.mNormalAmPmTimeTop) * 0.8d);
            this.mShrinkHourTimeTop = (int) (((double) this.mNormalHourTimeTop) / 0.8d);
            this.mShrinkMinionTimeTop = (int) (((double) this.mNormalMinionTimeTop) / 0.8d);
            this.mShrinkTimeLeft = (int) (((double) this.mNormalTimeLeft) * 0.8d);
            this.mShrinkAmLeft = this.mShrinkTimeLeft + ((int) (((double) (this.mNormalAmLeft - this.mNormalTimeLeft)) / 0.8d));
        }
    }

    public CoverClockGalleryLayout(Context context) {
        this(context, null);
    }

    public CoverClockGalleryLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverClockGalleryLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mBInShrinkMode = false;
        this.mCurClockIndex = 0;
        this.mTmpClockIndex = 0;
        this.mAnalogClockNum = 0;
        this.mTotalClockNum = 0;
        this.mBackgroudDrawableIdArray = null;
        this.mDrawable = null;
        this.mShrinkAmPmTimeTop = 285;
        this.mNormalAmPmTimeTop = 400;
        this.mShrinkMinionTimeTop = -140;
        this.mNormalMinionTimeTop = -105;
        this.mShrinkHourTimeTop = -85;
        this.mNormalHourTimeTop = -54;
        this.mShrinkTimeLeft = 42;
        this.mNormalTimeLeft = 75;
        this.mShrinkAmLeft = 72;
        this.mNormalAmLeft = 93;
        this.loadClockBackground = false;
        this.mAnalogClockShrink = 400;
        this.mAnalogClockNormal = 500;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 14:
                        Log.d("CoverClockGalleryLayout", "MSG_ENTER_SHRINKMODE mTmpClockIndex==" + CoverClockGalleryLayout.this.mTmpClockIndex);
                        CoverClockGalleryLayout.this.setCurClockIndex(CoverClockGalleryLayout.this.mTmpClockIndex);
                        CoverClockGalleryLayout.this.updateBackgroundView(CoverClockGalleryLayout.this.mTmpClockIndex);
                        CoverClockGalleryLayout.this.switchMode(false);
                        return;
                    case 15:
                        CoverClockGalleryLayout.this.createAnalogClockFromResource();
                        CoverClockGalleryLayout.this.initVerticalGallery();
                        CoverClockGalleryLayout.this.loadClockBackground = true;
                        Log.d("CoverClockGalleryLayout", "MSG_LOAD_RESOURCE_COMPLETE");
                        return;
                    case 16:
                        CoverClockGalleryLayout.this.switchMode(true);
                        return;
                    default:
                        return;
                }
            }
        };
        this.mTimeSubject = new TimeSubject(context);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d("CoverClockGalleryLayout", "creatCurrentClockFromResource");
        initCoverArea();
        this.mCurClockIndex = getCurClockIndex();
        this.mTmpClockIndex = this.mCurClockIndex;
        creatCurrentClockFromResource();
        initBackground();
        this.mThread = new ThreadAddClockBackground();
        Log.d("CoverClockGalleryLayout", "creatCurrentClockFromResource ++");
    }

    private void initCoverArea() {
        String[] location = SystemProperties.get("ro.config.huawei_smallwindow").split(",");
        if (location.length == 4) {
            int xStart = Integer.parseInt(location[0]);
            int xEnd = Integer.parseInt(location[2]);
            int yStart = Integer.parseInt(location[1]);
            int yEnd = Integer.parseInt(location[3]);
            if (xEnd - xStart > 0 && yEnd - yStart > 0) {
                mClockSize = xEnd - xStart;
                mTotalHeight = yEnd - yStart;
            }
            mGalleryWidth = (int) (((double) mClockSize) * 0.8d);
            mGalleryHeight = (int) (((double) mTotalHeight) * 0.8d);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isInShrinkMode()) {
            return true;
        }
        if (!(2 == ev.getAction() || ev.getAction() == 0)) {
            if (1 == ev.getAction()) {
            }
            return super.dispatchTouchEvent(ev);
        }
        this.mHandler.removeMessages(14);
        this.mHandler.sendEmptyMessageDelayed(14, 4000);
        return super.dispatchTouchEvent(ev);
    }

    private boolean createAllClockBackground() {
        int idx;
        Log.d("CoverClockGalleryLayout", "createAllClockBackground");
        TypedArray resArray = getResources().obtainTypedArray(CoverResourceUtils.getResIdentifier(getContext(), "cover_analog_clock_arrays", "array", "com.android.systemui", R$array.cover_analog_clock_arrays));
        this.mBackgroudDrawableIdArray[0] = CoverResourceUtils.getResIdentifier(getContext(), "cover_clock_background", "drawable", "com.android.systemui", DEFAULT_BG_DRAWABLE_ID);
        for (idx = 0; idx != resArray.length(); idx += 5) {
            this.mBackgroudDrawableIdArray[(idx / 5) + 1] = resArray.getResourceId(idx, 0);
        }
        resArray.recycle();
        for (idx = 0; idx < this.mTotalClockNum; idx++) {
            if (this.mThread != null && !this.mThread.getLoadClockBgComplete()) {
                return false;
            }
            int clockIndex = idx;
            if (this.mDrawable[clockIndex] == null) {
                Bitmap bmp = null;
                try {
                    bmp = BitmapFactory.decodeResource(getResources(), this.mBackgroudDrawableIdArray[clockIndex]);
                } catch (OutOfMemoryError e) {
                    Log.d("CoverClockGalleryLayout", "createAnalogClockFromResource out of memory");
                }
                if (bmp == null) {
                    Log.d("CoverClockGalleryLayout", "createAnalogClockFromResource bmp = null");
                }
                Bitmap blurBitmap = MusicUtils.getBlurBitmap(getContext(), bmp, 561, 1061);
                if (bmp != null) {
                    bmp.recycle();
                }
                this.mDrawable[clockIndex] = new BitmapDrawable(getResources(), blurBitmap);
            }
        }
        Log.d("CoverClockGalleryLayout", "createAllClockBackground+");
        return true;
    }

    private void creatCurrentClockFromResource() {
        TypedArray resArray = getResources().obtainTypedArray(CoverResourceUtils.getResIdentifier(getContext(), "cover_analog_clock_arrays", "array", "com.android.systemui", R$array.cover_analog_clock_arrays));
        this.mAnalogClockNum = resArray.length() / 5;
        this.mTotalClockNum = this.mAnalogClockNum + 1;
        this.mBackgroudDrawableIdArray = new int[(this.mAnalogClockNum + 1)];
        this.mBackgroudDrawableIdArray[0] = CoverResourceUtils.getResIdentifier(getContext(), "cover_clock_background", "drawable", "com.android.systemui", DEFAULT_BG_DRAWABLE_ID);
        this.mDrawable = new BitmapDrawable[this.mTotalClockNum];
        this.mClockArray = new View[this.mTotalClockNum];
        if (this.mCurClockIndex == 0) {
            this.mClockArray[0] = getDigitalClock();
        } else if (this.mCurClockIndex > 0) {
            int idx = (this.mCurClockIndex - 1) * 5;
            int clockIndex = (idx / 5) + 1;
            this.mBackgroudDrawableIdArray[clockIndex] = resArray.getResourceId(idx, 0);
            this.mClockArray[clockIndex] = new CoverAnalogClockLayout(getContext());
            ((CoverAnalogClockLayout) this.mClockArray[clockIndex]).initClock(resArray.getResourceId(idx + 1, 0), new int[]{resArray.getResourceId(idx + 2, 0), resArray.getResourceId(idx + 3, 0), resArray.getResourceId(idx + 4, 0)});
            this.mTimeSubject.attachTimeChangeObserver((CoverAnalogClockLayout) this.mClockArray[clockIndex]);
        }
        updateLayoutParamsForGallery(this.mCurClockIndex);
        addView(this.mClockArray[this.mCurClockIndex]);
        this.mVerticalGalleryAdapter = new VerticalGalleryAdatper();
        this.mVerticalGallery = new VerticalGallery(getContext());
        LayoutParams lp = new LayoutParams(-2, -1);
        lp.addRule(14);
        this.mVerticalGallery.setLayoutParams(lp);
        this.mVerticalGallery.setAdapter(this.mVerticalGalleryAdapter);
        this.mVerticalGallery.setSpacing(50);
        this.mVerticalGallery.setSelection(this.mCurClockIndex);
        this.mVerticalGallery.setCallbackDuringFling(false);
        this.mVerticalGallery.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(VerticalGalleryAdapterView<?> verticalGalleryAdapterView, View view, int position, long id) {
                Log.d("CoverClockGalleryLayout", "setOnItemSelectedListener onItemSelected" + position);
                CoverClockGalleryLayout.this.mTmpClockIndex = position;
                CoverClockGalleryLayout.this.updateBackgroundView(position);
            }

            public void onNothingSelected(VerticalGalleryAdapterView<?> verticalGalleryAdapterView) {
            }
        });
        this.mVerticalGallery.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(VerticalGalleryAdapterView<?> verticalGalleryAdapterView, View view, int position, long id) {
                if (position != CoverClockGalleryLayout.this.mCurClockIndex) {
                    CoverClockGalleryLayout.this.updateBackgroundView(position);
                    CoverClockGalleryLayout.this.setCurClockIndex(position);
                }
                CoverClockGalleryLayout.this.switchMode(false);
            }
        });
    }

    private void createAnalogClockFromResource() {
        Log.d("CoverClockGalleryLayout", "createAnalogClockFromResource");
        TypedArray resArray = getResources().obtainTypedArray(CoverResourceUtils.getResIdentifier(getContext(), "cover_analog_clock_arrays", "array", "com.android.systemui", R$array.cover_analog_clock_arrays));
        if (this.mClockArray[0] == null) {
            this.mClockArray[0] = getDigitalClock();
        }
        for (int idx = 0; idx != resArray.length(); idx += 5) {
            int clockIndex = (idx / 5) + 1;
            if (this.mClockArray[clockIndex] == null) {
                this.mClockArray[clockIndex] = new CoverAnalogClockLayout(getContext());
                ((CoverAnalogClockLayout) this.mClockArray[clockIndex]).initClock(resArray.getResourceId(idx + 1, 0), new int[]{resArray.getResourceId(idx + 2, 0), resArray.getResourceId(idx + 3, 0), resArray.getResourceId(idx + 4, 0)});
                this.mTimeSubject.attachTimeChangeObserver((CoverAnalogClockLayout) this.mClockArray[clockIndex]);
            }
        }
        resArray.recycle();
        Log.d("CoverClockGalleryLayout", "createAllClockBackground+");
    }

    private void initBackground() {
        this.mBackgroundView = findViewById(R$id.bg_view);
        this.mBgBackgroundView = findViewById(R$id.bg_background_view);
        this.mBgBackgroundView.setVisibility(4);
        this.mBackgroundView.setBackground(this.mDrawable[this.mCurClockIndex]);
        this.mBackgroundView.setVisibility(4);
    }

    public void setSwitchPanelBackground(int position) {
        if (this.mSwitchPanel == null) {
            return;
        }
        if (-1 == position) {
            if (this.mBInShrinkMode) {
                this.mSwitchPanel.setBackground(this.mDrawable[this.mCurClockIndex]);
            } else {
                this.mSwitchPanel.setBackgroundResource(this.mBackgroudDrawableIdArray[this.mCurClockIndex]);
            }
        } else if (this.mBInShrinkMode) {
            this.mSwitchPanel.setBackground(this.mDrawable[position]);
        } else {
            this.mSwitchPanel.setBackgroundResource(this.mBackgroudDrawableIdArray[position]);
        }
    }

    public boolean isInShrinkMode() {
        return this.mBInShrinkMode;
    }

    private int getCurClockIndex() {
        return getContext().getSharedPreferences("cover_clock", 0).getInt("cover_clock_index", 0);
    }

    public void setSwitchPanel(View view) {
        this.mSwitchPanel = view;
    }

    public void setContainer(View view) {
        this.mContainer = view;
    }

    private void setCurClockIndex(int index) {
        this.mCurClockIndex = index;
        Editor editor = getContext().getSharedPreferences("cover_clock", 0).edit();
        editor.putInt("cover_clock_index", index);
        editor.commit();
        setSwitchPanelBackground(-1);
    }

    private void updateLayoutParamsForGallery(int idx) {
        VerticalGallery.LayoutParams layoutParams = new VerticalGallery.LayoutParams(mClockSize, mTotalHeight);
        layoutParams.addRule(14);
        this.mClockArray[idx].setLayoutParams(layoutParams);
    }

    private void initVerticalGallery() {
        for (int idx = 0; idx != this.mTotalClockNum; idx++) {
            updateLayoutParamsForGallery(idx);
        }
        removeView(this.mClockArray[this.mCurClockIndex]);
        addView(this.mVerticalGallery);
    }

    private void updateBackgroundView(final int position) {
        setSwitchPanelBackground(position);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(this.mBackgroundView, "alpha", new float[]{1.0f, 0.0f});
        alphaAnimator.setInterpolator(new DecelerateInterpolator());
        alphaAnimator.setDuration(250);
        alphaAnimator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator arg0) {
            }

            public void onAnimationRepeat(Animator arg0) {
            }

            public void onAnimationEnd(Animator arg0) {
                CoverClockGalleryLayout.this.mBackgroundView.setBackground(CoverClockGalleryLayout.this.mDrawable[position]);
            }

            public void onAnimationCancel(Animator arg0) {
            }
        });
        alphaAnimator.start();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mThread != null) {
            this.mThread.start();
        }
        this.mTimeSubject.registerBroadcastReceiverAndContentObserver();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mTimeSubject.unregisterBroadcastReceiverAndContentObserver();
        if (this.mThread != null) {
            this.mThread.setLoadClockBgComplete(false);
            try {
                this.mThread.join();
            } catch (InterruptedException e) {
                Log.d("CoverClockGalleryLayout", "onDetachedFromWindow");
                e.printStackTrace();
            }
            this.mThread = null;
        }
        int id = 0;
        while (id < this.mTotalClockNum) {
            if (!(this.mDrawable == null || this.mDrawable[id] == null || this.mDrawable[id].getBitmap() == null)) {
                this.mDrawable[id].getBitmap().recycle();
                this.mDrawable[id] = null;
            }
            id++;
        }
        this.mHandler.removeCallbacksAndMessages(null);
    }

    private View getDigitalClock() {
        if (this.mCoverDigitalClockLayout == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            int digitalClockLayoutId = getContext().getResources().getIdentifier("com.android.systemui:layout/cover_digital_clock_layout" + WINDOWS_SIZE, null, null);
            if (digitalClockLayoutId <= 0) {
                digitalClockLayoutId = getContext().getResources().getIdentifier("com.android.systemui:layout/cover_digital_clock_layout", null, null);
            }
            this.mCoverDigitalClockLayout = (CoverDigitalClockLayout) inflater.inflate(digitalClockLayoutId, null);
            initDigitalClockLayout();
            this.mTimeSubject.attachTimeChangeObserver(this.mCoverDigitalClockLayout);
        }
        return this.mCoverDigitalClockLayout;
    }

    public void enlargeDirectly() {
        Log.d("CoverClockGalleryLayout", "enlargeDirectly mBInShrinkMode =" + this.mBInShrinkMode);
        setCurClockIndex(this.mTmpClockIndex);
        updateBackgroundView(this.mTmpClockIndex);
        switchMode(false);
    }

    public void switchMode(boolean isShrink) {
        if (isShrink) {
            this.mHandler.sendEmptyMessageDelayed(14, 4000);
        } else {
            this.mHandler.removeMessages(14);
        }
        if (this.mBInShrinkMode != isShrink) {
            if (this.loadClockBackground || !isShrink) {
                ValueAnimator widthAnimator;
                ValueAnimator heightAnimator;
                ValueAnimator widthAnimatorAnalog;
                ObjectAnimator animateX;
                ObjectAnimator animateY;
                ObjectAnimator animateX1;
                ObjectAnimator animateY1;
                ValueAnimator layoutAnimator;
                ValueAnimator layoutAnimator1;
                ValueAnimator layoutAnimator2;
                ValueAnimator layoutAnimatorX;
                ValueAnimator layoutAnimatorAm;
                this.mBInShrinkMode = isShrink;
                if (this.mBInShrinkMode) {
                    this.mBackgroundView.setVisibility(0);
                    this.mBgBackgroundView.setVisibility(0);
                } else {
                    this.mBackgroundView.setVisibility(4);
                    this.mBgBackgroundView.setVisibility(4);
                }
                AnimatorSet as = new AnimatorSet();
                setContainerVisibility(isShrink);
                float[] fArr;
                int idx;
                if (isShrink) {
                    widthAnimator = ValueAnimator.ofInt(new int[]{mClockSize, mGalleryWidth});
                    heightAnimator = ValueAnimator.ofInt(new int[]{mTotalHeight, mGalleryHeight});
                    widthAnimatorAnalog = ValueAnimator.ofInt(new int[]{this.mAnalogClockNormal, this.mAnalogClockShrink});
                    this.mCoverDigitalClockLayout.hideDateTextView(true);
                    this.mCoverDigitalClockLayout.setBackgroundResource(this.mBackgroudDrawableIdArray[0]);
                    fArr = new float[2];
                    animateX = ObjectAnimator.ofFloat(this.mCoverDigitalClockLayout.getmHourTimeTextView(), "scaleX", new float[]{1.0f, 0.8f});
                    fArr = new float[2];
                    animateY = ObjectAnimator.ofFloat(this.mCoverDigitalClockLayout.getmHourTimeTextView(), "scaleY", new float[]{1.0f, 0.8f});
                    fArr = new float[2];
                    animateX1 = ObjectAnimator.ofFloat(this.mCoverDigitalClockLayout.getmMinutesTimeTextView(), "scaleX", new float[]{1.0f, 0.8f});
                    fArr = new float[2];
                    animateY1 = ObjectAnimator.ofFloat(this.mCoverDigitalClockLayout.getmMinutesTimeTextView(), "scaleY", new float[]{1.0f, 0.8f});
                    for (idx = 1; idx != this.mTotalClockNum; idx++) {
                        ((CoverAnalogClockLayout) this.mClockArray[idx]).hideDateTextView(true);
                        this.mClockArray[idx].setBackgroundResource(this.mBackgroudDrawableIdArray[idx]);
                    }
                    layoutAnimator = ValueAnimator.ofInt(new int[]{this.mNormalAmPmTimeTop, this.mShrinkAmPmTimeTop});
                    layoutAnimator1 = ValueAnimator.ofInt(new int[]{this.mNormalHourTimeTop, this.mShrinkHourTimeTop});
                    layoutAnimator2 = ValueAnimator.ofInt(new int[]{this.mNormalMinionTimeTop, this.mShrinkMinionTimeTop});
                    layoutAnimatorX = ValueAnimator.ofInt(new int[]{this.mNormalTimeLeft, this.mShrinkTimeLeft});
                    layoutAnimatorAm = ValueAnimator.ofInt(new int[]{this.mNormalAmLeft, this.mShrinkAmLeft});
                    this.mSwitchPanel.setBackground(this.mDrawable[this.mCurClockIndex]);
                    widthAnimator.removeAllListeners();
                } else {
                    widthAnimator = ValueAnimator.ofInt(new int[]{mGalleryWidth, mClockSize});
                    heightAnimator = ValueAnimator.ofInt(new int[]{mGalleryHeight, mTotalHeight});
                    widthAnimatorAnalog = ValueAnimator.ofInt(new int[]{this.mAnalogClockShrink, this.mAnalogClockNormal});
                    this.mCoverDigitalClockLayout.hideDateTextView(false);
                    fArr = new float[2];
                    animateX = ObjectAnimator.ofFloat(this.mCoverDigitalClockLayout.getmHourTimeTextView(), "scaleX", new float[]{0.8f, 1.0f});
                    fArr = new float[2];
                    animateY = ObjectAnimator.ofFloat(this.mCoverDigitalClockLayout.getmHourTimeTextView(), "scaleY", new float[]{0.8f, 1.0f});
                    fArr = new float[2];
                    animateX1 = ObjectAnimator.ofFloat(this.mCoverDigitalClockLayout.getmMinutesTimeTextView(), "scaleX", new float[]{0.8f, 1.0f});
                    fArr = new float[2];
                    animateY1 = ObjectAnimator.ofFloat(this.mCoverDigitalClockLayout.getmMinutesTimeTextView(), "scaleY", new float[]{0.8f, 1.0f});
                    for (idx = 1; idx != this.mTotalClockNum; idx++) {
                        ((CoverAnalogClockLayout) this.mClockArray[idx]).hideDateTextView(false);
                    }
                    layoutAnimator = ValueAnimator.ofInt(new int[]{this.mShrinkAmPmTimeTop, this.mNormalAmPmTimeTop});
                    layoutAnimator1 = ValueAnimator.ofInt(new int[]{this.mShrinkHourTimeTop, this.mNormalHourTimeTop});
                    layoutAnimator2 = ValueAnimator.ofInt(new int[]{this.mShrinkMinionTimeTop, this.mNormalMinionTimeTop});
                    layoutAnimatorX = ValueAnimator.ofInt(new int[]{this.mShrinkTimeLeft, this.mNormalTimeLeft});
                    layoutAnimatorAm = ValueAnimator.ofInt(new int[]{this.mShrinkAmLeft, this.mNormalAmLeft});
                    widthAnimator.addListener(new AnimatorListener() {
                        public void onAnimationStart(Animator arg0) {
                        }

                        public void onAnimationRepeat(Animator arg0) {
                        }

                        public void onAnimationEnd(Animator arg0) {
                            CoverClockGalleryLayout.this.mCoverDigitalClockLayout.setBackgroundResource(0);
                            for (int idx = 1; idx != CoverClockGalleryLayout.this.mTotalClockNum; idx++) {
                                CoverClockGalleryLayout.this.mClockArray[idx].setBackgroundResource(0);
                            }
                            CoverClockGalleryLayout.this.mSwitchPanel.setBackgroundResource(CoverClockGalleryLayout.this.mBackgroudDrawableIdArray[CoverClockGalleryLayout.this.mCurClockIndex]);
                        }

                        public void onAnimationCancel(Animator arg0) {
                        }
                    });
                }
                widthAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                        for (int idx = 0; idx != CoverClockGalleryLayout.this.mTotalClockNum; idx++) {
                            VerticalGallery.LayoutParams layoutParams = (VerticalGallery.LayoutParams) CoverClockGalleryLayout.this.mClockArray[idx].getLayoutParams();
                            layoutParams.width = val;
                            CoverClockGalleryLayout.this.mClockArray[idx].setLayoutParams(layoutParams);
                        }
                    }
                });
                heightAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                        for (int idx = 0; idx != CoverClockGalleryLayout.this.mTotalClockNum; idx++) {
                            VerticalGallery.LayoutParams layoutParams = (VerticalGallery.LayoutParams) CoverClockGalleryLayout.this.mClockArray[idx].getLayoutParams();
                            layoutParams.height = val;
                            CoverClockGalleryLayout.this.mClockArray[idx].setLayoutParams(layoutParams);
                        }
                    }
                });
                layoutAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        LayoutParams mAmPmLayoutParams = (LayoutParams) CoverClockGalleryLayout.this.mCoverDigitalClockLayout.getAmPmTextView().getLayoutParams();
                        mAmPmLayoutParams.topMargin = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                        CoverClockGalleryLayout.this.mCoverDigitalClockLayout.getAmPmTextView().setLayoutParams(mAmPmLayoutParams);
                    }
                });
                layoutAnimator1.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        LayoutParams mHourTimeLayoutParams = (LayoutParams) CoverClockGalleryLayout.this.mCoverDigitalClockLayout.getmHourTimeTextView().getLayoutParams();
                        mHourTimeLayoutParams.topMargin = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                        CoverClockGalleryLayout.this.mCoverDigitalClockLayout.getmHourTimeTextView().setLayoutParams(mHourTimeLayoutParams);
                    }
                });
                layoutAnimator2.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        LayoutParams mMinutesTimeLayoutParams = (LayoutParams) CoverClockGalleryLayout.this.mCoverDigitalClockLayout.getmMinutesTimeTextView().getLayoutParams();
                        mMinutesTimeLayoutParams.topMargin = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                        CoverClockGalleryLayout.this.mCoverDigitalClockLayout.getmMinutesTimeTextView().setLayoutParams(mMinutesTimeLayoutParams);
                    }
                });
                layoutAnimatorX.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        int val = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                        LayoutParams mMinutesTimeLayoutParams = (LayoutParams) CoverClockGalleryLayout.this.mCoverDigitalClockLayout.getmMinutesTimeTextView().getLayoutParams();
                        mMinutesTimeLayoutParams.leftMargin = val;
                        CoverClockGalleryLayout.this.mCoverDigitalClockLayout.getmMinutesTimeTextView().setLayoutParams(mMinutesTimeLayoutParams);
                        LayoutParams mHourTimeLayoutParams = (LayoutParams) CoverClockGalleryLayout.this.mCoverDigitalClockLayout.getmHourTimeTextView().getLayoutParams();
                        mHourTimeLayoutParams.leftMargin = val;
                        CoverClockGalleryLayout.this.mCoverDigitalClockLayout.getmHourTimeTextView().setLayoutParams(mHourTimeLayoutParams);
                    }
                });
                layoutAnimatorAm.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        LayoutParams mAmPmLayoutParams = (LayoutParams) CoverClockGalleryLayout.this.mCoverDigitalClockLayout.getAmPmTextView().getLayoutParams();
                        mAmPmLayoutParams.leftMargin = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                        CoverClockGalleryLayout.this.mCoverDigitalClockLayout.getAmPmTextView().setLayoutParams(mAmPmLayoutParams);
                    }
                });
                if ("_1010x678".equals(WINDOWS_SIZE)) {
                    widthAnimatorAnalog.addUpdateListener(new AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int val = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                            for (int idx = 1; idx != CoverClockGalleryLayout.this.mTotalClockNum; idx++) {
                                LayoutParams layoutParams = (LayoutParams) ((CoverAnalogClockLayout) CoverClockGalleryLayout.this.mClockArray[idx]).mAnalogClock.getLayoutParams();
                                layoutParams.width = val;
                                ((CoverAnalogClockLayout) CoverClockGalleryLayout.this.mClockArray[idx]).mAnalogClock.setLayoutParams(layoutParams);
                            }
                        }
                    });
                }
                as.playTogether(new Animator[]{widthAnimator, heightAnimator, animateX, animateY, animateX1, animateY1, layoutAnimator, layoutAnimator1, layoutAnimator2, layoutAnimatorX, layoutAnimatorAm, widthAnimatorAnalog});
                as.setDuration(400);
                as.setInterpolator(new DecelerateInterpolator());
                as.start();
                return;
            }
            this.mHandler.removeMessages(16);
            this.mHandler.sendEmptyMessageDelayed(16, 100);
        }
    }

    private void setContainerVisibility(boolean bHide) {
        if (this.mContainer == null) {
            return;
        }
        if (bHide) {
            this.mContainer.setVisibility(4);
        } else {
            this.mContainer.setVisibility(0);
        }
    }
}
