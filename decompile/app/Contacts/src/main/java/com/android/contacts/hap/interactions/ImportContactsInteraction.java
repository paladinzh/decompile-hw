package com.android.contacts.hap.interactions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.widget.ArrayAdapter;
import com.android.contacts.activities.TransactionSafeActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimStateListener;
import com.android.contacts.hap.util.ManageContactsUtil;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.util.ExceptionCapture;
import com.google.android.gms.R;
import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;

public class ImportContactsInteraction {
    public static final String IMPORTCONTACTSINTERACTIONTAG = ImportContactsInteraction.class.getSimpleName();
    public Activity mActivity;

    public static class ImportContactsDialogFragment extends DialogFragment implements OnClickListener, OnDismissListener, SimStateListener {
        private String ACCOUNT_CHOOSER_TAG = "accounts_chooser_tag_icd";
        private AlertDialog mAlertDialog;
        private List<ImportItem> mImportList;
        private ArrayAdapter<ImportItem> mImportListAdapter;

        public void onResume() {
            SimFactoryManager.addSimStateListener(this);
            super.onResume();
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Fragment dialogFragment = getFragmentManager().findFragmentByTag(this.ACCOUNT_CHOOSER_TAG);
            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(getTargetFragment(), 1091);
            }
        }

        public void onPause() {
            SimFactoryManager.removeSimStateListener(this);
            if (this.mAlertDialog != null) {
                this.mAlertDialog.dismiss();
            }
            super.onPause();
        }

        public static void show(FragmentManager aFragmentManager, Fragment targetFragment, ArrayList<ImportItem> aImportList) {
            ImportContactsDialogFragment importDialogFragment = new ImportContactsDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("ImportList", aImportList);
            importDialogFragment.setArguments(bundle);
            importDialogFragment.setTargetFragment(targetFragment, 1091);
            importDialogFragment.show(aFragmentManager, ImportContactsInteraction.IMPORTCONTACTSINTERACTIONTAG);
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Activity activity = getActivity();
            this.mImportList = getArguments().getParcelableArrayList("ImportList");
            this.mImportListAdapter = new ArrayAdapter(activity, R.layout.select_dialog_item, this.mImportList);
            this.mAlertDialog = new Builder(activity).setAdapter(this.mImportListAdapter, this).setTitle(R.string.btntxt_import_contacts).create();
            return this.mAlertDialog;
        }

        public void onClick(DialogInterface aDialog, int aWhich) {
            if (this.mImportList.size() <= aWhich || aWhich < 0) {
                aDialog.dismiss();
                return;
            }
            switch (((ImportItem) this.mImportList.get(aWhich)).mTag) {
                case 1:
                    ExceptionCapture.reportScene(80);
                    ManageContactsUtil.handleImportFromSDCardRequest(getActivity(), getTargetFragment());
                    return;
                case 2:
                    getActivity().startActivity(CommonUtilMethods.getIntentForCopyFromSimActivity(-1));
                    return;
                case 3:
                    getActivity().startActivity(CommonUtilMethods.getIntentForCopyFromSimActivity(0));
                    return;
                case 4:
                    getActivity().startActivity(CommonUtilMethods.getIntentForCopyFromSimActivity(1));
                    return;
                case 5:
                    ExceptionCapture.reportScene(81);
                    getActivity().startActivity(CommonUtilMethods.getImportContactsViaotherPhonesIntent());
                    return;
                default:
                    return;
            }
        }

        public void simStateChanged(int aSubScription) {
            this.mImportList = ImportContactsInteraction.prepareListItem(getActivity());
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    ImportContactsDialogFragment.this.mImportListAdapter.clear();
                    ImportContactsDialogFragment.this.mImportListAdapter.addAll(ImportContactsDialogFragment.this.mImportList);
                    ImportContactsDialogFragment.this.mImportListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @VisibleForTesting
    static class ImportItem implements Parcelable {
        public static final Creator<ImportItem> CREATOR = new Creator<ImportItem>() {
            public ImportItem createFromParcel(Parcel aIn) {
                return new ImportItem(aIn);
            }

            public ImportItem[] newArray(int aSize) {
                return new ImportItem[aSize];
            }
        };
        String mImportOption;
        int mTag;

        public ImportItem(Parcel aIn) {
            this.mTag = aIn.readInt();
            this.mImportOption = aIn.readString();
        }

        public String toString() {
            return this.mImportOption;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel aDest, int aFlags) {
            aDest.writeInt(this.mTag);
            aDest.writeString(this.mImportOption);
        }
    }

    public ImportContactsInteraction(Activity aActivity) {
        this.mActivity = aActivity;
    }

    private boolean isSafeToCommitTransactions() {
        if (this.mActivity instanceof TransactionSafeActivity) {
            return ((TransactionSafeActivity) this.mActivity).isSafeToCommitTransactions();
        }
        return true;
    }

    public void startLoadImportOptions(Fragment targetFragment) {
        if (isSafeToCommitTransactions()) {
            ImportContactsDialogFragment.show(this.mActivity.getFragmentManager(), targetFragment, prepareListItem(this.mActivity));
        }
    }

    public static ArrayList<ImportItem> prepareListItem(Activity aActivity) {
        ArrayList<ImportItem> importListItem = new ArrayList();
        ImportItem storageItem = new ImportItem();
        storageItem.mTag = 1;
        storageItem.mImportOption = aActivity.getResources().getString(R.string.storage_option);
        importListItem.add(storageItem);
        if (MultiUsersUtils.isCurrentUserOwner()) {
            String importOption = aActivity.getResources().getString(R.string.import_option_for_sim_card);
            if (SimFactoryManager.isBothSimEnabled()) {
                String mSim1Label = aActivity.getResources().getString(R.string.sim_one_account_name);
                String mSim2Label = aActivity.getResources().getString(R.string.sim_two_account_name);
                ImportItem firstSimItem = new ImportItem();
                ImportItem secondSimItem = new ImportItem();
                firstSimItem.mTag = 3;
                firstSimItem.mImportOption = mSim1Label;
                importListItem.add(firstSimItem);
                secondSimItem.mTag = 4;
                secondSimItem.mImportOption = mSim2Label;
                importListItem.add(secondSimItem);
            } else if (!SimFactoryManager.isNoSimEnabled()) {
                ImportItem singleSimItem = new ImportItem();
                singleSimItem.mTag = 2;
                if (aActivity.getResources().getBoolean(R.bool.config_check_Russian_Grammar)) {
                    singleSimItem.mImportOption = String.format(importOption, new Object[]{aActivity.getResources().getString(R.string.sim_account_name)});
                    importListItem.add(singleSimItem);
                } else {
                    singleSimItem.mImportOption = String.format(importOption, new Object[]{aActivity.getResources().getString(R.string.str_simaccount_name)});
                    importListItem.add(singleSimItem);
                }
            }
        }
        if (CommonUtilMethods.isActivityAvailable(aActivity, CommonUtilMethods.getImportContactsViaBtIntent())) {
            ImportItem otherPhoneItem = new ImportItem();
            otherPhoneItem.mTag = 5;
            otherPhoneItem.mImportOption = aActivity.getResources().getString(R.string.another_device_dialog_option);
            importListItem.add(otherPhoneItem);
        }
        return importListItem;
    }
}
