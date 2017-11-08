package fyusion.vislib;

/* compiled from: Unknown */
public class FyuseWrapper {
    public static void getAngleAxis(FloatVec floatVec, FloatVec floatVec2) {
        FyuseWrapperJNI.getAngleAxis(FloatVec.getCPtr(floatVec), floatVec, FloatVec.getCPtr(floatVec2), floatVec2);
    }

    public static String narrow(String str) {
        return FyuseWrapperJNI.narrow(str);
    }
}
