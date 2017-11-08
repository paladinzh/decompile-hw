package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.media.StoryAlbum;
import com.huawei.gallery.media.database.MergedMedia;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

public class DiscoverStoryAlbumSet extends MediaSet implements FutureListener<ArrayList<MediaSet>> {
    private static final MyPrinter LOG = new MyPrinter("DiscoverStoryAlbumSet");
    private static final String[] PROJECTION = GalleryMediaItem.copyProjection();
    private static final Uri[] mWatchUris = new Uri[]{StoryAlbum.URI};
    private ArrayList<MediaSet> mAlbums = new ArrayList();
    private GalleryApp mApplication;
    private Formatter mFormatter = null;
    private final Handler mHandler;
    private boolean mIsLoading;
    private ArrayList<MediaSet> mLoadBuffer;
    private Future<ArrayList<MediaSet>> mLoadTask;
    private DataManager mManager;
    private final ChangeNotifier mNotifier;
    private StringBuilder mStringBuilder = new StringBuilder();

    private class AlbumsLoader extends BaseJob<ArrayList<MediaSet>> {
        private AlbumsLoader() {
        }

        public String workContent() {
            return "reload location cluster album set";
        }

        public ArrayList<MediaSet> run(JobContext jc) {
            ArrayList<MediaSet> ret = new ArrayList();
            Closeable closeable = null;
            Closeable closeable2 = null;
            ContentResolver resolver = DiscoverStoryAlbumSet.this.mApplication.getAndroidContext().getContentResolver();
            closeable = resolver.query(MergedMedia.URI.buildUpon().appendPath("cluster_view").build(), StoryAlbum.getProjectionView(), null, null, null);
            if (closeable != null) {
                while (closeable.moveToNext()) {
                    MediaSet mediaSet = DiscoverStoryAlbumSet.this.mManager.getMediaSet(DiscoverStoryAlbumSet.this.mPath.getChild(closeable.getString(0)));
                    if (mediaSet instanceof DiscoverStoryAlbum) {
                        mediaSet.setName(closeable.getString(3));
                        mediaSet.setSubName(DiscoverStoryAlbumSet.this.getDateString(closeable.getLong(1), closeable.getLong(2)));
                        int id = closeable.getInt(4);
                        ArrayList<MediaItem> list = new ArrayList();
                        closeable2 = resolver.query(GalleryMedia.URI, DiscoverStoryAlbumSet.PROJECTION, "_id = ? ", new String[]{Integer.toString(id)}, null);
                        if (closeable2 == null) {
                            ArrayList<MediaSet> arrayList = null;
                            return arrayList;
                        }
                        Path path;
                        while (closeable2.moveToNext()) {
                            list.add(GalleryMediaSetBase.loadOrUpdateItem(closeable2, DiscoverStoryAlbumSet.this.mApplication.getDataManager(), DiscoverStoryAlbumSet.this.mApplication));
                        }
                        Utils.closeSilently(closeable2);
                        int size = list.size();
                        MediaItem item = size > 0 ? (MediaItem) list.get(size / 2) : null;
                        if (item == null) {
                            path = null;
                        } else {
                            try {
                                path = item.getPath();
                            } catch (RuntimeException e) {
                                arrayList = DiscoverStoryAlbumSet.LOG;
                                arrayList.w("load location album failed. " + e.getMessage());
                            } finally {
                                Utils.closeSilently(closeable);
                                Utils.closeSilently(closeable2);
                            }
                        }
                        ((DiscoverStoryAlbum) mediaSet).setCoverPath(path);
                        ret.add(mediaSet);
                    }
                }
            }
            DiscoverStoryAlbumSet.LOG.d("mediaSet size =" + ret.size());
            Utils.closeSilently(closeable);
            Utils.closeSilently(closeable2);
            return ret;
        }
    }

    public DiscoverStoryAlbumSet(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber());
        this.mManager = application.getDataManager();
        this.mApplication = application;
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mNotifier = new ChangeNotifier((MediaSet) this, mWatchUris, application);
    }

    public synchronized MediaSet getSubMediaSet(int index) {
        return (MediaSet) this.mAlbums.get(index);
    }

    public synchronized boolean isLoading() {
        return this.mIsLoading;
    }

    public synchronized int getSubMediaSetCount() {
        LOG.d("mAlbums.size=" + this.mAlbums.size());
        return this.mAlbums.size();
    }

    public synchronized ArrayList<MediaItem> getCoverItems() {
        ArrayList<MediaItem> items;
        items = new ArrayList();
        for (MediaSet set : this.mAlbums) {
            MediaItem item = set.getCoverMediaItem();
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    public long reload() {
        synchronized (this) {
            if (this.mNotifier.isDirty()) {
                if (this.mLoadTask != null) {
                    this.mLoadTask.cancel();
                }
                this.mIsLoading = true;
                TraceController.printDebugInfo("submit AlbumsLoader " + this.mPath);
                this.mLoadTask = this.mApplication.getThreadPool().submit(new AlbumsLoader(), this);
            }
            if (this.mLoadBuffer != null) {
                this.mAlbums = this.mLoadBuffer;
                this.mLoadBuffer = null;
                for (MediaSet album : this.mAlbums) {
                    album.reload();
                }
                this.mDataVersion = MediaObject.nextVersionNumber();
            }
        }
        return this.mDataVersion;
    }

    private String getDateString(long minMillis, long maxMillis) {
        if (this.mFormatter == null) {
            this.mFormatter = new Formatter(this.mStringBuilder, Locale.getDefault());
        }
        this.mStringBuilder.delete(0, this.mStringBuilder.length());
        return GalleryUtils.getFormatDateRangeString(this.mApplication.getAndroidContext(), this.mFormatter, minMillis, maxMillis).replaceAll("/", ".");
    }

    public void onFutureDone(Future<ArrayList<MediaSet>> future) {
        synchronized (this) {
            if (this.mLoadTask != future) {
                return;
            }
            this.mLoadBuffer = (ArrayList) future.get();
            if (this.mLoadBuffer == null) {
                return;
            }
            this.mIsLoading = false;
            this.mHandler.post(new Runnable() {
                public void run() {
                    DiscoverStoryAlbumSet.this.notifyContentChanged();
                }
            });
        }
    }

    public String getName() {
        return this.mApplication.getResources().getString(R.string.highlights_album_title);
    }
}
