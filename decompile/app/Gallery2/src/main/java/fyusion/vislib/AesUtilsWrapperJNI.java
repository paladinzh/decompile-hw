package fyusion.vislib;

/* compiled from: Unknown */
public class AesUtilsWrapperJNI {
    public static final native String AESUtils_decrypt(long j, AESUtils aESUtils, String str, String str2, String str3);

    public static final native String AESUtils_decryptFileToString(long j, AESUtils aESUtils, String str);

    public static final native String AESUtils_decryptLogFileToString(long j, AESUtils aESUtils, String str);

    public static final native String AESUtils_decryptMagicFileToString(long j, AESUtils aESUtils, String str);

    public static final native boolean AESUtils_encryptStringToFile(long j, AESUtils aESUtils, String str, String str2);

    public static final native void delete_AESUtils(long j);

    public static final native long new_AESUtils();
}
