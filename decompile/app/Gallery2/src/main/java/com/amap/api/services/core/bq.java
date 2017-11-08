package com.amap.api.services.core;

import android.os.Build.VERSION;
import com.android.gallery3d.gadget.XmlUtils;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

/* compiled from: HttpUrlUtil */
public class bq {
    private static br a;
    private static TrustManager g = new bw();
    private int b;
    private int c;
    private boolean d;
    private SSLContext e;
    private Proxy f;

    public static void a(br brVar) {
        a = brVar;
    }

    bq(int i, int i2, Proxy proxy, boolean z) {
        this.b = i;
        this.c = i2;
        this.f = proxy;
        this.d = z;
        if (z) {
            try {
                SSLContext instance = SSLContext.getInstance("TLS");
                instance.init(null, new TrustManager[]{g}, null);
                this.e = instance;
            } catch (Throwable e) {
                ay.a(e, "HttpUrlUtil", "HttpUrlUtil");
                e.printStackTrace();
            } catch (Throwable e2) {
                ay.a(e2, "HttpUrlUtil", "HttpUrlUtil");
                e2.printStackTrace();
            } catch (Throwable e22) {
                ay.a(e22, "HttpUtil", "HttpUtil");
                e22.printStackTrace();
            }
        }
    }

    bv a(String str, Map<String, String> map, Map<String, String> map2) throws v {
        try {
            String a = a((Map) map2);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(str);
            if (a != null) {
                stringBuffer.append("?").append(a);
            }
            HttpURLConnection a2 = a(new URL(stringBuffer.toString()));
            a(map, a2);
            a2.setRequestMethod("GET");
            a2.setDoInput(true);
            a2.connect();
            return a(a2);
        } catch (Throwable e) {
            ay.a(e, "HttpUrlUtil", "getRequest");
            e.printStackTrace();
            return null;
        } catch (Throwable e2) {
            ay.a(e2, "HttpUrlUtil", "getRequest");
            e2.printStackTrace();
            return null;
        } catch (Throwable e22) {
            ay.a(e22, "HttpUrlUtil", "getRequest");
            e22.printStackTrace();
            return null;
        }
    }

    bv a(String str, Map<String, String> map, Map<String, String> map2, byte[] bArr) throws v {
        if (map2 != null) {
            String a = a((Map) map2);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(str);
            if (a != null) {
                stringBuffer.append("?").append(a);
            }
            str = stringBuffer.toString();
        }
        try {
            return a(str, (Map) map, bArr);
        } catch (Throwable th) {
            ay.a(th, "HttpUrlUtil", "PostReqeust3");
            th.printStackTrace();
            return null;
        }
    }

    bv a(String str, Map<String, String> map, byte[] bArr) throws v {
        try {
            HttpURLConnection a = a(new URL(str));
            a(map, a);
            a.setRequestMethod("POST");
            a.setUseCaches(false);
            a.setDoInput(true);
            a.setDoOutput(true);
            if (bArr != null && bArr.length > 0) {
                DataOutputStream dataOutputStream = new DataOutputStream(a.getOutputStream());
                dataOutputStream.write(bArr);
                dataOutputStream.close();
            }
            a.connect();
            return a(a);
        } catch (Throwable e) {
            ay.a(e, "HttpUrlUtil", "postRequest");
            e.printStackTrace();
            return null;
        } catch (Throwable e2) {
            ay.a(e2, "HttpUrlUtil", "postRequest");
            e2.printStackTrace();
            return null;
        } catch (Throwable e22) {
            ay.a(e22, "HttpUrlUtil", "postRequest");
            e22.printStackTrace();
            return null;
        }
    }

    bv b(String str, Map<String, String> map, Map<String, String> map2) throws v {
        String a = a((Map) map2);
        if (a == null) {
            return a(str, (Map) map, new byte[0]);
        }
        try {
            return a(str, (Map) map, a.getBytes(XmlUtils.INPUT_ENCODING));
        } catch (Throwable e) {
            ay.a(e, "HttpUrlUtil", "postRequest1");
            e.printStackTrace();
            return a(str, (Map) map, a.getBytes());
        }
    }

    bv a(String str, Map<String, String> map, Map<String, String> map2, HttpEntity httpEntity) throws v {
        ByteArrayOutputStream byteArrayOutputStream;
        InputStream content;
        Throwable e;
        if (map2 != null) {
            String a = a((Map) map2);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(str);
            if (a != null) {
                stringBuffer.append("?").append(a);
            }
            str = stringBuffer.toString();
        }
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                content = httpEntity.getContent();
                try {
                    byte[] bArr = new byte[1024];
                    while (true) {
                        int read = content.read(bArr);
                        if (read == -1) {
                            break;
                        }
                        byteArrayOutputStream.write(bArr, 0, read);
                    }
                    bv a2 = a(str, (Map) map, byteArrayOutputStream.toByteArray());
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable e2) {
                            ay.a(e2, "HttpUrlUtil", "postRequest2");
                            e2.printStackTrace();
                        }
                    }
                    if (content != null) {
                        try {
                            content.close();
                        } catch (Throwable e3) {
                            ay.a(e3, "HttpUrlUtil", "postRequest2");
                            e3.printStackTrace();
                        }
                    }
                    return a2;
                } catch (IllegalStateException e4) {
                    e = e4;
                } catch (IOException e5) {
                    e = e5;
                } catch (Throwable th) {
                    e = th;
                }
            } catch (IllegalStateException e6) {
                e = e6;
                content = null;
                try {
                    ay.a(e, "HttpUrlUtil", "postRequest2");
                    e.printStackTrace();
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable e7) {
                            ay.a(e7, "HttpUrlUtil", "postRequest2");
                            e7.printStackTrace();
                        }
                    }
                    if (content != null) {
                        try {
                            content.close();
                        } catch (Throwable e72) {
                            ay.a(e72, "HttpUrlUtil", "postRequest2");
                            e72.printStackTrace();
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    e72 = th2;
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (Throwable e22) {
                            ay.a(e22, "HttpUrlUtil", "postRequest2");
                            e22.printStackTrace();
                        }
                    }
                    if (content != null) {
                        try {
                            content.close();
                        } catch (Throwable e32) {
                            ay.a(e32, "HttpUrlUtil", "postRequest2");
                            e32.printStackTrace();
                        }
                    }
                    throw e72;
                }
            } catch (IOException e8) {
                e72 = e8;
                content = null;
                ay.a(e72, "HttpUrlUtil", "postRequest2");
                e72.printStackTrace();
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (Throwable e722) {
                        ay.a(e722, "HttpUrlUtil", "postRequest2");
                        e722.printStackTrace();
                    }
                }
                if (content != null) {
                    try {
                        content.close();
                    } catch (Throwable e7222) {
                        ay.a(e7222, "HttpUrlUtil", "postRequest2");
                        e7222.printStackTrace();
                    }
                }
                return null;
            } catch (Throwable th3) {
                e7222 = th3;
                content = null;
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (content != null) {
                    content.close();
                }
                throw e7222;
            }
        } catch (IllegalStateException e9) {
            e7222 = e9;
            content = null;
            byteArrayOutputStream = null;
            ay.a(e7222, "HttpUrlUtil", "postRequest2");
            e7222.printStackTrace();
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (content != null) {
                content.close();
            }
            return null;
        } catch (IOException e10) {
            e7222 = e10;
            content = null;
            byteArrayOutputStream = null;
            ay.a(e7222, "HttpUrlUtil", "postRequest2");
            e7222.printStackTrace();
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (content != null) {
                content.close();
            }
            return null;
        } catch (Throwable th4) {
            e7222 = th4;
            content = null;
            byteArrayOutputStream = null;
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (content != null) {
                content.close();
            }
            throw e7222;
        }
    }

    private void a(Map<String, String> map, HttpURLConnection httpURLConnection) {
        if (map != null) {
            for (String str : map.keySet()) {
                httpURLConnection.addRequestProperty(str, (String) map.get(str));
            }
        }
        httpURLConnection.setConnectTimeout(this.b);
        httpURLConnection.setReadTimeout(this.c);
    }

    private HttpURLConnection a(URL url) throws IOException {
        HttpURLConnection httpURLConnection;
        if (this.f == null) {
            httpURLConnection = (HttpURLConnection) url.openConnection();
        } else {
            httpURLConnection = url.openConnection(this.f);
        }
        if (this.d) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) httpURLConnection;
            httpsURLConnection.setSSLSocketFactory(this.e.getSocketFactory());
        } else {
            httpURLConnection = httpURLConnection;
        }
        if (VERSION.SDK != null && VERSION.SDK_INT > 13) {
            httpURLConnection.setRequestProperty("Connection", "close");
        }
        return httpURLConnection;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private bv a(HttpURLConnection httpURLConnection) throws v, IOException {
        InputStream inputStream;
        InputStream pushbackInputStream;
        IOException e;
        Throwable th;
        PushbackInputStream pushbackInputStream2;
        InputStream inputStream2 = null;
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            Map headerFields = httpURLConnection.getHeaderFields();
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == SmsCheckResult.ESCT_200) {
                byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    inputStream = httpURLConnection.getInputStream();
                    try {
                        pushbackInputStream = new PushbackInputStream(inputStream, 2);
                        try {
                            int read;
                            bv bvVar;
                            byte[] bArr = new byte[2];
                            pushbackInputStream.read(bArr);
                            pushbackInputStream.unread(bArr);
                            if (bArr[0] == (byte) 31) {
                                if (bArr[1] == (byte) -117) {
                                    inputStream2 = new GZIPInputStream(pushbackInputStream);
                                    bArr = new byte[1024];
                                    while (true) {
                                        read = inputStream2.read(bArr);
                                        if (read == -1) {
                                            break;
                                        }
                                        byteArrayOutputStream.write(bArr, 0, read);
                                    }
                                    if (a != null) {
                                        a.a();
                                    }
                                    bvVar = new bv();
                                    bvVar.a = byteArrayOutputStream.toByteArray();
                                    bvVar.b = headerFields;
                                    if (byteArrayOutputStream != null) {
                                        try {
                                            byteArrayOutputStream.close();
                                        } catch (Throwable e2) {
                                            ay.a(e2, "HttpUrlUtil", "parseResult");
                                            e2.printStackTrace();
                                        }
                                    }
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
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
                                    if (inputStream2 != null) {
                                        try {
                                            inputStream2.close();
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
                                    return bvVar;
                                }
                            }
                            inputStream2 = pushbackInputStream;
                            bArr = new byte[1024];
                            while (true) {
                                read = inputStream2.read(bArr);
                                if (read == -1) {
                                    break;
                                }
                                byteArrayOutputStream.write(bArr, 0, read);
                            }
                            if (a != null) {
                                a.a();
                            }
                            bvVar = new bv();
                            bvVar.a = byteArrayOutputStream.toByteArray();
                            bvVar.b = headerFields;
                            if (byteArrayOutputStream != null) {
                                byteArrayOutputStream.close();
                            }
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            if (pushbackInputStream != null) {
                                pushbackInputStream.close();
                            }
                            if (inputStream2 != null) {
                                inputStream2.close();
                            }
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                            }
                            return bvVar;
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
                            pushbackInputStream = inputStream2;
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
                        if (inputStream != null) {
                            try {
                                inputStream.close();
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
                    inputStream = null;
                    throw e;
                } catch (Throwable th4) {
                    th = th4;
                    pushbackInputStream = null;
                    inputStream = null;
                    if (byteArrayOutputStream != null) {
                        byteArrayOutputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
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
            throw new v("网络异常原因：" + httpURLConnection.getResponseMessage() + " 网络异常状态码：" + responseCode);
        } catch (IOException e9) {
            e = e9;
            pushbackInputStream = null;
            inputStream = null;
            byteArrayOutputStream = null;
            throw e;
        } catch (Throwable th5) {
            th = th5;
            pushbackInputStream = null;
            inputStream = null;
            byteArrayOutputStream = null;
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
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

    private String a(Map<String, String> map) {
        List linkedList = new LinkedList();
        if (map != null) {
            for (Entry entry : map.entrySet()) {
                linkedList.add(new BasicNameValuePair((String) entry.getKey(), (String) entry.getValue()));
            }
        }
        if (linkedList.size() <= 0) {
            return null;
        }
        return URLEncodedUtils.format(linkedList, XmlUtils.INPUT_ENCODING);
    }
}
