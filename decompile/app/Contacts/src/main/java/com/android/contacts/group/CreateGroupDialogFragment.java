package com.android.contacts.group;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.amap.api.services.core.AMapException;
import com.android.contacts.ContactSaveService;
import com.android.contacts.GroupMetaDataLoader;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.editor.SelectAccountDialogFragment;
import com.android.contacts.group.GroupDetailFragment.Listener;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AccountsListAdapter.AccountListFilter;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public class CreateGroupDialogFragment extends DialogFragment {
    private static final String TAG = CreateGroupDialogFragment.class.getSimpleName();
    private static CreateGroupDialogFragment instance;
    private static boolean mFragmentAdded;
    private AlertDialog alertDialog;
    private Listener groupDetailListener;
    private String mAccountName;
    private String mAccountType;
    private String mAction;
    private Activity mActivity;
    private String mDataSet;
    private ArrayList<AccountWithDataSet> mFilteredAccountsWithDataSet;
    private long mGroupId;
    private final LoaderCallbacks<Cursor> mGroupMetaDataLoaderListener = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            if (HwLog.HWDBG) {
                HwLog.d(CreateGroupDialogFragment.TAG, "onCreateLoader mGroupUri:" + CreateGroupDialogFragment.this.mGroupUri);
            }
            return new GroupMetaDataLoader(CreateGroupDialogFragment.this.mActivity.getApplicationContext(), CreateGroupDialogFragment.this.mGroupUri);
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                if (HwLog.HWDBG) {
                    HwLog.d(CreateGroupDialogFragment.TAG, "onLoadFinished mGroupUri:" + CreateGroupDialogFragment.this.mGroupUri);
                }
                if (data.moveToFirst()) {
                    CreateGroupDialogFragment.this.bindGroupMetaData(data);
                    CreateGroupDialogFragment.showEditDialog(CreateGroupDialogFragment.this.mActivity);
                    return;
                }
                if (CreateGroupDialogFragment.this.mListener != null) {
                    CreateGroupDialogFragment.this.mListener.onGroupNotFound();
                }
                CreateGroupDialogFragment.this.updateTitle(null);
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
            if (HwLog.HWDBG) {
                HwLog.d(CreateGroupDialogFragment.TAG, "onLoaderReset");
            }
        }
    };
    private boolean mGroupNameIsReadOnly;
    private EditText mGroupNameView;
    private Uri mGroupUri;
    private Bundle mIntentExtras;
    private boolean mIsPredefined;
    private GroupEditorFragment.Listener mListener;
    private LoaderManager mLoadMananger;
    private String mOriginalGroupName = "";
    private Button mPositiveButton;

    public void setGroupDetailListener(Listener groupDetailListener) {
        this.groupDetailListener = groupDetailListener;
    }

    public void setLoadMananger(LoaderManager loadMananger) {
        this.mLoadMananger = loadMananger;
    }

    public void setListener(GroupEditorFragment.Listener Listener) {
        this.mListener = Listener;
    }

    public static synchronized CreateGroupDialogFragment newInstance() {
        CreateGroupDialogFragment createGroupDialogFragment;
        synchronized (CreateGroupDialogFragment.class) {
            if (instance == null) {
                instance = new CreateGroupDialogFragment();
                mFragmentAdded = false;
            }
            createGroupDialogFragment = instance;
        }
        return createGroupDialogFragment;
    }

    public void showDialog(Activity activity, String action, Uri groupUri, Bundle intentExtras) {
        load(action, groupUri, intentExtras);
        this.mActivity = activity;
        if ("android.intent.action.EDIT".equals(action)) {
            startGroupMetadataLoader(this.mLoadMananger);
        } else if ("android.intent.action.INSERT".equals(action)) {
            Account account;
            String str;
            if (EmuiFeatureManager.isAndroidMVersion()) {
                if (this.mIntentExtras == null) {
                    account = null;
                } else {
                    account = (Account) this.mIntentExtras.getParcelable("android.provider.extra.ACCOUNT");
                }
                if (this.mIntentExtras == null) {
                    str = null;
                } else {
                    str = this.mIntentExtras.getString("android.provider.extra.DATA_SET");
                }
            } else {
                if (this.mIntentExtras == null) {
                    account = null;
                } else {
                    account = (Account) this.mIntentExtras.getParcelable("com.android.contacts.extra.ACCOUNT");
                }
                if (this.mIntentExtras == null) {
                    str = null;
                } else {
                    str = this.mIntentExtras.getString("com.android.contacts.extra.DATA_SET");
                }
            }
            if (HwLog.HWFLOW) {
                HwLog.d(TAG, "showDialog account != null:" + (account != null));
            }
            if (account != null) {
                this.mAccountName = account.name;
                this.mAccountType = account.type;
                this.mDataSet = str;
                showEditDialog(activity);
            } else if (activity != null) {
                if (activity.getIntent() != null) {
                    this.mFilteredAccountsWithDataSet = activity.getIntent().getParcelableArrayListExtra("target_account");
                }
                selectAccountAndCreateGroup(activity);
            }
        }
    }

    public void setmActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public void onDestroy() {
        if (this.mLoadMananger != null) {
            this.mLoadMananger.destroyLoader(2);
        }
        this.mActivity = null;
        destroyInstance();
        super.onDestroy();
    }

    private static void destroyInstance() {
        instance = null;
        mFragmentAdded = false;
    }

    private void selectAccountAndCreateGroup(Activity activity) {
        List<AccountWithDataSet> accounts = AccountTypeManager.getInstance(activity.getApplicationContext()).getGroupWritableAccounts();
        if (accounts.isEmpty()) {
            HwLog.e(TAG, "No accounts were found.");
            if (this.mListener != null) {
                this.mListener.onAccountsNotFound();
            }
            return;
        }
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "accounts.size():" + accounts.size());
        }
        if (accounts.size() == 1) {
            this.mAccountName = ((AccountWithDataSet) accounts.get(0)).name;
            this.mAccountType = ((AccountWithDataSet) accounts.get(0)).type;
            this.mDataSet = ((AccountWithDataSet) accounts.get(0)).dataSet;
            showEditDialog(activity);
            return;
        }
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "mFilteredAccountsWithDataSet:" + (this.mFilteredAccountsWithDataSet != null));
        }
        if (this.mFilteredAccountsWithDataSet == null || this.mFilteredAccountsWithDataSet.size() != 1) {
            if (!(activity instanceof PeopleActivity)) {
                SelectAccountDialogFragment.showFragment(activity.getFragmentManager(), getTargetFragment(), R.string.dialog_new_group_account, AccountListFilter.ACCOUNTS_GROUP_WRITABLE, null);
            } else if (((PeopleActivity) activity).getFrameFragmentManager() != null) {
                SelectAccountDialogFragment.showFragment(((PeopleActivity) activity).getFrameFragmentManager(), getTargetFragment(), R.string.dialog_new_group_account, AccountListFilter.ACCOUNTS_GROUP_WRITABLE, null);
            }
            return;
        }
        this.mAccountName = ((AccountWithDataSet) this.mFilteredAccountsWithDataSet.get(0)).name;
        this.mAccountType = ((AccountWithDataSet) this.mFilteredAccountsWithDataSet.get(0)).type;
        this.mDataSet = ((AccountWithDataSet) this.mFilteredAccountsWithDataSet.get(0)).dataSet;
        showEditDialog(activity);
    }

    private static void showEditDialog(Activity activity) {
        if (activity != null && !mFragmentAdded) {
            mFragmentAdded = true;
            if (!(activity instanceof PeopleActivity)) {
                activity.getFragmentManager().beginTransaction().add(instance, "").commitAllowingStateLoss();
            } else if (((PeopleActivity) activity).getFrameFragmentManager() != null) {
                ((PeopleActivity) activity).getFrameFragmentManager().beginTransaction().add(instance, "").commitAllowingStateLoss();
            }
        }
    }

    public Dialog onCreateDialog(Bundle state) {
        if (state != null) {
            this.mAction = state.getString("action");
            this.mGroupUri = (Uri) state.getParcelable("groupUri");
            this.mGroupId = state.getLong("groupId");
            this.mAccountName = state.getString("accountName");
            this.mAccountType = state.getString("accountType");
            this.mDataSet = state.getString("dataSet");
            this.mGroupNameIsReadOnly = state.getBoolean("groupNameIsReadOnly");
            this.mOriginalGroupName = state.getString("originalGroupName");
            this.mIsPredefined = state.getBoolean("isPreDefined");
            if (HwLog.HWDBG) {
                HwLog.d(TAG, "onCreateDialog action:" + this.mAction);
            }
        }
        this.alertDialog = new Builder(getActivity()).create();
        String title = "";
        if ("android.intent.action.INSERT".equals(this.mAction)) {
            title = getResources().getString(R.string.create_group_dialog_title);
        } else if ("android.intent.action.EDIT".equals(this.mAction)) {
            title = getResources().getString(R.string.contacts_group_rename_title);
        }
        this.alertDialog.setTitle(title);
        View view = ((LayoutInflater) getActivity().getSystemService("layout_inflater")).inflate(R.layout.group_create_dialog, null);
        this.alertDialog.setView(view);
        this.alertDialog.setButton(-2, getActivity().getString(17039360), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        this.alertDialog.setButton(-1, getActivity().getString(17039370), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CreateGroupDialogFragment.this.save(CreateGroupDialogFragment.this.getActivity());
                dialog.dismiss();
            }
        });
        this.alertDialog.setCanceledOnTouchOutside(false);
        this.alertDialog.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(DialogInterface aDialog, int aKeyCode, KeyEvent aKeyEvent) {
                if (4 != aKeyCode) {
                    return false;
                }
                CreateGroupDialogFragment.this.dismiss();
                return true;
            }
        });
        this.alertDialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialog) {
                CreateGroupDialogFragment.this.mPositiveButton = CreateGroupDialogFragment.this.alertDialog.getButton(-1);
                CreateGroupDialogFragment.this.mPositiveButton.setEnabled(CreateGroupDialogFragment.this.hasValidGroupName());
            }
        });
        this.mGroupNameView = (EditText) view.findViewById(R.id.input_group_name);
        this.mGroupNameView.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (CreateGroupDialogFragment.this.mPositiveButton == null) {
                    return;
                }
                if (s == null || s.toString().trim().length() == 0) {
                    CreateGroupDialogFragment.this.mPositiveButton.setEnabled(false);
                } else {
                    CreateGroupDialogFragment.this.mPositiveButton.setEnabled(true);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        setGroupNameViewText();
        return this.alertDialog;
    }

    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().clearFlags(131080);
            dialog.getWindow().setSoftInputMode(5);
        }
    }

    private void setGroupNameViewText() {
        if (!((this.alertDialog != null && this.alertDialog.isShowing()) || this.mGroupNameView == null || this.mOriginalGroupName == null)) {
            this.mGroupNameView.setText(this.mOriginalGroupName);
            this.mGroupNameView.setSelection(this.mOriginalGroupName.length());
        }
    }

    public void load(String action, Uri groupUri, Bundle intentExtras) {
        this.mAction = action;
        this.mGroupUri = groupUri;
        this.mGroupId = groupUri != null ? ContentUris.parseId(this.mGroupUri) : 0;
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "action:" + action + " uri:" + groupUri + " mGroupId:" + this.mGroupId);
        }
        this.mIntentExtras = intentExtras;
    }

    private void startGroupMetadataLoader(LoaderManager loadManager) {
        if (loadManager != null) {
            loadManager.initLoader(2, new Bundle(), this.mGroupMetaDataLoaderListener);
        }
    }

    private void bindGroupMetaData(Cursor cursor) {
        boolean deleted;
        HwLog.i(TAG, "Group not found with URI: " + this.mGroupUri + " Closing activity now.");
        if (cursor.getInt(8) == 1) {
            deleted = true;
        } else {
            deleted = false;
        }
        if (deleted) {
            updateTitle(null);
            return;
        }
        boolean z;
        this.mOriginalGroupName = cursor.getString(4);
        this.mAccountName = cursor.getString(0);
        this.mAccountType = cursor.getString(1);
        if (this.mActivity != null) {
            this.mOriginalGroupName = CommonUtilMethods.parseGroupDisplayName(this.mAccountType, this.mOriginalGroupName, this.mActivity.getApplicationContext(), cursor.getString(9), cursor.getInt(10), cursor.getString(11));
        }
        this.mIsPredefined = CommonUtilMethods.isPredefinedGroup(cursor.getString(12));
        this.mDataSet = cursor.getString(2);
        if (cursor.getInt(7) == 1) {
            z = true;
        } else {
            z = false;
        }
        this.mGroupNameIsReadOnly = z;
        updateTitle(this.mOriginalGroupName);
        setGroupNameViewText();
        if (this.mActivity != null) {
            this.mActivity.invalidateOptionsMenu();
        }
    }

    private void updateTitle(String title) {
        if (this.groupDetailListener != null) {
            this.groupDetailListener.onGroupTitleUpdated(title);
        }
    }

    public boolean save(Activity activity) {
        if (activity == null) {
            return false;
        }
        if (!hasValidGroupName()) {
            Toast.makeText(activity, R.string.cannot_save_group_Toast, 0).show();
            return false;
        } else if (hasNameChange()) {
            if (HwLog.HWDBG) {
                HwLog.d(TAG, "save() mAction:" + this.mAction);
            }
            Intent saveIntent = null;
            long[] membersToAddArray = new long[0];
            if ("android.intent.action.INSERT".equals(this.mAction)) {
                saveIntent = ContactSaveService.createNewGroupIntent(activity, new AccountWithDataSet(this.mAccountName, this.mAccountType, this.mDataSet), this.mGroupNameView.getText().toString().trim(), membersToAddArray, activity.getClass(), "saveCompleted");
                StatisticalHelper.report(AMapException.CODE_AMAP_ENGINE_RESPONSE_DATA_ERROR);
            } else if ("android.intent.action.EDIT".equals(this.mAction)) {
                Context context = activity;
                long[] jArr = membersToAddArray;
                saveIntent = ContactSaveService.createGroupRenameIntent(context, this.mGroupId, new AccountWithDataSet(this.mAccountName, this.mAccountType, this.mDataSet), getUpdatedName(), jArr, new long[0], activity.getClass(), "saveCompleted", this.mIsPredefined);
            }
            if (saveIntent != null) {
                activity.startService(saveIntent);
            }
            return true;
        } else {
            onSaveCompleted(activity, false, this.mGroupUri);
            return true;
        }
    }

    private String getUpdatedName() {
        String groupNameFromTextView = this.mGroupNameView.getText().toString().trim();
        if (groupNameFromTextView.equals(this.mOriginalGroupName)) {
            return null;
        }
        return groupNameFromTextView;
    }

    public void onSaveCompleted(Activity activity, boolean hadChanges, Uri groupUri) {
        int resultCode;
        Intent resultIntent;
        boolean success = groupUri != null;
        HwLog.d(TAG, "onSaveCompleted(" + groupUri + ")");
        if (hadChanges && !success) {
            Toast.makeText(activity, R.string.groupSavedErrorToast_Toast, 0).show();
        }
        if (!success || groupUri == null) {
            resultCode = 0;
            resultIntent = null;
        } else {
            String requestAuthority = groupUri.getAuthority();
            resultIntent = new Intent();
            if ("contacts".equals(requestAuthority)) {
                resultIntent.setData(ContentUris.withAppendedId(Uri.parse("content://contacts/groups"), ContentUris.parseId(groupUri)));
            } else {
                resultIntent.setData(groupUri);
            }
            resultCode = -1;
            resultIntent.setAction(this.mAction);
        }
        if (this.mListener != null) {
            this.mListener.onSaveFinished(resultCode, resultIntent);
        }
    }

    private boolean hasValidGroupName() {
        return (this.mGroupNameView == null || TextUtils.isEmpty(this.mGroupNameView.getText().toString().trim())) ? false : true;
    }

    private boolean hasNameChange() {
        if (this.mGroupNameView == null || this.mGroupNameView.getText().toString().equals(this.mOriginalGroupName)) {
            return false;
        }
        return true;
    }

    public void onAccountChosen(AccountWithDataSet account, Bundle extraArgs) {
        this.mAccountName = account.name;
        this.mAccountType = account.type;
        this.mDataSet = account.dataSet;
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "onAccountChosen mActivity:" + (this.mActivity == null));
        }
        showEditDialog(this.mActivity);
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.mAction = savedInstanceState.getString("action");
            if (HwLog.HWDBG) {
                HwLog.d(TAG, "onViewStateRestored action:" + this.mAction);
            }
        }
        super.onViewStateRestored(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("action", this.mAction);
        outState.putParcelable("groupUri", this.mGroupUri);
        outState.putLong("groupId", this.mGroupId);
        outState.putString("accountName", this.mAccountName);
        outState.putString("accountType", this.mAccountType);
        outState.putString("dataSet", this.mDataSet);
        outState.putBoolean("groupNameIsReadOnly", this.mGroupNameIsReadOnly);
        outState.putString("originalGroupName", this.mOriginalGroupName);
        outState.putBoolean("isPreDefined", this.mIsPredefined);
    }
}
