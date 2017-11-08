package com.trustlook.sdk.cloudscan;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import com.trustlook.sdk.Constants;
import com.trustlook.sdk.data.PkgInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Locale;

public class PkgUtils {
    public static PkgInfo populatePkgInfo(String str, String str2) {
        PkgInfo pkgInfo = new PkgInfo(str);
        if (!(str == null || str2 == null)) {
            File file = new File(str2);
            pkgInfo.setMd5(a(file, "MD5"));
            pkgInfo.setPkgSize(file.length());
        }
        return pkgInfo;
    }

    public static PackageInfo getPackageInfo(Context context, String str) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(str, 4224);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo;
    }

    private static String a(File file, String str) {
        try {
            MessageDigest instance = MessageDigest.getInstance(str);
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] bArr = new byte[8192];
                while (true) {
                    int read = fileInputStream.read(bArr);
                    if (read <= 0) {
                        break;
                    }
                    try {
                        instance.update(bArr, 0, read);
                    } catch (Throwable e) {
                        throw new RuntimeException("Unable to process file for MD5", e);
                    } catch (Throwable th) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e2) {
                            Log.e(Constants.TAG, "Exception on closing MD5 input stream " + e2);
                        }
                    }
                }
                String bigInteger = new BigInteger(1, instance.digest()).toString(16);
                bigInteger = String.format("%32s", new Object[]{bigInteger}).replace(' ', '0').toUpperCase(Locale.US);
                try {
                    fileInputStream.close();
                } catch (IOException e22) {
                    Log.e(Constants.TAG, "Exception on closing MD5 input stream " + e22);
                }
                return bigInteger;
            } catch (Throwable e3) {
                Log.e(Constants.TAG, "Exception while getting FileInputStream", e3);
                return null;
            }
        } catch (Throwable e32) {
            Log.e(Constants.TAG, "Exception while getting Digest", e32);
            return null;
        }
    }
}
