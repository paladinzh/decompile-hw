package com.android.gallery3d.data;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.TraceController;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.media.GeoKnowledge;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;
import java.util.ArrayList;

public class DiscoverLocationSet extends MediaSet implements FutureListener<ArrayList<MediaSet>> {
    private static final MyPrinter LOG = new MyPrinter("PhotoShareCategory");
    private static final Uri[] mWatchUris = new Uri[]{GalleryMedia.URI, GeoKnowledge.URI, GalleryMedia.URI};
    private String[] PROJECTION = new String[]{"geo_code", "_data", "count(1) record_count "};
    private ArrayList<MediaSet> mAlbums = new ArrayList();
    private GalleryApp mApplication;
    private final Handler mHandler;
    private boolean mIsCloudAutoUploadSwitchOpen = false;
    private boolean mIsLoading;
    private ArrayList<MediaSet> mLoadBuffer;
    private Future<ArrayList<MediaSet>> mLoadTask;
    private DataManager mManager;
    private final ChangeNotifier mNotifier;
    private final ReloadNotifier mReloadNotifier;

    private class AlbumsLoader extends BaseJob<ArrayList<MediaSet>> {
        private AlbumsLoader() {
        }

        public String workContent() {
            return "reload location cluster album set";
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public ArrayList<MediaSet> run(JobContext jc) {
            ArrayList<MediaSet> ret = new ArrayList(10);
            try {
                String whereClause = "geo_code !='' AND geo_code IS NOT NULL AND latitude != '0.0' AND longitude != '0.0' AND " + BucketHelper.getExcludeHiddenWhereClause(DiscoverLocationSet.this.mApplication.getAndroidContext()) + ") GROUP BY 1, (1  ";
                if (!DiscoverLocationSet.this.mIsCloudAutoUploadSwitchOpen) {
                    whereClause = " local_media_id != -1 AND " + whereClause;
                }
                Closeable c = DiscoverLocationSet.this.mApplication.getAndroidContext().getContentResolver().query(GalleryMedia.URI, DiscoverLocationSet.this.PROJECTION, whereClause, null, " record_count DESC, geo_code ASC ");
                if (c != null) {
                    while (c.moveToNext()) {
                        MediaSet mediaSet = DiscoverLocationSet.this.mManager.getMediaSet(DiscoverLocationSet.this.mPath.getChild(c.getString(0)));
                        if (mediaSet != null) {
                            ret.add(mediaSet);
                        }
                    }
                }
                Utils.closeSilently(c);
            } catch (RuntimeException e) {
                DiscoverLocationSet.LOG.w("load location album failed.");
            } catch (Throwable th) {
                Utils.closeSilently(null);
            }
            return ret;
        }
    }

    public DiscoverLocationSet(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber());
        this.mManager = application.getDataManager();
        this.mApplication = application;
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mNotifier = new ChangeNotifier((MediaSet) this, mWatchUris, application);
        this.mReloadNotifier = new ReloadNotifier(this, Constant.RELOAD_DISCOVER_LOCATION, application);
    }

    public synchronized MediaSet getSubMediaSet(int index) {
        return (MediaSet) this.mAlbums.get(index);
    }

    public synchronized boolean isLoading() {
        return this.mIsLoading;
    }

    public synchronized int getSubMediaSetCount() {
        return this.mAlbums.size();
    }

    public long reload() {
        boolean isCloudChanged = false;
        if (this.mIsCloudAutoUploadSwitchOpen != CloudSwitchHelper.isCloudAutoUploadSwitchOpen()) {
            isCloudChanged = true;
            this.mIsCloudAutoUploadSwitchOpen = CloudSwitchHelper.isCloudAutoUploadSwitchOpen();
        }
        synchronized (this) {
            if ((this.mNotifier.isDirty() | this.mReloadNotifier.isDirty()) || isCloudChanged) {
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

    public synchronized void onFutureDone(Future<ArrayList<MediaSet>> future) {
        if (this.mLoadTask == future) {
            this.mLoadBuffer = (ArrayList) future.get();
            this.mIsLoading = false;
            if (this.mLoadBuffer == null) {
                GalleryLog.printDFXLog("DiscoverLocationSet mLoadBuffer is NULL for DFX");
                this.mLoadBuffer = new ArrayList();
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    DiscoverLocationSet.this.notifyContentChanged();
                }
            });
        }
    }

    public String getName() {
        return this.mApplication.getResources().getString(R.string.location);
    }
}
