package com.huawei.keyguard.support.magazine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler.Callback;
import android.os.Message;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.R$drawable;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.data.MusicInfo;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.support.magazine.MagazineWallpaper.IMagazineLoadCallback;
import com.huawei.keyguard.theme.KeyguardTheme;
import com.huawei.keyguard.util.BitmapUtils;
import com.huawei.keyguard.util.FpUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.util.OsUtils;
import com.huawei.keyguard.util.WallpaperUtils;
import com.huawei.keyguard.view.WallpaperPagerAdapter;
import com.huawei.keyguard.view.effect.bokeh.BokehDrawable;
import com.huawei.openalliance.ad.inter.constant.EventType;

public class KeyguardWallpaper implements Callback {
    private static final ColorDrawable sDefaultDrawer = new ColorDrawable(-65536);
    private static KeyguardWallpaper sKeyguardWallpaper = null;
    private boolean mBlockUIUpdate = false;
    private Context mContext;
    private Drawable mCurrentWallPaper = null;
    private boolean mFailWhenLoadMagazineWallpaper = false;
    private IMagazineLoadCallback mMagazineLoader = new IMagazineLoadCallback() {
        public void onMagazinePicLoaded(BigPicture pic) {
            boolean z = true;
            if (KeyguardWallpaper.this.mWallPaperType == 2) {
                KeyguardWallpaper keyguardWallpaper = KeyguardWallpaper.this;
                if (!(pic == null || pic.getBitmap() == null)) {
                    z = false;
                }
                keyguardWallpaper.mFailWhenLoadMagazineWallpaper = z;
                if (KeyguardWallpaper.this.mFailWhenLoadMagazineWallpaper) {
                    KeyguardWallpaper.this.aysncLoadSimpleWallpaper(false);
                } else {
                    int loadType;
                    KeyguardWallpaper.this.updateWallpaper(pic.getBokehDrawable(KeyguardWallpaper.this.mContext));
                    WallpaperPagerAdapter adapter = WallpaperPagerAdapter.getInst(KeyguardWallpaper.this.mContext, null);
                    if (KeyguardWallpaper.this.mMagazineWallpaper == null) {
                        adapter.getClass();
                        loadType = 1;
                    } else if (KeyguardWallpaper.this.mMagazineWallpaper.isContentChange()) {
                        adapter.getClass();
                        loadType = 3;
                    } else {
                        adapter.getClass();
                        loadType = 1;
                    }
                    HwLog.d("KGWallpaper", "loadType:" + loadType);
                    adapter.loadPagerImageView(loadType);
                    if (KeyguardWallpaper.this.mMagazineWallpaper != null) {
                        KeyguardWallpaper.this.mMagazineWallpaper.setContentChange(false);
                    }
                }
            }
        }
    };
    private MagazineWallpaper mMagazineWallpaper;
    private Runnable mStatusbarUpdater = new Runnable() {
        public void run() {
            if (HwKeyguardUpdateMonitor.getInstance().isShowing() && (KeyguardWallpaper.this.mCurrentWallPaper instanceof BokehDrawable)) {
                HwKeyguardPolicy.getInst().updateKeyguardStatusbarColor(((BokehDrawable) KeyguardWallpaper.this.mCurrentWallPaper).getColorInfo());
            }
        }
    };
    private KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() {
        public void onScreenTurnedOn() {
            if (KeyguardWallpaper.this.mWallPaperType == 2 && KeyguardWallpaper.this.mMagazineWallpaper != null && (((HwUnlockUtils.isSecure(KeyguardWallpaper.this.mContext) && FpUtils.isKeyguardLocked(KeyguardWallpaper.this.mContext)) || !HwUnlockUtils.isSecure(KeyguardWallpaper.this.mContext)) && FpUtils.isScreenOn(KeyguardWallpaper.this.mContext))) {
                BigPictureInfo bigPic = KeyguardWallpaper.this.mMagazineWallpaper.getPictureInfo(0);
                HwLockScreenReporter.reportAdEvent(KeyguardWallpaper.this.mContext, bigPic, EventType.IMPRESSION);
                if (bigPic != null) {
                    HwLockScreenReporter.statReport(KeyguardWallpaper.this.mContext, 1001, "{picture:" + bigPic.getPicName() + "}");
                }
            }
            if (KeyguardWallpaper.this.mUserChangeStyle != -10000) {
                GlobalContext.getUIHandler().postDelayed(new Runnable() {
                    public void run() {
                        KeyguardWallpaper.this.mUserChangeStyle = -10000;
                        HwLog.w("KGWallpaper", "let wallpaper show normally");
                    }
                }, 500);
                return;
            }
            KeyguardWallpaper.this.resetBlockState(true);
            HwLockScreenReporter.reportMagazinePictureInfo(KeyguardWallpaper.this.mContext, 162, 0);
        }

        public void onFinishedGoingToSleep(int why) {
            super.onFinishedGoingToSleep(why);
            KeyguardWallpaper.this.resetBlockState(true);
        }

        public void onUserSwitching(int userId) {
            GlobalContext.getBackgroundHandler().post(new Runnable() {
                public void run() {
                    if (HwFyuseUtils.isSupport3DFyuse()) {
                        HwLog.w("KGWallpaper", "onUserSwitching record enable status");
                        HwFyuseUtils.recordMagazineEnableStatus(HwFyuseUtils.isMagazineSwitchEnale(KeyguardWallpaper.this.mContext));
                    }
                }
            });
        }

        public void onStartedGoingToSleep(int why) {
            if (HwFyuseUtils.isSupport3DFyuse()) {
                HwFyuseUtils.recordMagazineEnableStatus(HwFyuseUtils.isMagazineSwitchEnale(KeyguardWallpaper.this.mContext));
            }
            if (KeyguardWallpaper.this.needSwitchWallperWhenScreenOff()) {
                HwLog.w("KGWallpaper", "onStartedGoingToSleep switch to next Magazine");
                KeyguardWallpaper.this.mMagazineWallpaper.switchPicture(1, KeyguardWallpaper.this.mMagazineLoader);
                KeyguardWallpaper.this.mBlockUIUpdate = true;
                return;
            }
            HwLog.w("KGWallpaper", "onStartedGoingToSleep skip switch Magazine");
            if (FpUtils.isKeyguardLocked(KeyguardWallpaper.this.mContext) && KeyguardWallpaper.this.mWallPaperType == 2 && KeyguardWallpaper.this.mMagazineWallpaper != null) {
                BigPictureInfo bigPic = KeyguardWallpaper.this.mMagazineWallpaper.getPictureInfo(0);
                HwLockScreenReporter.reportAdEvent(KeyguardWallpaper.this.mContext, bigPic, EventType.SHOWEND);
                if (bigPic != null) {
                    HwLockScreenReporter.statReport(KeyguardWallpaper.this.mContext, 1002, "{picture:" + bigPic.getPicName() + "}");
                }
            }
        }
    };
    private int mUserChangeStyle = -10000;
    private int mWallPaperType = 2;
    private Runnable mWallpaperLoader = new Runnable() {
        public void run() {
            try {
                KeyguardWallpaper.this.getWallpaper();
            } catch (NullPointerException e) {
                if (KeyguardWallpaper.this.mWallPaperType == 100) {
                    HwLog.w("KGWallpaper", "HwKeyguardPolicy maybe not inited");
                    GlobalContext.getBackgroundHandler().postDelayed(this, 500);
                    return;
                }
                HwLog.e("KGWallpaper", "getWallpaper failed", e);
            } catch (Exception e2) {
                HwLog.e("KGWallpaper", "getWallpaper failed", e2);
            }
        }
    };

    public static KeyguardWallpaper getInst(Context context) {
        KeyguardWallpaper tmp;
        boolean doRegist = false;
        synchronized (KeyguardWallpaper.class) {
            if (sKeyguardWallpaper == null) {
                doRegist = true;
                sKeyguardWallpaper = new KeyguardWallpaper(context);
            }
            tmp = sKeyguardWallpaper;
        }
        if (doRegist) {
            AppHandler.addListener(tmp);
            KeyguardUpdateMonitor.getInstance(context).registerCallback(tmp.mUpdateCallback);
            GlobalContext.getBackgroundHandler().post(tmp.mWallpaperLoader);
        }
        return tmp;
    }

    private KeyguardWallpaper(Context context) {
        this.mContext = context;
        this.mMagazineWallpaper = MagazineWallpaper.getInst(context);
    }

    public void switchMagazine(int type) {
        this.mWallPaperType = 2;
        if (MagazineUtils.isUserCustomedWallpaper(this.mContext)) {
            setUserCustomedWallpaper(this.mContext, false, true);
            MagazineUtils.setAutoSwitchMagazine(this.mContext, false, "user switch to magazine");
        }
        this.mBlockUIUpdate = false;
        this.mUserChangeStyle = -10000;
        this.mMagazineWallpaper.switchPicture(type, this.mMagazineLoader);
    }

    public int getCurruntType() {
        return this.mWallPaperType;
    }

    public Drawable getCurrentWallPaper() {
        return this.mCurrentWallPaper;
    }

    private void updateWallpaper(Drawable newWallpaper) {
        String hitInfo = "not change";
        if (!BitmapUtils.isSameDrawable(newWallpaper, this.mCurrentWallPaper)) {
            this.mCurrentWallPaper = newWallpaper;
            if (this.mBlockUIUpdate) {
                hitInfo = "block update ui";
            } else {
                hitInfo = "send update message";
                AppHandler.sendMessage(21, this.mWallPaperType, 0, this.mCurrentWallPaper);
            }
            updateShaderColor();
        }
        HwLog.w("KGWallpaper", "update Wallpaper and " + hitInfo);
    }

    public Drawable getWallpaper() {
        int type = KeyguardTheme.getInst().getLockStyle();
        if (!(type == 7 || type == 5 || type == 6 || type == 8 || !MagazineUtils.isUserCustomedWallpaper(this.mContext))) {
            type = 100;
        }
        Drawable newWallpaper = loadWallpaper(type);
        if (newWallpaper != null) {
            updateWallpaper(newWallpaper);
        }
        return newWallpaper;
    }

    private Drawable loadWallpaper(int type) {
        if (type == 2) {
            if (this.mMagazineWallpaper.getPictureSize() == 0) {
                if (this.mMagazineWallpaper.hasInitialized() || HwKeyguardUpdateMonitor.getInstance(this.mContext).isFirstTimeStartupAndEncrypted()) {
                    type = 1;
                } else {
                    HwLog.w("KGWallpaper", "wait for Magazine loaded later");
                    return null;
                }
            }
        } else if (type == 7 || (type == 8 && MusicInfo.getInst().isPlaying())) {
            HwLog.v("KGWallpaper", "Load music wallpaper");
            this.mWallPaperType = type;
            return BokehDrawable.create(this.mContext, MusicInfo.getInst().getBlurBitmap(this.mContext), false);
        } else if (this.mCurrentWallPaper != null && type == this.mWallPaperType) {
            return this.mCurrentWallPaper;
        }
        int oldType = this.mWallPaperType;
        this.mWallPaperType = type;
        HwLog.d("KGWallpaper", "get for type. " + type + " from " + oldType);
        if (type == 6) {
            return aysncLoadSimpleWallpaper(true);
        }
        if (type == 5) {
            return this.mContext.getResources().getDrawable(R$drawable.lockscreen_background_extremepower);
        }
        Bitmap bmpWallpaper = null;
        if (type == 100) {
            bmpWallpaper = HwKeyguardPolicy.getInst().getLockScreenWallpaper();
        } else if (type == 2) {
            if (this.mUserChangeStyle == OsUtils.getCurrentUser()) {
                HwLog.w("KGWallpaper", "User has changed style");
            } else {
                bigPic = this.mMagazineWallpaper.getWallPaper(0, this.mMagazineLoader);
                if (bigPic != null && bigPic.isSameDrawable(this.mCurrentWallPaper, this.mContext)) {
                    return this.mCurrentWallPaper;
                }
                bmpWallpaper = bigPic == null ? null : HwUnlockUtils.isLandscape(this.mContext) ? bigPic.getLandBitmap() : bigPic.getBitmap();
                if (bmpWallpaper == null) {
                    return null;
                }
                BokehDrawable drawable = BokehDrawable.create(this.mContext, bmpWallpaper, hasFlareOnWallpaper());
                drawable.setBitmapPath(bigPic.getPicPath());
                return drawable;
            }
        } else if (type == 8) {
            if (MagazineUtils.isUserCustomedWallpaper(this.mContext)) {
                bmpWallpaper = BitmapUtils.blurBitmap(this.mContext, HwKeyguardPolicy.getInst().getLockScreenWallpaper(), null, 25.0f);
            } else if (this.mCurrentWallPaper == null) {
                Bitmap bitmap;
                if (MagazineUtils.isMagazineLockStyle()) {
                    bigPic = this.mMagazineWallpaper.getWallPaper(0, this.mMagazineLoader);
                    bitmap = bigPic == null ? null : bigPic.getBitmap();
                } else {
                    HwLog.d("KGWallpaper", "getKeyguardWallpaper");
                    bitmap = WallpaperUtils.getKeyguardWallpaper(this.mContext);
                }
                if (bitmap == null) {
                    HwLog.d("KGWallpaper", "get current wallpaper is null");
                    BitmapDrawable bmpDraw = (BitmapDrawable) WallpaperUtils.getThemeWallpaper(this.mContext);
                    if (bmpDraw != null) {
                        bitmap = bmpDraw.getBitmap();
                    } else {
                        HwLog.d("KGWallpaper", "can't get wallpaper!");
                        return null;
                    }
                }
                bmpWallpaper = BitmapUtils.blurBitmap(this.mContext, bitmap, null, 25.0f);
            } else {
                bmpWallpaper = BitmapUtils.blurBitmap(this.mContext, WallpaperUtils.drawableToBitmap(this.mCurrentWallPaper), null, 25.0f);
            }
        }
        if (bmpWallpaper != null) {
            return BokehDrawable.create(this.mContext, bmpWallpaper, hasFlareOnWallpaper());
        }
        if (oldType != 1 || this.mCurrentWallPaper == null) {
            HwLog.w("KGWallpaper", "Load simple wallpaper as default ");
            if (this.mUserChangeStyle != OsUtils.getCurrentUser()) {
                this.mWallPaperType = 1;
            }
            return aysncLoadSimpleWallpaper(false);
        }
        HwLog.w("KGWallpaper", "using old wallpaper");
        return this.mCurrentWallPaper;
    }

    public IMagazineLoadCallback getCallback() {
        return this.mMagazineLoader;
    }

    private boolean hasFlareOnWallpaper() {
        int lockType = KeyguardTheme.getInst().getLockStyle();
        boolean noFlare = (lockType == 7 || lockType == 5 || lockType == 4 || lockType == 3) ? true : lockType == 8;
        if (noFlare) {
            return false;
        }
        return true;
    }

    private Drawable aysncLoadSimpleWallpaper(final boolean desktopOnly) {
        GlobalContext.getBackgroundHandler().post(new Runnable() {
            public void run() {
                Bitmap bmp = desktopOnly ? WallpaperUtils.getKeyguardWallpaper(KeyguardWallpaper.this.mContext, true) : WallpaperUtils.getKeyguardWallpaper(KeyguardWallpaper.this.mContext);
                if (bmp == null) {
                    HwLog.w("KGWallpaper", "No wallpaper can be loaded");
                    KeyguardWallpaper.this.updateWallpaper(KeyguardWallpaper.sDefaultDrawer);
                    return;
                }
                KeyguardWallpaper.this.updateWallpaper(BokehDrawable.create(KeyguardWallpaper.this.mContext, bmp, KeyguardWallpaper.this.hasFlareOnWallpaper()));
            }
        });
        return null;
    }

    public void setUserCustomedWallpaper(Context context, boolean userAssigned, boolean needClearOld) {
        HwLog.w("KGWallpaper", "setUserAssignedPicture ." + userAssigned);
        MagazineUtils.setUserCustomedWallpaper(context, userAssigned);
        if (needClearOld) {
            this.mCurrentWallPaper = null;
        }
        GlobalContext.getBackgroundHandler().post(this.mWallpaperLoader);
    }

    private boolean needSwitchWallperWhenScreenOff() {
        boolean ret;
        if (this.mWallPaperType == 2 && MagazineUtils.isAutoSwitchMagazine(this.mContext, false) && !MagazineUtils.isUserCustomedWallpaper(this.mContext)) {
            ret = -10000 == this.mUserChangeStyle;
        } else {
            ret = false;
        }
        if (!ret && this.mWallPaperType == 2 && this.mFailWhenLoadMagazineWallpaper) {
            HwLog.w("KGWallpaper", "reload magazine wallpaper for last fail-op.");
            ret = true;
        }
        if (!ret) {
            HwLog.w("KGWallpaper", "skiped as " + this.mWallPaperType + " - " + this.mUserChangeStyle + "; " + MagazineUtils.isAutoSwitchMagazine(this.mContext, false) + " " + MagazineUtils.isUserCustomedWallpaper(this.mContext));
        }
        return ret;
    }

    private void resetBlockState(boolean notice) {
        if (notice && this.mBlockUIUpdate) {
            AppHandler.sendMessage(21, this.mWallPaperType, 0, this.mCurrentWallPaper);
        }
        this.mBlockUIUpdate = false;
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                if (Boolean.TRUE.equals(msg.obj)) {
                    if (msg.arg2 == 0) {
                        setUserCustomedWallpaper(this.mContext, false, true);
                        MagazineUtils.setAutoSwitchMagazine(this.mContext, true, "theme updated");
                    }
                    this.mUserChangeStyle = OsUtils.getCurrentUser();
                }
                this.mCurrentWallPaper = null;
                resetBlockState(false);
                GlobalContext.getBackgroundHandler().post(this.mWallpaperLoader);
                break;
            case 22:
                HwLog.w("KGWallpaper", "user set lockscreen wallpaper");
                this.mCurrentWallPaper = null;
                resetBlockState(false);
                setUserCustomedWallpaper(this.mContext, true, true);
                break;
            case 110:
                HwLog.w("KGWallpaper", "music state change");
                if (2 != MusicInfo.getInst().getPlayState()) {
                    GlobalContext.getBackgroundHandler().post(this.mWallpaperLoader);
                    break;
                }
                break;
        }
        return false;
    }

    public void updateShaderColor() {
        GlobalContext.getUIHandler().post(this.mStatusbarUpdater);
    }

    public boolean reloadPicturesAfterFirstLogin() {
        if (!this.mMagazineWallpaper.reloadPicturesAfterFirstLogin()) {
            return false;
        }
        HwLog.w("KGWallpaper", "reloadPicturesAfterFirstLogin: " + getWallpaper());
        return true;
    }
}
