package com.android.systemui.statusbar;

import android.content.Context;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.NetWorkUtils;
import com.android.systemui.utils.SystemUiUtil;

public class SignalUnitCmccView extends SignalUnitNormalView {
    private static final boolean SGLTE = SystemProperties.getBoolean("ro.config.hw_sglte", false);
    static final int[][] TELEPHONY_SIGNAL_STRENGTH = new int[][]{new int[]{R.drawable.stat_sys_signal_0, R.drawable.stat_sys_signal_1, R.drawable.stat_sys_signal_2, R.drawable.stat_sys_signal_3, R.drawable.stat_sys_signal_4}, new int[]{R.drawable.stat_sys_signal_0_fully, R.drawable.stat_sys_signal_1_fully, R.drawable.stat_sys_signal_2_fully, R.drawable.stat_sys_signal_3_fully, R.drawable.stat_sys_signal_4_fully}};
    final int[][] DATA_SIGNAL_STRENGTH;
    final int[][] TELEPHONY_SIGNAL_STRENGTH_CMCC_CARD1_M;
    final int[][] TELEPHONY_SIGNAL_STRENGTH_CMCC_CARD1_S;
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

    public SignalUnitCmccView(Context context) {
        this(context, null);
    }

    public SignalUnitCmccView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalUnitCmccView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mMobileTypeSlaveId = 0;
        this.mMobileStrengthSlaveId = 0;
        this.mMobileTypeMasterId = 0;
        this.mMobileTypeRoamId = 0;
        this.mInetCon = 0;
        this.DATA_SIGNAL_STRENGTH = TELEPHONY_SIGNAL_STRENGTH;
        this.TELEPHONY_SIGNAL_STRENGTH_CMCC_CARD1_M = new int[][]{new int[]{R.drawable.stat_signal_dualcard_up0, R.drawable.stat_signal_dualcard_up1, R.drawable.stat_signal_dualcard_up2, R.drawable.stat_signal_dualcard_up3, R.drawable.stat_signal_dualcard_up4}, new int[]{R.drawable.stat_signal_dualcard_up0, R.drawable.stat_signal_dualcard_up1, R.drawable.stat_signal_dualcard_up2, R.drawable.stat_signal_dualcard_up3, R.drawable.stat_signal_dualcard_up4}};
        this.TELEPHONY_SIGNAL_STRENGTH_CMCC_CARD1_S = new int[][]{new int[]{R.drawable.stat_signal_dualcard_down0, R.drawable.stat_signal_dualcard_down1, R.drawable.stat_signal_dualcard_down2, R.drawable.stat_signal_dualcard_down3, R.drawable.stat_signal_dualcard_down4}, new int[]{R.drawable.stat_signal_dualcard_down0, R.drawable.stat_signal_dualcard_down1, R.drawable.stat_signal_dualcard_down2, R.drawable.stat_signal_dualcard_down3, R.drawable.stat_signal_dualcard_down4}};
    }

    protected void onAttachedToWindow() {
        this.mMobileSignalSlave = (ImageView) findViewById(R.id.mobile_signal_slave);
        this.mMobileTypeSlave = (ImageView) findViewById(R.id.mobile_type_slave);
        this.mMobileTypeRoam = (ImageView) findViewById(R.id.td_mobile_type_roam);
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
        if (masterLevel < 0 || slaveLevel < 0) {
            return 1;
        }
        return 0;
    }

    private boolean isSGLTEMode() {
        return SystemProperties.getBoolean("persist.radio.svlte_mode", false);
    }

    private void updateStrengthData(int sub, int masterType, int masterLevel, int slaveType, int slaveLevel) {
        this.mMobileTypeSlaveId = 0;
        this.mMobileStrengthSlaveId = 0;
        this.mMobileTypeRoamId = 0;
        this.mMobileTypeMasterId = 0;
        if (this.mIsRoam) {
            this.mMobileTypeMasterId = R.drawable.stat_sys_signal_type_roam;
            if (masterLevel >= 0 && 8 != masterLevel) {
                this.mMobileStrengthId = this.DATA_SIGNAL_STRENGTH[this.mInetCon][masterLevel];
            }
            switch (masterType) {
                case 1:
                    if (sub == 1) {
                        this.mMobileTypeRoamId = R.drawable.stat_sys_signal_card2_roam_type_2g;
                        return;
                    } else {
                        this.mMobileTypeRoamId = R.drawable.stat_sys_signal_card1_roam_type_2g;
                        return;
                    }
                case 2:
                    this.mMobileTypeRoamId = R.drawable.stat_sys_signal_card1_roam_type_3g;
                    return;
                case 3:
                    this.mMobileTypeRoamId = R.drawable.stat_sys_signal_card1_roam_type_4g;
                    return;
                default:
                    return;
            }
        }
        switch (masterType) {
            case 1:
                if (sub != 1) {
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
                this.mMobileTypeMasterId = R.drawable.stat_signal_dualcard_type_up_4g;
                break;
        }
        if (getDiaplayType(masterLevel, slaveLevel) == 0) {
            switch (slaveType) {
                case 1:
                case 2:
                case 3:
                    if (sub != 1) {
                        this.mMobileTypeSlaveId = R.drawable.stat_signal_dualcard_type_down_2g;
                        break;
                    } else {
                        this.mMobileTypeSlaveId = R.drawable.stat_sys_signal_type_2g;
                        break;
                    }
            }
            if (masterLevel >= 0 && 8 != masterLevel) {
                this.mMobileStrengthId = this.TELEPHONY_SIGNAL_STRENGTH_CMCC_CARD1_M[this.mInetCon][masterLevel];
            } else if (SGLTE && isSGLTEMode() && masterLevel == 8) {
                this.mMobileStrengthId = R.drawable.stat_signal_dualcard_up_null;
            }
            if (slaveLevel >= 0 && 8 != slaveLevel) {
                this.mMobileStrengthSlaveId = this.TELEPHONY_SIGNAL_STRENGTH_CMCC_CARD1_S[this.mInetCon][slaveLevel];
            } else if (SGLTE && isSGLTEMode() && slaveLevel == 8) {
                this.mMobileStrengthSlaveId = R.drawable.stat_signal_dualcard_down_null;
            }
        } else if (masterLevel >= 0 && 8 != masterLevel) {
            this.mMobileStrengthId = this.DATA_SIGNAL_STRENGTH[this.mInetCon][masterLevel];
        }
        if (SystemUiUtil.isSupportVSim() && sub == NetWorkUtils.getVSimSubId()) {
            this.mMobileStrengthId = NetWorkUtils.getTjtIcons(this.mInetCon, masterLevel);
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
        if (this.mMobileActivityId == 0) {
            this.mMobileTypeId = 0;
        }
        super.refreshView();
    }

    private void updateDataType(int sub, int dataState, int dataType) {
        if (dataState == 2) {
            switch (dataType) {
                case 1:
                case 2:
                    this.mMobileTypeId = R.drawable.stat_sys_data_connected_e_sglte;
                    return;
                case 3:
                case 8:
                case 9:
                case 10:
                case 14:
                case 15:
                case 17:
                case 30:
                    this.mMobileTypeId = R.drawable.stat_sys_data_connected_3g_sglte;
                    return;
                case 13:
                case 18:
                case 31:
                    this.mMobileTypeId = R.drawable.stat_sys_data_connected_4g_sglte;
                    return;
                default:
                    this.mMobileTypeId = R.drawable.stat_sys_data_connected_g_sglte;
                    return;
            }
        }
    }
}
