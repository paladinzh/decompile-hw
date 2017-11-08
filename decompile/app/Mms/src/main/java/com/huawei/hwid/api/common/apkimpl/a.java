package com.huawei.hwid.api.common.apkimpl;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import com.huawei.cloudservice.LoginHandler;
import com.huawei.hwid.core.a.c;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.ui.common.f;

/* compiled from: APKCloudAccountImpl */
public class a {
    public static void a(Context context, String str, String str2, Bundle bundle, LoginHandler loginHandler, c cVar) {
        com.huawei.hwid.core.c.b.a.e("APKCloudAccountImpl", "getAccountsByType use the apk");
        if (c(context)) {
            com.huawei.hwid.api.common.a.a(context, loginHandler, cVar);
            a(context, str, str2, bundle);
            return;
        }
        b(context, str, str2, bundle);
    }

    private static boolean c(Context context) {
        if (!d.b(context, "com.huawei.hwid.LOGIN_DIALOG")) {
            com.huawei.hwid.core.c.b.a.b("APKCloudAccountImpl", "hwid does not has the action: com.huawei.hwid.LOGIN_DIALOG");
            return false;
        } else if (!d.b(context, "com.huawei.hwid.LOGIN")) {
            com.huawei.hwid.core.c.b.a.b("APKCloudAccountImpl", "hwid does not has the action: com.huawei.hwid.LOGIN");
            return false;
        } else if (d.b(context, "com.huawei.hwid.ACCOUNT_MANAGER")) {
            com.huawei.hwid.core.c.b.a.b("APKCloudAccountImpl", "hwid has all actions");
            return true;
        } else {
            com.huawei.hwid.core.c.b.a.b("APKCloudAccountImpl", "hwid does not has the action: com.huawei.hwid.ACCOUNT_MANAGER");
            return false;
        }
    }

    private static void a(Context context, String str, String str2, Bundle bundle) {
        boolean z = bundle.getBoolean("popLogin", false);
        boolean z2 = bundle.getBoolean("chooseWindow", false);
        int i = bundle.getInt("scope", 0);
        boolean z3 = bundle.getBoolean("needAuth", true);
        Account[] accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
        Intent intent = new Intent();
        if (accountsByType == null || accountsByType.length == 0) {
            intent.setAction("com.huawei.hwid.START_BY_OOBE");
        } else {
            intent.setAction("com.huawei.hwid.ACCOUNT_MANAGER");
            if (!z2) {
                intent.putExtra("chooseAccount", z2);
                intent.putExtra("needAuth", z3);
                intent.putExtra("accountName", str2);
            }
        }
        intent.putExtra("scope", i);
        intent.putExtra("popLogin", z);
        intent.putExtra("startActivityWay", f.FromApp.ordinal());
        intent.putExtra("requestTokenType", str);
        com.huawei.hwid.core.c.b.a.b("APKCloudAccountImpl", "intent= " + com.huawei.hwid.core.encrypt.f.a(intent));
        intent.setPackage("com.huawei.hwid");
        d.a(context, intent, 0);
    }

    private static void b(Context context, String str, String str2, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(context, DummyActivity.class);
        intent.putExtra("requestTokenType", str);
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putString("accountName", str2);
        bundle.putBoolean("isFromApk", true);
        intent.putExtra("bundle", bundle);
        intent.setFlags(1048576);
        d.a(context, intent, 0);
    }

    public static boolean a(Context context) {
        Cursor query;
        boolean z;
        Account[] accountsByType;
        Throwable th;
        Cursor cursor = null;
        try {
            query = context.getContentResolver().query(Uri.parse("content://com.huawei.hwid.api.provider/has_login"), null, null, null, null);
            if (query != null && query.moveToFirst()) {
                if (1 != query.getInt(query.getColumnIndex("hasLogin"))) {
                    z = false;
                } else {
                    z = true;
                }
                com.huawei.hwid.core.c.b.a.b("APKCloudAccountImpl", "Account has Login: " + z);
            } else {
                try {
                    accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
                    if (accountsByType != null) {
                        if (accountsByType.length > 0) {
                            z = true;
                        }
                    }
                    z = false;
                } catch (Exception e) {
                    try {
                        accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
                        if (accountsByType != null) {
                            if (accountsByType.length > 0) {
                                z = true;
                                if (query != null) {
                                    query.close();
                                    return z;
                                }
                                return z;
                            }
                        }
                        z = false;
                        if (query != null) {
                            query.close();
                            return z;
                        }
                        return z;
                    } catch (Throwable th2) {
                        cursor = query;
                        th = th2;
                        if (cursor != null) {
                            cursor.close();
                        }
                        throw th;
                    }
                }
            }
            if (query != null) {
                query.close();
                return z;
            }
        } catch (Exception e2) {
            query = null;
            accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
            if (accountsByType != null) {
                if (accountsByType.length > 0) {
                    z = true;
                    if (query != null) {
                        query.close();
                        return z;
                    }
                    return z;
                }
            }
            z = false;
            if (query != null) {
                query.close();
                return z;
            }
            return z;
        } catch (Throwable th3) {
            th = th3;
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        return z;
    }
}
