package com.autonavi.amap.mapcore;

import javax.microedition.khronos.opengles.GL10;

public interface IMapCallback {
    byte[] OnMapCharsWidthsRequired(MapCore mapCore, int[] iArr, int i, int i2);

    void OnMapDataRequired(MapCore mapCore, int i, String[] strArr);

    void OnMapDestory(GL10 gl10, MapCore mapCore);

    void OnMapLabelsRequired(MapCore mapCore, int[] iArr, int i);

    void OnMapProcessEvent(MapCore mapCore);

    void OnMapReferencechanged(MapCore mapCore, String str, String str2);

    void OnMapSufaceChanged(GL10 gl10, MapCore mapCore, int i, int i2);

    void OnMapSurfaceCreate(GL10 gl10, MapCore mapCore);

    void OnMapSurfaceRenderer(GL10 gl10, MapCore mapCore, int i);

    void onIndoorBuildingActivity(MapCore mapCore, byte[] bArr);

    void onIndoorDataRequired(MapCore mapCore, int i, String[] strArr, int[] iArr, int[] iArr2);

    void requestRender();
}
