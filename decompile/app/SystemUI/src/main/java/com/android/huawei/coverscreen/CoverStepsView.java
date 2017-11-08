package com.android.huawei.coverscreen;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.keyguard.R$id;
import com.huawei.keyguard.data.StepCounterInfo;
import com.huawei.keyguard.events.HwUpdateMonitor;
import com.huawei.keyguard.events.HwUpdateMonitor.HwUpdateCallback;
import com.huawei.keyguard.util.HwUnlockUtils;
import fyusion.vislib.BuildConfig;

public class CoverStepsView extends RelativeLayout {
    private Handler mHandler;
    private ImageView mImageViewRunner;
    private TextView mTextViewSteps;
    HwUpdateCallback mUpdateCallback;

    public CoverStepsView(Context context) {
        this(context, null);
    }

    public CoverStepsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverStepsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mUpdateCallback = new HwUpdateCallback() {
            public void onStepCounterChange(StepCounterInfo info) {
                if (info == null) {
                    Log.i("CoverStepsView", "onStepCounterChange info is null - no change happened!");
                    return;
                }
                Log.i("CoverStepsView", "onStepCounterChange StepCounterInfo info" + info);
                Message message = CoverStepsView.this.mHandler.obtainMessage(1002);
                message.obj = info;
                CoverStepsView.this.mHandler.sendMessage(message);
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1002:
                        if (msg.obj instanceof StepCounterInfo) {
                            StepCounterInfo si = msg.obj;
                            if (!si.getEnableCounterChanged()) {
                                CoverStepsView.this.onStepsChange(si.getStepsCount());
                                break;
                            }
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.i("CoverStepsView", " onFinishInflate");
        View v = findViewById(R$id.cover_steps_textview);
        if (v instanceof TextView) {
            this.mTextViewSteps = (TextView) v;
        }
        v = findViewById(R$id.cover_steps_imageview);
        if (v instanceof ImageView) {
            this.mImageViewRunner = (ImageView) v;
        }
    }

    protected void onAttachedToWindow() {
        int i = 0;
        super.onAttachedToWindow();
        Log.i("CoverStepsView", "onAttachedToWindow");
        HwUpdateMonitor.getInstance(getContext()).registerCallback(this.mUpdateCallback);
        if (this.mTextViewSteps == null || this.mImageViewRunner == null) {
            Log.w("CoverStepsView", "onAttachedToWindow, mTextViewSteps = " + this.mTextViewSteps + ", mImageViewRunner = " + this.mImageViewRunner);
            return;
        }
        int i2;
        boolean bStepEnable = HwUnlockUtils.isStepCounterEnabled(getContext()).booleanValue();
        ImageView imageView = this.mImageViewRunner;
        if (bStepEnable) {
            i2 = 0;
        } else {
            i2 = 8;
        }
        imageView.setVisibility(i2);
        TextView textView = this.mTextViewSteps;
        if (!bStepEnable) {
            i = 8;
        }
        textView.setVisibility(i);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        HwUpdateMonitor.getInstance(getContext()).unRegisterCallback(this.mUpdateCallback);
    }

    public void onStepsChange(int step) {
        if (this.mTextViewSteps == null) {
            Log.w("CoverStepsView", "onStepNumChange, mSteps is null!");
            return;
        }
        Log.i("CoverStepsView", "onStepCounterChange StepCounterInfo step =" + step);
        if (HwUnlockUtils.isStepCounterEnabled(getContext()).booleanValue() && this.mImageViewRunner.getVisibility() != 0) {
            this.mImageViewRunner.setVisibility(0);
        }
        this.mTextViewSteps.setText(BuildConfig.FLAVOR + step);
    }
}
