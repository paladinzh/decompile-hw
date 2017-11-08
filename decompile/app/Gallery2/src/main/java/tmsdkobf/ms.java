package tmsdkobf;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.gadget.XmlUtils;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tmsdk.common.ErrorCode;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.d;
import tmsdk.common.utils.h;
import tmsdk.common.utils.h.a;

/* compiled from: Unknown */
public final class ms {
    private static final String[][] Bk;

    static {
        String[][] strArr = new String[69][];
        strArr[0] = new String[]{"3gp", "video/3gpp"};
        strArr[1] = new String[]{"apk", "application/vnd.android.package-archive"};
        strArr[2] = new String[]{"asf", "video/x-ms-asf"};
        strArr[3] = new String[]{"avi", "video/x-msvideo"};
        strArr[4] = new String[]{"bin", "application/octet-stream"};
        strArr[5] = new String[]{"bmp", "image/bmp"};
        strArr[6] = new String[]{"c", "text/plain"};
        strArr[7] = new String[]{"class", "application/octet-stream"};
        strArr[8] = new String[]{"conf", "text/plain"};
        strArr[9] = new String[]{"cpp", "text/plain"};
        strArr[10] = new String[]{"doc", "application/msword"};
        strArr[11] = new String[]{"docx", "application/msword"};
        strArr[12] = new String[]{"exe", "application/octet-stream"};
        strArr[13] = new String[]{"gif", "image/gif"};
        strArr[14] = new String[]{"gtar", "application/x-gtar"};
        strArr[15] = new String[]{"gz", "application/x-gzip"};
        strArr[16] = new String[]{"h", "text/plain"};
        strArr[17] = new String[]{"htm", "text/html"};
        strArr[18] = new String[]{"html", "text/html"};
        strArr[19] = new String[]{"jar", "application/java-archive"};
        strArr[20] = new String[]{"java", "text/plain"};
        strArr[21] = new String[]{"jpeg", "image/jpeg"};
        strArr[22] = new String[]{"jpg", "image/jpeg"};
        strArr[23] = new String[]{"js", "application/x-javascript"};
        strArr[24] = new String[]{"log", "text/plain"};
        strArr[25] = new String[]{"m3u", "audio/x-mpegurl"};
        strArr[26] = new String[]{"m4a", "audio/mp4a-latm"};
        strArr[27] = new String[]{"m4b", "audio/mp4a-latm"};
        strArr[28] = new String[]{"m4p", "audio/mp4a-latm"};
        strArr[29] = new String[]{"m4u", "video/vnd.mpegurl"};
        strArr[30] = new String[]{"m4v", "video/x-m4v"};
        strArr[31] = new String[]{"mov", "video/quicktime"};
        strArr[32] = new String[]{"mp2", "audio/x-mpeg"};
        strArr[33] = new String[]{"mp3", "audio/x-mpeg"};
        strArr[34] = new String[]{"mp4", "video/mp4"};
        strArr[35] = new String[]{"mpc", "application/vnd.mpohn.certificate"};
        strArr[36] = new String[]{"mpe", "video/mpeg"};
        strArr[37] = new String[]{"mpeg", "video/mpeg"};
        strArr[38] = new String[]{"mpg", "video/mpeg"};
        strArr[39] = new String[]{"mpg4", "video/mp4"};
        strArr[40] = new String[]{"mpga", "audio/mpeg"};
        strArr[41] = new String[]{"msg", "application/vnd.ms-outlook"};
        strArr[42] = new String[]{"ogg", "audio/ogg"};
        strArr[43] = new String[]{"pdf", "application/pdf"};
        strArr[44] = new String[]{"png", "image/png"};
        strArr[45] = new String[]{"pps", "application/vnd.ms-powerpoint"};
        strArr[46] = new String[]{"ppsx", "application/vnd.ms-powerpoint"};
        strArr[47] = new String[]{"ppt", "application/vnd.ms-powerpoint"};
        strArr[48] = new String[]{"pptx", "application/vnd.ms-powerpoint"};
        strArr[49] = new String[]{"xls", "application/vnd.ms-excel"};
        strArr[50] = new String[]{"xlsx", "application/vnd.ms-excel"};
        strArr[51] = new String[]{"prop", "text/plain"};
        strArr[52] = new String[]{"rar", "application/x-rar-compressed"};
        strArr[53] = new String[]{"rc", "text/plain"};
        strArr[54] = new String[]{"rmvb", "audio/x-pn-realaudio"};
        strArr[55] = new String[]{"rtf", "application/rtf"};
        strArr[56] = new String[]{"sh", "text/plain"};
        strArr[57] = new String[]{"tar", "application/x-tar"};
        strArr[58] = new String[]{"tgz", "application/x-compressed"};
        strArr[59] = new String[]{"txt", "text/plain"};
        strArr[60] = new String[]{"wav", "audio/x-wav"};
        strArr[61] = new String[]{"wma", "audio/x-ms-wma"};
        strArr[62] = new String[]{"wmv", "audio/x-ms-wmv"};
        strArr[63] = new String[]{"wps", "application/vnd.ms-works"};
        strArr[64] = new String[]{"xml", "text/plain"};
        strArr[65] = new String[]{"z", "application/x-compress"};
        strArr[66] = new String[]{"zip", "application/zip"};
        strArr[67] = new String[]{"epub", "application/epub+zip"};
        strArr[68] = new String[]{"", "*/*"};
        Bk = strArr;
    }

    public static int a(Context context, Object obj, String str, String str2) {
        int i = 0;
        FileOutputStream fileOutputStream = null;
        int i2 = -2;
        if (obj == null || str == null || str2 == null) {
            return -57;
        }
        try {
            fileOutputStream = context.openFileOutput(str2, 0);
            fi fiVar = new fi();
            fiVar.Z(XmlUtils.INPUT_ENCODING);
            fiVar.put(str, obj);
            byte[] encrypt = TccCryptor.encrypt(fiVar.m(), null);
            if (encrypt == null) {
                i = -2;
            } else {
                fileOutputStream.write(encrypt);
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e2) {
            i2 = -1;
            e2.printStackTrace();
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        } catch (IOException e32) {
            i2 = ErrorCode.ERR_FILE_OP;
            e32.printStackTrace();
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            }
        } catch (Throwable th) {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
        }
        return i;
        i = i2;
        return i;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized String a(Context context, String str, String str2) {
        String str3;
        InputStream open;
        Throwable th;
        boolean z = true;
        FileOutputStream fileOutputStream = null;
        synchronized (ms.class) {
            File file;
            File file2;
            boolean exists;
            boolean isUpdatableAssetFile;
            String str4;
            File file3;
            File file4;
            FileOutputStream fileOutputStream2;
            InputStream inputStream;
            byte[] bArr;
            int read;
            if (str2 != null) {
                if (!str2.equals("")) {
                    file = new File(str2);
                    if (!file.exists() || !file.isDirectory()) {
                        file.mkdirs();
                    }
                    str3 = str2 + File.separator + str;
                    file2 = new File(str3);
                    exists = file2.exists();
                    isUpdatableAssetFile = UpdateConfig.isUpdatableAssetFile(str);
                    if (!str.equals(UpdateConfig.VIRUS_BASE_NAME)) {
                        if (!str.equals(UpdateConfig.VIRUS_BASE_EN_NAME)) {
                            z = false;
                            if (!exists && isUpdatableAssetFile) {
                                str4 = (String) UpdateConfig.sDeprecatedNameMap.get(str);
                                if (str4 != null) {
                                    str4 = str3.substring(0, str3.lastIndexOf(File.separator) + 1) + str4;
                                    file3 = new File(str4);
                                    file4 = new File(str4 + UpdateConfig.PATCH_SUFIX);
                                    if (file3.exists()) {
                                        file3.delete();
                                    }
                                    if (file4.exists()) {
                                        file4.delete();
                                    }
                                }
                            }
                            if (exists) {
                                if (!str.equals("MToken.zip")) {
                                    if (str.equals(UpdateConfig.VIRUS_BASE_NAME) || str.equals(UpdateConfig.VIRUS_BASE_EN_NAME)) {
                                    }
                                    if (!str.equals(UpdateConfig.LOCATION_NAME) || !o(context)) {
                                        if (!isUpdatableAssetFile || str.equals(UpdateConfig.VIRUS_BASE_NAME) || str.equals(UpdateConfig.VIRUS_BASE_EN_NAME) || str.equals(UpdateConfig.LOCATION_NAME) || !d(context, str)) {
                                            if (isUpdatableAssetFile) {
                                                fileOutputStream2 = null;
                                                if (inputStream != null) {
                                                    try {
                                                        inputStream.close();
                                                    } catch (IOException e) {
                                                    }
                                                }
                                                if (fileOutputStream2 != null) {
                                                    try {
                                                        fileOutputStream2.close();
                                                    } catch (IOException e2) {
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            a(file2);
                            open = context.getResources().getAssets().open(str, 1);
                            try {
                                fileOutputStream2 = new FileOutputStream(file2);
                                try {
                                    bArr = new byte[FragmentTransaction.TRANSIT_EXIT_MASK];
                                    while (true) {
                                        read = open.read(bArr);
                                        if (read > 0) {
                                            break;
                                        }
                                        fileOutputStream2.write(bArr, 0, read);
                                    }
                                    fileOutputStream2.getChannel().force(true);
                                    fileOutputStream2.flush();
                                    inputStream = open;
                                    if (inputStream != null) {
                                        inputStream.close();
                                    }
                                    if (fileOutputStream2 != null) {
                                        fileOutputStream2.close();
                                    }
                                } catch (IOException e3) {
                                    fileOutputStream = fileOutputStream2;
                                } catch (Throwable th2) {
                                    Throwable th3 = th2;
                                    fileOutputStream = fileOutputStream2;
                                    th = th3;
                                }
                            } catch (IOException e4) {
                                try {
                                    d.c("getCommonFilePath", "getCommonFilePath error");
                                    str4 = "";
                                    if (open != null) {
                                        try {
                                            open.close();
                                        } catch (IOException e5) {
                                        }
                                    }
                                    if (fileOutputStream != null) {
                                        try {
                                            fileOutputStream.close();
                                        } catch (IOException e6) {
                                        }
                                    }
                                    return str4;
                                } catch (Throwable th4) {
                                    th = th4;
                                    if (open != null) {
                                        try {
                                            open.close();
                                        } catch (IOException e7) {
                                        }
                                    }
                                    if (fileOutputStream != null) {
                                        try {
                                            fileOutputStream.close();
                                        } catch (IOException e8) {
                                        }
                                    }
                                    throw th;
                                }
                            }
                        }
                    }
                }
            }
            str2 = context.getFilesDir().toString();
            file = new File(str2);
            if (!file.exists()) {
                str3 = str2 + File.separator + str;
                file2 = new File(str3);
                exists = file2.exists();
                isUpdatableAssetFile = UpdateConfig.isUpdatableAssetFile(str);
                if (str.equals(UpdateConfig.VIRUS_BASE_NAME)) {
                    if (str.equals(UpdateConfig.VIRUS_BASE_EN_NAME)) {
                        z = false;
                        str4 = (String) UpdateConfig.sDeprecatedNameMap.get(str);
                        if (str4 != null) {
                            str4 = str3.substring(0, str3.lastIndexOf(File.separator) + 1) + str4;
                            file3 = new File(str4);
                            file4 = new File(str4 + UpdateConfig.PATCH_SUFIX);
                            if (file3.exists()) {
                                file3.delete();
                            }
                            if (file4.exists()) {
                                file4.delete();
                            }
                        }
                        if (exists) {
                            if (str.equals("MToken.zip")) {
                                if (str.equals(UpdateConfig.VIRUS_BASE_NAME)) {
                                    if (!str.equals(UpdateConfig.LOCATION_NAME)) {
                                    }
                                    if (!isUpdatableAssetFile) {
                                    }
                                    if (isUpdatableAssetFile) {
                                        fileOutputStream2 = null;
                                        if (inputStream != null) {
                                            inputStream.close();
                                        }
                                        if (fileOutputStream2 != null) {
                                            fileOutputStream2.close();
                                        }
                                    }
                                }
                            }
                        }
                        a(file2);
                        open = context.getResources().getAssets().open(str, 1);
                        fileOutputStream2 = new FileOutputStream(file2);
                        bArr = new byte[FragmentTransaction.TRANSIT_EXIT_MASK];
                        while (true) {
                            read = open.read(bArr);
                            if (read > 0) {
                                break;
                            }
                            fileOutputStream2.write(bArr, 0, read);
                        }
                        fileOutputStream2.getChannel().force(true);
                        fileOutputStream2.flush();
                        inputStream = open;
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (fileOutputStream2 != null) {
                            fileOutputStream2.close();
                        }
                    }
                }
            }
            file.mkdirs();
            str3 = str2 + File.separator + str;
            try {
                file2 = new File(str3);
                exists = file2.exists();
                isUpdatableAssetFile = UpdateConfig.isUpdatableAssetFile(str);
                if (str.equals(UpdateConfig.VIRUS_BASE_NAME)) {
                    if (str.equals(UpdateConfig.VIRUS_BASE_EN_NAME)) {
                        z = false;
                        str4 = (String) UpdateConfig.sDeprecatedNameMap.get(str);
                        if (str4 != null) {
                            str4 = str3.substring(0, str3.lastIndexOf(File.separator) + 1) + str4;
                            file3 = new File(str4);
                            file4 = new File(str4 + UpdateConfig.PATCH_SUFIX);
                            if (file3.exists()) {
                                file3.delete();
                            }
                            if (file4.exists()) {
                                file4.delete();
                            }
                        }
                        if (exists) {
                            if (str.equals("MToken.zip")) {
                                if (str.equals(UpdateConfig.VIRUS_BASE_NAME)) {
                                    if (!str.equals(UpdateConfig.LOCATION_NAME)) {
                                    }
                                    if (!isUpdatableAssetFile) {
                                    }
                                    if (isUpdatableAssetFile) {
                                        fileOutputStream2 = null;
                                        if (inputStream != null) {
                                            inputStream.close();
                                        }
                                        if (fileOutputStream2 != null) {
                                            fileOutputStream2.close();
                                        }
                                    }
                                }
                            }
                        }
                        a(file2);
                        open = context.getResources().getAssets().open(str, 1);
                        fileOutputStream2 = new FileOutputStream(file2);
                        bArr = new byte[FragmentTransaction.TRANSIT_EXIT_MASK];
                        while (true) {
                            read = open.read(bArr);
                            if (read > 0) {
                                break;
                            }
                            fileOutputStream2.write(bArr, 0, read);
                        }
                        fileOutputStream2.getChannel().force(true);
                        fileOutputStream2.flush();
                        inputStream = open;
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (fileOutputStream2 != null) {
                            fileOutputStream2.close();
                        }
                    }
                }
            } catch (IOException e9) {
                open = null;
                d.c("getCommonFilePath", "getCommonFilePath error");
                str4 = "";
                if (open != null) {
                    open.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return str4;
            } catch (Throwable th5) {
                th = th5;
                open = null;
                if (open != null) {
                    open.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        }
        return str3;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static <T> ArrayList<T> a(Context context, String str, String str2, T t) {
        FileInputStream openFileInput;
        ByteArrayOutputStream byteArrayOutputStream;
        ByteArrayOutputStream byteArrayOutputStream2;
        FileInputStream fileInputStream;
        Object obj;
        ByteArrayOutputStream byteArrayOutputStream3;
        Throwable th;
        ArrayList<T> arrayList = null;
        if (str == null || str2 == null) {
            return null;
        }
        try {
            openFileInput = context.openFileInput(str2);
            try {
                byteArrayOutputStream = new ByteArrayOutputStream();
                ArrayList<T> arrayList2;
                try {
                    byte[] bArr = new byte[1024];
                    while (true) {
                        int read = openFileInput.read(bArr);
                        if (read == -1) {
                            break;
                        }
                        byteArrayOutputStream.write(bArr, 0, read);
                    }
                    bArr = byteArrayOutputStream.toByteArray();
                    fi fiVar = new fi();
                    fiVar.Z(XmlUtils.INPUT_ENCODING);
                    fiVar.b(TccCryptor.decrypt(bArr, null));
                    arrayList2 = new ArrayList();
                    arrayList2.add(t);
                    arrayList = (ArrayList) fiVar.a(str, (Object) arrayList2);
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                } catch (FileNotFoundException e3) {
                    byteArrayOutputStream2 = byteArrayOutputStream;
                    fileInputStream = openFileInput;
                    Object obj2 = arrayList2;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (byteArrayOutputStream2 != null) {
                        try {
                            byteArrayOutputStream2.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    obj = byteArrayOutputStream3;
                    return arrayList;
                } catch (IOException e5) {
                    arrayList = arrayList2;
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (IOException e6) {
                            e6.printStackTrace();
                        }
                    }
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    return arrayList;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (FileNotFoundException e7) {
                fileInputStream = openFileInput;
                byteArrayOutputStream3 = null;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (byteArrayOutputStream2 != null) {
                    byteArrayOutputStream2.close();
                }
                obj = byteArrayOutputStream3;
                return arrayList;
            } catch (IOException e8) {
                byteArrayOutputStream = null;
                if (openFileInput != null) {
                    openFileInput.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                return arrayList;
            } catch (Throwable th3) {
                Throwable th4 = th3;
                byteArrayOutputStream = null;
                th = th4;
                if (openFileInput != null) {
                    try {
                        openFileInput.close();
                    } catch (IOException e62) {
                        e62.printStackTrace();
                    }
                }
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e2222) {
                        e2222.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e9) {
            fileInputStream = null;
            byteArrayOutputStream3 = null;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (byteArrayOutputStream2 != null) {
                byteArrayOutputStream2.close();
            }
            obj = byteArrayOutputStream3;
            return arrayList;
        } catch (IOException e10) {
            byteArrayOutputStream = null;
            openFileInput = null;
            if (openFileInput != null) {
                openFileInput.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            return arrayList;
        } catch (Throwable th32) {
            openFileInput = null;
            th = th32;
            byteArrayOutputStream = null;
            if (openFileInput != null) {
                openFileInput.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            throw th;
        }
        return arrayList;
    }

    public static void a(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    private static boolean a(Context context, String str, boolean z) {
        InputStream open;
        int i;
        int i2;
        Exception e;
        InputStream fileInputStream;
        int i3;
        Throwable th;
        boolean z2 = false;
        if (z) {
            return true;
        }
        byte[] bArr;
        int i4;
        try {
            open = context.getAssets().open(str);
            try {
                byte[] bArr2 = new byte[28];
                open.read(bArr2);
                i = (((bArr2[4] & 255) | ((bArr2[5] & 255) << 8)) | ((bArr2[6] & 255) << 16)) | ((bArr2[7] & 255) << 24);
                try {
                    i2 = ((bArr2[27] & 255) << 24) | (((bArr2[24] & 255) | ((bArr2[25] & 255) << 8)) | ((bArr2[26] & 255) << 16));
                    if (open != null) {
                        try {
                            open.close();
                        } catch (IOException e2) {
                        }
                    }
                } catch (Exception e3) {
                    e = e3;
                    try {
                        e.printStackTrace();
                        if (open != null) {
                            try {
                                open.close();
                            } catch (IOException e4) {
                            }
                        }
                        i2 = 0;
                        fileInputStream = new FileInputStream(context.getFilesDir().toString() + File.separator + str);
                        try {
                            bArr = new byte[28];
                            fileInputStream.read(bArr);
                            i3 = (((bArr[4] & 255) | ((bArr[5] & 255) << 8)) | ((bArr[6] & 255) << 16)) | ((bArr[7] & 255) << 24);
                            try {
                                i4 = ((bArr[27] & 255) << 24) | (((bArr[24] & 255) | ((bArr[25] & 255) << 8)) | ((bArr[26] & 255) << 16));
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (IOException e5) {
                                    }
                                }
                            } catch (Exception e6) {
                                e = e6;
                                try {
                                    Exception e7;
                                    e7.printStackTrace();
                                    if (fileInputStream != null) {
                                        try {
                                            fileInputStream.close();
                                        } catch (IOException e8) {
                                        }
                                    }
                                    i4 = 0;
                                    if (i != i3) {
                                        return z2;
                                    }
                                    z2 = true;
                                    return z2;
                                } catch (Throwable th2) {
                                    th = th2;
                                    if (fileInputStream != null) {
                                        try {
                                            fileInputStream.close();
                                        } catch (IOException e9) {
                                        }
                                    }
                                    throw th;
                                }
                            }
                        } catch (Exception e10) {
                            e7 = e10;
                            i3 = 0;
                            e7.printStackTrace();
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            i4 = 0;
                            if (i != i3) {
                                return z2;
                            }
                            z2 = true;
                            return z2;
                        }
                        if (i != i3) {
                            return z2;
                        }
                        z2 = true;
                        return z2;
                    } catch (Throwable th3) {
                        th = th3;
                        if (open != null) {
                            try {
                                open.close();
                            } catch (IOException e11) {
                            }
                        }
                        throw th;
                    }
                }
            } catch (Exception e12) {
                e = e12;
                i = 0;
                e.printStackTrace();
                if (open != null) {
                    open.close();
                }
                i2 = 0;
                fileInputStream = new FileInputStream(context.getFilesDir().toString() + File.separator + str);
                bArr = new byte[28];
                fileInputStream.read(bArr);
                i3 = (((bArr[4] & 255) | ((bArr[5] & 255) << 8)) | ((bArr[6] & 255) << 16)) | ((bArr[7] & 255) << 24);
                i4 = ((bArr[27] & 255) << 24) | (((bArr[24] & 255) | ((bArr[25] & 255) << 8)) | ((bArr[26] & 255) << 16));
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (i != i3) {
                    return z2;
                }
                z2 = true;
                return z2;
            }
        } catch (Exception e13) {
            e = e13;
            open = null;
            i = 0;
            e.printStackTrace();
            if (open != null) {
                open.close();
            }
            i2 = 0;
            fileInputStream = new FileInputStream(context.getFilesDir().toString() + File.separator + str);
            bArr = new byte[28];
            fileInputStream.read(bArr);
            i3 = (((bArr[4] & 255) | ((bArr[5] & 255) << 8)) | ((bArr[6] & 255) << 16)) | ((bArr[7] & 255) << 24);
            i4 = ((bArr[27] & 255) << 24) | (((bArr[24] & 255) | ((bArr[25] & 255) << 8)) | ((bArr[26] & 255) << 16));
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (i != i3) {
                return z2;
            }
            z2 = true;
            return z2;
        } catch (Throwable th4) {
            th = th4;
            open = null;
            if (open != null) {
                open.close();
            }
            throw th;
        }
        try {
            fileInputStream = new FileInputStream(context.getFilesDir().toString() + File.separator + str);
            bArr = new byte[28];
            fileInputStream.read(bArr);
            i3 = (((bArr[4] & 255) | ((bArr[5] & 255) << 8)) | ((bArr[6] & 255) << 16)) | ((bArr[7] & 255) << 24);
            i4 = ((bArr[27] & 255) << 24) | (((bArr[24] & 255) | ((bArr[25] & 255) << 8)) | ((bArr[26] & 255) << 16));
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (Exception e14) {
            fileInputStream = open;
            e7 = e14;
            i3 = 0;
            e7.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            i4 = 0;
            if (i != i3) {
                return z2;
            }
            z2 = true;
            return z2;
        } catch (Throwable th5) {
            th = th5;
            fileInputStream = open;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th;
        }
        if (i != i3 || r0 > r3) {
            z2 = true;
        }
        return z2;
    }

    public static String[] b(File file) {
        BufferedInputStream bufferedInputStream;
        ByteArrayOutputStream byteArrayOutputStream;
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            try {
                byteArrayOutputStream = new ByteArrayOutputStream();
            } catch (FileNotFoundException e3) {
                e = e3;
                byteArrayOutputStream = null;
                try {
                    e.printStackTrace();
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (IOException e5) {
                            e5.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (IOException e6) {
                e222 = e6;
                byteArrayOutputStream = null;
                e222.printStackTrace();
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e2222) {
                        e2222.printStackTrace();
                    }
                }
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e22222) {
                        e22222.printStackTrace();
                    }
                }
                return null;
            } catch (Throwable th3) {
                th = th3;
                byteArrayOutputStream = null;
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                throw th;
            }
            try {
                byte[] bArr = new byte[1024];
                while (true) {
                    int read = bufferedInputStream.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    byteArrayOutputStream.write(bArr, 0, read);
                }
                String[] split = new String(byteArrayOutputStream.toByteArray()).split("\\n");
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e42) {
                        e42.printStackTrace();
                    }
                }
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e52) {
                        e52.printStackTrace();
                    }
                }
                return split;
            } catch (FileNotFoundException e7) {
                e = e7;
                e.printStackTrace();
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                return null;
            } catch (IOException e8) {
                e22222 = e8;
                e22222.printStackTrace();
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                return null;
            }
        } catch (FileNotFoundException e9) {
            e = e9;
            byteArrayOutputStream = null;
            bufferedInputStream = null;
            e.printStackTrace();
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            return null;
        } catch (IOException e10) {
            e22222 = e10;
            byteArrayOutputStream = null;
            bufferedInputStream = null;
            e22222.printStackTrace();
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            return null;
        } catch (Throwable th4) {
            th = th4;
            byteArrayOutputStream = null;
            bufferedInputStream = null;
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            throw th;
        }
    }

    public static boolean cy(String str) {
        return new File(str).exists();
    }

    public static String cz(String str) {
        BufferedInputStream bufferedInputStream;
        ByteArrayOutputStream byteArrayOutputStream;
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        BufferedInputStream bufferedInputStream2 = null;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(str));
            try {
                byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    byte[] bArr = new byte[1024];
                    while (true) {
                        int read = bufferedInputStream.read(bArr);
                        if (read == -1) {
                            break;
                        }
                        byteArrayOutputStream.write(bArr, 0, read);
                    }
                    String str2 = new String(byteArrayOutputStream.toByteArray());
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    }
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e32) {
                            e32.printStackTrace();
                        }
                    }
                    return str2;
                } catch (FileNotFoundException e4) {
                    e = e4;
                    bufferedInputStream2 = bufferedInputStream;
                    try {
                        e.printStackTrace();
                        if (byteArrayOutputStream != null) {
                            try {
                                byteArrayOutputStream.close();
                            } catch (IOException e22) {
                                e22.printStackTrace();
                            }
                        }
                        if (bufferedInputStream2 != null) {
                            try {
                                bufferedInputStream2.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
                            }
                        }
                        return "";
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedInputStream = bufferedInputStream2;
                        if (byteArrayOutputStream != null) {
                            try {
                                byteArrayOutputStream.close();
                            } catch (IOException e322) {
                                e322.printStackTrace();
                            }
                        }
                        if (bufferedInputStream != null) {
                            try {
                                bufferedInputStream.close();
                            } catch (IOException e3222) {
                                e3222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (IOException e5) {
                    e222 = e5;
                    try {
                        e222.printStackTrace();
                        if (byteArrayOutputStream != null) {
                            try {
                                byteArrayOutputStream.close();
                            } catch (IOException e2222) {
                                e2222.printStackTrace();
                            }
                        }
                        if (bufferedInputStream != null) {
                            try {
                                bufferedInputStream.close();
                            } catch (IOException e22222) {
                                e22222.printStackTrace();
                            }
                        }
                        return "";
                    } catch (Throwable th3) {
                        th = th3;
                        if (byteArrayOutputStream != null) {
                            byteArrayOutputStream.close();
                        }
                        if (bufferedInputStream != null) {
                            bufferedInputStream.close();
                        }
                        throw th;
                    }
                }
            } catch (FileNotFoundException e6) {
                e = e6;
                byteArrayOutputStream = null;
                bufferedInputStream2 = bufferedInputStream;
                e.printStackTrace();
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (bufferedInputStream2 != null) {
                    bufferedInputStream2.close();
                }
                return "";
            } catch (IOException e7) {
                e22222 = e7;
                byteArrayOutputStream = null;
                e22222.printStackTrace();
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                return "";
            } catch (Throwable th4) {
                th = th4;
                byteArrayOutputStream = null;
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e8) {
            e = e8;
            byteArrayOutputStream = null;
            e.printStackTrace();
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (bufferedInputStream2 != null) {
                bufferedInputStream2.close();
            }
            return "";
        } catch (IOException e9) {
            e22222 = e9;
            byteArrayOutputStream = null;
            bufferedInputStream = null;
            e22222.printStackTrace();
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            return "";
        } catch (Throwable th5) {
            th = th5;
            byteArrayOutputStream = null;
            bufferedInputStream = null;
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            throw th;
        }
    }

    private static boolean d(Context context, String str) {
        InputStream open;
        int i;
        int i2;
        Exception e;
        boolean z;
        Throwable th;
        FileInputStream fileInputStream = null;
        File file = new File(TMSDKContext.getApplicaionContext().getFilesDir() + File.separator + str);
        if (!file.exists()) {
            return true;
        }
        try {
            open = context.getAssets().open(str, 1);
            try {
                i = mr.c(open).Bi;
                if (open != null) {
                    try {
                        open.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                i2 = i;
            } catch (Exception e3) {
                e = e3;
                try {
                    e.printStackTrace();
                    if (open != null) {
                        try {
                            open.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                    }
                    z = false;
                    if (i2 != 0) {
                        return false;
                    }
                    try {
                        open = new FileInputStream(file);
                        try {
                            i = mr.c(open).Bi;
                            if (open != null) {
                                try {
                                    open.close();
                                } catch (IOException e22) {
                                    e22.printStackTrace();
                                }
                            }
                        } catch (Exception e5) {
                            e = e5;
                            InputStream inputStream = open;
                            try {
                                e.printStackTrace();
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (IOException e42) {
                                        e42.printStackTrace();
                                    }
                                }
                                i = 0;
                                return i2 > i;
                            } catch (Throwable th2) {
                                th = th2;
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (IOException e222) {
                                        e222.printStackTrace();
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            inputStream = open;
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            throw th;
                        }
                    } catch (Exception e6) {
                        e = e6;
                        e.printStackTrace();
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        i = 0;
                        if (i2 > i) {
                        }
                        return i2 > i;
                    }
                    if (i2 > i) {
                    }
                    return i2 > i;
                } catch (Throwable th4) {
                    th = th4;
                    if (open != null) {
                        try {
                            open.close();
                        } catch (IOException e2222) {
                            e2222.printStackTrace();
                        }
                    }
                    throw th;
                }
            }
        } catch (Exception e7) {
            e = e7;
            open = null;
            e.printStackTrace();
            if (open != null) {
                open.close();
            }
            z = false;
            if (i2 != 0) {
                return false;
            }
            open = new FileInputStream(file);
            i = mr.c(open).Bi;
            if (open != null) {
                open.close();
            }
            if (i2 > i) {
            }
            return i2 > i;
        } catch (Throwable th5) {
            th = th5;
            open = null;
            if (open != null) {
                open.close();
            }
            throw th;
        }
        if (i2 != 0) {
            return false;
        }
        open = new FileInputStream(file);
        i = mr.c(open).Bi;
        if (open != null) {
            open.close();
        }
        if (i2 > i) {
        }
        return i2 > i;
    }

    public static boolean dE() {
        String str = Environment.getExternalStorageDirectory().toString() + "/DCIM";
        File file = new File(str);
        if (!file.isDirectory() && !file.mkdirs()) {
            return false;
        }
        file = new File(str, ".probe");
        try {
            if (file.exists()) {
                file.delete();
            }
            if (!file.createNewFile()) {
                return false;
            }
            file.delete();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean eV() {
        nc ncVar = new nc("tms");
        String string = ncVar.getString("soft_version", "");
        String strFromEnvMap = TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_SOFTVERSION);
        if (string.equals(strFromEnvMap)) {
            return false;
        }
        ncVar.a("soft_version", strFromEnvMap, true);
        return true;
    }

    public static boolean eW() {
        String externalStorageState = Environment.getExternalStorageState();
        return externalStorageState != null ? externalStorageState.equals("mounted") : false;
    }

    public static List<String> eX() {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        List<String> arrayList = new ArrayList();
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/mounts"));
            try {
                String str = "^/(?:sys|system|dev|cache|proc|acct|data|efs|osh|pds|(?:mnt/asec)|(?:mnt/obb)|(?:mnt/secure))/*.*$";
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    String[] split = readLine.split("\\s+");
                    if (split.length >= 4 && split[3].startsWith("rw")) {
                        CharSequence charSequence = split[1];
                        if (charSequence.equals("/")) {
                            continue;
                        } else {
                            Matcher matcher = Pattern.compile(str).matcher(charSequence);
                            if (matcher != null) {
                                if (matcher.find()) {
                                }
                            }
                            if (!arrayList.contains(charSequence)) {
                                arrayList.add(charSequence);
                            }
                        }
                    }
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                return arrayList;
            } catch (FileNotFoundException e4) {
                e2 = e4;
            } catch (IOException e5) {
                e3 = e5;
            }
        } catch (FileNotFoundException e6) {
            e2 = e6;
            bufferedReader = null;
            try {
                e2.printStackTrace();
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                return arrayList;
            } catch (Throwable th2) {
                th = th2;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e7) {
                        e7.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (IOException e8) {
            e32 = e8;
            bufferedReader = null;
            e32.printStackTrace();
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e322) {
                    e322.printStackTrace();
                }
            }
            return arrayList;
        } catch (Throwable th3) {
            th = th3;
            bufferedReader = null;
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            throw th;
        }
    }

    public static List<String> eY() {
        List<String> arrayList = new ArrayList();
        try {
            arrayList.add(Environment.getExternalStorageDirectory().getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        arrayList.add("/storage");
        arrayList.add("/mnt/sdcard");
        arrayList.add("/mnt/sdcard-ext");
        arrayList.add("/storage/sdcard1");
        arrayList.addAll(eX());
        List<String> arrayList2 = new ArrayList();
        for (String file : arrayList) {
            Object canonicalPath;
            File file2 = new File(file);
            if (file2.exists() && file2.canRead()) {
                try {
                    canonicalPath = file2.getCanonicalPath();
                } catch (IOException e2) {
                }
                if (!(canonicalPath == null || arrayList2.contains(canonicalPath))) {
                    arrayList2.add(canonicalPath);
                }
            }
            canonicalPath = null;
            arrayList2.add(canonicalPath);
        }
        arrayList.clear();
        return arrayList2;
    }

    private static boolean o(Context context) {
        int i;
        InputStream fileInputStream;
        int i2;
        Exception e;
        Throwable th;
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(UpdateConfig.LOCATION_NAME, 1);
            byte[] bArr = new byte[8];
            inputStream.read(bArr);
            i = ((bArr[7] & 255) << 24) | (((bArr[4] & 255) | ((bArr[5] & 255) << 8)) | ((bArr[6] & 255) << 16));
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                }
            }
        } catch (Exception e3) {
            e3.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                }
            }
            i = 0;
        } catch (Throwable th2) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                }
            }
        }
        try {
            fileInputStream = new FileInputStream(context.getFilesDir().toString() + File.separator + UpdateConfig.LOCATION_NAME);
            try {
                byte[] bArr2 = new byte[8];
                fileInputStream.read(bArr2);
                i2 = ((bArr2[7] & 255) << 24) | (((bArr2[4] & 255) | ((bArr2[5] & 255) << 8)) | ((bArr2[6] & 255) << 16));
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e6) {
                    }
                }
            } catch (Exception e7) {
                e = e7;
                try {
                    e.printStackTrace();
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e8) {
                        }
                    }
                    i2 = 0;
                    return i > i2;
                } catch (Throwable th3) {
                    th = th3;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e9) {
                        }
                    }
                    throw th;
                }
            }
        } catch (Exception e10) {
            Exception exception = e10;
            fileInputStream = inputStream;
            e = exception;
            e.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            i2 = 0;
            if (i > i2) {
            }
        } catch (Throwable th4) {
            th = th4;
            fileInputStream = inputStream;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th;
        }
        if (i > i2) {
        }
    }

    public static int p(long j) {
        int i = 1;
        if (!eW()) {
            return 1;
        }
        if (!dE()) {
            return 2;
        }
        a aVar = new a();
        h.a(aVar);
        if (aVar.Ld < j) {
            i = 0;
        }
        return i == 0 ? 3 : 0;
    }

    public static List<String> p(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService("storage");
        List<String> arrayList = new ArrayList();
        try {
            Object[] objArr = (Object[]) storageManager.getClass().getMethod("getVolumeList", new Class[0]).invoke(storageManager, new Object[0]);
            if (objArr != null && objArr.length > 0) {
                Method declaredMethod = objArr[0].getClass().getDeclaredMethod("getPath", new Class[0]);
                Method method = storageManager.getClass().getMethod("getVolumeState", new Class[]{String.class});
                for (Object invoke : objArr) {
                    String str = (String) declaredMethod.invoke(invoke, new Object[0]);
                    if (str != null) {
                        if ("mounted".equals(method.invoke(storageManager, new Object[]{str}))) {
                            arrayList.add(str);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public static final String q(String str, String str2) {
        String decode = Uri.decode(str);
        if (decode != null) {
            int indexOf = decode.indexOf(63);
            if (indexOf > 0) {
                decode = decode.substring(0, indexOf);
            }
            if (!decode.endsWith("/")) {
                indexOf = decode.lastIndexOf(47) + 1;
                if (indexOf > 0) {
                    decode = decode.substring(indexOf);
                    if (decode == null) {
                        decode = str2;
                    }
                    return decode == null ? decode : "downloadfile";
                }
            }
        }
        decode = null;
        if (decode == null) {
            decode = str2;
        }
        if (decode == null) {
        }
    }
}
