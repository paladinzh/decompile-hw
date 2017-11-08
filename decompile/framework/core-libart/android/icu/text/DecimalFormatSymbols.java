package android.icu.text;

import android.icu.impl.CurrencyData;
import android.icu.impl.CurrencyData.CurrencyDisplayInfo;
import android.icu.impl.CurrencyData.CurrencyFormatInfo;
import android.icu.impl.CurrencyData.CurrencySpacingInfo;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SoftCache;
import android.icu.impl.locale.LanguageTag;
import android.icu.util.Currency;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.ULocale.Type;
import android.icu.util.UResourceBundle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.ChoiceFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;

public class DecimalFormatSymbols implements Cloneable, Serializable {
    public static final int CURRENCY_SPC_CURRENCY_MATCH = 0;
    public static final int CURRENCY_SPC_INSERT = 2;
    public static final int CURRENCY_SPC_SURROUNDING_MATCH = 1;
    private static final SoftCache<ULocale, CacheData, Void> cachedLocaleData = new SoftCache<ULocale, CacheData, Void>() {
        protected CacheData createInstance(ULocale key, Void unused) {
            return DecimalFormatSymbols.loadSymbols(key);
        }
    };
    private static final int currentSerialVersion = 8;
    private static final long serialVersionUID = 5772796243397350300L;
    private String NaN;
    private ULocale actualLocale;
    private transient Currency currency;
    private String currencyPattern = null;
    private String[] currencySpcAfterSym;
    private String[] currencySpcBeforeSym;
    private String currencySymbol;
    private char decimalSeparator;
    private char digit;
    private char[] digits;
    private String exponentMultiplicationSign = null;
    private String exponentSeparator;
    private char exponential;
    private char groupingSeparator;
    private String infinity;
    private String intlCurrencySymbol;
    private char minusSign;
    private String minusString = null;
    private char monetaryGroupingSeparator;
    private char monetarySeparator;
    private char padEscape;
    private char patternSeparator;
    private char perMill;
    private char percent;
    private char plusSign;
    private String plusString = null;
    private Locale requestedLocale;
    private int serialVersionOnStream = 8;
    private char sigDigit;
    private ULocale ulocale;
    private ULocale validLocale;
    private char zeroDigit;

    private static class CacheData {
        public final char[] digits;
        public final String[] symbolsArray;

        public CacheData(char[] digits, String[] symbolsArray) {
            this.digits = digits;
            this.symbolsArray = symbolsArray;
        }
    }

    public DecimalFormatSymbols() {
        initialize(ULocale.getDefault(Category.FORMAT));
    }

    public DecimalFormatSymbols(Locale locale) {
        initialize(ULocale.forLocale(locale));
    }

    public DecimalFormatSymbols(ULocale locale) {
        initialize(locale);
    }

    public static DecimalFormatSymbols getInstance() {
        return new DecimalFormatSymbols();
    }

    public static DecimalFormatSymbols getInstance(Locale locale) {
        return new DecimalFormatSymbols(locale);
    }

    public static DecimalFormatSymbols getInstance(ULocale locale) {
        return new DecimalFormatSymbols(locale);
    }

    public static Locale[] getAvailableLocales() {
        return ICUResourceBundle.getAvailableLocales();
    }

    public static ULocale[] getAvailableULocales() {
        return ICUResourceBundle.getAvailableULocales();
    }

    public char getZeroDigit() {
        if (this.digits != null) {
            return this.digits[0];
        }
        return this.zeroDigit;
    }

    public char[] getDigits() {
        if (this.digits != null) {
            return (char[]) this.digits.clone();
        }
        char[] digitArray = new char[10];
        for (int i = 0; i < 10; i++) {
            digitArray[i] = (char) (this.zeroDigit + i);
        }
        return digitArray;
    }

    char[] getDigitsLocal() {
        if (this.digits != null) {
            return this.digits;
        }
        char[] digitArray = new char[10];
        for (int i = 0; i < 10; i++) {
            digitArray[i] = (char) (this.zeroDigit + i);
        }
        return digitArray;
    }

    public void setZeroDigit(char zeroDigit) {
        if (this.digits != null) {
            this.digits[0] = zeroDigit;
            for (int i = 1; i < 10; i++) {
                this.digits[i] = (char) (zeroDigit + i);
            }
            return;
        }
        this.zeroDigit = zeroDigit;
    }

    public char getSignificantDigit() {
        return this.sigDigit;
    }

    public void setSignificantDigit(char sigDigit) {
        this.sigDigit = sigDigit;
    }

    public char getGroupingSeparator() {
        return this.groupingSeparator;
    }

    public void setGroupingSeparator(char groupingSeparator) {
        this.groupingSeparator = groupingSeparator;
    }

    public char getDecimalSeparator() {
        return this.decimalSeparator;
    }

    public void setDecimalSeparator(char decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    public char getPerMill() {
        return this.perMill;
    }

    public void setPerMill(char perMill) {
        this.perMill = perMill;
    }

    public char getPercent() {
        return this.percent;
    }

    public void setPercent(char percent) {
        this.percent = percent;
    }

    public char getDigit() {
        return this.digit;
    }

    public void setDigit(char digit) {
        this.digit = digit;
    }

    public char getPatternSeparator() {
        return this.patternSeparator;
    }

    public void setPatternSeparator(char patternSeparator) {
        this.patternSeparator = patternSeparator;
    }

    public String getInfinity() {
        return this.infinity;
    }

    public void setInfinity(String infinity) {
        this.infinity = infinity;
    }

    public String getNaN() {
        return this.NaN;
    }

    public void setNaN(String NaN) {
        this.NaN = NaN;
    }

    public char getMinusSign() {
        return this.minusSign;
    }

    @Deprecated
    public String getMinusString() {
        return this.minusString;
    }

    public void setMinusSign(char minusSign) {
        this.minusSign = minusSign;
        this.minusString = new String(new char[]{minusSign});
    }

    public String getCurrencySymbol() {
        return this.currencySymbol;
    }

    public void setCurrencySymbol(String currency) {
        this.currencySymbol = currency;
    }

    public String getInternationalCurrencySymbol() {
        return this.intlCurrencySymbol;
    }

    public void setInternationalCurrencySymbol(String currency) {
        this.intlCurrencySymbol = currency;
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public void setCurrency(Currency currency) {
        if (currency == null) {
            throw new NullPointerException();
        }
        this.currency = currency;
        this.intlCurrencySymbol = currency.getCurrencyCode();
        this.currencySymbol = currency.getSymbol(this.requestedLocale);
    }

    public char getMonetaryDecimalSeparator() {
        return this.monetarySeparator;
    }

    public char getMonetaryGroupingSeparator() {
        return this.monetaryGroupingSeparator;
    }

    String getCurrencyPattern() {
        return this.currencyPattern;
    }

    public void setMonetaryDecimalSeparator(char sep) {
        this.monetarySeparator = sep;
    }

    public void setMonetaryGroupingSeparator(char sep) {
        this.monetaryGroupingSeparator = sep;
    }

    public String getExponentMultiplicationSign() {
        return this.exponentMultiplicationSign;
    }

    public void setExponentMultiplicationSign(String exponentMultiplicationSign) {
        this.exponentMultiplicationSign = exponentMultiplicationSign;
    }

    public String getExponentSeparator() {
        return this.exponentSeparator;
    }

    public void setExponentSeparator(String exp) {
        this.exponentSeparator = exp;
    }

    public char getPlusSign() {
        return this.plusSign;
    }

    @Deprecated
    public String getPlusString() {
        return this.plusString;
    }

    public void setPlusSign(char plus) {
        this.plusSign = plus;
        this.plusString = new String(new char[]{this.plusSign});
    }

    public char getPadEscape() {
        return this.padEscape;
    }

    public void setPadEscape(char c) {
        this.padEscape = c;
    }

    public String getPatternForCurrencySpacing(int itemType, boolean beforeCurrency) {
        if (itemType < 0 || itemType > 2) {
            throw new IllegalArgumentException("unknown currency spacing: " + itemType);
        } else if (beforeCurrency) {
            return this.currencySpcBeforeSym[itemType];
        } else {
            return this.currencySpcAfterSym[itemType];
        }
    }

    public void setPatternForCurrencySpacing(int itemType, boolean beforeCurrency, String pattern) {
        if (itemType < 0 || itemType > 2) {
            throw new IllegalArgumentException("unknown currency spacing: " + itemType);
        } else if (beforeCurrency) {
            this.currencySpcBeforeSym[itemType] = pattern;
        } else {
            this.currencySpcAfterSym[itemType] = pattern;
        }
    }

    public Locale getLocale() {
        return this.requestedLocale;
    }

    public ULocale getULocale() {
        return this.ulocale;
    }

    public Object clone() {
        try {
            return (DecimalFormatSymbols) super.clone();
        } catch (Throwable e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof DecimalFormatSymbols)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        DecimalFormatSymbols other = (DecimalFormatSymbols) obj;
        int i = 0;
        while (i <= 2) {
            if (!this.currencySpcBeforeSym[i].equals(other.currencySpcBeforeSym[i]) || !this.currencySpcAfterSym[i].equals(other.currencySpcAfterSym[i])) {
                return false;
            }
            i++;
        }
        if (other.digits == null) {
            for (i = 0; i < 10; i++) {
                if (this.digits[i] != other.zeroDigit + i) {
                    return false;
                }
            }
        } else if (!Arrays.equals(this.digits, other.digits)) {
            return false;
        }
        if (this.groupingSeparator == other.groupingSeparator && this.decimalSeparator == other.decimalSeparator && this.percent == other.percent && this.perMill == other.perMill && this.digit == other.digit && this.minusSign == other.minusSign && this.minusString.equals(other.minusString) && this.patternSeparator == other.patternSeparator && this.infinity.equals(other.infinity) && this.NaN.equals(other.NaN) && this.currencySymbol.equals(other.currencySymbol) && this.intlCurrencySymbol.equals(other.intlCurrencySymbol) && this.padEscape == other.padEscape && this.plusSign == other.plusSign && this.plusString.equals(other.plusString) && this.exponentSeparator.equals(other.exponentSeparator) && this.monetarySeparator == other.monetarySeparator && this.monetaryGroupingSeparator == other.monetaryGroupingSeparator) {
            z = this.exponentMultiplicationSign.equals(other.exponentMultiplicationSign);
        }
        return z;
    }

    public int hashCode() {
        return (((this.digits[0] * 37) + this.groupingSeparator) * 37) + this.decimalSeparator;
    }

    private static boolean isBidiMark(char c) {
        return c == '‎' || c == '‏' || c == '؜';
    }

    private void initialize(ULocale locale) {
        this.requestedLocale = locale.toLocale();
        this.ulocale = locale;
        CacheData symbolData = (CacheData) cachedLocaleData.getInstance(locale, null);
        this.digits = (char[]) symbolData.digits.clone();
        String[] numberElements = symbolData.symbolsArray;
        ULocale uloc = ((ICUResourceBundle) UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", locale)).getULocale();
        setLocale(uloc, uloc);
        this.decimalSeparator = numberElements[0].charAt(0);
        this.groupingSeparator = numberElements[1].charAt(0);
        this.patternSeparator = numberElements[2].charAt(0);
        this.percent = numberElements[3].charAt(numberElements[3].length() - 1);
        this.minusString = numberElements[4];
        char charAt = (this.minusString.length() <= 1 || !isBidiMark(this.minusString.charAt(0))) ? this.minusString.charAt(0) : this.minusString.charAt(1);
        this.minusSign = charAt;
        this.plusString = numberElements[5];
        charAt = (this.plusString.length() <= 1 || !isBidiMark(this.plusString.charAt(0))) ? this.plusString.charAt(0) : this.plusString.charAt(1);
        this.plusSign = charAt;
        this.exponentSeparator = numberElements[6];
        this.perMill = numberElements[7].charAt(0);
        this.infinity = numberElements[8];
        this.NaN = numberElements[9];
        if (numberElements[10] != null) {
            this.monetarySeparator = numberElements[10].charAt(0);
        } else {
            this.monetarySeparator = this.decimalSeparator;
        }
        if (numberElements[11] != null) {
            this.monetaryGroupingSeparator = numberElements[11].charAt(0);
        } else {
            this.monetaryGroupingSeparator = this.groupingSeparator;
        }
        if (numberElements[12] != null) {
            this.exponentMultiplicationSign = numberElements[12];
        } else {
            this.exponentMultiplicationSign = "×";
        }
        this.digit = '#';
        this.padEscape = '*';
        this.sigDigit = '@';
        CurrencyDisplayInfo info = CurrencyData.provider.getInstance(locale, true);
        this.currency = Currency.getInstance(locale);
        if (this.currency != null) {
            String format;
            this.intlCurrencySymbol = this.currency.getCurrencyCode();
            boolean[] isChoiceFormat = new boolean[1];
            String currname = this.currency.getName(locale, 0, isChoiceFormat);
            if (isChoiceFormat[0]) {
                format = new ChoiceFormat(currname).format(2.0d);
            } else {
                format = currname;
            }
            this.currencySymbol = format;
            CurrencyFormatInfo fmtInfo = info.getFormatInfo(this.intlCurrencySymbol);
            if (fmtInfo != null) {
                this.currencyPattern = fmtInfo.currencyPattern;
                this.monetarySeparator = fmtInfo.monetarySeparator;
                this.monetaryGroupingSeparator = fmtInfo.monetaryGroupingSeparator;
            }
        } else {
            this.intlCurrencySymbol = "XXX";
            this.currencySymbol = "¤";
        }
        this.currencySpcBeforeSym = new String[3];
        this.currencySpcAfterSym = new String[3];
        initSpacingInfo(info.getSpacingInfo());
    }

    static CacheData loadSymbols(ULocale locale) {
        String nsName;
        NumberingSystem ns = NumberingSystem.getInstance(locale);
        char[] digits = new char[10];
        if (ns == null || ns.getRadix() != 10 || ns.isAlgorithmic() || !NumberingSystem.isValidDigitString(ns.getDescription())) {
            digits[0] = '0';
            digits[1] = '1';
            digits[2] = '2';
            digits[3] = '3';
            digits[4] = '4';
            digits[5] = '5';
            digits[6] = '6';
            digits[7] = '7';
            digits[8] = '8';
            digits[9] = '9';
            nsName = "latn";
        } else {
            String digitString = ns.getDescription();
            digits[0] = digitString.charAt(0);
            digits[1] = digitString.charAt(1);
            digits[2] = digitString.charAt(2);
            digits[3] = digitString.charAt(3);
            digits[4] = digitString.charAt(4);
            digits[5] = digitString.charAt(5);
            digits[6] = digitString.charAt(6);
            digits[7] = digitString.charAt(7);
            digits[8] = digitString.charAt(8);
            digits[9] = digitString.charAt(9);
            nsName = ns.getName();
        }
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", locale);
        boolean isLatn = nsName.equals("latn");
        String baseKey = "NumberElements/" + nsName + "/symbols/";
        String latnKey = "NumberElements/latn/symbols/";
        String[] symbolKeys = new String[]{"decimal", "group", "list", "percentSign", "minusSign", "plusSign", "exponential", "perMille", "infinity", "nan", "currencyDecimal", "currencyGroup", "superscriptingExponent"};
        String[] fallbackElements = new String[]{".", ",", ";", "%", LanguageTag.SEP, "+", DateFormat.ABBR_WEEKDAY, "‰", "∞", "NaN", null, null};
        String[] symbolsArray = new String[symbolKeys.length];
        for (int i = 0; i < symbolKeys.length; i++) {
            try {
                symbolsArray[i] = rb.getStringWithFallback(baseKey + symbolKeys[i]);
            } catch (MissingResourceException e) {
                if (isLatn) {
                    symbolsArray[i] = fallbackElements[i];
                } else {
                    try {
                        symbolsArray[i] = rb.getStringWithFallback(latnKey + symbolKeys[i]);
                    } catch (MissingResourceException e2) {
                        symbolsArray[i] = fallbackElements[i];
                    }
                }
            }
        }
        return new CacheData(digits, symbolsArray);
    }

    private void initSpacingInfo(CurrencySpacingInfo spcInfo) {
        this.currencySpcBeforeSym[0] = spcInfo.beforeCurrencyMatch;
        this.currencySpcBeforeSym[1] = spcInfo.beforeContextMatch;
        this.currencySpcBeforeSym[2] = spcInfo.beforeInsert;
        this.currencySpcAfterSym[0] = spcInfo.afterCurrencyMatch;
        this.currencySpcAfterSym[1] = spcInfo.afterContextMatch;
        this.currencySpcAfterSym[2] = spcInfo.afterInsert;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (this.serialVersionOnStream < 1) {
            this.monetarySeparator = this.decimalSeparator;
            this.exponential = 'E';
        }
        if (this.serialVersionOnStream < 2) {
            this.padEscape = '*';
            this.plusSign = '+';
            this.exponentSeparator = String.valueOf(this.exponential);
        }
        if (this.serialVersionOnStream < 3) {
            this.requestedLocale = Locale.getDefault();
        }
        if (this.serialVersionOnStream < 4) {
            this.ulocale = ULocale.forLocale(this.requestedLocale);
        }
        if (this.serialVersionOnStream < 5) {
            this.monetaryGroupingSeparator = this.groupingSeparator;
        }
        if (this.serialVersionOnStream < 6) {
            if (this.currencySpcBeforeSym == null) {
                this.currencySpcBeforeSym = new String[3];
            }
            if (this.currencySpcAfterSym == null) {
                this.currencySpcAfterSym = new String[3];
            }
            initSpacingInfo(CurrencySpacingInfo.DEFAULT);
        }
        if (this.serialVersionOnStream < 7) {
            if (this.minusString == null) {
                this.minusString = new String(new char[]{this.minusSign});
            }
            if (this.plusString == null) {
                this.plusString = new String(new char[]{this.plusSign});
            }
        }
        if (this.serialVersionOnStream < 8 && this.exponentMultiplicationSign == null) {
            this.exponentMultiplicationSign = "×";
        }
        this.serialVersionOnStream = 8;
        this.currency = Currency.getInstance(this.intlCurrencySymbol);
    }

    public final ULocale getLocale(Type type) {
        return type == ULocale.ACTUAL_LOCALE ? this.actualLocale : this.validLocale;
    }

    final void setLocale(ULocale valid, ULocale actual) {
        Object obj;
        Object obj2 = 1;
        if (valid == null) {
            obj = 1;
        } else {
            obj = null;
        }
        if (actual != null) {
            obj2 = null;
        }
        if (obj != obj2) {
            throw new IllegalArgumentException();
        }
        this.validLocale = valid;
        this.actualLocale = actual;
    }
}
