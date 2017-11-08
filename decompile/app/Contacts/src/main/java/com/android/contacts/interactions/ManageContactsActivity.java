package com.android.contacts.interactions;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.android.contacts.ContactsUtils;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.editor.HAPSelectAccountDialogFragment;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimStateListener;
import com.android.contacts.hap.sim.SimUtility;
import com.android.contacts.hap.util.HAPAccountListAdapter.AccountListFilter;
import com.android.contacts.hap.util.ManageContactsUtil;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AccountSelectionUtil;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.contacts.vcard.ExportVCardActivity;
import com.google.android.gms.R;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;

public class ManageContactsActivity extends Activity {
    private static final String TAG = ManageContactsActivity.class.getSimpleName();
    private static HwCustManageContactsActivity mManageContactsActivity;
    private final IntentFilter homeFilter = new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS");
    private final BroadcastReceiver homePressReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.intent.action.CLOSE_SYSTEM_DIALOGS")) {
                String reason = intent.getStringExtra("reason");
                if (reason != null && reason.equals("homekey")) {
                    ManageContactsActivity.this.finish();
                }
            }
        }
    };

    public static class PrefsFragment extends PreferenceFragment implements SimStateListener {
        private boolean mAreContactsAvailable;
        private String mCallingActivity;
        private ContactListFilter mCurrentFilter;
        private LoaderCallbacks<Cursor> mFetchContactsCountLoader = new LoaderCallbacks<Cursor>() {
            public Loader<Cursor> onCreateLoader(int aId, Bundle aBundle) {
                Uri lUri = Contacts.CONTENT_URI;
                if (PrefsFragment.this.mCurrentFilter.filterType != -3) {
                    Builder builder = lUri.buildUpon();
                    if (PrefsFragment.this.mCurrentFilter.filterType == 0) {
                        PrefsFragment.this.mCurrentFilter.addAccountQueryParameterToUrl(builder);
                    }
                    lUri = builder.build();
                }
                return new CursorLoader(PrefsFragment.this.getActivity(), lUri, new String[]{"_id"}, PrefsFragment.this.getSelectionForLoader(), null, null);
            }

            public void onLoadFinished(Loader<Cursor> loader, Cursor aCursor) {
                boolean z = false;
                if (HwLog.HWDBG) {
                    HwLog.d(ManageContactsActivity.TAG, "onLoadFinished!!!");
                }
                if (aCursor != null) {
                    PrefsFragment prefsFragment = PrefsFragment.this;
                    if (aCursor.getCount() > 0) {
                        z = true;
                    }
                    prefsFragment.mAreContactsAvailable = z;
                    PrefsFragment.this.updatePreferencesBasedOnContactCount();
                }
            }

            public void onLoaderReset(Loader<Cursor> loader) {
            }
        };
        private boolean mIsResumed;
        private boolean mRefreshPreferencesOnSIMStateChange;
        private boolean mShowOnlyContactsWithPhoneNumbers;
        private boolean mShowSIMContactsPreferenceON;

        private String getSelectionForLoader() {
            if (HwLog.HWDBG) {
                HwLog.d(ManageContactsActivity.TAG, "filter type :: " + this.mCurrentFilter.filterType);
            }
            StringBuilder lSelection = new StringBuilder();
            switch (this.mCurrentFilter.filterType) {
                case -3:
                    lSelection.append("in_visible_group=1");
                    if (this.mShowOnlyContactsWithPhoneNumbers) {
                        lSelection.append(" AND ").append("has_phone_number").append("=1");
                        break;
                    }
                    break;
                case -2:
                    if (!this.mShowSIMContactsPreferenceON) {
                        lSelection.append("_id").append(" IN (SELECT DISTINCT ").append("contact_id").append(" FROM view_raw_contacts WHERE ");
                        if (SimFactoryManager.isDualSim()) {
                            lSelection.append("account_type").append(" NOT IN ('").append("com.android.huawei.sim").append("','").append("com.android.huawei.secondsim").append("')");
                        } else {
                            lSelection.append("account_type").append("!='").append("com.android.huawei.sim").append("'");
                        }
                        lSelection.append(")");
                    }
                    if (this.mShowOnlyContactsWithPhoneNumbers) {
                        if (!TextUtils.isEmpty(lSelection)) {
                            lSelection.append(" AND ");
                        }
                        lSelection.append("has_phone_number").append("=1");
                        break;
                    }
                    break;
                case 0:
                    if (this.mShowOnlyContactsWithPhoneNumbers) {
                        lSelection.append("has_phone_number").append("=1");
                        break;
                    }
                    break;
            }
            String lSelectionClause = lSelection.toString();
            if (HwLog.HWDBG) {
                HwLog.d(ManageContactsActivity.TAG, "getSelectionForLoader lSelection :: " + lSelectionClause);
            }
            return lSelectionClause;
        }

        protected void updatePreferencesBasedOnContactCount() {
            if (HwLog.HWDBG) {
                HwLog.d(ManageContactsActivity.TAG, "updatePreferencesBasedOnContactCount mAreContactsAvailable :: " + this.mAreContactsAvailable);
            }
            enablePreference(findPreferenceByKeyResourceId(R.string.key_prefs_share), this.mAreContactsAvailable);
        }

        public void onAttach(Activity aActivity) {
            super.onAttach(aActivity);
            if (HwLog.HWDBG) {
                HwLog.d(ManageContactsActivity.TAG, "onAttach!!!");
            }
            Intent lIntent = aActivity.getIntent();
            this.mAreContactsAvailable = lIntent.getBooleanExtra("CONTACTS_ARE_AVAILABLE", false);
            this.mCallingActivity = lIntent.getStringExtra("CALLING_ACTIVITY");
            SharedPreferences lSharedPrefs = SharePreferenceUtil.getDefaultSp_de(aActivity);
            this.mCurrentFilter = ContactListFilter.restoreDefaultPreferences(lSharedPrefs);
            if (this.mCurrentFilter == null) {
                this.mCurrentFilter = ContactListFilter.createFilterWithType(-2);
            }
            this.mShowSIMContactsPreferenceON = lSharedPrefs.getBoolean("preference_show_sim_contacts", true);
            this.mShowOnlyContactsWithPhoneNumbers = SharePreferenceUtil.getDefaultSp_de(aActivity).getBoolean("preference_contacts_only_phonenumber", false);
        }

        public Preference findPreferenceByKeyResourceId(int aResId) {
            return super.findPreference(getString(aResId));
        }

        public void onCreate(Bundle aSavedInstanceState) {
            super.onCreate(aSavedInstanceState);
            if (HwLog.HWDBG) {
                HwLog.d(ManageContactsActivity.TAG, "onCreate!!");
            }
            addPreferencesFromResource(R.xml.preference_manage_contacts);
            showOrHidePreferences();
            Fragment dialogFragment = getFragmentManager().findFragmentByTag("Manage_Contacts");
            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 1091);
            }
            SimFactoryManager.addSimStateListener(this);
            getLoaderManager().initLoader(1, null, this.mFetchContactsCountLoader);
        }

        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            ((ListView) getView().findViewById(16908298)).setDivider(null);
        }

        public void onStart() {
            super.onStart();
            getListView().setFooterDividersEnabled(false);
        }

        private void showOrHidePreferences() {
            Resources lRes = getActivity().getResources();
            PreferenceGroup lPrefGroup = (PreferenceGroup) findPreferenceByKeyResourceId(R.string.key_prefs_cat_import);
            lPrefGroup.setTitle(CommonUtilMethods.upPercase(getString(R.string.title_prefs_cat_import)));
            if (!lRes.getBoolean(R.bool.config_allow_import_from_sdcard)) {
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_import_from_storage));
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_import_from_storage_divider));
            }
            if (!CommonUtilMethods.isActivityAvailable(getActivity(), CommonUtilMethods.getImportContactsViaBtIntent())) {
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_import_via_Bt));
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_import_via_Bt_divider));
            }
            if (!CommonUtilMethods.isActivityAvailable(getActivity(), CommonUtilMethods.getImportContactsFromQQIntent())) {
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_import_from_qq));
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_import_via_Bt_divider));
            }
            lPrefGroup = (PreferenceGroup) findPreferenceByKeyResourceId(R.string.key_prefs_cat_export);
            lPrefGroup.setTitle(CommonUtilMethods.upPercase(getString(R.string.title_prefs_cat_export)));
            if (!lRes.getBoolean(R.bool.config_allow_export_to_sdcard)) {
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_export_to_storage));
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_export_to_storage_divider));
            }
            if (ManageContactsActivity.mManageContactsActivity != null && ManageContactsActivity.mManageContactsActivity.isRemoveShareMenu()) {
                ManageContactsActivity.mManageContactsActivity.removeShareMenuPreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_share));
            }
            updatePreferencesBasedOnSIMState();
        }

        private void enablePreference(Preference aPref, boolean aEnable) {
            if (HwLog.HWDBG) {
                HwLog.d(ManageContactsActivity.TAG, "enablePreference :: aEnable " + aEnable);
            }
            if (aPref != null) {
                aPref.setEnabled(aEnable);
            }
        }

        private void removePreference(PreferenceGroup aPrefGroup, Preference aPref) {
            if (aPrefGroup != null && aPref != null) {
                aPrefGroup.removePreference(aPref);
            }
        }

        public boolean onPreferenceTreeClick(PreferenceScreen aPreferenceScreen, Preference aPreference) {
            String lKey = aPreference.getKey();
            if (HwLog.HWDBG) {
                HwLog.d(ManageContactsActivity.TAG, "onPreferenceClick " + lKey);
            }
            if (lKey == null) {
                return super.onPreferenceTreeClick(aPreferenceScreen, aPreference);
            }
            if (lKey.equals(getString(R.string.key_prefs_share))) {
                if (HwLog.HWFLOW) {
                    HwLog.i(ManageContactsActivity.TAG, "Click share Contacts.");
                }
                ExceptionCapture.reportScene(66);
                StatisticalHelper.report(4016);
                startActivity(CommonUtilMethods.getShareContactsIntent());
            }
            if (lKey.equals(getString(R.string.key_prefs_import_from_storage))) {
                if (HwLog.HWFLOW) {
                    HwLog.i(ManageContactsActivity.TAG, "Click import Contacts from storage.");
                }
                ManageContactsUtil.handleImportFromSDCardRequest(getActivity(), this);
                ExceptionCapture.reportScene(63);
            } else if (lKey.equals(getString(R.string.key_prefs_import_from_sim)) || lKey.equals(getString(R.string.key_prefs_import_from_sim1)) || lKey.equals(getString(R.string.key_prefs_import_from_sim2))) {
                if (HwLog.HWFLOW) {
                    HwLog.i(ManageContactsActivity.TAG, "Click import Contacts from SIM.");
                }
                handleCopyRequest(lKey);
            } else if (lKey.equals(getString(R.string.key_prefs_import_via_Bt))) {
                ExceptionCapture.reportScene(64);
                startActivity(new Intent("com.android.huawei.bluetooth.prepare"));
            } else if (lKey.equals(getString(R.string.key_prefs_import_from_qq))) {
                startActivity(CommonUtilMethods.getImportContactsFromQQIntent());
            } else if (lKey.equals(getString(R.string.key_prefs_export_to_storage))) {
                if (HwLog.HWFLOW) {
                    HwLog.i(ManageContactsActivity.TAG, "Click export Contacts to storage.");
                }
                ExceptionCapture.reportScene(65);
                Intent exportIntent = new Intent(getActivity(), ExportVCardActivity.class);
                exportIntent.putExtra("CALLING_ACTIVITY", this.mCallingActivity);
                getActivity().startActivity(exportIntent);
            } else if (!lKey.equals(getString(R.string.key_prefs_export_to_sim)) && !lKey.equals(getString(R.string.key_prefs_export_to_sim1)) && !lKey.equals(getString(R.string.key_prefs_export_to_sim2))) {
                return super.onPreferenceTreeClick(aPreferenceScreen, aPreference);
            } else {
                if (HwLog.HWFLOW) {
                    HwLog.i(ManageContactsActivity.TAG, "Click import Contacts to SIM.");
                }
                handleExportToSIM(lKey);
            }
            return true;
        }

        private boolean handleExportToSIM(String aKey) {
            boolean lExportToSIM = false;
            boolean lExportToSIM1 = false;
            boolean lExportToSIM2 = false;
            boolean lExportToSIMOperation = false;
            Bundle bundle = new Bundle();
            if (aKey.equals(getString(R.string.key_prefs_export_to_sim))) {
                lExportToSIMOperation = true;
                lExportToSIM = true;
                bundle.putBoolean("EXCLUDE_SIM", true);
                bundle.putInt("which_sim", -1);
            } else if (aKey.equals(getString(R.string.key_prefs_export_to_sim1))) {
                lExportToSIMOperation = true;
                lExportToSIM1 = true;
                bundle.putBoolean("EXCLUDE_SIM1", true);
                bundle.putInt("which_sim", 0);
            } else if (aKey.equals(getString(R.string.key_prefs_export_to_sim2))) {
                lExportToSIMOperation = true;
                lExportToSIM2 = true;
                bundle.putBoolean("EXCLUDE_SIM2", true);
                bundle.putInt("which_sim", 1);
            }
            bundle.putBoolean("export_to_sim", lExportToSIMOperation);
            AccountTypeManager lAcctMgrInstance = AccountTypeManager.getInstance(getActivity());
            ArrayList arrayList = null;
            if (lExportToSIM) {
                arrayList = (ArrayList) lAcctMgrInstance.getAccountsExcludeSim(false);
            } else if (lExportToSIM1) {
                if (SimUtility.isSimReady(1)) {
                    arrayList = (ArrayList) lAcctMgrInstance.getAccountsExcludeSim1(false);
                } else {
                    arrayList = (ArrayList) lAcctMgrInstance.getAccountsExcludeSim(false);
                }
            } else if (lExportToSIM2) {
                if (SimUtility.isSimReady(0)) {
                    arrayList = (ArrayList) lAcctMgrInstance.getAccountsExcludeSim2(false);
                } else {
                    arrayList = (ArrayList) lAcctMgrInstance.getAccountsExcludeSim(false);
                }
            }
            if (arrayList == null || arrayList.size() != 1) {
                HAPSelectAccountDialogFragment.show(getFragmentManager(), this, R.string.dialog_title_export_from, AccountListFilter.ACCOUNTS_COPY_ALLOWED, bundle, null, "Manage_Contacts");
                return false;
            }
            ((ManageContactsActivity) getActivity()).onCopyAccountChosen((AccountWithDataSet) arrayList.get(0), bundle);
            return false;
        }

        private boolean handleCopyRequest(String aResId) {
            if (getString(R.string.key_prefs_import_from_sim).equals(aResId)) {
                startCopyFromSIMActivity(-1);
                return true;
            }
            String lMessage;
            if (getString(R.string.key_prefs_import_from_sim1).equals(aResId)) {
                lMessage = String.format(getString(R.string.delete_sim_progress), new Object[]{SimFactoryManager.getSimCardDisplayLabel(0)});
                if (SimFactoryManager.getSharedPreferences("SimInfoFile", 0).getBoolean("sim_delete_progress", false)) {
                    showAlertDialog(R.string.contact_str_copysim_notification, lMessage);
                    return false;
                }
                startCopyFromSIMActivity(0);
            } else if (getString(R.string.key_prefs_import_from_sim2).equals(aResId)) {
                lMessage = String.format(getString(R.string.delete_sim_progress), new Object[]{SimFactoryManager.getSimCardDisplayLabel(1)});
                if (SimFactoryManager.getSharedPreferences("SimInfoFile", 1).getBoolean("sim_delete_progress", false)) {
                    showAlertDialog(R.string.contact_str_copysim_notification, lMessage);
                    return false;
                }
                startCopyFromSIMActivity(1);
            } else {
                Bundle lBundle = new Bundle();
                lBundle.putInt("resId", R.string.copy_to_account);
                HAPSelectAccountDialogFragment.show(getFragmentManager(), this, R.string.dialog_copy_contact_account, AccountListFilter.ACCOUNTS_COPY_ALLOWED, lBundle, null, "Manage_Contacts");
                return false;
            }
            return true;
        }

        private void showAlertDialog(int aTitleResId, String aMessage) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle(aTitleResId).setPositiveButton(R.string.contact_known_button_text, new OnClickListener() {
                public void onClick(DialogInterface aDialogInterface, int which) {
                    aDialogInterface.dismiss();
                }
            });
            if (!isAdded() || getActivity() == null) {
                builder.setMessage(aMessage);
            } else {
                View view = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(aMessage);
                builder.setView(view);
            }
            builder.create().show();
        }

        private void startCopyFromSIMActivity(int aSubscription) {
            getActivity().startActivity(CommonUtilMethods.getIntentForCopyFromSimActivity(aSubscription));
        }

        public void simStateChanged(int aSubScription) {
            if (HwLog.HWDBG) {
                HwLog.d(ManageContactsActivity.TAG, "simStateChanged aSubScription :: " + aSubScription);
            }
            if (this.mIsResumed) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        PrefsFragment.this.updatePreferencesBasedOnSIMState();
                    }
                });
            } else {
                this.mRefreshPreferencesOnSIMStateChange = true;
            }
        }

        private void updatePreferencesBasedOnSIMState() {
            if (HwLog.HWDBG) {
                HwLog.d(ManageContactsActivity.TAG, "updatePreferencesBasedOnSIMState!!!");
            }
            this.mRefreshPreferencesOnSIMStateChange = false;
            boolean lIsSimReady = false;
            boolean lIsSim1Ready = false;
            boolean lIsSim2Ready = false;
            PreferenceGroup lPrefGroup = (PreferenceGroup) findPreferenceByKeyResourceId(R.string.key_prefs_cat_import);
            String lFirstSimLabel = SimFactoryManager.getSimCardDisplayLabel(0);
            Preference lPref;
            if (SimFactoryManager.isDualSim()) {
                boolean isSimActive;
                boolean isSimActive2;
                String lSecondSimLabel = SimFactoryManager.getSimCardDisplayLabel(1);
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_import_from_sim));
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_import_from_sim_divider));
                lPref = findPreferenceByKeyResourceId(R.string.key_prefs_import_from_sim1);
                if (SimFactoryManager.hasIccCard(0)) {
                    isSimActive = SimFactoryManager.isSimActive(0);
                } else {
                    isSimActive = false;
                }
                if (HwLog.HWDBG) {
                    HwLog.d(ManageContactsActivity.TAG, "lIsSIM1Present :: " + isSimActive);
                }
                if (MultiUsersUtils.isCurrentUserOwner() && isSimActive) {
                    if (lPrefGroup == null) {
                        lPrefGroup = addImportPreferenceCategory();
                    }
                    if (lPref == null) {
                        lPref = addPreference(ContactsUtils.getImportString(getActivity(), lFirstSimLabel), R.integer.prefs_order_import_from_sim1, R.string.key_prefs_import_from_sim1, lPrefGroup);
                        addPreference(null, R.integer.prefs_order_import_from_sim1_divider, R.string.key_prefs_import_from_sim1_divider, lPrefGroup);
                    } else {
                        lPref.setTitle(ContactsUtils.getImportString(getActivity(), lFirstSimLabel));
                    }
                    lIsSim1Ready = SimUtility.isSimStateLoaded(0, getActivity());
                    if (lIsSim1Ready) {
                        enablePreference(lPref, true);
                    } else {
                        enablePreference(lPref, false);
                    }
                } else {
                    removePreference(lPrefGroup, lPref);
                    removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_import_from_sim1_divider));
                }
                lPref = findPreferenceByKeyResourceId(R.string.key_prefs_import_from_sim2);
                if (SimFactoryManager.hasIccCard(1)) {
                    isSimActive2 = SimFactoryManager.isSimActive(1);
                } else {
                    isSimActive2 = false;
                }
                if (HwLog.HWDBG) {
                    HwLog.d(ManageContactsActivity.TAG, "lISSIM2Present :: " + isSimActive2);
                }
                if (MultiUsersUtils.isCurrentUserOwner() && isSimActive2) {
                    if (lPrefGroup == null) {
                        lPrefGroup = addImportPreferenceCategory();
                    }
                    if (lPref == null) {
                        lPref = addPreference(ContactsUtils.getImportString(getActivity(), lSecondSimLabel), R.integer.prefs_order_import_from_sim2, R.string.key_prefs_import_from_sim2, lPrefGroup);
                        addPreference(null, R.integer.prefs_order_import_from_sim2_divider, R.string.key_prefs_import_from_sim2_divider, lPrefGroup);
                    } else {
                        lPref.setTitle(ContactsUtils.getImportString(getActivity(), lSecondSimLabel));
                    }
                    lIsSim2Ready = SimUtility.isSimStateLoaded(1, getActivity());
                    if (lIsSim2Ready) {
                        enablePreference(lPref, true);
                    } else {
                        enablePreference(lPref, false);
                    }
                } else {
                    removePreference(lPrefGroup, lPref);
                    removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_import_from_sim2_divider));
                }
                lPrefGroup = (PreferenceGroup) findPreferenceByKeyResourceId(R.string.key_prefs_cat_export);
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_export_to_sim));
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_export_to_sim_divider));
                lPref = findPreferenceByKeyResourceId(R.string.key_prefs_export_to_sim1);
                if (MultiUsersUtils.isCurrentUserOwner() && isSimActive) {
                    if (lPrefGroup == null) {
                        lPrefGroup = addExportPreferenceCategory();
                    }
                    if (lPref == null) {
                        lPref = addPreference(ContactsUtils.getExportString(getActivity(), lFirstSimLabel), R.integer.prefs_order_export_to_sim1, R.string.key_prefs_export_to_sim1, lPrefGroup);
                        addPreference(null, R.integer.prefs_order_export_to_sim1_divider, R.string.key_prefs_export_to_sim1_divider, lPrefGroup);
                    } else {
                        lPref.setTitle(ContactsUtils.getExportString(getActivity(), lFirstSimLabel));
                    }
                    enablePreference(lPref, lIsSim1Ready);
                } else {
                    removePreference(lPrefGroup, lPref);
                    removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_export_to_sim1_divider));
                }
                lPref = findPreferenceByKeyResourceId(R.string.key_prefs_export_to_sim2);
                if (MultiUsersUtils.isCurrentUserOwner() && isSimActive2) {
                    if (lPrefGroup == null) {
                        lPrefGroup = addExportPreferenceCategory();
                    }
                    if (lPref == null) {
                        lPref = addPreference(ContactsUtils.getExportString(getActivity(), lSecondSimLabel), R.integer.prefs_order_export_to_sim2, R.string.key_prefs_export_to_sim2, lPrefGroup);
                        addPreference(null, R.integer.prefs_order_export_to_sim2_divider, R.string.key_prefs_export_to_sim2_divider, lPrefGroup);
                    } else {
                        lPref.setTitle(ContactsUtils.getExportString(getActivity(), lSecondSimLabel));
                    }
                    enablePreference(lPref, lIsSim2Ready);
                } else {
                    removePreference(lPrefGroup, lPref);
                    removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_export_to_sim2_divider));
                }
            } else {
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_import_from_sim1));
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_import_from_sim1_divider));
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_import_from_sim2));
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_import_from_sim2_divider));
                boolean lIsSIMPresent = SimFactoryManager.hasIccCard(-1);
                if (HwLog.HWDBG) {
                    HwLog.d(ManageContactsActivity.TAG, "lIsSIMPresent :: " + lIsSIMPresent);
                }
                lPref = findPreferenceByKeyResourceId(R.string.key_prefs_import_from_sim);
                if (MultiUsersUtils.isCurrentUserOwner() && lIsSIMPresent) {
                    if (lPrefGroup == null) {
                        lPrefGroup = addImportPreferenceCategory();
                    }
                    if (lPref == null) {
                        lPref = addPreference(ContactsUtils.getImportString(getActivity(), lFirstSimLabel), R.integer.prefs_order_import_from_sim, R.string.key_prefs_import_from_sim, lPrefGroup);
                        addPreference(null, R.integer.prefs_order_import_from_sim_divider, R.string.key_prefs_import_from_sim_divider, lPrefGroup);
                    } else {
                        lPref.setTitle(ContactsUtils.getImportString(getActivity(), lFirstSimLabel));
                    }
                    lIsSimReady = SimUtility.isSimStateLoaded(-1, getActivity());
                    if (lIsSimReady) {
                        enablePreference(lPref, true);
                    } else {
                        enablePreference(lPref, false);
                    }
                } else {
                    removePreference(lPrefGroup, lPref);
                    removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_import_from_sim_divider));
                }
                lPrefGroup = (PreferenceGroup) findPreferenceByKeyResourceId(R.string.key_prefs_cat_export);
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_export_to_sim1));
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_export_to_sim1_divider));
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_export_to_sim2));
                removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_export_to_sim2_divider));
                lPref = findPreferenceByKeyResourceId(R.string.key_prefs_export_to_sim);
                if (MultiUsersUtils.isCurrentUserOwner() && lIsSIMPresent) {
                    if (lPrefGroup == null) {
                        lPrefGroup = addExportPreferenceCategory();
                    }
                    if (lPref == null) {
                        lPref = addPreference(ContactsUtils.getExportString(getActivity(), lFirstSimLabel), R.integer.prefs_order_export_to_sim, R.string.key_prefs_export_to_sim, lPrefGroup);
                        addPreference(null, R.integer.prefs_order_export_to_sim_divider, R.string.key_prefs_export_to_sim_divider, lPrefGroup);
                    } else {
                        lPref.setTitle(ContactsUtils.getExportString(getActivity(), lFirstSimLabel));
                    }
                    enablePreference(lPref, lIsSimReady);
                } else {
                    removePreference(lPrefGroup, lPref);
                    removePreference(lPrefGroup, findPreferenceByKeyResourceId(R.string.key_prefs_export_to_sim_divider));
                }
            }
            PreferenceScreen lPrefsScreen = getPreferenceScreen();
            if (lPrefGroup != null && lPrefGroup.getPreferenceCount() == 0) {
                lPrefsScreen.removePreference(lPrefGroup);
            }
            lPrefGroup = (PreferenceGroup) findPreferenceByKeyResourceId(R.string.key_prefs_cat_import);
            if (lPrefGroup != null && lPrefGroup.getPreferenceCount() == 0) {
                lPrefsScreen.removePreference(lPrefGroup);
            }
        }

        private PreferenceGroup addImportPreferenceCategory() {
            if (HwLog.HWDBG) {
                HwLog.d(ManageContactsActivity.TAG, "addImportPreferenceCategory!!");
            }
            Context lContext = getActivity();
            PreferenceGroup lGroup = new PreferenceCategory(lContext);
            lGroup.setOrder(lContext.getResources().getInteger(R.integer.prefs_cat_order_import));
            lGroup.setKey(getString(R.string.key_prefs_cat_import));
            lGroup.setTitle(CommonUtilMethods.upPercase(getString(R.string.title_prefs_cat_import)));
            getPreferenceScreen().addPreference(lGroup);
            return lGroup;
        }

        private PreferenceGroup addExportPreferenceCategory() {
            if (HwLog.HWDBG) {
                HwLog.d(ManageContactsActivity.TAG, "addExportPreferenceCategory!!");
            }
            Context lContext = getActivity();
            PreferenceGroup lGroup = new PreferenceCategory(lContext);
            lGroup.setOrder(lContext.getResources().getInteger(R.integer.prefs_cat_order_export));
            lGroup.setKey(getString(R.string.key_prefs_cat_export));
            lGroup.setTitle(CommonUtilMethods.upPercase(getString(R.string.title_prefs_cat_export)));
            getPreferenceScreen().addPreference(lGroup);
            return lGroup;
        }

        private Preference addPreference(String aTitle, int aPrefOrder, int aPrefKeyResId, PreferenceGroup aPrefGroup) {
            if (HwLog.HWDBG) {
                HwLog.d(ManageContactsActivity.TAG, "addPreference!!");
            }
            Context lContext = getActivity();
            Preference lPref = new Preference(lContext);
            lPref.setOrder(lContext.getResources().getInteger(aPrefOrder));
            lPref.setKey(getString(aPrefKeyResId));
            if (aTitle == null) {
                lPref.setLayoutResource(R.layout.listdivider);
            } else {
                lPref.setTitle(aTitle);
                lPref.setWidgetLayoutResource(R.layout.preference_widget_arrow);
            }
            aPrefGroup.addPreference(lPref);
            return lPref;
        }

        public void onPause() {
            super.onPause();
            this.mIsResumed = false;
        }

        public void onResume() {
            super.onResume();
            this.mIsResumed = true;
            if (this.mRefreshPreferencesOnSIMStateChange) {
                updatePreferencesBasedOnSIMState();
            }
        }

        public void onDestroy() {
            super.onDestroy();
            SimFactoryManager.removeSimStateListener(this);
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (1091 != requestCode) {
                super.onActivityResult(requestCode, resultCode, data);
            } else if (-1 == resultCode) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    AccountWithDataSet account = (AccountWithDataSet) bundle.get("account");
                    Bundle extrasBundle = (Bundle) bundle.getParcelable("extra_args");
                    if (account != null) {
                        ((ManageContactsActivity) getActivity()).onCopyAccountChosen(account, extrasBundle);
                    }
                }
            } else {
                ((ManageContactsActivity) getActivity()).onAccountSelectorCancelled();
            }
        }
    }

    static {
        mManageContactsActivity = null;
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            mManageContactsActivity = (HwCustManageContactsActivity) HwCustUtils.createObj(HwCustManageContactsActivity.class, new Object[0]);
        }
    }

    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        if (CommonUtilMethods.isLargeThemeApplied(getResources())) {
            getWindow().setFlags(16777216, 16777216);
        }
        setTheme(R.style.ContactsPreferencesTheme);
        if (arg0 == null) {
            getFragmentManager().beginTransaction().replace(16908290, new PrefsFragment()).commit();
        }
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(4, 4);
        }
        registerReceiver(this.homePressReceiver, this.homeFilter, "android.permission.INJECT_EVENTS", null);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                return true;
            default:
                return false;
        }
    }

    protected void onDestroy() {
        try {
            if (this.homePressReceiver != null) {
                unregisterReceiver(this.homePressReceiver);
            }
        } catch (IllegalArgumentException e) {
            HwLog.e(TAG, "HomePressReceiver not regist!");
        }
        super.onDestroy();
    }

    public void onCopyAccountChosen(AccountWithDataSet aAccount, Bundle aExtraArgs) {
        int lResId = -1;
        boolean lExportToSIM = false;
        if (aExtraArgs != null) {
            lResId = aExtraArgs.getInt("resId");
            lExportToSIM = aExtraArgs.getBoolean("export_to_sim", false);
        }
        if (lResId == R.string.import_from_sdcard) {
            if (HwLog.HWDBG) {
                HwLog.d(TAG, "Import contacts from sd card option selected");
            }
            AccountSelectionUtil.doImport(this, lResId, aAccount);
            return;
        }
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "onCopyAccountChosen!!!");
        }
        Intent intent = new Intent();
        intent.setPackage("com.android.contacts");
        intent.setAction("android.intent.action.HAP_COPY_TO_ACCOUNT");
        intent.putExtra("export_to_sim", lExportToSIM);
        if (lExportToSIM) {
            intent.putExtras(aExtraArgs);
        }
        intent.putExtra("extra_account_name", aAccount.name);
        intent.putExtra("extra_account_type", aAccount.type);
        intent.putExtra("extra_account_data_set", aAccount.dataSet);
        startActivity(intent);
    }

    public void onAccountSelectorCancelled() {
    }
}
