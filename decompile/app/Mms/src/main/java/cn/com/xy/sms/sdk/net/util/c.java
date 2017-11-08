package cn.com.xy.sms.sdk.net.util;

import android.content.Context;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.net.l;
import cn.com.xy.sms.sdk.util.KeyManager;
import com.google.android.gms.location.places.Place;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
final class c {
    private static long a = -1;
    private static boolean b = false;
    private static boolean c = false;

    c() {
    }

    private static void a(Context context, boolean z) {
        Throwable th;
        FileOutputStream fileOutputStream = null;
        FileOutputStream fileOutputStream2;
        try {
            a = System.currentTimeMillis();
            b = z;
            File file = new File(context.getApplicationContext().getFilesDir() + File.separator + "check.log");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            fileOutputStream2 = new FileOutputStream(file, false);
            try {
                fileOutputStream2.write(("checkEnable=" + z + "\n").getBytes());
                fileOutputStream2.write(("checkTime=" + System.currentTimeMillis() + "\n").getBytes());
                fileOutputStream2.flush();
            } catch (Throwable th2) {
                th = th2;
                if (fileOutputStream2 != null) {
                    try {
                        fileOutputStream2.close();
                    } catch (Throwable th3) {
                    }
                }
                throw th;
            }
            try {
                fileOutputStream2.close();
            } catch (Throwable th4) {
            }
        } catch (Throwable th5) {
            Throwable th6 = th5;
            fileOutputStream2 = null;
            th = th6;
            if (fileOutputStream2 != null) {
                fileOutputStream2.close();
            }
            throw th;
        }
    }

    static boolean a(Context context) {
        if (a == -1) {
            Map c = c(context);
            if (c == null) {
                a = 0;
                b = true;
            } else {
                b = Boolean.valueOf((String) c.get("checkEnable")).booleanValue();
                a = Long.valueOf((String) c.get("checkTime")).longValue();
            }
        }
        if (b && a != 0) {
            if (!(a + 86400000 >= System.currentTimeMillis())) {
            }
            if (a == 0) {
                return true;
            }
            if (b) {
                return b;
            }
            throw new Exception(" PLEASE CHECK NETWORK IS OK.");
        }
        new d(context).start();
        if (a == 0) {
            return true;
        }
        if (b) {
            return b;
        }
        throw new Exception(" PLEASE CHECK NETWORK IS OK.");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static /* synthetic */ void b(Context context) {
        InputStream inputStream;
        InputStream inputStream2;
        HttpURLConnection httpURLConnection;
        boolean z;
        File file;
        FileOutputStream fileOutputStream;
        InputStream inputStream3;
        Throwable th;
        HttpURLConnection httpURLConnection2;
        Throwable th2;
        boolean z2 = true;
        FileOutputStream fileOutputStream2 = null;
        if (!c) {
            c = true;
            try {
                KeyManager.initAppKey();
                HttpURLConnection httpURLConnection3 = (HttpURLConnection) new URL("http://sdk.bizport.cn:8081/check").openConnection();
                try {
                    httpURLConnection3.setConnectTimeout(40000);
                    httpURLConnection3.setReadTimeout(40000);
                    httpURLConnection3.setDoInput(true);
                    httpURLConnection3.setDoOutput(true);
                    httpURLConnection3.setRequestMethod("GET");
                    httpURLConnection3.setUseCaches(false);
                    httpURLConnection3.setInstanceFollowRedirects(true);
                    httpURLConnection3.setRequestProperty("channel", l.b);
                    httpURLConnection3.connect();
                    if (httpURLConnection3.getResponseCode() != SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) {
                        z2 = false;
                        inputStream = null;
                    } else {
                        inputStream = httpURLConnection3.getInputStream();
                        try {
                            byte[] bArr = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            while (true) {
                                try {
                                    int read = inputStream.read(bArr);
                                    if (read != -1) {
                                        byteArrayOutputStream.write(bArr, 0, read);
                                    }
                                    break;
                                } catch (Throwable th3) {
                                    inputStream3 = inputStream;
                                    th = th3;
                                    httpURLConnection2 = httpURLConnection3;
                                    th2 = th;
                                }
                            }
                            byteArrayOutputStream.flush();
                            if (!new String(byteArrayOutputStream.toByteArray(), "UTF-8").equals("true")) {
                                z2 = false;
                            }
                            byteArrayOutputStream.close();
                        } catch (Throwable th32) {
                            inputStream3 = inputStream;
                            th = th32;
                            httpURLConnection2 = httpURLConnection3;
                            th2 = th;
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable th4) {
                        }
                    }
                    if (httpURLConnection3 != null) {
                        httpURLConnection3.disconnect();
                    }
                } catch (Throwable th322) {
                    th = th322;
                    httpURLConnection2 = httpURLConnection3;
                    th2 = th;
                    if (inputStream3 != null) {
                        try {
                            inputStream3.close();
                        } catch (Throwable th5) {
                            throw th2;
                        }
                    }
                    if (httpURLConnection2 != null) {
                        httpURLConnection2.disconnect();
                    }
                    throw th2;
                }
            } catch (Throwable th6) {
                th2 = th6;
                httpURLConnection2 = null;
                if (inputStream3 != null) {
                    inputStream3.close();
                }
                if (httpURLConnection2 != null) {
                    httpURLConnection2.disconnect();
                }
                throw th2;
            }
            try {
                a = System.currentTimeMillis();
                b = z2;
                file = new File(context.getApplicationContext().getFilesDir() + File.separator + "check.log");
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                fileOutputStream = new FileOutputStream(file, false);
                try {
                    fileOutputStream.write(("checkEnable=" + z2 + "\n").getBytes());
                    fileOutputStream.write(("checkTime=" + System.currentTimeMillis() + "\n").getBytes());
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (Throwable th7) {
                    fileOutputStream2 = fileOutputStream;
                    th2 = th7;
                    if (fileOutputStream2 != null) {
                        try {
                            fileOutputStream2.close();
                        } catch (Throwable th8) {
                        }
                    }
                    try {
                        throw th2;
                    } catch (Throwable th9) {
                    }
                }
            } catch (Throwable th10) {
                th2 = th10;
                if (fileOutputStream2 != null) {
                    fileOutputStream2.close();
                }
                throw th2;
            }
            c = false;
        }
    }

    private static Map<String, String> c(Context context) {
        LineNumberReader lineNumberReader;
        Throwable th;
        LineNumberReader lineNumberReader2 = null;
        try {
            File file = new File(context.getApplicationContext().getFilesDir() + File.separator + "check.log");
            if (!file.exists()) {
                file.createNewFile();
            }
            lineNumberReader = new LineNumberReader(new FileReader(file));
            Map<String, String> map = null;
            while (true) {
                try {
                    String readLine = lineNumberReader.readLine();
                    if (readLine != null) {
                        if (map == null) {
                            map = new HashMap();
                        }
                        String[] split = readLine.split("=");
                        map.put(split[0], split[1]);
                    } else {
                        try {
                            break;
                        } catch (Throwable th2) {
                        }
                    }
                } catch (Throwable th3) {
                    Throwable th4 = th3;
                    lineNumberReader2 = lineNumberReader;
                    th = th4;
                }
            }
            lineNumberReader.close();
            return map;
        } catch (Throwable th5) {
            th = th5;
            if (lineNumberReader2 != null) {
                try {
                    lineNumberReader2.close();
                } catch (Throwable th6) {
                }
            }
            throw th;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void d(Context context) {
        InputStream inputStream;
        HttpURLConnection httpURLConnection;
        boolean z;
        File file;
        FileOutputStream fileOutputStream;
        InputStream inputStream2;
        Throwable th;
        HttpURLConnection httpURLConnection2;
        Throwable th2;
        boolean z2 = true;
        FileOutputStream fileOutputStream2 = null;
        if (!c) {
            c = true;
            try {
                KeyManager.initAppKey();
                HttpURLConnection httpURLConnection3 = (HttpURLConnection) new URL("http://sdk.bizport.cn:8081/check").openConnection();
                try {
                    InputStream inputStream3;
                    httpURLConnection3.setConnectTimeout(40000);
                    httpURLConnection3.setReadTimeout(40000);
                    httpURLConnection3.setDoInput(true);
                    httpURLConnection3.setDoOutput(true);
                    httpURLConnection3.setRequestMethod("GET");
                    httpURLConnection3.setUseCaches(false);
                    httpURLConnection3.setInstanceFollowRedirects(true);
                    httpURLConnection3.setRequestProperty("channel", l.b);
                    httpURLConnection3.connect();
                    if (httpURLConnection3.getResponseCode() != SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) {
                        z2 = false;
                        inputStream3 = null;
                    } else {
                        inputStream3 = httpURLConnection3.getInputStream();
                        try {
                            byte[] bArr = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            while (true) {
                                try {
                                    int read = inputStream3.read(bArr);
                                    if (read != -1) {
                                        byteArrayOutputStream.write(bArr, 0, read);
                                    }
                                    break;
                                } catch (Throwable th3) {
                                    inputStream2 = inputStream3;
                                    th = th3;
                                    httpURLConnection2 = httpURLConnection3;
                                    th2 = th;
                                }
                            }
                            byteArrayOutputStream.flush();
                            if (!new String(byteArrayOutputStream.toByteArray(), "UTF-8").equals("true")) {
                                z2 = false;
                            }
                            byteArrayOutputStream.close();
                        } catch (Throwable th32) {
                            inputStream2 = inputStream3;
                            th = th32;
                            httpURLConnection2 = httpURLConnection3;
                            th2 = th;
                        }
                    }
                    if (inputStream3 != null) {
                        try {
                            inputStream3.close();
                        } catch (Throwable th4) {
                        }
                    }
                    if (httpURLConnection3 != null) {
                        httpURLConnection3.disconnect();
                    }
                } catch (Throwable th322) {
                    th = th322;
                    httpURLConnection2 = httpURLConnection3;
                    th2 = th;
                    if (inputStream2 != null) {
                        try {
                            inputStream2.close();
                        } catch (Throwable th5) {
                            throw th2;
                        }
                    }
                    if (httpURLConnection2 != null) {
                        httpURLConnection2.disconnect();
                    }
                    throw th2;
                }
            } catch (Throwable th6) {
                th2 = th6;
                httpURLConnection2 = null;
                if (inputStream2 != null) {
                    inputStream2.close();
                }
                if (httpURLConnection2 != null) {
                    httpURLConnection2.disconnect();
                }
                throw th2;
            }
            try {
                a = System.currentTimeMillis();
                b = z2;
                file = new File(context.getApplicationContext().getFilesDir() + File.separator + "check.log");
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                fileOutputStream = new FileOutputStream(file, false);
                try {
                    fileOutputStream.write(("checkEnable=" + z2 + "\n").getBytes());
                    fileOutputStream.write(("checkTime=" + System.currentTimeMillis() + "\n").getBytes());
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (Throwable th7) {
                    fileOutputStream2 = fileOutputStream;
                    th2 = th7;
                    if (fileOutputStream2 != null) {
                        try {
                            fileOutputStream2.close();
                        } catch (Throwable th8) {
                        }
                    }
                    try {
                        throw th2;
                    } catch (Throwable th9) {
                    }
                }
            } catch (Throwable th10) {
                th2 = th10;
                if (fileOutputStream2 != null) {
                    fileOutputStream2.close();
                }
                throw th2;
            }
            c = false;
        }
    }
}
