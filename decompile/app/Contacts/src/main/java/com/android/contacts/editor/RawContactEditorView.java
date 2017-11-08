package com.android.contacts.editor;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.contacts.GroupMetaData;
import com.android.contacts.activities.ContactEditorActivity;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.editor.ContactEditorFragment.RingtoneListener;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.hap.editor.RingtoneEditorView;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.RawContactModifier;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountType.EditType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.HiCloudUtil;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.LunarUtils;
import com.android.contacts.util.Objects;
import com.android.contacts.util.PhoneCapabilityTester;
import com.google.android.gms.R;
import com.google.common.collect.ImmutableList;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.HashSet;

public class RawContactEditorView extends BaseRawContactEditorView {
    static int anniversaryPos = -1;
    static int birthdayPos = -1;
    static int importantDatePos = -1;
    static int lunarBirthdayPos = -1;
    AlertDialog dialog;
    KindSectionView eventView = null;
    private int mAccountContainerMargin;
    private String mAccountType;
    private LinearLayout mAccountsContainer;
    private View mAccountsFromText;
    private Button mAddFieldButton;
    private boolean mAutoAddToDefaultGroup = true;
    private int mCurrentMaxOrganizedNameWidth = 0;
    private int mCurrentMaxPhoneticNameWidth = 0;
    private int mCurrentMaxStructuredNameWidth = 0;
    private HwCustRawContactEditorView mCust = null;
    private LinearLayout mEditNameContanier;
    private ViewGroup mFields;
    private ContactEditorFragment mFragment;
    private RawContactDeltaList mGroupEditableList;
    private DataKind mGroupMembershipKind;
    private GroupMembershipView mGroupMembershipView;
    private Cursor mGroupMetaData;
    private Handler mHandler = null;
    private LayoutInflater mInflater;
    private Intent mIntent;
    private boolean mIsDelayedLoadingItemExist = false;
    private boolean mIsQueryCompanyInfo = true;
    private StructuredNameEditorView mName;
    private boolean mOnlyOneEditText = false;
    private TextFieldsEditorView mOrganisationName;
    private PhoneticNameEditorView mPhoneticName;
    private boolean mPhoneticNameAdded;
    private long mRawContactId = -1;
    private Runnable mRunnableForDelay = null;
    private ContactEditorScrollView mScrollView;
    private RawContactDelta mState;
    private RawContactDeltaList mStateList;
    private LinearLayout mTitleContainer;
    private TextView mTitleTextView;
    private boolean mViewDelayedLoadingSwitch = false;
    private RawContactDeltaList mWritableList;
    private boolean stepIntoOnce = true;
    private AccountType typeLocal;
    private ViewIdGenerator vigLocal;

    private static class SavedState implements Parcelable {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public boolean mPhoneticNameAdded;
        public Parcelable mSuperState;

        SavedState(Parcelable superState) {
            this.mSuperState = superState;
        }

        private SavedState(Parcel in) {
            boolean z = false;
            this.mSuperState = in.readParcelable(getClass().getClassLoader());
            if (in.readInt() != 0) {
                z = true;
            }
            this.mPhoneticNameAdded = z;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            int i = 0;
            out.writeParcelable(this.mSuperState, 0);
            if (this.mPhoneticNameAdded) {
                i = 1;
            }
            out.writeInt(i);
        }
    }

    public RawContactEditorView(Context context) {
        super(context);
    }

    public RawContactEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        View view = getPhotoEditor();
        if (view != null) {
            view.setEnabled(enabled);
        }
        if (this.mName != null) {
            this.mName.setEnabled(enabled);
        }
        if (this.mPhoneticName != null) {
            this.mPhoneticName.setEnabled(enabled);
        }
        if (this.mOrganisationName != null) {
            this.mOrganisationName.setEnabled(enabled);
        }
        if (this.mFields != null) {
            int count = this.mFields.getChildCount();
            for (int i = 0; i < count; i++) {
                this.mFields.getChildAt(i).setEnabled(enabled);
            }
        }
        if (this.mGroupMembershipView != null) {
            this.mGroupMembershipView.setEnabled(enabled);
        }
        this.mAddFieldButton.setEnabled(enabled);
    }

    private void handleAddFieldItemClick(KindSectionView aView, boolean aHideButton) {
        if ("#phoneticName".equals(aView.getKind().mimeType)) {
            this.mPhoneticNameAdded = true;
            updatePhoneticNameVisibility();
            this.mPhoneticName.requestFocus();
            EditText firstEditField = this.mPhoneticName.getFirstEditField();
            InputMethodManager lManager = (InputMethodManager) getContext().getSystemService("input_method");
            if (firstEditField != null) {
                lManager.hideSoftInputFromWindow(firstEditField.getWindowToken(), 1);
            }
            lManager.toggleSoftInput(1, 1);
        } else {
            aView.addItem(true, false);
            if ("vnd.android.huawei.cursor.item/ringtone".equals(aView.getKind().mimeType)) {
                this.mRingtone = aView.getRingtoneView();
                RingtoneListener ringtoneListener = this.mFragment.getRingtoneListener(this.mRingtone);
                this.mRingtone.setEditorListener(ringtoneListener);
                if (ringtoneListener != null) {
                    ringtoneListener.onRequest(6);
                }
            }
        }
        if (aHideButton) {
            setAddButtonVisibility(8);
        }
        updateAdditionButtonVisibilityState();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mInflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        this.mEditNameContanier = (LinearLayout) findViewById(R.id.edit_name_container);
        this.mName = (StructuredNameEditorView) findViewById(R.id.edit_name);
        this.mName.setDeletable(false);
        this.mName.setRawContactEditorView(this);
        this.mPhoneticName = (PhoneticNameEditorView) findViewById(R.id.edit_phonetic_name);
        this.mPhoneticName.setDeletable(false);
        this.mPhoneticName.setRawContactEditorView(this);
        this.mOrganisationName = (TextFieldsEditorView) findViewById(R.id.edit_organisation);
        this.mOrganisationName.setDeletable(false);
        this.mOrganisationName.setRawContactEditorView(this);
        this.mFields = (ViewGroup) findViewById(R.id.sect_fields);
        this.mAccountsFromText = findViewById(R.id.accounts_hint);
        this.mAccountsContainer = (LinearLayout) findViewById(R.id.accounts_container);
        this.mScrollView = (ContactEditorScrollView) getScrollView();
        this.mTitleContainer = (LinearLayout) findViewById(R.id.editor_accout_types);
        if (this.mTitleContainer != null) {
            this.mTitleTextView = (TextView) this.mTitleContainer.findViewById(R.id.account_type);
            int color = ImmersionUtils.getControlColor(getResources());
            if (color != 0) {
                this.mTitleTextView.setTextColor(color);
            }
        }
        this.mAccountContainerMargin = getContext().getResources().getDimensionPixelSize(R.dimen.contact_editor_account_margin);
        this.mAddFieldButton = (Button) findViewById(R.id.button_add_field);
        this.mAddFieldButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (RawContactEditorView.this.stepIntoOnce) {
                    RawContactEditorView.this.inflateXmlWithDelay();
                    RawContactEditorView.this.stepIntoOnce = false;
                }
                StatisticalHelper.report(2006);
                final ArrayList<ContactEditorInfo> lViews = RawContactEditorView.this.getSectionViewsWithoutFields();
                ArrayList<String> lFields = new ArrayList();
                for (int i = 0; i < lViews.size(); i++) {
                    if (!((ContactEditorInfo) lViews.get(i)).getKind().mimeType.equals("vnd.android.cursor.item/contact_event")) {
                        lFields.add(((ContactEditorInfo) lViews.get(i)).getTitle());
                    } else if (i == RawContactEditorView.birthdayPos) {
                        lFields.add(RawContactEditorView.this.getResources().getString(Event.getTypeResource(Integer.valueOf(3))));
                    } else if (i == RawContactEditorView.anniversaryPos) {
                        lFields.add(RawContactEditorView.this.getResources().getString(Event.getTypeResource(Integer.valueOf(1))));
                    } else if (i == RawContactEditorView.importantDatePos) {
                        lFields.add(RawContactEditorView.this.getResources().getString(R.string.event_important_date));
                    } else if (i == RawContactEditorView.lunarBirthdayPos) {
                        lFields.add(RawContactEditorView.this.getResources().getString(R.string.event_lunar_birthday));
                    }
                }
                RawContactEditorView.this.dialog = new Builder(RawContactEditorView.this.getContext()).setAdapter(new ArrayAdapter(RawContactEditorView.this.getContext(), R.layout.select_dialog_item, lFields), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        boolean z = true;
                        ContactEditorInfo view = (ContactEditorInfo) lViews.get(which);
                        RawContactEditorView rawContactEditorView;
                        if (view.getKind().mimeType == "vnd.android.cursor.item/contact_event") {
                            RawContactEditorView.this.eventView = (KindSectionView) view;
                            RawContactEditorView.this.eventView.getKind().shouldShowDatePicker = true;
                            if (which == RawContactEditorView.birthdayPos) {
                                RawContactEditorView.this.eventView.getKind().typeToSelect = 3;
                            } else if (which == RawContactEditorView.anniversaryPos) {
                                RawContactEditorView.this.eventView.getKind().typeToSelect = 1;
                            } else if (which == RawContactEditorView.importantDatePos) {
                                RawContactEditorView.this.eventView.getKind().typeToSelect = 2;
                            } else if (which == RawContactEditorView.lunarBirthdayPos) {
                                RawContactEditorView.this.eventView.getKind().typeToSelect = 4;
                            }
                            rawContactEditorView = RawContactEditorView.this;
                            KindSectionView kindSectionView = RawContactEditorView.this.eventView;
                            if (lViews.size() != 1) {
                                z = false;
                            }
                            rawContactEditorView.handleAddFieldItemClick(kindSectionView, z);
                        } else if (view instanceof KindSectionView) {
                            rawContactEditorView = RawContactEditorView.this;
                            KindSectionView kindSectionView2 = (KindSectionView) view;
                            if (lViews.size() != 1) {
                                z = false;
                            }
                            rawContactEditorView.handleAddFieldItemClick(kindSectionView2, z);
                        } else if (view == RawContactEditorView.this.mGroupMembershipView) {
                            RawContactEditorView.this.mGroupMembershipView.setVisibility(0);
                            RawContactEditorView.this.mGroupMembershipView.onClick(RawContactEditorView.this.mGroupMembershipView);
                        } else if (view == RawContactEditorView.this.mRingtone) {
                            RawContactEditorView.this.mRingtone.setVisibility(0);
                            RawContactEditorView.this.mRingtone.onClick(RawContactEditorView.this.mRingtone);
                        }
                    }
                }).create();
                RawContactEditorView.this.dialog.show();
            }
        });
        if ((getContext() instanceof ContactEditorActivity) && ((ContactEditorActivity) getContext()).isInMultiWindowMode()) {
            setAddFieldButtonWith();
        }
    }

    public void inflateXmlWithDelay() {
        if (this.mViewDelayedLoadingSwitch) {
            if (this.mCust != null) {
                this.mCust.removeViews(this.mFields, this.mVibration);
            }
            if (this.typeLocal != null) {
                for (DataKind kind : this.typeLocal.getSortedDataKinds()) {
                    if (kind.editable) {
                        String mimeType = kind.mimeType;
                        if ("vnd.android.cursor.item/im".equals(mimeType) || "vnd.android.cursor.item/postal-address_v2".equals(mimeType) || "vnd.android.cursor.item/nickname".equals(mimeType) || "vnd.android.cursor.item/website".equals(mimeType) || "vnd.android.cursor.item/contact_event".equals(mimeType) || "vnd.android.cursor.item/relation".equals(mimeType)) {
                            if (kind.fieldList != null) {
                                KindSectionView section = (KindSectionView) this.mInflater.inflate(R.layout.item_kind_section, this.mFields, false);
                                section.setState(kind, this.mStateList, this.mState, false, this.vigLocal);
                                section.setEnabled(isEnabled());
                                this.mFields.addView(section);
                            }
                        } else if ("vnd.android.cursor.item/group_membership".equals(mimeType)) {
                            handleGroupMembershipView(this.mState, this.typeLocal);
                        } else if ("vnd.android.huawei.cursor.item/ringtone".equals(mimeType) && this.mRingtone == null) {
                            handleRingtone(kind, this.mState, this.typeLocal, this.vigLocal, false, false);
                        }
                    }
                }
                if (this.mCust != null) {
                    if (this.mVibration == null) {
                        this.mVibration = this.mCust.inflateNewViews(getContext(), this.mInflater, this.mFragment, this.mFields, getRawContactId());
                    }
                    this.mCust.addViews(this.mFields, this.mVibration);
                }
                this.mIsDelayedLoadingItemExist = false;
            }
        }
    }

    private boolean checkSetState(RawContactDeltaList stateList, RawContactDelta state, AccountType type) {
        return stateList == null || stateList.isEmpty() || state == null || type == null;
    }

    public void setState(RawContactDeltaList stateList, RawContactDelta state, AccountType type, ViewIdGenerator vig, boolean isProfile) {
        this.mStateList = stateList;
        this.mState = state;
        this.vigLocal = vig;
        this.typeLocal = type;
        this.mFields.removeAllViews();
        if (!checkSetState(stateList, state, type)) {
            int numRawContacts = this.mStateList.size();
            setId(vig.getId(state, null, null, -1));
            AccountTypeManager accountTypes = AccountTypeManager.getInstance(getContext());
            if (numRawContacts > 1) {
                RawContactModifier.ensureKindExists(accountTypes, stateList, state, type, "vnd.android.cursor.item/name");
                RawContactModifier.ensureKindExists(accountTypes, stateList, state, type, "vnd.android.cursor.item/organization");
                RawContactModifier.ensureKindExists(accountTypes, stateList, state, type, "vnd.android.cursor.item/phone_v2");
            } else {
                RawContactModifier.ensureKindExists(state, type, "vnd.android.cursor.item/name");
                RawContactModifier.ensureKindExists(state, type, "vnd.android.cursor.item/organization");
                RawContactModifier.ensureKindExists(state, type, "vnd.android.cursor.item/phone_v2");
            }
            if (!CommonUtilMethods.isSimplifiedModeEnabled()) {
                if (numRawContacts > 1) {
                    RawContactModifier.ensureKindExists(accountTypes, stateList, state, type, "vnd.android.cursor.item/email_v2");
                    RawContactModifier.ensureKindExists(accountTypes, stateList, state, type, "vnd.android.cursor.item/note");
                } else {
                    RawContactModifier.ensureKindExists(state, type, "vnd.android.cursor.item/email_v2");
                    RawContactModifier.ensureKindExists(state, type, "vnd.android.cursor.item/note");
                }
            }
            this.mRawContactId = state.getRawContactId().longValue();
            boolean isCreateNewContact = "android.intent.action.INSERT".equals(this.mFragment.getAction());
            if (isCreateNewContact) {
                handleAccountSelectionDisplay(state, type, isProfile);
            } else {
                handleAccountsContainerDisplay(state, type, isProfile);
            }
            if (this.mFragment != null) {
                ActionBar mActionBar = this.mFragment.getActivity().getActionBar();
                if (isCreateNewContact) {
                    mActionBar.setTitle(getContext().getString(R.string.menu_newContact));
                } else {
                    mActionBar.setTitle(getContext().getString(R.string.edit_contact));
                }
            }
            this.mAccountType = state.getAccountType();
            if (CommonUtilMethods.isSimAccount(this.mAccountType)) {
                getEditHead().setVisibility(8);
                this.mOnlyOneEditText = true;
            } else {
                getEditHead().setVisibility(0);
                this.mOnlyOneEditText = false;
            }
            if (this.mWritableList == null || this.mWritableList.isEmpty()) {
                RawContactModifier.ensureKindExists(state, type, "vnd.android.cursor.item/photo");
            } else {
                for (RawContactDelta writeState : this.mWritableList) {
                    RawContactModifier.ensureKindExists(writeState, writeState.getAccountType(accountTypes), "vnd.android.cursor.item/photo");
                }
            }
            setHasPhotoEditor(type.getKindForMimetype("vnd.android.cursor.item/photo") != null);
            getPhotoEditor().setEnabled(isEnabled());
            this.mFields.setVisibility(0);
            setNameVisibility();
            setLayout(isProfile);
            if (this.mIntent != null) {
                this.mViewDelayedLoadingSwitch = this.mIntent.getBooleanExtra("ViewDelayedLoadingSwitch", false);
            }
            if (!handleRawContactEditor(isCreateNewContact, state, type, vig, isProfile)) {
                String mimeType;
                ArrayList<DataKind> firstHandle = new ArrayList();
                ArrayList<DataKind> delayHandle = new ArrayList();
                for (DataKind kind : type.getSortedDataKinds()) {
                    if (kind.editable) {
                        mimeType = kind.mimeType;
                        if ("vnd.android.cursor.item/name".equals(mimeType)) {
                            firstHandle.add(kind);
                        } else if ("vnd.android.cursor.item/photo".equals(mimeType)) {
                            firstHandle.add(kind);
                        } else if ("vnd.android.cursor.item/organization".equals(mimeType)) {
                            firstHandle.add(kind);
                        } else if ("#phoneticName".equals(mimeType)) {
                            firstHandle.add(kind);
                        } else if ("vnd.android.cursor.item/phone_v2".equals(mimeType)) {
                            firstHandle.add(kind);
                        } else if ("vnd.android.cursor.item/email_v2".equals(mimeType)) {
                            firstHandle.add(kind);
                        } else if ("vnd.android.cursor.item/note".equals(mimeType)) {
                            firstHandle.add(kind);
                        } else if (kind.fieldList != null) {
                            if (this.mViewDelayedLoadingSwitch && ("vnd.android.cursor.item/im".equals(mimeType) || "vnd.android.cursor.item/postal-address_v2".equals(mimeType) || "vnd.android.cursor.item/nickname".equals(mimeType) || "vnd.android.cursor.item/website".equals(mimeType) || "vnd.android.cursor.item/contact_event".equals(mimeType) || "vnd.android.cursor.item/relation".equals(mimeType) || "vnd.android.cursor.item/group_membership".equals(mimeType) || "vnd.android.huawei.cursor.item/ringtone".equals(mimeType))) {
                                this.mIsDelayedLoadingItemExist = true;
                            } else {
                                delayHandle.add(kind);
                            }
                        }
                    }
                }
                for (DataKind kind2 : firstHandle) {
                    if (kind2.editable) {
                        mimeType = kind2.mimeType;
                        if ("vnd.android.cursor.item/name".equals(mimeType)) {
                            handleStructuredName(mimeType, type, state, vig);
                        } else if ("vnd.android.cursor.item/photo".equals(mimeType)) {
                            handlePhoto(kind2, state, vig);
                        } else if ("vnd.android.cursor.item/organization".equals(mimeType)) {
                            handleOrganisation(kind2, state, type, vig);
                        } else if ("vnd.android.cursor.item/phone_v2".equals(mimeType) || "#phoneticName".equals(mimeType)) {
                            handleDefault(mimeType, kind2, state, type, vig, isProfile);
                        } else if ("vnd.android.cursor.item/note".equals(mimeType)) {
                            handleDefault(mimeType, kind2, state, type, vig, isProfile);
                        } else if ("vnd.android.cursor.item/email_v2".equals(mimeType)) {
                            handleDefault(mimeType, kind2, state, type, vig, isProfile);
                        }
                    }
                }
                this.mHandler = new Handler();
                final ArrayList<DataKind> arrayList = delayHandle;
                final RawContactDelta rawContactDelta = state;
                final AccountType accountType = type;
                final ViewIdGenerator viewIdGenerator = vig;
                final boolean z = isProfile;
                this.mRunnableForDelay = new Runnable() {
                    public void run() {
                        for (DataKind kind : arrayList) {
                            if (kind.editable) {
                                String mimeType = kind.mimeType;
                                if ("vnd.android.cursor.item/group_membership".equals(mimeType)) {
                                    RawContactEditorView.this.handleGroupMembershipView(rawContactDelta, accountType);
                                } else if ("vnd.android.huawei.cursor.item/ringtone".equals(mimeType)) {
                                    RawContactEditorView.this.handleRingtone(kind, rawContactDelta, accountType, viewIdGenerator, z, false);
                                } else {
                                    RawContactEditorView.this.handleDefault(mimeType, kind, rawContactDelta, accountType, viewIdGenerator, z);
                                }
                            }
                        }
                        RawContactEditorView.this.handleFinal(rawContactDelta, accountType, viewIdGenerator);
                    }
                };
                this.mHandler.postDelayed(this.mRunnableForDelay, 800);
            }
        }
    }

    private void setLayout(boolean isProfile) {
        if (CommonUtilMethods.isSimplifiedModeEnabled() && ("com.android.huawei.phone".equalsIgnoreCase(this.mAccountType) || HwCustCommonConstants.EAS_ACCOUNT_TYPE.equalsIgnoreCase(this.mAccountType) || isProfile)) {
            this.mEditNameContanier.setPadding(getPaddingLeft(), getResources().getDimensionPixelSize(R.dimen.editor_simple_name_paddingtop), getPaddingRight(), 0);
        }
        if (CommonUtilMethods.isSimAccount(this.mAccountType)) {
            LayoutParams params = (LayoutParams) this.mName.getLayoutParams();
            params.setMarginStart(getResources().getDimensionPixelSize(R.dimen.editor_item_image_type_width));
            this.mName.setLayoutParams(params);
            if (this.mAccountsContainer != null) {
                LayoutParams accountsContainerlayoutParams = (LayoutParams) this.mAccountsContainer.getLayoutParams();
                accountsContainerlayoutParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen.contact_editor_item_left_margin));
                this.mAccountsContainer.setLayoutParams(accountsContainerlayoutParams);
            }
            if (this.mAccountsFromText != null) {
                LayoutParams accountsFromTextlayoutParams = (LayoutParams) this.mAccountsFromText.getLayoutParams();
                accountsFromTextlayoutParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen.contact_editor_item_left_margin));
                this.mAccountsFromText.setLayoutParams(accountsFromTextlayoutParams);
            }
        }
    }

    private boolean handleRawContactEditor(boolean isCreateNewContact, RawContactDelta state, AccountType type, ViewIdGenerator vig, boolean isProfile) {
        if (isCreateNewContact && !this.mOnlyOneEditText && !this.mFragment.isRebindNewContact()) {
            return false;
        }
        for (DataKind kind : type.getSortedDataKinds()) {
            if (kind.editable) {
                String mimeType = kind.mimeType;
                if ("vnd.android.cursor.item/name".equals(mimeType)) {
                    handleStructuredName(mimeType, type, state, vig);
                } else if ("vnd.android.cursor.item/photo".equals(mimeType)) {
                    handlePhoto(kind, state, vig);
                } else if ("vnd.android.cursor.item/group_membership".equals(mimeType) && !this.mFragment.isRebindNewContact()) {
                    handleGroupMembershipView(state, type);
                } else if ("vnd.android.cursor.item/organization".equals(mimeType)) {
                    handleOrganisation(kind, state, type, vig);
                } else if ("vnd.android.huawei.cursor.item/ringtone".equals(mimeType)) {
                    handleRingtone(kind, state, type, vig, isProfile, this.mOnlyOneEditText);
                } else if (kind.fieldList != null) {
                    if (this.mViewDelayedLoadingSwitch && ("vnd.android.cursor.item/im".equals(mimeType) || "vnd.android.cursor.item/postal-address_v2".equals(mimeType) || "vnd.android.cursor.item/note".equals(mimeType) || "vnd.android.cursor.item/nickname".equals(mimeType) || "vnd.android.cursor.item/website".equals(mimeType) || "vnd.android.cursor.item/contact_event".equals(mimeType) || "vnd.android.cursor.item/relation".equals(mimeType))) {
                        this.mIsDelayedLoadingItemExist = true;
                    } else {
                        handleDefault(mimeType, kind, state, type, vig, isProfile);
                    }
                }
            }
        }
        handleFinal(state, type, vig);
        return true;
    }

    private void setNameVisibility() {
        this.mName.setEnabled(isEnabled());
        if (getContext().getResources().getBoolean(R.bool.config_editor_phonetic_show_expansion)) {
            this.mPhoneticName.setEnabled(isEnabled());
        } else {
            this.mPhoneticName.hideExpansuinView();
        }
        this.mOrganisationName.setEnabled(isEnabled());
        this.mName.setVisibility(0);
        this.mPhoneticName.setVisibility(0);
        this.mOrganisationName.setVisibility(8);
    }

    private void handleAccountSelectionDisplay(RawContactDelta state, AccountType type, boolean isProfile) {
        CharSequence accountType;
        if (!isProfile) {
            accountType = type.getDisplayLabel(getContext());
            if (this.mTitleTextView != null) {
                this.mTitleTextView.setText(getUpperString(getContext().getString(R.string.contact_editor_new_item_format, new Object[]{accountType})));
            }
        } else if (!TextUtils.isEmpty(state.getAccountName())) {
            accountType = type.getDisplayLabel(getContext());
            String text = getContext().getString(R.string.external_profile_title, new Object[]{accountType});
            if (this.mTitleTextView != null) {
                this.mTitleTextView.setText(getUpperString(text));
            }
        } else if (this.mTitleTextView != null) {
            this.mTitleTextView.setText(getUpperString(getContext().getString(R.string.local_profile_title)));
        }
    }

    private String getUpperString(String text) {
        if (text != null) {
            return text.toUpperCase().trim();
        }
        return text;
    }

    private void handleAccountsContainerDisplay(RawContactDelta state, AccountType type, boolean isProfile) {
        TextView accountTypeTextView;
        if (isProfile) {
            LinearLayout accoutContainer = (LinearLayout) this.mInflater.inflate(R.layout.editor_source_account_container, this.mAccountsContainer, false);
            this.mAccountsContainer.addView(accoutContainer);
            accountTypeTextView = (TextView) accoutContainer.findViewById(R.id.account_type);
            ((TextView) accoutContainer.findViewById(R.id.account_name)).setVisibility(8);
            if (TextUtils.isEmpty(state.getAccountName())) {
                accountTypeTextView.setText(R.string.local_profile_title);
                return;
            }
            CharSequence accountType = type.getDisplayLabel(getContext());
            accountTypeTextView.setText(getContext().getString(R.string.external_profile_title, new Object[]{accountType}));
            return;
        }
        for (AccountWithDataSet account : getAccountsList()) {
            accoutContainer = (LinearLayout) this.mInflater.inflate(R.layout.editor_source_account_container, this.mAccountsContainer, false);
            this.mAccountsContainer.addView(accoutContainer);
            accountTypeTextView = (TextView) accoutContainer.findViewById(R.id.account_type);
            TextView accountNameTextView = (TextView) accoutContainer.findViewById(R.id.account_name);
            CharSequence accountTypeText = AccountTypeManager.getInstance(getContext()).getAccountType(account.type, account.dataSet).getDisplayLabel(getContext());
            if (HwLog.HWDBG) {
                HwLog.d("RawContactEditorView.java", "from displayLaybel, accountType=" + accountTypeText);
            }
            if (TextUtils.isEmpty(account.name)) {
                accountNameTextView.setVisibility(8);
            } else if (CommonUtilMethods.isLocalDefaultAccount(account.type)) {
                if (HiCloudUtil.isHiCloudAccountLogOn()) {
                    accountNameTextView.setVisibility(0);
                    accountNameTextView.setText(HiCloudUtil.getHiCloudAccountName());
                } else {
                    accountNameTextView.setVisibility(8);
                }
            } else if (CommonUtilMethods.isSimAccount(account.type) || account.name.equalsIgnoreCase(accountTypeText.toString())) {
                accountNameTextView.setVisibility(8);
            } else {
                accountTypeTextView.setVisibility(0);
                accountNameTextView.setVisibility(0);
                accountNameTextView.setText(account.name);
            }
            accountTypeTextView.setText(accountTypeText);
        }
    }

    private void handleStructuredName(String mimeType, AccountType type, RawContactDelta state, ViewIdGenerator vig) {
        int editorIndex;
        ValuesDelta primary = state.getPrimaryEntry(mimeType);
        this.mName.setAccountType(type.accountType);
        if (CommonUtilMethods.isSimAccount(type.accountType)) {
            this.mName.setIsSimAccount(true);
        } else {
            this.mName.setIsSimAccount(false);
        }
        boolean valueHasBeenSet = false;
        if (primary != null) {
            this.mName.setValues(type.getKindForMimetype("#displayName"), primary, state, false, vig);
            valueHasBeenSet = true;
        }
        int numRawContacts = this.mStateList.size();
        for (editorIndex = 0; editorIndex < numRawContacts; editorIndex++) {
            RawContactDelta rawContactDelta = (RawContactDelta) this.mStateList.get(editorIndex);
            ValuesDelta primaryEntry = rawContactDelta.getPrimaryEntry(mimeType);
            if (primaryEntry != null) {
                this.mName.addEntry(primaryEntry);
                if (!valueHasBeenSet) {
                    this.mName.setValues(type.getKindForMimetype("#displayName"), primaryEntry, state, false, vig);
                    valueHasBeenSet = true;
                }
            } else {
                RawContactModifier.ensureKindExists(rawContactDelta, rawContactDelta.getAccountType(AccountTypeManager.getInstance(getContext())), mimeType);
                ValuesDelta entry = rawContactDelta.getPrimaryEntry(mimeType);
                if (entry != null) {
                    this.mName.addEntry(entry);
                }
            }
        }
        if (CommonUtilMethods.isSimAccount(type.accountType) || "com.huawei.himessage".equals(type.accountType)) {
            if ("com.huawei.himessage".equals(type.accountType) && primary != null) {
                this.mName.eraseStructuredName(primary);
                primary.put("data1", primary.getAsString("data1"));
                EditText lNameEditor = this.mName.getFirstEditField();
                if (lNameEditor != null) {
                    lNameEditor.setFilters(new InputFilter[]{CommonUtilMethods.getProfileInputFilter()});
                }
            }
            this.mPhoneticName.setVisibility(8);
        } else {
            valueHasBeenSet = false;
            if (primary != null) {
                this.mPhoneticName.setValues(type.getKindForMimetype("#phoneticName"), primary, state, false, vig);
                valueHasBeenSet = true;
            }
            if (!(this.mWritableList == null || this.mWritableList.isEmpty())) {
                for (editorIndex = 0; editorIndex < this.mWritableList.size(); editorIndex++) {
                    primaryEntry = ((RawContactDelta) this.mWritableList.get(editorIndex)).getPrimaryEntry(mimeType);
                    if (primaryEntry != null) {
                        this.mPhoneticName.addEntry(primaryEntry);
                        if (!valueHasBeenSet) {
                            this.mPhoneticName.setValues(type.getKindForMimetype("#phoneticName"), primaryEntry, state, false, vig);
                            valueHasBeenSet = true;
                        }
                    }
                }
            }
            updatePhoneticNameVisibility();
        }
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCust = (HwCustRawContactEditorView) HwCustUtils.createObj(HwCustRawContactEditorView.class, new Object[0]);
        }
    }

    private void handlePhoto(DataKind kind, RawContactDelta state, ViewIdGenerator vig) {
        ValuesDelta primary = state.getPrimaryEntry("vnd.android.cursor.item/photo");
        kind.setmBitmapFromDetailCard(getBitmapFromDetail());
        getPhotoEditor().setValues(kind, primary, state, false, vig);
        if (this.mWritableList != null && !this.mWritableList.isEmpty()) {
            for (RawContactDelta writeState : this.mWritableList) {
                ValuesDelta entry = writeState.getPrimaryEntry("vnd.android.cursor.item/photo");
                if (entry != null) {
                    getPhotoEditor().addEntry(entry);
                }
            }
        }
    }

    private void handleGroupMembershipView(RawContactDelta state, AccountType type) {
        if (!CommonUtilMethods.isSimplifiedModeEnabled()) {
            this.mGroupMembershipKind = type.getKindForMimetype("vnd.android.cursor.item/group_membership");
            if (this.mGroupMembershipKind != null) {
                this.mGroupMembershipView = (GroupMembershipView) this.mInflater.inflate(R.layout.item_group_membership, this.mFields, false);
                this.mGroupMembershipView.setEnabled(isEnabled());
                this.mGroupMembershipView.setKind(this.mGroupMembershipKind);
            }
            if (this.mGroupMembershipView != null) {
                this.mGroupMembershipView.setFragment(this.mFragment);
                if (this.mGroupEditableList == null || this.mGroupEditableList.isEmpty()) {
                    this.mGroupMembershipView.setState(this.mState);
                } else {
                    this.mGroupMembershipView.setState(this.mGroupEditableList);
                }
                this.mGroupMembershipView.setGroupMetaData(this.mGroupMetaData);
                this.mFields.addView(this.mGroupMembershipView);
                this.mGroupMembershipView.setVisibility(8);
            }
        }
    }

    private void handleOrganisation(DataKind kind, RawContactDelta state, AccountType type, ViewIdGenerator vig) {
        if (CommonUtilMethods.isSimplifiedModeEnabled()) {
            this.mOrganisationName.setVisibility(8);
            return;
        }
        this.mOrganisationName.setVisibility(0);
        ValuesDelta primary = state.getPrimaryEntry("vnd.android.cursor.item/organization");
        boolean valueHasBeenSet = false;
        if (primary != null) {
            this.mOrganisationName.setValues(kind, primary, state, false, vig);
            valueHasBeenSet = true;
        }
        int numRawContacts = this.mStateList.size();
        for (int editorIndex = 0; editorIndex < numRawContacts; editorIndex++) {
            ValuesDelta primaryEntry = ((RawContactDelta) this.mStateList.get(editorIndex)).getPrimaryEntry("vnd.android.cursor.item/organization");
            if (primaryEntry != null) {
                this.mOrganisationName.addEntry(primaryEntry);
                if (!valueHasBeenSet) {
                    this.mOrganisationName.setValues(kind, primaryEntry, state, false, vig);
                    valueHasBeenSet = true;
                }
            }
        }
        if (this.mCust != null) {
            this.mCust.hideTextFieldsEditorView(this.mOrganisationName);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleRingtone(DataKind kind, RawContactDelta state, AccountType type, ViewIdGenerator vig, boolean isProfile, boolean showSection) {
        int i = 0;
        if (!isProfile && EmuiFeatureManager.isSystemVoiceCapable() && QueryUtil.isHAPProviderInstalled()) {
            RingtoneEditorView section = (RingtoneEditorView) this.mInflater.inflate(R.layout.listitem_ringtone, this.mFields, false);
            section.setValues(kind, state.getValues(), this.mStateList, state, !type.areContactsWritable(), vig);
            this.mRingtone = section;
            this.mRingtone.setEditorListener(this.mFragment.getRingtoneListener(this.mRingtone));
            this.mRingtone.setDataKind(kind);
            this.mFields.addView(this.mRingtone);
            RingtoneEditorView ringtoneEditorView = this.mRingtone;
            if (!showSection && this.mRingtone.getRingtone() == null) {
                i = 8;
            }
            ringtoneEditorView.setVisibility(i);
        }
    }

    private void handleDefault(String mimeType, DataKind kind, RawContactDelta state, AccountType type, ViewIdGenerator vig, boolean isProfile) {
        if (!isProfile || !"vnd.android.huawei.cursor.item/ringtone".equals(mimeType)) {
            if ("vnd.android.cursor.item/group_membership".equals(mimeType)) {
                handleGroupMembershipView(state, type);
                return;
            }
            KindSectionView section = (KindSectionView) this.mInflater.inflate(R.layout.item_kind_section, this.mFields, false);
            if (section.mHwCustContactEditorCustomizationObj != null) {
                section.mHwCustContactEditorCustomizationObj.addSectionViewProperty(isProfile);
            }
            if (CommonUtilMethods.isSimAccount(type.accountType)) {
                section.setAccountType(type.accountType);
                section.setIsSimAccount(true);
            } else {
                section.setIsSimAccount(false);
            }
            section.setState(kind, this.mStateList, state, false, vig);
            section.setEnabled(isEnabled());
            this.mFields.addView(section);
        }
    }

    private void handleFinal(RawContactDelta state, AccountType type, ViewIdGenerator vig) {
        if (this.mCust != null) {
            if (this.mVibration == null) {
                this.mVibration = this.mCust.inflateNewViews(getContext(), this.mInflater, this.mFragment, this.mFields, getRawContactId());
            }
            this.mCust.addViews(this.mFields, this.mVibration);
        }
        addToDefaultGroupIfNeeded();
        this.mAddFieldButton.setEnabled(isEnabled());
        updateAdditionButtonVisibilityState();
        this.mFragment.onAllItemDone(this, this.mRawContactId, this.typeLocal);
    }

    public void setIntent(Intent intent) {
        this.mIntent = intent;
    }

    public void setGroupMetaData(Cursor groupMetaData) {
        this.mGroupMetaData = groupMetaData;
        if (this.mGroupMembershipKind != null) {
            addToDefaultGroupIfNeeded();
            if (this.mGroupMembershipView != null) {
                this.mGroupMembershipView.setGroupMetaData(groupMetaData);
            }
        }
    }

    public void setFragment(ContactEditorFragment aFragment) {
        this.mFragment = aFragment;
        if (this.mGroupMembershipView != null) {
            this.mGroupMembershipView.setFragment(aFragment);
        }
    }

    public void setAutoAddToDefaultGroup(boolean flag) {
        this.mAutoAddToDefaultGroup = flag;
    }

    private void addToDefaultGroupIfNeeded() {
        if (this.mAutoAddToDefaultGroup && this.mGroupMetaData != null && !this.mGroupMetaData.isClosed() && this.mState != null) {
            boolean hasGroupMembership = false;
            ArrayList<ValuesDelta> entries = this.mState.getMimeEntries("vnd.android.cursor.item/group_membership");
            if (entries != null) {
                for (ValuesDelta values : entries) {
                    Long id = values.getGroupRowId();
                    if (id != null && id.longValue() != 0) {
                        hasGroupMembership = true;
                        break;
                    }
                }
            }
            if (!hasGroupMembership) {
                long defaultGroupId = getDefaultGroupId();
                if (defaultGroupId != -1) {
                    RawContactModifier.insertChild(this.mState, this.mGroupMembershipKind).setGroupRowId(defaultGroupId);
                }
            }
        }
    }

    private long getDefaultGroupId() {
        String accountType = this.mState.getAccountType();
        String accountName = this.mState.getAccountName();
        String accountDataSet = this.mState.getDataSet();
        this.mGroupMetaData.moveToPosition(-1);
        while (this.mGroupMetaData.moveToNext()) {
            String name = this.mGroupMetaData.getString(0);
            String type = this.mGroupMetaData.getString(1);
            String dataSet = this.mGroupMetaData.getString(2);
            if (name != null && name.equals(accountName) && type != null && type.equals(accountType) && Objects.equal(dataSet, accountDataSet)) {
                long groupId = this.mGroupMetaData.getLong(3);
                if (!(this.mGroupMetaData.isNull(5) || this.mGroupMetaData.getInt(5) == 0)) {
                    return groupId;
                }
            }
        }
        return -1;
    }

    public TextFieldsEditorView getNameEditor() {
        return this.mName;
    }

    public TextFieldsEditorView getPhoneticNameEditor() {
        return this.mPhoneticName;
    }

    public boolean isPhoneticNameVisible() {
        if (this.mPhoneticName == null || this.mPhoneticName.getFirstEditField() == null || this.mPhoneticName.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    private void updatePhoneticNameVisibility() {
        if ((getContext().getResources().getBoolean(R.bool.config_editor_include_phonetic_name) & (EmuiFeatureManager.isChinaArea() ? 0 : 1)) || this.mPhoneticName.hasData() || this.mPhoneticNameAdded) {
            this.mPhoneticName.setVisibility(0);
        } else {
            this.mPhoneticName.setVisibility(8);
        }
    }

    public long getRawContactId() {
        return this.mRawContactId;
    }

    public String getAccountType() {
        return this.mAccountType;
    }

    private ArrayList<ContactEditorInfo> getSectionViewsWithoutFields() {
        ArrayList<ContactEditorInfo> fields = new ArrayList(this.mFields.getChildCount());
        for (int i = 0; i < this.mFields.getChildCount(); i++) {
            View child = this.mFields.getChildAt(i);
            if ((child instanceof GroupMembershipView) && child.getVisibility() != 0) {
                fields.add((GroupMembershipView) child);
            } else if ((child instanceof RingtoneEditorView) && child.getVisibility() != 0) {
                fields.add((RingtoneEditorView) child);
            } else if (child instanceof KindSectionView) {
                KindSectionView sectionView = (KindSectionView) child;
                if (sectionView.getEditorCount() <= 0 || sectionView.getKind().mimeType.equals("vnd.android.cursor.item/contact_event")) {
                    DataKind kind = sectionView.getKind();
                    if ((kind.typeOverallMax != 1 || sectionView.getEditorCount() == 0 || kind.mimeType.equals("vnd.android.cursor.item/contact_event")) && !"#displayName".equals(kind.mimeType) && (!("#phoneticName".equals(kind.mimeType) && this.mPhoneticName.getVisibility() == 0) && (PhoneCapabilityTester.isSipEnabled(getContext()) || !"vnd.android.cursor.item/sip_address".equals(kind.mimeType)))) {
                        if (kind.mimeType.equals("vnd.android.cursor.item/contact_event")) {
                            for (EditType edit : kind.typeList) {
                                if (edit.rawValue == 3) {
                                    if (sectionView.isBirthdayPresent(edit.rawValue)) {
                                        birthdayPos = -1;
                                    } else {
                                        fields.add(sectionView);
                                        birthdayPos = fields.size() - 1;
                                    }
                                } else if (edit.rawValue == 4 && LunarUtils.isChineseRegion(getContext())) {
                                    if (sectionView.isBirthdayPresent(edit.rawValue)) {
                                        lunarBirthdayPos = -1;
                                    } else {
                                        fields.add(sectionView);
                                        lunarBirthdayPos = fields.size() - 1;
                                    }
                                } else if (edit.rawValue == 1) {
                                    fields.add(sectionView);
                                    anniversaryPos = fields.size() - 1;
                                } else if (edit.rawValue == 2) {
                                    fields.add(sectionView);
                                    importantDatePos = fields.size() - 1;
                                }
                            }
                        } else {
                            fields.add(sectionView);
                        }
                    }
                }
            }
        }
        return fields;
    }

    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        if (this.dialog != null && this.dialog.isShowing()) {
            this.dialog.dismiss();
        }
        if (this.mPhoneticNameAdded) {
            state.mPhoneticNameAdded = this.mPhoneticNameAdded;
        }
        return state;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.mSuperState);
        if (ss.mPhoneticNameAdded) {
            this.mPhoneticNameAdded = ss.mPhoneticNameAdded;
            updatePhoneticNameVisibility();
            updateAdditionButtonVisibilityState();
        }
    }

    private void updateAdditionButtonVisibilityState() {
        if (this.mIsDelayedLoadingItemExist) {
            setAddButtonVisibility(0);
            return;
        }
        for (int i = 0; i < this.mFields.getChildCount(); i++) {
            View child = this.mFields.getChildAt(i);
            if (child instanceof KindSectionView) {
                KindSectionView sectionView = (KindSectionView) child;
                DataKind kind = sectionView.getKind();
                if ((sectionView.getEditorCount() <= 0 || "vnd.android.cursor.item/contact_event".equals(kind.mimeType)) && ((kind.typeOverallMax != 1 || sectionView.getEditorCount() == 0) && !"#displayName".equals(kind.mimeType) && (!("#phoneticName".equals(kind.mimeType) && this.mPhoneticName.getVisibility() == 0) && (PhoneCapabilityTester.isSipEnabled(getContext()) || !"vnd.android.cursor.item/sip_address".equals(kind.mimeType))))) {
                    setAddButtonVisibility(0);
                    return;
                }
            }
        }
        setAddButtonVisibility(8);
    }

    private void setAddButtonVisibility(int visibility) {
        this.mAddFieldButton.setVisibility(visibility);
        if (visibility == 8) {
            setAccoutContainerMargins();
        }
    }

    public ImmutableList<GroupMetaData> getSelectedGroupsMetaData() {
        ImmutableList.Builder<GroupMetaData> lGroupListBuilder = new ImmutableList.Builder();
        if (this.mGroupMembershipView != null) {
            HashSet<GroupMetaData> lGroupMetaDetaSet = this.mGroupMembershipView.getSeletedGroupsMetaData();
            if (!lGroupMetaDetaSet.isEmpty()) {
                for (Object lMetaData : lGroupMetaDetaSet) {
                    lGroupListBuilder.add(lMetaData);
                }
            }
        }
        return lGroupListBuilder.build();
    }

    public ViewGroup getTitleContainer() {
        return this.mTitleContainer;
    }

    private void setAccoutContainerMargins() {
        if (this.mAccountsFromText != null) {
            LayoutParams lp = (LayoutParams) this.mAccountsFromText.getLayoutParams();
            lp.setMargins(lp.getMarginStart(), this.mAccountContainerMargin, lp.getMarginEnd(), 0);
            this.mAccountsFromText.setLayoutParams(lp);
        }
    }

    public boolean isAllFieldsEmpty() {
        if (getPhotoEditor() != null && getPhotoEditor().hasSetPhoto()) {
            return false;
        }
        if (EmuiFeatureManager.isChinaArea()) {
            if (!this.mName.isAllVisibleEditTextEmpty()) {
                return false;
            }
        } else if (!this.mName.isEmpty()) {
            return false;
        }
        if (!this.mPhoneticName.isEmpty() || !this.mOrganisationName.isEmpty()) {
            return false;
        }
        for (int i = 0; i < this.mFields.getChildCount(); i++) {
            View child = this.mFields.getChildAt(i);
            if ((child instanceof KindSectionView) && !((KindSectionView) child).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public GroupMembershipView getGroupMembershipView() {
        return this.mGroupMembershipView;
    }

    public static void setAnniverseryPos(int aAnniversaryPos) {
        anniversaryPos = aAnniversaryPos;
    }

    public void removeHandlerCallback() {
        if (this.mHandler != null) {
            this.mHandler.removeCallbacks(this.mRunnableForDelay);
        }
    }

    public void setWriteabeList(RawContactDeltaList writableList) {
        this.mWritableList = writableList;
    }

    public void setGroupEditableList(RawContactDeltaList groupEditableList) {
        this.mGroupEditableList = groupEditableList;
    }

    public void hideCompanyPop() {
        findViewById(R.id.company_popup).setVisibility(8);
        if (this.mOrganisationName != null) {
            this.mOrganisationName.cancelPopupCompanyListTask();
        }
    }

    public void setQueryCompanyInfoState(boolean isQueryCompanyInfo) {
        this.mIsQueryCompanyInfo = isQueryCompanyInfo;
    }

    public boolean getQueryCompanyInfoState() {
        return this.mIsQueryCompanyInfo;
    }

    public void setPopupCompanyShowState(boolean state) {
        if (this.mScrollView != null) {
            this.mScrollView.setEventDeliveryState(state);
        }
    }

    private void setAddFieldButtonWith() {
        ViewGroup.LayoutParams params = this.mAddFieldButton.getLayoutParams();
        params.width = -1;
        this.mAddFieldButton.setLayoutParams(params);
    }
}
