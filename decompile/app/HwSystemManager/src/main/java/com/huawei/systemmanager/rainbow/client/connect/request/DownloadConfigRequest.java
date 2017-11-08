package com.huawei.systemmanager.rainbow.client.connect.request;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.push.PushResponse;
import com.huawei.systemmanager.rainbow.client.util.OperationLocal;
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

public class DownloadConfigRequest extends AbsServerStreamRequest {
    private static final String TAG = "DownloadConfigRequest";
    protected boolean mDownloadSuccess = false;
    private PushResponse mPushResponse = null;
    protected String mSerSignature = null;
    protected String mServerVer = null;
    private String mUrl = null;

    public DownloadConfigRequest(PushResponse pushResponse, String url, String serverVer, String serSignature) {
        this.mPushResponse = pushResponse;
        this.mUrl = url;
        this.mServerVer = serverVer;
        this.mSerSignature = serSignature;
    }

    protected String getRequestUrl(RequestType type) {
        return this.mUrl;
    }

    protected void addExtPostRequestParam(Context ctx, JSONObject param) {
    }

    protected int checkResponseCode(Context ctx, int resultCode) {
        return 0;
    }

    public boolean isDownloadSuccess() {
        return this.mDownloadSuccess;
    }

    protected boolean parseResponseAndPost(Context ctx, InputStream inputStream) {
        HwLog.d(TAG, "parseResponseAndPost inputStream ");
        return saveToCache(ctx, inputStream);
    }

    protected boolean saveToCache(Context context, InputStream inputStream) {
        FileNotFoundException e;
        Object fos;
        Object obj;
        Throwable th;
        IOException e2;
        Object bos;
        String cacheDir = String.valueOf(context.getCacheDir()) + "/";
        String filePath = this.mPushResponse != null ? this.mPushResponse.getFilePath() : "";
        HwLog.i(TAG, "fileName: " + filePath);
        Closeable closeable = null;
        Closeable closeable2 = null;
        File file = new File(cacheDir, filePath);
        Closeable closeable3 = null;
        boolean ok = true;
        try {
            if (!file.getParentFile().exists()) {
                ok = file.getParentFile().mkdirs();
            }
            if (ok) {
                if (!file.exists()) {
                    ok = file.createNewFile();
                }
                if (ok) {
                    BufferedInputStream bis = new BufferedInputStream(inputStream);
                    try {
                        BufferedOutputStream bos2;
                        FileOutputStream fos2 = new FileOutputStream(file);
                        try {
                            bos2 = new BufferedOutputStream(fos2);
                        } catch (FileNotFoundException e3) {
                            e = e3;
                            fos = fos2;
                            obj = bis;
                            try {
                                HwLog.e(TAG, "", e);
                                Closeables.close(closeable);
                                Closeables.close(closeable2);
                                Closeables.close(closeable3);
                                return false;
                            } catch (Throwable th2) {
                                th = th2;
                                Closeables.close(closeable);
                                Closeables.close(closeable2);
                                Closeables.close(closeable3);
                                throw th;
                            }
                        } catch (IOException e4) {
                            e2 = e4;
                            fos = fos2;
                            obj = bis;
                            HwLog.e(TAG, "", e2);
                            Closeables.close(closeable);
                            Closeables.close(closeable2);
                            Closeables.close(closeable3);
                            return false;
                        } catch (Throwable th3) {
                            th = th3;
                            fos = fos2;
                            obj = bis;
                            Closeables.close(closeable);
                            Closeables.close(closeable2);
                            Closeables.close(closeable3);
                            throw th;
                        }
                        try {
                            byte[] bytes = new byte[4096];
                            while (true) {
                                int readLength = bis.read(bytes);
                                if (readLength == -1) {
                                    break;
                                }
                                bos2.write(bytes, 0, readLength);
                            }
                            bos2.flush();
                            Closeables.close(bis);
                            Closeables.close(bos2);
                            Closeables.close(fos2);
                            if (TextUtils.isEmpty(this.mSerSignature)) {
                                this.mDownloadSuccess = true;
                                return true;
                            }
                            String signature = OperationLocal.getSha256ContentsDigest(file, this.mServerVer);
                            HwLog.d(TAG, "local***signature==" + signature);
                            HwLog.d(TAG, "server***mSerSignature==" + this.mSerSignature);
                            if (!this.mSerSignature.equals(signature)) {
                                return false;
                            }
                            this.mDownloadSuccess = true;
                            return true;
                        } catch (FileNotFoundException e5) {
                            e = e5;
                            closeable3 = fos2;
                            closeable2 = bos2;
                            closeable = bis;
                            HwLog.e(TAG, "", e);
                            Closeables.close(closeable);
                            Closeables.close(closeable2);
                            Closeables.close(closeable3);
                            return false;
                        } catch (IOException e6) {
                            e2 = e6;
                            fos = fos2;
                            bos = bos2;
                            obj = bis;
                            HwLog.e(TAG, "", e2);
                            Closeables.close(closeable);
                            Closeables.close(closeable2);
                            Closeables.close(closeable3);
                            return false;
                        } catch (Throwable th4) {
                            th = th4;
                            fos = fos2;
                            bos = bos2;
                            obj = bis;
                            Closeables.close(closeable);
                            Closeables.close(closeable2);
                            Closeables.close(closeable3);
                            throw th;
                        }
                    } catch (FileNotFoundException e7) {
                        e = e7;
                        obj = bis;
                        HwLog.e(TAG, "", e);
                        Closeables.close(closeable);
                        Closeables.close(closeable2);
                        Closeables.close(closeable3);
                        return false;
                    } catch (IOException e8) {
                        e2 = e8;
                        obj = bis;
                        HwLog.e(TAG, "", e2);
                        Closeables.close(closeable);
                        Closeables.close(closeable2);
                        Closeables.close(closeable3);
                        return false;
                    } catch (Throwable th5) {
                        th = th5;
                        obj = bis;
                        Closeables.close(closeable);
                        Closeables.close(closeable2);
                        Closeables.close(closeable3);
                        throw th;
                    }
                }
                HwLog.e(TAG, "createNewFile failed");
                Closeables.close(null);
                Closeables.close(null);
                Closeables.close(null);
                return false;
            }
            HwLog.e(TAG, "mkdir failed");
            Closeables.close(null);
            Closeables.close(null);
            Closeables.close(null);
            return false;
        } catch (FileNotFoundException e9) {
            e = e9;
            HwLog.e(TAG, "", e);
            Closeables.close(closeable);
            Closeables.close(closeable2);
            Closeables.close(closeable3);
            return false;
        } catch (IOException e10) {
            e2 = e10;
            HwLog.e(TAG, "", e2);
            Closeables.close(closeable);
            Closeables.close(closeable2);
            Closeables.close(closeable3);
            return false;
        }
    }
}
