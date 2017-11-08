package com.android.contacts.model.account;

import android.content.ContentValues;
import android.content.Context;
import com.android.contacts.model.account.AccountType.DefinitionException;
import com.android.contacts.model.account.AccountType.EditField;
import com.android.contacts.model.account.BaseAccountType.EventActionInflater;
import com.android.contacts.model.account.BaseAccountType.RelationActionInflater;
import com.android.contacts.model.account.BaseAccountType.SimpleInflater;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.util.DateUtils;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.google.common.collect.Lists;
import java.util.List;

public class GoogleAccountType extends BaseAccountType {
    private static final List<String> mExtensionPackages = Lists.newArrayList("com.google.android.apps.plus");

    public GoogleAccountType(Context context, String authenticatorPackageName) {
        this.accountType = "com.google";
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
            addDataKindWebsite(context);
            addDataKindSipAddress(context);
            addDataKindGroupMembership(context);
            addDataKindRelation(context);
            addDataKindEvent(context);
            this.mIsInitialized = true;
        } catch (DefinitionException e) {
            HwLog.e("GoogleAccountType", "Problem building account type", e);
        }
    }

    public List<String> getExtensionPackageNames() {
        return mExtensionPackages;
    }

    protected DataKind addDataKindPhone(Context context) throws DefinitionException {
        DataKind kind = super.addDataKindPhone(context);
        kind.typeColumn = "data2";
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(BaseAccountType.buildPhoneType(2));
        kind.typeList.add(BaseAccountType.buildPhoneType(3));
        kind.typeList.add(BaseAccountType.buildPhoneType(1));
        kind.typeList.add(BaseAccountType.buildPhoneType(12));
        kind.typeList.add(BaseAccountType.buildPhoneType(4).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(5).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(6).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(7));
        kind.typeList.add(BaseAccountType.buildPhoneType(0).setSecondary(true).setCustomColumn("data3"));
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.phoneLabelsGroup, 3));
        return kind;
    }

    protected DataKind addDataKindEmail(Context context) throws DefinitionException {
        DataKind kind = super.addDataKindEmail(context);
        kind.typeColumn = "data2";
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(BaseAccountType.buildEmailType(1));
        kind.typeList.add(BaseAccountType.buildEmailType(2));
        kind.typeList.add(BaseAccountType.buildEmailType(3));
        kind.typeList.add(BaseAccountType.buildEmailType(0).setSecondary(true).setCustomColumn("data3"));
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.emailLabelsGroup, 33));
        return kind;
    }

    private DataKind addDataKindRelation(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/relation", R.string.relationLabelsGroup, 160, true));
        kind.actionHeader = new RelationActionInflater();
        kind.actionBody = new SimpleInflater("data1");
        kind.typeColumn = "data2";
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(BaseAccountType.buildRelationType(1));
        kind.typeList.add(BaseAccountType.buildRelationType(2));
        kind.typeList.add(BaseAccountType.buildRelationType(3));
        kind.typeList.add(BaseAccountType.buildRelationType(4));
        kind.typeList.add(BaseAccountType.buildRelationType(5));
        kind.typeList.add(BaseAccountType.buildRelationType(6));
        kind.typeList.add(BaseAccountType.buildRelationType(7));
        kind.typeList.add(BaseAccountType.buildRelationType(8));
        kind.typeList.add(BaseAccountType.buildRelationType(9));
        kind.typeList.add(BaseAccountType.buildRelationType(10));
        kind.typeList.add(BaseAccountType.buildRelationType(11));
        kind.typeList.add(BaseAccountType.buildRelationType(12));
        kind.typeList.add(BaseAccountType.buildRelationType(13));
        kind.typeList.add(BaseAccountType.buildRelationType(14));
        kind.typeList.add(BaseAccountType.buildRelationType(0).setSecondary(true).setCustomColumn("data3"));
        kind.defaultValues = new ContentValues();
        kind.defaultValues.put("data2", Integer.valueOf(14));
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.relationLabelsGroup, 8289));
        return kind;
    }

    private DataKind addDataKindEvent(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/contact_event", R.string.eventLabelsGroup, 150, true));
        kind.actionHeader = new EventActionInflater();
        kind.actionBody = new SimpleInflater("data1");
        kind.typeColumn = "data2";
        kind.typeList = Lists.newArrayList();
        DateUtils.setDataKindDateFormat(kind, false);
        kind.typeList.add(BaseAccountType.buildEventType(3, true).setSpecificMax(1));
        kind.typeList.add(BaseAccountType.buildEventType(1, false));
        kind.typeList.add(BaseAccountType.buildEventType(2, false));
        kind.typeList.add(BaseAccountType.buildEventType(0, false).setSecondary(true).setCustomColumn("data3"));
        kind.defaultValues = new ContentValues();
        kind.defaultValues.put("data2", Integer.valueOf(3));
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.eventLabelsGroup, 1));
        return kind;
    }

    public boolean isGroupMembershipEditable() {
        return true;
    }

    public boolean areContactsWritable() {
        return true;
    }

    public String getViewContactNotifyServiceClassName() {
        return "com.google.android.syncadapters.contacts.SyncHighResPhotoIntentService";
    }

    public String getViewContactNotifyServicePackageName() {
        return "com.google.android.syncadapters.contacts";
    }
}
