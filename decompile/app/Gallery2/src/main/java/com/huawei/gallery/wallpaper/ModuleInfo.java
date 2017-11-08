package com.huawei.gallery.wallpaper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

public class ModuleInfo {
    public static final Uri CONTENT_URI = Uri.parse("content://com.huawei.android.thememanager.ContentProvider/moduleInfo");
    private static String[] sProjection = new String[]{"module_name", "preview_path"};
    private String mModuleName = "";
    private String mPreviewPath = "";

    public ModuleInfo(Cursor cursor) {
        this.mModuleName = cursor.getString(cursor.getColumnIndex("module_name"));
        this.mPreviewPath = cursor.getString(cursor.getColumnIndex("preview_path"));
    }

    public static ModuleInfo loadModuleInfo(Context context, String moduleName) {
        List<ModuleInfo> infoList = loadModuleInfo(context, CONTENT_URI, "module_name='" + moduleName + "'");
        if (infoList.size() > 0) {
            return (ModuleInfo) infoList.get(0);
        }
        return null;
    }

    public static List<ModuleInfo> loadModuleInfo(Context context, Uri uri, String where) {
        List<ModuleInfo> infoList = new ArrayList();
        Cursor cursor = context.getContentResolver().query(uri, sProjection, where, null, null);
        if (cursor == null) {
            return infoList;
        }
        while (cursor.moveToNext()) {
            try {
                infoList.add(new ModuleInfo(cursor));
            } catch (IllegalArgumentException e) {
            } catch (Exception e2) {
            } finally {
                cursor.close();
            }
        }
        return infoList;
    }

    public String getPreviewPath() {
        return this.mPreviewPath;
    }
}
