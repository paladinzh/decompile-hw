package com.android.gallery3d.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import com.android.gallery3d.util.GalleryUtils;
import java.util.ArrayList;

public class UriAlbum extends MediaSet {
    private ArrayList<Path> mAllItems = new ArrayList();
    private Context mContext;
    private DataManager mDataManager;
    private boolean mDirty = true;
    private ArrayList<String> mOutputs = new ArrayList();

    public UriAlbum(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber());
        this.mContext = application.getAndroidContext();
        this.mDataManager = application.getDataManager();
    }

    public void initMediaItem(Intent intent) {
        this.mAllItems.clear();
        this.mDirty = true;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                ArrayList<String> uriList = bundle.getStringArrayList("key-item-uri-list");
                if (uriList != null && uriList.size() != 0) {
                    this.mOutputs = bundle.getStringArrayList("key-item-uri-output-list");
                    String contentType = GalleryUtils.getContentType(this.mContext, intent);
                    for (String uriString : uriList) {
                        if (uriString != null) {
                            Path path = this.mDataManager.findPathByUri(GalleryUtils.convertFileUriToContentUri(this.mContext, Uri.parse(uriString)), contentType);
                            if (!(path == null || this.mAllItems.contains(path))) {
                                this.mAllItems.add(path);
                            }
                        }
                    }
                }
            }
        }
    }

    public void addItemPath(Path path) {
        if (!(path == null || this.mAllItems.contains(path))) {
            this.mAllItems.add(path);
            this.mDirty = true;
        }
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        int size = this.mAllItems.size();
        if (start >= size + 1) {
            return new ArrayList();
        }
        int end = Math.min(start + count, size);
        final MediaItem[] buf = new MediaItem[(end - start)];
        this.mDataManager.mapMediaItems(new ArrayList(this.mAllItems.subList(start, end)), new ItemConsumer() {
            public void consume(int index, MediaItem item) {
                buf[index] = item;
            }
        }, 0);
        ArrayList<MediaItem> result = new ArrayList(end - start);
        for (int i = 0; i < buf.length; i++) {
            MediaItem item = buf[i];
            int index = i + start;
            if (!(item == null || this.mOutputs == null || index >= this.mOutputs.size())) {
                item.setOutputUri(Uri.parse((String) this.mOutputs.get(index)));
            }
            result.add(item);
        }
        return result;
    }

    public int getMediaItemCount() {
        return this.mAllItems.size();
    }

    public String getName() {
        return "uri album";
    }

    public long reload() {
        if (this.mDirty) {
            this.mDataVersion = MediaObject.nextVersionNumber();
            this.mDirty = false;
        }
        return this.mDataVersion;
    }

    public boolean isLeafAlbum() {
        return true;
    }
}
