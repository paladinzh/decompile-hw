package com.android.contacts.editor;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.RawContacts;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.contacts.GeoUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.RawContactModifier;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.model.dataitem.DataKind;
import com.google.android.gms.R;
import java.util.ArrayList;

public class RawContactReadOnlyEditorView extends BaseRawContactEditorView implements OnClickListener {
    private LinearLayout mAccountsContainer;
    private Button mEditExternallyButton;
    private ViewGroup mGeneral;
    private LayoutInflater mInflater;
    private Listener mListener;
    private TextView mName;
    private long mRawContactId = -1;
    private RawContactDelta mState;

    public interface Listener {
        void onExternalEditorRequest(AccountWithDataSet accountWithDataSet, Uri uri);
    }

    public RawContactReadOnlyEditorView(Context context) {
        super(context);
    }

    public RawContactReadOnlyEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mInflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        this.mName = (TextView) findViewById(R.id.read_only_name);
        this.mEditExternallyButton = (Button) findViewById(R.id.button_edit_externally);
        this.mEditExternallyButton.setOnClickListener(this);
        this.mGeneral = (ViewGroup) findViewById(R.id.sect_general);
        this.mAccountsContainer = (LinearLayout) findViewById(R.id.accounts_container);
    }

    public void setState(RawContactDeltaList stateList, RawContactDelta state, AccountType type, ViewIdGenerator vig, boolean isProfile) {
        this.mGeneral.removeAllViews();
        this.mState = state;
        if (stateList != null && !stateList.isEmpty() && state != null && type != null) {
            CharSequence asString;
            int editorIndex;
            int i;
            Integer tmpType;
            RawContactModifier.ensureKindExists(state, type, "vnd.android.cursor.item/name");
            handleAccountsDisplay(stateList, state, type, isProfile);
            this.mRawContactId = state.getRawContactId().longValue();
            DataKind kind = type.getKindForMimetype("vnd.android.cursor.item/photo");
            if (kind != null) {
                RawContactModifier.ensureKindExists(state, type, "vnd.android.cursor.item/photo");
                setHasPhotoEditor(type.getKindForMimetype("vnd.android.cursor.item/photo") != null);
                getPhotoEditor().setValues(kind, state.getPrimaryEntry("vnd.android.cursor.item/photo"), state, !type.areContactsWritable(), vig);
            }
            ValuesDelta primary = state.getPrimaryEntry("vnd.android.cursor.item/name");
            TextView textView = this.mName;
            if (primary != null) {
                asString = primary.getAsString("data1");
            } else {
                asString = getContext().getString(R.string.missing_name);
            }
            textView.setText(asString);
            if (type.getEditContactActivityClassName() != null) {
                this.mEditExternallyButton.setVisibility(0);
            } else {
                this.mEditExternallyButton.setVisibility(8);
            }
            int numRawContacts = stateList.size();
            Resources res = getContext().getResources();
            for (editorIndex = 0; editorIndex < numRawContacts; editorIndex++) {
                ArrayList<ValuesDelta> phones = ((RawContactDelta) stateList.get(editorIndex)).getMimeEntries("vnd.android.cursor.item/phone_v2");
                if (phones != null) {
                    i = 0;
                    while (i < phones.size()) {
                        ValuesDelta phone = (ValuesDelta) phones.get(i);
                        if (phone.getPhoneNumber() != null) {
                            CharSequence phoneType;
                            String phoneNumber = PhoneNumberUtils.formatNumber(phone.getPhoneNumber(), phone.getPhoneNormalizedNumber(), GeoUtil.getCurrentCountryIso(getContext()));
                            tmpType = phone.getAsInteger("data2");
                            if (!phone.containsKey("data2") || tmpType == null) {
                                phoneType = null;
                            } else {
                                phoneType = Phone.getTypeLabel(res, tmpType.intValue(), phone.getAsString("data3"));
                            }
                            bindData(getContext().getText(R.string.phoneLabelsGroup), phoneNumber, phoneType);
                            i++;
                        } else {
                            return;
                        }
                    }
                    continue;
                }
            }
            for (editorIndex = 0; editorIndex < numRawContacts; editorIndex++) {
                ArrayList<ValuesDelta> emails = ((RawContactDelta) stateList.get(editorIndex)).getMimeEntries("vnd.android.cursor.item/email_v2");
                if (emails != null) {
                    for (i = 0; i < emails.size(); i++) {
                        CharSequence typeLabel;
                        ValuesDelta email = (ValuesDelta) emails.get(i);
                        String emailAddress = email.getAsString("data1");
                        if (email.containsKey("data2")) {
                            tmpType = email.getAsInteger("data2");
                            if (tmpType != null) {
                                typeLabel = Email.getTypeLabel(res, tmpType.intValue(), email.getAsString("data3"));
                            } else {
                                typeLabel = null;
                            }
                        } else {
                            typeLabel = null;
                        }
                        bindData(getContext().getText(R.string.emailLabelsGroup), emailAddress, typeLabel);
                    }
                }
            }
            for (editorIndex = 0; editorIndex < numRawContacts; editorIndex++) {
                ArrayList<ValuesDelta> postAddresses = ((RawContactDelta) stateList.get(editorIndex)).getMimeEntries("vnd.android.cursor.item/postal-address_v2");
                if (postAddresses != null) {
                    for (i = 0; i < postAddresses.size(); i++) {
                        CharSequence typeLabel2;
                        ValuesDelta address = (ValuesDelta) postAddresses.get(i);
                        String postAddress = address.getAsString("data1");
                        if (address.containsKey("data2")) {
                            tmpType = address.getAsInteger("data2");
                            if (tmpType != null) {
                                typeLabel2 = StructuredPostal.getTypeLabel(res, tmpType.intValue(), address.getAsString("data3"));
                            } else {
                                typeLabel2 = null;
                            }
                        } else {
                            typeLabel2 = null;
                        }
                        bindData(getContext().getText(R.string.emailLabelsGroup), postAddress, typeLabel2);
                    }
                }
            }
            CharSequence ringtone = state.getValues().getAsString("custom_ringtone");
            if ("-1".equals(ringtone)) {
                ringtone = getContext().getText(R.string.contact_silent_ringtone).toString();
            } else if (!TextUtils.isEmpty(ringtone)) {
                Ringtone lRingtone = null;
                if (!TextUtils.isEmpty(ringtone)) {
                    lRingtone = RingtoneManager.getRingtone(getContext(), Uri.parse(ringtone));
                }
                if (lRingtone != null) {
                    if (!TextUtils.isEmpty(lRingtone.getTitle(getContext()))) {
                        ringtone = lRingtone.getTitle(getContext());
                    }
                }
                ringtone = null;
            }
            if (TextUtils.isEmpty(ringtone)) {
                ringtone = getContext().getString(R.string.default_ringtone);
            }
            bindData(getContext().getText(R.string.label_ringtone), getContext().getString(R.string.phone_ringtone_string), ringtone);
            if (this.mGeneral.getChildCount() > 0) {
                this.mGeneral.setVisibility(0);
            } else {
                this.mGeneral.setVisibility(8);
            }
        }
    }

    private void handleAccountsDisplay(RawContactDeltaList stateList, RawContactDelta state, AccountType type, boolean isProfile) {
        if (isProfile) {
            View accoutsContainer = (LinearLayout) this.mInflater.inflate(R.layout.editor_source_account_container, this.mAccountsContainer, false);
            this.mAccountsContainer.addView(accoutsContainer);
            TextView accountTypeTextView = (TextView) accoutsContainer.findViewById(R.id.account_type);
            ((TextView) accoutsContainer.findViewById(R.id.account_name)).setVisibility(8);
            String accountName = state.getAccountName();
            if (TextUtils.isEmpty(accountName)) {
                accountTypeTextView.setText(R.string.local_profile_title);
                return;
            }
            CharSequence accountType = type.getDisplayLabel(getContext());
            accountTypeTextView.setText(getContext().getString(R.string.external_profile_title, new Object[]{accountType}));
            accountTypeTextView.setText(accountName);
            return;
        }
        for (AccountWithDataSet account : getAccountsList()) {
            LinearLayout accoutContainer = (LinearLayout) this.mInflater.inflate(R.layout.editor_source_account_container, this.mAccountsContainer, false);
            this.mAccountsContainer.addView(accoutContainer);
            TextView accountTypeText = (TextView) accoutContainer.findViewById(R.id.account_type);
            TextView accountNameText = (TextView) accoutContainer.findViewById(R.id.account_name);
            accountType = AccountTypeManager.getInstance(getContext()).getAccountType(account.type, account.dataSet).getDisplayLabel(getContext());
            if (TextUtils.isEmpty(accountType)) {
                accountType = CommonUtilMethods.getHiCloudAccountPhoneDisplayString(getContext());
            }
            if (TextUtils.isEmpty(account.name) || account.name.equalsIgnoreCase(accountType.toString())) {
                accountNameText.setVisibility(8);
            } else {
                accountNameText.setVisibility(0);
                accountNameText.setText(account.name);
            }
            accountTypeText.setText(accountType);
        }
    }

    private void bindData(CharSequence titleText, CharSequence data, CharSequence type) {
        View field = this.mInflater.inflate(R.layout.item_read_only_field, this.mGeneral, false);
        ((TextView) field.findViewById(R.id.data)).setText(data);
        TextView typeView = (TextView) field.findViewById(R.id.type);
        if (TextUtils.isEmpty(type)) {
            typeView.setVisibility(8);
        } else {
            typeView.setText(type);
        }
        this.mGeneral.addView(field);
    }

    public long getRawContactId() {
        return this.mRawContactId;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.button_edit_externally && this.mListener != null && this.mState != null) {
            this.mListener.onExternalEditorRequest(new AccountWithDataSet(this.mState.getAccountName(), this.mState.getAccountType(), this.mState.getDataSet()), ContentUris.withAppendedId(RawContacts.CONTENT_URI, this.mRawContactId));
        }
    }
}
