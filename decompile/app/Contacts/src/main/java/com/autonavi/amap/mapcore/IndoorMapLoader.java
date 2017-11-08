package com.autonavi.amap.mapcore;

import android.text.TextUtils;
import com.amap.api.mapcore.s;
import com.amap.api.mapcore.util.bl;
import com.amap.api.mapcore.util.bn;
import com.amap.api.mapcore.util.bx;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;

public class IndoorMapLoader extends BaseMapLoader {
    private static final String IndoorSignKey = "@1071a2a4e3gte2Uc32cY3a98Tf33H1c4Gc23f";
    private String mIndoorChannel = "amap7";

    public IndoorMapLoader(MapCore mapCore, BaseMapCallImplement baseMapCallImplement, int i) {
        this.datasource = i;
        this.mGLMapEngine = mapCore;
        this.mMapCallback = baseMapCallImplement;
        this.createtime = System.currentTimeMillis();
    }

    public static int getInt2(byte[] bArr, int i) {
        return ((((bArr[i + 0] & 255) << 24) + ((bArr[i + 1] & 255) << 16)) + ((bArr[i + 2] & 255) << 8)) + ((bArr[i + 3] & 255) << 0);
    }

    public static short getShort2(byte[] bArr, int i) {
        return (short) (((bArr[i + 0] & 255) << 8) + ((bArr[i + 1] & 255) << 0));
    }

    private String getIndoorMD5Params(String str) {
        return Md5Utility.getStringMD5(this.mIndoorChannel + str + IndoorSignKey).toUpperCase();
    }

    private String getIndoorRequestParams() {
        String str = ";";
        String str2 = null;
        String str3 = null;
        String str4 = null;
        for (int i = 0; i < this.mapTiles.size(); i++) {
            String gridName = ((MapSourceGridData) this.mapTiles.get(i)).getGridName();
            int i2 = ((MapSourceGridData) this.mapTiles.get(i)).mIndoorIndex;
            int i3 = ((MapSourceGridData) this.mapTiles.get(i)).mIndoorVersion;
            if (!(gridName == null || gridName.length() == 0 || containllegal(gridName) || !isAssic(gridName))) {
                if (str4 != null) {
                    str4 = str4 + gridName + str;
                } else {
                    str4 = gridName + str;
                }
                if (str3 != null) {
                    str3 = str3 + i2 + str;
                } else {
                    str3 = i2 + str;
                }
                if (str2 != null) {
                    str2 = str2 + i3 + str;
                } else {
                    str2 = i3 + str;
                }
            }
        }
        if (!TextUtils.isEmpty(str4)) {
            if (str4.endsWith(str) || str4.endsWith(HwCustPreloadContacts.EMPTY_STRING)) {
                str4 = str4.substring(0, str4.length() - 1);
            }
        }
        if (!TextUtils.isEmpty(str3)) {
            if (str3.endsWith(str) || str3.endsWith(HwCustPreloadContacts.EMPTY_STRING)) {
                str3 = str3.substring(0, str3.length() - 1);
            }
        }
        if (!TextUtils.isEmpty(str2)) {
            if (str2.endsWith(str) || str2.endsWith(HwCustPreloadContacts.EMPTY_STRING)) {
                str2 = str2.substring(0, str2.length() - 1);
            }
        }
        StringBuffer stringBuffer = new StringBuffer();
        try {
            stringBuffer.append("poiid=").append(str4).append("&").append("floor=").append(str3).append("&").append("version=").append(str2).append("&").append("diver=").append(s.e).append("&").append("servicetype=unify").append("&").append("zoomlevel=").append((int) this.mGLMapEngine.getMapstate().getMapZoomer()).append("&").append("key=").append(bl.f(this.mMapCallback.getContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return assScodeToParma(stringBuffer);
    }

    private String assScodeToParma(StringBuffer stringBuffer) {
        String d = bx.d(stringBuffer.toString());
        String a = bn.a();
        stringBuffer.append("&ts=" + a + "&");
        stringBuffer.append("scode=" + bn.a(this.mMapCallback.getContext(), a, d));
        return stringBuffer.toString();
    }

    protected String getGridParma() {
        return getIndoorRequestParams();
    }

    protected String getMapAddress() {
        return "http://restapi.amap.com/v3/indoor/indoormaps";
    }

    protected String getMapSvrPath() {
        switch (this.datasource) {
            case 10:
                return "?";
            default:
                return null;
        }
    }

    public boolean isRequestValid() {
        return this.mMapCallback.isIndoorGridsInScreen(this.mapTiles, this.datasource);
    }

    private void processReceivedIndoorData() {
        if (this.nextImgDataLength != 0) {
            if (this.recievedDataSize >= this.nextImgDataLength) {
                processReceivedTileDataV4(this.recievedDataBuffer, 0, this.nextImgDataLength);
                Convert.moveArray(this.recievedDataBuffer, this.nextImgDataLength, this.recievedDataBuffer, 0, this.recievedDataSize - this.nextImgDataLength);
                this.recievedDataSize -= this.nextImgDataLength;
                this.nextImgDataLength = 0;
                processReceivedIndoorData();
            }
        } else if (this.recievedDataSize >= 6) {
            this.nextImgDataLength = getInt2(this.recievedDataBuffer, 0);
            processReceivedIndoorData();
        }
    }

    protected void processReceivedTileDataV4(byte[] bArr, int i, int i2) {
        int i3 = i + 4;
        int i4 = i3 + 1;
        byte b = bArr[i3];
        if (b <= (byte) 10) {
            String str;
            String str2 = "";
            if (b > (byte) 0 && (i4 + b) - 1 < i2) {
                str = new String(bArr, i4, b);
            } else {
                str = str2;
            }
            int i5 = i4 + b;
            if (this.mGLMapEngine.isMapEngineValid() && i2 > i3) {
                int i6;
                short short2 = getShort2(bArr, i5);
                if (this.mMapCallback.isIndoorGridInScreen(this.datasource, str, short2)) {
                    i6 = 0;
                } else {
                    i6 = 1;
                }
                if (this.mGLMapEngine.putMapData(bArr, i3, i2 - i3, this.datasource, 0)) {
                    VMapDataCache.getInstance().putRecoder(null, str + "-" + short2, this.datasource);
                }
                if (i6 != 0) {
                    doCancel();
                }
            }
        }
    }

    protected void processRecivedVersionOrScenicWidgetData() {
        if (this.datasource == 9) {
            processRecivedVersionData(this.recievedDataBuffer, 0, this.recievedDataSize);
        }
    }

    protected boolean processReceivedDataHeader(int i) {
        if (this.recievedDataSize <= 5) {
            return false;
        }
        Convert.moveArray(this.recievedDataBuffer, 6, this.recievedDataBuffer, 0, i - 6);
        this.recievedDataSize -= 6;
        this.nextImgDataLength = 0;
        this.recievedHeader = true;
        processReceivedIndoorData();
        return true;
    }

    protected boolean isNeedProcessReturn() {
        if (this.datasource != 9) {
            return false;
        }
        return true;
    }

    protected void processRecivedDataByType() {
        processReceivedIndoorData();
    }
}
