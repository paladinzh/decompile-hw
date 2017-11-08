package com.autonavi.amap.mapcore;

import android.content.Context;

public abstract class MapTilsCacheAndResManager {
    public static final String AUTONAVI_DATA_PATH = "data";
    public static final String AUTONAVI_PATH = "amap";
    public static final int ICONS_DATA = 2;
    public static final String MAP_CACHE_PATH_NAME = "cache";
    public static final String MAP_DATA_OFFLINE_PATH_NAME = "vmap";
    public static final String MAP_MAP_ASSETS_NAME = "map_assets";
    public static final String MAP_RES_EXT_PATH_NAME = "vmap4res";
    public static final String MAP_TILES_PATH_NAME = "vmap4tiles";
    public static final int STYLE_DATA = 1;

    public static class RetStyleIconsFile {
        public int clientVersion;
        public String fullName;
        public String name;
        public int serverVersion;
        public int type;
    }

    public abstract boolean canUpate(String str);

    public abstract void checkDir();

    public abstract void clearOnlineMapTilsCache();

    public abstract String getBaseMapPath();

    public abstract byte[] getIconsData(String str, RetStyleIconsFile retStyleIconsFile);

    public abstract String getMapCachePath();

    public abstract String getMapExtResPath();

    public abstract String getMapOfflineDataPath();

    public abstract String getMapOnlineDataPath();

    public abstract byte[] getOtherResData(String str);

    public abstract byte[] getStyleData(String str, RetStyleIconsFile retStyleIconsFile);

    public abstract void saveFile(String str, int i, int i2, byte[] bArr);

    public static MapTilsCacheAndResManager getInstance(Context context) {
        return MapTilsCacheAndResManagerImpl.instance(context);
    }
}
