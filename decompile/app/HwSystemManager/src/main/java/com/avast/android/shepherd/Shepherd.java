package com.avast.android.shepherd;

import android.content.Context;
import android.os.Bundle;
import com.avast.android.shepherd.obfuscated.ab;
import com.avast.android.shepherd.obfuscated.ac;
import com.avast.android.shepherd.obfuscated.ad;
import com.avast.android.shepherd.obfuscated.bv.aq;
import com.avast.android.shepherd.obfuscated.bv.aq.b;
import com.avast.android.shepherd.obfuscated.bv.aq.c;
import com.avast.android.shepherd.obfuscated.v;
import com.avast.android.shepherd.obfuscated.x;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* compiled from: Unknown */
public class Shepherd {
    public static final String BUNDLE_PARAMS_AMS_VPS_VERSION_STRING_KEY = "intent.extra.ams.VPS_VERSION";
    public static final String BUNDLE_PARAMS_AUID_STRING_KEY = "intent.extra.common.AUID";
    public static final String BUNDLE_PARAMS_HARDWARE_ID_STRING_KEY = "intent.extra.common.HARDWARE_ID";
    public static final String BUNDLE_PARAMS_INSTALLATION_GUID_KEY = "intent.extra.common.INSTALLATION_GUID";
    public static final String BUNDLE_PARAMS_IS_PREMIUM_BOOLEAN_KEY = "intent.extra.common.IS_PREMIUM";
    public static final String BUNDLE_PARAMS_OEM_PARTNER_STRING_KEY = "intent.extra.common.OEM_PARTNER";
    public static final String BUNDLE_PARAMS_PARTNER_ID_KEY = "intent.extra.common.PARTNER_ID";
    public static final String BUNDLE_PARAMS_PROFILE_ID_STRING_KEY = "intent.extra.common.PROFILE_ID";
    public static final String BUNDLE_PARAMS_REFERRER_STRING_KEY = "intent.extra.common.REFERRER";
    public static final String BUNDLE_PARAMS_SDK_API_KEY_KEY = "intent.extra.common.API_KEY";
    public static final String BUNDLE_PARAMS_UUID_STRING_KEY = "intent.extra.common.UUID";
    private static App a = null;
    private static ac b = null;
    private static Bundle c = new Bundle();
    private static Map<Sdk, Bundle> d = new HashMap();
    private static boolean e = false;
    private static boolean f = false;
    private static Context g;
    private static ShepherdConfig h;

    /* compiled from: Unknown */
    public enum App {
        MOBILE_SECURITY(b.AMS),
        ANTI_THEFT(b.AAT),
        BACKUP(b.ABCK),
        SECURELINE(b.ASL),
        BATTERY_SAVER(b.ABS),
        INSTALLER(b.AIN),
        RANSOMWARE_REMOVAL(b.ARR),
        DOWNLOAD_MANAGER(b.ADM),
        CLEANER(b.ACL),
        PASSWORD_MANAGER(b.APM),
        WIFI_FINDER(b.AWF);
        
        private static final HashMap<b, App> a = null;
        private final b b;

        static {
            a = new HashMap();
            Iterator it = EnumSet.allOf(App.class).iterator();
            while (it.hasNext()) {
                App app = (App) it.next();
                a.put(app.getId(), app);
            }
        }

        private App(b bVar) {
            this.b = bVar;
        }

        public static App get(b bVar) {
            return (App) a.get(bVar);
        }

        public final b getId() {
            return this.b;
        }
    }

    /* compiled from: Unknown */
    public enum Sdk {
        AV_SDK(c.SDK_AAV),
        AT_SDK(c.SDK_AAT),
        SL_SDK(c.SDK_ASL);
        
        private static final HashMap<c, Sdk> a = null;
        private final c b;

        static {
            a = new HashMap();
            Iterator it = EnumSet.allOf(Sdk.class).iterator();
            while (it.hasNext()) {
                Sdk sdk = (Sdk) it.next();
                a.put(sdk.getId(), sdk);
            }
        }

        private Sdk(c cVar) {
            this.b = cVar;
        }

        public static Sdk get(c cVar) {
            return (Sdk) a.get(cVar);
        }

        public final c getId() {
            return this.b;
        }
    }

    private static void a() {
        Bundle paramsBundle = getParamsBundle();
        Map sdkParamsBundles = getSdkParamsBundles();
        if (ad.a(BUNDLE_PARAMS_INSTALLATION_GUID_KEY, paramsBundle, sdkParamsBundles) == null) {
            throw new IllegalStateException("No installation GUID set. Use Shepherd.BUNDLE_PARAMS_INSTALLATION_GUID_KEY to put it to the params bundle.");
        } else if (ad.a(BUNDLE_PARAMS_HARDWARE_ID_STRING_KEY, paramsBundle, sdkParamsBundles) == null) {
            throw new IllegalStateException("No hardware id set. Use Shepherd.BUNDLE_PARAMS_HARDWARE_ID_STRING_KEY to put it to the params bundle.");
        } else if (ad.a(BUNDLE_PARAMS_PROFILE_ID_STRING_KEY, paramsBundle, sdkParamsBundles) == null) {
            throw new IllegalStateException("No profile id set. Use Shepherd.BUNDLE_PARAMS_PROFILE_ID_STRING_KEY to put it to the params bundle.");
        }
    }

    private static void a(Context context, boolean z) {
        g = context.getApplicationContext();
        if (z) {
            a();
            v.a(context).a(false);
        }
        x.a("avast! Shepherd");
        h = ShepherdConfig.a(context);
        b = ac.a(context);
    }

    private static void a(Bundle bundle) {
        if (bundle != null) {
            c.putAll(bundle);
        }
    }

    public static App currentCaller() {
        return a;
    }

    public static void forceUpdateShepherdConfig() {
        a();
        v.a(g).a(true);
    }

    public static synchronized ShepherdConfig getConfig() {
        ShepherdConfig shepherdConfig;
        synchronized (Shepherd.class) {
            if (f) {
                shepherdConfig = h;
            } else {
                throw new RuntimeException("You have to call init or initSdk first");
            }
        }
        return shepherdConfig;
    }

    public static synchronized DataLayer getDataLayer() {
        DataLayer a;
        synchronized (Shepherd.class) {
            if (f) {
                a = h.a();
            } else {
                throw new RuntimeException("You have to call init or initSdk first");
            }
        }
        return a;
    }

    public static Bundle getParamsBundle() {
        return c;
    }

    public static synchronized Map<Sdk, Bundle> getSdkParamsBundles() {
        Map<Sdk, Bundle> unmodifiableMap;
        synchronized (Shepherd.class) {
            unmodifiableMap = Collections.unmodifiableMap(d);
        }
        return unmodifiableMap;
    }

    public static synchronized aq getShepherdParamsProto() {
        aq a;
        synchronized (Shepherd.class) {
            if (f) {
                a = new ad(g, ab.a(g).b()).a();
            } else {
                throw new RuntimeException("You have to call init or initSdk first");
            }
        }
        return a;
    }

    public static synchronized void init(App app, Context context, Bundle bundle) {
        synchronized (Shepherd.class) {
            init(app, context, bundle, true);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void init(App app, Context context, Bundle bundle, boolean z) {
        synchronized (Shepherd.class) {
            if (app == null || context == null) {
                throw new IllegalArgumentException("Caller and context can't be null");
            } else if (!e) {
                a = app;
                a(bundle);
                a(context, z);
                e = true;
                f = true;
            } else if (!app.equals(a)) {
                throw new RuntimeException("Init already called with a different caller");
            } else if (c.isEmpty()) {
                a(bundle);
            }
        }
    }

    public static synchronized void initSdk(Sdk sdk, Context context, Bundle bundle) {
        synchronized (Shepherd.class) {
            initSdk(sdk, context, bundle, true);
        }
    }

    public static synchronized void initSdk(Sdk sdk, Context context, Bundle bundle, boolean z) {
        synchronized (Shepherd.class) {
            if (sdk == null || context == null) {
                throw new IllegalArgumentException("SDK and context can't be null");
            }
            Bundle bundle2 = (Bundle) d.get(sdk);
            if (bundle2 == null) {
                Object bundle3;
                Map map = d;
                if (bundle == null) {
                    bundle3 = new Bundle();
                }
                map.put(sdk, bundle3);
                a(context, z);
                f = true;
                return;
            }
            bundle2.putAll(bundle);
        }
    }

    public static void ping() {
        b.a();
    }

    public static synchronized void updateParams(Bundle bundle) {
        synchronized (Shepherd.class) {
            if (e) {
                a(bundle);
                getConfig().a(getParamsBundle());
                if (!bundle.containsKey(BUNDLE_PARAMS_REFERRER_STRING_KEY)) {
                    if (!(bundle.containsKey(BUNDLE_PARAMS_AUID_STRING_KEY) || bundle.containsKey(BUNDLE_PARAMS_UUID_STRING_KEY))) {
                    }
                }
                forceUpdateShepherdConfig();
            } else {
                throw new RuntimeException("You have to call init first");
            }
        }
    }

    public static synchronized void updateSdkParams(Sdk sdk, Bundle bundle) {
        synchronized (Shepherd.class) {
            if (sdk == null) {
                throw new IllegalArgumentException("SDK can't be null");
            } else if (bundle != null) {
                Bundle bundle2 = (Bundle) d.get(sdk);
                if (f && bundle2 != null) {
                    bundle2.putAll(bundle);
                    getConfig().a(bundle2);
                    return;
                }
                throw new RuntimeException("You have to call initSdk first");
            }
        }
    }

    public static void updateShepherdConfig() {
        a();
        v.a(g).a(false);
    }
}
