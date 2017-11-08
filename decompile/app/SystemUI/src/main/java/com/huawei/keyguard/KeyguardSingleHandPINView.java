package com.huawei.keyguard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import com.android.keyguard.KeyguardPINView;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$id;
import com.huawei.keyguard.events.EventCenter;
import com.huawei.keyguard.events.EventCenter.IContentListener;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.SingleHandAdapter;
import com.huawei.keyguard.util.SingleHandAdapter.UpdateSinglehandView;
import com.huawei.keyguard.util.SingleHandUtils;
import com.huawei.keyguard.view.widget.GravityView;

public class KeyguardSingleHandPINView extends KeyguardPINView implements OnClickListener, UpdateSinglehandView, IContentListener {
    private SingleHandAdapter mAdapter;
    private boolean mHasWindowFocus;
    private ImageButton mLeftModeButton;
    private float mNormalHeight;
    private float mNormalWidth;
    private GravityView mNumPadView;
    private ImageButton mRightModeButton;
    private float mSingleHandHeight;
    private float mSingleHandWidth;
    private int mSinglehandMode;
    private boolean mSinglehandSwitch;
    private float mTranslationX;

    public KeyguardSingleHandPINView(Context context) {
        this(context, null);
    }

    public KeyguardSingleHandPINView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mLeftModeButton = null;
        this.mRightModeButton = null;
        this.mSinglehandMode = 0;
        this.mSinglehandSwitch = false;
        this.mNumPadView = null;
        this.mHasWindowFocus = true;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mAdapter = new SingleHandAdapter(this.mContext, this);
        this.mNormalWidth = (float) getResources().getDisplayMetrics().widthPixels;
        this.mSingleHandWidth = getResources().getDimension(R$dimen.single_hand_pin_numpad_width);
        this.mSingleHandHeight = getResources().getDimension(R$dimen.single_hand_pin_numpad_height);
        this.mNormalHeight = getResources().getDimension(R$dimen.keyguard_pin_numpad_height);
        this.mTranslationX = getResources().getDimension(R$dimen.keyguard_pin_mode_btn_width);
        configSinglehandArea();
    }

    private void configSinglehandArea() {
        this.mLeftModeButton = (ImageButton) findViewById(R$id.switch_mode_left);
        this.mRightModeButton = (ImageButton) findViewById(R$id.switch_mode_right);
        this.mNumPadView = (GravityView) findViewById(R$id.gravity_pin_numpad);
        if (this.mAdapter != null) {
            this.mAdapter.setAnimatedViews(this.mLeftModeButton, this.mRightModeButton, this.mNumPadView);
        }
        if (this.mLeftModeButton == null || this.mRightModeButton == null) {
            HwLog.e("KeyguardSingleHandPINView", "Some of the required single hand views doesn't exist!");
            return;
        }
        onUpdateSinglehandView(SingleHandUtils.isSingleHandOpen(this.mContext), SingleHandUtils.getSingleHandMode(this.mContext), false);
        this.mLeftModeButton.setOnClickListener(this);
        this.mRightModeButton.setOnClickListener(this);
    }

    public void onClick(View v) {
        doHapticKeyClick();
        if (this.mCallback != null) {
            this.mCallback.userActivity();
        }
        if (v.getId() == R$id.switch_mode_left) {
            onUpdateSinglehandView(true, 1, true);
        } else if (v.getId() == R$id.switch_mode_right) {
            onUpdateSinglehandView(true, 2, true);
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

    public int getSinglehandStatus() {
        return this.mSinglehandMode;
    }
}
