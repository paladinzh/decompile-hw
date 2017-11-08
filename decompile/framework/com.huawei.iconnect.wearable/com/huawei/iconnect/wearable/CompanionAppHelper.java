package com.huawei.iconnect.wearable;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.util.SparseArray;
import java.util.HashMap;

public class CompanionAppHelper {
    public static final String ANDROID_WARE_CN_PACKAGE_NAME = "com.google.android.wearable.app.cn";
    public static final String ANDROID_WARE_PACKAGE_NAME = "com.google.android.wearable.app";
    private static final int COMPANION_APP_INFO_GOOGLE_BLE_TYPE_LOCAL = 2;
    private static final int COMPANION_APP_INFO_GOOGLE_BLE_TYPE_OVERSEA = 0;
    private static final String GOOGLE_BLE_COMPANION_APP_PKG_NAME = "google_ble_companion_app_pkg_name";
    private static final String GOOGLE_BLE_DEVICE_TYPE = "google_ble_device_type";
    private static final String GOOGLE_BLE_SUPPORT_AUTO_RECONNECT = "google_ble_support_auto_reconnect";
    public static final String HUAWEI_WARE_PACKAGE_NAME = "com.huawei.bone";
    private static final String HW_BLE_COMPANION_APP_PKG_NAME = "hw_ble_companion_app_pkg_name";
    private static final String HW_BLE_DEVICE_TYPE = "hw_ble_device_type";
    private static final String HW_BLE_SUPPORT_AUTO_RECONNECT = "hw_ble_support_auto_reconnect";
    private static final String ICONNECT_PACKAGE_NAME = "com.huawei.iconnect";
    private static final String TAG = "CompanionAppHelper";
    private static SparseArray<String> mGooglePkgNameMap = new SparseArray();
    static SparseArray<Boolean> mGoogleReconnecMap = new SparseArray();
    static SparseArray<Boolean> mHwBleReconnectMap = new SparseArray();
    private static SparseArray<String> mHwPkgNameMap = new SparseArray();
    private static boolean mIsInited = false;
    private static final Object mLock = new Object();

    static boolean loadIfNeeded(Context context) {
        synchronized (mLock) {
            if (mIsInited) {
                return true;
            }
            HwLog.d(TAG, "loadIfNeeded load xml");
            PackageManager pm = context.getPackageManager();
            Context iConnectContext = context.createPackageContext(ICONNECT_PACKAGE_NAME, 0);
            if (iConnectContext == null) {
                HwLog.e(TAG, "loadIfNeeded iConnectContext == null");
                return false;
            }
            Resources res = iConnectContext.getResources();
            int[] hwtypes = res.getIntArray(res.getIdentifier(HW_BLE_DEVICE_TYPE, "array", ICONNECT_PACKAGE_NAME));
            String[] hwPkgNames = res.getStringArray(res.getIdentifier(HW_BLE_COMPANION_APP_PKG_NAME, "array", ICONNECT_PACKAGE_NAME));
            int[] hwReconnctSupport = res.getIntArray(res.getIdentifier(HW_BLE_SUPPORT_AUTO_RECONNECT, "array", ICONNECT_PACKAGE_NAME));
            if (hwtypes.length == hwPkgNames.length && hwtypes.length == hwReconnctSupport.length) {
                int i = 0;
                while (i < hwtypes.length) {
                    try {
                        mHwPkgNameMap.put(hwtypes[i], hwPkgNames[i]);
                        mHwBleReconnectMap.put(hwtypes[i], Boolean.valueOf(hwReconnctSupport[i] > 0));
                        i++;
                    } catch (NameNotFoundException e) {
                        HwLog.e(TAG, "NameNotFoundException:" + e);
                        return false;
                    }
                }
                int[] googleTypes = res.getIntArray(res.getIdentifier(GOOGLE_BLE_DEVICE_TYPE, "array", ICONNECT_PACKAGE_NAME));
                String[] googlePkgNames = res.getStringArray(res.getIdentifier(GOOGLE_BLE_COMPANION_APP_PKG_NAME, "array", ICONNECT_PACKAGE_NAME));
                int[] googleReconnectSupport = res.getIntArray(res.getIdentifier(GOOGLE_BLE_SUPPORT_AUTO_RECONNECT, "array", ICONNECT_PACKAGE_NAME));
                if (googleTypes.length == googlePkgNames.length && googleTypes.length == googleReconnectSupport.length) {
                    for (i = 0; i < googleTypes.length; i++) {
                        mGooglePkgNameMap.put(googleTypes[i], googlePkgNames[i]);
                        mGoogleReconnecMap.put(googleTypes[i], Boolean.valueOf(googleReconnectSupport[i] > 0));
                    }
                    mIsInited = true;
                    return true;
                }
                HwLog.e(TAG, "parseXml error,type,pkgName,disNames length not equals");
                return false;
            }
            HwLog.e(TAG, "parseXml error,type,pkgName,disNames length not equals");
            return false;
        }
    }

    public static String getPackageNameOfCompanion(Context context, HashMap<Integer, byte[]> datas, BluetoothDevice device) {
        if (context == null || device == null) {
            HwLog.e(TAG, "getPackageNameOfCompanion error,param check error");
            return null;
        }
        loadIfNeeded(context);
        int id = ManufacturerDataHelper.getCompanyId(datas);
        byte[] data = ManufacturerDataHelper.getManufacturerData(datas);
        String address = device.getAddress();
        String name = device.getName();
        HwLog.d(TAG, "getPackageNameOfCompanion companyid:" + id + " data:" + ManufacturerDataHelper.toString(data) + " address:" + address + " name:" + name);
        if (ManufacturerDataHelper.isHuaweiBleDevice(id)) {
            String packageName = (String) mHwPkgNameMap.get(ManufacturerDataHelper.getBleDeviceType(id, data, address, name));
            HwLog.d(TAG, "getpackageName:" + packageName);
            if (packageName == null && ManufacturerDataHelper.isHuaweiBandByName(name)) {
                packageName = HUAWEI_WARE_PACKAGE_NAME;
            }
            return packageName;
        } else if (ManufacturerDataHelper.isHuaweiBandByName(name)) {
            return HUAWEI_WARE_PACKAGE_NAME;
        } else {
            if (ManufacturerDataHelper.isGoogleBleDevice(id, name)) {
                HwLog.e(TAG, "device.getType:" + device.getType());
                if (device.getType() != 1) {
                    return (String) mGooglePkgNameMap.get(ManufacturerDataHelper.getBleDeviceType(id, data, address, name));
                } else if (name == null || !name.startsWith(ManufacturerDataHelper.HUAWEI_WATCH_NAME_LOCAL)) {
                    return "";
                } else {
                    return (String) mGooglePkgNameMap.get(2);
                }
            } else if (!ManufacturerDataHelper.isHuaweiWatchByName(name)) {
                return null;
            } else {
                HwLog.d(TAG, "getDevice type by name:" + name);
                if (name == null || !name.startsWith(ManufacturerDataHelper.HUAWEI_WATCH_NAME_LOCAL)) {
                    return "";
                }
                return (String) mGooglePkgNameMap.get(2);
            }
        }
    }

    public static String getPackageNameOfCompanion(Context context, HashMap<Integer, byte[]> data, String address, String name) {
        return getPackageNameOfCompanion(context, ManufacturerDataHelper.getCompanyId(data), ManufacturerDataHelper.getManufacturerData(data), address, name);
    }

    public static String getPackageNameOfCompanion(Context context, int id, byte[] data, String address, String name) {
        if (context == null || address == null || name == null) {
            HwLog.e(TAG, "getPackageNameOfCompanion error,param check error");
            return null;
        }
        loadIfNeeded(context);
        HwLog.d(TAG, "getPackageNameOfCompanion companyid:" + id + " data:" + ManufacturerDataHelper.toString(data) + " address:" + address + " name:" + name);
        if (ManufacturerDataHelper.isHuaweiBleDevice(id)) {
            String packageName = (String) mHwPkgNameMap.get(ManufacturerDataHelper.getBleDeviceType(id, data, address, name));
            HwLog.d(TAG, "getpackageName:" + packageName);
            if (packageName == null && ManufacturerDataHelper.isHuaweiBandByName(name)) {
                packageName = HUAWEI_WARE_PACKAGE_NAME;
            }
            return packageName;
        } else if (ManufacturerDataHelper.isHuaweiBandByName(name)) {
            return HUAWEI_WARE_PACKAGE_NAME;
        } else {
            if (!ManufacturerDataHelper.isHuaweiWatchByName(name)) {
                return null;
            }
            if (name.startsWith(ManufacturerDataHelper.HUAWEI_WATCH_NAME_LOCAL)) {
                return (String) mGooglePkgNameMap.get(2);
            }
            return "";
        }
    }
}
