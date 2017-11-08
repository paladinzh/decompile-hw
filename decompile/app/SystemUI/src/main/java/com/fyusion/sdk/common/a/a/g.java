package com.fyusion.sdk.common.a.a;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public class g {
    private final Set<d> a = Collections.synchronizedSet(new TreeSet());
    private final List<String> b = Collections.synchronizedList(new ArrayList());
    private final SharedPreferences c;

    g(Context context) {
        if (context != null) {
            this.c = context.getSharedPreferences("FYULYTICS_STORE", 0);
            f();
            g();
            return;
        }
        throw new IllegalArgumentException("must provide valid context");
    }

    static StringBuilder a(Collection<d> collection, String str) {
        StringBuilder stringBuilder = new StringBuilder();
        for (d b : collection) {
            b.b(stringBuilder);
            stringBuilder.append(str);
        }
        if (collection.size() > 0) {
            stringBuilder.delete(stringBuilder.length() - str.length(), stringBuilder.length());
        }
        return stringBuilder;
    }

    private static String b(Collection<String> collection, String str) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String append : collection) {
            stringBuilder.append(append);
            stringBuilder.append(str);
        }
        if (collection.size() > 0) {
            stringBuilder.delete(stringBuilder.length() - str.length(), stringBuilder.length());
        }
        return stringBuilder.toString();
    }

    private void f() {
        Object string = this.c.getString("EVENTS", null);
        if (!TextUtils.isEmpty(string)) {
            for (String jSONObject : string.split(":::")) {
                try {
                    d b = d.b(new JSONObject(jSONObject));
                    if (b != null) {
                        this.a.add(b);
                    }
                } catch (JSONException e) {
                }
            }
        }
    }

    private void g() {
        Object string = this.c.getString("CONNECTIONS", null);
        if (!TextUtils.isEmpty(string)) {
            this.b.addAll(Arrays.asList(string.split(":::")));
        }
    }

    public int a() {
        return this.a.size();
    }

    void a(d dVar) {
        if (this.a.size() < 100) {
            this.a.add(dVar);
            this.c.edit().putString("EVENTS", a(this.a, ":::").toString()).apply();
        }
    }

    public synchronized void a(Collection<d> collection) {
        if (collection != null) {
            if (collection.size() > 0 && this.a.removeAll(collection)) {
                this.c.edit().putString("EVENTS", a(this.a, ":::").toString()).apply();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean a(String str) {
        if (str != null) {
            if (str.length() > 0 && !d()) {
                this.b.add(str);
                this.c.edit().putString("CONNECTIONS", b(this.b, ":::")).apply();
                return true;
            }
        }
    }

    public Set<d> b() {
        return this.a;
    }

    public synchronized void b(String str) {
        if (str != null) {
            if (str.length() > 0 && this.b.remove(str)) {
                this.c.edit().putString("CONNECTIONS", b(this.b, ":::")).apply();
            }
        }
    }

    public List<String> c() {
        return this.b;
    }

    public boolean d() {
        return this.b.size() >= 1000;
    }

    public boolean e() {
        return this.b.size() == 0;
    }
}
