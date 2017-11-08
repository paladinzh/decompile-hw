package dalvik.bytecode;

public final class OpcodeInfo {
    public static final int MAXIMUM_PACKED_VALUE = 255;
    public static final int MAXIMUM_VALUE = 65535;

    public static boolean isInvoke(int packedOpcode) {
        return false;
    }

    private OpcodeInfo() {
    }
}
