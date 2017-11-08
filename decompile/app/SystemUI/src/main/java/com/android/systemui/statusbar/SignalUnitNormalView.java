package com.android.systemui.statusbar;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.HwTelephonyIcons;
import com.android.systemui.statusbar.policy.NetWorkUtils;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;
import com.huawei.cust.HwCustUtils;

public class SignalUnitNormalView extends LinearLayout {
    protected static final boolean IS_FIVE_SIGNAL = SystemProperties.getBoolean("ro.config.hw_show_5_sigbar", true);
    boolean IS_SHOW_BUSY_ICON;
    private HwCustSignalUnitNormalView mCustSignalUnitView;
    boolean mDataPosFixed;
    int mInetCon;
    boolean mIsRoam;
    boolean mIsSuspend;
    int mMarsterStrengthLevel;
    int mMobileActivityId;
    ImageView mMobileDataActivity;
    ViewGroup mMobileDataGroup;
    ImageView mMobileRoam;
    ImageView mMobileSignal;
    int mMobileStrengthId;
    ImageView mMobileType;
    int mMobileTypeId;
    int mNetworkType;
    int mSubscription;
    int mType;

    public SignalUnitNormalView(Context context) {
        this(context, null);
    }

    public SignalUnitNormalView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalUnitNormalView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mInetCon = 0;
        this.mMarsterStrengthLevel = 0;
        this.mDataPosFixed = SystemProperties.getBoolean("ro.config.sysui.datapos", false);
        this.mSubscription = 0;
        this.mMobileStrengthId = -1;
        this.mMobileTypeId = -1;
        this.mMobileActivityId = -1;
        this.mIsRoam = false;
        this.mNetworkType = -1;
        this.mType = 0;
        this.IS_SHOW_BUSY_ICON = SystemProperties.getBoolean("ro.config.show_busyicon", true);
        this.mIsSuspend = false;
        this.mCustSignalUnitView = (HwCustSignalUnitNormalView) HwCustUtils.createObj(HwCustSignalUnitNormalView.class, new Object[]{this});
        this.mMobileStrengthId = R.drawable.stat_sys_signal_0;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mMobileSignal = (ImageView) findViewById(R.id.mobile_signal);
        this.mMobileType = (ImageView) findViewById(R.id.mobile_type);
        this.mMobileDataActivity = (ImageView) findViewById(R.id.mobile_inout);
        this.mMobileDataGroup = (ViewGroup) findViewById(R.id.mobile_data);
        this.mMobileRoam = (ImageView) findViewById(R.id.mobile_roam);
        refreshView();
        Intent intent = new Intent("com.android.systemui.signalclusterview.onAttach");
        intent.setPackage(getContext().getPackageName());
        getContext().sendBroadcast(intent);
    }

    protected void onDetachedFromWindow() {
        this.mMobileSignal = null;
        this.mMobileType = null;
        this.mMobileDataActivity = null;
        this.mMobileDataGroup = null;
        this.mMobileRoam = null;
        super.onDetachedFromWindow();
    }

    public void setMobileSinalData(int strengthIcon, int mobileTypeIcon, int mobileActIcon) {
        if (R.drawable.stat_sys_no_sim == strengthIcon || strengthIcon == R.drawable.stat_sys_signal_null || !IS_FIVE_SIGNAL) {
            this.mMobileStrengthId = strengthIcon;
        } else {
            this.mMobileStrengthId = HwTelephonyIcons.getFiveSignalIcons(this.mInetCon, this.mMarsterStrengthLevel);
        }
        this.mMobileTypeId = mobileTypeIcon;
        if (this.mMobileActivityId != mobileActIcon) {
            HwLog.i("SignalUnitNormalView", "mobileActIcon = " + mobileActIcon);
        }
        this.mMobileActivityId = mobileActIcon;
        refreshView();
    }

    public void setExtData(int sub, int inetCon, boolean isRoam, boolean isSuspend, int[] extArgs) {
        this.mInetCon = inetCon;
        this.mSubscription = sub;
        this.mIsRoam = isRoam;
        this.mIsSuspend = isSuspend;
        if (extArgs.length > 0) {
            this.mNetworkType = extArgs[0];
        }
        if (extArgs.length > 1) {
            this.mMarsterStrengthLevel = extArgs[1];
        }
    }

    void updateView(ImageView targetView, int value) {
        if (targetView == null) {
            return;
        }
        if (value > 0) {
            targetView.setVisibility(0);
            targetView.setImageResource(value);
            return;
        }
        targetView.setVisibility(8);
    }

    void refreshView() {
        updateStrengthId();
        updateView(this.mMobileSignal, this.mMobileStrengthId);
        updateMobileType();
        if (this.mCustSignalUnitView != null) {
            this.mCustSignalUnitView.dualCardNetworkBooster();
        }
        if (!this.mDataPosFixed) {
            updateView(this.mMobileDataActivity, this.mMobileActivityId);
        } else if (this.mMobileDataGroup != null) {
            this.mMobileDataGroup.setVisibility(8);
        }
        if (!(IS_FIVE_SIGNAL || this.mMobileType == null || this.mMobileDataActivity == null || 8 != this.mMobileType.getVisibility())) {
            this.mMobileDataActivity.setVisibility(8);
        }
        if (this.mIsRoam) {
            updateView(this.mMobileRoam, R.drawable.stat_sys_data_connected_roam);
        } else {
            updateView(this.mMobileRoam, 0);
        }
    }

    private void updateStrengthId() {
        boolean isSkytone = SystemUiUtil.isSupportVSim() && this.mSubscription == NetWorkUtils.getVSimSubId();
        if (this.mMarsterStrengthLevel >= 0 && isSkytone) {
            int fiveTJTSignalIcons;
            if (IS_FIVE_SIGNAL) {
                fiveTJTSignalIcons = HwTelephonyIcons.getFiveTJTSignalIcons(this.mInetCon, this.mMarsterStrengthLevel);
            } else {
                fiveTJTSignalIcons = NetWorkUtils.getTjtIcons(this.mInetCon, this.mMarsterStrengthLevel);
            }
            this.mMobileStrengthId = fiveTJTSignalIcons;
        }
    }

    void updateMobileType() {
        boolean isSkytone = SystemUiUtil.isSupportVSim() && this.mSubscription == NetWorkUtils.getVSimSubId();
        boolean isShowBusyIcon = this.mIsSuspend && this.IS_SHOW_BUSY_ICON && !isSkytone;
        updateView(this.mMobileType, isShowBusyIcon ? R.drawable.stat_sys_signal_type_waiting : this.mMobileTypeId);
    }

    int getMobileActivityIconId(boolean activityIn, boolean activityOut) {
        if (activityIn && activityOut) {
            return R.drawable.stat_sys_signal_inout;
        }
        if (activityIn) {
            return R.drawable.stat_sys_signal_in;
        }
        if (activityOut) {
            return R.drawable.stat_sys_signal_out;
        }
        return R.drawable.single_stat_sys_signal_connected;
    }
}
