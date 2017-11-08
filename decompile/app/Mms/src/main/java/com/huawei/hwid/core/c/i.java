package com.huawei.hwid.core.c;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.text.TextUtils;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.constants.b;
import com.huawei.hwid.core.encrypt.e;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: FileUtil */
public class i {
    public static boolean a(File file) {
        a.b("FileUtil", "deleteFile : file.getName=" + file.getName());
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
                        a.d("FileUtil", "IOException / " + e2.toString(), e2);
                    }
                }
            } catch (FileNotFoundException e3) {
                e2 = e3;
                fileOutputStream = fileOutputStream2;
                try {
                    a.d("FileUtil", "writeAgreement FileNotFoundException:" + e2.toString(), e2);
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable e22) {
                            a.d("FileUtil", "IOException / " + e22.toString(), e22);
                        }
                    }
                    return false;
                } catch (Throwable th) {
                    e22 = th;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable e4) {
                            a.d("FileUtil", "IOException / " + e4.toString(), e4);
                        }
                    }
                    throw e22;
                }
            } catch (IOException e5) {
                e22 = e5;
                fileOutputStream = fileOutputStream2;
                a.d("FileUtil", "writeAgreement IOException:" + e22.toString(), e22);
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable e222) {
                        a.d("FileUtil", "IOException / " + e222.toString(), e222);
                    }
                }
                return false;
            } catch (NullPointerException e6) {
                e222 = e6;
                fileOutputStream = fileOutputStream2;
                a.d("FileUtil", "NullPointerException / " + e222.toString(), e222);
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable e2222) {
                        a.d("FileUtil", "IOException / " + e2222.toString(), e2222);
                    }
                }
                return true;
            } catch (Exception e7) {
                e2222 = e7;
                fileOutputStream = fileOutputStream2;
                a.d("FileUtil", "Exception / " + e2222.toString(), e2222);
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable e22222) {
                        a.d("FileUtil", "IOException / " + e22222.toString(), e22222);
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
            a.d("FileUtil", "writeAgreement FileNotFoundException:" + e22222.toString(), e22222);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return false;
        } catch (IOException e9) {
            e22222 = e9;
            a.d("FileUtil", "writeAgreement IOException:" + e22222.toString(), e22222);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return false;
        } catch (NullPointerException e10) {
            e22222 = e10;
            a.d("FileUtil", "NullPointerException / " + e22222.toString(), e22222);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return true;
        } catch (Exception e11) {
            e22222 = e11;
            a.d("FileUtil", "Exception / " + e22222.toString(), e22222);
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
        synchronized (i.class) {
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
                            a.b("FileUtil", "inStream is null");
                        } catch (FileNotFoundException e2) {
                            e = e2;
                            fileOutputStream = fileOutputStream3;
                            fileInputStream = openFileInput;
                            fileOutputStream2 = fileOutputStream;
                        } catch (IOException e3) {
                            e = e3;
                            fileOutputStream = fileOutputStream3;
                            inputStream = openFileInput;
                            fileOutputStream2 = fileOutputStream;
                        } catch (NullPointerException e4) {
                            e = e4;
                            fileOutputStream = fileOutputStream3;
                            inputStream = openFileInput;
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
                            } catch (Throwable e5) {
                                a.d("FileUtil", "IOException / " + e5.toString(), e5);
                            }
                        }
                        if (openFileInput != null) {
                            try {
                                openFileInput.close();
                            } catch (Throwable e6) {
                                a.d("FileUtil", "IOException / " + e6.toString(), e6);
                            }
                        }
                    } else {
                        for (Object equals : b.a()) {
                            if (str.equals(equals)) {
                                property = e.c(context, property);
                                break;
                            }
                        }
                        if (fileOutputStream3 != null) {
                            try {
                                fileOutputStream3.close();
                            } catch (Throwable e52) {
                                a.d("FileUtil", "IOException / " + e52.toString(), e52);
                            }
                        }
                        if (openFileInput != null) {
                            try {
                                openFileInput.close();
                            } catch (Throwable e62) {
                                a.d("FileUtil", "IOException / " + e62.toString(), e62);
                            }
                        }
                    }
                } catch (FileNotFoundException e7) {
                    e = e7;
                    fileOutputStream = fileOutputStream3;
                    obj = fileOutputStream2;
                    fileOutputStream2 = fileOutputStream;
                    try {
                        a.d("FileUtil", "Can not find the file settings.properties", e);
                        if (fileOutputStream2 != null) {
                            try {
                                fileOutputStream2.close();
                            } catch (Throwable e8) {
                                a.d("FileUtil", "IOException / " + e8.toString(), e8);
                            }
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Throwable e82) {
                                a.d("FileUtil", "IOException / " + e82.toString(), e82);
                            }
                        }
                        return "";
                    } catch (Throwable th2) {
                        e82 = th2;
                        if (fileOutputStream2 != null) {
                            try {
                                fileOutputStream2.close();
                            } catch (Throwable e622) {
                                a.d("FileUtil", "IOException / " + e622.toString(), e622);
                            }
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Throwable e6222) {
                                a.d("FileUtil", "IOException / " + e6222.toString(), e6222);
                            }
                        }
                        throw e82;
                    }
                } catch (IOException e9) {
                    e82 = e9;
                    fileOutputStream = fileOutputStream3;
                    obj = fileOutputStream2;
                    fileOutputStream2 = fileOutputStream;
                    a.d("FileUtil", "IOException / " + e82.toString(), e82);
                    if (fileOutputStream2 != null) {
                        try {
                            fileOutputStream2.close();
                        } catch (Throwable e822) {
                            a.d("FileUtil", "IOException / " + e822.toString(), e822);
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable e8222) {
                            a.d("FileUtil", "IOException / " + e8222.toString(), e8222);
                        }
                    }
                    return "";
                } catch (NullPointerException e10) {
                    e8222 = e10;
                    fileOutputStream = fileOutputStream3;
                    obj = fileOutputStream2;
                    fileOutputStream2 = fileOutputStream;
                    a.d("FileUtil", "NullPointerException / " + e8222.toString(), e8222);
                    if (fileOutputStream2 != null) {
                        try {
                            fileOutputStream2.close();
                        } catch (Throwable e82222) {
                            a.d("FileUtil", "IOException / " + e82222.toString(), e82222);
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable e822222) {
                            a.d("FileUtil", "IOException / " + e822222.toString(), e822222);
                        }
                    }
                    return "";
                } catch (Throwable th3) {
                    e822222 = th3;
                    fileOutputStream = fileOutputStream3;
                    obj = fileOutputStream2;
                    fileOutputStream2 = fileOutputStream;
                    if (fileOutputStream2 != null) {
                        fileOutputStream2.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw e822222;
                }
            } catch (FileNotFoundException e11) {
                e822222 = e11;
                obj = fileOutputStream2;
                a.d("FileUtil", "Can not find the file settings.properties", e822222);
                if (fileOutputStream2 != null) {
                    fileOutputStream2.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return "";
            } catch (IOException e12) {
                e822222 = e12;
                fileInputStream = fileOutputStream2;
                a.d("FileUtil", "IOException / " + e822222.toString(), e822222);
                if (fileOutputStream2 != null) {
                    fileOutputStream2.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return "";
            } catch (NullPointerException e13) {
                e822222 = e13;
                fileInputStream = fileOutputStream2;
                a.d("FileUtil", "NullPointerException / " + e822222.toString(), e822222);
                if (fileOutputStream2 != null) {
                    fileOutputStream2.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return "";
            } catch (Throwable th4) {
                e822222 = th4;
                fileInputStream = fileOutputStream2;
                if (fileOutputStream2 != null) {
                    fileOutputStream2.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw e822222;
            }
        }
        return property;
        return property;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void a(Context context, String str, String str2) {
        FileInputStream openFileInput;
        Throwable th;
        FileOutputStream fileOutputStream;
        FileInputStream fileInputStream = null;
        synchronized (i.class) {
            if (context == null || str2 == null || str == null) {
                a.b("FileUtil", "at least 1 param is null");
                return;
            }
            Properties properties = new Properties();
            try {
                File filesDir = context.getFilesDir();
                if (filesDir != null) {
                    if (filesDir.getPath() != null) {
                        if (new File(filesDir.getPath() + "/" + "settings.properties").exists()) {
                            openFileInput = context.openFileInput("settings.properties");
                            if (openFileInput == null) {
                                a.b("FileUtil", "inStream is null");
                            } else {
                                properties.load(openFileInput);
                            }
                        } else {
                            openFileInput = fileInputStream;
                        }
                        try {
                            OutputStream openFileOutput = context.openFileOutput("settings.properties", 0);
                            for (Object equals : b.a()) {
                                if (str.equals(equals)) {
                                    str2 = e.b(context, str2);
                                    break;
                                }
                            }
                            properties.setProperty(str, str2);
                            FileInputStream fileInputStream2;
                            OutputStream outputStream;
                            if (openFileOutput == null) {
                                a.b("FileUtil", "outStream is null");
                                fileInputStream2 = openFileInput;
                                outputStream = openFileOutput;
                                fileInputStream = fileInputStream2;
                            } else {
                                properties.store(openFileOutput, "accountagent");
                                fileInputStream2 = openFileInput;
                                outputStream = openFileOutput;
                                fileInputStream = fileInputStream2;
                            }
                            if (r0 != null) {
                                try {
                                    r0.close();
                                } catch (IOException e) {
                                    a.d("FileUtil", "setProperties IOException");
                                }
                            }
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e2) {
                                    a.d("FileUtil", "IOException");
                                }
                            }
                        } catch (FileNotFoundException e3) {
                        } catch (IOException e4) {
                        } catch (NullPointerException e5) {
                        } catch (Throwable th2) {
                        }
                    }
                }
                FileOutputStream fileOutputStream2 = fileInputStream;
                if (fileOutputStream2 != null) {
                    fileOutputStream2.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (FileNotFoundException e6) {
                openFileInput = fileInputStream;
                try {
                    a.d("FileUtil", "FileNotFoundException");
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e7) {
                            a.d("FileUtil", "setProperties IOException");
                        }
                    }
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (IOException e8) {
                            a.d("FileUtil", "IOException");
                        }
                    }
                } catch (Throwable th3) {
                    Throwable th4 = th3;
                    Object obj = fileInputStream;
                    fileInputStream = openFileInput;
                    th = th4;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e9) {
                            a.d("FileUtil", "setProperties IOException");
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e10) {
                            a.d("FileUtil", "IOException");
                        }
                    }
                    throw th;
                }
            } catch (IOException e11) {
                openFileInput = fileInputStream;
                a.d("FileUtil", "IOException");
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e12) {
                        a.d("FileUtil", "setProperties IOException");
                    }
                }
                if (openFileInput != null) {
                    try {
                        openFileInput.close();
                    } catch (IOException e13) {
                        a.d("FileUtil", "IOException");
                    }
                }
            } catch (NullPointerException e14) {
                openFileInput = fileInputStream;
                a.d("FileUtil", "NullPointerException");
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e15) {
                        a.d("FileUtil", "setProperties IOException");
                    }
                }
                if (openFileInput != null) {
                    try {
                        openFileInput.close();
                    } catch (IOException e16) {
                        a.d("FileUtil", "IOException");
                    }
                }
            } catch (Throwable th5) {
                th = th5;
                fileOutputStream = fileInputStream;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        }
    }

    public static synchronized void a(Context context, String[] strArr) {
        FileInputStream openFileInput;
        Throwable e;
        FileOutputStream fileOutputStream = null;
        synchronized (i.class) {
            Properties properties = new Properties();
            try {
                if (new File(context.getFilesDir().getPath() + "/" + "settings.properties").exists()) {
                    openFileInput = context.openFileInput("settings.properties");
                    if (openFileInput == null) {
                        a.b("FileUtil", "inStream is null");
                    } else {
                        properties.load(openFileInput);
                    }
                } else {
                    openFileInput = null;
                }
                try {
                    fileOutputStream = context.openFileOutput("settings.properties", 0);
                    if (strArr != null) {
                        if (strArr.length > 0) {
                            for (Object obj : strArr) {
                                if (obj != null) {
                                    properties.remove(obj);
                                }
                            }
                        }
                    }
                    if (fileOutputStream == null) {
                        a.b("FileUtil", "outStream is null");
                    } else {
                        properties.store(fileOutputStream, "accountagent");
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable e2) {
                            a.d("FileUtil", "removeProperties IOException:" + e2.getMessage(), e2);
                        }
                    }
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (Throwable e22) {
                            a.d("FileUtil", "IOException / " + e22.toString(), e22);
                        }
                    }
                } catch (FileNotFoundException e3) {
                    e22 = e3;
                    try {
                        a.d("FileUtil", "removeProperties FileNotFoundException:" + e22.getMessage(), e22);
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (Throwable e222) {
                                a.d("FileUtil", "removeProperties IOException:" + e222.getMessage(), e222);
                            }
                        }
                        if (openFileInput != null) {
                            try {
                                openFileInput.close();
                            } catch (Throwable e2222) {
                                a.d("FileUtil", "IOException / " + e2222.toString(), e2222);
                            }
                        }
                    } catch (Throwable th) {
                        e2222 = th;
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (Throwable e4) {
                                a.d("FileUtil", "removeProperties IOException:" + e4.getMessage(), e4);
                            }
                        }
                        if (openFileInput != null) {
                            try {
                                openFileInput.close();
                            } catch (Throwable e5) {
                                a.d("FileUtil", "IOException / " + e5.toString(), e5);
                            }
                        }
                        throw e2222;
                    }
                } catch (IOException e6) {
                    e2222 = e6;
                    a.d("FileUtil", "removeProperties IOException:" + e2222.getMessage(), e2222);
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable e22222) {
                            a.d("FileUtil", "removeProperties IOException:" + e22222.getMessage(), e22222);
                        }
                    }
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (Throwable e222222) {
                            a.d("FileUtil", "IOException / " + e222222.toString(), e222222);
                        }
                    }
                } catch (NullPointerException e7) {
                    e222222 = e7;
                    a.d("FileUtil", "NullPointerException / " + e222222.toString(), e222222);
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable e2222222) {
                            a.d("FileUtil", "removeProperties IOException:" + e2222222.getMessage(), e2222222);
                        }
                    }
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (Throwable e22222222) {
                            a.d("FileUtil", "IOException / " + e22222222.toString(), e22222222);
                        }
                    }
                } catch (Throwable th2) {
                    e22222222 = th2;
                    a.d("FileUtil", "Throwable / " + e22222222.toString(), e22222222);
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Throwable e222222222) {
                            a.d("FileUtil", "removeProperties IOException:" + e222222222.getMessage(), e222222222);
                        }
                    }
                    if (openFileInput != null) {
                        try {
                            openFileInput.close();
                        } catch (Throwable e2222222222) {
                            a.d("FileUtil", "IOException / " + e2222222222.toString(), e2222222222);
                        }
                    }
                }
            } catch (FileNotFoundException e8) {
                e2222222222 = e8;
                openFileInput = null;
                a.d("FileUtil", "removeProperties FileNotFoundException:" + e2222222222.getMessage(), e2222222222);
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (openFileInput != null) {
                    openFileInput.close();
                }
            } catch (IOException e9) {
                e2222222222 = e9;
                openFileInput = null;
                a.d("FileUtil", "removeProperties IOException:" + e2222222222.getMessage(), e2222222222);
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (openFileInput != null) {
                    openFileInput.close();
                }
            } catch (NullPointerException e10) {
                e2222222222 = e10;
                openFileInput = null;
                a.d("FileUtil", "NullPointerException / " + e2222222222.toString(), e2222222222);
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (openFileInput != null) {
                    openFileInput.close();
                }
            } catch (Throwable th3) {
                e2222222222 = th3;
                openFileInput = null;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (openFileInput != null) {
                    openFileInput.close();
                }
                throw e2222222222;
            }
        }
    }

    public static String c(Context context, String str) {
        String e = d.e(context);
        String f = d.f(context);
        String str2 = "policy";
        str2 = "terms";
        str2 = "policyAndTerms";
        str2 = "vipAgreement";
        StringBuffer stringBuffer = new StringBuffer();
        if ("2".equals(str)) {
            stringBuffer.append("policy");
        } else if ("0".equals(str)) {
            stringBuffer.append("terms");
        } else if ("6".equals(str)) {
            stringBuffer.append("vipAgreement");
        } else {
            stringBuffer.append("policyAndTerms");
        }
        stringBuffer.append("-");
        stringBuffer.append(e);
        stringBuffer.append("-");
        stringBuffer.append(f);
        return stringBuffer.toString();
    }

    public static void d(Context context, String str) {
        AccountManager.get(context).setUserData(new Account(str, "com.huawei.hwid"), "fingerprintBindType", "0");
        a(context, new String[]{"bindFingetUserId"});
    }

    public static void e(Context context, String str) {
        AccountManager.get(context).setUserData(new Account(str, "com.huawei.hwid"), "fingerprintBindType", "1");
    }

    public static void f(Context context, String str) {
        AccountManager.get(context).setUserData(new Account(str, "com.huawei.hwid"), "verifyTimes", "0");
    }

    public static void a(XmlSerializer xmlSerializer, String str, String str2) {
        try {
            xmlSerializer.startTag("", str);
            xmlSerializer.text(str2);
            xmlSerializer.endTag("", str);
        } catch (Throwable e) {
            a.d("FileUtil", "IllegalArgumentException / " + e.toString(), e);
        } catch (Throwable e2) {
            a.d("FileUtil", "IllegalStateException / " + e2.toString(), e2);
        } catch (Throwable e22) {
            a.d("FileUtil", "IOException / " + e22.toString(), e22);
        } catch (Throwable e222) {
            a.d("FileUtil", "NullPointerException / " + e222.toString(), e222);
        }
    }
}
