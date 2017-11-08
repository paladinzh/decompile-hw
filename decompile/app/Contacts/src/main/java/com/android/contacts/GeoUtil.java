package com.android.contacts;

import android.content.Context;
import android.text.TextUtils;
import com.android.contacts.compatibility.CountryMonitor;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
import java.util.Locale;

public class GeoUtil {
    public static String getCurrentCountryIso(Context context) {
        return CountryMonitor.getInstance(context).getCountryIso();
    }

    public static String getGeocodedLocationFor(Context context, String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber) || context == null) {
            return null;
        }
        PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder.getInstance();
        try {
            PhoneNumber structuredPhoneNumber = PhoneNumberUtil.getInstance().parse(phoneNumber, getCurrentCountryIso(context));
            Locale locale = context.getResources().getConfiguration().locale;
            if (locale == null) {
                return null;
            }
            return geocoder.getDescriptionForNumber(structuredPhoneNumber, locale);
        } catch (NumberParseException e) {
            return null;
        }
    }
}
