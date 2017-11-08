package com.huawei.hwid.ui.common;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Xml;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.encrypt.e;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

/* compiled from: HistoryAccount */
public class h {
    private static final String[] a = new String[]{"ar", "fa", "iw"};
    private static final List b = new ArrayList();

    static {
        a();
    }

    private static void a() {
        if (a != null) {
            for (Object add : a) {
                b.add(add);
            }
        }
        a.a("HistoryAccount", "languageList size is " + b.size());
    }

    public static void a(Context context, Handler handler) {
        ArrayList b = b(context, "historyAccounts.xml");
        ArrayList arrayList = new ArrayList();
        if (b.isEmpty()) {
            a.b("HistoryAccount", "there is not has logined account");
        } else {
            Iterator it = b.iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                if (!TextUtils.isEmpty(str)) {
                    arrayList.add(str);
                }
            }
        }
        Message message = new Message();
        message.what = 100;
        message.obj = arrayList;
        handler.sendMessage(message);
    }

    public static void a(Context context, EditText editText, LinearLayout linearLayout) {
        float f = context.getResources().getDisplayMetrics().density;
        editText.setPadding(((int) f) * 13, 0, ((int) f) * 13, 0);
        linearLayout.setVisibility(8);
    }

    public static void b(Context context, EditText editText, LinearLayout linearLayout) {
        float f = context.getResources().getDisplayMetrics().density;
        if (a(context)) {
            editText.setPadding(((int) f) * 50, 0, ((int) f) * 13, 0);
        } else {
            editText.setPadding(((int) f) * 13, 0, ((int) f) * 50, 0);
        }
        linearLayout.setVisibility(0);
    }

    private static boolean a(Context context) {
        if (b.contains(d.e(context))) {
            return true;
        }
        return false;
    }

    public static void a(Context context, String str) {
        if (context != null && !TextUtils.isEmpty(str)) {
            context.deleteFile(str);
        }
    }

    public static ArrayList b(Context context, String str) {
        InputStream fileInputStream;
        Exception e;
        Throwable th;
        if (context == null || TextUtils.isEmpty(str)) {
            return new ArrayList();
        }
        ArrayList arrayList = new ArrayList();
        try {
            String str2 = context.getFilesDir().getAbsolutePath() + "/" + str;
            if (new File(str2).exists()) {
                fileInputStream = new FileInputStream(str2);
                try {
                    XmlPullParser newPullParser = Xml.newPullParser();
                    if (newPullParser != null) {
                        newPullParser.setInput(fileInputStream, null);
                        Object obj = "";
                        for (int eventType = newPullParser.getEventType(); 1 != eventType; eventType = newPullParser.next()) {
                            String name = newPullParser.getName();
                            switch (eventType) {
                                case 2:
                                    if (!"historyName".equals(name)) {
                                        break;
                                    }
                                    obj = newPullParser.nextText();
                                    break;
                                case 3:
                                    if (!TextUtils.isEmpty(obj)) {
                                        arrayList.add(e.c(context, obj));
                                    }
                                    obj = "";
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e2) {
                                a.d("HistoryAccount", "IOException / " + e2.getMessage());
                            }
                        }
                        return arrayList;
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e22) {
                            a.d("HistoryAccount", "IOException / " + e22.getMessage());
                        }
                    }
                    return arrayList;
                } catch (Exception e3) {
                    e = e3;
                }
            } else {
                a.b("HistoryAccount", "historyFile not exist !");
                return new ArrayList();
            }
        } catch (Exception e4) {
            e = e4;
            fileInputStream = null;
            try {
                a.d("HistoryAccount", "getHistoryAccountFromXML error:" + e.getMessage());
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e222) {
                        a.d("HistoryAccount", "IOException / " + e222.getMessage());
                    }
                }
                return arrayList;
            } catch (Throwable th2) {
                th = th2;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e5) {
                        a.d("HistoryAccount", "IOException / " + e5.getMessage());
                    }
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            fileInputStream = null;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            throw th;
        }
    }

    public static boolean a(Context context, boolean z, boolean z2) {
        if (!"com.huawei.hwid".equals(context.getPackageName())) {
            a.b("HistoryAccount", "SDK doesn't use history account");
            return false;
        } else if (z) {
            a.b("HistoryAccount", "it is come from AccountManagerActivity");
            return false;
        } else if (z2) {
            return true;
        } else {
            a.b("HistoryAccount", "it is not need to show history choose");
            return false;
        }
    }
}
