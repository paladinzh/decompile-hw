package com.fyusion.sdk.common.ext.filter;

import fyusion.vislib.BuildConfig;

/* compiled from: Unknown */
public class FilterParser {
    private static float a(String str) {
        try {
            return Float.parseFloat(str.trim());
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }

    private static int b(String str) {
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static ImageFilter parseBlockFilter(String str, String str2) {
        if (str2 == null) {
            return null;
        }
        String[] split = str2.replace("[", BuildConfig.FLAVOR).replace("]", BuildConfig.FLAVOR).split(",");
        if (split.length < 3) {
            return null;
        }
        FilterControl sharpenFilter;
        float a = a(split[0]);
        int b = b(split[1]);
        int b2 = b(split[2]);
        int i = -1;
        switch (str.hashCode()) {
            case -1522756109:
                if (str.equals("SHARPEN")) {
                    i = 0;
                    break;
                }
                break;
            case 2041959:
                if (str.equals("BLUR")) {
                    i = 2;
                    break;
                }
                break;
            case 2027936058:
                if (str.equals("VIGNETTE")) {
                    i = 1;
                    break;
                }
                break;
        }
        switch (i) {
            case 0:
                sharpenFilter = new SharpenFilter();
                break;
            case 1:
                sharpenFilter = new VignetteFilter();
                break;
            case 2:
                sharpenFilter = new BlurFilter();
                break;
            default:
                return null;
        }
        BlockFilter blockFilter = (BlockFilter) sharpenFilter.createFilter(a);
        blockFilter.setImageSize(b, b2);
        return blockFilter;
    }

    public static ImageFilter parseMultiControlsFilter(String str, String str2) {
        FilterControl highlightsFilter;
        float a = a(str2);
        Object obj = -1;
        switch (str.hashCode()) {
            case -1523173581:
                if (str.equals("SHADOWS")) {
                    obj = 1;
                    break;
                }
                break;
            case 842397247:
                if (str.equals("HIGHLIGHTS")) {
                    obj = null;
                    break;
                }
                break;
        }
        switch (obj) {
            case null:
                highlightsFilter = new HighlightsFilter();
                break;
            case 1:
                highlightsFilter = new ShadowsFilter();
                break;
            default:
                return null;
        }
        return highlightsFilter.createFilter(a);
    }

    public static ImageFilter parsePerPixel(String str, String str2) {
        if (str2 == null) {
            return null;
        }
        FilterControl boudelaireToneCurve;
        float a = a(str2.replace("[", BuildConfig.FLAVOR).replace("]", BuildConfig.FLAVOR));
        Object obj = -1;
        switch (str.hashCode()) {
            case -1305397551:
                if (str.equals("Bukowski")) {
                    obj = 2;
                    break;
                }
                break;
            case -1143378681:
                if (str.equals("EXPOSURE")) {
                    obj = 11;
                    break;
                }
                break;
            case -793263502:
                if (str.equals("Dolores")) {
                    obj = 4;
                    break;
                }
                break;
            case -508611403:
                if (str.equals("Horenstein")) {
                    obj = 7;
                    break;
                }
                break;
            case -299959267:
                if (str.equals("Eastman")) {
                    obj = 3;
                    break;
                }
                break;
            case -181326628:
                if (str.equals("Suzanne")) {
                    obj = 6;
                    break;
                }
                break;
            case 2163804:
                if (str.equals("Emma")) {
                    obj = 5;
                    break;
                }
                break;
            case 215679746:
                if (str.equals("CONTRAST")) {
                    obj = 9;
                    break;
                }
                break;
            case 254601170:
                if (str.equals("SATURATION")) {
                    obj = 10;
                    break;
                }
                break;
            case 451740734:
                if (str.equals("Boudelaire")) {
                    obj = null;
                    break;
                }
                break;
            case 1133254737:
                if (str.equals("BRIGHTNESS")) {
                    obj = 8;
                    break;
                }
                break;
            case 2133018664:
                if (str.equals("Ginger")) {
                    obj = 1;
                    break;
                }
                break;
        }
        switch (obj) {
            case null:
                boudelaireToneCurve = new BoudelaireToneCurve();
                break;
            case 1:
                boudelaireToneCurve = new GingerToneCurve();
                break;
            case 2:
                boudelaireToneCurve = new BukowskiToneCurve();
                break;
            case 3:
                boudelaireToneCurve = new EastmanToneCurve();
                break;
            case 4:
                boudelaireToneCurve = new DoloresToneCurve();
                break;
            case 5:
                boudelaireToneCurve = new EmmaToneCurve();
                break;
            case 6:
                boudelaireToneCurve = new SuzanneToneCurve();
                break;
            case 7:
                boudelaireToneCurve = new HorensteinToneCurve();
                break;
            case 8:
                boudelaireToneCurve = new BrightnessFilter();
                break;
            case 9:
                boudelaireToneCurve = new ContrastFilter();
                break;
            case 10:
                boudelaireToneCurve = new SaturationFilter();
                break;
            case 11:
                boudelaireToneCurve = new ExposureFilter();
                break;
            default:
                return null;
        }
        return boudelaireToneCurve.createFilter(a);
    }
}
