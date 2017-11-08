package com.huawei.mms.util;

import android.text.TextUtils;
import com.huawei.cspcommon.MLog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    public static void zipFiles(Collection<File> resFileList, File zipFile) throws IOException {
        zipFiles(resFileList, zipFile, null);
    }

    public static void zipFiles(Collection<File> resFileList, File zipFile, String comment) throws IOException {
        Throwable th;
        long begin = System.currentTimeMillis();
        ZipOutputStream zipOutputStream = null;
        try {
            ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile), 1048576));
            try {
                for (File resFile : resFileList) {
                    zipFile(resFile, zipout, "");
                }
                if (!TextUtils.isEmpty(comment)) {
                    zipout.setComment(comment);
                }
                if (zipout != null) {
                    zipout.close();
                }
                MLog.i("ZipUtils", "ZipUtilsZip attachments files usage " + (System.currentTimeMillis() - begin));
            } catch (Throwable th2) {
                th = th2;
                zipOutputStream = zipout;
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            if (zipOutputStream != null) {
                zipOutputStream.close();
            }
            throw th;
        }
    }

    public static void unZipFile(File zipFile, String folderPath) throws ZipException, IOException {
        FileNotFoundException fe;
        IOException ie;
        Throwable th;
        File desDir = new File(folderPath);
        if (!(desDir.exists() || desDir.mkdirs())) {
            MLog.d("ZipUtils", "mkdir failed (for file doesn't exist)");
        }
        ZipFile zipFile2 = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        int fileEntries = 0;
        try {
            ZipFile zipFile3 = new ZipFile(zipFile);
            try {
                Enumeration<?> entries = zipFile3.entries();
                OutputStream out = null;
                while (entries.hasMoreElements()) {
                    try {
                        ZipEntry entry = (ZipEntry) entries.nextElement();
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        inputStream = zipFile3.getInputStream(entry);
                        String canonicalPath = new File(entry.getName()).getCanonicalPath();
                        if (canonicalPath.startsWith(new File(".").getCanonicalPath())) {
                            File desFile = new File(new String((folderPath + File.separator + canonicalPath).getBytes("8859_1"), "GB2312"));
                            if (!desFile.exists()) {
                                File fileParentDir = desFile.getParentFile();
                                if (fileParentDir == null) {
                                    outputStream = out;
                                } else {
                                    if (!(fileParentDir.exists() || fileParentDir.mkdirs())) {
                                        MLog.d("ZipUtils", "mkdir failed (for fileParentDir doesn't exist)");
                                    }
                                    if (!desFile.createNewFile()) {
                                        MLog.d("ZipUtils", "create new file failed");
                                    }
                                }
                            }
                            if (out != null) {
                                out.close();
                            }
                            OutputStream fileOutputStream = new FileOutputStream(desFile);
                            byte[] buffer = new byte[1048576];
                            int total = 0;
                            while (true) {
                                int realLength = inputStream.read(buffer);
                                if (realLength <= 0) {
                                    break;
                                }
                                fileOutputStream.write(buffer, 0, realLength);
                                total += realLength;
                                if (total > 104857600) {
                                    throw new IOException("Too big file to unzip.");
                                }
                            }
                            fileEntries++;
                            if (fileEntries > 1024) {
                                MLog.e("ZipUtils", "Too many files to unzip.");
                                break;
                            }
                        } else {
                            outputStream = out;
                        }
                        out = outputStream;
                    } catch (FileNotFoundException e) {
                        fe = e;
                        outputStream = out;
                        zipFile2 = zipFile3;
                    } catch (IOException e2) {
                        ie = e2;
                        outputStream = out;
                        zipFile2 = zipFile3;
                    } catch (Throwable th2) {
                        th = th2;
                        outputStream = out;
                        zipFile2 = zipFile3;
                    }
                }
                outputStream = out;
                if (zipFile3 != null) {
                    try {
                        zipFile3.close();
                    } catch (Exception e3) {
                        MLog.d("ZipUtils", e3.getMessage());
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Exception e32) {
                        MLog.d("ZipUtils", e32.getMessage());
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e322) {
                        MLog.d("ZipUtils", e322.getMessage());
                    }
                }
                zipFile2 = zipFile3;
            } catch (FileNotFoundException e4) {
                fe = e4;
                zipFile2 = zipFile3;
            } catch (IOException e5) {
                ie = e5;
                zipFile2 = zipFile3;
            } catch (Throwable th3) {
                th = th3;
                zipFile2 = zipFile3;
            }
        } catch (FileNotFoundException e6) {
            fe = e6;
            try {
                MLog.d("ZipUtils", fe.getMessage());
                if (zipFile2 != null) {
                    try {
                        zipFile2.close();
                    } catch (Exception e3222) {
                        MLog.d("ZipUtils", e3222.getMessage());
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Exception e32222) {
                        MLog.d("ZipUtils", e32222.getMessage());
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e322222) {
                        MLog.d("ZipUtils", e322222.getMessage());
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                if (zipFile2 != null) {
                    try {
                        zipFile2.close();
                    } catch (Exception e3222222) {
                        MLog.d("ZipUtils", e3222222.getMessage());
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Exception e32222222) {
                        MLog.d("ZipUtils", e32222222.getMessage());
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e322222222) {
                        MLog.d("ZipUtils", e322222222.getMessage());
                    }
                }
                throw th;
            }
        } catch (IOException e7) {
            ie = e7;
            MLog.d("ZipUtils", ie.getMessage());
            if (zipFile2 != null) {
                try {
                    zipFile2.close();
                } catch (Exception e3222222222) {
                    MLog.d("ZipUtils", e3222222222.getMessage());
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e32222222222) {
                    MLog.d("ZipUtils", e32222222222.getMessage());
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e322222222222) {
                    MLog.d("ZipUtils", e322222222222.getMessage());
                }
            }
        }
    }

    private static void zipFile(File resFile, ZipOutputStream zipout, String rootpath) throws FileNotFoundException, IOException {
        ZipException ze;
        Throwable th;
        Exception e;
        String rootpath2 = new String((rootpath + (rootpath.trim().length() == 0 ? "" : File.separator) + resFile.getName()).getBytes("8859_1"), "GB2312");
        if (resFile.isDirectory()) {
            File[] fileList = resFile.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    zipFile(file, zipout, rootpath2);
                }
            } else {
                return;
            }
        }
        BufferedInputStream bufferedInputStream = null;
        try {
            byte[] buffer = new byte[1048576];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(resFile), 1048576);
            try {
                zipout.putNextEntry(new ZipEntry(rootpath2));
                while (true) {
                    int realLength = in.read(buffer);
                    if (realLength == -1) {
                        break;
                    }
                    zipout.write(buffer, 0, realLength);
                }
                zipout.flush();
                zipout.closeEntry();
                if (in != null) {
                    in.close();
                }
            } catch (ZipException e2) {
                ze = e2;
                bufferedInputStream = in;
                try {
                    MLog.d("ZipUtils", ze.getMessage());
                    if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                    }
                    throw th;
                }
            } catch (Exception e3) {
                e = e3;
                bufferedInputStream = in;
                MLog.d("ZipUtils", e.getMessage());
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedInputStream = in;
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                throw th;
            }
        } catch (ZipException e4) {
            ze = e4;
            MLog.d("ZipUtils", ze.getMessage());
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
        } catch (Exception e5) {
            e = e5;
            MLog.d("ZipUtils", e.getMessage());
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir == null) {
            return false;
        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children == null) {
                return false;
            }
            for (String file : children) {
                if (!deleteDir(new File(dir, file))) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static boolean copyFile(String srcString, String dstString) {
        Throwable th;
        File src = new File(srcString);
        File dst = new File(dstString);
        if (!src.exists()) {
            return false;
        }
        if (!src.isFile()) {
            return false;
        }
        if (!src.canRead()) {
            return false;
        }
        if (!dst.getParentFile().exists()) {
            MLog.i("ZipUtils", "mkdir result " + dst.getParentFile().mkdirs());
        }
        if (dst.exists() && !dst.delete()) {
            return false;
        }
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            FileInputStream fis = new FileInputStream(src);
            try {
                FileOutputStream fos = new FileOutputStream(dst);
                try {
                    byte[] buf = new byte[1000];
                    while (true) {
                        int c = fis.read(buf);
                        if (c <= 0) {
                            break;
                        }
                        fos.write(buf, 0, c);
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            MLog.e("ZipUtils", e.getMessage());
                        }
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e2) {
                            MLog.e("ZipUtils", e2.getMessage());
                        }
                    }
                } catch (FileNotFoundException e3) {
                    fileOutputStream = fos;
                    fileInputStream = fis;
                    try {
                        MLog.e("ZipUtils", "copyFile: FileNotFoundException");
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e22) {
                                MLog.e("ZipUtils", e22.getMessage());
                            }
                        }
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e222) {
                                MLog.e("ZipUtils", e222.getMessage());
                            }
                        }
                        return true;
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e2222) {
                                MLog.e("ZipUtils", e2222.getMessage());
                            }
                        }
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e22222) {
                                MLog.e("ZipUtils", e22222.getMessage());
                            }
                        }
                        throw th;
                    }
                } catch (IOException e4) {
                    fileOutputStream = fos;
                    fileInputStream = fis;
                    MLog.e("ZipUtils", "copyFile: IOException");
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e222222) {
                            MLog.e("ZipUtils", e222222.getMessage());
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e2222222) {
                            MLog.e("ZipUtils", e2222222.getMessage());
                        }
                    }
                    return true;
                } catch (Throwable th3) {
                    th = th3;
                    fileOutputStream = fos;
                    fileInputStream = fis;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e5) {
                fileInputStream = fis;
                MLog.e("ZipUtils", "copyFile: FileNotFoundException");
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return true;
            } catch (IOException e6) {
                fileInputStream = fis;
                MLog.e("ZipUtils", "copyFile: IOException");
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return true;
            } catch (Throwable th4) {
                th = th4;
                fileInputStream = fis;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            MLog.e("ZipUtils", "copyFile: FileNotFoundException");
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return true;
        } catch (IOException e8) {
            MLog.e("ZipUtils", "copyFile: IOException");
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return true;
        }
        return true;
    }
}
