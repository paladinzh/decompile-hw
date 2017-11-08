package com.huawei.hwid.manager.b;

import android.content.Context;
import android.util.Xml;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.i;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.encrypt.e;
import com.huawei.hwid.core.encrypt.f;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: SDKAccountXmlImpl */
public class b {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void a(Context context, String str, List list, boolean z) {
        List a = a(list);
        if (a != null && !a.isEmpty() && list != null && !list.isEmpty()) {
            XmlSerializer newSerializer = Xml.newSerializer();
            Writer stringWriter = new StringWriter();
            try {
                newSerializer.setOutput(stringWriter);
                newSerializer.startDocument("UTF-8", Boolean.valueOf(true));
                newSerializer.startTag("", "accounts");
                newSerializer.attribute("", "size", list.size() + "");
                for (HwAccount hwAccount : list) {
                    if (hwAccount != null) {
                        newSerializer.startTag("", "account");
                        newSerializer.attribute("", "appId", hwAccount.b());
                        String a2 = hwAccount.a();
                        if (z) {
                            a2 = e.b(context, a2);
                        }
                        i.a(newSerializer, "accountName", a2);
                        a2 = hwAccount.c();
                        if (z) {
                            a2 = e.b(context, a2);
                        }
                        i.a(newSerializer, "userId", a2);
                        a2 = hwAccount.h();
                        if (z) {
                            a2 = e.b(context, a2);
                        }
                        String str2 = "deviceId";
                        if (a2 == null) {
                            a2 = "";
                        }
                        i.a(newSerializer, str2, a2);
                        a2 = hwAccount.i();
                        str2 = "deviceType";
                        if (a2 == null) {
                            a2 = "";
                        }
                        i.a(newSerializer, str2, a2);
                        i.a(newSerializer, "serviceToken", e.b(context, hwAccount.f()));
                        a2 = hwAccount.e();
                        if (z) {
                            a2 = e.b(context, a2);
                        }
                        str2 = "Cookie";
                        if (a2 == null) {
                            a2 = "";
                        }
                        i.a(newSerializer, str2, a2);
                        i.a(newSerializer, "siteId", hwAccount.d() + "");
                        String g = hwAccount.g();
                        a2 = "accountType";
                        if (g == null) {
                            g = "";
                        }
                        i.a(newSerializer, a2, g);
                        newSerializer.endTag("", "account");
                    }
                }
                newSerializer.endTag("", "accounts");
                newSerializer.endDocument();
                a.a("SDKAccountXmlImpl", "write accounts into file " + str + ": " + i.a(context.getFilesDir().getAbsolutePath() + "/", str, d.e(stringWriter.toString())));
                try {
                    stringWriter.close();
                } catch (IOException e) {
                    a.d("SDKAccountXmlImpl", "IOException / " + e.toString());
                }
            } catch (IllegalArgumentException e2) {
                a.d("SDKAccountXmlImpl", "write accounts failed!" + e2.getMessage());
            } catch (IllegalStateException e3) {
                a.d("SDKAccountXmlImpl", "write accounts failed!" + e3.getMessage());
                try {
                    stringWriter.close();
                } catch (IOException e4) {
                    a.d("SDKAccountXmlImpl", "IOException / " + e4.toString());
                }
            } catch (IOException e42) {
                a.d("SDKAccountXmlImpl", "write accounts failed!" + e42.getMessage());
                try {
                    stringWriter.close();
                } catch (IOException e422) {
                    a.d("SDKAccountXmlImpl", "IOException / " + e422.toString());
                }
            } catch (Exception e5) {
                a.d("SDKAccountXmlImpl", "write accounts failed!" + e5.getMessage());
                try {
                    stringWriter.close();
                } catch (IOException e4222) {
                    a.d("SDKAccountXmlImpl", "IOException / " + e4222.toString());
                }
            } catch (Throwable th) {
                try {
                    stringWriter.close();
                } catch (IOException e6) {
                    a.d("SDKAccountXmlImpl", "IOException / " + e6.toString());
                }
            }
        }
    }

    public static ArrayList a(String str, Context context, boolean z) {
        InputStream fileInputStream;
        ArrayList a;
        Throwable e;
        InputStream inputStream = null;
        try {
            File file = new File(context.getFilesDir().getAbsolutePath() + "/" + str);
            if (file.exists()) {
                fileInputStream = new FileInputStream(file);
                try {
                    XmlPullParser newPullParser = Xml.newPullParser();
                    newPullParser.setInput(fileInputStream, null);
                    a = a(newPullParser, context, z);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable e2) {
                            a.d("SDKAccountXmlImpl", "IOException / " + e2.toString(), e2);
                        }
                    }
                    return a;
                } catch (XmlPullParserException e3) {
                    e = e3;
                    try {
                        a.d("SDKAccountXmlImpl", "read xml failed!" + e.getMessage(), e);
                        a = new ArrayList();
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Throwable e22) {
                                a.d("SDKAccountXmlImpl", "IOException / " + e22.toString(), e22);
                            }
                        }
                        return a;
                    } catch (Throwable th) {
                        e = th;
                        inputStream = fileInputStream;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable e222) {
                                a.d("SDKAccountXmlImpl", "IOException / " + e222.toString(), e222);
                            }
                        }
                        throw e;
                    }
                } catch (FileNotFoundException e4) {
                    e = e4;
                    inputStream = fileInputStream;
                    try {
                        a.d("SDKAccountXmlImpl", "read xml failed!" + e.getMessage(), e);
                        a = new ArrayList();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable e2222) {
                                a.d("SDKAccountXmlImpl", "IOException / " + e2222.toString(), e2222);
                            }
                        }
                        return a;
                    } catch (Throwable th2) {
                        e = th2;
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        throw e;
                    }
                } catch (IOException e5) {
                    e = e5;
                    inputStream = fileInputStream;
                    a.d("SDKAccountXmlImpl", "read xml failed!" + e.getMessage(), e);
                    a = new ArrayList();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable e22222) {
                            a.d("SDKAccountXmlImpl", "IOException / " + e22222.toString(), e22222);
                        }
                    }
                    return a;
                } catch (Exception e6) {
                    e = e6;
                    inputStream = fileInputStream;
                    a.d("SDKAccountXmlImpl", "read xml failed!" + e.getMessage(), e);
                    a = new ArrayList();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable e222222) {
                            a.d("SDKAccountXmlImpl", "IOException / " + e222222.toString(), e222222);
                        }
                    }
                    return a;
                }
            }
            a.a("SDKAccountXmlImpl", " sdk filepath not exist");
            return new ArrayList();
        } catch (XmlPullParserException e7) {
            e = e7;
            fileInputStream = null;
            a.d("SDKAccountXmlImpl", "read xml failed!" + e.getMessage(), e);
            a = new ArrayList();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return a;
        } catch (FileNotFoundException e8) {
            e = e8;
            a.d("SDKAccountXmlImpl", "read xml failed!" + e.getMessage(), e);
            a = new ArrayList();
            if (inputStream != null) {
                inputStream.close();
            }
            return a;
        } catch (IOException e9) {
            e = e9;
            a.d("SDKAccountXmlImpl", "read xml failed!" + e.getMessage(), e);
            a = new ArrayList();
            if (inputStream != null) {
                inputStream.close();
            }
            return a;
        } catch (Exception e10) {
            e = e10;
            a.d("SDKAccountXmlImpl", "read xml failed!" + e.getMessage(), e);
            a = new ArrayList();
            if (inputStream != null) {
                inputStream.close();
            }
            return a;
        }
    }

    private static ArrayList a(XmlPullParser xmlPullParser, Context context, boolean z) throws XmlPullParserException, IOException {
        a.e("SDKAccountXmlImpl", "parseAccountsFromXml Start");
        int eventType = xmlPullParser.getEventType();
        ArrayList arrayList = new ArrayList();
        HwAccount hwAccount = new HwAccount();
        while (1 != eventType) {
            HwAccount hwAccount2;
            String name = xmlPullParser.getName();
            switch (eventType) {
                case 0:
                    hwAccount2 = hwAccount;
                    break;
                case 2:
                    String nextText;
                    if (!"account".equals(name)) {
                        if (!"accountName".equals(name)) {
                            if (!"userId".equals(name)) {
                                if (!"deviceId".equals(name)) {
                                    if (!"deviceType".equals(name)) {
                                        if (!"serviceToken".equals(name)) {
                                            if (!"Cookie".equals(name)) {
                                                if (!"siteId".equals(name)) {
                                                    if (!"accountType".equals(name)) {
                                                        hwAccount2 = hwAccount;
                                                        break;
                                                    }
                                                    hwAccount.f(xmlPullParser.nextText());
                                                    hwAccount2 = hwAccount;
                                                    break;
                                                }
                                                try {
                                                    eventType = Integer.parseInt(xmlPullParser.nextText());
                                                    hwAccount.a(eventType);
                                                    a.e("SDKAccountXmlImpl", "read mSiteId: " + eventType);
                                                } catch (NumberFormatException e) {
                                                    a.c("SDKAccountXmlImpl", "NumberFormatException: read accounts.xml parseInt error");
                                                } catch (Exception e2) {
                                                    a.c("SDKAccountXmlImpl", "read accounts.xml parseInt error");
                                                }
                                                hwAccount2 = hwAccount;
                                                break;
                                            }
                                            nextText = xmlPullParser.nextText();
                                            if (z) {
                                                nextText = e.c(context, nextText);
                                            }
                                            hwAccount.d(nextText);
                                            hwAccount2 = hwAccount;
                                            break;
                                        }
                                        hwAccount.e(e.c(context, xmlPullParser.nextText()));
                                        hwAccount2 = hwAccount;
                                        break;
                                    }
                                    nextText = xmlPullParser.nextText();
                                    hwAccount.h(nextText);
                                    a.e("SDKAccountXmlImpl", "read mDeviceType: " + nextText);
                                    hwAccount2 = hwAccount;
                                    break;
                                }
                                nextText = xmlPullParser.nextText();
                                if (z) {
                                    nextText = e.c(context, nextText);
                                }
                                hwAccount.g(nextText);
                                a.e("SDKAccountXmlImpl", "read mDeviceId: " + f.a(nextText));
                                hwAccount2 = hwAccount;
                                break;
                            }
                            nextText = xmlPullParser.nextText();
                            if (z) {
                                nextText = e.c(context, nextText);
                            }
                            hwAccount.c(nextText);
                            a.e("SDKAccountXmlImpl", "read mUserId: " + f.a(nextText));
                            hwAccount2 = hwAccount;
                            break;
                        }
                        nextText = xmlPullParser.nextText();
                        if (z) {
                            nextText = e.c(context, nextText);
                        }
                        hwAccount.a(nextText);
                        a.e("SDKAccountXmlImpl", "read mUserName: " + f.c(nextText));
                        hwAccount2 = hwAccount;
                        break;
                    }
                    nextText = xmlPullParser.getAttributeValue("", "appId");
                    if (p.e(nextText)) {
                        a.e("SDKAccountXmlImpl", "authTokenType is null");
                        nextText = d.d(context);
                        hwAccount.d(i.b(context, "Cookie"));
                    }
                    hwAccount.b(nextText);
                    a.e("SDKAccountXmlImpl", "read authTokenType: " + nextText);
                    hwAccount2 = hwAccount;
                    break;
                case 3:
                    if (!"account".equals(name)) {
                        hwAccount2 = hwAccount;
                        break;
                    }
                    a.e("SDKAccountXmlImpl", "parseAccountsFromXml add account");
                    arrayList.add(hwAccount);
                    hwAccount2 = new HwAccount();
                    break;
                default:
                    hwAccount2 = hwAccount;
                    break;
            }
            HwAccount hwAccount3 = hwAccount2;
            eventType = xmlPullParser.next();
            hwAccount = hwAccount3;
        }
        return arrayList;
    }

    private static List a(List list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Object arrayList = new ArrayList();
        for (HwAccount hwAccount : list) {
            if (!d.a(hwAccount)) {
                arrayList.add(hwAccount);
            }
        }
        try {
            if (!arrayList.isEmpty()) {
                if (list.containsAll(arrayList)) {
                    list.removeAll(arrayList);
                }
            }
        } catch (Exception e) {
            a.d("SDKAccountXmlImpl", e.toString());
        }
        return list;
    }
}
