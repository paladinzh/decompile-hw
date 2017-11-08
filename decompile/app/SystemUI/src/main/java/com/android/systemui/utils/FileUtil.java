package com.android.systemui.utils;

import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class FileUtil {
    public static String getPresetPath(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        File file;
        ArrayList<File> files = getHwCfgFileList("xml/" + fileName, 0);
        if (files != null && files.size() > 0) {
            Log.d("FileUtil", "getHwCfgFileList, files.size = " + files.size());
            int i = files.size() - 1;
            while (i >= 0) {
                file = (File) files.get(i);
                if (file == null || !file.exists()) {
                    i--;
                } else {
                    Log.d("FileUtil", "file.path=" + file.getPath());
                    return file.getPath();
                }
            }
        }
        file = new File(new StringBuffer("/data/cust/xml").toString(), fileName);
        if (file.exists()) {
            return file.getPath();
        }
        file = new File(new StringBuffer("/system/etc").toString(), fileName);
        if (file.exists()) {
            return file.getPath();
        }
        file = new File(new StringBuffer("/system/etc").append("/xml").toString(), fileName);
        if (file.exists()) {
            return file.getPath();
        }
        return null;
    }

    public static ArrayList<File> getHwCfgFileList(String fileName, int type) {
        Class<?> cfg = CompatUtils.getClass("com.huawei.cust.HwCfgFilePolicy");
        if (cfg != null) {
            Method method = CompatUtils.getMethod(cfg, "getCfgFileList", String.class, Integer.TYPE);
            if (method != null) {
                try {
                    return (ArrayList) CompatUtils.invoke(null, method, fileName, Integer.valueOf(type));
                } catch (UnsupportedOperationException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
