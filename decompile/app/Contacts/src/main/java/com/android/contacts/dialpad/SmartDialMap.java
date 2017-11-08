package com.android.contacts.dialpad;

public interface SmartDialMap {
    byte getDialpadIndex(char c);

    char getDialpadNumericCharacter(char c);

    boolean isValidDialpadAlphabeticChar(char c);

    boolean isValidDialpadCharacter(char c);

    boolean isValidDialpadNumericChar(char c);

    char normalizeCharacter(char c);
}
