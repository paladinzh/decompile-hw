package com.fyusion.sdk.viewer.internal.b.a;

import android.text.TextUtils;
import com.fyusion.sdk.common.DLog;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

/* compiled from: Unknown */
public class d implements a<InputStream> {
    static final b a = new a();
    private final com.fyusion.sdk.viewer.internal.b.c.d b;
    private final int c;
    private final b d;
    private HttpURLConnection e;
    private InputStream f;
    private volatile boolean g;

    /* compiled from: Unknown */
    interface b {
        HttpURLConnection a(URL url) throws IOException;
    }

    /* compiled from: Unknown */
    private static class a implements b {
        private a() {
        }

        public HttpURLConnection a(URL url) throws IOException {
            return (HttpURLConnection) url.openConnection();
        }
    }

    public d(com.fyusion.sdk.viewer.internal.b.c.d dVar) {
        this(dVar, 2500, a);
    }

    public d(com.fyusion.sdk.viewer.internal.b.c.d dVar, int i) {
        this(dVar, i, a);
    }

    d(com.fyusion.sdk.viewer.internal.b.c.d dVar, int i, b bVar) {
        this.b = dVar;
        this.c = i;
        this.d = bVar;
    }

    private InputStream a(HttpURLConnection httpURLConnection) throws IOException {
        InputStream a;
        if (TextUtils.isEmpty(httpURLConnection.getContentEncoding())) {
            a = com.fyusion.sdk.viewer.internal.f.a.a(httpURLConnection.getInputStream(), (long) httpURLConnection.getContentLength());
        } else {
            a = httpURLConnection.getInputStream();
        }
        this.f = a;
        return this.f;
    }

    private InputStream a(URL url, int i, URL url2, Map<String, String> map) throws IOException {
        if (i < 5) {
            if (url2 != null) {
                try {
                    if (url.toURI().equals(url2.toURI())) {
                        throw new com.fyusion.sdk.viewer.internal.b.d("In re-direct loop");
                    }
                } catch (URISyntaxException e) {
                }
            }
            this.e = this.d.a(url);
            for (Entry entry : map.entrySet()) {
                this.e.addRequestProperty((String) entry.getKey(), (String) entry.getValue());
            }
            this.e.setConnectTimeout(this.c);
            this.e.setReadTimeout(this.c);
            this.e.setUseCaches(false);
            this.e.setDoInput(true);
            this.e.connect();
            if (this.g) {
                return null;
            }
            int responseCode = this.e.getResponseCode();
            if (responseCode / 100 == 2) {
                return a(this.e);
            }
            if (responseCode / 100 == 3) {
                Object headerField = this.e.getHeaderField("Location");
                if (!TextUtils.isEmpty(headerField)) {
                    return a(new URL(url, headerField), i + 1, url, map);
                }
                throw new com.fyusion.sdk.viewer.internal.b.d("Received empty or null redirect url");
            } else if (responseCode != -1) {
                throw new com.fyusion.sdk.viewer.internal.b.d(this.e.getResponseMessage(), responseCode);
            } else {
                throw new com.fyusion.sdk.viewer.internal.b.d(responseCode);
            }
        }
        throw new com.fyusion.sdk.viewer.internal.b.d("Too many (> 5) redirects!");
    }

    public void a() {
        if (this.f != null) {
            try {
                this.f.close();
            } catch (IOException e) {
            }
        }
        if (this.e != null) {
            this.e.disconnect();
        }
    }

    public void a(com.fyusion.sdk.viewer.d dVar, com.fyusion.sdk.viewer.internal.b.a.a.a<? super InputStream> aVar) {
        com.fyusion.sdk.core.util.d.a();
        try {
            aVar.a(a(this.b.a(), 0, null, this.b.b()));
        } catch (Exception e) {
            DLog.w("HttpUrlFetcher", "Failed to load data for url", e);
            aVar.a(e);
        }
    }

    public void b() {
        this.g = true;
    }

    public com.fyusion.sdk.viewer.internal.b.a c() {
        return com.fyusion.sdk.viewer.internal.b.a.REMOTE;
    }
}
