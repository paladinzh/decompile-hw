package com.huawei.systemmanager.power.comm;

import java.util.ArrayList;
import java.util.List;

public class SpecialAPpList {
    private static final List<String> mSpecialAppList = new ArrayList();

    static {
        mSpecialAppList.add("com.tencent.mobileqq");
        mSpecialAppList.add("com.tencent.mm");
        mSpecialAppList.add("com.tencent.qq");
        mSpecialAppList.add("com.tencent.mqq");
        mSpecialAppList.add("com.whatsapp");
        mSpecialAppList.add("com.viber.voip");
        mSpecialAppList.add("com.skype.raider");
        mSpecialAppList.add("com.vkontakte.android");
        mSpecialAppList.add("ru.ok.android");
        mSpecialAppList.add("com.instagram.android");
        mSpecialAppList.add("com.imo.android.imoim");
        mSpecialAppList.add("com.facebook.orca");
        mSpecialAppList.add("com.facebook.katana");
        mSpecialAppList.add("com.snapchat.android");
        mSpecialAppList.add("com.facebook.lite");
        mSpecialAppList.add("com.azarlive.android");
        mSpecialAppList.add("com.truecaller");
        mSpecialAppList.add("com.jb.gosms");
        mSpecialAppList.add("co.happybits.marcopolo");
        mSpecialAppList.add("org.telegram.messenger");
        mSpecialAppList.add("com.facefarsi.app");
        mSpecialAppList.add("com.facebook.groups");
        mSpecialAppList.add("com.sgiggle.production");
        mSpecialAppList.add("jp.naver.line.android");
        mSpecialAppList.add("com.kakao.talk");
        mSpecialAppList.add("net.daum.android.daum");
        mSpecialAppList.add("com.kakao.story");
        mSpecialAppList.add("com.nhn.android.band");
        mSpecialAppList.add("jp.co.yahoo.android.ymail");
        mSpecialAppList.add("jp.naver.lineplay.android");
        mSpecialAppList.add("ccom.bbm");
        mSpecialAppList.add("com.sparkslab.dcardreader");
        mSpecialAppList.add("com.viber.installer");
        mSpecialAppList.add("com.yahoo.mobile.client.android.mail");
        mSpecialAppList.add("ru.mail.mailapp");
        mSpecialAppList.add("de.gmx.mobile.android.mail");
        mSpecialAppList.add("de.web.mobile.android.mail");
        mSpecialAppList.add("bg.abv.andro.emailapp");
        mSpecialAppList.add("org.kman.AquaMail");
        mSpecialAppList.add("com.google.android.gm");
    }

    public static boolean isSpecialApp(String packageName) {
        if (mSpecialAppList.contains(packageName)) {
            return true;
        }
        return false;
    }
}
