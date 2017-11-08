package com.huawei.hwid.update;

import android.content.Context;
import com.android.gallery3d.gadget.XmlUtils;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.update.a.a;
import com.huawei.hwid.update.a.c;
import com.huawei.hwid.vermanager.b;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class g {
    public ByteArrayOutputStream a(Context context, int i) throws IllegalArgumentException, IllegalStateException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        a aVar = new a(context);
        String str = b.a().g() + "CheckEx.action";
        e.b("OtaHttpRequest", "url: " + f.a(str));
        e.b("OtaHttpRequest", "deviceInfo: " + f.a(aVar.a(i).toString()));
        int a = a(context, str, new ByteArrayEntity(aVar.a(i).toString().getBytes(Charset.forName(XmlUtils.INPUT_ENCODING))), byteArrayOutputStream);
        e.b("OtaHttpRequest", "statusCode:" + a);
        if (a == SmsCheckResult.ESCT_200) {
            return byteArrayOutputStream;
        }
        a(byteArrayOutputStream);
        return null;
    }

    private int a(Context context, String str, ByteArrayEntity byteArrayEntity, OutputStream outputStream) throws ClientProtocolException, IOException {
        HttpResponse httpResponse = null;
        HttpClient a = b.a().a(context);
        Object httpPost = new HttpPost(str);
        int i = -1;
        if (byteArrayEntity != null) {
            byteArrayEntity.setChunked(false);
            byteArrayEntity.setContentEncoding(XmlUtils.INPUT_ENCODING);
            httpPost.setEntity(byteArrayEntity);
        }
        httpPost.getParams().setIntParameter("http.socket.timeout", 30000);
        httpPost.getParams().setIntParameter("http.connection.timeout", 30000);
        try {
            httpResponse = a.execute(httpPost);
        } catch (Exception e) {
            e.d("OtaHttpRequest", "https Exception: " + e.getMessage());
        }
        if (!(httpResponse == null || httpResponse.getStatusLine() == null)) {
            i = httpResponse.getStatusLine().getStatusCode();
            if (outputStream != null) {
                httpResponse.getEntity().writeTo(outputStream);
            }
        }
        return i;
    }

    public ByteArrayOutputStream a(Context context, String str) throws ClientProtocolException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (a(context, str, byteArrayOutputStream) == SmsCheckResult.ESCT_200) {
            return byteArrayOutputStream;
        }
        a(byteArrayOutputStream);
        return null;
    }

    public boolean a(Context context, c cVar) throws ClientProtocolException, IOException {
        boolean z = false;
        if (cVar == null) {
            return false;
        }
        if (SmsCheckResult.ESCT_200 == a(context, b.a().g() + "UpdateReport.action", new ByteArrayEntity(cVar.a(context).toString().getBytes(Charset.forName(XmlUtils.INPUT_ENCODING))), null)) {
            z = true;
        }
        return z;
    }

    public ByteArrayOutputStream b(Context context, String str) throws ClientProtocolException, IOException {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (a(context, str, byteArrayOutputStream) == SmsCheckResult.ESCT_200) {
            return byteArrayOutputStream;
        }
        a(byteArrayOutputStream);
        return null;
    }

    private int a(Context context, String str, OutputStream outputStream) throws ClientProtocolException, IOException {
        String replace;
        HttpResponse execute;
        int i;
        HttpClient a = b.a().a(context);
        if (str.startsWith("http:")) {
            replace = str.replaceFirst("http:", "https:").replace(":8180", "");
        } else {
            replace = str;
        }
        HttpUriRequest httpGet = new HttpGet(replace);
        httpGet.getParams().setIntParameter("http.socket.timeout", 30000);
        httpGet.getParams().setIntParameter("http.connection.timeout", 30000);
        try {
            execute = a.execute(httpGet);
        } catch (Exception e) {
            try {
                if (str.startsWith("https")) {
                    str = str.replaceFirst("https", "http");
                }
                HttpUriRequest httpGet2 = new HttpGet(str);
                httpGet2.getParams().setIntParameter("http.socket.timeout", 30000);
                httpGet2.getParams().setIntParameter("http.connection.timeout", 30000);
                execute = new DefaultHttpClient().execute(httpGet2);
            } catch (Exception e2) {
                e.d("OtaHttpRequest", "http Exception" + e2.getMessage());
                return -1;
            }
        }
        if (execute == null || execute.getStatusLine() == null) {
            i = -1;
        } else {
            i = execute.getStatusLine().getStatusCode();
            if (outputStream != null) {
                execute.getEntity().writeTo(outputStream);
            }
        }
        return i;
    }

    private void a(OutputStream outputStream) {
        try {
            outputStream.close();
        } catch (IOException e) {
            e.d("OtaHttpRequest", "outputStream close IOException");
        } catch (Exception e2) {
            e.d("OtaHttpRequest", "outputStream close Exception");
        }
    }
}
