package com.autonavi.amap.mapcore;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.storage.StorageManager;
import com.amap.api.maps.MapsInitializer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class FileUtil {
    private static final String TAG = "FileUtil";

    public static void copy(Context context, String str, File file) throws Exception {
        file.delete();
        InputStream open = context.getAssets().open(str);
        byte[] bArr = new byte[open.available()];
        open.read(bArr);
        open.close();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(bArr);
        fileOutputStream.close();
    }

    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (listFiles != null) {
                for (int i = 0; i < listFiles.length; i++) {
                    if (listFiles[i].isFile()) {
                        if (!listFiles[i].delete()) {
                            return false;
                        }
                    } else if (!deleteFile(listFiles[i])) {
                        return false;
                    } else {
                        listFiles[i].delete();
                    }
                }
            }
        }
        file.delete();
        return true;
    }

    public static String getMapBaseStorage(Context context) {
        File file;
        String str = "map_base_path";
        if (VERSION.SDK_INT > 18) {
            str = "map_base_path_v44";
        }
        String str2 = "";
        SharedPreferences sharedPreferences = context.getSharedPreferences("base_path", 0);
        if (MapsInitializer.sdcardDir != null && MapsInitializer.sdcardDir.trim().length() > 0) {
            str2 = MapsInitializer.sdcardDir;
        } else {
            str2 = sharedPreferences.getString(str, "");
        }
        if (str2 != null && str2.length() > 2) {
            file = new File(str2);
            if (!file.exists()) {
                file.mkdir();
            }
            if (file.isDirectory()) {
                if (file.canWrite()) {
                    return str2;
                }
                str2 = context.getCacheDir().toString();
                if (str2 != null && str2.length() > 2 && new File(str2).isDirectory()) {
                    return str2;
                }
            }
        }
        str2 = getExternalStroragePath(context);
        if (str2 != null && str2.length() > 2) {
            str2 = str2 + File.separator + MapTilsCacheAndResManager.AUTONAVI_PATH;
            file = new File(str2);
            if (!file.exists()) {
                file.mkdir();
            }
            if (file.isDirectory()) {
                Editor edit = sharedPreferences.edit();
                edit.putString(str, str2);
                edit.commit();
                createNoMediaFileIfNotExist(str2);
                return str2;
            }
        }
        str = context.getCacheDir().toString();
        if (str != null && str.length() > 2) {
            str = str + File.separator + MapTilsCacheAndResManager.AUTONAVI_PATH;
            File file2 = new File(str);
            if (!file2.exists()) {
                file2.mkdir();
            }
            if (file2.isDirectory()) {
                return str;
            }
        }
        return str;
    }

    public static String getExternalStroragePath(Context context) {
        int i = VERSION.SDK_INT;
        if (i >= 12) {
            try {
                String str;
                StorageManager storageManager = (StorageManager) context.getSystemService("storage");
                Method method = StorageManager.class.getMethod("getVolumeList", new Class[0]);
                Method method2 = StorageManager.class.getMethod("getVolumeState", new Class[]{String.class});
                Object[] objArr = (Object[]) method.invoke(storageManager, new Object[0]);
                Boolean.valueOf(false);
                String str2 = "";
                String str3 = "";
                str2 = "";
                String str4 = "";
                int length = objArr.length;
                int i2 = 0;
                while (i2 < length) {
                    Object obj = objArr[i2];
                    Method method3 = obj.getClass().getMethod("getPath", new Class[0]);
                    str2 = (String) method3.invoke(obj, new Object[0]);
                    String str5 = (String) method2.invoke(storageManager, new Object[]{method3.invoke(obj, new Object[0])});
                    Boolean bool = (Boolean) obj.getClass().getMethod("isRemovable", new Class[0]).invoke(obj, new Object[0]);
                    if (str2.toLowerCase().contains("private")) {
                        str5 = str4;
                        str2 = str3;
                    } else if (!bool.booleanValue()) {
                        continue;
                    } else if (str2 == null || str5 == null) {
                        str5 = str4;
                        str2 = str3;
                    } else if (str5.equals("mounted")) {
                        if (i > 18) {
                            try {
                                File[] externalFilesDirs = context.getExternalFilesDirs(null);
                                if (externalFilesDirs == null) {
                                    str2 = null;
                                } else if (externalFilesDirs.length > 1) {
                                    str2 = externalFilesDirs[1].getAbsolutePath();
                                }
                                str = str2;
                            } catch (Exception e) {
                                str = str2;
                            }
                        } else {
                            str = str2;
                        }
                        if (i <= 18) {
                            if (!(str3 == null || str4 == null || !str4.equals("mounted"))) {
                                str = str3;
                            }
                            return str;
                        }
                        if (str != null || str3 == null) {
                            str3 = str;
                        } else if (str4 == null) {
                            str3 = str;
                        } else if (!str4.equals("mounted")) {
                            str3 = str;
                        }
                        return str3;
                    } else {
                        str5 = str4;
                        str2 = str3;
                    }
                    i2++;
                    str4 = str5;
                    str3 = str2;
                }
                str = null;
                if (i <= 18) {
                    if (str != null) {
                        if (str4 == null) {
                            str3 = str;
                        } else if (str4.equals("mounted")) {
                            str3 = str;
                        }
                        return str3;
                    }
                    str3 = str;
                    return str3;
                }
                str = str3;
                return str;
            } catch (Throwable th) {
            }
        }
        if (Environment.getExternalStorageState().equals("mounted")) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    public static void writeDatasToFile(String str, byte[] bArr) {
        WriteLock writeLock = new ReentrantReadWriteLock().writeLock();
        writeLock.lock();
        if (bArr != null) {
            try {
                if (bArr.length != 0) {
                    File file = new File(str);
                    if (file.exists()) {
                        file.delete();
                    }
                    file.createNewFile();
                    OutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(bArr);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                writeLock.unlock();
            }
        }
        writeLock.unlock();
    }

    public static byte[] readFileContents(String str) {
        try {
            File file = new File(str);
            if (!file.exists()) {
                return null;
            }
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bArr = new byte[1024];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (true) {
                int read = fileInputStream.read(bArr);
                if (read == -1) {
                    byteArrayOutputStream.close();
                    fileInputStream.close();
                    return byteArrayOutputStream.toByteArray();
                }
                byteArrayOutputStream.write(bArr, 0, read);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void createNoMediaFileIfNotExist(String str) {
    }
}
