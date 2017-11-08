package com.android.gallery3d.util;

import android.content.Context;
import android.os.Environment;
import android.util.SparseIntArray;
import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaSet;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class MediaSetUtils {
    public static final int DOCRECTIFY_BUCKET_ID = getBucketIdForInnerStorage(BucketNames.DOCUMENT_RECTIFY);
    public static final int DOWNLOAD_BUCKET_ID = getBucketId("download");
    public static final String EXTERNAL_STORAGE_DIRCETORY = Environment.getExternalStorageDirectory().toString();
    public static final int HWTHEME_WALLPAPER_BUCKET_ID = getBucketIdForInnerStorage("HWThemes/HWWallpapers");
    public static final int IMPORTED_BUCKET_ID = getBucketId("Imported");
    public static final int MAGAZINE_UNLOCK_BUCKET_ID = getBucketId("MagazineUnlock");
    public static final Comparator<MediaSet> NAME_COMPARATOR = new NameComparator();
    public static final int PRELOAD_PICTURES_BUCKET_ID = GalleryUtils.getBucketId(GalleryUtils.getPreloadMediaDirectory() + "/Pictures");
    public static final int SCREENSHOTS_BUCKET_ID = getBucketId("Pictures/Screenshots");
    private static int sCameraBucketID;
    private static File sCameraDir;
    private static final SparseIntArray sNameMap = new SparseIntArray();
    private static int sScreenshotsBucketID;

    public static class NameComparator implements Comparator<MediaSet> {
        public int compare(MediaSet set1, MediaSet set2) {
            int result = set1.getName().compareToIgnoreCase(set2.getName());
            if (result != 0) {
                return result;
            }
            return set1.getPath().toString().compareTo(set2.getPath().toString());
        }
    }

    static {
        init();
    }

    private static void init() {
        GalleryStorageManager galleryStorageManager = GalleryStorageManager.getInstance();
        GalleryStorage innerGalleryStorage = galleryStorageManager.getInnerGalleryStorage();
        ArrayList<GalleryStorage> outerGalleryStorageList = galleryStorageManager.getOuterGalleryStorageList();
        GalleryLog.d("MediaSetUtils", "innerGalleryStorage:" + innerGalleryStorage + ", outerGalleryStorageList:" + outerGalleryStorageList);
        if (innerGalleryStorage == null || outerGalleryStorageList.size() <= 0) {
            sCameraDir = new File(Environment.getExternalStorageDirectory(), Constant.CAMERA_PATH);
            sScreenshotsBucketID = GalleryUtils.getBucketId(new File(Environment.getExternalStorageDirectory(), "/Pictures/Screenshots").toString());
        } else {
            sCameraDir = new File(innerGalleryStorage.getPath(), Constant.CAMERA_PATH);
            sScreenshotsBucketID = GalleryUtils.getBucketId(new File(innerGalleryStorage.getPath(), "/Pictures/Screenshots").toString());
        }
        sCameraBucketID = GalleryUtils.getBucketId(sCameraDir.toString());
    }

    public static File getCameraDir() {
        return sCameraDir;
    }

    public static int getCameraBucketId() {
        return sCameraBucketID;
    }

    public static int getScreenshotsBucketID() {
        return sScreenshotsBucketID;
    }

    public static int getBucketId(String name) {
        return GalleryUtils.getBucketId(EXTERNAL_STORAGE_DIRCETORY + "/" + name);
    }

    private static int getBucketIdForInnerStorage(String name) {
        String path = EXTERNAL_STORAGE_DIRCETORY;
        GalleryStorage innerGalleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
        if (innerGalleryStorage != null) {
            path = innerGalleryStorage.getPath();
        }
        return GalleryUtils.getBucketId(path + "/" + name);
    }

    public static int bucketId2ResourceId(int id, Context context) {
        if (sNameMap.size() == 0) {
            initNameMap(context);
        }
        return sNameMap.get(id, 0);
    }

    public static void reset() {
        init();
        initCardRelativeNameMap();
    }

    private static void initCardRelativeNameMap() {
        GalleryStorageManager galleryStorageManager = GalleryStorageManager.getInstance();
        ArrayList<GalleryStorage> outerGalleryStorageList = galleryStorageManager.getOuterGalleryStorageList();
        sNameMap.put(sCameraBucketID, R.string.camera_folder_only_phone);
        if (outerGalleryStorageList.size() == 1) {
            sNameMap.put(((GalleryStorage) outerGalleryStorageList.get(0)).getBucketID(Constant.CAMERA_PATH), R.string.camera_folder_only_sdcard);
        } else {
            for (GalleryStorage galleryStorage : outerGalleryStorageList) {
                sNameMap.put(galleryStorage.getBucketID(Constant.CAMERA_PATH), R.string.camera_folder_multi_sdcard);
            }
        }
        if (GalleryUtils.isScreenRecorderExist()) {
            sNameMap.put(sScreenshotsBucketID, R.string.screenshots_folder_only_phone);
            if (outerGalleryStorageList.size() == 1) {
                sNameMap.put(((GalleryStorage) outerGalleryStorageList.get(0)).getBucketID("/Pictures/Screenshots"), R.string.screenshots_folder_only_sdcard);
            } else {
                for (GalleryStorage galleryStorage2 : outerGalleryStorageList) {
                    sNameMap.put(galleryStorage2.getBucketID("/Pictures/Screenshots"), R.string.screenshots_folder_multi_sdcard);
                }
            }
        } else {
            sNameMap.put(SCREENSHOTS_BUCKET_ID, R.string.folder_screenshot);
        }
        GalleryStorage innerGalleryStorage = galleryStorageManager.getInnerGalleryStorage();
        if (innerGalleryStorage != null) {
            sNameMap.put(GalleryUtils.getBucketId(innerGalleryStorage.getPath()), R.string.internal_storage_root_directory);
        }
        if (outerGalleryStorageList.size() == 1) {
            sNameMap.put(GalleryUtils.getBucketId(((GalleryStorage) outerGalleryStorageList.get(0)).getPath()), R.string.external_storage_root_directory);
            return;
        }
        for (GalleryStorage galleryStorage22 : outerGalleryStorageList) {
            sNameMap.put(GalleryUtils.getBucketId(galleryStorage22.getPath()), R.string.external_storage_multi_root_directory);
        }
    }

    private static void initNameMap(Context context) {
        initCardRelativeNameMap();
        sNameMap.put(PRELOAD_PICTURES_BUCKET_ID, R.string.preset_pictures);
        sNameMap.put(DOWNLOAD_BUCKET_ID, R.string.folder_download);
        sNameMap.put(IMPORTED_BUCKET_ID, R.string.folder_imported);
        sNameMap.put(MAGAZINE_UNLOCK_BUCKET_ID, R.string.folder_magazine_unlock);
        sNameMap.put(getBucketId("EditedOnlinePhotos"), R.string.folder_edited_online_photos);
        sNameMap.put(getBucketId("CloudPicture"), R.string.photoshare_download);
        sNameMap.put(getBucketId("tencent/QQ_Images"), R.string.folder_qq_images);
        sNameMap.put(getBucketId("tencent/QQ_Favorite"), R.string.folder_qq_favorite);
        sNameMap.put(getBucketId("tencent/QzonePic"), R.string.folder_qzone);
        sNameMap.put(getBucketId("tencent/MicroMsg/WeiXin"), R.string.folder_qq_weixin);
        sNameMap.put(getBucketId("sina/weibo/save"), R.string.folder_sina_weibo_save);
        sNameMap.put(getBucketId("sina/weibo/weibo"), R.string.folder_sina_weibo_save);
        sNameMap.put(getBucketId("taobao"), R.string.folder_taobao);
        sNameMap.put(getBucketId("UCDownloads"), R.string.folder_ucdownloads);
        sNameMap.put(getBucketId("QIYIVideo"), R.string.folder_qiyi_video);
        sNameMap.put(getBucketId("dianping"), R.string.folder_dianping);
        sNameMap.put(getBucketId("MTXX"), R.string.folder_mtxx);
        sNameMap.put(getBucketId("Photowonder"), R.string.folder_photowonder);
        sNameMap.put(getBucketId("MYXJ"), R.string.folder_myxj);
        sNameMap.put(getBucketId("Pictures/InstaMag"), R.string.folder_instamag);
        sNameMap.put(getBucketId("MTTT"), R.string.folder_mttt);
        sNameMap.put(getBucketId("MomanCamera"), R.string.folder_momancamera);
        sNameMap.put(getBucketId("Bluetooth"), R.string.folder_bluetooth);
        sNameMap.put(getBucketId("ShareViaWLAN"), R.string.folder_wlan);
        sNameMap.put(getBucketId("Pictures"), R.string.pictures);
        sNameMap.put(getBucketId("Video"), R.string.folder_video);
        sNameMap.put(getBucketId("百度魔拍"), R.string.folder_wondercam);
        sNameMap.put(getBucketId("DCIM/GroupRecorder"), R.string.folder_group_recorder);
        sNameMap.put(getBucketId("Pictures/Recover"), R.string.toolbarbutton_recover);
    }

    public static int getCameraAlbumStringId() {
        return R.string.camera;
    }

    public static int getScreenshotsAlbumStringId() {
        return R.string.screenshots;
    }
}
