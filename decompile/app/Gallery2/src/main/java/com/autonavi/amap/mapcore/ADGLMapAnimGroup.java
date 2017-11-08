package com.autonavi.amap.mapcore;

import android.os.SystemClock;
import com.amap.api.mapcore.util.y;
import com.amap.api.mapcore.util.z;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.huawei.watermark.manager.parse.WMElement;

public class ADGLMapAnimGroup extends ADGLAnimation {
    public static final int CAMERA_MAX_DEGREE = 60;
    public static final int CAMERA_MIN_DEGREE = 0;
    public static final int MAXMAPLEVEL = 20;
    public static final int MINMAPLEVEL = 3;
    int _endZoomDuration;
    boolean _hasCheckParams;
    boolean _hasMidZoom;
    int _midZoomDuration;
    z _moveParam = null;
    public boolean _needMove;
    boolean _needRotateCamera;
    boolean _needRotateMap;
    boolean _needZoom;
    y _rotateCameraParam = null;
    y _rotateMapParam = null;
    int _startZoomDuration;
    y _zoomEndParam = null;
    y _zoomStartParam = null;

    public ADGLMapAnimGroup(int i) {
        reset();
        this._duration = i;
    }

    public void setDuration(int i) {
        this._duration = i;
    }

    public void reset() {
        this._isOver = false;
        this._hasCheckParams = false;
        this._needZoom = false;
        this._needMove = false;
        this._moveParam = null;
        this._needRotateMap = false;
        this._rotateMapParam = null;
        this._hasMidZoom = false;
        this._duration = 0;
        if (this._rotateMapParam != null) {
            this._rotateMapParam.a();
        }
        if (this._moveParam != null) {
            this._moveParam.a();
        }
        if (this._zoomStartParam != null) {
            this._zoomStartParam.a();
        }
        if (this._zoomEndParam != null) {
            this._zoomEndParam.a();
        }
        if (this._rotateCameraParam != null) {
            this._rotateCameraParam.a();
        }
    }

    public boolean isValid() {
        if (this._needRotateCamera || this._needRotateMap || this._needMove || this._needZoom) {
            return true;
        }
        return false;
    }

    public void setToMapAngle(float f, int i) {
        float f2 = f % 360.0f;
        this._needRotateMap = true;
        if (this._rotateMapParam == null) {
            this._rotateMapParam = new y();
        }
        this._rotateMapParam.a();
        this._rotateMapParam.a(i, WMElement.CAMERASIZEVALUE1B1);
        this._rotateMapParam.d(f2);
    }

    public void setToMapCenterGeo(int i, int i2, int i3) {
        if (i > 0 && i2 > 0) {
            this._needMove = true;
            if (this._moveParam == null) {
                this._moveParam = new z();
            }
            this._moveParam.a();
            this._moveParam.a(i3, WMElement.CAMERASIZEVALUE1B1);
            this._moveParam.b((float) i, (float) i2);
        }
    }

    public void setToMapLevel(float f, int i) {
        this._needZoom = true;
        this._midZoomDuration = 0;
        this._hasMidZoom = false;
        if (checkLevel(f)) {
            initZoomStartParam(f, i);
        } else {
            this._needZoom = false;
        }
    }

    public void setToMapLevel(float f, float f2, int i) {
        this._needZoom = true;
        this._midZoomDuration = 0;
        this._hasMidZoom = false;
        if (i > 0 && i < this._duration) {
            this._midZoomDuration = i;
        }
        if (checkLevel(f) && checkLevel(f2)) {
            this._hasMidZoom = true;
            initZoomStartParam(f2, 0);
            initZoomEndParam(f2, f, 0);
        } else if (checkLevel(f)) {
            this._hasMidZoom = false;
            initZoomStartParam(f, 0);
        } else if (checkLevel(f2)) {
            this._hasMidZoom = false;
            initZoomStartParam(f2, 0);
        } else {
            this._needZoom = false;
        }
    }

    public void setToCameraDegree(float f, int i) {
        this._needRotateCamera = false;
        if (f <= BitmapDescriptorFactory.HUE_YELLOW && f >= 0.0f) {
            this._needRotateCamera = true;
            if (this._rotateCameraParam == null) {
                this._rotateCameraParam = new y();
            }
            this._rotateCameraParam.a();
            this._rotateCameraParam.a(i, WMElement.CAMERASIZEVALUE1B1);
            this._rotateCameraParam.d(f);
        }
    }

    public static boolean checkLevel(float f) {
        return f >= MapConfig.MIN_ZOOM && f <= MapConfig.MAX_ZOOM_INDOOR;
    }

    private void initZoomStartParam(float f, int i) {
        if (this._zoomStartParam == null) {
            this._zoomStartParam = new y();
        }
        this._zoomStartParam.a();
        this._zoomStartParam.a(i, WMElement.CAMERASIZEVALUE1B1);
        this._zoomStartParam.d(f);
    }

    private void initZoomEndParam(float f, float f2, int i) {
        if (this._zoomEndParam == null) {
            this._zoomEndParam = new y();
        }
        this._zoomEndParam.a();
        this._zoomEndParam.a(i, WMElement.CAMERASIZEVALUE1B1);
        this._zoomEndParam.d(f2);
        this._zoomEndParam.c(f);
    }

    public void commitAnimation(Object obj) {
        this._isOver = true;
        this._hasCheckParams = false;
        MapProjection mapProjection = (MapProjection) obj;
        if (mapProjection != null) {
            float mapZoomer;
            float e;
            if (this._needZoom) {
                if (this._zoomStartParam != null) {
                    mapZoomer = mapProjection.getMapZoomer();
                    this._zoomStartParam.c(mapZoomer);
                    if (this._hasMidZoom) {
                        boolean z;
                        e = this._zoomEndParam.e() - this._zoomEndParam.f();
                        if (((double) Math.abs(this._zoomStartParam.f() - mapZoomer)) < 1.0E-6d) {
                            z = true;
                        } else {
                            z = false;
                        }
                        if (z || ((double) Math.abs(e)) < 1.0E-6d) {
                            this._hasMidZoom = false;
                            this._zoomStartParam.d(this._zoomEndParam.f());
                            this._zoomStartParam.b();
                            this._zoomEndParam = null;
                        } else {
                            this._zoomStartParam.b();
                            this._zoomEndParam.b();
                        }
                    }
                    if (!this._hasMidZoom && ((double) Math.abs(this._zoomStartParam.e() - this._zoomStartParam.f())) < 1.0E-6d) {
                        this._needZoom = false;
                    }
                    if (this._needZoom) {
                        if (this._hasMidZoom) {
                            this._startZoomDuration = (this._duration - this._midZoomDuration) >> 1;
                            this._endZoomDuration = this._startZoomDuration;
                        } else {
                            this._startZoomDuration = this._duration;
                        }
                    }
                } else {
                    this._hasCheckParams = true;
                    return;
                }
            }
            if (this._needMove && this._moveParam != null) {
                IPoint iPoint = new IPoint();
                mapProjection.getGeoCenter(iPoint);
                this._moveParam.a((float) iPoint.x, (float) iPoint.y);
                this._needMove = this._moveParam.b();
            }
            if (this._needRotateMap && this._rotateMapParam != null) {
                e = mapProjection.getMapAngle();
                mapZoomer = this._rotateMapParam.f();
                if (e > BitmapDescriptorFactory.HUE_CYAN && mapZoomer == 0.0f) {
                    mapZoomer = 360.0f;
                }
                int i = ((int) mapZoomer) - ((int) e);
                if (i > 180) {
                    mapZoomer -= 360.0f;
                } else if (i < -180) {
                    mapZoomer += 360.0f;
                }
                this._rotateMapParam.c(e);
                this._rotateMapParam.d(mapZoomer);
                this._needRotateMap = this._rotateMapParam.b();
            }
            if (this._needRotateCamera && this._rotateCameraParam != null) {
                this._rotateCameraParam.c(mapProjection.getCameraHeaderAngle());
                this._needRotateCamera = this._rotateCameraParam.b();
            }
            if (this._needMove || this._needZoom || this._needRotateMap || this._needRotateCamera) {
                this._isOver = false;
            } else {
                this._isOver = true;
            }
            this._hasCheckParams = true;
            this._startTime = SystemClock.uptimeMillis();
        }
    }

    public void doAnimation(Object obj) {
        float f = WMElement.CAMERASIZEVALUE1B1;
        MapProjection mapProjection = (MapProjection) obj;
        if (mapProjection != null) {
            if (!this._hasCheckParams) {
                commitAnimation(obj);
            }
            if (!this._isOver) {
                this._offsetTime = SystemClock.uptimeMillis() - this._startTime;
                if (((float) this._duration) == 0.0f) {
                    this._isOver = true;
                    return;
                }
                float f2 = ((float) this._offsetTime) / ((float) this._duration);
                if (f2 > WMElement.CAMERASIZEVALUE1B1) {
                    this._isOver = true;
                } else if (f2 < 0.0f) {
                    this._isOver = true;
                    return;
                } else {
                    f = f2;
                }
                if (this._needZoom) {
                    mapProjection.getMapZoomer();
                    if (this._hasMidZoom) {
                        if (this._offsetTime > ((long) this._startZoomDuration)) {
                            boolean z;
                            if (this._offsetTime > ((long) (this._startZoomDuration + this._midZoomDuration))) {
                                z = true;
                            } else {
                                z = false;
                            }
                            if (z) {
                                this._zoomEndParam.b(((float) ((this._offsetTime - ((long) this._startZoomDuration)) - ((long) this._midZoomDuration))) / ((float) this._endZoomDuration));
                                f2 = this._zoomEndParam.g();
                            } else {
                                f2 = this._zoomStartParam.f();
                            }
                        } else {
                            this._zoomStartParam.b(((float) this._offsetTime) / ((float) this._startZoomDuration));
                            f2 = this._zoomStartParam.g();
                        }
                        if (this._isOver) {
                            f2 = this._zoomEndParam.f();
                        }
                    } else {
                        this._zoomStartParam.b(f);
                        f2 = this._zoomStartParam.g();
                    }
                    mapProjection.setMapZoomer(f2);
                }
                if (this._moveParam != null && this._needMove) {
                    this._moveParam.b(f);
                    int e = (int) this._moveParam.e();
                    int f3 = (int) this._moveParam.f();
                    int g = (int) this._moveParam.g();
                    int h = (int) this._moveParam.h();
                    float c = this._moveParam.c();
                    mapProjection.setGeoCenter(e, f3);
                    FPoint fPoint = new FPoint();
                    mapProjection.getMapCenter(fPoint);
                    float f4 = fPoint.x;
                    f2 = fPoint.y;
                    FPoint fPoint2 = new FPoint();
                    mapProjection.geo2Map(g, h, fPoint2);
                    mapProjection.setMapCenter(f4 + ((fPoint2.x - f4) * c), f2 + ((fPoint2.y - f2) * c));
                }
                if (this._rotateMapParam != null && this._needRotateMap) {
                    this._rotateMapParam.b(f);
                    mapProjection.setMapAngle((float) ((int) this._rotateMapParam.g()));
                }
                if (this._rotateCameraParam != null && this._needRotateCamera) {
                    this._rotateCameraParam.b(f);
                    mapProjection.setCameraHeaderAngle((float) ((int) this._rotateCameraParam.g()));
                }
            }
        }
    }

    public boolean typeEqueal(ADGLMapAnimGroup aDGLMapAnimGroup) {
        if (this._needRotateCamera == aDGLMapAnimGroup._needRotateCamera && this._needRotateMap == aDGLMapAnimGroup._needRotateMap && this._needZoom == aDGLMapAnimGroup._needZoom && this._needMove == aDGLMapAnimGroup._needMove) {
            return true;
        }
        return false;
    }
}
