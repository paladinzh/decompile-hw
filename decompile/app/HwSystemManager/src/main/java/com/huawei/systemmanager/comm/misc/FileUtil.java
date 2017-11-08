package com.huawei.systemmanager.comm.misc;

import android.content.Context;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.text.TextUtils;
import android.text.format.Formatter;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipFile;
import libcore.io.Libcore;

public class FileUtil {
    private static final String TAG = "FileUtil";

    public static void deleteFile(String filePathe) {
        deleteFile(new File(filePathe));
    }

    public static void deleteFile(File file) {
        if (file == null) {
            HwLog.e(TAG, "deleteFile called, but file is null!");
        } else {
            deleteFileInner(file);
        }
    }

    private static void deleteFileInner(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteFileInner(child);
                }
            }
            if (!file.delete()) {
                HwLog.i(TAG, "delete file failed!, path:");
            }
            return;
        }
        if (!file.delete()) {
            HwLog.i(TAG, "delete file failed!, path:");
        }
    }

    public static long getSingleFileSize(String path) {
        try {
            return Libcore.os.stat(path).st_size;
        } catch (ErrnoException e) {
            HwLog.e(TAG, "getSingleFileSize error, path:");
            return 0;
        } catch (Exception e2) {
            return 0;
        }
    }

    public static long getFileSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return 0;
        }
        return getFileSizeInner(new File(path));
    }

    private static long getFileSizeInner(File file) {
        if (!file.exists()) {
            return 0;
        }
        if (!file.isDirectory()) {
            return file.length();
        }
        long size = file.length();
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                size += child.length();
            }
        }
        return size;
    }

    public static boolean isDirectory(String path) {
        try {
            return OsConstants.S_ISDIR(Libcore.os.stat(path).st_mode);
        } catch (ErrnoException e) {
            HwLog.e(TAG, "isDirectory error, path:");
            return false;
        } catch (Exception e2) {
            return false;
        }
    }

    public static boolean isExsist(String path) {
        try {
            return Libcore.os.access(path, OsConstants.F_OK);
        } catch (ErrnoException e) {
            HwLog.d(TAG, "isExsist error, path:");
            return false;
        } catch (Exception e2) {
            return false;
        }
    }

    public static long getlastModified(String path) {
        long j = 0;
        try {
            return Libcore.os.stat(path).st_mtime * 1000;
        } catch (ErrnoException e) {
            return j;
        } catch (Exception e2) {
            return j;
        } catch (Error e3) {
            return j;
        }
    }

    public static long getlastAccess(String path) {
        long j = 0;
        try {
            return Libcore.os.stat(path).st_atime * 1000;
        } catch (ErrnoException e) {
            return j;
        } catch (Exception e2) {
            return j;
        } catch (Error e3) {
            return j;
        }
    }

    public static String getFileSize(long size) {
        return getFileSize(GlobalContext.getContext(), size);
    }

    public static String getFileSize(Context ctx, long size) {
        return Formatter.formatFileSize(ctx, size);
    }

    public static String getFileSuffix(String path) {
        if (path == null) {
            return "";
        }
        int dotIndex = path.lastIndexOf(".");
        return dotIndex >= 0 ? path.substring(dotIndex) : "";
    }

    public static String getFileName(String path) {
        int separatorIndex = path.lastIndexOf(File.separatorChar);
        return separatorIndex < 0 ? path : path.substring(separatorIndex + 1, path.length());
    }

    public static String getParent(String path) {
        int length = path.length();
        int firstInPath = 0;
        if (File.separatorChar == '\\' && length > 2 && path.charAt(1) == ':') {
            firstInPath = 2;
        }
        int index = path.lastIndexOf(File.separatorChar);
        if (index == -1 && firstInPath > 0) {
            index = 2;
        }
        if (index == -1 || path.charAt(length - 1) == File.separatorChar) {
            return null;
        }
        if (path.indexOf(File.separatorChar) == index && path.charAt(firstInPath) == File.separatorChar) {
            return path.substring(0, index + 1);
        }
        return path.substring(0, index);
    }

    public static String join(String prefix, String suffix) {
        boolean haveSlash;
        int prefixLength = prefix.length();
        if (prefixLength <= 0 || prefix.charAt(prefixLength - 1) != File.separatorChar) {
            haveSlash = false;
        } else {
            haveSlash = true;
        }
        if (!haveSlash) {
            haveSlash = suffix.length() > 0 && suffix.charAt(0) == File.separatorChar;
        }
        return haveSlash ? prefix + suffix : prefix + File.separatorChar + suffix;
    }

    public static boolean isZipFile(String path) {
        ZipFile zipFile;
        ZipFile zipFile2 = null;
        boolean isZipFile = true;
        try {
            zipFile = new ZipFile(path);
        } catch (FileNotFoundException e) {
            isZipFile = false;
            HwLog.i(TAG, "FileNotFoundException, Read the  zip file failed!");
        } catch (IOException e2) {
            isZipFile = false;
            HwLog.i(TAG, "IOException, Read the  zip file failed!");
        } catch (Exception e3) {
            isZipFile = false;
            HwLog.i(TAG, "Exception, Read the  zip file failed!");
        } finally {
            Closeables.close(
/*
Method generation error in method: com.huawei.systemmanager.comm.misc.FileUtil.isZipFile(java.lang.String):boolean
jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x003a: INVOKE  (wrap: java.util.zip.ZipFile
  ?: MERGE  (r4_1 'zipFile' java.util.zip.ZipFile) = (r4_0 'zipFile2' java.util.zip.ZipFile), (r5_0 'zipFile' java.util.zip.ZipFile)) com.huawei.systemmanager.comm.misc.Closeables.close(java.io.Closeable):void type: STATIC in method: com.huawei.systemmanager.comm.misc.FileUtil.isZipFile(java.lang.String):boolean
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:226)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:203)
	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:100)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:50)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:93)
	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:297)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:87)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:53)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:187)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:328)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:265)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:228)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:118)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:83)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:19)
	at jadx.core.ProcessClass.process(ProcessClass.java:43)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: ?: MERGE  (r4_1 'zipFile' java.util.zip.ZipFile) = (r4_0 'zipFile2' java.util.zip.ZipFile), (r5_0 'zipFile' java.util.zip.ZipFile) in method: com.huawei.systemmanager.comm.misc.FileUtil.isZipFile(java.lang.String):boolean
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:226)
	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:101)
	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:679)
	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:649)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:343)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
	... 21 more
Caused by: jadx.core.utils.exceptions.CodegenException: MERGE can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:530)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:514)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:211)
	... 26 more

*/

            public static String[] formatFileSizeByString(Context context, long number) {
                String value;
                float result = (float) number;
                int suffix = 17039497;
                if (result > 900.0f) {
                    suffix = 17039498;
                    result /= 1024.0f;
                }
                if (result > 900.0f) {
                    suffix = 17039499;
                    result /= 1024.0f;
                }
                if (result > 900.0f) {
                    suffix = 17039500;
                    result /= 1024.0f;
                }
                if (result > 900.0f) {
                    suffix = 17039501;
                    result /= 1024.0f;
                }
                if (result > 900.0f) {
                    suffix = 17039502;
                    result /= 1024.0f;
                }
                if (result < Utility.ALPHA_MAX) {
                    value = String.format("%.2f", new Object[]{Float.valueOf(result)});
                } else if (result < HSMConst.DEVICE_SIZE_100) {
                    value = String.format("%.2f", new Object[]{Float.valueOf(result)});
                } else if (result < 100.0f) {
                    value = String.format("%.2f", new Object[]{Float.valueOf(result)});
                } else {
                    value = String.format("%.0f", new Object[]{Float.valueOf(result)});
                }
                return new String[]{value, context.getString(suffix)};
            }
        }
