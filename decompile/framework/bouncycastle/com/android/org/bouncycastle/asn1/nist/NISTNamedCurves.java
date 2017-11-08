package com.android.org.bouncycastle.asn1.nist;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.sec.SECNamedCurves;
import com.android.org.bouncycastle.asn1.sec.SECObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x9.X9ECParameters;
import com.android.org.bouncycastle.util.Strings;
import java.util.Enumeration;
import java.util.Hashtable;

public class NISTNamedCurves {
    static final Hashtable names = new Hashtable();
    static final Hashtable objIds = new Hashtable();

    static {
        defineCurveAlias("B-163", SECObjectIdentifiers.sect163r2);
        defineCurveAlias("B-233", SECObjectIdentifiers.sect233r1);
        defineCurveAlias("B-283", SECObjectIdentifiers.sect283r1);
        defineCurveAlias("B-409", SECObjectIdentifiers.sect409r1);
        defineCurveAlias("B-571", SECObjectIdentifiers.sect571r1);
        defineCurveAlias("K-163", SECObjectIdentifiers.sect163k1);
        defineCurveAlias("K-233", SECObjectIdentifiers.sect233k1);
        defineCurveAlias("K-283", SECObjectIdentifiers.sect283k1);
        defineCurveAlias("K-409", SECObjectIdentifiers.sect409k1);
        defineCurveAlias("K-571", SECObjectIdentifiers.sect571k1);
        defineCurveAlias("P-192", SECObjectIdentifiers.secp192r1);
        defineCurveAlias("P-224", SECObjectIdentifiers.secp224r1);
        defineCurveAlias("P-256", SECObjectIdentifiers.secp256r1);
        defineCurveAlias("P-384", SECObjectIdentifiers.secp384r1);
        defineCurveAlias("P-521", SECObjectIdentifiers.secp521r1);
    }

    static void defineCurveAlias(String name, ASN1ObjectIdentifier oid) {
        objIds.put(name.toUpperCase(), oid);
        names.put(oid, name);
    }

    public static X9ECParameters getByName(String name) {
        ASN1ObjectIdentifier oid = getOID(name);
        if (oid == null) {
            return null;
        }
        return getByOID(oid);
    }

    public static X9ECParameters getByOID(ASN1ObjectIdentifier oid) {
        return SECNamedCurves.getByOID(oid);
    }

    public static ASN1ObjectIdentifier getOID(String name) {
        return (ASN1ObjectIdentifier) objIds.get(Strings.toUpperCase(name));
    }

    public static String getName(ASN1ObjectIdentifier oid) {
        return (String) names.get(oid);
    }

    public static Enumeration getNames() {
        return names.elements();
    }
}
