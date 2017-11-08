package com.fyusion.sdk.common.ext;

import android.support.annotation.NonNull;
import com.fyusion.sdk.common.FyuseSDK;
import com.fyusion.sdk.common.a;
import com.fyusion.sdk.common.ext.util.b;
import com.fyusion.sdk.common.h;
import fyusion.vislib.AESUtils;
import fyusion.vislib.BuildConfig;
import fyusion.vislib.Fyuse;
import fyusion.vislib.FyuseFrameInformationVec;
import fyusion.vislib.FyuseSize;
import fyusion.vislib.FyuseSlice;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class f extends Fyuse {
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

    public f() {
        a();
    }

    private void a() {
        this.f = new ArrayList();
        FyuseSize fyuseSize = new FyuseSize(0.0d, 0.0d);
        fyuseSize.height = 0.0d;
        fyuseSize.width = 0.0d;
        setCameraSize(fyuseSize);
        setCameraFile(k.ag);
        setNumberOfCameraFrames(0);
        setWhetherRecordedWithFrontCamera(false);
        setFrameTimestamps(new FyuseFrameInformationVec());
        setProcessedSize(fyuseSize);
        setNumberOfProcessedFrames(0);
        setProcessedFile(k.ar);
        setNumberOfStabilizedFrames(0);
        setOffsetsFile(k.ah);
        setStabilizationDataFrameOffset(0);
        setNumberOfMotionFrames(0);
        setMotionFile(k.ai);
        setDirectionX(1.0f);
        setDirectionY(0.0f);
        setGravityX(0.0f);
        setGravityY(1.0f);
        setLoopClosed(false);
        setStartFrame(0);
        setEndFrame(-1);
        setThumbnailIndex(0);
        setCurrentFilterID("raw");
        setSelectedFilterID("raw");
        setMax_number_frames_(300);
        setMax_ready_for_more_media_data_check_fails_(k.q);
        setCamera_fps_indicator_frame_window_size_(k.s);
        setPost_processing_width_(k.C);
        setStabilization_smoothing_iterations_(k.I);
        setStabilization_smoothing_range_(k.J);
        setMax_stabilization_motion_(k.K);
        setMax_low_fps_iso_value_(k.R);
        setMin_high_fps_iso_value_(k.S);
        setNum_frames_to_crop_(3);
        setCurrent_version_number_(k.V);
        setMjpeg_video_fps_(k.i);
        setH264_recording_video_fps_(k.j);
        setH264_upload_video_fps_(k.k);
        setReady_for_more_media_data_sleep_time_(k.r);
        setMax_stabilization_angle_(k.L);
        setFyuse_quality_(k.Q);
        setAverage_iso_value_(0.0f);
        setApply_face_detection_for_stabilization_(k.B);
        setWrite_mjpeg_fyuse_using_gpu_(k.E);
        setEnable_standard_unsharpen_filter_(k.X);
        setNight_mode_(false);
        setFrameSelectionType(0);
        String a = b.a();
        if (a != null) {
            setDeviceID(a);
        }
        setUniqueDeviceID(a.g());
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
        fyuseSlice.setIndex_file_name(k.as);
        fyuseSlice.setH264_file_name(k.aw);
        return fyuseSlice;
    }

    @NonNull
    public FyuseSlice a(int i) {
        int i2 = 0;
        if (getNumberOfSlices() > 0) {
            if (g) {
                h.d("fyuseDebug", "looking for frame number: " + i);
            }
            while (i2 < getNumberOfSlices()) {
                FyuseSlice slice = getSlice(i2);
                if (g) {
                    h.d("fyuseDebug", "Slice: " + i2 + " start: " + slice.getStart_frame() + " end: " + slice.getEnd_frame());
                }
                if (i >= slice.getStart_frame() && i <= slice.getEnd_frame()) {
                    if (g) {
                        h.d("fyuseDebug", "found thumbnail frame: " + i + " in slice: " + i2);
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
            String str2 = BuildConfig.FLAVOR;
            FileInputStream fileInputStream = new FileInputStream(str);
            byte[] bArr = new byte[((int) file.length())];
            fileInputStream.read(bArr);
            fileInputStream.close();
            String str3 = new String(bArr);
            if (z) {
                str3 = b(str);
            }
            if (!loadFromXMLString(str3) && g) {
                h.d("readFromXMLFile", "XML file loading failed!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean a(String str) {
        String str2 = str + File.separator + "fyuse.xml";
        String str3 = str + File.separator + k.ae;
        if (new File(str3).exists()) {
            a(str3, true);
            if (g) {
                h.a("readFromFilePath", "Read in fyuse with numProcessed: " + getNumberOfProcessedFrames() + " and # frames: " + getNumberOfCameraFrames() + " highResolutionSlices: " + getNumberOfSlices());
            }
            return true;
        } else if (new File(str2).exists()) {
            if (g) {
                h.a("readFromFilePath", "raading XML file");
            }
            a(str2, false);
            return true;
        } else {
            h.c("readFromFilePath", "No file found: " + str3);
            if (g) {
                h.a("readFromFilePath", "Loaded: " + str3 + " with Slices: " + getNumberOfSlices() + " processedFrames: " + getNumberOfProcessedFrames());
            }
            return false;
        }
    }

    public boolean a(String str, String str2) {
        String str3 = str + File.separator + str2;
        return str2 != k.ae ? saveToXMLFile(str3) : saveToMagicFile(str3);
    }

    String b(String str) {
        String str2 = BuildConfig.FLAVOR;
        if (g) {
            h.a("decryptMagicFile", "Original magic file: " + str);
        }
        str2 = new AESUtils().decryptMagicFileToString(str);
        if (g) {
            h.a("decryptMagicFile", "Decrypted magic file: " + str2);
        }
        return str2;
    }
}
