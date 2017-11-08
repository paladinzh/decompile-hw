package com.trustlook.sdk.data;

@Deprecated
public enum LegitState {
    BAD(0, "Bad"),
    SYSTEM(2, "System"),
    SIGNATURE_ERROR(-3, "Sig calculate error"),
    INADEQUATE_INFO(-2, "Inadequate info provided"),
    DEBUG(3, "Debug"),
    NOT_DECIDE(-1, "Goog or bad not decided yet"),
    GOOD(1, "Good"),
    UNKNOWN(999, "We do not care");
    
    private int a;
    private String b;

    public final int getValue() {
        return this.a;
    }

    public final void setValue(int i) {
        this.a = i;
    }

    public final String getCause() {
        return this.b;
    }

    public final void setCause(String str) {
        this.b = str;
    }

    private LegitState(int i, String str) {
        this.a = i;
        this.b = str;
    }

    public static LegitState getLegitStateFromValue(int i) {
        for (LegitState legitState : values()) {
            if (legitState.a == i) {
                return legitState;
            }
        }
        return UNKNOWN;
    }
}
