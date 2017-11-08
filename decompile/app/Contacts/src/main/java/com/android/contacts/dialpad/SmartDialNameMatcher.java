package com.android.contacts.dialpad;

import android.text.TextUtils;
import com.android.contacts.dialpad.SmartDialPrefix.PhoneNumberTokens;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.ArrayList;

public class SmartDialNameMatcher {
    public static final SmartDialMap LATIN_SMART_DIAL_MAP = new LatinSmartDialMap();
    private final SmartDialMap mMap;
    private final ArrayList<SmartDialMatchPosition> mMatchPositions;
    private String mNameMatchMask;
    private String mPhoneNumberMatchMask;
    private final String mQuery;

    @VisibleForTesting
    public SmartDialNameMatcher(String query) {
        this(query, LATIN_SMART_DIAL_MAP);
    }

    public SmartDialNameMatcher(String query, SmartDialMap map) {
        this.mMatchPositions = Lists.newArrayList();
        this.mNameMatchMask = "";
        this.mPhoneNumberMatchMask = "";
        this.mQuery = query;
        this.mMap = map;
    }

    private void constructEmptyMask(StringBuilder builder, int length) {
        for (int i = 0; i < length; i++) {
            builder.append("0");
        }
    }

    private void replaceBitInMask(StringBuilder builder, SmartDialMatchPosition matchPos) {
        for (int i = matchPos.start; i < matchPos.end; i++) {
            builder.replace(i, i + 1, CallInterceptDetails.BRANDED_STATE);
        }
    }

    public static String normalizeNumber(String number, SmartDialMap map) {
        return normalizeNumber(number, 0, map);
    }

    public static String normalizeNumber(String number, int offset, SmartDialMap map) {
        StringBuilder s = new StringBuilder();
        for (int i = offset; i < number.length(); i++) {
            char ch = number.charAt(i);
            if (map.isValidDialpadNumericChar(ch)) {
                s.append(ch);
            }
        }
        return s.toString();
    }

    @VisibleForTesting
    public SmartDialMatchPosition matchesNumber(String phoneNumber, String query, boolean useNanp) {
        StringBuilder builder = new StringBuilder();
        constructEmptyMask(builder, phoneNumber.length());
        this.mPhoneNumberMatchMask = builder.toString();
        SmartDialMatchPosition matchPos = matchesNumberWithOffset(phoneNumber, query, 0);
        if (matchPos == null) {
            PhoneNumberTokens phoneNumberTokens = SmartDialPrefix.parsePhoneNumber(phoneNumber);
            if (phoneNumberTokens.countryCodeOffset != 0) {
                matchPos = matchesNumberWithOffset(phoneNumber, query, phoneNumberTokens.countryCodeOffset);
            }
            if (matchPos == null && phoneNumberTokens.nanpCodeOffset != 0 && useNanp) {
                matchPos = matchesNumberWithOffset(phoneNumber, query, phoneNumberTokens.nanpCodeOffset);
            }
        }
        if (matchPos != null) {
            replaceBitInMask(builder, matchPos);
            this.mPhoneNumberMatchMask = builder.toString();
        }
        return matchPos;
    }

    public SmartDialMatchPosition matchesNumber(String phoneNumber, String query) {
        return matchesNumber(phoneNumber, query, true);
    }

    private SmartDialMatchPosition matchesNumberWithOffset(String phoneNumber, String query, int offset) {
        if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(query)) {
            return null;
        }
        int queryAt = 0;
        int numberAt = offset;
        for (int i = offset; i < phoneNumber.length() && queryAt != query.length(); i++) {
            char ch = phoneNumber.charAt(i);
            if (this.mMap.isValidDialpadNumericChar(ch)) {
                if (ch != query.charAt(queryAt)) {
                    return null;
                }
                queryAt++;
            } else if (queryAt == 0 && offset != 0) {
                offset++;
            }
            numberAt++;
        }
        return new SmartDialMatchPosition(offset + 0, numberAt);
    }

    @VisibleForTesting
    boolean matchesCombination(String displayName, String query, ArrayList<SmartDialMatchPosition> matchList) {
        StringBuilder builder = new StringBuilder();
        constructEmptyMask(builder, displayName.length());
        this.mNameMatchMask = builder.toString();
        int nameLength = displayName.length();
        int queryLength = query.length();
        if (nameLength < queryLength) {
            return false;
        }
        if (queryLength == 0) {
            return false;
        }
        int nameStart = 0;
        int queryStart = 0;
        int tokenStart = 0;
        int seperatorCount = 0;
        ArrayList<SmartDialMatchPosition> partial = new ArrayList();
        while (nameStart < nameLength && queryStart < queryLength) {
            char ch = this.mMap.normalizeCharacter(displayName.charAt(nameStart));
            if (this.mMap.isValidDialpadCharacter(ch)) {
                if (this.mMap.isValidDialpadAlphabeticChar(ch)) {
                    ch = this.mMap.getDialpadNumericCharacter(ch);
                }
                if (ch != query.charAt(queryStart)) {
                    if (queryStart == 0 || this.mMap.isValidDialpadCharacter(this.mMap.normalizeCharacter(displayName.charAt(nameStart - 1)))) {
                        while (nameStart < nameLength && this.mMap.isValidDialpadCharacter(this.mMap.normalizeCharacter(displayName.charAt(nameStart)))) {
                            nameStart++;
                        }
                        nameStart++;
                    }
                    queryStart = 0;
                    seperatorCount = 0;
                    tokenStart = nameStart;
                } else if (queryStart == queryLength - 1) {
                    matchList.add(new SmartDialMatchPosition(tokenStart, (queryLength + tokenStart) + seperatorCount));
                    for (SmartDialMatchPosition match : matchList) {
                        replaceBitInMask(builder, match);
                    }
                    this.mNameMatchMask = builder.toString();
                    return true;
                } else {
                    if (queryStart < 1) {
                        int j = nameStart;
                        while (j < nameLength && this.mMap.isValidDialpadCharacter(this.mMap.normalizeCharacter(displayName.charAt(j)))) {
                            j++;
                        }
                        if (j < nameLength - 1) {
                            String remainder = displayName.substring(j + 1);
                            ArrayList<SmartDialMatchPosition> partialTemp = Lists.newArrayList();
                            if (matchesCombination(remainder, query.substring(queryStart + 1), partialTemp)) {
                                SmartDialMatchPosition.advanceMatchPositions(partialTemp, j + 1);
                                partialTemp.add(0, new SmartDialMatchPosition(nameStart, nameStart + 1));
                                partial = partialTemp;
                            }
                        }
                    }
                    nameStart++;
                    queryStart++;
                }
            } else {
                nameStart++;
                if (queryStart == 0) {
                    tokenStart = nameStart;
                } else {
                    seperatorCount++;
                }
            }
        }
        if (partial.isEmpty()) {
            return false;
        }
        matchList.addAll(partial);
        for (SmartDialMatchPosition match2 : matchList) {
            replaceBitInMask(builder, match2);
        }
        this.mNameMatchMask = builder.toString();
        return true;
    }

    public boolean matches(String displayName) {
        this.mMatchPositions.clear();
        return matchesCombination(displayName, this.mQuery, this.mMatchPositions);
    }
}
