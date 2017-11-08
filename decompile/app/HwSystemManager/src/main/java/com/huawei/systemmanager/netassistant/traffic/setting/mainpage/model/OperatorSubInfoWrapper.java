package com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model;

import android.text.TextUtils;
import com.huawei.systemmanager.sdk.tmsdk.netassistant.TrafficCorrectionWrapper;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import tmsdk.bg.module.network.CodeName;

public class OperatorSubInfoWrapper {
    public static final int DEFAULT_INDEX = 0;
    private static final String TAG = "OperatorSubInfoWrapper";

    public static List<ICodeName> getProvinceList() {
        List<CodeName> names = TrafficCorrectionWrapper.getInstance().getAllProvinces();
        ArrayList<ICodeName> list = new ArrayList();
        if (names != null) {
            for (CodeName codeName : names) {
                list.add(new OperatorSubInfo(codeName.mCode, codeName.mName));
            }
        }
        return list;
    }

    public static List<ICodeName> getCityList(String provinceCode) {
        List<CodeName> names = TrafficCorrectionWrapper.getInstance().getCities(provinceCode);
        ArrayList<ICodeName> list = new ArrayList();
        if (names != null) {
            for (CodeName codeName : names) {
                list.add(new OperatorSubInfo(codeName.mCode, codeName.mName));
            }
        }
        return list;
    }

    public static List<ICodeName> getOperatorList() {
        List<CodeName> names = TrafficCorrectionWrapper.getInstance().getCarries();
        ArrayList<ICodeName> list = new ArrayList();
        if (names != null) {
            for (CodeName codeName : names) {
                list.add(new OperatorSubInfo(codeName.mCode, codeName.mName));
            }
        }
        return list;
    }

    public static List<ICodeName> getBrandList(String operatorCode) {
        List<CodeName> names = TrafficCorrectionWrapper.getInstance().getBrands(operatorCode);
        ArrayList<ICodeName> list = new ArrayList();
        if (names != null) {
            for (CodeName codeName : names) {
                list.add(new OperatorSubInfo(codeName.mCode, codeName.mName));
            }
        }
        return list;
    }

    public static ICodeName getDefaultProvince() {
        List<ICodeName> list = getProvinceList();
        if (!list.isEmpty()) {
            return (ICodeName) list.get(0);
        }
        HwLog.i(TAG, "getDefaultProvince is null");
        return null;
    }

    public static ICodeName getDefaultCity(String provinceCode) {
        return (ICodeName) getCityList(provinceCode).get(0);
    }

    public static ICodeName getDefaultOperator() {
        List<ICodeName> list = getOperatorList();
        if (!list.isEmpty()) {
            return (ICodeName) list.get(0);
        }
        HwLog.i(TAG, "getDefaultOperator is null");
        return null;
    }

    public static ICodeName getDefaultBrand(String operatorCode) {
        return (ICodeName) getBrandList(operatorCode).get(0);
    }

    public static ICodeName getDefaultProvinceByICodeName(String code) {
        List<ICodeName> list = getProvinceList();
        for (ICodeName iCodeName : list) {
            if (TextUtils.equals(iCodeName.getCode(), code)) {
                return iCodeName;
            }
        }
        if (!list.isEmpty()) {
            return (ICodeName) list.get(0);
        }
        HwLog.i(TAG, "getDefaultProvinceByICodeName is null");
        return null;
    }

    public static ICodeName getDefaultCityByICodeName(String provinceCode, String code) {
        List<ICodeName> list = getCityList(provinceCode);
        for (ICodeName iCodeName : list) {
            if (TextUtils.equals(iCodeName.getCode(), code)) {
                return iCodeName;
            }
        }
        return (ICodeName) list.get(0);
    }

    public static ICodeName getDefaultOperatorByICodeName(String code) {
        List<ICodeName> list = getOperatorList();
        for (ICodeName iCodeName : list) {
            if (TextUtils.equals(iCodeName.getCode(), code)) {
                return iCodeName;
            }
        }
        if (!list.isEmpty()) {
            return (ICodeName) list.get(0);
        }
        HwLog.i(TAG, "getDefaultOperatorByICodeName is null");
        return null;
    }

    public static ICodeName getDefaultBrandByICodeName(String operatorCode) {
        List<ICodeName> list = getBrandList(operatorCode);
        return (ICodeName) list.get(list.size() >= 2 ? 1 : 0);
    }
}
