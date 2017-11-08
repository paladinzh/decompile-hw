package com.android.contacts.hap.sim;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.model.account.AccountType.DefinitionException;
import com.android.contacts.model.account.AccountType.EditField;
import com.android.contacts.model.account.BaseAccountType;
import com.android.contacts.model.account.BaseAccountType.EmailActionInflater;
import com.android.contacts.model.account.BaseAccountType.PhoneActionAltInflater;
import com.android.contacts.model.account.BaseAccountType.PhoneActionInflater;
import com.android.contacts.model.account.BaseAccountType.SimpleInflater;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.util.HwLog;
import com.google.android.collect.Lists;
import com.google.android.gms.R;

public class SimAccountType extends BaseAccountType {
    private SimConfigListener mListener;
    protected SimConfig mSimConfig;
    private int mSubscription;
    protected boolean mWritable;

    protected SimAccountType(Context context, SimConfig aSimConfig, int aSubscription, String aAccountType) {
        this.accountType = aAccountType;
        this.mSimConfig = aSimConfig;
        this.mSubscription = aSubscription;
    }

    public DataKind addDataKindStructuredName(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/name", R.string.nameLabelsGroup, -1, true));
        kind.actionHeader = new SimpleInflater((int) R.string.nameLabelsGroup);
        kind.actionBody = new SimpleInflater("data1");
        kind.typeOverallMax = 1;
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.full_name, 8289));
        return kind;
    }

    public DataKind addDataKindDisplayName(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("#displayName", R.string.nameLabelsGroup, -1, true));
        kind.actionHeader = new SimpleInflater((int) R.string.nameLabelsGroup);
        kind.actionBody = new SimpleInflater("data1");
        kind.typeOverallMax = 1;
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.full_name, 8289));
        return kind;
    }

    public DataKind addDataKindPhone(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/phone_v2", R.string.phoneLabelsGroup, 10, true));
        kind.iconAltRes = R.drawable.dial_num_2_blk;
        kind.iconAltDescriptionRes = R.string.sms;
        kind.actionHeader = new PhoneActionInflater();
        kind.actionAltHeader = new PhoneActionAltInflater();
        kind.actionBody = new SimpleInflater("data1");
        kind.typeColumn = "data2";
        if (HwLog.HWDBG) {
            HwLog.v("SimAccountType", "ANR Enabled : " + this.mSimConfig.isANREnabled());
        }
        if (this.mSimConfig.isANREnabled()) {
            kind.typeOverallMax = 2;
        } else {
            kind.typeOverallMax = 1;
        }
        kind.typeColumn = "data2";
        kind.typeList = Lists.newArrayList();
        kind.typeList.add(BaseAccountType.buildPhoneType(2));
        kind.typeList.add(BaseAccountType.buildPhoneType(1));
        kind.typeList.add(BaseAccountType.buildPhoneType(3));
        kind.typeList.add(BaseAccountType.buildPhoneType(4).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(5).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(6).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(7));
        kind.typeList.add(BaseAccountType.buildPhoneType(0).setSecondary(true).setCustomColumn("data3"));
        kind.typeList.add(BaseAccountType.buildPhoneType(8).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(9).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(10).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(11).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(12).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(13).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(14).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(15).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(16).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(17).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(18).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(19).setSecondary(true));
        kind.typeList.add(BaseAccountType.buildPhoneType(20).setSecondary(true));
        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField("data1", R.string.phoneLabelsGroup, 3));
        return kind;
    }

    public DataKind addDataKindEmail(Context context) throws DefinitionException {
        DataKind kind = addKind(new DataKind("vnd.android.cursor.item/email_v2", R.string.emailLabelsGroup, 15, true));
        kind.actionHeader = new EmailActionInflater();
        kind.actionBody = new SimpleInflater("data1");
        if (HwLog.HWDBG) {
            HwLog.v("SimAccountType", "Email Enabled : " + this.mSimConfig.isEmailEnabled());
        }
        if (this.mSimConfig.isEmailEnabled()) {
            kind.typeOverallMax = 1;
        } else {
            kind.typeOverallMax = 0;
        }
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

    public boolean areContactsWritable() {
        return QueryUtil.isHAPProviderInstalled() ? this.mWritable : false;
    }

    public void forceWritable(boolean aWritable) {
        if (HwLog.HWDBG) {
            HwLog.v("SimAccountType", "forceWritable : " + aWritable);
        }
        if (aWritable != this.mWritable) {
            this.mWritable = aWritable;
            if (this.mListener != null) {
                this.mListener.configChanged();
            }
        }
    }

    public void setConfigChangeListener(SimConfigListener aListener) {
        this.mListener = aListener;
    }

    public boolean isProfileEditable() {
        return false;
    }

    public CharSequence getDisplayLabel(Context context) {
        return SimFactoryManager.getSimCardDisplayLabel(this.mSubscription);
    }

    public Drawable getDisplayIcon(Context context) {
        return SimFactoryManager.getSimAccountDisplayIcon(this.mSubscription);
    }
}
