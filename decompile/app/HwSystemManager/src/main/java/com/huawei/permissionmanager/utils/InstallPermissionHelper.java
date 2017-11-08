package com.huawei.permissionmanager.utils;

import android.content.Context;
import android.util.SparseIntArray;
import com.huawei.permissionmanager.db.RecommendDBHelper;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderConst;
import com.huawei.systemmanager.rainbow.comm.base.CloudProviderConst.RecommendCallMethod;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendItem;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendParamException;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendQueryInput;
import com.huawei.systemmanager.rainbow.db.recommend.RecommendQueryOutput;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class InstallPermissionHelper {
    private static final String LOG_TAG = "InstallPermissionHelper";

    public static com.huawei.permissionmanager.utils.PermissionBase getPermissionBaseInfo(android.content.Context r1, int r2, int r3, int r4, java.lang.String r5) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.permissionmanager.utils.InstallPermissionHelper.getPermissionBaseInfo(android.content.Context, int, int, int, java.lang.String):com.huawei.permissionmanager.utils.PermissionBase
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.permissionmanager.utils.InstallPermissionHelper.getPermissionBaseInfo(android.content.Context, int, int, int, java.lang.String):com.huawei.permissionmanager.utils.PermissionBase");
    }

    private static SparseIntArray getRecommendItemMap(Context context, String pkgName) {
        if (RecommendDBHelper.getInstance(context).getRecommendPackageInstallSwitchStatus()) {
            List<RecommendItem> recommendList = new ArrayList();
            try {
                recommendList = (List) RecommendQueryOutput.fromBundle(context.getContentResolver().call(CloudProviderConst.CLOUD_AUTHORITY_URI, RecommendCallMethod.CALL_METHOD_QUERY_RECOMMEND, null, RecommendQueryInput.generateOnePkgMultiItemInput(6, pkgName, null))).get(pkgName);
            } catch (RecommendParamException e) {
                HwLog.e(LOG_TAG, "error getRecommendItemList RecommendParamException");
            } catch (Exception e2) {
                HwLog.e(LOG_TAG, "error getRecommendItemList Exception");
            }
            if (recommendList == null || recommendList.isEmpty()) {
                return null;
            }
            SparseIntArray permissionIdMap = PermissionMap.getPermissionIdMap();
            if (permissionIdMap.size() == 0) {
                return null;
            }
            SparseIntArray permissionRecommendMap = new SparseIntArray();
            for (RecommendItem recommendItem : recommendList) {
                int permissionType = permissionIdMap.get(recommendItem.getConfigItemId());
                int recommendValue = RecommendCfg.getCfgFromRecommendVaule(recommendItem.getConfigType());
                if (recommendValue != 0) {
                    permissionRecommendMap.put(permissionType, recommendValue);
                }
            }
            return permissionRecommendMap;
        }
        HwLog.e(LOG_TAG, "The install recommend switch is closed now!");
        return null;
    }
}
