package com.android.contacts.hap.roaming;

public class RoamingLearnCarrier {
    private String mDailNetworkCountryIso;
    private String mDialNumber;
    private boolean mIsRoaming;
    private boolean mOriginalNormalizedNumberIsNull;
    private String mOriginalNumber;

    public RoamingLearnCarrier(String number, boolean mOriginalNormalizedNumberIsNull) {
        this(null, number, mOriginalNormalizedNumberIsNull);
    }

    public boolean getOriginalNormalizedNumberIsNull() {
        return this.mOriginalNormalizedNumberIsNull;
    }

    public RoamingLearnCarrier(String dailNumber, String number, boolean mOriginalNormalizedNumberIsNull) {
        this.mDialNumber = null;
        this.mOriginalNormalizedNumberIsNull = false;
        this.mOriginalNumber = null;
        this.mIsRoaming = false;
        this.mDailNetworkCountryIso = null;
        this.mDialNumber = IsPhoneNetworkRoamingUtils.removeDashesAndBlanksBrackets(dailNumber);
        this.mOriginalNumber = IsPhoneNetworkRoamingUtils.removeDashesAndBlanksBrackets(number);
        this.mOriginalNormalizedNumberIsNull = mOriginalNormalizedNumberIsNull;
        this.mDailNetworkCountryIso = IsPhoneNetworkRoamingUtils.getNetworkCountryIso();
    }

    public String getmDailNetworkCountryIso() {
        return this.mDailNetworkCountryIso;
    }

    public String getDialNumber() {
        return this.mDialNumber;
    }

    public String getOriginalNumber() {
        return this.mOriginalNumber;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        boolean result = false;
        if (o instanceof RoamingLearnCarrier) {
            String tempNumber = ((RoamingLearnCarrier) o).mDialNumber;
            if (!(this.mDialNumber == null || tempNumber == null)) {
                result = this.mDialNumber.equals(tempNumber);
            }
        }
        return result;
    }

    public int hashCode() {
        return (this.mDialNumber == null ? 0 : this.mDialNumber.hashCode()) + 31;
    }
}
