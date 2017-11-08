package com.huawei.gallery.editor.pipeline;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import com.android.gallery3d.R;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.DisplayEngineUtils;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.huawei.gallery.editor.cache.ImageLoader;
import com.huawei.gallery.editor.category.Action;
import com.huawei.gallery.editor.category.BaseViewAdapter;
import com.huawei.gallery.editor.category.CategoryAdapter;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.draw.ArrowDraw;
import com.huawei.gallery.editor.filters.draw.CustMosaicDraw;
import com.huawei.gallery.editor.filters.draw.DrawStyle;
import com.huawei.gallery.editor.filters.draw.FreeLineDraw;
import com.huawei.gallery.editor.filters.draw.MosaicDraw;
import com.huawei.gallery.editor.filters.draw.SimpleShapeDraw;
import com.huawei.gallery.editor.filters.fx.FilterLoader;
import com.huawei.gallery.editor.imageshow.MasterImage;
import com.huawei.gallery.editor.imageshow.MasterImage.Delegate;
import com.huawei.gallery.editor.omron.FaceDetectionIMP;
import com.huawei.gallery.editor.screenshotseditor.ui.ScreenShotsEditorView;
import com.huawei.gallery.editor.sfb.FaceEdit;
import com.huawei.gallery.editor.step.AdjustEditorStep;
import com.huawei.gallery.editor.step.EditorStep;
import com.huawei.gallery.editor.step.FaceEditorStep;
import com.huawei.gallery.editor.ui.BaseEditorView;
import java.io.File;
import java.util.ArrayList;

public class SimpleEditorManager implements Delegate, RenderingRequestCaller {
    private ArrayList<Action> mActions = new ArrayList();
    private ProcessingService mBoundService;
    private BaseViewAdapter mCategoryFiltersAdapter = null;
    private Context mContext;
    private File mDestinationDirectory = null;
    private Future<DetectFaceResult> mDetectFaceTask;
    private BaseEditorView mEditorView;
    private BaseViewAdapter mFacesViewAdapter = null;
    private SparseArray<CategoryAdapter> mFiltersAdpater;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (SimpleEditorManager.this.mBoundService == null || !SimpleEditorManager.this.mBoundService.tryLockPipeline()) {
                        sendEmptyMessageDelayed(1, 1000);
                        return;
                    }
                    try {
                        SimpleEditorManager.this.getImage().resetTemporaryBitmap();
                        return;
                    } finally {
                        SimpleEditorManager.this.mBoundService.unlockPipeline();
                    }
                    break;
                default:
                    return;
            }
        }
    };
    private Listener mListener;
    MasterImage mMasterImage = null;
    private Bitmap mSource;
    private CreateEditorBitmapTask mTask = null;

    private class CreateEditorBitmapTask extends AsyncTask<Uri, Boolean, Bitmap> {
        private MediaItem mItem;

        CreateEditorBitmapTask(MediaItem mediaItem) {
            this.mItem = mediaItem;
        }

        protected Bitmap doInBackground(Uri... uri) {
            Bitmap bmp = ImageLoader.getEditorBitmap(uri[0], SimpleEditorManager.this.mContext, this.mItem);
            if (DisplayEngineUtils.isDisplayEngineEnable()) {
                return DisplayEngineUtils.processScreenNailACE(bmp, this.mItem, null);
            }
            return bmp;
        }

        protected void onCancelled(Bitmap bmp) {
            GalleryLog.d("SimpleEditorManager", "task need not post. use screenNail for editor");
            if (!(bmp == null || bmp.isRecycled())) {
                bmp.recycle();
            }
            super.onCancelled(bmp);
        }

        protected void onPostExecute(Bitmap bmp) {
            GLRoot root = SimpleEditorManager.this.mEditorView.getGLRoot();
            if (root != null) {
                root.lockRenderThread();
                try {
                    GalleryLog.d("SimpleEditorManager", "task post. use decode bitmap for editor");
                    SimpleEditorManager.this.mMasterImage.updateBitmap(bmp);
                    if (bmp != null) {
                        SimpleEditorManager.this.mHandler.sendEmptyMessage(1);
                    }
                    SimpleEditorManager.this.loadBitmapCompleted(true);
                } finally {
                    root.unlockRenderThread();
                }
            }
        }
    }

    private static class DetectFaceJob extends BaseJob<DetectFaceResult> {
        private Bitmap mBitmap;
        private DelegateCache mCache;

        public DetectFaceJob(Bitmap src, DelegateCache cache) {
            this.mBitmap = src;
            this.mCache = cache;
        }

        public DetectFaceResult run(JobContext jc) {
            DetectFaceResult result = new DetectFaceResult();
            if (this.mBitmap == null || jc.isCancelled() || (!EditorLoadLib.SFBJNI_LOADED && !EditorLoadLib.OMRONJNI_LOADED)) {
                return result;
            }
            try {
                result.result = haveFace(this.mBitmap);
                return result;
            } finally {
                if (this.mCache != null) {
                    this.mCache.cache(this.mBitmap);
                } else {
                    this.mBitmap.recycle();
                }
                result.recycleOrCache = true;
                this.mBitmap = null;
            }
        }

        private boolean haveFace(Bitmap bitmap) {
            boolean z = false;
            if (bitmap == null || bitmap.isRecycled()) {
                return false;
            }
            int num = 0;
            FaceDetectionIMP faceDetection;
            if (EditorLoadLib.isArcSoftLoaded()) {
                faceDetection = new FaceDetectionIMP(bitmap);
                num = faceDetection.detect();
                faceDetection.destroy();
            } else if (EditorLoadLib.SFBJNI_LOADED) {
                num = FaceEdit.detect(bitmap);
            } else if (EditorLoadLib.OMRONJNI_LOADED) {
                faceDetection = new FaceDetectionIMP(bitmap);
                num = faceDetection.detect();
                faceDetection.destroy();
            }
            if (num > 0) {
                z = true;
            }
            return z;
        }

        public String workContent() {
            return "detecface with FaceEdit.";
        }
    }

    private class DetectFaceJobListener implements FutureListener<DetectFaceResult> {
        private Bitmap mBitmap;
        private DelegateCache mCache;

        public DetectFaceJobListener(Bitmap src, DelegateCache cache) {
            this.mBitmap = src;
            this.mCache = cache;
        }

        public void onFutureDone(final Future<DetectFaceResult> future) {
            SimpleEditorManager.this.mEditorView.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (future.get() != null) {
                        if (!((DetectFaceResult) future.get()).recycleOrCache) {
                            if (DetectFaceJobListener.this.mCache != null) {
                                DetectFaceJobListener.this.mCache.cache(DetectFaceJobListener.this.mBitmap);
                            } else {
                                DetectFaceJobListener.this.mBitmap.recycle();
                            }
                            DetectFaceJobListener.this.mBitmap = null;
                        }
                        if (future == SimpleEditorManager.this.mDetectFaceTask && SimpleEditorManager.this.mListener != null) {
                            SimpleEditorManager.this.mListener.onDetectedFace(((DetectFaceResult) future.get()).result);
                        }
                    }
                }
            });
        }
    }

    private static class DetectFaceResult {
        boolean recycleOrCache;
        boolean result;

        private DetectFaceResult() {
            this.result = false;
            this.recycleOrCache = false;
        }
    }

    public interface Listener {
        void onDetectedFace(boolean z);

        void onServiceConnected();

        void onStackChanged(boolean z, boolean z2, boolean z3);
    }

    public void available(RenderingRequest request) {
        GLRoot glRoot = getGLRoot();
        if (glRoot != null) {
            glRoot.lockRenderThread();
        }
        try {
            if (request.getBitmap() != null) {
                if (request.isGemoeteryRendering()) {
                    this.mMasterImage.updateGeometryOnlyBitmap(request.getBitmap());
                }
                if (glRoot != null) {
                    glRoot.unlockRenderThread();
                }
            }
        } finally {
            if (glRoot != null) {
                glRoot.unlockRenderThread();
            }
        }
    }

    public SimpleEditorManager(Context context, BaseEditorView editorView) {
        this.mEditorView = editorView;
        this.mContext = context;
    }

    public void create() {
        this.mBoundService = new ProcessingService(this.mContext);
        this.mBoundService.setSimpleEditorManager(this);
        this.mBoundService.onStart();
        if (this.mListener != null) {
            this.mListener.onServiceConnected();
        }
    }

    public void destroy() {
        if (this.mBoundService != null) {
            this.mBoundService.onDestroy();
        }
    }

    public DrawStyle[] getDrawingsTypes() {
        return new DrawStyle[]{new FreeLineDraw(), new ArrowDraw(), new SimpleShapeDraw(0), new SimpleShapeDraw(1), new SimpleShapeDraw(2), new MosaicDraw(1), new MosaicDraw(0), new CustMosaicDraw(R.drawable.img_patten_2, getContext()), new CustMosaicDraw(R.drawable.img_patten_3, getContext()), new CustMosaicDraw(R.drawable.img_patten_4, getContext()), new CustMosaicDraw(R.drawable.img_patten_5, getContext()), new CustMosaicDraw(R.drawable.img_patten_6, getContext()), new CustMosaicDraw(R.drawable.img_patten_7, getContext()), new CustMosaicDraw(R.drawable.img_patten_8, getContext()), new CustMosaicDraw(R.drawable.img_patten_9, getContext())};
    }

    public boolean setSource(MediaItem mediaItem, Bitmap source, int orientation) {
        if (this.mBoundService != null) {
            this.mBoundService.prepareEnterEditor();
        }
        createMasterImage();
        this.mSource = source;
        this.mDestinationDirectory = mediaItem.getDestinationDirectory();
        getImage().loadTemporaryBitmap(mediaItem.getContentUri(), source, orientation);
        if (loadBitmapCompleted(false)) {
            this.mTask = new CreateEditorBitmapTask(mediaItem);
            this.mTask.execute(new Uri[]{mediaItem.getContentUri()});
            return true;
        }
        GalleryLog.d("SimpleEditorManager", "loadBitmapCompleted failed!");
        if (this.mBoundService != null) {
            this.mBoundService.prepareLeaveEditor();
        }
        getImage().resetBitmap();
        return false;
    }

    public void cancelTask() {
        if (this.mTask != null) {
            this.mTask.cancel(false);
        }
    }

    public Bitmap getSource() {
        return this.mSource;
    }

    public void post(Bitmap source, ImagePreset preset, int type) {
        RenderingRequest.post(this, source, preset, type, this);
    }

    private boolean loadBitmapCompleted(boolean isUpdate) {
        if (this.mBoundService == null) {
            GalleryLog.d("SimpleEditorManager", "mBoundService == null");
            return false;
        }
        createMasterImage();
        Bitmap largeBitmap = getImage().getOriginalBitmapLarge() != null ? getImage().getOriginalBitmapLarge() : getImage().getOriginalBitmapLargeTemporary();
        if (largeBitmap == null) {
            GalleryLog.d("SimpleEditorManager", "largeBitmap == null");
            return false;
        }
        this.mBoundService.setMasterImage(this.mMasterImage);
        this.mBoundService.setOriginalBitmap(largeBitmap);
        this.mCategoryFiltersAdapter.imageLoaded();
        this.mFacesViewAdapter.imageLoaded();
        if (this.mFiltersAdpater != null) {
            for (int i = 0; i < this.mFiltersAdpater.size(); i++) {
                ((CategoryAdapter) this.mFiltersAdpater.get(this.mFiltersAdpater.keyAt(i))).imageLoaded();
            }
        }
        if (isUpdate) {
            this.mMasterImage.invalidatePreview();
        } else {
            initSharedBuffer(new ImagePreset(), largeBitmap);
            this.mEditorView.invalidate();
        }
        this.mMasterImage.resetGeometryImages(true);
        return true;
    }

    private void initSharedBuffer(ImagePreset preset, Bitmap largeBitmap) {
        this.mMasterImage.updatePreset(preset);
        this.mMasterImage.initSharedBuffer(preset, largeBitmap);
    }

    public ProcessingService getProcessingService() {
        return this.mBoundService;
    }

    public void updatePreviewBuffer() {
        this.mBoundService.updatePreviewBuffer();
    }

    public Context getContext() {
        return this.mContext;
    }

    public void registerAction(Action action) {
        if (!this.mActions.contains(action)) {
            this.mActions.add(action);
        }
    }

    public SparseArray<CategoryAdapter> getFiltersAdpater() {
        return this.mFiltersAdpater;
    }

    public BaseViewAdapter getAdjustViewAdapter() {
        return this.mCategoryFiltersAdapter;
    }

    public BaseViewAdapter getFacesViewAdapter() {
        return this.mFacesViewAdapter;
    }

    public void updateUIAfterServiceStarted() {
        setupMasterImage();
        fillCategories();
        loadBitmapCompleted(false);
    }

    private void setupMasterImage() {
        createMasterImage();
    }

    private void fillCategories() {
        fillLooks();
        fillFaces();
        fillEffects();
    }

    private void fillLooks() {
        if (this.mFiltersAdpater != null) {
            this.mFiltersAdpater.clear();
        }
        this.mFiltersAdpater = new SparseArray();
        new FilterLoader().fillAdapter(this.mFiltersAdpater, this);
    }

    private void fillFaces() {
        if (this.mBoundService != null) {
            ArrayList<FilterRepresentation> filtersRepresentations = this.mBoundService.getFaces();
            if (this.mFacesViewAdapter != null) {
                this.mFacesViewAdapter.clear();
            }
            this.mFacesViewAdapter = new BaseViewAdapter(this.mContext);
            for (FilterRepresentation representation : filtersRepresentations) {
                this.mFacesViewAdapter.add(new Action(this, representation, FaceEditorStep.class));
            }
        }
    }

    private void fillEffects() {
        if (this.mBoundService != null) {
            ArrayList<FilterRepresentation> filtersRepresentations = this.mBoundService.getEffects();
            if (this.mCategoryFiltersAdapter != null) {
                this.mCategoryFiltersAdapter.clear();
            }
            this.mCategoryFiltersAdapter = new BaseViewAdapter(this.mContext);
            for (FilterRepresentation representation : filtersRepresentations) {
                this.mCategoryFiltersAdapter.add(new Action(this, representation, AdjustEditorStep.class));
            }
        }
    }

    public void showRepresentations(ArrayList<FilterRepresentation> representations, EditorStep editorStep) {
        if (representations.size() != 0) {
            for (int i = 0; i < representations.size(); i++) {
                editorStep.add((FilterRepresentation) representations.get(i));
            }
            this.mMasterImage.showRepresentation(editorStep);
        }
    }

    public void showRepresentation(FilterRepresentation representation, EditorStep editorStep) {
        if (representation != null) {
            editorStep.add(representation);
            this.mMasterImage.showRepresentation(editorStep);
        }
    }

    public void pushEditorStep(EditorStep step) {
        this.mMasterImage.pushEditorStep(step);
        this.mMasterImage.resetImagePreset();
    }

    public void redo() {
        this.mMasterImage.redo();
        this.mMasterImage.resetImagePreset();
    }

    public void undo() {
        this.mMasterImage.undo();
        this.mMasterImage.resetImagePreset();
    }

    public Uri getUri() {
        return this.mMasterImage.getUri();
    }

    public void completeSaveImage(Uri saveUri) {
        this.mEditorView.leaveEditor(saveUri, false);
    }

    public void invalidate(int type, Bitmap preview) {
        switch (type) {
            case 0:
                if (this.mDetectFaceTask != null) {
                    this.mDetectFaceTask.cancel();
                }
                this.mDetectFaceTask = null;
                break;
            case 1:
                this.mEditorView.invalidate();
                if (this.mEditorView.inPreviewState() && preview != null) {
                    this.mDetectFaceTask = this.mEditorView.getGalleryContext().getThreadPool().submit(new DetectFaceJob(preview, this.mMasterImage), new DetectFaceJobListener(preview, this.mMasterImage), EditorLoadLib.SFBJNI_LOADED ? 3 : 4);
                    break;
                }
                return;
                break;
        }
    }

    public void onStackChanged(boolean canUndo, boolean canRedo, boolean canCompare) {
        if (this.mListener != null) {
            this.mListener.onStackChanged(canUndo, canRedo, canCompare);
        }
    }

    public void saveCurrentPreset() {
        getImage().saveCurrentPreset();
    }

    public void restoreSavedPreset() {
        this.mMasterImage.restoreSavedPreset();
    }

    public void setListener(Listener l) {
        this.mListener = l;
    }

    public void freeTexture() {
        if (this.mMasterImage != null) {
            this.mMasterImage.onLeaveEditor();
        }
    }

    public void onLeaveEditor(boolean needFreeTexture) {
        this.mHandler.removeCallbacksAndMessages(null);
        if (this.mDetectFaceTask != null) {
            this.mDetectFaceTask.cancel();
        }
        this.mDetectFaceTask = null;
        if (needFreeTexture) {
            this.mMasterImage.onLeaveEditor();
        }
        if (this.mFiltersAdpater != null) {
            for (int i = 0; i < this.mFiltersAdpater.size(); i++) {
                ((CategoryAdapter) this.mFiltersAdpater.get(this.mFiltersAdpater.keyAt(i))).resetActionImage();
            }
        }
        if (this.mCategoryFiltersAdapter != null) {
            this.mCategoryFiltersAdapter.resetAllRepresentation();
        }
        if (this.mBoundService != null) {
            this.mBoundService.onLeaveEditor();
        }
        cancelTask();
    }

    public File getDestinationDirectory() {
        return this.mDestinationDirectory;
    }

    public MasterImage getImage() {
        return this.mMasterImage;
    }

    public boolean waitForDataLoad(Uri targetUri, Bitmap previewBitmap) {
        return this.mEditorView.waitForDataLoad(targetUri, previewBitmap);
    }

    public GLRoot getGLRoot() {
        return this.mEditorView.getGLRoot();
    }

    private void createMasterImage() {
        if (this.mMasterImage == null) {
            this.mMasterImage = new MasterImage(this.mContext, this, this.mEditorView instanceof ScreenShotsEditorView ? 1 : 0);
        }
    }

    public ArrayList<FilterRepresentation> getTools() {
        return this.mBoundService.getTools();
    }

    public ArrayList<FilterRepresentation> getMosaics() {
        return this.mBoundService.getMosaics();
    }

    public ArrayList<FilterRepresentation> getSplash() {
        return this.mBoundService.getSplash();
    }

    public ArrayList<FilterRepresentation> getIllusion() {
        return this.mBoundService.getIllusion();
    }
}
