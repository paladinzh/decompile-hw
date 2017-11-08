package com.autonavi.amap.mapcore;

public abstract class MapMessage {
    public static final int GESTURE_STATE_BEGIN = 100;
    public static final int GESTURE_STATE_CHANGE = 101;
    public static final int GESTURE_STATE_END = 102;
    public static final int MAX_CAMERA_HEADER_DEGREE = 65;
    protected static final int MAX_FARCLIPANGLE_CAMERAHEADERANGLE = 40;
    public static final int MSGTYPE_GESTURE_HOVER = 33;
    public static final int MSGTYPE_GESTURE_MOVE = 32;
    public static final int MSGTYPE_GESTURE_ROTATE = 31;
    public static final int MSGTYPE_GESTURE_SCALE = 30;
    public static final int MSGTYPE_NAVIOVERLAY_STATE = 13;
    public static final int MSGTYPE_PARAMETER_SET = 19;
    public int anchorX;
    public int anchorY;
    public float bearing = Float.NaN;
    public IPoint geoPoint;
    public int height = 0;
    public boolean isChangeFinished = false;
    public boolean isUseAnchor = false;
    public MapConfig mapConfig;
    protected int state_ = 0;
    public float tilt = Float.NaN;
    public int width = 0;
    public float zoom = Float.NaN;

    public abstract void runCameraUpdate(MapProjection mapProjection);

    protected void win2geo(MapProjection mapProjection, int i, int i2, IPoint iPoint) {
        FPoint fPoint = new FPoint();
        mapProjection.win2Map(i, i2, fPoint);
        mapProjection.map2Geo(fPoint.x, fPoint.y, iPoint);
    }

    public ADGLMapAnimGroup generateMapAnimation(MapProjection mapProjection, int i) {
        runCameraUpdate(mapProjection);
        ADGLMapAnimGroup aDGLMapAnimGroup = new ADGLMapAnimGroup(i);
        aDGLMapAnimGroup.setToCameraDegree(mapProjection.getCameraHeaderAngle(), 0);
        aDGLMapAnimGroup.setToMapAngle(mapProjection.getMapAngle(), 0);
        aDGLMapAnimGroup.setToMapLevel(mapProjection.getMapZoomer(), 0);
        IPoint iPoint = new IPoint();
        mapProjection.getGeoCenter(iPoint);
        aDGLMapAnimGroup.setToMapCenterGeo(iPoint.x, iPoint.y, 0);
        return aDGLMapAnimGroup;
    }

    public void mergeCameraUpdateDelegate(MapMessage mapMessage) {
    }

    public int getMapGestureState() {
        return this.state_;
    }
}
