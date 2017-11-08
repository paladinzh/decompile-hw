package com.huawei.systemmanager.rainbow.client.connect.request;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant;
import com.huawei.systemmanager.rainbow.client.util.OperationLocal;
import com.huawei.systemmanager.util.HwLog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DownloadConfigFileRequest extends DownloadConfigRequest {
    private static final String TAG = "DownloadConfigFileRequest";

    public DownloadConfigFileRequest(String url, String serverVer, String serSignature) {
        super(null, url, serverVer, serSignature);
    }

    protected boolean saveToCache(Context context, InputStream inputStream) {
        IOException e;
        Object fos;
        Throwable th;
        Object bis;
        Closeable closeable = null;
        BufferedOutputStream bos = null;
        File file = createFile(ClientConstant.IWARE_FILE_PATH, OperationLocal.getFileID());
        if (file == null) {
            HwLog.w(TAG, "create iware file failed!");
            return false;
        }
        Closeable closeable2 = null;
        try {
            BufferedInputStream bis2;
            BufferedOutputStream bos2;
            FileOutputStream fos2 = new FileOutputStream(file);
            try {
                bis2 = new BufferedInputStream(inputStream);
            } catch (IOException e2) {
                e = e2;
                fos = fos2;
                try {
                    HwLog.e(TAG, "", e);
                    Closeables.close(closeable);
                    Closeables.close(bos);
                    Closeables.close(closeable2);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    Closeables.close(closeable);
                    Closeables.close(bos);
                    Closeables.close(closeable2);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fos = fos2;
                Closeables.close(closeable);
                Closeables.close(bos);
                Closeables.close(closeable2);
                throw th;
            }
            try {
                bos2 = new BufferedOutputStream(fos2);
            } catch (IOException e3) {
                e = e3;
                fos = fos2;
                bis = bis2;
                HwLog.e(TAG, "", e);
                Closeables.close(closeable);
                Closeables.close(bos);
                Closeables.close(closeable2);
                return false;
            } catch (Throwable th4) {
                th = th4;
                fos = fos2;
                bis = bis2;
                Closeables.close(closeable);
                Closeables.close(bos);
                Closeables.close(closeable2);
                throw th;
            }
            try {
                byte[] bytes = new byte[4096];
                while (true) {
                    int readLength = bis2.read(bytes);
                    if (readLength == -1) {
                        break;
                    }
                    bos2.write(bytes, 0, readLength);
                }
                bos2.flush();
                Closeables.close(bis2);
                Closeables.close(bos2);
                Closeables.close(fos2);
                if (TextUtils.isEmpty(this.mSerSignature)) {
                    this.mDownloadSuccess = true;
                    return true;
                }
                String signature = OperationLocal.getSha256ContentsDigest(file, this.mServerVer);
                HwLog.i(TAG, "local***signature==" + signature);
                HwLog.i(TAG, "server***mSerSignature==" + this.mSerSignature);
                if (!this.mSerSignature.equals(signature)) {
                    return false;
                }
                this.mDownloadSuccess = true;
                return true;
            } catch (IOException e4) {
                e = e4;
                closeable2 = fos2;
                bos = bos2;
                closeable = bis2;
                HwLog.e(TAG, "", e);
                Closeables.close(closeable);
                Closeables.close(bos);
                Closeables.close(closeable2);
                return false;
            } catch (Throwable th5) {
                th = th5;
                fos = fos2;
                bos = bos2;
                bis = bis2;
                Closeables.close(closeable);
                Closeables.close(bos);
                Closeables.close(closeable2);
                throw th;
            }
        } catch (IOException e5) {
            e = e5;
            HwLog.e(TAG, "", e);
            Closeables.close(closeable);
            Closeables.close(bos);
            Closeables.close(closeable2);
            return false;
        }
    }

    private File createFile(String dir, String fileName) {
        boolean z = false;
        boolean z2 = true;
        File fileDir = new File(dir);
        if (!fileDir.exists()) {
            z2 = fileDir.mkdir();
            HwLog.i(TAG, "fileDir.mkdir() " + z2);
        }
        File file = new File(dir + "/" + fileName);
        if (file.exists()) {
            boolean deleted = file.delete();
            HwLog.i(TAG, "file.delete() deleted = " + deleted);
            if (!deleted) {
                return null;
            }
        }
        try {
            z = file.createNewFile();
            HwLog.i(TAG, "file.createNewFile() ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!(z2 && r4)) {
            file = null;
        }
        return file;
    }
}
