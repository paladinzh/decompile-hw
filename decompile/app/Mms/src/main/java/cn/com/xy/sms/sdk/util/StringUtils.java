package cn.com.xy.sms.sdk.util;

import com.google.android.gms.location.places.Place;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Document;

/* compiled from: Unknown */
public class StringUtils {
    public static final String MPLUG86 = "+86";
    private static final String a = "0123456789ABCDEF";
    private static final String b = "UTF-8";
    private static final String c = "+86";
    private static final String d = "\\+86";
    private static final String e = "0086";
    private static final String f = "86";
    private static final String g = "17951";
    private static final String h = "12593";
    public static final String phoneFiled10193 = "10193";
    public static final String phoneFiled12520 = "12520";
    public static final String phoneFiled17908 = "17908";
    public static final String phoneFiled17909 = "17909";
    public static final String phoneFiled17911 = "17911";
    public static final String phoneFiled179110 = "179110";

    public static byte[] MD5(byte[] bArr) {
        MessageDigest instance;
        try {
            instance = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            instance = null;
        }
        if (instance == null) {
            return null;
        }
        instance.update(bArr);
        return instance.digest();
    }

    public static boolean allValuesIsNotNull(String... strArr) {
        if (strArr == null || strArr.length == 0) {
            return false;
        }
        for (String isNull : strArr) {
            if (isNull(isNull)) {
                return false;
            }
        }
        return true;
    }

    public static List<String> arryToList(JSONArray jSONArray) {
        if (jSONArray != null) {
            try {
                if (jSONArray.length() > 0) {
                    List<String> arrayList = new ArrayList();
                    for (int i = 0; i < jSONArray.length(); i++) {
                        arrayList.add(jSONArray.get(i).toString());
                    }
                    return arrayList;
                }
            } catch (JSONException e) {
            }
        }
        return null;
    }

    public static String bytesToHexString(byte[] bArr) {
        int i = 0;
        if (bArr == null) {
            return null;
        }
        String str = "0123456789abcdef";
        char[] cArr = new char[(bArr.length * 2)];
        int i2 = 0;
        while (i2 < bArr.length) {
            cArr[i] = (char) str.charAt((bArr[i2] >> 4) & 15);
            i++;
            cArr[i] = (char) str.charAt(bArr[i2] & 15);
            i2++;
            i++;
        }
        return String.valueOf(cArr);
    }

    public static byte[] compressGZip(byte[] bArr) {
        byte[] bArr2 = null;
        try {
            OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gZIPOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            gZIPOutputStream.write(bArr);
            gZIPOutputStream.finish();
            gZIPOutputStream.close();
            bArr2 = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return bArr2;
        } catch (Throwable th) {
            return bArr2;
        }
    }

    public static byte[] decodToBytes(String str) {
        Throwable th;
        ByteArrayOutputStream byteArrayOutputStream = null;
        ByteArrayOutputStream byteArrayOutputStream2;
        try {
            byteArrayOutputStream2 = new ByteArrayOutputStream(str.length() / 2);
            int i = 0;
            while (i < str.length()) {
                try {
                    byteArrayOutputStream2.write((a.indexOf(str.charAt(i)) << 4) | a.indexOf(str.charAt(i + 1)));
                    i += 2;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    byteArrayOutputStream = byteArrayOutputStream2;
                    th = th3;
                }
            }
            byte[] toByteArray = byteArrayOutputStream2.toByteArray();
            try {
                byteArrayOutputStream2.close();
            } catch (IOException e) {
            }
            return toByteArray;
        } catch (Throwable th4) {
            th = th4;
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e2) {
                }
            }
            throw th;
        }
    }

    public static String decode(String str) {
        try {
            return new String(decodToBytes(str), b);
        } catch (Throwable th) {
            return null;
        }
    }

    public static String encode(String str) {
        try {
            return encode(str.getBytes(b));
        } catch (Throwable th) {
            return null;
        }
    }

    public static String encode(byte[] bArr) {
        try {
            StringBuilder stringBuilder = new StringBuilder(bArr.length << 1);
            for (int i = 0; i < bArr.length; i++) {
                stringBuilder.append(a.charAt((bArr[i] & 240) >> 4));
                stringBuilder.append(a.charAt(bArr[i] & 15));
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getFileMD5(String str) {
        try {
            InputStream fileInputStream = new FileInputStream(str);
            byte[] bArr = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
            MessageDigest instance = MessageDigest.getInstance("MD5");
            while (true) {
                int read = fileInputStream.read(bArr);
                if (read > 0) {
                    instance.update(bArr, 0, read);
                } else {
                    fileInputStream.close();
                    return getMD5(instance.digest());
                }
            }
        } catch (Throwable th) {
            return null;
        }
    }

    public static long getLongByString(String str) {
        try {
            return isNull(str) ? -1 : Long.parseLong(str);
        } catch (Throwable th) {
            return -1;
        }
    }

    public static String getMD5(String str) {
        return getMD5(str.getBytes());
    }

    public static String getMD5(byte[] bArr) {
        return bytesToHexString(MD5(bArr));
    }

    public static String getNoNullString(String str) {
        return str != null ? str.trim() : "";
    }

    public static String getPhoneNumberNo86(String str) {
        if (isNull(str)) {
            return str;
        }
        str = str.replace(" ", "").replace("-", "").replace("(", "").replace(")", "");
        return !str.startsWith("+86") ? !str.startsWith(e) ? !str.startsWith(f) ? (str.startsWith(g) && str.length() > 10) ? str.replaceFirst(g, "") : (str.startsWith(h) && str.length() > 10) ? str.replaceFirst(h, "") : (!str.startsWith(phoneFiled12520) || str.length() <= 10) ? str : str.replaceFirst(phoneFiled12520, "") : str.replaceFirst(f, "") : str.replaceFirst(e, "") : str.replaceFirst(d, "");
    }

    public static String getSubString(String str) {
        return (!isNull(str) && str.length() >= 7) ? str.substring(0, 7) : str;
    }

    public static String getTwoDigitType(String str) {
        return !isNull(str) ? str.length() >= 2 ? str.length() <= 2 ? str : "99" : "0" + str : "";
    }

    public static String getValueByKey(Map<String, String> map, String str) {
        return (map == null || map.isEmpty() || isNull(str)) ? "" : (String) map.get(str);
    }

    public static String handlerAssemble(String str) {
        try {
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < 4; i++) {
                int i2 = i << 4;
                stringBuffer.append(decode(str.substring(i2, i2 + 16)));
            }
            return stringBuffer.toString();
        } catch (Throwable th) {
            return "";
        }
    }

    public static boolean isNull(String str) {
        return str == null || str.trim().length() == 0 || str.trim().equalsIgnoreCase("null");
    }

    public static boolean isNull2(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isNumber(String str) {
        if (isNull(str)) {
            return false;
        }
        return Pattern.compile("[0-9]*").matcher(getPhoneNumberNo86(str)).matches();
    }

    public static boolean isPhoneNumber(String str) {
        return !isNull(str) ? sj(getPhoneNumberNo86(str)) : false;
    }

    public static String[] jsonArryToArray(JSONArray jSONArray) {
        if (jSONArray == null || jSONArray.length() <= 0) {
            return null;
        }
        String[] strArr = new String[jSONArray.length()];
        for (int i = 0; i < jSONArray.length(); i++) {
            try {
                strArr[i] = jSONArray.getString(i);
            } catch (JSONException e) {
            }
        }
        return strArr;
    }

    public static String replaceBlank(String str) {
        return !isNull(str) ? str.replaceAll("\\s", "") : null;
    }

    public static boolean sj(String str) {
        if (str == null || str.length() != 11 || "13800138000".equals(str)) {
            return false;
        }
        if (str.startsWith("13") || str.startsWith("14") || str.startsWith("15") || str.startsWith("18") || str.startsWith("17")) {
            return true;
        }
        return false;
    }

    public static Document stringConvertXML(String str, String str2) {
        Closeable byteArrayInputStream;
        Document parse;
        Throwable th;
        Closeable closeable = null;
        if (isNull(str)) {
            return closeable;
        }
        try {
            if (str.indexOf("?>") != -1) {
                str = str.substring(str.indexOf("?>") + 2);
            }
            StringBuilder stringBuilder = new StringBuilder(str2);
            stringBuilder.append(str);
            DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
            byteArrayInputStream = new ByteArrayInputStream(stringBuilder.toString().getBytes("utf-8"));
            try {
                parse = newInstance.newDocumentBuilder().parse(byteArrayInputStream);
                f.a(byteArrayInputStream);
            } catch (Throwable th2) {
                Throwable th3 = th2;
                closeable = byteArrayInputStream;
                th = th3;
                f.a(closeable);
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            f.a(closeable);
            throw th;
        }
        return parse;
    }

    public static String trim(String str) {
        return (str == null || str.length() == 0) ? "" : str.trim().replace("\r", "").replace("\n", "").replace("\t", "");
    }

    public static byte[] uncompressGZip(byte[] bArr) {
        InputStream byteArrayInputStream = new ByteArrayInputStream(bArr);
        GZIPInputStream gZIPInputStream = new GZIPInputStream(byteArrayInputStream);
        byte[] bArr2 = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (true) {
            int read = gZIPInputStream.read(bArr2, 0, Place.TYPE_SUBLOCALITY_LEVEL_2);
            if (read != -1) {
                byteArrayOutputStream.write(bArr2, 0, read);
            } else {
                bArr2 = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
                gZIPInputStream.close();
                byteArrayInputStream.close();
                return bArr2;
            }
        }
    }
}
