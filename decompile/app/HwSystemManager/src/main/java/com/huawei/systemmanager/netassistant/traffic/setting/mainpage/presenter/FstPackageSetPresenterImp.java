package com.huawei.systemmanager.netassistant.traffic.setting.mainpage.presenter;

import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.traffic.setting.NatSettingManager;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model.PackageInfo;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model.TrafficPackageModel;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.ui.viewmodel.FstPackageSetView;
import com.huawei.systemmanager.netassistant.traffic.statusspeed.NatSettingInfo;
import com.huawei.systemmanager.sdk.tmsdk.netassistant.SimProfileDes;

public class FstPackageSetPresenterImp implements FstPackageSetPresenter {
    private static final String TAG = FstPackageSetPresenterImp.class.getSimpleName();
    TrafficPackageModel mModel;
    FstPackageSetView mView;

    public FstPackageSetPresenterImp(FstPackageSetView view, String imsi) {
        this.mView = view;
        this.mModel = TrafficPackageModel.getDefault(imsi);
    }

    public void init() {
        this.mView.showDefaultView(0, 0, NatSettingInfo.getUnlockScreenNotify(GlobalContext.getContext()));
        this.mView.setTrafficUnitEntries(this.mModel.getTrafficUnitRes());
        this.mView.setStartDayEntries(this.mModel.createStartDays());
    }

    public void finishPackageSet(float pkgNum, int pkgUnit, int startDay, boolean lockSwitch, SimProfileDes simProfileDes) {
        this.mModel.setPkgNum(pkgNum);
        this.mModel.setPkgUnit(pkgUnit);
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_VAL, String.valueOf(pkgUnit));
        HsmStat.statE((int) Events.E_NETASSISTANT_PACKAGE_UNIT, statParam);
        this.mModel.setStartDay(startDay);
        this.mModel.setLockSwitch(lockSwitch);
        this.mModel.setSimProfileDes(simProfileDes);
        this.mView.finishPackageSet();
    }

    public void save(float pkgNum, int pkgUnit, int startDay, boolean lockSwitch, SimProfileDes simProfileDes) {
        this.mModel.setPkgNum(pkgNum);
        this.mModel.setPkgUnit(pkgUnit);
        this.mModel.setStartDay(startDay);
        this.mModel.setLockSwitch(lockSwitch);
        PackageInfo info = new PackageInfo();
        info.setPackage(this.mModel.getPkgNum(), this.mModel.getTrafficUnit(this.mModel.getPkgUnit()));
        int startDayNum = this.mModel.getStartDay() + 1;
        long size = info.getComputableNum();
        NatSettingManager.setPackageSetting(this.mModel.getmImsi(), size, startDayNum, null, null, null, null, this.mModel.isLockSwitch());
    }
}
