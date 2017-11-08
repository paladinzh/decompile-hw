package com.android.contacts.hap.att;

import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import com.android.contacts.ext.HwCustProfileSimNumberUpdaterUtil;
import com.android.contacts.ext.phone.SetupPhoneAccount;
import com.android.contacts.util.HwCustContactFeatureUtils;

public class HwCustAttFirstLaunchTask {
    private static final boolean IS_PRELOAD_CONTACTS_ENABLED = SystemProperties.getBoolean("ro.config.hw_opt_pre_contact", false);
    private static final String TAG = HwCustAttFirstLaunchTask.class.getSimpleName();
    private static volatile boolean isRunning = false;
    private FirstLaunchTask mFirstLaunchTask = new FirstLaunchTask();

    private static class FirstLaunchTask extends AsyncTask<Void, Void, Void> {
        private Context mAppContext;

        private FirstLaunchTask() {
        }

        protected Void doInBackground(Void... params) {
            HwCustAttFirstLaunchTask.isRunning = true;
            if (HwCustContactFeatureUtils.isShowAccServiceGrp()) {
                addPredefinedContactsFromCust();
            }
            if (HwCustContactFeatureUtils.isShowMyInfoForMyProfile()) {
                HwCustProfileSimNumberUpdaterUtil.checkAndUpdateProfileNumber(this.mAppContext);
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            HwCustAttFirstLaunchTask.isRunning = false;
            super.onPostExecute(result);
        }

        public void setContext(Context context) {
            this.mAppContext = context;
        }

        private void addPredefinedContactsFromCust() {
            if (this.mAppContext == null) {
                Log.e(HwCustAttFirstLaunchTask.TAG, "Context is null. Predefined Contacts not added.");
                return;
            }
            ContentResolver cr = this.mAppContext.getContentResolver();
            if (!HwCustAttFirstLaunchTask.IS_PRELOAD_CONTACTS_ENABLED && -1 == System.getInt(cr, "hw_service_contact_loaded", -1)) {
                SetupPhoneAccount.addTypeAndParseLoadPreDefinedContacts(this.mAppContext);
                System.putInt(cr, "hw_service_contact_loaded", 1);
            }
        }
    }

    public void handleFirstLaunch(Context aContext) {
        if (!isRunning && aContext != null) {
            this.mFirstLaunchTask.setContext(aContext.getApplicationContext());
            this.mFirstLaunchTask.execute(new Void[0]);
        }
    }
}
