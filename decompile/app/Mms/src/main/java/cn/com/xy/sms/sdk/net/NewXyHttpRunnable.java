package cn.com.xy.sms.sdk.net;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.net.util.a;
import cn.com.xy.sms.sdk.net.util.b;
import cn.com.xy.sms.sdk.net.util.h;
import cn.com.xy.sms.sdk.net.util.m;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.f;
import cn.com.xy.sms.sdk.util.u;
import cn.com.xy.sms.util.w;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Map;
import javax.crypto.Cipher;
import javax.net.ssl.SSLHandshakeException;

/* compiled from: Unknown */
public class NewXyHttpRunnable extends a {
    public static final String ERROR_CODE_PARE_ERR = "2";
    public static final String ERROR_CODE_SERVICE_ERR = "3";
    public static final String ERROR_CODE_TOKEN_FAILED = "1";
    public static String RSA_PRV_KEY = null;
    private static String a = "HTTP";

    public NewXyHttpRunnable(String str, String str2, XyCallBack xyCallBack, boolean z, boolean z2, Map<String, String> map) {
        super(str, null, str2, z, "", xyCallBack, z2);
    }

    public void run() {
        HttpURLConnection httpURLConnection;
        Throwable th;
        Object obj = 1;
        InputStream inputStream = null;
        OutputStream outputStream;
        try {
            httpURLConnection = getHttpURLConnection();
            if (httpURLConnection != null) {
                try {
                    httpURLConnection.connect();
                    outputStream = httpURLConnection.getOutputStream();
                    if (outputStream != null) {
                        try {
                            int i;
                            byte[] doFinal;
                            String str;
                            byte[] bytes = !this.isCompress ? this.content.getBytes("utf-8") : StringUtils.compressGZip(this.content.getBytes("utf-8"));
                            if (this.isLogin) {
                                Key generatePrivate = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(b.a(RSA_PRV_KEY)));
                                Cipher instance = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                                instance.init(1, generatePrivate);
                                int length = bytes.length;
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                int i2 = 0;
                                i = 0;
                                while (length - i > 0) {
                                    doFinal = length - i <= 117 ? instance.doFinal(bytes, i, length - i) : instance.doFinal(bytes, i, 117);
                                    byteArrayOutputStream.write(doFinal, 0, doFinal.length);
                                    i2++;
                                    i = i2 * 117;
                                }
                                byte[] toByteArray = byteArrayOutputStream.toByteArray();
                                byteArrayOutputStream.close();
                                doFinal = toByteArray;
                                str = null;
                            } else {
                                str = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.AESKEY);
                                doFinal = a.a(bytes, h.a(str));
                            }
                            if (doFinal != null) {
                                outputStream.write(doFinal, 0, doFinal.length);
                            }
                            outputStream.flush();
                            i = httpURLConnection.getResponseCode();
                            if (i == SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE) {
                                String headerField = httpURLConnection.getHeaderField("XY-ERR-CODE");
                                if (StringUtils.isNull(headerField)) {
                                    inputStream = httpURLConnection.getInputStream();
                                    doFinal = f.b(inputStream);
                                    int length2 = doFinal.length;
                                    a.logNetInfo(this.content, length2);
                                    if (((long) length2) > Constant.NET_MAX_SIZE) {
                                        obj = null;
                                    }
                                    if (obj == null) {
                                        callBack(-9, "");
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
                                    } else if (length2 != 0) {
                                        byte[] b = !this.isLogin ? a.b(doFinal, h.a(str)) : h.b(doFinal, RSA_PRV_KEY);
                                        if (this.isCompress) {
                                            try {
                                                b = StringUtils.uncompressGZip(b);
                                            } catch (Throwable th3) {
                                            }
                                        }
                                        callBack(0, new String(b, "UTF-8"));
                                    } else {
                                        callBack(-5, "");
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
                                        return;
                                    }
                                }
                                callBack(headerField, "");
                                if (outputStream != null) {
                                    try {
                                        outputStream.close();
                                    } catch (Throwable th5) {
                                    }
                                }
                                if (httpURLConnection != null) {
                                    httpURLConnection.disconnect();
                                    return;
                                }
                                return;
                            } else if (i != 204) {
                                callBack(-8, "");
                            } else {
                                String headerField2 = httpURLConnection.getHeaderField("XY-ERR-CODE");
                                if (!StringUtils.isNull(headerField2)) {
                                    callBack(headerField2, "token refresh");
                                    if (outputStream != null) {
                                        try {
                                            outputStream.close();
                                        } catch (Throwable th6) {
                                        }
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
                                } catch (Throwable th7) {
                                }
                            }
                            if (outputStream != null) {
                                outputStream.close();
                            }
                            if (httpURLConnection != null) {
                                httpURLConnection.disconnect();
                                return;
                            }
                        } catch (Throwable th8) {
                            obj = th8;
                            try {
                                if (obj.getClass() != SocketTimeoutException.class) {
                                    callBack(-6, "");
                                } else if (obj.getClass() != SSLHandshakeException.class) {
                                    callBack(-7, "");
                                } else {
                                    callBack(-12, "");
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
                                    } catch (Throwable th9) {
                                        return;
                                    }
                                }
                            } catch (Throwable th10) {
                                th = th10;
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (Throwable th11) {
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
                        } catch (Throwable th12) {
                        }
                    }
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                        return;
                    }
                    return;
                } catch (Throwable th13) {
                    th = th13;
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
                } catch (Throwable th14) {
                }
            }
        } catch (Throwable th15) {
            th = th15;
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
        String str2 = l.b;
        String token = NetUtil.getToken();
        if (!StringUtils.isNull(token)) {
            str2 = new StringBuilder(String.valueOf(str2)).append(token).toString();
            httpURLConnection.addRequestProperty(NetUtil.REQ_QUERY_TOEKN, token);
        }
        str2 = m.a(l.a, str2);
        httpURLConnection.addRequestProperty("appkey", l.b);
        httpURLConnection.addRequestProperty("app-key-sign", str2);
        httpURLConnection.addRequestProperty("recordState", u.a());
        httpURLConnection.addRequestProperty("sdkversion", NetUtil.APPVERSION);
        addHeadSign(httpURLConnection);
        httpURLConnection.addRequestProperty("abi", a.a());
        httpURLConnection.addRequestProperty("uiversion", DexUtil.getUIVersion());
        token = w.d();
        String c = w.c();
        str2 = DexUtil.getOnLineConfigureData(4);
        if (StringUtils.isNull(str2)) {
            str2 = "bizport.cn/66dc91e8b78b1c284027a3eb1be0a70e";
        }
        httpURLConnection.addRequestProperty("ai", m.a(token));
        httpURLConnection.addRequestProperty("ni", m.a(c));
        httpURLConnection.addRequestProperty("referer", str2);
        httpURLConnection.addRequestProperty("Content-Type", "application/octet-stream");
        if (LogManager.debug) {
            httpURLConnection.getRequestProperties();
        }
    }
}
