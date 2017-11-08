package com.huawei.gallery.displayengine;

import com.android.gallery3d.util.DisplayEngineUtils;

public class DisplayEngineFactory {
    private static final int DO_ACE = ((DisplayEngineUtils.getCapDoAce() ? 1 : 0) << 0);
    private static final int DO_ACM;
    private static final int DO_GMP;
    private static final int DO_SHARPNESS;
    private static final int DO_SR;

    static {
        int i;
        int i2 = 1;
        DisplayEngineUtils.waitForInit();
        if (DisplayEngineUtils.getCapDoSr()) {
            i = 1;
        } else {
            i = 0;
        }
        DO_SR = i << 1;
        if (DisplayEngineUtils.getCapDoSharpness()) {
            i = 1;
        } else {
            i = 0;
        }
        DO_SHARPNESS = i << 2;
        if (DisplayEngineUtils.getCapDoGmp()) {
            i = 1;
        } else {
            i = 0;
        }
        DO_GMP = i << 3;
        if (!DisplayEngineUtils.getCapDoAcm()) {
            i2 = 0;
        }
        DO_ACM = i2 << 4;
    }

    public static DisplayEngine buildDisplayEngine(int imageWidth, int imageHeight, int border, int type, float maxScale) {
        int algoType;
        DisplayEngine displayEngine;
        switch (type) {
            case 0:
                algoType = (((DO_ACE | DO_SR) | DO_SHARPNESS) | DO_GMP) | DO_ACM;
                if (algoType == 0) {
                    return null;
                }
                return new ScreenNailCommonDisplayEngine(imageWidth, imageHeight, algoType);
            case 1:
                algoType = (DO_ACE | DO_GMP) | DO_ACM;
                if (algoType == 0) {
                    return null;
                }
                displayEngine = new ScreenNailAceDisplayEngine(imageWidth, imageHeight);
                if (!displayEngine.initialize(0, algoType, maxScale)) {
                    displayEngine = null;
                }
                return displayEngine;
            case 2:
            case 3:
                algoType = DO_ACE | DO_GMP;
                if (algoType == 0) {
                    return null;
                }
                displayEngine = new ScreenNailAceDisplayEngine(imageWidth, imageHeight);
                if (!displayEngine.initialize(0, algoType, maxScale)) {
                    displayEngine = null;
                }
                return displayEngine;
            case 4:
                algoType = (DO_ACE | DO_GMP) | DO_ACM;
                if (algoType == 0) {
                    return null;
                }
                displayEngine = new TileAceDisplayEngine(imageWidth, imageHeight);
                if (!displayEngine.initialize(border, algoType, maxScale)) {
                    displayEngine = null;
                }
                return displayEngine;
            case 5:
                algoType = DO_SR | DO_SHARPNESS;
                if (algoType == 0) {
                    return null;
                }
                displayEngine = new TileScaleDisplayEngine(imageWidth, imageHeight);
                if (!displayEngine.initialize(border, algoType, maxScale)) {
                    displayEngine = null;
                }
                return displayEngine;
            case 6:
                algoType = DO_SR | DO_SHARPNESS;
                if (algoType == 0) {
                    return null;
                }
                displayEngine = new ScreenNailScaleDisplayEngine(imageWidth, imageHeight);
                if (!displayEngine.initialize(border, algoType, maxScale)) {
                    displayEngine = null;
                }
                return displayEngine;
            case 7:
                algoType = (DO_ACE | DO_ACM) | DO_GMP;
                if (algoType == 0) {
                    return null;
                }
                displayEngine = new ScreenNailAceDisplayEngine(imageWidth, imageHeight);
                if (!displayEngine.initialize(0, algoType, maxScale)) {
                    displayEngine = null;
                }
                return displayEngine;
            default:
                throw new IllegalStateException("display engine do not support this type:" + type);
        }
    }

    public static DisplayEngine buildDisplayEngine(int imageWidth, int imageHeight, int border, int type) {
        return buildDisplayEngine(imageWidth, imageHeight, border, type, 2.0f);
    }
}
