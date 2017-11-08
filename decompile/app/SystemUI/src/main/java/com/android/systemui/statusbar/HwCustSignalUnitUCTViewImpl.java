package com.android.systemui.statusbar;

import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import com.android.systemui.R;
import com.android.systemui.utils.SystemUiUtil;
import fyusion.vislib.BuildConfig;

public class HwCustSignalUnitUCTViewImpl extends HwCustSignalUnitUCTView {
    private static final boolean IS_ORANGE_OPERATOR = SystemProperties.get("ro.config.hw_opta", BuildConfig.FLAVOR).equals("109");
    protected SignalUnitUCTView mParent;

    public HwCustSignalUnitUCTViewImpl(SignalUnitUCTView parent) {
        super(parent);
        this.mParent = parent;
    }

    public void updateStrengthData(int sub, int masterType, int masterLevel, int slaveType, int slaveLevel) {
        int networkType = SystemUiUtil.getCurrentNetWorkTypeBySlotId(sub);
        switch (masterType) {
            case 2:
                update3GMobileNetworkIcon(sub, networkType);
                return;
            default:
                return;
        }
    }

    public void updateMobileDataType(int sub, int dataType) {
        switch (TelephonyManager.getNetworkClass(dataType)) {
            case 2:
                update3GMobileDataIcon(sub, dataType);
                return;
            default:
                return;
        }
    }

    private void update3GMobileNetworkIcon(int sub, int networkType) {
        switch (networkType) {
            case 8:
            case 9:
            case 10:
                if (!this.mParent.mIsRoam && IS_ORANGE_OPERATOR) {
                    this.mParent.mMobileTypeId = R.drawable.stat_sys_data_connected_c_3gplus;
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void update3GMobileDataIcon(int sub, int dataType) {
        switch (dataType) {
            case 8:
            case 9:
            case 10:
                if (IS_ORANGE_OPERATOR) {
                    this.mParent.mMobileTypeId = R.drawable.stat_sys_data_connected_c_3gplus;
                    return;
                }
                return;
            default:
                return;
        }
    }
}
