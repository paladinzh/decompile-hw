package com.android.gallery3d.data;

import android.content.Context;
import android.os.FileUtils;
import android.os.Process;
import com.android.gallery3d.util.GalleryLog;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilePreference {
    private static final String TAG = FilePreference.class.getSimpleName();

    public static void put(Context context, String name) {
        File target = getTargetFile(context, name);
        if (!target.exists()) {
            try {
                GalleryLog.d(TAG, "create new File " + target.createNewFile());
                FileUtils.setPermissions(target.getPath(), 508, Process.myUid(), 1023);
            } catch (IOException e) {
                GalleryLog.i(TAG, "File.createNewFile() failed in put() method, reason: IOException.");
            }
        }
    }

    private static File ensureFolderExist(Context context) {
        return context.getDir("system_hidden_folder", 1);
    }

    private static File getTargetFile(Context context, String name) {
        return new File(ensureFolderExist(context), name);
    }

    public static boolean get(Context context, String name) {
        return getTargetFile(context, name).exists();
    }

    public static boolean remove(Context context, String name) {
        return getTargetFile(context, name).delete();
    }

    public static List<String> getAll(Context context) {
        int i = 0;
        File[] files = ensureFolderExist(context).listFiles();
        if (files == null || files.length == 0) {
            return new ArrayList(0);
        }
        List<String> ret = new ArrayList(3);
        int length = files.length;
        while (i < length) {
            ret.add(files[i].getName());
            i++;
        }
        return ret;
    }
}
