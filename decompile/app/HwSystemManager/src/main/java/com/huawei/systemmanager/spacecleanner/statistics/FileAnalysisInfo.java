package com.huawei.systemmanager.spacecleanner.statistics;

import android.text.TextUtils;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class FileAnalysisInfo {
    private static final String APK = "APK";
    private static final String APP_NUM = "APP_NUM";
    private static final String APP_PKG = "APP_PKG";
    private static final String APP_SIZE = "APP_SIZE";
    private static final String ARCHIVES = "ARCHIVES";
    private static final String DOC = "DOC";
    public static final String HAS_SEND_NOTIFY = "1";
    private static final String IS_NOTIFY = "IS_NOTIFY";
    private static final String LF_NUM = "LF_NUM";
    private static final String LF_SIZE = "LF_SIZE";
    private static final String MUSIC = "MUSIC";
    public static final String NOT_SEND_NOTIFY = "2";
    private static final String OTHER = "OTHER";
    private static final String PHOTO = "PHOTO";
    private static final String TAG = "FileAnalysisInfo";
    private static final String TIME = "TIME";
    private static final String VIDEO = "VIDEO";
    public static final String ZERO_SIZE = "0";
    public int mAppNum = 0;
    public String mAppPkgNames;
    public long mAppSize = 0;
    private List<InfoItemBean> mInfoItemBeans = new ArrayList();
    public String mIsNotify;
    public long mLargeFileApkSize = 0;
    public long mLargeFileArchivesSize = 0;
    public long mLargeFileDocSize = 0;
    public long mLargeFileMusicSize = 0;
    public int mLargeFileNum = 0;
    public long mLargeFileOtherSize = 0;
    public long mLargeFilePhotoSize = 0;
    public long mLargeFileSize = 0;
    public long mLargeFileVideoSize = 0;
    public long mTime = 0;

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

    public void setLargeFileVideoSize() {
        setItem(new InfoItemBean(VIDEO, String.valueOf(this.mLargeFileVideoSize)));
    }

    public void setLargeFilePhotoSize() {
        setItem(new InfoItemBean(PHOTO, String.valueOf(this.mLargeFilePhotoSize)));
    }

    public void setLargeFileApkSize() {
        setItem(new InfoItemBean(APK, String.valueOf(this.mLargeFileApkSize)));
    }

    public void setLargeFileDocSize() {
        setItem(new InfoItemBean(DOC, String.valueOf(this.mLargeFileDocSize)));
    }

    public void setLargeFileArchivesSize() {
        setItem(new InfoItemBean(ARCHIVES, String.valueOf(this.mLargeFileArchivesSize)));
    }

    public void setLargeFileOtherSize() {
        setItem(new InfoItemBean(OTHER, String.valueOf(this.mLargeFileOtherSize)));
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

    public void setIsNotify() {
        setItem(new InfoItemBean(IS_NOTIFY, this.mIsNotify));
    }

    public String getReport() {
        setTime();
        setLargeFileNum();
        setLargeFileSize();
        setLargeFileMusicSize();
        setLargeFileVideoSize();
        setLargeFilePhotoSize();
        setLargeFileApkSize();
        setLargeFileDocSize();
        setLargeFileArchivesSize();
        setLargeFileOtherSize();
        setAppNum();
        setAppSize();
        setAppPkgs();
        setIsNotify();
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
