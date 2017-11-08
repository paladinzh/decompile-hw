package com.android.gallery3d.data;

import android.net.Uri;
import android.text.TextUtils;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.media.StoryAlbum;
import com.huawei.gallery.util.MyPrinter;

public class DiscoverStoryAlbum extends GalleryMediaTimegroupAlbum {
    private static final MyPrinter LOG = new MyPrinter("DiscoverStoryAlbum");
    private String mClusterCode;
    private Path mCoverPath = null;
    private String mName = "";
    private String mSubName = "";

    public DiscoverStoryAlbum(Path path, GalleryApp application, String clusterCode) {
        super(path, application);
        this.mClusterCode = clusterCode;
    }

    protected String getOrderBy() {
        return "showDateToken ASC, _id DESC";
    }

    protected Uri[] getWatchUris() {
        return new Uri[]{GalleryMedia.URI, StoryAlbum.URI};
    }

    protected void initLocalClause() {
        this.mTimeLatLng.initWhereClause(" 1 = 1", "substr(_display_name, 1, length(_display_name) - length('000.JPG')) NOT IN (SELECT substr(_display_name, 1, length(_display_name) - length('000_COVER.JPG')) FROM gallery_media WHERE media_type = 1 AND " + GalleryUtils.getBurstQueryClause() + ")");
    }

    public String getClusterCode() {
        return this.mClusterCode;
    }

    protected String getQuickClause() {
        return "";
    }

    public String getName() {
        if (TextUtils.isEmpty(this.mName)) {
            return "";
        }
        return this.mName;
    }

    public String getSubName() {
        if (TextUtils.isEmpty(this.mSubName)) {
            return "";
        }
        return this.mSubName;
    }

    public void setName(String name) {
        if (!TextUtils.isEmpty(name)) {
            this.mName = name;
        }
    }

    public boolean rename(String newName) {
        setName(newName);
        StoryAlbum.renameStoryAlbum(this.mClusterCode, newName, this.mApplication.getAndroidContext().getContentResolver());
        return true;
    }

    public void setSubName(String name) {
        if (!TextUtils.isEmpty(name)) {
            this.mSubName = name;
        }
    }

    public String getQueryClauseExtra() {
        return "story_id = " + this.mClusterCode;
    }

    public String getWhereQueryClause() {
        return super.getWhereQueryClause() + " AND " + "story_id" + " = " + this.mClusterCode;
    }

    public MediaItem getCoverMediaItem() {
        GalleryUtils.assertNotInRenderThread();
        DataManager dataManager = this.mApplication.getDataManager();
        synchronized (this) {
            Path path = this.mCoverPath;
        }
        if (path != null) {
            MediaItem item = (MediaItem) dataManager.getMediaObject(path);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    public synchronized void setCoverPath(Path path) {
        this.mCoverPath = path;
    }
}
