package com.loc;

import android.os.Build.VERSION;
import com.amap.api.maps.AMapException;
import com.google.android.gms.location.places.Place;
import com.loc.bp.a;
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
public class bq {
    private static br a;
    private int b;
    private int c;
    private boolean d;
    private SSLContext e;
    private Proxy f;
    private volatile boolean g;
    private long h;
    private long i;
    private HostnameVerifier j;

    bq(int i, int i2, Proxy proxy) {
        this(i, i2, proxy, false);
    }

    bq(int i, int i2, Proxy proxy, boolean z) {
        this.g = false;
        this.h = -1;
        this.i = 0;
        this.j = new bu(this);
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
                aa.a(e, "HttpUrlUtil", "HttpUrlUtil");
            } catch (Throwable e2) {
                aa.a(e2, "HttpUtil", "HttpUtil");
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private bt a(HttpURLConnection httpURLConnection) throws l, IOException {
        ByteArrayOutputStream byteArrayOutputStream;
        IOException e;
        Throwable th;
        PushbackInputStream pushbackInputStream;
        InputStream inputStream = null;
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
                            bt btVar;
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
                                    btVar = new bt();
                                    btVar.a = byteArrayOutputStream.toByteArray();
                                    btVar.b = headerFields;
                                    if (byteArrayOutputStream != null) {
                                        try {
                                            byteArrayOutputStream.close();
                                        } catch (Throwable e2) {
                                            aa.a(e2, "HttpUrlUtil", "parseResult");
                                            e2.printStackTrace();
                                        }
                                    }
                                    if (inputStream2 != null) {
                                        try {
                                            inputStream2.close();
                                        } catch (Throwable e3) {
                                            aa.a(e3, "HttpUrlUtil", "parseResult");
                                            e3.printStackTrace();
                                        }
                                    }
                                    if (pushbackInputStream2 != null) {
                                        try {
                                            pushbackInputStream2.close();
                                        } catch (Throwable e4) {
                                            aa.a(e4, "HttpUrlUtil", "parseResult");
                                            e4.printStackTrace();
                                        }
                                    }
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (Throwable e42) {
                                            aa.a(e42, "HttpUrlUtil", "parseResult");
                                            e42.printStackTrace();
                                        }
                                    }
                                    if (httpURLConnection != null) {
                                        try {
                                            httpURLConnection.disconnect();
                                        } catch (Throwable e422) {
                                            aa.a(e422, "HttpUrlUtil", "parseResult");
                                            e422.printStackTrace();
                                        }
                                    }
                                    return btVar;
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
                            btVar = new bt();
                            btVar.a = byteArrayOutputStream.toByteArray();
                            btVar.b = headerFields;
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
                            return btVar;
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
                                aa.a(e22, "HttpUrlUtil", "parseResult");
                                e22.printStackTrace();
                            }
                        }
                        if (inputStream2 != null) {
                            try {
                                inputStream2.close();
                            } catch (Throwable e32) {
                                aa.a(e32, "HttpUrlUtil", "parseResult");
                                e32.printStackTrace();
                            }
                        }
                        if (pushbackInputStream != null) {
                            try {
                                pushbackInputStream.close();
                            } catch (Throwable e7) {
                                aa.a(e7, "HttpUrlUtil", "parseResult");
                                e7.printStackTrace();
                            }
                        }
                        if (pushbackInputStream2 != null) {
                            try {
                                pushbackInputStream2.close();
                            } catch (Throwable e4222) {
                                aa.a(e4222, "HttpUrlUtil", "parseResult");
                                e4222.printStackTrace();
                            }
                        }
                        if (httpURLConnection != null) {
                            try {
                                httpURLConnection.disconnect();
                            } catch (Throwable e42222) {
                                aa.a(e42222, "HttpUrlUtil", "parseResult");
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
            throw new l("网络异常原因：" + httpURLConnection.getResponseMessage() + " 网络异常状态码：" + responseCode);
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

    public static void a(br brVar) {
        a = brVar;
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
            aa.a(th, "HttpUrlUtil", "addHeaders");
        }
        httpURLConnection.setConnectTimeout(this.b);
        httpURLConnection.setReadTimeout(this.c);
    }

    bt a(String str, Map<String, String> map, byte[] bArr) throws l {
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
            throw new l(AMapException.ERROR_CONNECTION);
        } catch (MalformedURLException e2) {
            e2.printStackTrace();
            throw new l("url异常 - MalformedURLException");
        } catch (UnknownHostException e3) {
            e3.printStackTrace();
            throw new l("未知主机 - UnKnowHostException");
        } catch (SocketException e4) {
            e4.printStackTrace();
            throw new l(AMapException.ERROR_SOCKET);
        } catch (SocketTimeoutException e5) {
            e5.printStackTrace();
            throw new l("socket 连接超时 - SocketTimeoutException");
        } catch (IOException e6) {
            e6.printStackTrace();
            throw new l("IO 操作异常 - IOException");
        } catch (Throwable th) {
            aa.a(th, "HttpUrlUtil", "makePostReqeust");
            l lVar = new l(AMapException.ERROR_UNKNOWN);
        }
    }

    HttpURLConnection a(String str, Map<String, String> map, boolean z) throws IOException {
        q.a();
        URL url = new URL(str);
        HttpURLConnection openConnection = this.f == null ? (HttpURLConnection) url.openConnection() : url.openConnection(this.f);
        if (this.d) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) openConnection;
            httpsURLConnection.setSSLSocketFactory(this.e.getSocketFactory());
            httpsURLConnection.setHostnameVerifier(this.j);
        } else {
            openConnection = openConnection;
        }
        if (VERSION.SDK != null && VERSION.SDK_INT > 13) {
            openConnection.setRequestProperty("Connection", "close");
        }
        a(map, openConnection);
        if (z) {
            openConnection.setRequestMethod("POST");
            openConnection.setUseCaches(false);
            openConnection.setDoInput(true);
            openConnection.setDoOutput(true);
        } else {
            openConnection.setRequestMethod("GET");
            openConnection.setDoInput(true);
        }
        return openConnection;
    }

    void a(long j) {
        this.i = j;
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
                    aVar.a(new l("网络异常原因：" + httpURLConnection.getResponseMessage() + " 网络异常状态码：" + responseCode));
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
                    aVar.b();
                } else {
                    aVar.c();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable e) {
                        e.printStackTrace();
                        aa.a(e, "HttpUrlUtil", "makeDownloadGetRequest");
                    } catch (Throwable e2) {
                        e2.printStackTrace();
                        aa.a(e2, "HttpUrlUtil", "makeDownloadGetRequest");
                    }
                }
                if (httpURLConnection != null) {
                    try {
                        httpURLConnection.disconnect();
                    } catch (Throwable e22) {
                        e22.printStackTrace();
                        aa.a(e22, "HttpUrlUtil", "makeDownloadGetRequest");
                    }
                }
            } catch (Throwable e222) {
                aVar.a(e222);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable e2222) {
                        e2222.printStackTrace();
                        aa.a(e2222, "HttpUrlUtil", "makeDownloadGetRequest");
                    } catch (Throwable e22222) {
                        e22222.printStackTrace();
                        aa.a(e22222, "HttpUrlUtil", "makeDownloadGetRequest");
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
                        aa.a(e2222222, "HttpUrlUtil", "makeDownloadGetRequest");
                    } catch (Throwable e22222222) {
                        e22222222.printStackTrace();
                        aa.a(e22222222, "HttpUrlUtil", "makeDownloadGetRequest");
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
                        aa.a(e2222222222, "HttpUrlUtil", "makeDownloadGetRequest");
                    } catch (Throwable e22222222222) {
                        e22222222222.printStackTrace();
                        aa.a(e22222222222, "HttpUrlUtil", "makeDownloadGetRequest");
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
                        aa.a(e2222222222222, "HttpUrlUtil", "makeDownloadGetRequest");
                    } catch (Throwable e22222222222222) {
                        e22222222222222.printStackTrace();
                        aa.a(e22222222222222, "HttpUrlUtil", "makeDownloadGetRequest");
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
                        aa.a(e2222222222222222, "HttpUrlUtil", "makeDownloadGetRequest");
                    } catch (Throwable e22222222222222222) {
                        e22222222222222222.printStackTrace();
                        aa.a(e22222222222222222, "HttpUrlUtil", "makeDownloadGetRequest");
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
                        aa.a(e2222222222222222222, "HttpUrlUtil", "makeDownloadGetRequest");
                    } catch (Throwable e22222222222222222222) {
                        e22222222222222222222.printStackTrace();
                        aa.a(e22222222222222222222, "HttpUrlUtil", "makeDownloadGetRequest");
                    }
                }
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            } catch (Throwable e222222222222222222222) {
                e222222222222222222222.printStackTrace();
                aa.a(e222222222222222222222, "HttpUrlUtil", "makeDownloadGetRequest");
            }
        }
    }

    void b(long j) {
        this.h = j;
    }
}
