package com.android.gallery3d.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.data.ClusterWorker.ClusterListener;
import com.android.gallery3d.data.LocationClustering.LocationInfoListener;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool.JobContext;
import java.util.ArrayList;
import java.util.HashSet;

public class ClusterAlbumSet extends MediaSet implements ContentListener, FutureListener<Void> {
    private ArrayList<ClusterAlbum> mAlbums = new ArrayList();
    private GalleryApp mApplication;
    private MediaSet mBaseSet;
    private Clusters mClusters;
    private ConnectivityManager mConnectivityManager;
    private IntentFilter mFilter;
    private boolean mFirstReloadDone;
    private Future<Void> mFuture;
    private Handler mHandler;
    private boolean mIsLoading = false;
    private int mKind;
    private LocationClustering mLocationClustering;
    private MyLocationInfoListener mMyLocationInfoListener;
    private boolean mNetworkChangeReload = false;
    private boolean mNetworkConnnect = false;
    private NetworkInfo mNetworkInfo;
    private BroadcastReceiver mReceiver;
    private boolean mUseCluster = false;

    private class MyLocationInfoListener implements LocationInfoListener {
        private ClusterAlbumSet mClusterAlbumset;

        public MyLocationInfoListener(ClusterAlbumSet clusterAlbumset) {
            this.mClusterAlbumset = clusterAlbumset;
        }

        public void onLocationInfoChange() {
            updateAlbumsContent(false);
            ClusterAlbumSet.this.mDataVersion = MediaObject.nextVersionNumber();
            ClusterAlbumSet.this.notifyContentChanged();
        }

        public void onUpdateAll() {
            synchronized (DataManager.LOCK) {
                ClusterAlbumSet.this.mAlbums.clear();
            }
            updateAlbumsContent(true);
            ClusterAlbumSet.this.mDataVersion = MediaObject.nextVersionNumber();
            ClusterAlbumSet.this.notifyContentChanged();
        }

        private void updateAlbumsContent(boolean isUpdateAll) {
            try {
                int size = ClusterAlbumSet.this.mLocationClustering.getNumberOfClusters();
                DataManager dataManager = ClusterAlbumSet.this.mApplication.getDataManager();
                for (int i = 0; i < size; i++) {
                    String childName = ClusterAlbumSet.this.mLocationClustering.getClusterName(i);
                    Path childPath = ClusterAlbumSet.this.mPath.getChild(childName.replace(',', ' '));
                    synchronized (DataManager.LOCK) {
                        ClusterAlbum album = (ClusterAlbum) dataManager.peekMediaObject(childPath);
                        if (album == null) {
                            album = new ClusterAlbum(childPath, dataManager, this.mClusterAlbumset);
                        }
                        album.setCoverMediaItem(ClusterAlbumSet.this.mLocationClustering.getClusterCover(i));
                        album.setName(childName);
                        album.setMediaItems(ClusterAlbumSet.this.mLocationClustering.getCluster(i));
                        if (!ClusterAlbumSet.this.mAlbums.contains(album)) {
                            if (childName.equals(ClusterAlbumSet.this.mLocationClustering.getNoLocationString())) {
                                ClusterAlbumSet.this.mAlbums.add(album);
                            } else if (isUpdateAll) {
                                ClusterAlbumSet.this.mAlbums.add(album);
                            } else {
                                ClusterAlbumSet.this.mAlbums.add(0, album);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                GalleryLog.e("ClusterAlbumSet", "In onLocationInfoChange Exception " + e.toString());
            }
        }
    }

    public ClusterAlbumSet(Path path, GalleryApp application, MediaSet baseSet, int kind) {
        super(path, -1);
        this.mApplication = application;
        this.mBaseSet = baseSet;
        this.mKind = kind;
        this.mHandler = new Handler(application.getMainLooper());
        switch (this.mKind) {
            case 0:
                this.mClusters = new TimeClusters(application, this, this.mBaseSet, path);
                this.mUseCluster = true;
                break;
            case 1:
                this.mClusters = new LocationClusters(application, this, this.mBaseSet, path);
                this.mUseCluster = true;
                break;
            default:
                this.mClusters = null;
                break;
        }
        if (this.mUseCluster) {
            this.mClusters.setClusterListener(new ClusterListener() {
                public void onClusterCreated(ClusterAlbum album) {
                    refreshAlbumSet();
                    GalleryLog.i("ClusterAlbumSet", "run-time  one cluster created!");
                }

                public void onClusterChanged(ClusterAlbum album, boolean shouldRefresh) {
                    if (shouldRefresh) {
                        album.addDataVersion();
                        album.notifyContentChanged();
                        refreshAlbumSet();
                    }
                }

                private void refreshAlbumSet() {
                    ClusterAlbumSet.this.mDataVersion = MediaObject.nextVersionNumber();
                    ClusterAlbumSet.this.notifyContentChanged();
                }

                public void onClusterDone() {
                    refreshAlbumSet();
                }
            });
        }
        if (this.mKind == 1) {
            this.mConnectivityManager = (ConnectivityManager) application.getAndroidContext().getSystemService("connectivity");
            this.mNetworkInfo = this.mConnectivityManager.getActiveNetworkInfo();
            if (this.mNetworkInfo != null) {
                this.mNetworkConnnect = this.mNetworkInfo.isAvailable();
            }
            this.mFilter = new IntentFilter();
            this.mFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            this.mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                        ClusterAlbumSet.this.mNetworkInfo = ClusterAlbumSet.this.mConnectivityManager.getActiveNetworkInfo();
                        if (ClusterAlbumSet.this.mNetworkInfo != null && ClusterAlbumSet.this.mNetworkInfo.isAvailable() != ClusterAlbumSet.this.mNetworkConnnect) {
                            if (!ClusterAlbumSet.this.mNetworkConnnect) {
                                ClusterAlbumSet.this.mNetworkChangeReload = true;
                                ClusterAlbumSet.this.notifyContentChanged();
                            }
                            ClusterAlbumSet.this.mNetworkConnnect = ClusterAlbumSet.this.mNetworkInfo.isAvailable();
                        }
                    }
                }
            };
            if (!this.mUseCluster) {
                this.mLocationClustering = new LocationClustering(this.mApplication.getAndroidContext(), this.mApplication);
                this.mMyLocationInfoListener = new MyLocationInfoListener(this);
                this.mLocationClustering.addLocationInfoListener(this.mMyLocationInfoListener);
            }
        }
        baseSet.addContentListener(this);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public MediaSet getSubMediaSet(int index) {
        synchronized (DataManager.LOCK) {
            if (index >= (this.mUseCluster ? this.mClusters.size() : this.mAlbums.size())) {
                return null;
            }
            MediaSet mediaSet = this.mUseCluster ? this.mClusters.get(index) : (MediaSet) this.mAlbums.get(index);
        }
    }

    public int getSubMediaSetCount() {
        return this.mUseCluster ? this.mClusters.size() : this.mAlbums.size();
    }

    public String getName() {
        return this.mBaseSet.getName();
    }

    public long reload() {
        this.mIsLoading = true;
        if (this.mBaseSet.reload() <= this.mDataVersion && !(this.mNetworkChangeReload && this.mKind == 1)) {
            if (GalleryUtils.getLanguageChanged() && this.mKind == 0) {
            }
            this.mIsLoading = false;
            return this.mDataVersion;
        }
        if (GalleryUtils.getLanguageChanged() && this.mKind == 0) {
            GalleryUtils.setLanguageChangedFalse();
            updateClusters();
        } else if (this.mNetworkChangeReload && this.mKind == 1) {
            this.mNetworkChangeReload = false;
            if (!this.mUseCluster) {
                this.mLocationClustering.setFirstDivide(true);
            }
            updateClusters();
        } else if (this.mFirstReloadDone) {
            updateClustersContents();
        } else {
            updateClusters();
        }
        this.mFirstReloadDone = true;
        this.mDataVersion = MediaObject.nextVersionNumber();
        this.mIsLoading = false;
        return this.mDataVersion;
    }

    public boolean isLoading() {
        return this.mIsLoading;
    }

    public void onContentDirty() {
        GalleryLog.printDFXLog("source kind " + this.mKind);
        notifyContentChanged();
    }

    private void updateClusters() {
        if (this.mUseCluster) {
            if (this.mClusters != null) {
                if (this.mFuture != null) {
                    this.mFuture.cancel();
                }
                this.mFuture = this.mClusters.reload();
            }
        } else if (this.mKind == 1) {
            this.mLocationClustering.run(this.mBaseSet);
            while (this.mAlbums.size() == 0 && this.mLocationClustering.isLoading()) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                }
            }
        } else {
            Clustering clustering;
            int i;
            ClusterAlbum album;
            Context context = this.mApplication.getAndroidContext();
            switch (this.mKind) {
                case 0:
                    clustering = new TimeClustering(context);
                    break;
                case 1:
                    clustering = new LocationClustering(context, this.mApplication);
                    break;
                case 2:
                    clustering = new TagClustering(context);
                    break;
                default:
                    clustering = new SizeClustering(context);
                    break;
            }
            clustering.run(this.mBaseSet);
            int n = clustering.getNumberOfClusters();
            DataManager dataManager = this.mApplication.getDataManager();
            ArrayList<ClusterAlbum> tmpAlbums = new ArrayList();
            for (i = 0; i < n; i++) {
                Path childPath;
                String childName = clustering.getClusterName(i);
                if (this.mKind == 2) {
                    childPath = this.mPath.getChild(Uri.encode(childName));
                } else if (this.mKind == 3) {
                    childPath = this.mPath.getChild(((SizeClustering) clustering).getMinSize(i));
                } else if (this.mKind == 0) {
                    childPath = this.mPath.getChild(childName);
                } else if (this.mKind == 1) {
                    childPath = this.mPath.getChild(childName.replace(',', ' '));
                } else {
                    childPath = this.mPath.getChild(i);
                }
                synchronized (DataManager.LOCK) {
                    album = (ClusterAlbum) dataManager.peekMediaObject(childPath);
                    if (album == null) {
                        album = new ClusterAlbum(childPath, dataManager, this);
                    }
                    album.setMediaItems(clustering.getCluster(i));
                    album.setName(childName);
                    album.setCoverMediaItem(clustering.getClusterCover(i));
                    if (!tmpAlbums.contains(album)) {
                        tmpAlbums.add(album);
                    }
                }
            }
            synchronized (DataManager.LOCK) {
                for (i = 0; i < this.mAlbums.size(); i++) {
                    album = (ClusterAlbum) this.mAlbums.get(i);
                    if (!tmpAlbums.contains(album)) {
                        GalleryLog.d("ClusterAlbumSet", "Cluster dropped, clean album " + album);
                        album.setMediaItems(new ArrayList<Path>() {
                        });
                    }
                }
                this.mAlbums = tmpAlbums;
            }
        }
    }

    private void updateClustersContents() {
        if (this.mUseCluster) {
            this.mApplication.getThreadPool().submit(new BaseJob<Void>() {
                public Void run(JobContext jc) {
                    ClusterAlbumSet.this.updateClustersContentsInternal();
                    return null;
                }

                public boolean isHeavyJob() {
                    return true;
                }

                public String workContent() {
                    return "updateClustersContentsInternal";
                }
            }, this);
        } else {
            updateClustersContentsInternal();
        }
    }

    private void updateClustersContentsInternal() {
        final HashSet<Path> existing = new HashSet();
        this.mBaseSet.enumerateTotalMediaItems(new ItemConsumer() {
            public void consume(int index, MediaItem item) {
                existing.add(item.getPath());
            }
        });
        HashSet<Path> removedItems = new HashSet();
        synchronized (DataManager.LOCK) {
            int i = (this.mUseCluster ? this.mClusters.size() : this.mAlbums.size()) - 1;
            while (i >= 0) {
                ArrayList<Path> oldPaths = this.mUseCluster ? this.mClusters.getMediaItems(i) : ((ClusterAlbum) this.mAlbums.get(i)).getMediaItems();
                ArrayList<Path> newPaths = new ArrayList();
                int m = oldPaths.size();
                for (int j = 0; j < m; j++) {
                    Path p = (Path) oldPaths.get(j);
                    if (existing.contains(p) || removedItems.contains(p)) {
                        newPaths.add(p);
                        removedItems.add(p);
                        existing.remove(p);
                    } else if (!this.mUseCluster && this.mKind == 1) {
                        ((ClusterAlbum) this.mAlbums.get(i)).setCoverMediaItem(null);
                        this.mLocationClustering.deletePath(((ClusterAlbum) this.mAlbums.get(i)).getName(), p);
                    }
                }
                if (this.mUseCluster) {
                    this.mClusters.get(i).setMediaItems(newPaths);
                } else {
                    ((ClusterAlbum) this.mAlbums.get(i)).setMediaItems(newPaths);
                }
                if (newPaths.isEmpty()) {
                    if (this.mUseCluster) {
                        this.mClusters.remove(i);
                    } else {
                        this.mAlbums.remove(i);
                    }
                }
                i--;
            }
        }
        if (existing.size() >= 1) {
            updateClusters();
        }
        this.mDataVersion = MediaObject.nextVersionNumber();
    }

    public synchronized void onFutureDone(Future<Void> future) {
        GalleryLog.printDFXLog("source kind " + this.mKind);
        this.mHandler.post(new Runnable() {
            public void run() {
                ClusterAlbumSet.this.notifyContentChanged();
            }
        });
    }
}
