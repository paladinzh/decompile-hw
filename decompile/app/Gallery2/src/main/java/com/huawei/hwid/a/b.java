package com.huawei.hwid.a;

import android.content.Context;
import com.huawei.hwid.core.datatype.HwAccount;
import java.util.ArrayList;

public interface b {
    ArrayList<HwAccount> a(Context context, String str);

    void a(Context context, String str, String str2, String str3);

    void a(Context context, String str, String str2, String str3, String str4);

    boolean a(Context context, HwAccount hwAccount);

    boolean a(Context context, ArrayList<HwAccount> arrayList);

    HwAccount b(Context context, String str, String str2);

    void b(Context context, String str);

    void b(Context context, String str, String str2, String str3);
}
