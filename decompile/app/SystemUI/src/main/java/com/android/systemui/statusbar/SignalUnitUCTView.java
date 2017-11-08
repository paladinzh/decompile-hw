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
import com.huawei.cust.HwCustUtils;

public class SignalUnitUCTView extends SignalUnitNormalView {
    static final int[][] TELEPHONY_SIGNAL_STRENGTH = new int[][]{new int[]{R.drawable.stat_sys_signal_0, R.drawable.stat_sys_signal_1, R.drawable.stat_sys_signal_2, R.drawable.stat_sys_signal_3, R.drawable.stat_sys_signal_4}, new int[]{R.drawable.stat_sys_signal_0_fully, R.drawable.stat_sys_signal_1_fully, R.drawable.stat_sys_signal_2_fully, R.drawable.stat_sys_signal_3_fully, R.drawable.stat_sys_signal_4_fully}};
    static final int[][] TELEPHONY_SIGNAL_STRENGTH_FIVE = new int[][]{new int[]{R.drawable.stat_sys_signal_five_0, R.drawable.stat_sys_signal_five_1, R.drawable.stat_sys_signal_five_2, R.drawable.stat_sys_signal_five_3, R.drawable.stat_sys_signal_five_4, R.drawable.stat_sys_signal_five_5}, new int[]{R.drawable.stat_sys_signal_five_0_fully, R.drawable.stat_sys_signal_five_1_fully, R.drawable.stat_sys_signal_five_2_fully, R.drawable.stat_sys_signal_five_3_fully, R.drawable.stat_sys_signal_five_4_fully, R.drawable.stat_sys_signal_five_5_fully}};
    final int[][] DATA_SIGNAL_STRENGTH;
    final int[][] DATA_SIGNAL_STRENGTH_FIVE;
    final int[][] TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_FIVE_M;
    final int[][] TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_FIVE_S;
    final int[][] TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_M;
    final int[][] TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_S;
    private HwCustSignalUnitUCTView mCust;
    protected int mInetCon;
    private ImageView mMobileSignalSlave;
    private int mMobileStrengthSlaveId;
    private ImageView mMobileTypeMaster;
    private int mMobileTypeMasterId;
    private ImageView mMobileTypeRoam;
    private int mMobileTypeRoamId;
    private ImageView mMobileTypeSlave;
    private int mMobileTypeSlaveId;
    private int[] mNetworkData;

    public SignalUnitUCTView(Context context) {
        this(context, null);
    }

    public SignalUnitUCTView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalUnitUCTView(Context context, AttributeSet attrs, int defStyle) {
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
        this.mCust = (HwCustSignalUnitUCTView) HwCustUtils.createObj(HwCustSignalUnitUCTView.class, new Object[]{this});
    }

    protected void onAttachedToWindow() {
        this.mMobileSignalSlave = (ImageView) findViewById(R.id.mobile_signal_slave);
        this.mMobileTypeSlave = (ImageView) findViewById(R.id.mobile_type_slave);
        this.mMobileTypeRoam = (ImageView) findViewById(R.id.uct_mobile_type_roam);
        this.mMobileTypeMaster = (ImageView) findViewById(R.id.mobile_signal_type);
        this.mNetworkData = new int[7];
        for (int i = 0; i < 7; i++) {
            this.mNetworkData[i] = -1;
        }
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        this.mMobileSignalSlave = null;
        this.mMobileTypeSlave = null;
        this.mMobileTypeRoam = null;
        this.mNetworkData = null;
        this.mMobileTypeMaster = null;
        super.onDetachedFromWindow();
    }

    public void setExtData(int sub, int inetCon, boolean isRoam, boolean isSuspend, int[] extArgs) {
        super.setExtData(sub, inetCon, isRoam, isSuspend, extArgs);
        this.mInetCon = inetCon;
        if (this.mNetworkData != null) {
            for (int i = 0; i < extArgs.length; i++) {
                this.mNetworkData[i] = extArgs[i];
            }
        }
    }

    protected int getDiaplayType(int masterLevel, int slaveLevel) {
        if (masterLevel < 0 || slaveLevel < 0 || this.mIsSuspend) {
            return 1;
        }
        return 0;
    }

    private void updateStrengthData(int sub, int masterType, int masterLevel, int slaveType, int slaveLevel) {
        int i = R.drawable.stat_sys_data_connected_2g;
        this.mMobileTypeSlaveId = 0;
        this.mMobileStrengthSlaveId = 0;
        this.mMobileTypeRoamId = 0;
        this.mMobileTypeMasterId = 0;
        int networkType = SystemUiUtil.getCurrentNetWorkTypeBySlotId(sub);
        if (this.mIsRoam) {
            if (masterLevel >= 0) {
                this.mMobileStrengthId = IS_FIVE_SIGNAL ? this.DATA_SIGNAL_STRENGTH_FIVE[this.mInetCon][masterLevel] : this.DATA_SIGNAL_STRENGTH[this.mInetCon][masterLevel];
            }
            if (!this.mIsSuspend || !this.IS_SHOW_BUSY_ICON) {
                this.mMobileTypeMasterId = R.drawable.stat_sys_signal_type_roam;
                switch (masterType) {
                    case 1:
                        this.mMobileTypeRoamId = R.drawable.stat_sys_signal_roam_type_2g;
                        break;
                    case 2:
                        update3GMobileNetworkIcon(sub, networkType, false);
                        break;
                    case 3:
                        this.mMobileTypeRoamId = R.drawable.stat_sys_signal_roam_type_4g;
                        break;
                }
            }
            this.mMobileTypeMasterId = R.drawable.stat_sys_signal_type_waiting;
            return;
        }
        boolean isDoubleSignal = getDiaplayType(masterLevel, slaveLevel) == 0;
        switch (masterType) {
            case 1:
                this.mMobileTypeMasterId = isDoubleSignal ? R.drawable.stat_signal_dualcard_type_up_2g : R.drawable.stat_sys_data_connected_2g;
                break;
            case 2:
                update3GMobileNetworkIcon(sub, networkType, isDoubleSignal);
                break;
            case 3:
                if (HwNetworkControllerImpl.getLteCASub() != sub) {
                    this.mMobileTypeMasterId = isDoubleSignal ? R.drawable.stat_signal_dualcard_type_up_4g : R.drawable.stat_sys_data_connected_4g;
                    break;
                } else {
                    this.mMobileTypeMasterId = isDoubleSignal ? R.drawable.stat_sys_data_dualcard_fully_connected_4gplus : R.drawable.stat_sys_data_fully_connected_4gplus;
                    break;
                }
        }
        int i2;
        if (isDoubleSignal) {
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
                    i2 = this.TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_FIVE_S[this.mInetCon][slaveLevel];
                } else {
                    i2 = this.TELEPHONY_SIGNAL_STRENGTH_TELECOM_CARD1_S[this.mInetCon][slaveLevel];
                }
                this.mMobileStrengthSlaveId = i2;
            }
        } else {
            boolean isSkytone = SystemUiUtil.isSupportVSim() && sub == NetWorkUtils.getVSimSubId();
            if (this.mIsSuspend && !isSkytone) {
                if (this.IS_SHOW_BUSY_ICON) {
                    i = R.drawable.stat_sys_signal_type_waiting;
                } else if (this.mMobileTypeMasterId == 0) {
                    i = 0;
                }
                this.mMobileTypeMasterId = i;
            }
            if (masterLevel >= 0) {
                if (IS_FIVE_SIGNAL) {
                    i2 = this.DATA_SIGNAL_STRENGTH_FIVE[this.mInetCon][masterLevel];
                } else {
                    i2 = this.DATA_SIGNAL_STRENGTH[this.mInetCon][masterLevel];
                }
                this.mMobileStrengthId = i2;
                if (isSkytone) {
                    if (IS_FIVE_SIGNAL) {
                        i2 = HwTelephonyIcons.getFiveTJTSignalIcons(this.mInetCon, masterLevel);
                    } else {
                        i2 = NetWorkUtils.getTjtIcons(this.mInetCon, masterLevel);
                    }
                    this.mMobileStrengthId = i2;
                }
            }
        }
        if (this.mCust != null) {
            this.mCust.updateStrengthData(sub, masterType, masterLevel, slaveType, slaveLevel);
        }
    }

    private void update3GMobileNetworkIcon(int sub, int networkType, boolean isDoubleSignal) {
        switch (networkType) {
            case 8:
            case 9:
            case 10:
            case 18:
            case 19:
                if (this.mIsRoam) {
                    this.mMobileTypeRoamId = R.drawable.stat_sys_signal_roam_type_h;
                    return;
                } else {
                    this.mMobileTypeMasterId = R.drawable.stat_sys_data_connected_h;
                    return;
                }
            case 15:
                if (this.mIsRoam) {
                    this.mMobileTypeRoamId = R.drawable.stat_sys_signal_roam_type_hplus;
                    return;
                } else {
                    this.mMobileTypeMasterId = R.drawable.stat_sys_data_connected_hplus;
                    return;
                }
            default:
                if (this.mIsRoam) {
                    this.mMobileTypeRoamId = R.drawable.stat_sys_signal_roam_type_3g;
                    return;
                } else {
                    this.mMobileTypeMasterId = isDoubleSignal ? R.drawable.stat_signal_dualcard_type_up_3g : R.drawable.stat_sys_data_connected_3g;
                    return;
                }
        }
    }

    private void update3GMobileDataIcon(int sub, int dataType) {
        switch (dataType) {
            case 8:
            case 9:
            case 10:
            case 18:
            case 19:
                this.mMobileTypeId = R.drawable.stat_sys_data_connected_c_h;
                return;
            case 15:
                this.mMobileTypeId = R.drawable.stat_sys_data_connected_c_hplus;
                return;
            default:
                this.mMobileTypeId = R.drawable.stat_sys_data_connected_c_3g;
                return;
        }
    }

    private void updateMobileConnectedData(int sub, int dataConnected, int dataActivity, int dataType) {
        boolean isDouble = true;
        this.mMobileTypeId = 0;
        this.mMobileActivityId = 0;
        if (this.mNetworkData != null) {
            if (getDiaplayType(this.mNetworkData[1], this.mNetworkData[3]) != 0) {
                isDouble = false;
            }
            boolean isShowTelecomDataIcon = isDouble && !this.mIsRoam;
            if (dataConnected == 2 && !NetWorkUtils.get3GCallingState(sub)) {
                updateMobileDataType(sub, dataType);
                switch (dataActivity) {
                    case 1:
                        this.mMobileActivityId = isShowTelecomDataIcon ? R.drawable.stat_sys_double_in : R.drawable.stat_sys_signal_in;
                        break;
                    case 2:
                        this.mMobileActivityId = isShowTelecomDataIcon ? R.drawable.stat_sys_double_out : R.drawable.stat_sys_signal_out;
                        break;
                    case 3:
                        this.mMobileActivityId = isShowTelecomDataIcon ? R.drawable.stat_sys_double_inout : R.drawable.stat_sys_signal_inout;
                        break;
                    default:
                        int i;
                        if (isShowTelecomDataIcon) {
                            i = R.drawable.single_stat_sys_double_connected;
                        } else {
                            i = R.drawable.single_stat_sys_signal_connected;
                        }
                        this.mMobileActivityId = i;
                        break;
                }
            }
        }
    }

    private void updateMobileDataType(int sub, int dataType) {
        switch (TelephonyManager.getNetworkClass(dataType)) {
            case 1:
                this.mMobileTypeId = R.drawable.stat_sys_data_connected_c_2g;
                break;
            case 2:
                update3GMobileDataIcon(sub, dataType);
                break;
            case 3:
                if (HwNetworkControllerImpl.getLteCASub() != sub) {
                    this.mMobileTypeId = R.drawable.stat_sys_data_connected_c_4g;
                    break;
                } else {
                    this.mMobileTypeId = R.drawable.stat_sys_data_connected_c_4gplus;
                    break;
                }
            default:
                this.mMobileTypeId = 0;
                break;
        }
        if (this.mCust != null) {
            this.mCust.updateMobileDataType(sub, dataType);
        }
        if (IS_FIVE_SIGNAL) {
            this.mMobileTypeId = -1;
        }
    }

    void refreshView() {
        if (this.mNetworkData != null) {
            updateStrengthData(this.mSubscription, this.mNetworkData[0], this.mNetworkData[1], this.mNetworkData[2], this.mNetworkData[3]);
            updateMobileConnectedData(this.mSubscription, this.mNetworkData[4], this.mNetworkData[5], this.mNetworkData[6]);
        }
        updateView(this.mMobileSignalSlave, this.mMobileStrengthSlaveId);
        updateView(this.mMobileTypeSlave, this.mMobileTypeSlaveId);
        updateView(this.mMobileTypeRoam, this.mMobileTypeRoamId);
        updateView(this.mMobileTypeMaster, this.mMobileTypeMasterId);
        if (this.mMobileDataGroup != null) {
            if (NetWorkUtils.getVSimCurCardType() == 1 || this.mIsSuspend) {
                Log.d("SignalUnitUCTView", " vsimcardtype or suspend uct refreshView GONE");
                this.mMobileDataGroup.setVisibility(8);
            } else {
                this.mMobileDataGroup.setVisibility(0);
            }
        }
        super.refreshView();
    }

    void updateMobileType() {
        updateView(this.mMobileType, this.mMobileTypeId);
    }

    int getMobileActivityIconId(boolean activityIn, boolean activityOut) {
        boolean isDouble = true;
        if (this.mNetworkData == null) {
            return 0;
        }
        if (getDiaplayType(this.mNetworkData[1], this.mNetworkData[3]) != 0) {
            isDouble = false;
        }
        boolean isShowTelecomDataIcon = isDouble && !this.mIsRoam;
        int mMobileActivityIconId = (activityIn && activityOut) ? isShowTelecomDataIcon ? R.drawable.stat_sys_double_inout : R.drawable.stat_sys_signal_inout : activityIn ? isShowTelecomDataIcon ? R.drawable.stat_sys_double_in : R.drawable.stat_sys_signal_in : activityOut ? isShowTelecomDataIcon ? R.drawable.stat_sys_double_out : R.drawable.stat_sys_signal_out : isShowTelecomDataIcon ? R.drawable.single_stat_sys_double_connected : R.drawable.single_stat_sys_signal_connected;
        return mMobileActivityIconId;
    }
}
