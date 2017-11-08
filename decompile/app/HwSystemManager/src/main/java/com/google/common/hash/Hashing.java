package com.google.common.hash;

import com.google.common.annotations.Beta;

@Beta
public final class Hashing {

    private static class Sha256Holder {
        static final HashFunction SHA_256 = new MessageDigestHashFunction("SHA-256", "Hashing.sha256()");

        private Sha256Holder() {
        }
    }

    public static HashFunction sha256() {
        return Sha256Holder.SHA_256;
    }

    private Hashing() {
    }
}
