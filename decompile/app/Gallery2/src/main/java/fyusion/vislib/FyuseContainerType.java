package fyusion.vislib;

/* compiled from: Unknown */
public final class FyuseContainerType {
    public static final FyuseContainerType FEED = new FyuseContainerType("FEED", FyuseContainerUtilsWrapperJNI.FEED_get());
    public static final FyuseContainerType PROCESSED = new FyuseContainerType("PROCESSED", FyuseContainerUtilsWrapperJNI.PROCESSED_get());
    public static final FyuseContainerType RECORDED = new FyuseContainerType("RECORDED", FyuseContainerUtilsWrapperJNI.RECORDED_get());
    public static final FyuseContainerType UPLOAD = new FyuseContainerType("UPLOAD", FyuseContainerUtilsWrapperJNI.UPLOAD_get());
    private static int swigNext = 0;
    private static FyuseContainerType[] swigValues = new FyuseContainerType[]{RECORDED, PROCESSED, UPLOAD, FEED};
    private final String swigName;
    private final int swigValue;

    private FyuseContainerType(String str) {
        this.swigName = str;
        int i = swigNext;
        swigNext = i + 1;
        this.swigValue = i;
    }

    private FyuseContainerType(String str, int i) {
        this.swigName = str;
        this.swigValue = i;
        swigNext = i + 1;
    }

    private FyuseContainerType(String str, FyuseContainerType fyuseContainerType) {
        this.swigName = str;
        this.swigValue = fyuseContainerType.swigValue;
        swigNext = this.swigValue + 1;
    }

    public static FyuseContainerType swigToEnum(int i) {
        int i2 = 0;
        if (i < swigValues.length && i >= 0 && swigValues[i].swigValue == i) {
            return swigValues[i];
        }
        while (i2 < swigValues.length) {
            if (swigValues[i2].swigValue == i) {
                return swigValues[i2];
            }
            i2++;
        }
        throw new IllegalArgumentException("No enum " + FyuseContainerType.class + " with value " + i);
    }

    public final int swigValue() {
        return this.swigValue;
    }

    public String toString() {
        return this.swigName;
    }
}
