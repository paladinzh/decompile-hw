package com.huawei.systemmanager.spacecleanner.statistics;

import android.text.TextUtils;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class FileAnalysisCleanInfo {
    private static final String APK = "APK";
    private static final String APP_NUM = "APP_NUM";
    private static final String APP_PKG = "APP_PKG";
    private static final String APP_SIZE = "APP_SIZE";
    private static final String APP_TIME = "APP_TIME";
    private static final String ARCHIVES = "ARCHIVES";
    private static final String AR_TIME = "AR_TIME";
    private static final String A_TIME = "A_TIME";
    private static final String DOC = "DOC";
    private static final String D_TIME = "D_TIME";
    private static final String LF_NUM = "LF_NUM";
    private static final String LF_SIZE = "LF_SIZE";
    private static final String MUSIC = "MUSIC";
    private static final String M_TIME = "M_TIME";
    private static final String OTHER = "OTHER";
    private static final String O_TIME = "O_TIME";
    private static final String PHOTO = "PHOTO";
    private static final String P_TIME = "P_TIME";
    private static final String TAG = "FileAnalysisCleanInfo";
    private static final String TIME = "TIME";
    private static final String VIDEO = "VIDEO";
    private static final String V_TIME = "V_TIME";
    public static final String ZERO_SIZE = "0";
    public int mARTime = 0;
    public int mATime = 0;
    public int mAppNum = 0;
    public String mAppPkgNames;
    public long mAppSize = 0;
    public int mAppTime = 0;
    public int mDTime = 0;
    private List<InfoItemBean> mInfoItemBeans = new ArrayList();
    public int mLargeFileApkNum = 0;
    public long mLargeFileApkSize = 0;
    public int mLargeFileArchivesNum = 0;
    public long mLargeFileArchivesSize = 0;
    public int mLargeFileDocNum = 0;
    public long mLargeFileDocSize = 0;
    public int mLargeFileMusicNum = 0;
    public long mLargeFileMusicSize = 0;
    public int mLargeFileNum = 0;
    public int mLargeFileOtherNum = 0;
    public long mLargeFileOtherSize = 0;
    public int mLargeFilePhotoNum = 0;
    public long mLargeFilePhotoSize = 0;
    public long mLargeFileSize = 0;
    public int mLargeFileVideoNum = 0;
    public long mLargeFileVideoSize = 0;
    public int mMTime = 0;
    public int mOTime = 0;
    public int mPTime = 0;
    public long mTime = 0;
    public int mVTime = 0;

    public void setItem(InfoItemBean bean) {
        if (bean != null) {
            String value = bean.getValue();
            if (!(TextUtils.isEmpty(value) || "0".equals(value))) {
                this.mInfoItemBeans.add(bean);
            }
        }
    }

    public void setTime() {
        setItem(new InfoItemBean(TIME, String.valueOf(this.mTime)));
    }

    public void setLargeFileNum() {
        setItem(new InfoItemBean(LF_NUM, String.valueOf(this.mLargeFileNum)));
    }

    public void setLargeFileSize() {
        setItem(new InfoItemBean(LF_SIZE, String.valueOf(this.mLargeFileSize)));
    }

    public void setLargeFileMusicSize() {
        setItem(new InfoItemBean(MUSIC, String.valueOf(this.mLargeFileMusicSize)));
    }

    public void setMTime() {
        if (this.mLargeFileMusicNum > 0) {
            this.mMTime /= this.mLargeFileMusicNum;
            setItem(new InfoItemBean(M_TIME, String.valueOf(this.mMTime)));
            return;
        }
        HwLog.i(TAG, "setMTime mLargeFileMusicNum:" + this.mLargeFileMusicNum);
    }

    public void setLargeFileVideoSize() {
        setItem(new InfoItemBean(VIDEO, String.valueOf(this.mLargeFileVideoSize)));
    }

    public void setVTime() {
        if (this.mLargeFileVideoNum > 0) {
            this.mVTime /= this.mLargeFileVideoNum;
            setItem(new InfoItemBean(V_TIME, String.valueOf(this.mVTime)));
            return;
        }
        HwLog.i(TAG, "setVTime mLargeFileVideoNum:" + this.mLargeFileVideoNum);
    }

    public void setLargeFilePhotoSize() {
        setItem(new InfoItemBean(PHOTO, String.valueOf(this.mLargeFilePhotoSize)));
    }

    public void setPTime() {
        if (this.mLargeFilePhotoNum > 0) {
            this.mPTime /= this.mLargeFilePhotoNum;
            setItem(new InfoItemBean(P_TIME, String.valueOf(this.mPTime)));
            return;
        }
        HwLog.i(TAG, "setPTime mLargeFilePhotoNum:" + this.mLargeFilePhotoNum);
    }

    public void setLargeFileApkSize() {
        setItem(new InfoItemBean(APK, String.valueOf(this.mLargeFileApkSize)));
    }

    public void setATime() {
        if (this.mLargeFileApkNum > 0) {
            this.mATime /= this.mLargeFileApkNum;
            setItem(new InfoItemBean(A_TIME, String.valueOf(this.mATime)));
            return;
        }
        HwLog.i(TAG, "setATime mLargeFileApkNum:" + this.mLargeFileApkNum);
    }

    public void setLargeFileDocSize() {
        setItem(new InfoItemBean(DOC, String.valueOf(this.mLargeFileDocSize)));
    }

    public void setDTime() {
        if (this.mLargeFileDocNum > 0) {
            this.mDTime /= this.mLargeFileDocNum;
            setItem(new InfoItemBean(D_TIME, String.valueOf(this.mDTime)));
            return;
        }
        HwLog.i(TAG, "setDTime mLargeFileDocNum:" + this.mLargeFileDocNum);
    }

    public void setLargeFileArchivesSize() {
        setItem(new InfoItemBean(ARCHIVES, String.valueOf(this.mLargeFileArchivesSize)));
    }

    public void setARTime() {
        if (this.mLargeFileArchivesNum > 0) {
            this.mARTime /= this.mLargeFileArchivesNum;
            setItem(new InfoItemBean(AR_TIME, String.valueOf(this.mARTime)));
            return;
        }
        HwLog.i(TAG, "setARTime mLargeFileArchivesNum:" + this.mLargeFileArchivesNum);
    }

    public void setLargeFileOtherSize() {
        setItem(new InfoItemBean(OTHER, String.valueOf(this.mLargeFileOtherSize)));
    }

    public void setOTime() {
        if (this.mLargeFileOtherNum > 0) {
            this.mOTime /= this.mLargeFileOtherNum;
            setItem(new InfoItemBean(O_TIME, String.valueOf(this.mOTime)));
            return;
        }
        HwLog.i(TAG, "setOTime mLargeFileOtherNum:" + this.mLargeFileOtherNum);
    }

    public void setAppNum() {
        setItem(new InfoItemBean(APP_NUM, String.valueOf(this.mAppNum)));
    }

    public void setAppSize() {
        setItem(new InfoItemBean(APP_SIZE, String.valueOf(this.mAppSize)));
    }

    public void setAppPkgs() {
        setItem(new InfoItemBean(APP_PKG, this.mAppPkgNames));
    }

    public void setAppTime() {
        if (this.mAppNum > 0) {
            this.mAppTime /= this.mAppNum;
            setItem(new InfoItemBean(APP_TIME, String.valueOf(this.mAppTime)));
            return;
        }
        HwLog.i(TAG, "setAppTime mAppNum:" + this.mAppNum);
    }

    public String getReport() {
        setTime();
        setLargeFileNum();
        setLargeFileSize();
        setLargeFileMusicSize();
        setMTime();
        setLargeFileVideoSize();
        setVTime();
        setLargeFilePhotoSize();
        setPTime();
        setLargeFileApkSize();
        setATime();
        setLargeFileDocSize();
        setDTime();
        setLargeFileArchivesSize();
        setARTime();
        setLargeFileOtherSize();
        setOTime();
        setAppNum();
        setAppSize();
        setAppPkgs();
        setAppTime();
        StringBuilder info = new StringBuilder();
        int size = this.mInfoItemBeans.size();
        if (size <= 0) {
            HwLog.e(TAG, "mInfoItemBeans is null!");
            return info.toString();
        }
        info.append("{");
        for (int index = 0; index < size; index++) {
            info.append(((InfoItemBean) this.mInfoItemBeans.get(index)).toString());
            if (index < size - 1) {
                info.append(ConstValues.SEPARATOR_KEYWORDS_EN);
            }
        }
        info.append("}");
        return info.toString();
    }
}
