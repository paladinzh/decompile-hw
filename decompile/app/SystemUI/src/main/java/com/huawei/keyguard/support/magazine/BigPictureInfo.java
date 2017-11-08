package com.huawei.keyguard.support.magazine;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;
import java.io.UnsupportedEncodingException;

public class BigPictureInfo {
    public int bucketId = -1;
    private DescriptionInfo descriptionInfo = new DescriptionInfo();
    private GalleryInfo galleryInfo = new GalleryInfo();
    private IdentityInfo identityInfo = new IdentityInfo();
    public int keyId = -1;
    public int picFormat = 1;

    public static class DescriptionInfo {
        private String adcontentid;
        private String appVer = BuildConfig.FLAVOR;
        private String content = BuildConfig.FLAVOR;
        private String contentUrl = BuildConfig.FLAVOR;
        private String cpName = BuildConfig.FLAVOR;
        private String download = BuildConfig.FLAVOR;
        private String packagename = BuildConfig.FLAVOR;
        private String title = BuildConfig.FLAVOR;
        private String works = BuildConfig.FLAVOR;
        private String worksDes = BuildConfig.FLAVOR;

        public boolean hasContent() {
            return this.content.length() > 0 || this.title.length() > 0 || this.cpName.length() > 0;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("DescriptionInfo : ").append(" title = ").append(this.title).append(" content = ").append(this.content).append(" cpName = ").append(this.cpName).append(" download = ").append(this.download).append(" contentUrl = ").append(this.contentUrl).append(" packagename = ").append(this.packagename).append(" worksDes = ").append(this.worksDes).append(" works = ").append(this.works).append(" appVer = ").append(this.appVer).append(" adcontentid = ").append(this.adcontentid);
            return sb.toString();
        }

        public static DescriptionInfo parseDescription(String description) {
            DescriptionInfo info = new DescriptionInfo();
            if (TextUtils.isEmpty(description) || DescriptionHelper.isMessyDescription(description)) {
                return info;
            }
            SparseArray<String> list = new SparseArray();
            list.append(description.indexOf("<mgzn-title>"), "<mgzn-title>");
            list.append(description.indexOf("<mgzn-cpname>"), "<mgzn-cpname>");
            list.append(description.indexOf("<mgzn-download>"), "<mgzn-download>");
            list.append(description.indexOf("<mgzn-contenturi>"), "<mgzn-contenturi>");
            list.append(description.indexOf("<mgzn-pkgname>"), "<mgzn-pkgname>");
            list.append(description.indexOf("<mgzn-content>"), "<mgzn-content>");
            list.append(description.indexOf("<mgzn-works>"), "<mgzn-works>");
            list.append(description.indexOf("<mgzn-worksdes>"), "<mgzn-worksdes>");
            list.append(description.indexOf("<mgzn-appver>"), "<mgzn-appver>");
            list.append(description.indexOf("<mgzn-contentid>"), "<mgzn-contentid>");
            info.setTitle(BigPictureInfo.getSubDescription(description, list, "<mgzn-title>"));
            info.setCpName(BigPictureInfo.getSubDescription(description, list, "<mgzn-cpname>"));
            info.setDownload(BigPictureInfo.getSubDescription(description, list, "<mgzn-download>"));
            info.setContentUrl(BigPictureInfo.getSubDescription(description, list, "<mgzn-contenturi>"));
            info.setPackagename(BigPictureInfo.getSubDescription(description, list, "<mgzn-pkgname>"));
            info.setContent(BigPictureInfo.getSubDescription(description, list, "<mgzn-content>"));
            info.setWorks(BigPictureInfo.getSubDescription(description, list, "<mgzn-works>"));
            info.setWorksDes(BigPictureInfo.getSubDescription(description, list, "<mgzn-worksdes>"));
            info.setAppVer(BigPictureInfo.getSubDescription(description, list, "<mgzn-appver>"));
            info.setAdcontentid(BigPictureInfo.getSubDescription(description, list, "<mgzn-contentid>"));
            return info;
        }

        public boolean isUserCommentDescriptionValid() {
            return !TextUtils.isEmpty(new StringBuilder().append(this.title).append(this.cpName).append(this.download).append(this.contentUrl).append(this.packagename).append(this.content).append(this.appVer).append(this.adcontentid).toString());
        }

        public String getTitle() {
            return this.title;
        }

        public void setTitle(String title) {
            if (title != null) {
                title = title.trim();
            }
            this.title = title;
        }

        public String getContent() {
            return this.content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getAdcontentid() {
            return this.adcontentid;
        }

        public void setAdcontentid(String adcontentid) {
            this.adcontentid = adcontentid;
        }

        public String getAppVer() {
            return this.appVer;
        }

        private void setWorks(String works) {
            this.works = works;
        }

        public String getWorksDes() {
            return this.worksDes;
        }

        private void setAppVer(String appVer) {
            this.appVer = appVer;
        }

        private void setWorksDes(String works) {
            String worksDeatailDes = BuildConfig.FLAVOR;
            if (!TextUtils.isEmpty(this.worksDes)) {
                if (20 < this.worksDes.length()) {
                    worksDeatailDes = this.worksDes.substring(0, 20);
                } else {
                    worksDeatailDes = this.worksDes;
                }
            }
            this.worksDes = worksDeatailDes;
        }

        private void setCpName(String cpName) {
            this.cpName = cpName;
        }

        private void setDownload(String download) {
            this.download = download;
        }

        private void setContentUrl(String contentUrl) {
            this.contentUrl = contentUrl;
        }

        private void setPackagename(String packagename) {
            this.packagename = packagename;
        }

        public String getDescriptionPackagename() {
            return this.packagename;
        }

        public String getDownload() {
            return this.download;
        }

        public String getContentUrl() {
            return this.contentUrl;
        }

        public boolean getLinkVisible() {
            return (TextUtils.isEmpty(this.contentUrl) && (TextUtils.isEmpty(this.download) || TextUtils.isEmpty(this.packagename))) ? false : true;
        }

        public String getCpName() {
            return this.cpName;
        }

        public boolean isWorksMagazineUnlock() {
            return "1".equals(this.works);
        }
    }

    public static class GalleryInfo {
        private boolean isCustom = false;
        private boolean isHidden = false;
        private boolean isNew = true;
        private boolean isPrivate = false;
        private boolean mIsFavorite = false;

        public GalleryInfo(boolean isNew, boolean isCustom, boolean isPrivate, boolean isHidden, boolean mIsFavorite) {
            this.isNew = isNew;
            this.isCustom = isCustom;
            this.isPrivate = isPrivate;
            this.isHidden = isHidden;
            this.mIsFavorite = mIsFavorite;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("GalleryInfo : ").append(" isNew = ").append(this.isNew).append(" isCustom = ").append(this.isCustom).append(" isPrivate = ").append(this.isPrivate).append(" isHidden = ").append(this.isHidden).append(" mIsFavorite = ").append(this.mIsFavorite);
            return sb.toString();
        }

        public void setFavoriteInfo(boolean favorite) {
            this.mIsFavorite = favorite;
        }

        private boolean getFavoriteInfo() {
            return this.mIsFavorite;
        }

        private boolean getIsCustom() {
            return this.isCustom;
        }
    }

    public static class IdentityInfo {
        private String channelId;
        private int picFormat;
        private String picName;
        private String picPath;
        private String picUniqueName;
        private String themeTitle;
        private String version;

        public IdentityInfo() {
            this.themeTitle = BuildConfig.FLAVOR;
            this.version = BuildConfig.FLAVOR;
            this.picName = BuildConfig.FLAVOR;
            this.picUniqueName = BuildConfig.FLAVOR;
            this.picPath = BuildConfig.FLAVOR;
            this.picFormat = -1;
        }

        public IdentityInfo(String themeTitle, String version, String picName, String picUniqueName, String picPath, String channel, int picFormat) {
            this.themeTitle = themeTitle;
            this.version = version;
            this.picName = picName;
            this.picUniqueName = picUniqueName;
            this.picPath = picPath;
            this.channelId = channel;
            this.picFormat = picFormat;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("IdentityInfo : ").append(", themeTitle = ").append(this.themeTitle).append(", version = ").append(this.version).append(", picName = ").append(this.picName).append(", picUniqueName = ").append(this.picUniqueName).append(", picPath = ").append(this.picPath).append(", channelId = ").append(this.channelId);
            return sb.toString();
        }

        private String getPicName() {
            return this.picName;
        }

        private String getPicUniqueName() {
            return this.picUniqueName;
        }

        public String getPicPath() {
            return this.picPath;
        }

        private int getPicFormat() {
            return this.picFormat;
        }

        private void setPicFormat(int picFormat) {
            this.picFormat = picFormat;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BigPictureInfo : ").append(", identityInfo = ").append(this.identityInfo).append(", descriptionInfo = ").append(this.descriptionInfo).append(", galleryInfo = ").append(this.galleryInfo);
        return sb.toString();
    }

    public boolean isFyuseFormatPic() {
        boolean z = true;
        if (!HwFyuseUtils.isSupport3DFyuse() || !HwFyuseUtils.getMagazineEnableStatus()) {
            return false;
        }
        String path = getPicPath();
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        int originType = this.identityInfo.getPicFormat();
        if (originType != -1) {
            if (originType != 11) {
                z = false;
            }
            return z;
        }
        int fileType = HwFyuseUtils.getFileType(path);
        if (fileType == 0) {
            HwLog.w("BigPictureInfo", "FILE_TYPE_NOT_EXIST");
            return false;
        }
        this.identityInfo.setPicFormat(fileType);
        HwFyuseUtils.updateSinglePicFormat(path, fileType);
        HwLog.i("BigPictureInfo", "isFyuseFormatPic getFileType: " + fileType);
        if (11 != fileType) {
            z = false;
        }
        return z;
    }

    public void setIdentityInfo(IdentityInfo info) {
        if (info != null) {
            this.identityInfo = info;
        }
    }

    public void setDescriptionInfo(DescriptionInfo info) {
        if (info != null) {
            this.descriptionInfo = info;
        }
    }

    public void setGalleryInfo(GalleryInfo info) {
        if (info != null) {
            this.galleryInfo = info;
        }
    }

    public int getPicFormat() {
        return this.identityInfo.getPicFormat();
    }

    public void setFavoriteInfo(boolean favoriteInfo) {
        this.galleryInfo.setFavoriteInfo(favoriteInfo);
    }

    public String getPicPath() {
        return this.identityInfo.getPicPath();
    }

    public String getContentUrl() {
        return this.descriptionInfo.getContentUrl();
    }

    public String getDownload() {
        return this.descriptionInfo.getDownload();
    }

    public String getBigPackagename() {
        return this.descriptionInfo.getDescriptionPackagename();
    }

    public String getAppVer() {
        return this.descriptionInfo.getAppVer();
    }

    public String getTitle() {
        return this.descriptionInfo.getTitle();
    }

    public String getCpName() {
        return this.descriptionInfo.getCpName();
    }

    public String getContent() {
        return this.descriptionInfo.getContent();
    }

    public String getPicName() {
        return this.identityInfo.getPicName();
    }

    public String getPicUniqueName() {
        return this.identityInfo.getPicUniqueName();
    }

    public DescriptionInfo getDescriptionInfo() {
        return this.descriptionInfo;
    }

    public IdentityInfo getIdentityInfo() {
        return this.identityInfo;
    }

    public String getChannelId() {
        return this.identityInfo.channelId;
    }

    public boolean getFavoriteInfo() {
        return this.galleryInfo.getFavoriteInfo();
    }

    public boolean getIsCustom() {
        return this.galleryInfo.getIsCustom();
    }

    public GalleryInfo getGalleryInfo() {
        return this.galleryInfo;
    }

    public static String getSubDescription(String description, SparseArray<String> list, String tag) {
        int index = list.indexOfValue(tag);
        int last = list.size() - 1;
        int start;
        if (index >= 0 && index < last) {
            start = list.keyAt(index) + tag.length();
            int end = list.keyAt(index + 1);
            if (start >= end || start - tag.length() < 0 || end >= description.length()) {
                return BuildConfig.FLAVOR;
            }
            return removeIllegalStr(description.substring(start, end));
        } else if (index != last) {
            return BuildConfig.FLAVOR;
        } else {
            start = list.keyAt(index) + tag.length();
            if (start - tag.length() < 0 || start >= description.length()) {
                return BuildConfig.FLAVOR;
            }
            return removeIllegalStr(description.substring(start));
        }
    }

    private static String removeIllegalStr(String subDescription) {
        if (TextUtils.isEmpty(subDescription)) {
            Log.e("BigPictureInfo", "removeIllegalStr subDescription is empty");
            return BuildConfig.FLAVOR;
        }
        try {
            String subDescriptionHex = HexDump.toHexString(subDescription.getBytes("UTF-8"));
            if (subDescriptionHex.endsWith("00")) {
                subDescription = new String(HexDump.hexStringToByteArray(subDescriptionHex.substring(0, subDescriptionHex.length() - "00".length())), "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            Log.e("BigPictureInfo", "removeIllegalStr error");
        }
        return filterDesc(subDescription);
    }

    private static String filterDesc(String desc) {
        if (TextUtils.isEmpty(desc) || desc.indexOf("<") == -1) {
            return desc;
        }
        return desc.substring(0, desc.indexOf("<"));
    }
}
