package sun.security.ssl;

import java.io.IOException;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.security.spec.RSAPublicKeySpec;
import java.util.Map.Entry;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import sun.security.ec.ECParameters;
import sun.security.ec.NamedCurve;
import sun.security.jca.ProviderList;
import sun.security.jca.Providers;

final class JsseJce {
    static final String CIPHER_3DES = "DESede/CBC/NoPadding";
    static final String CIPHER_AES = "AES/CBC/NoPadding";
    static final String CIPHER_DES = "DES/CBC/NoPadding";
    static final String CIPHER_RC4 = "RC4";
    static final String CIPHER_RSA_PKCS1 = "RSA/ECB/PKCS1Padding";
    static final String SIGNATURE_DSA = "DSA";
    static final String SIGNATURE_ECDSA = "SHA1withECDSA";
    static final String SIGNATURE_RAWDSA = "RawDSA";
    static final String SIGNATURE_RAWECDSA = "NONEwithECDSA";
    static final String SIGNATURE_RAWRSA = "NONEwithRSA";
    static final String SIGNATURE_SSLRSA = "MD5andSHA1withRSA";
    private static final Debug debug = Debug.getInstance("ssl");
    private static Boolean ecAvailable;
    private static final ProviderList fipsProviderList;
    private static final boolean kerberosAvailable;

    private static final class SunCertificates extends Provider {
        SunCertificates(final Provider p) {
            super("SunCertificates", 1.0d, "SunJSSE internal");
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    for (Entry<Object, Object> entry : p.entrySet()) {
                        String key = (String) entry.getKey();
                        if (key.startsWith("CertPathValidator.") || key.startsWith("CertPathBuilder.") || key.startsWith("CertStore.") || key.startsWith("CertificateFactory.")) {
                            SunCertificates.this.put(key, entry.getValue());
                        }
                    }
                    return null;
                }
            });
        }
    }

    static {
        boolean temp;
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() throws Exception {
                    Class.forName("sun.security.krb5.PrincipalName", true, null);
                    return null;
                }
            });
            temp = true;
        } catch (Exception e) {
            temp = false;
        }
        kerberosAvailable = temp;
        if (SunJSSE.isFIPS()) {
            Provider sun = Security.getProvider("SUN");
            if (sun == null) {
                throw new RuntimeException("FIPS mode: SUN provider must be installed");
            }
            Provider sunCerts = new SunCertificates(sun);
            fipsProviderList = ProviderList.newList(SunJSSE.cryptoProvider, sunCerts);
            return;
        }
        fipsProviderList = null;
    }

    private JsseJce() {
    }

    static synchronized boolean isEcAvailable() {
        boolean booleanValue;
        synchronized (JsseJce.class) {
            if (ecAvailable == null) {
                try {
                    getSignature(SIGNATURE_ECDSA);
                    getSignature(SIGNATURE_RAWECDSA);
                    getKeyAgreement("ECDH");
                    getKeyFactory("EC");
                    getKeyPairGenerator("EC");
                    ecAvailable = Boolean.valueOf(true);
                } catch (Exception e) {
                    ecAvailable = Boolean.valueOf(false);
                }
            }
            booleanValue = ecAvailable.booleanValue();
        }
        return booleanValue;
    }

    static synchronized void clearEcAvailable() {
        synchronized (JsseJce.class) {
            ecAvailable = null;
        }
    }

    static boolean isKerberosAvailable() {
        return kerberosAvailable;
    }

    static Cipher getCipher(String transformation) throws NoSuchAlgorithmException {
        try {
            if (SunJSSE.cryptoProvider == null) {
                return Cipher.getInstance(transformation);
            }
            return Cipher.getInstance(transformation, SunJSSE.cryptoProvider);
        } catch (Throwable e) {
            throw new NoSuchAlgorithmException(e);
        }
    }

    static Signature getSignature(String algorithm) throws NoSuchAlgorithmException {
        if (SunJSSE.cryptoProvider == null) {
            return Signature.getInstance(algorithm);
        }
        if (algorithm != SIGNATURE_SSLRSA || SunJSSE.cryptoProvider.getService("Signature", algorithm) != null) {
            return Signature.getInstance(algorithm, SunJSSE.cryptoProvider);
        }
        try {
            return Signature.getInstance(algorithm, "SunJSSE");
        } catch (Throwable e) {
            throw new NoSuchAlgorithmException(e);
        }
    }

    static KeyGenerator getKeyGenerator(String algorithm) throws NoSuchAlgorithmException {
        if (SunJSSE.cryptoProvider == null) {
            return KeyGenerator.getInstance(algorithm);
        }
        return KeyGenerator.getInstance(algorithm, SunJSSE.cryptoProvider);
    }

    static KeyPairGenerator getKeyPairGenerator(String algorithm) throws NoSuchAlgorithmException {
        if (SunJSSE.cryptoProvider == null) {
            return KeyPairGenerator.getInstance(algorithm);
        }
        return KeyPairGenerator.getInstance(algorithm, SunJSSE.cryptoProvider);
    }

    static KeyAgreement getKeyAgreement(String algorithm) throws NoSuchAlgorithmException {
        if (SunJSSE.cryptoProvider == null) {
            return KeyAgreement.getInstance(algorithm);
        }
        return KeyAgreement.getInstance(algorithm, SunJSSE.cryptoProvider);
    }

    static Mac getMac(String algorithm) throws NoSuchAlgorithmException {
        if (SunJSSE.cryptoProvider == null) {
            return Mac.getInstance(algorithm);
        }
        return Mac.getInstance(algorithm, SunJSSE.cryptoProvider);
    }

    static KeyFactory getKeyFactory(String algorithm) throws NoSuchAlgorithmException {
        if (SunJSSE.cryptoProvider == null) {
            return KeyFactory.getInstance(algorithm);
        }
        return KeyFactory.getInstance(algorithm, SunJSSE.cryptoProvider);
    }

    static SecureRandom getSecureRandom() throws KeyManagementException {
        if (SunJSSE.cryptoProvider == null) {
            return new SecureRandom();
        }
        try {
            return SecureRandom.getInstance("PKCS11", SunJSSE.cryptoProvider);
        } catch (NoSuchAlgorithmException e) {
            for (Service s : SunJSSE.cryptoProvider.getServices()) {
                if (s.getType().equals("SecureRandom")) {
                    try {
                        return SecureRandom.getInstance(s.getAlgorithm(), SunJSSE.cryptoProvider);
                    } catch (NoSuchAlgorithmException e2) {
                    }
                }
            }
            throw new KeyManagementException("FIPS mode: no SecureRandom  implementation found in provider " + SunJSSE.cryptoProvider.getName());
        }
    }

    static MessageDigest getMD5() {
        return getMessageDigest("MD5");
    }

    static MessageDigest getSHA() {
        return getMessageDigest("SHA");
    }

    static MessageDigest getMessageDigest(String algorithm) {
        try {
            if (SunJSSE.cryptoProvider == null) {
                return MessageDigest.getInstance(algorithm);
            }
            return MessageDigest.getInstance(algorithm, SunJSSE.cryptoProvider);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm " + algorithm + " not available", e);
        }
    }

    static int getRSAKeyLength(PublicKey key) {
        BigInteger modulus;
        if (key instanceof RSAPublicKey) {
            modulus = ((RSAPublicKey) key).getModulus();
        } else {
            modulus = getRSAPublicKeySpec(key).getModulus();
        }
        return modulus.bitLength();
    }

    static RSAPublicKeySpec getRSAPublicKeySpec(PublicKey key) {
        if (key instanceof RSAPublicKey) {
            RSAPublicKey rsaKey = (RSAPublicKey) key;
            return new RSAPublicKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent());
        }
        try {
            return (RSAPublicKeySpec) getKeyFactory("RSA").getKeySpec(key, RSAPublicKeySpec.class);
        } catch (Exception e) {
            throw ((RuntimeException) new RuntimeException().initCause(e));
        }
    }

    static ECParameterSpec getECParameterSpec(String namedCurveOid) {
        return NamedCurve.getECParameterSpec(namedCurveOid);
    }

    static String getNamedCurveOid(ECParameterSpec params) {
        return ECParameters.getCurveName(params);
    }

    static ECPoint decodePoint(byte[] encoded, EllipticCurve curve) throws IOException {
        return ECParameters.decodePoint(encoded, curve);
    }

    static byte[] encodePoint(ECPoint point, EllipticCurve curve) {
        return ECParameters.encodePoint(point, curve);
    }

    static Object beginFipsProvider() {
        if (fipsProviderList == null) {
            return null;
        }
        return Providers.beginThreadProviderList(fipsProviderList);
    }

    static void endFipsProvider(Object o) {
        if (fipsProviderList != null) {
            Providers.endThreadProviderList((ProviderList) o);
        }
    }
}
