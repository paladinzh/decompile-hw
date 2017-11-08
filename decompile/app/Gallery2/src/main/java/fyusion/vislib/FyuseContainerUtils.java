package fyusion.vislib;

/* compiled from: Unknown */
public class FyuseContainerUtils {
    protected boolean swigCMemOwn;
    private long swigCPtr;

    public FyuseContainerUtils() {
        this(FyuseContainerUtilsWrapperJNI.new_FyuseContainerUtils(), true);
    }

    protected FyuseContainerUtils(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    public static boolean composeFromDir(String str, String str2, Platform platform, FyuseContainerType fyuseContainerType) {
        return FyuseContainerUtilsWrapperJNI.FyuseContainerUtils_composeFromDir(str, str2, platform.swigValue(), fyuseContainerType.swigValue());
    }

    public static boolean decomposeImageDataToDir(String str, String str2, Platform platform) {
        return FyuseContainerUtilsWrapperJNI.FyuseContainerUtils_decomposeImageDataToDir(str, str2, platform.swigValue());
    }

    public static int decomposeMetadataToDir(String str, String str2, Platform platform) {
        return FyuseContainerUtilsWrapperJNI.FyuseContainerUtils_decomposeMetadataToDir(str, str2, platform.swigValue());
    }

    public static boolean decomposeToDir(String str, String str2, Platform platform) {
        return FyuseContainerUtilsWrapperJNI.FyuseContainerUtils_decomposeToDir(str, str2, platform.swigValue());
    }

    protected static long getCPtr(FyuseContainerUtils fyuseContainerUtils) {
        return fyuseContainerUtils != null ? fyuseContainerUtils.swigCPtr : 0;
    }

    public static int getContainerType(String str, Platform platform) {
        return FyuseContainerUtilsWrapperJNI.FyuseContainerUtils_getContainerType(str, platform.swigValue());
    }

    public static int getImageDataOffset(String str, Platform platform) {
        return FyuseContainerUtilsWrapperJNI.FyuseContainerUtils_getImageDataOffset(str, platform.swigValue());
    }

    public static boolean isTagged(String str) {
        return FyuseContainerUtilsWrapperJNI.FyuseContainerUtils_isTagged(str);
    }

    public static boolean loadMagicDataFromFile(String str, Fyuse fyuse, FrameBlender frameBlender, Platform platform) {
        return FyuseContainerUtilsWrapperJNI.FyuseContainerUtils_loadMagicDataFromFile(str, Fyuse.getCPtr(fyuse), fyuse, FrameBlender.getCPtr(frameBlender), frameBlender, platform.swigValue());
    }

    public static int loadMetadataForProcessing(String str, StabilizationData stabilizationData, IMUData iMUData, SWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage sWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage, Fyuse fyuse, Platform platform) {
        return FyuseContainerUtilsWrapperJNI.FyuseContainerUtils_loadMetadataForProcessing(str, StabilizationData.getCPtr(stabilizationData), stabilizationData, IMUData.getCPtr(iMUData), iMUData, SWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage.getCPtr(sWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage), sWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage, Fyuse.getCPtr(fyuse), fyuse, platform.swigValue());
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseContainerUtilsWrapperJNI.delete_FyuseContainerUtils(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }
}
