package com.huawei.optimizer.addetect;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import java.util.List;

public interface IAdAppInfo extends Parcelable {
    boolean deleteApp(Context context);

    int getActionsSize();

    List<String> getAdPlatformInfoList();

    int getAdPlatformSize();

    String getApkVersion();

    String getAppLabel();

    List<String> getCommonActionsDescription();

    Drawable getIcon();

    int getOperationDescripId();

    String getPackageName();

    List<String> getRiskActionsDescription();

    boolean isDeleted(Context context);
}
