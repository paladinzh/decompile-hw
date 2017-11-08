package com.huawei.keyguard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import com.android.keyguard.KeyguardPatternView;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$id;
import com.huawei.keyguard.events.EventCenter;
import com.huawei.keyguard.events.EventCenter.IContentListener;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.SingleHandAdapter;
import com.huawei.keyguard.util.SingleHandAdapter.UpdateSinglehandView;
import com.huawei.keyguard.util.SingleHandUtils;
import com.huawei.keyguard.view.widget.GravityView;

public class KeyguardSingleHandPatternView extends KeyguardPatternView implements OnClickListener, UpdateSinglehandView, IContentListener {
    private SingleHandAdapter mAdapter;
    private boolean mEnableHaptics;
    private boolean mHasWindowFocus;
    private View mLeftHolderView;
    private ImageButton mLeftModeButton;
    private float mNormalHeight;
    private float mNormalWidth;
    private GravityView mNumPadView;
    private ImageButton mRightModeButton;
    private float mSingleHandHeight;
    private float mSingleHandMidWidth;
    private float mSingleHandWidth;
    private int mSinglehandMode;
    private boolean mSinglehandSwitch;
    private float mTranslationX;

    public KeyguardSingleHandPatternView(Context context) {
        this(context, null);
    }

    public KeyguardSingleHandPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mLeftModeButton = null;
        this.mRightModeButton = null;
        this.mLeftHolderView = null;
        this.mSinglehandMode = 0;
        this.mSinglehandSwitch = false;
        this.mEnableHaptics = false;
        this.mNumPadView = null;
        this.mHasWindowFocus = true;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mAdapter = new SingleHandAdapter(this.mContext, this);
        this.mNormalWidth = (float) getResources().getDisplayMetrics().widthPixels;
        this.mNormalHeight = getResources().getDimension(R$dimen.keyguard_security_height);
        this.mSingleHandWidth = getResources().getDimension(R$dimen.single_hand_pin_numpad_width);
        this.mSingleHandHeight = getResources().getDimension(R$dimen.single_hand_pattern_view_height);
        this.mTranslationX = getResources().getDimension(R$dimen.keyguard_pin_mode_btn_width);
        this.mSingleHandMidWidth = getResources().getDimension(R$dimen.single_hand_pattern_view_middle_width);
        this.mEnableHaptics = this.mLockPatternUtils.isTactileFeedbackEnabled();
        configSinglehandArea();
    }

    public int getSinglehandStatus() {
        return this.mSinglehandMode;
    }

    private void configSinglehandArea() {
        this.mLeftModeButton = (ImageButton) findViewById(R$id.switch_mode_left);
        this.mRightModeButton = (ImageButton) findViewById(R$id.switch_mode_right);
        this.mNumPadView = (GravityView) findViewById(R$id.gravity_pin_numpad);
        if (this.mAdapter != null) {
            this.mAdapter.setAnimatedViews(this.mLeftModeButton, this.mRightModeButton, this.mNumPadView);
        }
        this.mLeftHolderView = findViewById(R$id.place_holder_left);
        onUpdateSinglehandView(SingleHandUtils.isSingleHandOpen(this.mContext), SingleHandUtils.getSingleHandMode(this.mContext), false);
        this.mLeftModeButton.setOnClickListener(this);
        this.mRightModeButton.setOnClickListener(this);
    }

    public void onClick(View v) {
        doHapticKeyClick();
        if (this.mCallback != null) {
            this.mCallback.userActivity();
        }
        int viewId = v.getId();
        if (viewId == R$id.switch_mode_left) {
            onUpdateSinglehandView(true, 1, true);
        } else if (viewId == R$id.switch_mode_right) {
            onUpdateSinglehandView(true, 2, true);
        } else if (viewId == R$id.forgot_password_button) {
            HwLog.w("KeyguardSingleHandPatternView", this.mCallback == null ? "Skip showBackupSecurity." : "showBackupSecurity. ");
            if (this.mCallback != null) {
                this.mCallback.showBackupSecurity();
            }
            updateFooter();
        }
    }

    public void onContentChange(boolean selfChange) {
        if (!this.mHasWindowFocus) {
            onUpdateSinglehandView(SingleHandUtils.isSingleHandOpen(this.mContext), SingleHandUtils.getSingleHandMode(this.mContext), true);
        }
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventCenter.getInst().listenContent(2, this);
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventCenter.getInst().stopListenContent(this);
    }

    private void doHapticKeyClick() {
        if (this.mEnableHaptics) {
            performHapticFeedback(1, 3);
        }
    }

    public void onUpdateSinglehandView(boolean isSinglehand, int mode, boolean updateMode) {
        boolean isSingleMode = false;
        if ((isSinglehand != this.mSinglehandSwitch || mode != this.mSinglehandMode) && this.mAdapter != null) {
            if (isSinglehand) {
                if (mode == 1) {
                    isSingleMode = true;
                } else if (mode == 2) {
                    isSingleMode = true;
                }
            }
            if (!isSingleMode) {
                this.mAdapter.initAnimatorSet(0.0f, 0.0f, this.mNormalWidth, this.mNormalHeight, 0.0f);
            } else if (SingleHandUtils.isMediumScreen()) {
                this.mAdapter.initAnimatorSet(0.0f, 0.0f, this.mSingleHandMidWidth, this.mNormalHeight, (float) this.mLeftHolderView.getWidth());
            } else if (mode == 1) {
                this.mAdapter.initAnimatorSet(0.0f, 1.0f, this.mSingleHandWidth, this.mSingleHandHeight, 0.0f);
            } else if (mode == 2) {
                this.mAdapter.initAnimatorSet(1.0f, 0.0f, this.mSingleHandWidth, this.mSingleHandHeight, this.mTranslationX);
            }
            if (this.mAdapter.startAnimatorUpdateView()) {
                if (updateMode) {
                    SingleHandUtils.setSingleHandMode(this.mContext, mode);
                }
                if (SingleHandUtils.isGravitySensorModeOpen(this.mContext)) {
                    this.mAdapter.setModeDirection(mode);
                }
                this.mSinglehandMode = mode;
                this.mSinglehandSwitch = isSinglehand;
            }
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        this.mHasWindowFocus = hasWindowFocus;
        this.mAdapter.setWindowFocus(hasWindowFocus);
    }
}
