package com.android.systemui.statusbar;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.HwNetworkControllerImpl;
import com.android.systemui.statusbar.policy.HwTelephonyIcons;
import com.android.systemui.statusbar.policy.NetWorkUtils;
import com.android.systemui.utils.SystemUiUtil;
import com.huawei.android.util.NoExtAPIException;

public class SignalUnitTelecomView extends SignalUnitNormalView {
    static final int[][] TELEPHONY_SIGNAL_STRENGTH = new int[][]{new int[]{R.drawable.stat_sys_signal_0, R.drawable.stat_sys_signal_1, R.drawable.stat_sys_signal_2, R.drawable.stat_sys_signal_3, R.drawable.stat_sys_signal_4}, new int[]{R.drawable.stat_sys_signal_0_fully, R.drawable.stat_sys_signal_1_fully, R.drawable.stat_sys_signal_2_fully, R.drawable.stat_sys_signal_3_fully, R.drawable.stat_sys_signal_4_fully}};
    static final int[][] TELEPHONY_SIGNAL_STRENGTH_FIVE = new int[][]{new int[]{R.drawable.stat_sys_signal_five_0, R.drawable.stat_sys_signal_five_1, R.drawable.stat_sys_signal_five_2, R.drawable.stat_sys_signal_five_3, R.drawable.stat_sys_signal_five_4, R.drawable.stat_sys_signal_five_5}, new int[]{R.drawable.stat_sys_signal_five_0_fully, R.drawable.stat_sys_signal_five_1_fully, R.drawable.stat_sys_signal_five_2_fully, R.drawable.stat_sys_signal_five_3_fully, R.drawable.stat_sys_signal_five_4_fully, R.drawable.stat_sys_signal_five_5_fully}};
    final int[][] DATA_SIGNAL_STRENGTH;
    final int[][] DATA_SIGNAL_STRENGTH_FIVE;
    final int[][] TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_FIVE_M;
    final int[][] TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_FIVE_S;
    final int[][] TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_M;
    final int[][] TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_S;
    private int mInetCon;
    private ImageView mMobileSignalSlave;
    private int mMobileStrengthSlaveId;
    private ImageView mMobileTypeMaster;
    private int mMobileTypeMasterId;
    private ImageView mMobileTypeRoam;
    private int mMobileTypeRoamId;
    private ImageView mMobileTypeSlave;
    private int mMobileTypeSlaveId;
    private int[] mNetworkDate;

    public SignalUnitTelecomView(Context context) {
        this(context, null);
    }

    public SignalUnitTelecomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalUnitTelecomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mMobileTypeSlaveId = 0;
        this.mMobileStrengthSlaveId = 0;
        this.mMobileTypeMasterId = 0;
        this.mMobileTypeRoamId = 0;
        this.mInetCon = 0;
        this.DATA_SIGNAL_STRENGTH = TELEPHONY_SIGNAL_STRENGTH;
        this.DATA_SIGNAL_STRENGTH_FIVE = TELEPHONY_SIGNAL_STRENGTH_FIVE;
        this.TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_FIVE_M = new int[][]{new int[]{R.drawable.stat_signal_dualcard_five_up0, R.drawable.stat_signal_dualcard_five_up1, R.drawable.stat_signal_dualcard_five_up2, R.drawable.stat_signal_dualcard_five_up3, R.drawable.stat_signal_dualcard_five_up4, R.drawable.stat_signal_dualcard_five_up5}, new int[]{R.drawable.stat_signal_dualcard_five_up0, R.drawable.stat_signal_dualcard_five_up1, R.drawable.stat_signal_dualcard_five_up2, R.drawable.stat_signal_dualcard_five_up3, R.drawable.stat_signal_dualcard_five_up4, R.drawable.stat_signal_dualcard_five_up5}};
        this.TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_FIVE_S = new int[][]{new int[]{R.drawable.stat_signal_dualcard_five_down0, R.drawable.stat_signal_dualcard_five_down1, R.drawable.stat_signal_dualcard_five_down2, R.drawable.stat_signal_dualcard_five_down3, R.drawable.stat_signal_dualcard_five_down4, R.drawable.stat_signal_dualcard_five_down5}, new int[]{R.drawable.stat_signal_dualcard_five_down0, R.drawable.stat_signal_dualcard_five_down1, R.drawable.stat_signal_dualcard_five_down2, R.drawable.stat_signal_dualcard_five_down3, R.drawable.stat_signal_dualcard_five_down4, R.drawable.stat_signal_dualcard_five_down5}};
        this.TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_M = new int[][]{new int[]{R.drawable.stat_signal_dualcard_up0, R.drawable.stat_signal_dualcard_up1, R.drawable.stat_signal_dualcard_up2, R.drawable.stat_signal_dualcard_up3, R.drawable.stat_signal_dualcard_up4}, new int[]{R.drawable.stat_signal_dualcard_up0, R.drawable.stat_signal_dualcard_up1, R.drawable.stat_signal_dualcard_up2, R.drawable.stat_signal_dualcard_up3, R.drawable.stat_signal_dualcard_up4}};
        this.TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_S = new int[][]{new int[]{R.drawable.stat_signal_dualcard_down0, R.drawable.stat_signal_dualcard_down1, R.drawable.stat_signal_dualcard_down2, R.drawable.stat_signal_dualcard_down3, R.drawable.stat_signal_dualcard_down4}, new int[]{R.drawable.stat_signal_dualcard_down0, R.drawable.stat_signal_dualcard_down1, R.drawable.stat_signal_dualcard_down2, R.drawable.stat_signal_dualcard_down3, R.drawable.stat_signal_dualcard_down4}};
    }

    protected void onAttachedToWindow() {
        this.mMobileSignalSlave = (ImageView) findViewById(R.id.mobile_signal_slave);
        this.mMobileTypeSlave = (ImageView) findViewById(R.id.mobile_type_slave);
        this.mMobileTypeRoam = (ImageView) findViewById(R.id.clg_mobile_type_roam);
        this.mMobileTypeMaster = (ImageView) findViewById(R.id.mobile_signal_type);
        this.mNetworkDate = new int[7];
        for (int i = 0; i < 7; i++) {
            this.mNetworkDate[i] = -1;
        }
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        this.mMobileSignalSlave = null;
        this.mMobileTypeSlave = null;
        this.mMobileTypeRoam = null;
        this.mNetworkDate = null;
        this.mMobileTypeMaster = null;
        super.onDetachedFromWindow();
    }

    public void setExtData(int sub, int inetCon, boolean isRoam, boolean isSuspend, int[] extArgs) {
        super.setExtData(sub, inetCon, isRoam, isSuspend, extArgs);
        this.mInetCon = inetCon;
        if (this.mNetworkDate != null) {
            for (int i = 0; i < extArgs.length; i++) {
                this.mNetworkDate[i] = extArgs[i];
            }
        }
    }

    private int getDiaplayType(int masterLevel, int slaveLevel) {
        if (masterLevel < 0 || slaveLevel < 0 || this.mIsSuspend) {
            return 1;
        }
        return 0;
    }

    private void updateStrengthData(int sub, int masterType, int masterLevel, int slaveType, int slaveLevel) {
        this.mMobileTypeSlaveId = 0;
        this.mMobileStrengthSlaveId = 0;
        this.mMobileTypeRoamId = 0;
        this.mMobileTypeMasterId = 0;
        int slot;
        if (this.mIsRoam) {
            if (masterLevel >= 0) {
                this.mMobileStrengthId = IS_FIVE_SIGNAL ? this.DATA_SIGNAL_STRENGTH_FIVE[this.mInetCon][masterLevel] : this.DATA_SIGNAL_STRENGTH[this.mInetCon][masterLevel];
            }
            if (!this.mIsSuspend || !this.IS_SHOW_BUSY_ICON) {
                this.mMobileTypeMasterId = R.drawable.stat_sys_signal_type_roam;
                switch (masterType) {
                    case 1:
                        slot = 0;
                        try {
                            slot = NetWorkUtils.getDefaultSlot();
                        } catch (NoExtAPIException e) {
                            Log.v("SignalUnitTelecomView", "getUserDefaultSubscription->NoExtAPIException");
                        }
                        if (sub == slot) {
                            this.mMobileTypeRoamId = R.drawable.stat_sys_signal_card1_roam_type_2g;
                            break;
                        } else {
                            this.mMobileTypeRoamId = R.drawable.stat_sys_signal_card2_roam_type_2g;
                            break;
                        }
                    case 2:
                        this.mMobileTypeRoamId = R.drawable.stat_sys_signal_card1_roam_type_3g;
                        break;
                    case 3:
                        this.mMobileTypeRoamId = R.drawable.stat_sys_signal_card1_roam_type_4g;
                        break;
                }
            }
            this.mMobileTypeMasterId = R.drawable.stat_sys_signal_type_waiting;
            return;
        }
        switch (masterType) {
            case 1:
                slot = 0;
                try {
                    slot = NetWorkUtils.getDefaultSlot();
                } catch (NoExtAPIException e2) {
                    Log.v("SignalUnitTelecomView", "getUserDefaultSubscription->NoExtAPIException");
                }
                if (sub == slot) {
                    this.mMobileTypeMasterId = R.drawable.stat_signal_dualcard_type_up_2g;
                    break;
                } else {
                    this.mMobileTypeMasterId = R.drawable.stat_sys_signal_type_2g;
                    break;
                }
            case 2:
                this.mMobileTypeMasterId = R.drawable.stat_signal_dualcard_type_up_3g;
                break;
            case 3:
                if (HwNetworkControllerImpl.getLteCASub() != sub) {
                    this.mMobileTypeMasterId = R.drawable.stat_signal_dualcard_type_up_4g;
                    break;
                } else {
                    this.mMobileTypeMasterId = R.drawable.stat_sys_data_dualcard_fully_connected_4gplus;
                    break;
                }
        }
        int i;
        if (getDiaplayType(masterLevel, slaveLevel) == 0) {
            switch (slaveType) {
                case 1:
                case 2:
                case 3:
                    this.mMobileTypeSlaveId = R.drawable.stat_signal_dualcard_type_down_2g;
                    break;
            }
            if (masterLevel >= 0) {
                this.mMobileStrengthId = IS_FIVE_SIGNAL ? this.TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_FIVE_M[this.mInetCon][masterLevel] : this.TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_M[this.mInetCon][masterLevel];
            }
            if (slaveLevel >= 0) {
                if (IS_FIVE_SIGNAL) {
                    i = this.TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_FIVE_S[this.mInetCon][slaveLevel];
                } else {
                    i = this.TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_S[this.mInetCon][slaveLevel];
                }
                this.mMobileStrengthSlaveId = i;
            }
        } else {
            boolean isSkytone = SystemUiUtil.isSupportVSim() && sub == NetWorkUtils.getVSimSubId();
            if (this.mIsSuspend && this.IS_SHOW_BUSY_ICON && !isSkytone) {
                this.mMobileTypeMasterId = R.drawable.stat_sys_signal_type_waiting;
            }
            if (masterLevel >= 0) {
                this.mMobileStrengthId = IS_FIVE_SIGNAL ? this.DATA_SIGNAL_STRENGTH_FIVE[this.mInetCon][masterLevel] : this.DATA_SIGNAL_STRENGTH[this.mInetCon][masterLevel];
                if (isSkytone) {
                    if (IS_FIVE_SIGNAL) {
                        i = HwTelephonyIcons.getFiveTJTSignalIcons(this.mInetCon, masterLevel);
                    } else {
                        i = NetWorkUtils.getTjtIcons(this.mInetCon, masterLevel);
                    }
                    this.mMobileStrengthId = i;
                }
            }
        }
    }

    private void updateDataType(int sub, int dataState, int dataType) {
        Log.d("SignalUnitTelecomView", "sub is " + sub + "   dataType is" + dataType);
        this.mMobileTypeId = 0;
        if (dataState == 2 && !NetWorkUtils.get3GCallingState(sub)) {
            switch (TelephonyManager.getNetworkClass(dataType)) {
                case 1:
                    int slot = 0;
                    try {
                        slot = NetWorkUtils.getDefaultSlot();
                    } catch (NoExtAPIException e) {
                        Log.v("SignalUnitTelecomView", "getUserDefaultSubscription->NoExtAPIException");
                    }
                    this.mMobileTypeId = sub == slot ? R.drawable.stat_sys_data_connected_c1_2g : R.drawable.stat_sys_data_connected_c2_2g;
                    break;
                case 2:
                    this.mMobileTypeId = R.drawable.stat_sys_data_fully_connected_3g;
                    break;
                case 3:
                    this.mMobileTypeId = R.drawable.stat_sys_data_fully_connected_4g;
                    if (HwNetworkControllerImpl.getLteCASub() == sub) {
                        this.mMobileTypeId = R.drawable.stat_sys_data_connected_c_4gplus;
                        break;
                    }
                    break;
            }
            if (IS_FIVE_SIGNAL) {
                this.mMobileTypeId = -1;
            }
        }
    }

    void refreshView() {
        if (this.mNetworkDate != null) {
            updateStrengthData(this.mSubscription, this.mNetworkDate[0], this.mNetworkDate[1], this.mNetworkDate[2], this.mNetworkDate[3]);
            updateDataType(this.mSubscription, this.mNetworkDate[4], this.mNetworkDate[6]);
        }
        updateView(this.mMobileSignalSlave, this.mMobileStrengthSlaveId);
        updateView(this.mMobileTypeSlave, this.mMobileTypeSlaveId);
        updateView(this.mMobileTypeRoam, this.mMobileTypeRoamId);
        updateView(this.mMobileTypeMaster, this.mMobileTypeMasterId);
        if (this.mMobileDataGroup != null && this.mMobileActivityId != 0 && !this.mIsSuspend) {
            this.mMobileDataGroup.setVisibility(0);
        } else if (this.mMobileDataGroup != null) {
            this.mMobileDataGroup.setVisibility(8);
        }
        super.refreshView();
    }

    void updateMobileType() {
        updateView(this.mMobileType, this.mMobileTypeId);
    }

    int getMobileActivityIconId(boolean activityIn, boolean activityOut) {
        boolean isDouble = true;
        if (this.mNetworkDate == null) {
            return 0;
        }
        if (getDiaplayType(this.mNetworkDate[1], this.mNetworkDate[3]) != 0) {
            isDouble = false;
        }
        boolean isShowTelecomDataIcon = isDouble && !this.mIsRoam;
        int mMobileActivityIconId = (activityIn && activityOut) ? isShowTelecomDataIcon ? R.drawable.stat_sys_double_inout : R.drawable.stat_sys_signal_inout : activityIn ? isShowTelecomDataIcon ? R.drawable.stat_sys_double_in : R.drawable.stat_sys_signal_in : activityOut ? isShowTelecomDataIcon ? R.drawable.stat_sys_double_out : R.drawable.stat_sys_signal_out : isShowTelecomDataIcon ? R.drawable.single_stat_sys_double_connected : R.drawable.single_stat_sys_signal_connected;
        return mMobileActivityIconId;
    }
}
