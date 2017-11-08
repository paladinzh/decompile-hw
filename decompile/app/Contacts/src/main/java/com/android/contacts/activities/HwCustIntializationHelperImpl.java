package com.android.contacts.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import com.android.contacts.activities.ActionBarAdapter.TabState;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.android.contacts.util.LogConfig;
import com.google.android.gms.R;

public class HwCustIntializationHelperImpl extends HwCustIntializationHelper {
    private int mEnableAAB = 0;

    public HwCustIntializationHelperImpl(Context context) {
        int i = 0;
        super(context);
        if (HwCustCommonConstants.IS_AAB_ATT) {
            i = 1;
        }
        this.mEnableAAB = i;
    }

    public void checkAndStartSyncClient(Context context, int tab, boolean startFromSettings) {
        if (isEnableAAB()) {
            if (tab != TabState.ALL) {
                if (LogConfig.HWDBG) {
                    Log.d(HwCustCommonConstants.TAG_AAB, "It isn't trigger from the home screen by press Contact icon.");
                }
                return;
            }
            HwCustCommonUtilMethods.startAABClient(context, startFromSettings);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if (1 == this.mEnableAAB) {
            outState.putBoolean("donot_restart_aab", true);
        }
    }

    public void initializeCust(Context context, ActionBarAdapter actionBarAdapter, boolean startFromSettings, boolean isRecreatedInstance, Bundle savedState) {
        boolean isStartAAB = false;
        if (1 == this.mEnableAAB) {
            if (isRecreatedInstance) {
                isStartAAB = savedState.getBoolean("donot_restart_aab", false);
            }
            if (!isStartAAB && actionBarAdapter != null) {
                checkAndStartSyncClient(context, actionBarAdapter.getCurrentTab(), startFromSettings);
            }
        }
    }

    private boolean isEnableAAB() {
        return HwCustCommonConstants.IS_AAB_ATT;
    }

    public void customizeOptionsMenu(Activity activity, Menu menu, Menu submenu) {
        if (isEnableAAB() && submenu != null) {
            submenu.add(R.string.menu_att_accounts);
        }
        if (activity != null && menu != null && HwCustContactFeatureUtils.isShowVisualMailBox()) {
            addVVMMenu(activity, menu);
        }
    }

    private void addVVMMenu(final Activity activity, Menu menu) {
        MenuItem vvmItem = menu.add(R.id.dialer_options, 0, 1, R.string.description_vvm_button).setIcon(R.drawable.ic_vvm);
        vvmItem.setShowAsAction(2);
        vvmItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                HwCustCommonUtilMethods.startVVM(activity);
                return true;
            }
        });
    }
}
