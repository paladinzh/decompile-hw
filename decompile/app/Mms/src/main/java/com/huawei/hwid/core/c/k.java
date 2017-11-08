package com.huawei.hwid.core.c;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Xml;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.datatype.SMSCountryInfo;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* compiled from: IpCountryUtil */
public class k {
    private static CharSequence[] a = null;
    private static CharSequence[] b = null;
    private static HashMap c = new HashMap();
    private static ArrayList d = new ArrayList();

    public static synchronized void a(Context context) {
        synchronized (k.class) {
            int i;
            if (a != null) {
                if (!(a.length == 0 || b == null || b.length == 0)) {
                    return;
                }
            }
            a = context.getResources().getTextArray(m.c(context, "CS_country"));
            b = context.getResources().getTextArray(m.c(context, "CS_national_code"));
            c = new HashMap();
            for (i = 0; i < a.length; i++) {
                c.put(b[i], a[i]);
            }
            Arrays.sort(b);
            int length = b.length;
            a = new CharSequence[length];
            for (i = 0; i < length; i++) {
                a[i] = (CharSequence) c.get(b[i]);
            }
        }
    }

    private static ArrayList a(String str, Context context) {
        InputStream fileInputStream;
        Throwable th;
        InputStream inputStream = null;
        ArrayList arrayList = new ArrayList();
        try {
            fileInputStream = new FileInputStream((context.getFilesDir().getAbsolutePath() + "/") + str);
            try {
                XmlPullParser newPullParser = Xml.newPullParser();
                newPullParser.setInput(fileInputStream, null);
                ArrayList a = a(newPullParser, context);
                if (fileInputStream == null) {
                    return a;
                }
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    a.d("IpCountryUtil", "IOException");
                }
                return a;
            } catch (FileNotFoundException e2) {
                try {
                    a.c("IpCountryUtil", "get SMS country failed form local");
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e3) {
                            a.d("IpCountryUtil", "IOException");
                        }
                        return arrayList;
                    }
                    return arrayList;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    inputStream = fileInputStream;
                    th = th3;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e4) {
                            a.d("IpCountryUtil", "IOException");
                        }
                    }
                    throw th;
                }
            } catch (XmlPullParserException e5) {
                inputStream = fileInputStream;
                try {
                    a.c("IpCountryUtil", "get SMS country failed form local");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e6) {
                            a.d("IpCountryUtil", "IOException");
                        }
                        return arrayList;
                    }
                    return arrayList;
                } catch (Throwable th4) {
                    th = th4;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    throw th;
                }
            } catch (IOException e7) {
                inputStream = fileInputStream;
                a.c("IpCountryUtil", "get SMS country failed form local");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e8) {
                        a.d("IpCountryUtil", "IOException");
                    }
                    return arrayList;
                }
                return arrayList;
            } catch (Exception e9) {
                inputStream = fileInputStream;
                a.c("IpCountryUtil", "get SMS country failed form local");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e10) {
                        a.d("IpCountryUtil", "IOException");
                    }
                    return arrayList;
                }
                return arrayList;
            }
        } catch (FileNotFoundException e11) {
            fileInputStream = null;
            a.c("IpCountryUtil", "get SMS country failed form local");
            if (fileInputStream != null) {
                fileInputStream.close();
                return arrayList;
            }
            return arrayList;
        } catch (XmlPullParserException e12) {
            a.c("IpCountryUtil", "get SMS country failed form local");
            if (inputStream != null) {
                inputStream.close();
                return arrayList;
            }
            return arrayList;
        } catch (IOException e13) {
            a.c("IpCountryUtil", "get SMS country failed form local");
            if (inputStream != null) {
                inputStream.close();
                return arrayList;
            }
            return arrayList;
        } catch (Exception e14) {
            a.c("IpCountryUtil", "get SMS country failed form local");
            if (inputStream != null) {
                inputStream.close();
                return arrayList;
            }
            return arrayList;
        }
    }

    public static String a(Context context, String str) {
        String d;
        if (c.isEmpty()) {
            a(context);
        }
        if (c.containsKey(str)) {
            CharSequence charSequence = (CharSequence) c.get(str);
        } else {
            d = d(context, str);
        }
        return d;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean a(Context context, String str, String str2) {
        FileOutputStream fileOutputStream = null;
        try {
            context.deleteFile(str);
            fileOutputStream = context.openFileOutput(str, 0);
            fileOutputStream.write(str2.getBytes("UTF-8"));
            fileOutputStream.close();
            a(new ArrayList());
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    a.d("IpCountryUtil", "IOException");
                }
            }
            return true;
        } catch (FileNotFoundException e2) {
            a.d("IpCountryUtil", "writeSMSAvailableCountryXML FileNotFoundException");
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e3) {
                    a.d("IpCountryUtil", "IOException");
                }
            }
            return false;
        } catch (IOException e4) {
            a.d("IpCountryUtil", "writeSMSAvailableCountryXML IOException");
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e5) {
                    a.d("IpCountryUtil", "IOException");
                }
            }
            return false;
        } catch (Throwable th) {
            Throwable th2 = th;
            FileOutputStream fileOutputStream2 = fileOutputStream;
            Throwable th3 = th2;
            if (fileOutputStream2 != null) {
                try {
                    fileOutputStream2.close();
                } catch (IOException e6) {
                    a.d("IpCountryUtil", "IOException");
                }
            }
            throw th3;
        }
    }

    public static synchronized void a(ArrayList arrayList) {
        synchronized (k.class) {
            d = arrayList;
        }
    }

    public static ArrayList b(Context context) {
        if (d.isEmpty()) {
            try {
                a(a("countryInfolist.xml", context));
            } catch (Throwable e) {
                a.d("IpCountryUtil", "get smscountrylist from server failed", e);
            }
            if (!d.isEmpty()) {
                a.b("IpCountryUtil", "countryInfoList in smscountrylist");
                return d;
            }
        }
        if (d.isEmpty()) {
            h(context);
        }
        return d;
    }

    public static void b(Context context, String str) {
        com.huawei.hwid.core.b.a.a(context).b("ip_countryCallingCode", str);
    }

    public static void c(Context context, String str) {
        com.huawei.hwid.core.b.a.a(context).b("ip_countryEnglishName", str);
    }

    public static void a(Context context, int i) {
        com.huawei.hwid.core.b.a.a(context).a("ip_countrySiteID", i);
    }

    public static String c(Context context) {
        return com.huawei.hwid.core.b.a.a(context).a("ip_countryEnglishName", "");
    }

    public static String d(Context context) {
        String a = com.huawei.hwid.core.b.a.a(context).a("ip_countryCallingCode", "");
        a.b("IpCountryUtil", "getIpCountryCallingCode = " + a);
        return a;
    }

    public static boolean e(Context context) {
        String d = d(context);
        String c = c(context);
        if (!StringUtils.MPLUG86.equalsIgnoreCase(d) && !"CHINA".equalsIgnoreCase(c)) {
            return false;
        }
        a.a("IpCountryUtil", "ChinaUser");
        return true;
    }

    public static boolean f(Context context) {
        String d = d(context);
        a.b("IpCountryUtil", "ipCountryCallingCode =" + d);
        ArrayList b = b(context);
        if (!b.isEmpty()) {
            Iterator it = b.iterator();
            while (it.hasNext()) {
                if (((SMSCountryInfo) it.next()).a().equals(d)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String g(Context context) {
        return com.huawei.hwid.core.b.a.a(context).a("countryCallingCode", "");
    }

    private static ArrayList a(XmlPullParser xmlPullParser, Context context) throws XmlPullParserException, IOException {
        ArrayList arrayList = new ArrayList();
        SMSCountryInfo sMSCountryInfo = new SMSCountryInfo();
        Object obj = null;
        SMSCountryInfo sMSCountryInfo2 = sMSCountryInfo;
        int eventType = xmlPullParser.getEventType();
        while (1 != eventType) {
            String name = xmlPullParser.getName();
            switch (eventType) {
                case 2:
                    if (!"CountryInfo".equals(name)) {
                        if (obj == null) {
                            break;
                        }
                        com.huawei.hwid.core.helper.a.a.a(xmlPullParser, sMSCountryInfo2, name, context);
                        break;
                    }
                    int i = 1;
                    break;
                case 3:
                    if ("CountryInfo".equals(name)) {
                        arrayList.add(sMSCountryInfo2);
                    }
                    sMSCountryInfo2 = new SMSCountryInfo();
                    break;
                default:
                    break;
            }
            eventType = xmlPullParser.next();
        }
        return arrayList;
    }

    private static void h(Context context) {
        SMSCountryInfo sMSCountryInfo = new SMSCountryInfo();
        sMSCountryInfo.a(StringUtils.MPLUG86);
        sMSCountryInfo.b("cn");
        sMSCountryInfo.c("中国  +86");
        d.add(sMSCountryInfo);
        a.b("IpCountryUtil", "get smscountrylist from defalut");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String d(Context context, String str) {
        XmlPullParser xml = context.getResources().getXml(m.b(context, "national_code"));
        if (xml == null) {
            return null;
        }
        while (true) {
            try {
                int next = xml.next();
                if (next == 1) {
                    break;
                } else if (next == 2) {
                    if (str.equalsIgnoreCase(xml.getName())) {
                        break;
                    }
                }
            } catch (Throwable e) {
                a.d("IpCountryUtil", "Got XmlPullParserException parsing countries " + e.toString(), e);
                if (xml instanceof XmlResourceParser) {
                    ((XmlResourceParser) xml).close();
                }
            } catch (Throwable e2) {
                a.d("IpCountryUtil", "Got IOException  parsing countries " + e2.toString(), e2);
                if (xml instanceof XmlResourceParser) {
                    ((XmlResourceParser) xml).close();
                }
            } catch (Throwable th) {
                if (xml instanceof XmlResourceParser) {
                    ((XmlResourceParser) xml).close();
                }
            }
        }
        String nextText = xml.nextText();
        if (xml instanceof XmlResourceParser) {
            ((XmlResourceParser) xml).close();
        }
        return nextText;
    }
}
