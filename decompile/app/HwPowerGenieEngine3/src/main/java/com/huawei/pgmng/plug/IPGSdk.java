package com.huawei.pgmng.plug;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.util.List;
import java.util.Map;

public interface IPGSdk extends IInterface {

    public static abstract class Stub extends Binder implements IPGSdk {
        public Stub() {
            attachInterface(this, "com.huawei.pgmng.plug.IPGSdk");
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            int _result2;
            switch (code) {
                case NativeAdapter.PLATFORM_MTK /*1*/:
                    data.enforceInterface("com.huawei.pgmng.plug.IPGSdk");
                    _result = checkStateByPid(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case NativeAdapter.PLATFORM_HI /*2*/:
                    data.enforceInterface("com.huawei.pgmng.plug.IPGSdk");
                    _result = checkStateByPkg(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case NativeAdapter.PLATFORM_K3V3 /*3*/:
                    data.enforceInterface("com.huawei.pgmng.plug.IPGSdk");
                    _result2 = getPkgType(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 4:
                    data.enforceInterface("com.huawei.pgmng.plug.IPGSdk");
                    List<String> _result3 = getHibernateApps(data.readString());
                    reply.writeNoException();
                    reply.writeStringList(_result3);
                    return true;
                case 5:
                    data.enforceInterface("com.huawei.pgmng.plug.IPGSdk");
                    _result = hibernateApps(data.readString(), data.createStringArrayList(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 6:
                    data.enforceInterface("com.huawei.pgmng.plug.IPGSdk");
                    String _result4 = getTopFrontApp(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 7:
                    data.enforceInterface("com.huawei.pgmng.plug.IPGSdk");
                    int[] _result5 = getSupportedStates();
                    reply.writeNoException();
                    reply.writeIntArray(_result5);
                    return true;
                case 8:
                    data.enforceInterface("com.huawei.pgmng.plug.IPGSdk");
                    _result = isStateSupported(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 9:
                    data.enforceInterface("com.huawei.pgmng.plug.IPGSdk");
                    _result = registerSink(com.huawei.pgmng.plug.IStateRecognitionSink.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 10:
                    data.enforceInterface("com.huawei.pgmng.plug.IPGSdk");
                    _result = unregisterSink(com.huawei.pgmng.plug.IStateRecognitionSink.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 11:
                    data.enforceInterface("com.huawei.pgmng.plug.IPGSdk");
                    _result = enableStateEvent(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 12:
                    data.enforceInterface("com.huawei.pgmng.plug.IPGSdk");
                    _result = disableStateEvent(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 13:
                    data.enforceInterface("com.huawei.pgmng.plug.IPGSdk");
                    Map _result6 = getSensorInfoByUid(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeMap(_result6);
                    return true;
                case 14:
                    data.enforceInterface("com.huawei.pgmng.plug.IPGSdk");
                    _result2 = getThermalInfo(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.pgmng.plug.IPGSdk");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean checkStateByPid(String str, int i, int i2) throws RemoteException;

    boolean checkStateByPkg(String str, String str2, int i) throws RemoteException;

    boolean disableStateEvent(int i) throws RemoteException;

    boolean enableStateEvent(int i) throws RemoteException;

    List<String> getHibernateApps(String str) throws RemoteException;

    int getPkgType(String str, String str2) throws RemoteException;

    Map getSensorInfoByUid(String str, int i) throws RemoteException;

    int[] getSupportedStates() throws RemoteException;

    int getThermalInfo(String str, int i) throws RemoteException;

    String getTopFrontApp(String str) throws RemoteException;

    boolean hibernateApps(String str, List<String> list, String str2) throws RemoteException;

    boolean isStateSupported(int i) throws RemoteException;

    boolean registerSink(IStateRecognitionSink iStateRecognitionSink) throws RemoteException;

    boolean unregisterSink(IStateRecognitionSink iStateRecognitionSink) throws RemoteException;
}
