package com.android.contacts.hap.editor;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.HAPAccountListAdapter;
import com.android.contacts.hap.util.HAPAccountListAdapter.AccountListFilter;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public final class HAPSelectAccountDialogFragment extends DialogFragment {
    private AccountListFilter mAccountListFilter;
    private AccountWithDataSet mCurrentAccount;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public static void show(FragmentManager fragmentManager, Fragment targetFragment, int titleResourceId, AccountListFilter accountListFilter, Bundle extraArgs, AccountWithDataSet accountWithDataSet, String tag) {
        if (fragmentManager != null) {
            Bundle args = new Bundle();
            args.putInt("title_res_id", titleResourceId);
            args.putSerializable("list_filter", accountListFilter);
            String str = "extra_args";
            if (extraArgs == null) {
                extraArgs = Bundle.EMPTY;
            }
            args.putBundle(str, extraArgs);
            HAPSelectAccountDialogFragment instance = new HAPSelectAccountDialogFragment();
            instance.setArguments(args);
            instance.setTargetFragment(targetFragment, 1091);
            instance.setCurrentAccount(accountWithDataSet);
            instance.setAccountListFilter(accountListFilter);
            CommonUtilMethods.showFragment(fragmentManager, instance, tag);
        }
    }

    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        HAPAccountListAdapter lAccountAdapter;
        Builder builder = new Builder(getActivity());
        Bundle args = getArguments();
        Bundle bundle = args.getBundle("extra_args");
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        boolean lExportToSIM = false;
        if (bundle != null) {
            z = bundle.getBoolean("EXCLUDE_SIM");
            z2 = bundle.getBoolean("EXCLUDE_SIM1");
            z3 = bundle.getBoolean("EXCLUDE_SIM2");
            lExportToSIM = bundle.getBoolean("export_to_sim", false);
        }
        if (HwLog.HWDBG) {
            HwLog.d("HAPSelectAccountDialogFragment", "exportToSIM --> " + lExportToSIM);
        }
        if (lExportToSIM) {
            lAccountAdapter = new HAPAccountListAdapter(builder.getContext(), this.mAccountListFilter, this.mCurrentAccount, bundle);
        } else if (z || (z2 && z3)) {
            lAccountAdapter = new HAPAccountListAdapter(builder.getContext(), this.mAccountListFilter, this.mCurrentAccount, 1);
        } else if (z2) {
            lAccountAdapter = new HAPAccountListAdapter(builder.getContext(), this.mAccountListFilter, this.mCurrentAccount, 2);
        } else if (z3) {
            lAccountAdapter = new HAPAccountListAdapter(builder.getContext(), this.mAccountListFilter, this.mCurrentAccount, 3);
        } else {
            lAccountAdapter = new HAPAccountListAdapter(builder.getContext(), this.mAccountListFilter, this.mCurrentAccount, 0);
        }
        OnClickListener clickListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                HAPSelectAccountDialogFragment.this.onAccountSelected(lAccountAdapter.getItem(which));
            }
        };
        builder.setTitle(args.getInt("title_res_id"));
        builder.setSingleChoiceItems(lAccountAdapter, 0, clickListener);
        AlertDialog result = builder.create();
        if (getActivity() instanceof PeopleActivity) {
            ((PeopleActivity) getActivity()).mGlobalDialogReference = result;
        }
        return result;
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        dismiss();
        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(1091, 0, null);
        }
    }

    private void onAccountSelected(AccountWithDataSet account) {
        if (SimFactoryManager.isDualSim()) {
            String message = null;
            SharedPreferences prefs1 = SimFactoryManager.getSharedPreferences("SimInfoFile", 0);
            SharedPreferences prefs2 = SimFactoryManager.getSharedPreferences("SimInfoFile", 1);
            boolean isFirstSimDeleteInProgress = prefs1.getBoolean("sim_delete_progress", false);
            boolean isSecondSimDeleteInProgress = SimFactoryManager.getSharedPreferences("SimInfoFile", 1).getBoolean("sim_delete_progress", false);
            if (isFirstSimDeleteInProgress && !isSecondSimDeleteInProgress) {
                message = String.format(getString(R.string.delete_sim_progress), new Object[]{SimFactoryManager.getSimCardDisplayLabel(0)});
            } else if (!isFirstSimDeleteInProgress && isSecondSimDeleteInProgress) {
                message = String.format(getString(R.string.delete_sim_progress), new Object[]{SimFactoryManager.getSimCardDisplayLabel(1)});
            } else if (isFirstSimDeleteInProgress && isSecondSimDeleteInProgress) {
                message = String.format(getString(R.string.delete_sim_progress), new Object[]{getString(R.string.str_simaccount_name)});
            }
            if ((isFirstSimDeleteInProgress && account.type.equals("com.android.huawei.sim")) || (isSecondSimDeleteInProgress && account.type.equals("com.android.huawei.secondsim"))) {
                Builder builder = new Builder(getActivity()).setTitle(R.string.contact_str_copysim_notification).setPositiveButton(R.string.contact_known_button_text, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                if (!isAdded() || getActivity() == null) {
                    builder.setMessage(message);
                } else {
                    View view = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                    ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(message);
                    builder.setView(view);
                }
                builder.create().show();
                return;
            }
        }
        if ("com.android.huawei.sim".equals(account.type) || "com.android.huawei.secondsim".equals(account.type)) {
            StatisticalHelper.sendReport(1135, 1);
        } else if ("com.android.huawei.phone".equals(account.type)) {
            StatisticalHelper.sendReport(1135, 0);
        }
        if (getTargetFragment() != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("account", account);
            bundle.putParcelable("extra_args", getArguments().getBundle("extra_args"));
            Intent dataIntent = new Intent();
            dataIntent.putExtras(bundle);
            getTargetFragment().onActivityResult(1091, -1, dataIntent);
        }
    }

    public void setCurrentAccount(AccountWithDataSet accountWithDataSet) {
        this.mCurrentAccount = accountWithDataSet;
    }

    public void setAccountListFilter(AccountListFilter aAccountListFilter) {
        this.mAccountListFilter = aAccountListFilter;
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() != null && (getActivity() instanceof PeopleActivity)) {
            ((PeopleActivity) getActivity()).mGlobalDialogReference = null;
        }
    }
}
