package com.huawei.hwid.core.b.a;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import com.android.gallery3d.gadget.XmlUtils;
import com.autonavi.amap.mapcore.ERROR_CODE;
import com.huawei.hwid.b.a;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.d.b;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.exception.TokenInvalidatedException;
import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class d {
    private static boolean a = false;

    public static void a(Context context, a aVar, String str, Handler handler) {
        if (aVar.r() <= 0) {
            aVar.c(b.a(context, str));
        }
        e.a("RequestManager", "globalSiteId:" + aVar.r());
        new e(context, aVar, handler, str).start();
    }

    public static Bundle a(Context context, a aVar, String str) {
        int t = aVar.t();
        int i = 3 - t;
        e.b("RequestManager", "request is " + aVar.getClass().getName() + "  requestTimes = " + t + "  startFromTimes = " + i);
        a(context, aVar, str, i);
        Bundle i2 = aVar.i();
        if (SmsCheckResult.ESCT_200 == aVar.j() && aVar.k() == 0) {
            i2.putAll(aVar.h());
        }
        return i2;
    }

    private static void a(Context context, a aVar, String str, int i) {
        if (i < 3) {
            int i2 = i + 1;
            try {
                if (b.a(context)) {
                    HttpResponse a = c.a(context, aVar, str);
                    Header[] headers = a.getHeaders("Set-Cookie");
                    int statusCode = a.getStatusLine().getStatusCode();
                    aVar.a(statusCode);
                    e.b("RequestManager", "httpResponseCode = " + statusCode);
                    String entityUtils = EntityUtils.toString(a.getEntity(), XmlUtils.INPUT_ENCODING);
                    e.b("RequestManager", "response responseXMLContent = " + f.a(entityUtils, true));
                    if (!TextUtils.isEmpty(entityUtils) && entityUtils.contains("<html")) {
                        aVar.a((int) ERROR_CODE.CONN_CREATE_FALSE);
                        return;
                    } else if (SmsCheckResult.ESCT_200 != statusCode) {
                        if (SmsCheckResult.ESCT_307 != statusCode) {
                            e.b("RequestManager", "httpResponseCode is " + statusCode + ", prepare to retry: " + i2);
                            a(context, aVar, str, i2);
                        } else {
                            Header firstHeader = a.getFirstHeader("location");
                            if (firstHeader != null) {
                                String value = firstHeader.getValue();
                                if (!TextUtils.isEmpty(value)) {
                                    aVar.d(value);
                                    a(context, aVar, str, i2);
                                }
                            }
                        }
                        return;
                    } else {
                        if (entityUtils != null) {
                            e.e("RequestManager", "parse response start");
                            if (com.huawei.hwid.core.b.a.a.d.URLType.equals(aVar.a())) {
                                aVar.b(entityUtils);
                            } else {
                                aVar.a(entityUtils);
                            }
                            e.e("RequestManager", "parse response end");
                            b(context, aVar, headers.length <= 0 ? "" : headers[0].getValue());
                            if (!(aVar instanceof com.huawei.hwid.core.b.a.a.d)) {
                                b(context, aVar, str, i2);
                            } else if (a) {
                                a(false);
                                b(context, aVar, str, i2);
                            }
                        }
                        return;
                    }
                }
                aVar.a(1007);
            } catch (Throwable e) {
                e.d("RequestManager", "SSLPeerUnverifiedException", e);
                aVar.a(3008);
            } catch (Throwable e2) {
                e.d("RequestManager", e2.getMessage(), e2);
                aVar.a((int) ERROR_CODE.CONN_CREATE_FALSE);
            } catch (Throwable e22) {
                e.d("RequestManager", e22.getMessage(), e22);
                aVar.a(1002);
            } catch (Throwable e222) {
                e.d("RequestManager", e222.getMessage(), e222);
                aVar.a(1003);
            } catch (Throwable e2222) {
                e.d("RequestManager", "IOException:" + e2222.getMessage(), e2222);
                aVar.a(1005);
                e.b("RequestManager", "IOException, prepare to retry: " + i2);
                a(context, aVar, str, i2);
            } catch (Throwable e22222) {
                e.d("RequestManager", e22222.getMessage(), e22222);
                aVar.a(1006);
            } catch (Throwable e222222) {
                e.d("RequestManager", e222222.getMessage(), e222222);
                aVar.a(3000);
            } catch (Throwable e2222222) {
                e.d("RequestManager", "NullPointerException", e2222222);
                aVar.a(3001);
            }
        } else {
            e.e("RequestManager", "exceed max request try time");
        }
    }

    private static void b(Context context, a aVar, String str) {
        if (!TextUtils.isEmpty(str) && aVar.k() == 0) {
            Object obj = null;
            if (aVar instanceof com.huawei.hwid.core.b.a.a.d) {
                obj = ((com.huawei.hwid.core.b.a.a.d) aVar).u();
            }
            if (!TextUtils.isEmpty(obj)) {
                a.a(context).a(obj, str);
            }
        }
    }

    private static void b(Context context, a aVar, String str, int i) throws IOException, TokenInvalidatedException {
        if (!TextUtils.isEmpty(str)) {
            com.huawei.hwid.a.b a = com.huawei.hwid.a.a.a(context);
            if (b(aVar)) {
                HwAccount b = a.b(context, str, null);
                String str2 = "";
                if (b != null) {
                    str2 = b.g();
                }
                if (!TextUtils.isEmpty(str2)) {
                    a.a(context, str, "com.huawei.hwid", str2);
                    HwAccount b2 = a.b(context, str, null);
                    String str3 = "";
                    if (b2 != null) {
                        str3 = b2.g();
                    }
                    if (TextUtils.isEmpty(str3)) {
                        e.b("RequestManager", "autoCheck removeAccount");
                        a.b(context, str, "com.huawei.hwid", str2);
                    } else {
                        aVar.c(str3);
                        a.b(context, str, "com.huawei.hwid", str3);
                        a(context, aVar, str, i);
                        return;
                    }
                }
                throw new TokenInvalidatedException("token is invalidated");
            } else if (a(aVar)) {
                e.e("RequestManager", "user session is out of date.");
                a.a(context, str, null, "Cookie", "");
                if (a(context, str, aVar)) {
                    a(context, aVar, str, i);
                }
            }
        }
    }

    private static boolean a(a aVar) {
        if (70001101 != aVar.k()) {
            return false;
        }
        return true;
    }

    private static boolean b(a aVar) {
        if (70002015 == aVar.k() || 70002016 == aVar.k()) {
            return true;
        }
        return false;
    }

    private static boolean a(Context context, String str, a aVar) throws IOException, TokenInvalidatedException {
        com.huawei.hwid.core.a.b bVar = new com.huawei.hwid.core.a.b(context, "5", str);
        HwAccount b = a.a(context).b();
        if (b == null) {
            b = a.a(context).c();
        }
        Object obj = "";
        String str2 = "";
        if (b != null) {
            obj = b.g();
            str2 = b.c();
        }
        if (TextUtils.isEmpty(obj)) {
            throw new TokenInvalidatedException("token is null");
        }
        a(true);
        a dVar = new com.huawei.hwid.core.b.a.a.d(context, str2, obj, b.a(context, str), null);
        a(context, dVar, str, 0);
        aVar.a(dVar.j());
        if (dVar.j() == SmsCheckResult.ESCT_200) {
            if (dVar.k() == 70002016 || dVar.k() == 70002015) {
                aVar.a(3000);
            }
        }
        bVar.a(b.a());
        try {
            if (SmsCheckResult.ESCT_200 != aVar.h().getInt("responseCode")) {
                bVar.b(String.valueOf(dVar.k()));
                bVar.c(dVar.l());
                return false;
            } else if (dVar.k() != 0) {
                if (b.a(context)) {
                    c.a(bVar, context);
                }
                return false;
            } else {
                if (b.a(context)) {
                    c.a(bVar, context);
                }
                return true;
            }
        } finally {
            if (b.a(context)) {
                c.a(bVar, context);
            }
        }
    }

    private static synchronized void a(boolean z) {
        synchronized (d.class) {
            a = z;
        }
    }
}
