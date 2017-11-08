package com.fyusion.sdk.common.ext;

import android.support.annotation.NonNull;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.FyuseSDK;
import com.fyusion.sdk.common.a;
import com.fyusion.sdk.common.ext.filter.ImageFilterAbstractFactory;
import com.fyusion.sdk.common.ext.util.b;
import com.huawei.watermark.manager.parse.WMElement;
import fyusion.vislib.AESUtils;
import fyusion.vislib.Fyuse;
import fyusion.vislib.FyuseFrameInformationVec;
import fyusion.vislib.FyuseSize;
import fyusion.vislib.FyuseSlice;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class e extends Fyuse {
    private static boolean g = false;
    boolean a;
    boolean b;
    boolean c;
    boolean d;
    List<Boolean> e = null;
    public List<Boolean> f;

    static {
        System.loadLibrary("vislib_jni");
    }

    public e() {
        a();
    }

    private void a() {
        this.f = new ArrayList();
        FyuseSize fyuseSize = new FyuseSize(0.0d, 0.0d);
        fyuseSize.height = 0.0d;
        fyuseSize.width = 0.0d;
        setCameraSize(fyuseSize);
        setCameraFile(j.ag);
        setNumberOfCameraFrames(0);
        setWhetherRecordedWithFrontCamera(false);
        setFrameTimestamps(new FyuseFrameInformationVec());
        setProcessedSize(fyuseSize);
        setNumberOfProcessedFrames(0);
        setProcessedFile(j.ar);
        setNumberOfStabilizedFrames(0);
        setOffsetsFile(j.ah);
        setStabilizationDataFrameOffset(0);
        setNumberOfMotionFrames(0);
        setMotionFile(j.ai);
        setDirectionX(WMElement.CAMERASIZEVALUE1B1);
        setDirectionY(0.0f);
        setGravityX(0.0f);
        setGravityY(WMElement.CAMERASIZEVALUE1B1);
        setLoopClosed(false);
        setStartFrame(0);
        setEndFrame(-1);
        setThumbnailIndex(0);
        setCurrentFilterID(ImageFilterAbstractFactory.NO_TONE_CURVE_FILTER);
        setSelectedFilterID(ImageFilterAbstractFactory.NO_TONE_CURVE_FILTER);
        setMax_number_frames_(300);
        setMax_ready_for_more_media_data_check_fails_(j.q);
        setCamera_fps_indicator_frame_window_size_(j.s);
        setPost_processing_width_(j.C);
        setStabilization_smoothing_iterations_(j.I);
        setStabilization_smoothing_range_(j.J);
        setMax_stabilization_motion_(j.K);
        setMax_low_fps_iso_value_(j.R);
        setMin_high_fps_iso_value_(j.S);
        setNum_frames_to_crop_(3);
        setCurrent_version_number_(j.V);
        setMjpeg_video_fps_(j.i);
        setH264_recording_video_fps_(j.j);
        setH264_upload_video_fps_(j.k);
        setReady_for_more_media_data_sleep_time_(j.r);
        setMax_stabilization_angle_(j.L);
        setFyuse_quality_(j.Q);
        setAverage_iso_value_(0.0f);
        setApply_face_detection_for_stabilization_(j.B);
        setWrite_mjpeg_fyuse_using_gpu_(j.E);
        setEnable_standard_unsharpen_filter_(j.X);
        setNight_mode_(false);
        setFrameSelectionType(0);
        String b = b.b();
        if (b != null) {
            setDeviceID(b);
        }
        setUniqueDeviceID(a.i());
        setAppVersionUsedToRecord(FyuseSDK.getVersion());
        this.a = false;
        this.b = false;
        this.c = false;
        this.d = false;
    }

    @NonNull
    private FyuseSlice b() {
        FyuseSlice fyuseSlice = new FyuseSlice();
        fyuseSlice.setIndex(0);
        fyuseSlice.setStart_frame(0);
        fyuseSlice.setEnd_frame(getNumberOfProcessedFrames() - 1);
        fyuseSlice.setMjpeg_file_name(getProcessedFile());
        fyuseSlice.setIndex_file_name(j.as);
        fyuseSlice.setH264_file_name(j.aw);
        return fyuseSlice;
    }

    @NonNull
    public FyuseSlice a(int i) {
        int i2 = 0;
        if (getNumberOfSlices() > 0) {
            if (g) {
                DLog.e("fyuseDebug", "looking for frame number: " + i);
            }
            while (i2 < getNumberOfSlices()) {
                FyuseSlice slice = getSlice(i2);
                if (g) {
                    DLog.e("fyuseDebug", "Slice: " + i2 + " start: " + slice.getStart_frame() + " end: " + slice.getEnd_frame());
                }
                if (i >= slice.getStart_frame() && i <= slice.getEnd_frame()) {
                    if (g) {
                        DLog.e("fyuseDebug", "found thumbnail frame: " + i + " in slice: " + i2);
                    }
                    return slice;
                }
                i2++;
            }
        }
        return b();
    }

    public void a(int i, int i2, int i3, int i4) {
        int i5 = i - i3;
        if (i2 < 0) {
            i2 = (getNumberOfProcessedFrames() - 1) - i5;
        }
        for (int i6 = 0; i6 < getNumberOfSlices(); i6++) {
            FyuseSlice slice = getSlice(i6);
            slice.setStart_frame(slice.getStart_frame() - i5);
            slice.setEnd_frame(slice.getEnd_frame() - i5);
            setSlice(i6, slice);
        }
        setStartFrame(getStartFrame() - i5);
        if (getEndFrame() >= 0) {
            setEndFrame(getEndFrame() - i5);
        }
        setStabilizationDataFrameOffset(i5);
        setThumbnailIndex(Math.max(0, getThumbnailIndex() - i5));
        setNumberOfProcessedFrames((i2 - i) + 1);
    }

    void a(String str, boolean z) {
        try {
            File file = new File(str);
            String str2 = "";
            FileInputStream fileInputStream = new FileInputStream(str);
            byte[] bArr = new byte[((int) file.length())];
            fileInputStream.read(bArr);
            fileInputStream.close();
            String str3 = new String(bArr);
            if (z) {
                str3 = b(str);
            }
            if (!loadFromXMLString(str3) && g) {
                DLog.e("readFromXMLFile", "XML file loading failed!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean a(String str) {
        String str2 = str + File.separator + "fyuse.xml";
        String str3 = str + File.separator + j.ae;
        if (new File(str3).exists()) {
            a(str3, true);
            if (g) {
                DLog.d("readFromFilePath", "Read in fyuse with numProcessed: " + getNumberOfProcessedFrames() + " and # frames: " + getNumberOfCameraFrames() + " highResolutionSlices: " + getNumberOfSlices());
            }
            return true;
        } else if (new File(str2).exists()) {
            if (g) {
                DLog.d("readFromFilePath", "raading XML file");
            }
            a(str2, false);
            return true;
        } else {
            DLog.w("readFromFilePath", "No file found: " + str3);
            if (g) {
                DLog.d("readFromFilePath", "Loaded: " + str3 + " with Slices: " + getNumberOfSlices() + " processedFrames: " + getNumberOfProcessedFrames());
            }
            return false;
        }
    }

    public boolean a(String str, String str2) {
        String str3 = str + File.separator + str2;
        return str2 != j.ae ? saveToXMLFile(str3) : saveToMagicFile(str3);
    }

    String b(String str) {
        String str2 = "";
        if (g) {
            DLog.d("decryptMagicFile", "Original magic file: " + str);
        }
        str2 = new AESUtils().decryptMagicFileToString(str);
        if (g) {
            DLog.d("decryptMagicFile", "Decrypted magic file: " + str2);
        }
        return str2;
    }
}
