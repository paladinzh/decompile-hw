package sun.security.ssl;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

final class CipherSuite implements Comparable {
    private static final boolean ALLOW_ECC = Debug.getBooleanProperty("com.sun.net.ssl.enableECC", DYNAMIC_AVAILABILITY);
    static final BulkCipher B_3DES = new BulkCipher("DESede/CBC/NoPadding", 24, 8, DYNAMIC_AVAILABILITY);
    static final BulkCipher B_AES_128 = new BulkCipher("AES/CBC/NoPadding", 16, 16, DYNAMIC_AVAILABILITY);
    static final BulkCipher B_AES_256 = new BulkCipher("AES/CBC/NoPadding", 32, 16, DYNAMIC_AVAILABILITY);
    static final BulkCipher B_DES = new BulkCipher("DES/CBC/NoPadding", 8, 8, DYNAMIC_AVAILABILITY);
    static final BulkCipher B_DES_40 = new BulkCipher("DES/CBC/NoPadding", 5, 8, 8, DYNAMIC_AVAILABILITY);
    static final BulkCipher B_IDEA = new BulkCipher("IDEA", 16, 8, false);
    static final BulkCipher B_NULL = new BulkCipher("NULL", 0, 0, 0, DYNAMIC_AVAILABILITY);
    static final BulkCipher B_RC2_40 = new BulkCipher("RC2", 5, 16, 8, false);
    static final BulkCipher B_RC4_128 = new BulkCipher("RC4", 16, 0, DYNAMIC_AVAILABILITY);
    static final BulkCipher B_RC4_40 = new BulkCipher("RC4", 5, 16, 0, DYNAMIC_AVAILABILITY);
    static final CipherSuite C_NULL = valueOf(0, 0);
    static final CipherSuite C_SCSV = valueOf(0, 255);
    static final int DEFAULT_SUITES_PRIORITY = 300;
    static final boolean DYNAMIC_AVAILABILITY = true;
    static final MacAlg M_MD5 = new MacAlg("MD5", 16, 64, 9);
    static final MacAlg M_NULL = new MacAlg("NULL", 0, 0, 0);
    static final MacAlg M_SHA = new MacAlg("SHA", 20, 64, 9);
    static final MacAlg M_SHA256 = new MacAlg("SHA256", 32, 64, 9);
    static final MacAlg M_SHA384 = new MacAlg("SHA384", 48, 128, 17);
    static final int SUPPORTED_SUITES_PRIORITY = 1;
    private static final Map<Integer, CipherSuite> idMap = new HashMap();
    private static final Map<String, CipherSuite> nameMap = new HashMap();
    final boolean allowed;
    final BulkCipher cipher;
    final boolean exportable;
    final int id;
    final KeyExchange keyExchange;
    final MacAlg macAlg;
    final String name;
    final int obsoleted;
    final PRF prfAlg;
    final int priority;
    final int supported;

    static final class BulkCipher {
        private static final Map<BulkCipher, Boolean> availableCache = new HashMap(8);
        final String algorithm;
        final boolean allowed;
        final String description;
        final int expandedKeySize;
        final boolean exportable;
        final boolean isCBCMode;
        final int ivSize;
        final int keySize;
        final String transformation;

        BulkCipher(String transformation, int keySize, int expandedKeySize, int ivSize, boolean allowed) {
            boolean z = false;
            this.transformation = transformation;
            String[] splits = transformation.split("/");
            this.algorithm = splits[0];
            if (splits.length > 1) {
                z = "CBC".equalsIgnoreCase(splits[1]);
            }
            this.isCBCMode = z;
            this.description = this.algorithm + "/" + (keySize << 3);
            this.keySize = keySize;
            this.ivSize = ivSize;
            this.allowed = allowed;
            this.expandedKeySize = expandedKeySize;
            this.exportable = CipherSuite.DYNAMIC_AVAILABILITY;
        }

        BulkCipher(String transformation, int keySize, int ivSize, boolean allowed) {
            this.transformation = transformation;
            String[] splits = transformation.split("/");
            this.algorithm = splits[0];
            this.isCBCMode = splits.length <= 1 ? false : "CBC".equalsIgnoreCase(splits[1]);
            this.description = this.algorithm + "/" + (keySize << 3);
            this.keySize = keySize;
            this.ivSize = ivSize;
            this.allowed = allowed;
            this.expandedKeySize = keySize;
            this.exportable = false;
        }

        CipherBox newCipher(ProtocolVersion version, SecretKey key, IvParameterSpec iv, SecureRandom random, boolean encrypt) throws NoSuchAlgorithmException {
            return CipherBox.newCipherBox(version, this, key, iv, random, encrypt);
        }

        boolean isAvailable() {
            if (!this.allowed) {
                return false;
            }
            if (this == CipherSuite.B_AES_256) {
                return isAvailable(this);
            }
            return CipherSuite.DYNAMIC_AVAILABILITY;
        }

        static synchronized void clearAvailableCache() {
            synchronized (BulkCipher.class) {
                availableCache.clear();
            }
        }

        private static synchronized boolean isAvailable(BulkCipher cipher) {
            boolean booleanValue;
            synchronized (BulkCipher.class) {
                Boolean b = (Boolean) availableCache.get(cipher);
                if (b == null) {
                    try {
                        BulkCipher bulkCipher = cipher;
                        bulkCipher.newCipher(ProtocolVersion.DEFAULT, new SecretKeySpec(new byte[cipher.expandedKeySize], cipher.algorithm), new IvParameterSpec(new byte[cipher.ivSize]), null, CipherSuite.DYNAMIC_AVAILABILITY);
                        b = Boolean.TRUE;
                    } catch (NoSuchAlgorithmException e) {
                        b = Boolean.FALSE;
                    }
                    availableCache.put(cipher, b);
                }
                booleanValue = b.booleanValue();
            }
            return booleanValue;
        }

        public String toString() {
            return this.description;
        }
    }

    enum KeyExchange {
        K_NULL("NULL", false),
        K_RSA("RSA", CipherSuite.DYNAMIC_AVAILABILITY),
        K_RSA_EXPORT("RSA_EXPORT", CipherSuite.DYNAMIC_AVAILABILITY),
        K_DH_RSA("DH_RSA", false),
        K_DH_DSS("DH_DSS", false),
        K_DHE_DSS("DHE_DSS", CipherSuite.DYNAMIC_AVAILABILITY),
        K_DHE_RSA("DHE_RSA", CipherSuite.DYNAMIC_AVAILABILITY),
        K_DH_ANON("DH_anon", CipherSuite.DYNAMIC_AVAILABILITY),
        K_ECDH_ECDSA("ECDH_ECDSA", CipherSuite.ALLOW_ECC),
        K_ECDH_RSA("ECDH_RSA", CipherSuite.ALLOW_ECC),
        K_ECDHE_ECDSA("ECDHE_ECDSA", CipherSuite.ALLOW_ECC),
        K_ECDHE_RSA("ECDHE_RSA", CipherSuite.ALLOW_ECC),
        K_ECDH_ANON("ECDH_anon", CipherSuite.ALLOW_ECC),
        K_KRB5("KRB5", CipherSuite.DYNAMIC_AVAILABILITY),
        K_KRB5_EXPORT("KRB5_EXPORT", CipherSuite.DYNAMIC_AVAILABILITY),
        K_SCSV("SCSV", CipherSuite.DYNAMIC_AVAILABILITY);
        
        final boolean allowed;
        private final boolean alwaysAvailable;
        final String name;

        private KeyExchange(String name, boolean allowed) {
            boolean z = false;
            this.name = name;
            this.allowed = allowed;
            if (!(!allowed || name.startsWith("EC") || name.startsWith("KRB"))) {
                z = CipherSuite.DYNAMIC_AVAILABILITY;
            }
            this.alwaysAvailable = z;
        }

        boolean isAvailable() {
            boolean z = false;
            if (this.alwaysAvailable) {
                return CipherSuite.DYNAMIC_AVAILABILITY;
            }
            if (this.name.startsWith("EC")) {
                if (this.allowed) {
                    z = JsseJce.isEcAvailable();
                }
                return z;
            } else if (!this.name.startsWith("KRB")) {
                return this.allowed;
            } else {
                if (this.allowed) {
                    z = JsseJce.isKerberosAvailable();
                }
                return z;
            }
        }

        public String toString() {
            return this.name;
        }
    }

    static final class MacAlg {
        final int hashBlockSize;
        final int minimalPaddingSize;
        final String name;
        final int size;

        MacAlg(String name, int size, int hashBlockSize, int minimalPaddingSize) {
            this.name = name;
            this.size = size;
            this.hashBlockSize = hashBlockSize;
            this.minimalPaddingSize = minimalPaddingSize;
        }

        MAC newMac(ProtocolVersion protocolVersion, SecretKey secret) throws NoSuchAlgorithmException, InvalidKeyException {
            return new MAC(this, protocolVersion, secret);
        }

        public String toString() {
            return this.name;
        }
    }

    enum PRF {
        P_NONE("NONE", 0, 0),
        P_SHA256("SHA-256", 32, 64),
        P_SHA384("SHA-384", 48, 128),
        P_SHA512("SHA-512", 64, 128);
        
        private final int prfBlockSize;
        private final String prfHashAlg;
        private final int prfHashLength;

        private PRF(String prfHashAlg, int prfHashLength, int prfBlockSize) {
            this.prfHashAlg = prfHashAlg;
            this.prfHashLength = prfHashLength;
            this.prfBlockSize = prfBlockSize;
        }

        String getPRFHashAlg() {
            return this.prfHashAlg;
        }

        int getPRFHashLength() {
            return this.prfHashLength;
        }

        int getPRFBlockSize() {
            return this.prfBlockSize;
        }
    }

    static {
        boolean N;
        if (SunJSSE.isFIPS()) {
            N = false;
        } else {
            N = DYNAMIC_AVAILABILITY;
        }
        add("SSL_NULL_WITH_NULL_NULL", 0, 1, KeyExchange.K_NULL, B_NULL, false);
        int tls11 = ProtocolVersion.TLS11.v;
        int tls12 = ProtocolVersion.TLS12.v;
        add("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", 49188, 599, KeyExchange.K_ECDHE_ECDSA, B_AES_256, DYNAMIC_AVAILABILITY, 65535, tls12, PRF.P_SHA384);
        int p = 599 - 1;
        add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", 49192, p, KeyExchange.K_ECDHE_RSA, B_AES_256, DYNAMIC_AVAILABILITY, 65535, tls12, PRF.P_SHA384);
        p--;
        add("TLS_RSA_WITH_AES_256_CBC_SHA256", 61, p, KeyExchange.K_RSA, B_AES_256, DYNAMIC_AVAILABILITY, 65535, tls12, PRF.P_SHA256);
        p--;
        add("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384", 49190, p, KeyExchange.K_ECDH_ECDSA, B_AES_256, DYNAMIC_AVAILABILITY, 65535, tls12, PRF.P_SHA384);
        p--;
        add("TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384", 49194, p, KeyExchange.K_ECDH_RSA, B_AES_256, DYNAMIC_AVAILABILITY, 65535, tls12, PRF.P_SHA384);
        p--;
        add("TLS_DHE_RSA_WITH_AES_256_CBC_SHA256", 107, p, KeyExchange.K_DHE_RSA, B_AES_256, DYNAMIC_AVAILABILITY, 65535, tls12, PRF.P_SHA256);
        p--;
        add("TLS_DHE_DSS_WITH_AES_256_CBC_SHA256", 106, p, KeyExchange.K_DHE_DSS, B_AES_256, DYNAMIC_AVAILABILITY, 65535, tls12, PRF.P_SHA256);
        p--;
        add("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA", 49162, p, KeyExchange.K_ECDHE_ECDSA, B_AES_256, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", 49172, p, KeyExchange.K_ECDHE_RSA, B_AES_256, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_RSA_WITH_AES_256_CBC_SHA", 53, p, KeyExchange.K_RSA, B_AES_256, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA", 49157, p, KeyExchange.K_ECDH_ECDSA, B_AES_256, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_ECDH_RSA_WITH_AES_256_CBC_SHA", 49167, p, KeyExchange.K_ECDH_RSA, B_AES_256, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_DHE_RSA_WITH_AES_256_CBC_SHA", 57, p, KeyExchange.K_DHE_RSA, B_AES_256, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_DHE_DSS_WITH_AES_256_CBC_SHA", 56, p, KeyExchange.K_DHE_DSS, B_AES_256, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", 49187, p, KeyExchange.K_ECDHE_ECDSA, B_AES_128, DYNAMIC_AVAILABILITY, 65535, tls12, PRF.P_SHA256);
        p--;
        add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", 49191, p, KeyExchange.K_ECDHE_RSA, B_AES_128, DYNAMIC_AVAILABILITY, 65535, tls12, PRF.P_SHA256);
        p--;
        add("TLS_RSA_WITH_AES_128_CBC_SHA256", 60, p, KeyExchange.K_RSA, B_AES_128, DYNAMIC_AVAILABILITY, 65535, tls12, PRF.P_SHA256);
        p--;
        add("TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256", 49189, p, KeyExchange.K_ECDH_ECDSA, B_AES_128, DYNAMIC_AVAILABILITY, 65535, tls12, PRF.P_SHA256);
        p--;
        add("TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256", 49193, p, KeyExchange.K_ECDH_RSA, B_AES_128, DYNAMIC_AVAILABILITY, 65535, tls12, PRF.P_SHA256);
        p--;
        add("TLS_DHE_RSA_WITH_AES_128_CBC_SHA256", 103, p, KeyExchange.K_DHE_RSA, B_AES_128, DYNAMIC_AVAILABILITY, 65535, tls12, PRF.P_SHA256);
        p--;
        add("TLS_DHE_DSS_WITH_AES_128_CBC_SHA256", 64, p, KeyExchange.K_DHE_DSS, B_AES_128, DYNAMIC_AVAILABILITY, 65535, tls12, PRF.P_SHA256);
        p--;
        add("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", 49161, p, KeyExchange.K_ECDHE_ECDSA, B_AES_128, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", 49171, p, KeyExchange.K_ECDHE_RSA, B_AES_128, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_RSA_WITH_AES_128_CBC_SHA", 47, p, KeyExchange.K_RSA, B_AES_128, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA", 49156, p, KeyExchange.K_ECDH_ECDSA, B_AES_128, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_ECDH_RSA_WITH_AES_128_CBC_SHA", 49166, p, KeyExchange.K_ECDH_RSA, B_AES_128, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_DHE_RSA_WITH_AES_128_CBC_SHA", 51, p, KeyExchange.K_DHE_RSA, B_AES_128, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_DHE_DSS_WITH_AES_128_CBC_SHA", 50, p, KeyExchange.K_DHE_DSS, B_AES_128, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_ECDHE_ECDSA_WITH_RC4_128_SHA", 49159, p, KeyExchange.K_ECDHE_ECDSA, B_RC4_128, N);
        p--;
        add("TLS_ECDHE_RSA_WITH_RC4_128_SHA", 49169, p, KeyExchange.K_ECDHE_RSA, B_RC4_128, N);
        p--;
        add("SSL_RSA_WITH_RC4_128_SHA", 5, p, KeyExchange.K_RSA, B_RC4_128, N);
        p--;
        add("TLS_ECDH_ECDSA_WITH_RC4_128_SHA", 49154, p, KeyExchange.K_ECDH_ECDSA, B_RC4_128, N);
        p--;
        add("TLS_ECDH_RSA_WITH_RC4_128_SHA", 49164, p, KeyExchange.K_ECDH_RSA, B_RC4_128, N);
        p--;
        add("TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA", 49160, p, KeyExchange.K_ECDHE_ECDSA, B_3DES, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA", 49170, p, KeyExchange.K_ECDHE_RSA, B_3DES, DYNAMIC_AVAILABILITY);
        p--;
        add("SSL_RSA_WITH_3DES_EDE_CBC_SHA", 10, p, KeyExchange.K_RSA, B_3DES, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA", 49155, p, KeyExchange.K_ECDH_ECDSA, B_3DES, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA", 49165, p, KeyExchange.K_ECDH_RSA, B_3DES, DYNAMIC_AVAILABILITY);
        p--;
        add("SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", 22, p, KeyExchange.K_DHE_RSA, B_3DES, DYNAMIC_AVAILABILITY);
        p--;
        add("SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", 19, p, KeyExchange.K_DHE_DSS, B_3DES, N);
        p--;
        add("SSL_RSA_WITH_RC4_128_MD5", 4, p, KeyExchange.K_RSA, B_RC4_128, N);
        add("TLS_EMPTY_RENEGOTIATION_INFO_SCSV", 255, p - 1, KeyExchange.K_SCSV, B_NULL, DYNAMIC_AVAILABILITY);
        add("TLS_DH_anon_WITH_AES_256_CBC_SHA256", 109, 299, KeyExchange.K_DH_ANON, B_AES_256, N, 65535, tls12, PRF.P_SHA256);
        p = 299 - 1;
        add("TLS_ECDH_anon_WITH_AES_256_CBC_SHA", 49177, p, KeyExchange.K_ECDH_ANON, B_AES_256, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_DH_anon_WITH_AES_256_CBC_SHA", 58, p, KeyExchange.K_DH_ANON, B_AES_256, N);
        p--;
        add("TLS_DH_anon_WITH_AES_128_CBC_SHA256", 108, p, KeyExchange.K_DH_ANON, B_AES_128, N, 65535, tls12, PRF.P_SHA256);
        p--;
        add("TLS_ECDH_anon_WITH_AES_128_CBC_SHA", 49176, p, KeyExchange.K_ECDH_ANON, B_AES_128, DYNAMIC_AVAILABILITY);
        p--;
        add("TLS_DH_anon_WITH_AES_128_CBC_SHA", 52, p, KeyExchange.K_DH_ANON, B_AES_128, N);
        p--;
        add("TLS_ECDH_anon_WITH_RC4_128_SHA", 49174, p, KeyExchange.K_ECDH_ANON, B_RC4_128, N);
        p--;
        add("SSL_DH_anon_WITH_RC4_128_MD5", 24, p, KeyExchange.K_DH_ANON, B_RC4_128, N);
        p--;
        add("TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA", 49175, p, KeyExchange.K_ECDH_ANON, B_3DES, DYNAMIC_AVAILABILITY);
        p--;
        add("SSL_DH_anon_WITH_3DES_EDE_CBC_SHA", 27, p, KeyExchange.K_DH_ANON, B_3DES, N);
        p--;
        add("TLS_RSA_WITH_NULL_SHA256", 59, p, KeyExchange.K_RSA, B_NULL, N, 65535, tls12, PRF.P_SHA256);
        p--;
        add("TLS_ECDHE_ECDSA_WITH_NULL_SHA", 49158, p, KeyExchange.K_ECDHE_ECDSA, B_NULL, N);
        p--;
        add("TLS_ECDHE_RSA_WITH_NULL_SHA", 49168, p, KeyExchange.K_ECDHE_RSA, B_NULL, N);
        p--;
        add("SSL_RSA_WITH_NULL_SHA", 2, p, KeyExchange.K_RSA, B_NULL, N);
        p--;
        add("TLS_ECDH_ECDSA_WITH_NULL_SHA", 49153, p, KeyExchange.K_ECDH_ECDSA, B_NULL, N);
        p--;
        add("TLS_ECDH_RSA_WITH_NULL_SHA", 49163, p, KeyExchange.K_ECDH_RSA, B_NULL, N);
        p--;
        add("TLS_ECDH_anon_WITH_NULL_SHA", 49173, p, KeyExchange.K_ECDH_ANON, B_NULL, N);
        p--;
        add("SSL_RSA_WITH_NULL_MD5", 1, p, KeyExchange.K_RSA, B_NULL, N);
        p--;
        add("SSL_RSA_WITH_DES_CBC_SHA", 9, p, KeyExchange.K_RSA, B_DES, N, tls12);
        p--;
        add("SSL_DHE_RSA_WITH_DES_CBC_SHA", 21, p, KeyExchange.K_DHE_RSA, B_DES, N, tls12);
        p--;
        add("SSL_DHE_DSS_WITH_DES_CBC_SHA", 18, p, KeyExchange.K_DHE_DSS, B_DES, N, tls12);
        p--;
        add("SSL_DH_anon_WITH_DES_CBC_SHA", 26, p, KeyExchange.K_DH_ANON, B_DES, N, tls12);
        p--;
        add("SSL_RSA_EXPORT_WITH_RC4_40_MD5", 3, p, KeyExchange.K_RSA_EXPORT, B_RC4_40, N, tls11);
        p--;
        add("SSL_DH_anon_EXPORT_WITH_RC4_40_MD5", 23, p, KeyExchange.K_DH_ANON, B_RC4_40, N, tls11);
        p--;
        add("SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", 8, p, KeyExchange.K_RSA_EXPORT, B_DES_40, N, tls11);
        p--;
        add("SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", 20, p, KeyExchange.K_DHE_RSA, B_DES_40, N, tls11);
        p--;
        add("SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", 17, p, KeyExchange.K_DHE_DSS, B_DES_40, N, tls11);
        p--;
        add("SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA", 25, p, KeyExchange.K_DH_ANON, B_DES_40, N, tls11);
        p--;
        add("TLS_KRB5_WITH_RC4_128_SHA", 32, p, KeyExchange.K_KRB5, B_RC4_128, N);
        p--;
        add("TLS_KRB5_WITH_RC4_128_MD5", 36, p, KeyExchange.K_KRB5, B_RC4_128, N);
        p--;
        add("TLS_KRB5_WITH_3DES_EDE_CBC_SHA", 31, p, KeyExchange.K_KRB5, B_3DES, N);
        p--;
        add("TLS_KRB5_WITH_3DES_EDE_CBC_MD5", 35, p, KeyExchange.K_KRB5, B_3DES, N);
        p--;
        add("TLS_KRB5_WITH_DES_CBC_SHA", 30, p, KeyExchange.K_KRB5, B_DES, N, tls12);
        p--;
        add("TLS_KRB5_WITH_DES_CBC_MD5", 34, p, KeyExchange.K_KRB5, B_DES, N, tls12);
        p--;
        add("TLS_KRB5_EXPORT_WITH_RC4_40_SHA", 40, p, KeyExchange.K_KRB5_EXPORT, B_RC4_40, N, tls11);
        p--;
        add("TLS_KRB5_EXPORT_WITH_RC4_40_MD5", 43, p, KeyExchange.K_KRB5_EXPORT, B_RC4_40, N, tls11);
        p--;
        add("TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA", 38, p, KeyExchange.K_KRB5_EXPORT, B_DES_40, N, tls11);
        add("TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5", 41, p - 1, KeyExchange.K_KRB5_EXPORT, B_DES_40, N, tls11);
        add("SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5", 6);
        add("SSL_RSA_WITH_IDEA_CBC_SHA", 7);
        add("SSL_DH_DSS_EXPORT_WITH_DES40_CBC_SHA", 11);
        add("SSL_DH_DSS_WITH_DES_CBC_SHA", 12);
        add("SSL_DH_DSS_WITH_3DES_EDE_CBC_SHA", 13);
        add("SSL_DH_RSA_EXPORT_WITH_DES40_CBC_SHA", 14);
        add("SSL_DH_RSA_WITH_DES_CBC_SHA", 15);
        add("SSL_DH_RSA_WITH_3DES_EDE_CBC_SHA", 16);
        add("SSL_FORTEZZA_DMS_WITH_NULL_SHA", 28);
        add("SSL_FORTEZZA_DMS_WITH_FORTEZZA_CBC_SHA", 29);
        add("SSL_RSA_EXPORT1024_WITH_DES_CBC_SHA", 98);
        add("SSL_DHE_DSS_EXPORT1024_WITH_DES_CBC_SHA", 99);
        add("SSL_RSA_EXPORT1024_WITH_RC4_56_SHA", 100);
        add("SSL_DHE_DSS_EXPORT1024_WITH_RC4_56_SHA", 101);
        add("SSL_DHE_DSS_WITH_RC4_128_SHA", 102);
        add("NETSCAPE_RSA_FIPS_WITH_3DES_EDE_CBC_SHA", 65504);
        add("NETSCAPE_RSA_FIPS_WITH_DES_CBC_SHA", 65505);
        add("SSL_RSA_FIPS_WITH_DES_CBC_SHA", 65278);
        add("SSL_RSA_FIPS_WITH_3DES_EDE_CBC_SHA", 65279);
        add("TLS_KRB5_WITH_IDEA_CBC_SHA", 33);
        add("TLS_KRB5_WITH_IDEA_CBC_MD5", 37);
        add("TLS_KRB5_EXPORT_WITH_RC2_CBC_40_SHA", 39);
        add("TLS_KRB5_EXPORT_WITH_RC2_CBC_40_MD5", 42);
        add("TLS_RSA_WITH_SEED_CBC_SHA", 150);
        add("TLS_DH_DSS_WITH_SEED_CBC_SHA", 151);
        add("TLS_DH_RSA_WITH_SEED_CBC_SHA", 152);
        add("TLS_DHE_DSS_WITH_SEED_CBC_SHA", 153);
        add("TLS_DHE_RSA_WITH_SEED_CBC_SHA", 154);
        add("TLS_DH_anon_WITH_SEED_CBC_SHA", 155);
        add("TLS_PSK_WITH_RC4_128_SHA", 138);
        add("TLS_PSK_WITH_3DES_EDE_CBC_SHA", 139);
        add("TLS_PSK_WITH_AES_128_CBC_SHA", 140);
        add("TLS_PSK_WITH_AES_256_CBC_SHA", 141);
        add("TLS_DHE_PSK_WITH_RC4_128_SHA", 142);
        add("TLS_DHE_PSK_WITH_3DES_EDE_CBC_SHA", 143);
        add("TLS_DHE_PSK_WITH_AES_128_CBC_SHA", 144);
        add("TLS_DHE_PSK_WITH_AES_256_CBC_SHA", 145);
        add("TLS_RSA_PSK_WITH_RC4_128_SHA", 146);
        add("TLS_RSA_PSK_WITH_3DES_EDE_CBC_SHA", 147);
        add("TLS_RSA_PSK_WITH_AES_128_CBC_SHA", 148);
        add("TLS_RSA_PSK_WITH_AES_256_CBC_SHA", 149);
        add("TLS_PSK_WITH_NULL_SHA", 44);
        add("TLS_DHE_PSK_WITH_NULL_SHA", 45);
        add("TLS_RSA_PSK_WITH_NULL_SHA", 46);
        add("TLS_DH_DSS_WITH_AES_128_CBC_SHA", 48);
        add("TLS_DH_RSA_WITH_AES_128_CBC_SHA", 49);
        add("TLS_DH_DSS_WITH_AES_256_CBC_SHA", 54);
        add("TLS_DH_RSA_WITH_AES_256_CBC_SHA", 55);
        add("TLS_DH_DSS_WITH_AES_128_CBC_SHA256", 62);
        add("TLS_DH_RSA_WITH_AES_128_CBC_SHA256", 63);
        add("TLS_DH_DSS_WITH_AES_256_CBC_SHA256", 104);
        add("TLS_DH_RSA_WITH_AES_256_CBC_SHA256", 105);
        add("TLS_RSA_WITH_AES_128_GCM_SHA256", 156);
        add("TLS_RSA_WITH_AES_256_GCM_SHA384", 157);
        add("TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", 158);
        add("TLS_DHE_RSA_WITH_AES_256_GCM_SHA384", 159);
        add("TLS_DH_RSA_WITH_AES_128_GCM_SHA256", 160);
        add("TLS_DH_RSA_WITH_AES_256_GCM_SHA384", 161);
        add("TLS_DHE_DSS_WITH_AES_128_GCM_SHA256", 162);
        add("TLS_DHE_DSS_WITH_AES_256_GCM_SHA384", 163);
        add("TLS_DH_DSS_WITH_AES_128_GCM_SHA256", 164);
        add("TLS_DH_DSS_WITH_AES_256_GCM_SHA384", 165);
        add("TLS_DH_anon_WITH_AES_128_GCM_SHA256", 166);
        add("TLS_DH_anon_WITH_AES_256_GCM_SHA384", 167);
        add("TLS_PSK_WITH_AES_128_GCM_SHA256", 168);
        add("TLS_PSK_WITH_AES_256_GCM_SHA384", 169);
        add("TLS_DHE_PSK_WITH_AES_128_GCM_SHA256", 170);
        add("TLS_DHE_PSK_WITH_AES_256_GCM_SHA384", 171);
        add("TLS_RSA_PSK_WITH_AES_128_GCM_SHA256", 172);
        add("TLS_RSA_PSK_WITH_AES_256_GCM_SHA384", 173);
        add("TLS_PSK_WITH_AES_128_CBC_SHA256", 174);
        add("TLS_PSK_WITH_AES_256_CBC_SHA384", 175);
        add("TLS_PSK_WITH_NULL_SHA256", 176);
        add("TLS_PSK_WITH_NULL_SHA384", 177);
        add("TLS_DHE_PSK_WITH_AES_128_CBC_SHA256", 178);
        add("TLS_DHE_PSK_WITH_AES_256_CBC_SHA384", 179);
        add("TLS_DHE_PSK_WITH_NULL_SHA256", 180);
        add("TLS_DHE_PSK_WITH_NULL_SHA384", 181);
        add("TLS_RSA_PSK_WITH_AES_128_CBC_SHA256", 182);
        add("TLS_RSA_PSK_WITH_AES_256_CBC_SHA384", 183);
        add("TLS_RSA_PSK_WITH_NULL_SHA256", 184);
        add("TLS_RSA_PSK_WITH_NULL_SHA384", 185);
        add("TLS_RSA_WITH_CAMELLIA_128_CBC_SHA", 65);
        add("TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA", 66);
        add("TLS_DH_RSA_WITH_CAMELLIA_128_CBC_SHA", 67);
        add("TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA", 68);
        add("TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA", 69);
        add("TLS_DH_anon_WITH_CAMELLIA_128_CBC_SHA", 70);
        add("TLS_RSA_WITH_CAMELLIA_256_CBC_SHA", 132);
        add("TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA", 133);
        add("TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA", 134);
        add("TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA", 135);
        add("TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA", 136);
        add("TLS_DH_anon_WITH_CAMELLIA_256_CBC_SHA", 137);
        add("TLS_RSA_WITH_CAMELLIA_128_CBC_SHA256", 186);
        add("TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA256", 187);
        add("TLS_DH_RSA_WITH_CAMELLIA_128_CBC_SHA256", 188);
        add("TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA256", 189);
        add("TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA256", 190);
        add("TLS_DH_anon_WITH_CAMELLIA_128_CBC_SHA256", 191);
        add("TLS_RSA_WITH_CAMELLIA_256_CBC_SHA256", 192);
        add("TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA256", 193);
        add("TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA256", 194);
        add("TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA256", 195);
        add("TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA256", 196);
        add("TLS_DH_anon_WITH_CAMELLIA_256_CBC_SHA256", 197);
        add("TLS_SRP_SHA_WITH_3DES_EDE_CBC_SHA", 49178);
        add("TLS_SRP_SHA_RSA_WITH_3DES_EDE_CBC_SHA", 49179);
        add("TLS_SRP_SHA_DSS_WITH_3DES_EDE_CBC_SHA", 49180);
        add("TLS_SRP_SHA_WITH_AES_128_CBC_SHA", 49181);
        add("TLS_SRP_SHA_RSA_WITH_AES_128_CBC_SHA", 49182);
        add("TLS_SRP_SHA_DSS_WITH_AES_128_CBC_SHA", 49183);
        add("TLS_SRP_SHA_WITH_AES_256_CBC_SHA", 49184);
        add("TLS_SRP_SHA_RSA_WITH_AES_256_CBC_SHA", 49185);
        add("TLS_SRP_SHA_DSS_WITH_AES_256_CBC_SHA", 49186);
        add("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", 49195);
        add("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", 49196);
        add("TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256", 49197);
        add("TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384", 49198);
        add("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", 49199);
        add("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", 49200);
        add("TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256", 49201);
        add("TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384", 49202);
        add("TLS_ECDHE_PSK_WITH_RC4_128_SHA", 49203);
        add("TLS_ECDHE_PSK_WITH_3DES_EDE_CBC_SHA", 49204);
        add("TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA", 49205);
        add("TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA", 49206);
        add("TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA256", 49207);
        add("TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA384", 49208);
        add("TLS_ECDHE_PSK_WITH_NULL_SHA", 49209);
        add("TLS_ECDHE_PSK_WITH_NULL_SHA256", 49210);
        add("TLS_ECDHE_PSK_WITH_NULL_SHA384", 49211);
    }

    private CipherSuite(String name, int id, int priority, KeyExchange keyExchange, BulkCipher cipher, boolean allowed, int obsoleted, int supported, PRF prfAlg) {
        this.name = name;
        this.id = id;
        this.priority = priority;
        this.keyExchange = keyExchange;
        this.cipher = cipher;
        this.exportable = cipher.exportable;
        if (name.endsWith("_MD5")) {
            this.macAlg = M_MD5;
        } else if (name.endsWith("_SHA")) {
            this.macAlg = M_SHA;
        } else if (name.endsWith("_SHA256")) {
            this.macAlg = M_SHA256;
        } else if (name.endsWith("_SHA384")) {
            this.macAlg = M_SHA384;
        } else if (name.endsWith("_NULL")) {
            this.macAlg = M_NULL;
        } else if (name.endsWith("_SCSV")) {
            this.macAlg = M_NULL;
        } else {
            throw new IllegalArgumentException("Unknown MAC algorithm for ciphersuite " + name);
        }
        this.allowed = (allowed & keyExchange.allowed) & cipher.allowed;
        this.obsoleted = obsoleted;
        this.supported = supported;
        this.prfAlg = prfAlg;
    }

    private CipherSuite(String name, int id) {
        this.name = name;
        this.id = id;
        this.allowed = false;
        this.priority = 0;
        this.keyExchange = null;
        this.cipher = null;
        this.macAlg = null;
        this.exportable = false;
        this.obsoleted = 65535;
        this.supported = 0;
        this.prfAlg = PRF.P_NONE;
    }

    boolean isAvailable() {
        return (this.allowed && this.keyExchange.isAvailable()) ? this.cipher.isAvailable() : false;
    }

    boolean isNegotiable() {
        return this != C_SCSV ? isAvailable() : false;
    }

    public int compareTo(Object o) {
        return ((CipherSuite) o).priority - this.priority;
    }

    public String toString() {
        return this.name;
    }

    static CipherSuite valueOf(String s) {
        if (s == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        CipherSuite c = (CipherSuite) nameMap.get(s);
        if (c != null && c.allowed) {
            return c;
        }
        throw new IllegalArgumentException("Unsupported ciphersuite " + s);
    }

    static CipherSuite valueOf(int id1, int id2) {
        id1 &= 255;
        id2 &= 255;
        int id = (id1 << 8) | id2;
        CipherSuite c = (CipherSuite) idMap.get(Integer.valueOf(id));
        if (c != null) {
            return c;
        }
        String h1 = Integer.toString(id1, 16);
        return new CipherSuite("Unknown 0x" + h1 + ":0x" + Integer.toString(id2, 16), id);
    }

    static Collection<CipherSuite> allowedCipherSuites() {
        return nameMap.values();
    }

    private static void add(String name, int id, int priority, KeyExchange keyExchange, BulkCipher cipher, boolean allowed, int obsoleted, int supported, PRF prf) {
        CipherSuite c = new CipherSuite(name, id, priority, keyExchange, cipher, allowed, obsoleted, supported, prf);
        if (idMap.put(Integer.valueOf(id), c) != null) {
            throw new RuntimeException("Duplicate ciphersuite definition: " + id + ", " + name);
        } else if (c.allowed && nameMap.put(name, c) != null) {
            throw new RuntimeException("Duplicate ciphersuite definition: " + id + ", " + name);
        }
    }

    private static void add(String name, int id, int priority, KeyExchange keyExchange, BulkCipher cipher, boolean allowed, int obsoleted) {
        PRF prf = PRF.P_SHA256;
        if (obsoleted < ProtocolVersion.TLS12.v) {
            prf = PRF.P_NONE;
        }
        add(name, id, priority, keyExchange, cipher, allowed, obsoleted, 0, prf);
    }

    private static void add(String name, int id, int priority, KeyExchange keyExchange, BulkCipher cipher, boolean allowed) {
        add(name, id, priority, keyExchange, cipher, allowed, 65535);
    }

    private static void add(String name, int id) {
        if (idMap.put(Integer.valueOf(id), new CipherSuite(name, id)) != null) {
            throw new RuntimeException("Duplicate ciphersuite definition: " + id + ", " + name);
        }
    }
}
