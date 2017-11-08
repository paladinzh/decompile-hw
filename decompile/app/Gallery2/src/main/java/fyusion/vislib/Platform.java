package fyusion.vislib;

/* compiled from: Unknown */
public final class Platform {
    public static final Platform Android = new Platform("Android", FyuseContainerUtilsWrapperJNI.Android_get());
    public static final Platform iOS = new Platform("iOS", FyuseContainerUtilsWrapperJNI.iOS_get());
    private static int swigNext = 0;
    private static Platform[] swigValues = new Platform[]{iOS, Android};
    private final String swigName;
    private final int swigValue;

    private Platform(String str) {
        this.swigName = str;
        int i = swigNext;
        swigNext = i + 1;
        this.swigValue = i;
    }

    private Platform(String str, int i) {
        this.swigName = str;
        this.swigValue = i;
        swigNext = i + 1;
    }

    private Platform(String str, Platform platform) {
        this.swigName = str;
        this.swigValue = platform.swigValue;
        swigNext = this.swigValue + 1;
    }

    public static Platform swigToEnum(int i) {
        int i2 = 0;
        if (i < swigValues.length && i >= 0 && swigValues[i].swigValue == i) {
            return swigValues[i];
        }
        while (i2 < swigValues.length) {
            if (swigValues[i2].swigValue == i) {
                return swigValues[i2];
            }
            i2++;
        }
        throw new IllegalArgumentException("No enum " + Platform.class + " with value " + i);
    }

    public final int swigValue() {
        return this.swigValue;
    }

    public String toString() {
        return this.swigName;
    }
}
