package com.android.deskclock.smartcover;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.deskclock.AlarmAlertWakeLock;
import com.android.deskclock.R;
import com.android.deskclock.alarmclock.CoverView;
import com.android.deskclock.alarmclock.LockAlarmFullActivity;
import com.android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class HwCustSmartCoverManagerImpl extends HwCustSmartCoverManager {
    private static final String TAG = "HwCustSmartCoverManagerImpl";
    private AlphaAnimation mAlphaAnimationOfAnalogClockPanel = null;
    private AlphaAnimation mAlphaAnimationOfAnalogClockPanel1 = null;
    private AlphaAnimation mAlphaAnimationOfHandler = null;
    private ImageView mAnalogClockEar = null;
    private RelativeLayout mAnalogClockWithoutHandler = null;
    private Context mContext = null;
    private RelativeLayout mCoverAnalogClock = null;
    private CoverAnimateFrameView mCoverAnimateFrameView = null;
    private CoverAnimateFrameView mCoverCloseLayout = null;
    private SmartCoverDigitalClockView mCoverDigitalClock = null;
    private CoverView mCoverScreen = null;
    private AnimationDrawable mEarAnimationDrawable = null;
    private ImageView mGrayAnalogPanal = null;
    private Handler mHandler = null;
    private AnimationSet mHourAnimationSet = new AnimationSet(true);
    private ImageView mHourImage = null;
    private RotateAnimation mHourRotateAnimation = null;
    private ScaleAnimation mHourScaleAnimation = null;
    private AnimationSet mMiniteAnimationSet = new AnimationSet(true);
    private ImageView mMiniteImage = null;
    private RotateAnimation mMiniteRotateAnimation = null;
    private ScaleAnimation mMiniteScaleAnimation = null;
    private ObjectAnimator mRotateAnimalOfAnalogClock = null;
    private AnimatorSet mRotateAnimatorSetOfGray = null;
    private AnimatorSet mRotateAnimatorSetOfWhite = null;
    private Util mUtil = null;
    private ImageView mWhiteAnalogPanal = null;
    Runnable runnable = new Runnable() {
        public void run() {
            if (HwCustSmartCoverManagerImpl.this.mEarAnimationDrawable != null && HwCustSmartCoverManagerImpl.this.mCoverAnimateFrameView != null && HwCustSmartCoverManagerImpl.this.mHandler != null) {
                if (HwCustSmartCoverManagerImpl.this.mEarAnimationDrawable.isRunning()) {
                    HwCustSmartCoverManagerImpl.this.mEarAnimationDrawable.stop();
                    HwCustSmartCoverManagerImpl.this.mHandler.postDelayed(HwCustSmartCoverManagerImpl.this.runnable, 600);
                } else {
                    HwCustSmartCoverManagerImpl.this.mEarAnimationDrawable.start();
                    HwCustSmartCoverManagerImpl.this.mCoverAnimateFrameView.startAnimal();
                    HwCustSmartCoverManagerImpl.this.mHandler.postDelayed(HwCustSmartCoverManagerImpl.this.runnable, 1800);
                }
            }
        }
    };

    public HwCustSmartCoverManagerImpl(Context context) {
        this.mContext = context;
    }

    public boolean isSmartCoverEnable() {
        return SystemProperties.getBoolean("ro.config.show_smart_cover", false);
    }

    public void initAnimation() {
        this.mRotateAnimalOfAnalogClock = ObjectAnimator.ofFloat(this.mCoverAnalogClock, "rotationY", new float[]{-90.0f, 0.0f});
        this.mRotateAnimalOfAnalogClock.setInterpolator(new AccelerateDecelerateInterpolator());
        this.mRotateAnimalOfAnalogClock.setDuration(1000);
        this.mAlphaAnimationOfAnalogClockPanel = new AlphaAnimation(0.0f, 1.0f);
        this.mAlphaAnimationOfAnalogClockPanel.setDuration(900);
        this.mAlphaAnimationOfAnalogClockPanel1 = new AlphaAnimation(0.0f, 0.0f);
        this.mAlphaAnimationOfAnalogClockPanel1.setDuration(100);
        this.mAlphaAnimationOfAnalogClockPanel1.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation arg0) {
            }

            public void onAnimationRepeat(Animation arg0) {
            }

            public void onAnimationEnd(Animation arg0) {
                if (HwCustSmartCoverManagerImpl.this.mAnalogClockWithoutHandler != null) {
                    HwCustSmartCoverManagerImpl.this.mAnalogClockWithoutHandler.startAnimation(HwCustSmartCoverManagerImpl.this.mAlphaAnimationOfAnalogClockPanel);
                }
            }
        });
        ObjectAnimator whiteAnalogClock = ObjectAnimator.ofFloat(this.mGrayAnalogPanal, "rotationY", new float[]{0.0f, 15.0f});
        whiteAnalogClock.setDuration(80);
        ObjectAnimator whiteAnalogClock1 = ObjectAnimator.ofFloat(this.mGrayAnalogPanal, "rotationY", new float[]{15.0f, -15.0f});
        whiteAnalogClock1.setDuration(160);
        ObjectAnimator whiteAnalogClock2 = ObjectAnimator.ofFloat(this.mGrayAnalogPanal, "rotationY", new float[]{-15.0f, 10.0f});
        whiteAnalogClock2.setDuration(120);
        ObjectAnimator whiteAnalogClock3 = ObjectAnimator.ofFloat(this.mGrayAnalogPanal, "rotationY", new float[]{10.0f, -10.0f});
        whiteAnalogClock3.setDuration(100);
        ObjectAnimator whiteAnalogClock4 = ObjectAnimator.ofFloat(this.mGrayAnalogPanal, "rotationY", new float[]{-10.0f, 0.0f});
        whiteAnalogClock4.setDuration(60);
        List<Animator> objectAnimatorListOfWhite = new ArrayList();
        objectAnimatorListOfWhite.add(whiteAnalogClock);
        objectAnimatorListOfWhite.add(whiteAnalogClock1);
        objectAnimatorListOfWhite.add(whiteAnalogClock2);
        objectAnimatorListOfWhite.add(whiteAnalogClock3);
        objectAnimatorListOfWhite.add(whiteAnalogClock4);
        this.mRotateAnimatorSetOfWhite = new AnimatorSet();
        this.mRotateAnimatorSetOfWhite.playSequentially(objectAnimatorListOfWhite);
        this.mRotateAnimatorSetOfWhite.setStartDelay(500);
        this.mRotateAnimatorSetOfWhite.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (HwCustSmartCoverManagerImpl.this.mHandler != null) {
                    HwCustSmartCoverManagerImpl.this.mHandler.post(HwCustSmartCoverManagerImpl.this.runnable);
                }
            }
        });
        ObjectAnimator grayAnalogClock = ObjectAnimator.ofFloat(this.mWhiteAnalogPanal, "rotationY", new float[]{0.0f, 10.0f});
        grayAnalogClock.setDuration(60);
        ObjectAnimator grayAnalogClock1 = ObjectAnimator.ofFloat(this.mWhiteAnalogPanal, "rotationY", new float[]{10.0f, -10.0f});
        grayAnalogClock1.setDuration(120);
        ObjectAnimator grayAnalogClock2 = ObjectAnimator.ofFloat(this.mWhiteAnalogPanal, "rotationY", new float[]{-10.0f, 0.0f});
        grayAnalogClock2.setDuration(60);
        List<Animator> objectAnimatorListOfGray = new ArrayList();
        objectAnimatorListOfGray.add(grayAnalogClock);
        objectAnimatorListOfGray.add(grayAnalogClock1);
        objectAnimatorListOfGray.add(grayAnalogClock2);
        this.mRotateAnimatorSetOfGray = new AnimatorSet();
        this.mRotateAnimatorSetOfGray.playSequentially(objectAnimatorListOfGray);
        this.mRotateAnimatorSetOfGray.setStartDelay(500);
        Util.getTime(this.mContext);
        float miniteToDegrees = ((float) (DigitalClockAdapter.getMinite() * 360)) / 60.0f;
        float hourToDegrees = 90.0f + (((((float) DigitalClockAdapter.getHour()) + (((float) DigitalClockAdapter.getMinite()) / 60.0f)) * 360.0f) / 12.0f);
        this.mMiniteRotateAnimation = new RotateAnimation(miniteToDegrees - 180.0f, miniteToDegrees, 1, 0.5f, 1, 0.92f);
        this.mMiniteRotateAnimation.setDuration(1600);
        this.mMiniteRotateAnimation.setStartOffset(160);
        this.mMiniteRotateAnimation.setFillAfter(true);
        this.mHourRotateAnimation = new RotateAnimation(hourToDegrees - 120.0f, hourToDegrees, 1, 0.92f, 1, 0.5f);
        this.mHourRotateAnimation.setDuration(1600);
        this.mHourRotateAnimation.setStartOffset(160);
        this.mHourRotateAnimation.setFillAfter(true);
        this.mAlphaAnimationOfHandler = new AlphaAnimation(0.0f, 1.0f);
        this.mAlphaAnimationOfHandler.setDuration(900);
        this.mAlphaAnimationOfHandler.setStartOffset(160);
        this.mMiniteScaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, 1, 0.5f, 1, 1.0f);
        this.mMiniteScaleAnimation.setDuration(500);
        this.mMiniteScaleAnimation.setStartOffset(160);
        this.mMiniteScaleAnimation.setFillAfter(true);
        this.mHourScaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, 1, 1.0f, 1, 0.5f);
        this.mHourScaleAnimation.setDuration(500);
        this.mHourScaleAnimation.setStartOffset(160);
        this.mHourScaleAnimation.setFillAfter(true);
        this.mMiniteAnimationSet.addAnimation(this.mAlphaAnimationOfHandler);
        this.mMiniteAnimationSet.addAnimation(this.mMiniteRotateAnimation);
        this.mMiniteAnimationSet.addAnimation(this.mMiniteScaleAnimation);
        this.mMiniteAnimationSet.setFillAfter(true);
        this.mHourAnimationSet.addAnimation(this.mAlphaAnimationOfHandler);
        this.mHourAnimationSet.addAnimation(this.mHourRotateAnimation);
        this.mHourAnimationSet.addAnimation(this.mHourScaleAnimation);
        this.mHourAnimationSet.setFillAfter(true);
        this.mEarAnimationDrawable = new AnimationDrawable();
        for (int i = 0; i < 7; i++) {
            this.mEarAnimationDrawable.addFrame(this.mContext.getResources().getDrawable(R.drawable.clock_ear_1), 50);
            this.mEarAnimationDrawable.addFrame(this.mContext.getResources().getDrawable(R.drawable.clock_ear_2), 50);
            this.mEarAnimationDrawable.addFrame(this.mContext.getResources().getDrawable(R.drawable.clock_ear_3), 50);
            this.mEarAnimationDrawable.addFrame(this.mContext.getResources().getDrawable(R.drawable.clock_ear_4), 50);
        }
        this.mEarAnimationDrawable.addFrame(this.mContext.getResources().getDrawable(R.drawable.clock_ear_1), 800);
        this.mEarAnimationDrawable.setOneShot(false);
        if (this.mAnalogClockEar != null) {
            this.mAnalogClockEar.setBackground(this.mEarAnimationDrawable);
        }
    }

    public CoverView addCoverScreen(Handler handler) {
        Log.d(TAG, "addCoverScreen");
        if (handler == null || this.mContext == null) {
            return null;
        }
        this.mHandler = handler;
        AlarmAlertWakeLock.acquireBrightScreenWakeLock(this.mContext);
        this.mCoverScreen = (CoverView) LayoutInflater.from(this.mContext).inflate(R.layout.activity_alarm_cover, null);
        this.mCoverDigitalClock = (SmartCoverDigitalClockView) this.mCoverScreen.findViewById(R.id.digitalclock);
        this.mCoverAnalogClock = (RelativeLayout) this.mCoverScreen.findViewById(R.id.AnalogClock);
        this.mCoverCloseLayout = (CoverAnimateFrameView) this.mCoverScreen.findViewById(R.id.close_layout);
        this.mAnalogClockEar = (ImageView) this.mCoverScreen.findViewById(R.id.AnalogClock_Ear);
        this.mMiniteImage = (ImageView) this.mCoverScreen.findViewById(R.id.AnalogClock_MinHand);
        this.mHourImage = (ImageView) this.mCoverScreen.findViewById(R.id.AnalogClock_HourHand);
        this.mAnalogClockWithoutHandler = (RelativeLayout) this.mCoverScreen.findViewById(R.id.AnalogClockPart);
        this.mWhiteAnalogPanal = (ImageView) this.mCoverScreen.findViewById(R.id.AnalogClock_Panel);
        this.mGrayAnalogPanal = (ImageView) this.mCoverScreen.findViewById(R.id.AnalogClock_Panel_In);
        this.mCoverAnimateFrameView = (CoverAnimateFrameView) this.mCoverScreen.findViewById(R.id.close_layout);
        initAnimation();
        if (this.mCoverDigitalClock == null || this.mCoverAnalogClock == null || this.mCoverCloseLayout == null) {
            Log.e(TAG, "view is null!");
            return null;
        }
        this.mCoverAnalogClock.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HwCustSmartCoverManagerImpl.this.addSnoozeCoverView(HwCustSmartCoverManagerImpl.this.mHandler);
            }
        });
        this.mCoverCloseLayout.setMainHandler(this.mHandler);
        this.mUtil = Util.getInstance(this.mContext);
        if (this.mUtil != null) {
            Util util = this.mUtil;
            Util.setHwCustSmartCoverManagerImpl(this);
        }
        return this.mCoverScreen;
    }

    public void startAnimal() {
        Log.d(TAG, "startAnimal");
        this.mRotateAnimalOfAnalogClock.start();
        this.mAnalogClockWithoutHandler.startAnimation(this.mAlphaAnimationOfAnalogClockPanel1);
        this.mHourImage.startAnimation(this.mHourAnimationSet);
        this.mMiniteImage.startAnimation(this.mMiniteAnimationSet);
        this.mRotateAnimatorSetOfWhite.start();
        this.mRotateAnimatorSetOfGray.start();
    }

    public void rotateTimeHandler() {
        float miniteFromDegrees = ((float) (DigitalClockAdapter.getPriMinite() * 360)) / 60.0f;
        float hourFromDegrees = 90.0f + (((((float) DigitalClockAdapter.getPriHour()) + (((float) DigitalClockAdapter.getPriMinite()) / 60.0f)) * 360.0f) / 12.0f);
        float miniteToDegrees = ((float) (DigitalClockAdapter.getMinite() * 360)) / 60.0f;
        float hourToDegrees = 90.0f + (((((float) DigitalClockAdapter.getHour()) + (((float) DigitalClockAdapter.getMinite()) / 60.0f)) * 360.0f) / 12.0f);
        if (this.mMiniteImage == null || this.mHourImage == null) {
            Log.e(TAG, "rotateTimeHandler null point");
            return;
        }
        this.mMiniteRotateAnimation = new RotateAnimation(miniteFromDegrees, miniteToDegrees, 1, 0.5f, 1, 0.92f);
        this.mMiniteRotateAnimation.setDuration(10);
        this.mMiniteRotateAnimation.setFillAfter(true);
        this.mHourRotateAnimation = new RotateAnimation(hourFromDegrees, hourToDegrees, 1, 0.92f, 1, 0.5f);
        this.mHourRotateAnimation.setDuration(10);
        this.mHourRotateAnimation.setFillAfter(true);
        this.mMiniteImage.startAnimation(this.mMiniteRotateAnimation);
        this.mHourImage.startAnimation(this.mHourRotateAnimation);
    }

    public void addSnoozeCoverView(Handler handler) {
        if (this.mContext != null && handler != null) {
            this.mHandler = handler;
            this.mHandler.sendEmptyMessage(LockAlarmFullActivity.MESSAGE_SNOOZE);
            this.mCoverScreen = (CoverView) LayoutInflater.from(this.mContext).inflate(R.layout.activity_alarm_cover_snooze, null);
            int snoozeMinutes = PreferenceManager.getDefaultSharedPreferences(this.mContext).getInt("snooze_duration", 10);
            ((TextView) this.mCoverScreen.findViewById(R.id.snooze_textview)).setText(this.mContext.getResources().getQuantityString(R.plurals.smart_cover_snooze_other, Long.valueOf((long) snoozeMinutes).intValue(), new Object[]{Integer.valueOf(Long.valueOf((long) snoozeMinutes).intValue())}));
            if (this.mUtil == null) {
                this.mUtil = Util.getInstance(this.mContext);
            }
            if (this.mUtil != null) {
                this.mUtil.addView(this.mCoverScreen);
            }
        }
    }
}
