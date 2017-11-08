package com.huawei.gallery.livephoto;

import android.os.SystemProperties;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.gadget.XmlUtils;
import com.android.gallery3d.util.GalleryLog;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.Closeable;
import java.io.FileInputStream;
import java.util.Arrays;

public class LiveUtils {
    public static final boolean LIVE_ENABLE = SystemProperties.getBoolean("ro.hwcamera.livephoto_enable", false);
    static boolean sNeedDownloadTips = true;

    public static int getExtInfoLength() {
        return 40;
    }

    @SuppressWarnings({"REC_CATCH_EXCEPTION"})
    public static long getVideoOffset(String filepath) {
        Exception ex;
        Throwable th;
        if (filepath == null) {
            return -1;
        }
        Closeable closeable = null;
        try {
            Closeable in = new FileInputStream(filepath);
            try {
                long fileLength = (long) in.available();
                if (fileLength < 40) {
                    Utils.closeSilently(in);
                    return -1;
                }
                long skiped = in.skip(fileLength - 20);
                byte[] buffer = new byte[20];
                if (in.read(buffer) != 20) {
                    GalleryLog.w("LiveUtils", "file length:" + fileLength + "skiped " + skiped);
                    Utils.closeSilently(in);
                    return -1;
                }
                String tag = new String(buffer, XmlUtils.INPUT_ENCODING).trim();
                if (tag.startsWith("LIVE_")) {
                    long parseLong = Long.parseLong(tag.split("_")[1]);
                    Utils.closeSilently(in);
                    return parseLong;
                }
                Utils.closeSilently(in);
                closeable = in;
                return -1;
            } catch (Exception e) {
                ex = e;
                closeable = in;
                try {
                    GalleryLog.w("LiveUtils", "get offset from file error. " + ex);
                    Utils.closeSilently(closeable);
                    return -1;
                } catch (Throwable th2) {
                    th = th2;
                    Utils.closeSilently(closeable);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                closeable = in;
                Utils.closeSilently(closeable);
                throw th;
            }
        } catch (Exception e2) {
            ex = e2;
            GalleryLog.w("LiveUtils", "get offset from file error. " + ex);
            Utils.closeSilently(closeable);
            return -1;
        }
    }

    @SuppressWarnings({"REC_CATCH_EXCEPTION", "PZLA_PREFER_ZERO_LENGTH_ARRAYS"})
    public static int[] readPlayInfo(String filepath) {
        Exception ex;
        Throwable th;
        if (filepath == null) {
            return null;
        }
        Closeable in = null;
        try {
            Closeable in2 = new FileInputStream(filepath);
            try {
                long fileLength = (long) in2.available();
                if (fileLength < 40) {
                    Utils.closeSilently(in2);
                    return null;
                }
                long skiped = in2.skip(fileLength - 40);
                byte[] buffer = new byte[20];
                if (in2.read(buffer) != 20) {
                    GalleryLog.w("LiveUtils", "file length:" + fileLength + "skiped " + skiped);
                    Utils.closeSilently(in2);
                    return null;
                }
                String[] info = new String(buffer, XmlUtils.INPUT_ENCODING).trim().split(":");
                GalleryLog.d("LiveUtils", "play info from file: " + Arrays.toString(info));
                if (info.length < 2) {
                    Utils.closeSilently(in2);
                    return null;
                }
                int[] playInfo = new int[]{Integer.parseInt(info[0]), Integer.parseInt(info[1])};
                Utils.closeSilently(in2);
                return playInfo;
            } catch (Exception e) {
                ex = e;
                in = in2;
                try {
                    GalleryLog.w("LiveUtils", "get offset from file error. " + ex);
                    Utils.closeSilently(in);
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    Utils.closeSilently(in);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                in = in2;
                Utils.closeSilently(in);
                throw th;
            }
        } catch (Exception e2) {
            ex = e2;
            GalleryLog.w("LiveUtils", "get offset from file error. " + ex);
            Utils.closeSilently(in);
            return null;
        }
    }
}
