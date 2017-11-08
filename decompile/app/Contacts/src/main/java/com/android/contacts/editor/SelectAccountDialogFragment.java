package com.android.contacts.editor;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.AccountsListAdapter;
import com.android.contacts.util.AccountsListAdapter.AccountListFilter;

public final class SelectAccountDialogFragment extends DialogFragment {

    public interface Listener {
        void onAccountChosen(AccountWithDataSet accountWithDataSet, Bundle bundle);

        void onAccountSelectorCancelled();
    }

    public static <F extends Fragment & Listener> void show(FragmentManager fragmentManager, F targetFragment, int titleResourceId, AccountListFilter accountListFilter, Bundle extraArgs) {
        Bundle args = new Bundle();
        args.putInt("title_res_id", titleResourceId);
        args.putSerializable("list_filter", accountListFilter);
        String str = "extra_args";
        if (extraArgs == null) {
            extraArgs = Bundle.EMPTY;
        }
        args.putBundle(str, extraArgs);
        SelectAccountDialogFragment instance = new SelectAccountDialogFragment();
        instance.setArguments(args);
        instance.setTargetFragment(targetFragment, 0);
        instance.show(fragmentManager, null);
    }

    public static void showFragment(FragmentManager fragmentManager, Fragment targetFragment, int titleResourceId, AccountListFilter accountListFilter, Bundle extraArgs) {
        Bundle args = new Bundle();
        args.putInt("title_res_id", titleResourceId);
        args.putSerializable("list_filter", accountListFilter);
        String str = "extra_args";
        if (extraArgs == null) {
            extraArgs = Bundle.EMPTY;
        }
        args.putBundle(str, extraArgs);
        SelectAccountDialogFragment instance = new SelectAccountDialogFragment();
        instance.setArguments(args);
        if (targetFragment != null) {
            instance.setTargetFragment(targetFragment, 0);
        }
        fragmentManager.beginTransaction().add(instance, "").commitAllowingStateLoss();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new Builder(getActivity());
        Bundle args = getArguments();
        final AccountsListAdapter accountAdapter = new AccountsListAdapter(builder.getContext(), (AccountListFilter) args.getSerializable("list_filter"), null);
        OnClickListener clickListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                SelectAccountDialogFragment.this.onAccountSelected(accountAdapter.getItem(which));
            }
        };
        builder.setTitle(args.getInt("title_res_id"));
        builder.setSingleChoiceItems(accountAdapter, 0, clickListener);
        return builder.create();
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null && (targetFragment instanceof Listener)) {
            ((Listener) targetFragment).onAccountSelectorCancelled();
        } else if (getActivity() instanceof Listener) {
            ((Listener) getActivity()).onAccountSelectorCancelled();
        }
    }

    private void onAccountSelected(AccountWithDataSet account) {
        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null && (targetFragment instanceof Listener)) {
            ((Listener) targetFragment).onAccountChosen(account, getArguments().getBundle("extra_args"));
        } else if (getActivity() instanceof Listener) {
            ((Listener) getActivity()).onAccountChosen(account, getArguments().getBundle("extra_args"));
        }
    }
}
