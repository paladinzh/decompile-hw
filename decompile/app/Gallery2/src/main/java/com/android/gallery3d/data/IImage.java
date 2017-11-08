package com.android.gallery3d.data;

import android.graphics.BitmapRegionDecoder;
import com.android.gallery3d.util.ThreadPool.Job;

public interface IImage {
    boolean isSupportTranslateVoiceImageToVideo();

    Job<BitmapRegionDecoder> requestLargeImage(byte[] bArr, int i, int i2);
}
