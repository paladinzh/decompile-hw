package com.huawei.mms.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import cn.com.xy.sms.sdk.constant.Constant;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HwCloudBackUpManager {
    public static final Long HW_CLOUD_SERVICE_ALERT_INTERVAL = Long.valueOf(Constant.month);
    private static HwCloudBackUpManager instance;
    private AlertDialog mDialog;
    private boolean mNeedShowHwCloudServiceGuideAlert;

    public static HwCloudBackUpManager getInstance() {
        if (instance == null) {
            instance = new HwCloudBackUpManager();
        }
        return instance;
    }

    public void checkNeedShowHwCloudAlert(Context context) {
        boolean z = false;
        if (context != null) {
            if (isGuideActivityExist(context) && checkhwCloudAlertGuideShowTime(context) && isHwCloudServiceClose(context) && UserHandle.myUserId() == 0) {
                z = true;
            }
            this.mNeedShowHwCloudServiceGuideAlert = z;
            MLog.i("HwCloudUtils", "checkNeedShowHwCloudAlert result " + this.mNeedShowHwCloudServiceGuideAlert);
        }
    }

    private boolean checkhwCloudAlertGuideShowTime(Context context) {
        Long currentTimeMillis = Long.valueOf(System.currentTimeMillis());
        Set hwCloudServiceAlertTimes = PreferenceManager.getDefaultSharedPreferences(context).getStringSet("PREFERENCE_HW_CLOUD_SERVICE_ALERT_TIMES", new HashSet());
        MLog.i("HwCloudUtils", "hwCloudServiceAlertTimes set " + hwCloudServiceAlertTimes);
        if (hwCloudServiceAlertTimes.size() > 1) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("PREFERENCE_HW_CLOUD_SERVICE_ALERT_TIMES_STRING", "0;" + getLastShowTimeMillis(hwCloudServiceAlertTimes).longValue()).commit();
            return false;
        }
        if (hwCloudServiceAlertTimes.size() == 1) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("PREFERENCE_HW_CLOUD_SERVICE_ALERT_TIMES_STRING", String.valueOf(hwCloudServiceAlertTimes.toArray()[0])).commit();
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove("PREFERENCE_HW_CLOUD_SERVICE_ALERT_TIMES").commit();
        String hwCloudServiceAlertTimesString = PreferenceManager.getDefaultSharedPreferences(context).getString("PREFERENCE_HW_CLOUD_SERVICE_ALERT_TIMES_STRING", "");
        if (TextUtils.isEmpty(hwCloudServiceAlertTimesString)) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("PREFERENCE_HW_CLOUD_SERVICE_ALERT_TIMES_STRING", String.valueOf(System.currentTimeMillis())).commit();
            return false;
        } else if (hwCloudServiceAlertTimesString.split(";").length >= 2) {
            return false;
        } else {
            if (currentTimeMillis.longValue() - Long.valueOf(getLastShowTimeMillis(hwCloudServiceAlertTimesString.split(";"))).longValue() < HW_CLOUD_SERVICE_ALERT_INTERVAL.longValue()) {
                return false;
            }
            MLog.i("HwCloudUtils", "checkhwCloudAlertGuideShowTime result true ");
            return true;
        }
    }

    public boolean isHwCloudServiceClose(Context context) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.parse("content://com.huawei.android.hicloud.SwitchStatusProvider/backup"), new String[]{"switch_status"}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int result = cursor.getInt(0);
                    if (result == 0) {
                        MLog.d("HwCloudUtils", "HwCloudUtils isHwCloudServiceOpen " + result);
                        if (!cursor.isClosed()) {
                            cursor.close();
                        }
                        if (!(cursor == null || cursor.isClosed())) {
                            cursor.close();
                        }
                        return true;
                    }
                }
            }
            if (!(cursor == null || cursor.isClosed())) {
                cursor.close();
            }
        } catch (Exception exception) {
            MLog.e("HwCloudUtils", exception.getMessage());
            if (!(cursor == null || cursor.isClosed())) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (!(cursor == null || cursor.isClosed())) {
                cursor.close();
            }
        }
        return false;
    }

    public void switchToHwCloudService(Context context) {
        Intent intent = new Intent("com.huawei.hicloud.action.EXTERNAL_LOGIN");
        Bundle bundle = new Bundle();
        bundle.putString("module", "sms");
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    private void addCurrentTimeStampToAlertShowRecords(Context context) {
        String original = PreferenceManager.getDefaultSharedPreferences(context).getString("PREFERENCE_HW_CLOUD_SERVICE_ALERT_TIMES_STRING", "");
        MLog.i("HwCloudUtils", "addCurrentTimeStampToAlertShowRecords before alertTimes " + original);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("PREFERENCE_HW_CLOUD_SERVICE_ALERT_TIMES_STRING", original + ";" + String.valueOf(System.currentTimeMillis())).commit();
        MLog.i("HwCloudUtils", "addCurrentTimeStampToAlertShowRecords after alertTimes " + PreferenceManager.getDefaultSharedPreferences(context).getString("PREFERENCE_HW_CLOUD_SERVICE_ALERT_TIMES_STRING", ""));
    }

    private Long getLastShowTimeMillis(Set<String> hwCloudServiceAlertTimes) {
        Long lastShowTimeMllis = Long.valueOf(0);
        int length = hwCloudServiceAlertTimes.size();
        if (length == 0) {
            return lastShowTimeMllis;
        }
        String[] hwCloludAlertTimes = new String[length];
        hwCloudServiceAlertTimes.toArray(hwCloludAlertTimes);
        Arrays.sort(hwCloludAlertTimes);
        lastShowTimeMllis = Long.valueOf(Long.parseLong(hwCloludAlertTimes[length - 1]));
        MLog.i("HwCloudUtils", "getLastShowTimeMillis " + lastShowTimeMllis);
        return lastShowTimeMllis;
    }

    private long getLastShowTimeMillis(String[] hwCloudServiceAlertTimes) {
        Long lastShowTimeMllis = Long.valueOf(0);
        int length = hwCloudServiceAlertTimes.length;
        if (length == 0) {
            return lastShowTimeMllis.longValue();
        }
        Arrays.sort(hwCloudServiceAlertTimes);
        lastShowTimeMllis = Long.valueOf(Long.parseLong(hwCloudServiceAlertTimes[length - 1]));
        MLog.i("HwCloudUtils", "getLastShowTimeMillis " + lastShowTimeMllis);
        return lastShowTimeMllis.longValue();
    }

    public void showHwCloudServiceGuideAlert(final Context context) {
        if (context != null) {
            HwBackgroundLoader.getUIHandler().postDelayed(new Runnable() {
                public void run() {
                    if (HwCloudBackUpManager.this.mDialog == null && HwCloudBackUpManager.this.mNeedShowHwCloudServiceGuideAlert) {
                        Builder builder = new Builder(context);
                        View view = View.inflate(context, R.layout.cloud_backup_dialog, null);
                        builder.setTitle(R.string.hw_cloud_alert_title_new).setView(view);
                        Button confirmButton = (Button) view.findViewById(R.id.confirm_cloud_backup);
                        Button cancelButton = (Button) view.findViewById(R.id.cancel_cloud_backup);
                        HwCloudBackUpManager.this.mDialog = builder.show();
                        final Context context = context;
                        confirmButton.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                HwCloudBackUpManager.this.switchToHwCloudService(context);
                                HwCloudBackUpManager.this.dismissHwCloudServiceGuideAlert();
                                StatisticalHelper.incrementReportCount(context, 2243);
                            }
                        });
                        context = context;
                        cancelButton.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                HwCloudBackUpManager.this.dismissHwCloudServiceGuideAlert();
                                StatisticalHelper.incrementReportCount(context, 2244);
                            }
                        });
                        HwCloudBackUpManager.this.addCurrentTimeStampToAlertShowRecords(context);
                    }
                }
            }, 100);
        }
    }

    public void dismissHwCloudServiceGuideAlert() {
        if (this.mDialog != null) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
    }

    private boolean isGuideActivityExist(Context context) {
        if (new Intent("com.huawei.hicloud.action.EXTERNAL_LOGIN").resolveActivity(context.getPackageManager()) != null) {
            return true;
        }
        return false;
    }
}
