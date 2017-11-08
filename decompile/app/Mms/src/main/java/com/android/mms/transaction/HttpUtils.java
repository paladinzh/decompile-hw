package com.android.mms.transaction;

import android.content.Context;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.mms.MmsConfig;
import com.android.mms.ui.MessageListAdapter;
import com.android.mms.ui.MessageUtils;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ErrorMonitor;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.MmsRadarInfoManager;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class HttpUtils {
    private static final boolean IS_CMCC;
    private static final HwCustHttpUtils mHwCustHttpUtils = ((HwCustHttpUtils) HwCustUtils.createObj(HwCustHttpUtils.class, new Object[0]));
    private static boolean sDTTest = false;

    static {
        boolean equals;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("01")) {
            equals = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
        } else {
            equals = false;
        }
        IS_CMCC = equals;
    }

    public static boolean isInDTTest() {
        return sDTTest;
    }

    private static void reportSendStatus(Context context, boolean state, String uri, Exception exception) {
        Intent intent = new Intent("com.huawei.dtci.broadcast.mms.report");
        intent.putExtra("result", state);
        if (exception != null) {
            intent.putExtra("exception", exception.getClass().getName());
            intent.putExtra("exception_detail", exception);
        }
    }

    private HttpUtils() {
    }

    protected static void httpDisconnection(String uri) {
        AndroidHttpClient client = MessageListAdapter.getMmsTransactionCleintFromMap(uri);
        if (client != null) {
            client.close();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected static byte[] httpConnection(Context context, long token, String url, byte[] pdu, int method, boolean isProxySet, String proxyHost, int proxyPort) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("URL must not be null.");
        }
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("Mms_TXM_HTTP", "httpConnection: params list");
            String str = "Mms_TXM_HTTP";
            StringBuilder append = new StringBuilder().append("\tmethod\t\t= ");
            String str2 = method == 1 ? "POST" : method == 2 ? "GET" : "UNKNOWN";
            MLog.v(str, append.append(str2).toString());
            MLog.v("Mms_TXM_HTTP", "\tproxyHost\t= " + proxyHost);
            MLog.v("Mms_TXM_HTTP", "\tproxyPort\t= " + proxyPort);
        }
        AndroidHttpClient androidHttpClient = null;
        try {
            HttpRequest req;
            String str3;
            URI uri = new URI(url);
            HttpHost httpHost = new HttpHost(uri.getHost(), uri.getPort(), "http");
            androidHttpClient = createHttpClient(context);
            switch (method) {
                case 1:
                    ProgressCallbackEntity entity = new ProgressCallbackEntity(context, token, pdu);
                    entity.setContentType("application/vnd.wap.mms-message");
                    HttpPost httpPost = new HttpPost(url);
                    httpPost.setEntity(entity);
                    req = httpPost;
                    break;
                case 2:
                    HttpRequest httpGet = new HttpGet(url);
                    break;
                default:
                    MLog.e("Mms_TXM_HTTP", "Unknown HTTP method: " + method + ". Must be one of POST[" + 1 + "] or GET[" + 2 + "].");
                    if (androidHttpClient != null) {
                        androidHttpClient.close();
                    }
                    return null;
            }
            HttpParams params = androidHttpClient.getParams();
            if (isProxySet) {
                ConnRouteParams.setDefaultProxy(params, new HttpHost(proxyHost, proxyPort));
            } else {
                ConnRouteParams.setDefaultProxy(params, ConnRouteParams.NO_HOST);
            }
            req.setParams(params);
            req.addHeader("Accept", "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic");
            if (mHwCustHttpUtils != null) {
                mHwCustHttpUtils.addHeader(context, req, method);
            }
            String xWapProfileTagName = MmsConfig.getUaProfTagName();
            if (mHwCustHttpUtils == null) {
                str3 = null;
            } else {
                str3 = mHwCustHttpUtils.getChameleonUAprof(context);
            }
            if (str3 == null) {
                str3 = MmsConfig.getUaProfUrl();
            }
            if (str3 != null) {
                MLog.v("Mms_TXN", "Mms_TXM_HTTP", "[HttpUtils] httpConn: xWapProfUrl");
                req.addHeader(xWapProfileTagName, str3);
            }
            String extraHttpParams = MmsConfig.getHttpParams();
            if (extraHttpParams != null) {
                String line1Number = ((TelephonyManager) context.getSystemService("phone")).getLine1Number();
                String line1Key = MmsConfig.getHttpParamsLine1Key();
                for (String paramPair : extraHttpParams.split("\\|")) {
                    String[] splitPair = paramPair.split(":", 2);
                    if (splitPair.length == 2) {
                        String name = splitPair[0].trim();
                        String value = splitPair[1].trim();
                        if (!(line1Key == null || line1Number == null)) {
                            value = value.replace(line1Key, line1Number);
                        }
                        if (!(TextUtils.isEmpty(name) || TextUtils.isEmpty(value))) {
                            req.addHeader(name, value);
                        }
                    }
                }
            }
            req.addHeader("Accept-Language", getCurrentAcceptLanguage(Locale.getDefault()));
            HttpResponse response = androidHttpClient.execute(httpHost, req);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != 200) {
                MLog.e("Mms_TXM_HTTP", "HttpUtils response code error:" + status.getReasonPhrase());
                throw new IOException("HTTP error: " + status.getReasonPhrase());
            }
            HttpEntity entity2 = response.getEntity();
            byte[] bArr = null;
            if (entity2 == null) {
                if (androidHttpClient != null) {
                    androidHttpClient.close();
                }
                return null;
            }
            DataInputStream dis;
            try {
                if (entity2.getContentLength() > 0) {
                    MLog.v("Mms_TXM_HTTP", "httpConnection: transfer entity.getContentLength() > 0");
                    bArr = new byte[((int) entity2.getContentLength())];
                    dis = new DataInputStream(entity2.getContent());
                    dis.readFully(bArr);
                    dis.close();
                }
            } catch (IOException e) {
                MLog.e("Mms_TXM_HTTP", "Error closing input stream: " + e.getMessage());
            } catch (Throwable th) {
                entity2.consumeContent();
            }
            if (entity2.isChunked()) {
                MLog.v("Mms_TXM_HTTP", "httpConnection: transfer encoding is chunked");
                int bytesTobeRead = MmsConfig.getMaxMessageSize();
                byte[] tempBody = new byte[bytesTobeRead];
                dis = new DataInputStream(entity2.getContent());
                int bytesRead = 0;
                int offset = 0;
                boolean readError = false;
                do {
                    try {
                        bytesRead = dis.read(tempBody, offset, bytesTobeRead);
                        if (bytesRead > 0) {
                            bytesTobeRead -= bytesRead;
                            offset += bytesRead;
                        }
                        if (bytesRead >= 0) {
                        }
                    } catch (IOException e2) {
                        readError = true;
                        MLog.e("Mms_TXM_HTTP", "httpConnection: error reading input stream" + e2.getMessage());
                    } catch (Throwable th2) {
                        try {
                            dis.close();
                        } catch (IOException e22) {
                            MLog.e("Mms_TXM_HTTP", "Error closing input stream: " + e22.getMessage());
                        }
                    }
                    if (bytesRead == -1 || readError || offset <= 0) {
                        MLog.e("Mms_TXM_HTTP", "httpConnection: Response entity too large or empty");
                    } else {
                        bArr = new byte[offset];
                        System.arraycopy(tempBody, 0, bArr, 0, offset);
                        MLog.v("Mms_TXM_HTTP", "httpConnection: Chunked response length [" + Integer.toString(offset) + "]");
                    }
                    dis.close();
                } while (bytesTobeRead > 0);
                if (bytesRead == -1) {
                }
                MLog.e("Mms_TXM_HTTP", "httpConnection: Response entity too large or empty");
                try {
                    dis.close();
                } catch (IOException e222) {
                    MLog.e("Mms_TXM_HTTP", "Error closing input stream: " + e222.getMessage());
                }
            }
            entity2.consumeContent();
            if (isInDTTest()) {
                reportSendStatus(context, true, url, null);
            }
            if (androidHttpClient != null) {
                androidHttpClient.close();
            }
            return bArr;
        } catch (URISyntaxException e3) {
            handleHttpConnectionException(context, e3, url);
            if (androidHttpClient != null) {
                androidHttpClient.close();
            }
        } catch (IllegalStateException e4) {
            handleHttpConnectionException(context, e4, url);
            if (androidHttpClient != null) {
                androidHttpClient.close();
            }
        } catch (IllegalArgumentException e5) {
            handleHttpConnectionException(context, e5, url);
            if (androidHttpClient != null) {
                androidHttpClient.close();
            }
        } catch (SocketException e6) {
            handleHttpConnectionException(context, e6, url);
            if (androidHttpClient != null) {
                androidHttpClient.close();
            }
        } catch (Exception e7) {
            handleHttpConnectionException(context, e7, url);
            if (androidHttpClient != null) {
                androidHttpClient.close();
            }
        } catch (Throwable th3) {
            if (androidHttpClient != null) {
                androidHttpClient.close();
            }
        }
    }

    protected static byte[] httpConnection(Context context, long token, String url, byte[] pdu, int method, boolean isProxySet, String proxyHost, int proxyPort, String uri) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("URL must not be null.");
        }
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("Mms_TXM_HTTP", "httpConnection: params list");
            MLog.v("Mms_TXM_HTTP", "\tproxyHost\t= " + proxyHost);
            MLog.v("Mms_TXM_HTTP", "\tproxyPort\t= " + proxyPort);
            String str = "Mms_TXM_HTTP";
            StringBuilder append = new StringBuilder().append("\tmethod\t\t= ");
            String str2 = method == 1 ? "POST" : method == 2 ? "GET" : "UNKNOWN";
            MLog.v(str, append.append(str2).toString());
        }
        AndroidHttpClient androidHttpClient = null;
        boolean z = false;
        MmsConnectionManager mmsConnectionManager = MessageListAdapter.getConnectionManagerFromMap(uri);
        try {
            URI uri2 = new URI(url);
            HttpHost httpHost = new HttpHost(uri2.getHost(), uri2.getPort(), "http");
            androidHttpClient = createHttpClient(context);
            if (mmsConnectionManager != null) {
                if (!MessageListAdapter.getDownloadingStatusFromMap(uri)) {
                    return null;
                }
                z = MessageListAdapter.getManualDownloadFromMap(uri);
            }
            MessageListAdapter.saveConnectionManagerToMap(uri, MessageListAdapter.getUserStopTransaction(uri), true, z, androidHttpClient);
            switch (method) {
                case 1:
                    ProgressCallbackEntity progressCallbackEntity = new ProgressCallbackEntity(context, token, pdu);
                    progressCallbackEntity.setContentType("application/vnd.wap.mms-message");
                    HttpPost httpPost = new HttpPost(url);
                    httpPost.setEntity(progressCallbackEntity);
                    Object req = httpPost;
                    break;
                case 2:
                    HttpRequest httpGet = new HttpGet(url);
                    break;
                default:
                    MLog.e("Mms_TXM_HTTP", "Unknown HTTP method: " + method + ". Must be one of POST[" + 1 + "] or GET[" + 2 + "].");
                    if (androidHttpClient != null) {
                        androidHttpClient.close();
                    }
                    MessageListAdapter.saveConnectionManagerToMap(uri, MessageListAdapter.getUserStopTransaction(uri), MessageListAdapter.getDownloadingStatusFromMap(uri), MessageListAdapter.getManualDownloadFromMap(uri), null);
                    return null;
            }
            HttpParams params = androidHttpClient.getParams();
            if (isProxySet) {
                ConnRouteParams.setDefaultProxy(params, new HttpHost(proxyHost, proxyPort));
            } else {
                ConnRouteParams.setDefaultProxy(params, ConnRouteParams.NO_HOST);
            }
            req.setParams(params);
            req.addHeader("Accept", "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic");
            mHwCustHttpUtils.addHeader(context, req, method);
            String xWapProfileTagName = MmsConfig.getUaProfTagName();
            String xWapProfileUrl = mHwCustHttpUtils.getChameleonUAprof(context);
            if (xWapProfileUrl == null) {
                xWapProfileUrl = MmsConfig.getUaProfUrl();
            }
            if (xWapProfileUrl != null) {
                if (MLog.isLoggable("Mms_TXN", 2)) {
                    MLog.d("Mms_TXM_HTTP", "[HttpUtils] httpConn: xWapProfUrl=" + xWapProfileUrl);
                }
                req.addHeader(xWapProfileTagName, xWapProfileUrl);
            }
            String extraHttpParams = MmsConfig.getHttpParams();
            if (extraHttpParams != null) {
                String line1Number = ((TelephonyManager) context.getSystemService("phone")).getLine1Number();
                String line1Key = MmsConfig.getHttpParamsLine1Key();
                for (String paramPair : extraHttpParams.split("\\|")) {
                    String[] splitPair = paramPair.split(":", 2);
                    if (splitPair.length == 2) {
                        String name = splitPair[0].trim();
                        String value = splitPair[1].trim();
                        if (!(line1Key == null || line1Number == null)) {
                            value = value.replace(line1Key, line1Number);
                        }
                        if (!(TextUtils.isEmpty(name) || TextUtils.isEmpty(value))) {
                            req.addHeader(name, value);
                        }
                    }
                }
            }
            req.addHeader("Accept-Language", getCurrentAcceptLanguage(Locale.getDefault()));
            HttpResponse response = androidHttpClient.execute(httpHost, req);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != 200) {
                throw new IOException("HTTP error: " + status.getReasonPhrase());
            }
            HttpEntity entity = response.getEntity();
            byte[] bArr = null;
            if (entity != null) {
                DataInputStream dis;
                try {
                    if (entity.getContentLength() > 0) {
                        MLog.v("Mms_TXM_HTTP", "httpConnection: transfer entity.getContentLength() > 0");
                        dis = new DataInputStream(entity.getContent());
                        bArr = new byte[((int) entity.getContentLength())];
                        dis.readFully(bArr);
                        dis.close();
                    }
                } catch (IOException e) {
                    MLog.e("Mms_TXM_HTTP", "Error closing input stream: " + e.getMessage());
                } catch (Throwable th) {
                    entity.consumeContent();
                }
                if (entity.isChunked()) {
                    MLog.v("Mms_TXM_HTTP", "httpConnection: transfer encoding is chunked");
                    dis = new DataInputStream(entity.getContent());
                    int bytesTobeRead = MmsConfig.getMaxMessageSize();
                    byte[] tempBody = new byte[bytesTobeRead];
                    int offset = 0;
                    int bytesRead = 0;
                    boolean readError = false;
                    while (true) {
                        try {
                            bytesRead = dis.read(tempBody, offset, bytesTobeRead);
                            if (bytesRead > 0) {
                                bytesTobeRead -= bytesRead;
                                offset += bytesRead;
                            }
                            if (bytesRead >= 0) {
                                if (bytesTobeRead <= 0) {
                                }
                            }
                        } catch (IOException e2) {
                            readError = true;
                            MLog.e("Mms_TXM_HTTP", "httpConnection: error reading input stream" + e2.getMessage());
                        } catch (Throwable th2) {
                            try {
                                dis.close();
                            } catch (IOException e22) {
                                MLog.e("Mms_TXM_HTTP", "IOException when closing input stream: " + e22.getMessage());
                            }
                        }
                        if (bytesRead != -1 || offset <= 0 || readError) {
                            ErrorMonitor.reportRadar(907000022, "httpConnection: Response entity too large or empty");
                        } else {
                            bArr = new byte[offset];
                            System.arraycopy(tempBody, 0, bArr, 0, offset);
                            MLog.v("Mms_TXM_HTTP", "httpConnection: Chunked response length [" + Integer.toString(offset) + "]");
                        }
                        try {
                            dis.close();
                        } catch (IOException e222) {
                            MLog.e("Mms_TXM_HTTP", "IOException when closing input stream: " + e222.getMessage());
                        }
                    }
                }
                entity.consumeContent();
                if (isInDTTest()) {
                    reportSendStatus(context, true, url, null);
                }
            }
            if (androidHttpClient != null) {
                androidHttpClient.close();
            }
            MessageListAdapter.saveConnectionManagerToMap(uri, MessageListAdapter.getUserStopTransaction(uri), MessageListAdapter.getDownloadingStatusFromMap(uri), MessageListAdapter.getManualDownloadFromMap(uri), null);
            return bArr;
        } catch (SocketException e3) {
            handleHttpConnectionException(context, e3, url);
            return null;
        } catch (IllegalStateException e4) {
            handleHttpConnectionException(context, e4, url);
            return null;
        } catch (IllegalArgumentException e5) {
            handleHttpConnectionException(context, e5, url);
            return null;
        } catch (Exception e6) {
            handleHttpConnectionException(context, e6, url);
            return null;
        } catch (Exception e7) {
            handleHttpConnectionException(context, e7, url);
            return null;
        } finally {
            if (androidHttpClient != null) {
                androidHttpClient.close();
            }
            MessageListAdapter.saveConnectionManagerToMap(uri, MessageListAdapter.getUserStopTransaction(uri), MessageListAdapter.getDownloadingStatusFromMap(uri), MessageListAdapter.getManualDownloadFromMap(uri), null);
        }
    }

    private static void handleHttpConnectionException(Context context, Exception exception, String url) throws IOException {
        if (isInDTTest()) {
            reportSendStatus(context, false, url, exception);
        }
        String errorMsg = exception == null ? "" : exception.getMessage();
        MmsRadarInfoManager.getInstance().writeLogMsg(1330, errorMsg);
        MLog.e("Mms_TXM_HTTP", "Url: " + url + "\n" + errorMsg);
        IOException e = new IOException(errorMsg);
        e.initCause(exception);
        throw e;
    }

    private static AndroidHttpClient createHttpClient(Context context) {
        String userAgent;
        if (IS_CMCC) {
            userAgent = getUserAgent();
        } else if (MmsConfig.getEnableMmsCustomizedUA()) {
            userAgent = setCustomizedUserAgent(Build.DISPLAY, MmsConfig.getUserAgent());
        } else {
            userAgent = catUserAgent(Build.MANUFACTURER, Build.MODEL, MmsConfig.getUserAgent());
        }
        userAgent = mHwCustHttpUtils.getCustomUserAgent(context, userAgent);
        AndroidHttpClient client = AndroidHttpClient.newInstance(userAgent, context);
        HttpParams params = client.getParams();
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        int soTimeout = MmsConfig.getHttpSocketTimeout();
        int subId = MessageUtils.getPreferredDataSubscription();
        MLog.v("Mms_TXM_HTTP", "getSubInUse subId = " + subId);
        int dataNetworkType = TelephonyManager.getDefault().getDataNetworkType(subId);
        MLog.v("Mms_TXM_HTTP", "getDataNetworkType -- dataNetworkType = " + dataNetworkType);
        if (dataNetworkType == 1) {
            params.setIntParameter("http.socket.send-buffer", 8760);
        } else if (dataNetworkType == 2) {
            params.setIntParameter("http.socket.send-buffer", 16384);
        } else if (dataNetworkType == 7) {
            params.setIntParameter("http.socket.send-buffer", 16384);
        } else if (dataNetworkType == 3) {
            params.setIntParameter("http.socket.send-buffer", 349525);
        } else if (dataNetworkType == 5 || dataNetworkType == 6 || dataNetworkType == 12) {
            params.setIntParameter("http.socket.send-buffer", 16384);
        } else if (dataNetworkType == 14) {
            params.setIntParameter("http.socket.send-buffer", 16384);
        } else if (dataNetworkType == 8) {
            params.setIntParameter("http.socket.send-buffer", 52429);
        } else if (dataNetworkType == 10) {
            params.setIntParameter("http.socket.send-buffer", 100663);
        } else if (dataNetworkType == 15) {
            params.setIntParameter("http.socket.send-buffer", 192239);
        }
        if (MLog.isLoggable("Mms_TXN", 3)) {
            MLog.d("Mms_TXM_HTTP", "[HttpUtils] createHttpClient w/ socket timeout " + soTimeout + " ms, " + ", UA=" + userAgent);
        }
        HttpConnectionParams.setSoTimeout(params, soTimeout);
        return client;
    }

    private static String getUserAgent() {
        StringBuilder sb = new StringBuilder();
        sb.append("HUAWEI_").append(Build.PRODUCT).append("/1.0 ");
        sb.append("Android/").append(VERSION.RELEASE).append(" (Linux; U; Android ").append(VERSION.RELEASE).append("; zh-cn) ");
        sb.append("Release/03.20.2013 ");
        sb.append("Browser/WAP2.0 (AppleWebKit/537.36)");
        return sb.toString();
    }

    private static String catUserAgent(String manufacturer, String model, String config) {
        boolean z = true;
        String SLASH = "/";
        String HYPHEN = "-";
        String lc_manufacturer = null;
        String lc_model = null;
        String userAgent = config;
        String CAT = "/";
        if (config == null || config.isEmpty()) {
            return "Android-Mms/2.0";
        }
        boolean z2;
        if (manufacturer != null) {
            lc_manufacturer = manufacturer.toLowerCase(Locale.getDefault()).trim();
        }
        if (model != null) {
            lc_model = model.toLowerCase().trim();
        }
        String lc_ua = config.toLowerCase().trim();
        if (lc_model == null || lc_model.isEmpty() || lc_ua.startsWith("huawei")) {
            z2 = true;
        } else {
            z2 = lc_ua.startsWith(lc_model);
        }
        if (!z2) {
            userAgent = model + CAT + config;
            CAT = "-";
        }
        lc_ua = userAgent.toLowerCase().trim();
        if (!(lc_manufacturer == null || lc_manufacturer.isEmpty() || lc_ua.startsWith("huawei"))) {
            z = lc_ua.startsWith(lc_manufacturer);
        }
        if (!z) {
            userAgent = manufacturer + CAT + userAgent;
            CAT = "-";
        }
        return userAgent.replace(' ', '-');
    }

    public static String getCurrentAcceptLanguage(Locale locale) {
        StringBuilder buffer = new StringBuilder();
        addLocaleToHttpAcceptLanguage(buffer, locale);
        if (!Locale.US.equals(locale)) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append("en-US");
            if (mHwCustHttpUtils != null) {
                mHwCustHttpUtils.checkHttpHeaderUseCurrentLocale(buffer);
            }
        }
        return buffer.toString();
    }

    private static String convertObsoleteLanguageCodeToNew(String langCode) {
        if (langCode == null) {
            return null;
        }
        if ("iw".equals(langCode)) {
            return "he";
        }
        if ("in".equals(langCode)) {
            return "id";
        }
        if ("ji".equals(langCode)) {
            return "yi";
        }
        return langCode;
    }

    private static void addLocaleToHttpAcceptLanguage(StringBuilder builder, Locale locale) {
        String language = convertObsoleteLanguageCodeToNew(locale.getLanguage());
        if (language != null) {
            builder.append(language);
            String country = locale.getCountry();
            if (country != null) {
                builder.append("-");
                builder.append(country);
            }
        }
    }

    private static String setCustomizedUserAgent(String displayID, String config) {
        if (config == null || config.isEmpty()) {
            return "Android-Mms/2.0";
        }
        String userAgent = config;
        if (displayID == null || displayID.isEmpty()) {
            return userAgent;
        }
        return config.replace("softwareVersion", displayID);
    }
}
