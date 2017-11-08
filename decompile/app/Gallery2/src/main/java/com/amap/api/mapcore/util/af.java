package com.amap.api.mapcore.util;

import android.graphics.Point;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLngBounds;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapMessage;
import com.autonavi.amap.mapcore.MapProjection;

/* compiled from: CameraUpdateDelegate */
public abstract class af extends MapMessage {
    public a a = a.none;
    public float b;
    public float c;
    public float d;
    public CameraPosition e;
    public LatLngBounds f;
    public Point g = null;
    public int h;
    public int i;
    public int j;
    public int k;

    /* compiled from: CameraUpdateDelegate */
    public enum a {
        none,
        zoomIn,
        changeCenter,
        changeTilt,
        changeBearing,
        changeBearingGeoCenter,
        changeGeoCenterZoom,
        zoomOut,
        zoomTo,
        zoomBy,
        scrollBy,
        newCameraPosition,
        newLatLngBounds,
        newLatLngBoundsWithSize,
        changeGeoCenterZoomTiltBearing
    }

    protected void a(MapProjection mapProjection) {
        this.zoom = !Float.isNaN(this.zoom) ? this.zoom : mapProjection.getMapZoomer();
        this.bearing = !Float.isNaN(this.bearing) ? this.bearing : mapProjection.getMapAngle();
        this.tilt = !Float.isNaN(this.tilt) ? this.tilt : mapProjection.getCameraHeaderAngle();
        this.zoom = eh.a(this.mapConfig, this.zoom);
        this.tilt = eh.a(this.tilt, this.zoom);
        this.bearing = (float) (((((double) this.bearing) % 360.0d) + 360.0d) % 360.0d);
        if (this.isUseAnchor) {
            if (this.geoPoint == null) {
                this.geoPoint = a(mapProjection, this.anchorX, this.anchorY);
            }
        } else if (this.g != null && this.geoPoint == null) {
            this.geoPoint = a(mapProjection, this.g.x, this.g.y);
        }
        if (!Float.isNaN(this.zoom)) {
            mapProjection.setMapZoomer(this.zoom);
        }
        if (!Float.isNaN(this.bearing)) {
            mapProjection.setMapAngle(this.bearing);
        }
        if (!Float.isNaN(this.tilt)) {
            mapProjection.setCameraHeaderAngle(this.tilt);
        }
        if (this.isUseAnchor) {
            a(mapProjection, this.geoPoint);
        } else if (this.g != null) {
            a(mapProjection, this.geoPoint, this.g.x, this.g.y);
        } else if (this.geoPoint != null) {
            if (this.geoPoint.x != 0 || this.geoPoint.y != 0) {
                mapProjection.setGeoCenter(this.geoPoint.x, this.geoPoint.y);
            }
        }
    }

    protected void a(MapProjection mapProjection, IPoint iPoint) {
        a(mapProjection, iPoint, this.anchorX, this.anchorY);
    }

    protected void a(MapProjection mapProjection, IPoint iPoint, int i, int i2) {
        mapProjection.recalculate();
        IPoint a = a(mapProjection, i, i2);
        IPoint iPoint2 = new IPoint();
        mapProjection.getGeoCenter(iPoint2);
        mapProjection.setGeoCenter((iPoint2.x + iPoint.x) - a.x, (iPoint2.y + iPoint.y) - a.y);
    }

    protected IPoint a(MapProjection mapProjection, int i, int i2) {
        FPoint fPoint = new FPoint();
        mapProjection.win2Map(i, i2, fPoint);
        IPoint iPoint = new IPoint();
        mapProjection.map2Geo(fPoint.x, fPoint.y, iPoint);
        return iPoint;
    }
}
