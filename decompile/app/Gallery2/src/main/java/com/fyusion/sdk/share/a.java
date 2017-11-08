package com.fyusion.sdk.share;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/* compiled from: Unknown */
class a {
    private static final String d = ("|o_O|" + Long.toString(System.currentTimeMillis()) + "|o_O|");
    private String a;
    private HttpURLConnection b;
    private OutputStream c;

    a(String str) {
        this.a = str;
    }

    private void b(String str, String str2) throws Exception {
        this.c.write(("--" + d + "\r\n").getBytes());
        this.c.write("Content-Type: text/plain\r\n".getBytes());
        this.c.write(("Content-Disposition: form-data; name=\"" + str + "\"\r\n").getBytes());
        this.c.write(("\r\n" + str2 + "\r\n").getBytes());
    }

    protected void a() throws Exception {
        this.b = (HttpURLConnection) new URL(this.a).openConnection();
        this.b.setRequestMethod("POST");
        this.b.setDoInput(true);
        this.b.setDoOutput(true);
        this.b.setRequestProperty("Connection", "Keep-Alive");
        this.b.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + d);
        this.b.connect();
        this.c = this.b.getOutputStream();
    }

    protected void a(String str, String str2) throws Exception {
        b(str, str2);
    }

    protected void a(String str, String str2, byte[] bArr) throws Exception {
        this.c.write(("--" + d + "\r\n").getBytes());
        this.c.write(("Content-Disposition: form-data; name=\"" + str + "\"; filename=\"" + str2 + "\"\r\n").getBytes());
        this.c.write("Content-Type: application/octet-stream\r\n".getBytes());
        this.c.write("Content-Transfer-Encoding: binary\r\n".getBytes());
        this.c.write("\r\n".getBytes());
        this.c.write(bArr);
        this.c.write("\r\n".getBytes());
    }

    protected void b() throws Exception {
        this.c.write(("--" + d + "--" + "\r\n").getBytes());
    }

    protected String c() throws Exception {
        InputStream inputStream = this.b.getInputStream();
        byte[] bArr = new byte[1024];
        StringBuilder stringBuilder = new StringBuilder();
        while (inputStream.read(bArr) != -1) {
            stringBuilder.append(new String(bArr));
        }
        this.b.disconnect();
        return stringBuilder.toString();
    }
}
