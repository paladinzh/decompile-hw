package cn.com.xy.sms.sdk.util;

import android.content.res.AssetManager;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.b;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import com.google.android.gms.location.places.Place;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipFile;

/* compiled from: Unknown */
public final class f {
    public static ConcurrentHashMap<String, String> a = new ConcurrentHashMap();
    private static String b;
    private static boolean c;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int a(String str, String str2, String str3, boolean z) {
        HttpURLConnection httpURLConnection;
        Closeable closeable;
        Throwable th;
        Throwable th2;
        Closeable closeable2 = null;
        HttpURLConnection httpURLConnection2 = null;
        Closeable closeable3 = null;
        Closeable bufferedOutputStream;
        try {
            if (StringUtils.isNull(str)) {
                a(null);
                a(null);
                a(null);
                return -1;
            }
            byte[] bArr;
            long contentLength;
            File file;
            int i;
            int read;
            String substring;
            Object obj;
            if (a(new StringBuilder(String.valueOf(str2)).append(str3).toString())) {
                c(new StringBuilder(String.valueOf(str2)).append(str3).toString());
            }
            if (a(new StringBuilder(String.valueOf(str2)).append(str3).append(".temp").toString())) {
                c(new StringBuilder(String.valueOf(str2)).append(str3).append(".temp").toString());
            }
            if (!str.startsWith("https")) {
                if (!str.startsWith("HTTPS")) {
                    HttpURLConnection httpURLConnection3 = (HttpURLConnection) new URL(str).openConnection();
                    try {
                        String onLineConfigureData = DexUtil.getOnLineConfigureData(4);
                        if (StringUtils.isNull(onLineConfigureData)) {
                            onLineConfigureData = "bizport.cn/66dc91e8b78b1c284027a3eb1be0a70e";
                        }
                        httpURLConnection3.addRequestProperty("referer", onLineConfigureData);
                        httpURLConnection2 = httpURLConnection3;
                        httpURLConnection2.setConnectTimeout(60000);
                        httpURLConnection2.setReadTimeout(60000);
                        closeable2 = httpURLConnection2.getInputStream();
                        if (httpURLConnection2.getResponseCode() == SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) {
                            a(closeable2);
                            a(null);
                            a(httpURLConnection2);
                            return -1;
                        } else if (b(httpURLConnection2.getURL().toString(), str)) {
                            a(closeable2);
                            a(null);
                            a(httpURLConnection2);
                            return -1;
                        } else {
                            bArr = new byte[8192];
                            contentLength = (long) httpURLConnection2.getContentLength();
                            file = new File(new StringBuilder(String.valueOf(str2)).append(str3).append(".temp").toString());
                            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
                            i = 0;
                            while (!Thread.interrupted()) {
                                try {
                                    read = closeable2.read(bArr);
                                    if (read != -1) {
                                        break;
                                    }
                                    bufferedOutputStream.write(bArr, 0, read);
                                    i += read;
                                } catch (MalformedURLException e) {
                                    httpURLConnection = httpURLConnection2;
                                    closeable = closeable2;
                                } catch (Throwable th3) {
                                    th = th3;
                                    closeable3 = bufferedOutputStream;
                                    th2 = th;
                                }
                            }
                            bufferedOutputStream.flush();
                            substring = str3.substring(0, str3.lastIndexOf("."));
                            if (((long) i) != contentLength) {
                                obj = 1;
                                if (z && !substring.equals(o.a(file))) {
                                    obj = null;
                                }
                                if (obj == null && !str3.startsWith("duoqu_")) {
                                    a(closeable2);
                                    a(bufferedOutputStream);
                                    a(httpURLConnection2);
                                    return -1;
                                }
                                a(str2, new StringBuilder(String.valueOf(str3)).append(".temp").toString(), str3);
                                a(closeable2);
                                a(bufferedOutputStream);
                                a(httpURLConnection2);
                                return 0;
                            }
                            if (a(new StringBuilder(String.valueOf(str2)).append(str3).toString())) {
                                c(new StringBuilder(String.valueOf(str2)).append(str3).toString());
                            }
                            if (a(new StringBuilder(String.valueOf(str2)).append(str3).append(".temp").toString())) {
                                c(new StringBuilder(String.valueOf(str2)).append(str3).append(".temp").toString());
                            }
                            a(closeable2);
                            a(bufferedOutputStream);
                            a(httpURLConnection2);
                            return -1;
                        }
                    } catch (MalformedURLException e2) {
                        closeable = closeable2;
                        HttpURLConnection httpURLConnection4 = httpURLConnection3;
                        bufferedOutputStream = null;
                        httpURLConnection = httpURLConnection4;
                        a(closeable);
                        a(bufferedOutputStream);
                        a(httpURLConnection);
                        return -1;
                    } catch (Throwable th4) {
                        th = th4;
                        httpURLConnection2 = httpURLConnection3;
                        th2 = th;
                        a(closeable2);
                        a(closeable3);
                        a(httpURLConnection2);
                        throw th2;
                    }
                }
            }
            httpURLConnection2 = b.a(str, 0);
            httpURLConnection2.setConnectTimeout(60000);
            httpURLConnection2.setReadTimeout(60000);
            closeable2 = httpURLConnection2.getInputStream();
            if (httpURLConnection2.getResponseCode() == SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) {
                a(closeable2);
                a(null);
                a(httpURLConnection2);
                return -1;
            } else if (b(httpURLConnection2.getURL().toString(), str)) {
                a(closeable2);
                a(null);
                a(httpURLConnection2);
                return -1;
            } else {
                bArr = new byte[8192];
                contentLength = (long) httpURLConnection2.getContentLength();
                file = new File(new StringBuilder(String.valueOf(str2)).append(str3).append(".temp").toString());
                bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
                i = 0;
                while (!Thread.interrupted()) {
                    read = closeable2.read(bArr);
                    if (read != -1) {
                        break;
                    }
                    bufferedOutputStream.write(bArr, 0, read);
                    i += read;
                }
                bufferedOutputStream.flush();
                substring = str3.substring(0, str3.lastIndexOf("."));
                if (((long) i) != contentLength) {
                    if (a(new StringBuilder(String.valueOf(str2)).append(str3).toString())) {
                        c(new StringBuilder(String.valueOf(str2)).append(str3).toString());
                    }
                    if (a(new StringBuilder(String.valueOf(str2)).append(str3).append(".temp").toString())) {
                        c(new StringBuilder(String.valueOf(str2)).append(str3).append(".temp").toString());
                    }
                    a(closeable2);
                    a(bufferedOutputStream);
                    a(httpURLConnection2);
                    return -1;
                }
                obj = 1;
                obj = null;
                if (obj == null) {
                    a(closeable2);
                    a(bufferedOutputStream);
                    a(httpURLConnection2);
                    return -1;
                }
                a(str2, new StringBuilder(String.valueOf(str3)).append(".temp").toString(), str3);
                a(closeable2);
                a(bufferedOutputStream);
                a(httpURLConnection2);
                return 0;
            }
        } catch (MalformedURLException e3) {
            bufferedOutputStream = null;
            httpURLConnection = httpURLConnection2;
            closeable = closeable2;
            a(closeable);
            a(bufferedOutputStream);
            a(httpURLConnection);
            return -1;
        } catch (Throwable th5) {
            th2 = th5;
            a(closeable2);
            a(closeable3);
            a(httpURLConnection2);
            throw th2;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static File a(String str, String str2, InputStream inputStream) {
        File file;
        File file2;
        Throwable th;
        Closeable closeable = null;
        if (inputStream == null) {
            return null;
        }
        Closeable fileOutputStream;
        try {
            file = new File(new StringBuilder(String.valueOf(str)).append(str2).toString());
            fileOutputStream = new FileOutputStream(file);
            try {
                byte[] bArr = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                while (true) {
                    int read = inputStream.read(bArr);
                    if (read <= 0) {
                        break;
                    }
                    fileOutputStream.write(bArr, 0, read);
                }
                fileOutputStream.flush();
                a(fileOutputStream);
                a((Closeable) inputStream);
            } catch (Throwable th2) {
                file2 = file;
                try {
                    if (a(new StringBuilder(String.valueOf(str)).append(str2).toString())) {
                        c(new StringBuilder(String.valueOf(str)).append(str2).toString());
                    }
                    a(fileOutputStream);
                    a((Closeable) inputStream);
                    file = file2;
                    return file;
                } catch (Throwable th3) {
                    Throwable th4 = th3;
                    closeable = fileOutputStream;
                    th = th4;
                    a(closeable);
                    a((Closeable) inputStream);
                    throw th;
                }
            }
        } catch (Throwable th5) {
            th = th5;
            a(closeable);
            a((Closeable) inputStream);
            throw th;
        }
        return file;
    }

    public static File a(String str, String str2, String str3, boolean z, XyCallBack xyCallBack) {
        try {
            if (NetUtil.checkAccessNetWork(2)) {
                File file = a(new StringBuilder(String.valueOf(str)).append(str2).toString(), str3, str2, z) != 0 ? null : new File(new StringBuilder(String.valueOf(str3)).append(str2).toString());
                if (file != null && file.exists()) {
                    XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(5), str2, file);
                } else {
                    XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-6), str2, "download failed");
                    file = null;
                }
                return file;
            }
            throw new Exception("no network");
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), str2, th.getMessage());
            return null;
        }
    }

    public static String a(InputStream inputStream) {
        try {
            return new String(b(inputStream), "UTF-8");
        } catch (Throwable th) {
            return "";
        }
    }

    public static void a() {
        try {
            List e = e(Constant.getPARSE_PATH(), "PU", ".jar");
            if (e != null) {
                int size = e.size();
                for (int i = 0; i < size; i++) {
                    String name = ((File) e.get(i)).getName();
                    int lastIndexOf = name.lastIndexOf("_");
                    if (lastIndexOf != -1) {
                        a.put(name.substring(0, lastIndexOf + 1), name);
                    }
                }
            }
        } catch (Throwable th) {
        }
    }

    public static void a(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable th) {
            }
        }
    }

    public static void a(File file) {
        if (file != null) {
            try {
                if (file.exists()) {
                    if (file.isFile()) {
                        file.delete();
                        return;
                    }
                    if (file.isDirectory()) {
                        File[] listFiles = file.listFiles();
                        if (listFiles != null) {
                            for (File a : listFiles) {
                                a(a);
                            }
                            file.delete();
                            return;
                        }
                        file.delete();
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    public static void a(String str, String str2) {
        try {
            File file = new File(str);
            if (file.exists()) {
                file.renameTo(new File(str2));
            }
        } catch (Throwable th) {
        }
    }

    public static void a(String str, String str2, String str3) {
        a(new StringBuilder(String.valueOf(str)).append(str2).toString(), new StringBuilder(String.valueOf(str)).append(str3).toString());
    }

    public static void a(String str, String str2, String str3, String str4) {
        try {
            a(e(str, str2, str3), str4);
        } catch (Throwable th) {
        }
    }

    private static void a(HttpURLConnection httpURLConnection) {
        if (httpURLConnection != null) {
            try {
                httpURLConnection.disconnect();
            } catch (Throwable th) {
            }
        }
    }

    private static void a(List<File> list, String str) {
        if (list != null) {
            try {
                if (!list.isEmpty()) {
                    Iterator it = list.iterator();
                    while (it != null && it.hasNext()) {
                        File file = (File) it.next();
                        if (!file.getName().equals(str)) {
                            file.delete();
                        }
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    public static void a(ZipFile zipFile) {
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (Throwable th) {
            }
        }
    }

    public static boolean a(String str) {
        return new File(str).exists();
    }

    public static InputStream b(String str) {
        try {
            File file = new File(str);
            return !file.exists() ? null : new FileInputStream(file);
        } catch (Throwable th) {
            return null;
        }
    }

    public static String b() {
        return "3634343535373433";
    }

    public static void b(String str, String str2, String str3) {
        try {
            File dir = Constant.getContext().getDir("outdex", 0);
            if (dir != null) {
                a(e(dir.getCanonicalPath(), str, str2), str3);
            }
        } catch (Throwable th) {
        }
    }

    private static boolean b(String str, String str2) {
        try {
            if (str.length() > str2.length()) {
                int indexOf = str.indexOf("?");
                if (!(indexOf == -1 || str.substring(indexOf + 1).indexOf(str2.replaceFirst("https://", "").replaceFirst("HTTPS://", "").replaceFirst("http://", "").replaceFirst("HTTP://", "")) == -1)) {
                    return false;
                }
            }
            return true;
        } catch (Throwable th) {
            return false;
        }
    }

    public static byte[] b(InputStream inputStream) {
        Throwable th;
        byte[] bArr = new byte[2560];
        Closeable byteArrayOutputStream = new ByteArrayOutputStream();
        Closeable closeable = null;
        Closeable dataInputStream;
        try {
            dataInputStream = new DataInputStream(inputStream);
            while (true) {
                try {
                    int read = dataInputStream.read(bArr);
                    if (read > 0) {
                        byteArrayOutputStream.write(bArr, 0, read);
                    } else {
                        byte[] toByteArray = byteArrayOutputStream.toByteArray();
                        a(dataInputStream);
                        a((Closeable) inputStream);
                        a(byteArrayOutputStream);
                        return toByteArray;
                    }
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    closeable = dataInputStream;
                    th = th3;
                }
            }
        } catch (Throwable th4) {
            th = th4;
            a(closeable);
            a((Closeable) inputStream);
            a(byteArrayOutputStream);
            throw th;
        }
    }

    public static AssetManager c() {
        AssetManager assetManager = null;
        try {
            assetManager = DuoquUtils.getSdkDoAction().getExtendAssetManager();
        } catch (Exception e) {
        }
        return assetManager != null ? assetManager : Constant.getContext().getResources().getAssets();
    }

    public static boolean c(String str) {
        try {
            if (StringUtils.isNull(str)) {
                return false;
            }
            File file = new File(str);
            if (!file.exists() || !file.isFile()) {
                return false;
            }
            file.delete();
            return true;
        } catch (Throwable th) {
            return false;
        }
    }

    public static boolean c(String str, String str2, String str3) {
        try {
            File file = new File(str);
            if (!file.exists()) {
                file.mkdir();
            }
            File[] listFiles = file.listFiles(new s(str2, str3));
            if (listFiles != null && listFiles.length > 0) {
                return true;
            }
        } catch (Throwable th) {
        }
        return false;
    }

    public static String d(String str, String str2, String str3) {
        try {
            String str4 = (String) a.remove(str2);
            if (str4 != null) {
                return new StringBuilder(String.valueOf(str)).append(File.separator).append(str4).toString();
            }
            File file = new File(str);
            if (!file.exists()) {
                file.mkdir();
            }
            File[] listFiles = file.listFiles(new s(str2, str3));
            if (listFiles != null && listFiles.length > 0) {
                return listFiles[0].getCanonicalPath();
            }
            return "";
        } catch (Throwable th) {
        }
    }

    public static void d(String str) {
        a(new File(str));
    }

    public static List<File> e(String str, String str2, String str3) {
        List arrayList = new ArrayList();
        try {
            File file = new File(str);
            if (!file.exists()) {
                file.mkdir();
            }
            File[] listFiles = file.listFiles(new s(str2, str3));
            if (listFiles != null && listFiles.length > 0) {
                return Arrays.asList(listFiles);
            }
        } catch (Throwable th) {
        }
        return arrayList;
    }

    public static byte[] e(String str) {
        return b(new FileInputStream(str));
    }

    public static int f(String str, String str2, String str3) {
        return a(str, str2, str3, false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String f(String str) {
        Closeable closeable = null;
        try {
            closeable = c().open(str);
            byte[] bArr = new byte[closeable.available()];
            closeable.read(bArr);
            closeable.close();
            String str2 = new String(bArr, "GB2312");
            if (StringUtils.isNull(str2)) {
                a(closeable);
                return ThemeUtil.SET_NULL_STR;
            }
            a(closeable);
            return str2;
        } catch (IOException e) {
            a(closeable);
        } catch (Throwable th) {
            Throwable th2 = th;
            Closeable closeable2 = closeable;
            Throwable th3 = th2;
            a(closeable2);
            throw th3;
        }
    }

    public static List<String> g(String str) {
        Closeable open;
        Closeable bufferedReader;
        Throwable th;
        Closeable closeable = null;
        try {
            open = c().open(str);
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(open, "GB2312"));
                try {
                    List<String> arrayList = new ArrayList();
                    while (true) {
                        String readLine = bufferedReader.readLine();
                        if (readLine != null) {
                            arrayList.add(readLine);
                        } else {
                            a(bufferedReader);
                            a(open);
                            return arrayList;
                        }
                    }
                } catch (IOException e) {
                    a(bufferedReader);
                    a(open);
                    return null;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    closeable = bufferedReader;
                    th = th3;
                    a(closeable);
                    a(open);
                    throw th;
                }
            } catch (IOException e2) {
                bufferedReader = null;
                a(bufferedReader);
                a(open);
                return null;
            } catch (Throwable th4) {
                th = th4;
                a(closeable);
                a(open);
                throw th;
            }
        } catch (IOException e3) {
            bufferedReader = null;
            open = null;
            a(bufferedReader);
            a(open);
            return null;
        } catch (Throwable th5) {
            th = th5;
            open = null;
            a(closeable);
            a(open);
            throw th;
        }
    }

    public static String h(String str) {
        Throwable th;
        Closeable closeable = null;
        Closeable open;
        Closeable bufferedReader;
        try {
            open = c().open(str);
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(open, "GB2312"));
                while (true) {
                    try {
                        String readLine = bufferedReader.readLine();
                        if (readLine == null) {
                            break;
                        } else if (!StringUtils.isNull(readLine) && readLine.indexOf("PVER:") != -1) {
                            readLine = readLine.substring(5).trim();
                            a(bufferedReader);
                            a(open);
                            return readLine;
                        }
                    } catch (Throwable th2) {
                        Throwable th3 = th2;
                        closeable = bufferedReader;
                        th = th3;
                    }
                }
                a(bufferedReader);
                a(open);
            } catch (Throwable th4) {
                th = th4;
                a(closeable);
                a(open);
                throw th;
            }
        } catch (Throwable th5) {
            th = th5;
            open = null;
            a(closeable);
            a(open);
            throw th;
        }
        return "";
    }
}
