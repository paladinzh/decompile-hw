package com.android.rcs;

import com.android.mms.MmsConfig;

public class RcsMmsConfig {
    public static boolean getEnablePeopleActionBarMultiLine() {
        return MmsConfig.getMmsBoolConfig("enablePeopleActionBarMultiLine", false);
    }

    public static boolean getConfigRoamingNationalAsLocal() {
        return MmsConfig.getMmsBoolConfig("configRoamingNationalAsLocal", false);
    }

    public static boolean getEnableCotaFeature() {
        return MmsConfig.getMmsBoolConfig("enableCotaFeatrue", false);
    }

    public static boolean getSaveMmsEmailAdress() {
        return MmsConfig.getMmsBoolConfig("enableSaveMmsEmailAdress", false);
    }
}
