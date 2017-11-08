package com.huawei.rcs.commonInterface;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Surface;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.Place;
import com.huawei.rcs.commonInterface.metadata.Capabilities;
import com.huawei.rcs.commonInterface.metadata.PeerInformation;
import java.util.List;
import java.util.Map;

public interface IfMsgplus extends IInterface {

    public static abstract class Stub extends Binder implements IfMsgplus {

        private static class Proxy implements IfMsgplus {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public int login(String number, String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(number);
                    _data.writeString(password);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int registerCallback(int eventId, IfMsgplusCb cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeInt(eventId);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unRegisterCallback(int eventId, IfMsgplusCb cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeInt(eventId);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendMessageIm(String msg, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(msg);
                    _data.writeString(address);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendMessageImWithLocalId(String msg, String address, long msgIdBeforeSend) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(msg);
                    _data.writeString(address);
                    _data.writeLong(msgIdBeforeSend);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendComposingState(String address, int composingType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(address);
                    _data.writeInt(composingType);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendFile(String msg, String address, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(msg);
                    _data.writeString(address);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int groupSendFile(String groupID, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(groupID);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cancelFile(long msgid, boolean isOutGoing, long chatType) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeLong(msgid);
                    if (isOutGoing) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeLong(chatType);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int receiveFile(long msgID, long chatTye) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeLong(msgID);
                    _data.writeLong(chatTye);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int rejectFile(long msgID, long chatTye) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeLong(msgID);
                    _data.writeLong(chatTye);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int incomingContentShareHandle(long imgID, boolean isAccept) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeLong(imgID);
                    if (isAccept) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cancelImage(long imgID, boolean isOutGoing) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeLong(imgID);
                    if (isOutGoing) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendImage(String msg, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(msg);
                    _data.writeString(address);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendVideo(String msg, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(msg);
                    _data.writeString(address);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendVoice(String msg, String address, int duration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(msg);
                    _data.writeString(address);
                    _data.writeInt(duration);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int groupSendVoice(String msg, String groupId, int duration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(msg);
                    _data.writeString(groupId);
                    _data.writeInt(duration);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendLocation(String address, double latitude, double longitude, String title, String subTitle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(address);
                    _data.writeDouble(latitude);
                    _data.writeDouble(longitude);
                    _data.writeString(title);
                    _data.writeString(subTitle);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int chatSendLocation(String address, double latitude, double longitude, String title, String subTitle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(address);
                    _data.writeDouble(latitude);
                    _data.writeDouble(longitude);
                    _data.writeString(title);
                    _data.writeString(subTitle);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendMassLocation(List<String> address, double latitude, double longitude, String title, String subTitle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeStringList(address);
                    _data.writeDouble(latitude);
                    _data.writeDouble(longitude);
                    _data.writeString(title);
                    _data.writeString(subTitle);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int groupSendLocation(String groupId, double latitude, double longitude, String title, String subTitle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(groupId);
                    _data.writeDouble(latitude);
                    _data.writeDouble(longitude);
                    _data.writeString(title);
                    _data.writeString(subTitle);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int resendGroupMessageLocation(long msgId, String groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeLong(msgId);
                    _data.writeString(groupId);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isRcsUeser(String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(number);
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

            public Capabilities getContactCapabilities(String MSISDN) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Capabilities capabilities;
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(MSISDN);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        capabilities = (Capabilities) Capabilities.CREATOR.createFromParcel(_reply);
                    } else {
                        capabilities = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return capabilities;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Capabilities getMyCapabilities() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Capabilities capabilities;
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        capabilities = (Capabilities) Capabilities.CREATOR.createFromParcel(_reply);
                    } else {
                        capabilities = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return capabilities;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int requestContactCapabilities(String contact) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(contact);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendImReadReport(String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(address);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setRequestDeliveryStatus(boolean request) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (request) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setRequestDisplayStatus(boolean request) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (request) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setAllowSendDisplayStatus(boolean allow) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (allow) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getLoginState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(31, _data, _reply, 0);
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

            public int getMaxFileSizePermited() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getWarFileSizePermited() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String createGroup(String topic, List<PeerInformation> members) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(topic);
                    _data.writeTypedList(members);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String createGroupByNumList(String topic, List<String> members) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(topic);
                    _data.writeStringList(members);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendGroupMessage(String groupID, String msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(groupID);
                    _data.writeString(msg);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendGroupMessageWithLocalId(String groupID, String msg, long localId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(groupID);
                    _data.writeString(msg);
                    _data.writeLong(localId);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getGlobalGroupID(String groupID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(groupID);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getGroupMemberCount(String groupID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(groupID);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateGroupTopic(String groupID, String groupTopic) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(groupID);
                    _data.writeString(groupTopic);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getGroupTopic(String groupID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(groupID);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void readGroupChatMessage(String groupID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(groupID);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeConversation(String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(address);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteMessageItem(long msgid, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeLong(msgid);
                    _data.writeString(address);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void makeLogout() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int initVideoShare(String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(number);
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int acceptVideoShare() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int termVideoShare(boolean isCaller) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (isCaller) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int connectVideoShare(boolean isCaller) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (isCaller) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setLocalVideoSurfaceHandle(Surface videoShareSurface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (videoShareSurface != null) {
                        _data.writeInt(1);
                        videoShareSurface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(50, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setRemoteVideoSurfaceHandle(Surface videoShareSurface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (videoShareSurface != null) {
                        _data.writeInt(1);
                        videoShareSurface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteLocalVideoSurfaceHandle(Surface videoShareSurface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (videoShareSurface != null) {
                        _data.writeInt(1);
                        videoShareSurface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(52, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteLocalVideoSurface(Surface videoShareSurface, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (videoShareSurface != null) {
                        _data.writeInt(1);
                        videoShareSurface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(53, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteRemoteVideoSurfaceHandle(Surface videoShareSurface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (videoShareSurface != null) {
                        _data.writeInt(1);
                        videoShareSurface.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(54, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int switchVideoShareCamera() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(55, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setVideoShareCameraRotate(int orientation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeInt(orientation);
                    this.mRemote.transact(56, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addGroupMembers(String groupID, List<PeerInformation> members) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(groupID);
                    _data.writeTypedList(members);
                    this.mRemote.transact(57, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void exitGroup(String groupID, boolean bClearRecord) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(groupID);
                    if (bClearRecord) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(58, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAutoAcceptGroupInviter(boolean autoAccept) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (autoAccept) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(59, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void acceptGroupInviteAccept(String globalgroupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(globalgroupId);
                    this.mRemote.transact(60, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void rejectGroupInvite(String globalgroupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(globalgroupId);
                    this.mRemote.transact(61, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Map checkUserLoginStatusByNumber(List<String> numbers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeStringList(numbers);
                    this.mRemote.transact(62, _data, _reply, 0);
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isImAvailable(String phoneNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(phoneNum);
                    this.mRemote.transact(63, _data, _reply, 0);
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

            public boolean isImAvailableWithTimeOut(String phoneNum, boolean ignoreTimeOut) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(phoneNum);
                    if (ignoreTimeOut) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(64, _data, _reply, 0);
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

            public boolean needStoreNotification(String phoneNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(phoneNum);
                    this.mRemote.transact(65, _data, _reply, 0);
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

            public boolean isFtSupportStoreAndFoward() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(66, _data, _reply, 0);
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

            public boolean isFtAvailable(String phoneNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(phoneNum);
                    this.mRemote.transact(67, _data, _reply, 0);
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

            public boolean isGroupMember(String groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(groupId);
                    this.mRemote.transact(68, _data, _reply, 0);
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

            public void acceptImSession(String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(number);
                    this.mRemote.transact(69, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getImSesionStart() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(70, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMaxGroupMemberSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(71, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setGroupMessageRequestDeliveryStatus(boolean request) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (request) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(72, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getNumMatch() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(73, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendGroupComposingState(String groupId, int composingType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(groupId);
                    _data.writeInt(composingType);
                    this.mRemote.transact(74, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isImWarnSfOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(75, _data, _reply, 0);
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

            public int getMsgCapValidityTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(76, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean compareUri(String number1, String number2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(number1);
                    _data.writeString(number2);
                    this.mRemote.transact(77, _data, _reply, 0);
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

            public boolean isImSupportStoreAndFoward() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(78, _data, _reply, 0);
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

            public boolean warnIMmessageDeferredIfNeed(String phoneNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(phoneNum);
                    this.mRemote.transact(79, _data, _reply, 0);
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

            public int resendMessageIm(long msgId, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeLong(msgId);
                    _data.writeString(address);
                    this.mRemote.transact(80, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendFtReadReport(long msgID, long chatTye) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeLong(msgID);
                    _data.writeLong(chatTye);
                    this.mRemote.transact(81, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setCsCallIdle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(82, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setCsCallIdleWithNumber(String phoneNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(phoneNumber);
                    this.mRemote.transact(83, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setCsCallOffHook(String phoneNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(phoneNumber);
                    this.mRemote.transact(84, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isGroupOwner(String groupId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(groupId);
                    this.mRemote.transact(85, _data, _reply, 0);
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

            public String getCurrentLoginUserNumber() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(86, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getLocalPhoneNumber() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(87, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int resendMessageFile(long msgId, long chatType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeLong(msgId);
                    _data.writeLong(chatType);
                    this.mRemote.transact(88, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getXmlConfigValue(int id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeInt(id);
                    this.mRemote.transact(89, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int requestCapabilitiesInCsCall(String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(number);
                    this.mRemote.transact(90, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int saveNickname(String nickname) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(nickname);
                    this.mRemote.transact(91, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getDmConfig(int configNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeInt(configNum);
                    this.mRemote.transact(92, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRcsSwitcherDefaultStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(93, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getGroupNickname() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(94, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendFileWithLocalId(String msg, String address, long msgIdBeforeSend, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(msg);
                    _data.writeString(address);
                    _data.writeLong(msgIdBeforeSend);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(95, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int groupSendFileWithLocalId(String groupID, Bundle bundle, long localId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(groupID);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(localId);
                    this.mRemote.transact(96, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean preCallCreate(String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(number);
                    this.mRemote.transact(97, _data, _reply, 0);
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

            public boolean preCallDestroy() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(98, _data, _reply, 0);
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

            public void preCallAccept(String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(number);
                    this.mRemote.transact(99, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void preCallReject(String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(number);
                    this.mRemote.transact(100, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void preCallsetCallState(int callState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeInt(callState);
                    this.mRemote.transact(101, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void preCallSendImage(String imagePath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(imagePath);
                    this.mRemote.transact(102, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void preCallSendImportance(int importance) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeInt(importance);
                    this.mRemote.transact(OfflineMapStatus.EXCEPTION_SDCARD, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void preCallSendCompserInfo(Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(LocationRequest.PRIORITY_LOW_POWER, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void preCallsetCallStateByNumber(String number, int callState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(number);
                    _data.writeInt(callState);
                    this.mRemote.transact(LocationRequest.PRIORITY_NO_POWER, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void preCallSendImageByNumber(String number, String imagePath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(number);
                    _data.writeString(imagePath);
                    this.mRemote.transact(106, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void preCallSendImportanceByNumber(String number, int importance) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(number);
                    _data.writeInt(importance);
                    this.mRemote.transact(107, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void preCallSendCompserInfoByNumber(String number, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(number);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(108, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getVoiceMessageMaxDuration(String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(address);
                    this.mRemote.transact(109, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getVoiceMessageMaxSize(String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(address);
                    this.mRemote.transact(110, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendNote(String note, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(note);
                    _data.writeString(address);
                    this.mRemote.transact(111, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendAudioMessage(String audioFilePath, int duration, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(audioFilePath);
                    _data.writeInt(duration);
                    _data.writeString(address);
                    this.mRemote.transact(112, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void acceptAudioMessage(long msgId, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeLong(msgId);
                    _data.writeString(address);
                    this.mRemote.transact(113, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void rejectAudioMessage(long msgId, String address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeLong(msgId);
                    _data.writeString(address);
                    this.mRemote.transact(114, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getUsualMessages() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(115, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getQuickResponds() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(116, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean shareSketchInit(String number, int sharetype) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(number);
                    _data.writeInt(sharetype);
                    this.mRemote.transact(117, _data, _reply, 0);
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

            public void shareSketchCreate(boolean isreconnect) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (isreconnect) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(118, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shareSketchEnd() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(119, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shareSketchAccept() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(120, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shareSketchReject() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(121, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shareSketchCancel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    this.mRemote.transact(122, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shareSketchSendInfo(Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(123, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shareSketchSaveDB(Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(124, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setIncallChatThreadId(long threadId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeLong(threadId);
                    this.mRemote.transact(125, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getIncallChatState(long threadId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeLong(threadId);
                    this.mRemote.transact(126, _data, _reply, 0);
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

            public boolean hasConversation(String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplus");
                    _data.writeString(number);
                    this.mRemote.transact(127, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, "com.huawei.rcs.commonInterface.IfMsgplus");
        }

        public static IfMsgplus asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.rcs.commonInterface.IfMsgplus");
            if (iin == null || !(iin instanceof IfMsgplus)) {
                return new Proxy(obj);
            }
            return (IfMsgplus) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            String _arg0;
            String _arg1;
            Bundle bundle;
            boolean _result2;
            Capabilities _result3;
            String _result4;
            Surface surface;
            Bundle bundle2;
            long _result5;
            String[] _result6;
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = login(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = registerCallback(data.readInt(), com.huawei.rcs.commonInterface.IfMsgplusCb.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 3:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    unRegisterCallback(data.readInt(), com.huawei.rcs.commonInterface.IfMsgplusCb.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = sendMessageIm(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 5:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = sendMessageImWithLocalId(data.readString(), data.readString(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 6:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = sendComposingState(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 7:
                    Bundle bundle3;
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _arg0 = data.readString();
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        bundle3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle3 = null;
                    }
                    _result = sendFile(_arg0, _arg1, bundle3);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 8:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = groupSendFile(_arg0, bundle);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 9:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = cancelFile(data.readLong(), data.readInt() != 0, data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 10:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = receiveFile(data.readLong(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 11:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = rejectFile(data.readLong(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 12:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = incomingContentShareHandle(data.readLong(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 13:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = cancelImage(data.readLong(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 14:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = sendImage(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 15:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = sendVideo(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 16:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = sendVoice(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 17:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = groupSendVoice(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 18:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = sendLocation(data.readString(), data.readDouble(), data.readDouble(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 19:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = chatSendLocation(data.readString(), data.readDouble(), data.readDouble(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 20:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = sendMassLocation(data.createStringArrayList(), data.readDouble(), data.readDouble(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 21:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = groupSendLocation(data.readString(), data.readDouble(), data.readDouble(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 22:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = resendGroupMessageLocation(data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 23:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = isRcsUeser(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 24:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result3 = getContactCapabilities(data.readString());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(1);
                        _result3.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 25:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result3 = getMyCapabilities();
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(1);
                        _result3.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 26:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = requestContactCapabilities(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 27:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = sendImReadReport(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 28:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = setRequestDeliveryStatus(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 29:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = setRequestDisplayStatus(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 30:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = setAllowSendDisplayStatus(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 31:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = getLoginState();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 32:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = getMaxFileSizePermited();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 33:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = getWarFileSizePermited();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 34:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result4 = createGroup(data.readString(), data.createTypedArrayList(PeerInformation.CREATOR));
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 35:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result4 = createGroupByNumList(data.readString(), data.createStringArrayList());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 36:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    sendGroupMessage(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 37:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    sendGroupMessageWithLocalId(data.readString(), data.readString(), data.readLong());
                    reply.writeNoException();
                    return true;
                case 38:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result4 = getGlobalGroupID(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 39:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = getGroupMemberCount(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 40:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    updateGroupTopic(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 41:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result4 = getGroupTopic(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 42:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    readGroupChatMessage(data.readString());
                    reply.writeNoException();
                    return true;
                case 43:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    removeConversation(data.readString());
                    reply.writeNoException();
                    return true;
                case 44:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    deleteMessageItem(data.readLong(), data.readString());
                    reply.writeNoException();
                    return true;
                case Place.TYPE_HAIR_CARE /*45*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    makeLogout();
                    reply.writeNoException();
                    return true;
                case Place.TYPE_HARDWARE_STORE /*46*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = initVideoShare(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_HEALTH /*47*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = acceptVideoShare();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_HINDU_TEMPLE /*48*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = termVideoShare(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_HOME_GOODS_STORE /*49*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = connectVideoShare(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_HOSPITAL /*50*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (data.readInt() != 0) {
                        surface = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        surface = null;
                    }
                    _result = setLocalVideoSurfaceHandle(surface);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_INSURANCE_AGENCY /*51*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (data.readInt() != 0) {
                        surface = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        surface = null;
                    }
                    _result = setRemoteVideoSurfaceHandle(surface);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_JEWELRY_STORE /*52*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (data.readInt() != 0) {
                        surface = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        surface = null;
                    }
                    _result = deleteLocalVideoSurfaceHandle(surface);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_LAUNDRY /*53*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (data.readInt() != 0) {
                        surface = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        surface = null;
                    }
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = deleteLocalVideoSurface(surface, bundle);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_LAWYER /*54*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (data.readInt() != 0) {
                        surface = (Surface) Surface.CREATOR.createFromParcel(data);
                    } else {
                        surface = null;
                    }
                    _result = deleteRemoteVideoSurfaceHandle(surface);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_LIBRARY /*55*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = switchVideoShareCamera();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_LIQUOR_STORE /*56*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = setVideoShareCameraRotate(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_LOCAL_GOVERNMENT_OFFICE /*57*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    addGroupMembers(data.readString(), data.createTypedArrayList(PeerInformation.CREATOR));
                    reply.writeNoException();
                    return true;
                case Place.TYPE_LOCKSMITH /*58*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    exitGroup(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case Place.TYPE_LODGING /*59*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    setAutoAcceptGroupInviter(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case Place.TYPE_MEAL_DELIVERY /*60*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    acceptGroupInviteAccept(data.readString());
                    reply.writeNoException();
                    return true;
                case Place.TYPE_MEAL_TAKEAWAY /*61*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    rejectGroupInvite(data.readString());
                    reply.writeNoException();
                    return true;
                case Place.TYPE_MOSQUE /*62*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    Map _result7 = checkUserLoginStatusByNumber(data.createStringArrayList());
                    reply.writeNoException();
                    reply.writeMap(_result7);
                    return true;
                case Place.TYPE_MOVIE_RENTAL /*63*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = isImAvailable(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case Place.TYPE_MOVIE_THEATER /*64*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = isImAvailableWithTimeOut(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case Place.TYPE_MOVING_COMPANY /*65*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = needStoreNotification(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case Place.TYPE_MUSEUM /*66*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = isFtSupportStoreAndFoward();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case Place.TYPE_NIGHT_CLUB /*67*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = isFtAvailable(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case Place.TYPE_PAINTER /*68*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = isGroupMember(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case Place.TYPE_PARK /*69*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    acceptImSession(data.readString());
                    reply.writeNoException();
                    return true;
                case Place.TYPE_PARKING /*70*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = getImSesionStart();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_PET_STORE /*71*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = getMaxGroupMemberSize();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_PHARMACY /*72*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = setGroupMessageRequestDeliveryStatus(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_PHYSIOTHERAPIST /*73*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = getNumMatch();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_PLACE_OF_WORSHIP /*74*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = sendGroupComposingState(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_PLUMBER /*75*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = isImWarnSfOn();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case Place.TYPE_POLICE /*76*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = getMsgCapValidityTime();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_POST_OFFICE /*77*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = compareUri(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case Place.TYPE_REAL_ESTATE_AGENCY /*78*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = isImSupportStoreAndFoward();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case Place.TYPE_RESTAURANT /*79*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = warnIMmessageDeferredIfNeed(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case Place.TYPE_ROOFING_CONTRACTOR /*80*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = resendMessageIm(data.readLong(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_RV_PARK /*81*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = sendFtReadReport(data.readLong(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_SCHOOL /*82*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = setCsCallIdle();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_SHOE_STORE /*83*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = setCsCallIdleWithNumber(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_SHOPPING_MALL /*84*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = setCsCallOffHook(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_SPA /*85*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = isGroupOwner(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case Place.TYPE_STADIUM /*86*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result4 = getCurrentLoginUserNumber();
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case Place.TYPE_STORAGE /*87*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result4 = getLocalPhoneNumber();
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case Place.TYPE_STORE /*88*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = resendMessageFile(data.readLong(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_SUBWAY_STATION /*89*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result4 = getXmlConfigValue(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case Place.TYPE_SYNAGOGUE /*90*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = requestCapabilitiesInCsCall(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_TAXI_STAND /*91*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = saveNickname(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_TRAIN_STATION /*92*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result4 = getDmConfig(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case Place.TYPE_TRAVEL_AGENCY /*93*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result = getRcsSwitcherDefaultStatus();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_UNIVERSITY /*94*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result4 = getGroupNickname();
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case Place.TYPE_VETERINARY_CARE /*95*/:
                    Bundle bundle4;
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _arg0 = data.readString();
                    _arg1 = data.readString();
                    long _arg2 = data.readLong();
                    if (data.readInt() != 0) {
                        bundle4 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle4 = null;
                    }
                    _result = sendFileWithLocalId(_arg0, _arg1, _arg2, bundle4);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case Place.TYPE_ZOO /*96*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = groupSendFileWithLocalId(_arg0, bundle, data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 97:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = preCallCreate(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 98:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = preCallDestroy();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 99:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    preCallAccept(data.readString());
                    reply.writeNoException();
                    return true;
                case 100:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    preCallReject(data.readString());
                    reply.writeNoException();
                    return true;
                case 101:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    preCallsetCallState(data.readInt());
                    reply.writeNoException();
                    return true;
                case 102:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    preCallSendImage(data.readString());
                    reply.writeNoException();
                    return true;
                case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    preCallSendImportance(data.readInt());
                    reply.writeNoException();
                    return true;
                case LocationRequest.PRIORITY_LOW_POWER /*104*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    preCallSendCompserInfo(bundle2);
                    reply.writeNoException();
                    return true;
                case LocationRequest.PRIORITY_NO_POWER /*105*/:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    preCallsetCallStateByNumber(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 106:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    preCallSendImageByNumber(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 107:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    preCallSendImportanceByNumber(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 108:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    preCallSendCompserInfoByNumber(_arg0, bundle);
                    reply.writeNoException();
                    return true;
                case 109:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result5 = getVoiceMessageMaxDuration(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result5);
                    return true;
                case 110:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result5 = getVoiceMessageMaxSize(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result5);
                    return true;
                case 111:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    sendNote(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 112:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    sendAudioMessage(data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 113:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    acceptAudioMessage(data.readLong(), data.readString());
                    reply.writeNoException();
                    return true;
                case 114:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    rejectAudioMessage(data.readLong(), data.readString());
                    reply.writeNoException();
                    return true;
                case 115:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result6 = getUsualMessages();
                    reply.writeNoException();
                    reply.writeStringArray(_result6);
                    return true;
                case 116:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result6 = getQuickResponds();
                    reply.writeNoException();
                    reply.writeStringArray(_result6);
                    return true;
                case 117:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = shareSketchInit(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 118:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    shareSketchCreate(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 119:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    shareSketchEnd();
                    reply.writeNoException();
                    return true;
                case 120:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    shareSketchAccept();
                    reply.writeNoException();
                    return true;
                case 121:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    shareSketchReject();
                    reply.writeNoException();
                    return true;
                case 122:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    shareSketchCancel();
                    reply.writeNoException();
                    return true;
                case 123:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    shareSketchSendInfo(bundle2);
                    reply.writeNoException();
                    return true;
                case 124:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    shareSketchSaveDB(bundle2);
                    reply.writeNoException();
                    return true;
                case 125:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    setIncallChatThreadId(data.readLong());
                    reply.writeNoException();
                    return true;
                case 126:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = getIncallChatState(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 127:
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplus");
                    _result2 = hasConversation(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.rcs.commonInterface.IfMsgplus");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void acceptAudioMessage(long j, String str) throws RemoteException;

    void acceptGroupInviteAccept(String str) throws RemoteException;

    void acceptImSession(String str) throws RemoteException;

    int acceptVideoShare() throws RemoteException;

    void addGroupMembers(String str, List<PeerInformation> list) throws RemoteException;

    int cancelFile(long j, boolean z, long j2) throws RemoteException;

    int cancelImage(long j, boolean z) throws RemoteException;

    int chatSendLocation(String str, double d, double d2, String str2, String str3) throws RemoteException;

    Map checkUserLoginStatusByNumber(List<String> list) throws RemoteException;

    boolean compareUri(String str, String str2) throws RemoteException;

    int connectVideoShare(boolean z) throws RemoteException;

    String createGroup(String str, List<PeerInformation> list) throws RemoteException;

    String createGroupByNumList(String str, List<String> list) throws RemoteException;

    int deleteLocalVideoSurface(Surface surface, Bundle bundle) throws RemoteException;

    int deleteLocalVideoSurfaceHandle(Surface surface) throws RemoteException;

    void deleteMessageItem(long j, String str) throws RemoteException;

    int deleteRemoteVideoSurfaceHandle(Surface surface) throws RemoteException;

    void exitGroup(String str, boolean z) throws RemoteException;

    Capabilities getContactCapabilities(String str) throws RemoteException;

    String getCurrentLoginUserNumber() throws RemoteException;

    String getDmConfig(int i) throws RemoteException;

    String getGlobalGroupID(String str) throws RemoteException;

    int getGroupMemberCount(String str) throws RemoteException;

    String getGroupNickname() throws RemoteException;

    String getGroupTopic(String str) throws RemoteException;

    int getImSesionStart() throws RemoteException;

    boolean getIncallChatState(long j) throws RemoteException;

    String getLocalPhoneNumber() throws RemoteException;

    boolean getLoginState() throws RemoteException;

    int getMaxFileSizePermited() throws RemoteException;

    int getMaxGroupMemberSize() throws RemoteException;

    int getMsgCapValidityTime() throws RemoteException;

    Capabilities getMyCapabilities() throws RemoteException;

    int getNumMatch() throws RemoteException;

    String[] getQuickResponds() throws RemoteException;

    int getRcsSwitcherDefaultStatus() throws RemoteException;

    String[] getUsualMessages() throws RemoteException;

    long getVoiceMessageMaxDuration(String str) throws RemoteException;

    long getVoiceMessageMaxSize(String str) throws RemoteException;

    int getWarFileSizePermited() throws RemoteException;

    String getXmlConfigValue(int i) throws RemoteException;

    int groupSendFile(String str, Bundle bundle) throws RemoteException;

    int groupSendFileWithLocalId(String str, Bundle bundle, long j) throws RemoteException;

    int groupSendLocation(String str, double d, double d2, String str2, String str3) throws RemoteException;

    int groupSendVoice(String str, String str2, int i) throws RemoteException;

    boolean hasConversation(String str) throws RemoteException;

    int incomingContentShareHandle(long j, boolean z) throws RemoteException;

    int initVideoShare(String str) throws RemoteException;

    boolean isFtAvailable(String str) throws RemoteException;

    boolean isFtSupportStoreAndFoward() throws RemoteException;

    boolean isGroupMember(String str) throws RemoteException;

    boolean isGroupOwner(String str) throws RemoteException;

    boolean isImAvailable(String str) throws RemoteException;

    boolean isImAvailableWithTimeOut(String str, boolean z) throws RemoteException;

    boolean isImSupportStoreAndFoward() throws RemoteException;

    boolean isImWarnSfOn() throws RemoteException;

    boolean isRcsUeser(String str) throws RemoteException;

    int login(String str, String str2) throws RemoteException;

    void makeLogout() throws RemoteException;

    boolean needStoreNotification(String str) throws RemoteException;

    void preCallAccept(String str) throws RemoteException;

    boolean preCallCreate(String str) throws RemoteException;

    boolean preCallDestroy() throws RemoteException;

    void preCallReject(String str) throws RemoteException;

    void preCallSendCompserInfo(Bundle bundle) throws RemoteException;

    void preCallSendCompserInfoByNumber(String str, Bundle bundle) throws RemoteException;

    void preCallSendImage(String str) throws RemoteException;

    void preCallSendImageByNumber(String str, String str2) throws RemoteException;

    void preCallSendImportance(int i) throws RemoteException;

    void preCallSendImportanceByNumber(String str, int i) throws RemoteException;

    void preCallsetCallState(int i) throws RemoteException;

    void preCallsetCallStateByNumber(String str, int i) throws RemoteException;

    void readGroupChatMessage(String str) throws RemoteException;

    int receiveFile(long j, long j2) throws RemoteException;

    int registerCallback(int i, IfMsgplusCb ifMsgplusCb) throws RemoteException;

    void rejectAudioMessage(long j, String str) throws RemoteException;

    int rejectFile(long j, long j2) throws RemoteException;

    void rejectGroupInvite(String str) throws RemoteException;

    void removeConversation(String str) throws RemoteException;

    int requestCapabilitiesInCsCall(String str) throws RemoteException;

    int requestContactCapabilities(String str) throws RemoteException;

    int resendGroupMessageLocation(long j, String str) throws RemoteException;

    int resendMessageFile(long j, long j2) throws RemoteException;

    int resendMessageIm(long j, String str) throws RemoteException;

    int saveNickname(String str) throws RemoteException;

    void sendAudioMessage(String str, int i, String str2) throws RemoteException;

    int sendComposingState(String str, int i) throws RemoteException;

    int sendFile(String str, String str2, Bundle bundle) throws RemoteException;

    int sendFileWithLocalId(String str, String str2, long j, Bundle bundle) throws RemoteException;

    int sendFtReadReport(long j, long j2) throws RemoteException;

    int sendGroupComposingState(String str, int i) throws RemoteException;

    void sendGroupMessage(String str, String str2) throws RemoteException;

    void sendGroupMessageWithLocalId(String str, String str2, long j) throws RemoteException;

    int sendImReadReport(String str) throws RemoteException;

    int sendImage(String str, String str2) throws RemoteException;

    int sendLocation(String str, double d, double d2, String str2, String str3) throws RemoteException;

    int sendMassLocation(List<String> list, double d, double d2, String str, String str2) throws RemoteException;

    int sendMessageIm(String str, String str2) throws RemoteException;

    int sendMessageImWithLocalId(String str, String str2, long j) throws RemoteException;

    void sendNote(String str, String str2) throws RemoteException;

    int sendVideo(String str, String str2) throws RemoteException;

    int sendVoice(String str, String str2, int i) throws RemoteException;

    int setAllowSendDisplayStatus(boolean z) throws RemoteException;

    void setAutoAcceptGroupInviter(boolean z) throws RemoteException;

    int setCsCallIdle() throws RemoteException;

    int setCsCallIdleWithNumber(String str) throws RemoteException;

    int setCsCallOffHook(String str) throws RemoteException;

    int setGroupMessageRequestDeliveryStatus(boolean z) throws RemoteException;

    void setIncallChatThreadId(long j) throws RemoteException;

    int setLocalVideoSurfaceHandle(Surface surface) throws RemoteException;

    int setRemoteVideoSurfaceHandle(Surface surface) throws RemoteException;

    int setRequestDeliveryStatus(boolean z) throws RemoteException;

    int setRequestDisplayStatus(boolean z) throws RemoteException;

    int setVideoShareCameraRotate(int i) throws RemoteException;

    void shareSketchAccept() throws RemoteException;

    void shareSketchCancel() throws RemoteException;

    void shareSketchCreate(boolean z) throws RemoteException;

    void shareSketchEnd() throws RemoteException;

    boolean shareSketchInit(String str, int i) throws RemoteException;

    void shareSketchReject() throws RemoteException;

    void shareSketchSaveDB(Bundle bundle) throws RemoteException;

    void shareSketchSendInfo(Bundle bundle) throws RemoteException;

    int switchVideoShareCamera() throws RemoteException;

    int termVideoShare(boolean z) throws RemoteException;

    void unRegisterCallback(int i, IfMsgplusCb ifMsgplusCb) throws RemoteException;

    void updateGroupTopic(String str, String str2) throws RemoteException;

    boolean warnIMmessageDeferredIfNeed(String str) throws RemoteException;
}
