package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.as.a;
import com.avast.android.shepherd.obfuscated.as.c;
import com.avast.android.shepherd.obfuscated.as.e;
import com.avast.android.shepherd.obfuscated.as.g;
import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.util.EntityUtils;

/* compiled from: Unknown */
public class o {
    static final /* synthetic */ boolean f = (!o.class.desiredAssertionStatus());
    private static final Random g = new Random();
    private static final char[] h = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static String i = "cert/auth.jks";
    protected String a;
    protected s b = new t();
    protected AbstractHttpClient c = new DefaultHttpClient();
    protected a d;
    protected DefaultHttpRequestRetryHandler e = new DefaultHttpRequestRetryHandler();
    private final AtomicLong j = new AtomicLong(g.nextLong());
    private final AtomicLong k = new AtomicLong(System.currentTimeMillis());

    private String a(byte[] bArr) {
        char[] cArr = new char[(bArr.length * 2)];
        for (int i = 0; i < bArr.length; i++) {
            int i2 = bArr[i] & 255;
            cArr[i * 2] = (char) h[i2 >>> 4];
            cArr[(i * 2) + 1] = (char) h[i2 & 15];
        }
        return new String(cArr);
    }

    public static SchemeRegistry a(KeyStore keyStore) {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        if (keyStore != null) {
            schemeRegistry.register(new Scheme("https", new SSLSocketFactory(keyStore), 443));
        }
        schemeRegistry.register(new Scheme("http", new PlainSocketFactory(), 80));
        return schemeRegistry;
    }

    protected n a() {
        return a(this.d, null);
    }

    public n a(a aVar, n nVar) {
        if (this.a != null) {
            HttpRequestBase httpPost = new HttpPost(URI.create(this.a + "/" + "V1" + "/" + "REG"));
            try {
                c.a a = c.i().a(aVar).a(g.e().a(c()));
                if (nVar != null) {
                    a.a(nVar.a());
                }
                httpPost.setEntity(new ByteArrayEntity(a.d().toByteArray()));
                e a2 = e.a(a(httpPost));
                Calendar.getInstance(TimeZone.getTimeZone("GMT+1")).setTimeInMillis(a2.j());
                return new n(a2.f(), a2.h(), a2.j());
            } catch (Throwable e) {
                throw new u(e);
            }
        }
        throw new NullPointerException("You have to set authorization host by calling setAuthHost() before calling register().");
    }

    public void a(a aVar) {
        this.d = aVar;
    }

    public void a(String str) {
        if (!str.startsWith("http")) {
            str = "https://" + str;
        }
        this.a = str;
    }

    protected byte[] a(p pVar, long j, n nVar, byte[] bArr, boolean z) {
        try {
            if (!f && bArr == null) {
                throw new AssertionError();
            }
            HttpRequestBase httpPost = new HttpPost(pVar.a(a(nVar.a().toByteArray()), Long.toString(j)));
            b("Preparing request to " + httpPost.getURI().toString() + ", data length " + bArr.length);
            byte[] a = d.a(nVar.b().toByteArray(), j);
            HttpEntity byteArrayEntity = new ByteArrayEntity(d.a(bArr, a));
            byteArrayEntity.setChunked(z);
            httpPost.setEntity(byteArrayEntity);
            byte[] a2 = a(httpPost);
            if (a2.length != 0) {
                OutputStream bVar = new b(1024);
                OutputStream a3 = d.a(bVar, a);
                a3.write(a2);
                a3.close();
                return ByteString.copyFrom(bVar.b()).toByteArray();
            }
            b("Empty response, skipping decryption");
            return a2;
        } catch (Throwable e) {
            throw new r(e);
        } catch (Throwable e2) {
            throw new r(e2);
        } catch (Throwable e22) {
            throw new r(e22);
        } catch (Throwable e222) {
            throw new r(e222);
        } catch (Throwable e2222) {
            throw new r(e2222);
        } catch (Throwable e22222) {
            throw new r(e22222);
        } catch (Throwable e222222) {
            throw new r(e222222);
        }
    }

    protected byte[] a(p pVar, byte[] bArr, boolean z) {
        n a = this.b.a();
        if (a == null || a.d()) {
            if (this.d != null) {
                a = a();
                if (a == null) {
                    throw new IllegalStateException("Loading key failed");
                }
                this.b.a(a);
            } else {
                throw new IllegalStateException("Identity not set but needed for key registration");
            }
        }
        return a(pVar, b(), a, bArr, z);
    }

    protected byte[] a(InputStream inputStream) {
        byte[] bArr = new byte[16384];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            int read = inputStream.read(bArr, 0, bArr.length);
            if (read == -1) {
                byteArrayOutputStream.flush();
                return byteArrayOutputStream.toByteArray();
            }
            byteArrayOutputStream.write(bArr, 0, read);
        }
    }

    protected byte[] a(String str, InputStream inputStream, boolean z) {
        return a(str, a(inputStream), z);
    }

    public byte[] a(String str, byte[] bArr) {
        return a(str, bArr, true);
    }

    protected byte[] a(String str, byte[] bArr, boolean z) {
        return a(p.a(str), bArr, z);
    }

    protected byte[] a(HttpRequestBase httpRequestBase) {
        HttpResponse execute = this.c.execute(httpRequestBase);
        b("Response code: " + execute.getStatusLine().getStatusCode());
        if (execute.getStatusLine().getStatusCode() == 200) {
            byte[] toByteArray = EntityUtils.toByteArray(execute.getEntity());
            b("Raw response size: " + (toByteArray == null ? 0 : toByteArray.length));
            return toByteArray == null ? new byte[0] : toByteArray;
        } else {
            throw new IOException("Invalid response code: " + execute.getStatusLine().getStatusCode());
        }
    }

    protected long b() {
        return this.k.incrementAndGet();
    }

    protected void b(String str) {
    }

    protected ByteString c() {
        long incrementAndGet = this.j.incrementAndGet();
        ByteBuffer allocate = ByteBuffer.allocate(8);
        allocate.putLong(incrementAndGet);
        allocate.flip();
        return ByteString.copyFrom(allocate);
    }
}
