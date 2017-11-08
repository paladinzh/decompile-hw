package com.android.contacts.util;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.list.AccountFilterActivity;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListFilterController;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.google.android.gms.R;

public class AccountFilterUtil {
    private static final String TAG = AccountFilterUtil.class.getSimpleName();

    public static boolean updateAccountFilterTitleForPeople(TextView textView, ContactListFilter filter, boolean showTitleForAllAccounts) {
        return updateAccountFilterTitle(textView, filter, showTitleForAllAccounts, false);
    }

    private static boolean updateAccountFilterTitle(TextView headerTextView, ContactListFilter filter, boolean showTitleForAllAccounts, boolean forPhone) {
        Context context = headerTextView.getContext();
        AccountTypeManager acountTypeManager = AccountTypeManager.getInstance(context.getApplicationContext());
        if (filter != null) {
            AccountType accountType = acountTypeManager.getAccountType(filter.accountType, filter.dataSet);
            if (forPhone) {
                if (filter.filterType == -2) {
                    if (showTitleForAllAccounts) {
                        headerTextView.setText(CommonUtilMethods.upPercase(context.getString(R.string.list_filter_phones)));
                        return true;
                    }
                    headerTextView.setText(null);
                    return false;
                } else if (filter.filterType == 0) {
                    if (!"com.android.huawei.phone".equalsIgnoreCase(filter.accountType) && !CommonUtilMethods.isSimAccount(filter.accountType)) {
                        headerTextView.setText(CommonUtilMethods.upPercase(context.getString(R.string.contact_list_contacts_in_account, new Object[]{filter.accountName})));
                    } else if (context.getResources().getBoolean(R.bool.config_check_Russian_Grammar) && "com.android.huawei.phone".equalsIgnoreCase(filter.accountType)) {
                        headerTextView.setText(CommonUtilMethods.upPercase(context.getString(R.string.contact_list_contacts_in_account, new Object[]{context.getString(R.string.phoneLabelsGroup_from)})));
                    } else {
                        headerTextView.setText(CommonUtilMethods.upPercase(context.getString(R.string.contact_list_contacts_in_account, new Object[]{accountType.getDisplayLabel(context).toString()})));
                    }
                    return true;
                } else if (filter.filterType == -3) {
                    headerTextView.setText(CommonUtilMethods.upPercase(context.getString(R.string.contact_listCustomView)));
                    return true;
                } else {
                    headerTextView.setText(null);
                    HwLog.w(TAG, "Filter type \"" + filter.filterType + "\" isn't expected.");
                    return false;
                }
            } else if (filter.filterType == -2) {
                if (showTitleForAllAccounts) {
                    headerTextView.setText(CommonUtilMethods.upPercase(context.getString(R.string.list_filter_all_accounts)));
                    return true;
                }
                headerTextView.setText(null);
                return false;
            } else if (filter.filterType == 0) {
                if ("com.android.huawei.phone".equalsIgnoreCase(filter.accountType)) {
                    if (context.getResources().getBoolean(R.bool.config_check_Russian_Grammar)) {
                        headerTextView.setText(CommonUtilMethods.upPercase(context.getString(R.string.contact_list_contacts_in_account, new Object[]{context.getString(R.string.phoneLabelsGroup_in)})));
                    } else {
                        headerTextView.setText(CommonUtilMethods.upPercase(context.getString(R.string.contact_list_contacts_in_account, new Object[]{accountType.getDisplayLabel(context).toString()})));
                    }
                } else if (CommonUtilMethods.isSimAccount(filter.accountType)) {
                    headerTextView.setText(CommonUtilMethods.upPercase(context.getString(R.string.contact_list_contacts_in_account, new Object[]{SimFactoryManager.getSimCardDisplayLabel(filter.accountType)})));
                } else {
                    headerTextView.setText(CommonUtilMethods.upPercase(context.getString(R.string.contact_list_contacts_in_account, new Object[]{filter.accountName})));
                }
                return true;
            } else if (filter.filterType == -3) {
                headerTextView.setText(CommonUtilMethods.upPercase(context.getString(R.string.contact_listCustomView)));
                return true;
            } else if (filter.filterType == -6) {
                headerTextView.setText(CommonUtilMethods.upPercase(context.getString(R.string.listSingleContact)));
                return true;
            } else {
                headerTextView.setText(null);
                HwLog.w(TAG, "Filter type \"" + filter.filterType + "\" isn't expected.");
                return false;
            }
        }
        HwLog.w(TAG, "Filter is null.");
        return false;
    }

    public static String getFilterStringToDisplay(Context context, ContactListFilter filter, boolean showTitleForAllAccounts) {
        AccountTypeManager acountTypeManager = AccountTypeManager.getInstance(context.getApplicationContext());
        if (filter != null) {
            AccountType accountType = acountTypeManager.getAccountType(filter.accountType, filter.dataSet);
            if (filter.filterType == 0) {
                if ("com.android.huawei.phone".equalsIgnoreCase(filter.accountType)) {
                    if (context.getResources().getBoolean(R.bool.config_check_Russian_Grammar)) {
                        return context.getString(R.string.phoneLabelsGroup_in);
                    }
                    return accountType.getDisplayLabel(context).toString();
                } else if (CommonUtilMethods.isSimAccount(filter.accountType)) {
                    return SimFactoryManager.getSimCardDisplayLabel(filter.accountType);
                } else {
                    CharSequence displayLabel = accountType.getDisplayLabel(context);
                    if (displayLabel == null) {
                        return "";
                    }
                    return displayLabel.toString();
                }
            } else if (filter.filterType == -3) {
                return context.getString(R.string.contact_listCustomView);
            } else {
                if (filter.filterType == -6) {
                    return context.getString(R.string.listSingleContact);
                }
                HwLog.w(TAG, "Filter type \"" + filter.filterType + "\" isn't expected.");
                return null;
            }
        }
        HwLog.w(TAG, "Filter is null.");
        return null;
    }

    public static void startAccountFilterActivityForResult(Fragment fragment, int requestCode, ContactListFilter currentFilter) {
        Activity activity = fragment.getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, AccountFilterActivity.class);
            intent.putExtra("currentFilter", currentFilter);
            fragment.startActivityForResult(intent, requestCode);
            return;
        }
        HwLog.w(TAG, "getActivity() returned null. Ignored");
    }

    public static void handleAccountFilterResult(ContactListFilterController filterController, int resultCode, Intent data) {
        if (resultCode == -1) {
            ContactListFilter filter = (ContactListFilter) data.getParcelableExtra("contactListFilter");
            if (filter != null) {
                if (filter.filterType == -3) {
                    filterController.selectCustomFilter();
                } else {
                    filterController.setContactListFilter(filter, true);
                }
            }
        }
    }
}
