package com.amap.api.services.core;

import android.os.Build.VERSION;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
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
public class cg {
    private static ch a;
    private int b;
    private int c;
    private boolean d;
    private SSLContext e;
    private Proxy f;
    private volatile boolean g = false;
    private long h = -1;
    private long i = 0;
    private HostnameVerifier j = new cm(this);

    public static void a(ch chVar) {
        a = chVar;
    }

    cg(int i, int i2, Proxy proxy, boolean z) {
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
                ay.a(e, "HttpUrlUtil", "HttpUrlUtil");
            } catch (Throwable e2) {
                ay.a(e2, "HttpUtil", "HttpUtil");
            }
        }
    }

    cl a(String str, Map<String, String> map, Map<String, String> map2) throws ai {
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
            throw new ai(AMapException.ERROR_CONNECTION);
        } catch (MalformedURLException e2) {
            throw new ai("url异常 - MalformedURLException");
        } catch (UnknownHostException e3) {
            throw new ai("未知主机 - UnKnowHostException");
        } catch (SocketException e4) {
            throw new ai(AMapException.ERROR_SOCKET);
        } catch (SocketTimeoutException e5) {
            throw new ai("socket 连接超时 - SocketTimeoutException");
        } catch (IOException e6) {
            throw new ai("IO 操作异常 - IOException");
        } catch (Throwable th) {
            th.printStackTrace();
            ai aiVar = new ai(AMapException.ERROR_UNKNOWN);
        }
    }

    cl a(String str, Map<String, String> map, byte[] bArr) throws ai {
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
            throw new ai(AMapException.ERROR_CONNECTION);
        } catch (MalformedURLException e2) {
            e2.printStackTrace();
            throw new ai("url异常 - MalformedURLException");
        } catch (UnknownHostException e3) {
            e3.printStackTrace();
            throw new ai("未知主机 - UnKnowHostException");
        } catch (SocketException e4) {
            e4.printStackTrace();
            throw new ai(AMapException.ERROR_SOCKET);
        } catch (SocketTimeoutException e5) {
            e5.printStackTrace();
            throw new ai("socket 连接超时 - SocketTimeoutException");
        } catch (IOException e6) {
            e6.printStackTrace();
            throw new ai("IO 操作异常 - IOException");
        } catch (Throwable th) {
            ay.a(th, "HttpUrlUtil", "makePostReqeust");
            ai aiVar = new ai(AMapException.ERROR_UNKNOWN);
        }
    }

    HttpURLConnection a(String str, Map<String, String> map, boolean z) throws IOException {
        HttpURLConnection httpURLConnection;
        an.a();
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
    private cl a(HttpURLConnection httpURLConnection) throws ai, IOException {
        ByteArrayOutputStream byteArrayOutputStream;
        InputStream pushbackInputStream;
        IOException e;
        Throwable th;
        PushbackInputStream pushbackInputStream2;
        InputStream inputStream = null;
        InputStream inputStream2;
        try {
            Map headerFields = httpURLConnection.getHeaderFields();
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) {
                byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    inputStream2 = httpURLConnection.getInputStream();
                    try {
                        pushbackInputStream = new PushbackInputStream(inputStream2, 2);
                        try {
                            int read;
                            cl clVar;
                            byte[] bArr = new byte[2];
                            pushbackInputStream.read(bArr);
                            pushbackInputStream.unread(bArr);
                            if (bArr[0] == (byte) 31) {
                                if (bArr[1] == (byte) -117) {
                                    inputStream = new GZIPInputStream(pushbackInputStream);
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
                                    clVar = new cl();
                                    clVar.a = byteArrayOutputStream.toByteArray();
                                    clVar.b = headerFields;
                                    if (byteArrayOutputStream != null) {
                                        try {
                                            byteArrayOutputStream.close();
                                        } catch (Throwable e2) {
                                            ay.a(e2, "HttpUrlUtil", "parseResult");
                                            e2.printStackTrace();
                                        }
                                    }
                                    if (inputStream2 != null) {
                                        try {
                                            inputStream2.close();
                                        } catch (Throwable e3) {
                                            ay.a(e3, "HttpUrlUtil", "parseResult");
                                            e3.printStackTrace();
                                        }
                                    }
                                    if (pushbackInputStream != null) {
                                        try {
                                            pushbackInputStream.close();
                                        } catch (Throwable e4) {
                                            ay.a(e4, "HttpUrlUtil", "parseResult");
                                            e4.printStackTrace();
                                        }
                                    }
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (Throwable e42) {
                                            ay.a(e42, "HttpUrlUtil", "parseResult");
                                            e42.printStackTrace();
                                        }
                                    }
                                    if (httpURLConnection != null) {
                                        try {
                                            httpURLConnection.disconnect();
                                        } catch (Throwable e422) {
                                            ay.a(e422, "HttpUrlUtil", "parseResult");
                                            e422.printStackTrace();
                                        }
                                    }
                                    return clVar;
                                }
                            }
                            inputStream = pushbackInputStream;
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
                            clVar = new cl();
                            clVar.a = byteArrayOutputStream.toByteArray();
                            clVar.b = headerFields;
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
                            return clVar;
                        } catch (IOException e5) {
                            e = e5;
                        }
                    } catch (IOException e6) {
                        e = e6;
                        pushbackInputStream = null;
                        try {
                            throw e;
                        } catch (Throwable th2) {
                            th = th2;
                            InputStream inputStream3 = pushbackInputStream;
                            pushbackInputStream = inputStream;
                            pushbackInputStream2 = inputStream3;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        pushbackInputStream = null;
                        if (byteArrayOutputStream != null) {
                            try {
                                byteArrayOutputStream.close();
                            } catch (Throwable e22) {
                                ay.a(e22, "HttpUrlUtil", "parseResult");
                                e22.printStackTrace();
                            }
                        }
                        if (inputStream2 != null) {
                            try {
                                inputStream2.close();
                            } catch (Throwable e32) {
                                ay.a(e32, "HttpUrlUtil", "parseResult");
                                e32.printStackTrace();
                            }
                        }
                        if (pushbackInputStream2 != null) {
                            try {
                                pushbackInputStream2.close();
                            } catch (Throwable e7) {
                                ay.a(e7, "HttpUrlUtil", "parseResult");
                                e7.printStackTrace();
                            }
                        }
                        if (pushbackInputStream != null) {
                            try {
                                pushbackInputStream.close();
                            } catch (Throwable e4222) {
                                ay.a(e4222, "HttpUrlUtil", "parseResult");
                                e4222.printStackTrace();
                            }
                        }
                        if (httpURLConnection != null) {
                            try {
                                httpURLConnection.disconnect();
                            } catch (Throwable e42222) {
                                ay.a(e42222, "HttpUrlUtil", "parseResult");
                                e42222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (IOException e8) {
                    e = e8;
                    pushbackInputStream = null;
                    inputStream2 = null;
                    throw e;
                } catch (Throwable th4) {
                    th = th4;
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
            throw new ai("网络异常原因：" + httpURLConnection.getResponseMessage() + " 网络异常状态码：" + responseCode);
        } catch (IOException e9) {
            e = e9;
            pushbackInputStream = null;
            inputStream2 = null;
            byteArrayOutputStream = null;
            throw e;
        } catch (Throwable th5) {
            th = th5;
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
            httpURLConnection.addRequestProperty("csid", UUID.randomUUID().toString().replaceAll("-", "").toLowerCase());
        } catch (Throwable th) {
            ay.a(th, "HttpUrlUtil", "addHeaders");
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
