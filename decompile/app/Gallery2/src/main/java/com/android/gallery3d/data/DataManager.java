package com.android.gallery3d.data;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.app.StitchingChangeListener;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import com.android.gallery3d.data.MediaSource.PathId;
import com.android.gallery3d.settings.HicloudAccountReceiver;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.PasteWorker;
import com.android.gallery3d.util.PasteWorker.PasteEventHandler;
import com.huawei.gallery.extfile.FyuseFile;
import com.huawei.gallery.photorectify.RectifyUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.threedmodel.ThreeDModelImageUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.WeakHashMap;

public class DataManager implements StitchingChangeListener {
    public static final Object LOCK = new Object();
    private static final String TOP_NO_VIRTUAL_IMAGE_SET_PATH = ("/combo/{" + (ApiHelper.HAS_MTP ? "/mtp," : "") + "/local/image" + "}");
    private static final String TOP_SET_PATH = ("/combo/{" + (ApiHelper.HAS_MTP ? "/mtp," : "") + "/local/all/outside,/local/all/inside" + "}");
    public static final Comparator<MediaItem> sDateTakenComparator = new DateTakenComparator();
    private int mActiveCount = 0;
    private GalleryApp mApplication;
    private final Handler mDefaultMainHandler;
    private HashMap<Uri, NotifyBroker> mNotifierMap = new HashMap();
    private PasteWorker mPasteWorker;
    private HashMap<Uri, ReloadNotifyBroker> mReloadNotifierMap = new HashMap();
    private HashMap<String, MediaSource> mSourceMap = new LinkedHashMap();

    private static class DateTakenComparator implements Comparator<MediaItem> {
        private DateTakenComparator() {
        }

        public int compare(MediaItem item1, MediaItem item2) {
            return -Utils.compare(item1.getDateInMs(), item2.getDateInMs());
        }
    }

    private static class NotifyBroker extends ContentObserver {
        private WeakHashMap<ChangeNotifier, Object> mNotifiers = new WeakHashMap();

        public NotifyBroker(Handler handler) {
            super(handler);
        }

        public synchronized void registerNotifier(ChangeNotifier notifier) {
            this.mNotifiers.put(notifier, null);
        }

        public synchronized void onChange(boolean selfChange) {
            for (ChangeNotifier notifier : this.mNotifiers.keySet()) {
                notifier.onChange(selfChange);
            }
        }
    }

    private static class ReloadNotifyBroker {
        private WeakHashMap<IReloadNotifier, Object> mNotifiers;

        private ReloadNotifyBroker() {
            this.mNotifiers = new WeakHashMap();
        }

        public synchronized void registerNotifier(IReloadNotifier notifier) {
            this.mNotifiers.put(notifier, null);
        }

        public synchronized void reload(int reloadType) {
            for (IReloadNotifier notifier : this.mNotifiers.keySet()) {
                notifier.onChange(reloadType);
            }
        }
    }

    public static String getTopOutSideSetPath() {
        String str;
        StringBuilder append = new StringBuilder().append("/combo/{/virtual/all/photoshare,/virtual/all/camera,/virtual/all/camera_video,").append(RectifyUtils.isRectifyNativeSupport() ? "/virtual/all/doc_rectify," : "").append(ThreeDModelImageUtils.isThreeDModelImageNativeSupport() ? "/virtual/all/3d_model_image," : "").append(FyuseFile.isSupport3DPanorama() ? "/virtual/all/3d_panorama," : "").append("/virtual/all/favorite,");
        if (GalleryUtils.isScreenRecorderExist()) {
            str = "/virtual/all/screenshots,/virtual/all/screenshots_video,";
        } else {
            str = "";
        }
        append = append.append(str);
        if (ApiHelper.HAS_MTP) {
            str = "/mtp,";
        } else {
            str = "";
        }
        append = append.append(str).append("/local/all/outside,/virtual/all/other,");
        if (RecycleUtils.supportRecycle()) {
            str = "/virtual/all/recycle";
        } else {
            str = "";
        }
        return append.append(str).append("}").toString();
    }

    public static String getTopOutSideSetPathExcludeCloudAndRecycleAlbum() {
        String str;
        StringBuilder append = new StringBuilder().append("/combo/{/virtual/all/camera,/virtual/all/camera_video,").append(RectifyUtils.isRectifyNativeSupport() ? "/virtual/all/doc_rectify," : "").append(ThreeDModelImageUtils.isThreeDModelImageNativeSupport() ? "/virtual/all/3d_model_image," : "").append(FyuseFile.isSupport3DPanorama() ? "/virtual/all/3d_panorama," : "").append("/virtual/all/favorite,");
        if (GalleryUtils.isScreenRecorderExist()) {
            str = "/virtual/all/screenshots,/virtual/all/screenshots_video,";
        } else {
            str = "";
        }
        append = append.append(str);
        if (ApiHelper.HAS_MTP) {
            str = "/mtp,";
        } else {
            str = "";
        }
        return append.append(str).append("/local/all/outside,/virtual/all/other").append("}").toString();
    }

    private static String getTopImageSetPath() {
        String str;
        StringBuilder append = new StringBuilder().append("/combo/{").append(ApiHelper.HAS_MTP ? "/mtp," : "").append("/virtual/image/camera,").append(RectifyUtils.isRectifyNativeSupport() ? "/virtual/image/doc_rectify," : "").append(ThreeDModelImageUtils.isThreeDModelImageNativeSupport() ? "/virtual/all/3d_model_image," : "");
        if (FyuseFile.isSupport3DPanorama()) {
            str = "/virtual/image/3d_panorama,";
        } else {
            str = "";
        }
        append = append.append(str).append("/virtual/image/favorite,");
        if (GalleryUtils.isScreenRecorderExist()) {
            str = "/virtual/image/screenshots,";
        } else {
            str = "";
        }
        return append.append(str).append("/local/image/outside,/local/image/inside").append("}").toString();
    }

    private static String getTopVideoSetPath() {
        return "/combo/{" + (ApiHelper.HAS_MTP ? "/mtp," : "") + "/virtual/all/camera_video,/virtual/video/favorite," + (GalleryUtils.isScreenRecorderExist() ? "/virtual/all/screenshots_video," : "") + "/local/video/outside,/local/video/inside" + "}";
    }

    private static String getTopLocalVideoSetPath() {
        return "/combo/{/virtual/all/camera_video,/virtual/video/favorite," + (GalleryUtils.isScreenRecorderExist() ? "/virtual/all/screenshots_video," : "") + "/local/video/outside,/local/video/inside" + "}";
    }

    private static String getTopLocalAndMergeCardImageSetPath() {
        return "/combo/{" + (ApiHelper.HAS_MTP ? "/mtp," : "") + "/virtual/image/camera," + (GalleryUtils.isScreenRecorderExist() ? "/virtual/image/screenshots," : "") + "/local/image/outside,/local/image/inside" + "}";
    }

    private static String getTopLocalAndMergeCardSetPath() {
        return "/combo/{/virtual/all/camera," + (GalleryUtils.isScreenRecorderExist() ? "/virtual/all/screenshots," : "") + "/local/all/outside,/local/all/inside" + "}";
    }

    public static String getTopPasteSetPath() {
        return "/combo/{/local/all/camerapaste," + (GalleryUtils.isScreenRecorderExist() ? "/local/all/screenshotspaste," : "") + "/local/all/outside/paste,/local/all/inside/paste" + "}";
    }

    public DataManager(GalleryApp application) {
        this.mApplication = application;
        this.mDefaultMainHandler = new Handler(application.getMainLooper());
        this.mPasteWorker = new PasteWorker(application, this);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void initializeSourceMap() {
        if (this.mSourceMap.isEmpty()) {
            addSource(new LocalSource(this.mApplication));
            if (ApiHelper.HAS_MTP) {
                addSource(new MtpSource(this.mApplication));
            }
            addSource(new VirtualSource(this.mApplication));
            addSource(new ComboSource(this.mApplication));
            addSource(new ClusterSource(this.mApplication));
            addSource(new KeyguardSource(this.mApplication));
            addSource(new FilterSource(this.mApplication));
            addSource(new SecureSource(this.mApplication));
            addSource(new UriSource(this.mApplication));
            addSource(new SnailSource(this.mApplication));
            addSource(new PhotoShareSource(this.mApplication));
            addSource(new GallerySource(this.mApplication));
            if (this.mActiveCount > 0) {
                for (MediaSource source : this.mSourceMap.values()) {
                    source.resume();
                }
            }
        }
    }

    public String getTopSetPath(int typeBits) {
        switch (typeBits) {
            case 1:
                return getTopImageSetPath();
            case 2:
                return getTopVideoSetPath();
            case 3:
                return TOP_SET_PATH;
            case HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_LOGOUT /*65536*/:
                return TOP_NO_VIRTUAL_IMAGE_SET_PATH;
            case 131072:
                return getTopOutSideSetPath();
            case 262144:
                return "/combo/{/local/all/inside}";
            case 393216:
                return TOP_SET_PATH;
            case 524288:
                return getTopImageSetPath();
            case 524292:
                return "/combo/{/local/image/outside,/local/image/inside}";
            case 1048576:
                return getTopVideoSetPath();
            case 1048580:
                return getTopLocalVideoSetPath();
            case 1572868:
                return "/combo/{/local/all/outside,/local/all/inside}";
            case 2097152:
                return getTopLocalAndMergeCardSetPath();
            case 4194304:
                return getTopLocalAndMergeCardImageSetPath();
            case 8388608:
                return "/combo/{/local/all/outside/hidden,/local/all/inside/hidden}";
            case 16777216:
                return getTopPasteSetPath();
            default:
                throw new IllegalArgumentException();
        }
    }

    void addSource(MediaSource source) {
        if (source != null) {
            this.mSourceMap.put(source.getPrefix(), source);
        }
    }

    public MediaObject peekMediaObject(Path path) {
        return path.getObject();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public MediaObject getMediaObject(Path path) {
        synchronized (LOCK) {
            MediaObject obj = path.getObject();
            if (obj != null) {
                return obj;
            }
            MediaSource source = (MediaSource) this.mSourceMap.get(path.getPrefix());
            if (source == null) {
                GalleryLog.w("DataManager", "cannot find media source for path: " + path);
                return null;
            }
            try {
                MediaObject object = source.createMediaObject(path);
                if (object == null) {
                    GalleryLog.w("DataManager", "cannot create media object: " + path);
                }
            } catch (Throwable t) {
                GalleryLog.w("DataManager", "exception in creating media object: " + path + "." + t.getMessage());
                return null;
            }
        }
    }

    public MediaObject getMediaObject(String s) {
        return getMediaObject(Path.fromString(s));
    }

    public MediaSet getMediaSet(Path path) {
        return (MediaSet) getMediaObject(path);
    }

    public MediaSet getMediaSet(String s) {
        return (MediaSet) getMediaObject(s);
    }

    public MediaSet[] getMediaSetsFromString(String segment) {
        String[] seq = Path.splitSequence(segment);
        ArrayList<MediaSet> result = new ArrayList();
        for (String mediaSet : seq) {
            MediaSet set = getMediaSet(mediaSet);
            if (set != null) {
                result.add(set);
            }
        }
        if (result.size() == 0) {
            return null;
        }
        return (MediaSet[]) result.toArray(new MediaSet[result.size()]);
    }

    public void mapMediaItems(ArrayList<Path> list, ItemConsumer consumer, int startIndex) {
        HashMap<String, ArrayList<PathId>> map = new HashMap();
        int n = list.size();
        for (int i = 0; i < n; i++) {
            Path path = (Path) list.get(i);
            String prefix = path.getPrefix();
            ArrayList<PathId> group = (ArrayList) map.get(prefix);
            if (group == null) {
                group = new ArrayList();
                map.put(prefix, group);
            }
            group.add(new PathId(path, i + startIndex));
        }
        for (Entry<String, ArrayList<PathId>> entry : map.entrySet()) {
            ((MediaSource) this.mSourceMap.get((String) entry.getKey())).mapMediaItems((ArrayList) entry.getValue(), consumer);
        }
    }

    public void delete(Path path) {
        getMediaObject(path).delete();
    }

    public boolean delete(Path path, int deleteFlag) {
        return getMediaObject(path).delete(deleteFlag);
    }

    public void moveIN(Path path) {
        getMediaObject(path).moveIN();
    }

    public void moveOUT(Path path) {
        getMediaObject(path).moveOUT();
    }

    public boolean rename(Path path, String newName) {
        return getMediaObject(path).rename(newName);
    }

    public void setAsFavorite(Path path, Context context) {
        ((MediaItem) getMediaObject(path)).setAsFavorite(context);
    }

    public void cancelFavorite(Path path, Context context) {
        ((MediaItem) getMediaObject(path)).cancelFavorite(context);
    }

    public void editPhotoShare(Context context, Path path) {
        getMediaObject(path).editPhotoShare(context);
    }

    public void removeFromStoryAlbum(Path path, String code) {
        getMediaObject(path).removeFromStoryAlbum(code);
    }

    public boolean initPaste(Bundle data, PasteEventHandler eventHandler, ArrayList<Path> processList) {
        return this.mPasteWorker.initPaste(data, eventHandler, processList);
    }

    public void paste(Path path, Bundle data, PasteEventHandler eventHandler) {
        MediaObject mediaObject = getMediaObject(path);
        if (mediaObject != null) {
            this.mPasteWorker.paste(mediaObject, data, eventHandler);
        }
    }

    public void onPasteCanceled(Bundle data, PasteEventHandler eventHandler) {
        this.mPasteWorker.onPasteCanceled(data, eventHandler);
    }

    public void onPasteComplete(PasteEventHandler eventHandler) {
        this.mPasteWorker.onPasteComplete(eventHandler);
    }

    public void rotate(Path path, int degrees) {
        getMediaObject(path).rotate(degrees);
    }

    public Uri getContentUri(Path path) {
        return getMediaObject(path).getContentUri();
    }

    public int getMediaType(Path path) {
        return getMediaObject(path).getMediaType();
    }

    public Path findPathByUri(Uri uri, String type) {
        if (uri == null) {
            return null;
        }
        for (MediaSource source : this.mSourceMap.values()) {
            Path path = source.findPathByUri(uri, type);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    public Path getDefaultSetOf(Path item) {
        Path path = null;
        if (item == null) {
            return null;
        }
        MediaSource source = (MediaSource) this.mSourceMap.get(item.getPrefix());
        if (source != null) {
            path = source.getDefaultSetOf(item);
        }
        return path;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void registerChangeNotifier(Uri uri, ChangeNotifier notifier) {
        Throwable th;
        synchronized (this.mNotifierMap) {
            try {
                NotifyBroker broker = (NotifyBroker) this.mNotifierMap.get(uri);
                if (broker == null) {
                    NotifyBroker broker2 = new NotifyBroker(this.mDefaultMainHandler);
                    try {
                        this.mApplication.getContentResolver().registerContentObserver(uri, true, broker2);
                        this.mNotifierMap.put(uri, broker2);
                        broker = broker2;
                    } catch (Throwable th2) {
                        th = th2;
                        broker = broker2;
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public void notifyChange(Uri uri) {
        synchronized (this.mNotifierMap) {
            NotifyBroker broker = (NotifyBroker) this.mNotifierMap.get(uri);
            if (broker == null) {
                return;
            }
            broker.onChange(true);
        }
    }

    public void notifyChange() {
        synchronized (this.mNotifierMap) {
            for (Uri uri : this.mNotifierMap.keySet()) {
                NotifyBroker broker = (NotifyBroker) this.mNotifierMap.get(uri);
                if (broker == null) {
                    return;
                }
                broker.onChange(true);
            }
        }
    }

    public void resume() {
        int i = this.mActiveCount + 1;
        this.mActiveCount = i;
        if (i == 1) {
            for (MediaSource source : this.mSourceMap.values()) {
                source.resume();
            }
        }
    }

    public void pause() {
        int i = this.mActiveCount - 1;
        this.mActiveCount = i;
        if (i == 0) {
            for (MediaSource source : this.mSourceMap.values()) {
                source.pause();
            }
        }
    }

    public void broadcastLocalDeletion() {
        LocalBroadcastManager.getInstance(this.mApplication.getAndroidContext()).sendBroadcast(new Intent("com.android.gallery3d.action.DELETE_PICTURE"));
    }

    public void notifyReload(Uri uri, int reloadType) {
        synchronized (this.mReloadNotifierMap) {
            ReloadNotifyBroker broker = (ReloadNotifyBroker) this.mReloadNotifierMap.get(uri);
            if (broker == null) {
                return;
            }
            broker.reload(reloadType);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void registerReloadNotifier(Uri uri, IReloadNotifier notifier) {
        Throwable th;
        synchronized (this.mReloadNotifierMap) {
            try {
                ReloadNotifyBroker broker = (ReloadNotifyBroker) this.mReloadNotifierMap.get(uri);
                if (broker == null) {
                    ReloadNotifyBroker broker2 = new ReloadNotifyBroker();
                    try {
                        this.mReloadNotifierMap.put(uri, broker2);
                        broker = broker2;
                    } catch (Throwable th2) {
                        th = th2;
                        broker = broker2;
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    public boolean ignoreForward(Path albumPath, String originalSetPathString) {
        boolean z = true;
        if (albumPath == null || originalSetPathString == null) {
            return false;
        }
        Path originalSetPath = Path.fromString(originalSetPathString);
        if (!"local".equalsIgnoreCase(albumPath.getPrefix()) || !"local".equalsIgnoreCase(originalSetPath.getPrefix())) {
            return false;
        }
        try {
            try {
                if (Integer.parseInt(albumPath.getSuffix()) != Integer.parseInt(originalSetPath.getSuffix())) {
                    z = false;
                }
                return z;
            } catch (NumberFormatException e) {
                return false;
            }
        } catch (NumberFormatException e2) {
            return true;
        }
    }
}
