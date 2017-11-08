package com.android.contacts.editor;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.editor.AggregationSuggestionEngine.RawContact;
import com.android.contacts.editor.AggregationSuggestionEngine.Suggestion;
import com.android.contacts.model.AccountTypeManager;
import com.google.android.gms.R;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;

public class AggregationSuggestionView extends LinearLayout {
    private long mContactId;
    private Listener mListener;
    private String mLookupKey;
    private boolean mNewContact;
    private List<RawContact> mRawContacts = Lists.newArrayList();

    public interface Listener {
        void onEditAction(Uri uri);

        void onJoinAction(long j, List<Long> list);
    }

    public AggregationSuggestionView(Context context) {
        super(context);
    }

    public AggregationSuggestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AggregationSuggestionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setNewContact(boolean flag) {
        this.mNewContact = flag;
    }

    public void bindSuggestion(Suggestion suggestion) {
        this.mContactId = suggestion.contactId;
        this.mLookupKey = suggestion.lookupKey;
        this.mRawContacts = suggestion.rawContacts;
        ImageView photo = (ImageView) findViewById(R.id.aggregation_suggestion_photo);
        if (suggestion.photo != null) {
            photo.setImageDrawable(ContactPhotoManager.createRoundPhotoDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(suggestion.photo, 0, suggestion.photo.length))));
        } else {
            photo.setImageResource(R.drawable.ic_contact_picture_holo_light);
        }
        ((TextView) findViewById(R.id.aggregation_suggestion_name)).setText(suggestion.name);
        TextView data = (TextView) findViewById(R.id.aggregation_suggestion_data);
        String dataText = null;
        if (suggestion.nickname != null) {
            dataText = suggestion.nickname;
        } else if (suggestion.emailAddress != null) {
            dataText = suggestion.emailAddress;
        } else if (suggestion.phoneNumber != null) {
            dataText = suggestion.phoneNumber;
        }
        data.setText(dataText);
    }

    private boolean canEditSuggestedContact() {
        if (!this.mNewContact) {
            return false;
        }
        AccountTypeManager accountTypes = AccountTypeManager.getInstance(getContext());
        for (RawContact rawContact : this.mRawContacts) {
            String accountType = rawContact.accountType;
            String dataSet = rawContact.dataSet;
            if (accountType == null) {
                return true;
            }
            if (accountTypes.getAccountType(accountType, dataSet).areContactsWritable()) {
                return true;
            }
        }
        return false;
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public boolean handleItemClickEvent() {
        if (this.mListener == null || !isEnabled()) {
            return false;
        }
        if (canEditSuggestedContact()) {
            this.mListener.onEditAction(Contacts.getLookupUri(this.mContactId, this.mLookupKey));
        } else {
            ArrayList<Long> rawContactIds = Lists.newArrayList();
            for (RawContact rawContact : this.mRawContacts) {
                rawContactIds.add(Long.valueOf(rawContact.rawContactId));
            }
            this.mListener.onJoinAction(this.mContactId, rawContactIds);
        }
        return true;
    }
}
