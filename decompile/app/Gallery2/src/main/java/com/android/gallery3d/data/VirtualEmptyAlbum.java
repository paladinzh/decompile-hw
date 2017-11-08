package com.android.gallery3d.data;

import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import java.util.ArrayList;

public class VirtualEmptyAlbum extends MediaSet {
    private final GalleryApp mApplication;
    private String mLabel = "empty";

    public VirtualEmptyAlbum(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
    }

    public String getName() {
        return this.mApplication.getResources().getString(R.string.add_new_empty_album);
    }

    public long reload() {
        return 0;
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        return super.getMediaItem(start, count);
    }

    public int getMediaItemCount() {
        return 1;
    }

    public String getLabel() {
        return this.mLabel;
    }

    public int getSupportedOperations() {
        return 0;
    }

    public boolean isLeafAlbum() {
        return true;
    }

    public boolean isVirtual() {
        return true;
    }
}
