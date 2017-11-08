package com.huawei.harassmentinterception.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.engine.HwEngineCaller;
import com.huawei.harassmentinterception.engine.HwEngineCallerManager;
import com.huawei.harassmentinterception.numbermark.HsmNumberMarkerManager;
import com.huawei.harassmentinterception.strategy.StrategyConfigs.StrategyId;
import com.huawei.harassmentinterception.util.PreferenceHelper;
import com.huawei.harassmentinterception.util.SmsIntentHelper;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.urlrecognition.OfficalUrlRecognitionManager;
import com.huawei.systemmanager.urlrecognition.UrlRecognitionManager;
import com.huawei.systemmanager.util.HwLog;
import java.util.Arrays;
import java.util.List;

public class BlockCheckProvider extends ContentProvider {
    public static final String AUTH = "com.huawei.systemmanager.BlockCheckProvider";
    public static final String BLOCK_SMS = "block_sms";
    public static final String CHECK_URL = "checkurl";
    private static final int INDICATOR_BLOCK_SMS_ALL = 1;
    private static final int INDICATOR_BLOCK_SMS_ONE = 2;
    private static final int INDICATOR_CHECKURL = 4;
    private static final int INDICATOR_NUMBERMARK = 3;
    private static final int INDICATOR_QUERY_NUMBERMARKER = 5;
    public static final String NUMBER_MARK = "numbermark";
    public static final int NUMBER_MARK_QUERY_TYPE_AUTO = 2;
    public static final int NUMBER_MARK_QUERY_TYPE_LOCAL = 0;
    public static final int NUMBER_MARK_QUERY_TYPE_ONLINE = 1;
    public static final String QUERY_NUMBER_MARKER = "query_numbermark";
    private static final String RISK_URL_CHECK_RESULT = "result";
    private static final String TAG = BlockCheckProvider.class.getSimpleName();
    private static UriMatcher mUriMatcher = new UriMatcher(-1);
    private OfficalUrlRecognitionManager mOfficalUrlRecognitionManager = new OfficalUrlRecognitionManager();
    private UrlRecognitionManager mUrlRecognitionManager = new UrlRecognitionManager();

    static {
        mUriMatcher.addURI(AUTH, BLOCK_SMS, 1);
        mUriMatcher.addURI(AUTH, "block_sms/#", 2);
        mUriMatcher.addURI(AUTH, "numbermark/#", 3);
        mUriMatcher.addURI(AUTH, "checkurl/#", 4);
        mUriMatcher.addURI(AUTH, QUERY_NUMBER_MARKER, 5);
    }

    public int delete(Uri arg0, String arg1, String[] arg2) {
        return 0;
    }

    public String getType(Uri arg0) {
        return null;
    }

    public Uri insert(Uri arg0, ContentValues arg1) {
        return null;
    }

    public boolean onCreate() {
        return true;
    }

    public Cursor query(Uri uri, String[] arg1, String arg2, String[] arg3, String arg4) {
        int nMatchCode = mUriMatcher.match(uri);
        HwLog.i(TAG, "do query called, match code is:" + nMatchCode);
        if (4 == nMatchCode) {
            HsmStat.statE(Events.E_HARASSMENT_CHECK_URL);
            return getCheckUrlResult(arg1);
        } else if (5 == nMatchCode) {
            return doQueryNumberMark(uri, arg1, arg2, arg3, arg4);
        } else {
            if (3 != nMatchCode) {
                HwLog.w(TAG, "query: Unsupported Uri = " + uri);
                return null;
            }
            String number = uri.getLastPathSegment();
            if (TextUtils.isEmpty(number)) {
                HwLog.w(TAG, "query: Invalid Uri = " + uri);
                return null;
            } else if (arg1 == null || arg1.length == 0) {
                HwLog.w(TAG, "parment is empty or null");
                return null;
            } else {
                int queryType = 0;
                int requestType = 16;
                long timeout = 0;
                HwLog.i(TAG, "paramenter = " + Arrays.deepToString(arg1));
                switch (arg1.length) {
                    case 1:
                        try {
                            queryType = Integer.parseInt(arg1[0]);
                            break;
                        } catch (Exception e) {
                            HwLog.w(TAG, "NumberFormatException: Invalid " + e.getMessage());
                            break;
                        }
                    case 2:
                        try {
                            queryType = Integer.parseInt(arg1[0]);
                            requestType = Integer.parseInt(arg1[1]);
                            break;
                        } catch (NumberFormatException e2) {
                            HwLog.w(TAG, "NumberFormatException: Invalid " + e2.getMessage());
                            break;
                        }
                    case 3:
                        try {
                            queryType = Integer.parseInt(arg1[0]);
                            requestType = Integer.parseInt(arg1[1]);
                            timeout = Long.parseLong(arg1[2]);
                            break;
                        } catch (NumberFormatException e22) {
                            HwLog.w(TAG, "NumberFormatException: Invalid " + e22.getMessage());
                            break;
                        }
                    default:
                        HwLog.e(TAG, "parment not support");
                        break;
                }
                return HsmNumberMarkerManager.getInstance(getContext()).getNumberMarkInfo(number, timeout, queryType, requestType);
            }
        }
    }

    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        return 0;
    }

    public Bundle call(String method, String arg, Bundle extras) {
        super.call(method, arg, extras);
        Bundle bundle = new Bundle();
        boolean z = false;
        if (TextUtils.isEmpty(method)) {
            HwLog.d(TAG, "Call method is empty");
            bundle.putBoolean(ConstValues.KEY_RESULT_IS_BLOCK, false);
            return bundle;
        }
        HwEngineCaller caller = HwEngineCallerManager.getInstance().getEngineCaller();
        if (caller == null) {
            if (isEngineSwitchOn()) {
                caller = new HwEngineCaller(GlobalContext.getContext());
                HwEngineCallerManager.getInstance().setEngineCaller(caller);
                caller.onSwitchIn(0);
                HwLog.d(TAG, "UI process die, so we need to reset HwEngineCallerManager");
            } else {
                HwLog.d(TAG, "Fail to get Engine Caller , Engine is off");
                bundle.putBoolean(ConstValues.KEY_RESULT_IS_BLOCK, false);
                return bundle;
            }
        }
        if (ConstValues.METHOD_ISBLOCKSMS.equals(method)) {
            Intent intent = (Intent) extras.getParcelable(ConstValues.KEY_SMS_PARAM);
            z = caller.handleSms(new SmsIntentWrapper(SmsIntentHelper.getSmsInfoFromIntent(GlobalContext.getContext(), intent), intent));
            HwLog.d(TAG, "isBlockSMS: " + z);
        }
        bundle.putBoolean(ConstValues.KEY_RESULT_IS_BLOCK, z);
        return bundle;
    }

    private boolean isEngineSwitchOn() {
        if ((StrategyId.BLOCK_INTELLIGENT.getValue() & PreferenceHelper.getInterceptionStrategy(getContext())) != 0) {
            return true;
        }
        return false;
    }

    private Cursor getCheckUrlResult(String[] args) {
        if (args == null || args.length < 1) {
            return null;
        }
        String url = args[0];
        if (TextUtils.isEmpty(url)) {
            HwLog.w(TAG, "query: Invalid Uri = " + url);
            return null;
        } else if (args.length < 2 || TextUtils.isEmpty(args[1])) {
            return this.mUrlRecognitionManager.getUrlCheckInfo(url);
        } else {
            int checkResult = this.mOfficalUrlRecognitionManager.checkOfficialLink(getContext(), args);
            HwLog.i(TAG, "OfficalUrlCheckResult = " + checkResult);
            if (checkResult != 0 && checkMaliciousLink(url) == 1) {
                checkResult = 3;
            }
            return this.mOfficalUrlRecognitionManager.getCursorFromCheckUrlResult(checkResult);
        }
    }

    private int checkMaliciousLink(String url) {
        int result = 0;
        Cursor urlCheckCursor = this.mUrlRecognitionManager.getUrlCheckInfo(url);
        if (urlCheckCursor != null && urlCheckCursor.getCount() > 0 && urlCheckCursor.moveToFirst()) {
            result = urlCheckCursor.getInt(urlCheckCursor.getColumnIndex("result"));
        }
        if (urlCheckCursor != null) {
            urlCheckCursor.close();
        }
        return result;
    }

    private Cursor doQueryNumberMark(Uri uri, String[] arg1, String arg2, String[] arg3, String arg4) {
        if (HsmCollections.isArrayEmpty(arg3)) {
            HwLog.w(TAG, "doQueryNumberMark called, but number array is null!");
            return null;
        }
        List<String> numbers = HsmCollections.newArrayList((Object[]) arg3);
        long timeout = 0;
        int requestType = 16;
        if (arg1 != null) {
            try {
                if (arg1.length >= 1) {
                    timeout = Long.parseLong(arg1[0]);
                }
                if (arg1.length >= 2) {
                    requestType = Integer.parseInt(arg1[1]);
                }
            } catch (NumberFormatException e) {
                HwLog.e(TAG, "doQueryNumberMark, NumberFormatException", e);
            } catch (Exception e2) {
                HwLog.e(TAG, "doQueryNumberMark, Exception", e2);
            }
        }
        HwLog.i(TAG, "doQueryNumberMark called, number num:" + numbers.size());
        return HsmNumberMarkerManager.getInstance(getContext()).getMultiNumberMarkInfo(numbers, timeout, requestType);
    }
}
