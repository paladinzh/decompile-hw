package com.huawei.netassistant.service;

import android.net.NetworkTemplate;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.netassistant.common.SimCardSettingsInfo;
import com.huawei.systemmanager.sdk.tmsdk.netassistant.SimProfileDes;
import java.util.List;

public interface INetAssistantService extends IInterface {

    public static abstract class Stub extends Binder implements INetAssistantService {
        private static final String DESCRIPTOR = "com.huawei.netassistant.service.INetAssistantService";
        static final int TRANSACTION_clearDailyWarnPreference = 53;
        static final int TRANSACTION_clearMonthLimitPreference = 54;
        static final int TRANSACTION_clearMonthWarnPreference = 55;
        static final int TRANSACTION_getAbnormalMobileAppList = 6;
        static final int TRANSACTION_getAbnormalWifiAppList = 7;
        static final int TRANSACTION_getAdjustBrand = 38;
        static final int TRANSACTION_getAdjustCity = 36;
        static final int TRANSACTION_getAdjustDate = 34;
        static final int TRANSACTION_getAdjustPackageValue = 33;
        static final int TRANSACTION_getAdjustProvider = 37;
        static final int TRANSACTION_getAdjustProvince = 35;
        static final int TRANSACTION_getBackGroundBytesByUid = 49;
        static final int TRANSACTION_getDayPerHourTraffic = 47;
        static final int TRANSACTION_getForeGroundBytesByUid = 48;
        static final int TRANSACTION_getMonth4GMobileAppList = 42;
        static final int TRANSACTION_getMonthMobileTotalBytes = 1;
        static final int TRANSACTION_getMonthPerDayTraffic = 46;
        static final int TRANSACTION_getMonthTrafficDailyDetailList = 45;
        static final int TRANSACTION_getMonthWifiBackBytes = 8;
        static final int TRANSACTION_getMonthWifiForeBytes = 9;
        static final int TRANSACTION_getMonthWifiTotalBytes = 3;
        static final int TRANSACTION_getMonthlyTotalBytes = 2;
        static final int TRANSACTION_getNetworkUsageDays = 58;
        static final int TRANSACTION_getPeriodMobileTotalBytes = 10;
        static final int TRANSACTION_getPeriodMobileTrafficAppList = 43;
        static final int TRANSACTION_getPeriodWifiTotalBytes = 11;
        static final int TRANSACTION_getPeriodWifiTrafficAppList = 44;
        static final int TRANSACTION_getSettingBeginDate = 25;
        static final int TRANSACTION_getSettingExcessMontyType = 27;
        static final int TRANSACTION_getSettingNotify = 31;
        static final int TRANSACTION_getSettingOverMarkDay = 29;
        static final int TRANSACTION_getSettingOverMarkMonth = 28;
        static final int TRANSACTION_getSettingRegularAdjustType = 26;
        static final int TRANSACTION_getSettingSpeedNotify = 32;
        static final int TRANSACTION_getSettingTotalPackage = 24;
        static final int TRANSACTION_getSettingUnlockScreen = 30;
        static final int TRANSACTION_getSimCardOperatorName = 50;
        static final int TRANSACTION_getSimCardSettingsInfo = 39;
        static final int TRANSACTION_getSimProfileDes = 57;
        static final int TRANSACTION_getTodayMobileTotalBytes = 4;
        static final int TRANSACTION_getTodayWifiTotalBytes = 5;
        static final int TRANSACTION_putSimProfileDes = 56;
        static final int TRANSACTION_sendAdjustSMS = 41;
        static final int TRANSACTION_setAdjustItemInfo = 12;
        static final int TRANSACTION_setNetAccessInfo = 40;
        static final int TRANSACTION_setOperatorInfo = 14;
        static final int TRANSACTION_setProvinceInfo = 13;
        static final int TRANSACTION_setSettingBeginDate = 16;
        static final int TRANSACTION_setSettingExcessMontyType = 18;
        static final int TRANSACTION_setSettingNotify = 22;
        static final int TRANSACTION_setSettingOverMarkDay = 20;
        static final int TRANSACTION_setSettingOverMarkMonth = 19;
        static final int TRANSACTION_setSettingRegularAdjustType = 17;
        static final int TRANSACTION_setSettingSpeedNotify = 23;
        static final int TRANSACTION_setSettingTotalPackage = 15;
        static final int TRANSACTION_setSettingUnlockScreen = 21;
        static final int TRANSACTION_startSpeedUpdate = 51;
        static final int TRANSACTION_stopSpeedUpdate = 52;

        private static class Proxy implements INetAssistantService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public long getMonthMobileTotalBytes(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getMonthlyTotalBytes(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getMonthWifiTotalBytes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getTodayMobileTotalBytes(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getTodayWifiTotalBytes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List getAbnormalMobileAppList(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List getAbnormalWifiAppList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getMonthWifiBackBytes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getMonthWifiForeBytes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getPeriodMobileTotalBytes(String imsi, long start, long end) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeLong(start);
                    _data.writeLong(end);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getPeriodWifiTotalBytes(long start, long end) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(start);
                    _data.writeLong(end);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setAdjustItemInfo(String imsi, int adjustType, long value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(adjustType);
                    _data.writeLong(value);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setProvinceInfo(String imsi, String provinceCode, String cityId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeString(provinceCode);
                    _data.writeString(cityId);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setOperatorInfo(String imsi, String providerCode, String brandCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeString(providerCode);
                    _data.writeString(brandCode);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSettingTotalPackage(String imsi, long value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeLong(value);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSettingBeginDate(String imsi, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(value);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSettingRegularAdjustType(String imsi, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(value);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSettingExcessMontyType(String imsi, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(value);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSettingOverMarkMonth(String imsi, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(value);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSettingOverMarkDay(String imsi, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(value);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSettingUnlockScreen(String imsi, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(value);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSettingNotify(String imsi, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(value);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setSettingSpeedNotify(String imsi, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeInt(value);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getSettingTotalPackage(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSettingBeginDate(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSettingRegularAdjustType(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSettingExcessMontyType(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSettingOverMarkMonth(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSettingOverMarkDay(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSettingUnlockScreen(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSettingNotify(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSettingSpeedNotify(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getAdjustPackageValue(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getAdjustDate(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getAdjustProvince(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getAdjustCity(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getAdjustProvider(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getAdjustBrand(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SimCardSettingsInfo getSimCardSettingsInfo(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    SimCardSettingsInfo simCardSettingsInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        simCardSettingsInfo = (SimCardSettingsInfo) SimCardSettingsInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        simCardSettingsInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return simCardSettingsInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setNetAccessInfo(int uid, int setNetAccessInfos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(setNetAccessInfos);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendAdjustSMS(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List getMonth4GMobileAppList(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List getPeriodMobileTrafficAppList(String imsi, long startTime, long endTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeLong(startTime);
                    _data.writeLong(endTime);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List getPeriodWifiTrafficAppList(long startTime, long endTime) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(startTime);
                    _data.writeLong(endTime);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List getMonthTrafficDailyDetailList(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List getMonthPerDayTraffic(NetworkTemplate template, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (template != null) {
                        _data.writeInt(1);
                        template.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List getDayPerHourTraffic(NetworkTemplate template, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (template != null) {
                        _data.writeInt(1);
                        template.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getForeGroundBytesByUid(NetworkTemplate template, int uid, long start, long end, long now) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (template != null) {
                        _data.writeInt(1);
                        template.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    _data.writeLong(start);
                    _data.writeLong(end);
                    _data.writeLong(now);
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getBackGroundBytesByUid(NetworkTemplate template, int uid, long start, long end, long now) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (template != null) {
                        _data.writeInt(1);
                        template.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    _data.writeLong(start);
                    _data.writeLong(end);
                    _data.writeLong(now);
                    this.mRemote.transact(Stub.TRANSACTION_getBackGroundBytesByUid, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getSimCardOperatorName(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    this.mRemote.transact(50, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startSpeedUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopSpeedUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(52, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearDailyWarnPreference(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(53, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearMonthLimitPreference(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(54, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearMonthWarnPreference(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(55, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void putSimProfileDes(SimProfileDes info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(56, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getSimProfileDes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(57, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        bundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getNetworkUsageDays(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    this.mRemote.transact(Stub.TRANSACTION_getNetworkUsageDays, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetAssistantService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetAssistantService)) {
                return new Proxy(obj);
            }
            return (INetAssistantService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            long _result;
            List _result2;
            boolean _result3;
            int _result4;
            String _result5;
            NetworkTemplate networkTemplate;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getMonthMobileTotalBytes(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getMonthlyTotalBytes(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getMonthWifiTotalBytes();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getTodayMobileTotalBytes(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getTodayWifiTotalBytes();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAbnormalMobileAppList(data.readString());
                    reply.writeNoException();
                    reply.writeList(_result2);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAbnormalWifiAppList();
                    reply.writeNoException();
                    reply.writeList(_result2);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getMonthWifiBackBytes();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getMonthWifiForeBytes();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getPeriodMobileTotalBytes(data.readString(), data.readLong(), data.readLong());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getPeriodWifiTotalBytes(data.readLong(), data.readLong());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setAdjustItemInfo(data.readString(), data.readInt(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setProvinceInfo(data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setOperatorInfo(data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setSettingTotalPackage(data.readString(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setSettingBeginDate(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setSettingRegularAdjustType(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setSettingExcessMontyType(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setSettingOverMarkMonth(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setSettingOverMarkDay(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setSettingUnlockScreen(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setSettingNotify(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setSettingSpeedNotify(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSettingTotalPackage(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSettingBeginDate(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 26:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSettingRegularAdjustType(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSettingExcessMontyType(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSettingOverMarkMonth(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSettingOverMarkDay(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 30:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSettingUnlockScreen(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 31:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSettingNotify(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 32:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getSettingSpeedNotify(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 33:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAdjustPackageValue(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 34:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAdjustDate(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 35:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getAdjustProvince(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case 36:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getAdjustCity(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case 37:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getAdjustProvider(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case 38:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getAdjustBrand(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case 39:
                    data.enforceInterface(DESCRIPTOR);
                    SimCardSettingsInfo _result6 = getSimCardSettingsInfo(data.readString());
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(1);
                        _result6.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 40:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = setNetAccessInfo(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 41:
                    data.enforceInterface(DESCRIPTOR);
                    sendAdjustSMS(data.readString());
                    reply.writeNoException();
                    return true;
                case 42:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getMonth4GMobileAppList(data.readString());
                    reply.writeNoException();
                    reply.writeList(_result2);
                    return true;
                case 43:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPeriodMobileTrafficAppList(data.readString(), data.readLong(), data.readLong());
                    reply.writeNoException();
                    reply.writeList(_result2);
                    return true;
                case 44:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPeriodWifiTrafficAppList(data.readLong(), data.readLong());
                    reply.writeNoException();
                    reply.writeList(_result2);
                    return true;
                case 45:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getMonthTrafficDailyDetailList(data.readString());
                    reply.writeNoException();
                    reply.writeList(_result2);
                    return true;
                case 46:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkTemplate = (NetworkTemplate) NetworkTemplate.CREATOR.createFromParcel(data);
                    } else {
                        networkTemplate = null;
                    }
                    _result2 = getMonthPerDayTraffic(networkTemplate, data.readInt());
                    reply.writeNoException();
                    reply.writeList(_result2);
                    return true;
                case 47:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkTemplate = (NetworkTemplate) NetworkTemplate.CREATOR.createFromParcel(data);
                    } else {
                        networkTemplate = null;
                    }
                    _result2 = getDayPerHourTraffic(networkTemplate, data.readInt());
                    reply.writeNoException();
                    reply.writeList(_result2);
                    return true;
                case 48:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkTemplate = (NetworkTemplate) NetworkTemplate.CREATOR.createFromParcel(data);
                    } else {
                        networkTemplate = null;
                    }
                    _result = getForeGroundBytesByUid(networkTemplate, data.readInt(), data.readLong(), data.readLong(), data.readLong());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_getBackGroundBytesByUid /*49*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkTemplate = (NetworkTemplate) NetworkTemplate.CREATOR.createFromParcel(data);
                    } else {
                        networkTemplate = null;
                    }
                    _result = getBackGroundBytesByUid(networkTemplate, data.readInt(), data.readLong(), data.readLong(), data.readLong());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 50:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getSimCardOperatorName(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case 51:
                    data.enforceInterface(DESCRIPTOR);
                    startSpeedUpdate();
                    reply.writeNoException();
                    return true;
                case 52:
                    data.enforceInterface(DESCRIPTOR);
                    stopSpeedUpdate();
                    reply.writeNoException();
                    return true;
                case 53:
                    data.enforceInterface(DESCRIPTOR);
                    clearDailyWarnPreference(data.readString());
                    reply.writeNoException();
                    return true;
                case 54:
                    data.enforceInterface(DESCRIPTOR);
                    clearMonthLimitPreference(data.readString());
                    reply.writeNoException();
                    return true;
                case 55:
                    data.enforceInterface(DESCRIPTOR);
                    clearMonthWarnPreference(data.readString());
                    reply.writeNoException();
                    return true;
                case 56:
                    SimProfileDes simProfileDes;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        simProfileDes = (SimProfileDes) SimProfileDes.CREATOR.createFromParcel(data);
                    } else {
                        simProfileDes = null;
                    }
                    putSimProfileDes(simProfileDes);
                    reply.writeNoException();
                    return true;
                case 57:
                    data.enforceInterface(DESCRIPTOR);
                    Bundle _result7 = getSimProfileDes();
                    reply.writeNoException();
                    if (_result7 != null) {
                        reply.writeInt(1);
                        _result7.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getNetworkUsageDays /*58*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getNetworkUsageDays(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void clearDailyWarnPreference(String str) throws RemoteException;

    void clearMonthLimitPreference(String str) throws RemoteException;

    void clearMonthWarnPreference(String str) throws RemoteException;

    List getAbnormalMobileAppList(String str) throws RemoteException;

    List getAbnormalWifiAppList() throws RemoteException;

    String getAdjustBrand(String str) throws RemoteException;

    String getAdjustCity(String str) throws RemoteException;

    long getAdjustDate(String str) throws RemoteException;

    long getAdjustPackageValue(String str) throws RemoteException;

    String getAdjustProvider(String str) throws RemoteException;

    String getAdjustProvince(String str) throws RemoteException;

    long getBackGroundBytesByUid(NetworkTemplate networkTemplate, int i, long j, long j2, long j3) throws RemoteException;

    List getDayPerHourTraffic(NetworkTemplate networkTemplate, int i) throws RemoteException;

    long getForeGroundBytesByUid(NetworkTemplate networkTemplate, int i, long j, long j2, long j3) throws RemoteException;

    List getMonth4GMobileAppList(String str) throws RemoteException;

    long getMonthMobileTotalBytes(String str) throws RemoteException;

    List getMonthPerDayTraffic(NetworkTemplate networkTemplate, int i) throws RemoteException;

    List getMonthTrafficDailyDetailList(String str) throws RemoteException;

    long getMonthWifiBackBytes() throws RemoteException;

    long getMonthWifiForeBytes() throws RemoteException;

    long getMonthWifiTotalBytes() throws RemoteException;

    long getMonthlyTotalBytes(String str) throws RemoteException;

    int getNetworkUsageDays(String str) throws RemoteException;

    long getPeriodMobileTotalBytes(String str, long j, long j2) throws RemoteException;

    List getPeriodMobileTrafficAppList(String str, long j, long j2) throws RemoteException;

    long getPeriodWifiTotalBytes(long j, long j2) throws RemoteException;

    List getPeriodWifiTrafficAppList(long j, long j2) throws RemoteException;

    int getSettingBeginDate(String str) throws RemoteException;

    int getSettingExcessMontyType(String str) throws RemoteException;

    int getSettingNotify(String str) throws RemoteException;

    int getSettingOverMarkDay(String str) throws RemoteException;

    int getSettingOverMarkMonth(String str) throws RemoteException;

    int getSettingRegularAdjustType(String str) throws RemoteException;

    int getSettingSpeedNotify(String str) throws RemoteException;

    long getSettingTotalPackage(String str) throws RemoteException;

    int getSettingUnlockScreen(String str) throws RemoteException;

    String getSimCardOperatorName(int i) throws RemoteException;

    SimCardSettingsInfo getSimCardSettingsInfo(String str) throws RemoteException;

    Bundle getSimProfileDes() throws RemoteException;

    long getTodayMobileTotalBytes(String str) throws RemoteException;

    long getTodayWifiTotalBytes() throws RemoteException;

    void putSimProfileDes(SimProfileDes simProfileDes) throws RemoteException;

    void sendAdjustSMS(String str) throws RemoteException;

    boolean setAdjustItemInfo(String str, int i, long j) throws RemoteException;

    boolean setNetAccessInfo(int i, int i2) throws RemoteException;

    boolean setOperatorInfo(String str, String str2, String str3) throws RemoteException;

    boolean setProvinceInfo(String str, String str2, String str3) throws RemoteException;

    boolean setSettingBeginDate(String str, int i) throws RemoteException;

    boolean setSettingExcessMontyType(String str, int i) throws RemoteException;

    boolean setSettingNotify(String str, int i) throws RemoteException;

    boolean setSettingOverMarkDay(String str, int i) throws RemoteException;

    boolean setSettingOverMarkMonth(String str, int i) throws RemoteException;

    boolean setSettingRegularAdjustType(String str, int i) throws RemoteException;

    boolean setSettingSpeedNotify(String str, int i) throws RemoteException;

    boolean setSettingTotalPackage(String str, long j) throws RemoteException;

    boolean setSettingUnlockScreen(String str, int i) throws RemoteException;

    void startSpeedUpdate() throws RemoteException;

    void stopSpeedUpdate() throws RemoteException;
}
