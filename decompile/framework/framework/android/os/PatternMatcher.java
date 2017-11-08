package android.os;

import android.os.Parcelable.Creator;

public class PatternMatcher implements Parcelable {
    public static final Creator<PatternMatcher> CREATOR = new Creator<PatternMatcher>() {
        public PatternMatcher createFromParcel(Parcel source) {
            return new PatternMatcher(source);
        }

        public PatternMatcher[] newArray(int size) {
            return new PatternMatcher[size];
        }
    };
    public static final int PATTERN_LITERAL = 0;
    public static final int PATTERN_PREFIX = 1;
    public static final int PATTERN_SIMPLE_GLOB = 2;
    private final String mPattern;
    private final int mType;

    public PatternMatcher(String pattern, int type) {
        this.mPattern = pattern;
        this.mType = type;
    }

    public final String getPath() {
        return this.mPattern;
    }

    public final int getType() {
        return this.mType;
    }

    public boolean match(String str) {
        return matchPattern(this.mPattern, str, this.mType);
    }

    public String toString() {
        String type = "? ";
        switch (this.mType) {
            case 0:
                type = "LITERAL: ";
                break;
            case 1:
                type = "PREFIX: ";
                break;
            case 2:
                type = "GLOB: ";
                break;
        }
        return "PatternMatcher{" + type + this.mPattern + "}";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPattern);
        dest.writeInt(this.mType);
    }

    public PatternMatcher(Parcel src) {
        this.mPattern = src.readString();
        this.mType = src.readInt();
    }

    static boolean matchPattern(String pattern, String match, int type) {
        boolean z = true;
        if (match == null) {
            return false;
        }
        if (type == 0) {
            return pattern.equals(match);
        }
        if (type == 1) {
            return match.startsWith(pattern);
        }
        if (type != 2) {
            return false;
        }
        int NP = pattern.length();
        if (NP <= 0) {
            if (match.length() > 0) {
                z = false;
            }
            return z;
        }
        int NM = match.length();
        int ip = 0;
        int im = 0;
        char charAt = pattern.charAt(0);
        while (ip < NP && im < NM) {
            boolean escaped;
            char c = charAt;
            ip++;
            charAt = ip < NP ? pattern.charAt(ip) : '\u0000';
            if (c == '\\') {
                escaped = true;
            } else {
                escaped = false;
            }
            if (escaped) {
                c = charAt;
                ip++;
                charAt = ip < NP ? pattern.charAt(ip) : '\u0000';
            }
            if (charAt == '*') {
                if (escaped || c != '.') {
                    while (match.charAt(im) == c) {
                        im++;
                        if (im >= NM) {
                            break;
                        }
                    }
                    ip++;
                    charAt = ip < NP ? pattern.charAt(ip) : '\u0000';
                } else if (ip >= NP - 1) {
                    return true;
                } else {
                    ip++;
                    charAt = pattern.charAt(ip);
                    if (charAt == '\\') {
                        ip++;
                        charAt = ip < NP ? pattern.charAt(ip) : '\u0000';
                    }
                    while (match.charAt(im) != charAt) {
                        im++;
                        if (im >= NM) {
                            break;
                        }
                    }
                    if (im == NM) {
                        return false;
                    }
                    ip++;
                    charAt = ip < NP ? pattern.charAt(ip) : '\u0000';
                    im++;
                }
            } else if (c != '.' && match.charAt(im) != c) {
                return false;
            } else {
                im++;
            }
        }
        if (ip < NP || im < NM) {
            return ip == NP + -2 && pattern.charAt(ip) == '.' && pattern.charAt(ip + 1) == '*';
        } else {
            return true;
        }
    }
}
