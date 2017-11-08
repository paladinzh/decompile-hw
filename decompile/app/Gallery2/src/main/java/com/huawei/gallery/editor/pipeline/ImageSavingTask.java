package com.huawei.gallery.editor.pipeline;

import android.graphics.Bitmap;
import android.net.Uri;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.editor.imageshow.MasterImage;
import com.huawei.gallery.editor.tools.ReportEditorMessage;
import com.huawei.gallery.editor.tools.SaveImage;
import java.io.File;
import org.json.JSONException;

public class ImageSavingTask extends ProcessingTask {
    private MasterImage mMasterImage;
    private ProcessingService mProcessingService;

    static class SaveRequest implements Request {
        File destinationDirectory;
        boolean exit = false;
        ImagePreset preset;
        Bitmap previewImage;
        int quality;
        Uri sourceUri;

        SaveRequest() {
        }
    }

    static class URIResult implements Result {
        boolean exit;
        Uri uri;

        URIResult() {
        }
    }

    public ImageSavingTask(ProcessingService service) {
        this.mProcessingService = service;
    }

    public void saveImage(Uri sourceUri, ImagePreset preset, Bitmap previewImage, File destinationDirectory) {
        SaveRequest request = new SaveRequest();
        request.sourceUri = sourceUri;
        request.preset = preset;
        request.quality = 96;
        request.previewImage = previewImage;
        request.destinationDirectory = destinationDirectory;
        postRequest(request);
    }

    public Result doInBackground(Request message) {
        SaveRequest request = (SaveRequest) message;
        Uri sourceUri = request.sourceUri;
        Bitmap previewImage = request.previewImage;
        ImagePreset preset = request.preset;
        SaveImage saveImage = new SaveImage(this.mProcessingService.getContext(), sourceUri, sourceUri, request.destinationDirectory, previewImage);
        CachingPipeline pipeline = new CachingPipeline(this.mProcessingService.getManager(), "Saving");
        pipeline.setMasterImage(this.mMasterImage);
        Uri uri = saveImage.processAndSaveImage(preset, request.quality, pipeline, this.mProcessingService);
        if (!(uri == null || this.mMasterImage == null || preset == null)) {
            try {
                int i;
                if (this.mMasterImage.getEditorType() == 0) {
                    i = 1;
                } else {
                    i = 55;
                }
                ReportToBigData.report(i, ReportEditorMessage.getReportMsg(preset.getFilters()));
            } catch (JSONException e) {
                GalleryLog.d("ImageSavingTask", "report is failed!");
            }
        }
        URIResult result = new URIResult();
        result.uri = uri;
        result.exit = request.exit;
        return result;
    }

    public void onResult(Result message) {
        URIResult result = (URIResult) message;
        GLRoot root = null;
        if (this.mMasterImage != null) {
            root = this.mMasterImage.getGLRoot();
        }
        if (root == null) {
            this.mProcessingService.completeSaveImage(result.uri, result.exit);
            return;
        }
        root.lockRenderThread();
        try {
            this.mProcessingService.completeSaveImage(result.uri, result.exit);
        } finally {
            root.unlockRenderThread();
        }
    }

    public void setMasterImage(MasterImage image) {
        this.mMasterImage = image;
    }
}
