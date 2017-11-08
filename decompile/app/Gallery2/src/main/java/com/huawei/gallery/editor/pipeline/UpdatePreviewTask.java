package com.huawei.gallery.editor.pipeline;

import android.graphics.Bitmap;
import com.android.gallery3d.ui.GLRoot;
import com.huawei.gallery.editor.filters.FiltersManager;
import com.huawei.gallery.editor.imageshow.MasterImage;

public class UpdatePreviewTask extends ProcessingTask {
    private boolean mHasUnhandledPreviewRequest = false;
    private MasterImage mMasterImage;
    private boolean mPipelineIsOn = false;
    private CachingPipeline mPreviewPipeline = null;

    static class PreviewResult implements Result {
        public Buffer buffer;
        public Bitmap preview;
        public ImagePreset previewPreset;

        PreviewResult() {
        }
    }

    public UpdatePreviewTask(FiltersManager manager) {
        this.mPreviewPipeline = new CachingPipeline(manager, "Preview");
    }

    public void setOriginal(Bitmap bitmap) {
        this.mPreviewPipeline.setOriginal(bitmap);
        this.mPipelineIsOn = true;
    }

    public void setMasterImage(MasterImage image) {
        this.mMasterImage = image;
        this.mPreviewPipeline.setMasterImage(image);
    }

    public boolean tryLockPipeline() {
        return this.mPreviewPipeline.tryLockPipeline();
    }

    public void unlockPipeline() {
        this.mPreviewPipeline.unlockPipeline();
    }

    public void updatePreview() {
        if (this.mPipelineIsOn) {
            this.mHasUnhandledPreviewRequest = !postRequest(null);
            if (this.mMasterImage != null) {
                this.mMasterImage.notifyObservers(0, null);
            }
        }
    }

    public boolean isPriorityTask() {
        return true;
    }

    public Result doInBackground(Request message) {
        ImagePreset renderingPreset = this.mMasterImage.getPreviewPreset().dequeuePreset();
        PreviewResult previewResult = new PreviewResult();
        if (renderingPreset != null) {
            previewResult.preview = this.mPreviewPipeline.compute(renderingPreset);
        }
        previewResult.previewPreset = renderingPreset;
        if (!(previewResult.preview == null || this.mMasterImage.computePreviewIsSameSize(previewResult.preview))) {
            previewResult.buffer = new Buffer(previewResult.preview, this.mMasterImage.getBitmapCache());
        }
        return previewResult;
    }

    public void onResult(Result message) {
        GLRoot glRoot = this.mMasterImage.getGLRoot();
        if (glRoot != null && (message instanceof PreviewResult)) {
            PreviewResult previewResult = (PreviewResult) message;
            glRoot.lockRenderThread();
            try {
                this.mMasterImage.updateShareBuffer(previewResult.previewPreset, previewResult.buffer, previewResult.preview);
                this.mMasterImage.notifyObservers(1, previewResult.preview);
                if (this.mHasUnhandledPreviewRequest) {
                    updatePreview();
                }
            } finally {
                glRoot.unlockRenderThread();
            }
        }
    }

    public void clearCache() {
        this.mHasUnhandledPreviewRequest = false;
        this.mPreviewPipeline.clearCache();
        this.mPreviewPipeline.reset();
    }
}
