package com.avast.android.sdk.engine.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import com.avast.android.sdk.engine.EngineInterface;
import com.avast.android.sdk.engine.ProgressObserver;
import com.avast.android.sdk.engine.UpdateCheckResultStructure;
import com.avast.android.sdk.engine.UpdateCheckResultStructure.UpdateCheck;
import com.avast.android.sdk.engine.UpdateResultStructure;
import com.avast.android.sdk.engine.UpdateResultStructure.UpdateResult;
import com.avast.android.sdk.engine.VpsInformation;
import com.avast.android.sdk.engine.obfuscated.af.a;
import com.avast.android.sdk.engine.obfuscated.af.a.b;
import com.avast.android.sdk.engine.obfuscated.ao;
import com.avast.android.sdk.engine.obfuscated.ar;
import com.avast.android.sdk.internal.SystemUtils;
import com.avast.android.sdk.internal.d;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipFile;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

/* compiled from: Unknown */
public class v {
    private static final String a = "utmp";
    private static final byte[] b = new byte[]{(byte) 65, (byte) 83, (byte) 87, (byte) 83, (byte) 105, (byte) 103, (byte) 110, (byte) 100};
    private static final byte[] c = new byte[]{(byte) 16, (byte) 60, (byte) 1, (byte) -68, (byte) -107, (byte) 117, (byte) 32, (byte) -21, (byte) 33, (byte) -31, (byte) 100, (byte) 18, (byte) -119, (byte) 2, (byte) -77, (byte) 42, (byte) 62, (byte) -40, (byte) -52, (byte) 15, (byte) 18, (byte) 92, (byte) -101, (byte) -78, (byte) 18, (byte) 15, (byte) -32, (byte) -70, (byte) 30, (byte) -53, (byte) -126, (byte) -37, (byte) -69, (byte) 47, (byte) -114, (byte) -57, (byte) -116, (byte) 76, (byte) 3, (byte) 11, (byte) 52, (byte) 50, (byte) 33, (byte) -64, (byte) -35, (byte) 101, (byte) 42, (byte) 62, (byte) 114, (byte) -50, (byte) 48, (byte) 108, (byte) -68, (byte) -123, (byte) -98, (byte) 41, (byte) 93, (byte) -34, (byte) 42, (byte) 1, (byte) -34, (byte) -36, (byte) -119, (byte) -26, (byte) 14, (byte) -63, (byte) -87, (byte) 111, (byte) 109, (byte) -120, (byte) 26, (byte) -83, (byte) 91, (byte) 6, (byte) -125, (byte) 91, (byte) 86, (byte) -29, (byte) 19, (byte) 42, (byte) 90, (byte) 97, (byte) 11, (byte) -111, (byte) 17, (byte) 75, (byte) 29, (byte) 18, (byte) -108, (byte) 115, (byte) 89, (byte) -45, (byte) 58, (byte) 111, (byte) -125, (byte) -92, (byte) 86, (byte) 65, (byte) -109, (byte) 89, (byte) -116, (byte) -13, (byte) 74, (byte) -24, (byte) 103, (byte) -29, (byte) 38, (byte) -25, (byte) -123, (byte) -4, (byte) -115, (byte) -27, (byte) -9, (byte) -120, (byte) 45, (byte) -114, (byte) 72, (byte) 32, (byte) 75, (byte) -95, (byte) -51, (byte) 88, (byte) -116, (byte) 78, (byte) -76, (byte) 29, (byte) -105, (byte) -7};
    private static final byte[] d = new byte[]{(byte) -48, (byte) 0, (byte) 22, (byte) 23, (byte) 125, (byte) -108, (byte) 44, (byte) -34, (byte) 62, (byte) -14, (byte) 83, (byte) 19, (byte) -107, (byte) 117, (byte) 95, (byte) -20, (byte) -48, (byte) -17, (byte) -41, (byte) 3, (byte) 56, (byte) 33, (byte) -34, (byte) 76, (byte) 69, (byte) 29, (byte) 48, (byte) 2, (byte) 61, (byte) -10, (byte) -97, (byte) 51, (byte) -65, (byte) -3, (byte) -12, (byte) 69, (byte) -28, (byte) 77, (byte) 49, (byte) 84, (byte) -42, (byte) 18, (byte) 16, (byte) -103, (byte) -66, (byte) -108, (byte) 54, (byte) 125, (byte) -54, (byte) 84, (byte) 110, (byte) -125, (byte) -16, (byte) 112, (byte) 2, (byte) -97, (byte) 110, (byte) 52, (byte) 34, (byte) 34, (byte) -85, (byte) -6, (byte) 2, (byte) -29, (byte) 99, (byte) 28, (byte) -78, (byte) 125, (byte) -100, (byte) 62, (byte) -11, (byte) 100, (byte) 98, (byte) -39, (byte) -51, (byte) 48, (byte) -63, (byte) -99, (byte) 74, (byte) -18, (byte) 51, (byte) -92, (byte) -75, (byte) -46, (byte) 27, (byte) -66, (byte) -95, (byte) 119, (byte) -101, (byte) 91, Byte.MAX_VALUE, (byte) -100, (byte) -92, (byte) 123, (byte) 18, (byte) 117, (byte) 33, (byte) 102, (byte) 122, (byte) -77, (byte) 111, (byte) -86, (byte) -6, (byte) 11, (byte) -83, (byte) -107, (byte) -94, (byte) 1, (byte) -19, (byte) -113, (byte) 37, (byte) -87, (byte) -113, (byte) -28, (byte) -27, (byte) -86, (byte) 115, (byte) -95, (byte) 54, (byte) -24, (byte) 103, (byte) -125, (byte) 106, (byte) 51, (byte) 20, (byte) 78, (byte) -43, (byte) -103};
    private static final byte[] e = new byte[]{(byte) -83, (byte) 83, (byte) 25, (byte) 5, (byte) 75, (byte) -46, (byte) -102, (byte) 32, (byte) 66, (byte) 5, (byte) -70, (byte) 49, (byte) 30, (byte) -29, (byte) -116, (byte) -41, (byte) 51, (byte) -20, (byte) 96, (byte) 125};
    private static final byte[] f = new byte[]{(byte) 50, (byte) 33, (byte) 11, (byte) -66, (byte) 59, (byte) 105, (byte) 24, (byte) -85, (byte) -97, (byte) -19, (byte) -51, (byte) -118, (byte) 51, (byte) -125, (byte) -21, (byte) 98, (byte) -95, (byte) 5, (byte) 72, (byte) -61, (byte) 103, (byte) -52, (byte) -22, (byte) 62, (byte) -42, (byte) -37, (byte) -36, (byte) -127, (byte) 11, (byte) 70, (byte) 120, (byte) 124, (byte) 42, (byte) 62, (byte) -111, (byte) 44, (byte) -76, (byte) 21, (byte) 59, (byte) -124, (byte) 81, (byte) -68, (byte) -120, (byte) 52, (byte) 93, (byte) 14, (byte) -94, (byte) 125, (byte) 125, (byte) -108, (byte) -103, (byte) -37, (byte) 98, (byte) -95, (byte) -106, (byte) 79, (byte) 107, (byte) 39, (byte) 62, (byte) 124, (byte) -81, (byte) -11, (byte) 84, (byte) 99, (byte) -56, (byte) -19, (byte) -125, (byte) 104, (byte) -73, (byte) 38, (byte) -33, (byte) 75, (byte) -35, (byte) 45, (byte) -94, (byte) 43, (byte) 24, (byte) -75, (byte) -37, (byte) 84, (byte) 25, (byte) 102, (byte) -103, (byte) -67, (byte) 10, (byte) -21, (byte) 9, (byte) 70, (byte) -67, (byte) 36, (byte) 46, (byte) 74, (byte) -122, (byte) -111, (byte) -21, (byte) 28, (byte) -47, (byte) 89, (byte) 55, (byte) 59, (byte) 42, (byte) 62, (byte) 82, (byte) -16, (byte) -114, (byte) 22, (byte) 13, (byte) 10, (byte) -119, (byte) -110, (byte) 36, (byte) 70, (byte) 16, (byte) -18, (byte) -54, (byte) -38, (byte) -42, (byte) -105, (byte) 96, (byte) -108, (byte) -16, (byte) -79, (byte) -9, (byte) 87, (byte) 98, (byte) 119, (byte) -43, (byte) -21};
    private static final String g = "vps_v4_info.vpx";
    private static final int h = 240;

    @SuppressLint({"NewApi"})
    public static UpdateCheckResultStructure a(Context context) {
        String uri = k.c(context, null).getUri().toString();
        AndroidHttpClient newInstance = AndroidHttpClient.newInstance("avdroid");
        HttpUriRequest httpGet = new HttpGet(uri + g);
        InputStream inputStream = null;
        HttpResponse httpResponse = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        UpdateCheckResultStructure updateCheckResultStructure = new UpdateCheckResultStructure();
        httpResponse = newInstance.execute(httpGet);
        if (httpResponse != null) {
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                int i;
                byte[] bArr;
                Object toByteArray;
                a a;
                b f;
                Integer valueOf;
                VpsInformation vpsInformation;
                String[] split;
                String[] split2;
                int i2;
                String[] split3;
                String[] strArr;
                Object obj;
                String[] split4;
                inputStream = entity.getContent();
                int i3 = 0;
                if ((httpResponse.getEntity().getContentLength() <= 0 ? 1 : null) == null) {
                    i3 = (int) httpResponse.getEntity().getContentLength();
                    if (((long) i3) != httpResponse.getEntity().getContentLength()) {
                        i = 0;
                        byteArrayOutputStream = i > 0 ? new ByteArrayOutputStream() : new ByteArrayOutputStream(i);
                        bArr = new byte[4096];
                        while (true) {
                            i = inputStream.read(bArr);
                            if (i < 0) {
                                break;
                            }
                            byteArrayOutputStream.write(bArr, 0, i);
                        }
                        toByteArray = byteArrayOutputStream.toByteArray();
                        if (toByteArray.length < b.length + 40) {
                            while (i3 < toByteArray.length) {
                                try {
                                    if (toByteArray[i3] != b[i3 - (toByteArray.length - b.length)]) {
                                    } else {
                                        updateCheckResultStructure.checkResult = UpdateCheck.ERROR_SIGNATURE_NOT_VALID;
                                        if (inputStream != null) {
                                            try {
                                                inputStream.close();
                                            } catch (IOException e) {
                                            }
                                        }
                                        if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                                            try {
                                                httpResponse.getEntity().consumeContent();
                                            } catch (IOException e2) {
                                            }
                                        }
                                        if (byteArrayOutputStream != null) {
                                            try {
                                                byteArrayOutputStream.close();
                                            } catch (IOException e3) {
                                            }
                                        }
                                        if (newInstance != null) {
                                            newInstance.close();
                                        }
                                        ao.a("Check result is " + updateCheckResultStructure);
                                        return updateCheckResultStructure;
                                    }
                                } catch (Throwable e4) {
                                    ao.a("Check exception", e4);
                                    updateCheckResultStructure.checkResult = UpdateCheck.ERROR_CONNECTION_PROBLEMS;
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (IOException e5) {
                                        }
                                    }
                                    if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                                        try {
                                            httpResponse.getEntity().consumeContent();
                                        } catch (IOException e6) {
                                        }
                                    }
                                    if (byteArrayOutputStream != null) {
                                        try {
                                            byteArrayOutputStream.close();
                                        } catch (IOException e7) {
                                        }
                                    }
                                    if (newInstance != null) {
                                        newInstance.close();
                                    }
                                    ao.a("Check result is " + updateCheckResultStructure);
                                    return updateCheckResultStructure;
                                } catch (Throwable th) {
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (IOException e8) {
                                        }
                                    }
                                    if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                                        try {
                                            httpResponse.getEntity().consumeContent();
                                        } catch (IOException e9) {
                                        }
                                    }
                                    if (byteArrayOutputStream != null) {
                                        try {
                                            byteArrayOutputStream.close();
                                        } catch (IOException e10) {
                                        }
                                    }
                                    if (newInstance != null) {
                                        newInstance.close();
                                    }
                                    ao.a("Check result is " + updateCheckResultStructure);
                                }
                            }
                            if ((toByteArray.length - b.length) - 40 < 0) {
                                System.arraycopy(toByteArray, (toByteArray.length - b.length) - 40, new byte[40], 0, 40);
                                bArr = new byte[((toByteArray.length - b.length) - 40)];
                                System.arraycopy(toByteArray, 0, bArr, 0, (toByteArray.length - b.length) - 40);
                                a = a.a(bArr);
                                if (a.e()) {
                                    updateCheckResultStructure.checkResult = UpdateCheck.ERROR_WRONG_PROTO_FILE;
                                    if (inputStream != null) {
                                        try {
                                            inputStream.close();
                                        } catch (IOException e11) {
                                        }
                                    }
                                    if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                                        try {
                                            httpResponse.getEntity().consumeContent();
                                        } catch (IOException e12) {
                                        }
                                    }
                                    if (byteArrayOutputStream != null) {
                                        try {
                                            byteArrayOutputStream.close();
                                        } catch (IOException e13) {
                                        }
                                    }
                                    if (newInstance != null) {
                                        newInstance.close();
                                    }
                                    ao.a("Check result is " + updateCheckResultStructure);
                                    return updateCheckResultStructure;
                                }
                                f = a.f();
                                if (f.f()) {
                                    if (f.i() && f.c()) {
                                        try {
                                            valueOf = Integer.valueOf(Integer.parseInt(f.d().replace("-", "").replace("-", "")));
                                            vpsInformation = EngineInterface.getVpsInformation(context, null);
                                            if (vpsInformation != null) {
                                                if (valueOf.compareTo(Integer.valueOf(Integer.parseInt(vpsInformation.version.replace("-", "").replace("-", "")))) <= 0) {
                                                    updateCheckResultStructure.checkResult = UpdateCheck.RESULT_UP_TO_DATE;
                                                    if (inputStream != null) {
                                                        try {
                                                            inputStream.close();
                                                        } catch (IOException e14) {
                                                        }
                                                    }
                                                    if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                                                        try {
                                                            httpResponse.getEntity().consumeContent();
                                                        } catch (IOException e15) {
                                                        }
                                                    }
                                                    if (byteArrayOutputStream != null) {
                                                        try {
                                                            byteArrayOutputStream.close();
                                                        } catch (IOException e16) {
                                                        }
                                                    }
                                                    if (newInstance != null) {
                                                        newInstance.close();
                                                    }
                                                    ao.a("Check result is " + updateCheckResultStructure);
                                                    return updateCheckResultStructure;
                                                }
                                            }
                                            if (q.a().equals(f.j())) {
                                                split = q.a().split("_");
                                                split2 = f.j().split("_");
                                                while (i2 < split.length) {
                                                    split3 = split[i2].split("-");
                                                    if (split3.length < 2) {
                                                        split3 = split[i2].split("-");
                                                    }
                                                    strArr = split3;
                                                    if (strArr.length < 2) {
                                                        obj = null;
                                                        while (r5 < split2.length) {
                                                            split4 = r4.split("-");
                                                            if (split4.length < 2) {
                                                                split4 = split2[i2].split("-");
                                                            }
                                                            if (split4.length < 2) {
                                                                if (!split4[0].equalsIgnoreCase(strArr[0])) {
                                                                    if (Integer.valueOf(Integer.parseInt(split4[1])).compareTo(Integer.valueOf(Integer.parseInt(strArr[1]))) > 0) {
                                                                        obj = 1;
                                                                    } else {
                                                                        updateCheckResultStructure.checkResult = UpdateCheck.ERROR_OLD_APPLICATION_VERSION;
                                                                        if (inputStream != null) {
                                                                            try {
                                                                                inputStream.close();
                                                                            } catch (IOException e17) {
                                                                            }
                                                                        }
                                                                        if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                                                                            try {
                                                                                httpResponse.getEntity().consumeContent();
                                                                            } catch (IOException e18) {
                                                                            }
                                                                        }
                                                                        if (byteArrayOutputStream != null) {
                                                                            try {
                                                                                byteArrayOutputStream.close();
                                                                            } catch (IOException e19) {
                                                                            }
                                                                        }
                                                                        if (newInstance != null) {
                                                                            newInstance.close();
                                                                        }
                                                                        ao.a("Check result is " + updateCheckResultStructure);
                                                                        return updateCheckResultStructure;
                                                                    }
                                                                }
                                                            } else {
                                                                updateCheckResultStructure.checkResult = UpdateCheck.ERROR_BROKEN_VERSION_STRINGS;
                                                                if (inputStream != null) {
                                                                    try {
                                                                        inputStream.close();
                                                                    } catch (IOException e20) {
                                                                    }
                                                                }
                                                                if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                                                                    try {
                                                                        httpResponse.getEntity().consumeContent();
                                                                    } catch (IOException e21) {
                                                                    }
                                                                }
                                                                if (byteArrayOutputStream != null) {
                                                                    try {
                                                                        byteArrayOutputStream.close();
                                                                    } catch (IOException e22) {
                                                                    }
                                                                }
                                                                if (newInstance != null) {
                                                                    newInstance.close();
                                                                }
                                                                ao.a("Check result is " + updateCheckResultStructure);
                                                                return updateCheckResultStructure;
                                                            }
                                                        }
                                                        if (obj == null) {
                                                        } else {
                                                            updateCheckResultStructure.checkResult = UpdateCheck.ERROR_OLD_APPLICATION_VERSION;
                                                            if (inputStream != null) {
                                                                try {
                                                                    inputStream.close();
                                                                } catch (IOException e23) {
                                                                }
                                                            }
                                                            if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                                                                try {
                                                                    httpResponse.getEntity().consumeContent();
                                                                } catch (IOException e24) {
                                                                }
                                                            }
                                                            if (byteArrayOutputStream != null) {
                                                                try {
                                                                    byteArrayOutputStream.close();
                                                                } catch (IOException e25) {
                                                                }
                                                            }
                                                            if (newInstance != null) {
                                                                newInstance.close();
                                                            }
                                                            ao.a("Check result is " + updateCheckResultStructure);
                                                            return updateCheckResultStructure;
                                                        }
                                                    }
                                                    updateCheckResultStructure.checkResult = UpdateCheck.ERROR_BROKEN_VERSION_STRINGS;
                                                    if (inputStream != null) {
                                                        try {
                                                            inputStream.close();
                                                        } catch (IOException e26) {
                                                        }
                                                    }
                                                    if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                                                        try {
                                                            httpResponse.getEntity().consumeContent();
                                                        } catch (IOException e27) {
                                                        }
                                                    }
                                                    if (byteArrayOutputStream != null) {
                                                        try {
                                                            byteArrayOutputStream.close();
                                                        } catch (IOException e28) {
                                                        }
                                                    }
                                                    if (newInstance != null) {
                                                        newInstance.close();
                                                    }
                                                    ao.a("Check result is " + updateCheckResultStructure);
                                                    return updateCheckResultStructure;
                                                }
                                                updateCheckResultStructure.checkResult = UpdateCheck.RESULT_UP_TO_DATE;
                                                if (inputStream != null) {
                                                    try {
                                                        inputStream.close();
                                                    } catch (IOException e29) {
                                                    }
                                                }
                                                if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                                                    try {
                                                        httpResponse.getEntity().consumeContent();
                                                    } catch (IOException e30) {
                                                    }
                                                }
                                                if (byteArrayOutputStream != null) {
                                                    try {
                                                        byteArrayOutputStream.close();
                                                    } catch (IOException e31) {
                                                    }
                                                }
                                                if (newInstance != null) {
                                                    newInstance.close();
                                                }
                                                ao.a("Check result is " + updateCheckResultStructure);
                                                return updateCheckResultStructure;
                                            }
                                            updateCheckResultStructure.checkResult = UpdateCheck.RESULT_UPDATE_AVAILABLE;
                                            updateCheckResultStructure.vpsUrl = uri + f.g();
                                            if (inputStream != null) {
                                                try {
                                                    inputStream.close();
                                                } catch (IOException e32) {
                                                }
                                            }
                                            if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                                                try {
                                                    httpResponse.getEntity().consumeContent();
                                                } catch (IOException e33) {
                                                }
                                            }
                                            if (byteArrayOutputStream != null) {
                                                try {
                                                    byteArrayOutputStream.close();
                                                } catch (IOException e34) {
                                                }
                                            }
                                            if (newInstance != null) {
                                                newInstance.close();
                                            }
                                            ao.a("Check result is " + updateCheckResultStructure);
                                            return updateCheckResultStructure;
                                        } catch (NumberFormatException e35) {
                                            updateCheckResultStructure.checkResult = UpdateCheck.ERROR_WRONG_PROTO_FILE;
                                            if (inputStream != null) {
                                                try {
                                                    inputStream.close();
                                                } catch (IOException e36) {
                                                }
                                            }
                                            if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                                                try {
                                                    httpResponse.getEntity().consumeContent();
                                                } catch (IOException e37) {
                                                }
                                            }
                                            if (byteArrayOutputStream != null) {
                                                try {
                                                    byteArrayOutputStream.close();
                                                } catch (IOException e38) {
                                                }
                                            }
                                            if (newInstance != null) {
                                                newInstance.close();
                                            }
                                            ao.a("Check result is " + updateCheckResultStructure);
                                            return updateCheckResultStructure;
                                        }
                                    }
                                }
                                updateCheckResultStructure.checkResult = UpdateCheck.ERROR_WRONG_PROTO_FILE;
                                if (inputStream != null) {
                                    try {
                                        inputStream.close();
                                    } catch (IOException e39) {
                                    }
                                }
                                if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                                    try {
                                        httpResponse.getEntity().consumeContent();
                                    } catch (IOException e40) {
                                    }
                                }
                                if (byteArrayOutputStream != null) {
                                    try {
                                        byteArrayOutputStream.close();
                                    } catch (IOException e41) {
                                    }
                                }
                                if (newInstance != null) {
                                    newInstance.close();
                                }
                                ao.a("Check result is " + updateCheckResultStructure);
                                return updateCheckResultStructure;
                            }
                            updateCheckResultStructure.checkResult = UpdateCheck.ERROR_WRONG_PROTO_FILE;
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e42) {
                                }
                            }
                            if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                                try {
                                    httpResponse.getEntity().consumeContent();
                                } catch (IOException e43) {
                                }
                            }
                            if (byteArrayOutputStream != null) {
                                try {
                                    byteArrayOutputStream.close();
                                } catch (IOException e44) {
                                }
                            }
                            if (newInstance != null) {
                                newInstance.close();
                            }
                            ao.a("Check result is " + updateCheckResultStructure);
                            return updateCheckResultStructure;
                        }
                        updateCheckResultStructure.checkResult = UpdateCheck.ERROR_WRONG_PROTO_FILE;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e45) {
                            }
                        }
                        if (!(httpResponse == null || httpResponse.getEntity() == null)) {
                            try {
                                httpResponse.getEntity().consumeContent();
                            } catch (IOException e46) {
                            }
                        }
                        if (byteArrayOutputStream != null) {
                            try {
                                byteArrayOutputStream.close();
                            } catch (IOException e47) {
                            }
                        }
                        if (newInstance != null) {
                            newInstance.close();
                        }
                        ao.a("Check result is " + updateCheckResultStructure);
                        return updateCheckResultStructure;
                    }
                }
                i = i3;
                if (i > 0) {
                }
                byteArrayOutputStream = i > 0 ? new ByteArrayOutputStream() : new ByteArrayOutputStream(i);
                bArr = new byte[4096];
                while (true) {
                    i = inputStream.read(bArr);
                    if (i < 0) {
                        break;
                    }
                    byteArrayOutputStream.write(bArr, 0, i);
                }
                toByteArray = byteArrayOutputStream.toByteArray();
                if (toByteArray.length < b.length + 40) {
                    updateCheckResultStructure.checkResult = UpdateCheck.ERROR_WRONG_PROTO_FILE;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    httpResponse.getEntity().consumeContent();
                    if (byteArrayOutputStream != null) {
                        byteArrayOutputStream.close();
                    }
                    if (newInstance != null) {
                        newInstance.close();
                    }
                    ao.a("Check result is " + updateCheckResultStructure);
                    return updateCheckResultStructure;
                }
                for (i3 = toByteArray.length - b.length; i3 < toByteArray.length; i3++) {
                    if (toByteArray[i3] != b[i3 - (toByteArray.length - b.length)]) {
                        updateCheckResultStructure.checkResult = UpdateCheck.ERROR_SIGNATURE_NOT_VALID;
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        httpResponse.getEntity().consumeContent();
                        if (byteArrayOutputStream != null) {
                            byteArrayOutputStream.close();
                        }
                        if (newInstance != null) {
                            newInstance.close();
                        }
                        ao.a("Check result is " + updateCheckResultStructure);
                        return updateCheckResultStructure;
                    }
                }
                if ((toByteArray.length - b.length) - 40 < 0) {
                    updateCheckResultStructure.checkResult = UpdateCheck.ERROR_WRONG_PROTO_FILE;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    httpResponse.getEntity().consumeContent();
                    if (byteArrayOutputStream != null) {
                        byteArrayOutputStream.close();
                    }
                    if (newInstance != null) {
                        newInstance.close();
                    }
                    ao.a("Check result is " + updateCheckResultStructure);
                    return updateCheckResultStructure;
                }
                System.arraycopy(toByteArray, (toByteArray.length - b.length) - 40, new byte[40], 0, 40);
                bArr = new byte[((toByteArray.length - b.length) - 40)];
                System.arraycopy(toByteArray, 0, bArr, 0, (toByteArray.length - b.length) - 40);
                a = a.a(bArr);
                if (a.e()) {
                    updateCheckResultStructure.checkResult = UpdateCheck.ERROR_WRONG_PROTO_FILE;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    httpResponse.getEntity().consumeContent();
                    if (byteArrayOutputStream != null) {
                        byteArrayOutputStream.close();
                    }
                    if (newInstance != null) {
                        newInstance.close();
                    }
                    ao.a("Check result is " + updateCheckResultStructure);
                    return updateCheckResultStructure;
                }
                f = a.f();
                if (f.f()) {
                    valueOf = Integer.valueOf(Integer.parseInt(f.d().replace("-", "").replace("-", "")));
                    vpsInformation = EngineInterface.getVpsInformation(context, null);
                    if (vpsInformation != null) {
                        if (valueOf.compareTo(Integer.valueOf(Integer.parseInt(vpsInformation.version.replace("-", "").replace("-", "")))) <= 0) {
                            updateCheckResultStructure.checkResult = UpdateCheck.RESULT_UP_TO_DATE;
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            httpResponse.getEntity().consumeContent();
                            if (byteArrayOutputStream != null) {
                                byteArrayOutputStream.close();
                            }
                            if (newInstance != null) {
                                newInstance.close();
                            }
                            ao.a("Check result is " + updateCheckResultStructure);
                            return updateCheckResultStructure;
                        }
                    }
                    if (q.a().equals(f.j())) {
                        split = q.a().split("_");
                        split2 = f.j().split("_");
                        for (i2 = 0; i2 < split.length; i2++) {
                            split3 = split[i2].split("-");
                            if (split3.length < 2) {
                                split3 = split[i2].split("-");
                            }
                            strArr = split3;
                            if (strArr.length < 2) {
                                updateCheckResultStructure.checkResult = UpdateCheck.ERROR_BROKEN_VERSION_STRINGS;
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                                httpResponse.getEntity().consumeContent();
                                if (byteArrayOutputStream != null) {
                                    byteArrayOutputStream.close();
                                }
                                if (newInstance != null) {
                                    newInstance.close();
                                }
                                ao.a("Check result is " + updateCheckResultStructure);
                                return updateCheckResultStructure;
                            }
                            obj = null;
                            for (String split5 : split2) {
                                split4 = split5.split("-");
                                if (split4.length < 2) {
                                    split4 = split2[i2].split("-");
                                }
                                if (split4.length < 2) {
                                    updateCheckResultStructure.checkResult = UpdateCheck.ERROR_BROKEN_VERSION_STRINGS;
                                    if (inputStream != null) {
                                        inputStream.close();
                                    }
                                    httpResponse.getEntity().consumeContent();
                                    if (byteArrayOutputStream != null) {
                                        byteArrayOutputStream.close();
                                    }
                                    if (newInstance != null) {
                                        newInstance.close();
                                    }
                                    ao.a("Check result is " + updateCheckResultStructure);
                                    return updateCheckResultStructure;
                                }
                                if (!split4[0].equalsIgnoreCase(strArr[0])) {
                                    if (Integer.valueOf(Integer.parseInt(split4[1])).compareTo(Integer.valueOf(Integer.parseInt(strArr[1]))) > 0) {
                                        updateCheckResultStructure.checkResult = UpdateCheck.ERROR_OLD_APPLICATION_VERSION;
                                        if (inputStream != null) {
                                            inputStream.close();
                                        }
                                        httpResponse.getEntity().consumeContent();
                                        if (byteArrayOutputStream != null) {
                                            byteArrayOutputStream.close();
                                        }
                                        if (newInstance != null) {
                                            newInstance.close();
                                        }
                                        ao.a("Check result is " + updateCheckResultStructure);
                                        return updateCheckResultStructure;
                                    }
                                    obj = 1;
                                }
                            }
                            if (obj == null) {
                                updateCheckResultStructure.checkResult = UpdateCheck.ERROR_OLD_APPLICATION_VERSION;
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                                httpResponse.getEntity().consumeContent();
                                if (byteArrayOutputStream != null) {
                                    byteArrayOutputStream.close();
                                }
                                if (newInstance != null) {
                                    newInstance.close();
                                }
                                ao.a("Check result is " + updateCheckResultStructure);
                                return updateCheckResultStructure;
                            }
                        }
                        updateCheckResultStructure.checkResult = UpdateCheck.RESULT_UP_TO_DATE;
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        httpResponse.getEntity().consumeContent();
                        if (byteArrayOutputStream != null) {
                            byteArrayOutputStream.close();
                        }
                        if (newInstance != null) {
                            newInstance.close();
                        }
                        ao.a("Check result is " + updateCheckResultStructure);
                        return updateCheckResultStructure;
                    }
                    updateCheckResultStructure.checkResult = UpdateCheck.RESULT_UPDATE_AVAILABLE;
                    updateCheckResultStructure.vpsUrl = uri + f.g();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    httpResponse.getEntity().consumeContent();
                    if (byteArrayOutputStream != null) {
                        byteArrayOutputStream.close();
                    }
                    if (newInstance != null) {
                        newInstance.close();
                    }
                    ao.a("Check result is " + updateCheckResultStructure);
                    return updateCheckResultStructure;
                }
                updateCheckResultStructure.checkResult = UpdateCheck.ERROR_WRONG_PROTO_FILE;
                if (inputStream != null) {
                    inputStream.close();
                }
                httpResponse.getEntity().consumeContent();
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (newInstance != null) {
                    newInstance.close();
                }
                ao.a("Check result is " + updateCheckResultStructure);
                return updateCheckResultStructure;
            }
            throw new IOException();
        }
        throw new IOException();
    }

    public static UpdateResultStructure a(Context context, File file) {
        int i = 0;
        UpdateResultStructure updateResultStructure = new UpdateResultStructure();
        File dir = context.getDir(q.a, 0);
        File[] listFiles = dir.listFiles();
        switch (z.a[q.a(context).ordinal()]) {
            case 1:
                ao.a("Can't unregister");
                updateResultStructure.result = UpdateResult.RESULT_UNKNOWN_ERROR;
                return updateResultStructure;
            default:
                File file2;
                if (listFiles != null) {
                    int i2 = 0;
                    while (i2 < listFiles.length) {
                        try {
                            file2 = new File(dir + File.separator + listFiles[i2].getName() + "_old");
                            if (listFiles[i2].renameTo(file2)) {
                                i2++;
                            } else {
                                ao.a("Can't rename " + listFiles[i2].getAbsolutePath() + " to " + file2);
                                updateResultStructure.result = UpdateResult.RESULT_UNKNOWN_ERROR;
                                return updateResultStructure;
                            }
                        } catch (IOException e) {
                            updateResultStructure.result = UpdateResult.RESULT_NOT_ENOUGH_INTERNAL_SPACE_TO_UPDATE;
                            return updateResultStructure;
                        }
                    }
                }
                File file3 = new File(dir + File.separator + file.getName());
                if (file.renameTo(file3)) {
                    File dir2 = context.getDir(q.a, 0);
                    String[] a = d.a(dir2, new ZipFile(file3), new x(SystemUtils.a()));
                    if (a == null || a.length == 0) {
                        ao.a("VPS native library for the given cpu architecture not found.");
                        d.a(dir2, new ZipFile(file3), new y());
                    }
                    switch (z.b[q.a(context, file3.getName(), a).ordinal()]) {
                        case 1:
                            updateResultStructure.result = UpdateResult.RESULT_UPDATED;
                            return updateResultStructure;
                        default:
                            q.a(context);
                            if (d(context)) {
                                updateResultStructure.result = UpdateResult.RESULT_INVALID_VPS;
                                return updateResultStructure;
                            }
                            updateResultStructure.result = UpdateResult.RESULT_UNKNOWN_ERROR;
                            if (!file3.delete()) {
                                ao.a("Can't delete " + file3.getAbsolutePath());
                            }
                            File[] listFiles2 = dir.listFiles();
                            if (listFiles2 != null) {
                                while (i < listFiles2.length) {
                                    if (listFiles2[i].getName().endsWith("_old")) {
                                        String name = listFiles2[i].getName();
                                        file2 = new File(dir + File.separator + name.substring(0, name.length() - 5));
                                        if (!listFiles2[i].renameTo(file2)) {
                                            ao.a("Can't rename " + listFiles2[i] + " to " + file2);
                                        }
                                    }
                                    i++;
                                }
                            }
                            return updateResultStructure;
                    }
                    updateResultStructure.result = UpdateResult.RESULT_NOT_ENOUGH_INTERNAL_SPACE_TO_UPDATE;
                    return updateResultStructure;
                }
                ao.a("Can't rename " + file.getAbsolutePath() + " to " + file3);
                updateResultStructure.result = UpdateResult.RESULT_UNKNOWN_ERROR;
                return updateResultStructure;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @SuppressLint({"NewApi"})
    public static File a(Context context, String str, ProgressObserver progressObserver) throws HttpException {
        ar arVar;
        Throwable e;
        OutputStream outputStream;
        HttpEntity httpEntity;
        ByteArrayOutputStream byteArrayOutputStream;
        int i = 0;
        FileOutputStream fileOutputStream = null;
        try {
            HttpUriRequest httpGet = new HttpGet(str);
            AndroidHttpClient newInstance = AndroidHttpClient.newInstance("avdroid");
            HttpEntity entity;
            FileOutputStream fileOutputStream2;
            try {
                entity = newInstance.execute(httpGet).getEntity();
                if (entity != null) {
                    try {
                        int contentLength;
                        if ((entity.getContentLength() <= 0 ? 1 : 0) == 0) {
                            contentLength = (int) entity.getContentLength();
                            if (((long) contentLength) == entity.getContentLength()) {
                                i = contentLength;
                            }
                        }
                        OutputStream byteArrayOutputStream2 = i <= 0 ? new ByteArrayOutputStream() : new ByteArrayOutputStream(i);
                        try {
                            arVar = new ar(entity.getContent(), entity.getContentLength(), new w(progressObserver));
                            try {
                                byte[] bArr = new byte[4096];
                                while (true) {
                                    contentLength = arVar.read(bArr);
                                    if (contentLength < 0) {
                                        break;
                                    }
                                    byteArrayOutputStream2.write(bArr, 0, contentLength);
                                }
                                bArr = byteArrayOutputStream2.toByteArray();
                                File file = new File(context.getDir(a, 0).getAbsolutePath() + "/" + str.substring(str.lastIndexOf("/")));
                                fileOutputStream2 = new FileOutputStream(file);
                                try {
                                    fileOutputStream2.write(bArr);
                                    fileOutputStream2.flush();
                                    if (arVar != null) {
                                        try {
                                            arVar.close();
                                        } catch (IOException e2) {
                                        }
                                    }
                                    if (entity != null) {
                                        try {
                                            entity.consumeContent();
                                        } catch (IOException e3) {
                                        }
                                    }
                                    if (byteArrayOutputStream2 != null) {
                                        try {
                                            byteArrayOutputStream2.close();
                                        } catch (IOException e4) {
                                        }
                                    }
                                    if (fileOutputStream2 != null) {
                                        try {
                                            fileOutputStream2.close();
                                        } catch (IOException e5) {
                                        }
                                    }
                                    if (newInstance != null) {
                                        newInstance.close();
                                    }
                                    ao.a("Downloaded VPS file is at " + file.getAbsolutePath());
                                    return file;
                                } catch (IOException e6) {
                                    e = e6;
                                    Object obj = arVar;
                                    outputStream = byteArrayOutputStream2;
                                    httpEntity = entity;
                                    try {
                                        ao.a("Download of new VPS failed", e);
                                        throw new HttpException();
                                    } catch (Throwable th) {
                                        e = th;
                                        entity = httpEntity;
                                        byteArrayOutputStream = outputStream;
                                        arVar = r2;
                                        fileOutputStream = fileOutputStream2;
                                        if (arVar != null) {
                                            try {
                                                arVar.close();
                                            } catch (IOException e7) {
                                            }
                                        }
                                        if (entity != null) {
                                            try {
                                                entity.consumeContent();
                                            } catch (IOException e8) {
                                            }
                                        }
                                        if (byteArrayOutputStream != null) {
                                            try {
                                                byteArrayOutputStream.close();
                                            } catch (IOException e9) {
                                            }
                                        }
                                        if (fileOutputStream != null) {
                                            try {
                                                fileOutputStream.close();
                                            } catch (IOException e10) {
                                            }
                                        }
                                        if (newInstance != null) {
                                            newInstance.close();
                                        }
                                        throw e;
                                    }
                                } catch (Throwable th2) {
                                    e = th2;
                                    fileOutputStream = fileOutputStream2;
                                    if (arVar != null) {
                                        arVar.close();
                                    }
                                    if (entity != null) {
                                        entity.consumeContent();
                                    }
                                    if (byteArrayOutputStream != null) {
                                        byteArrayOutputStream.close();
                                    }
                                    if (fileOutputStream != null) {
                                        fileOutputStream.close();
                                    }
                                    if (newInstance != null) {
                                        newInstance.close();
                                    }
                                    throw e;
                                }
                            } catch (IOException e11) {
                                e = e11;
                                fileOutputStream2 = null;
                                ao.a("Can't write new VPS", e);
                                if (arVar != null) {
                                    try {
                                        arVar.close();
                                    } catch (IOException e12) {
                                    }
                                }
                                if (entity != null) {
                                    try {
                                        entity.consumeContent();
                                    } catch (IOException e13) {
                                    }
                                }
                                if (byteArrayOutputStream2 != null) {
                                    try {
                                        byteArrayOutputStream2.close();
                                    } catch (IOException e14) {
                                    }
                                }
                                if (fileOutputStream2 != null) {
                                    try {
                                        fileOutputStream2.close();
                                    } catch (IOException e15) {
                                    }
                                }
                                if (newInstance != null) {
                                    newInstance.close();
                                }
                                return null;
                            } catch (NullPointerException e16) {
                                fileOutputStream2 = null;
                                ao.a("NPE in VPS update");
                                if (arVar != null) {
                                    try {
                                        arVar.close();
                                    } catch (IOException e17) {
                                    }
                                }
                                if (entity != null) {
                                    try {
                                        entity.consumeContent();
                                    } catch (IOException e18) {
                                    }
                                }
                                if (byteArrayOutputStream2 != null) {
                                    try {
                                        byteArrayOutputStream2.close();
                                    } catch (IOException e19) {
                                    }
                                }
                                if (fileOutputStream2 != null) {
                                    try {
                                        fileOutputStream2.close();
                                    } catch (IOException e20) {
                                    }
                                }
                                if (newInstance != null) {
                                    newInstance.close();
                                }
                                return null;
                            } catch (Throwable th3) {
                                e = th3;
                            }
                        } catch (IOException e21) {
                            e = e21;
                            fileOutputStream2 = null;
                            outputStream = byteArrayOutputStream2;
                            httpEntity = entity;
                            ao.a("Download of new VPS failed", e);
                            throw new HttpException();
                        } catch (Throwable th4) {
                            e = th4;
                            arVar = null;
                            if (arVar != null) {
                                arVar.close();
                            }
                            if (entity != null) {
                                entity.consumeContent();
                            }
                            if (byteArrayOutputStream != null) {
                                byteArrayOutputStream.close();
                            }
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                            if (newInstance != null) {
                                newInstance.close();
                            }
                            throw e;
                        }
                    } catch (IOException e22) {
                        e = e22;
                        fileOutputStream2 = null;
                        outputStream = null;
                        httpEntity = entity;
                        ao.a("Download of new VPS failed", e);
                        throw new HttpException();
                    } catch (Throwable th5) {
                        e = th5;
                        arVar = null;
                        byteArrayOutputStream = null;
                        if (arVar != null) {
                            arVar.close();
                        }
                        if (entity != null) {
                            entity.consumeContent();
                        }
                        if (byteArrayOutputStream != null) {
                            byteArrayOutputStream.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        if (newInstance != null) {
                            newInstance.close();
                        }
                        throw e;
                    }
                }
                throw new HttpException("Response entity is null");
            } catch (IOException e23) {
                e = e23;
                fileOutputStream2 = null;
                outputStream = null;
                Object obj2 = null;
                ao.a("Download of new VPS failed", e);
                throw new HttpException();
            } catch (Throwable th6) {
                e = th6;
                arVar = null;
                byteArrayOutputStream = null;
                entity = null;
                if (arVar != null) {
                    arVar.close();
                }
                if (entity != null) {
                    entity.consumeContent();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (newInstance != null) {
                    newInstance.close();
                }
                throw e;
            }
        } catch (IllegalArgumentException e24) {
            ao.a("Invalid VPS uri");
            throw new HttpException();
        }
    }

    public static boolean a() {
        return true;
    }

    public static int b() {
        return h;
    }

    public static boolean b(Context context) {
        File[] listFiles = context.getDir(q.a, 0).listFiles();
        boolean z = true;
        if (listFiles != null) {
            boolean z2 = true;
            int i = 0;
            while (i < listFiles.length) {
                if (listFiles[i].getName().endsWith("_old") && !listFiles[i].delete()) {
                    ao.a("Can't delete " + listFiles[i].getAbsolutePath());
                    z2 = false;
                }
                i++;
            }
            z = z2;
        }
        c(context);
        return z;
    }

    public static boolean b(Context context, File file) {
        try {
            return new aa(context, file).a();
        } catch (IOException e) {
            ao.a("VPS is not a valid ZIP file");
            return false;
        }
    }

    public static void c(Context context) {
        int i = 0;
        File dir = context.getDir(a, 0);
        File[] listFiles = dir.listFiles();
        if (listFiles != null) {
            while (i < listFiles.length) {
                if (!listFiles[i].delete()) {
                    ao.a("Can't clean up file " + listFiles[i].getAbsolutePath());
                }
                i++;
            }
        }
        if (!dir.delete()) {
            ao.a("Can't clean up dir " + dir.getAbsolutePath());
        }
    }

    private static boolean d(Context context) {
        File[] listFiles = context.getDir(q.a, 0).listFiles();
        if (listFiles == null) {
            return true;
        }
        boolean z = true;
        int i = 0;
        while (i < listFiles.length) {
            if (!(listFiles[i].getName().endsWith("_old") || listFiles[i].delete())) {
                ao.a("Can't delete " + listFiles[i].getAbsolutePath());
                z = false;
            }
            i++;
        }
        return z;
    }
}
