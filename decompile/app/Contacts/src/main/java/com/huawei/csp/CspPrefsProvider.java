package com.huawei.csp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.contacts.util.HwLog;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CspPrefsProvider extends ContentProvider {
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    public String getType(Uri uri) {
        return "*/*";
    }

    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    public boolean onCreate() {
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public Bundle call(String method, String arg, Bundle extras) {
        if (!isPermittedtoCall(getContext(), Binder.getCallingUid())) {
            HwLog.e("CspPrefProvider", "Invalid caller");
            return super.call(method, arg, extras);
        } else if (!"get_pref_files_content".equals(method)) {
            return super.call(method, arg, extras);
        } else {
            Bundle ret = getCspPreference(getContext());
            if (HwLog.HWDBG) {
                HwLog.d("CspPrefProvider", "status " + ret.getInt("call_status", 0) + ";  " + ret);
            }
            return ret;
        }
    }

    private boolean isPermittedtoCall(Context context, int callerId) {
        String[] callerPackages = context.getPackageManager().getPackagesForUid(callerId);
        if (callerPackages == null || callerPackages.length == 0) {
            return false;
        }
        int i = 0;
        while (i < callerPackages.length) {
            if ("com.android.mms".equals(callerPackages[i]) || "com.huawei.contactstool".equals(callerPackages[i])) {
                return true;
            }
            i++;
        }
        return false;
    }

    private static Bundle getCspPreference(Context context) {
        if (HwLog.HWDBG) {
            HwLog.d("CspPrefProvider", "test get pref files and content");
        }
        Bundle ret = listPrefFiles(context);
        ArrayList<String> fileLists = ret.getStringArrayList("target_file_list");
        if (fileLists == null) {
            HwLog.e("CspPrefProvider", "Can't get Pref file list from csp");
            ret.putInt("call_status", -1);
            return ret;
        }
        int total = 0;
        for (String str : fileLists) {
            Bundle content = getFileContent(context, str);
            if (content == null || content.size() == 0) {
                HwLog.e("CspPrefProvider", "Can't get file content" + str);
            } else {
                HwLog.w("CspPrefProvider", "get file content " + str + "; " + content.size());
                ret.putBundle(str, content);
                total++;
            }
        }
        ret.putInt("call_status", total);
        return ret;
    }

    private static Bundle listPrefFiles(Context context) {
        String SKIP_PREFS = "SimInfoFile";
        Bundle retBundle = new Bundle();
        File tstPrefFile = context.getSharedPrefsFile("test_temp_emui_4_0");
        File prefDir = tstPrefFile.getParentFile();
        if (prefDir == null) {
            if (HwLog.HWDBG) {
                HwLog.d("CspPrefProvider", "can't get test pref file.");
            }
            return retBundle;
        }
        if (tstPrefFile.exists() && HwLog.HWDBG) {
            if (tstPrefFile.delete()) {
                HwLog.d("CspPrefProvider", "delete temp test file");
            } else {
                HwLog.d("CspPrefProvider", "delete temp test file failed");
            }
        }
        if (prefDir.exists() && prefDir.isDirectory()) {
            File[] files = prefDir.listFiles();
            if (files == null) {
                if (HwLog.HWDBG) {
                    HwLog.d("CspPrefProvider", " file list is null");
                }
                return retBundle;
            }
            ArrayList<String> fileLists = new ArrayList(files.length);
            int sufixLen = ".xml".length();
            for (File name : files) {
                String fileName = name.getName();
                if (!(TextUtils.isEmpty(fileName) || !fileName.endsWith(".xml") || fileName.startsWith("SimInfoFile"))) {
                    fileLists.add(fileName.substring(0, fileName.length() - sufixLen));
                }
            }
            if (fileLists.size() == 0) {
                HwLog.e("CspPrefProvider", "No pref files exists?");
                return retBundle;
            }
            retBundle.putStringArrayList("target_file_list", fileLists);
            return retBundle;
        }
        HwLog.e("CspPrefProvider", "pref dir not exists ? " + prefDir.getAbsolutePath());
        return retBundle;
    }

    private static Bundle getFileContent(Context context, String file) {
        if (TextUtils.isEmpty(file)) {
            return null;
        }
        Map<String, ?> allPrefers = context.getSharedPreferences(file, 0).getAll();
        int len = allPrefers.size();
        if (len == 0) {
            if (HwLog.HWDBG) {
                HwLog.d("CspPrefProvider", "no content exists" + file);
            }
            return null;
        }
        Bundle retBundle = new Bundle(len);
        for (int i = 0; i < len; i++) {
            for (Entry<String, ?> ent : allPrefers.entrySet()) {
                Object value = ent.getValue();
                String key = (String) ent.getKey();
                if (!(value == null || key == null)) {
                    String clazz = value.getClass().getSimpleName();
                    if ("String".equals(clazz)) {
                        retBundle.putString(key, (String) value);
                    } else if ("Integer".equals(clazz)) {
                        retBundle.putInt(key, ((Integer) value).intValue());
                    } else if ("Boolean".equals(clazz)) {
                        retBundle.putBoolean(key, ((Boolean) value).booleanValue());
                    } else if ("Long".equals(clazz)) {
                        retBundle.putLong(key, ((Long) value).longValue());
                    } else if ("Float".equals(clazz)) {
                        retBundle.putFloat(key, ((Float) value).floatValue());
                    } else if ("Set".equals(clazz)) {
                        retBundle.putStringArrayList(key, new ArrayList((Set) value));
                    } else {
                        HwLog.e("CspPrefProvider", "GetFileContent unsupport type " + clazz + "; " + key);
                    }
                }
            }
        }
        return retBundle;
    }
}
