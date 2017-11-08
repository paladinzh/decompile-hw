package com.autonavi.amap.mapcore;

import android.text.TextUtils;
import com.amap.api.mapcore.util.ff;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public abstract class BaseMapLoader {
    long createtime;
    int datasource = 0;
    public HttpURLConnection httpURLConnection = null;
    volatile boolean inRequest = false;
    volatile boolean isFinished = false;
    volatile boolean mCanceled = false;
    int mCapaticy = 30720;
    int mCapaticyExt = 10240;
    MapCore mGLMapEngine;
    BaseMapCallImplement mMapCallback;
    long m_reqestStartLen;
    int mapLevel;
    public ArrayList<MapSourceGridData> mapTiles = new ArrayList();
    int nextImgDataLength = 0;
    byte[] recievedDataBuffer;
    int recievedDataSize = 0;
    boolean recievedHeader = false;

    protected abstract String getGridParma();

    protected abstract String getMapAddress();

    protected abstract String getMapSvrPath();

    protected abstract boolean isNeedProcessReturn();

    public abstract boolean isRequestValid();

    protected abstract boolean processReceivedDataHeader(int i) throws UnsupportedEncodingException;

    protected abstract void processRecivedDataByType() throws UnsupportedEncodingException;

    protected abstract void processRecivedVersionOrScenicWidgetData() throws UnsupportedEncodingException;

    protected void processReceivedTileDataV4(byte[] bArr, int i, int i2) throws UnsupportedEncodingException {
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

    protected String getURL(String str, String str2, String str3) {
        String str4 = "";
        str4 = "";
        return str + str2 + str3;
    }

    protected void initTestTime() {
        this.m_reqestStartLen = System.currentTimeMillis();
    }

    protected void privteTestTime(String str, String str2) {
    }

    protected boolean isAssic(String str) {
        if (str == null) {
            return false;
        }
        char[] toCharArray = str.toCharArray();
        int i = 0;
        while (i < toCharArray.length) {
            if (toCharArray[i] >= 'Ā' || toCharArray[i] <= '\u0000') {
                return false;
            }
            i++;
        }
        return true;
    }

    protected boolean containllegal(String str) {
        if (str.contains("<") || str.contains("[")) {
            return true;
        }
        return false;
    }

    public void OnException(int i) {
        privteTestTime("", " network error:" + i);
        this.isFinished = true;
        if (!(this.datasource == 6 || this.datasource == 4 || this.datasource == 1 || this.mCanceled)) {
        }
        this.isFinished = true;
    }

    public synchronized boolean hasFinished() {
        boolean z = false;
        synchronized (this) {
            if (!this.mCanceled) {
                if (!this.isFinished) {
                }
            }
            z = true;
        }
        return z;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void doCancel() throws UnsupportedEncodingException {
        if (!(this.mCanceled || this.isFinished)) {
            this.mCanceled = true;
            try {
                if (this.httpURLConnection != null) {
                    if (this.inRequest) {
                        this.httpURLConnection.disconnect();
                    }
                }
            } catch (Throwable th) {
            } finally {
                onConnectionOver();
                this.mMapCallback = null;
                this.mGLMapEngine = null;
            }
        }
    }

    private synchronized void onConnectionOver() throws UnsupportedEncodingException {
        processRecivedVersionOrScenicWidgetData();
        this.recievedDataBuffer = null;
        this.nextImgDataLength = 0;
        this.recievedDataSize = 0;
        int i = 0;
        while (i < this.mapTiles.size()) {
            try {
                this.mMapCallback.tileProcessCtrl.removeTile(((MapSourceGridData) this.mapTiles.get(i)).keyGridName);
                i++;
            } catch (Throwable th) {
            }
        }
        this.isFinished = true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void doRequest() throws UnsupportedEncodingException, InterruptedException {
        InputStream inputStream;
        Throwable th;
        InputStream inputStream2 = null;
        if (!this.mCanceled && !this.isFinished) {
            if (isRequestValid()) {
                String mapAddress = getMapAddress();
                String mapSvrPath = getMapSvrPath();
                if (mapSvrPath != null && mapSvrPath.length() != 0 && mapAddress != null) {
                    Object gridParma = getGridParma();
                    if (!TextUtils.isEmpty(gridParma)) {
                        this.inRequest = true;
                        try {
                            Proxy a = ff.a(this.mMapCallback.getContext());
                            mapAddress = getURL(mapAddress, mapSvrPath, gridParma);
                            if (a == null) {
                                this.httpURLConnection = (HttpURLConnection) new URL(mapAddress).openConnection();
                            } else {
                                this.httpURLConnection = (HttpURLConnection) new URL(mapAddress).openConnection(a);
                            }
                            if (this.httpURLConnection == null) {
                                OnException(1002);
                            } else {
                                this.httpURLConnection.setConnectTimeout(20000);
                                this.httpURLConnection.setRequestMethod("GET");
                                this.httpURLConnection.connect();
                                if (this.httpURLConnection.getResponseCode() != SmsCheckResult.ESCT_200) {
                                    OnException(1002);
                                    inputStream = null;
                                } else {
                                    inputStream = this.httpURLConnection.getInputStream();
                                    try {
                                        onConnectionOpened();
                                        byte[] bArr = new byte[512];
                                        boolean z = true;
                                        while (true) {
                                            int read = inputStream.read(bArr);
                                            if (read <= -1) {
                                                break;
                                            }
                                            if (z) {
                                                privteTestTime("recievedFirstByte:", "");
                                                z = false;
                                            }
                                            Thread.currentThread();
                                            if (!Thread.interrupted()) {
                                                if (this.mCanceled) {
                                                    break;
                                                }
                                                onConnectionRecieveData(bArr, read);
                                            } else {
                                                throw new InterruptedException();
                                            }
                                        }
                                    } catch (IllegalArgumentException e) {
                                        inputStream2 = inputStream;
                                        onConnectionOver();
                                        if (!(inputStream2 == null || this.mCanceled)) {
                                            try {
                                                inputStream2.close();
                                            } catch (IOException e2) {
                                                OnException(1002);
                                            }
                                        }
                                        return;
                                    } catch (SecurityException e3) {
                                        inputStream2 = inputStream;
                                        onConnectionOver();
                                        if (!(inputStream2 == null || this.mCanceled)) {
                                            try {
                                                inputStream2.close();
                                            } catch (IOException e4) {
                                                OnException(1002);
                                            }
                                        }
                                        return;
                                    } catch (OutOfMemoryError e5) {
                                        inputStream2 = inputStream;
                                        onConnectionOver();
                                        if (!(inputStream2 == null || this.mCanceled)) {
                                            try {
                                                inputStream2.close();
                                            } catch (IOException e6) {
                                                OnException(1002);
                                            }
                                        }
                                        return;
                                    } catch (IllegalStateException e7) {
                                        inputStream2 = inputStream;
                                        onConnectionOver();
                                        if (!(inputStream2 == null || this.mCanceled)) {
                                            try {
                                                inputStream2.close();
                                            } catch (IOException e8) {
                                                OnException(1002);
                                            }
                                        }
                                        return;
                                    } catch (IOException e9) {
                                        inputStream2 = inputStream;
                                        try {
                                            OnException(1002);
                                            onConnectionOver();
                                            if (!(inputStream2 == null || this.mCanceled)) {
                                                try {
                                                    inputStream2.close();
                                                } catch (IOException e10) {
                                                    OnException(1002);
                                                }
                                            }
                                            return;
                                        } catch (Throwable th2) {
                                            th = th2;
                                            onConnectionOver();
                                            if (!(inputStream2 == null || this.mCanceled)) {
                                                try {
                                                    inputStream2.close();
                                                } catch (IOException e11) {
                                                    OnException(1002);
                                                }
                                            }
                                            throw th;
                                        }
                                    } catch (NullPointerException e12) {
                                        inputStream2 = inputStream;
                                        onConnectionOver();
                                        if (!(inputStream2 == null || this.mCanceled)) {
                                            try {
                                                inputStream2.close();
                                            } catch (IOException e13) {
                                                OnException(1002);
                                            }
                                        }
                                        return;
                                    } catch (Throwable th3) {
                                        Throwable th4 = th3;
                                        inputStream2 = inputStream;
                                        th = th4;
                                        onConnectionOver();
                                        inputStream2.close();
                                        throw th;
                                    }
                                }
                                inputStream2 = inputStream;
                            }
                            onConnectionOver();
                            if (!(inputStream2 == null || this.mCanceled)) {
                                try {
                                    inputStream2.close();
                                } catch (IOException e14) {
                                    OnException(1002);
                                }
                            }
                        } catch (IllegalArgumentException e15) {
                            onConnectionOver();
                            inputStream2.close();
                            return;
                        } catch (SecurityException e16) {
                            onConnectionOver();
                            inputStream2.close();
                            return;
                        } catch (OutOfMemoryError e17) {
                            onConnectionOver();
                            inputStream2.close();
                            return;
                        } catch (IllegalStateException e18) {
                            onConnectionOver();
                            inputStream2.close();
                            return;
                        } catch (IOException e19) {
                            OnException(1002);
                            onConnectionOver();
                            inputStream2.close();
                            return;
                        } catch (NullPointerException e20) {
                            onConnectionOver();
                            inputStream2.close();
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            doCancel();
        }
    }

    public void onConnectionError(BaseMapLoader baseMapLoader, int i, String str) {
    }

    protected void onConnectionOpened() {
        this.recievedDataBuffer = new byte[this.mCapaticy];
        this.nextImgDataLength = 0;
        this.recievedDataSize = 0;
        this.recievedHeader = false;
    }

    public void addReuqestTiles(MapSourceGridData mapSourceGridData) {
        this.mapTiles.add(mapSourceGridData);
    }

    private void onConnectionRecieveData(byte[] bArr, int i) throws UnsupportedEncodingException {
        if (this.mCapaticy < this.recievedDataSize + i) {
            try {
                this.mCapaticy += this.mCapaticyExt;
                Object obj = new byte[this.mCapaticy];
                System.arraycopy(this.recievedDataBuffer, 0, obj, 0, this.recievedDataSize);
                this.recievedDataBuffer = obj;
            } catch (OutOfMemoryError e) {
                doCancel();
                return;
            }
        }
        try {
            System.arraycopy(bArr, 0, this.recievedDataBuffer, this.recievedDataSize, i);
            this.recievedDataSize += i;
            if (!isNeedProcessReturn()) {
                if (this.recievedHeader || processReceivedDataHeader(i)) {
                    processRecivedDataByType();
                }
            }
        } catch (ArrayIndexOutOfBoundsException e2) {
            doCancel();
        } catch (Exception e3) {
            doCancel();
        }
    }

    protected void processRecivedData() throws UnsupportedEncodingException {
        if (this.nextImgDataLength != 0) {
            if (this.recievedDataSize >= this.nextImgDataLength) {
                int i = Convert.getInt(this.recievedDataBuffer, 0);
                int i2 = Convert.getInt(this.recievedDataBuffer, 4);
                if (i2 != 0) {
                    try {
                        GZIPInputStream gZIPInputStream = new GZIPInputStream(new ByteArrayInputStream(this.recievedDataBuffer, 8, i));
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] bArr = new byte[128];
                        while (true) {
                            int read = gZIPInputStream.read(bArr);
                            if (read <= -1) {
                                break;
                            }
                            byteArrayOutputStream.write(bArr, 0, read);
                        }
                        processRecivedTileData(byteArrayOutputStream.toByteArray(), 0, i2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    processRecivedTileData(this.recievedDataBuffer, 8, i + 8);
                }
                if (this.nextImgDataLength > 0) {
                    Convert.moveArray(this.recievedDataBuffer, this.nextImgDataLength, this.recievedDataBuffer, 0, this.recievedDataSize - this.nextImgDataLength);
                }
                this.recievedDataSize -= this.nextImgDataLength;
                this.nextImgDataLength = 0;
                processRecivedData();
            }
        } else if (this.recievedDataSize >= 8) {
            this.nextImgDataLength = Convert.getInt(this.recievedDataBuffer, 0) + 8;
            processRecivedData();
        }
    }

    protected void processReceivedDataV4() throws UnsupportedEncodingException {
        if (this.nextImgDataLength != 0) {
            if (this.recievedDataSize >= this.nextImgDataLength) {
                processReceivedTileDataV4(this.recievedDataBuffer, 8, this.nextImgDataLength);
                Convert.moveArray(this.recievedDataBuffer, this.nextImgDataLength, this.recievedDataBuffer, 0, this.recievedDataSize - this.nextImgDataLength);
                this.recievedDataSize -= this.nextImgDataLength;
                this.nextImgDataLength = 0;
                processReceivedDataV4();
            }
        } else if (this.recievedDataSize >= 8) {
            this.nextImgDataLength = Convert.getInt(this.recievedDataBuffer, 0) + 8;
            processReceivedDataV4();
        }
    }

    void processRecivedTileData(byte[] bArr, int i, int i2) throws UnsupportedEncodingException {
        int i3 = ((i + 2) + 2) + 4;
        int i4 = i3 + 1;
        byte b = bArr[i3];
        String str = "";
        if (b > (byte) 0 && (i4 + b) - 1 < i2) {
            str = new String(bArr, i4, b, "utf-8");
        }
        if (this.mGLMapEngine.isMapEngineValid() && i2 > i) {
            int i5;
            if (this.mMapCallback.isGridInScreen(this.datasource, str)) {
                i5 = 0;
            } else {
                i5 = 1;
            }
            VMapDataCache.getInstance().putRecoder(null, str, this.datasource);
            this.mGLMapEngine.putMapData(bArr, i, i2 - i, this.datasource, 0);
            if (i5 != 0) {
                doCancel();
            }
        }
    }

    void processRecivedVersionData(byte[] bArr, int i, int i2) throws UnsupportedEncodingException {
        if (i2 > 0 && i2 <= bArr.length && Convert.getInt(bArr, 0) == 0 && Convert.getInt(bArr, 4) == 0) {
            int i3 = Convert.getInt(bArr, 8);
            int i4 = 1;
            ArrayList arrayList = new ArrayList();
            int i5 = 12;
            int i6 = 0;
            while (i6 < i3) {
                String str = "";
                if (i5 < i2) {
                    int i7 = i5 + 1;
                    byte b = bArr[i5];
                    if (b <= (byte) 0 || i7 + b >= i2) {
                        i4 = 0;
                        break;
                    }
                    arrayList.add(new String(bArr, i7, b, "utf-8"));
                    i5 = (b + i7) + 4;
                    i6++;
                } else {
                    i4 = 0;
                    break;
                }
            }
            if (i4 != 0) {
                for (i6 = 0; i6 < arrayList.size(); i6++) {
                    VMapDataCache.getInstance().putRecoder(null, (String) arrayList.get(i6), this.datasource);
                }
                this.mGLMapEngine.putMapData(bArr, 0, i2, this.datasource, 0);
            }
        }
    }
}
