package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.R;
import com.huawei.telephony.HuaweiTelephonyManagerCustEx;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class HwCustMobileSignalControllerImpl extends HwCustMobileSignalController {
    private static final boolean IS_ATT = SystemProperties.getBoolean("ro.config.replace_signal_icon", false);
    private static final boolean IS_SFR = SystemProperties.getBoolean("ro.config.show_2g_3gplus", false);
    private static final boolean IS_SGLTE = SystemProperties.getBoolean("ro.config.hw_sglte", false);
    private static final int SHOW_NETWORKTYPE_DEFAULT = 1;
    private static final String TAG = HwCustMobileSignalControllerImpl.class.getSimpleName();
    private static final boolean mDChspapShow4g = SystemProperties.getBoolean("ro.config.is_dchspap_show_4g", false);
    private static final boolean mDChspapShowhPlus = SystemProperties.getBoolean("ro.config.is_3g_show_hgplus", false);
    private static final boolean mHsdpaHspaShow3g = SystemProperties.getBoolean("ro.systemui.is_h_show_3g", false);
    private static final boolean mHsdpaHspaShow3gPlus = SystemProperties.getBoolean("ro.config.is_h_show_3gplus", false);
    private static final boolean mHspaShow4g = SystemProperties.getBoolean("ro.config.is_hspa_show_4g", false);
    private static final boolean mHspapShow4g = SystemProperties.getBoolean("ro.config.is_hh_show_4g", false);
    public static final boolean mLteShow4glte = SystemProperties.getBoolean("ro.config.lte_show_northamerica", false);
    private static final boolean mOnlyLteShow4glte = SystemProperties.getBoolean("ro.systemui.lte_show_4glte", false);
    private final boolean IS_HH_ROAM_SHOW_3G = SystemProperties.getBoolean("ro.config.is_hspa_roam_show_3g", false);
    private String custBasedDataType = System.getString(this.mContext.getContentResolver(), "hw_data_type_by_cust");
    private String customized_network_type = System.getString(this.mContext.getContentResolver(), "hw_customized_networkType_entry");
    HashSet<String> m4gBySimMccMnc = new HashSet();
    HashSet<String> mDigicelMccmnc = new HashSet();
    HashSet<String> mIranMccMnc = new HashSet();
    HashSet<String> mLteBySimMccMnc = new HashSet();
    HashSet<String> mNextelMccMnc = new HashSet();
    private TelephonyManager mPhone = ((TelephonyManager) this.mContext.getSystemService("phone"));
    HashSet<String> mPolandMccMnc = new HashSet();
    private String mSfrMccMncString = System.getString(this.mContext.getContentResolver(), "hw_data_type_by_plmn");
    String mStr4gBySimMccMnc = null;
    String mStrDigicelPlmn = null;
    String mStrIranMccMnc = null;
    String mStrLteBySimMccMnc = null;
    String mStrNextelMccMnc = null;
    String mStrPolandMccMnc = null;
    String mStrTurkeyMccMnc = null;
    HashSet<String> mTurkeyMccMnc = new HashSet();

    public HwCustMobileSignalControllerImpl(HwMobileSignalController parent, Context context, SubscriptionInfo info) {
        super(parent, context, info);
    }

    public int updateDataType(int typeIcon, int mDataNetType, int sub, boolean isCAstate) {
        int dataNetType = typeIcon;
        String currentMccMnc = TelephonyManager.from(this.mContext).getSimOperator(sub);
        boolean isDigicel = isCurrentBySim(sub, this.mStrDigicelPlmn, this.mDigicelMccmnc, "hw_customized_networkType_entry", currentMccMnc);
        boolean isMccMnc = isCurrentBySim(sub, this.mStrPolandMccMnc, this.mPolandMccMnc, "hw_mcc_mnc", currentMccMnc);
        boolean isShow4g = isCurrentBySim(sub, this.mStr4gBySimMccMnc, this.m4gBySimMccMnc, "hw_show_4g", currentMccMnc);
        boolean isShowLte = isCurrentBySim(sub, this.mStrLteBySimMccMnc, this.mLteBySimMccMnc, "hw_show_lte", currentMccMnc);
        boolean isHspapShow4g = isCurrentBySim(sub, this.mStrNextelMccMnc, this.mNextelMccMnc, "hw_hspap_show_4g", currentMccMnc);
        boolean isShow4point5 = isCurrentBySim(sub, this.mStrTurkeyMccMnc, this.mTurkeyMccMnc, "hw_show_4_5G_for_mcc", currentMccMnc);
        boolean isShow4point5ForCA = isCurrentBySim(sub, this.mStrIranMccMnc, this.mIranMccMnc, "hw_show_4g_to_4_5g_ca", currentMccMnc);
        String regPlmn = TelephonyManager.from(this.mContext).getNetworkOperator(sub);
        boolean mIsShowNetworkType = System.getInt(this.mContext.getContentResolver(), "isShowNetworkType", 1) == 1;
        switch (mDataNetType) {
            case 0:
                if (isMccMnc || !mIsShowNetworkType) {
                    dataNetType = -1;
                    break;
                }
            case 1:
                dataNetType = R.drawable.stat_sys_data_fully_connected_g;
                if (isDigicel) {
                    dataNetType = R.drawable.stat_sys_data_fully_connected_e;
                } else if (IS_SFR || isDataTypeByPlmn(currentMccMnc)) {
                    dataNetType = R.drawable.stat_sys_data_fully_connected_2g;
                }
                if (IS_ATT) {
                    dataNetType = R.drawable.stat_sys_data_fully_connected_g;
                    break;
                }
                break;
            case 2:
                if (IS_SFR && !isDataTypeByPlmn(currentMccMnc)) {
                    dataNetType = R.drawable.stat_sys_data_fully_connected_2g;
                }
                if (IS_ATT) {
                    dataNetType = R.drawable.stat_sys_data_fully_connected_e;
                    break;
                }
                break;
            case 3:
            case 14:
                if (mLteShow4glte) {
                    dataNetType = northAmericanUpdateDataNetType(mDataNetType);
                }
                if (isDigicel) {
                    dataNetType = R.drawable.stat_sys_data_fully_connected_h;
                }
                if (IS_ATT) {
                    if (!mHspapShow4g) {
                        dataNetType = R.drawable.stat_sys_data_fully_connected_3g;
                        break;
                    }
                    dataNetType = R.drawable.stat_sys_data_fully_connected_4g;
                    break;
                }
                break;
            case 8:
            case 9:
            case 10:
                dataNetType = R.drawable.stat_sys_data_fully_connected_h;
                if (mLteShow4glte) {
                    dataNetType = northAmericanUpdateDataNetType(mDataNetType);
                } else if (IS_SFR || mHsdpaHspaShow3gPlus || isDataTypeByPlmn(currentMccMnc)) {
                    dataNetType = R.drawable.stat_sys_data_fully_connected_3gplus;
                } else if (mHsdpaHspaShow3g) {
                    dataNetType = R.drawable.stat_sys_data_connected_3g;
                }
                if (IS_ATT) {
                    if (mHspapShow4g) {
                        dataNetType = R.drawable.stat_sys_data_fully_connected_4g;
                    } else {
                        dataNetType = R.drawable.stat_sys_data_fully_connected_3g;
                    }
                }
                if (mHspaShow4g) {
                    dataNetType = R.drawable.stat_sys_data_connected_4g;
                }
                if (this.IS_HH_ROAM_SHOW_3G && this.mPhone.isNetworkRoaming()) {
                    dataNetType = R.drawable.stat_sys_data_connected_3g;
                    break;
                }
                break;
            case 13:
                dataNetType = getLteNetWorkTypeIcon(typeIcon, isShow4g, isMccMnc, isCAstate, isShowLte, isShow4point5, isShow4point5ForCA, currentMccMnc, regPlmn);
                break;
            case 15:
                dataNetType = R.drawable.stat_sys_data_fully_connected_hplus;
                if (mLteShow4glte) {
                    dataNetType = northAmericanUpdateDataNetType(mDataNetType);
                }
                if (isHspapShow4g || mHspapShow4g) {
                    dataNetType = R.drawable.stat_sys_data_fully_connected_4g;
                } else if (IS_SFR || isDataTypeByPlmn(currentMccMnc)) {
                    dataNetType = R.drawable.stat_sys_data_fully_connected_3gplus;
                }
                if (IS_ATT) {
                    if (mHspapShow4g) {
                        dataNetType = R.drawable.stat_sys_data_fully_connected_4g;
                    } else {
                        dataNetType = R.drawable.stat_sys_data_fully_connected_3g;
                    }
                }
                if (this.IS_HH_ROAM_SHOW_3G && this.mPhone.isNetworkRoaming()) {
                    dataNetType = R.drawable.stat_sys_data_connected_3g;
                    break;
                }
                break;
            case 30:
                if (mDChspapShow4g) {
                    dataNetType = R.drawable.stat_sys_data_fully_connected_4g;
                }
                if (mDChspapShowhPlus || isDataTypeByPlmn(currentMccMnc)) {
                    dataNetType = R.drawable.stat_sys_data_fully_connected_hplus;
                    break;
                }
        }
        return updateDataTypeByCust(dataNetType, mDataNetType, sub, currentMccMnc);
    }

    public int updateDataTypeNoDataConnected(int typeIcon, int mDataNetType, int sub, boolean isCAstate) {
        if (IS_ATT) {
            return updateDataType(typeIcon, mDataNetType, sub, isCAstate);
        }
        if (mDataNetType != 13 || !isCAstate) {
            return typeIcon;
        }
        if (isCurrentBySim(sub, this.mStrIranMccMnc, this.mIranMccMnc, "hw_show_4g_to_4_5g_ca", TelephonyManager.from(this.mContext).getSimOperator(sub))) {
            typeIcon = R.drawable.stat_sys_data_fully_connected_4point5g;
        }
        return typeIcon;
    }

    private int northAmericanUpdateDataNetType(int mDataNetType) {
        if (mDataNetType == 13) {
            return R.drawable.stat_sys_data_connected_4glte;
        }
        return R.drawable.stat_sys_data_fully_connected_4g;
    }

    private void initSet(String strMccMnc, HashSet hashMccMnc, String strPlmn) {
        if (hashMccMnc == null) {
            hashMccMnc = new HashSet();
        }
        if (strMccMnc == null && hashMccMnc.size() == 0) {
            strMccMnc = System.getString(this.mContext.getContentResolver(), strPlmn);
        }
        if (!TextUtils.isEmpty(strMccMnc)) {
            String[] mccmnc = strMccMnc.split(";");
            for (String trim : mccmnc) {
                hashMccMnc.add(trim.trim());
            }
        }
    }

    private boolean isCurrentBySim(int sub, String strMccMnc, HashSet hashMccMnc, String strPlmn, String currMccMnc) {
        Log.d(TAG, "sub =" + sub + ",strMccMnc =" + strMccMnc + ",hashMccMnc =" + hashMccMnc + ",strPlmn =" + strPlmn);
        boolean isCustomedMcc = false;
        String currentMccMnc = currMccMnc;
        Object currentMcc = null;
        if (currMccMnc.length() >= 3) {
            currentMcc = currMccMnc.substring(0, 3);
        }
        initSet(strMccMnc, hashMccMnc, strPlmn);
        if (!(hashMccMnc.size() == 0 || currMccMnc.length() == 0 || (!hashMccMnc.contains(currMccMnc) && !hashMccMnc.contains(r0)))) {
            isCustomedMcc = true;
        }
        Log.d(TAG, "isCurrentBySim()=" + isCustomedMcc);
        return isCustomedMcc;
    }

    private int updateDataTypeByCust(int dataNetType, int mDataNetType, int sub, String currMccMnc) {
        String[] strArr = null;
        Object value = null;
        Map<String, String> dataNetHashMap = new HashMap(10);
        if (!TextUtils.isEmpty(this.custBasedDataType)) {
            strArr = this.custBasedDataType.split(",");
            for (String split : strArr) {
                String[] splitListItems = split.split(":");
                dataNetHashMap.put(splitListItems[0], splitListItems[1]);
            }
        }
        if (!TextUtils.isEmpty(this.customized_network_type)) {
            if (!isCurrentBySim(sub, this.mStrDigicelPlmn, this.mDigicelMccmnc, "hw_customized_networkType_entry", currMccMnc) || r8 == null) {
                return dataNetType;
            }
        }
        String value2;
        switch (mDataNetType) {
            case 8:
                value2 = (String) dataNetHashMap.get("HSDPA");
                break;
            case 9:
                value2 = (String) dataNetHashMap.get("HSUPA");
                break;
            case 10:
                value2 = (String) dataNetHashMap.get("HSPA");
                break;
            case 15:
                value2 = (String) dataNetHashMap.get("HSPAP");
                break;
        }
        if (TextUtils.isEmpty(value)) {
            return dataNetType;
        }
        return checkForMatch(value, dataNetType);
    }

    private int checkForMatch(String value, int dataNetType) {
        if (value.equalsIgnoreCase("h+")) {
            return R.drawable.stat_sys_data_fully_connected_hplus;
        }
        if (value.equalsIgnoreCase("h")) {
            return R.drawable.stat_sys_data_fully_connected_h;
        }
        if (value.equalsIgnoreCase("3g")) {
            return R.drawable.stat_sys_data_fully_connected_3g;
        }
        if (value.equalsIgnoreCase("3g+")) {
            return R.drawable.stat_sys_data_fully_connected_3gplus;
        }
        return dataNetType;
    }

    private boolean isDataTypeByPlmn(String currentMccMnc) {
        boolean isCustomedMcc = false;
        if (!TextUtils.isEmpty(this.mSfrMccMncString) && !TextUtils.isEmpty(currentMccMnc)) {
            String[] mccmnc = this.mSfrMccMncString.trim().split(",");
            for (Object equals : mccmnc) {
                if (currentMccMnc.equals(equals)) {
                    isCustomedMcc = true;
                    break;
                }
            }
        }
        Log.d(TAG, "currentMccMnc = " + currentMccMnc + " , isDataTypeByPlmn() = " + isCustomedMcc);
        return isCustomedMcc;
    }

    public boolean isShowMmsNetworkIcon() {
        return SystemProperties.getBoolean("ro.config.show_mms_data_icon", false);
    }

    public void updateExtNetworkData(SignalStrength signalStrength, ServiceState serviceState, int callState) {
        if (IS_SGLTE) {
            int networkClass = TelephonyManager.getNetworkClass(TelephonyManager.from(this.mContext).getNetworkType(this.mInfo.getSubscriptionId()));
            int targetClass = networkClass;
            if (networkClass == 3) {
                this.mParent.mExtData.mMasterNetWorkType = 3;
                this.mParent.mExtData.mMasterNetWorkLevel = HuaweiTelephonyManagerCustEx.getDataNetworkLevel(signalStrength);
                this.mParent.mExtData.mSlaveNetWorkType = 1;
                this.mParent.mExtData.mSlaveNetWorkLevel = HuaweiTelephonyManagerCustEx.getVoiceNetworkLevel(signalStrength);
            } else if (networkClass == 2) {
                this.mParent.mExtData.mMasterNetWorkType = 2;
                this.mParent.mExtData.mMasterNetWorkLevel = HuaweiTelephonyManagerCustEx.getDataNetworkLevel(signalStrength);
                this.mParent.mExtData.mSlaveNetWorkType = 1;
                this.mParent.mExtData.mSlaveNetWorkLevel = HuaweiTelephonyManagerCustEx.getVoiceNetworkLevel(signalStrength);
            } else if (networkClass == 1) {
                this.mParent.mExtData.mMasterNetWorkType = 1;
                this.mParent.mExtData.mMasterNetWorkLevel = HuaweiTelephonyManagerCustEx.getVoiceNetworkLevel(signalStrength);
                this.mParent.mExtData.mSlaveNetWorkType = 0;
                this.mParent.mExtData.mSlaveNetWorkLevel = -1;
            } else {
                this.mParent.mExtData.mMasterNetWorkType = 0;
                this.mParent.mExtData.mMasterNetWorkLevel = -1;
                this.mParent.mExtData.mSlaveNetWorkType = 0;
                this.mParent.mExtData.mSlaveNetWorkLevel = -1;
            }
        }
    }

    private boolean isShow4point5(String currentMccMnc, String regPlmn) {
        boolean isCustShow4point5 = false;
        String strMccMnc = System.getString(this.mContext.getContentResolver(), "hw_show_4_5G_for_mcc");
        if (!(TextUtils.isEmpty(strMccMnc) || TextUtils.isEmpty(currentMccMnc) || TextUtils.isEmpty(regPlmn))) {
            String[] mccmnc = strMccMnc.split(";");
            int i = 0;
            while (i < mccmnc.length) {
                if (currentMccMnc.startsWith(mccmnc[i]) && regPlmn.startsWith(mccmnc[i])) {
                    isCustShow4point5 = true;
                }
                i++;
            }
        }
        Log.d(TAG, "isCustShow4point5 = " + isCustShow4point5);
        return isCustShow4point5;
    }

    private int getLteNetWorkTypeIcon(int dataNetType, boolean isShow4g, boolean isMccMnc, boolean isCAstate, boolean isShowLte, boolean isShow4point5, boolean isShow4point5ForCA, String currentMccMnc, String regPlmn) {
        if (mLteShow4glte || mOnlyLteShow4glte) {
            dataNetType = R.drawable.stat_sys_data_connected_4glte;
        } else if (isShow4g) {
            if (isMccMnc && isCAstate) {
                dataNetType = R.drawable.stat_sys_data_fully_connected_4gplus;
            } else {
                dataNetType = R.drawable.stat_sys_data_fully_connected_4g;
            }
        } else if (isShowLte) {
            if (isMccMnc && isCAstate) {
                dataNetType = R.drawable.stat_sys_data_fully_connected_lteplus;
            } else {
                dataNetType = R.drawable.stat_sys_data_fully_connected_lte;
            }
        } else if (isMccMnc) {
            if (isCAstate) {
                dataNetType = R.drawable.stat_sys_data_fully_connected_lteplus;
            } else {
                dataNetType = R.drawable.stat_sys_data_fully_connected_lte;
            }
        }
        if (IS_ATT) {
            dataNetType = R.drawable.stat_sys_data_fully_connected_lte;
        }
        if (isShow4point5 && isShow4point5(currentMccMnc, regPlmn)) {
            dataNetType = R.drawable.stat_sys_data_fully_connected_4point5g;
        }
        if (isShow4point5ForCA && isCAstate) {
            return R.drawable.stat_sys_data_fully_connected_4point5g;
        }
        return dataNetType;
    }
}
