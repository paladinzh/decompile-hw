package fyusion.vislib;

/* compiled from: Unknown */
public class FyuseContainerUtilsWrapperJNI {
    public static final native int Android_get();

    public static final native int FEED_get();

    public static final native String FYUSE_DATA_FILE_get();

    public static final native void FYUSE_DATA_FILE_set(String str);

    public static final native int FYUSE_DATA_get();

    public static final native String FYUSE_JSON_DATA_FILE_get();

    public static final native void FYUSE_JSON_DATA_FILE_set(String str);

    public static final native int FYUSE_JSON_DATA_get();

    public static final native String FYUSE_RAW_IMAGE_DATA_FILE_get();

    public static final native void FYUSE_RAW_IMAGE_DATA_FILE_set(String str);

    public static final native int FYUSE_RAW_IMAGE_DATA_get();

    public static final native boolean FyuseContainerUtils_composeFromDir(String str, String str2, int i, int i2);

    public static final native boolean FyuseContainerUtils_decomposeImageDataToDir(String str, String str2, int i);

    public static final native int FyuseContainerUtils_decomposeMetadataToDir(String str, String str2, int i);

    public static final native boolean FyuseContainerUtils_decomposeToDir(String str, String str2, int i);

    public static final native int FyuseContainerUtils_getContainerType(String str, int i);

    public static final native int FyuseContainerUtils_getImageDataOffset(String str, int i);

    public static final native boolean FyuseContainerUtils_isTagged(String str);

    public static final native boolean FyuseContainerUtils_loadMagicDataFromFile(String str, long j, Fyuse fyuse, long j2, FrameBlender frameBlender, int i);

    public static final native int FyuseContainerUtils_loadMetadataForProcessing(String str, long j, StabilizationData stabilizationData, long j2, IMUData iMUData, long j3, SWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage sWIGTYPE_p_fyusion__IntermediateStabilizationDataStorage, long j4, Fyuse fyuse, int i);

    public static final native String INTERMEDIATE_STABILIZATION_DATA_FILE_get();

    public static final native void INTERMEDIATE_STABILIZATION_DATA_FILE_set(String str);

    public static final native int INTERMEDIATE_STABILIZATION_DATA_get();

    public static final native String MOTION_DATA_FILE_get();

    public static final native void MOTION_DATA_FILE_set(String str);

    public static final native int MOTION_DATA_get();

    public static final native int PROCESSED_get();

    public static final native String PROGRESS_DATA_FILE_get();

    public static final native void PROGRESS_DATA_FILE_set(String str);

    public static final native int PROGRESS_DATA_get();

    public static final native int RECORDED_get();

    public static final native String SLICE_DATA_BASE_get();

    public static final native void SLICE_DATA_BASE_set(String str);

    public static final native String SLICE_DATA_EXTENSION_get();

    public static final native void SLICE_DATA_EXTENSION_set(String str);

    public static final native int SLICE_DATA_get();

    public static final native String STABILIZATION_DATA_FILE_get();

    public static final native void STABILIZATION_DATA_FILE_set(String str);

    public static final native int STABILIZATION_DATA_get();

    public static final native String TAGGING_DATA_FILE_get();

    public static final native void TAGGING_DATA_FILE_set(String str);

    public static final native int TAGGING_DATA_get();

    public static final native String THUMB_JPEG_125X125_FILE_get();

    public static final native void THUMB_JPEG_125X125_FILE_set(String str);

    public static final native int THUMB_JPEG_125X125_get();

    public static final native String THUMB_JPEG_FILE_get();

    public static final native void THUMB_JPEG_FILE_set(String str);

    public static final native int THUMB_JPEG_get();

    public static final native String TWEENING_DATA_FILE_get();

    public static final native void TWEENING_DATA_FILE_set(String str);

    public static final native int TWEENING_DATA_get();

    public static final native int UPLOAD_get();

    public static final native void delete_FyuseContainerUtils(long j);

    public static final native String getFyuseDataFilename();

    public static final native String getFyuseJSONDataFilename();

    public static final native String getFyuseRawImageDataFilename();

    public static final native String getIntermediateStabilizationDataFilename();

    public static final native String getMotionDataFilename();

    public static final native String getProgressDataFilename();

    public static final native String getSliceDataBase();

    public static final native String getSliceDataExtension();

    public static final native String getStabilizationDataFilename();

    public static final native String getTaggingDataFilename();

    public static final native String getThumbJPEG125x125Filename();

    public static final native String getThumbJPEGFilename();

    public static final native String getTweeningDataFilename();

    public static final native int iOS_get();

    public static final native long new_FyuseContainerUtils();
}
