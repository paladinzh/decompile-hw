package com.amap.api.maps;

import com.amap.api.maps.CoordinateConverter.CoordType;

/* compiled from: CoordinateConverter */
/* synthetic */ class a {
    static final /* synthetic */ int[] a = new int[CoordType.values().length];

    static {
        try {
            a[CoordType.BAIDU.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            a[CoordType.MAPBAR.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            a[CoordType.MAPABC.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            a[CoordType.SOSOMAP.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            a[CoordType.ALIYUN.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            a[CoordType.GOOGLE.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            a[CoordType.GPS.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
    }
}
