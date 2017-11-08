package sun.security.ec;

import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.PublicKey;
import java.security.interfaces.ECKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public final class ECKeyFactory extends KeyFactorySpi {
    public static final KeyFactory INSTANCE;
    public static final Provider ecInternalProvider;

    static {
        final Provider p = new Provider("SunEC-Internal", 1.0d, null) {
        };
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                p.put("KeyFactory.EC", ECKeyFactory.class.getName());
                p.put("AlgorithmParameters.EC", ECParameters.class.getName());
                p.put("Alg.Alias.AlgorithmParameters.1.2.840.10045.2.1", "EC");
                return null;
            }
        });
        try {
            INSTANCE = KeyFactory.getInstance("EC", p);
            ecInternalProvider = p;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static ECKey toECKey(Key key) throws InvalidKeyException {
        if (!(key instanceof ECKey)) {
            return (ECKey) INSTANCE.translateKey(key);
        }
        ECKey ecKey = (ECKey) key;
        checkKey(ecKey);
        return ecKey;
    }

    private static void checkKey(ECKey key) throws InvalidKeyException {
        if (key instanceof ECPublicKey) {
            if (key instanceof ECPublicKeyImpl) {
                return;
            }
        } else if (!(key instanceof ECPrivateKey)) {
            throw new InvalidKeyException("Neither a public nor a private key");
        } else if (key instanceof ECPrivateKeyImpl) {
            return;
        }
        String keyAlg = ((Key) key).getAlgorithm();
        if (!keyAlg.equals("EC")) {
            throw new InvalidKeyException("Not an EC key: " + keyAlg);
        }
    }

    protected Key engineTranslateKey(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("Key must not be null");
        }
        String keyAlg = key.getAlgorithm();
        if (!keyAlg.equals("EC")) {
            throw new InvalidKeyException("Not an EC key: " + keyAlg);
        } else if (key instanceof PublicKey) {
            return implTranslatePublicKey((PublicKey) key);
        } else {
            if (key instanceof PrivateKey) {
                return implTranslatePrivateKey((PrivateKey) key);
            }
            throw new InvalidKeyException("Neither a public nor a private key");
        }
    }

    protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
        try {
            return implGeneratePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw e;
        } catch (Throwable e2) {
            throw new InvalidKeySpecException(e2);
        }
    }

    protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws InvalidKeySpecException {
        try {
            return implGeneratePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            throw e;
        } catch (Throwable e2) {
            throw new InvalidKeySpecException(e2);
        }
    }

    private PublicKey implTranslatePublicKey(PublicKey key) throws InvalidKeyException {
        if (key instanceof ECPublicKey) {
            if (key instanceof ECPublicKeyImpl) {
                return key;
            }
            ECPublicKey ecKey = (ECPublicKey) key;
            return new ECPublicKeyImpl(ecKey.getW(), ecKey.getParams());
        } else if ("X.509".equals(key.getFormat())) {
            return new ECPublicKeyImpl(key.getEncoded());
        } else {
            throw new InvalidKeyException("Public keys must be instance of ECPublicKey or have X.509 encoding");
        }
    }

    private PrivateKey implTranslatePrivateKey(PrivateKey key) throws InvalidKeyException {
        if (key instanceof ECPrivateKey) {
            if (key instanceof ECPrivateKeyImpl) {
                return key;
            }
            ECPrivateKey ecKey = (ECPrivateKey) key;
            return new ECPrivateKeyImpl(ecKey.getS(), ecKey.getParams());
        } else if ("PKCS#8".equals(key.getFormat())) {
            return new ECPrivateKeyImpl(key.getEncoded());
        } else {
            throw new InvalidKeyException("Private keys must be instance of ECPrivateKey or have PKCS#8 encoding");
        }
    }

    private PublicKey implGeneratePublic(KeySpec keySpec) throws GeneralSecurityException {
        if (keySpec instanceof X509EncodedKeySpec) {
            return new ECPublicKeyImpl(((X509EncodedKeySpec) keySpec).getEncoded());
        }
        if (keySpec instanceof ECPublicKeySpec) {
            ECPublicKeySpec ecSpec = (ECPublicKeySpec) keySpec;
            return new ECPublicKeyImpl(ecSpec.getW(), ecSpec.getParams());
        }
        throw new InvalidKeySpecException("Only ECPublicKeySpec and X509EncodedKeySpec supported for EC public keys");
    }

    private PrivateKey implGeneratePrivate(KeySpec keySpec) throws GeneralSecurityException {
        if (keySpec instanceof PKCS8EncodedKeySpec) {
            return new ECPrivateKeyImpl(((PKCS8EncodedKeySpec) keySpec).getEncoded());
        }
        if (keySpec instanceof ECPrivateKeySpec) {
            ECPrivateKeySpec ecSpec = (ECPrivateKeySpec) keySpec;
            return new ECPrivateKeyImpl(ecSpec.getS(), ecSpec.getParams());
        }
        throw new InvalidKeySpecException("Only ECPrivateKeySpec and PKCS8EncodedKeySpec supported for EC private keys");
    }

    protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) throws InvalidKeySpecException {
        try {
            key = engineTranslateKey(key);
            if (key instanceof ECPublicKey) {
                ECPublicKey ecKey = (ECPublicKey) key;
                if (ECPublicKeySpec.class.isAssignableFrom(keySpec)) {
                    return new ECPublicKeySpec(ecKey.getW(), ecKey.getParams());
                }
                if (X509EncodedKeySpec.class.isAssignableFrom(keySpec)) {
                    return new X509EncodedKeySpec(key.getEncoded());
                }
                throw new InvalidKeySpecException("KeySpec must be ECPublicKeySpec or X509EncodedKeySpec for EC public keys");
            } else if (!(key instanceof ECPrivateKey)) {
                throw new InvalidKeySpecException("Neither public nor private key");
            } else if (PKCS8EncodedKeySpec.class.isAssignableFrom(keySpec)) {
                return new PKCS8EncodedKeySpec(key.getEncoded());
            } else {
                if (ECPrivateKeySpec.class.isAssignableFrom(keySpec)) {
                    ECPrivateKey ecKey2 = (ECPrivateKey) key;
                    return new ECPrivateKeySpec(ecKey2.getS(), ecKey2.getParams());
                }
                throw new InvalidKeySpecException("KeySpec must be ECPrivateKeySpec or PKCS8EncodedKeySpec for EC private keys");
            }
        } catch (Throwable e) {
            throw new InvalidKeySpecException(e);
        }
    }
}
