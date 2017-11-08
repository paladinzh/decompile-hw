package com.huawei.gallery.editor.pipeline;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.FiltersManager;
import com.huawei.gallery.editor.filters.ImageFilterFx;
import com.huawei.gallery.editor.imageshow.MasterImage;
import com.huawei.gallery.editor.tools.SaveImage.WaitForDataLoad;
import java.io.File;
import java.util.ArrayList;

public class ProcessingService implements WaitForDataLoad {
    private Context mContext;
    private FiltersManager mFiltersManager = new FiltersManager();
    private ImageSavingTask mImageSavingTask = new ImageSavingTask(this);
    private FiltersManager mPreviewFiltersManager = new FiltersManager();
    private ProcessingTaskController mProcessingTaskController = new ProcessingTaskController();
    private RenderingRequestTask mRenderingRequestTask = new RenderingRequestTask(this.mFiltersManager);
    private SimpleEditorManager mSimpleEditorManager;
    private UpdatePreviewTask mUpdatePreviewTask = new UpdatePreviewTask(this.mPreviewFiltersManager);

    public ProcessingService(Context context) {
        this.mContext = context;
        this.mProcessingTaskController.add(this.mImageSavingTask);
        this.mProcessingTaskController.add(this.mUpdatePreviewTask);
        this.mProcessingTaskController.add(this.mRenderingRequestTask);
        setupPipeline(context);
    }

    public Context getContext() {
        return this.mContext;
    }

    public FiltersManager getManager() {
        return this.mFiltersManager;
    }

    public ArrayList<FilterRepresentation> getTools() {
        return this.mFiltersManager.getTools();
    }

    public ArrayList<FilterRepresentation> getEffects() {
        return this.mFiltersManager.getEffects();
    }

    public ArrayList<FilterRepresentation> getFaces() {
        return this.mFiltersManager.getFaces();
    }

    public ArrayList<FilterRepresentation> getMosaics() {
        return this.mFiltersManager.getMosaics();
    }

    public ArrayList<FilterRepresentation> getSplash() {
        return this.mFiltersManager.getSplash();
    }

    public ArrayList<FilterRepresentation> getIllusion() {
        return this.mFiltersManager.getIllusion();
    }

    public void setSimpleEditorManager(SimpleEditorManager editorManager) {
        this.mSimpleEditorManager = editorManager;
    }

    public void setOriginalBitmap(Bitmap originalBitmap) {
        if (this.mUpdatePreviewTask != null) {
            this.mUpdatePreviewTask.setOriginal(originalBitmap);
            this.mRenderingRequestTask.setOriginal(originalBitmap);
        }
    }

    public void updatePreviewBuffer() {
        this.mUpdatePreviewTask.updatePreview();
    }

    public void postRenderingRequest(RenderingRequest request, int delay) {
        this.mRenderingRequestTask.postRenderingRequest(request, delay);
    }

    public void onDestroy() {
        this.mProcessingTaskController.quit();
        GalleryLog.d("ProcessingService", "ProcessingService has destroy.");
    }

    public void saveImage(Uri sourceUri, ImagePreset preset) {
        handleSaveRequest(sourceUri, preset, this.mSimpleEditorManager.getImage().getPreviewBitmap(), this.mSimpleEditorManager.getDestinationDirectory());
    }

    public void onStart() {
        if (this.mSimpleEditorManager != null) {
            this.mSimpleEditorManager.updateUIAfterServiceStarted();
        }
    }

    public void setMasterImage(MasterImage image) {
        this.mRenderingRequestTask.setMasterImage(image);
        this.mUpdatePreviewTask.setMasterImage(image);
        this.mImageSavingTask.setMasterImage(image);
    }

    public boolean tryLockPipeline() {
        return this.mUpdatePreviewTask.tryLockPipeline();
    }

    public void unlockPipeline() {
        this.mUpdatePreviewTask.unlockPipeline();
    }

    public void handleSaveRequest(Uri sourceUri, ImagePreset preset, Bitmap previewImage, File destinationDirectory) {
        this.mImageSavingTask.saveImage(sourceUri, preset, previewImage, destinationDirectory);
    }

    public void completeSaveImage(Uri result, boolean exit) {
        this.mSimpleEditorManager.completeSaveImage(result);
    }

    private void setupPipeline(Context context) {
        Resources res = context.getResources();
        this.mPreviewFiltersManager.setResources(res);
        this.mFiltersManager.setResources(res);
        FiltersManager filtersManager = this.mFiltersManager;
        if (EditorLoadLib.SFBJNI_LOADED || EditorLoadLib.isArcSoftLoaded()) {
            filtersManager.addSfbFaces();
        } else {
            filtersManager.addOmronFaces();
        }
        filtersManager.addTools(context);
        filtersManager.addMosaic();
        filtersManager.addEffects();
        if (ImageFilterFx.supportIllusionFilter()) {
            filtersManager.addIllusion();
        }
        if (ImageFilterFx.supportSplashFilter()) {
            filtersManager.addSplash();
        }
    }

    public void prepareEnterEditor() {
        this.mProcessingTaskController.inEditor(true);
    }

    public void prepareLeaveEditor() {
        this.mProcessingTaskController.inEditor(false);
    }

    public void onLeaveEditor() {
        if (this.mUpdatePreviewTask != null) {
            this.mUpdatePreviewTask.clearCache();
            if (this.mSimpleEditorManager != null) {
                this.mSimpleEditorManager.getImage().getBitmapCache().clearup();
            }
            this.mProcessingTaskController.getProcessingHandler().removeCallbacksAndMessages(null);
            this.mProcessingTaskController.inEditor(false);
        }
    }

    public boolean waitForDataLoad(Uri targetUri, Bitmap previewBitmap) {
        if (this.mSimpleEditorManager == null) {
            return false;
        }
        return this.mSimpleEditorManager.waitForDataLoad(targetUri, previewBitmap);
    }
}
