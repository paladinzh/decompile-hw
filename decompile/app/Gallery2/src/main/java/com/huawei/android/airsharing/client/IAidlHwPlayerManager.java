package com.huawei.android.airsharing.client;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.android.airsharing.api.EHwTransportState;
import com.huawei.android.airsharing.api.HwMediaInfo;
import com.huawei.android.airsharing.api.HwMediaPosition;
import com.huawei.android.airsharing.api.HwObject;
import com.huawei.android.airsharing.api.HwServer;
import java.util.List;

public interface IAidlHwPlayerManager extends IInterface {

    public static abstract class Stub extends Binder implements IAidlHwPlayerManager {

        private static class Proxy implements IAidlHwPlayerManager {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public boolean startServer(int pid, String serverName, String serverType) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    _data.writeString(serverName);
                    _data.writeString(serverType);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean stopServer(int pid) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<HwServer> getServerList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    List<HwServer> _result = _reply.createTypedArrayList(HwServer.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void subscribServers(int pid, String serverType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    _data.writeString(serverType);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unsubscribServers(int pid, String serverType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    _data.writeString(serverType);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int connectToServer(HwServer fg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    if (fg == null) {
                        _data.writeInt(0);
                    } else {
                        _data.writeInt(1);
                        fg.writeToParcel(_data, 0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disconnect() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setHwSharingListener(int pid, IAidlHwListener mHwSharingListener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    if (mHwSharingListener != null) {
                        iBinder = mHwSharingListener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clsHwSharingListener(int pid, IAidlHwListener mHwSharingListener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    if (mHwSharingListener != null) {
                        iBinder = mHwSharingListener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean PlayMedia(int pid, HwMediaInfo mediaInfo, boolean isHwAirsharing, HwObject extendObj) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    if (mediaInfo == null) {
                        _data.writeInt(0);
                    } else {
                        _data.writeInt(1);
                        mediaInfo.writeToParcel(_data, 0);
                    }
                    _data.writeInt(!isHwAirsharing ? 0 : 1);
                    if (extendObj == null) {
                        _data.writeInt(0);
                    } else {
                        _data.writeInt(1);
                        extendObj.writeToParcel(_data, 0);
                    }
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public HwMediaPosition getPosition(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    HwMediaPosition hwMediaPosition;
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        hwMediaPosition = null;
                    } else {
                        hwMediaPosition = (HwMediaPosition) HwMediaPosition.CREATOR.createFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return hwMediaPosition;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean Seek(int pid, String targetPostion) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    _data.writeString(targetPostion);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean Pause(int pid) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean Resume(int pid) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean Stop(int pid) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isRendering(int pid) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasPlayer(int pid) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public HwMediaInfo getMediaInfo(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    HwMediaInfo hwMediaInfo;
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        hwMediaInfo = null;
                    } else {
                        hwMediaInfo = (HwMediaInfo) HwMediaInfo.CREATOR.createFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return hwMediaInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSeekTarget(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVolume(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setVolume(int pid, int volume) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    _data.writeInt(volume);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyVolumeChanged(int pid, int desiredVolume) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    _data.writeInt(desiredVolume);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyPositionChanged(int pid, HwMediaPosition positionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    if (positionInfo == null) {
                        _data.writeInt(0);
                    } else {
                        _data.writeInt(1);
                        positionInfo.writeToParcel(_data, 0);
                    }
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyTransportStateChanged(int pid, EHwTransportState transState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _data.writeInt(pid);
                    if (transState == null) {
                        _data.writeInt(0);
                    } else {
                        _data.writeInt(1);
                        transState.writeToParcel(_data, 0);
                    }
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public HwServer getRenderingServer() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    HwServer hwServer;
                    _data.writeInterfaceToken("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        hwServer = null;
                    } else {
                        hwServer = (HwServer) HwServer.CREATOR.createFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return hwServer;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.android.airsharing.client.IAidlHwPlayerManager");
        }

        public static IAidlHwPlayerManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
            if (iin != null && (iin instanceof IAidlHwPlayerManager)) {
                return (IAidlHwPlayerManager) iin;
            }
            return new Proxy(obj);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            int i;
            int _result2;
            int _arg0;
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _result = startServer(data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 2:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _result = stopServer(data.readInt());
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 3:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    List<HwServer> _result3 = getServerList();
                    reply.writeNoException();
                    reply.writeTypedList(_result3);
                    return true;
                case 4:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    subscribServers(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    unsubscribServers(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 6:
                    HwServer hwServer;
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    if (data.readInt() == 0) {
                        hwServer = null;
                    } else {
                        hwServer = (HwServer) HwServer.CREATOR.createFromParcel(data);
                    }
                    _result2 = connectToServer(hwServer);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 7:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    disconnect();
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    setHwSharingListener(data.readInt(), com.huawei.android.airsharing.client.IAidlHwListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    clsHwSharingListener(data.readInt(), com.huawei.android.airsharing.client.IAidlHwListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 10:
                    HwMediaInfo hwMediaInfo;
                    boolean _arg2;
                    HwObject hwObject;
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _arg0 = data.readInt();
                    if (data.readInt() == 0) {
                        hwMediaInfo = null;
                    } else {
                        hwMediaInfo = (HwMediaInfo) HwMediaInfo.CREATOR.createFromParcel(data);
                    }
                    if (data.readInt() == 0) {
                        _arg2 = false;
                    } else {
                        _arg2 = true;
                    }
                    if (data.readInt() == 0) {
                        hwObject = null;
                    } else {
                        hwObject = (HwObject) HwObject.CREATOR.createFromParcel(data);
                    }
                    _result = PlayMedia(_arg0, hwMediaInfo, _arg2, hwObject);
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 11:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    HwMediaPosition _result4 = getPosition(data.readInt());
                    reply.writeNoException();
                    if (_result4 == null) {
                        reply.writeInt(0);
                    } else {
                        reply.writeInt(1);
                        _result4.writeToParcel(reply, 1);
                    }
                    return true;
                case 12:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _result = Seek(data.readInt(), data.readString());
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 13:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _result = Pause(data.readInt());
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 14:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _result = Resume(data.readInt());
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 15:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _result = Stop(data.readInt());
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 16:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _result = isRendering(data.readInt());
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 17:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _result = hasPlayer(data.readInt());
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 18:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    HwMediaInfo _result5 = getMediaInfo(data.readInt());
                    reply.writeNoException();
                    if (_result5 == null) {
                        reply.writeInt(0);
                    } else {
                        reply.writeInt(1);
                        _result5.writeToParcel(reply, 1);
                    }
                    return true;
                case 19:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _result2 = getSeekTarget(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 20:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _result2 = getVolume(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 21:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _result = setVolume(data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 22:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    notifyVolumeChanged(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 23:
                    HwMediaPosition hwMediaPosition;
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _arg0 = data.readInt();
                    if (data.readInt() == 0) {
                        hwMediaPosition = null;
                    } else {
                        hwMediaPosition = (HwMediaPosition) HwMediaPosition.CREATOR.createFromParcel(data);
                    }
                    notifyPositionChanged(_arg0, hwMediaPosition);
                    reply.writeNoException();
                    return true;
                case 24:
                    EHwTransportState eHwTransportState;
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    _arg0 = data.readInt();
                    if (data.readInt() == 0) {
                        eHwTransportState = null;
                    } else {
                        eHwTransportState = (EHwTransportState) EHwTransportState.CREATOR.createFromParcel(data);
                    }
                    notifyTransportStateChanged(_arg0, eHwTransportState);
                    reply.writeNoException();
                    return true;
                case 25:
                    data.enforceInterface("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    HwServer _result6 = getRenderingServer();
                    reply.writeNoException();
                    if (_result6 == null) {
                        reply.writeInt(0);
                    } else {
                        reply.writeInt(1);
                        _result6.writeToParcel(reply, 1);
                    }
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.android.airsharing.client.IAidlHwPlayerManager");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean Pause(int i) throws RemoteException;

    boolean PlayMedia(int i, HwMediaInfo hwMediaInfo, boolean z, HwObject hwObject) throws RemoteException;

    boolean Resume(int i) throws RemoteException;

    boolean Seek(int i, String str) throws RemoteException;

    boolean Stop(int i) throws RemoteException;

    void clsHwSharingListener(int i, IAidlHwListener iAidlHwListener) throws RemoteException;

    int connectToServer(HwServer hwServer) throws RemoteException;

    void disconnect() throws RemoteException;

    HwMediaInfo getMediaInfo(int i) throws RemoteException;

    HwMediaPosition getPosition(int i) throws RemoteException;

    HwServer getRenderingServer() throws RemoteException;

    int getSeekTarget(int i) throws RemoteException;

    List<HwServer> getServerList() throws RemoteException;

    int getVolume(int i) throws RemoteException;

    boolean hasPlayer(int i) throws RemoteException;

    boolean isRendering(int i) throws RemoteException;

    void notifyPositionChanged(int i, HwMediaPosition hwMediaPosition) throws RemoteException;

    void notifyTransportStateChanged(int i, EHwTransportState eHwTransportState) throws RemoteException;

    void notifyVolumeChanged(int i, int i2) throws RemoteException;

    void setHwSharingListener(int i, IAidlHwListener iAidlHwListener) throws RemoteException;

    boolean setVolume(int i, int i2) throws RemoteException;

    boolean startServer(int i, String str, String str2) throws RemoteException;

    boolean stopServer(int i) throws RemoteException;

    void subscribServers(int i, String str) throws RemoteException;

    void unsubscribServers(int i, String str) throws RemoteException;
}
