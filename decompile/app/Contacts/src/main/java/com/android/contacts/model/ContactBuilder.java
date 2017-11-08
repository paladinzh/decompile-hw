package com.android.contacts.model;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import com.android.contacts.GeoUtil;
import com.android.contacts.GroupMetaData;
import com.android.contacts.editor.PhoneticNameEditorView;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountTypeWithDataSet;
import com.android.contacts.model.dataitem.DataItem;
import com.android.contacts.model.dataitem.PhoneDataItem;
import com.android.contacts.util.DataStatus;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.NameConverter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class ContactBuilder {
    private Context mContext;
    private boolean mForNewContactBeforeSave;
    private boolean mIsUserProfile = false;
    private RawContactDeltaList mState;

    private static class DisplayNameAndSource {
        String mDisplayName;
        int mDisplayNameSource;

        public DisplayNameAndSource(String aDisplayName, int aDisplayNameSource) {
            this.mDisplayName = aDisplayName;
            this.mDisplayNameSource = aDisplayNameSource;
        }
    }

    public ContactBuilder(Context aContext, RawContactDeltaList aState, boolean aIsUserProfile) {
        this.mContext = aContext;
        this.mState = aState;
        this.mIsUserProfile = aIsUserProfile;
    }

    public Contact getNewContactFromState() {
        this.mForNewContactBeforeSave = true;
        return getContact(null);
    }

    public Contact getContact(Uri aLoogupUri) {
        int numberOfRawContact = this.mState.size();
        if (numberOfRawContact == 0) {
            return null;
        }
        Contact result = null;
        Builder<RawContact> rawContactsBuilder = new Builder();
        ImmutableMap.Builder<Long, DataStatus> statusesBuilder = new ImmutableMap.Builder();
        RawContactDeltaList state = this.mState;
        HwLog.i("ContactBuilder", "getContact numberOfRawContact:" + numberOfRawContact);
        for (int i = 0; i < numberOfRawContact; i++) {
            RawContactDelta rawContactDelta = (RawContactDelta) state.get(i);
            if (i == 0) {
                result = prepareContact(rawContactDelta, aLoogupUri);
                if (result == null) {
                    break;
                }
            }
            RawContact rawContact = new RawContact(rawContactDelta.getValues().getCompleteValues());
            for (Entry<String, ArrayList<ValuesDelta>> entry : rawContactDelta.getEntries().entrySet()) {
                Iterator<ValuesDelta> iterator2 = ((ArrayList) entry.getValue()).iterator();
                while (iterator2.hasNext()) {
                    ValuesDelta valuesDelta = (ValuesDelta) iterator2.next();
                    String data1 = valuesDelta.getAsString("data1");
                    String data15 = valuesDelta.getAsString("data15");
                    if (!TextUtils.isEmpty(data1) || !TextUtils.isEmpty(data15)) {
                        rawContact.addDataItemValues(valuesDelta.getCompleteValues());
                        if (!(valuesDelta.getAsString("mode") == null || valuesDelta.getAsString("status") == null)) {
                            DataStatus status = new DataStatus();
                            status.fromValuesDelta(valuesDelta);
                            statusesBuilder.put(Long.valueOf((long) valuesDelta.getAsInteger("_id", Integer.valueOf(0)).intValue()), status);
                        }
                    }
                }
            }
            rawContactsBuilder.add((Object) rawContact);
        }
        if (result != null) {
            result.setRawContacts(rawContactsBuilder.build());
            result.setStatuses(statusesBuilder.build());
            loadInvitableAccountTypes(result, this.mContext);
            computeFormattedPhoneNumbers(result, this.mContext);
        }
        HwLog.i("ContactBuilder", "getContact END");
        return result;
    }

    private Contact prepareContact(RawContactDelta aRawContactDelta, Uri aLoogupUri) {
        if (!this.mForNewContactBeforeSave && aLoogupUri == null) {
            return null;
        }
        String displayName;
        String altDisplayName;
        Uri requestedUri = aLoogupUri;
        Uri contactUri = null;
        if (!this.mForNewContactBeforeSave) {
            contactUri = Contacts.lookupContact(this.mContext.getContentResolver(), aLoogupUri);
        }
        ValuesDelta valueDelta = aRawContactDelta.getValues();
        Integer starredVal = valueDelta.getAsInteger("starred");
        boolean starred = (starredVal == null || starredVal.intValue() == 0) ? false : true;
        Integer presence = valueDelta.getAsInteger("contact_presence", Integer.valueOf(0));
        boolean sendToVoicemail = valueDelta.getAsInteger("send_to_voicemail", Integer.valueOf(0)).intValue() == 1;
        String customRingtone = valueDelta.getAsString("custom_ringtone");
        int displayNameSource = valueDelta.getAsInteger("display_name_source", Integer.valueOf(0)).intValue();
        String sortKey = aRawContactDelta.getSortKey();
        ArrayList<ValuesDelta> nameDelta = aRawContactDelta.getMimeEntries("vnd.android.cursor.item/name");
        String phoneticFamilyName = null;
        if (this.mForNewContactBeforeSave) {
            DisplayNameAndSource lData = computeDisplayNameAndDisplayNameSources(nameDelta, aRawContactDelta);
            displayName = lData.mDisplayName;
            displayNameSource = lData.mDisplayNameSource;
            altDisplayName = displayName;
            if (40 == displayNameSource && nameDelta != null && nameDelta.size() > 0) {
                phoneticFamilyName = buildPhoneticName((ValuesDelta) nameDelta.get(0));
            }
        } else if (nameDelta == null || nameDelta.size() <= 0) {
            phoneticFamilyName = valueDelta.getPhoneticFamilyName();
            displayName = valueDelta.getAsString("raw_contact_display_name");
            altDisplayName = valueDelta.getAsString("display_name_alt");
        } else {
            ValuesDelta name = (ValuesDelta) nameDelta.get(0);
            phoneticFamilyName = buildPhoneticName(name);
            displayName = name.getDisplayName();
            altDisplayName = displayName;
        }
        long photoId = 0;
        boolean isUserProfile = this.mIsUserProfile;
        String str = null;
        if (aLoogupUri != null) {
            List<String> pathSegs = aLoogupUri.getPathSegments();
            if (pathSegs == null) {
                return null;
            }
            str = (String) pathSegs.get(pathSegs.size() - 2);
        }
        long id = -1;
        if (contactUri != null) {
            id = Long.parseLong(contactUri.getLastPathSegment());
        }
        ArrayList<ValuesDelta> photoDelta = aRawContactDelta.getMimeEntries("vnd.android.cursor.item/photo");
        byte[] lPhotoData = null;
        if (photoDelta != null && photoDelta.size() > 0) {
            lPhotoData = ((ValuesDelta) photoDelta.get(0)).getPhoto();
        }
        if (this.mForNewContactBeforeSave && photoDelta != null && photoDelta.size() > 0) {
            ValuesDelta photo = (ValuesDelta) photoDelta.get(0);
            if (photo.getAfter().containsKey("data1")) {
                photoId = photo.getAfter().getAsLong("data1").longValue();
            }
        }
        Contact contact = new Contact(aLoogupUri, aLoogupUri, aLoogupUri, 0, str, id, aRawContactDelta.getRawContactId().longValue(), displayNameSource, photoId, null, displayName, altDisplayName, phoneticFamilyName, starred, presence, sendToVoicemail, customRingtone, isUserProfile, sortKey);
        contact.setPhotoBinaryData(lPhotoData);
        return contact;
    }

    private static void loadInvitableAccountTypes(Contact aContactData, Context aContext) {
        Builder<AccountType> resultListBuilder = new Builder();
        if (!aContactData.isUserProfile()) {
            Map<AccountTypeWithDataSet, AccountType> invitables = AccountTypeManager.getInstance(aContext).getUsableInvitableAccountTypes();
            if (!invitables.isEmpty()) {
                Map<AccountTypeWithDataSet, AccountType> resultMap = Maps.newHashMap(invitables);
                for (RawContact rawContact : aContactData.getRawContacts()) {
                    resultMap.remove(AccountTypeWithDataSet.get(rawContact.getAccountTypeString(), rawContact.getDataSet()));
                }
                resultListBuilder.addAll(resultMap.values());
            }
        }
        aContactData.setInvitableAccountTypes(resultListBuilder.build());
    }

    private static void computeFormattedPhoneNumbers(Contact contactData, Context aContext) {
        String countryIso = GeoUtil.getCurrentCountryIso(aContext);
        ImmutableList<RawContact> rawContacts = contactData.getRawContacts();
        int rawContactCount = rawContacts.size();
        for (int rawContactIndex = 0; rawContactIndex < rawContactCount; rawContactIndex++) {
            List<DataItem> dataItems = ((RawContact) rawContacts.get(rawContactIndex)).getDataItems();
            int dataCount = dataItems.size();
            for (int dataIndex = 0; dataIndex < dataCount; dataIndex++) {
                DataItem dataItem = (DataItem) dataItems.get(dataIndex);
                if (dataItem instanceof PhoneDataItem) {
                    ((PhoneDataItem) dataItem).computeFormattedPhoneNumber(countryIso);
                }
            }
        }
    }

    private String buildPhoneticName(ValuesDelta aNamedDelta) {
        String family = aNamedDelta.getPhoneticFamilyName();
        String given = aNamedDelta.getPhoneticGivenName();
        String middle = aNamedDelta.getPhoneticMiddleName();
        if ("ru".equalsIgnoreCase(Locale.getDefault().getCountry())) {
            return PhoneticNameEditorView.buildPhoneticName(family, given, middle);
        }
        return PhoneticNameEditorView.buildPhoneticName(family, middle, given);
    }

    private DisplayNameAndSource computeDisplayNameAndDisplayNameSources(ArrayList<ValuesDelta> nameDelta, RawContactDelta aRawContactDelta) {
        String str = null;
        int lDisplayNameSource = 0;
        if (nameDelta != null && nameDelta.size() > 0) {
            str = ((ValuesDelta) nameDelta.get(0)).getDisplayName();
            if (isEmpty(str)) {
                str = getNameFromNameDelta((ValuesDelta) nameDelta.get(0));
            }
        }
        if (isEmpty(str)) {
            nameDelta = aRawContactDelta.getMimeEntries("vnd.android.cursor.item/nickname");
            if (nameDelta != null && nameDelta.size() > 0) {
                str = ((ValuesDelta) nameDelta.get(0)).getAsString("data1");
            }
            if (isEmpty(str)) {
                nameDelta = aRawContactDelta.getMimeEntries("vnd.android.cursor.item/organization");
                if (nameDelta != null && nameDelta.size() > 0) {
                    String company = ((ValuesDelta) nameDelta.get(0)).getAsString("data1");
                    if (TextUtils.isEmpty(company)) {
                        str = ((ValuesDelta) nameDelta.get(0)).getAsString("data4");
                    } else {
                        str = company;
                    }
                }
                if (isEmpty(str)) {
                    nameDelta = aRawContactDelta.getMimeEntries("vnd.android.cursor.item/phone_v2");
                    if (nameDelta != null && nameDelta.size() > 0) {
                        str = ((ValuesDelta) nameDelta.get(0)).getAsString("data1");
                    }
                    if (isEmpty(str)) {
                        nameDelta = aRawContactDelta.getMimeEntries("vnd.android.cursor.item/email_v2");
                        if (nameDelta != null && nameDelta.size() > 0) {
                            str = ((ValuesDelta) nameDelta.get(0)).getAsString("data1");
                        }
                        if (isEmpty(str)) {
                            str = null;
                        } else {
                            lDisplayNameSource = 10;
                        }
                    } else {
                        lDisplayNameSource = 20;
                    }
                } else {
                    lDisplayNameSource = 30;
                }
            } else {
                lDisplayNameSource = 35;
            }
        } else {
            lDisplayNameSource = 40;
        }
        return new DisplayNameAndSource(str, lDisplayNameSource);
    }

    private boolean isEmpty(String aInput) {
        return aInput != null ? aInput.trim().equals("") : true;
    }

    public void setGroupMetaDeta(Contact aContact, ImmutableList<GroupMetaData> aGroupMetaData) {
        if (aContact != null) {
            aContact.setGroupMetaData(aGroupMetaData);
        }
    }

    private String getNameFromNameDelta(ValuesDelta aNameDelta) {
        return NameConverter.getDisplayNameFromValuesDelta(this.mContext, aNameDelta);
    }
}
