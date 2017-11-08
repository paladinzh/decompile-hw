package com.android.org.conscrypt;

import com.android.org.conscrypt.NativeRef.HMAC_CTX;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.MacSpi;
import javax.crypto.SecretKey;

public abstract class OpenSSLMac extends MacSpi {
    private HMAC_CTX ctx;
    private final long evp_md;
    private byte[] keyBytes;
    private final byte[] singleByte;
    private final int size;

    public static class HmacMD5 extends OpenSSLMac {
        private static final long EVP_MD = NativeCrypto.EVP_get_digestbyname("md5");
        private static final int SIZE = NativeCrypto.EVP_MD_size(EVP_MD);

        public HmacMD5() {
            super(EVP_MD, SIZE);
        }
    }

    public static class HmacSHA1 extends OpenSSLMac {
        private static final long EVP_MD = NativeCrypto.EVP_get_digestbyname("sha1");
        private static final int SIZE = NativeCrypto.EVP_MD_size(EVP_MD);

        public HmacSHA1() {
            super(EVP_MD, SIZE);
        }
    }

    public static class HmacSHA224 extends OpenSSLMac {
        private static final long EVP_MD = NativeCrypto.EVP_get_digestbyname("sha224");
        private static final int SIZE = NativeCrypto.EVP_MD_size(EVP_MD);

        public HmacSHA224() throws NoSuchAlgorithmException {
            super(EVP_MD, SIZE);
        }
    }

    public static class HmacSHA256 extends OpenSSLMac {
        private static final long EVP_MD = NativeCrypto.EVP_get_digestbyname("sha256");
        private static final int SIZE = NativeCrypto.EVP_MD_size(EVP_MD);

        public HmacSHA256() throws NoSuchAlgorithmException {
            super(EVP_MD, SIZE);
        }
    }

    public static class HmacSHA384 extends OpenSSLMac {
        private static final long EVP_MD = NativeCrypto.EVP_get_digestbyname("sha384");
        private static final int SIZE = NativeCrypto.EVP_MD_size(EVP_MD);

        public HmacSHA384() throws NoSuchAlgorithmException {
            super(EVP_MD, SIZE);
        }
    }

    public static class HmacSHA512 extends OpenSSLMac {
        private static final long EVP_MD = NativeCrypto.EVP_get_digestbyname("sha512");
        private static final int SIZE = NativeCrypto.EVP_MD_size(EVP_MD);

        public HmacSHA512() {
            super(EVP_MD, SIZE);
        }
    }

    private OpenSSLMac(long evp_md, int size) {
        this.singleByte = new byte[1];
        this.evp_md = evp_md;
        this.size = size;
    }

    protected int engineGetMacLength() {
        return this.size;
    }

    protected void engineInit(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (!(key instanceof SecretKey)) {
            throw new InvalidKeyException("key must be a SecretKey");
        } else if (params != null) {
            throw new InvalidAlgorithmParameterException("unknown parameter type");
        } else {
            this.keyBytes = key.getEncoded();
            if (this.keyBytes == null) {
                throw new InvalidKeyException("key cannot be encoded");
            }
            resetContext();
        }
    }

    private final void resetContext() {
        HMAC_CTX ctxLocal = new HMAC_CTX(NativeCrypto.HMAC_CTX_new());
        if (this.keyBytes != null) {
            NativeCrypto.HMAC_Init_ex(ctxLocal, this.keyBytes, this.evp_md);
        }
        this.ctx = ctxLocal;
    }

    protected void engineUpdate(byte input) {
        this.singleByte[0] = input;
        engineUpdate(this.singleByte, 0, 1);
    }

    protected void engineUpdate(byte[] input, int offset, int len) {
        NativeCrypto.HMAC_Update(this.ctx, input, offset, len);
    }

    protected void engineUpdate(ByteBuffer input) {
        if (!input.hasRemaining()) {
            return;
        }
        if (input.isDirect()) {
            long baseAddress = NativeCrypto.getDirectBufferAddress(input);
            if (baseAddress == 0) {
                super.engineUpdate(input);
                return;
            }
            int position = input.position();
            if (position < 0) {
                throw new RuntimeException("Negative position");
            }
            long ptr = baseAddress + ((long) position);
            int len = input.remaining();
            if (len < 0) {
                throw new RuntimeException("Negative remaining amount");
            }
            NativeCrypto.HMAC_UpdateDirect(this.ctx, ptr, len);
            input.position(position + len);
            return;
        }
        super.engineUpdate(input);
    }

    protected byte[] engineDoFinal() {
        byte[] output = NativeCrypto.HMAC_Final(this.ctx);
        resetContext();
        return output;
    }

    protected void engineReset() {
        resetContext();
    }
}
