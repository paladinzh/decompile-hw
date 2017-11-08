package com.avast.android.shepherd;

import com.avast.android.shepherd.obfuscated.bc.aa;
import com.avast.android.shepherd.obfuscated.bc.ab;
import com.avast.android.shepherd.obfuscated.bc.c;
import com.avast.android.shepherd.obfuscated.bc.g;
import com.avast.android.shepherd.obfuscated.bc.m;
import com.avast.android.shepherd.obfuscated.bc.o;
import com.avast.android.shepherd.obfuscated.bc.q;
import com.avast.android.shepherd.obfuscated.bc.q.a;
import com.avast.android.shepherd.obfuscated.x;
import com.google.protobuf.ByteString;

/* compiled from: Unknown */
public class DefaultConfigFactory {

    /* compiled from: Unknown */
    public interface ConfigCustomLayer {
        q createModifiedConfig(q qVar);
    }

    private static q a(a aVar) {
        try {
            ConfigCustomLayer configCustomLayer = (ConfigCustomLayer) Class.forName("com.avast.shepherd.DefaultConfigCustomLayer").newInstance();
            x.b("DefaultConfigFactory", "A customization layer for the default Shepherd config has been found. Calling the getModifiedConfig method.");
            return configCustomLayer.createModifiedConfig(aVar.d());
        } catch (ClassNotFoundException e) {
            x.b("DefaultConfigFactory", "A customization layer for the default Shepherd config not found.");
            return aVar.d();
        } catch (Throwable e2) {
            x.a("DefaultConfigFactory", "The found default Shepherd config customization layer class isn't compatible.", e2);
            return aVar.d();
        } catch (Throwable e22) {
            x.a("DefaultConfigFactory", "The found default Shepherd config customization layer class isn't compatible.", e22);
            return aVar.d();
        } catch (Throwable e222) {
            x.a("DefaultConfigFactory", "Unable to instantiate the found default Shepherd config customization layer", e222);
            return aVar.d();
        }
    }

    public static q getDefaultConfig() {
        a w = q.w();
        o.a v = o.v();
        v.a(aa.DEBUG);
        ab.a g = ab.g();
        g.a(ByteString.copyFromUtf8(".*"));
        g.a(aa.WARNING);
        v.a(g);
        v.b(ByteString.copyFromUtf8("https://ff-billing.avast.com"));
        m.a j = m.j();
        j.b(500);
        j.a(500);
        j.c(3600000);
        v.a(j);
        w.a(v);
        g.a aa = g.aa();
        aa.a(ByteString.copyFromUtf8("http://au.ff.avast.com:80/android/"));
        aa.e(ByteString.copyFromUtf8("http://al.ff.avast.com:80/F/"));
        aa.h(ByteString.copyFromUtf8("http://ab.ff.avast.com:80/cgi-bin/submit50.cgi"));
        aa.f(ByteString.copyFromUtf8("http://ui.ff.avast.com:80/urlinfo/v4/_MD/"));
        aa.g(ByteString.copyFromUtf8("http://ta.ff.avast.com:80/F/"));
        aa.i(ByteString.copyFromUtf8("https://ipm-provider.ff.avast.com"));
        aa.d(ByteString.copyFromUtf8("http://ai.ff.avast.com/F/"));
        w.a(aa);
        c.a e = c.e();
        e.a(ByteString.copyFromUtf8("https://ff-backup.avast.com"));
        w.a(e);
        return a(w);
    }
}
