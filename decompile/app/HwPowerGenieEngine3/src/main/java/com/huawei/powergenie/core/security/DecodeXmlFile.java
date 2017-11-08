package com.huawei.powergenie.core.security;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.RSAPrivateKeySpec;
import javax.crypto.Cipher;

public class DecodeXmlFile {
    private static BigInteger mBig;
    private static BigInteger mEx = new BigInteger("1476");
    private static PrivateKey pKey;
    private static BigInteger result;
    private static String ts = "2540BE4";

    private static PrivateKey getPKey(String modulus, String privateExponent) throws Exception {
        BigInteger e = new BigInteger(privateExponent, 10);
        result = result.multiply(mEx);
        return KeyFactory.getInstance("RSA").generatePrivate(new RSAPrivateKeySpec(new BigInteger(modulus + "1957b839c6538ac7", 16), e.multiply(mBig).add(result).add(new BigInteger("676641"))));
    }

    private static InputStream decodeFile(InputStream fileIn) throws Exception {
        StringBuffer decriptBuf = new StringBuffer();
        DataInputStream dis = new DataInputStream(fileIn);
        Cipher decriptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        decriptCipher.init(2, pKey);
        int readlen = 0;
        byte[] readbyte = new byte[128];
        if (dis != null) {
            while (dis.available() > 0) {
                int readint = dis.readInt() & -1;
                for (int i = 0; i < 4; i++) {
                    readbyte[readlen + i] = (byte) (readint >>> (24 - (i * 8)));
                }
                readlen += 4;
                if (readlen >= 128) {
                    decriptBuf.append(new String(decriptCipher.doFinal(readbyte)));
                    readlen = 0;
                }
            }
        }
        return new ByteArrayInputStream(decriptBuf.toString().getBytes());
    }

    public static InputStream getDecodeInputStream(InputStream inFileStream) throws Exception {
        ts = "2540BE4";
        mEx = new BigInteger("1476");
        String smodulus = "be6561489fd328d7959dbf17f051b784ad9e5016d8ca27d95498823ab3e7183d88e860eda0e4a240b60ce92f435137e296567cc22590e9b2f1279f692a6eea0dcabd6c7186b5dbb500bfe43637182edcce90edd89480d85f9ef93116c463e49f7f04599b2bf2e09f2dbfff2d45041e1239706ab630238469";
        String sprivateExponet = "12565455915482169874509283215015776896419126892792937649022121577988577182628999618677208361806797232402506950578354817213294342344875063269873436599783951707404062417274486682370167652039562398018218141670050535396629116656991538584970584883579410050231567459610878643439799235116953116589567931191";
        ts += "00";
        mBig = new BigInteger(ts, 16);
        try {
            result = mEx.multiply(mEx);
            pKey = getPKey(smodulus, sprivateExponet);
            return decodeFile(inFileStream);
        } catch (Exception e) {
            throw e;
        }
    }
}
