package com.huawei.gallery.map.app;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.settings.GallerySettings;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionMode;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.app.AbstractGalleryFragment;
import com.huawei.gallery.app.TimeBucketPage;
import com.huawei.gallery.map.data.ClusterInfo;
import com.huawei.gallery.map.data.MapAlbum;
import com.huawei.gallery.map.data.MapAlbumSet.MapDataListener;
import com.huawei.gallery.map.data.MapDataLoader;
import com.huawei.gallery.map.data.MapLatLng;
import com.huawei.gallery.util.MyPrinter;
import com.huawei.gallery.util.UIUtils;
import java.util.List;

public abstract class MapFragmentBase extends AbstractGalleryFragment implements MapDataListener, OnSharedPreferenceChangeListener {
    private MyPrinter LOG = new MyPrinter("MapFragmentBase");
    protected boolean isReloading = false;
    private GalleryActionBar mActionBar;
    protected double mClusterRadius = -1.0d;
    protected int mClusterStatus = 1;
    private IntentFilter mConnectivityChangeFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    private BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (MapFragmentBase.this.isSupportNetworkControll()) {
                MapFragmentBase.this.showUseNetworkTipsIfNeeded();
            }
        }
    };
    private Bundle mData = new Bundle();
    protected MapDataLoader mDataLoader;
    protected long mDataVersion = -1;
    protected boolean mForAll;
    protected MapCenter mGlobalBound;
    protected MapCenter mGroupBound;
    private Handler mHanlder = null;
    private boolean mIsMapButtonVisible = true;
    protected MapCenter mMapCenter = null;
    protected int mMarkSize = -1;
    private boolean mNetworkTipsShown = false;
    private MapCenter mNiceCenter = null;
    protected boolean mNoPic;
    private TextView mNoPicTips;
    private int mOldStatus = this.mClusterStatus;
    private Runnable mTipUpdater = new Runnable() {
        public void run() {
            MapFragmentBase.this.updateNoPicTips(MapFragmentBase.this.mNoPic);
        }
    };
    private TextView mUseNetworkTextView;

    public static class ClusterRadiusAndDate {
        public String clusterDate;
        public double clusterRadius;
    }

    public static class MapCenter {
        public float bearing;
        public double centerLat;
        public double centerLng;
        public float tilt;
        public float zoomLevel;

        public MapCenter() {
            this(0.0d, 0.0d, 0.0f, 0.0f, 0.0f);
        }

        public MapCenter(double lat, double lng, float zoom, float tilt, float bearing) {
            this.centerLat = lat;
            this.centerLng = lng;
            this.zoomLevel = zoom;
            this.tilt = tilt;
            this.bearing = bearing;
        }

        public MapCenter getCopy() {
            return new MapCenter(this.centerLat, this.centerLng, this.zoomLevel, this.tilt, this.bearing);
        }
    }

    private class OpenMapAlbum implements Runnable {
        final MapAlbum album;

        OpenMapAlbum(MapAlbum mapAlbum) {
            this.album = mapAlbum;
        }

        public void run() {
            if (this.album != null) {
                Bundle data = new Bundle();
                data.putString("media-path", this.album.getPath().toString());
                data.putBoolean("only-local-camera-video-album", false);
                Intent target = new Intent(MapFragmentBase.this.getActivity(), MapSlotAlbumActivity.class);
                target.putExtras(data);
                MapFragmentBase.this.startActivity(target);
            }
        }
    }

    protected abstract int clampZoomLevel(int i);

    protected abstract int getAcceptableMinLevel();

    protected abstract int getAcceptableZoomLevel();

    protected abstract double[] getMeterPerPxArray();

    protected abstract boolean isMapFragmentValid();

    protected abstract void moveCamera(MapCenter mapCenter);

    protected abstract void onLoadFinish(List<ClusterInfo> list);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = getArguments();
        if (data != null) {
            this.mData.putAll(data);
            MediaSet mMediaSet = getGalleryContext().getDataManager().getMediaSet(Path.fromString("/cluster/{" + this.mData.getString("KEY_INIT_VISIBLE_MAP_FOR_PATH", TimeBucketPage.SOURCE_DATA_PATH) + "}/map"));
            this.mIsMapButtonVisible = this.mData.getBoolean("KEY_SUPPORT_GLOBEL", true);
            this.mDataLoader = new MapDataLoader(mMediaSet, this);
            this.mHanlder = new Handler();
        }
    }

    protected boolean isZoomButtonVisible() {
        return !this.mForAll ? this.mIsMapButtonVisible : false;
    }

    protected void init(View mainView) {
        this.mMarkSize = getResources().getDimensionPixelSize(R.dimen.map_cluster_radius);
        if (this.mMapCenter == null) {
            Point displaySize = UIUtils.getDisplaySizeWithoutStatusBar(getActivity());
            initMapCenter(displaySize.x - (this.mMarkSize * 2), ((displaySize.y - mainView.getPaddingTop()) - mainView.getPaddingBottom()) - (this.mMarkSize * 4));
        }
        this.mNoPicTips = (TextView) mainView.findViewById(R.id.no_picture_tips);
        this.mUseNetworkTextView = (TextView) mainView.findViewById(R.id.network_tips);
        if (this.mUseNetworkTextView != null) {
            this.mUseNetworkTextView.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    MapFragmentBase.this.showUseNetworkDialog(MapFragmentBase.this.getActivity(), R.string.use_network_dialog_title);
                }
            });
        }
    }

    private void initMapCenter(int showWidthPx, int showHeightPx) {
        RectF rect = (RectF) this.mData.getParcelable("KEY_INIT_VISIBLE_MAP_RECT");
        if (rect == null) {
            throw new RuntimeException("Must Pass in a visible rect");
        }
        boolean overRanged = MapUtils.isMapOverRanged((double) rect.height(), (double) rect.width());
        this.mMapCenter = new MapCenter();
        MapLatLng latlng = MapConverter.transform((double) rect.centerY(), (double) rect.centerX(), new MapLatLng());
        this.mMapCenter.centerLng = latlng.longitude;
        this.mMapCenter.centerLat = latlng.latitude;
        if (((double) rect.left) == 0.0d && ((double) rect.right) == 0.0d) {
            this.mMapCenter.zoomLevel = (float) getAcceptableMinLevel();
        } else {
            int acceptableLevel = getAcceptableZoomLevel();
            int level = MapUtils.getBestZoomScale(showWidthPx, showHeightPx, (double) rect.width(), (double) rect.height(), getMeterPerPxArray(), acceptableLevel);
            this.mMapCenter.zoomLevel = (float) Math.min(clampZoomLevel(level - 1), acceptableLevel);
        }
        this.mMapCenter.tilt = 0.0f;
        this.mMapCenter.bearing = 0.0f;
        if (overRanged) {
            this.mNiceCenter = this.mMapCenter.getCopy();
        }
        this.mNoPic = this.mData.getBoolean("KEY_INIT_VISIBLE_MAP_NO_PIC", false);
        this.mForAll = this.mData.getBoolean("KEY_INIT_VISIBLE_MAP_FOR_ALL", false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mActionBar = getGalleryActionBar();
    }

    public void onStart() {
        super.onStart();
        this.mDataVersion = -1;
        MapManager.getInstance().onStart(this);
    }

    protected void onCreateActionBar(Menu menu) {
        super.onCreateActionBar(menu);
        requestFeature(34);
        if (this.mActionBar != null) {
            ActionMode am = this.mActionBar.enterStandardTitleActionMode(false);
            am.setBothAction(Action.NONE, Action.NONE);
            am.setTitle((int) R.string.map_album);
            am.show();
        }
    }

    public void onResume() {
        super.onResume();
        this.mDataLoader.resume();
        if (isSupportNetworkControll()) {
            PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
            showUseNetworkTipsIfNeeded();
            getActivity().registerReceiver(this.mConnectivityChangeReceiver, this.mConnectivityChangeFilter);
        }
        updateNoPicTips(this.mNoPic);
    }

    public void onPause() {
        super.onPause();
        this.mDataLoader.pause();
        if (isSupportNetworkControll()) {
            PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
            hideUseNetworkTipsIfNeeded();
            getActivity().unregisterReceiver(this.mConnectivityChangeReceiver);
        }
        updateNoPicTips(false);
    }

    public void onStop() {
        super.onStop();
        MapManager.getInstance().onStop();
    }

    public void onActionItemClicked(Action action) {
    }

    public boolean onBackPressed() {
        return false;
    }

    public boolean onMarkClicked(MapMarkBase mark) {
        if (this.isReloading || mark == null || mark.isAnimating()) {
            GalleryLog.d("MapFragmentBase", "click but fail");
            return false;
        }
        this.mHanlder.post(new OpenMapAlbum(mark.getAlbum()));
        return true;
    }

    protected ClusterRadiusAndDate getClusterRadiusAndDate() {
        ClusterRadiusAndDate clusterRadiusAndDate = new ClusterRadiusAndDate();
        if (this.mMarkSize < 0 || this.mMapCenter.zoomLevel < 0.0f) {
            clusterRadiusAndDate.clusterRadius = 0.5d;
        } else {
            clusterRadiusAndDate.clusterRadius = MapUtils.getMapClusterRadius((double) this.mMarkSize, getMeterPerPxArray(), this.mMapCenter.zoomLevel);
        }
        clusterRadiusAndDate.clusterDate = this.mData.getString("KEY_START_TO_END_DATE");
        return clusterRadiusAndDate;
    }

    private boolean equal(float value1, float value2) {
        return Math.abs(value1 - value2) <= 0.002f;
    }

    protected boolean onMapCenterChanged(MapCenter point) {
        if (equal(0.0f, point.bearing) && equal(0.0f, point.tilt) && !(equal(this.mMapCenter.bearing, 0.0f) && equal(this.mMapCenter.tilt, 0.0f))) {
            this.LOG.d("compass is clicked");
            ReportToBigData.report(16, String.format("{SwitchMapView:%s}", new Object[]{"MapCompass"}));
        }
        boolean isZoomLevelChange = this.mMapCenter.zoomLevel != point.zoomLevel;
        boolean statusChanged = this.mOldStatus != this.mClusterStatus;
        this.mMapCenter = point;
        if (this.mClusterStatus == 2) {
            this.mGlobalBound = this.mMapCenter;
        } else if (this.mClusterStatus == 1) {
            this.mGroupBound = this.mMapCenter;
        }
        this.mOldStatus = this.mClusterStatus;
        if (isZoomLevelChange || statusChanged) {
            this.mDataLoader.triggerReload(this.mClusterStatus);
        }
        return isZoomLevelChange;
    }

    public void onLoadFinish(List<ClusterInfo> clusterInfoList, long dataVersion, double clusterRadius) {
        boolean z = true;
        if (this.mDataVersion < dataVersion || Math.abs(this.mClusterRadius - clusterRadius) >= 2.0E-7d) {
            this.mDataVersion = dataVersion;
            this.mClusterRadius = clusterRadius;
            this.isReloading = true;
            if (clusterInfoList != null) {
                z = clusterInfoList.isEmpty();
            }
            this.mNoPic = z;
            this.mHanlder.post(this.mTipUpdater);
            onLoadFinish(clusterInfoList);
            this.isReloading = false;
        }
    }

    private void updateNoPicTips(boolean show) {
        if (this.mNoPicTips != null) {
            if (!show) {
                this.mNoPicTips.setVisibility(4);
            } else if (this.mUseNetworkTextView == null || !this.mNetworkTipsShown) {
                this.mNoPicTips.setVisibility(0);
            } else {
                this.mNoPicTips.setVisibility(4);
            }
        }
    }

    private void showUseNetworkTipsIfNeeded() {
        int i = 0;
        if (this.mUseNetworkTextView != null) {
            boolean useNetwork = GallerySettings.getBoolean(getActivity(), GallerySettings.KEY_USE_NETWORK, false);
            setNetworkEnable(useNetwork);
            this.mNetworkTipsShown = !useNetwork;
            TextView textView = this.mUseNetworkTextView;
            if (useNetwork) {
                i = 4;
            }
            textView.setVisibility(i);
            updateNoPicTips(this.mNoPic);
        }
    }

    private void hideUseNetworkTipsIfNeeded() {
        if (this.mUseNetworkTextView != null && this.mNetworkTipsShown) {
            this.mNetworkTipsShown = false;
            this.mUseNetworkTextView.setVisibility(4);
        }
    }

    private void showUseNetworkDialog(final Context context, int titleId) {
        View tipsView = LayoutInflater.from(context).inflate(R.layout.use_network_dialog, null);
        AlertDialog tipsDialog = new Builder(context).setTitle(titleId).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                editor.putBoolean(GallerySettings.KEY_USE_NETWORK, true);
                editor.apply();
                ReportToBigData.report(9);
            }
        }).create();
        int padding = context.getResources().getDimensionPixelSize(R.dimen.alter_dialog_padding_left_right);
        tipsDialog.setView(tipsView, padding, padding, padding, 0);
        tipsDialog.show();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (GallerySettings.KEY_USE_NETWORK.equals(key) && isSupportNetworkControll()) {
            showUseNetworkTipsIfNeeded();
        }
    }

    @Deprecated
    public void setNetworkEnable(boolean enable) {
    }

    public boolean isSupportNetworkControll() {
        return false;
    }

    protected void runOnUiThread(Runnable task) {
        this.mHanlder.post(task);
    }

    public void sendNewestPicture(double lat, double lng) {
        if (this.mNiceCenter != null) {
            this.mNiceCenter.centerLat = lat;
            this.mNiceCenter.centerLng = lng;
            runOnUiThread(new Runnable() {
                public void run() {
                    if (MapFragmentBase.this.mNiceCenter != null) {
                        MapFragmentBase.this.moveCamera(MapFragmentBase.this.mNiceCenter);
                        MapFragmentBase.this.mNiceCenter = null;
                    }
                }
            });
        }
    }
}
