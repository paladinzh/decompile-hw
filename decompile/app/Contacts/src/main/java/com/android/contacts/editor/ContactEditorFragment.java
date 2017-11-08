package com.android.contacts.editor;

import android.accounts.Account;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.services.core.AMapException;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsApplication;
import com.android.contacts.GroupMetaDataLoader;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.activities.ContactDetailActivity.TranslucentActivity;
import com.android.contacts.activities.ContactEditorAccountsChangedActivity;
import com.android.contacts.activities.ContactEditorActivity;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.activities.ProfileSimpleCardActivity;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.datepicker.DatePickerDialog;
import com.android.contacts.detail.PhotoSelectionHandler;
import com.android.contacts.detail.PhotoSelectionHandler.PhotoActionListener;
import com.android.contacts.editor.AggregationSuggestionEngine.Suggestion;
import com.android.contacts.editor.Editor.EditorListener;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.editor.RingtoneEditorView;
import com.android.contacts.hap.optimize.OptimizationUtil;
import com.android.contacts.hap.sim.SimConfig;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.Contact;
import com.android.contacts.model.ContactBuilder;
import com.android.contacts.model.ContactLoader;
import com.android.contacts.model.RawContact;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.RawContactModifier;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.model.account.ExchangeAccountType;
import com.android.contacts.model.account.FallbackAccountType;
import com.android.contacts.model.account.GoogleAccountType;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AccountsListAdapter;
import com.android.contacts.util.AccountsListAdapter.AccountListFilter;
import com.android.contacts.util.ContactPhotoUtils;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HiCloudUtil;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.android.gms.Manifest.permission;
import com.google.android.gms.R;
import com.google.android.gms.common.Scopes;
import com.google.common.collect.ImmutableList;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.cspcommon.util.DialerHighlighter;
import com.huawei.cust.HwCustUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContactEditorFragment extends Fragment implements com.android.contacts.editor.AggregationSuggestionEngine.Listener, com.android.contacts.editor.AggregationSuggestionView.Listener, com.android.contacts.editor.RawContactReadOnlyEditorView.Listener {
    private long ANIMAION_DERATION = 200;
    private float CONTENT_TRANSLATION = 450.0f;
    boolean hasPhoto = false;
    private boolean isCamCard = false;
    private String mAction;
    private AggregationSuggestionEngine mAggregationSuggestionEngine;
    private OnItemClickListener mAggregationSuggestionItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            ((AggregationSuggestionView) view).handleItemClickEvent();
            ContactEditorFragment.this.mAggregationSuggestionPopup.dismiss();
            ContactEditorFragment.this.mAggregationSuggestionPopup = null;
            StatisticalHelper.report(2008);
        }
    };
    private ListPopupWindow mAggregationSuggestionPopup;
    private long mAggregationSuggestionsRawContactId;
    private AlertDialog mAlertDialog;
    private boolean mAutoAddToDefaultGroup;
    private Bitmap mBitmapFromDetail;
    private CancelListener mCancelListener = new CancelListener();
    private final EntityDeltaComparator mComparator = new EntityDeltaComparator();
    private BaseRawContactEditorView mContactEditor;
    private long mContactId = 0;
    private long mContactIdForJoin;
    private boolean mContactWritableForJoin;
    private FrameLayout mContent;
    private Context mContext;
    private PhotoHandler mCurrentPhotoHandler;
    private Uri mCurrentPhotoUri;
    private HwCustContactEditorFragment mCust = null;
    private CustAnimationListener mCustAnimationListener;
    private Messenger mCustomAnimMessenger;
    private final LoaderCallbacks<Contact> mDataLoaderListener = new LoaderCallbacks<Contact>() {
        public Loader<Contact> onCreateLoader(int id, Bundle args) {
            ContactEditorFragment.this.mLoaderStartTime = SystemClock.elapsedRealtime();
            if (CommonUtilMethods.isMergeFeatureEnabled()) {
                return new ContactLoader(ContactEditorFragment.this.mContext, ContactEditorFragment.this.mLookupUri, true, true);
            }
            return new ContactLoader(ContactEditorFragment.this.mContext, ContactEditorFragment.this.mLookupUri, true);
        }

        public void onLoadFinished(Loader<Contact> loader, Contact data) {
            ContactEditorFragment.this.onContactDataLoadFinished(data);
        }

        public void onLoaderReset(Loader<Contact> loader) {
        }
    };
    private ContactEditorUtils mEditorUtils;
    private boolean mEnabled = true;
    private boolean mExcludeSim;
    private boolean mExcludeSim1;
    private boolean mExcludeSim2;
    private final LoaderCallbacks<Cursor> mGroupLoaderListener = new LoaderCallbacks<Cursor>() {
        public CursorLoader onCreateLoader(int id, Bundle args) {
            return new GroupMetaDataLoader(ContactEditorFragment.this.mContext, Groups.CONTENT_URI, true);
        }

        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                ContactEditorFragment.this.mGroupMetaData = data;
                ContactEditorFragment.this.bindGroupMetaData();
            }
        }

        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };
    private Cursor mGroupMetaData;
    private Handler mHandler = new EditorHandler();
    private HwCustContactEditorCustomization mHwCustContactEditorCustomizationObj = null;
    private long mInstanceToken;
    private Bundle mIntentExtras;
    private boolean mIsActivityLeaveUserHint;
    private boolean mIsActivityStopped;
    private boolean mIsDialogShownOnce = true;
    private boolean mIsEmailExtraPresent;
    private boolean mIsFromPhoneUnderKeyguard;
    private boolean mIsNotifyLaunchPendding;
    private boolean mIsOutAnimRunning;
    private boolean mIsPhotoChange = false;
    private boolean mIsReplaceDialog = false;
    private boolean mIsRequireQuery;
    private boolean mIsSelectingAccount;
    private boolean mIsUserProfile = false;
    private Listener mListener;
    private long mLoaderStartTime;
    private Uri mLookupUri;
    private Messenger mMessenger = new Messenger(this.mHandler);
    private long mNameRawContactID = -1;
    private boolean mNewLocalProfile = false;
    private String mNewNumber = null;
    private AccountType mOldAccountType;
    private ArrayList<ValuesDelta> mPhones;
    private String mPredefineContactPath = null;
    private String mPredefineLookup = null;
    private SharedPreferences mPredefinedPrefs;
    private long mRawContactIdRequestingPhoto;
    private long mRawContactIdRequestingRingtone = -1;
    private boolean mRebindNewContact = false;
    private boolean mRequestFocus;
    private RingtoneListener mRingtoneListener;
    private View mRootView;
    private ContactEditorScrollView mScrollView;
    private BroadcastReceiver mSimAbsentReceiver = null;
    private AlertDialog mSimReplaceAlertDialog;
    private RawContactDeltaList mState;
    private int mStatus;
    private Bundle mUpdatedPhotos = new Bundle();
    private ViewIdGenerator mViewIdGenerator;
    private RawContactDeltaList mWritableList;
    private PhotoHandler photoHandler;

    public interface Listener {
        void onContactNotFound();

        void onContactSplit(Uri uri);

        void onCustomCreateContactActivityRequested(AccountWithDataSet accountWithDataSet, Bundle bundle);

        void onCustomEditContactActivityRequested(AccountWithDataSet accountWithDataSet, Uri uri, Bundle bundle, boolean z);

        void onEditOtherContactRequested(Uri uri, ArrayList<ContentValues> arrayList);

        void onReverted();

        void onSaveFinished(Intent intent);
    }

    public interface CustAnimationListener {
        void onBackToDetailAndFinish();

        void onNotifyDetailResult(int i, Intent intent);

        void onStartDetailAndFinish();

        void restoreActivityFromTransparent();

        void setActivityTransparent();
    }

    private static final class AggregationSuggestionAdapter extends BaseAdapter {
        private final Activity mActivity;
        private final com.android.contacts.editor.AggregationSuggestionView.Listener mListener;
        private final boolean mSetNewContact;
        private final List<Suggestion> mSuggestions;

        public AggregationSuggestionAdapter(Activity activity, boolean setNewContact, com.android.contacts.editor.AggregationSuggestionView.Listener listener, List<Suggestion> suggestions) {
            this.mActivity = activity;
            this.mSetNewContact = setNewContact;
            this.mListener = listener;
            this.mSuggestions = suggestions;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Suggestion suggestion = (Suggestion) getItem(position);
            AggregationSuggestionView suggestionView = (AggregationSuggestionView) this.mActivity.getLayoutInflater().inflate(R.layout.aggregation_suggestions_item, null);
            suggestionView.setNewContact(this.mSetNewContact);
            suggestionView.setListener(this.mListener);
            suggestionView.bindSuggestion(suggestion);
            return suggestionView;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public Object getItem(int position) {
            return this.mSuggestions.get(position);
        }

        public int getCount() {
            return this.mSuggestions.size();
        }
    }

    public static class CancelEditDialogFragment extends DialogFragment {
        public static void show(ContactEditorFragment fragment) {
            CancelEditDialogFragment dialog = new CancelEditDialogFragment();
            dialog.setTargetFragment(fragment, 0);
            dialog.show(fragment.getFragmentManager(), "cancelEditor");
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Builder builder = new Builder(getActivity()).setIconAttribute(16843605).setNegativeButton(getString(R.string.contact_menu_discard), new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int whichButton) {
                    ((ContactEditorFragment) CancelEditDialogFragment.this.getTargetFragment()).doRevertAction();
                    if (((ContactEditorFragment) CancelEditDialogFragment.this.getTargetFragment()).isCamCard) {
                        StatisticalHelper.report(1160);
                    }
                }
            }).setPositiveButton(R.string.description_save_button, new OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int whichButton) {
                    ((ContactEditorFragment) CancelEditDialogFragment.this.getTargetFragment()).doSaveAction();
                    if (((ContactEditorFragment) CancelEditDialogFragment.this.getTargetFragment()).isCamCard) {
                        StatisticalHelper.report(1159);
                    }
                }
            });
            builder.setMessage(R.string.save_confirmation_dialog_message);
            AlertDialog mDialog = builder.create();
            mDialog.setMessageNotScrolling();
            return mDialog;
        }

        public void onStop() {
            super.onStop();
            if (getDialog() != null) {
                getDialog().dismiss();
            }
        }
    }

    private class CancelListener implements OnClickListener, OnCancelListener {
        private CancelListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            ContactEditorFragment.this.getActivity().finish();
        }

        public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
            ContactEditorFragment.this.getActivity().finish();
        }
    }

    class DelayedRunnable implements Runnable {
        IBinder mBinder;

        public DelayedRunnable(IBinder binder) {
            this.mBinder = binder;
        }

        public void run() {
            InputMethodManager lManager = (InputMethodManager) ContactEditorFragment.this.mContext.getSystemService("input_method");
            lManager.hideSoftInputFromWindow(this.mBinder, 1);
            lManager.toggleSoftInput(1, 1);
        }
    }

    private class EditorHandler extends Handler {
        private EditorHandler() {
        }

        public void handleMessage(Message msg) {
            if (HwLog.HWFLOW) {
                HwLog.i("ContactEditorFragment", "handleMessage,what=" + msg.what + ",ag1=" + msg.arg1 + ",arg2=" + msg.arg2 + ",obj=" + msg.obj);
            }
            switch (msg.what) {
                case 101:
                    if (ContactEditorFragment.this.mContext != null) {
                        ContactEditorFragment.this.mContext.startService((Intent) msg.obj);
                    }
                    if ("android.intent.action.EDIT".equals(ContactEditorFragment.this.mAction)) {
                        StatisticalHelper.report(AMapException.CODE_AMAP_ENGINE_TABLEID_NOT_EXIST);
                        return;
                    }
                    return;
                case 1002:
                    ContactEditorFragment.this.startAnimateIn();
                    return;
                case 1005:
                    if (msg.obj != null) {
                        ContactEditorFragment.this.mCustomAnimMessenger = (Messenger) msg.obj;
                        return;
                    } else {
                        ContactEditorFragment.this.mCustomAnimMessenger = null;
                        return;
                    }
                default:
                    return;
            }
        }
    }

    private class EntityDeltaComparator implements Comparator<RawContactDelta> {
        private EntityDeltaComparator() {
        }

        public int compare(RawContactDelta one, RawContactDelta two) {
            if (one.equals(two)) {
                return 0;
            }
            AccountTypeManager accountTypes = AccountTypeManager.getInstance(ContactEditorFragment.this.mContext);
            String accountType1 = one.getValues().getAsString("account_type");
            AccountType type1 = accountTypes.getAccountType(accountType1, one.getValues().getAsString("data_set"));
            String accountType2 = two.getValues().getAsString("account_type");
            AccountType type2 = accountTypes.getAccountType(accountType2, two.getValues().getAsString("data_set"));
            if (!type1.areContactsWritable() && type2.areContactsWritable()) {
                return 1;
            }
            if (type1.areContactsWritable() && !type2.areContactsWritable()) {
                return -1;
            }
            boolean areBothPhoneAccount = false;
            boolean isPhoneAccount1 = "com.android.huawei.phone".equalsIgnoreCase(accountType1);
            boolean isPhoneAccount2 = "com.android.huawei.phone".equalsIgnoreCase(accountType2);
            if (isPhoneAccount1 && !isPhoneAccount2) {
                return -1;
            }
            if (!isPhoneAccount1 && isPhoneAccount2) {
                return 1;
            }
            int value;
            if (isPhoneAccount1 && isPhoneAccount2) {
                areBothPhoneAccount = true;
            }
            boolean areBothGoogleAccount = false;
            if (!areBothPhoneAccount) {
                boolean isGoogleAccount1 = type1 instanceof GoogleAccountType;
                boolean isGoogleAccount2 = type2 instanceof GoogleAccountType;
                if (isGoogleAccount1 && !isGoogleAccount2) {
                    return -1;
                }
                if (!isGoogleAccount1 && isGoogleAccount2) {
                    return 1;
                }
                if (isGoogleAccount1 && isGoogleAccount2) {
                    areBothGoogleAccount = true;
                }
            }
            boolean skipAccountTypeCheck = false;
            if (!(areBothPhoneAccount || areBothGoogleAccount)) {
                boolean isExchangeAccount1 = type1 instanceof ExchangeAccountType;
                boolean isExchangeAccount2 = type2 instanceof ExchangeAccountType;
                if (isExchangeAccount1 && !isExchangeAccount2) {
                    return -1;
                }
                if (!isExchangeAccount1 && isExchangeAccount2) {
                    return 1;
                }
                if (isExchangeAccount1 && isExchangeAccount2) {
                    skipAccountTypeCheck = true;
                }
            }
            if (!skipAccountTypeCheck) {
                if (type1.accountType == null && type2.accountType != null) {
                    return 1;
                }
                value = (type1.accountType != null ? type1.accountType : "").compareTo(type2.accountType != null ? type2.accountType : "");
                if (value != 0) {
                    return value;
                }
                if (type1.dataSet != null) {
                    value = type1.dataSet.compareTo(type2.dataSet != null ? type2.dataSet : "");
                    if (value != 0) {
                        return value;
                    }
                } else if (type2.dataSet != null) {
                    return 1;
                }
            }
            String oneAccount = one.getAccountName();
            if (oneAccount == null) {
                oneAccount = "";
            }
            String twoAccount = two.getAccountName();
            if (twoAccount == null) {
                twoAccount = "";
            }
            value = oneAccount.compareTo(twoAccount);
            if (value != 0) {
                return value;
            }
            Long oneId = one.getRawContactId();
            Long twoId = two.getRawContactId();
            if (oneId == null) {
                return -1;
            }
            if (twoId == null) {
                return 1;
            }
            return Long.valueOf(oneId.longValue() - twoId.longValue()).intValue();
        }
    }

    public static class JoinSuggestedContactDialogFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Builder builder = new Builder(getActivity()).setIconAttribute(16843605).setTitle(R.string.aggregation_suggestion_join_dialog_title).setPositiveButton(17039379, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    ((ContactEditorFragment) JoinSuggestedContactDialogFragment.this.getTargetFragment()).doJoinSuggestedContact(JoinSuggestedContactDialogFragment.this.getArguments().getLongArray("rawContactIds"));
                }
            }).setNegativeButton(17039369, null);
            if (!isAdded() || getActivity() == null) {
                builder.setMessage(R.string.aggregation_suggestion_join_dialog_message);
            } else {
                View view = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(R.string.aggregation_suggestion_join_dialog_message);
                builder.setView(view);
            }
            return builder.create();
        }
    }

    private class PhoneNumberSelectedListener implements OnClickListener, OnMultiChoiceClickListener {
        private int mCurrentIndex = 0;

        public void onClick(DialogInterface dialog, int which) {
            ListView alerdialogListView = ((AlertDialog) dialog).getListView();
            if (which == -1) {
                if (this.mCurrentIndex != -1) {
                    ValuesDelta newphone = (ValuesDelta) ContactEditorFragment.this.mPhones.get(this.mCurrentIndex);
                    newphone.put("data1", ContactEditorFragment.this.mNewNumber);
                    newphone.putNull("data4");
                    dialog.dismiss();
                    ContactEditorFragment.this.mIsReplaceDialog = false;
                    ContactEditorFragment.this.mIsDialogShownOnce = false;
                    ContactEditorFragment.this.mStatus = 1;
                    ContactEditorFragment.this.mRequestFocus = true;
                    ContactEditorFragment.this.bindEditors();
                    return;
                }
                dialog.dismiss();
                ContactEditorFragment.this.getActivity().finish();
            } else if (which == -2) {
                dialog.dismiss();
                ContactEditorFragment.this.getActivity().finish();
            } else {
                this.mCurrentIndex = which;
                if (!alerdialogListView.isItemChecked(which)) {
                    this.mCurrentIndex = -1;
                }
            }
        }

        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            onClick(dialog, which);
        }
    }

    private final class PhotoHandler extends PhotoSelectionHandler {
        private final BaseRawContactEditorView mEditor;
        private final PhotoActionListener mPhotoEditorListener;
        final long mRawContactId;
        private ArrayList<Long> mRawContactIdList = new ArrayList();

        private final class PhotoEditorListener extends PhotoActionListener implements EditorListener {
            private PhotoEditorListener() {
                super();
            }

            public void onRequest(int request) {
                if (ContactEditorFragment.this.hasValidState() && request == 1) {
                    ContactEditorFragment.this.hideSoftInputWindow();
                    PhotoHandler.this.onClick(PhotoHandler.this.mEditor.getPhotoEditor());
                }
            }

            public void onDeleteRequested(Editor removedEditor) {
            }

            public void onUseAsPrimaryChosen() {
                if (ContactEditorFragment.this.mContactEditor != null) {
                    ContactEditorFragment.this.mContactEditor.getPhotoEditor().setSuperPrimary();
                }
                ContactEditorFragment.this.bindEditors();
            }

            public void onRemovePictureChosen() {
                PhotoHandler.this.mEditor.setPhotoBitmap(null);
                ContactEditorFragment.this.mBitmapFromDetail = null;
                ContactEditorFragment.this.mUpdatedPhotos.clear();
                ContactEditorFragment.this.mRebindNewContact = true;
                ContactEditorFragment.this.bindEditors();
            }

            public void onPhotoSelected(Uri uri) throws FileNotFoundException {
                Bitmap bitmap = ContactPhotoUtils.getBitmapFromUri(PhotoHandler.this.mContext, uri);
                if (PhotoHandler.this.mRawContactIdList.size() > 0) {
                    ContactEditorFragment.this.setPhoto(PhotoHandler.this.mRawContactIdList, bitmap, uri, PhotoHandler.this.mEditor);
                } else {
                    ContactEditorFragment.this.setPhoto(PhotoHandler.this.mRawContactId, bitmap, uri, PhotoHandler.this.mEditor);
                }
                ContactEditorFragment.this.mCurrentPhotoHandler = null;
                ContactEditorFragment.this.mRebindNewContact = true;
                ContactEditorFragment.this.bindEditors();
            }

            public Uri getCurrentPhotoUri() {
                return ContactEditorFragment.this.mCurrentPhotoUri;
            }

            public void onPhotoSelectionDismissed() {
            }
        }

        public PhotoHandler(Context context, BaseRawContactEditorView editor, int photoMode, RawContactDeltaList state) {
            super(context, editor.getPhotoEditor(), photoMode, false, state);
            this.mEditor = editor;
            this.mRawContactId = editor.getRawContactId();
            if (!(state == null || state.isEmpty())) {
                for (int i = 0; i < state.size(); i++) {
                    this.mRawContactIdList.add(((RawContactDelta) state.get(i)).getRawContactId());
                }
            }
            this.mPhotoEditorListener = new PhotoEditorListener();
        }

        public PhotoActionListener getListener() {
            return this.mPhotoEditorListener;
        }

        public void startPhotoActivity(Intent intent, int requestCode, Uri photoUri) {
            ContactEditorFragment.this.mRawContactIdRequestingPhoto = this.mEditor.getRawContactId();
            ContactEditorFragment.this.mCurrentPhotoHandler = this;
            ContactEditorFragment.this.mStatus = 4;
            ContactEditorFragment.this.mCurrentPhotoUri = photoUri;
            ContactEditorFragment.this.startActivityForResult(intent, requestCode);
            ContactEditorFragment.this.mIsPhotoChange = true;
        }
    }

    public static class RingtoneListener implements EditorListener, OnClickListener, Serializable {
        private static final long serialVersionUID = 1;
        private String mAccountType;
        private ContactEditorFragment mContactEditorFragment;
        private RingtoneEditorView mEditor;
        private long mRawContactId;
        private boolean mReadOnly;

        public RingtoneListener(ContactEditorFragment contactEditorFragment, long rawContactId, boolean readOnly, RingtoneEditorView editor, String accountType) {
            this.mRawContactId = rawContactId;
            this.mReadOnly = readOnly;
            this.mEditor = editor;
            this.mContactEditorFragment = contactEditorFragment;
            this.mAccountType = accountType;
        }

        public void setEditor(RingtoneEditorView editor) {
            this.mEditor = editor;
        }

        public void selectRingtone() {
            this.mContactEditorFragment.doPickRingtoneAction(this.mRawContactId, this.mEditor.getRingtone(), this.mAccountType);
        }

        public void onRequest(int request) {
            if (this.mContactEditorFragment.hasValidState() && request == 6 && !this.mReadOnly) {
                if (this.mContactEditorFragment.getActivity() instanceof ContactEditorActivity) {
                    ContactEditorActivity activity = (ContactEditorActivity) this.mContactEditorFragment.getActivity();
                    if (activity.checkCallingOrSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != 0) {
                        activity.requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);
                        return;
                    }
                }
                selectRingtone();
            }
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            switch (which) {
                case 0:
                    for (RawContactDelta delta : this.mContactEditorFragment.mState) {
                        if (delta.isVisible()) {
                            ArrayList<ValuesDelta> mRingtones = delta.getMimeEntries("vnd.android.huawei.cursor.item/ringtone");
                            if (!(mRingtones == null || mRingtones.isEmpty())) {
                                ((ValuesDelta) mRingtones.get(0)).put("is_super_primary", 0);
                            }
                        }
                    }
                    this.mEditor.setSuperPrimary(true);
                    return;
                case 1:
                    this.mEditor.setRingtone(null);
                    return;
                case 2:
                    this.mContactEditorFragment.doPickRingtoneAction(this.mRawContactId, this.mEditor.getRingtone(), this.mAccountType);
                    return;
                default:
                    HwLog.w("ContactEditorFragment", "Invalid button identifier: " + which);
                    return;
            }
        }

        public void onDeleteRequested(Editor editor) {
        }
    }

    public static class SuggestionEditConfirmationDialogFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Builder builder = new Builder(getActivity()).setIconAttribute(16843605).setTitle(R.string.aggregation_suggestion_edit_dialog_title).setPositiveButton(17039379, new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    ((ContactEditorFragment) SuggestionEditConfirmationDialogFragment.this.getTargetFragment()).doEditSuggestedContact((Uri) SuggestionEditConfirmationDialogFragment.this.getArguments().getParcelable("contactUri"));
                }
            }).setNegativeButton(17039369, null);
            if (!isAdded() || getActivity() == null) {
                builder.setMessage(R.string.aggregation_edit_dialog_message_suggestion);
            } else {
                View view = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
                ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(R.string.aggregation_edit_dialog_message_suggestion);
                builder.setView(view);
            }
            return builder.create();
        }

        public void onStop() {
            super.onStop();
            if (getDialog() != null) {
                getDialog().dismiss();
            }
        }
    }

    public class ToastRunnabeImp implements Runnable {
        private int mToastTextId;

        public ToastRunnabeImp(int toastTextId) {
            this.mToastTextId = toastTextId;
        }

        public void run() {
            Toast.makeText(ContactEditorFragment.this.getActivity(), this.mToastTextId, 0).show();
        }
    }

    public void setEnabled(boolean enabled) {
        if (this.mEnabled != enabled) {
            this.mEnabled = enabled;
            if (this.mContactEditor != null) {
                this.mContactEditor.setEnabled(enabled);
            }
            Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
        this.mEditorUtils = ContactEditorUtils.getInstance(this.mContext);
        try {
            this.mCustAnimationListener = (CustAnimationListener) activity;
            if (EmuiFeatureManager.isProductCustFeatureEnable()) {
                this.mCust = (HwCustContactEditorFragment) HwCustUtils.createObj(HwCustContactEditorFragment.class, new Object[]{this.mContext});
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement CustAnimationListener");
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mAlertDialog != null && this.mAlertDialog.isShowing()) {
            this.mAlertDialog.dismiss();
            this.mAlertDialog.show();
        }
        if (this.photoHandler != null) {
            this.photoHandler.destroy();
        }
        hideCompanyPop();
        if (newConfig.orientation != 1) {
            restoreInterfaceFrommTransparent();
        }
        if (this.mAggregationSuggestionPopup != null && this.mAggregationSuggestionPopup.isShowing()) {
            this.mAggregationSuggestionPopup.dismiss();
        }
        addMargin();
    }

    public void onPause() {
        super.onPause();
        if (this.mCust != null && this.mCust.isSupportValidateDuplicate()) {
            this.mCust.setValue();
        }
        hideCompanyPop();
        if (this.mSimAbsentReceiver != null) {
            getActivity().unregisterReceiver(this.mSimAbsentReceiver);
        }
    }

    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("com.android.huawei.sim_intent_absent");
        this.mSimAbsentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (ContactEditorFragment.this.mState != null && ContactEditorFragment.this.mState.get(0) != null && CommonUtilMethods.isSimAccount(((RawContactDelta) ContactEditorFragment.this.mState.get(0)).getAccountType())) {
                    ContactEditorFragment.this.getActivity().finish();
                }
            }
        };
        getActivity().registerReceiver(this.mSimAbsentReceiver, filter, permission.HW_CONTACTS_ALL, null);
        addMargin();
    }

    public void onStop() {
        super.onStop();
        this.mIsActivityStopped = true;
        if (this.mAggregationSuggestionEngine != null) {
            this.mAggregationSuggestionEngine.quit();
        }
        if (this.mAggregationSuggestionPopup != null && this.mAggregationSuggestionPopup.isShowing()) {
            this.mAggregationSuggestionPopup.dismiss();
        }
        DatePickerDialog mDatePickerDialog = DatePickerDialog.getDatePickerDialog();
        if (mDatePickerDialog != null) {
            mDatePickerDialog.dismiss();
            DatePickerDialog.setDatePickerDialog(null);
        }
        if ((this.mCust == null || !this.mCust.isSupportValidateDuplicate()) && !getActivity().isChangingConfigurations() && this.mStatus == 1) {
            if (!(this.mIsFromPhoneUnderKeyguard || isAllFieldsEmpty())) {
                Intent intent = getActivity().getIntent();
                String str = "reload_need_clear_photo";
                boolean z = this.mUpdatedPhotos != null && this.mUpdatedPhotos.size() > 0;
                intent.putExtra(str, z);
                save(1);
                disMissDialog();
            }
            this.mIsFromPhoneUnderKeyguard = false;
        }
        this.mIsActivityLeaveUserHint = false;
    }

    public void onDestroy() {
        super.onDestroy();
        Editor lEditor = this.mPredefinedPrefs.edit();
        lEditor.putBoolean("need_load_animation", true);
        lEditor.commit();
        if (this.mContactEditor instanceof RawContactEditorView) {
            this.mContactEditor.removeHandlerCallback();
        }
        if (!getActivity().isChangingConfigurations()) {
            this.mEditorUtils.setExcludeSim(false);
            this.mEditorUtils.setExcludeSim1(false);
            this.mEditorUtils.setExcludeSim2(false);
        }
        if (this.mSimReplaceAlertDialog != null) {
            this.mSimReplaceAlertDialog.dismiss();
        }
        disMissDialog();
        if (this.mCustomAnimMessenger != null) {
            try {
                this.mCustomAnimMessenger.send(Message.obtain(null, 1005, null));
            } catch (RemoteException e) {
                HwLog.e("ContactEditorFragment", "onDestory,send message to remote handler faild");
            }
            this.mCustomAnimMessenger = null;
        }
        if (this.mAlertDialog != null && this.mAlertDialog.isShowing()) {
            this.mAlertDialog.dismiss();
        }
        hideCompanyPop();
    }

    private void disMissDialog() {
        if (this.mContactEditor instanceof RawContactEditorView) {
            RawContactEditorView editorView = this.mContactEditor;
            if (editorView.dialog != null && editorView.dialog.isShowing()) {
                editorView.dialog.dismiss();
            }
        }
        if (this.photoHandler != null) {
            this.photoHandler.destroy();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        PLog.d(0, "ContactEditorFragment onCreateView begin");
        View view = inflater.inflate(R.layout.contact_editor_fragment, container, false);
        if (this.mCust != null && this.mCust.isSupportValidateDuplicate()) {
            this.mCust.setNewContactFragment(this);
        }
        this.mContent = (FrameLayout) view.findViewById(R.id.editors);
        setHasOptionsMenu(true);
        if (this.mState != null) {
            bindEditors();
        } else {
            this.mIsRequireQuery = handleContactsFromDetail();
        }
        this.mRootView = view;
        PLog.d(0, "ContactEditorFragment onCreateView end");
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if ("android.intent.action.EDIT".equals(this.mAction)) {
            if (!this.mIsRequireQuery) {
                getLoaderManager().initLoader(1, new Bundle(), this.mDataLoaderListener);
            }
        } else if ("android.intent.action.INSERT".equals(this.mAction)) {
            if (OptimizationUtil.isLoadCompoentDelayedOnInsertContact()) {
                handleInsertActionDelayed();
            } else {
                handleInsertAction();
            }
        } else if (!"saveCompleted".equals(this.mAction)) {
            if (HwLog.HWFLOW) {
                HwLog.i("ContactEditorFragment", "Unknown Action String " + this.mAction + ". Only support " + "android.intent.action.EDIT" + " or " + "android.intent.action.INSERT");
            }
            getActivity().finish();
        }
        if (!(savedInstanceState == null || savedInstanceState.getSparseParcelableArray("android:view_state") == null || this.mRootView == null)) {
            checkView(this.mRootView);
        }
        boolean needLoadAnimation = this.mPredefinedPrefs.getBoolean("need_load_animation", true);
        if (needLoadAnimation && iSfromPressureOfIntent()) {
            needLoadAnimation = false;
        }
        if (EmuiFeatureManager.isUseCustAnimation() && this.mCustomAnimMessenger != null && !CommonUtilMethods.isSimplifiedModeEnabled() && getResources().getConfiguration().orientation == 1 && r2 && savedInstanceState == null) {
            Editor lEditor = this.mPredefinedPrefs.edit();
            lEditor.putBoolean("need_load_animation", false);
            lEditor.commit();
            setInterfaceTransparent();
            if (HwLog.HWFLOW) {
                HwLog.i("ContactEditorFragment", "onActivityCreated,should use custom animation,set window transparent");
                return;
            }
            return;
        }
        restoreInterfaceFrommTransparent();
        if (HwLog.HWFLOW) {
            HwLog.i("ContactEditorFragment", "onActivityCreated,NOT use custom animation,restore window from transparent");
        }
    }

    private void handleInsertActionDelayed() {
        OptimizationUtil.postTaskToRunAferActivitylaunched(getActivity(), this.mHandler, new Runnable() {
            public void run() {
                ContactEditorFragment.this.handleInsertAction();
            }
        });
    }

    private void handleInsertAction() {
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
        if (this.mIntentExtras != null) {
            this.mPredefineContactPath = this.mIntentExtras.getString("predefinePhoto");
            this.mPredefineLookup = this.mIntentExtras.getString("predefinelookup");
        }
        if (account != null) {
            createContact(new AccountWithDataSet(account.name, account.type, str));
        } else {
            selectAccountAndCreateContact();
        }
    }

    private void checkView(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int i = 0;
            while (i < group.getChildCount()) {
                View v = group.getChildAt(i);
                if (v == null) {
                    int i2 = i - 1;
                    group.removeViewAt(i);
                    HwLog.w("ContactEditorFragment", "Remove null view! container id = " + group.getId());
                    i = i2;
                } else {
                    checkView(v);
                }
                i++;
            }
        }
    }

    public void onStart() {
        this.mIsActivityStopped = false;
        getLoaderManager().initLoader(2, new Bundle(), this.mGroupLoaderListener);
        super.onStart();
    }

    public void load(String action, Uri lookupUri, Bundle intentExtras) {
        boolean containsKey;
        boolean z = false;
        this.mAction = action;
        this.mLookupUri = lookupUri;
        this.mIntentExtras = intentExtras;
        if (this.mIntentExtras != null) {
            containsKey = this.mIntentExtras.containsKey(Scopes.EMAIL);
        } else {
            containsKey = false;
        }
        this.mIsEmailExtraPresent = containsKey;
        if (this.mIntentExtras != null) {
            containsKey = this.mIntentExtras.containsKey("addToDefaultDirectory");
        } else {
            containsKey = false;
        }
        this.mAutoAddToDefaultGroup = containsKey;
        if (this.mIntentExtras != null) {
            z = this.mIntentExtras.getBoolean("newLocalProfile");
        }
        this.mNewLocalProfile = z;
    }

    public void setListener(Listener value) {
        this.mListener = value;
    }

    public void onCreate(Bundle savedState) {
        Intent intent = getActivity().getIntent();
        if (savedState != null) {
            this.mLookupUri = (Uri) savedState.getParcelable("uri");
            this.mAction = savedState.getString("action");
        }
        super.onCreate(savedState);
        if (savedState == null) {
            this.mViewIdGenerator = new ViewIdGenerator();
        } else {
            this.mState = (RawContactDeltaList) savedState.getParcelable("state");
            this.mRawContactIdRequestingPhoto = savedState.getLong("photorequester");
            this.mRawContactIdRequestingRingtone = savedState.getLong("ringtonerequester");
            this.mViewIdGenerator = (ViewIdGenerator) savedState.getParcelable("viewidgenerator");
            this.mCurrentPhotoUri = (Uri) savedState.getParcelable("currentphotouri");
            this.mContactIdForJoin = savedState.getLong("contactidforjoin");
            this.mContactWritableForJoin = savedState.getBoolean("contactwritableforjoin");
            this.mAggregationSuggestionsRawContactId = savedState.getLong("showJoinSuggestions");
            this.mEnabled = savedState.getBoolean("enabled");
            this.mStatus = savedState.getInt("status");
            this.mNewLocalProfile = savedState.getBoolean("newLocalProfile");
            this.mIsUserProfile = savedState.getBoolean("isUserProfile");
            this.mUpdatedPhotos = (Bundle) savedState.getParcelable("updatedPhotos");
            if (intent.getBooleanExtra("reload_need_clear_photo", false) && this.mUpdatedPhotos != null) {
                this.mUpdatedPhotos.clear();
            }
            this.mIsReplaceDialog = savedState.getBoolean("simNumberIsReplaceDialog");
            this.mIsDialogShownOnce = savedState.getBoolean("simDialogIsShownOnce");
        }
        this.mPredefinedPrefs = SharePreferenceUtil.getDefaultSp_de(this.mContext);
        this.isCamCard = intent.getBooleanExtra("key_from_camcard", false);
        this.mIsFromPhoneUnderKeyguard = intent.getBooleanExtra("FromPhoneUnderKeyguard", false);
        this.mInstanceToken = System.currentTimeMillis();
        if (EmuiFeatureManager.isUseCustAnimation()) {
            this.mCustomAnimMessenger = (Messenger) intent.getParcelableExtra("intent_extra_messenger");
        }
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mHwCustContactEditorCustomizationObj = (HwCustContactEditorCustomization) HwCustUtils.createObj(HwCustContactEditorCustomization.class, new Object[0]);
        }
        if (!HiCloudUtil.isHiCloudAccountHasRead() && !HiCloudUtil.isHiCloudAccountLogOn()) {
            ContactsThreadPool.getInstance().execute(new Runnable() {
                public void run() {
                    HiCloudUtil.getHicloudAccountState(ContactEditorFragment.this.mContext);
                }
            });
        }
    }

    public void setData(Contact data) {
        if (this.mState != null) {
            HwLog.v("ContactEditorFragment", "Ignoring background change. This will have to be rebased later");
            return;
        }
        ImmutableList<RawContact> rawContacts = data.getRawContacts();
        if (rawContacts.size() == 1) {
            RawContact rawContact = (RawContact) rawContacts.get(0);
            String type = rawContact.getAccountTypeString();
            String dataSet = rawContact.getDataSet();
            AccountType accountType = rawContact.getAccountType(this.mContext);
            if (!(accountType.getEditContactActivityClassName() == null || accountType.areContactsWritable())) {
                if (this.mListener != null) {
                    this.mListener.onCustomEditContactActivityRequested(new AccountWithDataSet(rawContact.getAccountName(), type, dataSet), ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContact.getId().longValue()), this.mIntentExtras, true);
                }
                return;
            }
        }
        bindEditorsForExistingContact(data);
    }

    public void onExternalEditorRequest(AccountWithDataSet account, Uri uri) {
        this.mListener.onCustomEditContactActivityRequested(account, uri, null, false);
    }

    private void bindEditorsForExistingContact(Contact contact) {
        setEnabled(true);
        this.mState = contact.createRawContactDeltaList();
        if (this.mState != null) {
            this.mContactId = contact.getId();
            this.mNameRawContactID = contact.getNameRawContactId();
            checkSimState(this.mState);
            setIntentExtras(this.mIntentExtras);
            this.mIntentExtras = null;
            this.mIsUserProfile = contact.isUserProfile();
            boolean localProfileExists = false;
            if (this.mIsUserProfile) {
                for (RawContactDelta state : this.mState) {
                    state.setProfileQueryUri();
                    if (state.getValues().getAsString("account_type") == null) {
                        localProfileExists = true;
                    }
                }
                if (!localProfileExists) {
                    RawContact rawContact = new RawContact();
                    rawContact.setAccountToLocal();
                    RawContactDelta insert = new RawContactDelta(ValuesDelta.fromAfter(rawContact.getValues()));
                    insert.setProfileQueryUri();
                    this.mState.add(insert);
                }
            }
            Intent lIntent = getActivity().getIntent();
            if (lIntent != null) {
                this.mNewNumber = lIntent.getStringExtra("phone");
                if (TextUtils.isEmpty(this.mNewNumber)) {
                    CharSequence tempNumber = lIntent.getCharSequenceExtra("phone");
                    this.mNewNumber = TextUtils.isEmpty(tempNumber) ? null : tempNumber.toString();
                }
            }
            if (this.mNewNumber != null && "android.intent.action.EDIT".equals(this.mAction)) {
                RawContactDelta rawContactDelta = (RawContactDelta) this.mState.get(0);
                AccountType type = rawContactDelta.getAccountType(AccountTypeManager.getInstance(this.mContext));
                if (CommonUtilMethods.isSimAccount(rawContactDelta.getAccountType())) {
                    saveNewNumberToSimAccount(rawContactDelta, type);
                }
            }
            this.mRequestFocus = true;
            bindEditors();
        }
    }

    private void checkSimState(RawContactDeltaList state) {
        String accountType = ((RawContactDelta) this.mState.get(0)).getAccountType();
        if (isAdded() && CommonUtilMethods.isSimAccount(accountType) && (AccountTypeManager.getInstance(this.mContext).getAccountType(accountType, null) instanceof FallbackAccountType)) {
            doRevertAction();
            Toast.makeText(getActivity(), String.format(getString(R.string.sim_not_ready), new Object[]{SimFactoryManager.getSimCardDisplayLabel(accountType)}), 0).show();
            if (HwLog.HWFLOW) {
                HwLog.i("ContactEditorFragment", "sim account not ready for: " + accountType);
            }
            getActivity().finish();
        }
    }

    private void saveNewNumberToSimAccount(RawContactDelta rawContactDelta, AccountType type) {
        boolean isFull = false;
        this.mPhones = rawContactDelta.getMimeEntries("vnd.android.cursor.item/phone_v2");
        if (this.mPhones != null) {
            ValuesDelta phone;
            if (EmuiFeatureManager.getEmailAnrSupport()) {
                DataKind kind = type.getKindForMimetype("vnd.android.cursor.item/phone_v2");
                if (kind != null && kind.typeOverallMax > 0 && kind.typeOverallMax == this.mPhones.size()) {
                    isFull = true;
                }
                phone = (ValuesDelta) this.mPhones.get(this.mPhones.size() - 1);
            } else {
                phone = (ValuesDelta) this.mPhones.get(0);
            }
            if (phone != null) {
                String oldNumber = phone.getAsString("data1");
                if (this.mIsReplaceDialog || !(oldNumber == null || DialerHighlighter.cleanNumber(oldNumber, false).equalsIgnoreCase(DialerHighlighter.cleanNumber(this.mNewNumber, false)) || !this.mIsDialogShownOnce)) {
                    if (!EmuiFeatureManager.getEmailAnrSupport()) {
                        phone.put("data1", this.mNewNumber);
                        phone.putNull("data4");
                    } else if (!isFull) {
                        ValuesDelta newphone = (ValuesDelta) this.mPhones.get(this.mPhones.size() - 1);
                        newphone.put("data1", this.mNewNumber);
                        newphone.putNull("data4");
                    }
                    if (!EmuiFeatureManager.getEmailAnrSupport()) {
                        this.mIsReplaceDialog = true;
                        this.mStatus = 0;
                        showSimReplaceConfirmation();
                    } else if (isFull) {
                        this.mIsReplaceDialog = true;
                        this.mStatus = 0;
                        PhoneNumberSelectedListener listener = new PhoneNumberSelectedListener();
                        int size = this.mPhones.size();
                        CharSequence[] items = new CharSequence[size];
                        for (int index = 0; index < size; index++) {
                            items[index] = "" + ((ValuesDelta) this.mPhones.get(index)).getAsString("data1");
                        }
                        Builder builder = new Builder(getActivity()).setTitle(R.string.contact_editor_overwrite_number_title).setPositiveButton(17039370, listener).setOnCancelListener(this.mCancelListener).setNegativeButton(17039360, this.mCancelListener);
                        builder.setSingleChoiceItems(items, 0, listener);
                        builder.create().show();
                    } else {
                        this.mIsDialogShownOnce = false;
                    }
                }
            }
        }
    }

    public void setIntentExtras(Bundle extras) {
        if (extras != null && extras.size() != 0) {
            AccountTypeManager accountTypes = AccountTypeManager.getInstance(this.mContext);
            if (this.mState != null) {
                for (RawContactDelta state : this.mState) {
                    AccountType type = state.getAccountType(accountTypes);
                    if (type.areContactsWritable()) {
                        RawContactModifier.parseExtras(this.mContext, type, state, extras);
                    }
                }
            }
        }
    }

    private void selectAccountAndCreateContact() {
        if (this.mNewLocalProfile) {
            createContact(null);
            return;
        }
        this.mEditorUtils.setExcludeSim(this.mExcludeSim);
        this.mEditorUtils.setExcludeSim1(this.mExcludeSim1);
        this.mEditorUtils.setExcludeSim2(this.mExcludeSim2);
        int lDefaultAccFromPref = this.mPredefinedPrefs.getInt("contact_default_account", -1);
        AccountWithDataSet lDefaultAccount = null;
        if (lDefaultAccFromPref != -1) {
            boolean isSim1Present = SimFactoryManager.hasIccCard(0);
            boolean isSim2Present = SimFactoryManager.hasIccCard(1);
            if (isSim1Present || !isSim2Present) {
                lDefaultAccount = this.mEditorUtils.getPredefinedDefaultAccount(lDefaultAccFromPref);
            } else {
                lDefaultAccount = this.mEditorUtils.getPredefinedDefaultAccount(2);
            }
        }
        if (lDefaultAccount == null) {
            if (this.mHwCustContactEditorCustomizationObj != null) {
                this.mHwCustContactEditorCustomizationObj.customizeDefaultAccount(this.mEditorUtils, this.mContext);
            }
            if (!this.mEditorUtils.shouldShowAccountChangedNotification()) {
                AccountWithDataSet defaultAccount = this.mEditorUtils.getDefaultAccount();
                if (defaultAccount == null) {
                    createContact(getDefaultLocalAccount());
                } else if (!this.mIsEmailExtraPresent || CommonUtilMethods.isGroupEmailSupported(defaultAccount.type, defaultAccount.dataSet, this.mContext)) {
                    createContact(defaultAccount);
                } else {
                    if (HwLog.HWFLOW) {
                        HwLog.i("ContactEditorFragment", "default account is null, show showAccountSelectingActivity position 2");
                    }
                    showAccountSelectingActivity();
                }
            } else if (!isOnlyLocalAccountPresent(this.mExcludeSim, this.mExcludeSim1, this.mExcludeSim2)) {
                if (HwLog.HWFLOW) {
                    HwLog.i("ContactEditorFragment", "default account is null, show showAccountSelectingActivity position 1");
                }
                showAccountSelectingActivity();
            }
        } else if (this.mIsEmailExtraPresent && !CommonUtilMethods.isGroupEmailSupported(lDefaultAccount.type, lDefaultAccount.dataSet, this.mContext)) {
            if (HwLog.HWFLOW) {
                HwLog.i("ContactEditorFragment", "default account not null,email account present,show showAccountSelectingActivity");
            }
            showAccountSelectingActivity();
        } else if (this.mEditorUtils.isValidAccount(lDefaultAccount)) {
            createContact(lDefaultAccount);
        } else {
            createContact(getDefaultLocalAccount());
        }
    }

    private void showAccountSelectingActivity() {
        Intent intent = new Intent(this.mContext, ContactEditorAccountsChangedActivity.class);
        this.mStatus = 4;
        startActivityForResult(intent, 1);
        this.mIsSelectingAccount = true;
    }

    private boolean isOnlyLocalAccountPresent(boolean excludeSim, boolean excludeSim1, boolean excludeSim2) {
        List<AccountWithDataSet> accounts;
        AccountTypeManager accountTypes = AccountTypeManager.getInstance(this.mContext);
        if (excludeSim) {
            accounts = accountTypes.getAccountsExcludeSim(true);
        } else if (excludeSim1 && !excludeSim2) {
            accounts = accountTypes.getAccountsExcludeSim1(true);
        } else if (excludeSim2 && !excludeSim1) {
            accounts = accountTypes.getAccountsExcludeSim2(true);
        } else if (excludeSim2 && excludeSim1) {
            accounts = accountTypes.getAccountsExcludeBothSim(true);
        } else {
            accounts = AccountTypeManager.getInstance(this.mContext).getAccounts(true);
        }
        int numAccounts = accounts.size();
        if (1 == numAccounts) {
            AccountWithDataSet account = (AccountWithDataSet) accounts.get(0);
            if ("com.android.huawei.phone".equalsIgnoreCase(account.type)) {
                this.mEditorUtils.saveDefaultAndAllAccounts(account);
                createContact(account);
                return true;
            }
        } else if (numAccounts < 1) {
            accountTypes.hiCloudServiceLogOnOff();
            HwLog.e("ContactEditorFragment", "fatal error! current accounts count=" + numAccounts);
        }
        return false;
    }

    private AccountWithDataSet getDefaultLocalAccount() {
        return new AccountWithDataSet("Phone", "com.android.huawei.phone", null);
    }

    private void createContact() {
        List<AccountWithDataSet> accounts = AccountTypeManager.getInstance(this.mContext).getAccounts(true);
        if (accounts.isEmpty()) {
            createContact(null);
        } else {
            createContact((AccountWithDataSet) accounts.get(0));
        }
    }

    private void createContact(AccountWithDataSet account) {
        String str;
        String str2 = null;
        AccountTypeManager accountTypes = AccountTypeManager.getInstance(this.mContext);
        if (account != null) {
            str = account.type;
        } else {
            str = null;
        }
        if (account != null) {
            str2 = account.dataSet;
        }
        AccountType accountType = accountTypes.getAccountType(str, str2);
        if (accountType.getCreateContactActivityClassName() == null) {
            bindEditorsForNewContact(account, accountType);
            if (isAdded()) {
                getActivity().getIntent().putExtra("ViewDelayedLoadingSwitch", false);
            }
        } else if (this.mListener != null) {
            this.mListener.onCustomCreateContactActivityRequested(account, this.mIntentExtras);
        }
    }

    private void rebindEditorsForNewContact(RawContactDelta oldState, AccountWithDataSet oldAccount, AccountWithDataSet newAccount) {
        this.mRebindNewContact = true;
        AccountTypeManager accountTypes = AccountTypeManager.getInstance(this.mContext);
        AccountType oldAccountType = accountTypes.getAccountType(oldAccount.type, oldAccount.dataSet);
        AccountType newAccountType = accountTypes.getAccountType(newAccount.type, newAccount.dataSet);
        if (newAccountType.getCreateContactActivityClassName() != null) {
            HwLog.w("ContactEditorFragment", "external activity called in rebind situation");
            if (this.mListener != null) {
                this.mListener.onCustomCreateContactActivityRequested(newAccount, this.mIntentExtras);
                return;
            }
            return;
        }
        this.mState = null;
        bindEditorsForNewContact(newAccount, newAccountType, oldState, oldAccountType);
    }

    private void bindEditorsForNewContact(AccountWithDataSet account, AccountType accountType) {
        bindEditorsForNewContact(account, accountType, null, null);
    }

    public AccountType getOldAccountType() {
        return this.mOldAccountType;
    }

    private void bindEditorsForNewContact(AccountWithDataSet newAccount, AccountType newAccountType, RawContactDelta oldState, AccountType oldAccountType) {
        PLog.d(0, "ContactEditorFragment bindEditorsForNewContact begin");
        this.mStatus = 1;
        this.mOldAccountType = oldAccountType;
        RawContact rawContact = new RawContact();
        if (newAccount != null) {
            rawContact.setAccount(newAccount);
        } else {
            rawContact.setAccountToLocal();
        }
        RawContactDelta insert = new RawContactDelta(ValuesDelta.fromAfter(rawContact.getValues()));
        if (oldState == null) {
            RawContactModifier.parseExtras(this.mContext, newAccountType, insert, this.mIntentExtras);
        } else {
            RawContactModifier.migrateStateForNewContact(this.mContext, oldState, insert, oldAccountType, newAccountType);
        }
        if (this.mNewLocalProfile) {
            insert.setProfileQueryUri();
        }
        if (this.mState == null) {
            this.mState = RawContactDeltaList.fromSingle(insert);
        } else {
            this.mState.add(insert);
        }
        if (HwLog.HWDBG) {
            HwLog.d("ContactEditorFragment", "isCamCard = " + this.isCamCard);
        }
        if (this.isCamCard) {
            Uri uri = (Uri) getActivity().getIntent().getParcelableExtra("image_uri");
            if (uri != null) {
                this.mUpdatedPhotos.putParcelable(String.valueOf(((RawContactDelta) this.mState.get(0)).getRawContactId().longValue()), uri);
            }
        }
        this.mRequestFocus = true;
        bindEditors();
        PLog.d(15, "ContactEditorFragment bindEditorsForNewContact end");
    }

    private void bindEditors() {
        Context lActivityRef = getActivity();
        if (this.mState != null && !this.mState.isEmpty() && this.mComparator != null && lActivityRef != null) {
            long rawContactId;
            int numRawContacts = this.mState.size();
            if (numRawContacts > 1) {
                Collections.sort(this.mState, this.mComparator);
            }
            Intent intent = lActivityRef.getIntent();
            this.mContent.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
            AccountTypeManager accountTypes = AccountTypeManager.getInstance(this.mContext);
            boolean lIsCreateNewContact = "android.intent.action.INSERT".equals(this.mAction);
            this.mWritableList = new RawContactDeltaList();
            RawContactDeltaList groupEditableList = new RawContactDeltaList();
            if (lIsCreateNewContact) {
                this.mWritableList.add((RawContactDelta) this.mState.get(0));
            } else {
                RawContactDeltaList<RawContactDelta> invisibleList = new RawContactDeltaList();
                for (int index = 0; index < numRawContacts; index++) {
                    RawContactDelta rawContactDelta = (RawContactDelta) this.mState.get(index);
                    if (rawContactDelta.isVisible()) {
                        boolean isProfileEditable;
                        rawContactId = rawContactDelta.getRawContactId().longValue();
                        AccountType current_type = rawContactDelta.getAccountType(accountTypes);
                        if (ContactsContract.isProfileId(rawContactId)) {
                            isProfileEditable = current_type.isProfileEditable();
                        } else {
                            isProfileEditable = false;
                        }
                        if (!current_type.areContactsWritable() ? isProfileEditable : true) {
                            this.mWritableList.add(rawContactDelta);
                        }
                        if (current_type.isGroupMembershipEditable()) {
                            groupEditableList.add(rawContactDelta);
                        }
                    } else {
                        invisibleList.add(rawContactDelta);
                    }
                }
                if (!invisibleList.isEmpty()) {
                    for (RawContactDelta rawContactState : invisibleList) {
                        this.mState.remove(rawContactState);
                    }
                }
                if (this.mState.isEmpty()) {
                    return;
                }
            }
            RawContactDelta bestState = (RawContactDelta) this.mState.get(0);
            if (this.mCust != null) {
                bestState = this.mCust.getBestState(this.mState, bestState, this.mNameRawContactID);
            }
            AccountType type = bestState.getAccountType(accountTypes);
            String accountType = bestState.getAccountType();
            rawContactId = bestState.getRawContactId().longValue();
            boolean lIsContactReadOnly = this.mWritableList.isEmpty();
            initActionBar(lIsContactReadOnly);
            if (lIsContactReadOnly) {
                this.mContactEditor = (RawContactReadOnlyEditorView) inflater.inflate(R.layout.raw_contact_readonly_editor_view, this.mContent, false);
                ((RawContactReadOnlyEditorView) this.mContactEditor).setListener(this);
            } else {
                if (lIsCreateNewContact) {
                    this.mContactEditor = (RawContactEditorView) inflater.inflate(R.layout.raw_contact_editor_view_new, this.mContent, false);
                } else {
                    this.mContactEditor = (RawContactEditorView) inflater.inflate(R.layout.raw_contact_editor_view, this.mContent, false);
                }
                ((RawContactEditorView) this.mContactEditor).setFragment(this);
                ((RawContactEditorView) this.mContactEditor).setIntent(intent);
                ((RawContactEditorView) this.mContactEditor).setWriteabeList(this.mWritableList);
                ((RawContactEditorView) this.mContactEditor).setGroupEditableList(groupEditableList);
            }
            if (!this.mRebindNewContact) {
                this.mContactEditor.setBitmapFromDetail(this.mBitmapFromDetail);
            }
            this.mContactEditor.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    ContactEditorFragment.this.hideCompanyPop();
                    return false;
                }
            });
            this.mContactEditor.setCurrentActivity(lActivityRef);
            PhotoEditorView photoEditor = this.mContactEditor.getPhotoEditor();
            boolean isProfile = isEditingUserProfile();
            if (photoEditor != null) {
                photoEditor.setContactId(this.mContactId);
            }
            if (!isProfile) {
                this.mContactEditor.setAccountsList(this.mState);
            }
            this.mScrollView = (ContactEditorScrollView) this.mContactEditor.getScrollView();
            this.mScrollView.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    ContactEditorFragment.this.hideCompanyPop();
                    return false;
                }
            });
            if (!lIsContactReadOnly) {
                if (!lIsCreateNewContact || this.mNewLocalProfile) {
                    disableAccountSwitcher(((RawContactEditorView) this.mContactEditor).getTitleContainer());
                } else if (this.mEditorUtils.getWritableAccounts().size() > 1) {
                    addAccountSwitcher((RawContactDelta) this.mState.get(0), this.mContactEditor);
                } else {
                    disableAccountSwitcher(((RawContactEditorView) this.mContactEditor).getTitleContainer());
                }
            }
            this.mContactEditor.setEnabled(this.mEnabled);
            if (CommonUtilMethods.isSimAccount(accountType)) {
                this.mContactEditor.findViewById(R.id.edit_photo).setVisibility(8);
            }
            this.mContent.addView(this.mContactEditor);
            this.mContactEditor.setState(this.mState, bestState, type, this.mViewIdGenerator, isEditingUserProfile());
            updatedBitmapForRawContact(this.mContactEditor, this.mWritableList, accountType, rawContactId);
            bindPhotoHandler(this.mContactEditor, type, this.mWritableList, false);
            if (this.mContactEditor instanceof RawContactEditorView) {
                RawContactEditorView rawContactEditor = (RawContactEditorView) this.mContactEditor;
                final Context context = lActivityRef;
                final RawContactEditorView rawContactEditorView = rawContactEditor;
                EditorListener anonymousClass9 = new EditorListener() {
                    public void onRequest(int request) {
                        if (!(context.isFinishing() || request != 2 || ContactEditorFragment.this.isEditingUserProfile())) {
                            ContactEditorFragment.this.acquireAggregationSuggestions(context, rawContactEditorView);
                        }
                    }

                    public void onDeleteRequested(Editor removedEditor) {
                    }
                };
                TextFieldsEditorView nameEditor = rawContactEditor.getNameEditor();
                if (this.mRequestFocus) {
                    nameEditor.requestFocus();
                    this.mRequestFocus = false;
                    EditText firstEditField = nameEditor.getFirstEditField();
                    if ("android.intent.action.INSERT".equals(this.mAction) && firstEditField != null) {
                        InputMethodManager lManager = (InputMethodManager) this.mContext.getSystemService("input_method");
                        if (iSfromPressureOfIntent()) {
                            StatisticalHelper.report(1176);
                            new Handler().postDelayed(new DelayedRunnable(firstEditField.getWindowToken()), 200);
                        } else {
                            lManager.hideSoftInputFromWindow(firstEditField.getWindowToken(), 1);
                            lManager.toggleSoftInput(1, 1);
                        }
                    }
                }
                nameEditor.setEditorListener(anonymousClass9);
                rawContactEditor.getPhoneticNameEditor().setEditorListener(anonymousClass9);
                rawContactEditor.setAutoAddToDefaultGroup(this.mAutoAddToDefaultGroup);
                if (rawContactId == this.mAggregationSuggestionsRawContactId) {
                    acquireAggregationSuggestions(lActivityRef, rawContactEditor);
                }
            }
            addMargin();
            PLog.d(1004, "ContactEditorFragment.Edit contact end for jlog.");
        }
    }

    private boolean iSfromPressureOfIntent() {
        Intent quickActionIntent = null;
        if (getActivity() != null) {
            quickActionIntent = getActivity().getIntent();
        }
        if (quickActionIntent != null) {
            return quickActionIntent.getBooleanExtra("from_to_quickaction", false);
        }
        return false;
    }

    public void onAllItemDone(RawContactEditorView editor, long rawContactId, AccountType type) {
        RingtoneEditorView ringtoneEditor = editor.getRingtoneEditor();
        if (ringtoneEditor != null) {
            ringtoneEditor.setEditorListener(new RingtoneListener(this, rawContactId, !type.areContactsWritable(), ringtoneEditor, type.accountType));
        } else {
            this.mRingtoneListener = new RingtoneListener(this, rawContactId, !type.areContactsWritable(), null, null);
        }
        if (!CommonUtilMethods.isSimplifiedModeEnabled() && this.mGroupMetaData != null) {
            editor.setGroupMetaData(this.mGroupMetaData);
        }
    }

    public void showSimReplaceConfirmation() {
        Builder alertDialog = new Builder(getActivity());
        alertDialog.setTitle(R.string.editContactDescription);
        alertDialog.setPositiveButton(getActivity().getString(17039379), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ContactEditorFragment.this.mIsReplaceDialog = false;
                ContactEditorFragment.this.mIsDialogShownOnce = false;
                ContactEditorFragment.this.mStatus = 1;
            }
        });
        alertDialog.setNegativeButton(getActivity().getString(17039369), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ContactEditorFragment.this.doRevertAction();
                dialog.dismiss();
                ContactEditorFragment.this.getActivity().finish();
            }
        });
        alertDialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                ContactEditorFragment.this.doRevertAction();
                dialog.dismiss();
                ContactEditorFragment.this.getActivity().finish();
            }
        });
        if (!isAdded() || getActivity() == null) {
            alertDialog.setMessage(getActivity().getString(R.string.str_sim_redit_replace_confirmation));
        } else {
            View view = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
            ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(getActivity().getString(R.string.str_sim_redit_replace_confirmation));
            alertDialog.setView(view);
        }
        this.mSimReplaceAlertDialog = alertDialog.create();
        alertDialog.show();
    }

    private void updatedBitmapForRawContact(BaseRawContactEditorView editor, RawContactDeltaList list, String accountType, long rawContactId) {
        Bitmap bitMap;
        if (!this.mIsPhotoChange && this.mPredefineContactPath != null && !CommonUtilMethods.isSimAccount(accountType) && "-1".equals(this.mPredefineLookup) && rawContactId < 0) {
            bitMap = BitmapFactory.decodeFile(this.mPredefineContactPath.split("//")[2]);
            if (bitMap != null) {
                editor.setPhotoBitmap(bitMap);
            }
        } else if (!this.mUpdatedPhotos.isEmpty() && !list.isEmpty()) {
            for (RawContactDelta writeState : list) {
                Uri photoUri = (Uri) this.mUpdatedPhotos.getParcelable(String.valueOf(writeState.getRawContactId()));
                if (photoUri != null) {
                    String path = ContactPhotoUtils.pathForTempPhoto(this.mContext, photoUri.getLastPathSegment());
                    if (this.isCamCard && photoUri.toString().contains("contactscamcard")) {
                        path = path.toString().replace("com.android.contacts", "com.huawei.contactscamcard");
                    }
                    bitMap = BitmapFactory.decodeFile(path);
                    if (bitMap != null) {
                        editor.setPhotoBitmap(bitMap);
                        return;
                    }
                    List<String> segments = photoUri.getPathSegments();
                    if (segments.size() - 2 > 0) {
                        String tmpPath = (String) segments.get(segments.size() - 2);
                        if (tmpPath.startsWith("CCardPhoto-")) {
                            Bitmap bitMap2 = BitmapFactory.decodeFile(ContactPhotoUtils.pathForTempPhoto(this.mContext, tmpPath, photoUri.getLastPathSegment()));
                            if (bitMap2 != null) {
                                editor.setPhotoBitmap(bitMap2);
                                return;
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
    }

    private void bindPhotoHandler(BaseRawContactEditorView editor, AccountType type, RawContactDeltaList state, boolean aIsPartialMerge) {
        int mode;
        if (type.areContactsWritable()) {
            if (editor.hasSetPhoto()) {
                mode = 14;
            } else {
                mode = 4;
            }
        } else if (editor.hasSetPhoto() && hasMoreThanOnePhoto()) {
            mode = 1;
        } else {
            editor.getPhotoEditor().setEditorListener(null);
            return;
        }
        this.photoHandler = new PhotoHandler(this.mContext, editor, mode, state);
        if (CommonUtilMethods.isSimAccount(type.accountType)) {
            this.mUpdatedPhotos.clear();
        }
        editor.getPhotoEditor().setEditorListener((PhotoEditorListener) this.photoHandler.getListener());
        if (this.mRawContactIdRequestingPhoto == editor.getRawContactId()) {
            this.mCurrentPhotoHandler = this.photoHandler;
        }
    }

    public RingtoneListener getRingtoneListener(RingtoneEditorView editor) {
        if (this.mRingtoneListener != null) {
            this.mRingtoneListener.setEditor(editor);
        }
        return this.mRingtoneListener;
    }

    private void bindGroupMetaData() {
        if (this.mContactEditor != null && this.mGroupMetaData != null) {
            this.mContactEditor.setGroupMetaData(this.mGroupMetaData);
        }
    }

    private void saveDefaultAccountIfNecessary() {
        AccountWithDataSet account = null;
        if ("android.intent.action.INSERT".equals(this.mAction) || this.mState.size() != 1 || isEditingUserProfile()) {
            RawContactDelta rawContactDelta = (RawContactDelta) this.mState.get(0);
            String name = rawContactDelta.getAccountName();
            String type = rawContactDelta.getAccountType();
            String dataSet = rawContactDelta.getDataSet();
            if (!(name == null || type == null)) {
                account = new AccountWithDataSet(name, type, dataSet);
            }
            if (account != null) {
                this.mEditorUtils.saveDefaultAndAllAccounts(account);
            }
        }
    }

    private void addAccountSwitcher(final RawContactDelta currentState, BaseRawContactEditorView editor) {
        final AccountWithDataSet currentAccount = new AccountWithDataSet(currentState.getAccountName(), currentState.getAccountType(), currentState.getDataSet());
        final ViewGroup anchorView = ((RawContactEditorView) editor).getTitleContainer();
        anchorView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ContactEditorFragment.this.showAlertDialog(anchorView, currentState, currentAccount);
            }
        });
    }

    private void hideSoftInputWindow() {
        Activity activity = getActivity();
        if (activity != null && activity.getWindow() != null) {
            View lCurrentViewWithFocus = activity.getWindow().getCurrentFocus();
            if (lCurrentViewWithFocus != null && lCurrentViewWithFocus.getWindowToken() != null) {
                InputMethodManager lManager = (InputMethodManager) activity.getSystemService("input_method");
                if (lManager != null) {
                    lManager.hideSoftInputFromWindow(lCurrentViewWithFocus.getWindowToken(), 0);
                }
            }
        }
    }

    private void disableAccountSwitcher(ViewGroup accountContainer) {
        if (accountContainer != null) {
            accountContainer.setEnabled(false);
            View arrowView = accountContainer.findViewById(R.id.account_arrow);
            if (arrowView != null) {
                arrowView.setVisibility(8);
            }
            View textView = accountContainer.findViewById(R.id.account_type);
            if (textView != null) {
                LayoutParams lp = new LayoutParams(-2, -2);
                lp.setMargins(0, 0, 0, 0);
                textView.setLayoutParams(lp);
            }
        }
    }

    private boolean isSimContact() {
        if (this.mState == null || this.mState.size() != 1) {
            return false;
        }
        boolean z;
        String accountType = ((RawContactDelta) this.mState.get(0)).getAccountType();
        if ("com.android.huawei.sim".equals(accountType)) {
            z = true;
        } else {
            z = "com.android.huawei.secondsim".equals(accountType);
        }
        return z;
    }

    private boolean hasValidState() {
        return this.mState != null && this.mState.size() > 0;
    }

    public boolean hasPendingChanges() {
        if (RawContactModifier.hasChanges(this.mState, AccountTypeManager.getInstance(this.mContext))) {
            return true;
        }
        return this.mCust != null ? this.mCust.hasCustomFeatureChange(this.mContactEditor.getRawContactId()) : false;
    }

    private boolean checkSaveState(Activity lActivityRef) {
        return lActivityRef == null || this.mState == null || !hasValidState() || this.mStatus != 1;
    }

    private boolean handleSimContactSave(Activity lActivityRef, String accountType) {
        if (isSimContact()) {
            int spaceEmailCount = SimFactoryManager.getSpareEmailCount(SimFactoryManager.getSlotIdBasedOnAccountType(accountType));
            RawContactDelta state = (RawContactDelta) this.mState.get(0);
            if (state != null) {
                ArrayList<ValuesDelta> entries = state.getMimeEntries("vnd.android.cursor.item/email_v2");
                if (entries != null) {
                    for (ValuesDelta entry : entries) {
                        String emailData = entry.getEmailData();
                        if (emailData != null) {
                            if (!TextUtils.isEmpty(emailData) && spaceEmailCount <= 0 && !entry.beforeExists()) {
                                lActivityRef.runOnUiThread(new ToastRunnabeImp(R.string.email_full));
                                return false;
                            } else if (emailData.length() > 38) {
                                lActivityRef.runOnUiThread(new ToastRunnabeImp(R.string.simcard_email_overabundance));
                                return false;
                            }
                        }
                    }
                } else {
                    HwLog.d("ContactEditorFragment", "Email.CONTENT_ITEM_TYPE entries in null");
                }
            }
        }
        return true;
    }

    private void addBigPoint() {
        if (!isEditingUserProfile()) {
            return;
        }
        if (this.mUpdatedPhotos != null && this.mUpdatedPhotos.size() > 0) {
            StatisticalHelper.report(1124);
        } else if (hasPendingChanges()) {
            StatisticalHelper.report(1124);
        }
    }

    private boolean handlePendingNotChanges(int saveMode) {
        if (hasPendingChanges()) {
            return false;
        }
        if (this.mLookupUri == null && saveMode == 1) {
            this.mStatus = 1;
            return true;
        }
        onSaveCompleted(false, saveMode, this.mLookupUri != null, this.mLookupUri, this.mInstanceToken);
        if (this.mLookupUri == null) {
            this.mStatus = 1;
        }
        return true;
    }

    private boolean checkPostExecute(Boolean result) {
        return (result.booleanValue() && isAdded()) ? false : true;
    }

    public boolean save(final int saveMode) {
        if (HwLog.HWFLOW) {
            HwLog.i("ContactEditorFragment", "save contact");
        }
        final Activity lActivityRef = getActivity();
        if (checkSaveState(lActivityRef)) {
            return false;
        }
        final String accountType = ((RawContactDelta) this.mState.get(0)).getAccountType();
        new AsyncTask<String, Void, Boolean>() {
            protected Boolean doInBackground(String... params) {
                if (ContactEditorFragment.this.handleSimContactSave(lActivityRef, params[0])) {
                    return Boolean.valueOf(true);
                }
                return Boolean.valueOf(false);
            }

            protected void onPostExecute(Boolean result) {
                if (!ContactEditorFragment.this.checkPostExecute(result)) {
                    if (saveMode == 0 || saveMode == 2) {
                        ContactEditorFragment.this.getLoaderManager().destroyLoader(1);
                    }
                    ContactEditorFragment.this.mStatus = 2;
                    ContactEditorFragment.this.addBigPoint();
                    if (!ContactEditorFragment.this.handlePendingNotChanges(saveMode)) {
                        boolean lLaunchDetailsForNewContact;
                        boolean fromProfileCreate;
                        boolean isFromDetailActivity;
                        Contact resultContact;
                        Class<? extends Activity> callbackActivity;
                        Intent intent;
                        ContactBuilder contactBuilder;
                        ContactsApplication app;
                        long lReqId;
                        Intent retIntent;
                        boolean isfromdetailCreateContact;
                        SimConfig simConfig;
                        String mSimCardDisplayLabel;
                        boolean fromDefault;
                        boolean fromDialpad;
                        Intent lIntent = lActivityRef.getIntent();
                        boolean handleInMemoryLaunch = 4 != saveMode ? saveMode == 0 : true;
                        if (!ContactEditorFragment.this.mIsActivityLeaveUserHint && handleInMemoryLaunch) {
                            if (!lIntent.getBooleanExtra("finishActivityOnSaveCompleted", false)) {
                                lLaunchDetailsForNewContact = "android.intent.action.INSERT".equals(lIntent.getAction());
                                fromProfileCreate = lIntent.getBooleanExtra("from_list_profile_create", false);
                                if (!lLaunchDetailsForNewContact) {
                                    ContactEditorFragment.this.setEnabled(false);
                                }
                                ContactEditorFragment.this.saveDefaultAccountIfNecessary();
                                isFromDetailActivity = lIntent.getBooleanExtra("isFromDetailActivity", false);
                                if (ContactEditorFragment.this.mHwCustContactEditorCustomizationObj != null) {
                                    ContactEditorFragment.this.mHwCustContactEditorCustomizationObj.handleSaveCustomization(ContactEditorFragment.this.isEditingUserProfile(), ContactEditorFragment.this.mState, ContactEditorFragment.this.mContext);
                                }
                                resultContact = null;
                                if (lLaunchDetailsForNewContact) {
                                    callbackActivity = ((Activity) ContactEditorFragment.this.mContext).getClass();
                                } else {
                                    callbackActivity = ContactDetailActivity.class;
                                }
                                if (ContactEditorFragment.this.isEditingUserProfile() && saveMode != 1) {
                                    callbackActivity = ProfileSimpleCardActivity.class;
                                }
                                intent = ContactSaveService.createSaveContactIntentWithToken(ContactEditorFragment.this.mContext.getApplicationContext(), ContactEditorFragment.this.mState, "saveMode", saveMode, ContactEditorFragment.this.isEditingUserProfile(), callbackActivity, "saveCompleted", ContactEditorFragment.this.mUpdatedPhotos, ContactEditorFragment.this.mInstanceToken);
                                if (ContactEditorFragment.this.mCust != null) {
                                    ContactEditorFragment.this.mCust.addSaveIntentExtras(intent);
                                }
                                if (ContactEditorFragment.this.isCamCard) {
                                    intent.putExtra("key_from_camcard", true);
                                }
                                if (ContactEditorFragment.this.mUpdatedPhotos != null) {
                                    ContactEditorFragment.this.hasPhoto = ContactEditorFragment.this.mUpdatedPhotos.size() == 0;
                                }
                                if (handleInMemoryLaunch) {
                                    if (isFromDetailActivity) {
                                        resultContact = new ContactBuilder(ContactEditorFragment.this.mContext, ContactEditorFragment.this.mState, ContactEditorFragment.this.isEditingUserProfile()).getContact(ContactEditorFragment.this.mLookupUri);
                                    } else if (!lLaunchDetailsForNewContact || fromProfileCreate) {
                                        intent.putExtra("launch_contact_details_before_contact_saved", true);
                                        contactBuilder = new ContactBuilder(ContactEditorFragment.this.mContext, ContactEditorFragment.this.mState, ContactEditorFragment.this.isEditingUserProfile());
                                        resultContact = contactBuilder.getNewContactFromState();
                                        if (ContactEditorFragment.this.mContactEditor instanceof RawContactEditorView) {
                                            HwLog.e("ContactEditorFragment", "set group meta data exception");
                                        } else {
                                            contactBuilder.setGroupMetaDeta(resultContact, ((RawContactEditorView) ContactEditorFragment.this.mContactEditor).getSelectedGroupsMetaData());
                                        }
                                    } else {
                                        HwLog.e("ContactEditorFragment", "set resultContact exception ");
                                    }
                                }
                                if (!lLaunchDetailsForNewContact) {
                                    ContactEditorFragment.this.mHandler.sendMessageDelayed(ContactEditorFragment.this.mHandler.obtainMessage(101, intent), 500);
                                }
                                if (resultContact != null) {
                                    app = (ContactsApplication) ContactEditorFragment.this.getActivity().getApplication();
                                    lReqId = System.currentTimeMillis();
                                    app.setContactResultForDetail(resultContact, lReqId);
                                    retIntent = new Intent();
                                    retIntent.putExtra("requestid", lReqId);
                                    isfromdetailCreateContact = ContactEditorFragment.this.getActivity().getIntent().getBooleanExtra("isFromDetailActivityCreateContact", false);
                                    if (!lLaunchDetailsForNewContact || fromProfileCreate) {
                                        if ("com.android.huawei.sim".equals(accountType) || "com.android.huawei.secondsim".equals(accountType)) {
                                            simConfig = SimFactoryManager.getSimConfig(accountType);
                                            if (simConfig != null) {
                                                Toast.makeText(ContactEditorFragment.this.getActivity(), String.format(ContactEditorFragment.this.getString(R.string.sim_save_unknown_error), new Object[]{SimFactoryManager.getSimCardDisplayLabel(accountType)}), 1).show();
                                                ContactEditorFragment.this.getActivity().finish();
                                                return;
                                            } else if (simConfig.getAvailableFreeSpace() == 0) {
                                                mSimCardDisplayLabel = SimFactoryManager.getSimCardDisplayLabel(accountType);
                                                Toast.makeText(ContactEditorFragment.this.getActivity(), String.format(ContactEditorFragment.this.getString(R.string.sim_full), new Object[]{mSimCardDisplayLabel, mSimCardDisplayLabel}), 1).show();
                                                ContactEditorFragment.this.getActivity().finish();
                                                return;
                                            }
                                        }
                                        if (isfromdetailCreateContact) {
                                            if (ContactEditorFragment.this.isEditingUserProfile()) {
                                                retIntent.setComponent(new ComponentName(ContactEditorFragment.this.getActivity(), ContactDetailActivity.class));
                                            } else {
                                                retIntent.setComponent(new ComponentName(ContactEditorFragment.this.getActivity(), ProfileSimpleCardActivity.class));
                                                retIntent.putExtra("from_profile_editor", ContactEditorFragment.this.isEditingUserProfile());
                                            }
                                            retIntent.putExtra("intent_key_has_photo", ContactEditorFragment.this.hasPhoto);
                                            retIntent.putExtra("serviceIntent", intent);
                                            retIntent.putExtra("intent_key_is_from_editor", true);
                                            retIntent.putExtra("intent_key_is_from_dialpad", lIntent.getBooleanExtra("intent_key_is_from_dialpad", false));
                                            retIntent.putExtra("phone", lIntent.getStringExtra("phone"));
                                            if (ContactEditorFragment.this.isAllFieldsEmpty()) {
                                                Toast.makeText(ContactEditorFragment.this.mContext, R.string.no_contact_details, 1).show();
                                            } else if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                                                ContactEditorFragment.this.startActivity(retIntent);
                                            } else {
                                                fromDefault = lIntent.getBooleanExtra("intent_key_is_from_default", false);
                                                fromDialpad = lIntent.getBooleanExtra("intent_key_is_from_dialpad", false);
                                                if (fromDefault) {
                                                    ContactEditorFragment.this.mContext.startService(ContactSaveService.createSaveContactIntentWithToken(ContactEditorFragment.this.mContext.getApplicationContext(), ContactEditorFragment.this.mState, "saveMode", saveMode, ContactEditorFragment.this.isEditingUserProfile(), PeopleActivity.class, "saveCompleted", ContactEditorFragment.this.mUpdatedPhotos, ContactEditorFragment.this.mInstanceToken));
                                                } else if (fromDialpad) {
                                                    ContactEditorFragment.this.startActivity(retIntent);
                                                } else {
                                                    retIntent.setClass(ContactEditorFragment.this.getActivity(), TranslucentActivity.class);
                                                    ContactEditorFragment.this.startActivity(retIntent);
                                                }
                                            }
                                            if (EmuiVersion.isSupportEmui()) {
                                                ContactEditorFragment.this.getActivity().finish();
                                            } else {
                                                ContactEditorFragment.this.mCustAnimationListener.onStartDetailAndFinish();
                                            }
                                        } else {
                                            retIntent.putExtra("serviceIntent", intent);
                                            retIntent.putExtra("isFromDetailActivityCreateContact", true);
                                            retIntent.putExtra("intent_key_has_photo", ContactEditorFragment.this.hasPhoto);
                                            ContactEditorFragment.this.getActivity().setResult(-1, retIntent);
                                            ContactEditorFragment.this.mCustAnimationListener.onBackToDetailAndFinish();
                                        }
                                    } else if (ContactEditorFragment.this.isEditingUserProfile()) {
                                        retIntent.setData(ContactEditorFragment.this.mLookupUri);
                                        retIntent.putExtra("intent_key_has_photo", ContactEditorFragment.this.hasPhoto);
                                        retIntent.setClass(ContactEditorFragment.this.mContext, ProfileSimpleCardActivity.class);
                                        retIntent.setFlags(67108864);
                                        retIntent.putExtra("from_profile_editor", ContactEditorFragment.this.isEditingUserProfile());
                                        ContactEditorFragment.this.getActivity().setResult(-1, retIntent);
                                    } else {
                                        retIntent.setData(ContactEditorFragment.this.mLookupUri);
                                        retIntent.putExtra("intent_key_has_photo", ContactEditorFragment.this.hasPhoto);
                                        if (EmuiVersion.isSupportEmui() && isFromDetailActivity) {
                                            retIntent.putExtra("intent_key_is_from_editor", true);
                                            if (!ContactEditorFragment.this.isEditingUserProfile()) {
                                                ContactEditorFragment.this.getActivity().setResult(0, retIntent);
                                            }
                                            ContactEditorFragment.this.mCustAnimationListener.onNotifyDetailResult(-1, retIntent);
                                            ContactEditorFragment.this.mCustAnimationListener.onBackToDetailAndFinish();
                                        } else {
                                            ContactEditorFragment.this.getActivity().setResult(-1, retIntent);
                                            ContactEditorFragment.this.getActivity().finish();
                                        }
                                    }
                                }
                                ContactEditorFragment.this.mUpdatedPhotos = new Bundle();
                                ContactEditorFragment.this.handleEditingUserProfile(saveMode, resultContact, fromProfileCreate);
                            }
                        }
                        lLaunchDetailsForNewContact = false;
                        fromProfileCreate = lIntent.getBooleanExtra("from_list_profile_create", false);
                        if (lLaunchDetailsForNewContact) {
                            ContactEditorFragment.this.setEnabled(false);
                        }
                        ContactEditorFragment.this.saveDefaultAccountIfNecessary();
                        isFromDetailActivity = lIntent.getBooleanExtra("isFromDetailActivity", false);
                        if (ContactEditorFragment.this.mHwCustContactEditorCustomizationObj != null) {
                            ContactEditorFragment.this.mHwCustContactEditorCustomizationObj.handleSaveCustomization(ContactEditorFragment.this.isEditingUserProfile(), ContactEditorFragment.this.mState, ContactEditorFragment.this.mContext);
                        }
                        resultContact = null;
                        if (lLaunchDetailsForNewContact) {
                            callbackActivity = ((Activity) ContactEditorFragment.this.mContext).getClass();
                        } else {
                            callbackActivity = ContactDetailActivity.class;
                        }
                        callbackActivity = ProfileSimpleCardActivity.class;
                        intent = ContactSaveService.createSaveContactIntentWithToken(ContactEditorFragment.this.mContext.getApplicationContext(), ContactEditorFragment.this.mState, "saveMode", saveMode, ContactEditorFragment.this.isEditingUserProfile(), callbackActivity, "saveCompleted", ContactEditorFragment.this.mUpdatedPhotos, ContactEditorFragment.this.mInstanceToken);
                        if (ContactEditorFragment.this.mCust != null) {
                            ContactEditorFragment.this.mCust.addSaveIntentExtras(intent);
                        }
                        if (ContactEditorFragment.this.isCamCard) {
                            intent.putExtra("key_from_camcard", true);
                        }
                        if (ContactEditorFragment.this.mUpdatedPhotos != null) {
                            if (ContactEditorFragment.this.mUpdatedPhotos.size() == 0) {
                            }
                            ContactEditorFragment.this.hasPhoto = ContactEditorFragment.this.mUpdatedPhotos.size() == 0;
                        }
                        if (handleInMemoryLaunch) {
                            if (isFromDetailActivity) {
                                resultContact = new ContactBuilder(ContactEditorFragment.this.mContext, ContactEditorFragment.this.mState, ContactEditorFragment.this.isEditingUserProfile()).getContact(ContactEditorFragment.this.mLookupUri);
                            } else {
                                if (lLaunchDetailsForNewContact) {
                                }
                                intent.putExtra("launch_contact_details_before_contact_saved", true);
                                contactBuilder = new ContactBuilder(ContactEditorFragment.this.mContext, ContactEditorFragment.this.mState, ContactEditorFragment.this.isEditingUserProfile());
                                resultContact = contactBuilder.getNewContactFromState();
                                if (ContactEditorFragment.this.mContactEditor instanceof RawContactEditorView) {
                                    HwLog.e("ContactEditorFragment", "set group meta data exception");
                                } else {
                                    contactBuilder.setGroupMetaDeta(resultContact, ((RawContactEditorView) ContactEditorFragment.this.mContactEditor).getSelectedGroupsMetaData());
                                }
                            }
                        }
                        if (lLaunchDetailsForNewContact) {
                            ContactEditorFragment.this.mHandler.sendMessageDelayed(ContactEditorFragment.this.mHandler.obtainMessage(101, intent), 500);
                        }
                        if (resultContact != null) {
                            app = (ContactsApplication) ContactEditorFragment.this.getActivity().getApplication();
                            lReqId = System.currentTimeMillis();
                            app.setContactResultForDetail(resultContact, lReqId);
                            retIntent = new Intent();
                            retIntent.putExtra("requestid", lReqId);
                            isfromdetailCreateContact = ContactEditorFragment.this.getActivity().getIntent().getBooleanExtra("isFromDetailActivityCreateContact", false);
                            if (lLaunchDetailsForNewContact) {
                            }
                            simConfig = SimFactoryManager.getSimConfig(accountType);
                            if (simConfig != null) {
                                if (simConfig.getAvailableFreeSpace() == 0) {
                                    mSimCardDisplayLabel = SimFactoryManager.getSimCardDisplayLabel(accountType);
                                    Toast.makeText(ContactEditorFragment.this.getActivity(), String.format(ContactEditorFragment.this.getString(R.string.sim_full), new Object[]{mSimCardDisplayLabel, mSimCardDisplayLabel}), 1).show();
                                    ContactEditorFragment.this.getActivity().finish();
                                    return;
                                }
                                if (isfromdetailCreateContact) {
                                    if (ContactEditorFragment.this.isEditingUserProfile()) {
                                        retIntent.setComponent(new ComponentName(ContactEditorFragment.this.getActivity(), ContactDetailActivity.class));
                                    } else {
                                        retIntent.setComponent(new ComponentName(ContactEditorFragment.this.getActivity(), ProfileSimpleCardActivity.class));
                                        retIntent.putExtra("from_profile_editor", ContactEditorFragment.this.isEditingUserProfile());
                                    }
                                    retIntent.putExtra("intent_key_has_photo", ContactEditorFragment.this.hasPhoto);
                                    retIntent.putExtra("serviceIntent", intent);
                                    retIntent.putExtra("intent_key_is_from_editor", true);
                                    retIntent.putExtra("intent_key_is_from_dialpad", lIntent.getBooleanExtra("intent_key_is_from_dialpad", false));
                                    retIntent.putExtra("phone", lIntent.getStringExtra("phone"));
                                    if (ContactEditorFragment.this.isAllFieldsEmpty()) {
                                        Toast.makeText(ContactEditorFragment.this.mContext, R.string.no_contact_details, 1).show();
                                    } else if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                                        ContactEditorFragment.this.startActivity(retIntent);
                                    } else {
                                        fromDefault = lIntent.getBooleanExtra("intent_key_is_from_default", false);
                                        fromDialpad = lIntent.getBooleanExtra("intent_key_is_from_dialpad", false);
                                        if (fromDefault) {
                                            ContactEditorFragment.this.mContext.startService(ContactSaveService.createSaveContactIntentWithToken(ContactEditorFragment.this.mContext.getApplicationContext(), ContactEditorFragment.this.mState, "saveMode", saveMode, ContactEditorFragment.this.isEditingUserProfile(), PeopleActivity.class, "saveCompleted", ContactEditorFragment.this.mUpdatedPhotos, ContactEditorFragment.this.mInstanceToken));
                                        } else if (fromDialpad) {
                                            ContactEditorFragment.this.startActivity(retIntent);
                                        } else {
                                            retIntent.setClass(ContactEditorFragment.this.getActivity(), TranslucentActivity.class);
                                            ContactEditorFragment.this.startActivity(retIntent);
                                        }
                                    }
                                    if (EmuiVersion.isSupportEmui()) {
                                        ContactEditorFragment.this.getActivity().finish();
                                    } else {
                                        ContactEditorFragment.this.mCustAnimationListener.onStartDetailAndFinish();
                                    }
                                } else {
                                    retIntent.putExtra("serviceIntent", intent);
                                    retIntent.putExtra("isFromDetailActivityCreateContact", true);
                                    retIntent.putExtra("intent_key_has_photo", ContactEditorFragment.this.hasPhoto);
                                    ContactEditorFragment.this.getActivity().setResult(-1, retIntent);
                                    ContactEditorFragment.this.mCustAnimationListener.onBackToDetailAndFinish();
                                }
                            } else {
                                Toast.makeText(ContactEditorFragment.this.getActivity(), String.format(ContactEditorFragment.this.getString(R.string.sim_save_unknown_error), new Object[]{SimFactoryManager.getSimCardDisplayLabel(accountType)}), 1).show();
                                ContactEditorFragment.this.getActivity().finish();
                                return;
                            }
                        }
                        ContactEditorFragment.this.mUpdatedPhotos = new Bundle();
                        ContactEditorFragment.this.handleEditingUserProfile(saveMode, resultContact, fromProfileCreate);
                    }
                }
            }
        }.execute(new String[]{accountType});
        return true;
    }

    private void handleEditingUserProfile(int saveMode, Contact resultContact, boolean fromProfileCreate) {
        if (isEditingUserProfile() && saveMode != 1) {
            ContactsApplication app = (ContactsApplication) getActivity().getApplication();
            long lReqId = System.currentTimeMillis();
            app.setContactResultForDetail(resultContact, lReqId);
            Intent retIntent = new Intent();
            retIntent.putExtra("requestid", lReqId);
            retIntent.setClass(this.mContext, ProfileSimpleCardActivity.class);
            retIntent.setFlags(67108864);
            if (saveMode == 0) {
                retIntent.putExtra("from_list_profile_create", fromProfileCreate);
            } else {
                retIntent.putExtra("from_profile_editor", isEditingUserProfile());
            }
            retIntent.putExtra("intent_key_has_photo", this.hasPhoto);
            startActivity(retIntent);
        }
    }

    public boolean revert() {
        if (this.mState == null || !hasPendingChanges()) {
            doRevertAction();
        } else {
            CancelEditDialogFragment.show(this);
        }
        return true;
    }

    private void doRevertAction() {
        ExceptionCapture.reportScene(61);
        this.mStatus = 3;
        if (!EmuiFeatureManager.isUseCustAnimation() || CommonUtilMethods.isSimplifiedModeEnabled() || getResources().getConfiguration().orientation != 1 || this.mCustomAnimMessenger == null) {
            if (isEditingUserProfile()) {
                boolean fromProfileCreate = false;
                if (!(getActivity() == null || getActivity().getIntent() == null)) {
                    fromProfileCreate = getActivity().getIntent().getBooleanExtra("from_list_profile_create", false);
                }
                Intent profileIntent = new Intent("com.huawei.android.intent.action.PROFILE_CONTACT", this.mLookupUri);
                profileIntent.setFlags(67108864);
                profileIntent.setClass(this.mContext, ProfileSimpleCardActivity.class);
                if (!fromProfileCreate) {
                    startActivity(profileIntent);
                }
            }
            if (this.mListener != null) {
                this.mListener.onReverted();
                return;
            }
            return;
        }
        startAnimateOut();
    }

    public void doSaveAction() {
        HwLog.i("ContactEditorFragment", "doSaveAction Start");
        ExceptionCapture.reportScene(60);
        if (this.mCust == null || !this.mCust.isSupportValidateDuplicate()) {
            save(0);
        } else {
            this.mCust.doValidateDuplicate(0, this.mState, this.mStatus, 1, getActivity(), this);
        }
        HwLog.i("ContactEditorFragment", "doSaveAction end");
    }

    public void onJoinCompleted(Uri uri) {
        boolean z;
        if (uri != null) {
            z = true;
        } else {
            z = false;
        }
        onSaveCompleted(false, 1, z, uri, this.mInstanceToken);
    }

    public void onSaveCompleted(boolean hadChanges, int saveMode, boolean saveSucceeded, Uri contactLookupUri, long instanceToken) {
        if (hadChanges && saveSucceeded && saveMode != 3 && getActivity().getIntent().getBooleanExtra("PICK_CONTACT_PHOTO", false)) {
            Intent intent = new Intent();
            intent.setData(contactLookupUri);
            getActivity().setResult(-1, intent);
            getActivity().finish();
        } else if (instanceToken == this.mInstanceToken) {
            switch (saveMode) {
                case 0:
                case 4:
                    Intent resultIntent;
                    if (!saveSucceeded || contactLookupUri == null) {
                        resultIntent = null;
                    } else {
                        Object authority = this.mLookupUri == null ? null : this.mLookupUri.getAuthority();
                        String legacyAuthority = "contacts";
                        resultIntent = new Intent();
                        if (isEditingUserProfile()) {
                            resultIntent.setAction("com.huawei.android.intent.action.PROFILE_CONTACT");
                            resultIntent.setData(contactLookupUri);
                            resultIntent.setClass(this.mContext, ProfileSimpleCardActivity.class);
                            resultIntent.setFlags(67108864);
                            startActivity(resultIntent);
                            getActivity().finish();
                            return;
                        }
                        boolean isFromAddExistContact = getActivity().getIntent().getBooleanExtra("extra_add_exist_contact", false);
                        resultIntent.setAction("android.intent.action.VIEW");
                        if ("contacts".equals(authority)) {
                            Uri uri = Contacts.lookupContact(this.mContext.getContentResolver(), contactLookupUri);
                            if (uri != null) {
                                resultIntent.setData(ContentUris.withAppendedId(Uri.parse("content://contacts/people"), ContentUris.parseId(uri)));
                            }
                        } else {
                            if (isFromAddExistContact) {
                                resultIntent.putExtra("extra_add_exist_contact", true);
                                resultIntent.putExtra("intent_key_has_photo", this.hasPhoto);
                            }
                            resultIntent.setData(contactLookupUri);
                        }
                    }
                    this.mStatus = 3;
                    if (this.mListener != null) {
                        this.mListener.onSaveFinished(resultIntent);
                        break;
                    }
                    break;
                case 1:
                case 3:
                    if (!saveSucceeded || contactLookupUri == null) {
                        if (!saveSucceeded && contactLookupUri == null) {
                            this.mStatus = 1;
                            setEnabled(true);
                            break;
                        }
                    }
                    if (saveMode == 3 && hasValidState()) {
                        showJoinAggregateActivity(contactLookupUri);
                    }
                    this.mState = null;
                    load("android.intent.action.EDIT", contactLookupUri, null);
                    this.mStatus = 0;
                    getLoaderManager().restartLoader(1, new Bundle(), this.mDataLoaderListener);
                    break;
                    break;
                case 2:
                    this.mStatus = 3;
                    if (this.mListener == null) {
                        HwLog.d("ContactEditorFragment", "No listener registered, can not call onSplitFinished");
                        break;
                    } else {
                        this.mListener.onContactSplit(contactLookupUri);
                        break;
                    }
            }
        }
    }

    private void showJoinAggregateActivity(Uri contactLookupUri) {
        if (contactLookupUri != null && isAdded()) {
            this.mContactIdForJoin = ContentUris.parseId(contactLookupUri);
            this.mContactWritableForJoin = isContactWritable();
            Intent intent = new Intent("com.android.contacts.action.JOIN_CONTACT");
            intent.putExtra("com.android.contacts.action.CONTACT_ID", this.mContactIdForJoin);
            startActivityForResult(intent, 0);
        }
    }

    private void joinAggregate(long contactId) {
        this.mContext.startService(ContactSaveService.createJoinContactsIntent(this.mContext.getApplicationContext(), this.mContactIdForJoin, contactId, this.mContactWritableForJoin, ContactEditorActivity.class, "joinCompleted"));
    }

    private boolean isContactWritable() {
        AccountTypeManager accountTypes = AccountTypeManager.getInstance(this.mContext);
        if (this.mState != null) {
            int size = this.mState.size();
            for (int i = 0; i < size; i++) {
                if (((RawContactDelta) this.mState.get(i)).getAccountType(accountTypes).areContactsWritable()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isEditingUserProfile() {
        return !this.mNewLocalProfile ? this.mIsUserProfile : true;
    }

    protected long getContactId() {
        if (this.mState != null) {
            for (RawContactDelta rawContact : this.mState) {
                Long contactId = rawContact.getValues().getAsLong("contact_id");
                if (contactId != null) {
                    return contactId.longValue();
                }
            }
        }
        return 0;
    }

    private void acquireAggregationSuggestions(Context context, RawContactEditorView rawContactEditor) {
        if (!CommonUtilMethods.isSimAccount(rawContactEditor.getAccountType())) {
            this.mAggregationSuggestionsRawContactId = rawContactEditor.getRawContactId();
            if (!this.mIsActivityStopped) {
                if (this.mAggregationSuggestionEngine == null) {
                    this.mAggregationSuggestionEngine = new AggregationSuggestionEngine(context);
                    this.mAggregationSuggestionEngine.setListener(this);
                    this.mAggregationSuggestionEngine.start();
                }
                this.mAggregationSuggestionEngine.setContactId(getContactId());
                this.mAggregationSuggestionEngine.onNameChange(rawContactEditor.getNameEditor().getValues());
            }
        }
    }

    public void onAggregationSuggestionChange() {
        if (isAdded() && this.mState != null && this.mStatus == 1) {
            if (this.mAggregationSuggestionPopup != null && this.mAggregationSuggestionPopup.isShowing()) {
                this.mAggregationSuggestionPopup.dismiss();
            }
            if (!(this.mAggregationSuggestionEngine.getSuggestedContactCount() == 0 || this.mContactEditor == null)) {
                boolean isContactInsert;
                View anchorView = this.mContactEditor.findViewById(R.id.anchor_view);
                this.mAggregationSuggestionPopup = new ListPopupWindow(this.mContext, null);
                this.mAggregationSuggestionPopup.setAnchorView(anchorView);
                this.mAggregationSuggestionPopup.setWidth(anchorView.getWidth());
                this.mAggregationSuggestionPopup.setInputMethodMode(2);
                ListPopupWindow listPopupWindow = this.mAggregationSuggestionPopup;
                Activity activity = getActivity();
                if (this.mState.size() == 1) {
                    isContactInsert = ((RawContactDelta) this.mState.get(0)).isContactInsert();
                } else {
                    isContactInsert = false;
                }
                listPopupWindow.setAdapter(new AggregationSuggestionAdapter(activity, isContactInsert, this, this.mAggregationSuggestionEngine.getSuggestions()));
                this.mAggregationSuggestionPopup.setOnItemClickListener(this.mAggregationSuggestionItemClickListener);
                if (!QueryUtil.isHAPProviderInstalled() || (this.mState.size() == 1 && ((RawContactDelta) this.mState.get(0)).isContactInsert())) {
                    View editors = this.mContactEditor.findViewById(R.id.editors);
                    if (editors != null) {
                        this.mAggregationSuggestionPopup.setVerticalOffset(editors.getHeight() - this.mContext.getResources().getDimensionPixelSize(R.dimen.create_new_contact_name_popwindow_top_differ_distance));
                        int leftDistance = this.mContext.getResources().getDimensionPixelSize(R.dimen.create_new_contact_name_popwindow_left_differ_distance);
                        if (CommonUtilMethods.isLayoutRTL()) {
                            this.mAggregationSuggestionPopup.setHorizontalOffset(Math.abs(leftDistance));
                        } else {
                            this.mAggregationSuggestionPopup.setHorizontalOffset(leftDistance);
                        }
                    }
                    if (isAdded()) {
                        this.mAggregationSuggestionPopup.show();
                    } else {
                        HwLog.w("ContactEditorFragment", "onAggregationSuggestionChange activity is not running");
                    }
                }
            }
        }
    }

    public void onJoinAction(long contactId, List<Long> rawContactIdList) {
        long[] rawContactIds = new long[rawContactIdList.size()];
        for (int i = 0; i < rawContactIds.length; i++) {
            rawContactIds[i] = ((Long) rawContactIdList.get(i)).longValue();
        }
        JoinSuggestedContactDialogFragment dialog = new JoinSuggestedContactDialogFragment();
        Bundle args = new Bundle();
        args.putLongArray("rawContactIds", rawContactIds);
        dialog.setArguments(args);
        dialog.setTargetFragment(this, 0);
        try {
            dialog.show(getFragmentManager(), "join");
        } catch (Exception e) {
        }
    }

    protected void doJoinSuggestedContact(long[] rawContactIds) {
        if (hasValidState() && this.mStatus == 1) {
            this.mState.setJoinWithRawContacts(rawContactIds);
            save(1);
        }
    }

    public String getAction() {
        return this.mAction;
    }

    public void onEditAction(Uri contactLookupUri) {
        SuggestionEditConfirmationDialogFragment dialog = new SuggestionEditConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("contactUri", contactLookupUri);
        dialog.setArguments(args);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "edit");
    }

    protected void doEditSuggestedContact(Uri contactUri) {
        if (this.mListener != null) {
            this.mStatus = 3;
            this.mListener.onEditOtherContactRequested(contactUri, ((RawContactDelta) this.mState.get(0)).getContentValues());
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("uri", this.mLookupUri);
        outState.putString("action", this.mAction);
        if (hasValidState()) {
            outState.putParcelable("state", this.mState);
        }
        outState.putLong("photorequester", this.mRawContactIdRequestingPhoto);
        outState.putLong("ringtonerequester", this.mRawContactIdRequestingRingtone);
        outState.putParcelable("viewidgenerator", this.mViewIdGenerator);
        outState.putParcelable("currentphotouri", this.mCurrentPhotoUri);
        outState.putLong("contactidforjoin", this.mContactIdForJoin);
        outState.putBoolean("contactwritableforjoin", this.mContactWritableForJoin);
        outState.putLong("showJoinSuggestions", this.mAggregationSuggestionsRawContactId);
        outState.putBoolean("enabled", this.mEnabled);
        outState.putBoolean("newLocalProfile", this.mNewLocalProfile);
        outState.putBoolean("isUserProfile", this.mIsUserProfile);
        outState.putInt("status", this.mStatus);
        outState.putParcelable("updatedPhotos", this.mUpdatedPhotos);
        outState.putBoolean("simNumberIsReplaceDialog", this.mIsReplaceDialog);
        outState.putBoolean("simDialogIsShownOnce", this.mIsDialogShownOnce);
        super.onSaveInstanceState(outState);
    }

    public void setStatus(int aStatus) {
        this.mStatus = aStatus;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.mStatus == 4) {
            this.mStatus = 1;
        }
        if (this.mCurrentPhotoHandler == null || !this.mCurrentPhotoHandler.handlePhotoActivityResult(requestCode, resultCode, data)) {
            switch (requestCode) {
                case 0:
                    if (resultCode == -1) {
                        if (data != null) {
                            joinAggregate(ContentUris.parseId(data.getData()));
                            break;
                        }
                    }
                    return;
                    break;
                case 1:
                    this.mIsSelectingAccount = false;
                    if (resultCode == -1) {
                        if (this.mIsNotifyLaunchPendding) {
                            onActivityLaunchedFirst();
                        }
                        if (data != null) {
                            AccountWithDataSet account;
                            if (EmuiFeatureManager.isAndroidMVersion()) {
                                account = (AccountWithDataSet) data.getParcelableExtra("android.provider.extra.ACCOUNT");
                            } else {
                                account = (AccountWithDataSet) data.getParcelableExtra("com.android.contacts.extra.ACCOUNT");
                            }
                            if (account != null) {
                                createContact(account);
                                return;
                            }
                        }
                        createContact();
                        break;
                    }
                    getActivity().finish();
                    getActivity().overridePendingTransition(0, 0);
                    return;
                case 1000:
                case 1001:
                    if (resultCode != 0) {
                        if (!(this.mContactEditor == null || data == null)) {
                            Uri pickedUri;
                            if (requestCode == 1000) {
                                pickedUri = (Uri) data.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI");
                            } else {
                                pickedUri = data.getData();
                                this.mContext.getSharedPreferences("com.android.contacts.custom_ringtone", 0).edit().putString(pickedUri.toString(), CommonUtilMethods.getPathFromUri(this.mContext, pickedUri)).commit();
                            }
                            if (pickedUri != null) {
                                this.mContactEditor.setRingtone(pickedUri.toString());
                            } else {
                                this.mContactEditor.setRingtone("-1");
                            }
                            this.mRawContactIdRequestingRingtone = -1;
                            break;
                        }
                    }
                    return;
                case 1002:
                    if (resultCode == -1 && data != null) {
                        long[] checkedGroupList = data.getLongArrayExtra("checkedGroupList");
                        if (this.mContactEditor instanceof RawContactEditorView) {
                            GroupMembershipView groupMembershipView = ((RawContactEditorView) this.mContactEditor).getGroupMembershipView();
                            if (groupMembershipView != null) {
                                if (!groupMembershipView.isAllGroupLoaded(checkedGroupList)) {
                                    groupMembershipView.setCheckedGroupList(checkedGroupList);
                                    break;
                                } else {
                                    groupMembershipView.updateGroup(checkedGroupList);
                                    break;
                                }
                            }
                        }
                    }
                    break;
            }
            if (this.mCust != null) {
                this.mCust.customizeOnActivityResult(requestCode, resultCode, data, getActivity(), this.mContent, this.mRawContactIdRequestingRingtone);
            }
        }
    }

    private void setPhoto(long rawContact, Bitmap photo, Uri photoUri, BaseRawContactEditorView editor) {
        if (photo != null && photo.getHeight() >= 0) {
            if (photo.getWidth() < 0) {
            }
            if (editor == null) {
                editor.setPhotoBitmap(photo);
            } else {
                HwLog.w("ContactEditorFragment", "The contact that requested the photo is no longer present.");
            }
            this.mUpdatedPhotos.putParcelable(String.valueOf(rawContact), photoUri);
        }
        HwLog.w("ContactEditorFragment", "Invalid bitmap passed to setPhoto()");
        if (editor == null) {
            HwLog.w("ContactEditorFragment", "The contact that requested the photo is no longer present.");
        } else {
            editor.setPhotoBitmap(photo);
        }
        this.mUpdatedPhotos.putParcelable(String.valueOf(rawContact), photoUri);
    }

    private void setPhoto(ArrayList<Long> rawContactList, Bitmap photo, Uri photoUri, BaseRawContactEditorView editor) {
        if (photo != null && photo.getHeight() >= 0) {
            if (photo.getWidth() < 0) {
            }
            if (editor == null) {
                editor.setPhotoBitmap(photo);
            } else {
                HwLog.w("ContactEditorFragment", "The contact that requested the photo is no longer present.");
            }
            for (Long rawContact : rawContactList) {
                this.mUpdatedPhotos.putParcelable(String.valueOf(rawContact), photoUri);
            }
        }
        HwLog.w("ContactEditorFragment", "Invalid bitmap passed to setPhoto()");
        if (editor == null) {
            HwLog.w("ContactEditorFragment", "The contact that requested the photo is no longer present.");
        } else {
            editor.setPhotoBitmap(photo);
        }
        while (rawContact$iterator.hasNext()) {
            this.mUpdatedPhotos.putParcelable(String.valueOf(rawContact), photoUri);
        }
    }

    private boolean hasMoreThanOnePhoto() {
        int countWithPicture = 0;
        int numEntities = this.mState.size();
        for (int i = 0; i < numEntities; i++) {
            RawContactDelta entity = (RawContactDelta) this.mState.get(i);
            if (entity.isVisible()) {
                ValuesDelta primary = entity.getPrimaryEntry("vnd.android.cursor.item/photo");
                if (primary == null || primary.getPhoto() == null) {
                    Uri uri = (Uri) this.mUpdatedPhotos.getParcelable(String.valueOf(entity.getRawContactId().longValue()));
                    if (uri != null) {
                        try {
                            InputStream is = this.mContext.getContentResolver().openInputStream(uri);
                            countWithPicture++;
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    HwLog.w("ContactEditorFragment", "io exception when close input stream");
                                }
                            }
                        } catch (FileNotFoundException e2) {
                        }
                    }
                } else {
                    countWithPicture++;
                }
                if (countWithPicture > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    public void onContactDataLoadFinished(Contact data) {
        HwLog.v("ContactEditorFragment", "Time needed for loading: " + (SystemClock.elapsedRealtime() - this.mLoaderStartTime));
        if (data.isLoaded()) {
            this.mStatus = 1;
            this.mLookupUri = data.getLookupUri();
            long setDataStartTime = SystemClock.elapsedRealtime();
            this.mAction = "android.intent.action.EDIT";
            setData(data);
            HwLog.v("ContactEditorFragment", "Time needed for setting UI: " + (SystemClock.elapsedRealtime() - setDataStartTime));
            return;
        }
        HwLog.i("ContactEditorFragment", "No contact found. Closing activity");
        if (this.mListener != null) {
            this.mListener.onContactNotFound();
        }
    }

    boolean doPickRingtoneAction(long rawContactId, String lRingtone, String accountType) {
        if (!hasValidState()) {
            return false;
        }
        Uri uri;
        this.mRawContactIdRequestingRingtone = rawContactId;
        Uri lDefaultUri = CommonUtilMethods.initializeDefaultRingtone(this.mContext, accountType);
        if ("-1".equals(lRingtone)) {
            uri = null;
        } else if (TextUtils.isEmpty(lRingtone)) {
            uri = lDefaultUri;
        } else {
            uri = Uri.parse(lRingtone);
        }
        Intent lRingtoneSlectionIntent = CommonUtilMethods.getRingtoneIntent(getActivity(), uri);
        setStatus(4);
        startActivityForResult(lRingtoneSlectionIntent, 1000);
        return true;
    }

    public void setExcludeSim(boolean b) {
        this.mExcludeSim = b;
    }

    public void setExcludeSim1(boolean b) {
        this.mExcludeSim1 = b;
    }

    public void setExcludeSim2(boolean b) {
        this.mExcludeSim2 = b;
    }

    public void setExcludeBothSim(boolean b) {
        this.mExcludeSim1 = b;
        this.mExcludeSim2 = b;
    }

    private boolean handleContactsFromDetail() {
        Activity activity = getActivity();
        Intent intent = activity.getIntent();
        Contact contact = null;
        if (intent != null) {
            long reqId = intent.getLongExtra("requestid", -1);
            if (-1 != reqId) {
                contact = ((ContactsApplication) activity.getApplication()).getContactAndReset(reqId);
                if (contact != null) {
                    this.mBitmapFromDetail = contact.getBitmap();
                }
            }
        }
        if (contact == null) {
            return false;
        }
        if (HwLog.HWDBG) {
            HwLog.d("Optimization", "Contacts editor populated with detail contacts object.");
        }
        if (OptimizationUtil.isLoadCompoentDelayedOnInsertContact()) {
            loadContactFromDetailDelayed(contact);
        } else {
            onContactDataLoadFinished(contact);
        }
        return true;
    }

    private void loadContactFromDetailDelayed(final Contact contactFromCaller) {
        OptimizationUtil.postTaskToRunAferActivitylaunched(getActivity(), this.mHandler, new Runnable() {
            public void run() {
                ContactEditorFragment.this.onContactDataLoadFinished(contactFromCaller);
            }
        });
    }

    public void setActivityLeaveUserHint(boolean aIsActivityLeaveUserHint) {
        if (!this.mIsActivityStopped) {
            this.mIsActivityLeaveUserHint = aIsActivityLeaveUserHint;
        }
    }

    private void initActionBar(boolean isReadOnly) {
        if (getActivity() instanceof ContactEditorActivity) {
            ((ContactEditorActivity) getActivity()).onContactLoaded(isReadOnly);
        }
    }

    private boolean isAllFieldsEmpty() {
        if (!(this.mContactEditor instanceof RawContactEditorView) || ((RawContactEditorView) this.mContactEditor).isAllFieldsEmpty()) {
            return true;
        }
        return false;
    }

    private void startAnimateIn() {
        if (getActivity() == null || this.mContactEditor == null) {
            if (HwLog.HWFLOW) {
                HwLog.i("ContactEditorFragment", "mContactEditor=" + this.mContactEditor);
            }
            return;
        }
        Resources res = getResources();
        View actionBar = getActivity().findViewById(16909291);
        View editorHead = getEditorHead();
        View editorContent = this.mContactEditor.getScrollView();
        actionBar.setTranslationY(-((float) res.getDimensionPixelSize(R.dimen.contact_editor_animation_action_bar_translation)));
        editorHead.setScaleX(0.0f);
        editorHead.setScaleY(0.0f);
        editorContent.setAlpha(0.0f);
        editorContent.setTranslationY(this.CONTENT_TRANSLATION);
        restoreInterfaceFrommTransparent();
        ObjectAnimator actionBarTyAnim = ObjectAnimator.ofFloat(actionBar, View.TRANSLATION_Y, new float[]{0.0f});
        ObjectAnimator actionBarAlphaAnim = ObjectAnimator.ofFloat(actionBar, View.ALPHA, new float[]{1.0f});
        ObjectAnimator headScaleXAnim = ObjectAnimator.ofFloat(editorHead, View.SCALE_X, new float[]{1.0f});
        ObjectAnimator headScaleYAnim = ObjectAnimator.ofFloat(editorHead, View.SCALE_Y, new float[]{1.0f});
        ObjectAnimator contentAlphaAnim = ObjectAnimator.ofFloat(editorContent, View.ALPHA, new float[]{1.0f});
        ObjectAnimator contentTyAnim = ObjectAnimator.ofFloat(editorContent, View.TRANSLATION_Y, new float[]{0.0f});
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(actionBarTyAnim).with(actionBarAlphaAnim).with(headScaleXAnim).with(headScaleYAnim).with(contentAlphaAnim).with(contentTyAnim);
        animatorSet.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, R.interpolator.cubic_bezier_interpolator_type_10_90));
        animatorSet.setDuration(this.ANIMAION_DERATION);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                if (ContactEditorFragment.this.mCustomAnimMessenger != null) {
                    try {
                        ContactEditorFragment.this.mCustomAnimMessenger.send(Message.obtain(null, 1006));
                    } catch (RemoteException e) {
                        HwLog.e("ContactEditorFragment", "AnimateIn end,send message failed");
                    }
                }
            }
        });
        animatorSet.start();
    }

    private void startAnimateOut() {
        if (!this.mIsOutAnimRunning) {
            try {
                this.mCustomAnimMessenger.send(Message.obtain(null, 1003));
            } catch (RemoteException e) {
                HwLog.e("ContactEditorFragment", "AnimateOut end,send message failed");
            }
            Resources res = getResources();
            View actionBar = getActivity().findViewById(16909291);
            View editorHead = getEditorHead();
            View editorContent = this.mContactEditor.getScrollView();
            float actionBarTy = (float) res.getDimensionPixelSize(R.dimen.contact_editor_animation_action_bar_translation);
            ObjectAnimator actionBarTyAnim = ObjectAnimator.ofFloat(actionBar, View.TRANSLATION_Y, new float[]{-actionBarTy});
            ObjectAnimator actionBarAlphaAnim = ObjectAnimator.ofFloat(actionBar, View.ALPHA, new float[]{0.0f});
            ObjectAnimator headScaleXAnim = ObjectAnimator.ofFloat(editorHead, View.SCALE_X, new float[]{0.0f});
            ObjectAnimator headScaleYAnim = ObjectAnimator.ofFloat(editorHead, View.SCALE_Y, new float[]{0.0f});
            ObjectAnimator contentAlphaAnim = ObjectAnimator.ofFloat(editorContent, View.ALPHA, new float[]{0.0f});
            ObjectAnimator contentTyAnim = ObjectAnimator.ofFloat(editorContent, View.TRANSLATION_Y, new float[]{this.CONTENT_TRANSLATION});
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(actionBarTyAnim).with(actionBarAlphaAnim).with(headScaleXAnim).with(headScaleYAnim).with(contentAlphaAnim).with(contentTyAnim);
            animatorSet.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, R.interpolator.cubic_bezier_interpolator_type_90_10));
            animatorSet.setDuration(this.ANIMAION_DERATION);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    ContactEditorFragment.this.mIsOutAnimRunning = true;
                }

                public void onAnimationEnd(Animator animator) {
                    ContactEditorFragment.this.mIsOutAnimRunning = false;
                    Activity activity = ContactEditorFragment.this.getActivity();
                    if (activity != null) {
                        if (ContactEditorFragment.this.mCustomAnimMessenger != null) {
                            try {
                                ContactEditorFragment.this.mCustomAnimMessenger.send(Message.obtain(null, 1004));
                            } catch (RemoteException e) {
                                HwLog.e("ContactEditorFragment", "AnimateOut end,send message failed");
                            } finally {
                                activity.finish();
                            }
                        }
                        activity.overridePendingTransition(0, 0);
                    }
                }

                public void onAnimationCancel(Animator animation) {
                    ContactEditorFragment.this.mIsOutAnimRunning = false;
                }
            });
            animatorSet.start();
        }
    }

    public void onActivityLaunchedFirst() {
        if (HwLog.HWFLOW) {
            HwLog.i("ContactEditorFragment", "onActivityLaunchedFirst,mCustomAnimMessenger=" + this.mCustomAnimMessenger + ",mIsSelectingAccount=" + this.mIsSelectingAccount);
        }
        if (getActivity() != null && !getActivity().isFinishing()) {
            if (this.mCustomAnimMessenger == null) {
                restoreInterfaceFrommTransparent();
            } else if (this.mIsSelectingAccount) {
                this.mIsNotifyLaunchPendding = true;
            } else {
                this.mIsNotifyLaunchPendding = false;
                Message msg = Message.obtain(null, 1001, 1, 0, this.mMessenger);
                if (getResources().getConfiguration().orientation != 1) {
                    msg.arg1 = 0;
                    restoreInterfaceFrommTransparent();
                }
                try {
                    this.mCustomAnimMessenger.send(msg);
                } catch (RemoteException e) {
                    HwLog.e("ContactEditorFragment", "onActivityLaunchedFirst,sent message failed");
                    restoreInterfaceFrommTransparent();
                }
            }
        }
    }

    private void setInterfaceTransparent() {
        this.mCustAnimationListener.setActivityTransparent();
    }

    private void restoreInterfaceFrommTransparent() {
        this.mCustAnimationListener.restoreActivityFromTransparent();
    }

    public ViewGroup getEditorHead() {
        if (this.mContactEditor != null) {
            return this.mContactEditor.getEditHead();
        }
        return null;
    }

    public void scrollToTopSmoothly() {
        if (this.mScrollView != null) {
            this.mScrollView.smoothScrollTo(0, 0);
        }
    }

    public void setFragmentStatus(int status) {
        this.mStatus = status;
    }

    public boolean isRebindNewContact() {
        return this.mRebindNewContact;
    }

    private void hideCompanyPop() {
        if (this.mContactEditor != null && (this.mContactEditor instanceof RawContactEditorView)) {
            ((RawContactEditorView) this.mContactEditor).hideCompanyPop();
        }
    }

    public RawContactEditorView getRawContactEditorView() {
        if (this.mContactEditor instanceof RawContactEditorView) {
            return (RawContactEditorView) this.mContactEditor;
        }
        return null;
    }

    public void showAlertDialog(ViewGroup anchorView, final RawContactDelta currentState, final AccountWithDataSet currentAccount) {
        Builder builer = new Builder(this.mContext);
        if (this.mAlertDialog == null || !this.mAlertDialog.isShowing()) {
            AccountsListAdapter adapter;
            hideSoftInputWindow();
            if (this.mEditorUtils.isExcludeSim()) {
                adapter = new AccountsListAdapter(this.mContext, AccountListFilter.ACCOUNTS_EXCLUDE_SIM, currentAccount, true);
            } else if (this.mExcludeSim1 && this.mExcludeSim2) {
                adapter = new AccountsListAdapter(this.mContext, AccountListFilter.ACCOUNTS_EXCLUDE_SIM, currentAccount, true);
            } else if (this.mExcludeSim1) {
                adapter = new AccountsListAdapter(this.mContext, AccountListFilter.ACCOUNTS_EXCLUDE_SIM1, currentAccount, true);
            } else if (this.mExcludeSim2) {
                adapter = new AccountsListAdapter(this.mContext, AccountListFilter.ACCOUNTS_EXCLUDE_SIM2, currentAccount, true);
            } else {
                adapter = new AccountsListAdapter(this.mContext, AccountListFilter.ACCOUNTS_CONTACT_WRITABLE, currentAccount, true);
            }
            builer.setAdapter(adapter, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    AccountWithDataSet newAccount = adapter.getItem(which);
                    if ("com.android.huawei.sim".equals(newAccount.type) && !SimFactoryManager.isSIM1CardPresent()) {
                        return;
                    }
                    if (!"com.android.huawei.secondsim".equals(newAccount.type) || SimFactoryManager.isSIM2CardPresent()) {
                        if (!newAccount.equals(currentAccount)) {
                            adapter.setCurrentAccount(newAccount);
                            ContactEditorFragment.this.rebindEditorsForNewContact(currentState, currentAccount, newAccount);
                        }
                        dialog.dismiss();
                    }
                }
            });
            this.mAlertDialog = builer.create();
            this.mAlertDialog.show();
        }
    }

    private void addMargin() {
        if (this.mContactEditor != null && this.mContext != null) {
            View view = this.mContactEditor.findViewById(R.id.body);
            if (view != null) {
                ContactEditorUtils.setViewMargin(this.mContext, view, this.mContext.getResources().getConfiguration().orientation);
            }
        }
    }
}
