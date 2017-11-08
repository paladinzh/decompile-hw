package com.huawei.hwid.api.common.apkimpl;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import com.huawei.cloudservice.LoginHandler;
import com.huawei.hwid.api.common.o;
import com.huawei.hwid.core.a.b;
import com.huawei.hwid.core.d.b.e;

public class a {
    public static void a(Context context, String str, String str2, Bundle bundle, LoginHandler loginHandler, b bVar) {
        e.e("APKCloudAccountImpl", "getAccountsByType use the apk");
        if (bundle.getBoolean("AIDL") && com.huawei.hwid.core.d.b.c(context, "com.huawei.hwid.ICloudService") && b(context, bundle)) {
            o a = o.a(context, str, bundle);
            a.a(loginHandler);
            a.a();
            return;
        }
        a(context, str, str2, bundle);
    }

    private static void a(Context context, String str, String str2, Bundle bundle) {
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
        com.huawei.hwid.core.d.b.a(context, intent, 0);
    }

    public static boolean a(Context context) {
        boolean z;
        Throwable th;
        Cursor cursor = null;
        ContentResolver contentResolver = context.getContentResolver();
        Account[] accountsByType;
        try {
            contentResolver = contentResolver.query(Uri.parse("content://com.huawei.hwid.api.provider/has_login"), null, null, null, null);
            if (contentResolver != null && contentResolver.moveToFirst()) {
                if (1 != contentResolver.getInt(contentResolver.getColumnIndex("hasLogin"))) {
                    z = false;
                } else {
                    z = true;
                }
                e.b("APKCloudAccountImpl", "Account has Login: " + z);
            } else {
                try {
                    accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
                    if (accountsByType != null) {
                        if (accountsByType.length > 0) {
                            z = true;
                        }
                    }
                    z = false;
                } catch (RuntimeException e) {
                    accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
                    if (accountsByType != null) {
                        if (accountsByType.length > 0) {
                            z = true;
                            if (contentResolver != null) {
                                contentResolver.close();
                                return z;
                            }
                            return z;
                        }
                    }
                    z = false;
                    if (contentResolver != null) {
                        contentResolver.close();
                        return z;
                    }
                    return z;
                } catch (Exception e2) {
                    accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
                    if (accountsByType != null) {
                        if (accountsByType.length > 0) {
                            z = true;
                            if (contentResolver != null) {
                                contentResolver.close();
                                return z;
                            }
                            return z;
                        }
                    }
                    z = false;
                    if (contentResolver != null) {
                        contentResolver.close();
                        return z;
                    }
                    return z;
                }
            }
            if (contentResolver != null) {
                contentResolver.close();
                return z;
            }
        } catch (RuntimeException e3) {
            contentResolver = null;
            accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
            if (accountsByType != null) {
                if (accountsByType.length > 0) {
                    z = true;
                    if (contentResolver != null) {
                        contentResolver.close();
                        return z;
                    }
                    return z;
                }
            }
            z = false;
            if (contentResolver != null) {
                contentResolver.close();
                return z;
            }
            return z;
        } catch (Exception e4) {
            contentResolver = null;
            accountsByType = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
            if (accountsByType != null) {
                if (accountsByType.length > 0) {
                    z = true;
                    if (contentResolver != null) {
                        contentResolver.close();
                        return z;
                    }
                    return z;
                }
            }
            z = false;
            if (contentResolver != null) {
                contentResolver.close();
                return z;
            }
            return z;
        } catch (Throwable th2) {
            Object obj = contentResolver;
            th = th2;
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
        return z;
    }

    public static String c(Context context) {
        Cursor query;
        Exception e;
        Throwable th;
        if (context != null) {
            try {
                query = context.getContentResolver().query(Uri.parse("content://com.huawei.hwid.api.provider/hwid_account_status"), null, null, null, null);
                if (query != null) {
                    try {
                        if (query.moveToFirst()) {
                            String string = query.getString(query.getColumnIndex("accountstatus"));
                            e.b("APKCloudAccountImpl", "accountStatus: " + string);
                            if (query != null) {
                                query.close();
                            }
                            return string;
                        }
                    } catch (Exception e2) {
                        e = e2;
                        try {
                            e.d("APKCloudAccountImpl", e.getMessage());
                            if (query != null) {
                                query.close();
                            }
                            return "";
                        } catch (Throwable th2) {
                            th = th2;
                            if (query != null) {
                                query.close();
                            }
                            throw th;
                        }
                    }
                }
                if (query != null) {
                    query.close();
                }
            } catch (Exception e3) {
                e = e3;
                query = null;
                e.d("APKCloudAccountImpl", e.getMessage());
                if (query != null) {
                    query.close();
                }
                return "";
            } catch (Throwable th3) {
                th = th3;
                query = null;
                if (query != null) {
                    query.close();
                }
                throw th;
            }
            return "";
        }
        e.b("APKCloudAccountImpl", "context is null");
        return "";
    }

    private static boolean b(Context context, Bundle bundle) {
        return (bundle != null && bundle.getBoolean("check_sim_status") && "blocked".equals(c(context))) ? false : true;
    }
}
