package com.huawei.harassmentinterception.blackwhitelist;

import android.content.Context;

public interface ExistInListCallBack {
    boolean isExistInList(Context context, String str);
}
