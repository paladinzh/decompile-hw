package android.icu.impl;

import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.util.MissingResourceException;

public class CalendarUtil {
    private static final String CALKEY = "calendar";
    private static ICUCache<String, String> CALTYPE_CACHE = new SimpleCache();
    private static final String DEFCAL = "gregorian";

    public static String getCalendarType(ULocale loc) {
        String calType = loc.getKeywordValue(CALKEY);
        if (calType != null) {
            return calType;
        }
        String baseLoc = loc.getBaseName();
        calType = (String) CALTYPE_CACHE.get(baseLoc);
        if (calType != null) {
            return calType;
        }
        ULocale canonical = ULocale.createCanonical(loc.toString());
        calType = canonical.getKeywordValue(CALKEY);
        if (calType == null) {
            String region = canonical.getCountry();
            if (region.length() == 0) {
                region = ULocale.addLikelySubtags(canonical).getCountry();
            }
            try {
                UResourceBundle order;
                UResourceBundle calPref = UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("calendarPreferenceData");
                try {
                    order = calPref.get(region);
                } catch (MissingResourceException e) {
                    order = calPref.get("001");
                }
                calType = order.getString(0);
            } catch (MissingResourceException e2) {
            }
            if (calType == null) {
                calType = DEFCAL;
            }
        }
        CALTYPE_CACHE.put(baseLoc, calType);
        return calType;
    }
}
