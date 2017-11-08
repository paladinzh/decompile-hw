package com.android.gallery3d.util;

import java.util.HashSet;
import java.util.Set;

public final class Prop4g {
    static String ANDROID_DATA = "/android/data/";
    static String MAGAZINE_UNLOCK = "/MagazineUnlock";
    static final String[] sBlackListProp = new String[]{"/mmcache/*", "/AnyofficeiconDownload/*", "/AnyofficeiconDownload/com.huawei.anyoffice.mail/*", "/AnyofficeiconDownload/com.huawei.anyoffice.onebox/*", "/com.inveno.hwread/Interset/*", "/com.taobao.ju.android/splash/*", "/jingdong/image/*", "/didi/imgs/flier/*", "/didi/imgs/taxi/*", "/didi/imgs/car/*", "/ShareSDK/com.sdu.didi.psnger/cache/images/*", "/VIPOneCar/*", "/DHF/*", "/soufun/res/cache/splash_ads/*", "/ShareSDK/com.sinovatech.unicom.ui/cache/image/*", "/nrcolortouch/lightapps/discovery/scripts/images/drawable-xhdpi/*", "/nrcolortouch/lightapps/discovery/scripts/images/drawable-xxhdpi/*", "/baidu/*", "/baidu/hybird/com.baidu.netdisk/icons/*", "/baidu/hybird/noti_icons/*", "/baidu/pushservice/Iappicons/*", "/BaiduMap/cache/OUA/assets/place/img/*", "/BaiduMap/cache/OUB/assets/place/img/openmap/*", "/qqmusic/qbiz/html5/243/imgcache.qq.com/mediastyle/mobile/ipad/img/*", "/qqmusic/qbiz/html5/243/imgcache.qq.com/mediastyle/mobile/ipad/base64/*", "/sogou/sga/dimcode/*", "/360Browser/download/search_nav/*", "/appmanager/*", "/CSDN/*", "/HRAndroidFrame/linksImage/*", "/HRAndroidFrame/docDetail/*", "/HRAndroidFrame/headImage/*", "/Letv/share/*"};
    static Set<String> sSharePrivilege = new HashSet(5);
    static String[] sWhiteListProp = new String[]{"/system/media/Pre-loaded/Pictures/", "/system/media/Pre-loaded/Video/", "/Pictures/Screenshots/", "/Pictures/Recover/", "/Huawei Share/", "/WLAN 直连/", "/WLAN Direct/", "/Bluetooth/", "/Download/", "/Pictures/", "/Video/", "/sina/weibo/save/", "/sina/weibo/weibo/", "/tencent/MicroMsg/WeiXin/", "/CloudPicture/", "/MagazineUnlock/", "/Movies/", "/DCIM/GroupRecorder/", Constant.CAMERA_PATH + "/DocRectify/"};

    private Prop4g() {
    }

    static {
        sSharePrivilege.add("com.huawei.gallery.photoshare.ui.ShareToCloudAlbumActivity");
    }
}
