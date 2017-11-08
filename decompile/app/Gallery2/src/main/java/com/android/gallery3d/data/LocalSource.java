package com.android.gallery3d.data;

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.UriMatcher;
import android.net.Uri;
import com.amap.api.services.core.AMapException;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import com.android.gallery3d.data.MediaSource.IdComparator;
import com.android.gallery3d.data.MediaSource.PathId;
import com.android.gallery3d.util.GalleryLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LocalSource extends MediaSource {
    public static final Comparator<PathId> sIdComparator = new IdComparator();
    private GalleryApp mApplication;
    private ContentProviderClient mClient;
    private PathMatcher mMatcher;
    private final UriMatcher mUriMatcher = new UriMatcher(-1);

    public LocalSource(GalleryApp context) {
        super("local");
        this.mApplication = context;
        this.mMatcher = new PathMatcher();
        this.mMatcher.add("/local/image", 0);
        this.mMatcher.add("/local/video", 1);
        this.mMatcher.add("/local/all", 6);
        this.mMatcher.add("/local/album/from/camera", 20);
        this.mMatcher.add("/local/image/*", 2);
        this.mMatcher.add("/local/video/*", 3);
        this.mMatcher.add("/local/all/*", 7);
        this.mMatcher.add("/local/albumoutside", 34);
        this.mMatcher.add("/local/image/albumoutside", 36);
        this.mMatcher.add("/local/camera", 20);
        this.mMatcher.add("/local/image/camera", 27);
        this.mMatcher.add("/local/all/inside", 21);
        this.mMatcher.add("/local/all/outside", 22);
        this.mMatcher.add("/local/image/item/*", 4);
        this.mMatcher.add("/local/video/item/*", 5);
        this.mMatcher.add("/local/image/outside", 23);
        this.mMatcher.add("/local/image/inside", 25);
        this.mMatcher.add("/local/video/outside", 24);
        this.mMatcher.add("/local/video/inside", 26);
        this.mMatcher.add("/local/image/outside/*", 28);
        this.mMatcher.add("/local/image/inside/*", 30);
        this.mMatcher.add("/local/video/outside/*", 29);
        this.mMatcher.add("/local/video/inside/*", 31);
        this.mMatcher.add("/local/all/inside/*", 32);
        this.mMatcher.add("/local/all/outside/*", 33);
        this.mMatcher.add("/local/all/outside/hidden", 23);
        this.mMatcher.add("/local/all/inside/hidden", 25);
        this.mMatcher.add("/local/all/camerapaste", 35);
        this.mMatcher.add("/local/all/outside/paste", 35);
        this.mMatcher.add("/local/all/inside/paste", 35);
        this.mMatcher.add("/local/kids/camera", 37);
        this.mMatcher.add("/local/kids/paint", 38);
        this.mMatcher.add("/local/kids/media", 39);
        this.mMatcher.add("/local/kids/parent/*", 42);
        this.mMatcher.add("/local/screenshots", 40);
        this.mMatcher.add("/local/image/screenshots", 41);
        this.mMatcher.add("/local/all/screenshotspaste", 35);
        this.mUriMatcher.addURI("media", "external/images/media/#", 4);
        this.mUriMatcher.addURI("media", "external/video/media/#", 5);
        this.mUriMatcher.addURI("media", "external/images/media", 2);
        this.mUriMatcher.addURI("media", "external/video/media", 3);
        this.mUriMatcher.addURI("media", "external/file", 7);
    }

    public MediaObject createMediaObject(Path path) {
        GalleryApp app = this.mApplication;
        switch (this.mMatcher.match(path)) {
            case 0:
            case 1:
            case 6:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case AMapException.ERROR_CODE_URL /*26*/:
            case AMapException.ERROR_CODE_QUOTA /*35*/:
                return new LocalAlbumSet(path, this.mApplication);
            case 2:
            case AMapException.ERROR_CODE_UNKNOW_SERVICE /*28*/:
            case 30:
                return new LocalAlbum(path, app, this.mMatcher.getIntVar(0), true);
            case 3:
            case AMapException.ERROR_CODE_PROTOCOL /*29*/:
            case 31:
                return new LocalAlbum(path, app, this.mMatcher.getIntVar(0), false);
            case 4:
                return new LocalImage(path, this.mApplication, this.mMatcher.getIntVar(0));
            case 5:
                return new LocalVideo(path, this.mApplication, this.mMatcher.getIntVar(0));
            case 7:
            case 32:
            case 33:
                return new LocalBucketAlbum(path, app, this.mMatcher.getIntVar(0));
            case 20:
            case AMapException.ERROR_CODE_UNKNOW_HOST /*27*/:
                return new LocalCameraAlbum(path, app);
            case AMapException.ERROR_CODE_SERVER /*34*/:
                return new LocalMediaSetAlbum(path, app);
            case AMapException.ERROR_CODE_REQUEST /*36*/:
                return new LocalMediaImageSetAlbum(path, app);
            case 37:
            case 38:
            case 39:
            case 42:
                return new LocalKidsAlbum(path, app);
            case 40:
            case 41:
                return new LocalScreenshotsAlbum(path, app);
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }

    private static int getMediaType(String type, int defaultType) {
        if (type == null) {
            return defaultType;
        }
        try {
            int value = Integer.parseInt(type);
            if ((value & 5) != 0) {
                return value;
            }
            return defaultType;
        } catch (NumberFormatException e) {
            GalleryLog.w("LocalSource", "invalid type: " + type + "." + e.getMessage());
        }
    }

    private Path getAlbumPath(Uri uri, int defaultType) {
        int mediaType = getMediaType(uri.getQueryParameter("mediaTypes"), defaultType);
        String bucketId = uri.getQueryParameter("bucketId");
        try {
            int id = Integer.parseInt(bucketId);
            switch (mediaType) {
                case 1:
                    return Path.fromString("/local/image").getChild(id);
                case 4:
                    return Path.fromString("/local/video").getChild(id);
                default:
                    return Path.fromString("/local/all").getChild(id);
            }
        } catch (NumberFormatException e) {
            GalleryLog.w("LocalSource", "invalid bucket id: " + bucketId + "." + e.getMessage());
            return null;
        }
    }

    public Path findPathByUri(Uri uri, String type) {
        Path path = null;
        try {
            long id;
            switch (this.mUriMatcher.match(uri)) {
                case 2:
                    return getAlbumPath(uri, 1);
                case 3:
                    return getAlbumPath(uri, 4);
                case 4:
                    id = ContentUris.parseId(uri);
                    if (id >= 0) {
                        path = LocalImage.ITEM_PATH.getChild(id);
                    }
                    return path;
                case 5:
                    id = ContentUris.parseId(uri);
                    if (id >= 0) {
                        path = LocalVideo.ITEM_PATH.getChild(id);
                    }
                    return path;
                case 7:
                    return getAlbumPath(uri, 0);
            }
        } catch (NumberFormatException e) {
            GalleryLog.w("LocalSource", "uri: " + uri.toString() + "." + e.getMessage());
        }
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
            if (parent == LocalImage.ITEM_PATH) {
                imageList.add(pid);
            } else if (parent == LocalVideo.ITEM_PATH) {
                videoList.add(pid);
            }
        }
        processMapMediaItems(imageList, consumer, true);
        processMapMediaItems(videoList, consumer, false);
    }

    private void processMapMediaItems(ArrayList<PathId> list, ItemConsumer consumer, boolean isImage) {
        Collections.sort(list, sIdComparator);
        int n = list.size();
        int i = 0;
        while (i < n) {
            PathId pid = (PathId) list.get(i);
            ArrayList<Integer> ids = new ArrayList();
            int startId = Integer.parseInt(pid.path.getSuffix());
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
            MediaItem[] items = LocalAlbum.getMediaItemById(this.mApplication, isImage, ids);
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
            GalleryLog.w("LocalSource", "LocalSource pause get exception:" + e);
        }
    }
}
