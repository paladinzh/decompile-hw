package com.google.android.gms.internal;

import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.net.UrlQuerySanitizer.ParameterValuePair;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.HashMap;

/* compiled from: Unknown */
public class de extends WebViewClient {
    private ap lV;
    private final Object mg = new Object();
    protected final dd ng;
    private final HashMap<String, ar> qf = new HashMap();
    private u qg;
    private br qh;
    private a qi;
    private boolean qj = false;
    private boolean qk;
    private bu ql;

    /* compiled from: Unknown */
    public interface a {
        void a(dd ddVar);
    }

    public de(dd ddVar, boolean z) {
        this.ng = ddVar;
        this.qk = z;
    }

    private void a(bq bqVar) {
        bo.a(this.ng.getContext(), bqVar);
    }

    private static boolean b(Uri uri) {
        String scheme = uri.getScheme();
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    private void c(Uri uri) {
        String path = uri.getPath();
        ar arVar = (ar) this.qf.get(path);
        if (arVar == null) {
            da.w("No GMSG handler found for GMSG: " + uri);
            return;
        }
        HashMap hashMap = new HashMap();
        UrlQuerySanitizer urlQuerySanitizer = new UrlQuerySanitizer();
        urlQuerySanitizer.setAllowUnregisteredParamaters(true);
        urlQuerySanitizer.setUnregisteredParameterValueSanitizer(UrlQuerySanitizer.getAllButNulLegal());
        urlQuerySanitizer.parseUrl(uri.toString());
        for (ParameterValuePair parameterValuePair : urlQuerySanitizer.getParameterList()) {
            hashMap.put(parameterValuePair.mParameter, parameterValuePair.mValue);
        }
        if (da.n(2)) {
            da.v("Received GMSG: " + path);
            for (String str : hashMap.keySet()) {
                da.v("  " + str + ": " + ((String) hashMap.get(str)));
            }
        }
        arVar.a(this.ng, hashMap);
    }

    public final void a(bn bnVar) {
        br brVar = null;
        boolean be = this.ng.be();
        u uVar = (be && !this.ng.Q().lo) ? null : this.qg;
        if (!be) {
            brVar = this.qh;
        }
        a(new bq(bnVar, uVar, brVar, this.ql, this.ng.bd()));
    }

    public final void a(a aVar) {
        this.qi = aVar;
    }

    public void a(u uVar, br brVar, ap apVar, bu buVar, boolean z) {
        a("/appEvent", new ao(apVar));
        a("/canOpenURLs", aq.lW);
        a("/click", aq.lX);
        a("/close", aq.lY);
        a("/customClose", aq.lZ);
        a("/httpTrack", aq.ma);
        a("/log", aq.mb);
        a("/open", aq.mc);
        a("/touch", aq.md);
        a("/video", aq.me);
        this.qg = uVar;
        this.qh = brVar;
        this.lV = apVar;
        this.ql = buVar;
        o(z);
    }

    public final void a(String str, ar arVar) {
        this.qf.put(str, arVar);
    }

    public final void a(boolean z, int i) {
        u uVar = (this.ng.be() && !this.ng.Q().lo) ? null : this.qg;
        a(new bq(uVar, this.qh, this.ql, this.ng, z, i, this.ng.bd()));
    }

    public final void a(boolean z, int i, String str) {
        br brVar = null;
        boolean be = this.ng.be();
        u uVar = (be && !this.ng.Q().lo) ? null : this.qg;
        if (!be) {
            brVar = this.qh;
        }
        a(new bq(uVar, brVar, this.lV, this.ql, this.ng, z, i, str, this.ng.bd()));
    }

    public final void a(boolean z, int i, String str, String str2) {
        br brVar = null;
        boolean be = this.ng.be();
        u uVar = (be && !this.ng.Q().lo) ? null : this.qg;
        if (!be) {
            brVar = this.qh;
        }
        a(new bq(uVar, brVar, this.lV, this.ql, this.ng, z, i, str, str2, this.ng.bd()));
    }

    public final void ar() {
        synchronized (this.mg) {
            this.qj = false;
            this.qk = true;
            final bo ba = this.ng.ba();
            if (ba != null) {
                if (cz.aX()) {
                    ba.ar();
                } else {
                    cz.pT.post(new Runnable(this) {
                        final /* synthetic */ de qn;

                        public void run() {
                            ba.ar();
                        }
                    });
                }
            }
        }
    }

    public boolean bi() {
        boolean z;
        synchronized (this.mg) {
            z = this.qk;
        }
        return z;
    }

    public final void o(boolean z) {
        this.qj = z;
    }

    public final void onPageFinished(WebView webView, String url) {
        if (this.qi != null) {
            this.qi.a(this.ng);
            this.qi = null;
        }
    }

    public final boolean shouldOverrideUrlLoading(WebView webView, String url) {
        da.v("AdWebView shouldOverrideUrlLoading: " + url);
        Uri parse = Uri.parse(url);
        if ("gmsg".equalsIgnoreCase(parse.getScheme()) && "mobileads.google.com".equalsIgnoreCase(parse.getHost())) {
            c(parse);
        } else if (this.qj && webView == this.ng && b(parse)) {
            return super.shouldOverrideUrlLoading(webView, url);
        } else {
            if (this.ng.willNotDraw()) {
                da.w("AdWebView unable to handle URL: " + url);
            } else {
                try {
                    l bc = this.ng.bc();
                    if (bc != null) {
                        if (bc.a(parse)) {
                            parse = bc.a(parse, this.ng.getContext());
                        }
                    }
                } catch (m e) {
                    da.w("Unable to append parameter to URL: " + url);
                }
                a(new bn("android.intent.action.VIEW", parse.toString(), null, null, null, null, null));
            }
        }
        return true;
    }
}
