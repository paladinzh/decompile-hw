package fyusion.vislib;

/* compiled from: Unknown */
public class Fyuse {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public Fyuse() {
        this(FyuseWrapperJNI.new_Fyuse(), true);
    }

    protected Fyuse(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    public static void deleteView(String str) {
        FyuseWrapperJNI.Fyuse_deleteView(str);
    }

    protected static long getCPtr(Fyuse fyuse) {
        return fyuse != null ? fyuse.swigCPtr : 0;
    }

    public static String getView() {
        return FyuseWrapperJNI.Fyuse_getView();
    }

    public void addFrameInformation(FyuseFrameInformation fyuseFrameInformation) {
        FyuseWrapperJNI.Fyuse_addFrameInformation(this.swigCPtr, this, FyuseFrameInformation.getCPtr(fyuseFrameInformation), fyuseFrameInformation);
    }

    public void addSlice(FyuseSlice fyuseSlice) {
        FyuseWrapperJNI.Fyuse_addSlice(this.swigCPtr, this, FyuseSlice.getCPtr(fyuseSlice), fyuseSlice);
    }

    public void clearCaches() {
        FyuseWrapperJNI.Fyuse_clearCaches(this.swigCPtr, this);
    }

    public void clearFrameInformations() {
        FyuseWrapperJNI.Fyuse_clearFrameInformations(this.swigCPtr, this);
    }

    public void clearSlices() {
        FyuseWrapperJNI.Fyuse_clearSlices(this.swigCPtr, this);
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_Fyuse(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public String getAppVersionUsedToRecord() {
        return FyuseWrapperJNI.Fyuse_getAppVersionUsedToRecord(this.swigCPtr, this);
    }

    public String getAppVersionUsedToUpload() {
        return FyuseWrapperJNI.Fyuse_getAppVersionUsedToUpload(this.swigCPtr, this);
    }

    public boolean getApply_face_detection_for_stabilization_() {
        return FyuseWrapperJNI.Fyuse_apply_face_detection_for_stabilization__get(this.swigCPtr, this);
    }

    public float getAverage_iso_value_() {
        return FyuseWrapperJNI.Fyuse_average_iso_value__get(this.swigCPtr, this);
    }

    public String getCameraFile() {
        return FyuseWrapperJNI.Fyuse_getCameraFile(this.swigCPtr, this);
    }

    public int getCameraOrientation() {
        return FyuseWrapperJNI.Fyuse_getCameraOrientation(this.swigCPtr, this);
    }

    public FyuseSize getCameraSize() {
        return FyuseWrapperJNI.Fyuse_getCameraSize(this.swigCPtr, this);
    }

    public int getCamera_fps_indicator_frame_window_size_() {
        return FyuseWrapperJNI.Fyuse_camera_fps_indicator_frame_window_size__get(this.swigCPtr, this);
    }

    public Date getCreationDate() {
        return new Date(FyuseWrapperJNI.Fyuse_getCreationDate(this.swigCPtr, this), false);
    }

    public String getCurrentFilterID() {
        return FyuseWrapperJNI.Fyuse_getCurrentFilterID(this.swigCPtr, this);
    }

    public int getCurrent_version_number_() {
        return FyuseWrapperJNI.Fyuse_current_version_number__get(this.swigCPtr, this);
    }

    public float getCurvature() {
        return FyuseWrapperJNI.Fyuse_getCurvature(this.swigCPtr, this);
    }

    public String getDeviceID() {
        return FyuseWrapperJNI.Fyuse_getDeviceID(this.swigCPtr, this);
    }

    public float getDirectionX() {
        return FyuseWrapperJNI.Fyuse_getDirectionX(this.swigCPtr, this);
    }

    public float getDirectionY() {
        return FyuseWrapperJNI.Fyuse_getDirectionY(this.swigCPtr, this);
    }

    public boolean getEnable_standard_unsharpen_filter_() {
        return FyuseWrapperJNI.Fyuse_enable_standard_unsharpen_filter__get(this.swigCPtr, this);
    }

    public int getEndFrame() {
        return FyuseWrapperJNI.Fyuse_getEndFrame(this.swigCPtr, this);
    }

    public boolean getFixed_last_nil_frame_error_() {
        return FyuseWrapperJNI.Fyuse_fixed_last_nil_frame_error__get(this.swigCPtr, this);
    }

    public FyuseFrameInformation getFrameInformation(int i) {
        return new FyuseFrameInformation(FyuseWrapperJNI.Fyuse_getFrameInformation(this.swigCPtr, this, i), false);
    }

    public int getFrameSelectionType() {
        return FyuseWrapperJNI.Fyuse_getFrameSelectionType(this.swigCPtr, this);
    }

    public FyuseFrameInformationVec getFrameTimestamps() {
        return new FyuseFrameInformationVec(FyuseWrapperJNI.Fyuse_getFrameTimestamps(this.swigCPtr, this), false);
    }

    public float getFyuse_quality_() {
        return FyuseWrapperJNI.Fyuse_fyuse_quality__get(this.swigCPtr, this);
    }

    public double getGlobalScale() {
        return FyuseWrapperJNI.Fyuse_getGlobalScale(this.swigCPtr, this);
    }

    public float getGravityX() {
        return FyuseWrapperJNI.Fyuse_getGravityX(this.swigCPtr, this);
    }

    public float getGravityY() {
        return FyuseWrapperJNI.Fyuse_getGravityY(this.swigCPtr, this);
    }

    public int getH264_recording_video_fps_() {
        return FyuseWrapperJNI.Fyuse_h264_recording_video_fps__get(this.swigCPtr, this);
    }

    public int getH264_upload_video_fps_() {
        return FyuseWrapperJNI.Fyuse_h264_upload_video_fps__get(this.swigCPtr, this);
    }

    public boolean getHave_user_input_for_stabilization_() {
        return FyuseWrapperJNI.Fyuse_have_user_input_for_stabilization__get(this.swigCPtr, this);
    }

    public float getIMUDirectionX() {
        return FyuseWrapperJNI.Fyuse_getIMUDirectionX(this.swigCPtr, this);
    }

    public float getIMUDirectionY() {
        return FyuseWrapperJNI.Fyuse_getIMUDirectionY(this.swigCPtr, this);
    }

    public String getListOfAdjustmentFilterParameters() {
        return FyuseWrapperJNI.Fyuse_getListOfAdjustmentFilterParameters(this.swigCPtr, this);
    }

    public String getListOfAdjustmentFilters() {
        return FyuseWrapperJNI.Fyuse_getListOfAdjustmentFilters(this.swigCPtr, this);
    }

    public int getLoopClosedEndFrame() {
        return FyuseWrapperJNI.Fyuse_getLoopClosedEndFrame(this.swigCPtr, this);
    }

    public int getLoopClosedStartFrame() {
        return FyuseWrapperJNI.Fyuse_getLoopClosedStartFrame(this.swigCPtr, this);
    }

    public boolean getLoop_closed_() {
        return FyuseWrapperJNI.Fyuse_loop_closed__get(this.swigCPtr, this);
    }

    public int getLoop_closed_end_frame_() {
        return FyuseWrapperJNI.Fyuse_loop_closed_end_frame__get(this.swigCPtr, this);
    }

    public int getLoop_closed_start_frame_() {
        return FyuseWrapperJNI.Fyuse_loop_closed_start_frame__get(this.swigCPtr, this);
    }

    public int getMax_low_fps_iso_value_() {
        return FyuseWrapperJNI.Fyuse_max_low_fps_iso_value__get(this.swigCPtr, this);
    }

    public int getMax_number_frames_() {
        return FyuseWrapperJNI.Fyuse_max_number_frames__get(this.swigCPtr, this);
    }

    public int getMax_ready_for_more_media_data_check_fails_() {
        return FyuseWrapperJNI.Fyuse_max_ready_for_more_media_data_check_fails__get(this.swigCPtr, this);
    }

    public float getMax_stabilization_angle_() {
        return FyuseWrapperJNI.Fyuse_max_stabilization_angle__get(this.swigCPtr, this);
    }

    public int getMax_stabilization_motion_() {
        return FyuseWrapperJNI.Fyuse_max_stabilization_motion__get(this.swigCPtr, this);
    }

    public int getMin_high_fps_iso_value_() {
        return FyuseWrapperJNI.Fyuse_min_high_fps_iso_value__get(this.swigCPtr, this);
    }

    public int getMjpeg_video_fps_() {
        return FyuseWrapperJNI.Fyuse_mjpeg_video_fps__get(this.swigCPtr, this);
    }

    public String getMotionFile() {
        return FyuseWrapperJNI.Fyuse_getMotionFile(this.swigCPtr, this);
    }

    public boolean getNight_mode_() {
        return FyuseWrapperJNI.Fyuse_night_mode__get(this.swigCPtr, this);
    }

    public int getNum_frames_to_crop_() {
        return FyuseWrapperJNI.Fyuse_num_frames_to_crop__get(this.swigCPtr, this);
    }

    public int getNumberOfCameraFrames() {
        return FyuseWrapperJNI.Fyuse_getNumberOfCameraFrames(this.swigCPtr, this);
    }

    public int getNumberOfFrameInformations() {
        return FyuseWrapperJNI.Fyuse_getNumberOfFrameInformations(this.swigCPtr, this);
    }

    public int getNumberOfMotionFrames() {
        return FyuseWrapperJNI.Fyuse_getNumberOfMotionFrames(this.swigCPtr, this);
    }

    public int getNumberOfProcessedFrames() {
        return FyuseWrapperJNI.Fyuse_getNumberOfProcessedFrames(this.swigCPtr, this);
    }

    public int getNumberOfSlices() {
        return FyuseWrapperJNI.Fyuse_getNumberOfSlices(this.swigCPtr, this);
    }

    public int getNumberOfStabilizedFrames() {
        return FyuseWrapperJNI.Fyuse_getNumberOfStabilizedFrames(this.swigCPtr, this);
    }

    public String getOffsetsFile() {
        return FyuseWrapperJNI.Fyuse_getOffsetsFile(this.swigCPtr, this);
    }

    public FyusePlacemark getPlacemark() {
        return new FyusePlacemark(FyuseWrapperJNI.Fyuse_getPlacemark(this.swigCPtr, this), false);
    }

    public int getPost_processing_width_() {
        return FyuseWrapperJNI.Fyuse_post_processing_width__get(this.swigCPtr, this);
    }

    public String getProcessedFile() {
        return FyuseWrapperJNI.Fyuse_getProcessedFile(this.swigCPtr, this);
    }

    public FyuseSize getProcessedSize() {
        return FyuseWrapperJNI.Fyuse_getProcessedSize(this.swigCPtr, this);
    }

    public float getReady_for_more_media_data_sleep_time_() {
        return FyuseWrapperJNI.Fyuse_ready_for_more_media_data_sleep_time__get(this.swigCPtr, this);
    }

    public String getSelectedFilterID() {
        return FyuseWrapperJNI.Fyuse_getSelectedFilterID(this.swigCPtr, this);
    }

    public String getSelectedFilterParameter() {
        return FyuseWrapperJNI.Fyuse_getSelectedFilterParameter(this.swigCPtr, this);
    }

    public FyuseSlice getSlice(int i) {
        return new FyuseSlice(FyuseWrapperJNI.Fyuse_getSlice(this.swigCPtr, this, i), false);
    }

    public FyuseSliceVec getSliceInformation() {
        return new FyuseSliceVec(FyuseWrapperJNI.Fyuse_getSliceInformation(this.swigCPtr, this), false);
    }

    public int getStabilizationDataFrameOffset() {
        return FyuseWrapperJNI.Fyuse_getStabilizationDataFrameOffset(this.swigCPtr, this);
    }

    public int getStabilization_smoothing_iterations_() {
        return FyuseWrapperJNI.Fyuse_stabilization_smoothing_iterations__get(this.swigCPtr, this);
    }

    public int getStabilization_smoothing_range_() {
        return FyuseWrapperJNI.Fyuse_stabilization_smoothing_range__get(this.swigCPtr, this);
    }

    public int getStartFrame() {
        return FyuseWrapperJNI.Fyuse_getStartFrame(this.swigCPtr, this);
    }

    public float getSwipeDirectionX() {
        return FyuseWrapperJNI.Fyuse_getSwipeDirectionX(this.swigCPtr, this);
    }

    public float getSwipeDirectionY() {
        return FyuseWrapperJNI.Fyuse_getSwipeDirectionY(this.swigCPtr, this);
    }

    public int getThumbnailIndex() {
        return FyuseWrapperJNI.Fyuse_getThumbnailIndex(this.swigCPtr, this);
    }

    public String getUniqueDeviceID() {
        return FyuseWrapperJNI.Fyuse_getUniqueDeviceID(this.swigCPtr, this);
    }

    public SWIGTYPE_p_cv__Point2f getUser_swipe_end_() {
        return new SWIGTYPE_p_cv__Point2f(FyuseWrapperJNI.Fyuse_user_swipe_end__get(this.swigCPtr, this), true);
    }

    public SWIGTYPE_p_cv__Point2f getUser_swipe_start_() {
        return new SWIGTYPE_p_cv__Point2f(FyuseWrapperJNI.Fyuse_user_swipe_start__get(this.swigCPtr, this), true);
    }

    public int getVersion() {
        return FyuseWrapperJNI.Fyuse_getVersion(this.swigCPtr, this);
    }

    public String getVideoEncodingCode() {
        return FyuseWrapperJNI.Fyuse_getVideoEncodingCode(this.swigCPtr, this);
    }

    public boolean getWrite_mjpeg_fyuse_using_gpu_() {
        return FyuseWrapperJNI.Fyuse_write_mjpeg_fyuse_using_gpu__get(this.swigCPtr, this);
    }

    public boolean hasBeenUploadedInSlicedForm() {
        return FyuseWrapperJNI.Fyuse_hasBeenUploadedInSlicedForm(this.swigCPtr, this);
    }

    public void insertFrameInformation(FyuseFrameInformation fyuseFrameInformation, int i) {
        FyuseWrapperJNI.Fyuse_insertFrameInformation(this.swigCPtr, this, FyuseFrameInformation.getCPtr(fyuseFrameInformation), fyuseFrameInformation, i);
    }

    public void insertSlice(FyuseSlice fyuseSlice, int i) {
        FyuseWrapperJNI.Fyuse_insertSlice(this.swigCPtr, this, FyuseSlice.getCPtr(fyuseSlice), fyuseSlice, i);
    }

    public boolean isConvex() {
        return FyuseWrapperJNI.Fyuse_isConvex(this.swigCPtr, this);
    }

    public boolean isLoopClosed() {
        return FyuseWrapperJNI.Fyuse_isLoopClosed(this.swigCPtr, this);
    }

    public boolean isPortrait() {
        return FyuseWrapperJNI.Fyuse_isPortrait(this.swigCPtr, this);
    }

    public boolean loadFromMagicFile(String str) {
        return FyuseWrapperJNI.Fyuse_loadFromMagicFile(this.swigCPtr, this, str);
    }

    public boolean loadFromXMLString(String str) {
        return FyuseWrapperJNI.Fyuse_loadFromXMLString(this.swigCPtr, this, str);
    }

    public void printToConsole() {
        FyuseWrapperJNI.Fyuse_printToConsole(this.swigCPtr, this);
    }

    public void removeAllSlices() {
        FyuseWrapperJNI.Fyuse_removeAllSlices(this.swigCPtr, this);
    }

    public void removeFrameInformation(int i) {
        FyuseWrapperJNI.Fyuse_removeFrameInformation(this.swigCPtr, this, i);
    }

    public void removeSlice(int i) {
        FyuseWrapperJNI.Fyuse_removeSlice(this.swigCPtr, this, i);
    }

    public boolean saveToMagicFile(String str) {
        return FyuseWrapperJNI.Fyuse_saveToMagicFile(this.swigCPtr, this, str);
    }

    public boolean saveToXMLFile(String str) {
        return FyuseWrapperJNI.Fyuse_saveToXMLFile(this.swigCPtr, this, str);
    }

    public String saveToXMLString() {
        return FyuseWrapperJNI.Fyuse_saveToXMLString(this.swigCPtr, this);
    }

    public void setAppVersionUsedToRecord(String str) {
        FyuseWrapperJNI.Fyuse_setAppVersionUsedToRecord(this.swigCPtr, this, str);
    }

    public void setAppVersionUsedToUpload(String str) {
        FyuseWrapperJNI.Fyuse_setAppVersionUsedToUpload(this.swigCPtr, this, str);
    }

    public void setApply_face_detection_for_stabilization_(boolean z) {
        FyuseWrapperJNI.Fyuse_apply_face_detection_for_stabilization__set(this.swigCPtr, this, z);
    }

    public void setAverage_iso_value_(float f) {
        FyuseWrapperJNI.Fyuse_average_iso_value__set(this.swigCPtr, this, f);
    }

    public void setCameraFile(String str) {
        FyuseWrapperJNI.Fyuse_setCameraFile(this.swigCPtr, this, str);
    }

    public void setCameraOrientation(int i) {
        FyuseWrapperJNI.Fyuse_setCameraOrientation(this.swigCPtr, this, i);
    }

    public void setCameraSize(FyuseSize fyuseSize) {
        FyuseWrapperJNI.Fyuse_setCameraSize(this.swigCPtr, this, fyuseSize);
    }

    public void setCamera_fps_indicator_frame_window_size_(int i) {
        FyuseWrapperJNI.Fyuse_camera_fps_indicator_frame_window_size__set(this.swigCPtr, this, i);
    }

    public void setCreationData(Date date) {
        FyuseWrapperJNI.Fyuse_setCreationData(this.swigCPtr, this, Date.getCPtr(date), date);
    }

    public void setCurrentFilterID(String str) {
        FyuseWrapperJNI.Fyuse_setCurrentFilterID(this.swigCPtr, this, str);
    }

    public void setCurrent_version_number_(int i) {
        FyuseWrapperJNI.Fyuse_current_version_number__set(this.swigCPtr, this, i);
    }

    public void setCurvature(float f) {
        FyuseWrapperJNI.Fyuse_setCurvature(this.swigCPtr, this, f);
    }

    public void setDefaultValues() {
        FyuseWrapperJNI.Fyuse_setDefaultValues(this.swigCPtr, this);
    }

    public void setDeviceID(String str) {
        FyuseWrapperJNI.Fyuse_setDeviceID(this.swigCPtr, this, str);
    }

    public void setDirectionX(float f) {
        FyuseWrapperJNI.Fyuse_setDirectionX(this.swigCPtr, this, f);
    }

    public void setDirectionY(float f) {
        FyuseWrapperJNI.Fyuse_setDirectionY(this.swigCPtr, this, f);
    }

    public void setEnable_standard_unsharpen_filter_(boolean z) {
        FyuseWrapperJNI.Fyuse_enable_standard_unsharpen_filter__set(this.swigCPtr, this, z);
    }

    public void setEndFrame(int i) {
        FyuseWrapperJNI.Fyuse_setEndFrame(this.swigCPtr, this, i);
    }

    public void setFixed_last_nil_frame_error_(boolean z) {
        FyuseWrapperJNI.Fyuse_fixed_last_nil_frame_error__set(this.swigCPtr, this, z);
    }

    public void setFrameInformation(int i, FyuseFrameInformation fyuseFrameInformation) {
        FyuseWrapperJNI.Fyuse_setFrameInformation(this.swigCPtr, this, i, FyuseFrameInformation.getCPtr(fyuseFrameInformation), fyuseFrameInformation);
    }

    public void setFrameSelectionType(int i) {
        FyuseWrapperJNI.Fyuse_setFrameSelectionType(this.swigCPtr, this, i);
    }

    public void setFrameTimestamps(FyuseFrameInformationVec fyuseFrameInformationVec) {
        FyuseWrapperJNI.Fyuse_setFrameTimestamps(this.swigCPtr, this, FyuseFrameInformationVec.getCPtr(fyuseFrameInformationVec), fyuseFrameInformationVec);
    }

    public void setFyuse_quality_(float f) {
        FyuseWrapperJNI.Fyuse_fyuse_quality__set(this.swigCPtr, this, f);
    }

    public void setGlobalScale(float f) {
        FyuseWrapperJNI.Fyuse_setGlobalScale(this.swigCPtr, this, f);
    }

    public void setGravityX(float f) {
        FyuseWrapperJNI.Fyuse_setGravityX(this.swigCPtr, this, f);
    }

    public void setGravityY(float f) {
        FyuseWrapperJNI.Fyuse_setGravityY(this.swigCPtr, this, f);
    }

    public void setH264_recording_video_fps_(int i) {
        FyuseWrapperJNI.Fyuse_h264_recording_video_fps__set(this.swigCPtr, this, i);
    }

    public void setH264_upload_video_fps_(int i) {
        FyuseWrapperJNI.Fyuse_h264_upload_video_fps__set(this.swigCPtr, this, i);
    }

    public void setHave_user_input_for_stabilization_(boolean z) {
        FyuseWrapperJNI.Fyuse_have_user_input_for_stabilization__set(this.swigCPtr, this, z);
    }

    public void setListOfAdjustmentFilterParameters(String str) {
        FyuseWrapperJNI.Fyuse_setListOfAdjustmentFilterParameters(this.swigCPtr, this, str);
    }

    public void setListOfAdjustmentFilters(String str) {
        FyuseWrapperJNI.Fyuse_setListOfAdjustmentFilters(this.swigCPtr, this, str);
    }

    public void setLoopClosed(boolean z) {
        FyuseWrapperJNI.Fyuse_setLoopClosed(this.swigCPtr, this, z);
    }

    public void setLoopClosedEndFrame(int i) {
        FyuseWrapperJNI.Fyuse_setLoopClosedEndFrame(this.swigCPtr, this, i);
    }

    public void setLoopClosedStartFrame(int i) {
        FyuseWrapperJNI.Fyuse_setLoopClosedStartFrame(this.swigCPtr, this, i);
    }

    public void setLoop_closed_(boolean z) {
        FyuseWrapperJNI.Fyuse_loop_closed__set(this.swigCPtr, this, z);
    }

    public void setLoop_closed_end_frame_(int i) {
        FyuseWrapperJNI.Fyuse_loop_closed_end_frame__set(this.swigCPtr, this, i);
    }

    public void setLoop_closed_start_frame_(int i) {
        FyuseWrapperJNI.Fyuse_loop_closed_start_frame__set(this.swigCPtr, this, i);
    }

    public void setMax_low_fps_iso_value_(int i) {
        FyuseWrapperJNI.Fyuse_max_low_fps_iso_value__set(this.swigCPtr, this, i);
    }

    public void setMax_number_frames_(int i) {
        FyuseWrapperJNI.Fyuse_max_number_frames__set(this.swigCPtr, this, i);
    }

    public void setMax_ready_for_more_media_data_check_fails_(int i) {
        FyuseWrapperJNI.Fyuse_max_ready_for_more_media_data_check_fails__set(this.swigCPtr, this, i);
    }

    public void setMax_stabilization_angle_(float f) {
        FyuseWrapperJNI.Fyuse_max_stabilization_angle__set(this.swigCPtr, this, f);
    }

    public void setMax_stabilization_motion_(int i) {
        FyuseWrapperJNI.Fyuse_max_stabilization_motion__set(this.swigCPtr, this, i);
    }

    public void setMin_high_fps_iso_value_(int i) {
        FyuseWrapperJNI.Fyuse_min_high_fps_iso_value__set(this.swigCPtr, this, i);
    }

    public void setMjpeg_video_fps_(int i) {
        FyuseWrapperJNI.Fyuse_mjpeg_video_fps__set(this.swigCPtr, this, i);
    }

    public void setMotionFile(String str) {
        FyuseWrapperJNI.Fyuse_setMotionFile(this.swigCPtr, this, str);
    }

    public void setNight_mode_(boolean z) {
        FyuseWrapperJNI.Fyuse_night_mode__set(this.swigCPtr, this, z);
    }

    public void setNum_frames_to_crop_(int i) {
        FyuseWrapperJNI.Fyuse_num_frames_to_crop__set(this.swigCPtr, this, i);
    }

    public void setNumberOfCameraFrames(int i) {
        FyuseWrapperJNI.Fyuse_setNumberOfCameraFrames(this.swigCPtr, this, i);
    }

    public void setNumberOfMotionFrames(int i) {
        FyuseWrapperJNI.Fyuse_setNumberOfMotionFrames(this.swigCPtr, this, i);
    }

    public void setNumberOfProcessedFrames(int i) {
        FyuseWrapperJNI.Fyuse_setNumberOfProcessedFrames(this.swigCPtr, this, i);
    }

    public void setNumberOfStabilizedFrames(int i) {
        FyuseWrapperJNI.Fyuse_setNumberOfStabilizedFrames(this.swigCPtr, this, i);
    }

    public void setOffsetsFile(String str) {
        FyuseWrapperJNI.Fyuse_setOffsetsFile(this.swigCPtr, this, str);
    }

    public void setPlacemark(FyusePlacemark fyusePlacemark) {
        FyuseWrapperJNI.Fyuse_setPlacemark(this.swigCPtr, this, FyusePlacemark.getCPtr(fyusePlacemark), fyusePlacemark);
    }

    public void setPost_processing_width_(int i) {
        FyuseWrapperJNI.Fyuse_post_processing_width__set(this.swigCPtr, this, i);
    }

    public void setProcessedFile(String str) {
        FyuseWrapperJNI.Fyuse_setProcessedFile(this.swigCPtr, this, str);
    }

    public void setProcessedSize(FyuseSize fyuseSize) {
        FyuseWrapperJNI.Fyuse_setProcessedSize(this.swigCPtr, this, fyuseSize);
    }

    public void setReady_for_more_media_data_sleep_time_(float f) {
        FyuseWrapperJNI.Fyuse_ready_for_more_media_data_sleep_time__set(this.swigCPtr, this, f);
    }

    public void setSelectedFilterID(String str) {
        FyuseWrapperJNI.Fyuse_setSelectedFilterID(this.swigCPtr, this, str);
    }

    public void setSelectedFilterParameter(String str) {
        FyuseWrapperJNI.Fyuse_setSelectedFilterParameter(this.swigCPtr, this, str);
    }

    public void setSlice(int i, FyuseSlice fyuseSlice) {
        FyuseWrapperJNI.Fyuse_setSlice(this.swigCPtr, this, i, FyuseSlice.getCPtr(fyuseSlice), fyuseSlice);
    }

    public void setSliceInformation(FyuseSliceVec fyuseSliceVec) {
        FyuseWrapperJNI.Fyuse_setSliceInformation(this.swigCPtr, this, FyuseSliceVec.getCPtr(fyuseSliceVec), fyuseSliceVec);
    }

    public void setStabilizationDataFrameOffset(int i) {
        FyuseWrapperJNI.Fyuse_setStabilizationDataFrameOffset(this.swigCPtr, this, i);
    }

    public void setStabilization_smoothing_iterations_(int i) {
        FyuseWrapperJNI.Fyuse_stabilization_smoothing_iterations__set(this.swigCPtr, this, i);
    }

    public void setStabilization_smoothing_range_(int i) {
        FyuseWrapperJNI.Fyuse_stabilization_smoothing_range__set(this.swigCPtr, this, i);
    }

    public void setStartFrame(int i) {
        FyuseWrapperJNI.Fyuse_setStartFrame(this.swigCPtr, this, i);
    }

    public void setThumbnailIndex(int i) {
        FyuseWrapperJNI.Fyuse_setThumbnailIndex(this.swigCPtr, this, i);
    }

    public void setUniqueDeviceID(String str) {
        FyuseWrapperJNI.Fyuse_setUniqueDeviceID(this.swigCPtr, this, str);
    }

    public void setUploadedInSlicedForm(boolean z) {
        FyuseWrapperJNI.Fyuse_setUploadedInSlicedForm(this.swigCPtr, this, z);
    }

    public void setUser_swipe_end_(SWIGTYPE_p_cv__Point2f sWIGTYPE_p_cv__Point2f) {
        FyuseWrapperJNI.Fyuse_user_swipe_end__set(this.swigCPtr, this, SWIGTYPE_p_cv__Point2f.getCPtr(sWIGTYPE_p_cv__Point2f));
    }

    public void setUser_swipe_start_(SWIGTYPE_p_cv__Point2f sWIGTYPE_p_cv__Point2f) {
        FyuseWrapperJNI.Fyuse_user_swipe_start__set(this.swigCPtr, this, SWIGTYPE_p_cv__Point2f.getCPtr(sWIGTYPE_p_cv__Point2f));
    }

    public void setVersion(int i) {
        FyuseWrapperJNI.Fyuse_setVersion(this.swigCPtr, this, i);
    }

    public void setVideoEncodingCode(String str) {
        FyuseWrapperJNI.Fyuse_setVideoEncodingCode(this.swigCPtr, this, str);
    }

    public void setWasRecordedInNightMode(boolean z) {
        FyuseWrapperJNI.Fyuse_setWasRecordedInNightMode(this.swigCPtr, this, z);
    }

    public void setWasRecordedInSelfiePanoramaMode(boolean z) {
        FyuseWrapperJNI.Fyuse_setWasRecordedInSelfiePanoramaMode(this.swigCPtr, this, z);
    }

    public void setWhetherRecordedWithFrontCamera(boolean z) {
        FyuseWrapperJNI.Fyuse_setWhetherRecordedWithFrontCamera(this.swigCPtr, this, z);
    }

    public void setWrite_mjpeg_fyuse_using_gpu_(boolean z) {
        FyuseWrapperJNI.Fyuse_write_mjpeg_fyuse_using_gpu__set(this.swigCPtr, this, z);
    }

    public boolean wasRecordedInNightMode() {
        return FyuseWrapperJNI.Fyuse_wasRecordedInNightMode(this.swigCPtr, this);
    }

    public boolean wasRecordedInSelfiePanoramaMode() {
        return FyuseWrapperJNI.Fyuse_wasRecordedInSelfiePanoramaMode(this.swigCPtr, this);
    }

    public boolean wasRecordedUsingFrontCamera() {
        return FyuseWrapperJNI.Fyuse_wasRecordedUsingFrontCamera(this.swigCPtr, this);
    }
}
