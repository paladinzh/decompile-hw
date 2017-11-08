package com.huawei.hwid.api.common;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.cloudservice.CloudAccount;
import com.huawei.cloudservice.CloudRequestHandler;
import com.huawei.cloudservice.LoginHandler;
import com.huawei.hwid.core.a.b;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.d.j;
import com.huawei.hwid.core.d.k;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.update.i;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressLint({"InlinedApi"})
public class d {
    private static Object b = new Object();
    private static Map<String, ArrayList<BroadcastReceiver>> d = new HashMap();
    private HwAccount a = new HwAccount();
    private Context e;
    private CloudRequestHandler f;
    private String g;
    private CloudRequestHandler h = new e(this);

    static class a extends BroadcastReceiver {
        private Context a = null;
        private boolean b = false;
        private LoginHandler c = null;
        private b d;

        public a(Context context, LoginHandler loginHandler, b bVar) {
            this.a = context;
            this.c = loginHandler;
            this.d = bVar;
        }

        private void a(Context context, Intent intent) {
            boolean booleanExtra = intent.getBooleanExtra("isUseSDK", false);
            boolean booleanExtra2 = intent.getBooleanExtra("isChangeAccount", false);
            e.b("CloudAccountImpl", "isUseSDK=" + booleanExtra + ",isSwitchAccount=" + booleanExtra2);
            if (this.c == null) {
                e.b("CloudAccountImpl", "handler is null,so cannot handler message");
                return;
            }
            String str = "";
            HwAccount b;
            if (booleanExtra) {
                if (booleanExtra2) {
                    str = intent.getStringExtra("currAccount");
                } else {
                    b = d.c(context, intent);
                    if (!(b == null || TextUtils.isEmpty(b.b()))) {
                        str = b.b();
                    }
                }
                CloudAccount[] a = d.a(context);
                this.c.onLogin(a, d.a(a, str));
            } else {
                b = d.c(context, intent);
                com.huawei.hwid.b.a.a(context).a(b);
                CloudAccount[] a2 = d.a(context);
                if (!(b == null || TextUtils.isEmpty(b.b()))) {
                    str = b.b();
                }
                this.c.onLogin(a2, d.a(a2, str));
                if (this.d == null) {
                    e.b("CloudAccountImpl", "in CloudAccountImpl, opLogItem is null");
                } else {
                    this.d.a(com.huawei.hwid.core.d.b.a());
                    c.a(this.d, context);
                    v.a(null);
                }
            }
            v.a(context, str);
        }

        private void b(Context context, Intent intent) {
            ErrorStatus errorStatus = new ErrorStatus(3002, "use the sdk: press back key");
            boolean booleanExtra = intent.getBooleanExtra("isUseSDK", true);
            Bundle bundleExtra = intent.getBundleExtra("bundle");
            if (!(bundleExtra == null || booleanExtra)) {
                int i = bundleExtra.getInt("errorcode");
                Object string = bundleExtra.getString("errorreason");
                if (!(i == 0 || TextUtils.isEmpty(string))) {
                    errorStatus = new ErrorStatus(i, string);
                }
            }
            if (this.c != null) {
                this.c.onError(errorStatus);
            }
            a(context, errorStatus);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            synchronized (d.b) {
                if (this.b) {
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putBoolean("LoginBroadcastReceiver", true);
                d.a(this.a, bundle);
                this.b = true;
            }
        }

        private void a(Context context, ErrorStatus errorStatus) {
            if (!com.huawei.hwid.core.d.b.i(context) || !v.b(context)) {
                e.b("CloudAccountImpl", "when report login Log, is sdk. not report");
            } else if (this.d != null) {
                this.d.a(com.huawei.hwid.core.d.b.a());
                if (errorStatus != null) {
                    this.d.b(String.valueOf(errorStatus.getErrorCode()));
                    this.d.c(errorStatus.getErrorReason());
                }
                c.a(this.d, context);
                v.a(null);
            } else {
                e.b("CloudAccountImpl", "when reportLog, opLogItem is null");
            }
        }
    }

    public static void a(Context context, String str, Bundle bundle, LoginHandler loginHandler) {
        boolean z = false;
        if (!a(context, loginHandler)) {
            e.b("CloudAccountImpl", "getAccountsByType: context or handler is null");
        } else if (!com.huawei.hwid.core.d.b.i(context)) {
            e.b("CloudAccountImpl", "can not use hwid");
            loginHandler.onError(new ErrorStatus(33, "hwid is not exit"));
        } else if (v.b(context)) {
            Bundle bundle2;
            boolean z2;
            boolean z3;
            j(context);
            v.a(loginHandler);
            e.b("CloudAccountImpl", "mHandler is " + loginHandler);
            f(context);
            String d = d(context, str);
            if (bundle == null) {
                bundle2 = new Bundle();
                z2 = false;
                z3 = false;
            } else {
                boolean z4 = bundle.getBoolean("popLogin", false);
                z2 = bundle.getBoolean("chooseWindow", false);
                z = bundle.getInt("loginChannel", 0);
                z3 = z4;
                bundle2 = bundle;
            }
            if (z) {
                e.e("CloudAccountImpl", "getAccountsByType:isSelectAccount=" + z2 + ",isPopLogin=" + z3);
                if (TextUtils.isEmpty(d)) {
                    d = com.huawei.hwid.core.d.b.d(context);
                }
                if (b(context, d, loginHandler)) {
                    h(context);
                    String a = v.a(context);
                    b a2 = a(context, d(context), a);
                    v.a(a2);
                    com.huawei.hwid.api.common.apkimpl.a.a(context, d, a, bundle2, loginHandler, a2);
                    return;
                }
                return;
            }
            e.b("CloudAccountImpl", "loginChannel can't be null!");
            loginHandler.onError(new ErrorStatus(12, "loginChannel can't be null!"));
        } else {
            e.b("CloudAccountImpl", "hwid is not exit");
            loginHandler.onError(new ErrorStatus(34, "hwid is not exit"));
        }
    }

    private static boolean b(Context context, String str, LoginHandler loginHandler) {
        if (context.getPackageName().equals(str)) {
            return true;
        }
        ErrorStatus errorStatus = new ErrorStatus(12, "tokenType is not the same as package name");
        e.b("CloudAccountImpl", "error: " + errorStatus.toString());
        loginHandler.onError(errorStatus);
        return false;
    }

    public static int a(CloudAccount[] cloudAccountArr, String str) {
        if (!(TextUtils.isEmpty(str) || cloudAccountArr == null || cloudAccountArr.length <= 0)) {
            for (int i = 0; i < cloudAccountArr.length; i++) {
                if (str.equalsIgnoreCase(cloudAccountArr[i].getAccountName())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static CloudAccount[] a(Context context) {
        if (context != null) {
            List a = com.huawei.hwid.a.a.a(context).a(context, com.huawei.hwid.core.d.b.m(context));
            if (a == null || a.isEmpty()) {
                return new CloudAccount[0];
            }
            CloudAccount[] cloudAccountArr = new CloudAccount[a.size()];
            for (int i = 0; i < cloudAccountArr.length; i++) {
                d dVar = new d();
                dVar.a((HwAccount) a.get(i));
                cloudAccountArr[i] = new CloudAccount(dVar);
            }
            return cloudAccountArr;
        }
        e.b("CloudAccountImpl", "context is null");
        return new CloudAccount[0];
    }

    private void a(HwAccount hwAccount) {
        this.a = hwAccount;
    }

    private static void h(Context context) {
        i(context);
        if (v.c()) {
            e.e("CloudAccountImpl", "begin to init accounts");
            a(context);
            v.a(false);
            e.e("CloudAccountImpl", "initData");
            if (TextUtils.isEmpty(v.a(context))) {
                List a = com.huawei.hwid.a.a.a(context).a(context, com.huawei.hwid.core.d.b.m(context));
                if (a != null && !a.isEmpty()) {
                    String b = ((HwAccount) a.get(0)).b();
                    v.a(context, b);
                    e.e("CloudAccountImpl", "initData===> mCurrentLoginUserName:" + f.c(b));
                }
            }
        }
    }

    private static synchronized void i(Context context) {
        synchronized (d.class) {
            e.b("CloudAccountImpl", "synAccountFromApkToSDK");
            if (com.huawei.hwid.core.d.b.i(context)) {
                if (v.b(context)) {
                    Account[] accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
                    if (accountsByType != null) {
                        if (accountsByType.length != 0) {
                            List<HwAccount> a = com.huawei.hwid.a.a.a(context).a(context, com.huawei.hwid.core.d.b.m(context));
                            if (!(a == null || a.isEmpty())) {
                                e.e("CloudAccountImpl", "sdk has accounts， so need to synchronize accounts");
                                ArrayList arrayList = new ArrayList();
                                for (HwAccount hwAccount : a) {
                                    Object obj;
                                    String b = hwAccount.b();
                                    for (Account account : accountsByType) {
                                        if (b.equals(account.name)) {
                                            obj = 1;
                                            break;
                                        }
                                    }
                                    obj = null;
                                    if (obj != null) {
                                        arrayList.add(hwAccount);
                                    }
                                }
                                if (arrayList.isEmpty()) {
                                    b(context);
                                } else {
                                    com.huawei.hwid.a.a.a(context).a(context, arrayList);
                                    e.e("CloudAccountImpl", "save accounts size: " + arrayList.size());
                                }
                                v.a(context, c(context, v.a(context)));
                            }
                        }
                    }
                    e.e("CloudAccountImpl", "apk has no account, clear all sdk accounts");
                    b(context);
                }
            }
        }
    }

    private static String c(Context context, String str) {
        List<HwAccount> a = com.huawei.hwid.a.a.a(context).a(context, com.huawei.hwid.core.d.b.m(context));
        if (a == null || a.isEmpty()) {
            return "";
        }
        for (HwAccount b : a) {
            if (b.b().equals(str)) {
                return str;
            }
        }
        return ((HwAccount) a.get(0)).b();
    }

    public static void b(Context context) {
        if (context != null) {
            e.b("CloudAccountImpl", "clear all accout data");
            com.huawei.hwid.a.a.a(context).b(context, com.huawei.hwid.core.d.b.m(context));
            v.a(context, "");
            return;
        }
        e.b("CloudAccountImpl", "context is null");
    }

    public static void a(Context context, LoginHandler loginHandler, b bVar) {
        if (context != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.huawei.cloudserive.loginSuccess");
            intentFilter.addAction("com.huawei.cloudserive.loginFailed");
            intentFilter.addAction("com.huawei.cloudserive.loginCancel");
            if (d.containsKey("LoginBroadcastReceiver")) {
                e(context, "LoginBroadcastReceiver");
            }
            BroadcastReceiver aVar = new a(context, loginHandler, bVar);
            try {
                context.registerReceiver(aVar, intentFilter);
                a(aVar, "LoginBroadcastReceiver");
            } catch (Exception e) {
                e.b("CloudAccountImpl", "BroadcastReceiver components are not allowed to register to receive intents");
            }
            return;
        }
        e.b("CloudAccountImpl", "context is null");
    }

    private static HwAccount c(Context context, Intent intent) {
        HwAccount hwAccount = new HwAccount();
        if (intent.hasExtra("hwaccount")) {
            return (HwAccount) intent.getParcelableExtra("hwaccount");
        }
        if (intent.hasExtra("accountBundle")) {
            return v.a(context, intent.getBundleExtra("accountBundle"));
        }
        if (intent.hasExtra("bundle")) {
            return v.a(context, intent.getBundleExtra("bundle"));
        }
        return hwAccount;
    }

    public HwAccount a() {
        return this.a;
    }

    public static boolean d(Context context) {
        if (context == null) {
            e.b("CloudAccountImpl", "context is null");
            return false;
        } else if (com.huawei.hwid.core.d.b.i(context) && v.b(context)) {
            return com.huawei.hwid.api.common.apkimpl.a.a(context);
        } else {
            return false;
        }
    }

    public void a(Context context, String str, CloudRequestHandler cloudRequestHandler) {
        if (!v.a(context, cloudRequestHandler)) {
            e.b("CloudAccountImpl", "getUserInfo: context or cloudRequestHandler is null");
        } else if (!com.huawei.hwid.core.d.b.i(context)) {
            e.b("CloudAccountImpl", "can not use hwid");
            cloudRequestHandler.onError(new ErrorStatus(33, "hwid is not exit"));
        } else if (v.b(context)) {
            com.huawei.hwid.core.d.b.a(context, this.a.d(), str, cloudRequestHandler);
        } else {
            e.b("CloudAccountImpl", "hwid is not exit");
            cloudRequestHandler.onError(new ErrorStatus(34, "hwid is not exit"));
        }
    }

    private void a(String str, Context context, CloudRequestHandler cloudRequestHandler) {
        HashMap hashMap = new HashMap();
        hashMap.put("userID", this.a.d());
        hashMap.put("reqClientType", "7");
        hashMap.put("fileCnt", "1");
        hashMap.put("ver", "10002");
        String a = a(com.huawei.hwid.core.b.a.a.c(), this.a.e());
        if (com.huawei.hwid.core.d.b.a(context)) {
            String string;
            a = com.huawei.hwid.core.d.f.a(context, a(str), a, hashMap, this.a.b());
            Intent intent = new Intent();
            com.huawei.hwid.core.d.f.a(a, intent);
            Bundle extras = intent.getExtras();
            if (extras != null) {
                string = extras.getString("fileUrlB", "");
            } else {
                string = "";
            }
            if (string.isEmpty()) {
                string = "upload headPic faild";
                Bundle extras2 = intent.getExtras();
                if (extras2 != null) {
                    string = extras2.getString("errorDesc", string);
                }
                ErrorStatus errorStatus = new ErrorStatus(37, string);
                e.e("CloudAccountImpl", "upload faild :" + a);
                cloudRequestHandler.onError(errorStatus);
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putBoolean("isSuccess", true);
            bundle.putString("url", string);
            cloudRequestHandler.onFinish(bundle);
            return;
        }
        cloudRequestHandler.onError(new ErrorStatus(5, context.getString(j.a(context, "CS_no_network_content"))));
        e.b("CloudAccountImpl", "error: have no network");
    }

    private File a(String str) {
        File file = new File(str);
        if (file.exists()) {
            e.c("CloudAccountImpl", "Photo is existed ");
            return file;
        }
        e.c("CloudAccountImpl", "Photo is not existed ");
        return null;
    }

    private String a(String str, int i) {
        e.a("CloudAccountImpl", "genUpdateHeadUrl, mSiteId is " + i);
        String str2 = "";
        if (i >= 1 && i <= 999) {
            str2 = String.valueOf(i);
        }
        return k.a(str, new String[]{"\\{0\\}", str2});
    }

    public static synchronized void f(Context context) {
        synchronized (d.class) {
            e.a(context);
        }
    }

    private static String d(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return com.huawei.hwid.core.d.b.m(context);
        }
        com.huawei.hwid.core.d.f.a(context, "tokenType", str);
        return str;
    }

    private static b a(Context context, boolean z, String str) {
        if (z) {
            return new b(context, "105", str);
        }
        return new b(context, "101", str);
    }

    private static boolean a(Context context, LoginHandler loginHandler) {
        if (loginHandler == null) {
            e.b("CloudAccountImpl", "loginHandler is null");
            return false;
        } else if (context != null) {
            return true;
        } else {
            e.b("CloudAccountImpl", "context is null");
            loginHandler.onError(new ErrorStatus(12, "context is null"));
            return false;
        }
    }

    public static void a(Context context, Bundle bundle) {
        if (context == null || bundle == null || d == null) {
            e.b("CloudAccountImpl", "context, bundle or broadcast is null, can't remove broadcast");
            return;
        }
        boolean z = bundle.getBoolean("LoginBroadcastReceiver", false);
        boolean z2 = bundle.getBoolean("LogoutBroadcastReceiver", false);
        boolean z3 = bundle.getBoolean("FingerBroadcastReceiver", false);
        boolean z4 = bundle.getBoolean("OpenLoginBroadcastReceiver", false);
        boolean z5 = bundle.getBoolean("BindSafePhoneBroadcastReceiver", false);
        if (z) {
            e(context, "LoginBroadcastReceiver");
        }
        if (z2) {
            e(context, "LogoutBroadcastReceiver");
        }
        if (z3) {
            e(context, "FingerBroadcastReceiver");
        }
        if (z4) {
            e(context, "OpenLoginBroadcastReceiver");
        }
        if (z5) {
            e(context, "BindSafePhoneBroadcastReceiver");
        }
    }

    private static synchronized boolean e(Context context, String str) {
        boolean z = false;
        synchronized (d.class) {
            ArrayList arrayList = (ArrayList) d.get(str);
            if (arrayList != null) {
                if (!arrayList.isEmpty()) {
                    Collection arrayList2 = new ArrayList();
                    Iterator it = arrayList.iterator();
                    while (it.hasNext()) {
                        BroadcastReceiver broadcastReceiver = (BroadcastReceiver) it.next();
                        try {
                            context.unregisterReceiver(broadcastReceiver);
                            arrayList2.add(broadcastReceiver);
                            z = true;
                            e.b("CloudAccountImpl", "remove " + str + " success!");
                        } catch (Exception e) {
                            e.c("CloudAccountImpl", e.getMessage());
                        }
                    }
                    if (!arrayList2.isEmpty()) {
                        arrayList.removeAll(arrayList2);
                    }
                }
            }
        }
        return z;
    }

    public static synchronized void a(BroadcastReceiver broadcastReceiver, String str) {
        synchronized (d.class) {
            ArrayList arrayList = (ArrayList) d.get(str);
            if (arrayList == null) {
                arrayList = new ArrayList();
            }
            arrayList.add(broadcastReceiver);
            d.put(str, arrayList);
        }
    }

    private static void j(Context context) {
        if (i.a(context).a() != null) {
            Object c = i.a(context).c(context);
            if (!TextUtils.isEmpty(c)) {
                File file = new File(c);
                if (file.exists()) {
                    try {
                        if (!file.delete()) {
                            e.d("CloudAccountImpl", "delete old apk error");
                        }
                    } catch (Exception e) {
                        e.d("CloudAccountImpl", "delete old apk error,error is " + e.getMessage());
                    }
                }
            }
            i.a(context).b();
        }
    }
}
