package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import java.io.UnsupportedEncodingException;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.b;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class ls {
    private static String TAG = "CryptorUtils";

    public static String b(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        try {
            byte[] encrypt = TccCryptor.encrypt(context, str.getBytes("gbk"), null);
            if (encrypt != null) {
                return b.encodeToString(encrypt, 0);
            }
        } catch (UnsupportedEncodingException e) {
            d.c(TAG, "getEncodeString, UnsupportedEncodingException: " + e);
        } catch (Exception e2) {
            d.c(TAG, "getEncodeString, Exception: " + e2);
        }
        return null;
    }

    public static String c(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        try {
            byte[] decrypt = TccCryptor.decrypt(context, b.decode(str, 0), null);
            if (decrypt != null) {
                return new String(decrypt, "gbk");
            }
        } catch (UnsupportedEncodingException e) {
            d.c(TAG, "getDecodeString, UnsupportedEncodingException: " + e);
        } catch (Exception e2) {
            d.c(TAG, "getDecodeString, Exception: " + e2);
        }
        return null;
    }
}
