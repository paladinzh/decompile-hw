package com.huawei.gallery.photoshare.uploadservice;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.RemoteException;
import android.os.SystemClock;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.BitmapPool;
import com.android.gallery3d.data.CloudSwitchHelper;
import com.android.gallery3d.data.DecodeUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.photoshare.utils.MD5Utils;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.watermark.manager.parse.WMElement;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class UploadService extends IntentService implements FilenameFilter {
    private static final Uri GALLERY_URI = GalleryMedia.URI;
    private static final String[] PROJECTION = new String[]{"bucket_id", "_data", "bucket_relative_path", "hash", "media_type"};
    private static BitmapPool sBitmapPool;
    private GalleryApp mApplication;
    private File mCacheFolder;

    public boolean accept(File dir, String name) {
        if (name.lastIndexOf(95) > 0) {
            String str = name.substring(name.lastIndexOf(95));
            if (str.equals("_1") || str.equals("_2")) {
                return true;
            }
        }
        return false;
    }

    public UploadService() {
        super("UploadService");
    }

    public void onCreate() {
        super.onCreate();
        this.mApplication = (GalleryApp) getApplication();
        GalleryLog.d("UploadService", this.mApplication.toString());
        this.mCacheFolder = new File(GalleryStorageManager.getInstance().getInnerGalleryStorage().getPath(), "/Android/data/" + getApplicationContext().getPackageName() + "/uploadcache");
        if (!this.mCacheFolder.exists()) {
            GalleryLog.d("UploadService", "make dir status : " + this.mCacheFolder.mkdirs());
        }
        initBitmapPool();
    }

    private synchronized void initBitmapPool() {
        if (sBitmapPool == null) {
            sBitmapPool = new BitmapPool(2);
        }
    }

    protected void onHandleIntent(Intent intent) {
        boolean z = true;
        String action = intent.getAction();
        String str;
        if ("com.huawei.photoShare.action.UPLOAD_PASSIVELY".equals(action)) {
            str = "UploadService";
            StringBuilder append = new StringBuilder().append("switchStatus:").append(CloudSwitchHelper.isCloudAutoUploadSwitchOpen()).append(" ");
            if (PhotoShareUtils.hasNeverSynchronizedCloudData()) {
                z = false;
            }
            GalleryLog.d(str, append.append(z).toString());
            if (CloudSwitchHelper.isCloudAutoUploadSwitchOpen() && !PhotoShareUtils.hasNeverSynchronizedCloudData()) {
                clearCache();
                passivelyUploadFiles();
            }
        } else if ("com.huawei.photoShare.action.UPLOAD_FORWARDLY".equals(action)) {
            clearCache();
            String albumId = intent.getStringExtra("albumId");
            String absolutePath = intent.getStringExtra("absPath");
            String relativeBucketPath = intent.getStringExtra("relative-bucketPath");
            String hash = intent.getStringExtra("hash");
            int mediaType = intent.getIntExtra("media-type", 1);
            if (relativeBucketPath == null) {
                str = GalleryMedia.getBucketRelativePath(absolutePath);
            } else {
                str = relativeBucketPath;
            }
            forwardlyUploadFile(getSingleFileInfo(absolutePath, albumId, str, hash, mediaType));
        }
    }

    private void clearCache() {
        File[] tempFile = this.mCacheFolder.listFiles(this);
        if (tempFile != null) {
            ArrayList<File> files = new ArrayList(Arrays.asList(tempFile));
            if (!files.isEmpty()) {
                int i;
                File file;
                String fileName;
                int fileSize = files.size();
                HashSet list = getAllNeedUploadHashes();
                for (i = fileSize - 1; i >= 0; i--) {
                    file = (File) files.get(i);
                    fileName = file.getName();
                    if (!list.contains(fileName.substring(0, fileName.indexOf("_")))) {
                        files.remove(i);
                        GalleryLog.d("UploadService", "delete file status : " + file.delete());
                    }
                }
                if (files.size() >= SmsCheckResult.ESCT_200) {
                    ArrayList<FileInfo> fileInfosList = new ArrayList();
                    ArrayList<String> fileHashList = new ArrayList();
                    for (i = 0; i < 10; i++) {
                        file = (File) files.get(i);
                        fileName = file.getName();
                        String fileHash = fileName.substring(0, fileName.indexOf("_"));
                        if (!fileHashList.contains(fileHash)) {
                            fileHashList.add(fileHash);
                            FileInfo info = uploadCacheFileIfNeeded(file);
                            if (info != null) {
                                fileInfosList.add(info);
                            }
                        }
                    }
                    if (!fileInfosList.isEmpty()) {
                        forwardlyUploadFiles(fileInfosList);
                        stopSelf();
                    }
                }
            }
        }
    }

    private void passivelyUploadFiles() {
        notifyCloudUpload(getWaitUploadGeneralFile(10));
    }

    private void forwardlyUploadFiles(ArrayList<FileInfo> list) {
        if (!list.isEmpty()) {
            WaitUploadGeneralFile uploadThumbnailFile = new WaitUploadGeneralFile();
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                uploadThumbnailFile.addFileInfo((FileInfo) list.get(i));
            }
            notifyCloudUpload(uploadThumbnailFile);
        }
    }

    private void forwardlyUploadFile(FileInfo fileInfo) {
        if (fileInfo != null) {
            WaitUploadGeneralFile uploadThumbnailFile = new WaitUploadGeneralFile();
            uploadThumbnailFile.addFileInfo(fileInfo);
            notifyCloudUpload(uploadThumbnailFile);
        }
    }

    private WaitUploadGeneralFile getWaitUploadGeneralFile(int limitCount) {
        WaitUploadGeneralFile uploadThumbnailFile = new WaitUploadGeneralFile();
        Builder uriBuilder = GALLERY_URI.buildUpon();
        uriBuilder.appendQueryParameter("limit", String.valueOf(limitCount));
        Uri uri = uriBuilder.build();
        Closeable closeable = null;
        closeable = this.mApplication.getContentResolver().query(uri, PROJECTION, "(bucket_id IN (" + PhotoShareUtils.getAutoUploadBucketIds() + ") AND cloud_media_id = -1 AND relative_cloud_media_id = -1)", null, "showDateToken DESC, _id DESC");
        if (closeable == null) {
            GalleryLog.w("UploadService", "query fail: " + uri);
            WaitUploadGeneralFile waitUploadGeneralFile = null;
            return waitUploadGeneralFile;
        }
        while (closeable.moveToNext()) {
            int bucketId = closeable.getInt(0);
            if (isBucketAutoUpload(bucketId)) {
                String bucketRelativePath;
                String albumIdFromBucketId = getAlbumIdFromBucketId(bucketId);
                String absPath = closeable.getString(1);
                String bucketRelativePath2 = closeable.getString(2);
                String string = closeable.getString(3);
                int i = closeable.getInt(4);
                if (bucketRelativePath2 == null) {
                    bucketRelativePath = GalleryMedia.getBucketRelativePath(absPath);
                } else {
                    bucketRelativePath = bucketRelativePath2;
                }
                try {
                    FileInfo singleFileInfo = getSingleFileInfo(absPath, albumIdFromBucketId, bucketRelativePath, string, i);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                    if (singleFileInfo != null) {
                        uploadThumbnailFile.addFileInfo(singleFileInfo);
                    }
                } catch (SecurityException e2) {
                    waitUploadGeneralFile = "UploadService";
                    GalleryLog.w((String) waitUploadGeneralFile, "No permission to query!");
                } finally {
                    Utils.closeSilently(closeable);
                }
            }
        }
        Utils.closeSilently(closeable);
        return uploadThumbnailFile;
    }

    private HashSet<String> getAllNeedUploadHashes() {
        Closeable closeable = null;
        String selection = "cloud_media_id = -1 AND relative_cloud_media_id = -1";
        HashSet<String> resSet = new HashSet();
        try {
            closeable = this.mApplication.getContentResolver().query(GALLERY_URI, new String[]{"hash"}, selection, null, "showDateToken DESC, _id DESC");
            if (closeable == null) {
                GalleryLog.w("UploadService", "query fail: " + GALLERY_URI);
                Utils.closeSilently(closeable);
                return resSet;
            }
            while (closeable.moveToNext()) {
                resSet.add(closeable.getString(0));
            }
            Utils.closeSilently(closeable);
            return resSet;
        } catch (SecurityException e) {
            GalleryLog.w("UploadService", "No permission to query!");
            Utils.closeSilently(closeable);
            return resSet;
        } catch (Throwable th) {
            Utils.closeSilently(closeable);
            return resSet;
        }
    }

    private FileInfo getSingleFileInfo(String absPath, String albumId, String bucketRelativePath, String hash, int mediaType) {
        ArrayList<File> files = createThumbnail(absPath, hash, mediaType);
        if (files.isEmpty()) {
            return null;
        }
        String microThumbnailPath = ((File) files.get(0)).getAbsolutePath();
        String lcdThumbnailPath = ((File) files.get(1)).getAbsolutePath();
        FileInfo singleFileInfo = new FileInfo();
        singleFileInfo.setAlbumId(albumId);
        singleFileInfo.setLocalThumbPath(microThumbnailPath);
        singleFileInfo.setLocalBigThumbPath(lcdThumbnailPath);
        singleFileInfo.setExpand(bucketRelativePath);
        singleFileInfo.setLocalRealPath(absPath);
        return singleFileInfo;
    }

    private ArrayList<File> createThumbnail(String filePath, String hash, int mediaType) {
        ArrayList<File> thumbNails = new ArrayList();
        if (filePath == null) {
            return thumbNails;
        }
        if (hash == null) {
            hash = MD5Utils.getMD5(new File(filePath));
        }
        String fileName_1 = hash + "_1";
        String fileName_2 = hash + "_2";
        GalleryLog.d("UploadService", "file name is : " + fileName_1 + " and " + fileName_2 + " path:" + filePath);
        thumbNails.add(0, new File(this.mCacheFolder.getPath(), fileName_1));
        boolean writeStatus = writeThumbnailToFile(filePath, (File) thumbNails.get(0), 2, mediaType);
        thumbNails.add(1, new File(this.mCacheFolder.getPath(), fileName_2));
        if (!(writeStatus & writeThumbnailToFile(filePath, (File) thumbNails.get(1), 16, mediaType))) {
            thumbNails = new ArrayList();
        }
        return thumbNails;
    }

    private FileInfo uploadCacheFileIfNeeded(File file) {
        String fileName = file.getName();
        String hash = fileName.substring(0, fileName.indexOf("_"));
        Closeable closeable = null;
        FileInfo fileInfo;
        try {
            closeable = this.mApplication.getContentResolver().query(GALLERY_URI, new String[]{"bucket_id", "bucket_relative_path", "_data", "media_type"}, "(hash = ?)", new String[]{hash}, "showDateToken DESC, _id DESC");
            if (closeable == null) {
                GalleryLog.w("UploadService", "query fail: " + GALLERY_URI);
                fileInfo = null;
                return fileInfo;
            } else if (closeable.moveToNext()) {
                int bucketId = closeable.getInt(0);
                if (isBucketAutoUpload(bucketId)) {
                    String albumId = getAlbumIdFromBucketId(bucketId);
                    String absPath = closeable.getString(2);
                    FileInfo fileInfo2 = getSingleFileInfo(absPath, albumId, closeable.getString(1), getHashFromAbsPath(absPath), closeable.getInt(3));
                    Utils.closeSilently(closeable);
                    return fileInfo2;
                }
                GalleryLog.d("UploadService", "delete file status : " + file.delete());
                Utils.closeSilently(closeable);
                return null;
            } else {
                Utils.closeSilently(closeable);
                return null;
            }
        } catch (SecurityException e) {
            fileInfo = "UploadService";
            GalleryLog.w((String) fileInfo, "No permission to query!");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    private boolean isBucketAutoUpload(int bucketId) {
        return PhotoShareUtils.getsAutoUploadAlbumBucketId().keySet().contains(String.valueOf(bucketId));
    }

    private String getAlbumIdFromBucketId(int bucketId) {
        return (String) PhotoShareUtils.getsAutoUploadAlbumBucketId().get(String.valueOf(bucketId));
    }

    private String getHashFromAbsPath(String absPath) {
        Closeable closeable = null;
        String str = null;
        try {
            closeable = this.mApplication.getContentResolver().query(GALLERY_URI, new String[]{"hash"}, "(_data = ?)", new String[]{absPath}, "showDateToken DESC, _id DESC");
            if (closeable == null) {
                GalleryLog.w("UploadService", "query fail: " + GALLERY_URI);
                return null;
            }
            while (closeable.moveToNext()) {
                str = closeable.getString(0);
            }
            Utils.closeSilently(closeable);
            return str;
        } catch (SecurityException e) {
            GalleryLog.w("UploadService", "No permission to query!");
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    private void notifyCloudUpload(WaitUploadGeneralFile uploadThumbnailFile) {
        GalleryLog.d("UploadService", "start upload");
        try {
            PhotoShareUtils.getServer().uploadGeneralFileList(uploadThumbnailFile.getFileInfoList());
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
    }

    private Bitmap getImageThumbnail(String filePath, int type, int targetSize) {
        Throwable e;
        Throwable ex;
        Throwable th;
        Closeable closeable = null;
        Bitmap thumbnail = null;
        try {
            Closeable fis = new FileInputStream(filePath);
            try {
                FileDescriptor fd = fis.getFD();
                Options options = new Options();
                options.inBitmap = DecodeUtils.findCachedBitmap(sBitmapPool, ThreadPool.JOB_CONTEXT_STUB, fd, options);
                thumbnail = DecodeUtils.decodeThumbnail(ThreadPool.JOB_CONTEXT_STUB, filePath, options, targetSize, type);
                Utils.closeSilently(fis);
                closeable = fis;
            } catch (FileNotFoundException e2) {
                e = e2;
                closeable = fis;
                GalleryLog.w("UploadService", e);
                Utils.closeSilently(closeable);
                return thumbnail;
            } catch (Exception e3) {
                ex = e3;
                closeable = fis;
                try {
                    GalleryLog.w("UploadService", ex);
                    Utils.closeSilently(closeable);
                    return thumbnail;
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
        } catch (FileNotFoundException e4) {
            e = e4;
            GalleryLog.w("UploadService", e);
            Utils.closeSilently(closeable);
            return thumbnail;
        } catch (Exception e5) {
            ex = e5;
            GalleryLog.w("UploadService", ex);
            Utils.closeSilently(closeable);
            return thumbnail;
        }
        return thumbnail;
    }

    private boolean writeThumbnailToFile(String filePath, File file, int type, int mediaType) {
        Throwable th;
        if (file.exists() && file.length() > 0) {
            return true;
        }
        Closeable os = null;
        Bitmap thumbnail = null;
        long startTime = SystemClock.uptimeMillis();
        int targetSize = getTargetSize(type);
        if (3 == mediaType) {
            Bitmap bitmap = BitmapUtils.createVideoThumbnail(filePath);
            if (bitmap != null) {
                int length;
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                if (w > h) {
                    length = w;
                } else {
                    length = h;
                }
                float scale = ((float) targetSize) / ((float) length);
                Matrix scaleMatrix = new Matrix();
                if (scale > WMElement.CAMERASIZEVALUE1B1) {
                    scale = WMElement.CAMERASIZEVALUE1B1;
                }
                scaleMatrix.postScale(scale, scale);
                thumbnail = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), scaleMatrix, false);
            }
        } else if (1 == mediaType) {
            thumbnail = getImageThumbnail(filePath, type, targetSize);
        } else {
            GalleryLog.d("UploadService", "media type not support :" + mediaType);
        }
        if (thumbnail == null) {
            GalleryLog.d("UploadService", "create thumbnail failed!");
            return false;
        }
        try {
            Closeable fileOutputStream = new FileOutputStream(file);
            try {
                thumbnail.compress(CompressFormat.JPEG, 90, fileOutputStream);
                GalleryLog.d("UploadService", "thumbnail created,cost time = " + (SystemClock.uptimeMillis() - startTime));
                Utils.closeSilently(fileOutputStream);
                return true;
            } catch (FileNotFoundException e) {
                os = fileOutputStream;
                try {
                    GalleryLog.d("UploadService", "FileNotFoundException!");
                    Utils.closeSilently(os);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    Utils.closeSilently(os);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                os = fileOutputStream;
                Utils.closeSilently(os);
                throw th;
            }
        } catch (FileNotFoundException e2) {
            GalleryLog.d("UploadService", "FileNotFoundException!");
            Utils.closeSilently(os);
            return false;
        }
    }

    private int getTargetSize(int type) {
        switch (type) {
            case 2:
                return SmsCheckResult.ESCT_200;
            case 16:
                return 1920;
            default:
                return SmsCheckResult.ESCT_200;
        }
    }
}
