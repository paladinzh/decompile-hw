package com.autonavi.amap.mapcore;

import android.content.Context;
import android.text.TextUtils;
import com.google.android.gms.location.places.Place;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;

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
    Context mContext;
    GL10 mGL = null;
    private IMapCallback mMapcallback = null;
    long native_instance = 0;
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

    public MapCore(Context context) {
        this.mContext = context;
        MapTilsCacheAndResManager.getInstance(context).checkDir();
    }

    public void newMap() {
        MapTilsCacheAndResManager.getInstance(this.mContext).checkDir();
        String baseMapPath = MapTilsCacheAndResManager.getInstance(this.mContext).getBaseMapPath();
        this.textTextureGenerator = new TextTextureGenerator();
        this.tmp_3072bytes_data = ByteBuffer.allocate(3072).array();
        this.native_instance = nativeNewInstance(baseMapPath, this.textTextureGenerator.getFontVersion());
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
        if (this.native_instance != 0) {
            nativeSurfaceChange(this.native_instance, this, i, i2);
        }
    }

    public void drawFrame(GL10 gl10) {
        if (this.native_instance != 0) {
            nativeSurfaceRenderMap(this.native_instance, this);
        }
    }

    public void setStyleData(byte[] bArr, int i, int i2) {
        if (this.native_instance != 0 && bArr != null && bArr.length > 0) {
            nativeSetStyleData(this.native_instance, bArr, i, i2);
        }
    }

    public void setInternaltexture(byte[] bArr, int i) {
        if (this.native_instance != 0) {
            nativeSetInternaltexture(this.native_instance, bArr, i);
        }
    }

    public long getInstanceHandle() {
        return this.native_instance;
    }

    public MapProjection getMapstate() {
        return this.native_instance != 0 ? new MapProjection(nativeGetMapstate(this.native_instance)) : null;
    }

    public void setMapstate(MapProjection mapProjection) {
        if (this.native_instance != 0) {
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

    public int getLabelBuffer(int i, int i2, int i3, byte[] bArr) {
        return this.native_instance != 0 ? nativeSelectMapPois(this.native_instance, i, i2, i3, bArr) : 0;
    }

    public SelectedMapPoi GetSelectedMapPoi(int i, int i2, int i3) {
        if (this.native_instance != 0) {
            byte[] bArr = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
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

    private void OnMapReferencechanged(String str, String str2) {
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

    public void fillCurGridListWithDataType(ArrayList<MapSourceGridData> arrayList, int i) {
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
                        String str = new String(this.tmp_3072bytes_data, i3, b3);
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
