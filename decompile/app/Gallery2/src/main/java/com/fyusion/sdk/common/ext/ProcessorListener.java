package com.fyusion.sdk.common.ext;

import android.graphics.Bitmap;

/* compiled from: Unknown */
public interface ProcessorListener {
    void onError(ProcessItem processItem, ProcessError processError);

    void onImageDataReady(ProcessItem processItem);

    void onMetadataReady(ProcessItem processItem, int i);

    void onProcessComplete(ProcessItem processItem);

    void onProgress(ProcessItem processItem, int i, int i2, Bitmap bitmap);

    void onSliceFound(ProcessItem processItem, int i);

    void onSliceReady(ProcessItem processItem, int i);

    void onTweensReady(ProcessItem processItem);
}
