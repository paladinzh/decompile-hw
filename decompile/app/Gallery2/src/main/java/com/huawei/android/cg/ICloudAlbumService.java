package com.huawei.android.cg;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.amap.api.services.core.AMapException;
import com.android.gallery3d.util.PasteWorker.PasteEventHandler;
import com.autonavi.amap.mapcore.ADGLMapAnimGroup;
import com.autonavi.amap.mapcore.MapMessage;
import com.huawei.android.cg.vo.AccountInfo;
import com.huawei.android.cg.vo.CategoryInfo;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.FileInfoDetail;
import com.huawei.android.cg.vo.FileInfoGroup;
import com.huawei.android.cg.vo.SettingsProp;
import com.huawei.android.cg.vo.ShareInfo;
import com.huawei.android.cg.vo.ShareReceiver;
import com.huawei.android.cg.vo.SwitchInfo;
import com.huawei.android.cg.vo.TagFileInfo;
import com.huawei.android.cg.vo.TagFileInfoGroup;
import com.huawei.android.cg.vo.TagInfo;
import com.huawei.watermark.ui.WMComponent;
import java.util.List;

public interface ICloudAlbumService extends IInterface {

    public static abstract class Stub extends Binder implements ICloudAlbumService {

        private static class Proxy implements ICloudAlbumService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public AccountInfo getLogOnInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    AccountInfo accountInfo;
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        accountInfo = (AccountInfo) AccountInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        accountInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return accountInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public SwitchInfo getSwitchInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    SwitchInfo switchInfo;
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        switchInfo = (SwitchInfo) SwitchInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        switchInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return switchInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerCallback(ICloudAlbumCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(3, _data, _reply, 0);
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

            public void unregisterCallback(ICloudAlbumCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setAlbumProperties(SettingsProp settingsProp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    if (settingsProp != null) {
                        _data.writeInt(1);
                        settingsProp.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteGeneralAlbum(String albumId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(albumId);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int modifyGeneralAlbum(String albumId, String albumName, String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(albumId);
                    _data.writeString(albumName);
                    _data.writeString(path);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int moveGeneralFile(String sourceAlbumId, String destAlbumId, FileInfo[] fileList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(sourceAlbumId);
                    _data.writeString(destAlbumId);
                    _data.writeTypedArray(fileList, 0);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uploadGeneralFile(String albumId, String absolutePath, String relativePath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(albumId);
                    _data.writeString(absolutePath);
                    _data.writeString(relativePath);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int modifyGeneralFile(FileInfo fileInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    if (fileInfo != null) {
                        _data.writeInt(1);
                        fileInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteGeneralFile() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void refreshGeneralAlbum() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void refreshSingleGeneralAlbum(String albumId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(albumId);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int downloadPhotoThumb(FileInfo[] fileInfo, int thumbType, int operationType, boolean forceDownload) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeTypedArray(fileInfo, 0);
                    _data.writeInt(thumbType);
                    _data.writeInt(operationType);
                    if (forceDownload) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cancelDownloadPhotoThumb(FileInfo[] fileInfo, int thumbType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeTypedArray(fileInfo, 0);
                    _data.writeInt(thumbType);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int isGeneralFileDownloading(FileInfo[] fileInfos, int thumbType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeTypedArray(fileInfos, 0);
                    _data.writeInt(thumbType);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ShareReceiver> getAccountList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    List<ShareReceiver> _result = _reply.createTypedArrayList(ShareReceiver.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ShareReceiver> isHWAccountList(List<ShareReceiver> accountList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeTypedList(accountList);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    List<ShareReceiver> _result = _reply.createTypedArrayList(ShareReceiver.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ShareInfo> getShareList(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeInt(type);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    List<ShareInfo> _result = _reply.createTypedArrayList(ShareInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ShareInfo getShare(String shareId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ShareInfo shareInfo;
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        shareInfo = (ShareInfo) ShareInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        shareInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return shareInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int createShare(ShareInfo shareInfo, String[] listFilePaths) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    if (shareInfo != null) {
                        _data.writeInt(1);
                        shareInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringArray(listFilePaths);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int createShareFromAlbum(ShareInfo shareInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    if (shareInfo != null) {
                        _data.writeInt(1);
                        shareInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int modifyShareAlbum(String shareId, String shareName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    _data.writeString(shareName);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int modifyShareFile(FileInfo fileInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    if (fileInfo != null) {
                        _data.writeInt(1);
                        fileInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int modifyShareRecAdd(ShareInfo shareInfo, List<ShareReceiver> addAccountList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    if (shareInfo != null) {
                        _data.writeInt(1);
                        shareInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedList(addAccountList);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int modifyShareRecDel(ShareInfo shareInfo, List<ShareReceiver> delAccountList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    if (shareInfo != null) {
                        _data.writeInt(1);
                        shareInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedList(delAccountList);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<FileInfo> deleteShareFile(String shareId, List<FileInfo> fileList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    _data.writeTypedList(fileList);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    List<FileInfo> _result = _reply.createTypedArrayList(FileInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addFileToShare(String shareId, String[] filePaths) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    _data.writeStringArray(filePaths);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cancelShare(String shareId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteShareAlbum(String shareId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int downloadSharePhotoThumb(FileInfo[] fileInfo, int thumbType, int operationType, boolean forceDownload) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeTypedArray(fileInfo, 0);
                    _data.writeInt(thumbType);
                    _data.writeInt(operationType);
                    if (forceDownload) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cancelShareDownloadTask(FileInfo[] fileList, int thumbType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeTypedArray(fileList, 0);
                    _data.writeInt(thumbType);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int pauseShareDownloadTask(FileInfo[] fileList, int thumbType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeTypedArray(fileList, 0);
                    _data.writeInt(thumbType);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int isShareFileDownloading(FileInfo fileInfo, int thumbType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    if (fileInfo != null) {
                        _data.writeInt(1);
                        fileInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(thumbType);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<FileInfo> getShareFileInfoListLimit(String shareId, int fileType, int start, int queryNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    _data.writeInt(fileType);
                    _data.writeInt(start);
                    _data.writeInt(queryNum);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                    List<FileInfo> _result = _reply.createTypedArrayList(FileInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getShareFileInfoListLimitCount(String shareId, int fileType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    _data.writeInt(fileType);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getShareFileInfoTotalSize(String shareId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public FileInfo getShareFileInfo(String shareId, String hash) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    FileInfo fileInfo;
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    _data.writeString(hash);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        fileInfo = (FileInfo) FileInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        fileInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return fileInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<FileInfo> getSharePreFileInfoList(String shareId, int FileType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    _data.writeInt(FileType);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    List<FileInfo> _result = _reply.createTypedArrayList(FileInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public FileInfo getSharePreFileInfo(String shareId, String hash) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    FileInfo fileInfo;
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    _data.writeString(hash);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        fileInfo = (FileInfo) FileInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        fileInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return fileInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<FileInfo> getSharePreFileInfoListLimit(String shareId, int fileType, int start, int queryNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    _data.writeInt(fileType);
                    _data.writeInt(start);
                    _data.writeInt(queryNum);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                    List<FileInfo> _result = _reply.createTypedArrayList(FileInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getSharePreFileInfoListLimitCount(String shareId, int fileType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    _data.writeInt(fileType);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void refreshShare() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void refreshSingleShare(String shareId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int shareResultConfirm(String shareId, int result, String ownerId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    _data.writeInt(result);
                    _data.writeString(ownerId);
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cancelReceiveShare(String shareId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isExistLocalData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(47, _data, _reply, 0);
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

            public String createShortLink(String albumId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(albumId);
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAlbumHeadPic(String shareId, String[] userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    _data.writeStringArray(userId);
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ShareReceiver> getAlbumLocalHeadPic(String shareId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    this.mRemote.transact(50, _data, _reply, 0);
                    _reply.readException();
                    List<ShareReceiver> _result = _reply.createTypedArrayList(ShareReceiver.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAIDLVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cancelUploadTaskAll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(52, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cancelUploadTask(FileInfoDetail[] fileInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeTypedArray(fileInfo, 0);
                    this.mRemote.transact(53, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startUploadTaskAll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(54, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startUploadTask(FileInfoDetail[] fileInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeTypedArray(fileInfo, 0);
                    this.mRemote.transact(55, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteUploadHistoryAll(int fileStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeInt(fileStatus);
                    this.mRemote.transact(56, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteUploadHistory(FileInfoDetail[] fileInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeTypedArray(fileInfo, 0);
                    this.mRemote.transact(57, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<FileInfoDetail> getUploadFileInfoListLimit(int fileStatus, int start, int queryNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeInt(fileStatus);
                    _data.writeInt(start);
                    _data.writeInt(queryNum);
                    this.mRemote.transact(58, _data, _reply, 0);
                    _reply.readException();
                    List<FileInfoDetail> _result = _reply.createTypedArrayList(FileInfoDetail.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getUploadFileInfoListCount(int fileStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeInt(fileStatus);
                    this.mRemote.transact(59, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cancelDownloadTaskAll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(60, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int cancelDownloadTask(FileInfoDetail[] fileInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeTypedArray(fileInfo, 0);
                    this.mRemote.transact(61, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startDownloadTaskAll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(62, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startDownloadTask(FileInfoDetail[] fileInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeTypedArray(fileInfo, 0);
                    this.mRemote.transact(63, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteDownloadHistoryAll(int fileStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeInt(fileStatus);
                    this.mRemote.transact(64, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deleteDownloadHistory(FileInfoDetail[] fileInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeTypedArray(fileInfo, 0);
                    this.mRemote.transact(65, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<FileInfoDetail> getDownloadFileInfoListLimit(int fileStatus, int start, int queryNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeInt(fileStatus);
                    _data.writeInt(start);
                    _data.writeInt(queryNum);
                    this.mRemote.transact(66, _data, _reply, 0);
                    _reply.readException();
                    List<FileInfoDetail> _result = _reply.createTypedArrayList(FileInfoDetail.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDownloadFileInfoListCount(int fileStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeInt(fileStatus);
                    this.mRemote.transact(67, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public FileInfoDetail getUploadManualFileInfo(String albumId, String hash) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    FileInfoDetail fileInfoDetail;
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(albumId);
                    _data.writeString(hash);
                    this.mRemote.transact(68, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        fileInfoDetail = (FileInfoDetail) FileInfoDetail.CREATOR.createFromParcel(_reply);
                    } else {
                        fileInfoDetail = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return fileInfoDetail;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public FileInfoDetail getDownloadManualFileInfo(String albumId, String hash) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    FileInfoDetail fileInfoDetail;
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(albumId);
                    _data.writeString(hash);
                    this.mRemote.transact(69, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        fileInfoDetail = (FileInfoDetail) FileInfoDetail.CREATOR.createFromParcel(_reply);
                    } else {
                        fileInfoDetail = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return fileInfoDetail;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void refreshTag() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(70, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void refreshSingleTag(String categoryId, String tagId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeString(tagId);
                    this.mRemote.transact(71, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<CategoryInfo> getCategoryInfoList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(72, _data, _reply, 0);
                    _reply.readException();
                    List<CategoryInfo> _result = _reply.createTypedArrayList(CategoryInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TagInfo> getTagInfoListLimit(String categoryId, int start, int queryNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeInt(start);
                    _data.writeInt(queryNum);
                    this.mRemote.transact(73, _data, _reply, 0);
                    _reply.readException();
                    List<TagInfo> _result = _reply.createTypedArrayList(TagInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getTagInfoListCount(String categoryId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    this.mRemote.transact(74, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TagFileInfo> getTagFileInfoListLimit(String categoryId, String tagId, int start, int queryNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeString(tagId);
                    _data.writeInt(start);
                    _data.writeInt(queryNum);
                    this.mRemote.transact(75, _data, _reply, 0);
                    _reply.readException();
                    List<TagFileInfo> _result = _reply.createTypedArrayList(TagFileInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getTagFileInfoListCount(String categoryId, String tagId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeString(tagId);
                    this.mRemote.transact(76, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TagFileInfo> getCertificateListLimit(int start, int queryNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeInt(start);
                    _data.writeInt(queryNum);
                    this.mRemote.transact(77, _data, _reply, 0);
                    _reply.readException();
                    List<TagFileInfo> _result = _reply.createTypedArrayList(TagFileInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCertificateCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(78, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> deleteTagFileInfoList(String categoryId, String tagId, TagFileInfo[] tagFileList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeString(tagId);
                    _data.writeTypedArray(tagFileList, 0);
                    this.mRemote.transact(79, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TagFileInfo> modifyTagFileInfoList(String categoryId, String tagId, String tagName, TagFileInfo[] tagFileList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeString(tagId);
                    _data.writeString(tagName);
                    _data.writeTypedArray(tagFileList, 0);
                    this.mRemote.transact(80, _data, _reply, 0);
                    _reply.readException();
                    List<TagFileInfo> _result = _reply.createTypedArrayList(TagFileInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TagFileInfo> moveTagFileInfoList(String categoryId, String tagId, TagFileInfo[] tagFileList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeString(tagId);
                    _data.writeTypedArray(tagFileList, 0);
                    this.mRemote.transact(81, _data, _reply, 0);
                    _reply.readException();
                    List<TagFileInfo> _result = _reply.createTypedArrayList(TagFileInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TagInfo> deleteTagInfoList(String categoryId, TagInfo[] tagList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeTypedArray(tagList, 0);
                    this.mRemote.transact(82, _data, _reply, 0);
                    _reply.readException();
                    List<TagInfo> _result = _reply.createTypedArrayList(TagInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TagInfo> modifyTagInfoList(String categoryId, TagInfo[] tagList, String tagName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeTypedArray(tagList, 0);
                    _data.writeString(tagName);
                    this.mRemote.transact(83, _data, _reply, 0);
                    _reply.readException();
                    List<TagInfo> _result = _reply.createTypedArrayList(TagInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getTagAuth() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(84, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getLocalTagAuth() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
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

            public CategoryInfo getCategoryInfo(String categoryId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    CategoryInfo categoryInfo;
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    this.mRemote.transact(86, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        categoryInfo = (CategoryInfo) CategoryInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        categoryInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return categoryInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public TagInfo getTagInfo(String categoryId, String tagId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    TagInfo tagInfo;
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeString(tagId);
                    this.mRemote.transact(87, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        tagInfo = (TagInfo) TagInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        tagInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return tagInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public TagFileInfo getTagFileInfo(TagFileInfo tagFileInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    TagFileInfo tagFileInfo2;
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    if (tagFileInfo != null) {
                        _data.writeInt(1);
                        tagFileInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(88, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        tagFileInfo2 = (TagFileInfo) TagFileInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        tagFileInfo2 = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return tagFileInfo2;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TagFileInfo> moveToTagFileInfoList(String categoryId, String tagId, TagFileInfo[] tagFileList, String tagTargetId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeString(tagId);
                    _data.writeTypedArray(tagFileList, 0);
                    _data.writeString(tagTargetId);
                    this.mRemote.transact(89, _data, _reply, 0);
                    _reply.readException();
                    List<TagFileInfo> _result = _reply.createTypedArrayList(TagFileInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TagFileInfo> deleteTagItemInfoList(String categoryId, String tagId, TagFileInfo[] tagFileList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeString(tagId);
                    _data.writeTypedArray(tagFileList, 0);
                    this.mRemote.transact(90, _data, _reply, 0);
                    _reply.readException();
                    List<TagFileInfo> _result = _reply.createTypedArrayList(TagFileInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int downloadTagInfoCoverPhoto(String categoryId, String tagId, TagFileInfo[] tagFileList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeString(tagId);
                    _data.writeTypedArray(tagFileList, 0);
                    this.mRemote.transact(91, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<FileInfo> getShareFileInfoListByHash(String[] hash) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeStringArray(hash);
                    this.mRemote.transact(92, _data, _reply, 0);
                    _reply.readException();
                    List<FileInfo> _result = _reply.createTypedArrayList(FileInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<ShareInfo> getShareGroupList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(93, _data, _reply, 0);
                    _reply.readException();
                    List<ShareInfo> _result = _reply.createTypedArrayList(ShareInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<FileInfoGroup> getFileInfoGroupListLimit(String shareId, int start, int queryNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    _data.writeInt(start);
                    _data.writeInt(queryNum);
                    this.mRemote.transact(94, _data, _reply, 0);
                    _reply.readException();
                    List<FileInfoGroup> _result = _reply.createTypedArrayList(FileInfoGroup.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getFileInfoGroupListCount(String shareId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    this.mRemote.transact(95, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<FileInfo> getFileInfoListByGroupLimit(String shareId, int start, int queryNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    _data.writeInt(start);
                    _data.writeInt(queryNum);
                    this.mRemote.transact(96, _data, _reply, 0);
                    _reply.readException();
                    List<FileInfo> _result = _reply.createTypedArrayList(FileInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<FileInfo> getFileInfoListByGroupBatchLimit(String shareId, int batch, int start, int queryNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    _data.writeInt(batch);
                    _data.writeInt(start);
                    _data.writeInt(queryNum);
                    this.mRemote.transact(97, _data, _reply, 0);
                    _reply.readException();
                    List<FileInfo> _result = _reply.createTypedArrayList(FileInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int updateShareInfoPrivilege(String shareId, int privilege) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(shareId);
                    _data.writeInt(privilege);
                    this.mRemote.transact(98, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSupportSns() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(99, _data, _reply, 0);
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

            public String[] getSnsGroupList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(100, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getFVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(101, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearFVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(102, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int isGeneralFileUploading(FileInfo[] fileInfos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeTypedArray(fileInfos, 0);
                    this.mRemote.transact(OfflineMapStatus.EXCEPTION_SDCARD, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TagFileInfoGroup> getTagFileInfoGroupLimit(String categoryId, String tagId, int start, int queryNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeString(tagId);
                    _data.writeInt(start);
                    _data.writeInt(queryNum);
                    this.mRemote.transact(104, _data, _reply, 0);
                    _reply.readException();
                    List<TagFileInfoGroup> _result = _reply.createTypedArrayList(TagFileInfoGroup.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TagFileInfo> getTagFileInfoGroupBatchLimit(String categoryId, String tagId, long batchCTime, int start, int queryNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeString(tagId);
                    _data.writeLong(batchCTime);
                    _data.writeInt(start);
                    _data.writeInt(queryNum);
                    this.mRemote.transact(105, _data, _reply, 0);
                    _reply.readException();
                    List<TagFileInfo> _result = _reply.createTypedArrayList(TagFileInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TagFileInfoGroup> getCertificateListBatchLimit(int start, int queryNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeInt(start);
                    _data.writeInt(queryNum);
                    this.mRemote.transact(106, _data, _reply, 0);
                    _reply.readException();
                    List<TagFileInfoGroup> _result = _reply.createTypedArrayList(TagFileInfoGroup.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<TagFileInfo> getCertificateListGroupBatchLimit(long batchId, int start, int queryNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeLong(batchId);
                    _data.writeInt(start);
                    _data.writeInt(queryNum);
                    this.mRemote.transact(107, _data, _reply, 0);
                    _reply.readException();
                    List<TagFileInfo> _result = _reply.createTypedArrayList(TagFileInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCertificateBatchCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(108, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getTagFileInfoListBatchCount(String categoryId, String tagId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeString(tagId);
                    this.mRemote.transact(109, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public TagFileInfo getMaxConfidenceTagFileInfo(String categoryId, String tagId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    TagFileInfo tagFileInfo;
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(categoryId);
                    _data.writeString(tagId);
                    this.mRemote.transact(110, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        tagFileInfo = (TagFileInfo) TagFileInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        tagFileInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return tagFileInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getPhotoThumbSize(int thumbType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeInt(thumbType);
                    this.mRemote.transact(111, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deletePhotoThumb(FileInfo[] fileInfo, int thumbType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeTypedArray(fileInfo, 0);
                    _data.writeInt(thumbType);
                    this.mRemote.transact(112, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uploadGeneralFileList(List<FileInfo> fileInfoList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeTypedList(fileInfoList);
                    this.mRemote.transact(113, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startAsyncAlbumInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(114, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getServerTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    this.mRemote.transact(115, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public FileInfoDetail getDownloadFileInfoByUniqueId(String uniqueId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    FileInfoDetail fileInfoDetail;
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumService");
                    _data.writeString(uniqueId);
                    this.mRemote.transact(116, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        fileInfoDetail = (FileInfoDetail) FileInfoDetail.CREATOR.createFromParcel(_reply);
                    } else {
                        fileInfoDetail = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return fileInfoDetail;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.android.cg.ICloudAlbumService");
        }

        public static ICloudAlbumService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.android.cg.ICloudAlbumService");
            if (iin == null || !(iin instanceof ICloudAlbumService)) {
                return new Proxy(obj);
            }
            return (ICloudAlbumService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            int _result2;
            FileInfo fileInfo;
            List<ShareReceiver> _result3;
            List<ShareInfo> _result4;
            ShareInfo shareInfo;
            List<FileInfo> _result5;
            long _result6;
            FileInfo _result7;
            String _result8;
            List<FileInfoDetail> _result9;
            FileInfoDetail _result10;
            List<TagInfo> _result11;
            List<TagFileInfo> _result12;
            TagFileInfo _result13;
            List<TagFileInfoGroup> _result14;
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    AccountInfo _result15 = getLogOnInfo();
                    reply.writeNoException();
                    if (_result15 != null) {
                        reply.writeInt(1);
                        _result15.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 2:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    SwitchInfo _result16 = getSwitchInfo();
                    reply.writeNoException();
                    if (_result16 != null) {
                        reply.writeInt(1);
                        _result16.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 3:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result = registerCallback(com.huawei.android.cg.ICloudAlbumCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 4:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    unregisterCallback(com.huawei.android.cg.ICloudAlbumCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 5:
                    SettingsProp settingsProp;
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    if (data.readInt() != 0) {
                        settingsProp = (SettingsProp) SettingsProp.CREATOR.createFromParcel(data);
                    } else {
                        settingsProp = null;
                    }
                    _result2 = setAlbumProperties(settingsProp);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 6:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = deleteGeneralAlbum(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 7:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = modifyGeneralAlbum(data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 8:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = moveGeneralFile(data.readString(), data.readString(), (FileInfo[]) data.createTypedArray(FileInfo.CREATOR));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 9:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = uploadGeneralFile(data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 10:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    if (data.readInt() != 0) {
                        fileInfo = (FileInfo) FileInfo.CREATOR.createFromParcel(data);
                    } else {
                        fileInfo = null;
                    }
                    _result2 = modifyGeneralFile(fileInfo);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 11:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    deleteGeneralFile();
                    reply.writeNoException();
                    return true;
                case 12:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    refreshGeneralAlbum();
                    reply.writeNoException();
                    return true;
                case 13:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    refreshSingleGeneralAlbum(data.readString());
                    reply.writeNoException();
                    return true;
                case 14:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = downloadPhotoThumb((FileInfo[]) data.createTypedArray(FileInfo.CREATOR), data.readInt(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 15:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = cancelDownloadPhotoThumb((FileInfo[]) data.createTypedArray(FileInfo.CREATOR), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 16:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = isGeneralFileDownloading((FileInfo[]) data.createTypedArray(FileInfo.CREATOR), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 17:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result3 = getAccountList();
                    reply.writeNoException();
                    reply.writeTypedList(_result3);
                    return true;
                case 18:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result3 = isHWAccountList(data.createTypedArrayList(ShareReceiver.CREATOR));
                    reply.writeNoException();
                    reply.writeTypedList(_result3);
                    return true;
                case 19:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result4 = getShareList(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result4);
                    return true;
                case 20:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    ShareInfo _result17 = getShare(data.readString());
                    reply.writeNoException();
                    if (_result17 != null) {
                        reply.writeInt(1);
                        _result17.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 21:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    if (data.readInt() != 0) {
                        shareInfo = (ShareInfo) ShareInfo.CREATOR.createFromParcel(data);
                    } else {
                        shareInfo = null;
                    }
                    _result2 = createShare(shareInfo, data.createStringArray());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 22:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    if (data.readInt() != 0) {
                        shareInfo = (ShareInfo) ShareInfo.CREATOR.createFromParcel(data);
                    } else {
                        shareInfo = null;
                    }
                    _result2 = createShareFromAlbum(shareInfo);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 23:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = modifyShareAlbum(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 24:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    if (data.readInt() != 0) {
                        fileInfo = (FileInfo) FileInfo.CREATOR.createFromParcel(data);
                    } else {
                        fileInfo = null;
                    }
                    _result2 = modifyShareFile(fileInfo);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 25:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    if (data.readInt() != 0) {
                        shareInfo = (ShareInfo) ShareInfo.CREATOR.createFromParcel(data);
                    } else {
                        shareInfo = null;
                    }
                    _result2 = modifyShareRecAdd(shareInfo, data.createTypedArrayList(ShareReceiver.CREATOR));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case AMapException.ERROR_CODE_URL /*26*/:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    if (data.readInt() != 0) {
                        shareInfo = (ShareInfo) ShareInfo.CREATOR.createFromParcel(data);
                    } else {
                        shareInfo = null;
                    }
                    _result2 = modifyShareRecDel(shareInfo, data.createTypedArrayList(ShareReceiver.CREATOR));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case AMapException.ERROR_CODE_UNKNOW_HOST /*27*/:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result5 = deleteShareFile(data.readString(), data.createTypedArrayList(FileInfo.CREATOR));
                    reply.writeNoException();
                    reply.writeTypedList(_result5);
                    return true;
                case AMapException.ERROR_CODE_UNKNOW_SERVICE /*28*/:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = addFileToShare(data.readString(), data.createStringArray());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case AMapException.ERROR_CODE_PROTOCOL /*29*/:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = cancelShare(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 30:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = deleteShareAlbum(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 31:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = downloadSharePhotoThumb((FileInfo[]) data.createTypedArray(FileInfo.CREATOR), data.readInt(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 32:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = cancelShareDownloadTask((FileInfo[]) data.createTypedArray(FileInfo.CREATOR), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 33:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = pauseShareDownloadTask((FileInfo[]) data.createTypedArray(FileInfo.CREATOR), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case AMapException.ERROR_CODE_SERVER /*34*/:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    if (data.readInt() != 0) {
                        fileInfo = (FileInfo) FileInfo.CREATOR.createFromParcel(data);
                    } else {
                        fileInfo = null;
                    }
                    _result2 = isShareFileDownloading(fileInfo, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case AMapException.ERROR_CODE_QUOTA /*35*/:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result5 = getShareFileInfoListLimit(data.readString(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result5);
                    return true;
                case AMapException.ERROR_CODE_REQUEST /*36*/:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = getShareFileInfoListLimitCount(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 37:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result6 = getShareFileInfoTotalSize(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result6);
                    return true;
                case 38:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result7 = getShareFileInfo(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result7 != null) {
                        reply.writeInt(1);
                        _result7.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 39:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result5 = getSharePreFileInfoList(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result5);
                    return true;
                case 40:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result7 = getSharePreFileInfo(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result7 != null) {
                        reply.writeInt(1);
                        _result7.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 41:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result5 = getSharePreFileInfoListLimit(data.readString(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result5);
                    return true;
                case 42:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = getSharePreFileInfoListLimitCount(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 43:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    refreshShare();
                    reply.writeNoException();
                    return true;
                case 44:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    refreshSingleShare(data.readString());
                    reply.writeNoException();
                    return true;
                case 45:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = shareResultConfirm(data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 46:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = cancelReceiveShare(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 47:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result = isExistLocalData();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 48:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result8 = createShortLink(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result8);
                    return true;
                case 49:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = getAlbumHeadPic(data.readString(), data.createStringArray());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 50:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result3 = getAlbumLocalHeadPic(data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result3);
                    return true;
                case 51:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = getAIDLVersion();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 52:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = cancelUploadTaskAll();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 53:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = cancelUploadTask((FileInfoDetail[]) data.createTypedArray(FileInfoDetail.CREATOR));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 54:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = startUploadTaskAll();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 55:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = startUploadTask((FileInfoDetail[]) data.createTypedArray(FileInfoDetail.CREATOR));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 56:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = deleteUploadHistoryAll(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 57:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = deleteUploadHistory((FileInfoDetail[]) data.createTypedArray(FileInfoDetail.CREATOR));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 58:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result9 = getUploadFileInfoListLimit(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result9);
                    return true;
                case 59:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = getUploadFileInfoListCount(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case ADGLMapAnimGroup.CAMERA_MAX_DEGREE /*60*/:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = cancelDownloadTaskAll();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 61:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = cancelDownloadTask((FileInfoDetail[]) data.createTypedArray(FileInfoDetail.CREATOR));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 62:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = startDownloadTaskAll();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 63:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = startDownloadTask((FileInfoDetail[]) data.createTypedArray(FileInfoDetail.CREATOR));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 64:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = deleteDownloadHistoryAll(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case MapMessage.MAX_CAMERA_HEADER_DEGREE /*65*/:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = deleteDownloadHistory((FileInfoDetail[]) data.createTypedArray(FileInfoDetail.CREATOR));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 66:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result9 = getDownloadFileInfoListLimit(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result9);
                    return true;
                case 67:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = getDownloadFileInfoListCount(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 68:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result10 = getUploadManualFileInfo(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result10 != null) {
                        reply.writeInt(1);
                        _result10.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 69:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result10 = getDownloadManualFileInfo(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result10 != null) {
                        reply.writeInt(1);
                        _result10.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 70:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    refreshTag();
                    reply.writeNoException();
                    return true;
                case 71:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    refreshSingleTag(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 72:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    List<CategoryInfo> _result18 = getCategoryInfoList();
                    reply.writeNoException();
                    reply.writeTypedList(_result18);
                    return true;
                case 73:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result11 = getTagInfoListLimit(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result11);
                    return true;
                case 74:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = getTagInfoListCount(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 75:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result12 = getTagFileInfoListLimit(data.readString(), data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result12);
                    return true;
                case 76:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = getTagFileInfoListCount(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 77:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result12 = getCertificateListLimit(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result12);
                    return true;
                case 78:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = getCertificateCount();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 79:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    List<String> _result19 = deleteTagFileInfoList(data.readString(), data.readString(), (TagFileInfo[]) data.createTypedArray(TagFileInfo.CREATOR));
                    reply.writeNoException();
                    reply.writeStringList(_result19);
                    return true;
                case 80:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result12 = modifyTagFileInfoList(data.readString(), data.readString(), data.readString(), (TagFileInfo[]) data.createTypedArray(TagFileInfo.CREATOR));
                    reply.writeNoException();
                    reply.writeTypedList(_result12);
                    return true;
                case 81:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result12 = moveTagFileInfoList(data.readString(), data.readString(), (TagFileInfo[]) data.createTypedArray(TagFileInfo.CREATOR));
                    reply.writeNoException();
                    reply.writeTypedList(_result12);
                    return true;
                case 82:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result11 = deleteTagInfoList(data.readString(), (TagInfo[]) data.createTypedArray(TagInfo.CREATOR));
                    reply.writeNoException();
                    reply.writeTypedList(_result11);
                    return true;
                case 83:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result11 = modifyTagInfoList(data.readString(), (TagInfo[]) data.createTypedArray(TagInfo.CREATOR), data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result11);
                    return true;
                case 84:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = getTagAuth();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 85:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result = getLocalTagAuth();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 86:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    CategoryInfo _result20 = getCategoryInfo(data.readString());
                    reply.writeNoException();
                    if (_result20 != null) {
                        reply.writeInt(1);
                        _result20.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 87:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    TagInfo _result21 = getTagInfo(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result21 != null) {
                        reply.writeInt(1);
                        _result21.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 88:
                    TagFileInfo tagFileInfo;
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    if (data.readInt() != 0) {
                        tagFileInfo = (TagFileInfo) TagFileInfo.CREATOR.createFromParcel(data);
                    } else {
                        tagFileInfo = null;
                    }
                    _result13 = getTagFileInfo(tagFileInfo);
                    reply.writeNoException();
                    if (_result13 != null) {
                        reply.writeInt(1);
                        _result13.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 89:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result12 = moveToTagFileInfoList(data.readString(), data.readString(), (TagFileInfo[]) data.createTypedArray(TagFileInfo.CREATOR), data.readString());
                    reply.writeNoException();
                    reply.writeTypedList(_result12);
                    return true;
                case WMComponent.ORI_90 /*90*/:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result12 = deleteTagItemInfoList(data.readString(), data.readString(), (TagFileInfo[]) data.createTypedArray(TagFileInfo.CREATOR));
                    reply.writeNoException();
                    reply.writeTypedList(_result12);
                    return true;
                case 91:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = downloadTagInfoCoverPhoto(data.readString(), data.readString(), (TagFileInfo[]) data.createTypedArray(TagFileInfo.CREATOR));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 92:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result5 = getShareFileInfoListByHash(data.createStringArray());
                    reply.writeNoException();
                    reply.writeTypedList(_result5);
                    return true;
                case 93:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result4 = getShareGroupList();
                    reply.writeNoException();
                    reply.writeTypedList(_result4);
                    return true;
                case 94:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    List<FileInfoGroup> _result22 = getFileInfoGroupListLimit(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result22);
                    return true;
                case 95:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = getFileInfoGroupListCount(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 96:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result5 = getFileInfoListByGroupLimit(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result5);
                    return true;
                case 97:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result5 = getFileInfoListByGroupBatchLimit(data.readString(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result5);
                    return true;
                case 98:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = updateShareInfoPrivilege(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case PasteEventHandler.PASTE_EVENT_UNKNOWN /*99*/:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result = isSupportSns();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 100:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    String[] _result23 = getSnsGroupList();
                    reply.writeNoException();
                    reply.writeStringArray(_result23);
                    return true;
                case 101:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result8 = getFVersion();
                    reply.writeNoException();
                    reply.writeString(_result8);
                    return true;
                case 102:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    clearFVersion();
                    reply.writeNoException();
                    return true;
                case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = isGeneralFileUploading((FileInfo[]) data.createTypedArray(FileInfo.CREATOR));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 104:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result14 = getTagFileInfoGroupLimit(data.readString(), data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result14);
                    return true;
                case 105:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result12 = getTagFileInfoGroupBatchLimit(data.readString(), data.readString(), data.readLong(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result12);
                    return true;
                case 106:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result14 = getCertificateListBatchLimit(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result14);
                    return true;
                case 107:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result12 = getCertificateListGroupBatchLimit(data.readLong(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result12);
                    return true;
                case 108:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = getCertificateBatchCount();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 109:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = getTagFileInfoListBatchCount(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 110:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result13 = getMaxConfidenceTagFileInfo(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result13 != null) {
                        reply.writeInt(1);
                        _result13.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 111:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result6 = getPhotoThumbSize(data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result6);
                    return true;
                case 112:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = deletePhotoThumb((FileInfo[]) data.createTypedArray(FileInfo.CREATOR), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 113:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = uploadGeneralFileList(data.createTypedArrayList(FileInfo.CREATOR));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 114:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result2 = startAsyncAlbumInfo();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 115:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result6 = getServerTime();
                    reply.writeNoException();
                    reply.writeLong(_result6);
                    return true;
                case 116:
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumService");
                    _result10 = getDownloadFileInfoByUniqueId(data.readString());
                    reply.writeNoException();
                    if (_result10 != null) {
                        reply.writeInt(1);
                        _result10.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.android.cg.ICloudAlbumService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int addFileToShare(String str, String[] strArr) throws RemoteException;

    int cancelDownloadPhotoThumb(FileInfo[] fileInfoArr, int i) throws RemoteException;

    int cancelDownloadTask(FileInfoDetail[] fileInfoDetailArr) throws RemoteException;

    int cancelDownloadTaskAll() throws RemoteException;

    int cancelReceiveShare(String str) throws RemoteException;

    int cancelShare(String str) throws RemoteException;

    int cancelShareDownloadTask(FileInfo[] fileInfoArr, int i) throws RemoteException;

    int cancelUploadTask(FileInfoDetail[] fileInfoDetailArr) throws RemoteException;

    int cancelUploadTaskAll() throws RemoteException;

    void clearFVersion() throws RemoteException;

    int createShare(ShareInfo shareInfo, String[] strArr) throws RemoteException;

    int createShareFromAlbum(ShareInfo shareInfo) throws RemoteException;

    String createShortLink(String str) throws RemoteException;

    int deleteDownloadHistory(FileInfoDetail[] fileInfoDetailArr) throws RemoteException;

    int deleteDownloadHistoryAll(int i) throws RemoteException;

    int deleteGeneralAlbum(String str) throws RemoteException;

    void deleteGeneralFile() throws RemoteException;

    int deletePhotoThumb(FileInfo[] fileInfoArr, int i) throws RemoteException;

    int deleteShareAlbum(String str) throws RemoteException;

    List<FileInfo> deleteShareFile(String str, List<FileInfo> list) throws RemoteException;

    List<String> deleteTagFileInfoList(String str, String str2, TagFileInfo[] tagFileInfoArr) throws RemoteException;

    List<TagInfo> deleteTagInfoList(String str, TagInfo[] tagInfoArr) throws RemoteException;

    List<TagFileInfo> deleteTagItemInfoList(String str, String str2, TagFileInfo[] tagFileInfoArr) throws RemoteException;

    int deleteUploadHistory(FileInfoDetail[] fileInfoDetailArr) throws RemoteException;

    int deleteUploadHistoryAll(int i) throws RemoteException;

    int downloadPhotoThumb(FileInfo[] fileInfoArr, int i, int i2, boolean z) throws RemoteException;

    int downloadSharePhotoThumb(FileInfo[] fileInfoArr, int i, int i2, boolean z) throws RemoteException;

    int downloadTagInfoCoverPhoto(String str, String str2, TagFileInfo[] tagFileInfoArr) throws RemoteException;

    int getAIDLVersion() throws RemoteException;

    List<ShareReceiver> getAccountList() throws RemoteException;

    int getAlbumHeadPic(String str, String[] strArr) throws RemoteException;

    List<ShareReceiver> getAlbumLocalHeadPic(String str) throws RemoteException;

    CategoryInfo getCategoryInfo(String str) throws RemoteException;

    List<CategoryInfo> getCategoryInfoList() throws RemoteException;

    int getCertificateBatchCount() throws RemoteException;

    int getCertificateCount() throws RemoteException;

    List<TagFileInfoGroup> getCertificateListBatchLimit(int i, int i2) throws RemoteException;

    List<TagFileInfo> getCertificateListGroupBatchLimit(long j, int i, int i2) throws RemoteException;

    List<TagFileInfo> getCertificateListLimit(int i, int i2) throws RemoteException;

    FileInfoDetail getDownloadFileInfoByUniqueId(String str) throws RemoteException;

    int getDownloadFileInfoListCount(int i) throws RemoteException;

    List<FileInfoDetail> getDownloadFileInfoListLimit(int i, int i2, int i3) throws RemoteException;

    FileInfoDetail getDownloadManualFileInfo(String str, String str2) throws RemoteException;

    String getFVersion() throws RemoteException;

    int getFileInfoGroupListCount(String str) throws RemoteException;

    List<FileInfoGroup> getFileInfoGroupListLimit(String str, int i, int i2) throws RemoteException;

    List<FileInfo> getFileInfoListByGroupBatchLimit(String str, int i, int i2, int i3) throws RemoteException;

    List<FileInfo> getFileInfoListByGroupLimit(String str, int i, int i2) throws RemoteException;

    boolean getLocalTagAuth() throws RemoteException;

    AccountInfo getLogOnInfo() throws RemoteException;

    TagFileInfo getMaxConfidenceTagFileInfo(String str, String str2) throws RemoteException;

    long getPhotoThumbSize(int i) throws RemoteException;

    long getServerTime() throws RemoteException;

    ShareInfo getShare(String str) throws RemoteException;

    FileInfo getShareFileInfo(String str, String str2) throws RemoteException;

    List<FileInfo> getShareFileInfoListByHash(String[] strArr) throws RemoteException;

    List<FileInfo> getShareFileInfoListLimit(String str, int i, int i2, int i3) throws RemoteException;

    int getShareFileInfoListLimitCount(String str, int i) throws RemoteException;

    long getShareFileInfoTotalSize(String str) throws RemoteException;

    List<ShareInfo> getShareGroupList() throws RemoteException;

    List<ShareInfo> getShareList(int i) throws RemoteException;

    FileInfo getSharePreFileInfo(String str, String str2) throws RemoteException;

    List<FileInfo> getSharePreFileInfoList(String str, int i) throws RemoteException;

    List<FileInfo> getSharePreFileInfoListLimit(String str, int i, int i2, int i3) throws RemoteException;

    int getSharePreFileInfoListLimitCount(String str, int i) throws RemoteException;

    String[] getSnsGroupList() throws RemoteException;

    SwitchInfo getSwitchInfo() throws RemoteException;

    int getTagAuth() throws RemoteException;

    TagFileInfo getTagFileInfo(TagFileInfo tagFileInfo) throws RemoteException;

    List<TagFileInfo> getTagFileInfoGroupBatchLimit(String str, String str2, long j, int i, int i2) throws RemoteException;

    List<TagFileInfoGroup> getTagFileInfoGroupLimit(String str, String str2, int i, int i2) throws RemoteException;

    int getTagFileInfoListBatchCount(String str, String str2) throws RemoteException;

    int getTagFileInfoListCount(String str, String str2) throws RemoteException;

    List<TagFileInfo> getTagFileInfoListLimit(String str, String str2, int i, int i2) throws RemoteException;

    TagInfo getTagInfo(String str, String str2) throws RemoteException;

    int getTagInfoListCount(String str) throws RemoteException;

    List<TagInfo> getTagInfoListLimit(String str, int i, int i2) throws RemoteException;

    int getUploadFileInfoListCount(int i) throws RemoteException;

    List<FileInfoDetail> getUploadFileInfoListLimit(int i, int i2, int i3) throws RemoteException;

    FileInfoDetail getUploadManualFileInfo(String str, String str2) throws RemoteException;

    boolean isExistLocalData() throws RemoteException;

    int isGeneralFileDownloading(FileInfo[] fileInfoArr, int i) throws RemoteException;

    int isGeneralFileUploading(FileInfo[] fileInfoArr) throws RemoteException;

    List<ShareReceiver> isHWAccountList(List<ShareReceiver> list) throws RemoteException;

    int isShareFileDownloading(FileInfo fileInfo, int i) throws RemoteException;

    boolean isSupportSns() throws RemoteException;

    int modifyGeneralAlbum(String str, String str2, String str3) throws RemoteException;

    int modifyGeneralFile(FileInfo fileInfo) throws RemoteException;

    int modifyShareAlbum(String str, String str2) throws RemoteException;

    int modifyShareFile(FileInfo fileInfo) throws RemoteException;

    int modifyShareRecAdd(ShareInfo shareInfo, List<ShareReceiver> list) throws RemoteException;

    int modifyShareRecDel(ShareInfo shareInfo, List<ShareReceiver> list) throws RemoteException;

    List<TagFileInfo> modifyTagFileInfoList(String str, String str2, String str3, TagFileInfo[] tagFileInfoArr) throws RemoteException;

    List<TagInfo> modifyTagInfoList(String str, TagInfo[] tagInfoArr, String str2) throws RemoteException;

    int moveGeneralFile(String str, String str2, FileInfo[] fileInfoArr) throws RemoteException;

    List<TagFileInfo> moveTagFileInfoList(String str, String str2, TagFileInfo[] tagFileInfoArr) throws RemoteException;

    List<TagFileInfo> moveToTagFileInfoList(String str, String str2, TagFileInfo[] tagFileInfoArr, String str3) throws RemoteException;

    int pauseShareDownloadTask(FileInfo[] fileInfoArr, int i) throws RemoteException;

    void refreshGeneralAlbum() throws RemoteException;

    void refreshShare() throws RemoteException;

    void refreshSingleGeneralAlbum(String str) throws RemoteException;

    void refreshSingleShare(String str) throws RemoteException;

    void refreshSingleTag(String str, String str2) throws RemoteException;

    void refreshTag() throws RemoteException;

    boolean registerCallback(ICloudAlbumCallback iCloudAlbumCallback) throws RemoteException;

    int setAlbumProperties(SettingsProp settingsProp) throws RemoteException;

    int shareResultConfirm(String str, int i, String str2) throws RemoteException;

    int startAsyncAlbumInfo() throws RemoteException;

    int startDownloadTask(FileInfoDetail[] fileInfoDetailArr) throws RemoteException;

    int startDownloadTaskAll() throws RemoteException;

    int startUploadTask(FileInfoDetail[] fileInfoDetailArr) throws RemoteException;

    int startUploadTaskAll() throws RemoteException;

    void unregisterCallback(ICloudAlbumCallback iCloudAlbumCallback) throws RemoteException;

    int updateShareInfoPrivilege(String str, int i) throws RemoteException;

    int uploadGeneralFile(String str, String str2, String str3) throws RemoteException;

    int uploadGeneralFileList(List<FileInfo> list) throws RemoteException;
}
