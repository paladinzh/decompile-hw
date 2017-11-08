package com.huawei.openalliance.ad.utils.c;

import android.content.Context;
import com.huawei.openalliance.ad.utils.b.d;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

/* compiled from: Unknown */
public class e {
    public static HttpResponse a(Context context, HttpUriRequest httpUriRequest) throws UnsupportedEncodingException, IllegalArgumentException, IllegalStateException, IOException {
        try {
            HttpClient a = f.a();
            a.getParams().setParameter("http.protocol.handle-redirects", Boolean.valueOf(false));
            return a.execute(httpUriRequest);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e2) {
            d.a("HttpUtilsOfficial", "execute request fail", e2);
            throw new IOException("IOException[don't set proxy]");
        }
    }
}
