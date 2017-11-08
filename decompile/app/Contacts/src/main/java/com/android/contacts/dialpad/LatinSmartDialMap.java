package com.android.contacts.dialpad;

import java.util.HashMap;

public class LatinSmartDialMap implements SmartDialMap {
    private static final char[] LATIN_LETTERS_TO_DIGITS = new char[]{'2', '2', '2', '3', '3', '3', '4', '4', '4', '5', '5', '5', '6', '6', '6', '7', '7', '7', '7', '8', '8', '8', '9', '9', '9', '9'};

    public boolean isValidDialpadAlphabeticChar(char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    public boolean isValidDialpadNumericChar(char ch) {
        return ch >= '0' && ch <= '9';
    }

    public boolean isValidDialpadCharacter(char ch) {
        return !isValidDialpadAlphabeticChar(ch) ? isValidDialpadNumericChar(ch) : true;
    }

    public char normalizeCharacter(char ch) {
        HashMap<Character, Character> hm = GetCharacterData.getInstance().getCharData();
        if (hm.containsKey(Character.valueOf(ch))) {
            return ((Character) hm.get(Character.valueOf(ch))).charValue();
        }
        return ch;
    }

    public byte getDialpadIndex(char ch) {
        if (ch >= '0' && ch <= '9') {
            return (byte) (ch - 48);
        }
        if (ch < 'a' || ch > 'z') {
            return (byte) -1;
        }
        return (byte) (LATIN_LETTERS_TO_DIGITS[ch - 97] - 48);
    }

    public char getDialpadNumericCharacter(char ch) {
        if (ch < 'a' || ch > 'z') {
            return ch;
        }
        return LATIN_LETTERS_TO_DIGITS[ch - 97];
    }
}
