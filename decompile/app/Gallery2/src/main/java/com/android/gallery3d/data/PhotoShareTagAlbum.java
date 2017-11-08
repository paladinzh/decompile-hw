package com.android.gallery3d.data;

import android.os.RemoteException;
import android.text.TextUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.PhotoShareCategoryAlbum.Category;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.ShareInfo;
import com.huawei.android.cg.vo.TagFileInfo;
import com.huawei.android.cg.vo.TagInfo;
import com.huawei.gallery.photoshare.utils.PhotoShareConstants;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class PhotoShareTagAlbum extends MediaSet {
    private static final String[] PROJECTION = new String[]{"fileName", "createTime", "localThumbPath", "localBigThumbPath", "localRealPath", "albumId", "fileType", "hash", "expand", "fileId", "size", "source"};
    private static final String TAG = PhotoShareTagAlbum.class.getSimpleName();
    private final GalleryApp mApplication;
    private int mCachedCount = -1;
    private Category mCategory;
    private String mCategoryID;
    private String mLastHash = null;
    private long mLastRequest = 0;
    private String mName;
    private PhotoShareChangeNotifier mPhotoShareChangeNotifier;
    private int mResId;
    private String mTagID;
    private TagInfo mTagInfo;

    public PhotoShareTagAlbum(Path path, GalleryApp galleryApp, TagInfo tagInfo) {
        int i = 0;
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = galleryApp;
        this.mTagInfo = tagInfo;
        this.mTagID = this.mTagInfo.getTagId();
        this.mCategoryID = this.mTagInfo.getCategoryId();
        this.mName = this.mTagInfo.getTagName();
        this.mCategory = Category.getName(this.mCategoryID);
        if (this.mCategory != Category.PEOPLE) {
            if (this.mCategory != null) {
                i = this.mCategory.resId;
            }
            this.mResId = i;
        } else if (TextUtils.isEmpty(this.mTagInfo.getTagName())) {
            this.mResId = R.string.photoshare_tag_unnamed;
        } else {
            this.mResId = 0;
        }
        this.mPhotoShareChangeNotifier = new PhotoShareChangeNotifier(this, 2, 3);
    }

    public void updateTagInfo(TagInfo info) {
        this.mTagInfo = info;
        this.mName = this.mTagInfo.getTagName();
    }

    public TagInfo getTagInfo() {
        return this.mTagInfo;
    }

    public String getName() {
        if (this.mResId != 0) {
            return this.mApplication.getResources().getString(this.mResId);
        }
        return this.mName;
    }

    public int getMediaItemCount() {
        if (this.mCachedCount == -1) {
            try {
                if (!PhotoShareUtils.isClassifySwitchOpen() || !PhotoShareUtils.isCloudPhotoSwitchOpen()) {
                    this.mCachedCount = 0;
                } else if (Category.DOCUMENT_CARD.categoryId.equalsIgnoreCase(this.mCategoryID)) {
                    this.mCachedCount = PhotoShareUtils.getServer().getCertificateCount();
                } else {
                    this.mCachedCount = PhotoShareUtils.getServer().getTagFileInfoListCount(this.mCategoryID, this.mTagID);
                }
                GalleryLog.v(TAG, "CategoryID " + this.mCategoryID + " TagID " + this.mTagID + " count " + this.mCachedCount);
            } catch (RemoteException e) {
                PhotoShareUtils.dealRemoteException(e);
            }
        }
        return this.mCachedCount;
    }

    public int getAlbumType() {
        if ("0".equalsIgnoreCase(this.mCategoryID)) {
            return 5;
        }
        return 6;
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        ArrayList<MediaItem> list = new ArrayList();
        GalleryUtils.assertNotInRenderThread();
        List<TagFileInfo> tagFileInfoList = getTagFileInfo(start, count);
        if (tagFileInfoList.isEmpty()) {
            return list;
        }
        int i;
        ArrayList<String> hashList = new ArrayList();
        for (i = 0; i < tagFileInfoList.size(); i++) {
            if (tagFileInfoList.get(i) != null) {
                hashList.add(((TagFileInfo) tagFileInfoList.get(i)).getHash());
            }
        }
        HashMap<String, FileInfo> map = getFileInfo((String[]) hashList.toArray(new String[hashList.size()]));
        for (i = 0; i < tagFileInfoList.size(); i++) {
            TagFileInfo tagFileInfo = (TagFileInfo) tagFileInfoList.get(i);
            if (tagFileInfo != null) {
                String faceId;
                MediaItem item;
                if ("0".equalsIgnoreCase(this.mCategoryID)) {
                    faceId = tagFileInfo.getFaceId();
                } else {
                    faceId = "-1";
                }
                Path childPath = this.mPath.getChild(tagFileInfo.getHash()).getChild(faceId);
                if (map.get(tagFileInfo.getHash()) != null) {
                    item = loadOrUpdateItem(childPath, tagFileInfo, (FileInfo) map.get(tagFileInfo.getHash()), this.mApplication);
                } else {
                    item = loadOrUpdateItem(childPath, tagFileInfo, this.mApplication);
                }
                list.add(item);
            }
        }
        if (list.size() != count) {
            GalleryLog.v(TAG, "categoryId =" + this.mCategoryID + ", tagId =" + this.mTagID + ", reload start " + start + ", " + "count " + count + ", real reloaded size " + list.size());
        }
        return list;
    }

    private HashMap<String, FileInfo> getFileInfo(String[] hash) {
        HashMap<String, FileInfo> result = new HashMap();
        List tempList = null;
        try {
            tempList = PhotoShareUtils.getServer().getShareFileInfoListByHash(hash);
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        if (tempList != null && tempList.size() > 0) {
            for (int i = 0; i < tempList.size(); i++) {
                result.put(((FileInfo) tempList.get(i)).getHash(), (FileInfo) tempList.get(i));
            }
        }
        return result;
    }

    private List<TagFileInfo> getTagFileInfo(int start, int count) {
        List<TagFileInfo> result = new ArrayList();
        try {
            List<TagFileInfo> tempList;
            int batch = count / SmsCheckResult.ESCT_200;
            for (int i = 0; i < batch; i++) {
                if (Category.DOCUMENT_CARD.categoryId.equalsIgnoreCase(this.mCategoryID)) {
                    tempList = PhotoShareUtils.getServer().getCertificateListLimit(start, SmsCheckResult.ESCT_200);
                } else {
                    tempList = PhotoShareUtils.getServer().getTagFileInfoListLimit(this.mCategoryID, this.mTagID, start, SmsCheckResult.ESCT_200);
                }
                if (tempList != null) {
                    result.addAll(tempList);
                }
                start += SmsCheckResult.ESCT_200;
            }
            int left = count % SmsCheckResult.ESCT_200;
            if (left > 0) {
                if (Category.DOCUMENT_CARD.categoryId.equalsIgnoreCase(this.mCategoryID)) {
                    tempList = PhotoShareUtils.getServer().getCertificateListLimit(start, left);
                } else {
                    tempList = PhotoShareUtils.getServer().getTagFileInfoListLimit(this.mCategoryID, this.mTagID, start, left);
                }
                if (tempList != null) {
                    result.addAll(tempList);
                }
            }
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        return result;
    }

    private boolean isAutoSyncAlbumID(String albumID) {
        if (albumID.equalsIgnoreCase("default-album-1") || albumID.equalsIgnoreCase("default-album-2") || albumID.startsWith("default-album-200-")) {
            return true;
        }
        return isThirdAutoUploadAlbumId(albumID);
    }

    private MediaItem loadOrUpdateItem(Path path, TagFileInfo tagFileInfo, GalleryApp app) {
        PhotoShareTagFile item;
        String hash = tagFileInfo.getHash();
        String str = null;
        FileInfo fileInfo = null;
        int folderType = 1;
        if (tagFileInfo.getAlbumList() != null) {
            int albumListSize = tagFileInfo.getAlbumList().size();
            ArrayList<String> searchedAlbumID = new ArrayList();
            for (int i = 0; i < albumListSize; i++) {
                String albumID = (String) tagFileInfo.getAlbumList().get(i);
                if (!(albumID == null || searchedAlbumID.contains(albumID))) {
                    searchedAlbumID.add(albumID);
                    try {
                        if (isAutoSyncAlbumID(albumID)) {
                            str = albumID;
                            folderType = 1;
                            fileInfo = getFileInfo(this.mApplication, albumID, hash);
                        } else {
                            ShareInfo shareInfo = PhotoShareUtils.getServer().getShare(albumID);
                            if (shareInfo != null) {
                                str = shareInfo.getShareName();
                            }
                            folderType = 2;
                            fileInfo = PhotoShareUtils.getServer().getShareFileInfo(albumID, hash);
                        }
                    } catch (RemoteException e) {
                        PhotoShareUtils.dealRemoteException(e);
                        GalleryLog.printDFXLog("PhotoShareTagAlbum for DFX RemoteException");
                    }
                    if (fileInfo != null) {
                        break;
                    }
                }
            }
        }
        if (fileInfo == null) {
            fileInfo = new FileInfo();
            GalleryLog.v(TAG, "PhotoShareTagFile Failed path " + path);
        }
        synchronized (DataManager.LOCK) {
            item = createPhotoShareTagFile(path, app, fileInfo, folderType, str, tagFileInfo);
        }
        return item;
    }

    private PhotoShareTagFile createPhotoShareTagFile(Path path, GalleryApp app, FileInfo fileInfo, int folderType, String name, TagFileInfo tagFileInfo) {
        PhotoShareTagFile item = (PhotoShareTagFile) app.getDataManager().peekMediaObject(path);
        if (item == null) {
            return new PhotoShareTagFile(path, app, fileInfo, folderType, name, tagFileInfo);
        }
        item.updateFromTagFileInfo(tagFileInfo, fileInfo);
        return item;
    }

    private boolean isThirdAutoUploadAlbumId(String albumId) {
        String userId = PhotoShareUtils.getUserId();
        String qqAlbumId = "default-album-101-" + userId;
        String weiXinAlbumId = "default-album-102-" + userId;
        String weiBoAlbumId = "default-album-103-" + userId;
        if (qqAlbumId.equalsIgnoreCase(albumId) || weiXinAlbumId.equalsIgnoreCase(albumId) || weiBoAlbumId.equalsIgnoreCase(albumId)) {
            return true;
        }
        return false;
    }

    private MediaItem loadOrUpdateItem(Path path, TagFileInfo tagFileInfo, FileInfo fileInfo, GalleryApp app) {
        int folderType;
        PhotoShareTagFile item;
        DataManager dataManager = app.getDataManager();
        if (fileInfo == null) {
            fileInfo = new FileInfo();
            GalleryLog.v(TAG, "PhotoShareTagFile Failed path " + path);
        }
        String albumID = fileInfo.getAlbumId();
        if ("default-album-1".equalsIgnoreCase(albumID) || "default-album-2".equalsIgnoreCase(albumID)) {
            folderType = 1;
        } else {
            folderType = 2;
        }
        String name = albumID == null ? fileInfo.getShareId() : albumID;
        synchronized (DataManager.LOCK) {
            item = (PhotoShareTagFile) dataManager.peekMediaObject(path);
            if (item == null) {
                item = new PhotoShareTagFile(path, app, fileInfo, folderType, name, tagFileInfo);
            } else {
                item.updateFromTagFileInfo(tagFileInfo, fileInfo);
            }
        }
        return item;
    }

    public synchronized long reload() {
        if (this.mPhotoShareChangeNotifier.isDirty()) {
            this.mDataVersion = MediaObject.nextVersionNumber();
            this.mCachedCount = -1;
        }
        return this.mDataVersion;
    }

    private void updateCoverTagInfo(final MediaItem item) {
        if (TextUtils.isEmpty(item.getFilePath())) {
            long now = System.currentTimeMillis();
            long interval = now - this.mLastRequest;
            String hash = item.getFileInfo().getHash();
            if (!TextUtils.isEmpty(hash)) {
                if (interval >= 15000 || interval <= 0 || !hash.equalsIgnoreCase(this.mLastHash)) {
                    this.mLastRequest = now;
                    this.mLastHash = hash;
                    new Thread() {
                        public void run() {
                            try {
                                TagFileInfo tagFileInfo = ((PhotoShareTagFile) item).getTagFileInfo();
                                PhotoShareUtils.getServer().downloadTagInfoCoverPhoto(tagFileInfo.getCategoryId(), tagFileInfo.getTagId(), new TagFileInfo[]{tagFileInfo});
                                GalleryLog.v(PhotoShareTagAlbum.TAG, "downloadTagInfoCoverPhoto categoryId " + PhotoShareTagAlbum.this.mCategoryID + " tagID " + PhotoShareTagAlbum.this.mTagID + " hash " + tagFileInfo.getHash());
                            } catch (RemoteException e) {
                                PhotoShareUtils.dealRemoteException(e);
                            }
                        }
                    }.start();
                } else {
                    GalleryLog.v(TAG, "request category cover too often");
                }
            }
        }
    }

    private TagFileInfo createFileInfoByExt(PhotoShareTagFile tagFile) throws JSONException {
        if (!PhotoShareUtils.isFileExists(tagFile.getTagFileInfo().getThumbUrl())) {
            String ext = this.mTagInfo.getExt1();
            if (!TextUtils.isEmpty(ext)) {
                JSONObject expandJson = new JSONObject(ext);
                Object hash = null;
                Object faceID = null;
                if (expandJson.has("hash")) {
                    hash = expandJson.getString("hash");
                }
                if (expandJson.has("faceId")) {
                    faceID = expandJson.getString("faceId");
                }
                if (!(TextUtils.isEmpty(hash) || TextUtils.isEmpty(faceID))) {
                    TagFileInfo input = new TagFileInfo();
                    input.setCategoryId(this.mCategoryID);
                    input.setTagId(this.mTagID);
                    input.setHash(hash);
                    input.setFaceId(faceID);
                    return input;
                }
            }
        }
        return null;
    }

    public MediaItem getCoverMediaItem() {
        ArrayList<MediaItem> items = getMediaItem(0, 1);
        if (items.size() > 0) {
            if (this.mCategory != Category.PEOPLE) {
                MediaItem item;
                TagFileInfo maxConfidenceTagFileInfo = null;
                try {
                    maxConfidenceTagFileInfo = PhotoShareUtils.getServer().getMaxConfidenceTagFileInfo(this.mCategoryID, this.mTagID);
                } catch (RemoteException e) {
                    PhotoShareUtils.dealRemoteException(e);
                }
                if (maxConfidenceTagFileInfo == null || TextUtils.isEmpty(maxConfidenceTagFileInfo.getSpConfidence())) {
                    item = (MediaItem) items.get(0);
                } else {
                    item = loadOrUpdateItem(this.mPath.getChild(maxConfidenceTagFileInfo.getHash()).getChild("-1"), maxConfidenceTagFileInfo, this.mApplication);
                }
                updateCoverTagInfo(item);
                return item;
            }
            PhotoShareTagFile tagFile = (PhotoShareTagFile) items.get(0);
            TagFileInfo tagFileInfo = tagFile.getTagFileInfo();
            FileInfo fileInfo = tagFile.getFileInfo();
            try {
                TagFileInfo input = createFileInfoByExt(tagFile);
                if (input != null) {
                    try {
                        TagFileInfo tempTagFileInfo = PhotoShareUtils.getServer().getTagFileInfo(input);
                        if (tempTagFileInfo != null) {
                            List<FileInfo> tempList = PhotoShareUtils.getServer().getShareFileInfoListByHash(new String[]{input.getHash()});
                            if (tempList != null && tempList.size() > 0) {
                                final TagFileInfo tagFileInfoRequested = tagFileInfo;
                                tagFileInfo = tempTagFileInfo;
                                final FileInfo fileInfoRequested = fileInfo;
                                fileInfo = (FileInfo) tempList.get(0);
                                new Thread() {
                                    public void run() {
                                        PhotoShareTagAlbum.this.getTagCover(Path.fromString("/photoshare/tagcover/" + PhotoShareTagAlbum.this.mCategoryID + "/" + PhotoShareTagAlbum.this.mTagID + "/" + tagFileInfoRequested.getHash() + "/" + tagFileInfoRequested.getFaceId()), PhotoShareTagAlbum.this.mApplication, fileInfoRequested, tagFileInfoRequested);
                                    }
                                }.start();
                            }
                        }
                    } catch (RemoteException e2) {
                        PhotoShareUtils.dealRemoteException(e2);
                    }
                }
            } catch (JSONException e3) {
                GalleryLog.i(TAG, "A JSONException has occurred in getCoverMediaItem() method.");
            }
            return getTagCover(Path.fromString("/photoshare/tagcover/" + this.mCategoryID + "/" + this.mTagID + "/" + tagFileInfo.getHash() + "/" + tagFileInfo.getFaceId()), this.mApplication, fileInfo, tagFileInfo);
        }
        GalleryLog.v(TAG, "PhotoShareTagAlbum getCoverMediaItem null, tagID " + this.mTagID);
        return null;
    }

    public boolean isLeafAlbum() {
        return true;
    }

    private PhotoShareTagCover getTagCover(Path path, GalleryApp application, FileInfo fileInfo, TagFileInfo tagFileInfo) {
        int folderType;
        PhotoShareTagCover cover;
        String albumID = fileInfo.getAlbumId();
        if ("default-album-1".equalsIgnoreCase(albumID) || "default-album-2".equalsIgnoreCase(albumID)) {
            folderType = 1;
        } else {
            folderType = 2;
        }
        String albumName = albumID == null ? fileInfo.getShareId() : albumID;
        synchronized (DataManager.LOCK) {
            cover = (PhotoShareTagCover) application.getDataManager().peekMediaObject(path);
            if (cover == null) {
                cover = new PhotoShareTagCover(path, this.mApplication, fileInfo, folderType, albumName, tagFileInfo);
            } else {
                cover.updateFromTagFileInfo(tagFileInfo, fileInfo);
            }
            cover.requestFaceThumbUrl();
        }
        return cover;
    }

    public static FileInfo getFileInfo(GalleryApp application, String albumId, String hash) {
        Closeable closeable = null;
        try {
            closeable = application.getContentResolver().query(PhotoShareConstants.CLOUD_FILE_TABLE_URI, PROJECTION, "albumId = ? and hash = ?", new String[]{albumId, hash}, null);
            if (closeable == null || !closeable.moveToNext()) {
                Utils.closeSilently(closeable);
                return null;
            }
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileName(closeable.getString(0));
            fileInfo.setCreateTime(closeable.getLong(1));
            fileInfo.setLocalThumbPath(closeable.getString(2));
            fileInfo.setLocalBigThumbPath(closeable.getString(3));
            fileInfo.setLocalRealPath(closeable.getString(4));
            fileInfo.setAlbumId(closeable.getString(5));
            fileInfo.setFileType(closeable.getInt(6));
            fileInfo.setHash(closeable.getString(7));
            fileInfo.setExpand(closeable.getString(8));
            fileInfo.setFileId(closeable.getString(9));
            fileInfo.setSize(closeable.getLong(10));
            fileInfo.setShareId("");
            String source = closeable.getString(11);
            if (TextUtils.isEmpty(source)) {
                source = "UNKNOW";
            }
            fileInfo.setSource(source);
            return fileInfo;
        } catch (RuntimeException e) {
            GalleryLog.e("photoshareLogTag", "query cloud file error albumId " + albumId + " hash" + hash);
        } finally {
            Utils.closeSilently(closeable);
        }
    }
}
