package com.autonavi.amap.mapcore;

import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.WeightedLatLng;
import com.huawei.watermark.manager.parse.WMElement;

public class MapConfig implements Cloneable {
    public static final int DEFAULT_RATIO = 1;
    private static final int GEO_POINT_ZOOM = 20;
    public static final float MAX_ZOOM = 19.0f;
    public static final float MAX_ZOOM_INDOOR = 20.0f;
    public static final float MIN_ZOOM = 3.0f;
    public static final int MSG_AUTH_FAILURE = 2;
    public static final int MSG_CALLBACK_MAPLOADED = 18;
    public static final int MSG_CALLBACK_ONTOUCHEVENT = 16;
    public static final int MSG_CALLBACK_SCREENSHOT = 17;
    public static final int MSG_CAMERAUPDATE_CHANGE = 10;
    public static final int MSG_CAMERAUPDATE_FINISH = 12;
    public static final int MSG_CAMERAUPDATE_NEWBOUNDS = 11;
    public static final int MSG_CAMERAUPDATE_UPDATEBOUNDS = 13;
    public static final int MSG_COMPASSVIEW_CHANGESTATE = 15;
    public static final int MSG_INFOWINDOW_UPDATE = 21;
    public static final int MSG_TILEOVERLAY_REFRESH = 19;
    public static final int MSG_ZOOMVIEW_CHANGESTATE = 14;
    private static final int TILE_SIZE_POW = 8;
    private volatile double changeGridRatio = WeightedLatLng.DEFAULT_INTENSITY;
    private volatile double changeRatio = WeightedLatLng.DEFAULT_INTENSITY;
    private volatile int changedCounter = 0;
    private int grid_x = 0;
    private int grid_y = 0;
    private boolean isBearingChanged = false;
    private boolean isBuildingEnable = true;
    private boolean isCenterChanged = false;
    private boolean isIndoorEnable = false;
    private boolean isMapTextEnable = true;
    private boolean isNeedUpdateMapRectNextFrame = false;
    private boolean isNeedUpdateZoomControllerState = false;
    private boolean isSetLimitZoomLevel;
    private boolean isTiltChanged = false;
    private boolean isTrafficEnabled = false;
    private boolean isZoomChanged = false;
    MapConfig last_mapconfig = null;
    private IPoint[] limitIPoints;
    private LatLngBounds limitLatLngBounds;
    private float limitZoomLevel;
    private float mapPerPixelUnitLength;
    private FPoint[] mapRect = null;
    public float maxZoomLevel = MAX_ZOOM;
    public float minZoomLevel = MIN_ZOOM;
    private float s_c = 0.0f;
    private float s_r = 0.0f;
    private int s_x = 221010267;
    private int s_y = 101697799;
    private float s_z = 10.0f;

    public MapConfig(boolean z) {
        if (z) {
            this.last_mapconfig = new MapConfig(false);
            this.last_mapconfig.setGridXY(0, 0);
            this.last_mapconfig.setS_x(0);
            this.last_mapconfig.setS_y(0);
            this.last_mapconfig.setS_z(0.0f);
            this.last_mapconfig.setS_c(0.0f);
            this.last_mapconfig.setS_r(0.0f);
        }
    }

    public int getChangedCounter() {
        return this.changedCounter;
    }

    public void resetChangedCounter() {
        this.changedCounter = 0;
    }

    public boolean isMapStateChange() {
        boolean z = true;
        if (this.last_mapconfig == null) {
            return false;
        }
        boolean z2;
        int s_x = this.last_mapconfig.getS_x();
        int s_y = this.last_mapconfig.getS_y();
        float s_z = this.last_mapconfig.getS_z();
        float s_c = this.last_mapconfig.getS_c();
        float s_r = this.last_mapconfig.getS_r();
        this.isCenterChanged = s_x != this.s_x;
        if (s_y == this.s_y) {
            z2 = this.isCenterChanged;
        } else {
            z2 = true;
        }
        this.isCenterChanged = z2;
        if (s_z != this.s_z) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.isZoomChanged = z2;
        if (this.isZoomChanged) {
            if (s_z <= this.minZoomLevel) {
                z2 = true;
            } else {
                z2 = false;
            }
            if (!z2) {
                if (!(this.s_z <= this.minZoomLevel)) {
                    if (!(s_z >= this.maxZoomLevel) && this.s_z < this.maxZoomLevel) {
                        this.isNeedUpdateZoomControllerState = false;
                    }
                }
            }
            this.isNeedUpdateZoomControllerState = true;
        }
        if (s_c != this.s_c) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.isTiltChanged = z2;
        if (s_r != this.s_r) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.isBearingChanged = z2;
        if (!(this.isCenterChanged || this.isZoomChanged || this.isTiltChanged || this.isBearingChanged || this.isNeedUpdateMapRectNextFrame)) {
            z = false;
        }
        if (!z) {
            return z;
        }
        this.isNeedUpdateMapRectNextFrame = false;
        this.changedCounter++;
        s_x = (int) this.s_z;
        setGridXY(this.s_x >> ((20 - s_x) + 8), this.s_y >> ((20 - s_x) + 8));
        changeRatio();
        return z;
    }

    protected void setGridXY(int i, int i2) {
        if (this.last_mapconfig != null) {
            this.last_mapconfig.setGridXY(this.grid_x, this.grid_y);
        }
        this.grid_x = i;
        this.grid_y = i2;
    }

    protected int getGrid_X() {
        return this.grid_x;
    }

    protected int getGrid_Y() {
        return this.grid_y;
    }

    public double getChangeRatio() {
        return this.changeRatio;
    }

    public double getChangeGridRatio() {
        return this.changeGridRatio;
    }

    private void changeRatio() {
        float f = WMElement.CAMERASIZEVALUE1B1;
        double d = WeightedLatLng.DEFAULT_INTENSITY;
        int s_x = this.last_mapconfig.getS_x();
        int s_y = this.last_mapconfig.getS_y();
        float s_z = this.last_mapconfig.getS_z();
        float s_c = this.last_mapconfig.getS_c();
        float s_r = this.last_mapconfig.getS_r();
        this.changeRatio = (double) (Math.abs(this.s_x - s_x) + Math.abs(this.s_y - s_y));
        this.changeRatio = (s_z == this.s_z ? WeightedLatLng.DEFAULT_INTENSITY : (double) Math.abs(s_z - this.s_z)) * this.changeRatio;
        float abs = s_c == this.s_c ? WMElement.CAMERASIZEVALUE1B1 : Math.abs(s_c - this.s_c);
        if (s_r != this.s_r) {
            f = Math.abs(s_r - this.s_r);
        }
        this.changeRatio *= (double) abs;
        this.changeRatio *= (double) f;
        this.changeGridRatio = (double) (Math.abs(this.last_mapconfig.getGrid_X() - this.grid_x) + (this.last_mapconfig.getGrid_Y() - this.grid_y));
        if (this.changeGridRatio != 0.0d) {
            d = this.changeGridRatio;
        }
        this.changeGridRatio = d;
        this.changeGridRatio = ((double) abs) * this.changeGridRatio;
        this.changeGridRatio *= (double) f;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" s_x: ");
        stringBuilder.append(this.s_x);
        stringBuilder.append(" s_y: ");
        stringBuilder.append(this.s_y);
        stringBuilder.append(" s_z: ");
        stringBuilder.append(this.s_z);
        stringBuilder.append(" s_c: ");
        stringBuilder.append(this.s_c);
        stringBuilder.append(" s_r: ");
        stringBuilder.append(this.s_r);
        return stringBuilder.toString();
    }

    public boolean isZoomChanged() {
        return this.isZoomChanged;
    }

    public boolean isTiltChanged() {
        return this.isTiltChanged;
    }

    public boolean isBearingChanged() {
        return this.isBearingChanged;
    }

    public boolean isIndoorEnable() {
        return this.isIndoorEnable;
    }

    public void setIndoorEnable(boolean z) {
        this.isIndoorEnable = z;
    }

    public boolean isBuildingEnable() {
        return this.isBuildingEnable;
    }

    public void setBuildingEnable(boolean z) {
        this.isBuildingEnable = z;
    }

    public boolean isMapTextEnable() {
        return this.isMapTextEnable;
    }

    public void setMapTextEnable(boolean z) {
        this.isMapTextEnable = z;
    }

    public boolean isTrafficEnabled() {
        return this.isTrafficEnabled;
    }

    public void setTrafficEnabled(boolean z) {
        this.isTrafficEnabled = z;
    }

    public boolean isNeedUpdateZoomControllerState() {
        return this.isNeedUpdateZoomControllerState;
    }

    public int getS_x() {
        return this.s_x;
    }

    public void setS_x(int i) {
        if (this.last_mapconfig != null) {
            this.last_mapconfig.setS_x(this.s_x);
        }
        this.s_x = i;
    }

    public int getS_y() {
        return this.s_y;
    }

    public void setS_y(int i) {
        if (this.last_mapconfig != null) {
            this.last_mapconfig.setS_y(this.s_y);
        }
        this.s_y = i;
    }

    public float getS_z() {
        return this.s_z;
    }

    public void setS_z(float f) {
        if (this.last_mapconfig != null) {
            this.last_mapconfig.setS_z(this.s_z);
        }
        this.s_z = f;
    }

    public float getS_c() {
        return this.s_c;
    }

    public void setS_c(float f) {
        if (this.last_mapconfig != null) {
            this.last_mapconfig.setS_c(this.s_c);
        }
        this.s_c = f;
    }

    public float getS_r() {
        return this.s_r;
    }

    public void setS_r(float f) {
        if (this.last_mapconfig != null) {
            this.last_mapconfig.setS_r(this.s_r);
        }
        this.s_r = f;
    }

    public FPoint[] getMapRect() {
        return this.mapRect;
    }

    public void setMapRect(FPoint[] fPointArr) {
        if (this.last_mapconfig != null) {
            this.last_mapconfig.setMapRect(fPointArr);
        }
        this.mapRect = fPointArr;
    }

    public void setMaxZoomLevel(float f) {
        float f2 = MAX_ZOOM;
        float f3 = MIN_ZOOM;
        if (f <= MAX_ZOOM) {
            f2 = f;
        }
        if (f2 >= MIN_ZOOM) {
            f3 = f2;
        }
        this.isSetLimitZoomLevel = true;
        this.maxZoomLevel = f3;
    }

    public void setMinZoomLevel(float f) {
        float f2 = MAX_ZOOM;
        float f3 = MIN_ZOOM;
        if (f >= MIN_ZOOM) {
            f3 = f;
        }
        if (f3 <= MAX_ZOOM) {
            f2 = f3;
        }
        this.isSetLimitZoomLevel = true;
        this.minZoomLevel = f2;
    }

    public float getMaxZoomLevel() {
        return this.maxZoomLevel;
    }

    public float getMinZoomLevel() {
        return this.minZoomLevel;
    }

    public IPoint[] getLimitIPoints() {
        return this.limitIPoints;
    }

    public void setLimitIPoints(IPoint[] iPointArr) {
        this.limitIPoints = iPointArr;
    }

    public float getLimitZoomLevel() {
        return this.limitZoomLevel;
    }

    public void setLimitZoomLevel(float f) {
        this.limitZoomLevel = f;
    }

    public boolean isSetLimitZoomLevel() {
        return this.isSetLimitZoomLevel;
    }

    public LatLngBounds getLimitLatLngBounds() {
        return this.limitLatLngBounds;
    }

    public void setLimitLatLngBounds(LatLngBounds latLngBounds) {
        this.limitLatLngBounds = latLngBounds;
        if (latLngBounds == null) {
            setLimitZoomLevel(0.0f);
            resetMinMaxZoomPreference();
        }
    }

    public void resetMinMaxZoomPreference() {
        this.minZoomLevel = MIN_ZOOM;
        this.maxZoomLevel = MAX_ZOOM;
        this.isSetLimitZoomLevel = false;
    }

    public void updateMapRectNextFrame(boolean z) {
        this.isNeedUpdateMapRectNextFrame = z;
    }

    public void setMapPerPixelUnitLength(float f) {
        this.mapPerPixelUnitLength = f;
    }

    public float getMapPerPixelUnitLength() {
        return this.mapPerPixelUnitLength;
    }
}
