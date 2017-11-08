package com.android.gallery3d.data;

import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterWorker<P> extends Thread {
    private Comparator<P> mAlbumComparator;
    private List<String> mAlbumName = new ArrayList();
    private Map<String, ArrayList<Path>> mAlbumPaths = new HashMap();
    private Map<String, ClusterAlbum> mAlbums = new HashMap();
    private Map<Path, P> mCachedPathP = new HashMap();
    private Proxy<P> mClient;
    private DataManager mDataManager;
    private volatile boolean mDone = false;
    private Comparator<P> mItemComparator;
    private final ArrayDeque<P> mItemQueue = new ArrayDeque();
    private Comparator<String> mKeyComparator = new Comparator<String>() {
        public int compare(String lhs, String rhs) {
            if (ClusterWorker.this.mAlbumComparator != null) {
                if ("-1".equals(lhs)) {
                    return 1;
                }
                if ("-1".equals(rhs) || "-2".equals(lhs)) {
                    return -1;
                }
                if ("-2".equals(rhs)) {
                    return 1;
                }
                ArrayList<Path> list1 = (ArrayList) ClusterWorker.this.mAlbumPaths.get(lhs);
                ArrayList<Path> list2 = (ArrayList) ClusterWorker.this.mAlbumPaths.get(rhs);
                if (list1 == list2) {
                    return 0;
                }
                if (list1 == null || list1.size() == 0) {
                    return 1;
                }
                if (list2 == null || list2.size() == 0) {
                    return -1;
                }
                Path p2 = (Path) list2.get(0);
                P item1 = ClusterWorker.this.mCachedPathP.get((Path) list1.get(0));
                P item2 = ClusterWorker.this.mCachedPathP.get(p2);
                if (item1 == null && item2 == null) {
                    return 0;
                }
                if (item1 == null) {
                    return 1;
                }
                if (item2 == null) {
                    return -1;
                }
                return ClusterWorker.this.mAlbumComparator.compare(item1, item2);
            } else if (lhs == null && rhs == null) {
                return 0;
            } else {
                if (rhs == null) {
                    return -1;
                }
                if (lhs == null) {
                    return 1;
                }
                return rhs.compareTo(lhs);
            }
        }
    };
    private ClusterListener mListener;
    private MediaSet mParent;
    private Comparator<Path> mPathPComparator = new Comparator<Path>() {
        public int compare(Path lhs, Path rhs) {
            P lp = ClusterWorker.this.mCachedPathP.get(lhs);
            P rp = ClusterWorker.this.mCachedPathP.get(rhs);
            if (lp == rp) {
                return 0;
            }
            if (lp == null) {
                return 1;
            }
            if (rp == null) {
                return -1;
            }
            return ClusterWorker.this.mItemComparator.compare(lp, rp);
        }
    };
    private Path mRoot;

    public interface ClusterListener {
        void onClusterChanged(ClusterAlbum clusterAlbum, boolean z);

        void onClusterCreated(ClusterAlbum clusterAlbum);

        void onClusterDone();
    }

    public interface Proxy<T> {
        String generateCaption(T t);

        String getClusterKey(T t);

        Path getPath(T t);
    }

    public ClusterWorker(DataManager manager, MediaSet parent, Path root) {
        this.mDataManager = manager;
        this.mRoot = root;
        this.mParent = parent;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        Throwable th;
        while (true) {
            synchronized (this) {
                if (this.mItemQueue.isEmpty()) {
                    if (this.mDone) {
                        notifyDone();
                    }
                    Utils.waitWithoutInterrupt(this);
                } else {
                    P item = this.mItemQueue.removeFirst();
                    if (this.mClient == null) {
                        GalleryLog.w("ClusterWorker", "Client for proxy is null !!!");
                    } else {
                        ClusterAlbum album;
                        ArrayList<Path> pathList;
                        String key = this.mClient.getClusterKey(item);
                        Path childPath = this.mRoot.getChild(key);
                        synchronized (this) {
                            album = (ClusterAlbum) this.mAlbums.get(key);
                            pathList = (ArrayList) this.mAlbumPaths.get(key);
                        }
                        if (album == null) {
                            album = (ClusterAlbum) this.mDataManager.peekMediaObject(childPath);
                            if (album == null) {
                                album = new ClusterAlbum(childPath, this.mDataManager, this.mParent);
                            }
                            album.setName(this.mClient.generateCaption(item));
                            synchronized (this) {
                                if (pathList == null) {
                                    ArrayList<Path> pathList2 = new ArrayList();
                                    try {
                                        this.mAlbumPaths.put(key, pathList2);
                                        pathList = pathList2;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        pathList = pathList2;
                                    }
                                }
                                try {
                                    pathList.clear();
                                    this.mAlbums.put(key, album);
                                    this.mAlbumName.add(key);
                                    album.setMediaItems((ArrayList) pathList.clone());
                                    Collections.sort(this.mAlbumName, this.mKeyComparator);
                                } catch (Throwable th3) {
                                    th = th3;
                                }
                            }
                        }
                        Path path = this.mClient.getPath(item);
                        pathList.add(path);
                        album.setMediaItems((ArrayList) pathList.clone());
                        this.mCachedPathP.put(path, item);
                        int size = pathList.size();
                        if (this.mListener != null) {
                            boolean z;
                            ClusterListener clusterListener = this.mListener;
                            if (size % 100 == 0) {
                                z = true;
                            } else {
                                z = false;
                            }
                            clusterListener.onClusterChanged(album, z);
                        }
                    }
                }
            }
        }
        throw th;
    }

    public synchronized String getName(int index) {
        if (index >= 0) {
            if (index < this.mAlbumName.size()) {
                return (String) this.mAlbumName.get(index);
            }
        }
        return null;
    }

    public synchronized ClusterAlbum get(int index) {
        return (ClusterAlbum) this.mAlbums.get(getName(index));
    }

    public synchronized String remove(int index) {
        if (index >= 0) {
            if (index < this.mAlbumName.size()) {
                String key = (String) this.mAlbumName.remove(index);
                this.mAlbums.remove(key);
                return key;
            }
        }
        return null;
    }

    public synchronized int size() {
        return this.mAlbumName.size();
    }

    public synchronized void addItem(P item) {
        this.mItemQueue.add(item);
        notifyAll();
    }

    public synchronized void clear() {
        this.mItemQueue.clear();
        this.mAlbumName.clear();
        this.mAlbums.clear();
        notifyAll();
    }

    public synchronized void setDone(boolean done) {
        this.mDone = done;
        notifyAll();
    }

    public void setClusterListener(ClusterListener l) {
        this.mListener = l;
    }

    public void setItemProxy(Proxy<P> c) {
        this.mClient = c;
    }

    public void setItemComparator(Comparator<P> comparator) {
        this.mItemComparator = comparator;
    }

    public void setAlbumCompartor(Comparator<P> comparator) {
        this.mAlbumComparator = comparator;
    }

    private void notifyDone() {
        GalleryLog.d("ClusterWorker", "notify done, will refresh albumset");
        ClusterListener l = this.mListener;
        if (l != null) {
            boolean hasComparator = this.mItemComparator != null;
            Comparator<Path> c = this.mPathPComparator;
            for (String key : this.mAlbumName) {
                if (hasComparator) {
                    ArrayList<Path> paths = (ArrayList) this.mAlbumPaths.get(key);
                    if (paths != null && paths.size() != 0) {
                        Collections.sort(paths, c);
                    } else {
                        return;
                    }
                }
                l.onClusterChanged((ClusterAlbum) this.mAlbums.get(key), true);
            }
            Collections.sort(this.mAlbumName, this.mKeyComparator);
            l.onClusterDone();
            this.mCachedPathP.clear();
        }
    }
}
