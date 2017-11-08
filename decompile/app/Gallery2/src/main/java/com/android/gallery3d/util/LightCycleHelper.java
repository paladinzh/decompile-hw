package com.android.gallery3d.util;

import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.app.StitchingProgressManager;

public class LightCycleHelper {
    public static final PanoramaMetadata NOT_PANORAMA = new PanoramaMetadata(false, false);

    public static class PanoramaMetadata {
        public final boolean mIsPanorama360;
        public final boolean mUsePanoramaViewer;

        public PanoramaMetadata(boolean usePanoramaViewer, boolean isPanorama360) {
            this.mUsePanoramaViewer = usePanoramaViewer;
            this.mIsPanorama360 = isPanorama360;
        }
    }

    public static StitchingProgressManager createStitchingManagerInstance(GalleryApp app) {
        return null;
    }
}
