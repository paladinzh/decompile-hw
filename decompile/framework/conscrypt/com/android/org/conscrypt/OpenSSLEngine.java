package com.android.org.conscrypt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

public class OpenSSLEngine {
    private static final Object mLoadingLock = new Object();
    private final long ctx;

    private static class BoringSSL {
        public static final OpenSSLEngine INSTANCE = new OpenSSLEngine();

        private BoringSSL() {
        }
    }

    static {
        if (!NativeCrypto.isBoringSSL) {
            NativeCrypto.ENGINE_load_dynamic();
        }
    }

    public static OpenSSLEngine getInstance(String engine) throws IllegalArgumentException {
        if (NativeCrypto.isBoringSSL) {
            return BoringSSL.INSTANCE;
        }
        if (engine == null) {
            throw new NullPointerException("engine == null");
        }
        long engineCtx;
        synchronized (mLoadingLock) {
            engineCtx = NativeCrypto.ENGINE_by_id(engine);
            if (engineCtx == 0) {
                throw new IllegalArgumentException("Unknown ENGINE id: " + engine);
            }
            NativeCrypto.ENGINE_add(engineCtx);
        }
        return new OpenSSLEngine(engineCtx);
    }

    private OpenSSLEngine() {
        this.ctx = 0;
    }

    private OpenSSLEngine(long engineCtx) {
        this.ctx = engineCtx;
        if (NativeCrypto.ENGINE_init(engineCtx) == 0) {
            NativeCrypto.ENGINE_free(engineCtx);
            throw new IllegalArgumentException("Could not initialize engine");
        }
    }

    public PrivateKey getPrivateKeyById(String id) throws InvalidKeyException {
        if (id == null) {
            throw new NullPointerException("id == null");
        }
        long keyRef = NativeCrypto.ENGINE_load_private_key(this.ctx, id);
        if (keyRef == 0) {
            return null;
        }
        try {
            return new OpenSSLKey(keyRef, this, id).getPrivateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new InvalidKeyException(e);
        }
    }

    long getEngineContext() {
        return this.ctx;
    }

    protected void finalize() throws Throwable {
        try {
            if (!NativeCrypto.isBoringSSL) {
                NativeCrypto.ENGINE_finish(this.ctx);
                NativeCrypto.ENGINE_free(this.ctx);
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof OpenSSLEngine)) {
            return false;
        }
        OpenSSLEngine other = (OpenSSLEngine) o;
        if (other.getEngineContext() == this.ctx) {
            return true;
        }
        String id = NativeCrypto.ENGINE_get_id(this.ctx);
        if (id == null) {
            return false;
        }
        return id.equals(NativeCrypto.ENGINE_get_id(other.getEngineContext()));
    }

    public int hashCode() {
        return (int) this.ctx;
    }
}
