package ucd.ui.util;

public class DensityUtil {
    private static float convertRatio = 1.0f;
    private static volatile float[] screenSize;
    private static float srcHeight = 1080.0f;
    private static float srcWidth = 1920.0f;

    public static float[] getScreenSize() {
        if (screenSize == null) {
            return null;
        }
        float[] copy = new float[screenSize.length];
        System.arraycopy(screenSize, 0, copy, 0, screenSize.length);
        return copy;
    }
}
