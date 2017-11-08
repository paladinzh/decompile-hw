package sun.net.www.protocol.http;

import java.security.AccessController;
import java.util.HashMap;
import java.util.Iterator;
import sun.net.www.HeaderParser;
import sun.net.www.MessageHeader;
import sun.security.action.GetPropertyAction;

public class AuthenticationHeader {
    static String authPref;
    boolean dontUseNegotiate = false;
    private final HttpCallerInfo hci;
    String hdrname;
    HeaderParser preferred;
    String preferred_r;
    MessageHeader rsp;
    HashMap schemes;

    static class SchemeMapValue {
        HeaderParser parser;
        String raw;

        SchemeMapValue(HeaderParser h, String r) {
            this.raw = r;
            this.parser = h;
        }
    }

    static {
        authPref = null;
        authPref = (String) AccessController.doPrivileged(new GetPropertyAction("http.auth.preference"));
        if (authPref != null) {
            authPref = authPref.toLowerCase();
            if (authPref.equals("spnego") || authPref.equals("kerberos")) {
                authPref = "negotiate";
            }
        }
    }

    public String toString() {
        return "AuthenticationHeader: prefer " + this.preferred_r;
    }

    public AuthenticationHeader(String hdrname, MessageHeader response, HttpCallerInfo hci, boolean dontUseNegotiate) {
        this.hci = hci;
        this.dontUseNegotiate = dontUseNegotiate;
        this.rsp = response;
        this.hdrname = hdrname;
        this.schemes = new HashMap();
        parse();
    }

    public HttpCallerInfo getHttpCallerInfo() {
        return this.hci;
    }

    private void parse() {
        SchemeMapValue tmp;
        Iterator iter = this.rsp.multiValueIterator(this.hdrname);
        while (iter.hasNext()) {
            String raw = (String) iter.next();
            HeaderParser hp = new HeaderParser(raw);
            Iterator keys = hp.keys();
            int i = 0;
            int lastSchemeIndex = -1;
            while (keys.hasNext()) {
                keys.next();
                if (hp.findValue(i) == null) {
                    if (lastSchemeIndex != -1) {
                        HeaderParser hpn = hp.subsequence(lastSchemeIndex, i);
                        this.schemes.put(hpn.findKey(0), new SchemeMapValue(hpn, raw));
                    }
                    lastSchemeIndex = i;
                }
                i++;
            }
            if (i > lastSchemeIndex) {
                hpn = hp.subsequence(lastSchemeIndex, i);
                this.schemes.put(hpn.findKey(0), new SchemeMapValue(hpn, raw));
            }
        }
        SchemeMapValue schemeMapValue = null;
        if (authPref != null) {
            schemeMapValue = (SchemeMapValue) this.schemes.get(authPref);
            if (schemeMapValue != null) {
                if (this.dontUseNegotiate && authPref.equals("negotiate")) {
                    schemeMapValue = null;
                }
                if (schemeMapValue != null) {
                    this.preferred = schemeMapValue.parser;
                    this.preferred_r = schemeMapValue.raw;
                }
            }
        }
        if (schemeMapValue == null && !this.dontUseNegotiate) {
            tmp = (SchemeMapValue) this.schemes.get("negotiate");
            if (tmp != null) {
                if (this.hci == null || !NegotiateAuthentication.isSupported(new HttpCallerInfo(this.hci, "Negotiate"))) {
                    tmp = null;
                }
                schemeMapValue = tmp;
            }
        }
        if (schemeMapValue == null && !this.dontUseNegotiate) {
            tmp = (SchemeMapValue) this.schemes.get("kerberos");
            if (tmp != null) {
                if (this.hci == null || !NegotiateAuthentication.isSupported(new HttpCallerInfo(this.hci, "Kerberos"))) {
                    tmp = null;
                }
                schemeMapValue = tmp;
            }
        }
        if (schemeMapValue == null) {
            schemeMapValue = (SchemeMapValue) this.schemes.get("digest");
            if (schemeMapValue == null) {
                schemeMapValue = (SchemeMapValue) this.schemes.get("ntlm");
                if (schemeMapValue == null) {
                    schemeMapValue = (SchemeMapValue) this.schemes.get("basic");
                }
            }
        }
        if (schemeMapValue != null) {
            this.preferred = schemeMapValue.parser;
            this.preferred_r = schemeMapValue.raw;
        }
    }

    public HeaderParser headerParser() {
        return this.preferred;
    }

    public String scheme() {
        if (this.preferred != null) {
            return this.preferred.findKey(0);
        }
        return null;
    }

    public String raw() {
        return this.preferred_r;
    }

    public boolean isPresent() {
        return this.preferred != null;
    }
}
