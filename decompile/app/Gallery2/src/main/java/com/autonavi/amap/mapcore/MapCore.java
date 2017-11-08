package com.autonavi.amap.mapcore;

import android.content.Context;
import android.text.TextUtils;
import com.amap.api.mapcore.util.aa;
import com.amap.api.mapcore.util.ab;
import com.amap.api.mapcore.util.eh;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Poi;
import com.autonavi.amap.mapcore.interfaces.IAMap;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.opengles.GL10;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class MapCore {
    public static final int AM_DATA_BMP_BASEMAP = 2;
    public static final int AM_DATA_GEO_BUILDING = 1;
    public static final int AM_DATA_GUIDE = 11;
    public static final int AM_DATA_INDOOR = 10;
    public static final int AM_DATA_MODEL = 6;
    public static final int AM_DATA_POI = 8;
    public static final int AM_DATA_ROADMAP = 0;
    public static final int AM_DATA_SATELLITE = 3;
    public static final int AM_DATA_SCENIC_WIDGET = 101;
    public static final int AM_DATA_SCREEN = 5;
    public static final int AM_DATA_STANDARD = 7;
    public static final int AM_DATA_VEC_TMC = 4;
    public static final int AM_DATA_VERSION = 9;
    public static final int MAPRENDER_BASEMAPBEGIN = 1;
    public static final int MAPRENDER_BUILDINGBEGIN = 2;
    public static final int MAPRENDER_CAN_STOP_AND_FULLSCREEN_RENDEROVER = 999;
    public static final int MAPRENDER_ENTER = 0;
    public static final int MAPRENDER_GRID_CAN_FILL = 11;
    public static final int MAPRENDER_LABELSBEGIN = 3;
    public static final int MAPRENDER_LABELSEND = 4;
    public static final int MAPRENDER_NOMORENEEDRENDER = 6;
    public static final int MAPRENDER_ORTHOPROJECTION = 7;
    public static final int MAPRENDER_RENDEROVER = 5;
    public static final int TEXTURE_BACKGROUND = 1;
    public static final int TEXTURE_ICON = 0;
    public static final int TEXTURE_RAILWAY = 8;
    public static final int TEXTURE_ROADARROW = 2;
    public static final int TEXTURE_ROADROUND = 3;
    public static final int TEXTURE_TMC_BLACK = 7;
    public static final int TEXTURE_TMC_GREEN = 6;
    public static final int TEXTURE_TMC_RED = 4;
    public static final int TEXTURE_TMC_YELLOW = 5;
    private int browserCount = 0;
    boolean isAnimationStep = false;
    boolean isGestureStep = false;
    boolean isMoveCameraStep = false;
    Context mContext;
    GL10 mGL = null;
    private CopyOnWriteArrayList<MapMessage> mGestureMessageList = new CopyOnWriteArrayList();
    IAMap mMap = null;
    private IMapCallback mMapcallback = null;
    private CopyOnWriteArrayList<MapMessage> mStateMessageList = new CopyOnWriteArrayList();
    private ab map_anims_mgr = null;
    private int map_gesture_count = 0;
    long native_instance = 0;
    long native_mapprojection_instance = 0;
    private int surface_height = 0;
    private int surface_width = 0;
    private TextTextureGenerator textTextureGenerator = null;
    byte[] tmp_3072bytes_data;

    private static native void nativeAddPoiFilter(long j, int i, int i2, int i3, float f, float f2, String str);

    private static native boolean nativeCanStopRenderMap(long j);

    private static native void nativeChangeMapEnv(long j, String str);

    private static native void nativeClearPoiFilter(long j);

    private static native void nativeDestroy(long j, MapCore mapCore);

    private static native int nativeGetMapStateInstance(long j);

    private static native long nativeGetMapstate(long j);

    private static native void nativeGetScreenGrids(long j, byte[] bArr, int i);

    private static native int nativeGetSelectedMapPois(long j, int i, int i2, int i3, byte[] bArr);

    private static native long nativeNewInstance(String str, String str2);

    private static native void nativePutCharbitmap(long j, int i, byte[] bArr);

    private static native int nativePutMapdata(long j, int i, byte[] bArr);

    private static native int nativePutMapdata(long j, int i, byte[] bArr, int i2);

    private static native void nativeRemovePoiFilter(long j, String str);

    private static native int nativeSelectMapPois(long j, int i, int i2, int i3, byte[] bArr);

    private static native void nativeSetCityBound(long j, byte[] bArr);

    private static native void nativeSetIndoorBuildingToBeActive(long j, String str, int i, String str2);

    private static native void nativeSetInternalTexture(long j, byte[] bArr, int i);

    private static native void nativeSetInternaltexture(long j, byte[] bArr, int i);

    private static native void nativeSetMapstate(long j, long j2);

    private static native void nativeSetParmater(long j, int i, int i2, int i3, int i4, int i5);

    private static native void nativeSetStyleData(long j, byte[] bArr, int i, int i2);

    private static native void nativeSetparameter(long j, int i, int i2, int i3, int i4, int i5);

    private static native void nativeSurfaceChange(long j, MapCore mapCore, int i, int i2);

    private static native void nativeSurfaceCreate(long j, MapCore mapCore);

    private static native void nativeSurfaceRenderMap(long j, MapCore mapCore);

    static {
        try {
            System.loadLibrary("gdinamapv4sdk752");
            System.loadLibrary("gdinamapv4sdk752ex");
        } catch (Throwable th) {
        }
    }

    public MapCore(Context context, IAMap iAMap) {
        this.mContext = context;
        this.mMap = iAMap;
        MapTilsCacheAndResManager.getInstance(context).checkDir();
    }

    public void newMap() {
        MapTilsCacheAndResManager.getInstance(this.mContext).checkDir();
        String baseMapPath = MapTilsCacheAndResManager.getInstance(this.mContext).getBaseMapPath();
        this.map_anims_mgr = new ab();
        this.textTextureGenerator = new TextTextureGenerator();
        this.tmp_3072bytes_data = ByteBuffer.allocate(3072).array();
        this.native_instance = nativeNewInstance(baseMapPath, this.textTextureGenerator.getFontVersion());
        if (this.native_instance != 0) {
            this.native_mapprojection_instance = nativeGetMapstate(this.native_instance);
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public void setGL(GL10 gl10) {
        this.mGL = gl10;
    }

    public void setMapCallback(IMapCallback iMapCallback) {
        this.mMapcallback = iMapCallback;
    }

    public boolean isMapEngineValid() {
        return this.native_instance != 0;
    }

    public void surfaceCreate(GL10 gl10) {
        if (this.native_instance != 0) {
            nativeSurfaceCreate(this.native_instance, this);
        }
    }

    public void surfaceChange(GL10 gl10, int i, int i2) {
        int i3 = 120;
        if (this.native_instance != 0) {
            int i4;
            nativeSurfaceChange(this.native_instance, this, i, i2);
            int i5 = this.mContext.getResources().getDisplayMetrics().densityDpi;
            float f = this.mContext.getResources().getDisplayMetrics().density;
            int i6 = 100;
            if (i5 <= 120) {
                i4 = 1;
                i5 = 50;
            } else if (i5 <= SmsCheckResult.ESCT_160) {
                int i7;
                if (Math.max(i, i2) > 480) {
                    i7 = 100;
                    i3 = SmsCheckResult.ESCT_160;
                } else {
                    i7 = 120;
                }
                i4 = 1;
                i5 = i3;
                i6 = i7;
            } else if (i5 > 240) {
                if (i5 <= SmsCheckResult.ESCT_320) {
                    i5 = 180;
                    i4 = 3;
                    i6 = 50;
                } else if (i5 > 480) {
                    i5 = 360;
                    i4 = 4;
                    i6 = 40;
                } else {
                    i5 = 300;
                    i4 = 3;
                    i6 = 50;
                }
            } else if (Math.min(i, i2) < 1000) {
                i5 = 150;
                i4 = 2;
                i6 = 70;
            } else {
                i5 = SmsCheckResult.ESCT_200;
                i4 = 2;
                i6 = 60;
            }
            setParameter(2051, i6, i5, (int) (f * 100.0f), i4);
            this.mMap.setZoomScaleParam(((float) i6) / 100.0f);
            setParameter(ERROR_CODE.CONN_CREATE_FALSE, 0, 0, 0, 0);
            setParameter(1023, 1, 0, 0, 0);
        }
    }

    public void drawFrame(GL10 gl10) {
        if (this.native_instance != 0) {
            processMessage();
            nativeSurfaceRenderMap(this.native_instance, this);
        }
    }

    private void processMessage() {
        try {
            processGestureMessage();
            if (this.mGestureMessageList.size() <= 0) {
                processStateMapMessage();
            } else if (this.mStateMessageList.size() > 0) {
                this.mStateMessageList.clear();
            }
        } catch (Exception e) {
        }
    }

    private void processGestureMessage() {
        if (this.mGestureMessageList.size() > 0) {
            this.isGestureStep = true;
            MapProjection mapstate = getMapstate();
            if (mapstate != null) {
                mapstate.recalculate();
                while (this.mGestureMessageList.size() > 0) {
                    MapMessage mapMessage = (MapMessage) this.mGestureMessageList.remove(0);
                    if (mapMessage == null) {
                        break;
                    }
                    if (mapMessage.width == 0) {
                        mapMessage.width = this.surface_width;
                    }
                    if (mapMessage.height == 0) {
                        mapMessage.height = this.surface_height;
                    }
                    int mapGestureState = mapMessage.getMapGestureState();
                    if (mapGestureState == 100) {
                        gestureBegin();
                    } else if (mapGestureState == 101) {
                        mapMessage.runCameraUpdate(mapstate);
                    } else if (mapGestureState == 102) {
                        gestureEnd();
                    }
                }
                setMapstate(mapstate);
                mapstate.recycle();
                return;
            }
            return;
        }
        if (this.isGestureStep) {
            this.isGestureStep = false;
            if (this.map_anims_mgr.b() <= 0) {
                this.mMap.onChangeFinish();
            }
        }
    }

    private void processStateMapMessage() {
        if (this.mStateMessageList.size() > 0) {
            this.isMoveCameraStep = true;
            MapProjection mapstate = getMapstate();
            if (mapstate != null) {
                while (this.mStateMessageList.size() > 0) {
                    MapMessage mapMessage = (MapMessage) this.mStateMessageList.remove(0);
                    if (mapMessage == null) {
                        break;
                    }
                    if (mapMessage.width == 0) {
                        mapMessage.width = this.surface_width;
                    }
                    if (mapMessage.height == 0) {
                        mapMessage.height = this.surface_height;
                    }
                    mapstate.recalculate();
                    mapMessage.runCameraUpdate(mapstate);
                }
                setMapstate(mapstate);
                mapstate.recycle();
                return;
            }
            return;
        }
        if (this.isMoveCameraStep) {
            this.isMoveCameraStep = false;
            this.mMap.onChangeFinish();
        }
    }

    private void gestureBegin() {
        this.map_gesture_count++;
    }

    private void gestureEnd() {
        this.map_gesture_count--;
    }

    public synchronized void clearAllMessages() {
        clearAllGestureMessage();
        this.mStateMessageList.clear();
    }

    public synchronized void clearAllGestureMessage() {
        this.mGestureMessageList.clear();
        this.map_gesture_count = 0;
    }

    public synchronized void addMessage(MapMessage mapMessage) {
        if (mapMessage != null) {
            this.mStateMessageList.add(mapMessage);
        }
    }

    public synchronized int getMapCount() {
        return this.mStateMessageList.size();
    }

    public synchronized MapMessage getStateMessage() {
        if (getMapCount() == 0) {
            return null;
        }
        MapMessage mapMessage = (MapMessage) this.mStateMessageList.get(0);
        this.mStateMessageList.remove(mapMessage);
        return mapMessage;
    }

    public synchronized int getStateMessageCount() {
        return this.mStateMessageList.size();
    }

    public synchronized void addGestureMessage(MapMessage mapMessage) {
        if (mapMessage != null) {
            this.mGestureMessageList.add(mapMessage);
        }
    }

    public void startMapSlidAnim(final IPoint iPoint, final float f, final float f2) {
        this.mMap.onFling();
        this.mMap.getMainHandler().post(new Runnable() {
            public void run() {
                float f = 12000.0f;
                MapCore.this.clearAnimations();
                if (iPoint != null) {
                    float f2;
                    MapProjection mapProjection = new MapProjection(MapCore.this);
                    mapProjection.recalculate();
                    float f3 = f;
                    float f4 = f2;
                    float abs = Math.abs(f3);
                    float abs2 = Math.abs(f4);
                    if ((abs > abs2 ? abs : abs2) <= 12000.0f) {
                        f = f4;
                        f2 = f3;
                    } else if (abs > abs2) {
                        f2 = f3 > 0.0f ? 12000.0f : (float) (-12000);
                        f = (12000.0f / abs) * f4;
                    } else {
                        f2 = (12000.0f / abs2) * f3;
                        if (f4 <= 0.0f) {
                            f = (float) (-12000);
                        }
                    }
                    ADGLAnimation aaVar = new aa(500, MapCore.this.surface_width / 2, MapCore.this.surface_height / 2);
                    aaVar.a(f2, f);
                    aaVar.a(mapProjection);
                    mapProjection.recycle();
                    MapCore.this.map_anims_mgr.a(aaVar);
                }
            }
        });
    }

    public boolean addMapAnimation(ADGLAnimation aDGLAnimation) {
        if (this.map_anims_mgr == null || aDGLAnimation == null || !aDGLAnimation.isValid()) {
            return false;
        }
        this.map_anims_mgr.a(aDGLAnimation);
        return true;
    }

    public synchronized void clearAnimations() {
        this.map_anims_mgr.a();
    }

    public synchronized int getAnimateionsCount() {
        return this.map_anims_mgr.b();
    }

    public void setStyleData(byte[] bArr, int i, int i2) {
        if (this.native_instance != 0 && bArr != null && bArr.length > 0) {
            nativeSetStyleData(this.native_instance, bArr, i, i2);
        }
    }

    public void setInternaltexture(byte[] bArr, int i) {
        if (this.native_instance != 0 && bArr != null && bArr.length > 0) {
            nativeSetInternaltexture(this.native_instance, bArr, i);
        }
    }

    public long getInstanceHandle() {
        return this.native_instance;
    }

    public synchronized MapProjection getMapstate() {
        if (this.native_instance == 0 || this.native_mapprojection_instance == 0) {
            return null;
        }
        return new MapProjection(this.native_mapprojection_instance, this);
    }

    public void setMapstate(MapProjection mapProjection) {
        if (this.native_instance != 0) {
            if (!(this.mMap == null || this.mMap.getMapConfig() == null)) {
                MapConfig mapConfig = this.mMap.getMapConfig();
                LatLngBounds limitLatLngBounds = mapConfig.getLimitLatLngBounds();
                if (limitLatLngBounds != null) {
                    IPoint[] iPointArr;
                    int i;
                    IPoint[] limitIPoints = mapConfig.getLimitIPoints();
                    if (limitIPoints != null) {
                        iPointArr = limitIPoints;
                    } else {
                        MapProjection.lonlat2Geo(limitLatLngBounds.northeast.longitude, limitLatLngBounds.northeast.latitude, new IPoint());
                        MapProjection.lonlat2Geo(limitLatLngBounds.southwest.longitude, limitLatLngBounds.southwest.latitude, new IPoint());
                        limitIPoints = new IPoint[]{r3, r4};
                        mapConfig.setLimitIPoints(limitIPoints);
                        iPointArr = limitIPoints;
                    }
                    MapProjection mapProjection2 = new MapProjection(this);
                    float a = eh.a(mapProjection2, mapConfig, iPointArr[0], iPointArr[1], this.surface_width, this.surface_height);
                    mapProjection2.recycle();
                    this.mMap.getMapConfig().setMinZoomLevel(a);
                    this.mMap.getMapConfig().setLimitZoomLevel(a);
                    this.mMap.getMapConfig().setLimitIPoints(iPointArr);
                    mapProjection.setMapZoomer(mapProjection.getMapZoomer());
                    mapProjection.recalculate();
                    IPoint iPoint = new IPoint();
                    mapProjection.getGeoCenter(iPoint);
                    int i2 = iPoint.x;
                    int i3 = iPoint.y;
                    mapProjection.recalculate();
                    FPoint[] a2 = eh.a(this.mMap, false, mapProjection);
                    IPoint[] iPointArr2 = new IPoint[a2.length];
                    for (i = 0; i < a2.length; i++) {
                        IPoint iPoint2 = new IPoint();
                        mapProjection.map2Geo(a2[i].x, a2[i].y, iPoint2);
                        iPointArr2[i] = iPoint2;
                    }
                    Integer[] a3 = eh.a(iPointArr, iPointArr2, i2, i3);
                    if (a3 != null && a3.length == 2) {
                        i3 = a3[0].intValue();
                        i = a3[1].intValue();
                    } else {
                        i = i3;
                        i3 = i2;
                    }
                    mapProjection.setGeoCenter(i3, i);
                }
            }
            nativeSetMapstate(this.native_instance, mapProjection.getInstanceHandle());
        }
    }

    public void setParameter(int i, int i2, int i3, int i4, int i5) {
        if (this.native_instance != 0) {
            nativeSetparameter(this.native_instance, i, i2, i3, i4, i5);
        }
    }

    public void setIndoorBuildingToBeActive(String str, int i, String str2) {
        if (!TextUtils.isEmpty(str) && !TextUtils.isEmpty(str2)) {
            nativeSetIndoorBuildingToBeActive(this.native_instance, str, i, str2);
        }
    }

    public boolean putMapData(byte[] bArr, int i, int i2, int i3, int i4) {
        boolean z = false;
        if (this.native_instance != 0) {
            if (bArr.length != i2 || i != 0) {
                Object obj = new byte[i2];
                System.arraycopy(bArr, i, obj, 0, i2);
                if (nativePutMapdata(this.native_instance, i3, obj) > 0) {
                    z = true;
                }
            } else if (nativePutMapdata(this.native_instance, i3, bArr) > 0) {
                z = true;
            }
            if (this.mMapcallback != null) {
                this.mMapcallback.requestRender();
            }
        }
        return z;
    }

    public Poi getPoiItem(int i, int i2, int i3) {
        if (!this.mMap.isMaploaded()) {
            return null;
        }
        try {
            SelectedMapPoi selectedMapPoi = getSelectedMapPoi(i, i2, i3);
            if (selectedMapPoi == null) {
                return null;
            }
            DPoint dPoint = new DPoint();
            MapProjection.geo2LonLat(selectedMapPoi.mapx, selectedMapPoi.mapy, dPoint);
            return new Poi(selectedMapPoi.name, new LatLng(dPoint.y, dPoint.x, false), selectedMapPoi.poiid);
        } catch (Throwable th) {
            return null;
        }
    }

    public SelectedMapPoi getSelectedMapPoi(int i, int i2, int i3) {
        if (this.native_instance != 0) {
            byte[] bArr = new byte[1024];
            int nativeGetSelectedMapPois = nativeGetSelectedMapPois(this.native_instance, i, i2, i3, bArr);
            if (nativeGetSelectedMapPois != 0 && nativeGetSelectedMapPois > 0) {
                byte b;
                SelectedMapPoi selectedMapPoi = new SelectedMapPoi();
                selectedMapPoi.winx = Convert.getInt(bArr, 0);
                selectedMapPoi.winy = Convert.getInt(bArr, 4);
                selectedMapPoi.mapx = Convert.getInt(bArr, 8);
                selectedMapPoi.mapy = Convert.getInt(bArr, 12);
                selectedMapPoi.iconXmin = Convert.getInt(bArr, 16);
                selectedMapPoi.iconXmax = Convert.getInt(bArr, 20);
                selectedMapPoi.iconYmin = Convert.getInt(bArr, 24);
                selectedMapPoi.iconYmax = Convert.getInt(bArr, 28);
                byte b2 = bArr[32];
                StringBuffer stringBuffer = new StringBuffer();
                int i4 = 33;
                for (b = (byte) 0; b < b2; b++) {
                    i4 += 2;
                    stringBuffer.append((char) Convert.getShort(bArr, i4));
                }
                selectedMapPoi.name = stringBuffer.toString();
                nativeGetSelectedMapPois = i4 + 1;
                b2 = bArr[i4];
                stringBuffer = new StringBuffer();
                i4 = nativeGetSelectedMapPois;
                for (b = (byte) 0; b < b2; b++) {
                    char c = (char) Convert.getShort(bArr, i4);
                    i4 += 2;
                    if (c == '\u0000') {
                        break;
                    }
                    stringBuffer.append(c);
                }
                selectedMapPoi.poiid = stringBuffer.toString();
                return selectedMapPoi;
            }
        }
        return null;
    }

    public void putCharbitmap(int i, byte[] bArr) {
        if (this.native_instance != 0) {
            nativePutCharbitmap(this.native_instance, i, bArr);
        }
    }

    public boolean canStopRenderMap() {
        if (this.native_instance != 0) {
            return nativeCanStopRenderMap(this.native_instance);
        }
        return false;
    }

    private void OnMapSurfaceCreate() {
        if (this.mMapcallback != null) {
            this.mMapcallback.OnMapSurfaceCreate(this.mGL, this);
        }
    }

    private void OnMapSufaceChanged(int i, int i2) {
        this.surface_width = i;
        this.surface_height = i2;
        if (this.mMapcallback != null) {
            this.mMapcallback.OnMapSufaceChanged(this.mGL, this, i, i2);
        }
    }

    private void OnMapSurfaceRenderer(int i) {
        if (this.mMapcallback != null) {
            this.mMapcallback.OnMapSurfaceRenderer(this.mGL, this, i);
        }
    }

    private void OnMapProcessEvent() {
        if (this.map_anims_mgr.b() > 0) {
            MapProjection mapstate = getMapstate();
            mapstate.recalculate();
            this.map_anims_mgr.a(mapstate);
            setMapstate(mapstate);
            mapstate.recycle();
            this.isAnimationStep = true;
        } else if (this.isAnimationStep) {
            this.isAnimationStep = false;
            if (this.mGestureMessageList.size() <= 0) {
                this.mMap.onChangeFinish();
            }
        }
        if (this.mMapcallback != null) {
            this.mMapcallback.OnMapProcessEvent(this);
        }
    }

    private void OnMapDestory() {
        if (this.mMapcallback != null) {
            this.mMapcallback.OnMapDestory(this.mGL, this);
        }
    }

    public void OnMapDataRequired(int i, String[] strArr) {
        if (this.mMapcallback != null) {
            try {
                this.mMapcallback.OnMapDataRequired(this, i, strArr);
            } catch (Throwable th) {
            }
        }
    }

    public void OnMapLabelsRequired(int[] iArr, int i) {
        if (this.mMapcallback != null) {
            this.mMapcallback.OnMapLabelsRequired(this, iArr, i);
        }
    }

    private byte[] OnMapCharsWidthsRequired(int[] iArr, int i, int i2) {
        if (this.mMapcallback == null) {
            return null;
        }
        return this.mMapcallback.OnMapCharsWidthsRequired(this, iArr, i, i2);
    }

    private synchronized void OnMapReferencechanged(String str, String str2) {
        if (this.mMapcallback != null) {
            this.mMapcallback.OnMapReferencechanged(this, str, str2);
        }
    }

    public void onIndoorBuildingActivity(byte[] bArr) {
        if (this.mMapcallback != null) {
            try {
                this.mMapcallback.onIndoorBuildingActivity(this, bArr);
            } catch (Throwable th) {
            }
        }
    }

    public void onIndoorDataRequired(int i, String[] strArr, int[] iArr, int[] iArr2) {
        if (this.mMapcallback != null) {
            try {
                this.mMapcallback.onIndoorDataRequired(this, i, strArr, iArr, iArr2);
            } catch (Throwable th) {
            }
        }
    }

    public void destroy() throws Throwable {
        if (this.native_instance != 0) {
            nativeDestroy(this.native_instance, this);
            this.native_instance = 0;
            this.textTextureGenerator = null;
            this.tmp_3072bytes_data = null;
        }
    }

    public void fillCurGridListWithDataType(ArrayList<MapSourceGridData> arrayList, int i) throws UnsupportedEncodingException {
        if (this.native_instance != 0) {
            nativeGetScreenGrids(this.native_instance, this.tmp_3072bytes_data, i);
            byte b = this.tmp_3072bytes_data[0];
            if (b > (byte) 0 && b <= (byte) 100 && arrayList != null) {
                arrayList.clear();
                byte b2 = (byte) 0;
                int i2 = 1;
                while (b2 < b && i2 < 3072) {
                    int i3 = i2 + 1;
                    byte b3 = this.tmp_3072bytes_data[i2];
                    if (b3 > (byte) 0 && b3 <= (byte) 20 && i3 + b3 <= 3072) {
                        String str = new String(this.tmp_3072bytes_data, i3, b3, "utf-8");
                        i3 += b3;
                        if (i != 10) {
                            i3++;
                            arrayList.add(new MapSourceGridData(str, i));
                        } else {
                            short s = Convert.getShort(this.tmp_3072bytes_data, i3);
                            i3 = (i3 + 2) + 1;
                            arrayList.add(new MapSourceGridData(str, i, s, 0));
                        }
                    }
                    b2++;
                    i2 = i3;
                }
            }
        }
    }
}
