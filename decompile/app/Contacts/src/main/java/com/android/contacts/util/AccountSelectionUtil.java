package com.android.contacts.util;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.vcard.ImportVCardActivity;
import com.google.android.gms.R;
import java.util.List;

public class AccountSelectionUtil {
    private static Uri mPath;
    private static boolean mVCardShare = false;

    public static class AccountSelectedListener implements OnClickListener {
        protected final List<AccountWithDataSet> mAccountList;
        private final Activity mActivity;
        private final int mResId;

        public AccountSelectedListener(Activity activity, List<AccountWithDataSet> accountList, int resId) {
            if (accountList == null || accountList.size() == 0) {
                HwLog.e("AccountSelectionUtil", "The size of Account list is 0.");
            }
            this.mActivity = activity;
            this.mAccountList = accountList;
            this.mResId = resId;
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            AccountSelectionUtil.doImport(this.mActivity, this.mResId, (AccountWithDataSet) this.mAccountList.get(which));
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView text1;
        TextView text2;

        ViewHolder() {
        }
    }

    public static Dialog getSelectAccountDialog(Activity activity, int resId, OnClickListener onClickListener, OnCancelListener onCancelListener, boolean aSimAccountDisabel) {
        List<AccountWithDataSet> writableAccountList;
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(activity);
        if (aSimAccountDisabel) {
            writableAccountList = accountTypes.getAccountsExcludeSim(true);
        } else {
            writableAccountList = accountTypes.getAccounts(true);
        }
        final LayoutInflater dialogInflater = (LayoutInflater) activity.getSystemService("layout_inflater");
        ArrayAdapter<AccountWithDataSet> accountAdapter = new ArrayAdapter<AccountWithDataSet>(activity, R.layout.account_selector_list_item, writableAccountList) {
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder viewHolder;
                if (convertView == null) {
                    convertView = dialogInflater.inflate(R.layout.account_selector_list_item, parent, false);
                    viewHolder = new ViewHolder();
                    viewHolder.text1 = (TextView) convertView.findViewById(16908308);
                    viewHolder.text2 = (TextView) convertView.findViewById(16908309);
                    viewHolder.icon = (ImageView) convertView.findViewById(16908294);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                AccountWithDataSet account = (AccountWithDataSet) getItem(position);
                AccountType accountType = accountTypes.getAccountType(account.type, account.dataSet);
                Context context = getContext();
                viewHolder.text1.setText(accountType.getDisplayLabel(context));
                if (CommonUtilMethods.isLocalDefaultAccount(account.type)) {
                    if (HiCloudUtil.getHicloudAccountState(context) == 1) {
                        viewHolder.text1.setText(CommonUtilMethods.getHiCloudAccountLogOnSyncStateDisplayString(context, HiCloudUtil.isHicloudSyncStateEnabled(context)));
                        viewHolder.text2.setText(HiCloudUtil.getHiCloudAccountName());
                        viewHolder.text2.setVisibility(0);
                        viewHolder.text2.setEllipsize(TruncateAt.MIDDLE);
                    } else {
                        viewHolder.text2.setVisibility(8);
                    }
                } else if (CommonUtilMethods.isSimAccount(account.type)) {
                    viewHolder.text2.setVisibility(8);
                } else {
                    viewHolder.text2.setText(account.name);
                    viewHolder.text2.setVisibility(0);
                    viewHolder.text2.setEllipsize(TruncateAt.MIDDLE);
                }
                viewHolder.icon.setImageDrawable(accountType.getDisplayIcon(context));
                return convertView;
            }
        };
        if (onClickListener == null) {
            onClickListener = new AccountSelectedListener(activity, writableAccountList, resId);
        }
        if (onCancelListener == null) {
            onCancelListener = new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                }
            };
        }
        return new Builder(activity).setTitle(R.string.Import_from_storage).setSingleChoiceItems(accountAdapter, 0, onClickListener).setOnCancelListener(onCancelListener).create();
    }

    public static void doImport(Activity activity, int resId, AccountWithDataSet account) {
        switch (resId) {
            case R.string.import_from_sim:
                doImportFromSim(activity, account);
                return;
            case R.string.import_from_sdcard:
                doImportFromSdCard(activity, account);
                return;
            default:
                HwLog.w("AccountSelectionUtil", "Unknow resource id: " + resId);
                return;
        }
    }

    public static void doImportFromSim(Activity activity, AccountWithDataSet account) {
        Intent importIntent = new Intent("android.intent.action.VIEW");
        importIntent.setType("vnd.android.cursor.item/sim-contact");
        if (account != null) {
            importIntent.putExtra("account_name", account.name);
            importIntent.putExtra("account_type", account.type);
            importIntent.putExtra("data_set", account.dataSet);
        }
        importIntent.setClassName("com.android.phone", "com.android.phone.SimContacts");
        try {
            activity.startActivity(importIntent);
        } catch (ActivityNotFoundException e) {
            HwLog.e("AccountSelectionUtil", "doImportFromSim method activity not found!");
        }
    }

    public static void doImportFromSdCard(Activity activity, AccountWithDataSet account) {
        Intent importIntent = new Intent(activity, ImportVCardActivity.class);
        if (account != null) {
            importIntent.putExtra("account_name", account.name);
            importIntent.putExtra("account_type", account.type);
            importIntent.putExtra("data_set", account.dataSet);
        }
        if (mVCardShare) {
            importIntent.setAction("android.intent.action.VIEW");
            importIntent.setData(mPath);
        }
        mVCardShare = false;
        mPath = null;
        try {
            activity.startActivityForResult(importIntent, 0);
        } catch (ActivityNotFoundException e) {
            HwLog.e("AccountSelectionUtil", "doImportFromSdCard method activity not found!");
        }
    }
}
