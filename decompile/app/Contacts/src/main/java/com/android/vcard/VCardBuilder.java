package com.android.vcard;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.SystemProperties;
import android.provider.ContactsContract.Groups;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.vcard.VCardUtils.PhoneNumberUtilsPort;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.google.android.gms.location.places.Place;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VCardBuilder {
    private static final Set<String> sAllowedAndroidPropertySet = Collections.unmodifiableSet(new HashSet(Arrays.asList(new String[]{"vnd.android.cursor.item/nickname", "vnd.android.cursor.item/contact_event", "vnd.android.cursor.item/relation"})));
    private static final Map<Integer, Integer> sPostalTypePriorityMap = new HashMap();
    private final boolean mAppendTypeParamName;
    private StringBuilder mBuilder;
    private final String mCharset;
    private boolean mEndAppended;
    private final boolean mIsDoCoMo;
    private final boolean mIsJapaneseMobilePhone;
    private final boolean mIsV30OrV40;
    private final boolean mNeedsToConvertPhoneticString;
    private final boolean mOnlyOneNoteFieldIsAvailable;
    private final boolean mRefrainsQPToNameProperties;
    private final boolean mShouldAppendCharsetParam;
    private final boolean mShouldUseQuotedPrintable;
    private final boolean mUsesAndroidProperty;
    private final boolean mUsesDefactProperty;
    private final String mVCardCharsetParameter;
    private final int mVCardType;

    private static class PostalStruct {
        final String addressData;
        final boolean appendCharset;
        final boolean reallyUseQuotedPrintable;

        public PostalStruct(boolean reallyUseQuotedPrintable, boolean appendCharset, String addressData) {
            this.reallyUseQuotedPrintable = reallyUseQuotedPrintable;
            this.appendCharset = appendCharset;
            this.addressData = addressData;
        }
    }

    static {
        sPostalTypePriorityMap.put(Integer.valueOf(1), Integer.valueOf(0));
        sPostalTypePriorityMap.put(Integer.valueOf(2), Integer.valueOf(1));
        sPostalTypePriorityMap.put(Integer.valueOf(3), Integer.valueOf(2));
        sPostalTypePriorityMap.put(Integer.valueOf(0), Integer.valueOf(3));
    }

    public VCardBuilder(int vcardType, String charset) {
        boolean z;
        boolean z2 = true;
        this.mVCardType = vcardType;
        if (VCardConfig.isVersion40(vcardType)) {
            Log.w("vCard", "Should not use vCard 4.0 when building vCard. It is not officially published yet.");
        }
        if (VCardConfig.isVersion30(vcardType)) {
            z = true;
        } else {
            z = VCardConfig.isVersion40(vcardType);
        }
        this.mIsV30OrV40 = z;
        this.mShouldUseQuotedPrintable = VCardConfig.shouldUseQuotedPrintable(vcardType);
        this.mIsDoCoMo = VCardConfig.isDoCoMo(vcardType);
        this.mIsJapaneseMobilePhone = VCardConfig.needsToConvertPhoneticString(vcardType);
        this.mOnlyOneNoteFieldIsAvailable = VCardConfig.onlyOneNoteFieldIsAvailable(vcardType);
        this.mUsesAndroidProperty = VCardConfig.usesAndroidSpecificProperty(vcardType);
        this.mUsesDefactProperty = VCardConfig.usesDefactProperty(vcardType);
        this.mRefrainsQPToNameProperties = VCardConfig.shouldRefrainQPToNameProperties(vcardType);
        this.mAppendTypeParamName = VCardConfig.appendTypeParamName(vcardType);
        this.mNeedsToConvertPhoneticString = VCardConfig.needsToConvertPhoneticString(vcardType);
        if (VCardConfig.isVersion30(vcardType) && "UTF-8".equalsIgnoreCase(charset)) {
            z2 = false;
        }
        this.mShouldAppendCharsetParam = z2;
        if (VCardConfig.isDoCoMo(vcardType)) {
            if ("SHIFT_JIS".equalsIgnoreCase(charset)) {
                this.mCharset = charset;
            } else if (TextUtils.isEmpty(charset)) {
                this.mCharset = "SHIFT_JIS";
            } else {
                this.mCharset = charset;
            }
            this.mVCardCharsetParameter = "CHARSET=SHIFT_JIS";
        } else if (TextUtils.isEmpty(charset)) {
            Log.i("vCard", "Use the charset \"UTF-8\" for export.");
            this.mCharset = "UTF-8";
            this.mVCardCharsetParameter = "CHARSET=UTF-8";
        } else {
            this.mCharset = charset;
            this.mVCardCharsetParameter = "CHARSET=" + charset;
        }
        clear();
    }

    public void clear() {
        this.mBuilder = new StringBuilder();
        this.mEndAppended = false;
        appendLine("BEGIN", "VCARD");
        if (VCardConfig.isVersion40(this.mVCardType)) {
            appendLine("VERSION", "4.0");
        } else if (VCardConfig.isVersion30(this.mVCardType)) {
            appendLine("VERSION", "3.0");
        } else {
            if (!VCardConfig.isVersion21(this.mVCardType)) {
                Log.w("vCard", "Unknown vCard version detected.");
            }
            appendLine("VERSION", "2.1");
        }
    }

    private boolean containsNonEmptyName(ContentValues contentValues) {
        return (TextUtils.isEmpty(contentValues.getAsString("data3")) && TextUtils.isEmpty(contentValues.getAsString("data5")) && TextUtils.isEmpty(contentValues.getAsString("data2")) && TextUtils.isEmpty(contentValues.getAsString("data4")) && TextUtils.isEmpty(contentValues.getAsString("data6")) && TextUtils.isEmpty(contentValues.getAsString("data9")) && TextUtils.isEmpty(contentValues.getAsString("data8")) && TextUtils.isEmpty(contentValues.getAsString("data7")) && TextUtils.isEmpty(contentValues.getAsString("data1"))) ? false : true;
    }

    private ContentValues getPrimaryContentValueWithStructuredName(List<ContentValues> contentValuesList) {
        ContentValues primaryContentValues = null;
        ContentValues subprimaryContentValues = null;
        for (ContentValues contentValues : contentValuesList) {
            if (contentValues != null) {
                Integer isSuperPrimary = contentValues.getAsInteger("is_super_primary");
                if (isSuperPrimary != null && isSuperPrimary.intValue() > 0) {
                    primaryContentValues = contentValues;
                    break;
                } else if (primaryContentValues == null) {
                    Integer isPrimary = contentValues.getAsInteger("is_primary");
                    if (isPrimary != null && isPrimary.intValue() > 0 && containsNonEmptyName(contentValues)) {
                        primaryContentValues = contentValues;
                    } else if (subprimaryContentValues == null && containsNonEmptyName(contentValues)) {
                        subprimaryContentValues = contentValues;
                    }
                }
            }
        }
        if (primaryContentValues != null) {
            return primaryContentValues;
        }
        if (subprimaryContentValues != null) {
            return subprimaryContentValues;
        }
        return new ContentValues();
    }

    private VCardBuilder appendNamePropertiesV40(List<ContentValues> contentValuesList) {
        if (this.mIsDoCoMo || this.mNeedsToConvertPhoneticString) {
            Log.w("vCard", "Invalid flag is used in vCard 4.0 construction. Ignored.");
        }
        if (contentValuesList == null || contentValuesList.isEmpty()) {
            appendLine("FN", "");
            return this;
        }
        boolean isEmpty;
        ContentValues contentValues = getPrimaryContentValueWithStructuredName(contentValuesList);
        String familyName = contentValues.getAsString("data3");
        String middleName = contentValues.getAsString("data5");
        String givenName = contentValues.getAsString("data2");
        String prefix = contentValues.getAsString("data4");
        String suffix = contentValues.getAsString("data6");
        String formattedName = contentValues.getAsString("data1");
        if (TextUtils.isEmpty(familyName) && TextUtils.isEmpty(givenName) && TextUtils.isEmpty(middleName) && TextUtils.isEmpty(prefix) && TextUtils.isEmpty(suffix)) {
            if (TextUtils.isEmpty(formattedName)) {
                appendLine("FN", "");
                return this;
            }
            familyName = formattedName;
        }
        String phoneticFamilyName = contentValues.getAsString("data9");
        String phoneticMiddleName = contentValues.getAsString("data8");
        String phoneticGivenName = contentValues.getAsString("data7");
        String escapedFamily = escapeCharacters(familyName);
        String escapedGiven = escapeCharacters(givenName);
        String escapedMiddle = escapeCharacters(middleName);
        String escapedPrefix = escapeCharacters(prefix);
        String escapedSuffix = escapeCharacters(suffix);
        this.mBuilder.append("N");
        if (TextUtils.isEmpty(phoneticFamilyName) && TextUtils.isEmpty(phoneticMiddleName)) {
            isEmpty = TextUtils.isEmpty(phoneticGivenName);
        } else {
            isEmpty = false;
        }
        if (!isEmpty) {
            this.mBuilder.append(";");
            this.mBuilder.append("SORT-AS=").append(VCardUtils.toStringAsV40ParamValue(escapeCharacters(phoneticFamilyName) + ';' + escapeCharacters(phoneticGivenName) + ';' + escapeCharacters(phoneticMiddleName)));
        }
        this.mBuilder.append(":");
        this.mBuilder.append(escapedFamily);
        this.mBuilder.append(";");
        this.mBuilder.append(escapedGiven);
        this.mBuilder.append(";");
        this.mBuilder.append(escapedMiddle);
        this.mBuilder.append(";");
        this.mBuilder.append(escapedPrefix);
        this.mBuilder.append(";");
        this.mBuilder.append(escapedSuffix);
        this.mBuilder.append("\r\n");
        if (TextUtils.isEmpty(formattedName)) {
            Log.w("vCard", "DISPLAY_NAME is empty.");
            appendLine("FN", escapeCharacters(VCardUtils.constructNameFromElements(VCardConfig.getNameOrderType(this.mVCardType), familyName, middleName, givenName, prefix, suffix)));
        } else {
            String escapedFormatted = escapeCharacters(formattedName);
            this.mBuilder.append("FN");
            this.mBuilder.append(":");
            this.mBuilder.append(escapedFormatted);
            this.mBuilder.append("\r\n");
        }
        appendPhoneticNameFields(contentValues);
        return this;
    }

    public VCardBuilder appendNameProperties(List<ContentValues> contentValuesList) {
        if (VCardConfig.isVersion40(this.mVCardType)) {
            return appendNamePropertiesV40(contentValuesList);
        }
        if (contentValuesList == null || contentValuesList.isEmpty()) {
            if (VCardConfig.isVersion30(this.mVCardType)) {
                appendLine("N", "");
                appendLine("FN", "");
            } else if (this.mIsDoCoMo) {
                appendLine("N", "");
            }
            return this;
        }
        ContentValues contentValues = getPrimaryContentValueWithStructuredName(contentValuesList);
        String familyName = contentValues.getAsString("data3");
        String middleName = contentValues.getAsString("data5");
        String givenName = contentValues.getAsString("data2");
        String prefix = contentValues.getAsString("data4");
        String suffix = contentValues.getAsString("data6");
        String displayName = contentValues.getAsString("data1");
        if (!TextUtils.isEmpty(familyName) || !TextUtils.isEmpty(givenName)) {
            boolean reallyUseQuotedPrintableToName;
            String formattedName;
            boolean reallyUseQuotedPrintableToFN;
            String encodedFamily;
            String encodedGiven;
            String encodedMiddle;
            String encodedPrefix;
            String encodedSuffix;
            boolean reallyAppendCharsetParameterToName = shouldAppendCharsetParam(familyName, givenName, middleName, prefix, suffix);
            if (this.mRefrainsQPToNameProperties) {
                reallyUseQuotedPrintableToName = false;
            } else {
                if (VCardUtils.containsOnlyNonCrLfPrintableAscii(familyName)) {
                    if (VCardUtils.containsOnlyNonCrLfPrintableAscii(givenName)) {
                        if (VCardUtils.containsOnlyNonCrLfPrintableAscii(middleName)) {
                            if (VCardUtils.containsOnlyNonCrLfPrintableAscii(prefix)) {
                                if (VCardUtils.containsOnlyNonCrLfPrintableAscii(suffix)) {
                                    reallyUseQuotedPrintableToName = false;
                                }
                            }
                        }
                    }
                }
                reallyUseQuotedPrintableToName = true;
            }
            if (TextUtils.isEmpty(displayName)) {
                formattedName = VCardUtils.constructNameFromElements(VCardConfig.getNameOrderType(this.mVCardType), familyName, middleName, givenName, prefix, suffix);
            } else {
                formattedName = displayName;
            }
            boolean reallyAppendCharsetParameterToFN = shouldAppendCharsetParam(formattedName);
            if (this.mRefrainsQPToNameProperties) {
                reallyUseQuotedPrintableToFN = false;
            } else {
                reallyUseQuotedPrintableToFN = !VCardUtils.containsOnlyNonCrLfPrintableAscii(formattedName);
            }
            if (reallyUseQuotedPrintableToName) {
                encodedFamily = encodeQuotedPrintable(familyName);
                encodedGiven = encodeQuotedPrintable(givenName);
                encodedMiddle = encodeQuotedPrintable(middleName);
                encodedPrefix = encodeQuotedPrintable(prefix);
                encodedSuffix = encodeQuotedPrintable(suffix);
            } else {
                encodedFamily = escapeCharacters(familyName);
                encodedGiven = escapeCharacters(givenName);
                encodedMiddle = escapeCharacters(middleName);
                encodedPrefix = escapeCharacters(prefix);
                encodedSuffix = escapeCharacters(suffix);
            }
            String encodedFormattedname = reallyUseQuotedPrintableToFN ? encodeQuotedPrintable(formattedName) : escapeCharacters(formattedName);
            this.mBuilder.append("N");
            if (this.mIsDoCoMo) {
                if (reallyAppendCharsetParameterToName) {
                    this.mBuilder.append(";");
                    this.mBuilder.append(this.mVCardCharsetParameter);
                }
                if (reallyUseQuotedPrintableToName) {
                    this.mBuilder.append(";");
                    this.mBuilder.append("ENCODING=QUOTED-PRINTABLE");
                }
                this.mBuilder.append(":");
                this.mBuilder.append(formattedName);
                this.mBuilder.append(";");
                this.mBuilder.append(";");
                this.mBuilder.append(";");
                this.mBuilder.append(";");
            } else {
                if (reallyAppendCharsetParameterToName) {
                    this.mBuilder.append(";");
                    this.mBuilder.append(this.mVCardCharsetParameter);
                }
                if (reallyUseQuotedPrintableToName) {
                    this.mBuilder.append(";");
                    this.mBuilder.append("ENCODING=QUOTED-PRINTABLE");
                }
                this.mBuilder.append(":");
                this.mBuilder.append(encodedFamily);
                this.mBuilder.append(";");
                String localeRegion = SystemProperties.get("ro.product.locale.region", "");
                if (localeRegion.equalsIgnoreCase("CN")) {
                    Log.d("VCardBuilder", "encodedMiddle = ");
                    this.mBuilder.append(encodedMiddle);
                }
                this.mBuilder.append(encodedGiven);
                this.mBuilder.append(";");
                if (!localeRegion.equalsIgnoreCase("CN")) {
                    this.mBuilder.append(encodedMiddle);
                }
                this.mBuilder.append(";");
                this.mBuilder.append(encodedPrefix);
                this.mBuilder.append(";");
                this.mBuilder.append(encodedSuffix);
            }
            this.mBuilder.append("\r\n");
            this.mBuilder.append("FN");
            if (reallyAppendCharsetParameterToFN) {
                this.mBuilder.append(";");
                this.mBuilder.append(this.mVCardCharsetParameter);
            }
            if (reallyUseQuotedPrintableToFN) {
                this.mBuilder.append(";");
                this.mBuilder.append("ENCODING=QUOTED-PRINTABLE");
            }
            this.mBuilder.append(":");
            this.mBuilder.append(encodedFormattedname);
            this.mBuilder.append("\r\n");
        } else if (!TextUtils.isEmpty(displayName)) {
            buildSinglePartNameField("N", displayName);
            this.mBuilder.append(";");
            this.mBuilder.append(";");
            this.mBuilder.append(";");
            this.mBuilder.append(";");
            this.mBuilder.append("\r\n");
            buildSinglePartNameField("FN", displayName);
            this.mBuilder.append("\r\n");
        } else if (VCardConfig.isVersion30(this.mVCardType)) {
            appendLine("N", "");
            appendLine("FN", "");
        } else if (this.mIsDoCoMo) {
            appendLine("N", "");
        }
        appendPhoneticNameFields(contentValues);
        return this;
    }

    private void buildSinglePartNameField(String property, String part) {
        boolean reallyUseQuotedPrintable;
        String encodedPart;
        if (this.mRefrainsQPToNameProperties) {
            reallyUseQuotedPrintable = false;
        } else {
            reallyUseQuotedPrintable = !VCardUtils.containsOnlyNonCrLfPrintableAscii(part);
        }
        if (reallyUseQuotedPrintable) {
            encodedPart = encodeQuotedPrintable(part);
        } else {
            encodedPart = escapeCharacters(part);
        }
        this.mBuilder.append(property);
        if (shouldAppendCharsetParam(part)) {
            this.mBuilder.append(";");
            this.mBuilder.append(this.mVCardCharsetParameter);
        }
        if (reallyUseQuotedPrintable) {
            this.mBuilder.append(";");
            this.mBuilder.append("ENCODING=QUOTED-PRINTABLE");
        }
        this.mBuilder.append(":");
        this.mBuilder.append(encodedPart);
    }

    private void appendPhoneticNameFields(ContentValues contentValues) {
        String phoneticFamilyName;
        String phoneticMiddleName;
        String phoneticGivenName;
        String tmpPhoneticFamilyName = contentValues.getAsString("data9");
        String tmpPhoneticMiddleName = contentValues.getAsString("data8");
        String tmpPhoneticGivenName = contentValues.getAsString("data7");
        if (this.mNeedsToConvertPhoneticString) {
            phoneticFamilyName = VCardUtils.toHalfWidthString(tmpPhoneticFamilyName);
            phoneticMiddleName = VCardUtils.toHalfWidthString(tmpPhoneticMiddleName);
            phoneticGivenName = VCardUtils.toHalfWidthString(tmpPhoneticGivenName);
        } else {
            phoneticFamilyName = tmpPhoneticFamilyName;
            phoneticMiddleName = tmpPhoneticMiddleName;
            phoneticGivenName = tmpPhoneticGivenName;
        }
        if (TextUtils.isEmpty(phoneticFamilyName) && TextUtils.isEmpty(phoneticMiddleName) && TextUtils.isEmpty(phoneticGivenName)) {
            if (this.mIsDoCoMo) {
                this.mBuilder.append("SOUND");
                this.mBuilder.append(";");
                this.mBuilder.append("X-IRMC-N");
                this.mBuilder.append(":");
                this.mBuilder.append(";");
                this.mBuilder.append(";");
                this.mBuilder.append(";");
                this.mBuilder.append(";");
                this.mBuilder.append("\r\n");
            }
            return;
        }
        boolean reallyUseQuotedPrintable;
        String encodedPhoneticFamilyName;
        String encodedPhoneticMiddleName;
        String encodedPhoneticGivenName;
        if (!VCardConfig.isVersion40(this.mVCardType)) {
            if (VCardConfig.isVersion30(this.mVCardType)) {
                String sortString = VCardUtils.constructNameFromElements(this.mVCardType, phoneticFamilyName, phoneticMiddleName, phoneticGivenName);
                this.mBuilder.append("SORT-STRING");
                if (VCardConfig.isVersion30(this.mVCardType)) {
                    if (shouldAppendCharsetParam(sortString)) {
                        this.mBuilder.append(";");
                        this.mBuilder.append(this.mVCardCharsetParameter);
                    }
                }
                this.mBuilder.append(":");
                this.mBuilder.append(escapeCharacters(sortString));
                this.mBuilder.append("\r\n");
            } else if (this.mIsJapaneseMobilePhone) {
                this.mBuilder.append("SOUND");
                this.mBuilder.append(";");
                this.mBuilder.append("X-IRMC-N");
                if (this.mRefrainsQPToNameProperties) {
                    reallyUseQuotedPrintable = false;
                } else {
                    if (VCardUtils.containsOnlyNonCrLfPrintableAscii(phoneticFamilyName)) {
                        if (VCardUtils.containsOnlyNonCrLfPrintableAscii(phoneticMiddleName)) {
                            if (VCardUtils.containsOnlyNonCrLfPrintableAscii(phoneticGivenName)) {
                                reallyUseQuotedPrintable = false;
                            }
                        }
                    }
                    reallyUseQuotedPrintable = true;
                }
                if (reallyUseQuotedPrintable) {
                    encodedPhoneticFamilyName = encodeQuotedPrintable(phoneticFamilyName);
                    encodedPhoneticMiddleName = encodeQuotedPrintable(phoneticMiddleName);
                    encodedPhoneticGivenName = encodeQuotedPrintable(phoneticGivenName);
                } else {
                    encodedPhoneticFamilyName = escapeCharacters(phoneticFamilyName);
                    encodedPhoneticMiddleName = escapeCharacters(phoneticMiddleName);
                    encodedPhoneticGivenName = escapeCharacters(phoneticGivenName);
                }
                if (shouldAppendCharsetParam(encodedPhoneticFamilyName, encodedPhoneticMiddleName, encodedPhoneticGivenName)) {
                    this.mBuilder.append(";");
                    this.mBuilder.append(this.mVCardCharsetParameter);
                }
                this.mBuilder.append(":");
                boolean first = true;
                if (!TextUtils.isEmpty(encodedPhoneticFamilyName)) {
                    this.mBuilder.append(encodedPhoneticFamilyName);
                    first = false;
                }
                if (!TextUtils.isEmpty(encodedPhoneticMiddleName)) {
                    if (first) {
                        first = false;
                    } else {
                        this.mBuilder.append(' ');
                    }
                    this.mBuilder.append(encodedPhoneticMiddleName);
                }
                if (!TextUtils.isEmpty(encodedPhoneticGivenName)) {
                    if (!first) {
                        this.mBuilder.append(' ');
                    }
                    this.mBuilder.append(encodedPhoneticGivenName);
                }
                this.mBuilder.append(";");
                this.mBuilder.append(";");
                this.mBuilder.append(";");
                this.mBuilder.append(";");
                this.mBuilder.append("\r\n");
            }
        }
        if (this.mUsesDefactProperty) {
            if (!TextUtils.isEmpty(phoneticGivenName)) {
                if (this.mShouldUseQuotedPrintable) {
                    reallyUseQuotedPrintable = !VCardUtils.containsOnlyNonCrLfPrintableAscii(phoneticGivenName);
                } else {
                    reallyUseQuotedPrintable = false;
                }
                if (reallyUseQuotedPrintable) {
                    encodedPhoneticGivenName = encodeQuotedPrintable(phoneticGivenName);
                } else {
                    encodedPhoneticGivenName = escapeCharacters(phoneticGivenName);
                }
                this.mBuilder.append("X-PHONETIC-FIRST-NAME");
                if (shouldAppendCharsetParam(phoneticGivenName)) {
                    this.mBuilder.append(";");
                    this.mBuilder.append(this.mVCardCharsetParameter);
                }
                if (reallyUseQuotedPrintable) {
                    this.mBuilder.append(";");
                    this.mBuilder.append("ENCODING=QUOTED-PRINTABLE");
                }
                this.mBuilder.append(":");
                this.mBuilder.append(encodedPhoneticGivenName);
                this.mBuilder.append("\r\n");
            }
            if (!TextUtils.isEmpty(phoneticMiddleName)) {
                if (this.mShouldUseQuotedPrintable) {
                    reallyUseQuotedPrintable = !VCardUtils.containsOnlyNonCrLfPrintableAscii(phoneticMiddleName);
                } else {
                    reallyUseQuotedPrintable = false;
                }
                if (reallyUseQuotedPrintable) {
                    encodedPhoneticMiddleName = encodeQuotedPrintable(phoneticMiddleName);
                } else {
                    encodedPhoneticMiddleName = escapeCharacters(phoneticMiddleName);
                }
                this.mBuilder.append("X-PHONETIC-MIDDLE-NAME");
                if (shouldAppendCharsetParam(phoneticMiddleName)) {
                    this.mBuilder.append(";");
                    this.mBuilder.append(this.mVCardCharsetParameter);
                }
                if (reallyUseQuotedPrintable) {
                    this.mBuilder.append(";");
                    this.mBuilder.append("ENCODING=QUOTED-PRINTABLE");
                }
                this.mBuilder.append(":");
                this.mBuilder.append(encodedPhoneticMiddleName);
                this.mBuilder.append("\r\n");
            }
            if (!TextUtils.isEmpty(phoneticFamilyName)) {
                if (this.mShouldUseQuotedPrintable) {
                    reallyUseQuotedPrintable = !VCardUtils.containsOnlyNonCrLfPrintableAscii(phoneticFamilyName);
                } else {
                    reallyUseQuotedPrintable = false;
                }
                if (reallyUseQuotedPrintable) {
                    encodedPhoneticFamilyName = encodeQuotedPrintable(phoneticFamilyName);
                } else {
                    encodedPhoneticFamilyName = escapeCharacters(phoneticFamilyName);
                }
                this.mBuilder.append("X-PHONETIC-LAST-NAME");
                if (shouldAppendCharsetParam(phoneticFamilyName)) {
                    this.mBuilder.append(";");
                    this.mBuilder.append(this.mVCardCharsetParameter);
                }
                if (reallyUseQuotedPrintable) {
                    this.mBuilder.append(";");
                    this.mBuilder.append("ENCODING=QUOTED-PRINTABLE");
                }
                this.mBuilder.append(":");
                this.mBuilder.append(encodedPhoneticFamilyName);
                this.mBuilder.append("\r\n");
            }
        }
    }

    public VCardBuilder appendNickNames(List<ContentValues> contentValuesList) {
        boolean useAndroidProperty;
        if (this.mIsV30OrV40) {
            useAndroidProperty = false;
        } else if (!this.mUsesAndroidProperty) {
            return this;
        } else {
            useAndroidProperty = true;
        }
        if (contentValuesList != null) {
            for (ContentValues contentValues : contentValuesList) {
                String nickname = contentValues.getAsString("data1");
                if (!TextUtils.isEmpty(nickname)) {
                    if (useAndroidProperty) {
                        appendAndroidSpecificProperty("vnd.android.cursor.item/nickname", contentValues);
                    } else {
                        appendLineWithCharsetAndQPDetection("NICKNAME", nickname);
                    }
                }
            }
        }
        return this;
    }

    public VCardBuilder appendPhones(List<ContentValues> contentValuesList, VCardPhoneNumberTranslationCallback translationCallback) {
        boolean phoneLineExists = false;
        if (contentValuesList != null) {
            Set<String> phoneSet = new HashSet();
            for (ContentValues contentValues : contentValuesList) {
                Integer typeAsObject = contentValues.getAsInteger("data2");
                String label = contentValues.getAsString("data3");
                Integer isPrimaryAsInteger = contentValues.getAsInteger("is_primary");
                boolean isPrimary = isPrimaryAsInteger != null && isPrimaryAsInteger.intValue() > 0;
                String phoneNumber = contentValues.getAsString("data1");
                if (phoneNumber != null) {
                    phoneNumber = phoneNumber.trim();
                }
                if (!TextUtils.isEmpty(phoneNumber)) {
                    int type = typeAsObject != null ? typeAsObject.intValue() : 1;
                    if (translationCallback != null) {
                        phoneNumber = translationCallback.onValueReceived(phoneNumber, type, label, isPrimary);
                        if (!phoneSet.contains(phoneNumber)) {
                            phoneSet.add(phoneNumber);
                            appendTelLine(Integer.valueOf(type), label, phoneNumber, isPrimary);
                        }
                    } else if (type == 6 || VCardConfig.refrainPhoneNumberFormatting(this.mVCardType)) {
                        phoneLineExists = true;
                        if (!phoneSet.contains(phoneNumber)) {
                            phoneSet.add(phoneNumber);
                            appendTelLine(Integer.valueOf(type), label, phoneNumber, isPrimary);
                        }
                    } else {
                        List<String> phoneNumberList = splitPhoneNumbers(phoneNumber);
                        if (!phoneNumberList.isEmpty()) {
                            phoneLineExists = true;
                            for (String actualPhoneNumber : phoneNumberList) {
                                if (!phoneSet.contains(actualPhoneNumber)) {
                                    String formatted;
                                    String numberWithControlSequence = actualPhoneNumber.replace(',', 'p').replace(';', 'w');
                                    if (TextUtils.equals(numberWithControlSequence, actualPhoneNumber)) {
                                        StringBuilder digitsOnlyBuilder = new StringBuilder();
                                        int length = actualPhoneNumber.length();
                                        for (int i = 0; i < length; i++) {
                                            char ch = actualPhoneNumber.charAt(i);
                                            if (Character.isDigit(ch) || ch == '+') {
                                                digitsOnlyBuilder.append(ch);
                                            }
                                        }
                                        if (VCardConfig.getNotConvertPhoneNumber()) {
                                            formatted = numberWithControlSequence;
                                        } else {
                                            formatted = PhoneNumberUtilsPort.formatNumber(digitsOnlyBuilder.toString(), VCardUtils.getPhoneNumberFormat(this.mVCardType));
                                        }
                                    } else {
                                        formatted = numberWithControlSequence;
                                    }
                                    if (!(!VCardConfig.isVersion40(this.mVCardType) || TextUtils.isEmpty(formatted) || formatted.startsWith("tel:"))) {
                                        formatted = "tel:" + formatted;
                                    }
                                    phoneSet.add(actualPhoneNumber);
                                    appendTelLine(Integer.valueOf(type), label, formatted, isPrimary);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!phoneLineExists && this.mIsDoCoMo) {
            appendTelLine(Integer.valueOf(1), "", "", false);
        }
        return this;
    }

    private List<String> splitPhoneNumbers(String phoneNumber) {
        List<String> phoneList = new ArrayList();
        StringBuilder builder = new StringBuilder();
        int length = phoneNumber.length();
        for (int i = 0; i < length; i++) {
            char ch = phoneNumber.charAt(i);
            if (ch != '\n' || builder.length() <= 0) {
                builder.append(ch);
            } else {
                phoneList.add(builder.toString());
                builder = new StringBuilder();
            }
        }
        if (builder.length() > 0) {
            phoneList.add(builder.toString());
        }
        return phoneList;
    }

    public VCardBuilder appendEmails(List<ContentValues> contentValuesList) {
        boolean emailAddressExists = false;
        if (contentValuesList != null) {
            Set<String> addressSet = new HashSet();
            for (ContentValues contentValues : contentValuesList) {
                String emailAddress = contentValues.getAsString("data1");
                if (emailAddress != null) {
                    emailAddress = emailAddress.trim();
                }
                if (!TextUtils.isEmpty(emailAddress)) {
                    Integer typeAsObject = contentValues.getAsInteger("data2");
                    int type = typeAsObject != null ? typeAsObject.intValue() : 3;
                    String label = contentValues.getAsString("data3");
                    Integer isPrimaryAsInteger = contentValues.getAsInteger("is_primary");
                    boolean isPrimary = isPrimaryAsInteger != null && isPrimaryAsInteger.intValue() > 0;
                    emailAddressExists = true;
                    if (!addressSet.contains(emailAddress)) {
                        addressSet.add(emailAddress);
                        appendEmailLine(type, label, emailAddress, isPrimary);
                    }
                }
            }
        }
        if (!emailAddressExists && this.mIsDoCoMo) {
            appendEmailLine(1, "", "", false);
        }
        return this;
    }

    public VCardBuilder appendPostals(List<ContentValues> contentValuesList) {
        if (contentValuesList == null || contentValuesList.isEmpty()) {
            if (this.mIsDoCoMo) {
                this.mBuilder.append("ADR");
                this.mBuilder.append(";");
                this.mBuilder.append("HOME");
                this.mBuilder.append(":");
                this.mBuilder.append("\r\n");
            }
        } else if (this.mIsDoCoMo) {
            appendPostalsForDoCoMo(contentValuesList);
        } else {
            appendPostalsForGeneric(contentValuesList);
        }
        return this;
    }

    private void appendPostalsForDoCoMo(List<ContentValues> contentValuesList) {
        int currentPriority = Integer.MAX_VALUE;
        int currentType = Integer.MAX_VALUE;
        ContentValues currentContentValues = null;
        for (ContentValues contentValues : contentValuesList) {
            if (contentValues != null) {
                Integer typeAsInteger = contentValues.getAsInteger("data2");
                Integer priorityAsInteger = (Integer) sPostalTypePriorityMap.get(typeAsInteger);
                int priority = priorityAsInteger != null ? priorityAsInteger.intValue() : Integer.MAX_VALUE;
                if (priority < currentPriority) {
                    currentPriority = priority;
                    currentType = typeAsInteger.intValue();
                    currentContentValues = contentValues;
                    if (priority == 0) {
                        break;
                    }
                } else {
                    continue;
                }
            }
        }
        if (currentContentValues == null) {
            Log.w("vCard", "Should not come here. Must have at least one postal data.");
        } else {
            appendPostalLine(currentType, currentContentValues.getAsString("data3"), currentContentValues, false, true);
        }
    }

    private void appendPostalsForGeneric(List<ContentValues> contentValuesList) {
        for (ContentValues contentValues : contentValuesList) {
            if (contentValues != null) {
                boolean isPrimary;
                Integer typeAsInteger = contentValues.getAsInteger("data2");
                int type = typeAsInteger != null ? typeAsInteger.intValue() : 1;
                String label = contentValues.getAsString("data3");
                Integer isPrimaryAsInteger = contentValues.getAsInteger("is_primary");
                if (isPrimaryAsInteger == null || isPrimaryAsInteger.intValue() <= 0) {
                    isPrimary = false;
                } else {
                    isPrimary = true;
                }
                appendPostalLine(type, label, contentValues, isPrimary, false);
            }
        }
    }

    private PostalStruct tryConstructPostalStruct(ContentValues contentValues) {
        String rawPoBox = contentValues.getAsString("data5");
        String rawNeighborhood = contentValues.getAsString("data6");
        String rawStreet = contentValues.getAsString("data4");
        String rawLocality = contentValues.getAsString("data7");
        String rawRegion = contentValues.getAsString("data8");
        String rawPostalCode = contentValues.getAsString("data9");
        String rawCountry = contentValues.getAsString("data10");
        String[] rawAddressArray = new String[]{rawPoBox, rawNeighborhood, rawStreet, rawLocality, rawRegion, rawPostalCode, rawCountry};
        boolean reallyUseQuotedPrintable;
        if (VCardUtils.areAllEmpty(rawAddressArray)) {
            String rawFormattedAddress = contentValues.getAsString("data1");
            if (TextUtils.isEmpty(rawFormattedAddress)) {
                return null;
            }
            String encodedFormattedAddress;
            if (this.mShouldUseQuotedPrintable) {
                reallyUseQuotedPrintable = !VCardUtils.containsOnlyNonCrLfPrintableAscii(rawFormattedAddress);
            } else {
                reallyUseQuotedPrintable = false;
            }
            boolean appendCharset = !VCardUtils.containsOnlyPrintableAscii(rawFormattedAddress);
            if (reallyUseQuotedPrintable) {
                encodedFormattedAddress = encodeQuotedPrintable(rawFormattedAddress);
            } else {
                encodedFormattedAddress = escapeCharacters(rawFormattedAddress);
            }
            StringBuilder addressBuilder = new StringBuilder();
            addressBuilder.append(";");
            addressBuilder.append(encodedFormattedAddress);
            addressBuilder.append(";");
            addressBuilder.append(";");
            addressBuilder.append(";");
            addressBuilder.append(";");
            addressBuilder.append(";");
            return new PostalStruct(reallyUseQuotedPrintable, appendCharset, addressBuilder.toString());
        }
        String encodedPoBox;
        String encodedStreet;
        String encodedLocality;
        String encodedRegion;
        String encodedPostalCode;
        String encodedCountry;
        String encodedNeighborhood;
        reallyUseQuotedPrintable = this.mShouldUseQuotedPrintable ? !VCardUtils.containsOnlyNonCrLfPrintableAscii(rawAddressArray) : false;
        if (VCardUtils.containsOnlyPrintableAscii(rawAddressArray)) {
            appendCharset = false;
        } else {
            appendCharset = true;
        }
        if (reallyUseQuotedPrintable) {
            encodedPoBox = encodeQuotedPrintable(rawPoBox);
            encodedStreet = encodeQuotedPrintable(rawStreet);
            encodedLocality = encodeQuotedPrintable(rawLocality);
            encodedRegion = encodeQuotedPrintable(rawRegion);
            encodedPostalCode = encodeQuotedPrintable(rawPostalCode);
            encodedCountry = encodeQuotedPrintable(rawCountry);
            encodedNeighborhood = encodeQuotedPrintable(rawNeighborhood);
        } else {
            encodedPoBox = escapeCharacters(rawPoBox);
            encodedStreet = escapeCharacters(rawStreet);
            encodedLocality = escapeCharacters(rawLocality);
            encodedRegion = escapeCharacters(rawRegion);
            encodedPostalCode = escapeCharacters(rawPostalCode);
            encodedCountry = escapeCharacters(rawCountry);
            encodedNeighborhood = escapeCharacters(rawNeighborhood);
        }
        addressBuilder = new StringBuilder();
        addressBuilder.append(encodedPoBox);
        addressBuilder.append(";");
        addressBuilder.append(encodedNeighborhood);
        addressBuilder.append(";");
        addressBuilder.append(encodedStreet);
        addressBuilder.append(";");
        addressBuilder.append(encodedLocality);
        addressBuilder.append(";");
        addressBuilder.append(encodedRegion);
        addressBuilder.append(";");
        addressBuilder.append(encodedPostalCode);
        addressBuilder.append(";");
        addressBuilder.append(encodedCountry);
        return new PostalStruct(reallyUseQuotedPrintable, appendCharset, addressBuilder.toString());
    }

    public VCardBuilder appendIms(List<ContentValues> contentValuesList) {
        if (contentValuesList != null) {
            for (ContentValues contentValues : contentValuesList) {
                Integer protocolAsObject = contentValues.getAsInteger("data5");
                if (protocolAsObject != null) {
                    String propertyName = VCardUtils.getPropertyNameForIm(protocolAsObject.intValue());
                    if (propertyName != null) {
                        String data = contentValues.getAsString("data1");
                        if (data != null) {
                            data = data.trim();
                        }
                        if (!TextUtils.isEmpty(data)) {
                            int intValue;
                            CharSequence typeAsString;
                            boolean isPrimary;
                            Integer typeAsInteger = contentValues.getAsInteger("data2");
                            if (typeAsInteger != null) {
                                intValue = typeAsInteger.intValue();
                            } else {
                                intValue = 3;
                            }
                            switch (intValue) {
                                case 0:
                                    String label = contentValues.getAsString("data3");
                                    if (label == null) {
                                        typeAsString = null;
                                        break;
                                    }
                                    typeAsString = "X-" + label;
                                    break;
                                case 1:
                                    typeAsString = "HOME";
                                    break;
                                case 2:
                                    typeAsString = "WORK";
                                    break;
                                default:
                                    typeAsString = null;
                                    break;
                            }
                            List<String> parameterList = new ArrayList();
                            if (!TextUtils.isEmpty(typeAsString)) {
                                parameterList.add(typeAsString);
                            }
                            Integer isPrimaryAsInteger = contentValues.getAsInteger("is_primary");
                            if (isPrimaryAsInteger == null || isPrimaryAsInteger.intValue() <= 0) {
                                isPrimary = false;
                            } else {
                                isPrimary = true;
                            }
                            if (isPrimary) {
                                parameterList.add("PREF");
                            }
                            appendLineWithCharsetAndQPDetection(propertyName, parameterList, data);
                        }
                    }
                }
            }
        }
        return this;
    }

    public VCardBuilder appendWebsites(List<ContentValues> contentValuesList) {
        if (contentValuesList != null) {
            for (ContentValues contentValues : contentValuesList) {
                String website = contentValues.getAsString("data1");
                if (website != null) {
                    website = website.trim();
                }
                if (!TextUtils.isEmpty(website)) {
                    appendLineWithCharsetAndQPDetection("URL", website);
                }
            }
        }
        return this;
    }

    public VCardBuilder appendOrganizations(List<ContentValues> contentValuesList) {
        if (contentValuesList != null) {
            for (ContentValues contentValues : contentValuesList) {
                boolean z;
                String company = contentValues.getAsString("data1");
                if (company != null) {
                    company = company.trim();
                }
                String department = contentValues.getAsString("data5");
                if (department != null) {
                    department = department.trim();
                }
                String title = contentValues.getAsString("data4");
                if (title != null) {
                    title = title.trim();
                }
                StringBuilder orgBuilder = new StringBuilder();
                if (!TextUtils.isEmpty(company)) {
                    orgBuilder.append(company);
                }
                if (!TextUtils.isEmpty(department)) {
                    if (orgBuilder.length() > 0) {
                        orgBuilder.append(';');
                    }
                    orgBuilder.append(department);
                }
                String str = "ORG";
                boolean z2 = !VCardUtils.containsOnlyPrintableAscii(orgBuilder.toString());
                if (this.mShouldUseQuotedPrintable) {
                    z = !VCardUtils.containsOnlyNonCrLfPrintableAscii(orgline);
                } else {
                    z = false;
                }
                appendLine(str, orgline, z2, z);
                if (!TextUtils.isEmpty(title)) {
                    str = "TITLE";
                    if (VCardUtils.containsOnlyPrintableAscii(title)) {
                        z2 = false;
                    } else {
                        z2 = true;
                    }
                    if (this.mShouldUseQuotedPrintable) {
                        z = !VCardUtils.containsOnlyNonCrLfPrintableAscii(title);
                    } else {
                        z = false;
                    }
                    appendLine(str, title, z2, z);
                }
            }
        }
        return this;
    }

    public VCardBuilder appendPhotos(List<ContentValues> contentValuesList) {
        if (contentValuesList != null) {
            for (ContentValues contentValues : contentValuesList) {
                if (contentValues != null) {
                    byte[] data = contentValues.getAsByteArray("data15");
                    if (data != null) {
                        String photoType = VCardUtils.guessImageType(data);
                        if (photoType == null) {
                            Log.d("vCard", "Unknown photo type. Ignored.");
                        } else {
                            String photoString = new String(Base64.encode(data, 2));
                            if (!TextUtils.isEmpty(photoString)) {
                                appendPhotoLine(photoString, photoType);
                            }
                        }
                    }
                }
            }
        }
        return this;
    }

    public VCardBuilder appendNotes(List<ContentValues> contentValuesList) {
        if (contentValuesList != null) {
            boolean shouldAppendCharsetInfo;
            boolean reallyUseQuotedPrintable;
            String noteStr;
            if (this.mOnlyOneNoteFieldIsAvailable) {
                StringBuilder noteBuilder = new StringBuilder();
                boolean first = true;
                for (ContentValues contentValues : contentValuesList) {
                    String note = contentValues.getAsString("data1");
                    if (note == null) {
                        note = "";
                    }
                    if (note.length() > 0) {
                        if (first) {
                            first = false;
                        } else {
                            noteBuilder.append('\n');
                        }
                        noteBuilder.append(note);
                    }
                }
                shouldAppendCharsetInfo = !VCardUtils.containsOnlyPrintableAscii(noteBuilder.toString());
                if (this.mShouldUseQuotedPrintable) {
                    reallyUseQuotedPrintable = !VCardUtils.containsOnlyNonCrLfPrintableAscii(noteStr);
                } else {
                    reallyUseQuotedPrintable = false;
                }
                appendLine("NOTE", noteStr, shouldAppendCharsetInfo, reallyUseQuotedPrintable);
            } else {
                for (ContentValues contentValues2 : contentValuesList) {
                    noteStr = contentValues2.getAsString("data1");
                    if (!TextUtils.isEmpty(noteStr)) {
                        shouldAppendCharsetInfo = !VCardUtils.containsOnlyPrintableAscii(noteStr);
                        if (this.mShouldUseQuotedPrintable) {
                            reallyUseQuotedPrintable = !VCardUtils.containsOnlyNonCrLfPrintableAscii(noteStr);
                        } else {
                            reallyUseQuotedPrintable = false;
                        }
                        appendLine("NOTE", noteStr, shouldAppendCharsetInfo, reallyUseQuotedPrintable);
                    }
                }
            }
        }
        return this;
    }

    public VCardBuilder appendEvents(List<ContentValues> contentValuesList) {
        if (contentValuesList != null) {
            String primaryBirthday = null;
            String secondaryBirthday = null;
            for (ContentValues contentValues : contentValuesList) {
                if (contentValues != null) {
                    int eventType;
                    Integer eventTypeAsInteger = contentValues.getAsInteger("data2");
                    if (eventTypeAsInteger != null) {
                        eventType = eventTypeAsInteger.intValue();
                    } else {
                        eventType = 2;
                    }
                    if (eventType == 3) {
                        String birthdayCandidate = contentValues.getAsString("data1");
                        if (birthdayCandidate != null) {
                            boolean isSuperPrimary;
                            Integer isSuperPrimaryAsInteger = contentValues.getAsInteger("is_super_primary");
                            if (isSuperPrimaryAsInteger == null || isSuperPrimaryAsInteger.intValue() <= 0) {
                                isSuperPrimary = false;
                            } else {
                                isSuperPrimary = true;
                            }
                            if (isSuperPrimary) {
                                primaryBirthday = birthdayCandidate;
                                break;
                            }
                            boolean isPrimary;
                            Integer isPrimaryAsInteger = contentValues.getAsInteger("is_primary");
                            if (isPrimaryAsInteger == null || isPrimaryAsInteger.intValue() <= 0) {
                                isPrimary = false;
                            } else {
                                isPrimary = true;
                            }
                            if (isPrimary) {
                                primaryBirthday = birthdayCandidate;
                            } else if (secondaryBirthday == null) {
                                secondaryBirthday = birthdayCandidate;
                            }
                        } else {
                            continue;
                        }
                    } else if (this.mUsesAndroidProperty) {
                        appendAndroidSpecificProperty("vnd.android.cursor.item/contact_event", contentValues);
                    }
                }
            }
            if (primaryBirthday != null) {
                appendLineWithCharsetAndQPDetection("BDAY", primaryBirthday.trim());
            } else if (secondaryBirthday != null) {
                appendLineWithCharsetAndQPDetection("BDAY", secondaryBirthday.trim());
            }
        }
        return this;
    }

    public VCardBuilder appendRelation(List<ContentValues> contentValuesList) {
        if (this.mUsesAndroidProperty && contentValuesList != null) {
            for (ContentValues contentValues : contentValuesList) {
                if (contentValues != null) {
                    appendAndroidSpecificProperty("vnd.android.cursor.item/relation", contentValues);
                }
            }
        }
        return this;
    }

    public void appendGroupMemberships(List<ContentValues> contentValuesList, ContentResolver contentResolver, Map<Long, String> groupMap) {
        if (contentValuesList == null) {
            Log.i("vCard", "appendGroupMembership null contentValuesList");
            return;
        }
        for (ContentValues contentValues : contentValuesList) {
            if (contentValues != null) {
                Long groupId = contentValues.getAsLong("data1");
                if (groupId != null) {
                    String groupTitle = getGroupTitle(groupId, contentResolver, groupMap);
                    if (!TextUtils.isEmpty(groupTitle)) {
                        Log.d("vCard", "appendGroupMembership groupTitle = " + groupTitle);
                        appendLine("X-GROUP-MEMBERSHIP", groupTitle);
                    }
                }
            }
        }
    }

    private String getGroupTitle(Long groupId, ContentResolver contentResolver, Map<Long, String> groupMap) {
        String title = (String) groupMap.get(groupId);
        if (!TextUtils.isEmpty(title)) {
            return title;
        }
        String selection = "_id=? AND deleted<>1";
        try {
            Cursor cursor = contentResolver.query(Groups.CONTENT_URI, null, "_id=? AND deleted<>1", new String[]{String.valueOf(groupId)}, null);
            if (cursor == null) {
                Log.e("vCard", "query group returns null cursor");
                return null;
            }
            if (cursor.moveToFirst()) {
                title = cursor.getString(cursor.getColumnIndex("title"));
                if (TextUtils.isEmpty(title)) {
                    return null;
                }
                groupMap.put(groupId, title);
            }
            return title;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void appendPostalLine(int type, String label, ContentValues contentValues, boolean isPrimary, boolean emitEveryTime) {
        boolean reallyUseQuotedPrintable;
        boolean appendCharset;
        String addressValue;
        PostalStruct postalStruct = tryConstructPostalStruct(contentValues);
        if (postalStruct != null) {
            reallyUseQuotedPrintable = postalStruct.reallyUseQuotedPrintable;
            appendCharset = postalStruct.appendCharset;
            addressValue = postalStruct.addressData;
        } else if (emitEveryTime) {
            reallyUseQuotedPrintable = false;
            appendCharset = false;
            addressValue = "";
        } else {
            return;
        }
        List<String> parameterList = new ArrayList();
        if (isPrimary) {
            parameterList.add("PREF");
        }
        switch (type) {
            case 0:
                if (!TextUtils.isEmpty(label)) {
                    if (VCardUtils.containsOnlyAlphaDigitHyphen(label)) {
                        parameterList.add("X-" + label);
                        break;
                    }
                }
                break;
            case 1:
                parameterList.add("HOME");
                break;
            case 2:
                parameterList.add("WORK");
                break;
            case 3:
                break;
            default:
                Log.e("vCard", "Unknown StructuredPostal type: " + type);
                break;
        }
        this.mBuilder.append("ADR");
        if (!parameterList.isEmpty()) {
            this.mBuilder.append(";");
            appendTypeParameters(parameterList);
        }
        if (appendCharset) {
            this.mBuilder.append(";");
            this.mBuilder.append(this.mVCardCharsetParameter);
        }
        if (reallyUseQuotedPrintable) {
            this.mBuilder.append(";");
            this.mBuilder.append("ENCODING=QUOTED-PRINTABLE");
        }
        this.mBuilder.append(":");
        this.mBuilder.append(addressValue);
        this.mBuilder.append("\r\n");
    }

    public void appendEmailLine(int type, String label, String rawValue, boolean isPrimary) {
        CharSequence charSequence;
        switch (type) {
            case 0:
                if (!VCardUtils.isMobilePhoneLabel(label)) {
                    if (!TextUtils.isEmpty(label)) {
                        if (VCardUtils.containsOnlyAlphaDigitHyphen(label)) {
                            charSequence = "X-" + label;
                            break;
                        }
                    }
                    charSequence = null;
                    break;
                }
                charSequence = "CELL";
                break;
            case 1:
                charSequence = "HOME";
                break;
            case 2:
                charSequence = "WORK";
                break;
            case 3:
                charSequence = null;
                break;
            case 4:
                charSequence = "CELL";
                break;
            default:
                Log.e("vCard", "Unknown Email type: " + type);
                charSequence = null;
                break;
        }
        List<String> parameterList = new ArrayList();
        if (isPrimary) {
            parameterList.add("PREF");
        }
        if (!TextUtils.isEmpty(charSequence)) {
            parameterList.add(charSequence);
        }
        appendLineWithCharsetAndQPDetection("EMAIL", parameterList, rawValue);
    }

    public void appendTelLine(Integer typeAsInteger, String label, String encodedValue, boolean isPrimary) {
        int type;
        this.mBuilder.append("TEL");
        this.mBuilder.append(";");
        if (typeAsInteger == null) {
            type = 7;
        } else {
            type = typeAsInteger.intValue();
        }
        ArrayList<String> parameterList = new ArrayList();
        switch (type) {
            case 0:
                if (!TextUtils.isEmpty(label)) {
                    if (!VCardUtils.isMobilePhoneLabel(label)) {
                        if (!this.mIsV30OrV40) {
                            String upperLabel = label.toUpperCase();
                            if (!VCardUtils.isValidInV21ButUnknownToContactsPhoteType(upperLabel)) {
                                if (VCardUtils.containsOnlyAlphaDigitHyphen(label)) {
                                    parameterList.add("X-" + label);
                                    break;
                                }
                            }
                            parameterList.add(upperLabel);
                            break;
                        }
                        parameterList.add(label);
                        break;
                    }
                    parameterList.add("CELL");
                    break;
                }
                parameterList.add("VOICE");
                break;
                break;
            case 1:
                parameterList.addAll(Arrays.asList(new String[]{"HOME"}));
                break;
            case 2:
                parameterList.add("CELL");
                break;
            case 3:
                parameterList.addAll(Arrays.asList(new String[]{"WORK"}));
                break;
            case 4:
                parameterList.addAll(Arrays.asList(new String[]{"WORK", "FAX"}));
                break;
            case 5:
                parameterList.addAll(Arrays.asList(new String[]{"HOME", "FAX"}));
                break;
            case 6:
                if (!this.mIsDoCoMo) {
                    parameterList.add("PAGER");
                    break;
                } else {
                    parameterList.add("VOICE");
                    break;
                }
            case 7:
                parameterList.add("VOICE");
                break;
            case 9:
                parameterList.add("CAR");
                break;
            case 10:
                parameterList.add("WORK");
                isPrimary = true;
                break;
            case 11:
                parameterList.add("ISDN");
                break;
            case 12:
                isPrimary = true;
                break;
            case 13:
                parameterList.add("FAX");
                break;
            case 15:
                parameterList.add("TLX");
                break;
            case 17:
                parameterList.addAll(Arrays.asList(new String[]{"WORK", "CELL"}));
                break;
            case 18:
                parameterList.add("WORK");
                if (!this.mIsDoCoMo) {
                    parameterList.add("PAGER");
                    break;
                } else {
                    parameterList.add("VOICE");
                    break;
                }
            case 20:
                parameterList.add("MSG");
                break;
        }
        if (isPrimary) {
            parameterList.add("PREF");
        }
        if (parameterList.isEmpty()) {
            appendUncommonPhoneType(this.mBuilder, Integer.valueOf(type));
        } else {
            appendTypeParameters(parameterList);
        }
        this.mBuilder.append(":");
        this.mBuilder.append(encodedValue);
        this.mBuilder.append("\r\n");
    }

    private void appendUncommonPhoneType(StringBuilder builder, Integer type) {
        if (this.mIsDoCoMo) {
            builder.append("VOICE");
            return;
        }
        String phoneType = VCardUtils.getPhoneTypeString(type);
        if (phoneType != null) {
            appendTypeParameter(phoneType);
        } else {
            Log.e("vCard", "Unknown or unsupported (by vCard) Phone type: " + type);
        }
    }

    public void appendPhotoLine(String encodedValue, String photoType) {
        StringBuilder tmpBuilder = new StringBuilder();
        tmpBuilder.append("PHOTO");
        tmpBuilder.append(";");
        if (this.mIsV30OrV40) {
            tmpBuilder.append("ENCODING=B");
        } else {
            tmpBuilder.append("ENCODING=BASE64");
        }
        tmpBuilder.append(";");
        appendTypeParameter(tmpBuilder, photoType);
        tmpBuilder.append(":");
        tmpBuilder.append(encodedValue);
        String tmpStr = tmpBuilder.toString();
        tmpBuilder = new StringBuilder();
        int lineCount = 0;
        int length = tmpStr.length();
        int maxNumForFirstLine = 75 - "\r\n".length();
        int maxNumInGeneral = maxNumForFirstLine - HwCustPreloadContacts.EMPTY_STRING.length();
        int maxNum = maxNumForFirstLine;
        for (int i = 0; i < length; i++) {
            tmpBuilder.append(tmpStr.charAt(i));
            lineCount++;
            if (lineCount > maxNum) {
                tmpBuilder.append("\r\n");
                tmpBuilder.append(HwCustPreloadContacts.EMPTY_STRING);
                maxNum = maxNumInGeneral;
                lineCount = 0;
            }
        }
        this.mBuilder.append(tmpBuilder.toString());
        this.mBuilder.append("\r\n");
        this.mBuilder.append("\r\n");
    }

    public VCardBuilder appendSipAddresses(List<ContentValues> contentValuesList) {
        boolean useXProperty;
        if (this.mIsV30OrV40) {
            useXProperty = false;
        } else if (!this.mUsesDefactProperty) {
            return this;
        } else {
            useXProperty = true;
        }
        if (contentValuesList != null) {
            for (ContentValues contentValues : contentValuesList) {
                String sipAddress = contentValues.getAsString("data1");
                if (!TextUtils.isEmpty(sipAddress)) {
                    if (useXProperty) {
                        if (sipAddress.startsWith("sip:")) {
                            if (sipAddress.length() != 4) {
                                sipAddress = sipAddress.substring(4);
                            }
                        }
                        appendLineWithCharsetAndQPDetection("X-SIP", sipAddress);
                    } else {
                        String propertyName;
                        if (!sipAddress.startsWith("sip:")) {
                            sipAddress = "sip:" + sipAddress;
                        }
                        if (VCardConfig.isVersion40(this.mVCardType)) {
                            propertyName = "TEL";
                        } else {
                            propertyName = "IMPP";
                        }
                        appendLineWithCharsetAndQPDetection(propertyName, sipAddress);
                    }
                }
            }
        }
        return this;
    }

    public void appendAndroidSpecificProperty(String mimeType, ContentValues contentValues) {
        if (sAllowedAndroidPropertySet.contains(mimeType)) {
            Collection<String> rawValueList = new ArrayList();
            for (int i = 1; i <= 15; i++) {
                String value = contentValues.getAsString(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH + i);
                if (value == null) {
                    value = "";
                }
                rawValueList.add(value);
            }
            boolean needCharset = this.mShouldAppendCharsetParam ? !VCardUtils.containsOnlyNonCrLfPrintableAscii((Collection) rawValueList) : false;
            boolean reallyUseQuotedPrintable = this.mShouldUseQuotedPrintable ? !VCardUtils.containsOnlyNonCrLfPrintableAscii((Collection) rawValueList) : false;
            this.mBuilder.append("X-ANDROID-CUSTOM");
            if (needCharset) {
                this.mBuilder.append(";");
                this.mBuilder.append(this.mVCardCharsetParameter);
            }
            if (reallyUseQuotedPrintable) {
                this.mBuilder.append(";");
                this.mBuilder.append("ENCODING=QUOTED-PRINTABLE");
            }
            this.mBuilder.append(":");
            this.mBuilder.append(mimeType);
            for (String rawValue : rawValueList) {
                String encodedValue;
                if (reallyUseQuotedPrintable) {
                    encodedValue = encodeQuotedPrintable(rawValue);
                } else {
                    encodedValue = escapeCharacters(rawValue);
                }
                this.mBuilder.append(";");
                this.mBuilder.append(encodedValue);
            }
            this.mBuilder.append("\r\n");
        }
    }

    public void appendLineWithCharsetAndQPDetection(String propertyName, String rawValue) {
        appendLineWithCharsetAndQPDetection(propertyName, null, rawValue);
    }

    public void appendLineWithCharsetAndQPDetection(String propertyName, List<String> parameterList, String rawValue) {
        boolean reallyUseQuotedPrintable = false;
        boolean needCharset = !VCardUtils.containsOnlyPrintableAscii(rawValue);
        if (this.mShouldUseQuotedPrintable) {
            if (!VCardUtils.containsOnlyNonCrLfPrintableAscii(rawValue)) {
                reallyUseQuotedPrintable = true;
            }
        }
        appendLine(propertyName, parameterList, rawValue, needCharset, reallyUseQuotedPrintable);
    }

    public void appendLine(String propertyName, String rawValue) {
        appendLine(propertyName, rawValue, false, false);
    }

    public void appendLine(String propertyName, String rawValue, boolean needCharset, boolean reallyUseQuotedPrintable) {
        appendLine(propertyName, null, rawValue, needCharset, reallyUseQuotedPrintable);
    }

    public void appendLine(String propertyName, List<String> parameterList, String rawValue, boolean needCharset, boolean reallyUseQuotedPrintable) {
        String encodedValue;
        this.mBuilder.append(propertyName);
        if (parameterList != null && parameterList.size() > 0) {
            this.mBuilder.append(";");
            appendTypeParameters(parameterList);
        }
        if (needCharset) {
            this.mBuilder.append(";");
            this.mBuilder.append(this.mVCardCharsetParameter);
        }
        if (reallyUseQuotedPrintable) {
            this.mBuilder.append(";");
            this.mBuilder.append("ENCODING=QUOTED-PRINTABLE");
            encodedValue = encodeQuotedPrintable(rawValue);
        } else {
            encodedValue = escapeCharacters(rawValue);
        }
        this.mBuilder.append(":");
        this.mBuilder.append(encodedValue);
        this.mBuilder.append("\r\n");
    }

    private void appendTypeParameters(List<String> types) {
        boolean first = true;
        for (String typeValue : types) {
            if (VCardConfig.isVersion30(this.mVCardType) || VCardConfig.isVersion40(this.mVCardType)) {
                String encoded;
                if (VCardConfig.isVersion40(this.mVCardType)) {
                    encoded = VCardUtils.toStringAsV40ParamValue(typeValue);
                } else {
                    encoded = VCardUtils.toStringAsV30ParamValue(typeValue);
                }
                if (!TextUtils.isEmpty(encoded)) {
                    if (first) {
                        first = false;
                    } else {
                        this.mBuilder.append(";");
                    }
                    appendTypeParameter(encoded);
                }
            } else if (VCardUtils.isV21Word(typeValue)) {
                if (first) {
                    first = false;
                } else {
                    this.mBuilder.append(";");
                }
                appendTypeParameter(typeValue);
            }
        }
    }

    private void appendTypeParameter(String type) {
        appendTypeParameter(this.mBuilder, type);
    }

    private void appendTypeParameter(StringBuilder builder, String type) {
        if (VCardConfig.isVersion40(this.mVCardType) || ((VCardConfig.isVersion30(this.mVCardType) || this.mAppendTypeParamName) && !this.mIsDoCoMo)) {
            builder.append("TYPE").append("=");
        }
        builder.append(type);
    }

    private boolean shouldAppendCharsetParam(String... propertyValueList) {
        if (!this.mShouldAppendCharsetParam) {
            return false;
        }
        int length = propertyValueList.length;
        for (int i = 0; i < length; i++) {
            if (!VCardUtils.containsOnlyPrintableAscii(propertyValueList[i])) {
                return true;
            }
        }
        return false;
    }

    private String encodeQuotedPrintable(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        byte[] strArray;
        StringBuilder builder = new StringBuilder();
        int index = 0;
        int lineCount = 0;
        try {
            strArray = str.getBytes(this.mCharset);
        } catch (UnsupportedEncodingException e) {
            Log.e("vCard", "Charset " + this.mCharset + " cannot be used. " + "Try default charset");
            strArray = str.getBytes();
        }
        while (index < strArray.length) {
            builder.append(String.format("=%02X", new Object[]{Byte.valueOf(strArray[index])}));
            index++;
            lineCount += 3;
            if (lineCount >= 67) {
                builder.append("=\r\n");
                lineCount = 0;
            }
        }
        return builder.toString();
    }

    private String escapeCharacters(String unescaped) {
        if (TextUtils.isEmpty(unescaped)) {
            return "";
        }
        StringBuilder tmpBuilder = new StringBuilder();
        int length = unescaped.length();
        int i = 0;
        while (i < length) {
            char ch = unescaped.charAt(i);
            switch (ch) {
                case '\r':
                    if (i + 1 < length && unescaped.charAt(i) == '\n') {
                        break;
                    }
                case '\n':
                    tmpBuilder.append("\\n");
                    break;
                case Place.TYPE_GYM /*44*/:
                    if (!this.mIsV30OrV40) {
                        tmpBuilder.append(ch);
                        break;
                    }
                    tmpBuilder.append("\\,");
                    break;
                case Place.TYPE_LODGING /*59*/:
                    tmpBuilder.append('\\');
                    tmpBuilder.append(';');
                    break;
                case Place.TYPE_MEAL_DELIVERY /*60*/:
                case Place.TYPE_MOSQUE /*62*/:
                    if (!this.mIsDoCoMo) {
                        tmpBuilder.append(ch);
                        break;
                    }
                    tmpBuilder.append('\\');
                    tmpBuilder.append(ch);
                    break;
                case Place.TYPE_TRAIN_STATION /*92*/:
                    tmpBuilder.append("\\\\");
                    break;
                default:
                    tmpBuilder.append(ch);
                    break;
            }
            i++;
        }
        return tmpBuilder.toString();
    }

    public String toString() {
        if (!this.mEndAppended) {
            if (this.mIsDoCoMo) {
                appendLine("X-CLASS", "PUBLIC");
                appendLine("X-REDUCTION", "");
                appendLine("X-NO", "");
                appendLine("X-DCM-HMN-MODE", "");
            }
            appendLine("END", "VCARD");
            this.mEndAppended = true;
        }
        return this.mBuilder.toString();
    }
}
