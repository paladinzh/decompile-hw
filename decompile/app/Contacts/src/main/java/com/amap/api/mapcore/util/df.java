package com.amap.api.mapcore.util;

import android.os.Build.VERSION;
import com.amap.api.mapcore.util.de.a;
import com.amap.api.maps.AMapException;
import com.google.android.gms.location.places.Place;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/* compiled from: HttpUrlUtil */
public class df {
    private static dh a;
    private int b;
    private int c;
    private boolean d;
    private SSLContext e;
    private Proxy f;
    private volatile boolean g;
    private long h;
    private long i;
    private HostnameVerifier j;

    public static void a(dh dhVar) {
        a = dhVar;
    }

    df(int i, int i2, Proxy proxy, boolean z) {
        this.g = false;
        this.h = -1;
        this.i = 0;
        this.j = new dg(this);
        this.b = i;
        this.c = i2;
        this.f = proxy;
        this.d = z;
        if (z) {
            try {
                SSLContext instance = SSLContext.getInstance("TLS");
                instance.init(null, null, null);
                this.e = instance;
            } catch (Throwable e) {
                cb.a(e, "HttpUrlUtil", "HttpUrlUtil");
            } catch (Throwable e2) {
                cb.a(e2, "HttpUtil", "HttpUtil");
            }
        }
    }

    df(int i, int i2, Proxy proxy) {
        this(i, i2, proxy, false);
    }

    void a() {
        this.g = true;
    }

    void a(long j) {
        this.i = j;
    }

    void b(long j) {
        this.h = j;
    }

    void a(String str, Map<String, String> map, Map<String, String> map2, a aVar) {
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        if (aVar != null) {
            try {
                String a = a((Map) map2);
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(str);
                if (a != null) {
                    stringBuffer.append("?").append(a);
                }
                httpURLConnection = a(stringBuffer.toString(), (Map) map, false);
                httpURLConnection.setRequestProperty("RANGE", "bytes=" + this.i + "-");
                httpURLConnection.connect();
                int responseCode = httpURLConnection.getResponseCode();
                if (((responseCode == 206 ? 0 : 1) & (responseCode == 200 ? 0 : 1)) != 0) {
                    aVar.a(new bk("网络异常原因：" + httpURLConnection.getResponseMessage() + " 网络异常状态码：" + responseCode));
                }
                inputStream = httpURLConnection.getInputStream();
                Object obj = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                while (!Thread.interrupted()) {
                    if (this.g) {
                        break;
                    }
                    responseCode = inputStream.read(obj, 0, Place.TYPE_SUBLOCALITY_LEVEL_2);
                    if (responseCode <= 0) {
                        break;
                    }
                    if (this.h != -1) {
                        if ((this.i >= this.h ? 1 : null) != null) {
                            break;
                        }
                    }
                    if (responseCode != Place.TYPE_SUBLOCALITY_LEVEL_2) {
                        Object obj2 = new byte[responseCode];
                        System.arraycopy(obj, 0, obj2, 0, responseCode);
                        aVar.a(obj2, this.i);
                    } else {
                        aVar.a(obj, this.i);
                    }
                    this.i = ((long) responseCode) + this.i;
                }
                if (this.g) {
                    aVar.d();
                } else {
                    aVar.e();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable e) {
                        e.printStackTrace();
                        cb.a(e, "HttpUrlUtil", "makeDownloadGetRequest");
                    } catch (Throwable e2) {
                        e2.printStackTrace();
                        cb.a(e2, "HttpUrlUtil", "makeDownloadGetRequest");
                    }
                }
                if (httpURLConnection != null) {
                    try {
                        httpURLConnection.disconnect();
                    } catch (Throwable e22) {
                        e22.printStackTrace();
                        cb.a(e22, "HttpUrlUtil", "makeDownloadGetRequest");
                    }
                }
            } catch (Throwable e222) {
                aVar.a(e222);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable e2222) {
                        e2222.printStackTrace();
                        cb.a(e2222, "HttpUrlUtil", "makeDownloadGetRequest");
                    } catch (Throwable e22222) {
                        e22222.printStackTrace();
                        cb.a(e22222, "HttpUrlUtil", "makeDownloadGetRequest");
                    }
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } catch (Throwable e222222) {
                aVar.a(e222222);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable e2222222) {
                        e2222222.printStackTrace();
                        cb.a(e2222222, "HttpUrlUtil", "makeDownloadGetRequest");
                    } catch (Throwable e22222222) {
                        e22222222.printStackTrace();
                        cb.a(e22222222, "HttpUrlUtil", "makeDownloadGetRequest");
                    }
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } catch (Throwable e222222222) {
                aVar.a(e222222222);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable e2222222222) {
                        e2222222222.printStackTrace();
                        cb.a(e2222222222, "HttpUrlUtil", "makeDownloadGetRequest");
                    } catch (Throwable e22222222222) {
                        e22222222222.printStackTrace();
                        cb.a(e22222222222, "HttpUrlUtil", "makeDownloadGetRequest");
                    }
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } catch (Throwable e222222222222) {
                aVar.a(e222222222222);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable e2222222222222) {
                        e2222222222222.printStackTrace();
                        cb.a(e2222222222222, "HttpUrlUtil", "makeDownloadGetRequest");
                    } catch (Throwable e22222222222222) {
                        e22222222222222.printStackTrace();
                        cb.a(e22222222222222, "HttpUrlUtil", "makeDownloadGetRequest");
                    }
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } catch (Throwable e222222222222222) {
                aVar.a(e222222222222222);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable e2222222222222222) {
                        e2222222222222222.printStackTrace();
                        cb.a(e2222222222222222, "HttpUrlUtil", "makeDownloadGetRequest");
                    } catch (Throwable e22222222222222222) {
                        e22222222222222222.printStackTrace();
                        cb.a(e22222222222222222, "HttpUrlUtil", "makeDownloadGetRequest");
                    }
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } catch (Throwable e222222222222222222) {
                aVar.a(e222222222222222222);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable e2222222222222222222) {
                        e2222222222222222222.printStackTrace();
                        cb.a(e2222222222222222222, "HttpUrlUtil", "makeDownloadGetRequest");
                    } catch (Throwable e22222222222222222222) {
                        e22222222222222222222.printStackTrace();
                        cb.a(e22222222222222222222, "HttpUrlUtil", "makeDownloadGetRequest");
                    }
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } catch (Throwable e222222222222222222222) {
                e222222222222222222222.printStackTrace();
                cb.a(e222222222222222222222, "HttpUrlUtil", "makeDownloadGetRequest");
            }
        }
    }

    dl a(String str, Map<String, String> map, Map<String, String> map2) throws bk {
        try {
            String a = a((Map) map2);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(str);
            if (a != null) {
                stringBuffer.append("?").append(a);
            }
            HttpURLConnection a2 = a(stringBuffer.toString(), (Map) map, false);
            a2.connect();
            return a(a2);
        } catch (ConnectException e) {
            throw new bk(AMapException.ERROR_CONNECTION);
        } catch (MalformedURLException e2) {
            throw new bk("url异常 - MalformedURLException");
        } catch (UnknownHostException e3) {
            throw new bk("未知主机 - UnKnowHostException");
        } catch (SocketException e4) {
            throw new bk(AMapException.ERROR_SOCKET);
        } catch (SocketTimeoutException e5) {
            throw new bk("socket 连接超时 - SocketTimeoutException");
        } catch (IOException e6) {
            throw new bk("IO 操作异常 - IOException");
        } catch (Throwable th) {
            th.printStackTrace();
            bk bkVar = new bk(AMapException.ERROR_UNKNOWN);
        }
    }

    dl a(String str, Map<String, String> map, byte[] bArr) throws bk {
        try {
            HttpURLConnection a = a(str, (Map) map, true);
            if (bArr != null && bArr.length > 0) {
                DataOutputStream dataOutputStream = new DataOutputStream(a.getOutputStream());
                dataOutputStream.write(bArr);
                dataOutputStream.close();
            }
            a.connect();
            return a(a);
        } catch (ConnectException e) {
            e.printStackTrace();
            throw new bk(AMapException.ERROR_CONNECTION);
        } catch (MalformedURLException e2) {
            e2.printStackTrace();
            throw new bk("url异常 - MalformedURLException");
        } catch (UnknownHostException e3) {
            e3.printStackTrace();
            throw new bk("未知主机 - UnKnowHostException");
        } catch (SocketException e4) {
            e4.printStackTrace();
            throw new bk(AMapException.ERROR_SOCKET);
        } catch (SocketTimeoutException e5) {
            e5.printStackTrace();
            throw new bk("socket 连接超时 - SocketTimeoutException");
        } catch (IOException e6) {
            e6.printStackTrace();
            throw new bk("IO 操作异常 - IOException");
        } catch (Throwable th) {
            cb.a(th, "HttpUrlUtil", "makePostReqeust");
            bk bkVar = new bk(AMapException.ERROR_UNKNOWN);
        }
    }

    HttpURLConnection a(String str, Map<String, String> map, boolean z) throws IOException {
        HttpURLConnection httpURLConnection;
        bq.a();
        URL url = new URL(str);
        if (this.f == null) {
            httpURLConnection = (HttpURLConnection) url.openConnection();
        } else {
            httpURLConnection = url.openConnection(this.f);
        }
        if (this.d) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) httpURLConnection;
            httpsURLConnection.setSSLSocketFactory(this.e.getSocketFactory());
            httpsURLConnection.setHostnameVerifier(this.j);
        } else {
            httpURLConnection = httpURLConnection;
        }
        if (VERSION.SDK != null && VERSION.SDK_INT > 13) {
            httpURLConnection.setRequestProperty("Connection", "close");
        }
        a(map, httpURLConnection);
        if (z) {
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
        } else {
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoInput(true);
        }
        return httpURLConnection;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private dl a(HttpURLConnection httpURLConnection) throws bk, IOException {
        IOException e;
        Throwable th;
        PushbackInputStream pushbackInputStream;
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream;
        InputStream inputStream2;
        InputStream pushbackInputStream2;
        try {
            Map headerFields = httpURLConnection.getHeaderFields();
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    inputStream2 = httpURLConnection.getInputStream();
                    try {
                        pushbackInputStream2 = new PushbackInputStream(inputStream2, 2);
                        try {
                            int read;
                            dl dlVar;
                            byte[] bArr = new byte[2];
                            pushbackInputStream2.read(bArr);
                            pushbackInputStream2.unread(bArr);
                            if (bArr[0] == (byte) 31) {
                                if (bArr[1] == (byte) -117) {
                                    inputStream = new GZIPInputStream(pushbackInputStream2);
                                    bArr = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                                    while (true) {
                                        read = inputStream.read(bArr);
                                        if (read == -1) {
                                            break;
                                        }
                                        byteArrayOutputStream.write(bArr, 0, read);
                                    }
                                    if (a != null) {
                                        a.a();
                                    }
                                    dlVar = new dl();
                                    dlVar.a = byteArrayOutputStream.toByteArray();
                                    dlVar.b = headerFields;
                                    if (byteArrayOutputStream != null) {
                                        try {
                                            byteArrayOutputStream.close();
                                        } catch (Throwable e2) {
                                            cb.a(e2, "HttpUrlUtil", "parseResult");
                                            e2.printStackTrace();
                                        }
                                    }
                                    if (inputStream2 != null) {
                                        try {
                                            inputStream2.close();
                                        } catch (Throwable e3) {
                                            cb.a(e3, "HttpUrlUtil", "parseResult");
                                            e3.printStackTrace();
                                        }
                                    }
                                    if (pushbackInputStream2 != null) {
                                        try {
                                            pushbackInputStream2.close();
                                        } catch (Throwable e4) {
                                            cb.a(e4, "HttpUrlUtil", "parseResult");
                                            e4.printStackTrace();
                                        }
                                    }
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (Throwable e42) {
                                            cb.a(e42, "HttpUrlUtil", "parseResult");
                                            e42.printStackTrace();
                                        }
                                    }
                                    if (httpURLConnection != null) {
                                        try {
                                            httpURLConnection.disconnect();
                                        } catch (Throwable e422) {
                                            cb.a(e422, "HttpUrlUtil", "parseResult");
                                            e422.printStackTrace();
                                        }
                                    }
                                    return dlVar;
                                }
                            }
                            inputStream = pushbackInputStream2;
                            bArr = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                            while (true) {
                                read = inputStream.read(bArr);
                                if (read == -1) {
                                    break;
                                }
                                byteArrayOutputStream.write(bArr, 0, read);
                            }
                            if (a != null) {
                                a.a();
                            }
                            dlVar = new dl();
                            dlVar.a = byteArrayOutputStream.toByteArray();
                            dlVar.b = headerFields;
                            if (byteArrayOutputStream != null) {
                                byteArrayOutputStream.close();
                            }
                            if (inputStream2 != null) {
                                inputStream2.close();
                            }
                            if (pushbackInputStream2 != null) {
                                pushbackInputStream2.close();
                            }
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                            }
                            return dlVar;
                        } catch (IOException e5) {
                            e = e5;
                        }
                    } catch (IOException e6) {
                        e = e6;
                        pushbackInputStream2 = null;
                        try {
                            throw e;
                        } catch (Throwable th2) {
                            th = th2;
                            InputStream inputStream3 = pushbackInputStream2;
                            pushbackInputStream2 = inputStream;
                            pushbackInputStream = inputStream3;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        pushbackInputStream2 = null;
                        if (byteArrayOutputStream != null) {
                            try {
                                byteArrayOutputStream.close();
                            } catch (Throwable e22) {
                                cb.a(e22, "HttpUrlUtil", "parseResult");
                                e22.printStackTrace();
                            }
                        }
                        if (inputStream2 != null) {
                            try {
                                inputStream2.close();
                            } catch (Throwable e32) {
                                cb.a(e32, "HttpUrlUtil", "parseResult");
                                e32.printStackTrace();
                            }
                        }
                        if (pushbackInputStream != null) {
                            try {
                                pushbackInputStream.close();
                            } catch (Throwable e7) {
                                cb.a(e7, "HttpUrlUtil", "parseResult");
                                e7.printStackTrace();
                            }
                        }
                        if (pushbackInputStream2 != null) {
                            try {
                                pushbackInputStream2.close();
                            } catch (Throwable e4222) {
                                cb.a(e4222, "HttpUrlUtil", "parseResult");
                                e4222.printStackTrace();
                            }
                        }
                        if (httpURLConnection != null) {
                            try {
                                httpURLConnection.disconnect();
                            } catch (Throwable e42222) {
                                cb.a(e42222, "HttpUrlUtil", "parseResult");
                                e42222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (IOException e8) {
                    e = e8;
                    pushbackInputStream2 = null;
                    inputStream2 = null;
                    throw e;
                } catch (Throwable th4) {
                    th = th4;
                    pushbackInputStream2 = null;
                    inputStream2 = null;
                    if (byteArrayOutputStream != null) {
                        byteArrayOutputStream.close();
                    }
                    if (inputStream2 != null) {
                        inputStream2.close();
                    }
                    if (pushbackInputStream != null) {
                        pushbackInputStream.close();
                    }
                    if (pushbackInputStream2 != null) {
                        pushbackInputStream2.close();
                    }
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                    throw th;
                }
            }
            throw new bk("网络异常原因：" + httpURLConnection.getResponseMessage() + " 网络异常状态码：" + responseCode);
        } catch (IOException e9) {
            e = e9;
            pushbackInputStream2 = null;
            inputStream2 = null;
            byteArrayOutputStream = null;
            throw e;
        } catch (Throwable th5) {
            th = th5;
            pushbackInputStream2 = null;
            inputStream2 = null;
            byteArrayOutputStream = null;
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (inputStream2 != null) {
                inputStream2.close();
            }
            if (pushbackInputStream != null) {
                pushbackInputStream.close();
            }
            if (pushbackInputStream2 != null) {
                pushbackInputStream2.close();
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            throw th;
        }
    }

    private void a(Map<String, String> map, HttpURLConnection httpURLConnection) {
        if (map != null) {
            for (String str : map.keySet()) {
                httpURLConnection.addRequestProperty(str, (String) map.get(str));
            }
        }
        try {
            httpURLConnection.addRequestProperty("csid", UUID.randomUUID().toString().replaceAll("-", "").toLowerCase());
        } catch (Throwable th) {
            cb.a(th, "HttpUrlUtil", "addHeaders");
        }
        httpURLConnection.setConnectTimeout(this.b);
        httpURLConnection.setReadTimeout(this.c);
    }

    static String a(Map<String, String> map) {
        if (map == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Entry entry : map.entrySet()) {
            String str = (String) entry.getKey();
            String str2 = (String) entry.getValue();
            if (str2 == null) {
                str2 = "";
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.append("&");
            }
            stringBuilder.append(URLEncoder.encode(str));
            stringBuilder.append("=");
            stringBuilder.append(URLEncoder.encode(str2));
        }
        return stringBuilder.toString();
    }
}
