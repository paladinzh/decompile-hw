package com.huawei.systemmanager.netassistant.traffic.setting.mainpage.presenter;

import com.huawei.netassistant.cardmanager.SimCardMethod;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.traffic.setting.NatSettingManager;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model.ICodeName;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model.OperatorSubInfoWrapper;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model.PackageInfo;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model.TrafficPackageModel;
import com.huawei.systemmanager.netassistant.traffic.setting.mainpage.ui.viewmodel.FstOperatorSetView;
import com.huawei.systemmanager.netassistant.traffic.trafficinfo.TrafficPackageSettings;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class FstOperatorSetPresenterImp implements FstOperatorSetPresenter {
    private static final String TAG = FstOperatorSetPresenterImp.class.getSimpleName();
    private TrafficPackageModel mModel;
    private FstOperatorSetView mView;

    public FstOperatorSetPresenterImp(FstOperatorSetView mView, String mImsi) {
        this.mView = mView;
        this.mModel = TrafficPackageModel.getDefault(mImsi);
    }

    public void showDefaultView() {
        if (this.mModel.getProvince() != null) {
            this.mView.setProvince(this.mModel.getProvince().getName());
            this.mView.setCity(this.mModel.getCity().getName());
        }
        if (this.mModel.getOperator() != null) {
            this.mView.setOperator(this.mModel.getOperator().getName());
            this.mView.setBrand(this.mModel.getBrand().getName());
        }
    }

    public void changeProvince() {
        List<ICodeName> provinces = OperatorSubInfoWrapper.getProvinceList();
        if (!provinces.isEmpty()) {
            int select = provinces.indexOf(this.mModel.getProvince());
            HwLog.d(TAG, "changeProvince select " + select);
            this.mView.showProvinceDialog(provinces, select);
        }
    }

    public void changeCity() {
        if (this.mModel.getProvince() != null) {
            List<ICodeName> citys = OperatorSubInfoWrapper.getCityList(this.mModel.getProvince().getCode());
            int select = citys.indexOf(this.mModel.getCity());
            HwLog.d(TAG, "changeProvince select " + select);
            this.mView.showCityDialog(citys, select);
        }
    }

    public void changeOperator() {
        if (this.mModel != null) {
            ICodeName codename = this.mModel.getOperator();
            if (codename != null) {
                List<ICodeName> operators = OperatorSubInfoWrapper.getOperatorList();
                int select = operators.indexOf(codename);
                HwLog.d(TAG, "changeProvince select " + select);
                this.mView.showOperatorDialog(operators, select);
            }
        }
    }

    public void changeBrand() {
        if (this.mModel != null) {
            ICodeName codename = this.mModel.getOperator();
            ICodeName brand = this.mModel.getBrand();
            if (codename != null && brand != null) {
                String code = codename.getCode();
                if (code != null) {
                    List<ICodeName> brands = OperatorSubInfoWrapper.getBrandList(code);
                    int select = brands.indexOf(brand);
                    HwLog.d(TAG, "changeProvince select " + select);
                    this.mView.showBrandDialog(brands, select);
                }
            }
        }
    }

    public void finishOperatorSet() {
        if (this.mModel.getProvince() == null) {
            this.mView.finishOperatorSet();
            return;
        }
        PackageInfo info = new PackageInfo();
        info.setPackage(this.mModel.getPkgNum(), this.mModel.getTrafficUnit(this.mModel.getPkgUnit()));
        int startDayNum = this.mModel.getStartDay() + 1;
        long size = info.getComputableNum();
        HwLog.d(TAG, "pkgnum = " + this.mModel.getPkgNum() + " unit = " + this.mModel.getPkgUnit() + " size = " + size + " startDayNum = " + startDayNum + " locakSwitch = " + this.mModel.isLockSwitch() + " province = " + this.mModel.getProvince().getName() + " city = " + this.mModel.getCity().getName() + " operator = " + this.mModel.getOperator().getName() + " brand = " + this.mModel.getBrand().getName());
        NatSettingManager.setPackageSetting(this.mModel.getmImsi(), size, startDayNum, this.mModel.getProvince().getCode(), this.mModel.getCity().getCode(), this.mModel.getOperator().getCode(), this.mModel.getBrand().getCode(), this.mModel.isLockSwitch());
        new TrafficPackageSettings(this.mModel.getmImsi()).save(null);
        int slot = SimCardMethod.getSimCardSlotNum(GlobalContext.getContext(), this.mModel.getmImsi()) + 1;
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_VAL, String.valueOf(slot));
        HsmStat.statE((int) Events.E_NETASSISTANT_PACKAGE_SETTINGS, statParam);
        String cityInfo = String.format("%s,%s", new Object[]{this.mModel.getProvince().getName(), this.mModel.getCity().getName()});
        statParam = HsmStatConst.constructJsonParams("LOC", cityInfo);
        HsmStat.statE(92, statParam);
        this.mView.finishOperatorSet();
    }

    public void onProvinceSet(ICodeName province) {
        this.mModel.setProvince(province);
        ICodeName city = OperatorSubInfoWrapper.getDefaultCity(province.getCode());
        this.mModel.setCity(city);
        this.mView.setProvince(province.getName());
        this.mView.setCity(city.getName());
    }

    public void onCitySet(ICodeName city) {
        this.mModel.setCity(city);
        this.mView.setCity(city.getName());
    }

    public void onOperatorSet(ICodeName operator) {
        this.mModel.setOperator(operator);
        ICodeName brand = OperatorSubInfoWrapper.getDefaultBrand(operator.getCode());
        this.mModel.setBrand(brand);
        this.mView.setOperator(operator.getName());
        this.mView.setBrand(brand.getName());
    }

    public void onBrandSet(ICodeName brand) {
        this.mModel.setBrand(brand);
        this.mView.setBrand(brand.getName());
    }
}
