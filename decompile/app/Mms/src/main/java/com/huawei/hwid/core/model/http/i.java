package com.huawei.hwid.core.model.http;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.exception.TokenInvalidatedException;
import com.huawei.hwid.core.model.http.request.ai;
import com.huawei.hwid.core.model.http.request.al;
import com.huawei.hwid.core.model.http.request.t;
import com.huawei.hwid.core.model.http.request.z;
import com.huawei.hwid.manager.g;
import com.huawei.membercenter.sdk.api.MemberServiceAPI;
import com.huawei.membercenter.sdk.api.MemberServiceAPI.IQueryMemberStatusCallback;
import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

/* compiled from: RequestManager */
public class i {
    private static boolean a = false;

    public static void a(Context context, a aVar, String str, Handler handler) {
        a(context, aVar, str, null, handler);
    }

    public static void a(Context context, a aVar, String str, HwAccount hwAccount, Handler handler) {
        if (aVar.r() <= 0) {
            aVar.c(d.a(context, str));
        }
        new k(context, aVar, handler, str, hwAccount).start();
    }

    public static Bundle a(Context context, a aVar, String str, HwAccount hwAccount) {
        int v = aVar.v();
        int i = 3 - v;
        a.b("RequestManager", "request is " + aVar.getClass().getName() + "  requestTimes = " + v + "  startFromTimes = " + i);
        a(context, aVar, str, hwAccount, i);
        Bundle i2 = aVar.i();
        if (SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE == aVar.j() && aVar.k() == 0) {
            i2.putAll(aVar.h());
        }
        return i2;
    }

    private static void a(Context context, a aVar, String str, HwAccount hwAccount, int i) {
        if (i < 3) {
            int i2 = i + 1;
            try {
                if (d.a(context)) {
                    HttpResponse a = h.a(context, aVar, str, hwAccount);
                    Header[] headers = a.getHeaders("Set-Cookie");
                    int statusCode = a.getStatusLine().getStatusCode();
                    aVar.a(statusCode);
                    a.b("RequestManager", "httpResponseCode = " + statusCode);
                    String entityUtils = EntityUtils.toString(a.getEntity(), "UTF-8");
                    a.a("RequestManager", "response responseXMLContent = " + f.a(entityUtils, true));
                    if (!TextUtils.isEmpty(entityUtils) && entityUtils.contains("<html")) {
                        aVar.a(1001);
                        return;
                    }
                    if (SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE != statusCode) {
                        if (307 != statusCode) {
                            a.b("RequestManager", "httpResponseCode is " + statusCode + ", prepare to retry: " + i2);
                            a(context, aVar, str, hwAccount, i2);
                        } else {
                            Header firstHeader = a.getFirstHeader(NetUtil.REQ_QUERY_LOCATION);
                            if (firstHeader != null) {
                                Object value = firstHeader.getValue();
                                if (!TextUtils.isEmpty(value)) {
                                    aVar.e(value);
                                    a(context, aVar, str, hwAccount, i2);
                                }
                            }
                        }
                    } else if (entityUtils != null) {
                        a.e("RequestManager", "parse response start");
                        if (e.URLType.equals(aVar.a())) {
                            aVar.b(entityUtils);
                        } else {
                            aVar.a(entityUtils);
                        }
                        a.e("RequestManager", "parse response end");
                        int length = headers.length;
                        if (aVar instanceof t) {
                            if (length > 0) {
                                ((t) aVar).p(headers[0].getValue());
                            }
                            if (aVar.k() != 0) {
                                com.huawei.hwid.c.a.b(context, aVar.h());
                            } else {
                                t.a(context, aVar.h());
                                if (d.l(context)) {
                                    a(context, aVar.h());
                                }
                            }
                        } else if (aVar instanceof ai) {
                            if (length > 0) {
                                ((ai) aVar).m(headers[0].getValue());
                            }
                        } else if (aVar instanceof z) {
                            if (length > 0) {
                                String value2 = headers[0].getValue();
                                com.huawei.hwid.manager.f.a(context).a(context, str, null, "Cookie", value2);
                            }
                        } else if ((aVar instanceof al) && length > 0) {
                            ((al) aVar).f(headers[0].getValue());
                        }
                        if (!(aVar instanceof z)) {
                            b(context, aVar, str, hwAccount, i2);
                        } else if (a) {
                            a(false);
                            b(context, aVar, str, hwAccount, i2);
                        }
                    }
                    return;
                }
                aVar.a(1005);
            } catch (Throwable e) {
                a.d("RequestManager", "SSLPeerUnverifiedException", e);
                aVar.a(3008);
            } catch (Throwable e2) {
                a.d("RequestManager", e2.getMessage(), e2);
                aVar.a(1001);
            } catch (Throwable e22) {
                a.d("RequestManager", e22.getMessage(), e22);
                aVar.a(1002);
            } catch (Throwable e222) {
                a.d("RequestManager", e222.getMessage(), e222);
                aVar.a(1003);
            } catch (Throwable e2222) {
                a.d("RequestManager", "IOException:" + e2222.getMessage(), e2222);
                aVar.a(1005);
                a.b("RequestManager", "IOException, prepare to retry: " + i2);
                a(context, aVar, str, hwAccount, i2);
            } catch (Throwable e22222) {
                a.d("RequestManager", e22222.getMessage(), e22222);
                aVar.a(1006);
            } catch (Throwable e222222) {
                a.d("RequestManager", e222222.getMessage(), e222222);
                aVar.a(3000);
            } catch (Throwable e2222222) {
                a.d("RequestManager", "NullPointerException", e2222222);
                aVar.a(3001);
            }
        } else {
            a.e("RequestManager", "exceed max request try time");
        }
    }

    private static void b(Context context, a aVar, String str, HwAccount hwAccount, int i) throws IOException, TokenInvalidatedException {
        if (!p.e(str)) {
            g a = com.huawei.hwid.manager.f.a(context);
            if (b(aVar)) {
                HwAccount c = a.c(context, str, null);
                Object obj = "";
                if (c != null) {
                    obj = c.f();
                }
                if (!TextUtils.isEmpty(obj)) {
                    a.b(context, "com.huawei.hwid", obj);
                    c = a.c(context, str, null);
                    String str2 = "";
                    if (c != null) {
                        str2 = c.f();
                    }
                    if (TextUtils.isEmpty(str2)) {
                        a.a(context, str, null);
                    } else {
                        aVar.c(str2);
                        a(context, aVar, str, hwAccount, i);
                        return;
                    }
                }
                throw new TokenInvalidatedException("token is invalidated");
            } else if (a(aVar)) {
                a.e("RequestManager", "user session is out of date.");
                a.a(context, str, null, "Cookie", "");
                if (a(context, str, hwAccount, aVar)) {
                    a(context, aVar, str, hwAccount, i);
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

    private static boolean a(Context context, String str, HwAccount hwAccount, a aVar) throws IOException, TokenInvalidatedException {
        c cVar = new c(context, "5", str);
        HwAccount c = com.huawei.hwid.manager.f.a(context).c(context, str, null);
        Object obj = "";
        String str2 = "";
        if (c != null) {
            obj = c.f();
            str2 = c.b();
        }
        if (TextUtils.isEmpty(obj)) {
            throw new TokenInvalidatedException("token is null");
        }
        a(true);
        a zVar = new z(context, str2, obj, d.a(context, str), null);
        a(context, zVar, str, hwAccount, 0);
        aVar.a(zVar.j());
        cVar.a(d.a());
        try {
            if (SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE != aVar.h().getInt("responseCode")) {
                cVar.c(String.valueOf(zVar.k()));
                cVar.d(zVar.l());
                return false;
            } else if (zVar.k() != 0) {
                if (d.a(context)) {
                    com.huawei.hwid.core.a.d.a(cVar, context);
                }
                return false;
            } else {
                if (d.a(context)) {
                    com.huawei.hwid.core.a.d.a(cVar, context);
                }
                return true;
            }
        } finally {
            if (d.a(context)) {
                com.huawei.hwid.core.a.d.a(cVar, context);
            }
        }
    }

    private static synchronized void a(boolean z) {
        synchronized (i.class) {
            a = z;
        }
    }

    private static void a(Context context, Bundle bundle) {
        a.a("RequestManager", "queryMemberStatus");
        a(context, new j(bundle, context));
    }

    private static void a(Context context, IQueryMemberStatusCallback iQueryMemberStatusCallback) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accountsByType = accountManager.getAccountsByType("com.huawei.hwid");
        if (accountsByType != null && accountsByType.length > 0) {
            Account account = accountsByType[0];
            String userData = accountManager.getUserData(account, "userId");
            String userData2 = accountManager.getUserData(account, "deviceType");
            String userData3 = accountManager.getUserData(account, "deviceId");
            String peekAuthToken = accountManager.peekAuthToken(account, "cloud");
            Bundle bundle = new Bundle();
            bundle.putString("userID", userData);
            bundle.putString("st", peekAuthToken);
            bundle.putString("deviceID", d.k(userData3));
            bundle.putString("deviceType", userData2);
            if (d.a(context)) {
                MemberServiceAPI.queryMemberStatus(bundle, context, iQueryMemberStatusCallback);
            }
            return;
        }
        a.b("RequestManager", "no account has logined in settings");
    }
}
