package com.huawei.hwid.core.d.a;

import android.content.Context;
import android.text.TextUtils;
import android.util.Xml;
import com.huawei.hwid.core.c.a;
import com.huawei.hwid.core.d.b;
import com.huawei.hwid.core.d.f;
import com.huawei.hwid.core.datatype.HwAccount;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class e implements b {
    public void a(Context context, int i, int i2) {
        if (i < i2) {
            a(context);
            b(context);
            return;
        }
        com.huawei.hwid.core.d.b.e.d("XmlFileGrade", "newVersion is less then oldVersion, onUpgrade error");
    }

    private void a(Context context) {
        com.huawei.hwid.core.d.b.e.b("XmlFileGrade", "update HwAccounts.xml when version update");
        a a = a.a(context);
        a.a("last_head_picture_url");
        com.huawei.hwid.core.d.b.e.b("XmlFileGrade", "delete last_head_picture_url in HwAccount.xml");
        a.a("DEVID");
        com.huawei.hwid.core.d.b.e.b("XmlFileGrade", "delete DEVID in HwAccount.xml");
        a.a("SUBDEVID");
        com.huawei.hwid.core.d.b.e.b("XmlFileGrade", "delete SUBDEVID in HwAccount.xml");
        a.a("hasEncryptHeadPictureUrl");
        com.huawei.hwid.core.d.b.e.b("XmlFileGrade", "delete hasEncryptHeadPictureUrl in HwAccount.xml");
        Object a2 = a.a("accessToken", "");
        if (!TextUtils.isEmpty(a2)) {
            String a3 = com.huawei.hwid.core.encrypt.e.a(context, a2);
            if (TextUtils.isEmpty(a3)) {
                a.a("accessToken");
                com.huawei.hwid.core.d.b.e.b("XmlFileGrade", "accessToken ecb decrypt error");
                return;
            }
            com.huawei.hwid.core.d.b.e.b("XmlFileGrade", "update accessToken in HwAccount.xml");
            a.b("accessToken", a3);
        }
    }

    private void b(Context context) {
        if (!b.h(context)) {
            List a;
            if (c(context)) {
                a = a("accounts.xml", context, false);
            } else {
                a = a("accounts.xml", context, true);
            }
            f.a(context, "accounts.xml");
            com.huawei.hwid.a.a.b.a(context, "accounts.xml", a, true);
        }
    }

    private boolean c(Context context) {
        if (TextUtils.isEmpty(f.b(context, "encryptversion")) && !b.a(context, "isSDKAccountDataEncrypted", false)) {
            return true;
        }
        return false;
    }

    private static ArrayList<HwAccount> a(String str, Context context, boolean z) {
        ArrayList<HwAccount> a;
        Throwable e;
        InputStream inputStream = null;
        InputStream fileInputStream;
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
                            com.huawei.hwid.core.d.b.e.d("XmlFileGrade", "IOException / " + e2.getMessage(), e2);
                        }
                    }
                    return a;
                } catch (XmlPullParserException e3) {
                    e = e3;
                    try {
                        com.huawei.hwid.core.d.b.e.d("XmlFileGrade", "read xml failed!" + e.getMessage(), e);
                        a = new ArrayList();
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (Throwable e22) {
                                com.huawei.hwid.core.d.b.e.d("XmlFileGrade", "IOException / " + e22.getMessage(), e22);
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
                                com.huawei.hwid.core.d.b.e.d("XmlFileGrade", "IOException / " + e222.getMessage(), e222);
                            }
                        }
                        throw e;
                    }
                } catch (FileNotFoundException e4) {
                    e = e4;
                    inputStream = fileInputStream;
                    try {
                        com.huawei.hwid.core.d.b.e.d("XmlFileGrade", "read xml failed!" + e.getMessage(), e);
                        a = new ArrayList();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable e2222) {
                                com.huawei.hwid.core.d.b.e.d("XmlFileGrade", "IOException / " + e2222.getMessage(), e2222);
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
                    com.huawei.hwid.core.d.b.e.d("XmlFileGrade", "read xml failed!" + e.getMessage(), e);
                    a = new ArrayList();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable e22222) {
                            com.huawei.hwid.core.d.b.e.d("XmlFileGrade", "IOException / " + e22222.getMessage(), e22222);
                        }
                    }
                    return a;
                } catch (Exception e6) {
                    e = e6;
                    inputStream = fileInputStream;
                    com.huawei.hwid.core.d.b.e.d("XmlFileGrade", "read xml failed!" + e.getMessage(), e);
                    a = new ArrayList();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable e222222) {
                            com.huawei.hwid.core.d.b.e.d("XmlFileGrade", "IOException / " + e222222.getMessage(), e222222);
                        }
                    }
                    return a;
                }
            }
            com.huawei.hwid.core.d.b.e.a("XmlFileGrade", " sdk filepath not exist");
            return new ArrayList();
        } catch (XmlPullParserException e7) {
            e = e7;
            fileInputStream = null;
            com.huawei.hwid.core.d.b.e.d("XmlFileGrade", "read xml failed!" + e.getMessage(), e);
            a = new ArrayList();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return a;
        } catch (FileNotFoundException e8) {
            e = e8;
            com.huawei.hwid.core.d.b.e.d("XmlFileGrade", "read xml failed!" + e.getMessage(), e);
            a = new ArrayList();
            if (inputStream != null) {
                inputStream.close();
            }
            return a;
        } catch (IOException e9) {
            e = e9;
            com.huawei.hwid.core.d.b.e.d("XmlFileGrade", "read xml failed!" + e.getMessage(), e);
            a = new ArrayList();
            if (inputStream != null) {
                inputStream.close();
            }
            return a;
        } catch (Exception e10) {
            e = e10;
            com.huawei.hwid.core.d.b.e.d("XmlFileGrade", "read xml failed!" + e.getMessage(), e);
            a = new ArrayList();
            if (inputStream != null) {
                inputStream.close();
            }
            return a;
        }
    }

    private static ArrayList<HwAccount> a(XmlPullParser xmlPullParser, Context context, boolean z) throws XmlPullParserException, IOException {
        com.huawei.hwid.core.d.b.e.e("XmlFileGrade", "parseAccountsFromXml Start");
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
                                                    com.huawei.hwid.core.d.b.e.e("XmlFileGrade", "read mSiteId: " + eventType);
                                                } catch (NumberFormatException e) {
                                                    com.huawei.hwid.core.d.b.e.c("XmlFileGrade", "NumberFormatException: read accounts.xml parseInt error");
                                                } catch (Exception e2) {
                                                    com.huawei.hwid.core.d.b.e.c("XmlFileGrade", "read accounts.xml parseInt error");
                                                }
                                                hwAccount2 = hwAccount;
                                                break;
                                            }
                                            nextText = xmlPullParser.nextText();
                                            if (z) {
                                                nextText = com.huawei.hwid.core.encrypt.e.a(context, nextText);
                                            }
                                            hwAccount.e(nextText);
                                            hwAccount2 = hwAccount;
                                            break;
                                        }
                                        hwAccount.f(com.huawei.hwid.core.encrypt.e.a(context, xmlPullParser.nextText()));
                                        hwAccount2 = hwAccount;
                                        break;
                                    }
                                    nextText = xmlPullParser.nextText();
                                    hwAccount.i(nextText);
                                    com.huawei.hwid.core.d.b.e.e("XmlFileGrade", "read mDeviceType: " + nextText);
                                    hwAccount2 = hwAccount;
                                    break;
                                }
                                nextText = xmlPullParser.nextText();
                                if (z) {
                                    nextText = com.huawei.hwid.core.encrypt.e.a(context, nextText);
                                }
                                hwAccount.h(nextText);
                                com.huawei.hwid.core.d.b.e.e("XmlFileGrade", "read mDeviceId: " + com.huawei.hwid.core.encrypt.f.a(nextText));
                                hwAccount2 = hwAccount;
                                break;
                            }
                            nextText = xmlPullParser.nextText();
                            if (z) {
                                nextText = com.huawei.hwid.core.encrypt.e.a(context, nextText);
                            }
                            hwAccount.d(nextText);
                            com.huawei.hwid.core.d.b.e.e("XmlFileGrade", "read u*!d: " + com.huawei.hwid.core.encrypt.f.a(nextText));
                            hwAccount2 = hwAccount;
                            break;
                        }
                        nextText = xmlPullParser.nextText();
                        if (z) {
                            nextText = com.huawei.hwid.core.encrypt.e.a(context, nextText);
                        }
                        hwAccount.b(nextText);
                        com.huawei.hwid.core.d.b.e.e("XmlFileGrade", "read u*n@me: " + com.huawei.hwid.core.encrypt.f.c(nextText));
                        hwAccount2 = hwAccount;
                        break;
                    }
                    nextText = xmlPullParser.getAttributeValue("", "appId");
                    if (TextUtils.isEmpty(nextText)) {
                        com.huawei.hwid.core.d.b.e.e("XmlFileGrade", "authTokenType is null");
                        nextText = b.d(context);
                        hwAccount.e(f.b(context, "Cookie"));
                    }
                    hwAccount.c(nextText);
                    com.huawei.hwid.core.d.b.e.e("XmlFileGrade", "read authTokenType: " + nextText);
                    hwAccount2 = hwAccount;
                    break;
                case 3:
                    if (!"account".equals(name)) {
                        hwAccount2 = hwAccount;
                        break;
                    }
                    com.huawei.hwid.core.d.b.e.e("XmlFileGrade", "parseAccountsFromXml add account");
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
