package com.android.gallery3d.data;

import android.content.ContentProviderClient;
import android.net.Uri;
import android.util.SparseArray;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import com.android.gallery3d.data.MediaSource.IdComparator;
import com.android.gallery3d.data.MediaSource.PathId;
import com.android.gallery3d.util.GalleryLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GallerySource extends MediaSource {
    private static final /* synthetic */ int[] -com-android-gallery3d-data-GallerySource$CodePathSwitchesValues = null;
    static int sCodeIndex = 0;
    public static final Comparator<PathId> sIdComparator = new IdComparator();
    private GalleryApp mApplication;
    private ContentProviderClient mClient;
    private PathMatcher mMatcher;
    private SparseArray<CodePath> mPathCode = new SparseArray(20);

    public enum CodePath {
        GALLERY_TIMEGROUP_ALBUM("/gallery/album/timebucket"),
        GALLERY_VIDEO_ITEM("/gallery/video/item/*"),
        GALLERY_IMAGE_ITEM("/gallery/image/item/*"),
        GALLERY_ALBUM_SET_PLACE("/gallery/album/place"),
        GALLERY_ALBUM_PLACE("/gallery/album/place/*"),
        GALLERY_ALBUM_SET_STORY("/gallery/album/story"),
        GALLERY_ALBUM_STORY("/gallery/album/story/*"),
        GALLERY_IMAGE_RECYCLE("/gallery/recycle/image/item/*"),
        GALLERY_VIDEO_RECYCLE("/gallery/recycle/video/item/*"),
        NONE("/gallery/none");
        
        public final int code;
        public final String path;

        private CodePath(String p) {
            int i = GallerySource.sCodeIndex;
            GallerySource.sCodeIndex = i + 1;
            this.code = i;
            this.path = p;
        }
    }

    private static /* synthetic */ int[] -getcom-android-gallery3d-data-GallerySource$CodePathSwitchesValues() {
        if (-com-android-gallery3d-data-GallerySource$CodePathSwitchesValues != null) {
            return -com-android-gallery3d-data-GallerySource$CodePathSwitchesValues;
        }
        int[] iArr = new int[CodePath.values().length];
        try {
            iArr[CodePath.GALLERY_ALBUM_PLACE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CodePath.GALLERY_ALBUM_SET_PLACE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CodePath.GALLERY_ALBUM_SET_STORY.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CodePath.GALLERY_ALBUM_STORY.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CodePath.GALLERY_IMAGE_ITEM.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[CodePath.GALLERY_IMAGE_RECYCLE.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[CodePath.GALLERY_TIMEGROUP_ALBUM.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[CodePath.GALLERY_VIDEO_ITEM.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[CodePath.GALLERY_VIDEO_RECYCLE.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[CodePath.NONE.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        -com-android-gallery3d-data-GallerySource$CodePathSwitchesValues = iArr;
        return iArr;
    }

    public GallerySource(GalleryApp context) {
        super("gallery");
        this.mApplication = context;
        this.mMatcher = new PathMatcher();
        for (CodePath cp : CodePath.values()) {
            this.mPathCode.put(cp.code, cp);
            this.mMatcher.add(cp.path, cp.code);
        }
    }

    public MediaObject createMediaObject(Path path) {
        GalleryApp app = this.mApplication;
        switch (-getcom-android-gallery3d-data-GallerySource$CodePathSwitchesValues()[((CodePath) this.mPathCode.get(this.mMatcher.match(path))).ordinal()]) {
            case 1:
                return new DiscoverLocation(path, this.mApplication, this.mMatcher.getVar(0));
            case 2:
                return new DiscoverLocationSet(path, this.mApplication);
            case 3:
                return new DiscoverStoryAlbumSet(path, this.mApplication);
            case 4:
                return new DiscoverStoryAlbum(path, this.mApplication, this.mMatcher.getVar(0));
            case 5:
                return new GalleryImage(path, this.mApplication, Integer.parseInt(path.getSuffix()));
            case 6:
                return new GalleryRecycleImage(path, this.mApplication, this.mMatcher.getVar(0));
            case 7:
                return new GalleryMediaTimegroupAlbum(path, app);
            case 8:
                return new GalleryVideo(path, this.mApplication, Integer.parseInt(path.getSuffix()));
            case 9:
                return new GalleryRecycleVideo(path, this.mApplication, this.mMatcher.getVar(0));
            case 10:
                return null;
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }

    public Path findPathByUri(Uri uri, String type) {
        return null;
    }

    public Path getDefaultSetOf(Path item) {
        MediaObject object = this.mApplication.getDataManager().getMediaObject(item);
        if (object instanceof LocalMediaItem) {
            return Path.fromString("/local/all").getChild(String.valueOf(((LocalMediaItem) object).getBucketId()));
        }
        return null;
    }

    public void mapMediaItems(ArrayList<PathId> list, ItemConsumer consumer) {
        ArrayList<PathId> imageList = new ArrayList();
        ArrayList<PathId> videoList = new ArrayList();
        int n = list.size();
        for (int i = 0; i < n; i++) {
            PathId pid = (PathId) list.get(i);
            Path parent = pid.path.getParent();
            if (parent == GalleryImage.IMAGE_PATH) {
                imageList.add(pid);
            } else if (parent == GalleryVideo.VIDEO_PATH) {
                videoList.add(pid);
            }
        }
        processMapMediaItems(imageList, consumer);
        processMapMediaItems(videoList, consumer);
    }

    private void processMapMediaItems(ArrayList<PathId> list, ItemConsumer consumer) {
        Collections.sort(list, sIdComparator);
        int n = list.size();
        int i = 0;
        while (i < n) {
            PathId pathID = (PathId) list.get(i);
            ArrayList<Integer> ids = new ArrayList();
            int startId = Integer.parseInt(pathID.path.getSuffix());
            ids.add(Integer.valueOf(startId));
            int j = i + 1;
            while (j < n) {
                int curId = Integer.parseInt(((PathId) list.get(j)).path.getSuffix());
                if (curId - startId >= 500) {
                    break;
                }
                ids.add(Integer.valueOf(curId));
                j++;
            }
            MediaItem[] items = GalleryMediaTimegroupAlbum.getMediaItemById(this.mApplication, ids);
            for (int k = i; k < j; k++) {
                consumer.consume(((PathId) list.get(k)).id, items[k - i]);
            }
            i = j;
        }
    }

    public void resume() {
        this.mClient = this.mApplication.getContentResolver().acquireContentProviderClient("media");
    }

    public void pause() {
        try {
            if (this.mClient != null) {
                this.mClient.release();
                this.mClient = null;
            }
        } catch (IllegalStateException e) {
            GalleryLog.w("GallerySource", "LocalSource pause get exception:" + e);
        }
    }
}
