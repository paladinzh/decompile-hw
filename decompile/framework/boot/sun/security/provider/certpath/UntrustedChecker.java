package sun.security.provider.certpath;

import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Set;
import sun.security.util.Debug;
import sun.security.util.UntrustedCertificates;

public final class UntrustedChecker extends PKIXCertPathChecker {
    private static final Debug debug = Debug.getInstance("certpath");

    public void init(boolean forward) throws CertPathValidatorException {
    }

    public boolean isForwardCheckingSupported() {
        return true;
    }

    public Set<String> getSupportedExtensions() {
        return null;
    }

    public void check(Certificate cert, Collection<String> collection) throws CertPathValidatorException {
        X509Certificate currCert = (X509Certificate) cert;
        if (UntrustedCertificates.isUntrusted(currCert)) {
            if (debug != null) {
                debug.println("UntrustedChecker: untrusted certificate " + currCert.getSubjectX500Principal());
            }
            throw new CertPathValidatorException("Untrusted certificate: " + currCert.getSubjectX500Principal());
        }
    }
}
