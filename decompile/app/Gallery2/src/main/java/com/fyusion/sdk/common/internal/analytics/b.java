package com.fyusion.sdk.common.internal.analytics;

import android.util.Log;
import com.fyusion.sdk.common.a;
import com.huawei.gallery.app.AbsAlbumPage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

/* compiled from: Unknown */
public class b implements Runnable {
    private final e a;
    private final String b;
    private final int c;

    b(String str, e eVar, int i) {
        this.b = str;
        this.a = eVar;
        this.c = i;
    }

    URLConnection a(String str) throws IOException {
        Throwable th;
        Throwable th2 = null;
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(this.b + Fyulytics.sharedInstance().b() + "&key=" + a.g()).openConnection();
        httpURLConnection.setConnectTimeout(30000);
        httpURLConnection.setReadTimeout(30000);
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Encoding", "gzip");
        GZIPOutputStream gZIPOutputStream = new GZIPOutputStream(httpURLConnection.getOutputStream(), 1024);
        try {
            gZIPOutputStream.write(str.getBytes());
            gZIPOutputStream.flush();
            if (gZIPOutputStream != null) {
                gZIPOutputStream.close();
            }
            return httpURLConnection;
        } catch (Throwable th22) {
            Throwable th3 = th22;
            th22 = th;
            th = th3;
        }
        if (gZIPOutputStream != null) {
            if (th22 == null) {
                gZIPOutputStream.close();
            } else {
                try {
                    gZIPOutputStream.close();
                } catch (Throwable th4) {
                    th22.addSuppressed(th4);
                }
            }
        }
        throw th;
        throw th;
    }

    public void run() {
        Throwable th;
        Throwable th2;
        URLConnection uRLConnection;
        Throwable th3;
        URLConnection uRLConnection2;
        int i = 0;
        while (i < this.c) {
            String str;
            InputStream inputStream;
            List c = this.a.c();
            if (c != null && c.size() != 0) {
                int i2 = i + 1;
                str = (String) c.get(0);
                try {
                    URLConnection a = a(str);
                    try {
                        int responseCode;
                        int i3;
                        a.connect();
                        if (a instanceof HttpURLConnection) {
                            inputStream = a.getInputStream();
                            try {
                                responseCode = ((HttpURLConnection) a).getResponseCode();
                                i3 = (responseCode >= SmsCheckResult.ESCT_200 && responseCode < 300) ? 1 : 0;
                                if (i3 == 0) {
                                    if (Fyulytics.sharedInstance().a()) {
                                        Log.w(Fyulytics.TAG, "HTTP error response code was " + responseCode + " from submitting event data: " + str);
                                    }
                                }
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                            } catch (Throwable th22) {
                                Throwable th4 = th22;
                                th22 = th;
                                th = th4;
                            }
                        } else {
                            responseCode = 0;
                            i3 = 1;
                        }
                        if (i3 != 0) {
                            if (Fyulytics.sharedInstance().a()) {
                                Log.d(Fyulytics.TAG, "ok ->" + str);
                            }
                            this.a.b(str);
                        } else if (responseCode >= AbsAlbumPage.LAUNCH_QUIK_ACTIVITY && responseCode < 500) {
                            if (Fyulytics.sharedInstance().a()) {
                                Log.d(Fyulytics.TAG, "fail " + responseCode + " ->" + str);
                            }
                            this.a.b(str);
                        } else if (a != null && (a instanceof HttpURLConnection)) {
                            ((HttpURLConnection) a).disconnect();
                            return;
                        } else {
                            return;
                        }
                        if (a != null && (a instanceof HttpURLConnection)) {
                            ((HttpURLConnection) a).disconnect();
                        }
                        i = i2;
                    } catch (Throwable th5) {
                        th4 = th5;
                        uRLConnection = a;
                        th3 = th4;
                    } catch (Throwable th6) {
                        th5 = th6;
                        uRLConnection2 = a;
                    }
                } catch (Throwable th52) {
                    th3 = th52;
                    uRLConnection = null;
                } catch (Throwable th62) {
                    th52 = th62;
                    uRLConnection2 = null;
                }
            } else {
                return;
            }
        }
        return;
        throw th52;
        try {
            if (Fyulytics.sharedInstance().a()) {
                Log.w(Fyulytics.TAG, "Got exception while trying to submit event data: " + str, th3);
            }
            if (uRLConnection != null && (uRLConnection instanceof HttpURLConnection)) {
                ((HttpURLConnection) uRLConnection).disconnect();
                return;
            }
            return;
        } catch (Throwable th622) {
            th4 = th622;
            uRLConnection2 = uRLConnection;
            th52 = th4;
            if (uRLConnection2 != null && (uRLConnection2 instanceof HttpURLConnection)) {
                ((HttpURLConnection) uRLConnection2).disconnect();
            }
            throw th52;
        }
        if (inputStream != null) {
            if (th22 == null) {
                inputStream.close();
            } else {
                try {
                    inputStream.close();
                } catch (Throwable th7) {
                    th22.addSuppressed(th7);
                }
            }
        }
        throw th52;
    }
}
