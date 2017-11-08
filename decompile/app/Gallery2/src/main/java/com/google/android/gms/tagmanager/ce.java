package com.google.android.gms.tagmanager;

import android.net.Uri;
import com.android.gallery3d.gadget.XmlUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/* compiled from: Unknown */
class ce {
    private static ce VS;
    private volatile String TM;
    private volatile a VT;
    private volatile String VU;
    private volatile String VV;

    /* compiled from: Unknown */
    enum a {
        NONE,
        CONTAINER,
        CONTAINER_DEBUG
    }

    ce() {
        clear();
    }

    private String bt(String str) {
        return str.split("&")[0].split("=")[1];
    }

    private String g(Uri uri) {
        return uri.getQuery().replace("&gtm_debug=x", "");
    }

    static ce ju() {
        ce ceVar;
        synchronized (ce.class) {
            if (VS == null) {
                VS = new ce();
            }
            ceVar = VS;
        }
        return ceVar;
    }

    void clear() {
        this.VT = a.NONE;
        this.VU = null;
        this.TM = null;
        this.VV = null;
    }

    synchronized boolean f(Uri uri) {
        try {
            String decode = URLDecoder.decode(uri.toString(), XmlUtils.INPUT_ENCODING);
            if (decode.matches("^tagmanager.c.\\S+:\\/\\/preview\\/p\\?id=\\S+&gtm_auth=\\S+&gtm_preview=\\d+(&gtm_debug=x)?$")) {
                bh.v("Container preview url: " + decode);
                if (decode.matches(".*?&gtm_debug=x$")) {
                    this.VT = a.CONTAINER_DEBUG;
                } else {
                    this.VT = a.CONTAINER;
                }
                this.VV = g(uri);
                if (this.VT != a.CONTAINER) {
                    if (this.VT != a.CONTAINER_DEBUG) {
                        this.TM = bt(this.VV);
                        return true;
                    }
                }
                this.VU = "/r?" + this.VV;
                this.TM = bt(this.VV);
                return true;
            } else if (!decode.matches("^tagmanager.c.\\S+:\\/\\/preview\\/p\\?id=\\S+&gtm_preview=$")) {
                bh.w("Invalid preview uri: " + decode);
                return false;
            } else if (!bt(uri.getQuery()).equals(this.TM)) {
                return false;
            } else {
                bh.v("Exit preview mode for container: " + this.TM);
                this.VT = a.NONE;
                this.VU = null;
                return true;
            }
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    String getContainerId() {
        return this.TM;
    }

    a jv() {
        return this.VT;
    }

    String jw() {
        return this.VU;
    }
}
