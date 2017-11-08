package com.android.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.systemui.R;
import com.android.systemui.utils.SystemUiUtil;
import com.huawei.telephony.HuaweiTelephonyManagerCustEx;

public class SignalUnitVSimView extends SignalUnitNormalView {
    int mVSimActivityId;
    private ImageView mVSimDataActivity;
    private ImageView mVSimDataType;
    int mVSimDataTypeId;
    private ImageView mVSimSignal;
    int mVSimStrengthId;

    public SignalUnitVSimView(Context context) {
        this(context, null);
    }

    public SignalUnitVSimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalUnitVSimView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mVSimStrengthId = -1;
        this.mVSimDataTypeId = -1;
        this.mVSimActivityId = -1;
        this.mVSimStrengthId = R.drawable.stat_sys_tjt_signal_0;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mVSimSignal = (ImageView) findViewById(R.id.vsim_signal);
        this.mVSimDataType = (ImageView) findViewById(R.id.vsim_type);
        this.mVSimDataActivity = (ImageView) findViewById(R.id.vsim_inout);
    }

    protected void onDetachedFromWindow() {
        this.mVSimSignal = null;
        this.mVSimDataType = null;
        this.mVSimDataActivity = null;
        super.onDetachedFromWindow();
    }

    public void setMobileSinalData(int strengthIcon, int mobileTypeIcon, int mobileActIcon) {
        if (SystemUiUtil.isSupportVSim()) {
            this.mVSimStrengthId = strengthIcon;
            this.mVSimDataTypeId = mobileTypeIcon;
            this.mVSimActivityId = mobileActIcon;
            if (HuaweiTelephonyManagerCustEx.getVSimCurCardType() != 2) {
                this.mVSimActivityId = 0;
            }
            refreshView();
        }
    }

    void refreshView() {
        if (SystemUiUtil.isSupportVSim()) {
            updateView(this.mVSimSignal, this.mVSimStrengthId);
            updateView(this.mVSimDataType, this.mVSimDataTypeId);
            updateView(this.mVSimDataActivity, this.mVSimActivityId);
        }
    }
}
