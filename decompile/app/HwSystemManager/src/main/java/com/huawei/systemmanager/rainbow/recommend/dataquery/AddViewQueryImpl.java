package com.huawei.systemmanager.rainbow.recommend.dataquery;

import android.content.Context;
import com.huawei.systemmanager.addviewmonitor.AddViewAppInfo;
import com.huawei.systemmanager.addviewmonitor.AddViewAppManager;
import com.huawei.systemmanager.rainbow.recommend.base.ConfigurationItem;
import com.huawei.systemmanager.rainbow.recommend.base.DataConstructUtils;
import java.util.List;
import java.util.Map;

class AddViewQueryImpl implements IConfigItemQuery {
    AddViewQueryImpl() {
    }

    public Map<String, List<ConfigurationItem>> getConfigurationOfItems(Context ctx) {
        Map<String, List<ConfigurationItem>> result = DataConstructUtils.generateEmptyResult();
        for (AddViewAppInfo info : AddViewAppManager.getInstance(ctx).initAddViewAppList()) {
            int i;
            String pkgName = info.mPkgName;
            DataConstructUtils.generateDefaultPackageItemList(result, pkgName);
            List list = (List) result.get(pkgName);
            if (info.mAddViewAllow) {
                i = 0;
            } else {
                i = 1;
            }
            list.add(new ConfigurationItem(6, i));
        }
        return result;
    }
}
