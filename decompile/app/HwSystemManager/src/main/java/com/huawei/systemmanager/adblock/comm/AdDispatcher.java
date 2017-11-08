package com.huawei.systemmanager.adblock.comm;

import android.content.Context;
import android.text.TextUtils;
import com.hsm.adblock.HsmAdCleanerManagerEx;
import com.hsm.adblock.HsmNetworkManagerEx;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AdDispatcher {
    private static final int AD_APK_DL = 0;
    private static final int AD_URL = 0;
    private static final String TAG = "AdBlock_AdDispatcher";

    public static void clearPackage(AdBlock adBlock) {
        clearPackages(Arrays.asList(new AdBlock[]{adBlock}), true);
    }

    private static void clearPackages(List<AdBlock> adBlocks, boolean includeDl) {
        List<String> viewList = new ArrayList();
        List<String> urlList = new ArrayList();
        List<String> dlList = new ArrayList();
        for (AdBlock adBlock : adBlocks) {
            if (!(TextUtils.isEmpty(adBlock.getViews()) && TextUtils.isEmpty(adBlock.getViewIds()))) {
                viewList.add(adBlock.getPkgName());
            }
            if (includeDl && adBlock.isDlCheck()) {
                dlList.add(adBlock.getPkgName());
            }
            if (!TextUtils.isEmpty(adBlock.getUrls()) || !TextUtils.isEmpty(adBlock.getTxUrls())) {
                urlList.add(adBlock.getPkgName());
            }
        }
        if (!viewList.isEmpty()) {
            HsmAdCleanerManagerEx.cleanAdFilterRules(viewList, false);
        }
        if (!urlList.isEmpty()) {
            HsmNetworkManagerEx.clearAdOrApkDlFilterRules((String[]) urlList.toArray(new String[0]), false, 0);
        }
        if (!dlList.isEmpty()) {
            HsmNetworkManagerEx.clearAdOrApkDlFilterRules((String[]) dlList.toArray(new String[0]), false, 0);
        }
    }

    public static void enablePackages(List<AdBlock> adBlocks, boolean enable) {
        if (enable) {
            setAdViewStrategy(adBlocks, false);
            setAdUrlStrategy(adBlocks, false);
            return;
        }
        clearPackages(adBlocks, false);
    }

    public static void setAdStrategy(Context context, AdBlock adBlock) {
        setAdStrategy(context, Arrays.asList(new AdBlock[]{adBlock}), false);
    }

    public static void setAdStrategy(Context context, List<AdBlock> adBlocks, boolean needReset) {
        setAdViewStrategy(adBlocks, needReset);
        setAdUrlStrategy(adBlocks, needReset);
        setApkDownloadBlackList(context, adBlocks, needReset);
    }

    public static void setAdViewStrategy(List<AdBlock> adBlocks, boolean needReset) {
        HashMap<String, List<String>> viewNameMap = new HashMap();
        HashMap<String, List<String>> viewIdMap = new HashMap();
        for (AdBlock adBlock : adBlocks) {
            if (adBlock.isEnable()) {
                List<String> views = adBlock.getViewList();
                if (!views.isEmpty()) {
                    viewNameMap.put(adBlock.getPkgName(), views);
                }
                List<String> viewIds = adBlock.getViewIdList();
                if (!viewIds.isEmpty()) {
                    viewIdMap.put(adBlock.getPkgName(), viewIds);
                }
            }
        }
        HwLog.i(TAG, "setAdViewStrategy viewNameMap=" + viewNameMap.toString() + ", viewIdMap=" + viewIdMap.toString() + ", needReset=" + needReset);
        if (needReset) {
            HsmAdCleanerManagerEx.setAdFilterRules(viewNameMap, viewIdMap, true);
        } else if (!viewNameMap.isEmpty() || !viewIdMap.isEmpty()) {
            HsmAdCleanerManagerEx.setAdFilterRules(viewNameMap, viewIdMap, false);
        }
    }

    public static void setAdUrlStrategy(List<AdBlock> adBlocks, boolean needReset) {
        HashMap<String, List<String>> urlMap = new HashMap();
        for (AdBlock adBlock : adBlocks) {
            if (adBlock.isEnable()) {
                List<String> urls = adBlock.getAllUrlList();
                if (!urls.isEmpty()) {
                    urlMap.put(adBlock.getPkgName(), urls);
                }
            }
        }
        HwLog.i(TAG, "setAdUrlStrategy urlMap=" + urlMap.toString() + ", needReset=" + needReset);
        if (needReset) {
            HsmNetworkManagerEx.setAdFilterRules(urlMap, true);
        } else if (!urlMap.isEmpty()) {
            HsmNetworkManagerEx.setAdFilterRules(urlMap, false);
        }
    }

    public static void setApkDownloadBlackList(Context context, List<AdBlock> adBlocks, boolean needReset) {
        if (AdUtils.isDlCheckEnable(context)) {
            List<String> dlList = new ArrayList();
            for (AdBlock adBlock : adBlocks) {
                if (adBlock.isDlCheck()) {
                    dlList.add(adBlock.getPkgName());
                }
            }
            HwLog.i(TAG, "setApkDownloadBlackList blackList=" + dlList.toString() + ", needReset=" + needReset);
            String[] blackArray = (String[]) dlList.toArray(new String[0]);
            if (needReset) {
                HsmNetworkManagerEx.setApkDlFilterRules(blackArray, true);
            } else if (blackArray.length > 0) {
                HsmNetworkManagerEx.setApkDlFilterRules(blackArray, false);
            }
            return;
        }
        HwLog.i(TAG, "setApkDownloadBlackList hw_download_non_market_apps is open, should not block");
        clearApkDownloadBlackList();
    }

    public static void clearApkDownloadBlackList() {
        HwLog.i(TAG, "clearApkDownloadBlackList");
        HsmNetworkManagerEx.clearAdOrApkDlFilterRules(new String[0], true, 0);
    }

    public static void setApkDlUrlUserResult(String downloadId, boolean isContinue) {
        HwLog.i(TAG, "setApkDlUrlUserResult downloadId=" + downloadId + ", isContinue=" + isContinue);
        HsmNetworkManagerEx.setApkDlUrlUserResult(downloadId, isContinue);
    }

    public static void printRules() {
        HwLog.i(TAG, "printRules");
        HsmAdCleanerManagerEx.printRuleMaps();
        HsmNetworkManagerEx.printAdOrApkDlFilterRules(0);
        HsmNetworkManagerEx.printAdOrApkDlFilterRules(0);
    }
}
