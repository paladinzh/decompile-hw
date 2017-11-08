package sun.security.ssl;

interface Record {
    public static final int OVERFLOW_OF_INT08 = 256;
    public static final int OVERFLOW_OF_INT16 = 65536;
    public static final int OVERFLOW_OF_INT24 = 16777216;
    public static final byte ct_alert = (byte) 21;
    public static final byte ct_application_data = (byte) 23;
    public static final byte ct_change_cipher_spec = (byte) 20;
    public static final byte ct_handshake = (byte) 22;
    public static final boolean enableCBCProtection = Debug.getBooleanProperty("jsse.enableCBCProtection", true);
    public static final int headerSize = 5;
    public static final int maxAlertRecordSize = 539;
    public static final int maxDataSize = 16384;
    public static final int maxDataSizeMinusOneByteRecord = 15846;
    public static final int maxExpansion = 1024;
    public static final int maxIVLength = 256;
    public static final int maxLargeRecordSize = 33305;
    public static final int maxPadding = 256;
    public static final int maxRecordSize = 16921;
    public static final int trailerSize = 20;
}
