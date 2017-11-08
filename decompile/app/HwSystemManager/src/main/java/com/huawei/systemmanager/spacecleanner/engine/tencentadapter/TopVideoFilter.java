package com.huawei.systemmanager.spacecleanner.engine.tencentadapter;

import com.google.common.collect.Sets;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.engine.SpaceXmlHelper;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.Set;

public class TopVideoFilter {
    private static Set<String> mTopVideoSet = Sets.newHashSet();

    public static boolean checkIsTopVideo(String pkgName) {
        initTopVideoSet();
        return mTopVideoSet.contains(pkgName);
    }

    private static void initTopVideoSet() {
        if (mTopVideoSet != null && mTopVideoSet.size() == 0) {
            mTopVideoSet.clear();
            mTopVideoSet.addAll(SpaceXmlHelper.getTopVideoList(GlobalContext.getContext()));
            HwLog.i(SpaceConst.TAG, "top video set is:" + mTopVideoSet);
        }
    }

    public static Set<String> getTopVideoApp() {
        initTopVideoSet();
        return mTopVideoSet;
    }
}
