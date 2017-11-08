package com.huawei.systemmanager.comm.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import java.lang.ref.WeakReference;

public interface IPreferenceClickListener extends OnPreferenceClickListener {

    public static abstract class SimplePreferenceBase implements IPreferenceClickListener {
        WeakReference<Activity> activityRef;
        protected int mIndex = -1;

        public abstract Intent getIntent(Context context);

        public SimplePreferenceBase(Activity ac) {
            this.activityRef = new WeakReference(ac);
        }

        public boolean onPreferenceClick(Preference preference) {
            Activity ac = (Activity) this.activityRef.get();
            if (ac == null) {
                return false;
            }
            try {
                ac.startActivity(getIntent(ac));
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(this.mIndex));
                HsmStat.statE((int) Events.E_APPMANAGER_CLICK_ITEM, statParam);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    Intent getIntent(Context context);
}
