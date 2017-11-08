package com.fyusion.sdk.common.ext.filter;

import android.text.TextUtils;
import android.util.Log;

/* compiled from: Unknown */
public class ImageFilterFactory extends ImageFilterAbstractFactory {
    public ImageFilter createImageFilter(String str, String str2) {
        if (TextUtils.isEmpty(str) || "raw".equals(str)) {
            return null;
        }
        Object obj = -1;
        switch (str.hashCode()) {
            case -1523173581:
                if (str.equals("SHADOWS")) {
                    obj = 16;
                    break;
                }
                break;
            case -1522756109:
                if (str.equals("SHARPEN")) {
                    obj = 12;
                    break;
                }
                break;
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
            case 2041959:
                if (str.equals("BLUR")) {
                    obj = 14;
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
            case 842397247:
                if (str.equals("HIGHLIGHTS")) {
                    obj = 15;
                    break;
                }
                break;
            case 1133254737:
                if (str.equals("BRIGHTNESS")) {
                    obj = 8;
                    break;
                }
                break;
            case 2027936058:
                if (str.equals("VIGNETTE")) {
                    obj = 13;
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
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
                return FilterParser.parsePerPixel(str, str2);
            case 12:
            case 13:
            case 14:
                return FilterParser.parseBlockFilter(str, str2);
            case 15:
            case 16:
                return FilterParser.parseMultiControlsFilter(str, str2);
            default:
                Log.e("ImageFilterFactory", "Unknown filter name: " + str);
                return null;
        }
    }
}
