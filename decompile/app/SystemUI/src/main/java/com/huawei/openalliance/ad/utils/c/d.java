package com.huawei.openalliance.ad.utils.c;

import android.content.Context;
import com.huawei.openalliance.ad.utils.e;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

/* compiled from: Unknown */
public class d {
    private static String a(HttpUriRequest httpUriRequest) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(httpUriRequest.getMethod()).append(" ").append(httpUriRequest.getProtocolVersion());
        stringBuffer.append(" head:");
        for (Header header : httpUriRequest.getAllHeaders()) {
            stringBuffer.append(header.getName()).append("=").append(header.getValue());
        }
        stringBuffer.append(" reqLine:" + httpUriRequest.getRequestLine());
        return stringBuffer.toString();
    }

    public static HttpResponse a(Context context, HttpUriRequest httpUriRequest) throws UnsupportedEncodingException, IllegalArgumentException, IllegalStateException, IOException {
        try {
            HttpClient a = a.a();
            com.huawei.openalliance.ad.utils.b.d.b("HttpUtils", e.a("direct connect start! req:" + a(httpUriRequest)));
            return a.execute(httpUriRequest);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e2) {
            com.huawei.openalliance.ad.utils.b.d.a("HttpUtils", "execute request fail", e2);
            throw new IOException("IOException[don't set proxy]");
        }
    }
}
