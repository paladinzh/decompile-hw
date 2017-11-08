package com.huawei.systemmanager.adblock.ui.connect.request;

import android.content.Context;
import android.content.pm.PackageInfo;
import com.huawei.systemmanager.adblock.comm.AdBlock;
import com.huawei.systemmanager.adblock.comm.AdConst.CloudRequest;
import com.huawei.systemmanager.adblock.comm.AdUtils;
import com.huawei.systemmanager.adblock.ui.connect.result.AdBlockResultHandle;
import com.huawei.systemmanager.rainbow.comm.request.AbsAdRequest;
import com.huawei.systemmanager.rainbow.comm.request.ICommonRequest.RequestType;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AdBlockRequest extends AbsAdRequest implements CloudRequest {
    private static final String TAG = "AdBlockRequest";
    private List<AdBlock> mAdBlocks;
    private final int mUpdateType;

    public AdBlockRequest(int updateType) {
        this.mUpdateType = updateType;
    }

    protected String getRequestUrl(RequestType type) {
        return "https://appsec.hicloud.com/hwmarket/installmgr/client/api";
    }

    protected void addExtPostRequestParam(Context ctx, Map<String, String> map) {
        addAdBlockExtParam(ctx, map);
    }

    protected int checkResponseCode(Context ctx, int resultCode) {
        return 0;
    }

    protected void parseResponseAndPost(Context ctx, JSONObject jsonResponse) throws JSONException {
        HwLog.i(TAG, "parseResponseAndPost: " + jsonResponse.toString());
        new AdBlockResultHandle().handleResult(ctx, jsonResponse, this.mAdBlocks, this.mUpdateType);
    }

    private void addAdBlockExtParam(Context ctx, Map<String, String> map) {
        map.put("method", "store.getControlStrategy");
        map.put("apps", getAppList(ctx));
    }

    private String getAppList(Context context) {
        switch (this.mUpdateType) {
            case 1:
                return getPartAppList(context);
            case 2:
                return getAllAppList(context);
            default:
                return "";
        }
    }

    private String getAllAppList(Context context) {
        this.mAdBlocks = AdBlock.getAllAdBlocks(context);
        HwLog.i(TAG, "getAllAppList mAdBlocks.size=" + this.mAdBlocks.size());
        try {
            JSONArray jArray = new JSONArray();
            for (PackageInfo info : PackageManagerWrapper.getInstalledPackages(context.getPackageManager(), 8192)) {
                if (!AdUtils.isSystem(info)) {
                    AdBlock adBlock = AdBlock.getByPkgName(this.mAdBlocks, info.packageName);
                    if (adBlock == null) {
                        adBlock = new AdBlock(info.packageName, info.versionCode, info.versionName);
                        this.mAdBlocks.add(adBlock);
                    } else {
                        adBlock.setVersionCode(info.versionCode);
                        adBlock.setVersionName(info.versionName);
                    }
                    resetOneApp(adBlock);
                    jArray.put(getOneApp(adBlock));
                }
            }
            return jArray.toString();
        } catch (JSONException e) {
            HwLog.e(TAG, "getAllAppList", e);
        } catch (RuntimeException e2) {
            HwLog.e(TAG, "getAllAppList", e2);
        }
        return "";
    }

    private String getPartAppList(Context context) {
        this.mAdBlocks = AdBlock.getAdBlocks(context, "dirty=1", null, null);
        HwLog.i(TAG, "getPartAppList mAdBlocks.size=" + this.mAdBlocks.size());
        try {
            JSONArray jArray = new JSONArray();
            for (AdBlock adBlock : this.mAdBlocks) {
                resetOneApp(adBlock);
                jArray.put(getOneApp(adBlock));
            }
            return jArray.toString();
        } catch (JSONException e) {
            HwLog.e(TAG, "getAllAppList", e);
            return "";
        } catch (RuntimeException e2) {
            HwLog.e(TAG, "getAllAppList", e2);
            return "";
        }
    }

    private void resetOneApp(AdBlock adBlock) {
        adBlock.setDirty(false);
        adBlock.setViews("");
        adBlock.setViewIds("");
        adBlock.setDlCheck(false);
        adBlock.setUrls("");
    }

    private JSONObject getOneApp(AdBlock adBlock) throws JSONException {
        JSONObject item = new JSONObject();
        HwLog.i(TAG, "getOneApp pkg=" + adBlock.getPkgName());
        item.put("pkgName", adBlock.getPkgName());
        item.put(CloudRequest.PARAM_VERSION_CODE, adBlock.getVersionCode());
        item.put("version", adBlock.getVersionName());
        return item;
    }
}
