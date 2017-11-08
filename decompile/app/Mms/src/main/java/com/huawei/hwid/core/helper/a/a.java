package com.huawei.hwid.core.helper.a;

import android.content.Context;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import com.amap.api.services.district.DistrictSearchQuery;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.k;
import com.huawei.hwid.core.c.t;
import com.huawei.hwid.core.datatype.AgreementVersion;
import com.huawei.hwid.core.datatype.DeviceInfo;
import com.huawei.hwid.core.datatype.SMSCountryInfo;
import com.huawei.hwid.core.datatype.SiteInfo;
import com.huawei.hwid.core.datatype.TmemberRight;
import com.huawei.hwid.core.datatype.UserAccountInfo;
import com.huawei.hwid.core.datatype.UserInfo;
import com.huawei.hwid.core.datatype.UserLoginInfo;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: RequestXmlParser */
public class a {
    public static void a(XmlPullParser xmlPullParser, UserInfo userInfo, String str) throws IllegalArgumentException, IllegalStateException, IOException, XmlPullParserException {
        if (xmlPullParser != null && userInfo != null && str != null) {
            if ("nickName".equals(str)) {
                userInfo.setNickName(xmlPullParser.nextText());
            } else if ("languageCode".equals(str)) {
                userInfo.setLanguageCode(xmlPullParser.nextText());
            } else if ("firstName".equals(str)) {
                userInfo.setFirstName(xmlPullParser.nextText());
            } else if ("lastName".equals(str)) {
                userInfo.setLastName(xmlPullParser.nextText());
            } else if ("userState".equals(str)) {
                userInfo.setUserState(xmlPullParser.nextText());
            } else if ("gender".equals(str)) {
                userInfo.setGender(xmlPullParser.nextText());
            } else if ("birthDate".equals(str)) {
                userInfo.setBirthDate(xmlPullParser.nextText());
            } else if ("address".equals(str)) {
                userInfo.setAddress(xmlPullParser.nextText());
            } else if ("occupation".equals(str)) {
                userInfo.setOccupation(xmlPullParser.nextText());
            } else if ("headPictureURL".equals(str)) {
                userInfo.setHeadPictureURL(xmlPullParser.nextText());
            } else if ("nationalCode".equals(str)) {
                userInfo.setNationalCode(xmlPullParser.nextText());
            } else if (DistrictSearchQuery.KEYWORDS_PROVINCE.equals(str)) {
                userInfo.setProvince(xmlPullParser.nextText());
            } else if ("city".equals(str)) {
                userInfo.setCity(xmlPullParser.nextText());
            } else if ("passwordPrompt".equals(str)) {
                userInfo.setPasswordPrompt(xmlPullParser.nextText());
            } else if ("passwordAnswer".equals(str)) {
                userInfo.setPasswordAnwser(xmlPullParser.nextText());
            } else if ("cloudAccount".equals(str)) {
                userInfo.setCloudAccount(xmlPullParser.nextText());
            } else if ("ServiceFlag".equals(str)) {
                userInfo.setServiceFlag(xmlPullParser.nextText());
            } else if ("userValidStatus".equals(str)) {
                userInfo.setUserValidStatus(xmlPullParser.nextText());
            } else if ("InviterUserID".equals(str)) {
                userInfo.setInviterUserID(xmlPullParser.nextText());
            } else if ("Inviter".equals(str)) {
                userInfo.setInviter(xmlPullParser.nextText());
            } else if (IccidInfoManager.UPDATE_TIME.equals(str)) {
                userInfo.setUpdateTime(xmlPullParser.nextText());
            } else if ("loginUserName".equals(str)) {
                userInfo.setLoginUserName(xmlPullParser.nextText());
            } else if ("loginUserNameFlag".equals(str)) {
                userInfo.setLoginUserNameFlag(xmlPullParser.nextText());
            } else if ("userStatusFlags".equals(str)) {
                userInfo.setuserStatusFlags(xmlPullParser.nextText());
            } else if ("twoStepVerify".equals(str)) {
                userInfo.settwoStepVerify(xmlPullParser.nextText());
            } else if ("twoStepTime".equals(str)) {
                userInfo.settwoStepTime(xmlPullParser.nextText());
            } else if ("resetPasswdMode".equals(str)) {
                userInfo.setResetPasswdMode(xmlPullParser.nextText());
            } else if ("userSignature".equals(str)) {
                userInfo.setUserSign(xmlPullParser.nextText());
            }
        }
    }

    public static void a(XmlPullParser xmlPullParser, UserLoginInfo userLoginInfo, String str) throws XmlPullParserException, IOException {
        if (xmlPullParser != null && userLoginInfo != null && str != null) {
            if ("userID".equals(str)) {
                userLoginInfo.setUserIDByUserLoginInfo(xmlPullParser.nextText());
            } else if ("registerTime".equals(str)) {
                userLoginInfo.setRegisterTime(xmlPullParser.nextText());
            } else if ("unRegisterTime".equals(str)) {
                userLoginInfo.setUnRegisterTime(xmlPullParser.nextText());
            } else if ("lastLoginTime".equals(str)) {
                userLoginInfo.setLastLoginTime(xmlPullParser.nextText());
            } else if ("registerClientType".equals(str)) {
                userLoginInfo.setRegisterClientType(xmlPullParser.nextText());
            } else if ("lastLoginIP".equals(str)) {
                userLoginInfo.setLastLoginIP(xmlPullParser.nextText());
            } else if ("registerClientIP".equals(str)) {
                userLoginInfo.setRegisterClientIP(xmlPullParser.nextText());
            } else if ("registerFrom".equals(str)) {
                userLoginInfo.setRegisterFrom(xmlPullParser.nextText());
            }
        }
    }

    public static void a(XmlPullParser xmlPullParser, SMSCountryInfo sMSCountryInfo, String str, Context context) throws XmlPullParserException, IOException {
        if (xmlPullParser != null && sMSCountryInfo != null && str != null) {
            if ("countryCallingCode".equals(str)) {
                sMSCountryInfo.a(xmlPullParser.nextText());
            } else if ("countryCode".equals(str)) {
                String nextText = xmlPullParser.nextText();
                sMSCountryInfo.b(nextText);
                sMSCountryInfo.c(k.a(context, nextText));
            }
        }
    }

    public static void a(XmlPullParser xmlPullParser, DeviceInfo deviceInfo, String str) throws XmlPullParserException, IOException {
        if (xmlPullParser != null && deviceInfo != null && str != null) {
            if ("deviceID".equals(str)) {
                deviceInfo.setDeviceIdInDeviceInfo(xmlPullParser.nextText());
            } else if ("deviceType".equals(str)) {
                deviceInfo.setDeviceType(xmlPullParser.nextText());
            } else if ("terminalType".equals(str)) {
                deviceInfo.setTerminalType(xmlPullParser.nextText());
            } else if ("deviceAliasName".equals(str)) {
                deviceInfo.setDeviceAliasName(xmlPullParser.nextText());
            } else if ("loginTime".equals(str)) {
                deviceInfo.setmLoginTime(xmlPullParser.nextText());
            } else if ("logoutTime".equals(str)) {
                deviceInfo.setmLogoutTime(xmlPullParser.nextText());
            } else if ("frequentlyUsed".equals(str)) {
                deviceInfo.setmFrequentlyUsed(xmlPullParser.nextText());
            }
        }
    }

    public static void a(XmlSerializer xmlSerializer, DeviceInfo deviceInfo) throws IllegalArgumentException, IllegalStateException, IOException {
        if (xmlSerializer != null && deviceInfo != null) {
            t.a(xmlSerializer, "deviceID", deviceInfo.getDeviceID());
            t.a(xmlSerializer, "deviceType", deviceInfo.getDeviceType());
            t.a(xmlSerializer, "terminalType", deviceInfo.getTerminalType());
            t.a(xmlSerializer, "deviceAliasName", deviceInfo.getDeviceAliasName());
        }
    }

    public static void a(XmlPullParser xmlPullParser, UserAccountInfo userAccountInfo, String str) throws XmlPullParserException, IOException {
        if (xmlPullParser != null && userAccountInfo != null && str != null) {
            if ("accountState".equals(str)) {
                userAccountInfo.setAccountState(xmlPullParser.nextText());
            } else if ("accountType".equals(str)) {
                userAccountInfo.setAccountType(xmlPullParser.nextText());
            } else if ("accountValidStatus".equals(str)) {
                userAccountInfo.setAccountValidStatus(xmlPullParser.nextText());
            } else if (IccidInfoManager.UPDATE_TIME.equals(str)) {
                userAccountInfo.setUpdateTime(xmlPullParser.nextText());
            } else if ("userAccount".equals(str)) {
                userAccountInfo.setUserAccount(xmlPullParser.nextText());
            } else if ("userEMail".equals(str)) {
                userAccountInfo.setUserEMail(xmlPullParser.nextText());
            } else if ("mobilePhone".equals(str)) {
                userAccountInfo.setMobilePhone(xmlPullParser.nextText());
            } else if ("emailState".equals(str)) {
                userAccountInfo.setUserEmailState(xmlPullParser.nextText());
            } else if ("mobilePhoneState".equals(str)) {
                userAccountInfo.setMobilePhoneState(xmlPullParser.nextText());
            }
        }
    }

    public static void a(XmlPullParser xmlPullParser, TmemberRight tmemberRight, String str) throws XmlPullParserException, IOException {
        if (xmlPullParser != null && tmemberRight != null && str != null) {
            if ("userID".equals(str)) {
                tmemberRight.a(Long.parseLong(xmlPullParser.nextText()));
            } else if ("deviceType".equals(str)) {
                try {
                    tmemberRight.a(Integer.parseInt(xmlPullParser.nextText()));
                } catch (Exception e) {
                    com.huawei.hwid.core.c.b.a.b("RequestXmlParser", e.toString());
                }
            } else if ("deviceId".equals(str)) {
                tmemberRight.a(xmlPullParser.nextText());
            } else if ("deviceID2".equals(str)) {
                tmemberRight.b(xmlPullParser.nextText());
            } else if ("terminalType".equals(str)) {
                tmemberRight.c(xmlPullParser.nextText());
            } else if ("rightsID".equals(str)) {
                try {
                    tmemberRight.b(Integer.parseInt(xmlPullParser.nextText()));
                } catch (Exception e2) {
                    com.huawei.hwid.core.c.b.a.b("RequestXmlParser", e2.getMessage());
                }
            } else if ("memberBindTime".equals(str)) {
                tmemberRight.d(xmlPullParser.nextText());
            } else if ("expiredDate".equals(str)) {
                String a;
                String str2 = "";
                try {
                    a = d.a(xmlPullParser.nextText(), Constant.PATTERN, "yyyyMMdd");
                } catch (Exception e22) {
                    com.huawei.hwid.core.c.b.a.c("RequestXmlParser", e22.getMessage());
                    a = str2;
                }
                tmemberRight.e(a);
            } else {
                com.huawei.hwid.core.c.b.a.b("RequestXmlParser", "in getTmemberRightTag nodeName:" + str + " is unknow");
            }
        }
    }

    public static void a(XmlSerializer xmlSerializer, AgreementVersion[] agreementVersionArr) throws IllegalArgumentException, IllegalStateException, IOException {
        if (xmlSerializer != null && agreementVersionArr != null && agreementVersionArr.length != 0) {
            for (AgreementVersion agreementVersion : agreementVersionArr) {
                xmlSerializer.startTag(null, "AgrVer");
                t.a(xmlSerializer, "ver", agreementVersion.c());
                t.a(xmlSerializer, "id", String.valueOf(agreementVersion.a()));
                t.a(xmlSerializer, "siteC", agreementVersion.b());
                xmlSerializer.endTag(null, "AgrVer");
            }
        }
    }

    public static void a(XmlPullParser xmlPullParser, AgreementVersion agreementVersion, String str) throws XmlPullParserException, IOException {
        if (xmlPullParser != null && agreementVersion != null && str != null) {
            if ("id".equals(str)) {
                agreementVersion.a(xmlPullParser.nextText());
            } else if ("siteC".equals(str)) {
                agreementVersion.b(xmlPullParser.nextText());
            } else if ("ver".equals(str)) {
                agreementVersion.c(xmlPullParser.nextText());
            }
        }
    }

    public static void a(XmlPullParser xmlPullParser, SiteInfo siteInfo, String str) throws XmlPullParserException, IOException {
        if (xmlPullParser != null && siteInfo != null && str != null) {
            if ("siteID".equals(str)) {
                siteInfo.a(xmlPullParser.nextText());
            } else if ("cy".equals(str)) {
                siteInfo.b(xmlPullParser.nextText());
            }
        }
    }
}
