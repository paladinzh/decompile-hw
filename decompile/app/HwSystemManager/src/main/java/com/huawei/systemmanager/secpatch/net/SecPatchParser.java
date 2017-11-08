package com.huawei.systemmanager.secpatch.net;

import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.secpatch.common.SecPatchCheckResult;
import com.huawei.systemmanager.secpatch.common.SecPatchQueryResult;
import com.huawei.systemmanager.secpatch.common.SecPatchQueryResult.SecPatchDetail;
import com.huawei.systemmanager.secpatch.common.SecPatchQueryResult.SecPatchVerInfo;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SecPatchParser {
    private static final String TAG = "SecPatchParser";

    public static SecPatchQueryResult parseSecPatchList(String jsonInfo) {
        SecPatchQueryResult secpatchInfo = new SecPatchQueryResult();
        if (TextUtils.isEmpty(jsonInfo) || !isValidFormat(jsonInfo)) {
            HwLog.w(TAG, "parseSecPatchList: Invalid jsonInfo");
            return secpatchInfo;
        }
        try {
            return parseSecPatchList(new JSONObject(jsonInfo));
        } catch (JSONException e) {
            HwLog.e(TAG, "parseSecPatchList: Exception", e);
            return secpatchInfo;
        }
    }

    public static SecPatchQueryResult parseSecPatchList(JSONObject jsonResponse) {
        HwLog.d(TAG, "parseSecPatchList: Starts");
        try {
            if (jsonResponse.has(ConstValues.KEY_RESULT_CODE)) {
                int nSrvCode = jsonResponse.getInt(ConstValues.KEY_RESULT_CODE);
                HwLog.i(TAG, "parseSecPatchList: Server code = " + nSrvCode);
                if (!isValidQueryResponse(nSrvCode)) {
                    return new SecPatchQueryResult(nSrvCode);
                }
                String patchInfo = null;
                if (jsonResponse.has(ConstValues.KEY_INFO)) {
                    patchInfo = jsonResponse.getString(ConstValues.KEY_INFO);
                }
                if (jsonResponse.has(ConstValues.KEY_PVLIST)) {
                    return new SecPatchQueryResult(nSrvCode, patchInfo, parseSecPatchVerList(jsonResponse));
                }
                HwLog.w(TAG, "parseSecPatchList: No pvlist info ,stop");
                return new SecPatchQueryResult(nSrvCode, patchInfo);
            }
            HwLog.w(TAG, "parseSecPatchList: Fail to get server code ,stop");
            return new SecPatchQueryResult();
        } catch (JSONException e) {
            HwLog.e(TAG, "parseSecPatchList: Exception", e);
            return new SecPatchQueryResult();
        }
    }

    public static SecPatchCheckResult parseCheckResult(String respnose) {
        SecPatchCheckResult checkVersionInfo = new SecPatchCheckResult();
        if (TextUtils.isEmpty(respnose) || !isValidFormat(respnose)) {
            HwLog.w(TAG, "parseCheckResult: Invalid info");
            return checkVersionInfo;
        }
        try {
            return parseCheckResult(new JSONObject(respnose));
        } catch (JSONException e) {
            HwLog.e(TAG, "parseCheckResult: Exception", e);
            return checkVersionInfo;
        }
    }

    public static SecPatchCheckResult parseCheckResult(JSONObject jsonResponse) {
        HwLog.d(TAG, "parseCheckResult: Starts");
        try {
            if (jsonResponse.has(ConstValues.KEY_RESULT_CODE)) {
                int nResponseCode = jsonResponse.getInt(ConstValues.KEY_RESULT_CODE);
                if (jsonResponse.has(ConstValues.KEY_CHECK_ALL_VERSION)) {
                    long allVersion = Long.parseLong(jsonResponse.getString(ConstValues.KEY_CHECK_ALL_VERSION));
                    if (jsonResponse.has(ConstValues.KEY_CHECK_AVA_VERSION)) {
                        return new SecPatchCheckResult(nResponseCode, allVersion, Long.parseLong(jsonResponse.getString(ConstValues.KEY_CHECK_AVA_VERSION)));
                    }
                    HwLog.w(TAG, "parseCheckResult: Fail to get avaVersion ,stop");
                    return new SecPatchCheckResult(nResponseCode, allVersion);
                }
                HwLog.w(TAG, "parseCheckResult: Fail to get allVersion ,stop");
                return new SecPatchCheckResult(nResponseCode);
            }
            HwLog.w(TAG, "parseCheckResult: Fail to get server code ,stop");
            return new SecPatchCheckResult();
        } catch (JSONException e) {
            HwLog.e(TAG, "parseCheckResult: Exception", e);
            return new SecPatchCheckResult();
        } catch (NumberFormatException ex) {
            HwLog.e(TAG, "parseCheckResult: NumberFormatException", ex);
            return new SecPatchCheckResult();
        }
    }

    private static boolean isValidFormat(String jsonInfo) {
        try {
            if (new JSONTokener(jsonInfo).nextValue() instanceof JSONObject) {
                return true;
            }
            HwLog.w(TAG, "isValidFormat: Invalid data format");
            return false;
        } catch (JSONException e) {
            HwLog.e(TAG, "isValidFormat: Exception", e);
            return false;
        }
    }

    private static boolean isValidQueryResponse(int nCode) {
        switch (nCode) {
            case 1:
                HwLog.e(TAG, "isValidQueryResponse: Server - Fail");
                return false;
            case 2:
                HwLog.e(TAG, "isValidQueryResponse: Server - invalid params");
                return false;
            case 210:
                return true;
            case 211:
                return true;
            case ConstValues.SRV_CODE_AUTH_FAIL /*401*/:
                HwLog.e(TAG, "isValidQueryResponse: Server - Authentication failure");
                return false;
            case ConstValues.SRV_CODE_INNER_ERR /*508*/:
                HwLog.e(TAG, "isValidQueryResponse: Server - Inner error");
                return false;
            default:
                HwLog.e(TAG, "isValidQueryResponse: Server - Invalid return code = " + nCode);
                return false;
        }
    }

    private static List<SecPatchVerInfo> parseSecPatchVerList(JSONObject root) {
        try {
            List<SecPatchVerInfo> secPatchVerList = new ArrayList();
            String strPvlist = root.getString(ConstValues.KEY_PVLIST);
            if (TextUtils.isEmpty(strPvlist)) {
                HwLog.i(TAG, "parseSecPatchVerList: Empty patch ver list info");
                return secPatchVerList;
            }
            JSONArray pvArray = new JSONArray(strPvlist);
            int nPvCount = pvArray.length();
            if (nPvCount <= 0) {
                HwLog.i(TAG, "parseSecPatchVerList: Empty patch ver array");
                return secPatchVerList;
            }
            HwLog.d(TAG, "parseSecPatchVerList: Patch ver array size = " + nPvCount);
            for (int nPvIndex = 0; nPvIndex < nPvCount; nPvIndex++) {
                JSONObject pv = pvArray.getJSONObject(nPvIndex);
                if (pv.has("pver")) {
                    String strPver = pv.getString("pver");
                    if (TextUtils.isEmpty(strPver)) {
                        HwLog.w(TAG, "parseSecPatchVerList: Invalid pver info ,skip. index = " + nPvIndex);
                    } else {
                        List<SecPatchDetail> pvlist = parseSecPatchDetailList(pv);
                        if (Utility.isNullOrEmptyList(pvlist)) {
                            HwLog.w(TAG, "parseSecPatchVerList: Invalid pvlist info ,skip. Index = " + nPvIndex + ", pver = " + strPver);
                        } else {
                            HwLog.d(TAG, "parseSecPatchVerList: Index = " + nPvIndex + ", pver = " + strPver + ", patch count = " + pvlist.size());
                            secPatchVerList.add(new SecPatchVerInfo(strPver, pvlist));
                        }
                    }
                } else {
                    HwLog.w(TAG, "parseSecPatchVerList: No pver info ,skip. index = " + nPvIndex);
                }
            }
            return secPatchVerList;
        } catch (JSONException e) {
            HwLog.e(TAG, "parseSecPatchMap: Exception", e);
            return null;
        }
    }

    private static List<SecPatchDetail> parseSecPatchDetailList(JSONObject pv) {
        if (pv.has(ConstValues.KEY_PLIST)) {
            try {
                JSONArray pvlist = pv.getJSONArray(ConstValues.KEY_PLIST);
                int pvCount = pvlist.length();
                if (pvCount <= 0) {
                    HwLog.w(TAG, "parseSecPatchDetailList: Empty pvlst.");
                    return null;
                }
                List<SecPatchDetail> secpathList = new ArrayList();
                for (int nIndex = 0; nIndex < pvCount; nIndex++) {
                    JSONObject secPtach = pvlist.getJSONObject(nIndex);
                    if (secPtach.has("sid")) {
                        String strSid = secPtach.getString("sid");
                        if (secPtach.has("ocid")) {
                            String strOcid = secPtach.getString("ocid");
                            if (secPtach.has("src")) {
                                String strSrc = secPtach.getString("src");
                                if (secPtach.has("digest")) {
                                    String strDigest = secPtach.getString("digest");
                                    if (secPtach.has("digest_en")) {
                                        String strDigest_en = secPtach.getString("digest_en");
                                        if (secPtach.has("ver")) {
                                            secpathList.add(new SecPatchDetail(strSid, strOcid, strSrc, strDigest, strDigest_en, secPtach.getString("ver")));
                                        } else {
                                            HwLog.w(TAG, "parseSecPatchDetailList: No fix_version info. index = " + nIndex);
                                        }
                                    } else {
                                        HwLog.w(TAG, "parseSecPatchDetailList: No digest_en info. index = " + nIndex);
                                    }
                                } else {
                                    HwLog.w(TAG, "parseSecPatchDetailList: No digest info ,skip. index = " + nIndex);
                                }
                            } else {
                                HwLog.w(TAG, "parseSecPatchDetailList: No src info ,skip. index = " + nIndex);
                            }
                        } else {
                            HwLog.w(TAG, "parseSecPatchDetailList: No ocid info ,skip. index = " + nIndex);
                        }
                    } else {
                        HwLog.w(TAG, "parseSecPatchDetailList: No sid info ,skip. index = " + nIndex);
                    }
                }
                return secpathList;
            } catch (JSONException e) {
                HwLog.e(TAG, "parseSecPatchDetailList: Exception", e);
                return null;
            }
        }
        HwLog.w(TAG, "parseSecPatchDetailList: No pvlst info.");
        return null;
    }
}
