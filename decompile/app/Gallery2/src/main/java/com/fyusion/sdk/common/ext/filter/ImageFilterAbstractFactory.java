package com.fyusion.sdk.common.ext.filter;

/* compiled from: Unknown */
public abstract class ImageFilterAbstractFactory {
    public static final String BLUR = "BLUR";
    public static final String BOUDELAIRE = "Boudelaire";
    public static final String BRIGHTNESS = "BRIGHTNESS";
    public static final String BUKOWSKI = "Bukowski";
    public static final String CONTRAST = "CONTRAST";
    public static final String DOLORES = "Dolores";
    public static final String EASTMAN = "Eastman";
    public static final String EMMA = "Emma";
    public static final String EXPOSURE = "EXPOSURE";
    public static final String GINGER = "Ginger";
    public static final String HIGHLIGHTS = "HIGHLIGHTS";
    public static final String HORENSTEIN = "Horenstein";
    public static final String NO_TONE_CURVE_FILTER = "raw";
    public static final String SATURATION = "SATURATION";
    public static final String SHADOWS = "SHADOWS";
    public static final String SHARPEN = "SHARPEN";
    public static final String SUZANNE = "Suzanne";
    public static final String VIGNETTE = "VIGNETTE";

    public abstract ImageFilter createImageFilter(String str, String str2);
}
