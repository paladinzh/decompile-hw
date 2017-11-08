package com.amap.api.mapcore.util;

import android.os.Build.VERSION;
import com.amap.api.mapcore.util.gx.a;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PushbackInputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

/* compiled from: HttpUrlUtil */
public class ha {
    private static hb a;
    private int b;
    private int c;
    private boolean d;
    private SSLContext e;
    private Proxy f;
    private volatile boolean g;
    private long h;
    private long i;
    private String j;
    private a k;
    private HostnameVerifier l;

    private void b() {
        try {
            this.j = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
        } catch (Throwable th) {
            fl.a(th, "HttpUrlUtil", "initCSID");
        }
    }

    public static void a(hb hbVar) {
        a = hbVar;
    }

    ha(int i, int i2, Proxy proxy, boolean z) {
        this(i, i2, proxy, z, null);
    }

    ha(int i, int i2, Proxy proxy, boolean z, a aVar) {
        this.g = false;
        this.h = -1;
        this.i = 0;
        this.l = new HostnameVerifier(this) {
            final /* synthetic */ ha a;

            {
                this.a = r1;
            }

            public boolean verify(String str, SSLSession sSLSession) {
                HostnameVerifier defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
                if (defaultHostnameVerifier.verify("*.amap.com", sSLSession) || defaultHostnameVerifier.verify("*.apilocate.amap.com", sSLSession)) {
                    return true;
                }
                return false;
            }
        };
        this.b = i;
        this.c = i2;
        this.f = proxy;
        this.d = z;
        this.k = aVar;
        b();
        if (z) {
            try {
                SSLContext instance = SSLContext.getInstance("TLS");
                instance.init(null, null, null);
                this.e = instance;
            } catch (Throwable th) {
                fl.a(th, "HttpUtil", "HttpUtil");
            }
        }
    }

    ha(int i, int i2, Proxy proxy) {
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

    void a(String str, Map<String, String> map, Map<String, String> map2, gz.a aVar) {
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
                if (((responseCode == SmsCheckResult.ESCT_206 ? 0 : 1) & (responseCode == SmsCheckResult.ESCT_200 ? 0 : 1)) != 0) {
                    aVar.a(new ex("网络异常原因：" + httpURLConnection.getResponseMessage() + " 网络异常状态码：" + responseCode));
                }
                inputStream = httpURLConnection.getInputStream();
                Object obj = new byte[1024];
                while (!Thread.interrupted()) {
                    if (this.g) {
                        break;
                    }
                    responseCode = inputStream.read(obj, 0, 1024);
                    if (responseCode <= 0) {
                        break;
                    }
                    if (this.h != -1) {
                        if ((this.i >= this.h ? 1 : null) != null) {
                            break;
                        }
                    }
                    if (responseCode != 1024) {
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
                        fl.a(e, "HttpUrlUtil", "makeDownloadGetRequest");
                    } catch (Throwable e2) {
                        fl.a(e2, "HttpUrlUtil", "makeDownloadGetRequest");
                    }
                }
                if (httpURLConnection != null) {
                    try {
                        httpURLConnection.disconnect();
                    } catch (Throwable e22) {
                        fl.a(e22, "HttpUrlUtil", "makeDownloadGetRequest");
                    }
                }
            } catch (Throwable e222) {
                fl.a(e222, "HttpUrlUtil", "makeDownloadGetRequest");
            }
        }
    }

    hf a(String str, Map<String, String> map, Map<String, String> map2) throws ex {
        ex e;
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
        } catch (ConnectException e2) {
            throw new ex("http连接失败 - ConnectionException");
        } catch (MalformedURLException e3) {
            throw new ex("url异常 - MalformedURLException");
        } catch (UnknownHostException e4) {
            throw new ex("未知主机 - UnKnowHostException");
        } catch (SocketException e5) {
            throw new ex("socket 连接异常 - SocketException");
        } catch (SocketTimeoutException e6) {
            throw new ex("socket 连接超时 - SocketTimeoutException");
        } catch (InterruptedIOException e7) {
            throw new ex("未知的错误");
        } catch (IOException e8) {
            throw new ex("IO 操作异常 - IOException");
        } catch (ex e9) {
            throw e9;
        } catch (Throwable th) {
            th.printStackTrace();
            e9 = new ex("未知的错误");
        }
    }

    hf a(String str, Map<String, String> map, byte[] bArr) throws ex {
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
            throw new ex("http连接失败 - ConnectionException");
        } catch (MalformedURLException e2) {
            e2.printStackTrace();
            throw new ex("url异常 - MalformedURLException");
        } catch (UnknownHostException e3) {
            e3.printStackTrace();
            throw new ex("未知主机 - UnKnowHostException");
        } catch (SocketException e4) {
            e4.printStackTrace();
            throw new ex("socket 连接异常 - SocketException");
        } catch (SocketTimeoutException e5) {
            e5.printStackTrace();
            throw new ex("socket 连接超时 - SocketTimeoutException");
        } catch (InterruptedIOException e6) {
            throw new ex("未知的错误");
        } catch (IOException e7) {
            e7.printStackTrace();
            throw new ex("IO 操作异常 - IOException");
        } catch (Throwable e8) {
            fl.a(e8, "HttpUrlUtil", "makePostReqeust");
            throw e8;
        } catch (Throwable e82) {
            fl.a(e82, "HttpUrlUtil", "makePostReqeust");
            ex exVar = new ex("未知的错误");
        }
    }

    HttpURLConnection a(String str, Map<String, String> map, boolean z) throws IOException {
        HttpURLConnection httpURLConnection;
        URLConnection uRLConnection = null;
        fc.a();
        URL url = new URL(str);
        if (this.k != null) {
            uRLConnection = this.k.a(this.f, url);
        }
        if (uRLConnection == null) {
            if (this.f == null) {
                uRLConnection = url.openConnection();
            } else {
                uRLConnection = url.openConnection(this.f);
            }
        }
        if (this.d) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) uRLConnection;
            httpsURLConnection.setSSLSocketFactory(this.e.getSocketFactory());
            httpsURLConnection.setHostnameVerifier(this.l);
        } else {
            httpURLConnection = (HttpURLConnection) uRLConnection;
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
    private hf a(HttpURLConnection httpURLConnection) throws ex, IOException {
        InputStream pushbackInputStream;
        IOException e;
        Throwable th;
        PushbackInputStream pushbackInputStream2;
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream;
        InputStream inputStream2;
        try {
            Map headerFields = httpURLConnection.getHeaderFields();
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == SmsCheckResult.ESCT_200) {
                byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    inputStream2 = httpURLConnection.getInputStream();
                    try {
                        pushbackInputStream = new PushbackInputStream(inputStream2, 2);
                        try {
                            int read;
                            hf hfVar;
                            byte[] bArr = new byte[2];
                            pushbackInputStream.read(bArr);
                            pushbackInputStream.unread(bArr);
                            if (bArr[0] == (byte) 31) {
                                if (bArr[1] == (byte) -117) {
                                    inputStream = new GZIPInputStream(pushbackInputStream);
                                    bArr = new byte[1024];
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
                                    hfVar = new hf();
                                    hfVar.a = byteArrayOutputStream.toByteArray();
                                    hfVar.b = headerFields;
                                    hfVar.c = this.j;
                                    if (byteArrayOutputStream != null) {
                                        try {
                                            byteArrayOutputStream.close();
                                        } catch (Throwable th2) {
                                            fl.a(th2, "HttpUrlUtil", "parseResult");
                                        }
                                    }
                                    if (inputStream2 != null) {
                                        try {
                                            inputStream2.close();
                                        } catch (Throwable th3) {
                                            fl.a(th3, "HttpUrlUtil", "parseResult");
                                        }
                                    }
                                    if (pushbackInputStream != null) {
                                        try {
                                            pushbackInputStream.close();
                                        } catch (Throwable th4) {
                                            fl.a(th4, "HttpUrlUtil", "parseResult");
                                        }
                                    }
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (Throwable th42) {
                                            fl.a(th42, "HttpUrlUtil", "parseResult");
                                        }
                                    }
                                    if (httpURLConnection != null) {
                                        try {
                                            httpURLConnection.disconnect();
                                        } catch (Throwable th422) {
                                            fl.a(th422, "HttpUrlUtil", "parseResult");
                                        }
                                    }
                                    return hfVar;
                                }
                            }
                            inputStream = pushbackInputStream;
                            bArr = new byte[1024];
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
                            hfVar = new hf();
                            hfVar.a = byteArrayOutputStream.toByteArray();
                            hfVar.b = headerFields;
                            hfVar.c = this.j;
                            if (byteArrayOutputStream != null) {
                                byteArrayOutputStream.close();
                            }
                            if (inputStream2 != null) {
                                inputStream2.close();
                            }
                            if (pushbackInputStream != null) {
                                pushbackInputStream.close();
                            }
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                            }
                            return hfVar;
                        } catch (IOException e2) {
                            e = e2;
                        }
                    } catch (IOException e3) {
                        e = e3;
                        pushbackInputStream = null;
                        try {
                            throw e;
                        } catch (Throwable th5) {
                            th = th5;
                            InputStream inputStream3 = pushbackInputStream;
                            pushbackInputStream = inputStream;
                            pushbackInputStream2 = inputStream3;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        pushbackInputStream = null;
                        if (byteArrayOutputStream != null) {
                            try {
                                byteArrayOutputStream.close();
                            } catch (Throwable th22) {
                                fl.a(th22, "HttpUrlUtil", "parseResult");
                            }
                        }
                        if (inputStream2 != null) {
                            try {
                                inputStream2.close();
                            } catch (Throwable th32) {
                                fl.a(th32, "HttpUrlUtil", "parseResult");
                            }
                        }
                        if (pushbackInputStream2 != null) {
                            try {
                                pushbackInputStream2.close();
                            } catch (Throwable th7) {
                                fl.a(th7, "HttpUrlUtil", "parseResult");
                            }
                        }
                        if (pushbackInputStream != null) {
                            try {
                                pushbackInputStream.close();
                            } catch (Throwable th4222) {
                                fl.a(th4222, "HttpUrlUtil", "parseResult");
                            }
                        }
                        if (httpURLConnection != null) {
                            try {
                                httpURLConnection.disconnect();
                            } catch (Throwable th42222) {
                                fl.a(th42222, "HttpUrlUtil", "parseResult");
                            }
                        }
                        throw th;
                    }
                } catch (IOException e4) {
                    e = e4;
                    pushbackInputStream = null;
                    inputStream2 = null;
                    throw e;
                } catch (Throwable th8) {
                    th = th8;
                    pushbackInputStream = null;
                    inputStream2 = null;
                    if (byteArrayOutputStream != null) {
                        byteArrayOutputStream.close();
                    }
                    if (inputStream2 != null) {
                        inputStream2.close();
                    }
                    if (pushbackInputStream2 != null) {
                        pushbackInputStream2.close();
                    }
                    if (pushbackInputStream != null) {
                        pushbackInputStream.close();
                    }
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                    throw th;
                }
            }
            String str;
            String str2 = "";
            if (headerFields == null) {
                str = str2;
            } else {
                List list = (List) headerFields.get("gsid");
                if (list != null && list.size() > 0) {
                    str = (String) list.get(0);
                } else {
                    str = str2;
                }
            }
            throw new ex("网络异常原因：" + httpURLConnection.getResponseMessage() + " 网络异常状态码：" + responseCode + "  " + str + " " + this.j);
        } catch (IOException e5) {
            e = e5;
            pushbackInputStream = null;
            inputStream2 = null;
            byteArrayOutputStream = null;
            throw e;
        } catch (Throwable th9) {
            th = th9;
            pushbackInputStream = null;
            inputStream2 = null;
            byteArrayOutputStream = null;
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (inputStream2 != null) {
                inputStream2.close();
            }
            if (pushbackInputStream2 != null) {
                pushbackInputStream2.close();
            }
            if (pushbackInputStream != null) {
                pushbackInputStream.close();
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
            httpURLConnection.addRequestProperty("csid", this.j);
        } catch (Throwable th) {
            fl.a(th, "HttpUrlUtil", "addHeaders");
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
