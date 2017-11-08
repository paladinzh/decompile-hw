package sun.security.ssl;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

final class EphemeralKeyManager {
    private static final int INDEX_RSA1024 = 1;
    private static final int INDEX_RSA512 = 0;
    private final EphemeralKeyPair[] keys = new EphemeralKeyPair[]{new EphemeralKeyPair(null), new EphemeralKeyPair(null)};

    private static class EphemeralKeyPair {
        private static final int MAX_USE = 200;
        private static final long USE_INTERVAL = 3600000;
        private long expirationTime;
        private KeyPair keyPair;
        private int uses;

        private EphemeralKeyPair(KeyPair keyPair) {
            this.keyPair = keyPair;
            this.expirationTime = System.currentTimeMillis() + USE_INTERVAL;
        }

        private boolean isValid() {
            if (this.keyPair == null || this.uses >= 200 || System.currentTimeMillis() >= this.expirationTime) {
                return false;
            }
            return true;
        }

        private KeyPair getKeyPair() {
            if (isValid()) {
                this.uses++;
                return this.keyPair;
            }
            this.keyPair = null;
            return null;
        }
    }

    EphemeralKeyManager() {
    }

    KeyPair getRSAKeyPair(boolean export, SecureRandom random) {
        int length;
        int index;
        KeyPair kp;
        if (export) {
            length = 512;
            index = 0;
        } else {
            length = 1024;
            index = 1;
        }
        synchronized (this.keys) {
            kp = this.keys[index].getKeyPair();
            if (kp == null) {
                try {
                    KeyPairGenerator kgen = JsseJce.getKeyPairGenerator("RSA");
                    kgen.initialize(length, random);
                    this.keys[index] = new EphemeralKeyPair(kgen.genKeyPair());
                    kp = this.keys[index].getKeyPair();
                } catch (Exception e) {
                }
            }
        }
        return kp;
    }
}
