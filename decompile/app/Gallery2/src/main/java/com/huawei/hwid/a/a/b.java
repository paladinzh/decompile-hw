package com.huawei.hwid.a.a;

import android.content.Context;
import android.text.TextUtils;
import android.util.Xml;
import com.android.gallery3d.gadget.XmlUtils;
import com.huawei.hwid.core.d.f;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.encrypt.e;
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

public class b {
    private static void a(Context context, HwAccount hwAccount, boolean z, XmlSerializer xmlSerializer) throws IllegalStateException, IllegalArgumentException, IOException {
        if (hwAccount != null) {
            xmlSerializer.startTag("", "account");
            xmlSerializer.attribute("", "appId", hwAccount.c());
            String b = hwAccount.b();
            if (z) {
                b = e.b(context, b);
            }
            f.a(xmlSerializer, "accountName", b);
            b = hwAccount.d();
            if (z) {
                b = e.b(context, b);
            }
            f.a(xmlSerializer, "userId", b);
            b = hwAccount.i();
            if (z) {
                b = e.b(context, b);
            }
            String str = "deviceId";
            if (b == null) {
                b = "";
            }
            f.a(xmlSerializer, str, b);
            b = hwAccount.j();
            str = "deviceType";
            if (b == null) {
                b = "";
            }
            f.a(xmlSerializer, str, b);
            f.a(xmlSerializer, "serviceToken", e.b(context, hwAccount.g()));
            b = hwAccount.f();
            if (z) {
                b = e.b(context, b);
            }
            str = "Cookie";
            if (b == null) {
                b = "";
            }
            f.a(xmlSerializer, str, b);
            f.a(xmlSerializer, "siteId", hwAccount.e() + "");
            b = hwAccount.h();
            str = "accountType";
            if (b == null) {
                b = "";
            }
            f.a(xmlSerializer, str, b);
            b = hwAccount.m();
            if (z) {
                b = e.b(context, b);
            }
            str = "loginUserName";
            if (b == null) {
                b = "";
            }
            f.a(xmlSerializer, str, b);
            b = hwAccount.a();
            if (z) {
                b = e.b(context, b);
            }
            str = "countryIsoCode";
            if (b == null) {
                b = "";
            }
            f.a(xmlSerializer, str, b);
            xmlSerializer.endTag("", "account");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void a(Context context, String str, List<HwAccount> list, boolean z) {
        List a = a(list);
        if (a != null && !a.isEmpty() && list != null && !list.isEmpty()) {
            XmlSerializer newSerializer = Xml.newSerializer();
            Writer stringWriter = new StringWriter();
            try {
                newSerializer.setOutput(stringWriter);
                newSerializer.startDocument(XmlUtils.INPUT_ENCODING, Boolean.valueOf(true));
                newSerializer.startTag("", "accounts");
                newSerializer.attribute("", "size", list.size() + "");
                for (HwAccount a2 : list) {
                    a(context, a2, z, newSerializer);
                }
                newSerializer.endTag("", "accounts");
                newSerializer.endDocument();
                com.huawei.hwid.core.d.b.e.a("SDKAccountXmlImpl", "write accounts into file " + str + ": " + f.a(context.getFilesDir().getAbsolutePath() + "/", str, com.huawei.hwid.core.d.b.c(stringWriter.toString())));
                try {
                    stringWriter.close();
                } catch (IOException e) {
                    com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "IOException / " + e.getMessage());
                }
            } catch (IllegalArgumentException e2) {
                com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "write accounts failed!" + e2.getMessage());
            } catch (IllegalStateException e3) {
                com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "write accounts failed!" + e3.getMessage());
                try {
                    stringWriter.close();
                } catch (IOException e4) {
                    com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "IOException / " + e4.getMessage());
                }
            } catch (IOException e42) {
                com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "write accounts failed!" + e42.getMessage());
                try {
                    stringWriter.close();
                } catch (IOException e422) {
                    com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "IOException / " + e422.getMessage());
                }
            } catch (Exception e5) {
                com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "write accounts failed!" + e5.getMessage());
                try {
                    stringWriter.close();
                } catch (IOException e4222) {
                    com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "IOException / " + e4222.getMessage());
                }
            } catch (Throwable th) {
                try {
                    stringWriter.close();
                } catch (IOException e6) {
                    com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "IOException / " + e6.getMessage());
                }
            }
        }
    }

    public static ArrayList<HwAccount> a(String str, Context context, boolean z) {
        InputStream fileInputStream;
        ArrayList<HwAccount> a;
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
                            com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "IOException / " + e2.getMessage(), e2);
                        }
                    }
                    return a;
                } catch (XmlPullParserException e3) {
                    e = e3;
                    try {
                        com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "read xml failed!" + e.getMessage(), e);
                        a = new ArrayList();
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Throwable e22) {
                                com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "IOException / " + e22.getMessage(), e22);
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
                                com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "IOException / " + e222.getMessage(), e222);
                            }
                        }
                        throw e;
                    }
                } catch (FileNotFoundException e4) {
                    e = e4;
                    inputStream = fileInputStream;
                    try {
                        com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "read xml failed!" + e.getMessage(), e);
                        a = new ArrayList();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable e2222) {
                                com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "IOException / " + e2222.getMessage(), e2222);
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
                    com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "read xml failed!" + e.getMessage(), e);
                    a = new ArrayList();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable e22222) {
                            com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "IOException / " + e22222.getMessage(), e22222);
                        }
                    }
                    return a;
                } catch (Exception e6) {
                    e = e6;
                    inputStream = fileInputStream;
                    com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "read xml failed!" + e.getMessage(), e);
                    a = new ArrayList();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable e222222) {
                            com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "IOException / " + e222222.getMessage(), e222222);
                        }
                    }
                    return a;
                }
            }
            com.huawei.hwid.core.d.b.e.a("SDKAccountXmlImpl", " sdk filepath not exist");
            return new ArrayList();
        } catch (XmlPullParserException e7) {
            e = e7;
            fileInputStream = null;
            com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "read xml failed!" + e.getMessage(), e);
            a = new ArrayList();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return a;
        } catch (FileNotFoundException e8) {
            e = e8;
            com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "read xml failed!" + e.getMessage(), e);
            a = new ArrayList();
            if (inputStream != null) {
                inputStream.close();
            }
            return a;
        } catch (IOException e9) {
            e = e9;
            com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "read xml failed!" + e.getMessage(), e);
            a = new ArrayList();
            if (inputStream != null) {
                inputStream.close();
            }
            return a;
        } catch (Exception e10) {
            e = e10;
            com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", "read xml failed!" + e.getMessage(), e);
            a = new ArrayList();
            if (inputStream != null) {
                inputStream.close();
            }
            return a;
        }
    }

    private static ArrayList<HwAccount> a(XmlPullParser xmlPullParser, Context context, boolean z) throws XmlPullParserException, IOException {
        com.huawei.hwid.core.d.b.e.e("SDKAccountXmlImpl", "parseAccountsFromXml Start");
        int eventType = xmlPullParser.getEventType();
        ArrayList<HwAccount> arrayList = new ArrayList();
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
                                                        if (!"loginUserName".equals(name)) {
                                                            if ("countryIsoCode".equals(name)) {
                                                                nextText = xmlPullParser.nextText();
                                                                if (z) {
                                                                    nextText = e.c(context, nextText);
                                                                }
                                                                hwAccount.a(nextText);
                                                                hwAccount2 = hwAccount;
                                                                break;
                                                            }
                                                        }
                                                        nextText = xmlPullParser.nextText();
                                                        if (z) {
                                                            nextText = e.c(context, nextText);
                                                        }
                                                        hwAccount.j(nextText);
                                                        com.huawei.hwid.core.d.b.e.e("SDKAccountXmlImpl", "read loginUserName: " + com.huawei.hwid.core.encrypt.f.a(nextText));
                                                        hwAccount2 = hwAccount;
                                                        break;
                                                    }
                                                    hwAccount.g(xmlPullParser.nextText());
                                                    hwAccount2 = hwAccount;
                                                    break;
                                                }
                                                try {
                                                    eventType = Integer.parseInt(xmlPullParser.nextText());
                                                    hwAccount.a(eventType);
                                                    com.huawei.hwid.core.d.b.e.e("SDKAccountXmlImpl", "read mSiteId: " + eventType);
                                                } catch (NumberFormatException e) {
                                                    com.huawei.hwid.core.d.b.e.c("SDKAccountXmlImpl", "NumberFormatException: read accounts.xml parseInt error");
                                                } catch (Exception e2) {
                                                    com.huawei.hwid.core.d.b.e.c("SDKAccountXmlImpl", "read accounts.xml parseInt error");
                                                }
                                                hwAccount2 = hwAccount;
                                                break;
                                            }
                                            nextText = xmlPullParser.nextText();
                                            if (z) {
                                                nextText = e.c(context, nextText);
                                            }
                                            hwAccount.e(nextText);
                                            hwAccount2 = hwAccount;
                                            break;
                                        }
                                        hwAccount.f(e.c(context, xmlPullParser.nextText()));
                                        hwAccount2 = hwAccount;
                                        break;
                                    }
                                    nextText = xmlPullParser.nextText();
                                    hwAccount.i(nextText);
                                    com.huawei.hwid.core.d.b.e.e("SDKAccountXmlImpl", "read mDeviceType: " + nextText);
                                    hwAccount2 = hwAccount;
                                    break;
                                }
                                nextText = xmlPullParser.nextText();
                                if (z) {
                                    nextText = e.c(context, nextText);
                                }
                                hwAccount.h(nextText);
                                com.huawei.hwid.core.d.b.e.e("SDKAccountXmlImpl", "read mDeviceId: " + com.huawei.hwid.core.encrypt.f.a(nextText));
                                hwAccount2 = hwAccount;
                                break;
                            }
                            nextText = xmlPullParser.nextText();
                            if (z) {
                                nextText = e.c(context, nextText);
                            }
                            hwAccount.d(nextText);
                            com.huawei.hwid.core.d.b.e.e("SDKAccountXmlImpl", "read mUserId: " + com.huawei.hwid.core.encrypt.f.a(nextText));
                            hwAccount2 = hwAccount;
                            break;
                        }
                        nextText = xmlPullParser.nextText();
                        if (z) {
                            nextText = e.c(context, nextText);
                        }
                        hwAccount.b(nextText);
                        com.huawei.hwid.core.d.b.e.e("SDKAccountXmlImpl", "read mUserName: " + com.huawei.hwid.core.encrypt.f.c(nextText));
                        hwAccount2 = hwAccount;
                        break;
                    }
                    nextText = xmlPullParser.getAttributeValue("", "appId");
                    if (TextUtils.isEmpty(nextText)) {
                        com.huawei.hwid.core.d.b.e.e("SDKAccountXmlImpl", "authTokenType is null");
                        nextText = com.huawei.hwid.core.d.b.d(context);
                        hwAccount.e(f.b(context, "Cookie"));
                    }
                    hwAccount.c(nextText);
                    com.huawei.hwid.core.d.b.e.e("SDKAccountXmlImpl", "read authTokenType: " + nextText);
                    hwAccount2 = hwAccount;
                    break;
                case 3:
                    if (!"account".equals(name)) {
                        hwAccount2 = hwAccount;
                        break;
                    }
                    com.huawei.hwid.core.d.b.e.e("SDKAccountXmlImpl", "parseAccountsFromXml add account");
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

    private static List<HwAccount> a(List<HwAccount> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Object arrayList = new ArrayList();
        for (HwAccount hwAccount : list) {
            if (!com.huawei.hwid.core.d.b.a(hwAccount)) {
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
            com.huawei.hwid.core.d.b.e.d("SDKAccountXmlImpl", e.getMessage());
        }
        return list;
    }
}
