package fyusion.vislib;

/* compiled from: Unknown */
public class FyuseWrapperJNI {
    public static final native void BoolVec_add(long j, BoolVec boolVec, boolean z);

    public static final native long BoolVec_capacity(long j, BoolVec boolVec);

    public static final native void BoolVec_clear(long j, BoolVec boolVec);

    public static final native boolean BoolVec_get(long j, BoolVec boolVec, int i);

    public static final native boolean BoolVec_isEmpty(long j, BoolVec boolVec);

    public static final native void BoolVec_reserve(long j, BoolVec boolVec, long j2);

    public static final native void BoolVec_set(long j, BoolVec boolVec, int i, boolean z);

    public static final native long BoolVec_size(long j, BoolVec boolVec);

    public static final native long CVTransform_transform_get(long j, CVTransform cVTransform);

    public static final native void CVTransform_transform_set(long j, CVTransform cVTransform, long j2, FloatVec floatVec);

    public static final native double Date_seconds_since_1970_get(long j, Date date);

    public static final native void Date_seconds_since_1970_set(long j, Date date, double d);

    public static final native void FloatVec_add(long j, FloatVec floatVec, float f);

    public static final native long FloatVec_capacity(long j, FloatVec floatVec);

    public static final native void FloatVec_clear(long j, FloatVec floatVec);

    public static final native float FloatVec_get(long j, FloatVec floatVec, int i);

    public static final native boolean FloatVec_isEmpty(long j, FloatVec floatVec);

    public static final native void FloatVec_reserve(long j, FloatVec floatVec, long j2);

    public static final native void FloatVec_set(long j, FloatVec floatVec, int i, float f);

    public static final native long FloatVec_size(long j, FloatVec floatVec);

    public static final native float FrameBlender_getStabilizationScale(long j, FrameBlender frameBlender);

    public static final native boolean FrameBlender_queryBlendingInfoForFrameId(long j, FrameBlender frameBlender, float f, int[] iArr, float[] fArr, long j2, TransformationParameters transformationParameters, int[] iArr2, float[] fArr2, long j3, TransformationParameters transformationParameters2);

    public static final native void FrameBlender_setIndexingOffset(long j, FrameBlender frameBlender, int i);

    public static final native void FrameBlender_setLoopClosed(long j, FrameBlender frameBlender, boolean z, int i, int i2);

    public static final native void FrameBlender_setSizes(long j, FrameBlender frameBlender, int i, int i2, int i3, int i4, float f);

    public static final native void FrameBlender_setStabilizedMJPEG(long j, FrameBlender frameBlender, boolean z);

    public static final native boolean FrameBlender_setTweeningFileAndSizes(long j, FrameBlender frameBlender, String str, int i, int i2, int i3, int i4, float f);

    public static final native String FyuseAddress_administrative_area_get(long j, FyuseAddress fyuseAddress);

    public static final native void FyuseAddress_administrative_area_set(long j, FyuseAddress fyuseAddress, String str);

    public static final native String FyuseAddress_country_get(long j, FyuseAddress fyuseAddress);

    public static final native void FyuseAddress_country_set(long j, FyuseAddress fyuseAddress, String str);

    public static final native String FyuseAddress_iso_country_code_get(long j, FyuseAddress fyuseAddress);

    public static final native void FyuseAddress_iso_country_code_set(long j, FyuseAddress fyuseAddress, String str);

    public static final native String FyuseAddress_locality_get(long j, FyuseAddress fyuseAddress);

    public static final native void FyuseAddress_locality_set(long j, FyuseAddress fyuseAddress, String str);

    public static final native String FyuseAddress_postal_code_get(long j, FyuseAddress fyuseAddress);

    public static final native void FyuseAddress_postal_code_set(long j, FyuseAddress fyuseAddress, String str);

    public static final native String FyuseAddress_sub_administrative_area_get(long j, FyuseAddress fyuseAddress);

    public static final native void FyuseAddress_sub_administrative_area_set(long j, FyuseAddress fyuseAddress, String str);

    public static final native String FyuseAddress_sub_locality_get(long j, FyuseAddress fyuseAddress);

    public static final native void FyuseAddress_sub_locality_set(long j, FyuseAddress fyuseAddress, String str);

    public static final native String FyuseAddress_sub_thoroughfare_get(long j, FyuseAddress fyuseAddress);

    public static final native void FyuseAddress_sub_thoroughfare_set(long j, FyuseAddress fyuseAddress, String str);

    public static final native String FyuseAddress_thoroughfare_get(long j, FyuseAddress fyuseAddress);

    public static final native void FyuseAddress_thoroughfare_set(long j, FyuseAddress fyuseAddress, String str);

    public static final native int FyuseBlacklistEntry_frame_get(long j, FyuseBlacklistEntry fyuseBlacklistEntry);

    public static final native void FyuseBlacklistEntry_frame_set(long j, FyuseBlacklistEntry fyuseBlacklistEntry, int i);

    public static final native String FyuseBlacklistEntry_reason_get(long j, FyuseBlacklistEntry fyuseBlacklistEntry);

    public static final native void FyuseBlacklistEntry_reason_set(long j, FyuseBlacklistEntry fyuseBlacklistEntry, String str);

    public static final native void FyuseFrameInformationVec_add(long j, FyuseFrameInformationVec fyuseFrameInformationVec, long j2, FyuseFrameInformation fyuseFrameInformation);

    public static final native long FyuseFrameInformationVec_capacity(long j, FyuseFrameInformationVec fyuseFrameInformationVec);

    public static final native void FyuseFrameInformationVec_clear(long j, FyuseFrameInformationVec fyuseFrameInformationVec);

    public static final native long FyuseFrameInformationVec_get(long j, FyuseFrameInformationVec fyuseFrameInformationVec, int i);

    public static final native boolean FyuseFrameInformationVec_isEmpty(long j, FyuseFrameInformationVec fyuseFrameInformationVec);

    public static final native void FyuseFrameInformationVec_reserve(long j, FyuseFrameInformationVec fyuseFrameInformationVec, long j2);

    public static final native void FyuseFrameInformationVec_set(long j, FyuseFrameInformationVec fyuseFrameInformationVec, int i, long j2, FyuseFrameInformation fyuseFrameInformation);

    public static final native long FyuseFrameInformationVec_size(long j, FyuseFrameInformationVec fyuseFrameInformationVec);

    public static final native double FyuseFrameInformation_brightness_value_get(long j, FyuseFrameInformation fyuseFrameInformation);

    public static final native void FyuseFrameInformation_brightness_value_set(long j, FyuseFrameInformation fyuseFrameInformation, double d);

    public static final native double FyuseFrameInformation_exposure_value_get(long j, FyuseFrameInformation fyuseFrameInformation);

    public static final native void FyuseFrameInformation_exposure_value_set(long j, FyuseFrameInformation fyuseFrameInformation, double d);

    public static final native boolean FyuseFrameInformation_has_imu_direction_settled_get(long j, FyuseFrameInformation fyuseFrameInformation);

    public static final native void FyuseFrameInformation_has_imu_direction_settled_set(long j, FyuseFrameInformation fyuseFrameInformation, boolean z);

    public static final native double FyuseFrameInformation_imu_direction_x_get(long j, FyuseFrameInformation fyuseFrameInformation);

    public static final native void FyuseFrameInformation_imu_direction_x_set(long j, FyuseFrameInformation fyuseFrameInformation, double d);

    public static final native double FyuseFrameInformation_imu_direction_y_get(long j, FyuseFrameInformation fyuseFrameInformation);

    public static final native void FyuseFrameInformation_imu_direction_y_set(long j, FyuseFrameInformation fyuseFrameInformation, double d);

    public static final native boolean FyuseFrameInformation_is_blacklisted_get(long j, FyuseFrameInformation fyuseFrameInformation);

    public static final native void FyuseFrameInformation_is_blacklisted_set(long j, FyuseFrameInformation fyuseFrameInformation, boolean z);

    public static final native boolean FyuseFrameInformation_is_dropped_online_get(long j, FyuseFrameInformation fyuseFrameInformation);

    public static final native void FyuseFrameInformation_is_dropped_online_set(long j, FyuseFrameInformation fyuseFrameInformation, boolean z);

    public static final native int FyuseFrameInformation_iso_value_get(long j, FyuseFrameInformation fyuseFrameInformation);

    public static final native void FyuseFrameInformation_iso_value_set(long j, FyuseFrameInformation fyuseFrameInformation, int i);

    public static final native double FyuseFrameInformation_timestamp_in_seconds_get(long j, FyuseFrameInformation fyuseFrameInformation);

    public static final native void FyuseFrameInformation_timestamp_in_seconds_set(long j, FyuseFrameInformation fyuseFrameInformation, double d);

    public static final native long FyusePlacemark_address_get(long j, FyusePlacemark fyusePlacemark);

    public static final native void FyusePlacemark_address_set(long j, FyusePlacemark fyusePlacemark, long j2, FyuseAddress fyuseAddress);

    public static final native float FyusePlacemark_latitude_get(long j, FyusePlacemark fyusePlacemark);

    public static final native void FyusePlacemark_latitude_set(long j, FyusePlacemark fyusePlacemark, float f);

    public static final native float FyusePlacemark_longitude_get(long j, FyusePlacemark fyusePlacemark);

    public static final native void FyusePlacemark_longitude_set(long j, FyusePlacemark fyusePlacemark, float f);

    public static final native String FyusePlacemark_preferred_location_get(long j, FyusePlacemark fyusePlacemark);

    public static final native void FyusePlacemark_preferred_location_set(long j, FyusePlacemark fyusePlacemark, String str);

    public static final native void FyuseSliceVec_add(long j, FyuseSliceVec fyuseSliceVec, long j2, FyuseSlice fyuseSlice);

    public static final native long FyuseSliceVec_capacity(long j, FyuseSliceVec fyuseSliceVec);

    public static final native void FyuseSliceVec_clear(long j, FyuseSliceVec fyuseSliceVec);

    public static final native long FyuseSliceVec_get(long j, FyuseSliceVec fyuseSliceVec, int i);

    public static final native boolean FyuseSliceVec_isEmpty(long j, FyuseSliceVec fyuseSliceVec);

    public static final native void FyuseSliceVec_reserve(long j, FyuseSliceVec fyuseSliceVec, long j2);

    public static final native void FyuseSliceVec_set(long j, FyuseSliceVec fyuseSliceVec, int i, long j2, FyuseSlice fyuseSlice);

    public static final native long FyuseSliceVec_size(long j, FyuseSliceVec fyuseSliceVec);

    public static final native int FyuseSlice_end_frame_get(long j, FyuseSlice fyuseSlice);

    public static final native void FyuseSlice_end_frame_set(long j, FyuseSlice fyuseSlice, int i);

    public static final native String FyuseSlice_h264_file_name_get(long j, FyuseSlice fyuseSlice);

    public static final native void FyuseSlice_h264_file_name_set(long j, FyuseSlice fyuseSlice, String str);

    public static final native String FyuseSlice_index_file_name_get(long j, FyuseSlice fyuseSlice);

    public static final native void FyuseSlice_index_file_name_set(long j, FyuseSlice fyuseSlice, String str);

    public static final native int FyuseSlice_index_get(long j, FyuseSlice fyuseSlice);

    public static final native void FyuseSlice_index_set(long j, FyuseSlice fyuseSlice, int i);

    public static final native boolean FyuseSlice_low_resolution_preview_get(long j, FyuseSlice fyuseSlice);

    public static final native void FyuseSlice_low_resolution_preview_set(long j, FyuseSlice fyuseSlice, boolean z);

    public static final native String FyuseSlice_mjpeg_file_name_get(long j, FyuseSlice fyuseSlice);

    public static final native void FyuseSlice_mjpeg_file_name_set(long j, FyuseSlice fyuseSlice, String str);

    public static final native int FyuseSlice_processed_height_get(long j, FyuseSlice fyuseSlice);

    public static final native void FyuseSlice_processed_height_set(long j, FyuseSlice fyuseSlice, int i);

    public static final native int FyuseSlice_processed_width_get(long j, FyuseSlice fyuseSlice);

    public static final native void FyuseSlice_processed_width_set(long j, FyuseSlice fyuseSlice, int i);

    public static final native int FyuseSlice_start_frame_get(long j, FyuseSlice fyuseSlice);

    public static final native void FyuseSlice_start_frame_set(long j, FyuseSlice fyuseSlice, int i);

    public static final native void Fyuse_addFrameInformation(long j, Fyuse fyuse, long j2, FyuseFrameInformation fyuseFrameInformation);

    public static final native void Fyuse_addSlice(long j, Fyuse fyuse, long j2, FyuseSlice fyuseSlice);

    public static final native boolean Fyuse_apply_face_detection_for_stabilization__get(long j, Fyuse fyuse);

    public static final native void Fyuse_apply_face_detection_for_stabilization__set(long j, Fyuse fyuse, boolean z);

    public static final native float Fyuse_average_iso_value__get(long j, Fyuse fyuse);

    public static final native void Fyuse_average_iso_value__set(long j, Fyuse fyuse, float f);

    public static final native int Fyuse_camera_fps_indicator_frame_window_size__get(long j, Fyuse fyuse);

    public static final native void Fyuse_camera_fps_indicator_frame_window_size__set(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_clearCaches(long j, Fyuse fyuse);

    public static final native void Fyuse_clearFrameInformations(long j, Fyuse fyuse);

    public static final native void Fyuse_clearSlices(long j, Fyuse fyuse);

    public static final native int Fyuse_current_version_number__get(long j, Fyuse fyuse);

    public static final native void Fyuse_current_version_number__set(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_deleteView(String str);

    public static final native boolean Fyuse_enable_standard_unsharpen_filter__get(long j, Fyuse fyuse);

    public static final native void Fyuse_enable_standard_unsharpen_filter__set(long j, Fyuse fyuse, boolean z);

    public static final native boolean Fyuse_fixed_last_nil_frame_error__get(long j, Fyuse fyuse);

    public static final native void Fyuse_fixed_last_nil_frame_error__set(long j, Fyuse fyuse, boolean z);

    public static final native float Fyuse_fyuse_quality__get(long j, Fyuse fyuse);

    public static final native void Fyuse_fyuse_quality__set(long j, Fyuse fyuse, float f);

    public static final native String Fyuse_getAppVersionUsedToRecord(long j, Fyuse fyuse);

    public static final native String Fyuse_getAppVersionUsedToUpload(long j, Fyuse fyuse);

    public static final native String Fyuse_getCameraFile(long j, Fyuse fyuse);

    public static final native int Fyuse_getCameraOrientation(long j, Fyuse fyuse);

    public static final native FyuseSize Fyuse_getCameraSize(long j, Fyuse fyuse);

    public static final native long Fyuse_getCreationDate(long j, Fyuse fyuse);

    public static final native String Fyuse_getCurrentFilterID(long j, Fyuse fyuse);

    public static final native float Fyuse_getCurvature(long j, Fyuse fyuse);

    public static final native String Fyuse_getDeviceID(long j, Fyuse fyuse);

    public static final native float Fyuse_getDirectionX(long j, Fyuse fyuse);

    public static final native float Fyuse_getDirectionY(long j, Fyuse fyuse);

    public static final native int Fyuse_getEndFrame(long j, Fyuse fyuse);

    public static final native long Fyuse_getFrameInformation(long j, Fyuse fyuse, int i);

    public static final native int Fyuse_getFrameSelectionType(long j, Fyuse fyuse);

    public static final native long Fyuse_getFrameTimestamps(long j, Fyuse fyuse);

    public static final native double Fyuse_getGlobalScale(long j, Fyuse fyuse);

    public static final native float Fyuse_getGravityX(long j, Fyuse fyuse);

    public static final native float Fyuse_getGravityY(long j, Fyuse fyuse);

    public static final native float Fyuse_getIMUDirectionX(long j, Fyuse fyuse);

    public static final native float Fyuse_getIMUDirectionY(long j, Fyuse fyuse);

    public static final native String Fyuse_getListOfAdjustmentFilterParameters(long j, Fyuse fyuse);

    public static final native String Fyuse_getListOfAdjustmentFilters(long j, Fyuse fyuse);

    public static final native int Fyuse_getLoopClosedEndFrame(long j, Fyuse fyuse);

    public static final native int Fyuse_getLoopClosedStartFrame(long j, Fyuse fyuse);

    public static final native String Fyuse_getMotionFile(long j, Fyuse fyuse);

    public static final native int Fyuse_getNumberOfCameraFrames(long j, Fyuse fyuse);

    public static final native int Fyuse_getNumberOfFrameInformations(long j, Fyuse fyuse);

    public static final native int Fyuse_getNumberOfMotionFrames(long j, Fyuse fyuse);

    public static final native int Fyuse_getNumberOfProcessedFrames(long j, Fyuse fyuse);

    public static final native int Fyuse_getNumberOfSlices(long j, Fyuse fyuse);

    public static final native int Fyuse_getNumberOfStabilizedFrames(long j, Fyuse fyuse);

    public static final native String Fyuse_getOffsetsFile(long j, Fyuse fyuse);

    public static final native long Fyuse_getPlacemark(long j, Fyuse fyuse);

    public static final native String Fyuse_getProcessedFile(long j, Fyuse fyuse);

    public static final native FyuseSize Fyuse_getProcessedSize(long j, Fyuse fyuse);

    public static final native String Fyuse_getSelectedFilterID(long j, Fyuse fyuse);

    public static final native String Fyuse_getSelectedFilterParameter(long j, Fyuse fyuse);

    public static final native long Fyuse_getSlice(long j, Fyuse fyuse, int i);

    public static final native long Fyuse_getSliceInformation(long j, Fyuse fyuse);

    public static final native int Fyuse_getStabilizationDataFrameOffset(long j, Fyuse fyuse);

    public static final native int Fyuse_getStartFrame(long j, Fyuse fyuse);

    public static final native float Fyuse_getSwipeDirectionX(long j, Fyuse fyuse);

    public static final native float Fyuse_getSwipeDirectionY(long j, Fyuse fyuse);

    public static final native int Fyuse_getThumbnailIndex(long j, Fyuse fyuse);

    public static final native String Fyuse_getUniqueDeviceID(long j, Fyuse fyuse);

    public static final native int Fyuse_getVersion(long j, Fyuse fyuse);

    public static final native String Fyuse_getVideoEncodingCode(long j, Fyuse fyuse);

    public static final native String Fyuse_getView();

    public static final native int Fyuse_h264_recording_video_fps__get(long j, Fyuse fyuse);

    public static final native void Fyuse_h264_recording_video_fps__set(long j, Fyuse fyuse, int i);

    public static final native int Fyuse_h264_upload_video_fps__get(long j, Fyuse fyuse);

    public static final native void Fyuse_h264_upload_video_fps__set(long j, Fyuse fyuse, int i);

    public static final native boolean Fyuse_hasBeenUploadedInSlicedForm(long j, Fyuse fyuse);

    public static final native boolean Fyuse_have_user_input_for_stabilization__get(long j, Fyuse fyuse);

    public static final native void Fyuse_have_user_input_for_stabilization__set(long j, Fyuse fyuse, boolean z);

    public static final native void Fyuse_insertFrameInformation(long j, Fyuse fyuse, long j2, FyuseFrameInformation fyuseFrameInformation, int i);

    public static final native void Fyuse_insertSlice(long j, Fyuse fyuse, long j2, FyuseSlice fyuseSlice, int i);

    public static final native boolean Fyuse_isConvex(long j, Fyuse fyuse);

    public static final native boolean Fyuse_isLoopClosed(long j, Fyuse fyuse);

    public static final native boolean Fyuse_isPortrait(long j, Fyuse fyuse);

    public static final native boolean Fyuse_loadFromMagicFile(long j, Fyuse fyuse, String str);

    public static final native boolean Fyuse_loadFromXMLString(long j, Fyuse fyuse, String str);

    public static final native boolean Fyuse_loop_closed__get(long j, Fyuse fyuse);

    public static final native void Fyuse_loop_closed__set(long j, Fyuse fyuse, boolean z);

    public static final native int Fyuse_loop_closed_end_frame__get(long j, Fyuse fyuse);

    public static final native void Fyuse_loop_closed_end_frame__set(long j, Fyuse fyuse, int i);

    public static final native int Fyuse_loop_closed_start_frame__get(long j, Fyuse fyuse);

    public static final native void Fyuse_loop_closed_start_frame__set(long j, Fyuse fyuse, int i);

    public static final native int Fyuse_max_low_fps_iso_value__get(long j, Fyuse fyuse);

    public static final native void Fyuse_max_low_fps_iso_value__set(long j, Fyuse fyuse, int i);

    public static final native int Fyuse_max_number_frames__get(long j, Fyuse fyuse);

    public static final native void Fyuse_max_number_frames__set(long j, Fyuse fyuse, int i);

    public static final native int Fyuse_max_ready_for_more_media_data_check_fails__get(long j, Fyuse fyuse);

    public static final native void Fyuse_max_ready_for_more_media_data_check_fails__set(long j, Fyuse fyuse, int i);

    public static final native float Fyuse_max_stabilization_angle__get(long j, Fyuse fyuse);

    public static final native void Fyuse_max_stabilization_angle__set(long j, Fyuse fyuse, float f);

    public static final native int Fyuse_max_stabilization_motion__get(long j, Fyuse fyuse);

    public static final native void Fyuse_max_stabilization_motion__set(long j, Fyuse fyuse, int i);

    public static final native int Fyuse_min_high_fps_iso_value__get(long j, Fyuse fyuse);

    public static final native void Fyuse_min_high_fps_iso_value__set(long j, Fyuse fyuse, int i);

    public static final native int Fyuse_mjpeg_video_fps__get(long j, Fyuse fyuse);

    public static final native void Fyuse_mjpeg_video_fps__set(long j, Fyuse fyuse, int i);

    public static final native boolean Fyuse_night_mode__get(long j, Fyuse fyuse);

    public static final native void Fyuse_night_mode__set(long j, Fyuse fyuse, boolean z);

    public static final native int Fyuse_num_frames_to_crop__get(long j, Fyuse fyuse);

    public static final native void Fyuse_num_frames_to_crop__set(long j, Fyuse fyuse, int i);

    public static final native int Fyuse_post_processing_width__get(long j, Fyuse fyuse);

    public static final native void Fyuse_post_processing_width__set(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_printToConsole(long j, Fyuse fyuse);

    public static final native float Fyuse_ready_for_more_media_data_sleep_time__get(long j, Fyuse fyuse);

    public static final native void Fyuse_ready_for_more_media_data_sleep_time__set(long j, Fyuse fyuse, float f);

    public static final native void Fyuse_removeAllSlices(long j, Fyuse fyuse);

    public static final native void Fyuse_removeFrameInformation(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_removeSlice(long j, Fyuse fyuse, int i);

    public static final native boolean Fyuse_saveToMagicFile(long j, Fyuse fyuse, String str);

    public static final native boolean Fyuse_saveToXMLFile(long j, Fyuse fyuse, String str);

    public static final native String Fyuse_saveToXMLString(long j, Fyuse fyuse);

    public static final native void Fyuse_setAppVersionUsedToRecord(long j, Fyuse fyuse, String str);

    public static final native void Fyuse_setAppVersionUsedToUpload(long j, Fyuse fyuse, String str);

    public static final native void Fyuse_setCameraFile(long j, Fyuse fyuse, String str);

    public static final native void Fyuse_setCameraOrientation(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_setCameraSize(long j, Fyuse fyuse, FyuseSize fyuseSize);

    public static final native void Fyuse_setCreationData(long j, Fyuse fyuse, long j2, Date date);

    public static final native void Fyuse_setCurrentFilterID(long j, Fyuse fyuse, String str);

    public static final native void Fyuse_setCurvature(long j, Fyuse fyuse, float f);

    public static final native void Fyuse_setDefaultValues(long j, Fyuse fyuse);

    public static final native void Fyuse_setDeviceID(long j, Fyuse fyuse, String str);

    public static final native void Fyuse_setDirectionX(long j, Fyuse fyuse, float f);

    public static final native void Fyuse_setDirectionY(long j, Fyuse fyuse, float f);

    public static final native void Fyuse_setEndFrame(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_setFrameInformation(long j, Fyuse fyuse, int i, long j2, FyuseFrameInformation fyuseFrameInformation);

    public static final native void Fyuse_setFrameSelectionType(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_setFrameTimestamps(long j, Fyuse fyuse, long j2, FyuseFrameInformationVec fyuseFrameInformationVec);

    public static final native void Fyuse_setGlobalScale(long j, Fyuse fyuse, float f);

    public static final native void Fyuse_setGravityX(long j, Fyuse fyuse, float f);

    public static final native void Fyuse_setGravityY(long j, Fyuse fyuse, float f);

    public static final native void Fyuse_setListOfAdjustmentFilterParameters(long j, Fyuse fyuse, String str);

    public static final native void Fyuse_setListOfAdjustmentFilters(long j, Fyuse fyuse, String str);

    public static final native void Fyuse_setLoopClosed(long j, Fyuse fyuse, boolean z);

    public static final native void Fyuse_setLoopClosedEndFrame(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_setLoopClosedStartFrame(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_setMotionFile(long j, Fyuse fyuse, String str);

    public static final native void Fyuse_setNumberOfCameraFrames(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_setNumberOfMotionFrames(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_setNumberOfProcessedFrames(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_setNumberOfStabilizedFrames(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_setOffsetsFile(long j, Fyuse fyuse, String str);

    public static final native void Fyuse_setPlacemark(long j, Fyuse fyuse, long j2, FyusePlacemark fyusePlacemark);

    public static final native void Fyuse_setProcessedFile(long j, Fyuse fyuse, String str);

    public static final native void Fyuse_setProcessedSize(long j, Fyuse fyuse, FyuseSize fyuseSize);

    public static final native void Fyuse_setSelectedFilterID(long j, Fyuse fyuse, String str);

    public static final native void Fyuse_setSelectedFilterParameter(long j, Fyuse fyuse, String str);

    public static final native void Fyuse_setSlice(long j, Fyuse fyuse, int i, long j2, FyuseSlice fyuseSlice);

    public static final native void Fyuse_setSliceInformation(long j, Fyuse fyuse, long j2, FyuseSliceVec fyuseSliceVec);

    public static final native void Fyuse_setStabilizationDataFrameOffset(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_setStartFrame(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_setThumbnailIndex(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_setUniqueDeviceID(long j, Fyuse fyuse, String str);

    public static final native void Fyuse_setUploadedInSlicedForm(long j, Fyuse fyuse, boolean z);

    public static final native void Fyuse_setVersion(long j, Fyuse fyuse, int i);

    public static final native void Fyuse_setVideoEncodingCode(long j, Fyuse fyuse, String str);

    public static final native void Fyuse_setWasRecordedInNightMode(long j, Fyuse fyuse, boolean z);

    public static final native void Fyuse_setWasRecordedInSelfiePanoramaMode(long j, Fyuse fyuse, boolean z);

    public static final native void Fyuse_setWhetherRecordedWithFrontCamera(long j, Fyuse fyuse, boolean z);

    public static final native int Fyuse_stabilization_smoothing_iterations__get(long j, Fyuse fyuse);

    public static final native void Fyuse_stabilization_smoothing_iterations__set(long j, Fyuse fyuse, int i);

    public static final native int Fyuse_stabilization_smoothing_range__get(long j, Fyuse fyuse);

    public static final native void Fyuse_stabilization_smoothing_range__set(long j, Fyuse fyuse, int i);

    public static final native long Fyuse_user_swipe_end__get(long j, Fyuse fyuse);

    public static final native void Fyuse_user_swipe_end__set(long j, Fyuse fyuse, long j2);

    public static final native long Fyuse_user_swipe_start__get(long j, Fyuse fyuse);

    public static final native void Fyuse_user_swipe_start__set(long j, Fyuse fyuse, long j2);

    public static final native boolean Fyuse_wasRecordedInNightMode(long j, Fyuse fyuse);

    public static final native boolean Fyuse_wasRecordedInSelfiePanoramaMode(long j, Fyuse fyuse);

    public static final native boolean Fyuse_wasRecordedUsingFrontCamera(long j, Fyuse fyuse);

    public static final native boolean Fyuse_write_mjpeg_fyuse_using_gpu__get(long j, Fyuse fyuse);

    public static final native void Fyuse_write_mjpeg_fyuse_using_gpu__set(long j, Fyuse fyuse, boolean z);

    public static final native int IntPair_first_get(long j, IntPair intPair);

    public static final native void IntPair_first_set(long j, IntPair intPair, int i);

    public static final native int IntPair_second_get(long j, IntPair intPair);

    public static final native void IntPair_second_set(long j, IntPair intPair, int i);

    public static final native void IntVec_add(long j, IntVec intVec, int i);

    public static final native long IntVec_capacity(long j, IntVec intVec);

    public static final native void IntVec_clear(long j, IntVec intVec);

    public static final native int IntVec_get(long j, IntVec intVec, int i);

    public static final native boolean IntVec_isEmpty(long j, IntVec intVec);

    public static final native void IntVec_reserve(long j, IntVec intVec, long j2);

    public static final native void IntVec_set(long j, IntVec intVec, int i, int i2);

    public static final native long IntVec_size(long j, IntVec intVec);

    public static final native void OfflineImageStabilizer_computeISDForNonDroppedFrames(long j, OfflineImageStabilizer offlineImageStabilizer, long j2, long j3, BoolVec boolVec, boolean z, int i, long j4);

    public static final native long OfflineImageStabilizer_computeTransformationParametersUsingOpenCV(long j, OfflineImageStabilizer offlineImageStabilizer, long j2, long j3);

    public static final native void OfflineImageStabilizer_computeVisualizationMesh(long j, OfflineImageStabilizer offlineImageStabilizer, long j2, long j3, VisualizationMeshStorage visualizationMeshStorage);

    public static final native void OfflineImageStabilizer_drawMeshOnBitmap(byte[] bArr, int i, int i2, int i3, long j, VisualizationMeshStorage visualizationMeshStorage, int i4, boolean z);

    public static final native void OfflineImageStabilizer_drawMeshOnBitmapUsingISD(byte[] bArr, int i, int i2, int i3, String str, int i4, boolean z);

    public static final native long OfflineImageStabilizer_estimateProcessedSize(long j, OfflineImageStabilizer offlineImageStabilizer, long j2, Fyuse fyuse);

    public static final native void OfflineImageStabilizer_updateDroppedFramesBasedOnOnlineIMUSelection(long j, OfflineImageStabilizer offlineImageStabilizer, long j2, Fyuse fyuse, long j3, BoolVec boolVec);

    public static final native void OfflineImageStabilizer_updateTransformations(long j, OfflineImageStabilizer offlineImageStabilizer, long j2, int i, int i2, long j3, BoolVec boolVec, long j4);

    public static final native void TransformationParametersVec_add(long j, TransformationParametersVec transformationParametersVec, long j2);

    public static final native long TransformationParametersVec_capacity(long j, TransformationParametersVec transformationParametersVec);

    public static final native void TransformationParametersVec_clear(long j, TransformationParametersVec transformationParametersVec);

    public static final native long TransformationParametersVec_get(long j, TransformationParametersVec transformationParametersVec, int i);

    public static final native boolean TransformationParametersVec_isEmpty(long j, TransformationParametersVec transformationParametersVec);

    public static final native void TransformationParametersVec_reserve(long j, TransformationParametersVec transformationParametersVec, long j2);

    public static final native void TransformationParametersVec_set(long j, TransformationParametersVec transformationParametersVec, int i, long j2, TransformationParameters transformationParameters);

    public static final native long TransformationParametersVec_size(long j, TransformationParametersVec transformationParametersVec);

    public static final native void VideoEncoder_createEncoder(long j, VideoEncoder videoEncoder, String str, int i, int i2, int i3, int i4);

    public static final native void VideoEncoder_destroyEncoder(long j, VideoEncoder videoEncoder);

    public static final native boolean VideoEncoder_encodeFrame(long j, VideoEncoder videoEncoder, byte[] bArr, int i, int i2, int i3, boolean z);

    public static final native void VideoExporter_renderLogoIntoFrame(long j, int i, int i2, int i3, long j2, int i4, int i5, int i6, int i7, int i8);

    public static final native void VideoExporter_renderRGBALogoIntoARGBFrame(long j, int i, int i2, int i3, long j2, int i4, int i5, int i6, int i7, int i8);

    public static final native boolean VideoExporter_selectFramesForTweenedVideoOfLength__SWIG_0(long j, VideoExporter videoExporter, int i, int i2, int i3, long j2, FloatVec floatVec, int i4);

    public static final native boolean VideoExporter_selectFramesForTweenedVideoOfLength__SWIG_1(long j, VideoExporter videoExporter, int i, int i2, int i3, long j2, FloatVec floatVec);

    public static final native boolean VideoExporter_selectFramesForVideoOfLength__SWIG_0(long j, VideoExporter videoExporter, int i, int i2, int i3, long j2, IntVec intVec, boolean z);

    public static final native boolean VideoExporter_selectFramesForVideoOfLength__SWIG_1(long j, VideoExporter videoExporter, int i, int i2, int i3, long j2, IntVec intVec);

    public static final native boolean VideoExporter_selectFramesForVideoOfLength__SWIG_2(long j, VideoExporter videoExporter, int i, int i2, int i3, long j2, IntVec intVec, long j3, IntVec intVec2, boolean z);

    public static final native boolean VideoExporter_selectFramesForVideoOfLength__SWIG_3(long j, VideoExporter videoExporter, int i, int i2, int i3, long j2, IntVec intVec, long j3, IntVec intVec2);

    public static final native void VisualizationMeshFrameDataVec_add(long j, VisualizationMeshFrameDataVec visualizationMeshFrameDataVec, long j2, VisualizationMeshFrameData visualizationMeshFrameData);

    public static final native long VisualizationMeshFrameDataVec_capacity(long j, VisualizationMeshFrameDataVec visualizationMeshFrameDataVec);

    public static final native void VisualizationMeshFrameDataVec_clear(long j, VisualizationMeshFrameDataVec visualizationMeshFrameDataVec);

    public static final native long VisualizationMeshFrameDataVec_get(long j, VisualizationMeshFrameDataVec visualizationMeshFrameDataVec, int i);

    public static final native boolean VisualizationMeshFrameDataVec_isEmpty(long j, VisualizationMeshFrameDataVec visualizationMeshFrameDataVec);

    public static final native void VisualizationMeshFrameDataVec_reserve(long j, VisualizationMeshFrameDataVec visualizationMeshFrameDataVec, long j2);

    public static final native void VisualizationMeshFrameDataVec_set(long j, VisualizationMeshFrameDataVec visualizationMeshFrameDataVec, int i, long j2, VisualizationMeshFrameData visualizationMeshFrameData);

    public static final native long VisualizationMeshFrameDataVec_size(long j, VisualizationMeshFrameDataVec visualizationMeshFrameDataVec);

    public static final native long VisualizationMeshFrameData_edge_pair_indices_get(long j, VisualizationMeshFrameData visualizationMeshFrameData);

    public static final native void VisualizationMeshFrameData_edge_pair_indices_set(long j, VisualizationMeshFrameData visualizationMeshFrameData, long j2, IntVec intVec);

    public static final native long VisualizationMeshFrameData_mesh_point_coordinates_get(long j, VisualizationMeshFrameData visualizationMeshFrameData);

    public static final native void VisualizationMeshFrameData_mesh_point_coordinates_set(long j, VisualizationMeshFrameData visualizationMeshFrameData, long j2, FloatVec floatVec);

    public static final native long VisualizationMeshStorage_frames_get(long j, VisualizationMeshStorage visualizationMeshStorage);

    public static final native void VisualizationMeshStorage_frames_set(long j, VisualizationMeshStorage visualizationMeshStorage, long j2, VisualizationMeshFrameDataVec visualizationMeshFrameDataVec);

    public static final native void delete_BoolVec(long j);

    public static final native void delete_CVTransform(long j);

    public static final native void delete_Date(long j);

    public static final native void delete_FloatVec(long j);

    public static final native void delete_FrameBlender(long j);

    public static final native void delete_Fyuse(long j);

    public static final native void delete_FyuseAddress(long j);

    public static final native void delete_FyuseBlacklistEntry(long j);

    public static final native void delete_FyuseFrameInformation(long j);

    public static final native void delete_FyuseFrameInformationVec(long j);

    public static final native void delete_FyusePlacemark(long j);

    public static final native void delete_FyuseSlice(long j);

    public static final native void delete_FyuseSliceVec(long j);

    public static final native void delete_IntPair(long j);

    public static final native void delete_IntVec(long j);

    public static final native void delete_OfflineImageStabilizer(long j);

    public static final native void delete_TransformationParametersVec(long j);

    public static final native void delete_VideoEncoder(long j);

    public static final native void delete_VideoExporter(long j);

    public static final native void delete_VisualizationMeshFrameData(long j);

    public static final native void delete_VisualizationMeshFrameDataVec(long j);

    public static final native void delete_VisualizationMeshStorage(long j);

    public static final native void getAngleAxis(long j, FloatVec floatVec, long j2, FloatVec floatVec2);

    public static final native String narrow(String str);

    public static final native long new_BoolVec__SWIG_0();

    public static final native long new_BoolVec__SWIG_1(long j);

    public static final native long new_CVTransform();

    public static final native long new_Date();

    public static final native long new_FloatVec__SWIG_0();

    public static final native long new_FloatVec__SWIG_1(long j);

    public static final native long new_FrameBlender();

    public static final native long new_Fyuse();

    public static final native long new_FyuseAddress();

    public static final native long new_FyuseBlacklistEntry();

    public static final native long new_FyuseFrameInformation();

    public static final native long new_FyuseFrameInformationVec__SWIG_0();

    public static final native long new_FyuseFrameInformationVec__SWIG_1(long j);

    public static final native long new_FyusePlacemark();

    public static final native long new_FyuseSlice();

    public static final native long new_FyuseSliceVec__SWIG_0();

    public static final native long new_FyuseSliceVec__SWIG_1(long j);

    public static final native long new_IntPair__SWIG_0();

    public static final native long new_IntPair__SWIG_1(int i, int i2);

    public static final native long new_IntPair__SWIG_2(long j, IntPair intPair);

    public static final native long new_IntVec__SWIG_0();

    public static final native long new_IntVec__SWIG_1(long j);

    public static final native long new_OfflineImageStabilizer();

    public static final native long new_TransformationParametersVec__SWIG_0();

    public static final native long new_TransformationParametersVec__SWIG_1(long j);

    public static final native long new_VideoEncoder();

    public static final native long new_VideoExporter();

    public static final native long new_VisualizationMeshFrameData();

    public static final native long new_VisualizationMeshFrameDataVec__SWIG_0();

    public static final native long new_VisualizationMeshFrameDataVec__SWIG_1(long j);

    public static final native long new_VisualizationMeshStorage();
}
