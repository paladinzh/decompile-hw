package fyusion.vislib;

/* compiled from: Unknown */
public class ImuUtils {
    public static native int[] frameIndicesAtSpecifiedIntervalsNormalized(long j, Fyuse fyuse, long j2, IMUData iMUData, boolean z, double[] dArr);

    public static int[] frameIndicesAtSpecifiedIntervalsNormalized(Fyuse fyuse, IMUData iMUData, boolean z, double[] dArr) {
        return frameIndicesAtSpecifiedIntervalsNormalized(Fyuse.getCPtr(fyuse), fyuse, IMUData.getCPtr(iMUData), iMUData, z, dArr);
    }

    public static native int[] frameIndicesAtSpecifiedIntervalsRadians(long j, Fyuse fyuse, long j2, IMUData iMUData, boolean z, double[] dArr);

    public static int[] frameIndicesAtSpecifiedIntervalsRadians(Fyuse fyuse, IMUData iMUData, boolean z, double[] dArr) {
        return frameIndicesAtSpecifiedIntervalsRadians(Fyuse.getCPtr(fyuse), fyuse, IMUData.getCPtr(iMUData), iMUData, z, dArr);
    }

    public static native int[] frameIndicesForUniformRotationIntervals(long j, Fyuse fyuse, long j2, IMUData iMUData, boolean z, int i);

    public static int[] frameIndicesForUniformRotationIntervals(Fyuse fyuse, IMUData iMUData, boolean z, int i) {
        return frameIndicesForUniformRotationIntervals(Fyuse.getCPtr(fyuse), fyuse, IMUData.getCPtr(iMUData), iMUData, z, i);
    }
}
