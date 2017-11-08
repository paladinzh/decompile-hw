package com.avast.android.sdk.engine.obfuscated;

import com.avast.android.sdk.engine.obfuscated.bm.a;
import com.avast.android.sdk.engine.obfuscated.bm.c;
import com.avast.android.sdk.engine.obfuscated.bm.e;
import com.avast.android.sdk.engine.obfuscated.bm.g;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.util.EntityUtils;

/* compiled from: Unknown */
public class p {
    static final /* synthetic */ boolean f = (!p.class.desiredAssertionStatus());
    private static final Random g = new Random();
    private static final char[] h = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static String i = "cert/auth.jks";
    protected String a;
    protected t b = new u();
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

    protected o a() throws v {
        return a(this.d, null);
    }

    public o a(a aVar, o oVar) throws v {
        if (this.a != null) {
            HttpRequestBase httpPost = new HttpPost(URI.create(this.a + "/" + "V1" + "/" + "REG"));
            try {
                c.a a = c.i().a(aVar).a(g.e().a(c()));
                if (oVar != null) {
                    a.a(oVar.a());
                }
                httpPost.setEntity(new ByteArrayEntity(a.d().toByteArray()));
                e a2 = e.a(a(httpPost));
                Calendar.getInstance(TimeZone.getTimeZone("GMT+1")).setTimeInMillis(a2.j());
                return new o(a2.f(), a2.h(), a2.j());
            } catch (Throwable e) {
                throw new v(e);
            }
        }
        throw new NullPointerException("You have to set authorization host by calling setAuthHost() before calling register().");
    }

    public void a(a aVar) {
        this.d = aVar;
    }

    public void a(t tVar) {
        this.b = tVar;
    }

    public void a(String str) {
        if (!str.startsWith("http")) {
            str = "https://" + str;
        }
        this.a = str;
    }

    protected byte[] a(q qVar, long j, o oVar, byte[] bArr, boolean z) throws s, IOException {
        try {
            if (!f && bArr == null) {
                throw new AssertionError();
            }
            HttpRequestBase httpPost = new HttpPost(qVar.a(a(oVar.a().toByteArray()), Long.toString(j)));
            b("Preparing request to " + httpPost.getURI().toString() + ", data length " + bArr.length);
            byte[] a = e.a(oVar.b().toByteArray(), j);
            HttpEntity byteArrayEntity = new ByteArrayEntity(e.a(bArr, a));
            byteArrayEntity.setChunked(z);
            httpPost.setEntity(byteArrayEntity);
            byte[] a2 = a(httpPost);
            if (a2.length != 0) {
                OutputStream aVar = new a(1024);
                OutputStream a3 = e.a(aVar, a);
                a3.write(a2);
                a3.close();
                return ByteString.copyFrom(aVar.b()).toByteArray();
            }
            b("Empty response, skipping decryption");
            return a2;
        } catch (Throwable e) {
            throw new s(e);
        } catch (Throwable e2) {
            throw new s(e2);
        } catch (Throwable e22) {
            throw new s(e22);
        } catch (Throwable e222) {
            throw new s(e222);
        } catch (Throwable e2222) {
            throw new s(e2222);
        } catch (Throwable e22222) {
            throw new s(e22222);
        } catch (Throwable e222222) {
            throw new s(e222222);
        }
    }

    protected byte[] a(q qVar, byte[] bArr, boolean z) throws s, IOException {
        o a = this.b.a();
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
        return a(qVar, b(), a, bArr, z);
    }

    public byte[] a(String str, byte[] bArr) throws s, IOException {
        return a(str, bArr, true);
    }

    protected byte[] a(String str, byte[] bArr, boolean z) throws s, IOException {
        return a(q.a(str), bArr, z);
    }

    protected byte[] a(HttpRequestBase httpRequestBase) throws IOException {
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
