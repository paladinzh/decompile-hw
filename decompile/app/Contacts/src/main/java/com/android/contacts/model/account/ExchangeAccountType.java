package com.android.contacts.model.account;

import android.content.ContentValues;
import android.content.Context;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.model.account.AccountType.DefinitionException;
import com.android.contacts.model.account.AccountType.EditField;
import com.android.contacts.model.account.BaseAccountType.EventActionInflater;
import com.android.contacts.model.account.BaseAccountType.PostalActionInflater;
import com.android.contacts.model.account.BaseAccountType.SimpleInflater;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.util.DateUtils;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.google.common.collect.Lists;
import com.huawei.cust.HwCustUtils;

public class ExchangeAccountType extends BaseAccountType {
    HwCustExchangeAccountType mCust = ((HwCustExchangeAccountType) HwCustUtils.createObj(HwCustExchangeAccountType.class, new Object[0]));

    public ExchangeAccountType(Context context, String authenticatorPackageName, String type) {
        this.accountType = type;
        this.resourcePackageName = null;
        this.syncAdapterPackageName = authenticatorPackageName;
        try {
            addDataKindStructuredName(context);
            addDataKindDisplayName(context);
            addDataKindPhoneticName(context);
            addDataKindNickname(context);
            addDataKindPhone(context);
            addDataKindEmail(context);
            addDataKindStructuredPostal(context);
            addDataKindIm(context);
            addDataKindOrganization(context);
            addDataKindPhoto(context);
            addRingtone(context);
            addDataKindNote(context);
            addDataKindEvent(context);
            addDataKindWebsite(context);
            addDataKindGroupMembership(context);
            this.mIsInitialized = true;
        } catch (DefinitionException e) {
            HwLog.e("ExchangeAccountType", "Problem building account type", e);
        }
    }

    public static boolean isExchangeType(String type) {
        return (HwCustCommonConstants.EAS_ACCOUNT_TYPE.equals(type) || "com.google.android.exchange".equals(type)) ? true : "com.google.android.gm.exchange".equals(type);
    }

    protected DataKind addDataKindPhoneticName(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("#phoneticName", R.string.name_phonetic, -1, true));
        kind.actionHeader = new SimpleInflater((int) R.string.nameLabelsGroup);
        kind.actionBody = new SimpleInflater("data1");
        kind.typeOverallMax = 1;
        kind.fieldList = Lists.newArrayList();
        if (context.getResources().getConfiguration().locale.getLanguage().equals("zh")) {
            kind.fieldList.add(new EditField("#phoneticName", R.string.name_phonetic, 193).setShortForm(true));
        } else {
            kind.fieldList.add(new EditField("data9", R.string.name_phonetic_family, 193));
            kind.fieldList.add(new EditField("data7", R.string.name_phonetic_given, 193));
        }
        return kind;
    }

    protected DataKind addDataKindNickname(Context context) throws DefinitionException {
        DataKind kind = super.addDataKindNickname(context);
        kind.typeOverallMax = 1;
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.nicknameLabelsGroup, 8289));
        return kind;
    }

    protected DataKind addDataKindPhone(Context context) throws DefinitionException {
        DataKind kind = super.addDataKindPhone(context);
        kind.typeOverallMax = 13;
        kind.typeColumn = "data2";
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(BaseAccountType.buildPhoneType(2).setSpecificMax(1));
        kind.typeList.add(BaseAccountType.buildPhoneType(1).setSpecificMax(2));
        if (this.mCust == null || !this.mCust.isAdditionalSprintFieldsRequired()) {
            kind.typeList.add(BaseAccountType.buildPhoneType(3).setSpecificMax(2));
            kind.typeList.add(BaseAccountType.buildPhoneType(4).setSecondary(true).setSpecificMax(1));
            kind.typeList.add(BaseAccountType.buildPhoneType(5).setSecondary(true).setSpecificMax(1));
            kind.typeList.add(BaseAccountType.buildPhoneType(9).setSecondary(true).setSpecificMax(1));
            kind.typeList.add(BaseAccountType.buildPhoneType(20).setSecondary(true).setSpecificMax(1));
        } else {
            this.mCust.addCustomDataKindPhone(kind.typeList);
        }
        kind.typeList.add(BaseAccountType.buildPhoneType(6).setSecondary(true).setSpecificMax(1));
        kind.typeList.add(BaseAccountType.buildPhoneType(10).setSecondary(true).setSpecificMax(1));
        kind.typeList.add(BaseAccountType.buildPhoneType(14).setSecondary(true).setSpecificMax(1));
        kind.typeList.add(BaseAccountType.buildPhoneType(19).setSecondary(true).setSpecificMax(1));
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.phoneLabelsGroup, 3));
        return kind;
    }

    protected DataKind addDataKindEmail(Context context) throws DefinitionException {
        DataKind kind = super.addDataKindEmail(context);
        kind.typeOverallMax = 3;
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.emailLabelsGroup, 33));
        return kind;
    }

    protected DataKind addDataKindStructuredPostal(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/postal-address_v2", R.string.contact_postalLabelsGroup, 25, true));
        kind.actionHeader = new PostalActionInflater();
        kind.actionBody = new SimpleInflater("data1");
        kind.typeColumn = "data2";
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(BaseAccountType.buildPostalType(2).setSpecificMax(1));
        kind.typeList.add(BaseAccountType.buildPostalType(1).setSpecificMax(1));
        kind.typeList.add(BaseAccountType.buildPostalType(3).setSpecificMax(1));
        kind.typeList.add(BaseAccountType.buildPostalType(0).setSecondary(true).setCustomColumn("data3"));
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.postal_address, 139377));
        kind.maxLinesForDisplay = 10;
        return kind;
    }

    protected DataKind addDataKindIm(Context context) throws DefinitionException {
        DataKind kind = super.addDataKindIm(context);
        kind.typeOverallMax = 3;
        kind.defaultValues = new ContentValues();
        kind.defaultValues.put("data2", Integer.valueOf(3));
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.imLabelsGroup, 33));
        return kind;
    }

    protected DataKind addDataKindOrganization(Context context) throws DefinitionException {
        DataKind kind = super.addDataKindOrganization(context);
        kind.typeOverallMax = 1;
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.add_organization, 8193).setShortForm(true));
        kind.fieldList.add(new EditField("data1", R.string.ghostData_company, 8193).setLongForm(true));
        kind.fieldList.add(new EditField("data4", R.string.ghostData_title, 8193).setLongForm(true));
        return kind;
    }

    protected DataKind addDataKindPhoto(Context context) throws DefinitionException {
        DataKind kind = super.addDataKindPhoto(context);
        kind.typeOverallMax = 1;
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data15", -1, -1));
        return kind;
    }

    protected DataKind addDataKindNote(Context context) throws DefinitionException {
        DataKind kind = super.addDataKindNote(context);
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.label_notes, 147457));
        return kind;
    }

    protected DataKind addDataKindEvent(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/contact_event", R.string.eventLabelsGroup, 150, true));
        kind.actionHeader = new EventActionInflater();
        kind.actionBody = new SimpleInflater("data1");
        kind.typeOverallMax = -1;
        kind.typeColumn = "data2";
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(BaseAccountType.buildEventType(3, false).setSpecificMax(1));
        kind.typeList.add(BaseAccountType.buildEventType(1, false));
        kind.typeList.add(BaseAccountType.buildEventType(2, false));
        kind.defaultValues = new ContentValues();
        kind.defaultValues.put("data2", Integer.valueOf(3));
        kind.dateFormatWithYear = DateUtils.getDateAndTimeFormat();
        kind.dateFormatWithoutYear = DateUtils.getNoYearDateFormat();
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.eventLabelsGroup, 1));
        return kind;
    }

    protected DataKind addDataKindWebsite(Context context) throws DefinitionException {
        DataKind kind = super.addDataKindWebsite(context);
        kind.typeOverallMax = 1;
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.websiteLabelsGroup, 17));
        return kind;
    }

    public boolean isGroupMembershipEditable() {
        return true;
    }

    public boolean areContactsWritable() {
        return true;
    }

    public boolean isProfileEditable() {
        return true;
    }
}
