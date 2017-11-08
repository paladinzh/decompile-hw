package com.autonavi.amap.mapcore;

import android.content.Context;

public interface IBaseMapCallback {
    void OnMapLoaderError(int i);

    Context getContext();

    String getMapSvrAddress();

    boolean isMapEngineValid();
}
