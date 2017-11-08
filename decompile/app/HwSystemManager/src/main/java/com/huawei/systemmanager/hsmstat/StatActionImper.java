package com.huawei.systemmanager.hsmstat;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.settingsearch.SettingSearchUtil;
import com.huawei.systemmanager.util.HwLog;
import java.util.Set;

/* compiled from: HsmStat */
class StatActionImper {
    private ActivityLifecycleCallbacks mActivityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        public void onActivityStopped(Activity activity) {
        }

        public void onActivityStarted(Activity activity) {
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        public void onActivityResumed(Activity activity) {
            StatActionImper.this.statActivityAction(activity, 2);
        }

        public void onActivityPaused(Activity activity) {
            StatActionImper.this.statActivityAction(activity, 3);
        }

        public void onActivityDestroyed(Activity activity) {
            StatActionImper.this.statActivityAction(activity, 4);
        }

        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                HwLog.i(HsmStatConst.TAG, "savedInstanceState is not null, didnot record");
            } else {
                StatActionImper.this.statActivityAction(activity, 1);
            }
        }
    };
    final IHsmStat mStater = new HsmStatProxy();
    private String newIntentActivityFrom;
    private String newIntentActivityName;

    StatActionImper() {
    }

    void initActivityCallBack(Application application) {
        if (isEnableInner()) {
            application.registerActivityLifecycleCallbacks(this.mActivityLifecycleCallbacks);
        }
    }

    void statActivityAction(Activity ac, int action) {
        if (checkShouldStat()) {
            String activityName = ac.getClass().getName();
            String from = "";
            if (action == 1) {
                from = checkIntentAndGuessFrom(ac.getIntent());
            } else if (action == 2 && activityName.equals(this.newIntentActivityName) && !TextUtils.isEmpty(this.newIntentActivityFrom)) {
                from = this.newIntentActivityFrom;
            }
            this.newIntentActivityName = "";
            this.newIntentActivityFrom = "";
            this.mStater.activityStat(action, activityName, from);
        }
    }

    void statE(String key, String... values) {
        if (checkShouldStat()) {
            statEInner(key, HsmStatConst.constructValue(values));
        }
    }

    private void statEInner(String key, String value) {
        this.mStater.eStat(key, value);
    }

    public void statR() {
        if (checkShouldStat()) {
            this.mStater.rStat();
        }
    }

    public boolean isEnable() {
        if (isEnableInner()) {
            return this.mStater.isEnable();
        }
        return false;
    }

    public boolean setEnable(boolean enbale) {
        if (isEnableInner()) {
            return this.mStater.setEnable(enbale);
        }
        return false;
    }

    private boolean checkShouldStat() {
        if (isEnableInner() && !ActivityManager.isUserAMonkey()) {
            return true;
        }
        return false;
    }

    private boolean isEnableInner() {
        if (HsmStatConst.isFeatureEnable()) {
            return true;
        }
        return false;
    }

    private String checkIntentAndGuessFrom(Intent intent) {
        String from = "";
        if (intent == null || Utility.hasUnparcelException(intent)) {
            return "";
        }
        from = checkInentIfFromNotification(intent);
        if (!TextUtils.isEmpty(from)) {
            return from;
        }
        from = intent.getStringExtra(HsmStatConst.PARAM_FROM_SHORT_CUT);
        if (!TextUtils.isEmpty(from)) {
            return from;
        }
        from = HsmStat.getSourceFromPackageName(intent.getStringExtra("package_name"));
        if (!TextUtils.isEmpty(from)) {
            return from;
        }
        from = checkIntentFromSettingSearch(intent);
        if (!TextUtils.isEmpty(from)) {
            return from;
        }
        Set<String> cates = intent.getCategories();
        if (cates == null || !cates.contains("android.intent.category.LAUNCHER")) {
            return "";
        }
        return "l";
    }

    void checkOnNewIntent(Activity activity, Intent intent) {
        if (intent != null) {
            String from = checkIntentAndGuessFrom(intent);
            if (!TextUtils.isEmpty(from)) {
                this.newIntentActivityName = activity.getClass().getName();
                this.newIntentActivityFrom = from;
            }
        }
    }

    private String checkInentIfFromNotification(Intent intent) {
        String action = intent.getStringExtra(HsmStatConst.KEY_NOTFICATION_EVENT);
        if (TextUtils.isEmpty(action)) {
            return "";
        }
        if (intent.getBooleanExtra(HsmStatConst.KEY_SHOULD_STAT, true)) {
            HsmStat.statClickNotificationNormal(action);
        }
        return "n";
    }

    private String checkIntentFromSettingSearch(Intent intent) {
        if (TextUtils.isEmpty(intent.getStringExtra(SettingSearchUtil.KEY_EXTRA_SETTING))) {
            return "";
        }
        return "s";
    }
}
