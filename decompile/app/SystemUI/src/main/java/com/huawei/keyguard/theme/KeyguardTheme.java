package com.huawei.keyguard.theme;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.data.MusicInfo;
import com.huawei.keyguard.data.SportInfo;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.events.EventCenter;
import com.huawei.keyguard.events.EventCenter.IEventListener;
import com.huawei.keyguard.events.MusicMonitor;
import com.huawei.keyguard.support.magazine.MagazineWallpaper;
import com.huawei.keyguard.util.HwLog;

public class KeyguardTheme implements IEventListener {
    private static KeyguardTheme sInst = null;
    private HwThemeParser mParser;
    private int mStyleType;

    public static KeyguardTheme getInst() {
        Throwable th;
        synchronized (KeyguardTheme.class) {
            try {
                if (sInst != null) {
                    KeyguardTheme keyguardTheme = sInst;
                    return keyguardTheme;
                }
                KeyguardTheme tmpInst = new KeyguardTheme();
                try {
                    sInst = tmpInst;
                    EventCenter.getInst().listen(4, tmpInst);
                    return tmpInst;
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    private KeyguardTheme() {
        this.mParser = null;
        this.mStyleType = 0;
        this.mParser = HwThemeParser.getInstance();
        this.mParser.parseThemeFromXml();
        this.mStyleType = getLockStyleType(this.mParser.getStyle(), this.mParser.getDynamicPath());
    }

    public int getLockStyle() {
        return this.mStyleType;
    }

    public static boolean isFullTouchTheme(int type) {
        return 7 == type || 3 == type || 4 == type || 8 == type;
    }

    private static int getLockStyleType(String style, String dynPath) {
        if (SportInfo.getInst().getCurrentMode() == 1 && !KeyguardCfg.isExtremePowerSavingMode()) {
            return 8;
        }
        if (MusicInfo.getInst().needShowMusicView()) {
            return 7;
        }
        if (KeyguardCfg.isExtremePowerSavingMode()) {
            return 5;
        }
        if (KeyguardCfg.isSimpleModeOn()) {
            return 6;
        }
        if ("dynamic".equals(style) || ("slide".equals(style) && !TextUtils.isEmpty(dynPath))) {
            return 3;
        }
        if ("amazing".equals(style)) {
            return 4;
        }
        if ("magazine".equals(style)) {
            return 2;
        }
        if (isSlideStyle(style)) {
            return 1;
        }
        Log.w("KeyguardTheme", "Unknow LockStyle: " + style);
        return 1;
    }

    private static final boolean isSlideStyle(String style) {
        return ("slide".equals(style) || "potter".equals(style)) ? true : "google".equals(style);
    }

    public boolean checkStyle(Context context, boolean forceUpdate, boolean userSwitched) {
        int i = 0;
        if (!forceUpdate) {
            MusicMonitor.getInst(context).freshState();
        }
        String oldStyle = this.mParser.getStyle();
        this.mParser.parseThemeFromXml();
        String newStyle = this.mParser.getStyle();
        if (!TextUtils.isEmpty(oldStyle) && !TextUtils.equals(oldStyle, newStyle)) {
            forceUpdate = true;
        } else if (forceUpdate) {
            Log.w("KeyguardTheme", "maybe change lockstyle same as old");
        }
        int newType = getLockStyleType(newStyle, this.mParser.getDynamicPath());
        if (this.mStyleType == newType && !forceUpdate) {
            return false;
        }
        HwLog.i("KeyguardTheme", "Style changed : " + this.mStyleType + " -> " + newType);
        this.mStyleType = newType;
        if (userSwitched) {
            i = 1;
        }
        AppHandler.sendMessage(1, newType, i, Boolean.valueOf(forceUpdate));
        return true;
    }

    public boolean onReceive(Context context, Intent intent) {
        if (intent == null) {
            return false;
        }
        checkStyle(context, true, false);
        MagazineWallpaper mMagazineWallpaper = MagazineWallpaper.getInst(context);
        if ("magazine".equals(this.mParser.getStyle()) && !mMagazineWallpaper.hasInitialized() && mMagazineWallpaper.getPictureSize() == 0) {
            mMagazineWallpaper.initDatas();
        }
        return false;
    }

    public boolean showGgStatusView() {
        return this.mStyleType == 1 || this.mStyleType == 5 || this.mStyleType == 6;
    }
}
