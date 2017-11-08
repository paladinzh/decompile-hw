package com.android.contacts.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.ContactMethods;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.contacts.ContactsUtils;
import com.android.contacts.editor.EventFieldEditorView;
import com.android.contacts.editor.PhoneticNameEditorView;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimStateServiceHandler;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountType.EditField;
import com.android.contacts.model.account.AccountType.EditType;
import com.android.contacts.model.account.AccountType.EventEditType;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.model.dataitem.StructuredNameDataItem;
import com.android.contacts.util.DateUtils;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.LunarUtils;
import com.android.contacts.util.NameConverter;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.google.android.gms.common.Scopes;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class RawContactModifier {
    private static final String TAG = RawContactModifier.class.getSimpleName();
    private static final Set<String> sGenericMimeTypesWithTypeSupport = new HashSet(Arrays.asList(new String[]{"vnd.android.cursor.item/phone_v2", "vnd.android.cursor.item/email_v2", "vnd.android.cursor.item/im", "vnd.android.cursor.item/nickname", "vnd.android.cursor.item/website", "vnd.android.cursor.item/relation", "vnd.android.cursor.item/sip_address"}));
    private static final Set<String> sGenericMimeTypesWithoutTypeSupport = new HashSet(Arrays.asList(new String[]{"vnd.android.cursor.item/organization", "vnd.android.cursor.item/note", "vnd.android.cursor.item/photo", "vnd.android.cursor.item/group_membership", "vnd.android.huawei.cursor.item/ringtone"}));

    public static boolean canInsert(RawContactDelta state, DataKind kind) {
        if (kind == null) {
            return false;
        }
        int visibleCount = state.getMimeEntriesCount(kind.mimeType, true);
        boolean validTypes = hasValidTypes(state, kind);
        boolean validOverall = kind.typeOverallMax != -1 ? visibleCount < kind.typeOverallMax : true;
        if (!validTypes) {
            validOverall = false;
        }
        return validOverall;
    }

    public static boolean hasValidTypes(RawContactDelta state, DataKind kind) {
        boolean z = true;
        if (!hasEditTypes(kind)) {
            return true;
        }
        if (getValidTypes(state, kind).size() <= 0) {
            z = false;
        }
        return z;
    }

    public static ValuesDelta ensureKindExists(RawContactDelta state, AccountType accountType, String mimeType) {
        DataKind kind = accountType.getKindForMimetype(mimeType);
        boolean hasChild = state.getMimeEntriesCount(mimeType, true) > 0;
        if (kind != null) {
            if (hasChild) {
                ArrayList<ValuesDelta> deltaList = state.getMimeEntries(mimeType);
                if (deltaList != null && deltaList.size() > 0) {
                    return (ValuesDelta) deltaList.get(0);
                }
            }
            ValuesDelta child = insertChild(state, kind);
            if (kind.mimeType.equals("vnd.android.cursor.item/photo")) {
                child.setFromTemplate(true);
            }
            return child;
        }
        return null;
    }

    public static ValuesDelta ensureKindExists(AccountTypeManager accountTypes, RawContactDeltaList stateList, RawContactDelta state, AccountType accountType, String mimeType) {
        if (stateList == null || stateList.size() == 0) {
            return ensureKindExists(state, accountType, mimeType);
        }
        int i;
        for (i = 0; i < stateList.size(); i++) {
            RawContactDelta rawContactDelta = (RawContactDelta) stateList.get(i);
            if (rawContactDelta.getAccountType(accountTypes).areContactsWritable()) {
                boolean hasChild;
                if (rawContactDelta.getMimeEntriesCount(mimeType, true) > 0) {
                    hasChild = true;
                } else {
                    hasChild = false;
                }
                if (hasChild) {
                    return (ValuesDelta) rawContactDelta.getMimeEntries(mimeType).get(0);
                }
            }
        }
        ValuesDelta child = null;
        for (i = 0; i < stateList.size(); i++) {
            rawContactDelta = (RawContactDelta) stateList.get(i);
            AccountType current_type = rawContactDelta.getAccountType(accountTypes);
            if (current_type.areContactsWritable()) {
                DataKind kind = current_type.getKindForMimetype(mimeType);
                if (kind != null) {
                    if (child == null) {
                        child = insertChild(rawContactDelta, kind);
                        if (kind.mimeType.equals("vnd.android.cursor.item/photo")) {
                            child.setFromTemplate(true);
                        }
                    } else {
                        rawContactDelta.addEntry(child);
                    }
                }
            }
        }
        return child;
    }

    public static ArrayList<EditType> getValidTypes(RawContactDelta state, DataKind kind) {
        return getValidTypes(state, kind, null, true, null);
    }

    public static ArrayList<EditType> getValidTypes(RawContactDelta state, DataKind kind, EditType forceInclude) {
        return getValidTypes(state, kind, forceInclude, true, null, null, true);
    }

    private static ArrayList<EditType> getValidTypes(RawContactDelta state, DataKind kind, EditType forceInclude, boolean includeSecondary, SparseIntArray typeCount) {
        return getValidTypes(state, kind, forceInclude, includeSecondary, typeCount, null, false);
    }

    public static ArrayList<EditType> getValidTypes(RawContactDelta state, DataKind kind, EditType forceInclude, boolean includeSecondary, SparseIntArray typeCount, Context context, boolean isSpinnerItem) {
        ArrayList<EditType> validTypes = new ArrayList();
        if (!hasEditTypes(kind)) {
            return validTypes;
        }
        if (typeCount == null) {
            typeCount = getTypeFrequencies(state, kind);
        }
        int overallCount = typeCount.get(Integer.MIN_VALUE);
        for (EditType type : kind.typeList) {
            boolean validOverall = kind.typeOverallMax == -1 || isSpinnerItem || overallCount <= kind.typeOverallMax;
            boolean validSpecific = type.specificMax == -1 || typeCount.get(type.rawValue) < type.specificMax;
            boolean validSecondary = includeSecondary || !type.secondary;
            boolean forcedInclude = type.equals(forceInclude);
            if (!"vnd.android.cursor.item/contact_event".equals(kind.mimeType) || LunarUtils.isChineseRegion(context) || type.rawValue != 4) {
                if (forcedInclude || ((validOverall && validSpecific && validSecondary) || "vnd.android.huawei.cursor.item/status_update".equals(kind.mimeType))) {
                    validTypes.add(type);
                }
            }
        }
        return validTypes;
    }

    private static SparseIntArray getTypeFrequencies(RawContactDelta state, DataKind kind) {
        SparseIntArray typeCount = new SparseIntArray();
        List<ValuesDelta> mimeEntries = state.getMimeEntries(kind.mimeType);
        if (mimeEntries == null) {
            return typeCount;
        }
        int totalCount = 0;
        for (ValuesDelta entry : mimeEntries) {
            if (entry.isVisible()) {
                totalCount++;
                EditType type = getCurrentType(entry, kind);
                if (type != null) {
                    typeCount.put(type.rawValue, typeCount.get(type.rawValue) + 1);
                }
            }
        }
        typeCount.put(Integer.MIN_VALUE, totalCount);
        return typeCount;
    }

    public static boolean hasEditTypes(DataKind kind) {
        return kind.typeList != null && kind.typeList.size() > 0;
    }

    public static EditType getCurrentType(ValuesDelta entry, DataKind kind) {
        Long rawValue = entry.getAsLong(kind.typeColumn);
        if (rawValue == null) {
            return null;
        }
        return getType(kind, rawValue.intValue());
    }

    public static EditType getType(DataKind kind, int rawValue) {
        for (EditType type : kind.typeList) {
            if (type.rawValue == rawValue) {
                return type;
            }
        }
        return null;
    }

    public static EditType getBestValidType(RawContactDelta state, DataKind kind, boolean includeSecondary, int exactValue) {
        if (kind.typeColumn == null) {
            return null;
        }
        SparseIntArray typeCount = getTypeFrequencies(state, kind);
        ArrayList<EditType> validTypes = getValidTypes(state, kind, null, includeSecondary, typeCount);
        if (validTypes.size() == 0) {
            return null;
        }
        if ("vnd.android.cursor.item/phone_v2".equals(kind.mimeType)) {
            ArrayList<ValuesDelta> entries = state.getMimeEntries("vnd.android.cursor.item/phone_v2");
            if (entries != null && entries.size() == 1) {
                return (EditType) validTypes.get(0);
            }
        }
        EditType lastType = (EditType) validTypes.get(validTypes.size() - 1);
        Iterator<EditType> iterator = validTypes.iterator();
        while (iterator.hasNext()) {
            EditType type = (EditType) iterator.next();
            int count = typeCount.get(type.rawValue);
            if (exactValue == type.rawValue) {
                return type;
            }
            if (count > 0) {
                iterator.remove();
            }
        }
        if (validTypes.size() > 0) {
            return (EditType) validTypes.get(0);
        }
        return lastType;
    }

    public static ValuesDelta insertChild(RawContactDelta state, DataKind kind) {
        EditType bestType = getBestValidType(state, kind, false, Integer.MIN_VALUE);
        if (bestType == null) {
            bestType = getBestValidType(state, kind, true, Integer.MIN_VALUE);
        }
        if (kind.typeToSelect != -1) {
            for (EditType type : kind.typeList) {
                if (type.rawValue == kind.typeToSelect) {
                    bestType = type;
                }
            }
        }
        return insertChild(state, kind, bestType);
    }

    public static ValuesDelta insertChild(RawContactDeltaList stateList, DataKind kind, AccountTypeManager accountTypes) {
        ValuesDelta resultEntry = null;
        int size = stateList.size();
        for (int i = 0; i < size; i++) {
            RawContactDelta state = (RawContactDelta) stateList.get(i);
            if (resultEntry == null) {
                if (isSuppportedDataKind(state, kind, accountTypes)) {
                    EditType bestType = getBestValidType(state, kind, false, Integer.MIN_VALUE);
                    if (bestType == null) {
                        bestType = getBestValidType(state, kind, true, Integer.MIN_VALUE);
                    }
                    if (kind.typeToSelect != -1) {
                        for (EditType type : kind.typeList) {
                            if (type.rawValue == kind.typeToSelect) {
                                bestType = type;
                            }
                        }
                    }
                    resultEntry = insertChild(state, kind, bestType);
                }
            } else if (isSuppportedDataKind(state, kind, accountTypes)) {
                state.addEntry(resultEntry);
            }
        }
        return resultEntry;
    }

    private static boolean isSuppportedDataKind(RawContactDelta rawContactState, DataKind destKind, AccountTypeManager accountTypes) {
        if (rawContactState == null || destKind == null) {
            return false;
        }
        DataKind validKind = rawContactState.getAccountType(accountTypes).getKindForMimetype(destKind.mimeType);
        if (validKind == null) {
            return false;
        }
        int current_cout = rawContactState.getMimeEntriesCount(destKind.mimeType, true);
        if (validKind.typeOverallMax < 0) {
            return true;
        }
        if (current_cout < validKind.typeOverallMax) {
            return true;
        }
        return false;
    }

    public static ValuesDelta insertChild(RawContactDelta state, DataKind kind, EditType type) {
        if (kind == null) {
            return null;
        }
        ContentValues after = new ContentValues();
        after.put("mimetype", kind.mimeType);
        if (kind.defaultValues != null) {
            after.putAll(kind.defaultValues);
        }
        if (!(kind.typeColumn == null || type == null)) {
            after.put(kind.typeColumn, Integer.valueOf(type.rawValue));
        }
        ValuesDelta child = ValuesDelta.fromAfter(after);
        state.addEntry(child);
        return child;
    }

    public static void trimEmpty(RawContactDeltaList set, AccountTypeManager accountTypes) {
        trimEmpty(null, set, accountTypes, false);
    }

    public static void trimEmpty(Context context, RawContactDeltaList set, AccountTypeManager accountTypes, boolean createSimGroup) {
        if (set != null) {
            for (RawContactDelta state : set) {
                ValuesDelta values = state.getValues();
                String accountType = values.getAsString("account_type");
                if (createSimGroup && ("com.android.huawei.sim".equals(accountType) || "com.android.huawei.secondsim".equals(accountType))) {
                    if (SimFactoryManager.isSIM1CardPresent()) {
                        SimStateServiceHandler.createSimGroup(context, 0);
                    }
                    if (SimFactoryManager.isSIM2CardPresent()) {
                        SimStateServiceHandler.createSimGroup(context, 1);
                    }
                }
                trimEmpty(state, accountTypes.getAccountType(accountType, values.getAsString("data_set")));
            }
        }
    }

    public static boolean hasChanges(RawContactDeltaList set, AccountTypeManager accountTypes) {
        if (set == null) {
            return false;
        }
        if (set.isMarkedForSplitting() || set.isMarkedForJoining()) {
            return true;
        }
        for (RawContactDelta state : set) {
            ValuesDelta values = state.getValues();
            if (hasChanges(state, accountTypes.getAccountType(values.getAsString("account_type"), values.getAsString("data_set")))) {
                return true;
            }
        }
        return false;
    }

    public static void trimEmpty(RawContactDelta state, AccountType accountType) {
        boolean hasValues = false;
        for (DataKind kind : accountType.getSortedDataKinds()) {
            ArrayList<ValuesDelta> entries = state.getMimeEntries(kind.mimeType);
            if (entries != null) {
                boolean isRingtone = TextUtils.equals("vnd.android.huawei.cursor.item/ringtone", kind.mimeType);
                boolean isGroupMember = TextUtils.equals("vnd.android.cursor.item/group_membership", kind.mimeType);
                if (!(isRingtone || isGroupMember)) {
                    for (ValuesDelta entry : entries) {
                        if (!entry.isInsert() ? entry.isUpdate() : true) {
                            boolean equals = TextUtils.equals("vnd.android.cursor.item/photo", kind.mimeType) ? TextUtils.equals("com.google", state.getValues().getAsString("account_type")) : false;
                            if (isEmpty(entry, kind) && !equals) {
                                entry.markDeleted();
                            } else if (!entry.isFromTemplate()) {
                                hasValues = true;
                            }
                        } else if (!(entry.isTransient() || entry.isDelete())) {
                            hasValues = true;
                        }
                    }
                }
            }
        }
        if (!hasValues) {
            state.markDeleted();
        }
    }

    private static boolean hasChanges(RawContactDelta state, AccountType accountType) {
        for (DataKind kind : accountType.getSortedDataKinds()) {
            String mimeType = kind.mimeType;
            if ("vnd.android.huawei.cursor.item/ringtone".equals(mimeType)) {
                if (state.getValues().getAfter().containsKey("custom_ringtone")) {
                    return true;
                }
            } else if (!"#phoneticName".equals(mimeType)) {
                ArrayList<ValuesDelta> entries = state.getMimeEntries(mimeType);
                if (entries != null) {
                    for (ValuesDelta entry : entries) {
                        boolean isRealInsert;
                        if (!entry.isInsert() || isEmpty(entry, kind)) {
                            isRealInsert = false;
                        } else {
                            isRealInsert = true;
                        }
                        if (isRealInsert || entry.isUpdate()) {
                            return true;
                        }
                        if (entry.isDelete()) {
                            return true;
                        }
                    }
                    continue;
                } else {
                    continue;
                }
            } else if (state.getValues().getAfter().containsKey("sort_key")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmpty(ValuesDelta values, DataKind kind) {
        boolean z = true;
        if ("vnd.android.cursor.item/photo".equals(kind.mimeType)) {
            if (values.getAsByteArray("data15") != null) {
                z = false;
            }
            return z;
        } else if (kind.fieldList == null) {
            return true;
        } else {
            for (EditField field : kind.fieldList) {
                if (ContactsUtils.isGraphic(values.getAsString(field.column))) {
                    return false;
                }
            }
            return true;
        }
    }

    protected static boolean areEqual(ValuesDelta values1, ContentValues values2, DataKind kind) {
        if (kind.fieldList == null) {
            return false;
        }
        for (EditField field : kind.fieldList) {
            if (!TextUtils.equals(values1.getAsString(field.column), values2.getAsString(field.column))) {
                return false;
            }
        }
        return true;
    }

    private static void parseExtraFromQRCode(AccountType accountType, RawContactDelta state, Bundle extras) {
        if (extras.containsKey("URL_KEY")) {
            state.setHasExtra("vnd.android.cursor.item/website", true);
            state.setExtraValue(extras.getString("URL_KEY"));
        }
        boolean hasWebsite = extras.containsKey("URL_KEY");
        DataKind kind = accountType.getKindForMimetype("vnd.android.cursor.item/website");
        if (hasWebsite && canInsert(state, kind)) {
            ValuesDelta child = insertChild(state, kind);
            String website = extras.getString("URL_KEY");
            if (ContactsUtils.isGraphic(website)) {
                child.put("data1", website);
            }
        }
        if (extras.containsKey("name")) {
            state.setHasExtra("#displayName", true);
            state.setExtraValue(extras.getString("name"));
        }
        if (extras.containsKey("postal")) {
            state.setHasExtra("vnd.android.cursor.item/postal-address_v2", true);
            state.setExtraValue(extras.getString("postal"));
        }
    }

    public static void parseExtras(Context context, AccountType accountType, RawContactDelta state, Bundle extras) {
        if (extras != null && extras.size() != 0) {
            boolean hasOrg;
            ValuesDelta child;
            parseStructuredNameExtra(context, accountType, state, extras);
            parseStructuredPostalExtra(accountType, state, extras);
            if (extras.containsKey("phone")) {
                boolean isFromCalllog;
                state.setHasExtra("vnd.android.cursor.item/phone_v2", true);
                if (!extras.getBoolean("extra_add_exist_contact", false)) {
                    if (!extras.getBoolean("isFromDetailActivityCreateContact", false)) {
                        isFromCalllog = extras.getBoolean("intent_key_is_from_dialpad", false);
                        state.setFromCalllog(isFromCalllog);
                        state.setExtraValue(extras.getString("phone"));
                    }
                }
                isFromCalllog = true;
                state.setFromCalllog(isFromCalllog);
                state.setExtraValue(extras.getString("phone"));
            }
            DataKind kind = accountType.getKindForMimetype("vnd.android.cursor.item/phone_v2");
            parseExtras(state, kind, extras, "phone_type", "phone", "data1");
            parseExtras(state, kind, extras, "secondary_phone_type", "secondary_phone", "data1");
            parseExtras(state, kind, extras, "tertiary_phone_type", "tertiary_phone", "data1");
            if (extras.containsKey(Scopes.EMAIL)) {
                state.setHasExtra("vnd.android.cursor.item/email_v2", true);
                state.setExtraValue(extras.getString(Scopes.EMAIL));
            }
            kind = accountType.getKindForMimetype("vnd.android.cursor.item/email_v2");
            parseExtras(state, kind, extras, "email_type", Scopes.EMAIL, "data1");
            parseExtras(state, kind, extras, "secondary_email_type", "secondary_email", "data1");
            parseExtras(state, kind, extras, "tertiary_email_type", "tertiary_email", "data1");
            parseExtrasExtended(state, kind, extras, "tertiary_email_extend", "data1");
            if (extras.containsKey("im_handle")) {
                state.setHasExtra("vnd.android.cursor.item/im", true);
                state.setExtraValue(extras.getString("im_handle"));
            }
            kind = accountType.getKindForMimetype("vnd.android.cursor.item/im");
            fixupLegacyImType(extras);
            parseExtras(state, kind, extras, "im_protocol", "im_handle", "data1");
            RawContactDelta rawContactDelta = state;
            Bundle bundle = extras;
            parseExtras(rawContactDelta, accountType.getKindForMimetype("vnd.android.cursor.item/im"), bundle, "sns_protocol", "sns_handle", "data1");
            if (extras.containsKey("company")) {
                state.setHasExtra("vnd.android.cursor.item/organization", true);
                state.setExtraValue(extras.getString("company"));
            }
            if (extras.containsKey("company")) {
                hasOrg = true;
            } else {
                hasOrg = extras.containsKey("job_title");
            }
            DataKind kindOrg = accountType.getKindForMimetype("vnd.android.cursor.item/organization");
            if (hasOrg && canInsert(state, kindOrg)) {
                child = insertChild(state, kindOrg);
                String company = extras.getString("company");
                if (ContactsUtils.isGraphic(company)) {
                    child.put("data1", company);
                }
                String title = extras.getString("job_title");
                if (ContactsUtils.isGraphic(title)) {
                    child.put("data4", title);
                }
            }
            if (extras.containsKey("notes")) {
                state.setHasExtra("vnd.android.cursor.item/note", true);
                state.setExtraValue(extras.getString("notes"));
            }
            boolean hasNotes = extras.containsKey("notes");
            DataKind kindNotes = accountType.getKindForMimetype("vnd.android.cursor.item/note");
            if (hasNotes && canInsert(state, kindNotes)) {
                child = insertChild(state, kindNotes);
                String notes = extras.getString("notes");
                if (ContactsUtils.isGraphic(notes)) {
                    child.put("data1", notes);
                }
            }
            parseExtraFromQRCode(accountType, state, extras);
            if (extras.getBoolean("key_from_camcard")) {
                state.setHasExtra(null, false);
            }
            ArrayList<ContentValues> values = extras.getParcelableArrayList(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH);
            if (values != null) {
                parseValues(state, accountType, values);
            }
        }
    }

    private static void parseStructuredNameExtra(Context context, AccountType accountType, RawContactDelta state, Bundle extras) {
        ensureKindExists(state, accountType, "vnd.android.cursor.item/name");
        ValuesDelta child = state.getPrimaryEntry("vnd.android.cursor.item/name");
        String name = extras.getString("name");
        if (ContactsUtils.isGraphic(name)) {
            DataKind kind = accountType.getKindForMimetype("vnd.android.cursor.item/name");
            boolean supportsDisplayName = false;
            if (kind.fieldList != null) {
                for (EditField field : kind.fieldList) {
                    if ("data1".equals(field.column)) {
                        supportsDisplayName = true;
                        break;
                    }
                }
            }
            if (supportsDisplayName) {
                child.put("data1", name);
            } else {
                Uri uri = ContactsContract.AUTHORITY_URI.buildUpon().appendPath("complete_name").appendQueryParameter("data1", name).build();
                Cursor cursor = context.getContentResolver().query(uri, new String[]{"data4", "data2", "data5", "data3", "data6"}, null, null, null);
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            child.put("data4", cursor.getString(0));
                            child.put("data2", cursor.getString(1));
                            child.put("data5", cursor.getString(2));
                            child.put("data3", cursor.getString(3));
                            child.put("data6", cursor.getString(4));
                        }
                        cursor.close();
                    } catch (Throwable th) {
                        cursor.close();
                    }
                }
            }
        }
        String phoneticName = extras.getString("phonetic_name");
        if (ContactsUtils.isGraphic(phoneticName)) {
            child.put("data7", phoneticName);
        }
    }

    private static void parseStructuredPostalExtra(AccountType accountType, RawContactDelta state, Bundle extras) {
        String address = null;
        DataKind kind = accountType.getKindForMimetype("vnd.android.cursor.item/postal-address_v2");
        ValuesDelta child = parseExtras(state, kind, extras, "postal_type", "postal", "data1");
        if (child != null) {
            address = child.getAsString("data1");
        }
        if (!TextUtils.isEmpty(address)) {
            boolean supportsFormatted = false;
            if (kind.fieldList != null) {
                for (EditField field : kind.fieldList) {
                    if ("data1".equals(field.column)) {
                        supportsFormatted = true;
                        break;
                    }
                }
            }
            if (child != null && !supportsFormatted) {
                child.put("data4", address);
                child.putNull("data1");
            }
        }
    }

    private static void parseValues(RawContactDelta state, AccountType accountType, ArrayList<ContentValues> dataValueList) {
        for (ContentValues values : dataValueList) {
            String mimeType = values.getAsString("mimetype");
            if (!TextUtils.isEmpty(mimeType) && !"vnd.android.cursor.item/name".equals(mimeType)) {
                if ("vnd.android.cursor.item/phone_v2".equals(mimeType)) {
                    values.remove("formattedPhoneNumber");
                    Integer type = values.getAsInteger("data2");
                    if (type != null && type.intValue() == 0 && TextUtils.isEmpty(values.getAsString("data3"))) {
                        values.put("data2", Integer.valueOf(2));
                    }
                }
                DataKind kind = accountType.getKindForMimetype(mimeType);
                if (kind != null) {
                    ValuesDelta entry = ValuesDelta.fromAfter(values);
                    if (!isEmpty(entry, kind)) {
                        ArrayList<ValuesDelta> entries = state.getMimeEntries(mimeType);
                        boolean addEntry;
                        if (kind.typeOverallMax != 1 || "vnd.android.cursor.item/group_membership".equals(mimeType)) {
                            addEntry = true;
                            int count = 0;
                            if (entries != null && entries.size() > 0) {
                                for (ValuesDelta delta : entries) {
                                    if (!delta.isDelete()) {
                                        if (areEqual(delta, values, kind)) {
                                            addEntry = false;
                                            break;
                                        }
                                        count++;
                                    }
                                }
                            }
                            if (kind.typeOverallMax != -1 && count >= kind.typeOverallMax) {
                                addEntry = true;
                            }
                            if (addEntry && adjustType(entry, entries, kind)) {
                                state.addEntry(entry);
                            }
                        } else {
                            addEntry = true;
                            if (entries != null && entries.size() > 0) {
                                for (ValuesDelta delta2 : entries) {
                                    if (!delta2.isDelete() && !isEmpty(delta2, kind)) {
                                        addEntry = false;
                                        break;
                                    }
                                }
                                if (addEntry) {
                                    for (ValuesDelta delta22 : entries) {
                                        delta22.markDeleted();
                                    }
                                }
                            }
                            if (addEntry) {
                                addEntry = adjustType(entry, entries, kind);
                            }
                            if (addEntry) {
                                state.addEntry(entry);
                            } else if ("vnd.android.cursor.item/note".equals(mimeType) && entries != null) {
                                for (ValuesDelta delta222 : entries) {
                                    if (!isEmpty(delta222, kind)) {
                                        delta222.put("data1", delta222.getAsString("data1") + "\n" + values.getAsString("data1"));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean adjustType(ValuesDelta entry, ArrayList<ValuesDelta> entries, DataKind kind) {
        if (kind.typeColumn == null || kind.typeList == null || kind.typeList.size() == 0) {
            return true;
        }
        Integer typeInteger = entry.getAsInteger(kind.typeColumn);
        int type = typeInteger != null ? typeInteger.intValue() : ((EditType) kind.typeList.get(0)).rawValue;
        if (isTypeAllowed(type, entries, kind)) {
            entry.put(kind.typeColumn, type);
            return true;
        }
        int size = kind.typeList.size();
        for (int i = 0; i < size; i++) {
            EditType editType = (EditType) kind.typeList.get(i);
            if (isTypeAllowed(editType.rawValue, entries, kind)) {
                entry.put(kind.typeColumn, editType.rawValue);
                return true;
            }
        }
        return false;
    }

    private static boolean isTypeAllowed(int type, ArrayList<ValuesDelta> entries, DataKind kind) {
        boolean z = true;
        int max = 0;
        int size = kind.typeList.size();
        for (int i = 0; i < size; i++) {
            EditType editType = (EditType) kind.typeList.get(i);
            if (editType.rawValue == type) {
                max = editType.specificMax;
                break;
            }
        }
        if (max == 0) {
            return false;
        }
        if (max == -1) {
            return true;
        }
        if (getEntryCountByType(entries, kind.typeColumn, type) >= max) {
            z = false;
        }
        return z;
    }

    private static int getEntryCountByType(ArrayList<ValuesDelta> entries, String typeColumn, int type) {
        int count = 0;
        if (entries != null) {
            for (ValuesDelta entry : entries) {
                Integer typeInteger = entry.getAsInteger(typeColumn);
                if (typeInteger != null && typeInteger.intValue() == type) {
                    count++;
                }
            }
        }
        return count;
    }

    private static void fixupLegacyImType(Bundle bundle) {
        String encodedString = bundle.getString("im_protocol");
        if (encodedString != null) {
            try {
                Object protocol = ContactMethods.decodeImProtocol(encodedString);
                if (protocol instanceof Integer) {
                    bundle.putInt("im_protocol", ((Integer) protocol).intValue());
                } else {
                    bundle.putString("im_protocol", (String) protocol);
                }
            } catch (IllegalArgumentException e) {
            }
        }
    }

    public static ValuesDelta parseExtras(RawContactDelta state, DataKind kind, Bundle extras, String typeExtra, String valueExtra, String valueColumn) {
        int i = 0;
        CharSequence value = extras.getCharSequence(valueExtra);
        if (kind == null) {
            return null;
        }
        boolean validValue;
        boolean canInsert = canInsert(state, kind);
        if (value != null) {
            validValue = TextUtils.isGraphic(value);
        } else {
            validValue = false;
        }
        if (!validValue || !canInsert) {
            return null;
        }
        state.setExtraValue(value.toString());
        if (!extras.containsKey(typeExtra)) {
            i = Integer.MIN_VALUE;
        }
        EditType editType = getBestValidType(state, kind, true, extras.getInt(typeExtra, i));
        ValuesDelta child = insertChild(state, kind, editType);
        child.put(valueColumn, value.toString());
        if (!(editType == null || editType.customColumn == null)) {
            child.put(editType.customColumn, extras.getString(typeExtra));
        }
        return child;
    }

    public static void parseExtrasExtended(RawContactDelta state, DataKind kind, Bundle extras, String valueExtra, String valueColumn) {
        ArrayList<String> values = extras.getStringArrayList(valueExtra);
        if (values != null) {
            for (String value : values) {
                if (kind != null) {
                    boolean canInsert = canInsert(state, kind);
                    if ((value != null ? TextUtils.isGraphic(value) : false) && canInsert) {
                        state.setExtraValue(value);
                        insertChild(state, kind, getBestValidType(state, kind, true, 2)).put(valueColumn, value.toString());
                    } else {
                        return;
                    }
                }
                return;
            }
        }
    }

    public static void migrateStateForNewContact(Context context, RawContactDelta oldState, RawContactDelta newState, AccountType oldAccountType, AccountType newAccountType) {
        String mimeType;
        if (newAccountType == oldAccountType) {
            for (DataKind kind : newAccountType.getSortedDataKinds()) {
                mimeType = kind.mimeType;
                if ("vnd.android.cursor.item/name".equals(mimeType)) {
                    migrateStructuredName(context, oldState, newState, kind);
                } else {
                    List<ValuesDelta> entryList = oldState.getMimeEntries(mimeType);
                    if (!(entryList == null || entryList.isEmpty())) {
                        for (ValuesDelta entry : entryList) {
                            ContentValues values = entry.getAfter();
                            if (values != null) {
                                newState.addEntry(ValuesDelta.fromAfter(values));
                            }
                        }
                    }
                }
            }
            return;
        }
        for (DataKind kind2 : newAccountType.getSortedDataKinds()) {
            if (kind2.editable) {
                mimeType = kind2.mimeType;
                String extraMimetype = oldState.getExtraMimetype();
                String extraValue = oldState.getExtraValue();
                if (extraMimetype != null && extraMimetype.equals(mimeType)) {
                    newState.setHasExtra(extraMimetype, oldState.getExtraBoolean());
                    newState.setExtraValue(extraValue);
                }
                if (!("#displayName".equals(mimeType) || "#phoneticName".equals(mimeType))) {
                    if ("vnd.android.cursor.item/name".equals(mimeType)) {
                        migrateStructuredName(context, oldState, newState, kind2);
                    } else if ("vnd.android.cursor.item/postal-address_v2".equals(mimeType)) {
                        migratePostal(oldState, newState, kind2);
                    } else if ("vnd.android.cursor.item/contact_event".equals(mimeType)) {
                        migrateEvent(oldState, newState, kind2, null);
                    } else if (sGenericMimeTypesWithoutTypeSupport.contains(mimeType)) {
                        migrateGenericWithoutTypeColumn(oldState, newState, kind2);
                    } else if (sGenericMimeTypesWithTypeSupport.contains(mimeType)) {
                        migrateGenericWithTypeColumn(oldState, newState, kind2);
                    } else {
                        throw new IllegalStateException("Unexpected editable mime-type: " + mimeType);
                    }
                }
            }
        }
    }

    private static ArrayList<ValuesDelta> ensureEntryMaxSize(RawContactDelta newState, DataKind kind, ArrayList<ValuesDelta> mimeEntries) {
        if (mimeEntries == null) {
            return null;
        }
        int typeOverallMax = kind.typeOverallMax;
        if (typeOverallMax >= 0 && mimeEntries.size() > typeOverallMax) {
            ArrayList<ValuesDelta> newMimeEntries = new ArrayList(typeOverallMax);
            for (int i = 0; i < typeOverallMax; i++) {
                newMimeEntries.add((ValuesDelta) mimeEntries.get(i));
            }
            mimeEntries = newMimeEntries;
        }
        return mimeEntries;
    }

    public static void migrateStructuredName(Context context, RawContactDelta oldState, RawContactDelta newState, DataKind newDataKind) {
        ValuesDelta delta = oldState.getPrimaryEntry("vnd.android.cursor.item/name");
        if (delta != null) {
            ContentValues values = delta.getAfter();
            if (values != null) {
                boolean supportDisplayName = false;
                boolean supportPhoneticFullName = false;
                boolean supportPhoneticFamilyName = false;
                boolean supportPhoneticMiddleName = false;
                boolean supportPhoneticGivenName = false;
                for (EditField editField : newDataKind.fieldList) {
                    if ("data1".equals(editField.column)) {
                        supportDisplayName = true;
                    }
                    if ("#phoneticName".equals(editField.column)) {
                        supportPhoneticFullName = true;
                    }
                    if ("data9".equals(editField.column)) {
                        supportPhoneticFamilyName = true;
                    }
                    if ("data8".equals(editField.column)) {
                        supportPhoneticMiddleName = true;
                    }
                    if ("data7".equals(editField.column)) {
                        supportPhoneticGivenName = true;
                    }
                }
                String displayName = values.getAsString("data1");
                if (TextUtils.isEmpty(displayName)) {
                    if (supportDisplayName) {
                        values.put("data1", NameConverter.structuredNameToDisplayName(context, values));
                        for (String field : NameConverter.getStructuredNameFields()) {
                            values.remove(field);
                        }
                    }
                } else if (!supportDisplayName) {
                    NameConverter.displayNameToStructuredName(context, displayName, values);
                    values.remove("data1");
                }
                String phoneticFullName = values.getAsString("#phoneticName");
                if (TextUtils.isEmpty(phoneticFullName)) {
                    if (supportPhoneticFullName) {
                        values.put("#phoneticName", PhoneticNameEditorView.buildPhoneticName(values.getAsString("data9"), values.getAsString("data8"), values.getAsString("data7")));
                    }
                    if (!supportPhoneticFamilyName) {
                        values.remove("data9");
                    }
                    if (!supportPhoneticMiddleName) {
                        values.remove("data8");
                    }
                    if (!supportPhoneticGivenName) {
                        values.remove("data7");
                    }
                } else if (!supportPhoneticFullName) {
                    StructuredNameDataItem tmpItem = PhoneticNameEditorView.parsePhoneticName(phoneticFullName, null);
                    values.remove("#phoneticName");
                    if (supportPhoneticFamilyName) {
                        values.put("data9", tmpItem.getPhoneticFamilyName());
                    } else {
                        values.remove("data9");
                    }
                    if (supportPhoneticMiddleName) {
                        values.put("data8", tmpItem.getPhoneticMiddleName());
                    } else {
                        values.remove("data8");
                    }
                    if (supportPhoneticGivenName) {
                        values.put("data7", tmpItem.getPhoneticGivenName());
                    } else {
                        values.remove("data7");
                    }
                }
                newState.addEntry(ValuesDelta.fromAfter(values));
            }
        }
    }

    public static void migratePostal(RawContactDelta oldState, RawContactDelta newState, DataKind newDataKind) {
        ArrayList<ValuesDelta> mimeEntries = ensureEntryMaxSize(newState, newDataKind, oldState.getMimeEntries("vnd.android.cursor.item/postal-address_v2"));
        if (mimeEntries != null && !mimeEntries.isEmpty()) {
            boolean supportFormattedAddress = false;
            boolean supportStreet = false;
            String firstColumn = ((EditField) newDataKind.fieldList.get(0)).column;
            for (EditField editField : newDataKind.fieldList) {
                if ("data1".equals(editField.column)) {
                    supportFormattedAddress = true;
                }
                if ("data4".equals(editField.column)) {
                    supportStreet = true;
                }
            }
            Set<Integer> supportedTypes = new HashSet();
            if (!(newDataKind.typeList == null || newDataKind.typeList.isEmpty())) {
                for (EditType editType : newDataKind.typeList) {
                    supportedTypes.add(Integer.valueOf(editType.rawValue));
                }
            }
            for (ValuesDelta entry : mimeEntries) {
                ContentValues values = entry.getAfter();
                if (values != null) {
                    Integer oldType = values.getAsInteger("data2");
                    if (!supportedTypes.contains(oldType)) {
                        int defaultType = 0;
                        if (newDataKind.defaultValues != null) {
                            defaultType = newDataKind.defaultValues.getAsInteger("data2").intValue();
                        } else if (!(newDataKind.typeList == null || newDataKind.typeList.isEmpty())) {
                            defaultType = ((EditType) newDataKind.typeList.get(0)).rawValue;
                        }
                        values.put("data2", Integer.valueOf(defaultType));
                        if (oldType != null && oldType.intValue() == 0) {
                            values.remove("data3");
                        }
                    }
                    String formattedAddress = values.getAsString("data1");
                    if (TextUtils.isEmpty(formattedAddress)) {
                        if (supportFormattedAddress) {
                            String[] structuredData = Locale.JAPANESE.getLanguage().equals(Locale.getDefault().getLanguage()) ? new String[]{values.getAsString("data10"), values.getAsString("data9"), values.getAsString("data8"), values.getAsString("data7"), values.getAsString("data6"), values.getAsString("data4"), values.getAsString("data5")} : new String[]{values.getAsString("data5"), values.getAsString("data4"), values.getAsString("data6"), values.getAsString("data7"), values.getAsString("data8"), values.getAsString("data9"), values.getAsString("data10")};
                            StringBuilder builder = new StringBuilder();
                            for (String elem : structuredData) {
                                if (!TextUtils.isEmpty(elem)) {
                                    builder.append(elem).append("\n");
                                }
                            }
                            values.put("data1", builder.toString());
                            values.remove("data5");
                            values.remove("data4");
                            values.remove("data6");
                            values.remove("data7");
                            values.remove("data8");
                            values.remove("data9");
                            values.remove("data10");
                        }
                    } else if (!supportFormattedAddress) {
                        values.remove("data1");
                        if (supportStreet) {
                            values.put("data4", formattedAddress);
                        } else {
                            values.put(firstColumn, formattedAddress);
                        }
                    }
                    newState.addEntry(ValuesDelta.fromAfter(values));
                }
            }
        }
    }

    public static void migrateEvent(RawContactDelta oldState, RawContactDelta newState, DataKind newDataKind, Integer defaultYear) {
        ArrayList<ValuesDelta> mimeEntries = ensureEntryMaxSize(newState, newDataKind, oldState.getMimeEntries("vnd.android.cursor.item/contact_event"));
        if (mimeEntries != null && !mimeEntries.isEmpty()) {
            SparseArray<EventEditType> allowedTypes = new SparseArray();
            for (EditType editType : newDataKind.typeList) {
                allowedTypes.put(editType.rawValue, (EventEditType) editType);
            }
            for (ValuesDelta entry : mimeEntries) {
                ContentValues values = entry.getAfter();
                if (values != null) {
                    String dateString = values.getAsString("data1");
                    Integer type = values.getAsInteger("data2");
                    if (!(type == null || allowedTypes.indexOfKey(type.intValue()) < 0 || TextUtils.isEmpty(dateString))) {
                        EventEditType suitableType = (EventEditType) allowedTypes.get(type.intValue());
                        ParsePosition parsePosition = new ParsePosition(0);
                        boolean yearOptional = false;
                        Date date = DateUtils.getDateAndTimeFormat().parse(dateString, parsePosition);
                        if (date == null) {
                            yearOptional = true;
                            date = DateUtils.getNoYearDateFormat().parse(dateString, parsePosition);
                        }
                        if (!(date == null || !yearOptional || suitableType == null || suitableType.isYearOptional())) {
                            Calendar calendar = Calendar.getInstance(DateUtils.UTC_TIMEZONE, Locale.US);
                            if (defaultYear == null) {
                                defaultYear = Integer.valueOf(calendar.get(1));
                            }
                            calendar.setTime(date);
                            calendar.set(defaultYear.intValue(), calendar.get(2), calendar.get(5), EventFieldEditorView.getDefaultHourForBirthday(), 0, 0);
                            values.put("data1", DateUtils.getFullDateFormat().format(calendar.getTime()));
                        }
                        newState.addEntry(ValuesDelta.fromAfter(values));
                    }
                }
            }
        }
    }

    public static void migrateGenericWithoutTypeColumn(RawContactDelta oldState, RawContactDelta newState, DataKind newDataKind) {
        ArrayList<ValuesDelta> mimeEntries = ensureEntryMaxSize(newState, newDataKind, oldState.getMimeEntries(newDataKind.mimeType));
        if (mimeEntries != null && !mimeEntries.isEmpty()) {
            for (ValuesDelta entry : mimeEntries) {
                ContentValues values = entry.getAfter();
                if (values != null) {
                    newState.addEntry(ValuesDelta.fromAfter(values));
                }
            }
        }
    }

    public static void migrateGenericWithTypeColumn(RawContactDelta oldState, RawContactDelta newState, DataKind newDataKind) {
        ArrayList<ValuesDelta> mimeEntries = oldState.getMimeEntries(newDataKind.mimeType);
        if (mimeEntries != null && !mimeEntries.isEmpty()) {
            Integer defaultType = null;
            if (newDataKind.defaultValues != null) {
                defaultType = newDataKind.defaultValues.getAsInteger("data2");
            }
            Set<Integer> allowedTypes = new TreeSet();
            SparseIntArray typeSpecificMaxMap = new SparseIntArray();
            if (defaultType != null) {
                allowedTypes.add(defaultType);
                typeSpecificMaxMap.put(defaultType.intValue(), -1);
            }
            if (!("vnd.android.cursor.item/im".equals(newDataKind.mimeType) || newDataKind.typeList == null || newDataKind.typeList.isEmpty())) {
                for (EditType editType : newDataKind.typeList) {
                    allowedTypes.add(Integer.valueOf(editType.rawValue));
                    typeSpecificMaxMap.put(editType.rawValue, editType.specificMax);
                }
                if (defaultType == null) {
                    defaultType = Integer.valueOf(((EditType) newDataKind.typeList.get(0)).rawValue);
                }
            }
            if (defaultType == null) {
                HwLog.w(TAG, "Default type isn't available for mimetype " + newDataKind.mimeType);
            }
            int typeOverallMax = newDataKind.typeOverallMax;
            SparseIntArray currentEntryCount = new SparseIntArray();
            int totalCount = 0;
            for (ValuesDelta entry : mimeEntries) {
                if (typeOverallMax != -1 && totalCount >= typeOverallMax) {
                    break;
                }
                ContentValues values = entry.getAfter();
                if (values != null) {
                    Integer typeForNewAccount;
                    Integer oldType = entry.getAsInteger("data2");
                    if (allowedTypes.contains(oldType)) {
                        typeForNewAccount = oldType;
                    } else if (defaultType != null) {
                        typeForNewAccount = defaultType;
                        values.put("data2", defaultType);
                        if (oldType != null && oldType.intValue() == 0) {
                            values.remove("data3");
                        }
                    } else {
                        typeForNewAccount = null;
                        values.remove("data2");
                    }
                    if (typeForNewAccount != null) {
                        int specificMax = typeSpecificMaxMap.get(typeForNewAccount.intValue(), 0);
                        if (specificMax >= 0) {
                            Integer newType = typeForNewAccount;
                            if (currentEntryCount.get(typeForNewAccount.intValue(), 0) >= specificMax) {
                                newType = getNextValidType(allowedTypes, typeSpecificMaxMap, currentEntryCount);
                                if (newType.intValue() == -1) {
                                }
                            }
                            int newTypeCount = currentEntryCount.get(newType.intValue(), 0);
                            values.put("data2", newType);
                            currentEntryCount.put(newType.intValue(), newTypeCount + 1);
                        }
                    }
                    newState.addEntry(ValuesDelta.fromAfter(values));
                    totalCount++;
                }
            }
        }
    }

    private static Integer getNextValidType(Set<Integer> allowedTypes, SparseIntArray typeSpecificMaxMap, SparseIntArray currentEntryCount) {
        Integer nextValidType = Integer.valueOf(-1);
        for (Integer type : allowedTypes) {
            int specificMax = typeSpecificMaxMap.get(type.intValue(), 0);
            if (specificMax >= 0 && currentEntryCount.get(type.intValue(), 0) < specificMax) {
                return type;
            }
        }
        return nextValidType;
    }
}
