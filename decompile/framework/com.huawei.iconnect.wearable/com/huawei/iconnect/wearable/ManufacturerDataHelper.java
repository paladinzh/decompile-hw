package com.huawei.iconnect.wearable;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.SystemProperties;
import java.util.HashMap;
import java.util.Locale;

public class ManufacturerDataHelper {
    public static final int BLE_DEVICE_BAND = 1;
    public static final int BLE_DEVICE_INVALID = 0;
    public static final int BLE_DEVICE_WATCH = 2;
    private static final int GOOGLE_COMPANY_ID = 224;
    private static final int HUAWEI_COMPANY_ID = 637;
    private static final int HUAWEI_TLV_MIN_LENGTH = 5;
    private static final String HUAWEI_WATCH_NAME = "huawei watch";
    public static final String HUAWEI_WATCH_NAME_LOCAL = "HUAWEI-WATCH";
    private static final String HUAWEI_WATCH_PORSCHE_NAME = "PORSCHE DESIGN";
    private static final int MAC_ADDRESS_LENGTH = 6;
    private static final String TAG = "ManufacturerDataHelper";

    static boolean isHuaweiBleDevice(int companyId) {
        return companyId == HUAWEI_COMPANY_ID;
    }

    static boolean isHuaweiBandByName(String name) {
        if (name == null) {
            return false;
        }
        HwLog.d(TAG, "isHuaweiBandByName:" + name);
        for (String str : new String[]{"honor band A1"}) {
            if (name.startsWith(str)) {
                return false;
            }
        }
        String[] bandNameArray = new String[HUAWEI_TLV_MIN_LENGTH];
        bandNameArray[0] = "honor zero-";
        bandNameArray[1] = "honor band-";
        bandNameArray[2] = "honor band Z1-";
        bandNameArray[3] = "HUAWEI Band-";
        bandNameArray[4] = "HUAWEI B3-";
        for (String str2 : bandNameArray) {
            if (name.startsWith(str2)) {
                return true;
            }
        }
        return false;
    }

    static boolean isHuaweiWatchByName(String name) {
        if (name != null) {
            return (name.toLowerCase(Locale.ENGLISH).contains(HUAWEI_WATCH_NAME) || name.startsWith(HUAWEI_WATCH_PORSCHE_NAME)) ? true : name.startsWith(HUAWEI_WATCH_NAME_LOCAL);
        } else {
            return false;
        }
    }

    public static boolean isGoogleBleDevice(int companyId, String name) {
        if (companyId != GOOGLE_COMPANY_ID || name == null) {
            return false;
        }
        return (name.toLowerCase().contains(HUAWEI_WATCH_NAME) || name.startsWith(HUAWEI_WATCH_PORSCHE_NAME)) ? true : name.startsWith(HUAWEI_WATCH_NAME_LOCAL);
    }

    static int getBleDeviceType(HashMap<Integer, byte[]> data, String address, String name) {
        return getBleDeviceType(getCompanyId(data), getManufacturerData(data), address, name);
    }

    public static int getBleDeviceType(int companyId, byte[] manufacturerData, String address, String name) {
        if (manufacturerData == null || manufacturerData.length < 2) {
            return -1;
        }
        if (isHuaweiTLVData(companyId, manufacturerData, address)) {
            return ((manufacturerData[0] & 255) << 8) + (manufacturerData[1] & 255);
        }
        if (isGoogleBleDevice(companyId, name)) {
            return manufacturerData[1] & 2;
        }
        return -1;
    }

    public static boolean isHuaweiTLVData(int companyId, byte[] manufacturerData, String address) {
        boolean z = true;
        if (manufacturerData == null || address == null) {
            return false;
        }
        HwLog.d(TAG, "companyId = " + companyId + " manufacturerData:" + toString(manufacturerData) + " address:" + address.substring(address.length() / 2));
        if (companyId != HUAWEI_COMPANY_ID) {
            return false;
        }
        if (manufacturerData.length >= MAC_ADDRESS_LENGTH) {
            int length = manufacturerData.length;
            Object[] objArr = new Object[MAC_ADDRESS_LENGTH];
            objArr[0] = Byte.valueOf(manufacturerData[length - 6]);
            objArr[1] = Byte.valueOf(manufacturerData[length - 5]);
            objArr[2] = Byte.valueOf(manufacturerData[length - 4]);
            objArr[3] = Byte.valueOf(manufacturerData[length - 3]);
            objArr[4] = Byte.valueOf(manufacturerData[length - 2]);
            objArr[HUAWEI_TLV_MIN_LENGTH] = Byte.valueOf(manufacturerData[length - 1]);
            if (String.format("%02X:%02X:%02X:%02X:%02X:%02X", objArr).equalsIgnoreCase(address)) {
                if (manufacturerData.length < 11) {
                    z = false;
                }
                return z;
            }
        }
        if (manufacturerData.length < HUAWEI_TLV_MIN_LENGTH) {
            z = false;
        }
        return z;
    }

    public static int getRemoteBleDeviceType(Context context, HashMap<Integer, byte[]> data, String address, String name) {
        return getRemoteBleDeviceType(context, getCompanyId(data), getManufacturerData(data), address, name);
    }

    private static int getRemoteBleDeviceType(Context context, int companyId, byte[] manufacturerData, String address, String name) {
        String packageName = CompanionAppHelper.getPackageNameOfCompanion(context, companyId, manufacturerData, address, name);
        if (packageName == null) {
            return 0;
        }
        if (CompanionAppHelper.HUAWEI_WARE_PACKAGE_NAME.equals(packageName)) {
            return 1;
        }
        if (CompanionAppHelper.ANDROID_WARE_CN_PACKAGE_NAME.equals(packageName) || CompanionAppHelper.ANDROID_WARE_PACKAGE_NAME.equals(packageName) || (name != null && name.startsWith(HUAWEI_WATCH_NAME_LOCAL))) {
            return 2;
        }
        return 0;
    }

    public static int getRemoteBleDeviceType(Context context, HashMap<Integer, byte[]> datas, BluetoothDevice device) {
        if (context == null || device == null) {
            HwLog.e(TAG, "getRemoteBleDeviceType param check error return");
            return 0;
        }
        String packageName = CompanionAppHelper.getPackageNameOfCompanion(context, datas, device);
        if (packageName == null) {
            return 0;
        }
        if (CompanionAppHelper.HUAWEI_WARE_PACKAGE_NAME.equals(packageName)) {
            return 1;
        }
        if (CompanionAppHelper.ANDROID_WARE_CN_PACKAGE_NAME.equals(packageName) || CompanionAppHelper.ANDROID_WARE_PACKAGE_NAME.equals(packageName) || "".equals(packageName)) {
            return 2;
        }
        return 0;
    }

    public static boolean isPhoneAndDeviceTypeUnmatched(Context context, HashMap<Integer, byte[]> datas, BluetoothDevice device) {
        String packageName = CompanionAppHelper.getPackageNameOfCompanion(context, datas, device);
        if (packageName == null) {
            return false;
        }
        boolean equals;
        boolean isGlobalVersion = isGlobalVersion();
        if (!(CompanionAppHelper.ANDROID_WARE_CN_PACKAGE_NAME.equals(packageName) && isGlobalVersion) && (!CompanionAppHelper.ANDROID_WARE_PACKAGE_NAME.equals(packageName) || isGlobalVersion)) {
            equals = packageName.equals("");
        } else {
            equals = true;
        }
        return equals;
    }

    public static boolean isPhoneAndDeviceTypeUnmatched(Context context, HashMap<Integer, byte[]> data, String address, String name) {
        boolean z = true;
        String packageName = CompanionAppHelper.getPackageNameOfCompanion(context, data, address, name);
        if (packageName == null) {
            return false;
        }
        boolean isGlobalVersion = isGlobalVersion();
        if (!(CompanionAppHelper.ANDROID_WARE_CN_PACKAGE_NAME.equals(packageName) && isGlobalVersion) && (!CompanionAppHelper.ANDROID_WARE_PACKAGE_NAME.equals(packageName) || isGlobalVersion)) {
            z = false;
        }
        return z;
    }

    private static boolean isGlobalVersion() {
        return ("zh".equals(SystemProperties.get("ro.product.locale.language")) && "CN".equals(SystemProperties.get("ro.product.locale.region"))) ? false : true;
    }

    public static boolean isHuaweiWatchPaired(Context context, HashMap<Integer, byte[]> data, String address, String name) {
        return isHuaweiWatchPaired(context, getCompanyId(data), getManufacturerData(data), address, name);
    }

    public static boolean isHuaweiWatchPaired(Context context, int companyId, byte[] manufacturerData, String address, String name) {
        if (isHuaweiTLVData(companyId, manufacturerData, address)) {
            String packageName = CompanionAppHelper.getPackageNameOfCompanion(context, companyId, manufacturerData, address, name);
            if (CompanionAppHelper.ANDROID_WARE_PACKAGE_NAME.equals(packageName) || CompanionAppHelper.ANDROID_WARE_CN_PACKAGE_NAME.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHuaweiWatchPaired(Context context, HashMap<Integer, byte[]> datas, BluetoothDevice device) {
        if (context == null || datas == null || datas.size() < 1 || device == null) {
            HwLog.e(TAG, "isHuaweiWatchPaired param check error,return");
            return false;
        }
        if (isHuaweiTLVData(getCompanyId(datas), getManufacturerData(datas), device.getAddress())) {
            String packageName = CompanionAppHelper.getPackageNameOfCompanion(context, datas, device);
            if (CompanionAppHelper.ANDROID_WARE_CN_PACKAGE_NAME.equals(packageName) || CompanionAppHelper.ANDROID_WARE_PACKAGE_NAME.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    static int getCompanyId(HashMap<Integer, byte[]> data) {
        if (data == null || data.size() <= 0) {
            return -1;
        }
        return ((Integer) data.keySet().iterator().next()).intValue();
    }

    static byte[] getManufacturerData(HashMap<Integer, byte[]> data) {
        if (data == null || data.size() <= 0) {
            return null;
        }
        return (byte[]) data.values().iterator().next();
    }

    static String toString(byte[] datas) {
        if (datas == null) {
            return null;
        }
        String result = "";
        for (byte data : datas) {
            result = result + ":" + data;
        }
        return result;
    }

    public static boolean isSupportAutoReconnect(Context context, int id, byte[] data, String address, String name) {
        boolean z = false;
        if (context == null || ((data == null && name == null) || address == null)) {
            HwLog.e(TAG, "isSupportAutoReconnect param check error");
            return false;
        }
        CompanionAppHelper.loadIfNeeded(context);
        Boolean support;
        if (isHuaweiBleDevice(id)) {
            support = (Boolean) CompanionAppHelper.mHwBleReconnectMap.get(getBleDeviceType(id, data, address, name));
            HwLog.d(TAG, "huawei bone support:" + support);
            if (support != null) {
                z = support.booleanValue();
            }
            return z;
        } else if (isGoogleBleDevice(id, name)) {
            support = (Boolean) CompanionAppHelper.mGoogleReconnecMap.get(getBleDeviceType(id, data, address, name));
            HwLog.d(TAG, "huawei watch:" + support);
            if (support != null) {
                z = support.booleanValue();
            }
            return z;
        } else if (!isHuaweiWatchByName(name)) {
            return false;
        } else {
            HwLog.d(TAG, "is huawei watch by name,return true:" + name);
            return true;
        }
    }
}
