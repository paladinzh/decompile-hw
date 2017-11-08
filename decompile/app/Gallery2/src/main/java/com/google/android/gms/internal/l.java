package com.google.android.gms.internal;

import android.content.Context;
import android.net.Uri;
import android.view.MotionEvent;

/* compiled from: Unknown */
public class l {
    private String[] kq;
    private h kr;

    private Uri a(Uri uri, Context context, String str, boolean z) throws m {
        try {
            if (uri.getQueryParameter("ms") == null) {
                return a(uri, "ms", !z ? this.kr.a(context) : this.kr.a(context, str));
            }
            throw new m("Query parameter already exists: ms");
        } catch (UnsupportedOperationException e) {
            throw new m("Provided Uri is not in a valid state");
        }
    }

    private Uri a(Uri uri, String str, String str2) throws UnsupportedOperationException {
        String uri2 = uri.toString();
        int indexOf = uri2.indexOf("&adurl");
        if (indexOf == -1) {
            indexOf = uri2.indexOf("?adurl");
        }
        return indexOf == -1 ? uri.buildUpon().appendQueryParameter(str, str2).build() : Uri.parse(new StringBuilder(uri2.substring(0, indexOf + 1)).append(str).append("=").append(str2).append("&").append(uri2.substring(indexOf + 1)).toString());
    }

    public Uri a(Uri uri, Context context) throws m {
        try {
            return a(uri, context, uri.getQueryParameter("ai"), true);
        } catch (UnsupportedOperationException e) {
            throw new m("Provided Uri is not in a valid state");
        }
    }

    public void a(MotionEvent motionEvent) {
        this.kr.a(motionEvent);
    }

    public boolean a(Uri uri) {
        if (uri != null) {
            try {
                String host = uri.getHost();
                for (String endsWith : this.kq) {
                    if (host.endsWith(endsWith)) {
                        return true;
                    }
                }
                return false;
            } catch (NullPointerException e) {
                return false;
            }
        }
        throw new NullPointerException();
    }

    public h y() {
        return this.kr;
    }
}
