package sun.security.provider.certpath;

import java.security.AlgorithmConstraints;
import java.security.AlgorithmParameters;
import java.security.CryptoPrimitive;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorException.BasicReason;
import java.security.cert.Certificate;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.PKIXReason;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import sun.security.util.DisabledAlgorithmConstraints;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X509CRLImpl;
import sun.security.x509.X509CertImpl;

public final class AlgorithmChecker extends PKIXCertPathChecker {
    private static final Set<CryptoPrimitive> SIGNATURE_PRIMITIVE_SET = EnumSet.of(CryptoPrimitive.SIGNATURE);
    private static final DisabledAlgorithmConstraints certPathDefaultConstraints = new DisabledAlgorithmConstraints(DisabledAlgorithmConstraints.PROPERTY_CERTPATH_DISABLED_ALGS);
    private final AlgorithmConstraints constraints;
    private PublicKey prevPubKey;
    private final PublicKey trustedPubKey;

    public AlgorithmChecker(TrustAnchor anchor) {
        this(anchor, certPathDefaultConstraints);
    }

    public AlgorithmChecker(AlgorithmConstraints constraints) {
        this.prevPubKey = null;
        this.trustedPubKey = null;
        this.constraints = constraints;
    }

    public AlgorithmChecker(TrustAnchor anchor, AlgorithmConstraints constraints) {
        if (anchor == null) {
            throw new IllegalArgumentException("The trust anchor cannot be null");
        }
        if (anchor.getTrustedCert() != null) {
            this.trustedPubKey = anchor.getTrustedCert().getPublicKey();
        } else {
            this.trustedPubKey = anchor.getCAPublicKey();
        }
        this.prevPubKey = this.trustedPubKey;
        this.constraints = constraints;
    }

    public void init(boolean forward) throws CertPathValidatorException {
        if (forward) {
            throw new CertPathValidatorException("forward checking not supported");
        } else if (this.trustedPubKey != null) {
            this.prevPubKey = this.trustedPubKey;
        } else {
            this.prevPubKey = null;
        }
    }

    public boolean isForwardCheckingSupported() {
        return false;
    }

    public Set<String> getSupportedExtensions() {
        return null;
    }

    public void check(Certificate cert, Collection<String> collection) throws CertPathValidatorException {
        if ((cert instanceof X509Certificate) && this.constraints != null) {
            try {
                X509CertImpl x509Cert = X509CertImpl.toImpl((X509Certificate) cert);
                PublicKey currPubKey = x509Cert.getPublicKey();
                String currSigAlg = x509Cert.getSigAlgName();
                try {
                    AlgorithmParameters currSigAlgParams = ((AlgorithmId) x509Cert.get(X509CertImpl.SIG_ALG)).getParameters();
                    if (this.constraints.permits(SIGNATURE_PRIMITIVE_SET, currSigAlg, currSigAlgParams)) {
                        boolean[] keyUsage = x509Cert.getKeyUsage();
                        if (keyUsage == null || keyUsage.length >= 9) {
                            if (keyUsage != null) {
                                Set<CryptoPrimitive> primitives = EnumSet.noneOf(CryptoPrimitive.class);
                                if (keyUsage[0] || keyUsage[1] || keyUsage[5] || keyUsage[6]) {
                                    primitives.add(CryptoPrimitive.SIGNATURE);
                                }
                                if (keyUsage[2]) {
                                    primitives.add(CryptoPrimitive.KEY_ENCAPSULATION);
                                }
                                if (keyUsage[3]) {
                                    primitives.add(CryptoPrimitive.PUBLIC_KEY_ENCRYPTION);
                                }
                                if (keyUsage[4]) {
                                    primitives.add(CryptoPrimitive.KEY_AGREEMENT);
                                }
                                if (!(primitives.isEmpty() || this.constraints.permits(primitives, currPubKey))) {
                                    throw new CertPathValidatorException("algorithm constraints check failed", null, null, -1, BasicReason.ALGORITHM_CONSTRAINED);
                                }
                            }
                            if (this.prevPubKey != null) {
                                if (currSigAlg != null && !this.constraints.permits(SIGNATURE_PRIMITIVE_SET, currSigAlg, this.prevPubKey, currSigAlgParams)) {
                                    throw new CertPathValidatorException("Algorithm constraints check failed: " + currSigAlg, null, null, -1, BasicReason.ALGORITHM_CONSTRAINED);
                                } else if (PKIX.isDSAPublicKeyWithoutParams(currPubKey)) {
                                    if (this.prevPubKey instanceof DSAPublicKey) {
                                        DSAParams params = ((DSAPublicKey) this.prevPubKey).getParams();
                                        if (params == null) {
                                            throw new CertPathValidatorException("Key parameters missing");
                                        }
                                        try {
                                            currPubKey = KeyFactory.getInstance("DSA").generatePublic(new DSAPublicKeySpec(((DSAPublicKey) currPubKey).getY(), params.getP(), params.getQ(), params.getG()));
                                        } catch (GeneralSecurityException e) {
                                            throw new CertPathValidatorException("Unable to generate key with inherited parameters: " + e.getMessage(), e);
                                        }
                                    }
                                    throw new CertPathValidatorException("Input key is not of a appropriate type for inheriting parameters");
                                }
                            }
                            this.prevPubKey = currPubKey;
                            return;
                        }
                        throw new CertPathValidatorException("incorrect KeyUsage extension", null, null, -1, PKIXReason.INVALID_KEY_USAGE);
                    }
                    throw new CertPathValidatorException("Algorithm constraints check failed: " + currSigAlg, null, null, -1, BasicReason.ALGORITHM_CONSTRAINED);
                } catch (Throwable ce) {
                    throw new CertPathValidatorException(ce);
                }
            } catch (Throwable ce2) {
                throw new CertPathValidatorException(ce2);
            }
        }
    }

    void trySetTrustAnchor(TrustAnchor anchor) {
        if (this.prevPubKey != null) {
            return;
        }
        if (anchor == null) {
            throw new IllegalArgumentException("The trust anchor cannot be null");
        } else if (anchor.getTrustedCert() != null) {
            this.prevPubKey = anchor.getTrustedCert().getPublicKey();
        } else {
            this.prevPubKey = anchor.getCAPublicKey();
        }
    }

    static void check(PublicKey key, X509CRL crl) throws CertPathValidatorException {
        try {
            check(key, X509CRLImpl.toImpl(crl).getSigAlgId());
        } catch (Throwable ce) {
            throw new CertPathValidatorException(ce);
        }
    }

    static void check(PublicKey key, AlgorithmId algorithmId) throws CertPathValidatorException {
        String sigAlgName = algorithmId.getName();
        if (!certPathDefaultConstraints.permits(SIGNATURE_PRIMITIVE_SET, sigAlgName, key, algorithmId.getParameters())) {
            throw new CertPathValidatorException("algorithm check failed: " + sigAlgName + " is disabled", null, null, -1, BasicReason.ALGORITHM_CONSTRAINED);
        }
    }
}
