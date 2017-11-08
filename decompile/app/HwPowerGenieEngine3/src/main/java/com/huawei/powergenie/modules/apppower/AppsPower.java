package com.huawei.powergenie.modules.apppower;

import android.os.SystemProperties;
import android.util.Log;
import com.huawei.powergenie.api.BaseModule;
import com.huawei.powergenie.core.PowerAction;
import com.huawei.powergenie.modules.apppower.hibernation.ASHManager;
import com.huawei.powergenie.modules.apppower.restrict.AppsRestrict;
import java.io.PrintWriter;

public final class AppsPower extends BaseModule {
    private ASHManager mAppsHibernation;
    private AppsRestrict mAppsRestrict;

    public void onCreate() {
        super.onCreate();
        this.mAppsRestrict = new AppsRestrict(getCoreContext());
        if (SystemProperties.getBoolean("ro.sys.pg_ash", true)) {
            this.mAppsHibernation = new ASHManager(getCoreContext(), getModId());
        } else {
            Log.i("AppsPower", "apps hibernation is not supported!");
        }
    }

    public void onStart() {
        super.onStart();
        addAction(303);
        addAction(300);
        addAction(301);
        addAction(226);
        addAction(227);
        addAction(305);
        addAction(307);
        addAction(208);
        addAction(230);
        addAction(245);
        addAction(224);
        addAction(308);
        addAction(255);
        addAction(312);
        addAction(324);
        addAction(314);
        addAction(302);
        addAction(328);
        addAction(329);
        addAction(350);
        addAction(356);
        addAction(263);
        addAction(264);
        addAction(310);
        addAction(311);
        addAction(304);
        addAction(357);
        addAction(275);
        addAction(270);
        addAction(256);
        addAction(273);
        addAction(318);
        addAction(2);
        addAction(4);
        addAction(359);
        addAction(322);
        addAction(276);
        addAction(277);
        addAction(278);
        addAction(279);
        addAction(280);
        addAction(281);
        addAction(284);
        addAction(282);
        addAction(283);
        addAction(337);
        addAction(358);
        addAction(252);
        addAction(508);
        if (this.mAppsRestrict != null) {
            this.mAppsRestrict.handleStart();
        }
        if (this.mAppsHibernation != null) {
            this.mAppsHibernation.handleStart();
        }
    }

    public boolean handleAction(PowerAction action) {
        if (!super.handleAction(action)) {
            return true;
        }
        if (this.mAppsRestrict != null) {
            this.mAppsRestrict.handleAction(action);
        }
        if (this.mAppsHibernation != null) {
            this.mAppsHibernation.handleAction(action);
        }
        return true;
    }

    public boolean dump(PrintWriter pw, String[] args) {
        pw.println("\nPGMODULE AppsPower ");
        if (this.mAppsHibernation != null) {
            this.mAppsHibernation.dump(pw);
        }
        return true;
    }
}
