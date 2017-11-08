package android.icu.text;

import android.icu.impl.CalendarData;
import android.icu.impl.DontCareFieldPosition;
import android.icu.impl.ICUCache;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import android.icu.impl.SimplePatternFormatter;
import android.icu.impl.StandardPlural;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.TableSink;
import android.icu.impl.UResource.Value;
import android.icu.lang.UCharacter;
import android.icu.text.DisplayContext.Type;
import android.icu.util.ICUException;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.lang.reflect.Array;
import java.util.EnumMap;
import java.util.Locale;

public final class RelativeDateTimeFormatter {
    private static final Cache cache = new Cache();
    private static final Style[] fallbackCache = new Style[3];
    private final BreakIterator breakIterator;
    private final DisplayContext capitalizationContext;
    private final MessageFormat combinedDateAndTime;
    private final DateFormatSymbols dateFormatSymbols;
    private final ULocale locale;
    private final NumberFormat numberFormat;
    private final EnumMap<Style, EnumMap<RelativeUnit, String[][]>> patternMap;
    private final PluralRules pluralRules;
    private final EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap;
    private final Style style;
    private int[] styleToDateFormatSymbolsWidth = new int[]{1, 3, 2};

    public enum AbsoluteUnit {
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        DAY,
        WEEK,
        MONTH,
        YEAR,
        NOW,
        QUARTER
    }

    private static class Cache {
        private final ICUCache<String, RelativeDateTimeFormatterData> cache;

        private Cache() {
            this.cache = new SimpleCache();
        }

        public RelativeDateTimeFormatterData get(ULocale locale) {
            String key = locale.toString();
            RelativeDateTimeFormatterData result = (RelativeDateTimeFormatterData) this.cache.get(key);
            if (result != null) {
                return result;
            }
            result = new Loader(locale).load();
            this.cache.put(key, result);
            return result;
        }
    }

    public enum Direction {
        LAST_2,
        LAST,
        THIS,
        NEXT,
        NEXT_2,
        PLAIN
    }

    private static class Loader {
        private final ULocale ulocale;

        public Loader(ULocale ulocale) {
            this.ulocale = ulocale;
        }

        public RelativeDateTimeFormatterData load() {
            RelDateTimeFmtDataSink sink = new RelDateTimeFmtDataSink(this.ulocale);
            ICUResourceBundle r = (ICUResourceBundle) UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", this.ulocale);
            r.getAllTableItemsWithFallback("fields", sink);
            for (Style testStyle : Style.values()) {
                Style newStyle1 = RelativeDateTimeFormatter.fallbackCache[testStyle.ordinal()];
                if (newStyle1 != null) {
                    Style newStyle2 = RelativeDateTimeFormatter.fallbackCache[newStyle1.ordinal()];
                    if (!(newStyle2 == null || RelativeDateTimeFormatter.fallbackCache[newStyle2.ordinal()] == null)) {
                        throw new IllegalStateException("Style fallback too deep");
                    }
                }
            }
            return new RelativeDateTimeFormatterData(sink.qualitativeUnitMap, sink.styleRelUnitPatterns, new CalendarData(this.ulocale, r.getStringWithFallback("calendar/default")).getDateTimePattern());
        }
    }

    private static final class RelDateTimeFmtDataSink extends TableSink {
        private static final /* synthetic */ int[] -android-icu-text-RelativeDateTimeFormatter$StyleSwitchesValues = null;
        int pastFutureIndex;
        EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap = new EnumMap(Style.class);
        RelativeSink relativeSink = new RelativeSink();
        RelativeTimeDetailSink relativeTimeDetailSink = new RelativeTimeDetailSink();
        RelativeTimeSink relativeTimeSink = new RelativeTimeSink();
        StringBuilder sb = new StringBuilder();
        Style style;
        EnumMap<Style, EnumMap<RelativeUnit, String[][]>> styleRelUnitPatterns = new EnumMap(Style.class);
        private ULocale ulocale = null;
        DateTimeUnit unit;
        UnitSink unitSink = new UnitSink();

        private enum DateTimeUnit {
            SECOND(RelativeUnit.SECONDS, null),
            MINUTE(RelativeUnit.MINUTES, null),
            HOUR(RelativeUnit.HOURS, null),
            DAY(RelativeUnit.DAYS, AbsoluteUnit.DAY),
            WEEK(RelativeUnit.WEEKS, AbsoluteUnit.WEEK),
            MONTH(RelativeUnit.MONTHS, AbsoluteUnit.MONTH),
            QUARTER(RelativeUnit.QUARTERS, AbsoluteUnit.QUARTER),
            YEAR(RelativeUnit.YEARS, AbsoluteUnit.YEAR),
            SUNDAY(null, AbsoluteUnit.SUNDAY),
            MONDAY(null, AbsoluteUnit.MONDAY),
            TUESDAY(null, AbsoluteUnit.TUESDAY),
            WEDNESDAY(null, AbsoluteUnit.WEDNESDAY),
            THURSDAY(null, AbsoluteUnit.THURSDAY),
            FRIDAY(null, AbsoluteUnit.FRIDAY),
            SATURDAY(null, AbsoluteUnit.SATURDAY);
            
            AbsoluteUnit absUnit;
            RelativeUnit relUnit;

            private DateTimeUnit(RelativeUnit relUnit, AbsoluteUnit absUnit) {
                this.relUnit = relUnit;
                this.absUnit = absUnit;
            }

            private static final DateTimeUnit orNullFromString(CharSequence keyword) {
                switch (keyword.length()) {
                    case 3:
                        if ("day".contentEquals(keyword)) {
                            return DAY;
                        }
                        if ("sun".contentEquals(keyword)) {
                            return SUNDAY;
                        }
                        if ("mon".contentEquals(keyword)) {
                            return MONDAY;
                        }
                        if ("tue".contentEquals(keyword)) {
                            return TUESDAY;
                        }
                        if ("wed".contentEquals(keyword)) {
                            return WEDNESDAY;
                        }
                        if ("thu".contentEquals(keyword)) {
                            return THURSDAY;
                        }
                        if ("fri".contentEquals(keyword)) {
                            return FRIDAY;
                        }
                        if ("sat".contentEquals(keyword)) {
                            return SATURDAY;
                        }
                        break;
                    case 4:
                        if ("hour".contentEquals(keyword)) {
                            return HOUR;
                        }
                        if ("week".contentEquals(keyword)) {
                            return WEEK;
                        }
                        if ("year".contentEquals(keyword)) {
                            return YEAR;
                        }
                        break;
                    case 5:
                        if ("month".contentEquals(keyword)) {
                            return MONTH;
                        }
                        break;
                    case 6:
                        if ("minute".contentEquals(keyword)) {
                            return MINUTE;
                        }
                        if ("second".contentEquals(keyword)) {
                            return SECOND;
                        }
                        break;
                    case 7:
                        if ("quarter".contentEquals(keyword)) {
                            return QUARTER;
                        }
                        break;
                }
                return null;
            }
        }

        class RelativeSink extends TableSink {
            RelativeSink() {
            }

            public void put(Key key, Value value) {
                EnumMap<AbsoluteUnit, EnumMap<Direction, String>> absMap = (EnumMap) RelDateTimeFmtDataSink.this.qualitativeUnitMap.get(RelDateTimeFmtDataSink.this.style);
                if (RelDateTimeFmtDataSink.this.unit.relUnit == RelativeUnit.SECONDS && key.contentEquals(AndroidHardcodedSystemProperties.JAVA_VERSION)) {
                    EnumMap<Direction, String> unitStrings = (EnumMap) absMap.get(AbsoluteUnit.NOW);
                    if (unitStrings == null) {
                        unitStrings = new EnumMap(Direction.class);
                        absMap.put(AbsoluteUnit.NOW, unitStrings);
                    }
                    if (unitStrings.get(Direction.PLAIN) == null) {
                        unitStrings.put(Direction.PLAIN, value.getString());
                    }
                    return;
                }
                Direction keyDirection = RelativeDateTimeFormatter.keyToDirection(key);
                if (keyDirection != null) {
                    AbsoluteUnit absUnit = RelDateTimeFmtDataSink.this.unit.absUnit;
                    if (absUnit != null) {
                        if (absMap == null) {
                            absMap = new EnumMap(AbsoluteUnit.class);
                            RelDateTimeFmtDataSink.this.qualitativeUnitMap.put(RelDateTimeFmtDataSink.this.style, absMap);
                        }
                        EnumMap<Direction, String> dirMap = (EnumMap) absMap.get(absUnit);
                        if (dirMap == null) {
                            dirMap = new EnumMap(Direction.class);
                            absMap.put(absUnit, dirMap);
                        }
                        if (dirMap.get(keyDirection) == null) {
                            dirMap.put(keyDirection, value.getString());
                        }
                    }
                }
            }
        }

        class RelativeTimeDetailSink extends TableSink {
            RelativeTimeDetailSink() {
            }

            public void put(Key key, Value value) {
                EnumMap<RelativeUnit, String[][]> unitPatterns = (EnumMap) RelDateTimeFmtDataSink.this.styleRelUnitPatterns.get(RelDateTimeFmtDataSink.this.style);
                if (unitPatterns == null) {
                    unitPatterns = new EnumMap(RelativeUnit.class);
                    RelDateTimeFmtDataSink.this.styleRelUnitPatterns.put(RelDateTimeFmtDataSink.this.style, unitPatterns);
                }
                String[][] patterns = (String[][]) unitPatterns.get(RelDateTimeFmtDataSink.this.unit.relUnit);
                if (patterns == null) {
                    patterns = (String[][]) Array.newInstance(String.class, new int[]{2, StandardPlural.COUNT});
                    unitPatterns.put(RelDateTimeFmtDataSink.this.unit.relUnit, patterns);
                }
                int pluralIndex = StandardPlural.indexFromString(key.toString());
                if (patterns[RelDateTimeFmtDataSink.this.pastFutureIndex][pluralIndex] == null) {
                    patterns[RelDateTimeFmtDataSink.this.pastFutureIndex][pluralIndex] = SimplePatternFormatter.compileToStringMinMaxPlaceholders(value.getString(), RelDateTimeFmtDataSink.this.sb, 0, 1);
                }
            }
        }

        class RelativeTimeSink extends TableSink {
            RelativeTimeSink() {
            }

            public TableSink getOrCreateTableSink(Key key, int initialSize) {
                if (key.contentEquals("past")) {
                    RelDateTimeFmtDataSink.this.pastFutureIndex = 0;
                } else if (!key.contentEquals("future")) {
                    return null;
                } else {
                    RelDateTimeFmtDataSink.this.pastFutureIndex = 1;
                }
                if (RelDateTimeFmtDataSink.this.unit.relUnit == null) {
                    return null;
                }
                return RelDateTimeFmtDataSink.this.relativeTimeDetailSink;
            }
        }

        class UnitSink extends TableSink {
            UnitSink() {
            }

            public void put(Key key, Value value) {
                if (key.contentEquals("dn")) {
                    AbsoluteUnit absUnit = RelDateTimeFmtDataSink.this.unit.absUnit;
                    if (absUnit != null) {
                        EnumMap<AbsoluteUnit, EnumMap<Direction, String>> unitMap = (EnumMap) RelDateTimeFmtDataSink.this.qualitativeUnitMap.get(RelDateTimeFmtDataSink.this.style);
                        if (unitMap == null) {
                            unitMap = new EnumMap(AbsoluteUnit.class);
                            RelDateTimeFmtDataSink.this.qualitativeUnitMap.put(RelDateTimeFmtDataSink.this.style, unitMap);
                        }
                        EnumMap<Direction, String> dirMap = (EnumMap) unitMap.get(absUnit);
                        if (dirMap == null) {
                            dirMap = new EnumMap(Direction.class);
                            unitMap.put(absUnit, dirMap);
                        }
                        if (dirMap.get(Direction.PLAIN) == null) {
                            String displayName = value.toString();
                            if (RelDateTimeFmtDataSink.this.ulocale.getLanguage().equals("en")) {
                                displayName = displayName.toLowerCase(Locale.ROOT);
                            }
                            dirMap.put(Direction.PLAIN, displayName);
                        }
                    }
                }
            }

            public TableSink getOrCreateTableSink(Key key, int initialSize) {
                if (key.contentEquals("relative")) {
                    return RelDateTimeFmtDataSink.this.relativeSink;
                }
                if (key.contentEquals("relativeTime")) {
                    return RelDateTimeFmtDataSink.this.relativeTimeSink;
                }
                return null;
            }
        }

        private static /* synthetic */ int[] -getandroid-icu-text-RelativeDateTimeFormatter$StyleSwitchesValues() {
            if (-android-icu-text-RelativeDateTimeFormatter$StyleSwitchesValues != null) {
                return -android-icu-text-RelativeDateTimeFormatter$StyleSwitchesValues;
            }
            int[] iArr = new int[Style.values().length];
            try {
                iArr[Style.LONG.ordinal()] = 3;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Style.NARROW.ordinal()] = 1;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Style.SHORT.ordinal()] = 2;
            } catch (NoSuchFieldError e3) {
            }
            -android-icu-text-RelativeDateTimeFormatter$StyleSwitchesValues = iArr;
            return iArr;
        }

        public RelDateTimeFmtDataSink(ULocale locale) {
            this.ulocale = locale;
        }

        private Style styleFromKey(Key key) {
            if (key.endsWith("-short")) {
                return Style.SHORT;
            }
            if (key.endsWith("-narrow")) {
                return Style.NARROW;
            }
            return Style.LONG;
        }

        private Style styleFromAlias(Value value) {
            String s = value.getAliasString();
            if (s.endsWith("-short")) {
                return Style.SHORT;
            }
            if (s.endsWith("-narrow")) {
                return Style.NARROW;
            }
            return Style.LONG;
        }

        private static int styleSuffixLength(Style style) {
            switch (-getandroid-icu-text-RelativeDateTimeFormatter$StyleSwitchesValues()[style.ordinal()]) {
                case 1:
                    return 7;
                case 2:
                    return 6;
                default:
                    return 0;
            }
        }

        public void put(Key key, Value value) {
            if (value.getType() == 3) {
                Style sourceStyle = styleFromKey(key);
                if (DateTimeUnit.orNullFromString(key.substring(0, key.length() - styleSuffixLength(sourceStyle))) != null) {
                    Style targetStyle = styleFromAlias(value);
                    if (sourceStyle == targetStyle) {
                        throw new ICUException("Invalid style fallback from " + sourceStyle + " to itself");
                    } else if (RelativeDateTimeFormatter.fallbackCache[sourceStyle.ordinal()] == null) {
                        RelativeDateTimeFormatter.fallbackCache[sourceStyle.ordinal()] = targetStyle;
                    } else if (RelativeDateTimeFormatter.fallbackCache[sourceStyle.ordinal()] != targetStyle) {
                        throw new ICUException("Inconsistent style fallback for style " + sourceStyle + " to " + targetStyle);
                    }
                }
            }
        }

        public TableSink getOrCreateTableSink(Key key, int initialSize) {
            this.style = styleFromKey(key);
            this.unit = DateTimeUnit.orNullFromString(key.substring(0, key.length() - styleSuffixLength(this.style)));
            if (this.unit == null) {
                return null;
            }
            return this.unitSink;
        }
    }

    private static class RelativeDateTimeFormatterData {
        public final String dateTimePattern;
        public final EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap;
        EnumMap<Style, EnumMap<RelativeUnit, String[][]>> relUnitPatternMap;

        public RelativeDateTimeFormatterData(EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap, EnumMap<Style, EnumMap<RelativeUnit, String[][]>> relUnitPatternMap, String dateTimePattern) {
            this.qualitativeUnitMap = qualitativeUnitMap;
            this.relUnitPatternMap = relUnitPatternMap;
            this.dateTimePattern = dateTimePattern;
        }
    }

    public enum RelativeUnit {
        SECONDS,
        MINUTES,
        HOURS,
        DAYS,
        WEEKS,
        MONTHS,
        YEARS,
        QUARTERS
    }

    public enum Style {
        LONG,
        SHORT,
        NARROW;
        
        private static final int INDEX_COUNT = 3;
    }

    public static RelativeDateTimeFormatter getInstance() {
        return getInstance(ULocale.getDefault(), null, Style.LONG, DisplayContext.CAPITALIZATION_NONE);
    }

    public static RelativeDateTimeFormatter getInstance(ULocale locale) {
        return getInstance(locale, null, Style.LONG, DisplayContext.CAPITALIZATION_NONE);
    }

    public static RelativeDateTimeFormatter getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    public static RelativeDateTimeFormatter getInstance(ULocale locale, NumberFormat nf) {
        return getInstance(locale, nf, Style.LONG, DisplayContext.CAPITALIZATION_NONE);
    }

    public static RelativeDateTimeFormatter getInstance(ULocale locale, NumberFormat nf, Style style, DisplayContext capitalizationContext) {
        BreakIterator breakIterator = null;
        RelativeDateTimeFormatterData data = cache.get(locale);
        if (nf == null) {
            nf = NumberFormat.getInstance(locale);
        } else {
            nf = (NumberFormat) nf.clone();
        }
        EnumMap enumMap = data.qualitativeUnitMap;
        EnumMap enumMap2 = data.relUnitPatternMap;
        MessageFormat messageFormat = new MessageFormat(data.dateTimePattern);
        PluralRules forLocale = PluralRules.forLocale(locale);
        if (capitalizationContext == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE) {
            breakIterator = BreakIterator.getSentenceInstance(locale);
        }
        return new RelativeDateTimeFormatter(enumMap, enumMap2, messageFormat, forLocale, nf, style, capitalizationContext, breakIterator, locale);
    }

    public static RelativeDateTimeFormatter getInstance(Locale locale, NumberFormat nf) {
        return getInstance(ULocale.forLocale(locale), nf);
    }

    public String format(double quantity, Direction direction, RelativeUnit unit) {
        if (direction == Direction.LAST || direction == Direction.NEXT) {
            String result;
            int pastFutureIndex = direction == Direction.NEXT ? 1 : 0;
            synchronized (this.numberFormat) {
                result = SimplePatternFormatter.formatCompiledPattern(getRelativeUnitPluralPattern(this.style, unit, pastFutureIndex, QuantityFormatter.selectPlural(Double.valueOf(quantity), this.numberFormat, this.pluralRules, new StringBuffer(), DontCareFieldPosition.INSTANCE)), new StringBuffer());
            }
            return adjustForContext(result);
        }
        throw new IllegalArgumentException("direction must be NEXT or LAST");
    }

    public String format(Direction direction, AbsoluteUnit unit) {
        if (unit != AbsoluteUnit.NOW || direction == Direction.PLAIN) {
            String result;
            if (direction != Direction.PLAIN || AbsoluteUnit.SUNDAY.ordinal() > unit.ordinal() || unit.ordinal() > AbsoluteUnit.SATURDAY.ordinal()) {
                result = getAbsoluteUnitString(this.style, unit, direction);
            } else {
                result = this.dateFormatSymbols.getWeekdays(1, this.styleToDateFormatSymbolsWidth[this.style.ordinal()])[(unit.ordinal() - AbsoluteUnit.SUNDAY.ordinal()) + 1];
            }
            if (result != null) {
                return adjustForContext(result);
            }
            return null;
        }
        throw new IllegalArgumentException("NOW can only accept direction PLAIN.");
    }

    private String getAbsoluteUnitString(Style style, AbsoluteUnit unit, Direction direction) {
        do {
            EnumMap<AbsoluteUnit, EnumMap<Direction, String>> unitMap = (EnumMap) this.qualitativeUnitMap.get(style);
            if (unitMap != null) {
                EnumMap<Direction, String> dirMap = (EnumMap) unitMap.get(unit);
                if (dirMap != null) {
                    String result = (String) dirMap.get(direction);
                    if (result != null) {
                        return result;
                    }
                }
            }
            style = fallbackCache[style.ordinal()];
        } while (style != null);
        return null;
    }

    public String combineDateAndTime(String relativeDateString, String timeString) {
        return this.combinedDateAndTime.format(new Object[]{timeString, relativeDateString}, new StringBuffer(), null).toString();
    }

    public NumberFormat getNumberFormat() {
        NumberFormat numberFormat;
        synchronized (this.numberFormat) {
            numberFormat = (NumberFormat) this.numberFormat.clone();
        }
        return numberFormat;
    }

    public DisplayContext getCapitalizationContext() {
        return this.capitalizationContext;
    }

    public Style getFormatStyle() {
        return this.style;
    }

    private String adjustForContext(String originalFormattedString) {
        if (this.breakIterator == null || originalFormattedString.length() == 0 || !UCharacter.isLowerCase(UCharacter.codePointAt((CharSequence) originalFormattedString, 0))) {
            return originalFormattedString;
        }
        String toTitleCase;
        synchronized (this.breakIterator) {
            toTitleCase = UCharacter.toTitleCase(this.locale, originalFormattedString, this.breakIterator, 768);
        }
        return toTitleCase;
    }

    private RelativeDateTimeFormatter(EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap, EnumMap<Style, EnumMap<RelativeUnit, String[][]>> patternMap, MessageFormat combinedDateAndTime, PluralRules pluralRules, NumberFormat numberFormat, Style style, DisplayContext capitalizationContext, BreakIterator breakIterator, ULocale locale) {
        this.qualitativeUnitMap = qualitativeUnitMap;
        this.patternMap = patternMap;
        this.combinedDateAndTime = combinedDateAndTime;
        this.pluralRules = pluralRules;
        this.numberFormat = numberFormat;
        this.style = style;
        if (capitalizationContext.type() != Type.CAPITALIZATION) {
            throw new IllegalArgumentException(capitalizationContext.toString());
        }
        this.capitalizationContext = capitalizationContext;
        this.breakIterator = breakIterator;
        this.locale = locale;
        this.dateFormatSymbols = new DateFormatSymbols(locale);
    }

    private String getRelativeUnitPluralPattern(Style style, RelativeUnit unit, int pastFutureIndex, StandardPlural pluralForm) {
        if (pluralForm != StandardPlural.OTHER) {
            String formatter = getRelativeUnitPattern(style, unit, pastFutureIndex, pluralForm);
            if (formatter != null) {
                return formatter;
            }
        }
        return getRelativeUnitPattern(style, unit, pastFutureIndex, StandardPlural.OTHER);
    }

    private String getRelativeUnitPattern(Style style, RelativeUnit unit, int pastFutureIndex, StandardPlural pluralForm) {
        int pluralIndex = pluralForm.ordinal();
        do {
            EnumMap<RelativeUnit, String[][]> unitMap = (EnumMap) this.patternMap.get(style);
            if (unitMap != null) {
                String[][] spfCompiledPatterns = (String[][]) unitMap.get(unit);
                if (!(spfCompiledPatterns == null || spfCompiledPatterns[pastFutureIndex][pluralIndex] == null)) {
                    return spfCompiledPatterns[pastFutureIndex][pluralIndex];
                }
            }
            style = fallbackCache[style.ordinal()];
        } while (style != null);
        return null;
    }

    private static Direction keyToDirection(Key key) {
        if (key.contentEquals("-2")) {
            return Direction.LAST_2;
        }
        if (key.contentEquals("-1")) {
            return Direction.LAST;
        }
        if (key.contentEquals(AndroidHardcodedSystemProperties.JAVA_VERSION)) {
            return Direction.THIS;
        }
        if (key.contentEquals("1")) {
            return Direction.NEXT;
        }
        if (key.contentEquals("2")) {
            return Direction.NEXT_2;
        }
        return null;
    }
}
