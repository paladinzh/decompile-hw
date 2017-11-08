package com.huawei.gallery.refocus.wideaperture.RangeMeasure.app;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.settings.GallerySettings;
import com.android.gallery3d.ui.AbsPhotoView;
import com.android.gallery3d.ui.EmptyPhotoView;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLRootView;
import com.android.gallery3d.util.ContextedUtils;
import com.huawei.gallery.app.AbsPhotoPage;
import com.huawei.gallery.refocus.app.RefocusSinglePhotoDataAdapter;
import com.huawei.gallery.refocus.ui.RefocusView;
import com.huawei.gallery.refocus.ui.RefocusView.Listener;
import com.huawei.gallery.refocus.wideaperture.RangeMeasure.ui.RangeMeasureView;
import com.huawei.gallery.refocus.wideaperture.app.WideAperturePhotoImpl;
import com.huawei.gallery.refocus.wideaperture.app.WideAperturePhotoImpl.WideAperturePhotoListener;
import com.huawei.gallery.util.LayoutHelper;

public class RangeMeasurePage extends AbsPhotoPage implements Listener, RangeMeasureView.Listener, WideAperturePhotoListener {
    private OnClickListener mErrorDialogListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            RangeMeasurePage.this.mRangeMeasureView.removeRangeMeasureView();
        }
    };
    private ImageView mGradient;
    private boolean mImageDecodeCompleted = false;
    private int mImageHeight;
    private boolean mImagePrepareCompleted = false;
    private int mImageWidth;
    private CheckBox mNoRangeMeasureTipsCheckBox;
    private OnClickListener mNotSupportDialogListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            RangeMeasurePage.this.mHandler.sendEmptyMessage(50);
        }
    };
    private RangeMeasureView mRangeMeasureView;
    private RefocusView mRefocusView;
    private Button mRemeasurementButton;
    private RelativeLayout mRoot;
    private OnClickListener mTipsDialogListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(RangeMeasurePage.this.mHost.getActivity()).edit();
            editor.putBoolean(GallerySettings.KEY_RANGE_MEASURE_NOTIPS, RangeMeasurePage.this.mNoRangeMeasureTipsCheckBox != null ? RangeMeasurePage.this.mNoRangeMeasureTipsCheckBox.isChecked() : false);
            editor.apply();
            if (RangeMeasurePage.this.mToast != null) {
                RangeMeasurePage.this.mToast.show();
            }
        }
    };
    private Toast mToast;
    private final OnSystemUiVisibilityChangeListener mUiVisibility = new OnSystemUiVisibilityChangeListener() {
        public void onSystemUiVisibilityChange(int visibility) {
            if (visibility == 0) {
                RangeMeasurePage.this.mHandler.removeMessages(2);
                RangeMeasurePage.this.mHandler.sendEmptyMessageDelayed(2, (long) RangeMeasurePage.HIDE_BARS_TIMEOUT);
            }
        }
    };
    private View mView;
    private WideAperturePhotoImpl mWideAperturePhoto;

    protected void onCreate(Bundle data, Bundle storedState) {
        super.onCreate(data, storedState);
        this.mFlags |= 16;
        if (data.getBoolean("is-secure-camera-album", false)) {
            this.mFlags |= FragmentTransaction.TRANSIT_EXIT_MASK;
        }
        initViews();
        initData(data);
    }

    private void initData(Bundle data) {
        Path itemPath = getItemPath(data);
        MediaItem mediaItem = null;
        if (itemPath != null) {
            mediaItem = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(itemPath);
        }
        if (mediaItem == null) {
            ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.fail_to_load_image_Toast, 1);
            return;
        }
        this.mModel = new RefocusSinglePhotoDataAdapter(this.mHost.getGalleryContext(), this.mHost.getGLRoot(), this.mRefocusView, mediaItem);
        this.mRefocusView.setModel(this.mModel);
        this.mRefocusView.setMediaItemScreenNail(mediaItem);
        updateCurrentPhoto(mediaItem);
        this.mWideAperturePhoto = new WideAperturePhotoImpl(this.mModel.getMediaItem(0).getFilePath(), this.mModel.getImageWidth(), this.mModel.getImageHeight());
        this.mWideAperturePhoto.setWideAperturePhotoListener(this);
        this.mWideAperturePhoto.setViewMode(2);
        this.mWideAperturePhoto.rangeMeasurePrepare();
        if (!this.mWideAperturePhoto.isDepthDataSupportRangeMeasure()) {
            showImageNotSupportDialog(this.mHost.getActivity(), R.string.range_measure_tips);
        } else if (!GallerySettings.getBoolean(this.mHost.getActivity(), GallerySettings.KEY_RANGE_MEASURE_NOTIPS, false)) {
            showTipsDialog(this.mHost.getActivity(), R.string.range_measure_tips);
        } else if (this.mToast != null) {
            this.mToast.show();
        }
    }

    private void initViews() {
        this.mRoot = (RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root);
        this.mRoot = (RelativeLayout) LayoutInflater.from(this.mHost.getActivity()).inflate(R.layout.range_measure_fragment_main, this.mRoot);
        this.mView = this.mRoot.findViewById(R.id.range_measure_root);
        this.mRefocusView = new RefocusView(this.mHost.getGalleryContext(), this.mHost.getGLRoot());
        this.mRefocusView.setGLRoot(this.mHost.getGLRoot());
        this.mRefocusView.setListener(this);
        this.mRootPane.addComponent(this.mRefocusView);
        this.mRangeMeasureView = (RangeMeasureView) this.mView.findViewById(R.id.range_measure_view);
        this.mRangeMeasureView.setListener(this);
        this.mRemeasurementButton = (Button) this.mView.findViewById(R.id.button_remeasure);
        this.mRemeasurementButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                RangeMeasurePage.this.mRangeMeasureView.removeRangeMeasureView();
            }
        });
        this.mGradient = (ImageView) this.mView.findViewById(R.id.gradient);
    }

    protected void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 2:
                this.mHost.getGLRoot().setLightsOutMode(true);
                return;
            default:
                super.onHandleMessage(msg);
                return;
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.mModel != null) {
            this.mActionBar.setActionBarVisible(false);
            this.mRefocusView.resume();
            ((GLRootView) this.mHost.getGLRoot()).setOnSystemUiVisibilityChangeListener(this.mUiVisibility);
            LayoutHelper.getNavigationBarHandler().update();
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.mToast != null) {
            this.mToast.cancel();
        }
        if (this.mModel != null) {
            this.mRefocusView.pause();
            ((GLRootView) this.mHost.getGLRoot()).setOnSystemUiVisibilityChangeListener(null);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mRefocusView.destroy();
        if (this.mWideAperturePhoto != null) {
            this.mWideAperturePhoto.cleanupResource();
        }
        this.mRoot.removeView(this.mView);
    }

    protected void onGLRootLayout(boolean changeSize, int left, int top, int right, int bottom) {
        this.mRefocusView.layout(0, 0, right - left, bottom - top);
    }

    protected boolean onBackPressed() {
        this.mHandler.sendEmptyMessage(50);
        return super.onBackPressed();
    }

    public void onNavigationBarChanged(boolean show, int height) {
        if (this.mRemeasurementButton != null) {
            int remeasureButtonWidth;
            Configuration config = this.mHost.getActivity().getResources().getConfiguration();
            int navigationBarHeight = config.orientation == 2 ? 0 : height;
            if (config.orientation == 2) {
                remeasureButtonWidth = this.mHost.getActivity().getResources().getDimensionPixelSize(R.dimen.remeasure_button_landscape_width);
            } else {
                remeasureButtonWidth = this.mHost.getActivity().getResources().getDimensionPixelSize(R.dimen.remeasure_button_width);
            }
            LayoutParams params = (LayoutParams) this.mRemeasurementButton.getLayoutParams();
            params.bottomMargin = this.mHost.getActivity().getResources().getDimensionPixelSize(R.dimen.remeasure_button_bottom_padding) + navigationBarHeight;
            params.width = remeasureButtonWidth;
            this.mRemeasurementButton.setLayoutParams(params);
        }
    }

    private void showTipsDialog(Context context, int titleId) {
        View tipsView = LayoutInflater.from(context).inflate(R.layout.range_measure_tips_dialog, null);
        this.mNoRangeMeasureTipsCheckBox = (CheckBox) tipsView.findViewById(R.id.range_measure_check_notips);
        new Builder(context).setTitle(titleId).setView(tipsView).setCancelable(false).setPositiveButton(R.string.ok, this.mTipsDialogListener).create().show();
    }

    private void showResultErrorDialog(Context context, int titleId) {
        new Builder(context).setTitle(titleId).setView(LayoutInflater.from(context).inflate(R.layout.range_measure_result_error_dialog, null)).setCancelable(false).setPositiveButton(R.string.ok, this.mErrorDialogListener).create().show();
    }

    private void showImageNotSupportDialog(Context context, int titleId) {
        new Builder(context).setTitle(titleId).setView(LayoutInflater.from(context).inflate(R.layout.range_measure_not_support_dialog, null)).setCancelable(false).setPositiveButton(R.string.ok, this.mNotSupportDialogListener).create().show();
    }

    public void onLoadStateChange(int state) {
        if (state == 2) {
            this.mHandler.sendEmptyMessage(50);
        }
    }

    public void onDecodeImageComplete() {
        this.mImageDecodeCompleted = true;
        this.mRefocusView.refresh();
    }

    public boolean onTouch(MotionEvent event) {
        if (!isImageReady() || this.mRangeMeasureView == null) {
            return false;
        }
        return this.mRangeMeasureView.onTouch(event);
    }

    public boolean isRangeMeasureMode() {
        return true;
    }

    public void refreshRangeMeasureView() {
        this.mRangeMeasureView.refreshRangeMeasureView();
    }

    public int getActionBarHeight() {
        return this.mActionBar.getActionBarHeight();
    }

    protected void checkIfNeedSwapImageWidthAndHeight() {
        this.mImageWidth = this.mModel.getImageWidth();
        this.mImageHeight = this.mModel.getImageHeight();
        if (this.mWideAperturePhoto.getOrientation() == 90 || this.mWideAperturePhoto.getOrientation() == 270) {
            this.mImageWidth ^= this.mImageHeight;
            this.mImageHeight ^= this.mImageWidth;
            this.mImageWidth ^= this.mImageHeight;
        }
    }

    public int rangeMeasurePointLocated(Point mBeginPosInImage, Point mEndPosInImage) {
        this.mRemeasurementButton.setVisibility(0);
        this.mGradient.setVisibility(0);
        int result = this.mWideAperturePhoto.rangeMeasure(mBeginPosInImage.x, mBeginPosInImage.y, mEndPosInImage.x, mEndPosInImage.y);
        if (result < 0) {
            showResultErrorDialog(this.mHost.getActivity(), R.string.range_measure_tips);
        }
        return result;
    }

    public void rangeMeasurePointUnLocated() {
        this.mRemeasurementButton.setVisibility(4);
        this.mGradient.setVisibility(4);
    }

    public Point getRealPositionInImage(Point touchPosition) {
        Point position = this.mRefocusView.getRealPositionInImage(touchPosition);
        if (position.x < 0 || position.y < 0) {
            return new Point(-1, -1);
        }
        return transformToPreviewCoordinate(position);
    }

    public Point getDisplayPositionInScreen(Point imagePosition) {
        return this.mRefocusView.transformToScreenCoordinate(transformToImageCoordinate(new Point(imagePosition.x, imagePosition.y)));
    }

    public boolean isImageReady() {
        return this.mImageDecodeCompleted ? this.mImagePrepareCompleted : false;
    }

    public Point transformToPreviewCoordinate(Point point) {
        checkIfNeedSwapImageWidthAndHeight();
        this.mWideAperturePhoto.resizePhoto(this.mImageWidth, this.mImageHeight);
        return this.mWideAperturePhoto.rangeMeasureTransformToPreviewCoordinate(point);
    }

    public Point transformToImageCoordinate(Point point) {
        checkIfNeedSwapImageWidthAndHeight();
        this.mWideAperturePhoto.resizePhoto(this.mImageWidth, this.mImageHeight);
        return this.mWideAperturePhoto.rangeMeasureTransformToPhotoCoordinate(point);
    }

    public void onPrepareComplete() {
        this.mImagePrepareCompleted = true;
    }

    public void onRefocusComplete() {
    }

    public void onSaveFileComplete(int saveState) {
    }

    public void onSaveAsComplete(int saveState, String filePath) {
    }

    public void finishRefocus() {
    }

    public void onGotFocusPoint() {
    }

    protected void onModelResume() {
        byte[] EDoFPhoto = this.mWideAperturePhoto.getEDoFPhoto();
        if (EDoFPhoto.length != 0) {
            this.mModel.resume(EDoFPhoto, 0, this.mWideAperturePhoto.getEDoFPhotoLen());
        } else {
            super.onModelResume();
        }
    }

    protected AbsPhotoView createPhotoView(GalleryContext context, GLRoot root) {
        return new EmptyPhotoView();
    }
}
