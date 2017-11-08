package com.avast.android.shepherd.obfuscated;

import com.avast.android.shepherd.obfuscated.as.g;
import com.avast.android.shepherd.obfuscated.as.i;
import com.avast.android.shepherd.obfuscated.as.i.b;
import com.avast.android.shepherd.obfuscated.as.k;
import com.avast.android.shepherd.obfuscated.as.k.a;
import com.avast.android.shepherd.obfuscated.as.m;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

/* compiled from: Unknown */
public class ah extends o {
    private static final char[] j = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    protected boolean g = true;
    protected ThreadSafeClientConnManager h;
    protected aj i;

    public ah(aj ajVar, s sVar) {
        a(ajVar, sVar);
    }

    private String a(byte[] bArr) {
        char[] cArr = new char[(bArr.length * 2)];
        for (int i = 0; i < bArr.length; i++) {
            int i2 = bArr[i] & 255;
            cArr[i * 2] = (char) j[i2 >>> 4];
            cArr[(i * 2) + 1] = (char) j[i2 & 15];
        }
        return new String(cArr);
    }

    public synchronized ah a(aj ajVar, s sVar) {
        return a(ajVar, sVar, null);
    }

    public synchronized ah a(aj ajVar, s sVar, SchemeRegistry schemeRegistry) {
        AbstractHttpClient defaultHttpClient;
        this.i = ajVar;
        this.b = sVar;
        if (schemeRegistry == null) {
            if (this.i.b != null) {
                try {
                    schemeRegistry = o.a(ajVar.b);
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e2) {
                    e2.printStackTrace();
                } catch (KeyStoreException e3) {
                    e3.printStackTrace();
                } catch (KeyManagementException e4) {
                    e4.printStackTrace();
                }
            }
        }
        HttpParams basicHttpParams = new BasicHttpParams();
        if (schemeRegistry == null) {
            defaultHttpClient = new DefaultHttpClient();
        } else {
            ClientConnectionManager threadSafeClientConnManager = new ThreadSafeClientConnManager(basicHttpParams, schemeRegistry);
            this.h = threadSafeClientConnManager;
            defaultHttpClient = new DefaultHttpClient(threadSafeClientConnManager, basicHttpParams);
        }
        this.c = defaultHttpClient;
        this.c.setReuseStrategy(new ai(this));
        a(ajVar.i);
        a(ajVar.d);
        return this;
    }

    protected m a(int i, int i2, MessageLite messageLite) {
        String str = this.i.e + "/" + "V1" + "/" + "MD" + "/%s/%s";
        a a = k.q().a(this.i.i).a(g.e().a(c())).a(i2).b(i).a(System.currentTimeMillis()).a(ByteString.copyFrom(messageLite != null ? messageLite.toByteArray() : new byte[0]));
        b("Sending metadata " + a(a.f().d().toByteArray()));
        m a2 = m.a(a(str, a.d().toByteArray()));
        StringBuilder append = new StringBuilder().append("Retrieve resolution ");
        Object d = (a2.e() && a2.f().c()) ? a2.f().d() : "NO RESOLUTION";
        b(append.append(d).toString());
        return a2;
    }

    public m a(MessageLite messageLite, int i, int i2, ar arVar) {
        try {
            m a = a(i, i2, messageLite);
            if (this.g && arVar != null) {
                try {
                    if (a.e()) {
                        if (a.f().c() && b.SEND.equals(a.f().d())) {
                            b("Metadata complete, sending raw data");
                            a(a, a.f(), arVar);
                            if (a.e() && a.f().c()) {
                                arVar.a(a.f().d());
                            }
                        }
                    }
                    b("Metadata complete, raw data is not wanted");
                    arVar.a(a.f().d());
                } catch (Throwable e) {
                    throw new RuntimeException("Sending raw data failed (" + this.i.e + ")", e);
                }
            }
            b("Metadata complete, skipping raw data");
            return a;
        } catch (Throwable e2) {
            throw new RuntimeException("Registration failed: cannot get key from the server (" + this.i.d + " => " + ")", e2);
        } catch (Throwable e22) {
            throw new RuntimeException("Sending metadata failed (" + this.i.e + " => " + ")", e22);
        }
    }

    public m a(MessageLite messageLite, ak akVar, int i, ar arVar) {
        return a(messageLite, akVar.a(), i, arVar);
    }

    protected void a(m mVar, i iVar, ar arVar) {
        Long l = null;
        if (iVar.e()) {
            ByteString f = iVar.f();
            Long valueOf = !iVar.i() ? null : Long.valueOf(iVar.j());
            if (iVar.k()) {
                l = Long.valueOf(iVar.l());
            }
            String str = this.i.e;
            if (iVar.g()) {
                str = "http://" + (iVar.h().size() != 4 ? Inet6Address.getByAddress(iVar.h().toByteArray()) : Inet4Address.getByAddress(iVar.h().toByteArray())).getHostAddress();
                b("Sending data to explicitly defined host: " + str);
            } else {
                b("Sending data to default host: " + str);
            }
            a(str + "/" + "V1" + "/" + "PD" + "/%s/%s/" + a(f.toByteArray()), arVar.a(valueOf, l, mVar), true);
            return;
        }
        throw new IllegalArgumentException("We don't have ticket for sending data!");
    }
}
