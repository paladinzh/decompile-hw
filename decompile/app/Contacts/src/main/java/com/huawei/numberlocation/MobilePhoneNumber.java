package com.huawei.numberlocation;

import android.content.Context;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MobilePhoneNumber implements PhoneNumber {
    private static final List<String> VirtualPrefixList = Arrays.asList(new String[]{"170", "171"});
    public String LOCATION_DATA_DAT;
    private Context mContext;
    private String mLocation = NULL;
    private String mMobilePhoneNumber = NULL;
    private String mPhoneOperator = NULL;
    private String mResultString = NULL;
    private String ndc3String = NULL;
    private String ndc7String = NULL;

    public MobilePhoneNumber(Context ctx, String numberString) {
        this.mContext = ctx;
        this.mMobilePhoneNumber = numberString;
        this.LOCATION_DATA_DAT = ctx.createDeviceProtectedStorageContext().getFilesDir() + File.separator + "numberlocation.dat";
    }

    public boolean parseMobilePhoneNumber() {
        this.ndc3String = NULL;
        this.ndc7String = NULL;
        if (this.mMobilePhoneNumber == null) {
            LogExt.d("SecurityGuard/NumberLocation", "mobilePhoneNumber is NULL");
            return false;
        }
        if (this.mMobilePhoneNumber.matches("^\\+86\\d{3,11}$")) {
            this.ndc3String = this.mMobilePhoneNumber.substring(3, 6);
            if (10 <= this.mMobilePhoneNumber.length()) {
                this.ndc7String = this.mMobilePhoneNumber.substring(3, 10);
                return true;
            }
        } else if (this.mMobilePhoneNumber.matches("^86\\d{3,11}$")) {
            this.ndc3String = this.mMobilePhoneNumber.substring(2, 5);
            if (9 <= this.mMobilePhoneNumber.length()) {
                this.ndc7String = this.mMobilePhoneNumber.substring(2, 9);
                return true;
            }
        } else if (this.mMobilePhoneNumber.matches("^(1\\d{2,10})$")) {
            this.ndc3String = this.mMobilePhoneNumber.substring(0, 3);
            if (7 <= this.mMobilePhoneNumber.length()) {
                this.ndc7String = this.mMobilePhoneNumber.substring(0, 7);
                LogExt.d("SecurityGuard/NumberLocation", "Parse MobilePhoneNumber success");
                return true;
            }
        } else if (this.mMobilePhoneNumber.matches("^0086\\d{3,11}$")) {
            this.ndc3String = this.mMobilePhoneNumber.substring(4, 7);
            if (11 <= this.mMobilePhoneNumber.length()) {
                this.ndc7String = this.mMobilePhoneNumber.substring(4, 11);
                return true;
            }
        }
        return false;
    }

    public String getSP() {
        return this.ndc3String;
    }

    public String getParseResult() {
        this.mResultString = NULL;
        if (parseMobilePhoneNumber()) {
            if (this.ndc7String != null) {
                this.mResultString = NumberLocationDb.queryUnicodeInformationByPhoneNum(this.ndc7String, this.LOCATION_DATA_DAT);
            }
            if ("null".equals(this.mResultString) || "".equals(this.mResultString)) {
                this.mResultString = null;
            }
            this.mLocation = this.mResultString;
            boolean isVirtual = VirtualPrefixList.contains(getSP());
            if (isVirtual && this.ndc7String != null) {
                this.mPhoneOperator = NumberLocationDb.queryUnicodeOPNamebyPhoneNumber(this.ndc7String, this.LOCATION_DATA_DAT);
            } else if (!(isVirtual || this.ndc3String == null)) {
                this.mPhoneOperator = NumberLocationDb.queryUnicodeOPNamebyPhoneNumber(this.ndc3String, this.LOCATION_DATA_DAT);
            }
            if (this.mPhoneOperator == null) {
                this.mPhoneOperator = new NumberData(this.mContext).getMobilePhoneSP_CN(getSP());
            }
            if (this.mPhoneOperator != null && this.mResultString != null) {
                this.mResultString += HwCustPreloadContacts.EMPTY_STRING + this.mPhoneOperator;
            } else if (this.mPhoneOperator != null && this.mResultString == null) {
                this.mResultString = this.mPhoneOperator;
            }
            return this.mResultString;
        }
        LogExt.d("SecurityGuard/NumberLocation", "ParseMobilePhoneNumber() failed");
        return this.mResultString;
    }

    protected String getPhoneOperator() {
        boolean isVirtual = VirtualPrefixList.contains(getSP());
        if (isVirtual && this.ndc7String != null) {
            this.mPhoneOperator = NumberLocationDb.queryUnicodeOPNamebyPhoneNumber(this.ndc7String, this.LOCATION_DATA_DAT);
        } else if (!(isVirtual || this.ndc3String == null)) {
            this.mPhoneOperator = NumberLocationDb.queryUnicodeOPNamebyPhoneNumber(this.ndc3String, this.LOCATION_DATA_DAT);
        }
        if (this.mPhoneOperator == null) {
            this.mPhoneOperator = new NumberData(this.mContext).getMobilePhoneSP_CN(getSP());
        }
        return this.mPhoneOperator;
    }

    protected String getLocation() {
        return this.mLocation;
    }
}
