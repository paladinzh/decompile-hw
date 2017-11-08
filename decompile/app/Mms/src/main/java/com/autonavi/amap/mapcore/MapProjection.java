package com.autonavi.amap.mapcore;

public class MapProjection {
    private boolean m_bNewInstance = false;
    long native_instance = 0;

    private static native void nativeDestroy(long j);

    private static native void nativeGeo2LonLat(int i, int i2, DPoint dPoint);

    private static native void nativeGeo2Map(long j, int i, int i2, FPoint fPoint);

    private static native void nativeGetBound(long j, IPoint iPoint);

    private static native float nativeGetCameraHeaderAngle(long j);

    private static native void nativeGetCenterMap(long j, FPoint fPoint);

    private static native void nativeGetGeoCenter(long j, IPoint iPoint);

    private static native float nativeGetMapAngle(long j);

    private static native void nativeGetMapCenter(long j, FPoint fPoint);

    private static native float nativeGetMapLenWithGeo(long j, int i);

    private static native float nativeGetMapLenWithWin(long j, int i);

    private static native float nativeGetMapLenWithWinbyY(long j, int i, int i2);

    private static native float nativeGetMapZoomer(long j);

    private static native void nativeLonLat2Geo(double d, double d2, IPoint iPoint);

    private static native void nativeMap2Geo(long j, float f, float f2, IPoint iPoint);

    private static native void nativeMap2Win(long j, float f, float f2, IPoint iPoint);

    private static native long nativeNewInstance(long j);

    private static native void nativeRecalculate(long j);

    private static native void nativeSetCameraHeaderAngle(long j, float f);

    private static native void nativeSetCenterWithMap(long j, float f, float f2);

    private static native void nativeSetGeoCenter(long j, int i, int i2);

    private static native void nativeSetMapAngle(long j, float f);

    private static native void nativeSetMapCenter(long j, float f, float f2);

    private static native void nativeSetMapZoomer(long j, float f);

    private static native void nativeWin2Map(long j, int i, int i2, FPoint fPoint);

    public MapProjection(MapCore mapCore) {
        this.native_instance = nativeNewInstance(mapCore.getInstanceHandle());
        this.m_bNewInstance = true;
    }

    public MapProjection(long j) {
        this.native_instance = j;
        this.m_bNewInstance = false;
    }

    public void recycle() {
        if (this.m_bNewInstance) {
            nativeDestroy(this.native_instance);
        }
    }

    long getInstanceHandle() {
        return this.native_instance;
    }

    public void getBound(IPoint iPoint) {
        nativeGetBound(this.native_instance, iPoint);
    }

    public void setGeoCenter(int i, int i2) {
        nativeSetGeoCenter(this.native_instance, i, i2);
    }

    public void getGeoCenter(IPoint iPoint) {
        nativeGetGeoCenter(this.native_instance, iPoint);
    }

    public void setMapCenter(float f, float f2) {
        nativeSetMapCenter(this.native_instance, f, f2);
    }

    public void getMapCenter(FPoint fPoint) {
        nativeGetMapCenter(this.native_instance, fPoint);
    }

    public void setMapZoomer(float f) {
        nativeSetMapZoomer(this.native_instance, f);
    }

    public float getMapZoomer() {
        return nativeGetMapZoomer(this.native_instance);
    }

    public void setMapAngle(float f) {
        nativeSetMapAngle(this.native_instance, f);
    }

    public float getMapAngle() {
        return nativeGetMapAngle(this.native_instance);
    }

    public void setCameraHeaderAngle(float f) {
        nativeSetCameraHeaderAngle(this.native_instance, f);
    }

    public float getCameraHeaderAngle() {
        return nativeGetCameraHeaderAngle(this.native_instance);
    }

    public void geo2Map(int i, int i2, FPoint fPoint) {
        nativeGeo2Map(this.native_instance, i, i2, fPoint);
    }

    public void map2Win(float f, float f2, IPoint iPoint) {
        nativeMap2Win(this.native_instance, f, f2, iPoint);
    }

    public void win2Map(int i, int i2, FPoint fPoint) {
        nativeWin2Map(this.native_instance, i, i2, fPoint);
    }

    public float getMapLenWithWinbyY(int i, int i2) {
        return nativeGetMapLenWithWinbyY(this.native_instance, i, i2);
    }

    public float getMapLenWithWin(int i) {
        return nativeGetMapLenWithWin(this.native_instance, i);
    }

    public void map2Geo(float f, float f2, IPoint iPoint) {
        nativeMap2Geo(this.native_instance, f, f2, iPoint);
    }

    public void setCenterWithMap(float f, float f2) {
        nativeSetCenterWithMap(this.native_instance, f, f2);
    }

    public void getCenterMap(FPoint fPoint) {
        nativeGetCenterMap(this.native_instance, fPoint);
    }

    public static void lonlat2Geo(double d, double d2, IPoint iPoint) {
        nativeLonLat2Geo(d, d2, iPoint);
    }

    public static void geo2LonLat(int i, int i2, DPoint dPoint) {
        nativeGeo2LonLat(i, i2, dPoint);
    }

    public float getMapLenWithGeo(int i) {
        return nativeGetMapLenWithGeo(this.native_instance, i);
    }

    public void recalculate() {
        nativeRecalculate(this.native_instance);
    }
}
