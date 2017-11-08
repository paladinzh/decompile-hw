package com.fyusion.sdk.share;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import com.a.a.a.j;
import com.a.a.a.k;
import com.a.a.d;
import com.a.a.m;
import com.a.a.n.b;
import com.a.a.n.c;
import com.a.a.s;
import com.fyusion.sdk.common.ext.e;
import com.fyusion.sdk.common.ext.l;
import com.fyusion.sdk.processor.g;
import com.fyusion.sdk.share.exception.ConnectionException;
import com.fyusion.sdk.share.exception.ServerException;
import com.huawei.watermark.manager.parse.WMElement;
import fyusion.vislib.FyusePlacemark;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
class h extends AsyncTask<Void, Void, Boolean> {
    private static final String C = h.class.getSimpleName();
    private static String u;
    private e A = null;
    private volatile boolean B = false;
    private boolean D = false;
    private String E;
    private g F;
    private int G = 0;
    private int H = 0;
    private int I = 0;
    private int J = 0;
    private String K;
    private Context L;
    private m M;
    private d N = null;
    public long a = 0;
    public long b = 0;
    public boolean c = false;
    public boolean d = false;
    public boolean e = false;
    boolean f = false;
    private int g = 0;
    private Semaphore h = new Semaphore(1);
    private final ArrayList<j> i = new ArrayList();
    private String j;
    private int k;
    private String l = "";
    private int m = 0;
    private int n = 0;
    private int o = 0;
    private int p = 0;
    private long q = 0;
    private long r = 0;
    private f s;
    private boolean t = false;
    private a v = a.WaitForAuthentication;
    private String w;
    private String x;
    private float y = 0.0f;
    private float z = 0.0f;

    /* compiled from: Unknown */
    private enum a {
        WaitForAuthentication,
        FetchSession,
        WaitForMagic,
        UploadMagic,
        UploadTween,
        WaitForSlices,
        UploadSlices,
        WaitForButton,
        SendDone,
        DoneWasSent,
        ExitWithSuccess,
        ExitWithError,
        ExitWithUserCancel,
        Exit;
        
        private static a[] o;

        static {
            o = values();
        }

        private a a() {
            return ordinal() < ExitWithSuccess.ordinal() ? o[(ordinal() + 1) % o.length] : o[ordinal()];
        }
    }

    h(Context context, File file, boolean z, String str, String str2) {
        ((ThreadPoolExecutor) AsyncTask.THREAD_POOL_EXECUTOR).setMaximumPoolSize(128);
        this.F = new g(new l(com.fyusion.sdk.common.ext.g.a(), file));
        this.f = z;
        this.w = str;
        this.E = this.E;
        this.L = context;
        this.x = str2;
    }

    private ConnectionException a(int i, Exception exception) {
        return a(i, exception.getMessage());
    }

    private ConnectionException a(int i, String str) {
        this.k = i;
        return new ConnectionException(i + " " + str);
    }

    private static String a(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            int read;
            MessageDigest instance = MessageDigest.getInstance("md5");
            byte[] bArr = new byte[FragmentTransaction.TRANSIT_EXIT_MASK];
            while (inputStream != null) {
                read = inputStream.read(bArr);
                if (read > 0) {
                    instance.update(bArr, 0, read);
                }
            }
            try {
                byte[] digest = instance.digest();
                StringBuilder stringBuilder = new StringBuilder();
                for (byte b : digest) {
                    stringBuilder.append(Integer.toString((b & 255) + 256, 16).substring(1));
                }
                String stringBuilder2 = stringBuilder.toString();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                    }
                }
                return stringBuilder2;
            } catch (IOException e2) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                    }
                }
                return null;
            } catch (Throwable th) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e4) {
                    }
                }
            }
        } catch (NoSuchAlgorithmException e5) {
            return null;
        }
    }

    private <T> void a(com.a.a.l<T> lVar) {
        lVar.a(new d(30000, 0, WMElement.CAMERASIZEVALUE1B1));
        j().a(lVar);
    }

    private void a(Exception exception) {
        if (!isCancelled() && !p()) {
            if (l()) {
                m();
            } else {
                b(exception);
            }
        }
    }

    private void a(String str) throws JSONException {
        JSONObject jSONObject = new JSONObject(str);
        if (jSONObject.getInt("success") <= 0) {
            b(a(28, "Error"));
        } else if (jSONObject.getString("a").equals("no")) {
            b(a(27, "Server NO"));
        } else {
            String string = jSONObject.getString("a");
            String string2 = jSONObject.getString("c");
            this.v = a.ExitWithSuccess;
            if (isCancelled()) {
                this.v = a.Exit;
                return;
            }
            if (this.s != null) {
                this.s.b(string, string2);
                try {
                    InputStream e = this.F.e();
                    Options options = new Options();
                    options.inSampleSize = 2;
                    this.s.a(string, BitmapFactory.decodeStream(e, null, options));
                    e.close();
                } catch (Exception e2) {
                    this.s.a(string, null);
                }
            }
            s();
        }
    }

    private void a(JSONObject jSONObject) throws JSONException {
        if (jSONObject.has("success") && jSONObject.getInt("success") > 0) {
            this.j = jSONObject.getString("msg");
            if (jSONObject.has("end")) {
                String string = jSONObject.getString("end");
                if (string.startsWith("http")) {
                    u = string;
                }
            }
            if (this.j.equals("UPLOADS_OFF")) {
                this.j = null;
                b(new ServerException("(3) UPLOADS OFF"));
            } else {
                b(3);
                this.l = !jSONObject.has("path") ? "" : jSONObject.getString("path");
                if (this.s != null) {
                    this.s.a(this.j, this.l);
                }
                this.v = this.v.a();
                b(true);
            }
        }
        if (jSONObject.has("error") && jSONObject.getInt("error") == 1) {
            b(a(4, "Server responded with error while fetching session. " + jSONObject.toString()));
        }
    }

    private void b(int i) {
        this.I += i;
        if (this.s != null && !p()) {
            this.s.b(this.I + this.J);
        }
    }

    private void b(Exception exception) {
        if (!isCancelled() && !p()) {
            this.v = a.ExitWithError;
            if (this.s != null) {
                this.s.a(exception);
            }
        }
    }

    private void b(boolean z) {
        if (z) {
            this.g = 0;
        }
        this.B = true;
    }

    private boolean b(String str) {
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (jSONObject.has("hash")) {
                CharSequence string = jSONObject.getString("hash");
                if (this.K != null && this.K.contains(string)) {
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void c(int i) {
        this.J = i;
        if (this.s != null && !p()) {
            this.s.b(this.I + this.J);
        }
    }

    private static void e() {
        String k = com.fyusion.sdk.common.a.k();
        if (k != null) {
            if (!k.startsWith("http")) {
            }
            u = k;
            u += "api/1.4/";
        }
        k = "https://www.fyu.se/";
        u = k;
        u += "api/1.4/";
    }

    private static String f() {
        return u;
    }

    private boolean g() {
        return this.v == a.WaitForAuthentication || this.v == a.WaitForMagic || this.v == a.WaitForSlices || this.v == a.WaitForButton;
    }

    private void h() {
        b(false);
    }

    private void i() {
        String m = com.fyusion.sdk.common.a.a().m();
        if (m == null || m.isEmpty()) {
            this.v = a.WaitForAuthentication;
            h();
            return;
        }
        a(new com.a.a.a.g(this, 1, f() + "uploads/session?" + "access_token=" + m + "&key=" + g.a(m), null, new c<JSONObject>(this) {
            final /* synthetic */ h a;

            {
                this.a = r1;
            }

            public void a(JSONObject jSONObject) {
                try {
                    if (!this.a.p()) {
                        this.a.a(jSONObject);
                    }
                } catch (Exception e) {
                    this.a.b(this.a.a(1, e));
                }
            }
        }, new b(this) {
            final /* synthetic */ h a;

            {
                this.a = r1;
            }

            public void a(s sVar) {
                this.a.a(this.a.a(2, "network error while fetching session"));
            }
        }) {
            final /* synthetic */ h a;

            protected Map<String, String> m() {
                Map<String, String> hashMap = new HashMap();
                if (!(this.a.w == null || this.a.w.isEmpty())) {
                    hashMap.put("description", this.a.w);
                }
                hashMap.put("privacy", !this.a.f ? "0" : "1");
                return hashMap;
            }
        });
    }

    private m j() {
        if (this.M == null) {
            this.M = k.a(this.L);
            this.M.a();
        }
        return this.M;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void k() {
        boolean z = true;
        if (this.v == a.WaitForAuthentication) {
            if (com.fyusion.sdk.common.a.a().m() != null) {
                this.v = this.v.a();
                h();
            }
            return;
        } else if (this.v == a.FetchSession) {
            i();
            return;
        } else if (this.v == a.UploadSlices) {
            if (this.n == 0 && this.d && this.x != null) {
                a(this.A.getNumberOfSlices());
                for (int i = 0; i < this.n; i++) {
                    a(i, true);
                }
            }
            int i2 = 0;
            while (i2 < this.n) {
                if (!isCancelled() && !p()) {
                    synchronized (this.i) {
                        if (this.i.size() != 0 && this.i.size() >= i2 + 1) {
                            final j jVar = (j) this.i.get(i2);
                        } else {
                            h();
                            return;
                        }
                    }
                }
                return;
            }
            this.v = this.v.a();
            b(true);
            return;
        } else if (this.v == a.WaitForMagic && this.c) {
            try {
                r0 = this.F.c();
                try {
                    this.q = (long) r0.available();
                    this.v = this.v.a();
                    r0.close();
                    b(true);
                } catch (Exception e) {
                    b(a(11, e));
                }
                return;
            } catch (FileNotFoundException e2) {
                b(a(10, "no magic"));
                return;
            }
        } else if (this.v == a.WaitForSlices && this.d) {
            this.v = this.v.a();
            h();
            return;
        } else if (this.v == a.WaitForButton && this.e) {
            this.v = this.v.a();
            h();
            return;
        } else {
            String str;
            d dVar;
            if (this.v == a.UploadMagic) {
                try {
                    r0 = this.F.c();
                    try {
                        if (this.x != null) {
                            this.A = this.F.d();
                            FyusePlacemark placemark = this.A.getPlacemark();
                            this.y = placemark.getLatitude();
                            this.z = placemark.getLongitude();
                        }
                        str = f() + "uploads/magic?" + "access_token=" + com.fyusion.sdk.common.a.a().m() + "&key=" + g.a(com.fyusion.sdk.common.a.a().m()) + "&id=" + this.j;
                        try {
                            this.K = a(this.F.c());
                        } catch (IOException e3) {
                            this.K = null;
                            Log.d(C, "md5 magic: ioexception");
                        }
                        dVar = new d(r0, str, new e(this) {
                            final /* synthetic */ h b;

                            public void a() {
                                try {
                                    r0.close();
                                } catch (Exception e) {
                                    this.b.b(this.b.a(30, e));
                                }
                                this.b.h.release(1);
                            }

                            public void a(long j, long j2) {
                            }

                            public void a(Exception exception) {
                                this.b.h.release(1);
                                this.b.a(this.b.a(16, exception));
                            }

                            public void a(String str) {
                                if (this.b.b(str)) {
                                    this.b.b(2);
                                    this.b.v = this.b.v.a();
                                    this.b.b(true);
                                    return;
                                }
                                this.b.a(this.b.a(99, "magic uploading incorrect hash"));
                            }

                            public void a(boolean z, long j) {
                                this.b.h.release(1);
                                try {
                                    r0.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        try {
                            synchronized (this.h) {
                                if (!isCancelled()) {
                                    if (!p()) {
                                        this.h.acquireUninterruptibly();
                                        dVar.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
                                        this.N = dVar;
                                    }
                                }
                                this.N = null;
                            }
                        } catch (RejectedExecutionException e4) {
                            h();
                            return;
                        }
                    } catch (Exception e5) {
                        b(a(13, e5));
                        return;
                    }
                } catch (FileNotFoundException e6) {
                    b(a(12, "no magic"));
                    return;
                }
            }
            if (this.v == a.UploadTween) {
                try {
                    r0 = this.F.f();
                    try {
                        int available = r0.available();
                        if (available > 0) {
                            this.r = (long) available;
                            str = f() + "uploads/tween?" + "access_token=" + com.fyusion.sdk.common.a.a().m() + "&key=" + g.a(com.fyusion.sdk.common.a.a().m()) + "&id=" + this.j;
                            try {
                                this.K = a(this.F.f());
                            } catch (IOException e7) {
                                this.K = null;
                                Log.d(C, "md5 tween: ioexception");
                            }
                            dVar = new d(r0, str, new e(this) {
                                final /* synthetic */ h b;

                                public void a() {
                                    try {
                                        r0.close();
                                    } catch (Exception e) {
                                        this.b.b(this.b.a(32, e));
                                    }
                                    this.b.h.release();
                                }

                                public void a(long j, long j2) {
                                }

                                public void a(Exception exception) {
                                    this.b.h.release();
                                    this.b.a(this.b.a(18, exception));
                                }

                                public void a(String str) {
                                    if (this.b.b(str)) {
                                        this.b.b(2);
                                        this.b.v = this.b.v.a();
                                        this.b.b(true);
                                        return;
                                    }
                                    this.b.a(this.b.a(98, "tween uploading incorrect hash"));
                                }

                                public void a(boolean z, long j) {
                                    this.b.h.release();
                                    try {
                                        r0.close();
                                    } catch (Exception e) {
                                        this.b.b(this.b.a(31, e));
                                    }
                                }
                            });
                            try {
                                dVar.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
                                this.N = dVar;
                            } catch (RejectedExecutionException e8) {
                            }
                        }
                    } catch (Exception e52) {
                        b(a(20, e52));
                        return;
                    }
                } catch (Throwable e9) {
                    Log.w(C, "Tween file is not found.", e9);
                    b(2);
                    this.v = this.v.a();
                    b(true);
                    return;
                }
            }
            if (this.v == a.SendDone) {
                this.v = a.DoneWasSent;
                a(new j(this, 1, f() + "uploads/done?" + "access_token=" + com.fyusion.sdk.common.a.a().m() + "&os=android&id=" + this.j, new c<String>(this) {
                    final /* synthetic */ h a;

                    {
                        this.a = r1;
                    }

                    public void a(String str) {
                        try {
                            this.a.a(str);
                        } catch (Exception e) {
                            this.a.b(this.a.a(21, e));
                        }
                    }
                }, new b(this) {
                    final /* synthetic */ h a;

                    {
                        this.a = r1;
                    }

                    public void a(s sVar) {
                        this.a.a(this.a.a(22, sVar.getMessage()));
                    }
                }) {
                    final /* synthetic */ h a;

                    protected Map<String, String> m() {
                        Map<String, String> hashMap = new HashMap();
                        if (!(this.a.w == null || this.a.w.isEmpty())) {
                            hashMap.put("description", this.a.w);
                        }
                        hashMap.put("privacy", !this.a.f ? "0" : "1");
                        if (this.a.x == null) {
                            hashMap.put("address", "");
                        } else {
                            hashMap.put("address", this.a.x);
                            hashMap.put("lat", String.valueOf(this.a.y));
                            hashMap.put("lng", String.valueOf(this.a.z));
                        }
                        return hashMap;
                    }
                });
            }
            return;
        }
        return;
        h();
    }

    private boolean l() {
        return this.g < 2;
    }

    private void m() {
        this.g++;
        this.a = this.b;
        h();
    }

    private int n() {
        return this.k <= 0 ? 999 : this.k;
    }

    private void o() {
        synchronized (this.i) {
            Iterator it = this.i.iterator();
            while (it.hasNext()) {
                j jVar = (j) it.next();
                try {
                    if (jVar.e != null) {
                        jVar.e.close();
                    }
                } catch (IOException e) {
                }
            }
            this.i.clear();
        }
    }

    private boolean p() {
        return this.v == a.Exit || this.v == a.ExitWithSuccess || this.v == a.ExitWithError || this.v == a.ExitWithUserCancel || isCancelled();
    }

    private long q() {
        long j = 0;
        for (int i = 0; i < this.i.size(); i++) {
            j jVar = (j) this.i.get(i);
            if (jVar.h) {
                j += jVar.f;
            }
        }
        return j;
    }

    private long r() {
        return (this.q + this.r) + q();
    }

    private void s() {
        if (this.s != null && !isCancelled()) {
            this.s.b(55);
        }
    }

    protected Boolean a(Void... voidArr) {
        boolean z = false;
        e();
        while (!p()) {
            try {
                Thread.sleep(50);
                if (isCancelled() || p()) {
                    break;
                }
                if (!this.B) {
                    if (!g()) {
                    }
                }
                this.B = false;
                k();
            } catch (InterruptedException e) {
                this.v = a.Exit;
            }
        }
        synchronized (this.h) {
            this.h.acquireUninterruptibly();
        }
        o();
        this.F.h();
        this.h.release();
        if (this.v == a.ExitWithSuccess) {
            z = true;
        }
        return Boolean.valueOf(z);
    }

    String a() {
        return this.v.toString();
    }

    void a(int i) {
        this.n = i;
        this.G = 45 / i;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void a(int i, boolean z) {
        synchronized (this.i) {
            j jVar;
            int size = this.i.size();
            while (true) {
                int i2 = size - 1;
                if (size > 0) {
                    jVar = (j) this.i.get(i2);
                    if (jVar.g == i) {
                        break;
                    }
                    size = i2;
                } else {
                    break;
                }
            }
            jVar.h = z;
            this.d = true;
            if (z) {
                this.H++;
                try {
                    InputStream b = this.F.b(i);
                    Log.d(C, "opened inputstream for " + this.E + " index " + i);
                    if (b != null) {
                        if (jVar.e != null) {
                            jVar.e.close();
                        }
                        jVar.e = b;
                        jVar.f = (long) b.available();
                        Log.d(C, "slicelen: " + jVar.f);
                    } else {
                        b(a(23, "slice stream not found"));
                    }
                } catch (Exception e) {
                    b(a(24, e));
                }
            }
        }
    }

    void a(f fVar) {
        this.s = fVar;
    }

    protected void a(Boolean bool) {
        int i = 0;
        if (this.s != null) {
            f fVar = this.s;
            if (!bool.booleanValue()) {
                i = n();
            }
            fVar.a(i);
        }
    }

    void a(boolean z) {
        if (z) {
            this.v = a.ExitWithUserCancel;
            this.k = 100;
            return;
        }
        cancel(true);
    }

    boolean b() {
        return this.A != null && this.A.isLoopClosed();
    }

    int c() {
        return this.A != null ? (int) this.A.getProcessedSize().height : 0;
    }

    void d() {
        if (this.N != null) {
            this.N.cancel(true);
        }
        synchronized (this.h) {
            this.h.acquireUninterruptibly();
        }
        this.v = a.Exit;
    }

    protected /* synthetic */ Object doInBackground(Object[] objArr) {
        return a((Void[]) objArr);
    }

    protected void onCancelled() {
        this.t = true;
        d();
        if (this.s != null) {
            this.s.a(n());
        }
        super.onCancelled();
    }

    protected /* synthetic */ void onPostExecute(Object obj) {
        a((Boolean) obj);
    }

    public String toString() {
        return this.v + " " + this.a + "/" + r();
    }
}
