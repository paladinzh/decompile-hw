package com.huawei.permission.runtime;

import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustTrashConst;
import java.util.Arrays;
import java.util.List;

public class TrustedApps {
    static boolean isTrustedApp(String pkg) {
        return getTrustedApps().contains(pkg);
    }

    private static List<String> getTrustedApps() {
        return Arrays.asList(new String[]{"com.huawei.locationsharing", "com.android.apps.tag", "com.android.bluetooth", "com.android.browser", "com.android.calendar", "com.android.cellbroadcastreceiver", HsmStatConst.CONTACTS_PACKAGE_NAME, "com.android.defcontainer", "com.android.deskclock", "com.android.dreams.phototable", "com.android.email", "com.android.exchange", HwCustTrashConst.GALLERY_DEFAULT_PKG_NAME, "com.android.launcher3", "com.android.htmlviewer", "com.android.hwmirror", "com.android.huawei.smartkey", "com.android.inputmethod.latin", "com.android.mediacenter", "com.android.mms", "com.android.mms.service", "com.android.nfc", "com.android.providers.contacts", "com.android.sharedstoragebackup", "com.android.soundrecorder", "com.baidu.input_huawei", "com.baidu.searchbox_huawei", "com.example.android.notepad", "com.gearedu.honorstudy.huawei", "com.google.android.gms", "com.google.android.marvin.talkback", "com.huawei.android.backup", "com.huawei.android.chr", "com.huawei.android.FloatTasks", "com.huawei.android.FMRadio", "com.huawei.android.hwouc", "com.huawei.android.hwpay", "com.huawei.android.launcher", "com.huawei.android.powermonitor", "com.huawei.android.pushagent", "com.huawei.android.remotecontrol", "com.huawei.android.remotecontrol ", "com.huawei.android.thememanager", "com.huawei.android.totemweather", "com.huawei.android.wfdft", "com.huawei.appmarket", "com.huawei.ca", "com.huawei.camera", "com.huawei.compass", "com.huawei.fans", "com.huawei.floatMms", "com.huawei.gamebox", "com.huawei.health", "com.huawei.hicare", "com.huawei.hidisk", "com.huawei.hisuite", "com.huawei.hwid", "com.huawei.hwireader", "com.huawei.hwstartupguide", "com.huawei.hwvplayer", "com.huawei.hwvplayer.youku", "com.huawei.ims", "com.huawei.intelligent", "com.huawei.kidsmode", "com.huawei.kidsmode.kidspaint", "com.huawei.KoBackup", "com.huawei.lives", "com.huawei.magnifier", "com.huawei.phonediagnose", HsmStatConst.PHONE_SERVICE_PACKAGE_NAME, "com.huawei.rcsserviceapplication", "com.huawei.remoteassistant", "com.huawei.screenrecorder", "com.huawei.vassistant", "com.huawei.vdrive", "com.huawei.wallet", "com.huawei.yellowpage", "com.iflytek.speechsuite", "com.nqmobile.antivirus20.hw", "com.nuance.swype.emui", "com.qeexo.smartshot", "com.svox.pico", "com.huawei.HwMultiScreenShot", "com.huawei.cloudwifi", "com.huawei.hiskytone", "com.huawei.skytone", "com.vmall.client", "com.huawei.scanner"});
    }
}
