package com.android.i18n.phonenumbers;

import java.util.Set;

@Deprecated
public class ShortNumberUtil {

    public enum ShortNumberCost {
        TOLL_FREE,
        STANDARD_RATE,
        PREMIUM_RATE,
        UNKNOWN_COST
    }

    public Set<String> getSupportedRegions() {
        return ShortNumberInfo.getInstance().getSupportedRegions();
    }

    public boolean connectsToEmergencyNumber(String number, String regionCode) {
        return ShortNumberInfo.getInstance().connectsToEmergencyNumber(number, regionCode);
    }

    public boolean isEmergencyNumber(String number, String regionCode) {
        return ShortNumberInfo.getInstance().isEmergencyNumber(number, regionCode);
    }
}
