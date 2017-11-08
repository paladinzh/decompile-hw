package com.huawei.hwid.core.c.a;

import android.content.Context;
import android.text.TextUtils;
import android.util.Xml;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.i;
import com.huawei.hwid.core.c.p;
import com.huawei.hwid.core.datatype.HwAccount;
import com.huawei.hwid.core.encrypt.f;
import com.huawei.hwid.manager.b.b;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* compiled from: XmlFileGrade */
public class e implements b {
    public void a(Context context, int i, int i2) {
        if (i < i2) {
            a(context);
            b(context);
            return;
        }
        a.d("XmlFileGrade", "newVersion is less then oldVersion, onUpgrade error");
    }

    private void a(Context context) {
        a.b("XmlFileGrade", "update HwAccounts.xml when version update");
        com.huawei.hwid.core.b.a a = com.huawei.hwid.core.b.a.a(context);
        a.b("last_head_picture_url");
        a.b("XmlFileGrade", "delete last_head_picture_url in HwAccount.xml");
        a.b("DEVID");
        a.b("XmlFileGrade", "delete DEVID in HwAccount.xml");
        a.b("SUBDEVID");
        a.b("XmlFileGrade", "delete SUBDEVID in HwAccount.xml");
        a.b("hasEncryptHeadPictureUrl");
        a.b("XmlFileGrade", "delete hasEncryptHeadPictureUrl in HwAccount.xml");
        Object a2 = a.a("accessToken", "");
        if (!TextUtils.isEmpty(a2)) {
            String a3 = com.huawei.hwid.core.encrypt.e.a(context, a2);
            if (TextUtils.isEmpty(a3)) {
                a.b("accessToken");
                a.b("XmlFileGrade", "accessToken ecb decrypt error");
                return;
            }
            a.b("XmlFileGrade", "update accessToken in HwAccount.xml");
            a.b("accessToken", a3);
        }
    }

    private void b(Context context) {
        if (!d.h(context)) {
            List a;
            if (c(context)) {
                a = a("accounts.xml", context, false);
            } else {
                a = a("accounts.xml", context, true);
            }
            i.a(context, "accounts.xml");
            b.a(context, "accounts.xml", a, true);
        }
    }

    private boolean c(Context context) {
        if (TextUtils.isEmpty(i.b(context, "encryptversion")) && !d.b(context, "isSDKAccountDataEncrypted", false)) {
            return true;
        }
        return false;
    }

    private static ArrayList a(String str, Context context, boolean z) {
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
                            a.d("XmlFileGrade", "IOException / " + e2.toString(), e2);
                        }
                    }
                    return a;
                } catch (XmlPullParserException e3) {
                    e = e3;
                    try {
                        a.d("XmlFileGrade", "read xml failed!" + e.getMessage(), e);
                        a = new ArrayList();
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Throwable e22) {
                                a.d("XmlFileGrade", "IOException / " + e22.toString(), e22);
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
                                a.d("XmlFileGrade", "IOException / " + e222.toString(), e222);
                            }
                        }
                        throw e;
                    }
                } catch (FileNotFoundException e4) {
                    e = e4;
                    inputStream = fileInputStream;
                    try {
                        a.d("XmlFileGrade", "read xml failed!" + e.getMessage(), e);
                        a = new ArrayList();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable e2222) {
                                a.d("XmlFileGrade", "IOException / " + e2222.toString(), e2222);
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
                    a.d("XmlFileGrade", "read xml failed!" + e.getMessage(), e);
                    a = new ArrayList();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable e22222) {
                            a.d("XmlFileGrade", "IOException / " + e22222.toString(), e22222);
                        }
                    }
                    return a;
                } catch (Exception e6) {
                    e = e6;
                    inputStream = fileInputStream;
                    a.d("XmlFileGrade", "read xml failed!" + e.getMessage(), e);
                    a = new ArrayList();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable e222222) {
                            a.d("XmlFileGrade", "IOException / " + e222222.toString(), e222222);
                        }
                    }
                    return a;
                }
            }
            a.a("XmlFileGrade", " sdk filepath not exist");
            return new ArrayList();
        } catch (XmlPullParserException e7) {
            e = e7;
            fileInputStream = null;
            a.d("XmlFileGrade", "read xml failed!" + e.getMessage(), e);
            a = new ArrayList();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return a;
        } catch (FileNotFoundException e8) {
            e = e8;
            a.d("XmlFileGrade", "read xml failed!" + e.getMessage(), e);
            a = new ArrayList();
            if (inputStream != null) {
                inputStream.close();
            }
            return a;
        } catch (IOException e9) {
            e = e9;
            a.d("XmlFileGrade", "read xml failed!" + e.getMessage(), e);
            a = new ArrayList();
            if (inputStream != null) {
                inputStream.close();
            }
            return a;
        } catch (Exception e10) {
            e = e10;
            a.d("XmlFileGrade", "read xml failed!" + e.getMessage(), e);
            a = new ArrayList();
            if (inputStream != null) {
                inputStream.close();
            }
            return a;
        }
    }

    private static ArrayList a(XmlPullParser xmlPullParser, Context context, boolean z) throws XmlPullParserException, IOException {
        a.e("XmlFileGrade", "parseAccountsFromXml Start");
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
                                                    a.e("XmlFileGrade", "read mSiteId: " + eventType);
                                                } catch (NumberFormatException e) {
                                                    a.c("XmlFileGrade", "NumberFormatException: read accounts.xml parseInt error");
                                                } catch (Exception e2) {
                                                    a.c("XmlFileGrade", "read accounts.xml parseInt error");
                                                }
                                                hwAccount2 = hwAccount;
                                                break;
                                            }
                                            nextText = xmlPullParser.nextText();
                                            if (z) {
                                                nextText = com.huawei.hwid.core.encrypt.e.a(context, nextText);
                                            }
                                            hwAccount.d(nextText);
                                            hwAccount2 = hwAccount;
                                            break;
                                        }
                                        hwAccount.e(com.huawei.hwid.core.encrypt.e.a(context, xmlPullParser.nextText()));
                                        hwAccount2 = hwAccount;
                                        break;
                                    }
                                    nextText = xmlPullParser.nextText();
                                    hwAccount.h(nextText);
                                    a.e("XmlFileGrade", "read mDeviceType: " + nextText);
                                    hwAccount2 = hwAccount;
                                    break;
                                }
                                nextText = xmlPullParser.nextText();
                                if (z) {
                                    nextText = com.huawei.hwid.core.encrypt.e.a(context, nextText);
                                }
                                hwAccount.g(nextText);
                                a.e("XmlFileGrade", "read mDeviceId: " + f.a(nextText));
                                hwAccount2 = hwAccount;
                                break;
                            }
                            nextText = xmlPullParser.nextText();
                            if (z) {
                                nextText = com.huawei.hwid.core.encrypt.e.a(context, nextText);
                            }
                            hwAccount.c(nextText);
                            a.e("XmlFileGrade", "read u*!d: " + f.a(nextText));
                            hwAccount2 = hwAccount;
                            break;
                        }
                        nextText = xmlPullParser.nextText();
                        if (z) {
                            nextText = com.huawei.hwid.core.encrypt.e.a(context, nextText);
                        }
                        hwAccount.a(nextText);
                        a.e("XmlFileGrade", "read u*n@me: " + f.c(nextText));
                        hwAccount2 = hwAccount;
                        break;
                    }
                    nextText = xmlPullParser.getAttributeValue("", "appId");
                    if (p.e(nextText)) {
                        a.e("XmlFileGrade", "authTokenType is null");
                        nextText = d.d(context);
                        hwAccount.d(i.b(context, "Cookie"));
                    }
                    hwAccount.b(nextText);
                    a.e("XmlFileGrade", "read authTokenType: " + nextText);
                    hwAccount2 = hwAccount;
                    break;
                case 3:
                    if (!"account".equals(name)) {
                        hwAccount2 = hwAccount;
                        break;
                    }
                    a.e("XmlFileGrade", "parseAccountsFromXml add account");
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
}
