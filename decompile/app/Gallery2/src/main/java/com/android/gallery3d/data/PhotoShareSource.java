package com.android.gallery3d.data;

import android.os.RemoteException;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.android.cg.vo.CategoryInfo;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.android.cg.vo.FileInfoDetail;
import com.huawei.android.cg.vo.ShareInfo;
import com.huawei.android.cg.vo.TagFileInfo;
import com.huawei.android.cg.vo.TagInfo;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.List;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class PhotoShareSource extends MediaSource {
    private GalleryApp mApplication;
    private PathMatcher mMatcher = new PathMatcher();

    private static class CreateObjectVars {
        String albumID;
        String albumName;
        int albumType;
        FileInfo fileInfo;
        boolean isImage;
        int mediaType;
        ShareInfo shareInfo;

        private CreateObjectVars() {
            this.shareInfo = null;
            this.fileInfo = null;
            this.albumName = null;
            this.albumID = null;
            this.albumType = 1;
            this.mediaType = 3;
            this.isImage = false;
        }

        void setIsImage(int pathType) {
            switch (pathType) {
                case 10:
                    this.mediaType = 1;
                    return;
                case 12:
                    this.mediaType = 2;
                    return;
                case 100:
                    this.isImage = true;
                    return;
                default:
                    return;
            }
        }

        void setShareAlbumType(ShareInfo shareInfo) {
            if (4 == shareInfo.getType()) {
                this.albumType = 7;
            } else if (1 == shareInfo.getType()) {
                this.albumType = 2;
            } else {
                this.albumType = 3;
            }
        }

        void setMyShareAlbumType(ShareInfo shareInfo) {
            if (1 == shareInfo.getType()) {
                this.albumType = 2;
            } else {
                this.albumType = 3;
            }
        }

        void setTagFileAlbumType(List<FileInfo> tempList) {
            this.fileInfo = (FileInfo) tempList.get(0);
            this.albumID = this.fileInfo.getAlbumId();
            if ("default-album-1".equalsIgnoreCase(this.albumID) || "default-album-2".equalsIgnoreCase(this.albumID)) {
                this.albumType = 1;
            } else {
                this.albumType = 2;
            }
        }

        void setClassifyCoverAlbumType(GalleryApp application, Path path) {
            this.albumID = path.getParent().getSuffix();
            try {
                if (this.albumID.equalsIgnoreCase("default-album-1") || this.albumID.equalsIgnoreCase("default-album-2")) {
                    this.albumName = this.albumID;
                    this.albumType = 1;
                    this.fileInfo = PhotoShareTagAlbum.getFileInfo(application, this.albumID, path.getSuffix());
                    if (this.fileInfo == null) {
                        this.fileInfo = new FileInfo();
                        GalleryLog.v("PhotoShareSource", "CLASSIFY_COVER_ITEM not find fileInfo " + path);
                    }
                }
                this.shareInfo = PhotoShareUtils.getServer().getShare(this.albumID);
                if (this.shareInfo != null) {
                    this.albumName = this.shareInfo.getShareName();
                }
                this.albumType = 2;
                this.fileInfo = PhotoShareUtils.getServer().getShareFileInfo(this.albumID, path.getSuffix());
                if (this.fileInfo == null) {
                    this.fileInfo = new FileInfo();
                    GalleryLog.v("PhotoShareSource", "CLASSIFY_COVER_ITEM not find fileInfo " + path);
                }
            } catch (RemoteException e) {
                PhotoShareUtils.dealRemoteException(e);
            }
        }
    }

    private com.android.gallery3d.data.CloudLocalAlbum createCloudLocalAlbum(com.android.gallery3d.data.Path r11) {
        /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at jadx.core.dex.visitors.ssa.SSATransform.placePhi(SSATransform.java:82)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r10 = this;
        r6 = 0;
        r7 = r11.getSuffix();
        r8 = 0;
        r1 = r10.mApplication;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r0 = r1.getContentResolver();	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r1 = com.huawei.gallery.photoshare.utils.PhotoShareConstants.CLOUD_ALBUM_TABLE_URI;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r2 = 2;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r3 = "albumName";	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r4 = 0;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r2[r4] = r3;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r3 = "lpath";	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r4 = 1;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r2[r4] = r3;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r3 = "albumId=?";	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r4 = 1;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r5 = 0;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r4[r5] = r7;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r5 = 0;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r8 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        if (r8 == 0) goto L_0x004a;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
    L_0x002d:
        r1 = r8.moveToNext();	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        if (r1 == 0) goto L_0x0075;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
    L_0x0033:
        r0 = new com.android.gallery3d.data.CloudLocalAlbum;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r2 = r10.mApplication;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r1 = 0;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r4 = r8.getString(r1);	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r1 = 1;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r5 = r8.getString(r1);	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r1 = r11;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r3 = r7;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r0.<init>(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
    L_0x0046:
        com.android.gallery3d.common.Utils.closeSilently(r8);
    L_0x0049:
        return r0;
    L_0x004a:
        r0 = r6;
        goto L_0x0046;
    L_0x004c:
        r9 = move-exception;
        r1 = "photoshareLogTag";	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r2.<init>();	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r3 = "AlbumsLoader SQLiteException  ";	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r3 = r9.toString();	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        r2 = r2.toString();	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        com.android.gallery3d.util.GalleryLog.d(r1, r2);	 Catch:{ Exception -> 0x004c, all -> 0x0070 }
        com.android.gallery3d.common.Utils.closeSilently(r8);
        r0 = r6;
        goto L_0x0049;
    L_0x0070:
        r1 = move-exception;
        com.android.gallery3d.common.Utils.closeSilently(r8);
        throw r1;
    L_0x0075:
        r0 = r6;
        goto L_0x0046;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.gallery3d.data.PhotoShareSource.createCloudLocalAlbum(com.android.gallery3d.data.Path):com.android.gallery3d.data.CloudLocalAlbum");
    }

    public PhotoShareSource(GalleryApp context) {
        super("photoshare");
        this.mApplication = context;
        this.mMatcher.add("/photoshare/image", 0);
        this.mMatcher.add("/photoshare/video", 1);
        this.mMatcher.add("/photoshare/all", 2);
        this.mMatcher.add("/photoshare/myshare/*", 3);
        this.mMatcher.add("/photoshare/classify", 4);
        this.mMatcher.add("/photoshare/local", 5);
        this.mMatcher.add("/photoshare/image/share/preview/*", 10);
        this.mMatcher.add("/photoshare/video/share/preview/*", 12);
        this.mMatcher.add("/photoshare/all/share/preview/*", 14);
        this.mMatcher.add("/photoshare/local/*", 20);
        this.mMatcher.add("/photoshare/image/share/nopreview/*", 10);
        this.mMatcher.add("/photoshare/video/share/nopreview/*", 12);
        this.mMatcher.add("/photoshare/all/share/nopreview/*", 14);
        this.mMatcher.add("/photoshare/classify/*", 44);
        this.mMatcher.add("/photoshare/exclude/*", 46);
        this.mMatcher.add("/photoshare/classify/*/*", 45);
        this.mMatcher.add("/photoshare/downup/*", 33);
        this.mMatcher.add("/photoshare/item/image/*/*", 100);
        this.mMatcher.add("/photoshare/item/video/*/*", 101);
        this.mMatcher.add("/photoshare/up/*/*", SmsCheckResult.ESCT_201);
        this.mMatcher.add("/photoshare/down/*/*", SmsCheckResult.ESCT_200);
        this.mMatcher.add("/photoshare/guid/down/*", 250);
        this.mMatcher.add("/photoshare/classify/*/*/*/*", 300);
        this.mMatcher.add("/photoshare/categorycover/*/*/*", SmsCheckResult.ESCT_301);
        this.mMatcher.add("/photoshare/tagcover/*/*/*/*", SmsCheckResult.ESCT_302);
        this.mMatcher.add("/photoshare/category/*", 500);
    }

    public static Path convertToNoPreView(Path sourcePath) {
        String albumId = sourcePath.getSuffix();
        Path parent = sourcePath.getParent();
        if ("preview".equals(parent.getSuffix())) {
            return parent.getParent().getChild("nopreview").getChild(albumId);
        }
        return sourcePath;
    }

    @SuppressWarnings({"SF_SWITCH_FALLTHROUGH"})
    public MediaObject createMediaObject(Path path) {
        CreateObjectVars vars = new CreateObjectVars();
        int pathType = this.mMatcher.match(path);
        vars.setIsImage(pathType);
        switch (pathType) {
            case 0:
            case 1:
            case 2:
            case 3:
                return new PhotoShareAlbumSet(path, this.mApplication);
            case 4:
                return new PhotoShareCategoryAlbumSet(path, this.mApplication);
            case 5:
                return new CloudLocalAlbumSet(path, this.mApplication);
            case 10:
            case 12:
            case 14:
                return createShareAllMediaObject(path, vars);
            case 20:
                return createCloudLocalAlbum(path);
            case 33:
                return new PhotoShareDownUpAlbum(path, this.mApplication);
            case 44:
                return createClassifyAlbum(path);
            case 45:
                return createTagAlbum(path);
            case 46:
                return createClassifyExcluedAlbum(path);
            case 100:
            case 101:
                return createVideoItem(path, vars);
            case SmsCheckResult.ESCT_200 /*200*/:
                return createDownItem(path);
            case SmsCheckResult.ESCT_201 /*201*/:
                return createUpItem(path);
            case 250:
                return createGuidDownItem(path);
            case 300:
            case SmsCheckResult.ESCT_302 /*302*/:
                return createTagFileItem(path, vars, pathType);
            case SmsCheckResult.ESCT_301 /*301*/:
                vars.setClassifyCoverAlbumType(this.mApplication, path);
                return new PhotoShareCategoryCover(path, this.mApplication, vars.fileInfo, vars.albumType, vars.albumName);
            case 500:
                String[] segments = path.split();
                if (segments.length >= 3) {
                    return new PhotoShareCategory(path, this.mApplication, Path.splitSequence(segments[2]));
                }
                throw new RuntimeException("bad path: " + path);
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }

    private MediaObject createShareAllMediaObject(Path path, CreateObjectVars vars) {
        ShareInfo shareInfo = null;
        int stateType = "nopreview".equals(path.getParent().getSuffix()) ? 2 : 1;
        try {
            shareInfo = PhotoShareUtils.getServer().getShare(path.getSuffix());
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        if (shareInfo == null) {
            throw new RuntimeException("bad path: " + path);
        }
        PhotoShareAlbum album;
        vars.setShareAlbumType(shareInfo);
        if (vars.albumType == 7 || vars.albumType == 3 || vars.albumType == 2) {
            album = new PhotoShareTimeBucketAlbum(path, this.mApplication, vars.mediaType, new PhotoShareShareAlbumInfo(shareInfo));
        } else {
            album = new PhotoShareAlbum(path, this.mApplication, vars.mediaType, stateType, new PhotoShareShareAlbumInfo(shareInfo));
        }
        album.setAlbumType(vars.albumType);
        return album;
    }

    private MediaObject createVideoItem(Path path, CreateObjectVars vars) {
        String albumID = path.getParent().getSuffix();
        ShareInfo shareInfo = null;
        try {
            shareInfo = PhotoShareUtils.getServer().getShare(albumID);
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        if (shareInfo == null) {
            throw new RuntimeException("bad path: " + path);
        }
        String albumName = shareInfo.getShareName();
        vars.setMyShareAlbumType(shareInfo);
        FileInfo fileInfo = null;
        try {
            fileInfo = PhotoShareUtils.getServer().getShareFileInfo(albumID, path.getSuffix());
            if (fileInfo == null && vars.albumType == 2) {
                fileInfo = PhotoShareUtils.getServer().getSharePreFileInfo(albumID, path.getSuffix());
            }
        } catch (RemoteException e2) {
            PhotoShareUtils.dealRemoteException(e2);
        }
        if (fileInfo == null) {
            throw new RuntimeException("bad path: " + path);
        } else if (vars.isImage) {
            return new PhotoShareImage(path, this.mApplication, fileInfo, vars.albumType, albumName);
        } else {
            return new PhotoShareVideo(path, this.mApplication, fileInfo, vars.albumType, albumName);
        }
    }

    private MediaObject createDownItem(Path path) {
        FileInfoDetail detail = null;
        try {
            detail = PhotoShareUtils.getServer().getDownloadManualFileInfo(path.getParent().getSuffix(), path.getSuffix());
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        if (detail != null) {
            return new PhotoShareDownUpItem(path, true, detail, this.mApplication);
        }
        throw new RuntimeException("bad path: " + path);
    }

    private MediaObject createGuidDownItem(Path path) {
        FileInfoDetail fileInfoDetail = null;
        try {
            fileInfoDetail = PhotoShareUtils.getServer().getDownloadFileInfoByUniqueId(path.getSuffix());
            if (fileInfoDetail == null) {
                throw new RuntimeException("bad path: " + path);
            }
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        return new PhotoShareDownUpItem(path, true, fileInfoDetail, this.mApplication);
    }

    private MediaObject createUpItem(Path path) {
        FileInfoDetail detail = null;
        try {
            detail = PhotoShareUtils.getServer().getUploadManualFileInfo(path.getParent().getSuffix(), path.getSuffix());
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        if (detail != null) {
            return new PhotoShareDownUpItem(path, false, detail, this.mApplication);
        }
        throw new RuntimeException("bad path: " + path);
    }

    private MediaObject createClassifyAlbum(Path path) {
        CategoryInfo categoryInfo = null;
        try {
            categoryInfo = PhotoShareUtils.getServer().getCategoryInfo(path.getSuffix());
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        if (categoryInfo != null) {
            return new PhotoShareCategoryAlbum(path, this.mApplication, categoryInfo);
        }
        throw new RuntimeException("bad path: " + path);
    }

    private MediaObject createClassifyExcluedAlbum(Path path) {
        CategoryInfo categoryInfo = null;
        try {
            categoryInfo = PhotoShareUtils.getServer().getCategoryInfo("0");
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        if (categoryInfo == null) {
            throw new RuntimeException("bad path: " + path);
        }
        return new PhotoShareCategoryAlbum(path, this.mApplication, categoryInfo, true, path.getSuffix());
    }

    private MediaObject createTagAlbum(Path path) {
        TagInfo tagInfo = null;
        try {
            tagInfo = PhotoShareUtils.getServer().getTagInfo(path.getParent().getSuffix(), path.getSuffix());
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        if (tagInfo != null) {
            return new PhotoShareTagAlbum(path, this.mApplication, tagInfo);
        }
        throw new RuntimeException("bad path: " + path);
    }

    private MediaObject createTagFileItem(Path path, CreateObjectVars vars, int pathType) {
        TagFileInfo tagFileInfo = null;
        FileInfo fileInfo = null;
        TagFileInfo input = new TagFileInfo();
        input.setCategoryId(path.getParent().getParent().getParent().getSuffix());
        input.setTagId(path.getParent().getParent().getSuffix());
        input.setHash(path.getParent().getSuffix());
        input.setFaceId(path.getSuffix());
        try {
            tagFileInfo = PhotoShareUtils.getServer().getTagFileInfo(input);
        } catch (RemoteException e) {
            PhotoShareUtils.dealRemoteException(e);
        }
        if (tagFileInfo == null) {
            throw new RuntimeException("bad path: " + path);
        }
        String hash = tagFileInfo.getHash();
        List tempList = null;
        try {
            tempList = PhotoShareUtils.getServer().getShareFileInfoListByHash(new String[]{hash});
        } catch (RemoteException e2) {
            PhotoShareUtils.dealRemoteException(e2);
        }
        if (tempList != null && tempList.size() > 0) {
            fileInfo = (FileInfo) tempList.get(0);
            vars.setTagFileAlbumType(tempList);
        }
        if (fileInfo == null) {
            fileInfo = new FileInfo();
            GalleryLog.v("PhotoShareSource", "TAGFILE_ITEM not find fileInfo " + path);
        }
        String albumName = fileInfo.getAlbumId() == null ? fileInfo.getShareId() : fileInfo.getAlbumId();
        if (pathType == 300) {
            return new PhotoShareTagFile(path, this.mApplication, fileInfo, vars.albumType, albumName, tagFileInfo);
        }
        return new PhotoShareTagCover(path, this.mApplication, fileInfo, vars.albumType, albumName, tagFileInfo);
    }
}
