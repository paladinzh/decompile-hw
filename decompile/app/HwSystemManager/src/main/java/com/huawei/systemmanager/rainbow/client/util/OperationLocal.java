package com.huawei.systemmanager.rainbow.client.util;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant;
import com.huawei.systemmanager.rainbow.client.base.CloudSpfKeys;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.util.HwLog;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class OperationLocal {
    private static final char[] HEX_CHAR = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String TAG = "OperationLocal";

    public static boolean openSystemManageClouds(Context context) {
        try {
            return new LocalSharedPrefrenceHelper(context).putString(CloudSpfKeys.SYSTEM_MANAGER_CLOUD, ClientConstant.SYSTEM_CLOUD_OPEN);
        } catch (Exception e) {
            HwLog.e(TAG, "openSystemManageClouds catch Exception: " + e.getMessage());
            return false;
        }
    }

    public static boolean closeSystemManageClouds(Context context) {
        try {
            return new LocalSharedPrefrenceHelper(context).putString(CloudSpfKeys.SYSTEM_MANAGER_CLOUD, ClientConstant.SYSTEM_CLOUD_CLOSE);
        } catch (Exception e) {
            HwLog.e(TAG, "closeSystemManageClouds catch Exception: " + e.getMessage());
            return false;
        }
    }

    public static String getSha256ContentsDigest(File file, String ver) {
        IOException e;
        Throwable th;
        if (TextUtils.isEmpty(ver)) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            InputStream inputStream = null;
            try {
                InputStream in = new BufferedInputStream(new FileInputStream(file), buf.length);
                while (true) {
                    try {
                        int chunkSize = in.read(buf);
                        if (chunkSize == -1) {
                            break;
                        }
                        digest.update(buf, 0, chunkSize);
                    } catch (IOException e2) {
                        e = e2;
                        inputStream = in;
                    } catch (Throwable th2) {
                        th = th2;
                        inputStream = in;
                    }
                }
                digest.update(converLongToBytes(ver));
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e3) {
                        HwLog.e(TAG, "stream closed failed {}", e3);
                    }
                }
                return toHexString(digest.digest());
            } catch (IOException e4) {
                e3 = e4;
                try {
                    HwLog.e(TAG, "getSha256ContentsDigest() error{}", e3);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e32) {
                            HwLog.e(TAG, "stream closed failed {}", e32);
                        }
                    }
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e322) {
                            HwLog.e(TAG, "stream closed failed {}", e322);
                        }
                    }
                    throw th;
                }
            }
        } catch (NoSuchAlgorithmException e5) {
            HwLog.e(TAG, "SHA-256 not available {}", e5);
            return null;
        }
    }

    private static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_CHAR[(b[i] & 240) >>> 4]);
            sb.append(HEX_CHAR[b[i] & 15]);
        }
        return sb.toString();
    }

    private static byte[] converLongToBytes(String l) {
        byte[] b = new byte[8];
        try {
            b = l.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            HwLog.e(TAG, "converLongToBytes {}", e);
        }
        return b;
    }

    public static String getFileID() {
        return SystemProperties.get("persist.sys.iaware_config_ver", "com.huawei.andorid.iaware.config_v1.0.xml");
    }
}
