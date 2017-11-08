package sun.security.ssl;

import java.security.AlgorithmConstraints;
import java.security.CryptoPrimitive;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import sun.security.util.KeyUtil;

final class SignatureAndHashAlgorithm {
    private static final Set<CryptoPrimitive> SIGNATURE_PRIMITIVE_SET = EnumSet.of(CryptoPrimitive.SIGNATURE);
    static final int SUPPORTED_ALG_PRIORITY_MAX_NUM = 240;
    private static final Map<Integer, SignatureAndHashAlgorithm> priorityMap = Collections.synchronizedSortedMap(new TreeMap());
    private static final Map<Integer, SignatureAndHashAlgorithm> supportedMap = Collections.synchronizedSortedMap(new TreeMap());
    private String algorithm;
    private HashAlgorithm hash;
    private int id;
    private int priority;
    private SignatureAlgorithm signature;

    enum HashAlgorithm {
        UNDEFINED("undefined", "", -1, -1),
        NONE("none", "NONE", 0, -1),
        MD5("md5", "MD5", 1, 16),
        SHA1("sha1", "SHA-1", 2, 20),
        SHA224("sha224", "SHA-224", 3, 28),
        SHA256("sha256", "SHA-256", 4, 32),
        SHA384("sha384", "SHA-384", 5, 48),
        SHA512("sha512", "SHA-512", 6, 64);
        
        final int length;
        final String name;
        final String standardName;
        final int value;

        private HashAlgorithm(String name, String standardName, int value, int length) {
            this.name = name;
            this.standardName = standardName;
            this.value = value;
            this.length = length;
        }

        static HashAlgorithm valueOf(int value) {
            HashAlgorithm algorithm = UNDEFINED;
            switch (value) {
                case 0:
                    return NONE;
                case 1:
                    return MD5;
                case 2:
                    return SHA1;
                case 3:
                    return SHA224;
                case 4:
                    return SHA256;
                case 5:
                    return SHA384;
                case 6:
                    return SHA512;
                default:
                    return algorithm;
            }
        }
    }

    enum SignatureAlgorithm {
        UNDEFINED("undefined", -1),
        ANONYMOUS("anonymous", 0),
        RSA("rsa", 1),
        DSA("dsa", 2),
        ECDSA("ecdsa", 3);
        
        final String name;
        final int value;

        private SignatureAlgorithm(String name, int value) {
            this.name = name;
            this.value = value;
        }

        static SignatureAlgorithm valueOf(int value) {
            SignatureAlgorithm algorithm = UNDEFINED;
            switch (value) {
                case 0:
                    return ANONYMOUS;
                case 1:
                    return RSA;
                case 2:
                    return DSA;
                case 3:
                    return ECDSA;
                default:
                    return algorithm;
            }
        }
    }

    static {
        synchronized (supportedMap) {
            supports(HashAlgorithm.MD5, SignatureAlgorithm.RSA, "MD5withRSA", 239);
            int p = 239 - 1;
            supports(HashAlgorithm.SHA1, SignatureAlgorithm.DSA, "SHA1withDSA", p);
            p--;
            supports(HashAlgorithm.SHA1, SignatureAlgorithm.RSA, "SHA1withRSA", p);
            p--;
            supports(HashAlgorithm.SHA1, SignatureAlgorithm.ECDSA, "SHA1withECDSA", p);
            p--;
            supports(HashAlgorithm.SHA224, SignatureAlgorithm.RSA, "SHA224withRSA", p);
            p--;
            supports(HashAlgorithm.SHA224, SignatureAlgorithm.ECDSA, "SHA224withECDSA", p);
            p--;
            supports(HashAlgorithm.SHA256, SignatureAlgorithm.RSA, "SHA256withRSA", p);
            p--;
            supports(HashAlgorithm.SHA256, SignatureAlgorithm.ECDSA, "SHA256withECDSA", p);
            p--;
            supports(HashAlgorithm.SHA384, SignatureAlgorithm.RSA, "SHA384withRSA", p);
            p--;
            supports(HashAlgorithm.SHA384, SignatureAlgorithm.ECDSA, "SHA384withECDSA", p);
            p--;
            supports(HashAlgorithm.SHA512, SignatureAlgorithm.RSA, "SHA512withRSA", p);
            supports(HashAlgorithm.SHA512, SignatureAlgorithm.ECDSA, "SHA512withECDSA", p - 1);
        }
    }

    private SignatureAndHashAlgorithm(HashAlgorithm hash, SignatureAlgorithm signature, String algorithm, int priority) {
        this.hash = hash;
        this.signature = signature;
        this.algorithm = algorithm;
        this.id = ((hash.value & 255) << 8) | (signature.value & 255);
        this.priority = priority;
    }

    private SignatureAndHashAlgorithm(String algorithm, int id, int sequence) {
        this.hash = HashAlgorithm.valueOf((id >> 8) & 255);
        this.signature = SignatureAlgorithm.valueOf(id & 255);
        this.algorithm = algorithm;
        this.id = id;
        this.priority = (sequence + SUPPORTED_ALG_PRIORITY_MAX_NUM) + 1;
    }

    static SignatureAndHashAlgorithm valueOf(int hash, int signature, int sequence) {
        hash &= 255;
        signature &= 255;
        int id = (hash << 8) | signature;
        SignatureAndHashAlgorithm signAlg = (SignatureAndHashAlgorithm) supportedMap.get(Integer.valueOf(id));
        if (signAlg == null) {
            return new SignatureAndHashAlgorithm("Unknown (hash:0x" + Integer.toString(hash, 16) + ", signature:0x" + Integer.toString(signature, 16) + ")", id, sequence);
        }
        return signAlg;
    }

    int getHashValue() {
        return (this.id >> 8) & 255;
    }

    int getSignatureValue() {
        return this.id & 255;
    }

    String getAlgorithmName() {
        return this.algorithm;
    }

    static int sizeInRecord() {
        return 2;
    }

    static Collection<SignatureAndHashAlgorithm> getSupportedAlgorithms(AlgorithmConstraints constraints) {
        Collection<SignatureAndHashAlgorithm> supported = new ArrayList();
        synchronized (priorityMap) {
            for (SignatureAndHashAlgorithm sigAlg : priorityMap.values()) {
                if (sigAlg.priority <= SUPPORTED_ALG_PRIORITY_MAX_NUM && constraints.permits(SIGNATURE_PRIMITIVE_SET, sigAlg.algorithm, null)) {
                    supported.add(sigAlg);
                }
            }
        }
        return supported;
    }

    static Collection<SignatureAndHashAlgorithm> getSupportedAlgorithms(Collection<SignatureAndHashAlgorithm> algorithms) {
        Collection<SignatureAndHashAlgorithm> supported = new ArrayList();
        for (SignatureAndHashAlgorithm sigAlg : algorithms) {
            if (sigAlg.priority <= SUPPORTED_ALG_PRIORITY_MAX_NUM) {
                supported.add(sigAlg);
            }
        }
        return supported;
    }

    static String[] getAlgorithmNames(Collection<SignatureAndHashAlgorithm> algorithms) {
        ArrayList<String> algorithmNames = new ArrayList();
        if (algorithms != null) {
            for (SignatureAndHashAlgorithm sigAlg : algorithms) {
                algorithmNames.add(sigAlg.algorithm);
            }
        }
        return (String[]) algorithmNames.toArray(new String[algorithmNames.size()]);
    }

    static Set<String> getHashAlgorithmNames(Collection<SignatureAndHashAlgorithm> algorithms) {
        Set<String> algorithmNames = new HashSet();
        if (algorithms != null) {
            for (SignatureAndHashAlgorithm sigAlg : algorithms) {
                if (sigAlg.hash.value > 0) {
                    algorithmNames.add(sigAlg.hash.standardName);
                }
            }
        }
        return algorithmNames;
    }

    static String getHashAlgorithmName(SignatureAndHashAlgorithm algorithm) {
        return algorithm.hash.standardName;
    }

    private static void supports(HashAlgorithm hash, SignatureAlgorithm signature, String algorithm, int priority) {
        SignatureAndHashAlgorithm pair = new SignatureAndHashAlgorithm(hash, signature, algorithm, priority);
        if (supportedMap.put(Integer.valueOf(pair.id), pair) != null) {
            throw new RuntimeException("Duplicate SignatureAndHashAlgorithm definition, id: " + pair.id);
        } else if (priorityMap.put(Integer.valueOf(pair.priority), pair) != null) {
            throw new RuntimeException("Duplicate SignatureAndHashAlgorithm definition, priority: " + pair.priority);
        }
    }

    static SignatureAndHashAlgorithm getPreferableAlgorithm(Collection<SignatureAndHashAlgorithm> algorithms, String expected) {
        return getPreferableAlgorithm(algorithms, expected, null);
    }

    static SignatureAndHashAlgorithm getPreferableAlgorithm(Collection<SignatureAndHashAlgorithm> algorithms, String expected, PrivateKey signingKey) {
        if (expected == null && !algorithms.isEmpty()) {
            for (SignatureAndHashAlgorithm sigAlg : algorithms) {
                if (sigAlg.priority <= SUPPORTED_ALG_PRIORITY_MAX_NUM) {
                    return sigAlg;
                }
            }
            return null;
        } else if (expected == null) {
            return null;
        } else {
            int maxDigestLength = Integer.MAX_VALUE;
            if (signingKey != null && "rsa".equalsIgnoreCase(signingKey.getAlgorithm()) && expected.equalsIgnoreCase("rsa")) {
                int keySize = KeyUtil.getKeySize(signingKey);
                if (keySize >= 768) {
                    maxDigestLength = HashAlgorithm.SHA512.length;
                } else if (keySize >= 512 && keySize < 768) {
                    maxDigestLength = HashAlgorithm.SHA256.length;
                } else if (keySize > 0 && keySize < 512) {
                    maxDigestLength = HashAlgorithm.SHA1.length;
                }
            }
            for (SignatureAndHashAlgorithm algorithm : algorithms) {
                int signValue = algorithm.id & 255;
                if (!expected.equalsIgnoreCase("rsa") || signValue != SignatureAlgorithm.RSA.value) {
                    if (!((expected.equalsIgnoreCase("dsa") && signValue == SignatureAlgorithm.DSA.value) || (expected.equalsIgnoreCase("ecdsa") && signValue == SignatureAlgorithm.ECDSA.value))) {
                        if (expected.equalsIgnoreCase("ec") && signValue == SignatureAlgorithm.ECDSA.value) {
                        }
                    }
                    return algorithm;
                } else if (algorithm.hash.length <= maxDigestLength) {
                    return algorithm;
                }
            }
            return null;
        }
    }
}
