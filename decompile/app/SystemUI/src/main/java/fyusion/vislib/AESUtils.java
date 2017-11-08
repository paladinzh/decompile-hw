package fyusion.vislib;

/* compiled from: Unknown */
public class AESUtils {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public AESUtils() {
        this(AesUtilsWrapperJNI.new_AESUtils(), true);
    }

    protected AESUtils(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(AESUtils aESUtils) {
        return aESUtils != null ? aESUtils.swigCPtr : 0;
    }

    public String decrypt(String str, String str2, String str3) {
        return AesUtilsWrapperJNI.AESUtils_decrypt(this.swigCPtr, this, str, str2, str3);
    }

    public String decryptFileToString(String str) {
        return AesUtilsWrapperJNI.AESUtils_decryptFileToString(this.swigCPtr, this, str);
    }

    public String decryptLogFileToString(String str) {
        return AesUtilsWrapperJNI.AESUtils_decryptLogFileToString(this.swigCPtr, this, str);
    }

    public String decryptMagicFileToString(String str) {
        return AesUtilsWrapperJNI.AESUtils_decryptMagicFileToString(this.swigCPtr, this, str);
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                AesUtilsWrapperJNI.delete_AESUtils(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    public boolean encryptStringToFile(String str, String str2) {
        return AesUtilsWrapperJNI.AESUtils_encryptStringToFile(this.swigCPtr, this, str, str2);
    }

    protected void finalize() {
        delete();
    }
}
