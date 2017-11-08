package com.huawei.gallery.editor.pipeline;

import android.graphics.Bitmap;
import com.android.gallery3d.gadget.HwTransitionReflection;
import com.huawei.gallery.editor.filters.FiltersManager;
import com.huawei.gallery.editor.imageshow.MasterImage;

public class RenderingRequestTask extends ProcessingTask {
    private boolean mPipelineIsOn = false;
    private CachingPipeline mPreviewPipeline = null;

    static class Render implements Request {
        int delay;
        RenderingRequest request;

        Render() {
        }
    }

    static class RenderResult implements Result {
        RenderingRequest request;

        RenderResult() {
        }
    }

    public RenderingRequestTask(FiltersManager manager) {
        this.mPreviewPipeline = new CachingPipeline(manager, HwTransitionReflection.TRANS_TYPE_NORMAL);
    }

    public void setOriginal(Bitmap bitmap) {
        this.mPreviewPipeline.setOriginal(bitmap);
        this.mPipelineIsOn = true;
    }

    public void setMasterImage(MasterImage image) {
        this.mPreviewPipeline.setMasterImage(image);
    }

    public void postRenderingRequest(RenderingRequest request, int delay) {
        if (this.mPipelineIsOn) {
            Render render = new Render();
            render.request = request;
            render.delay = delay;
            postRequest(render);
        }
    }

    public Result doInBackground(Request message) {
        RenderingRequest request = ((Render) message).request;
        if (request.getType() == 1) {
            this.mPreviewPipeline.renderGeometry(request);
        } else {
            this.mPreviewPipeline.renderIcon(request);
        }
        RenderResult result = new RenderResult();
        result.request = request;
        return result;
    }

    public void onResult(Result message) {
        if (message != null) {
            ((RenderResult) message).request.markAvailable();
        }
    }

    public boolean isDelayedTask(Request message) {
        if (((Render) message).request.getType() == 1) {
            return true;
        }
        return false;
    }

    public int getType(Request message) {
        return (this.mType << 16) | ((Render) message).request.getType();
    }
}
