package com.huawei.gallery.photoshare.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.sqlite.SQLiteException;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.StatFs;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.android.gallery3d.R;
import com.android.gallery3d.app.Config$CommonAlbumFragment;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.CloudSwitchHelper;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.PhotoShareAlbum;
import com.android.gallery3d.data.PhotoShareDownUpNotifier;
import com.android.gallery3d.settings.HicloudAccountManager;
import com.android.gallery3d.ui.ActionDeleteAndConfirm;
import com.android.gallery3d.ui.ActionRecycleAndConfirm;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.GalleryData;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.SharePreferenceUtils;
import com.huawei.android.cg.ICloudAlbumCallback;
import com.huawei.android.cg.ICloudAlbumCallback.Stub;
import com.huawei.android.cg.ICloudAlbumService;
import com.huawei.android.cg.vo.AccountInfo;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.SettingsProp;
import com.huawei.android.cg.vo.ShareInfo;
import com.huawei.android.cg.vo.TagInfo;
import com.huawei.gallery.app.PhotoShareTimeBucketActivity;
import com.huawei.gallery.media.CloudLocalSyncService;
import com.huawei.gallery.media.database.CloudTableOperateHelper;
import com.huawei.gallery.photoshare.receiver.PhotoShareNotificationDeleteIntentReceiver;
import com.huawei.gallery.photoshare.receiver.PhotoShareSdkCallBackManager;
import com.huawei.gallery.photoshare.receiver.PhotoShareSdkCallBackManager.MyListener;
import com.huawei.gallery.photoshare.ui.PhotoShareNewInviteActivity;
import com.huawei.gallery.photoshare.uploadservice.UploadService;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.servicemanager.CloudManager;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.util.DeleteMsgUtil;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class PhotoShareUtils {
    private static final Uri CLOUD_GUID_VERSION_URI = Uri.parse("content://com.huawei.android.cg.albumProvider/albumVersionStatus_guid");
    private static final Uri CLOUD_RECYCLE_ALBUM_VERSION_URI = Uri.parse("content://com.huawei.android.cg.albumProvider/recycleAlbumVersionStatus");
    private static final Uri CLOUD_VERIOSN_URI = Uri.parse("content://com.huawei.android.cg.albumProvider/albumVersionStatus");
    private static int COVER_HEIGHT = 0;
    private static int COVER_WIDTH = 0;
    public static final String INNER_CAMERA_PATH = (Environment.getExternalStorageDirectory() + Constant.CAMERA_PATH);
    public static final String INNER_SCREEN_SHOT_PATH = (Environment.getExternalStorageDirectory() + "/Pictures/Screenshots");
    private static int IS_CLOUD_GUID_SUPPORT = -1;
    private static int IS_CLOUD_NORMANDY_VERSION = -1;
    private static int IS_CLOUD_RECYCLE_SUPPORT = -1;
    private static final Object LOCK = new Object();
    private static final Uri LOGIN_STATUS_URI = Uri.parse("content://com.huawei.android.hicloud.loginProvider/login_user");
    public static final String PHOTOSHARE_DOWNLOAD_PATH = (Environment.getExternalStorageDirectory() + "/PhotoShareDownload");
    public static final String PHOTOSHARE_LCD_PATH = (Environment.getExternalStorageDirectory() + "/" + ".photoShare/thumb/lcd");
    public static final String PHOTOSHARE_THUMB_PATH = (Environment.getExternalStorageDirectory() + "/" + ".photoShare/thumb/thumb");
    private static final Uri QUERY_FVERSION_URI = Uri.parse("content://com.huawei.android.cg.albumProvider/queryFVersion");
    private static final Uri QUERY_UID_URI = Uri.parse("content://com.huawei.android.cg.albumProvider/queryUid");
    private static int SCROLL_VIEW_HEIGHT_MAX = 0;
    private static final int STORAGE_SIZE_G = getStorageSizeG();
    private static final Uri SUPPORT_CLOUD_URI = Uri.parse("content://com.huawei.android.hicloud.provider/is_support_cloudphoto");
    private static final Uri SWITCHER_STATUS_URI = Uri.parse("content://com.huawei.android.hicloud.provider/query_album_switch_status");
    private static final Uri SWITCH_STATUS_URI = Uri.parse("content://com.huawei.android.cg.albumProvider/switchStatus");
    private static Context mAppContext;
    private static boolean mCanUpdateDownloadStatusBarState = false;
    private static boolean mCanUpdateUploadStatusBarState = false;
    private static ArrayList<String> mDealingInvite = new ArrayList();
    private static NotifyBroker mDownUpNotifier = new NotifyBroker();
    private static FamilyShareCreateListener mFamilyShareCreateListener;
    private static Boolean mFirstBind = Boolean.valueOf(true);
    private static Handler mHandler;
    private static updateHeadInfoListener mHeadInfoListener;
    private static int mHeightPixels = 0;
    private static boolean mInitializeFinished = false;
    private static boolean mIsAPPInstalled = false;
    private static boolean mIsNeedAddPhoto = false;
    private static boolean mIsSupportPhotoShareFeature = true;
    private static WeakHashMap<DownLoadProgressListener, Object> mListeners = new WeakHashMap();
    private static volatile boolean mLockReload = false;
    private static AccountInfo mLogOnAccount = null;
    private static boolean mMethodRunOnce = true;
    private static Runnable mRunnable;
    private static int mSdkAidlVersion = 0;
    private static ICloudAlbumService mServer;
    private static ICloudAlbumCallback mServiceCallback = new Stub() {
        public void onResult(int msg, Bundle data) throws RemoteException {
            GalleryLog.v("PhotoShareUtils", "msg " + msg);
            if (PhotoShareUtils.checkDataIsValid(msg, data)) {
                final String string;
                switch (msg) {
                    case 4004:
                        PhotoShareUtils.setLogOnAccount(PhotoShareUtils.queryLogOnAccount());
                        PhotoShareUtils.clearDeletedPhoto();
                        PhotoShareUtils.addFolderChangeMessage(3);
                        break;
                    case 4005:
                        GalleryLog.w("PhotoShareUtils", "UI_NOTIFY_LOGOFF called");
                        PhotoShareUtils.clearDeletedPhoto();
                        PhotoShareUtils.setLogOnAccount(null);
                        PhotoShareUtils.sUserId = null;
                        PhotoShareUtils.updateLocalSwitch(false);
                        PhotoShareUtils.resetCloudPath();
                        PhotoShareUtils.enableDownloadStatusBarNotification(false);
                        PhotoShareUtils.enableUploadStatusBarNotification(false);
                        PhotoShareUtils.clearNotification(PhotoShareUtils.mAppContext);
                        PhotoShareUtils.notifyPhotoShareFolderChanged(3);
                        break;
                    case 7005:
                        final String hashDownloadProgress = data.getString("hash");
                        final String albumIdDownloadProgress = data.getString("albumId");
                        final int thumbTypeDownloadProgress = data.getInt("thumbType");
                        final Long total = Long.valueOf(data.getLong("totalSize"));
                        final Long current = Long.valueOf(data.getLong("currentSize"));
                        GalleryLog.printPhotoShareLog("PhotoShareUtils", "hash " + hashDownloadProgress + " albumId " + albumIdDownloadProgress + " thumbType " + thumbTypeDownloadProgress + " total " + total + " current " + current);
                        final String downloadProcessUniqueId = data.getString("uniqueId");
                        PhotoShareUtils.mHandler.post(new Runnable() {
                            public void run() {
                                for (DownLoadProgressListener listener : PhotoShareUtils.getListener()) {
                                    listener.downloadProgress(hashDownloadProgress, albumIdDownloadProgress, downloadProcessUniqueId, thumbTypeDownloadProgress, total, current);
                                }
                            }
                        });
                        break;
                    case 7006:
                        final int resultDownloadFinish = data.getInt("retCode");
                        final String hashDownloadFinish = data.getString("hash");
                        final String albumIdDownloadFinish = data.getString("albumId");
                        final int thumbTypeDownloadFinish = data.getInt("thumbType");
                        final String downloadFinishUniqueId = data.getString("uniqueId");
                        PhotoShareUtils.mHandler.post(new Runnable() {
                            public void run() {
                                for (DownLoadProgressListener listener : PhotoShareUtils.getListener()) {
                                    listener.downloadFinish(hashDownloadFinish, albumIdDownloadFinish, downloadFinishUniqueId, thumbTypeDownloadFinish, resultDownloadFinish);
                                }
                            }
                        });
                        if (resultDownloadFinish == 0) {
                            GalleryLog.printPhotoShareLog("PhotoShareUtils", " hash " + hashDownloadFinish + " albumId " + albumIdDownloadFinish + " thumbType " + thumbTypeDownloadFinish);
                            if (thumbTypeDownloadFinish == 2) {
                                PhotoShareUtils.addContentChangeMessage(1, albumIdDownloadFinish);
                            } else {
                                PhotoShareUtils.notifyPhotoShareContentChange(1, albumIdDownloadFinish);
                            }
                        }
                        if (thumbTypeDownloadFinish == 0) {
                            PhotoShareUtils.updateNotify();
                            PhotoShareUtils.refreshStatusBar(true);
                            break;
                        }
                        break;
                    case 7007:
                    case 7009:
                    case 7037:
                        PhotoShareUtils.addFolderChangeMessage(1);
                        break;
                    case 7008:
                    case 7010:
                        String albumIdFileUpdate = data.getString("albumId");
                        GalleryLog.printPhotoShareLog("PhotoShareUtils", "albumId " + albumIdFileUpdate + " result " + data.getInt("retCode"));
                        PhotoShareUtils.addContentChangeMessage(1, albumIdFileUpdate);
                        PhotoShareUtils.updateNotify();
                        break;
                    case 7011:
                        PhotoShareUtils.dealInviteResult(data.getString("albumId"), data.getString("shareAccount"), data.getInt("shareStatus"), data.getString("shareName"));
                        break;
                    case 7012:
                        PhotoShareUtils.dealNewInvite(data.getString("albumId"), data.getString("shareName"), data.getString("shareAccount"), data.getString("shareUserId"));
                        break;
                    case 7013:
                        PhotoShareUtils.reNameLocalMediaItem(data.getString("localRealPath"), data.getString("localRealPathOrg"));
                        break;
                    case 7014:
                        PhotoShareUtils.ownerReceiverChanged(data.getString("albumId"), data.getString("shareAccount"), data.getString("shareName"));
                        break;
                    case 7015:
                        PhotoShareUtils.receiverNotReceive(data.getString("albumId"), data.getString("shareAccount"), data.getString("shareName"));
                        break;
                    case 7019:
                        string = data.getString("albumId");
                        PhotoShareUtils.mHandler.post(new Runnable() {
                            public void run() {
                                if (PhotoShareUtils.mFamilyShareCreateListener != null) {
                                    PhotoShareUtils.mFamilyShareCreateListener.createFamilyShare(string);
                                }
                            }
                        });
                        break;
                    case 7021:
                        string = data.getString("albumId");
                        PhotoShareUtils.mHandler.post(new Runnable() {
                            public void run() {
                                if (PhotoShareUtils.mHeadInfoListener != null) {
                                    PhotoShareUtils.mHeadInfoListener.headInfoChanged(string);
                                }
                            }
                        });
                        break;
                    case 7022:
                        PhotoShareUtils.updateNotify();
                        PhotoShareUtils.refreshStatusBar(false);
                        break;
                    case 7023:
                        PhotoShareUtils.updateNotify();
                        PhotoShareUtils.refreshStatusBar(true);
                        break;
                    case 7025:
                        final String file_hash = data.getString("hash");
                        final String file_album = data.getString("albumId");
                        final long totalSize = data.getLong("totalSize");
                        final long finishSize = data.getLong("currentSize");
                        PhotoShareUtils.mHandler.post(new Runnable() {
                            public void run() {
                                for (DownLoadProgressListener listener : PhotoShareUtils.getListener()) {
                                    listener.downloadProgress(file_hash, file_album, null, 0, Long.valueOf(totalSize), Long.valueOf(finishSize));
                                }
                            }
                        });
                        PhotoShareUtils.updateNotify();
                        break;
                    case 7030:
                        PhotoShareUtils.addTagListChangeMessage(data.getString("categoryId"));
                        break;
                    case 7031:
                        PhotoShareUtils.addFolderChangeMessage(2);
                        break;
                    case 7032:
                    case 7034:
                        PhotoShareUtils.addTagContentChangeMessage(data.getString("categoryId"), data.getString("tagId"));
                        break;
                    case 7035:
                        Context context = PhotoShareUtils.mAppContext;
                        GalleryLog.d("PhotoShareUtils", "flversion changed, has context ? " + (context != null));
                        if (context != null) {
                            CloudLocalSyncService.stopCloudSync(context);
                        }
                        PhotoShareUtils.hasNeverSynchronizedCloudData();
                        PhotoShareUtils.makeSureDataBaseInitFalse();
                        PhotoShareUtils.resetCloudPath();
                        PhotoShareUtils.forceRefreshCloudManager();
                        PhotoShareUtils.notifyCloudUpload();
                        break;
                    case 7036:
                        RefreshHelper.setSyncFailed(true);
                        break;
                    case 8001:
                        PhotoShareUtils.updateLocalSwitch(data.getBoolean("GeneralAblum", false));
                        PhotoShareUtils.resetCloudPath();
                        PhotoShareUtils.forceRefreshCloudManager();
                        PhotoShareUtils.addFolderChangeMessage(2);
                        break;
                    case 8002:
                        PhotoShareUtils.notifyPhotoShareFolderChanged(1);
                        break;
                    case 8003:
                        PhotoShareUtils.notifyPhotoShareFolderChanged(2);
                        break;
                    case 8004:
                    case 8005:
                        PhotoShareUtils.notifyCloudUpload();
                        break;
                }
                return;
            }
            GalleryLog.d("photoshareLogTag", "data should not null");
        }
    };
    private static ServiceConnection mServiceConnection;
    private static ArrayList<String> mShareList = new ArrayList();
    private static PhotoShareStatusBar mStatusBar;
    private static final HashMap<String, Integer> mThirdPartAppNameMap = new HashMap();
    private static int mWidthPixels = 0;
    private static final HashMap<String, String> sAutoUploadAlbumBucketId = new HashMap();
    private static ArrayList<String> sCameraZeroSizePath = new ArrayList();
    private static final HashMap<String, String> sCloudAlbumBucketId = new HashMap();
    private static HashSet<String> sDeletedPhotoes = new HashSet();
    private static HandlerThread sHandlerThread;
    private static boolean sHasNeverSynchronizedCloudData = true;
    private static HandlerThread sJobThread = new HandlerThread("lcd-job-thread");
    private static long sLastBindServiceTime = 0;
    private static boolean sLocalSwitch;
    public static final int sMaxLcdThumbCount = getMaxLcdLoadCount();
    private static ArrayList<PhotoShareCallBackMessage> sMessages = new ArrayList();
    private static String[] sNeedResetMediaSetPath = new String[]{"/virtual/all/photoshare", "/virtual/photoshare", "/gallery/album/timebucket"};
    private static Runnable sRefreshChecker = new Runnable() {
        public void run() {
            synchronized (PhotoShareUtils.class) {
                PhotoShareUtils.notifyDataDirty();
                PhotoShareUtils.sRefreshHandler.postDelayed(PhotoShareUtils.sRefreshChecker, 1000);
            }
        }
    };
    private static Handler sRefreshHandler;
    public static final int sTrimThumbCount = getTrimLcdCount();
    private static String sUserId = null;
    private static Runnable sclearFVersion = new Runnable() {
        public void run() {
            PhotoShareUtils.clearFVersion();
        }
    };
    private static Runnable sdoAfterConnected = new Runnable() {
        public void run() {
            PhotoShareUtils.doAfterConnected();
        }
    };

    public interface DownLoadProgressListener {
        void downloadFinish(String str, String str2, String str3, int i, int i2);

        void downloadProgress(String str, String str2, String str3, int i, Long l, Long l2);
    }

    public interface FamilyShareCreateListener {
        void createFamilyShare(String str);
    }

    public interface updateHeadInfoListener {
        void headInfoChanged(String str);
    }

    private static class NotifyBroker {
        private WeakHashMap<PhotoShareDownUpNotifier, Object> mNotifiers;

        private NotifyBroker() {
            this.mNotifiers = new WeakHashMap();
        }

        public synchronized void registerNotifier(PhotoShareDownUpNotifier notifier) {
            this.mNotifiers.put(notifier, null);
        }

        public synchronized void onChange() {
            for (PhotoShareDownUpNotifier notifier : this.mNotifiers.keySet()) {
                notifier.onChange();
            }
        }
    }

    public static boolean isHiCloudLogin() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x005d in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r8 = 0;
        r6 = 0;
        r1 = mAppContext;
        r0 = r1.getContentResolver();
        r1 = LOGIN_STATUS_URI;	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        r2 = 0;	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        r3 = 0;	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        r4 = 0;	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        r5 = 0;	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        if (r6 == 0) goto L_0x002a;	 Catch:{ Exception -> 0x0039, all -> 0x005e }
    L_0x0014:
        r1 = r6.moveToNext();	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        if (r1 == 0) goto L_0x002a;	 Catch:{ Exception -> 0x0039, all -> 0x005e }
    L_0x001a:
        r1 = "PhotoShareUtils";	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        r2 = "isHiCloudLogin return true";	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        com.android.gallery3d.util.GalleryLog.v(r1, r2);	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        r1 = 1;
        if (r6 == 0) goto L_0x0029;
    L_0x0026:
        r6.close();
    L_0x0029:
        return r1;
    L_0x002a:
        if (r6 == 0) goto L_0x002f;
    L_0x002c:
        r6.close();
    L_0x002f:
        r1 = "PhotoShareUtils";
        r2 = "isHiCloudLogin return false";
        com.android.gallery3d.util.GalleryLog.v(r1, r2);
        return r8;
    L_0x0039:
        r7 = move-exception;
        r1 = "PhotoShareUtils";	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        r2.<init>();	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        r3 = "isHiCloudLogin Exception ";	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        r3 = r7.toString();	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        r2 = r2.toString();	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        com.android.gallery3d.util.GalleryLog.v(r1, r2);	 Catch:{ Exception -> 0x0039, all -> 0x005e }
        if (r6 == 0) goto L_0x005d;
    L_0x005a:
        r6.close();
    L_0x005d:
        return r8;
    L_0x005e:
        r1 = move-exception;
        if (r6 == 0) goto L_0x0064;
    L_0x0061:
        r6.close();
    L_0x0064:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.gallery.photoshare.utils.PhotoShareUtils.isHiCloudLogin():boolean");
    }

    static {
        sJobThread.start();
    }

    public static void addCameraPath(String data) {
        synchronized (sCameraZeroSizePath) {
            sCameraZeroSizePath.add(data);
        }
    }

    public static boolean getHasNeverSynchronizedCloudDataFromCache() {
        return sHasNeverSynchronizedCloudData;
    }

    public static void setHasNeverSynchronizedCloudData(boolean hasNeverSynchronizedCloudData) {
        sHasNeverSynchronizedCloudData = hasNeverSynchronizedCloudData;
    }

    public static boolean removeCameraPath(String data) {
        boolean remove;
        synchronized (sCameraZeroSizePath) {
            remove = sCameraZeroSizePath.remove(data);
        }
        return remove;
    }

    public static void addPhotos(String deletedPhoto) {
        synchronized (sDeletedPhotoes) {
            sDeletedPhotoes.add(deletedPhoto);
        }
    }

    public static boolean findDeletedPhotos(String deletedPhoto) {
        boolean contains;
        synchronized (sDeletedPhotoes) {
            contains = sDeletedPhotoes.contains(deletedPhoto);
        }
        return contains;
    }

    public static void deleteDeletedPhoto(String deletedPhoto) {
        synchronized (sDeletedPhotoes) {
            sDeletedPhotoes.remove(deletedPhoto);
        }
    }

    public static void clearDeletedPhoto() {
        synchronized (sDeletedPhotoes) {
            sDeletedPhotoes.clear();
        }
    }

    private static int getStorageSizeG() {
        int ret = 0;
        try {
            ret = (int) (new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath()).getTotalBytes() >> 30);
        } catch (Exception e) {
            GalleryLog.i("PhotoShareUtils", "Fail to access external storage." + e.getMessage());
        }
        GalleryLog.i("PhotoShareUtils", "storage size: " + ret + "G");
        return ret;
    }

    private static int getTrimLcdCount() {
        return STORAGE_SIZE_G >= 30 ? 2000 : 1000;
    }

    private static int getMaxLcdLoadCount() {
        return STORAGE_SIZE_G >= 30 ? 10000 : 4000;
    }

    public static void updateLocalSwitch(boolean localSwitch) {
        sLocalSwitch = localSwitch;
    }

    public static boolean getLocalSwitch() {
        return sLocalSwitch;
    }

    public static HashMap<String, String> getsAutoUploadAlbumBucketId() {
        return (HashMap) sAutoUploadAlbumBucketId.clone();
    }

    public static AccountInfo getLogOnAccount() {
        if (mLogOnAccount == null) {
            setLogOnAccount(queryLogOnAccount());
        }
        return mLogOnAccount;
    }

    public static void setLogOnAccount(AccountInfo accountInfo) {
        synchronized (LOCK) {
            mLogOnAccount = accountInfo;
        }
    }

    private static AccountInfo queryLogOnAccount() {
        AccountInfo myAccount = null;
        if (getServer() != null) {
            try {
                myAccount = getServer().getLogOnInfo();
            } catch (RemoteException e) {
                dealRemoteException(e);
            }
        }
        sUserId = queryUserId();
        return myAccount;
    }

    public static JobBulk getBulk(int thumbType) {
        return new JobBulk(mAppContext.getContentResolver(), sJobThread.getLooper(), thumbType);
    }

    private static void notifyDataDirty() {
        synchronized (sMessages) {
            if (!sMessages.isEmpty()) {
                PhotoShareCallBackMessage message;
                if (sMessages.size() >= 5) {
                    sMessages.clear();
                    message = new PhotoShareCallBackMessage();
                    message.setMessageType(2);
                    message.setAlbumSetType(3);
                } else {
                    message = (PhotoShareCallBackMessage) sMessages.remove(0);
                }
                switch (message.getMessageType()) {
                    case 1:
                        notifyPhotoShareContentChange(message.getAlbumSetType(), message.getAlbumPath());
                        break;
                    case 2:
                        notifyPhotoShareFolderChanged(message.getAlbumSetType());
                        break;
                    case 3:
                        notifyPhotoShareTagListChanged(message.getCategoryType());
                        break;
                    case 4:
                        notifyPhotoShareTagContentChanged(message.getCategoryType(), message.getTagAlbumID());
                        break;
                }
            }
        }
    }

    private static void addContentChangeMessage(int albumSetType, String albumPath) {
        if (!TextUtils.isEmpty(albumPath)) {
            PhotoShareCallBackMessage callbackMessage = new PhotoShareCallBackMessage();
            callbackMessage.setMessageType(1);
            callbackMessage.setAlbumSetType(albumSetType);
            callbackMessage.setAlbumPath(albumPath);
            addMessageCommon(callbackMessage);
        }
    }

    private static void addFolderChangeMessage(int albumSetType) {
        PhotoShareCallBackMessage callbackMessage = new PhotoShareCallBackMessage();
        callbackMessage.setMessageType(2);
        callbackMessage.setAlbumSetType(albumSetType);
        addMessageCommon(callbackMessage);
    }

    private static void addTagListChangeMessage(String categoryID) {
        PhotoShareCallBackMessage callbackMessage = new PhotoShareCallBackMessage();
        callbackMessage.setMessageType(3);
        callbackMessage.setCategoryType(categoryID);
        addMessageCommon(callbackMessage);
    }

    private static void addTagContentChangeMessage(String categoryID, String tagID) {
        PhotoShareCallBackMessage callbackMessage = new PhotoShareCallBackMessage();
        callbackMessage.setMessageType(4);
        callbackMessage.setCategoryType(categoryID);
        callbackMessage.setTagAlbumID(tagID);
        addMessageCommon(callbackMessage);
    }

    private static void addMessageCommon(PhotoShareCallBackMessage message) {
        synchronized (sMessages) {
            if (!sMessages.contains(message)) {
                sMessages.add(message);
            }
        }
    }

    public static void lockReload() {
        GalleryLog.v("PhotoShareUtils", "lockReload");
        mLockReload = true;
    }

    public static void unLockReload() {
        GalleryLog.v("PhotoShareUtils", "unLockReload");
        mLockReload = false;
    }

    private PhotoShareUtils() {
    }

    public static void setUpdateHeadInfoListener(updateHeadInfoListener listener) {
        mHeadInfoListener = listener;
    }

    public static void addListener(DownLoadProgressListener listener) {
        synchronized (mListeners) {
            if (mListeners.containsKey(listener)) {
                throw new IllegalArgumentException();
            }
            mListeners.put(listener, null);
        }
    }

    public static void removeListener(DownLoadProgressListener listener) {
        synchronized (mListeners) {
            if (mListeners.containsKey(listener)) {
                mListeners.remove(listener);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    private static HashSet<DownLoadProgressListener> getListener() {
        HashSet<DownLoadProgressListener> listeners = new HashSet();
        synchronized (mListeners) {
            listeners.addAll(mListeners.keySet());
        }
        return listeners;
    }

    public static void setFamilyShareCreateListener(FamilyShareCreateListener listener) {
        mFamilyShareCreateListener = listener;
    }

    public static int getScreenWidth() {
        return mWidthPixels;
    }

    public static void updateNotify() {
        mDownUpNotifier.onChange();
    }

    private static void forceRefreshCloudManager() {
        CloudManager cloudManager = (CloudManager) ((GalleryApp) mAppContext).getAppComponent(CloudManager.class);
        if (cloudManager != null) {
            cloudManager.forceRefresh();
        }
    }

    private static boolean checkDataIsValid(int msg, Bundle data) {
        if (data != null) {
            return true;
        }
        if (msg == 7008 || msg == 7010 || msg == 7006 || msg == 7005 || msg == 7013 || msg == 7011 || msg == 7012 || msg == 7014 || msg == 7015 || msg == 7021 || msg == 7025 || msg == 7032 || msg == 7030 || msg == 7019 || msg == 8001) {
            return false;
        }
        return true;
    }

    private static void checkCameraAlbumSwitch() {
        if (isCloudPhotoSwitchOpen()) {
            Closeable closeable = null;
            try {
                closeable = mAppContext.getContentResolver().query(PhotoShareConstants.AUTO_UPLOAD_ALBUM_TABLE_URI, new String[]{"count(*)"}, "albumType=1", null, null);
                if (closeable != null && closeable.moveToNext() && closeable.getInt(0) == 1) {
                    GalleryLog.v("PhotoShareUtils", "Camera Path already exist");
                    return;
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put("relativePath", "/DCIM/Camera");
                contentValues.put("albumName", "Camera");
                contentValues.put("albumId", "default-album-1");
                contentValues.put("albumType", Integer.valueOf(1));
                contentValues.put("tempId", Integer.valueOf(GalleryUtils.getBucketId("/DCIM/Camera")));
                mAppContext.getContentResolver().insert(PhotoShareConstants.AUTO_UPLOAD_ALBUM_TABLE_URI, contentValues);
                Utils.closeSilently(closeable);
            } catch (SQLiteException e) {
                GalleryLog.v("PhotoShareUtils", "checkCameraAlbumSwitch SQLiteException " + e.toString());
            } finally {
                Utils.closeSilently(closeable);
            }
        }
    }

    private static void resetCloudPath() {
        for (String path : sNeedResetMediaSetPath) {
            MediaSet mediaSet = (MediaSet) Path.fromString(path).getObject();
            if (mediaSet != null) {
                mediaSet.reset();
            }
        }
        CloudSwitchHelper.resetCloudAutoUploadSwitch();
        ((GalleryApp) mAppContext).getDataManager().notifyChange();
    }

    private static void dealInviteResult(String shareId, String receiverAcc, int status, String folderName) {
        folderName = getShareName(shareId, folderName);
        String content;
        String ticker_title;
        String contentTitle;
        if (status == 1) {
            content = MessageFormat.format(mAppContext.getString(R.string.photoshare_notify_confirm_contentText), new Object[]{receiverAcc, folderName});
            ticker_title = mAppContext.getString(R.string.photoshare_ticker_accept);
            contentTitle = ticker_title;
            createNotification(ticker_title, ticker_title, content, getNullPendingIntent(mAppContext), shareId + receiverAcc);
        } else if (status == 2) {
            content = MessageFormat.format(mAppContext.getString(R.string.photoshare_notify_reject_contentText), new Object[]{receiverAcc, folderName});
            ticker_title = mAppContext.getString(R.string.photoshare_ticker_reject);
            contentTitle = ticker_title;
            createNotification(ticker_title, ticker_title, content, getNullPendingIntent(mAppContext), shareId + receiverAcc);
        }
    }

    private static void dealNewInvite(String shareId, String shareName, String ownerAcc, String ownerId) {
        AccountInfo accountInfo = getLogOnAccount();
        if (!mDealingInvite.contains(shareId)) {
            try {
                if (getServer().getShare(shareId) == null) {
                    String accountName;
                    shareName = getShareName(shareId, shareName);
                    String content = MessageFormat.format(mAppContext.getString(R.string.photoshare_notify_invite_contentText), new Object[]{ownerAcc, shareName});
                    String ticker_title = mAppContext.getString(R.string.photoshare_ticker_invite);
                    String contentTitle = ticker_title;
                    Context context = mAppContext;
                    if (accountInfo != null) {
                        accountName = accountInfo.getAccountName();
                    } else {
                        accountName = "";
                    }
                    createNotification(ticker_title, ticker_title, content, getInvitePendingIntent(context, accountName, ownerAcc, shareId, shareName, ownerId), shareId);
                }
            } catch (RemoteException e) {
                dealRemoteException(e);
            }
        }
    }

    private static void receiverNotReceive(String shareId, String ownerAccount, String folderName) {
        String content = MessageFormat.format(mAppContext.getString(R.string.photoshare_notify_not_receive_contentText), new Object[]{ownerAccount, getShareName(shareId, folderName)});
        String ticker_title = mAppContext.getString(R.string.photoshare_ticker_not_receive);
        String contentTitle = ticker_title;
        createNotification(ticker_title, ticker_title, content, getNullPendingIntent(mAppContext), shareId + ownerAccount);
    }

    private static void ownerReceiverChanged(String shareId, String receiverAcc, String folderName) {
        String content = MessageFormat.format(mAppContext.getString(R.string.photoshare_notify_receiver_change_contentText), new Object[]{receiverAcc, getShareName(shareId, folderName)});
        String ticker_title = mAppContext.getString(R.string.photoshare_ticker_receiver_change);
        String contentTitle = ticker_title;
        createNotification(ticker_title, ticker_title, content, getNullPendingIntent(mAppContext), shareId + receiverAcc);
    }

    private static void createNotification(String ticker_title, String contentTitle, String content, PendingIntent contentIntent, final String shareId) {
        final Builder mBuilder = new Builder(mAppContext);
        final NotificationManager notificationManager = (NotificationManager) mAppContext.getSystemService("notification");
        mBuilder.setTicker(ticker_title);
        mBuilder.setSmallIcon(R.drawable.ic_gallery_notify_screenshot);
        mBuilder.setContentTitle(contentTitle);
        mBuilder.setContentText(content);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setDefaults(-1);
        mBuilder.setStyle(new BigTextStyle(mBuilder).bigText(content).setBigContentTitle(contentTitle));
        mHandler.post(new Runnable() {
            public void run() {
                notificationManager.notify("photoshare", PhotoShareUtils.getNotifyId(shareId), mBuilder.build());
            }
        });
    }

    private static PendingIntent getInvitePendingIntent(Context context, String loginAccount, String account, String shareId, String displayName, String ownerId) {
        if (loginAccount == null) {
            return null;
        }
        Intent intent = new Intent(context.getApplicationContext(), PhotoShareNewInviteActivity.class);
        intent.putExtra("loginAccount", loginAccount);
        intent.putExtra("shareAccount", account);
        intent.putExtra("shareid", shareId);
        intent.putExtra("shareName", displayName);
        intent.putExtra("ownerID", ownerId);
        return PendingIntent.getActivity(context, getNotifyId(shareId), intent, 134217728);
    }

    private static PendingIntent getNullPendingIntent(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(context, PhotoShareNotificationDeleteIntentReceiver.class));
        intent.setAction("com.huawei.gallery.action.DO_NOTHING");
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private static void reNameLocalMediaItem(String newPath, String oldPath) {
        File newFile = new File(newPath);
        if (newFile.exists()) {
            ContentValues values = new ContentValues();
            values.put("_data", newFile.getAbsolutePath());
            values.put("title", newPath.substring(newPath.lastIndexOf("/") + 1, newPath.lastIndexOf(".")));
            values.put("_display_name", newPath.substring(newPath.lastIndexOf("/") + 1));
            int updated = 0;
            try {
                updated = mAppContext.getContentResolver().update(Media.EXTERNAL_CONTENT_URI, values, "_data=?", new String[]{oldPath});
            } catch (IllegalArgumentException e) {
                GalleryLog.e("PhotoShareUtils", "IllegalArgumentExceptionN in mMediaProvider.update" + e.getMessage());
            } catch (SecurityException e2) {
                GalleryLog.w("PhotoShareUtils", "No permission to operate MediaProvider!");
            }
            if (updated == 0) {
                GalleryLog.v("PhotoShareUtils", "updateFailed " + newPath);
            }
            GalleryApp application = mAppContext;
            GalleryData favoriteData = application.getGalleryData();
            if (favoriteData.isMyFavorite(oldPath)) {
                if (favoriteData.updateFavorite(oldPath, false) == 0 || favoriteData.updateFavorite(newFile.getAbsolutePath(), true) == 0) {
                    GalleryLog.v("PhotoShareUtils", "Constant.MYFAVORITE_URI update failure");
                }
                application.getDataManager().notifyChange(Constant.MYFAVORITE_URI);
            }
        }
    }

    public static void notifyPhotoShareContentChange(int type, String path) {
        if (mLockReload) {
            GalleryLog.v("PhotoShareUtils", "lockReload not notifyPhotoShareContentChange");
            return;
        }
        synchronized (PhotoShareSdkCallBackManager.LOCK) {
            for (MyListener myListener : PhotoShareSdkCallBackManager.getInstance().getListeners().keySet()) {
                myListener.onContentChange(type, path);
            }
        }
    }

    public static void notifyPhotoShareFolderChanged(int type) {
        if (mLockReload) {
            GalleryLog.v("PhotoShareUtils", "lockReload not notifyPhotoShareFolderChanged");
            return;
        }
        synchronized (PhotoShareSdkCallBackManager.LOCK) {
            for (MyListener myListener : PhotoShareSdkCallBackManager.getInstance().getListeners().keySet()) {
                myListener.onFolderChange(type);
            }
        }
    }

    public static void notifyPhotoShareTagListChanged(String categoryID) {
        if (mLockReload) {
            GalleryLog.v("PhotoShareUtils", "lockReload not notifyPhotoShareTagListChanged");
            return;
        }
        synchronized (PhotoShareSdkCallBackManager.LOCK) {
            for (MyListener myListener : PhotoShareSdkCallBackManager.getInstance().getListeners().keySet()) {
                myListener.onTagListChanged(categoryID);
            }
        }
    }

    public static void notifyPhotoShareTagContentChanged(String categoryID, String tagID) {
        if (mLockReload) {
            GalleryLog.v("PhotoShareUtils", "lockReload not notifyPhotoShareTagContentChanged");
            return;
        }
        synchronized (PhotoShareSdkCallBackManager.LOCK) {
            for (MyListener myListener : PhotoShareSdkCallBackManager.getInstance().getListeners().keySet()) {
                myListener.onTagContentChanged(categoryID, tagID);
            }
        }
    }

    public static void addInvite(String shareId) {
        if (!mDealingInvite.contains(shareId)) {
            mDealingInvite.add(shareId);
        }
    }

    public static void removeInvite(String shareId) {
        mDealingInvite.remove(shareId);
    }

    private static void setSdkSettingsProp() {
        try {
            SettingsProp info = new SettingsProp();
            int microSize = Config$CommonAlbumFragment.get(mAppContext).slotViewSpec.getDefaultWidth();
            info.setThumbWidth(microSize);
            info.setThumbHeight(microSize);
            info.setLcdWidth(mWidthPixels);
            info.setLcdHeight(mHeightPixels);
            ArrayList<GalleryStorage> outerGalleryStorageList = GalleryStorageManager.getInstance().getOuterGalleryStorageList();
            info.setInternalRootPath(Environment.getExternalStorageDirectory().getPath());
            if (outerGalleryStorageList.size() > 0) {
                info.setExternalRootPath(((GalleryStorage) outerGalleryStorageList.get(0)).getPath());
            } else {
                info.setExternalRootPath(null);
            }
            info.setDownloadPath(PHOTOSHARE_DOWNLOAD_PATH);
            info.setLcdCachePath(PHOTOSHARE_LCD_PATH);
            info.setThumbCachePath(PHOTOSHARE_THUMB_PATH);
            info.setAutoLcdNum(2000);
            mServer.setAlbumProperties(info);
        } catch (RemoteException e) {
            dealRemoteException(e);
        }
    }

    public static void resetSDKSettings() {
        if (getServer() != null) {
            setSdkSettingsProp();
        }
    }

    public static void setApplicationContext(Context context) {
        mAppContext = context;
    }

    public static String getCloudAlbumIdByBucketId(String bucketId) {
        synchronized (sCloudAlbumBucketId) {
            String cloudAlbumId = (String) sCloudAlbumBucketId.get(bucketId);
        }
        if (RecycleUtils.supportRecycle() && TextUtils.isEmpty(cloudAlbumId)) {
            return RecycleUtils.getPreferenceValue(RecycleUtils.CLOUD_BUCKET_ALBUM_ID, bucketId);
        }
        return cloudAlbumId;
    }

    public static String getAutoUploadAlbumIdByBucketId(String bucketId) {
        String str;
        synchronized (sAutoUploadAlbumBucketId) {
            str = (String) sAutoUploadAlbumBucketId.get(bucketId);
        }
        return str;
    }

    public static String getAutoUploadBucketIds() {
        synchronized (sAutoUploadAlbumBucketId) {
            Set<String> autoUploadBuckets = sAutoUploadAlbumBucketId.keySet();
            if (autoUploadBuckets.size() == 0) {
                String str = "0";
                return str;
            }
            StringBuffer sb = new StringBuffer();
            for (String bucketId : autoUploadBuckets) {
                sb.append(", ").append(bucketId);
            }
            if (sb.length() > 0) {
                str = sb.substring(1);
                return str;
            }
            str = "0";
            return str;
        }
    }

    public static void initialCloudAlbumBucketId() {
        HashMap<String, String> cloudAlbumPathId = new HashMap();
        Closeable closeable = null;
        try {
            closeable = mAppContext.getContentResolver().query(PhotoShareConstants.CLOUD_ALBUM_TABLE_URI, new String[]{"lpath", "albumId"}, null, null, null);
            if (closeable == null) {
                GalleryLog.w("PhotoShareUtils", "query fail");
                return;
            }
            while (closeable.moveToNext()) {
                cloudAlbumPathId.put(closeable.getString(0), closeable.getString(1));
            }
            Utils.closeSilently(closeable);
            HashMap cloudAlbumBucketId = getCloudALbumBucketId(cloudAlbumPathId);
            synchronized (sCloudAlbumBucketId) {
                sCloudAlbumBucketId.clear();
                sCloudAlbumBucketId.putAll(cloudAlbumBucketId);
            }
        } catch (Exception e) {
            GalleryLog.d("photoshareLogTag", "initialAutoUploadAlbum SQLiteException  " + e.toString());
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public static HashMap<String, String> getCloudALbumBucketId(HashMap<String, String> albumPathId) {
        HashMap<String, String> cloudAlbumBucketId = new HashMap();
        GalleryStorageManager storageManager = GalleryStorageManager.getInstance();
        GalleryStorage innerStorage = storageManager.getInnerGalleryStorage();
        if (innerStorage == null) {
            return null;
        }
        for (String path : albumPathId.keySet()) {
            String albumId = (String) albumPathId.get(path);
            cloudAlbumBucketId.put(String.valueOf(innerStorage.getBucketID(path)), albumId);
            GalleryLog.d("photoshareLogTag", "innerStorage bucket id " + innerStorage.getBucketID(path) + " albumID " + albumId);
            ArrayList<Integer> outers = storageManager.getOuterGalleryStorageBucketIDsByArrayList(path);
            int size = outers.size();
            for (int i = 0; i < size; i++) {
                cloudAlbumBucketId.put(String.valueOf(outers.get(i)), albumId);
                GalleryLog.d("photoshareLogTag", "OuterStorage bucket id " + String.valueOf(outers.get(i)) + " albumID " + albumId);
            }
        }
        return cloudAlbumBucketId;
    }

    public static void initialAutoUploadAlbumBucketId() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:37)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:61)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r7 = new java.util.HashMap;
        r7.<init>();
        r8 = 0;
        r14 = com.huawei.gallery.storage.GalleryStorageManager.getInstance();
        r11 = r14.getInnerGalleryStorage();
        if (r11 != 0) goto L_0x0011;
    L_0x0010:
        return;
    L_0x0011:
        r0 = mAppContext;	 Catch:{ Exception -> 0x00da }
        r0 = r0.getContentResolver();	 Catch:{ Exception -> 0x00da }
        r1 = com.huawei.gallery.photoshare.utils.PhotoShareConstants.AUTO_UPLOAD_ALBUM_TABLE_URI;	 Catch:{ Exception -> 0x00da }
        r2 = 2;	 Catch:{ Exception -> 0x00da }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x00da }
        r3 = "relativePath";	 Catch:{ Exception -> 0x00da }
        r4 = 0;	 Catch:{ Exception -> 0x00da }
        r2[r4] = r3;	 Catch:{ Exception -> 0x00da }
        r3 = "albumId";	 Catch:{ Exception -> 0x00da }
        r4 = 1;	 Catch:{ Exception -> 0x00da }
        r2[r4] = r3;	 Catch:{ Exception -> 0x00da }
        r3 = 0;	 Catch:{ Exception -> 0x00da }
        r4 = 0;	 Catch:{ Exception -> 0x00da }
        r5 = 0;	 Catch:{ Exception -> 0x00da }
        r8 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x00da }
        if (r8 != 0) goto L_0x003e;	 Catch:{ Exception -> 0x00da }
    L_0x0031:
        r0 = "PhotoShareUtils";	 Catch:{ Exception -> 0x00da }
        r1 = "query fail";	 Catch:{ Exception -> 0x00da }
        com.android.gallery3d.util.GalleryLog.w(r0, r1);	 Catch:{ Exception -> 0x00da }
        com.android.gallery3d.common.Utils.closeSilently(r8);
        return;
    L_0x003e:
        r0 = r8.moveToNext();	 Catch:{ Exception -> 0x00da }
        if (r0 == 0) goto L_0x00c8;	 Catch:{ Exception -> 0x00da }
    L_0x0044:
        r0 = 0;	 Catch:{ Exception -> 0x00da }
        r13 = r8.getString(r0);	 Catch:{ Exception -> 0x00da }
        r0 = 1;	 Catch:{ Exception -> 0x00da }
        r6 = r8.getString(r0);	 Catch:{ Exception -> 0x00da }
        r0 = r11.getBucketID(r13);	 Catch:{ Exception -> 0x00da }
        r0 = java.lang.String.valueOf(r0);	 Catch:{ Exception -> 0x00da }
        r7.put(r0, r6);	 Catch:{ Exception -> 0x00da }
        r0 = "photoshareLogTag";	 Catch:{ Exception -> 0x00da }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00da }
        r1.<init>();	 Catch:{ Exception -> 0x00da }
        r2 = "innerStorage bucket id ";	 Catch:{ Exception -> 0x00da }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x00da }
        r2 = r11.getBucketID(r13);	 Catch:{ Exception -> 0x00da }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x00da }
        r2 = " albumID ";	 Catch:{ Exception -> 0x00da }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x00da }
        r1 = r1.append(r6);	 Catch:{ Exception -> 0x00da }
        r1 = r1.toString();	 Catch:{ Exception -> 0x00da }
        com.android.gallery3d.util.GalleryLog.d(r0, r1);	 Catch:{ Exception -> 0x00da }
        r12 = r14.getOuterGalleryStorageBucketIDsByArrayList(r13);	 Catch:{ Exception -> 0x00da }
        r10 = 0;	 Catch:{ Exception -> 0x00da }
    L_0x0087:
        r0 = r12.size();	 Catch:{ Exception -> 0x00da }
        if (r10 >= r0) goto L_0x003e;	 Catch:{ Exception -> 0x00da }
    L_0x008d:
        r0 = r12.get(r10);	 Catch:{ Exception -> 0x00da }
        r0 = java.lang.String.valueOf(r0);	 Catch:{ Exception -> 0x00da }
        r7.put(r0, r6);	 Catch:{ Exception -> 0x00da }
        r0 = "photoshareLogTag";	 Catch:{ Exception -> 0x00da }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00da }
        r1.<init>();	 Catch:{ Exception -> 0x00da }
        r2 = "OuterStorage bucket id ";	 Catch:{ Exception -> 0x00da }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x00da }
        r2 = r12.get(r10);	 Catch:{ Exception -> 0x00da }
        r2 = java.lang.String.valueOf(r2);	 Catch:{ Exception -> 0x00da }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x00da }
        r2 = " albumID ";	 Catch:{ Exception -> 0x00da }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x00da }
        r1 = r1.append(r6);	 Catch:{ Exception -> 0x00da }
        r1 = r1.toString();	 Catch:{ Exception -> 0x00da }
        com.android.gallery3d.util.GalleryLog.d(r0, r1);	 Catch:{ Exception -> 0x00da }
        r10 = r10 + 1;
        goto L_0x0087;
    L_0x00c8:
        com.android.gallery3d.common.Utils.closeSilently(r8);
    L_0x00cb:
        r1 = sAutoUploadAlbumBucketId;
        monitor-enter(r1);
        r0 = sAutoUploadAlbumBucketId;	 Catch:{ all -> 0x0102 }
        r0.clear();	 Catch:{ all -> 0x0102 }
        r0 = sAutoUploadAlbumBucketId;	 Catch:{ all -> 0x0102 }
        r0.putAll(r7);	 Catch:{ all -> 0x0102 }
        monitor-exit(r1);
        return;
    L_0x00da:
        r9 = move-exception;
        r0 = "photoshareLogTag";	 Catch:{ all -> 0x00fd }
        r1 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00fd }
        r1.<init>();	 Catch:{ all -> 0x00fd }
        r2 = "sAutoUploadAlbumBucketId SQLiteException  ";	 Catch:{ all -> 0x00fd }
        r1 = r1.append(r2);	 Catch:{ all -> 0x00fd }
        r2 = r9.toString();	 Catch:{ all -> 0x00fd }
        r1 = r1.append(r2);	 Catch:{ all -> 0x00fd }
        r1 = r1.toString();	 Catch:{ all -> 0x00fd }
        com.android.gallery3d.util.GalleryLog.d(r0, r1);	 Catch:{ all -> 0x00fd }
        com.android.gallery3d.common.Utils.closeSilently(r8);
        goto L_0x00cb;
    L_0x00fd:
        r0 = move-exception;
        com.android.gallery3d.common.Utils.closeSilently(r8);
        throw r0;
    L_0x0102:
        r0 = move-exception;
        monitor-exit(r1);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.gallery.photoshare.utils.PhotoShareUtils.initialAutoUploadAlbumBucketId():void");
    }

    public static synchronized void initialize(Context context) {
        boolean z = false;
        synchronized (PhotoShareUtils.class) {
            mHandler = new Handler(mAppContext.getMainLooper());
            getRealMetrics(mAppContext);
            clearNotification(mAppContext);
            initialCloudAlbumBucketId();
            initialAutoUploadAlbumBucketId();
            CloudTableOperateHelper.initCloudNameMaps();
            if (isGallerySupportPhotoShare() && isAppExist(context, "com.huawei.hidisk")) {
                z = isHiCloudSupportPhotoShare();
            }
            if (z) {
                initNameMap();
                if (sHandlerThread != null) {
                    sHandlerThread.quit();
                }
                sHandlerThread = new HandlerThread("callback_refresh");
                sHandlerThread.start();
                sRefreshHandler = new Handler(sHandlerThread.getLooper());
                GalleryLog.v("PhotoShareUtils", "sRefreshHandler initialized");
                sRefreshHandler.post(sRefreshChecker);
                mServiceConnection = new ServiceConnection() {
                    public void onServiceConnected(ComponentName arg0, IBinder arg1) {
                        PhotoShareUtils.mServer = ICloudAlbumService.Stub.asInterface(arg1);
                        if (PhotoShareUtils.sRefreshHandler != null) {
                            PhotoShareUtils.sRefreshHandler.post(PhotoShareUtils.sdoAfterConnected);
                        }
                    }

                    public void onServiceDisconnected(ComponentName arg0) {
                        GalleryLog.v("PhotoShareUtils", "onServiceDisconnected");
                        PhotoShareUtils.enableDownloadStatusBarNotification(false);
                        PhotoShareUtils.enableUploadStatusBarNotification(false);
                        PhotoShareUtils.clearNotification(PhotoShareUtils.mAppContext);
                        long t1 = System.currentTimeMillis();
                        PhotoShareUtils.bindPhotoShareService();
                        GalleryLog.i("PhotoShareUtils", "bindPhotoShareService duration=" + (System.currentTimeMillis() - t1));
                    }
                };
                bindPhotoShareService();
                sUserId = queryUserId();
                mInitializeFinished = true;
                if (!(mMethodRunOnce || mIsSupportPhotoShareFeature == mIsAPPInstalled)) {
                    GalleryLog.v("PhotoShareUtils", "mIsSupportPhotoShareFeature " + mIsSupportPhotoShareFeature + " mIsAPPInstalled " + mIsAPPInstalled);
                }
                GalleryLog.v("PhotoShareUtils", "Initialize finished");
                return;
            }
            mIsSupportPhotoShareFeature = false;
            mInitializeFinished = true;
            GalleryLog.v("PhotoShareUtils", "Initialize finished not support PhotoShare");
        }
    }

    private static void doAfterConnected() {
        try {
            boolean ret = mServer.registerCallback(mServiceCallback);
            hasNeverSynchronizedCloudData();
            mSdkAidlVersion = mServer.getAIDLVersion();
            GalleryLog.v("PhotoShareUtils", "SdkAidlVersion " + mSdkAidlVersion);
            GalleryLog.v("PhotoShareUtils", "GalleryAidlVersion 3");
            GalleryLog.v("PhotoShareUtils", "registerCallback result " + ret);
        } catch (RemoteException e) {
            dealRemoteException(e);
        }
        setSdkSettingsProp();
        checkCameraAlbumSwitch();
        setLogOnAccount(queryLogOnAccount());
        clearFVersionInThread();
        resetCloudPath();
        if (mFirstBind.booleanValue()) {
            GalleryLog.v("PhotoShareUtils", "onServiceConnected");
            registerReceiverForPhotoShare(mAppContext);
            mStatusBar = new PhotoShareStatusBar(mAppContext);
            if (getRunnable() != null) {
                GalleryLog.v("PhotoShareUtils", "getRunnable() is not null");
                mHandler.post(getRunnable());
                setRunnable(null);
            }
            mFirstBind = Boolean.valueOf(false);
        } else {
            GalleryLog.v("PhotoShareUtils", "PhotoShare apk killed and Re onServiceConnected");
        }
        notifyPhotoShareFolderChanged(3);
        updateNotify();
    }

    private static void clearFVersionInThread() {
        if (sRefreshHandler != null) {
            sRefreshHandler.post(sclearFVersion);
        }
    }

    private static void clearFVersion() {
        if (getLogOnAccount() != null && SharePreferenceUtils.getBooleanValue(mAppContext, "database_init", "database_init")) {
            GalleryLog.v("PhotoShareUtils", "need clearFVersion");
            try {
                if (getServer() != null) {
                    getServer().clearFVersion();
                    sHasNeverSynchronizedCloudData = true;
                }
            } catch (RemoteException e) {
                dealRemoteException(e);
            }
            SharePreferenceUtils.putBooleanValue(mAppContext, "database_init", "database_init", false);
        }
        refreshAlbum(7);
    }

    private static void makeSureDataBaseInitFalse() {
        if (SharePreferenceUtils.getBooleanValue(mAppContext, "database_init", "database_init")) {
            SharePreferenceUtils.putBooleanValue(mAppContext, "database_init", "database_init", false);
        }
    }

    private static void registerReceiverForPhotoShare(Context context) {
        BroadcastReceiver deleteIntentReceiver = new PhotoShareNotificationDeleteIntentReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.gallery.action.DOWNLOADNOTIFICATION_DELETE");
        filter.addAction("com.huawei.gallery.action.UPLOADNOTIFICATION_DELETE");
        filter.addAction("com.huawei.gallery.action.DO_NOTHING");
        context.registerReceiver(deleteIntentReceiver, filter, "com.huawei.gallery.permission.CLOUD_NOTIFICATION", null);
    }

    private static void initNameMap() {
        mThirdPartAppNameMap.put("default-album-101", Integer.valueOf(R.string.folder_qq_images));
        mThirdPartAppNameMap.put("default-album-102", Integer.valueOf(R.string.folder_qq_weixin));
        mThirdPartAppNameMap.put("default-album-103", Integer.valueOf(R.string.folder_sina_weibo_save));
    }

    public static int getResId(String name) {
        if ("default-album-1".equals(name)) {
            return R.string.folder_camera;
        }
        if ("default-album-2".equals(name)) {
            return R.string.folder_screenshot;
        }
        for (String key : mThirdPartAppNameMap.keySet()) {
            if (name.contains(key)) {
                return ((Integer) mThirdPartAppNameMap.get(key)).intValue();
            }
        }
        return 0;
    }

    private static String getShareName(String shareId, String folderName) {
        int resId = getResId(shareId);
        if (resId != 0) {
            return mAppContext.getResources().getString(resId);
        }
        return folderName;
    }

    private static void bindPhotoShareService() {
        Intent intent = new Intent("com.huawei.android.cg.CloudAlbumService");
        intent.setClassName("com.huawei.hidisk", "com.huawei.android.cg.CloudAlbumService");
        try {
            mIsSupportPhotoShareFeature = mAppContext.bindService(intent, mServiceConnection, 1);
        } catch (SecurityException e) {
            GalleryLog.v("PhotoShareUtils", e.toString());
            mIsSupportPhotoShareFeature = false;
        }
        GalleryLog.v("PhotoShareUtils", "bindService result is " + mIsSupportPhotoShareFeature);
    }

    public static void setRunnable(Runnable runnable) {
        mRunnable = runnable;
    }

    private static Runnable getRunnable() {
        return mRunnable;
    }

    private static void getRealMetrics(Context context) {
        if (mHeightPixels == 0 || mWidthPixels == 0) {
            DisplayMetrics metrics = new DisplayMetrics();
            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealMetrics(metrics);
            if (metrics.heightPixels > metrics.widthPixels) {
                mHeightPixels = metrics.heightPixels;
                mWidthPixels = metrics.widthPixels;
            } else {
                mHeightPixels = metrics.widthPixels;
                mWidthPixels = metrics.heightPixels;
            }
            SCROLL_VIEW_HEIGHT_MAX = (int) mAppContext.getResources().getDimension(R.dimen.photoshare_scrollview_maxhight);
        }
    }

    public static int getScrollViewMaxHeight() {
        return SCROLL_VIEW_HEIGHT_MAX;
    }

    public static void showSoftInput(EditText editText) {
        if (editText != null && editText.getContext() != null) {
            ((InputMethodManager) editText.getContext().getSystemService("input_method")).showSoftInput(editText, 0);
        }
    }

    public static void hideSoftInput(EditText editText) {
        if (editText != null && editText.getContext() != null) {
            ((InputMethodManager) editText.getContext().getSystemService("input_method")).hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    public static int getNotifyId(String sharePath) {
        if (TextUtils.isEmpty(sharePath)) {
            return -1;
        }
        int id = 0;
        for (char c : sharePath.toCharArray()) {
            id += c;
        }
        return id;
    }

    public static boolean isHiCloudLoginAndCloudPhotoSwitchOpen() {
        return isHiCloudLogin() ? isCloudPhotoSwitchOpen() : false;
    }

    private static boolean querySwitchStatus(int position) {
        Closeable closeable = null;
        try {
            closeable = mAppContext.getContentResolver().query(SWITCH_STATUS_URI, null, null, null, null);
            if (closeable == null || !closeable.moveToPosition(position)) {
                Utils.closeSilently(closeable);
                return false;
            }
            String statusString = closeable.getString(0);
            if (TextUtils.isEmpty(statusString)) {
                return false;
            }
            boolean equalsIgnoreCase = "1".equalsIgnoreCase(statusString);
            Utils.closeSilently(closeable);
            return equalsIgnoreCase;
        } catch (Exception e) {
            GalleryLog.v("PhotoShareUtils", "position " + position + " querySwitchStatus Exception " + e.toString());
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public static boolean isCloudPhotoSwitchOpen() {
        boolean cloudPhotoSwitchStatus = querySwitchStatus(0);
        updateLocalSwitch(cloudPhotoSwitchStatus);
        return cloudPhotoSwitchStatus;
    }

    public static boolean isShareSwitchOpen() {
        return querySwitchStatus(1);
    }

    public static boolean hasNeverSynchronizedCloudData() {
        boolean z = true;
        if (getServer() == null) {
            return true;
        }
        Object fVersion = null;
        try {
            fVersion = getServer().getFVersion();
        } catch (RemoteException e) {
            dealRemoteException(e);
        }
        if (!TextUtils.isEmpty(fVersion)) {
            z = "-1".equalsIgnoreCase(fVersion);
        }
        sHasNeverSynchronizedCloudData = z;
        return sHasNeverSynchronizedCloudData;
    }

    public static boolean isClassifySwitchOpen() {
        return querySwitchStatus(2);
    }

    public static boolean isFilePathValid(String path) {
        long fileSize = 0;
        try {
            File file = new File(path);
            if (!file.exists()) {
                return false;
            }
            fileSize = file.length();
            if (fileSize > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            GalleryLog.d("PhotoShareUtils", "Error In isFilePathValid " + e.toString());
        }
    }

    public static boolean isAppExist(Context context, String appName) {
        boolean result = false;
        try {
            result = context.getPackageManager().getApplicationInfo(appName, FragmentTransaction.TRANSIT_EXIT_MASK).enabled;
        } catch (NameNotFoundException e) {
            GalleryLog.v("PhotoShareUtils", appName + " is not exist!");
        }
        GalleryLog.v("PhotoShareUtils", appName + " result = " + result);
        return result;
    }

    private static boolean isHiCloudInstalled() {
        if (mMethodRunOnce) {
            mIsAPPInstalled = isAppExist(mAppContext, "com.huawei.hidisk");
            mMethodRunOnce = false;
        }
        return mIsAPPInstalled;
    }

    public static boolean isSupportPhotoShare() {
        if (mInitializeFinished) {
            return mIsSupportPhotoShareFeature;
        }
        boolean isHiCloudSupportPhotoShare = (isHiCloudInstalled() && isGallerySupportPhotoShare()) ? isHiCloudSupportPhotoShare() : false;
        return isHiCloudSupportPhotoShare;
    }

    public static boolean isHiCloudSupportPhotoShare() {
        return new Intent("com.huawei.hicloud.action.EXTERNAL_LOGIN").resolveActivity(mAppContext.getPackageManager()) != null;
    }

    public static boolean isGallerySupportPhotoShare() {
        return true;
    }

    public static boolean isShareNameValid(Context context, String shareName) {
        if (shareName == null || shareName.length() == 0) {
            ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_name_no_content_Toast, 0);
            return false;
        } else if (85 <= shareName.length()) {
            ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_name_reached_max_length_Toast, 0);
            return false;
        } else if (Pattern.matches("^[^\\\\/:*?<>\"|\\[\\]\\{\\}]+$", shareName)) {
            return true;
        } else {
            ContextedUtils.showToastQuickly(context, (int) R.string.photoshare_name_invalid, 0);
            return false;
        }
    }

    public static void cacheShareItemList(ArrayList<String> list) {
        clearShareItemList();
        if (list != null) {
            mShareList.addAll(list);
        }
    }

    public static void cacheShareItem(String path) {
        clearShareItemList();
        if (!path.isEmpty()) {
            mShareList.add(path);
        }
    }

    public static ArrayList<String> getShareItemList() {
        return (ArrayList) mShareList.clone();
    }

    public static void clearShareItemList() {
        if (!mShareList.isEmpty()) {
            mShareList.clear();
        }
    }

    public static void addPhotoToShared(final String shareId) {
        new Thread() {
            public void run() {
                PhotoShareUtils.doAddPhotoToShared(shareId);
            }
        }.start();
    }

    private static void doAddPhotoToShared(String shareId) {
        CharSequence charSequence = null;
        try {
            ArrayList<String> shareList = new ArrayList();
            shareList.addAll(getShareItemList());
            if (shareList.size() != 0) {
                ArrayList<String> fileNeedToAdd = checkMd5ExistsInShare(shareId, shareList);
                if (shareList.size() > fileNeedToAdd.size()) {
                    showFileExitsTips(shareList.size() - fileNeedToAdd.size());
                }
                if (!fileNeedToAdd.isEmpty()) {
                    int result = mServer.addFileToShare(shareId, (String[]) fileNeedToAdd.toArray(new String[fileNeedToAdd.size()]));
                    GalleryLog.v("PhotoShareUtils", "addFileToShare result " + result);
                    if (result != 0) {
                        charSequence = mAppContext.getResources().getString(R.string.share_to_cloudAlbum_failed);
                    } else {
                        enableUploadStatusBarNotification(true);
                        refreshStatusBar(false);
                    }
                }
                clearShareItemList();
            }
        } catch (RemoteException e) {
            charSequence = mAppContext.getResources().getString(R.string.share_to_cloudAlbum_failed);
            dealRemoteException(e);
        }
        if (charSequence != null) {
            ContextedUtils.showToastQuickly(mAppContext, charSequence, 0);
        }
    }

    public static void clearNotification(Context context) {
        try {
            ((NotificationManager) context.getSystemService("notification")).cancelAll();
        } catch (SecurityException e) {
            GalleryLog.v("PhotoShareUtils", "cancelAll SecurityException");
        }
    }

    public static long getFileSize(String fileName) {
        Exception e;
        Throwable th;
        Closeable closeable = null;
        try {
            Closeable fis = new FileInputStream(new File(fileName));
            try {
                long fileSize = (long) fis.available();
                Utils.closeSilently(fis);
                closeable = fis;
                return fileSize;
            } catch (Exception e2) {
                e = e2;
                closeable = fis;
                try {
                    GalleryLog.w("PhotoShareUtils", "Exception In getFileSize " + e.toString());
                    Utils.closeSilently(closeable);
                    return 0;
                } catch (Throwable th2) {
                    th = th2;
                    Utils.closeSilently(closeable);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                closeable = fis;
                Utils.closeSilently(closeable);
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            GalleryLog.w("PhotoShareUtils", "Exception In getFileSize " + e.toString());
            Utils.closeSilently(closeable);
            return 0;
        }
    }

    public static ICloudAlbumService getServer() {
        if (mServer == null) {
            GalleryLog.v("PhotoShareUtils", "mServer is NULL");
        }
        return mServer;
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            NetworkInfo mNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static boolean checkCharValid(String input, Context ctx) {
        byte[] bs = input.getBytes(Charset.defaultCharset());
        for (byte b : bs) {
            if ((b & 248) == 240) {
                ContextedUtils.showToastQuickly(ctx, (int) R.string.photoshare_name_invalid_new, 0);
                return false;
            }
        }
        return true;
    }

    public static AlertDialog getPhotoShareDialog(Context context, int titleId, int positiveId, int negativeId, String content, OnClickListener clickListener) {
        return getPhotoShareDialog(context, context.getString(titleId), positiveId, negativeId, content, clickListener);
    }

    public static AlertDialog getPhotoShareDialog(Context context, String title, int positiveId, int negativeId, String content, OnClickListener clickListener) {
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle(title).create();
        dialog.setMessage(content);
        if (negativeId != -1) {
            dialog.setButton(-2, context.getString(negativeId), clickListener);
        }
        dialog.setButton(-1, context.getString(positiveId), clickListener);
        return dialog;
    }

    public static boolean isFileExists(String fileName) {
        return !TextUtils.isEmpty(fileName) ? new File(fileName).exists() : false;
    }

    public static ShareInfo getShareInfo(String name) {
        ShareInfo newShare = null;
        try {
            List<ShareInfo> list = getServer().getShareList(1);
            if (list != null && list.size() > 0) {
                for (ShareInfo share : list) {
                    if (name.equals(share.getShareName())) {
                        newShare = share;
                        break;
                    }
                }
            }
            return newShare;
        } catch (RemoteException e) {
            dealRemoteException(e);
            return null;
        } catch (Throwable th) {
            return null;
        }
    }

    public static ArrayList<String> getFilePathFromPathString(GalleryContext context, ArrayList<String> list) {
        ArrayList<Path> filePaths = new ArrayList();
        if (list != null) {
            for (String item : list) {
                filePaths.add(Path.fromString(item));
            }
        }
        return getFilePathsFromPathReturnList(context, filePaths);
    }

    public static ArrayList<String> getFilePathsFromPathReturnList(GalleryContext context, ArrayList<Path> list) {
        ArrayList<String> filePaths = new ArrayList();
        if (list != null) {
            for (Path item : list) {
                MediaItem mediaItem = (MediaItem) context.getDataManager().getMediaObject(item);
                if (mediaItem != null) {
                    String filePath = mediaItem.getFilePath();
                    if (filePath != null) {
                        filePaths.add(filePath);
                    }
                }
            }
        }
        return filePaths;
    }

    public static ArrayList<String> getFilePathsFromPath(GalleryContext context, ArrayList<Path> list) {
        return getFilePathsFromPathReturnList(context, list);
    }

    public static ArrayList<String> checkMd5ExistsInShare(String shareId, ArrayList<String> filePath) {
        if (filePath.isEmpty()) {
            return filePath;
        }
        ArrayList<String> hashExists = new ArrayList();
        hashExists.addAll(getMd5(shareId, 1));
        hashExists.addAll(getMd5(shareId, 2));
        ArrayList<String> result = new ArrayList();
        for (int i = 0; i < filePath.size(); i++) {
            String hash = MD5Utils.getMD5(new File((String) filePath.get(i)));
            if (!hashExists.contains(hash)) {
                result.add((String) filePath.get(i));
                hashExists.add(hash);
            }
        }
        return result;
    }

    public static ArrayList<String> checkMd5ExistsWhenCreateNewShare(ArrayList<String> filePath) {
        if (filePath.isEmpty()) {
            return filePath;
        }
        ArrayList<String> result = new ArrayList();
        ArrayList<String> hashExists = new ArrayList();
        for (int i = 0; i < filePath.size(); i++) {
            String hash = MD5Utils.getMD5(new File((String) filePath.get(i)));
            if (!hashExists.contains(hash)) {
                result.add((String) filePath.get(i));
                hashExists.add(hash);
            }
        }
        return result;
    }

    private static ArrayList<String> getMd5(String shareId, int type) {
        int shareFileCount;
        ArrayList<String> hashExists = new ArrayList();
        if (1 == type) {
            try {
                shareFileCount = getServer().getShareFileInfoListLimitCount(shareId, 0);
            } catch (RemoteException e) {
                dealRemoteException(e);
            }
        } else {
            shareFileCount = getServer().getSharePreFileInfoListLimitCount(shareId, 0);
        }
        int group = shareFileCount / 100;
        for (int i = 0; i < group; i++) {
            List<FileInfo> fileLists;
            int j;
            if (1 == type) {
                fileLists = getServer().getShareFileInfoListLimit(shareId, 0, i * 100, 100);
            } else {
                fileLists = getServer().getSharePreFileInfoListLimit(shareId, 0, i * 100, 100);
            }
            if (fileLists != null) {
                for (j = 0; j < fileLists.size(); j++) {
                    hashExists.add(((FileInfo) fileLists.get(j)).getHash());
                }
            }
        }
        int left = shareFileCount % 100;
        if (left != 0) {
            if (1 == type) {
                fileLists = getServer().getShareFileInfoListLimit(shareId, 0, group * 100, left);
            } else {
                fileLists = getServer().getSharePreFileInfoListLimit(shareId, 0, group * 100, left);
            }
            if (fileLists != null) {
                for (j = 0; j < fileLists.size(); j++) {
                    hashExists.add(((FileInfo) fileLists.get(j)).getHash());
                }
            }
        }
        return hashExists;
    }

    public static ArrayList<FileInfo> getFileInfo(String shareId, int type, MediaSet set) {
        int fileCount;
        int i;
        List<FileInfo> tempList;
        ArrayList<FileInfo> fileLists = new ArrayList();
        ArrayList<FileInfo> result = new ArrayList();
        if (1 == type || 10 == type) {
            try {
                fileCount = set.getMediaItemCount();
            } catch (RemoteException e) {
                dealRemoteException(e);
            }
        } else {
            fileCount = getServer().getShareFileInfoListLimitCount(shareId, 0);
        }
        int group = fileCount / 100;
        for (i = 0; i < group; i++) {
            if (1 == type || 10 == type) {
                tempList = getCloudLocalFileInfo(set, i * 100, 100);
            } else {
                tempList = getServer().getShareFileInfoListLimit(shareId, 0, i * 100, 100);
            }
            if (tempList != null) {
                fileLists.addAll(tempList);
            }
        }
        int left = fileCount % 100;
        if (left != 0) {
            if (1 == type || 10 == type) {
                tempList = getCloudLocalFileInfo(set, group * 100, left);
            } else {
                tempList = getServer().getShareFileInfoListLimit(shareId, 0, group * 100, left);
            }
            if (tempList != null) {
                fileLists.addAll(tempList);
            }
        }
        int size = fileLists.size();
        for (i = 0; i < size; i++) {
            FileInfo fileInfoTemp = (FileInfo) fileLists.get(i);
            if (PhotoShareAlbum.getLocalRealPath(set.getName(), fileInfoTemp) == null) {
                result.add(fileInfoTemp);
            }
        }
        return result;
    }

    private static List<FileInfo> getCloudLocalFileInfo(MediaSet set, int start, int count) {
        ArrayList<FileInfo> result = new ArrayList();
        ArrayList<MediaItem> items = set.getMediaItem(start, count);
        for (int i = 0; i < items.size(); i++) {
            MediaItem mediaItem = (MediaItem) items.get(i);
            if (mediaItem.getFileInfo() != null) {
                result.add(mediaItem.getFileInfo());
            }
        }
        return result;
    }

    public static int addDownLoadTask(ArrayList<FileInfo> fileInfoArrayList, int type) {
        int result = 0;
        if (fileInfoArrayList == null || fileInfoArrayList.size() == 0) {
            return 0;
        }
        try {
            int fileCount = fileInfoArrayList.size();
            int group = fileCount / 100;
            for (int i = 0; i < group; i++) {
                if (1 == type || 10 == type) {
                    result += getServer().downloadPhotoThumb((FileInfo[]) fileInfoArrayList.subList(i * 100, (i + 1) * 100).toArray(new FileInfo[100]), 0, 0, false);
                } else {
                    result += getServer().downloadSharePhotoThumb((FileInfo[]) fileInfoArrayList.subList(i * 100, (i + 1) * 100).toArray(new FileInfo[100]), 0, 0, false);
                }
            }
            int left = fileCount % 100;
            if (left != 0) {
                if (1 == type || 10 == type) {
                    result += getServer().downloadPhotoThumb((FileInfo[]) fileInfoArrayList.subList(fileCount - left, fileCount).toArray(new FileInfo[left]), 0, 0, false);
                } else {
                    result += getServer().downloadSharePhotoThumb((FileInfo[]) fileInfoArrayList.subList(fileCount - left, fileCount).toArray(new FileInfo[left]), 0, 0, false);
                }
            }
        } catch (RemoteException e) {
            dealRemoteException(e);
        }
        return result;
    }

    public static void goToCreatedShare(String shareName, Context context) {
        ShareInfo shareToAdd = getShareInfo(shareName);
        if (shareToAdd != null) {
            Bundle data = new Bundle();
            Intent intent = new Intent();
            data.putString("media-path", "/photoshare/all/share/preview/*".replace("*", shareToAdd.getShareId()));
            data.putBoolean("only-local-camera-video-album", false);
            intent.setClass(context, PhotoShareTimeBucketActivity.class);
            intent.putExtras(data);
            context.startActivity(intent);
        }
    }

    public static long getFileSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size += getFileSize(fileList[i]);
                } else {
                    size += fileList[i].length();
                }
            }
        } catch (Exception e) {
            GalleryLog.v("PhotoShareUtils", "getFileSize Exception " + e.toString());
        }
        return size;
    }

    public static void showFileExitsTips(final int count) {
        mHandler.post(new Runnable() {
            public void run() {
                ContextedUtils.showToastQuickly(PhotoShareUtils.mAppContext, PhotoShareUtils.mAppContext.getResources().getQuantityString(R.plurals.photoshare_toast_file_exist, count, new Object[]{Integer.valueOf(count)}), 0);
            }
        });
    }

    public static void showSameFileTips(final int count) {
        mHandler.post(new Runnable() {
            public void run() {
                ContextedUtils.showToastQuickly(PhotoShareUtils.mAppContext, PhotoShareUtils.mAppContext.getResources().getQuantityString(R.plurals.photoshare_toast_same_file, count, new Object[]{Integer.valueOf(count)}), 0);
            }
        });
    }

    public static void storeFilePath(Activity activity) {
        Intent intent = activity.getIntent();
        Bundle extras = intent.getExtras();
        Uri uri;
        String path;
        if ("android.intent.action.SEND".equals(intent.getAction())) {
            if (extras != null && extras.containsKey("android.intent.extra.STREAM")) {
                uri = (Uri) extras.getParcelable("android.intent.extra.STREAM");
                if (uri == null) {
                    GalleryLog.v("PhotoShareUtils", "Uri is null");
                    return;
                }
                path = GalleryUtils.convertUriToPath(activity, uri);
                if (path != null) {
                    cacheShareItem(path);
                }
            }
        } else if ("android.intent.action.SEND_MULTIPLE".equals(intent.getAction()) && extras.containsKey("android.intent.extra.STREAM")) {
            ArrayList<Parcelable> mList = extras.getParcelableArrayList("android.intent.extra.STREAM");
            ArrayList<String> paths = new ArrayList();
            if (mList != null) {
                for (Parcelable pa : mList) {
                    uri = (Uri) pa;
                    if (uri == null) {
                        GalleryLog.v("PhotoShareUtils", "Uri is null");
                    } else {
                        path = GalleryUtils.convertUriToPath(activity, uri);
                        if (path != null) {
                            paths.add(path);
                        }
                    }
                }
                if (!paths.isEmpty()) {
                    cacheShareItemList(paths);
                }
            }
        }
    }

    public static String getValueFromJson(String expand, String key) {
        if (TextUtils.isEmpty(expand)) {
            return null;
        }
        try {
            JSONObject expandJson = new JSONObject(expand);
            if (expandJson.has(key)) {
                return expandJson.getString(key);
            }
        } catch (JSONException e) {
            GalleryLog.i("PhotoShareUtils", "A JSONException has occurred." + e.getMessage());
        }
        return null;
    }

    public static String getLoginUserId() {
        AccountInfo accountInfo = getLogOnAccount();
        if (accountInfo != null) {
            return accountInfo.getUserId();
        }
        return null;
    }

    public static void openAccountCenter(Context context) {
        if (context != null) {
            Intent intent = new Intent();
            intent.setAction("com.huawei.hwid.ACTION_MAIN_SETTINGS");
            intent.setPackage("com.huawei.hwid");
            intent.putExtra(TMSDKContext.CON_CHANNEL, HicloudAccountManager.CHANNEL);
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                GalleryLog.d("PhotoShareUtils", "jump to account center fail");
            }
        }
    }

    public static boolean isNetAllowed(Context context) {
        return true;
    }

    public static boolean isMobileNetConnected(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable() && networkInfo.getType() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isWifiConnected(Context context) {
        NetworkInfo wifiNetInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getNetworkInfo(1);
        if (wifiNetInfo == null || !wifiNetInfo.isConnected()) {
            return false;
        }
        return true;
    }

    public static void registerDownUp(PhotoShareDownUpNotifier notifier) {
        mDownUpNotifier.registerNotifier(notifier);
    }

    public static void refreshStatusBar(boolean isDownLoad) {
        if (mStatusBar != null) {
            if ((isDownLoad && mCanUpdateDownloadStatusBarState) || (!isDownLoad && mCanUpdateUploadStatusBarState)) {
                mStatusBar.updateNotification(isDownLoad);
            }
        }
    }

    public static void enableDownloadStatusBarNotification(boolean enabled) {
        mCanUpdateDownloadStatusBarState = enabled;
    }

    public static void enableUploadStatusBarNotification(boolean enabled) {
        mCanUpdateUploadStatusBarState = enabled;
    }

    public static int getRotationFromExif(String filePath) {
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(filePath);
        } catch (IOException ex) {
            GalleryLog.v("PhotoShareUtils", "getRotationFromExif Exception " + ex);
        }
        if (exifInterface != null) {
            return getOrientation(exifInterface.getAttributeInt("Orientation", -1));
        }
        return 0;
    }

    public static int getOrientation(int exifOrientation) {
        switch (exifOrientation) {
            case 1:
                return 0;
            case 3:
                return 180;
            case 6:
                return 90;
            case 8:
                return 270;
            default:
                GalleryLog.w("PhotoShareUtils", "getOrientation invalid " + exifOrientation);
                return 0;
        }
    }

    public static ArrayList<String> getAllTagName() {
        ArrayList<TagInfo> tagInfoList = new ArrayList();
        ArrayList<String> tagNameList = new ArrayList();
        try {
            List<TagInfo> temp;
            int count = getServer().getTagInfoListCount("0");
            int start = 0;
            int group = count / SmsCheckResult.ESCT_200;
            for (int i = 0; i < group; i++) {
                temp = getServer().getTagInfoListLimit("0", start, SmsCheckResult.ESCT_200);
                if (!(temp == null || temp.isEmpty())) {
                    tagInfoList.addAll(temp);
                }
                start += SmsCheckResult.ESCT_200;
            }
            int left = count % SmsCheckResult.ESCT_200;
            if (left != 0) {
                temp = getServer().getTagInfoListLimit("0", start, left);
                if (!(temp == null || temp.isEmpty())) {
                    tagInfoList.addAll(temp);
                }
            }
        } catch (RemoteException e) {
            dealRemoteException(e);
        }
        for (TagInfo info : tagInfoList) {
            String tagName = info.getTagName();
            if (!(TextUtils.isEmpty(tagName) || tagNameList.contains(tagName))) {
                tagNameList.add(tagName);
            }
        }
        return tagNameList;
    }

    public static void showDeleteAlertDialog(Context context, int sourceType, OnClickListener listener, int selectedCount, MediaSet mediaSet, boolean isPhotoPage, boolean isSyncedAlbum, boolean isHicloudAlbum) {
        ActionDeleteAndConfirm actionDeleteAndConfirm;
        String message = null;
        if (sourceType == 18 || sourceType == 17) {
            message = context.getResources().getQuantityString(R.plurals.photoshare_tagfile_delete_msg, selectedCount, new Object[]{Integer.valueOf(selectedCount)});
        }
        int albumType = mediaSet.getAlbumType();
        Resources res = context.getResources();
        if (!RecycleUtils.supportRecycle()) {
            actionDeleteAndConfirm = new ActionDeleteAndConfirm(context, message, DeleteMsgUtil.getDeleteTitle(res, mediaSet, selectedCount, isPhotoPage, isHicloudAlbum, isSyncedAlbum));
        } else if (albumType == 2 || albumType == 7) {
            if (isPhotoPage) {
                title = res.getString(R.string.dialog_deleteContentFromSharedAlbumPhotoPage);
            } else {
                title = res.getString(R.string.dialog_deletecontentfromsharedalbum);
            }
            actionDeleteAndConfirm = new ActionDeleteAndConfirm(context, message, title);
        } else {
            boolean isCloudOperateTogether = isHiCloudLoginAndCloudPhotoSwitchOpen();
            if (isPhotoPage) {
                if (isCloudOperateTogether) {
                    title = res.getQuantityString(R.plurals.delete_synced_photo_msg, 1, new Object[]{Integer.valueOf(30)});
                } else {
                    title = res.getQuantityString(R.plurals.delete_local_photo_msg, 1, new Object[]{Integer.valueOf(30)});
                }
            } else if (isCloudOperateTogether) {
                title = res.getQuantityString(R.plurals.delete_synced_photo_msg, 2, new Object[]{Integer.valueOf(30)});
            } else {
                title = res.getQuantityString(R.plurals.delete_local_photo_msg, 2, new Object[]{Integer.valueOf(30)});
            }
            actionDeleteAndConfirm = new ActionRecycleAndConfirm(context, message, title);
        }
        actionDeleteAndConfirm.setOnClickListener(listener);
        actionDeleteAndConfirm.updateStatus(isSyncedAlbum, isHicloudAlbum);
        actionDeleteAndConfirm.show();
    }

    public static void showDeleteTagFileAlertDialog(Context context, OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setPositiveButton(R.string.photoshare_move_classify, listener).setNegativeButton(R.string.cancel, listener);
        builder.setMessage(context.getString(R.string.photoshare_move_tag_tips_shorter));
        GalleryUtils.setTextColor(builder.show().getButton(-1), context.getResources());
    }

    public static int getCoverWidth() {
        if (COVER_WIDTH == 0) {
            COVER_WIDTH = (int) mAppContext.getResources().getDimension(R.dimen.photoshare_category_coverImage_width);
        }
        return COVER_WIDTH;
    }

    public static int getCoverHeight() {
        if (COVER_HEIGHT == 0) {
            COVER_HEIGHT = (int) mAppContext.getResources().getDimension(R.dimen.photoshare_category_coverImage_height);
        }
        return COVER_HEIGHT;
    }

    public static void dealRemoteException(RemoteException e) {
        if (e instanceof DeadObjectException) {
            long now = System.currentTimeMillis();
            long interval = now - sLastBindServiceTime;
            if (interval >= 5000 || interval <= 0) {
                sLastBindServiceTime = now;
                bindPhotoShareService();
            } else {
                GalleryLog.v("PhotoShareUtils", "BIND TOO OFTEN");
            }
        }
        GalleryLog.i("PhotoShareUtils", "A RemoteException has occurred." + e.getMessage());
    }

    public static void startServiceByImplicitIntent(Context context, Intent implicitIntent) {
        List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentServices(implicitIntent, 0);
        if (resolveInfo != null && resolveInfo.size() == 1) {
            ResolveInfo serviceInfo = (ResolveInfo) resolveInfo.get(0);
            ComponentName component = new ComponentName(serviceInfo.serviceInfo.packageName, serviceInfo.serviceInfo.name);
            Intent explicitIntent = new Intent(implicitIntent);
            explicitIntent.setComponent(component);
            context.startService(explicitIntent);
        }
    }

    public static void login(Context context) {
        if (context != null) {
            try {
                context.startActivity(new Intent("com.huawei.hicloud.action.GALLERY_LOGIN"));
            } catch (ActivityNotFoundException e) {
                GalleryLog.e("photoshareLogTag", "com.huawei.hicloud.action.GALLERY_LOGIN can not find Activity");
            }
        }
    }

    public static void refreshAlbum(final int flag) {
        new Thread() {
            public void run() {
                if (PhotoShareUtils.getServer() != null && PhotoShareUtils.getLogOnAccount() != null) {
                    try {
                        if ((flag & 2) != 0) {
                            PhotoShareUtils.getServer().refreshGeneralAlbum();
                        }
                        if ((flag & 1) != 0) {
                            PhotoShareUtils.getServer().refreshShare();
                        }
                        if ((flag & 4) != 0) {
                            PhotoShareUtils.getServer().refreshTag();
                        }
                    } catch (RemoteException e) {
                        PhotoShareUtils.dealRemoteException(e);
                    }
                }
            }
        }.start();
    }

    public static void startOpenNetService(Context context) {
        Intent intent = new Intent("com.huawei.android.ds.OPENSWITCH");
        intent.putExtra("startSource", "gallery");
        intent.putExtra("switchName", new String[]{"cloudAlbumNet"});
        startServiceByImplicitIntent(context, intent);
    }

    public static int parsePanorama3dSizeFromExpand(String expand) {
        int size = 0;
        if (TextUtils.isEmpty(expand)) {
            return 0;
        }
        try {
            JSONObject expandJson = new JSONObject(expand);
            if (expandJson.has("attach")) {
                String jsonString = expandJson.getString("attach");
                if (!TextUtils.isEmpty(jsonString)) {
                    String jsonString2 = new JSONArray(jsonString).getString(0);
                    if (!TextUtils.isEmpty(jsonString2)) {
                        JSONObject attach = new JSONObject(jsonString2);
                        if (attach.has("size")) {
                            size = attach.getInt("size");
                        }
                        GalleryLog.v("PhotoShareUtils", "mPanorama3dDataSize " + size);
                    }
                }
            }
        } catch (JSONException e) {
            GalleryLog.e("PhotoShareUtils", "initPanorama3dDataSize,e " + e);
        }
        return size;
    }

    public static boolean isSupportShareToCloud() {
        return isSupportPhotoShare() && !TextUtils.isEmpty(getLoginUserId()) && isShareSwitchOpen() && !isCloudNormandyVersion();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String queryUserId() {
        String str = null;
        try {
            Closeable cursor = mAppContext.getContentResolver().query(QUERY_UID_URI, null, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                str = cursor.getString(0);
            }
            Utils.closeSilently(cursor);
        } catch (Exception e) {
            GalleryLog.v("PhotoShareUtils", "getUserId Exception " + e.toString());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return str;
    }

    public static String getUserId() {
        if (sUserId == null && !mInitializeFinished) {
            sUserId = queryUserId();
        }
        return sUserId;
    }

    public static boolean isFversionEmpty() {
        Closeable closeable = null;
        String str = null;
        try {
            closeable = mAppContext.getContentResolver().query(QUERY_FVERSION_URI, null, null, null, null);
            if (closeable == null || !closeable.moveToNext()) {
                Utils.closeSilently(closeable);
                return false;
            }
            str = closeable.getString(0);
            return TextUtils.isEmpty(str);
        } catch (Exception e) {
            GalleryLog.v("PhotoShareUtils", "getFversion Exception " + e.toString());
            return false;
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public static String getDeletedFileIdentify(String hash, String relativePath) {
        return hash + "-" + relativePath;
    }

    public static boolean shouldShowCloudSwitch(Context context) {
        Long lastTime = Long.valueOf(context.getSharedPreferences("cloud_switch", 0).getLong("last-click-time", Long.MAX_VALUE));
        if (lastTime.longValue() == Long.MAX_VALUE) {
            return false;
        }
        boolean isCloudPhotoSwitchOpen = isCloudPhotoSwitchOpen();
        if (isCloudPhotoSwitchOpen) {
            saveClickTime(context, false);
            return false;
        }
        boolean timeLongEnough = true;
        GalleryLog.d("PhotoShareUtils", "Last save time: " + lastTime);
        if (lastTime.longValue() > 0) {
            Calendar now = Calendar.getInstance();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(lastTime.longValue());
            calendar.add(2, 1);
            timeLongEnough = calendar.before(now);
        }
        if (!timeLongEnough) {
            return false;
        }
        boolean isSupportPhotoShare = isSupportPhotoShare();
        boolean isHiCloudLogin = isHiCloudLogin();
        GalleryLog.d("PhotoShareUtils", "isSupportPhotoShare:" + isSupportPhotoShare + ", isHiCloudLogin:" + isHiCloudLogin + ", isCloudPhotoSwitchOpen:" + isCloudPhotoSwitchOpen);
        if (!isSupportPhotoShare) {
            isHiCloudLogin = false;
        }
        return isHiCloudLogin;
    }

    public static void saveClickTime(Context context, boolean showAfterMonth) {
        context.getSharedPreferences("cloud_switch", 0).edit().putLong("last-click-time", showAfterMonth ? System.currentTimeMillis() : Long.MAX_VALUE).commit();
    }

    public static void notifyCloudUpload() {
        if (isCloudNormandyVersion()) {
            Context context = mAppContext;
            Intent intent = new Intent("com.huawei.photoShare.action.UPLOAD_PASSIVELY");
            intent.setClass(context, UploadService.class);
            context.startService(intent);
        }
    }

    public static boolean isCloudNormandyVersion() {
        if (IS_CLOUD_NORMANDY_VERSION == -1) {
            IS_CLOUD_NORMANDY_VERSION = checkUriValueSupport(CLOUD_VERIOSN_URI);
        }
        if (IS_CLOUD_NORMANDY_VERSION == 1) {
            return true;
        }
        return false;
    }

    public static boolean isCloudRecycleAlbumSupport() {
        if (IS_CLOUD_RECYCLE_SUPPORT == -1) {
            IS_CLOUD_RECYCLE_SUPPORT = checkUriValueSupport(CLOUD_RECYCLE_ALBUM_VERSION_URI);
        }
        if (IS_CLOUD_RECYCLE_SUPPORT == 1) {
            return true;
        }
        return false;
    }

    public static boolean isGUIDSupport() {
        if (IS_CLOUD_GUID_SUPPORT == -1) {
            IS_CLOUD_GUID_SUPPORT = checkUriValueSupport(CLOUD_GUID_VERSION_URI);
        }
        if (IS_CLOUD_GUID_SUPPORT == 1) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int checkUriValueSupport(Uri uri) {
        int cloudVersion = -1;
        try {
            Closeable cursor = mAppContext.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                cloudVersion = cursor.getInt(cursor.getColumnIndex("switchStatus"));
            }
            Utils.closeSilently(cursor);
        } catch (Exception e) {
            GalleryLog.v("PhotoShareUtils", "query uri Exception " + e.toString());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        GalleryLog.d("PhotoShareUtils", "check uri value " + uri + " result: " + cloudVersion);
        if (cloudVersion == 0) {
            return 1;
        }
        return 0;
    }

    public static void notifyCloudUpload(String albumId, String absPath, String bucketRelativePath, String hash, int mediaType) {
        if (isCloudNormandyVersion()) {
            Intent intent = new Intent("com.huawei.photoShare.action.UPLOAD_FORWARDLY");
            intent.putExtra("albumId", albumId);
            intent.putExtra("absPath", absPath);
            intent.putExtra("relative-bucketPath", bucketRelativePath);
            intent.putExtra("hash", hash);
            intent.putExtra("media-type", mediaType);
            intent.setClass(mAppContext, UploadService.class);
            mAppContext.startService(intent);
            return;
        }
        try {
            getServer().uploadGeneralFile(albumId, absPath, bucketRelativePath);
        } catch (RemoteException e) {
            dealRemoteException(e);
        }
    }

    public static void checkAutoUpload(int cloudId, String data, String bucketId, String bucketRelativePath, String hash, int mediaType, boolean isSizeZero) {
        String albumID = getAutoUploadAlbumIdByBucketId(bucketId);
        if (!(TextUtils.isEmpty(albumID) || getServer() == null || !getLocalSwitch())) {
            if ("default-album-1".equalsIgnoreCase(albumID) && isSizeZero) {
                addCameraPath(data);
            } else if (cloudId == -1) {
                notifyCloudUpload(albumID, data, bucketRelativePath, hash, mediaType);
            }
        }
    }

    public static boolean checkUniqueId(String uniqueId, FileInfo fileInfo) {
        if (!isGUIDSupport() || TextUtils.isEmpty(uniqueId)) {
            return true;
        }
        return uniqueId.equalsIgnoreCase(fileInfo.getUniqueId());
    }
}
