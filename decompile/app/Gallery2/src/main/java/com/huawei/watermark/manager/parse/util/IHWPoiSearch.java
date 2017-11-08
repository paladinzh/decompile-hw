package com.huawei.watermark.manager.parse.util;

import android.location.Address;
import android.location.Location;
import java.util.List;

public interface IHWPoiSearch {

    public interface OnPoiSearchCallback {
        void onPoiSearched(List<Address> list);
    }

    void poiSearch(Location location, OnPoiSearchCallback onPoiSearchCallback);
}
