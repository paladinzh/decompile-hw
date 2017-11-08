package com.huawei.gallery.app;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar.OnMenuVisibilityListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.services.core.AMapException;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.app.IntentChooser;
import com.android.gallery3d.app.IntentChooser.IShareItem;
import com.android.gallery3d.app.IntentChooser.IntentChooserDialogClickListener;
import com.android.gallery3d.app.PhotoDataAdapter;
import com.android.gallery3d.app.SinglePhotoDataAdapter;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.CloudLocalAlbum;
import com.android.gallery3d.data.DiscoverLocation;
import com.android.gallery3d.data.GalleryMediaItem;
import com.android.gallery3d.data.GalleryMediaTimegroupAlbum;
import com.android.gallery3d.data.GalleryRecycleAlbum;
import com.android.gallery3d.data.IVideo;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.LocalMediaItem;
import com.android.gallery3d.data.MediaDetails;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MtpSource;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.UriAlbum;
import com.android.gallery3d.data.UriImage;
import com.android.gallery3d.menuexecutor.MenuEnableCtrller;
import com.android.gallery3d.settings.GallerySettings;
import com.android.gallery3d.ui.DetailLayout;
import com.android.gallery3d.ui.DetailLayout.OnDetailLoadedDelegate;
import com.android.gallery3d.ui.DetailsAddressResolver.AddressResolvingListener;
import com.android.gallery3d.ui.DetailsHelper;
import com.android.gallery3d.ui.DetailsHelper.CloseListener;
import com.android.gallery3d.ui.DetailsHelper.DetailsSource;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLRootView;
import com.android.gallery3d.ui.LivePhotoView;
import com.android.gallery3d.ui.MediaItemInfoView;
import com.android.gallery3d.ui.MenuExecutor.ExtraActionListener;
import com.android.gallery3d.ui.MenuExecutor.ProgressListener;
import com.android.gallery3d.ui.MenuExecutor.SimpleProgressListener;
import com.android.gallery3d.ui.MenuExecutorFactory.Style;
import com.android.gallery3d.ui.PhotoMagnifierManager;
import com.android.gallery3d.ui.PhotoView;
import com.android.gallery3d.ui.SimpleGestureListener;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.ContextedUtils;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.TraceController;
import com.autonavi.amap.mapcore.ADGLMapAnimGroup;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.actionbar.ActionBarStateBase;
import com.huawei.gallery.actionbar.DetailActionMode;
import com.huawei.gallery.actionbar.GalleryActionBar;
import com.huawei.gallery.actionbar.view.ActionItem;
import com.huawei.gallery.app.plugin.PhotoExtraButton;
import com.huawei.gallery.app.plugin.PhotoFragmentPluginManager;
import com.huawei.gallery.app.plugin.PhotoFragmentPluginManager.PluginHost;
import com.huawei.gallery.barcode.BarcodeInfoProcess;
import com.huawei.gallery.barcode.BarcodeInfoProcess.ReceivedBarcodeResultListener;
import com.huawei.gallery.barcode.BarcodeScanResultItem;
import com.huawei.gallery.data.CommentHelper;
import com.huawei.gallery.data.CommentInfo;
import com.huawei.gallery.editor.filters.ImageFilterFx;
import com.huawei.gallery.editor.pipeline.EditorLoadLib;
import com.huawei.gallery.editor.screenshotseditor.ui.ScreenShotsEditorView;
import com.huawei.gallery.editor.tools.MakeupUtils;
import com.huawei.gallery.editor.ui.BaseEditorView;
import com.huawei.gallery.editor.ui.BaseEditorView.Delegate;
import com.huawei.gallery.editor.ui.EditorView;
import com.huawei.gallery.extfile.FyuseManager;
import com.huawei.gallery.freeshare.FreeShare;
import com.huawei.gallery.freeshare.FreeShareAdapter;
import com.huawei.gallery.freeshare.FreeShareProxy;
import com.huawei.gallery.map.app.MapUtils;
import com.huawei.gallery.map.data.MapAlbum;
import com.huawei.gallery.multiscreen.MultiScreen;
import com.huawei.gallery.multiscreen.MultiScreen.MultiScreenListener;
import com.huawei.gallery.multiscreen.MultiScreenStub;
import com.huawei.gallery.photorectify.PhotoRectifyPage;
import com.huawei.gallery.photoshare.PhotoShareItem;
import com.huawei.gallery.photoshare.ui.ShareToCloudAlbumActivity;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.refocus.app.RefocusPage;
import com.huawei.gallery.refocus.wideaperture.RangeMeasure.app.RangeMeasurePage;
import com.huawei.gallery.refocus.wideaperture.photo3dview.app.WideAperturePhoto3DActivity;
import com.huawei.gallery.refocus.wideaperture.utils.WideAperturePhotoUtil;
import com.huawei.gallery.share.HwCustUtilsWrapper;
import com.huawei.gallery.ui.GalleryCustEditor;
import com.huawei.gallery.ui.GalleryCustEditor.EditorController;
import com.huawei.gallery.util.ColorfulUtils;
import com.huawei.gallery.util.DeleteMsgUtil;
import com.huawei.gallery.util.LayoutHelper;
import com.huawei.gallery.util.ScreenController;
import com.huawei.gallery.util.UIUtils;
import com.huawei.gallery.util.VideoEditorController;
import com.huawei.watermark.ui.WMComponent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class PhotoPage extends AbsPhotoPage implements AddressResolvingListener, Delegate, OnClickListener, OnDetailLoadedDelegate, PluginHost, IPhotoPage {
    private static final /* synthetic */ int[] -com-huawei-gallery-actionbar-ActionSwitchesValues = null;
    private static HwCustPhotoPage mHwCustPhotoPage = ((HwCustPhotoPage) HwCustUtilsWrapper.createObj(HwCustPhotoPage.class, new Object[0]));
    private Object EDIT_OBJ = new Object();
    protected final ActionBarProgressActionListener mActionProgressActionListener = new ActionBarProgressActionListener() {
        public void onStart() {
            if (PhotoPage.this.mHandler != null) {
                PhotoPage.this.mHandler.removeMessages(1);
            }
        }

        public void onEnd() {
            PhotoPage.this.refreshHidingMessage();
        }
    };
    private Handler mAsyncProcessingHandler;
    private BarcodeInfoProcess mBip;
    private boolean mCalledToSimpleEditor;
    private Runnable mChkPkgAndUpdateMenu = new Runnable() {
        public void run() {
            TraceController.beginSection("PhotoPage.onResume.checkEdit");
            boolean useMoreEdit = GalleryUtils.hasMoreEditorForPic(PhotoPage.this.mHost.getActivity());
            TraceController.endSection();
            TraceController.beginSection("PhotoPage.onResume.checkMap");
            boolean mapAbailable = GalleryUtils.isAnyMapAvailable(PhotoPage.this.mHost.getActivity());
            TraceController.endSection();
            PhotoPage.this.mIsScannerInstalled = PhotoPage.this.checkPackage("com.huawei.scanner");
            if (useMoreEdit != PhotoPage.this.mUseMoreEdit || PhotoPage.this.mIsMapAvailable != mapAbailable) {
                TraceController.beginSection("PhotoPage.onResume.updateMenu");
                PhotoPage.this.mUseMoreEdit = useMoreEdit;
                PhotoPage.this.mIsMapAvailable = mapAbailable;
                PhotoPage.this.updateMenuOperations();
                TraceController.endSection();
            }
        }
    };
    private CommentInfo mCommentInfo;
    private CommentLoader mCommentLoader = new CommentLoader();
    protected ProgressListener mConfirmDialogListener = new SimpleProgressListener() {
        private String reCoveryToastContent = null;

        public void setOnCompleteToastContent(String content) {
            this.reCoveryToastContent = content;
        }

        public void onProgressComplete(int result) {
            if (result == 1 && this.reCoveryToastContent != null) {
                ContextedUtils.showToastQuickly(PhotoPage.this.mHost.getActivity(), this.reCoveryToastContent, 0);
            }
            setOnCompleteToastContent(null);
        }

        public void onConfirmDialogShown() {
            PhotoPage.this.mHandler.removeMessages(1);
        }

        public ArrayList<Path> getExecutePath() {
            ArrayList<Path> paths = PhotoPage.this.mSelectionManager.getSelected(false);
            if (paths == null || paths.size() <= 0) {
                return null;
            }
            return paths;
        }

        public void onConfirmDialogDismissed(boolean confirmed) {
            PhotoPage.this.refreshHidingMessage();
            if (confirmed) {
                ReportToBigData.report(107);
            }
        }

        public boolean excuteExtraAction(ExtraActionListener listener, int deleteFlag) {
            PhotoPage.this.mHost.getGLRoot().lockRenderThread();
            try {
                GalleryLog.d("PhotoPage", "excute extra action, start delete animation");
                if (RecycleUtils.supportRecycle() || !(PhotoPage.this.mCurrentPhoto instanceof GalleryMediaItem) || deleteFlag != 1 || ((GalleryMediaItem) PhotoPage.this.mCurrentPhoto).getCloudMediaId() == -1) {
                    PhotoPage.this.mPhotoView.autoSlidePicture(listener);
                } else if (listener != null) {
                    listener.onExecuteExtraActionEnd();
                }
                PhotoPage.this.mHost.getGLRoot().unlockRenderThread();
                return true;
            } catch (Throwable th) {
                PhotoPage.this.mHost.getGLRoot().unlockRenderThread();
            }
        }

        public boolean isHicloudAlbum() {
            return PhotoPage.this.isHicloudAlbum();
        }

        public boolean isSyncedAlbum() {
            return PhotoPage.this.isSyncAlbum();
        }
    };
    private AlertDialog mCreateDialog;
    private Action[] mDefaultMenu = new Action[]{Action.SHARE, Action.NOT_MYFAVORITE, Action.DEL, Action.EDIT, Action.PHOTOSHARE_DOWNLOAD, Action.ADD_COMMENT, Action.EDIT_COMMENT, Action.MORE_EDIT, Action.RANGE_MEASURE, Action.SLIDESHOW, Action.PRINT, Action.RENAME, Action.SETAS, Action.ROTATE_RIGHT, Action.SHOW_ON_MAP, Action.SEE_BARCODE_INFO};
    private DetailLayout mDetailView;
    private DetailsHelper mDetailsHelper;
    private GalleryCustEditor mEditor;
    private BaseEditorView mEditorView;
    private boolean mFlingUpAllowed = false;
    private FreeShare mFreeShare;
    private FreeShareAdapter mFreeShareAdapter;
    private FreeShareItem mFreeShareItem;
    private boolean mFromCamera;
    private HandlerThread mHandlerThread;
    private boolean mInFreeShareMode = false;
    private boolean mInPluginMode = false;
    private IntentChooser mIntentChooser;
    private boolean mIsEditorViewAlreadyCreate = false;
    private boolean mIsFreeShareInit = false;
    private boolean mIsMapAvailable;
    private boolean mIsPickWithEdit;
    private boolean mIsPickWithPreview;
    private boolean mIsPreviewMode;
    private boolean mIsScannerInstalled;
    private boolean mIsSecureAlbum;
    private boolean mIsSetingResult = false;
    private boolean mIsViewAsUriImage;
    private boolean mKeepFromCamera;
    private boolean mKeepRemoteControl = false;
    private LivePhotoView mLivePhotoView;
    private boolean mMediaInfoLocationAllowed = false;
    private boolean mMediaInfoTimeAllowed = false;
    protected final OnMenuVisibilityListener mMenuVisibilityListener = new OnMenuVisibilityListener() {
        public void onMenuVisibilityChanged(boolean isVisible) {
            PhotoPage.this.mMenuVisible = isVisible;
            if (!isVisible) {
                PhotoPage.this.refreshHidingMessage();
            } else if (!PhotoPage.this.mIsScannerInstalled || !PhotoPage.this.isValidImage() || !PhotoPage.this.mCurrentPhoto.isBarcodeNeedScan()) {
            } else {
                if (!DrmUtils.isDrmFile(PhotoPage.this.mCurrentPhoto.getFilePath()) || !DrmUtils.haveCountConstraints(PhotoPage.this.mCurrentPhoto.getFilePath(), 7)) {
                    PhotoPage.this.mHandler.removeMessages(39);
                    PhotoPage.this.mHandler.sendEmptyMessageDelayed(39, 0);
                }
            }
        }
    };
    private MovingPicutreHelper mMovingPicutreHelper = new MovingPicutreHelper();
    private MultiScreenStub mMultiScreen = new MultiScreenStub();
    private MultiScreenListener mMultiScreenListener = new MultiScreenListener() {
        public void onUpdateActionItem(boolean existed, boolean rendering) {
            if (PhotoPage.this.mIsActive && !PhotoPage.this.mInPluginMode && !PhotoPage.this.inEditorMode()) {
                ActionBarStateBase state = PhotoPage.this.getGalleryActionBar().getCurrentMode();
                if (state instanceof DetailActionMode) {
                    DetailActionMode am = (DetailActionMode) state;
                    if (am.getMiddleActionItem() != null) {
                        boolean needReset;
                        Action currentAction = am.getMiddleActionItem().getAction();
                        if (currentAction == null || Action.MULTISCREEN.equalAction(currentAction)) {
                            needReset = true;
                        } else {
                            needReset = Action.MULTISCREEN_ACTIVITED.equalAction(currentAction);
                        }
                        if (existed) {
                            if (rendering) {
                                currentAction = Action.MULTISCREEN_ACTIVITED;
                            } else {
                                currentAction = Action.MULTISCREEN;
                            }
                        } else if (needReset) {
                            currentAction = Action.NONE;
                        }
                        am.setMiddleAction(currentAction);
                        if (!PhotoPage.this.mTipsManager.ready || !PhotoPage.this.mTipsManager.needTips) {
                            return;
                        }
                        if (existed) {
                            PhotoPage.this.mTipsManager.showTipsDelay();
                            PhotoPage.this.mHandler.removeMessages(1);
                            PhotoPage.this.showBars(true);
                        } else {
                            PhotoPage.this.mTipsManager.showing = false;
                            PhotoPage.this.mTipsManager.hideTips(false);
                        }
                    }
                }
            }
        }

        public void requestMedia() {
            PhotoPage.this.mMultiScreen.play(PhotoPage.this.mCurrentPhoto, true);
        }

        public void onUnInitServiceTimeOut() {
            PhotoPage.this.mShouldInitMultiScreenOnResume = true;
            PhotoPage.this.mMultiScreen.resetService();
        }
    };
    private WeakReference<Toast> mMyFavoriteToast = null;
    private boolean mNeedUpdateVersionByFavorite = false;
    private Uri[] mNfcPushUris = new Uri[1];
    private boolean mOnCreateDone = false;
    private MediaItemInfoView mPhotoInfoView;
    private PhotoShareItem mPhotoShareItem;
    private PictureFullViewCallback mPictureFullViewCallback;
    private PhotoFragmentPluginManager mPluginManager;
    private final ReceivedBarcodeResultListener mReceivedBarcodeResultListener = new ReceivedBarcodeResultListener() {
        public void onBarcodeResultReceived() {
            PhotoPage.this.updateMenuOperations();
        }
    };
    private BroadcastReceiver mRemoteBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (PhotoPage.this.mFromCamera) {
                PhotoPage.this.mHost.getStateManager().finishState(PhotoPage.this);
            }
        }
    };
    private MediaItem mResolveAddressPhoto = null;
    private ScreenController mScreenController;
    private EditText mSetNameTextView;
    private boolean mShouldInitMultiScreenOnResume = false;
    private boolean mShowDetails;
    private SimpleGestureListener mSimpleGestureListener = new SimpleGestureListener() {
        public void onLongPress(MotionEvent e) {
            GalleryLog.d("PhotoPage", "page onLongPress");
            MediaItem item = PhotoPage.this.mCurrentPhoto;
            if (item != null && item.getSpecialFileType() == 50) {
                ActionBarStateBase actionBarStateBase = PhotoPage.this.mHost.getGalleryActionBar().getCurrentMode();
                if (actionBarStateBase instanceof DetailActionMode) {
                    PhotoPage.this.mLivePhotoView.setLongPress(true);
                    PhotoPage.this.mPluginManager.onEventsHappens(item, ((DetailActionMode) actionBarStateBase).getExtraButton());
                }
            }
            super.onLongPress(e);
        }

        public boolean onScroll(float dx, float dy, float totalX, float totalY) {
            return PhotoPage.this.mLivePhotoView.isPlaying();
        }

        public void onDown(float x, float y) {
            GalleryLog.d("PhotoPage", "page onDown ");
            PhotoPage.this.mLivePhotoView.setLongPress(false);
            PhotoPage.this.mLivePhotoView.stop();
            super.onDown(x, y);
        }

        public void onUp() {
            GalleryLog.d("PhotoPage", "page onUp ");
            PhotoPage.this.mLivePhotoView.setLongPress(false);
            PhotoPage.this.mLivePhotoView.stop();
            super.onUp();
        }
    };
    private MultiScreenTipsManager mTipsManager;
    private boolean mUseMoreEdit = false;

    public interface PreviewDelgete {
        void setPreviewVisible(int i);

        void updatePreviewView(Bitmap bitmap);
    }

    public interface ActionBarProgressActionListener {
        void onEnd();

        void onStart();
    }

    private class CommentLoader extends BaseJob<Void> implements Runnable {
        private MediaItem mMediaItem;
        private volatile int mTaskCount;

        private CommentLoader() {
            this.mTaskCount = 0;
        }

        public void parseItemComment(MediaItem mediaItem) {
            if (!PhotoPage.this.mCalledToSimpleEditor) {
                this.mMediaItem = mediaItem;
                GalleryContext context = PhotoPage.this.mHost.getGalleryContext();
                if (context != null) {
                    context.getThreadPool().submit(this, null, 4);
                    addSync(1);
                }
            }
        }

        private synchronized int addSync(int i) {
            this.mTaskCount += i;
            return this.mTaskCount;
        }

        public String workContent() {
            return "load comment ";
        }

        public boolean isHeavyJob() {
            return true;
        }

        public Void run(JobContext jc) {
            MediaItem process = this.mMediaItem;
            PhotoPage.this.mCommentInfo = null;
            if (addSync(-1) == 0) {
                PhotoPage.this.mCommentInfo = readCommentFromFile(process);
                PhotoPage.this.mHandler.post(this);
            }
            return null;
        }

        private CommentInfo readCommentFromFile(MediaItem currentPhoto) {
            CommentInfo comment = null;
            TraceController.traceBegin("PhotoPage.updateCommentInfo");
            if (currentPhoto.supportComment()) {
                comment = CommentHelper.readComment(currentPhoto.getFilePath());
            }
            TraceController.traceEnd();
            return comment;
        }

        public void run() {
            PhotoPage.this.updateMenuForUserComment();
        }
    }

    private class FreeShareItem implements IShareItem {
        private Drawable icon;
        private boolean isClicked = false;
        private boolean isNeedSwitchPhoto = false;
        private String label;
        private ComponentName mComponent;

        public FreeShareItem() {
            this.label = PhotoPage.this.mHost.getActivity().getString(R.string.freeshare_title);
            this.icon = PhotoPage.this.mHost.getActivity().getResources().getDrawable(R.drawable.ic_transfer);
            this.mComponent = new ComponentName(PhotoPage.this.mHost.getActivity(), FreeshareStub.class);
        }

        public String getLabel() {
            return this.label;
        }

        public Drawable getIcon() {
            return this.icon;
        }

        public ComponentName getComponent() {
            return this.mComponent;
        }

        public void onClicked(Intent intent) {
            int i = 1;
            String action = intent.getAction();
            if ("android.intent.action.SEND".equals(action)) {
                Uri uri = (Uri) intent.getParcelableExtra("android.intent.extra.STREAM");
                if (uri == null) {
                    Log.d("PhotoPage", "freeshare receive null Uri ");
                    return;
                } else if (PhotoPage.this.mModel.getCurrentMediaItem() == null || !PhotoPage.this.mModel.getCurrentMediaItem().getContentUri().equals(uri)) {
                    Path path = PhotoPage.this.mHost.getGalleryContext().getDataManager().findPathByUri(uri, null);
                    setFreeShareItemNeedSwitchPhoto(true);
                    setFreeShareItemClicked(true);
                    PhotoPage.this.setCurrentPhotoByPath(path, null, true);
                    PhotoPage.this.mPhotoView.setVisibility(0);
                    PhotoPage.this.updateMenuOperations();
                    PhotoPage.this.mMultiScreen.requestRefreshInfo();
                    return;
                } else {
                    if (PhotoPage.this.mModel.getScreenNail(0) != null) {
                        setFreeShareItemClicked(false);
                        PhotoPage photoPage = PhotoPage.this;
                        if (PhotoPage.this.mFlingUpAllowed) {
                            i = 3;
                        }
                        photoPage.triggerFreeShare(i);
                    } else {
                        setFreeShareItemClicked(true);
                    }
                    return;
                }
            }
            Log.d("PhotoPage", "freeshare doesn't support " + action);
        }

        public int getKey() {
            return 1;
        }

        public String[] getSupportActions() {
            return new String[]{"android.intent.action.SEND"};
        }

        private void setFreeShareItemClicked(boolean clicked) {
            this.isClicked = clicked;
        }

        private boolean isFreeShareItemClicked() {
            return this.isClicked;
        }

        private void setFreeShareItemNeedSwitchPhoto(boolean needSwitchPhoto) {
            this.isNeedSwitchPhoto = needSwitchPhoto;
        }

        private boolean isFreeShareItemNeedSwitchPhoto() {
            return this.isNeedSwitchPhoto;
        }
    }

    private class MovingPicutreHelper {
        private int mIndex;
        private MediaItem mItem;

        private MovingPicutreHelper() {
        }

        void updatePhoto(MediaItem item) {
            this.mIndex = PhotoPage.this.mCurrentIndex;
            this.mItem = item;
            setWantPictureCenterCallbacks(true);
        }

        void onPictureCenter() {
            if (PhotoPage.this.mOnCreateDone) {
                setWantPictureCenterCallbacks(false);
                PhotoPage.this.mLivePhotoView.updatePhoto(this.mIndex, this.mItem);
            }
        }

        private void setWantPictureCenterCallbacks(final boolean wantCallback) {
            PhotoPage.this.mHandler.post(new Runnable() {
                public void run() {
                    PhotoPage.this.mPhotoView.setWantPictureCenterCallbacks(wantCallback);
                }
            });
        }
    }

    @TargetApi(17)
    private class MultiScreenTipsManager implements OnLayoutChangeListener, OnClickListener {
        private ImageView arrow;
        private TextView hint;
        private int marginOffset = -1;
        private boolean needTips;
        private boolean ready;
        private boolean showing;
        private boolean shown;
        private RelativeLayout tips;
        private WindowManager windowMgr;

        public MultiScreenTipsManager() {
            this.needTips = PreferenceManager.getDefaultSharedPreferences(PhotoPage.this.mHost.getActivity()).getBoolean("key-tips-shown", true);
            this.shown = false;
            this.showing = false;
        }

        @SuppressLint({"InflateParams"})
        private void setupTips() {
            if (this.tips == null) {
                this.tips = (RelativeLayout) LayoutInflater.from(PhotoPage.this.mHost.getActivity()).inflate(R.layout.multiscreen_tips, null);
                this.tips.setOnClickListener(this);
                this.arrow = (ImageView) this.tips.findViewById(R.id.arrow);
                this.hint = (TextView) this.tips.findViewById(R.id.hint);
                this.windowMgr = (WindowManager) PhotoPage.this.mHost.getActivity().getSystemService("window");
            }
        }

        public void showTipsDelay() {
            showTipsDelay(500);
        }

        public void showTipsDelay(int delay) {
            PhotoPage.this.mTipsManager.showing = true;
            PhotoPage.this.mHandler.removeMessages(35);
            PhotoPage.this.mHandler.sendEmptyMessageDelayed(35, (long) delay);
        }

        public void showTips() {
            if (!PhotoPage.this.mIsPreviewMode) {
                setupTips();
                this.showing = false;
                ActionBarStateBase state = PhotoPage.this.mActionBar.getCurrentMode();
                if (state instanceof DetailActionMode) {
                    ActionItem item = ((DetailActionMode) state).getMiddleActionItem();
                    if (item != null) {
                        int arrowStartMargin = GalleryUtils.getAbsoluteLeft(item.asView());
                        if (Action.MULTISCREEN.equalAction(item.getAction()) || Action.MULTISCREEN_ACTIVITED.equalAction(item.getAction())) {
                            View itemView = item.asView();
                            if (itemView.getVisibility() != 0) {
                                PhotoPage.this.refreshHidingMessage();
                                return;
                            }
                            boolean refresh = this.shown;
                            this.shown = true;
                            LayoutParams arrowParams = (LayoutParams) this.arrow.getLayoutParams();
                            LayoutParams hintParams = (LayoutParams) this.hint.getLayoutParams();
                            if (this.marginOffset < 0) {
                                this.arrow.measure(0, 0);
                                this.marginOffset = (itemView.getMeasuredWidth() / 2) - this.arrow.getMeasuredWidth();
                                this.hint.measure(0, 0);
                            }
                            arrowParams.setMarginStart(this.marginOffset + arrowStartMargin);
                            hintParams.setMarginStart(((itemView.getMeasuredWidth() / 2) + arrowStartMargin) - (this.hint.getMeasuredWidth() / 2));
                            arrowParams.topMargin = itemView.getBottom();
                            this.tips.updateViewLayout(this.arrow, arrowParams);
                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            lp.width = -1;
                            lp.height = -1;
                            lp.flags |= 1032;
                            lp.format = -3;
                            if (refresh) {
                                this.windowMgr.updateViewLayout(this.tips, lp);
                            } else {
                                this.windowMgr.addView(this.tips, lp);
                            }
                            PhotoPage.this.showBars(true);
                            return;
                        }
                        PhotoPage.this.refreshHidingMessage();
                        return;
                    }
                    return;
                }
                PhotoPage.this.refreshHidingMessage();
            }
        }

        public void hideTips(boolean noRemind) {
            if (this.shown) {
                this.shown = false;
                if (this.tips != null) {
                    this.windowMgr.removeView(this.tips);
                    if (noRemind) {
                        PreferenceManager.getDefaultSharedPreferences(PhotoPage.this.mHost.getActivity()).edit().putBoolean("key-tips-shown", false).commit();
                        this.needTips = false;
                    }
                    PhotoPage.this.refreshHidingMessage();
                }
            }
        }

        public void setUp() {
            PhotoPage.this.mHost.getActivity().getWindow().getDecorView().addOnLayoutChangeListener(this);
            this.ready = true;
        }

        public void cleanUp() {
            PhotoPage.this.mHost.getActivity().getWindow().getDecorView().removeOnLayoutChangeListener(this);
            this.ready = false;
        }

        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            boolean changed = true;
            if (left - oldLeft == 0 && top - oldTop == 0 && right - oldRight == 0 && bottom - oldBottom == 0) {
                changed = false;
            }
            if (changed && this.needTips && !this.showing) {
                showTipsDelay(50);
            }
        }

        public void onClick(View v) {
            hideTips(true);
        }
    }

    private class MyDetailsSource implements DetailsSource {
        private MyDetailsSource() {
        }

        public MediaDetails getDetails() {
            MediaItem mediaItem = PhotoPage.this.mModel.getMediaItem(0);
            if (mediaItem != null) {
                return mediaItem.getDetails();
            }
            return null;
        }

        public int setIndex() {
            return PhotoPage.this.mModel.getCurrentIndex();
        }
    }

    public class PictureFullViewCallback {
        private int mCallbackType;

        public PictureFullViewCallback(int type) {
            this.mCallbackType = type;
        }

        public void run() {
            switch (this.mCallbackType) {
                case 0:
                case 1:
                    PhotoPage.this.switchToAllFocusFragment();
                    return;
                case 2:
                    PhotoPage.this.switchTo3DViewPage();
                    return;
                case 3:
                    PhotoPage.this.switchToRangeMeasureFragment();
                    return;
                default:
                    return;
            }
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-actionbar-ActionSwitchesValues() {
        if (-com-huawei-gallery-actionbar-ActionSwitchesValues != null) {
            return -com-huawei-gallery-actionbar-ActionSwitchesValues;
        }
        int[] iArr = new int[Action.values().length];
        try {
            iArr[Action.ADD.ordinal()] = 30;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Action.ADD_ALBUM.ordinal()] = 31;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Action.ADD_COMMENT.ordinal()] = 1;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Action.AIRSHARE.ordinal()] = 32;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Action.ALBUM.ordinal()] = 33;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Action.ALL.ordinal()] = 34;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Action.BACK.ordinal()] = 35;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Action.CANCEL_DETAIL.ordinal()] = 2;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Action.COLLAGE.ordinal()] = 36;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Action.COMMENT.ordinal()] = 37;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Action.COPY.ordinal()] = 38;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[Action.DEALL.ordinal()] = 39;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[Action.DEL.ordinal()] = 3;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[Action.DETAIL.ordinal()] = 4;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[Action.DYNAMIC_ALBUM.ordinal()] = 40;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[Action.EDIT.ordinal()] = 5;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[Action.EDIT_COMMENT.ordinal()] = 6;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[Action.GOTO_GALLERY.ordinal()] = 7;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[Action.HIDE.ordinal()] = 41;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[Action.INFO.ordinal()] = 42;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[Action.KEYGUARD_LIKE.ordinal()] = 43;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[Action.KEYGUARD_NOT_LIKE.ordinal()] = 44;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[Action.LOOPPLAY.ordinal()] = 45;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[Action.MAP.ordinal()] = 46;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[Action.MENU.ordinal()] = 47;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[Action.MORE_EDIT.ordinal()] = 8;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[Action.MOVE.ordinal()] = 48;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[Action.MOVEIN.ordinal()] = 49;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[Action.MOVEOUT.ordinal()] = 50;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[Action.MULTISCREEN.ordinal()] = 9;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[Action.MULTISCREEN_ACTIVITED.ordinal()] = 10;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[Action.MULTI_SELECTION.ordinal()] = 51;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[Action.MULTI_SELECTION_ON.ordinal()] = 52;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[Action.MYFAVORITE.ordinal()] = 11;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[Action.NO.ordinal()] = 12;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[Action.NONE.ordinal()] = 53;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[Action.NOT_MYFAVORITE.ordinal()] = 13;
        } catch (NoSuchFieldError e37) {
        }
        try {
            iArr[Action.OK.ordinal()] = 14;
        } catch (NoSuchFieldError e38) {
        }
        try {
            iArr[Action.PHOTOSHARE_ACCOUNT.ordinal()] = 54;
        } catch (NoSuchFieldError e39) {
        }
        try {
            iArr[Action.PHOTOSHARE_ADDPICTURE.ordinal()] = 55;
        } catch (NoSuchFieldError e40) {
        }
        try {
            iArr[Action.PHOTOSHARE_BACKUP.ordinal()] = 15;
        } catch (NoSuchFieldError e41) {
        }
        try {
            iArr[Action.PHOTOSHARE_CANCEL_RECEIVE.ordinal()] = 56;
        } catch (NoSuchFieldError e42) {
        }
        try {
            iArr[Action.PHOTOSHARE_CLEAR.ordinal()] = 57;
        } catch (NoSuchFieldError e43) {
        }
        try {
            iArr[Action.PHOTOSHARE_COMBINE.ordinal()] = 58;
        } catch (NoSuchFieldError e44) {
        }
        try {
            iArr[Action.PHOTOSHARE_CONTACT.ordinal()] = 59;
        } catch (NoSuchFieldError e45) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_PEOPLE_TAG.ordinal()] = 60;
        } catch (NoSuchFieldError e46) {
        }
        try {
            iArr[Action.PHOTOSHARE_CREATE_NEW_SHARE.ordinal()] = 61;
        } catch (NoSuchFieldError e47) {
        }
        try {
            iArr[Action.PHOTOSHARE_DELETE.ordinal()] = 62;
        } catch (NoSuchFieldError e48) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD.ordinal()] = 63;
        } catch (NoSuchFieldError e49) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOADING.ordinal()] = 64;
        } catch (NoSuchFieldError e50) {
        }
        try {
            iArr[Action.PHOTOSHARE_DOWNLOAD_START.ordinal()] = 65;
        } catch (NoSuchFieldError e51) {
        }
        try {
            iArr[Action.PHOTOSHARE_EDITSHARE.ordinal()] = 66;
        } catch (NoSuchFieldError e52) {
        }
        try {
            iArr[Action.PHOTOSHARE_EMAIL.ordinal()] = 67;
        } catch (NoSuchFieldError e53) {
        }
        try {
            iArr[Action.PHOTOSHARE_LINK.ordinal()] = 68;
        } catch (NoSuchFieldError e54) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_DOWNLOAD.ordinal()] = 69;
        } catch (NoSuchFieldError e55) {
        }
        try {
            iArr[Action.PHOTOSHARE_MANAGE_UPLOAD.ordinal()] = 70;
        } catch (NoSuchFieldError e56) {
        }
        try {
            iArr[Action.PHOTOSHARE_MESSAGE.ordinal()] = 71;
        } catch (NoSuchFieldError e57) {
        }
        try {
            iArr[Action.PHOTOSHARE_MOVE.ordinal()] = 72;
        } catch (NoSuchFieldError e58) {
        }
        try {
            iArr[Action.PHOTOSHARE_MULTI_DOWNLOAD.ordinal()] = 73;
        } catch (NoSuchFieldError e59) {
        }
        try {
            iArr[Action.PHOTOSHARE_NOT_THIS_PERSON.ordinal()] = 74;
        } catch (NoSuchFieldError e60) {
        }
        try {
            iArr[Action.PHOTOSHARE_PAUSE.ordinal()] = 75;
        } catch (NoSuchFieldError e61) {
        }
        try {
            iArr[Action.PHOTOSHARE_REMOVE_PEOPLE_TAG.ordinal()] = 76;
        } catch (NoSuchFieldError e62) {
        }
        try {
            iArr[Action.PHOTOSHARE_RENAME.ordinal()] = 77;
        } catch (NoSuchFieldError e63) {
        }
        try {
            iArr[Action.PHOTOSHARE_SETTINGS.ordinal()] = 78;
        } catch (NoSuchFieldError e64) {
        }
        try {
            iArr[Action.PHOTOSHARE_UPLOAD_START.ordinal()] = 79;
        } catch (NoSuchFieldError e65) {
        }
        try {
            iArr[Action.PRINT.ordinal()] = 16;
        } catch (NoSuchFieldError e66) {
        }
        try {
            iArr[Action.RANGE_MEASURE.ordinal()] = 17;
        } catch (NoSuchFieldError e67) {
        }
        try {
            iArr[Action.RECYCLE_CLEAN_BIN.ordinal()] = 80;
        } catch (NoSuchFieldError e68) {
        }
        try {
            iArr[Action.RECYCLE_DELETE.ordinal()] = 81;
        } catch (NoSuchFieldError e69) {
        }
        try {
            iArr[Action.RECYCLE_RECOVERY.ordinal()] = 82;
        } catch (NoSuchFieldError e70) {
        }
        try {
            iArr[Action.REDO.ordinal()] = 18;
        } catch (NoSuchFieldError e71) {
        }
        try {
            iArr[Action.REMOVE.ordinal()] = 83;
        } catch (NoSuchFieldError e72) {
        }
        try {
            iArr[Action.RENAME.ordinal()] = 19;
        } catch (NoSuchFieldError e73) {
        }
        try {
            iArr[Action.RE_SEARCH.ordinal()] = 84;
        } catch (NoSuchFieldError e74) {
        }
        try {
            iArr[Action.ROTATE_LEFT.ordinal()] = 20;
        } catch (NoSuchFieldError e75) {
        }
        try {
            iArr[Action.ROTATE_RIGHT.ordinal()] = 21;
        } catch (NoSuchFieldError e76) {
        }
        try {
            iArr[Action.SAVE.ordinal()] = 22;
        } catch (NoSuchFieldError e77) {
        }
        try {
            iArr[Action.SAVE_BURST.ordinal()] = 85;
        } catch (NoSuchFieldError e78) {
        }
        try {
            iArr[Action.SEE_BARCODE_INFO.ordinal()] = 23;
        } catch (NoSuchFieldError e79) {
        }
        try {
            iArr[Action.SETAS.ordinal()] = 24;
        } catch (NoSuchFieldError e80) {
        }
        try {
            iArr[Action.SETAS_BOTH.ordinal()] = 86;
        } catch (NoSuchFieldError e81) {
        }
        try {
            iArr[Action.SETAS_FIXED.ordinal()] = 87;
        } catch (NoSuchFieldError e82) {
        }
        try {
            iArr[Action.SETAS_FIXED_ACTIVED.ordinal()] = 88;
        } catch (NoSuchFieldError e83) {
        }
        try {
            iArr[Action.SETAS_HOME.ordinal()] = 89;
        } catch (NoSuchFieldError e84) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE.ordinal()] = 90;
        } catch (NoSuchFieldError e85) {
        }
        try {
            iArr[Action.SETAS_SCROLLABLE_ACTIVED.ordinal()] = 91;
        } catch (NoSuchFieldError e86) {
        }
        try {
            iArr[Action.SETAS_UNLOCK.ordinal()] = 92;
        } catch (NoSuchFieldError e87) {
        }
        try {
            iArr[Action.SETTINGS.ordinal()] = 25;
        } catch (NoSuchFieldError e88) {
        }
        try {
            iArr[Action.SHARE.ordinal()] = 26;
        } catch (NoSuchFieldError e89) {
        }
        try {
            iArr[Action.SHOW_ON_MAP.ordinal()] = 27;
        } catch (NoSuchFieldError e90) {
        }
        try {
            iArr[Action.SINGLE_SELECTION.ordinal()] = 93;
        } catch (NoSuchFieldError e91) {
        }
        try {
            iArr[Action.SINGLE_SELECTION_ON.ordinal()] = 94;
        } catch (NoSuchFieldError e92) {
        }
        try {
            iArr[Action.SLIDESHOW.ordinal()] = 28;
        } catch (NoSuchFieldError e93) {
        }
        try {
            iArr[Action.STORY_ALBUM_REMOVE.ordinal()] = 95;
        } catch (NoSuchFieldError e94) {
        }
        try {
            iArr[Action.STORY_ITEM_REMOVE.ordinal()] = 96;
        } catch (NoSuchFieldError e95) {
        }
        try {
            iArr[Action.STORY_RENAME.ordinal()] = 97;
        } catch (NoSuchFieldError e96) {
        }
        try {
            iArr[Action.TIME.ordinal()] = 98;
        } catch (NoSuchFieldError e97) {
        }
        try {
            iArr[Action.TOGIF.ordinal()] = 99;
        } catch (NoSuchFieldError e98) {
        }
        try {
            iArr[Action.UNDO.ordinal()] = 29;
        } catch (NoSuchFieldError e99) {
        }
        try {
            iArr[Action.WITHOUT_UPDATE.ordinal()] = 100;
        } catch (NoSuchFieldError e100) {
        }
        try {
            iArr[Action.WITH_UPDATE.ordinal()] = 101;
        } catch (NoSuchFieldError e101) {
        }
        -com-huawei-gallery-actionbar-ActionSwitchesValues = iArr;
        return iArr;
    }

    protected native void destoryMorphoFilter();

    protected native void nativeApplyFilterDestroy();

    protected native void nativeApplyFilterInit();

    protected void onCreate(Bundle data, Bundle storedState) {
        TraceController.traceBegin("PhotoPage.onCreate");
        super.onCreate(data, storedState);
        this.mHandlerThread = new HandlerThread("PhotoPageAsynchronousHandler", -2);
        this.mHandlerThread.start();
        this.mAsyncProcessingHandler = new Handler(this.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 2:
                        TraceController.traceBegin("PhotoPage.INIT_FILTER_MOULD");
                        if (EditorLoadLib.FILTERJNI_LOADED) {
                            PhotoPage.this.initFilterMould();
                        }
                        TraceController.traceEnd();
                        return;
                    case 3:
                        TraceController.traceBegin("PhotoPage.DESTROY_FILTER_MOULD");
                        PhotoPage.this.destroyFilterMould();
                        TraceController.traceEnd();
                        return;
                    case 4:
                        TraceController.traceBegin("PhotoPage.DESTORY_MORPHO_FILTER");
                        PhotoPage.this.destroyMorphoFilterMould();
                        TraceController.traceEnd();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mIntentChooser = new IntentChooser(this.mHost.getActivity(), this.mIsSecureAlbum);
        this.mPhotoShareItem = new PhotoShareItem(this.mHost.getActivity());
        this.mMenuExecutor.setIntentChooser(this.mIntentChooser);
        TraceController.traceBegin("PhotoPage.onCreate.mPluginManager");
        this.mPluginManager = new PhotoFragmentPluginManager();
        this.mPluginManager.init((ViewGroup) this.mHost.getActivity().findViewById(R.id.gallery_root), this.mHost.getGalleryContext(), this, this.mActionProgressActionListener);
        this.mEditorView = createEditorView(data);
        TraceController.traceEnd();
        this.mEditorView.setGalleryContext(this.mHost.getGalleryContext());
        if (!(this.mEditorView instanceof EditorView) || this.mCalledToSimpleEditor) {
            TraceController.traceBegin("PhotoPage.onCreate.mEditorView.create");
            this.mEditorView.create();
            this.mIsEditorViewAlreadyCreate = true;
            TraceController.traceEnd();
        } else {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    TraceController.traceBegin("PhotoPage.onCreate.mEditorView.create");
                    PhotoPage.this.mEditorView.create();
                    PhotoPage.this.mIsEditorViewAlreadyCreate = true;
                    TraceController.traceEnd();
                }
            }, 300);
        }
        this.mEditorView.setParentLayout((RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root));
        this.mEditorView.setBackgroundColor(getBackgroundColor());
        this.mEditorView.setSecureCameraMode(this.mIsSecureAlbum);
        this.mRootPane.addComponent(this.mEditorView);
        TraceController.traceBegin("PhotoPage.onCreate.mPhotoInfoView");
        this.mPhotoInfoView = new MediaItemInfoView(this.mHost.getGalleryContext());
        this.mRootPane.addComponent(this.mPhotoInfoView);
        TraceController.traceEnd();
        TraceController.traceBegin("PhotoPage.onCreate.MultiScreenTipsManager");
        this.mMultiScreen.initialize(this.mHost.getActivity().getApplicationContext(), this.mHandler);
        this.mMultiScreen.addListener(this.mMultiScreenListener);
        this.mTipsManager = new MultiScreenTipsManager();
        this.mTipsManager.setUp();
        TraceController.endSection();
        setupNfcBeamPush();
        TraceController.traceBegin("PhotoPage.onCreate.mFreeShareItem");
        this.mFreeShareItem = this.mFreeShareItem == null ? new FreeShareItem() : this.mFreeShareItem;
        this.mFreeShareItem.setFreeShareItemClicked(data.getBoolean("is-free-share-item-clicked", false));
        this.mFreeShareItem.setFreeShareItemNeedSwitchPhoto(false);
        TraceController.traceEnd();
        TraceController.traceBegin("PhotoPage.onCreate send message INIT_FILTER_MOULD");
        this.mAsyncProcessingHandler.sendEmptyMessageDelayed(2, 1000);
        TraceController.traceEnd();
        this.mLivePhotoView = new LivePhotoView(this.mHost.getActivity());
        this.mRootPane.addComponent(this.mLivePhotoView);
        this.mPhotoView.setSimpleGestureListener(this.mSimpleGestureListener);
        this.mLivePhotoView.setPhotoView(this.mPhotoView);
        TraceController.traceEnd();
        this.mOnCreateDone = true;
    }

    private void enterPreviewMode() {
        resetFlag();
        if (this.mPhotoView instanceof PhotoView) {
            ((PhotoView) this.mPhotoView).lockPhotoMagnifier();
        }
        this.mNeedUpdateVersionByFavorite = true;
        this.mHost.getGalleryActionBar().setActionBarVisible(false, false);
        this.mHost.getActivity().findViewById(R.id.gallery_root).setVisibility(8);
    }

    protected void initializeData(Bundle data) {
        boolean z = false;
        TraceController.traceBegin("PhotoPage.initializeData");
        this.mFromCamera = data.getBoolean("local-merge-camera-album", false);
        this.mIsPreviewMode = data.getBoolean("preview_mode", false);
        this.mKeepFromCamera = data.getBoolean("keep-from-camera", false);
        this.mIsSecureAlbum = data.getBoolean("is-secure-camera-album", false);
        this.mCalledToSimpleEditor = data.getBoolean("to-simple-editor", false);
        this.mIsViewAsUriImage = data.getBoolean("view-as-uri-image", false);
        this.mIsPickWithEdit = data.getBoolean("editor_photo_has_result", false);
        this.mIsPickWithPreview = data.getBoolean("preview_photo_no_bar", false);
        if (this.mIsSecureAlbum) {
            this.mFlags |= FragmentTransaction.TRANSIT_EXIT_MASK;
        }
        if (this.mCalledToSimpleEditor) {
            Path itemPath = getItemPath(data);
            MediaItem mediaItem = null;
            if (itemPath != null) {
                mediaItem = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(itemPath);
            }
            if (mediaItem == null) {
                ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.fail_to_load_image_Toast, 1);
                TraceController.traceEnd();
                return;
            }
            SinglePhotoDataAdapter adapter = new SinglePhotoDataAdapter(this.mHost.getGalleryContext(), null, this.mPhotoView, mediaItem);
            if (!this.mCalledToSimpleEditor) {
                z = true;
            }
            adapter.changeSupportFullImage(z);
            this.mModel = adapter;
            this.mPhotoView.setModel(this.mModel);
            updateCurrentPhoto(mediaItem);
        } else {
            super.initializeData(data);
        }
        if (this.mMediaSet != null) {
            this.mMediaSet.setStartTakenTime(data.getLong("start-taken-time", 0));
        }
        TraceController.traceEnd();
    }

    protected void onStart() {
        super.onStart();
        TraceController.traceBegin("PhotoPage.onStart");
        this.mMultiScreen.enter();
        TraceController.traceEnd();
    }

    protected void onHandleMessage(Message msg) {
        MediaItem item;
        Bundle data;
        Intent target;
        switch (msg.what) {
            case 15:
                if (this.mCurrentPhoto == msg.obj) {
                    TraceController.beginSection("PhotoPage.MSG_UPDATE_SHARE_URI, lock gl thread");
                    setNfcBeamPushUri(this.mCurrentPhoto.getContentUri());
                    TraceController.traceEnd();
                    break;
                }
                break;
            case 16:
                if (this.mIsPreviewMode) {
                    if (this.mHost.getActivity() instanceof PreviewDelgete) {
                        ((PreviewDelgete) this.mHost.getActivity()).updatePreviewView(UIUtils.getGifBitmapFromScreenNail(this.mModel.getScreenNail()));
                    }
                    this.mHandler.sendEmptyMessageDelayed(16, 33);
                    break;
                }
                break;
            case 20:
                hideBars(false);
                item = this.mModel.getMediaItem(0);
                if (item != null) {
                    data = new Bundle();
                    data.putString("media-item-path", item.getPath().toString());
                    data.putBoolean("is-secure-camera-album", this.mIsSecureAlbum);
                    if (item.getRefocusPhotoType() == 2) {
                        data.putInt("Visual-Effects-Mode", 1);
                    } else {
                        data.putInt("Visual-Effects-Mode", 0);
                    }
                    this.mHost.getStateManager().startStateForResult(RefocusPage.class, 155, data);
                    break;
                }
                return;
            case 32:
                hideBars(false);
                item = this.mModel.getMediaItem(0);
                if (item != null) {
                    data = new Bundle();
                    data.putString("media-item-path", item.getPath().toString());
                    target = new Intent(this.mHost.getActivity(), WideAperturePhoto3DActivity.class);
                    target.putExtras(data);
                    this.mHost.getActivity().startActivityForResult(target, 156);
                    break;
                }
                return;
            case 33:
                hideBars(false);
                item = this.mModel.getMediaItem(0);
                if (item != null) {
                    data = new Bundle();
                    data.putString("media-item-path", item.getPath().toString());
                    data.putBoolean("is-secure-camera-album", this.mIsSecureAlbum);
                    this.mHost.getStateManager().startState(RangeMeasurePage.class, data);
                    break;
                }
                return;
            case AMapException.ERROR_CODE_SERVER /*34*/:
                item = this.mModel.getMediaItem(0);
                if (item != null) {
                    target = new Intent("android.intent.action.VIEW");
                    target.putExtra("is-secure-camera-album", inSecureAlbum());
                    String path = item.getFilePath();
                    if (!(path == null || path.isEmpty())) {
                        target.putExtra("3DModel_File_Path", path);
                    }
                    target.setClassName(this.mHost.getActivity(), "com.huawei.gallery.threedmodel.GalleryThreeDModelActivity");
                    target.setData(item.getContentUri());
                    target.setFlags(268435456);
                    this.mHost.getActivity().startActivityForResult(target, SmsCheckResult.ESCT_ILLEGAL);
                    break;
                }
                return;
            case AMapException.ERROR_CODE_QUOTA /*35*/:
                TraceController.beginSection("PhotoPage.MSG_SHOW_MULTISCREEN_TIPS, lock gl thread");
                this.mTipsManager.showTips();
                TraceController.traceEnd();
                break;
            case 39:
                TraceController.beginSection("PhotoPage.MSG_BARCODE_DETECTION, lock gl thread");
                scanQRcode();
                ReportToBigData.report(SmsCheckResult.ESCT_190);
                TraceController.traceEnd();
                break;
            case 51:
                break;
            case ADGLMapAnimGroup.CAMERA_MAX_DEGREE /*60*/:
                TraceController.beginSection("PhotoPage.MSG_RUN_OBJECT, lock gl thread");
                ((Runnable) msg.obj).run();
                TraceController.traceEnd();
                break;
            case 80:
                if (this.mIsActive) {
                    this.mHost.getStateManager().startStateForResult(PhotoPage.class, SmsCheckResult.ESCT_200, (Bundle) msg.obj);
                    break;
                }
                return;
            case 81:
                if (this.mIsActive) {
                    this.mHost.getStateManager().switchState(this, PhotoPage.class, (Bundle) msg.obj);
                    break;
                }
                return;
            case WMComponent.ORI_90 /*90*/:
                hideBars(false);
                item = this.mModel.getMediaItem(0);
                if (item != null) {
                    data = new Bundle();
                    data.putString("media-item-path", item.getPath().toString());
                    data.putBoolean("is-secure-camera-album", this.mIsSecureAlbum);
                    this.mHost.getStateManager().startStateForResult(PhotoRectifyPage.class, 230, data);
                    break;
                }
                return;
            default:
                super.onHandleMessage(msg);
                break;
        }
    }

    protected boolean noActionBar() {
        return (inEditorMode() || this.mInPluginMode || this.mInFreeShareMode) ? true : this.mPhotoInfoView.isVisible();
    }

    protected boolean onCreateActionBar(Menu menu) {
        boolean z = false;
        TraceController.beginSection("PhotoPage.onCreateActionbar");
        if (noActionBar()) {
            TraceController.endSection();
            return true;
        } else if (this.mIsPickWithPreview) {
            TraceController.endSection();
            hideBars(false);
            return true;
        } else {
            this.mHost.requestFeature(348);
            if (this.mCurrentPhoto == null) {
                getGalleryActionBar().setMenuVisible(false);
            }
            this.mShowBars = true;
            refreshHidingMessage();
            DetailActionMode am = this.mActionBar.enterDetailActionMode(false);
            if ((this.mKeepFromCamera || this.mFromCamera) && !this.mIsSecureAlbum) {
                am.setLeftAction(Action.GOTO_GALLERY);
            } else {
                am.setLeftAction(Action.NONE);
            }
            am.setRightAction(Action.DETAIL);
            am.setMiddleAction(Action.NONE);
            if (!(this.mMediaSet instanceof GalleryRecycleAlbum)) {
                z = true;
            }
            am.setHeadIconVisible(z);
            am.show();
            TraceController.endSection();
            if (this.mIsPreviewMode) {
                enterPreviewMode();
            }
            return super.onCreateActionBar(menu);
        }
    }

    protected void onGLRootLayout(boolean changeSize, int left, int top, int right, int bottom) {
        this.mPhotoView.layout(0, 0, right - left, bottom - top);
        this.mEditorView.layout(0, 0, right - left, bottom - top);
        this.mPhotoInfoView.layout(left, top, right, bottom);
        if (this.mShowDetails) {
            this.mDetailsHelper.layout(left, top, right, bottom);
        }
        this.mLivePhotoView.layout(0, 0, right - left, bottom - top);
    }

    protected boolean updateCurrentPhoto(MediaItem photo) {
        TraceController.traceBegin("PhotoPage.updateCurrentPhoto");
        this.mMultiScreen.play(photo, false);
        boolean changed = super.updateCurrentPhoto(photo);
        this.mCommentLoader.parseItemComment(photo);
        if (changed) {
            this.mMovingPicutreHelper.updatePhoto(photo);
            if (this.mPhotoView.getFilmMode()) {
                requestDeferredUpdate();
            } else if (!this.mInPluginMode) {
                updateUIForCurrentPhoto();
            }
        } else {
            updatePhotoInfoView();
            updateExtraButton();
            if (this.mIsActive) {
                updateMenuOperations();
            }
        }
        TraceController.traceEnd();
        return changed;
    }

    protected void updateTitle() {
        TraceController.traceBegin("PhotoPage.updateTitle");
        super.updateTitle();
        if (this.mShowDetails) {
            this.mDetailsHelper.reloadDetails();
        }
        TraceController.traceEnd();
    }

    public void onCurrentImageUpdated() {
        super.onCurrentImageUpdated();
        if (this.mShowDetails) {
            this.mDetailsHelper.reloadDetails();
        }
        if (this.mIsPreviewMode && (this.mHost.getActivity() instanceof PreviewDelgete)) {
            ((PreviewDelgete) this.mHost.getActivity()).updatePreviewView(UIUtils.getBitmapFromScreenNail(this.mModel.getScreenNail()));
            if (this.mCurrentPhoto != null && "image/gif".equals(this.mCurrentPhoto.getMimeType())) {
                this.mHandler.sendEmptyMessage(16);
            }
        }
    }

    protected void updateUIForCurrentPhoto() {
        if ((this.mIsActive || this.mIsSetingResult) && this.mCurrentPhoto != null) {
            TraceController.traceBegin("PhotoPage.updateUIForCurrentPhoto");
            updateMenuOperations();
            updatePhotoInfoView();
            updateExtraButton();
            if (this.mShowDetails) {
                this.mDetailsHelper.reloadDetails();
            }
            if ((this.mCurrentPhoto.getSupportedOperations() & 4) != 0) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(15, this.mCurrentPhoto));
            }
            TraceController.traceEnd();
        }
    }

    private void updateMenuOperations() {
        boolean z = false;
        MediaItem currentPhoto = this.mCurrentPhoto;
        if (currentPhoto != null && this.mActionBar != null) {
            ActionBarStateBase mode = this.mActionBar.getCurrentMode();
            if (mode != null) {
                boolean z2;
                TraceController.traceBegin("PhotoPage.updateMenuOperations");
                onInflateMenu(mode);
                mode.setActionEnable(canDoSlideShow(), Action.ACTION_ID_SLIDESHOW);
                if ((currentPhoto instanceof UriImage) || inSecureAlbum()) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                mode.setActionEnable(z2, Action.ACTION_ID_SETTINGS);
                if (currentPhoto.isMyFavorite()) {
                    mode.changeAction(Action.ACTION_ID_NOT_MYFAVORITE, Action.ACTION_ID_MYFAVORITE);
                } else {
                    mode.changeAction(Action.ACTION_ID_MYFAVORITE, Action.ACTION_ID_NOT_MYFAVORITE);
                }
                int supportedOperations = currentPhoto.getSupportedOperations();
                if (!this.mIsMapAvailable) {
                    supportedOperations &= -17;
                }
                if (inSecureAlbum()) {
                    supportedOperations &= -131089;
                }
                if (this.mIsViewAsUriImage) {
                    supportedOperations &= -5;
                }
                if (isSupportRangeMeasure(currentPhoto)) {
                    supportedOperations |= 4194304;
                }
                MenuEnableCtrller.updateMenuOperation(mode, supportedOperations);
                boolean useMoreEdit = false;
                if (this.mUseMoreEdit && (supportedOperations & 512) != 0) {
                    useMoreEdit = !(currentPhoto instanceof IVideo);
                }
                mode.setActionEnable(useMoreEdit, Action.ACTION_ID_MORE_EDIT);
                if ((mode instanceof DetailActionMode) && (this.mMediaSet instanceof UriAlbum)) {
                    DetailActionMode am = (DetailActionMode) mode;
                    if ((524288 & supportedOperations) != 0) {
                        am.setRightAction(Action.SAVE);
                    }
                }
                if (isFreeShareEnabled()) {
                    this.mIntentChooser.addShareItem(this.mFreeShareItem);
                }
                if (PhotoShareUtils.isSupportPhotoShare()) {
                    if (!(inSecureAlbum() || (supportedOperations & 4) == 0 || TextUtils.isEmpty(currentPhoto.getFilePath()))) {
                        z = true;
                    }
                    mode.setActionEnable(z, Action.ACTION_ID_PHOTOSHARE_BACKUP);
                }
                mode.setActionEnable(isContainBarcodeScanResult(), Action.ACTION_ID_SEE_BARCODE_INFO);
                updateMenuForUserComment();
                TraceController.traceEnd();
            }
        }
    }

    private void updateMenuForUserComment() {
        ActionBarStateBase mode = this.mActionBar.getCurrentMode();
        if (mode != null && (mode instanceof DetailActionMode)) {
            GalleryLog.d("PhotoPage", "disableComment ");
            this.mPhotoInfoView.setComment("");
            CommentInfo commentInfo = this.mCommentInfo;
            MediaItem currentPhoto = this.mCurrentPhoto;
            PhotoFragmentPluginManager pluginManager = this.mPluginManager;
            if (commentInfo == null || currentPhoto == null || pluginManager == null) {
                disableComment(mode);
                return;
            }
            PhotoExtraButton extraButton = ((DetailActionMode) mode).getExtraButton();
            PhotoExtraButton extraButton1 = ((DetailActionMode) mode).getExtraButton1();
            boolean extraButtonEnable = pluginManager.updatePhotoExtraButton(extraButton, currentPhoto);
            boolean extraButton1Enable = pluginManager.updatePhotoExtraButton(extraButton1, currentPhoto);
            if (extraButtonEnable || extraButton1Enable) {
                disableComment(mode);
                return;
            }
            String filepath = currentPhoto.getFilePath();
            if (filepath == null) {
                disableComment(mode);
                return;
            }
            if (commentInfo.supportByExif() && filepath.equals(commentInfo.getFilePath())) {
                boolean z;
                GalleryLog.d("PhotoPage", "updateMenuForUserComment : " + commentInfo);
                String content = commentInfo.getContent();
                boolean addComment = TextUtils.isEmpty(content);
                this.mPhotoInfoView.setComment(content);
                mode.setActionEnable(addComment, Action.ACTION_ID_ADD_COMMENT);
                if (addComment) {
                    z = false;
                } else {
                    z = true;
                }
                mode.setActionEnable(z, Action.ACTION_ID_EDIT_COMMENT);
            } else {
                disableComment(mode);
            }
        }
    }

    private void disableComment(ActionBarStateBase mode) {
        mode.setActionEnable(false, Action.ACTION_ID_ADD_COMMENT);
        mode.setActionEnable(false, Action.ACTION_ID_EDIT_COMMENT);
    }

    protected void onInflateMenu(ActionBarStateBase mode) {
        mode.setMenu(Math.min(5, this.mDefaultMenu.length), this.mDefaultMenu);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean canDoSlideShow() {
        if (this.mMediaSet == null || !isValidImage() || inSecureAlbum() || MtpSource.isMtpPath(this.mOriginalSetPathString)) {
            return false;
        }
        return true;
    }

    protected void onResume() {
        TraceController.beginSection("PhotoPage.onResume");
        super.onResume();
        this.mHost.getActivity().registerReceiver(this.mRemoteBroadcastReceiver, new IntentFilter("com.huawei.remotecontrol.disconnected"), "com.huawei.camera.permission.REMOTECONTROLLER", null);
        this.mHandler.post(this.mChkPkgAndUpdateMenu);
        if (this.mFromCamera) {
            if (this.mScreenController == null) {
                this.mScreenController = new ScreenController(this.mHost.getActivity());
            }
            this.mScreenController.onResume();
        }
        if (this.mShouldInitMultiScreenOnResume && !this.mMultiScreen.hasServiceInited()) {
            this.mShouldInitMultiScreenOnResume = false;
            this.mMultiScreen.initialize(this.mHost.getActivity().getApplicationContext(), this.mHandler);
        }
        this.mMultiScreen.requestRefreshInfo();
        this.mEditorView.resume();
        this.mPluginManager.onResume();
        updateSettings();
        updateExtraButton();
        ((GLRootView) this.mHost.getGLRoot()).setOnSystemUiVisibilityChangeListener(this.mSystemUiVisibilityChangeListener);
        this.mLivePhotoView.onResume();
        LayoutHelper.getNavigationBarHandler().update();
        this.mIntentChooser.resume();
        if (isFreeShareShowing()) {
            hideBars(false);
        }
        if (this.mActionBar != null) {
            this.mActionBar.addOnMenuVisibilityListener(this.mMenuVisibilityListener);
        }
        TraceController.endSection();
    }

    protected boolean onItemSelected(Action action) {
        this.mLivePhotoView.stop();
        if (super.onItemSelected(action)) {
            return true;
        }
        if (this.mPluginManager.onInterceptActionItemClick(action)) {
            return false;
        }
        if (this.mModel == null) {
            return false;
        }
        refreshHidingMessage();
        MediaItem current = this.mModel.getMediaItem(0);
        if (current == null) {
            return false;
        }
        Path path = current.getPath();
        ReportToBigData.reportActionForFragment("FromPhotoView", action, this.mSelectionManager);
        Intent intent;
        ArrayList<Path> items;
        switch (-getcom-huawei-gallery-actionbar-ActionSwitchesValues()[action.ordinal()]) {
            case 1:
            case 6:
                CommentInfo commentInfo = this.mCommentInfo;
                if (commentInfo != null) {
                    if (this.mShowDetails) {
                        hideDetails();
                    }
                    final CommentInfo commentInfo2 = commentInfo;
                    this.mEditor = new GalleryCustEditor(this.mHost.getActivity(), commentInfo.getContent(), new EditorController() {
                        public void onTextChanged(String text) {
                            commentInfo2.setContent(text);
                            PhotoPage.this.updateMenuForUserComment();
                            CommentHelper.writeComment(commentInfo2);
                            PhotoPage.this.mEditor = null;
                            ReportToBigData.report(105);
                        }

                        public int getSizeLimit() {
                            return commentInfo2.getContentSizeLimit();
                        }
                    }, inSecureAlbum());
                    this.mEditor.updateDialogWindowSize();
                }
                return true;
            case 2:
            case 4:
                if (this.mDetailView == null) {
                    this.mDetailView = (DetailLayout) ((LayoutInflater) this.mHost.getActivity().getSystemService("layout_inflater")).inflate(R.layout.details_layout, (RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root), false);
                    this.mDetailView.setDelegate(this);
                }
                if (this.mShowDetails) {
                    hideDetails();
                } else {
                    showDetails();
                    hideBars(true);
                }
                return true;
            case 3:
                if (!this.mPhotoView.isExtraActionDoing()) {
                    String str;
                    this.mSelectionManager.deSelectAll();
                    this.mSelectionManager.toggle(path);
                    Bundle data = new Bundle();
                    if (RecycleUtils.supportRecycle()) {
                        data.putInt("recycle_flag", 2);
                    } else {
                        data.putInt("recycle_flag", 0);
                    }
                    String message = DeleteMsgUtil.getDeleteMsg(this.mHost.getGalleryContext().getResources(), this.mHost.getGalleryContext().getDataManager(), this.mMediaSet, current.getVirtualFlags(), 1, null, current);
                    String title = DeleteMsgUtil.getDeleteTitle(this.mHost.getGalleryContext().getResources(), this.mMediaSet, 1, true, isHicloudAlbum(), isSyncAlbum());
                    if (current.isBurstCover()) {
                        title = message;
                        str = null;
                    } else {
                        str = message;
                    }
                    GalleryLog.d("PhotoPage", "want delete file:" + current.getFilePath());
                    this.mMenuExecutor.onMenuClicked(action, str, title, this.mConfirmDialogListener, data);
                    return true;
                }
                break;
            case 5:
                if (4 == current.getMediaType()) {
                    VideoEditorController.editVideo(this.mHost.getActivity(), current.getFilePath(), AbsAlbumPage.LAUNCH_QUIK_ACTIVITY);
                } else if (FyuseManager.getInstance().startEditFyuseFile(this.mHost.getActivity(), this.mCurrentPhoto)) {
                    return true;
                } else {
                    enterSimpleEditor();
                }
                return true;
            case 7:
                intent = new Intent(this.mHost.getActivity(), GalleryMain.class);
                intent.putExtra("key-no-page-history", true);
                this.mHost.getActivity().startActivity(intent);
                this.mHost.getActivity().finish();
                this.mHost.getActivity().overridePendingTransition(0, 0);
                return true;
            case 8:
            case 24:
            case AMapException.ERROR_CODE_UNKNOW_HOST /*27*/:
                excuteAction(action, path);
                return true;
            case 9:
            case 10:
                try {
                    this.mHost.getActivity().startActivity(this.mMultiScreen.getDeviceSelectorInfo());
                } catch (Exception e) {
                    GalleryLog.w(MultiScreen.class.getSimpleName() + "_" + "PhotoPage", "startActivity." + e.getMessage());
                }
                return true;
            case 11:
            case 13:
                if (this.mMediaSet != null && this.mNeedUpdateVersionByFavorite) {
                    this.mMediaSet.updateDataVersion();
                }
                handleMyFavoriteAction(action, path);
                return true;
            case 12:
            case 14:
            case 18:
            case 22:
            case AMapException.ERROR_CODE_PROTOCOL /*29*/:
                if (inEditorMode()) {
                    this.mEditorView.onActionItemClick(action);
                    break;
                }
                if (action == Action.SAVE) {
                    outputItem(path);
                }
                return true;
            case 15:
                items = new ArrayList(1);
                items.add(this.mCurrentPhoto.getPath());
                PhotoShareUtils.cacheShareItemList(PhotoShareUtils.getFilePathsFromPath(this.mHost.getGalleryContext(), items));
                intent = new Intent(this.mHost.getActivity(), ShareToCloudAlbumActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("inner_share", true);
                intent.putExtras(bundle);
                this.mHost.getActivity().startActivity(intent);
                return true;
            case 16:
                GalleryUtils.printSelectedImage(this.mHost.getActivity(), current);
                return true;
            case 17:
                onRangeMeasureItemClick();
                break;
            case 19:
                this.mSelectionManager.deSelectAll();
                this.mSelectionManager.toggle(path);
                createDialogIfNeeded(GalleryUtils.getMediaItemName(current), R.string.rename);
                return true;
            case 20:
            case 21:
                if (current.isVoiceImage()) {
                    this.mPluginManager.onPause();
                }
                excuteAction(action, path);
                return true;
            case 23:
                onBarcodeInfoTouched(0);
                return true;
            case 25:
                this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        PhotoPage.this.mHost.getActivity().startActivity(new Intent(PhotoPage.this.mHost.getActivity(), GallerySettings.class));
                    }
                }, 150);
                return true;
            case AMapException.ERROR_CODE_URL /*26*/:
                hideBars(false);
                if (inEditorMode()) {
                    this.mEditorView.onActionItemClick(action);
                    return true;
                } else if (current.is3DPanorama() && FyuseManager.getInstance().startShareFyuseFile(this.mHost.getActivity(), this.mCurrentPhoto, "LARGEVIEW")) {
                    return true;
                } else {
                    items = new ArrayList(1);
                    items.add(this.mCurrentPhoto.getPath());
                    Path mediaSetPath = null;
                    if (this.mMediaSet != null) {
                        mediaSetPath = this.mMediaSet.getPath();
                    }
                    if (PhotoShareUtils.isSupportShareToCloud()) {
                        this.mIntentChooser.addShareItem(this.mPhotoShareItem);
                    } else {
                        this.mIntentChooser.removeShareItem(this.mPhotoShareItem);
                    }
                    this.mIntentChooser.share(this.mHost.getGalleryContext(), this.mActionBar.getCurrentMode(), this.mMenuExecutor, mediaSetPath, items);
                    return true;
                }
            case AMapException.ERROR_CODE_UNKNOW_SERVICE /*28*/:
                if (mHwCustPhotoPage == null || !mHwCustPhotoPage.handleCustSlideshowItemClicked(this.mHost, path.toString())) {
                    hideBars(false);
                    this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            PhotoPage.this.startSlideShow();
                        }
                    }, 150);
                }
                return true;
        }
        return false;
    }

    protected void excuteAction(Action action, Path path) {
        this.mSelectionManager.deSelectAll();
        this.mSelectionManager.toggle(path);
        this.mMenuExecutor.onMenuClicked(action, null, null, null);
    }

    protected void onConfigurationChanged(Configuration config) {
        boolean z = true;
        super.onConfigurationChanged(config);
        ActionBarStateBase actionBarStateBase = this.mHost.getGalleryActionBar().getCurrentMode();
        if (actionBarStateBase instanceof DetailActionMode) {
            DetailActionMode detailActionMode = (DetailActionMode) actionBarStateBase;
            if (config.orientation != 1) {
                z = false;
            }
            detailActionMode.onConfigurationChanged(z);
        }
        this.mEditorView.onConfigurationChanged(config);
        if (this.mEditor != null) {
            this.mEditor.updateDialogWindowSize();
        }
    }

    protected boolean onBackPressed() {
        this.mKeepRemoteControl = true;
        if (this.mTipsManager.shown) {
            this.mTipsManager.hideTips(true);
            return true;
        } else if (isFreeShareShowing()) {
            this.mFreeShare.doHide();
            this.mInFreeShareMode = false;
            return true;
        } else if (inEditorMode()) {
            this.mEditorView.onBackPressed();
            return true;
        } else if (this.mPhotoView.getFilmMode()) {
            this.mPhotoView.setFilmMode(false);
            return true;
        } else if (this.mPluginManager.onBackPressed()) {
            if (this.mShowDetails) {
                hideDetails();
            }
            return true;
        } else if (this.mShowDetails) {
            hideDetails();
            return true;
        } else {
            this.mPhotoView.onDeleteDelay();
            this.mMultiScreen.exit();
            setStateResult(-1, getDefaultResult());
            if (this.mHost.getStateManager().getStateCount() <= 1) {
                return super.onBackPressed();
            }
            this.mHandler.sendEmptyMessage(50);
            return true;
        }
    }

    protected void onPause() {
        this.mLivePhotoView.stop();
        super.onPause();
        this.mHandler.removeCallbacks(this.mChkPkgAndUpdateMenu);
        this.mHost.getActivity().unregisterReceiver(this.mRemoteBroadcastReceiver);
        if (this.mFromCamera && !this.mKeepRemoteControl) {
            this.mHost.getActivity().sendBroadcast(new Intent("com.huawei.remotecontrol.stop"), "com.huawei.camera.permission.REMOTECONTROLLER");
        }
        this.mKeepRemoteControl = false;
        if (this.mFromCamera && this.mScreenController != null) {
            this.mScreenController.onPause();
        }
        if (this.mEditor != null) {
            this.mEditor.pause();
        }
        DetailsHelper.pause();
        if (this.mShowDetails) {
            hideDetails();
        }
        this.mIntentChooser.hideIfShowing();
        this.mIntentChooser.pause();
        this.mEditorView.pause();
        this.mPluginManager.onPause();
        this.mLivePhotoView.onPause();
        if (this.mTipsManager.shown) {
            this.mTipsManager.hideTips(false);
        }
        ((GLRootView) this.mHost.getGLRoot()).setOnSystemUiVisibilityChangeListener(null);
        this.mActionBar.removeOnMenuVisibilityListener(this.mMenuVisibilityListener);
        if (this.mBip != null) {
            this.mBip.setBarcodeResultListener(null);
        }
    }

    protected void onStop() {
        super.onStop();
        this.mMultiScreen.exit();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mLivePhotoView.destroy();
        this.mPluginManager.onDestroy();
        this.mEditorView.destroy();
        this.mPluginManager.onDestroy();
        this.mPhotoInfoView.recycle();
        this.mTipsManager.cleanUp();
        this.mMultiScreen.removeListener(this.mMultiScreenListener);
        cleanNfcBeamPush();
        destroyFreeShare();
        this.mAsyncProcessingHandler.removeCallbacksAndMessages(null);
        if (EditorLoadLib.FILTERJNI_LOADED) {
            this.mAsyncProcessingHandler.sendEmptyMessage(3);
        }
        if (EditorLoadLib.FILTERJNI_MORPHO_LOADED) {
            this.mAsyncProcessingHandler.sendEmptyMessage(4);
        }
        this.mHandlerThread.quitSafely();
        if (this.mCreateDialog != null) {
            GalleryUtils.setDialogDismissable(this.mCreateDialog, true);
            GalleryUtils.dismissDialogSafely(this.mCreateDialog, null);
            this.mCreateDialog = null;
        }
    }

    public void onSingleTapUp(int x, int y) {
        MediaItem item = this.mModel.getMediaItem(0);
        if (item != null) {
            boolean playVideo = (item.getSupportedOperations() & 128) != 0;
            boolean isDrmVideo = item.isDrm() && 4 == item.getMediaType();
            if (playVideo || isDrmVideo) {
                int w = this.mPhotoView.getWidth();
                int h = this.mPhotoView.getHeight();
                playVideo = Math.abs(x - (w / 2)) * 12 <= w ? Math.abs(y - (h / 2)) * 12 <= h : false;
            }
            if (!playVideo) {
                toggleBars();
                updatePhotoInfoView();
            } else if (!isDrmVideo || item.getRight()) {
                onPlayVideo(item);
            }
        }
    }

    protected void toggleBars() {
        if (!this.mIsPickWithPreview) {
            super.toggleBars();
        }
    }

    protected void onPlayVideo(MediaItem item) {
        this.mKeepRemoteControl = true;
        playVideo(this.mHost.getActivity(), item.getPlayUri(), item.getName());
    }

    private boolean inSecureAlbum() {
        return (this.mFromCamera || this.mKeepFromCamera) ? this.mIsSecureAlbum : false;
    }

    protected void showBars(boolean barWithAnim) {
        if (!this.mShowBars && !inBurstMode()) {
            if (this.mPhotoView == null || !this.mPhotoView.isTileViewFromCache()) {
                this.mShowBars = true;
                updateExtraButton();
                if (getGalleryActionBar().getCurrentMode() instanceof DetailActionMode) {
                    ((DetailActionMode) getGalleryActionBar().getCurrentMode()).setActionBarVisible(true, true, true);
                }
                if (this.mShowDetails) {
                    getGalleryActionBar().setHeadBarVisible(true, false);
                    getGalleryActionBar().setActionPanelVisible(true, barWithAnim);
                    getGalleryActionBar().setMenuVisible(true);
                } else {
                    getGalleryActionBar().setActionBarVisible(true, barWithAnim);
                }
                refreshHidingMessage();
                updatePhotoInfoView();
            }
        }
    }

    protected void hideBars(boolean barWithAnim) {
        TraceController.traceBegin("PhotoPage.hideBars");
        ActionBarStateBase mode;
        if (this.mShowBars) {
            this.mShowBars = false;
            if (!this.mInPluginMode) {
                mode = getGalleryActionBar().getCurrentMode();
                if ((mode instanceof DetailActionMode) && this.mShowDetails) {
                    ((DetailActionMode) mode).setActionBarVisible(false, false, true);
                    getGalleryActionBar().setActionPanelVisible(false, barWithAnim);
                    getGalleryActionBar().setMenuVisible(false);
                } else {
                    getGalleryActionBar().setActionBarVisible(false, barWithAnim);
                }
            }
            this.mHandler.removeMessages(1);
            updatePhotoInfoView();
            TraceController.traceEnd();
            return;
        }
        mode = getGalleryActionBar().getCurrentMode();
        if (!this.mInPluginMode && (mode instanceof DetailActionMode)) {
            ((DetailActionMode) mode).setActionBarVisible(false, false, this.mShowDetails);
            getGalleryActionBar().setHeadBarVisible(this.mShowDetails, false);
        }
        TraceController.traceEnd();
    }

    protected void refreshHidingMessage() {
        this.mHandler.removeMessages(1);
        if (!this.mPhotoView.getFilmMode() && !this.mTipsManager.shown && !this.mTipsManager.showing) {
            this.mHandler.sendEmptyMessageDelayed(1, (long) HIDE_BARS_TIMEOUT);
        }
    }

    protected void onStateResult(int requestCode, int resultCode, Intent data) {
        this.mIsSetingResult = true;
        MediaItem mediaItem;
        switch (requestCode) {
            case 1:
                super.onStateResult(requestCode, resultCode, data);
                break;
            case 10:
                if (resultCode == -1) {
                    this.mEditorView.leaveEditor();
                    onBackPressed();
                    break;
                }
                break;
            case 155:
                onLeavePluginMode(0, null);
                setSwipingEnabled(true);
                mediaItem = null;
                if (this.mCurrentPhoto != null) {
                    mediaItem = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(this.mCurrentPhoto.getPath());
                }
                Bitmap previewBitmap = null;
                if (mediaItem != null) {
                    previewBitmap = mediaItem.getScreenNailBitmap(1);
                }
                if (!(moveToImage(data.getData(), previewBitmap) || mediaItem == null)) {
                    this.mPhotoView.setMediaItemScreenNail(mediaItem);
                }
                this.mPhotoView.notifyImageChange(0);
                updateMenuOperations();
                break;
            case 156:
                mediaItem = null;
                onLeavePluginMode(0, null);
                setSwipingEnabled(true);
                if (this.mCurrentPhoto != null) {
                    mediaItem = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(this.mCurrentPhoto.getPath());
                }
                if (mediaItem != null) {
                    this.mPhotoView.setMediaItemScreenNail(mediaItem);
                }
                this.mPhotoView.notifyImageChange(0);
                updateMenuOperations();
                break;
            case SmsCheckResult.ESCT_ILLEGAL /*157*/:
                onLeavePluginMode(0, null);
                setSwipingEnabled(true);
                mediaItem = null;
                if (this.mCurrentPhoto != null) {
                    mediaItem = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(this.mCurrentPhoto.getPath());
                }
                if (mediaItem != null) {
                    this.mPhotoView.setMediaItemScreenNail(mediaItem);
                }
                this.mPhotoView.notifyImageChange(0);
                updateMenuOperations();
                break;
            case SmsCheckResult.ESCT_200 /*200*/:
                updateMenuOperations();
                break;
            case 230:
                onLeavePluginMode(0, null);
                setSwipingEnabled(true);
                mediaItem = null;
                if (this.mCurrentPhoto != null) {
                    mediaItem = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(this.mCurrentPhoto.getPath());
                }
                if (mediaItem != null) {
                    this.mPhotoView.setMediaItemScreenNail(mediaItem);
                }
                this.mPhotoView.notifyImageChange(0);
                updateMenuOperations();
                break;
            case AbsAlbumPage.LAUNCH_QUIK_ACTIVITY /*400*/:
                if (resultCode == -1) {
                    moveToNewVideo(data.getData());
                    break;
                }
                break;
            case 500:
                this.mIntentChooser.onReceiveShareResult(requestCode, resultCode, data);
                break;
            default:
                if (!(mHwCustPhotoPage == null || this.mMediaSet == null)) {
                    mHwCustPhotoPage.onCustStateResult(requestCode, resultCode, data, this.mHost, this.mMediaSet.getPath().toString(), this.mModel.getCurrentIndex());
                }
                this.mIntentChooser.onResult();
                break;
        }
        this.mIsSetingResult = false;
    }

    public void onFilmModeChanged(boolean enabled) {
        updatePhotoInfoView();
        if (isFreeShareEnabled()) {
            this.mIntentChooser.addShareItem(this.mFreeShareItem);
        } else {
            this.mIntentChooser.removeShareItem(this.mFreeShareItem);
        }
        this.mLivePhotoView.setFilmMode(enabled);
        this.mHandler.removeMessages(1);
        hideBars(false);
    }

    public void onPictureCenter(boolean isCamera) {
        GalleryLog.d("PhotoPage", "onPictureCenter");
        this.mMovingPicutreHelper.onPictureCenter();
        synchronized (this.EDIT_OBJ) {
            this.EDIT_OBJ.notifyAll();
        }
    }

    public void onPictureFullView() {
        if (this.mPictureFullViewCallback != null) {
            this.mPictureFullViewCallback.run();
        }
        setWantPictureFullViewCallbacks(false, 0);
    }

    public void onEnterPhotoMagnifierMode() {
        if (this.mShowBars) {
            hideBars(true);
        }
        updatePhotoInfoView();
    }

    public void onLeavePhotoMagnifierMode() {
        updatePhotoInfoView();
    }

    public boolean isDetailsShow() {
        return this.mShowDetails;
    }

    protected void onPhotoSharePhotoChanged() {
    }

    protected void onPhotoChanged(int index, Path item) {
        boolean isPhotoChanged = true;
        if (item != null && item.equalsIgnoreCase(this.mCurrentPhoto)) {
            isPhotoChanged = false;
        }
        if (isPhotoChanged) {
            this.mPluginManager.onPhotoChanged();
        } else if (this.mCurrentPhoto.getDataVersion() == this.mCurrentPhotoVersion) {
            if (index != this.mCurrentIndex) {
                super.onPhotoChanged(index, item);
            }
            return;
        }
        super.onPhotoChanged(index, item);
        if (isPhotoChanged) {
            onPhotoSharePhotoChanged();
            onDetailPhotoChanged();
        }
    }

    private void onDetailPhotoChanged() {
        if (this.mDetailView != null) {
            this.mDetailView.onPhotoChanged();
        }
    }

    protected void onLoadingFinished(boolean loadingFailed) {
        super.onLoadingFinished(loadingFailed);
        synchronized (this.EDIT_OBJ) {
            this.EDIT_OBJ.notifyAll();
        }
    }

    private void showDetails() {
        this.mShowDetails = true;
        if (this.mDetailsHelper == null) {
            this.mDetailsHelper = new DetailsHelper(this.mHost.getGalleryContext(), this.mRootPane, new MyDetailsSource(), this.mDetailView);
            this.mDetailsHelper.setCloseListener(new CloseListener() {
                public void onClose() {
                    PhotoPage.this.hideDetails();
                }
            });
        }
        if (this.mDetailView.getParent() == null) {
            ((RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root)).addView(this.mDetailView);
        }
        this.mDetailsHelper.show();
        ActionBarStateBase am = this.mActionBar.getCurrentMode();
        if (am instanceof DetailActionMode) {
            ((DetailActionMode) am).setRightAction(Action.CANCEL_DETAIL);
        }
    }

    private void hideDetails() {
        this.mShowDetails = false;
        this.mDetailsHelper.hide();
        ((RelativeLayout) this.mHost.getActivity().findViewById(R.id.gallery_root)).removeView(this.mDetailView);
        ActionBarStateBase am = this.mActionBar.getCurrentMode();
        if (am instanceof DetailActionMode) {
            ((DetailActionMode) am).setRightAction(Action.DETAIL);
        }
        refreshHidingMessage();
    }

    public Bitmap getCurrentBitmap() {
        if (this.mModel == null) {
            return null;
        }
        try {
            this.mHost.getGLRoot().lockRenderThread();
            Bitmap bitmapFromScreenNail = UIUtils.getBitmapFromScreenNail(this.mModel.getScreenNail());
            return bitmapFromScreenNail;
        } finally {
            this.mHost.getGLRoot().unlockRenderThread();
        }
    }

    public int getCustomDrawableId() {
        if ((getGalleryActionBar().getCurrentMode() instanceof DetailActionMode) && (this.mCurrentPhoto instanceof LocalMediaItem) && ((LocalMediaItem) this.mCurrentPhoto).isHdr()) {
            return R.drawable.ic_btn_hdr;
        }
        return 0;
    }

    public String getTitle() {
        if (this.mCurrentPhoto == null) {
            return "";
        }
        String name = this.mTitle != null ? this.mTitle : this.mCurrentPhoto.getName() != null ? this.mCurrentPhoto.getName() : "";
        return name;
    }

    public boolean photoShareDownLoadOrigin() {
        return false;
    }

    @TargetApi(16)
    private void setupNfcBeamPush() {
        if (ApiHelper.HAS_SET_BEAM_PUSH_URIS) {
            TraceController.traceBegin("PhotoPage.setupNfcBeamPush");
            NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this.mHost.getActivity());
            if (adapter != null) {
                adapter.setBeamPushUris(null, this.mHost.getActivity());
                adapter.setBeamPushUrisCallback(new CreateBeamUrisCallback() {
                    public Uri[] createBeamUris(NfcEvent event) {
                        return PhotoPage.this.mNfcPushUris;
                    }
                }, this.mHost.getActivity());
            }
            TraceController.traceEnd();
        }
    }

    private void setNfcBeamPushUri(Uri uri) {
        this.mNfcPushUris[0] = uri;
    }

    @TargetApi(16)
    private void cleanNfcBeamPush() {
        if (ApiHelper.HAS_SET_BEAM_PUSH_URIS && !this.mHost.getActivity().isDestroyed()) {
            NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this.mHost.getActivity());
            if (adapter != null) {
                adapter.setBeamPushUris(null, this.mHost.getActivity());
                adapter.setBeamPushUrisCallback(null, this.mHost.getActivity());
            }
        }
    }

    protected void playVideo(Activity activity, Uri uri, String title) {
        if (uri == null) {
            GalleryLog.e("PhotoPage", "can't find uri to play[maybe cloud video].");
            return;
        }
        try {
            ReportToBigData.report(40, String.format("{PhotoButton:%s}", new Object[]{"Video"}));
            GalleryUtils.playMovieUseHwVPlayer(activity, uri, inSecureAlbum());
        } catch (RuntimeException e) {
            GalleryUtils.playVideoFromCandidate(activity, uri, title, inSecureAlbum());
        }
    }

    public void onNavigationBarChanged(boolean show, int height) {
        this.mEditorView.onNavigationBarChange(height);
        this.mPhotoInfoView.onNavigationBarChange(height);
        if (this.mFreeShare != null) {
            this.mFreeShare.onNavigationBarChanged(show, height);
        }
    }

    public void onClick(View v) {
        if (isExtraButtonEnable() && this.mPluginManager.onEventsHappens(this.mCurrentPhoto, v)) {
            this.mHandler.removeMessages(1);
            this.mInPluginMode = true;
            GLRoot glRoot = this.mHost.getGLRoot();
            if (!(this.mCurrentPhoto == null || !this.mCurrentPhoto.isBurstCover() || this.mPhotoView == null || glRoot == null)) {
                glRoot.lockRenderThread();
                try {
                    this.mPhotoView.freeTextures();
                } finally {
                    glRoot.unlockRenderThread();
                }
            }
        }
    }

    public GalleryActionBar getGalleryActionBar() {
        return this.mActionBar;
    }

    public void onLeavePluginMode(int pluginID, Intent intent) {
        this.mInPluginMode = false;
        refreshHidingMessage();
        updateUIForCurrentPhoto();
        if (this.mShowBars) {
            hideBars(true);
        } else {
            this.mActionBar.setActionBarVisible(false);
        }
        this.mMultiScreen.requestRefreshInfo();
        if (pluginID == 1) {
            GLRoot glRoot = this.mHost.getGLRoot();
            if (!(this.mPhotoView == null || glRoot == null)) {
                glRoot.lockRenderThread();
                try {
                    this.mPhotoView.prepareTextures();
                } finally {
                    glRoot.unlockRenderThread();
                }
            }
            String path = null;
            if (intent != null) {
                path = intent.getStringExtra("focus-target");
            }
            if (path != null) {
                setCurrentPhotoByPath(Path.fromString(path), null, true);
            }
        }
    }

    private void updateExtraButton() {
        int i = 0;
        ActionBarStateBase actionBarStateBase = this.mHost.getGalleryActionBar().getCurrentMode();
        if ((actionBarStateBase instanceof DetailActionMode) && isExtraButtonEnable()) {
            int i2;
            DetailActionMode detailActionMode = (DetailActionMode) actionBarStateBase;
            PhotoExtraButton extraButton = detailActionMode.getExtraButton();
            PhotoExtraButton extraButton1 = detailActionMode.getExtraButton1();
            extraButton.setOnClickListener(this);
            extraButton1.setOnClickListener(this);
            if (this.mPluginManager.updatePhotoExtraButton(extraButton, this.mCurrentPhoto) && detailActionMode.isHeadIconVisible()) {
                i2 = 0;
            } else {
                i2 = 8;
            }
            extraButton.setVisibility(i2);
            if (!(this.mPluginManager.updatePhotoExtraButton(extraButton1, this.mCurrentPhoto) && detailActionMode.isHeadIconVisible())) {
                i = 8;
            }
            extraButton1.setVisibility(i);
        }
    }

    private boolean isExtraButtonEnable() {
        return (this.mCurrentPhoto == null || this.mInPluginMode || this.mPhotoView.getFilmMode()) ? false : true;
    }

    private void scanQRcode() {
        if (this.mModel != null) {
            this.mBip = BarcodeInfoProcess.newInstance();
            this.mBip.setBarcodeResultListener(this.mReceivedBarcodeResultListener);
            this.mBip.scan(this.mModel.getMediaItem(0));
        }
    }

    public void onBarcodeInfoTouched(int barcodeId) {
        BarcodeScanResultItem scanResult = this.mCurrentPhoto.getBarcodeResult();
        if (scanResult.getBarcodeScanResult().length != 0) {
            try {
                Intent intent = scanResult.getBarcodeScanResult()[0];
                intent.addFlags(268435456);
                intent.addFlags(67108864);
                this.mHost.getActivity().startActivity(intent);
                ReportToBigData.report(62);
            } catch (Exception e) {
                GalleryLog.e("PhotoPage", "start QrcodeDetailActivity exception:" + e.getMessage());
            }
        }
    }

    public void onFlingUp() {
        triggerFreeShare(this.mFlingUpAllowed ? 2 : 4);
    }

    private void triggerFreeShare(int launchmode) {
        int supportOperation = 0;
        if (this.mModel != null) {
            if (mHwCustPhotoPage == null || !mHwCustPhotoPage.isBluetoothRestricted(this.mHost.getActivity())) {
                if (this.mFreeShareAdapter == null) {
                    this.mFreeShareAdapter = ((AbstractGalleryActivity) this.mHost.getActivity()).getFreeShare();
                    this.mFreeShare = FreeShareProxy.get(this.mHost.getGalleryContext(), this.mFreeShareAdapter, (ViewGroup) this.mHost.getActivity().getWindow().getDecorView().findViewById(R.id.gallery_root));
                    LayoutHelper.getNavigationBarHandler().update();
                    this.mFreeShare.setModel(this.mModel);
                    this.mIsFreeShareInit = true;
                }
                boolean supportShare = true;
                if (!(this.mCurrentPhoto == null || inSecureAlbum())) {
                    supportOperation = this.mCurrentPhoto.getSupportedOperations();
                }
                if ((supportOperation & 4) == 0) {
                    supportShare = false;
                } else if (this.mCurrentPhoto != null && (this.mCurrentPhoto instanceof GalleryMediaItem)) {
                    supportShare = ((GalleryMediaItem) this.mCurrentPhoto).canShare();
                }
                if (this.mShowBars) {
                    hideBars(true);
                }
                if (this.mShowDetails) {
                    hideDetails();
                    hideBars(true);
                }
                if (this.mIsFreeShareInit) {
                    this.mFreeShare.doShow(launchmode, supportShare);
                }
                this.mInFreeShareMode = true;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onFlingDown() {
        if (this.mFreeShareAdapter != null && this.mIsFreeShareInit && this.mFreeShare.doCancel(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                PhotoPage.this.refreshHidingMessage();
            }
        })) {
            showBars(false);
            this.mHandler.removeMessages(1);
        }
    }

    public void onSwipeImages(float velocityX, float velocityY) {
        ReportToBigData.report(96);
    }

    private void destroyFreeShare() {
        if (this.mIsFreeShareInit) {
            this.mFreeShare.doClean();
            this.mIsFreeShareInit = false;
        }
    }

    private boolean isFreeShareShowing() {
        return this.mIsFreeShareInit ? this.mFreeShare.isShowing() : false;
    }

    public boolean isFreeShareEnabled() {
        boolean supportShare = ((this.mCurrentPhoto == null ? 0 : this.mCurrentPhoto.getSupportedOperations()) & 4) != 0;
        if (!FreeShareAdapter.FREESHARE_SUPPORTED || this.mOriginalSetPathString == null || !supportShare || this.mPhotoView.getFilmMode() || inSecureAlbum()) {
            return false;
        }
        return true;
    }

    private void updatePhotoInfoView() {
        if (this.mIsActive && !this.mCalledToSimpleEditor) {
            MediaItem currentPhoto = this.mCurrentPhoto;
            boolean show = (this.mShowDetails || !isValidImage() || this.mShowBars || inEditorMode() || this.mPhotoView.getFilmMode()) ? false : !PhotoMagnifierManager.getInstance().inMagnifierMode();
            this.mPhotoInfoView.setLableVisible(show);
            if (show) {
                TraceController.traceBegin("PhotoPage.updatePhotoInfoView");
                String date = "";
                if (this.mMediaInfoTimeAllowed) {
                    long dateInMs = currentPhoto.getDateInMs();
                    if (dateInMs > 0) {
                        date = GalleryUtils.getSettingFormatShortDateDependLocal(this.mHost.getActivity(), dateInMs);
                    }
                }
                this.mPhotoInfoView.setDate(date);
                String location = "";
                double[] latlng = new double[]{0.0d, 0.0d};
                if (this.mMediaInfoLocationAllowed) {
                    currentPhoto.getLatLong(latlng);
                    if (!GalleryUtils.isValidLocation(latlng[0], latlng[1])) {
                        DetailsHelper.cancel(this);
                        this.mPhotoInfoView.setLocation(location);
                        this.mPhotoInfoView.setLocation(latlng[0], latlng[1]);
                    } else if (this.mPhotoInfoView.needResolveAddress(latlng[0], latlng[1])) {
                        this.mResolveAddressPhoto = currentPhoto;
                        this.mPhotoInfoView.setLocation("");
                        this.mPhotoInfoView.setLocation(latlng[0], latlng[1]);
                        DetailsHelper.resolveAddress(this.mHost.getGalleryContext(), latlng, this, false);
                    }
                } else {
                    this.mPhotoInfoView.setLocation(location);
                }
                TraceController.traceEnd();
            }
        }
    }

    public void onAddressAvailable(String address) {
        if (this.mIsActive && this.mCurrentPhoto == this.mResolveAddressPhoto) {
            this.mPhotoInfoView.setLocation(address);
        }
    }

    public void updateSettings() {
        this.mMediaInfoTimeAllowed = GallerySettings.getBoolean(this.mHost.getActivity(), GallerySettings.KEY_DISPLAY_TIME_INFO, false);
        this.mMediaInfoLocationAllowed = GallerySettings.getBoolean(this.mHost.getActivity(), GallerySettings.KEY_DISPLAY_LOCATION_INFO, false);
        this.mFlingUpAllowed = GallerySettings.getBoolean(this.mHost.getActivity(), GallerySettings.KEY_FREESHARE_SLIDE_UP, false);
        this.mPhotoInfoView.setDateSwitch(this.mMediaInfoTimeAllowed);
        this.mPhotoInfoView.setLocationSwitch(this.mMediaInfoLocationAllowed);
    }

    private <T> T executeAndWait(Callable<T> callable) {
        FutureTask<T> task = new FutureTask(callable);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(60, task));
        try {
            return task.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e2) {
            throw new RuntimeException(e2);
        }
    }

    public boolean inEditorMode() {
        return this.mEditorView != null && this.mEditorView.getVisibility() == 0;
    }

    public boolean inBurstMode() {
        return (this.mCurrentPhoto == null || !this.mCurrentPhoto.isBurstCover()) ? false : this.mInPluginMode;
    }

    public void onRenderFinish() {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (!PhotoPage.this.mIsPreviewMode && (PhotoPage.this.mHost.getActivity() instanceof PreviewDelgete)) {
                    ((PreviewDelgete) PhotoPage.this.mHost.getActivity()).setPreviewVisible(8);
                }
            }
        });
    }

    public void onLoadStateChange(int state) {
        int i = 1;
        if (this.mFreeShareItem != null && !this.mFreeShareItem.isFreeShareItemNeedSwitchPhoto() && this.mFreeShareItem.isFreeShareItemClicked() && this.mModel.getScreenNail(0) != null) {
            this.mFreeShareItem.setFreeShareItemClicked(false);
            if (this.mFlingUpAllowed) {
                i = 3;
            }
            triggerFreeShare(i);
        } else if (this.mCalledToSimpleEditor) {
            if (state == 2) {
                this.mHost.getStateManager().finishState(this);
            } else if (state == 1) {
                enterSimpleEditor();
            }
        }
    }

    public boolean calledToSimpleEditor() {
        return this.mCalledToSimpleEditor;
    }

    private void enterSimpleEditor() {
        if (EditorLoadLib.IS_SUPPORT_IMAGE_EDIT) {
            if (this.mShowDetails) {
                hideDetails();
            }
            if (inEditorMode()) {
                GalleryLog.w("PhotoPage", "we are in editor already");
                return;
            } else if (this.mIsEditorViewAlreadyCreate) {
                this.mHost.getGLRoot().lockRenderThread();
                try {
                    Bitmap bitmap = UIUtils.getBitmapFromScreenNail(this.mModel.getScreenNail());
                    if (bitmap != null) {
                        MediaItem item = this.mModel.getCurrentMediaItem();
                        if (item == null) {
                            item = this.mCurrentPhoto;
                        }
                        if (item == null || !this.mEditorView.setSource(item, bitmap, item.getRotation())) {
                            this.mHost.getGLRoot().unlockRenderThread();
                            return;
                        }
                        this.mPhotoView.freeTextures();
                        this.mEditorView.enterEditor();
                        this.mPhotoView.setVisibility(1);
                        hideBars(true);
                        updatePhotoInfoView();
                        this.mHost.getGLRoot().unlockRenderThread();
                        return;
                    }
                    return;
                } finally {
                    this.mHost.getGLRoot().unlockRenderThread();
                }
            } else {
                GalleryLog.w("PhotoPage", "EditorViewCreate not create");
                return;
            }
        }
        GalleryLog.w("PhotoPage", "Image editor base so missing, disable to enter editor mode.");
    }

    public void onLeaveEditorMode(Uri uri) {
        if (!this.mCalledToSimpleEditor) {
            this.mPhotoView.setVisibility(0);
            if (this.mIsActive) {
                this.mPhotoView.prepareTextures();
                showBars(true);
            }
            updateMenuOperations();
            this.mMultiScreen.requestRefreshInfo();
        } else if (!this.mIsActive || (this.mEditorView instanceof ScreenShotsEditorView)) {
            this.mHost.getActivity().finish();
        } else {
            if (this.mIsPickWithEdit) {
                Intent intent = new Intent();
                intent.setData(uri);
                this.mHost.getActivity().setResult(-1, intent);
                this.mHost.getActivity().finish();
            } else if (!(uri == null || uri.equals(this.mCurrentPhoto.getContentUri()))) {
                setCurrentPhotoByPath(this.mHost.getGalleryContext().getDataManager().findPathByUri(uri, "image/jpeg"), null, true);
            }
            if (!this.mHandler.hasMessages(81)) {
                this.mHost.getActivity().finish();
            }
        }
    }

    public int getActionBarHeight() {
        return this.mActionBar.getActionBarHeight();
    }

    public void jumpPituIfNeeded(boolean hasModify) {
        if (hasModify) {
            ContextedUtils.showToastQuickly(this.mHost.getActivity(), (int) R.string.makeup_toast, 0);
        } else {
            Intent intent = MakeupUtils.buildPituIntent(this.mHost.getActivity().getApplicationContext(), this.mCurrentPhoto.getContentUri());
            if (intent != null) {
                try {
                    this.mHost.getActivity().startActivityForResult(intent, 10);
                } catch (Exception e) {
                    Log.w("PhotoPage", "startActivity fail" + e);
                }
            }
        }
    }

    public boolean isFromLocalImage() {
        return this.mCurrentPhoto instanceof LocalImage;
    }

    private boolean moveToImage(Uri targetUri, Bitmap previewBitmap) {
        if (targetUri == null || previewBitmap == null) {
            return false;
        }
        Path path = this.mHost.getGalleryContext().getDataManager().findPathByUri(targetUri, "image/jpeg");
        MediaItem item = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(path);
        if (item == null) {
            GalleryLog.d("PhotoPage", "can't get item for path:" + path);
            return false;
        }
        setCurrentPhotoByPath(path, previewBitmap, true);
        item.setScreenNailBitmapProxy(previewBitmap);
        this.mPhotoView.setMediaItemScreenNail(item);
        this.mPhotoView.setWantPictureCenterCallbacks(false);
        return true;
    }

    private boolean moveToNewVideo(Uri targetUri) {
        if (targetUri == null) {
            return false;
        }
        Path path = this.mHost.getGalleryContext().getDataManager().findPathByUri(targetUri, "video/mp4");
        if (((MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(path)) == null) {
            GalleryLog.d("PhotoPage", "can't get item for path:" + path);
            return false;
        }
        setCurrentPhotoByPath(path, null, false);
        return true;
    }

    public boolean waitForDataLoad(Uri targetUri, Bitmap previewBitmap) {
        boolean result = false;
        long start = System.currentTimeMillis();
        if (targetUri == null) {
            return false;
        }
        MediaItem item;
        if (this.mCalledToSimpleEditor) {
            item = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(this.mHost.getGalleryContext().getDataManager().findPathByUri(targetUri, "image/jpeg"));
            if (item != null) {
                item.setScreenNailBitmapProxy(previewBitmap);
            }
            return false;
        }
        if (this.mModel instanceof PhotoDataAdapter) {
            GalleryLog.d("PhotoPage", "wait for first data load finish.");
            synchronized (this.EDIT_OBJ) {
                Utils.waitWithoutInterrupt(this.EDIT_OBJ, 3000);
            }
            GalleryLog.d("PhotoPage", "first data load has finished.");
        }
        final Path path = this.mHost.getGalleryContext().getDataManager().findPathByUri(targetUri, "image/jpeg");
        GalleryLog.d("PhotoPage", "set current photo to data adapter.");
        final Bitmap bitmap = previewBitmap;
        Boolean needWait = (Boolean) executeAndWait(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return Boolean.valueOf(PhotoPage.this.setCurrentPhotoByPath(path, bitmap, false));
            }
        });
        GalleryLog.d("PhotoPage", "set current photo to data adapter has done.");
        if (needWait == null || !needWait.booleanValue()) {
            GalleryLog.d("PhotoPage", "no need wait.");
            return false;
        }
        item = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(path);
        if (item == null) {
            GalleryLog.d("PhotoPage", "can't get item for path:" + path);
            return false;
        }
        if (item != this.mModel.getMediaItem(0)) {
            GalleryLog.d("PhotoPage", "wait for second data load finish.");
            synchronized (this.EDIT_OBJ) {
                Utils.waitWithoutInterrupt(this.EDIT_OBJ, 3000);
            }
            GalleryLog.d("PhotoPage", "second data load has finished.");
        }
        if (item == this.mModel.getMediaItem(0)) {
            GalleryLog.d("PhotoPage", "set photo view visible.");
            bitmap = previewBitmap;
            executeAndWait(new Callable<Object>() {
                public Object call() throws Exception {
                    item.setScreenNailBitmapProxy(bitmap);
                    PhotoPage.this.mPhotoView.setMediaItemScreenNail(item);
                    PhotoPage.this.mPhotoView.setWantPictureCenterCallbacks(true);
                    PhotoPage.this.mPhotoView.setVisibility(0);
                    return null;
                }
            });
            GalleryLog.d("PhotoPage", "wait for target item screen nail in center.");
            synchronized (this.EDIT_OBJ) {
                Utils.waitWithoutInterrupt(this.EDIT_OBJ, 3000);
            }
            GalleryLog.d("PhotoPage", "target item screen nail is in center now.");
            executeAndWait(new Callable<Object>() {
                public Object call() throws Exception {
                    PhotoPage.this.mPhotoView.setWantPictureCenterCallbacks(false);
                    return null;
                }
            });
            result = true;
        }
        GalleryLog.d("PhotoPage", "wait for leaving editor(ms):" + (System.currentTimeMillis() - start));
        return result;
    }

    private boolean setCurrentPhotoByPath(Path path, Bitmap proxyBitmap, boolean forceSwitch) {
        if (path == null) {
            GalleryLog.d("PhotoPage", "path is null");
            return false;
        } else if (this.mHost.getActivity() == null) {
            GalleryLog.d("PhotoPage", "activity is null");
            return false;
        } else {
            Path albumPath = this.mHost.getGalleryContext().getDataManager().getDefaultSetOf(path);
            if (albumPath == null) {
                GalleryLog.d("PhotoPage", "albumPath is null");
                return false;
            }
            boolean needForward = !this.mHost.getGalleryContext().getDataManager().ignoreForward(albumPath, this.mOriginalSetPathString) ? !albumPath.equalsIgnoreCase(this.mOriginalSetPathString) : false;
            if (!needForward ? forceSwitch : true) {
                MediaItem item = (MediaItem) this.mHost.getGalleryContext().getDataManager().getMediaObject(path);
                if (!(item == null || proxyBitmap == null)) {
                    item.setScreenNailBitmapProxy(proxyBitmap);
                }
                Bundle data = new Bundle();
                data.putBoolean("keep-from-camera", !this.mKeepFromCamera ? this.mFromCamera : true);
                data.putString("media-set-path", albumPath.toString());
                if (proxyBitmap != null) {
                    data.putParcelable("media-set-bitmap", proxyBitmap);
                } else if (item != null) {
                    data.putParcelable("media-set-bitmap", item.getScreenNailBitmapProxy());
                }
                data.putString("media-item-path", path.toString());
                data.putLong("start-taken-time", this.mData.getLong("start-taken-time", 0));
                data.putBoolean("is-secure-camera-album", this.mIsSecureAlbum);
                if (this.mFromCamera && this.mSetPathString != null) {
                    data.putString("media-set-path", this.mSetPathString);
                }
                data.putBoolean("is-free-share-item-clicked", this.mFreeShareItem == null ? false : this.mFreeShareItem.isFreeShareItemClicked());
                Message msg = this.mHandler.obtainMessage(forceSwitch ? 81 : 80);
                msg.obj = data;
                this.mHandler.sendMessage(msg);
                return false;
            }
            this.mModel.setCurrentPhoto(path, this.mCurrentIndex);
            return true;
        }
    }

    public String getToken() {
        if (this.mCalledToSimpleEditor) {
            return "from-third-party-to-editor";
        }
        if (this.mFromCamera) {
            return "from-camera";
        }
        if (this.mHost.getActivity() instanceof SinglePhotoActivity) {
            return "from-view-activity";
        }
        return "from-gallery";
    }

    public TransitionStore getTransitionStore() {
        return this.mHost.getTransitionStore();
    }

    public void share(Uri uri, IntentChooserDialogClickListener listener) {
        Path path = this.mHost.getGalleryContext().getDataManager().findPathByUri(uri, "image/jpeg");
        if (path != null) {
            ArrayList<Path> items = new ArrayList(1);
            items.add(path);
            this.mIntentChooser.setIntentChooserDialogClickListener(listener);
            Path mediaSetPath = null;
            if (this.mMediaSet != null) {
                mediaSetPath = this.mMediaSet.getPath();
            }
            this.mIntentChooser.share(this.mHost.getGalleryContext(), this.mActionBar.getCurrentMode(), this.mMenuExecutor, mediaSetPath, items);
        }
    }

    private BaseEditorView createEditorView(Bundle data) {
        return data.getBoolean("is-screen-shot-edit", false) ? new ScreenShotsEditorView(this, this.mHost.getActivity()) : new EditorView(this, this.mHost.getActivity());
    }

    private void handleMyFavoriteAction(Action action, Path path) {
        if (Action.MYFAVORITE.equalAction(action)) {
            this.mActionBar.getCurrentMode().changeAction(Action.ACTION_ID_MYFAVORITE, Action.ACTION_ID_NOT_MYFAVORITE);
        } else {
            this.mActionBar.getCurrentMode().changeAction(Action.ACTION_ID_NOT_MYFAVORITE, Action.ACTION_ID_MYFAVORITE);
        }
        this.mSelectionManager.deSelectAll();
        this.mSelectionManager.toggle(path);
        this.mMenuExecutor.onMenuClicked(action, this.mConfirmDialogListener, false, false);
    }

    public void initFilterMould() {
        synchronized (ImageFilterFx.FILTER_LOCK) {
            nativeApplyFilterInit();
        }
    }

    public void destroyMorphoFilterMould() {
        synchronized (ImageFilterFx.FILTER_LOCK) {
            destoryMorphoFilter();
        }
    }

    public void destroyFilterMould() {
        synchronized (ImageFilterFx.FILTER_LOCK) {
            nativeApplyFilterDestroy();
        }
    }

    private void createDialogIfNeeded(String defaultName, int titleID) {
        ContextThemeWrapper context = GalleryUtils.getHwThemeContext(this.mHost.getActivity(), "androidhwext:style/Theme.Emui.Dialog");
        DialogInterface.OnClickListener dialogButtonListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                PhotoShareUtils.hideSoftInput(PhotoPage.this.mSetNameTextView);
                if (-1 == which && PhotoPage.this.mSetNameTextView != null) {
                    String newFileName = PhotoPage.this.mSetNameTextView.getText().toString().trim();
                    if (PhotoPage.this.mCurrentPhoto != null && GalleryUtils.isNewFileNameLegal(PhotoPage.this.mHost.getActivity(), PhotoPage.this.mCurrentPhoto, dialog, newFileName)) {
                        Bundle data = new Bundle();
                        data.putString("key_bucket_name_alias", newFileName);
                        PhotoPage.this.mMenuExecutor.startAction(R.string.rename, R.string.rename, null, false, false, Style.NORMAL_STYLE, PhotoPage.this.mSelectionManager.getProcessingList(false), data);
                        ReportToBigData.report(106);
                    } else {
                        return;
                    }
                }
                GalleryUtils.setDialogDismissable(dialog, true);
                if (PhotoPage.this.mCreateDialog != null) {
                    GalleryUtils.setDialogDismissable(PhotoPage.this.mCreateDialog, true);
                    GalleryUtils.dismissDialogSafely(PhotoPage.this.mCreateDialog, null);
                    PhotoPage.this.mCreateDialog = null;
                }
                PhotoPage.this.mSetNameTextView = null;
            }
        };
        if (this.mCreateDialog == null || !this.mCreateDialog.isShowing()) {
            this.mSetNameTextView = new EditText(context);
            this.mSetNameTextView.setSingleLine(true);
            ColorfulUtils.decorateColorfulForEditText(this.mHost.getActivity(), this.mSetNameTextView);
            this.mCreateDialog = GalleryUtils.createDialog(context, defaultName, titleID, dialogButtonListener, null, this.mSetNameTextView);
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    PhotoShareUtils.showSoftInput(PhotoPage.this.mSetNameTextView);
                }
            }, 300);
            if (inSecureAlbum()) {
                this.mCreateDialog.getWindow().addFlags(524288);
            }
            return;
        }
        GalleryLog.d("PhotoPage", "The dialog is showing, do not create any more");
    }

    private void outputItem(Path path) {
        this.mSelectionManager.deSelectAll();
        this.mSelectionManager.toggle(path);
        this.mMenuExecutor.onMenuClicked(Action.SAVE, null, null, null);
    }

    public boolean resetImageToFullView() {
        return this.mPhotoView.resetToFullView();
    }

    public void setWantPictureFullViewCallbacks(boolean wanted, int type) {
        this.mPhotoView.setWantPictureFullViewCallbacks(wanted);
        if (wanted) {
            this.mPictureFullViewCallback = new PictureFullViewCallback(type);
        } else {
            this.mPictureFullViewCallback = null;
        }
    }

    public void switchToAllFocusFragment() {
        this.mHandler.removeMessages(20);
        this.mHandler.sendEmptyMessage(20);
    }

    public void switchTo3DViewPage() {
        this.mHandler.removeMessages(32);
        this.mHandler.sendEmptyMessage(32);
    }

    public void playLivePhoto() {
        if (this.mLivePhotoView.isPlaying()) {
            GalleryLog.d("PhotoPage", "photo is playing already.");
        } else {
            this.mLivePhotoView.start();
        }
    }

    public void switchTo3DModelPage() {
        this.mHandler.removeMessages(34);
        this.mHandler.sendEmptyMessage(34);
    }

    public void setSwipingEnabled(boolean enabled) {
        this.mPhotoView.setSwipingEnabled(enabled);
    }

    protected boolean onFingprintKeyActivated() {
        return true;
    }

    private boolean isSupportRangeMeasure(MediaItem currentPhoto) {
        if (WideAperturePhotoUtil.supportRangeMeasure() && 2 == currentPhoto.getRefocusPhotoType()) {
            return true;
        }
        return false;
    }

    public void onRangeMeasureItemClick() {
        if (resetImageToFullView()) {
            setWantPictureFullViewCallbacks(true, 3);
            return;
        }
        setWantPictureFullViewCallbacks(false, 0);
        switchToRangeMeasureFragment();
    }

    private void switchToRangeMeasureFragment() {
        this.mHandler.removeMessages(33);
        this.mHandler.sendEmptyMessage(33);
    }

    public void switchToDocRectifyPage() {
        this.mHandler.removeMessages(90);
        this.mHandler.sendEmptyMessage(90);
    }

    public boolean checkPackage(String packageName) {
        boolean isExist = false;
        try {
            isExist = MapUtils.isPackagesExist(GalleryUtils.getContext(), packageName);
        } catch (RuntimeException e) {
            GalleryLog.d("PhotoPage", "Package not found:" + e);
        }
        return isExist;
    }

    public void onSlidePicture() {
        refreshHidingMessage();
    }

    protected boolean isSyncAlbum() {
        return !(this.mMediaSet instanceof GalleryMediaTimegroupAlbum) ? this.mMediaSet instanceof MapAlbum : true;
    }

    protected boolean isHicloudAlbum() {
        return !(this.mMediaSet instanceof CloudLocalAlbum) ? this.mMediaSet instanceof DiscoverLocation : true;
    }

    public boolean isContainBarcodeScanResult() {
        boolean z = false;
        if (this.mModel.getMediaItem(0) == null) {
            return false;
        }
        BarcodeScanResultItem resultItem = this.mModel.getMediaItem(0).getBarcodeResult();
        if (resultItem == null) {
            return false;
        }
        if (resultItem.getBarcodeScanResult().length != 0) {
            z = true;
        }
        return z;
    }
}
