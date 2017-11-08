package com.android.settings.fingerprint.enrollment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.fingerprint.Fingerprint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.ItemUseStat;
import com.android.settings.PrivacySpaceSettingsHelper;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settings.fingerprint.utils.BiometricManager;
import com.android.settings.fingerprint.utils.BiometricManager.CaptureCallback;
import com.android.settings.fingerprint.utils.BiometricManager.EnrollCallback;
import com.android.settings.fingerprint.utils.BiometricManager.EnrollProgress;
import com.android.settings.fingerprint.utils.BiometricManager.ProgressData;
import com.android.settings.fingerprint.utils.FingerprintUtils;
import com.android.settings.fingerprint.utils.HapticFeedback;
import com.android.settings.navigation.LazyLoadingAnimationContainer;
import com.android.settings.navigation.LazyLoadingAnimationContainer.OnAnimationStoppedListener;
import com.android.settings.navigation.NaviUtils;
import com.huawei.android.os.UserManagerEx;
import com.huawei.cust.HwCustUtils;
import java.util.List;

public class FingerprintEnrollFragment extends Fragment implements CaptureCallback, EnrollCallback, OnClickListener {
    private static final ArrayMap<Integer, Integer> RESUMABLE_FAIL_TIP_RES = new ArrayMap();
    private static final int[] STEP_MSG_IDS = new int[]{1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008};
    private static final ArrayMap<Integer, Integer> SUPPLEMENT_RES = new ArrayMap();
    private static final ArrayMap<Integer, Integer> SUPPLEMENT_RES_FLASH = new ArrayMap();
    private static final ArrayMap<Integer, Integer> SUPPLEMENT_TIP_RES = new ArrayMap();
    private AlphaAnimation mAlphaAnimation;
    private BiometricManager mBm;
    protected Context mContext;
    private List<Fingerprint> mCurrentFpList;
    private int mCurrentPhase = 0;
    private EnrollProgress mCurrentProgress;
    private Toast mDuplicatedFpTip;
    private Toast mDuplicatedNameToast;
    private View mEnrollBottomLayout;
    private Button mEnrollCancelButton;
    private boolean mEnrollFinished;
    private View mEnrollLayout;
    private LazyLoadingAnimationContainer mEnrollTipAnime;
    private AlertDialog mFingerLowCovergeDialog;
    private AlertDialog mFingerRegionChangeDialog;
    private AlertDialog mFingerStillDialog;
    private int mFpId;
    protected ImageView mFpImage;
    private ImageView mFpImageBg;
    private int mFpModeIndex;
    private String mFpName;
    protected AnimationDrawable mFpTipAnima;
    protected ImageView mFpTipImage;
    private ImageView mFpTipImageBg;
    private LazyLoadingAnimationContainer[] mFrontPressAnimes = new LazyLoadingAnimationContainer[9];
    private LazyLoadingAnimationContainer[] mFrontReleaseAnimes = new LazyLoadingAnimationContainer[8];
    private EnrollHandler mHandler;
    private final HapticFeedback mHaptic = new HapticFeedback();
    private boolean mHasDuplicated;
    private HwCustFingerprintEnroll mHwCustFingerprintEnroll;
    private AlertDialog mImageLowQualityDialog;
    private View mIntroEnrollLayout;
    private boolean mIsFifthStep;
    private boolean mIsPrivateUser;
    private LazyLoadingAnimationContainer[] mLegacyPressAnimes = new LazyLoadingAnimationContainer[5];
    private LazyLoadingAnimationContainer[] mLegacyReleaseAnimes = new LazyLoadingAnimationContainer[4];
    private int mMaxEnrollSteps = -1;
    private LazyLoadingAnimationContainer[] mNormalPressAnimes = new LazyLoadingAnimationContainer[6];
    private LazyLoadingAnimationContainer[] mNormalReleaseAnimes = new LazyLoadingAnimationContainer[5];
    private Button mOkBtn;
    private LazyLoadingAnimationContainer[] mPressAnimes;
    private LazyLoadingAnimationContainer[] mReleaseAnimes;
    private Button mRenameOrRestartBtn;
    private int mRequestCode;
    protected Resources mRes;
    private int mResumableFailure = 0;
    protected TextView mSubTipText;
    private AlertDialog mTimeoutDialog;
    protected TextView mTipDownText;
    private TextView mTipImproveDownText;
    private AlphaAnimation mTipInAnim = new AlphaAnimation(0.0f, 1.0f);
    private AlphaAnimation mTipOutAnim = new AlphaAnimation(1.0f, 0.0f);
    private TextSwitchAnimListener mTipSwitchAnimListner;
    protected TextView mTipText;
    private AlphaAnimation mTitleInAnim = new AlphaAnimation(0.0f, 1.0f);
    private AlphaAnimation mTitleOutAnim = new AlphaAnimation(1.0f, 0.0f);
    private TextSwitchAnimListener mTitleSwitchAnimListner;
    protected TextView mTitleText;
    private byte[] mToken;
    private int mUserId = UserHandle.myUserId();
    private UserInfo mUserInfo;
    private AlertDialog nameAlertDialog;
    private TextWatcher nameTextWatcher = new TextWatcher() {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
            if (FingerprintEnrollFragment.this.nameAlertDialog != null) {
                Button positiveBtn = FingerprintEnrollFragment.this.nameAlertDialog.getButton(-1);
                if (s == null || s.toString().trim().length() == 0 || FingerprintEnrollFragment.this.isDuplicatedFpName(s.toString().trim())) {
                    positiveBtn.setEnabled(false);
                } else {
                    positiveBtn.setEnabled(true);
                }
            }
        }
    };

    private class AnimeStopListener implements OnAnimationStoppedListener {
        public int step;
        boolean triggerNext;
        public int type;

        private AnimeStopListener() {
            this.triggerNext = true;
        }

        public void onAnimationStopped() {
            if (this.type == 1 && this.triggerNext) {
                if (this.step < FingerprintEnrollFragment.this.mMaxEnrollSteps - 1) {
                    FingerprintEnrollFragment.this.mReleaseAnimes[this.step].start();
                } else if (this.step == FingerprintEnrollFragment.this.mMaxEnrollSteps - 1) {
                    Log.d("FingerprintEnrollFragment", "onAnimationStopped showSupplementInfo");
                    FingerprintEnrollFragment.this.showSupplementInfo();
                }
            }
        }
    }

    public class EnrollHandler extends Handler {
        public void handleMessage(Message msg) {
            Log.i("FingerprintEnrollFragment", "handleMessage, msg.what = " + msg.what);
            if (FingerprintEnrollFragment.this.mHwCustFingerprintEnroll == null || !FingerprintEnrollFragment.this.mHwCustFingerprintEnroll.isSupportSepFingerPrint() || msg.what >= 100 || msg.what <= -1) {
                FingerprintEnrollFragment.this.handleEnrollMessage(msg);
            } else {
                FingerprintEnrollFragment.this.mHwCustFingerprintEnroll.handleMessage(msg);
            }
        }
    }

    private static class FingerprintInfo {
        int fpIndex;
        String fpName;

        public FingerprintInfo(int fpIndex, String fpName) {
            this.fpIndex = fpIndex;
            this.fpName = fpName;
        }
    }

    public static class TextSwitchAnimListener implements AnimationListener {
        public Animation nextAnim;
        public int nextTextResId = 0;
        public TextView textView;

        public TextSwitchAnimListener(TextView textView, int nextTextResId, Animation nextAnim) {
            this.textView = textView;
            this.nextTextResId = nextTextResId;
            this.nextAnim = nextAnim;
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            this.textView.setText(this.nextTextResId);
            this.textView.startAnimation(this.nextAnim);
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        this.mContext = getActivity();
        this.mRes = this.mContext.getResources();
        this.mHandler = new EnrollHandler();
        this.mEnrollFinished = false;
        this.mHaptic.init(this.mContext, true);
        this.mIsFifthStep = false;
        this.mAlphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        this.mAlphaAnimation.setDuration(700);
        this.mAlphaAnimation.setRepeatMode(2);
        this.mAlphaAnimation.setRepeatCount(-1);
        if (Utils.onlySupportPortrait()) {
            ((Activity) this.mContext).setRequestedOrientation(1);
        }
        this.mHwCustFingerprintEnroll = (HwCustFingerprintEnroll) HwCustUtils.createObj(HwCustFingerprintEnroll.class, new Object[]{this});
        Intent i = ((Activity) this.mContext).getIntent();
        if (i != null) {
            this.mRequestCode = i.getIntExtra("request_code", -1);
            this.mUserId = i.getIntExtra("android.intent.extra.USER", UserHandle.myUserId());
            this.mToken = i.getByteArrayExtra("hw_auth_token");
        }
        if (this.mToken == null) {
            Log.e("FingerprintEnrollFragment", "Challenge token should not be null.");
            finish();
        }
        this.mUserInfo = UserManager.get(this.mContext).getUserInfo(UserHandle.myUserId());
        this.mIsPrivateUser = UserManagerEx.isHwHiddenSpace(this.mUserInfo);
        initAlphaAnims();
        super.onCreate(savedInstanceState);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(2130968784, container, false);
        if (this.mHwCustFingerprintEnroll != null && this.mHwCustFingerprintEnroll.isFrontFingerPrint()) {
            this.mHwCustFingerprintEnroll.addFpBackButton((RelativeLayout) fragmentView.findViewById(2131886597).getParent());
        }
        this.mTitleText = (TextView) fragmentView.findViewById(2131886598);
        this.mTipText = (TextView) fragmentView.findViewById(2131886599);
        this.mTipText.setMovementMethod(ScrollingMovementMethod.getInstance());
        this.mTipText.setVisibility(0);
        this.mSubTipText = (TextView) fragmentView.findViewById(2131886600);
        this.mFpImage = (ImageView) fragmentView.findViewById(2131886608);
        this.mFpImageBg = (ImageView) fragmentView.findViewById(2131886609);
        if (this.mHwCustFingerprintEnroll != null && this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            this.mHwCustFingerprintEnroll.addAnimationView((RelativeLayout) this.mFpImageBg.getParent());
            this.mHwCustFingerprintEnroll.addPercentageView((RelativeLayout) this.mFpImageBg.getParent());
            this.mFpImage.setVisibility(8);
            this.mFpImageBg.setVisibility(8);
        }
        this.mRenameOrRestartBtn = (Button) fragmentView.findViewById(2131886612);
        this.mOkBtn = (Button) fragmentView.findViewById(2131886613);
        this.mRenameOrRestartBtn.setOnClickListener(this);
        this.mOkBtn.setOnClickListener(this);
        this.mTipDownText = (TextView) fragmentView.findViewById(2131886610);
        this.mTipImproveDownText = (TextView) fragmentView.findViewById(2131886611);
        this.mEnrollLayout = fragmentView.findViewById(2131886607);
        this.mEnrollBottomLayout = fragmentView.findViewById(2131886614);
        this.mEnrollCancelButton = (Button) fragmentView.findViewById(2131886615);
        this.mEnrollCancelButton.setOnClickListener(this);
        initIntroLayout(fragmentView);
        initAnimations();
        return fragmentView;
    }

    private void initIntroLayout(View fragmentView) {
        View frontLayout = fragmentView.findViewById(2131886601);
        View backLayout = fragmentView.findViewById(2131886604);
        if (NaviUtils.isFrontFingerNaviEnabled()) {
            backLayout.setVisibility(8);
            this.mIntroEnrollLayout = frontLayout;
            this.mFpTipImage = (ImageView) fragmentView.findViewById(2131886603);
            this.mFpTipImageBg = (ImageView) fragmentView.findViewById(2131886602);
            return;
        }
        frontLayout.setVisibility(8);
        this.mIntroEnrollLayout = backLayout;
        this.mFpTipImage = (ImageView) fragmentView.findViewById(2131886606);
        this.mFpTipImageBg = (ImageView) fragmentView.findViewById(2131886605);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (16908332 == item.getItemId()) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onResume() {
        super.onResume();
        if (!BiometricManager.isFingerprintEnabled(this.mContext)) {
            if (!SettingsExtUtils.isStartupGuideMode(this.mContext.getContentResolver())) {
                finish();
                return;
            } else if (!BiometricManager.setFingerprintEnabled(true)) {
                Log.e("FingerprintEnrollFragment", "set fingerprint enabled failed");
                finish();
                return;
            }
        }
        if (!this.mEnrollFinished) {
            this.mRenameOrRestartBtn.setVisibility(8);
            this.mOkBtn.setVisibility(8);
        }
        Log.e("FingerprintEnrollFragment", "BiometircManager.isFingerprintEnabled() = " + BiometricManager.isFingerprintEnabled(this.mContext));
        this.mBm = BiometricManager.open(this.mContext);
        if (this.mBm != null) {
            this.mBm.setCaptureCallback(this);
            this.mCurrentFpList = this.mBm.getFingerprintList(this.mUserId);
            generateFingerprintInfo();
            this.mBm.startEnroll(this, this.mToken, this.mUserId);
        } else {
            Toast.makeText(this.mContext, this.mRes.getString(2131627668), 0).show();
            finish();
        }
        this.mHandler.sendEmptyMessage(1101);
    }

    public void onPause() {
        super.onPause();
        releaseAd(this.mFpTipAnima);
        this.mHasDuplicated = false;
        if (BiometricManager.isFingerprintEnabled(this.mContext)) {
            if (this.mBm != null) {
                if (this.mEnrollFinished) {
                    if (this.mRequestCode == 401) {
                        this.mBm.setAssociation(this.mContext, "com.android.keyguard", true, this.mUserId);
                    }
                    FingerprintUtils.onFingerprintNumChanged(this.mContext, this.mUserId);
                } else {
                    this.mBm.cancelEnroll();
                }
                this.mBm.abort();
                this.mBm.release();
                this.mBm = null;
            }
            dismissDialog();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mHwCustFingerprintEnroll != null && this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            this.mHwCustFingerprintEnroll.destory();
        }
    }

    public void onCaptureCompleted() {
        if (this.mCurrentProgress == null) {
            Log.e("FingerprintEnrollFragment", "current progress is null!");
            return;
        }
        Log.i("FingerprintEnrollFragment", "onCaptureCompleted mCurrentProgress.currentStep = " + this.mCurrentProgress.currentStep + ", mCurrentProgress.totalSteps = " + this.mCurrentProgress.totalSteps + ", mCurrentProgress.percents = " + this.mCurrentProgress.percents + ", mCurrentProgress.guideDirection = " + this.mCurrentProgress.guideDirection + ", mMaxEnrollSteps = " + this.mMaxEnrollSteps + ", mCurrentPhase = " + this.mCurrentPhase);
        if (this.mHasDuplicated) {
            Log.i("FingerprintEnrollFragment", "onCaptureCompleted hasDuplicated, do not continue!");
        } else {
            this.mHandler.sendEmptyMessage(1102);
        }
    }

    private void switchTextWithAlpha(TextView textView, int resId, AlphaAnimation inAnim, AlphaAnimation outAnim, TextSwitchAnimListener listener) {
        if (listener == null) {
            listener = new TextSwitchAnimListener(textView, resId, inAnim);
        }
        textView.clearAnimation();
        inAnim.setDuration(250);
        outAnim.setDuration(250);
        listener.textView = textView;
        listener.nextTextResId = resId;
        listener.nextAnim = inAnim;
        outAnim.setAnimationListener(listener);
        textView.startAnimation(outAnim);
    }

    private void switchTitleToText(int resId) {
        Log.d("FingerprintEnrollFragment", "switchTitleToText!");
        switchTextWithAlpha(this.mTitleText, resId, this.mTitleInAnim, this.mTitleOutAnim, this.mTitleSwitchAnimListner);
    }

    private void switchTipToText(int resId) {
        Log.d("FingerprintEnrollFragment", "switchTipToText!");
        switchTextWithAlpha(this.mTipText, resId, this.mTipInAnim, this.mTipOutAnim, this.mTipSwitchAnimListner);
    }

    private void initAlphaAnims() {
        this.mTitleInAnim.setDuration(250);
        this.mTitleOutAnim.setDuration(250);
        this.mTitleSwitchAnimListner = new TextSwitchAnimListener(this.mTitleText, -1, this.mTitleInAnim);
        this.mTitleOutAnim.setAnimationListener(this.mTitleSwitchAnimListner);
        this.mTipInAnim.setDuration(250);
        this.mTipOutAnim.setDuration(250);
        this.mTipSwitchAnimListner = new TextSwitchAnimListener(this.mTipText, -1, this.mTipInAnim);
        this.mTipOutAnim.setAnimationListener(this.mTipSwitchAnimListner);
    }

    private int getTextSwitchStep() {
        if (NaviUtils.isFrontFingerNaviEnabled()) {
            return 4;
        }
        if (isStandardEnrollStep()) {
            return 1;
        }
        return 0;
    }

    private void clearAnimations() {
        int idx;
        OnAnimationStoppedListener listener;
        Log.d("FingerprintEnrollFragment", "clearAnimations");
        if (this.mPressAnimes != null) {
            for (idx = 0; idx < this.mPressAnimes.length; idx++) {
                listener = this.mPressAnimes[idx].getOnAnimationStoppedListener();
                if (listener != null && (listener instanceof AnimeStopListener)) {
                    ((AnimeStopListener) listener).triggerNext = false;
                }
                this.mPressAnimes[idx].stop();
            }
        }
        if (this.mReleaseAnimes != null) {
            for (idx = 0; idx < this.mReleaseAnimes.length; idx++) {
                listener = this.mReleaseAnimes[idx].getOnAnimationStoppedListener();
                if (listener != null && (listener instanceof AnimeStopListener)) {
                    ((AnimeStopListener) listener).triggerNext = false;
                }
                this.mReleaseAnimes[idx].stop();
            }
        }
        if (this.mEnrollTipAnime != null) {
            this.mEnrollTipAnime.stop();
        }
        this.mFpImageBg.clearAnimation();
        this.mFpImageBg.setVisibility(8);
    }

    private void stopAnimations() {
        Log.d("FingerprintEnrollFragment", "stopAnimations");
        if (this.mPressAnimes != null) {
            for (LazyLoadingAnimationContainer stop : this.mPressAnimes) {
                stop.stop();
            }
        }
        if (this.mReleaseAnimes != null) {
            for (LazyLoadingAnimationContainer stop2 : this.mReleaseAnimes) {
                stop2.stop();
            }
        }
        if (this.mEnrollTipAnime != null) {
            this.mEnrollTipAnime.stop();
        }
        this.mFpImageBg.clearAnimation();
        this.mFpImageBg.setVisibility(8);
    }

    public void onInput() {
        if (this.mHwCustFingerprintEnroll != null && this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            this.mIntroEnrollLayout.setVisibility(8);
            this.mEnrollLayout.setVisibility(0);
            this.mHwCustFingerprintEnroll.onInput();
        }
        dismissDialog();
    }

    public void onWaitingForInput() {
    }

    public void onEnrolled(int fpId) {
        if (this.mHwCustFingerprintEnroll == null || !this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            this.mHandler.sendEmptyMessage(99);
        }
        this.mHandler.sendEmptyMessageDelayed(2000, 400);
    }

    public void onEnrollmentFailed(int err) {
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3000, err, 0), 400);
    }

    public void onDuplicated() {
        if (this.mHwCustFingerprintEnroll != null && this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            this.mHasDuplicated = true;
            this.mHwCustFingerprintEnroll.onDuplicated();
        }
        this.mHandler.removeMessages(3001);
        this.mHandler.sendEmptyMessageDelayed(3001, 200);
        this.mHasDuplicated = true;
    }

    public void onImageLowQuality() {
        if (this.mHwCustFingerprintEnroll == null || !this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            Log.i("FingerprintEnrollFragment", "onImageLowQuality mCurrentPhase = " + this.mCurrentPhase + ", currentStep = " + getCurrentStep());
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1103, 2, 0));
            return;
        }
        this.mHwCustFingerprintEnroll.onImageLowQuality();
    }

    public void onFingerLowCoverge() {
        if (this.mHwCustFingerprintEnroll == null || !this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            Log.i("FingerprintEnrollFragment", "onFingerLowCoverge mCurrentPhase = " + this.mCurrentPhase + ", currentStep = " + getCurrentStep());
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1103, 3, 0));
            return;
        }
        this.mHwCustFingerprintEnroll.onFingerLowCoverge();
    }

    public void onGuideProgress(ProgressData enrollGuideData) {
        Log.i("FingerprintEnrollFragment", "onGuideProgress() called, nextDirection = " + enrollGuideData.guidedNextDirection + ", guidedProgress = " + enrollGuideData.guidedProgress);
        if (100 == enrollGuideData.guidedProgress && this.mHwCustFingerprintEnroll != null && this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            this.mHwCustFingerprintEnroll.drawTheLast();
            dismissDialog();
        }
    }

    public void onFingerRegionChange() {
        if (this.mHwCustFingerprintEnroll != null && this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            this.mHwCustFingerprintEnroll.onFingerRegionChange();
        }
        Log.i("FingerprintEnrollFragment", "onFingerRegionChange mCurrentPhase = " + this.mCurrentPhase + ", currentStep = " + getCurrentStep());
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1103, 4, 0));
    }

    public void onFingerStill() {
        if (this.mHwCustFingerprintEnroll != null && this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            this.mHwCustFingerprintEnroll.onFingerStill();
        }
        Log.i("FingerprintEnrollFragment", "onFingerStill mCurrentPhase = " + this.mCurrentPhase + ", currentStep = " + getCurrentStep());
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1103, 1, 0));
    }

    public void onSensorDirty() {
        Log.i("FingerprintEnrollFragment", "onSensorDirty mCurrentPhase = " + this.mCurrentPhase + ", currentStep = " + getCurrentStep());
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1103, 5, 0));
    }

    private void dealTextSwitchResumableFailure(int resId) {
        if (this.mCurrentPhase == 0 || this.mCurrentPhase == 1) {
            goToInitPhase();
        }
        switchTipToText(resId);
    }

    private void goToInitPhase() {
        Log.i("FingerprintEnrollFragment", "goToInitPhase mCurrentPhase = " + this.mCurrentPhase + ", currentStep = " + getCurrentStep());
        stopAnimations();
        if (this.mCurrentPhase == 0) {
            switchTitleToText(2131627691);
            this.mSubTipText.setVisibility(8);
        }
        this.mFpImage.setVisibility(0);
        this.mFpImage.setImageDrawable(this.mRes.getDrawable(2130837813));
        this.mFpImageBg.setVisibility(8);
        this.mTipDownText.setVisibility(8);
        this.mTipImproveDownText.setVisibility(8);
        this.mEnrollLayout.setVisibility(0);
        this.mEnrollBottomLayout.setVisibility(8);
        this.mIntroEnrollLayout.setVisibility(8);
        this.mCurrentPhase = 1;
    }

    static {
        initSupplementDrawables();
        initSupplementTips();
        initResumableTips();
    }

    private static void initSupplementDrawables() {
        SUPPLEMENT_RES.put(Integer.valueOf(2009), Integer.valueOf(2130837707));
        SUPPLEMENT_RES.put(Integer.valueOf(2010), Integer.valueOf(2130837707));
        SUPPLEMENT_RES.put(Integer.valueOf(2011), Integer.valueOf(2130837705));
        SUPPLEMENT_RES.put(Integer.valueOf(2007), Integer.valueOf(2130837705));
        SUPPLEMENT_RES.put(Integer.valueOf(2006), Integer.valueOf(2130837701));
        SUPPLEMENT_RES.put(Integer.valueOf(2005), Integer.valueOf(2130837701));
        SUPPLEMENT_RES.put(Integer.valueOf(2012), Integer.valueOf(2130837703));
        SUPPLEMENT_RES.put(Integer.valueOf(2008), Integer.valueOf(2130837703));
        SUPPLEMENT_RES_FLASH.put(Integer.valueOf(2009), Integer.valueOf(2130837708));
        SUPPLEMENT_RES_FLASH.put(Integer.valueOf(2010), Integer.valueOf(2130837708));
        SUPPLEMENT_RES_FLASH.put(Integer.valueOf(2011), Integer.valueOf(2130837706));
        SUPPLEMENT_RES_FLASH.put(Integer.valueOf(2007), Integer.valueOf(2130837706));
        SUPPLEMENT_RES_FLASH.put(Integer.valueOf(2006), Integer.valueOf(2130837702));
        SUPPLEMENT_RES_FLASH.put(Integer.valueOf(2005), Integer.valueOf(2130837702));
        SUPPLEMENT_RES_FLASH.put(Integer.valueOf(2012), Integer.valueOf(2130837704));
        SUPPLEMENT_RES_FLASH.put(Integer.valueOf(2008), Integer.valueOf(2130837704));
    }

    private static void initSupplementTips() {
        SUPPLEMENT_TIP_RES.put(Integer.valueOf(2009), Integer.valueOf(2131628847));
        SUPPLEMENT_TIP_RES.put(Integer.valueOf(2010), Integer.valueOf(2131628847));
        SUPPLEMENT_TIP_RES.put(Integer.valueOf(2011), Integer.valueOf(2131628847));
        SUPPLEMENT_TIP_RES.put(Integer.valueOf(2007), Integer.valueOf(2131628847));
        SUPPLEMENT_TIP_RES.put(Integer.valueOf(2006), Integer.valueOf(2131628847));
        SUPPLEMENT_TIP_RES.put(Integer.valueOf(2005), Integer.valueOf(2131628847));
        SUPPLEMENT_TIP_RES.put(Integer.valueOf(2012), Integer.valueOf(2131628847));
        SUPPLEMENT_TIP_RES.put(Integer.valueOf(2008), Integer.valueOf(2131628847));
    }

    private static void initResumableTips() {
        RESUMABLE_FAIL_TIP_RES.put(Integer.valueOf(1), Integer.valueOf(2131628869));
        RESUMABLE_FAIL_TIP_RES.put(Integer.valueOf(2), Integer.valueOf(2131628868));
        RESUMABLE_FAIL_TIP_RES.put(Integer.valueOf(3), Integer.valueOf(2131628871));
        RESUMABLE_FAIL_TIP_RES.put(Integer.valueOf(4), Integer.valueOf(2131628870));
        RESUMABLE_FAIL_TIP_RES.put(Integer.valueOf(5), Integer.valueOf(2131628955));
    }

    private void initNormalAnimations() {
        int idx;
        for (idx = 0; idx < this.mNormalPressAnimes.length; idx++) {
            this.mNormalPressAnimes[idx] = new LazyLoadingAnimationContainer(this.mFpImage);
            this.mNormalPressAnimes[idx].addAllFrames(EnrollAnimeRes.PRESS_RES[idx], EnrollAnimeRes.PRESS_DURATION[idx]);
            this.mNormalPressAnimes[idx].setOneshot(true);
            this.mNormalPressAnimes[idx].setTag("PressEnroll_" + (idx + 1));
        }
        for (idx = 0; idx < this.mNormalReleaseAnimes.length; idx++) {
            this.mNormalReleaseAnimes[idx] = new LazyLoadingAnimationContainer(this.mFpImage);
            this.mNormalReleaseAnimes[idx].addAllFrames(EnrollAnimeRes.RELEASE_RES[idx], EnrollAnimeRes.RELEASE_DURATION[idx]);
            this.mNormalReleaseAnimes[idx].setOneshot(true);
            this.mNormalReleaseAnimes[idx].setTag("ReleaseEnroll_" + (idx + 1));
        }
        for (idx = 0; idx < this.mLegacyPressAnimes.length; idx++) {
            if (idx == 0) {
                this.mLegacyPressAnimes[idx] = new LazyLoadingAnimationContainer(this.mFpImage);
                this.mLegacyPressAnimes[idx].addAllFrames(EnrollAnimeRes.LEGACY_PRESS_RES[idx], EnrollAnimeRes.LEGACY_PRESS_DURATION[idx]);
                this.mLegacyPressAnimes[idx].setOneshot(true);
            } else {
                this.mLegacyPressAnimes[idx] = this.mNormalPressAnimes[idx + 1];
            }
        }
        for (idx = 0; idx < this.mLegacyReleaseAnimes.length; idx++) {
            this.mLegacyReleaseAnimes[idx] = this.mNormalReleaseAnimes[idx + 1];
        }
    }

    private void initFrontAnimations() {
        int idx;
        for (idx = 0; idx < this.mFrontPressAnimes.length; idx++) {
            this.mFrontPressAnimes[idx] = new LazyLoadingAnimationContainer(this.mFpImage);
            this.mFrontPressAnimes[idx].addAllFrames(EnrollAnimeRes.FRONT_PRESS_RES[idx], EnrollAnimeRes.FRONT_PRESS_DURATION[idx]);
            this.mFrontPressAnimes[idx].setOneshot(true);
            this.mFrontPressAnimes[idx].setTag("FrontPressEnroll_" + (idx + 1));
        }
        for (idx = 0; idx < this.mFrontReleaseAnimes.length; idx++) {
            this.mFrontReleaseAnimes[idx] = new LazyLoadingAnimationContainer(this.mFpImage);
            this.mFrontReleaseAnimes[idx].addAllFrames(EnrollAnimeRes.FRONT_RELEASE_RES[idx], EnrollAnimeRes.FRONT_RELEASE_DURATION[idx]);
            this.mFrontReleaseAnimes[idx].setOneshot(true);
            this.mFrontReleaseAnimes[idx].setTag("FrontReleaseEnroll_" + (idx + 1));
        }
    }

    private void initAnimations() {
        this.mEnrollTipAnime = new LazyLoadingAnimationContainer(this.mFpTipImage);
        this.mEnrollTipAnime.setTag("EnrollIntro");
        if (NaviUtils.isFrontFingerNaviEnabled()) {
            this.mEnrollTipAnime.addAllFrames(EnrollAnimeRes.ENROLL_FRONT_TIP_RES, 1000);
            initFrontAnimations();
            return;
        }
        this.mEnrollTipAnime.addAllFrames(EnrollAnimeRes.ENROLL_BACK_TIP_RES, 1000);
        initNormalAnimations();
    }

    private void dispatchEnrollMessage(EnrollProgress progress) {
        if (this.mCurrentProgress != null) {
            Log.d("FingerprintEnrollFragment", "dispatchEnrollMessage new step = " + progress.currentStep + ", old step = " + this.mCurrentProgress.currentStep + ", total step = " + progress.totalSteps);
        } else {
            Log.d("FingerprintEnrollFragment", "dispatchEnrollMessage new step = " + progress.currentStep + ", total step = " + progress.totalSteps);
        }
        if (this.mCurrentProgress == null || progress.currentStep != this.mCurrentProgress.currentStep) {
            this.mCurrentProgress = progress;
            if (this.mCurrentProgress.currentStep >= 0) {
                if (this.mCurrentProgress.currentStep < getMaxStandardStep()) {
                    this.mHandler.sendEmptyMessage(STEP_MSG_IDS[this.mCurrentProgress.currentStep]);
                } else {
                    this.mHandler.sendEmptyMessage(1100);
                }
                return;
            }
            return;
        }
        Log.d("FingerprintEnrollFragment", "dispatchEnrollMessage repeat progress message, do not handle!");
    }

    private void handleEnrollMessage(Message msg) {
        Log.i("FingerprintEnrollFragment", "handleEnrollMessage msg = " + msg.what);
        if (msg.what == 1101) {
            handleIntroTips(msg);
        } else if (msg.what == 3000) {
            handleEnrollFailed(msg, msg.arg1);
        } else if (msg.what == 3001) {
            handleDuplicateFinger();
        } else if (msg.what == 1103) {
            handleResumableFailure(msg);
        } else {
            if (this.mHwCustFingerprintEnroll != null && this.mHwCustFingerprintEnroll.isSupportSepFingerPrint() && msg.what == 2000) {
                handleEnrollSuccess();
            }
            if (this.mCurrentProgress == null) {
                Log.e("FingerprintEnrollFragment", "can not handle enroll message due to null progress");
                return;
            }
            int maxStandardStepMsg = getMaxStandardStepMsg();
            if (msg.what >= 1000 && msg.what <= maxStandardStepMsg) {
                handleEnrollStep(msg.what - 1000);
            } else if (msg.what == 1100) {
                handleSupplementStep();
            } else if (msg.what == 2000) {
                handleEnrollSuccess();
            } else if (msg.what == 1102) {
                handleFingerRelease();
            }
        }
    }

    private int getMaxStandardStep() {
        if (NaviUtils.isFrontFingerNaviEnabled()) {
            return 9;
        }
        if (isStandardEnrollStep()) {
            return 6;
        }
        return 5;
    }

    private int getMaxStandardStepMsg() {
        return (getMaxStandardStep() + 1000) - 1;
    }

    private boolean isStandardEnrollStep() {
        return this.mMaxEnrollSteps != 5;
    }

    private void resetEnrollOnDuplicated() {
        this.mCurrentProgress = null;
        this.mEnrollFinished = false;
    }

    private void resetEnroll(int totalStep) {
        updateEnrollAnimations(totalStep);
        this.mCurrentProgress = null;
        this.mHasDuplicated = false;
        this.mEnrollFinished = false;
    }

    private void updateEnrollAnimations(int totalStep) {
        if (NaviUtils.isFrontFingerNaviEnabled()) {
            this.mPressAnimes = this.mFrontPressAnimes;
            this.mReleaseAnimes = this.mFrontReleaseAnimes;
            this.mMaxEnrollSteps = 9;
        } else if (totalStep == 6) {
            this.mPressAnimes = this.mLegacyPressAnimes;
            this.mReleaseAnimes = this.mLegacyReleaseAnimes;
            this.mMaxEnrollSteps = 5;
        } else {
            this.mPressAnimes = this.mNormalPressAnimes;
            this.mReleaseAnimes = this.mNormalReleaseAnimes;
            this.mMaxEnrollSteps = 6;
        }
        for (int idx = 0; idx < this.mPressAnimes.length; idx++) {
            AnimeStopListener listener = new AnimeStopListener();
            listener.type = 1;
            listener.step = idx;
            listener.triggerNext = true;
            if (this.mPressAnimes[idx] == null) {
                Log.e("FingerprintEnrollFragment", "null press animation, step = " + idx);
            } else {
                this.mPressAnimes[idx].setOnAnimationStoppedListener(listener);
            }
        }
    }

    private void handleFingerRelease() {
        Log.i("FingerprintEnrollFragment", "handleFingerRelease mCurrentProgress.currentStep = " + this.mCurrentProgress.currentStep);
        if (this.mCurrentProgress.currentStep < this.mMaxEnrollSteps) {
            int textSwitchStep = getTextSwitchStep();
            Log.d("FingerprintEnrollFragment", "handleFingerRelease textSwitchStep = " + textSwitchStep);
            if (this.mCurrentProgress.currentStep >= textSwitchStep && this.mCurrentProgress.currentStep < this.mMaxEnrollSteps - 1) {
                Log.d("FingerprintEnrollFragment", "handleFingerRelease mCurrentPhase = " + this.mCurrentPhase + ", currentStep = " + this.mCurrentProgress.currentStep);
                if (this.mCurrentPhase != 3) {
                    Log.i("FingerprintEnrollFragment", "PHASE switch : enroll -> enroll_edge");
                    switchTitleToText(2131627692);
                    switchTipToText(2131628678);
                }
                this.mCurrentPhase = 3;
            }
            this.mPressAnimes[this.mCurrentProgress.currentStep].stop();
            return;
        }
        Log.d("FingerprintEnrollFragment", "showSupplementInfo");
        showSupplementInfo();
    }

    private void handleResumableFailure(Message msg) {
        Log.i("FingerprintEnrollFragment", "handleResumableFailure, msg = " + msg.what + ", fail code = " + msg.arg1);
        int failCode = msg.arg1;
        dealTextSwitchResumableFailure(((Integer) RESUMABLE_FAIL_TIP_RES.get(Integer.valueOf(failCode))).intValue());
        this.mResumableFailure = failCode;
    }

    private void handleIntroTips(Message msg) {
        Log.d("FingerprintEnrollFragment", "handleIntroTips clear state, msg = " + msg.what);
        this.mTitleText.setText(this.mRes.getString(2131628898));
        this.mTitleText.setTextColor(this.mContext.getResources().getColor(2131427330));
        if (this.mHwCustFingerprintEnroll == null || !this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            if (NaviUtils.isFrontFingerNaviEnabled()) {
                if (Utils.isTablet()) {
                    this.mTipText.setText(2131628945);
                } else {
                    this.mTipText.setText(2131628958);
                }
                this.mTipText.setVisibility(0);
            } else {
                this.mTipText.setText(2131628956);
                this.mTipText.setVisibility(0);
            }
            this.mSubTipText.setVisibility(8);
        }
        this.mEnrollLayout.setVisibility(8);
        this.mIntroEnrollLayout.setVisibility(0);
        if (this.mHwCustFingerprintEnroll != null && this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            this.mRenameOrRestartBtn.setVisibility(8);
            this.mOkBtn.setVisibility(8);
            this.mTipDownText.setVisibility(0);
            this.mHwCustFingerprintEnroll.handleMessage(this.mHandler.obtainMessage(-1));
        }
        if (NaviUtils.isFrontFingerNaviEnabled()) {
            this.mFpTipImageBg.setImageResource(2130838164);
        } else {
            this.mFpTipImageBg.setImageResource(2130837616);
        }
        this.mEnrollTipAnime.start();
        this.mEnrollBottomLayout.setVisibility(8);
        this.mRenameOrRestartBtn.setVisibility(8);
        this.mOkBtn.setVisibility(8);
        this.mCurrentProgress = null;
        this.mEnrollFinished = false;
        this.mHasDuplicated = false;
        this.mResumableFailure = 0;
        this.mCurrentPhase = 0;
    }

    private void handleEnrollSuccess() {
        reportFpEnroll(true);
        Log.i("FingerprintEnrollFragment", "FP_TIP_SUCCESS success!");
        if (this.mHwCustFingerprintEnroll != null && this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            this.mHwCustFingerprintEnroll.handleMessage(this.mHandler.obtainMessage(100));
        }
        renameEnrolledFinger();
        this.mEnrollFinished = true;
        this.mTitleText.setText(this.mRes.getString(2131627639));
        if (this.mIsPrivateUser) {
            this.mFpName = this.mUserInfo.name + this.mRes.getString(2131627616);
        }
        this.mTipText.setText(String.format(this.mRes.getString(2131627648), new Object[]{this.mFpName}));
        this.mTipText.setVisibility(0);
        this.mSubTipText.setVisibility(4);
        if (this.mHwCustFingerprintEnroll == null || !this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            this.mFpImageBg.clearAnimation();
            this.mFpImageBg.setVisibility(8);
            this.mFpImage.setImageDrawable(this.mRes.getDrawable(EnrollAnimeRes.getCompleteFrameRes()));
            this.mTipDownText.setVisibility(8);
            this.mTipImproveDownText.setVisibility(8);
        }
        this.mEnrollBottomLayout.setVisibility(8);
        this.mRenameOrRestartBtn.setText(this.mRes.getString(2131627628));
        this.mRenameOrRestartBtn.setContentDescription(this.mRes.getString(2131627617));
        this.mOkBtn.setText(this.mRes.getString(2131627651));
        this.mOkBtn.setVisibility(0);
        this.mRenameOrRestartBtn.setVisibility(0);
        dismissDialog();
        this.mCurrentPhase = 5;
    }

    private void handleEnrollStep(int enrollStep) {
        Log.i("FingerprintEnrollFragment", "handleEnrollStep enrollStep = " + enrollStep + ", currentPhase = " + this.mCurrentPhase);
        this.mTitleText.setTextColor(this.mContext.getResources().getColor(2131427330));
        if (enrollStep > 0) {
            Log.d("FingerprintEnrollFragment", "handleEnrollStep stop release, enrollStep = " + enrollStep + ", to be stopped = " + (enrollStep - 1));
            this.mReleaseAnimes[enrollStep - 1].stop();
        }
        this.mTipImproveDownText.setVisibility(8);
        this.mEnrollTipAnime.stop();
        this.mIntroEnrollLayout.setVisibility(8);
        this.mEnrollLayout.setVisibility(0);
        this.mFpImageBg.setVisibility(8);
        this.mEnrollBottomLayout.setVisibility(8);
        this.mPressAnimes[enrollStep].start();
        dealTextSwitchEnrollStep();
    }

    private void handleSupplementStep() {
        Log.i("FingerprintEnrollFragment", "handleSupplementStep current step = " + this.mCurrentProgress.currentStep + ", direction = " + this.mCurrentProgress.guideDirection);
        this.mFpImageBg.clearAnimation();
        this.mFpImageBg.setVisibility(4);
        this.mFpImage.setImageResource(EnrollAnimeRes.getCompleteFrameRes());
        this.mFpImage.setVisibility(0);
        if (this.mResumableFailure != 0) {
            switchTipToText(2131627913);
            this.mResumableFailure = 0;
        }
    }

    private boolean isIgnoreTipSwitchInInitPhase(int textSwitchStep) {
        if (textSwitchStep == 0 && textSwitchStep == this.mCurrentProgress.currentStep) {
            return this.mCurrentPhase == 1;
        } else {
            return false;
        }
    }

    private boolean shouldDirectSwitchToEdgePhase(int textSwitchStep) {
        if (this.mCurrentPhase == 0 && textSwitchStep == 0 && textSwitchStep == this.mCurrentProgress.currentStep) {
            return true;
        }
        return false;
    }

    private void dealTextSwitchEnrollStep() {
        int textSwitchStep = getTextSwitchStep();
        Log.i("FingerprintEnrollFragment", "dealTextSwitchEnrollStep mCurrentPhase = " + this.mCurrentPhase + ", currentStep = " + getCurrentStep() + ", mResumableFailure = " + this.mResumableFailure + ", textSwitchStep = " + textSwitchStep);
        if (this.mCurrentPhase == 0) {
            if (textSwitchStep != 0) {
                switchTitleToText(2131627691);
            } else {
                switchTitleToText(2131627692);
            }
            switchTipToText(2131628678);
        } else if (!(this.mResumableFailure == 0 || isIgnoreTipSwitchInInitPhase(textSwitchStep))) {
            switchTipToText(2131628678);
        }
        if (this.mCurrentProgress.currentStep > textSwitchStep) {
            this.mCurrentPhase = 3;
        } else if (shouldDirectSwitchToEdgePhase(textSwitchStep)) {
            this.mCurrentPhase = 3;
        } else {
            this.mCurrentPhase = 2;
        }
        this.mResumableFailure = 0;
    }

    private void dealTextSwitchSupplement() {
        Log.i("FingerprintEnrollFragment", "dealTextSwitchSupplement mCurrentPhase = " + this.mCurrentPhase + ", currentStep = " + getCurrentStep() + ", mResumableFailure = " + this.mResumableFailure);
        if (this.mCurrentPhase != 4) {
            switchTitleToText(2131627912);
            switchTipToText(2131627913);
        }
    }

    private void showSupplementInfo() {
        Integer res = (Integer) SUPPLEMENT_RES.get(Integer.valueOf(this.mCurrentProgress.guideDirection));
        if (res == null) {
            res = Integer.valueOf(2130837707);
        }
        Integer flashRes = (Integer) SUPPLEMENT_RES_FLASH.get(Integer.valueOf(this.mCurrentProgress.guideDirection));
        if (flashRes == null) {
            flashRes = Integer.valueOf(2130837708);
        }
        Integer tipRes = (Integer) SUPPLEMENT_TIP_RES.get(Integer.valueOf(this.mCurrentProgress.guideDirection));
        if (tipRes == null) {
            tipRes = Integer.valueOf(2131627915);
        }
        dealTextSwitchSupplement();
        this.mTipText.setVisibility(0);
        this.mFpImage.setImageResource(res.intValue());
        this.mFpImageBg.setImageResource(flashRes.intValue());
        this.mFpImageBg.setVisibility(0);
        this.mFpImageBg.startAnimation(this.mAlphaAnimation);
        this.mTipDownText.setVisibility(8);
        this.mTipImproveDownText.setVisibility(0);
        this.mTipImproveDownText.setText(this.mRes.getString(tipRes.intValue()));
        this.mCurrentPhase = 4;
    }

    private void dealTextSwitchDuplicated() {
        Log.i("FingerprintEnrollFragment", "dealTextSwitchDuplicated mCurrentPhase = " + this.mCurrentPhase + ", currentStep = " + getCurrentStep());
        if (this.mCurrentPhase != 1) {
            switchTitleToText(2131627691);
            switchTipToText(2131628678);
        } else {
            if (this.mHasDuplicated) {
                this.mTipText.setText(2131628678);
            } else {
                switchTipToText(2131628678);
            }
            this.mTitleText.setText(2131627691);
        }
        this.mCurrentPhase = 1;
    }

    private void handleDuplicateFinger() {
        Log.w("FingerprintEnrollFragment", "FP_TIP_DUPLICATED Enrollment duplicated! phase = " + this.mCurrentPhase + ", currentStep = " + getCurrentStep());
        if (this.mHwCustFingerprintEnroll == null || !this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            this.mTipText.setVisibility(0);
            this.mSubTipText.setVisibility(4);
            this.mTipImproveDownText.setText(this.mRes.getString(2131627662));
            this.mTipImproveDownText.setVisibility(0);
            this.mTipImproveDownText.announceForAccessibility(this.mRes.getString(2131627662));
            stopAnimations();
            this.mFpImage.setVisibility(0);
            this.mFpImage.setImageDrawable(this.mRes.getDrawable(2130837813));
            this.mTipDownText.setVisibility(8);
            this.mIntroEnrollLayout.setVisibility(8);
            this.mEnrollLayout.setVisibility(0);
            this.mHasDuplicated = true;
            dealTextSwitchDuplicated();
            resetEnrollOnDuplicated();
            return;
        }
        this.mHwCustFingerprintEnroll.reset();
        showDuplicatedFpToast();
    }

    private void handleEnrollFailed(Message msg, int errCode) {
        reportFpEnroll(false);
        Log.w("FingerprintEnrollFragment", "FP_TIP_FAIL Enrollment failed! ErrCode = " + errCode + ", currentPhase = " + this.mCurrentPhase + ", currentStep = " + getCurrentStep());
        this.mTitleText.setText(this.mRes.getString(2131627640));
        this.mTitleText.setTextColor(-65536);
        if (this.mHwCustFingerprintEnroll != null && this.mHwCustFingerprintEnroll.isFrontFingerPrint()) {
            this.mHwCustFingerprintEnroll.handleMessage(msg);
        }
        if (errCode == 1) {
            Log.w("FingerprintEnrollFragment", "Fingerprint enrollment timeout!");
            this.mTipText.setVisibility(4);
            this.mSubTipText.setVisibility(4);
            handleEnrollTimeout();
        } else {
            this.mTipText.setText(String.format(this.mRes.getString(2131627649), new Object[]{this.mFpName}));
            this.mTipText.setVisibility(0);
            this.mSubTipText.setVisibility(0);
            this.mSubTipText.setText(this.mRes.getString(2131627650));
        }
        if (this.mHwCustFingerprintEnroll == null || !this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
            clearAnimations();
            this.mFpImage.setVisibility(0);
            this.mFpImage.setImageDrawable(this.mRes.getDrawable(2130837813));
            this.mEnrollBottomLayout.setVisibility(8);
            this.mIntroEnrollLayout.setVisibility(8);
            this.mTipDownText.setVisibility(8);
            this.mTipImproveDownText.setVisibility(8);
        } else {
            this.mHwCustFingerprintEnroll.reset();
        }
        this.mRenameOrRestartBtn.setContentDescription(this.mRes.getString(2131627618));
        this.mRenameOrRestartBtn.setText(this.mRes.getString(2131627661));
        this.mOkBtn.setText(this.mRes.getString(2131627651));
        this.mRenameOrRestartBtn.setVisibility(0);
        this.mOkBtn.setVisibility(0);
        if (this.mBm != null) {
            this.mBm.abort();
        }
        this.mResumableFailure = 0;
        this.mCurrentPhase = 6;
    }

    private void renameEnrolledFinger() {
        if (this.mBm != null) {
            Fingerprint curFp = getEnrolledFingerprint();
            if (curFp != null) {
                this.mBm.renameFingerprint(curFp.getFingerId(), this.mFpName, this.mUserId);
                this.mFpId = curFp.getFingerId();
                boolean ret = Secure.putInt(this.mContext.getContentResolver(), String.format("fp_index_%d", new Object[]{Integer.valueOf(curFp.getFingerId())}), this.mFpModeIndex);
                Log.i("FingerprintEnrollFragment", "Fingerprint enrolled successfully, ID = " + curFp.getFingerId() + ", name = " + this.mFpName + ", DB return = " + ret + ", index = " + this.mFpModeIndex);
                if (!ret) {
                    Log.e("FingerprintEnrollFragment", "Failed to save fingerprint index, id = " + curFp.getFingerId() + ", name = " + this.mFpName);
                    return;
                }
                return;
            }
            Log.e("FingerprintEnrollFragment", "Failed to get enrolled fingerprint, rename failed.");
        }
    }

    private boolean isInitPhaseRepeat(EnrollProgress progress) {
        if (this.mCurrentProgress != null && this.mCurrentProgress.currentStep == 0 && progress.currentStep == this.mCurrentProgress.currentStep) {
            return this.mCurrentPhase == 1;
        } else {
            return false;
        }
    }

    private int getCurrentStep() {
        return this.mCurrentProgress != null ? this.mCurrentProgress.currentStep : -1;
    }

    private boolean shouldResetEnrollState(EnrollProgress progress) {
        if (this.mCurrentProgress != null && progress.currentStep != 0) {
            return false;
        }
        if (this.mCurrentPhase == 0 || this.mCurrentPhase == 1 || this.mCurrentPhase == 6) {
            return true;
        }
        Log.e("FingerprintEnrollFragment", "Unexpected state, mCurrentPhase = " + this.mCurrentPhase + ", progress.currentStep = " + progress.currentStep + ", mCurrentProgress = " + getCurrentStep());
        return false;
    }

    public void onProgress(EnrollProgress progress) {
        if (progress.totalSteps < 6) {
            Log.e("FingerprintEnrollFragment", "invalid totalSteps = " + progress.totalSteps);
        } else if (progress.currentStep >= 0) {
            int persent = progress.percents;
            Log.i("FingerprintEnrollFragment", "persent = " + persent);
            if (this.mHwCustFingerprintEnroll == null || !this.mHwCustFingerprintEnroll.isSupportSepFingerPrint()) {
                Log.i("FingerprintEnrollFragment", "onProgress mCurrentProgress = " + this.mCurrentProgress + ", currentStep = " + getCurrentStep() + ", progress.currentStep = " + progress.currentStep + ", mCurrentPhase = " + this.mCurrentPhase + ", mHasDuplicated = " + this.mHasDuplicated);
                if (isInitPhaseRepeat(progress)) {
                    Log.w("FingerprintEnrollFragment", "step 0 repeat in INIT_PHASE, do not handle.");
                    return;
                }
                if (shouldResetEnrollState(progress)) {
                    resetEnroll(progress.totalSteps);
                    Log.i("FingerprintEnrollFragment", "onProgress reset. mMaxEnrollSteps = " + this.mMaxEnrollSteps);
                }
                dispatchEnrollMessage(progress);
                return;
            }
            Log.i("FingerprintEnrollFragment", "onProgress");
            this.mHandler.sendEmptyMessage(persent);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case 2131886612:
                if (this.mRes.getString(2131627617).equals(this.mRenameOrRestartBtn.getContentDescription())) {
                    showRenameDialog();
                    return;
                } else {
                    restartEnroll();
                    return;
                }
            case 2131886613:
                if (SettingsExtUtils.isStartupGuideMode(this.mContext.getContentResolver())) {
                    Log.e("FingerprintEnrollFragment", "FingerprintEnrollFragment settings the result as OK.");
                    ((Activity) this.mContext).setResult(-1);
                }
                finish();
                return;
            case 2131886615:
                Log.i("FingerprintEnrollFragment", "Fingerprint enrollment cancled by user.");
                ((Activity) this.mContext).setResult(0);
                finish();
                return;
            case 2131886629:
                Log.e("FingerprintEnrollFragment", "FingerprintEnrollFragment settings the result as CANCELED.");
                ((Activity) this.mContext).setResult(0);
                finish();
                return;
            default:
                return;
        }
    }

    private void showRenameDialog() {
        View renameView = LayoutInflater.from(this.mContext).inflate(2130968924, null);
        TextView tv = (TextView) renameView.findViewById(16908299);
        if (tv != null) {
            tv.setVisibility(8);
        }
        final EditText ev = (EditText) renameView.findViewById(16908291);
        Builder builder = new Builder(this.mContext);
        builder.setTitle(2131627653);
        ev.setText(this.mFpName);
        ev.setSelectAllOnFocus(true);
        ev.requestFocus();
        builder.setView(renameView);
        builder.setPositiveButton(2131627651, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                FingerprintEnrollFragment.this.mFpName = ev.getText().toString().trim();
                if (FingerprintEnrollFragment.this.mBm != null) {
                    Fingerprint curFp = FingerprintEnrollFragment.this.getEnrolledFingerprint();
                    if (curFp != null) {
                        FingerprintEnrollFragment.this.mBm.renameFingerprint(curFp.getFingerId(), FingerprintEnrollFragment.this.mFpName, FingerprintEnrollFragment.this.mUserId);
                    } else {
                        Log.e("FingerprintEnrollFragment", "Failed to get enrolled fingerprint, rename failed.");
                    }
                }
                dialog.dismiss();
                if (SettingsExtUtils.isStartupGuideMode(FingerprintEnrollFragment.this.mContext.getContentResolver())) {
                    FingerprintEnrollFragment.this.mTipText.setText(String.format(FingerprintEnrollFragment.this.mRes.getString(2131627648), new Object[]{FingerprintEnrollFragment.this.mFpName}));
                    FingerprintEnrollFragment.this.mTipText.setVisibility(0);
                    return;
                }
                FingerprintEnrollFragment.this.finish();
            }
        });
        builder.setNegativeButton(2131627652, null);
        AlertDialog alert = builder.create();
        this.nameAlertDialog = alert;
        ev.addTextChangedListener(this.nameTextWatcher);
        alert.getWindow().setSoftInputMode(5);
        alert.show();
        alert.getButton(-1).setEnabled(false);
    }

    private boolean isDuplicatedFpName(String fpName) {
        if (this.mBm == null || fpName == null) {
            return false;
        }
        List<Fingerprint> fps = this.mBm.getFingerprintList(this.mUserId);
        if (fps.isEmpty()) {
            return false;
        }
        for (Fingerprint fp : fps) {
            if (fpName.equals(fp.getName())) {
                if (this.mDuplicatedNameToast != null) {
                    this.mDuplicatedNameToast.cancel();
                }
                this.mDuplicatedNameToast = Toast.makeText(this.mContext, 2131627923, 0);
                this.mDuplicatedNameToast.show();
                return true;
            }
        }
        return false;
    }

    private void showDuplicatedFpToast() {
        if (this.mDuplicatedFpTip != null) {
            this.mDuplicatedFpTip.cancel();
        }
        this.mDuplicatedFpTip = Toast.makeText(this.mContext, this.mRes.getString(2131627662), 0);
        this.mDuplicatedFpTip.show();
    }

    private void dismissDialog() {
        if (this.mImageLowQualityDialog != null) {
            this.mImageLowQualityDialog.dismiss();
        }
        if (this.mFingerLowCovergeDialog != null) {
            this.mFingerLowCovergeDialog.dismiss();
        }
        if (this.mFingerRegionChangeDialog != null) {
            this.mFingerRegionChangeDialog.dismiss();
        }
        if (this.mFingerStillDialog != null) {
            this.mFingerStillDialog.dismiss();
        }
        if (this.mTimeoutDialog != null && this.mTimeoutDialog.isShowing()) {
            this.mTimeoutDialog.dismiss();
        }
    }

    private void finish() {
        if (this.mEnrollFinished && this.mFpId != 0) {
            Intent data = new Intent();
            data.putExtra("fp_id", this.mFpId);
            ((Activity) this.mContext).setResult(-1, data);
            ((FingerprintEnrollActivity) this.mContext).setNeedRealFinishResult(true);
        }
        ((Activity) this.mContext).finish();
    }

    private void restartEnroll() {
        if (this.mBm != null) {
            this.mBm.setCaptureCallback(this);
            this.mBm.startEnroll(this, this.mToken, this.mUserId);
            Log.d("FingerprintEnrollFragment", "click restartEnroll!");
            this.mHandler.sendEmptyMessage(1101);
        }
    }

    protected void releaseAd(AnimationDrawable ad) {
        if (ad != null) {
            ad.stop();
            ad.setCallback(null);
        }
    }

    private boolean containFp(List<Fingerprint> fpList, Fingerprint fpSpec) {
        for (Fingerprint fp : fpList) {
            if (fp.getFingerId() == fpSpec.getFingerId()) {
                return true;
            }
        }
        return false;
    }

    private Fingerprint getEnrolledFingerprint() {
        if (this.mBm == null || this.mCurrentFpList == null) {
            return null;
        }
        List<Fingerprint> updatedFpList = this.mBm.getFingerprintList(this.mUserId);
        if (updatedFpList.isEmpty()) {
            return null;
        }
        Fingerprint fpRet = null;
        int count = 0;
        for (Fingerprint fp : updatedFpList) {
            if (!containFp(this.mCurrentFpList, fp)) {
                count++;
                fpRet = fp;
            }
        }
        if (count <= 0 || count > 1) {
            return null;
        }
        return fpRet;
    }

    private int getMaxFingerprintIndex() {
        if (this.mBm == null) {
            return -1;
        }
        int max = -1;
        for (Fingerprint fp : this.mBm.getFingerprintList(this.mUserId)) {
            int index = Secure.getInt(this.mContext.getContentResolver(), String.format("fp_index_%d", new Object[]{Integer.valueOf(fp.getFingerId())}), -1);
            if (index == -1) {
                Log.e("FingerprintEnrollFragment", "Fingerprint index missing! ID = " + fp.getFingerId() + ", name = " + fp.getName());
            }
            if (index > max) {
                max = index;
            }
        }
        return max;
    }

    private FingerprintInfo getProperFingerprintInfo(int index) {
        int curIndex = index;
        String curName = "";
        if (this.mIsPrivateUser) {
            curName = this.mUserInfo.name + this.mContext.getString(2131627616);
        } else {
            curName = String.format(this.mRes.getString(2131628066, new Object[]{Integer.valueOf(index)}), new Object[0]);
            for (Fingerprint fp : this.mBm.getFingerprintList(this.mUserId)) {
                if (curName.equals(fp.getName())) {
                    curIndex++;
                    curName = String.format(this.mRes.getString(2131628066, new Object[]{Integer.valueOf(curIndex)}), new Object[0]);
                }
            }
        }
        return new FingerprintInfo(curIndex, curName);
    }

    private void generateFingerprintInfo() {
        int index = getMaxFingerprintIndex();
        Log.d("FingerprintEnrollFragment", "Max current fingerprint index = " + this.mFpModeIndex);
        if (index == -1) {
            index = 1;
        } else {
            index++;
        }
        FingerprintInfo info = getProperFingerprintInfo(index);
        this.mFpModeIndex = info.fpIndex;
        this.mFpName = info.fpName;
        Log.d("FingerprintEnrollFragment", "Final fingerprint index = " + this.mFpModeIndex + ", name = " + this.mFpName);
    }

    private void handleEnrollTimeout() {
        if (isAdded()) {
            if (this.mTimeoutDialog == null) {
                Builder builder = new Builder(this.mContext);
                builder.setTitle(2131628180);
                builder.setMessage(2131628181);
                builder.setPositiveButton(2131627651, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FingerprintEnrollFragment.this.getActivity().setResult(100);
                        FingerprintEnrollFragment.this.finish();
                    }
                });
                builder.setCancelable(false);
                this.mTimeoutDialog = builder.create();
            }
            if (!this.mTimeoutDialog.isShowing()) {
                this.mTimeoutDialog.show();
            }
        }
    }

    private void reportFpEnroll(boolean success) {
        String name;
        String status;
        if (success) {
            if (SettingsExtUtils.isStartupGuideMode(this.mContext.getContentResolver())) {
                name = "setupwizard_fp_enroll_success";
            } else {
                name = "fp_enroll_success";
            }
        } else if (SettingsExtUtils.isStartupGuideMode(this.mContext.getContentResolver())) {
            name = "setupwizard_fp_enroll_failure";
        } else {
            name = "fp_enroll_failure";
        }
        if (PrivacySpaceSettingsHelper.isPrivacyUser(this.mContext, this.mUserId)) {
            status = "privacy space";
        } else {
            status = String.valueOf(this.mUserId);
        }
        ItemUseStat.getInstance().handleClick(this.mContext, 2, name, status);
    }
}
