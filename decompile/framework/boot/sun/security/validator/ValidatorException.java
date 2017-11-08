package sun.security.validator;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class ValidatorException extends CertificateException {
    public static final Object T_ALGORITHM_DISABLED = "Certificate signature algorithm disabled";
    public static final Object T_CA_EXTENSIONS = "CA certificate extension check failed";
    public static final Object T_CERT_EXPIRED = "Certificate expired";
    public static final Object T_EE_EXTENSIONS = "End entity certificate extension check failed";
    public static final Object T_NAME_CHAINING = "Certificate chaining error";
    public static final Object T_NO_TRUST_ANCHOR = "No trusted certificate found";
    public static final Object T_SIGNATURE_ERROR = "Certificate signature validation failed";
    public static final Object T_UNTRUSTED_CERT = "Untrusted certificate";
    private static final long serialVersionUID = -2836879718282292155L;
    private X509Certificate cert;
    private Object type;

    public ValidatorException(String msg) {
        super(msg);
    }

    public ValidatorException(String msg, Throwable cause) {
        super(msg);
        initCause(cause);
    }

    public ValidatorException(Object type) {
        this(type, null);
    }

    public ValidatorException(Object type, X509Certificate cert) {
        super((String) type);
        this.type = type;
        this.cert = cert;
    }

    public ValidatorException(Object type, X509Certificate cert, Throwable cause) {
        this(type, cert);
        initCause(cause);
    }

    public ValidatorException(String msg, Object type, X509Certificate cert) {
        super(msg);
        this.type = type;
        this.cert = cert;
    }

    public ValidatorException(String msg, Object type, X509Certificate cert, Throwable cause) {
        this(msg, type, cert);
        initCause(cause);
    }

    public Object getErrorType() {
        return this.type;
    }

    public X509Certificate getErrorCertificate() {
        return this.cert;
    }
}
