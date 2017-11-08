package com.huawei.numberlocation;

import android.content.Context;
import java.io.File;

public class FixedPhoneNumber implements PhoneNumber {
    private String LOCATION_DATA_DAT;
    protected String areaCodeString = NULL;
    private String mFixedPhoneNumber = NULL;
    private String resultString = NULL;
    private String top2String = NULL;

    public FixedPhoneNumber(Context ctx, String numberString) {
        this.mFixedPhoneNumber = numberString;
        this.LOCATION_DATA_DAT = ctx.createDeviceProtectedStorageContext().getFilesDir() + File.separator + "numberlocation.dat";
    }

    public boolean parseFixedPhoneNumber() {
        this.areaCodeString = NULL;
        if (this.mFixedPhoneNumber == null) {
            LogExt.d("SecurityGuard/NumberLocation", "fixedPhoneNumber is NULL");
            return false;
        } else if (this.mFixedPhoneNumber.length() < 5) {
            return false;
        } else {
            this.top2String = this.mFixedPhoneNumber.substring(0, 2);
            if (this.top2String.equals("01") || this.top2String.equals("02")) {
                this.areaCodeString = this.mFixedPhoneNumber.substring(0, 3);
                LogExt.d("SecurityGuard/NumberLocation", "Parse fixedPhoneNumber success(01,02)");
                return true;
            }
            this.areaCodeString = this.mFixedPhoneNumber.substring(0, 4);
            LogExt.d("SecurityGuard/NumberLocation", "Parse fixedPhoneNumber success(else)");
            return true;
        }
    }

    public String getParseResult() {
        this.resultString = NULL;
        if (parseFixedPhoneNumber()) {
            this.resultString = NumberLocationDb.queryUnicodeInformationByTelNum(this.areaCodeString, this.LOCATION_DATA_DAT);
            return this.resultString;
        }
        LogExt.d("SecurityGuard/NumberLocation", "ParseFixedPhoneNumber() failed");
        return this.resultString;
    }
}
