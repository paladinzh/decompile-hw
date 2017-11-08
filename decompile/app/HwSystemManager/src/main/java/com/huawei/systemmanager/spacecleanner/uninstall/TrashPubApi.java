package com.huawei.systemmanager.spacecleanner.uninstall;

import android.content.Context;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TrashPubApi {
    public static String getAppTrashInfo(Context cxt, String[] pathes) {
        if (pathes == null) {
            return "";
        }
        List<String> dirFiles = filterPaths(pathes);
        StringBuilder msgStr = new StringBuilder();
        for (String appTrashDir : dirFiles) {
            List<String> files = getAppTrashFiles(appTrashDir);
            if (files.size() > 0) {
                msgStr.append(cxt.getString(R.string.space_app_trash_dir)).append("\n");
                msgStr.append(appTrashDir).append("\n");
                msgStr.append(cxt.getString(R.string.space_app_trash_files)).append("\n");
                for (String path : files) {
                    msgStr.append(path).append("\n");
                }
            } else {
                msgStr.append(cxt.getString(R.string.space_app_trash_files)).append("\n");
                msgStr.append(appTrashDir).append("\n");
            }
        }
        return msgStr.toString();
    }

    private static List<String> filterPaths(String[] pathes) {
        List<String> results = new ArrayList();
        for (String path1 : pathes) {
            boolean isChildFile = false;
            for (String path2 : pathes) {
                if (!path1.equalsIgnoreCase(path2) && path1.startsWith(path2)) {
                    isChildFile = true;
                    break;
                }
            }
            if (!isChildFile) {
                results.add(path1);
            }
        }
        return results;
    }

    private static List<String> getAppTrashFiles(String dir) {
        List<String> files = Lists.newArrayList();
        if (dir.length() <= 0) {
            return files;
        }
        File dirf = new File(dir);
        if (dirf.exists() && dirf.isDirectory()) {
            File[] allfiles = dirf.listFiles();
            if (allfiles != null) {
                for (File f : allfiles) {
                    files.add(f.getAbsolutePath());
                }
            }
        }
        return files;
    }
}
