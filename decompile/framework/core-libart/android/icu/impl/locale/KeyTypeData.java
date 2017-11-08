package android.icu.impl.locale;

import android.icu.impl.ICUResourceBundle;
import android.icu.util.Output;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.regex.Pattern;

public class KeyTypeData {
    static final /* synthetic */ boolean -assertionsDisabled;
    private static final Map<String, KeyData> KEYMAP = new HashMap();
    private static final Object[][] KEY_DATA = new Object[0][];

    private static abstract class SpecialTypeHandler {
        abstract boolean isValid(String str);

        private SpecialTypeHandler() {
        }

        String canonicalize(String value) {
            return AsciiUtil.toLowerString(value);
        }
    }

    private static class CodepointsTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("[0-9a-fA-F]{4,6}(-[0-9a-fA-F]{4,6})*");

        private CodepointsTypeHandler() {
            super();
        }

        boolean isValid(String value) {
            return pat.matcher(value).matches();
        }
    }

    private static class KeyData {
        String bcpId;
        String legacyId;
        EnumSet<SpecialType> specialTypes;
        Map<String, Type> typeMap;

        KeyData(String legacyId, String bcpId, Map<String, Type> typeMap, EnumSet<SpecialType> specialTypes) {
            this.legacyId = legacyId;
            this.bcpId = bcpId;
            this.typeMap = typeMap;
            this.specialTypes = specialTypes;
        }
    }

    private static class ReorderCodeTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("[a-zA-Z]{3,8}(-[a-zA-Z]{3,8})*");

        private ReorderCodeTypeHandler() {
            super();
        }

        boolean isValid(String value) {
            return pat.matcher(value).matches();
        }
    }

    private enum SpecialType {
        CODEPOINTS(new CodepointsTypeHandler()),
        REORDER_CODE(new ReorderCodeTypeHandler());
        
        SpecialTypeHandler handler;

        private SpecialType(SpecialTypeHandler handler) {
            this.handler = handler;
        }
    }

    private static class Type {
        String bcpId;
        String legacyId;

        Type(String legacyId, String bcpId) {
            this.legacyId = legacyId;
            this.bcpId = bcpId;
        }
    }

    public static String toBcpKey(String key) {
        KeyData keyData = (KeyData) KEYMAP.get(AsciiUtil.toLowerString(key));
        if (keyData != null) {
            return keyData.bcpId;
        }
        return null;
    }

    public static String toLegacyKey(String key) {
        KeyData keyData = (KeyData) KEYMAP.get(AsciiUtil.toLowerString(key));
        if (keyData != null) {
            return keyData.legacyId;
        }
        return null;
    }

    public static String toBcpType(String key, String type, Output<Boolean> isKnownKey, Output<Boolean> isSpecialType) {
        if (isKnownKey != null) {
            isKnownKey.value = Boolean.valueOf(false);
        }
        if (isSpecialType != null) {
            isSpecialType.value = Boolean.valueOf(false);
        }
        key = AsciiUtil.toLowerString(key);
        type = AsciiUtil.toLowerString(type);
        KeyData keyData = (KeyData) KEYMAP.get(key);
        if (keyData != null) {
            if (isKnownKey != null) {
                isKnownKey.value = Boolean.TRUE;
            }
            Type t = (Type) keyData.typeMap.get(type);
            if (t != null) {
                return t.bcpId;
            }
            if (keyData.specialTypes != null) {
                for (SpecialType st : keyData.specialTypes) {
                    if (st.handler.isValid(type)) {
                        if (isSpecialType != null) {
                            isSpecialType.value = Boolean.valueOf(true);
                        }
                        return st.handler.canonicalize(type);
                    }
                }
            }
        }
        return null;
    }

    public static String toLegacyType(String key, String type, Output<Boolean> isKnownKey, Output<Boolean> isSpecialType) {
        if (isKnownKey != null) {
            isKnownKey.value = Boolean.valueOf(false);
        }
        if (isSpecialType != null) {
            isSpecialType.value = Boolean.valueOf(false);
        }
        key = AsciiUtil.toLowerString(key);
        type = AsciiUtil.toLowerString(type);
        KeyData keyData = (KeyData) KEYMAP.get(key);
        if (keyData != null) {
            if (isKnownKey != null) {
                isKnownKey.value = Boolean.TRUE;
            }
            Type t = (Type) keyData.typeMap.get(type);
            if (t != null) {
                return t.legacyId;
            }
            if (keyData.specialTypes != null) {
                for (SpecialType st : keyData.specialTypes) {
                    if (st.handler.isValid(type)) {
                        if (isSpecialType != null) {
                            isSpecialType.value = Boolean.valueOf(true);
                        }
                        return st.handler.canonicalize(type);
                    }
                }
            }
        }
        return null;
    }

    private static void initFromResourceBundle() {
        UResourceBundle keyTypeDataRes = UResourceBundle.getBundleInstance("android/icu/impl/data/icudt56b", "keyTypeData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        UResourceBundle keyMapRes = keyTypeDataRes.get("keyMap");
        UResourceBundle typeMapRes = keyTypeDataRes.get("typeMap");
        UResourceBundle typeAliasRes = null;
        UResourceBundle bcpTypeAliasRes = null;
        try {
            typeAliasRes = keyTypeDataRes.get("typeAlias");
        } catch (MissingResourceException e) {
        }
        try {
            bcpTypeAliasRes = keyTypeDataRes.get("bcpTypeAlias");
        } catch (MissingResourceException e2) {
        }
        UResourceBundleIterator keyMapItr = keyMapRes.getIterator();
        while (keyMapItr.hasNext()) {
            String from;
            String to;
            Set<String> aliasSet;
            UResourceBundle keyMapEntry = keyMapItr.next();
            String legacyKeyId = keyMapEntry.getKey();
            String bcpKeyId = keyMapEntry.getString();
            boolean hasSameKey = false;
            if (bcpKeyId.length() == 0) {
                bcpKeyId = legacyKeyId;
                hasSameKey = true;
            }
            boolean isTZ = legacyKeyId.equals("timezone");
            Map<String, Set<String>> map = null;
            if (typeAliasRes != null) {
                UResourceBundle typeAliasResByKey = null;
                try {
                    typeAliasResByKey = typeAliasRes.get(legacyKeyId);
                } catch (MissingResourceException e3) {
                }
                if (typeAliasResByKey != null) {
                    map = new HashMap();
                    UResourceBundleIterator typeAliasResItr = typeAliasResByKey.getIterator();
                    while (typeAliasResItr.hasNext()) {
                        UResourceBundle typeAliasDataEntry = typeAliasResItr.next();
                        from = typeAliasDataEntry.getKey();
                        to = typeAliasDataEntry.getString();
                        if (isTZ) {
                            from = from.replace(':', '/');
                        }
                        aliasSet = (Set) map.get(to);
                        if (aliasSet == null) {
                            aliasSet = new HashSet();
                            map.put(to, aliasSet);
                        }
                        aliasSet.add(from);
                    }
                }
            }
            Map map2 = null;
            if (bcpTypeAliasRes != null) {
                UResourceBundle uResourceBundle = null;
                try {
                    uResourceBundle = bcpTypeAliasRes.get(bcpKeyId);
                } catch (MissingResourceException e4) {
                }
                if (uResourceBundle != null) {
                    map2 = new HashMap();
                    UResourceBundleIterator bcpTypeAliasResItr = uResourceBundle.getIterator();
                    while (bcpTypeAliasResItr.hasNext()) {
                        UResourceBundle bcpTypeAliasDataEntry = bcpTypeAliasResItr.next();
                        from = bcpTypeAliasDataEntry.getKey();
                        to = bcpTypeAliasDataEntry.getString();
                        aliasSet = (Set) map2.get(to);
                        if (aliasSet == null) {
                            aliasSet = new HashSet();
                            map2.put(to, aliasSet);
                        }
                        aliasSet.add(from);
                    }
                }
            }
            Map<String, Type> typeDataMap = new HashMap();
            Collection collection = null;
            UResourceBundle typeMapResByKey = null;
            try {
                typeMapResByKey = typeMapRes.get(legacyKeyId);
            } catch (MissingResourceException e5) {
                if (!-assertionsDisabled) {
                    throw new AssertionError();
                }
            }
            if (typeMapResByKey != null) {
                UResourceBundleIterator typeMapResByKeyItr = typeMapResByKey.getIterator();
                while (typeMapResByKeyItr.hasNext()) {
                    String bcpTypeId;
                    boolean hasSameType;
                    Type type;
                    Set<String> typeAliasSet;
                    Set<String> bcpTypeAliasSet;
                    UResourceBundle typeMapEntry = typeMapResByKeyItr.next();
                    String legacyTypeId = typeMapEntry.getKey();
                    boolean isSpecialType = false;
                    SpecialType[] values = SpecialType.values();
                    int i = 0;
                    int length = values.length;
                    while (i < length) {
                        SpecialType st = values[i];
                        if (legacyTypeId.equals(st.toString())) {
                            isSpecialType = true;
                            if (collection == null) {
                                collection = new HashSet();
                            }
                            collection.add(st);
                            if (isSpecialType) {
                                if (isTZ) {
                                    legacyTypeId = legacyTypeId.replace(':', '/');
                                }
                                bcpTypeId = typeMapEntry.getString();
                                hasSameType = false;
                                if (bcpTypeId.length() == 0) {
                                    bcpTypeId = legacyTypeId;
                                    hasSameType = true;
                                }
                                type = new Type(legacyTypeId, bcpTypeId);
                                typeDataMap.put(AsciiUtil.toLowerString(legacyTypeId), type);
                                if (!hasSameType) {
                                    typeDataMap.put(AsciiUtil.toLowerString(bcpTypeId), type);
                                }
                                if (map != null) {
                                    typeAliasSet = (Set) map.get(legacyTypeId);
                                    if (typeAliasSet != null) {
                                        for (String alias : typeAliasSet) {
                                            typeDataMap.put(AsciiUtil.toLowerString(alias), type);
                                        }
                                    }
                                }
                                if (map2 != null) {
                                    bcpTypeAliasSet = (Set) map2.get(bcpTypeId);
                                    if (bcpTypeAliasSet != null) {
                                        for (String alias2 : bcpTypeAliasSet) {
                                            typeDataMap.put(AsciiUtil.toLowerString(alias2), type);
                                        }
                                    }
                                }
                            }
                        } else {
                            i++;
                        }
                    }
                    if (isSpecialType) {
                        if (isTZ) {
                            legacyTypeId = legacyTypeId.replace(':', '/');
                        }
                        bcpTypeId = typeMapEntry.getString();
                        hasSameType = false;
                        if (bcpTypeId.length() == 0) {
                            bcpTypeId = legacyTypeId;
                            hasSameType = true;
                        }
                        type = new Type(legacyTypeId, bcpTypeId);
                        typeDataMap.put(AsciiUtil.toLowerString(legacyTypeId), type);
                        if (hasSameType) {
                            typeDataMap.put(AsciiUtil.toLowerString(bcpTypeId), type);
                        }
                        if (map != null) {
                            typeAliasSet = (Set) map.get(legacyTypeId);
                            if (typeAliasSet != null) {
                                while (alias$iterator.hasNext()) {
                                    typeDataMap.put(AsciiUtil.toLowerString(alias2), type);
                                }
                            }
                        }
                        if (map2 != null) {
                            bcpTypeAliasSet = (Set) map2.get(bcpTypeId);
                            if (bcpTypeAliasSet != null) {
                                while (alias$iterator.hasNext()) {
                                    typeDataMap.put(AsciiUtil.toLowerString(alias2), type);
                                }
                            }
                        }
                    }
                }
            }
            EnumSet<SpecialType> specialTypes = null;
            if (collection != null) {
                specialTypes = EnumSet.copyOf(collection);
            }
            KeyData keyData = new KeyData(legacyKeyId, bcpKeyId, typeDataMap, specialTypes);
            KEYMAP.put(AsciiUtil.toLowerString(legacyKeyId), keyData);
            if (!hasSameKey) {
                KEYMAP.put(AsciiUtil.toLowerString(bcpKeyId), keyData);
            }
        }
    }

    static {
        boolean z;
        if (KeyTypeData.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
        initFromResourceBundle();
    }

    private static void initFromTables() {
        for (Object[] keyDataEntry : KEY_DATA) {
            int i;
            String to;
            Set<String> aliasSet;
            String legacyKeyId = (String) keyDataEntry[0];
            String bcpKeyId = (String) keyDataEntry[1];
            String[][] typeData = (String[][]) keyDataEntry[2];
            String[][] typeAliasData = (String[][]) keyDataEntry[3];
            String[][] bcpTypeAliasData = (String[][]) keyDataEntry[4];
            boolean hasSameKey = false;
            if (bcpKeyId == null) {
                bcpKeyId = legacyKeyId;
                hasSameKey = true;
            }
            Map<String, Set<String>> map = null;
            if (typeAliasData != null) {
                map = new HashMap();
                for (String[] typeAliasDataEntry : typeAliasData) {
                    String from = typeAliasDataEntry[0];
                    to = typeAliasDataEntry[1];
                    aliasSet = (Set) map.get(to);
                    if (aliasSet == null) {
                        aliasSet = new HashSet();
                        map.put(to, aliasSet);
                    }
                    aliasSet.add(from);
                }
            }
            Map map2 = null;
            if (bcpTypeAliasData != null) {
                map2 = new HashMap();
                for (String[] bcpTypeAliasDataEntry : bcpTypeAliasData) {
                    from = bcpTypeAliasDataEntry[0];
                    to = bcpTypeAliasDataEntry[1];
                    aliasSet = (Set) map2.get(to);
                    if (aliasSet == null) {
                        aliasSet = new HashSet();
                        map2.put(to, aliasSet);
                    }
                    aliasSet.add(from);
                }
            }
            if (!-assertionsDisabled) {
                if ((typeData != null ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            Map<String, Type> typeDataMap = new HashMap();
            Collection specialTypeSet = null;
            for (String[] typeDataEntry : typeData) {
                String legacyTypeId = typeDataEntry[0];
                String bcpTypeId = typeDataEntry[1];
                boolean isSpecialType = false;
                SpecialType[] values = SpecialType.values();
                i = 0;
                int length = values.length;
                while (i < length) {
                    boolean hasSameType;
                    Type type;
                    Set<String> typeAliasSet;
                    Set<String> bcpTypeAliasSet;
                    SpecialType st = values[i];
                    if (legacyTypeId.equals(st.toString())) {
                        isSpecialType = true;
                        if (specialTypeSet == null) {
                            specialTypeSet = new HashSet();
                        }
                        specialTypeSet.add(st);
                        if (isSpecialType) {
                            hasSameType = false;
                            if (bcpTypeId == null) {
                                bcpTypeId = legacyTypeId;
                                hasSameType = true;
                            }
                            type = new Type(legacyTypeId, bcpTypeId);
                            typeDataMap.put(AsciiUtil.toLowerString(legacyTypeId), type);
                            if (!hasSameType) {
                                typeDataMap.put(AsciiUtil.toLowerString(bcpTypeId), type);
                            }
                            typeAliasSet = (Set) map.get(legacyTypeId);
                            if (typeAliasSet != null) {
                                for (String alias : typeAliasSet) {
                                    typeDataMap.put(AsciiUtil.toLowerString(alias), type);
                                }
                            }
                            bcpTypeAliasSet = (Set) map2.get(bcpTypeId);
                            if (bcpTypeAliasSet != null) {
                                for (String alias2 : bcpTypeAliasSet) {
                                    typeDataMap.put(AsciiUtil.toLowerString(alias2), type);
                                }
                            }
                        }
                    } else {
                        i++;
                    }
                }
                if (isSpecialType) {
                    hasSameType = false;
                    if (bcpTypeId == null) {
                        bcpTypeId = legacyTypeId;
                        hasSameType = true;
                    }
                    type = new Type(legacyTypeId, bcpTypeId);
                    typeDataMap.put(AsciiUtil.toLowerString(legacyTypeId), type);
                    if (hasSameType) {
                        typeDataMap.put(AsciiUtil.toLowerString(bcpTypeId), type);
                    }
                    typeAliasSet = (Set) map.get(legacyTypeId);
                    if (typeAliasSet != null) {
                        while (alias$iterator.hasNext()) {
                            typeDataMap.put(AsciiUtil.toLowerString(alias2), type);
                        }
                    }
                    bcpTypeAliasSet = (Set) map2.get(bcpTypeId);
                    if (bcpTypeAliasSet != null) {
                        while (alias$iterator.hasNext()) {
                            typeDataMap.put(AsciiUtil.toLowerString(alias2), type);
                        }
                    }
                }
            }
            EnumSet<SpecialType> specialTypes = null;
            if (specialTypeSet != null) {
                specialTypes = EnumSet.copyOf(specialTypeSet);
            }
            KeyData keyData = new KeyData(legacyKeyId, bcpKeyId, typeDataMap, specialTypes);
            KEYMAP.put(AsciiUtil.toLowerString(legacyKeyId), keyData);
            if (!hasSameKey) {
                KEYMAP.put(AsciiUtil.toLowerString(bcpKeyId), keyData);
            }
        }
    }
}
