package com.huawei.keyguard.support.magazine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.SparseArray;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.database.ClientHelper;
import com.huawei.keyguard.events.EventCenter;
import com.huawei.keyguard.events.EventCenter.IContentListener;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.monitor.RadarReporter;
import com.huawei.keyguard.monitor.RadarUtil;
import com.huawei.keyguard.support.PrivacyMode;
import com.huawei.keyguard.theme.HwThemeParser;
import com.huawei.keyguard.third.exif.ExifHelper;
import com.huawei.keyguard.util.BitmapUtils;
import com.huawei.keyguard.util.FpUtils;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.util.KeyguardUtils;
import com.huawei.keyguard.util.OsUtils;
import com.huawei.keyguard.util.WallpaperUtils;
import com.huawei.keyguard.view.effect.bokeh.BokehDrawable;
import com.huawei.openalliance.ad.inter.constant.EventType;
import fyusion.vislib.BuildConfig;

@SuppressLint({"NewApi"})
public class MagazineWallpaper implements IContentListener {
    private static MagazineWallpaper sInst = null;
    private Handler mBkHandler = GlobalContext.getBackgroundHandler();
    private boolean mBlockedAsEncryption = true;
    private Context mContext;
    private BgPicLoader mCurrentLoader = null;
    private boolean mIsContentChanged = false;
    private boolean mIsSdcardMountChecked = false;
    private Point mOutSize = new Point();
    private PictureList mPictures = new PictureList();
    private Runnable mReloadRunner = new Runnable() {
        public void run() {
            MagazineWallpaper.this.initDatas();
        }
    };
    private Handler mUIHandler = GlobalContext.getUIHandler();
    private Runnable mUpdatedescriptionRunner = new Runnable() {
        public void run() {
            BigPictureInfo picInfo = MagazineWallpaper.this.mPictures.getPictureInfo(1);
            if (picInfo != null) {
                picInfo.setDescriptionInfo(ExifHelper.getDescriptionInfoFromPicture(picInfo.getPicPath()));
            }
            picInfo = MagazineWallpaper.this.mPictures.getPictureInfo(0);
            if (picInfo != null) {
                picInfo.setDescriptionInfo(ExifHelper.getDescriptionInfoFromPicture(picInfo.getPicPath()));
            }
            picInfo = MagazineWallpaper.this.mPictures.getPictureInfo(-1);
            if (picInfo != null) {
                picInfo.setDescriptionInfo(ExifHelper.getDescriptionInfoFromPicture(picInfo.getPicPath()));
            }
        }
    };

    public interface IMagazineLoadCallback {
        void onMagazinePicLoaded(BigPicture bigPicture);
    }

    private class BgPicLoader implements Runnable {
        private IMagazineLoadCallback mCallback;
        private boolean mCanceled = false;
        private int mErrCnt = 0;
        private int mLoadType;
        private BigPictureInfo mPicInfo;

        BgPicLoader(BigPictureInfo picInfo, int type, IMagazineLoadCallback callback) {
            this.mPicInfo = picInfo;
            this.mCallback = callback;
            this.mLoadType = type;
        }

        public void run() {
            BigPicture bigPic;
            long start = SystemClock.uptimeMillis();
            HwLog.v("KGWallpaper_Magazine", "load magazine wallpaper start");
            do {
                bigPic = MagazineWallpaper.this.getBigPicture(this.mPicInfo);
                if (bigPic == null || !HwFyuseUtils.isFyuseProcessed(bigPic.getPicPath())) {
                    if (this.mErrCnt == 0 && ClientHelper.getInstance().checkPictureValidity(MagazineWallpaper.this.mContext)) {
                        HwLog.i("KGWallpaper_Magazine", "big pic is null and clear db.");
                        GlobalContext.getBackgroundHandler().removeCallbacks(MagazineWallpaper.this.mReloadRunner);
                        GlobalContext.getBackgroundHandler().postDelayed(MagazineWallpaper.this.mReloadRunner, 200);
                    }
                    if ("mounted".equals(Environment.getExternalStorageState())) {
                        MagazineWallpaper.this.mPictures.removeCurrent();
                    } else {
                        try {
                            this.mErrCnt++;
                        } catch (UserCanceledException e) {
                            HwLog.w("KGWallpaper_Magazine", "load magazine wallpaper: canceled");
                            return;
                        }
                    }
                    this.mPicInfo = MagazineWallpaper.this.mPictures.getPictureInfo(this.mLoadType);
                    HwLog.w("KGWallpaper_Magazine", "Magazine wallpaper file maybe deleted." + this.mLoadType + "; retry:" + this.mErrCnt);
                }
                if (bigPic != null) {
                    break;
                }
            } while (this.mErrCnt < MagazineWallpaper.this.mPictures.getPictureSize());
            onPictureLoaded(bigPic);
            HwLog.w("KGWallpaper_Magazine", "load magazine wallpaper use: " + (SystemClock.uptimeMillis() - start));
            synchronized (MagazineWallpaper.this) {
                MagazineWallpaper.this.mCurrentLoader = null;
            }
        }

        private void onPictureLoaded(final BigPicture bigPic) throws UserCanceledException {
            if (this.mCallback != null) {
                checkIfCanceled();
                if (bigPic == null) {
                    this.mCallback.onMagazinePicLoaded(bigPic);
                    return;
                }
                if (HwUnlockUtils.isTablet() && HwUnlockUtils.isLandscape(MagazineWallpaper.this.mContext)) {
                    bigPic.setBokehDrawable(BokehDrawable.create(MagazineWallpaper.this.mContext, bigPic.getLandBitmap(), true, false));
                } else {
                    bigPic.setBokehDrawable(BokehDrawable.create(MagazineWallpaper.this.mContext, bigPic.getBitmap(), true, false));
                }
                checkIfCanceled();
                MagazineWallpaper.this.mUIHandler.post(new Runnable() {
                    public void run() {
                        HwLog.d("KGWallpaper_Magazine", "Magazine Picture Loaded. " + BgPicLoader.this.mCanceled + "  " + BgPicLoader.this.mLoadType);
                        if (!BgPicLoader.this.mCanceled) {
                            if (BgPicLoader.this.mLoadType == 0) {
                                MagazineWallpaper.this.mPictures.setCurrentPicture(bigPic);
                            }
                            BgPicLoader.this.mCallback.onMagazinePicLoaded(bigPic);
                        }
                    }
                });
            }
        }

        private void checkIfCanceled() throws UserCanceledException {
            if (this.mCanceled) {
                throw new UserCanceledException();
            }
        }
    }

    private class PictureList {
        private int mCurrentIndx;
        private BigPicture mCurrentPicture;
        private SparseArray<BigPictureInfo> mPictureList;

        private PictureList() {
            this.mPictureList = new SparseArray();
            this.mCurrentIndx = -1;
            this.mCurrentPicture = null;
        }

        private boolean hasInitialized() {
            boolean z = false;
            synchronized (this) {
                if (this.mCurrentIndx >= 0) {
                    z = true;
                }
            }
            return z;
        }

        private SparseArray<BigPictureInfo> cloneData() {
            SparseArray<BigPictureInfo> clone;
            synchronized (this) {
                clone = this.mPictureList.clone();
            }
            return clone;
        }

        private int getNextIndex(int type) {
            synchronized (this) {
                int total = this.mPictureList.size();
                if (this.mCurrentIndx >= total || this.mCurrentIndx < 0) {
                    HwLog.w("KGWallpaper_Magazine", "Fail get next index - " + this.mCurrentIndx + "/" + total);
                    this.mCurrentIndx = 0;
                    return 0;
                }
                int indx = this.mCurrentIndx;
                if (type == 1) {
                    indx = this.mCurrentIndx + 1;
                    if (indx >= total) {
                        indx = 0;
                    }
                } else if (type == -1) {
                    indx = this.mCurrentIndx - 1;
                    if (indx < 0) {
                        indx = total - 1;
                    }
                } else if (type == -2) {
                    indx = this.mCurrentIndx - 2;
                    if (indx < 0) {
                        indx += total;
                    }
                } else if (type == 2) {
                    indx = this.mCurrentIndx + 2;
                    if (indx >= total) {
                        indx -= total;
                    }
                } else if (type == 3) {
                    indx = this.mCurrentIndx + 3;
                    if (indx >= total) {
                        indx -= total;
                    }
                } else if (type == -3) {
                    indx = this.mCurrentIndx - 3;
                    if (indx < 0) {
                        indx += total;
                    }
                }
                HwLog.d("KGWallpaper_Magazine", "getNextIndex: " + type + "; from " + this.mCurrentIndx + " to " + indx + "; size: " + total);
                return indx;
            }
        }

        private boolean checkIndexRange() {
            synchronized (this) {
                if (this.mCurrentIndx >= this.mPictureList.size() || this.mCurrentIndx < 0) {
                    HwLog.w("KGWallpaper_Magazine", "reset Index to 0.");
                    this.mCurrentIndx = 0;
                    return true;
                }
                return false;
            }
        }

        public BigPictureInfo removeCurrent() {
            synchronized (this) {
                int size = this.mPictureList.size();
                if (size == 0 || checkIndexRange()) {
                    HwLog.e("KGWallpaper_Magazine", "Rmove picture invalide state: " + size + " - " + this.mCurrentIndx);
                    return null;
                }
                BigPictureInfo bigInfo = (BigPictureInfo) this.mPictureList.valueAt(this.mCurrentIndx);
                this.mPictureList.removeAt(this.mCurrentIndx);
                checkIndexRange();
                return bigInfo;
            }
        }

        private BigPictureInfo getPictureInfo(int type) {
            synchronized (this) {
                if (this.mPictureList.size() == 0) {
                    return null;
                }
                BigPictureInfo bigPictureInfo = (BigPictureInfo) this.mPictureList.valueAt(getNextIndex(type));
                return bigPictureInfo;
            }
        }

        private void updateDatas(SparseArray<BigPictureInfo> pictureList) {
            synchronized (this) {
                int size = this.mPictureList.size();
                int currentKey = this.mCurrentIndx < 0 ? MagazineUtils.getCurrentPicId(MagazineWallpaper.this.mContext) : this.mCurrentIndx >= size ? -1 : this.mPictureList.keyAt(this.mCurrentIndx);
                this.mPictureList.clear();
                this.mPictureList = pictureList;
                this.mCurrentIndx = getIndexByKeyId(currentKey);
                if (this.mCurrentIndx < 0) {
                    HwLog.e("KGWallpaper_Magazine", "updateDatas lose picture index");
                    this.mCurrentIndx = 0;
                }
                HwLog.w("KGWallpaper_Magazine", "update list size: " + pictureList.size());
            }
            if (size == 0 || currentKey > -1) {
                KeyguardWallpaper.getInst(MagazineWallpaper.this.mContext).getWallpaper();
                return;
            }
            BigPictureInfo info = getPictureInfo(0);
            if (info != null) {
                MagazineWallpaper.this.loadPic(info, 0, null, true);
            }
        }

        private int getIndexByKeyId(int keyId) {
            if (keyId <= 0) {
                return -1;
            }
            synchronized (this) {
                for (int idx = 0; idx < this.mPictureList.size(); idx++) {
                    if (this.mPictureList.keyAt(idx) == keyId) {
                        HwLog.w("KGWallpaper_Magazine", "get Index for key " + keyId + " to " + idx);
                        return idx;
                    }
                }
                return -1;
            }
        }

        public void setCurrentPicture(BigPicture bigPic) {
            synchronized (this) {
                BigPictureInfo bInfo = bigPic.getBigPictureInfo();
                this.mCurrentIndx = getIndexByKeyId(bInfo.keyId);
                if (this.mCurrentIndx == -1 || bInfo != this.mPictureList.valueAt(this.mCurrentIndx)) {
                    HwLog.e("KGWallpaper_Magazine", "setCurrentPicture info mismatch : " + bInfo.keyId + " - " + this.mCurrentIndx);
                } else {
                    HwLog.e("KGWallpaper_Magazine", "setCurrentPicture to " + this.mCurrentIndx);
                    MagazineUtils.setCurrentPicId(MagazineWallpaper.this.mContext, bInfo.keyId);
                }
                this.mCurrentPicture = bigPic;
            }
        }

        public BigPicture getCurrentPicture() {
            BigPicture bigPicture;
            synchronized (this) {
                bigPicture = this.mCurrentPicture;
            }
            return bigPicture;
        }

        private int getPictureSize() {
            int size;
            synchronized (this) {
                size = this.mPictureList.size();
            }
            return size;
        }
    }

    private static class UserCanceledException extends Exception {
        private UserCanceledException() {
        }
    }

    public static MagazineWallpaper getInst(Context context) {
        synchronized (MagazineWallpaper.class) {
            if (sInst != null) {
                MagazineWallpaper magazineWallpaper = sInst;
                return magazineWallpaper;
            }
            sInst = new MagazineWallpaper(context);
            MagazineWallpaper inst = sInst;
            EventCenter.getInst().listenContent(4, inst);
            return inst;
        }
    }

    public void setCurrentPicture(BigPicture bigPic) {
        this.mPictures.setCurrentPicture(bigPic);
    }

    public int getPictureSize() {
        return this.mPictures.getPictureSize();
    }

    public boolean hasInitialized() {
        return this.mPictures.hasInitialized();
    }

    public BigPictureInfo getPictureInfo(int type) {
        return this.mPictures.getPictureInfo(type);
    }

    public BigPictureInfo removeCurrentWallpaper() {
        return this.mPictures.removeCurrent();
    }

    public BigPictureInfo getCurrentWallpaper() {
        BigPicture bigPicture = this.mPictures.getCurrentPicture();
        if (bigPicture == null) {
            return null;
        }
        return bigPicture.getBigPictureInfo();
    }

    public BigPicture getCurrentPicture() {
        return this.mPictures.getCurrentPicture();
    }

    public MagazineWallpaper(Context context) {
        this.mContext = context;
        this.mOutSize = HwUnlockUtils.getPoint(context);
    }

    private void updateBigPictureList() {
        if (this.mBlockedAsEncryption && OsUtils.isOwner() && HwKeyguardUpdateMonitor.getInstance(this.mContext).isFirstTimeStartupAndEncrypted()) {
            HwLog.w("KGWallpaper_Magazine", "updateBigPictureList skiped as BouncerForUser");
            return;
        }
        this.mBlockedAsEncryption = false;
        this.mPictures.updateDatas(ClientHelper.getInstance().querySelectedPictures(this.mContext, PrivacyMode.isPrivacyModeOn(this.mContext)));
    }

    public boolean reloadPicturesAfterFirstLogin() {
        if (!this.mBlockedAsEncryption) {
            return false;
        }
        HwLog.w("KGWallpaper_Magazine", "reloadPicturesAfterFirstLogin: " + this.mPictures.getPictureSize());
        this.mBlockedAsEncryption = false;
        checkAndLoadDatas();
        return true;
    }

    private void checkPictureDescriptions() {
        SparseArray<BigPictureInfo> pictureList = this.mPictures.cloneData();
        for (int i = 0; i < pictureList.size(); i++) {
            BigPictureInfo bInfo = (BigPictureInfo) pictureList.valueAt(i);
            if (!bInfo.getDescriptionInfo().hasContent()) {
                bInfo.setDescriptionInfo(ExifHelper.getDescriptionInfoFromPicture(bInfo.getPicPath()));
            }
        }
    }

    public boolean checkAndLoadDatas() {
        boolean z = false;
        if (this.mPictures.hasInitialized()) {
            return false;
        }
        if (this.mBlockedAsEncryption) {
            HwLog.w("KGWallpaper_Magazine", "A loader is queued or blocked.");
            if (KeyguardCfg.isCredentialProtected(this.mContext) && !HwKeyguardUpdateMonitor.getInstance(this.mContext).getStrongAuthTracker().hasUserAuthenticatedSinceBoot(0)) {
                z = true;
            }
            this.mBlockedAsEncryption = z;
        } else {
            this.mBkHandler.post(this.mReloadRunner);
        }
        return true;
    }

    public void initDatas() {
        updateBigPictureList();
        checkPictureDescriptions();
        String style = HwThemeParser.getInstance().getStyle(this.mContext);
        HwLog.w("KGWallpaper_Magazine", "reload wallpaper when: " + style + "; size: " + this.mPictures.getPictureSize());
        if (this.mPictures.getPictureSize() == 0 && "magazine".equals(style)) {
            KeyguardWallpaper.getInst(this.mContext).getWallpaper();
        }
    }

    public BigPicture getWallPaper(int type) {
        return getWallPaper(type, null);
    }

    public BigPicture getWallPaper(int type, IMagazineLoadCallback mCallBack) {
        if (checkAndLoadDatas()) {
            return null;
        }
        if (!getSdcardMountCheckedStatus()) {
            this.mBkHandler.post(new Runnable() {
                public void run() {
                    if (!KeyguardUtils.isSdcardMount()) {
                        RadarReporter.uploadSdcardMountRadar();
                        MagazineWallpaper.this.setSdcardMountCheckedStatus(true);
                    }
                }
            });
        }
        if (this.mPictures.getPictureSize() == 0) {
            HwLog.w("KGWallpaper_Magazine", "no magazine wallpaper: ");
            return null;
        }
        this.mBkHandler.post(this.mUpdatedescriptionRunner);
        BigPictureInfo picInfo = this.mPictures.getPictureInfo(type);
        BigPicture bigPic = this.mPictures.getCurrentPicture();
        if (type == 0 && picInfo != null && bigPic != null && TextUtils.equals(picInfo.getPicPath(), bigPic.getPicPath())) {
            return bigPic;
        }
        if (!(mCallBack == null || picInfo == null)) {
            HwLog.w("KGWallpaper_Magazine", "Load magazine with type : " + type);
            if (bigPic != null && FpUtils.isKeyguardLocked(this.mContext)) {
                HwLockScreenReporter.reportAdEvent(this.mContext, bigPic.getBigPictureInfo(), EventType.SHOWEND);
                if (bigPic.getBigPictureInfo() != null) {
                    HwLockScreenReporter.statReport(this.mContext, 1002, "{picture:" + bigPic.getBigPictureInfo().getPicName() + "}");
                }
            }
            loadPic(picInfo, type, mCallBack, true);
        }
        return null;
    }

    public synchronized boolean getSdcardMountCheckedStatus() {
        return this.mIsSdcardMountChecked;
    }

    public synchronized void setSdcardMountCheckedStatus(boolean checkStatus) {
        this.mIsSdcardMountChecked = checkStatus;
    }

    public void loadPic(BigPictureInfo picInfo, int type, IMagazineLoadCallback callback, boolean excluded) {
        if (excluded) {
            synchronized (this) {
                HwLog.w("KGWallpaper_Magazine", "loadPic: " + type);
                if (this.mCurrentLoader == null || this.mCurrentLoader.mPicInfo == null || this.mCurrentLoader.mPicInfo != picInfo) {
                    this.mCurrentLoader = new BgPicLoader(picInfo, type, callback);
                    this.mBkHandler.post(this.mCurrentLoader);
                    return;
                }
                HwLog.w("KGWallpaper_Magazine", "Already in loading for pic: " + picInfo.keyId);
                return;
            }
        }
        this.mBkHandler.post(new BgPicLoader(picInfo, type, callback));
    }

    private Bitmap getImageBitmap(String path) {
        if (HwFyuseUtils.isFyuseTypeFile(path)) {
            return HwFyuseUtils.getFyusePreBitmap(path);
        }
        return BitmapUtils.decodeFile(this.mContext, path, null);
    }

    public BigPicture getBigPicture(BigPictureInfo picInfo) {
        if (picInfo == null) {
            HwLog.w("KGWallpaper_Magazine", "getBigPicture with picInfo is null!");
            return null;
        }
        String path = picInfo.getPicPath();
        Bitmap bmp = null;
        try {
            bmp = getImageBitmap(path);
        } catch (OutOfMemoryError e) {
            RadarUtil.uploadUploadLockscreenOOM(this.mContext, "OutOfMemoryErrorget BigPicture path = " + path);
            HwLog.w("KGWallpaper_Magazine", "OutOfMemoryError get BigPicture path = " + path);
        }
        if (bmp == null) {
            RadarUtil.uploadLockscreenUnableAutochanged(this.mContext, "get BigPicture bitmap is null: " + path);
            HwLog.w("KGWallpaper_Magazine", "get BigPicture bitmap is null: " + path);
            return null;
        }
        Point outSize = WallpaperUtils.getRealScreenPoint(this.mContext, this.mOutSize);
        bmp = getCutBitmap(bmp, outSize);
        if (bmp == null) {
            HwLog.w("KGWallpaper_Magazine", "getCutBitmap bmp is null");
            return null;
        }
        BigPicture bigPic = new BigPicture();
        bigPic.set(picInfo, bmp);
        if (HwUnlockUtils.isSupportOrientation()) {
            Bitmap landbmp = getLandBitmap(bmp, outSize, path);
            if (landbmp == null) {
                HwLog.w("KGWallpaper_Magazine", "getLandBitmap landbmp is null");
                return null;
            }
            bigPic.setLandBmp(landbmp);
        }
        return bigPic;
    }

    public BigPicture getPortarit3DBigPicture(BigPictureInfo picInfo, Bitmap bmp) {
        if (bmp == null || picInfo == null) {
            return null;
        }
        BigPicture bigPic = new BigPicture();
        bigPic.set(picInfo, bmp);
        return bigPic;
    }

    private static Bitmap getCutBitmap(Bitmap bmp, Point outSize) {
        if (bmp == null || outSize == null) {
            HwLog.i("KGWallpaper_Magazine", "getLandBitmap bmp,outSize is null");
            return null;
        }
        float vWidth = (float) (outSize.x > outSize.y ? outSize.y : outSize.x);
        float vHeight = (float) (outSize.x > outSize.y ? outSize.x : outSize.y);
        float bWidth = (float) bmp.getWidth();
        float bHeight = (float) bmp.getHeight();
        if (vWidth <= 0.0f || vHeight <= 0.0f || bWidth <= 0.0f || bHeight <= 0.0f) {
            HwLog.w("KGWallpaper_Magazine", "get getCutBitmap, vWidth = " + vWidth + ", vHeight = " + vHeight + ", bWidth = " + bWidth + ", bHeight = " + bHeight);
            return null;
        }
        float bAspect = bWidth / bHeight;
        float vAspect = vWidth / vHeight;
        if (Float.compare(Math.abs(bAspect - vAspect), 0.0f) > 0) {
            int x = 0;
            int y = 0;
            int w = (int) bWidth;
            int h = (int) bHeight;
            if (bAspect < vAspect) {
                y = (int) ((((float) h) - (((float) w) / vAspect)) / 2.0f);
                h = (int) (((float) w) / vAspect);
            } else {
                x = (int) ((((float) w) - (((float) h) * vAspect)) / 2.0f);
                w = (((int) (((float) h) * vAspect)) / 2) * 2;
            }
            HwLog.d("KGWallpaper_Magazine", "get BigPicture, x = " + x + ", y = " + y + ",w = " + w + ", h = " + h);
            if (w <= 0 || h <= 0) {
                HwLog.w("KGWallpaper_Magazine", "getBigPicture cut bitmap fail for bitmap is too small: " + bWidth + "x" + bHeight);
            } else {
                bmp = Bitmap.createBitmap(bmp, x, y, w, h);
            }
        }
        return bmp;
    }

    private static Bitmap getLandBitmap(Bitmap bmp, Point outSize, String path) {
        if (bmp == null || outSize == null || path == null || path.trim().equals(BuildConfig.FLAVOR)) {
            HwLog.i("KGWallpaper_Magazine", "getLandBitmap bmp,outSize or path is null");
            return null;
        }
        float bWidth = (float) bmp.getWidth();
        float bHeight = (float) bmp.getHeight();
        float vWidth = (float) (outSize.x > outSize.y ? outSize.y : outSize.x);
        float vHeight = (float) (outSize.x > outSize.y ? outSize.x : outSize.y);
        float vAspect = vWidth / vHeight;
        int x = 0;
        int y = 0;
        if (vWidth <= 0.0f || vHeight <= 0.0f || bWidth <= 0.0f || bHeight <= 0.0f) {
            HwLog.w("KGWallpaper_Magazine", "getLandBitmap, vWidth = " + vWidth + ", vHeight = " + vHeight + ", bWidth = " + bWidth + ", bHeight = " + bHeight);
            return null;
        }
        int w;
        int h;
        Bitmap landbmp = BitmapUtils.decodeFile(MagazineUtils.getLandPicName(path));
        if (landbmp != null) {
            w = landbmp.getWidth();
            h = landbmp.getHeight();
        } else {
            w = (int) bWidth;
            h = (int) bHeight;
        }
        float bAspect = ((float) w) / ((float) h);
        if (Float.compare(Math.abs(bAspect - (1.0f / vAspect)), 0.0f) > 0) {
            if (bAspect < 1.0f / vAspect) {
                y = (int) ((((float) h) - (((float) w) * vAspect)) / 2.0f);
                h = (int) (((float) w) * vAspect);
            } else {
                x = (int) ((((float) w) - (((float) h) / vAspect)) / 2.0f);
                w = (((int) (((float) h) / vAspect)) / 2) * 2;
            }
        }
        HwLog.d("KGWallpaper_Magazine", "get LandBigPicture, x = " + x + ", y = " + y + ",w = " + w + ", h = " + h);
        if (w <= 0 || h <= 0) {
            HwLog.w("KGWallpaper_Magazine", "getBigPicture cut bitmap fail for bitmap is too small: " + bWidth + "x" + bHeight);
        } else if (landbmp != null) {
            landbmp = Bitmap.createBitmap(landbmp, x, y, w, h);
        } else {
            landbmp = Bitmap.createBitmap(bmp, x, y, w, h);
        }
        return landbmp;
    }

    public void onContentChange(boolean selfChange) {
        HwLog.w("KGWallpaper_Magazine", "Magazine getWallPaper content changed");
        if (this.mBlockedAsEncryption) {
            HwLog.w("KGWallpaper_Magazine", "Block reload picture list as encryption");
            return;
        }
        this.mBkHandler.removeCallbacks(this.mReloadRunner);
        this.mBkHandler.postDelayed(this.mReloadRunner, 500);
        this.mIsContentChanged = true;
    }

    public boolean isContentChange() {
        return this.mIsContentChanged;
    }

    public void setContentChange(boolean state) {
        this.mIsContentChanged = state;
    }

    public void switchPicture(int loadType, final IMagazineLoadCallback callback) {
        getWallPaper(loadType, new IMagazineLoadCallback() {
            public void onMagazinePicLoaded(BigPicture pic) {
                if (pic == null) {
                    HwLog.w("KGWallpaper_Magazine", "switchPicture failed");
                    return;
                }
                MagazineWallpaper.this.mPictures.setCurrentPicture(pic);
                if (callback != null) {
                    callback.onMagazinePicLoaded(pic);
                }
            }
        });
    }
}
