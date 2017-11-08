package com.fyusion.sdk.common.ext;

import android.util.Pair;
import java.util.List;

/* compiled from: Unknown */
public interface ProcessedImageListener {

    /* compiled from: Unknown */
    public enum ProcessedImageError {
        OPERATION_NOT_SUPPORTED(0),
        FYUSE_NOT_PREPARED(1),
        FYUSE_CORRUPTED(2),
        FYUSE_DOES_NOT_EXIST(3),
        INVALID_INPUT_RANGE(4);
        
        private int a;

        private ProcessedImageError(int i) {
            this.a = i;
        }

        public int getLevel() {
            return this.a;
        }
    }

    void onError(ProcessedImageError processedImageError, String str);

    void onImageDataReady(ProcessItem processItem, List<Pair<String, Integer>> list);
}
