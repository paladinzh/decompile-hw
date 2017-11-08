package com.avast.android.shepherd.obfuscated;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.huawei.harassmentinterception.common.ConstValues;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

/* compiled from: Unknown */
public class ab {
    private static ab a;
    private SharedPreferences b;

    private ab(Context context) {
        this.b = context.getSharedPreferences("shepherd", 0);
    }

    public static synchronized ab a(Context context) {
        ab abVar;
        synchronized (ab.class) {
            if (a == null) {
                a = new ab(context);
            }
            abVar = a;
        }
        return abVar;
    }

    static Set<String> a(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        Set<String> linkedHashSet = new LinkedHashSet();
        StringTokenizer stringTokenizer = new StringTokenizer(str, ConstValues.SEPARATOR_KEYWORDS_EN);
        while (stringTokenizer.hasMoreTokens()) {
            linkedHashSet.add(stringTokenizer.nextToken());
        }
        return linkedHashSet;
    }

    static String b(Set<String> set) {
        if (set == null || set.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            if (!TextUtils.isEmpty(str)) {
                stringBuilder.append(str);
                if (it.hasNext()) {
                    stringBuilder.append(ConstValues.SEPARATOR_KEYWORDS_EN);
                }
            }
        }
        return stringBuilder.toString();
    }

    public long a() {
        return this.b.getLong("shepherdNextUpdateTime", -1);
    }

    void a(long j) {
        this.b.edit().putLong("shepherdNextUpdateTime", j).commit();
    }

    public void a(Set<String> set) {
        this.b.edit().putString("appMarket", b((Set) set)).commit();
    }

    void a(boolean z) {
        this.b.edit().putBoolean("shepherdConnectivityChangeReceiverEnabled", z).commit();
    }

    public HashSet<String> b() {
        CharSequence string = this.b.getString("shepherdTags", "");
        String[] split = string.split(ConstValues.SEPARATOR_KEYWORDS_EN);
        HashSet<String> hashSet = new HashSet();
        if (!TextUtils.isEmpty(string)) {
            for (Object add : split) {
                hashSet.add(add);
            }
        }
        return hashSet;
    }

    void b(long j) {
        this.b.edit().putLong("shepherdLastUpdateAttemptTime", j).commit();
    }

    public Set<String> c() {
        return a(this.b.getString("appMarket", null));
    }
}
