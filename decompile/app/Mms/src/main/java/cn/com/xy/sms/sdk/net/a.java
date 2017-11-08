package cn.com.xy.sms.sdk.net;

import android.os.Build;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import cn.com.xy.sms.sdk.Iservice.OnlineParseInterface;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.net.util.m;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.sdk.util.f;
import cn.com.xy.sms.sdk.util.u;
import cn.com.xy.sms.util.w;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/* compiled from: Unknown */
public class a implements Runnable {
    private static String a = null;
    public static int readTimeout = 90000;
    public static int timeoutConnection = 40000;
    public XyCallBack callBack;
    public String cmd;
    public String content;
    public c hhd;
    public boolean isCompress = false;
    public boolean isLogin = false;
    public String url;

    public a(String str, c cVar, String str2, boolean z, XyCallBack xyCallBack) {
        init(str, cVar, str2, z, null, xyCallBack, false);
    }

    public a(String str, c cVar, String str2, boolean z, String str3, XyCallBack xyCallBack) {
        init(str, cVar, str2, z, str3, xyCallBack, false);
    }

    public a(String str, c cVar, String str2, boolean z, String str3, XyCallBack xyCallBack, boolean z2) {
        init(str, cVar, str2, z, str3, xyCallBack, z2);
    }

    static String a() {
        String str = "";
        if (VERSION.SDK_INT < 21) {
            return Build.CPU_ABI;
        }
        String[] strArr = Build.SUPPORTED_ABIS;
        return strArr.length > 0 ? strArr[0] : str;
    }

    public static String getDeviceId(boolean z) {
        try {
            if (a == null) {
                a = ((TelephonyManager) Constant.getContext().getSystemService("phone")).getDeviceId();
            }
            return !StringUtils.isNull(a) ? !z ? a : m.a(a) : "";
        } catch (Throwable th) {
            return "";
        }
    }

    protected static boolean isAppChannel() {
        if (StringUtils.isNull(l.b)) {
            return false;
        }
        try {
            OnlineParseInterface onlineParseImpl = DexUtil.getOnlineParseImpl(false);
            if (onlineParseImpl != null) {
                return onlineParseImpl.isAppChannel(l.b);
            }
        } catch (Throwable th) {
        }
        return true;
    }

    public static void logNetInfo(String str, int i) {
        try {
            if (DuoquUtils.getLogSdkDoAction() != null) {
                new StringBuilder("length=").append(i).append(" req=").append(str);
            }
        } catch (Throwable th) {
        }
    }

    protected void addHeadSign(HttpURLConnection httpURLConnection) {
        try {
            httpURLConnection.addRequestProperty("x", j.b());
            if (isAppChannel()) {
                if (this.url.endsWith("token/")) {
                    byte[] a = cn.com.xy.sms.sdk.net.util.a.a(getDeviceId(false).getBytes(), XyUtil.getXyValue().getBytes());
                    StringBuffer stringBuffer = new StringBuffer();
                    for (byte b : a) {
                        String toHexString = Integer.toHexString(b & 255);
                        if (toHexString.length() == 1) {
                            toHexString = "0" + toHexString;
                        }
                        stringBuffer.append(toHexString);
                    }
                    httpURLConnection.addRequestProperty("s", stringBuffer.toString());
                    return;
                }
                String deviceId = getDeviceId(true);
                if (StringUtils.isNull(deviceId)) {
                    deviceId = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.UNIQUE_CODE);
                    if (deviceId == null) {
                        deviceId = "";
                    }
                }
                httpURLConnection.addRequestProperty("p", deviceId);
            }
        } catch (Throwable th) {
            DexUtil.saveExceptionLog(th);
        }
    }

    public void callBack(int i, String str) {
        new StringBuilder("STATUS: ").append(i).append(" responseStr: ").append(str);
        if (this.callBack != null) {
            this.callBack.execute(Integer.valueOf(i), str);
        }
    }

    public void callBack(String str, String str2) {
        if (this.callBack != null) {
            this.callBack.execute(str, str2);
        }
    }

    public HttpURLConnection getHttpURLConnection() {
        try {
            KeyManager.initAppKey();
            HttpURLConnection b = (this.url.startsWith("https") || this.url.startsWith("HTTPS")) ? b.b(this.url) : (HttpURLConnection) new URL(this.url).openConnection();
            b.setConnectTimeout(timeoutConnection);
            b.setReadTimeout(readTimeout);
            b.setDoInput(true);
            b.setDoOutput(true);
            b.setRequestMethod("POST");
            b.setUseCaches(false);
            b.setInstanceFollowRedirects(true);
            setHttpHeader(this.hhd, this.isLogin, this.cmd, b);
            if (this.isCompress) {
                b.addRequestProperty("nz", "1");
            }
            b.addRequestProperty("encrypt", "2");
            return b;
        } catch (Throwable th) {
            return null;
        }
    }

    protected byte[] getRequestByteArray(byte[] bArr) {
        byte[] bytes = this.content.getBytes("UTF-8");
        if (this.isCompress) {
            bytes = StringUtils.compressGZip(bytes);
        }
        return cn.com.xy.sms.sdk.net.util.a.a(bytes, bArr);
    }

    protected byte[] getResponseByteArray(byte[] bArr, byte[] bArr2) {
        byte[] bArr3 = null;
        try {
            bArr3 = cn.com.xy.sms.sdk.net.util.a.b(bArr, bArr2);
        } catch (Throwable th) {
        }
        if (this.isCompress) {
            try {
                bArr3 = StringUtils.uncompressGZip(bArr3 != null ? bArr3 : bArr);
            } catch (Throwable th2) {
            }
        }
        return bArr3 != null ? bArr3 : bArr;
    }

    public void init(String str, c cVar, String str2, boolean z, String str3, XyCallBack xyCallBack, boolean z2) {
        this.hhd = cVar;
        this.url = str;
        this.content = str2;
        this.callBack = xyCallBack;
        this.isLogin = z;
        this.cmd = str3;
        this.isCompress = z2;
    }

    public void run() {
        OutputStream outputStream;
        Throwable th;
        Object obj = null;
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection;
        try {
            httpURLConnection = getHttpURLConnection();
            if (httpURLConnection != null) {
                try {
                    httpURLConnection.connect();
                    outputStream = httpURLConnection.getOutputStream();
                    if (outputStream != null) {
                        try {
                            byte[] bytes = XyUtil.getXyValue().getBytes();
                            outputStream.write(getRequestByteArray(bytes));
                            outputStream.flush();
                            int responseCode = httpURLConnection.getResponseCode();
                            if (responseCode != SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) {
                                callBack(-8, "code=" + responseCode);
                            } else {
                                inputStream = httpURLConnection.getInputStream();
                                byte[] b = f.b(inputStream);
                                int length = b.length;
                                logNetInfo(this.content, length);
                                if (((long) length) <= Constant.NET_MAX_SIZE) {
                                    obj = 1;
                                }
                                if (obj == null) {
                                    callBack(-9, "len > Constant.NET_MAX_SIZE");
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (Throwable th2) {
                                        }
                                    }
                                    if (outputStream != null) {
                                        outputStream.close();
                                    }
                                    if (httpURLConnection != null) {
                                        httpURLConnection.disconnect();
                                        return;
                                    }
                                    return;
                                } else if (length != 0) {
                                    callBack(0, new String(getResponseByteArray(b, bytes), "UTF-8"));
                                } else {
                                    callBack(-5, "len == 0");
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (Throwable th3) {
                                        }
                                    }
                                    if (outputStream != null) {
                                        outputStream.close();
                                    }
                                    if (httpURLConnection != null) {
                                        httpURLConnection.disconnect();
                                        return;
                                    }
                                    return;
                                }
                            }
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (Throwable th4) {
                                }
                            }
                            if (outputStream != null) {
                                outputStream.close();
                            }
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                                return;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            try {
                                if (th.getClass() != SocketTimeoutException.class) {
                                    callBack(-7, th.getMessage());
                                } else {
                                    callBack(-6, th.getMessage());
                                }
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                                if (outputStream != null) {
                                    outputStream.close();
                                }
                                if (httpURLConnection != null) {
                                    try {
                                        httpURLConnection.disconnect();
                                    } catch (Throwable th6) {
                                        return;
                                    }
                                }
                            } catch (Throwable th7) {
                                th = th7;
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (Throwable th8) {
                                        throw th;
                                    }
                                }
                                if (outputStream != null) {
                                    outputStream.close();
                                }
                                if (httpURLConnection != null) {
                                    httpURLConnection.disconnect();
                                }
                                throw th;
                            }
                        }
                    }
                    callBack(-7, "http out null");
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (Throwable th9) {
                        }
                    }
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                        return;
                    }
                    return;
                } catch (Throwable th10) {
                    th = th10;
                    outputStream = null;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                    throw th;
                }
            }
            callBack(-7, "http null");
            if (httpURLConnection != null) {
                try {
                    httpURLConnection.disconnect();
                } catch (Throwable th11) {
                }
            }
        } catch (Throwable th12) {
            th = th12;
            outputStream = null;
            httpURLConnection = null;
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            throw th;
        }
    }

    public void setHttpHeader(c cVar, boolean z, String str, HttpURLConnection httpURLConnection) {
        if (httpURLConnection != null) {
            httpURLConnection.addRequestProperty("Content-Type", "text/xml;UTF-8");
            String a = m.a(l.a, l.b);
            httpURLConnection.addRequestProperty("app-key", l.b);
            httpURLConnection.addRequestProperty("app-key-sign", a);
            httpURLConnection.addRequestProperty("compress", "1");
            httpURLConnection.addRequestProperty("loginid", "");
            httpURLConnection.addRequestProperty("recordState", u.a());
            httpURLConnection.addRequestProperty("sdkversion", NetUtil.APPVERSION);
            if (z) {
                httpURLConnection.addRequestProperty("h-token", m.a("", l.b));
                httpURLConnection.addRequestProperty("command", "0");
            } else {
                httpURLConnection.addRequestProperty("command", "1");
            }
            if (!StringUtils.isNull(str)) {
                httpURLConnection.addRequestProperty("cmd", str);
            }
            httpURLConnection.addRequestProperty("abi", a());
            httpURLConnection.addRequestProperty("uiversion", DexUtil.getUIVersion());
            String d = w.d();
            String c = w.c();
            a = DexUtil.getOnLineConfigureData(4);
            if (StringUtils.isNull(a)) {
                a = "bizport.cn/66dc91e8b78b1c284027a3eb1be0a70e";
            }
            httpURLConnection.addRequestProperty("ai", m.a(d));
            httpURLConnection.addRequestProperty("ni", m.a(c));
            httpURLConnection.addRequestProperty("referer", a);
            addHeadSign(httpURLConnection);
        }
    }
}
