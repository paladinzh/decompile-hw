package fyusion.vislib;

/* compiled from: Unknown */
public class VideoExporter {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    public VideoExporter() {
        this(FyuseWrapperJNI.new_VideoExporter(), true);
    }

    protected VideoExporter(long j, boolean z) {
        this.swigCMemOwn = z;
        this.swigCPtr = j;
    }

    protected static long getCPtr(VideoExporter videoExporter) {
        return videoExporter != null ? videoExporter.swigCPtr : 0;
    }

    public static void renderLogoIntoFrame(SWIGTYPE_p_unsigned_char sWIGTYPE_p_unsigned_char, int i, int i2, int i3, SWIGTYPE_p_unsigned_char sWIGTYPE_p_unsigned_char2, int i4, int i5, int i6, int i7, int i8) {
        FyuseWrapperJNI.VideoExporter_renderLogoIntoFrame(SWIGTYPE_p_unsigned_char.getCPtr(sWIGTYPE_p_unsigned_char), i, i2, i3, SWIGTYPE_p_unsigned_char.getCPtr(sWIGTYPE_p_unsigned_char2), i4, i5, i6, i7, i8);
    }

    public static void renderRGBALogoIntoARGBFrame(SWIGTYPE_p_unsigned_char sWIGTYPE_p_unsigned_char, int i, int i2, int i3, SWIGTYPE_p_unsigned_char sWIGTYPE_p_unsigned_char2, int i4, int i5, int i6, int i7, int i8) {
        FyuseWrapperJNI.VideoExporter_renderRGBALogoIntoARGBFrame(SWIGTYPE_p_unsigned_char.getCPtr(sWIGTYPE_p_unsigned_char), i, i2, i3, SWIGTYPE_p_unsigned_char.getCPtr(sWIGTYPE_p_unsigned_char2), i4, i5, i6, i7, i8);
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                FyuseWrapperJNI.delete_VideoExporter(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    protected void finalize() {
        delete();
    }

    public boolean selectFramesForTweenedVideoOfLength(int i, int i2, int i3, FloatVec floatVec) {
        return FyuseWrapperJNI.VideoExporter_selectFramesForTweenedVideoOfLength__SWIG_1(this.swigCPtr, this, i, i2, i3, FloatVec.getCPtr(floatVec), floatVec);
    }

    public boolean selectFramesForTweenedVideoOfLength(int i, int i2, int i3, FloatVec floatVec, int i4) {
        return FyuseWrapperJNI.VideoExporter_selectFramesForTweenedVideoOfLength__SWIG_0(this.swigCPtr, this, i, i2, i3, FloatVec.getCPtr(floatVec), floatVec, i4);
    }

    public boolean selectFramesForVideoOfLength(int i, int i2, int i3, IntVec intVec) {
        return FyuseWrapperJNI.VideoExporter_selectFramesForVideoOfLength__SWIG_1(this.swigCPtr, this, i, i2, i3, IntVec.getCPtr(intVec), intVec);
    }

    public boolean selectFramesForVideoOfLength(int i, int i2, int i3, IntVec intVec, IntVec intVec2) {
        return FyuseWrapperJNI.VideoExporter_selectFramesForVideoOfLength__SWIG_3(this.swigCPtr, this, i, i2, i3, IntVec.getCPtr(intVec), intVec, IntVec.getCPtr(intVec2), intVec2);
    }

    public boolean selectFramesForVideoOfLength(int i, int i2, int i3, IntVec intVec, IntVec intVec2, boolean z) {
        return FyuseWrapperJNI.VideoExporter_selectFramesForVideoOfLength__SWIG_2(this.swigCPtr, this, i, i2, i3, IntVec.getCPtr(intVec), intVec, IntVec.getCPtr(intVec2), intVec2, z);
    }

    public boolean selectFramesForVideoOfLength(int i, int i2, int i3, IntVec intVec, boolean z) {
        return FyuseWrapperJNI.VideoExporter_selectFramesForVideoOfLength__SWIG_0(this.swigCPtr, this, i, i2, i3, IntVec.getCPtr(intVec), intVec, z);
    }
}
