package com.common.imageloader.cache.disc.naming;

import com.common.imageloader.utils.L;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5FileNameGenerator implements FileNameGenerator {
    private static final String HASH_ALGORITHM = "MD5";
    private static final int RADIX = 36;

    public String generate(String imageUri) {
        return new BigInteger(getMD5(imageUri.getBytes(Charset.defaultCharset()))).abs().toString(36);
    }

    private byte[] getMD5(byte[] data) {
        byte[] hash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(data);
            hash = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            L.e(e);
        }
        return hash;
    }
}
