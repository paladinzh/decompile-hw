package sun.security.validator;

import java.security.AlgorithmConstraints;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;

public abstract class Validator {
    static final X509Certificate[] CHAIN0 = new X509Certificate[0];
    public static final String TYPE_PKIX = "PKIX";
    public static final String TYPE_SIMPLE = "Simple";
    public static final String VAR_CODE_SIGNING = "code signing";
    public static final String VAR_GENERIC = "generic";
    public static final String VAR_JCE_SIGNING = "jce signing";
    public static final String VAR_PLUGIN_CODE_SIGNING = "plugin code signing";
    public static final String VAR_TLS_CLIENT = "tls client";
    public static final String VAR_TLS_SERVER = "tls server";
    public static final String VAR_TSA_SERVER = "tsa server";
    final EndEntityChecker endEntityChecker;
    @Deprecated
    volatile Date validationDate;
    final String variant;

    abstract X509Certificate[] engineValidate(X509Certificate[] x509CertificateArr, Collection<X509Certificate> collection, AlgorithmConstraints algorithmConstraints, Object obj) throws CertificateException;

    public abstract Collection<X509Certificate> getTrustedCertificates();

    Validator(String type, String variant) {
        this.variant = variant;
        this.endEntityChecker = EndEntityChecker.getInstance(type, variant);
    }

    public static Validator getInstance(String type, String variant, KeyStore ks) {
        return getInstance(type, variant, KeyStores.getTrustedCerts(ks));
    }

    public static Validator getInstance(String type, String variant, Collection<X509Certificate> trustedCerts) {
        if (type.equals(TYPE_SIMPLE)) {
            return new SimpleValidator(variant, trustedCerts);
        }
        if (type.equals(TYPE_PKIX)) {
            return new PKIXValidator(variant, (Collection) trustedCerts);
        }
        throw new IllegalArgumentException("Unknown validator type: " + type);
    }

    public static Validator getInstance(String type, String variant, PKIXBuilderParameters params) {
        if (type.equals(TYPE_PKIX)) {
            return new PKIXValidator(variant, params);
        }
        throw new IllegalArgumentException("getInstance(PKIXBuilderParameters) can only be used with PKIX validator");
    }

    public final X509Certificate[] validate(X509Certificate[] chain) throws CertificateException {
        return validate(chain, null, null);
    }

    public final X509Certificate[] validate(X509Certificate[] chain, Collection<X509Certificate> otherCerts) throws CertificateException {
        return validate(chain, otherCerts, null);
    }

    public final X509Certificate[] validate(X509Certificate[] chain, Collection<X509Certificate> otherCerts, Object parameter) throws CertificateException {
        return validate(chain, otherCerts, null, parameter);
    }

    public final X509Certificate[] validate(X509Certificate[] chain, Collection<X509Certificate> otherCerts, AlgorithmConstraints constraints, Object parameter) throws CertificateException {
        chain = engineValidate(chain, otherCerts, constraints, parameter);
        if (chain.length > 1) {
            this.endEntityChecker.check(chain[0], parameter);
        }
        return chain;
    }

    @Deprecated
    public void setValidationDate(Date validationDate) {
        this.validationDate = validationDate;
    }
}
