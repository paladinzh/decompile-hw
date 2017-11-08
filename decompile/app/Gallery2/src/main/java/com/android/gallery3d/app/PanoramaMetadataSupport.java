package com.android.gallery3d.app;

import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaObject.PanoramaSupportCallback;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.LightCycleHelper;
import com.android.gallery3d.util.LightCycleHelper.PanoramaMetadata;
import java.util.ArrayList;

public class PanoramaMetadataSupport implements FutureListener<PanoramaMetadata> {
    private ArrayList<PanoramaSupportCallback> mCallbacksWaiting;
    private Future<PanoramaMetadata> mGetPanoMetadataTask;
    private Object mLock = new Object();
    private MediaObject mMediaObject;
    private PanoramaMetadata mPanoramaMetadata;

    public PanoramaMetadataSupport(MediaObject mediaObject) {
        this.mMediaObject = mediaObject;
    }

    public void onFutureDone(Future<PanoramaMetadata> future) {
        synchronized (this.mLock) {
            this.mPanoramaMetadata = (PanoramaMetadata) future.get();
            if (this.mPanoramaMetadata == null) {
                this.mPanoramaMetadata = LightCycleHelper.NOT_PANORAMA;
            }
            for (PanoramaSupportCallback cb : this.mCallbacksWaiting) {
                cb.panoramaInfoAvailable(this.mMediaObject, this.mPanoramaMetadata.mUsePanoramaViewer, this.mPanoramaMetadata.mIsPanorama360);
            }
            this.mGetPanoMetadataTask = null;
            this.mCallbacksWaiting = null;
        }
    }
}
