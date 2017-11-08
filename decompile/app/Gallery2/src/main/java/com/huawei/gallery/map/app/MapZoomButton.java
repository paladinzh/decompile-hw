package com.huawei.gallery.map.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import com.android.gallery3d.R;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.map.app.MapFragmentBase.MapCenter;

public class MapZoomButton extends ImageButton implements OnClickListener {
    private MapCameraHandler mHandler = null;
    private boolean mIsGlobal = false;
    private MapCenter mZoomBackCenter = null;

    public interface MapCameraHandler {
        MapCenter getCurrentCameraPosition();

        void moveCameraTo(MapCenter mapCenter, int i);

        void zoomGlobal(int i);

        void zoomGroup(int i);
    }

    public void updateIcon(int status) {
        int resId = -1;
        switch (status) {
            case 1:
                this.mIsGlobal = false;
                resId = R.drawable.btn_map_zoommax;
                break;
            case 2:
                this.mIsGlobal = true;
                resId = R.drawable.ic_gallery_map_location;
                break;
        }
        if (resId != -1) {
            setImageResource(resId);
        }
    }

    public void setMapCameraHandler(MapCameraHandler handler) {
        this.mHandler = handler;
    }

    public MapZoomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    private void zoomMin() {
        setImageResource(R.drawable.ic_gallery_map_location);
        this.mZoomBackCenter = this.mHandler.getCurrentCameraPosition();
        this.mHandler.zoomGlobal(2);
        this.mIsGlobal = true;
    }

    private void zoomMax() {
        setImageResource(R.drawable.btn_map_zoommax);
        this.mZoomBackCenter = this.mHandler.getCurrentCameraPosition();
        this.mHandler.zoomGroup(1);
        this.mIsGlobal = false;
    }

    private void switchMap() {
        int useStatus = this.mIsGlobal ? 1 : 2;
        updateIcon(useStatus);
        MapCenter zoomBackCenter = this.mHandler.getCurrentCameraPosition();
        this.mHandler.moveCameraTo(this.mZoomBackCenter, useStatus);
        this.mZoomBackCenter = zoomBackCenter;
    }

    public void onClick(View view) {
        if (this.mHandler != null) {
            String format = "{SwitchMapView:%s}";
            String targetType = this.mIsGlobal ? "MapGroup" : "MapGlobal";
            ReportToBigData.report(16, String.format(format, new Object[]{targetType}));
            if (this.mZoomBackCenter != null) {
                switchMap();
            } else if (this.mIsGlobal) {
                zoomMax();
            } else {
                zoomMin();
            }
        }
    }
}
