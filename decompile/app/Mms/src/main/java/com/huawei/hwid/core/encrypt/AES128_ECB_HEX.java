package com.huawei.hwid.core.encrypt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public abstract class AES128_ECB_HEX {
    public static byte[] decode(String str, byte[] bArr, int i) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return AES128_ECB.decode(HEX.decode(str), 0, bArr, i);
    }
}
