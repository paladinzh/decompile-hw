package cn.com.xy.sms.sdk.db.entity.a;

/* compiled from: Unknown */
public enum m {
    STATUS_NOT_REQUEST,
    STATUS_HAS_REQUEST,
    ALL;

    public final String toString() {
        switch (a()[ordinal()]) {
            case 1:
                return "0";
            case 2:
                return "1";
            case 3:
                return "2";
            default:
                return "UNKNOWN";
        }
    }
}
