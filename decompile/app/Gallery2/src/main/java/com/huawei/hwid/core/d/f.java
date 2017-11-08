package com.huawei.hwid.core.d;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.android.gallery3d.gadget.XmlUtils;
import com.android.internal.http.multipart.FilePart;
import com.android.internal.http.multipart.MultipartEntity;
import com.android.internal.http.multipart.Part;
import com.android.internal.http.multipart.StringPart;
import com.huawei.hwid.a.a;
import com.huawei.hwid.core.constants.a$a;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.encrypt.d;
import com.huawei.hwid.vermanager.b;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlSerializer;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class f {
    private static FileOutputStream a = null;
    private static FileInputStream b = null;
    private static Properties c = null;

    public static boolean a(File file) {
        e.b("FileUtil", "deleteFile : file.getName=" + file.getName());
        if (!file.exists()) {
            return true;
        }
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (listFiles != null) {
                for (File a : listFiles) {
                    a(a);
                }
            }
        }
        return file.delete();
    }

    public static boolean a(Context context, String str) {
        if (context == null || TextUtils.isEmpty(str)) {
            return false;
        }
        return a(new File(context.getFilesDir(), str));
    }

    public static boolean a(String str, String str2, byte[] bArr) {
        Throwable e;
        FileOutputStream fileOutputStream = null;
        if (str == null || TextUtils.isEmpty(str) || str2 == null || TextUtils.isEmpty(str2) || bArr == null) {
            return false;
        }
        try {
            File file = new File(str);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return false;
                }
            }
            FileOutputStream fileOutputStream2 = new FileOutputStream(new File(str + str2));
            try {
                fileOutputStream2.write(bArr);
                if (fileOutputStream2 != null) {
                    try {
                        fileOutputStream2.close();
                    } catch (Throwable e2) {
                        e.d("FileUtil", "IOException / " + e2.getMessage(), e2);
                    }
                }
            } catch (FileNotFoundException e3) {
                e2 = e3;
                fileOutputStream = fileOutputStream2;
                try {
                    e.d("FileUtil", "writeAgreement FileNotFoundException:" + e2.getMessage(), e2);
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable e22) {
                            e.d("FileUtil", "IOException / " + e22.getMessage(), e22);
                        }
                    }
                    return false;
                } catch (Throwable th) {
                    e22 = th;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable e4) {
                            e.d("FileUtil", "IOException / " + e4.getMessage(), e4);
                        }
                    }
                    throw e22;
                }
            } catch (IOException e5) {
                e22 = e5;
                fileOutputStream = fileOutputStream2;
                e.d("FileUtil", "writeAgreement IOException:" + e22.getMessage(), e22);
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable e222) {
                        e.d("FileUtil", "IOException / " + e222.getMessage(), e222);
                    }
                }
                return false;
            } catch (NullPointerException e6) {
                e222 = e6;
                fileOutputStream = fileOutputStream2;
                e.d("FileUtil", "NullPointerException / " + e222.getMessage(), e222);
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable e2222) {
                        e.d("FileUtil", "IOException / " + e2222.getMessage(), e2222);
                    }
                }
                return true;
            } catch (Exception e7) {
                e2222 = e7;
                fileOutputStream = fileOutputStream2;
                e.d("FileUtil", "Exception / " + e2222.getMessage(), e2222);
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable e22222) {
                        e.d("FileUtil", "IOException / " + e22222.getMessage(), e22222);
                    }
                }
                return true;
            } catch (Throwable th2) {
                e22222 = th2;
                fileOutputStream = fileOutputStream2;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw e22222;
            }
        } catch (FileNotFoundException e8) {
            e22222 = e8;
            e.d("FileUtil", "writeAgreement FileNotFoundException:" + e22222.getMessage(), e22222);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return false;
        } catch (IOException e9) {
            e22222 = e9;
            e.d("FileUtil", "writeAgreement IOException:" + e22222.getMessage(), e22222);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return false;
        } catch (NullPointerException e10) {
            e22222 = e10;
            e.d("FileUtil", "NullPointerException / " + e22222.getMessage(), e22222);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return true;
        } catch (Exception e11) {
            e22222 = e11;
            e.d("FileUtil", "Exception / " + e22222.getMessage(), e22222);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return true;
        }
        return true;
    }

    public static synchronized String b(Context context, String str) {
        Throwable e;
        FileOutputStream fileOutputStream;
        FileInputStream fileInputStream;
        InputStream inputStream;
        Object obj;
        FileOutputStream fileOutputStream2 = null;
        synchronized (f.class) {
            String property;
            try {
                FileOutputStream fileOutputStream3;
                Properties properties = new Properties();
                if (new File(context.getFilesDir().getPath() + "/" + "settings.properties").exists()) {
                    fileOutputStream3 = fileOutputStream2;
                } else {
                    fileOutputStream3 = context.openFileOutput("settings.properties", 0);
                }
                try {
                    InputStream openFileInput = context.openFileInput("settings.properties");
                    if (openFileInput == null) {
                        try {
                            e.b("FileUtil", "inStream is null");
                        } catch (FileNotFoundException e2) {
                            e = e2;
                            fileOutputStream = fileOutputStream3;
                            fileInputStream = openFileInput;
                            fileOutputStream2 = fileOutputStream;
                        } catch (Throwable th) {
                            e = th;
                            fileOutputStream = fileOutputStream3;
                            inputStream = openFileInput;
                            fileOutputStream2 = fileOutputStream;
                        }
                    } else {
                        properties.load(openFileInput);
                    }
                    property = properties.getProperty(str);
                    if (TextUtils.isEmpty(property)) {
                        if (fileOutputStream3 != null) {
                            try {
                                fileOutputStream3.close();
                            } catch (Throwable e3) {
                                e.d("FileUtil", "IOException / " + e3.getMessage(), e3);
                            }
                        }
                        if (openFileInput != null) {
                            try {
                                openFileInput.close();
                            } catch (Throwable e4) {
                                e.d("FileUtil", "IOException / " + e4.getMessage(), e4);
                            }
                        }
                    } else {
                        for (Object equals : a$a.a()) {
                            if (str.equals(equals)) {
                                property = com.huawei.hwid.core.encrypt.e.c(context, property);
                                break;
                            }
                        }
                        if (fileOutputStream3 != null) {
                            try {
                                fileOutputStream3.close();
                            } catch (Throwable e32) {
                                e.d("FileUtil", "IOException / " + e32.getMessage(), e32);
                            }
                        }
                        if (openFileInput != null) {
                            try {
                                openFileInput.close();
                            } catch (Throwable e42) {
                                e.d("FileUtil", "IOException / " + e42.getMessage(), e42);
                            }
                        }
                    }
                } catch (FileNotFoundException e5) {
                    e = e5;
                    fileOutputStream = fileOutputStream3;
                    obj = fileOutputStream2;
                    fileOutputStream2 = fileOutputStream;
                    try {
                        e.d("FileUtil", "Can not find the file settings.properties", e);
                        if (fileOutputStream2 != null) {
                            try {
                                fileOutputStream2.close();
                            } catch (Throwable e6) {
                                e.d("FileUtil", "IOException / " + e6.getMessage(), e6);
                            }
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Throwable e62) {
                                e.d("FileUtil", "IOException / " + e62.getMessage(), e62);
                            }
                        }
                        return "";
                    } catch (Throwable th2) {
                        e62 = th2;
                        if (fileOutputStream2 != null) {
                            try {
                                fileOutputStream2.close();
                            } catch (Throwable e422) {
                                e.d("FileUtil", "IOException / " + e422.getMessage(), e422);
                            }
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Throwable e4222) {
                                e.d("FileUtil", "IOException / " + e4222.getMessage(), e4222);
                            }
                        }
                        throw e62;
                    }
                } catch (Throwable th3) {
                    e62 = th3;
                    fileOutputStream = fileOutputStream3;
                    obj = fileOutputStream2;
                    fileOutputStream2 = fileOutputStream;
                    if (fileOutputStream2 != null) {
                        fileOutputStream2.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw e62;
                }
            } catch (FileNotFoundException e7) {
                e62 = e7;
                obj = fileOutputStream2;
                e.d("FileUtil", "Can not find the file settings.properties", e62);
                if (fileOutputStream2 != null) {
                    fileOutputStream2.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return "";
            } catch (Throwable th4) {
                e62 = th4;
                fileInputStream = fileOutputStream2;
                if (fileOutputStream2 != null) {
                    fileOutputStream2.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw e62;
            }
        }
        return property;
        return property;
    }

    private static void b(Context context, String str, String str2) throws Exception {
        int i = 0;
        File filesDir = context.getFilesDir();
        if (filesDir != null && filesDir.getPath() != null) {
            if (new File(filesDir.getPath() + "/" + "settings.properties").exists()) {
                b = context.openFileInput("settings.properties");
                if (b == null) {
                    e.b("FileUtil", "inStream is null");
                } else {
                    c.load(b);
                }
            }
            a = context.openFileOutput("settings.properties", 0);
            String[] a = a$a.a();
            int length = a.length;
            while (i < length) {
                if (str.equals(a[i])) {
                    str2 = com.huawei.hwid.core.encrypt.e.b(context, str2);
                    break;
                }
                i++;
            }
            c.setProperty(str, str2);
            if (a == null) {
                e.b("FileUtil", "outStream is null");
            } else {
                c.store(a, "accountagent");
            }
        }
    }

    public static synchronized void a(Context context, String str, String str2) {
        synchronized (f.class) {
            if (context == null || str2 == null || str == null) {
                e.b("FileUtil", "at least 1 param is null");
                return;
            }
            a = null;
            b = null;
            c = new Properties();
            try {
                b(context, str, str2);
                try {
                    if (a != null) {
                        a.close();
                    }
                } catch (IOException e) {
                    e.d("FileUtil", "setProperties IOException");
                }
                try {
                    if (b != null) {
                        b.close();
                    }
                } catch (IOException e2) {
                    e.d("FileUtil", "IOException");
                }
            } catch (Exception e3) {
                e.d("FileUtil", "IOException");
                try {
                    if (a != null) {
                        a.close();
                    }
                } catch (IOException e4) {
                    e.d("FileUtil", "setProperties IOException");
                }
                try {
                    if (b != null) {
                        b.close();
                    }
                } catch (IOException e5) {
                    e.d("FileUtil", "IOException");
                }
                return;
            } catch (Throwable th) {
                try {
                    if (a != null) {
                        a.close();
                    }
                } catch (IOException e6) {
                    e.d("FileUtil", "setProperties IOException");
                }
                try {
                    if (b != null) {
                        b.close();
                    }
                } catch (IOException e7) {
                    e.d("FileUtil", "IOException");
                }
            }
        }
    }

    private static void b(Context context, String[] strArr) throws Exception {
        int i = 0;
        if (new File(context.getFilesDir().getPath() + "/" + "settings.properties").exists()) {
            b = context.openFileInput("settings.properties");
            if (b == null) {
                e.b("FileUtil", "inStream is null");
            } else {
                c.load(b);
            }
        }
        a = context.openFileOutput("settings.properties", 0);
        if (strArr != null && strArr.length > 0) {
            int length = strArr.length;
            while (i < length) {
                Object obj = strArr[i];
                if (obj != null) {
                    c.remove(obj);
                }
                i++;
            }
        }
        if (a == null) {
            e.b("FileUtil", "outStream is null");
        } else {
            c.store(a, "accountagent");
        }
    }

    public static synchronized void a(Context context, String[] strArr) {
        synchronized (f.class) {
            a = null;
            b = null;
            c = new Properties();
            try {
                b(context, strArr);
                try {
                    if (a != null) {
                        a.close();
                    }
                } catch (Throwable e) {
                    e.d("FileUtil", "removeProperties IOException:" + e.getMessage(), e);
                }
                try {
                    if (b != null) {
                        b.close();
                    }
                } catch (Throwable e2) {
                    e.d("FileUtil", "IOException / " + e2.getMessage(), e2);
                }
            } catch (Exception e3) {
                e.d("FileUtil", "Exception / " + e3.getMessage());
                try {
                    if (a != null) {
                        a.close();
                    }
                } catch (Throwable e22) {
                    e.d("FileUtil", "removeProperties IOException:" + e22.getMessage(), e22);
                }
                try {
                    if (b != null) {
                        b.close();
                    }
                } catch (Throwable e222) {
                    e.d("FileUtil", "IOException / " + e222.getMessage(), e222);
                }
            } catch (Throwable th) {
                try {
                    if (a != null) {
                        a.close();
                    }
                } catch (Throwable e4) {
                    e.d("FileUtil", "removeProperties IOException:" + e4.getMessage(), e4);
                }
                try {
                    if (b != null) {
                        b.close();
                    }
                } catch (Throwable e42) {
                    e.d("FileUtil", "IOException / " + e42.getMessage(), e42);
                }
            }
        }
        return;
    }

    public static void a(XmlSerializer xmlSerializer, String str, String str2) {
        try {
            xmlSerializer.startTag("", str);
            xmlSerializer.text(str2);
            xmlSerializer.endTag("", str);
        } catch (Throwable e) {
            e.d("FileUtil", "IllegalArgumentException / " + e.getMessage(), e);
        } catch (Throwable e2) {
            e.d("FileUtil", "IllegalStateException / " + e2.getMessage(), e2);
        } catch (Throwable e22) {
            e.d("FileUtil", "IOException / " + e22.getMessage(), e22);
        } catch (Throwable e222) {
            e.d("FileUtil", "NullPointerException / " + e222.getMessage(), e222);
        }
    }

    @SuppressLint({"TrulyRandom"})
    public static void a(Context context, String str, HttpPost httpPost) {
        HwAccount b = a.a(context).b(context, str, null);
        if (b == null) {
            e.b("FileUtil", "account is null");
            return;
        }
        Object g = b.g();
        String d = b.d();
        Object a = com.huawei.hwid.b.a.a(context).a(d);
        if (TextUtils.isEmpty(d) || TextUtils.isEmpty(g)) {
            e.b("FileUtil", "token or userId is null");
            return;
        }
        String str2 = System.currentTimeMillis() + ":" + new SecureRandom().nextInt(1000);
        String str3 = "";
        if (!TextUtils.isEmpty(com.huawei.hwid.core.b.a.a.c())) {
            str3 = com.huawei.hwid.core.b.a.a.c().substring(com.huawei.hwid.core.b.a.a.c().lastIndexOf("/") + 1).replace("?Version=10002", "");
        }
        httpPost.addHeader("Authorization", "Digest user=" + d + "," + "nonce" + "=" + str2 + "," + "response" + "=" + d.a(str2 + ":" + "" + str3, g));
        if (!TextUtils.isEmpty(a)) {
            httpPost.addHeader("Cookie", a);
        }
    }

    public static String a(Context context, File file, String str, HashMap<String, String> hashMap, String str2) {
        e.b("FileUtil", "begin to upLoad photo");
        StringBuffer stringBuffer = new StringBuffer();
        try {
            Part[] a = a(file, (HashMap) hashMap);
            if (a.length >= 5) {
                HttpClient a2 = b.a().a(context, 18080, 18443);
                HttpPost httpPost = new HttpPost(str);
                a(context, str2, httpPost);
                a2.getParams().setParameter("http.protocol.content-charset", XmlUtils.INPUT_ENCODING);
                httpPost.getParams().setIntParameter("http.socket.timeout", 20000);
                httpPost.getParams().setIntParameter("http.connection.timeout", 20000);
                httpPost.setEntity(new MultipartEntity(a, httpPost.getParams()));
                HttpResponse execute = a2.execute(httpPost);
                int statusCode = execute.getStatusLine().getStatusCode();
                if (SmsCheckResult.ESCT_200 != statusCode) {
                    e.c("FileUtil", "resultCode is " + statusCode);
                } else {
                    e.a("FileUtil", "resultCode is ok");
                    String entityUtils = EntityUtils.toString(execute.getEntity(), XmlUtils.INPUT_ENCODING);
                    e.b("FileUtil", "response responseXMLContent = " + com.huawei.hwid.core.encrypt.f.a(entityUtils, true));
                    stringBuffer.append(entityUtils);
                }
                return stringBuffer.toString();
            }
            e.c("FileUtil", "param is null or not enough");
            return stringBuffer.toString();
        } catch (Throwable e) {
            e.d("FileUtil", "upload photo failed, NullPointerException : " + e.getMessage(), e);
        } catch (Throwable e2) {
            e.d("FileUtil", "upload photo failed, IOException : " + e2.getMessage(), e2);
        } catch (Throwable e22) {
            e.d("FileUtil", "upload photo failed Exception : " + e22.getMessage(), e22);
        }
    }

    public static Intent a(String str, Intent intent) {
        e.a("FileUtil", "begin to put  result string To intent");
        try {
            if (TextUtils.isEmpty(str)) {
                e.c("FileUtil", "string is empty");
                return null;
            }
            for (String split : str.split("&")) {
                String[] split2 = split.split("=");
                if (split2.length > 1) {
                    intent.putExtra(split2[0], split2[1]);
                }
            }
            return intent;
        } catch (Throwable e) {
            e.d("FileUtil", "put  result string To intent occur : " + e.getMessage(), e);
        }
    }

    public static Part[] a(File file, HashMap<String, String> hashMap) throws FileNotFoundException {
        if (file == null || !file.isFile() || hashMap == null || hashMap.isEmpty()) {
            e.c("FileUtil", "init body failed");
            return new Part[0];
        }
        Set<Entry> entrySet = hashMap.entrySet();
        int size = entrySet.size();
        Part[] partArr = new Part[(size + 1)];
        e.a("FileUtil", "begin to init body");
        int i = 0;
        for (Entry entry : entrySet) {
            partArr[i] = new StringPart((String) entry.getKey(), (String) entry.getValue());
            i++;
        }
        partArr[size] = new FilePart("BigImage", file);
        return partArr;
    }
}
