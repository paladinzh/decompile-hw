package com.android.contacts.interactions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.contacts.ContactSaveService;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.activities.VoiceSearchResultActivity;
import com.android.contacts.hap.numbermark.YellowPageContactUtil;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HiCloudUtil;
import com.google.android.gms.R;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.util.HashSet;

public class ContactDeletionInteraction extends Fragment implements LoaderCallbacks<Cursor>, OnDismissListener {
    private static final String[] ENTITY_PROJECTION = new String[]{"raw_contact_id", "account_type", "data_set", "contact_id", "lookup"};
    private static boolean mIsDelFromDetail;
    private static boolean mIsUserProfile;
    private boolean mActive;
    private Uri mContactUri;
    private Context mContext;
    private AlertDialog mDialog;
    private boolean mFinishActivityWhenDone;
    private int mLandColumnsNum = -1;
    @VisibleForTesting
    int mMessageId;
    private int mPortColumnsNum = -1;
    private int mStackEntryCount;
    private TestLoaderManager mTestLoaderManager;

    public static ContactDeletionInteraction start(Activity activity, Uri contactUri, boolean aIsDelFromDetail, int landColumns, int portColumns, int stackEntryCount) {
        mIsDelFromDetail = aIsDelFromDetail;
        if (contactUri == null || YellowPageContactUtil.isYellowPageUri(contactUri)) {
            return null;
        }
        FragmentManager fragmentManager = activity.getFragmentManager();
        ContactDeletionInteraction fragment = (ContactDeletionInteraction) fragmentManager.findFragmentByTag("deleteContact");
        if (fragment == null) {
            fragment = new ContactDeletionInteraction();
            fragment.setTestLoaderManager(null);
            fragment.setContactUri(contactUri);
            fragment.mLandColumnsNum = landColumns;
            fragment.mPortColumnsNum = portColumns;
            fragment.mStackEntryCount = stackEntryCount;
            fragmentManager.beginTransaction().add(fragment, "deleteContact").commitAllowingStateLoss();
        } else {
            fragment.setTestLoaderManager(null);
            fragment.setContactUri(contactUri);
            fragment.mLandColumnsNum = landColumns;
            fragment.mPortColumnsNum = portColumns;
            fragment.mStackEntryCount = stackEntryCount;
        }
        setFinishActivityWhenDone(fragment, activity, landColumns, portColumns, stackEntryCount);
        return fragment;
    }

    public static ContactDeletionInteraction start(Activity activity, Uri contactUri, boolean finishActivityWhenDone, boolean aIsDelFromDetail) {
        mIsDelFromDetail = aIsDelFromDetail;
        return startWithTestLoaderManager(activity, contactUri, finishActivityWhenDone, null);
    }

    @VisibleForTesting
    static ContactDeletionInteraction startWithTestLoaderManager(Activity activity, Uri contactUri, boolean finishActivityWhenDone, TestLoaderManager testLoaderManager) {
        if (contactUri == null || YellowPageContactUtil.isYellowPageUri(contactUri)) {
            return null;
        }
        FragmentManager fragmentManager = activity.getFragmentManager();
        ContactDeletionInteraction fragment = (ContactDeletionInteraction) fragmentManager.findFragmentByTag("deleteContact");
        if (fragment == null) {
            fragment = new ContactDeletionInteraction();
            fragment.setTestLoaderManager(testLoaderManager);
            fragment.setContactUri(contactUri);
            fragment.setFinishActivityWhenDone(finishActivityWhenDone);
            fragmentManager.beginTransaction().add(fragment, "deleteContact").commitAllowingStateLoss();
        } else {
            fragment.setTestLoaderManager(testLoaderManager);
            fragment.setContactUri(contactUri);
            fragment.setFinishActivityWhenDone(finishActivityWhenDone);
        }
        return fragment;
    }

    public LoaderManager getLoaderManager() {
        LoaderManager loaderManager = super.getLoaderManager();
        if (this.mTestLoaderManager == null) {
            return loaderManager;
        }
        this.mTestLoaderManager.setDelegate(loaderManager);
        return this.mTestLoaderManager;
    }

    private void setTestLoaderManager(TestLoaderManager mockLoaderManager) {
        this.mTestLoaderManager = mockLoaderManager;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.setOnDismissListener(null);
            this.mDialog.dismiss();
        }
    }

    public void setContactUri(Uri contactUri) {
        this.mContactUri = contactUri;
        this.mActive = true;
        if (isStarted()) {
            Bundle args = new Bundle();
            args.putParcelable("contactUri", this.mContactUri);
            getLoaderManager().restartLoader(R.id.dialog_delete_contact_loader_id, args, this);
        }
    }

    private void setFinishActivityWhenDone(boolean finishActivityWhenDone) {
        this.mFinishActivityWhenDone = finishActivityWhenDone;
    }

    boolean isStarted() {
        return isAdded();
    }

    public void onStart() {
        if (this.mActive) {
            Bundle args = new Bundle();
            args.putParcelable("contactUri", this.mContactUri);
            getLoaderManager().initLoader(R.id.dialog_delete_contact_loader_id, args, this);
        }
        super.onStart();
    }

    public void onStop() {
        super.onStop();
        if (this.mDialog != null) {
            this.mDialog.hide();
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this.mContext, Uri.withAppendedPath((Uri) args.getParcelable("contactUri"), "entities"), ENTITY_PROJECTION, null, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            if (this.mDialog != null) {
                this.mDialog.setOnDismissListener(null);
                this.mDialog.dismiss();
                this.mDialog = null;
            }
            if (this.mActive) {
                long contactId = 0;
                String lookupKey = null;
                boolean isPhoneAccountType = false;
                HashSet<Long> readOnlyRawContacts = Sets.newHashSet();
                HashSet<Long> writableRawContacts = Sets.newHashSet();
                AccountTypeManager accountTypes = AccountTypeManager.getInstance(getActivity());
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    long rawContactId = cursor.getLong(0);
                    String accountType = cursor.getString(1);
                    String dataSet = cursor.getString(2);
                    contactId = cursor.getLong(3);
                    lookupKey = cursor.getString(4);
                    AccountType type = accountTypes.getAccountType(accountType, dataSet);
                    if (!isPhoneAccountType && "com.android.huawei.phone".equals(accountType)) {
                        isPhoneAccountType = true;
                    }
                    if (type != null ? type.areContactsWritable() : true) {
                        writableRawContacts.add(Long.valueOf(rawContactId));
                    } else {
                        readOnlyRawContacts.add(Long.valueOf(rawContactId));
                    }
                }
                boolean isHiCloudEnabled = HiCloudUtil.isHicloudSyncStateEnabled(this.mContext);
                int readOnlyCount = readOnlyRawContacts.size();
                int writableCount = writableRawContacts.size();
                if (readOnlyCount > 0 && writableCount > 0) {
                    this.mMessageId = R.string.readOnlyMultipleContactDeleteHiCloud_message;
                } else if (readOnlyCount > 0 && writableCount == 0) {
                    this.mMessageId = R.string.readOnlyContactDelete;
                } else if (readOnlyCount == 0 && writableCount > 1) {
                    this.mMessageId = R.string.multipleContactDeleteHiCloud_message;
                } else if (isHiCloudEnabled && isPhoneAccountType) {
                    this.mMessageId = R.string.deleteHiCloud_message;
                } else if (mIsDelFromDetail) {
                    this.mMessageId = R.string.deleteConfirmation;
                } else {
                    this.mMessageId = R.string.delete_confirmation_selected_title;
                }
                Uri contactUri = Contacts.getLookupUri(contactId, lookupKey);
                Activity lActivity = getActivity();
                if (!(lActivity == null || lActivity.isFinishing())) {
                    showDialog(this.mMessageId, contactUri);
                }
                getLoaderManager().destroyLoader(R.id.dialog_delete_contact_loader_id);
            }
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void showDialog(int messageId, final Uri contactUri) {
        int titleID;
        Builder builder = new Builder(getActivity()).setIconAttribute(16843605).setNegativeButton(17039360, null).setPositiveButton(getString(R.string.menu_deleteContact), new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ContactDeletionInteraction.this.doDeleteContact(contactUri);
                StatisticalHelper.report(1122);
                ExceptionCapture.reportScene(62);
            }
        });
        if (mIsDelFromDetail) {
            titleID = R.string.deleteConfirmation;
        } else {
            titleID = R.string.delete_confirmation_selected_title;
        }
        if (titleID != messageId) {
            if (!isAdded() || getActivity() == null) {
                builder.setMessage(messageId);
            } else {
                View view = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(messageId);
                builder.setView(view);
            }
            builder.setTitle(titleID);
        } else {
            builder.setMessage(titleID);
        }
        this.mDialog = builder.create();
        this.mDialog.setMessageNotScrolling();
        this.mDialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialog) {
                if (ContactDeletionInteraction.this.mDialog != null) {
                    Button aPositiveButton = ContactDeletionInteraction.this.mDialog.getButton(-1);
                    if (!(aPositiveButton == null || ContactDeletionInteraction.this.getActivity() == null)) {
                        aPositiveButton.setTextColor(ContactDeletionInteraction.this.getActivity().getResources().getColor(R.color.delete_text_color));
                    }
                }
            }
        });
        this.mDialog.setOnDismissListener(this);
        if (getActivity() instanceof PeopleActivity) {
            ((PeopleActivity) getActivity()).mGlobalDialogReference = this.mDialog;
        }
        this.mDialog.show();
    }

    public void onDismiss(DialogInterface dialog) {
        Activity lActivity = getActivity();
        if (lActivity != null && (lActivity instanceof PeopleActivity)) {
            ((PeopleActivity) lActivity).mGlobalDialogReference = null;
        }
        this.mActive = false;
        this.mDialog = null;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("active", this.mActive);
        outState.putParcelable("contactUri", this.mContactUri);
        outState.putBoolean("finishWhenDone", this.mFinishActivityWhenDone);
        outState.putInt("key_land_columns_num", this.mLandColumnsNum);
        outState.putInt("key_port_columns_num", this.mPortColumnsNum);
        outState.putInt("key_stack_count", this.mStackEntryCount);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            this.mActive = savedInstanceState.getBoolean("active");
            this.mContactUri = (Uri) savedInstanceState.getParcelable("contactUri");
            this.mFinishActivityWhenDone = savedInstanceState.getBoolean("finishWhenDone");
            this.mLandColumnsNum = savedInstanceState.getInt("key_land_columns_num", -1);
            this.mPortColumnsNum = savedInstanceState.getInt("key_port_columns_num", -1);
            this.mStackEntryCount = savedInstanceState.getInt("key_stack_count", -1);
        }
        setFinishActivityWhenDone(this, getContext(), this.mLandColumnsNum, this.mPortColumnsNum, this.mStackEntryCount);
    }

    private static void setFinishActivityWhenDone(ContactDeletionInteraction fragment, Context context, int landColumns, int portColumns, int stackEntryCount) {
        int oren = context.getResources().getConfiguration().orientation;
        if (-1 == landColumns || oren != 2) {
            if (-1 != portColumns && oren == 1) {
                if (portColumns <= 1) {
                    fragment.mFinishActivityWhenDone = true;
                } else if (stackEntryCount > 2) {
                    fragment.mFinishActivityWhenDone = true;
                } else {
                    fragment.mFinishActivityWhenDone = false;
                }
            }
        } else if (landColumns <= 1) {
            fragment.mFinishActivityWhenDone = true;
        } else if (stackEntryCount > 2) {
            fragment.mFinishActivityWhenDone = true;
        } else {
            fragment.mFinishActivityWhenDone = false;
        }
    }

    protected void doDeleteContact(Uri contactUri) {
        if (mIsUserProfile) {
            Intent intent = new Intent("com.android.huawei.profile_exists");
            intent.putExtra("profile_exists", false);
            getActivity().sendBroadcast(intent);
        }
        this.mContext.startService(ContactSaveService.createDeleteContactIntent(this.mContext, contactUri));
        if (!isAdded() || !this.mFinishActivityWhenDone) {
            return;
        }
        if ((getActivity() instanceof PeopleActivity) || (getActivity() instanceof VoiceSearchResultActivity)) {
            getActivity().onBackPressed();
        } else {
            getActivity().finish();
        }
    }

    public static void isUserProfile(boolean aIsUserProfile) {
        mIsUserProfile = aIsUserProfile;
    }
}
