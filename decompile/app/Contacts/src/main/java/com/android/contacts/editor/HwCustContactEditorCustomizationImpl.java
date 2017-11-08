package com.android.contacts.editor;

import android.content.Context;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.view.View;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.R;
import java.util.ArrayList;

public class HwCustContactEditorCustomizationImpl extends HwCustContactEditorCustomization {
    private static final int FIRST_SIM_ACCOUNT = 1;
    private static final int SECOND_SIM_ACCOUNT = 2;
    private boolean mIsATTMyInfoHandled;
    private boolean mIsProfile;

    public void handleSaveCustomization(boolean aIsEditingUserProfile, RawContactDeltaList aState, Context aContext) {
        if (HwCustCommonConstants.IS_AAB_ATT || HwCustContactFeatureUtils.isAutoInsertSimNumberToProfile()) {
            checkAndUpdateNameNumber(aIsEditingUserProfile, aState, aContext);
            setPrimaryEntries(aState);
        }
    }

    private void checkAndUpdateNameNumber(boolean aIsEditingUserProfile, RawContactDeltaList aState, Context aContext) {
        if (aIsEditingUserProfile) {
            for (RawContactDelta delta : aState) {
                ArrayList<ValuesDelta> nameEntries = delta.getMimeEntries("vnd.android.cursor.item/name");
                if (!(nameEntries == null || HwCustCommonConstants.IS_AAB_ATT || HwCustContactFeatureUtils.IS_ATT_MY_INFO)) {
                    ValuesDelta lNameDelta = (ValuesDelta) nameEntries.get(0);
                    if (lNameDelta != null && TextUtils.isEmpty(lNameDelta.getAsString("data1"))) {
                        lNameDelta.put("data1", aContext.getString(R.string.string_aab_my_info));
                    }
                }
                ArrayList<ValuesDelta> numberEntries = delta.getMimeEntries("vnd.android.cursor.item/phone_v2");
                if (numberEntries != null) {
                    for (ValuesDelta lNumberDelta : numberEntries) {
                        if (lNumberDelta != null && !TextUtils.isEmpty(lNumberDelta.getAsString("data1")) && !lNumberDelta.isDelete()) {
                            lNumberDelta.put("data15", 1);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void addSectionViewProperty(boolean aIsProfile) {
        if (HwCustCommonConstants.IS_AAB_ATT || HwCustContactFeatureUtils.isAutoInsertSimNumberToProfile()) {
            this.mIsProfile = aIsProfile;
        }
    }

    public void customizeDefaultAccount(ContactEditorUtils aEditorUtils, Context aContext) {
        if (HwCustCommonConstants.IS_AAB_ATT) {
            for (AccountWithDataSet account : AccountTypeManager.getInstance(aContext).getAccounts(true)) {
                if (account.type != null && HwCustCommonConstants.AAB_ACCOUNT_TYPE.equals(account.type)) {
                    aEditorUtils.saveDefaultAndAllAccounts(account);
                    break;
                }
            }
        } else if (isContactDefaultSimAccount(aContext)) {
            boolean isSim1Present = SimFactoryManager.hasIccCard(0);
            boolean isSim2Present = SimFactoryManager.hasIccCard(1);
            if (isSim1Present || isSim2Present) {
                AccountWithDataSet lDefaultAccount;
                if (isSim1Present || !isSim2Present) {
                    lDefaultAccount = aEditorUtils.getPredefinedDefaultAccount(1);
                } else {
                    lDefaultAccount = aEditorUtils.getPredefinedDefaultAccount(2);
                }
                if (aEditorUtils.isValidAccount(lDefaultAccount)) {
                    aEditorUtils.saveDefaultAndAllAccounts(lDefaultAccount);
                }
            }
        }
    }

    public void handleEditorCustomization(DataKind mKind, ValuesDelta entry, RawContactDelta mState, boolean mReadOnly, ViewIdGenerator mViewIdGenerator, View view) {
        if ((HwCustCommonConstants.IS_AAB_ATT || HwCustContactFeatureUtils.isAutoInsertSimNumberToProfile()) && this.mIsProfile && !this.mIsATTMyInfoHandled && (view instanceof Editor) && mKind.fieldList.size() > 0 && "vnd.android.cursor.item/phone_v2".equals(mKind.mimeType)) {
            Editor editor = (Editor) view;
            if (TextUtils.isEmpty(entry.getAsString("data1"))) {
                editor.setValues(mKind, entry, mState, mReadOnly, mViewIdGenerator);
                editor.setDeletable(true);
            } else {
                editor.setValues(mKind, entry, mState, true, mViewIdGenerator);
                editor.setDeletable(false);
            }
            this.mIsATTMyInfoHandled = true;
        }
    }

    public void setPrimaryEntries(RawContactDeltaList aState) {
        if (HwCustCommonConstants.IS_AAB_ATT || HwCustContactFeatureUtils.isAutoInsertSimNumberToProfile()) {
            for (RawContactDelta delta : aState) {
                ArrayList<ValuesDelta> mimeEntries;
                int findIndex;
                if (hasNoPrimaryEntry("vnd.android.cursor.item/phone_v2", delta)) {
                    mimeEntries = delta.getMimeEntries("vnd.android.cursor.item/phone_v2");
                    findIndex = getPhoneIndexByType(mimeEntries, 2, false);
                    if (findIndex == -1) {
                        findIndex = getPhoneIndexByType(mimeEntries, 3, false);
                    }
                    if (findIndex == -1) {
                        findIndex = getPhoneIndexByType(mimeEntries, 1, false);
                    }
                    if (findIndex == -1) {
                        findIndex = getPhoneIndexByType(mimeEntries, 7, true);
                    }
                    setPrimaryData(mimeEntries, findIndex);
                }
                if (hasNoPrimaryEntry("vnd.android.cursor.item/email_v2", delta)) {
                    mimeEntries = delta.getMimeEntries("vnd.android.cursor.item/email_v2");
                    findIndex = getEmailIndexByType(mimeEntries, 1, false);
                    if (findIndex == -1) {
                        findIndex = getEmailIndexByType(mimeEntries, 2, false);
                    }
                    if (findIndex == -1) {
                        findIndex = getEmailIndexByType(mimeEntries, 3, true);
                    }
                    setPrimaryData(mimeEntries, findIndex);
                }
            }
        }
    }

    private boolean hasNoPrimaryEntry(String mimeType, RawContactDelta delta) {
        ArrayList<ValuesDelta> mimeEntries = delta.getMimeEntries(mimeType);
        if (mimeEntries == null) {
            return true;
        }
        for (ValuesDelta entry : mimeEntries) {
            if (entry.isPrimary() && !entry.isDelete()) {
                return false;
            }
        }
        return true;
    }

    private void setPrimaryData(ArrayList<ValuesDelta> aList, int aIndex) {
        if (aList != null && aIndex >= 0) {
            ((ValuesDelta) aList.get(aIndex)).put("is_primary", 1);
            ((ValuesDelta) aList.get(aIndex)).put("is_super_primary", 1);
        }
    }

    private int getPhoneIndexByType(ArrayList<ValuesDelta> aMimeEntries, int aType, boolean aCheckNull) {
        if (aMimeEntries == null) {
            return -1;
        }
        int i = -1;
        for (ValuesDelta entry : aMimeEntries) {
            i++;
            if ((entry.getPhoneType() == aType || aCheckNull) && !TextUtils.isEmpty(entry.getAsString("data1")) && !entry.isDelete()) {
                return i;
            }
        }
        return -1;
    }

    private int getEmailIndexByType(ArrayList<ValuesDelta> aMimeEntries, int aType, boolean aCheckNull) {
        if (aMimeEntries == null) {
            return -1;
        }
        int i = -1;
        for (ValuesDelta entry : aMimeEntries) {
            i++;
            if ((entry.getEmailType() == aType || aCheckNull) && !TextUtils.isEmpty(entry.getAsString("data1")) && !entry.isDelete()) {
                return i;
            }
        }
        return -1;
    }

    private boolean isContactDefaultSimAccount(Context mContext) {
        return "true".equals(System.getString(mContext.getContentResolver(), "contact_default_account"));
    }
}
