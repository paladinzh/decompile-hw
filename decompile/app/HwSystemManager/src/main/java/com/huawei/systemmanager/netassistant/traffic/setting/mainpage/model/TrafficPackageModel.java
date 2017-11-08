package com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model.PackageInfo.TrafficUnit;
import com.huawei.systemmanager.sdk.tmsdk.netassistant.SimProfileDes;
import com.huawei.systemmanager.util.HsmMonitor;
import com.huawei.systemmanager.util.HsmMonitor.EVENTID;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrafficPackageModel {
    public static final int DEFAULT_BEGIN_DAY = 1;
    private static final String TAG = "TrafficPackageModel";
    private static TrafficPackageModel sInstance;
    private boolean lockSwitch;
    private int mBeginDay = 1;
    private ICodeName mBrand;
    private ICodeName mCity;
    private String mImsi;
    private ICodeName mOperator;
    private INumUnit<TrafficUnit> mPackageInfo = new PackageInfo();
    private ICodeName mProvince;
    private SimProfileDes mSimProfileDes;
    private float pkgNum;
    private int pkgUnit;
    private int startDay;
    private TrafficUnit[] units = TrafficUnit.values();

    private TrafficPackageModel(String imsi) {
        this.mImsi = imsi;
    }

    public static synchronized TrafficPackageModel getDefault(String imsi) {
        TrafficPackageModel trafficPackageModel;
        synchronized (TrafficPackageModel.class) {
            if (sInstance == null) {
                sInstance = new TrafficPackageModel(imsi);
            }
            trafficPackageModel = sInstance;
        }
        return trafficPackageModel;
    }

    public void setProvince(ICodeName codeName) {
        if (this.mProvince != null) {
            this.mProvince.set(codeName);
        }
    }

    public ICodeName getProvince() {
        return this.mProvince;
    }

    public void setCity(ICodeName codeName) {
        if (this.mCity != null) {
            this.mCity.set(codeName);
        }
    }

    public ICodeName getCity() {
        return this.mCity;
    }

    public void setOperator(ICodeName codeName) {
        this.mOperator.set(codeName);
    }

    public ICodeName getOperator() {
        return this.mOperator;
    }

    public void setBrand(ICodeName codeName) {
        this.mBrand.set(codeName);
    }

    public ICodeName getBrand() {
        return this.mBrand;
    }

    public void setPackageInfo(int num, TrafficUnit unit) {
        this.mPackageInfo.setPackage((float) num, unit);
    }

    public INumUnit getPackageInfo() {
        return this.mPackageInfo;
    }

    public void setBeginDay(int day) {
        this.mBeginDay = day;
    }

    public int getBeginDay() {
        return this.mBeginDay;
    }

    public List<String> getTrafficUnitRes() {
        return Arrays.asList(GlobalContext.getContext().getResources().getStringArray(TrafficUnit.getTrafficUnitRes()));
    }

    public List<String> createStartDays() {
        Context context = GlobalContext.getContext();
        List<String> list = new ArrayList();
        for (int i = 1; i <= 31; i++) {
            list.add(context.getResources().getString(R.string.start_day_set_message, new Object[]{Integer.valueOf(i)}));
        }
        return list;
    }

    public TrafficUnit getTrafficUnit(int id) {
        return this.units[id];
    }

    public String getmImsi() {
        return this.mImsi;
    }

    public static synchronized void destoryInstance() {
        synchronized (TrafficPackageModel.class) {
            sInstance = null;
        }
    }

    public float getPkgNum() {
        return this.pkgNum;
    }

    public void setPkgNum(float pkgNum) {
        this.pkgNum = pkgNum;
    }

    public int getPkgUnit() {
        return this.pkgUnit;
    }

    public void setPkgUnit(int pkgUnit) {
        this.pkgUnit = pkgUnit;
    }

    public int getStartDay() {
        return this.startDay;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public boolean isLockSwitch() {
        return this.lockSwitch;
    }

    public void setLockSwitch(boolean lockSwitch) {
        this.lockSwitch = lockSwitch;
    }

    public SimProfileDes getSimProfileDes() {
        return this.mSimProfileDes;
    }

    public void setSimProfileDes(SimProfileDes simProfileDes) {
        int i;
        if (simProfileDes == null || !TextUtils.equals(this.mImsi, simProfileDes.imsi)) {
            simProfileDes = null;
        }
        this.mSimProfileDes = simProfileDes;
        String[] strArr = new String[2];
        strArr[0] = HsmStatConst.PARAM_VAL;
        if (this.mSimProfileDes != null) {
            i = 1;
        } else {
            i = 0;
        }
        strArr[1] = String.valueOf(i);
        HsmStat.statE((int) Events.E_NETASSISTANT_DEFAULT_OPERATOR_CHANGE, HsmStatConst.constructJsonParams(strArr));
        if (this.mSimProfileDes != null) {
            this.mProvince = OperatorSubInfoWrapper.getDefaultProvinceByICodeName(this.mSimProfileDes.province);
            if (this.mProvince != null) {
                this.mCity = OperatorSubInfoWrapper.getDefaultCityByICodeName(this.mProvince.getCode(), this.mSimProfileDes.city);
            }
            this.mOperator = OperatorSubInfoWrapper.getDefaultOperatorByICodeName(this.mSimProfileDes.carry);
            if (this.mOperator != null) {
                this.mBrand = OperatorSubInfoWrapper.getDefaultBrandByICodeName(this.mOperator.getCode());
                return;
            }
            return;
        }
        HwLog.e(TAG, "sim profile get failed");
        HsmMonitor.openEventStream(EVENTID.NETASSISTANT_OPERATOR_FAILED).setParam((short) 0, (String) null).setParam((short) 1, (String) null).sendEvent();
        this.mProvince = OperatorSubInfoWrapper.getDefaultProvince();
        if (this.mProvince != null) {
            this.mCity = OperatorSubInfoWrapper.getDefaultCity(this.mProvince.getCode());
        }
        this.mOperator = OperatorSubInfoWrapper.getDefaultOperator();
        if (this.mOperator != null) {
            this.mBrand = OperatorSubInfoWrapper.getDefaultBrand(this.mOperator.getCode());
        }
    }
}
