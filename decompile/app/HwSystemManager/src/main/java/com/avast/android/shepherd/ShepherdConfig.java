package com.avast.android.shepherd;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.avast.android.shepherd.Shepherd.Sdk;
import com.avast.android.shepherd.obfuscated.bc;
import com.avast.android.shepherd.obfuscated.bc.aa;
import com.avast.android.shepherd.obfuscated.bc.ab;
import com.avast.android.shepherd.obfuscated.bc.ah;
import com.avast.android.shepherd.obfuscated.bc.c;
import com.avast.android.shepherd.obfuscated.bc.g;
import com.avast.android.shepherd.obfuscated.bc.m;
import com.avast.android.shepherd.obfuscated.bc.o;
import com.avast.android.shepherd.obfuscated.bc.q;
import com.avast.android.shepherd.obfuscated.v;
import com.avast.android.shepherd.obfuscated.v.a;
import com.google.protobuf.ByteString;
import com.huawei.permissionmanager.db.DBHelper;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/* compiled from: Unknown */
public class ShepherdConfig implements a {
    private static final List<WeakReference<OnConfigChangedListener>> a = new LinkedList();
    private static ShepherdConfig b = null;
    private String c;
    private final q d = DefaultConfigFactory.getDefaultConfig();
    private q e;
    private CommonConfig f = null;
    private AmsConfig g = null;
    private AbckConfig h = null;
    private BurgerConfig i = null;
    private DataLayer j;

    /* compiled from: Unknown */
    public static final class AbckConfig {
        private final c a;
        private c b;

        private AbckConfig(q qVar, q qVar2) {
            if (qVar.k()) {
                this.a = qVar.l();
                b(qVar2);
                return;
            }
            throw new IllegalArgumentException("Default config must have ABCK config set");
        }

        private void b(q qVar) {
            if (qVar != null && qVar.k()) {
                this.b = qVar.l();
            }
        }

        protected void a(q qVar) {
            b(qVar);
        }

        public String getBackupBackendServerAddress() {
            synchronized (ShepherdConfig.b) {
                if (this.b != null && this.b.c()) {
                    String toStringUtf8 = this.b.d().toStringUtf8();
                    return toStringUtf8;
                }
                toStringUtf8 = this.a.d().toStringUtf8();
                return toStringUtf8;
            }
        }
    }

    /* compiled from: Unknown */
    public static final class AmsConfig {
        private final g a;
        private g b;

        private AmsConfig(q qVar, q qVar2) {
            if (qVar.e()) {
                this.a = qVar.f();
                b(qVar2);
                return;
            }
            throw new IllegalArgumentException("Default config must have AMS config set");
        }

        private void b(q qVar) {
            if (qVar != null && qVar.e()) {
                this.b = qVar.f();
            }
        }

        protected void a(q qVar) {
            b(qVar);
        }

        public URI getAccountPairingUrl() {
            URI uri;
            synchronized (ShepherdConfig.b) {
                String toStringUtf8 = (this.b != null && this.b.e()) ? this.b.f().toStringUtf8() : this.a.f().toStringUtf8();
                uri = new URI(toStringUtf8);
            }
            return uri;
        }

        public URI getAccountUnpairingUrl() {
            URI uri;
            synchronized (ShepherdConfig.b) {
                String toStringUtf8 = (this.b != null && this.b.g()) ? this.b.h().toStringUtf8() : this.a.h().toStringUtf8();
                uri = new URI(toStringUtf8);
            }
            return uri;
        }

        public URI getAiReportingUrl() {
            URI uri;
            synchronized (ShepherdConfig.b) {
                String toStringUtf8 = (this.b != null && this.b.i()) ? this.b.j().toStringUtf8() : this.a.j().toStringUtf8();
                uri = new URI(toStringUtf8);
            }
            return uri;
        }

        public URI getCommunityIQUrl() {
            URI uri;
            synchronized (ShepherdConfig.b) {
                String toStringUtf8 = (this.b != null && this.b.k()) ? this.b.l().toStringUtf8() : this.a.l().toStringUtf8();
                uri = new URI(toStringUtf8);
            }
            return uri;
        }

        public URI getFalsePositiveUrl() {
            URI uri;
            synchronized (ShepherdConfig.b) {
                String toStringUtf8 = (this.b != null && this.b.q()) ? this.b.r().toStringUtf8() : this.a.r().toStringUtf8();
                uri = new URI(toStringUtf8);
            }
            return uri;
        }

        public int getIntervalDefaultValue() {
            int X;
            synchronized (ShepherdConfig.b) {
                X = (this.b != null && this.b.W()) ? this.b.X() : this.a.X();
            }
            return X;
        }

        public int getIntervalInVehicle() {
            int T;
            synchronized (ShepherdConfig.b) {
                T = (this.b != null && this.b.S()) ? this.b.T() : this.a.T();
            }
            return T;
        }

        public int getIntervalInVehicleCharger() {
            int V;
            synchronized (ShepherdConfig.b) {
                V = (this.b != null && this.b.U()) ? this.b.V() : this.a.V();
            }
            return V;
        }

        public int getIntervalMoving() {
            int R;
            synchronized (ShepherdConfig.b) {
                R = (this.b != null && this.b.Q()) ? this.b.R() : this.a.R();
            }
            return R;
        }

        public int getIntervalSendResult() {
            int N;
            synchronized (ShepherdConfig.b) {
                N = (this.b != null && this.b.M()) ? this.b.N() : this.a.N();
            }
            return N;
        }

        public int getIntervalStill() {
            int P;
            synchronized (ShepherdConfig.b) {
                P = (this.b != null && this.b.O()) ? this.b.P() : this.a.P();
            }
            return P;
        }

        public URI getOfferwallContentServerUrl() {
            URI uri;
            synchronized (ShepherdConfig.b) {
                String toStringUtf8 = (this.b != null && this.b.A()) ? this.b.B().toStringUtf8() : this.a.B().toStringUtf8();
                uri = new URI(toStringUtf8);
            }
            return uri;
        }

        public boolean getPerformSurroundScan() {
            boolean Z;
            synchronized (ShepherdConfig.b) {
                this.b.J();
                Z = (this.b != null && this.b.Y()) ? this.b.Z() : this.a.Z();
            }
            return Z;
        }

        public boolean getSearchOpenWifi() {
            boolean L;
            synchronized (ShepherdConfig.b) {
                this.b.J();
                L = (this.b != null && this.b.K()) ? this.b.L() : this.a.L();
            }
            return L;
        }

        public URI getTyposquattingUrl() {
            URI uri;
            synchronized (ShepherdConfig.b) {
                String toStringUtf8 = (this.b != null && this.b.o()) ? this.b.p().toStringUtf8() : this.a.p().toStringUtf8();
                uri = new URI(toStringUtf8);
            }
            return uri;
        }

        public URI getVpsUpdateUrl() {
            URI uri;
            synchronized (ShepherdConfig.b) {
                String toStringUtf8 = (this.b != null && this.b.c()) ? this.b.d().toStringUtf8() : this.a.d().toStringUtf8();
                uri = new URI(toStringUtf8);
            }
            return uri;
        }

        public URI getWebshieldUrl() {
            URI uri;
            synchronized (ShepherdConfig.b) {
                String toStringUtf8 = (this.b != null && this.b.m()) ? this.b.n().toStringUtf8() : this.a.n().toStringUtf8();
                uri = new URI(toStringUtf8);
            }
            return uri;
        }

        public ah getWifiScannerApi() {
            ah J;
            synchronized (ShepherdConfig.b) {
                J = (this.b != null && this.b.I()) ? this.b.J() : this.a.J();
            }
            return J;
        }

        public boolean shouldShowBatterySaverCampaign() {
            synchronized (ShepherdConfig.b) {
                if (this.b != null && this.b.w()) {
                    boolean x = this.b.x();
                    return x;
                }
                x = this.a.x();
                return x;
            }
        }

        public boolean shouldShowBatterySaverOnDashboard() {
            synchronized (ShepherdConfig.b) {
                if (this.b != null && this.b.y()) {
                    boolean z = this.b.z();
                    return z;
                }
                z = this.a.z();
                return z;
            }
        }

        public boolean shouldShowCleanerOnDashboard() {
            synchronized (ShepherdConfig.b) {
                if (this.b != null && this.b.G()) {
                    boolean H = this.b.H();
                    return H;
                }
                H = this.a.H();
                return H;
            }
        }

        public boolean shouldShowGrimeFighterCampaign() {
            synchronized (ShepherdConfig.b) {
                if (this.b != null && this.b.C()) {
                    boolean D = this.b.D();
                    return D;
                }
                D = this.a.D();
                return D;
            }
        }

        public boolean shouldShowGrimeFighterOnDashboard() {
            synchronized (ShepherdConfig.b) {
                if (this.b != null && this.b.E()) {
                    boolean F = this.b.F();
                    return F;
                }
                F = this.a.F();
                return F;
            }
        }

        public boolean shouldShowSecurelineCampaignNotification() {
            synchronized (ShepherdConfig.b) {
                if (this.b != null && this.b.u()) {
                    boolean v = this.b.v();
                    return v;
                }
                v = this.a.v();
                return v;
            }
        }

        public boolean shouldShowSecurelineCampaignPopup() {
            synchronized (ShepherdConfig.b) {
                if (this.b != null && this.b.s()) {
                    boolean t = this.b.t();
                    return t;
                }
                t = this.a.t();
                return t;
            }
        }
    }

    /* compiled from: Unknown */
    public static final class BurgerConfig {
        private final m a;
        private m b;

        private BurgerConfig(q qVar, q qVar2) {
            if (qVar.c() && qVar.d().t()) {
                this.a = qVar.d().u();
                b(qVar2);
                return;
            }
            throw new IllegalArgumentException("Default config must have common config and burger config set");
        }

        private void b(q qVar) {
            if (qVar != null && qVar.c() && qVar.d().t()) {
                this.b = qVar.d().u();
            }
        }

        protected void a(q qVar) {
            b(qVar);
        }

        public int getEnvelopeCapacity() {
            synchronized (ShepherdConfig.b) {
                if (this.b != null && this.b.e()) {
                    int f = this.b.f();
                    return f;
                }
                f = this.a.f();
                return f;
            }
        }

        public int getIntervalSending() {
            synchronized (ShepherdConfig.b) {
                if (this.b != null && this.b.g()) {
                    int h = this.b.h();
                    return h;
                }
                h = this.a.h();
                return h;
            }
        }

        public int getQueueCapacity() {
            synchronized (ShepherdConfig.b) {
                if (this.b != null && this.b.c()) {
                    int d = this.b.d();
                    return d;
                }
                d = this.a.d();
                return d;
            }
        }

        public List<ByteString> getTopicFilteringRules() {
            synchronized (ShepherdConfig.b) {
                if (this.b == null) {
                    List<ByteString> i = this.a.i();
                    return i;
                }
                i = this.b.i();
                return i;
            }
        }
    }

    /* compiled from: Unknown */
    public final class CommonConfig {
        final /* synthetic */ ShepherdConfig a;
        private final o b;
        private o c;

        private CommonConfig(ShepherdConfig shepherdConfig, q qVar, q qVar2) {
            this.a = shepherdConfig;
            this.c = null;
            if (qVar.c()) {
                this.b = qVar.d();
                b(qVar2);
                return;
            }
            throw new IllegalArgumentException("Default config must have common config set");
        }

        private void b(q qVar) {
            if (qVar != null && qVar.c()) {
                this.c = qVar.d();
            }
        }

        protected void a(q qVar) {
            b(qVar);
            this.a.j = new DataLayer(qVar);
        }

        public String getBillingServerAddress() {
            synchronized (ShepherdConfig.b) {
                if (this.c != null && this.c.k()) {
                    String toStringUtf8 = this.c.l().toStringUtf8();
                    return toStringUtf8;
                }
                toStringUtf8 = this.b.l().toStringUtf8();
                return toStringUtf8;
            }
        }

        public String getConfigVersion() {
            synchronized (ShepherdConfig.b) {
                if (this.c != null && this.c.c()) {
                    String toStringUtf8 = this.c.d().toStringUtf8();
                    return toStringUtf8;
                }
                toStringUtf8 = this.b.d().toStringUtf8();
                return toStringUtf8;
            }
        }

        public List<ab> getLoggingRules() {
            synchronized (ShepherdConfig.b) {
                if (this.c != null && this.c.h() > 0) {
                    List<ab> g = this.c.g();
                    return g;
                }
                g = this.b.g();
                return g;
            }
        }

        public aa getTemporaryLogAllowedLoggingLevel() {
            synchronized (ShepherdConfig.b) {
                if (this.c != null && this.c.i()) {
                    aa j = this.c.j();
                    return j;
                }
                j = this.b.j();
                return j;
            }
        }

        public List<ByteString> getTrackingFilterRules() {
            synchronized (ShepherdConfig.b) {
                if (this.c != null && this.c.h() > 0) {
                    List<ByteString> q = this.c.q();
                    return q;
                }
                q = this.b.q();
                return q;
            }
        }

        public boolean isABTestEnabled(String str) {
            synchronized (ShepherdConfig.b) {
                for (bc.a aVar : this.c == null ? this.b.o() : this.c.o()) {
                    if (aVar.c() && aVar.d().toStringUtf8().equalsIgnoreCase(str)) {
                        return true;
                    }
                }
                return false;
            }
        }

        public boolean isFlagPresent(String str) {
            boolean z = false;
            synchronized (ShepherdConfig.b) {
                List p = this.c == null ? this.b.p() : this.c.p();
                if (p != null && p.contains(ByteString.copyFromUtf8(str))) {
                    z = true;
                }
            }
            return z;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean isInBTestingFraction(String str) {
            synchronized (ShepherdConfig.b) {
                for (bc.a aVar : this.c == null ? this.b.o() : this.c.o()) {
                    if (aVar.c() && aVar.d().toStringUtf8().equalsIgnoreCase(str) && aVar.e()) {
                        boolean z = ((float) (Math.abs(UUID.fromString(this.a.c).getLeastSignificantBits()) % DBHelper.HISTORY_MAX_SIZE)) < aVar.f() * 100.0f;
                    }
                }
                return false;
            }
        }

        public boolean useSandboxShepherd() {
            synchronized (ShepherdConfig.b) {
                if (this.c != null && this.c.m()) {
                    boolean n = this.c.n();
                    return n;
                }
                n = this.b.n();
                return n;
            }
        }

        public boolean useStagingShepherd() {
            synchronized (ShepherdConfig.b) {
                if (this.c != null && this.c.e()) {
                    boolean f = this.c.f();
                    return f;
                }
                f = this.b.f();
                return f;
            }
        }
    }

    /* compiled from: Unknown */
    public interface OnConfigChangedListener {
        void onConfigChanged(ShepherdConfig shepherdConfig);
    }

    private ShepherdConfig(Context context) {
        this.e = com.avast.android.shepherd.obfuscated.aa.a(context);
        v.a(context).a((a) this);
    }

    protected static synchronized ShepherdConfig a(Context context) {
        ShepherdConfig shepherdConfig;
        synchronized (ShepherdConfig.class) {
            if (b == null) {
                b = new ShepherdConfig(context);
                b.a(Shepherd.getParamsBundle());
                Map sdkParamsBundles = Shepherd.getSdkParamsBundles();
                for (Sdk sdk : sdkParamsBundles.keySet()) {
                    b.a((Bundle) sdkParamsBundles.get(sdk));
                }
            }
            shepherdConfig = b;
        }
        return shepherdConfig;
    }

    private static void b(Context context) {
        Iterator it = a.iterator();
        while (it.hasNext()) {
            OnConfigChangedListener onConfigChangedListener = (OnConfigChangedListener) ((WeakReference) it.next()).get();
            if (onConfigChangedListener != null) {
                onConfigChangedListener.onConfigChanged(a(context));
            } else {
                it.remove();
            }
        }
    }

    public static synchronized void registerOnConfigChangedListener(OnConfigChangedListener onConfigChangedListener) {
        synchronized (ShepherdConfig.class) {
            if (onConfigChangedListener != null) {
                a.add(new WeakReference(onConfigChangedListener));
                return;
            }
        }
    }

    public static synchronized void unregisterAllOnConfigChangedListeners() {
        synchronized (ShepherdConfig.class) {
            a.clear();
        }
    }

    public static synchronized void unregisterOnConfigChangedListener(OnConfigChangedListener onConfigChangedListener) {
        synchronized (ShepherdConfig.class) {
            if (onConfigChangedListener != null) {
                Iterator it = a.iterator();
                while (it.hasNext()) {
                    OnConfigChangedListener onConfigChangedListener2 = (OnConfigChangedListener) ((WeakReference) it.next()).get();
                    if (onConfigChangedListener2 != null) {
                        if (!onConfigChangedListener2.equals(onConfigChangedListener)) {
                        }
                    }
                    it.remove();
                }
                return;
            }
        }
    }

    protected DataLayer a() {
        if (this.j == null) {
            synchronized (b) {
                if (this.e == null) {
                    this.j = new DataLayer(this.d);
                } else {
                    this.j = new DataLayer(this.e);
                }
            }
        }
        return this.j;
    }

    protected void a(Bundle bundle) {
        synchronized (b) {
            if (TextUtils.isEmpty(this.c)) {
                this.c = bundle.getString(Shepherd.BUNDLE_PARAMS_INSTALLATION_GUID_KEY);
            }
        }
    }

    public synchronized AbckConfig getAbckConfig() {
        if (this.h == null) {
            this.h = new AbckConfig(this.d, this.e);
        }
        return this.h;
    }

    public synchronized AmsConfig getAmsConfig() {
        if (this.g == null) {
            this.g = new AmsConfig(this.d, this.e);
        }
        return this.g;
    }

    public synchronized BurgerConfig getBurgerConfig() {
        if (this.i == null) {
            this.i = new BurgerConfig(this.d, this.e);
        }
        return this.i;
    }

    public synchronized CommonConfig getCommonConfig() {
        if (this.f == null) {
            this.f = new CommonConfig(this.d, this.e);
        }
        return this.f;
    }

    public void onNewConfigDownloaded(Context context, byte[] bArr) {
        this.e = q.a(bArr);
        com.avast.android.shepherd.obfuscated.aa.a(context, bArr);
        if (this.f != null) {
            this.f.a(this.e);
        }
        if (this.g != null) {
            this.g.a(this.e);
        }
        if (this.h != null) {
            this.h.a(this.e);
        }
        if (this.i != null) {
            this.i.a(this.e);
        }
        b(context);
    }
}
