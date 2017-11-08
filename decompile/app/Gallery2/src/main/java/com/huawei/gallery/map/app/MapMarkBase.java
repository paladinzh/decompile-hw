package com.huawei.gallery.map.app;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.Interpolator;
import com.android.gallery3d.common.Utils;
import com.huawei.gallery.animation.CubicBezierInterpolator;
import com.huawei.gallery.map.data.ClusterInfo;
import com.huawei.gallery.map.data.MapAlbum;
import com.huawei.gallery.map.data.MapLatLng;
import com.huawei.gallery.util.MyPrinter;
import com.huawei.watermark.manager.parse.WMElement;

public abstract class MapMarkBase {
    protected static final MyPrinter LOG = new MyPrinter("MapMarkBase");
    protected boolean canRunAnim = true;
    private final Interpolator interpolator = new CubicBezierInterpolator(0.51f, 0.35f, 0.15f, WMElement.CAMERASIZEVALUE1B1);
    private boolean isAnimating = false;
    protected Handler mAnimHandler;
    private ClusterInfo mClusterInfo;
    private long mDuration = 300;
    private MapAlbum mMapAlbum;
    private MarkAnimator mMarkAnimator;
    protected MapLatLng mPosition = null;

    public interface AnimationListener {
        void onAnimationEnd(MapMarkBase mapMarkBase, MapLatLng mapLatLng, MapAlbum mapAlbum);
    }

    private class MarkAnimator implements Runnable {
        private MapLatLng mFrom = null;
        private Handler mHandler;
        private long mStartTime;
        private MapLatLng mTo = null;
        private AnimationListener tListener;

        MarkAnimator(Handler animHandler) {
            this.mHandler = animHandler;
        }

        MarkAnimator callbackOnce(AnimationListener listener) {
            this.tListener = listener;
            return this;
        }

        MarkAnimator from(MapLatLng from) {
            this.mFrom = from;
            return this;
        }

        MarkAnimator to(MapLatLng to) {
            this.mTo = to;
            return this;
        }

        void start() {
            this.mStartTime = SystemClock.uptimeMillis();
            this.mHandler.post(this);
        }

        void stop() {
            this.mHandler.removeCallbacks(this);
        }

        public void run() {
            MapLatLng from = this.mFrom;
            MapLatLng to = this.mTo;
            float t = MapMarkBase.this.interpolator.getInterpolation(Utils.clamp(((float) (SystemClock.uptimeMillis() - this.mStartTime)) / ((float) MapMarkBase.this.mDuration), 0.0f, (float) WMElement.CAMERASIZEVALUE1B1));
            if (MapMarkBase.this.shouldRunAnim(t)) {
                double lat = (((double) t) * to.latitude) + (((double) (WMElement.CAMERASIZEVALUE1B1 - t)) * from.latitude);
                MapMarkBase.this.setPosition(new MapLatLng(lat, (((double) t) * to.longitude) + (((double) (WMElement.CAMERASIZEVALUE1B1 - t)) * from.longitude)));
                this.mHandler.postDelayed(this, 16);
                return;
            }
            MapMarkBase.this.canRunAnim = false;
            MapMarkBase.this.isAnimating = false;
            MapMarkBase.this.doneAnimation(this.tListener);
            this.tListener = null;
        }
    }

    protected abstract void setPosition(MapLatLng mapLatLng);

    public abstract void updateIcon(Bitmap bitmap);

    public MapMarkBase(MapFragmentBase fragment) {
        this.mAnimHandler = new Handler(fragment.getActivity().getMainLooper());
        this.mMarkAnimator = new MarkAnimator(this.mAnimHandler);
    }

    protected void init(MapLatLng position) {
        this.canRunAnim = true;
        this.mPosition = position;
    }

    public void setMapAlbum(MapAlbum album) {
        this.mMapAlbum = album;
    }

    public void setClusterInfo(ClusterInfo info) {
        this.mClusterInfo = info;
    }

    public MapAlbum getAlbum() {
        if (this.mMapAlbum != null) {
            return this.mMapAlbum;
        }
        if (this.mClusterInfo != null) {
            return this.mClusterInfo.target;
        }
        return null;
    }

    public void destory() {
        this.canRunAnim = false;
        this.mMarkAnimator.stop();
    }

    public MapLatLng getPosition() {
        return this.mPosition;
    }

    public MapLatLng getAlbumPosition() {
        return getPosition();
    }

    protected boolean shouldRunAnim(float time) {
        return time < WMElement.CAMERASIZEVALUE1B1 ? this.canRunAnim : false;
    }

    public boolean isAnimating() {
        return this.isAnimating;
    }

    public void startAnimation(MapLatLng startFallingPos, AnimationListener listener) {
        MapLatLng start = startFallingPos;
        if (startFallingPos == null || !this.canRunAnim) {
            doneAnimation(listener);
            return;
        }
        this.isAnimating = true;
        setPosition(startFallingPos);
        this.mMarkAnimator.callbackOnce(listener).from(startFallingPos).to(this.mPosition).start();
    }

    private void doneAnimation(AnimationListener listener) {
        if (this.mClusterInfo != null) {
            this.mClusterInfo.addToTarget();
        }
        if (listener != null) {
            listener.onAnimationEnd(this, this.mPosition, this.mClusterInfo.target);
        }
        LOG.d("[doneAnimation] cluster info " + this.mClusterInfo + ", listener " + listener);
        setPosition(this.mPosition);
    }

    public void stopAnim() {
        this.canRunAnim = false;
        setPosition(this.mPosition);
    }
}
