package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadata;
import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadataCollection;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class MetadataManager {
    private static final String ALTERNATE_FORMATS_FILE_PREFIX = "/com/android/i18n/phonenumbers/data/PhoneNumberAlternateFormatsProto";
    private static final Logger LOGGER = Logger.getLogger(MetadataManager.class.getName());
    private static final String SHORT_NUMBER_METADATA_FILE_PREFIX = "/com/android/i18n/phonenumbers/data/ShortNumberMetadataProto";
    private static final Map<Integer, PhoneMetadata> callingCodeToAlternateFormatsMap = Collections.synchronizedMap(new HashMap());
    private static final Set<Integer> countryCodeSet = AlternateFormatsCountryCodeSet.getCountryCodeSet();
    private static final Set<String> regionCodeSet = ShortNumbersRegionCodeSet.getRegionCodeSet();
    private static final Map<String, PhoneMetadata> regionCodeToShortNumberMetadataMap = Collections.synchronizedMap(new HashMap());

    private MetadataManager() {
    }

    private static void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.toString());
            }
        }
    }

    private static void loadAlternateFormatsMetadataFromFile(int countryCallingCode) {
        IOException e;
        Throwable th;
        InputStream inputStream = null;
        try {
            InputStream in = new ObjectInputStream(PhoneNumberMatcher.class.getResourceAsStream("/com/android/i18n/phonenumbers/data/PhoneNumberAlternateFormatsProto_" + countryCallingCode));
            try {
                PhoneMetadataCollection alternateFormats = new PhoneMetadataCollection();
                alternateFormats.readExternal(in);
                for (PhoneMetadata metadata : alternateFormats.getMetadataList()) {
                    callingCodeToAlternateFormatsMap.put(Integer.valueOf(metadata.getCountryCode()), metadata);
                }
                close(in);
            } catch (IOException e2) {
                e = e2;
                inputStream = in;
                try {
                    LOGGER.log(Level.WARNING, e.toString());
                    close(inputStream);
                } catch (Throwable th2) {
                    th = th2;
                    close(inputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = in;
                close(inputStream);
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            LOGGER.log(Level.WARNING, e.toString());
            close(inputStream);
        }
    }

    static PhoneMetadata getAlternateFormatsForCountry(int countryCallingCode) {
        if (!countryCodeSet.contains(Integer.valueOf(countryCallingCode))) {
            return null;
        }
        synchronized (callingCodeToAlternateFormatsMap) {
            if (!callingCodeToAlternateFormatsMap.containsKey(Integer.valueOf(countryCallingCode))) {
                loadAlternateFormatsMetadataFromFile(countryCallingCode);
            }
        }
        return (PhoneMetadata) callingCodeToAlternateFormatsMap.get(Integer.valueOf(countryCallingCode));
    }

    private static void loadShortNumberMetadataFromFile(String regionCode) {
        IOException e;
        Throwable th;
        InputStream inputStream = null;
        try {
            InputStream in = new ObjectInputStream(PhoneNumberMatcher.class.getResourceAsStream("/com/android/i18n/phonenumbers/data/ShortNumberMetadataProto_" + regionCode));
            try {
                PhoneMetadataCollection shortNumberMetadata = new PhoneMetadataCollection();
                shortNumberMetadata.readExternal(in);
                for (PhoneMetadata metadata : shortNumberMetadata.getMetadataList()) {
                    regionCodeToShortNumberMetadataMap.put(regionCode, metadata);
                }
                close(in);
            } catch (IOException e2) {
                e = e2;
                inputStream = in;
                try {
                    LOGGER.log(Level.WARNING, e.toString());
                    close(inputStream);
                } catch (Throwable th2) {
                    th = th2;
                    close(inputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = in;
                close(inputStream);
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            LOGGER.log(Level.WARNING, e.toString());
            close(inputStream);
        }
    }

    static Set<String> getShortNumberMetadataSupportedRegions() {
        return regionCodeSet;
    }

    static PhoneMetadata getShortNumberMetadataForRegion(String regionCode) {
        if (!regionCodeSet.contains(regionCode)) {
            return null;
        }
        synchronized (regionCodeToShortNumberMetadataMap) {
            if (!regionCodeToShortNumberMetadataMap.containsKey(regionCode)) {
                loadShortNumberMetadataFromFile(regionCode);
            }
        }
        return (PhoneMetadata) regionCodeToShortNumberMetadataMap.get(regionCode);
    }
}
