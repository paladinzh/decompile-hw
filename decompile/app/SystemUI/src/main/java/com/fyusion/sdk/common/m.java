package com.fyusion.sdk.common;

import android.os.AsyncTask;
import fyusion.vislib.BuildConfig;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/* compiled from: Unknown */
class m extends AsyncTask<String, Void, Integer> {
    private n a;
    private long b = 0;
    private a c = a.WAITING;
    private String d;

    /* compiled from: Unknown */
    /* renamed from: com.fyusion.sdk.common.m$1 */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] a = new int[a.values().length];

        static {
            try {
                a[a.SUCCESS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                a[a.ERROR.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                a[a.TIMEOUT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* compiled from: Unknown */
    enum a {
        WAITING,
        SUCCESS,
        ERROR,
        TIMEOUT,
        CANCELLED
    }

    m() {
    }

    private void a(String str) {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
            httpURLConnection.setRequestProperty("User-Agent", BuildConfig.FLAVOR);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String readLine = bufferedReader.readLine();
            httpURLConnection.disconnect();
            inputStream.close();
            bufferedReader.close();
            this.d = readLine;
            this.c = a.SUCCESS;
        } catch (Exception e) {
            this.d = e.getMessage();
            this.c = a.ERROR;
        }
    }

    protected Integer a(String... strArr) {
        this.b = System.currentTimeMillis();
        a(strArr[0]);
        while (true) {
            if ((System.currentTimeMillis() >= this.b + 15000 ? 1 : 0) == 0 && this.c == a.WAITING) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    this.c = a.CANCELLED;
                }
            }
        }
        return Integer.valueOf(this.c.ordinal());
    }

    protected void a(n nVar) {
        this.a = nVar;
    }

    protected void a(Integer num) {
        if (this.a != null) {
            switch (AnonymousClass1.a[this.c.ordinal()]) {
                case 1:
                    this.a.a(this.d);
                    return;
                case 2:
                    this.a.b(this.d);
                    return;
                case 3:
                    this.a.a();
                    return;
                default:
                    return;
            }
        }
    }

    protected /* synthetic */ Object doInBackground(Object[] objArr) {
        return a((String[]) objArr);
    }

    protected /* synthetic */ void onPostExecute(Object obj) {
        a((Integer) obj);
    }
}
