package com.huawei.gallery.map.app;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.map.app.MapMarkBase.AnimationListener;
import com.huawei.gallery.map.data.ClusterInfo;
import com.huawei.gallery.map.data.ClusterManager;
import com.huawei.gallery.map.data.MapAlbum;
import com.huawei.gallery.map.data.MapLatLng;
import com.huawei.gallery.util.MyPrinter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MapMarkerManagerFragment extends MapFragmentBase {
    private static final MyPrinter LOG = new MyPrinter("MapMarkerManagerFragment");
    private boolean mActive = false;
    private SimpleRunnableProcessor mClusterThread;
    private AnimationListener mMarkListener = new AnimationListener() {
        public void onAnimationEnd(MapMarkBase mark, MapLatLng to, MapAlbum target) {
            MapMarkerManagerFragment.this.sendMsg(5, mark, to, target);
        }
    };
    private MapMarkMaker mMarkMaker;
    protected ArrayList<MapMarkBase> mMarkerList = new ArrayList();
    private Handler mUpdateMarkHandler = new Handler() {
        public void handleMessage(Message msg) {
            SimplePara para = msg.obj;
            ClusterInfo album;
            MapMarkBase mark;
            MapLatLng start;
            switch (msg.what) {
                case 1:
                    try {
                        MapMarkerManagerFragment.this.updateMark((List) para.get(0));
                        break;
                    } catch (RuntimeException e) {
                        GalleryLog.d("MapMarkerManagerFragment", "Error when update mark." + e.getMessage());
                        MapMarkerManagerFragment.this.clearMark();
                        break;
                    }
                case 2:
                    album = (ClusterInfo) para.get(0);
                    mark = MapMarkerManagerFragment.this.createMapMark(album.to, (Bitmap) para.get(1));
                    start = album.from;
                    if (start == null) {
                        start = MapMarkerManagerFragment.this.getFallingStartPosition(mark);
                    }
                    mark.setClusterInfo(album);
                    mark.startAnimation(start, MapMarkerManagerFragment.this.mMarkListener);
                    break;
                case 3:
                    album = (ClusterInfo) para.get(0);
                    Bitmap drawable = (Bitmap) para.get(1);
                    mark = (MapMarkBase) para.get(2);
                    mark.init(album.to);
                    mark.updateIcon(drawable);
                    start = album.from;
                    if (start == null) {
                        start = MapMarkerManagerFragment.this.getFallingStartPosition(mark);
                    }
                    mark.setClusterInfo(album);
                    mark.startAnimation(start, MapMarkerManagerFragment.this.mMarkListener);
                    break;
                case 4:
                    ArrayList<MapMarkBase> currentMarkerArray = (ArrayList) para.get(0);
                    MapMarkerManagerFragment.LOG.d("MSG_REMOVE_AND_STOP_ALL_MARK count : " + currentMarkerArray.size());
                    for (MapMarkBase mark2 : currentMarkerArray) {
                        mark2.destory();
                        MapMarkerManagerFragment.this.mMarkerList.remove(mark2);
                    }
                    break;
                case 5:
                    MapMarkBase temMark = (MapMarkBase) para.get(0);
                    MapAlbum album2 = (MapAlbum) para.get(2);
                    mark2 = MapMarkerManagerFragment.this.getMarkerInList(MapMarkerManagerFragment.this.mMarkerList, (MapLatLng) para.get(1));
                    if (mark2 != null) {
                        temMark.destory();
                        mark2.updateIcon(MapMarkerManagerFragment.this.mMarkMaker.generateDrawable(album2));
                        break;
                    }
                    mark2 = temMark;
                    temMark.setMapAlbum(album2);
                    MapMarkerManagerFragment.this.mMarkerList.add(temMark);
                    break;
                case 6:
                    treatRecheckMarksMsg();
                    break;
                default:
                    throw new IllegalArgumentException("invalide type : " + msg.what);
            }
            SimplePara.recycle(para);
        }

        private void treatRecheckMarksMsg() {
            for (MapMarkBase mark : new ArrayList(MapMarkerManagerFragment.this.mMarkerList)) {
                MapAlbum album = mark.getAlbum();
                if (album != null && ClusterManager.isValid(album) && album.getLocation().equals(mark.getPosition())) {
                    ClusterManager.modifyMapAlbum(album);
                    mark.updateIcon(MapMarkerManagerFragment.this.mMarkMaker.generateDrawable(album));
                } else {
                    mark.stopAnim();
                    MapMarkerManagerFragment.this.mMarkerList.remove(mark);
                    mark.destory();
                }
            }
        }
    };

    protected class GlobalCenter implements Runnable {
        double lat;
        double lng;

        public GlobalCenter(double[] center) {
            this.lat = center[0];
            this.lng = center[1];
        }

        public void run() {
            MapMarkerManagerFragment.this.moveToGlobalCenter(this.lat, this.lng);
        }
    }

    private static class SimplePara {
        private static List<SimplePara> mPool = new LinkedList();
        private Object[] mArgs;

        public static synchronized SimplePara obtain(Object... args) {
            synchronized (SimplePara.class) {
                if (mPool.isEmpty()) {
                    SimplePara simplePara = new SimplePara(args);
                    return simplePara;
                }
                SimplePara obj = (SimplePara) mPool.remove(0);
                if (obj == null) {
                    simplePara = new SimplePara(args);
                    return simplePara;
                }
                simplePara = obj.setArgs(args);
                return simplePara;
            }
        }

        public static synchronized void recycle(SimplePara obj) {
            synchronized (SimplePara.class) {
                if (obj == null) {
                    MapMarkerManagerFragment.LOG.e("recycle obj is null, action will be ignored !!!");
                    return;
                }
                if (mPool.size() >= 300) {
                    mPool.remove(0);
                }
                mPool.add(obj.releasArgs());
            }
        }

        private SimplePara(Object... args) {
            this.mArgs = args;
        }

        private SimplePara releasArgs() {
            this.mArgs = null;
            return this;
        }

        private SimplePara setArgs(Object... args) {
            this.mArgs = args;
            return this;
        }

        <T> T get(int index) {
            return this.mArgs[index];
        }
    }

    private class SimpleRunnableProcessor extends Thread {
        private List<ClusterInfo> mClusterInfoList;
        private final BlockingQueue<List<ClusterInfo>> mRunnableQueue = new LinkedBlockingQueue();
        private boolean mScheduleQuit = false;

        public SimpleRunnableProcessor(String threadName) {
            super("RunnableProcessor-" + threadName);
        }

        public void run() {
            loop0:
            while (true) {
                try {
                    MapMarkerManagerFragment.LOG.d("going to take task ...");
                    synchronized (this) {
                        this.mClusterInfoList = (List) this.mRunnableQueue.take();
                        MapMarkerManagerFragment.LOG.d("total abums is " + this.mClusterInfoList.size());
                        List currentMarkerArray = new ArrayList();
                        if (MapMarkerManagerFragment.this.mMarkMaker == null) {
                            MapMarkerManagerFragment.this.mMarkMaker = new MapMarkMaker(MapMarkerManagerFragment.this.getActivity());
                        }
                        MapMarkerManagerFragment.LOG.d("current mark count is " + MapMarkerManagerFragment.this.mMarkerList.size());
                        currentMarkerArray.addAll(MapMarkerManagerFragment.this.mMarkerList);
                        MapMarkerManagerFragment.this.mMarkerList.clear();
                        for (ClusterInfo cluster : this.mClusterInfoList) {
                            if (!MapMarkerManagerFragment.this.isDetached() && MapMarkerManagerFragment.this.getActivity() != null) {
                                if (MapMarkerManagerFragment.this.isMapFragmentValid()) {
                                    MapMarkBase mark = MapMarkerManagerFragment.this.getMarkerInList(currentMarkerArray, cluster);
                                    Bitmap drawable = MapMarkerManagerFragment.this.mMarkMaker.generateDrawable(cluster);
                                    if (mark == null) {
                                        MapMarkerManagerFragment.this.sendMsg(2, cluster, drawable);
                                    } else {
                                        currentMarkerArray.remove(mark);
                                        MapMarkerManagerFragment.this.sendMsg(3, cluster, drawable, mark);
                                    }
                                }
                            }
                        }
                        MapMarkerManagerFragment.this.sendMsg(4, currentMarkerArray);
                        MapMarkerManagerFragment.this.mUpdateMarkHandler.removeMessages(6);
                        MapMarkerManagerFragment.this.mUpdateMarkHandler.sendEmptyMessageDelayed(6, 500);
                        notifyAll();
                    }
                } catch (InterruptedException e) {
                    MapMarkerManagerFragment.LOG.e("Terminating " + getName());
                    return;
                }
            }
        }

        public void submit(List<ClusterInfo> ClusterInfoList) {
            if (this.mScheduleQuit) {
                MapMarkerManagerFragment.LOG.e("service will be quit, shouldn't submit any tasks.");
            }
            if (isAlive()) {
                this.mRunnableQueue.add(ClusterInfoList);
            } else {
                MapMarkerManagerFragment.LOG.e(getName() + " should be started before submitting tasks.");
            }
        }

        public void quit() {
            this.mScheduleQuit = true;
            if (this.mRunnableQueue.size() > 0) {
                MapMarkerManagerFragment.LOG.w("[quit] task queue in thread is not empty. ");
            }
            this.mRunnableQueue.clear();
            interrupt();
        }
    }

    protected abstract MapMarkBase createMapMark(MapLatLng mapLatLng, Bitmap bitmap);

    protected abstract MapLatLng getFallingStartPosition(MapMarkBase mapMarkBase);

    protected MapMarkBase getMarkerInList(List<MapMarkBase> markerList, MapLatLng latlng) {
        for (MapMarkBase marker : markerList) {
            if (marker.getPosition().equals(latlng)) {
                return marker;
            }
        }
        return null;
    }

    protected MapMarkBase getMarkerInList(List<MapMarkBase> markerList, ClusterInfo album) {
        MapLatLng latlng = new MapLatLng(album.to.latitude, album.to.longitude);
        for (MapMarkBase marker : markerList) {
            if (marker.getAlbumPosition().equals(latlng)) {
                return marker;
            }
        }
        return null;
    }

    private synchronized void sendMsg(int type, Object... objects) {
        if (this.mActive) {
            this.mUpdateMarkHandler.obtainMessage(type, SimplePara.obtain(objects)).sendToTarget();
        }
    }

    protected void clearMark() {
        for (MapMarkBase mark : this.mMarkerList) {
            mark.destory();
        }
        this.mMarkerList.clear();
    }

    protected void updateMark(List<ClusterInfo> albums) throws RuntimeException {
        if (isDetached() || getActivity() == null || !isMapFragmentValid()) {
            clearMark();
            return;
        }
        if (this.mClusterThread != null) {
            this.mClusterThread.submit(albums);
        }
    }

    public void onLoadFinish(List<ClusterInfo> albums) {
        if (this.mActive) {
            this.mUpdateMarkHandler.sendMessageDelayed(this.mUpdateMarkHandler.obtainMessage(1, SimplePara.obtain(albums)), 400);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        synchronized (this) {
            this.mActive = true;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        synchronized (this) {
            this.mActive = false;
            this.mUpdateMarkHandler.removeCallbacksAndMessages(null);
        }
    }

    public void onResume() {
        super.onResume();
        this.mClusterThread = new SimpleRunnableProcessor("cluster-thread");
        this.mClusterThread.start();
    }

    public void onPause() {
        if (this.mClusterThread != null) {
            this.mClusterThread.quit();
        }
        this.mUpdateMarkHandler.removeCallbacksAndMessages(null);
        this.mClusterThread = null;
        super.onPause();
    }

    protected void moveToGlobalCenter(double lat, double lng) {
    }
}
