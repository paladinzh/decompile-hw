package com.autonavi.amap.mapcore;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class NormalMapLoader extends BaseMapLoader {
    public NormalMapLoader(MapCore mapCore, BaseMapCallImplement baseMapCallImplement, int i) {
        this.datasource = i;
        this.mGLMapEngine = mapCore;
        this.mMapCallback = baseMapCallImplement;
        this.createtime = System.currentTimeMillis();
    }

    public String getGridParmaV4() {
        String str;
        String str2 = ";";
        String str3 = null;
        for (int i = 0; i < this.mapTiles.size(); i++) {
            String gridName = ((MapSourceGridData) this.mapTiles.get(i)).getGridName();
            if (!(gridName == null || gridName.length() == 0 || containllegal(gridName) || !isAssic(gridName))) {
                if (this.datasource != 4) {
                    str = gridName;
                } else if (((MapSourceGridData) this.mapTiles.get(i)).obj == null) {
                    str = gridName;
                } else {
                    try {
                        str = gridName + "-" + URLEncoder.encode((String) ((MapSourceGridData) this.mapTiles.get(i)).obj, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                    }
                }
                if (str3 != null) {
                    str3 = str3 + str + str2;
                } else {
                    str3 = str + str2;
                }
            }
        }
        if (str3 == null) {
            return null;
        }
        if (str3.length() > 0) {
            str = str3;
            while (str != null) {
                if (!str.endsWith(str2) && !str.endsWith(" ")) {
                    break;
                }
                str = str3.substring(0, str.length() - 1);
            }
            str3 = str;
        }
        if (str3.length() <= 0) {
            return null;
        }
        if (this.datasource == 0) {
            return "mapdataver=5&type=20&mesh=" + str3;
        }
        if (this.datasource == 1) {
            return "mapdataver=5&type=11&mesh=" + str3;
        }
        if (this.datasource == 7) {
            return "mapdataver=5&type=1&mesh=" + str3;
        }
        if (this.datasource == 8) {
            return "mapdataver=5&type=13&mesh=" + str3;
        }
        if (this.datasource == 9) {
            return "mapdataver=5&type=40&mesh=" + str3;
        }
        if (this.datasource == 2) {
            return "t=BMPBM&mapdataver=5&mesh=" + str3;
        }
        if (this.datasource == 3) {
            return "mapdataver=5&mesh=" + str3;
        }
        if (this.datasource == 4) {
            return "mapdataver=5&v=6.0.0&bver=2&mesh=" + str3;
        }
        if (this.datasource != 6) {
            return null;
        }
        return "t=VMMV3&mapdataver=5&type=mod&cp=0&mid=" + str3;
    }

    protected String getGridParma() {
        return getGridParmaV4();
    }

    protected String getMapSvrPath() {
        switch (this.datasource) {
            case 0:
            case 1:
            case 7:
            case 8:
            case 9:
                return "/ws/mps/vmap?";
            case 2:
            case 6:
                return "/amapsrv/MPS?";
            case 3:
                return "/ws/mps/smap?";
            case 4:
                return "/ws/mps/rtt?";
            default:
                return null;
        }
    }

    protected String getMapAddress() {
        return this.mMapCallback.getMapSvrAddress();
    }

    public boolean isRequestValid() {
        return this.mMapCallback.isGridsInScreen(this.mapTiles, this.datasource);
    }

    protected void processRecivedVersionOrScenicWidgetData() throws UnsupportedEncodingException {
        if (this.datasource == 9) {
            processRecivedVersionData(this.recievedDataBuffer, 0, this.recievedDataSize);
        }
    }

    protected void processRecivedDataByType() throws UnsupportedEncodingException {
        if (this.datasource == 0 || this.datasource == 1 || this.datasource == 8 || this.datasource == 7) {
            processReceivedDataV4();
        } else {
            super.processRecivedData();
        }
    }

    protected boolean processReceivedDataHeader(int i) throws UnsupportedEncodingException {
        if (this.recievedDataSize <= 7) {
            return false;
        }
        if (Convert.getInt(this.recievedDataBuffer, 0) == 0) {
            Convert.moveArray(this.recievedDataBuffer, 8, this.recievedDataBuffer, 0, i - 8);
            this.recievedDataSize -= 8;
            this.nextImgDataLength = 0;
            this.recievedHeader = true;
            if (this.datasource == 0 || this.datasource == 1 || this.datasource == 8 || this.datasource == 7) {
                processReceivedDataV4();
            } else {
                super.processRecivedData();
            }
            return true;
        }
        doCancel();
        return false;
    }

    protected boolean isNeedProcessReturn() {
        if (this.datasource != 9) {
            return false;
        }
        return true;
    }

    void processRecivedTileData(byte[] bArr, int i, int i2) throws UnsupportedEncodingException {
        if (i == 0) {
            super.processRecivedTileData(bArr, i, i2);
        } else if (this.datasource == 2 || this.datasource == 3) {
            processRecivedTileDataBmp(bArr, i, i2);
        } else if (this.datasource == 4) {
            processRecivedTileDataVTmc(bArr, i, i2);
        } else if (this.datasource != 6) {
            super.processRecivedTileData(bArr, i, i2);
        } else {
            processRecivedModels(bArr, i, i2);
        }
    }

    void processRecivedTileDataBmp(byte[] bArr, int i, int i2) throws UnsupportedEncodingException {
        String str;
        int i3 = i + 4;
        int i4 = i3 + 1;
        byte b = bArr[i3];
        String str2 = "";
        if (b > (byte) 0 && (i4 + b) - 1 < i2) {
            str = new String(bArr, i4, b, "utf-8");
        } else {
            str = str2;
        }
        if (this.mGLMapEngine.isMapEngineValid() && i2 > i) {
            int i5;
            if (this.mMapCallback.isGridInScreen(this.datasource, str)) {
                i5 = 0;
            } else {
                i5 = 1;
            }
            if (this.mGLMapEngine.putMapData(bArr, i, i2 - i, this.datasource, 0)) {
                VMapDataCache.getInstance().putRecoder(null, str, this.datasource);
            }
            if (i5 != 0) {
                doCancel();
            }
        }
    }

    void processRecivedTileDataVTmc(byte[] bArr, int i, int i2) throws UnsupportedEncodingException {
        int i3 = i + 4;
        int i4 = i3 + 1;
        byte b = bArr[i3];
        if (i4 + b <= bArr.length && i4 <= bArr.length - 1 && b >= (byte) 0) {
            String str = new String(bArr, i4, b, "utf-8");
            i3 = b + i4;
            i3 = bArr[i3] + (i3 + 1);
            if (this.mGLMapEngine.isMapEngineValid()) {
                VTMCDataCache instance = VTMCDataCache.getInstance();
                if (i2 > i) {
                    int i5;
                    Object obj = new byte[(i2 - i)];
                    System.arraycopy(bArr, i, obj, 0, i2 - i);
                    VTmcData putData = instance.putData(obj);
                    if (this.mMapCallback.isGridInScreen(this.datasource, str)) {
                        i5 = 0;
                    } else {
                        i5 = 1;
                    }
                    if (putData != null) {
                        this.mGLMapEngine.putMapData(putData.data, 0, putData.data.length, this.datasource, putData.createTime);
                    }
                    if (i5 != 0) {
                        doCancel();
                    }
                }
            }
        }
    }

    void processRecivedModels(byte[] bArr, int i, int i2) throws UnsupportedEncodingException {
        int i3 = i + 1;
        byte b = bArr[i];
        if (b >= (byte) 0) {
            String str = new String(bArr, i3, b, "utf-8");
            if (this.mGLMapEngine.isMapEngineValid() && i2 > i) {
                int i4;
                if (this.mMapCallback.isGridInScreen(this.datasource, str)) {
                    i4 = 0;
                } else {
                    i4 = 1;
                }
                this.mGLMapEngine.putMapData(bArr, i, i2 - i, this.datasource, 0);
                if (i4 != 0) {
                    doCancel();
                }
            }
        }
    }
}
