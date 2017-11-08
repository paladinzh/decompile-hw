package com.huawei.systemmanager.adblock.ui.connect.request;

import android.content.Context;
import com.huawei.systemmanager.adblock.comm.AdUtils;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.rainbow.comm.request.AbsServerStreamRequest;
import com.huawei.systemmanager.rainbow.comm.request.ICommonRequest.RequestType;
import com.huawei.systemmanager.util.HwLog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.json.JSONObject;

public class DlAppmarketRequest extends AbsServerStreamRequest {
    private static final String TAG = "AdBlock_DlAppmarketRequest";
    private boolean mDownloadSuccess = false;

    public DlAppmarketRequest() {
        setNeedDefaultParam(false);
    }

    protected String getRequestUrl(RequestType type) {
        return "http://a.vmall.com/appdl/C27162";
    }

    protected void addExtPostRequestParam(Context ctx, JSONObject param) {
    }

    public boolean isDownloadSuccess() {
        return this.mDownloadSuccess;
    }

    protected int checkResponseCode(Context ctx, int resultCode) {
        return 0;
    }

    protected boolean parseResponseAndPost(Context ctx, InputStream inputStream) {
        HwLog.d(TAG, "parseResponseAndPost inputStream ");
        return saveToCache(ctx, inputStream);
    }

    private boolean saveToCache(Context context, InputStream inputStream) {
        BufferedOutputStream bos;
        FileNotFoundException e;
        Object obj;
        Object bis;
        Throwable th;
        IOException e2;
        RuntimeException e3;
        Object bos2;
        Closeable closeable = null;
        Closeable closeable2 = null;
        File file = AdUtils.getAppmarketCacheFile(context);
        Closeable closeable3 = null;
        boolean ok = true;
        try {
            if (!file.exists()) {
                ok = file.createNewFile();
            }
            if (ok) {
                FileOutputStream fos = new FileOutputStream(file);
                try {
                    BufferedInputStream bis2 = new BufferedInputStream(inputStream);
                    try {
                        bos = new BufferedOutputStream(fos);
                    } catch (FileNotFoundException e4) {
                        e = e4;
                        obj = fos;
                        bis = bis2;
                        try {
                            HwLog.e(TAG, "saveToCache", e);
                            Closeables.close(inputStream);
                            Closeables.close(closeable);
                            Closeables.close(closeable2);
                            Closeables.close(closeable3);
                            return false;
                        } catch (Throwable th2) {
                            th = th2;
                            Closeables.close(inputStream);
                            Closeables.close(closeable);
                            Closeables.close(closeable2);
                            Closeables.close(closeable3);
                            throw th;
                        }
                    } catch (IOException e5) {
                        e2 = e5;
                        obj = fos;
                        bis = bis2;
                        HwLog.e(TAG, "saveToCache", e2);
                        Closeables.close(inputStream);
                        Closeables.close(closeable);
                        Closeables.close(closeable2);
                        Closeables.close(closeable3);
                        return false;
                    } catch (RuntimeException e6) {
                        e3 = e6;
                        obj = fos;
                        bis = bis2;
                        HwLog.e(TAG, "saveToCache", e3);
                        Closeables.close(inputStream);
                        Closeables.close(closeable);
                        Closeables.close(closeable2);
                        Closeables.close(closeable3);
                        return false;
                    } catch (Throwable th3) {
                        th = th3;
                        obj = fos;
                        bis = bis2;
                        Closeables.close(inputStream);
                        Closeables.close(closeable);
                        Closeables.close(closeable2);
                        Closeables.close(closeable3);
                        throw th;
                    }
                    try {
                        byte[] bytes = new byte[4096];
                        while (true) {
                            int readLength = bis2.read(bytes);
                            if (readLength != -1) {
                                bos.write(bytes, 0, readLength);
                            } else {
                                bos.flush();
                                Closeables.close(inputStream);
                                Closeables.close(bis2);
                                Closeables.close(bos);
                                Closeables.close(fos);
                                this.mDownloadSuccess = true;
                                return true;
                            }
                        }
                    } catch (FileNotFoundException e7) {
                        e = e7;
                        closeable3 = fos;
                        closeable2 = bos;
                        closeable = bis2;
                        HwLog.e(TAG, "saveToCache", e);
                        Closeables.close(inputStream);
                        Closeables.close(closeable);
                        Closeables.close(closeable2);
                        Closeables.close(closeable3);
                        return false;
                    } catch (IOException e8) {
                        e2 = e8;
                        obj = fos;
                        bos2 = bos;
                        bis = bis2;
                        HwLog.e(TAG, "saveToCache", e2);
                        Closeables.close(inputStream);
                        Closeables.close(closeable);
                        Closeables.close(closeable2);
                        Closeables.close(closeable3);
                        return false;
                    } catch (RuntimeException e9) {
                        e3 = e9;
                        obj = fos;
                        bos2 = bos;
                        bis = bis2;
                        HwLog.e(TAG, "saveToCache", e3);
                        Closeables.close(inputStream);
                        Closeables.close(closeable);
                        Closeables.close(closeable2);
                        Closeables.close(closeable3);
                        return false;
                    } catch (Throwable th4) {
                        th = th4;
                        obj = fos;
                        bos2 = bos;
                        bis = bis2;
                        Closeables.close(inputStream);
                        Closeables.close(closeable);
                        Closeables.close(closeable2);
                        Closeables.close(closeable3);
                        throw th;
                    }
                } catch (FileNotFoundException e10) {
                    e = e10;
                    obj = fos;
                    HwLog.e(TAG, "saveToCache", e);
                    Closeables.close(inputStream);
                    Closeables.close(closeable);
                    Closeables.close(closeable2);
                    Closeables.close(closeable3);
                    return false;
                } catch (IOException e11) {
                    e2 = e11;
                    obj = fos;
                    HwLog.e(TAG, "saveToCache", e2);
                    Closeables.close(inputStream);
                    Closeables.close(closeable);
                    Closeables.close(closeable2);
                    Closeables.close(closeable3);
                    return false;
                } catch (RuntimeException e12) {
                    e3 = e12;
                    obj = fos;
                    HwLog.e(TAG, "saveToCache", e3);
                    Closeables.close(inputStream);
                    Closeables.close(closeable);
                    Closeables.close(closeable2);
                    Closeables.close(closeable3);
                    return false;
                } catch (Throwable th5) {
                    th = th5;
                    obj = fos;
                    Closeables.close(inputStream);
                    Closeables.close(closeable);
                    Closeables.close(closeable2);
                    Closeables.close(closeable3);
                    throw th;
                }
            }
            HwLog.e(TAG, "createNewFile failed");
            Closeables.close(inputStream);
            Closeables.close(null);
            Closeables.close(null);
            Closeables.close(null);
            return false;
        } catch (FileNotFoundException e13) {
            e = e13;
            HwLog.e(TAG, "saveToCache", e);
            Closeables.close(inputStream);
            Closeables.close(closeable);
            Closeables.close(closeable2);
            Closeables.close(closeable3);
            return false;
        } catch (IOException e14) {
            e2 = e14;
            HwLog.e(TAG, "saveToCache", e2);
            Closeables.close(inputStream);
            Closeables.close(closeable);
            Closeables.close(closeable2);
            Closeables.close(closeable3);
            return false;
        } catch (RuntimeException e15) {
            e3 = e15;
            HwLog.e(TAG, "saveToCache", e3);
            Closeables.close(inputStream);
            Closeables.close(closeable);
            Closeables.close(closeable2);
            Closeables.close(closeable3);
            return false;
        }
    }
}
