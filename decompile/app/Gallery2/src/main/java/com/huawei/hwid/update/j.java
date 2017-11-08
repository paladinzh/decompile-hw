package com.huawei.hwid.update;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.text.TextUtils;
import com.huawei.hwid.core.d.b.e;
import com.huawei.hwid.update.a.b;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public final class j {
    public static String a(String str, Map<Integer, b> map) {
        String str2 = "1";
        try {
            String str3 = "";
            JSONObject jSONObject = new JSONObject(str);
            str2 = jSONObject.getString("status");
            if (jSONObject.has("forcedupdate")) {
                str3 = jSONObject.getString("forcedupdate");
            }
            if ("0".equals(str2)) {
                JSONArray jSONArray = jSONObject.getJSONArray("components");
                int length = jSONArray.length();
                e.b("OtaUtils", "length:" + length);
                for (int i = 0; i < length; i++) {
                    b bVar = new b();
                    JSONObject jSONObject2 = jSONArray.getJSONObject(i);
                    String string = jSONObject2.getString("url");
                    int i2 = jSONObject2.getInt("componentID");
                    bVar.b(i2);
                    bVar.b(jSONObject2.getString("name"));
                    bVar.c(jSONObject2.getString("version"));
                    bVar.a(jSONObject2.getString("versionID"));
                    bVar.d(string);
                    bVar.a(jSONObject2.getInt("size"));
                    bVar.h(jSONObject2.getString("createTime"));
                    bVar.g(jSONObject2.getString("description"));
                    bVar.f(str3);
                    map.put(Integer.valueOf(i2), bVar);
                }
            }
        } catch (JSONException e) {
            e.d("OtaUtils", "parse version to map error ,error is " + e.getMessage());
        }
        return str2;
    }

    public static String a(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        String language = configuration.locale.getLanguage();
        return (language + '-' + configuration.locale.getCountry()).toLowerCase(Locale.getDefault());
    }

    public static boolean a(Context context, long j) {
        boolean z;
        if (context.getExternalCacheDir().getUsableSpace() >= 3 * j) {
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    public static void a(InputStream inputStream, String str) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                if (TextUtils.isEmpty(str)) {
                    e.d("OtaUtils", e.getMessage());
                } else {
                    e.d(str, e.getMessage());
                }
            }
        }
    }

    public static InputStream a(InputStream inputStream) throws IOException {
        InputStream pushbackInputStream = new PushbackInputStream(inputStream);
        int read = pushbackInputStream.read();
        if (read == 239) {
            read = pushbackInputStream.read();
            if (read != SmsCheckResult.ESCT_187) {
                pushbackInputStream.unread(read);
                pushbackInputStream.unread(239);
                return pushbackInputStream;
            } else if (pushbackInputStream.read() == SmsCheckResult.ESCT_191) {
                return pushbackInputStream;
            } else {
                throw new IOException("UTF-8 file error");
            }
        }
        pushbackInputStream.unread(read);
        return pushbackInputStream;
    }

    public static void b(InputStream inputStream, String str) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
                if (TextUtils.isEmpty(str)) {
                    a(e, "OtaUtils");
                } else {
                    a(e, str);
                }
            }
        }
    }

    public static void a(OutputStream outputStream, String str) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Exception e) {
                if (TextUtils.isEmpty(str)) {
                    a(e, "OtaUtils");
                } else {
                    a(e, str);
                }
            }
        }
    }

    public static void a(Exception exception, String str) {
        if (exception != null && exception.getMessage() != null) {
            e.d(str, exception.getMessage());
        }
    }

    public static String a(String str) {
        if (str == null) {
            return "";
        }
        char[] toCharArray = str.toCharArray();
        int length = toCharArray.length;
        int i = 4;
        while (i < length && i <= 10) {
            toCharArray[i] = '0';
            i++;
        }
        return new String(toCharArray);
    }

    @TargetApi(19)
    public static boolean b(Context context) {
        List list = null;
        boolean z = false;
        try {
            PackageManager packageManager = context.getPackageManager();
            Intent intent = new Intent();
            String packageName = context.getPackageName();
            intent.setClassName(packageName, "com.huawei.hwid.update.OtaFileProvider");
            intent.setPackage(packageName);
            if (packageManager != null) {
                list = packageManager.queryIntentContentProviders(intent, 0);
            }
            if (list == null) {
                e.b("OtaUtils", "null == resolveInfos");
                return false;
            }
            if (!list.isEmpty()) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            e.d("OtaUtils", "hasOtaFileProvider error:" + e.getMessage());
            return false;
        }
    }
}
