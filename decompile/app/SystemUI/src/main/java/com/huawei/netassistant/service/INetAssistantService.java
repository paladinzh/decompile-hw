package com.huawei.netassistant.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface INetAssistantService extends IInterface {

    public static abstract class Stub extends Binder implements INetAssistantService {

        private static class Proxy implements INetAssistantService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public long getMonthMobileTotalBytes(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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

            public boolean setProvinceInfo(String imsi, int provinceCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    _data.writeString(imsi);
                    _data.writeInt(provinceCode);
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

            public boolean setOperatorInfo(String imsi, int providerCode, int brandCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    _data.writeString(imsi);
                    _data.writeInt(providerCode);
                    _data.writeInt(brandCode);
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
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

            public int getAdjustProvince(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    _data.writeString(imsi);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAdjustProvider(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    _data.writeString(imsi);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAdjustBrand(String imsi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    _data.writeString(imsi);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setNetAccessInfo(int uid, int setNetAccessInfos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    _data.writeInt(uid);
                    _data.writeInt(setNetAccessInfos);
                    this.mRemote.transact(38, _data, _reply, 0);
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    _data.writeString(imsi);
                    this.mRemote.transact(39, _data, _reply, 0);
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    _data.writeString(imsi);
                    this.mRemote.transact(40, _data, _reply, 0);
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    _data.writeString(imsi);
                    _data.writeLong(startTime);
                    _data.writeLong(endTime);
                    this.mRemote.transact(41, _data, _reply, 0);
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    _data.writeLong(startTime);
                    _data.writeLong(endTime);
                    this.mRemote.transact(42, _data, _reply, 0);
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    _data.writeString(imsi);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    _data.writeInt(slot);
                    this.mRemote.transact(44, _data, _reply, 0);
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    this.mRemote.transact(45, _data, _reply, 0);
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    this.mRemote.transact(46, _data, _reply, 0);
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    _data.writeString(imsi);
                    this.mRemote.transact(47, _data, _reply, 0);
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    _data.writeString(imsi);
                    this.mRemote.transact(48, _data, _reply, 0);
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
                    _data.writeInterfaceToken("com.huawei.netassistant.service.INetAssistantService");
                    _data.writeString(imsi);
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.netassistant.service.INetAssistantService");
        }

        public static INetAssistantService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.netassistant.service.INetAssistantService");
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
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result = getMonthMobileTotalBytes(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 2:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result = getMonthlyTotalBytes(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 3:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result = getMonthWifiTotalBytes();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 4:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result = getTodayMobileTotalBytes(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 5:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result = getTodayWifiTotalBytes();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 6:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result2 = getAbnormalMobileAppList(data.readString());
                    reply.writeNoException();
                    reply.writeList(_result2);
                    return true;
                case 7:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result2 = getAbnormalWifiAppList();
                    reply.writeNoException();
                    reply.writeList(_result2);
                    return true;
                case 8:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result = getMonthWifiBackBytes();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 9:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result = getMonthWifiForeBytes();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 10:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result = getPeriodMobileTotalBytes(data.readString(), data.readLong(), data.readLong());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 11:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result = getPeriodWifiTotalBytes(data.readLong(), data.readLong());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 12:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result3 = setAdjustItemInfo(data.readString(), data.readInt(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 13:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result3 = setProvinceInfo(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 14:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result3 = setOperatorInfo(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 15:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result3 = setSettingTotalPackage(data.readString(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 16:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result3 = setSettingBeginDate(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 17:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result3 = setSettingRegularAdjustType(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 18:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result3 = setSettingExcessMontyType(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 19:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result3 = setSettingOverMarkMonth(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 20:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result3 = setSettingOverMarkDay(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 21:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result3 = setSettingUnlockScreen(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 22:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result3 = setSettingNotify(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 23:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result3 = setSettingSpeedNotify(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 24:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result = getSettingTotalPackage(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 25:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result4 = getSettingBeginDate(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 26:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result4 = getSettingRegularAdjustType(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 27:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result4 = getSettingExcessMontyType(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 28:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result4 = getSettingOverMarkMonth(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 29:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result4 = getSettingOverMarkDay(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 30:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result4 = getSettingUnlockScreen(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 31:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result4 = getSettingNotify(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 32:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result4 = getSettingSpeedNotify(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 33:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result = getAdjustPackageValue(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 34:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result = getAdjustDate(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 35:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result4 = getAdjustProvince(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 36:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result4 = getAdjustProvider(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 37:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result4 = getAdjustBrand(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 38:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result3 = setNetAccessInfo(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 39:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    sendAdjustSMS(data.readString());
                    reply.writeNoException();
                    return true;
                case 40:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result2 = getMonth4GMobileAppList(data.readString());
                    reply.writeNoException();
                    reply.writeList(_result2);
                    return true;
                case 41:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result2 = getPeriodMobileTrafficAppList(data.readString(), data.readLong(), data.readLong());
                    reply.writeNoException();
                    reply.writeList(_result2);
                    return true;
                case 42:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result2 = getPeriodWifiTrafficAppList(data.readLong(), data.readLong());
                    reply.writeNoException();
                    reply.writeList(_result2);
                    return true;
                case 43:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    _result2 = getMonthTrafficDailyDetailList(data.readString());
                    reply.writeNoException();
                    reply.writeList(_result2);
                    return true;
                case 44:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    String _result5 = getSimCardOperatorName(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case 45:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    startSpeedUpdate();
                    reply.writeNoException();
                    return true;
                case 46:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    stopSpeedUpdate();
                    reply.writeNoException();
                    return true;
                case 47:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    clearDailyWarnPreference(data.readString());
                    reply.writeNoException();
                    return true;
                case 48:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    clearMonthLimitPreference(data.readString());
                    reply.writeNoException();
                    return true;
                case 49:
                    data.enforceInterface("com.huawei.netassistant.service.INetAssistantService");
                    clearMonthWarnPreference(data.readString());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.netassistant.service.INetAssistantService");
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

    int getAdjustBrand(String str) throws RemoteException;

    long getAdjustDate(String str) throws RemoteException;

    long getAdjustPackageValue(String str) throws RemoteException;

    int getAdjustProvider(String str) throws RemoteException;

    int getAdjustProvince(String str) throws RemoteException;

    List getMonth4GMobileAppList(String str) throws RemoteException;

    long getMonthMobileTotalBytes(String str) throws RemoteException;

    List getMonthTrafficDailyDetailList(String str) throws RemoteException;

    long getMonthWifiBackBytes() throws RemoteException;

    long getMonthWifiForeBytes() throws RemoteException;

    long getMonthWifiTotalBytes() throws RemoteException;

    long getMonthlyTotalBytes(String str) throws RemoteException;

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

    long getTodayMobileTotalBytes(String str) throws RemoteException;

    long getTodayWifiTotalBytes() throws RemoteException;

    void sendAdjustSMS(String str) throws RemoteException;

    boolean setAdjustItemInfo(String str, int i, long j) throws RemoteException;

    boolean setNetAccessInfo(int i, int i2) throws RemoteException;

    boolean setOperatorInfo(String str, int i, int i2) throws RemoteException;

    boolean setProvinceInfo(String str, int i) throws RemoteException;

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
