package com.huawei.powergenie.core.app;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.huawei.powergenie.api.ICoreContext;
import com.huawei.powergenie.api.IDeviceState;
import com.huawei.powergenie.api.IScenario;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.core.policy.DBWrapper;
import com.huawei.powergenie.core.policy.SharedPref;
import com.huawei.powergenie.debugtest.DbgUtils;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

public final class AppTypeRecognise {
    private static final HashMap<Integer, ArrayList<String>> mAppsTypeList = new HashMap();
    private static final ArrayList<String> mClockAppList = new ArrayList<String>() {
        {
            add("com.tencent.qqcalendar");
            add("com.icloverlabs.idrinkwaterapp");
            add("com.leixun.nvshen");
            add("com.getup.activity");
            add("com.when.coco");
            add("cn.etouch.ecalendar");
            add("cn.etouch.ecalendar.life");
            add("oms.mmc.app.almanac_inland");
            add("net.icycloud.tomato");
            add("com.lilysgame.calendar");
            add("com.keenvim.cnCalendar");
            add("com.secretlisa.sleep");
        }
    };
    private static final ArrayList<String> mEbookAppList = new ArrayList<String>() {
        {
            add("com.chaozh.iReaderFree");
            add("com.chaozh.iReaderFree15");
            add("com.netease.pris");
            add("com.sogou.novel");
            add("flipboard.cn");
            add("com.snda.cloudary");
            add("com.tadu.android");
        }
    };
    private static final ArrayList<String> mImAppWhiteList = new ArrayList<String>() {
        {
            add("com.alibaba.mobileim");
            add("com.taobao.qianniu");
            add("com.jd.im.seller");
            add("jd.dd.seller");
            add("com.facebook.orca");
            add("com.immomo.momo");
            add("com.eg.android.AlipayGphone");
            add("com.tencent.mm");
            add("com.tencent.mobileqq");
            add("im.yixin");
            add("com.whatsapp");
            add("com.snapchat.android");
            add("jp.naver.line.android");
            add("com.imo.android.imoim");
        }
    };
    private static final ArrayList<String> mInputMethodList = new ArrayList();
    private static final ArrayList<String> mLocationProviderList = new ArrayList();
    private static final ArrayList<String> mNewsClientList = new ArrayList<String>() {
        {
            add("com.ss.android.article.news");
            add("com.netease.newsreader.activity");
            add("com.tencent.news");
            add("com.sina.news");
            add("com.ifeng.news2");
            add("com.sohu.newsclient");
            add("cn.cntvnews");
            add("com.hipu.yidian");
            add("com.myzaker.ZAKER_Phone");
            add("com.baidu.news");
            add("com.wondertek.paper");
            add("cn.com.sina.sports");
            add("com.tencent.reading");
        }
    };
    private static final ArrayList<String> mNotImAppList = new ArrayList<String>() {
        {
            add("com.qzone");
            add("com.baidu.netdisk");
            add("com.dewmobile.kuaiya");
            add("com.qq.qcloud");
            add("com.youdao.note");
            add("com.evernote");
            add("com.ylmf.androidclient");
            add("com.cn21.ecloud");
            add("com.lenovo.anyshare");
            add("com.sina.VDisk");
            add("com.hoperun.intelligenceportal");
            add("cn.andouya");
            add("com.google.android.apps.plus");
            add("com.facishare.fs");
            add("com.google.android.apps.docs");
            add("com.sdu.didi.psnger");
            add("com.ubercab");
            add("com.szzc.ucar.pilot");
            add("com.sdu.didi.gsui");
            add("com.ubercab.driveri");
            add("com.yongche.android");
            add("com.didapinche.booking");
            add("com.funcity.taxi.passenger");
            add("com.youku.phone");
        }
    };
    private static final ArrayList<String> mNotScreenLockList = new ArrayList<String>() {
        {
            add("com.tencent.qqpimsecure");
            add("com.qihoo360.mobilesafe.opti.powerctl");
            add("com.qihoo360.mobilesafe");
            add("cn.opda.a.phonoalbumshoushou");
            add("com.moji.mjweather");
            add("com.tencent.news");
            add("org.cocos2d.fishingjoy3");
            add("com.codoon.gps");
            add("com.example.businesshall");
        }
    };
    private static final ArrayList<String> mNotSmsAppList = new ArrayList<String>() {
        {
            add("com.tencent.qqpimsecure");
            add("com.qihoo360.mobilesafe");
            add("cn.opda.a.phonoalbumshoushou");
            add("com.lbe.security");
        }
    };
    private static final ArrayList<String> mShopAppsList = new ArrayList<String>() {
        {
            add("com.taobao.taobao");
            add("com.sankuai.meituan");
            add("com.dianping.v1");
            add("com.tmall.wireless");
            add("com.jingdong.app.mall");
            add("com.achievo.vipshop");
            add("com.nuomi");
            add("com.suning.mobile.ebuy");
            add("com.mogujie");
            add("com.jm.android.jumei");
            add("com.koudai.weishop");
            add("com.fanli.android.apps");
            add("com.dangdang.buy2");
            add("cn.amazon.mShop.android");
            add("com.husor.beibei");
            add("com.thestore.main");
            add("com.taobao.fleamarket");
            add("com.gome.eshopnew");
        }
    };
    private static final HashMap<String, Integer> mTmpIMFrontSendMsgCount = new HashMap();
    private static final HashMap<String, Integer> mTmpIMSendMsgCount = new HashMap();
    private static final HashMap<String, Integer> mTmpSmsSendMsgCount = new HashMap();
    private static final ArrayList<Integer> mTypeList = new ArrayList<Integer>() {
        {
            add(Integer.valueOf(1));
            add(Integer.valueOf(2));
            add(Integer.valueOf(3));
            add(Integer.valueOf(4));
            add(Integer.valueOf(5));
            add(Integer.valueOf(6));
            add(Integer.valueOf(7));
            add(Integer.valueOf(8));
            add(Integer.valueOf(9));
            add(Integer.valueOf(10));
            add(Integer.valueOf(11));
            add(Integer.valueOf(12));
            add(Integer.valueOf(13));
            add(Integer.valueOf(14));
            add(Integer.valueOf(15));
            add(Integer.valueOf(16));
            add(Integer.valueOf(17));
            add(Integer.valueOf(18));
            add(Integer.valueOf(19));
            add(Integer.valueOf(20));
        }
    };
    private static final ArrayList<String> mVideoAppsList = new ArrayList<String>() {
        {
            add("com.duowan.mobile");
            add("air.tv.douyu.android");
            add("air.fyzb3");
            add("com.duowan.kiwi");
            add("org.fungo.fungolive");
            add("com.meelive.ingkee");
            add("android.zhibo8");
            add("com.longzhu.tga");
            add("cn.myhug.baobao");
            add("com.huajiao");
            add("com.melot.meshow");
            add("cn.v6.sixrooms");
            add("com.tencent.now");
            add("com.youku.phone");
            add("cn.vcinema.cinema");
            add("com.maimiao.live.tv");
            add("com.panda.videoliveplatform");
            add("com.huomaotv.mobile");
            add("tv.xiaoka.live");
            add("com.youku.crazytogether");
            add("com.kugou.fanxing");
            add("com.busap.myvideo");
            add("com.panda.videoliveplatform");
            add("com.kascend.chushou");
            add("com.gameabc.zhanqiAndroid");
            add("com.maimiao.live.tv");
            add("com.tencent.qgame");
            add("com.netease.cc");
            add("com.qike.telecast");
            add("com.smile.gifmaker");
            add("com.yixia.videoeditor");
            add("com.yixia.xiaokaxiu");
            add("cn.colorv");
            add("com.ss.android.ugc.aweme");
            add("com.ss.android.ugc.live");
            add("com.funinhand.weibo");
            add("com.hunantv.imgo.activity");
            add("com.baidu.video");
            add("dopool.player");
            add("com.erdo.android.FJDXCartoon");
            add("tv.acfundanmaku.video");
            add("com.fungo.loveshow.yuntu");
            add("com.cmcc.cmvideo");
            add("com.tiantiankan.ttkvod");
            add("com.unicom.woshipin");
            add("com.chaojishipin.sarrs");
            add("com.meitu.meipaimv");
            add("com.yixia.videoeditor");
        }
    };
    private ActivityManager mActivityManager;
    private final AppManager mAppManager;
    private Handler mAppTypeHandler = null;
    private ArrayList<String> mAudioFeatureList = new ArrayList();
    private ArrayList<String> mBrowserFeatureList = new ArrayList();
    private ArrayList<String> mBrowserMimeFeatureList = new ArrayList();
    private ArrayList<String> mClockFeatureList = new ArrayList();
    private Context mContext;
    private String mCurScrLockPkg = null;
    private final DBWrapper mDBWrapper;
    private String mDefaultInputMethodId = null;
    private String mDefaultInputMethodPkg = null;
    private ArrayList<String> mEbookFeatureList = new ArrayList();
    private ArrayList<String> mEmailFeatureList = new ArrayList();
    private ArrayList<String> mGalleryActivityList = new ArrayList();
    private final ICoreContext mICoreContext;
    private final IDeviceState mIDeviceState;
    private final IScenario mIScenario;
    private ArrayList<String> mImFeatureList = new ArrayList();
    private ArrayList<String> mImageFeatureList = new ArrayList();
    private boolean mInitFromSysFlag = false;
    private final InputMethodManager mInputMethod;
    private int mInputStartTouchCount = 0;
    private boolean mIsFrontPkgSentMsg = false;
    private boolean mIsNeedCheckInputMethodType = false;
    private String mLastInputPkg = null;
    private long mLastInputStartTime = 0;
    private long mLastInputStartTxBytes = 0;
    private int mLastInputUid = 0;
    private ArrayList<String> mLauncherList = new ArrayList();
    private ArrayList<String> mMarketActivityList = new ArrayList();
    private ArrayList<String> mMmsSmsFeatureList = new ArrayList();
    private ArrayList<String> mNavigationFeatureList = new ArrayList();
    private ArrayList<String> mOfficeFeatureList = new ArrayList();
    private PackageManager mPackageManager;
    private ArrayList<String> mPdfFeatureList = new ArrayList();
    private ArrayList<String> mScrLockPkgList = new ArrayList();
    private ArrayList<String> mSipFeatureList = new ArrayList();
    private ArrayList<String> mSmsFeatureList = new ArrayList();
    private long mSmsMsgChangedTime = 0;
    private final ContentObserver mSmsObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.i("AppTypeRecognise", "sms change !");
            AppTypeRecognise.this.mSmsMsgChangedTime = SystemClock.elapsedRealtime();
        }
    };
    private ArrayList<String> mVideoFeatureList = new ArrayList();

    private final class AppTypeHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    if (!AppTypeRecognise.this.recogniseSmsBySendMsg((String) msg.obj, (long) msg.arg1) && AppTypeRecognise.this.recogniseIMBySendMsg((String) msg.obj, (long) msg.arg2)) {
                        return;
                    }
                    return;
                case 101:
                    if (AppTypeRecognise.this.mLastInputUid > 0) {
                        AppTypeRecognise.this.mLastInputStartTxBytes = TrafficStats.getUidTxBytes(AppTypeRecognise.this.mLastInputUid);
                        AppTypeRecognise.this.mAppTypeHandler.sendMessageDelayed(AppTypeRecognise.this.mAppTypeHandler.obtainMessage(101), 1500);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    protected AppTypeRecognise(ICoreContext context, AppManager appManager) {
        this.mICoreContext = context;
        this.mAppManager = appManager;
        this.mDBWrapper = new DBWrapper(context.getContext());
        this.mIDeviceState = (IDeviceState) context.getService("device");
        this.mContext = this.mICoreContext.getContext();
        this.mPackageManager = this.mContext.getPackageManager();
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mInputMethod = (InputMethodManager) this.mContext.getSystemService("input_method");
        this.mIScenario = (IScenario) context.getService("scenario");
        this.mAppTypeHandler = new AppTypeHandler();
        initAppType();
        this.mContext.getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, this.mSmsObserver, -1);
    }

    protected boolean handleAppFront(PowerAction action) {
        return true;
    }

    protected void handlePackageChange(boolean isAdded, String pkgName) {
        if (isAdded) {
            mInputMethodList.clear();
            refreshAppList();
            recogniseAppType(pkgName);
            return;
        }
        removeAppFromList(pkgName);
    }

    protected void handleScreenState(boolean isScrOn) {
        if (!(!isScrOn || this.mCurScrLockPkg == null || this.mCurScrLockPkg.equals(this.mIScenario.getFrontPkg()))) {
            removeAppFromList(this.mCurScrLockPkg);
            this.mCurScrLockPkg = null;
        }
        this.mAppTypeHandler.removeMessages(101);
    }

    protected void handleAppFrontEvent(String frontApp) {
        if (this.mIDeviceState.isScreenOff()) {
            scrLockRecognise(frontApp);
        }
        this.mIsFrontPkgSentMsg = false;
    }

    protected void handleScreenUnlock() {
        if (this.mIsNeedCheckInputMethodType) {
            Log.i("AppTypeRecognise", "check inputmethod type when scr unlock...");
            checkInputMethodType();
            this.mIsNeedCheckInputMethodType = false;
        }
    }

    protected void handleInputStart() {
        this.mLastInputPkg = this.mIScenario.getFrontPkg();
        if (this.mLastInputPkg != null && needRecogniseIMSms(this.mLastInputPkg)) {
            this.mLastInputStartTime = SystemClock.elapsedRealtime();
            this.mLastInputUid = this.mAppManager.getUidByPkg(this.mLastInputPkg);
            this.mInputStartTouchCount = this.mIDeviceState.getTouchCount();
            this.mAppTypeHandler.sendMessageDelayed(this.mAppTypeHandler.obtainMessage(101), 1000);
            Log.i("AppTypeRecognise", "input start front pkg: " + this.mLastInputPkg);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void handleInputEnd() {
        this.mAppTypeHandler.removeMessages(101);
        if (this.mLastInputPkg != null && needRecogniseIMSms(this.mLastInputPkg)) {
            long now = SystemClock.elapsedRealtime();
            long inputDuraion = now - this.mLastInputStartTime;
            int touchCount = this.mIDeviceState.getTouchCount() - this.mInputStartTouchCount;
            Log.d("AppTypeRecognise", "input end-> front pkg: " + this.mLastInputPkg + " duraion:" + inputDuraion + " touchs:" + touchCount);
            if (inputDuraion <= 30000 && touchCount >= 10 && !recogniseSmsBySendMsg(this.mLastInputPkg, this.mLastInputStartTime) && !recogniseIMBySendMsg(this.mLastInputPkg, this.mLastInputStartTxBytes)) {
                this.mAppTypeHandler.sendMessageDelayed(this.mAppTypeHandler.obtainMessage(100, (int) now, (int) TrafficStats.getUidTxBytes(this.mLastInputUid), this.mLastInputPkg), 1500);
            }
        }
    }

    private boolean needRecogniseIMSms(String inputFrontPkg) {
        if (mNotImAppList.contains(inputFrontPkg) || mNotSmsAppList.contains(inputFrontPkg)) {
            Log.i("AppTypeRecognise", "input front pkg: " + inputFrontPkg + " is not im or sms");
            return false;
        } else if (this.mAppManager.isIgnoreGpsApp(inputFrontPkg)) {
            Log.i("AppTypeRecognise", "input front pkg: " + inputFrontPkg + " is gps ignore app");
            return false;
        } else if ((-1 == getAppType(inputFrontPkg) || mTmpSmsSendMsgCount.containsKey(inputFrontPkg) || mTmpIMSendMsgCount.containsKey(inputFrontPkg) || mTmpIMFrontSendMsgCount.containsKey(inputFrontPkg)) && UserHandle.getAppId(this.mAppManager.getUidByPkg(inputFrontPkg)) >= 10000) {
            return true;
        } else {
            return false;
        }
    }

    private boolean recogniseSmsBySendMsg(String lastInputPkg, long startTime) {
        if (lastInputPkg == null || startTime <= 0) {
            return true;
        }
        if (this.mSmsMsgChangedTime < startTime || this.mSmsMsgChangedTime - startTime > 2000) {
            return false;
        }
        Integer count = (Integer) mTmpSmsSendMsgCount.get(lastInputPkg);
        if (count != null) {
            HashMap hashMap = mTmpSmsSendMsgCount;
            int intValue = count.intValue() + 1;
            count = Integer.valueOf(intValue);
            hashMap.put(lastInputPkg, Integer.valueOf(intValue));
            if (count.intValue() >= 5) {
                Log.i("AppTypeRecognise", "new sms write to db app: " + lastInputPkg);
                DBWrapper.updateAppTypeToDb(this.mContext, lastInputPkg, 2);
                mTmpSmsSendMsgCount.remove(lastInputPkg);
            }
        } else {
            mTmpSmsSendMsgCount.put(lastInputPkg, Integer.valueOf(1));
            addAppToTypeList(lastInputPkg, 2);
            this.mAppManager.dispatchStateAction(278);
        }
        Log.i("AppTypeRecognise", "new sms app: " + lastInputPkg + " count: " + count);
        if (DbgUtils.DBG_TIPS) {
            DbgUtils.sendNotification("new sms: " + lastInputPkg, "count: " + count);
        }
        return true;
    }

    private boolean recogniseIMBySendMsg(String lastInputPkg, long startTxBytes) {
        if (lastInputPkg == null) {
            return true;
        }
        if (!this.mIDeviceState.isNetworkConnected()) {
            return true;
        }
        long sentBytes = TrafficStats.getUidTxBytes(this.mAppManager.getUidByPkg(lastInputPkg)) - startTxBytes;
        if (sentBytes <= 0) {
            return false;
        }
        long totalSrcOnHours = this.mIDeviceState.getScrOnTotalDuration() / 3600000;
        int type = getAppType(lastInputPkg);
        if (-1 == type) {
            Integer count = (Integer) mTmpIMSendMsgCount.get(lastInputPkg);
            if (count != null) {
                HashMap hashMap = mTmpIMSendMsgCount;
                int intValue = count.intValue() + 1;
                count = Integer.valueOf(intValue);
                hashMap.put(lastInputPkg, Integer.valueOf(intValue));
                if (count.intValue() >= 5 && ((long) count.intValue()) > totalSrcOnHours) {
                    addAppToTypeList(lastInputPkg, 11);
                    this.mAppManager.dispatchStateAction(278);
                    mTmpIMSendMsgCount.remove(lastInputPkg);
                }
            } else {
                mTmpIMSendMsgCount.put(lastInputPkg, Integer.valueOf(1));
            }
            Log.i("AppTypeRecognise", "new im app: " + lastInputPkg + " sent bytes:" + sentBytes + " count: " + count + " scron hours:" + totalSrcOnHours);
        }
        if (!this.mIsFrontPkgSentMsg) {
            this.mIsFrontPkgSentMsg = true;
            Integer frontSendCount = (Integer) mTmpIMFrontSendMsgCount.get(lastInputPkg);
            if (frontSendCount != null) {
                hashMap = mTmpIMFrontSendMsgCount;
                intValue = frontSendCount.intValue() + 1;
                frontSendCount = Integer.valueOf(intValue);
                hashMap.put(lastInputPkg, Integer.valueOf(intValue));
                if (11 == type && frontSendCount.intValue() >= 5 && ((long) frontSendCount.intValue()) > totalSrcOnHours) {
                    Log.i("AppTypeRecognise", "new im write to db app: " + lastInputPkg);
                    DBWrapper.updateAppTypeToDb(this.mContext, lastInputPkg, 11);
                    mTmpIMFrontSendMsgCount.remove(lastInputPkg);
                }
                if (-1 == type && frontSendCount.intValue() >= 3 && ((long) frontSendCount.intValue()) > totalSrcOnHours) {
                    addAppToTypeList(lastInputPkg, 11);
                    this.mAppManager.dispatchStateAction(278);
                }
            } else {
                mTmpIMFrontSendMsgCount.put(lastInputPkg, Integer.valueOf(1));
            }
            Log.i("AppTypeRecognise", "new im app: " + lastInputPkg + " sent bytes:" + sentBytes + " front send count: " + frontSendCount + " scron hours:" + totalSrcOnHours);
        }
        if (DbgUtils.DBG_TIPS) {
            DbgUtils.sendNotification("new im: " + lastInputPkg, " sent bytes:" + sentBytes);
        }
        return true;
    }

    private void initAppType() {
        if (SharedPref.getSettings(this.mContext, "init_apps_finish", false)) {
            Log.i("AppTypeRecognise", "init app type from DB...");
            initFromDB();
        } else {
            Log.i("AppTypeRecognise", "init app type from system...");
            initFromSystem();
        }
        Log.i("AppTypeRecognise", "init app type okey...");
    }

    private void initFromDB() {
        synchronized (mAppsTypeList) {
            for (Integer type : mTypeList) {
                DBWrapper dBWrapper = this.mDBWrapper;
                ArrayList<String> appList = DBWrapper.getAppTypeFromDb(this.mContext, type.intValue());
                if (appList != null) {
                    mAppsTypeList.put(type, appList);
                }
            }
        }
        this.mScrLockPkgList = getScreenLockList(true);
    }

    private void initFromSystem() {
        ArrayList<String> allAppsList = this.mAppManager.getAllApps(this.mContext);
        if (allAppsList != null && allAppsList.size() > 0) {
            this.mIsNeedCheckInputMethodType = true;
            mInputMethodList.clear();
            refreshAppList();
            this.mInitFromSysFlag = true;
            loadAppsType();
            for (String pkgName : allAppsList) {
                recogniseAppType(pkgName);
            }
            this.mInitFromSysFlag = false;
            clearAppsType();
        }
    }

    private void refreshAppList() {
        this.mLauncherList = getLauncherApps();
        this.mScrLockPkgList = getScreenLockList(true);
    }

    private void loadAppsType() {
        this.mClockFeatureList = getClockFeature(null);
        this.mBrowserFeatureList = getBrowserFeature(null);
        this.mEbookFeatureList = getEbookFeature(null);
        this.mOfficeFeatureList = getOfficeFeature(null);
        this.mVideoFeatureList = getVideoFeature(null);
        this.mImFeatureList = getImFeature(null);
        this.mSipFeatureList = getSipFeature(null);
        this.mSmsFeatureList = getSmsFeature(null);
        this.mMmsSmsFeatureList = getMmsSmsFeature(null);
        this.mEmailFeatureList = getEmailFeature(null);
        this.mAudioFeatureList = getAudioFeature(null);
        this.mNavigationFeatureList = getNavigationFeature(null);
        this.mBrowserMimeFeatureList = getBrowerMimeFeature(null);
        this.mImageFeatureList = getImageFeature(null);
        this.mMarketActivityList = getAppCategoryActivity(null, "android.intent.category.APP_MARKET");
        this.mGalleryActivityList = getAppCategoryActivity(null, "android.intent.category.APP_GALLERY");
        getApplications(null, new String[]{"application/pdf"}, null, this.mPdfFeatureList);
    }

    private void clearAppsType() {
        this.mClockFeatureList.clear();
        this.mEmailFeatureList.clear();
        this.mBrowserFeatureList.clear();
        this.mOfficeFeatureList.clear();
        this.mEbookFeatureList.clear();
        this.mVideoFeatureList.clear();
        this.mImFeatureList.clear();
        this.mSipFeatureList.clear();
        this.mMmsSmsFeatureList.clear();
        this.mSmsFeatureList.clear();
        this.mAudioFeatureList.clear();
        this.mPdfFeatureList.clear();
        this.mBrowserMimeFeatureList.clear();
        this.mImageFeatureList.clear();
        this.mNavigationFeatureList.clear();
        this.mMarketActivityList.clear();
        this.mGalleryActivityList.clear();
    }

    private void recogniseAppType(String pkgName) {
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "recogniseAppType, packName is null!");
            return;
        }
        long start = SystemClock.elapsedRealtime();
        int type = -1;
        if (isNewsClientApp(pkgName)) {
            type = 18;
        } else if (isShopApp(pkgName)) {
            type = 19;
        } else if (isClockApp(pkgName)) {
            type = 10;
        } else if (isInputMethodApp(pkgName)) {
            type = 4;
        } else if (isBrowserApp(pkgName)) {
            type = 6;
        } else if (isEmailApp(pkgName)) {
            type = 3;
        } else if (isSipApp(pkgName)) {
            type = 17;
        } else if (isSmsApp(pkgName)) {
            type = 2;
        } else if (isImApp(pkgName)) {
            type = 11;
        } else if (isLauncherApp(pkgName)) {
            type = 1;
        } else if (isEbookApp(pkgName)) {
            type = 7;
        } else if (isOfficeApp(pkgName)) {
            type = 15;
        } else if (isVideoApp(pkgName)) {
            type = 8;
        } else if (isMusicApp(pkgName)) {
            type = 12;
        } else if (isGalleryApp(pkgName)) {
            type = 16;
        } else if (isLocationProviderApp(pkgName)) {
            type = 14;
        } else if (isNavigationApp(pkgName)) {
            type = 13;
        } else if (isScrLockApp(pkgName)) {
            type = 9;
        } else if (isAppMarket(pkgName)) {
            type = 20;
        }
        if (-1 != type) {
            addAppToTypeList(pkgName, type);
        }
    }

    private void addAppToTypeList(String pkgName, int type) {
        synchronized (mAppsTypeList) {
            ArrayList<String> appList = (ArrayList) mAppsTypeList.get(Integer.valueOf(type));
            if (appList == null) {
                appList = new ArrayList();
            }
            if (!appList.contains(pkgName)) {
                appList.add(pkgName);
            }
            mAppsTypeList.put(Integer.valueOf(type), appList);
        }
    }

    private void removeAppFromList(String pkgName) {
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "removeAppFromList, packName is null!");
            return;
        }
        synchronized (mAppsTypeList) {
            for (Entry entry : mAppsTypeList.entrySet()) {
                ArrayList<String> pkgList = (ArrayList) entry.getValue();
                if (pkgList != null) {
                    boolean inList = false;
                    for (String name : pkgList) {
                        if (pkgName.equals(name)) {
                            inList = true;
                            break;
                        }
                    }
                    if (inList) {
                        pkgList.remove(pkgName);
                        return;
                    }
                }
            }
        }
    }

    public int getAppType(String pkgName) {
        int appType = -1;
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "pkgName is null, type unknow!");
            return -1;
        }
        synchronized (mAppsTypeList) {
            for (Entry entry : mAppsTypeList.entrySet()) {
                ArrayList<String> pkgList = (ArrayList) entry.getValue();
                if (pkgList != null && pkgList.contains(pkgName)) {
                    appType = ((Integer) entry.getKey()).intValue();
                    break;
                }
            }
        }
        return appType;
    }

    public ArrayList<String> getAppsByType(int type) {
        ArrayList<String> appList = (ArrayList) mAppsTypeList.get(Integer.valueOf(type));
        if (appList == null) {
            return new ArrayList();
        }
        return appList;
    }

    public void updateAppType(int newType, String appPkg) {
        if (appPkg != null) {
            int oldType = getAppType(appPkg);
            switch (oldType) {
                case NativeAdapter.PLATFORM_UNKNOWN /*-1*/:
                    addAppToTypeList(appPkg, newType);
                    DBWrapper.updateAppTypeToDb(this.mContext, appPkg, newType);
                    break;
                case 6:
                    if (newType == 5) {
                        Log.i("AppTypeRecognise", appPkg + "->new type:" + newType + " old type:" + oldType);
                        addAppToTypeList(appPkg, newType);
                        DBWrapper.updateAppTypeToDb(this.mContext, appPkg, newType);
                        break;
                    }
                    break;
            }
        }
    }

    private void checkInputMethodType() {
        ArrayList<String> inputMethodList = getInputMethodList();
        if (inputMethodList != null) {
            for (String name : inputMethodList) {
                int type = getAppType(name);
                if (type != 4) {
                    if (type != -1) {
                        removeAppFromList(name);
                    }
                    addAppToTypeList(name, 4);
                    DBWrapper.updateAppTypeToDb(this.mContext, name, 4);
                }
            }
        }
    }

    private void scrLockRecognise(String frontApp) {
        if (this.mIDeviceState.getScrOffDuration() <= 10000 && getAppType(frontApp) == -1 && this.mScrLockPkgList.contains(frontApp)) {
            if (!(this.mCurScrLockPkg == null || this.mCurScrLockPkg.equals(frontApp))) {
                removeAppFromList(this.mCurScrLockPkg);
            }
            this.mCurScrLockPkg = frontApp;
            Log.i("AppTypeRecognise", "scrlock app : " + this.mCurScrLockPkg);
            addAppToTypeList(this.mCurScrLockPkg, 9);
        }
    }

    private boolean isBrowserApp(String pkgName) {
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "pkgName is null, cannot recognise browser type!");
            return false;
        } else if (pkgName.equals("com.android.explore.tool0624") || pkgName.equals("com.storm.yeelion") || pkgName.equals("d.torrent.tsearch") || pkgName.equals("com.vmihalachi.ie") || pkgName.equals("com.juwan.market")) {
            return true;
        } else {
            if (pkgName.equals("com.taobao.taobao") || pkgName.equals("com.tmall.wireless") || pkgName.equals("com.taobao.ju.android") || pkgName.equals("com.xunlei.downloadprovider") || pkgName.equals("com.xfplay.play") || pkgName.equals("com.cleanmaster.security_cn") || pkgName.equals("com.xunlei.cloud") || pkgName.equals("com.xinmei365.font")) {
                return false;
            }
            if (this.mInitFromSysFlag) {
                if (this.mBrowserFeatureList.contains(pkgName) && !this.mNavigationFeatureList.contains(pkgName) && !hasGameEnginge(pkgName) && (pkgName.contains("browser") || hasRequestedPermission("com.android.browser.permission.READ_HISTORY_BOOKMARKS", pkgName) || this.mBrowserMimeFeatureList.contains(pkgName))) {
                    return true;
                }
            } else if (getBrowserFeature(pkgName).size() != 0 && getNavigationFeature(pkgName).size() == 0 && !hasGameEnginge(pkgName) && (pkgName.contains("browser") || hasRequestedPermission("com.android.browser.permission.READ_HISTORY_BOOKMARKS", pkgName) || getBrowerMimeFeature(pkgName).size() > 0)) {
                return true;
            }
            return false;
        }
    }

    private boolean isEmailApp(String pkgName) {
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "pkgName is null, cannot recognise email type!");
            return false;
        } else if ("com.sina.mail".equals(pkgName) || "com.mail.emails".equals(pkgName) || "com.aol.mobile.aolapp".equals(pkgName) || "com.regmail.keyone".equals(pkgName) || "com.transn.itlp.cycii".equals(pkgName) || "com.mail1click".equals(pkgName) || "com.android.qiushui.app.email".equals(pkgName) || "mail139.launcher".equals(pkgName) || "com.yahoolitemail".equals(pkgName) || "com.fuzixx.dokidokipostbox".equals(pkgName) || "com.ian.livewebmail".equals(pkgName) || "mail139.launcher.hd".equals(pkgName) || "com.google.android.email".equals(pkgName) || "me.youchai.yoc".equals(pkgName) || "com.vovk.hiibook".equals(pkgName) || "com.yahoo.mobile.client.android.mail".equals(pkgName) || "org.kman.AquaMail".equals(pkgName) || "com.google.android.gm".equals(pkgName) || "ru.mail.mailapp".equals(pkgName) || "ru.mail".equals(pkgName) || "de.gmx.mobile.android.mail".equals(pkgName)) {
            return true;
        } else {
            if (this.mInitFromSysFlag) {
                if (this.mEmailFeatureList.contains(pkgName)) {
                    return true;
                }
            } else if (getEmailFeature(pkgName).size() > 0) {
                return true;
            }
            return false;
        }
    }

    private boolean isSipApp(String pkgName) {
        if ("com.viber.voip".equals(pkgName) || "me.dingtone.app.im".equals(pkgName) || "finarea.MobileVoip".equals(pkgName)) {
            return true;
        }
        if (this.mInitFromSysFlag) {
            if (this.mSipFeatureList.contains(pkgName) && (hasRequestedPermission("android.permission.USE_SIP", pkgName) || hasRequestedPermission("android.permission.CALL_PHONE", pkgName))) {
                Log.i("AppTypeRecognise", "Sip app : " + pkgName);
                return true;
            }
        } else if ((hasRequestedPermission("android.permission.USE_SIP", pkgName) || hasRequestedPermission("android.permission.CALL_PHONE", pkgName)) && getSipFeature(pkgName).size() > 0) {
            Log.i("AppTypeRecognise", "Sip app : " + pkgName);
            return true;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isSmsApp(String pkgName) {
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "pkgName is null, cannot recognise sms type!");
            return false;
        } else if ("cn.com.fetion".equals(pkgName) || "com.jb.gosms.aemoji".equals(pkgName) || "com.jb.gosms".equals(pkgName)) {
            return true;
        } else {
            if ("com.whatsapp".equals(pkgName) || "com.tencent.qqpimsecure".equals(pkgName) || "com.jeejen.family".equals(pkgName)) {
                return false;
            }
            if (this.mInitFromSysFlag) {
                if (this.mSmsFeatureList.contains(pkgName) && this.mMmsSmsFeatureList.contains(pkgName)) {
                    return true;
                }
            } else if (getSmsFeature(pkgName).size() != 0 && getMmsSmsFeature(pkgName).size() > 0) {
                return true;
            }
            return false;
        }
    }

    private boolean isMusicApp(String pkgName) {
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "pkgName is null, cannot recognise music type!");
            return false;
        } else if ("com.xinmei365.font".equals(pkgName) || "com.estrongs.android.pop".equals(pkgName) || "com.zcdog.smartlocker.android".equals(pkgName) || "com.iplay.assistant".equals(pkgName)) {
            return false;
        } else {
            if ("com.apple.android.music".equals(pkgName) || "com.chrrs.cherrymusic".equals(pkgName) || "com.CoolTingMusicPlayerh".equals(pkgName)) {
                return true;
            }
            if (this.mInitFromSysFlag) {
                if (!(!this.mAudioFeatureList.contains(pkgName) || isImApp(pkgName) || this.mBrowserFeatureList.contains(pkgName))) {
                    return true;
                }
            } else if (!(getAudioFeature(pkgName).size() <= 0 || isImApp(pkgName) || this.mBrowserFeatureList.contains(pkgName))) {
                return true;
            }
            return (pkgName.contains("music") || pkgName.contains("sound")) && (hasActionActivity(pkgName, "android.intent.action.MUSIC_PLAYER") || hasActionActivity(pkgName, "android.media.action.MEDIA_PLAY_FROM_SEARCH"));
        }
    }

    private boolean isImApp(String pkgName) {
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "packName is null, cannot recognise im type!");
            return false;
        } else if (mImAppWhiteList.contains(pkgName)) {
            return true;
        } else {
            if (mNotImAppList.contains(pkgName) || !hasRequestedPermission("android.permission.CAMERA", pkgName)) {
                return false;
            }
            if (this.mInitFromSysFlag) {
                if (!this.mMarketActivityList.contains(pkgName) && this.mImFeatureList.contains(pkgName)) {
                    return true;
                }
            } else if (getAppCategoryActivity(pkgName, "android.intent.category.APP_MARKET").size() <= 0 && getImFeature(pkgName).size() > 0) {
                return true;
            }
            return false;
        }
    }

    private boolean isClockApp(String pkgName) {
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "packName is null, cannot recognise clock type!");
            return false;
        }
        String name = pkgName.toLowerCase();
        if (name.contains("clock") || name.contains("alarm") || name.contains("com.sugeun.stopwatch") || mClockAppList.contains(name)) {
            return true;
        }
        if ("com.wenba.bangbang".equals(pkgName) || "com.itings.myradio".equals(pkgName) || "com.kekeclient_".equals(pkgName)) {
            return false;
        }
        if (this.mInitFromSysFlag) {
            if (this.mClockFeatureList.contains(pkgName)) {
                return true;
            }
        } else if (getClockFeature(pkgName).size() > 0) {
            return true;
        }
        return false;
    }

    private boolean isInputMethodApp(String pkgName) {
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "pkgName is null, cannot recognise inputMethod type!");
            return false;
        } else if ("com.google.android.googlequicksearchbox".equals(pkgName) || "com.xxAssistant".equals(pkgName)) {
            return false;
        } else {
            if (mInputMethodList.size() == 0) {
                mInputMethodList.addAll(getInputMethodList());
            }
            if (mInputMethodList.contains(pkgName)) {
                return true;
            }
            return false;
        }
    }

    private boolean isNavigationApp(String pkgName) {
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "pkgName is null, cannot recognise navigation type!");
            return false;
        } else if ("com.virtualmaze.gpsdrivingroute".equals(pkgName) || "com.magnetic.openmaps".equals(pkgName) || "com.raxtone.flynavi".equals(pkgName) || "cx.ath.venator.locus".equals(pkgName) || "menion.android.locus.pro".equals(pkgName) || "com.ovital.ovitalMap".equals(pkgName)) {
            return true;
        } else {
            if ("com.zhaopin.social".equals(pkgName)) {
                return false;
            }
            if (this.mInitFromSysFlag) {
                if (this.mNavigationFeatureList.contains(pkgName)) {
                    return true;
                }
            } else if (getNavigationFeature(pkgName).size() > 0) {
                return true;
            }
            return false;
        }
    }

    private boolean isScrLockApp(String pkgName) {
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "pkgName is null, cannot recognise scrlock type!");
            return false;
        } else if (pkgName.equals(this.mCurScrLockPkg)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isEbookApp(String pkgName) {
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "pkgName is null, cannot recognise ebook type!");
            return false;
        } else if ("com.muzhiwan.market".equals(pkgName) || "com.ovital.ovitalMap".equals(pkgName) || "cx.ath.venator.locus".equals(pkgName) || "menion.android.locus.pro".equals(pkgName) || "menion.android.locus".equals(pkgName) || "com.netease.newsreader.activity".equals(pkgName) || "com.didapinche.booking".equals(pkgName) || "android.zhibo8".equals(pkgName) || "com.estrongs.android.pop".equals(pkgName) || "com.adobe.reader".equals(pkgName) || "com.mfw.roadbook".equals(pkgName) || "com.speedsoftware.rootexplorer".equals(pkgName) || "com.iflytek.ringdiyclient.ringbooks".equals(pkgName) || "com.ylmf.androidclient".equals(pkgName) || "com.iplay.assistant".equals(pkgName)) {
            return false;
        } else {
            if (pkgName.contains("reader") || pkgName.contains("book") || mEbookAppList.contains(pkgName)) {
                return true;
            }
            if (this.mInitFromSysFlag) {
                if (!(this.mBrowserFeatureList.contains(pkgName) || !this.mEbookFeatureList.contains(pkgName) || isOfficeApp(pkgName))) {
                    return true;
                }
            } else if (getBrowserFeature(pkgName).size() <= 0 && getEbookFeature(pkgName).size() > 0 && !isOfficeApp(pkgName)) {
                return true;
            }
            return false;
        }
    }

    private boolean isOfficeApp(String pkgName) {
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "pkgName is null, cannot recognise office type!");
            return false;
        } else if ("com.iplay.assistant".equals(pkgName) || "com.paic.yl.health".equals(pkgName) || "com.hexin.android.stocktrain".equals(pkgName) || "com.icbc.mobile.abroad".equals(pkgName) || "com.google.android.apps.docs".equals(pkgName)) {
            return false;
        } else {
            if (this.mInitFromSysFlag) {
                if (this.mBrowserFeatureList.contains(pkgName) || isImApp(pkgName)) {
                    return false;
                }
                if (this.mOfficeFeatureList.contains(pkgName)) {
                    return true;
                }
                return this.mPdfFeatureList.contains(pkgName) && !this.mEbookFeatureList.contains(pkgName);
            } else if (getBrowserFeature(pkgName).size() > 0 || isImApp(pkgName)) {
                return false;
            } else {
                if (getOfficeFeature(pkgName).size() > 0) {
                    return true;
                }
                ArrayList<String> pdfFeatureList = new ArrayList();
                getApplications(null, new String[]{"application/pdf"}, pkgName, pdfFeatureList);
                ArrayList<String> ebookFeatureList = getEbookFeature(pkgName);
                if (pdfFeatureList.size() > 0 && !ebookFeatureList.contains(pkgName)) {
                    return true;
                }
            }
        }
    }

    private boolean isVideoApp(String pkgName) {
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "pkgName is null, cannot recognise video type!");
            return false;
        } else if ("com.sohu.newsclient".equals(pkgName) || "com.maxmpz.audioplayer".equals(pkgName) || "com.ifeng.news2".equals(pkgName) || "com.zcdog.smartlocker.android".equals(pkgName) || "com.google.android.apps.plus".equals(pkgName) || "com.ylmf.androidclient".equals(pkgName) || "com.xinmei365.font".equals(pkgName) || "com.estrongs.android.pop".equals(pkgName) || "com.iplay.assistant".equals(pkgName)) {
            return false;
        } else {
            if (mVideoAppsList.contains(pkgName)) {
                return true;
            }
            if (this.mInitFromSysFlag) {
                return this.mVideoFeatureList.contains(pkgName) && !isGalleryApp(pkgName);
            } else {
                if (getVideoFeature(pkgName).size() > 0 && !isGalleryApp(pkgName)) {
                    return true;
                }
            }
        }
    }

    private boolean isGalleryApp(String pkgName) {
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "pkgName is null, cannot recognise image type!");
            return false;
        } else if ("com.xinmei365.font".equals(pkgName) || "com.dewmobile.kuaiya".equals(pkgName) || "com.google.android.apps.plus".equals(pkgName) || "com.estrongs.android.pop".equals(pkgName) || "com.icbc.mobile.abroad".equals(pkgName) || "com.iplay.assistant".equals(pkgName)) {
            return false;
        } else {
            if ("com.instagram.android".equals(pkgName)) {
                return true;
            }
            if (this.mInitFromSysFlag) {
                if (this.mGalleryActivityList.contains(pkgName)) {
                    return true;
                }
                return (!this.mImageFeatureList.contains(pkgName) || isBrowserApp(pkgName) || isImApp(pkgName)) ? false : true;
            } else if (getAppCategoryActivity(pkgName, "android.intent.category.APP_GALLERY").size() > 0) {
                return true;
            } else {
                if (getImageFeature(pkgName).size() > 0 && !isBrowserApp(pkgName) && !isImApp(pkgName)) {
                    return true;
                }
            }
        }
    }

    private boolean isLocationProviderApp(String pkgName) {
        if (pkgName == null) {
            Log.w("AppTypeRecognise", "pkgName is null, cannot recognise location provider type!");
            return false;
        }
        if (mLocationProviderList.size() == 0) {
            mLocationProviderList.addAll(getBestLocationProvider());
        }
        if (mLocationProviderList.contains(pkgName)) {
            return true;
        }
        return false;
    }

    private boolean isLauncherApp(String pkgName) {
        if ("com.huawei.hwmwlauncher".equals(pkgName)) {
            return true;
        }
        return this.mLauncherList.contains(pkgName);
    }

    private boolean isNewsClientApp(String pkgName) {
        return mNewsClientList.contains(pkgName);
    }

    private boolean isShopApp(String pkgName) {
        return mShopAppsList.contains(pkgName);
    }

    private boolean isAppMarket(String pkgName) {
        if ("com.huawei.appmarket".equals(pkgName)) {
            return true;
        }
        if (this.mInitFromSysFlag) {
            if (this.mMarketActivityList.contains(pkgName)) {
                return true;
            }
        } else if (getAppCategoryActivity(pkgName, "android.intent.category.APP_MARKET").size() > 0) {
            return true;
        }
        return false;
    }

    private void getApplications(Uri data, String[] mimeType, String pkgName, ArrayList<String> packageList) {
        Intent verification = new Intent("android.intent.action.VIEW");
        if (data == null) {
            data = Uri.fromParts("file", "", null);
        }
        if (pkgName != null) {
            verification.setPackage(pkgName);
        }
        List<ResolveInfo> tempActivities;
        ComponentInfo ci;
        if (mimeType == null) {
            verification.setData(data);
            tempActivities = this.mPackageManager.queryIntentActivitiesAsUser(verification, 786432, this.mAppManager.getCurUserId());
            if (tempActivities != null) {
                for (ResolveInfo temp : tempActivities) {
                    ci = temp.activityInfo != null ? temp.activityInfo : temp.serviceInfo;
                    if (!(ci == null || ci.packageName == null)) {
                        packageList.add(ci.packageName);
                    }
                }
                return;
            }
            return;
        }
        for (String type : mimeType) {
            verification.setDataAndType(data, type);
            tempActivities = this.mPackageManager.queryIntentActivitiesAsUser(verification, 786432, this.mAppManager.getCurUserId());
            if (tempActivities != null) {
                for (ResolveInfo temp2 : tempActivities) {
                    ci = temp2.activityInfo != null ? temp2.activityInfo : temp2.serviceInfo;
                    if (!(ci == null || ci.packageName == null)) {
                        packageList.add(ci.packageName);
                    }
                }
            }
        }
    }

    private ArrayList<String> getEbookFeature(String pkgName) {
        ArrayList<String> ebookList = new ArrayList();
        String[] DOCUMENT_MIME_TYPE = new String[]{"text/plain", "text/html", "application/epub+zip"};
        getApplications(null, DOCUMENT_MIME_TYPE, pkgName, ebookList);
        getApplications(Uri.parse("file://data/test.txt"), DOCUMENT_MIME_TYPE, pkgName, ebookList);
        ebookList.remove("com.muzhiwan.market");
        return ebookList;
    }

    private ArrayList<String> getOfficeFeature(String pkgName) {
        ArrayList<String> officeList = new ArrayList();
        getApplications(null, new String[]{"application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation"}, pkgName, officeList);
        return officeList;
    }

    private ArrayList<String> getNavigationFeature(String pkgName) {
        ArrayList<String> navigationList = new ArrayList();
        getApplications(Uri.parse("geo:38.899533,-77.036476"), null, pkgName, navigationList);
        return navigationList;
    }

    private ArrayList<String> getVideoFeature(String pkgName) {
        ArrayList<String> videoList = new ArrayList();
        getApplications(Uri.parse("file://data/test.mp4"), new String[]{"video/*"}, pkgName, videoList);
        videoList.remove("com.sohu.newsclient");
        return videoList;
    }

    private ArrayList<String> getImageFeature(String pkgName) {
        ArrayList<String> imageList = new ArrayList();
        getApplications(null, new String[]{"image/*"}, pkgName, imageList);
        return imageList;
    }

    private ArrayList<String> getLauncherApps() {
        ArrayList<String> launcherList = new ArrayList();
        Intent installIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        Intent uninstallIntent = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
        Intent homeProperty = new Intent("android.intent.action.MAIN");
        homeProperty.addCategory("android.intent.category.HOME");
        List<ResolveInfo> homeReceivers = this.mPackageManager.queryIntentActivitiesAsUser(homeProperty, 786432, this.mAppManager.getCurUserId());
        if (homeReceivers != null) {
            for (ResolveInfo tempReceiver : homeReceivers) {
                ComponentInfo ci = tempReceiver.activityInfo != null ? tempReceiver.activityInfo : tempReceiver.serviceInfo;
                if (!(ci == null || ci.packageName == null)) {
                    if (hasRequestedPermission("android.permission.PERSISTENT_ACTIVITY", ci.packageName)) {
                        launcherList.add(ci.packageName);
                    } else if (hasRequestedPermission("android.permission.BIND_APPWIDGET", ci.packageName)) {
                        launcherList.add(ci.packageName);
                    } else {
                        installIntent.setPackage(ci.packageName);
                        List<ResolveInfo> install = this.mPackageManager.queryBroadcastReceivers(installIntent, 786434);
                        if (install == null || install.size() <= 0) {
                            uninstallIntent.setPackage(ci.packageName);
                            List<ResolveInfo> uninstall = this.mPackageManager.queryBroadcastReceivers(uninstallIntent, 786434);
                            if (uninstall != null && uninstall.size() > 0) {
                                launcherList.add(ci.packageName);
                            }
                        } else {
                            launcherList.add(ci.packageName);
                        }
                    }
                }
            }
        }
        launcherList.add("com.google.android.launcher");
        launcherList.add("com.tencent.qlauncher.lite");
        launcherList.add("com.tencent.launcher");
        launcherList.add("com.lx.launcher");
        launcherList.add("net.suckga.iLauncher2");
        launcherList.add("com.jeejen.family");
        return launcherList;
    }

    public String getUsingLauncher() {
        ArrayList<String> launcherList = this.mLauncherList;
        if (launcherList.size() <= 0) {
            launcherList = getLauncherApps();
            this.mLauncherList = launcherList;
        }
        if (1 == launcherList.size()) {
            return (String) launcherList.get(0);
        }
        String launcherName = null;
        List<RunningTaskInfo> runningTasks = this.mActivityManager.getRunningTasks(100);
        for (int i = 0; i < runningTasks.size(); i++) {
            String taskName = ((RunningTaskInfo) runningTasks.get(i)).topActivity.getPackageName();
            if (launcherList.contains(taskName)) {
                launcherName = taskName;
                break;
            }
        }
        return launcherName;
    }

    public String getDefaultLauncher() {
        ArrayList<String> launcherApps = this.mLauncherList;
        if (launcherApps.size() <= 0) {
            launcherApps = getLauncherApps();
            this.mLauncherList = launcherApps;
        }
        if (1 == launcherApps.size()) {
            return (String) launcherApps.get(0);
        }
        ArrayList<IntentFilter> outFilter = new ArrayList();
        ArrayList<ComponentName> outComponentName = new ArrayList();
        for (String pkgName : launcherApps) {
            this.mPackageManager.getPreferredActivities(outFilter, outComponentName, pkgName);
            if (outComponentName.size() > 0) {
                return pkgName;
            }
        }
        return null;
    }

    public String getCurLiveWallpaper() {
        WallpaperInfo wInfo = WallpaperManager.getInstance(this.mContext).getWallpaperInfo();
        if (wInfo == null) {
            return null;
        }
        return wInfo.getPackageName();
    }

    private ArrayList<String> getSipFeature(String pkgName) {
        ArrayList<String> sipList = new ArrayList();
        Intent verification = new Intent("android.intent.action.CALL");
        if (pkgName != null) {
            verification.setPackage(pkgName);
        }
        verification.setData(Uri.fromParts("sip", "", null));
        List<ResolveInfo> tempActivities = this.mPackageManager.queryIntentActivitiesAsUser(verification, 786432, this.mAppManager.getCurUserId());
        if (tempActivities != null) {
            for (ResolveInfo temp : tempActivities) {
                ComponentInfo ci = temp.activityInfo != null ? temp.activityInfo : temp.serviceInfo;
                if (!(ci == null || ci.packageName == null)) {
                    sipList.add(ci.packageName);
                }
            }
        }
        return sipList;
    }

    private ArrayList<String> getBrowerMimeFeature(String pkgName) {
        ArrayList<String> browserMimeList = new ArrayList();
        getApplications(Uri.fromParts("file", "", null), new String[]{"text/html", "application/xhtml+xml", "application/vnd.wap.xhtml+xml"}, pkgName, browserMimeList);
        return browserMimeList;
    }

    private ArrayList<String> getMmsSmsFeature(String pkgName) {
        ArrayList<String> mmsSmsList = new ArrayList();
        getApplications(Uri.fromParts("file", "", null), new String[]{"vnd.android-dir/mms-sms"}, pkgName, mmsSmsList);
        return mmsSmsList;
    }

    private ArrayList<String> getSmsFeature(String pkgName) {
        ArrayList<String> smsList = new ArrayList();
        Intent verification = new Intent("android.intent.action.SENDTO");
        if (pkgName != null) {
            verification.setPackage(pkgName);
        }
        verification.setData(Uri.fromParts("smsto", "", null));
        List<ResolveInfo> tempActivities = this.mPackageManager.queryIntentActivitiesAsUser(verification, 786432, this.mAppManager.getCurUserId());
        if (tempActivities != null) {
            for (ResolveInfo temp : tempActivities) {
                ComponentInfo ci = temp.activityInfo != null ? temp.activityInfo : temp.serviceInfo;
                if (!(ci == null || ci.packageName == null)) {
                    smsList.add(ci.packageName);
                }
            }
        }
        if (!smsList.contains("cn.com.fetion")) {
            smsList.add("cn.com.fetion");
        }
        smsList.removeAll(mNotSmsAppList);
        return smsList;
    }

    private ArrayList<String> getEmailFeature(String pkgName) {
        ArrayList<String> emailApps = new ArrayList();
        Intent verification = new Intent("android.intent.action.SENDTO", Uri.parse("mailto:abc@abc.com"));
        if (pkgName != null) {
            verification.setPackage(pkgName);
        }
        List<ResolveInfo> tempActivities = this.mPackageManager.queryIntentActivitiesAsUser(verification, 786944, this.mAppManager.getCurUserId());
        if (tempActivities != null) {
            for (ResolveInfo temp : tempActivities) {
                ComponentInfo ci = temp.activityInfo != null ? temp.activityInfo : temp.serviceInfo;
                if (!(ci == null || ci.packageName == null)) {
                    emailApps.add(ci.packageName);
                }
            }
        }
        return emailApps;
    }

    private ArrayList<String> getInputMethodList() {
        List<InputMethodInfo> inputMethodList = this.mInputMethod.getInputMethodList();
        ArrayList<String> inputMethodPkgList = new ArrayList();
        for (int i = 0; i < inputMethodList.size(); i++) {
            String inputMethodName = ((InputMethodInfo) inputMethodList.get(i)).getPackageName();
            if (!(inputMethodName == null || inputMethodPkgList.contains(inputMethodName))) {
                inputMethodPkgList.add(inputMethodName);
            }
        }
        return inputMethodPkgList;
    }

    public String getDefaultInputMethod() {
        String defaultInputMethodId = Secure.getStringForUser(this.mContext.getContentResolver(), "default_input_method", -2);
        if (TextUtils.isEmpty(defaultInputMethodId)) {
            Log.w("AppTypeRecognise", "Can not found default inputMethod !");
            return null;
        } else if (this.mDefaultInputMethodPkg != null && defaultInputMethodId.equals(this.mDefaultInputMethodId)) {
            return this.mDefaultInputMethodPkg;
        } else {
            Log.d("AppTypeRecognise", "Default InputMethod Id : " + defaultInputMethodId);
            String str = null;
            for (InputMethodInfo input : this.mInputMethod.getInputMethodList()) {
                if (defaultInputMethodId.equals(input.getId())) {
                    str = input.getPackageName();
                    Log.d("AppTypeRecognise", "Default InputMethod : " + str);
                    break;
                }
            }
            if (str == null) {
                String[] inputMethodInfoStr = defaultInputMethodId.split("/");
                if (inputMethodInfoStr != null) {
                    str = inputMethodInfoStr[0];
                    Log.d("AppTypeRecognise", "get default InputMethod : " + str);
                } else {
                    Log.e("AppTypeRecognise", "not find default InputMethod");
                }
            }
            this.mDefaultInputMethodPkg = str;
            this.mDefaultInputMethodId = defaultInputMethodId;
            return str;
        }
    }

    private ArrayList<String> getScreenLockList(boolean includeSys) {
        ComponentInfo ci;
        ArrayList<String> screenLockList = new ArrayList();
        ArrayList<String> candidates = new ArrayList();
        List<ResolveInfo> receiverGroup = this.mPackageManager.queryBroadcastReceivers(new Intent("android.intent.action.USER_PRESENT"), 786946);
        if (receiverGroup != null && receiverGroup.size() > 0) {
            for (ResolveInfo temp : receiverGroup) {
                ci = temp.activityInfo != null ? temp.activityInfo : temp.serviceInfo;
                if (!(ci == null || ci.packageName == null || candidates.contains(ci.packageName))) {
                    candidates.add(ci.packageName);
                }
            }
        }
        List<ResolveInfo> deviceAdminGroup = this.mPackageManager.queryBroadcastReceivers(new Intent("android.app.action.DEVICE_ADMIN_ENABLED"), 786946);
        if (deviceAdminGroup != null && deviceAdminGroup.size() > 0) {
            for (ResolveInfo temp2 : deviceAdminGroup) {
                ci = temp2.activityInfo != null ? temp2.activityInfo : temp2.serviceInfo;
                if (!(ci == null || ci.packageName == null || candidates.contains(ci.packageName))) {
                    candidates.add(ci.packageName);
                }
            }
        }
        for (String pkg : candidates) {
            if (hasRequestedPermission("android.permission.DISABLE_KEYGUARD", pkg) && !screenLockList.contains(pkg)) {
                if (includeSys || !this.mAppManager.isSystemApp(this.mContext, pkg)) {
                    screenLockList.add(pkg);
                }
            }
        }
        screenLockList.removeAll(mNotScreenLockList);
        return screenLockList;
    }

    private ArrayList<String> getImFeature(String pkgName) {
        ArrayList<String> iMsgApps = new ArrayList();
        Intent sendMsg = new Intent("android.intent.action.SEND");
        if (pkgName != null) {
            sendMsg.setPackage(pkgName);
        }
        sendMsg.setType("text/*");
        List<ResolveInfo> textResolveInfo = this.mPackageManager.queryIntentActivitiesAsUser(sendMsg, 786432, this.mAppManager.getCurUserId());
        if (textResolveInfo != null && textResolveInfo.size() > 0) {
            ComponentInfo ci;
            sendMsg.setType("image/*");
            List<ResolveInfo> imageResolveInfo = this.mPackageManager.queryIntentActivitiesAsUser(sendMsg, 786432, this.mAppManager.getCurUserId());
            if (imageResolveInfo != null && imageResolveInfo.size() > 0) {
                sendMsg.setType("audio/*");
                List<ResolveInfo> audioResolveInfo = this.mPackageManager.queryIntentActivitiesAsUser(sendMsg, 786432, this.mAppManager.getCurUserId());
                if (audioResolveInfo != null && audioResolveInfo.size() > 0) {
                    for (ResolveInfo info : audioResolveInfo) {
                        ci = info.activityInfo != null ? info.activityInfo : info.serviceInfo;
                        if (!(ci == null || ci.packageName == null || iMsgApps.contains(ci.packageName))) {
                            iMsgApps.add(ci.packageName);
                        }
                    }
                }
            }
            if (imageResolveInfo != null && imageResolveInfo.size() > 0) {
                sendMsg.setType("video/*");
                List<ResolveInfo> videoResolveInfo = this.mPackageManager.queryIntentActivitiesAsUser(sendMsg, 786432, this.mAppManager.getCurUserId());
                if (videoResolveInfo != null && videoResolveInfo.size() > 0) {
                    for (ResolveInfo info2 : videoResolveInfo) {
                        ci = info2.activityInfo != null ? info2.activityInfo : info2.serviceInfo;
                        if (!(ci == null || ci.packageName == null || iMsgApps.contains(ci.packageName))) {
                            iMsgApps.add(ci.packageName);
                        }
                    }
                }
            }
        }
        return iMsgApps;
    }

    private ArrayList<String> getBrowserFeature(String pkgName) {
        ArrayList<String> browserList = new ArrayList();
        ArrayList<String> navigationList = getNavigationFeature(pkgName);
        getApplications(Uri.parse("http://www.google.com"), null, pkgName, browserList);
        browserList.removeAll(navigationList);
        browserList.remove("com.taobao.taobao");
        return browserList;
    }

    private ArrayList<String> getAudioFeature(String pkgName) {
        ArrayList<String> audioList = new ArrayList();
        getApplications(Uri.parse("file://data/test.mp3"), new String[]{"audio/*"}, pkgName, audioList);
        return audioList;
    }

    private ArrayList<String> getClockFeature(String pkgName) {
        ArrayList<String> clockList = new ArrayList();
        Intent alarmAction = new Intent("android.intent.action.SET_ALARM");
        if (pkgName != null) {
            alarmAction.setPackage(pkgName);
        }
        List<ResolveInfo> alarmActivity = this.mContext.getPackageManager().queryIntentActivitiesAsUser(alarmAction, 786432, this.mAppManager.getCurUserId());
        if (alarmActivity != null) {
            for (ResolveInfo temp : alarmActivity) {
                ComponentInfo ci = temp.activityInfo != null ? temp.activityInfo : temp.serviceInfo;
                if (!(ci == null || ci.packageName == null)) {
                    clockList.add(ci.packageName);
                }
            }
        }
        return clockList;
    }

    private ArrayList<String> getBestLocationProvider() {
        ArrayList<String> locationProvider = new ArrayList();
        Resources resources = this.mContext.getResources();
        ArrayList<String> pkgs = new ArrayList();
        String[] arrPkgs = resources.getStringArray(17236013);
        if (arrPkgs != null) {
            String packageName;
            pkgs.addAll(Arrays.asList(arrPkgs));
            String systemPackageName = this.mContext.getPackageName();
            ArrayList<HashSet<Signature>> sigSets = getSignatureSets(pkgs);
            for (ResolveInfo rInfo : this.mPackageManager.queryIntentServicesAsUser(new Intent("com.android.location.service.FusedLocationProvider"), 786560, this.mAppManager.getCurUserId())) {
                packageName = rInfo.serviceInfo.packageName;
                try {
                    if (isSignatureMatch(this.mPackageManager.getPackageInfo(packageName, 64).signatures, sigSets)) {
                        if (rInfo.serviceInfo.metaData != null && rInfo.serviceInfo.metaData.getInt("serviceVersion", -1) == 0 && (rInfo.serviceInfo.applicationInfo.flags & 1) != 0 && this.mPackageManager.checkSignatures(systemPackageName, packageName) == 0) {
                            locationProvider.add(packageName);
                            break;
                        }
                    }
                    Log.w("AppTypeRecognise", packageName + " resolves service FusedLocationProvider, but has wrong signature, ignoring");
                } catch (NameNotFoundException e) {
                }
            }
            ArrayList<String> actions = new ArrayList();
            actions.add("com.android.location.service.FusedProvider");
            actions.add("com.android.location.service.ActivityRecognitionProvider");
            actions.add("com.android.location.service.GeocodeProvider");
            actions.add("com.android.location.service.GeofenceProvider");
            actions.add("com.android.location.service.v3.NetworkLocationProvider");
            actions.add("com.android.location.service.FusedLocationProvider");
            for (String action : actions) {
                List<ResolveInfo> rInfos2 = this.mPackageManager.queryIntentServicesAsUser(new Intent(action), 786560, 0);
                int bestVersion = Integer.MIN_VALUE;
                Object obj = null;
                if (rInfos2 != null) {
                    for (ResolveInfo rInfo2 : rInfos2) {
                        packageName = rInfo2.serviceInfo.packageName;
                        try {
                            if (isSignatureMatch(this.mPackageManager.getPackageInfo(packageName, 64).signatures, sigSets)) {
                                int version = Integer.MIN_VALUE;
                                if (rInfo2.serviceInfo.metaData != null) {
                                    version = rInfo2.serviceInfo.metaData.getInt("serviceVersion", Integer.MIN_VALUE);
                                }
                                if (version > bestVersion) {
                                    bestVersion = version;
                                    obj = packageName;
                                }
                            } else {
                                Log.w("AppTypeRecognise", packageName + " resolves service " + action + ", but has wrong signature, ignoring");
                            }
                        } catch (NameNotFoundException e2) {
                            Log.wtf("AppTypeRecognise", e2);
                        }
                    }
                }
                if (!(obj == null || locationProvider.contains(obj))) {
                    locationProvider.add(obj);
                }
            }
            return locationProvider;
        }
        Log.w("AppTypeRecognise", "config_locationProviderPackageNames not found");
        return locationProvider;
    }

    private ArrayList<HashSet<Signature>> getSignatureSets(List<String> initialPackageNames) {
        ArrayList<HashSet<Signature>> sigSets = new ArrayList();
        int size = initialPackageNames.size();
        for (int i = 0; i < size; i++) {
            String pkg = (String) initialPackageNames.get(i);
            try {
                HashSet<Signature> set = new HashSet();
                set.addAll(Arrays.asList(this.mPackageManager.getPackageInfo(pkg, 64).signatures));
                sigSets.add(set);
            } catch (NameNotFoundException e) {
            }
        }
        return sigSets;
    }

    private boolean isSignatureMatch(Signature[] signatures, List<HashSet<Signature>> sigSets) {
        if (signatures == null) {
            return false;
        }
        HashSet<Signature> inputSet = new HashSet();
        for (Signature s : signatures) {
            inputSet.add(s);
        }
        for (HashSet<Signature> referenceSet : sigSets) {
            if (referenceSet.equals(inputSet)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRequestedPermission(String permName, String appPkg) {
        return getRequestedPermissions(appPkg).contains(permName);
    }

    private List<String> getRequestedPermissions(String appPkg) {
        List<String> permList = new ArrayList();
        try {
            String[] strList = this.mPackageManager.getPackageInfo(appPkg, 4096).requestedPermissions;
            if (strList == null || strList.length == 0) {
                return permList;
            }
            for (String permName : strList) {
                if (permName != null) {
                    permList.add(permName);
                }
            }
            return permList;
        } catch (NameNotFoundException e) {
            Log.w("AppTypeRecognise", "Couldn't retrieve permissions for package:" + appPkg);
            return permList;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean hasGameEnginge(String appPkg) {
        try {
            PackageInfo pkgInfo = this.mPackageManager.getPackageInfo(appPkg, 128);
            if (!(pkgInfo == null || pkgInfo.applicationInfo == null || pkgInfo.applicationInfo.metaData == null)) {
                CharSequence libName = pkgInfo.applicationInfo.metaData.getCharSequence("android.app.lib_name", null);
                if (libName != null && libName.toString().contains("cocos2")) {
                    Log.i("AppTypeRecognise", "has gm engine: " + libName + " about pkg:" + appPkg);
                    return true;
                }
            }
            return false;
        } catch (NameNotFoundException e) {
            Log.w("AppTypeRecognise", "Couldn't retrieve libs for package:" + appPkg);
            return false;
        }
    }

    private boolean hasActionActivity(String pkgName, String action) {
        Intent queryAction = new Intent(action);
        queryAction.setPackage(pkgName);
        List<ResolveInfo> resultActivity = this.mPackageManager.queryIntentActivitiesAsUser(queryAction, 786944, this.mAppManager.getCurUserId());
        if (resultActivity == null || resultActivity.size() <= 0) {
            return false;
        }
        return true;
    }

    private ArrayList<String> getAppCategoryActivity(String pkgName, String category) {
        ArrayList<String> categoryList = new ArrayList();
        Intent handlerIntent = new Intent("android.intent.action.MAIN");
        handlerIntent.addCategory(category);
        if (pkgName != null) {
            handlerIntent.setPackage(pkgName);
        }
        List<ResolveInfo> resultActivity = this.mPackageManager.queryIntentActivitiesAsUser(handlerIntent, 786944, this.mAppManager.getCurUserId());
        if (resultActivity != null) {
            for (ResolveInfo temp : resultActivity) {
                ComponentInfo ci = temp.activityInfo != null ? temp.activityInfo : temp.serviceInfo;
                if (!(ci == null || ci.packageName == null)) {
                    categoryList.add(ci.packageName);
                }
            }
        }
        return categoryList;
    }
}
