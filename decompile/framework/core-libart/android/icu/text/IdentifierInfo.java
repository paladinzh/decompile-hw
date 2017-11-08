package android.icu.text;

import android.icu.lang.UCharacter;
import android.icu.lang.UScript;
import android.icu.text.SpoofChecker.RestrictionLevel;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

@Deprecated
public class IdentifierInfo {
    private static final UnicodeSet ASCII = new UnicodeSet(0, 127).freeze();
    @Deprecated
    public static final Comparator<BitSet> BITSET_COMPARATOR = new Comparator<BitSet>() {
        public int compare(BitSet arg0, BitSet arg1) {
            int diff = arg0.cardinality() - arg1.cardinality();
            if (diff != 0) {
                return diff;
            }
            int i0 = arg0.nextSetBit(0);
            int i1 = arg1.nextSetBit(0);
            while (true) {
                diff = i0 - i1;
                if (diff != 0 || i0 <= 0) {
                    return diff;
                }
                i0 = arg0.nextSetBit(i0 + 1);
                i1 = arg1.nextSetBit(i1 + 1);
            }
            return diff;
        }
    };
    private static final BitSet CHINESE = set(new BitSet(), 25, 17, 5);
    private static final BitSet CONFUSABLE_WITH_LATIN = set(new BitSet(), 8, 14, 6);
    private static final BitSet JAPANESE = set(new BitSet(), 25, 17, 20, 22);
    private static final BitSet KOREAN = set(new BitSet(), 25, 17, 18);
    private final BitSet commonAmongAlternates = new BitSet();
    private String identifier;
    private final UnicodeSet identifierProfile = new UnicodeSet(0, 1114111);
    private final UnicodeSet numerics = new UnicodeSet();
    private final BitSet requiredScripts = new BitSet();
    private final Set<BitSet> scriptSetSet = new HashSet();

    private IdentifierInfo clear() {
        this.requiredScripts.clear();
        this.scriptSetSet.clear();
        this.numerics.clear();
        this.commonAmongAlternates.clear();
        return this;
    }

    @Deprecated
    public IdentifierInfo setIdentifierProfile(UnicodeSet identifierProfile) {
        this.identifierProfile.set(identifierProfile);
        return this;
    }

    @Deprecated
    public UnicodeSet getIdentifierProfile() {
        return new UnicodeSet(this.identifierProfile);
    }

    @Deprecated
    public IdentifierInfo setIdentifier(String identifier) {
        this.identifier = identifier;
        clear();
        BitSet scriptsForCP = new BitSet();
        int i = 0;
        while (i < identifier.length()) {
            int cp = Character.codePointAt(identifier, i);
            if (UCharacter.getType(cp) == 9) {
                this.numerics.add(cp - UCharacter.getNumericValue(cp));
            }
            UScript.getScriptExtensions(cp, scriptsForCP);
            scriptsForCP.clear(0);
            scriptsForCP.clear(1);
            switch (scriptsForCP.cardinality()) {
                case 0:
                    break;
                case 1:
                    this.requiredScripts.or(scriptsForCP);
                    break;
                default:
                    if (!this.requiredScripts.intersects(scriptsForCP) && this.scriptSetSet.add(scriptsForCP)) {
                        scriptsForCP = new BitSet();
                        break;
                    }
            }
            i += Character.charCount(cp);
        }
        if (this.scriptSetSet.size() > 0) {
            this.commonAmongAlternates.set(0, 167);
            Iterator<BitSet> it = this.scriptSetSet.iterator();
            while (it.hasNext()) {
                BitSet next = (BitSet) it.next();
                if (this.requiredScripts.intersects(next)) {
                    it.remove();
                } else {
                    this.commonAmongAlternates.and(next);
                    for (BitSet other : this.scriptSetSet) {
                        if (next != other && contains(next, other)) {
                            it.remove();
                        }
                    }
                }
            }
        }
        if (this.scriptSetSet.size() == 0) {
            this.commonAmongAlternates.clear();
        }
        return this;
    }

    @Deprecated
    public String getIdentifier() {
        return this.identifier;
    }

    @Deprecated
    public BitSet getScripts() {
        return (BitSet) this.requiredScripts.clone();
    }

    @Deprecated
    public Set<BitSet> getAlternates() {
        Set<BitSet> result = new HashSet();
        for (BitSet item : this.scriptSetSet) {
            result.add((BitSet) item.clone());
        }
        return result;
    }

    @Deprecated
    public UnicodeSet getNumerics() {
        return new UnicodeSet(this.numerics);
    }

    @Deprecated
    public BitSet getCommonAmongAlternates() {
        return (BitSet) this.commonAmongAlternates.clone();
    }

    @Deprecated
    public RestrictionLevel getRestrictionLevel() {
        int i = 1;
        if (!this.identifierProfile.containsAll(this.identifier) || getNumerics().size() > 1) {
            return RestrictionLevel.UNRESTRICTIVE;
        }
        if (ASCII.containsAll(this.identifier)) {
            return RestrictionLevel.ASCII;
        }
        int cardinality = this.requiredScripts.cardinality();
        if (this.commonAmongAlternates.cardinality() == 0) {
            i = this.scriptSetSet.size();
        }
        int cardinalityPlus = cardinality + i;
        if (cardinalityPlus < 2) {
            return RestrictionLevel.SINGLE_SCRIPT_RESTRICTIVE;
        }
        if (containsWithAlternates(JAPANESE, this.requiredScripts) || containsWithAlternates(CHINESE, this.requiredScripts) || containsWithAlternates(KOREAN, this.requiredScripts)) {
            return RestrictionLevel.HIGHLY_RESTRICTIVE;
        }
        if (cardinalityPlus == 2 && this.requiredScripts.get(25) && !this.requiredScripts.intersects(CONFUSABLE_WITH_LATIN)) {
            return RestrictionLevel.MODERATELY_RESTRICTIVE;
        }
        return RestrictionLevel.MINIMALLY_RESTRICTIVE;
    }

    @Deprecated
    public int getScriptCount() {
        return this.requiredScripts.cardinality() + (this.commonAmongAlternates.cardinality() == 0 ? this.scriptSetSet.size() : 1);
    }

    @Deprecated
    public String toString() {
        return this.identifier + ", " + this.identifierProfile.toPattern(false) + ", " + getRestrictionLevel() + ", " + displayScripts(this.requiredScripts) + ", " + displayAlternates(this.scriptSetSet) + ", " + this.numerics.toPattern(false);
    }

    private boolean containsWithAlternates(BitSet container, BitSet containee) {
        if (!contains(container, containee)) {
            return false;
        }
        for (BitSet alternatives : this.scriptSetSet) {
            if (!container.intersects(alternatives)) {
                return false;
            }
        }
        return true;
    }

    @Deprecated
    public static String displayAlternates(Set<BitSet> alternates) {
        if (alternates.size() == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        Set<BitSet> sorted = new TreeSet(BITSET_COMPARATOR);
        sorted.addAll(alternates);
        for (BitSet item : sorted) {
            if (result.length() != 0) {
                result.append("; ");
            }
            result.append(displayScripts(item));
        }
        return result.toString();
    }

    @Deprecated
    public static String displayScripts(BitSet scripts) {
        StringBuilder result = new StringBuilder();
        int i = scripts.nextSetBit(0);
        while (i >= 0) {
            if (result.length() != 0) {
                result.append(' ');
            }
            result.append(UScript.getShortName(i));
            i = scripts.nextSetBit(i + 1);
        }
        return result.toString();
    }

    @Deprecated
    public static BitSet parseScripts(String scriptsString) {
        BitSet result = new BitSet();
        for (String item : scriptsString.trim().split(",?\\s+")) {
            if (item.length() != 0) {
                result.set(UScript.getCodeFromName(item));
            }
        }
        return result;
    }

    @Deprecated
    public static Set<BitSet> parseAlternates(String scriptsSetString) {
        Set<BitSet> result = new HashSet();
        for (String item : scriptsSetString.trim().split("\\s*;\\s*")) {
            if (item.length() != 0) {
                result.add(parseScripts(item));
            }
        }
        return result;
    }

    @Deprecated
    public static final boolean contains(BitSet container, BitSet containee) {
        int i = containee.nextSetBit(0);
        while (i >= 0) {
            if (!container.get(i)) {
                return false;
            }
            i = containee.nextSetBit(i + 1);
        }
        return true;
    }

    @Deprecated
    public static final BitSet set(BitSet bitset, int... values) {
        for (int value : values) {
            bitset.set(value);
        }
        return bitset;
    }
}
