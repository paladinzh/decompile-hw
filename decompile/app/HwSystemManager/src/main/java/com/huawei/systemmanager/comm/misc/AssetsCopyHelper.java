package com.huawei.systemmanager.comm.misc;

import android.content.Context;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AssetsCopyHelper {
    public static final int BUFFER_SIZE = 1024;
    public static final String TAG = "AssetsCopyHelper";

    public static boolean copyAssetData(Context context, String assetsFileName, String targetPath) {
        IOException e;
        Throwable th;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        boolean isRet = true;
        try {
            inputStream = context.getAssets().open(assetsFileName);
            FileOutputStream output = new FileOutputStream(targetPath + File.separator + assetsFileName);
            try {
                byte[] buf = new byte[1024];
                while (true) {
                    int count = inputStream.read(buf);
                    if (count <= 0) {
                        break;
                    }
                    output.write(buf, 0, count);
                }
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e2) {
                        HwLog.e(TAG, "copyAssetData close output Exception" + e2.toString());
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e22) {
                        HwLog.e(TAG, "copyAssetData close inputStream Exception" + e22.toString());
                    }
                }
            } catch (IOException e3) {
                e22 = e3;
                fileOutputStream = output;
                try {
                    HwLog.e(TAG, "copyAssetData IOException", e22);
                    isRet = false;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e222) {
                            HwLog.e(TAG, "copyAssetData close output Exception" + e222.toString());
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e2222) {
                            HwLog.e(TAG, "copyAssetData close inputStream Exception" + e2222.toString());
                        }
                    }
                    return isRet;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e22222) {
                            HwLog.e(TAG, "copyAssetData close output Exception" + e22222.toString());
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e222222) {
                            HwLog.e(TAG, "copyAssetData close inputStream Exception" + e222222.toString());
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = output;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                throw th;
            }
        } catch (IOException e4) {
            e222222 = e4;
            HwLog.e(TAG, "copyAssetData IOException", e222222);
            isRet = false;
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            return isRet;
        }
        return isRet;
    }
}
