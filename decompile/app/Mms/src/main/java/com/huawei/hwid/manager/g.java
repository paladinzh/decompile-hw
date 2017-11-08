package com.huawei.hwid.manager;

import android.accounts.AccountManagerCallback;
import android.content.Context;
import com.huawei.hwid.core.datatype.HwAccount;
import java.util.ArrayList;

/* compiled from: IHwAccountManager */
public interface g {
    String a(Context context, String str, String str2, String str3);

    ArrayList a(Context context, String str);

    void a(Context context, String str, String str2);

    void a(Context context, String str, String str2, AccountManagerCallback accountManagerCallback);

    void a(Context context, String str, String str2, String str3, String str4);

    boolean a(Context context, HwAccount hwAccount);

    boolean a(Context context, ArrayList arrayList);

    void b(Context context, String str);

    void b(Context context, String str, String str2);

    HwAccount c(Context context, String str, String str2);

    boolean c(Context context, String str);

    void d(Context context, String str);
}
