package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.amap.api.services.district.DistrictSearchQuery;
import java.io.IOException;
import java.io.Serializable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class UserInfo implements Parcelable, Serializable {
    public static final Creator<UserInfo> CREATOR = new k();
    private String A;
    private String B;
    private String C;
    private String D;
    private String E;
    private String F;
    private String G;
    private String H;
    private String I;
    private String a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g;
    private String h;
    private String i;
    private String j;
    private String k;
    private String l;
    private String m;
    private String n;
    private String o;
    private String p;
    private String q;
    private String r;
    private String s;
    private String t;
    private String u;
    private String v;
    private String w;
    private String x;
    private String y;
    private String z;

    public void setCtfCode(String str) {
        this.I = str;
    }

    public void setCtfType(String str) {
        this.G = str;
    }

    public void setCtfVerifyFlag(String str) {
        this.H = str;
    }

    public void setUserValidStatus(String str) {
        this.s = str;
    }

    public void setInviterUserID(String str) {
        this.t = str;
    }

    public void setInviter(String str) {
        this.u = str;
    }

    public void setUpdateTime(String str) {
        this.v = str;
    }

    public void setNickName(String str) {
        this.a = str;
    }

    public void setUniqueNickName(String str) {
        this.b = str;
    }

    public void setLanguageCode(String str) {
        this.c = str;
    }

    public void setFirstName(String str) {
        this.d = str;
    }

    public void setLastName(String str) {
        this.e = str;
    }

    public void setUserState(String str) {
        this.f = str;
    }

    public void setGender(String str) {
        this.g = str;
    }

    public void setBirthDate(String str) {
        this.h = str;
    }

    public void setAddress(String str) {
        this.i = str;
    }

    public void setOccupation(String str) {
        this.j = str;
    }

    public String getHeadPictureURL() {
        return this.k;
    }

    public void setHeadPictureURL(String str) {
        this.k = str;
    }

    public void setNationalCode(String str) {
        this.l = str;
    }

    public void setProvince(String str) {
        this.m = str;
    }

    public void setCity(String str) {
        this.n = str;
    }

    public void setPasswordPrompt(String str) {
        this.o = str;
    }

    public void setPasswordAnwser(String str) {
        this.p = str;
    }

    public void setCloudAccount(String str) {
        this.q = str;
    }

    public void setServiceFlag(String str) {
        this.r = str;
    }

    public String getLoginUserName() {
        return this.w;
    }

    public void setLoginUserName(String str) {
        this.w = str;
    }

    public void setLoginUserNameFlag(String str) {
        this.x = str;
    }

    public void setuserStatusFlags(String str) {
        this.y = str;
    }

    public void settwoStepVerify(String str) {
        this.z = str;
    }

    public void settwoStepTime(String str) {
        this.A = str;
    }

    public void setResetPasswdMode(String str) {
        this.B = str;
    }

    public int describeContents() {
        return 0;
    }

    public void setUserSign(String str) {
        this.C = str;
    }

    public void setLoginNotice(String str) {
        this.D = str;
    }

    public void setGuardianUserID(String str) {
        this.E = str;
    }

    public void setGuardianAccount(String str) {
        this.F = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.i);
        parcel.writeString(this.h);
        parcel.writeString(this.n);
        parcel.writeString(this.q);
        parcel.writeString(this.d);
        parcel.writeString(this.g);
        parcel.writeString(this.k);
        parcel.writeString(this.c);
        parcel.writeString(this.e);
        parcel.writeString(this.l);
        parcel.writeString(this.a);
        parcel.writeString(this.b);
        parcel.writeString(this.j);
        parcel.writeString(this.p);
        parcel.writeString(this.o);
        parcel.writeString(this.m);
        parcel.writeString(this.r);
        parcel.writeString(this.f);
        parcel.writeString(this.s);
        parcel.writeString(this.t);
        parcel.writeString(this.u);
        parcel.writeString(this.v);
        parcel.writeString(this.w);
        parcel.writeString(this.x);
        parcel.writeString(this.y);
        parcel.writeString(this.z);
        parcel.writeString(this.A);
        parcel.writeString(this.B);
        parcel.writeString(this.D);
        parcel.writeString(this.C);
        parcel.writeString(this.E);
        parcel.writeString(this.F);
        parcel.writeString(this.I);
        parcel.writeString(this.H);
        parcel.writeString(this.G);
    }

    public static void setInfo1(XmlPullParser xmlPullParser, UserInfo userInfo, String str) throws IllegalArgumentException, IllegalStateException, IOException, XmlPullParserException {
        if ("nickName".equals(str)) {
            userInfo.setNickName(xmlPullParser.nextText());
        } else if ("uniquelyNickname".equals(str)) {
            userInfo.setUniqueNickName(xmlPullParser.nextText());
        } else if ("languageCode".equals(str)) {
            userInfo.setLanguageCode(xmlPullParser.nextText());
        } else if ("firstName".equals(str)) {
            userInfo.setFirstName(xmlPullParser.nextText());
        } else if ("lastName".equals(str)) {
            userInfo.setLastName(xmlPullParser.nextText());
        } else if ("userState".equals(str)) {
            userInfo.setUserState(xmlPullParser.nextText());
        } else if ("guardianAccount".equals(str)) {
            userInfo.setGuardianAccount(xmlPullParser.nextText());
        } else if ("guardianUserID".equals(str)) {
            userInfo.setGuardianUserID(xmlPullParser.nextText());
        } else if ("ctfCode".equals(str)) {
            userInfo.setCtfCode(xmlPullParser.nextText());
        } else if ("ctfType".equals(str)) {
            userInfo.setCtfType(xmlPullParser.nextText());
        } else if ("ctfVerifyFlag".equals(str)) {
            userInfo.setCtfVerifyFlag(xmlPullParser.nextText());
        } else if ("userValidStatus".equals(str)) {
            userInfo.setUserValidStatus(xmlPullParser.nextText());
        } else if ("InviterUserID".equals(str)) {
            userInfo.setInviterUserID(xmlPullParser.nextText());
        }
    }

    public static void setInfo2(XmlPullParser xmlPullParser, UserInfo userInfo, String str) throws IllegalArgumentException, IllegalStateException, IOException, XmlPullParserException {
        if ("gender".equals(str)) {
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
        } else if (DistrictSearchQuery.KEYWORDS_CITY.equals(str)) {
            userInfo.setCity(xmlPullParser.nextText());
        } else if ("passwordPrompt".equals(str)) {
            userInfo.setPasswordPrompt(xmlPullParser.nextText());
        } else if ("passwordAnswer".equals(str)) {
            userInfo.setPasswordAnwser(xmlPullParser.nextText());
        } else if ("cloudAccount".equals(str)) {
            userInfo.setCloudAccount(xmlPullParser.nextText());
        } else if ("ServiceFlag".equals(str)) {
            userInfo.setServiceFlag(xmlPullParser.nextText());
        }
    }

    public static void setInfo3(XmlPullParser xmlPullParser, UserInfo userInfo, String str) throws IllegalArgumentException, IllegalStateException, IOException, XmlPullParserException {
        if ("Inviter".equals(str)) {
            userInfo.setInviter(xmlPullParser.nextText());
        } else if ("updateTime".equals(str)) {
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
        } else if ("loginnotice".equals(str)) {
            userInfo.setLoginNotice(xmlPullParser.nextText());
        }
    }

    public static void getUserInfoIntag(XmlPullParser xmlPullParser, UserInfo userInfo, String str) throws IllegalArgumentException, IllegalStateException, IOException, XmlPullParserException {
        if (xmlPullParser != null && userInfo != null && str != null) {
            setInfo1(xmlPullParser, userInfo, str);
            setInfo2(xmlPullParser, userInfo, str);
            setInfo3(xmlPullParser, userInfo, str);
        }
    }

    public String toString() {
        return "UserInfo [mNickName=" + this.a + ", mUniqueNickName=" + this.b + ", mLanguageCode=" + this.c + ", mFirstName=" + this.d + ", mLastName=" + this.e + ", mUserState=" + this.f + ", mGender=" + this.g + ", mBirthDate=" + this.h + ", mAddress=" + this.i + ", mOccupation=" + this.j + ", mHeadPictureURL=" + this.k + ", mNationalCode=" + this.l + ", mProvince=" + this.m + ", mCity=" + this.n + ", mPasswordPrompt=" + this.o + ", mscrtdanws=" + this.p + ", mCloudAccount=" + this.q + ", mServiceFlag=" + this.r + ", mUserValidStatus=" + this.s + ", mInviterUserID=" + this.t + ", mInviter=" + this.u + ", mUpdateTime=" + this.v + ", mLoginUserName=" + this.w + ", mLoginUserNameFlag=" + this.x + ", muserStatusFlags=" + this.y + ", mtwoStepVerify=" + this.z + ", mtwoStepTime=" + this.A + ", mResetPasswdMode=" + this.B + ", mUserSign=" + this.C + ", mLoginNotice=" + this.D + ", mGuardianUserID=" + this.E + ", mGuardianAccount=" + this.F + ", mCtfType=" + this.G + ", mCtfVerifyFlag=" + this.H + ", mCtfCode=" + this.I + "]";
    }
}
