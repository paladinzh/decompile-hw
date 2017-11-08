package com.avast.android.shepherd.obfuscated;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.avast.android.shepherd.Shepherd;
import com.avast.android.shepherd.Shepherd.App;
import com.avast.android.shepherd.Shepherd.Sdk;
import com.avast.android.shepherd.obfuscated.bv.aa;
import com.avast.android.shepherd.obfuscated.bv.ac;
import com.avast.android.shepherd.obfuscated.bv.ae;
import com.avast.android.shepherd.obfuscated.bv.aq;
import com.avast.android.shepherd.obfuscated.bv.c;
import com.avast.android.shepherd.obfuscated.bv.e;
import com.avast.android.shepherd.obfuscated.bv.e.a;
import com.avast.android.shepherd.obfuscated.bv.g;
import com.avast.android.shepherd.obfuscated.bv.i;
import com.avast.android.shepherd.obfuscated.bv.k;
import com.avast.android.shepherd.obfuscated.bv.q;
import com.avast.android.shepherd.obfuscated.bv.q.b;
import com.avast.android.shepherd.obfuscated.bv.s;
import com.avast.android.shepherd.obfuscated.bv.w;
import com.google.protobuf.ByteString;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* compiled from: Unknown */
public class ad {
    private final ExecutorService a = Executors.newFixedThreadPool(3);
    private final String b;
    private final String c;
    private final Context d;
    private Set<String> e;
    private final String f;
    private final ByteString g;
    private final boolean h;
    private final String i;
    private final String j;
    private final String k;
    private final String l;
    private final String m;
    private int n;
    private final ab o;

    public ad(Context context, Set<String> set) {
        this.d = context.getApplicationContext();
        this.o = ab.a(this.d);
        this.e = set;
        this.m = Shepherd.getConfig().getCommonConfig().getConfigVersion();
        Bundle paramsBundle = Shepherd.getParamsBundle();
        if (paramsBundle == null || paramsBundle.isEmpty()) {
            if (Shepherd.getSdkParamsBundles() != null) {
                for (Sdk sdk : Shepherd.getSdkParamsBundles().keySet()) {
                    paramsBundle = (Bundle) Shepherd.getSdkParamsBundles().get(sdk);
                }
            }
        }
        this.f = paramsBundle.getString(Shepherd.BUNDLE_PARAMS_OEM_PARTNER_STRING_KEY);
        this.h = paramsBundle.getBoolean(Shepherd.BUNDLE_PARAMS_IS_PREMIUM_BOOLEAN_KEY);
        this.i = paramsBundle.getString(Shepherd.BUNDLE_PARAMS_PARTNER_ID_KEY);
        this.j = paramsBundle.getString(Shepherd.BUNDLE_PARAMS_REFERRER_STRING_KEY);
        this.k = paramsBundle.getString(Shepherd.BUNDLE_PARAMS_AUID_STRING_KEY);
        this.l = paramsBundle.getString(Shepherd.BUNDLE_PARAMS_UUID_STRING_KEY);
        this.b = paramsBundle.getString(Shepherd.BUNDLE_PARAMS_HARDWARE_ID_STRING_KEY);
        this.c = paramsBundle.getString(Shepherd.BUNDLE_PARAMS_PROFILE_ID_STRING_KEY);
        this.g = a(paramsBundle.getString(Shepherd.BUNDLE_PARAMS_INSTALLATION_GUID_KEY));
    }

    private a a(Bundle bundle) {
        a g = e.g();
        ae.a i = ae.i();
        i.b(ByteString.copyFromUtf8(this.d.getPackageName()));
        if (this.g != null) {
            i.c(this.g);
        }
        if (bundle.containsKey(Shepherd.BUNDLE_PARAMS_SDK_API_KEY_KEY)) {
            i.a(ByteString.copyFromUtf8(bundle.getString(Shepherd.BUNDLE_PARAMS_SDK_API_KEY_KEY)));
        }
        g.a(i);
        if (bundle.containsKey(Shepherd.BUNDLE_PARAMS_AMS_VPS_VERSION_STRING_KEY)) {
            g.a(ByteString.copyFromUtf8(bundle.getString(Shepherd.BUNDLE_PARAMS_AMS_VPS_VERSION_STRING_KEY)));
        }
        return g;
    }

    private static ByteString a(String str) {
        ByteBuffer allocate = ByteBuffer.allocate(16);
        long leastSignificantBits = UUID.fromString(str).getLeastSignificantBits();
        long mostSignificantBits = UUID.fromString(str).getMostSignificantBits();
        allocate.putLong(leastSignificantBits);
        allocate.putLong(mostSignificantBits);
        return ByteString.copyFrom(allocate.array());
    }

    public static String a(String str, Bundle bundle, Map<Sdk, Bundle> map) {
        String string = bundle.getString(str);
        if (string == null) {
            for (Sdk sdk : map.keySet()) {
                string = ((Bundle) map.get(sdk)).getString(str);
                if (string != null) {
                    break;
                }
            }
        }
        return string;
    }

    public static boolean a(Context context) {
        return Secure.getInt(context.getContentResolver(), "install_non_market_apps", 0) != 0;
    }

    private aa.a b() {
        aa.a s = aa.s();
        TelephonyManager telephonyManager = (TelephonyManager) this.d.getSystemService("phone");
        if (!(telephonyManager == null || telephonyManager.getSimOperatorName() == null)) {
            s.b(ByteString.copyFromUtf8(telephonyManager.getSimOperatorName()));
        }
        if (!"".equals(Locale.getDefault().getCountry())) {
            s.a(ByteString.copyFromUtf8(Locale.getDefault().getCountry()));
        }
        s.d(ByteString.copyFromUtf8(this.b));
        s.c(ByteString.copyFromUtf8(this.m));
        s.e(ByteString.copyFromUtf8(this.c));
        if (Shepherd.getConfig().getCommonConfig().isFlagPresent("flag_experimentals_enabled")) {
            y.a a = y.a();
            Set j = j();
            if (!(j == null || j.isEmpty())) {
                a.a(j);
            }
            a.a(i());
            a.a(a(this.d));
            Object a2 = a.a();
            if (!TextUtils.isEmpty(a2)) {
                s.f(ByteString.copyFromUtf8(a2));
            }
        }
        s.a o = s.o();
        o.a(ByteString.copyFromUtf8(VERSION.RELEASE));
        o.a(VERSION.SDK_INT);
        o.d(ByteString.copyFromUtf8(Build.BRAND));
        o.b(ByteString.copyFromUtf8(Build.ID));
        o.e(ByteString.copyFromUtf8(Build.MANUFACTURER));
        o.c(ByteString.copyFromUtf8(Build.MODEL));
        s.a(o);
        return s;
    }

    private c.a b(Bundle bundle) {
        c.a e = c.e();
        ae.a i = ae.i();
        i.b(ByteString.copyFromUtf8(this.d.getPackageName()));
        if (this.g != null) {
            i.c(this.g);
        }
        if (bundle.containsKey(Shepherd.BUNDLE_PARAMS_SDK_API_KEY_KEY)) {
            i.a(ByteString.copyFromUtf8(bundle.getString(Shepherd.BUNDLE_PARAMS_SDK_API_KEY_KEY)));
        }
        e.a(i);
        return e;
    }

    private b c() {
        b i = q.i();
        ac.a u = ac.u();
        if (App.MOBILE_SECURITY.equals(Shepherd.currentCaller())) {
            i.a(!this.h ? q.a.FREE : q.a.PREMIUM);
            u.c(ByteString.copyFromUtf8(Locale.getDefault().getLanguage()));
            if (!TextUtils.isEmpty(this.f)) {
                u.d(ByteString.copyFromUtf8(this.f));
            }
            if (this.g != null) {
                u.b(this.g);
            }
            if (!TextUtils.isEmpty(this.i)) {
                u.e(ByteString.copyFromUtf8(this.i));
            }
            if (!TextUtils.isEmpty(this.j)) {
                u.h(ByteString.copyFromUtf8(this.j));
            }
            if (!TextUtils.isEmpty(this.k)) {
                u.g(ByteString.copyFromUtf8(this.k));
            }
            if (!TextUtils.isEmpty(this.l)) {
                u.f(ByteString.copyFromUtf8(this.l));
            }
        }
        try {
            PackageInfo packageInfo = this.d.getPackageManager().getPackageInfo("com.avast.android.mobilesecurity", 0);
            u.a(ByteString.copyFromUtf8(packageInfo.versionName));
            u.a(packageInfo.versionCode);
        } catch (NameNotFoundException e) {
        }
        i.a(u);
        if (Shepherd.getParamsBundle().containsKey(Shepherd.BUNDLE_PARAMS_AMS_VPS_VERSION_STRING_KEY)) {
            i.a(ByteString.copyFromUtf8(Shepherd.getParamsBundle().getString(Shepherd.BUNDLE_PARAMS_AMS_VPS_VERSION_STRING_KEY)));
        }
        return i;
    }

    private bv.a.b d() {
        PackageInfo packageInfo;
        bv.a.b g = bv.a.g();
        ac.a u = ac.u();
        if (App.ANTI_THEFT.equals(Shepherd.currentCaller())) {
            if (z.a(this.d, "com.avast.android.at_play")) {
                g.a(!this.h ? bv.a.a.SIMPLE_FREE : bv.a.a.SIMPLE_PREMIUM);
            } else {
                g.a(!this.h ? bv.a.a.ADVANCED_FREE : bv.a.a.ADVANCED_PREMIUM);
            }
            u.c(ByteString.copyFromUtf8(Locale.getDefault().getLanguage()));
            if (!TextUtils.isEmpty(this.f)) {
                u.d(ByteString.copyFromUtf8(this.f));
            }
            if (this.g != null) {
                u.b(this.g);
            }
            if (!TextUtils.isEmpty(this.i)) {
                u.e(ByteString.copyFromUtf8(this.i));
            }
            if (!TextUtils.isEmpty(this.j)) {
                u.h(ByteString.copyFromUtf8(this.j));
            }
            if (!TextUtils.isEmpty(this.k)) {
                u.g(ByteString.copyFromUtf8(this.k));
            }
            if (!TextUtils.isEmpty(this.l)) {
                u.f(ByteString.copyFromUtf8(this.l));
            }
        }
        try {
            packageInfo = this.d.getPackageManager().getPackageInfo("com.avast.android.at_play", 0);
            u.a(ByteString.copyFromUtf8(packageInfo.versionName));
            u.a(packageInfo.versionCode);
        } catch (NameNotFoundException e) {
            try {
                packageInfo = this.d.getPackageManager().getPackageInfo("com.avast.android.antitheft", 0);
                u.a(ByteString.copyFromUtf8(packageInfo.versionName));
                u.a(packageInfo.versionCode);
            } catch (NameNotFoundException e2) {
            }
        }
        g.a(u);
        return g;
    }

    private g.b e() {
        g.b g = g.g();
        ac.a u = ac.u();
        if (App.BACKUP.equals(Shepherd.currentCaller())) {
            g.a(!this.h ? g.a.FREE : g.a.PREMIUM);
            u.c(ByteString.copyFromUtf8(Locale.getDefault().getLanguage()));
            if (!TextUtils.isEmpty(this.f)) {
                u.d(ByteString.copyFromUtf8(this.f));
            }
            if (this.g != null) {
                u.b(this.g);
            }
            if (!TextUtils.isEmpty(this.i)) {
                u.e(ByteString.copyFromUtf8(this.i));
            }
            if (!TextUtils.isEmpty(this.j)) {
                u.h(ByteString.copyFromUtf8(this.j));
            }
            if (!TextUtils.isEmpty(this.k)) {
                u.g(ByteString.copyFromUtf8(this.k));
            }
            if (!TextUtils.isEmpty(this.l)) {
                u.f(ByteString.copyFromUtf8(this.l));
            }
        }
        try {
            PackageInfo packageInfo = this.d.getPackageManager().getPackageInfo("com.avast.android.backup", 0);
            u.a(ByteString.copyFromUtf8(packageInfo.versionName));
            u.a(packageInfo.versionCode);
        } catch (NameNotFoundException e) {
        }
        g.a(u);
        return g;
    }

    private w.a f() {
        w.a e = w.e();
        ac.a u = ac.u();
        if (App.SECURELINE.equals(Shepherd.currentCaller())) {
            u.c(ByteString.copyFromUtf8(Locale.getDefault().getLanguage()));
            if (!TextUtils.isEmpty(this.f)) {
                u.d(ByteString.copyFromUtf8(this.f));
            }
            if (this.g != null) {
                u.b(this.g);
            }
            if (!TextUtils.isEmpty(this.i)) {
                u.e(ByteString.copyFromUtf8(this.i));
            }
            if (!TextUtils.isEmpty(this.j)) {
                u.h(ByteString.copyFromUtf8(this.j));
            }
            if (!TextUtils.isEmpty(this.k)) {
                u.g(ByteString.copyFromUtf8(this.k));
            }
            if (!TextUtils.isEmpty(this.l)) {
                u.f(ByteString.copyFromUtf8(this.l));
            }
        }
        try {
            PackageInfo packageInfo = this.d.getPackageManager().getPackageInfo("com.avast.android.vpn", 0);
            u.a(ByteString.copyFromUtf8(packageInfo.versionName));
            u.a(packageInfo.versionCode);
        } catch (NameNotFoundException e2) {
        }
        e.a(u);
        return e;
    }

    private i.a g() {
        i.a e = i.e();
        ac.a u = ac.u();
        if (App.BATTERY_SAVER.equals(Shepherd.currentCaller())) {
            u.c(ByteString.copyFromUtf8(Locale.getDefault().getLanguage()));
            if (!TextUtils.isEmpty(this.f)) {
                u.d(ByteString.copyFromUtf8(this.f));
            }
            if (this.g != null) {
                u.b(this.g);
            }
            if (!TextUtils.isEmpty(this.i)) {
                u.e(ByteString.copyFromUtf8(this.i));
            }
            if (!TextUtils.isEmpty(this.j)) {
                u.h(ByteString.copyFromUtf8(this.j));
            }
            if (!TextUtils.isEmpty(this.k)) {
                u.g(ByteString.copyFromUtf8(this.k));
            }
            if (!TextUtils.isEmpty(this.l)) {
                u.f(ByteString.copyFromUtf8(this.l));
            }
        }
        try {
            PackageInfo packageInfo = this.d.getPackageManager().getPackageInfo("com.avast.android.batterysaver", 0);
            u.a(ByteString.copyFromUtf8(packageInfo.versionName));
            u.a(packageInfo.versionCode);
        } catch (NameNotFoundException e2) {
        }
        e.a(u);
        return e;
    }

    private k.a h() {
        k.a e = k.e();
        ac.a u = ac.u();
        if (App.CLEANER.equals(Shepherd.currentCaller())) {
            u.c(ByteString.copyFromUtf8(Locale.getDefault().getLanguage()));
            if (!TextUtils.isEmpty(this.f)) {
                u.d(ByteString.copyFromUtf8(this.f));
            }
            if (this.g != null) {
                u.b(this.g);
            }
            if (!TextUtils.isEmpty(this.i)) {
                u.e(ByteString.copyFromUtf8(this.i));
            }
            if (!TextUtils.isEmpty(this.j)) {
                u.h(ByteString.copyFromUtf8(this.j));
            }
            if (!TextUtils.isEmpty(this.k)) {
                u.g(ByteString.copyFromUtf8(this.k));
            }
            if (!TextUtils.isEmpty(this.l)) {
                u.f(ByteString.copyFromUtf8(this.l));
            }
        }
        try {
            PackageInfo packageInfo = this.d.getPackageManager().getPackageInfo("com.avast.android.cleaner", 0);
            u.a(ByteString.copyFromUtf8(packageInfo.versionName));
            u.a(packageInfo.versionCode);
        } catch (NameNotFoundException e2) {
        }
        e.a(u);
        return e;
    }

    private int i() {
        if (this.n == 0) {
            try {
                this.n = this.d.getPackageManager().getPackageInfo("com.google.android.gms", 0).versionCode;
            } catch (NameNotFoundException e) {
                return -1;
            }
        }
        return this.n;
    }

    private Set<String> j() {
        Set<String> c = this.o.c();
        this.a.execute(new ae(this));
        return c;
    }

    public aq a() {
        aq.a N = aq.N();
        App currentCaller = Shepherd.currentCaller();
        if (currentCaller != null) {
            N.a(currentCaller.getId());
        }
        if (!(this.e == null || this.e.isEmpty())) {
            for (String copyFromUtf8 : this.e) {
                N.a(ByteString.copyFromUtf8(copyFromUtf8));
            }
        }
        N.a(b());
        if (z.a(this.d)) {
            N.a(c());
        }
        if (z.b(this.d) != null || z.c(this.d)) {
            N.a(d());
        }
        if (z.d(this.d)) {
            N.a(e());
        }
        if (z.e(this.d)) {
            N.a(f());
        }
        if (z.f(this.d)) {
            N.a(g());
        }
        if (z.g(this.d)) {
            N.a(h());
        }
        Map sdkParamsBundles = Shepherd.getSdkParamsBundles();
        if (!(sdkParamsBundles == null || sdkParamsBundles.get(Sdk.AV_SDK) == null)) {
            N.a(a((Bundle) sdkParamsBundles.get(Sdk.AV_SDK)));
            N.a(Sdk.AV_SDK.getId());
        }
        if (!(sdkParamsBundles == null || sdkParamsBundles.get(Sdk.AT_SDK) == null)) {
            N.a(b((Bundle) sdkParamsBundles.get(Sdk.AT_SDK)));
            N.a(Sdk.AT_SDK.getId());
        }
        if (!N.f() && N.g() > 0) {
            N.a(aq.b.SDK);
        }
        return N.d();
    }
}
