package com.android.contacts;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.hap.att.HwCustAttFirstLaunchTask;
import com.android.contacts.hap.sprint.calllog.HwCustDialpadCallIntercept;
import com.android.contacts.hap.sprint.dialpad.HwCustContactsAbbreviatedCodesUtils;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.hap.utils.EasContactsCache;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.android.contacts.util.HwCustPhoneServiceStateListener;
import com.huawei.android.provider.SettingsEx.Systemex;
import com.huawei.sprint.chameleon.provider.ChameleonContract;

public class HwCustContactApplicationHelperImpl extends HwCustContactApplicationHelper {
    private static final String TAG = "HwCustContactApplicationHelperImpl";
    private ContentObserver mChameleonDBObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Log.d(HwCustContactApplicationHelperImpl.TAG, "chameleon db's contacts nodes are changed");
            if (HwCustContactApplicationHelperImpl.this.mContext == null) {
                Log.d(HwCustContactApplicationHelperImpl.TAG, "Context didn't init.");
            } else {
                HwCustContactApplicationHelperImpl.this.reLoadSprintDataEntries(HwCustContactApplicationHelperImpl.this.mContext);
            }
        }
    };
    private ContentObserver mContactsObserver = null;
    private Context mContext = null;

    public void notifyCustOnChange(Context aContext) {
        if (HwCustCommonConstants.EAS_ACCOUNT_ICON_DISP_EMABLED) {
            EasContactsCache.getInstance(aContext).setDataChange();
        }
    }

    public boolean preloadContactFeatureEnabled() {
        return !HwCustContactFeatureUtils.isSupportPreloadContact();
    }

    public void handleCustomizationsOnCreate(Context aContext) {
        if (aContext != null) {
            handleFirstLaunch(aContext);
            if (HwCustCommonConstants.EAS_ACCOUNT_ICON_DISP_EMABLED) {
                this.mContext = aContext.getApplicationContext();
                if (this.mContactsObserver == null && this.mContext != null) {
                    this.mContactsObserver = new ContentObserver(new Handler()) {
                        public void onChange(boolean selfChange) {
                            Log.i(HwCustContactApplicationHelperImpl.TAG, "onChange called for EAS");
                            EasContactsCache.getInstance(HwCustContactApplicationHelperImpl.this.mContext).setDataChange();
                        }
                    };
                    this.mContext.getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true, this.mContactsObserver);
                }
                EasContactsCache.getInstance(aContext).refresh();
            }
            if (HwCustContactFeatureUtils.isVOWifiFeatureEnabled()) {
                HwCustPhoneServiceStateListener.startListeningServiceState();
            }
            if ("true".equals(Systemex.getString(aContext.getContentResolver(), "hw_search_contacts_online"))) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(aContext);
                if (sp.getBoolean("contacts_restore_factory_flag", true)) {
                    Editor editor = sp.edit();
                    editor.putBoolean("preference_show_online_contacts", true);
                    editor.putBoolean("contacts_restore_factory_flag", false);
                    editor.apply();
                }
            }
            if (HwCustContactFeatureUtils.isChameleonDBChangeObserver() && this.mChameleonDBObserver != null) {
                this.mContext = aContext;
                aContext.getContentResolver().registerContentObserver(ChameleonContract.CONTENT_URI_CONTACTS, true, this.mChameleonDBObserver);
            }
            fillPredefinedSprintData(aContext);
        }
    }

    public void handleCustomizationsOnTerminate(Context aContext) {
        if (HwCustCommonConstants.EAS_ACCOUNT_ICON_DISP_EMABLED) {
            if (!(this.mContactsObserver == null || this.mContext == null)) {
                this.mContext.getContentResolver().unregisterContentObserver(this.mContactsObserver);
            }
            EasContactsCache.getInstance(aContext).stop();
        }
        if (HwCustContactFeatureUtils.isVOWifiFeatureEnabled()) {
            HwCustPhoneServiceStateListener.stopListeningServiceState();
        }
        if (HwCustContactFeatureUtils.isChameleonDBChangeObserver() && this.mChameleonDBObserver != null) {
            aContext.getContentResolver().unregisterContentObserver(this.mChameleonDBObserver);
            this.mContext = null;
        }
    }

    private void reLoadSprintDataEntries(Context context) {
        fillPredefinedSprintData(context);
    }

    private void fillPredefinedSprintData(Context context) {
        if (context != null) {
            if (HwCustContactFeatureUtils.isSupportADCnodeFeature()) {
                new HwCustContactsAbbreviatedCodesUtils(context).prepareADCDataAsync();
            }
            if (HwCustContactFeatureUtils.isSupportCallInterceptFeature()) {
                HwCustDialpadCallIntercept.getInstance(context).reloadIfRequired();
            }
            if (HwCustContactFeatureUtils.isSupportPreloadContact()) {
                new HwCustPreloadContacts().loadPreloadContactsIfNeeded(context);
            }
        }
    }

    public boolean installYellowPages() {
        return HwCustContactFeatureUtils.isSupportADCnodeFeature();
    }

    private void handleFirstLaunch(Context aContext) {
        if (HwCustContactFeatureUtils.isShowMyInfoForMyProfile() || HwCustContactFeatureUtils.isShowAccServiceGrp()) {
            new HwCustAttFirstLaunchTask().handleFirstLaunch(aContext);
        }
    }
}
