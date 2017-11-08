package fyusion.vislib;

/* compiled from: Unknown */
public final class FyuseFileType {
    public static final FyuseFileType FYUSE_DATA = new FyuseFileType("FYUSE_DATA", FyuseContainerUtilsWrapperJNI.FYUSE_DATA_get());
    public static final FyuseFileType FYUSE_JSON_DATA = new FyuseFileType("FYUSE_JSON_DATA", FyuseContainerUtilsWrapperJNI.FYUSE_JSON_DATA_get());
    public static final FyuseFileType FYUSE_RAW_IMAGE_DATA = new FyuseFileType("FYUSE_RAW_IMAGE_DATA", FyuseContainerUtilsWrapperJNI.FYUSE_RAW_IMAGE_DATA_get());
    public static final FyuseFileType INTERMEDIATE_STABILIZATION_DATA = new FyuseFileType("INTERMEDIATE_STABILIZATION_DATA", FyuseContainerUtilsWrapperJNI.INTERMEDIATE_STABILIZATION_DATA_get());
    public static final FyuseFileType MOTION_DATA = new FyuseFileType("MOTION_DATA", FyuseContainerUtilsWrapperJNI.MOTION_DATA_get());
    public static final FyuseFileType PROGRESS_DATA = new FyuseFileType("PROGRESS_DATA", FyuseContainerUtilsWrapperJNI.PROGRESS_DATA_get());
    public static final FyuseFileType SLICE_DATA = new FyuseFileType("SLICE_DATA", FyuseContainerUtilsWrapperJNI.SLICE_DATA_get());
    public static final FyuseFileType STABILIZATION_DATA = new FyuseFileType("STABILIZATION_DATA", FyuseContainerUtilsWrapperJNI.STABILIZATION_DATA_get());
    public static final FyuseFileType TAGGING_DATA = new FyuseFileType("TAGGING_DATA", FyuseContainerUtilsWrapperJNI.TAGGING_DATA_get());
    public static final FyuseFileType THUMB_JPEG = new FyuseFileType("THUMB_JPEG", FyuseContainerUtilsWrapperJNI.THUMB_JPEG_get());
    public static final FyuseFileType THUMB_JPEG_125X125 = new FyuseFileType("THUMB_JPEG_125X125", FyuseContainerUtilsWrapperJNI.THUMB_JPEG_125X125_get());
    public static final FyuseFileType TWEENING_DATA = new FyuseFileType("TWEENING_DATA", FyuseContainerUtilsWrapperJNI.TWEENING_DATA_get());
    private static int swigNext = 0;
    private static FyuseFileType[] swigValues = new FyuseFileType[]{MOTION_DATA, PROGRESS_DATA, THUMB_JPEG, THUMB_JPEG_125X125, FYUSE_DATA, FYUSE_JSON_DATA, INTERMEDIATE_STABILIZATION_DATA, STABILIZATION_DATA, TWEENING_DATA, FYUSE_RAW_IMAGE_DATA, SLICE_DATA, TAGGING_DATA};
    private final String swigName;
    private final int swigValue;

    private FyuseFileType(String str) {
        this.swigName = str;
        int i = swigNext;
        swigNext = i + 1;
        this.swigValue = i;
    }

    private FyuseFileType(String str, int i) {
        this.swigName = str;
        this.swigValue = i;
        swigNext = i + 1;
    }

    private FyuseFileType(String str, FyuseFileType fyuseFileType) {
        this.swigName = str;
        this.swigValue = fyuseFileType.swigValue;
        swigNext = this.swigValue + 1;
    }

    public static FyuseFileType swigToEnum(int i) {
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
        throw new IllegalArgumentException("No enum " + FyuseFileType.class + " with value " + i);
    }

    public final int swigValue() {
        return this.swigValue;
    }

    public String toString() {
        return this.swigName;
    }
}
