package fyusion.vislib;

/* compiled from: Unknown */
public class ImuProgressEstimatorWrapper {
    protected long jni_imu_ptr_;

    /* compiled from: Unknown */
    public enum RotationDirection {
        UNSPECIFIED(0),
        CLOCKWISE(1),
        COUNTERCLOCKWISE(-1);
        
        private int value;

        private RotationDirection(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }

    public ImuProgressEstimatorWrapper() {
        jni_init();
    }

    public double getProgress(float[] fArr) {
        return jni_getProgress(fArr);
    }

    public double getProgressUsingQuaternions(float f, float f2, float f3, float f4) {
        return jni_getProgressUsingQuaternions(f, f2, f3, f4);
    }

    protected native double jni_getProgress(float[] fArr);

    protected native double jni_getProgressUsingQuaternions(float f, float f2, float f3, float f4);

    protected native void jni_init();

    protected native void jni_reset();

    protected native void jni_setAxisOfRotation(float f, float f2, float f3);

    protected native void jni_setRotationDirection(int i);

    protected native void jni_setTargetRotationAmount(double d);

    public void reset() {
        jni_reset();
    }

    public void setAxisOfRotation(float f, float f2, float f3) {
        jni_setAxisOfRotation(f, f2, f3);
    }

    public void setRotationDirection(RotationDirection rotationDirection) {
        jni_setRotationDirection(rotationDirection.getValue());
    }

    public void setTargetRotationAmount(double d) {
        jni_setTargetRotationAmount(d);
    }
}
