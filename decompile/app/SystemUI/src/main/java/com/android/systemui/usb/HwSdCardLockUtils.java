package com.android.systemui.usb;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.utils.HwLog;
import com.huawei.android.app.HwSdLockManager;
import fyusion.vislib.BuildConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;

public class HwSdCardLockUtils {
    private static HwSdLockManager sHwSdLockManager;

    private static final class MyFilenameFilter implements FilenameFilter {
        private MyFilenameFilter() {
        }

        public boolean accept(File dir, String filename) {
            return filename.length() == 9 ? filename.startsWith("mmc1:") : false;
        }
    }

    public static synchronized HwSdLockManager getHwSdLockManager(Context context) {
        HwSdLockManager hwSdLockManager;
        synchronized (HwSdCardLockUtils.class) {
            if (sHwSdLockManager == null) {
                sHwSdLockManager = new HwSdLockManager(context);
            }
            hwSdLockManager = sHwSdLockManager;
        }
        return hwSdLockManager;
    }

    public static boolean isFeatureSupported(Context context) {
        return getHwSdLockManager(context) != null;
    }

    public static boolean isSdCardEncrpyted(Context context) {
        boolean z = true;
        if (!isFeatureSupported(context)) {
            return false;
        }
        int state = getHwSdLockManager(context).getSDLockState();
        HwLog.d("HwSdCardLockUtils", "sdcard state=" + state);
        if (!(671 == state || 670 == state)) {
            z = false;
        }
        return z;
    }

    public static boolean unlockSDCard(Context context, String password) {
        boolean z = false;
        if (!isFeatureSupported(context)) {
            return false;
        }
        if (getHwSdLockManager(context).unlockSDCard(password) == 200) {
            z = true;
        }
        return z;
    }

    public static void insertSDcardID(Context context) {
        String ssid = getHwSdLockManager(context).getSDCardId();
        HwLog.d("HwSdCardLockUtils", "insertSDcardID=" + ssid);
        ContentResolver resolver = context.getContentResolver();
        ContentValues value = new ContentValues();
        if (!iscontains(context)) {
            value.put("sdcard_id", ssid);
            resolver.insert(HwSDCardLockProvider.CONTENT_URI, value);
        }
    }

    public static int deleteSDcardID(Context context) {
        HwLog.d("HwSdCardLockUtils", "deleteSDcardID=" + getHwSdLockManager(context).getSDCardId());
        ContentResolver resolver = context.getContentResolver();
        if (!iscontains(context)) {
            return -1;
        }
        return resolver.delete(HwSDCardLockProvider.CONTENT_URI, "sdcard_id=?", new String[]{ssid + BuildConfig.FLAVOR});
    }

    public static boolean iscontains(Context context) {
        Cursor cursor = null;
        try {
            cursor = new CursorLoader(context, HwSDCardLockProvider.CONTENT_URI, new String[]{"sdcard_id"}, null, null, null).loadInBackground();
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            String ssid = getHwSdLockManager(context).getSDCardId();
            if (cursor.moveToFirst()) {
                while (!cursor.getString(cursor.getColumnIndex("sdcard_id")).equals(ssid)) {
                    if (!cursor.moveToNext()) {
                    }
                }
                Log.d("HwSdCardLockUtils", "iscontains=true");
                if (cursor != null) {
                    cursor.close();
                }
                return true;
            }
            if (cursor != null) {
                cursor.close();
            }
            HwLog.d("HwSdCardLockUtils", "iscontains=false");
            return false;
        } catch (Exception e) {
            Log.d("HwSdCardLockUtils", e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String getCid() {
        String cid = "cid";
        String result = BuildConfig.FLAVOR;
        String mmc_path = getCidPath();
        if (TextUtils.isEmpty(mmc_path)) {
            return result;
        }
        File[] mmc1 = new File(mmc_path).listFiles(new MyFilenameFilter());
        if (mmc1 != null && mmc1.length == 1) {
            result = readLine(new File(mmc1[0], cid));
        }
        return result;
    }

    private static String getCidPath() {
        return "/sys/class/mmc_host/mmc1";
    }

    private static String readLine(File file) {
        IOException e;
        Throwable th;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            try {
                String s = reader2.readLine();
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
                return s;
            } catch (IOException e4) {
                e2 = e4;
                reader = reader2;
                try {
                    e2.printStackTrace();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        } catch (Exception e32) {
                            e32.printStackTrace();
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        } catch (Exception e322) {
                            e322.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                reader = reader2;
                if (reader != null) {
                    reader.close();
                }
                throw th;
            }
        } catch (IOException e5) {
            e222 = e5;
            e222.printStackTrace();
            if (reader != null) {
                reader.close();
            }
            return null;
        }
    }
}
