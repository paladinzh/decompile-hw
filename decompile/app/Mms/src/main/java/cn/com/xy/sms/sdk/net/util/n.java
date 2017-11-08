package cn.com.xy.sms.sdk.net.util;

import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.location.places.Place;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/* compiled from: Unknown */
public final class n {
    static Map<String, Boolean> a = new ConcurrentHashMap();
    private static String b = "95ad98c4ba9a0ec12a7dca2af77f312bef6fd02580c23fc082b28f1cab03d9d5b7694bd5dd9693a8b6786c9480dfbcc462373bd1b9f5bed66151be80a370465d6516f89e66d6d70ba52a3d063acbe4544a585d62896d953b3269efd345ff888e5ed7f7f7b60c862ca5a27f20ccdba704113a9861fcd91cf3f0fd7115987568d04f444224b3c2436b833ed0439b4fa8c92e938827f360b6a4a070fed4608a46c8a52023fabfd2561bcd4205052254caaffe9a55aa73254537a1a2c0efbcd76254bef3e01902ffee20b0a45b6c8e6beb496c9c3494d263dedf0fff4702ebbfee0cb568da4940b8f5f8c89aa96b2c21e2ff9596e30e26b18e1b563353843ee95787";
    private static HashMap<String, Integer> c = new HashMap();

    public static Boolean a(String str) {
        File file = new File(str);
        if (!file.exists()) {
            return Boolean.valueOf(false);
        }
        String name = file.getName();
        Boolean.valueOf(false);
        try {
            Boolean bool = (Boolean) a.get(name);
            if (bool != null) {
                return bool;
            }
            Constant.getContext();
            String b = b(str);
            if (!StringUtils.isNull(b)) {
                if (b.indexOf("95ad98c4ba9a0ec12a7dca2af77f312bef6fd02580c23fc082b28f1cab03d9d5b7694bd5dd9693a8b6786c9480dfbcc462373bd1b9f5bed66151be80a370465d6516f89e66d6d70ba52a3d063acbe4544a585d62896d953b3269efd345ff888e5ed7f7f7b60c862ca5a27f20ccdba704113a9861fcd91cf3f0fd7115987568d04f444224b3c2436b833ed0439b4fa8c92e938827f360b6a4a070fed4608a46c8a52023fabfd2561bcd4205052254caaffe9a55aa73254537a1a2c0efbcd76254bef3e01902ffee20b0a45b6c8e6beb496c9c3494d263dedf0fff4702ebbfee0cb568da4940b8f5f8c89aa96b2c21e2ff9596e30e26b18e1b563353843ee95787") != -1) {
                    bool = Boolean.valueOf(true);
                    a.d.execute(new o(name));
                    a.put(name, bool);
                    return bool;
                }
            }
            bool = Boolean.valueOf(false);
            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", " getSignResult not valid: " + name, null);
            a.put(name, bool);
            return bool;
        } catch (Throwable th) {
            return Boolean.valueOf(false);
        }
    }

    public static String a(String str, String str2) {
        BufferedReader bufferedReader;
        Throwable th;
        BufferedReader bufferedReader2 = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(new File(new StringBuilder(String.valueOf(str)).append(str2).toString())));
            try {
                StringBuffer stringBuffer = new StringBuffer();
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    stringBuffer.append(readLine);
                }
                String trim = stringBuffer.toString().trim();
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                }
                return trim;
            } catch (Throwable th2) {
                Throwable th3 = th2;
                bufferedReader2 = bufferedReader;
                th = th3;
                if (bufferedReader2 != null) {
                    try {
                        bufferedReader2.close();
                    } catch (IOException e2) {
                    }
                }
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            if (bufferedReader2 != null) {
                bufferedReader2.close();
            }
            throw th;
        }
    }

    public static void a() {
        a.h.execute(new p());
    }

    public static boolean a(byte b) {
        try {
            KeyManager.initAppKey();
        } catch (Throwable th) {
        }
        return !StringUtils.isNull(KeyManager.channel) ? !"XwIDAQABYUN".equals(KeyManager.channel) ? !"NQIDAQABCOOL".equals(KeyManager.channel) ? !"6QIDAQABSTARRYSKY".equals(KeyManager.channel) ? !"vwIDAQABLIANLUOOS".equals(KeyManager.channel) ? !"FEhNrwHTXL".equals(KeyManager.channel) ? !"1i1BDH2wONE+".equals(KeyManager.channel) ? !"Oq1QGcwIYUNOS".equals(KeyManager.channel) ? !"j3FIT5mwLETV".equals(KeyManager.channel) ? !"D6mKXM8MEIZU".equals(KeyManager.channel) ? !"3GdfMSKwHUAWEI".equals(KeyManager.channel) ? !"0GCSqGSITOS".equals(KeyManager.channel) ? !"wupzCqnwGUAIWU".equals(KeyManager.channel) ? !"XRyvMvZwSMARTISAN".equals(KeyManager.channel) ? !"dToXA5JQDAKELE".equals(KeyManager.channel) ? !"p5O4wKmwGIONEE".equals(KeyManager.channel) ? !"z5N7W51wKINGSUN".equals(KeyManager.channel) ? !"Cko59T6wSUGAR".equals(KeyManager.channel) ? !"oWIH+3ZQLEIDIANOS".equals(KeyManager.channel) ? !"al30zFgQTEST_T".equals(KeyManager.channel) ? !"gsjHPHwIKOOBEE".equals(KeyManager.channel) ? !"QlTNSIgQWENTAI2".equals(KeyManager.channel) ? !"JqyMtaHQNUBIA".equals(KeyManager.channel) ? !"15Du354QGIONEECARD".equals(KeyManager.channel) ? !"rahtBH7wTCL".equals(KeyManager.channel) ? !"xU6UT6pwTOS2".equals(KeyManager.channel) ? !"5Gx84kmwYULONG_COOLPAD".equals(KeyManager.channel) ? !"tnjdWFeQKTOUCH".equals(KeyManager.channel) ? !"Uj2pznXQHCT".equals(KeyManager.channel) ? !"XkXZJmwIPPTV".equals(KeyManager.channel) ? !"PzqP0ONQTOSWATCH".equals(KeyManager.channel) ? !"VCTyBOSwSmartisan".equals(KeyManager.channel) ? !"HUAWEITMW".equals(KeyManager.channel) ? !"HUAWEIAND".equals(KeyManager.channel) ? !"5rLWVKgQMEITU_PHONE".equals(KeyManager.channel) ? !"zcK2P6yQINNOS".equals(KeyManager.channel) ? !"J2kSrxdQGigaset".equals(KeyManager.channel) ? !"RbWRsTYQdroi".equals(KeyManager.channel) ? !"5zZZdrFQIUNI".equals(KeyManager.channel) ? !"nZpg6u3wDOOV".equals(KeyManager.channel) ? !"RQIDAQABONEPLUSCARDNEW".equals(KeyManager.channel) ? !"i3GPvZLwASUS".equals(KeyManager.channel) ? !"cNNrw5WQEBEN".equals(KeyManager.channel) ? !"cNNrw5WQEBEN".equals(KeyManager.channel) ? !"UdcqV6aQLANMO".equals(KeyManager.channel) ? !"PunKwZfwHISENSE".equals(KeyManager.channel) ? !"gO0o2CXwVIVO".equals(KeyManager.channel) ? !"kpGIJXywSAMSUNGFLOW".equals(KeyManager.channel) ? !"DEaerxdwASUSCARD".equals(KeyManager.channel) ? !"d7tjnrkwCNSAMSUNG".equals(KeyManager.channel) ? !"NVbQx3QQMEIZUCENTER".equals(KeyManager.channel) ? !"K8wgPuIwFREEMEOS".equals(KeyManager.channel) ? !"uDM3hYtwGIGASET".equals(KeyManager.channel) ? !"OmwdltCwONEPLUS2".equals(KeyManager.channel) ? !"eOXJhLyQLINGHIT".equals(KeyManager.channel) ? !"mmNPM4cQVNEW_ZTE2".equals(KeyManager.channel) ? !"ZkhM4GyQ360OS".equals(KeyManager.channel) ? !SmartSmsSdkUtil.DUOQU_SDK_CHANNEL.equals(KeyManager.channel) ? !"Hg9iPQ4wLIFENUM_A".equals(KeyManager.channel) ? !"vRICR8qQYULONG_COOLPAD2".equals(KeyManager.channel) ? !"v22YJ3QwKINGSOFTMAIL".equals(KeyManager.channel) ? !"W5MmRZCwIMOO".equals(KeyManager.channel) ? !"XHpWJNFQTCLOS".equals(KeyManager.channel) ? !"R1pU1XXwUNISCOPE".equals(KeyManager.channel) ? !"gOLrCBhQMEIZU2".equals(KeyManager.channel) ? !"MkekV0RQRAGENTEK".equals(KeyManager.channel) ? !"rNllyzbwLAKALA".equals(KeyManager.channel) ? !"YVmD5UkQ360OSBOX".equals(KeyManager.channel) ? !"MXUnXjvw360FLOW".equals(KeyManager.channel) ? !"sX7t39KQMEIZUDATA".equals(KeyManager.channel) ? !"2qqJKJbwZTE_TRIP".equals(KeyManager.channel) ? !"0LLy0INQWEHOME".equals(KeyManager.channel) ? !"n2zkSOdwZTE3".equals(KeyManager.channel) ? !"VrWc0QnQNUBIACARD".equals(KeyManager.channel) ? !"AINYCzUwMEIZUCENTER2".equals(KeyManager.channel) ? ("LLJ53XOw360CONTACTS".equals(KeyManager.channel) && b == (byte) 1) ? false : true : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 : (b == (byte) 1 || b == (byte) 2) ? false : true : b != (byte) 1 : b == (byte) 1 : b != (byte) 1 : b != (byte) 1 : b != (byte) 1 ? b != Constant.POWER_SMS_SPECIAL_VALUE ? true : true : false : b == (byte) 2 || b == (byte) 4 : false;
    }

    private static Certificate[] a(JarFile jarFile, JarEntry jarEntry) {
        Throwable th;
        Certificate[] certificateArr = null;
        InputStream inputStream;
        try {
            byte[] bArr = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
            inputStream = jarFile.getInputStream(jarEntry);
            if (inputStream != null) {
                do {
                    try {
                    } catch (IOException e) {
                    } catch (Throwable th2) {
                        th = th2;
                    }
                } while (inputStream.read(bArr, 0, Place.TYPE_SUBLOCALITY_LEVEL_2) != -1);
                if (jarEntry != null) {
                    certificateArr = jarEntry.getCertificates();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable th3) {
                    }
                }
                return certificateArr;
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th4) {
                }
            }
            return certificateArr;
        } catch (IOException e2) {
            inputStream = certificateArr;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th5) {
                }
            }
            return certificateArr;
        } catch (Throwable th6) {
            Throwable th7 = th6;
            inputStream = certificateArr;
            th = th7;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th8) {
                }
            }
            throw th;
        }
    }

    private static String b(String str) {
        Throwable th;
        JarFile jarFile = null;
        JarFile jarFile2;
        try {
            jarFile2 = new JarFile(str);
            try {
                Certificate[] a = a(jarFile2, jarFile2.getJarEntry("classes.dex"));
                if (a != null) {
                    if (a.length > 0 && a.length > 0) {
                        String obj = a[0].getPublicKey().toString();
                        try {
                            jarFile2.close();
                        } catch (Throwable th2) {
                        }
                        return obj;
                    }
                }
                jarFile2.close();
                try {
                    jarFile2.close();
                } catch (Throwable th3) {
                }
            } catch (Throwable th4) {
                Throwable th5 = th4;
                jarFile = jarFile2;
                th = th5;
                if (jarFile != null) {
                    try {
                        jarFile.close();
                    } catch (Throwable th6) {
                    }
                }
                throw th;
            }
        } catch (Throwable th7) {
            th = th7;
            if (jarFile != null) {
                jarFile.close();
            }
            throw th;
        }
        return "";
    }

    public static boolean b() {
        KeyManager.initAppKey();
        return true;
    }

    private static boolean b(byte b) {
        if (b == (byte) 1) {
            try {
                if (!c.a(Constant.getContext())) {
                    return false;
                }
                int hours = new Date().getHours();
                Integer num = (Integer) c.get(new StringBuilder(String.valueOf(hours)).toString());
                if (num != null) {
                    num = Integer.valueOf(num.intValue() + 1);
                } else {
                    c.clear();
                    num = Integer.valueOf(1);
                }
                c.put(new StringBuilder(String.valueOf(hours)).toString(), num);
                if (num.intValue() > VTMCDataCache.MAX_EXPIREDTIME) {
                    return false;
                }
            } catch (Throwable th) {
                return false;
            }
        }
        return true;
    }
}
